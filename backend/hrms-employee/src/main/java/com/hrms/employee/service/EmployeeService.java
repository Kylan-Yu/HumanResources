package com.hrms.employee.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.hrms.employee.dto.EmployeeCreateDTO;
import com.hrms.employee.dto.EmployeeUpdateDTO;
import com.hrms.employee.dto.EmployeeQueryDTO;
import com.hrms.employee.entity.Employee;
import com.hrms.employee.vo.EmployeeVO;

/**
 * 员工服务接口
 *
 * @author HRMS
 */
public interface EmployeeService extends IService<Employee> {

    /**
     * 分页查询员工
     */
    IPage<EmployeeVO> pageEmployees(EmployeeQueryDTO query);

    /**
     * 创建员工
     */
    Long createEmployee(EmployeeCreateDTO dto);

    /**
     * 更新员工
     */
    Boolean updateEmployee(Long id, EmployeeUpdateDTO dto);

    /**
     * 删除员工
     */
    Boolean deleteEmployee(Long id);

    /**
     * 根据ID获取员工详情
     */
    EmployeeVO getEmployeeById(Long id);

    /**
     * 更新员工状态
     */
    Boolean updateEmployeeStatus(Long id, Integer status);

    /**
     * 生成员工编号
     */
    String generateEmployeeNo();
}
