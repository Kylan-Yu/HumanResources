package com.hrms.auth.service.impl;

import com.hrms.auth.entity.Menu;
import com.hrms.auth.mapper.MenuMapper;
import com.hrms.auth.service.MenuQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;

/**
 * Menu query implementation.
 */
@Service
@RequiredArgsConstructor
public class MenuQueryServiceImpl implements MenuQueryService {

    private final MenuMapper menuMapper;

    @Override
    public List<Menu> getAllMenus() {
        return menuMapper.findAllMenus();
    }

    @Override
    public List<Menu> getCurrentUserMenus() {
        Long userId = currentUserId();
        if (userId == null) {
            return menuMapper.findAllMenus();
        }
        return menuMapper.findMenusByUserId(userId);
    }

    private Long currentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return null;
        }
        Object principal = authentication.getPrincipal();
        if (!(principal instanceof Map<?, ?> principalMap)) {
            return null;
        }
        Object idValue = principalMap.get("id");
        if (idValue == null || !StringUtils.hasText(String.valueOf(idValue))) {
            return null;
        }
        try {
            return Long.valueOf(String.valueOf(idValue));
        } catch (NumberFormatException ignored) {
            return null;
        }
    }
}
