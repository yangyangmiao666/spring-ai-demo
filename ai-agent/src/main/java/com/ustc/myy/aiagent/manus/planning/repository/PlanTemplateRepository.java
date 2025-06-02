package com.ustc.myy.aiagent.manus.planning.repository;

import com.ustc.myy.aiagent.manus.planning.model.po.PlanTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 计划模板数据访问接口
 */
@Repository
public interface PlanTemplateRepository extends JpaRepository<PlanTemplate, String> {

    /**
     * 根据计划模板ID查找计划模板
     *
     * @param planTemplateId 计划模板ID
     * @return 计划模板实体
     */
    Optional<PlanTemplate> findByPlanTemplateId(String planTemplateId);

    /**
     * 根据计划模板ID删除计划模板
     *
     * @param planTemplateId 计划模板ID
     */
    void deleteByPlanTemplateId(String planTemplateId);

}
