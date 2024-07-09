package com.ctrls.auto_enter_view.controller;

import com.ctrls.auto_enter_view.dto.common.JobPostingDetailDto;
import com.ctrls.auto_enter_view.dto.common.MainJobPostingDto;
import com.ctrls.auto_enter_view.service.JobPostingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/common/job-postings")
@RequiredArgsConstructor
@RestController
public class CommonJobPostingController {

  private final JobPostingService jobPostingService;

  // main 화면에 보여질 채용 공고 가져오기
  @GetMapping
  public ResponseEntity<MainJobPostingDto.Response> getAllJobPosting() {

    MainJobPostingDto.Response response = jobPostingService.getAllJobPosting();
    return ResponseEntity.ok(response);
  }

  // 채용 공고 상세 정보 가져오기
  @GetMapping("/{jobPostingKey}")
  public ResponseEntity<JobPostingDetailDto.Response> getDetailJobPosting(
      @PathVariable String jobPostingKey) {

    JobPostingDetailDto.Response response = jobPostingService.getJobPostingDetail(jobPostingKey);
    return ResponseEntity.ok(response);
  }
}