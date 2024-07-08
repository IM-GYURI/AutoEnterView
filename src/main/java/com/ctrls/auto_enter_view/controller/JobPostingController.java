package com.ctrls.auto_enter_view.controller;

import com.ctrls.auto_enter_view.dto.jobPosting.JobPostingDto;
import com.ctrls.auto_enter_view.dto.jobPosting.JobPostingInfoDto;
import com.ctrls.auto_enter_view.entity.JobPostingEntity;
import com.ctrls.auto_enter_view.service.JobPostingService;
import com.ctrls.auto_enter_view.service.JobPostingStepService;
import com.ctrls.auto_enter_view.service.JobPostingTechStackService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
public class JobPostingController {

  private final JobPostingService jobPostingService;

  private final JobPostingTechStackService jobPostingTechStackService;

  private final JobPostingStepService jobPostingStepService;

  @PostMapping("/companies/{companyKey}/job-postings")
  public ResponseEntity<String> createJobPosting(@PathVariable String companyKey,
      @RequestBody @Validated JobPostingDto.Request request) {

    JobPostingEntity jobPosting = jobPostingService.createJobPosting(companyKey, request);

    jobPostingTechStackService.createJobPostingTechStack(jobPosting, request);

    jobPostingStepService.createJobPostingStep(jobPosting, request);

    return ResponseEntity.ok("jobPostingKey: " + jobPosting.getJobPostingKey());
  }

  /**
   * 회사 본인이 등록한 채용공고 목록 조회
   *
   * @param companyKey
   * @return
   */
  @GetMapping("/companies/{companyKey}/posted-job-postings")
  public ResponseEntity<List<JobPostingInfoDto>> getJobPostingsByCompanyKey(
      @PathVariable String companyKey) {

    return ResponseEntity.ok(jobPostingService.getJobPostingsByCompanyKey(companyKey));
  }

  @PutMapping("/job-postings/{jobPostingKey}")
  public ResponseEntity<String> editJobPosting(@PathVariable String jobPostingKey,
      @RequestBody @Validated JobPostingDto.Request request) {

    jobPostingService.editJobPosting(jobPostingKey, request);
    jobPostingTechStackService.editJobPostingTechStack(jobPostingKey, request);
    jobPostingStepService.editJobPostingStep(jobPostingKey, request);

    return ResponseEntity.ok("수정 완료");
  }

  @Transactional
  @DeleteMapping("/job-postings/{jobPostingKey}")
  public ResponseEntity<String> deleteJobPosting(@PathVariable String jobPostingKey) {

    jobPostingService.deleteJobPosting(jobPostingKey);
    jobPostingTechStackService.deleteJobPostingTechStack(jobPostingKey);
    jobPostingStepService.deleteJobPostingStep(jobPostingKey);

    return ResponseEntity.ok("삭제 완료");
  }
}