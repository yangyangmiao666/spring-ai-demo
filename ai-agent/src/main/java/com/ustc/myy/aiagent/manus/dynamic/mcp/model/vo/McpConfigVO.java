package com.ustc.myy.aiagent.manus.dynamic.mcp.model.vo;


import com.ustc.myy.aiagent.manus.dynamic.mcp.model.po.McpConfigEntity;
import com.ustc.myy.aiagent.manus.dynamic.mcp.model.po.McpConfigType;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * VO对象，用于McpConfig的前端展示
 */
@Setter
@Getter
public class McpConfigVO {

    // Getters and Setters
    private Long id;

    private String mcpServerName;

    private McpConfigType connectionType;

    private String connectionConfig;

    private List<String> toolNames; // 添加工具名称列表，用于前端展示

    public McpConfigVO() {
    }

    public McpConfigVO(McpConfigEntity entity) {
        this.id = entity.getId();
        this.mcpServerName = entity.getMcpServerName();
        this.connectionType = entity.getConnectionType();
        this.connectionConfig = entity.getConnectionConfig();
        this.toolNames = new ArrayList<>(); // 初始化为空列表，实际使用时可能需要从其他地方获取
    }

    // 将VO列表转换为实体列表的静态方法
    public static List<McpConfigVO> fromEntities(List<McpConfigEntity> entities) {
        List<McpConfigVO> vos = new ArrayList<>();
        if (entities != null) {
            for (McpConfigEntity entity : entities) {
                vos.add(new McpConfigVO(entity));
            }
        }
        return vos;
    }

    @Override
    public String toString() {
        return "McpConfigVO{" + "id=" + id + ", mcpServerName='" + mcpServerName + '\'' + ", connectionType="
                + connectionType + ", connectionConfig='" + connectionConfig + '\'' + ", toolNames=" + toolNames + '}';
    }

}
