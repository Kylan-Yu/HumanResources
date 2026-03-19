package com.hrms.contract.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hrms.contract.entity.ContractRecord;
import com.hrms.contract.vo.ContractRecordVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 合同记录Mapper接口
 *
 * @author HRMS
 */
@Mapper
public interface ContractRecordMapper extends BaseMapper<ContractRecord> {

    /**
     * 根据合同ID查询合同记录
     */
    List<ContractRecordVO> selectRecordsByContractId(@Param("contractId") Long contractId);
}
