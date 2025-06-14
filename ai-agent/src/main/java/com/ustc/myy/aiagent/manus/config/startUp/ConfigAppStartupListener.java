package com.ustc.myy.aiagent.manus.config.startUp;

import com.ustc.myy.aiagent.manus.config.ConfigService;
import com.ustc.myy.aiagent.manus.config.entity.ConfigEntity;
import com.ustc.myy.aiagent.manus.dynamic.agent.service.DynamicAgentScanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class ConfigAppStartupListener implements ApplicationListener<ApplicationStartedEvent> {

	private static final Logger log = LoggerFactory.getLogger(ConfigAppStartupListener.class);

	@Autowired
	private ConfigService configService;

	@Autowired
	private DynamicAgentScanner dynamicAgentScanner;

	@Override
	public void onApplicationEvent(ApplicationStartedEvent event) {
		initializeConfigs();
		initializeDynamicAgents();
	}

	private void initializeConfigs() {
		try {
			List<ConfigEntity> allConfigs = configService.getAllConfigs();

			// 按配置组统计配置数量
			Map<String, Long> configCountByGroup = allConfigs.stream()
				.collect(Collectors.groupingBy(ConfigEntity::getConfigGroup, Collectors.counting()));

			// 记录配置系统的启动状态
			log.info("Configuration system initialized with {} total configs", allConfigs.size());
			configCountByGroup.forEach((group, count) -> log.info("Group '{}': {} configs", group, count));

			// 检查是否有自定义值的配置
			long customizedCount = allConfigs.stream()
				.filter(config -> !config.getConfigValue().equals(config.getDefaultValue()))
				.count();

			log.info("{} configs are using custom values", customizedCount);

		}
		catch (Exception e) {
			log.error("Failed to verify configuration system state", e);
		}
	}

	private void initializeDynamicAgents() {
		try {
			log.info("Starting to initialize dynamic agents...");
			dynamicAgentScanner.scanAndSaveAgents();
			log.info("Dynamic agents initialization completed");
		}
		catch (Exception e) {
			log.error("Failed to initialize dynamic agents", e);
		}
	}

}
