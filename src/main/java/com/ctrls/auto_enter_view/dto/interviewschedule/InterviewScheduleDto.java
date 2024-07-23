package com.ctrls.auto_enter_view.dto.interviewschedule;

import com.ctrls.auto_enter_view.entity.InterviewScheduleEntity;
import com.ctrls.auto_enter_view.entity.InterviewScheduleParticipantsEntity;
import com.ctrls.auto_enter_view.util.KeyGenerator;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class InterviewScheduleDto {

  @Getter
  @Builder
  @AllArgsConstructor
  @NoArgsConstructor
  public static class Request {

    @NotNull(message = "시작일은 필수 입력값 입니다.")
    private LocalDate startDate;

    @NotNull(message = "시작시간은 필수 입력값 입니다.")
    private LocalTime startTime;

    @Min(value = 1, message = "구간은 1분 이상이어야 합니다.")
    private int term;

    @Min(value = 1, message = "횟수는 1분 이상이어야 합니다.")
    private int times;

    public static InterviewScheduleEntity toEntity(String jobPostingKey, Long stepId,
        List<Request> request) {

      LocalDate firstDate = request.get(0).getStartDate();
      LocalDate lastDate = request.get(request.size() - 1).getStartDate();

      return InterviewScheduleEntity.builder()
          .interviewScheduleKey(KeyGenerator.generateKey())
          .jobPostingStepId(stepId)
          .jobPostingKey(jobPostingKey)
          .firstInterviewDate(firstDate)
          .lastInterviewDate(lastDate)
          .build();
    }

    public static InterviewScheduleParticipantsEntity toParticipantsEntity(String jobPostingKey,
        String interviewScheduleKey,
        Long stepId, LocalDateTime startDateTime, LocalDateTime endDateTime, String candidateKey,
        String candidateName) {

      return InterviewScheduleParticipantsEntity.builder()
          .interviewScheduleKey(interviewScheduleKey)
          .candidateKey(candidateKey)
          .candidateName(candidateName)
          .jobPostingKey(jobPostingKey)
          .jobPostingStepId(stepId)
          .interviewStartDatetime(startDateTime)
          .interviewEndDatetime(endDateTime)
          .build();
    }
  }

  @Getter
  @Builder
  @AllArgsConstructor
  @NoArgsConstructor
  public static class Response {

    private String interviewScheduleKey;
  }

  @Getter
  @Builder
  @AllArgsConstructor
  @NoArgsConstructor
  public static class TaskRequest {

    @NotNull(message = "종료일자는 필수 입력값 입니다.")
    private LocalDate endDate;

    public static InterviewScheduleEntity toEntity(String jobPostingKey, Long stepId,
        TaskRequest taskRequest) {

      return InterviewScheduleEntity.builder()
          .interviewScheduleKey(KeyGenerator.generateKey())
          .jobPostingStepId(stepId)
          .jobPostingKey(jobPostingKey)
          .lastInterviewDate(taskRequest.getEndDate())
          .build();
    }
  }
}