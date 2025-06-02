package com.ustc.myy.aiagent.manus.tool.browser.actions;

import com.microsoft.playwright.Page;
import com.ustc.myy.aiagent.manus.tool.browser.BrowserUseTool;
import com.ustc.myy.aiagent.manus.tool.code.ToolExecuteResult;

public class NewTabAction extends BrowserAction {

    public NewTabAction(BrowserUseTool browserUseTool) {
        super(browserUseTool);
    }

    @Override
    public ToolExecuteResult execute(BrowserRequestVO request) throws Exception {
        String url = request.getUrl();
        if (url == null) {
            return new ToolExecuteResult("URL is required for 'new_tab' action");
        }

        Page page = getCurrentPage(); // 打开新标签页
        page.navigate(url); // 导航到指定 URL
        return new ToolExecuteResult("Opened new tab with URL " + url);
    }

}
