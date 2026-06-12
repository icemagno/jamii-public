package com.jamii.sdk.example;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import java.security.Security;
import java.io.File;

@SpringBootApplication
public class JamiiApplication {

    @Value("${jamii.wallet.path}")
    private String walletPath;

    @Value("${jamii.wallet.password}")
    private String walletPassword;

    public static void main(String[] args) {
        Security.addProvider(new BouncyCastleProvider());
        SpringApplication.run(JamiiApplication.class, args);
    }

    @Bean
    public CommandLineRunner run() {
        return args -> {
            System.out.println("🚀 Jamii Production SDK Example (Java 22 + Spring Boot) iniciado com sucesso!");
            System.out.println("🌐 Acesse a interface web em: http://localhost:8080");
        };
    }
}
