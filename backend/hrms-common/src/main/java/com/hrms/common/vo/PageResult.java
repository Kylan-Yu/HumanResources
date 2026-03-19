package com.hrms.common.vo;

import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;
import java.util.List;

/**
 * 分页返回结果
 *
 * @param <T> 数据类型
 * @author HRMS
 */
@Data
@Schema(description = "分页返回结果")
public class PageResult<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 数据列表
     */
    @Schema(description = "数据列表")
    private List<T> records;

    /**
     * 总记录数
     */
    @Schema(description = "总记录数")
    private Long total;

    /**
     * 当前页码
     */
    @Schema(description = "当前页码")
    private Long current;

    /**
     * 每页显示条数
     */
    @Schema(description = "每页显示条数")
    private Long size;

    /**
     * 总页数
     */
    @Schema(description = "总页数")
    private Long pages;

    /**
     * 是否有下一页
     */
    @Schema(description = "是否有下一页")
    private Boolean hasNext;

    /**
     * 是否有上一页
     */
    @Schema(description = "是否有上一页")
    private Boolean hasPrevious;

    public PageResult() {
    }

    public PageResult(List<T> records, Long total, Long current, Long size) {
        this.records = records;
        this.total = total;
        this.current = current;
        this.size = size;
        this.pages = (total + size - 1) / size;
        this.hasNext = current < pages;
        this.hasPrevious = current > 1;
    }

    /**
     * 创建分页结果
     */
    public static <T> PageResult<T> of(List<T> records, Long total, Long current, Long size) {
        return new PageResult<>(records, total, current, size);
    }

    /**
     * 创建空分页结果
     */
    public static <T> PageResult<T> empty(Long current, Long size) {
        return new PageResult<>(null, 0L, current, size);
    }
}
