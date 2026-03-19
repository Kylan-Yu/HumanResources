package com.hrms.payroll.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hrms.common.exception.BusinessException;
import com.hrms.payroll.dto.PayrollStandardCreateDTO;
import com.hrms.payroll.dto.PayrollStandardQueryDTO;
import com.hrms.payroll.dto.PayrollStandardUpdateDTO;
import com.hrms.payroll.entity.PayrollStandard;
import com.hrms.payroll.mapper.PayrollStandardMapper;
import com.hrms.payroll.service.PayrollStandardService;
import com.hrms.payroll.vo.PayrollStandardVO;
import com.hrms.payroll.convert.PayrollStandardConvert;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * 薪资标准服务实现类
 *
 * @author HRMS
 */
@Service
@RequiredArgsConstructor
public class PayrollStandardServiceImpl implements PayrollStandardService {

    private final PayrollStandardMapper payrollStandardMapper;
    private final PayrollStandardConvert payrollStandardConvert;

    @Override
    public IPage<PayrollStandardVO> pagePayrollStandards(PayrollStandardQueryDTO query) {
        Page<PayrollStandardVO> page = new Page<>(query.getPageNum(), query.getPageSize());
        PayrollStandardVO queryVO = payrollStandardConvert.queryDtoToVo(query);
        return payrollStandardMapper.selectPayrollStandardPage(page, queryVO);
    }

    @Override
    public PayrollStandardVO getPayrollStandardById(Long id) {
        PayrollStandardVO standardVO = payrollStandardMapper.selectPayrollStandardById(id);
        if (standardVO == null) {
            throw new BusinessException("薪资标准不存在");
        }
        // 计算总薪资
        calculateTotalSalary(standardVO);
        return standardVO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createPayrollStandard(PayrollStandardCreateDTO dto) {
        // 转换为实体
        PayrollStandard standard = payrollStandardConvert.createDtoToEntity(dto);

        // 保存标准
        payrollStandardMapper.insert(standard);

        return standard.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updatePayrollStandard(Long id, PayrollStandardUpdateDTO dto) {
        PayrollStandard existingStandard = payrollStandardMapper.selectById(id);
        if (existingStandard == null) {
            throw new BusinessException("薪资标准不存在");
        }

        // 转换为实体
        PayrollStandard standard = payrollStandardConvert.updateDtoToEntity(dto);
        standard.setId(id);

        int result = payrollStandardMapper.updateById(standard);
        return result > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deletePayrollStandard(Long id) {
        PayrollStandard standard = payrollStandardMapper.selectById(id);
        if (standard == null) {
            throw new BusinessException("薪资标准不存在");
        }

        // 逻辑删除
        int result = payrollStandardMapper.deleteById(id);
        return result > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updatePayrollStandardStatus(Long id, String status) {
        PayrollStandard standard = payrollStandardMapper.selectById(id);
        if (standard == null) {
            throw new BusinessException("薪资标准不存在");
        }

        standard.setStatus(status);
        int result = payrollStandardMapper.updateById(standard);
        return result > 0;
    }

    @Override
    public PayrollStandardVO getPayrollStandardByEmployeeId(Long employeeId) {
        PayrollStandardVO standardVO = payrollStandardMapper.selectPayrollStandardByEmployeeId(employeeId);
        if (standardVO != null) {
            calculateTotalSalary(standardVO);
        }
        return standardVO;
    }

    /**
     * 计算总薪资
     */
    private void calculateTotalSalary(PayrollStandardVO standardVO) {
        BigDecimal totalSalary = BigDecimal.ZERO;
        
        if (standardVO.getBaseSalary() != null) {
            totalSalary = totalSalary.add(standardVO.getBaseSalary());
        }
        if (standardVO.getPerformanceSalary() != null) {
            totalSalary = totalSalary.add(standardVO.getPerformanceSalary());
        }
        if (standardVO.getPositionAllowance() != null) {
            totalSalary = totalSalary.add(standardVO.getPositionAllowance());
        }
        if (standardVO.getMealAllowance() != null) {
            totalSalary = totalSalary.add(standardVO.getMealAllowance());
        }
        if (standardVO.getTransportAllowance() != null) {
            totalSalary = totalSalary.add(standardVO.getTransportAllowance());
        }
        if (standardVO.getCommunicationAllowance() != null) {
            totalSalary = totalSalary.add(standardVO.getCommunicationAllowance());
        }
        if (standardVO.getHousingAllowance() != null) {
            totalSalary = totalSalary.add(standardVO.getHousingAllowance());
        }
        if (standardVO.getOtherAllowance() != null) {
            totalSalary = totalSalary.add(standardVO.getOtherAllowance());
        }
        
        standardVO.setTotalSalary(totalSalary);
    }
}
