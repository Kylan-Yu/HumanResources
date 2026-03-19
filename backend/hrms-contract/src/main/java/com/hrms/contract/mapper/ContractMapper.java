package com.hrms.contract.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hrms.contract.entity.Contract;
import com.hrms.contract.vo.ContractVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;

/**
 * 合同Mapper接口
 *
 * @author HRMS
 */
@Mapper
public interface ContractMapper extends BaseMapper<Contract> {

    /**
     * 分页查询合同信息
     */
    IPage<ContractVO> selectContractPage(Page<ContractVO> page, @Param("query") ContractVO query);

    /**
     * 根据ID查询合同详情
     */
    ContractVO selectContractById(@Param("id") Long id);

    /**
     * 生成合同编号
     */
    String generateContractNo(@Param("industryType") String industryType);

    /**
     * 检查合同编号是否存在
     */
    boolean existsByContractNo(@Param("contractNo") String contractNo);

    /**
     * 检查员工是否有生效的合同
     */
    boolean existsActiveContractByEmployeeId(@Param("employeeId") Long employeeId);

    /**
     * 查询即将到期的合同
     */
    IPage<ContractVO> selectExpireWarningPage(Page<ContractVO> page, @Param("warningDays") Integer warningDays);
}
