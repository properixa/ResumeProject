package org.servicehub.unit.component;

import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.servicehub.component.jwt.JwtTokenProvider;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.test.util.ReflectionTestUtils;
import org.servicehub.component.security.ServicehubUserDetails;

import java.util.Date;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private ServicehubUserDetails userDetails;

    @Mock
    private GrantedAuthority authority;

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider();
    }

    @Test
    void createToken_ShouldReturnValidJwt() {
        when(userDetails.getUsername()).thenReturn("test@example.com");
        when(userDetails.getId()).thenReturn(1L);
        when(userDetails.getAuthorities()).thenReturn(List.of(authority));
        when(authority.getAuthority()).thenReturn("ROLE_USER");

        String token = jwtTokenProvider.createToken(userDetails);

        assertThat(token)
                .isNotEmpty()
                .matches(jwt -> token.split("\\.").length == 3); // Проверяем структуру JWT
        assertThat(jwtTokenProvider.validateToken(token)).isTrue();
    }

    @Test
    void getEmailFromToken_ShouldReturnCorrectEmail() {
        when(userDetails.getUsername()).thenReturn("test@example.com");
        when(userDetails.getId()).thenReturn(1L);
        when(userDetails.getAuthorities()).thenReturn(List.of(authority));
        when(authority.getAuthority()).thenReturn("ROLE_USER");

        String token = jwtTokenProvider.createToken(userDetails);
        String email = jwtTokenProvider.getEmailFromToken(token);

        assertThat(email).isEqualTo("test@example.com");
    }

    @Test
    void getRolesFromToken_ShouldReturnCorrectRoles() {
        when(userDetails.getUsername()).thenReturn("test@example.com");
        when(userDetails.getId()).thenReturn(1L);
        when(userDetails.getAuthorities()).thenReturn(List.of(authority));
        when(authority.getAuthority()).thenReturn("ROLE_ADMIN");

        String token = jwtTokenProvider.createToken(userDetails);

        Set<String> roles = jwtTokenProvider.getRolesFromToken(token);

        assertThat(roles)
                .hasSize(1)
                .containsExactly("ROLE_ADMIN");
    }

    @Test
    void getIdFromToken_ShouldReturnCorrectId() {
        when(userDetails.getUsername()).thenReturn("test@example.com");
        when(userDetails.getId()).thenReturn(999L);
        when(userDetails.getAuthorities()).thenReturn(List.of(authority));
        when(authority.getAuthority()).thenReturn("ROLE_USER");

        String token = jwtTokenProvider.createToken(userDetails);

        Long id = jwtTokenProvider.getIdFromToken(token);

        assertThat(id).isEqualTo(999L);
    }


    @Test
    void validateToken_ValidToken_ShouldReturnTrue() {
        when(userDetails.getUsername()).thenReturn("test@example.com");
        when(userDetails.getId()).thenReturn(1L);
        when(userDetails.getAuthorities()).thenReturn(List.of(authority));
        when(authority.getAuthority()).thenReturn("ROLE_USER");

        String token = jwtTokenProvider.createToken(userDetails);

        boolean isValid = jwtTokenProvider.validateToken(token);

        assertThat(isValid).isTrue();
    }

    @Test
    void validateToken_MalformedToken_ShouldReturnFalse() {
        String malformed = "this.is.not.a.valid.jwt";

        boolean isValid = jwtTokenProvider.validateToken(malformed);

        assertThat(isValid).isFalse();
    }

    @Test
    void validateToken_NullOrEmpty_ShouldReturnFalse() {
        assertThat(jwtTokenProvider.validateToken(null)).isFalse();
        assertThat(jwtTokenProvider.validateToken("")).isFalse();
    }

    @Test
    void validateToken_TamperedToken_ShouldReturnFalse() {
        when(userDetails.getUsername()).thenReturn("test@example.com");
        when(userDetails.getId()).thenReturn(1L);
        when(userDetails.getAuthorities()).thenReturn(List.of(authority));
        when(authority.getAuthority()).thenReturn("ROLE_USER");

        String originalToken = jwtTokenProvider.createToken(userDetails);
        String tamperedToken = originalToken.substring(0, originalToken.length() - 1) + "X";
        boolean isValid = jwtTokenProvider.validateToken(tamperedToken);

        assertThat(isValid).isFalse();
    }

    @Test
    void validateToken_ExpiredToken_ShouldReturnFalse() {
        Date now = new Date();
        Date past = new Date(now.getTime() - 60_000);

        String expiredToken = Jwts.builder()
                .subject("test@example.com")
                .claim("id", 1L)
                .claim("roles", List.of("ROLE_USER"))
                .issuedAt(now)
                .expiration(past)
                .signWith(jwtTokenProvider.getKey())
                .compact();

        boolean isValid = jwtTokenProvider.validateToken(expiredToken);
        assertThat(isValid).isFalse();
    }
}
