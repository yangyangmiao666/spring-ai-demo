package com.ustc.myy.aiagent.manus.tool;

import com.ustc.myy.aiagent.manus.tool.code.ToolExecuteResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.ai.tool.function.FunctionToolCallback;
import org.springframework.ai.tool.metadata.ToolMetadata;

@Slf4j
public class TerminateTool implements ToolCallBiFunctionDef {

	private static String PARAMETERS = """
			{
			  "type" : "object",
			  "properties" : {
			    "message" : {
			      "type" : "string",
			      "description" : "The termination message"
			    }
			  },
			  "required" : [ "message" ]
			}
			""";

	public static final String name = "terminate";

	private static final String description = """

			Terminate the current execution step with a comprehensive summary message.
			This message will be passed as the final output of the current step and should include:

			- Detailed execution results and status
			- All relevant facts and data collected
			- Key findings and observations
			- Important insights and conclusions
			- Any actionable recommendations

			The summary should be thorough enough to provide complete context for subsequent steps or other agents.

			""";

	public static OpenAiApi.FunctionTool getToolDefinition() {
		OpenAiApi.FunctionTool.Function function = new OpenAiApi.FunctionTool.Function(description, name, PARAMETERS);
		return new OpenAiApi.FunctionTool(function);
	}

	public static FunctionToolCallback getFunctionToolCallback(String planId) {
		return FunctionToolCallback.builder(name, new TerminateTool(planId))
			.description(description)
			.inputSchema(PARAMETERS)
			.inputType(String.class)
			.toolMetadata(ToolMetadata.builder().returnDirect(true).build())
			.build();
	}

	private String planId;

	private String lastTerminationMessage = "";

	private boolean isTerminated = false;

	private String terminationTimestamp = "";

	@Override
	public String getCurrentToolStateString() {
		return String.format("""
				Termination Tool Status:
				- Current State: %s
				- Last Termination: %s
				- Termination Message: %s
				- Timestamp: %s
				""", isTerminated ? "🛑 Terminated" : "⚡ Active",
				isTerminated ? "Process was terminated" : "No termination recorded",
				lastTerminationMessage.isEmpty() ? "N/A" : lastTerminationMessage,
				terminationTimestamp.isEmpty() ? "N/A" : terminationTimestamp);
	}

	public TerminateTool(String planId) {
		this.planId = planId;
	}

	public ToolExecuteResult run(String toolInput) {
		log.info("Terminate toolInput: {}", toolInput);
		this.lastTerminationMessage = toolInput;
		this.isTerminated = true;
		this.terminationTimestamp = java.time.LocalDateTime.now().toString();

		return new ToolExecuteResult(toolInput);
	}

	@Override
	public ToolExecuteResult apply(String s, ToolContext toolContext) {
		return run(s);
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getDescription() {
		return description;
	}

	@Override
	public String getParameters() {
		return PARAMETERS;
	}

	@Override
	public Class<?> getInputType() {
		return String.class;
	}

	@Override
	public boolean isReturnDirect() {
		return true;
	}

	@Override
	public void setPlanId(String planId) {
		this.planId = planId;
	}

	@Override
	public void cleanup(String planId) {
		// do nothing
	}

	@Override
	public String getServiceGroup() {
		return "default-service-group";
	}

}
