package com.hrms.system.security;

import com.hrms.system.entity.User;
import com.hrms.system.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * User details loader for system service.
 */
@Service
@RequiredArgsConstructor
public class SystemUserDetailsService implements UserDetailsService {

    private final UserMapper userMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userMapper.findByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException("User not found: " + username);
        }
        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new UsernameNotFoundException("User disabled: " + username);
        }

        List<String> permissions = userMapper.findPermissionsByUserId(user.getId());
        System.out.println("[workflow-auth-fix] parsed userId: " + user.getId());
        System.out.println("[workflow-auth-fix] raw permissions: " + permissions);
        List<SimpleGrantedAuthority> authorities = permissions.stream()
                .filter(StringUtils::hasText)
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
        System.out.println("[workflow-auth-fix] granted authorities: " + authorities);

        return new LoginUser(
                user.getId(),
                user.getUsername(),
                user.getPassword(),
                user.getStatus() == null || user.getStatus() == 1,
                authorities
        );
    }
}

