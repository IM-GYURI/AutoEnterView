package com.ctrls.auto_enter_view.service;

import com.ctrls.auto_enter_view.dto.interviewschedule.InterviewScheduleDto.Request;
import com.ctrls.auto_enter_view.dto.interviewschedule.InterviewScheduleDto.Response;
import com.ctrls.auto_enter_view.dto.interviewschedule.InterviewScheduleDto.TaskRequest;
import com.ctrls.auto_enter_view.entity.CompanyEntity;
import com.ctrls.auto_enter_view.entity.InterviewScheduleEntity;
import com.ctrls.auto_enter_view.entity.JobPostingEntity;
import com.ctrls.auto_enter_view.enums.ErrorCode;
import com.ctrls.auto_enter_view.enums.ResponseMessage;
import com.ctrls.auto_enter_view.exception.CustomException;
import com.ctrls.auto_enter_view.repository.CompanyRepository;
import com.ctrls.auto_enter_view.repository.InterviewScheduleRepository;
import com.ctrls.auto_enter_view.repository.JobPostingRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class InterviewScheduleService {

  private final InterviewScheduleRepository interviewScheduleRepository;

  private final JobPostingRepository jobPostingRepository;

  private final CompanyRepository companyRepository;

  /**
   * 면접 일정 생성
   *
   * @param jobPostingKey
   * @param stepId
   * @param request
   * @param userDetails
   * @return
   */
  public Response createInterviewSchedule(String jobPostingKey, Long stepId,
      List<Request> request, UserDetails userDetails) {

    checkOwner(userDetails);

    InterviewScheduleEntity saved = interviewScheduleRepository.save(
        Request.toEntity(jobPostingKey, stepId, request));

    return Response.builder()
        .interviewScheduleKey(saved.getInterviewScheduleKey())
        .message(ResponseMessage.SUCCESS_INTERVIEW_SCHEDULE.getMessage())
        .build();
  }

  /**
   * 과제 일정 생성
   *
   * @param jobPostingKey
   * @param stepId
   * @param taskRequest
   * @param userDetails
   * @return
   */
  public Response createTaskSchedule(String jobPostingKey, Long stepId, TaskRequest taskRequest,
      UserDetails userDetails) {

    checkOwner(userDetails);

    InterviewScheduleEntity saved = interviewScheduleRepository.save(
        TaskRequest.toEntity(jobPostingKey, stepId, taskRequest));

    return Response.builder()
        .interviewScheduleKey(saved.getInterviewScheduleKey())
        .message(ResponseMessage.SUCCESS_CREATE_TASK_SCHEDULE.getMessage())
        .build();
  }

  // 본인 회사인지 체크
  private void checkOwner(UserDetails userDetails) {
    String userEmail = userDetails.getUsername();

    CompanyEntity companyEntity = companyRepository.findByEmail(userEmail)
        .orElseThrow(() -> new CustomException(
            ErrorCode.COMPANY_NOT_FOUND));

    JobPostingEntity jobPostingEntity = jobPostingRepository.findByCompanyKey(
        companyEntity.getCompanyKey());

    if (!jobPostingEntity.getCompanyKey().equals(companyEntity.getCompanyKey())) {
      throw new CustomException(ErrorCode.NO_AUTHORITY);
    }
  }
}