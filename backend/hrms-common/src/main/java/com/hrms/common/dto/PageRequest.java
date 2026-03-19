package com.hrms.common.dto;

import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.validation.constraints.Min;
import javax.validation.constraints.Max;
import java.io.Serializable;

/**
 * 分页请求参数
 *
 * @author HRMS
 */
@Data
@Schema(description = "分页请求参数")
public class PageRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 当前页码，从1开始
     */
    @Schema(description = "当前页码，从1开始", example = "1")
    @Min(value = 1, message = "页码最小值为1")
    private Integer pageNum = 1;

    /**
     * 每页显示条数
     */
    @Schema(description = "每页显示条数", example = "10")
    @Min(value = 1, message = "每页显示条数最小值为1")
    @Max(value = 100, message = "每页显示条数最大值为100")
    private Integer pageSize = 10;

    /**
     * 排序字段
     */
    @Schema(description = "排序字段", example = "created_time")
    private String orderBy;

    /**
     * 排序方式：asc-升序，desc-降序
     */
    @Schema(description = "排序方式：asc-升序，desc-降序", example = "desc")
    private String orderDirection = "desc";

    /**
     * 关键字搜索
     */
    @Schema(description = "关键字搜索", example = "张三")
    private String keyword;

    /**
     * 获取分页起始位置
     */
    public Integer getOffset() {
        return (pageNum - 1) * pageSize;
    }

    /**
     * 获取当前页码
     */
    public Integer getCurrent() {
        return pageNum;
    }

    /**
     * 获取每页显示条数
     */
    public Integer getSize() {
        return pageSize;
    }
}
