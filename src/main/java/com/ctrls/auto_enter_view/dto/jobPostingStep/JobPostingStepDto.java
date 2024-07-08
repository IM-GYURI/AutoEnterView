package com.ctrls.auto_enter_view.dto.jobPostingStep;

import com.ctrls.auto_enter_view.entity.JobPostingStepEntity;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobPostingStepDto {

  private Long stepId;

  private String stepName;

  // Entity -> DTO 변환
  public static JobPostingStepDto fromEntity(JobPostingStepEntity entity) {

    return JobPostingStepDto.builder()
        .stepId(entity.getId())
        .stepName(entity.getStep())
        .build();
  }

  // Entity 리스트 -> DTO 리스트 변환
  public static List<JobPostingStepDto> fromEntityList(List<JobPostingStepEntity> entities) {

    return entities.stream()
        .map(JobPostingStepDto::fromEntity)
        .toList();
  }
}