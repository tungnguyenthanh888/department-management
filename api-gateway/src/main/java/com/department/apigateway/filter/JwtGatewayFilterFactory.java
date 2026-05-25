package com.department.apigateway.filter;

import com.department.apigateway.jwt.JwtUtils;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class JwtGatewayFilterFactory extends AbstractGatewayFilterFactory<JwtGatewayFilterFactory.Config> {

    private final JwtUtils jwtUtils;

    public JwtGatewayFilterFactory(JwtUtils jwtUtils) {
        super(Config.class);
        this.jwtUtils = jwtUtils;
    }

    public static class Config {
        // Có thể thêm các thuộc tính cấu hình cho filter ở đây nếu cần
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            System.out.println("Vao Filter:");
            // 1. Kiểm tra xem có chứa Header Authorization không
            if (!request.getHeaders().containsHeader(HttpHeaders.AUTHORIZATION)) {
                return onError(exchange, "Missing Authorization Header", HttpStatus.UNAUTHORIZED);
            }

            String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

            // 2. Kiểm tra định dạng Bearer token
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return onError(exchange, "Invalid Authorization Header Format", HttpStatus.UNAUTHORIZED);
            }

            String token = authHeader.substring(7);

            try {
                // 3. Sử dụng JwtParser của JJWT để giải mã và xác thực token (Chữ ký, Hết hạn...)
                Claims claims = jwtUtils.extractAllClaims(token);

                // (Tùy chọn thêm) Lấy thông tin user truyền xuống các service con qua Header mới nếu cần
                ServerHttpRequest modifiedRequest = request.mutate()
                        .header("X-User-Id", claims.getSubject())
                        .header("X-User-Role", claims.get("role", String.class))
                        .build();

                return chain.filter(exchange.mutate().request(modifiedRequest).build());

            } catch (JwtException | IllegalArgumentException e) {
                // Bắt toàn bộ các lỗi: Sai chữ ký, Hết hạn, Token rỗng/lỗi...
                return onError(exchange, "Token Validation Failed: " + e.getMessage(), HttpStatus.UNAUTHORIZED);
            }
        };
    }

    // Hàm bổ trợ để ngắt luồng Reactive và trả về trạng thái 401 Unauthorized lập tức
    private Mono<Void> onError(ServerWebExchange exchange, String err, HttpStatus status) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(status);
        // Thiết lập thêm Header báo lỗi chi tiết cho phía Client dễ debug
        response.getHeaders().add("X-Gateway-Error", err);
        return response.setComplete();
    }
}