package com.ustc.myy.aiagent.manus.tool.browser.actions;

import com.microsoft.playwright.Page;
import com.ustc.myy.aiagent.manus.tool.browser.BrowserUseTool;
import com.ustc.myy.aiagent.manus.tool.code.ToolExecuteResult;

public class GetHtmlAction extends BrowserAction {

	private final int MAX_LENGTH = 50000;

	public GetHtmlAction(BrowserUseTool browserUseTool) {
		super(browserUseTool);
	}

	@Override
	public ToolExecuteResult execute(BrowserRequestVO request) throws Exception {
		Page page = getCurrentPage(); // 获取 Playwright 的 Page 实例
		StringBuilder allHtml = new StringBuilder();
		for (com.microsoft.playwright.Frame frame : page.frames()) {
			try {
				String html = frame.content();
				if (html != null && !html.isEmpty()) {
					allHtml.append("<!-- frame: ").append(frame.url()).append(" -->\n");
					allHtml.append(html).append("\n\n");
				}
			}
			catch (Exception e) {
				// 忽略异常
			}
		}
		String result = allHtml.toString();
		if (result.length() > MAX_LENGTH) {
			result = result.substring(0, MAX_LENGTH) + "...";
		}
		return new ToolExecuteResult(result);
	}

}
