package com.ustc.myy.aiagent.manus.dynamic.mcp.service;

import com.ustc.myy.aiagent.manus.dynamic.mcp.model.vo.McpState;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class McpStateHolderService {

	private Map<String, McpState> mcpStateMap = new ConcurrentHashMap<>();

	public McpState getMcpState(String key) {
		return mcpStateMap.get(key);
	}

	public void setMcpState(String key, McpState state) {
		mcpStateMap.put(key, state);
	}

	public void removeMcpState(String key) {
		mcpStateMap.remove(key);
	}

}
