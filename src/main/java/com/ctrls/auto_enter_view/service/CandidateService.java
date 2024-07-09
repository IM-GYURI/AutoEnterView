package com.ctrls.auto_enter_view.service;

import static com.ctrls.auto_enter_view.enums.ErrorCode.EMAIL_DUPLICATION;
import static com.ctrls.auto_enter_view.enums.ErrorCode.EMAIL_NOT_FOUND;
import static com.ctrls.auto_enter_view.enums.ErrorCode.PASSWORD_NOT_MATCH;
import static com.ctrls.auto_enter_view.enums.ErrorCode.USER_NOT_FOUND;

import com.ctrls.auto_enter_view.dto.candidate.CandidateApplyDto;
import com.ctrls.auto_enter_view.dto.candidate.ChangePasswordDto.Request;
import com.ctrls.auto_enter_view.dto.candidate.FindEmailDto;
import com.ctrls.auto_enter_view.dto.candidate.FindEmailDto.Response;
import com.ctrls.auto_enter_view.dto.candidate.SignUpDto;
import com.ctrls.auto_enter_view.dto.candidate.WithdrawDto;
import com.ctrls.auto_enter_view.entity.CandidateEntity;
import com.ctrls.auto_enter_view.entity.CandidateListEntity;
import com.ctrls.auto_enter_view.entity.CompanyEntity;
import com.ctrls.auto_enter_view.entity.JobPostingEntity;
import com.ctrls.auto_enter_view.enums.ErrorCode;
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
        .email(signUpDto.getEmail())
        .name(signUpDto.getName())
        .message(signUpDto.getName() + "님 회원가입을 환영합니다")
        .build();
  }

  // 회원 탈퇴
  public void withdraw(WithdrawDto.Request request, String candidateKey) {

    User principal = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

    CandidateEntity candidateEntity = candidateRepository.findByEmail(principal.getUsername())
        .orElseThrow(() -> new CustomException(USER_NOT_FOUND));

    // 응시자 정보의 응시자키와 URL 응시자키 일치 확인
    if (!candidateEntity.getCandidateKey().equals(candidateKey)) {
      throw new CustomException(USER_NOT_FOUND);
    }

    if (!passwordEncoder.matches(request.getPassword(), candidateEntity.getPassword())) {
      throw new CustomException(PASSWORD_NOT_MATCH);
    }

    candidateRepository.delete(candidateEntity);
  }

  // 비밀번호 수정
  public void changePassword(String candidateKey, Request request) {

    User principal = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

    CandidateEntity candidateEntity = candidateRepository.findByEmail(principal.getUsername())
        .orElseThrow(() -> new CustomException(USER_NOT_FOUND));

    // 응시자 정보의 응시자키와 URL 응시자키 일치 확인
    if (!candidateEntity.getCandidateKey().equals(candidateKey)) {
      throw new CustomException(USER_NOT_FOUND);
    }

    if (!passwordEncoder.matches(request.getOldPassword(), candidateEntity.getPassword())) {
      throw new CustomException(PASSWORD_NOT_MATCH);
    }

    candidateEntity.setPassword(passwordEncoder.encode(request.getNewPassword()));

    candidateRepository.save(candidateEntity);
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
  public CandidateApplyDto.Response getApplyJobPostings(String candidateKey) {
    List<CandidateListEntity> candidateListEntities = candidateListRepository.findAllByCandidateKey(candidateKey);

    List<CandidateApplyDto.ApplyInfo> applyInfoList = new ArrayList<>();

    for (CandidateListEntity candidateListEntity : candidateListEntities) {
      JobPostingEntity jobPostingEntity = jobPostingRepository.findByJobPostingKey(candidateListEntity.getJobPostingKey())
          .orElseThrow(() -> new CustomException(ErrorCode.JOB_POSTING_NOT_FOUND));

      String companyName = getCompanyName(jobPostingEntity.getCompanyKey());
      LocalDateTime applyDate = candidateListEntity.getCreatedAt();

      CandidateApplyDto.ApplyInfo applyInfo = CandidateApplyDto.ApplyInfo.from(jobPostingEntity, companyName, applyDate);
      applyInfoList.add(applyInfo);
    }

    log.info("{}개의 지원한 채용 공고 조회 완료", applyInfoList.size());
    return CandidateApplyDto.Response.builder()
        .applyJobPostingsList(applyInfoList)
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