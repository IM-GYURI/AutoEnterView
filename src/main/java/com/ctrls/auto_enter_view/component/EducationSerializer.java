package com.ctrls.auto_enter_view.component;

import com.ctrls.auto_enter_view.enums.Education;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import java.io.IOException;

public class EducationSerializer extends JsonSerializer<Education> {

  @Override
  public void serialize(Education value, JsonGenerator gen, SerializerProvider serializers)
      throws IOException {

    gen.writeString(value.getValue());
  }
}