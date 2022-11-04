package com.example.ola.config;

import com.example.ola.jwt.JwtAccessDeniedHandler;
import com.example.ola.jwt.JwtAuthenticationEntryPoint;
import com.example.ola.jwt.JwtSecurityConfig;
import com.example.ola.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@RequiredArgsConstructor
@EnableGlobalMethodSecurity(prePostEnabled = true)
@EnableWebSecurity
public class SecurityConfig {
    private final JwtTokenProvider jwtTokenProvider;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final JwtAccessDeniedHandler jwtAccessDeniedHandler;

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                .exceptionHandling()
                .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                .accessDeniedHandler(jwtAccessDeniedHandler)
                .and()
                .formLogin().disable()
                .httpBasic().disable()
                .csrf().disable()
                .headers().frameOptions().sameOrigin()
                .and()
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)

                .and()
                .authorizeRequests(
                        auth -> auth
                                .requestMatchers(PathRequest.toStaticResources().atCommonLocations()).permitAll()
//                                .mvcMatchers(
//                                        HttpMethod.GET,
//                                        "/api/posts/post/**",
//                                        "/api/posts/list",
//                                        "/ws-stomp/**",
//                                        "/ws-stomp",
//                                        "/ws-stomp/chat").permitAll()
//                                .mvcMatchers(
//                                        HttpMethod.POST,
//                                        "/api/users/join",
//                                        "/api/users/login",
//                                        "/ws-stomp/**",
//                                        "/ws-stomp",
//                                        "/ws-stomp/chat"
//                                ).permitAll()
                                .anyRequest().permitAll()
                )
                .apply(new JwtSecurityConfig(jwtTokenProvider))
                .and()
                .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }
}
