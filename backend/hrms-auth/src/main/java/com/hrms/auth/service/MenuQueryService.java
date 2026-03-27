package com.hrms.auth.service;

import com.hrms.auth.entity.Menu;

import java.util.List;

/**
 * Read-only menu query service.
 */
public interface MenuQueryService {

    List<Menu> getAllMenus();

    List<Menu> getCurrentUserMenus();
}
