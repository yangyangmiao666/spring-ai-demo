package com.ustc.myy.mcpserverdemo.config;

import com.ustc.myy.mcpserverdemo.mcpservertools.UserMcpServerTools;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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

    @Bean
    public ToolCallbackProvider userTools(UserMcpServerTools userMcpServerTools) {
        return MethodToolCallbackProvider.builder().toolObjects(userMcpServerTools).build();
    }
}
