package com.hrms.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hrms.system.entity.Menu;
import com.hrms.system.mapper.MenuMapper;
import com.hrms.system.service.MenuService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 菜单服务实现
 *
 * @author HRMS
 */
@Service
@RequiredArgsConstructor
public class MenuServiceImpl extends ServiceImpl<MenuMapper, Menu> implements MenuService {

    private final MenuMapper menuMapper;

    @Override
    public List<Menu> getMenuTree() {
        LambdaQueryWrapper<Menu> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Menu::getStatus, 1)
                .eq(Menu::getDeleted, 0)
                .orderByAsc(Menu::getSortOrder)
                .orderByAsc(Menu::getId);
        
        List<Menu> allMenus = list(wrapper);
        return buildMenuTree(allMenus, 0L);
    }

    @Override
    public List<Menu> getRoleMenus(Long roleId) {
        return menuMapper.selectMenusByRoleId(roleId);
    }

    @Override
    public void createMenu(Menu menu) {
        menu.setCreatedTime(LocalDateTime.now());
        menu.setUpdatedTime(LocalDateTime.now());
        save(menu);
    }

    @Override
    public void updateMenu(Menu menu) {
        Menu existingMenu = getById(menu.getId());
        if (existingMenu == null) {
            throw new RuntimeException("菜单不存在");
        }

        menu.setUpdatedTime(LocalDateTime.now());
        updateById(menu);
    }

    @Override
    public void deleteMenu(Long id) {
        Menu menu = getById(id);
        if (menu == null) {
            throw new RuntimeException("菜单不存在");
        }

        // 检查是否有子菜单
        LambdaQueryWrapper<Menu> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Menu::getParentId, id)
                .eq(Menu::getDeleted, 0);
        if (count(wrapper) > 0) {
            throw new RuntimeException("存在子菜单，无法删除");
        }

        // 逻辑删除
        menu.setDeleted(1);
        menu.setUpdatedTime(LocalDateTime.now());
        updateById(menu);
    }

    @Override
    public void updateMenuStatus(Long id, Integer status) {
        Menu menu = getById(id);
        if (menu == null) {
            throw new RuntimeException("菜单不存在");
        }

        menu.setStatus(status);
        menu.setUpdatedTime(LocalDateTime.now());
        updateById(menu);
    }

    @Override
    public void assignRoleMenus(Long roleId, List<Long> menuIds) {
        // 先删除原有权限
        menuMapper.deleteRoleMenus(roleId);
        
        // 分配新权限
        if (menuIds != null && !menuIds.isEmpty()) {
            menuMapper.insertRoleMenus(roleId, menuIds);
        }
    }

    /**
     * 构建菜单树
     */
    private List<Menu> buildMenuTree(List<Menu> menus, Long parentId) {
        return menus.stream()
                .filter(menu -> parentId.equals(menu.getParentId()))
                .peek(menu -> menu.setChildren(buildMenuTree(menus, menu.getId())))
                .collect(Collectors.toList());
    }
}
