package com.ctrls.auto_enter_view.repository;

import com.ctrls.auto_enter_view.entity.CompanyInfoEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface CompanyInfoRepository extends JpaRepository<CompanyInfoEntity, String> {

  Optional<CompanyInfoEntity> findByCompanyKey(String companyKey);

  boolean existsByCompanyKey(String companyKey);

  @Modifying
  @Query("DELETE FROM CompanyInfoEntity ci WHERE ci.companyKey =:companyKey")
  void deleteByCompanyKey(String companyKey);
}