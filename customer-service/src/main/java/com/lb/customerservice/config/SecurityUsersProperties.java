package com.lb.customerservice.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.security")
public class SecurityUsersProperties {

    private List<AppUser> users = new ArrayList<>();

    @Getter
    @Setter
    public static class AppUser {
        private String username;
        private String password;
        private String role;
    }
}