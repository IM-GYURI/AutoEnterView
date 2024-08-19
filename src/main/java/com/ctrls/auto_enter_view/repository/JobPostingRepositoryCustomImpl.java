package com.ctrls.auto_enter_view.repository;

import com.ctrls.auto_enter_view.entity.QJobPostingEntity;
import com.ctrls.auto_enter_view.entity.QJobPostingTechStackEntity;
import com.ctrls.auto_enter_view.enums.Education;
import com.ctrls.auto_enter_view.enums.JobCategory;
import com.ctrls.auto_enter_view.enums.TechStack;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class JobPostingRepositoryCustomImpl implements JobPostingRepositoryCustom {

  private final JPAQueryFactory queryFactory;

  @Override
  public List<String> searchJobPostings(
      JobCategory jobCategory,
      List<TechStack> techStacks,
      String employmentType,
      Integer minCareer, // 최소 경력
      Integer maxCareer, // 최대 경력
      Education education) {

    QJobPostingEntity jobPosting = QJobPostingEntity.jobPostingEntity;
    QJobPostingTechStackEntity jobPostingTechStack = QJobPostingTechStackEntity.jobPostingTechStackEntity;

    BooleanBuilder jobPostingPredicate = new BooleanBuilder();
    BooleanBuilder techStackPredicate = new BooleanBuilder();

    // 채용직무 필터링
    if (jobCategory != null) {
      jobPostingPredicate.and(jobPosting.jobCategory.eq(jobCategory));
    }

    // 기술스택 필터링
    if (techStacks != null && !techStacks.isEmpty()) {
      for (TechStack techStack : techStacks) {
        techStackPredicate.and(jobPostingTechStack.techName.eq(techStack));
      }
    }

    // 고용형태 필터링
    if (employmentType != null && !employmentType.isEmpty()) {
      jobPostingPredicate.and(jobPosting.employmentType.eq(employmentType));
    }

    // 필요 경력 범위 필터링
    if (minCareer != null && maxCareer != null) {
      jobPostingPredicate.and(jobPosting.career.between(minCareer, maxCareer));
    } else if (minCareer != null) {
      jobPostingPredicate.and(jobPosting.career.goe(minCareer));
    } else if (maxCareer != null) {
      jobPostingPredicate.and(jobPosting.career.loe(maxCareer));
    }

    // 필요 학력 필터링
    if (education != null) {
      jobPostingPredicate.and(jobPosting.education.eq(education));
    }

    List<String> matchingJobPostingKeys = queryFactory.selectDistinct(jobPosting.jobPostingKey)
        .from(jobPosting)
        .leftJoin(jobPostingTechStack)
        .on(jobPosting.jobPostingKey.eq(jobPostingTechStack.jobPostingKey))
        .where(jobPostingPredicate.and(techStackPredicate))
        .fetch();

    return matchingJobPostingKeys;
  }
}
