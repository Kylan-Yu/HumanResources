package com.hrms.system.controller;

import com.hrms.common.Result;
import com.hrms.system.entity.Dict;
import com.hrms.system.entity.DictItem;
import com.hrms.system.service.DictService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

/**
 * 字典控制器
 *
 * @author HRMS
 */
@Tag(name = "字典管理", description = "字典管理相关接口")
@RestController
@RequestMapping("/dicts")
@RequiredArgsConstructor
public class DictController {

    private final DictService dictService;

    @Operation(summary = "获取字典列表", description = "获取所有字典类型列表")
    @GetMapping
    public Result<List<Dict>> listDicts() {
        List<Dict> dicts = dictService.listEnabledDicts();
        return Result.success(dicts);
    }

    @Operation(summary = "获取字典详情", description = "根据ID获取字典详细信息")
    @GetMapping("/{id}")
    public Result<Dict> getDictDetail(@PathVariable Long id) {
        Dict dict = dictService.getById(id);
        return Result.success(dict);
    }

    @Operation(summary = "获取字典项", description = "根据字典类型获取字典项列表")
    @GetMapping("/{dictType}/items")
    public Result<List<DictItem>> getDictItems(@PathVariable String dictType) {
        List<DictItem> items = dictService.getDictItemsByType(dictType);
        return Result.success(items);
    }

    @Operation(summary = "创建字典", description = "创建新字典")
    @PostMapping
    public Result<Void> createDict(@Valid @RequestBody Dict dict) {
        dictService.createDict(dict);
        return Result.success();
    }

    @Operation(summary = "更新字典", description = "更新字典信息")
    @PutMapping("/{id}")
    public Result<Void> updateDict(@PathVariable Long id, @Valid @RequestBody Dict dict) {
        dict.setId(id);
        dictService.updateDict(dict);
        return Result.success();
    }

    @Operation(summary = "删除字典", description = "删除字典")
    @DeleteMapping("/{id}")
    public Result<Void> deleteDict(@PathVariable Long id) {
        dictService.deleteDict(id);
        return Result.success();
    }

    @Operation(summary = "创建字典项", description = "创建新字典项")
    @PostMapping("/items")
    public Result<Void> createDictItem(@Valid @RequestBody DictItem dictItem) {
        dictService.createDictItem(dictItem);
        return Result.success();
    }

    @Operation(summary = "更新字典项", description = "更新字典项信息")
    @PutMapping("/items/{id}")
    public Result<Void> updateDictItem(@PathVariable Long id, @Valid @RequestBody DictItem dictItem) {
        dictItem.setId(id);
        dictService.updateDictItem(dictItem);
        return Result.success();
    }

    @Operation(summary = "删除字典项", description = "删除字典项")
    @DeleteMapping("/items/{id}")
    public Result<Void> deleteDictItem(@PathVariable Long id) {
        dictService.deleteDictItem(id);
        return Result.success();
    }

    @Operation(summary = "更新字典状态", description = "启用或禁用字典")
    @PutMapping("/{id}/status")
    public Result<Void> updateDictStatus(@PathVariable Long id, @RequestParam Integer status) {
        dictService.updateDictStatus(id, status);
        return Result.success();
    }

    @Operation(summary = "更新字典项状态", description = "启用或禁用字典项")
    @PutMapping("/items/{id}/status")
    public Result<Void> updateDictItemStatus(@PathVariable Long id, @RequestParam Integer status) {
        dictService.updateDictItemStatus(id, status);
        return Result.success();
    }
}
