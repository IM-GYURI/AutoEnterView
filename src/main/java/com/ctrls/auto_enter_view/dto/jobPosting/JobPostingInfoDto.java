package com.ctrls.auto_enter_view.dto.jobPosting;

import com.ctrls.auto_enter_view.entity.JobPostingEntity;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobPostingInfoDto {

  private String jobPostingKey;

  private String title;

  private Integer career;

  private LocalDate endDate;

  public static JobPostingInfoDto fromEntity(JobPostingEntity jobPostingEntity) {
    return JobPostingInfoDto.builder()
        .jobPostingKey(jobPostingEntity.getJobPostingKey())
        .title(jobPostingEntity.getTitle())
        .career(jobPostingEntity.getCareer())
        .endDate(jobPostingEntity.getEndDate())
        .build();
  }
}
