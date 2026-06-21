package org.servicehub.component.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.flywaydb.core.internal.database.DatabaseExecutionStrategy;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class JwtTokenProvider {

    private final SecretKey key = Keys.hmacShaKeyFor(
            "super-secret-key-for-this-application-hehehehehe".getBytes(StandardCharsets.UTF_8)
    );
    private final long validityInMs = 3_600_000;

    public String createToken(String email, Set<String> roles) {
        Claims claims = Jwts.claims().subject(email).add("roles", roles).build();
        Date now = new Date();
        Date validity = new Date(now.getTime() + validityInMs);

        return Jwts.builder()
                .claims(claims)
                .issuedAt(now)
                .expiration(validity)
                .signWith(key)
                .compact();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser().verifyWith(key).build().parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public String getEmailFromToken(String token) {
        return Jwts.parser().verifyWith(key).build()
                .parseSignedClaims(token).getPayload().getSubject();
    }

    public Set<String> getRolesFromToken(String token) {
        List<?> roles = Jwts.parser().verifyWith(key).build()
                .parseSignedClaims(token).getPayload()
                .get("roles", List.class);
        return roles.stream()
                .map(String.class::cast)
                .collect(Collectors.toSet());
    }
}
