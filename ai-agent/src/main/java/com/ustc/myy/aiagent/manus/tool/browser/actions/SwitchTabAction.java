package com.ustc.myy.aiagent.manus.tool.browser.actions;

import com.microsoft.playwright.Page;
import com.ustc.myy.aiagent.manus.tool.browser.BrowserUseTool;
import com.ustc.myy.aiagent.manus.tool.code.ToolExecuteResult;

public class SwitchTabAction extends BrowserAction {

	private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(SwitchTabAction.class);

	public SwitchTabAction(BrowserUseTool browserUseTool) {
		super(browserUseTool);
	}

	@Override
	public ToolExecuteResult execute(BrowserRequestVO request) throws Exception {
		Integer tabId = request.getTabId();
		if (tabId == null || tabId < 0) {
			return new ToolExecuteResult("Tab ID is out of range for 'switch_tab' action");
		}

		Page page = getCurrentPage(); // 获取 Playwright 的 Page 实例
		Page targetPage = page.context().pages().get(tabId); // 切换到指定的标签页
		if (targetPage == null) {
			return new ToolExecuteResult("Tab ID " + tabId + " does not exist");
		}
		return new ToolExecuteResult("Switched to tab " + tabId);
	}

}
