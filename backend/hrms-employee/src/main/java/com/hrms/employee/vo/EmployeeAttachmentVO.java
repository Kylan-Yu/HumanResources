package com.hrms.employee.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 员工附件VO
 *
 * @author HRMS
 */
@Data
public class EmployeeAttachmentVO {

    /**
     * 附件ID
     */
    private Long id;

    /**
     * 员工ID
     */
    private Long employeeId;

    /**
     * 附件类型：id_card-身份证，diploma-毕业证，degree-学位证，contract-劳动合同，resume-简历，other-其他
     */
    private String attachmentType;

    /**
     * 附件类型描述
     */
    private String attachmentTypeDesc;

    /**
     * 文件名
     */
    private String fileName;

    /**
     * 文件路径
     */
    private String filePath;

    /**
     * 文件大小（字节）
     */
    private Long fileSize;

    /**
     * 文件大小（格式化）
     */
    private String fileSizeDesc;

    /**
     * 文件类型
     */
    private String fileType;

    /**
     * 上传时间
     */
    private LocalDateTime uploadTime;

    /**
     * 备注
     */
    private String remark;

    /**
     * 创建时间
     */
    private LocalDateTime createdTime;

    /**
     * 更新时间
     */
    private LocalDateTime updatedTime;
}
