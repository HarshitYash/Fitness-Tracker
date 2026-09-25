package com.project.fitness.controller;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
public class AdminController {
    @GetMapping("/hello")
    public String hello() {
        return "Hello Admin";
    }
}
