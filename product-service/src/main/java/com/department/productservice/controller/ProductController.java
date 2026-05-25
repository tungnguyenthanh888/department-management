package com.department.productservice.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/products")
public class ProductController {
    private static final List<Map<String, Object>> products = new ArrayList<>();

    static {
        products.add(Map.of("id", 1, "name", "iPhone 15 Pro", "price", 1000));
        products.add(Map.of("id", 2, "name", "MacBook Pro M3", "price", 2000));
    }

    @GetMapping
    public String demo(@RequestHeader(value = "X-User-Id", required = false) String userId,
                       @RequestHeader(value = "X-User-Role", required = false) String role)
    {
        return userId + role;
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')") // Hoặc dùng: hasAuthority('ROLE_ADMIN')
    public ResponseEntity<?> deleteProduct(@PathVariable int id) {
        // Tìm và xóa dữ liệu tĩnh giả lập
        boolean removed = products.removeIf(p -> p.get("id").equals(id));

        if (removed) {
            return ResponseEntity.ok(Map.of("message", "Đã xóa thành công sản phẩm có ID: " + id));
        } else {
            return ResponseEntity.status(404).body(Map.of("message", "Không tìm thấy sản phẩm cần xóa"));
        }
    }
}
