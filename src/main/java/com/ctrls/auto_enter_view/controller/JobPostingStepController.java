package com.ctrls.auto_enter_view.controller;

import com.ctrls.auto_enter_view.dto.candidateList.CandidateTechStackListDto;
import com.ctrls.auto_enter_view.dto.jobPostingStep.JobPostingStepsDto;
import com.ctrls.auto_enter_view.service.JobPostingStepService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/job-postings/{jobPostingKey}")
@RequiredArgsConstructor
@RestController
public class JobPostingStepController {

  private final JobPostingStepService jobPostingStepService;

  /**
   * 채용 공고 단계 전체 조회
   *
   * @param jobPostingKey
   * @return
   */
  @GetMapping("/steps")
  public ResponseEntity<JobPostingStepsDto> getJobPostingSteps(@PathVariable String jobPostingKey) {

    return ResponseEntity.ok(jobPostingStepService.getJobPostingSteps(jobPostingKey));
  }

  /**
   * 해당 채용 단계의 지원자 리스트 조회 : 지원자 key, 지원자 이름, 이력서 key, 기술 스택 리스트
   *
   * @param jobPostingKey
   * @param stepId
   * @return
   */
  @GetMapping("/steps/{stepId}/candidates-list")
  public ResponseEntity<List<CandidateTechStackListDto>> getCandidatesListByStepId(
      @PathVariable String jobPostingKey,
      @PathVariable Long stepId) {

    return ResponseEntity.ok(
        jobPostingStepService.getCandidatesListByStepId(jobPostingKey, stepId));
  }
}