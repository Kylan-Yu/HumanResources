package com.hrms.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hrms.system.entity.Position;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 岗位Mapper
 *
 * @author HRMS
 */
@Mapper
public interface PositionMapper extends BaseMapper<Position> {

    /**
     * 分页查询岗位
     */
    IPage<Position> selectPositionPage(Page<Position> page, @Param("positionName") String positionName, 
                                      @Param("positionCode") String positionCode, @Param("orgId") Long orgId, 
                                      @Param("deptId") Long deptId, @Param("status") Integer status);

    /**
     * 统计岗位下的员工数量
     */
    int countEmployeesByPosition(@Param("positionId") Long positionId);
}
