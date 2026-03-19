package com.hrms.org.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hrms.org.dto.OrganizationCreateDTO;
import com.hrms.org.entity.Organization;
import com.hrms.org.vo.OrganizationVO;

import java.util.List;

/**
 * 组织服务接口
 *
 * @author HRMS
 */
public interface OrganizationService extends IService<Organization> {

    /**
     * 获取组织树
     */
    List<OrganizationVO> getOrganizationTree();

    /**
     * 分页查询组织
     */
    List<OrganizationVO> listOrganizations(String orgName, String orgType, Integer status, String industryType);

    /**
     * 创建组织
     */
    Long createOrganization(OrganizationCreateDTO dto);

    /**
     * 更新组织
     */
    Boolean updateOrganization(Long id, OrganizationCreateDTO dto);

    /**
     * 删除组织
     */
    Boolean deleteOrganization(Long id);

    /**
     * 根据ID获取组织详情
     */
    OrganizationVO getOrganizationById(Long id);

    /**
     * 更新组织状态
     */
    Boolean updateOrganizationStatus(Long id, Integer status);

    /**
     * 构建组织树
     */
    List<OrganizationVO> buildOrganizationTree(List<Organization> organizations);
}
