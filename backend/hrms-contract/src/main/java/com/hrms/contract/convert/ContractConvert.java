package com.hrms.contract.convert;

import com.hrms.contract.dto.ContractCreateDTO;
import com.hrms.contract.dto.ContractQueryDTO;
import com.hrms.contract.dto.ContractUpdateDTO;
import com.hrms.contract.entity.Contract;
import com.hrms.contract.enums.ContractStatusEnum;
import com.hrms.contract.enums.ContractTypeEnum;
import com.hrms.contract.vo.ContractVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * 合同转换器
 *
 * @author HRMS
 */
@Mapper
@Component
public interface ContractConvert {

    /**
     * 创建DTO转实体
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "contractNo", ignore = true)
    @Mapping(target = "contractStatus", ignore = true)
    @Mapping(target = "renewCount", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "createdTime", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "updatedTime", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    Contract createDtoToEntity(ContractCreateDTO dto);

    /**
     * 更新DTO转实体
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "employeeId", ignore = true)
    @Mapping(target = "contractNo", ignore = true)
    @Mapping(target = "contractStatus", ignore = true)
    @Mapping(target = "renewCount", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "createdTime", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "updatedTime", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    Contract updateDtoToEntity(ContractUpdateDTO dto);

    /**
     * 实体转VO
     */
    @Mapping(target = "employeeName", ignore = true)
    @Mapping(target = "employeeNo", ignore = true)
    @Mapping(target = "contractTypeDesc", expression = "java(getContractTypeDesc(entity.getContractType()))")
    @Mapping(target = "contractStatusDesc", expression = "java(getContractStatusDesc(entity.getContractStatus()))")
    @Mapping(target = "industryTypeDesc", expression = "java(getIndustryTypeDesc(entity.getIndustryType()))")
    @Mapping(target = "records", ignore = true)
    ContractVO entityToVo(Contract entity);

    /**
     * 查询DTO转VO
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "employeeId", source = "employeeId")
    @Mapping(target = "employeeName", source = "employeeName")
    @Mapping(target = "contractNo", source = "contractNo")
    @Mapping(target = "contractType", source = "contractType")
    @Mapping(target = "contractTypeDesc", ignore = true)
    @Mapping(target = "contractSubject", ignore = true)
    @Mapping(target = "startDate", source = "startDateBegin")
    @Mapping(target = "endDate", ignore = true)
    @Mapping(target = "signDate", ignore = true)
    @Mapping(target = "contractStatus", source = "contractStatus")
    @Mapping(target = "contractStatusDesc", ignore = true)
    @Mapping(target = "renewCount", ignore = true)
    @Mapping(target = "industryType", source = "industryType")
    @Mapping(target = "industryTypeDesc", ignore = true)
    @Mapping(target = "extJson", ignore = true)
    @Mapping(target = "remark", ignore = true)
    @Mapping(target = "createdTime", ignore = true)
    @Mapping(target = "updatedTime", ignore = true)
    @Mapping(target = "records", ignore = true)
    ContractVO queryDtoToVo(ContractQueryDTO dto);

    /**
     * 获取合同类型描述
     */
    default String getContractTypeDesc(String contractType) {
        if (!StringUtils.hasText(contractType)) {
            return "";
        }
        try {
            return ContractTypeEnum.fromCode(contractType).getDescription();
        } catch (Exception e) {
            return contractType;
        }
    }

    /**
     * 获取合同状态描述
     */
    default String getContractStatusDesc(String contractStatus) {
        if (!StringUtils.hasText(contractStatus)) {
            return "";
        }
        try {
            return ContractStatusEnum.fromCode(contractStatus).getDescription();
        } catch (Exception e) {
            return contractStatus;
        }
    }

    /**
     * 获取行业类型描述
     */
    default String getIndustryTypeDesc(String industryType) {
        if (!StringUtils.hasText(industryType)) {
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

    /**
     * 计算合同剩余天数
     */
    default Integer calculateRemainingDays(LocalDate endDate) {
        if (endDate == null) {
            return null;
        }
        LocalDate today = LocalDate.now();
        if (endDate.isBefore(today)) {
            return 0;
        }
        return (int) ChronoUnit.DAYS.between(today, endDate);
    }
}
