package com.hrms.contract.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hrms.contract.entity.Contract;
import com.hrms.contract.vo.ContractVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ContractMapper extends BaseMapper<Contract> {

    IPage<ContractVO> selectContractPage(Page<ContractVO> page, @Param("query") ContractVO query);

    ContractVO selectContractById(@Param("id") Long id);

    String generateContractNo(@Param("industryType") String industryType);

    boolean existsByContractNo(@Param("contractNo") String contractNo);

    boolean existsActiveContractByEmployeeId(@Param("employeeId") Long employeeId);

    boolean existsEmployee(@Param("employeeId") Long employeeId);

    IPage<ContractVO> selectExpireWarningPage(Page<ContractVO> page, @Param("warningDays") Integer warningDays);
}
