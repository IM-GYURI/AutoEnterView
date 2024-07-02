package com.ctrls.auto_enter_view.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "interview_schedule_participants")
public class InterviewScheduleParticipantsEntity extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long interviewScheduleParticipantsId;

  @Column(nullable = false, unique = true)
  private String interviewScheduleKey;

  @Column(nullable = false, unique = true)
  private String candidateKey;

  @Column(nullable = false, unique = true)
  private String jobPostingKey;

  @Column(nullable = false, unique = true)
  private Long jobPostingStepId;

  private LocalDateTime interviewStartDatetime;

  private LocalDateTime interviewEndDatetime;

}