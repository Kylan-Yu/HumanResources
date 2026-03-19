package com.hrms.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hrms.auth.entity.Menu;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 菜单Mapper
 *
 * @author HRMS
 */
@Mapper
public interface MenuMapper extends BaseMapper<Menu> {

    /**
     * 查询所有菜单
     */
    @Select("SELECT id, parent_id, menu_name, menu_type, route_path, component_path, " +
            "permission_code, icon, sort_no, visible, status, created_time, updated_time, deleted " +
            "FROM sys_menu WHERE deleted = 0 ORDER BY sort_no")
    List<Menu> findAllMenus();

    /**
     * 根据用户ID查询菜单
     */
    @Select("SELECT DISTINCT m.id, m.parent_id, m.menu_name, m.menu_type, m.route_path, m.component_path, " +
            "m.permission_code, m.icon, m.sort_no, m.visible, m.status, m.created_time, m.updated_time, m.deleted " +
            "FROM sys_menu m " +
            "INNER JOIN sys_role_menu rm ON m.id = rm.menu_id " +
            "INNER JOIN sys_user_role ur ON rm.role_id = ur.role_id " +
            "WHERE ur.user_id = #{userId} AND m.deleted = 0 AND m.status = 1 " +
            "ORDER BY m.sort_no")
    List<Menu> findMenusByUserId(@Param("userId") Long userId);

    /**
     * 检查是否有子菜单
     */
    @Select("SELECT COUNT(1) FROM sys_menu WHERE parent_id = #{parentId} AND deleted = 0")
    int countChildrenByParentId(@Param("parentId") Long parentId);

    /**
     * 检查菜单是否被角色使用
     */
    @Select("SELECT COUNT(1) FROM sys_role_menu WHERE menu_id = #{menuId}")
    int countRoleMenuByMenuId(@Param("menuId") Long menuId);
}
