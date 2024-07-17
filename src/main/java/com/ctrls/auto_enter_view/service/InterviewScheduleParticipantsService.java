package com.ctrls.auto_enter_view.service;

import com.ctrls.auto_enter_view.dto.interviewschedule.InterviewScheduleDto.Request;
import com.ctrls.auto_enter_view.dto.interviewschedule.InterviewScheduleParticipantsDto.Response;
import com.ctrls.auto_enter_view.entity.InterviewScheduleParticipantsEntity;
import com.ctrls.auto_enter_view.enums.ErrorCode;
import com.ctrls.auto_enter_view.exception.CustomException;
import com.ctrls.auto_enter_view.repository.CandidateListRepository;
import com.ctrls.auto_enter_view.repository.InterviewScheduleParticipantsRepository;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
@Slf4j
public class InterviewScheduleParticipantsService {

  private final InterviewScheduleParticipantsRepository interviewScheduleParticipantsRepository;

  private final CandidateListRepository candidateListRepository;

  public void createInterviewSchedule(String jobPostingKey, Long stepId, List<Request> request) {

    List<String> candidatesKey = candidateListRepository.findCandidateKeyByJobPostingKeyAndJobPostingStepId(
        jobPostingKey, stepId);

    int candidateIndex = candidatesKey.size() - 1;

    for (Request requested : request) {
      LocalDateTime start = LocalDateTime.of(requested.getStartDate(), requested.getStartTime());

      for (int j = 0; j < requested.getTimes(); j++) {
        LocalDateTime startDateTime = start;
        start = start.plusMinutes(requested.getTerm());
        LocalDateTime endDatetime = start;

        if (candidateIndex < 0) {
          throw new CustomException(ErrorCode.CANDIDATE_INADEQUATE_ERROR);
        }

        interviewScheduleParticipantsRepository.save(Request.toParticipantsEntity(
            jobPostingKey, stepId, startDateTime, endDatetime, candidatesKey.get(candidateIndex)));

        candidateIndex--;
      }

    }
  }

  public List<Response> getAllInterviewSchedule(String jobPostingKey, Long stepId) {

    List<InterviewScheduleParticipantsEntity> entities = interviewScheduleParticipantsRepository.findAllByJobPostingKeyAndJobPostingStepId(
        jobPostingKey,
        stepId);

    return entities.stream().map(Response::fromEntity).toList();
  }
}