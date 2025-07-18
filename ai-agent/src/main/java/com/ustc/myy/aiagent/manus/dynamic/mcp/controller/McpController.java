package com.ustc.myy.aiagent.manus.dynamic.mcp.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ustc.myy.aiagent.manus.dynamic.mcp.model.po.McpConfigEntity;
import com.ustc.myy.aiagent.manus.dynamic.mcp.model.vo.McpConfigRequestVO;
import com.ustc.myy.aiagent.manus.dynamic.mcp.model.vo.McpConfigVO;
import com.ustc.myy.aiagent.manus.dynamic.mcp.service.McpService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/mcp")
public class McpController {

	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(McpController.class);

	@Autowired
	private McpService mcpService;

	/**
	 * List All MCP Server
	 * @return All MCP Server as VO objects
	 */
	@GetMapping("/list")
	public ResponseEntity<List<McpConfigVO>> list() {
		List<McpConfigEntity> entities = mcpService.getMcpServers();
		List<McpConfigVO> vos = McpConfigVO.fromEntities(entities);
		return ResponseEntity.ok(vos);
	}

	/**
	 * Add MCP Server
	 * @param requestVO MCP Server Configuration Request VO
	 */
	@PostMapping("/add")
	public ResponseEntity<String> add(@RequestBody McpConfigRequestVO requestVO) throws IOException {
		String configJson = requestVO.getConfigJson();

		// 检查是否是简短格式（没有mcpServers包装）的JSON
		try {
			ObjectMapper objectMapper = new ObjectMapper();
			JsonNode jsonNode = objectMapper.readTree(configJson);

			// 如果不包含mcpServers字段，则需要转换为完整格式
			if (!jsonNode.has("mcpServers")) {
				logger.info("Detected short format JSON, converting to full format");

				// 检查是否是简单的键值对格式（不包含外层大括号）
				if (configJson.trim().startsWith("\"") && configJson.contains(":")) {
					// 简单键值对格式，需要添加外层大括号
					configJson = "{" + configJson + "}";
				}

				// 创建完整的配置格式
				StringBuilder fullJsonBuilder = new StringBuilder();
				fullJsonBuilder.append("{\n  \"mcpServers\": ");
				fullJsonBuilder.append(configJson);
				fullJsonBuilder.append("\n}");

				// 更新requestVO中的configJson
				configJson = fullJsonBuilder.toString();
				requestVO.setConfigJson(configJson);
				logger.info("Converted to full format: {}", configJson);
			}
		}
		catch (Exception e) {
			logger.warn("Error checking JSON format, proceeding with original format", e);
		}

		mcpService.addMcpServer(requestVO);
		return ResponseEntity.ok("success");
	}

	/**
	 * Remove MCP Server
	 * @param id MCP Config ID
	 */
	@GetMapping("/remove")
	public ResponseEntity<String> remove(@RequestParam(name = "id") long id) throws IOException {
		mcpService.removeMcpServer(id);
		return ResponseEntity.ok("Success");
	}

	/**
	 * remove mcp server by name
	 */
	@PostMapping("/remove/{name}")
	public ResponseEntity<String> removeByName(@PathVariable("name") String mcpServerName) throws IOException {
		mcpService.removeMcpServer(mcpServerName);
		return ResponseEntity.ok("Success");
	}

}
