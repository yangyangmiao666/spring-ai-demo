package com.ustc.myy.mcpclientserverdemo.config;

import jakarta.annotation.Resource;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * chat & mcp-client配置类
 *
 * @author YangyangMiao
 * @version 1.0
 * @email yangyangmiao666@outlook.com
 * @date 2025/4/20 00:19
 */
@Configuration
public class ChatClientConfig {

    private final OllamaChatModel ollamaChatModel;

    @Resource
    private final ToolCallbackProvider tools;

    @Autowired
    public ChatClientConfig(OllamaChatModel ollamaChatModel, ToolCallbackProvider tools) {
        this.ollamaChatModel = ollamaChatModel;
        this.tools = tools;
    }

    @Bean
    public ChatClient ollamaChatClient() {
        return ChatClient.builder(ollamaChatModel)
                .defaultSystem("你是一个可爱的助手，名字叫小糯米")
                .defaultTools(tools)
                .defaultAdvisors(new MessageChatMemoryAdvisor(new InMemoryChatMemory()))
                .build();
    }
}
