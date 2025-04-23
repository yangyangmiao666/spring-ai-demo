package com.ustc.myy.ragdemo.controller;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/embedding-demo")
public class EmbeddingController {

    private final EmbeddingModel embeddingModel;

    @Autowired
    public EmbeddingController(@Qualifier("openAiEmbeddingModel") EmbeddingModel embeddingModel) {
        this.embeddingModel = embeddingModel;
    }

    @GetMapping("/embedding")
    public float[] embedding(@RequestParam String message) {
        return embeddingModel.embed(message);
    }
}
