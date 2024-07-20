package com.ctrls.auto_enter_view.repository;

import com.ctrls.auto_enter_view.entity.ResumeTechStackEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ResumeTechStackRepository extends JpaRepository<ResumeTechStackEntity, Long> {

  List<ResumeTechStackEntity> findAllByResumeKey(String resumeKey);

  @Modifying
  @Query("DELETE FROM ResumeTechStackEntity r WHERE r.resumeKey = :resumeKey")
  void deleteAllByResumeKey(String resumeKey);

  List<ResumeTechStackEntity> findTechStacksByResumeKey(String resumeKey);
}