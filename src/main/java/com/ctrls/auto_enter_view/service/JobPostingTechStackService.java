package com.ctrls.auto_enter_view.service;

import com.ctrls.auto_enter_view.dto.jobPosting.JobPostingDto;
import com.ctrls.auto_enter_view.dto.jobPosting.JobPostingDto.Request;
import com.ctrls.auto_enter_view.entity.JobPostingEntity;
import com.ctrls.auto_enter_view.entity.JobPostingTechStackEntity;
import com.ctrls.auto_enter_view.enums.TechStack;
import com.ctrls.auto_enter_view.repository.JobPostingTechStackRepository;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class JobPostingTechStackService {

  private final JobPostingTechStackRepository jobPostingTechStackRepository;

  /**
   * 채용 단계 생성
   *
   * @param jobPostingEntity 채용 공고 ENTITY
   * @param request          채용 공고 생성 DTO
   */
  public void createJobPostingTechStack(JobPostingEntity jobPostingEntity,
      JobPostingDto.Request request) {
    log.info("채용 단계 생성 - 기술 스택 생성");
    List<TechStack> techStack = request.getTechStack();

    List<JobPostingTechStackEntity> entities = techStack.stream()
        .map(e -> Request.toTechStackEntity(jobPostingEntity, e))
        .collect(Collectors.toList());

    jobPostingTechStackRepository.saveAll(entities);
  }

  /**
   * 채용 공고 수정하기 삭제 후 새로 저장
   *
   * @param jobPostingKey 채용공고 KEY
   * @param request       채용공고 수정 DTO
   */
  public void editJobPostingTechStack(String jobPostingKey, JobPostingDto.Request request) {
    log.info("채용 공고 수정하기 - 기술 스택 삭제 후 새로 저장");
    List<JobPostingTechStackEntity> entities = jobPostingTechStackRepository.findAllByJobPostingKey(
        jobPostingKey);

    jobPostingTechStackRepository.deleteAll(entities);

    List<TechStack> techStack = request.getTechStack();

    List<JobPostingTechStackEntity> techStackEntities = techStack.stream()
        .map(e -> Request.toTechStackEntity(jobPostingKey, e))
        .toList();

    jobPostingTechStackRepository.saveAll(techStackEntities);
  }

  // 채용 공고 삭제하기
  public void deleteJobPostingTechStack(String jobPostingKey) {
    log.info("채용 공고 삭제하기 - 기술 스택 전체 삭제");
    jobPostingTechStackRepository.deleteByJobPostingKey(jobPostingKey);
  }
}