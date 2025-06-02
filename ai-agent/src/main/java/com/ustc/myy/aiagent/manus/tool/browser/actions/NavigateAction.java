package com.ustc.myy.aiagent.manus.tool.browser.actions;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.Page.WaitForLoadStateOptions;
import com.microsoft.playwright.options.LoadState;
import com.ustc.myy.aiagent.manus.tool.browser.BrowserUseTool;
import com.ustc.myy.aiagent.manus.tool.code.ToolExecuteResult;

public class NavigateAction extends BrowserAction {

	public NavigateAction(BrowserUseTool browserUseTool) {
		super(browserUseTool);
	}

	@Override
	public ToolExecuteResult execute(BrowserRequestVO request) throws Exception {
		String url = request.getUrl();
		Integer timeout = getBrowserUseTool().getManusProperties().getBrowserRequestTimeout();
		if (timeout == null) {
			timeout = 30; // 默认超时时间为 30 秒
		}
		if (url == null) {
			return new ToolExecuteResult("URL is required for 'navigate' action");
		}
		// 自动补全 URL 前缀
		if (!url.startsWith("http://") && !url.startsWith("https://")) {
			url = "https://" + url;
		}
		Page page = getCurrentPage(); // 获取 Playwright 的 Page 实例
		page.navigate(url, new Page.NavigateOptions().setTimeout(timeout * 1000));

		// 在调用 page.content() 之前，确保页面已完全加载
		page.waitForLoadState(LoadState.DOMCONTENTLOADED, new WaitForLoadStateOptions().setTimeout(timeout * 1000));

		return new ToolExecuteResult("Navigated to " + url);
	}

}
