package com.ctrls.auto_enter_view.component;

import com.ctrls.auto_enter_view.entity.ApplicantEntity;
import com.ctrls.auto_enter_view.repository.ApplicantRepository;
import com.ctrls.auto_enter_view.service.FilteringService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ScoringJob implements Job {

  private final FilteringService filteringService;
  private final ApplicantRepository applicantRepository;

  @Override
  public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
    String jobPostingKey = jobExecutionContext.getJobDetail().getJobDataMap()
        .getString("jobPostingKey");

    // 해당 jobPosting의 applicantEntity 리스트를 받아오기
    List<ApplicantEntity> applicants = applicantRepository.findAllByJobPostingKey(jobPostingKey);

    for (ApplicantEntity applicantEntity : applicants) {
      // 지원자별로 이력서 점수 매기기
      filteringService.calculateResumeScore(applicantEntity.getCandidateKey(), jobPostingKey);
    }
  }
}
