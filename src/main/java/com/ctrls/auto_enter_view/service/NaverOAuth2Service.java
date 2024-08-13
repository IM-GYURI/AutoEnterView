package com.ctrls.auto_enter_view.service;

import com.ctrls.auto_enter_view.dto.candidate.NaverOAuth2User;
import java.util.Map;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NaverOAuth2Service extends DefaultOAuth2UserService {

  @Override
  @Transactional
  public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
    OAuth2User oAuth2User = super.loadUser(userRequest);
    String registrationId = userRequest.getClientRegistration().getRegistrationId();
    Map<String, Object> attributes;

    if (registrationId.equals("naver")) {
      attributes = oAuth2User.getAttributes();

      Map<String, Object> response = (Map<String, Object>) attributes.get("response");
      if (response == null) {
        throw new OAuth2AuthenticationException(
            new OAuth2Error(OAuth2ErrorCodes.INVALID_REQUEST, "No 'response' field in attributes",
                null));
      }

      String key = "naver-" + response.get("id");

      Object name = response.get("name");
      Object email = response.get("email");
      Object mobile = response.get("mobile");

      Map<String, Object> immutableMap = Map.of(
          "key", key,
          "name", name != null ? name.toString() : null,
          "email", email != null ? email.toString() : null,
          "mobile", mobile != null ? mobile.toString() : null
      );

      return new NaverOAuth2User(immutableMap);
    } else {
      throw new OAuth2AuthenticationException(new OAuth2Error(OAuth2ErrorCodes.INVALID_REQUEST));
    }
  }
}
