package com.hrms.system.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.hrms.system.entity.User;

/**
 * 用户服务接口
 *
 * @author HRMS
 */
public interface UserService extends IService<User> {

    /**
     * 分页查询用户
     */
    IPage<User> pageUsers(Page<User> page, String username, String realName, Integer status);
}
