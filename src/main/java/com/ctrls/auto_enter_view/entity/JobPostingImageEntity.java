package com.ctrls.auto_enter_view.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "job_posting_image")
public class JobPostingImageEntity extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long jobPostingImageId;

  @Column(nullable = false, unique = true)
  private String jobPostingKey;

  @Column(nullable = false)
  private String fileName;

  @Column(nullable = false)
  private String originalFileName;

  @Column(nullable = false)
  private String filePath;

}
