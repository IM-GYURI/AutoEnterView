package com.ctrls.auto_enter_view.controller;

import com.ctrls.auto_enter_view.dto.interviewschedule.InterviewScheduleDto.Request;
import com.ctrls.auto_enter_view.service.InterviewScheduleParticipantsService;
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
public class InterviewScheduleParticipantsController {

  private final InterviewScheduleParticipantsService interviewScheduleParticipantsService;

  @PostMapping("/job-postings/{jobPostingKey}/steps/{stepId}/interview-schedule-participants")
  public ResponseEntity<String> createInterviewSchedule(@PathVariable String jobPostingKey,
      @PathVariable Long stepId, @RequestBody List<Request> request) {

    interviewScheduleParticipantsService.createInterviewSchedule(jobPostingKey, stepId, request);

    return ResponseEntity.ok("개인 면접 일정 생성 완료");
  }


}