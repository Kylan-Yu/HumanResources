package com.hrms.contract.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.hrms.contract.dto.ContractCreateDTO;
import com.hrms.contract.dto.ContractQueryDTO;
import com.hrms.contract.dto.ContractRenewDTO;
import com.hrms.contract.dto.ContractUpdateDTO;
import com.hrms.contract.vo.ContractVO;

/**
 * 合同服务接口
 *
 * @author HRMS
 */
public interface ContractService {

    /**
     * 分页查询合同
     */
    IPage<ContractVO> pageContracts(ContractQueryDTO query);

    /**
     * 根据ID查询合同详情
     */
    ContractVO getContractById(Long id);

    /**
     * 创建合同
     */
    Long createContract(ContractCreateDTO dto);

    /**
     * 更新合同
     */
    Boolean updateContract(Long id, ContractUpdateDTO dto);

    /**
     * 删除合同
     */
    Boolean deleteContract(Long id);

    /**
     * 更新合同状态
     */
    Boolean updateContractStatus(Long id, String status);

    /**
     * 续签合同
     */
    Boolean renewContract(Long id, ContractRenewDTO dto);

    /**
     * 查询即将到期的合同
     */
    IPage<ContractVO> pageExpireWarningContracts(Integer pageNum, Integer pageSize, Integer warningDays);
}
