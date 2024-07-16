package com.ctrls.auto_enter_view.service;

import com.ctrls.auto_enter_view.dto.interviewschedule.InterviewScheduleDto.Request;
import com.ctrls.auto_enter_view.dto.interviewschedule.InterviewScheduleDto.Response;
import com.ctrls.auto_enter_view.entity.InterviewScheduleEntity;
import com.ctrls.auto_enter_view.enums.ResponseMessage;
import com.ctrls.auto_enter_view.repository.InterviewScheduleRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class InterviewScheduleService {

  private final InterviewScheduleRepository interviewScheduleRepository;

  public Response createInterviewSchedule(String jobPostingKey, Long stepId,
      List<Request> request) {

    InterviewScheduleEntity saved = interviewScheduleRepository.save(
        Request.toEntity(jobPostingKey, stepId, request));

    return Response.builder()
        .interviewScheduleKey(saved.getInterviewScheduleKey())
        .message(ResponseMessage.SUCCESS_INTERVIEW_SCHEDULE.getMessage())
        .build();
  }
}