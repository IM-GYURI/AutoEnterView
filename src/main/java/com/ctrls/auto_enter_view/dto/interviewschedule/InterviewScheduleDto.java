package com.ctrls.auto_enter_view.dto.interviewschedule;

import java.sql.Time;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

public class InterviewScheduleDto {

  @Getter
  @Builder
  @AllArgsConstructor
  @NoArgsConstructor
  public static class Request {

    LocalDate startDate;

    @DateTimeFormat(pattern = "HH:mm")
    Time startTime;

    @DateTimeFormat(pattern = "mm")
    Time term;

    Long times;

  }

}
