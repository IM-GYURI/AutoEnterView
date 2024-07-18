package com.ctrls.auto_enter_view.entity;

import com.ctrls.auto_enter_view.dto.interviewschedule.InterviewScheduleParticipantsDto.Request;
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
  private Long id;

  @Column(nullable = false)
  private String interviewScheduleKey;

  @Column(nullable = false)
  private String candidateKey;

  @Column(nullable = false)
  private String candidateName;

  @Column(nullable = false)
  private String jobPostingKey;

  @Column(nullable = false)
  private Long jobPostingStepId;

  private LocalDateTime interviewStartDatetime;

  private LocalDateTime interviewEndDatetime;

  public void updateEntity(Request request) {

    this.interviewStartDatetime = request.getInterviewStartDatetime();
    this.interviewEndDatetime = request.getInterviewEndDatetime();
  }
}