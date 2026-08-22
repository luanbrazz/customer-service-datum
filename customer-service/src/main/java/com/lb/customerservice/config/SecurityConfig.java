package com.lb.customerservice.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableConfigurationProperties(SecurityUsersProperties.class)
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public InMemoryUserDetailsManager userDetailsService(SecurityUsersProperties properties, PasswordEncoder encoder) {
        List<UserDetails> users = properties.getUsers().stream()
                .map(u -> User.withUsername(u.getUsername())
                        .password(encoder.encode(u.getPassword()))
                        .roles(u.getRole())
                        .build())
                .toList();
        return new InMemoryUserDetailsManager(users);
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.POST, "/customers/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/customers/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/customers/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/customers/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/customers/**").hasAnyRole("USER", "ADMIN")
                        .requestMatchers("/h2-console/**").permitAll()
                        .requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**").permitAll()
                        .anyRequest().authenticated()
                )
                .headers(headers -> headers.frameOptions(frame -> frame.disable()))
                .httpBasic(basic -> {
                });
        return http.build();
    }
}