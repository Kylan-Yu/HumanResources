package com.hrms.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hrms.system.entity.Org;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 组织Mapper
 *
 * @author HRMS
 */
@Mapper
public interface OrgMapper extends BaseMapper<Org> {

    /**
     * 统计组织下的部门数量
     */
    int countDeptsByOrg(@Param("orgId") Long orgId);
}
