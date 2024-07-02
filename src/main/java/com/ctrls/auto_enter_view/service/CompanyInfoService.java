package com.ctrls.auto_enter_view.service;

import com.ctrls.auto_enter_view.repository.CompanyInfoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service

public class CompanyInfoService {

  private final CompanyInfoRepository companyInfoRepository;
}
