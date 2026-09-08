package com.jfseat.lowcode.page;

import com.jfseat.lowcode.integration.PlmIntegrationProperties;
import com.jfseat.lowcode.plm.PlmFieldService;
import com.jfseat.lowcode.plm.PlmFieldResponse;
import com.jfseat.lowcode.plm.PlmActionResponse;
import com.jfseat.lowcode.plm.PlmActionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.server.ResponseStatusException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.LinkedHashMap;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;
import java.util.LinkedHashSet;
import java.util.Set;

@Service
public class PagePublishService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PagePublishService.class);
    private final PageService pageService;
    private final PlmIntegrationProperties properties;
    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final PlmFieldService plmFieldService;
    private final PlmActionService plmActionService;

    public PagePublishService(PageService pageService,
                              PlmIntegrationProperties properties,
                              ObjectMapper objectMapper,
                              PlmFieldService plmFieldService,
                              PlmActionService plmActionService) {
        this.pageService = pageService;
        this.properties = properties;
        this.restClient = RestClient.create();
        this.objectMapper = objectMapper;
        this.plmFieldService = plmFieldService;
        this.plmActionService = plmActionService;
    }

    /**
     * 将设计器当前已保存版本通过TWXTicketService发布到PLM Page
     **
     * @param pageCode 页面编码
     * @return 发布结果
     * @throws ResponseStatusException 页面不存在、配置缺失或PLM调用失败
     * @author caipan by codex
     * @date 2026/9/5 16:30
     */
    public PagePublishResponse publish(String pageCode) {
        PageResponse page = pageService.findByPageCode(pageCode)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "页面不存在"));
        validateConfiguration();

        Map<String, Object> pagePackage = new LinkedHashMap<>();
        pagePackage.put("formatVersion", 1);
        pagePackage.put("pageCode", page.pageCode());
        pagePackage.put("pageName", page.pageName());
        pagePackage.put("version", page.currentVersion());
        pagePackage.put("schema", page.schema());
        pagePackage.put("plmConfig", page.plmConfig());
        Map<String, Object> resources = new LinkedHashMap<>();
        List<String> fieldCodes = readFieldCodes(page.plmConfig());
        List<PlmFieldResponse> fields = plmFieldService.findByCodes(fieldCodes);
        resources.put("fields", fields);
        //20260906 update by caipan 页面只发布实际引用的已启用动作，PLM运行端据此解析JPO调用。
        boolean hasPlmRange = fields.stream().anyMatch(field -> "PLM_RANGE".equals(field.rangeSource()));
        List<String> actionCodes = readActionCodes(page.plmConfig(), !fields.isEmpty(), hasPlmRange);
        List<PlmActionResponse> actions = plmActionService.findEnabledByCodes(actionCodes);
        validatePublishedActions(actionCodes, actions);
        resources.put("actions", actions);
        pagePackage.put("resources", resources);

        String baseUrl = properties.baseUrl().replaceAll("/+$", "");
        try {
            String responseBody = restClient.post()
                    .uri(baseUrl + "/TWXPublicRest/TWXTicketService"
                            + "?JPOName=JF_LowCodePage&FuncName=publishPage")
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .header("SecurityContext", properties.securityContext())
                    .header("X-3DSLogin-ticket", properties.loginTicket())
                    .body(pagePackage)
                    .retrieve()
                    .body(String.class);
            JsonNode result;
            try {
                result = extractJsonPayload(responseBody);
            } catch (Exception exception) {
                LOGGER.warn("PLM Page发布响应无法解析，pageCode={}，response={}",
                        pageCode, responsePreview(responseBody));
                throw exception;
            }
            if (result.path("status").asInt(1) != 0) {
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                        "PLM发布失败：" + result.path("msg").asText("未知错误"));
            }
            return new PagePublishResponse(page.pageCode(), page.currentVersion(),
                    result.path("msg").asText("发布成功"));
        } catch (ResponseStatusException exception) {
            LOGGER.warn("PLM Page发布业务失败，pageCode={}，reason={}", pageCode, exception.getReason());
            throw exception;
        } catch (RestClientResponseException exception) {
            LOGGER.warn("PLM Page发布HTTP失败，pageCode={}，status={}",
                    pageCode, exception.getStatusCode().value(), exception);
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                    "PLM发布失败：HTTP " + exception.getStatusCode().value());
        } catch (Exception exception) {
            LOGGER.warn("PLM Page发布调用失败，pageCode={}，exception={}",
                    pageCode, exception.getClass().getName(), exception);
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                    "PLM发布失败：" + rootCauseMessage(exception));
        }
    }

    /**
     * 读取页面PLM配置中声明的字段编码
     **
     * @param plmConfig 页面PLM配置
     * @return 去除空值后的字段编码列表
     * @author caipan by codex
     * @date 2026/9/6 16:30
     */
    private List<String> readFieldCodes(JsonNode plmConfig) {
        List<String> fieldCodes = new ArrayList<>();
        JsonNode source = plmConfig == null ? null : plmConfig.path("fieldCodes");
        if (source != null && source.isArray()) {
            source.forEach(item -> {
                String code = item.asText("").trim();
                if (!code.isEmpty() && !fieldCodes.contains(code)) {
                    fieldCodes.add(code);
                }
            });
        }
        return fieldCodes;
    }

    /**
     * 读取页面引用动作并补充Runtime字段元数据动作
     **
     * @param plmConfig 页面PLM配置
     * @param hasFields 页面是否引用PLM字段
     * @param hasPlmRange 页面是否包含PLM属性Range字段
     * @return 去重后的动作编码
     * @author caipan by codex
     * @date 2026/9/6 22:10
     */
    private List<String> readActionCodes(JsonNode plmConfig, boolean hasFields, boolean hasPlmRange) {
        Set<String> actionCodes = new LinkedHashSet<>();
        JsonNode source = plmConfig == null ? null : plmConfig.path("actionCodes");
        if (source != null && source.isArray()) {
            source.forEach(item -> {
                String code = item.asText("").trim();
                if (!code.isEmpty()) {
                    actionCodes.add(code);
                }
            });
        }
        if (hasFields) {
            actionCodes.add("QUERY_PAGE_FIELD_METADATA");
        }
        if (hasPlmRange) {
            actionCodes.add("QUERY_ATTRIBUTE_RANGE");
        }
        return new ArrayList<>(actionCodes);
    }

    /**
     * 校验页面引用动作均存在且处于启用状态
     **
     * @param requestedCodes 页面请求发布的动作编码
     * @param actions 动作库返回的可发布动作
     * @throws ResponseStatusException 存在缺失或禁用动作
     * @author caipan by codex
     * @date 2026/9/6 22:10
     */
    private void validatePublishedActions(List<String> requestedCodes, List<PlmActionResponse> actions) {
        Set<String> resolvedCodes = new LinkedHashSet<>();
        actions.forEach(action -> resolvedCodes.add(action.actionCode()));
        List<String> unavailableCodes = requestedCodes.stream()
                .filter(code -> !resolvedCodes.contains(code))
                .toList();
        if (!unavailableCodes.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "页面引用的动作不存在或已禁用：" + String.join(", ", unavailableCodes));
        }
    }

    /**
     * 从TWXTicketService可能附带HTML前缀的响应中提取末尾JSON
     **
     * @param responseBody TicketService原始响应
     * @return JSON响应
     * @throws Exception 响应中不存在有效JSON
     * @author caipan by codex
     * @date 2026/9/5 16:30
     */
    JsonNode extractJsonPayload(String responseBody) throws Exception {
        if (responseBody == null || responseBody.isBlank()) {
            throw new IllegalArgumentException("PLM未返回内容");
        }
        String body = responseBody.trim();
        for (int start = body.indexOf('{'); start >= 0; start = body.indexOf('{', start + 1)) {
            int end = findJsonObjectEnd(body, start);
            if (end < 0) {
                continue;
            }
            try {
                return objectMapper.readTree(body.substring(start, end + 1));
            } catch (Exception ignored) {
                // 继续查找下一个对象起点，跳过script中的JavaScript对象。
            }
        }
        JsonNode mapResult = parseTicketServiceMap(body);
        if (mapResult != null) {
            return mapResult;
        }
        throw new IllegalArgumentException("PLM返回内容不是有效JSON");
    }

    /**
     * 兼容TWXTicketService将JPO Map返回值输出为Java Map文本的情况
     **
     * @param responseBody PLM响应正文
     * @return 转换后的标准发布结果，不符合格式时返回null
     * @author caipan by codex
     * @date 2026/9/5 22:45
     */
    private JsonNode parseTicketServiceMap(String responseBody) {
        int start = responseBody.indexOf('{');
        int end = responseBody.indexOf('}', start + 1);
        if (start < 0 || end < 0) {
            return null;
        }
        Map<String, String> values = new HashMap<>();
        for (String item : responseBody.substring(start + 1, end).split(",\\s*")) {
            int separator = item.indexOf('=');
            if (separator > 0) {
                values.put(item.substring(0, separator).trim(), item.substring(separator + 1).trim());
            }
        }
        if (!values.containsKey("pageCode") || !values.containsKey("contentBytes")
                || !values.containsKey("replaced")) {
            return null;
        }
        var result = objectMapper.createObjectNode();
        result.put("status", 0);
        result.put("msg", "发布成功");
        var data = result.putObject("data");
        values.forEach(data::put);
        return result;
    }

    /**
     * 定位字符串中一个完整JSON对象的结束位置
     **
     * @param text 原始响应
     * @param start 左花括号位置
     * @return 配对右花括号位置，不完整时返回-1
     * @author caipan by codex
     * @date 2026/9/5 22:00
     */
    private int findJsonObjectEnd(String text, int start) {
        int depth = 0;
        boolean inString = false;
        boolean escaped = false;
        for (int index = start; index < text.length(); index++) {
            char current = text.charAt(index);
            if (inString) {
                if (escaped) {
                    escaped = false;
                } else if (current == '\\') {
                    escaped = true;
                } else if (current == '"') {
                    inString = false;
                }
                continue;
            }
            if (current == '"') {
                inString = true;
            } else if (current == '{') {
                depth++;
            } else if (current == '}' && --depth == 0) {
                return index;
            }
        }
        return -1;
    }

    private void validateConfiguration() {
        if (isBlank(properties.baseUrl()) || isBlank(properties.securityContext())
                || isBlank(properties.loginTicket())) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "PLM发布配置不完整，请检查config/plm-integration.yml");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private String rootCauseMessage(Exception exception) {
        Throwable cause = exception;
        while (cause.getCause() != null) {
            cause = cause.getCause();
        }
        String message = cause.getMessage();
        return message == null || message.isBlank()
                ? cause.getClass().getSimpleName()
                : message;
    }

    /**
     * 生成不包含请求凭据的PLM响应诊断摘要
     **
     * @param responseBody PLM响应正文
     * @return 压缩换行并限制长度的响应摘要
     * @author caipan by codex
     * @date 2026/9/5 22:40
     */
    private String responsePreview(String responseBody) {
        if (responseBody == null) {
            return "<null>";
        }
        String preview = responseBody.replaceAll("[\\r\\n\\t]+", " ").trim();
        return preview.length() <= 2000 ? preview : preview.substring(0, 2000) + "...";
    }
}
