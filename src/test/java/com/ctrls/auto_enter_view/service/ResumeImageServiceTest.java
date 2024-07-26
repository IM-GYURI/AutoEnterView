package com.ctrls.auto_enter_view.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ctrls.auto_enter_view.component.KeyGenerator;
import com.ctrls.auto_enter_view.component.S3ImageUpload;
import com.ctrls.auto_enter_view.dto.resume.ResumeDto;
import com.ctrls.auto_enter_view.entity.ResumeEntity;
import com.ctrls.auto_enter_view.entity.ResumeImageEntity;
import com.ctrls.auto_enter_view.repository.ResumeImageRepository;
import com.ctrls.auto_enter_view.repository.ResumeRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.multipart.MultipartFile;

class ResumeImageServiceTest {

  @Mock
  private KeyGenerator keyGenerator;

  @Mock
  private ResumeImageRepository resumeImageRepository;

  @Mock
  private S3ImageUpload s3ImageUpload;

  @Mock
  private ResumeRepository resumeRepository;

  @InjectMocks
  private ResumeImageService resumeImageService;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  @DisplayName("새 이미지 업로드 성공 테스트")
  void uploadImage_NewImage() {
    // Given
    MultipartFile mockFile = mock(MultipartFile.class);
    String resumeKey = "testResumeKey";
    String imageUrl = "http://test-image-url.com";

    when(keyGenerator.generateKey()).thenReturn(resumeKey);
    when(resumeImageRepository.findByResumeKey(resumeKey)).thenReturn(Optional.empty());
    when(s3ImageUpload.uploadImage(mockFile, "resume-images")).thenReturn(imageUrl);

    // When
    ResumeDto.Response response = resumeImageService.uploadImage(mockFile, resumeKey);

    // Then
    assertEquals(resumeKey, response.getResumeKey());
    assertEquals(imageUrl, response.getResumeImageUrl());
    verify(resumeImageRepository).save(argThat(entity ->
        entity.getResumeKey().equals(resumeKey) &&
            entity.getResumeImageUrl().equals(imageUrl)));
  }

  @Test
  @DisplayName("기존 이미지 수정 성공 테스트")
  void uploadImage_ExistingImage() {
    // Given
    MultipartFile mockFile = mock(MultipartFile.class);
    String resumeKey = "testResumeKey";
    String oldImageUrl = "http://old-image-url.com";
    String newImageUrl = "http://new-image-url.com";

    ResumeImageEntity existingImage = ResumeImageEntity.builder()
        .resumeKey(resumeKey)
        .resumeImageUrl(oldImageUrl)
        .build();

    when(keyGenerator.generateKey()).thenReturn(resumeKey);
    when(resumeImageRepository.findByResumeKey(resumeKey)).thenReturn(Optional.of(existingImage));
    when(s3ImageUpload.uploadImage(mockFile, "resume-images")).thenReturn(newImageUrl);

    // When
    ResumeDto.Response response = resumeImageService.uploadImage(mockFile, resumeKey);

    // Then
    assertEquals(resumeKey, response.getResumeKey());
    assertEquals(newImageUrl, response.getResumeImageUrl());
    verify(s3ImageUpload).deleteImage(oldImageUrl);
    verify(resumeImageRepository).save(existingImage);
  }

  @Test
  @DisplayName("기존 이미지 조회 성공 테스트")
  void getExistingResumeImage_Exists() {
    // Given
    String resumeKey = "testResumeKey";
    String imageUrl = "http://test-image-url.com";
    ResumeImageEntity resumeImage = ResumeImageEntity.builder()
        .resumeKey(resumeKey)
        .resumeImageUrl(imageUrl)
        .build();

    when(keyGenerator.generateKey()).thenReturn(resumeKey);
    when(resumeImageRepository.findByResumeKey(resumeKey)).thenReturn(Optional.of(resumeImage));

    // When
    ResumeDto.Response response = resumeImageService.getExistingResumeImage(resumeKey);

    // Then
    assertEquals(resumeKey, response.getResumeKey());
    assertEquals(imageUrl, response.getResumeImageUrl());
  }

  @Test
  @DisplayName("이미지 삭제 성공 테스트")
  void deleteImage_Exists() {
    // Given
    String candidateKey = "testCandidateKey";
    String resumeKey = "testResumeKey";
    String imageUrl = "http://test-image-url.com";

    ResumeEntity resume = ResumeEntity.builder().resumeKey(resumeKey).build();
    ResumeImageEntity resumeImage = ResumeImageEntity.builder()
        .resumeKey(resumeKey)
        .resumeImageUrl(imageUrl)
        .build();

    when(resumeRepository.findByCandidateKey(candidateKey)).thenReturn(Optional.of(resume));
    when(resumeImageRepository.findByResumeKey(resumeKey)).thenReturn(Optional.of(resumeImage));

    // When
    resumeImageService.deleteImage(candidateKey);

    // Then
    verify(s3ImageUpload).deleteImage(imageUrl);
    verify(resumeImageRepository).delete(resumeImage);
  }
}