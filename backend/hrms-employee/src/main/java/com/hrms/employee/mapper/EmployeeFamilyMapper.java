package com.hrms.employee.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hrms.employee.entity.EmployeeFamily;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 员工家庭成员Mapper
 *
 * @author HRMS
 */
@Mapper
public interface EmployeeFamilyMapper extends BaseMapper<EmployeeFamily> {

    /**
     * 根据员工ID查询家庭成员
     */
    @Select("SELECT id, employee_id, name, relationship, gender, birthday, id_card_no, " +
            "mobile, occupation, work_unit, remark, created_time, updated_time " +
            "FROM hr_employee_family WHERE employee_id = #{employeeId} AND deleted = 0 ORDER BY created_time")
    List<EmployeeFamily> findByEmployeeId(@Param("employeeId") Long employeeId);
}
