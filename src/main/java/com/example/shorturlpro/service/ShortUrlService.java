package com.example.shorturlpro.service;

import com.example.shorturlpro.dto.ShortUrlCreateRequest;
import com.example.shorturlpro.dto.ShortUrlGenerateRequest;
import com.example.shorturlpro.dto.ShortUrlGenerateResponse;
import com.example.shorturlpro.entity.ShortUrl;
import com.example.shorturlpro.entity.ShortUrlStatus;
import com.example.shorturlpro.repository.ShortUrlRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Random;

/**
 * 短链接核心服务类
 * 处理短链接的生成、查询、跳转等业务逻辑
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ShortUrlService {

    // Base62字符集：数字0-9 + 小写字母a-z + 大写字母A-Z
    private static final String BASE62 = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final int CODE_LENGTH = 6; // 短码长度为6位

    private final ShortUrlRepository shortUrlRepository;

    @Value("${app.domain:http://localhost:8080}")
    private String appDomain;

    private final Random random = new Random();

    /**
     * 【重载】生成短链接（关联用户ID）
     * 适配控制器传递userId的场景
     *
     * @param request 生成请求参数
     * @param userId  创建者用户ID（未登录时为null）
     * @return 生成响应
     */
    @Transactional
    public ShortUrlGenerateResponse generateShortUrl(ShortUrlGenerateRequest request, Long userId) {
        log.info("开始生成短链接（关联用户ID: {}），原始链接: {}", userId, request.getOriginalUrl());

        // 输入校验（复用原有逻辑）
        if (request.getOriginalUrl() == null || request.getOriginalUrl().trim().isEmpty()) {
            throw new IllegalArgumentException("原始链接不能为空");
        }
        if (!request.getOriginalUrl().matches("^https?://.+")) {
            throw new IllegalArgumentException("原始链接格式不正确，请以http://或https://开头");
        }

        // 生成唯一的短码（复用原有逻辑）
        String shortCode = generateUniqueShortCode();

        // 构造短链接实体（新增：设置userId）
        ShortUrl shortUrl = new ShortUrl();
        shortUrl.setName(extractNameFromUrl(request.getOriginalUrl()));
        shortUrl.setOriginalUrl(request.getOriginalUrl().trim());
        shortUrl.setShortCode(shortCode);
        shortUrl.setStatus(ShortUrlStatus.ENABLED); // 默认启用
        shortUrl.setClickCount(0L);
        shortUrl.setAppId(request.getAppId());
        shortUrl.setUserId(userId); // 新增：关联创建者用户ID（未登录时为null）
        shortUrl.setCreatedAt(LocalDateTime.now());
        shortUrl.setUpdatedAt(LocalDateTime.now());

        // 保存到数据库
        ShortUrl saved = shortUrlRepository.save(shortUrl);
        log.info("短链接生成成功（关联用户ID: {}），短码: {}, ID: {}", userId, shortCode, saved.getId());

        // 构造返回结果（复用原有逻辑）
        ShortUrlGenerateResponse response = new ShortUrlGenerateResponse();
        response.setShortCode(shortCode);
        response.setShortUrl(appDomain + "/" + shortCode);

        return response;
    }

    /**
     * 【原有】生成短链接（无用户ID）
     * 保持兼容，供无需关联用户的场景调用
     *
     * @param request 生成请求参数
     * @return 生成响应
     */
    @Transactional
    public ShortUrlGenerateResponse generateShortUrl(ShortUrlGenerateRequest request) {
        // 调用重载方法，userId传null（未关联用户）
        return generateShortUrl(request, null);
    }

    /**
     * 根据短码获取原始链接并增加点击次数
     * 改进版本：使用同步事务确保点击次数准确自增
     */
    @Cacheable(value = "shortUrl", key = "#shortCode")
    @Transactional
    public String getOriginalUrlAndIncrement(String shortCode) {
        log.debug("查询短链接，短码: {}", shortCode);

        Optional<ShortUrl> optional = shortUrlRepository.findByShortCode(shortCode);
        if (optional.isEmpty()) {
            log.warn("短链接不存在，短码: {}", shortCode);
            throw new RuntimeException("短链接不存在");
        }

        ShortUrl shortUrl = optional.get();
        if (shortUrl.getStatus() != ShortUrlStatus.ENABLED) {
            log.warn("短链接已被禁用，短码: {}", shortCode);
            throw new RuntimeException("短链接已被禁用");
        }
        if (shortUrl.getExpiresAt() != null && shortUrl.getExpiresAt().isBefore(LocalDateTime.now())) {
            log.warn("短链接已过期，短码: {}", shortCode);
            throw new RuntimeException("短链接已过期");
        }

        // 同步增加点击次数（在同一个事务中）
        incrementClickCountSync(shortUrl.getId());

        log.info("短链接跳转成功，短码: {}, 原始链接: {}, 当前点击次数: {}", 
                shortCode, shortUrl.getOriginalUrl(), shortUrl.getClickCount() + 1);
        return shortUrl.getOriginalUrl();
    }

    /**
     * 同步增加点击次数
     * 在同一事务中执行，确保数据一致性
     */
    private void incrementClickCountSync(Long id) {
        try {
            shortUrlRepository.incrementClickCount(id);
            log.debug("点击次数自增成功，短链接ID: {}", id);
        } catch (Exception e) {
            log.error("更新点击次数失败，ID: {}", id, e);
            throw new RuntimeException("更新点击统计失败", e);
        }
    }

    /**
     * 生成唯一的短码
     */
    private String generateUniqueShortCode() {
        int maxAttempts = 10; // 最大尝试次数
        for (int i = 0; i < maxAttempts; i++) {
            String shortCode = generateRandomCode();
            if (!shortUrlRepository.existsByShortCode(shortCode)) {
                return shortCode;
            }
            log.debug("短码已存在，重新生成: {}", shortCode);
        }
        throw new RuntimeException("生成唯一短码失败，请稍后重试");
    }

    /**
     * 生成随机短码（Base62编码）
     */
    private String generateRandomCode() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < CODE_LENGTH; i++) {
            int index = random.nextInt(BASE62.length());
            sb.append(BASE62.charAt(index));
        }
        return sb.toString();
    }

    /**
     * 从URL中提取名称
     */
    private String extractNameFromUrl(String url) {
        try {
            String host = url.replaceFirst("^https?://", "").split("/")[0];
            return host.length() > 20 ? host.substring(0, 20) + "..." : host;
        } catch (Exception e) {
            return "未知链接";
        }
    }

    /**
     * 根据短码查询短链接详情（带缓存）
     */
    @Cacheable(value = "shortUrlDetail", key = "#shortCode")
    public Optional<ShortUrl> findByShortCode(String shortCode) {
        return shortUrlRepository.findByShortCode(shortCode);
    }

    /**
     * 清除缓存
     */
    @CacheEvict(value = {"shortUrl", "shortUrlDetail"}, key = "#shortCode")
    public void clearCache(String shortCode) {
        log.debug("清除短链接缓存，短码: {}", shortCode);
    }

    /**
     * 管理员创建短链接
     */
    @Transactional
    public ShortUrl createShortUrlByAdmin(ShortUrlCreateRequest request) {
        log.info("管理员创建短链接: name={}, originalUrl={}", request.getName(), request.getOriginalUrl());

        // 参数校验
        if (request.getOriginalUrl() == null || request.getOriginalUrl().trim().isEmpty()) {
            throw new IllegalArgumentException("原始链接不能为空");
        }
        if (!request.getOriginalUrl().matches("^https?://.+")) {
            throw new IllegalArgumentException("原始链接格式不正确，请以http://或https://开头");
        }

        // 生成短码
        String shortCode;
        if (request.getShortCode() != null && !request.getShortCode().trim().isEmpty()) {
            // 使用指定的短码
            shortCode = request.getShortCode().trim();
            if (shortUrlRepository.existsByShortCode(shortCode)) {
                throw new IllegalArgumentException("短码已存在: " + shortCode);
            }
        } else {
            // 自动生成短码
            shortCode = generateUniqueShortCode();
        }

        // 创建短链接实体
        ShortUrl shortUrl = new ShortUrl();
        shortUrl.setName(request.getName());
        shortUrl.setOriginalUrl(request.getOriginalUrl().trim());
        shortUrl.setShortCode(shortCode);
        shortUrl.setStatus(request.getStatus() != null ? request.getStatus() : ShortUrlStatus.ENABLED);
        shortUrl.setClickCount(0L);
        shortUrl.setAppId(request.getAppId());
        shortUrl.setUserId(null); // 管理员创建的链接暂时设为null，后续可扩展
        shortUrl.setExpiresAt(request.getExpiresAt());
        shortUrl.setCreatedAt(LocalDateTime.now());
        shortUrl.setUpdatedAt(LocalDateTime.now());

        ShortUrl saved = shortUrlRepository.save(shortUrl);
        log.info("管理员创建短链接成功，ID: {}, 短码: {}", saved.getId(), shortCode);
        
        return saved;
    }

    /**
     * 管理员更新短链接
     */
    @Transactional
    @CacheEvict(value = {"shortUrl", "shortUrlDetail"}, key = "#id")
    public ShortUrl updateShortUrlByAdmin(Long id, ShortUrlCreateRequest request) {
        log.info("管理员更新短链接: id={}, name={}", id, request.getName());

        // 查找现有记录
        ShortUrl existing = shortUrlRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("短链接不存在，ID: " + id));

        // 如果短码被修改，检查新短码是否已存在
        if (request.getShortCode() != null && !request.getShortCode().equals(existing.getShortCode())) {
            if (shortUrlRepository.existsByShortCode(request.getShortCode())) {
                throw new IllegalArgumentException("短码已存在: " + request.getShortCode());
            }
            existing.setShortCode(request.getShortCode());
        }

        // 更新其他字段
        existing.setName(request.getName());
        existing.setOriginalUrl(request.getOriginalUrl());
        existing.setStatus(request.getStatus() != null ? request.getStatus() : existing.getStatus());
        existing.setAppId(request.getAppId());
        existing.setExpiresAt(request.getExpiresAt());
        existing.setUpdatedAt(LocalDateTime.now());

        ShortUrl updated = shortUrlRepository.save(existing);
        log.info("管理员更新短链接成功，ID: {}", id);
        
        return updated;
    }

    /**
     * 管理员删除短链接
     */
    @Transactional
    @CacheEvict(value = {"shortUrl", "shortUrlDetail"}, allEntries = true)
    public void deleteShortUrlByAdmin(Long id) {
        log.info("管理员删除短链接: id={}", id);

        if (!shortUrlRepository.existsById(id)) {
            throw new RuntimeException("短链接不存在，ID: " + id);
        }

        shortUrlRepository.deleteById(id);
        log.info("管理员删除短链接成功，ID: {}", id);
    }

    /**
     * 管理员批量删除短链接
     */
    @Transactional
    @CacheEvict(value = {"shortUrl", "shortUrlDetail"}, allEntries = true)
    public void batchDeleteShortUrlsByAdmin(List<Long> ids) {
        log.info("管理员批量删除短链接，IDs: {}", ids);
        
        shortUrlRepository.deleteAllById(ids);
        log.info("管理员批量删除成功，共删除 {} 条记录", ids.size());
    }
}