package com.ustc.myy.aiagent.manus.dynamic.agent.repository;

import com.ustc.myy.aiagent.manus.dynamic.agent.entity.DynamicAgentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DynamicAgentRepository extends JpaRepository<DynamicAgentEntity, Long> {

	DynamicAgentEntity findByAgentName(String agentName);

}
