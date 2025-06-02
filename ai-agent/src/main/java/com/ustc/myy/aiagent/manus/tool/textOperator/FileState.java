
package com.ustc.myy.aiagent.manus.tool.textOperator;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FileState {

	private String currentFilePath = "";

	private String lastOperationResult = "";

	private final Object fileLock = new Object();
}
