package com.ctrls.auto_enter_view.dto.jobPosting;

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

  private String jobCategory;

  private LocalDate startDate;

  private LocalDate endDate;
}
