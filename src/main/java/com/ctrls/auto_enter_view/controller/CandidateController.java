package com.ctrls.auto_enter_view.controller;

import com.ctrls.auto_enter_view.dto.candidate.CandidateApplyDto;
import com.ctrls.auto_enter_view.dto.candidate.FindEmailDto;
import com.ctrls.auto_enter_view.dto.candidate.FindEmailDto.Response;
import com.ctrls.auto_enter_view.dto.candidate.SignUpDto;
import com.ctrls.auto_enter_view.service.CandidateService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
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

  /**
   * 지원자 회원가입
   *
   * @param request SignUpDto.Request
   * @return SignUpDto.Response
   */
  @PostMapping("/signup")
  public ResponseEntity<SignUpDto.Response> signUp(
      @RequestBody @Validated SignUpDto.Request request) {

    SignUpDto.Response response = candidateService.signUp(request);

    return ResponseEntity.ok(response);
  }

  /**
   * 지원자 가입한 이메일 찾기
   *
   * @param request FindEmailDto.Request
   * @return FindEmailDto.Response
   */
  @PostMapping("/find-email")
  public ResponseEntity<FindEmailDto.Response> findEmail(
      @RequestBody @Validated FindEmailDto.Request request) {

    Response response = candidateService.findEmail(request);

    return ResponseEntity.ok(response);
  }

  /**
   * 본인이 지원한 채용 공고 조회하기
   *
   * @param userDetails 로그인 된 사용자 정보
   * @param candidateKey 지원자 PK
   * @param page 페이징 처리 시 page 시작 1
   * @param size 페이징 처리 시 한번에 가져오는 size 20
   * @return CandidateApplyDto.Response
   */
  @GetMapping("/{candidateKey}/applied-job-postings")
  public ResponseEntity<CandidateApplyDto.Response> getApplyJobPosting(
      @AuthenticationPrincipal UserDetails userDetails,
      @PathVariable String candidateKey,
      @RequestParam(defaultValue = "1") int page,
      @RequestParam(defaultValue = "20") int size
  ) {

    CandidateApplyDto.Response response = candidateService.getApplyJobPostings(userDetails,
        candidateKey, page, size);

    return ResponseEntity.ok(response);
  }
}