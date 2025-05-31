package com.ustc.myy.springaitoolsdemo.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * chat & mcp-client配置类
 *
 * @author YangyangMiao
 * @version 1.0
 * @email yangyangmiao666@icloud.com
 * @date 2025/4/20 00:19
 */
@Configuration
public class ChatClientConfig {

//    private final OllamaChatModel ollamaChatModel;

    private final OpenAiChatModel openAiChatModel;

//    @Qualifier(value = "mcpTools")
    private final ToolCallbackProvider mcpTools;

//    @Qualifier(value = "userTools")
    private final ToolCallbackProvider userTools;

    @Autowired
    public ChatClientConfig(// OllamaChatModel ollamaChatModel,
                            OpenAiChatModel openAiChatModel,
                            @Qualifier(value = "mcpTools") ToolCallbackProvider mcpTools,
                            @Qualifier(value = "userTools") ToolCallbackProvider userTools) {
//        this.ollamaChatModel = ollamaChatModel;
        this.openAiChatModel = openAiChatModel;
        this.mcpTools = mcpTools;
        this.userTools = userTools;
    }

//    @Bean
//    public ChatClient ollamaChatClient() {
//        return ChatClient.builder(ollamaChatModel)
//                .defaultSystem("你是一个可爱的助手，名字叫小糯米")
//                .defaultTools(tools)
//                .defaultAdvisors(new MessageChatMemoryAdvisor(new InMemoryChatMemory()),
//                        new SimpleLoggerAdvisor())
//                .build();
//    }

    @Bean
    public ChatMemory chatMemory() {
        return MessageWindowChatMemory.builder().build();
    }


    @Bean
    public ChatClient openAiChatClient(ChatMemory chatMemory) {
        return ChatClient.builder(openAiChatModel)
                .defaultSystem("你是一个可爱的助手，名字叫小糯米")
                .defaultToolCallbacks(mcpTools, userTools)
                .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build(),
                        new SimpleLoggerAdvisor())
                .build();
    }
}
