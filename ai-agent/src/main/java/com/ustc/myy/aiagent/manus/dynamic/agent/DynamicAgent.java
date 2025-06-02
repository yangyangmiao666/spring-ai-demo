package com.ustc.myy.aiagent.manus.dynamic.agent;

import com.ustc.myy.aiagent.manus.agent.AgentState;
import com.ustc.myy.aiagent.manus.agent.ReActAgent;
import com.ustc.myy.aiagent.manus.config.ManusProperties;
import com.ustc.myy.aiagent.manus.llm.LlmService;
import com.ustc.myy.aiagent.manus.planning.PlanningFactory;
import com.ustc.myy.aiagent.manus.planning.executor.PlanExecutor;
import com.ustc.myy.aiagent.manus.recorder.PlanExecutionRecorder;
import com.ustc.myy.aiagent.manus.recorder.entity.AgentExecutionRecord;
import com.ustc.myy.aiagent.manus.recorder.entity.ThinkActRecord;
import com.ustc.myy.aiagent.manus.tool.TerminateTool;
import io.micrometer.common.util.StringUtils;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage.ToolCall;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.ToolResponseMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.ai.model.tool.ToolCallingChatOptions;
import org.springframework.ai.model.tool.ToolCallingManager;
import org.springframework.ai.model.tool.ToolExecutionResult;
import org.springframework.ai.tool.ToolCallback;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static org.springframework.ai.chat.memory.ChatMemory.CONVERSATION_ID;

@Slf4j
public class DynamicAgent extends ReActAgent {

	private final String agentName;

	private final String agentDescription;

	private final String nextStepPrompt;

	@Setter
    private ToolCallbackProvider toolCallbackProvider;

	private final List<String> availableToolKeys;

	private ChatResponse response;

	private Prompt userPrompt;

	protected ThinkActRecord thinkActRecord;

	private final ToolCallingManager toolCallingManager;

	public void clearUp(String planId) {
		Map<String, PlanningFactory.ToolCallBackContext> toolCallBackContext = toolCallbackProvider.getToolCallBackContext();
		for (PlanningFactory.ToolCallBackContext toolCallBack : toolCallBackContext.values()) {
			try {
				toolCallBack.functionInstance().cleanup(planId);
			}
			catch (Exception e) {
				log.error("Error cleaning up tool callback context: {}", e.getMessage(), e);
			}
		}
	}

	public DynamicAgent(LlmService llmService, PlanExecutionRecorder planExecutionRecorder,
						ManusProperties manusProperties, String name, String description, String nextStepPrompt,
						List<String> availableToolKeys, ToolCallingManager toolCallingManager,
						Map<String, Object> initialAgentSetting) {
		super(llmService, planExecutionRecorder, manusProperties, initialAgentSetting);
		this.agentName = name;
		this.agentDescription = description;
		this.nextStepPrompt = nextStepPrompt;
		this.availableToolKeys = availableToolKeys;
		this.toolCallingManager = toolCallingManager;
	}

	@Override
	protected boolean think() {
		collectAndSetEnvDataForTools();

		AgentExecutionRecord planExecutionRecord = planExecutionRecorder.getCurrentAgentExecutionRecord(getPlanId());
		thinkActRecord = new ThinkActRecord(planExecutionRecord.getId());
		thinkActRecord.setActStartTime(LocalDateTime.now());
		planExecutionRecorder.recordThinkActExecution(getPlanId(), planExecutionRecord.getId(), thinkActRecord);

		try {
			return executeWithRetry();
		}
		catch (Exception e) {
			log.error(String.format("🚨 Oops! The %s's thinking process hit a snag: %s", getName(), e.getMessage()), e);
			thinkActRecord.recordError(e.getMessage());
			return false;
		}
	}

	private boolean executeWithRetry() {
		int attempt = 0;
		while (attempt < 3) {
			attempt++;
			List<Message> messages = new ArrayList<>();
			addThinkPrompt(messages);

			ChatOptions chatOptions = ToolCallingChatOptions.builder().internalToolExecutionEnabled(false).build();
			Message nextStepMessage = getNextStepWithEnvMessage();
			messages.add(nextStepMessage);
			thinkActRecord.startThinking(messages.toString());

			log.debug("Messages prepared for the prompt: {}", messages);

			userPrompt = new Prompt(messages, chatOptions);

			List<ToolCallback> callbacks = getToolCallList();
			ChatClient chatClient = llmService.getAgentChatClient();
			response = chatClient.prompt(userPrompt)
				.advisors(memoryAdvisor -> memoryAdvisor.param(CONVERSATION_ID, getPlanId()))
				.toolCallbacks(callbacks)
				.call()
				.chatResponse();

			List<ToolCall> toolCalls = Objects.requireNonNull(response).getResult().getOutput().getToolCalls();
			String responseByLLm = response.getResult().getOutput().getText();

			thinkActRecord.finishThinking(responseByLLm);

			log.info(String.format("✨ %s's thoughts: %s", getName(), responseByLLm));
			log.info(String.format("🛠️ %s selected %d tools to use", getName(), toolCalls.size()));

			if (!toolCalls.isEmpty()) {
				log.info(String.format("🧰 Tools being prepared: %s",
						toolCalls.stream().map(ToolCall::name).collect(Collectors.toList())));
				thinkActRecord.setActionNeeded(true);
				thinkActRecord.setToolName(toolCalls.getFirst().name());
				thinkActRecord.setToolParameters(toolCalls.getFirst().arguments());
				thinkActRecord.setStatus("SUCCESS");
				return true;
			}

			log.warn("Attempt {}: No tools selected. Retrying...", attempt);
		}

		thinkActRecord.setStatus("FAILED");
		return false;
	}

	@Override
	protected AgentExecResult act() {
		try {
			List<ToolCall> toolCalls = response.getResult().getOutput().getToolCalls();
			ToolCall toolCall = toolCalls.getFirst();

			thinkActRecord.startAction("Executing tool: " + toolCall.name(), toolCall.name(), toolCall.arguments());
			ToolExecutionResult toolExecutionResult = toolCallingManager.executeToolCalls(userPrompt, response);

			// setData(getData());
			ToolResponseMessage toolResponseMessage = (ToolResponseMessage) toolExecutionResult.conversationHistory().getLast();

			llmService.getAgentMemory().add(getPlanId(), toolResponseMessage);
			String llmCallResponse = toolResponseMessage.getResponses().getFirst().responseData();

			log.info(String.format("🔧 Tool %s's executing result: %s", getName(), llmCallResponse));

			thinkActRecord.finishAction(llmCallResponse, "SUCCESS");
			String toolcallName = toolCall.name();
			AgentExecResult agentExecResult;
			// 如果是终止工具，则返回完成状态
			// 否则返回运行状态
			if (TerminateTool.name.equals(toolcallName)) {
				agentExecResult = new AgentExecResult(llmCallResponse, AgentState.COMPLETED);
			}
			else {
				agentExecResult = new AgentExecResult(llmCallResponse, AgentState.IN_PROGRESS);
			}
			return agentExecResult;
		}
		catch (Exception e) {
			ToolCall toolCall = response.getResult().getOutput().getToolCalls().getFirst();
			ToolResponseMessage.ToolResponse toolResponse = new ToolResponseMessage.ToolResponse(toolCall.id(),
					toolCall.name(), "Error: " + e.getMessage());
			ToolResponseMessage toolResponseMessage = new ToolResponseMessage(List.of(toolResponse), Map.of());
			llmService.getAgentMemory().add(getPlanId(), toolResponseMessage);
			log.error(e.getMessage());

			thinkActRecord.recordError(e.getMessage());

			return new AgentExecResult(e.getMessage(), AgentState.FAILED);
		}
	}

	@Override
	public String getName() {
		return this.agentName;
	}

	@Override
	public String getDescription() {
		return this.agentDescription;
	}

	@Override
	protected Message getNextStepWithEnvMessage() {
		if (StringUtils.isBlank(this.nextStepPrompt)) {
			return new UserMessage("");
		}
		PromptTemplate promptTemplate = new PromptTemplate(this.nextStepPrompt);
		return promptTemplate.createMessage(getMergedData());
	}

	private Map<String, Object> getMergedData() {
        Map<String, Object> data = new HashMap<>(getInitSettingData());
		data.put(PlanExecutor.EXECUTION_ENV_STRING_KEY, convertEnvDataToString());
		return data;
	}

	@Override
	protected Message addThinkPrompt(List<Message> messages) {
		super.addThinkPrompt(messages);
		String envPrompt = """

				当前步骤的环境信息是:
				{current_step_env_data}

				""";

		SystemPromptTemplate promptTemplate = new SystemPromptTemplate(envPrompt);
		Message systemMessage = promptTemplate.createMessage(getMergedData());
		messages.add(systemMessage);
		return systemMessage;
	}

	@Override
	public List<ToolCallback> getToolCallList() {
		List<ToolCallback> toolCallbacks = new ArrayList<>();
		Map<String, PlanningFactory.ToolCallBackContext> toolCallBackContext = toolCallbackProvider.getToolCallBackContext();
		for (String toolKey : availableToolKeys) {
			if (toolCallBackContext.containsKey(toolKey)) {
				PlanningFactory.ToolCallBackContext toolCallback = toolCallBackContext.get(toolKey);
				if (toolCallback != null) {
					toolCallbacks.add(toolCallback.toolCallback());
				}
			}
			else {
				log.warn("Tool callback for {} not found in the map.", toolKey);
			}
		}
		return toolCallbacks;
	}

	public void addEnvData(String key, String value) {
		Map<String, Object> data = new HashMap<>(super.getInitSettingData());
        data.put(key, value);
	}

    protected String collectEnvData(String toolCallName) {
		PlanningFactory.ToolCallBackContext context = toolCallbackProvider.getToolCallBackContext().get(toolCallName);
		if (context != null) {
			return context.functionInstance().getCurrentToolStateString();
		}
		// 如果没有找到对应的工具回调上下文，返回空字符串
		return "";
	}

	public void collectAndSetEnvDataForTools() {

        Map<String, Object> oldMap = getEnvData();
        Map<String, Object> toolEnvDataMap = new HashMap<>(oldMap);

		// 用新数据覆盖旧数据
		for (String toolKey : availableToolKeys) {
			String envData = collectEnvData(toolKey);
			toolEnvDataMap.put(toolKey, envData);
		}
		log.debug("收集到的工具环境数据: {}", toolEnvDataMap);

		setEnvData(toolEnvDataMap);
	}

	public String convertEnvDataToString() {
		StringBuilder envDataStringBuilder = new StringBuilder();

		for (String toolKey : availableToolKeys) {
			Object value = getEnvData().get(toolKey);
			if (value == null || value.toString().isEmpty()) {
				continue; // Skip tools with no data
			}
			envDataStringBuilder.append(toolKey).append(" 的上下文信息：\n");
			envDataStringBuilder.append("    ").append(value.toString()).append("\n");
		}

		return envDataStringBuilder.toString();
	}

}
