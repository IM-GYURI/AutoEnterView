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

  /**
   * 회사 정보 생성하기
   *
   * @param userDetails 로그인 된 사용자 정보
   * @param companyKey 회사 PK
   * @param request CreateCompanyInfoDto.Request
   * @throws CustomException ALREADY_EXISTS : 회사 정보가 이미 존재하는 경우
   */
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

  /**
   * 회사 정보 조회하기
   * @param companyKey 회사 PK
   * @return ReadCompanyInfoDto.Response
   */
  @Transactional(readOnly = true)
  public Response readInfo(String companyKey) {
    CompanyInfoEntity companyInfoEntity = companyInfoRepository.findByCompanyKey(companyKey)
        .orElseGet(CompanyInfoEntity::new);

    return Response.toDto(companyInfoEntity);
  }

  /**
   * 회사 정보 수정하기
   * @param userDetails 로그인 된 사용자 정보
   * @param companyKey 회사 PK
   * @param request CreateCompanyInfoDto.Request
   * @throws CustomException NOT_FOUND : 회사 정보를 찾을 수 없는 경우
   */
  @Transactional
  public void updateInfo(UserDetails userDetails, String companyKey, Request request) {
    authCheck(userDetails, companyKey);

    CompanyInfoEntity companyInfoEntity = companyInfoRepository.findByCompanyKey(companyKey)
        .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND));

    companyInfoEntity.updateEntity(request);

    companyInfoRepository.save(companyInfoEntity);
  }

  /**
   * 회사 정보 삭제하기
   * @param userDetails 로그인 된 사용자 정보
   * @param companyKey 회사 PK
   */
  @Transactional
  public void deleteInfo(UserDetails userDetails, String companyKey) {
    authCheck(userDetails, companyKey);

    companyInfoRepository.deleteByCompanyKey(companyKey);
  }

  /**
   * 권한 체크
   *
   * @param userDetails 로그인 된 사용자 정보
   * @param companyKey 회사 PK
   * @return 회사명
   * @throws CustomException EMAIL_NOT_FOUND : 이메일로 가입된 회사를 찾을 수 없는 경우
   * @throws CustomException NO_AUTHORITY : 로그인한 사용자의 회사키, 매개변수의 회사키가 일치하지 않는 경우
   */
  private String authCheck(UserDetails userDetails, String companyKey) {
    CompanyEntity companyEntity = companyRepository.findByEmail(userDetails.getUsername())
        .orElseThrow(() -> new CustomException(EMAIL_NOT_FOUND));

    if (!companyEntity.getCompanyKey().equals(companyKey)) {
      throw new CustomException(NO_AUTHORITY);
    }

    return companyEntity.getCompanyName();
  }
}