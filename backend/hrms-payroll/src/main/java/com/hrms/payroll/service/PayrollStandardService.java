package com.hrms.payroll.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.hrms.payroll.dto.PayrollStandardCreateDTO;
import com.hrms.payroll.dto.PayrollStandardUpdateDTO;
import com.hrms.payroll.vo.PayrollStandardVO;

/**
 * 薪资标准服务接口
 *
 * @author HRMS
 */
public interface PayrollStandardService {

    /**
     * 分页查询薪资标准
     */
    IPage<PayrollStandardVO> pagePayrollStandards(PayrollStandardQueryDTO query);

    /**
     * 根据ID查询薪资标准详情
     */
    PayrollStandardVO getPayrollStandardById(Long id);

    /**
     * 创建薪资标准
     */
    Long createPayrollStandard(PayrollStandardCreateDTO dto);

    /**
     * 更新薪资标准
     */
    Boolean updatePayrollStandard(Long id, PayrollStandardUpdateDTO dto);

    /**
     * 删除薪资标准
     */
    Boolean deletePayrollStandard(Long id);

    /**
     * 更新薪资标准状态
     */
    Boolean updatePayrollStandardStatus(Long id, String status);

    /**
     * 根据员工ID查询适用的薪资标准
     */
    PayrollStandardVO getPayrollStandardByEmployeeId(Long employeeId);
}
