package com.devmate.icebreakerservice.config;

import com.google.cloud.vertexai.generativeai.GenerativeModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GeminiConfig {

    @Value("${gemini.api.key}")
    private String geminiApiKey;

    @Value("${gemini.model.name:gemini-pro}")
    private String modelName;

    @Bean
    public GenerativeModel generativeModel() {
        return new GenerativeModel(modelName, geminiApiKey);
    }
}
