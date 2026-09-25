package com.project.fitness.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.oauth2.client.CommonOAuth2Provider;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class OAuthClientConfig {

    @Bean
    ClientRegistrationRepository clientRegistrationRepository(
            @Value("${app.oauth.google.client-id:}") String googleId,
            @Value("${app.oauth.google.client-secret:}") String googleSecret,
            @Value("${app.oauth.github.client-id:}") String githubId,
            @Value("${app.oauth.github.client-secret:}") String githubSecret) {
        List<ClientRegistration> registrations = new ArrayList<>();
        if (isConfigured(googleId, googleSecret)) {
            registrations.add(CommonOAuth2Provider.GOOGLE.getBuilder("google")
                    .clientId(googleId.trim())
                    .clientSecret(googleSecret.trim())
                    .scope("openid", "profile", "email")
                    .redirectUri("{baseUrl}/login/oauth2/code/{registrationId}")
                    .build());
        }
        if (isConfigured(githubId, githubSecret)) {
            registrations.add(CommonOAuth2Provider.GITHUB.getBuilder("github")
                    .clientId(githubId.trim())
                    .clientSecret(githubSecret.trim())
                    .scope("read:user", "user:email")
                    .redirectUri("{baseUrl}/login/oauth2/code/{registrationId}")
                    .build());
        }
        if (registrations.isEmpty()) {
            return new EmptyClientRegistrationRepository();
        }
        return new InMemoryClientRegistrationRepository(registrations);
    }

    public static boolean isConfigured(String id, String secret) {
        return id != null && secret != null
                && !id.isBlank() && !secret.isBlank()
                && !id.startsWith("demo-");
    }

    /**
     * Placeholder so the app starts when Google/GitHub keys are not set yet.
     */
    static final class EmptyClientRegistrationRepository implements ClientRegistrationRepository {
        @Override
        public ClientRegistration findByRegistrationId(String registrationId) {
            return null;
        }
    }
}
