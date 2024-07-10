package com.ctrls.auto_enter_view.dto.candidate;

import com.ctrls.auto_enter_view.entity.JobPostingEntity;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

public class CandidateApplyDto {

  @Getter
  @AllArgsConstructor
  @Builder
  public static class Response {

    private List<ApplyInfo> applyJobPostingsList;
    private int totalPages;
    private long totalElements;
  }

  @Getter
  @AllArgsConstructor
  @Builder
  public static class ApplyInfo {

    private String companyKey;
    private String jobPostingKey;
    private String title;
    private String companyName;
    private LocalDate startDate;
    private LocalDate endDate;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime applyDate;

    public static ApplyInfo from(JobPostingEntity entity, String companyName, LocalDateTime applyDate) {
      return ApplyInfo.builder()
          .companyKey(entity.getCompanyKey())
          .jobPostingKey(entity.getJobPostingKey())
          .title(entity.getTitle())
          .companyName(companyName)
          .startDate(entity.getStartDate())
          .endDate(entity.getEndDate())
          .applyDate(applyDate)
          .build();
    }
  }

}
