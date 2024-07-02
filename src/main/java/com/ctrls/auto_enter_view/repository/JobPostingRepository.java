package com.ctrls.auto_enter_view.repository;

import com.ctrls.auto_enter_view.entity.JobPosting;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JobPostingRepository extends JpaRepository<JobPosting, Long> {

}
