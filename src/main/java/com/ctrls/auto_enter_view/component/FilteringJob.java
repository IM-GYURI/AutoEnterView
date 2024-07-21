package com.ctrls.auto_enter_view.component;

import com.ctrls.auto_enter_view.service.FilteringService;
import lombok.RequiredArgsConstructor;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FilteringJob implements Job {

  private final FilteringService filteringService;

  @Override
  public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
    String jobPostingKey = jobExecutionContext.getJobDetail().getJobDataMap()
        .getString("jobPostingKey");

    try {
      filteringService.filterCandidates(jobPostingKey);
    } catch (Exception e) {
      throw new JobExecutionException("Failed to filter candidates", e);
    }
  }
}
