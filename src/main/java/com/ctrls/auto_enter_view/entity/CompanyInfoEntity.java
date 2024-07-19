package com.ctrls.auto_enter_view.entity;

import com.ctrls.auto_enter_view.dto.company.CreateCompanyInfoDto.Request;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "company_info")
public class CompanyInfoEntity extends BaseEntity {

  @Id
  private String companyInfoKey;

  @Column(nullable = false, unique = true)
  private String companyKey;

  @Column(nullable = false)
  private String companyName;

  @Column(nullable = false)
  private int employees;

  @Column(nullable = false)
  private LocalDate companyAge;

  @Column(nullable = false)
  private String companyUrl;

  @Column(nullable = false)
  private String boss;

  @Column(nullable = false)
  private String address;

  public void updateEntity(Request request) {

    employees = request.getEmployees();
    companyAge = request.getCompanyAge();
    companyUrl = request.getCompanyUrl();
    boss = request.getBoss();
    address = request.getAddress();
  }
}