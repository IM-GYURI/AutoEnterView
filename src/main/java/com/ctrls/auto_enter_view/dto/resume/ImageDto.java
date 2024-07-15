package com.ctrls.auto_enter_view.dto.resume;

import com.ctrls.auto_enter_view.entity.ResumeImageEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@AllArgsConstructor
@Builder
@Getter
public class ImageDto {

  private String fileName;
  private String originalFileName;
  private String filePath;

  public ResumeImageEntity toEntity(String resumeKey) {

    return ResumeImageEntity.builder()
        .resumeKey(resumeKey)
        .fileName(fileName)
        .originalFileName(originalFileName)
        .filePath(filePath)
        .build();
  }
}