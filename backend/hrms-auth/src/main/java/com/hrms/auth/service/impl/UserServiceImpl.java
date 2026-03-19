package com.hrms.auth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hrms.auth.dto.UserCreateDTO;
import com.hrms.auth.dto.UserUpdateDTO;
import com.hrms.auth.dto.UserQueryDTO;
import com.hrms.auth.dto.AssignRoleDTO;
import com.hrms.auth.entity.User;
import com.hrms.auth.mapper.UserMapper;
import com.hrms.auth.service.UserService;
import com.hrms.auth.vo.UserVO;
import com.hrms.auth.vo.RoleVO;
import com.hrms.auth.convert.UserConvert;
import com.hrms.common.exception.BusinessException;
import com.hrms.common.ResultCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户服务实现类
 *
 * @author HRMS
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private final PasswordEncoder passwordEncoder;
    private final UserConvert userConvert;

    @Override
    public IPage<UserVO> pageUsers(UserQueryDTO query) {
        Page<User> page = new Page<>(query.getPageNum(), query.getPageSize());
        IPage<User> userPage = baseMapper.selectUserPage(page, query);
        
        return userPage.convert(user -> {
            UserVO vo = userConvert.toVO(user);
            // 查询角色信息
            List<String> roles = baseMapper.findRolesByUserId(user.getId());
            vo.setRoles(roles.stream().map(roleCode -> {
                RoleVO roleVO = new RoleVO();
                roleVO.setRoleCode(roleCode);
                return roleVO;
            }).collect(Collectors.toList()));
            return vo;
        });
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createUser(UserCreateDTO dto) {
        // 检查用户名是否存在
        if (baseMapper.checkUsernameExists(dto.getUsername(), null) > 0) {
            throw new BusinessException(ResultCode.USERNAME_ALREADY_EXISTS);
        }
        
        // 检查手机号是否存在
        if (StringUtils.hasText(dto.getMobile()) && 
            baseMapper.checkMobileExists(dto.getMobile(), null) > 0) {
            throw new BusinessException(ResultCode.MOBILE_ALREADY_EXISTS);
        }
        
        // 检查邮箱是否存在
        if (StringUtils.hasText(dto.getEmail()) && 
            baseMapper.checkEmailExists(dto.getEmail(), null) > 0) {
            throw new BusinessException(ResultCode.EMAIL_ALREADY_EXISTS);
        }

        User user = userConvert.toEntity(dto);
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setStatus(1);
        
        save(user);
        return user.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateUser(Long id, UserUpdateDTO dto) {
        User existingUser = getById(id);
        if (existingUser == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        // 检查手机号是否存在
        if (StringUtils.hasText(dto.getMobile()) && 
            baseMapper.checkMobileExists(dto.getMobile(), id) > 0) {
            throw new BusinessException(ResultCode.MOBILE_ALREADY_EXISTS);
        }
        
        // 检查邮箱是否存在
        if (StringUtils.hasText(dto.getEmail()) && 
            baseMapper.checkEmailExists(dto.getEmail(), id) > 0) {
            throw new BusinessException(ResultCode.EMAIL_ALREADY_EXISTS);
        }

        BeanUtils.copyProperties(dto, existingUser, "id", "username", "password", "createdTime");
        updateById(existingUser);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteUser(Long id) {
        User user = getById(id);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        
        // TODO: 检查是否有关联数据
        
        return removeById(id);
    }

    @Override
    public UserVO getUserById(Long id) {
        User user = getById(id);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        
        UserVO vo = userConvert.toVO(user);
        
        // 查询角色信息
        List<String> roles = baseMapper.findRolesByUserId(id);
        vo.setRoles(roles.stream().map(roleCode -> {
            RoleVO roleVO = new RoleVO();
            roleVO.setRoleCode(roleCode);
            return roleVO;
        }).collect(Collectors.toList()));
        
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean assignRoles(AssignRoleDTO dto) {
        User user = getById(dto.getUserId());
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        // TODO: 实现角色分配逻辑
        // 1. 删除原有角色关联
        // 2. 添加新的角色关联
        
        return true;
    }

    @Override
    public Boolean updateUserStatus(Long id, Integer status) {
        User user = getById(id);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        
        user.setStatus(status);
        return updateById(user);
    }

    @Override
    public Boolean resetPassword(Long id, String newPassword) {
        User user = getById(id);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        
        user.setPassword(passwordEncoder.encode(newPassword));
        return updateById(user);
    }

    @Override
    public void updateLastLoginInfo(Long userId, String loginIp) {
        User user = getById(userId);
        if (user != null) {
            user.setLastLoginTime(LocalDateTime.now());
            // TODO: 设置登录IP字段
            updateById(user);
        }
    }
}
