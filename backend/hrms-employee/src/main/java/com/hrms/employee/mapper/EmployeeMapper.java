package com.hrms.employee.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hrms.employee.dto.EmployeeQueryDTO;
import com.hrms.employee.entity.Employee;
import com.hrms.employee.vo.EmployeeVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 员工Mapper
 *
 * @author HRMS
 */
@Mapper
public interface EmployeeMapper extends BaseMapper<Employee> {

    /**
     * 分页查询员工
     */
    IPage<EmployeeVO> selectEmployeePage(Page<EmployeeVO> page, @Param("query") EmployeeQueryDTO query);

    /**
     * 检查员工编号是否存在
     */
    @Select("SELECT COUNT(1) FROM hr_employee WHERE employee_no = #{employeeNo} AND deleted = 0 AND id != #{excludeId}")
    int checkEmployeeNoExists(@Param("employeeNo") String employeeNo, @Param("excludeId") Long excludeId);

    /**
     * 检查身份证号是否存在
     */
    @Select("SELECT COUNT(1) FROM hr_employee WHERE id_card_no = #{idCardNo} AND deleted = 0 AND id != #{excludeId}")
    int checkIdCardNoExists(@Param("idCardNo") String idCardNo, @Param("excludeId") Long excludeId);

    /**
     * 检查手机号是否存在
     */
    @Select("SELECT COUNT(1) FROM hr_employee WHERE mobile = #{mobile} AND deleted = 0 AND id != #{excludeId}")
    int checkMobileExists(@Param("mobile") String mobile, @Param("excludeId") Long excludeId);

    /**
     * 检查邮箱是否存在
     */
    @Select("SELECT COUNT(1) FROM hr_employee WHERE email = #{email} AND deleted = 0 AND id != #{excludeId}")
    int checkEmailExists(@Param("email") String email, @Param("excludeId") Long excludeId);

    /**
     * 生成员工编号
     */
    @Select("SELECT CONCAT('EMP', DATE_FORMAT(NOW(), '%Y%m%d'), LPAD(IFNULL(MAX(CAST(SUBSTRING(employee_no, 11) AS UNSIGNED)), 0) + 1, 4, '0')) " +
            "FROM hr_employee WHERE employee_no LIKE CONCAT('EMP', DATE_FORMAT(NOW(), '%Y%m%d'), '%') AND deleted = 0")
    String generateEmployeeNo();

    /**
     * 根据部门ID查询员工数量
     */
    @Select("SELECT COUNT(1) FROM hr_employee e " +
            "INNER JOIN hr_employee_job j ON e.id = j.employee_id " +
            "WHERE j.dept_id = #{deptId} AND e.deleted = 0 AND j.deleted = 0")
    int countEmployeesByDeptId(@Param("deptId") Long deptId);
}
