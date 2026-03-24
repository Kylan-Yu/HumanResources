package com.hrms.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hrms.system.entity.Dept;

import java.util.List;

/**
 * 部门服务接口
 *
 * @author HRMS
 */
public interface DeptService extends IService<Dept> {

    /**
     * 获取部门树
     */
    List<Dept> getDeptTree();

    /**
     * 根据组织ID获取部门列表
     */
    List<Dept> getDeptsByOrgId(Long orgId);

    /**
     * 创建部门
     */
    void createDept(Dept dept);

    /**
     * 更新部门
     */
    void updateDept(Dept dept);

    /**
     * 删除部门
     */
    void deleteDept(Long id);

    /**
     * 批量删除部门
     */
    void batchDeleteDepts(List<Long> ids);

    /**
     * 更新部门状态
     */
    void updateDeptStatus(Long id, Integer status);

    /**
     * 获取所有启用的部门
     */
    List<Dept> listEnabledDepts();
}
