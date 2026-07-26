package com.archcore.security;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class SecurityController {


    @GetMapping("/test-endpoint")
    public String testEndpoint() {
        return "test-endpoint";
    }
}
