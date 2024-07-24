package com.ctrls.auto_enter_view.service;

import com.ctrls.auto_enter_view.dto.resume.ResumeDto;
import com.ctrls.auto_enter_view.entity.ResumeImageEntity;
import com.ctrls.auto_enter_view.repository.ResumeImageRepository;
import com.ctrls.auto_enter_view.repository.ResumeRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ResumeImageService {

  private final ResumeImageRepository resumeImageRepository;
  private final S3ImageUploadService s3ImageUploadService;
  private final ResumeRepository resumeRepository;

  /**
   * 이미지 파일 업로드
   *
   * @param image
   * @param resumeKey
   * @return
   */
  public ResumeDto.Response uploadImage(MultipartFile image, String resumeKey) {

    ResumeImageEntity resumeImage = resumeImageRepository.findByResumeKey(resumeKey)
        .orElseGet(() -> ResumeImageEntity.builder()
            .resumeKey(resumeKey)
            .build());

    // 기존 이미지가 있고, 새 이미지가 제공된 경우에만 기존 이미지 삭제
    if (resumeImage.getResumeImageUrl() != null) {
      s3ImageUploadService.deleteImage(resumeImage.getResumeImageUrl());
    }

    String imageUrl = s3ImageUploadService.uploadImage(image, "resume-images");
    resumeImage.updateResumeImageUrl(imageUrl);
    resumeImageRepository.save(resumeImage);

    return new ResumeDto.Response(resumeKey, imageUrl);
  }

  /**
   * 기존 이미지 정보 반환 -> 업데이트 시 확인
   *
   * @param resumeKey
   * @return
   */
  public ResumeDto.Response getExistingResumeImage(String resumeKey) {

    Optional<ResumeImageEntity> resumeImageOpt = resumeImageRepository.findByResumeKey(resumeKey);

    if (resumeImageOpt.isPresent()) {
      ResumeImageEntity resumeImage = resumeImageOpt.get();
      return new ResumeDto.Response(resumeKey, resumeImage.getResumeImageUrl());
    } else {
      return new ResumeDto.Response(resumeKey, null);
    }
  }

  // 이미지 파일 삭제
  public void deleteImage(String candidateKey) {

    resumeRepository.findByCandidateKey(candidateKey)
        .ifPresent(resume -> {
          String resumeKey = resume.getResumeKey();

          resumeImageRepository.findByResumeKey(resumeKey)
              .ifPresent(image -> {
                String imageUrl = image.getResumeImageUrl();
                s3ImageUploadService.deleteImage(imageUrl);
                resumeImageRepository.delete(image);
              });
        });
  }
}