package com.ustc.myy.aiagent.manus.tool.browser.actions;

import com.microsoft.playwright.Page;
import com.ustc.myy.aiagent.manus.tool.browser.BrowserUseTool;
import com.ustc.myy.aiagent.manus.tool.browser.InteractiveElement;
import com.ustc.myy.aiagent.manus.tool.code.ToolExecuteResult;

public class KeyEnterAction extends BrowserAction {

	public KeyEnterAction(BrowserUseTool browserUseTool) {
		super(browserUseTool);
	}

	@Override
	public ToolExecuteResult execute(BrowserRequestVO request) throws Exception {
		Integer index = request.getIndex();
		if (index == null) {
			return new ToolExecuteResult("Index is required for 'key_enter' action");
		}

		Page page = getCurrentPage();
		// 获取注册表
		var driverWrapper = getDriverWrapper();
		var registry = driverWrapper.getInteractiveElementRegistry();
		// 获取目标元素
		var elementOpt = registry.getElementById(index);
		if (elementOpt.isEmpty()) {
			return new ToolExecuteResult("Element with index " + index + " not found");
		}
		InteractiveElement enterElement = elementOpt.get();
		// 执行回车操作
		try {
			enterElement.getLocator().press("Enter");
		}
		catch (Exception e) {
			return new ToolExecuteResult("Failed to press Enter on element at index " + index + ": " + e.getMessage());
		}
		return new ToolExecuteResult("Hit the enter key at index " + index);
	}

}
