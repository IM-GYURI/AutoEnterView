package com.ctrls.auto_enter_view.dto.jobPosting;

import com.ctrls.auto_enter_view.dto.candidateList.CandidateTechStackInterviewInfoDto;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobPostingEveryInfoDto {

  private Long stepId;

  private String stepName;

  private List<CandidateTechStackInterviewInfoDto> candidateTechStackInterviewInfoDtoList;
}
