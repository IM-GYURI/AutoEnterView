package com.ctrls.auto_enter_view.service;

import com.ctrls.auto_enter_view.enums.ErrorCode;
import com.ctrls.auto_enter_view.exception.CustomException;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.ObjectCannedACL;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Slf4j
@RequiredArgsConstructor
@Service
public class S3ImageUploadService {

  private final S3Client s3Client;

  @Value("${cloud.aws.s3.bucket}")
  private String bucketName;

  // 허용된 파일 확장자 목록
  private final List<String> allowedExtensions = Arrays.asList("jpg", "jpeg", "png");

  // 최대 허용 파일 크기 (10MB로 설정)
  private final long maxFileSize = 10 * 1024 * 1024;

  /**
   * 이미지 파일 업로드
   *
   * @param file
   * @param directory
   * @return
   * @throws CustomException ErrorCode.INVALID_FILE_FORMAT 올바르지 않은 확장자 파일
   * @throws CustomException ErrorCode.FILE_SIZE_EXCEEDED 파일 크기 초과
   * @throws CustomException ErrorCode.S3_UPLOAD_ERROR 파일 업로드 실패
   */
  public String uploadImage(MultipartFile file, String directory) {

    // 확장자 검사
    String originalFilename = file.getOriginalFilename();
    String extension = originalFilename.substring(originalFilename.lastIndexOf(".") + 1)
        .toLowerCase();

    if (!allowedExtensions.contains(extension)) {
      throw new CustomException(ErrorCode.INVALID_FILE_FORMAT);
    }

    // 파일 크기 검사
    if (file.getSize() > maxFileSize) {
      throw new CustomException(ErrorCode.FILE_SIZE_EXCEEDED);
    }

    String fileName = UUID.randomUUID() + "." + extension;
    String key = directory + "/" + fileName;

    PutObjectRequest putObjectRequest = PutObjectRequest.builder()
        .bucket(bucketName)
        .key(key)
        .contentType(file.getContentType()) // Content-Type 설정
        .acl(ObjectCannedACL.PUBLIC_READ) // 퍼블릭 읽기 권한 설정
        .build();

    try {
      s3Client.putObject(putObjectRequest,
          RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
    } catch (IOException e) {
      throw new CustomException(ErrorCode.S3_UPLOAD_ERROR);
    }

    // S3에 업로드된 파일의 URL을 반환
    return String.format("https://%s.s3.amazonaws.com/%s", bucketName, key);
  }

  /**
   * 이미지 파일 삭제
   *
   * @param imageUrl
   * @throws CustomException ErrorCode.FAILED_TO_DELETE_IMAGE 이미지 삭제 실패
   */
  public void deleteImage(String imageUrl) {

    try {
      String key = extractKeyFromUrl(imageUrl);
      DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
          .bucket(bucketName)
          .key(key)
          .build();
      s3Client.deleteObject(deleteObjectRequest);
      log.info("이미지 삭제 성공 : {}", imageUrl);
    } catch (Exception e) {
      throw new CustomException(ErrorCode.FAILED_TO_DELETE_IMAGE);
    }
  }

  /**
   * key 추출하기
   *
   * @param imageUrl
   * @return
   * @throws CustomException ErrorCode.INVALID_IMAGE_URL 올바르지 않은 이미지 URL
   */
  private String extractKeyFromUrl(String imageUrl) {

    String bucketUrl = String.format("https://%s.s3.amazonaws.com/", bucketName);
    if (imageUrl.startsWith(bucketUrl)) {
      String key = imageUrl.substring(bucketUrl.length());
      log.info("삭제할 image key : {}", key);
      return key;
    } else {
      throw new CustomException(ErrorCode.INVALID_IMAGE_URL);
    }
  }
}