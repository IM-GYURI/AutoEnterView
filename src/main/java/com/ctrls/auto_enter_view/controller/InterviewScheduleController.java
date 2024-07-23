package com.ctrls.auto_enter_view.controller;

import com.ctrls.auto_enter_view.dto.interviewschedule.InterviewScheduleDto.Request;
import com.ctrls.auto_enter_view.dto.interviewschedule.InterviewScheduleDto.Response;
import com.ctrls.auto_enter_view.dto.interviewschedule.InterviewScheduleDto.TaskRequest;
import com.ctrls.auto_enter_view.service.InterviewScheduleService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
public class InterviewScheduleController {

  private final InterviewScheduleService interviewScheduleService;

  /**
   * 면접 일정 생성하기
   *
   * @param jobPostingKey 채용 공고 PK
   * @param stepId 채용 단계 PK
   * @param request List<InterviewScheduleDto.Request>
   * @param userDetails 로그인 된 사용자 정보
   * @return InterviewScheduleDto.Response
   */
  @PostMapping("/job-postings/{jobPostingKey}/steps/{stepId}/interview-schedule")
  public ResponseEntity<Response> createInterviewSchedule(@PathVariable String jobPostingKey,
      @PathVariable Long stepId, @RequestBody List<Request> request,
      @AuthenticationPrincipal UserDetails userDetails) {

    Response response = interviewScheduleService.createInterviewSchedule(jobPostingKey,
        stepId, request, userDetails);

    return ResponseEntity.ok().body(response);
  }

  /**
   * 과제 일정 생성 하기
   *
   * @param jobPostingKey 채용 공고 PK
   * @param stepId 채용 단계 PK
   * @param request InterviewScheduleDto.TaskRequest
   * @param userDetails 로그인 된 사용자 정보
   * @return InterviewScheduleDto.Response
   */
  @PostMapping("/job-postings/{jobPostingKey}/steps/{stepId}/task-schedule")
  public ResponseEntity<Response> createTaskSchedule(@PathVariable String jobPostingKey,
      @PathVariable Long stepId, @RequestBody TaskRequest request,
      @AuthenticationPrincipal UserDetails userDetails) {

    Response response = interviewScheduleService.createTaskSchedule(jobPostingKey, stepId,
        request, userDetails);

    return ResponseEntity.ok(response);
  }
}