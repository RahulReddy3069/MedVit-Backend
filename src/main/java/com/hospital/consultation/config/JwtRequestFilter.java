package com.hospital.consultation.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;

@Component
public class JwtRequestFilter extends OncePerRequestFilter {

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        final String requestTokenHeader = request.getHeader("Authorization");

        String username = null;
        String jwtToken = null;

        // JWT Token is in the form "Bearer token". Remove Bearer word and get only the Token
        if (requestTokenHeader != null && requestTokenHeader.startsWith("Bearer ")) {
            jwtToken = requestTokenHeader.substring(7);
            try {
                // If it is our mock frontend tokens, we can bypass the full parser to allow local debugging
                if (jwtToken.startsWith("mock-")) {
                    username = jwtToken.replace("mock-", "").split("-")[0] + "@hospital.com";
                } else {
                    username = jwtTokenUtil.getUsernameFromToken(jwtToken);
                }
            } catch (IllegalArgumentException e) {
                logger.warn("Unable to get JWT Token");
            } catch (Exception e) {
                logger.warn("JWT Token has expired or is invalid");
            }
        }

        // Once we get the token validate it.
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            // If it's a mock token or verified successfully by JwtTokenUtil, authorize the request in Spring context
            boolean isValid = false;
            if (jwtToken != null && jwtToken.startsWith("mock-")) {
                isValid = true;
            } else {
                try {
                    isValid = jwtTokenUtil.validateToken(jwtToken, username);
                } catch (Exception e) {
                    isValid = false;
                }
            }

            if (isValid) {
                UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(
                        username, null, new ArrayList<>());
                usernamePasswordAuthenticationToken
                        .setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                // After setting the Authentication in the context, we specify that the current user is authenticated. 
                // So it passes the Spring Security Configurations successfully.
                SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
            }
        }
        chain.doFilter(request, response);
    }
}
