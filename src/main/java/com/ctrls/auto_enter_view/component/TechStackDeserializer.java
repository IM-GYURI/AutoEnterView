package com.ctrls.auto_enter_view.component;

import com.ctrls.auto_enter_view.enums.TechStack;
import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import java.io.IOException;

public class TechStackDeserializer extends JsonDeserializer<TechStack> {

  @Override
  public TechStack deserialize(JsonParser jsonParser, DeserializationContext deserializationContext)
      throws IOException, JacksonException {

    String value = jsonParser.getText().toUpperCase();

    return TechStack.parse(value);
  }
}