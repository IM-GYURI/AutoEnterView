package com.ctrls.auto_enter_view.dto.interviewschedule;

import com.ctrls.auto_enter_view.entity.InterviewScheduleEntity;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.cglib.core.Local;
import org.springframework.format.annotation.DateTimeFormat;

public class InterviewScheduleDto {

  @Getter
  @Builder
  @AllArgsConstructor
  @NoArgsConstructor
  public static class Request {

    private LocalDate startDate;

    @DateTimeFormat(pattern = "HH:mm")
    private LocalTime startTime;

    @DateTimeFormat(pattern = "mm")
    private LocalTime term;

    private Integer times;

    public static InterviewScheduleEntity toEntity(String jobPostingKey, Long stepId,
        List<Request> request) {

      LocalDate firstDate = request.get(0).getStartDate();
      LocalDate lastDate = request.get(request.size() - 1).getStartDate();

      return InterviewScheduleEntity.builder()
          .jobPostingStepId(stepId)
          .jobPostingKey(jobPostingKey)
          .firstInterviewDate(firstDate)
          .lastInterviewDate(lastDate)
          .build();
    }

  }

}
