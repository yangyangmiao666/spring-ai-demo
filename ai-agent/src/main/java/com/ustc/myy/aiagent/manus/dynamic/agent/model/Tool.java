package com.ustc.myy.aiagent.manus.dynamic.agent.model;

import lombok.Data;

@Data
public class Tool {

    // Getters and Setters
    private String key;

	private String name;

	private String description;

	private boolean enabled;

	private String serviceGroup;

}
