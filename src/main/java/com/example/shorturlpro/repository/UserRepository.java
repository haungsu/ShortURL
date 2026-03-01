package com.example.shorturlpro.repository;

import com.example.shorturlpro.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

/**
 * JPA接口，用于查询用户（Spring Data JPA自动实现，无需写SQL）
 */
public interface UserRepository extends JpaRepository<User, Long> {
    // 按用户名查询用户（用于登录认证）
    Optional<User> findByUsername(String username);
}