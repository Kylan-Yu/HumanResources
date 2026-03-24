package com.hrms.contract.convert;

import com.hrms.contract.dto.ContractCreateDTO;
import com.hrms.contract.dto.ContractQueryDTO;
import com.hrms.contract.dto.ContractUpdateDTO;
import com.hrms.contract.entity.Contract;
import com.hrms.contract.enums.ContractStatusEnum;
import com.hrms.contract.enums.ContractTypeEnum;
import com.hrms.contract.vo.ContractVO;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Component
public class ContractConvert {

    public Contract createDtoToEntity(ContractCreateDTO dto) {
        if (dto == null) {
            return null;
        }
        Contract entity = new Contract();
        entity.setEmployeeId(dto.getEmployeeId());
        entity.setContractType(dto.getContractType());
        entity.setContractSubject(dto.getContractSubject());
        entity.setStartDate(dto.getStartDate());
        entity.setEndDate(dto.getEndDate());
        entity.setSignDate(dto.getSignDate());
        entity.setIndustryType(dto.getIndustryType());
        entity.setExtJson(dto.getExtJson());
        entity.setRemark(dto.getRemark());
        return entity;
    }

    public Contract updateDtoToEntity(ContractUpdateDTO dto) {
        if (dto == null) {
            return null;
        }
        Contract entity = new Contract();
        entity.setContractType(dto.getContractType());
        entity.setContractSubject(dto.getContractSubject());
        entity.setStartDate(dto.getStartDate());
        entity.setEndDate(dto.getEndDate());
        entity.setSignDate(dto.getSignDate());
        entity.setIndustryType(dto.getIndustryType());
        entity.setExtJson(dto.getExtJson());
        entity.setRemark(dto.getRemark());
        return entity;
    }

    public ContractVO entityToVo(Contract entity) {
        if (entity == null) {
            return null;
        }
        ContractVO vo = new ContractVO();
        vo.setId(entity.getId());
        vo.setEmployeeId(entity.getEmployeeId());
        vo.setContractNo(entity.getContractNo());
        vo.setContractType(entity.getContractType());
        vo.setContractTypeDesc(getContractTypeDesc(entity.getContractType()));
        vo.setContractSubject(entity.getContractSubject());
        vo.setStartDate(entity.getStartDate());
        vo.setEndDate(entity.getEndDate());
        vo.setSignDate(entity.getSignDate());
        vo.setContractStatus(entity.getContractStatus());
        vo.setContractStatusDesc(getContractStatusDesc(entity.getContractStatus()));
        vo.setRenewCount(entity.getRenewCount());
        vo.setIndustryType(entity.getIndustryType());
        vo.setIndustryTypeDesc(getIndustryTypeDesc(entity.getIndustryType()));
        vo.setExtJson(entity.getExtJson());
        vo.setRemark(entity.getRemark());
        vo.setCreatedTime(entity.getCreatedTime());
        vo.setUpdatedTime(entity.getUpdatedTime());
        return vo;
    }

    public ContractVO queryDtoToVo(ContractQueryDTO dto) {
        if (dto == null) {
            return null;
        }
        ContractVO vo = new ContractVO();
        vo.setEmployeeId(dto.getEmployeeId());
        vo.setEmployeeName(dto.getEmployeeName());
        vo.setContractNo(dto.getContractNo());
        vo.setContractType(dto.getContractType());
        vo.setContractStatus(dto.getContractStatus());
        vo.setStartDate(dto.getStartDateBegin());
        vo.setEndDate(dto.getEndDateBegin());
        vo.setIndustryType(dto.getIndustryType());
        return vo;
    }

    public String getContractTypeDesc(String contractType) {
        if (!StringUtils.hasText(contractType)) {
            return "";
        }
        try {
            return ContractTypeEnum.fromCode(contractType).getDescription();
        } catch (Exception e) {
            return contractType;
        }
    }

    public String getContractStatusDesc(String contractStatus) {
        if (!StringUtils.hasText(contractStatus)) {
            return "";
        }
        try {
            return ContractStatusEnum.fromCode(contractStatus).getDescription();
        } catch (Exception e) {
            return contractStatus;
        }
    }

    public String getIndustryTypeDesc(String industryType) {
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

    public Integer calculateRemainingDays(LocalDate endDate) {
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
