package com.ustc.myy.aiagent.manus.tool.browser.actions;

import com.microsoft.playwright.ElementHandle;
import com.microsoft.playwright.Page;
import com.ustc.myy.aiagent.manus.tool.browser.BrowserUseTool;
import com.ustc.myy.aiagent.manus.tool.browser.DriverWrapper;
import com.ustc.myy.aiagent.manus.tool.browser.InteractiveElement;
import com.ustc.myy.aiagent.manus.tool.code.ToolExecuteResult;

import java.util.List;
import java.util.Random;

public abstract class BrowserAction {

	public abstract ToolExecuteResult execute(BrowserRequestVO request) throws Exception;

	private final BrowserUseTool browserUseTool;

	public BrowserAction(BrowserUseTool browserUseTool) {
		this.browserUseTool = browserUseTool;
	}

	public BrowserUseTool getBrowserUseTool() {
		return browserUseTool;
	}

	/**
	 * 模拟人类行为
	 * @param element Playwright的ElementHandle实例
	 */
	protected void simulateHumanBehavior(ElementHandle element) {
		try {
			// 添加随机延迟
			Thread.sleep(new Random().nextInt(500) + 200);
		}
		catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}

	/**
	 * 获取 DriverWrapper 实例
	 * @return DriverWrapper
	 */
	protected DriverWrapper getDriverWrapper() {

		return browserUseTool.getDriver();
	}

	/**
	 * 获取当前页面 Page 实例
	 * @return 当前 Playwright 的 Page 实例
	 */
	protected Page getCurrentPage() {
		DriverWrapper driverWrapper = getDriverWrapper();
		return driverWrapper.getCurrentPage();
	}

	/**
	 * 获取可交互元素
	 * @param page Playwright的Page实例
	 * @return 可交互元素列表
	 */
	protected List<InteractiveElement> getInteractiveElements(Page page) {
		DriverWrapper driverWrapper = browserUseTool.getDriver();
		return driverWrapper.getInteractiveElementRegistry().getAllElements(page);
	}

}
