package com.example.shorturlpro.service;

import com.example.shorturlpro.dto.ShortUrlCreateRequest;
import com.example.shorturlpro.dto.ShortUrlGenerateRequest;
import com.example.shorturlpro.dto.ShortUrlGenerateResponse;
import com.example.shorturlpro.entity.ShortUrl;
import com.example.shorturlpro.entity.ShortUrlStatus;
import com.example.shorturlpro.repository.ShortUrlRepository;
import com.example.shorturlpro.util.LogUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

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
    @Value("${app.short-url.code-length:6}")
    private int code_length;

    private final ShortUrlRepository shortUrlRepository;

    @Value("${app.short-url.base-url:http://localhost:8080}")
    private String fallbackAppDomain;
    
    @Value("${app.short-url.default-expire-days:30}")
    private int defaultExpireDays;

    private final Random random = new Random();

    /**
     * 动态获取用户实际访问的域名（适配内网穿透动态域名）
     * 优先读取反向代理透传的头信息，无则读取原生请求信息，兜底用配置值
     * @return 动态域名（如：https://your-domain.com）
     */
    public String getDynamicAppDomain() {
        // 获取当前请求上下文
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            log.warn("无请求上下文，使用兜底域名: {}", fallbackAppDomain);
            return fallbackAppDomain;
        }

        HttpServletRequest request = attributes.getRequest();

        // 1. 优先读取反向代理/内网穿透透传的真实域名（适配ngrok/FRP等工具）
        String forwardedHost = request.getHeader("X-Forwarded-Host");
        String forwardedProto = request.getHeader("X-Forwarded-Proto"); // http/https

        if (forwardedHost != null && forwardedProto != null) {
            log.debug("读取到X-Forwarded头信息，Host: {}, Proto: {}", forwardedHost, forwardedProto);
            // 处理X-Forwarded-Host包含端口的情况（比如 xxx.ngrok.io:8080）
            if (forwardedHost.contains(":")) {
                return String.format("%s://%s", forwardedProto, forwardedHost);
            } else {
                // 无端口时，根据协议自动补默认端口（80/443）
                int defaultPort = "https".equals(forwardedProto) ? 443 : 80;
                return defaultPort == 80 || defaultPort == 443
                        ? String.format("%s://%s", forwardedProto, forwardedHost)
                        : String.format("%s://%s:%d", forwardedProto, forwardedHost, defaultPort);
            }
        }

        // 2. 无反向代理头，读取原生请求信息
        String scheme = request.getScheme(); // http/https
        String serverName = request.getServerName(); // 实际访问的域名/IP
        int serverPort = request.getServerPort(); // 实际访问的端口

        log.debug("读取原生请求信息，Scheme: {}, ServerName: {}, Port: {}", scheme, serverName, serverPort);

        // 拼接完整域名（排除80/443默认端口，避免多余的:80/:443）
        if (serverPort == 80 || serverPort == 443) {
            return String.format("%s://%s", scheme, serverName);
        } else {
            return String.format("%s://%s:%d", scheme, serverName, serverPort);
        }
    }

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
        long startTime = System.currentTimeMillis();
        String operation = "SHORT_URL_GENERATE";
        
        try {
            LogUtil.setOperation(operation);
            LogUtil.setUserContext(userId, userId != null ? "user_" + userId : "anonymous");
            
            log.info("开始生成短链接，用户ID: {}, 原始链接: {}", userId, request.getOriginalUrl());
            LogUtil.logBusiness(operation, String.format("开始生成短链接，用户ID=%s, 原始链接=%s", 
                userId, request.getOriginalUrl()));

            // 输入校验
            if (request.getOriginalUrl() == null || request.getOriginalUrl().trim().isEmpty()) {
                String errorMsg = "原始链接不能为空";
                LogUtil.logBusinessFailure(operation, errorMsg, new IllegalArgumentException(errorMsg));
                throw new IllegalArgumentException(errorMsg);
            }
            if (!request.getOriginalUrl().matches("^https?://.+")) {
                String errorMsg = "原始链接格式不正确，请以http://或https://开头";
                LogUtil.logBusinessFailure(operation, errorMsg, new IllegalArgumentException(errorMsg));
                throw new IllegalArgumentException(errorMsg);
            }

            // 生成唯一的短码
            String shortCode = generateUniqueShortCode();
            log.debug("生成短码: {}", shortCode);

            // 构造短链接实体
            ShortUrl shortUrl = new ShortUrl();
            shortUrl.setName(extractNameFromUrl(request.getOriginalUrl()));
            shortUrl.setOriginalUrl(request.getOriginalUrl().trim());
            shortUrl.setShortCode(shortCode);
            shortUrl.setStatus(ShortUrlStatus.ENABLED);
            shortUrl.setClickCount(0L);
            shortUrl.setAppId(request.getAppId());
            shortUrl.setUserId(userId);
            shortUrl.setExpiresAt(LocalDateTime.now().plusDays(defaultExpireDays));
            shortUrl.setCreatedAt(LocalDateTime.now());
            shortUrl.setUpdatedAt(LocalDateTime.now());

            // 保存到数据库
            ShortUrl saved = shortUrlRepository.save(shortUrl);
            long duration = System.currentTimeMillis() - startTime;
            
            log.info("短链接生成成功，用户ID: {}, 短码: {}, ID: {}, 耗时: {}ms", 
                userId, shortCode, saved.getId(), duration);
            LogUtil.logBusinessSuccess(operation, String.format(
                "短链接生成成功，用户ID=%s, 短码=%s, ID=%d, 原始链接=%s, 耗时=%dms", 
                userId, shortCode, saved.getId(), request.getOriginalUrl(), duration));

            // 构造返回结果
            ShortUrlGenerateResponse response = new ShortUrlGenerateResponse();
            response.setShortCode(shortCode);
            String dynamicDomain = getDynamicAppDomain();
            response.setShortUrl(dynamicDomain + "/" + shortCode);

            return response;
            
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            LogUtil.logBusinessFailure(operation, String.format(
                "短链接生成失败，用户ID=%s, 原始链接=%s, 耗时=%dms", 
                userId, request.getOriginalUrl(), duration), e);
            throw e;
        }
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
     * 优化：直接返回数据库中存储的原始URL，跳转逻辑更清晰
     */
    @Cacheable(value = "shortUrl", key = "#shortCode")
    @Transactional
    public String getOriginalUrlAndIncrement(String shortCode) {
        long startTime = System.currentTimeMillis();
        String operation = "SHORT_URL_REDIRECT";
        
        try {
            LogUtil.setOperation(operation);
            log.debug("查询短链接，短码: {}", shortCode);
            LogUtil.logBusiness(operation, String.format("开始跳转查询，短码=%s", shortCode));

            Optional<ShortUrl> optional = shortUrlRepository.findByShortCode(shortCode);
            if (optional.isEmpty()) {
                String errorMsg = "短链接不存在";
                log.warn("短链接不存在，短码: {}", shortCode);
                LogUtil.logBusinessFailure(operation, String.format("短链接不存在，短码=%s", shortCode), 
                    new RuntimeException(errorMsg));
                throw new RuntimeException(errorMsg);
            }

            ShortUrl shortUrl = optional.get();
            if (shortUrl.getStatus() != ShortUrlStatus.ENABLED) {
                String errorMsg = "短链接已被禁用";
                log.warn("短链接已被禁用，短码: {}", shortCode);
                LogUtil.logBusinessFailure(operation, String.format("短链接已被禁用，短码=%s", shortCode), 
                    new RuntimeException(errorMsg));
                throw new RuntimeException(errorMsg);
            }
            if (shortUrl.getExpiresAt() != null && shortUrl.getExpiresAt().isBefore(LocalDateTime.now())) {
                String errorMsg = "短链接已过期";
                log.warn("短链接已过期，短码: {}", shortCode);
                LogUtil.logBusinessFailure(operation, String.format("短链接已过期，短码=%s", shortCode), 
                    new RuntimeException(errorMsg));
                throw new RuntimeException(errorMsg);
            }

            // 同步增加点击次数
            incrementClickCountSync(shortUrl.getId());

            String originalUrl = shortUrl.getOriginalUrl();
            long duration = System.currentTimeMillis() - startTime;
            
            log.info("短链接跳转成功，短码: {}, 原始链接: {}, 当前点击次数: {}, 耗时: {}ms", 
                    shortCode, originalUrl, shortUrl.getClickCount() + 1, duration);
            LogUtil.logBusinessSuccess(operation, String.format(
                "跳转成功，短码=%s, 原始链接=%s, 点击次数=%d, 耗时=%dms", 
                shortCode, originalUrl, shortUrl.getClickCount() + 1, duration));
            
            return originalUrl;
            
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            LogUtil.logBusinessFailure(operation, String.format(
                "跳转失败，短码=%s, 耗时=%dms", shortCode, duration), e);
            throw e;
        }
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
        for (int i = 0; i < code_length; i++) {
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
        long startTime = System.currentTimeMillis();
        String operation = "ADMIN_CREATE_SHORT_URL";
        
        try {
            LogUtil.setOperation(operation);
            LogUtil.setUserContext(null, "admin");
            
            log.info("管理员创建短链接: name={}, originalUrl={}", request.getName(), request.getOriginalUrl());
            LogUtil.logBusiness(operation, String.format("管理员创建短链接，名称=%s, 原始链接=%s", 
                request.getName(), request.getOriginalUrl()));

            // 参数校验
            if (request.getOriginalUrl() == null || request.getOriginalUrl().trim().isEmpty()) {
                String errorMsg = "原始链接不能为空";
                LogUtil.logBusinessFailure(operation, errorMsg, new IllegalArgumentException(errorMsg));
                throw new IllegalArgumentException(errorMsg);
            }
            if (!request.getOriginalUrl().matches("^https?://.+")) {
                String errorMsg = "原始链接格式不正确，请以http://或https://开头";
                LogUtil.logBusinessFailure(operation, errorMsg, new IllegalArgumentException(errorMsg));
                throw new IllegalArgumentException(errorMsg);
            }

            // 生成短码
            String shortCode;
            if (request.getShortCode() != null && !request.getShortCode().trim().isEmpty()) {
                // 使用指定的短码
                shortCode = request.getShortCode().trim();
                if (shortUrlRepository.existsByShortCode(shortCode)) {
                    String errorMsg = "短码已存在: " + shortCode;
                    LogUtil.logBusinessFailure(operation, errorMsg, new IllegalArgumentException(errorMsg));
                    throw new IllegalArgumentException(errorMsg);
                }
                log.info("使用指定短码: {}", shortCode);
            } else {
                // 自动生成短码
                shortCode = generateUniqueShortCode();
                log.info("自动生成短码: {}", shortCode);
            }

            // 创建短链接实体
            ShortUrl shortUrl = new ShortUrl();
            shortUrl.setName(request.getName());
            shortUrl.setOriginalUrl(request.getOriginalUrl().trim());
            shortUrl.setShortCode(shortCode);
            shortUrl.setStatus(request.getStatus() != null ? request.getStatus() : ShortUrlStatus.ENABLED);
            shortUrl.setClickCount(0L);
            shortUrl.setAppId(request.getAppId());
            shortUrl.setUserId(null);
            if (request.getExpiresAt() != null) {
                shortUrl.setExpiresAt(request.getExpiresAt());
            } else {
                shortUrl.setExpiresAt(LocalDateTime.now().plusDays(defaultExpireDays));
            }
            shortUrl.setCreatedAt(LocalDateTime.now());
            shortUrl.setUpdatedAt(LocalDateTime.now());

            ShortUrl saved = shortUrlRepository.save(shortUrl);
            long duration = System.currentTimeMillis() - startTime;
            
            log.info("管理员创建短链接成功，ID: {}, 短码: {}, 耗时: {}ms", saved.getId(), shortCode, duration);
            LogUtil.logBusinessSuccess(operation, String.format(
                "创建成功，ID=%d, 短码=%s, 名称=%s, 原始链接=%s, 耗时=%dms", 
                saved.getId(), shortCode, request.getName(), request.getOriginalUrl(), duration));
            
            return saved;
            
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            LogUtil.logBusinessFailure(operation, String.format(
                "创建失败，名称=%s, 原始链接=%s, 耗时=%dms", 
                request.getName(), request.getOriginalUrl(), duration), e);
            throw e;
        }
    }

    /**
     * 管理员更新短链接
     */
    @Transactional
    @CacheEvict(value = {"shortUrl", "shortUrlDetail"}, key = "#id")
    public ShortUrl updateShortUrlByAdmin(Long id, ShortUrlCreateRequest request) {
        long startTime = System.currentTimeMillis();
        String operation = "ADMIN_UPDATE_SHORT_URL";
        
        try {
            LogUtil.setOperation(operation);
            LogUtil.setUserContext(null, "admin");
            
            log.info("管理员更新短链接: id={}, name={}", id, request.getName());
            LogUtil.logBusiness(operation, String.format("管理员更新短链接，ID=%d, 名称=%s", id, request.getName()));

            // 查找现有记录
            ShortUrl existing = shortUrlRepository.findById(id)
                    .orElseThrow(() -> {
                        String errorMsg = "短链接不存在，ID: " + id;
                        LogUtil.logBusinessFailure(operation, errorMsg, new RuntimeException(errorMsg));
                        return new RuntimeException(errorMsg);
                    });

            // 如果短码被修改，检查新短码是否已存在
            if (request.getShortCode() != null && !request.getShortCode().equals(existing.getShortCode())) {
                if (shortUrlRepository.existsByShortCode(request.getShortCode())) {
                    String errorMsg = "短码已存在: " + request.getShortCode();
                    LogUtil.logBusinessFailure(operation, errorMsg, new IllegalArgumentException(errorMsg));
                    throw new IllegalArgumentException(errorMsg);
                }
                log.info("更新短码: {} -> {}", existing.getShortCode(), request.getShortCode());
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
            long duration = System.currentTimeMillis() - startTime;
            
            log.info("管理员更新短链接成功，ID: {}, 耗时: {}ms", id, duration);
            LogUtil.logBusinessSuccess(operation, String.format(
                "更新成功，ID=%d, 短码=%s, 名称=%s, 耗时=%dms", 
                id, updated.getShortCode(), request.getName(), duration));
            
            return updated;
            
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            LogUtil.logBusinessFailure(operation, String.format(
                "更新失败，ID=%d, 名称=%s, 耗时=%dms", id, request.getName(), duration), e);
            throw e;
        }
    }

    /**
     * 管理员删除短链接
     */
    @Transactional
    @CacheEvict(value = {"shortUrl", "shortUrlDetail"}, allEntries = true)
    public void deleteShortUrlByAdmin(Long id) {
        long startTime = System.currentTimeMillis();
        String operation = "ADMIN_DELETE_SHORT_URL";
        
        try {
            LogUtil.setOperation(operation);
            LogUtil.setUserContext(null, "admin");
            
            log.info("管理员删除短链接: id={}", id);
            LogUtil.logBusiness(operation, String.format("管理员删除短链接，ID=%d", id));

            if (!shortUrlRepository.existsById(id)) {
                String errorMsg = "短链接不存在，ID: " + id;
                LogUtil.logBusinessFailure(operation, errorMsg, new RuntimeException(errorMsg));
                throw new RuntimeException(errorMsg);
            }

            shortUrlRepository.deleteById(id);
            long duration = System.currentTimeMillis() - startTime;
            
            log.info("管理员删除短链接成功，ID: {}, 耗时: {}ms", id, duration);
            LogUtil.logBusinessSuccess(operation, String.format("删除成功，ID=%d, 耗时=%dms", id, duration));
            
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            LogUtil.logBusinessFailure(operation, String.format("删除失败，ID=%d, 耗时=%dms", id, duration), e);
            throw e;
        }
    }

    /**
     * 管理员批量删除短链接
     */
    @Transactional
    @CacheEvict(value = {"shortUrl", "shortUrlDetail"}, allEntries = true)
    public void batchDeleteShortUrlsByAdmin(List<Long> ids) {
        long startTime = System.currentTimeMillis();
        String operation = "ADMIN_BATCH_DELETE_SHORT_URL";
        
        try {
            LogUtil.setOperation(operation);
            LogUtil.setUserContext(null, "admin");
            
            log.info("管理员批量删除短链接，IDs: {}", ids);
            LogUtil.logBusiness(operation, String.format("管理员批量删除短链接，IDs数量=%d", ids.size()));
            
            shortUrlRepository.deleteAllById(ids);
            long duration = System.currentTimeMillis() - startTime;
            
            log.info("管理员批量删除成功，共删除 {} 条记录，耗时: {}ms", ids.size(), duration);
            LogUtil.logBusinessSuccess(operation, String.format(
                "批量删除成功，删除数量=%d, IDs=%s, 耗时=%dms", ids.size(), ids, duration));
            
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            LogUtil.logBusinessFailure(operation, String.format(
                "批量删除失败，IDs数量=%d, 耗时=%dms", ids.size(), duration), e);
            throw e;
        }
    }
    
    /**
     * 定时任务：自动清理过期链接
     * 每天凌晨2点执行一次
     */
    // @Scheduled(cron = "0 0 2 * * ?")  // 注释掉定时任务，在需要时可启用
    public void autoCleanupExpiredUrls() {
        long startTime = System.currentTimeMillis();
        String operation = "AUTO_CLEANUP_EXPIRED_URLS";
        
        try {
            LogUtil.setOperation(operation);
            log.info("开始执行自动清理过期链接任务");
            LogUtil.logBusiness(operation, "开始执行自动清理过期链接任务");
            
            List<ShortUrl> expiredUrls = shortUrlRepository.findByExpiresAtBeforeAndStatus(
                LocalDateTime.now(), ShortUrlStatus.ENABLED);
            
            int deletedCount = expiredUrls.size();
            if (deletedCount > 0) {
                List<Long> expiredIds = expiredUrls.stream()
                    .map(ShortUrl::getId)
                    .toList();
                shortUrlRepository.deleteAllById(expiredIds);
                
                log.info("自动清理完成，共清理 {} 条过期链接", deletedCount);
                LogUtil.logBusinessSuccess(operation, String.format(
                    "自动清理完成，清理数量=%d, 过期链接IDs=%s", deletedCount, expiredIds));
            } else {
                log.info("没有发现过期链接，无需清理");
                LogUtil.logBusinessSuccess(operation, "没有发现过期链接，无需清理");
            }
            
            long duration = System.currentTimeMillis() - startTime;
            log.info("自动清理任务完成，耗时：{}ms", duration);
            
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("自动清理过期链接失败，耗时：{}ms", duration, e);
            LogUtil.logBusinessFailure(operation, String.format(
                "自动清理失败，耗时=%dms", duration), e);
        }
    }
}