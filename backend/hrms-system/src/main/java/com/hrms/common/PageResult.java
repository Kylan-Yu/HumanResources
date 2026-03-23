package com.hrms.common;

import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.Data;

import java.util.List;

/**
 * 分页结果
 *
 * @author HRMS
 */
@Data
public class PageResult<T> {

    private List<T> records;
    private Long total;
    private Integer current;
    private Integer size;
    private Integer pages;
    private Boolean hasNext;
    private Boolean hasPrevious;

    public PageResult() {}

    public PageResult(List<T> records, Long total, Integer current, Integer size) {
        this.records = records;
        this.total = total;
        this.current = current;
        this.size = size;
        this.pages = (int) Math.ceil((double) total / size);
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
