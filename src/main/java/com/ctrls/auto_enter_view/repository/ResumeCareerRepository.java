package com.ctrls.auto_enter_view.repository;

import com.ctrls.auto_enter_view.entity.ResumeCareerEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ResumeCareerRepository extends JpaRepository<ResumeCareerEntity, Long> {

}
