package com.ustc.myy.aiagent.manus.tool.browser.actions;

import com.microsoft.playwright.Page;
import com.ustc.myy.aiagent.manus.tool.browser.BrowserUseTool;
import com.ustc.myy.aiagent.manus.tool.code.ToolExecuteResult;

public class ExecuteJsAction extends BrowserAction {

	public ExecuteJsAction(BrowserUseTool browserUseTool) {
		super(browserUseTool);
	}

	@Override
	public ToolExecuteResult execute(BrowserRequestVO request) throws Exception {
		String script = request.getScript();
		if (script == null) {
			return new ToolExecuteResult("Script is required for 'execute_js' action");
		}

		Page page = getCurrentPage(); // 获取 Playwright 的 Page 实例
		Object result = page.evaluate(script);

		if (result == null) {
			return new ToolExecuteResult("Successfully executed JavaScript code.");
		}
		else {
			return new ToolExecuteResult(result.toString());
		}
	}

}
