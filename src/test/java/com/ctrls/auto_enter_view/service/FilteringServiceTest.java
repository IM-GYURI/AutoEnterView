package com.ctrls.auto_enter_view.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ctrls.auto_enter_view.component.FilteringJob;
import com.ctrls.auto_enter_view.component.ScoringJob;
import com.ctrls.auto_enter_view.exception.CustomException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.SimpleTrigger;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;

@ExtendWith(MockitoExtension.class)
class FilteringServiceTest {

  @Mock
  private Scheduler scheduler;

  @InjectMocks
  private FilteringService filteringService;

  @Test
  @DisplayName("스코어링 + 필터링 스케줄링 : 성공")
  public void testScheduleResumeScoringJob_Success() throws Exception {
    String jobPostingKey = "jobPostingKey";
    LocalDate endDate = LocalDate.now();
    LocalDateTime filteringDateTime = LocalDateTime.now().plusMinutes(3);
    Date nextExecutionDate = Date.from(
        filteringDateTime.atZone(ZoneId.systemDefault()).toInstant());

    when(scheduler.checkExists(any(JobKey.class))).thenReturn(true);
    when(scheduler.deleteJob(any(JobKey.class))).thenReturn(true);
    when(scheduler.scheduleJob(any(JobDetail.class), any(Trigger.class))).thenReturn(
        nextExecutionDate);

    filteringService.scheduleResumeScoringJob(jobPostingKey, endDate);

    JobKey jobKeyA = JobKey.jobKey("resumeScoringJob", "group1");
    JobKey jobKeyB = JobKey.jobKey("filteringJob", "group1");

    verify(scheduler, times(1)).checkExists(jobKeyA);
    verify(scheduler, times(1)).checkExists(jobKeyB);
    verify(scheduler, times(1)).deleteJob(jobKeyA);
    verify(scheduler, times(1)).deleteJob(jobKeyB);

    JobDetail jobDetailA = JobBuilder.newJob(ScoringJob.class)
        .withIdentity("resumeScoringJob", "group1")
        .setJobData(new JobDataMap() {{
          put("jobPostingKey", jobPostingKey);
        }})
        .build();

    SimpleTrigger triggerA = TriggerBuilder.newTrigger()
        .withIdentity("resumeScoringTrigger", "group1")
        .startAt(Date.from(filteringDateTime.atZone(ZoneId.systemDefault()).toInstant()))
        .withSchedule(
            SimpleScheduleBuilder.simpleSchedule().withMisfireHandlingInstructionFireNow())
        .build();

    JobDetail jobDetailB = JobBuilder.newJob(FilteringJob.class)
        .withIdentity("filteringJob", "group1")
        .setJobData(new JobDataMap() {{
          put("jobPostingKey", jobPostingKey);
        }})
        .build();

    SimpleTrigger triggerB = TriggerBuilder.newTrigger()
        .withIdentity("filteringTrigger", "group1")
        .startAt(
            Date.from(filteringDateTime.plusMinutes(1).atZone(ZoneId.systemDefault()).toInstant()))
        .withSchedule(
            SimpleScheduleBuilder.simpleSchedule().withMisfireHandlingInstructionFireNow())
        .build();

    verify(scheduler, times(1)).scheduleJob(eq(jobDetailA), eq(triggerA));
    verify(scheduler, times(1)).scheduleJob(eq(jobDetailB), eq(triggerB));
  }

  @Test
  @DisplayName("스코어링 + 필터링 스케줄링 : 실패 - checkExists 실패")
  public void testScheduleResumeScoringJob_CheckExistsFailure() throws Exception {
    String jobPostingKey = "jobPostingKey";
    LocalDate endDate = LocalDate.now();

    when(scheduler.checkExists(any(JobKey.class))).thenThrow(
        new SchedulerException("checkExists failed"));

    assertThrows(CustomException.class,
        () -> filteringService.scheduleResumeScoringJob(jobPostingKey, endDate));

    verify(scheduler, times(1)).checkExists(any(JobKey.class));
    verify(scheduler, never()).deleteJob(any(JobKey.class));
    verify(scheduler, never()).scheduleJob(any(JobDetail.class), any(Trigger.class));
  }

  @Test
  @DisplayName("스코어링 + 필터링 스케줄링 실패 - deleteJob 실패")
  public void testScheduleResumeScoringJob_DeleteJobFailure() throws Exception {
    String jobPostingKey = "jobPostingKey";
    LocalDate endDate = LocalDate.now();

    when(scheduler.checkExists(any(JobKey.class))).thenReturn(true);
    when(scheduler.deleteJob(any(JobKey.class))).thenThrow(
        new SchedulerException("deleteJob failed"));

    assertThrows(CustomException.class,
        () -> filteringService.scheduleResumeScoringJob(jobPostingKey, endDate));

    verify(scheduler, times(1)).checkExists(any(JobKey.class));
    verify(scheduler, times(1)).deleteJob(any(JobKey.class));
    verify(scheduler, never()).scheduleJob(any(JobDetail.class), any(Trigger.class));
  }
}