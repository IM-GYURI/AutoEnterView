package com.ctrls.auto_enter_view.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "candidate_list")
public class CandidateListEntity extends BaseEntity {

  @Id
  private String candidateListKey;

  @Column(nullable = false, unique = true)
  private Long jobPostingStepId;

  @Column(nullable = false, unique = true)
  private String jobPostingKey;

  @Column(nullable = false, unique = true)
  private String candidateKey;

  @Column(nullable = false)
  private String candidateName;
}