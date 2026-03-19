package com.hrms.common;

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
    private List<T> list;

    /**
     * 总记录数
     */
    @Schema(description = "总记录数")
    private Long total;

    /**
     * 当前页码
     */
    @Schema(description = "当前页码")
    private Integer pageNum;

    /**
     * 每页大小
     */
    @Schema(description = "每页大小")
    private Integer pageSize;

    /**
     * 总页数
     */
    @Schema(description = "总页数")
    private Integer pages;

    public PageResult() {
    }

    public PageResult(List<T> list, Long total, Integer pageNum, Integer pageSize) {
        this.list = list;
        this.total = total;
        this.pageNum = pageNum;
        this.pageSize = pageSize;
        this.pages = (int) Math.ceil((double) total / pageSize);
    }

    /**
     * 创建分页结果
     */
    public static <T> PageResult<T> of(List<T> list, Long total, Integer pageNum, Integer pageSize) {
        return new PageResult<>(list, total, pageNum, pageSize);
    }

    /**
     * 创建空分页结果
     */
    public static <T> PageResult<T> empty() {
        return new PageResult<>(null, 0L, 1, 10);
    }
}
