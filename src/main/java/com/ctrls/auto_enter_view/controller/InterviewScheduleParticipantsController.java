package com.ctrls.auto_enter_view.controller;

import com.ctrls.auto_enter_view.repository.InterviewScheduleParticipantsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
public class InterviewScheduleParticipantsController {

  private final InterviewScheduleParticipantsRepository interviewScheduleParticipantsRepository;

  
}