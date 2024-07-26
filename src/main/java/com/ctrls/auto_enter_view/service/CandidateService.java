package com.ctrls.auto_enter_view.service;

import static com.ctrls.auto_enter_view.enums.ErrorCode.EMAIL_DUPLICATION;
import static com.ctrls.auto_enter_view.enums.ErrorCode.EMAIL_NOT_FOUND;
import static com.ctrls.auto_enter_view.enums.ErrorCode.USER_NOT_FOUND_BY_NAME_AND_PHONE;

import com.ctrls.auto_enter_view.component.KeyGenerator;
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
  private final KeyGenerator keyGenerator;

  /**
   * 지원자 회원 가입
   *
   * @param request SignUpDto.Request
   * @return SignUpDto.Response
   * @throws CustomException EMAIL_DUPLICATION : 회원 이메일 중복된 경우
   */
  public SignUpDto.Response signUp(SignUpDto.Request request) {
    log.info("이메일 중복 확인");
    if (candidateRepository.existsByEmail(request.getEmail())) {
      throw new CustomException(EMAIL_DUPLICATION);
    }

    String key = keyGenerator.generateKey();

    String encoded = passwordEncoder.encode(request.getPassword());

    CandidateEntity candidate = request.toEntity(key, request, encoded);

    log.info("지원자 회원 가입");
    candidateRepository.save(candidate);

    return SignUpDto.Response.builder()
        .candidateKey(candidate.getCandidateKey())
        .email(request.getEmail())
        .name(request.getName())
        .build();
  }

  /**
   * 이름과 전화번호로 지원자 이메일 찾기
   *
   * @param request FindEmailDto.Request
   * @return FindEmailDto.Response
   * @throws CustomException USER_NOT_FOUND_BY_NAME_AND_PHONE : 이름과 전화번호가 일치하는 지원자가 없는 경우
   */
  public Response findEmail(FindEmailDto.Request request) {
    log.info("이름과 전화번호로 지원자 이메일 찾기");

    CandidateEntity candidateEntity = candidateRepository.findByNameAndPhoneNumber(
            request.getName(), request.getPhoneNumber())
        .orElseThrow(() -> new CustomException(USER_NOT_FOUND_BY_NAME_AND_PHONE));

    return Response.builder()
        .email(candidateEntity.getEmail())
        .build();
  }

  /**
   * 로그인 한 지원자 email로 candidateKey 추출하기
   *
   * @param candidateEmail 지원자 이메일
   * @return 지원자의 candidateKey
   * @throws CustomException EMAIL_NOT_FOUND : 가입된 지원자 이메일이 없는 경우
   */
  public String findCandidateKeyByEmail(String candidateEmail) {
    log.info("로그인한 지원자 email로 candidateKey 추출");

    return candidateRepository.findByEmail(candidateEmail)
        .map(CandidateEntity::getCandidateKey)
        .orElseThrow(() -> new CustomException(EMAIL_NOT_FOUND));
  }

  /**
   * 이력서 존재 여부 확인하기
   *
   * @param candidateKey 지원자 PK
   * @return boolean : 이력서가 존재하면 ture, 존재하지 않으면 false
   */
  public boolean hasResume(String candidateKey) {
    log.info("이력서 존재 여부 확인");
    return resumeRepository.existsByCandidateKey(candidateKey);
  }

  /**
   * 지원자가 지원한 채용 공고 조회하기
   *
   * @param userDetails  로그인 된 사용자 정보
   * @param candidateKey 지원자 PK
   * @param page         페이징 처리 시 page 시작 1
   * @param size         이징 처리 시 한번에 가져오는 size 20
   * @return CandidateApplyDto.Response
   * @throws CustomException CANDIDATE_NOT_FOUND : 가입된 지원자를 찾을 수 없는 경우
   * @throws CustomException NO_AUTHORITY : 권한이 없는 경우 (본인이 아닌 경우)
   */
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