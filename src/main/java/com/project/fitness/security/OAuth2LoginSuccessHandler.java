package com.project.fitness.security;

import com.project.fitness.entity.AuthProvider;
import com.project.fitness.entity.Role;
import com.project.fitness.entity.User;
import com.project.fitness.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        OAuth2AuthenticationToken oauth = (OAuth2AuthenticationToken) authentication;
        String registrationId = oauth.getAuthorizedClientRegistrationId();
        AuthProvider provider = "github".equalsIgnoreCase(registrationId)
                ? AuthProvider.GITHUB
                : AuthProvider.GOOGLE;

        OAuth2User oauthUser = oauth.getPrincipal();
        String email = extractEmail(oauthUser, registrationId);
        String firstName = extractFirstName(oauthUser);
        String lastName = extractLastName(oauthUser, firstName);

        User user = userRepository.findByEmail(email).orElseGet(() -> userRepository.save(User.builder()
                .email(email)
                .password(passwordEncoder.encode(UUID.randomUUID().toString()))
                .firstName(firstName)
                .lastName(lastName)
                .role(Role.USER)
                .authProvider(provider)
                .emailVerified(true)
                .phoneVerified(false)
                .build()));

        if (user.getAuthProvider() == AuthProvider.LOCAL) {
            user.setEmailVerified(true);
            userRepository.save(user);
        } else if (!user.isEmailVerified()) {
            user.setEmailVerified(true);
            userRepository.save(user);
        }

        var details = org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail())
                .password(user.getPassword())
                .authorities(List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name())))
                .build();

        String token = jwtService.generateToken(details);
        response.sendRedirect("/?token=" + token
                + "&userId=" + user.getId()
                + "&email=" + user.getEmail()
                + "&firstName=" + java.net.URLEncoder.encode(user.getFirstName(), java.nio.charset.StandardCharsets.UTF_8)
                + "&lastName=" + java.net.URLEncoder.encode(user.getLastName(), java.nio.charset.StandardCharsets.UTF_8));
    }

    private String extractEmail(OAuth2User user, String registrationId) {
        String email = user.getAttribute("email");
        if (email != null && !email.isBlank()) {
            return email.toLowerCase().trim();
        }
        if ("github".equalsIgnoreCase(registrationId)) {
            String login = user.getAttribute("login");
            if (login != null) {
                return login.toLowerCase() + "@users.noreply.github.com";
            }
        }
        return UUID.randomUUID() + "@oauth.local";
    }

    private String extractFirstName(OAuth2User user) {
        String name = user.getAttribute("given_name");
        if (name != null && !name.isBlank()) {
            return name;
        }
        name = user.getAttribute("name");
        if (name != null && !name.isBlank()) {
            return name.split(" ")[0];
        }
        String login = user.getAttribute("login");
        return login != null ? login : "User";
    }

    private String extractLastName(OAuth2User user, String firstName) {
        String family = user.getAttribute("family_name");
        if (family != null && !family.isBlank()) {
            return family;
        }
        String name = user.getAttribute("name");
        if (name != null && name.contains(" ")) {
            return name.substring(name.indexOf(' ') + 1);
        }
        return "Account";
    }
}
