package com.ctrls.auto_enter_view.dto.jobPostingStep;

import jakarta.validation.constraints.NotBlank;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EditJobPostingStepDto {

  private long currentStepId;

  @NotBlank(message = "지원자 키는 필수 입력값 입니다.")
  private List<String> candidateKey;
}