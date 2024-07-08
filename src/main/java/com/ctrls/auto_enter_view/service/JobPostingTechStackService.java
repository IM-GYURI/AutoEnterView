package com.ctrls.auto_enter_view.service;

import com.ctrls.auto_enter_view.dto.jobPosting.JobPostingDto;
import com.ctrls.auto_enter_view.dto.jobPosting.JobPostingDto.Request;
import com.ctrls.auto_enter_view.entity.JobPostingEntity;
import com.ctrls.auto_enter_view.entity.JobPostingTechStackEntity;
import com.ctrls.auto_enter_view.repository.JobPostingTechStackRepository;
import java.util.ArrayList;
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

  public void createJobPostingTechStack(JobPostingEntity jobPostingEntity,
      JobPostingDto.Request request) {

    List<String> techStack = request.getTechStack();

    List<JobPostingTechStackEntity> entities = techStack.stream()
        .map(e -> Request.toTechStackEntity(jobPostingEntity, e))
        .collect(Collectors.toList());

    jobPostingTechStackRepository.saveAll(entities);
  }

  // 채용 공고 key -> 기술 스택 조회
  public List<String> getTechStackByJobPostingKey(String jobPostingKey) {

    List<JobPostingTechStackEntity> entities = jobPostingTechStackRepository.findByJobPostingKey(
        jobPostingKey);
    List<String> techStack = new ArrayList<>();

    for (JobPostingTechStackEntity entity : entities) {
      techStack.add(entity.getTechName());
    }

    log.info("techStack 가져오기 성공 {}", techStack);
    return techStack;
  }

  public void editJobPostingTechStack(String jobPostingKey, JobPostingDto.Request request) {

    List<JobPostingTechStackEntity> entities = jobPostingTechStackRepository.findByJobPostingKey(
        jobPostingKey);

    jobPostingTechStackRepository.deleteAll(entities);

    List<String> techStack = request.getTechStack();

    List<JobPostingTechStackEntity> techStackEntities = techStack.stream()
        .map(e -> Request.toTechStackEntity(jobPostingKey, e))
        .toList();

    jobPostingTechStackRepository.saveAll(techStackEntities);
  }

  public void deleteJobPostingTechStack(String jobPostingKey) {

    jobPostingTechStackRepository.deleteByJobPostingKey(jobPostingKey);
  }
}