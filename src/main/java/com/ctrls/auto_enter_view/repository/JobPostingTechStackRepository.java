package com.ctrls.auto_enter_view.repository;

import com.ctrls.auto_enter_view.entity.JobPostingTechStackEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JobPostingTechStackRepository extends
    JpaRepository<JobPostingTechStackEntity, Long> {

  List<JobPostingTechStackEntity> findAllByJobPostingKey(String jobPostingKey);

  void deleteByJobPostingKey(String jobPostingKey);
}