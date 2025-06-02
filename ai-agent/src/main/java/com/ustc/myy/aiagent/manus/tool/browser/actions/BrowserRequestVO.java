package com.ustc.myy.aiagent.manus.tool.browser.actions;

import com.alibaba.fastjson2.annotation.JSONField;
import lombok.Getter;
import lombok.Setter;

/**
 * 浏览器工具请求对象 用于封装浏览器操作的请求参数
 */
@Setter
@Getter
public class BrowserRequestVO {

	/**
	 * 浏览器操作类型 支持: navigate, click, input_text, key_enter, screenshot, get_html, get_text,
	 * execute_js, scroll, switch_tab, new_tab, close_tab, refresh, get_element_position,
	 * move_to_and_click
	 */
	private String action;

	/**
	 * URL地址，用于navigate和new_tab操作
	 */
	private String url;

	/**
	 * 元素索引，用于click、input_text和key_enter操作
	 */
	private Integer index;

	/**
	 * 要输入的文本，用于input_text操作
	 */
	private String text;

	/**
	 * JavaScript代码，用于execute_js操作
	 */
	private String script;

	/**
	 * 滚动像素，用于scroll操作 正数向下滚动，负数向上滚动
	 */
	@JSONField(name = "scroll_amount")
	private Integer scrollAmount;

	/**
	 * 标签页ID，用于switch_tab操作
	 */
	@JSONField(name = "tab_id")
	private Integer tabId;

	/**
	 * 元素名称，用于get_element_position操作
	 */
	@JSONField(name = "element_name")
	private String elementName;

	/**
	 * X坐标，用于move_to_and_click操作
	 */
	@JSONField(name = "position_x")
	private Integer positionX;

	/**
	 * Y坐标，用于move_to_and_click操作
	 */
	@JSONField(name = "position_y")
	private Integer positionY;

}
