package com.ustc.myy.mcpclientserverdemo.controller.ai;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

/**
 * mcp-client-demo1
 *
 * @author YangyangMiao
 * @version 1.0
 * @email yangyangmiao666@outlook.com
 * @date 2025/4/19 19:13
 */
@RestController
@RequestMapping("/mcp-client-server-demo")
@RequiredArgsConstructor
public class ChatController {

    private final ChatClient chatClient;

//    @Autowired
//    public ChatController(ChatClient chatClient) {
//        this.chatClient = chatClient;
//    }

    @GetMapping("/ai/generate")
    public String generate(@RequestParam(value = "message", defaultValue = "给我讲一个笑话") String message) {
        return chatClient.prompt().user(message).call().content();
    }

    @GetMapping("/ai/generate-stream")
    public Flux<String> generateFlux(@RequestParam(value = "message", defaultValue = "给我讲一个笑话") String message) {
        return chatClient.prompt().user(message).stream().content();
    }
}
