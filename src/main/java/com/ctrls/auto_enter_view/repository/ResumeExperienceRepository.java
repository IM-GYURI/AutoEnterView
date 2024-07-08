package com.ctrls.auto_enter_view.repository;

import com.ctrls.auto_enter_view.entity.ResumeExperienceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ResumeExperienceRepository extends JpaRepository<ResumeExperienceEntity, Long> {

}