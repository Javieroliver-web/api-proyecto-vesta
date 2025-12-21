package com.vesta.api.config;

import com.vesta.api.util.JWTUtil;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    @Autowired
    private JWTUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);

            try {
                String username = jwtUtil.extractUsername(token);
                String rol = jwtUtil.extractClaim(token, claims -> claims.get("rol", String.class));

                if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    if (jwtUtil.validateToken(token, username)) {
                        SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + rol);
                        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                                username, null, Collections.singletonList(authority));
                        SecurityContextHolder.getContext().setAuthentication(authToken);
                    }
                }
            } catch (ExpiredJwtException e) {
                logger.warn("Token expirado para usuario: {}", e.getClaims().getSubject());
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setHeader("X-Auth-Error", "Token expired");
            } catch (MalformedJwtException e) {
                logger.warn("Token malformado recibido desde IP: {}", request.getRemoteAddr());
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setHeader("X-Auth-Error", "Malformed token");
            } catch (SignatureException e) {
                logger.warn("Firma de token inválida desde IP: {}", request.getRemoteAddr());
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setHeader("X-Auth-Error", "Invalid signature");
            } catch (Exception e) {
                logger.error("Error inesperado validando token", e);
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setHeader("X-Auth-Error", "Authentication error");
            }
        }
        filterChain.doFilter(request, response);
    }
}