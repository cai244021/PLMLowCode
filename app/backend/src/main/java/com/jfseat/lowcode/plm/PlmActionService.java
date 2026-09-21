package com.jfseat.lowcode.plm;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.node.JsonNodeFactory;

import java.util.List;
import java.util.LinkedHashSet;
import java.util.Set;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import tools.jackson.databind.JsonNode;

@Service
public class PlmActionService {

    private final PlmActionRepository repository;

    public PlmActionService(PlmActionRepository repository) {
        this.repository = repository;
    }

    /**
     * 查询全部PLM动作定义
     **
     * @return 动作定义列表
     * @author caipan by codex
     * @date 2026/9/3 11:10
     */
    @Transactional(readOnly = true)
    public List<PlmActionResponse> findAll() {
        return repository.findAllByOrderByActionCodeAsc().stream()
                .map(PlmActionResponse::from)
                .toList();
    }

    /**
     * 按编码查询可发布的已启用PLM动作
     **
     * @param actionCodes 页面引用的动作编码
     * @return 按动作库顺序返回的已启用动作
     * @author caipan by codex
     * @date 2026/9/6 22:10
     */
    @Transactional(readOnly = true)
    public List<PlmActionResponse> findEnabledByCodes(List<String> actionCodes) {
        Set<String> requestedCodes = Set.copyOf(actionCodes);
        return repository.findAllByOrderByActionCodeAsc().stream()
                .filter(action -> action.isEnabled() && requestedCodes.contains(action.getActionCode()))
                .map(PlmActionResponse::from)
                .toList();
    }

    /**
     * 新增或更新PLM动作定义
     **
     * @param actionCode 动作编码
     * @param request 动作配置
     * @return 保存后的动作定义
     * @author caipan by codex
     * @date 2026/9/3 11:10
     */
    @Transactional
    public PlmActionResponse save(String actionCode, PlmActionRequest request) {
        var inputMapping = request.inputMapping() == null
                ? JsonNodeFactory.instance.objectNode()
                : request.inputMapping();
        var outputMapping = request.outputMapping() == null
                ? JsonNodeFactory.instance.objectNode()
                : request.outputMapping();
        var inputParameters = buildParameterDefinitions(request.inputParameters(), inputMapping);
        var outputParameters = buildParameterDefinitions(request.outputParameters(), outputMapping);
        validateActionContract(request, inputMapping, outputMapping, inputParameters, outputParameters);
        var action = repository.findByActionCode(actionCode)
                .orElseGet(() -> new PlmActionDefinition(
                        actionCode, request, inputMapping, outputMapping, inputParameters, outputParameters
                ));
        action.update(request, inputMapping, outputMapping, inputParameters, outputParameters);
        return PlmActionResponse.from(repository.save(action));
    }

    /**
     * 校验公共动作可被PLM运行端安全、稳定地解释
     **
     * @param request 动作基础配置
     * @param inputMapping 公共输入到JPO参数的映射
     * @param outputMapping JPO结果到公共输出的映射
     * @param inputParameters 输入参数契约
     * @param outputParameters 输出参数契约
     * @throws ResponseStatusException 动作契约不完整或映射非法
     * @author caipan by codex
     * @date 2026/9/13 11:30
     */
    private void validateActionContract(PlmActionRequest request, JsonNode inputMapping,
                                        JsonNode outputMapping, JsonNode inputParameters,
                                        JsonNode outputParameters) {
        //20260917 update by caipan 公共动作必须是PLM端可真实执行的JPO动作，页面跳转由事件Effect承载
        if (!Set.of("CREATE", "UPDATE", "QUERY", "ACTION").contains(request.actionKind())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "不支持的PLM动作类型");
        }
        if (!inputMapping.isObject() || !outputMapping.isObject()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "动作输入和输出映射必须是JSON对象");
        }
        if (!isMemberName(request.jpoName()) || !isMemberName(request.methodName())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "JPO名称和执行方法只允许字母、数字、下划线、美元符号、点或短横线");
        }
        validateMappingExpressions(inputMapping);
        validateMappingExpressions(outputMapping);
        validateParameterDefinitions(inputParameters, inputMapping, "输入");
        validateParameterDefinitions(outputParameters, outputMapping, "输出");
    }

    /**
     * 兼容旧动作并生成参数契约
     **
     * @param definitions 请求中的参数契约
     * @param mapping 动作映射
     * @return JsonNode 规范后的参数契约数组
     * @author caipan by codex
     * @date 2026/9/21 14:10
     */
    private JsonNode buildParameterDefinitions(JsonNode definitions, JsonNode mapping) {
        if (definitions != null) {
            return definitions;
        }
        var result = JsonNodeFactory.instance.arrayNode();
        if (mapping.isObject()) {
            for (String name : mapping.propertyNames()) {
                var parameter = result.addObject();
                parameter.put("name", name);
                parameter.put("dataType", "ANY");
                parameter.put("required", false);
                parameter.put("description", "");
            }
        }
        return result;
    }

    /**
     * 校验动作参数契约与映射保持一一对应
     **
     * @param definitions 参数契约
     * @param mapping 动作映射
     * @param label 错误提示中的输入或输出标识
     * @throws ResponseStatusException 参数名称、类型或映射不一致
     * @author caipan by codex
     * @date 2026/9/21 14:10
     */
    private void validateParameterDefinitions(JsonNode definitions, JsonNode mapping, String label) {
        if (!definitions.isArray()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, label + "参数定义必须是JSON数组");
        }
        Set<String> names = new LinkedHashSet<>();
        Set<String> supportedTypes = Set.of("ANY", "STRING", "NUMBER", "BOOLEAN", "OBJECT", "ARRAY");
        for (JsonNode definition : definitions) {
            String name = definition.path("name").asText("").trim();
            String dataType = definition.path("dataType").asText("").trim();
            String description = definition.path("description").asText("");
            if (!definition.isObject() || !name.matches("[A-Za-z_][A-Za-z0-9_.-]{0,99}")) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        label + "参数名称只允许字母开头及字母、数字、下划线、点或短横线");
            }
            if (!names.add(name)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, label + "参数名称不能重复：" + name);
            }
            if (!supportedTypes.contains(dataType)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, label + "参数类型不支持：" + dataType);
            }
            if (!definition.path("required").isBoolean() || description.length() > 500) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        label + "参数的必填标记或说明不合法：" + name);
            }
            if (!mapping.has(name)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        label + "参数缺少对应映射：" + name);
            }
        }
        for (String mappingName : mapping.propertyNames()) {
            if (!names.contains(mappingName)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        label + "映射缺少参数定义：" + mappingName);
            }
        }
    }

    private boolean isMemberName(String value) {
        return value != null && value.matches("[A-Za-z0-9_$.-]{1,150}");
    }

    private void validateMappingExpressions(JsonNode node) {
        if (node.isObject() || node.isArray()) {
            node.forEach(this::validateMappingExpressions);
            return;
        }
        if (!node.isTextual()) {
            return;
        }
        String text = node.asText();
        var matcher = java.util.regex.Pattern.compile("\\$\\{([^{}]+)}").matcher(text);
        while (matcher.find()) {
            String path = matcher.group(1).trim();
            if (path.isEmpty() || path.contains("__proto__") || path.contains("prototype")
                    || path.contains("constructor")) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "动作映射包含不安全或空的数据路径");
            }
        }
    }

    /**
     * 删除PLM动作定义
     **
     * @param actionCode 动作编码
     * @author caipan by codex
     * @date 2026/9/3 11:10
     */
    @Transactional
    public void delete(String actionCode) {
        repository.findByActionCode(actionCode).ifPresent(repository::delete);
    }
}
