package com.ctrls.auto_enter_view.repository;

import com.ctrls.auto_enter_view.entity.InterviewScheduleEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface InterviewScheduleRepository extends
    JpaRepository<InterviewScheduleEntity, String> {

  Optional<InterviewScheduleEntity> findByJobPostingStepId(Long stepId);

  Optional<InterviewScheduleEntity> findByInterviewScheduleKey(String interviewScheduleKey);

  @Query("SELECT i.interviewScheduleKey FROM InterviewScheduleEntity i WHERE i.jobPostingKey = :jobPostingKey AND i.jobPostingStepId = :stepId")
  Optional<String> findInterviewScheduleKeyByJobPostingKeyAndStepId(String jobPostingKey,
      Long stepId);

  Optional<InterviewScheduleEntity> findByJobPostingKeyAndJobPostingStepId(String jobPostingKey,
      Long stepId);

}