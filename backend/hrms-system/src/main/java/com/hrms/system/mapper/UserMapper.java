package com.hrms.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hrms.system.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 用户Mapper
 *
 * @author HRMS
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {

    /**
     * 分页查询用户（包含角色信息）
     */
    IPage<User> selectUserPageWithRoles(Page<User> page, @Param("username") String username, 
                                       @Param("realName") String realName, @Param("status") Integer status);

    @Select("SELECT id, username, password, real_name, phone, email, avatar, status, last_login_time, last_login_ip, industry_type, ext_json, created_by, created_time, updated_by, updated_time, deleted " +
            "FROM sys_user WHERE username = #{username} AND deleted = 0")
    User findByUsername(@Param("username") String username);

    @Select("SELECT DISTINCT m.permission " +
            "FROM sys_menu m " +
            "INNER JOIN sys_role_menu rm ON m.id = rm.menu_id " +
            "INNER JOIN sys_user_role ur ON rm.role_id = ur.role_id " +
            "WHERE ur.user_id = #{userId} AND m.deleted = 0 AND m.status = 1 AND m.permission IS NOT NULL AND m.permission <> ''")
    java.util.List<String> findPermissionsByUserId(@Param("userId") Long userId);

    @Select("SELECT DISTINCT r.role_code " +
            "FROM sys_role r " +
            "INNER JOIN sys_user_role ur ON ur.role_id = r.id " +
            "WHERE ur.user_id = #{userId} AND r.deleted = 0 AND r.status = 1")
    java.util.List<String> findRolesByUserId(@Param("userId") Long userId);
}
