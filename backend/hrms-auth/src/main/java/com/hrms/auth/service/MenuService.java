package com.hrms.auth.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hrms.auth.dto.MenuCreateDTO;
import com.hrms.auth.entity.Menu;
import com.hrms.auth.vo.MenuVO;

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
    List<MenuVO> getMenuTree();

    /**
     * 根据用户ID获取菜单树
     */
    List<MenuVO> getUserMenuTree(Long userId);

    /**
     * 创建菜单
     */
    Long createMenu(MenuCreateDTO dto);

    /**
     * 更新菜单
     */
    Boolean updateMenu(Long id, MenuCreateDTO dto);

    /**
     * 删除菜单
     */
    Boolean deleteMenu(Long id);

    /**
     * 根据ID获取菜单详情
     */
    MenuVO getMenuById(Long id);

    /**
     * 构建菜单树
     */
    List<MenuVO> buildMenuTree(List<Menu> menus);
}
