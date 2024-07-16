package com.ctrls.auto_enter_view.repository;

import com.ctrls.auto_enter_view.entity.CandidateListEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface CandidateListRepository extends JpaRepository<CandidateListEntity, String> {

  List<CandidateListEntity> findAllByJobPostingKeyAndJobPostingStepId(String jobPostingKey,
      Long jobPostingStepId);

  boolean existsByJobPostingKeyAndJobPostingStepId(String jobPostingKey, Long jobPostingStepId);

  boolean existsByCandidateKeyAndJobPostingKey(String candidateKey, String jobPostingKey);

  Page<CandidateListEntity> findAllByCandidateKey(String candidateKey, Pageable pageable);

  @Query("SELECT c.candidateKey FROM CandidateListEntity c WHERE c.jobPostingKey = :jobPostingKey AND c.jobPostingStepId = :stepId")
  List<String> findCandidateKeyByJobPostingKeyAndJobPostingStepId(String jobPostingKey,
      Long stepId);

}
