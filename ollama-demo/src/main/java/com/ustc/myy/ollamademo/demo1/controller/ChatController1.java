package com.ustc.myy.ollamademo.demo1.controller;

import java.util.Map;

import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import reactor.core.publisher.Flux;

/**
 * Ollama ChatController
 *
 * @author YangyangMiao
 * @email yangyangmiao666@icloud.com
 * @date 2025/4/19 10:59
 * @version 1.0
 */
@RestController
@RequestMapping("/ollama-demo1")
public class ChatController1 {

    private final OllamaChatModel chatModel;

    @Autowired
    public ChatController1(OllamaChatModel chatModel) {
        this.chatModel = chatModel;
    }

    @GetMapping(value = "/ai/generate", produces = "text/html;charset=utf-8")
    public Map<String, String> generate(@RequestParam(value = "message", defaultValue = "给我讲一个笑话") String message) {
        return Map.of("generation", this.chatModel.call(message));
    }

    @GetMapping(value = "/ai/generate-stream")
    public Flux<ChatResponse> generateStream(@RequestParam(value = "message", defaultValue = "给我讲一个笑话") String message) {
        Prompt prompt = new Prompt(new UserMessage(message));
        return this.chatModel.stream(prompt);
    }

}
