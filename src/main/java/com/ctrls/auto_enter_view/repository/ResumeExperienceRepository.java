package com.ctrls.auto_enter_view.repository;

import com.ctrls.auto_enter_view.entity.ResumeExperienceEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ResumeExperienceRepository extends JpaRepository<ResumeExperienceEntity, Long> {

  List<ResumeExperienceEntity> findAllByResumeKey(String resumeKey);

  @Modifying
  @Query("DELETE FROM ResumeExperienceEntity r WHERE r.resumeKey = :resumeKey")
  void deleteAllByResumeKey(String resumeKey);

  int countAllByResumeKey(String resumeKey);
}