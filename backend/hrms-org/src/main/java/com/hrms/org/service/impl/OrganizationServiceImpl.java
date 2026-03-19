package com.hrms.org.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hrms.org.dto.OrganizationCreateDTO;
import com.hrms.org.entity.Organization;
import com.hrms.org.mapper.OrganizationMapper;
import com.hrms.org.service.OrganizationService;
import com.hrms.org.vo.OrganizationVO;
import com.hrms.org.convert.OrganizationConvert;
import com.hrms.common.exception.BusinessException;
import com.hrms.common.ResultCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 组织服务实现类
 *
 * @author HRMS
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrganizationServiceImpl extends ServiceImpl<OrganizationMapper, Organization> implements OrganizationService {

    private final OrganizationConvert organizationConvert;

    @Override
    public List<OrganizationVO> getOrganizationTree() {
        List<Organization> organizations = baseMapper.findAllOrganizations();
        return buildOrganizationTree(organizations);
    }

    @Override
    public List<OrganizationVO> listOrganizations(String orgName, String orgType, Integer status, String industryType) {
        // TODO: 实现分页查询逻辑
        return getOrganizationTree();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createOrganization(OrganizationCreateDTO dto) {
        // 检查组织编码是否存在
        if (baseMapper.checkOrgCodeExists(dto.getOrgCode(), null) > 0) {
            throw new BusinessException(ResultCode.ORG_CODE_ALREADY_EXISTS);
        }

        Organization organization = organizationConvert.toEntity(dto);
        organization.setStatus(1);
        
        save(organization);
        return organization.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateOrganization(Long id, OrganizationCreateDTO dto) {
        Organization existingOrg = getById(id);
        if (existingOrg == null) {
            throw new BusinessException(ResultCode.ORG_NOT_FOUND);
        }

        // 检查组织编码是否存在
        if (baseMapper.checkOrgCodeExists(dto.getOrgCode(), id) > 0) {
            throw new BusinessException(ResultCode.ORG_CODE_ALREADY_EXISTS);
        }

        BeanUtils.copyProperties(dto, existingOrg, "id", "createdTime", "createdBy");
        updateById(existingOrg);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteOrganization(Long id) {
        Organization organization = getById(id);
        if (organization == null) {
            throw new BusinessException(ResultCode.ORG_NOT_FOUND);
        }

        // 检查是否有子组织
        if (baseMapper.countChildrenByParentId(id) > 0) {
            throw new BusinessException(ResultCode.ORG_HAS_CHILDREN);
        }

        // 检查是否有部门
        if (baseMapper.countDepartmentsByOrgId(id) > 0) {
            throw new BusinessException(ResultCode.ORG_HAS_DEPARTMENTS);
        }

        // 检查是否有员工
        if (baseMapper.countEmployeesByOrgId(id) > 0) {
            throw new BusinessException(ResultCode.ORG_HAS_EMPLOYEES);
        }

        return removeById(id);
    }

    @Override
    public OrganizationVO getOrganizationById(Long id) {
        Organization organization = getById(id);
        if (organization == null) {
            throw new BusinessException(ResultCode.ORG_NOT_FOUND);
        }
        
        OrganizationVO vo = organizationConvert.toVO(organization);
        
        // TODO: 查询关联信息（负责人名称、部门数量、员工数量等）
        
        return vo;
    }

    @Override
    public Boolean updateOrganizationStatus(Long id, Integer status) {
        Organization organization = getById(id);
        if (organization == null) {
            throw new BusinessException(ResultCode.ORG_NOT_FOUND);
        }
        
        organization.setStatus(status);
        return updateById(organization);
    }

    @Override
    public List<OrganizationVO> buildOrganizationTree(List<Organization> organizations) {
        // 转换为VO
        List<OrganizationVO> voList = organizations.stream()
                .map(organizationConvert::toVO)
                .collect(Collectors.toList());

        // 构建父子关系映射
        Map<Long, List<OrganizationVO>> childrenMap = voList.stream()
                .filter(vo -> vo.getParentId() != null && vo.getParentId() != 0)
                .collect(Collectors.groupingBy(OrganizationVO::getParentId));

        // 设置子节点
        voList.forEach(vo -> {
            List<OrganizationVO> children = childrenMap.get(vo.getId());
            if (children != null && !children.isEmpty()) {
                vo.setChildren(children);
            }
        });

        // 返回根节点
        return voList.stream()
                .filter(vo -> vo.getParentId() == null || vo.getParentId() == 0)
                .collect(Collectors.toList());
    }
}
