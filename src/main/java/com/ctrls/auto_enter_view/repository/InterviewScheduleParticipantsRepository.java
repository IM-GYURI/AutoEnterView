package com.ctrls.auto_enter_view.repository;

import com.ctrls.auto_enter_view.entity.InterviewScheduleParticipantsEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InterviewScheduleParticipantsRepository extends
    JpaRepository<InterviewScheduleParticipantsEntity, Long> {

  List<InterviewScheduleParticipantsEntity> findAllByInterviewScheduleKey(
      String interviewScheduleKey);

  List<InterviewScheduleParticipantsEntity> findAllByJobPostingKeyAndJobPostingStepId(
      String jobPostingKey, Long stepId);
}