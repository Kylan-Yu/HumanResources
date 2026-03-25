package com.hrms.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hrms.system.entity.Menu;

import java.util.List;

/**
 * Menu service.
 */
public interface MenuService extends IService<Menu> {

    List<Menu> getMenuTree();

    List<Menu> getRoleMenus(Long roleId);

    List<Menu> getUserMenuTree(Long userId);

    void createMenu(Menu menu);

    void updateMenu(Menu menu);

    void deleteMenu(Long id);

    void updateMenuStatus(Long id, Integer status);

    void assignRoleMenus(Long roleId, List<Long> menuIds);
}
