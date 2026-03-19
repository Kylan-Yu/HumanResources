package com.hrms.employee.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 员工附件实体
 *
 * @author HRMS
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("hr_employee_attachment")
public class EmployeeAttachment {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 员工ID
     */
    @TableField("employee_id")
    private Long employeeId;

    /**
     * 附件类型：id_card-身份证，diploma-毕业证，degree-学位证，contract-劳动合同，resume-简历，other-其他
     */
    @TableField("attachment_type")
    private String attachmentType;

    /**
     * 文件名
     */
    @TableField("file_name")
    private String fileName;

    /**
     * 文件路径
     */
    @TableField("file_path")
    private String filePath;

    /**
     * 文件大小（字节）
     */
    @TableField("file_size")
    private Long fileSize;

    /**
     * 文件类型
     */
    @TableField("file_type")
    private String fileType;

    /**
     * 上传时间
     */
    @TableField("upload_time")
    private LocalDateTime uploadTime;

    /**
     * 备注
     */
    @TableField("remark")
    private String remark;

    /**
     * 创建时间
     */
    @TableField(value = "created_time", fill = FieldFill.INSERT)
    private LocalDateTime createdTime;

    /**
     * 更新时间
     */
    @TableField(value = "updated_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedTime;

    /**
     * 删除标记：0-未删除，1-已删除
     */
    @TableField("deleted")
    @TableLogic
    private Integer deleted;
}
