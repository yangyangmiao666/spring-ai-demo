//package com.ustc.myy.mcpclientdemo.config;
//
//import io.modelcontextprotocol.client.McpAsyncClient;
//import io.modelcontextprotocol.client.McpClient;
//import io.modelcontextprotocol.client.McpSyncClient;
//import io.modelcontextprotocol.client.transport.HttpClientSseClientTransport;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.ai.chat.client.ChatClient;
//import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;
//import org.springframework.ai.ollama.OllamaChatModel;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//
///**
// * mcp-sse配置类
// *
// * @author YangyangMiao
// * @version 1.0
// * @email yangyangmiao666@icloud.com
// * @date 2025/4/21 21:36
// */
//@Slf4j
//@Configuration
//public class McpConfig {
//
////    @Bean(destroyMethod = "close")
////    public McpSyncClient mcpClient() {
////        McpSyncClient mcpSyncClient = McpClient.sync(HttpClientSseClientTransport.builder("http://localhost:8091").build())
////                .toolsChangeConsumer(tools -> {
////                    log.info("tools change: {}", tools);
////                }).build();
////        mcpSyncClient.initialize();
////        return mcpSyncClient;
////    }
//
//    @Bean(destroyMethod = "close")
//    public McpSyncClient mcpClient() {
//        McpSyncClient mcpSyncClient = McpClient.sync(HttpClientSseClientTransport.builder("http://localhost:8091").build())
//                .toolsChangeConsumer(tools -> log.info("tools change: {}", tools)).build();
//        mcpSyncClient.initialize();
//        return mcpSyncClient;
//    }
//
//    @Bean
//    public ChatClient chatClient(OllamaChatModel model, McpSyncClient mcpSyncClient) {
//        return ChatClient.builder(model).defaultSystem("你是一个可爱的助手，名字叫小糯米").defaultTools(
//                new SyncMcpToolCallbackProvider(mcpSyncClient)
//        ).build();
//    }
//}
