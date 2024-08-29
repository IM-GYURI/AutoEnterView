package com.ctrls.auto_enter_view.repository;

import static com.ctrls.auto_enter_view.entity.QCompanyEntity.companyEntity;
import static com.ctrls.auto_enter_view.entity.QJobPostingEntity.jobPostingEntity;
import static com.ctrls.auto_enter_view.entity.QJobPostingTechStackEntity.jobPostingTechStackEntity;

import com.ctrls.auto_enter_view.entity.QJobPostingEntity;
import com.ctrls.auto_enter_view.entity.QJobPostingTechStackEntity;
import com.ctrls.auto_enter_view.enums.Education;
import com.ctrls.auto_enter_view.enums.JobCategory;
import com.ctrls.auto_enter_view.enums.TechStack;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.BooleanExpression;
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
      Integer minCareer,
      Integer maxCareer,
      Education education,
      Long minSalary,
      Long maxSalary) {

    QJobPostingEntity jobPosting = jobPostingEntity;
    QJobPostingTechStackEntity jobPostingTechStack = jobPostingTechStackEntity;

    BooleanBuilder jobPostingPredicate = new BooleanBuilder();
    BooleanBuilder techStackPredicate = new BooleanBuilder();

    if (jobCategory != null) {
      jobPostingPredicate.and(jobPosting.jobCategory.eq(jobCategory));
    }

    if (techStacks != null && !techStacks.isEmpty()) {
      for (TechStack techStack : techStacks) {
        techStackPredicate.and(jobPostingTechStack.techName.eq(techStack));
      }
    }

    if (employmentType != null && !employmentType.isEmpty()) {
      jobPostingPredicate.and(jobPosting.employmentType.eq(employmentType));
    }

    if (minCareer != null && maxCareer != null) {
      jobPostingPredicate.and(jobPosting.career.between(minCareer, maxCareer));
    } else if (minCareer != null) {
      jobPostingPredicate.and(jobPosting.career.goe(minCareer));
    } else if (maxCareer != null) {
      jobPostingPredicate.and(jobPosting.career.loe(maxCareer));
    }

    if (education != null) {
      jobPostingPredicate.and(jobPosting.education.eq(education));
    }

    if (minSalary != null && maxSalary != null) {
      jobPostingPredicate.and(jobPosting.salary.between(minSalary, maxSalary));
    } else if (minSalary != null) {
      jobPostingPredicate.and(jobPosting.salary.goe(minSalary));
    } else if (maxSalary != null) {
      jobPostingPredicate.and(jobPosting.salary.loe(maxSalary));
    }

    List<String> matchingJobPostingKeys = queryFactory.selectDistinct(jobPosting.jobPostingKey)
        .from(jobPosting)
        .leftJoin(jobPostingTechStack)
        .on(jobPosting.jobPostingKey.eq(jobPostingTechStack.jobPostingKey))
        .where(jobPostingPredicate.and(techStackPredicate))
        .fetch();

    return matchingJobPostingKeys;
  }

  @Override
  public List<String> searchJobPostingsByKeyword(String keyword) {
    BooleanExpression jobPostingCondition = jobPostingEntity.title.containsIgnoreCase(keyword)
        .or(jobPostingEntity.jobCategory.stringValue().containsIgnoreCase(keyword))
        .or(jobPostingEntity.career.stringValue().containsIgnoreCase(keyword))
        .or(jobPostingEntity.education.stringValue().containsIgnoreCase(keyword))
        .or(jobPostingEntity.employmentType.containsIgnoreCase(keyword))
        .or(jobPostingEntity.salary.stringValue().containsIgnoreCase(keyword))
        .or(jobPostingEntity.jobPostingContent.containsIgnoreCase(keyword));

    BooleanExpression techStackCondition = jobPostingTechStackEntity.techName.stringValue()
        .containsIgnoreCase(keyword);

    BooleanExpression companyCondition = companyEntity.companyName.containsIgnoreCase(keyword);

    return queryFactory.selectDistinct(jobPostingEntity.jobPostingKey)
        .from(jobPostingEntity)
        .leftJoin(jobPostingTechStackEntity)
        .on(jobPostingEntity.jobPostingKey.eq(jobPostingTechStackEntity.jobPostingKey))
        .leftJoin(companyEntity).on(jobPostingEntity.companyKey.eq(companyEntity.companyKey))
        .where(jobPostingCondition.or(techStackCondition).or(companyCondition))
        .fetch();
  }
}
