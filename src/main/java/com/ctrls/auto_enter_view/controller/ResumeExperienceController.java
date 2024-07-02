package com.ctrls.auto_enter_view.controller;

import com.ctrls.auto_enter_view.service.ResumeExperienceService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
public class ResumeExperienceController {

  private final ResumeExperienceService resumeExperienceService;
}
