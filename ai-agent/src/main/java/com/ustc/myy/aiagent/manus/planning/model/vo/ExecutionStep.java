package com.ustc.myy.aiagent.manus.planning.model.vo;

import com.ustc.myy.aiagent.manus.agent.AgentState;
import com.ustc.myy.aiagent.manus.agent.BaseAgent;
import lombok.Data;

/**
 * 单个步骤的执行结果
 *
 * @author YangyangMiao
 * @version 1.0
 * @email yangyangmiao666@icloud.com
 * @date 2025/6/1 15:04
 */
@Data
public class ExecutionStep {

    private Integer stepIndex;

    private String stepRequirement;

    private String result;

    private BaseAgent agent;

    public AgentState getStatus() {
        return agent == null ? AgentState.NOT_STARTED : agent.getState();
    }

    public String getStepInStr() {
        String agentState;
        if (agent != null) {
            agentState = agent.getState().toString();
        } else {
            agentState = AgentState.NOT_STARTED.toString();
        }
        StringBuilder sb = new StringBuilder();
        sb.append(stepIndex);
        sb.append(". ");
        sb.append("[").append(agentState).append("]");
        sb.append(" ");
        sb.append(stepRequirement);

        return sb.toString();
    }

    /**
     * 将步骤转换为JSON字符串
     *
     * @return 步骤的JSON字符串表示
     */
    public String toJson() {
        StringBuilder json = new StringBuilder();
        json.append("    {");
        json.append("\"stepRequirement\": \"").append(stepRequirement.replace("\"", "\\\"")).append("\" ");

        if (result != null && !result.isEmpty()) {
            json.append(", \"result\": \"").append(result.replace("\"", "\\\"").replace("\n", "\\n")).append("\"");
        }

        json.append("}");
        return json.toString();
    }

    /**
     * 从JsonNode解析并创建ExecutionStep对象
     *
     * @param stepNode JsonNode对象
     * @return 解析后的ExecutionStep对象
     */
    public static ExecutionStep fromJson(com.fasterxml.jackson.databind.JsonNode stepNode) {
        ExecutionStep step = new ExecutionStep();

        // 设置步骤需求
        String stepRequirement = stepNode.has("stepRequirement") ? stepNode.get("stepRequirement").asText() : "未指定步骤";
        step.setStepRequirement(stepRequirement);

        // 设置步骤索引（如果有）
        if (stepNode.has("stepIndex")) {
            step.setStepIndex(stepNode.get("stepIndex").asInt());
        }

        // 设置步骤结果（如果有）
        if (stepNode.has("result")) {
            step.setResult(stepNode.get("result").asText());
        }

        return step;
    }

}
