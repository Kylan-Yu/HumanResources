package com.hrms.contract.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hrms.common.exception.BusinessException;
import com.hrms.contract.dto.ContractCreateDTO;
import com.hrms.contract.dto.ContractQueryDTO;
import com.hrms.contract.dto.ContractRenewDTO;
import com.hrms.contract.dto.ContractUpdateDTO;
import com.hrms.contract.entity.Contract;
import com.hrms.contract.entity.ContractRecord;
import com.hrms.contract.enums.ContractRecordTypeEnum;
import com.hrms.contract.enums.ContractStatusEnum;
import com.hrms.contract.enums.ContractTypeEnum;
import com.hrms.contract.mapper.ContractMapper;
import com.hrms.contract.mapper.ContractRecordMapper;
import com.hrms.contract.service.ContractService;
import com.hrms.contract.vo.ContractVO;
import com.hrms.contract.convert.ContractConvert;
import com.hrms.employee.feign.EmployeeFeignClient;
import com.hrms.org.feign.OrgFeignClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * 合同服务实现类
 *
 * @author HRMS
 */
@Service
@RequiredArgsConstructor
public class ContractServiceImpl implements ContractService {

    private final ContractMapper contractMapper;
    private final ContractRecordMapper contractRecordMapper;
    private final ContractConvert contractConvert;
    private final EmployeeFeignClient employeeFeignClient;
    private final OrgFeignClient orgFeignClient;

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

        // 查询合同记录
        List<ContractRecordVO> records = contractRecordMapper.selectRecordsByContractId(id);
        contractVO.setRecords(records);

        // 检查合同状态（如果已过期则标记为EXPIRED）
        if (contractVO.getEndDate() != null && contractVO.getEndDate().isBefore(LocalDate.now())) {
            if (ContractStatusEnum.ACTIVE.getCode().equals(contractVO.getContractStatus())) {
                contractVO.setContractStatus(ContractStatusEnum.EXPIRED.getCode());
                contractVO.setContractStatusDesc(ContractStatusEnum.EXPIRED.getDescription());
            }
        }

        return contractVO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createContract(ContractCreateDTO dto) {
        // 检查员工是否存在
        checkEmployeeExists(dto.getEmployeeId());

        // 生成合同编号
        String contractNo = generateContractNo(dto.getIndustryType());

        // 转换为实体
        Contract contract = contractConvert.createDtoToEntity(dto);
        contract.setContractNo(contractNo);
        contract.setContractStatus(ContractStatusEnum.DRAFT.getCode());
        contract.setRenewCount(0);

        // 保存合同
        contractMapper.insert(contract);

        // 创建合同记录
        createContractRecord(contract.getId(), ContractRecordTypeEnum.CREATE, null, null, "创建合同");

        return contract.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateContract(Long id, ContractUpdateDTO dto) {
        Contract existingContract = contractMapper.selectById(id);
        if (existingContract == null) {
            throw new BusinessException("合同不存在");
        }

        // 转换为实体
        Contract contract = contractConvert.updateDtoToEntity(dto);
        contract.setId(id);

        // 更新合同
        int result = contractMapper.updateById(contract);

        // 创建合同记录
        if (result > 0) {
            createContractRecord(id, ContractRecordTypeEnum.UPDATE, null, null, "更新合同信息");
        }

        return result > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteContract(Long id) {
        Contract contract = contractMapper.selectById(id);
        if (contract == null) {
            throw new BusinessException("合同不存在");
        }

        // 逻辑删除
        int result = contractMapper.deleteById(id);

        // 创建合同记录
        if (result > 0) {
            createContractRecord(id, ContractRecordTypeEnum.TERMINATE, null, null, "删除合同");
        }

        return result > 0;
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

        int result = contractMapper.updateById(contract);

        // 创建合同记录
        if (result > 0) {
            createContractRecord(id, ContractRecordTypeEnum.STATUS_CHANGE, oldStatus, status, "状态变更");
        }

        return result > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean renewContract(Long id, ContractRenewDTO dto) {
        Contract contract = contractMapper.selectById(id);
        if (contract == null) {
            throw new BusinessException("合同不存在");
        }

        // 更新合同信息
        contract.setEndDate(dto.getNewEndDate());
        contract.setSignDate(dto.getNewSignDate());
        contract.setRenewCount(contract.getRenewCount() + 1);
        contract.setContractStatus(ContractStatusEnum.ACTIVE.getCode());

        int result = contractMapper.updateById(contract);

        // 创建合同记录
        if (result > 0) {
            String oldValue = String.format("原结束日期: %s, 续签次数: %d", 
                contract.getEndDate().toString(), contract.getRenewCount() - 1);
            String newValue = String.format("新结束日期: %s, 续签次数: %d", 
                dto.getNewEndDate().toString(), contract.getRenewCount());
            createContractRecord(id, ContractRecordTypeEnum.RENEW, oldValue, newValue, dto.getRenewReason());
        }

        return result > 0;
    }

    @Override
    public IPage<ContractVO> pageExpireWarningContracts(Integer pageNum, Integer pageSize, Integer warningDays) {
        Page<ContractVO> page = new Page<>(pageNum, pageSize);
        return contractMapper.selectExpireWarningPage(page, warningDays);
    }

    /**
     * 生成合同编号
     */
    private String generateContractNo(String industryType) {
        String prefix = "company".equals(industryType) ? "CT" : "HT";
        String dateStr = LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd"));
        
        // 查询当天已生成的合同数量
        String contractNo = contractMapper.generateContractNo(industryType);
        if (StringUtils.hasText(contractNo)) {
            return contractNo;
        }
        
        return prefix + dateStr + "001";
    }

    /**
     * 检查员工是否存在
     */
    private void checkEmployeeExists(Long employeeId) {
        try {
            employeeFeignClient.getEmployeeById(employeeId);
        } catch (Exception e) {
            throw new BusinessException("员工不存在");
        }
    }

    /**
     * 创建合同记录
     */
    private void createContractRecord(Long contractId, ContractRecordTypeEnum recordType, 
                                    String oldValue, String newValue, String changeReason) {
        ContractRecord record = new ContractRecord();
        record.setContractId(contractId);
        record.setRecordType(recordType.getCode());
        record.setOldValue(oldValue);
        record.setNewValue(newValue);
        record.setChangeReason(changeReason);
        record.setOperatorId(1L); // TODO: 从当前登录用户获取
        record.setOperatorName("系统管理员"); // TODO: 从当前登录用户获取
        record.setCreatedTime(LocalDateTime.now());
        
        contractRecordMapper.insert(record);
    }
}
