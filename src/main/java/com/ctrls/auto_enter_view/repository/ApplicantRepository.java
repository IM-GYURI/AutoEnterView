package com.ctrls.auto_enter_view.repository;

import com.ctrls.auto_enter_view.entity.ApplicantEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ApplicantRepository extends JpaRepository<ApplicantEntity, Long> {

  List<ApplicantEntity> findAllByJobPostingKey(String jobPostingKey);
}