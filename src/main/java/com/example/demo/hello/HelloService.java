package com.example.demo.hello;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class HelloService {

    private final HelloRepository helloRepository;

    @Transactional
    public Long makeDefaultHello(){
        HelloEntity hello = HelloEntity.builder()
                .text1("Hello")
                .text2("world")
                .build();
        return helloRepository.save(hello).getId();
    }

    @Transactional
    HelloDto findDefault(){
        HelloEntity entity = helloRepository.findHelloEntityByText1("Hello");
        HelloDto dto = HelloDto.builder()
                .text(entity.getText1())
                .text2(entity.getText2())
                .build();
        return dto;
    }

}
