package com.ctrls.auto_enter_view.service;

import com.ctrls.auto_enter_view.dto.jobposting.JobPostingDto;
import com.ctrls.auto_enter_view.dto.jobposting.JobPostingDto.Request;
import com.ctrls.auto_enter_view.entity.JobPostingEntity;
import com.ctrls.auto_enter_view.entity.JobPostingStepEntity;
import com.ctrls.auto_enter_view.repository.JobPostingStepRepository;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
@Slf4j
public class JobPostingStepService {

  private final JobPostingStepRepository jobPostingStepRepository;

  public void createJobPostingStep(JobPostingEntity entity, JobPostingDto.Request request) {

    List<String> jobPostingStep = request.getJobPostingStep();

    List<JobPostingStepEntity> entities = jobPostingStep.stream()
        .map(e -> Request.toStepEntity(entity, e))
        .collect(Collectors.toList());

    jobPostingStepRepository.saveAll(entities);


  }

  public void editJobPostingStep(String jobPostingKey, JobPostingDto.Request request) {

    List<JobPostingStepEntity> entities = jobPostingStepRepository.findByJobPostingKey(
        jobPostingKey);

    jobPostingStepRepository.deleteAll(entities);

    List<String> jobPostingStep = request.getJobPostingStep();

    List<JobPostingStepEntity> jobPostingStepEntities = jobPostingStep.stream()
        .map(e -> Request.toStepEntity(jobPostingKey, e))
        .toList();

    jobPostingStepRepository.saveAll(jobPostingStepEntities);


  }

  public void deleteJobPostingStep(String jobPostingKey) {
    jobPostingStepRepository.deleteByJobPostingKey(jobPostingKey);

  }
}