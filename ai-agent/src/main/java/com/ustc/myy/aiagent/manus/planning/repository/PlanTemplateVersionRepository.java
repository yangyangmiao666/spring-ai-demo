package com.ustc.myy.aiagent.manus.planning.repository;

import com.ustc.myy.aiagent.manus.planning.model.po.PlanTemplateVersion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 计划模板版本数据访问接口
 */
@Repository
public interface PlanTemplateVersionRepository extends JpaRepository<PlanTemplateVersion, Long> {

    /**
     * 根据计划模板ID查找所有版本，按版本号排序
     *
     * @param planTemplateId 计划模板ID
     * @return 版本列表
     */
    List<PlanTemplateVersion> findByPlanTemplateIdOrderByVersionIndexAsc(String planTemplateId);

    /**
     * 根据计划模板ID查找最大的版本号
     *
     * @param planTemplateId 计划模板ID
     * @return 最大版本号，如果没有版本则返回null
     */
    @Query("SELECT MAX(v.versionIndex) FROM PlanTemplateVersion v WHERE v.planTemplateId = :planTemplateId")
    Integer findMaxVersionIndexByPlanTemplateId(@Param("planTemplateId") String planTemplateId);

    /**
     * 根据计划模板ID和版本号查找特定版本
     *
     * @param planTemplateId 计划模板ID
     * @param versionIndex   版本号
     * @return 计划模板版本实体
     */
    PlanTemplateVersion findByPlanTemplateIdAndVersionIndex(String planTemplateId, Integer versionIndex);

    /**
     * 根据计划模板ID删除所有版本
     *
     * @param planTemplateId 计划模板ID
     */
    void deleteByPlanTemplateId(String planTemplateId);

}
