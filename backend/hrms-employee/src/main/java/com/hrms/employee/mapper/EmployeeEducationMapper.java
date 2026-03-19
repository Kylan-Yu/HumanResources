package com.hrms.employee.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hrms.employee.entity.EmployeeEducation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 员工教育经历Mapper
 *
 * @author HRMS
 */
@Mapper
public interface EmployeeEducationMapper extends BaseMapper<EmployeeEducation> {

    /**
     * 根据员工ID查询教育经历
     */
    @Select("SELECT id, employee_id, school_name, education_level, major, start_date, end_date, " +
            "is_highest, degree_type, graduation_certificate, remark, created_time, updated_time " +
            "FROM hr_employee_education WHERE employee_id = #{employeeId} AND deleted = 0 ORDER BY is_highest DESC, start_date DESC")
    List<EmployeeEducation> findByEmployeeId(@Param("employeeId") Long employeeId);
}
