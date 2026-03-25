package com.hrms.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hrms.system.entity.Org;
import com.hrms.system.mapper.OrgMapper;
import com.hrms.system.service.OrgService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Organization service implementation.
 */
@Service
@RequiredArgsConstructor
public class OrgServiceImpl extends ServiceImpl<OrgMapper, Org> implements OrgService {

    private static final Long ROOT_PARENT_ID = 0L;

    private final OrgMapper orgMapper;
    private final ObjectMapper objectMapper;

    @Override
    public List<Org> getOrgTree() {
        LambdaQueryWrapper<Org> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Org::getStatus, 1)
                .eq(Org::getDeleted, 0)
                .orderByAsc(Org::getSortOrder)
                .orderByAsc(Org::getId);

        List<Org> allOrgs = list(wrapper);
        allOrgs.forEach(this::fillVirtualProfileFields);
        return buildOrgTree(allOrgs, ROOT_PARENT_ID);
    }

    @Override
    public void createOrg(Org org) {
        LambdaQueryWrapper<Org> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Org::getOrgCode, org.getOrgCode());
        if (count(wrapper) > 0) {
            throw new RuntimeException("组织编码已存在");
        }

        mergeProfileFieldsIntoExtJson(org, null);
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

        if (!Objects.equals(existingOrg.getOrgCode(), org.getOrgCode())) {
            LambdaQueryWrapper<Org> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Org::getOrgCode, org.getOrgCode())
                    .ne(Org::getId, org.getId());
            if (count(wrapper) > 0) {
                throw new RuntimeException("组织编码已存在");
            }
        }

        mergeProfileFieldsIntoExtJson(org, existingOrg);
        org.setUpdatedTime(LocalDateTime.now());
        updateById(org);
    }

    @Override
    public void deleteOrg(Long id) {
        Org org = getById(id);
        if (org == null) {
            throw new RuntimeException("组织不存在");
        }

        LambdaQueryWrapper<Org> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Org::getParentId, id)
                .eq(Org::getDeleted, 0);
        if (count(wrapper) > 0) {
            throw new RuntimeException("存在子组织，无法删除");
        }

        if (orgMapper.countDeptsByOrg(id) > 0) {
            throw new RuntimeException("该组织下还有部门，无法删除");
        }

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
        List<Org> orgs = list(wrapper);
        orgs.forEach(this::fillVirtualProfileFields);
        return orgs;
    }

    private List<Org> buildOrgTree(List<Org> orgs, Long parentId) {
        Long normalizedParentId = normalizeParentId(parentId);
        return orgs.stream()
                .filter(org -> Objects.equals(normalizeParentId(org.getParentId()), normalizedParentId))
                .peek(org -> org.setChildren(buildOrgTree(orgs, org.getId())))
                .collect(Collectors.toList());
    }

    private Long normalizeParentId(Long parentId) {
        return parentId == null ? ROOT_PARENT_ID : parentId;
    }

    private void fillVirtualProfileFields(Org org) {
        Map<String, Object> ext = parseExtJson(org.getExtJson());
        org.setLegalPerson(stringValue(ext.get("legalPerson")));
        org.setUnifiedSocialCreditCode(stringValue(ext.get("unifiedSocialCreditCode")));
        org.setAddress(stringValue(ext.get("address")));
        org.setPhone(stringValue(ext.get("phone")));
        org.setEmail(stringValue(ext.get("email")));
    }

    private void mergeProfileFieldsIntoExtJson(Org org, Org existingOrg) {
        Map<String, Object> ext = existingOrg == null
                ? parseExtJson(org.getExtJson())
                : parseExtJson(existingOrg.getExtJson());

        mergeTextField(ext, "legalPerson", org.getLegalPerson());
        mergeTextField(ext, "unifiedSocialCreditCode", org.getUnifiedSocialCreditCode());
        mergeTextField(ext, "address", org.getAddress());
        mergeTextField(ext, "phone", org.getPhone());
        mergeTextField(ext, "email", org.getEmail());

        org.setExtJson(writeExtJson(ext));
    }

    private void mergeTextField(Map<String, Object> ext, String key, String value) {
        if (value == null) {
            return;
        }
        if (StringUtils.hasText(value)) {
            ext.put(key, value.trim());
        } else {
            ext.remove(key);
        }
    }

    private Map<String, Object> parseExtJson(String extJson) {
        if (!StringUtils.hasText(extJson)) {
            return new LinkedHashMap<>();
        }
        try {
            return objectMapper.readValue(extJson, new TypeReference<>() {
            });
        } catch (Exception ignored) {
            return new LinkedHashMap<>();
        }
    }

    private String writeExtJson(Map<String, Object> ext) {
        if (ext == null || ext.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(ext);
        } catch (Exception ignored) {
            return null;
        }
    }

    private String stringValue(Object value) {
        return value == null ? null : String.valueOf(value);
    }
}
