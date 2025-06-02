package com.ustc.myy.aiagent.manus.planning.model.vo;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;


/**
 * 执行上下文类，用于在计划的创建、执行和总结过程中传递和维护状态信息。 该类作为计划执行流程中的核心数据载体，在
 * {@link com.ustc.myy.aiagent.manus.planning.coordinator.PlanningCoordinator}
 * 的各个阶段之间传递。
 * <p>
 * 主要职责： - 存储计划ID和计划实体信息 - 保存用户原始请求 - 维护计划执行状态 - 存储执行结果摘要 - 控制是否需要生成执行总结
 *
 * @author YangyangMiao
 * @version 1.0
 * @email yangyangmiao666@icloud.com
 * @date 2025/6/1 14:55
 * @see com.ustc.myy.aiagent.manus.planning.model.vo.ExecutionPlan
 * @see com.ustc.myy.aiagent.manus.planning.coordinator.PlanningCoordinator
 */
@Data
public class ExecutionContext {

    /**
     * 工具上下文，存储工具执行的上下文信息
     */
    private Map<String, String> toolsContext = new HashMap<>();

    /**
     * 计划的唯一标识符
     */
    private String planId;

    /**
     * 执行计划实体，包含计划的详细信息和执行步骤
     */
    private ExecutionPlan plan;

    /**
     * 用户的原始请求内容
     */
    private String userRequest;

    /**
     * 计划执行完成后的结果摘要
     */
    private String resultSummary;

    /**
     * 是否需要为执行结果调用大模型生成摘要，true是调用大模型，false是不调用直接输出结果
     */
    private boolean needSummary;

    /**
     * 计划执行是否成功的标志
     */
    private boolean success = false;

    /**
     * 是否使用记忆， 场景是 如果只构建计划，那么不应该用记忆，否则记忆无法删除
     */
    private boolean useMemory = false;

    public void addToolContext(String toolsKey, String value) {
        this.toolsContext.put(toolsKey, value);
    }


    /**
     * 使用另一个ExecutionContext实例的内容更新当前实例
     * <p>
     * 此方法会复制传入context的计划实体、用户请求和结果摘要到当前实例
     *
     * @param context 源执行上下文实例
     */
    public void updateContext(ExecutionContext context) {
        this.plan = context.getPlan();
        this.userRequest = context.getUserRequest();
        this.resultSummary = context.getResultSummary();
    }
}
