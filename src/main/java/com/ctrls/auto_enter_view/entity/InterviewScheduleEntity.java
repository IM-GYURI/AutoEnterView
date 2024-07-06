package com.ctrls.auto_enter_view.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "interview_schedule")
public class InterviewScheduleEntity extends BaseEntity {

  @Id
  private String interviewScheduleKey;

  @Column(nullable = false, unique = true)
  private Long jobPostingStepId;

  @Column(nullable = false, unique = true)
  private String jobPostingKey;

  @Column(nullable = false)
  private LocalDate firstInterviewDate;

  @Column(nullable = false)
  private LocalDate lastInterviewDate;
}