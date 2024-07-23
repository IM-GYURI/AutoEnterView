package com.ctrls.auto_enter_view.controller;

import com.ctrls.auto_enter_view.dto.common.JobPostingDetailDto;
import com.ctrls.auto_enter_view.dto.common.MainJobPostingDto;
import com.ctrls.auto_enter_view.service.JobPostingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/common/job-postings")
@RequiredArgsConstructor
@RestController
public class CommonJobPostingController {

  private final JobPostingService jobPostingService;

  /**
   * Main 화면에 보여질 채용 공고 전체 조회하기
   *
   * @param page 페이징 처리 시 page 시작 1
   * @param size 페이징 처리 시 한번에 가져오는 size 24
   * @return MainJobPostingDto.Response
   */
  @GetMapping
  public ResponseEntity<MainJobPostingDto.Response> getAllJobPosting(
      @RequestParam(defaultValue = "1") int page,
      @RequestParam(defaultValue = "24") int size) {

    MainJobPostingDto.Response response = jobPostingService.getAllJobPosting(page, size);
    return ResponseEntity.ok(response);
  }

  /**
   * 채용 공고 상세 조회하기
   *
   * @param jobPostingKey 채용 공고 PK
   * @return JobPostingDetailDto.Response
   */
  @GetMapping("/{jobPostingKey}")
  public ResponseEntity<JobPostingDetailDto.Response> getDetailJobPosting(
      @PathVariable String jobPostingKey) {

    JobPostingDetailDto.Response response = jobPostingService.getJobPostingDetail(jobPostingKey);
    return ResponseEntity.ok(response);
  }
}