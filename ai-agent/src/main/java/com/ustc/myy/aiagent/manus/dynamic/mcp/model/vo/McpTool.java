package com.ustc.myy.aiagent.manus.dynamic.mcp.model.vo;

import com.ustc.myy.aiagent.manus.dynamic.mcp.service.McpStateHolderService;
import com.ustc.myy.aiagent.manus.tool.ToolCallBiFunctionDef;
import com.ustc.myy.aiagent.manus.tool.code.ToolExecuteResult;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.ToolCallback;

public class McpTool implements ToolCallBiFunctionDef {

	private final ToolCallback toolCallback;

	private String planId;

	private String serviceNameString;

	private McpStateHolderService mcpStateHolderService;

	public McpTool(ToolCallback toolCallback, String serviceNameString, String planId,
			McpStateHolderService mcpStateHolderService) {
		this.toolCallback = toolCallback;
		this.serviceNameString = serviceNameString;
		this.planId = planId;
		this.mcpStateHolderService = mcpStateHolderService;
	}

	@Override
	public String getName() {
		return toolCallback.getToolDefinition().name();
	}

	@Override
	public String getDescription() {
		return toolCallback.getToolDefinition().description();
	}

	@Override
	public String getParameters() {
		return toolCallback.getToolDefinition().inputSchema();
	}

	@Override
	public Class<?> getInputType() {
		return String.class;
	}

	@Override
	public boolean isReturnDirect() {
		return false;
	}

	@Override
	public void setPlanId(String planId) {
		this.planId = planId;
	}

	@Override
	public String getCurrentToolStateString() {
		McpState mcpState = mcpStateHolderService.getMcpState(planId);
		if (mcpState != null) {
			return mcpState.getState();
		}
		return "";
	}

	@Override
	public ToolExecuteResult apply(String s, ToolContext toolContext) {
		String result = toolCallback.call(s, toolContext);
        // 这里可以将结果存储到McpStateHolderService中
		McpState mcpState = mcpStateHolderService.getMcpState(planId);
		if (mcpState == null) {
			mcpState = new McpState();
			mcpStateHolderService.setMcpState(planId, mcpState);
		}
		mcpState.setState(result);

		return new ToolExecuteResult(result);
	}

	@Override
	public void cleanup(String planId) {
		mcpStateHolderService.removeMcpState(planId);
	}

	@Override
	public String getServiceGroup() {
		return serviceNameString;
	}

}
