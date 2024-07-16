package com.ctrls.auto_enter_view.dto.mailAlarmInfo;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class MailAlarmInfoDto {

  private String mailContent;

  @NotNull
  @Future(message = "예약 발송 시간은 현재 시간 이후여야 합니다.")
  private LocalDateTime mailSendDateTime;
}
