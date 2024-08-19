package com.ctrls.auto_enter_view.controller;

import com.ctrls.auto_enter_view.enums.Education;
import com.ctrls.auto_enter_view.enums.JobCategory;
import com.ctrls.auto_enter_view.enums.TechStack;
import com.ctrls.auto_enter_view.service.SearchService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class SearchController {

  private final SearchService searchService;


  @GetMapping("/api/search")
  public List<String> searchJobPostings(
      @RequestParam(required = false) JobCategory position,
      @RequestParam(required = false) List<TechStack> techStacks,
      @RequestParam(required = false) String employmentType,
      @RequestParam(required = false) Integer minCareer,
      @RequestParam(required = false) Integer maxCareer,
      @RequestParam(required = false) Education education) {

    return searchService.searchJobPostings(position, techStacks, employmentType, minCareer,
        maxCareer, education);
  }
}
