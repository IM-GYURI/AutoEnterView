package com.ctrls.auto_enter_view.controller;

import com.ctrls.auto_enter_view.dto.resume.ResumeDto;
import com.ctrls.auto_enter_view.dto.resume.ResumeReadDto;
import com.ctrls.auto_enter_view.enums.ResponseMessage;
import com.ctrls.auto_enter_view.service.ResumeImageService;
import com.ctrls.auto_enter_view.service.ResumeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RequestMapping("/candidates/{candidateKey}/resume")
@RequiredArgsConstructor
@RestController
public class ResumeController {

  private final ResumeService resumeService;
  private final ResumeImageService resumeImageService;

  /**
   * 이력서 생성하기
   *
   * @param userDetails 로그인 된 사용자 정보
   * @param candidateKey 지원자 PK
   * @param request ResumeDto.Request
   * @param image MultipartFile
   * @return ResumeDto.Response
   */
  @PostMapping
  public ResponseEntity<ResumeDto.Response> createResume(
      @AuthenticationPrincipal UserDetails userDetails,
      @PathVariable String candidateKey,
      @RequestPart(value = "resumeInfo") @Validated ResumeDto.Request request,
      @RequestPart(value = "image", required = false) MultipartFile image) {

    String resumeKey = resumeService.createResume(userDetails, candidateKey, request);

    ResumeDto.Response response;
    if (image != null && !image.isEmpty()) {
      response = resumeImageService.uploadImage(image, resumeKey);
    } else {
      response = new ResumeDto.Response(resumeKey, null);
    }

    return ResponseEntity.ok(response);
  }

  /**
   * 이력서 조회하기
   *
   * @param userDetails 로그인 된 사용자 정보
   * @param candidateKey 지원자 PK
   * @return ResumeReadDto.Response
   */
  @GetMapping
  public ResponseEntity<ResumeReadDto.Response> readResume(
      @AuthenticationPrincipal UserDetails userDetails,
      @PathVariable String candidateKey
  ) {

    ResumeReadDto.Response response = resumeService.readResume(userDetails, candidateKey);
    return ResponseEntity.ok(response);
  }

  /**
   * 이력서 수정하기
   *
   * @param userDetails 로그인 된 사용자 정보
   * @param candidateKey 지원자 PK
   * @param request ResumeDto.Request
   * @param image MultipartFile
   * @return ResumeDto.Response
   */
  @PutMapping
  public ResponseEntity<ResumeDto.Response> updateResume(
      @AuthenticationPrincipal UserDetails userDetails,
      @PathVariable String candidateKey,
      @RequestPart(value = "resumeInfo") @Validated ResumeDto.Request request,
      @RequestPart(value = "image", required = false) MultipartFile image) {

    // 이력서 정보 업데이트
    String resumeKey = resumeService.updateResume(userDetails, candidateKey, request);

    ResumeDto.Response response;
    if (image != null && !image.isEmpty()) {
      response = resumeImageService.uploadImage(image, resumeKey);
    } else {
      response = resumeImageService.getExistingResumeImage(resumeKey);
    }

    return ResponseEntity.ok(response);
  }

  /**
   * 이력서 삭제하기
   *
   * @param userDetails 로그인 된 사용자 정보
   * @param candidateKey 지원자 PK
   * @return ResponseMessage
   */
  @Transactional
  @DeleteMapping
  public ResponseEntity<String> deleteResume(
      @AuthenticationPrincipal UserDetails userDetails,
      @PathVariable String candidateKey) {

    resumeImageService.deleteImage(candidateKey);
    resumeService.deleteResume(userDetails, candidateKey);

    return ResponseEntity.ok(ResponseMessage.SUCCESS_DELETE_RESUME.getMessage());
  }
}