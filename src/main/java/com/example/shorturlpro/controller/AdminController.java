package com.example.shorturlpro.controller;

import com.example.shorturlpro.dto.ApiResponse;
import com.example.shorturlpro.dto.ShortUrlCreateRequest;
import com.example.shorturlpro.dto.ShortUrlResponse;
import com.example.shorturlpro.entity.ShortUrl;
import com.example.shorturlpro.entity.ShortUrlStatus;
import com.example.shorturlpro.repository.ShortUrlRepository;
import com.example.shorturlpro.service.ShortUrlService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 管理员专用控制器
 * 提供管理员对所有短链接的完全管理权限
 */
@Slf4j
@RestController
@RequestMapping("/api/short-url/admin")
@RequiredArgsConstructor
@Tag(name = "管理员接口", description = "管理员专用的短链接管理接口")
public class AdminController {

    private final ShortUrlRepository shortUrlRepository;
    private final ShortUrlService shortUrlService;

    /**
     * 获取统计信息（管理员权限）
     */
    @GetMapping("/stats")
    @Operation(
            summary = "获取统计信息",
            description = "管理员获取系统统计信息\n\n**权限要求**：ROLE_ADMIN"
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "获取成功",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = Map.class)))
    public ResponseEntity<ApiResponse<Map<String, Object>>> getStats() {
        try {
            log.info("管理员查询统计信息");
            
            Map<String, Object> stats = new HashMap<>();
            
            // 总链接数
            long totalUrls = shortUrlRepository.count();
            stats.put("totalUrls", totalUrls);
            
            // 总点击量
            long totalClicks = shortUrlRepository.sumAllClickCounts();
            stats.put("totalClicks", totalClicks);
            
            // 今日点击量
            LocalDate today = LocalDate.now();
            LocalDateTime startOfDay = today.atStartOfDay();
            LocalDateTime endOfDay = today.atTime(23, 59, 59);
            long todayClicks = shortUrlRepository.sumClickCountsByDateRange(startOfDay, endOfDay);
            stats.put("todayClicks", todayClicks);
            
            // 活跃链接数（启用状态的链接）
            long activeUrls = shortUrlRepository.countByStatus(ShortUrlStatus.ENABLED);
            stats.put("activeUrls", activeUrls);
            
            log.info("统计信息查询完成: totalUrls={}, totalClicks={}, todayClicks={}, activeUrls={}", 
                    totalUrls, totalClicks, todayClicks, activeUrls);
                        
            return ResponseEntity.ok(ApiResponse.success(stats));
            
        } catch (Exception e) {
            log.error("查询统计信息失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("查询统计信息失败: " + e.getMessage()));
        }
    }

    /**
     * 获取所有短链接列表（管理员权限）
     */
    @GetMapping("/list")
    @Operation(
            summary = "获取短链接列表",
            description = "管理员获取所有短链接列表，支持分页、搜索和筛选\n\n**权限要求**：ROLE_ADMIN"
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "获取成功",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = Page.class)))
    public ResponseEntity<ApiResponse<Page<ShortUrlResponse>>> getAllShortUrls(
            @Parameter(description = "页码（从0开始）") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "搜索关键词") @RequestParam(required = false) String search,
            @Parameter(description = "状态筛选") @RequestParam(required = false) String status,
            @Parameter(description = "排序字段") @RequestParam(defaultValue = "createdAt,desc") String sort) {

        try {
            log.info("管理员查询短链接列表: page={}, size={}, search={}, status={}, sort={}", 
                    page, size, search, status, sort);

            // 解析排序参数
            String[] sortParts = sort.split(",");
            Sort.Direction direction = sortParts.length > 1 && "asc".equalsIgnoreCase(sortParts[1]) 
                    ? Sort.Direction.ASC : Sort.Direction.DESC;
            Sort sortBy = Sort.by(direction, sortParts[0]);

            Pageable pageable = PageRequest.of(page, size, sortBy);

            Page<ShortUrl> shortUrlPage;
            if (search != null && !search.trim().isEmpty()) {
                if (status != null && !status.trim().isEmpty()) {
                    ShortUrlStatus statusEnum = ShortUrlStatus.valueOf(status.toUpperCase());
                    shortUrlPage = shortUrlRepository.findByShortCodeContainingOrOriginalUrlContainingAndStatus(
                            search, statusEnum, pageable);
                } else {
                    shortUrlPage = shortUrlRepository.findByShortCodeContainingOrOriginalUrlContaining(
                            search, pageable);
                }
            } else if (status != null && !status.trim().isEmpty()) {
                ShortUrlStatus statusEnum = ShortUrlStatus.valueOf(status.toUpperCase());
                shortUrlPage = shortUrlRepository.findByStatus(statusEnum, pageable);
            } else {
                shortUrlPage = shortUrlRepository.findAll(pageable);
            }

            Page<ShortUrlResponse> responsePage = shortUrlPage.map(this::convertToResponse);
            
            log.info("查询完成，共找到 {} 条记录", responsePage.getTotalElements());
            
            return ResponseEntity.ok(ApiResponse.success(responsePage));

        } catch (Exception e) {
            log.error("查询短链接列表失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("查询短链接列表失败: " + e.getMessage()));
        }
    }

    /**
     * 创建新的短链接（管理员权限）
     */
    @PostMapping
    @Operation(
            summary = "创建短链接",
            description = "管理员创建新的短链接\n\n**权限要求**：ROLE_ADMIN"
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "创建成功",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = ShortUrlResponse.class)))
    public ResponseEntity<ApiResponse<ShortUrlResponse>> createShortUrl(@Valid @RequestBody ShortUrlCreateRequest request) {
        try {
            log.info("管理员创建短链接: name={}, originalUrl={}", request.getName(), request.getOriginalUrl());
            
            ShortUrl shortUrl = shortUrlService.createShortUrlByAdmin(request);
            ShortUrlResponse response = convertToResponse(shortUrl);
            
            log.info("短链接创建成功: id={}, shortCode={}", shortUrl.getId(), shortUrl.getShortCode());
            
            return ResponseEntity.ok(ApiResponse.success(response));
            
        } catch (Exception e) {
            log.error("创建短链接失败", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.badRequest("创建短链接失败: " + e.getMessage()));
        }
    }

    /**
     * 更新短链接（管理员权限）
     */
    @PutMapping("/{id}")
    @Operation(
            summary = "更新短链接",
            description = "管理员更新短链接信息\n\n**权限要求**：ROLE_ADMIN"
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "更新成功")
    public ResponseEntity<ApiResponse<ShortUrlResponse>> updateShortUrl(
            @Parameter(description = "短链接ID") @PathVariable Long id,
            @Valid @RequestBody ShortUrlCreateRequest request) {
        
        try {
            log.info("管理员更新短链接: id={}, name={}", id, request.getName());
            
            ShortUrl updatedShortUrl = shortUrlService.updateShortUrlByAdmin(id, request);
            
            log.info("短链接更新成功: id={}", id);
            
            return ResponseEntity.ok(ApiResponse.success(convertToResponse(updatedShortUrl)));
            
        } catch (RuntimeException e) {
            log.warn("更新短链接失败: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.badRequest("更新短链接失败: " + e.getMessage()));
        } catch (Exception e) {
            log.error("更新短链接失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("更新短链接失败: " + e.getMessage()));
        }
    }

    /**
     * 删除短链接（管理员权限）
     */
    @DeleteMapping("/{id}")
    @Operation(
            summary = "删除短链接",
            description = "管理员删除短链接\n\n**权限要求**：ROLE_ADMIN"
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "删除成功")
    public ResponseEntity<ApiResponse<Object>> deleteShortUrl(
            @Parameter(description = "短链接ID") @PathVariable Long id) {
        
        try {
            log.info("管理员删除短链接: id={}", id);
            
            shortUrlService.deleteShortUrlByAdmin(id);
            
            log.info("短链接删除成功: id={}", id);
            return ResponseEntity.ok(ApiResponse.success("删除成功", null));
            
        } catch (RuntimeException e) {
            log.warn("删除短链接失败: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.badRequest("删除短链接失败: " + e.getMessage()));
        } catch (Exception e) {
            log.error("删除短链接失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("删除短链接失败: " + e.getMessage()));
        }
    }

    /**
     * 批量删除短链接（管理员权限）
     */
    @DeleteMapping("/batch")
    @Operation(
            summary = "批量删除短链接",
            description = "管理员批量删除多个短链接\n\n**权限要求**：ROLE_ADMIN"
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "批量删除成功")
    public ResponseEntity<ApiResponse<Object>> batchDeleteShortUrls(@RequestBody List<Long> ids) {
        try {
            log.info("管理员批量删除短链接: ids={}", ids);
            
            shortUrlService.batchDeleteShortUrlsByAdmin(ids);
            
            log.info("批量删除成功，共删除 {} 条记录", ids.size());
            return ResponseEntity.ok(ApiResponse.success("批量删除成功，共删除 " + ids.size() + " 条记录", null));
            
        } catch (Exception e) {
            log.error("批量删除失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("批量删除失败: " + e.getMessage()));
        }
    }

    /**
     * 导出短链接数据为Excel（管理员权限）
     */
    @GetMapping("/export")
    @Operation(
            summary = "导出短链接数据",
            description = "管理员导出短链接数据为Excel文件\n\n**权限要求**：ROLE_ADMIN"
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "导出成功")
    public ResponseEntity<byte[]> exportShortUrls(
            @Parameter(description = "搜索关键词") @RequestParam(required = false) String search,
            @Parameter(description = "状态筛选") @RequestParam(required = false) String status) {
        
        try {
            log.info("管理员导出短链接数据: search={}, status={}", search, status);

            List<ShortUrl> shortUrls;
            if (search != null && !search.trim().isEmpty()) {
                if (status != null && !status.trim().isEmpty()) {
                    ShortUrlStatus statusEnum = ShortUrlStatus.valueOf(status.toUpperCase());
                    shortUrls = shortUrlRepository.findByShortCodeContainingOrOriginalUrlContainingAndStatus(
                            search, statusEnum);
                } else {
                    shortUrls = shortUrlRepository.findByShortCodeContainingOrOriginalUrlContaining(
                            search);
                }
            } else if (status != null && !status.trim().isEmpty()) {
                ShortUrlStatus statusEnum = ShortUrlStatus.valueOf(status.toUpperCase());
                shortUrls = shortUrlRepository.findByStatus(statusEnum);
            } else {
                shortUrls = shortUrlRepository.findAll();
            }

            byte[] excelData = generateExcel(shortUrls);
            
            HttpHeaders headers = new HttpHeaders();
            String filename = "shorturls_" + LocalDateTime.now().toLocalDate() + ".xlsx";
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", 
                    URLEncoder.encode(filename, StandardCharsets.UTF_8));

            log.info("导出完成，共 {} 条记录", shortUrls.size());
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(excelData);

        } catch (Exception e) {
            log.error("导出失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 生成Excel文件
     */
    private byte[] generateExcel(List<ShortUrl> shortUrls) throws Exception {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("短链接数据");

        // 创建标题行
        Row headerRow = sheet.createRow(0);
        String[] headers = {"ID", "名称", "短码", "原链接", "状态", "点击量", "创建者", "创建时间", "过期时间"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            
            // 设置标题样式
            CellStyle headerStyle = workbook.createCellStyle();
            Font font = workbook.createFont();
            font.setBold(true);
            headerStyle.setFont(font);
            cell.setCellStyle(headerStyle);
        }

        // 填充数据
        int rowNum = 1;
        for (ShortUrl url : shortUrls) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(url.getId());
            row.createCell(1).setCellValue(url.getName() != null ? url.getName() : "");
            row.createCell(2).setCellValue(url.getShortCode());
            row.createCell(3).setCellValue(url.getOriginalUrl());
            row.createCell(4).setCellValue(url.getStatus().toString());
            row.createCell(5).setCellValue(url.getClickCount());
            row.createCell(6).setCellValue(url.getUserId() != null ? "用户" + url.getUserId() : "匿名");
            row.createCell(7).setCellValue(url.getCreatedAt() != null ? url.getCreatedAt().toString() : "");
            row.createCell(8).setCellValue(url.getExpiresAt() != null ? url.getExpiresAt().toString() : "");
        }

        // 自动调整列宽
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();

        return outputStream.toByteArray();
    }

    /**
     * 转换实体为响应DTO
     */
    private ShortUrlResponse convertToResponse(ShortUrl shortUrl) {
        ShortUrlResponse response = new ShortUrlResponse();
        response.setId(shortUrl.getId());
        response.setName(shortUrl.getName());
        response.setShortCode(shortUrl.getShortCode());
        response.setOriginalUrl(shortUrl.getOriginalUrl());
        response.setStatus(shortUrl.getStatus().name());
        response.setClickCount(shortUrl.getClickCount());
        response.setUserId(shortUrl.getUserId());
        response.setAppId(shortUrl.getAppId());
        response.setExpiresAt(shortUrl.getExpiresAt());
        response.setCreatedAt(shortUrl.getCreatedAt());
        response.setUpdatedAt(shortUrl.getUpdatedAt());
        return response;
    }
}