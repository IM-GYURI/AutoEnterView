package com.ctrls.auto_enter_view.component;

import com.ctrls.auto_enter_view.enums.TechStack;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import java.io.IOException;

public class TechStackSerializer extends JsonSerializer<TechStack> {

  @Override
  public void serialize(TechStack value, JsonGenerator gen, SerializerProvider serializers)
      throws IOException {

    gen.writeString(value.getValue());
  }
}