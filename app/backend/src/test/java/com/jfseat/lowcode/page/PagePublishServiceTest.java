package com.jfseat.lowcode.page;

import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PagePublishServiceTest {

    /**
     * 验证达索响应附带script和HTML前缀时仍可读取末尾JSON
     **
     * @throws Exception JSON解析异常
     * @author caipan by codex
     * @date 2026/9/5 16:30
     */
    @Test
    void extractsJsonAfterDassaultHtmlPrefix() throws Exception {
        var service = new PagePublishService(null, null, new ObjectMapper(), null, null);
        var result = service.extractJsonPayload("<script>window.test={enabled:true};</script>\n<!DOCTYPE html>\n"
                + "{\"msg\":\"发布成功\",\"data\":{\"name\":\"JF_TEST\"},\"status\":0}"
                + "\n<script src=\"emxUICore.js\"></script></body></html>");
        assertEquals(0, result.path("status").asInt());
        assertEquals("发布成功", result.path("msg").asText());
    }

    /**
     * 验证TWXTicketService的Java Map文本可识别为发布成功
     **
     * @throws Exception 响应解析异常
     * @author caipan by codex
     * @date 2026/9/5 22:45
     */
    @Test
    void extractsTicketServiceJavaMapResult() throws Exception {
        var service = new PagePublishService(null, null, new ObjectMapper(), null, null);
        var result = service.extractJsonPayload(
                "{replaced=true, contentBytes=3324, version=6, pageCode=JF_COMPETITIVE_BOM_CREATE}");
        assertEquals(0, result.path("status").asInt());
        assertEquals("JF_COMPETITIVE_BOM_CREATE", result.path("data").path("pageCode").asText());
    }
}
