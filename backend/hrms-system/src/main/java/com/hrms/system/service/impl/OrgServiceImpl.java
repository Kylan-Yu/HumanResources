package com.hrms.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hrms.system.entity.Org;
import com.hrms.system.mapper.OrgMapper;
import com.hrms.system.service.OrgService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 组织服务实现
 *
 * @author HRMS
 */
@Service
@RequiredArgsConstructor
public class OrgServiceImpl extends ServiceImpl<OrgMapper, Org> implements OrgService {

    private final OrgMapper orgMapper;

    @Override
    public List<Org> getOrgTree() {
        LambdaQueryWrapper<Org> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Org::getStatus, 1)
                .eq(Org::getDeleted, 0)
                .orderByAsc(Org::getSortOrder)
                .orderByAsc(Org::getId);
        
        List<Org> allOrgs = list(wrapper);
        // 暂时返回扁平列表，不构建树形结构
        return allOrgs;
    }

    @Override
    public void createOrg(Org org) {
        // 检查组织编码是否重复
        LambdaQueryWrapper<Org> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Org::getOrgCode, org.getOrgCode());
        if (count(wrapper) > 0) {
            throw new RuntimeException("组织编码已存在");
        }

        org.setCreatedTime(LocalDateTime.now());
        org.setUpdatedTime(LocalDateTime.now());
        save(org);
    }

    @Override
    public void updateOrg(Org org) {
        Org existingOrg = getById(org.getId());
        if (existingOrg == null) {
            throw new RuntimeException("组织不存在");
        }

        // 如果组织编码发生变化，检查是否重复
        if (!existingOrg.getOrgCode().equals(org.getOrgCode())) {
            LambdaQueryWrapper<Org> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Org::getOrgCode, org.getOrgCode())
                    .ne(Org::getId, org.getId());
            if (count(wrapper) > 0) {
                throw new RuntimeException("组织编码已存在");
            }
        }

        org.setUpdatedTime(LocalDateTime.now());
        updateById(org);
    }

    @Override
    public void deleteOrg(Long id) {
        Org org = getById(id);
        if (org == null) {
            throw new RuntimeException("组织不存在");
        }

        // 检查是否有子组织
        LambdaQueryWrapper<Org> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Org::getParentId, id)
                .eq(Org::getDeleted, 0);
        if (count(wrapper) > 0) {
            throw new RuntimeException("存在子组织，无法删除");
        }

        // 检查是否有部门
        if (orgMapper.countDeptsByOrg(id) > 0) {
            throw new RuntimeException("该组织下还有部门，无法删除");
        }

        // 逻辑删除
        org.setDeleted(1);
        org.setUpdatedTime(LocalDateTime.now());
        updateById(org);
    }

    @Override
    public void batchDeleteOrgs(List<Long> ids) {
        for (Long id : ids) {
            deleteOrg(id);
        }
    }

    @Override
    public void updateOrgStatus(Long id, Integer status) {
        Org org = getById(id);
        if (org == null) {
            throw new RuntimeException("组织不存在");
        }

        org.setStatus(status);
        org.setUpdatedTime(LocalDateTime.now());
        updateById(org);
    }

    @Override
    public List<Org> listEnabledOrgs() {
        LambdaQueryWrapper<Org> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Org::getStatus, 1)
                .eq(Org::getDeleted, 0)
                .orderByAsc(Org::getSortOrder)
                .orderByAsc(Org::getId);
        return list(wrapper);
    }

    /**
     * 构建组织树
     */
    private List<Org> buildOrgTree(List<Org> orgs, Long parentId) {
        return orgs.stream()
                .filter(org -> parentId.equals(org.getParentId()))
                .peek(org -> org.setChildren(buildOrgTree(orgs, org.getId())))
                .collect(Collectors.toList());
    }
}
