package com.ctrls.auto_enter_view.service;

import com.ctrls.auto_enter_view.dto.candidateList.CandidateListDto;
import com.ctrls.auto_enter_view.dto.interviewschedule.InterviewScheduleDto.Request;
import com.ctrls.auto_enter_view.dto.interviewschedule.InterviewScheduleParticipantsDto;
import com.ctrls.auto_enter_view.dto.interviewschedule.InterviewScheduleParticipantsDto.Response;
import com.ctrls.auto_enter_view.entity.InterviewScheduleEntity;
import com.ctrls.auto_enter_view.entity.InterviewScheduleParticipantsEntity;
import com.ctrls.auto_enter_view.entity.MailAlarmInfoEntity;
import com.ctrls.auto_enter_view.enums.ErrorCode;
import com.ctrls.auto_enter_view.exception.CustomException;
import com.ctrls.auto_enter_view.repository.CandidateListRepository;
import com.ctrls.auto_enter_view.repository.InterviewScheduleParticipantsRepository;
import com.ctrls.auto_enter_view.repository.InterviewScheduleRepository;
import com.ctrls.auto_enter_view.repository.MailAlarmInfoRepository;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
@Slf4j
public class InterviewScheduleParticipantsService {

  private final InterviewScheduleParticipantsRepository interviewScheduleParticipantsRepository;

  private final CandidateListRepository candidateListRepository;

  private final InterviewScheduleRepository interviewScheduleRepository;

  private final MailAlarmInfoRepository mailAlarmInfoRepository;

  private final MailAlarmInfoService mailAlarmInfoService;

  /**
   * 개인 면접 일정 생성
   *
   * @param jobPostingKey
   * @param stepId
   * @param request
   */
  public void createInterviewSchedule(String jobPostingKey, Long stepId, List<Request> request) {

    String interviewScheduleKey = interviewScheduleRepository.findInterviewScheduleKeyByJobPostingKey(
        jobPostingKey).orElseThrow(() -> new CustomException(ErrorCode.JOB_POSTING_KEY_NOT_FOUND));

    List<String> candidateKeyList = candidateListRepository.findCandidateKeyByJobPostingKeyAndJobPostingStepId(
        jobPostingKey, stepId);

    List<CandidateListDto> candidateListDtoList = candidateKeyList.stream()
        .map(e -> CandidateListDto.builder()
            .candidateKey(e)
            .candidateName(candidateListRepository.findCandidateNameByCandidateKey(e))
            .build())
        .toList();

    int candidateListIndex = candidateListDtoList.size() - 1;

    for (Request requested : request) {
      LocalDateTime start = LocalDateTime.of(requested.getStartDate(), requested.getStartTime());

      for (int j = 0; j < requested.getTimes(); j++) {
        LocalDateTime startDateTime = start;
        start = start.plusMinutes(requested.getTerm());
        LocalDateTime endDatetime = start;

        if (candidateListIndex < 0) {
          throw new CustomException(ErrorCode.CANDIDATE_INADEQUATE_ERROR);
        }

        interviewScheduleParticipantsRepository.save(Request.toParticipantsEntity(
            jobPostingKey, interviewScheduleKey, stepId, startDateTime, endDatetime,
            candidateListDtoList.get(candidateListIndex).getCandidateKey(),
            candidateListDtoList.get(candidateListIndex).getCandidateName()));

        candidateListIndex--;
      }
    }
  }

  /**
   * 개인 면접 일정 전체 조회
   *
   * @param jobPostingKey
   * @param stepId
   * @return
   */
  public List<Response> getAllInterviewSchedule(String jobPostingKey, Long stepId) {

    List<InterviewScheduleParticipantsEntity> entities = interviewScheduleParticipantsRepository.findAllByJobPostingKeyAndJobPostingStepId(
        jobPostingKey,
        stepId);

    return entities.stream().map(Response::fromEntity).toList();
  }

  /**
   * 개인 면접 일정 수정
   *
   * @param interviewScheduleKey
   * @param candidateKey
   * @param request
   */
  @Transactional
  public void updatePersonalInterviewSchedule(String interviewScheduleKey, String candidateKey,
      InterviewScheduleParticipantsDto.Request request) {

    InterviewScheduleEntity interviewScheduleEntity = interviewScheduleRepository.findByInterviewScheduleKey(
            interviewScheduleKey)
        .orElseThrow(() -> new CustomException(ErrorCode.INTERVIEW_SCHEDULE_NOT_FOUND));

    InterviewScheduleParticipantsEntity participantsEntity = interviewScheduleParticipantsRepository.findByInterviewScheduleKeyAndCandidateKey(
        interviewScheduleKey, candidateKey);

    // 변경될 사항이 첫번째 인터뷰 날짜인지, 마지막 인터뷰 날짜인지 체크
    if (isFirstInterview(participantsEntity, interviewScheduleEntity)) {

      interviewScheduleEntity.updateFirstInterviewDate(
          request.getInterviewStartDatetime().toLocalDate());
    } else if (isLastInterview(participantsEntity, interviewScheduleEntity)) {

      interviewScheduleEntity.updateLastInterviewDate(request.getInterviewEndDatetime()
          .toLocalDate());
    }

    participantsEntity.updateEntity(request);

  }

  /**
   * 개인 면접 일정 전체 삭제
   *
   * @param jobPostingKey
   * @param stepId
   */
  @Transactional
  public void deleteAllInterviewSchedule(String jobPostingKey, Long stepId) {
    InterviewScheduleEntity interviewScheduleEntity = interviewScheduleRepository.findByJobPostingKeyAndJobPostingStepId(
            jobPostingKey, stepId)
        .orElseThrow(() -> new CustomException(ErrorCode.INTERVIEW_SCHEDULE_NOT_FOUND));

    List<InterviewScheduleParticipantsEntity> participants = interviewScheduleParticipantsRepository.findAllByJobPostingKeyAndJobPostingStepId(
        jobPostingKey, stepId);

    MailAlarmInfoEntity mailAlarmInfoEntity = mailAlarmInfoRepository.findByInterviewScheduleKey(
            interviewScheduleEntity.getInterviewScheduleKey())
        .orElseThrow(() -> new CustomException(ErrorCode.MAIL_ALARM_INFO_NOT_FOUND));

    // 예약된 메일의 시간이 현재 시간보다 이전이면 이미 발송된 것으로 간주하고 취소 메일 발송
    if (mailAlarmInfoEntity.getMailSendDateTime().isBefore(LocalDateTime.now())) {
      mailAlarmInfoService.sendCancellationMailToParticipants(interviewScheduleEntity,
          participants);
    } else {
      // 예약된 메일 시간이 현재 시간 이후이면 예약 취소
      mailAlarmInfoService.unscheduleMailJob(mailAlarmInfoEntity);
    }
    mailAlarmInfoRepository.delete(mailAlarmInfoEntity);

    interviewScheduleParticipantsRepository.deleteAll(participants);
    interviewScheduleRepository.delete(interviewScheduleEntity);
  }

  // 첫번째 인터뷰 날짜인지 확인
  public boolean isFirstInterview(InterviewScheduleParticipantsEntity participantsEntity,
      InterviewScheduleEntity interviewScheduleEntity) {

    return participantsEntity.getInterviewStartDatetime().toLocalDate()
        .isEqual(interviewScheduleEntity.getFirstInterviewDate());
  }

  // 마지막 인터뷰 날짜인지 확인
  public boolean isLastInterview(InterviewScheduleParticipantsEntity participantsEntity,
      InterviewScheduleEntity interviewScheduleEntity) {

    return participantsEntity.getInterviewEndDatetime().toLocalDate()
        .isEqual(interviewScheduleEntity.getLastInterviewDate());
  }
}