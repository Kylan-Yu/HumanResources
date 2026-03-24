package com.hrms.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hrms.system.entity.DictItem;
import com.hrms.system.mapper.DictItemMapper;
import com.hrms.system.service.DictItemService;
import org.springframework.stereotype.Service;

/**
 * 字典项服务实现
 *
 * @author HRMS
 */
@Service
public class DictItemServiceImpl extends ServiceImpl<DictItemMapper, DictItem> implements DictItemService {
}
