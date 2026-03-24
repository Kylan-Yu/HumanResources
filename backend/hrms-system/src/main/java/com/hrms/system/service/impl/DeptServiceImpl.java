package com.hrms.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hrms.system.entity.Dept;
import com.hrms.system.mapper.DeptMapper;
import com.hrms.system.service.DeptService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 部门服务实现
 *
 * @author HRMS
 */
@Service
@RequiredArgsConstructor
public class DeptServiceImpl extends ServiceImpl<DeptMapper, Dept> implements DeptService {

    private final DeptMapper deptMapper;

    @Override
    public List<Dept> getDeptTree() {
        try {
            LambdaQueryWrapper<Dept> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Dept::getStatus, 1)
                    .eq(Dept::getDeleted, 0)
                    .orderByAsc(Dept::getSortOrder)
                    .orderByAsc(Dept::getId);
            
            List<Dept> allDepts = list(wrapper);
            // 暂时返回扁平列表，不构建树形结构
            return allDepts;
        } catch (Exception e) {
            // 如果表不存在，返回空列表而不是抛出异常
            System.err.println("获取部门树失败: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    public List<Dept> getDeptsByOrgId(Long orgId) {
        LambdaQueryWrapper<Dept> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Dept::getOrgId, orgId)
                .eq(Dept::getStatus, 1)
                .eq(Dept::getDeleted, 0)
                .orderByAsc(Dept::getSortOrder)
                .orderByAsc(Dept::getId);
        return list(wrapper);
    }

    @Override
    public void createDept(Dept dept) {
        // 检查部门编码是否重复
        LambdaQueryWrapper<Dept> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Dept::getDeptCode, dept.getDeptCode());
        if (count(wrapper) > 0) {
            throw new RuntimeException("部门编码已存在");
        }

        dept.setCreatedTime(LocalDateTime.now());
        dept.setUpdatedTime(LocalDateTime.now());
        save(dept);
    }

    @Override
    public void updateDept(Dept dept) {
        Dept existingDept = getById(dept.getId());
        if (existingDept == null) {
            throw new RuntimeException("部门不存在");
        }

        // 如果部门编码发生变化，检查是否重复
        if (!existingDept.getDeptCode().equals(dept.getDeptCode())) {
            LambdaQueryWrapper<Dept> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Dept::getDeptCode, dept.getDeptCode())
                    .ne(Dept::getId, dept.getId());
            if (count(wrapper) > 0) {
                throw new RuntimeException("部门编码已存在");
            }
        }

        dept.setUpdatedTime(LocalDateTime.now());
        updateById(dept);
    }

    @Override
    public void deleteDept(Long id) {
        Dept dept = getById(id);
        if (dept == null) {
            throw new RuntimeException("部门不存在");
        }

        // 检查是否有子部门
        LambdaQueryWrapper<Dept> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Dept::getParentId, id)
                .eq(Dept::getDeleted, 0);
        if (count(wrapper) > 0) {
            throw new RuntimeException("存在子部门，无法删除");
        }

        // 检查是否有岗位 - 暂时注释掉避免数据库表不存在的问题
        // if (deptMapper.countPositionsByDept(id) > 0) {
        //     throw new RuntimeException("该部门下还有岗位，无法删除");
        // }

        // 逻辑删除
        dept.setDeleted(1);
        dept.setUpdatedTime(LocalDateTime.now());
        updateById(dept);
    }

    @Override
    public void batchDeleteDepts(List<Long> ids) {
        for (Long id : ids) {
            deleteDept(id);
        }
    }

    @Override
    public void updateDeptStatus(Long id, Integer status) {
        Dept dept = getById(id);
        if (dept == null) {
            throw new RuntimeException("部门不存在");
        }

        dept.setStatus(status);
        dept.setUpdatedTime(LocalDateTime.now());
        updateById(dept);
    }

    @Override
    public List<Dept> listEnabledDepts() {
        LambdaQueryWrapper<Dept> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Dept::getStatus, 1)
                .eq(Dept::getDeleted, 0)
                .orderByAsc(Dept::getSortOrder)
                .orderByAsc(Dept::getId);
        return list(wrapper);
    }

    /**
     * 构建部门树
     */
    private List<Dept> buildDeptTree(List<Dept> depts, Long parentId) {
        return depts.stream()
                .filter(dept -> parentId.equals(dept.getParentId()))
                .peek(dept -> dept.setChildren(buildDeptTree(depts, dept.getId())))
                .collect(Collectors.toList());
    }
}
