package com.ctrls.auto_enter_view.repository;

import com.ctrls.auto_enter_view.entity.CandidateListEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CandidateListRepository extends JpaRepository<CandidateListEntity, Long> {

  List<CandidateListEntity> findAllByJobPostingKeyAndJobPostingStepId(String jobPostingKey,
      Long jobPostingStepId);

  boolean existsByCandidateKeyAndJobPostingKey(String candidateKey, String jobPostingKey);
}
