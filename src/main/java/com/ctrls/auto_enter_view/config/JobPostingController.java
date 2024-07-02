package com.ctrls.auto_enter_view.config;

import com.ctrls.auto_enter_view.service.JobPostingService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
public class JobPostingController {

  private final JobPostingService jobPostingService;
}
