package com.ctrls.auto_enter_view.dto.company;

import com.ctrls.auto_enter_view.entity.CompanyInfoEntity;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class CreateCompanyInfoDto {

  @AllArgsConstructor
  @Builder
  @Getter
  @NoArgsConstructor
  public static class Request {

    @Min(value = 1, message = "사원수는 1명 이상이어야 합니다.")
    private int employees;

    @NotNull(message = "설립일은 필수 입력값 입니다.")
    private LocalDate companyAge;

    @NotBlank(message = "회사 홈페이지 주소는 필수 입력값 입니다.")
    private String companyUrl;

    @NotBlank(message = "대표자는 필수 입력값 입니다.")
    private String boss;

    @NotBlank(message = "주소는 필수 입력값 입니다.")
    private String address;

    public CompanyInfoEntity toEntity(String key, String companyKey, String companyName) {

      return CompanyInfoEntity.builder()
          .companyInfoKey(key)
          .companyKey(companyKey)
          .companyName(companyName)
          .employees(employees)
          .companyAge(companyAge)
          .companyUrl(companyUrl)
          .boss(boss)
          .address(address)
          .build();
    }
  }
}