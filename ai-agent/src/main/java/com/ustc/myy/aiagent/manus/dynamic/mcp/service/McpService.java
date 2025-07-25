package com.ustc.myy.aiagent.manus.dynamic.mcp.service;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.RemovalListener;
import com.ustc.myy.aiagent.manus.dynamic.mcp.model.po.McpConfigEntity;
import com.ustc.myy.aiagent.manus.dynamic.mcp.model.po.McpConfigType;
import com.ustc.myy.aiagent.manus.dynamic.mcp.model.vo.McpConfigRequestVO;
import com.ustc.myy.aiagent.manus.dynamic.mcp.model.vo.McpServerConfig;
import com.ustc.myy.aiagent.manus.dynamic.mcp.model.vo.McpServersConfig;
import com.ustc.myy.aiagent.manus.dynamic.mcp.model.vo.McpServiceEntity;
import com.ustc.myy.aiagent.manus.dynamic.mcp.repository.McpConfigRepository;
import io.modelcontextprotocol.client.McpAsyncClient;
import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.transport.ServerParameters;
import io.modelcontextprotocol.client.transport.StdioClientTransport;
import io.modelcontextprotocol.client.transport.WebFluxSseClientTransport;
import io.modelcontextprotocol.spec.McpClientTransport;
import io.modelcontextprotocol.spec.McpSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.mcp.AsyncMcpToolCallbackProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Component
public class McpService {

	private static final Logger logger = LoggerFactory.getLogger(McpService.class);

	@Autowired
	private McpConfigRepository mcpConfigRepository;

	private final LoadingCache<String, Map<String, McpServiceEntity>> toolCallbackMapCache = CacheBuilder.newBuilder()
		.expireAfterAccess(10, TimeUnit.MINUTES)
		.removalListener((RemovalListener<String, Map<String, McpServiceEntity>>) notification -> {
			Map<String, McpServiceEntity> mcpServiceEntityMap = notification.getValue();
			if (mcpServiceEntityMap == null) {
				return;
			}
			for (McpServiceEntity mcpServiceEntity : mcpServiceEntityMap.values()) {
				try {
					mcpServiceEntity.getMcpAsyncClient().close();
				}
				catch (Throwable t) {
					logger.error("Failed to close MCP client", t);
				}
			}
		})
		.build(new CacheLoader<>() {
			@Override
			public Map<String, McpServiceEntity> load(String key) throws Exception {
				return loadMcpServices(mcpConfigRepository.findAll());
			}
		});

	private Map<String, McpServiceEntity> loadMcpServices(List<McpConfigEntity> mcpConfigEntities) throws IOException {
		Map<String, McpServiceEntity> toolCallbackMap = new ConcurrentHashMap<>();
		for (McpConfigEntity mcpConfigEntity : mcpConfigEntities) {
			if (mcpConfigEntity.getConnectionType() == null) {
				throw new IOException("Connection type is required");
			}
			McpConfigType type = mcpConfigEntity.getConnectionType();
			String serverName = mcpConfigEntity.getMcpServerName();
			switch (type) {

				case SSE -> {
					McpClientTransport transport = null;
					try (JsonParser jsonParser = new ObjectMapper()
						.createParser(mcpConfigEntity.getConnectionConfig())) {
						McpServerConfig mcpServerConfig = jsonParser.readValueAs(McpServerConfig.class);
						if (mcpServerConfig.getUrl() == null || mcpServerConfig.getUrl().isEmpty()) {
							throw new IOException("Invalid MCP server URL");
						}

						// 获取URL和base_uri
						String url = mcpServerConfig.getUrl();

						// 移除末尾的"/sse"部分
						if (url.endsWith("/sse")) {
							url = url.substring(0, url.length() - 4); // 移除末尾的"/sse"
						}
						// 移除末尾的斜杠，避免与baseUrl组合时产生路径问题
						else if (url.endsWith("/")) {
							url = url.substring(0, url.length() - 1);
						}

						logger.info("Connecting to SSE endpoint: {}", url);

						// 创建WebClient并添加必要的请求头
						WebClient.Builder webClient = WebClient.builder()
							.baseUrl(url)
							.defaultHeader("Accept", "text/event-stream")
							.defaultHeader("Content-Type", "application/json");

						transport = new WebFluxSseClientTransport(webClient, new ObjectMapper());
						McpServiceEntity mcpServiceEntity = configureMcpTransport(serverName, transport);
						if (mcpServiceEntity != null) {
							toolCallbackMap.put(serverName, mcpServiceEntity);
						}
					}
				}
				case STUDIO -> {
					McpClientTransport transport = null;
					try (JsonParser jsonParser = new ObjectMapper()
						.createParser(mcpConfigEntity.getConnectionConfig())) {
						McpServerConfig mcpServerConfig = jsonParser.readValueAs(McpServerConfig.class);

						// 提取命令参数
						String command = mcpServerConfig.getCommand();
						List<String> args = mcpServerConfig.getArgs();
						Map<String, String> env = mcpServerConfig.getEnv();

						// 检查命令是否存在
						if (command == null || command.isEmpty()) {
							throw new IOException(
									"Missing required 'command' field in server configuration for " + serverName);
						}

						// 使用Builder模式创建ServerParameters实例
						ServerParameters.Builder builder = ServerParameters.builder(command);

						// 添加参数
						if (args != null && !args.isEmpty()) {
							builder.args(args);
						}

						// 添加环境变量
						if (env != null && !env.isEmpty()) {
							builder.env(env);
						}

						// 构建ServerParameters实例
						ServerParameters serverParameters = builder.build();
						transport = new StdioClientTransport(serverParameters, new ObjectMapper());

						// 配置MCP客户端
						McpServiceEntity mcpServiceEntity = configureMcpTransport(serverName, transport);
						if (mcpServiceEntity != null) {
							toolCallbackMap.put(serverName, mcpServiceEntity);
						}
						logger.info("STUDIO MCP Client configured for server: {}", serverName);
					}
					catch (Exception e) {
						logger.error("Error creating STUDIO transport: ", e);
						throw new IOException("Failed to create StdioClientTransport: " + e.getMessage(), e);
					}
				}
				case STREAMING -> {
					// 处理STREAMING类型的连接
					// 注意：此处需要实现STREAMING类型的处理逻辑
					logger.warn("STREAMING connection type is not fully implemented yet");
					throw new UnsupportedOperationException("STREAMING connection type is not supported yet");
				}
			}
		}
		return toolCallbackMap;
	}

	private McpServiceEntity configureMcpTransport(String mcpServerName, McpClientTransport transport)
			throws IOException {
		if (transport != null) {
			McpAsyncClient mcpAsyncClient = McpClient.async(transport)
				.clientInfo(new McpSchema.Implementation(mcpServerName, "1.0.0"))
				.build();
			try {
				mcpAsyncClient.initialize().block();
				logger.info("MCP transport configured successfully for: {}", mcpServerName);

				AsyncMcpToolCallbackProvider callbackProvider = new AsyncMcpToolCallbackProvider(mcpAsyncClient);
				return new McpServiceEntity(mcpAsyncClient, callbackProvider, mcpServerName);
			}
			catch (Exception e) {
				logger.error("Failed to initialize MCP transport for {}", mcpServerName, e);
				return null;
			}
		}
		return null;
	}

	public void addMcpServer(McpConfigRequestVO mcpConfig) throws IOException {
		insertOrUpdateMcpRepo(mcpConfig);
		toolCallbackMapCache.invalidateAll();
	}

	public List<McpConfigEntity> insertOrUpdateMcpRepo(McpConfigRequestVO mcpConfigVO) throws IOException {
		List<McpConfigEntity> entityList = new ArrayList<>();
		try (JsonParser jsonParser = new ObjectMapper().createParser(mcpConfigVO.getConfigJson())) {
			McpServersConfig mcpServerConfig = jsonParser.readValueAs(McpServersConfig.class);
			String type = mcpConfigVO.getConnectionType();
			McpConfigType mcpConfigType = McpConfigType.valueOf(type);
			if (McpConfigType.STUDIO.equals(mcpConfigType)) {
				// STUDIO类型的连接需要特殊处理
				mcpServerConfig.getMcpServers().forEach((name, config) -> {
					if (config.getCommand() == null || config.getCommand().isEmpty()) {
						throw new IllegalArgumentException(
								"Missing required 'command' field in server configuration for " + name);
					}
					if (config.getUrl() != null && !config.getUrl().isEmpty()) {
						throw new IllegalArgumentException(
								"STUDIO type should not have 'url' field in server configuration for " + name);
					}
				});
			}
			else if (McpConfigType.SSE.equals(mcpConfigType)) {
				// SSE类型的连接需要特殊处理
				mcpServerConfig.getMcpServers().forEach((name, config) -> {
					if (config.getUrl() == null || config.getUrl().isEmpty()) {
						throw new IllegalArgumentException(
								"Missing required 'url' field in server configuration for " + name);
					}
					if (config.getCommand() != null && !config.getCommand().isEmpty()) {
						throw new IllegalArgumentException(
								"SSE type should not have 'command' field in server configuration for " + name);
					}
				});
			}
			else if (McpConfigType.STREAMING.equals(mcpConfigType)) {
				throw new UnsupportedOperationException("STREAMING connection type is not supported yet");
			}

			// 迭代处理每个MCP服务器配置
			for (Map.Entry<String, McpServerConfig> entry : mcpServerConfig.getMcpServers().entrySet()) {
				String serverName = entry.getKey();
				McpServerConfig serverConfig = entry.getValue();

				// 使用ServerConfig的toJson方法将配置转换为JSON字符串
				String configJson = serverConfig.toJson();

				// 查找对应的MCP配置实体
				McpConfigEntity mcpConfigEntity = mcpConfigRepository.findByMcpServerName(serverName);
				if (mcpConfigEntity == null) {
					mcpConfigEntity = new McpConfigEntity();
					mcpConfigEntity.setConnectionConfig(configJson);
					mcpConfigEntity.setMcpServerName(serverName);
					mcpConfigEntity.setConnectionType(mcpConfigType);
				}
				else {
					mcpConfigEntity.setConnectionConfig(configJson);
					mcpConfigEntity.setConnectionType(mcpConfigType);
				}
				McpConfigEntity entity = mcpConfigRepository.save(mcpConfigEntity);
				entityList.add(entity);
				logger.info("MCP server '{}' has been saved to database.", serverName);

			}
		}
		return entityList;

	}

	public void removeMcpServer(long id) {
		Optional<McpConfigEntity> mcpConfigEntity = mcpConfigRepository.findById(id);
		mcpConfigEntity.ifPresent(this::removeMcpServer);
	}

	public void removeMcpServer(String mcpServerName) {
		var mcpConfig = mcpConfigRepository.findByMcpServerName(mcpServerName);
		removeMcpServer(mcpConfig);
	}

	private void removeMcpServer(McpConfigEntity mcpConfig) {
		if (null == mcpConfig) {
			return;
		}

		mcpConfigRepository.delete(mcpConfig);
		toolCallbackMapCache.invalidateAll();
	}

	public List<McpConfigEntity> getMcpServers() {
		return mcpConfigRepository.findAll();
	}

	public List<McpServiceEntity> getFunctionCallbacks(String planId) {
		try {
			return new ArrayList<>(
					this.toolCallbackMapCache.get(Optional.ofNullable(planId).orElse("DEFAULT")).values());
		}
		catch (Throwable t) {
			logger.error("Failed to get function callbacks for plan: {}", planId, t);
			return new ArrayList<>();
		}
	}

}
