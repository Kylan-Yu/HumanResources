package com.hrms.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hrms.system.entity.Role;
import com.hrms.system.mapper.RoleMapper;
import com.hrms.system.service.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 角色服务实现
 *
 * @author HRMS
 */
@Service
@RequiredArgsConstructor
public class RoleServiceImpl extends ServiceImpl<RoleMapper, Role> implements RoleService {

    private final RoleMapper roleMapper;

    @Override
    public IPage<Role> pageRoles(Page<Role> page, String roleName, String roleCode, Integer status) {
        return roleMapper.selectRolePage(page, roleName, roleCode, status);
    }

    @Override
    public List<Role> listEnabledRoles() {
        LambdaQueryWrapper<Role> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Role::getStatus, 1)
                .orderByAsc(Role::getSortOrder)
                .orderByDesc(Role::getCreatedTime);
        return list(wrapper);
    }

    @Override
    public void createRole(Role role) {
        // 检查角色编码是否重复
        LambdaQueryWrapper<Role> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Role::getRoleCode, role.getRoleCode());
        if (count(wrapper) > 0) {
            throw new RuntimeException("角色编码已存在");
        }

        role.setCreatedTime(LocalDateTime.now());
        role.setUpdatedTime(LocalDateTime.now());
        save(role);
    }

    @Override
    public void updateRole(Role role) {
        Role existingRole = getById(role.getId());
        if (existingRole == null) {
            throw new RuntimeException("角色不存在");
        }

        // 如果角色编码发生变化，检查是否重复
        if (!existingRole.getRoleCode().equals(role.getRoleCode())) {
            LambdaQueryWrapper<Role> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Role::getRoleCode, role.getRoleCode())
                    .ne(Role::getId, role.getId());
            if (count(wrapper) > 0) {
                throw new RuntimeException("角色编码已存在");
            }
        }

        role.setUpdatedTime(LocalDateTime.now());
        updateById(role);
    }

    @Override
    public void deleteRole(Long id) {
        Role role = getById(id);
        if (role == null) {
            throw new RuntimeException("角色不存在");
        }

        // 检查是否有用户使用该角色
        if (roleMapper.countUsersByRole(id) > 0) {
            throw new RuntimeException("该角色下还有用户，无法删除");
        }

        // 逻辑删除
        role.setDeleted(1);
        role.setUpdatedTime(LocalDateTime.now());
        updateById(role);
    }

    @Override
    public void batchDeleteRoles(List<Long> ids) {
        for (Long id : ids) {
            deleteRole(id);
        }
    }

    @Override
    public void updateRoleStatus(Long id, Integer status) {
        Role role = getById(id);
        if (role == null) {
            throw new RuntimeException("角色不存在");
        }

        role.setStatus(status);
        role.setUpdatedTime(LocalDateTime.now());
        updateById(role);
    }
}
