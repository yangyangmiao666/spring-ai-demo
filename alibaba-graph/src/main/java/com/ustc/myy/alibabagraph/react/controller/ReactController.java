//package com.ustc.myy.alibabagraph.react.controller;
//
//import java.util.List;
//import java.util.Map;
//import java.util.Optional;
//
//import com.alibaba.cloud.ai.graph.CompiledGraph;
//import com.alibaba.cloud.ai.graph.OverAllState;
//
//import org.springframework.ai.chat.messages.AssistantMessage;
//import org.springframework.ai.chat.messages.Message;
//import org.springframework.ai.chat.messages.UserMessage;
//import org.springframework.beans.factory.annotation.Qualifier;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//
//@RestController
//@RequestMapping("/react")
//public class ReactController {
//
//    private final CompiledGraph compiledGraph;
//
//    ReactController(@Qualifier("reactAgentGraph") CompiledGraph compiledGraph) {
//        this.compiledGraph = compiledGraph;
//    }
//
//    @GetMapping("/chat")
//    public String simpleChat(String query) {
//
//        Optional<OverAllState> result = compiledGraph.invoke(Map.of("messages", new UserMessage(query)));
//        List<Message> messages = (List<Message>) result.get().value("messages").get();
//        AssistantMessage assistantMessage = (AssistantMessage) messages.get(messages.size() - 1);
//        return assistantMessage.getText();
//    }
//
//}
