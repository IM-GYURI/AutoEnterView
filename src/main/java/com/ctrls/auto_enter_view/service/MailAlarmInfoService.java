package com.ctrls.auto_enter_view.service;

import static com.ctrls.auto_enter_view.enums.ErrorCode.FAILED_MAIL_SCHEDULING;
import static com.ctrls.auto_enter_view.enums.ErrorCode.FAILED_MAIL_UNSCHEDULING;
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
import org.springframework.security.core.userdetails.UserDetails;
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
   * @param companyKey       회사 KEY
   * @param jobPostingKey    채용 공고 KEY
   * @param stepId           채용 단계 ID
   * @param mailAlarmInfoDto 메일 발송 예약 정보 DTO
   * @throws CustomException INTERVIEW_SCHEDULE_NOT_FOUND 면접 일정 없음 일정이 없음
   * @throws CustomException MAIL_ALARM_TIME_BEFORE_NOW 메일 예약 시간이 과거임
   * @throws CustomException FAILED_MAIL_SCHEDULING 메일 예약 등록을 실패함
   */
  public void createMailAlarmInfo(String companyKey, String jobPostingKey, Long stepId,
      MailAlarmInfoDto mailAlarmInfoDto, UserDetails userDetails) {

    CompanyEntity company = findCompanyByPrincipal(userDetails);

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
      throw new CustomException(FAILED_MAIL_SCHEDULING);
    }
  }

  /**
   * 예약된 메일 수정
   *
   * @param companyKey       회사 KEY
   * @param jobPostingKey    채용 공고 KEY
   * @param stepId           채용 단계 ID
   * @param mailAlarmInfoDto 메일 발송 예약 정보 DTO
   * @throws CustomException INTERVIEW_SCHEDULE_NOT_FOUND 면접 일정 없음 일정이 없음
   * @throws CustomException MAIL_ALARM_INFO_NOT_FOUND 메일 예약이 없음
   * @throws CustomException MAIL_ALARM_TIME_BEFORE_NOW 메일 예약 시간이 과거임
   * @throws CustomException FAILED_MAIL_SCHEDULING 메일 예약 등록을 실패함
   */
  @Transactional
  public void editMailAlarmInfo(String companyKey, String jobPostingKey, Long stepId,
      MailAlarmInfoDto mailAlarmInfoDto, UserDetails userDetails) {

    CompanyEntity company = findCompanyByPrincipal(userDetails);

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
      throw new CustomException(FAILED_MAIL_SCHEDULING);
    }
  }

  /**
   * 기존의 Quartz 스케줄링된 작업 삭제
   *
   * @param mailAlarmInfo 메일 예약 정보 ENTITY
   * @throws CustomException FAILED_MAIL_UNSCHEDULING 메일 예약 취소를 실패함
   */
  public void unscheduleMailJob(MailAlarmInfoEntity mailAlarmInfo) {

    try {
      TriggerKey triggerKey = TriggerKey.triggerKey("mailTrigger" + mailAlarmInfo.getId(),
          "mailGroup");
      scheduler.unscheduleJob(triggerKey);
    } catch (SchedulerException e) {
      log.error("Error unscheduling mail job", e);
      throw new CustomException(FAILED_MAIL_UNSCHEDULING);
    }
  }

  // 예약 메일 스케쥴링
  private void scheduleMailJob(MailAlarmInfoEntity mailAlarmInfo) throws SchedulerException {

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

  /**
   * 예약 시간이 되면 지원자들에게 메일 발송 - 면접
   *
   * @param participants        일정 참가자 리스트
   * @param mailAlarmInfoEntity 메일 예약 정보 ENTITY
   * @throws CustomException JOB_POSTING_NOT_FOUND 채용 공고 없음
   * @throws CustomException JOB_POSTING_STEP_NOT_FOUND 채용 단계 없음
   * @throws CustomException USER_NOT_FOUND 사용자 없음
   */
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

  /**
   * 예약 시간이 되면 지원자들에게 메일 발송 - 과제
   *
   * @param participants        일정 참가자 리스트
   * @param mailAlarmInfoEntity 메일 예약 정보 ENTITY
   * @throws CustomException JOB_POSTING_NOT_FOUND 채용 공고 없음
   * @throws CustomException JOB_POSTING_STEP_NOT_FOUND 채용 단계 없음
   * @throws CustomException INTERVIEW_SCHEDULE_NOT_FOUND 면접 일정 없음
   * @throws CustomException USER_NOT_FOUND 사용자 없음
   */
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

  /**
   * 취소 메일 발송
   *
   * @param interviewScheduleEntity 면접 일정 ENTITY
   * @param participants            일정 참가자 리스트
   * @throws CustomException JOB_POSTING_NOT_FOUND 채용 공고 없음
   * @throws CustomException JOB_POSTING_STEP_NOT_FOUND 채용 단계 없음
   * @throws CustomException USER_NOT_FOUND 사용자 없음
   */
  public void sendCancellationMailToParticipants(InterviewScheduleEntity interviewScheduleEntity,
      List<InterviewScheduleParticipantsEntity> participants) {
    // 과제인지 면접인지 구분
    // 과제라면 firstInterviewDate가 null
    // 면접이라면 firstInterviewDate가 null이 아님
    // isTask = true -> 과제
    // isTask = false -> 면접
    boolean isTask = interviewScheduleEntity.getFirstInterviewDate() == null;

    JobPostingEntity jobPostingEntity = jobPostingRepository.findByJobPostingKey(
            interviewScheduleEntity.getJobPostingKey())
        .orElseThrow(() -> new CustomException(JOB_POSTING_NOT_FOUND));

    JobPostingStepEntity jobPostingStep = jobPostingStepRepository.findById(
            participants.get(0).getJobPostingStepId())
        .orElseThrow(() -> new CustomException(JOB_POSTING_STEP_NOT_FOUND));

    if (isTask) {
      // 과제일 경우
      for (InterviewScheduleParticipantsEntity participant : participants) {
        String to = candidateRepository.findByCandidateKey(participant.getCandidateKey())
            .orElseThrow(() -> new CustomException(USER_NOT_FOUND)).getEmail();

        String subject = "과제 취소 안내 : " + jobPostingEntity.getTitle();
        String text = "과제 일정이 <strong>취소</strong>되었음을 안내드립니다.<br><br>"
            + "취소된 과제 정보<br>" + jobPostingEntity.getTitle() + " - " + jobPostingStep.getStep()
            + "<br> 취소된 과제 마감 일시 : " + participant.getInterviewEndDatetime()
            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd EEEE HH:mm"))
            + "<br><br>";

        mailComponent.sendHtmlMail(to, subject, text, true);
      }
    } else {
      // 면접일 경우
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
  }

  /**
   * 사용자 인증 정보로 회사 entity 찾기
   *
   * @param userDetails 사용자 정보
   * @return 회사 ENTITY
   * @throws CustomException USER_NOT_FOUND 사용자 없음
   */
  private CompanyEntity findCompanyByPrincipal(UserDetails userDetails) {

    return companyRepository.findByEmail(userDetails.getUsername())
        .orElseThrow(() -> new CustomException(USER_NOT_FOUND));
  }

  /**
   * 회사 본인인지 확인
   *
   * @param company    회사 ENTITY
   * @param companyKey 회사 KEY
   * @throws CustomException USER_NOT_FOUND 사용자 없음
   */
  private void verifyCompanyOwnership(CompanyEntity company, String companyKey) {

    if (!company.getCompanyKey().equals(companyKey)) {
      throw new CustomException(USER_NOT_FOUND);
    }
  }
}