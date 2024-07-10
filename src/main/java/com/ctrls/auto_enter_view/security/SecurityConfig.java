package com.ctrls.auto_enter_view.security;

import com.ctrls.auto_enter_view.enums.UserRole;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

  private final JwtAuthenticationFilter jwtAuthenticationFilter;

  @Bean
  public PasswordEncoder passwordEncoder() {

    return new BCryptPasswordEncoder();
  }

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

    http
        // JWT 인증을 사용하는 경우 - csrf 보호 비활성화
        .csrf(AbstractHttpConfigurer::disable)

        // cors 설정 활성화
        .cors(cors -> cors.configurationSource(corsConfigurationSource()))

        // httpBasic 비활성화
        .httpBasic(AbstractHttpConfigurer::disable)

        // session 설정
        .sessionManagement(sessionManagement ->
            sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        )
        .authorizeHttpRequests(authHttpRequest -> authHttpRequest

            .requestMatchers("/companies/signup", "/candidates/signup").permitAll()
            .requestMatchers("/candidates/**").hasRole(UserRole.ROLE_CANDIDATE.name().substring(5))
            .requestMatchers("/common/signout").authenticated()
            .requestMatchers("/common/**").permitAll()
            .requestMatchers("/error").permitAll()

            // company 설정
            .requestMatchers(HttpMethod.GET, "/companies/*/information").permitAll()
            .requestMatchers("/companies/**").hasRole(UserRole.ROLE_COMPANY.name().substring(5))

            // else
            .requestMatchers("/swagger-ui/**", "/swagger-resources/**", "/v3/api-docs/**")
            .permitAll()
            .anyRequest().authenticated())

        // JWT 필터 추가
        .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

    return http.build();
  }

  @Bean
  public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();

    // 모든 출처 허용
    configuration.setAllowedOrigins(List.of("*"));

    // 허용할 메서드 설정
    configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE"));

    // 모든 헤더 허용
    configuration.setAllowedHeaders(List.of("*"));

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);

    return source;
  }
}