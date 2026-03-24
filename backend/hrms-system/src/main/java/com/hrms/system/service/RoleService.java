package com.hrms.system.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.hrms.system.entity.Role;

import java.util.List;

/**
 * 角色服务接口
 *
 * @author HRMS
 */
public interface RoleService extends IService<Role> {

    /**
     * 分页查询角色
     */
    IPage<Role> pageRoles(Page<Role> page, String roleName, String roleCode, Integer status);

    /**
     * 获取所有启用的角色
     */
    List<Role> listEnabledRoles();

    /**
     * 创建角色
     */
    void createRole(Role role);

    /**
     * 更新角色
     */
    void updateRole(Role role);

    /**
     * 删除角色
     */
    void deleteRole(Long id);

    /**
     * 批量删除角色
     */
    void batchDeleteRoles(List<Long> ids);

    /**
     * 更新角色状态
     */
    void updateRoleStatus(Long id, Integer status);
}
