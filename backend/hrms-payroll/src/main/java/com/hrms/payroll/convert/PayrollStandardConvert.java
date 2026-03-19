package com.hrms.payroll.convert;

import com.hrms.payroll.dto.PayrollStandardCreateDTO;
import com.hrms.payroll.dto.PayrollStandardQueryDTO;
import com.hrms.payroll.dto.PayrollStandardUpdateDTO;
import com.hrms.payroll.entity.PayrollStandard;
import com.hrms.payroll.vo.PayrollStandardVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.springframework.stereotype.Component;

/**
 * 薪资标准转换器
 *
 * @author HRMS
 */
@Mapper
@Component
public interface PayrollStandardConvert {

    /**
     * 创建DTO转实体
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "createdTime", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "updatedTime", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    PayrollStandard createDtoToEntity(PayrollStandardCreateDTO dto);

    /**
     * 更新DTO转实体
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "createdTime", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "updatedTime", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    PayrollStandard updateDtoToEntity(PayrollStandardUpdateDTO dto);

    /**
     * 实体转VO
     */
    @Mapping(target = "orgName", ignore = true)
    @Mapping(target = "deptName", ignore = true)
    @Mapping(target = "positionName", ignore = true)
    @Mapping(target = "statusDesc", expression = "java(getStatusDesc(entity.getStatus()))")
    @Mapping(target = "industryTypeDesc", expression = "java(getIndustryTypeDesc(entity.getIndustryType()))")
    @Mapping(target = "totalSalary", ignore = true)
    PayrollStandardVO entityToVo(PayrollStandard entity);

    /**
     * 查询DTO转VO
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "standardName", source = "standardName")
    @Mapping(target = "orgName", ignore = true)
    @Mapping(target = "orgId", source = "orgId")
    @Mapping(target = "deptName", ignore = true)
    @Mapping(target = "deptId", source = "deptId")
    @Mapping(target = "positionName", ignore = true)
    @Mapping(target = "positionId", source = "positionId")
    @Mapping(target = "gradeLevel", source = "gradeLevel")
    @Mapping(target = "baseSalary", ignore = true)
    @Mapping(target = "performanceSalary", ignore = true)
    @Mapping(target = "positionAllowance", ignore = true)
    @Mapping(target = "mealAllowance", ignore = true)
    @Mapping(target = "transportAllowance", ignore = true)
    @Mapping(target = "communicationAllowance", ignore = true)
    @Mapping(target = "housingAllowance", ignore = true)
    @Mapping(target = "otherAllowance", ignore = true)
    @Mapping(target = "totalSalary", ignore = true)
    @Mapping(target = "status", source = "status")
    @Mapping(target = "statusDesc", ignore = true)
    @Mapping(target = "industryType", source = "industryType")
    @Mapping(target = "industryTypeDesc", ignore = true)
    @Mapping(target = "extJson", ignore = true)
    @Mapping(target = "remark", ignore = true)
    @Mapping(target = "createdTime", ignore = true)
    @Mapping(target = "updatedTime", ignore = true)
    PayrollStandardVO queryDtoToVo(PayrollStandardQueryDTO dto);

    /**
     * 获取状态描述
     */
    default String getStatusDesc(String status) {
        if (status == null) {
            return "";
        }
        switch (status) {
            case "ACTIVE":
                return "启用";
            case "INACTIVE":
                return "禁用";
            default:
                return status;
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
