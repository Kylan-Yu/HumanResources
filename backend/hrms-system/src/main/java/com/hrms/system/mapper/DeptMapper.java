package com.hrms.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hrms.system.entity.Dept;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 部门Mapper
 *
 * @author HRMS
 */
@Mapper
public interface DeptMapper extends BaseMapper<Dept> {

    /**
     * 统计部门下的岗位数量
     */
    int countPositionsByDept(@Param("deptId") Long deptId);
}
