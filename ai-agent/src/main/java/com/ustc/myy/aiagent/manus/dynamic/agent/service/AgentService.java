package com.ustc.myy.aiagent.manus.dynamic.agent.service;

import com.ustc.myy.aiagent.manus.agent.BaseAgent;
import com.ustc.myy.aiagent.manus.dynamic.agent.model.Tool;

import java.util.List;
import java.util.Map;

public interface AgentService {

	List<AgentConfig> getAllAgents();

	AgentConfig getAgentById(String id);

	AgentConfig createAgent(AgentConfig agentConfig);

	AgentConfig updateAgent(AgentConfig agentConfig);

	void deleteAgent(String id);

	List<Tool> getAvailableTools();

	/**
	 * 创建并返回一个可用的BaseAgent对象 类似于PlanningFactory中的createPlanningCoordinator方法
	 * @param name 代理名称
	 * @param planId 计划ID，用于标识代理所属的计划
	 * @return 创建的BaseAgent对象
	 */
	BaseAgent createDynamicBaseAgent(String name, String planId, Map<String, Object> initialAgentSetting);

}
