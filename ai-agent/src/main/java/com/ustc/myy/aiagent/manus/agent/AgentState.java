package com.ustc.myy.aiagent.manus.agent;

/**
 * Agent状态
 *
 * @author YangyangMiao
 * @version 1.0
 * @email yangyangmiao666@icloud.com
 * @date 2025/6/1 02:34
 */
public enum AgentState {

    NOT_STARTED("not_started"),
    IN_PROGRESS("in_progress"),
    COMPLETED("completed"),
    BLOCKED("blocked"),
    FAILED("failed");

    private final String state;

    AgentState(String state) {
        this.state = state;
    }

    @Override
    public String toString() {
        return state;
    }

}
