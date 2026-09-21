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
        validatePageBindings(page.schema(), page.plmConfig());

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
        validateEventActionParameters(page.plmConfig(), actions);
        //20260917 update by caipan 页面只携带动作引用，接口和映射从独立服务端注册表解析。
        resources.put("actions", actions.stream().map(action -> Map.of(
                "actionCode", action.actionCode(), "actionName", action.actionName())).toList());
        pagePackage.put("resources", resources);

        String baseUrl = properties.baseUrl().replaceAll("/+$", "");
        try {
            //20260917 update by caipan 先同步公共动作库，注册表失败时禁止继续发布页面。
            String registryBody = restClient.post()
                    .uri(baseUrl + "/TWXPublicRest/TWXTicketService"
                            + "?JPOName=JF_LowCodePage&FuncName=publishActionRegistry")
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .header("SecurityContext", properties.securityContext())
                    .header("X-3DSLogin-ticket", properties.loginTicket())
                    .body(Map.of("actions", plmActionService.findAll()))
                    .retrieve().body(String.class);
            JsonNode registryResult = extractJsonPayload(registryBody);
            if (registryResult.path("status").asInt(1) != 0) {
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                        "动作注册表发布失败：" + registryResult.path("msg").asText("未知错误"));
            }
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
        JsonNode eventBindings = plmConfig == null ? null : plmConfig.path("eventBindings");
        if (eventBindings != null && eventBindings.isArray()) {
            eventBindings.forEach(binding -> {
                addActionCode(actionCodes, binding.path("action").path("actionCode"));
                collectChainedActionCodes(actionCodes, binding.path("success"));
                collectChainedActionCodes(actionCodes, binding.path("failure"));
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

    private void collectChainedActionCodes(Set<String> actionCodes, JsonNode effects) {
        if (!effects.isArray()) {
            return;
        }
        effects.forEach(effect -> {
            if ("CHAIN_ACTION".equals(effect.path("type").asText())) {
                addActionCode(actionCodes, effect.path("action").path("actionCode"));
            }
        });
    }

    private void addActionCode(Set<String> actionCodes, JsonNode source) {
        String code = source.asText("").trim();
        if (!code.isEmpty()) {
            actionCodes.add(code);
        }
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
     * 发布前按公共动作契约校验事件输入参数
     **
     * @param plmConfig 页面PLM绑定配置
     * @param actions 当前页面引用的公共动作
     * @throws ResponseStatusException 事件缺少必填参数、包含未知参数或静态值类型错误
     * @author caipan by codex
     * @date 2026/9/21 14:35
     */
    void validateEventActionParameters(JsonNode plmConfig, List<PlmActionResponse> actions) {
        JsonNode bindings = plmConfig == null ? null : plmConfig.path("eventBindings");
        if (bindings == null || !bindings.isArray()) {
            return;
        }
        Map<String, PlmActionResponse> actionByCode = new HashMap<>();
        actions.forEach(action -> actionByCode.put(action.actionCode(), action));
        for (JsonNode binding : bindings) {
            String bindingId = binding.path("id").asText("");
            validateActionInvocation(binding.path("action"), actionByCode, "事件 " + bindingId);
            for (String branch : List.of("success", "failure")) {
                for (JsonNode effect : binding.path(branch)) {
                    if ("CHAIN_ACTION".equals(effect.path("type").asText())) {
                        validateActionInvocation(effect.path("action"), actionByCode,
                                "事件 " + bindingId + " 的链式动作");
                    }
                }
            }
        }
    }

    /**
     * 校验一次页面动作调用满足注册表参数契约
     **
     * @param invocation 页面动作调用配置
     * @param actionByCode 已启用动作索引
     * @param location 错误提示中的配置位置
     * @throws ResponseStatusException 参数契约校验失败
     * @author caipan by codex
     * @date 2026/9/21 14:35
     */
    private void validateActionInvocation(JsonNode invocation,
                                          Map<String, PlmActionResponse> actionByCode,
                                          String location) {
        String actionCode = invocation.path("actionCode").asText("");
        PlmActionResponse action = actionByCode.get(actionCode);
        if (action == null || action.inputParameters() == null || !action.inputParameters().isArray()) {
            return;
        }
        JsonNode mapping = invocation.path("inputMapping");
        Map<String, JsonNode> definitions = new LinkedHashMap<>();
        for (JsonNode definition : action.inputParameters()) {
            definitions.put(definition.path("name").asText(), definition);
        }
        for (JsonNode definition : action.inputParameters()) {
            String name = definition.path("name").asText();
            if (definition.path("required").asBoolean(false) && !mapping.has(name)) {
                throw badBinding(location + " 缺少动作 " + actionCode + " 的必填参数：" + name);
            }
        }
        for (String name : mapping.propertyNames()) {
            JsonNode definition = definitions.get(name);
            if (definition == null) {
                throw badBinding(location + " 包含动作 " + actionCode + " 未定义的参数：" + name);
            }
            JsonNode value = mapping.path(name);
            if (value.isTextual() && value.asText().contains("${")) {
                continue;
            }
            String dataType = definition.path("dataType").asText("ANY");
            boolean validType = "ANY".equals(dataType)
                    || ("STRING".equals(dataType) && value.isTextual())
                    || ("NUMBER".equals(dataType) && value.isNumber())
                    || ("BOOLEAN".equals(dataType) && value.isBoolean())
                    || ("OBJECT".equals(dataType) && value.isObject())
                    || ("ARRAY".equals(dataType) && value.isArray());
            if (!validType) {
                throw badBinding(location + " 的参数 " + name + " 不符合类型 " + dataType);
            }
        }
    }

    /**
     * 发布前校验V2事件引用的组件、动作和效果配置
     **
     * @param schema 页面AMIS结构
     * @param plmConfig 页面PLM绑定配置
     * @throws ResponseStatusException 事件配置无法由Runtime解释
     * @author caipan by codex
     * @date 2026/9/13 12:10
     */
    void validatePageBindings(JsonNode schema, JsonNode plmConfig) {
        JsonNode bindings = plmConfig == null ? null : plmConfig.path("eventBindings");
        if (bindings == null || !bindings.isArray()) {
            return;
        }
        Map<String, String> componentTypes = new LinkedHashMap<>();
        collectComponentTypes(schema, componentTypes);
        Set<String> componentIds = componentTypes.keySet();
        Set<String> bindingIds = new LinkedHashSet<>();
        Set<String> supportedEvents = Set.of("init", "click", "submit", "change",
                "rowClick", "selectionChange", "drop");
        Set<String> effectsRequiringTarget = Set.of("SET_DATA", "REFRESH_ROW",
                "APPEND_ROWS", "REMOVE_ROWS", "RESET");
        for (JsonNode binding : bindings) {
            String bindingId = binding.path("id").asText("").trim();
            if (bindingId.isEmpty() || !bindingIds.add(bindingId)) {
                throw badBinding("事件标识不能为空且不能重复：" + bindingId);
            }
            String componentId = binding.path("source").path("componentId").asText("").trim();
            String event = binding.path("source").path("event").asText("").trim();
            if (!componentIds.contains(componentId)) {
                throw badBinding("事件 " + bindingId + " 引用的组件不存在：" + componentId);
            }
            if (!supportedEvents.contains(event)) {
                throw badBinding("事件 " + bindingId + " 的触发类型不支持：" + event);
            }
            String componentType = componentTypes.get(componentId);
            boolean compatible = ("init".equals(event) && "service".equals(componentType))
                    || ("click".equals(event) && "button".equals(componentType))
                    || ("submit".equals(event) && "form".equals(componentType))
                    || ("change".equals(event) && Set.of("input-text", "textarea", "input-number",
                            "select", "radios", "checkboxes", "checkbox", "switch", "input-date",
                            "input-datetime", "input-file", "input-image", "input-tree", "input-tag",
                            "input-table", "hidden").contains(componentType))
                    || (Set.of("rowClick", "selectionChange", "drop").contains(event)
                            && Set.of("crud", "table", "table2").contains(componentType));
            if (!compatible) {
                throw badBinding("事件 " + bindingId + " 的组件类型 " + componentType
                        + " 不支持触发 " + event);
            }
            //20260917 update by caipan V2拖拽保留对象类型白名单，避免迁移旧配置后扩大可投放范围
            JsonNode acceptedTypes = binding.path("source").path("acceptedTypes");
            if ("drop".equals(event) && !acceptedTypes.isMissingNode()
                    && (!acceptedTypes.isArray() || !allTextValues(acceptedTypes))) {
                throw badBinding("事件 " + bindingId + " 的允许拖入类型必须是字符串数组");
            }
            validateActionReference(binding.path("action"), "事件 " + bindingId);
            validateEffects(binding.path("success"), bindingId, componentIds, effectsRequiringTarget);
            validateEffects(binding.path("failure"), bindingId, componentIds, effectsRequiringTarget);
        }
    }

    /**
     * 校验JSON数组中每个元素都是非空字符串
     **
     * @param values 待校验数组
     * @return boolean 全部合法时返回true
     * @author caipan by codex
     * @date 2026/9/17 14:30
     */
    private boolean allTextValues(JsonNode values) {
        for (JsonNode value : values) {
            if (!value.isTextual() || value.asText().isBlank()) {
                return false;
            }
        }
        return true;
    }

    private void validateEffects(JsonNode effects, String bindingId, Set<String> componentIds,
                                 Set<String> effectsRequiringTarget) {
        if (!effects.isArray()) {
            throw badBinding("事件 " + bindingId + " 的成功和失败效果必须是数组");
        }
        Set<String> supportedEffects = Set.of("SET_DATA", "RELOAD", "REFRESH_ROW", "APPEND_ROWS",
                "REMOVE_ROWS", "RESET", "OPEN_DIALOG", "OPEN_DRAWER", "CLOSE", "OPEN_DETAIL",
                "NAVIGATE", "NOTIFY", "CHAIN_ACTION");
        for (JsonNode effect : effects) {
            String type = effect.path("type").asText("").trim();
            String target = effect.path("target").asText("").trim();
            if (!supportedEffects.contains(type)) {
                throw badBinding("事件 " + bindingId + " 包含不支持的效果：" + type);
            }
            if (effectsRequiringTarget.contains(type) && !componentIds.contains(target)) {
                throw badBinding("事件 " + bindingId + " 的效果 " + type
                        + " 引用的组件不存在：" + target);
            }
            if ("CHAIN_ACTION".equals(type)) {
                validateActionReference(effect.path("action"), "事件 " + bindingId + " 的链式动作");
            }
        }
    }

    private void validateActionReference(JsonNode action, String location) {
        String actionCode = action.path("actionCode").asText("").trim();
        if (!actionCode.matches("[A-Z0-9_]{1,100}")) {
            throw badBinding(location + " 缺少合法的公共动作编码");
        }
        if (!action.path("inputMapping").isObject()) {
            throw badBinding(location + " 的动作输入映射必须是JSON对象");
        }
        //20260921 update by caipan 阻止空参数名进入发布包，避免运行时静默覆盖或丢失参数
        for (String field : action.path("inputMapping").propertyNames()) {
            if (field.isBlank()) {
                throw badBinding(location + " 的动作输入映射包含空参数名");
            }
        }
    }

    private void collectComponentTypes(JsonNode node, Map<String, String> componentTypes) {
        if (node == null) {
            return;
        }
        if (node.isObject()) {
            String id = node.path("id").asText("").trim();
            if (!id.isEmpty()) {
                componentTypes.put(id, node.path("type").asText("").trim());
            }
        }
        if (node.isObject() || node.isArray()) {
            node.forEach(child -> collectComponentTypes(child, componentTypes));
        }
    }

    private ResponseStatusException badBinding(String message) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, "页面事件配置错误：" + message);
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
