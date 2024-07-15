package com.ctrls.auto_enter_view.component;

import com.ctrls.auto_enter_view.enums.TechStack;
import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.module.SimpleModule;
import java.io.IOException;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
public class TechStackDeserializer extends JsonDeserializer<TechStack> {

  @Override
  public TechStack deserialize(JsonParser p, DeserializationContext ctxt)
      throws IOException, JacksonException {

    String value = p.getText();

    System.out.println("value = " + value);

    return TechStack.fromString(value);
  }

  @Bean
  public Module techStackModule() {

    SimpleModule module = new SimpleModule();
    module.addDeserializer(TechStack.class, new TechStackDeserializer());

    return module;
  }
}