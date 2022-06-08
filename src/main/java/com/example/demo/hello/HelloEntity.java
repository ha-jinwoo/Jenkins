package com.example.demo.hello;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class HelloEntity {

    @Id @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "hello_id")
    private Long id;

    private String text1;
    private String text2;

    @Builder
    public HelloEntity(String text1, String text2) {
        this.text1 = text1;
        this.text2 = text2;
    }
}