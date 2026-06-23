package com.zappy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ZappyBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(ZappyBackendApplication.class, args);
        System.out.println("===========================================");
        System.out.println("  Zappy Backend da khoi dong thanh cong!");
        System.out.println("  API: http://localhost:8080/api/");
        System.out.println("===========================================");
    }
}
