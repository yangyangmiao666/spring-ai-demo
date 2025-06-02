package com.ustc.myy.aiagent.manus.planning;


import com.ustc.myy.aiagent.manus.config.ManusProperties;
import com.ustc.myy.aiagent.manus.dynamic.agent.entity.DynamicAgentEntity;
import com.ustc.myy.aiagent.manus.dynamic.agent.service.AgentService;
import com.ustc.myy.aiagent.manus.dynamic.agent.service.DynamicAgentLoader;
import com.ustc.myy.aiagent.manus.dynamic.mcp.model.vo.McpServiceEntity;
import com.ustc.myy.aiagent.manus.dynamic.mcp.model.vo.McpTool;
import com.ustc.myy.aiagent.manus.dynamic.mcp.service.McpService;
import com.ustc.myy.aiagent.manus.dynamic.mcp.service.McpStateHolderService;
import com.ustc.myy.aiagent.manus.llm.LlmService;
import com.ustc.myy.aiagent.manus.planning.coordinator.PlanningCoordinator;
import com.ustc.myy.aiagent.manus.planning.creator.PlanCreator;
import com.ustc.myy.aiagent.manus.planning.executor.PlanExecutor;
import com.ustc.myy.aiagent.manus.planning.finalizer.PlanFinalizer;
import com.ustc.myy.aiagent.manus.recorder.PlanExecutionRecorder;
import com.ustc.myy.aiagent.manus.tool.DocLoaderTool;
import com.ustc.myy.aiagent.manus.tool.PlanningTool;
import com.ustc.myy.aiagent.manus.tool.TerminateTool;
import com.ustc.myy.aiagent.manus.tool.ToolCallBiFunctionDef;
import com.ustc.myy.aiagent.manus.tool.bash.Bash;
import com.ustc.myy.aiagent.manus.tool.browser.BrowserUseTool;
import com.ustc.myy.aiagent.manus.tool.browser.ChromeDriverService;
import com.ustc.myy.aiagent.manus.tool.code.CodeUtils;
import com.ustc.myy.aiagent.manus.tool.code.PythonExecute;
import com.ustc.myy.aiagent.manus.tool.searchAPI.GoogleSearch;
import com.ustc.myy.aiagent.manus.tool.textOperator.TextFileOperator;
import com.ustc.myy.aiagent.manus.tool.textOperator.TextFileService;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.util.Timeout;
import org.springframework.ai.model.tool.ToolCallingManager;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.function.FunctionToolCallback;
import org.springframework.ai.tool.metadata.ToolMetadata;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * 规划工厂类
 *
 * @author YangyangMiao
 * @version 1.0
 * @email yangyangmiao666@icloud.com
 * @date 2025/6/1 19:22
 */
@Service
public class PlanningFactory {

    private final ChromeDriverService chromeDriverService;

    private final PlanExecutionRecorder recorder;

    private final ManusProperties manusProperties;

    private final TextFileService textFileService;

    @Autowired
    private AgentService agentService;

    private final McpService mcpService;

    private ConcurrentHashMap<String, PlanningCoordinator> flowMap = new ConcurrentHashMap<>();

    @Autowired
    @Lazy
    private LlmService llmService;

    @Autowired
    @Lazy
    private ToolCallingManager toolCallingManager;

    @Autowired
    private DynamicAgentLoader dynamicAgentLoader;

    @Autowired
    private McpStateHolderService mcpStateHolderService;

    public PlanningFactory(ChromeDriverService chromeDriverService,
                           PlanExecutionRecorder recorder,
                           ManusProperties manusProperties,
                           TextFileService textFileService,
                           McpService mcpService) {
        this.chromeDriverService = chromeDriverService;
        this.recorder = recorder;
        this.manusProperties = manusProperties;
        this.textFileService = textFileService;
        this.mcpService = mcpService;
    }

    // public PlanningCoordinator getOrCreatePlanningFlow(String planId) {
    // PlanningCoordinator flow = flowMap.computeIfAbsent(planId, key -> {
    // return createPlanningCoordinator(planId);
    // });
    // return flow;
    // }

    // public boolean removePlanningFlow(String planId) {
    // return flowMap.remove(planId) != null;
    // }

    public PlanningCoordinator createPlanningCoordinator(String planId) {

        // Add all dynamic agents from the database
        List<DynamicAgentEntity> agentEntities = dynamicAgentLoader.getAllAgents();

        PlanningTool planningTool = new PlanningTool();

        PlanCreator planCreator = new PlanCreator(agentEntities, llmService, planningTool, recorder);
        PlanExecutor planExecutor = new PlanExecutor(agentEntities, llmService, recorder, agentService);
        PlanFinalizer planFinalizer = new PlanFinalizer(llmService, recorder);

        return new PlanningCoordinator(planCreator, planExecutor, planFinalizer);
    }

    public record ToolCallBackContext(ToolCallback toolCallback,
                                      ToolCallBiFunctionDef functionInstance) {
    }

    public Map<String, ToolCallBackContext> toolCallbackMap(String planId) {
        Map<String, ToolCallBackContext> toolCallbackMap = new HashMap<>();
        List<ToolCallBiFunctionDef> toolDefinitions = new ArrayList<>();

        // 添加所有工具定义
        toolDefinitions.add(BrowserUseTool.getInstance(chromeDriverService));
        toolDefinitions.add(new TerminateTool(planId));
        toolDefinitions.add(new Bash(CodeUtils.WORKING_DIR));
        toolDefinitions.add(new DocLoaderTool());
        toolDefinitions.add(new TextFileOperator(CodeUtils.WORKING_DIR, textFileService));
        toolDefinitions.add(new GoogleSearch());
        toolDefinitions.add(new PythonExecute());
        List<McpServiceEntity> functionCallbacks = mcpService.getFunctionCallbacks(planId);
        for (McpServiceEntity toolCallback : functionCallbacks) {
            String serviceGroup = toolCallback.getServiceGroup();
            ToolCallback[] tCallbacks = toolCallback.getAsyncMcpToolCallbackProvider().getToolCallbacks();
            for (ToolCallback tCallback : tCallbacks) {
                // 这里的 serviceGroup 是工具的名称
                toolDefinitions.add(new McpTool(tCallback, serviceGroup, planId, mcpStateHolderService));
            }
        }

        // 为每个工具创建 FunctionToolCallback
        for (ToolCallBiFunctionDef toolDefinition : toolDefinitions) {
            FunctionToolCallback functionToolcallback = FunctionToolCallback
                    .builder(toolDefinition.getName(), toolDefinition)
                    .description(toolDefinition.getDescription())
                    .inputSchema(toolDefinition.getParameters())
                    .inputType(toolDefinition.getInputType())
                    .toolMetadata(ToolMetadata.builder().returnDirect(toolDefinition.isReturnDirect()).build())
                    .build();
            toolDefinition.setPlanId(planId);
            ToolCallBackContext functionToolcallbackContext = new ToolCallBackContext(functionToolcallback,
                    toolDefinition);
            toolCallbackMap.put(toolDefinition.getName(), functionToolcallbackContext);
        }
        return toolCallbackMap;
    }

    @Bean
    public RestClient.Builder createRestClient() {
        // 1. 配置超时时间（单位：毫秒）
        int connectionTimeout = 600000; // 连接超时时间
        int readTimeout = 600000; // 响应读取超时时间
        int writeTimeout = 600000; // 请求写入超时时间

        // 2. 创建 RequestConfig 并设置超时
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(Timeout.of(10, TimeUnit.MINUTES)) // 设置连接超时
                .setResponseTimeout(Timeout.of(10, TimeUnit.MINUTES))
                .setConnectionRequestTimeout(Timeout.of(10, TimeUnit.MINUTES))
                .build();

        // 3. 创建 CloseableHttpClient 并应用配置
        CloseableHttpClient httpClient = HttpClients.custom().setDefaultRequestConfig(requestConfig).build();

        // 4. 使用 HttpComponentsClientHttpRequestFactory 包装 HttpClient
        HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory(httpClient);

        // 5. 创建 RestClient 并设置请求工厂
        return RestClient.builder().requestFactory(requestFactory);
    }

    /**
     * Provides an empty ToolCallbackProvider implementation when MCP is disabled
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(name = "spring.ai.mcp.client.enabled", havingValue = "false")
    public ToolCallbackProvider emptyToolCallbackProvider() {
        return () -> new ToolCallback[0];
    }

}
