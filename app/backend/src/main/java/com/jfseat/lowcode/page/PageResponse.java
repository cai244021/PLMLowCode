package com.jfseat.lowcode.page;

import tools.jackson.databind.JsonNode;

import java.time.Instant;

public record PageResponse(
        String pageCode,
        String pageName,
        int currentVersion,
        JsonNode schema,
        JsonNode plmConfig,
        Instant updatedAt
) {
    /**
     * 将页面实体转换成接口响应
     **
     * @param page 页面实体
     * @return 页面响应
     * @author caipan by codex
     * @date 2026/9/3 10:00
     */
    public static PageResponse from(LowCodePage page) {
        return new PageResponse(
                page.getPageCode(),
                page.getPageName(),
                page.getCurrentVersion(),
                page.getSchemaJson(),
                page.getPlmConfigJson(),
                page.getUpdatedAt()
        );
    }
}
