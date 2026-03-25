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
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Menu service implementation.
 */
@Service
@RequiredArgsConstructor
public class MenuServiceImpl extends ServiceImpl<MenuMapper, Menu> implements MenuService {

    private static final long ROOT_PARENT_ID = 0L;

    private final MenuMapper menuMapper;

    @Override
    public List<Menu> getMenuTree() {
        LambdaQueryWrapper<Menu> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Menu::getStatus, 1)
                .eq(Menu::getDeleted, 0)
                .orderByAsc(Menu::getSortOrder)
                .orderByAsc(Menu::getId);

        List<Menu> allMenus = list(wrapper);
        return buildMenuTree(allMenus, ROOT_PARENT_ID);
    }

    @Override
    public List<Menu> getRoleMenus(Long roleId) {
        return menuMapper.selectMenusByRoleId(roleId);
    }

    @Override
    public List<Menu> getUserMenuTree(Long userId) {
        List<Menu> menus = menuMapper.selectUserMenuTree(userId);
        return buildMenuTree(menus, ROOT_PARENT_ID);
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
            throw new RuntimeException("Menu does not exist");
        }

        menu.setUpdatedTime(LocalDateTime.now());
        updateById(menu);
    }

    @Override
    public void deleteMenu(Long id) {
        Menu menu = getById(id);
        if (menu == null) {
            throw new RuntimeException("Menu does not exist");
        }

        LambdaQueryWrapper<Menu> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Menu::getParentId, id)
                .eq(Menu::getDeleted, 0);
        if (count(wrapper) > 0) {
            throw new RuntimeException("Menu has child nodes and cannot be deleted");
        }

        menu.setDeleted(1);
        menu.setUpdatedTime(LocalDateTime.now());
        updateById(menu);
    }

    @Override
    public void updateMenuStatus(Long id, Integer status) {
        Menu menu = getById(id);
        if (menu == null) {
            throw new RuntimeException("Menu does not exist");
        }

        menu.setStatus(status);
        menu.setUpdatedTime(LocalDateTime.now());
        updateById(menu);
    }

    @Override
    public void assignRoleMenus(Long roleId, List<Long> menuIds) {
        menuMapper.deleteRoleMenus(roleId);
        if (menuIds != null && !menuIds.isEmpty()) {
            menuMapper.insertRoleMenus(roleId, menuIds);
        }
    }

    private List<Menu> buildMenuTree(List<Menu> menus, Long parentId) {
        Long normalizedParentId = normalizeParentId(parentId);
        return menus.stream()
                .filter(menu -> Objects.equals(normalizeParentId(menu.getParentId()), normalizedParentId))
                .peek(menu -> menu.setChildren(buildMenuTree(menus, menu.getId())))
                .collect(Collectors.toList());
    }

    private Long normalizeParentId(Long parentId) {
        return parentId == null ? ROOT_PARENT_ID : parentId;
    }
}
