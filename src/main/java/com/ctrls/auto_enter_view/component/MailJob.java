package com.ctrls.auto_enter_view.component;

import com.ctrls.auto_enter_view.entity.CandidateListEntity;
import com.ctrls.auto_enter_view.entity.InterviewScheduleEntity;
import com.ctrls.auto_enter_view.entity.InterviewScheduleParticipantsEntity;
import com.ctrls.auto_enter_view.entity.MailAlarmInfoEntity;
import com.ctrls.auto_enter_view.repository.CandidateListRepository;
import com.ctrls.auto_enter_view.repository.InterviewScheduleParticipantsRepository;
import com.ctrls.auto_enter_view.repository.InterviewScheduleRepository;
import com.ctrls.auto_enter_view.repository.MailAlarmInfoRepository;
import com.ctrls.auto_enter_view.service.MailAlarmInfoService;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MailJob implements Job {

  private final MailAlarmInfoService mailAlarmInfoService;
  private final MailAlarmInfoRepository mailAlarmInfoRepository;
  private final InterviewScheduleRepository interviewScheduleRepository;
  private final CandidateListRepository candidateListRepository;
  private final InterviewScheduleParticipantsRepository interviewScheduleParticipantsRepository;

  @Override
  public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
    Long mailId = jobExecutionContext.getJobDetail().getJobDataMap().getLong("mailId");
    MailAlarmInfoEntity mailAlarmInfoEntity = mailAlarmInfoRepository.findById(mailId)
        .orElseThrow(() -> new JobExecutionException("Mail not found"));

    String interviewScheduleKey = mailAlarmInfoEntity.getInterviewScheduleKey();
    Optional<InterviewScheduleEntity> interviewScheduleEntity = interviewScheduleRepository.findByInterviewScheduleKey(
        interviewScheduleKey);

    boolean isTask = interviewScheduleEntity.isPresent()
        && interviewScheduleEntity.get().getFirstInterviewDate() == null;
    boolean isInterview = interviewScheduleEntity.isPresent()
        && interviewScheduleEntity.get().getFirstInterviewDate() != null;

    if (isTask) {
      // 과제일 경우
      List<CandidateListEntity> participants = candidateListRepository.findAllByJobPostingKeyAndJobPostingStepId(
          mailAlarmInfoEntity.getJobPostingKey(), mailAlarmInfoEntity.getJobPostingStepId());

      mailAlarmInfoService.sendTaskMailToCandidates(participants, mailAlarmInfoEntity);
    }

    if (isInterview) {
      // 면접일 경우
      List<InterviewScheduleParticipantsEntity> participants = interviewScheduleParticipantsRepository.findAllByInterviewScheduleKey(
          mailAlarmInfoEntity.getInterviewScheduleKey());

      mailAlarmInfoService.sendInterviewMailToCandidates(participants, mailAlarmInfoEntity);
    }
  }
}
