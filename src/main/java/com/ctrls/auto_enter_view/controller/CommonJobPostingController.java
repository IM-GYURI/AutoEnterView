package com.ctrls.auto_enter_view.controller;

import com.ctrls.auto_enter_view.dto.common.MainJobPostingDto;
import com.ctrls.auto_enter_view.service.JobPostingService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/common")
@RequiredArgsConstructor
@RestController
public class CommonJobPostingController {

  private final JobPostingService jobPostingService;

  // main 화면에 보여질 채용 공고 가져오기
  @GetMapping("/job-postings")
  public ResponseEntity<List<MainJobPostingDto.Response>> findAllJobPosting() {

    List<MainJobPostingDto.Response> response = jobPostingService.getAllJobPosting();
    return ResponseEntity.ok(response);
  }

}
