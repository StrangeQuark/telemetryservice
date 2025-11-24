// Integration file: Auth

package com.strangequark.telemetryservice.utility;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.security.Key;

@Service
public class JwtUtility {
    private static final Logger LOGGER = LoggerFactory.getLogger(JwtUtility.class);

    @Value("${ACCESS_SECRET_KEY}")
    private String SECRET_KEY;

    public boolean validateToken() {
        LOGGER.debug("Attempting to validate JWT");

        try {
            String token = getTokenFromHeader();
            Key key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(SECRET_KEY));

            Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token);

            LOGGER.debug("JWT is valid");
            return true;
        } catch (Exception ex) {
            LOGGER.error("Failed to validate token: " + ex.getMessage());
            LOGGER.debug("Stack trace: ", ex);
            return false;
        }
    }

    private String getTokenFromHeader() {
        LOGGER.debug("Attempting to get token from header");
        try {
            ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs == null) {
                throw new IllegalStateException("No request context available");
            }

            HttpServletRequest request = attrs.getRequest();
            String authHeader = request.getHeader("Authorization");

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                throw new RuntimeException("Missing or invalid Authorization header");
            }

            LOGGER.debug("Token successfully retrieved from header");
            return authHeader.substring(7); // Remove "Bearer "
        } catch (Exception ex) {
            LOGGER.error("Failed to get token from header: " + ex.getMessage());
            LOGGER.debug("Stack trace: ", ex);
            return null;
        }
    }
}
