package com.hrms.org.convert;

import com.hrms.org.dto.OrganizationCreateDTO;
import com.hrms.org.entity.Organization;
import com.hrms.org.vo.OrganizationVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

/**
 * 组织转换器
 *
 * @author HRMS
 */
@Component
public class OrganizationConvert {

    /**
     * DTO转Entity
     */
    public Organization toEntity(OrganizationCreateDTO dto) {
        Organization organization = new Organization();
        BeanUtils.copyProperties(dto, organization);
        return organization;
    }

    /**
     * Entity转VO
     */
    public OrganizationVO toVO(Organization entity) {
        OrganizationVO vo = new OrganizationVO();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }
}
