package com.ctrls.auto_enter_view.repository;

import com.ctrls.auto_enter_view.entity.ResumeEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ResumeRepository extends JpaRepository<ResumeEntity, Long> {

  Optional<ResumeEntity> findByCandidateKey(String candidateKey);

  boolean existsByCandidateKey(String candidateKey);

  @Modifying
  @Query("DELETE FROM ResumeEntity r WHERE r.candidateKey = :candidateKey")
  void deleteByCandidateKey(String candidateKey);
}