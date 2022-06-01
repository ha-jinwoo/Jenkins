package com.example.demo;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {
    @GetMapping("/hello")
    public String hello() {
        return "Hello Aurora3~!!!";
    }

    @GetMapping("/hello2")
    public String hello2() {
        return "Hi jenkins";
    }
}
