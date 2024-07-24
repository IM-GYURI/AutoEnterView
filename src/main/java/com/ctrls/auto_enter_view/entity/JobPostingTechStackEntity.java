package com.ctrls.auto_enter_view.entity;

import com.ctrls.auto_enter_view.enums.TechStack;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "job_posting_tech_stack")
public class JobPostingTechStackEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String jobPostingKey;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private TechStack techName;

  public void updateEntity(TechStack techName) {

    this.techName = techName;
  }
}