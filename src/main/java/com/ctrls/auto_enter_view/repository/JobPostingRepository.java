package com.ctrls.auto_enter_view.repository;

import com.ctrls.auto_enter_view.entity.JobPostingEntity;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface JobPostingRepository extends JpaRepository<JobPostingEntity, String> {

  Optional<JobPostingEntity> findByJobPostingKey(String jobPostingKey);

  List<JobPostingEntity> findAllByCompanyKey(String companyKey);

  void deleteByJobPostingKey(String jobPostingKey);

  boolean existsByJobPostingKeyAndEndDateGreaterThanEqual(String jobPostingKey,
      LocalDate currentDate);

  @Query("SELECT j FROM JobPostingEntity j "
      + "LEFT JOIN CompanyEntity c "
      + "ON j.companyKey = c.companyKey "
      + "WHERE c.companyKey IS NOT NULL "
      + "AND j.endDate >= :currentDate")
  Page<JobPostingEntity> findByEndDateGreaterThanEqual(LocalDate currentDate, Pageable pageable);

}
