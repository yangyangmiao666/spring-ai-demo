package com.ustc.myy.aiagent.manus.controller;

import com.ustc.myy.aiagent.manus.planning.PlanningFactory;
import com.ustc.myy.aiagent.manus.planning.coordinator.PlanIdDispatcher;
import com.ustc.myy.aiagent.manus.planning.coordinator.PlanningCoordinator;
import com.ustc.myy.aiagent.manus.planning.model.vo.ExecutionContext;
import com.ustc.myy.aiagent.manus.recorder.PlanExecutionRecorder;
import com.ustc.myy.aiagent.manus.recorder.entity.PlanExecutionRecord;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * ManusController
 *
 * @author YangyangMiao
 * @version 1.0
 * @email yangyangmiao666@icloud.com
 * @date 2025/6/1 14:04
 */
@RestController
@RequestMapping("/api/executor")
@Slf4j
public class ManusController {

    private final PlanningFactory planningFactory;

    private final PlanExecutionRecorder planExecutionRecorder;

    private final PlanIdDispatcher planIdDispatcher;

    public ManusController(@Lazy PlanningFactory planningFactory,
                           PlanExecutionRecorder planExecutionRecorder,
                           PlanIdDispatcher planIdDispatcher) {
        this.planningFactory = planningFactory;
        this.planExecutionRecorder = planExecutionRecorder;
        this.planIdDispatcher = planIdDispatcher;
    }

    /**
     * 异步执行 Manus 请求
     *
     * @param request 包含用户查询的请求
     * @return 任务ID及状态
     */
    @PostMapping("/execute")
    public ResponseEntity<Map<String, Object>> executeQuery(@RequestBody Map<String, String> request) {
        String query = request.get("query");
        if (query == null || query.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "查询内容不能为空"));
        }
        ExecutionContext context = new ExecutionContext();
        context.setUserRequest(query);
        // 使用 PlanIdDispatcher 生成唯一的计划ID
        String planId = planIdDispatcher.generatePlanId();
        context.setPlanId(planId);
        context.setNeedSummary(true);
        // 获取或创建规划流程
        PlanningCoordinator planningFlow = planningFactory.createPlanningCoordinator(planId);

        // 异步执行任务
        CompletableFuture.supplyAsync(() -> {
            try {
                return planningFlow.executePlan(context);
            } catch (Exception e) {
                log.error("执行计划失败", e);
                throw new RuntimeException("执行计划失败: " + e.getMessage(), e);
            }
        });

        // 返回任务ID及初始状态
        Map<String, Object> response = new HashMap<>();
        response.put("planId", planId);
        response.put("status", "processing");
        response.put("message", "任务已提交，正在处理中");

        return ResponseEntity.ok(response);
    }

    /**
     * 获取详细的执行记录
     *
     * @param planId 计划ID
     * @return 执行记录的 JSON 表示
     */
    @GetMapping("/details/{planId}")
    public synchronized ResponseEntity<String> getExecutionDetails(@PathVariable("planId") String planId) {
        PlanExecutionRecord planRecord = planExecutionRecorder.getExecutionRecord(planId);

        if (planRecord == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(planRecord.toJson());
    }

    /**
     * 删除指定计划ID的执行记录
     *
     * @param planId 计划ID
     * @return 删除操作的结果
     */
    @DeleteMapping("/details/{planId}")
    public ResponseEntity<Map<String, String>> removeExecutionDetails(@PathVariable("planId") String planId) {
        PlanExecutionRecord planRecord = planExecutionRecorder.getExecutionRecord(planId);
        if (planRecord == null) {
            return ResponseEntity.notFound().build();
        }

        try {
            planExecutionRecorder.removeExecutionRecord(planId);
            return ResponseEntity.ok(Map.of("message", "执行记录已成功删除", "planId", planId));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", "删除记录失败: " + e.getMessage()));
        }
    }

}
