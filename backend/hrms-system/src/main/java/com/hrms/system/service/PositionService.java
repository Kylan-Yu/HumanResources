package com.hrms.system.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.hrms.system.entity.Position;

import java.util.List;

/**
 * 岗位服务接口
 *
 * @author HRMS
 */
public interface PositionService extends IService<Position> {

    /**
     * 分页查询岗位
     */
    IPage<Position> pagePositions(Page<Position> page, String positionName, String positionCode, 
                                 Long orgId, Long deptId, Integer status);

    /**
     * 根据组织ID获取岗位列表
     */
    List<Position> getPositionsByOrgId(Long orgId);

    /**
     * 根据部门ID获取岗位列表
     */
    List<Position> getPositionsByDeptId(Long deptId);

    /**
     * 创建岗位
     */
    void createPosition(Position position);

    /**
     * 更新岗位
     */
    void updatePosition(Position position);

    /**
     * 删除岗位
     */
    void deletePosition(Long id);

    /**
     * 批量删除岗位
     */
    void batchDeletePositions(List<Long> ids);

    /**
     * 更新岗位状态
     */
    void updatePositionStatus(Long id, Integer status);

    /**
     * 获取所有启用的岗位
     */
    List<Position> listEnabledPositions();
}
