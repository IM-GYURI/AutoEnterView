package com.ctrls.auto_enter_view.repository;

import com.ctrls.auto_enter_view.entity.JobPostingStepEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JobPostingStepRepository extends JpaRepository<JobPostingStepEntity, Long> {

  List<JobPostingStepEntity> findAllByJobPostingKey(String jobPostingKey);

  boolean existsByIdAndJobPostingKey(Long stepId, String jobPostingKey);
}
