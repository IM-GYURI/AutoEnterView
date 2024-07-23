package com.ctrls.auto_enter_view.dto.resume;

import com.ctrls.auto_enter_view.entity.ResumeExperienceEntity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@AllArgsConstructor
@Builder
@Getter
public class ExperienceDto {

  @NotBlank(message = "경험 이름은 필수 입력값 입니다.")
  private String experienceName;

  @NotNull(message = "시작일은 필수 입력값 입니다.")
  private LocalDate startDate;

  @NotNull(message = "종료일은 필수 입력값 입니다.")
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