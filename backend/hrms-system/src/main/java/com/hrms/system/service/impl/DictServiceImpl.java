package com.hrms.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hrms.system.entity.Dict;
import com.hrms.system.entity.DictItem;
import com.hrms.system.mapper.DictMapper;
import com.hrms.system.service.DictItemService;
import com.hrms.system.service.DictService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 字典服务实现
 *
 * @author HRMS
 */
@Service
@RequiredArgsConstructor
public class DictServiceImpl extends ServiceImpl<DictMapper, Dict> implements DictService {

    private final DictItemService dictItemService;

    @Override
    public List<Dict> listEnabledDicts() {
        LambdaQueryWrapper<Dict> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Dict::getStatus, 1)
                .eq(Dict::getDeleted, 0)
                .orderByAsc(Dict::getDictType);
        return list(wrapper);
    }

    @Override
    public void createDict(Dict dict) {
        // 检查字典类型是否重复
        LambdaQueryWrapper<Dict> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Dict::getDictType, dict.getDictType());
        if (count(wrapper) > 0) {
            throw new RuntimeException("字典类型已存在");
        }

        dict.setCreatedTime(LocalDateTime.now());
        dict.setUpdatedTime(LocalDateTime.now());
        save(dict);
    }

    @Override
    public void updateDict(Dict dict) {
        Dict existingDict = getById(dict.getId());
        if (existingDict == null) {
            throw new RuntimeException("字典不存在");
        }

        // 如果字典类型发生变化，检查是否重复
        if (!existingDict.getDictType().equals(dict.getDictType())) {
            LambdaQueryWrapper<Dict> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Dict::getDictType, dict.getDictType())
                    .ne(Dict::getId, dict.getId());
            if (count(wrapper) > 0) {
                throw new RuntimeException("字典类型已存在");
            }
        }

        dict.setUpdatedTime(LocalDateTime.now());
        updateById(dict);
    }

    @Override
    public void deleteDict(Long id) {
        Dict dict = getById(id);
        if (dict == null) {
            throw new RuntimeException("字典不存在");
        }

        // 检查是否有字典项
        LambdaQueryWrapper<DictItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DictItem::getDictType, dict.getDictType())
                .eq(DictItem::getDeleted, 0);
        if (dictItemService.count(wrapper) > 0) {
            throw new RuntimeException("该字典下还有字典项，无法删除");
        }

        // 逻辑删除
        dict.setDeleted(1);
        dict.setUpdatedTime(LocalDateTime.now());
        updateById(dict);
    }

    @Override
    public void updateDictStatus(Long id, Integer status) {
        Dict dict = getById(id);
        if (dict == null) {
            throw new RuntimeException("字典不存在");
        }

        dict.setStatus(status);
        dict.setUpdatedTime(LocalDateTime.now());
        updateById(dict);
    }

    @Override
    public List<DictItem> getDictItemsByType(String dictType) {
        LambdaQueryWrapper<DictItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DictItem::getDictType, dictType)
                .eq(DictItem::getStatus, 1)
                .eq(DictItem::getDeleted, 0)
                .orderByAsc(DictItem::getDictSort)
                .orderByAsc(DictItem::getId);
        return dictItemService.list(wrapper);
    }

    @Override
    public void createDictItem(DictItem dictItem) {
        dictItem.setCreatedTime(LocalDateTime.now());
        dictItem.setUpdatedTime(LocalDateTime.now());
        dictItemService.save(dictItem);
    }

    @Override
    public void updateDictItem(DictItem dictItem) {
        DictItem existingItem = dictItemService.getById(dictItem.getId());
        if (existingItem == null) {
            throw new RuntimeException("字典项不存在");
        }

        dictItem.setUpdatedTime(LocalDateTime.now());
        dictItemService.updateById(dictItem);
    }

    @Override
    public void deleteDictItem(Long id) {
        DictItem dictItem = dictItemService.getById(id);
        if (dictItem == null) {
            throw new RuntimeException("字典项不存在");
        }

        // 逻辑删除
        dictItem.setDeleted(1);
        dictItem.setUpdatedTime(LocalDateTime.now());
        dictItemService.updateById(dictItem);
    }

    @Override
    public void updateDictItemStatus(Long id, Integer status) {
        DictItem dictItem = dictItemService.getById(id);
        if (dictItem == null) {
            throw new RuntimeException("字典项不存在");
        }

        dictItem.setStatus(status);
        dictItem.setUpdatedTime(LocalDateTime.now());
        dictItemService.updateById(dictItem);
    }
}
