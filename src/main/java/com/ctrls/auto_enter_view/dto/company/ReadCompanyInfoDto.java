package com.ctrls.auto_enter_view.dto.company;

import com.ctrls.auto_enter_view.entity.CompanyInfoEntity;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class ReadCompanyInfoDto {

  @AllArgsConstructor
  @Builder
  @Getter
  @NoArgsConstructor
  public static class Response {

    private String companyName;
    private int employees;
    private LocalDate companyAge;
    private String companyUrl;
    private String boss;
    private String address;

    public static Response toDto(CompanyInfoEntity e) {

      return Response.builder()
          .companyName(e.getCompanyName())
          .employees(e.getEmployees())
          .companyAge(e.getCompanyAge())
          .companyUrl(e.getCompanyUrl())
          .boss(e.getBoss())
          .address(e.getAddress())
          .build();
    }
  }
}