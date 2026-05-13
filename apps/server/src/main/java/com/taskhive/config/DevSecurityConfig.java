package com.taskhive.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@Profile("dev")
public class DevSecurityConfig {

    @Bean
    @Order(1)
    public SecurityFilterChain devFilterChain(HttpSecurity http) throws Exception {
        return http
                .securityMatcher("/api/dev/**")
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                .build();
    }
}
