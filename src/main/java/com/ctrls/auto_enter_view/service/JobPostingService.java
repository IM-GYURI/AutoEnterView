package com.ctrls.auto_enter_view.service;

import com.ctrls.auto_enter_view.dto.common.MainJobPostingDto;
import com.ctrls.auto_enter_view.dto.common.MainJobPostingDto.JobPostingMainInfo;
import com.ctrls.auto_enter_view.dto.common.MainJobPostingDto.Response;
import com.ctrls.auto_enter_view.dto.jobposting.JobPostingDto.Request;
import com.ctrls.auto_enter_view.entity.CompanyEntity;
import com.ctrls.auto_enter_view.entity.JobPostingEntity;
import com.ctrls.auto_enter_view.enums.ErrorCode;
import com.ctrls.auto_enter_view.exception.CustomException;
import com.ctrls.auto_enter_view.repository.CompanyRepository;
import com.ctrls.auto_enter_view.repository.JobPostingRepository;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class JobPostingService {

  private final JobPostingRepository jobPostingRepository;
  private final CompanyRepository companyRepository;
  private final JobPostingTechStackService jobPostingTechStackService;

  public JobPostingEntity createJobPosting(String companyKey, Request request) {

    JobPostingEntity entity = Request.toEntity(companyKey, request);
    return jobPostingRepository.save(entity);
  }


  public List<Response> getAllJobPosting() {
    List<JobPostingEntity> jobPostingEntities = jobPostingRepository.findAll();
    List<Response> responseList = new ArrayList<>();

    for (JobPostingEntity entity : jobPostingEntities) {
      String companyKey = entity.getCompanyKey();
      CompanyEntity companyEntity = companyRepository.findByCompanyKey(companyKey)
          .orElseThrow(() -> new CustomException(ErrorCode.COMPANY_NOT_FOUND));

      String companyName = companyEntity.getCompanyName();
      log.info("회사명 조회 완료 : {}", companyName);

      String jobPostingKey = entity.getJobPostingKey();
      List<String> techStack = jobPostingTechStackService.getTechStackByJobPostingKey(jobPostingKey);

      JobPostingMainInfo jobPostingMainInfo = JobPostingMainInfo.from(entity, companyName, techStack);

      MainJobPostingDto.Response response = MainJobPostingDto.Response.builder()
          .jobPostingsList(List.of(jobPostingMainInfo))
          .build();

      responseList.add(response);
    }

    log.info("총 {}개의 채용 공고 조회 완료", responseList.size());
    return responseList;
  }
}