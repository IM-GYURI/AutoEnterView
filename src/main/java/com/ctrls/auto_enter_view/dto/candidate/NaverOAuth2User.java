package com.ctrls.auto_enter_view.dto.candidate;

import com.ctrls.auto_enter_view.enums.UserRole;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import lombok.AllArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

@AllArgsConstructor
public class NaverOAuth2User implements OAuth2User {

  private final Map<String, Object> attributes;

  @Override
  public <A> A getAttribute(String name) {
    return (A) attributes.get(name);
  }

  @Override
  public Map<String, Object> getAttributes() {
    return attributes;
  }

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return Collections.singletonList(new SimpleGrantedAuthority(UserRole.ROLE_CANDIDATE.name()));
  }

  @Override
  public String getName() {
    return (String) attributes.get("key");
  }
}
