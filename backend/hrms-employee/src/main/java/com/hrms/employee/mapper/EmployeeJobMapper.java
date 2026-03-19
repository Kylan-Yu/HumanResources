package com.hrms.employee.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hrms.employee.entity.EmployeeJob;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 员工任职信息Mapper
 *
 * @author HRMS
 */
@Mapper
public interface EmployeeJobMapper extends BaseMapper<EmployeeJob> {

    /**
     * 根据员工ID查询任职信息
     */
    @Select("SELECT id, employee_id, org_id, dept_id, position_id, rank_id, leader_id, " +
            "employee_type, employment_type, entry_date, regular_date, work_location, " +
            "is_main_job, status, created_time, updated_time " +
            "FROM hr_employee_job WHERE employee_id = #{employeeId} AND deleted = 0 ORDER BY is_main_job DESC")
    List<EmployeeJob> findByEmployeeId(@Param("employeeId") Long employeeId);

    /**
     * 根据员工ID查询主任职
     */
    @Select("SELECT id, employee_id, org_id, dept_id, position_id, rank_id, leader_id, " +
            "employee_type, employment_type, entry_date, regular_date, work_location, " +
            "is_main_job, status, created_time, updated_time " +
            "FROM hr_employee_job WHERE employee_id = #{employeeId} AND is_main_job = 1 AND deleted = 0")
    EmployeeJob findMainJobByEmployeeId(@Param("employeeId") Long employeeId);

    /**
     * 检查员工是否有主任职
     */
    @Select("SELECT COUNT(1) FROM hr_employee_job WHERE employee_id = #{employeeId} AND is_main_job = 1 AND deleted = 0 AND id != #{excludeId}")
    int countMainJobByEmployeeId(@Param("employeeId") Long employeeId, @Param("excludeId") Long excludeId);
}
