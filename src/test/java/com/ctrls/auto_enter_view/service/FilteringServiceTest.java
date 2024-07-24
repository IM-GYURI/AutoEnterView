package com.ctrls.auto_enter_view.service;

import static com.ctrls.auto_enter_view.enums.Education.BACHELOR;
import static com.ctrls.auto_enter_view.enums.ErrorCode.SCHEDULE_FAILED;
import static com.ctrls.auto_enter_view.enums.JobCategory.BACKEND;
import static com.ctrls.auto_enter_view.enums.UserRole.ROLE_CANDIDATE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ctrls.auto_enter_view.component.FilteringJob;
import com.ctrls.auto_enter_view.component.ScoringJob;
import com.ctrls.auto_enter_view.entity.ApplicantEntity;
import com.ctrls.auto_enter_view.entity.AppliedJobPostingEntity;
import com.ctrls.auto_enter_view.entity.CandidateEntity;
import com.ctrls.auto_enter_view.entity.CandidateListEntity;
import com.ctrls.auto_enter_view.entity.JobPostingEntity;
import com.ctrls.auto_enter_view.entity.JobPostingStepEntity;
import com.ctrls.auto_enter_view.exception.CustomException;
import com.ctrls.auto_enter_view.repository.ApplicantRepository;
import com.ctrls.auto_enter_view.repository.AppliedJobPostingRepository;
import com.ctrls.auto_enter_view.repository.CandidateListRepository;
import com.ctrls.auto_enter_view.repository.CandidateRepository;
import com.ctrls.auto_enter_view.repository.JobPostingRepository;
import com.ctrls.auto_enter_view.repository.JobPostingStepRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
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
import org.quartz.TriggerKey;

@ExtendWith(MockitoExtension.class)
class FilteringServiceTest {

  @Mock
  private Scheduler scheduler;

  @Mock
  private ApplicantRepository applicantRepository;

  @Mock
  private CandidateRepository candidateRepository;

  @Mock
  private CandidateListRepository candidateListRepository;

  @Mock
  private AppliedJobPostingRepository appliedJobPostingRepository;

  @Mock
  private JobPostingRepository jobPostingRepository;

  @Mock
  private JobPostingStepRepository jobPostingStepRepository;

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
  @DisplayName("스코어링 + 필터링 스케줄링 : 실패 - SCHEDULE_FAILED")
  public void testScheduleResumeScoringJob_ScheduleFailed() throws Exception {
    String jobPostingKey = "jobPostingKey";
    LocalDate endDate = LocalDate.now();

    JobKey resumeScoringJobKey = JobKey.jobKey("resumeScoringJob", "group1");
    JobKey filteringJobKey = JobKey.jobKey("filteringJob", "group1");

    when(scheduler.checkExists(resumeScoringJobKey)).thenReturn(true);
    when(scheduler.deleteJob(resumeScoringJobKey)).thenReturn(true);

    doThrow(new SchedulerException("scheduleJob failed")).when(scheduler)
        .scheduleJob(any(JobDetail.class), any(Trigger.class));

    CustomException exception = assertThrows(CustomException.class,
        () -> filteringService.scheduleResumeScoringJob(jobPostingKey, endDate));

    assertEquals(SCHEDULE_FAILED, exception.getErrorCode());

    verify(scheduler).checkExists(resumeScoringJobKey);
    verify(scheduler).deleteJob(resumeScoringJobKey);
    verify(scheduler, never()).checkExists(filteringJobKey);
    verify(scheduler, never()).deleteJob(filteringJobKey);
    verify(scheduler).scheduleJob(any(JobDetail.class), any(Trigger.class));
  }

  @Test
  @DisplayName("스케줄링 취소 : 성공")
  public void testUnscheduleResumeScoringJob_Success() throws Exception {
    String jobPostingKey = "jobPostingKey";

    TriggerKey scoringTriggerKey = TriggerKey.triggerKey("resumeScoringTrigger-" + jobPostingKey,
        "group1");
    TriggerKey filteringTriggerKey = TriggerKey.triggerKey("filteringTrigger-" + jobPostingKey,
        "group1");

    filteringService.unscheduleResumeScoringJob(jobPostingKey);

    verify(scheduler).unscheduleJob(scoringTriggerKey);
    verify(scheduler).unscheduleJob(filteringTriggerKey);
  }

  @Test
  @DisplayName("스케줄링 취소 : 실패 - UNSCHEDULE_FAILED")
  public void testUnscheduleResumeScoringJob_Failure() throws Exception {
    String jobPostingKey = "jobPostingKey";

    TriggerKey scoringTriggerKey = TriggerKey.triggerKey("resumeScoringTrigger-" + jobPostingKey,
        "group1");
    TriggerKey filteringTriggerKey = TriggerKey.triggerKey("filteringTrigger-" + jobPostingKey,
        "group1");

    doThrow(new SchedulerException("unscheduleJob failed")).when(scheduler)
        .unscheduleJob(scoringTriggerKey);

    CustomException exception = assertThrows(CustomException.class,
        () -> filteringService.unscheduleResumeScoringJob(jobPostingKey));
    assertEquals("스케줄링 취소에 실패하였습니다.", exception.getMessage());

    verify(scheduler).unscheduleJob(scoringTriggerKey);
    verify(scheduler, never()).unscheduleJob(filteringTriggerKey);
  }

  @Test
  @DisplayName("점수로 지원자 필터링 : 성공")
  public void testFilterCandidates_Success() {
    String jobPostingKey = "jobPostingKey";
    JobPostingEntity jobPosting = JobPostingEntity.builder()
        .jobPostingKey(jobPostingKey)
        .companyKey("companyKey")
        .title("제목")
        .jobCategory(BACKEND)
        .education(BACHELOR)
        .startDate(LocalDate.parse("2025-04-02"))
        .endDate(LocalDate.parse(("2025-04-05")))
        .passingNumber(2)
        .career(3)
        .salary(30000000L)
        .workTime("무관")
        .workLocation("주소")
        .employmentType("인턴")
        .jobPostingContent("공고 내용")
        .build();

    ApplicantEntity applicant1 = ApplicantEntity.builder()
        .id(1L)
        .candidateKey("candidateKey1")
        .jobPostingKey(jobPostingKey)
        .score(90)
        .build();

    ApplicantEntity applicant2 = ApplicantEntity.builder()
        .id(2L)
        .candidateKey("candidateKey2")
        .jobPostingKey(jobPostingKey)
        .score(80)
        .build();

    ApplicantEntity applicant3 = ApplicantEntity.builder()
        .id(3L)
        .candidateKey("candidateKey3")
        .jobPostingKey(jobPostingKey)
        .score(85)
        .build();

    JobPostingStepEntity jobPostingStepEntity = JobPostingStepEntity.builder()
        .id(1L)
        .jobPostingKey(jobPostingKey)
        .step("서류 단계")
        .build();

    CandidateEntity candidate1 = CandidateEntity.builder()
        .candidateKey("candidateKey1")
        .email("test1@example.com")
        .name("candidate1")
        .phoneNumber("010-0000-0000")
        .password("Password123!")
        .role(ROLE_CANDIDATE)
        .build();

    CandidateEntity candidate2 = CandidateEntity.builder()
        .candidateKey("candidateKey2")
        .email("test2@example.com")
        .name("candidate2")
        .phoneNumber("010-1111-0000")
        .password("Password000!")
        .role(ROLE_CANDIDATE)
        .build();

    CandidateEntity candidate3 = CandidateEntity.builder()
        .candidateKey("candidateKey3")
        .email("test3@example.com")
        .name("candidate3")
        .phoneNumber("010-2222-0000")
        .password("Password333!")
        .role(ROLE_CANDIDATE)
        .build();

    AppliedJobPostingEntity appliedJobPosting1 = mock(AppliedJobPostingEntity.class);
    AppliedJobPostingEntity appliedJobPosting3 = mock(AppliedJobPostingEntity.class);

    when(jobPostingRepository.findByJobPostingKey(jobPostingKey)).thenReturn(
        Optional.of(jobPosting));
    when(applicantRepository.findAllByJobPostingKey(jobPostingKey)).thenReturn(
        Arrays.asList(applicant1, applicant2, applicant3));
    when(jobPostingStepRepository.findFirstByJobPostingKeyOrderByIdAsc(jobPostingKey)).thenReturn(
        Optional.of(jobPostingStepEntity));
    when(candidateRepository.findByCandidateKey("candidateKey1")).thenReturn(
        Optional.of(candidate1));
    when(candidateRepository.findByCandidateKey("candidateKey3")).thenReturn(
        Optional.of(candidate3));
    when(appliedJobPostingRepository.findByCandidateKeyAndJobPostingKey("candidateKey1",
        jobPostingKey)).thenReturn(Optional.of(appliedJobPosting1));
    when(appliedJobPostingRepository.findByCandidateKeyAndJobPostingKey("candidateKey3",
        jobPostingKey)).thenReturn(Optional.of(appliedJobPosting3));

    filteringService.filterCandidates(jobPostingKey);

    verify(candidateListRepository, times(2)).save(any(CandidateListEntity.class));
    verify(appliedJobPosting1).updateStepName("서류 단계");
    verify(appliedJobPosting3).updateStepName("서류 단계");
  }

  @Test
  @DisplayName("점수로 지원자 필터링 : 실패 - JOB_POSTING_NOT_FOUND")
  public void testFilterCandidates_JobPostingNotFound() {
    String jobPostingKey = "jobPostingKey";

    when(jobPostingRepository.findByJobPostingKey(jobPostingKey)).thenReturn(Optional.empty());

    CustomException exception = assertThrows(CustomException.class,
        () -> filteringService.filterCandidates(jobPostingKey));
    assertEquals("채용 공고를 찾을 수 없습니다.", exception.getMessage());
  }

  @Test
  @DisplayName("점수로 지원자 필터링 : 실패 - JOB_POSTING_STEP_NOT_FOUND")
  public void testFilterCandidates_JobPostingStepNotFound() {
    String jobPostingKey = "jobPostingKey";
    JobPostingEntity jobPosting = new JobPostingEntity();

    when(jobPostingRepository.findByJobPostingKey(jobPostingKey)).thenReturn(
        Optional.of(jobPosting));
    when(jobPostingStepRepository.findFirstByJobPostingKeyOrderByIdAsc(jobPostingKey)).thenReturn(
        Optional.empty());

    CustomException exception = assertThrows(CustomException.class,
        () -> filteringService.filterCandidates(jobPostingKey));
    assertEquals("채용 공고의 해당 단계를 찾을 수 없습니다.", exception.getMessage());
  }

  @Test
  @DisplayName("점수로 지원자 필터링 : 실패 - CANDIDATE_NOT_FOUND")
  public void testFilterCandidates_CandidateNotFound() {
    String jobPostingKey = "jobPostingKey";
    JobPostingEntity jobPosting = JobPostingEntity.builder()
        .jobPostingKey(jobPostingKey)
        .companyKey("companyKey")
        .title("제목")
        .jobCategory(BACKEND)
        .education(BACHELOR)
        .startDate(LocalDate.parse("2025-04-02"))
        .endDate(LocalDate.parse(("2025-04-05")))
        .passingNumber(2)
        .career(3)
        .salary(30000000L)
        .workTime("무관")
        .workLocation("주소")
        .employmentType("인턴")
        .jobPostingContent("공고 내용")
        .build();

    ApplicantEntity applicant = ApplicantEntity.builder()
        .id(1L)
        .candidateKey("candidateKey1")
        .jobPostingKey(jobPostingKey)
        .score(90)
        .build();

    JobPostingStepEntity jobPostingStepEntity = JobPostingStepEntity.builder()
        .id(1L)
        .jobPostingKey(jobPostingKey)
        .step("서류 단계")
        .build();

    when(jobPostingRepository.findByJobPostingKey(jobPostingKey)).thenReturn(
        Optional.of(jobPosting));
    when(applicantRepository.findAllByJobPostingKey(jobPostingKey)).thenReturn(List.of(applicant));
    when(jobPostingStepRepository.findFirstByJobPostingKeyOrderByIdAsc(jobPostingKey)).thenReturn(
        Optional.of(jobPostingStepEntity));
    when(candidateRepository.findByCandidateKey("candidateKey1")).thenReturn(Optional.empty());

    CustomException exception = assertThrows(CustomException.class,
        () -> filteringService.filterCandidates(jobPostingKey));
    assertEquals("가입된 지원자를 찾을 수 없습니다.", exception.getMessage());
  }

  @Test
  @DisplayName("점수로 지원자 필터링 : 실패 - APPLY_NOT_FOUND")
  public void testFilterCandidates_ApplyNotFound() {
    String jobPostingKey = "jobPostingKey";
    String candidateKey = "candidateKey";

    JobPostingEntity jobPosting = JobPostingEntity.builder()
        .jobPostingKey(jobPostingKey)
        .companyKey("companyKey")
        .title("제목")
        .jobCategory(BACKEND)
        .education(BACHELOR)
        .startDate(LocalDate.parse("2025-04-02"))
        .endDate(LocalDate.parse("2025-04-05"))
        .passingNumber(2)
        .career(3)
        .salary(30000000L)
        .workTime("무관")
        .workLocation("주소")
        .employmentType("인턴")
        .jobPostingContent("공고 내용")
        .build();

    JobPostingStepEntity jobPostingStepEntity = JobPostingStepEntity.builder()
        .id(1L)
        .jobPostingKey(jobPostingKey)
        .step("서류 단계")
        .build();

    ApplicantEntity applicant = ApplicantEntity.builder()
        .candidateKey(candidateKey)
        .jobPostingKey(jobPostingKey)
        .score(100)
        .build();

    when(jobPostingRepository.findByJobPostingKey(jobPostingKey)).thenReturn(
        Optional.of(jobPosting));
    when(applicantRepository.findAllByJobPostingKey(jobPostingKey)).thenReturn(
        Collections.singletonList(applicant));
    when(jobPostingStepRepository.findFirstByJobPostingKeyOrderByIdAsc(jobPostingKey)).thenReturn(
        Optional.of(jobPostingStepEntity));
    when(candidateRepository.findByCandidateKey(candidateKey)).thenReturn(
        Optional.empty());

    CustomException thrown = assertThrows(CustomException.class, () -> {
      filteringService.filterCandidates(jobPostingKey);
    });

    assertEquals("가입된 지원자를 찾을 수 없습니다.", thrown.getMessage());
  }
}