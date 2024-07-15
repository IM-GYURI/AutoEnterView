package com.ctrls.auto_enter_view.config;

import com.ctrls.auto_enter_view.component.TechStackDeserializer;
import com.ctrls.auto_enter_view.enums.TechStack;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JsonConfig {

  @Bean
  public Module module() {

    SimpleModule simpleModule = new SimpleModule();
    simpleModule.addDeserializer(TechStack.class, new TechStackDeserializer());

    return simpleModule;
  }
}