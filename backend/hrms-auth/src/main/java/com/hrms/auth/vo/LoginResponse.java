package com.hrms.auth.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 登录响应VO
 *
 * @author HRMS
 */
@Data
@Schema(description = "登录响应")
public class LoginResponse {

    /**
     * 访问令牌
     */
    @Schema(description = "访问令牌")
    private String accessToken;

    /**
     * 刷新令牌
     */
    @Schema(description = "刷新令牌")
    private String refreshToken;

    /**
     * 令牌类型
     */
    @Schema(description = "令牌类型", example = "Bearer")
    private String tokenType = "Bearer";

    /**
     * 过期时间（秒）
     */
    @Schema(description = "过期时间（秒）")
    private Long expiresIn;

    /**
     * 用户ID
     */
    @Schema(description = "用户ID")
    private Long userId;

    /**
     * 用户名
     */
    @Schema(description = "用户名")
    private String username;

    /**
     * 真实姓名
     */
    @Schema(description = "真实姓名")
    private String realName;

    /**
     * 邮箱
     */
    @Schema(description = "邮箱")
    private String email;

    /**
     * 手机号
     */
    @Schema(description = "手机号")
    private String mobile;

    /**
     * 头像
     */
    @Schema(description = "头像")
    private String avatar;

    /**
     * 所属组织ID
     */
    @Schema(description = "所属组织ID")
    private Long orgId;

    /**
     * 所属部门ID
     */
    @Schema(description = "所属部门ID")
    private Long deptId;

    /**
     * 岗位ID
     */
    @Schema(description = "岗位ID")
    private Long positionId;

    /**
     * 行业类型
     */
    @Schema(description = "行业类型")
    private String industryType;

    /**
     * 角色列表
     */
    @Schema(description = "角色列表")
    private List<String> roles;

    /**
     * 权限列表
     */
    @Schema(description = "权限列表")
    private List<String> permissions;

    /**
     * 菜单树
     */
    @Schema(description = "菜单树")
    private List<MenuVO> menuTree;
}
