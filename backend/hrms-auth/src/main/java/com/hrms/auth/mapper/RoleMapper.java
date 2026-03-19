package com.hrms.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hrms.auth.entity.Role;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 角色Mapper
 *
 * @author HRMS
 */
@Mapper
public interface RoleMapper extends BaseMapper<Role> {

    /**
     * 分页查询角色
     */
    IPage<Role> selectRolePage(Page<Role> page, @Param("roleName") String roleName, @Param("status") Integer status);

    /**
     * 检查角色编码是否存在
     */
    @Select("SELECT COUNT(1) FROM sys_role WHERE role_code = #{roleCode} AND deleted = 0 AND id != #{excludeId}")
    int checkRoleCodeExists(@Param("roleCode") String roleCode, @Param("excludeId") Long excludeId);

    /**
     * 根据用户ID查询角色列表
     */
    @Select("SELECT r.* FROM sys_role r " +
            "INNER JOIN sys_user_role ur ON r.id = ur.role_id " +
            "WHERE ur.user_id = #{userId} AND r.deleted = 0 AND r.status = 1")
    List<Role> findRolesByUserId(@Param("userId") Long userId);

    /**
     * 根据角色ID查询菜单ID列表
     */
    @Select("SELECT menu_id FROM sys_role_menu WHERE role_id = #{roleId}")
    List<Long> findMenuIdsByRoleId(@Param("roleId") Long roleId);

    /**
     * 根据用户ID查询菜单ID列表
     */
    @Select("SELECT DISTINCT rm.menu_id FROM sys_role_menu rm " +
            "INNER JOIN sys_user_role ur ON rm.role_id = ur.role_id " +
            "WHERE ur.user_id = #{userId}")
    List<Long> findMenuIdsByUserId(@Param("userId") Long userId);
}
