package com.ustc.myy.mcpserverdemo.config;

import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.ustc.myy.mcpserverdemo.mcpservertools.UserTools;

/**
 * mcp-server配置类
 *
 * @author YangyangMiao
 * @version 1.0
 * @email yangyangmiao666@icloud.com
 * @date 2025/4/19 22:23
 */
@Configuration
public class McpServerConfig {

    @Bean
    public ToolCallbackProvider toolCallbackProvider(UserTools userTools) {
        return MethodToolCallbackProvider.builder().toolObjects(userTools).build();
    }
}
