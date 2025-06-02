package com.ustc.myy.aiagent.manus.tool.browser.actions;

import com.microsoft.playwright.Page;
import com.ustc.myy.aiagent.manus.tool.browser.BrowserUseTool;
import com.ustc.myy.aiagent.manus.tool.code.ToolExecuteResult;

public class GetTextAction extends BrowserAction {

	private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(GetTextAction.class);

	public GetTextAction(BrowserUseTool browserUseTool) {
		super(browserUseTool);
	}

	@Override
	public ToolExecuteResult execute(BrowserRequestVO request) throws Exception {
		Page page = getCurrentPage(); // 获取 Playwright 的 Page 实例
		StringBuilder allText = new StringBuilder();
		for (com.microsoft.playwright.Frame frame : page.frames()) {
			try {
				String text = frame.innerText("body");
				if (text != null && !text.isEmpty()) {
					allText.append(text).append("\n");
				}
			}
			catch (Exception e) {
				// 忽略没有body的frame
			}
		}
		String result = allText.toString().trim();
		log.info("get_text all frames body is {}", result);
		return new ToolExecuteResult(result);
	}

}
