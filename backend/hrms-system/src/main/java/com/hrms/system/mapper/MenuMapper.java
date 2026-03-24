package com.hrms.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hrms.system.entity.Menu;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 菜单Mapper
 *
 * @author HRMS
 */
@Mapper
public interface MenuMapper extends BaseMapper<Menu> {

    /**
     * 查询用户菜单树
     */
    List<Menu> selectUserMenuTree(@Param("userId") Long userId);

    /**
     * 查询所有菜单（构建树形结构）
     */
    List<Menu> selectAllMenus();

    /**
     * 根据角色ID查询菜单
     */
    List<Menu> selectMenusByRoleId(@Param("roleId") Long roleId);

    /**
     * 删除角色菜单关联
     */
    void deleteRoleMenus(@Param("roleId") Long roleId);

    /**
     * 插入角色菜单关联
     */
    void insertRoleMenus(@Param("roleId") Long roleId, @Param("menuIds") List<Long> menuIds);
}
