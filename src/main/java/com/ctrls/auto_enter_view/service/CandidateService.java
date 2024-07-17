package com.ctrls.auto_enter_view.service;

import static com.ctrls.auto_enter_view.enums.ErrorCode.EMAIL_DUPLICATION;
import static com.ctrls.auto_enter_view.enums.ErrorCode.EMAIL_NOT_FOUND;
import static com.ctrls.auto_enter_view.enums.ErrorCode.USER_NOT_FOUND;

import com.ctrls.auto_enter_view.dto.candidate.CandidateApplyDto;
import com.ctrls.auto_enter_view.dto.candidate.FindEmailDto;
import com.ctrls.auto_enter_view.dto.candidate.FindEmailDto.Response;
import com.ctrls.auto_enter_view.dto.candidate.SignUpDto;
import com.ctrls.auto_enter_view.entity.CandidateEntity;
import com.ctrls.auto_enter_view.entity.CandidateListEntity;
import com.ctrls.auto_enter_view.entity.CompanyEntity;
import com.ctrls.auto_enter_view.entity.JobPostingEntity;
import com.ctrls.auto_enter_view.enums.ErrorCode;
import com.ctrls.auto_enter_view.enums.ResponseMessage;
import com.ctrls.auto_enter_view.exception.CustomException;
import com.ctrls.auto_enter_view.repository.CandidateListRepository;
import com.ctrls.auto_enter_view.repository.CandidateRepository;
import com.ctrls.auto_enter_view.repository.CompanyRepository;
import com.ctrls.auto_enter_view.repository.JobPostingRepository;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
@Slf4j
public class CandidateService {

  private final CandidateRepository candidateRepository;
  private final CompanyRepository companyRepository;
  private final CandidateListRepository candidateListRepository;
  private final JobPostingRepository jobPostingRepository;
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
        .message(ResponseMessage.SIGNUP.getMessage())
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

  // candidateKey -> 지원자 정보 조회 : 이름
  public String getCandidateNameByKey(String candidateKey) {
    return candidateRepository.findByCandidateKey(candidateKey)
        .map(CandidateEntity::getName)
        .orElseThrow(() -> new CustomException(ErrorCode.CANDIDATE_NOT_FOUND));
  }

  // 지원자가 지원한 채용 공고 조회
  public CandidateApplyDto.Response getApplyJobPostings(String candidateKey, int page, int size) {
    Pageable pageable = PageRequest.of(page - 1, size);
    Page<CandidateListEntity> candidateListPage = candidateListRepository.findAllByCandidateKey(
        candidateKey, pageable);

    List<CandidateApplyDto.ApplyInfo> applyInfoList = new ArrayList<>();

    for (CandidateListEntity candidateListEntity : candidateListPage.getContent()) {
      JobPostingEntity jobPostingEntity = jobPostingRepository.findByJobPostingKey(
              candidateListEntity.getJobPostingKey())
          .orElseThrow(() -> new CustomException(ErrorCode.JOB_POSTING_NOT_FOUND));

      String companyName = getCompanyName(jobPostingEntity.getCompanyKey());
      LocalDateTime applyDate = candidateListEntity.getCreatedAt();

      CandidateApplyDto.ApplyInfo applyInfo = CandidateApplyDto.ApplyInfo.from(jobPostingEntity,
          companyName, applyDate);
      applyInfoList.add(applyInfo);
    }

    log.info("{}개의 지원한 채용 공고 조회 완료", applyInfoList.size());
    return CandidateApplyDto.Response.builder()
        .applyJobPostingsList(applyInfoList)
        .totalPages(candidateListPage.getTotalPages())
        .totalElements(candidateListPage.getTotalElements())
        .build();
  }

  // 회사 이름 가져오기
  private String getCompanyName(String companyKey) {
    CompanyEntity companyEntity = companyRepository.findByCompanyKey(companyKey)
        .orElseThrow(() -> new CustomException(ErrorCode.COMPANY_NOT_FOUND));

    String companyName = companyEntity.getCompanyName();
    log.info("회사명 조회 완료 : {}", companyName);
    return companyName;
  }
}