package com.hrms.recruit.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.hrms.recruit.dto.RecruitRequirementCreateDTO;
import com.hrms.recruit.dto.RecruitRequirementUpdateDTO;
import com.hrms.recruit.vo.RecruitRequirementVO;

/**
 * 招聘需求服务接口
 *
 * @author HRMS
 */
public interface RecruitRequirementService {

    /**
     * 分页查询招聘需求
     */
    IPage<RecruitRequirementVO> pageRecruitRequirements(RecruitRequirementQueryDTO query);

    /**
     * 根据ID查询招聘需求详情
     */
    RecruitRequirementVO getRecruitRequirementById(Long id);

    /**
     * 创建招聘需求
     */
    Long createRecruitRequirement(RecruitRequirementCreateDTO dto);

    /**
     * 更新招聘需求
     */
    Boolean updateRecruitRequirement(Long id, RecruitRequirementUpdateDTO dto);

    /**
     * 删除招聘需求
     */
    Boolean deleteRecruitRequirement(Long id);

    /**
     * 更新招聘需求状态
     */
    Boolean updateRecruitRequirementStatus(Long id, String status);
}
