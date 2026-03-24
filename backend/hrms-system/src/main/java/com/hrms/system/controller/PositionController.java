package com.hrms.system.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hrms.common.Result;
import com.hrms.common.PageResult;
import com.hrms.system.entity.Position;
import com.hrms.system.service.PositionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

/**
 * 岗位控制器
 *
 * @author HRMS
 */
@Tag(name = "岗位管理", description = "岗位管理相关接口")
@RestController
@RequestMapping("/position")
@RequiredArgsConstructor
public class PositionController {

    private final PositionService positionService;

    @Operation(summary = "岗位分页查询", description = "分页查询岗位列表")
    @GetMapping("/page")
    public Result<PageResult<Position>> pagePositions(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String positionName,
            @RequestParam(required = false) String positionCode,
            @RequestParam(required = false) Long orgId,
            @RequestParam(required = false) Long deptId,
            @RequestParam(required = false) Integer status
    ) {
        Page<Position> page = new Page<>(pageNum, pageSize);
        IPage<Position> positionPage = positionService.pagePositions(page, positionName, positionCode, orgId, deptId, status);
        return Result.success(PageResult.of(positionPage));
    }

    @Operation(summary = "根据组织ID获取岗位列表", description = "根据组织ID获取岗位列表")
    @GetMapping("/org/{orgId}")
    public Result<List<Position>> getPositionsByOrgId(@PathVariable Long orgId) {
        List<Position> positions = positionService.getPositionsByOrgId(orgId);
        return Result.success(positions);
    }

    @Operation(summary = "根据部门ID获取岗位列表", description = "根据部门ID获取岗位列表")
    @GetMapping("/dept/{deptId}")
    public Result<List<Position>> getPositionsByDeptId(@PathVariable Long deptId) {
        List<Position> positions = positionService.getPositionsByDeptId(deptId);
        return Result.success(positions);
    }

    @Operation(summary = "获取岗位详情", description = "根据ID获取岗位详细信息")
    @GetMapping("/{id}")
    public Result<Position> getPositionDetail(@PathVariable Long id) {
        Position position = positionService.getById(id);
        return Result.success(position);
    }

    @Operation(summary = "创建岗位", description = "创建新岗位")
    @PostMapping
    public Result<Void> createPosition(@Valid @RequestBody Position position) {
        positionService.createPosition(position);
        return Result.success();
    }

    @Operation(summary = "更新岗位", description = "更新岗位信息")
    @PutMapping("/{id}")
    public Result<Void> updatePosition(@PathVariable Long id, @Valid @RequestBody Position position) {
        position.setId(id);
        positionService.updatePosition(position);
        return Result.success();
    }

    @Operation(summary = "删除岗位", description = "删除岗位")
    @DeleteMapping("/{id}")
    public Result<Void> deletePosition(@PathVariable Long id) {
        positionService.deletePosition(id);
        return Result.success();
    }

    @Operation(summary = "批量删除岗位", description = "批量删除岗位")
    @DeleteMapping("/batch")
    public Result<Void> batchDeletePositions(@RequestBody List<Long> ids) {
        positionService.batchDeletePositions(ids);
        return Result.success();
    }

    @Operation(summary = "更新岗位状态", description = "启用或禁用岗位")
    @PutMapping("/{id}/status")
    public Result<Void> updatePositionStatus(@PathVariable Long id, @RequestParam Integer status) {
        positionService.updatePositionStatus(id, status);
        return Result.success();
    }

    @Operation(summary = "获取所有岗位", description = "获取所有启用的岗位列表")
    @GetMapping("/list")
    public Result<List<Position>> listPositions() {
        List<Position> positions = positionService.listEnabledPositions();
        return Result.success(positions);
    }
}
