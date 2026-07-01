package org.servicehub.unit.component;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.servicehub.component.jwt.JwtTokenFilter;
import org.servicehub.component.jwt.JwtTokenProvider;
import org.servicehub.dto.auth.UserPrincipal;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtTokenFilterTest {

    @Mock
    private JwtTokenProvider provider;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private JwtTokenFilter filter;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void doFilter_NoAuthorizationHeader_ShouldNotSetAuthentication() throws Exception {
        when(request.getHeader("Authorization")).thenReturn(null);

        filter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(provider, never()).validateToken(any());
    }

    @Test
    void doFilter_InvalidAuthHeader_ShouldNotSetAuthentication() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Basic token");

        filter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(provider, never()).validateToken(any());
    }

    @Test
    void doFilter_TokenNotValid_ShouldNotSetAuthentication() throws Exception {
        String token = "invalid.token";
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(provider.validateToken(token)).thenReturn(false);

        filter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(provider).validateToken(token);
        verify(provider, never()).getIdFromToken(any());
        verify(provider, never()).getEmailFromToken(any());
        verify(provider, never()).getRolesFromToken(any());
    }

    @Test
    void doFilter_ValidToken_ShouldSetAuthentication() throws Exception {
        String token = "valid.jwt.token";
        Long userId = 42L;
        String email = "john.doe@example.com";
        Set<String> roles = Set.of("ROLE_USER", "ROLE_ADMIN");

        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(provider.validateToken(token)).thenReturn(true);
        when(provider.getIdFromToken(token)).thenReturn(userId);
        when(provider.getEmailFromToken(token)).thenReturn(email);
        when(provider.getRolesFromToken(token)).thenReturn(roles);

        filter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertThat(authentication).isNotNull();
        assertThat(authentication.isAuthenticated()).isTrue();

        Object principal = authentication.getPrincipal();
        assertThat(principal).isInstanceOf(UserPrincipal.class);
        UserPrincipal userPrincipal = (UserPrincipal) principal;
        assertThat(userPrincipal).isNotNull();
        assertThat(userPrincipal.getId()).isEqualTo(userId);
        assertThat(userPrincipal.getEmail()).isEqualTo(email);

        assertThat(authentication.getAuthorities())
                .extracting("authority")
                .containsExactlyInAnyOrder("ROLE_USER", "ROLE_ADMIN");

        assertThat(authentication.getDetails()).isNotNull();
    }
}
