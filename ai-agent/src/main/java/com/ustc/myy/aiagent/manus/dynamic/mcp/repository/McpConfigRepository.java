package com.ustc.myy.aiagent.manus.dynamic.mcp.repository;

import com.ustc.myy.aiagent.manus.dynamic.mcp.model.po.McpConfigEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * McpConfig
 */
@Repository
public interface McpConfigRepository extends JpaRepository<McpConfigEntity, Long> {

    McpConfigEntity findByMcpServerName(String mcpServerName);

}
