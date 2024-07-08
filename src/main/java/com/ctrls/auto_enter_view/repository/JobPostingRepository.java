package com.ctrls.auto_enter_view.repository;

import com.ctrls.auto_enter_view.entity.JobPostingEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JobPostingRepository extends JpaRepository<JobPostingEntity, String> {

  Optional<JobPostingEntity> findByJobPostingKey(String jobPostingKey);

  List<JobPostingEntity> findAllByCompanyKey(String companyKey);
}
