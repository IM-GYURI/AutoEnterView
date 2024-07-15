package com.ctrls.auto_enter_view.dto.resume;

import com.ctrls.auto_enter_view.entity.ResumeExperienceEntity;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@AllArgsConstructor
@Builder
@Getter
public class ExperienceDto {

  private String experienceName;
  private LocalDate startDate;
  private LocalDate endDate;

  public ResumeExperienceEntity toEntity(String resumeKey) {

    return ResumeExperienceEntity.builder()
        .resumeKey(resumeKey)
        .experienceName(experienceName)
        .startDate(startDate)
        .endDate(endDate)
        .build();
  }
}