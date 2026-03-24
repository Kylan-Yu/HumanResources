package com.hrms.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hrms.system.entity.Position;
import com.hrms.system.mapper.PositionMapper;
import com.hrms.system.service.PositionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 岗位服务实现
 *
 * @author HRMS
 */
@Service
@RequiredArgsConstructor
public class PositionServiceImpl extends ServiceImpl<PositionMapper, Position> implements PositionService {

    private final PositionMapper positionMapper;

    @Override
    public IPage<Position> pagePositions(Page<Position> page, String positionName, String positionCode, 
                                         Long orgId, Long deptId, Integer status) {
        return positionMapper.selectPositionPage(page, positionName, positionCode, orgId, deptId, status);
    }

    @Override
    public List<Position> getPositionsByOrgId(Long orgId) {
        LambdaQueryWrapper<Position> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Position::getOrgId, orgId)
                .eq(Position::getStatus, 1)
                .eq(Position::getDeleted, 0)
                .orderByAsc(Position::getSortOrder)
                .orderByAsc(Position::getId);
        return list(wrapper);
    }

    @Override
    public List<Position> getPositionsByDeptId(Long deptId) {
        LambdaQueryWrapper<Position> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Position::getDeptId, deptId)
                .eq(Position::getStatus, 1)
                .eq(Position::getDeleted, 0)
                .orderByAsc(Position::getSortOrder)
                .orderByAsc(Position::getId);
        return list(wrapper);
    }

    @Override
    public void createPosition(Position position) {
        // 检查岗位编码是否重复
        LambdaQueryWrapper<Position> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Position::getPositionCode, position.getPositionCode());
        if (count(wrapper) > 0) {
            throw new RuntimeException("岗位编码已存在");
        }

        position.setCreatedTime(LocalDateTime.now());
        position.setUpdatedTime(LocalDateTime.now());
        save(position);
    }

    @Override
    public void updatePosition(Position position) {
        Position existingPosition = getById(position.getId());
        if (existingPosition == null) {
            throw new RuntimeException("岗位不存在");
        }

        // 如果岗位编码发生变化，检查是否重复
        if (!existingPosition.getPositionCode().equals(position.getPositionCode())) {
            LambdaQueryWrapper<Position> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Position::getPositionCode, position.getPositionCode())
                    .ne(Position::getId, position.getId());
            if (count(wrapper) > 0) {
                throw new RuntimeException("岗位编码已存在");
            }
        }

        position.setUpdatedTime(LocalDateTime.now());
        updateById(position);
    }

    @Override
    public void deletePosition(Long id) {
        Position position = getById(id);
        if (position == null) {
            throw new RuntimeException("岗位不存在");
        }

        // 检查是否有员工使用该岗位
        if (positionMapper.countEmployeesByPosition(id) > 0) {
            throw new RuntimeException("该岗位下还有员工，无法删除");
        }

        // 逻辑删除
        position.setDeleted(1);
        position.setUpdatedTime(LocalDateTime.now());
        updateById(position);
    }

    @Override
    public void batchDeletePositions(List<Long> ids) {
        for (Long id : ids) {
            deletePosition(id);
        }
    }

    @Override
    public void updatePositionStatus(Long id, Integer status) {
        Position position = getById(id);
        if (position == null) {
            throw new RuntimeException("岗位不存在");
        }

        position.setStatus(status);
        position.setUpdatedTime(LocalDateTime.now());
        updateById(position);
    }

    @Override
    public List<Position> listEnabledPositions() {
        LambdaQueryWrapper<Position> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Position::getStatus, 1)
                .eq(Position::getDeleted, 0)
                .orderByAsc(Position::getSortOrder)
                .orderByAsc(Position::getId);
        return list(wrapper);
    }
}
