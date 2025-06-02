package com.ustc.myy.aiagent.manus.dynamic.agent.service;

import com.ustc.myy.aiagent.manus.config.ManusProperties;
import com.ustc.myy.aiagent.manus.dynamic.agent.DynamicAgent;
import com.ustc.myy.aiagent.manus.dynamic.agent.entity.DynamicAgentEntity;
import com.ustc.myy.aiagent.manus.dynamic.agent.repository.DynamicAgentRepository;
import com.ustc.myy.aiagent.manus.llm.LlmService;
import com.ustc.myy.aiagent.manus.recorder.PlanExecutionRecorder;
import org.springframework.ai.model.tool.ToolCallingManager;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class DynamicAgentLoader {

	private final DynamicAgentRepository repository;

	private final LlmService llmService;

	private final PlanExecutionRecorder recorder;

	private final ManusProperties properties;

	private final ToolCallingManager toolCallingManager;

	public DynamicAgentLoader(DynamicAgentRepository repository, @Lazy LlmService llmService,
			PlanExecutionRecorder recorder, ManusProperties properties, @Lazy ToolCallingManager toolCallingManager) {
		this.repository = repository;
		this.llmService = llmService;
		this.recorder = recorder;
		this.properties = properties;
		this.toolCallingManager = toolCallingManager;
	}

	public DynamicAgent loadAgent(String agentName, Map<String, Object> initialAgentSetting) {
		DynamicAgentEntity entity = repository.findByAgentName(agentName);
		if (entity == null) {
			throw new IllegalArgumentException("Agent not found: " + agentName);
		}

		return new DynamicAgent(llmService, recorder, properties, entity.getAgentName(), entity.getAgentDescription(),
				entity.getNextStepPrompt(), entity.getAvailableToolKeys(), toolCallingManager, initialAgentSetting);
	}

	public List<DynamicAgentEntity> getAllAgents() {
		return repository.findAll();
	}

}
