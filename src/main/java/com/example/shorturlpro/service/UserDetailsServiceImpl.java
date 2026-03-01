package com.example.shorturlpro.service;

import com.example.shorturlpro.entity.User;
import com.example.shorturlpro.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * 实现Spring Security的UserDetailsService，从数据库加载用户信息
 */
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    // 注入UserRepository（JPA接口，用于查询用户）
    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("用户不存在：" + username));

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername()) // 登录用户名
                .password(user.getPassword()) // 数据库中BCrypt加密后的密码
                .authorities(user.getRoles().toArray(new String[0])) // 用户角色
                .accountExpired(false) // 账户是否过期：false=未过期
                .accountLocked(false) // 账户是否锁定：false=未锁定
                .credentialsExpired(false) // 凭证（密码）是否过期：false=未过期（
                .disabled(user.getIsDeleted()) // 账户是否禁用：isDeleted=true则禁用（关联t_user表的逻辑删除字段）
                .build();
    }
}