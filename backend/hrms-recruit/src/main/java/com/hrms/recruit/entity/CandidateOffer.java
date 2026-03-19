package com.hrms.recruit.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 候选人Offer实体类
 *
 * @author HRMS
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("hr_candidate_offer")
public class CandidateOffer {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 候选人ID
     */
    @TableField("candidate_id")
    private Long candidateId;

    /**
     * Offer编号
     */
    @TableField("offer_no")
    private String offerNo;

    /**
     * 职位ID
     */
    @TableField("position_id")
    private Long positionId;

    /**
     * 薪资金额
     */
    @TableField("salary_amount")
    private Long salaryAmount;

    /**
     * 入职日期
     */
    @TableField("entry_date")
    private LocalDate entryDate;

    /**
     * Offer状态
     */
    @TableField("offer_status")
    private String offerStatus;

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
