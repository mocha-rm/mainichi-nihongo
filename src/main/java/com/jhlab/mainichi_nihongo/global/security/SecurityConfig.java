package com.jhlab.mainichi_nihongo.global.security;

import com.jhlab.mainichi_nihongo.global.security.jwt.JwtAuthFilter;
import com.jhlab.mainichi_nihongo.global.security.oauth2.CustomOAuth2UserService;
import com.jhlab.mainichi_nihongo.global.security.oauth2.OAuth2FailureHandler;
import com.jhlab.mainichi_nihongo.global.security.oauth2.OAuth2SuccessHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
        private final CorsProperties corsProperties;
        private final JwtAuthFilter jwtAuthFilter;
        private final CustomOAuth2UserService customOAuth2UserService;
        private final OAuth2SuccessHandler oAuth2SuccessHandler;
        private final OAuth2FailureHandler oAuth2FailureHandler;

        public static final String[] WHITE_LIST = {
                        "/",
                        "/api/subscribe",
                        "/api/unsubscribe",
                        "/api/subscribers",
                        "/api/contents/**",
                        "/api/admin/**",
                        "/api/auth/**",
                        "/oauth2/**",
                        "/login/oauth2/**",
                        "/login" // 기본 로그인 페이지도 허용
        };

        public static final String[] PREMIUM_ENDPOINTS = {
                        "/api/scrap/**",
                        "/api/vocabulary/**",
                        "/api/dialects/**",
                        "/api/culture/**"
        };

        @Bean
        public SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {
                httpSecurity.csrf(AbstractHttpConfigurer::disable);

                httpSecurity.cors(cors -> cors.configurationSource(request -> {
                        var config = new org.springframework.web.cors.CorsConfiguration();
                        config.setAllowedOrigins(corsProperties.getAllowedOrigins());
                        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
                        config.setAllowedHeaders(List.of("*"));
                        config.setAllowCredentials(true);
                        return config;
                }));

                httpSecurity.sessionManagement(
                                session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

                httpSecurity.formLogin(AbstractHttpConfigurer::disable);
                httpSecurity.httpBasic(AbstractHttpConfigurer::disable);

                httpSecurity.oauth2Login(oauth2 -> oauth2
                                .authorizationEndpoint(authorization -> authorization
                                                .baseUri("/oauth2/authorization"))
                                .redirectionEndpoint(redirection -> redirection
                                                .baseUri("/login/oauth2/code/*"))
                                .userInfoEndpoint(userInfo -> userInfo
                                                .userService(customOAuth2UserService))
                                .successHandler(oAuth2SuccessHandler)
                                .failureHandler(oAuth2FailureHandler));

                httpSecurity.addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

                httpSecurity.authorizeHttpRequests(authorize -> authorize
                                .requestMatchers(WHITE_LIST).permitAll()
                                .requestMatchers(PREMIUM_ENDPOINTS).hasRole("PREMIUM")
                                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                                .anyRequest().authenticated());

                return httpSecurity.build();
        }
}
