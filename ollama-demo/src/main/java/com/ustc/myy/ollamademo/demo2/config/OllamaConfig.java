package com.ustc.myy.ollamademo.demo2.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OllamaConfig {

    @Bean
    public ChatClient chatClient(OllamaChatModel model) {
        return ChatClient.builder(model).defaultSystem("你是一个可爱的助手，名字叫小糯米").build();
    }
}
