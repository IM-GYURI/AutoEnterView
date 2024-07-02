package com.ctrls.auto_enter_view.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "job_posting")
public class JobPostingEntity extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long jobPostingId;

  @Column(nullable = false)
  private String jobPostingKey;

  @Column(nullable = false)
  private String companyKey;

  @Column(nullable = false)
  private String title;

  private String jobCategory;

  private Integer career;

  @Column(nullable = false)
  private String workLocation;

  private String education;

  @Column(nullable = false)
  private String employmentType;

  @Column(nullable = false)
  private String salary;

  @Column(nullable = false)
  private String workTime;

  @Column(nullable = false)
  private LocalDateTime startDateTime;

  @Column(nullable = false)
  private LocalDateTime endDateTime;

  private String jobPostingContent;
}
