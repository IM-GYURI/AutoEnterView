package com.ctrls.auto_enter_view.config;

import com.ctrls.auto_enter_view.service.CompanyInfoService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
public class CompanyInfoController {

  private final CompanyInfoService companyInfoService;
}
