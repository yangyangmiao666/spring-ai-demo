package com.ustc.myy.aiagent.manus.config;

import lombok.Getter;

public class ConfigCacheEntry<T> {

	@Getter
    private T value;

	private long lastUpdateTime;

	private static final long EXPIRATION_TIME = 30000; // 30秒过期

	public ConfigCacheEntry(T value) {
		this.value = value;
		this.lastUpdateTime = System.currentTimeMillis();
	}

    public void setValue(T value) {
		this.value = value;
		this.lastUpdateTime = System.currentTimeMillis();
	}

	public boolean isExpired() {
		return System.currentTimeMillis() - lastUpdateTime > EXPIRATION_TIME;
	}

}
