package com.ustc.myy.aiagent.manus.tool.browser.actions;

import com.microsoft.playwright.Page;
import com.ustc.myy.aiagent.manus.tool.browser.BrowserUseTool;
import com.ustc.myy.aiagent.manus.tool.code.ToolExecuteResult;

public class ScreenShotAction extends BrowserAction {

	public ScreenShotAction(BrowserUseTool browserUseTool) {
		super(browserUseTool);
	}

	@Override
	public ToolExecuteResult execute(BrowserRequestVO request) throws Exception {
		Page page = getCurrentPage(); // 获取 Playwright 的 Page 实例
		byte[] screenshot = page.screenshot(); // 捕获屏幕截图
		String base64Screenshot = java.util.Base64.getEncoder().encodeToString(screenshot);

		return new ToolExecuteResult("Screenshot captured (base64 length: " + base64Screenshot.length() + ")");
	}

}
