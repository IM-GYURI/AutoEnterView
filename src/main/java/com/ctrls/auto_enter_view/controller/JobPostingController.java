package com.ctrls.auto_enter_view.controller;

import com.ctrls.auto_enter_view.dto.jobposting.JobPostingDto;
import com.ctrls.auto_enter_view.entity.JobPostingEntity;
import com.ctrls.auto_enter_view.service.JobPostingService;
import com.ctrls.auto_enter_view.service.JobPostingStepService;
import com.ctrls.auto_enter_view.service.JobPostingTechStackService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
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
}