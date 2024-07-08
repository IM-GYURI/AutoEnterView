package com.ctrls.auto_enter_view.repository;

import com.ctrls.auto_enter_view.entity.JobPostingEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JobPostingRepository extends JpaRepository<JobPostingEntity, String> {

  JobPostingEntity findByJobPostingKey(String jobPostingKey);

  void deleteByJobPostingKey(String jobPostingKey);
}