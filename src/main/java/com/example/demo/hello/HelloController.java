package com.example.demo.hello;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class HelloController {

    private final HelloService helloService;

    @GetMapping("/hello")
    public String hello() {
        return "Hello Aurora3~!!!";
    }

    @GetMapping("/hello/dto")
    public HelloDto helloDto(){
        return helloService.findDefault();
    }

    @PostMapping("/hello/dto")
    public void putHello(){
        helloService.makeDefaultHello();
    }
}
