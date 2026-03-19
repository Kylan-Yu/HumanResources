package com.hrms.employee.convert;

import com.hrms.employee.dto.EmployeeCreateDTO;
import com.hrms.employee.dto.EmployeeUpdateDTO;
import com.hrms.employee.entity.Employee;
import com.hrms.employee.vo.EmployeeVO;
import com.hrms.employee.enums.EmployeeStatusEnum;
import com.hrms.employee.enums.GenderEnum;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;

/**
 * 员工转换器
 *
 * @author HRMS
 */
@Component
public class EmployeeConvert {

    /**
     * DTO转Entity
     */
    public Employee toEntity(EmployeeCreateDTO dto) {
        Employee employee = new Employee();
        BeanUtils.copyProperties(dto, employee);
        employee.setEmployeeStatus(1); // 默认在职
        return employee;
    }

    /**
     * Entity转VO
     */
    public EmployeeVO toVO(Employee entity) {
        EmployeeVO vo = new EmployeeVO();
        BeanUtils.copyProperties(entity, vo);
        
        // 设置性别描述
        if (entity.getGender() != null) {
            GenderEnum genderEnum = GenderEnum.getByCode(entity.getGender());
            vo.setGenderDesc(genderEnum != null ? genderEnum.getDesc() : "");
        }
        
        // 设置年龄
        if (entity.getBirthday() != null) {
            vo.setAge(Period.between(entity.getBirthday(), LocalDate.now()).getYears());
        }
        
        // 设置员工状态描述
        if (entity.getEmployeeStatus() != null) {
            EmployeeStatusEnum statusEnum = EmployeeStatusEnum.getByCode(entity.getEmployeeStatus());
            vo.setEmployeeStatusDesc(statusEnum != null ? statusEnum.getDesc() : "");
        }
        
        // 设置行业类型描述
        if (entity.getIndustryType() != null) {
            vo.setIndustryTypeDesc("company".equals(entity.getIndustryType()) ? "企业" : "医院");
        }
        
        return vo;
    }

    /**
     * UpdateDTO转Entity
     */
    public void updateEntity(EmployeeUpdateDTO dto, Employee employee) {
        BeanUtils.copyProperties(dto, employee, "id", "employeeNo", "createdTime", "createdBy");
    }
}
