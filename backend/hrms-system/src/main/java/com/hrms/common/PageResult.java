package com.hrms.common;

import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.Data;

import java.util.List;

/**
 * 分页结果。
 */
@Data
public class PageResult<T> {

    /**
     * 当前前端使用字段。
     */
    private List<T> list;
    private Long total;
    private Integer pageNum;
    private Integer pageSize;
    private Integer pages;

    /**
     * 兼容旧前端字段。
     */
    private List<T> records;
    private Integer current;
    private Integer size;
    private Boolean hasNext;
    private Boolean hasPrevious;

    public PageResult() {}

    public PageResult(List<T> records, Long total, Integer current, Integer size) {
        this.list = records;
        this.records = records;
        this.total = total;
        this.pageNum = current;
        this.pageSize = size;
        this.current = current;
        this.size = size;
        this.pages = size == null || size == 0 ? 0 : (int) Math.ceil((double) total / size);
        this.hasNext = current < pages;
        this.hasPrevious = current > 1;
    }

    public static <T> PageResult<T> of(IPage<T> page) {
        return new PageResult<>(page.getRecords(), page.getTotal(),
                (int) page.getCurrent(), (int) page.getSize());
    }

    public static <T> PageResult<T> of(List<T> records, Long total, Integer current, Integer size) {
        return new PageResult<>(records, total, current, size);
    }
}
