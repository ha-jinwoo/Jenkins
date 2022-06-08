package com.example.demo.hello;

import lombok.Builder;
import lombok.Getter;

@Getter
public class HelloDto {

    String text;
    String text2;

    @Builder
    public HelloDto(String text, String text2) {
        this.text = text;
        this.text2 = text2;
    }
}