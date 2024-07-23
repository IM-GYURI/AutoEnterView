package com.ctrls.auto_enter_view.controller;

import com.ctrls.auto_enter_view.dto.interviewschedule.InterviewScheduleDto.Request;
import com.ctrls.auto_enter_view.dto.interviewschedule.InterviewScheduleParticipantsDto;
import com.ctrls.auto_enter_view.dto.interviewschedule.InterviewScheduleParticipantsDto.Response;
import com.ctrls.auto_enter_view.enums.ResponseMessage;
import com.ctrls.auto_enter_view.service.InterviewScheduleParticipantsService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
public class InterviewScheduleParticipantsController {

  private final InterviewScheduleParticipantsService interviewScheduleParticipantsService;

  /**
   * 개인 면접 일정 생성하기
   *
   * @param jobPostingKey 채용 공고 PK
   * @param stepId 채용 단계 PK
   * @param request InterviewScheduleDto.Request
   * @return ResponseMessage
   */
  @PostMapping("/job-postings/{jobPostingKey}/steps/{stepId}/interview-schedule-participants")
  public ResponseEntity<String> createInterviewSchedule(@PathVariable String jobPostingKey,
      @PathVariable Long stepId, @RequestBody List<Request> request,
      @AuthenticationPrincipal UserDetails userDetails) {

    interviewScheduleParticipantsService.createInterviewSchedule(jobPostingKey, stepId, request,
        userDetails);

    return ResponseEntity.ok(ResponseMessage.SUCCESS_PERSONAL_INTERVIEW_SCHEDULE.getMessage());
  }

  /**
   * 면접 일정 조회하기
   *
   * @param jobPostingKey 채용 공고 PK
   * @param stepId 채용 단계 PK
   * @return List<InterviewScheduleParticipantsDto.Response>
   */
  @GetMapping("/job-postings/{jobPostingKey}/steps/{stepId}/interview-schedule-participants")

  public ResponseEntity<List<Response>> getAllInterviewSchedule(@PathVariable String jobPostingKey,
      @PathVariable Long stepId, @AuthenticationPrincipal UserDetails userDetails) {
    List<Response> responseList = interviewScheduleParticipantsService.getAllInterviewSchedule(
        jobPostingKey, stepId, userDetails);

    return ResponseEntity.ok().body(responseList);

  }

  /**
   * 개인 면접 일정 수정하기
   *
   * @param interviewScheduleKey 면접 일정 PK
   * @param candidateKey 지원자 PK
   * @param request InterviewScheduleParticipantsDto.Request
   * @return ResponseMessage
   */
  @PutMapping("/interview-schedule-participants/{interviewScheduleKey}/candidates/{candidateKey}")
  public ResponseEntity<String> updatePersonalInterviewSchedule(
      @PathVariable String interviewScheduleKey,
      @PathVariable String candidateKey,
      @RequestBody InterviewScheduleParticipantsDto.Request request,
      @AuthenticationPrincipal UserDetails userDetails) {

    interviewScheduleParticipantsService.updatePersonalInterviewSchedule(interviewScheduleKey,
        candidateKey, request, userDetails);

    return ResponseEntity.ok(ResponseMessage.SUCCESS_UPDATE_INTERVIEW_SCHEDULE.getMessage());
  }

  /**
   * 전체 면접 일정 삭제하기
   *
   * @param jobPostingKey 채용 공고 PK
   * @param stepId 채용 단계 PK
   * @return ResponseMessage
   */
  @DeleteMapping("/job-postings/{jobPostingKey}/steps/{stepId}/interview-schedule")
  public ResponseEntity<String> deleteAllInterviewSchedule(@PathVariable String jobPostingKey,
      @PathVariable Long stepId, @AuthenticationPrincipal UserDetails userDetails) {
    interviewScheduleParticipantsService.deleteAllInterviewSchedule(jobPostingKey, stepId,
        userDetails);

    return ResponseEntity.ok(ResponseMessage.SUCCESS_DELETE_INTERVIEW_SCHEDULE.getMessage());
  }
}