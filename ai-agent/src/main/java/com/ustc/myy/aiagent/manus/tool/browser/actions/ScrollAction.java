package com.ustc.myy.aiagent.manus.tool.browser.actions;

import com.microsoft.playwright.Page;
import com.ustc.myy.aiagent.manus.tool.browser.BrowserUseTool;
import com.ustc.myy.aiagent.manus.tool.code.ToolExecuteResult;

public class ScrollAction extends BrowserAction {

	private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ScrollAction.class);

	public ScrollAction(BrowserUseTool browserUseTool) {
		super(browserUseTool);
	}

	@Override
	public ToolExecuteResult execute(BrowserRequestVO request) throws Exception {
		Integer scrollAmount = request.getScrollAmount();

		if (scrollAmount == null) {
			return new ToolExecuteResult("Scroll amount is required for 'scroll' action");
		}

		Page page = getCurrentPage(); // 获取 Playwright 的 Page 实例
		page.evaluate("window.scrollBy(0, arguments[0])", scrollAmount); // 使用 Playwright
																			// 执行滚动

		String direction = scrollAmount > 0 ? "down" : "up";
		return new ToolExecuteResult("Scrolled " + direction + " by " + Math.abs(scrollAmount) + " pixels");
	}

}
