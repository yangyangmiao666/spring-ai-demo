package com.ustc.myy.aiagent.manus.tool.browser.actions;

import com.microsoft.playwright.Page;
import com.ustc.myy.aiagent.manus.tool.browser.BrowserUseTool;
import com.ustc.myy.aiagent.manus.tool.code.ToolExecuteResult;

public class CloseTabAction extends BrowserAction {

	private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CloseTabAction.class);

	public CloseTabAction(BrowserUseTool browserUseTool) {
		super(browserUseTool);
	}

	@Override
	public ToolExecuteResult execute(BrowserRequestVO request) throws Exception {
		Page page = getCurrentPage(); // 获取 Playwright 的 Page 实例

		// 关闭当前页面
		page.close();

		// 刷新标签页信息
		return new ToolExecuteResult("Closed current tab");
	}

}
