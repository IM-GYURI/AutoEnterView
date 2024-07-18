package com.ctrls.auto_enter_view.controller;

import com.ctrls.auto_enter_view.dto.interviewschedule.InterviewScheduleDto.Request;
import com.ctrls.auto_enter_view.dto.interviewschedule.InterviewScheduleParticipantsDto;
import com.ctrls.auto_enter_view.dto.interviewschedule.InterviewScheduleParticipantsDto.Response;
import com.ctrls.auto_enter_view.enums.ResponseMessage;
import com.ctrls.auto_enter_view.service.InterviewScheduleParticipantsService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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
   * 개인 면접 일정 생성
   *
   * @param jobPostingKey
   * @param stepId
   * @param request
   * @return
   */
  @PostMapping("/job-postings/{jobPostingKey}/steps/{stepId}/interview-schedule-participants")
  public ResponseEntity<String> createInterviewSchedule(@PathVariable String jobPostingKey,
      @PathVariable Long stepId, @RequestBody List<Request> request) {

    interviewScheduleParticipantsService.createInterviewSchedule(jobPostingKey, stepId, request);

    return ResponseEntity.ok(ResponseMessage.SUCCESS_PERSONAL_INTERVIEW_SCHEDULE.getMessage());
  }

  /**
   * 면접 일정 조회
   *
   * @param jobPostingKey
   * @param stepId
   * @return
   */
  @GetMapping("/job-postings/{jobPostingKey}/steps/{stepId}/interview-schedule-participants")

  public ResponseEntity<List<Response>> getAllInterviewSchedule(@PathVariable String jobPostingKey,
      @PathVariable Long stepId) {
    List<Response> responseList = interviewScheduleParticipantsService.getAllInterviewSchedule(
        jobPostingKey, stepId);

    return ResponseEntity.ok().body(responseList);

  }

  /**
   * 개인 면접 일정 수정
   *
   * @param interviewScheduleKey
   * @param candidateKey
   * @param request
   * @return
   */
  @PutMapping("/interview-schedule-participants/{interviewScheduleKey}/candidates/{candidateKey}")
  public ResponseEntity<String> updatePersonalInterviewSchedule(
      @PathVariable String interviewScheduleKey,
      @PathVariable String candidateKey,
      @RequestBody InterviewScheduleParticipantsDto.Request request) {

    interviewScheduleParticipantsService.updatePersonalInterviewSchedule(interviewScheduleKey,
        candidateKey, request);

    return ResponseEntity.ok(ResponseMessage.SUCCESS_UPDATE_INTERVIEW_SCHEDULE.getMessage());
  }

  /**
   * 전체 면접 일정 삭제
   *
   * @param interviewScheduleKey
   * @return
   */
  @DeleteMapping("/interview-schedule-participants/{interviewScheduleKey}")
  public ResponseEntity<String> deleteAllInterviewSchedule(
      @PathVariable String interviewScheduleKey) {

    interviewScheduleParticipantsService.deleteAllInterviewSchedule(interviewScheduleKey);

    return ResponseEntity.ok(ResponseMessage.SUCCESS_DELETE_INTERVIEW_SCHEDULE.getMessage());
  }
}