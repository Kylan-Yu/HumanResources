package com.hrms.contract.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 合同实体类
 *
 * @author HRMS
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("hr_contract")
public class Contract {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 员工ID
     */
    @TableField("employee_id")
    private Long employeeId;

    /**
     * 合同编号
     */
    @TableField("contract_no")
    private String contractNo;

    /**
     * 合同类型
     */
    @TableField("contract_type")
    private String contractType;

    /**
     * 合同主体
     */
    @TableField("contract_subject")
    private String contractSubject;

    /**
     * 开始日期
     */
    @TableField("start_date")
    private LocalDate startDate;

    /**
     * 结束日期
     */
    @TableField("end_date")
    private LocalDate endDate;

    /**
     * 签署日期
     */
    @TableField("sign_date")
    private LocalDate signDate;

    /**
     * 合同状态
     */
    @TableField("contract_status")
    private String contractStatus;

    /**
     * 续签次数
     */
    @TableField("renew_count")
    private Integer renewCount;

    /**
     * 行业类型
     */
    @TableField("industry_type")
    private String industryType;

    /**
     * 扩展字段
     */
    @TableField("ext_json")
    private String extJson;

    /**
     * 备注
     */
    @TableField("remark")
    private String remark;

    /**
     * 创建人
     */
    @TableField(value = "created_by", fill = FieldFill.INSERT)
    private Long createdBy;

    /**
     * 创建时间
     */
    @TableField(value = "created_time", fill = FieldFill.INSERT)
    private LocalDateTime createdTime;

    /**
     * 更新人
     */
    @TableField(value = "updated_by", fill = FieldFill.INSERT_UPDATE)
    private Long updatedBy;

    /**
     * 更新时间
     */
    @TableField(value = "updated_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedTime;

    /**
     * 删除标记
     */
    @TableLogic
    @TableField("deleted")
    private Integer deleted;
}
