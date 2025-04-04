package com.atom.spring.ai.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "document")
public record DocumentProperties(
    @DefaultValue(value = "/src/main/resources/upload") String uploadLocation,
    @DefaultValue(value = "/src/main/resources/vector/vector-store.json") String vectorStoreFileLocation) {}
