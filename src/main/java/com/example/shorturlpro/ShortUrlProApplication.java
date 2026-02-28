package com.example.shorturlpro;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Component;

@SpringBootApplication
public class ShortUrlProApplication {

    public static void main(String[] args) {
        SpringApplication.run(ShortUrlProApplication.class, args);
    }

}
