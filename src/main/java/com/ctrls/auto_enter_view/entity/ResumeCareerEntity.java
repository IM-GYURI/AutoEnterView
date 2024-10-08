package com.ctrls.auto_enter_view.entity;

import com.ctrls.auto_enter_view.enums.JobCategory;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
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
@Table(name = "resume_career")
public class ResumeCareerEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String resumeKey;

  private String companyName;

  private JobCategory jobCategory;

  private LocalDate startDate;

  private LocalDate endDate;

  private int calculatedCareer;
}