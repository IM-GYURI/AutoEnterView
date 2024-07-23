package com.ctrls.auto_enter_view.dto.resume;

import com.ctrls.auto_enter_view.entity.ResumeCertificateEntity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@AllArgsConstructor
@Builder
@Getter
public class CertificateDto {

  @NotBlank(message = "자격증 이름은 필수 입력값 입니다.")
  private String certificateName;

  @NotNull(message = "취득일은 필수 입력값 입니다.")
  private LocalDate certificateDate;

  public ResumeCertificateEntity toEntity(String resumeKey) {

    return ResumeCertificateEntity.builder()
        .resumeKey(resumeKey)
        .certificateName(certificateName)
        .certificateDate(certificateDate)
        .build();
  }
}