package com.ctrls.auto_enter_view.dto.jobPostingStep;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobPostingStepsDto {

  private String jobPostingKey;

  private List<JobPostingStepDto> jobPostingSteps;
}
