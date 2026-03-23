package com.hrms.system.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hrms.system.entity.User;
import com.hrms.system.mapper.UserMapper;
import com.hrms.system.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 用户服务实现
 *
 * @author HRMS
 */
@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private final UserMapper userMapper;

    @Override
    public IPage<User> pageUsers(Page<User> page, String username, String realName, Integer status) {
        return userMapper.selectUserPageWithRoles(page, username, realName, status);
    }
}
