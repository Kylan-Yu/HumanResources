package com.hrms.org.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hrms.org.entity.Organization;
import com.hrms.org.vo.OrganizationVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 组织Mapper
 *
 * @author HRMS
 */
@Mapper
public interface OrganizationMapper extends BaseMapper<Organization> {

    /**
     * 分页查询组织
     */
    IPage<OrganizationVO> selectOrganizationPage(Page<OrganizationVO> page, @Param("orgName") String orgName, 
                                                @Param("orgType") String orgType, @Param("status") Integer status,
                                                @Param("industryType") String industryType);

    /**
     * 获取组织树
     */
    @Select("SELECT id, org_code, org_name, org_type, parent_id, industry_type, " +
            "leader_id, contact_phone, address, status, created_time, updated_time " +
            "FROM hr_org WHERE deleted = 0 ORDER BY org_code")
    List<Organization> findAllOrganizations();

    /**
     * 检查组织编码是否存在
     */
    @Select("SELECT COUNT(1) FROM hr_org WHERE org_code = #{orgCode} AND deleted = 0 AND id != #{excludeId}")
    int checkOrgCodeExists(@Param("orgCode") String orgCode, @Param("excludeId") Long excludeId);

    /**
     * 检查是否有子组织
     */
    @Select("SELECT COUNT(1) FROM hr_org WHERE parent_id = #{parentId} AND deleted = 0")
    int countChildrenByParentId(@Param("parentId") Long parentId);

    /**
     * 检查是否有部门
     */
    @Select("SELECT COUNT(1) FROM hr_department WHERE org_id = #{orgId} AND deleted = 0")
    int countDepartmentsByOrgId(@Param("orgId") Long orgId);

    /**
     * 检查是否有员工
     */
    @Select("SELECT COUNT(1) FROM hr_employee WHERE org_id = #{orgId} AND deleted = 0")
    int countEmployeesByOrgId(@Param("orgId") Long orgId);

    /**
     * 根据用户ID获取可访问的组织列表
     */
    List<Organization> findOrganizationsByUserId(@Param("userId") Long userId);
}
