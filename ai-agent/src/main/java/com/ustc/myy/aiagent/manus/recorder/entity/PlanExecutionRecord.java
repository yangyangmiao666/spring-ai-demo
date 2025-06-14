package com.ustc.myy.aiagent.manus.recorder.entity;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 规划执行记录类，用于跟踪和记录PlanningFlow执行过程的详细信息。
 * <p>
 * 数据结构分为四个主要部分：
 * <p>
 * 1. 基本信息 (Basic Info) - id: 记录的唯一标识 - planId: 计划的唯一标识 - title: 计划标题 - startTime: 执行开始时间
 * - endTime: 执行结束时间 - userRequest: 用户的原始请求
 * <p>
 * 2. 计划结构 (Plan Structure) - steps: 计划步骤列表 - stepStatuses: 步骤状态列表 - stepNotes: 步骤备注列表 -
 * stepAgents: 与每个步骤关联的智能体
 * <p>
 * 3. 执行过程数据 (Execution Data) - currentStepIndex: 当前执行的步骤索引 - agentExecutionRecords:
 * 每个智能体执行的记录 - executorKeys: 执行者键列表 - resultState: 共享结果状态
 * <p>
 * 4. 执行结果 (Execution Result) - completed: 是否完成 - progress: 执行进度（百分比） - summary: 执行总结
 */
@Setter
@Getter
public class PlanExecutionRecord implements JsonSerializable {

	// 记录的唯一标识符
	private Long id;

	// 计划的唯一标识符
	private String planId;

	// 计划标题
	private String title;

	// 用户的原始请求
	private String userRequest;

	// 执行开始的时间戳
	private LocalDateTime startTime;

	// 执行结束的时间戳
	private LocalDateTime endTime;

	// 计划的步骤列表
	private List<String> steps;

	// 当前执行的步骤索引
	private Integer currentStepIndex;

	// 是否完成
	private boolean completed;

	// 执行总结
	private String summary;

    /**
     * -- GETTER --
     *  获取按执行顺序排列的智能体执行记录列表
     */
    // List to maintain the sequence of agent executions
	private List<AgentExecutionRecord> agentExecutionSequence;

	/**
	 * 默认构造函数
	 */
	public PlanExecutionRecord(String planId) {
		this.planId = planId;
		this.steps = new ArrayList<>();
		this.startTime = LocalDateTime.now();
		this.completed = false;
		this.agentExecutionSequence = new ArrayList<>();
	}

	/**
	 * 添加一个执行步骤
	 * @param step 步骤描述
	 * @param agentName 执行智能体名称
	 */
	public void addStep(String step, String agentName) {
		this.steps.add(step);
	}

	/**
	 * 添加智能体执行记录
	 * @param record 执行记录
	 */
	public void addAgentExecutionRecord(AgentExecutionRecord record) {
		this.agentExecutionSequence.add(record);
	}

    /**
	 * 完成执行，设置结束时间
	 */
	public void complete(String summary) {
		this.endTime = LocalDateTime.now();
		this.completed = true;
		this.summary = summary;
	}

	/**
	 * 保存记录到持久化存储 空实现，由具体的存储实现来覆盖 同时会递归保存所有AgentExecutionRecord
	 * @return 保存后的记录ID
	 */
	public Long save() {
		// 如果ID为空，生成一个随机ID
		if (this.id == null) {
			// 使用时间戳和随机数组合生成ID
			long timestamp = System.currentTimeMillis();
			int random = (int) (Math.random() * 1000000);
			this.id = timestamp * 1000 + random;
		}

		// Save all AgentExecutionRecords
		if (agentExecutionSequence != null) {
			for (AgentExecutionRecord agentRecord : agentExecutionSequence) {
				agentRecord.save();
			}
		}
		return this.id;
	}

	// Getters and Setters

    /**
	 * 返回此记录的字符串表示形式，包含关键字段信息
	 * @return 包含记录关键信息的字符串
	 */
	@Override
	public String toString() {
		return String.format(
				"PlanExecutionRecord{id=%d, planId='%s', title='%s', steps=%d, currentStep=%d/%d, completed=%b}", id,
				planId, title, steps.size(), currentStepIndex != null ? currentStepIndex + 1 : 0, steps.size(),
				completed);
	}

	/**
	 * 将记录转换为JSON格式的字符串 包含所有关键字段，包括： - 基本信息（id, planId, title等） - 时间信息（startTime, endTime）
	 * - 执行状态（currentStepIndex, completed等） - 步骤信息（steps） -
	 * 智能体执行记录（agentExecutionSequence）
	 * @return JSON格式的字符串
	 */
	@Override
	public String toJson() {
		StringBuilder json = new StringBuilder();
		json.append("{");

		// 基本信息
		appendField(json, "id", id, true);
		appendField(json, "planId", planId, true);
		appendField(json, "title", title, true);
		appendField(json, "userRequest", userRequest, true);

		// 时间信息
		if (startTime != null) {
			appendField(json, "startTime", startTime.toString(), true);
		}
		if (endTime != null) {
			appendField(json, "endTime", endTime.toString(), true);
		}

		// 执行状态
		appendField(json, "currentStepIndex", currentStepIndex, false);
		appendField(json, "completed", completed, false);
		appendField(json, "summary", summary, true);

		// 步骤信息
		if (steps != null && !steps.isEmpty()) {
			json.append("\"steps\":[");
			for (int i = 0; i < steps.size(); i++) {
				if (i > 0)
					json.append(",");
				json.append("\"").append(escapeJson(steps.get(i))).append("\"");
			}
			json.append("],");
		}

		// 智能体执行记录
		if (agentExecutionSequence != null && !agentExecutionSequence.isEmpty()) {
			json.append("\"agentExecutionSequence\":[");
			for (int i = 0; i < agentExecutionSequence.size(); i++) {
				if (i > 0)
					json.append(",");
				json.append(agentExecutionSequence.get(i).toJson());
			}
			json.append("],");
		}

		// 移除末尾多余的逗号
		if (json.charAt(json.length() - 1) == ',') {
			json.setLength(json.length() - 1);
		}

		json.append("}");
		return json.toString();
	}

}
