package com.hrms.employee.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hrms.employee.entity.EmployeeAttachment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 员工附件Mapper
 *
 * @author HRMS
 */
@Mapper
public interface EmployeeAttachmentMapper extends BaseMapper<EmployeeAttachment> {

    /**
     * 根据员工ID查询附件
     */
    @Select("SELECT id, employee_id, attachment_type, file_name, file_path, file_size, " +
            "file_type, upload_time, remark, created_time, updated_time " +
            "FROM hr_employee_attachment WHERE employee_id = #{employeeId} AND deleted = 0 ORDER BY upload_time DESC")
    List<EmployeeAttachment> findByEmployeeId(@Param("employeeId") Long employeeId);
}
