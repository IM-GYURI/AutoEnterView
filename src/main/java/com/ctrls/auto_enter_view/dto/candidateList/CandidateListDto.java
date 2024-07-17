package com.ctrls.auto_enter_view.dto.candidateList;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CandidateListDto {

  private String candidateKey;

  private String candidateName;

}
