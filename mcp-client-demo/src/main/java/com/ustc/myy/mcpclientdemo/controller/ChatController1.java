package com.ustc.myy.mcpclientdemo.controller;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * mcp-client-demo1
 *
 * @author YangyangMiao
 * @email yangyangmiao666@icloud.com
 * @date 2025/4/19 19:13
 * @version 1.0
 */
@RestController
@RequestMapping("/mcp-client-demo1")
public class ChatController1 {


    private final ChatClient chatClient;
//
//    @Autowired
//    public ChatController1(ChatClient chatClient) {
//        this.chatClient = chatClient;
//    }

    @Autowired
    public ChatController1(ChatClient chatClient) {
        this.chatClient = chatClient;

    }

    @GetMapping("/ai/generate")
    public String generate(@RequestParam(value = "message", defaultValue = "给我讲一个笑话") String message) {
        return chatClient.prompt().user(message).call().content();
    }
}
