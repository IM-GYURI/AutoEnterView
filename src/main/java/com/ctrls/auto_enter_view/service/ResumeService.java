package com.ctrls.auto_enter_view.service;

import com.ctrls.auto_enter_view.dto.resume.ResumeDto.Request;
import com.ctrls.auto_enter_view.dto.resume.ResumeReadDto;
import com.ctrls.auto_enter_view.entity.ApplicantEntity;
import com.ctrls.auto_enter_view.entity.CandidateEntity;
import com.ctrls.auto_enter_view.entity.CompanyEntity;
import com.ctrls.auto_enter_view.entity.JobPostingEntity;
import com.ctrls.auto_enter_view.entity.ResumeCareerEntity;
import com.ctrls.auto_enter_view.entity.ResumeCertificateEntity;
import com.ctrls.auto_enter_view.entity.ResumeEntity;
import com.ctrls.auto_enter_view.entity.ResumeExperienceEntity;
import com.ctrls.auto_enter_view.entity.ResumeImageEntity;
import com.ctrls.auto_enter_view.entity.ResumeTechStackEntity;
import com.ctrls.auto_enter_view.enums.ErrorCode;
import com.ctrls.auto_enter_view.enums.UserRole;
import com.ctrls.auto_enter_view.exception.CustomException;
import com.ctrls.auto_enter_view.repository.ApplicantRepository;
import com.ctrls.auto_enter_view.repository.CandidateRepository;
import com.ctrls.auto_enter_view.repository.CompanyRepository;
import com.ctrls.auto_enter_view.repository.JobPostingRepository;
import com.ctrls.auto_enter_view.repository.ResumeCareerRepository;
import com.ctrls.auto_enter_view.repository.ResumeCertificateRepository;
import com.ctrls.auto_enter_view.repository.ResumeExperienceRepository;
import com.ctrls.auto_enter_view.repository.ResumeImageRepository;
import com.ctrls.auto_enter_view.repository.ResumeRepository;
import com.ctrls.auto_enter_view.repository.ResumeTechStackRepository;
import com.ctrls.auto_enter_view.component.KeyGenerator;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
@Slf4j
public class ResumeService {

  private final ApplicantRepository applicantRepository;
  private final CandidateRepository candidateRepository;
  private final CompanyRepository companyRepository;
  private final JobPostingRepository jobPostingRepository;
  private final ResumeCareerRepository resumeCareerRepository;
  private final ResumeCertificateRepository resumeCertificateRepository;
  private final ResumeExperienceRepository resumeExperienceRepository;
  private final ResumeImageRepository resumeImageRepository;
  private final ResumeRepository resumeRepository;
  private final ResumeTechStackRepository resumeTechStackRepository;
  private final KeyGenerator keyGenerator;

  /**
   * 이력서 생성
   *
   * @param userDetails  사용자 정보
   * @param candidateKey 지원자 KEY
   * @param request      이력서 생성 DTO
   * @return 이력서 KEY STRING
   * @throws CustomException ErrorCode.CANDIDATE_NOT_FOUND 지원자 없음
   * @throws CustomException ErrorCode.NO_AUTHORITY 다른 지원자가 이력서 생성을 시도함
   * @throws CustomException ErrorCode.ALREADY_EXISTS 이미 이력서가 존재함
   */
  @Transactional
  public String createResume(UserDetails userDetails, String candidateKey, Request request) {

    // 지원자
    CandidateEntity candidateEntity = candidateRepository.findByEmail(userDetails.getUsername())
        .orElseThrow(() -> new CustomException(
            ErrorCode.CANDIDATE_NOT_FOUND));

    // 검증
    if (!candidateEntity.getCandidateKey().equals(candidateKey)) {
      throw new CustomException(ErrorCode.NO_AUTHORITY);
    }

    // 중복 검사
    if (resumeRepository.existsByCandidateKey(candidateKey)) {
      throw new CustomException(ErrorCode.ALREADY_EXISTS);
    }

    String resumeKey = keyGenerator.generateKey();

    ResumeEntity resumeEntity = request.toEntity(resumeKey, candidateKey);

    // 경험, 경력, 기술스택, 증명사진, 자격 저장
    saveElse(resumeKey, request);

    resumeRepository.save(resumeEntity);

    return resumeKey;
  }

  /**
   * 이력서 조회
   *
   * @param userDetails  사용자 정보
   * @param candidateKey 지원자 KEY
   * @return 이력서 조회 DTO
   * @throws CustomException ErrorCode.COMPANY_NOT_FOUND 회사 계정이 없음
   * @throws CustomException ErrorCode.CANDIDATE_NOT_FOUND 지원자 계정이 없음
   * @throws CustomException ErrorCode.NO_AUTHORITY 지원받지 않은 회사나 다른 지원자가 조회를 시도함
   */
  @Transactional(readOnly = true)
  public ResumeReadDto.Response readResume(UserDetails userDetails, String candidateKey) {

    // 사용자 권한
    Collection<? extends GrantedAuthority> authorities = userDetails.getAuthorities();

    for (GrantedAuthority authority : authorities) {
      String role = authority.getAuthority();

      boolean isCompany = role.equals(UserRole.ROLE_COMPANY.name());
      boolean isCandidate = role.equals(UserRole.ROLE_CANDIDATE.name());

      // 회사인 경우
      if (isCompany) {
        CompanyEntity companyEntity = companyRepository.findByEmail(userDetails.getUsername())
            .orElseThrow(() -> new CustomException(ErrorCode.COMPANY_NOT_FOUND));

        // 지원자가 회사의 채용 공고에 지원했는지 확인
        List<JobPostingEntity> jobPostingEntities = jobPostingRepository.findAllByCompanyKey(
            companyEntity.getCompanyKey());

        for (JobPostingEntity jobPostingEntity : jobPostingEntities) {
          List<ApplicantEntity> applicantEntities = applicantRepository.findAllByJobPostingKey(
              jobPostingEntity.getJobPostingKey());

          boolean matched = applicantEntities.stream()
              .anyMatch(e -> e.getCandidateKey().equals(candidateKey));

          // 지원자가 존재하는 경우
          if (matched) {
            ResumeEntity resumeEntity = resumeRepository.findByCandidateKey(candidateKey)
                .orElseGet(ResumeEntity::new);

            return buildResponseDto(resumeEntity);
          }
        }
      }

      // 지원자인 경우
      else if (isCandidate) {
        CandidateEntity candidateEntity = candidateRepository.findByEmail(userDetails.getUsername())
            .orElseThrow(() -> new CustomException(
                ErrorCode.CANDIDATE_NOT_FOUND));

        // 지원자가 이력서의 작성자인 경우
        if (candidateEntity.getCandidateKey().equals(candidateKey)) {
          ResumeEntity resumeEntity = resumeRepository.findByCandidateKey(candidateKey)
              .orElseGet(ResumeEntity::new);

          return buildResponseDto(resumeEntity);
        }
      }

      // 권한 없음
      throw new CustomException(ErrorCode.NO_AUTHORITY);
    }

    // 컴파일 오류 방지용 RETURN
    return null;
  }

  /**
   * 이력서 수정
   *
   * @param userDetails  사용자 정보
   * @param candidateKey 지원자 KEY
   * @param request      이력서 수정 DTO
   * @return 이력서 KEY STRING
   * @throws CustomException ErrorCode.CANDIDATE_NOT_FOUND 지원자 계정이 없음
   * @throws CustomException ErrorCode.NO_AUTHORITY 다른 지원자가 수정을 시도함
   * @throws CustomException ErrorCode.RESUME_NOT_FOUND 이력서가 없음
   */
  @Transactional
  public String updateResume(UserDetails userDetails, String candidateKey, Request request) {

    CandidateEntity candidateEntity = candidateRepository.findByEmail(userDetails.getUsername())
        .orElseThrow(() -> new CustomException(ErrorCode.CANDIDATE_NOT_FOUND));

    // 이력서의 작성자가 아닌 경우
    if (!candidateEntity.getCandidateKey().equals(candidateKey)) {
      throw new CustomException(ErrorCode.NO_AUTHORITY);
    }

    ResumeEntity resumeEntity = resumeRepository.findByCandidateKey(candidateKey)
        .orElseThrow(() -> new CustomException(ErrorCode.RESUME_NOT_FOUND));

    resumeEntity.updateEntity(request);

    updateElse(resumeEntity.getResumeKey(), request);

    return resumeEntity.getResumeKey();
  }

  /**
   * 이력서 삭제
   *
   * @param userDetails  사용자 정보
   * @param candidateKey 지원자 KEY
   * @throws CustomException ErrorCode.CANDIDATE_NOT_FOUND 지원자의 계정이 없음
   * @throws CustomException ErrorCode.NO_AUTHORITY 다른 지원자가 삭제를 시도함
   * @throws CustomException ErrorCode.RESUME_NOT_FOUND 이력서가 없음
   */
  @Transactional
  public void deleteResume(UserDetails userDetails, String candidateKey) {

    CandidateEntity candidateEntity = candidateRepository.findByEmail(userDetails.getUsername())
        .orElseThrow(() -> new CustomException(ErrorCode.CANDIDATE_NOT_FOUND));

    // 이력서의 작성자가 아닌 경우
    if (!candidateEntity.getCandidateKey().equals(candidateKey)) {
      throw new CustomException(ErrorCode.NO_AUTHORITY);
    }

    String resumeKey = resumeRepository.findByCandidateKey(candidateKey)
        .orElseThrow(() -> new CustomException(ErrorCode.RESUME_NOT_FOUND)).getResumeKey();

    deleteElse(resumeKey);

    resumeRepository.deleteByCandidateKey(candidateKey);
  }

  // 이력서 추가 정보를 저장하는 메서드
  private void saveElse(String resumeKey, Request request) {
    // 경력 저장
    if (request.getCareer() != null) {
      List<ResumeCareerEntity> list = request.getCareer().stream()
          .map(career -> career.toEntity(resumeKey))
          .collect(Collectors.toList());
      resumeCareerRepository.saveAll(list);
    }

    // 경험 저장
    if (request.getExperience() != null) {
      List<ResumeExperienceEntity> list = request.getExperience().stream()
          .map(experience -> experience.toEntity(resumeKey))
          .collect(Collectors.toList());
      resumeExperienceRepository.saveAll(list);
    }

    // 기술스택 저장
    if (request.getTechStack() != null) {
      List<ResumeTechStackEntity> list = request.getTechStack().stream()
          .map(techStack -> techStack.toEntity(resumeKey))
          .collect(Collectors.toList());
      resumeTechStackRepository.saveAll(list);
    }

    // 자격 저장
    if (request.getCertificates() != null) {
      List<ResumeCertificateEntity> list = request.getCertificates().stream()
          .map(certificate -> certificate.toEntity(resumeKey))
          .collect(Collectors.toList());
      resumeCertificateRepository.saveAll(list);
    }
  }

  // 이력서 추가 정보를 수정하는 메서드
  private void updateElse(String resumeKey, Request request) {

    deleteElse(resumeKey);
    saveElse(resumeKey, request);
  }

  // 이력서 추가 정보를 삭제하는 메서드
  private void deleteElse(String resumeKey) {

    resumeTechStackRepository.deleteAllByResumeKey(resumeKey);
    resumeCareerRepository.deleteAllByResumeKey(resumeKey);
    resumeExperienceRepository.deleteAllByResumeKey(resumeKey);
    resumeCertificateRepository.deleteAllByResumeKey(resumeKey);
  }

  /**
   * 이력서 정보와 추가 정보를 조립하고 반환하는 메서드
   *
   * @param resumeEntity 이력서 ENTITY
   * @return 이력서 정보, 이력서가 없다면 모든 필드가 null 및 빈 리스트
   */
  private ResumeReadDto.Response buildResponseDto(ResumeEntity resumeEntity) {

    String resumeKey = resumeEntity.getResumeKey();

    List<ResumeCareerEntity> resumeCareerEntities = resumeCareerRepository.findAllByResumeKey(
        resumeKey);

    List<ResumeCertificateEntity> resumeCertificateEntities = resumeCertificateRepository.findAllByResumeKey(
        resumeKey);

    List<ResumeExperienceEntity> resumeExperienceEntities = resumeExperienceRepository.findAllByResumeKey(
        resumeKey);

    List<ResumeTechStackEntity> resumeTechStackEntities = resumeTechStackRepository.findAllByResumeKey(
        resumeKey);

    String resumeImageUrl = resumeImageRepository.findByResumeKey(resumeKey)
        .map(ResumeImageEntity::getResumeImageUrl)
        .orElse(null);

    return ResumeReadDto.Response.builder()
        .entity(resumeEntity)
        .career(resumeCareerEntities)
        .certificates(resumeCertificateEntities)
        .experience(resumeExperienceEntities)
        .techStack(resumeTechStackEntities)
        .image(resumeImageUrl)
        .build();
  }
}