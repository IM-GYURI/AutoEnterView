package com.ctrls.auto_enter_view.service;

import com.ctrls.auto_enter_view.dto.company.CreateCompanyInfoDto.Request;
import com.ctrls.auto_enter_view.dto.company.ReadCompanyInfoDto.Response;
import com.ctrls.auto_enter_view.entity.CompanyEntity;
import com.ctrls.auto_enter_view.entity.CompanyInfoEntity;
import com.ctrls.auto_enter_view.enums.ErrorCode;
import com.ctrls.auto_enter_view.exception.CustomException;
import com.ctrls.auto_enter_view.repository.CompanyInfoRepository;
import com.ctrls.auto_enter_view.repository.CompanyRepository;
import com.ctrls.auto_enter_view.util.KeyGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Service
public class CompanyInfoService {

  private final CompanyInfoRepository companyInfoRepository;
  private final CompanyRepository companyRepository;

  @Transactional
  public void createInfo(String companyKey, Request request) {

    authCheck(companyKey);

    String key = KeyGenerator.generateKey();

    CompanyInfoEntity companyInfoEntity = request.toEntity(key, companyKey);

    try {
      companyInfoRepository.save(companyInfoEntity);
    } catch (DataIntegrityViolationException e) {
      throw new CustomException(ErrorCode.INTER_SERVER_ERROR);
    }
  }

  @Transactional(readOnly = true)
  public Response readInfo(String companyKey) {

    CompanyInfoEntity companyInfoEntity = companyInfoRepository.findByCompanyKey(companyKey)
        .orElseGet(CompanyInfoEntity::new);

    return Response.toDto(companyInfoEntity);
  }

  @Transactional
  public void updateInfo(String companyKey, Request request) {

    authCheck(companyKey);

    CompanyInfoEntity companyInfoEntity = companyInfoRepository.findByCompanyKey(companyKey)
        .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND));

    companyInfoEntity.updateEntity(request);

    companyInfoRepository.save(companyInfoEntity);
  }

  @Transactional
  public void deleteInfo(String companyKey) {

    authCheck(companyKey);

    companyInfoRepository.deleteByCompanyKey(companyKey);
  }

  private void authCheck(String companyKey) {

    UserDetails principal = (UserDetails) SecurityContextHolder.getContext().getAuthentication()
        .getPrincipal();

    CompanyEntity companyEntity = companyRepository.findByEmail(principal.getUsername())
        .orElseThrow(() -> new CustomException(ErrorCode.EMAIL_NOT_FOUND));

    if (!companyEntity.getCompanyKey().equals(companyKey)) {
      throw new CustomException(ErrorCode.NO_AUTHORITY);
    }
  }
}