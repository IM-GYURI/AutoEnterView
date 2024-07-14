package com.ctrls.auto_enter_view.repository;

import com.ctrls.auto_enter_view.entity.CandidateListEntity;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CandidateListRepository extends JpaRepository<CandidateListEntity, String> {

  List<CandidateListEntity> findAllByJobPostingKeyAndJobPostingStepId(String jobPostingKey,
      Long jobPostingStepId);

  boolean existsByJobPostingKeyAndJobPostingStepId(String jobPostingKey, Long jobPostingStepId);

  boolean existsByCandidateKeyAndJobPostingKey(String candidateKey, String jobPostingKey);

  Page<CandidateListEntity> findAllByCandidateKey(String candidateKey, Pageable pageable);

}
