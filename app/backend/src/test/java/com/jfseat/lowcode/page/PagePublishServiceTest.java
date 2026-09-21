package com.jfseat.lowcode.page;

import com.jfseat.lowcode.plm.PlmActionResponse;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PagePublishServiceTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

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

    /**
     * 发布前拒绝V2事件引用不存在的页面组件
     **
     * @throws Exception JSON构造失败
     * @author caipan by codex
     * @date 2026/9/13 12:10
     */
    @Test
    void rejectsMissingEventComponentBeforePublish() throws Exception {
        var service = new PagePublishService(null, null, objectMapper, null, null);
        var schema = objectMapper.readTree("{\"type\":\"page\",\"body\":[{\"id\":\"u:list\",\"type\":\"crud\"}]}");
        var config = objectMapper.readTree("{\"eventBindings\":[{\"id\":\"reload\","
                + "\"source\":{\"componentId\":\"u:missing\",\"event\":\"click\"},"
                + "\"action\":{\"actionCode\":\"QUERY_LIST\",\"inputMapping\":{}},"
                + "\"success\":[],\"failure\":[]}]}");
        assertThrows(ResponseStatusException.class, () -> service.validatePageBindings(schema, config));
    }

    /**
     * 合法V2事件及指定组件刷新效果可以通过发布前校验
     **
     * @throws Exception JSON构造失败
     * @author caipan by codex
     * @date 2026/9/13 12:10
     */
    @Test
    void acceptsValidEventBindingsBeforePublish() throws Exception {
        var service = new PagePublishService(null, null, objectMapper, null, null);
        var schema = objectMapper.readTree("{\"type\":\"page\",\"body\":[{\"id\":\"u:list\",\"type\":\"crud\"},{\"id\":\"u:query\",\"type\":\"button\"}]}");
        var config = objectMapper.readTree("{\"eventBindings\":[{\"id\":\"reload\","
                + "\"source\":{\"componentId\":\"u:query\",\"event\":\"click\"},"
                + "\"action\":{\"actionCode\":\"QUERY_LIST\",\"inputMapping\":{}},"
                + "\"success\":[{\"type\":\"RELOAD\",\"target\":\"u:list\"}],\"failure\":[]}]}");
        service.validatePageBindings(schema, config);
    }

    /**
     * V1刷新事件升级为V2后允许RELOAD不指定组件，表示刷新当前运行页面
     **
     * @throws Exception JSON构造失败
     * @author caipan by codex
     * @date 2026/9/17 14:05
     */
    @Test
    void acceptsPageReloadWithoutTargetBeforePublish() throws Exception {
        var service = new PagePublishService(null, null, objectMapper, null, null);
        var schema = objectMapper.readTree("{\"type\":\"page\",\"body\":[{\"id\":\"u:query\",\"type\":\"button\"}]}");
        var config = objectMapper.readTree("{\"eventBindings\":[{\"id\":\"reload-page\","
                + "\"source\":{\"componentId\":\"u:query\",\"event\":\"click\"},"
                + "\"action\":{\"actionCode\":\"QUERY_LIST\",\"inputMapping\":{}},"
                + "\"success\":[{\"type\":\"RELOAD\"}],\"failure\":[]}]}");
        service.validatePageBindings(schema, config);
    }

    /**
     * 发布前拒绝组件类型无法触发的事件
     **
     * @throws Exception JSON构造失败
     * @author caipan by codex
     * @date 2026/9/17 14:30
     */
    @Test
    void rejectsEventUnsupportedByComponentType() throws Exception {
        var service = new PagePublishService(null, null, objectMapper, null, null);
        var schema = objectMapper.readTree("{\"type\":\"page\",\"body\":[{\"id\":\"u:list\",\"type\":\"crud\"}]}");
        var config = objectMapper.readTree("{\"eventBindings\":[{\"id\":\"submit-table\","
                + "\"source\":{\"componentId\":\"u:list\",\"event\":\"submit\"},"
                + "\"action\":{\"actionCode\":\"QUERY_LIST\",\"inputMapping\":{}},"
                + "\"success\":[],\"failure\":[]}]}");
        assertThrows(ResponseStatusException.class, () -> service.validatePageBindings(schema, config));
    }

    /**
     * 发布前拒绝非字符串的拖入类型白名单
     **
     * @throws Exception JSON构造失败
     * @author caipan by codex
     * @date 2026/9/17 14:35
     */
    @Test
    void rejectsInvalidDropAcceptedTypes() throws Exception {
        var service = new PagePublishService(null, null, objectMapper, null, null);
        var schema = objectMapper.readTree("{\"type\":\"page\",\"body\":[{\"id\":\"u:list\",\"type\":\"crud\"}]}");
        var config = objectMapper.readTree("{\"eventBindings\":[{\"id\":\"drop-table\","
                + "\"source\":{\"componentId\":\"u:list\",\"event\":\"drop\",\"acceptedTypes\":[1]},"
                + "\"action\":{\"actionCode\":\"QUERY_ROW\",\"inputMapping\":{}},"
                + "\"success\":[],\"failure\":[]}]}");
        assertThrows(ResponseStatusException.class, () -> service.validatePageBindings(schema, config));
    }

    /**
     * 发布前拒绝输入映射中的空参数名
     **
     * @throws Exception JSON构造失败
     * @author caipan by codex
     * @date 2026/9/21 11:20
     */
    @Test
    void rejectsBlankActionInputParameterName() throws Exception {
        var service = new PagePublishService(null, null, objectMapper, null, null);
        var schema = objectMapper.readTree("{\"type\":\"page\",\"body\":[{\"id\":\"u:query\",\"type\":\"button\"}]}");
        var config = objectMapper.readTree("{\"eventBindings\":[{\"id\":\"query\","
                + "\"source\":{\"componentId\":\"u:query\",\"event\":\"click\"},"
                + "\"action\":{\"actionCode\":\"QUERY_LIST\",\"inputMapping\":{\"\":\"${objectId}\"}},"
                + "\"success\":[],\"failure\":[]}]}");
        assertThrows(ResponseStatusException.class, () -> service.validatePageBindings(schema, config));
    }

    /**
     * 发布前拒绝事件缺少公共动作必填参数
     **
     * @throws Exception JSON构造失败
     * @author caipan by codex
     * @date 2026/9/21 14:40
     */
    @Test
    void rejectsMissingRequiredActionParameter() throws Exception {
        var service = new PagePublishService(null, null, objectMapper, null, null);
        var config = objectMapper.readTree("{\"eventBindings\":[{\"id\":\"query\","
                + "\"action\":{\"actionCode\":\"QUERY_LIST\",\"inputMapping\":{}},"
                + "\"success\":[],\"failure\":[]}]}");
        var action = new PlmActionResponse("QUERY_LIST", "查询", "QUERY", "JF_Test", "query",
                "POST", objectMapper.readTree("{\"objectId\":\"${objectId}\"}"), objectMapper.readTree("{}"),
                objectMapper.readTree("[{\"name\":\"objectId\",\"dataType\":\"STRING\",\"required\":true,\"description\":\"对象ID\"}]"),
                objectMapper.readTree("[]"), true, Instant.EPOCH);
        assertThrows(ResponseStatusException.class,
                () -> service.validateEventActionParameters(config, List.of(action)));
    }

    /**
     * 发布前允许动态模板满足带类型的必填参数
     **
     * @throws Exception JSON构造失败
     * @author caipan by codex
     * @date 2026/9/21 14:40
     */
    @Test
    void acceptsMappedRequiredActionParameter() throws Exception {
        var service = new PagePublishService(null, null, objectMapper, null, null);
        var config = objectMapper.readTree("{\"eventBindings\":[{\"id\":\"query\","
                + "\"action\":{\"actionCode\":\"QUERY_LIST\",\"inputMapping\":{\"objectId\":\"${plmContext.objectId}\"}},"
                + "\"success\":[],\"failure\":[]}]}");
        var action = new PlmActionResponse("QUERY_LIST", "查询", "QUERY", "JF_Test", "query",
                "POST", objectMapper.readTree("{\"objectId\":\"${objectId}\"}"), objectMapper.readTree("{}"),
                objectMapper.readTree("[{\"name\":\"objectId\",\"dataType\":\"STRING\",\"required\":true,\"description\":\"对象ID\"}]"),
                objectMapper.readTree("[]"), true, Instant.EPOCH);
        service.validateEventActionParameters(config, List.of(action));
    }
}
