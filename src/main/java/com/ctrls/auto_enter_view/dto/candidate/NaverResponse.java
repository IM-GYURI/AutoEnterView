package com.ctrls.auto_enter_view.dto.candidate;

import java.util.Map;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class NaverResponse implements OAuth2Response {

  private final Map<String, Object> attribute;

  @Override
  public String getProvider() {
    return "naver";
  }

  @Override
  public String getProviderId() {
    Map<String, Object> response = (Map<String, Object>) attribute.get("response");
    Object providerId = response != null ? response.get("id") : null;
    return providerId != null ? providerId.toString() : null;
  }

  @Override
  public String getEmail() {
    Map<String, Object> response = (Map<String, Object>) attribute.get("response");
    Object email = response != null ? response.get("email") : null;
    return email != null ? email.toString() : null;
  }

  @Override
  public String getName() {
    Map<String, Object> response = (Map<String, Object>) attribute.get("response");
    Object name = response != null ? response.get("name") : null;
    return name != null ? name.toString() : null;
  }

  @Override
  public String getNumber() {
    Map<String, Object> response = (Map<String, Object>) attribute.get("response");
    Object number = response != null ? response.get("mobile") : null;
    return number != null ? number.toString() : null;
  }
}
