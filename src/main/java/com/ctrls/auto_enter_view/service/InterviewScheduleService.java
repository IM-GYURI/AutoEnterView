package com.ctrls.auto_enter_view.service;

import com.ctrls.auto_enter_view.dto.interviewschedule.InterviewScheduleDto.Request;
import com.ctrls.auto_enter_view.dto.interviewschedule.InterviewScheduleDto.Response;
import com.ctrls.auto_enter_view.dto.interviewschedule.InterviewScheduleDto.TaskRequest;
import com.ctrls.auto_enter_view.entity.CompanyEntity;
import com.ctrls.auto_enter_view.entity.InterviewScheduleEntity;
import com.ctrls.auto_enter_view.entity.JobPostingEntity;
import com.ctrls.auto_enter_view.enums.ErrorCode;
import com.ctrls.auto_enter_view.exception.CustomException;
import com.ctrls.auto_enter_view.repository.CompanyRepository;
import com.ctrls.auto_enter_view.repository.InterviewScheduleRepository;
import com.ctrls.auto_enter_view.repository.JobPostingRepository;
import com.ctrls.auto_enter_view.util.KeyGenerator;
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
  private final KeyGenerator keyGenerator;

  /**
   * 면접 일정 생성
   *
   * @param jobPostingKey 채용 공고 PK
   * @param stepId        채용 공고 단계 PK
   * @param request       InterviewScheduleDto.Request
   * @param userDetails   로그인 된 사용자 정보
   * @return InterviewScheduleDto.Response
   * @throws CustomException NO_AUTHORITY : 로그인한 사용자의 회사키와 매개변수의 회사키가 일치하지 않는 경우
   */
  public Response createInterviewSchedule(String jobPostingKey, Long stepId,
      List<Request> request, UserDetails userDetails) {

    checkOwner(userDetails, jobPostingKey);

    String key = keyGenerator.generateKey();

    InterviewScheduleEntity saved = interviewScheduleRepository.save(
        Request.toEntity(key, jobPostingKey, stepId, request));

    return Response.builder()
        .interviewScheduleKey(saved.getInterviewScheduleKey())
        .build();
  }

  /**
   * 과제 일정 생성
   *
   * @param jobPostingKey 채용 공고 PK
   * @param stepId        채용 공고 단계 PK
   * @param taskRequest   InterviewScheduleDto.TaskRequest
   * @param userDetails   로그인 된 사용자 정보
   * @return InterviewScheduleDto.Response
   * @throws CustomException NO_AUTHORITY : 로그인한 사용자의 회사키와 매개변수의 회사키가 일치하지 않는 경우
   */
  public Response createTaskSchedule(String jobPostingKey, Long stepId, TaskRequest taskRequest,
      UserDetails userDetails) {

    checkOwner(userDetails, jobPostingKey);

    String key = keyGenerator.generateKey();

    InterviewScheduleEntity saved = interviewScheduleRepository.save(
        TaskRequest.toEntity(key, jobPostingKey, stepId, taskRequest));

    return Response.builder()
        .interviewScheduleKey(saved.getInterviewScheduleKey())
        .build();
  }

  /**
   * 본인 회사인지 체크
   *
   * @param userDetails   로그인 된 사용자 정보
   * @param jobPostingKey 채용 공고 PK
   * @throws CustomException COMPANY_NOT_FOUND : 회사를 찾을 수 없는 경우
   * @throws CustomException JOB_POSTING_NOT_FOUND : 채용 공고를 찾을 수 없는 경우
   * @throws CustomException NO_AUTHORITY : 로그인한 사용자의 회사키와 매개변수의 회사키가 일치하지 않는 경우
   */
  private void checkOwner(UserDetails userDetails, String jobPostingKey) {

    CompanyEntity companyEntity = companyRepository.findByEmail(userDetails.getUsername())
        .orElseThrow(() -> new CustomException(
            ErrorCode.COMPANY_NOT_FOUND));

    JobPostingEntity jobPostingEntity = jobPostingRepository.findByJobPostingKey(
        jobPostingKey).orElseThrow(() -> new CustomException(ErrorCode.JOB_POSTING_NOT_FOUND));

    if (!jobPostingEntity.getCompanyKey().equals(companyEntity.getCompanyKey())) {
      throw new CustomException(ErrorCode.NO_AUTHORITY);
    }
  }
}