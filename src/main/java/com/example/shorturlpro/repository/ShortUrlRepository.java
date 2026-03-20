package com.example.shorturlpro.repository;

import com.example.shorturlpro.entity.ShortUrl;
import com.example.shorturlpro.entity.ShortUrlStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.LocalDateTime;
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
    List<ShortUrl> findByUserId(Long userId);

    /**
     * 根据状态查询短链接列表
     *
     * @param status 状态
     * @return 短链接列表
     */
    List<ShortUrl> findByStatus(ShortUrlStatus status);

    /**
     * 根据状态查询短链接列表（分页）
     *
     * @param status 状态
     * @param pageable 分页参数
     * @return 短链接分页结果
     */
    Page<ShortUrl> findByStatus(ShortUrlStatus status, Pageable pageable);

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

    /**
     * 根据短码、原链接或名称模糊查询（分页）
     */
    @Query("SELECT s FROM ShortUrl s WHERE s.shortCode LIKE CONCAT('%', :keyword, '%') OR s.originalUrl LIKE CONCAT('%', :keyword, '%') OR s.name LIKE CONCAT('%', :keyword, '%')")
    Page<ShortUrl> findByShortCodeContainingOrOriginalUrlContainingOrNameContaining(
            @Param("keyword") String keyword, 
            Pageable pageable);

    /**
     * 根据短码、原链接或名称模糊查询（不分页）
     */
    @Query("SELECT s FROM ShortUrl s WHERE s.shortCode LIKE CONCAT('%', :keyword, '%') OR s.originalUrl LIKE CONCAT('%', :keyword, '%') OR s.name LIKE CONCAT('%', :keyword, '%')")
    List<ShortUrl> findByShortCodeContainingOrOriginalUrlContainingOrNameContaining(
            @Param("keyword") String keyword);

    /**
     * 根据短码或原链接模糊查询（分页） - 保留原有方法兼容性
     */
    @Query("SELECT s FROM ShortUrl s WHERE s.shortCode LIKE CONCAT('%', :keyword, '%') OR s.originalUrl LIKE CONCAT('%', :keyword, '%')")
    Page<ShortUrl> findByShortCodeContainingOrOriginalUrlContaining(
            @Param("keyword") String keyword, 
            Pageable pageable);

    /**
     * 根据短码或原链接模糊查询（不分页） - 保留原有方法兼容性
     */
    @Query("SELECT s FROM ShortUrl s WHERE s.shortCode LIKE CONCAT('%', :keyword, '%') OR s.originalUrl LIKE CONCAT('%', :keyword, '%')")
    List<ShortUrl> findByShortCodeContainingOrOriginalUrlContaining(
            @Param("keyword") String keyword);

    /**
     * 根据短码、原链接或名称模糊查询且状态筛选（分页）
     */
    @Query("SELECT s FROM ShortUrl s WHERE (s.shortCode LIKE CONCAT('%', :keyword, '%') OR s.originalUrl LIKE CONCAT('%', :keyword, '%') OR s.name LIKE CONCAT('%', :keyword, '%')) AND s.status = :status")
    Page<ShortUrl> findByShortCodeContainingOrOriginalUrlContainingOrNameContainingAndStatus(
            @Param("keyword") String keyword, 
            @Param("status") ShortUrlStatus status, 
            Pageable pageable);

    /**
     * 根据短码、原链接或名称模糊查询且状态筛选（不分页）
     */
    @Query("SELECT s FROM ShortUrl s WHERE (s.shortCode LIKE CONCAT('%', :keyword, '%') OR s.originalUrl LIKE CONCAT('%', :keyword, '%') OR s.name LIKE CONCAT('%', :keyword, '%')) AND s.status = :status")
    List<ShortUrl> findByShortCodeContainingOrOriginalUrlContainingOrNameContainingAndStatus(
            @Param("keyword") String keyword, 
            @Param("status") ShortUrlStatus status);

    /**
     * 根据短码或原链接模糊查询且状态筛选（分页） - 保留原有方法兼容性
     */
    @Query("SELECT s FROM ShortUrl s WHERE (s.shortCode LIKE CONCAT('%', :keyword, '%') OR s.originalUrl LIKE CONCAT('%', :keyword, '%')) AND s.status = :status")
    Page<ShortUrl> findByShortCodeContainingOrOriginalUrlContainingAndStatus(
            @Param("keyword") String keyword, 
            @Param("status") ShortUrlStatus status, 
            Pageable pageable);

    /**
     * 根据短码或原链接模糊查询且状态筛选（不分页） - 保留原有方法兼容性
     */
    @Query("SELECT s FROM ShortUrl s WHERE (s.shortCode LIKE CONCAT('%', :keyword, '%') OR s.originalUrl LIKE CONCAT('%', :keyword, '%')) AND s.status = :status")
    List<ShortUrl> findByShortCodeContainingOrOriginalUrlContainingAndStatus(
            @Param("keyword") String keyword, 
            @Param("status") ShortUrlStatus status);

    /**
     * 统计所有点击次数（简化版）
     */
    default long sumAllClickCounts() {
        return sumTotalClickCount();
    }

    /**
     * 根据日期范围统计点击次数
     * 注意：由于click_count是累计值，这个方法实际上不能准确统计某一天的新增点击
     * 这里保持原逻辑以兼容现有代码
     */
    @Query("SELECT COALESCE(SUM(s.clickCount), 0) FROM ShortUrl s WHERE s.createdAt BETWEEN :startDate AND :endDate")
    long sumClickCountsByDateRange(@Param("startDate") LocalDateTime startDate, 
                                  @Param("endDate") LocalDateTime endDate);
    
    /**
     * 统计今日新增点击次数（基于创建时间的近似统计）
     * 这个方法统计今天创建的所有短链接的总点击次数
     */
    default long getTodayClicksApproximate() {
        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.atTime(23, 59, 59);
        return sumClickCountsByDateRange(startOfDay, endOfDay);
    }

    /**
     * 根据状态统计数量
     */
    @Query("SELECT COUNT(s) FROM ShortUrl s WHERE s.status = :status")
    long countByStatus(@Param("status") ShortUrlStatus status);
}