package com.ctrls.auto_enter_view.repository;

import com.ctrls.auto_enter_view.entity.CandidateEntity;
import jakarta.transaction.Transactional;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CandidateRepository extends JpaRepository<CandidateEntity, Long> {

  boolean existsByEmail(String email);

  boolean existsByCandidateKey(String candidateKey);

  Optional<CandidateEntity> findByCandidateKey(String candidateKey);
}
