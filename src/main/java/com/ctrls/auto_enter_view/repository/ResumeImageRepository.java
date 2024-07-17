package com.ctrls.auto_enter_view.repository;

import com.ctrls.auto_enter_view.entity.ResumeImageEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ResumeImageRepository extends JpaRepository<ResumeImageEntity, Long> {

  Optional<ResumeImageEntity> findByResumeKey(String resumeKey);

}