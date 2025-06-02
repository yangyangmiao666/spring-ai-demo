package com.ustc.myy.aiagent.manus.agent;

import com.ustc.myy.aiagent.manus.config.ManusProperties;
import com.ustc.myy.aiagent.manus.llm.LlmService;
import com.ustc.myy.aiagent.manus.recorder.PlanExecutionRecorder;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;


/**
 * ReAct（Reasoning + Acting）模式的智能体基类 实现了思考(Reasoning)和行动(Acting)交替执行的智能体模式
 *
 * @author YangyangMiao
 * @version 1.0
 * @email yangyangmiao666@icloud.com
 * @date 2025/6/1 02:35
 */
@Slf4j
public abstract class ReActAgent extends BaseAgent {

    /**
     * 构造函数
     *
     * @param llmService            LLM服务实例，用于处理自然语言交互
     * @param planExecutionRecorder 计划执行记录器，用于记录执行过程
     * @param manusProperties       Manus配置属性
     */
    public ReActAgent(LlmService llmService,
                      PlanExecutionRecorder planExecutionRecorder,
                      ManusProperties manusProperties,
                      Map<String, Object> initialAgentSetting) {
        super(llmService, planExecutionRecorder, manusProperties, initialAgentSetting);
    }

    /**
     * 执行思考过程，判断是否需要采取行动
     * <p>
     * 子类实现要求： 1. 分析当前状态和上下文 2. 进行逻辑推理，得出下一步行动的决策 3. 返回是否需要执行行动
     * <p>
     * 示例实现： - 如果需要调用工具，返回true - 如果当前步骤已完成，返回false
     *
     * @return true表示需要执行行动，false表示当前不需要行动
     */
    protected abstract boolean think();

    /**
     * 执行具体的行动
     * <p>
     * 子类实现要求： 1. 基于think()的决策执行具体操作 2. 可以是工具调用、状态更新等具体行为 3. 返回执行结果的描述信息
     * <p>
     * 示例实现： - ToolCallAgent：执行选定的工具调用 - BrowserAgent：执行浏览器操作
     *
     * @return 行动执行的结果描述
     */
    protected abstract AgentExecResult act();

    /**
     * 执行一个完整的思考-行动步骤
     *
     * @return 如果不需要行动则返回思考完成的消息，否则返回行动的执行结果
     */
    @Override
    public AgentExecResult step() {

        boolean shouldAct = think();
        if (!shouldAct) {
            AgentExecResult result = new AgentExecResult("Thinking complete - no action needed",
                    AgentState.IN_PROGRESS);

            return result;
        }
        return act();
    }

}
