package com.ctrls.auto_enter_view.service;

import static com.ctrls.auto_enter_view.enums.ErrorCode.EMAIL_NOT_FOUND;
import static com.ctrls.auto_enter_view.enums.ErrorCode.NO_AUTHORITY;

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
  public void createInfo(UserDetails userDetails, String companyKey, Request request) {
    String companyName = authCheck(userDetails, companyKey);

    if (companyInfoRepository.existsByCompanyKey(companyKey)) {
      throw new CustomException(ErrorCode.ALREADY_EXISTS);
    }

    String key = KeyGenerator.generateKey();

    CompanyInfoEntity companyInfoEntity = request.toEntity(key, companyKey, companyName);

    companyInfoRepository.save(companyInfoEntity);
  }

  @Transactional(readOnly = true)
  public Response readInfo(String companyKey) {
    CompanyInfoEntity companyInfoEntity = companyInfoRepository.findByCompanyKey(companyKey)
        .orElseGet(CompanyInfoEntity::new);

    return Response.toDto(companyInfoEntity);
  }

  @Transactional
  public void updateInfo(UserDetails userDetails, String companyKey, Request request) {
    authCheck(userDetails, companyKey);

    CompanyInfoEntity companyInfoEntity = companyInfoRepository.findByCompanyKey(companyKey)
        .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND));

    companyInfoEntity.updateEntity(request);

    companyInfoRepository.save(companyInfoEntity);
  }

  @Transactional
  public void deleteInfo(UserDetails userDetails, String companyKey) {
    authCheck(userDetails, companyKey);

    companyInfoRepository.deleteByCompanyKey(companyKey);
  }

  private String authCheck(UserDetails userDetails, String companyKey) {
    CompanyEntity companyEntity = companyRepository.findByEmail(userDetails.getUsername())
        .orElseThrow(() -> new CustomException(EMAIL_NOT_FOUND));

    if (!companyEntity.getCompanyKey().equals(companyKey)) {
      throw new CustomException(NO_AUTHORITY);
    }

    return companyEntity.getCompanyName();
  }
}