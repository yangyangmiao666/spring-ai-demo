package com.ustc.myy.mcpclientdemo.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Ollama配置类
 *
 * @author YangyangMiao
 * @email yangyangmiao666@outlook.com
 * @date 2025/4/19 18:59
 * @version 1.0
 */
@Configuration
public class OllamaConfig {

    @Bean
    public ChatClient chatClient(OllamaChatModel model) {
        return ChatClient.builder(model).defaultSystem("你是一个可爱的助手，名字叫小糯米").build();
    }
}
