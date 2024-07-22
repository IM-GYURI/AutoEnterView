package com.ctrls.auto_enter_view.service;

import static com.ctrls.auto_enter_view.enums.ErrorCode.EMAIL_DUPLICATION;
import static com.ctrls.auto_enter_view.enums.ErrorCode.EMAIL_NOT_FOUND;
import static com.ctrls.auto_enter_view.enums.ErrorCode.USER_NOT_FOUND;

import com.ctrls.auto_enter_view.dto.candidate.CandidateApplyDto;
import com.ctrls.auto_enter_view.dto.candidate.CandidateApplyDto.ApplyInfo;
import com.ctrls.auto_enter_view.dto.candidate.FindEmailDto;
import com.ctrls.auto_enter_view.dto.candidate.FindEmailDto.Response;
import com.ctrls.auto_enter_view.dto.candidate.SignUpDto;
import com.ctrls.auto_enter_view.entity.AppliedJobPostingEntity;
import com.ctrls.auto_enter_view.entity.CandidateEntity;
import com.ctrls.auto_enter_view.enums.ErrorCode;
import com.ctrls.auto_enter_view.exception.CustomException;
import com.ctrls.auto_enter_view.repository.AppliedJobPostingRepository;
import com.ctrls.auto_enter_view.repository.CandidateRepository;
import com.ctrls.auto_enter_view.repository.ResumeRepository;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
@Slf4j
public class CandidateService {

  private final CandidateRepository candidateRepository;
  private final ResumeRepository resumeRepository;
  private final AppliedJobPostingRepository appliedJobPostingRepository;
  private final PasswordEncoder passwordEncoder;

  // 회원 가입
  public SignUpDto.Response signUp(SignUpDto.Request signUpDto) {

    if (candidateRepository.existsByEmail(signUpDto.getEmail())) {
      throw new CustomException(EMAIL_DUPLICATION);
    }

    String encoded = passwordEncoder.encode(signUpDto.getPassword());

    CandidateEntity candidate = signUpDto.toEntity(signUpDto, encoded);

    candidateRepository.save(candidate);

    return SignUpDto.Response.builder()
        .candidateKey(candidate.getCandidateKey())
        .email(signUpDto.getEmail())
        .name(signUpDto.getName())
        .build();
  }

  // 회원 탈퇴
  public void withdraw(String candidateKey) {

    User principal = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

    CandidateEntity candidateEntity = candidateRepository.findByEmail(principal.getUsername())
        .orElseThrow(() -> new CustomException(USER_NOT_FOUND));

    // 응시자 정보의 응시자키와 URL 응시자키 일치 확인
    if (!candidateEntity.getCandidateKey().equals(candidateKey)) {
      throw new CustomException(USER_NOT_FOUND);
    }

    candidateRepository.delete(candidateEntity);
  }

  public Response findEmail(FindEmailDto.Request request) {

    CandidateEntity candidateEntity = candidateRepository.findByNameAndPhoneNumber(
            request.getName(), request.getPhoneNumber())
        .orElseThrow(() -> new CustomException(EMAIL_NOT_FOUND));

    return Response.builder()
        .email(candidateEntity.getEmail())
        .build();
  }

  // 로그인 한 지원자 email -> candidateKey 추출
  public String findCandidateKeyByEmail(String candidateEmail) {

    return candidateRepository.findByEmail(candidateEmail)
        .map(CandidateEntity::getCandidateKey)
        .orElseThrow(() -> new CustomException(EMAIL_NOT_FOUND));
  }

  // 이력서 존재 여부 확인하기
  public boolean hasResume(String candidateKey) {
    return resumeRepository.existsByCandidateKey(candidateKey);
  }

  // candidateKey -> 지원자 정보 조회 : 이름
  public String getCandidateNameByKey(String candidateKey) {

    return candidateRepository.findByCandidateKey(candidateKey)
        .map(CandidateEntity::getName)
        .orElseThrow(() -> new CustomException(ErrorCode.CANDIDATE_NOT_FOUND));
  }

  // 지원자가 지원한 채용 공고 조회
  public CandidateApplyDto.Response getApplyJobPostings(UserDetails userDetails,
      String candidateKey, int page, int size) {

    CandidateEntity candidateEntity = candidateRepository.findByEmail(userDetails.getUsername())
        .orElseThrow(() -> new CustomException(ErrorCode.CANDIDATE_NOT_FOUND));

    // 본인 확인
    if (!candidateEntity.getCandidateKey().equals(candidateKey)) {
      throw new CustomException(ErrorCode.NO_AUTHORITY);
    }

    Pageable pageable = PageRequest.of(page - 1, size);

    Page<AppliedJobPostingEntity> appliedJobPostingEntityPage = appliedJobPostingRepository.findAllByCandidateKey(
        candidateKey, pageable);

    List<CandidateApplyDto.ApplyInfo> applyInfoList = appliedJobPostingEntityPage.stream()
        .map(ApplyInfo::from).collect(Collectors.toList());

    log.info("{}개의 지원한 채용 공고 조회 완료", applyInfoList.size());

    return CandidateApplyDto.Response.builder()
        .appliedJobPostingsList(applyInfoList)
        .totalPages(appliedJobPostingEntityPage.getTotalPages())
        .totalElements(appliedJobPostingEntityPage.getTotalElements())
        .build();
  }
}