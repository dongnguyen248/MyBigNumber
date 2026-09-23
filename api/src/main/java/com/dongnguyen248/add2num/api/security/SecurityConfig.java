package com.dongnguyen248.add2num.api.security;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(authorize -> authorize.anyRequest().authenticated())
                .httpBasic(httpBasic -> { });
        return http.build();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    UserDetailsService userDetailsService(
            PasswordEncoder passwordEncoder,
            @Value("${api.security.technician-username:}") String technicianUsername,
            @Value("${api.security.technician-password:}") String technicianPassword,
            @Value("${api.security.supervisor-username:}") String supervisorUsername,
            @Value("${api.security.supervisor-password:}") String supervisorPassword) {
        List<UserDetails> users = new ArrayList<>();
        addUser(users, passwordEncoder, technicianUsername, technicianPassword, "TECHNICIAN");
        addUser(users, passwordEncoder, supervisorUsername, supervisorPassword, "SUPERVISOR");
        return new InMemoryUserDetailsManager(users.toArray(UserDetails[]::new));
    }

    private void addUser(
            List<UserDetails> users,
            PasswordEncoder passwordEncoder,
            String username,
            String password,
            String role) {
        if (username != null && !username.isBlank() && password != null && !password.isBlank()) {
            users.add(User.withUsername(username)
                    .password(passwordEncoder.encode(password))
                    .roles(role)
                    .build());
        }
    }
}
