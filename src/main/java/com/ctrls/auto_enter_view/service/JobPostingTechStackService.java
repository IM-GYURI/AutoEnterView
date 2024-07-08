package com.ctrls.auto_enter_view.service;

import com.ctrls.auto_enter_view.dto.jobPosting.JobPostingDto;
import com.ctrls.auto_enter_view.dto.jobPosting.JobPostingDto.Request;
import com.ctrls.auto_enter_view.entity.JobPostingEntity;
import com.ctrls.auto_enter_view.entity.JobPostingTechStackEntity;
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

  public void createJobPostingTechStack(JobPostingEntity jobPostingEntity,
      JobPostingDto.Request request) {

    List<String> techStack = request.getTechStack();

    List<JobPostingTechStackEntity> entities = techStack.stream()
        .map(e -> Request.toTechStackEntity(jobPostingEntity, e))
        .collect(Collectors.toList());

    jobPostingTechStackRepository.saveAll(entities);

  }
}
