package com.hrms.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hrms.system.entity.Role;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 角色Mapper
 *
 * @author HRMS
 */
@Mapper
public interface RoleMapper extends BaseMapper<Role> {

    /**
     * 分页查询角色
     */
    IPage<Role> selectRolePage(Page<Role> page, @Param("roleName") String roleName, 
                              @Param("roleCode") String roleCode, @Param("status") Integer status);

    /**
     * 统计角色下的用户数量
     */
    int countUsersByRole(@Param("roleId") Long roleId);
}
