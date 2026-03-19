package com.hrms.auth.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hrms.auth.dto.RoleCreateDTO;
import com.hrms.auth.dto.AssignRoleDTO;
import com.hrms.auth.entity.Role;
import com.hrms.auth.vo.RoleVO;

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
    List<RoleVO> listRoles(String roleName, Integer status);

    /**
     * 创建角色
     */
    Long createRole(RoleCreateDTO dto);

    /**
     * 更新角色
     */
    Boolean updateRole(Long id, RoleCreateDTO dto);

    /**
     * 删除角色
     */
    Boolean deleteRole(Long id);

    /**
     * 根据ID获取角色详情
     */
    RoleVO getRoleById(Long id);

    /**
     * 分配菜单
     */
    Boolean assignMenus(Long roleId, List<Long> menuIds);

    /**
     * 获取角色菜单ID列表
     */
    List<Long> getRoleMenuIds(Long roleId);

    /**
     * 更新角色状态
     */
    Boolean updateRoleStatus(Long id, Integer status);
}
