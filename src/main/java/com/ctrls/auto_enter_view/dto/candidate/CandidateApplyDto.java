package com.ctrls.auto_enter_view.dto.candidate;

import com.ctrls.auto_enter_view.entity.AppliedJobPostingEntity;
import java.time.LocalDate;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

public class CandidateApplyDto {

  @Getter
  @AllArgsConstructor
  @Builder
  public static class Response {

    private List<ApplyInfo> appliedJobPostingsList;
    private int totalPages;
    private long totalElements;
  }

  @Getter
  @AllArgsConstructor
  @Builder
  public static class ApplyInfo {

    private String jobPostingKey;
    private LocalDate startDate;
    private String stepName;
    private String title;

    public static ApplyInfo from(AppliedJobPostingEntity appliedJobPostingEntity) {

      return ApplyInfo.builder()
          .jobPostingKey(appliedJobPostingEntity.getJobPostingKey())
          .startDate(appliedJobPostingEntity.getStartDate())
          .stepName(appliedJobPostingEntity.getStepName())
          .title(appliedJobPostingEntity.getTitle())
          .build();
    }
  }
}