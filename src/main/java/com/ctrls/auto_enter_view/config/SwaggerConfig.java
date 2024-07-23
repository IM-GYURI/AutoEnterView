package com.ctrls.auto_enter_view.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

  @Bean
  public OpenAPI openAPI() {

    return new OpenAPI()
        .info(new Info()
            .title("AutoEnterView")
            .description("채용 프로세스 자동화 프로젝트")
            .version("1.0")
        );
  }
}