package com.hrms.recruit.convert;

import com.hrms.recruit.dto.RecruitRequirementCreateDTO;
import com.hrms.recruit.dto.RecruitRequirementQueryDTO;
import com.hrms.recruit.dto.RecruitRequirementUpdateDTO;
import com.hrms.recruit.entity.RecruitRequirement;
import com.hrms.recruit.enums.RequirementStatusEnum;
import com.hrms.recruit.vo.RecruitRequirementVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.springframework.stereotype.Component;

/**
 * 招聘需求转换器
 *
 * @author HRMS
 */
@Mapper
@Component
public interface RecruitRequirementConvert {

    /**
     * 创建DTO转实体
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "requirementNo", ignore = true)
    @Mapping(target = "requirementStatus", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "createdTime", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "updatedTime", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    RecruitRequirement createDtoToEntity(RecruitRequirementCreateDTO dto);

    /**
     * 更新DTO转实体
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "requirementNo", ignore = true)
    @Mapping(target = "requirementStatus", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "createdTime", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "updatedTime", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    RecruitRequirement updateDtoToEntity(RecruitRequirementUpdateDTO dto);

    /**
     * 实体转VO
     */
    @Mapping(target = "orgName", ignore = true)
    @Mapping(target = "deptName", ignore = true)
    @Mapping(target = "positionName", ignore = true)
    @Mapping(target = "urgencyLevelDesc", expression = "java(getUrgencyLevelDesc(entity.getUrgencyLevel()))")
    @Mapping(target = "requirementStatusDesc", expression = "java(getRequirementStatusDesc(entity.getRequirementStatus()))")
    @Mapping(target = "industryTypeDesc", expression = "java(getIndustryTypeDesc(entity.getIndustryType()))")
    @Mapping(target = "positions", ignore = true)
    RecruitRequirementVO entityToVo(RecruitRequirement entity);

    /**
     * 查询DTO转VO
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "requirementNo", ignore = true)
    @Mapping(target = "orgName", ignore = true)
    @Mapping(target = "orgId", source = "orgId")
    @Mapping(target = "deptName", ignore = true)
    @Mapping(target = "deptId", source = "deptId")
    @Mapping(target = "positionName", ignore = true)
    @Mapping(target = "positionId", source = "positionId")
    @Mapping(target = "headcount", ignore = true)
    @Mapping(target = "urgencyLevel", source = "urgencyLevel")
    @Mapping(target = "urgencyLevelDesc", ignore = true)
    @Mapping(target = "requirementStatus", source = "requirementStatus")
    @Mapping(target = "requirementStatusDesc", ignore = true)
    @Mapping(target = "expectedEntryDate", source = "expectedEntryDateBegin")
    @Mapping(target = "reason", ignore = true)
    @Mapping(target = "industryType", source = "industryType")
    @Mapping(target = "industryTypeDesc", ignore = true)
    @Mapping(target = "extJson", ignore = true)
    @Mapping(target = "remark", ignore = true)
    @Mapping(target = "createdTime", ignore = true)
    @Mapping(target = "updatedTime", ignore = true)
    @Mapping(target = "positions", ignore = true)
    RecruitRequirementVO queryDtoToVo(RecruitRequirementQueryDTO dto);

    /**
     * 获取紧急程度描述
     */
    default String getUrgencyLevelDesc(String urgencyLevel) {
        if (urgencyLevel == null) {
            return "";
        }
        switch (urgencyLevel) {
            case "HIGH":
                return "高";
            case "MEDIUM":
                return "中";
            case "LOW":
                return "低";
            default:
                return urgencyLevel;
        }
    }

    /**
     * 获取需求状态描述
     */
    default String getRequirementStatusDesc(String requirementStatus) {
        if (requirementStatus == null) {
            return "";
        }
        try {
            return RequirementStatusEnum.fromCode(requirementStatus).getDescription();
        } catch (Exception e) {
            return requirementStatus;
        }
    }

    /**
     * 获取行业类型描述
     */
    default String getIndustryTypeDesc(String industryType) {
        if (industryType == null) {
            return "";
        }
        switch (industryType) {
            case "company":
                return "企业";
            case "hospital":
                return "医院";
            default:
                return industryType;
        }
    }
}
