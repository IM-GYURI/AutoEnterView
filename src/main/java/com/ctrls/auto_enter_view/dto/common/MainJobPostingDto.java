package com.ctrls.auto_enter_view.dto.common;

import com.ctrls.auto_enter_view.entity.JobPostingEntity;
import com.ctrls.auto_enter_view.enums.TechStack;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class MainJobPostingDto {

  @Getter
  @AllArgsConstructor
  @NoArgsConstructor
  @Builder
  public static class Response implements Serializable {

    private List<JobPostingMainInfo> jobPostingsList;
    private int totalPages;
    private long totalElements;
  }

  @Getter
  @AllArgsConstructor
  @NoArgsConstructor
  @Builder
  public static class JobPostingMainInfo implements Serializable{

    private String jobPostingKey;
    private String companyName;
    private String title;
    private List<TechStack> techStack;
    private LocalDate endDate;

    public static JobPostingMainInfo from(JobPostingEntity entity, String companyName,
        List<TechStack> techStack) {

      return JobPostingMainInfo.builder()
          .jobPostingKey(entity.getJobPostingKey())
          .companyName(companyName)
          .title(entity.getTitle())
          .techStack(techStack)
          .endDate(entity.getEndDate())
          .build();
    }
  }
}