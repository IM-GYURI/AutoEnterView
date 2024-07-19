package com.ctrls.auto_enter_view.controller;

import com.ctrls.auto_enter_view.dto.interviewschedule.InterviewScheduleDto.Request;
import com.ctrls.auto_enter_view.dto.interviewschedule.InterviewScheduleDto.Response;
import com.ctrls.auto_enter_view.dto.interviewschedule.InterviewScheduleDto.TaskRequest;
import com.ctrls.auto_enter_view.dto.interviewschedule.InterviewScheduleParticipantsDto;
import com.ctrls.auto_enter_view.service.InterviewScheduleService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
public class InterviewScheduleController {

  private final InterviewScheduleService interviewScheduleService;

  // 면접 일정 생성
  @PostMapping("/job-postings/{jobPostingKey}/steps/{stepId}/interview-schedule")
  public ResponseEntity<Response> createInterviewSchedule(@PathVariable String jobPostingKey,
      @PathVariable Long stepId, @RequestBody List<Request> request) {

    Response response = interviewScheduleService.createInterviewSchedule(jobPostingKey,
        stepId, request);

    return ResponseEntity.ok().body(response);
  }

  // 과제 일정 생성
  @PostMapping("/job-postings/{jobPostingKey}/steps/{stepId}/task-schedule")
  public ResponseEntity<Response> createTaskSchedule(@PathVariable String jobPostingKey,
      @PathVariable Long stepId, @RequestBody TaskRequest taskRequest) {

    Response response = interviewScheduleService.createTaskSchedule(jobPostingKey, stepId,
        taskRequest);

    return ResponseEntity.ok().body(response);
  }
}