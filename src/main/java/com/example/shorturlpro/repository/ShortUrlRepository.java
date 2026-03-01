package com.example.shorturlpro.repository;

import com.example.shorturlpro.entity.ShortUrl;
import com.example.shorturlpro.entity.ShortUrlStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 短链接数据访问接口
 * 继承JPA的JpaRepository，自动提供基础CRUD方法
 */
@Repository
public interface ShortUrlRepository extends JpaRepository<ShortUrl, Long> {

    /**
     * 根据短码查询短链接（精确匹配）
     *
     * @param shortCode 短码
     * @return 短链接对象
     */
    Optional<ShortUrl> findByShortCode(String shortCode);

    /**
     * 根据短码和状态查询短链接
     *
     * @param shortCode 短码
     * @param status    状态
     * @return 短链接对象
     */
    Optional<ShortUrl> findByShortCodeAndStatus(String shortCode, ShortUrlStatus status);

    /**
     * 根据应用标识查询短链接列表
     *
     * @param appId 应用标识
     * @return 短链接列表
     */
    List<ShortUrl> findByAppId(String appId);

    /**
     * 根据创建用户ID查询短链接列表
     *
     * @param userId 用户ID
     * @return 短链接列表
     */
    List<ShortUrl> findByCreateUserId(Long userId);

    /**
     * 根据状态查询短链接列表
     *
     * @param status 状态
     * @return 短链接列表
     */
    List<ShortUrl> findByStatus(ShortUrlStatus status);

    /**
     * 查询短码是否存在
     *
     * @param shortCode 短码
     * @return 是否存在
     */
    boolean existsByShortCode(String shortCode);

    /**
     * 更新点击次数（原子性操作）
     *
     * @param id 短链接ID
     */
    @Modifying
    @Query("UPDATE ShortUrl s SET s.clickCount = s.clickCount + 1 WHERE s.id = :id")
    void incrementClickCount(@Param("id") Long id);

    /**
     * 批量更新状态
     *
     * @param ids    ID列表
     * @param status 新状态
     */
    @Modifying
    @Query("UPDATE ShortUrl s SET s.status = :status WHERE s.id IN :ids")
    void batchUpdateStatus(@Param("ids") List<Long> ids, @Param("status") ShortUrlStatus status);

    /**
     * 统计总数量
     *
     * @return 总数
     */
    @Query("SELECT COUNT(s) FROM ShortUrl s")
    long countTotal();

    /**
     * 统计总点击次数
     *
     * @return 总点击次数
     */
    @Query("SELECT COALESCE(SUM(s.clickCount), 0) FROM ShortUrl s")
    long sumTotalClickCount();

    /**
     * 统计各状态数量
     *
     * @return 状态统计结果
     */
    @Query("SELECT s.status, COUNT(s) FROM ShortUrl s GROUP BY s.status")
    List<Object[]> countByStatus();
}