package com.ctrls.auto_enter_view.repository;

import com.ctrls.auto_enter_view.entity.CandidateEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CandidateRepository extends JpaRepository<CandidateEntity, String> {

  boolean existsByEmail(String email);

  boolean existsByCandidateKey(String candidateKey);

  Optional<CandidateEntity> findByEmail(String email);

  Optional<CandidateEntity> findByCandidateKey(String candidateKey);

  Optional<CandidateEntity> findByNameAndPhoneNumber(String name, String phoneNumber);
}