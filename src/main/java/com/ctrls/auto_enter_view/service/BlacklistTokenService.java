package com.ctrls.auto_enter_view.service;

import com.ctrls.auto_enter_view.enums.ErrorCode;
import com.ctrls.auto_enter_view.exception.CustomException;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class BlacklistTokenService {

  private final RedisTemplate<String, String> redisTemplate;

  /**
   * 블랙 리스트에 토큰 추가하기
   *
   * @param token 토큰 정보
   * @throws CustomException BLACKLIST_TOKEN_ADD_FAILED : 블랙 리스트에 토큰 추가 실패한 경우
   */
  public void addToBlacklist(String token) {

    if (token.startsWith("Bearer ")) {
      token = token.substring(7);
    }
    log.info("add blacklist token : {}", token);
    try {
      redisTemplate.opsForValue().set(token, "logout", 60, TimeUnit.MINUTES);
    } catch (Exception e) {
      throw new CustomException(ErrorCode.BLACKLIST_TOKEN_ADD_FAILED);
    }
  }

  /**
   * 블랙 리스트에 토큰이 존재하는지 조회하기
   *
   * @param token 토큰 정보
   * @return boolean : 토큰이 블랙 리스트에 존재하면 true, 존재하지 않으면 false
   */
  public boolean isTokenBlacklist(String token) {

    boolean isBlacklist = redisTemplate.opsForValue().get(token) != null;
    log.info(String.valueOf(isBlacklist));

    return isBlacklist;
  }
}