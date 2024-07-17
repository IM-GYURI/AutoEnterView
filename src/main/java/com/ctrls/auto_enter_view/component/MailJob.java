package com.ctrls.auto_enter_view.component;

import com.ctrls.auto_enter_view.entity.InterviewScheduleParticipantsEntity;
import com.ctrls.auto_enter_view.entity.MailAlarmInfoEntity;
import com.ctrls.auto_enter_view.repository.InterviewScheduleParticipantsRepository;
import com.ctrls.auto_enter_view.repository.MailAlarmInfoRepository;
import com.ctrls.auto_enter_view.service.MailAlarmInfoService;
import java.util.List;
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
  private final InterviewScheduleParticipantsRepository interviewScheduleParticipantsRepository;

  @Override
  public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
    Long mailId = jobExecutionContext.getJobDetail().getJobDataMap().getLong("mailId");
    MailAlarmInfoEntity mailAlarmInfoEntity = mailAlarmInfoRepository.findById(mailId)
        .orElseThrow(() -> new JobExecutionException("Mail not found"));

    List<InterviewScheduleParticipantsEntity> participants = interviewScheduleParticipantsRepository.findAllByInterviewScheduleKey(
        mailAlarmInfoEntity.getInterviewScheduleKey());

    mailAlarmInfoService.sendMailToCandidates(participants, mailAlarmInfoEntity);
  }
}
