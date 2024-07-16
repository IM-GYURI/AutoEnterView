package com.ctrls.auto_enter_view.service;

import com.ctrls.auto_enter_view.dto.interviewschedule.InterviewScheduleDto.Request;
import com.ctrls.auto_enter_view.entity.InterviewScheduleEntity;
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

  public void createInterviewSchedule(String jobPostingKey, Long stepId, List<Request> request) {
    InterviewScheduleEntity entity = Request.toEntity(jobPostingKey, stepId, request);
    interviewScheduleRepository.save(entity);
  }
}