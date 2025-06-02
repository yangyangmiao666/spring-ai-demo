package com.ustc.myy.aiagent.manus.planning.finalizer;

import com.ustc.myy.aiagent.manus.llm.LlmService;
import com.ustc.myy.aiagent.manus.planning.model.vo.ExecutionContext;
import com.ustc.myy.aiagent.manus.planning.model.vo.ExecutionPlan;
import com.ustc.myy.aiagent.manus.recorder.PlanExecutionRecorder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 负责生成计划执行总结的类
 *
 * @author YangyangMiao
 * @version 1.0
 * @email yangyangmiao666@icloud.com
 * @date 2025/6/1 22:23
 */
@Slf4j
public class PlanFinalizer {

    private final LlmService llmService;

    protected final PlanExecutionRecorder recorder;

    public PlanFinalizer(LlmService llmService,
                         PlanExecutionRecorder recorder) {
        this.llmService = llmService;
        this.recorder = recorder;
    }

    /**
     * 生成计划执行总结
     *
     * @param context 执行上下文，包含用户请求和执行的过程信息
     */
    public void generateSummary(ExecutionContext context) {
        if (context == null || context.getPlan() == null) {
            throw new IllegalArgumentException("执行上下文或者规划不能为空");
        }
        if (!context.isNeedSummary()) {
            log.info("不需要生成总结，或者请开启生成总结");
            String summary = context.getPlan().getPlanExecutionStateStringFormat(false);
            context.setResultSummary(summary);
            recordPlanCompletion(context, summary);
            return;
        }
        ExecutionPlan plan = context.getPlan();
        String executionDetail = plan.getPlanExecutionStateStringFormat(false);
        try {
            String userRequest = context.getUserRequest();

            SystemPromptTemplate systemPromptTemplate = new SystemPromptTemplate("""
                    您是 JManus，一个能够回应用户请求的AI助手，你需要根据这个分步骤的执行计划的执行结果，来回应用户的请求。
                    
                    分步骤计划的执行详情：
                    {executionDetail}
                    
                    请根据执行详情里面的信息，来回应用户的请求。
                    
                    """);

            Message systemMessage = systemPromptTemplate.createMessage(Map.of("executionDetail", executionDetail));

            String userRequestTemplate = """
                    当前的用户请求是:
                    {userRequest}
                    """;

            PromptTemplate userMessageTemplate = new PromptTemplate(userRequestTemplate);
            Message userMessage = userMessageTemplate.createMessage(Map.of("userRequest", userRequest));

            Prompt prompt = new Prompt(List.of(systemMessage, userMessage));

            ChatResponse response = llmService
                    .getPlanningChatClient()
                    .prompt(prompt)
                    .call()
                    .chatResponse();

            String summary = Objects.requireNonNull(response).getResult().getOutput().getText();
            context.setResultSummary(summary);

            recordPlanCompletion(context, summary);
            log.info("生成总结：{}", summary);
        } catch (Exception e) {
            log.error("生成总结失败：", e);
            throw new RuntimeException("生成总结失败", e);
        } finally {
            llmService.clearConversationMemory(plan.getPlanId());
        }
    }

    /**
     * Record plan completion
     *
     * @param context The execution context
     * @param summary The summary of the plan execution
     */
    private void recordPlanCompletion(ExecutionContext context, String summary) {
        recorder.recordPlanCompletion(context.getPlan().getPlanId(), summary);

        log.info("Plan completed with ID: {} and summary: {}", context.getPlan().getPlanId(), summary);
    }

}
