package com.ctrls.auto_enter_view.entity;

import com.ctrls.auto_enter_view.enums.UserRole;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@Builder
@Entity
@Getter
@NoArgsConstructor
@Table(name = "company")
public class CompanyEntity extends BaseEntity {

  @Id
  private String companyKey;

  @Column(nullable = false)
  private String email;

  @Column(nullable = false)
  @Setter
  private String password;

  @Column(nullable = false)
  private String companyName;

  @Column(nullable = false)
  private String companyNumber;

  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  private UserRole role;
}