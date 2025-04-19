package com.ustc.myy.mcpclientserverdemo.config;

import com.ustc.myy.mcpclientserverdemo.annotations.McpTool;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

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
    @Primary
    public ToolCallbackProvider userTools() {
        return MethodToolCallbackProvider.builder().toolObjects(
                context.getBeansWithAnnotation(McpTool.class).values().toArray()
        ).build();
    }
}
