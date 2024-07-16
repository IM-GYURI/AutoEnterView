package com.ctrls.auto_enter_view.repository;

import com.ctrls.auto_enter_view.entity.ResumeCareerEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ResumeCareerRepository extends JpaRepository<ResumeCareerEntity, Long> {

  List<ResumeCareerEntity> findAllByResumeKey(String resumeKey);

  @Modifying
  @Query("DELETE FROM ResumeCareerEntity r WHERE r.resumeKey = :resumeKey")
  void deleteAllByResumeKey(String resumeKey);
}