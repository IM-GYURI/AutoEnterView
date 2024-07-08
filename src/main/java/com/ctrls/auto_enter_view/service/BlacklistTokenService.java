package com.ctrls.auto_enter_view.service;

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

  // 블랙 리스트에 토큰 추가
  public void addToBlacklist(String token) {

    if (token.startsWith("Bearer ")) {
      token = token.substring(7);
    }
    log.info("add blacklist token : {}", token);
    redisTemplate.opsForValue().set(token, "logout", 60, TimeUnit.MINUTES);
  }

  // 블랙 리스트에 토큰이 존재하는지 조회
  public boolean isTokenBlacklist(String token) {

    boolean isBlacklist = redisTemplate.opsForValue().get(token) != null;
    log.info(String.valueOf(isBlacklist));

    return isBlacklist;
  }
}