package com.ctrls.auto_enter_view.dto.resume;

import com.ctrls.auto_enter_view.entity.ResumeCertificateEntity;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@AllArgsConstructor
@Builder
@Getter
public class CertificateDto {

  private String certificateName;
  private LocalDate certificateDate;

  public ResumeCertificateEntity toEntity(String resumeKey) {

    return ResumeCertificateEntity.builder()
        .resumeKey(resumeKey)
        .certificateName(certificateName)
        .certificateDate(certificateDate)
        .build();
  }
}