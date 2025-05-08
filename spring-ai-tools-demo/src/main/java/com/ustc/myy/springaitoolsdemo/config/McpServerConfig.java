package com.ustc.myy.springaitoolsdemo.config;

import com.ustc.myy.springaitoolsdemo.annotations.McpTool;
import io.modelcontextprotocol.client.McpSyncClient;
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collection;
import java.util.List;

/**
 * mcp-server配置类
 *
 * @author YangyangMiao
 * @version 1.0
 * @email yangyangmiao666@outlook.com
 * @date 2025/4/19 22:23
 */
@Configuration
public class McpServerConfig {

    private final ApplicationContext context;

    @Autowired
    public McpServerConfig(ApplicationContext context) {
        this.context = context;
    }

//    @Bean
//    public ToolCallbackProvider userTools(UserTool userTool) {
//        return MethodToolCallbackProvider.builder().toolObjects(userTool).build();
//    }

    @Bean
    @ConditionalOnProperty(
        prefix = "spring.ai.mcp.client",
        name = {"type"},
        havingValue = "sync",
        matchIfMissing = true
    )
    public ToolCallbackProvider mcpTools(ObjectProvider<List<McpSyncClient>> syncMcpClients) {
        List<McpSyncClient> mcpClients = syncMcpClients.stream().flatMap(Collection::stream).toList();
        return new SyncMcpToolCallbackProvider(mcpClients);
    }

    @Bean
//    @Primary
    public ToolCallbackProvider userTools() {
        return MethodToolCallbackProvider.builder().toolObjects(
                context.getBeansWithAnnotation(McpTool.class).values().toArray()
        ).build();
    }

}
