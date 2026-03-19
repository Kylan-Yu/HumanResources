package com.hrms.employee.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hrms.employee.entity.EmployeeWorkExperience;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 员工工作经历Mapper
 *
 * @author HRMS
 */
@Mapper
public interface EmployeeWorkExperienceMapper extends BaseMapper<EmployeeWorkExperience> {

    /**
     * 根据员工ID查询工作经历
     */
    @Select("SELECT id, employee_id, company_name, position, start_date, end_date, " +
            "job_description, resign_reason, witness, witness_mobile, remark, created_time, updated_time " +
            "FROM hr_employee_work_experience WHERE employee_id = #{employeeId} AND deleted = 0 ORDER BY start_date DESC")
    List<EmployeeWorkExperience> findByEmployeeId(@Param("employeeId") Long employeeId);
}
