package com.ctrls.auto_enter_view.service;

import static com.ctrls.auto_enter_view.enums.ErrorCode.INTERVIEW_SCHEDULE_NOT_FOUND;
import static com.ctrls.auto_enter_view.enums.ErrorCode.JOB_POSTING_NOT_FOUND;
import static com.ctrls.auto_enter_view.enums.ErrorCode.JOB_POSTING_STEP_NOT_FOUND;
import static com.ctrls.auto_enter_view.enums.ErrorCode.MAIL_ALARM_INFO_NOT_FOUND;
import static com.ctrls.auto_enter_view.enums.ErrorCode.MAIL_ALARM_TIME_BEFORE_NOW;
import static com.ctrls.auto_enter_view.enums.ErrorCode.USER_NOT_FOUND;

import com.ctrls.auto_enter_view.component.MailComponent;
import com.ctrls.auto_enter_view.component.MailJob;
import com.ctrls.auto_enter_view.dto.mailAlarmInfo.MailAlarmInfoDto;
import com.ctrls.auto_enter_view.entity.CandidateListEntity;
import com.ctrls.auto_enter_view.entity.CompanyEntity;
import com.ctrls.auto_enter_view.entity.InterviewScheduleEntity;
import com.ctrls.auto_enter_view.entity.InterviewScheduleParticipantsEntity;
import com.ctrls.auto_enter_view.entity.JobPostingEntity;
import com.ctrls.auto_enter_view.entity.JobPostingStepEntity;
import com.ctrls.auto_enter_view.entity.MailAlarmInfoEntity;
import com.ctrls.auto_enter_view.exception.CustomException;
import com.ctrls.auto_enter_view.repository.CandidateRepository;
import com.ctrls.auto_enter_view.repository.CompanyRepository;
import com.ctrls.auto_enter_view.repository.InterviewScheduleRepository;
import com.ctrls.auto_enter_view.repository.JobPostingRepository;
import com.ctrls.auto_enter_view.repository.JobPostingStepRepository;
import com.ctrls.auto_enter_view.repository.MailAlarmInfoRepository;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerKey;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class MailAlarmInfoService {

  private final MailAlarmInfoRepository mailAlarmInfoRepository;
  private final CompanyRepository companyRepository;
  private final InterviewScheduleRepository interviewScheduleRepository;
  private final JobPostingRepository jobPostingRepository;
  private final JobPostingStepRepository jobPostingStepRepository;
  private final CandidateRepository candidateRepository;
  private final MailComponent mailComponent;
  private final Scheduler scheduler;

  /**
   * 메일 예약 생성
   *
   * @param companyKey
   * @param jobPostingKey
   * @param stepId
   * @param mailAlarmInfoDto
   */
  public void createMailAlarmInfo(String companyKey, String jobPostingKey, Long stepId,
      MailAlarmInfoDto mailAlarmInfoDto) {
    User principal = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    CompanyEntity company = findCompanyByPrincipal(principal);

    verifyCompanyOwnership(company, companyKey);

    InterviewScheduleEntity interviewScheduleEntity = interviewScheduleRepository.findByJobPostingKeyAndJobPostingStepId(
        jobPostingKey, stepId).orElseThrow(() -> new CustomException(INTERVIEW_SCHEDULE_NOT_FOUND));

    // 메일 예약 시간이 현재 시간 이후인지 확인
    if (!mailAlarmInfoDto.getMailSendDateTime().isAfter(LocalDateTime.now())) {
      throw new CustomException(MAIL_ALARM_TIME_BEFORE_NOW);
    }

    // 메일 예약 생성 및 저장
    MailAlarmInfoEntity mailAlarmInfoEntity = MailAlarmInfoEntity.builder()
        .interviewScheduleKey(interviewScheduleEntity.getInterviewScheduleKey())
        .jobPostingStepId(stepId)
        .jobPostingKey(jobPostingKey)
        .mailContent(mailAlarmInfoDto.getMailContent())
        .mailSendDateTime(mailAlarmInfoDto.getMailSendDateTime())
        .build();

    mailAlarmInfoRepository.save(mailAlarmInfoEntity);

    // 메일 예약 스케쥴링
    try {
      scheduleMailJob(mailAlarmInfoEntity);
    } catch (SchedulerException e) {
      throw new RuntimeException("Error scheduling mail job");
    }
  }

  /**
   * 예약된 메일 수정
   *
   * @param companyKey
   * @param jobPostingKey
   * @param stepId
   * @param mailAlarmInfoDto
   */
  @Transactional
  public void editMailAlarmInfo(String companyKey, String jobPostingKey, Long stepId,
      MailAlarmInfoDto mailAlarmInfoDto) {
    User principal = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    CompanyEntity company = findCompanyByPrincipal(principal);

    verifyCompanyOwnership(company, companyKey);

    InterviewScheduleEntity interviewScheduleEntity = interviewScheduleRepository.findByJobPostingKeyAndJobPostingStepId(
        jobPostingKey, stepId).orElseThrow(() -> new CustomException(INTERVIEW_SCHEDULE_NOT_FOUND));

    // 이미 생성된 예약 메일이 있는지 확인
    MailAlarmInfoEntity mailAlarmInfoEntity = mailAlarmInfoRepository.findByInterviewScheduleKey(
            interviewScheduleEntity.getInterviewScheduleKey())
        .orElseThrow(() -> new CustomException(MAIL_ALARM_INFO_NOT_FOUND));

    // 메일 예약 시간이 현재 시간 이후인지 확인
    if (!mailAlarmInfoDto.getMailSendDateTime().isAfter(LocalDateTime.now())) {
      throw new CustomException(MAIL_ALARM_TIME_BEFORE_NOW);
    }

    // 예약 메일 엔티티 수정
    mailAlarmInfoEntity.updateEntity(mailAlarmInfoDto);

    // 기존의 Quartz 스케줄링된 작업 삭제
    unscheduleMailJob(mailAlarmInfoEntity);

    // 수정된 시간에 맞춰 새로운 스케줄 설정
    try {
      scheduleMailJob(mailAlarmInfoEntity);
    } catch (SchedulerException e) {
      throw new RuntimeException("Error scheduling mail job");
    }
  }

  // 기존의 Quartz 스케줄링된 작업 삭제
  public void unscheduleMailJob(MailAlarmInfoEntity mailAlarmInfo) {
    try {
      TriggerKey triggerKey = TriggerKey.triggerKey("mailTrigger" + mailAlarmInfo.getId(),
          "mailGroup");
      scheduler.unscheduleJob(triggerKey);
    } catch (SchedulerException e) {
      log.error("Error unscheduling mail job", e);
      throw new RuntimeException("Error unscheduling mail job");
    }
  }

  // 예약 메일 스케쥴링
  public void scheduleMailJob(MailAlarmInfoEntity mailAlarmInfo) throws SchedulerException {
    JobDetail jobDetail = JobBuilder.newJob(MailJob.class)
        .withIdentity("mailJob" + mailAlarmInfo.getId(), "mailGroup")
        .usingJobData("mailId", mailAlarmInfo.getId())
        .build();

    Trigger trigger = TriggerBuilder.newTrigger()
        .withIdentity("mailTrigger" + mailAlarmInfo.getId(), "mailGroup")
        .startAt(Date.from(
            mailAlarmInfo.getMailSendDateTime().atZone(ZoneId.systemDefault()).toInstant()))
        .withSchedule(SimpleScheduleBuilder.simpleSchedule())
        .build();

    scheduler.scheduleJob(jobDetail, trigger);
  }

  // 예약 시간이 되면 지원자들에게 메일 발송 - 면접
  @Transactional
  public void sendInterviewMailToCandidates(List<InterviewScheduleParticipantsEntity> participants,
      MailAlarmInfoEntity mailAlarmInfoEntity) {
    JobPostingEntity jobPostingEntity = jobPostingRepository.findByJobPostingKey(
            mailAlarmInfoEntity.getJobPostingKey())
        .orElseThrow(() -> new CustomException(JOB_POSTING_NOT_FOUND));

    JobPostingStepEntity jobPostingStep = jobPostingStepRepository.findById(
            mailAlarmInfoEntity.getJobPostingStepId())
        .orElseThrow(() -> new CustomException(JOB_POSTING_STEP_NOT_FOUND));

    for (InterviewScheduleParticipantsEntity participant : participants) {
      // 후보자의 이메일 주소 조회
      String to = candidateRepository.findByCandidateKey(participant.getCandidateKey())
          .orElseThrow(() -> new CustomException(USER_NOT_FOUND)).getEmail();

      Duration duration = Duration.between(participant.getInterviewStartDatetime(),
          participant.getInterviewEndDatetime());
      long minutes = duration.toMinutes();

      // 메일 제목 및 내용 설정
      String subject =
          "면접 일정 알림 : " + jobPostingEntity.getTitle() + " - " + jobPostingStep.getStep();
      String text = "지원해주신 " + jobPostingEntity.getTitle() + "의 " + jobPostingStep.getStep()
          + " 면접 일정 안내드립니다.<br><br>" + "<strong>면접 일시 : "
          + participant.getInterviewStartDatetime()
          .format(DateTimeFormatter.ofPattern("yyyy-MM-dd EEEE HH:mm")) + "</strong><br><br>"
          + "면접 시간 : " + minutes + "분<br><br>"
          + mailAlarmInfoEntity.getMailContent();

      // HTML 형식으로 메일 발송
      mailComponent.sendHtmlMail(to, subject, text, true);
    }
  }

  // 예약 시간이 되면 지원자들에게 메일 발송 - 과제
  @Transactional
  public void sendTaskMailToCandidates(List<CandidateListEntity> participants,
      MailAlarmInfoEntity mailAlarmInfoEntity) {
    JobPostingEntity jobPostingEntity = jobPostingRepository.findByJobPostingKey(
            mailAlarmInfoEntity.getJobPostingKey())
        .orElseThrow(() -> new CustomException(JOB_POSTING_NOT_FOUND));

    JobPostingStepEntity jobPostingStep = jobPostingStepRepository.findById(
            mailAlarmInfoEntity.getJobPostingStepId())
        .orElseThrow(() -> new CustomException(JOB_POSTING_STEP_NOT_FOUND));

    InterviewScheduleEntity interviewScheduleEntity = interviewScheduleRepository.findByInterviewScheduleKey(
            mailAlarmInfoEntity.getInterviewScheduleKey())
        .orElseThrow(() -> new CustomException(INTERVIEW_SCHEDULE_NOT_FOUND));

    for (CandidateListEntity participant : participants) {
      // 후보자의 이메일 주소 조회
      String to = candidateRepository.findByCandidateKey(participant.getCandidateKey())
          .orElseThrow(() -> new CustomException(USER_NOT_FOUND)).getEmail();

      LocalDateTime taskLastDateTime = LocalDateTime.of(
          interviewScheduleEntity.getLastInterviewDate(), LocalTime.of(23, 59, 59));

      // 메일 제목 및 내용 설정
      String subject =
          "과제 일정 알림 : " + jobPostingEntity.getTitle() + " - " + jobPostingStep.getStep();
      String text = "지원해주신 " + jobPostingEntity.getTitle() + "의 " + jobPostingStep.getStep()
          + " 과제 일정 안내드립니다.<br><br>" + "<strong>과제 마감 일시 : "
          + taskLastDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd EEEE HH:mm"))
          + "</strong><br><br>"
          + mailAlarmInfoEntity.getMailContent();

      // HTML 형식으로 메일 발송
      mailComponent.sendHtmlMail(to, subject, text, true);
    }
  }

  // 취소 메일 발송
  public void sendCancellationMailToParticipants(InterviewScheduleEntity interviewScheduleEntity,
      List<InterviewScheduleParticipantsEntity> participants) {
    JobPostingEntity jobPostingEntity = jobPostingRepository.findByJobPostingKey(
            interviewScheduleEntity.getJobPostingKey())
        .orElseThrow(() -> new CustomException(JOB_POSTING_NOT_FOUND));

    JobPostingStepEntity jobPostingStep = jobPostingStepRepository.findById(
            participants.get(0).getJobPostingStepId())
        .orElseThrow(() -> new CustomException(JOB_POSTING_STEP_NOT_FOUND));

    for (InterviewScheduleParticipantsEntity participant : participants) {
      String to = candidateRepository.findByCandidateKey(participant.getCandidateKey())
          .orElseThrow(() -> new CustomException(USER_NOT_FOUND)).getEmail();

      String subject = "면접 일정 취소 안내 : " + jobPostingEntity.getTitle();
      String text = "예정되었던 면접 일정이 <strong>취소</strong>되었음을 안내드립니다.<br><br>"
          + "취소된 면접 정보<br>" + jobPostingEntity.getTitle() + " - " + jobPostingStep.getStep()
          + "<br> 취소된 면접 일시 : " + participant.getInterviewStartDatetime()
          .format(DateTimeFormatter.ofPattern("yyyy-MM-dd EEEE HH:mm"));

      mailComponent.sendHtmlMail(to, subject, text, true);
    }
  }

  // 사용자 인증 정보로 회사 entity 찾기
  private CompanyEntity findCompanyByPrincipal(User principal) {

    return companyRepository.findByEmail(principal.getUsername())
        .orElseThrow(() -> new CustomException(USER_NOT_FOUND));
  }

  // 회사 본인인지 확인
  private void verifyCompanyOwnership(CompanyEntity company, String companyKey) {

    if (!company.getCompanyKey().equals(companyKey)) {
      throw new CustomException(USER_NOT_FOUND);
    }
  }
}