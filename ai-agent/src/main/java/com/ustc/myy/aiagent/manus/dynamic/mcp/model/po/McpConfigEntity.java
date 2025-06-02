package com.ustc.myy.aiagent.manus.dynamic.mcp.model.po;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Entity
@Table(name = "mcp_config")
public class McpConfigEntity {

    // Getters and Setters
    @Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, unique = true)
	private String mcpServerName;

	@Column(nullable = false)
	@Enumerated(EnumType.STRING)
	private McpConfigType connectionType;

	@Column(nullable = false, length = 4000)
	private String connectionConfig;

    @Override
	public String toString() {
		return "McpConfigEntity{" + "id=" + id + ", mcpServerName='" + mcpServerName + '\'' + ", connectionType="
				+ connectionType + ", connectionConfig='" + connectionConfig + '\'' + '}';
	}

}
