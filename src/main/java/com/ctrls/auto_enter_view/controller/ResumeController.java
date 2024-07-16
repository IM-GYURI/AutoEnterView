package com.ctrls.auto_enter_view.controller;

import com.ctrls.auto_enter_view.dto.resume.ResumeDto;
import com.ctrls.auto_enter_view.dto.resume.ResumeDto.Response;
import com.ctrls.auto_enter_view.dto.resume.ResumeReadDto;
import com.ctrls.auto_enter_view.enums.ResponseMessage;
import com.ctrls.auto_enter_view.service.ResumeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/candidates/{candidateKey}/resume")
@RequiredArgsConstructor
@RestController
public class ResumeController {

  private final ResumeService resumeService;

  // 이력서 생성
  @PostMapping
  public ResponseEntity<Response> createResume(
      @AuthenticationPrincipal UserDetails userDetails,
      @PathVariable String candidateKey,
      @RequestBody @Validated ResumeDto.Request request
  ) {

    Response response = resumeService.createResume(userDetails, candidateKey, request);

    return ResponseEntity.ok(response);
  }

  // 이력서 조회
  @GetMapping
  public ResponseEntity<ResumeReadDto.Response> readResume(
      @AuthenticationPrincipal UserDetails userDetails,
      @PathVariable String candidateKey
  ) {

    ResumeReadDto.Response response = resumeService.readResume(userDetails, candidateKey);

    return ResponseEntity.ok(response);
  }

  // 이력서 수정
  @PutMapping
  public ResponseEntity<String> updateResume(
      @AuthenticationPrincipal UserDetails userDetails,
      @PathVariable String candidateKey,
      @RequestBody @Validated ResumeDto.Request request
  ) {

    resumeService.updateResume(userDetails, candidateKey, request);

    return ResponseEntity.ok(ResponseMessage.SUCCESS_UPDATE_RESUME.getMessage());
  }

  // 이력서 삭제
  @DeleteMapping
  public ResponseEntity<String> deleteResume(
      @AuthenticationPrincipal UserDetails userDetails,
      @PathVariable String candidateKey
  ) {

    resumeService.deleteResume(userDetails, candidateKey);

    return ResponseEntity.ok(ResponseMessage.SUCCESS_DELETE_RESUME.getMessage());
  }
}