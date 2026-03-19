package com.hrms.recruit.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hrms.recruit.entity.Candidate;
import com.hrms.recruit.vo.CandidateVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 候选人Mapper接口
 *
 * @author HRMS
 */
@Mapper
public interface CandidateMapper extends BaseMapper<Candidate> {

    /**
     * 分页查询候选人
     */
    IPage<CandidateVO> selectCandidatePage(Page<CandidateVO> page, @Param("query") CandidateVO query);

    /**
     * 根据ID查询候选人详情
     */
    CandidateVO selectCandidateById(@Param("id") Long id);

    /**
     * 生成候选人编号
     */
    String generateCandidateNo(@Param("industryType") String industryType);
}
