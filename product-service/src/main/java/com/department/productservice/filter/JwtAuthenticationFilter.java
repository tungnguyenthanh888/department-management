package com.department.productservice.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class JwtAuthenticationFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try {
            String username = request.getHeader("X-User-Id");
            String role = request.getHeader("X-User-Role"); // Lấy field "role" ra

            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                // 4. Chuyển đổi Role thành SimpleGrantedAuthority (Thêm tiền tố ROLE_ để dùng được hasRole)
                List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_" + role));

                // 5. Tạo đối tượng Authentication và nạp vào SecurityContextHolder
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        username, null, authorities
                );
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (Exception e) {
            // Token không hợp lệ hoặc hết hạn -> Không nạp authentication, Spring Security tự chặn 403/401 sau
            logger.error("Xác thực JWT thất bại: " + e.getMessage());
        }

        filterChain.doFilter(request, response);
    }
}
