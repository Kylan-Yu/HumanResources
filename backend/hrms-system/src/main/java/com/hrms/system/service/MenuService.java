package com.hrms.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hrms.system.entity.Menu;

import java.util.List;

/**
 * 菜单服务接口
 *
 * @author HRMS
 */
public interface MenuService extends IService<Menu> {

    /**
     * 获取菜单树
     */
    List<Menu> getMenuTree();

    /**
     * 获取角色菜单
     */
    List<Menu> getRoleMenus(Long roleId);

    /**
     * 创建菜单
     */
    void createMenu(Menu menu);

    /**
     * 更新菜单
     */
    void updateMenu(Menu menu);

    /**
     * 删除菜单
     */
    void deleteMenu(Long id);

    /**
     * 更新菜单状态
     */
    void updateMenuStatus(Long id, Integer status);

    /**
     * 为角色分配菜单
     */
    void assignRoleMenus(Long roleId, List<Long> menuIds);
}
