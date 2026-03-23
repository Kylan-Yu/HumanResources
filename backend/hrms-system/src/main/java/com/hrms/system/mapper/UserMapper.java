package com.hrms.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hrms.system.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

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
}
