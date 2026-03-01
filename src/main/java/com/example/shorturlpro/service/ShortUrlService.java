package com.example.shorturlpro.service;

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
    private static final int CODE_LENGTH = 6; // 默认短码长度

    private final ShortUrlRepository shortUrlRepository;

    @Value("${app.domain:http://localhost:8080}")
    private String appDomain;

    private final Random random = new Random();

    /**
     * 生成短链接
     *
     * @param request 生成请求参数
     * @return 生成响应
     */
    @Transactional
    public ShortUrlGenerateResponse generateShortUrl(ShortUrlGenerateRequest request) {
        log.info("开始生成短链接，原始链接: {}", request.getOriginalUrl());

        // 输入校验
        if (request.getOriginalUrl() == null || request.getOriginalUrl().trim().isEmpty()) {
            throw new IllegalArgumentException("原始链接不能为空");
        }

        // URL格式简单校验（可根据需要加强）
        if (!request.getOriginalUrl().matches("^https?://.+")) {
            throw new IllegalArgumentException("原始链接格式不正确，请以http://或https://开头");
        }

        // 生成唯一的短码
        String shortCode = generateUniqueShortCode();

        // 构造短链接实体
        ShortUrl shortUrl = new ShortUrl();
        shortUrl.setName(extractNameFromUrl(request.getOriginalUrl()));
        shortUrl.setOriginalUrl(request.getOriginalUrl().trim());
        shortUrl.setShortCode(shortCode);
        shortUrl.setStatus(ShortUrlStatus.ENABLED); // 默认启用
        shortUrl.setClickCount(0L);
        shortUrl.setAppId(request.getAppId());
        shortUrl.setCreatedAt(LocalDateTime.now());
        shortUrl.setUpdatedAt(LocalDateTime.now());

        // 保存到数据库
        ShortUrl saved = shortUrlRepository.save(shortUrl);
        log.info("短链接生成成功，短码: {}, ID: {}", shortCode, saved.getId());

        // 构造返回结果
        ShortUrlGenerateResponse response = new ShortUrlGenerateResponse();
        response.setShortCode(shortCode);
        response.setShortUrl(appDomain + "/" + shortCode);

        return response;
    }

    /**
     * 根据短码获取原始链接并增加点击次数
     *
     * @param shortCode 短码
     * @return 原始链接
     */
    @Cacheable(value = "shortUrl", key = "#shortCode")
    public String getOriginalUrlAndIncrement(String shortCode) {
        log.debug("查询短链接，短码: {}", shortCode);

        Optional<ShortUrl> optional = shortUrlRepository.findByShortCode(shortCode);
        if (optional.isEmpty()) {
            log.warn("短链接不存在，短码: {}", shortCode);
            throw new RuntimeException("短链接不存在");
        }

        ShortUrl shortUrl = optional.get();

        // 检查状态
        if (shortUrl.getStatus() != ShortUrlStatus.ENABLED) {
            log.warn("短链接已被禁用，短码: {}", shortCode);
            throw new RuntimeException("短链接已被禁用");
        }

        // 检查是否过期
        if (shortUrl.getExpiresAt() != null && shortUrl.getExpiresAt().isBefore(LocalDateTime.now())) {
            log.warn("短链接已过期，短码: {}", shortCode);
            throw new RuntimeException("短链接已过期");
        }

        // 异步增加点击次数（避免影响跳转性能）
        incrementClickCountAsync(shortUrl.getId());

        log.info("短链接跳转成功，短码: {}, 原始链接: {}", shortCode, shortUrl.getOriginalUrl());
        return shortUrl.getOriginalUrl();
    }

    /**
     * 异步增加点击次数
     *
     * @param id 短链接ID
     */
    private void incrementClickCountAsync(Long id) {
        new Thread(() -> {
            try {
                shortUrlRepository.incrementClickCount(id);
            } catch (Exception e) {
                log.error("更新点击次数失败，ID: {}", id, e);
            }
        }).start();
    }

    /**
     * 生成唯一的短码
     *
     * @return 唯一短码
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
     *
     * @return 随机短码
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
     * 从URL中提取名称（用于短链接显示）
     *
     * @param url 原始URL
     * @return 名称
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
     *
     * @param shortCode 短码
     * @return 短链接对象
     */
    @Cacheable(value = "shortUrlDetail", key = "#shortCode")
    public Optional<ShortUrl> findByShortCode(String shortCode) {
        return shortUrlRepository.findByShortCode(shortCode);
    }

    /**
     * 清除缓存（当短链接状态变更时调用）
     *
     * @param shortCode 短码
     */
    @CacheEvict(value = {"shortUrl", "shortUrlDetail"}, key = "#shortCode")
    public void clearCache(String shortCode) {
        log.debug("清除短链接缓存，短码: {}", shortCode);
    }
}