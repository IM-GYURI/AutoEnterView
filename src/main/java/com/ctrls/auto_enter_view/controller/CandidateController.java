package com.ctrls.auto_enter_view.controller;

import com.ctrls.auto_enter_view.dto.candidate.CandidateApplyDto;
import com.ctrls.auto_enter_view.dto.candidate.FindEmailDto;
import com.ctrls.auto_enter_view.dto.candidate.FindEmailDto.Response;
import com.ctrls.auto_enter_view.dto.candidate.SignUpDto;
import com.ctrls.auto_enter_view.service.CandidateService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/candidates")
@RequiredArgsConstructor
@RestController
public class CandidateController {

  private final CandidateService candidateService;

  // 회원 가입
  @PostMapping("/signup")
  public ResponseEntity<SignUpDto.Response> signUp(
      @RequestBody @Validated SignUpDto.Request signUpDto) {

    SignUpDto.Response response = candidateService.signUp(signUpDto);

    return ResponseEntity.ok().body(response);
  }

  // 이메일 찾기
  @PostMapping("/find-email")
  public ResponseEntity<FindEmailDto.Response> findEmail(
      @RequestBody @Validated FindEmailDto.Request request) {

    Response response = candidateService.findEmail(request);

    return ResponseEntity.ok(response);
  }

  // (지원자) 지원한 채용 공고 조회하기
  @GetMapping("/{candidateKey}/applied-job-postings")
  public ResponseEntity<CandidateApplyDto.Response> getApplyJobPosting(
      @PathVariable String candidateKey,
      @RequestParam(defaultValue = "1") int page,
      @RequestParam(defaultValue = "20") int size) {

    CandidateApplyDto.Response response = candidateService.getApplyJobPostings(candidateKey, page,
        size);

    return ResponseEntity.ok(response);
  }

}