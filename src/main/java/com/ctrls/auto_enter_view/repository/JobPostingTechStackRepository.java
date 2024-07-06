package com.ctrls.auto_enter_view.repository;

import com.ctrls.auto_enter_view.entity.JobPostingTechStackEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JobPostingTechStackRepository extends
    JpaRepository<JobPostingTechStackEntity, Long> {

}