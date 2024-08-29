package com.ctrls.auto_enter_view.service;

import com.ctrls.auto_enter_view.enums.Education;
import com.ctrls.auto_enter_view.enums.JobCategory;
import com.ctrls.auto_enter_view.enums.TechStack;
import com.ctrls.auto_enter_view.repository.JobPostingRepositoryCustom;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SearchService {

  private final JobPostingRepositoryCustom jobPostingRepositoryCustom;

  public List<String> searchJobPostings(
      JobCategory jobCategory,
      List<TechStack> techStacks,
      String employmentType,
      Integer minCareer,
      Integer maxCareer,
      Education education,
      Long minSalary,
      Long maxSalary) {

    return jobPostingRepositoryCustom.searchJobPostings(jobCategory, techStacks, employmentType,
        minCareer, maxCareer, education, minSalary, maxSalary);
  }

  public List<String> searchJobPostingsByKeyword(String keyword) {
    return jobPostingRepositoryCustom.searchJobPostingsByKeyword(keyword);
  }
}
