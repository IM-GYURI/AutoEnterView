package com.ctrls.auto_enter_view.repository;

import com.ctrls.auto_enter_view.enums.Education;
import com.ctrls.auto_enter_view.enums.JobCategory;
import com.ctrls.auto_enter_view.enums.TechStack;
import java.util.List;

public interface JobPostingRepositoryCustom {

  /**
   * 필터링된 채용 공고의 jobPostingKey 조회
   *
   * @param jobCategory    채용 직무
   * @param techStacks     기술 스택 목록
   * @param employmentType 고용 형태
   * @param minCareer      최소 경력
   * @param maxCareer      최소 경력
   * @param education      학력
   * @return 필터링된 jobPostingKey 목록
   */
  List<String> searchJobPostings(
      JobCategory jobCategory,
      List<TechStack> techStacks,
      String employmentType,
      Integer minCareer,
      Integer maxCareer,
      Education education,
      Long minSalary,
      Long maxSalary);

  List<String> searchJobPostingsByKeyword(String keyword);
}
