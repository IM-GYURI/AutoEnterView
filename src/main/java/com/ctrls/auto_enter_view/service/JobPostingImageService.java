package com.ctrls.auto_enter_view.service;

import com.ctrls.auto_enter_view.dto.jobPosting.JobPostingDto;
import com.ctrls.auto_enter_view.entity.JobPostingImageEntity;
import com.ctrls.auto_enter_view.repository.JobPostingImageRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
public class JobPostingImageService {

  private final JobPostingImageRepository jobPostingImageRepository;
  private final S3ImageUploadService s3ImageUploadService;

  // 이미지 파일 업로드
  public JobPostingDto.Response uploadImage(MultipartFile image, String jobPostingKey) {
    JobPostingImageEntity jobPostingImage = jobPostingImageRepository.findByJobPostingKey(jobPostingKey)
        .orElseGet(() -> JobPostingImageEntity.builder()
            .jobPostingKey(jobPostingKey)
            .build());

    // 기존 이미지가 있다면 삭제
    if (jobPostingImage.getCompanyImageUrl() != null) {
      s3ImageUploadService.deleteImage(jobPostingImage.getCompanyImageUrl());
    }

    String imageUrl = s3ImageUploadService.uploadImage(image, "job-posting-images");
    jobPostingImage.updateCompanyImageUrl(imageUrl);
    jobPostingImageRepository.save(jobPostingImage);

    return new JobPostingDto.Response(jobPostingKey, imageUrl);
  }

  // 이미지 파일 조회 -> URL 반환
  public String getImageUrl(String jobPostingKey) {
    Optional<JobPostingImageEntity> imageEntityOpt = jobPostingImageRepository.findByJobPostingKey(jobPostingKey);

    return imageEntityOpt.map(JobPostingImageEntity::getCompanyImageUrl).orElse(null);
  }

  // 이미지 파일 조회 -> Response 반환
  public JobPostingDto.Response getJobPostingImage(String jobPostingKey) {
    return jobPostingImageRepository.findByJobPostingKey(jobPostingKey)
        .map(image -> new JobPostingDto.Response(jobPostingKey, image.getCompanyImageUrl()))
        .orElse(new JobPostingDto.Response(jobPostingKey, null));
  }

  // 이미지 파일 삭제
  public void deleteImage(String jobPostingKey) {
    jobPostingImageRepository.findByJobPostingKey(jobPostingKey)
        .ifPresent(image -> {
          String imageUrl = image.getCompanyImageUrl();
          s3ImageUploadService.deleteImage(imageUrl);
          jobPostingImageRepository.delete(image);
        });
  }

}