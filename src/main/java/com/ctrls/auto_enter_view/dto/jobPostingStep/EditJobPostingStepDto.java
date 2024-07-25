package com.ctrls.auto_enter_view.dto.jobPostingStep;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
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

  @NotEmpty(message = "지원자 키는 필수 입력값 입니다.")
  @Size(min = 1, message = "최소 한 명의 지원자는 선택되어야 합니다.")
  private List<String> candidateKeys;
}