package com.ctrls.auto_enter_view.repository;

import com.ctrls.auto_enter_view.entity.AppliedJobPostingEntity;
import java.time.LocalDate;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface AppliedJobPostingRepository extends JpaRepository<AppliedJobPostingEntity, Long> {

  Page<AppliedJobPostingEntity> findAllByCandidateKey(String candidateKey, Pageable pageable);

  Optional<AppliedJobPostingEntity> findByCandidateKeyAndJobPostingKey(String candidateKey,
      String jobPostingKey);

  @Modifying
  @Query("UPDATE AppliedJobPostingEntity a SET a.endDate = :endDate WHERE a.jobPostingKey = :jobPostingKey")
  void updateEndDateByJobPostingKey(LocalDate endDate, String jobPostingKey);
}