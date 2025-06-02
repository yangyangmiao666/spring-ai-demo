package com.ustc.myy.aiagent.manus.dynamic.mcp.model.vo;

import io.modelcontextprotocol.client.McpAsyncClient;
import lombok.Getter;
import lombok.Setter;
import org.springframework.ai.mcp.AsyncMcpToolCallbackProvider;

@Setter
@Getter
public class McpServiceEntity {

	private McpAsyncClient mcpAsyncClient;

	private AsyncMcpToolCallbackProvider asyncMcpToolCallbackProvider;

	private String serviceGroup;

	/**
	 * 创建一个 McpServiceEntity 实例
	 * @param mcpAsyncClient MCP异步客户端
	 * @param asyncMcpToolCallbackProvider 异步MCP工具回调提供者
	 * @param serviceGroup 服务组
	 */
	public McpServiceEntity(McpAsyncClient mcpAsyncClient, AsyncMcpToolCallbackProvider asyncMcpToolCallbackProvider,
			String serviceGroup) {
		this.mcpAsyncClient = mcpAsyncClient;
		this.asyncMcpToolCallbackProvider = asyncMcpToolCallbackProvider;
		this.serviceGroup = serviceGroup;
	}

}
