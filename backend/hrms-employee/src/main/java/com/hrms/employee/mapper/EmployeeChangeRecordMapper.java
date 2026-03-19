package com.hrms.employee.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hrms.employee.entity.EmployeeChangeRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 员工异动记录Mapper
 *
 * @author HRMS
 */
@Mapper
public interface EmployeeChangeRecordMapper extends BaseMapper<EmployeeChangeRecord> {

    /**
     * 根据员工ID查询异动记录
     */
    @Select("SELECT id, employee_id, change_type, change_date, before_value, after_value, " +
            "change_reason, approver_id, approve_time, remark, created_time, updated_time " +
            "FROM hr_employee_change_record WHERE employee_id = #{employeeId} AND deleted = 0 ORDER BY change_date DESC")
    List<EmployeeChangeRecord> findByEmployeeId(@Param("employeeId") Long employeeId);
}
