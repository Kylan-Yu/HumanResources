package com.hrms.recruit.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hrms.recruit.entity.RecruitRequirement;
import com.hrms.recruit.vo.RecruitRequirementVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 招聘需求Mapper接口
 *
 * @author HRMS
 */
@Mapper
public interface RecruitRequirementMapper extends BaseMapper<RecruitRequirement> {

    /**
     * 分页查询招聘需求
     */
    IPage<RecruitRequirementVO> selectRecruitRequirementPage(Page<RecruitRequirementVO> page, @Param("query") RecruitRequirementVO query);

    /**
     * 根据ID查询招聘需求详情
     */
    RecruitRequirementVO selectRecruitRequirementById(@Param("id") Long id);

    /**
     * 生成需求编号
     */
    String generateRequirementNo(@Param("industryType") String industryType);
}
