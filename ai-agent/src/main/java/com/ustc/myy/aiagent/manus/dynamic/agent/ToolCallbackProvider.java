package com.ustc.myy.aiagent.manus.dynamic.agent;


import com.ustc.myy.aiagent.manus.planning.PlanningFactory;

import java.util.Map;

public interface ToolCallbackProvider {

    Map<String, PlanningFactory.ToolCallBackContext> getToolCallBackContext();

}
