package com.ustc.myy.aiagent.manus.planning.coordinator;


import com.ustc.myy.aiagent.manus.planning.creator.PlanCreator;
import com.ustc.myy.aiagent.manus.planning.executor.PlanExecutor;
import com.ustc.myy.aiagent.manus.planning.finalizer.PlanFinalizer;
import com.ustc.myy.aiagent.manus.planning.model.vo.ExecutionContext;

/**
 * 计划流程的总协调器 负责协调计划的创建、执行和总结三个主要步骤
 *
 * @author YangyangMiao
 * @version 1.0
 * @email yangyangmiao666@icloud.com
 * @date 2025/6/1 21:25
 */
public class PlanningCoordinator {

    private final PlanCreator planCreator;

    private final PlanExecutor planExecutor;

    private final PlanFinalizer planFinalizer;

    public PlanningCoordinator(PlanCreator planCreator,
                               PlanExecutor planExecutor,
                               PlanFinalizer planFinalizer) {
        this.planCreator = planCreator;
        this.planExecutor = planExecutor;
        this.planFinalizer = planFinalizer;
    }

    /**
     * 仅创建计划，不执行
     *
     * @param context 执行上下文
     * @return 执行上下文
     */
    public ExecutionContext createPlan(ExecutionContext context) {
        // 只执行创建计划步骤
        context.setUseMemory(false);
        planCreator.createPlan(context);
        return context;
    }

    /**
     * 执行完整的规划流程
     *
     * @param context 执行上下文
     * @return 执行总结
     */
    public ExecutionContext executePlan(ExecutionContext context) {
        context.setUseMemory(true);
        // 1. 创建规划
        planCreator.createPlan(context);

        // 2. 执行规划
        planExecutor.executeAllSteps(context);

        // 3. 生成总结
        planFinalizer.generateSummary(context);

        return context;
    }

    /**
     * 执行已有计划（跳过创建计划步骤）
     *
     * @param context 包含现有计划的执行上下文
     * @return 执行总结
     */
    public ExecutionContext executeExistingPlan(ExecutionContext context) {
        // 1. 执行计划
        planExecutor.executeAllSteps(context);

        // 2. 生成总结
        planFinalizer.generateSummary(context);

        return context;
    }

}
