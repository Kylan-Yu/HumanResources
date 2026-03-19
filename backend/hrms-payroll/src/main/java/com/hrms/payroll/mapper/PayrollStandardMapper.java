package com.hrms.payroll.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hrms.payroll.entity.PayrollStandard;
import com.hrms.payroll.vo.PayrollStandardVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 薪资标准Mapper接口
 *
 * @author HRMS
 */
@Mapper
public interface PayrollStandardMapper extends BaseMapper<PayrollStandard> {

    /**
     * 分页查询薪资标准
     */
    IPage<PayrollStandardVO> selectPayrollStandardPage(Page<PayrollStandardVO> page, @Param("query") PayrollStandardVO query);

    /**
     * 根据ID查询薪资标准详情
     */
    PayrollStandardVO selectPayrollStandardById(@Param("id") Long id);

    /**
     * 根据员工ID查询适用的薪资标准
     */
    PayrollStandardVO selectPayrollStandardByEmployeeId(@Param("employeeId") Long employeeId);
}
