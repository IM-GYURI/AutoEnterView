package com.ctrls.auto_enter_view.entity;

import com.ctrls.auto_enter_view.dto.resume.ResumeDto.Request;
import com.ctrls.auto_enter_view.enums.Education;
import com.ctrls.auto_enter_view.enums.JobCategory;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Builder
@Entity
@Getter
@NoArgsConstructor
@Table(name = "resume")
public class ResumeEntity extends BaseEntity {

  @Id
  private String resumeKey;

  @Column(nullable = false, unique = true)
  private String candidateKey;

  private String title;

  private JobCategory jobWant;

  private String name;

  private String gender;

  private LocalDate birthDate;

  private String email;

  private String phoneNumber;

  private String address;

  private Education education;

  private String schoolName;

  private String portfolio;

  public void updateEntity(Request request) {

    title = request.getTitle();
    jobWant = request.getJobWant();
    name = request.getName();
    gender = request.getGender();
    birthDate = request.getBirthDate();
    email = request.getEmail();
    phoneNumber = request.getPhoneNumber();
    address = request.getAddress();
    education = request.getEducation();
    schoolName = request.getSchoolName();
    portfolio = request.getPortfolio();
  }
}