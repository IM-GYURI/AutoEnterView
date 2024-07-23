package com.ctrls.auto_enter_view.dto.candidateList;

import com.ctrls.auto_enter_view.enums.TechStack;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CandidateTechStackInterviewInfoDto {

  private String candidateKey;

  private String candidateName;

  private String resumeKey;

  private List<TechStack> techStack;

  @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
  private LocalDateTime scheduleDateTime;
}