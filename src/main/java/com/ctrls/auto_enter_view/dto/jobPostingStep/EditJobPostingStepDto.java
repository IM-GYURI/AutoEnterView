package com.ctrls.auto_enter_view.dto.jobPostingStep;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EditJobPostingStepDto {
  private Long currentStepId;
  private String candidateKey;
}