package com.ctrls.auto_enter_view.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.ctrls.auto_enter_view.component.S3ImageUpload;
import com.ctrls.auto_enter_view.dto.jobPosting.JobPostingDto;
import com.ctrls.auto_enter_view.entity.JobPostingImageEntity;
import com.ctrls.auto_enter_view.repository.JobPostingImageRepository;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

@ExtendWith(MockitoExtension.class)
class JobPostingImageServiceTest {

  @Mock
  private JobPostingImageRepository jobPostingImageRepository;

  @Mock
  private S3ImageUpload s3ImageUpload;

  @InjectMocks
  private JobPostingImageService jobPostingImageService;

  @Test
  @DisplayName("이미지 파일 업로드 테스트")
  void uploadImage() {
    // given
    MultipartFile image = mock(MultipartFile.class);
    String jobPostingKey = "jobPostingKey";
    String companyImageUrl = "https://example.com/image.jpg";

    when(jobPostingImageRepository.findByJobPostingKey(jobPostingKey))
        .thenReturn(Optional.empty());
    when(s3ImageUpload.uploadImage(image, "job-posting-images")).thenReturn(companyImageUrl);

    ArgumentCaptor<JobPostingImageEntity> entityCaptor = ArgumentCaptor.forClass(JobPostingImageEntity.class);

    // when
    JobPostingDto.Response response = jobPostingImageService.uploadImage(image, jobPostingKey);

    // then
    assertNotNull(response);
    assertEquals(jobPostingKey, response.getJobPostingKey());
    assertEquals(companyImageUrl, response.getJobPostingImageUrl());

    verify(jobPostingImageRepository, times(1)).save(entityCaptor.capture());
    JobPostingImageEntity savedEntity = entityCaptor.getValue();
    assertEquals(jobPostingKey, savedEntity.getJobPostingKey());
    assertEquals(companyImageUrl, savedEntity.getCompanyImageUrl());
  }

  @Test
  @DisplayName("채용공고 이미지 조회 테스트")
  void getJobPostingImage() {
    // given
    String jobPostingKey = "jobPostingKey";
    String companyImageUrl = "https://example.com/image.jpg";

    JobPostingImageEntity imageEntity = JobPostingImageEntity.builder()
        .jobPostingKey(jobPostingKey)
        .companyImageUrl(companyImageUrl)
        .build();

    when(jobPostingImageRepository.findByJobPostingKey(jobPostingKey))
        .thenReturn(Optional.of(imageEntity));

    // when
    JobPostingDto.Response response = jobPostingImageService.getJobPostingImage(jobPostingKey);

    // then
    assertNotNull(response);
    assertEquals(jobPostingKey, response.getJobPostingKey());
    assertEquals(companyImageUrl, response.getJobPostingImageUrl());
  }

  @Test
  @DisplayName("이미지 파일 삭제 테스트")
  void deleteImage() {
    // given
    String jobPostingKey = "jobPostingKey";
    String companyImageUrl = "https://example.com/image.jpg";

    JobPostingImageEntity imageEntity = JobPostingImageEntity.builder()
        .jobPostingKey(jobPostingKey)
        .companyImageUrl(companyImageUrl)
        .build();

    when(jobPostingImageRepository.findByJobPostingKey(jobPostingKey))
        .thenReturn(Optional.of(imageEntity));

    // when
    jobPostingImageService.deleteImage(jobPostingKey);

    // then
    verify(s3ImageUpload, times(1)).deleteImage(companyImageUrl);
    verify(jobPostingImageRepository, times(1)).delete(imageEntity);
  }
}