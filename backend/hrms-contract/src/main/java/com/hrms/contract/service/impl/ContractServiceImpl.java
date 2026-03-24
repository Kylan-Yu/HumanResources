package com.hrms.contract.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hrms.common.exception.BusinessException;
import com.hrms.contract.convert.ContractConvert;
import com.hrms.contract.dto.ContractCreateDTO;
import com.hrms.contract.dto.ContractQueryDTO;
import com.hrms.contract.dto.ContractRenewDTO;
import com.hrms.contract.dto.ContractUpdateDTO;
import com.hrms.contract.entity.Contract;
import com.hrms.contract.entity.ContractRecord;
import com.hrms.contract.enums.ContractRecordTypeEnum;
import com.hrms.contract.enums.ContractStatusEnum;
import com.hrms.contract.mapper.ContractMapper;
import com.hrms.contract.mapper.ContractRecordMapper;
import com.hrms.contract.service.ContractService;
import com.hrms.contract.vo.ContractRecordVO;
import com.hrms.contract.vo.ContractVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ContractServiceImpl implements ContractService {

    private final ContractMapper contractMapper;
    private final ContractRecordMapper contractRecordMapper;
    private final ContractConvert contractConvert;

    @Override
    public IPage<ContractVO> pageContracts(ContractQueryDTO query) {
        Page<ContractVO> page = new Page<>(query.getPageNum(), query.getPageSize());
        ContractVO queryVO = contractConvert.queryDtoToVo(query);
        return contractMapper.selectContractPage(page, queryVO);
    }

    @Override
    public ContractVO getContractById(Long id) {
        ContractVO contractVO = contractMapper.selectContractById(id);
        if (contractVO == null) {
            throw new BusinessException("合同不存在");
        }

        List<ContractRecordVO> records = contractRecordMapper.selectRecordsByContractId(id);
        contractVO.setRecords(records);

        if (contractVO.getEndDate() != null
                && contractVO.getEndDate().isBefore(LocalDate.now())
                && ContractStatusEnum.ACTIVE.getCode().equals(contractVO.getContractStatus())) {
            contractVO.setContractStatus(ContractStatusEnum.EXPIRED.getCode());
            contractVO.setContractStatusDesc(ContractStatusEnum.EXPIRED.getDescription());
        }
        return contractVO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createContract(ContractCreateDTO dto) {
        checkEmployeeExists(dto.getEmployeeId());

        Contract contract = contractConvert.createDtoToEntity(dto);
        contract.setContractNo(generateContractNo(dto.getIndustryType()));
        contract.setContractStatus(ContractStatusEnum.DRAFT.getCode());
        contract.setRenewCount(0);
        contractMapper.insert(contract);

        createContractRecord(contract.getId(), ContractRecordTypeEnum.CREATE, null, null, "创建合同");
        return contract.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateContract(Long id, ContractUpdateDTO dto) {
        Contract existing = contractMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException("合同不存在");
        }

        Contract contract = contractConvert.updateDtoToEntity(dto);
        contract.setId(id);
        int rows = contractMapper.updateById(contract);
        if (rows > 0) {
            createContractRecord(id, ContractRecordTypeEnum.UPDATE, null, null, "更新合同信息");
        }
        return rows > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteContract(Long id) {
        Contract contract = contractMapper.selectById(id);
        if (contract == null) {
            throw new BusinessException("合同不存在");
        }

        int rows = contractMapper.deleteById(id);
        if (rows > 0) {
            createContractRecord(id, ContractRecordTypeEnum.TERMINATE, null, null, "删除合同");
        }
        return rows > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateContractStatus(Long id, String status) {
        Contract contract = contractMapper.selectById(id);
        if (contract == null) {
            throw new BusinessException("合同不存在");
        }

        String oldStatus = contract.getContractStatus();
        contract.setContractStatus(status);
        int rows = contractMapper.updateById(contract);
        if (rows > 0) {
            createContractRecord(id, ContractRecordTypeEnum.STATUS_CHANGE, oldStatus, status, "状态变更");
        }
        return rows > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean renewContract(Long id, ContractRenewDTO dto) {
        Contract contract = contractMapper.selectById(id);
        if (contract == null) {
            throw new BusinessException("合同不存在");
        }

        LocalDate oldEndDate = contract.getEndDate();
        Integer oldRenewCount = contract.getRenewCount() == null ? 0 : contract.getRenewCount();

        contract.setEndDate(dto.getNewEndDate());
        contract.setSignDate(dto.getNewSignDate());
        contract.setRenewCount(oldRenewCount + 1);
        contract.setContractStatus(ContractStatusEnum.ACTIVE.getCode());

        int rows = contractMapper.updateById(contract);
        if (rows > 0) {
            String oldValue = "原结束日期: " + (oldEndDate == null ? "" : oldEndDate) + ", 续签次数: " + oldRenewCount;
            String newValue = "新结束日期: " + dto.getNewEndDate() + ", 续签次数: " + contract.getRenewCount();
            createContractRecord(id, ContractRecordTypeEnum.RENEW, oldValue, newValue, dto.getRenewReason());
        }
        return rows > 0;
    }

    @Override
    public IPage<ContractVO> pageExpireWarningContracts(Integer pageNum, Integer pageSize, Integer warningDays) {
        return contractMapper.selectExpireWarningPage(new Page<>(pageNum, pageSize), warningDays);
    }

    private String generateContractNo(String industryType) {
        String finalIndustryType = StringUtils.hasText(industryType) ? industryType : "company";
        String contractNo = contractMapper.generateContractNo(finalIndustryType);
        if (StringUtils.hasText(contractNo)) {
            return contractNo;
        }
        String prefix = "company".equals(finalIndustryType) ? "CT" : "HT";
        return prefix + LocalDate.now().format(java.time.format.DateTimeFormatter.BASIC_ISO_DATE) + "001";
    }

    private void checkEmployeeExists(Long employeeId) {
        if (employeeId == null || !contractMapper.existsEmployee(employeeId)) {
            throw new BusinessException("员工不存在");
        }
    }

    private void createContractRecord(Long contractId,
                                      ContractRecordTypeEnum recordType,
                                      String oldValue,
                                      String newValue,
                                      String changeReason) {
        ContractRecord record = new ContractRecord();
        record.setContractId(contractId);
        record.setRecordType(recordType.getCode());
        record.setOldValue(oldValue);
        record.setNewValue(newValue);
        record.setChangeReason(changeReason);
        record.setOperatorId(1L);
        record.setCreatedTime(LocalDateTime.now());
        contractRecordMapper.insert(record);
    }
}
