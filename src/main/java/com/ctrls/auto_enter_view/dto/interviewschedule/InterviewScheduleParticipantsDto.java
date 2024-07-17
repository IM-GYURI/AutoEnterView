package com.ctrls.auto_enter_view.dto.interviewschedule;

import com.ctrls.auto_enter_view.entity.InterviewScheduleParticipantsEntity;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class InterviewScheduleParticipantsDto {


  @Getter
  @Builder
  @AllArgsConstructor
  @NoArgsConstructor
  public static class Response {

    private String interviewScheduleKey;

    private String candidateKey;

    private String candidateName;

    private LocalDateTime interviewStartDateTime;

    private LocalDateTime interviewEndDateTime;


    public static Response fromEntity(InterviewScheduleParticipantsEntity entity) {

      return Response.builder()
          .interviewScheduleKey(entity.getInterviewScheduleKey())
          .candidateKey(entity.getCandidateKey())
          .candidateName(entity.getCandidateName())
          .interviewStartDateTime(entity.getInterviewStartDatetime())
          .interviewEndDateTime(entity.getInterviewEndDatetime())
          .build();
    }

  }

}
