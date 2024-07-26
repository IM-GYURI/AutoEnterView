package com.ctrls.auto_enter_view.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ctrls.auto_enter_view.enums.ErrorCode;
import com.ctrls.auto_enter_view.exception.CustomException;
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
  @DisplayName("블랙리스트에 토큰 추가하기 - 성공")
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
  @DisplayName("블랙리스트에 토큰 추가하기 - 실패")
  void addToBlacklistException() {
    // given
    String token = "Bearer testToken";
    String substringToken = "testToken";
    doThrow(new RuntimeException()).when(valueOperations).set(substringToken, "logout", 60, TimeUnit.MINUTES);

    // when
    CustomException exception = assertThrows(CustomException.class, () -> blacklistTokenService.addToBlacklist(token));

    // then
    assertNotNull(exception);
    assertEquals(ErrorCode.BLACKLIST_TOKEN_ADD_FAILED, exception.getErrorCode());
    verify(valueOperations, times(1)).set(substringToken, "logout", 60, TimeUnit.MINUTES);
  }

  @Test
  @DisplayName("블랙리스트에 토큰 존재여부 확인")
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