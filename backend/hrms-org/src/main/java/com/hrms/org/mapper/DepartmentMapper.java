package com.hrms.org.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hrms.org.entity.Department;
import com.hrms.org.vo.DepartmentVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 部门Mapper
 *
 * @author HRMS
 */
@Mapper
public interface DepartmentMapper extends BaseMapper<Department> {

    /**
     * 分页查询部门
     */
    IPage<DepartmentVO> selectDepartmentPage(Page<DepartmentVO> page, @Param("deptName") String deptName,
                                           @Param("orgId") Long orgId, @Param("status") Integer status,
                                           @Param("industryType") String industryType);

    /**
     * 获取部门树
     */
    @Select("SELECT id, org_id, parent_id, dept_code, dept_name, dept_type, " +
            "leader_id, sort_no, status, industry_type, created_time, updated_time " +
            "FROM hr_department WHERE deleted = 0 ORDER BY org_id, sort_no")
    List<Department> findAllDepartments();

    /**
     * 根据组织ID获取部门列表
     */
    @Select("SELECT id, org_id, parent_id, dept_code, dept_name, dept_type, " +
            "leader_id, sort_no, status, industry_type, created_time, updated_time " +
            "FROM hr_department WHERE org_id = #{orgId} AND deleted = 0 ORDER BY sort_no")
    List<Department> findDepartmentsByOrgId(@Param("orgId") Long orgId);

    /**
     * 检查部门编码是否存在
     */
    @Select("SELECT COUNT(1) FROM hr_department WHERE dept_code = #{deptCode} AND org_id = #{orgId} AND deleted = 0 AND id != #{excludeId}")
    int checkDeptCodeExists(@Param("deptCode") String deptCode, @Param("orgId") Long orgId, @Param("excludeId") Long excludeId);

    /**
     * 检查是否有子部门
     */
    @Select("SELECT COUNT(1) FROM hr_department WHERE parent_id = #{parentId} AND deleted = 0")
    int countChildrenByParentId(@Param("parentId") Long parentId);

    /**
     * 检查是否有岗位
     */
    @Select("SELECT COUNT(1) FROM hr_position WHERE dept_id = #{deptId} AND deleted = 0")
    int countPositionsByDeptId(@Param("deptId") Long deptId);

    /**
     * 检查是否有员工
     */
    @Select("SELECT COUNT(1) FROM hr_employee_job WHERE dept_id = #{deptId} AND deleted = 0")
    int countEmployeesByDeptId(@Param("deptId") Long deptId);
}
