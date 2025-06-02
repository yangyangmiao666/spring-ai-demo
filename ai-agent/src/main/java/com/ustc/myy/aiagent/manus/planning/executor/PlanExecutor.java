package com.ustc.myy.aiagent.manus.planning.executor;

import cn.hutool.core.collection.CollectionUtil;
import com.ustc.myy.aiagent.manus.agent.AgentState;
import com.ustc.myy.aiagent.manus.agent.BaseAgent;
import com.ustc.myy.aiagent.manus.dynamic.agent.entity.DynamicAgentEntity;
import com.ustc.myy.aiagent.manus.dynamic.agent.service.AgentService;
import com.ustc.myy.aiagent.manus.llm.LlmService;
import com.ustc.myy.aiagent.manus.planning.model.vo.ExecutionContext;
import com.ustc.myy.aiagent.manus.planning.model.vo.ExecutionPlan;
import com.ustc.myy.aiagent.manus.planning.model.vo.ExecutionStep;
import com.ustc.myy.aiagent.manus.recorder.PlanExecutionRecorder;
import com.ustc.myy.aiagent.manus.recorder.entity.PlanExecutionRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 负责执行计划的类
 *
 * @author YangyangMiao
 * @version 1.0
 * @email yangyangmiao666@icloud.com
 * @date 2025/6/1 22:23
 */
public class PlanExecutor {

    private static final Logger logger = LoggerFactory.getLogger(PlanExecutor.class);

    protected final PlanExecutionRecorder recorder;

    // 匹配字符串开头的方括号，支持中文和其他字符
    Pattern pattern = Pattern.compile("^\\s*\\[([^]]+)]");

    private final List<DynamicAgentEntity> agents;

    private final AgentService agentService;

    private LlmService llmService;

    // Define static final strings for the keys used in executorParams
    public static final String PLAN_STATUS_KEY = "planStatus";

    public static final String CURRENT_STEP_INDEX_KEY = "currentStepIndex";

    public static final String STEP_TEXT_KEY = "stepText";

    public static final String EXTRA_PARAMS_KEY = "extraParams";

    public static final String EXECUTION_ENV_STRING_KEY = "current_step_env_data";

    public PlanExecutor(List<DynamicAgentEntity> agents,
                        LlmService llmService,
                        PlanExecutionRecorder recorder,
                        AgentService agentService) {
        this.agents = agents;
        this.recorder = recorder;
        this.agentService = agentService;
        this.llmService = llmService;
    }

    /**
     * 执行整个计划的所有步骤
     *
     * @param context 执行上下文，包含用户请求和执行的过程信息
     */
    public void executeAllSteps(ExecutionContext context) {
        BaseAgent executor = null;
        try {
            recordPlanExecutionStart(context);
            ExecutionPlan plan = context.getPlan();
            List<ExecutionStep> steps = plan.getSteps();

            if (CollectionUtil.isNotEmpty(steps)) {
                for (ExecutionStep step : steps) {
                    BaseAgent executorStep = executeStep(step, context);
                    if (executorStep != null) {
                        executor = executorStep;
                    }
                }
            }

            context.setSuccess(true);
        } finally {
            String planId = context.getPlanId();
            llmService.clearAgentMemory(planId);
            if (executor != null) {
                executor.clearUp(planId);
            }
        }
    }

    /**
     * 执行单个步骤
     *
     * @param executor 执行器
     * @param stepInfo 步骤信息
     * @return 步骤执行结果
     */
    private BaseAgent executeStep(ExecutionStep step, ExecutionContext context) {

        try {
            String stepType = getStepFromStepReq(step.getStepRequirement());

            int stepIndex = step.getStepIndex();

            String planStatus = context.getPlan().getPlanExecutionStateStringFormat(true);

            String stepText = step.getStepRequirement();
            Map<String, Object> initSettings = new HashMap<>();
            initSettings.put(PLAN_STATUS_KEY, planStatus);
            initSettings.put(CURRENT_STEP_INDEX_KEY, String.valueOf(stepIndex));
            initSettings.put(STEP_TEXT_KEY, stepText);
            initSettings.put(EXTRA_PARAMS_KEY, context.getPlan().getExecutionParams());
            BaseAgent executor = getExecutorForStep(stepType, context, initSettings);
            if (executor == null) {
                logger.error("步骤类型{}找不到执行者", stepType);
                step.setResult("步骤类型找不到执行者：" + stepType);
                return null;
            }
            step.setAgent(executor);
            executor.setState(AgentState.IN_PROGRESS);

            recordStepStart(step, context);
            String stepResultStr = executor.run();
            // Execute the step
            step.setResult(stepResultStr);
            return executor;
        } catch (Exception e) {
            logger.error("执行步骤失败：{}", e.getMessage(), e);
            step.setResult("执行步骤失败：" + e.getMessage());
        } finally {
            recordStepEnd(step, context);
        }
        return null;
    }

    private String getStepFromStepReq(String stepRequirement) {
        Matcher matcher = pattern.matcher(stepRequirement);
        if (matcher.find()) {
            // 对匹配到的内容进行trim和转小写处理
            return matcher.group(1).trim().toLowerCase();
        }
        return "DEFAULT_AGENT"; // Default agent if no match found
    }

    /**
     * 获取步骤的执行器
     *
     * @param stepType 步骤类型
     * @return 对应的执行器
     */
    private BaseAgent getExecutorForStep(String stepType, ExecutionContext context, Map<String, Object> initSettings) {
        // 根据步骤类型获取对应的执行器
        for (DynamicAgentEntity agent : agents) {
            if (agent.getAgentName().equalsIgnoreCase(stepType)) {
                return agentService.createDynamicBaseAgent(agent.getAgentName(), context.getPlan().getPlanId(),
                        initSettings);
            }
        }
        throw new IllegalArgumentException(
                "该步骤类型没有agent执行者，检查你的agent列表：" + stepType);
    }

    protected PlanExecutionRecorder getRecorder() {
        return recorder;
    }

    private void recordPlanExecutionStart(ExecutionContext context) {
        PlanExecutionRecord record = getOrCreatePlanExecutionRecord(context);

        record.setPlanId(context.getPlan().getPlanId());
        record.setStartTime(LocalDateTime.now());
        record.setTitle(context.getPlan().getTitle());
        record.setUserRequest(context.getUserRequest());
        retrieveExecutionSteps(context, record);
        getRecorder().recordPlanExecution(record);
    }

    private void retrieveExecutionSteps(ExecutionContext context, PlanExecutionRecord record) {
        List<String> steps = new ArrayList<>();
        for (ExecutionStep step : context.getPlan().getSteps()) {
            steps.add(step.getStepInStr());
        }
        record.setSteps(steps);
    }

    /**
     * Initialize the plan execution record
     */
    private PlanExecutionRecord getOrCreatePlanExecutionRecord(ExecutionContext context) {
        PlanExecutionRecord record = getRecorder().getExecutionRecord(context.getPlanId());
        if (record == null) {
            record = new PlanExecutionRecord(context.getPlanId());
        }
        getRecorder().recordPlanExecution(record);
        return record;
    }

    private void recordStepStart(ExecutionStep step, ExecutionContext context) {
        // 更新 PlanExecutionRecord 中的当前步骤索引
        PlanExecutionRecord record = getOrCreatePlanExecutionRecord(context);
        int currentStepIndex = step.getStepIndex();
        record.setCurrentStepIndex(currentStepIndex);
        retrieveExecutionSteps(context, record);
        getRecorder().recordPlanExecution(record);
    }

    /**
     * 记录步骤执行完成
     *
     * @param step    执行的步骤
     * @param context 执行上下文
     */
    private void recordStepEnd(ExecutionStep step, ExecutionContext context) {
        // 更新 PlanExecutionRecord 中的步骤状态
        PlanExecutionRecord record = getOrCreatePlanExecutionRecord(context);
        int currentStepIndex = step.getStepIndex();
        record.setCurrentStepIndex(currentStepIndex);
        // 重新获取所有步骤状态
        retrieveExecutionSteps(context, record);
        getRecorder().recordPlanExecution(record);
    }

}
