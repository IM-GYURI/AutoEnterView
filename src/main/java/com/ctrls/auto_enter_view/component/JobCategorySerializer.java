package com.ctrls.auto_enter_view.component;

import com.ctrls.auto_enter_view.enums.JobCategory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import java.io.IOException;

public class JobCategorySerializer extends JsonSerializer<JobCategory> {

  @Override
  public void serialize(JobCategory value, JsonGenerator gen, SerializerProvider serializers)
      throws IOException {

    gen.writeString(value.getValue());
  }
}