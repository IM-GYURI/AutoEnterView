package com.ctrls.auto_enter_view.controller;

import com.ctrls.auto_enter_view.dto.jobPosting.JobPostingDto;
import com.ctrls.auto_enter_view.dto.jobPosting.JobPostingInfoDto;
import com.ctrls.auto_enter_view.entity.JobPostingEntity;
import com.ctrls.auto_enter_view.enums.ErrorCode;
import com.ctrls.auto_enter_view.enums.ResponseMessage;
import com.ctrls.auto_enter_view.exception.CustomException;
import com.ctrls.auto_enter_view.service.CandidateService;
import com.ctrls.auto_enter_view.service.JobPostingImageService;
import com.ctrls.auto_enter_view.service.JobPostingService;
import com.ctrls.auto_enter_view.service.JobPostingStepService;
import com.ctrls.auto_enter_view.service.JobPostingTechStackService;
import java.util.List;
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
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RequiredArgsConstructor
@RestController
public class JobPostingController {

  private final JobPostingService jobPostingService;
  private final JobPostingTechStackService jobPostingTechStackService;
  private final JobPostingStepService jobPostingStepService;
  private final CandidateService candidateService;
  private final JobPostingImageService jobPostingImageService;

  // 채용 공고 생성하기
  @Transactional
  @PostMapping("/companies/{companyKey}/job-postings")
  public ResponseEntity<JobPostingDto.Response> createJobPosting(
      @PathVariable String companyKey,
      @RequestPart(value = "jobPostingInfo") @Validated JobPostingDto.Request request,
      @RequestPart(value = "image", required = false) MultipartFile image) {

    JobPostingEntity jobPosting = jobPostingService.createJobPosting(companyKey, request);

    jobPostingTechStackService.createJobPostingTechStack(jobPosting, request);
    jobPostingStepService.createJobPostingStep(jobPosting, request);

    JobPostingDto.Response response;
    if (image != null && !image.isEmpty()) {
      response = jobPostingImageService.uploadImage(image, jobPosting.getJobPostingKey());
    } else {
      response = new JobPostingDto.Response(jobPosting.getJobPostingKey(), null);
    }

    return ResponseEntity.ok(response);
  }

  /**
   * 회사 본인이 등록한 채용공고 목록 조회
   *
   * @param companyKey
   * @return
   */
  @GetMapping("/companies/{companyKey}/posted-job-postings")
  public ResponseEntity<List<JobPostingInfoDto>> getJobPostingsByCompanyKey(
      @PathVariable String companyKey) {

    return ResponseEntity.ok(jobPostingService.getJobPostingsByCompanyKey(companyKey));
  }

  // 채용 공고 수정
  @PutMapping("/job-postings/{jobPostingKey}")
  public ResponseEntity<JobPostingDto.Response> editJobPosting(@PathVariable String jobPostingKey,
      @RequestPart(value = "jobPostingInfo") @Validated JobPostingDto.Request request,
      @RequestPart(value = "image", required = false) MultipartFile image) {

    jobPostingService.editJobPosting(jobPostingKey, request);
    jobPostingTechStackService.editJobPostingTechStack(jobPostingKey, request);

    JobPostingDto.Response response;
    if (image != null && !image.isEmpty()) {
      response = jobPostingImageService.uploadImage(image, jobPostingKey);
    } else {
      response = jobPostingImageService.getJobPostingImage(jobPostingKey);
    }

    return ResponseEntity.ok(response);
  }

  // 채용 공고 삭제하기
  @Transactional
  @DeleteMapping("/job-postings/{jobPostingKey}")
  public ResponseEntity<String> deleteJobPosting(@PathVariable String jobPostingKey) {

    jobPostingService.deleteJobPosting(jobPostingKey);
    jobPostingTechStackService.deleteJobPostingTechStack(jobPostingKey);
    jobPostingStepService.deleteJobPostingStep(jobPostingKey);
    jobPostingImageService.deleteImage(jobPostingKey);

    return ResponseEntity.ok(ResponseMessage.SUCCESS_DELETE_JOB_POSTING.getMessage());
  }

  // (지원자) 채용 공고 지원하기
  @PostMapping("/job-postings/{jobPostingKey}/apply")
  public ResponseEntity<String> applyJobPosting(
      @PathVariable String jobPostingKey,
      @AuthenticationPrincipal UserDetails userDetails) {

    String candidateEmail = userDetails.getUsername();
    String candidateKey = candidateService.findCandidateKeyByEmail(candidateEmail);

    if (!candidateService.hasResume(candidateKey)) {
      throw new CustomException(ErrorCode.RESUME_NOT_FOUND);
    }

    jobPostingService.applyJobPosting(jobPostingKey, candidateKey);

    return ResponseEntity.ok(ResponseMessage.SUCCESS_JOB_POSTING_APPLY.getMessage());
  }
}