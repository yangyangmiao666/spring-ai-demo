package com.ustc.myy.aiagent.manus.dynamic.agent.service;

import com.ustc.myy.aiagent.manus.agent.BaseAgent;
import com.ustc.myy.aiagent.manus.dynamic.agent.DynamicAgent;
import com.ustc.myy.aiagent.manus.dynamic.agent.ToolCallbackProvider;
import com.ustc.myy.aiagent.manus.dynamic.agent.entity.DynamicAgentEntity;
import com.ustc.myy.aiagent.manus.dynamic.agent.model.Tool;
import com.ustc.myy.aiagent.manus.dynamic.agent.repository.DynamicAgentRepository;
import com.ustc.myy.aiagent.manus.llm.LlmService;
import com.ustc.myy.aiagent.manus.planning.PlanningFactory;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.model.tool.ToolCallingManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AgentServiceImpl implements AgentService {

	private static final String DEFAULT_AGENT_NAME = "DEFAULT_AGENT";

	private static final Logger log = LoggerFactory.getLogger(AgentServiceImpl.class);

	private final DynamicAgentLoader dynamicAgentLoader;

	private final DynamicAgentRepository repository;

	private final PlanningFactory planningFactory;

	@Autowired
	@Lazy
	private LlmService llmService;

	@Autowired
	@Lazy
	private ToolCallingManager toolCallingManager;

	@Autowired
	public AgentServiceImpl(@Lazy DynamicAgentLoader dynamicAgentLoader, DynamicAgentRepository repository,
			@Lazy PlanningFactory planningFactory) {
		this.dynamicAgentLoader = dynamicAgentLoader;
		this.repository = repository;
		this.planningFactory = planningFactory;
	}

	@Override
	public List<AgentConfig> getAllAgents() {
		return repository.findAll().stream().map(this::mapToAgentConfig).collect(Collectors.toList());
	}

	@Override
	public AgentConfig getAgentById(String id) {
		DynamicAgentEntity entity = repository.findById(Long.parseLong(id))
			.orElseThrow(() -> new IllegalArgumentException("Agent not found: " + id));
		return mapToAgentConfig(entity);
	}

	@Override
	public AgentConfig createAgent(AgentConfig config) {
		try {
			// 检查是否已存在同名Agent
			DynamicAgentEntity existingAgent = repository.findByAgentName(config.getName());
			if (existingAgent != null) {
				log.info("发现同名Agent: {}，更新Agent", config.getName());
				config.setId(existingAgent.getId().toString());
				return updateAgent(config);
			}

			DynamicAgentEntity entity = new DynamicAgentEntity();
			entity = mergePrompts(entity, config.getName());
			updateEntityFromConfig(entity, config);
			entity = repository.save(entity);
			log.info("成功创建新Agent: {}", config.getName());
			return mapToAgentConfig(entity);
		}
		catch (Exception e) {
			log.warn("创建Agent过程中发生异常: {}，错误信息: {}", config.getName(), e.getMessage());
			// 如果是唯一性约束违反异常，尝试返回已存在的Agent
			if (e.getMessage() != null && e.getMessage().contains("Unique")) {
				DynamicAgentEntity existingAgent = repository.findByAgentName(config.getName());
				if (existingAgent != null) {
					log.info("返回已存在的Agent: {}", config.getName());
					return mapToAgentConfig(existingAgent);
				}
			}
			throw e;
		}
	}

	@Override
	public AgentConfig updateAgent(AgentConfig config) {
		DynamicAgentEntity entity = repository.findById(Long.parseLong(config.getId()))
			.orElseThrow(() -> new IllegalArgumentException("Agent not found: " + config.getId()));
		updateEntityFromConfig(entity, config);
		entity = repository.save(entity);
		return mapToAgentConfig(entity);
	}

	@Override
	public void deleteAgent(String id) {
		DynamicAgentEntity entity = repository.findById(Long.parseLong(id))
			.orElseThrow(() -> new IllegalArgumentException("Agent not found: " + id));

		if (DEFAULT_AGENT_NAME.equals(entity.getAgentName())) {
			throw new IllegalArgumentException("不能删除默认 Agent");
		}

		repository.deleteById(Long.parseLong(id));
	}

	@Override
	public List<Tool> getAvailableTools() {

		Map<String, PlanningFactory.ToolCallBackContext> toolcallContext = planningFactory.toolCallbackMap(null);
		return toolcallContext.entrySet().stream().map(entry -> {
			Tool tool = new Tool();
			tool.setKey(entry.getKey());
			tool.setName(entry.getKey()); // You might want to provide a more friendly
											// name
			tool.setDescription(entry.getValue().functionInstance().getDescription());
			tool.setEnabled(true);
			tool.setServiceGroup(entry.getValue().functionInstance().getServiceGroup());
			return tool;
		}).collect(Collectors.toList());
	}

	private AgentConfig mapToAgentConfig(DynamicAgentEntity entity) {
		AgentConfig config = new AgentConfig();
		entity = mergePrompts(entity, entity.getAgentName());
		config.setId(entity.getId().toString());
		config.setName(entity.getAgentName());
		config.setDescription(entity.getAgentDescription());
		config.setSystemPrompt(entity.getSystemPrompt());
		config.setNextStepPrompt(entity.getNextStepPrompt());
		config.setAvailableTools(entity.getAvailableToolKeys());
		config.setClassName(entity.getClassName());
		return config;
	}

	private void updateEntityFromConfig(DynamicAgentEntity entity, AgentConfig config) {
		entity.setAgentName(config.getName());
		entity.setAgentDescription(config.getDescription());
		String nextStepPrompt = config.getNextStepPrompt();
		entity = mergePrompts(entity, config.getName());
		entity.setNextStepPrompt(nextStepPrompt);

		// 1. 创建新集合，保证唯一性和顺序
		java.util.Set<String> toolSet = new java.util.LinkedHashSet<>();
		List<String> availableTools = config.getAvailableTools();
		if (availableTools != null) {
			toolSet.addAll(availableTools);
		}
		// 2. 添加 TerminateTool（如不存在）
		if (!toolSet.contains(com.ustc.myy.aiagent.manus.tool.TerminateTool.name)) {
			log.info("为Agent[{}]添加必要的工具: {}", config.getName(),
					com.ustc.myy.aiagent.manus.tool.TerminateTool.name);
			toolSet.add(com.ustc.myy.aiagent.manus.tool.TerminateTool.name);
		}
		// 3. 转为 List 并设置
		entity.setAvailableToolKeys(new java.util.ArrayList<>(toolSet));
		entity.setClassName(config.getName());
	}

	private DynamicAgentEntity mergePrompts(DynamicAgentEntity entity, String agentName) {
		// 这里的SystemPrompt属性已经废弃，直接使用nextStepPrompt
		if (StringUtils.isNotBlank(entity.getSystemPrompt())) {
			String systemPrompt = entity.getSystemPrompt();
			String nextPrompt = entity.getNextStepPrompt();
			// 这里的SystemPrompt属性已经废弃，直接使用nextStepPrompt
			if (nextPrompt != null && !nextPrompt.trim().isEmpty()) {
				nextPrompt = systemPrompt + "\n" + nextPrompt;
			}
			log.warn(
					"Agent[{}]的SystemPrompt不为空， 但属性已经废弃，只保留nextPrompt， 本次将agent 的内容合并，如需要该内容在prompt生效，请直接更新界面的唯一的那个prompt , 当前制定的值: {}",
					agentName, nextPrompt);
			entity.setSystemPrompt(" ");
		}
		return entity;
	}

	@Override
	public BaseAgent createDynamicBaseAgent(String name, String planId, Map<String, Object> initialAgentSetting) {

		log.info("创建新的BaseAgent: {}, planId: {}", name, planId);

		try {
			// 通过dynamicAgentLoader加载已存在的Agent
			DynamicAgent agent = dynamicAgentLoader.loadAgent(name, initialAgentSetting);

			// 设置planId
			agent.setPlanId(planId);
			// 设置工具回调映射
			Map<String, PlanningFactory.ToolCallBackContext> toolCallbackMap = planningFactory.toolCallbackMap(planId);
			agent.setToolCallbackProvider(() -> toolCallbackMap);

			log.info("成功加载BaseAgent: {}, 可用工具数量: {}", name, agent.getToolCallList().size());

			return agent;
		}
		catch (Exception e) {
			log.error("加载BaseAgent过程中发生异常: {}, 错误信息: {}", name, e.getMessage(), e);
			throw new RuntimeException("加载BaseAgent失败: " + e.getMessage(), e);
		}
	}

}
