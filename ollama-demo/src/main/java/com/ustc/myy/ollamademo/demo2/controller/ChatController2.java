package com.ustc.myy.ollamademo.demo2.controller;

import org.springframework.ai.chat.client.ChatClient;
import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Ollama ChatController
 *
 * @author YangyangMiao
 * @email yangyangmiao666@icloud.com
 * @date 2025/4/19 11:02
 * @version 1.0
 */
@RestController
@RequestMapping("/ollama-demo2")
public class ChatController2 {

    private final ChatClient chatClient;

    @Autowired
    public ChatController2(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    @GetMapping(value = "/ai/generate", produces = "text/html;charset=utf-8")
    public String generate(@RequestParam(value = "message", defaultValue = "给我讲一个笑话") String message, @RequestParam(value = "chatId") String chatId) {
        return chatClient.prompt()
                .user(message)
                .advisors(advisorSpec -> advisorSpec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId))
                .call()
                .content();
    }
}
