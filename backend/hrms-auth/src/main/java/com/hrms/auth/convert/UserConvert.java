package com.hrms.auth.convert;

import com.hrms.auth.dto.UserCreateDTO;
import com.hrms.auth.dto.UserUpdateDTO;
import com.hrms.auth.entity.User;
import com.hrms.auth.vo.UserVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

/**
 * 用户转换器
 *
 * @author HRMS
 */
@Component
public class UserConvert {

    /**
     * DTO转Entity
     */
    public User toEntity(UserCreateDTO dto) {
        User user = new User();
        BeanUtils.copyProperties(dto, user);
        return user;
    }

    /**
     * Entity转VO
     */
    public UserVO toVO(User entity) {
        UserVO vo = new UserVO();
        BeanUtils.copyProperties(entity, vo, "password");
        return vo;
    }

    /**
     * UpdateDTO转Entity
     */
    public void updateEntity(UserUpdateDTO dto, User user) {
        BeanUtils.copyProperties(dto, user, "id", "username", "password", "createdTime");
    }
}
