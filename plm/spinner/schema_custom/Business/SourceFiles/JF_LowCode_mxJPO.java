import com.alibaba.fastjson.JSONObject;
import com.dscn.plm.util.NioJDUtils;
import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.DomainRelationship;
import com.matrixone.apps.domain.util.*;
import com.matrixone.apps.framework.ui.UICache;
import com.matrixone.apps.framework.ui.UIUtil;
import matrix.db.Context;
import matrix.db.JPO;
import matrix.db.Policy;
import matrix.db.StateRequirement;
import matrix.db.StateRequirementList;
import matrix.util.StringList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
/*
 * @description:和其他系统集成的JPO BI SRM ESB等
 * @author: caipan
 * @date: 2025/7/25 09:44:06
 * @param: * @param[1] null
 * @return:
 **/
public class JF_LowCode_mxJPO extends DomainObject {
    private static final Logger JF_LOGGER = LoggerFactory.getLogger(JF_LowCode_mxJPO.class);

    public static void main(String[] args) {
        System.out.println("JF_LowCode_mxJPO");
    }

    /**
     * 查询当前登录用户拥有的DA列表，供PLM低代码页面加载
     **
     * @param context 当前PLM登录上下文
     * @param args 查询参数，支持page、perPage和clientSide
     * @return Map 包含items和total
     * @throws Exception DA查询失败时抛出异常
     * @author caipan by codex
     * @date 2026/9/5 14:00
     */
    public Map getCurrentUserDAListLowCode(Context context, String[] args) throws Exception {
        Map params = JPO.unpackArgs(args);
        int page = params.get("page") instanceof Number
                ? Math.max(1, ((Number) params.get("page")).intValue()) : 1;
        int perPage = params.get("perPage") instanceof Number
                ? Math.max(1, Math.min(200, ((Number) params.get("perPage")).intValue())) : 20;
        //20260906 update by caipan 支持一次加载全部数据后由AMIS在浏览器内分页、筛选和排序
        boolean clientSide = Boolean.TRUE.equals(params.get("clientSide"))
                || "true".equalsIgnoreCase(String.valueOf(params.get("clientSide")));

        String projectNameSelect = "attribute[JFProjectName]";
        String titleSelect = "attribute[Title]";
        String changeTypeSelect = "attribute[JFChangeType]";
        String projectPhaseSelect = "attribute[JFProjectPhase]";
        String affectedPlantSelect = "attribute[JFAffectsFactory]";
        String deviationReasonSelect = "attribute[JFReasonDeviation]";
        String beforeChangeSelect = "attribute[JFBeforeChange]";
        String afterChangeSelect = "attribute[JFAfterChange]";
        String startTimeSelect = "attribute[JFDAStartTime]";
        String closeTimeSelect = "attribute[JFDACloseTime]";
        String extendedCloseTimeSelect = "attribute[JFDAExtensionTime]";
        StringList selects = new StringList();
        selects.addAll(Arrays.asList(DomainConstants.SELECT_ID, DomainConstants.SELECT_NAME,
                DomainConstants.SELECT_OWNER, DomainConstants.SELECT_CURRENT, DomainConstants.SELECT_POLICY,
                DomainConstants.SELECT_ORIGINATED, projectNameSelect, titleSelect, changeTypeSelect,
                projectPhaseSelect, affectedPlantSelect, deviationReasonSelect, beforeChangeSelect,
                afterChangeSelect, startTimeSelect, closeTimeSelect, extendedCloseTimeSelect));

        String ownerWhere = JF_PublicMethodClass_mxJPO.buildStringInStrings(
                "owner=='", context.getUser(), "'");
        MapList source = DomainObject.findObjects(context, "JFDA",
                JF_PLMConstants_mxJPO.STRING_SYMB_ASTERISK, ownerWhere, selects);
        int total = source.size();
        int fromIndex = clientSide ? 0 : (int) Math.min((long) (page - 1) * perPage, total);
        int toIndex = clientSide ? total : Math.min(fromIndex + perPage, total);
        //20260906 update by caipan 直接返回ENOVIA标准MapList及原始select key，不再逐行转换字段别名
        MapList items = new MapList();
        for (int index = fromIndex; index < toIndex; index++) {
            items.add(source.get(index));
        }

        Map<String, Object> result = new HashMap<>();
        result.put("items", items);
        result.put("total", total);
        return result;
    }

    /**
     * 按当前PLM登录语言解析页面字段标题和属性Range选项
     **
     * @param context 当前PLM登录上下文
     * @param args 页面字段元数据，包含fieldCode、i18nKey、rangeSource和rangeConfig
     * @return Map 以fieldCode为键的国际化字段元数据
     * @throws Exception 国际化资源或属性Range读取失败时抛出异常
     * @author caipan by codex
     * @date 2026/9/6 20:30
     */
    public Map getPageFieldMetadataLowCode(Context context, String[] args) throws Exception {
        Map params = JPO.unpackArgs(args);
        Object fieldsParam = params.get("fields");
        List fields = fieldsParam instanceof List ? (List) fieldsParam : new ArrayList();
        if (fields.size() > 300) {
            throw new IllegalArgumentException("页面字段数量不能超过300个");
        }

        Map<String, Object> metadata = new LinkedHashMap<>();
        for (Object item : fields) {
            if (!(item instanceof Map)) {
                continue;
            }
            Map field = (Map) item;
            String fieldCode = stringValue(field.get("fieldCode"));
            if (!fieldCode.matches("[A-Z0-9_]{1,100}")) {
                continue;
            }
            Map<String, Object> fieldMetadata = new LinkedHashMap<>();
            String displayName = stringValue(field.get("displayName"));
            String i18nKey = stringValue(field.get("i18nKey"));
            fieldMetadata.put("label", resolvePageI18nLabel(context, i18nKey, displayName));

            if ("PLM_RANGE".equals(stringValue(field.get("rangeSource")))) {
                Map rangeConfig = field.get("rangeConfig") instanceof Map
                        ? (Map) field.get("rangeConfig") : new HashMap();
                String attributeName = stringValue(rangeConfig.get("attributeName"));
                if (!attributeName.matches("[A-Za-z0-9_. -]{1,300}")) {
                    throw new IllegalArgumentException("字段" + fieldCode + "的PLM Range属性名不合法");
                }
                StringList choices = mxAttr.getChoices(context, attributeName);
                List<Map<String, String>> options = new ArrayList<>();
                for (Object choice : choices) {
                    String value = String.valueOf(choice);
                    String label = EnoviaResourceBundle.getRangeI18NString(
                            context, attributeName, value, context.getLocale().getLanguage());
                    Map<String, String> option = new LinkedHashMap<>();
                    option.put("value", value);
                    option.put("label", UIUtil.isNullOrEmpty(label) ? value : label);
                    options.add(option);
                }
                fieldMetadata.put("options", options);
            } else if ("PLM_STATE".equals(stringValue(field.get("rangeSource")))) {
                Map rangeConfig = field.get("rangeConfig") instanceof Map
                        ? (Map) field.get("rangeConfig") : new HashMap();
                String policyName = stringValue(rangeConfig.get("policyName"));
                if (!policyName.matches("[A-Za-z0-9_. -]{1,300}")) {
                    throw new IllegalArgumentException("字段" + fieldCode + "的PLM Policy名称不合法");
                }
                StateRequirementList states = new Policy(policyName).getStateRequirements(context);
                List<Map<String, String>> options = new ArrayList<>();
                for (Object itemState : states) {
                    String value = ((StateRequirement) itemState).getName();
                    String label = EnoviaResourceBundle.getStateI18NString(
                            context, policyName, value, context.getLocale().getLanguage());
                    Map<String, String> option = new LinkedHashMap<>();
                    option.put("value", value);
                    option.put("label", UIUtil.isNullOrEmpty(label) ? value : label);
                    options.add(option);
                }
                fieldMetadata.put("options", options);
            }
            metadata.put(fieldCode, fieldMetadata);
        }
        Map<String, Object> result = new HashMap<>();
        result.put("fields", metadata);
        return result;
    }

    /**
     * 解析一个字段国际化Key，未配置或未命中时返回设计器显示名称
     **
     * @param context 当前PLM登录上下文
     * @param i18nKey 国际化Key，可使用bundle::key格式指定资源包
     * @param fallback 未命中时的默认显示名称
     * @return 当前语言的字段名称
     * @throws Exception PLM国际化资源读取失败时抛出异常
     * @author caipan by codex
     * @date 2026/9/6 20:30
     */
    private String resolvePageI18nLabel(Context context, String i18nKey, String fallback) throws Exception {
        if (UIUtil.isNullOrEmpty(i18nKey)) {
            return fallback;
        }
        String bundle = i18nKey.startsWith("emxComponents.")
                ? "emxComponentsStringResource" : "emxFrameworkStringResource";
        String key = i18nKey;
        int separator = i18nKey.indexOf("::");
        if (separator > 0) {
            bundle = i18nKey.substring(0, separator);
            key = i18nKey.substring(separator + 2);
        }
        if (!bundle.matches("[A-Za-z0-9_.-]{1,200}") || !key.matches("[A-Za-z0-9_.-]{1,300}")) {
            throw new IllegalArgumentException("字段国际化Key不合法");
        }
        String label = EnoviaResourceBundle.getProperty(context, bundle, context.getLocale(), key);
        return UIUtil.isNullOrEmpty(label) || key.equals(label) ? fallback : label;
    }

    /**
     * 将页面元数据参数安全转换为去除首尾空格的字符串
     **
     * @param value 原始参数值
     * @return 非null字符串
     * @author caipan by codex
     * @date 2026/9/6 20:30
     */
    private String stringValue(Object value) {
        return value == null ? "" : String.valueOf(value).trim();
    }

    /**
     * 创建DA申请单并关联所选项目，供PLM低代码页面提交
     **
     * @param context 当前PLM登录上下文
     * @param args 创建参数，包含项目、标题、变更类型、项目阶段和变更说明
     * @return Map 创建对象的objectId和name
     * @throws Exception 参数校验、对象创建、属性写入或项目关联失败时抛出异常
     * @author caipan by codex
     * @date 2026/9/5 23:57
     */
    public Map createDALowCode(Context context, String[] args) throws Exception {
        Map params = JPO.unpackArgs(args);
        String projectId = params.get("projectId") == null ? "" : String.valueOf(params.get("projectId")).trim();
        String title = params.get("title") == null ? "" : String.valueOf(params.get("title")).trim();
        String changeType = params.get("changeType") == null ? "" : String.valueOf(params.get("changeType")).trim();
        String projectPhase = params.get("projectPhase") == null ? "" : String.valueOf(params.get("projectPhase")).trim();
        Object affectedPlantParam = params.get("affectedPlant");
        StringList affectedPlantValues = new StringList();
        StringList allowedAffectedPlants = mxAttr.getChoices(context, "JFAffectsFactory");
        List affectedPlantSource = affectedPlantParam instanceof List
                ? (List) affectedPlantParam
                : Arrays.asList(affectedPlantParam == null ? new String[0]
                : String.valueOf(affectedPlantParam).split(","));
        for (Object value : affectedPlantSource) {
            String plant = value == null ? "" : String.valueOf(value).trim();
            if (!UIUtil.isNullOrEmpty(plant) && !allowedAffectedPlants.contains(plant)) {
                throw new IllegalArgumentException("影响工厂不在允许范围内: " + plant);
            }
            if (!UIUtil.isNullOrEmpty(plant) && !affectedPlantValues.contains(plant)) {
                affectedPlantValues.add(plant);
            }
        }
        String affectedPlant = affectedPlantValues.join(",");
        String deviationReason = params.get("deviationReason") == null ? "" : String.valueOf(params.get("deviationReason")).trim();
        String beforeChange = params.get("beforeChange") == null ? "" : String.valueOf(params.get("beforeChange")).trim();
        String afterChange = params.get("afterChange") == null ? "" : String.valueOf(params.get("afterChange")).trim();

        if (UIUtil.isNullOrEmpty(projectId)) {
            throw new IllegalArgumentException("请选择项目");
        }
        if (UIUtil.isNullOrEmpty(title) || title.length() > 200) {
            throw new IllegalArgumentException("标题不能为空且长度不能超过200个字符");
        }
        if (!Arrays.asList("CustomerRequirements", "DesignDeviations", "ProcessDeviations", "VAVE")
                .contains(changeType)) {
            throw new IllegalArgumentException("变更类型不在允许范围内");
        }
        if (!Arrays.asList("BatchProduction", "DV", "PV").contains(projectPhase)) {
            throw new IllegalArgumentException("项目阶段不在允许范围内");
        }
        if (UIUtil.isNullOrEmpty(affectedPlant)) {
            throw new IllegalArgumentException("影响工厂不能为空");
        }
        if (UIUtil.isNullOrEmpty(deviationReason) || deviationReason.length() > 2000) {
            throw new IllegalArgumentException("偏差原因/描述不能为空且长度不能超过2000个字符");
        }
        if (UIUtil.isNullOrEmpty(beforeChange) || beforeChange.length() > 2000) {
            throw new IllegalArgumentException("变更前不能为空且长度不能超过2000个字符");
        }
        if (UIUtil.isNullOrEmpty(afterChange) || afterChange.length() > 2000) {
            throw new IllegalArgumentException("变更后不能为空且长度不能超过2000个字符");
        }

        DomainObject project = DomainObject.newInstance(context, projectId);
        if (!"Project Space".equals(project.getInfo(context, DomainConstants.SELECT_TYPE))) {
            throw new IllegalArgumentException("选择的对象不是项目");
        }

        ContextUtil.startTransaction(context, true);
        try {
            String generatorName = UICache.getObjectGenerator(context, "type_JFDA", "");
            String name = DomainObject.getAutoGeneratedName(context, generatorName, "");
            Policy policy = new Policy("JFDA");
            String revision = policy.getFirstInSequence(context);
            DomainObject da = DomainObject.newInstance(context);
            da.createObject(context, "JFDA", name, revision, "JFDA", context.getVault().getName());

            Map<String, String> attributes = new HashMap<>();
            attributes.put(DomainConstants.ATTRIBUTE_TITLE, title);
            attributes.put("JFProjectName", project.getDescription(context));
            attributes.put("JFChangeType", changeType);
            attributes.put("JFProjectPhase", projectPhase);
            attributes.put("JFAffectsFactory", affectedPlant);
            attributes.put("JFReasonDeviation", deviationReason);
            attributes.put("JFBeforeChange", beforeChange);
            attributes.put("JFAfterChange", afterChange);
            da.setAttributeValues(context, attributes);
            DomainRelationship.connect(context, da, "JFChange2Project", project);

            ContextUtil.commitTransaction(context);
            Map<String, String> result = new HashMap<>();
            result.put("objectId", da.getId(context));
            result.put("name", name);
            return result;
        } catch (Exception e) {
            ContextUtil.abortTransaction(context);
            throw e;
        }
    }

    /**
     * 批量删除当前用户所有且处于草稿状态的DA申请单
     **
     * @param context 当前PLM登录上下文
     * @param args 删除参数，ids为逗号分隔的DA对象ID
     * @return Map 包含deletedCount
     * @throws Exception 对象类型、所有者、状态校验或删除失败时抛出异常
     * @author caipan by codex
     * @date 2026/9/6 15:30
     */
    public Map deleteDALowCode(Context context, String[] args) throws Exception {
        Map params = JPO.unpackArgs(args);
        StringList objectIds = new StringList();
        Object idsParam = params.get("ids");
        List idSource = idsParam instanceof List
                ? (List) idsParam
                : Arrays.asList(idsParam == null ? new String[0] : String.valueOf(idsParam).split(","));
        for (Object item : idSource) {
            String objectId = stringValue(item);
            if (!UIUtil.isNullOrEmpty(objectId) && !objectIds.contains(objectId)) {
                objectIds.add(objectId);
            }
        }
        if (objectIds.size() == 0 && params.get("items") instanceof List) {
            for (Object item : (List) params.get("items")) {
                if (item instanceof Map) {
                    String objectId = stringValue(((Map) item).get(DomainConstants.SELECT_ID));
                    if (!UIUtil.isNullOrEmpty(objectId) && !objectIds.contains(objectId)) {
                        objectIds.add(objectId);
                    }
                }
            }
        }
        if (objectIds.size() == 0) {
            throw new IllegalArgumentException("请至少选择一条DA申请单");
        }
        if (objectIds.size() > 100) {
            throw new IllegalArgumentException("单次最多删除100条DA申请单");
        }

        String user = context.getUser();
        StringList selects = new StringList();
        selects.add(DomainConstants.SELECT_TYPE);
        selects.add(DomainConstants.SELECT_NAME);
        selects.add(DomainConstants.SELECT_OWNER);
        selects.add(DomainConstants.SELECT_CURRENT);
        for (Object item : objectIds) {
            String objectId = String.valueOf(item);
            Map info = DomainObject.newInstance(context, objectId).getInfo(context, selects);
            String name = UIUtil.getValue(info, DomainConstants.SELECT_NAME);
            if (!"JFDA".equals(UIUtil.getValue(info, DomainConstants.SELECT_TYPE))) {
                throw new IllegalArgumentException("选中对象不是DA申请单: " + name);
            }
            if (!user.equals(UIUtil.getValue(info, DomainConstants.SELECT_OWNER))
                    || !"In_Work".equals(UIUtil.getValue(info, DomainConstants.SELECT_CURRENT))) {
                throw new IllegalArgumentException("只能删除草稿状态并且所有者是自己的DA单据: " + name);
            }
        }

        ContextUtil.startTransaction(context, true);
        try {
            ContextUtil.pushContext(context);
            try {
                DomainObject.deleteObjects(context, objectIds.toStringArray());
            } finally {
                ContextUtil.popContext(context);
            }
            ContextUtil.commitTransaction(context);
        } catch (Exception exception) {
            ContextUtil.abortTransaction(context);
            throw exception;
        }

        Map<String, Object> result = new HashMap<>();
        result.put("deletedCount", objectIds.size());
        return result;
    }

    /**
     * 查询PLM属性Range并按当前登录语言返回AMIS选项
     **
     * @param context 当前PLM登录上下文
     * @param args 查询参数，包含attributeName
     * @return Map 包含options，每项包含Range原值value和国际化显示值label
     * @throws Exception 属性不存在或Range读取失败时抛出异常
     * @author caipan by codex
     * @date 2026/9/6 16:30
     */
    public Map getAttributeRangeOptionsLowCode(Context context, String[] args) throws Exception {
        Map params = JPO.unpackArgs(args);
        String attributeName = params.get("attributeName") == null
                ? "" : String.valueOf(params.get("attributeName")).trim();
        if (UIUtil.isNullOrEmpty(attributeName)
                || !attributeName.matches("[A-Za-z0-9_. -]{1,300}")) {
            throw new IllegalArgumentException("PLM Range属性名不合法");
        }

        String language = context.getLocale().getLanguage();
        StringList choices = mxAttr.getChoices(context, attributeName);
        List<Map<String, String>> options = new ArrayList<>();
        for (Object item : choices) {
            String value = String.valueOf(item);
            String label = EnoviaResourceBundle.getRangeI18NString(
                    context, attributeName, value, language);
            Map<String, String> option = new LinkedHashMap<>();
            option.put("value", value);
            option.put("label", UIUtil.isNullOrEmpty(label) ? value : label);
            options.add(option);
        }
        Map<String, Object> result = new HashMap<>();
        result.put("options", options);
        return result;
    }


 }
