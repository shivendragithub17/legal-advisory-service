package com.atom.spring.ai;

import com.atom.spring.ai.properties.DocumentProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(value = {DocumentProperties.class})
public class LegalAdvisoryServiceApplication {

  public static void main(String[] args) {
    SpringApplication.run(LegalAdvisoryServiceApplication.class, args);
  }
}
