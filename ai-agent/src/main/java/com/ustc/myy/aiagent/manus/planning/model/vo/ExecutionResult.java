package com.ustc.myy.aiagent.manus.planning.model.vo;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * 计划执行的整体结果
 */
@Setter
@Getter
public class ExecutionResult {

    // Getters and Setters
    private String planId;

	private List<ExecutionStep> stepResults;

	private String executionDetails;

	private boolean success;

    public void addStepResult(ExecutionStep stepResult) {
		if (this.stepResults == null) {
			this.stepResults = new ArrayList<>();
		}
		this.stepResults.add(stepResult);
	}

}
