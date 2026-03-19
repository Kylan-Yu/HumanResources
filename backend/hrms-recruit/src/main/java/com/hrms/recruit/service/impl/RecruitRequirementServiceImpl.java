package com.hrms.recruit.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hrms.common.exception.BusinessException;
import com.hrms.recruit.dto.RecruitRequirementCreateDTO;
import com.hrms.recruit.dto.RecruitRequirementQueryDTO;
import com.hrms.recruit.dto.RecruitRequirementUpdateDTO;
import com.hrms.recruit.entity.RecruitRequirement;
import com.hrms.recruit.enums.RequirementStatusEnum;
import com.hrms.recruit.mapper.RecruitRequirementMapper;
import com.hrms.recruit.service.RecruitRequirementService;
import com.hrms.recruit.vo.RecruitRequirementVO;
import com.hrms.recruit.convert.RecruitRequirementConvert;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 招聘需求服务实现类
 *
 * @author HRMS
 */
@Service
@RequiredArgsConstructor
public class RecruitRequirementServiceImpl implements RecruitRequirementService {

    private final RecruitRequirementMapper recruitRequirementMapper;
    private final RecruitRequirementConvert recruitRequirementConvert;

    @Override
    public IPage<RecruitRequirementVO> pageRecruitRequirements(RecruitRequirementQueryDTO query) {
        Page<RecruitRequirementVO> page = new Page<>(query.getPageNum(), query.getPageSize());
        RecruitRequirementVO queryVO = recruitRequirementConvert.queryDtoToVo(query);
        return recruitRequirementMapper.selectRecruitRequirementPage(page, queryVO);
    }

    @Override
    public RecruitRequirementVO getRecruitRequirementById(Long id) {
        RecruitRequirementVO requirementVO = recruitRequirementMapper.selectRecruitRequirementById(id);
        if (requirementVO == null) {
            throw new BusinessException("招聘需求不存在");
        }
        return requirementVO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createRecruitRequirement(RecruitRequirementCreateDTO dto) {
        // 生成需求编号
        String requirementNo = generateRequirementNo(dto.getIndustryType());

        // 转换为实体
        RecruitRequirement requirement = recruitRequirementConvert.createDtoToEntity(dto);
        requirement.setRequirementNo(requirementNo);
        requirement.setRequirementStatus(RequirementStatusEnum.DRAFT.getCode());

        // 保存需求
        recruitRequirementMapper.insert(requirement);

        return requirement.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateRecruitRequirement(Long id, RecruitRequirementUpdateDTO dto) {
        RecruitRequirement existingRequirement = recruitRequirementMapper.selectById(id);
        if (existingRequirement == null) {
            throw new BusinessException("招聘需求不存在");
        }

        // 转换为实体
        RecruitRequirement requirement = recruitRequirementConvert.updateDtoToEntity(dto);
        requirement.setId(id);

        int result = recruitRequirementMapper.updateById(requirement);
        return result > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteRecruitRequirement(Long id) {
        RecruitRequirement requirement = recruitRequirementMapper.selectById(id);
        if (requirement == null) {
            throw new BusinessException("招聘需求不存在");
        }

        // 逻辑删除
        int result = recruitRequirementMapper.deleteById(id);
        return result > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateRecruitRequirementStatus(Long id, String status) {
        RecruitRequirement requirement = recruitRequirementMapper.selectById(id);
        if (requirement == null) {
            throw new BusinessException("招聘需求不存在");
        }

        requirement.setRequirementStatus(status);
        int result = recruitRequirementMapper.updateById(requirement);
        return result > 0;
    }

    /**
     * 生成需求编号
     */
    private String generateRequirementNo(String industryType) {
        String prefix = "company".equals(industryType) ? "REQ" : "HREQ";
        String dateStr = LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd"));
        
        // 查询当天已生成的需求数量
        String requirementNo = recruitRequirementMapper.generateRequirementNo(industryType);
        if (requirementNo != null && !requirementNo.isEmpty()) {
            return requirementNo;
        }
        
        return prefix + dateStr + "001";
    }
}
