package com.example.demo.hello;

import org.springframework.data.jpa.repository.JpaRepository;

public interface HelloRepository extends JpaRepository<HelloEntity, Long> {
    HelloEntity findHelloEntityByText1(String Text1);
}
