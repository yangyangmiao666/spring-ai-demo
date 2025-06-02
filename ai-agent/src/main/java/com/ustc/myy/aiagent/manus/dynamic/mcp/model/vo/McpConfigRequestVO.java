package com.ustc.myy.aiagent.manus.dynamic.mcp.model.vo;

import lombok.Getter;
import lombok.Setter;

/**
 * MCP配置请求值对象，用于接收前端传递的配置信息
 */
@Setter
@Getter
public class McpConfigRequestVO {

	/**
	 * 连接类型：STUDIO, STREAMING, SSE
	 */
	private String connectionType;

	/**
	 * MCP服务器配置的JSON字符串
	 */
	private String configJson;

}
