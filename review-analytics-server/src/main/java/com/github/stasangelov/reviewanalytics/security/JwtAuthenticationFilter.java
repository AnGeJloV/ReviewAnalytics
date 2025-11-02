package com.github.stasangelov.reviewanalytics.security;

import com.github.stasangelov.reviewanalytics.entity.User;
import com.github.stasangelov.reviewanalytics.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;

/**
 * Кастомный фильтр безопасности, который выполняется для каждого входящего HTTP-запроса.
 * Его основная задача - проверить наличие и валидность JWT-токена в заголовке Authorization.
 * Если токен валиден, фильтр аутентифицирует пользователя в контексте Spring Security.
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String token = resolveToken(request);

        try {
            if (StringUtils.hasText(token) && jwtTokenProvider.validateToken(token)) {
                // Получаем username (email)
                String username = jwtTokenProvider.getUsername(token);

                if (username == null) {
                    filterChain.doFilter(request, response);
                    return;
                }

                // Загружаем сущность User из БД, чтобы проверить active
                User user = userRepository.findByEmail(username).orElse(null);
                if (user == null) {
                    response.sendError(HttpStatus.FORBIDDEN.value(), "User not found");
                    return;
                }

                if (!user.isActive()) {
                    response.sendError(HttpStatus.FORBIDDEN.value(), "User account is disabled");
                    return;
                }

                // Загружаем UserDetails и создаём Authentication (чтобы сохранить совместимость с остальной системой)
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                Authentication authentication = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );

                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (Exception ex) {

        }

        filterChain.doFilter(request, response);
    }

    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
