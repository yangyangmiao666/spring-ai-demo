package com.ustc.myy.alibabagraph.react.config;


import com.alibaba.cloud.ai.graph.CompiledGraph;
import com.alibaba.cloud.ai.graph.GraphRepresentation;
import com.alibaba.cloud.ai.graph.exception.GraphStateException;
import com.alibaba.cloud.ai.graph.agent.ReactAgent;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.tool.resolution.ToolCallbackResolver;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ReactAutoconfiguration {

    @Bean
    public ReactAgent reactAgent(ChatModel chatModel, ToolCallbackResolver resolver) throws GraphStateException {
        ChatClient chatClient = ChatClient.builder(chatModel)
                .defaultAdvisors(new SimpleLoggerAdvisor())
                .defaultOptions(OpenAiChatOptions.builder().internalToolExecutionEnabled(false).build())
                .build();

        return ReactAgent.builder()
                .name("React Agent Demo")
                .chatClient(chatClient)
                .resolver(resolver)
                .maxIterations(10)
                .build();
    }

    @Bean
    public CompiledGraph reactAgentGraph(@Qualifier("reactAgent") ReactAgent reactAgent)
            throws GraphStateException {

        GraphRepresentation graphRepresentation = reactAgent.getStateGraph()
                .getGraph(GraphRepresentation.Type.PLANTUML);

        System.out.println("\n\n");
        System.out.println(graphRepresentation.content());
        System.out.println("\n\n");

        return reactAgent.getAndCompileGraph();
    }
}
