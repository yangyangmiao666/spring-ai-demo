package com.ustc.myy.aiagent.manus.config;

import com.ustc.myy.aiagent.manus.config.entity.ConfigInputType;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "manus")
public class ManusProperties {

	@Lazy
	@Autowired
	private ConfigService configService;

	@Setter
    @ConfigProperty(group = "manus", subGroup = "browser", key = "headless", path = "manus.browser.headless",
			description = "是否使用无头浏览器模式", defaultValue = "false", inputType = ConfigInputType.CHECKBOX,
			options = { @ConfigOption(value = "true", label = "是"), @ConfigOption(value = "false", label = "否") })
	private volatile Boolean browserHeadless;

	public Boolean getBrowserHeadless() {
		String configPath = "manus.browser.headless";
		String value = configService.getConfigValue(configPath);
		if (value != null) {
			browserHeadless = Boolean.valueOf(value);
		}
		return browserHeadless;
	}

    @Setter
    @ConfigProperty(group = "manus", subGroup = "interaction", key = "openBrowser", path = "manus.openBrowserAuto",
			description = "启动时自动打开浏览器", defaultValue = "true", inputType = ConfigInputType.CHECKBOX,
			options = { @ConfigOption(value = "true", label = "是"), @ConfigOption(value = "false", label = "否") })
	private volatile Boolean openBrowserAuto;

	public Boolean getOpenBrowserAuto() {
		String configPath = "manus.openBrowserAuto";
		String value = configService.getConfigValue(configPath);
		if (value != null) {
			openBrowserAuto = Boolean.valueOf(value);
		}
		return openBrowserAuto;
	}

    @Setter
    @ConfigProperty(group = "manus", subGroup = "interaction", key = "consoleQuery", path = "manus.consoleQuery",
			description = "启用控制台交互模式", defaultValue = "false", inputType = ConfigInputType.CHECKBOX,
			options = { @ConfigOption(value = "true", label = "是"), @ConfigOption(value = "false", label = "否") })
	private volatile Boolean consoleQuery;

	public Boolean getConsoleQuery() {
		String configPath = "manus.consoleQuery";
		String value = configService.getConfigValue(configPath);
		if (value != null) {
			consoleQuery = Boolean.valueOf(value);
		}
		return consoleQuery;
	}

    @Setter
    @ConfigProperty(group = "manus", subGroup = "browser", key = "requestTimeout",
			path = "manus.browser.requestTimeout", description = "浏览器请求超时时间(秒)", defaultValue = "180",
			inputType = ConfigInputType.NUMBER)
	private volatile Integer browserRequestTimeout;

	@Setter
    @ConfigProperty(group = "manus", subGroup = "browser", key = "debug", path = "manus.browser.debug",
			description = "浏览器debug模式", defaultValue = "false", inputType = ConfigInputType.CHECKBOX,
			options = { @ConfigOption(value = "true", label = "是"), @ConfigOption(value = "false", label = "否") })
	private volatile Boolean browserDebug;

	@Setter
    @ConfigProperty(group = "manus", subGroup = "agent", key = "maxSteps", path = "manus.maxSteps",
			description = "智能体执行最大步数", defaultValue = "20", inputType = ConfigInputType.NUMBER)
	private volatile Integer maxSteps;

	public Integer getBrowserRequestTimeout() {
		String configPath = "manus.browser.requestTimeout";
		String value = configService.getConfigValue(configPath);
		if (value != null) {
			browserRequestTimeout = Integer.valueOf(value);
		}
		return browserRequestTimeout;
	}

    public Boolean getBrowserDebug() {
		String configPath = "manus.browser.debug";
		String value = configService.getConfigValue(configPath);
		if (value != null) {
			browserDebug = Boolean.valueOf(value);
		}
		return browserDebug;
	}

    public Integer getMaxSteps() {
		String configPath = "manus.maxSteps";
		String value = configService.getConfigValue(configPath);
		if (value != null) {
			maxSteps = Integer.valueOf(value);
		}
		return maxSteps;
	}

    @Setter
    @ConfigProperty(group = "manus", subGroup = "agent", key = "resetAgents", path = "manus.resetAgents",
			description = "重置所有agent", defaultValue = "true", inputType = ConfigInputType.CHECKBOX,
			options = { @ConfigOption(value = "true", label = "是"), @ConfigOption(value = "false", label = "否") })
	private volatile Boolean resetAgents;

	public Boolean getResetAgents() {
		String configPath = "manus.resetAgents";
		String value = configService.getConfigValue(configPath);
		if (value != null) {
			resetAgents = Boolean.valueOf(value);
		}
		return resetAgents;
	}

}
