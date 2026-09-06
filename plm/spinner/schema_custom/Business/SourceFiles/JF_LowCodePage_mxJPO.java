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
import java.util.LinkedHashMap;
import java.util.Map;

public class JF_LowCodePage_mxJPO {
    private static final int MAX_PAGE_BYTES = 2 * 1024 * 1024;
    private static final String PAGE_CODE_PATTERN = "[A-Z0-9_]{1,100}";

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
