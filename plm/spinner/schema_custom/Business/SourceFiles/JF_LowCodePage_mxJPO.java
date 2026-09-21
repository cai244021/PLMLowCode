import com.google.gson.Gson;
import com.matrixone.apps.domain.util.FrameworkException;
import com.matrixone.apps.domain.util.MqlUtil;
import matrix.db.Context;
import matrix.db.JPO;
import matrix.db.Page;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class JF_LowCodePage_mxJPO {
    private static final int MAX_PAGE_BYTES = 2 * 1024 * 1024;
    private static final String PAGE_CODE_PATTERN = "[A-Z0-9_]{1,100}";
    private static final String ACTION_CODE_PATTERN = "[A-Z0-9_]{1,100}";
    private static final String JPO_MEMBER_PATTERN = "[A-Za-z0-9_]{1,200}";
    private static final String ACTION_REGISTRY_PAGE = "JF_LOWCODE_ACTION_REGISTRY";

    /**
     * 发布低代码配置包到同名ENOVIA Page对象
     **
     * @param context 当前PLM登录上下文
     * @param args 配置包参数
     * @return String 标准JSON发布结果
     * @throws Exception 参数非法或Page写入失败时抛出异常
     * @author caipan by codex
     * @date 2026/9/5 14:00
     */
    public String publishPage(Context context, String[] args) throws Exception {
        Map pagePackage = JPO.unpackArgs(args);
        String pageCode = String.valueOf(pagePackage.get("pageCode"));
        //20260917 update by caipan 普通页面发布不能覆盖独立动作注册表。
        if (ACTION_REGISTRY_PAGE.equals(pageCode)) {
            throw new IllegalArgumentException("保留页面编码不能用于业务页面");
        }
        if (!pageCode.matches(PAGE_CODE_PATTERN)) {
            throw new IllegalArgumentException("页面编码只能使用大写字母、数字、下划线，长度1至100");
        }
        if (!(pagePackage.get("schema") instanceof Map)) {
            throw new IllegalArgumentException("页面配置包缺少schema");
        }

        //TWXTicketService可能在入参Map中附加登录上下文，Page只保存低代码配置白名单字段。
        Map<String, Object> publishedPackage = new LinkedHashMap<>();
        publishedPackage.put("formatVersion", pagePackage.get("formatVersion"));
        publishedPackage.put("pageCode", pageCode);
        publishedPackage.put("pageName", pagePackage.get("pageName"));
        publishedPackage.put("version", pagePackage.get("version"));
        publishedPackage.put("schema", pagePackage.get("schema"));
        publishedPackage.put("plmConfig", pagePackage.get("plmConfig"));
        //20260906 update by caipan 只保存字段和动作资源快照，丢弃TicketService附加内容及未知资源。
        if (pagePackage.get("resources") instanceof Map) {
            Map sourceResources = (Map) pagePackage.get("resources");
            Map<String, Object> publishedResources = new LinkedHashMap<>();
            if (sourceResources.get("fields") instanceof List) {
                publishedResources.put("fields", sourceResources.get("fields"));
            }
            if (sourceResources.get("actions") instanceof List) {
                //20260917 update by caipan 发布包仅保留引用，客户端提交的执行地址和映射不落入页面。
                List<Map> references = new ArrayList<>();
                for (Object value : (List) sourceResources.get("actions")) {
                    if (!(value instanceof Map)) {
                        throw new IllegalArgumentException("页面动作引用必须是对象");
                    }
                    Map action = (Map) value;
                    String code = String.valueOf(action.get("actionCode"));
                    if (!code.matches(ACTION_CODE_PATTERN)) {
                        throw new IllegalArgumentException("页面动作编码非法");
                    }
                    Map reference = new LinkedHashMap();
                    reference.put("actionCode", code);
                    reference.put("actionName", action.get("actionName"));
                    references.add(reference);
                }
                publishedResources.put("actions", references);
            }
            publishedPackage.put("resources", publishedResources);
        }

        String contents = new Gson().toJson(publishedPackage);
        int contentBytes = contents.getBytes(StandardCharsets.UTF_8).length;
        if (contentBytes > MAX_PAGE_BYTES) {
            throw new IllegalArgumentException("页面配置包不能超过2MB");
        }

        boolean exists = pageExists(context, pageCode);
        File contentFile = File.createTempFile("jf-lowcode-page-", ".json");
        try {
            Files.write(contentFile.toPath(), contents.getBytes(StandardCharsets.UTF_8));
            if (exists) {
                MqlUtil.mqlCommand(context, "modify page $1 file $2 description $3",
                        pageCode, contentFile.getAbsolutePath(), "JF PLM Low-Code published page");
            } else {
                MqlUtil.mqlCommand(context, "add page $1 file $2 description $3",
                        pageCode, contentFile.getAbsolutePath(), "JF PLM Low-Code published page");
            }
        } finally {
            Files.deleteIfExists(contentFile.toPath());
        }

        Map<String, Object> data = new HashMap<>();
        data.put("pageCode", pageCode);
        data.put("version", pagePackage.get("version"));
        data.put("replaced", exists);
        data.put("contentBytes", contentBytes);
        Map<String, Object> result = new HashMap<>();
        result.put("status", 0);
        result.put("msg", "发布成功");
        result.put("data", data);
        return new Gson().toJson(result);
    }

    /**
     * 读取低代码Page对象中的配置包
     **
     * @param context 当前PLM登录上下文
     * @param args 参数中包含pageCode
     * @return Map 包含pageCode和JSON内容
     * @throws Exception Page不存在或读取失败时抛出异常
     * @author caipan by codex
     * @date 2026/9/5 14:00
     */
    public Map getPublishedPage(Context context, String[] args) throws Exception {
        Map params = JPO.unpackArgs(args);
        String pageCode = String.valueOf(params.get("pageCode"));
        if (!pageCode.matches(PAGE_CODE_PATTERN)) {
            throw new IllegalArgumentException("页面编码不合法");
        }
        Page page = new Page(pageCode);
        try {
            page.open(context);
            String contents = page.getContents(context);
            Map<String, Object> result = new HashMap<>();
            result.put("pageCode", pageCode);
            result.put("contents", contents);
            return result;
        } finally {
            try {
                page.close(context);
            } catch (Exception ignored) {
                //Page打开失败时无需再次覆盖原始异常。
            }
        }
    }

    /**
     * 为Widget和Space返回统一响应封装的已发布Page配置
     **
     * @param context 当前PLM登录上下文
     * @param args 参数中包含pageCode
     * @return String 包含status、msg、data的JSON响应
     * @throws Exception Page不存在或读取失败时抛出异常
     * @author caipan by codex
     * @date 2026/9/17 16:30
     */
    public String getPublishedPageContents(Context context, String[] args) throws Exception {
        //20260917 update by caipan 页面加载与动作执行使用同一响应结构。
        Map<String, Object> result = new LinkedHashMap<>();
        try {
            Map data = getPublishedPage(context, args);
            Map pagePackage = new Gson().fromJson(String.valueOf(data.get("contents")), Map.class);
            if (pagePackage == null || !(pagePackage.get("schema") instanceof Map)
                    || !(pagePackage.get("plmConfig") instanceof Map)) {
                throw new IllegalArgumentException("已发布页面配置包格式不正确");
            }
            result.put("status", 0);
            result.put("msg", "");
            result.put("data", pagePackage);
        } catch (Exception e) {
            result.put("status", 1);
            result.put("msg", e.getMessage() == null ? "已发布页面加载失败" : e.getMessage());
            result.put("data", new HashMap());
        }
        return new Gson().toJson(result);
    }

    /**
     * 从已发布Page中解析并校验当前页面允许执行的动作
     **
     * @param context 当前PLM登录上下文
     * @param args 参数中包含pageCode和actionCode
     * @return Map 已发布动作定义
     * @throws Exception Page或动作配置非法时抛出异常
     * @author caipan by codex
     * @date 2026/9/6 22:10
     */
    public Map getPublishedAction(Context context, String[] args) throws Exception {
        Map params = JPO.unpackArgs(args);
        String pageCode = String.valueOf(params.get("pageCode"));
        String actionCode = String.valueOf(params.get("actionCode"));
        if (!pageCode.matches(PAGE_CODE_PATTERN) || !actionCode.matches(ACTION_CODE_PATTERN)) {
            throw new IllegalArgumentException("页面编码或动作编码不合法");
        }

        Map<String, Object> pageParams = new HashMap<>();
        pageParams.put("pageCode", pageCode);
        Map pageData = getPublishedPage(context, JPO.packArgs(pageParams));
        Map pagePackage = new Gson().fromJson(String.valueOf(pageData.get("contents")), Map.class);
        Object resourcesValue = pagePackage.get("resources");
        if (!(resourcesValue instanceof Map)) {
            throw new IllegalArgumentException("当前页面未发布动作资源");
        }
        Object actionsValue = ((Map) resourcesValue).get("actions");
        if (!(actionsValue instanceof List)) {
            throw new IllegalArgumentException("当前页面未发布动作资源");
        }
        for (Object item : (List) actionsValue) {
            if (!(item instanceof Map)) {
                continue;
            }
            Map reference = (Map) item;
            if (!actionCode.equals(String.valueOf(reference.get("actionCode")))) {
                continue;
            }
            //20260917 update by caipan 页面仅声明动作引用，执行定义始终从服务端注册表读取。
            Map registryParams = new HashMap();
            registryParams.put("pageCode", ACTION_REGISTRY_PAGE);
            Map registryPage = getPublishedPage(context, JPO.packArgs(registryParams));
            Map registry = new Gson().fromJson(String.valueOf(registryPage.get("contents")), Map.class);
            if (!(registry.get("actions") instanceof Map)
                    || !(((Map) registry.get("actions")).get(actionCode) instanceof Map)) {
                throw new IllegalArgumentException("动作未注册，请在设计器重新发布：" + actionCode);
            }
            Map action = (Map) ((Map) registry.get("actions")).get(actionCode);
            if (!Boolean.TRUE.equals(action.get("enabled"))) {
                throw new IllegalArgumentException("动作已禁用");
            }
            Object jpoValue = action.get("jpoName");
            Object methodValue = action.get("methodName");
            if (!(jpoValue instanceof String) || !(methodValue instanceof String)
                    || !((String) jpoValue).matches(JPO_MEMBER_PATTERN)
                    || !((String) methodValue).matches(JPO_MEMBER_PATTERN)) {
                throw new IllegalArgumentException("动作JPO配置不合法");
            }
            if (!"POST".equals(String.valueOf(action.get("httpMethod")))) {
                throw new IllegalArgumentException("PLM动作只允许POST请求");
            }
            return new LinkedHashMap(action);
        }
        throw new IllegalArgumentException("当前页面未发布动作：" + actionCode);
    }

    /**
     * 解析页面动作并按动作库输入映射生成受控JPO调用参数
     **
     * @param context 当前PLM登录上下文
     * @param args 参数中包含pageCode、actionCode和页面请求参数
     * @return Map JPO名称、方法、动作信息和映射后参数
     * @throws Exception 动作不存在或映射非法时抛出异常
     * @author caipan by codex
     * @date 2026/9/6 22:10
     */
    public Map prepareActionInvocation(Context context, String[] args) throws Exception {
        Map request = JPO.unpackArgs(args);
        Map<String, Object> actionParams = new HashMap<>();
        actionParams.put("pageCode", request.get("pageCode"));
        actionParams.put("actionCode", request.get("actionCode"));
        Map action = getPublishedAction(context, JPO.packArgs(actionParams));
        Map sourceParams = request.get("params") instanceof Map
                ? (Map) request.get("params") : new HashMap();
        Map inputMapping = action.get("inputMapping") instanceof Map
                ? (Map) action.get("inputMapping") : new HashMap();
        Map<String, Object> invokeParams = new LinkedHashMap<>();
        if (inputMapping.isEmpty()) {
            invokeParams.putAll(sourceParams);
        } else {
            for (Object keyValue : inputMapping.keySet()) {
                String key = String.valueOf(keyValue);
                invokeParams.put(key, resolveMappedValue(inputMapping.get(keyValue), sourceParams));
            }
        }

        Map<String, Object> invocation = new LinkedHashMap<>();
        invocation.put("jpoName", action.get("jpoName"));
        invocation.put("methodName", action.get("methodName"));
        invocation.put("actionName", action.get("actionName"));
        invocation.put("actionKind", action.get("actionKind"));
        invocation.put("params", invokeParams);
        invocation.put("outputMapping", action.get("outputMapping"));
        return invocation;
    }

    /**
     * 按公共动作输出映射转换JPO原始返回
     **
     * @param context 当前PLM登录上下文
     * @param args 参数中包含outputMapping和JPO原始result
     * @return Object 空映射返回原结果，否则返回映射后的标准动作数据
     * @throws Exception 输出映射处理失败时抛出异常
     * @author caipan by codex
     * @date 2026/9/13 10:00
     */
    public Object mapActionOutput(Context context, String[] args) throws Exception {
        Map request = JPO.unpackArgs(args);
        Map outputMapping = request.get("outputMapping") instanceof Map
                ? (Map) request.get("outputMapping") : new HashMap();
        Object result = request.get("result");
        if (outputMapping.isEmpty()) {
            return result;
        }
        Map<String, Object> source = new HashMap<>();
        source.put("result", result);
        source.put("data", result);
        Object mappedResult = resolveMappedValue(normalizeLegacyOutputMapping(outputMapping), source);
        if (result instanceof Map && mappedResult instanceof Map) {
            Map<String, Object> merged = new LinkedHashMap<>((Map) result);
            merged.putAll((Map) mappedResult);
            return merged;
        }
        return mappedResult;
    }

    /**
     * 为Dashboard Widget执行已发布页面中绑定的通用动作
     **
     * @param context 当前PLM登录上下文
     * @param args 参数中包含pageCode、actionCode和params
     * @return String 标准JSON动作结果
     * @throws Exception 动作调用发生不可恢复错误时抛出异常
     * @author caipan by codex
     * @date 2026/9/17 16:30
     */
    public String executePublishedAction(Context context, String[] args) throws Exception {
        Map<String, Object> result = new LinkedHashMap<>();
        //20260917 update by caipan 在任何业务JPO调用之前校验动作协议主版本。
        result.put("protocolVersion", 1);
        try {
            Object version = ((Map) JPO.unpackArgs(args)).get("protocolVersion");
            if (!(version instanceof Number) || ((Number) version).doubleValue() != 1.0) {
                throw new IllegalArgumentException("Action协议不兼容：需要版本1，请配套更新Widget或Space入口");
            }
            Map invocation = prepareActionInvocation(context, args);
            String jpoName = String.valueOf(invocation.get("jpoName"));
            String methodName = String.valueOf(invocation.get("methodName"));
            Map mappedParams = invocation.get("params") instanceof Map
                    ? (Map) invocation.get("params") : new HashMap();
            Object data = JPO.invoke(context, jpoName, null, methodName,
                    JPO.packArgs(mappedParams), Object.class);
            Map<String, Object> outputParams = new HashMap<>();
            outputParams.put("outputMapping", invocation.get("outputMapping"));
            outputParams.put("result", data);
            data = mapActionOutput(context, JPO.packArgs(outputParams));
            String actionKind = String.valueOf(invocation.get("actionKind"));
            String actionName = String.valueOf(invocation.get("actionName"));
            result.put("status", 0);
            result.put("msg", "QUERY".equals(actionKind) ? "" : actionName + "成功");
            result.put("data", data == null ? new HashMap() : data);
        } catch (Exception e) {
            result.put("status", 1);
            //20260921 update by caipan 统一提取Matrix和Trigger业务提示，避免向页面暴露错误码及Java包装信息
            result.put("msg", getActionErrorMessage(e));
            result.put("data", new HashMap());
        }
        return new Gson().toJson(result);
    }

    /**
     * 提取低代码动作异常中的业务提示
     **
     * @param exception Matrix、JPO或Trigger抛出的异常
     * @return String 可直接展示给当前页面用户的错误提示
     * @author caipan by codex
     * @date 2026/9/21 10:30
     */
    private String getActionErrorMessage(Exception exception) {
        String message = exception == null ? "" : exception.getMessage();
        Throwable cause = exception == null ? null : exception.getCause();
        while ((message == null || message.trim().length() == 0) && cause != null) {
            message = cause.getMessage();
            cause = cause.getCause();
        }
        if (message == null || message.trim().length() == 0) {
            return "执行失败";
        }
        message = message.replace('\r', ' ').replace('\n', ' ').replaceAll("\\s+", " ").trim();
        int messageIndex = message.lastIndexOf("Message:");
        if (messageIndex >= 0) {
            message = message.substring(messageIndex + "Message:".length()).trim();
        }
        int endIndex = message.length();
        int severityIndex = message.indexOf("Severity:");
        int errorCodeIndex = message.indexOf("ErrorCode:");
        if (severityIndex >= 0) {
            endIndex = Math.min(endIndex, severityIndex);
        }
        if (errorCodeIndex >= 0) {
            endIndex = Math.min(endIndex, errorCodeIndex);
        }
        message = message.substring(0, endIndex).trim()
                .replaceFirst("^System Error:\\s*#\\d+:\\s*", "")
                .replaceFirst("^java\\.[A-Za-z0-9_.$]+:\\s*", "")
                .trim();
        if ("Check trigger blocked event".equalsIgnoreCase(message)) {
            return "操作被业务Trigger校验阻止，请检查对象状态或关联数据";
        }
        return message.length() == 0 ? "执行失败" : message;
    }

    /**
     * 兼容历史动作库中使用data.items格式保存的输出路径
     **
     * @param template 历史或V2输出映射
     * @return 将历史路径转换为V2表达式后的映射
     * @author caipan by codex
     * @date 2026/9/13 12:40
     */
    private Object normalizeLegacyOutputMapping(Object template) {
        if (template instanceof String) {
            String text = (String) template;
            if (!text.contains("${") && (text.startsWith("data.") || text.startsWith("result."))) {
                return "${" + text + "}";
            }
            return text;
        }
        if (template instanceof Map) {
            Map<String, Object> normalized = new LinkedHashMap<>();
            Map sourceMap = (Map) template;
            for (Object keyValue : sourceMap.keySet()) {
                normalized.put(String.valueOf(keyValue),
                        normalizeLegacyOutputMapping(sourceMap.get(keyValue)));
            }
            return normalized;
        }
        if (template instanceof List) {
            List<Object> normalized = new ArrayList<>();
            for (Object item : (List) template) {
                normalized.add(normalizeLegacyOutputMapping(item));
            }
            return normalized;
        }
        return template;
    }

    /**
     * 递归解析动作输入映射中的${path}变量
     **
     * @param template 映射模板值
     * @param source 页面请求参数
     * @return 解析后的参数值
     * @author caipan by codex
     * @date 2026/9/6 22:10
     */
    private Object resolveMappedValue(Object template, Map source) {
        if (template instanceof String) {
            String text = (String) template;
            java.util.regex.Matcher exactMatcher = java.util.regex.Pattern
                    .compile("^" + java.util.regex.Pattern.quote("${")
                            + "([^{}]+)" + java.util.regex.Pattern.quote("}") + "$").matcher(text);
            if (exactMatcher.matches()) {
                return readPath(source, exactMatcher.group(1));
            }
            java.util.regex.Matcher matcher = java.util.regex.Pattern
                    .compile(java.util.regex.Pattern.quote("${")
                            + "([^{}]+)" + java.util.regex.Pattern.quote("}")).matcher(text);
            StringBuffer mappedText = new StringBuffer();
            while (matcher.find()) {
                Object value = readPath(source, matcher.group(1));
                matcher.appendReplacement(mappedText, java.util.regex.Matcher.quoteReplacement(
                        value == null ? "" : String.valueOf(value)));
            }
            matcher.appendTail(mappedText);
            return mappedText.toString();
        }
        if (template instanceof Map) {
            Map<String, Object> mapped = new LinkedHashMap<>();
            Map sourceMap = (Map) template;
            for (Object keyValue : sourceMap.keySet()) {
                mapped.put(String.valueOf(keyValue), resolveMappedValue(sourceMap.get(keyValue), source));
            }
            return mapped;
        }
        if (template instanceof List) {
            List<Object> mapped = new ArrayList<>();
            for (Object item : (List) template) {
                mapped.add(resolveMappedValue(item, source));
            }
            return mapped;
        }
        return template;
    }

    /**
     * 按点分路径读取页面请求参数
     **
     * @param source 页面请求参数
     * @param path 参数路径
     * @return 路径对应值，不存在时返回null
     * @author caipan by codex
     * @date 2026/9/6 22:10
     */
    private Object readPath(Map source, String path) {
        Object value = source;
        String normalizedPath = path.replaceAll("\\[(\\d+)\\]", ".$1");
        for (String segment : normalizedPath.split("\\.")) {
            if ("__proto__".equals(segment) || "prototype".equals(segment)
                    || "constructor".equals(segment)) {
                return null;
            }
            if (value instanceof Map) {
                value = ((Map) value).get(segment);
            } else if (value instanceof List && segment.matches("\\d+")) {
                int index = Integer.parseInt(segment);
                List values = (List) value;
                value = index < values.size() ? values.get(index) : null;
            } else {
                return null;
            }
        }
        return value;
    }

    /**
     * 判断同名Page是否已经存在
     **
     * @param context 当前PLM登录上下文
     * @param pageCode 页面编码
     * @return boolean 存在返回true
     * @throws Exception Page查询失败时抛出异常
     * @author caipan by codex
     * @date 2026/9/5 14:00
     */
    private boolean pageExists(Context context, String pageCode) throws Exception {
        try {
            String existingName = MqlUtil.mqlCommand(context,
                    "print page $1 select name dump", pageCode);
            return pageCode.equals(existingName == null ? "" : existingName.trim());
        } catch (FrameworkException exception) {
            return false;
        }
    }

    /**
     * 发布完整公共动作库到独立注册表，使用当前用户原生Page管理权限
     **
     * @param context 当前PLM登录上下文，不提升权限
     * @param args 包含actions完整动作列表，含禁用动作
     * @return String 标准JSON发布结果
     * @throws Exception 定义非法或当前用户无Page管理权限
     * @author caipan by codex
     * @date 2026/9/17 17:30
     */
    public String publishActionRegistry(Context context, String[] args) throws Exception {
        Map request = JPO.unpackArgs(args);
        if (!(request.get("actions") instanceof List)) {
            throw new IllegalArgumentException("注册表必须包含完整actions列表");
        }
        Map<String, Object> actions = new LinkedHashMap<>();
        for (Object value : (List) request.get("actions")) {
            if (!(value instanceof Map)) {
                throw new IllegalArgumentException("动作定义必须是对象");
            }
            Map action = (Map) value;
            String code = String.valueOf(action.get("actionCode"));
            if (!code.matches(ACTION_CODE_PATTERN) || actions.containsKey(code)
                    || !(action.get("jpoName") instanceof String)
                    || !(action.get("methodName") instanceof String)
                    || !String.valueOf(action.get("jpoName")).matches(JPO_MEMBER_PATTERN)
                    || !String.valueOf(action.get("methodName")).matches(JPO_MEMBER_PATTERN)
                    || !(action.get("enabled") instanceof Boolean)
                    || !"POST".equals(action.get("httpMethod"))
                    || !(action.get("inputMapping") instanceof Map)
                    || !(action.get("outputMapping") instanceof Map)) {
                throw new IllegalArgumentException("动作定义非法或编码重复：" + code);
            }
            Map<String, Object> definition = new LinkedHashMap<>();
            for (String key : new String[]{"actionCode", "actionName", "actionKind", "jpoName",
                    "methodName", "httpMethod", "inputMapping", "outputMapping", "enabled", "updatedAt"}) {
                definition.put(key, action.get(key));
            }
            actions.put(code, definition);
        }
        Map<String, Object> registry = new LinkedHashMap<>();
        registry.put("formatVersion", 1);
        registry.put("actions", actions);
        byte[] contents = new Gson().toJson(registry).getBytes(StandardCharsets.UTF_8);
        if (contents.length > MAX_PAGE_BYTES) {
            throw new IllegalArgumentException("动作注册表不能超过2MB");
        }
        File file = File.createTempFile("jf-lowcode-registry-", ".json");
        try {
            Files.write(file.toPath(), contents);
            boolean exists = pageExists(context, ACTION_REGISTRY_PAGE);
            MqlUtil.mqlCommand(context, exists
                    ? "modify page $1 file $2 description $3"
                    : "add page $1 file $2 description $3",
                    ACTION_REGISTRY_PAGE, file.getAbsolutePath(), "JF Low-Code action registry");
        } finally {
            Files.deleteIfExists(file.toPath());
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("status", 0);
        result.put("msg", "动作注册表发布成功");
        result.put("data", actions.size());
        return new Gson().toJson(result);
    }

}
