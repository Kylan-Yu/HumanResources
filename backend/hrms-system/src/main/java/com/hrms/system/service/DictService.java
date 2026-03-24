package com.hrms.system.service;

import com.hrms.system.entity.Dict;
import com.hrms.system.entity.DictItem;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 字典服务接口
 *
 * @author HRMS
 */
public interface DictService extends IService<Dict> {

    /**
     * 获取所有启用的字典
     */
    List<Dict> listEnabledDicts();

    /**
     * 创建字典
     */
    void createDict(Dict dict);

    /**
     * 更新字典
     */
    void updateDict(Dict dict);

    /**
     * 删除字典
     */
    void deleteDict(Long id);

    /**
     * 更新字典状态
     */
    void updateDictStatus(Long id, Integer status);

    /**
     * 根据字典类型获取字典项
     */
    List<DictItem> getDictItemsByType(String dictType);

    /**
     * 创建字典项
     */
    void createDictItem(DictItem dictItem);

    /**
     * 更新字典项
     */
    void updateDictItem(DictItem dictItem);

    /**
     * 删除字典项
     */
    void deleteDictItem(Long id);

    /**
     * 更新字典项状态
     */
    void updateDictItemStatus(Long id, Integer status);
}
