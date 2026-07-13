package com.innowise.paymentservice.security.filter;

import com.google.common.net.HttpHeaders;
import feign.FeignException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import com.innowise.paymentservice.dto.TokenValidationRequestDto;
import com.innowise.paymentservice.dto.TokenValidationResponseDto;
import com.innowise.paymentservice.feign.AuthServiceClient;
import com.innowise.paymentservice.security.UserPrincipal;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final AuthServiceClient authServiceClient;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);

        if(header == null || !header.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = header.substring(7);

        TokenValidationResponseDto validationResponse;

        try {
            validationResponse = authServiceClient.validate(new TokenValidationRequestDto(token));
        } catch (FeignException e) {
            response.sendError(e.status());
            return;
        }

        if(validationResponse == null || !validationResponse.isValid()) {
            response.setStatus(HttpStatus.FORBIDDEN.value());
            return;
        }

        List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_" + validationResponse.getRole()));

        UserPrincipal principal = new UserPrincipal(validationResponse.getUserId(), validationResponse.getRole());

        Authentication authentication = new UsernamePasswordAuthenticationToken(principal, null, authorities);

        SecurityContextHolder.getContext().setAuthentication(authentication);

        filterChain.doFilter(request, response);
    }
}
