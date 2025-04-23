package com.ustc.myy.mcpclientserverdemo.controller.ai;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
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
public class ChatController {

    private final ChatClient ollamaChatClient;

    private final ChatClient openAiChatClient;


    @Autowired
    public ChatController(@Qualifier("ollamaChatClient") ChatClient ollamaChatClient,
                          @Qualifier("openAiChatClient") ChatClient openAiChatClient) {
        this.openAiChatClient = openAiChatClient;
        this.ollamaChatClient = ollamaChatClient;
    }

    @GetMapping("/ai/generate")
    public String generate(@RequestParam(value = "message", defaultValue = "给我讲一个笑话") String message) {
        return openAiChatClient.prompt().user(message).call().content();
    }

    @GetMapping("/ai/generate-stream")
    public Flux<String> generateFlux(@RequestParam(value = "message", defaultValue = "给我讲一个笑话") String message) {
        return openAiChatClient.prompt().user(message).stream().content();
    }
}
