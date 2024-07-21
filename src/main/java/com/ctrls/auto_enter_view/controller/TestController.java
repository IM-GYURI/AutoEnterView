package com.ctrls.auto_enter_view.controller;

import com.ctrls.auto_enter_view.service.FilteringService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;



  @Controller
  @RequiredArgsConstructor
  @RequestMapping("/api-test/eunsun")
  public class TestController {

    private final FilteringService filteringService;

    @PostMapping
    public ResponseEntity<?> calculateScore(@RequestBody ScoreCalculationRequest request) {

      filteringService.calculateResumeScore(request.getCandidateKey(), request.getJobPostingKey());

      return ResponseEntity.ok("OK");
    }
  }

    @Getter
    class ScoreCalculationRequest {
      private String candidateKey;
      private String jobPostingKey;
  }

