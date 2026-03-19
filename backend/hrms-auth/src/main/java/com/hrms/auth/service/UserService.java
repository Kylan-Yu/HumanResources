package com.hrms.auth.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.hrms.auth.dto.UserCreateDTO;
import com.hrms.auth.dto.UserUpdateDTO;
import com.hrms.auth.dto.UserQueryDTO;
import com.hrms.auth.dto.AssignRoleDTO;
import com.hrms.auth.entity.User;
import com.hrms.auth.vo.UserVO;

/**
 * 用户服务接口
 *
 * @author HRMS
 */
public interface UserService extends IService<User> {

    /**
     * 分页查询用户
     */
    IPage<UserVO> pageUsers(UserQueryDTO query);

    /**
     * 创建用户
     */
    Long createUser(UserCreateDTO dto);

    /**
     * 更新用户
     */
    Boolean updateUser(Long id, UserUpdateDTO dto);

    /**
     * 删除用户
     */
    Boolean deleteUser(Long id);

    /**
     * 根据ID获取用户详情
     */
    UserVO getUserById(Long id);

    /**
     * 分配角色
     */
    Boolean assignRoles(AssignRoleDTO dto);

    /**
     * 更新用户状态
     */
    Boolean updateUserStatus(Long id, Integer status);

    /**
     * 重置密码
     */
    Boolean resetPassword(Long id, String newPassword);

    /**
     * 更新最后登录信息
     */
    void updateLastLoginInfo(Long userId, String loginIp);
}
