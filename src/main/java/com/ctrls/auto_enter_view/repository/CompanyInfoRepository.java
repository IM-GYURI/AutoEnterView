package com.ctrls.auto_enter_view.repository;

import com.ctrls.auto_enter_view.entity.CompanyInfoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CompanyInfoRepository extends JpaRepository<CompanyInfoEntity, String> {

}