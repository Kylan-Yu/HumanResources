package com.hrms.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hrms.auth.entity.User;
import com.hrms.auth.dto.UserQueryDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 用户Mapper
 *
 * @author HRMS
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {

    /**
     * 根据用户名查询用户
     */
    @Select("SELECT id, username, password, real_name, phone AS mobile, email, avatar, status, " +
            "industry_type, last_login_time, " +
            "created_by, created_time, updated_by, updated_time, deleted " +
            "FROM sys_user WHERE username = #{username} AND deleted = 0")
    User findByUsername(@Param("username") String username);

    /**
     * 根据用户ID查询权限
     */
    @Select("SELECT DISTINCT m.permission " +
            "FROM sys_menu m " +
            "INNER JOIN sys_role_menu rm ON m.id = rm.menu_id " +
            "INNER JOIN sys_user_role ur ON rm.role_id = ur.role_id " +
            "WHERE ur.user_id = #{userId} AND m.deleted = 0 AND m.status = 1 AND m.permission IS NOT NULL AND m.permission <> ''")
    List<String> findPermissionsByUserId(@Param("userId") Long userId);

    /**
     * 根据用户ID查询角色
     */
    @Select("SELECT r.role_code " +
            "FROM sys_role r " +
            "INNER JOIN sys_user_role ur ON r.id = ur.role_id " +
            "WHERE ur.user_id = #{userId} AND r.deleted = 0 AND r.status = 1")
    List<String> findRolesByUserId(@Param("userId") Long userId);

    /**
     * 分页查询用户
     */
    IPage<User> selectUserPage(Page<User> page, @Param("query") UserQueryDTO query);

    /**
     * 检查用户名是否存在
     */
    @Select("SELECT COUNT(1) FROM sys_user WHERE username = #{username} AND deleted = 0 AND (#{excludeId} IS NULL OR id != #{excludeId})")
    int checkUsernameExists(@Param("username") String username, @Param("excludeId") Long excludeId);

    /**
     * 检查手机号是否存在
     */
    @Select("SELECT COUNT(1) FROM sys_user WHERE phone = #{mobile} AND deleted = 0 AND (#{excludeId} IS NULL OR id != #{excludeId})")
    int checkMobileExists(@Param("mobile") String mobile, @Param("excludeId") Long excludeId);

    /**
     * 检查邮箱是否存在
     */
    @Select("SELECT COUNT(1) FROM sys_user WHERE email = #{email} AND deleted = 0 AND (#{excludeId} IS NULL OR id != #{excludeId})")
    int checkEmailExists(@Param("email") String email, @Param("excludeId") Long excludeId);

    /**
     * 根据角色ID查询用户数量
     */
    @Select("SELECT COUNT(1) FROM sys_user_role WHERE role_id = #{roleId}")
    int countUsersByRoleId(@Param("roleId") Long roleId);
}
