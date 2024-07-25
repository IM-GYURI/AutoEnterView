package com.ctrls.auto_enter_view.service;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

@ExtendWith(MockitoExtension.class)
class BlacklistTokenServiceTest {

  @Mock
  private RedisTemplate<String, String> redisTemplate;

  @Mock
  private ValueOperations<String, String> valueOperations;

  @InjectMocks
  private BlacklistTokenService blacklistTokenService;

  @BeforeEach
  void setUp() {
    when(redisTemplate.opsForValue()).thenReturn(valueOperations);
  }

  @Test
  @DisplayName("블랙리스트에 토큰 추가 테스트")
  void addToBlacklist() {
    // given
    String token = "Bearer testToken";
    String substringToken = "testToken";
    doNothing().when(valueOperations).set(substringToken, "logout", 60, TimeUnit.MINUTES);

    // when
    blacklistTokenService.addToBlacklist(token);

    // then
    verify(valueOperations, times(1)).set(substringToken, "logout", 60, TimeUnit.MINUTES);
  }

  @Test
  @DisplayName("블랙리스트에 토큰 존재여부 확인 테스트")
  void isTokenBlacklist() {
    // given
    String token = "testToken";
    when(valueOperations.get(token)).thenReturn("logout");

    // when
    boolean isBlacklist = blacklistTokenService.isTokenBlacklist(token);

    // then
    assertTrue(isBlacklist);
    verify(valueOperations, times(1)).get(token);
  }
}