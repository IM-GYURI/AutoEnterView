package com.ctrls.auto_enter_view.repository;

import com.ctrls.auto_enter_view.entity.CompanyEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CompanyRepository extends JpaRepository<CompanyEntity, Long> {

  Optional<CompanyEntity> findByCompanyKey(String companyKey);
}
