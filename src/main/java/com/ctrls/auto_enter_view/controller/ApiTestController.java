package com.ctrls.auto_enter_view.controller;

import com.ctrls.auto_enter_view.service.ScoringService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequiredArgsConstructor
@RequestMapping("/api-test")
public class ApiTestController {

  private final ScoringService filteringService;

  @PostMapping
  public ResponseEntity<?> scoreApplicants(@RequestBody String jobPostingKey) {

    filteringService.scoreApplicants(jobPostingKey);

    return ResponseEntity.ok("OK");
  }
}