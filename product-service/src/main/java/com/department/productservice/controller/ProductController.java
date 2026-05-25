package com.department.productservice.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/products")
public class ProductController {
    @GetMapping
    public String demo(@RequestHeader(value = "X-User-Id", required = false) String userId,
                       @RequestHeader(value = "X-User-Role", required = false) String role)
    {
        return userId + role;
    }
}
