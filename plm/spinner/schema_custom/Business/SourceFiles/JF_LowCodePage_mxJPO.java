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
                publishedResources.put("actions", sourceResources.get("actions"));
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
            Map action = (Map) item;
            if (!actionCode.equals(String.valueOf(action.get("actionCode")))) {
                continue;
            }
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
        return invocation;
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
            if (text.startsWith("${") && text.endsWith("}") && text.length() > 3) {
                return readPath(source, text.substring(2, text.length() - 1));
            }
            return text;
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
        for (String segment : path.split("\\.")) {
            if (!(value instanceof Map)) {
                return null;
            }
            value = ((Map) value).get(segment);
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

}
