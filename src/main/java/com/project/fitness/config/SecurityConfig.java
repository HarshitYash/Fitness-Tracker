package com.project.fitness.config;

import com.project.fitness.security.JwtAuthenticationFilter;
import com.project.fitness.security.CustomUserDetailsService;
import com.project.fitness.security.OAuth2LoginSuccessHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final JwtAuthenticationFilter jwtAuthFilter;
    private final CustomUserDetailsService userDetailsService;
    private final OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;
    private final PasswordEncoder passwordEncoder;
    private final org.springframework.security.oauth2.client.registration.ClientRegistrationRepository clientRegistrationRepository;

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .cors(Customizer.withDefaults())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/",
                    "/index.html",
                    "/home",
                    "/login",                  // <--- Add this
                    "/register",
                    "/forgot-password",
                    "/reset-password",
                    "/verify-otp",
                    "/dashboard",
                    "/activities",
                    "/recommendations",
                    "/css/**",
                    "/js/**",
                    "/img/**",
                    "/favicon.ico",
                    "/favicon.svg",
                    "/error",
                    "/api/auth/**",
                    "/api/v1/auth/**",
                    "/oauth2/**",
                    "/login/oauth2/**",
                    "/swagger-ui/**",
                    "/swagger-ui.html",
                    "/v3/api-docs/**"
                ).permitAll()
                .requestMatchers("/api/admin/**", "/api/v1/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            .exceptionHandling(e -> e
                .defaultAuthenticationEntryPointFor(
                    new org.springframework.security.web.authentication.Http403ForbiddenEntryPoint(),
                    request -> request.getRequestURI() != null && request.getRequestURI().startsWith("/api/")
                )
            );

        if (clientRegistrationRepository != null && (clientRegistrationRepository.findByRegistrationId("google") != null
                || clientRegistrationRepository.findByRegistrationId("github") != null)) {
            http.oauth2Login(oauth -> oauth
                .clientRegistrationRepository(clientRegistrationRepository)
                .loginPage("/")               // <--- Tells Spring your login page is at "/"
                .failureUrl("/?error=oauth_cancelled") // <--- Redirects to "/" if cancelled/fails
                .successHandler(oAuth2LoginSuccessHandler)
            );
        }

        http.authenticationProvider(authenticationProvider())
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}

