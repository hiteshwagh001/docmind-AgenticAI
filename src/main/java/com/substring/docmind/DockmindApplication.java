package com.substring.docmind;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import jakarta.annotation.PostConstruct;
@SpringBootApplication
public class DockmindApplication {

	@Value("${spring.ai.openai.api-key}")
	private String apiKey;

	public static void main(String[] args) {
		SpringApplication.run(DockmindApplication.class, args);
	}

	@PostConstruct
	public void verifyKey() {
		System.out.println("OPENAI KEY = " + apiKey);
	}

}
