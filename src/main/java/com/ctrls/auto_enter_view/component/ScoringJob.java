package com.ctrls.auto_enter_view.component;

import com.ctrls.auto_enter_view.service.ScoringService;
import lombok.RequiredArgsConstructor;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ScoringJob implements Job {

  private final ScoringService scoringService;

  @Override
  public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
    String jobPostingKey = jobExecutionContext.getJobDetail().getJobDataMap()
        .getString("jobPostingKey");

    scoringService.scoreApplicants(jobPostingKey);
  }
}
