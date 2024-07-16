package com.ctrls.auto_enter_view.repository;

import com.ctrls.auto_enter_view.entity.JobPostingImageEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JobPostingImageRepository extends JpaRepository<JobPostingImageEntity, Long> {
  Optional<JobPostingImageEntity> findByJobPostingKey(String jobPostingKey);

}