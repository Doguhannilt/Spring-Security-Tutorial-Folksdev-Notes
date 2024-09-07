package com.SpringSecurityFolksDevTutorial.demo.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

import java.net.http.HttpRequest;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService  users() {
        UserDetails user1 = User.builder()
                .username("fsk")
                .password(passwordEncoder().encode("fsk"))
                .roles("USER")
                .build();
        UserDetails admin = User.builder()
                .username("fsk_admin")
                .password(passwordEncoder().encode("fsk_admin"))
                .roles("ADMIN")
                .build();
        return new InMemoryUserDetailsManager(user1, admin);
    }



    @Bean
    public SecurityFilterChain filterChain(HttpSecurity security) throws Exception {
        security
                .headers(x -> x.frameOptions(HeadersConfigurer.FrameOptionsConfig:: disable))
                .csrf(csrf -> csrf.disable())
                .formLogin(AbstractHttpConfigurer:: disable)
                // Kim gelirse gelsin kabul et
                .authorizeHttpRequests(x-> x.requestMatchers("/public/**", "/auth/**").permitAll())
                        // Yukarıdaki url'ler hariç herhangi bir yer için authenticated olması lazım
                        .authorizeHttpRequests(x -> x.anyRequest().authenticated())
                .httpBasic(Customizer.withDefaults());
        return security.build();
    }
}
