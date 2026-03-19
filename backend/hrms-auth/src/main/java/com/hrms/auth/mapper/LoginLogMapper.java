package com.hrms.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hrms.auth.entity.LoginLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 登录日志Mapper
 *
 * @author HRMS
 */
@Mapper
public interface LoginLogMapper extends BaseMapper<LoginLog> {

    /**
     * 分页查询登录日志
     */
    List<LoginLog> selectLoginLogPage(@Param("username") String username, 
                                      @Param("loginStatus") Integer loginStatus,
                                      @Param("startTime") LocalDateTime startTime,
                                      @Param("endTime") LocalDateTime endTime);

    /**
     * 根据用户ID查询最近登录记录
     */
    @Select("SELECT * FROM sys_login_log WHERE user_id = #{userId} ORDER BY login_time DESC LIMIT 10")
    List<LoginLog> findRecentLoginLogsByUserId(@Param("userId") Long userId);
}
