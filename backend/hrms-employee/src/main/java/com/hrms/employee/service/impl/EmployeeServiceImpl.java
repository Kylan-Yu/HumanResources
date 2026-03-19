package com.hrms.employee.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hrms.common.exception.BusinessException;
import com.hrms.common.ResultCode;
import com.hrms.employee.dto.EmployeeCreateDTO;
import com.hrms.employee.dto.EmployeeUpdateDTO;
import com.hrms.employee.dto.EmployeeQueryDTO;
import com.hrms.employee.entity.Employee;
import com.hrms.employee.entity.EmployeeJob;
import com.hrms.employee.enums.EmployeeStatusEnum;
import com.hrms.employee.mapper.EmployeeMapper;
import com.hrms.employee.mapper.EmployeeJobMapper;
import com.hrms.employee.service.EmployeeService;
import com.hrms.employee.vo.EmployeeVO;
import com.hrms.employee.convert.EmployeeConvert;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.List;

/**
 * 员工服务实现类
 *
 * @author HRMS
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmployeeServiceImpl extends ServiceImpl<EmployeeMapper, Employee> implements EmployeeService {

    private final EmployeeConvert employeeConvert;
    private final EmployeeJobMapper employeeJobMapper;

    @Override
    public IPage<EmployeeVO> pageEmployees(EmployeeQueryDTO query) {
        Page<EmployeeVO> page = new Page<>(query.getPageNum(), query.getPageSize());
        return baseMapper.selectEmployeePage(page, query);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createEmployee(EmployeeCreateDTO dto) {
        // 检查身份证号是否存在
        if (StringUtils.hasText(dto.getIdCardNo()) && 
            baseMapper.checkIdCardNoExists(dto.getIdCardNo(), null) > 0) {
            throw new BusinessException(ResultCode.ID_CARD_NO_ALREADY_EXISTS);
        }

        // 检查手机号是否存在
        if (StringUtils.hasText(dto.getMobile()) && 
            baseMapper.checkMobileExists(dto.getMobile(), null) > 0) {
            throw new BusinessException(ResultCode.MOBILE_ALREADY_EXISTS);
        }

        // 检查邮箱是否存在
        if (StringUtils.hasText(dto.getEmail()) && 
            baseMapper.checkEmailExists(dto.getEmail(), null) > 0) {
            throw new BusinessException(ResultCode.EMAIL_ALREADY_EXISTS);
        }

        // 生成员工编号
        String employeeNo = generateEmployeeNo();

        // 创建员工基础信息
        Employee employee = employeeConvert.toEntity(dto);
        employee.setEmployeeNo(employeeNo);
        save(employee);

        // 如果有任职信息，创建主任职
        if (dto.getJobInfo() != null) {
            EmployeeJob job = new EmployeeJob();
            BeanUtils.copyProperties(dto.getJobInfo(), job);
            job.setEmployeeId(employee.getId());
            job.setIsMainJob(1);
            job.setStatus(1);
            employeeJobMapper.insert(job);
        }

        return employee.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateEmployee(Long id, EmployeeUpdateDTO dto) {
        Employee existingEmployee = getById(id);
        if (existingEmployee == null) {
            throw new BusinessException(ResultCode.EMPLOYEE_NOT_FOUND);
        }

        // 检查身份证号是否存在
        if (StringUtils.hasText(dto.getIdCardNo()) && 
            baseMapper.checkIdCardNoExists(dto.getIdCardNo(), id) > 0) {
            throw new BusinessException(ResultCode.ID_CARD_NO_ALREADY_EXISTS);
        }

        // 检查手机号是否存在
        if (StringUtils.hasText(dto.getMobile()) && 
            baseMapper.checkMobileExists(dto.getMobile(), id) > 0) {
            throw new BusinessException(ResultCode.MOBILE_ALREADY_EXISTS);
        }

        // 检查邮箱是否存在
        if (StringUtils.hasText(dto.getEmail()) && 
            baseMapper.checkEmailExists(dto.getEmail(), id) > 0) {
            throw new BusinessException(ResultCode.EMAIL_ALREADY_EXISTS);
        }

        // 记录状态变更
        Integer oldStatus = existingEmployee.getEmployeeStatus();
        Integer newStatus = dto.getEmployeeStatus();
        
        if (oldStatus != null && newStatus != null && !oldStatus.equals(newStatus)) {
            // TODO: 创建异动记录
            log.info("员工状态变更：员工ID={}, 原状态={}, 新状态={}", id, oldStatus, newStatus);
        }

        employeeConvert.updateEntity(dto, existingEmployee);
        updateById(existingEmployee);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteEmployee(Long id) {
        Employee employee = getById(id);
        if (employee == null) {
            throw new BusinessException(ResultCode.EMPLOYEE_NOT_FOUND);
        }

        // TODO: 检查是否有关联数据

        return removeById(id);
    }

    @Override
    public EmployeeVO getEmployeeById(Long id) {
        Employee employee = getById(id);
        if (employee == null) {
            throw new BusinessException(ResultCode.EMPLOYEE_NOT_FOUND);
        }

        EmployeeVO vo = employeeConvert.toVO(employee);

        // TODO: 查询关联信息（任职信息、家庭成员、教育经历等）

        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateEmployeeStatus(Long id, Integer status) {
        Employee employee = getById(id);
        if (employee == null) {
            throw new BusinessException(ResultCode.EMPLOYEE_NOT_FOUND);
        }

        Integer oldStatus = employee.getEmployeeStatus();
        if (!oldStatus.equals(status)) {
            // TODO: 创建异动记录
            log.info("员工状态变更：员工ID={}, 原状态={}, 新状态={}", id, oldStatus, status);
        }

        employee.setEmployeeStatus(status);
        return updateById(employee);
    }

    @Override
    public String generateEmployeeNo() {
        String employeeNo = baseMapper.generateEmployeeNo();
        if (employeeNo == null) {
            // 如果生成失败，使用默认格式
            employeeNo = "EMP" + LocalDate.now().toString().replace("-", "") + "0001";
        }
        return employeeNo;
    }
}
