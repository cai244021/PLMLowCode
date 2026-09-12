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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
        //20260910 update by caipan 报表直接读取关闭状态实际到达时间，避免把计划关闭时间当成实际关闭时间
        String actualCloseTimeSelect = "state[Close].actual";
        StringList selects = new StringList();
        selects.addAll(Arrays.asList(DomainConstants.SELECT_ID, DomainConstants.SELECT_NAME,
                DomainConstants.SELECT_OWNER, DomainConstants.SELECT_CURRENT, DomainConstants.SELECT_POLICY,
                DomainConstants.SELECT_ORIGINATED, projectNameSelect, titleSelect, changeTypeSelect,
                projectPhaseSelect, affectedPlantSelect, deviationReasonSelect, beforeChangeSelect,
                afterChangeSelect, startTimeSelect, closeTimeSelect, extendedCloseTimeSelect,
                actualCloseTimeSelect));

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
     * 按对象ID查询可拖入DA列表的一行数据
     **
     * @param context 当前PLM登录上下文
     * @param args 查询参数，包含DA对象objectId
     * @return Map 与DA列表列绑定一致的对象数据
     * @throws Exception 对象不存在、类型不匹配、非当前用户数据或无权访问时抛出异常
     * @author caipan by codex
     * @date 2026/9/12 17:20
     */
    public Map getDATableRowLowCode(Context context, String[] args) throws Exception {
        Map params = JPO.unpackArgs(args);
        String objectId = stringValue(params.get("objectId"));
        if (!objectId.matches("[A-Za-z0-9.:-]{1,100}")) {
            throw new IllegalArgumentException("DA对象ID不合法");
        }

        StringList selects = StringList.create(
                DomainConstants.SELECT_ID, DomainConstants.SELECT_TYPE, DomainConstants.SELECT_NAME,
                DomainConstants.SELECT_OWNER, DomainConstants.SELECT_CURRENT, DomainConstants.SELECT_POLICY,
                DomainConstants.SELECT_ORIGINATED, "attribute[JFProjectName]", "attribute[Title]",
                "attribute[JFChangeType]", "attribute[JFProjectPhase]", "attribute[JFAffectsFactory]",
                "attribute[JFReasonDeviation]", "attribute[JFBeforeChange]", "attribute[JFAfterChange]",
                "attribute[JFDAStartTime]", "attribute[JFDACloseTime]", "attribute[JFDAExtensionTime]");
        Map row = DomainObject.newInstance(context, objectId).getInfo(context, selects);
        if (!"JFDA".equals(UIUtil.getValue(row, DomainConstants.SELECT_TYPE))) {
            throw new IllegalArgumentException("所选对象不是DA申请单");
        }
        if (!context.getUser().equals(UIUtil.getValue(row, DomainConstants.SELECT_OWNER))) {
            throw new IllegalArgumentException("只能加载当前用户拥有的DA申请单");
        }
        return row;
    }

    /**
     * 查询DA详情页所需的特性、零件、流程、生命周期和附件数据
     **
     * @param context 当前PLM登录上下文
     * @param args 查询参数，包含DA对象objectId
     * @return Map 包含DA特性及五页签所需数据
     * @throws Exception 对象不存在、类型不匹配、无权访问或数据查询失败时抛出异常
     * @author caipan by codex
     * @date 2026/9/12 16:30
     */
    public Map getDADetailLowCode(Context context, String[] args) throws Exception {
        Map params = JPO.unpackArgs(args);
        String objectId = stringValue(params.get("objectId"));
        if (!objectId.matches("[A-Za-z0-9.:-]{1,100}")) {
            throw new IllegalArgumentException("DA对象ID不合法");
        }

        DomainObject daObj = DomainObject.newInstance(context, objectId);
        StringList propertySelects = StringList.create(
                DomainConstants.SELECT_ID, DomainConstants.SELECT_TYPE, DomainConstants.SELECT_NAME,
                DomainConstants.SELECT_REVISION, DomainConstants.SELECT_POLICY, DomainConstants.SELECT_CURRENT,
                DomainConstants.SELECT_DESCRIPTION, DomainConstants.SELECT_OWNER, DomainConstants.SELECT_ORIGINATED,
                DomainConstants.SELECT_MODIFIED, "attribute[Title]", "attribute[JFProjectName]",
                "attribute[JFProjectPhase]", "attribute[JFChangeType]", "attribute[JFAffectsFactory]",
                "attribute[JFReasonDeviation]", "attribute[JFBeforeChange]", "attribute[JFAfterChange]",
                "attribute[JFDAStartTime]", "attribute[JFDACloseTime]", "attribute[JFDAExtensionTime]",
                "attribute[JFDAExtensionTimeBak]", "attribute[JFDADelayCount]", "attribute[JFDAIsDelay]");
        Map daInfo = daObj.getInfo(context, propertySelects);
        if (!"JFDA".equals(UIUtil.getValue(daInfo, DomainConstants.SELECT_TYPE))) {
            throw new IllegalArgumentException("所选对象不是DA申请单");
        }
        if (!context.getUser().equals(UIUtil.getValue(daInfo, DomainConstants.SELECT_OWNER))) {
            throw new IllegalArgumentException("无权查看该DA申请单");
        }

        StringList partSelects = StringList.create(
                DomainConstants.SELECT_ID, DomainConstants.SELECT_NAME, DomainConstants.SELECT_REVISION,
                DomainConstants.SELECT_CURRENT, DomainConstants.SELECT_DESCRIPTION, DomainConstants.SELECT_OWNER,
                "attribute[EnterpriseExtension.V_PartNumber]", "attribute[JF_VPMReference.JF_PartNameEN]",
                "attribute[JF_VPMReference.JF_PartNameCN]");
        MapList parts = daObj.getRelatedObjects(context, "JFDA2VPMReference", "VPMReference",
                partSelects, new StringList(), false, true, (short) 1, "", "", 0);

        StringList processSelects = StringList.create(
                DomainConstants.SELECT_ID, DomainConstants.SELECT_NAME, DomainConstants.SELECT_CURRENT,
                DomainConstants.SELECT_OWNER, DomainConstants.SELECT_ORIGINATED, DomainConstants.SELECT_MODIFIED,
                "attribute[Title]", "attribute[Route Status]");
        StringList processRelSelects = StringList.create("attribute[Route Base State]");
        MapList processes = daObj.getRelatedObjects(context, DomainConstants.RELATIONSHIP_OBJECT_ROUTE,
                DomainConstants.TYPE_ROUTE, processSelects, processRelSelects,
                false, true, (short) 1, "", "", 0);

        String policyName = UIUtil.getValue(daInfo, DomainConstants.SELECT_POLICY);
        String currentState = UIUtil.getValue(daInfo, DomainConstants.SELECT_CURRENT);
        StateRequirementList states = new Policy(policyName).getStateRequirements(context);
        StringList lifecycleSelects = new StringList();
        for (Object itemState : states) {
            lifecycleSelects.add("state[" + ((StateRequirement) itemState).getName() + "].actual");
        }
        Map lifecycleInfo = daObj.getInfo(context, lifecycleSelects);
        MapList lifecycle = new MapList();
        int stateIndex = 0;
        boolean reachedCurrent = false;
        for (Object itemState : states) {
            String stateName = ((StateRequirement) itemState).getName();
            Map<String, Object> stateRow = new LinkedHashMap<>();
            stateRow.put("sequence", ++stateIndex);
            stateRow.put("state", stateName);
            stateRow.put("stateLabel", EnoviaResourceBundle.getStateI18NString(
                    context, policyName, stateName, context.getLocale().getLanguage()));
            stateRow.put("actualDate", UIUtil.getValue(lifecycleInfo, "state[" + stateName + "].actual"));
            if (stateName.equals(currentState)) {
                stateRow.put("status", "CURRENT");
                reachedCurrent = true;
            } else {
                stateRow.put("status", reachedCurrent ? "PENDING" : "COMPLETED");
            }
            lifecycle.add(stateRow);
        }

        StringList attachmentSelects = StringList.create(
                DomainConstants.SELECT_ID, DomainConstants.SELECT_NAME, DomainConstants.SELECT_REVISION,
                DomainConstants.SELECT_CURRENT, DomainConstants.SELECT_DESCRIPTION, DomainConstants.SELECT_OWNER,
                DomainConstants.SELECT_MODIFIED, "attribute[Title]");
        MapList attachments = daObj.getRelatedObjects(context, "Reference Document", "Document",
                attachmentSelects, new StringList(), false, true, (short) 1, "", "", 0);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("objectId", objectId);
        result.put("type", UIUtil.getValue(daInfo, DomainConstants.SELECT_TYPE));
        result.put("name", UIUtil.getValue(daInfo, DomainConstants.SELECT_NAME));
        result.put("revision", UIUtil.getValue(daInfo, DomainConstants.SELECT_REVISION));
        result.put("policy", policyName);
        result.put("current", currentState);
        result.put("description", UIUtil.getValue(daInfo, DomainConstants.SELECT_DESCRIPTION));
        result.put("owner", UIUtil.getValue(daInfo, DomainConstants.SELECT_OWNER));
        result.put("originated", UIUtil.getValue(daInfo, DomainConstants.SELECT_ORIGINATED));
        result.put("modified", UIUtil.getValue(daInfo, DomainConstants.SELECT_MODIFIED));
        result.put("title", UIUtil.getValue(daInfo, "attribute[Title]"));
        result.put("projectName", UIUtil.getValue(daInfo, "attribute[JFProjectName]"));
        result.put("projectPhase", UIUtil.getValue(daInfo, "attribute[JFProjectPhase]"));
        result.put("changeType", UIUtil.getValue(daInfo, "attribute[JFChangeType]"));
        result.put("affectedPlant", UIUtil.getValue(daInfo, "attribute[JFAffectsFactory]"));
        result.put("deviationReason", UIUtil.getValue(daInfo, "attribute[JFReasonDeviation]"));
        result.put("beforeChange", UIUtil.getValue(daInfo, "attribute[JFBeforeChange]"));
        result.put("afterChange", UIUtil.getValue(daInfo, "attribute[JFAfterChange]"));
        result.put("startTime", UIUtil.getValue(daInfo, "attribute[JFDAStartTime]"));
        result.put("closeTime", UIUtil.getValue(daInfo, "attribute[JFDACloseTime]"));
        result.put("extensionTime", UIUtil.getValue(daInfo, "attribute[JFDAExtensionTime]"));
        result.put("extensionTimeBak", UIUtil.getValue(daInfo, "attribute[JFDAExtensionTimeBak]"));
        result.put("delayCount", UIUtil.getValue(daInfo, "attribute[JFDADelayCount]"));
        result.put("isDelay", UIUtil.getValue(daInfo, "attribute[JFDAIsDelay]"));
        result.put("parts", parts);
        result.put("processes", processes);
        result.put("lifecycle", lifecycle);
        result.put("attachments", attachments);
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
        //20260906 update by caipan 创建校验与PLM属性Range保持一致，避免设计器动态选项与JPO固定白名单不一致
        StringList allowedChangeTypes = mxAttr.getChoices(context, "JFChangeType");
        StringList allowedProjectPhases = mxAttr.getChoices(context, "JFProjectPhase");
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
        if (UIUtil.isNullOrEmpty(changeType) || !allowedChangeTypes.contains(changeType)) {
            throw new IllegalArgumentException("变更类型不在PLM属性Range内: " + changeType);
        }
        if (UIUtil.isNullOrEmpty(projectPhase) || !allowedProjectPhases.contains(projectPhase)) {
            throw new IllegalArgumentException("项目阶段不在PLM属性Range内: " + projectPhase);
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

    /**
     * 执行低代码搜索配置中的includeOIDprogram并清洗候选对象ID
     **
     * @param context 当前PLM登录上下文
     * @param args 达索搜索请求参数，包含lowCodeIncludeOIDprogram
     * @return StringList 去空、去重后的候选对象ID
     * @throws Exception 原候选程序执行失败或候选对象超过安全上限时抛出异常
     * @author caipan by codex
     * @date 2026/9/9 18:00
     */
    public StringList filterIncludeSearchOIDsLowCode(Context context, String[] args) throws Exception {
        return filterSearchOIDsLowCode(context, args,
                "lowCodeIncludeOIDprogram", "includeOIDprogram");
    }

    /**
     * 执行低代码搜索配置中的excludeOIDprogram并清洗排除对象ID
     **
     * @param context 当前PLM登录上下文
     * @param args 达索搜索请求参数，包含lowCodeExcludeOIDprogram
     * @return StringList 去空、去重后的排除对象ID
     * @throws Exception 原排除程序执行失败或排除对象超过安全上限时抛出异常
     * @author caipan by codex
     * @date 2026/9/9 18:00
     */
    public StringList filterExcludeSearchOIDsLowCode(Context context, String[] args) throws Exception {
        return filterSearchOIDsLowCode(context, args,
                "lowCodeExcludeOIDprogram", "excludeOIDprogram");
    }

    /**
     * 调用原达索搜索ID程序并统一执行数量保护
     **
     * @param context 当前PLM登录上下文
     * @param args 达索搜索请求参数
     * @param delegateParameter 保存原JPO方法的参数名
     * @param originalParameter 达索原始JPO参数名
     * @return StringList 去空、去重后的对象ID
     * @throws Exception 原搜索程序配置不合法、调用失败或结果超过5000条时抛出异常
     * @author caipan by codex
     * @date 2026/9/9 18:00
     */
    private StringList filterSearchOIDsLowCode(Context context, String[] args,
                                                String delegateParameter,
                                                String originalParameter) throws Exception {
        Map params = JPO.unpackArgs(args);
        String programSpec = stringValue(params.get(delegateParameter));
        if (!programSpec.matches("[A-Za-z0-9_$.-]{1,150}:[A-Za-z0-9_$.-]{1,150}")) {
            throw new IllegalArgumentException("低代码搜索候选程序配置不合法");
        }
        String[] programParts = programSpec.split(":", 2);
        if ("JF_LowCode".equals(programParts[0])
                && ("filterIncludeSearchOIDsLowCode".equals(programParts[1])
                || "filterExcludeSearchOIDsLowCode".equals(programParts[1]))) {
            throw new IllegalArgumentException("低代码搜索候选程序不允许循环调用");
        }

        Map delegateParams = new HashMap(params);
        delegateParams.put(originalParameter, programSpec);
        delegateParams.remove(delegateParameter);
        StringList sourceIds = (StringList) JPO.invoke(context, programParts[0], null,
                programParts[1], JPO.packArgs(delegateParams), StringList.class);
        Set<String> uniqueIds = new LinkedHashSet<>();
        if (sourceIds != null) {
            for (Object item : sourceIds) {
                String objectId = stringValue(item);
                if (!UIUtil.isNullOrEmpty(objectId)) {
                    uniqueIds.add(objectId);
                }
            }
        }
        if (uniqueIds.size() > 5000) {
            throw new IllegalArgumentException("低代码搜索候选对象超过5000条，请增加索引过滤条件");
        }
        if (uniqueIds.size() > 2000) {
            JF_LOGGER.warn("PLM low-code search has {} candidate IDs, client={}",
                    uniqueIds.size(), stringValue(params.get("lowCodeSearchClient")));
        }
        StringList result = new StringList();
        result.addAll(uniqueIds);
        return result;
    }

    /**
     * 查询通用PLM报表汇总数据
     **
     * @param context 当前PLM登录上下文
     * @param args 报表参数，必须包含白名单内的reportCode
     * @return Map 包含metrics、charts、insight和filterOptions
     * @throws Exception 报表编码不合法或PLM数据查询失败时抛出异常
     * @author caipan by codex
     * @date 2026/9/10 16:00
     */
    public Map getReportSummaryLowCode(Context context, String[] args) throws Exception {
        Map params = JPO.unpackArgs(args);
        String reportCode = validateReportCode(params.get("reportCode"));
        List<Map<String, Object>> rows = loadReportRowsLowCode(context, reportCode);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("reportCode", reportCode);
        result.put("metrics", buildReportMetricsLowCode(context, reportCode, rows));
        result.put("charts", buildReportChartsLowCode(context, reportCode, rows));
        result.put("insight", buildReportInsightLowCode(context, reportCode, rows));
        result.put("filterOptions", buildReportFilterOptionsLowCode(rows));
        return result;
    }

    /**
     * 查询通用PLM报表明细数据
     **
     * @param context 当前PLM登录上下文
     * @param args 报表编码、scope、筛选、排序和分页参数
     * @return Map 包含当前页items、筛选后total和动态过滤选项
     * @throws Exception 报表编码、筛选参数不合法或PLM数据查询失败时抛出异常
     * @author caipan by codex
     * @date 2026/9/10 16:00
     */
    public Map getReportDetailsLowCode(Context context, String[] args) throws Exception {
        Map params = JPO.unpackArgs(args);
        String reportCode = validateReportCode(params.get("reportCode"));
        List<Map<String, Object>> source = loadReportRowsLowCode(context, reportCode);
        List<Map<String, Object>> filtered = new ArrayList<>();
        for (Map<String, Object> row : source) {
            if (matchesReportFiltersLowCode(reportCode, row, params)) {
                filtered.add(row);
            }
        }
        sortReportRowsLowCode(reportCode, filtered, params);
        int page = reportIntValue(params.get("page"), 1, 1, 100000);
        int perPage = reportIntValue(params.get("perPage"), 20, 1, 200);
        int fromIndex = (int) Math.min((long) (page - 1) * perPage, filtered.size());
        int toIndex = Math.min(fromIndex + perPage, filtered.size());
        MapList items = new MapList();
        for (int index = fromIndex; index < toIndex; index++) {
            items.add(filtered.get(index));
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("items", items);
        result.put("total", filtered.size());
        //20260911 update by caipan 明细CRUD使用独立数据域，直接返回全量可见数据生成的过滤选项
        result.put("filterOptions", buildReportFilterOptionsLowCode(source));
        return result;
    }

    /**
     * 校验通用报表编码白名单
     **
     * @param value 请求中的报表编码
     * @return String 合法报表编码
     * @throws IllegalArgumentException 编码为空或不在白名单时抛出异常
     * @author caipan by codex
     * @date 2026/9/10 16:00
     */
    private String validateReportCode(Object value) {
        String reportCode = stringValue(value);
        if (!Arrays.asList("APPROVAL_TASK", "PROJECT_TASK_STATUS", "CHANGE_EXECUTION")
                .contains(reportCode)) {
            throw new IllegalArgumentException("不支持的PLM报表编码: " + reportCode);
        }
        return reportCode;
    }

    /**
     * 按报表编码加载当前用户有权访问的PLM数据
     **
     * @param context 当前PLM登录上下文
     * @param reportCode 已校验的报表编码
     * @return List 标准化报表行，保留ENOVIA原始select key
     * @throws Exception PLM数据查询失败时抛出异常
     * @author caipan by codex
     * @date 2026/9/10 16:00
     */
    private List<Map<String, Object>> loadReportRowsLowCode(Context context,
                                                             String reportCode) throws Exception {
        if ("APPROVAL_TASK".equals(reportCode)) {
            return loadApprovalTaskRowsLowCode(context);
        }
        if ("PROJECT_TASK_STATUS".equals(reportCode)) {
            return loadProjectTaskRowsLowCode(context);
        }
        return loadChangeExecutionRowsLowCode(context);
    }

    /**
     * 复用达索我的任务查询并生成审核任务报表行
     **
     * @param context 当前PLM登录上下文
     * @return List 当前用户、角色或用户组承接的审核任务
     * @throws Exception 达索任务查询失败时抛出异常
     * @author caipan by codex
     * @date 2026/9/10 16:00
     */
    private List<Map<String, Object>> loadApprovalTaskRowsLowCode(Context context) throws Exception {
        MapList source = (MapList) JPO.invoke(context, "emxInboxTask", null,
                "getMyDeskTasks", JPO.packArgs(new HashMap()), MapList.class);
        List<Map<String, Object>> rows = new ArrayList<>();
        String routeNameSelect = "from[Route Task].to.name";
        String objectNameSelect = "from[Route Task].to.to[Route Scope].from.name";
        String objectTitleSelect = "from[Route Task].to.to[Route Scope].from.attribute[Title]";
        String objectIdSelect = "from[Route Task].to.to[Route Scope].from.id";
        String dueDateSelect = "attribute[Scheduled Completion Date]";
        String taskTypeSelect = "attribute[Route Action]";
        String completeState = FrameworkUtil.lookupStateName(context,
                DomainConstants.POLICY_INBOX_TASK, "state_Complete");
        for (Object item : source) {
            if (!(item instanceof Map)
                    || !DomainConstants.TYPE_INBOX_TASK.equals(stringValue(((Map) item).get(DomainConstants.SELECT_TYPE)))) {
                continue;
            }
            Map sourceRow = (Map) item;
            Map<String, Object> row = new LinkedHashMap<>(sourceRow);
            String taskId = stringValue(sourceRow.get(DomainConstants.SELECT_ID));
            String title = firstReportValue(sourceRow, DomainConstants.SELECT_ATTRIBUTE_TITLE,
                    DomainConstants.SELECT_NAME);
            String current = stringValue(sourceRow.get(DomainConstants.SELECT_CURRENT));
            if (current.equals(completeState)) {
                continue;
            }
            String taskType = stringValue(sourceRow.get(taskTypeSelect));
            Date dueDate = reportDateValue(sourceRow.get(dueDateSelect));
            int overdueDays = dueDate == null || !dueDate.before(new Date())
                    ? 0 : reportDaysBetween(dueDate, new Date());
            int remainingDays = dueDate == null ? Integer.MAX_VALUE
                    : reportDaysBetween(new Date(), dueDate);
            String riskLevel = overdueDays > 0 ? "danger"
                    : remainingDays <= 3 ? "warning" : "success";
            row.put("id", taskId);
            row.put("taskId", taskId);
            row.put("name", title);
            row.put("routeTitle", stringValue(sourceRow.get(routeNameSelect)));
            row.put("taskType", taskType);
            row.put("taskTypeLabel", reportRangeLabel(context, "Route Action", taskType));
            row.put("businessObjectName", firstReportValue(sourceRow,
                    objectTitleSelect, objectNameSelect));
            row.put("current", current);
            row.put("currentLabel", reportStateLabel(context,
                    DomainConstants.POLICY_INBOX_TASK, current));
            row.put("riskLevel", riskLevel);
            row.put("dueDate", reportDateText(dueDate));
            row.put("overdueDays", overdueDays);
            row.put("owner", firstReportValue(sourceRow,
                    "TaskAssignee", DomainConstants.SELECT_OWNER));
            row.put("originated", reportDateText(reportDateValue(
                    sourceRow.get(DomainConstants.SELECT_ORIGINATED))));
            row.put("objectId", stringValue(sourceRow.get(objectIdSelect)));
            row.put("_scope", overdueDays > 0 ? "overdue"
                    : remainingDays <= 3 ? "dueSoon" : "normal");
            rows.add(row);
        }
        return rows;
    }

    /**
     * 查询当前用户拥有的项目任务并生成状态报表行
     **
     * @param context 当前PLM登录上下文
     * @return List 当前用户拥有且有权读取的项目任务
     * @throws Exception 项目任务查询失败时抛出异常
     * @author caipan by codex
     * @date 2026/9/10 16:00
     */
    private List<Map<String, Object>> loadProjectTaskRowsLowCode(Context context) throws Exception {
        String plannedStartSelect = "attribute[Task Estimated Start Date]";
        String plannedFinishSelect = "attribute[Task Estimated Finish Date]";
        String actualFinishSelect = "attribute[Task Actual Finish Date]";
        String progressSelect = "attribute[Percent Complete]";
        String milestoneSelect = "attribute[Task Type]";
        String projectNameSelect = "to[Project Access Key].from.from[Project Access List].to.name";
        String predecessorSelect = "to[Dependency].from.name";
        StringList selects = new StringList();
        selects.addAll(Arrays.asList(DomainConstants.SELECT_ID, DomainConstants.SELECT_TYPE,
                DomainConstants.SELECT_NAME, DomainConstants.SELECT_OWNER, DomainConstants.SELECT_CURRENT,
                DomainConstants.SELECT_POLICY, DomainConstants.SELECT_ORIGINATED,
                DomainConstants.SELECT_ATTRIBUTE_TITLE, plannedStartSelect, plannedFinishSelect,
                actualFinishSelect, progressSelect, milestoneSelect, projectNameSelect,
                predecessorSelect));
        // 复用现有任务类型配置和“当前用户拥有”口径，再批量补齐报表select，避免复制任务类型白名单
        MapList ownedTasks = (MapList) JPO.invoke(context, "JF_MyTask", null,
                "getAllProjectTask", JPO.packArgs(new HashMap()), MapList.class);
        StringList taskIds = new StringList();
        for (Object item : ownedTasks) {
            if (item instanceof Map) {
                String taskId = stringValue(((Map) item).get(DomainConstants.SELECT_ID));
                if (!UIUtil.isNullOrEmpty(taskId)) {
                    taskIds.add(taskId);
                }
            }
        }
        MapList source = taskIds.size() == 0 ? new MapList()
                : DomainObject.getInfo(context, taskIds.toStringArray(), selects);
        List<Map<String, Object>> rows = new ArrayList<>();
        for (Object item : source) {
            Map sourceRow = (Map) item;
            Map<String, Object> row = new LinkedHashMap<>(sourceRow);
            String objectId = stringValue(sourceRow.get(DomainConstants.SELECT_ID));
            String current = stringValue(sourceRow.get(DomainConstants.SELECT_CURRENT));
            String policy = stringValue(sourceRow.get(DomainConstants.SELECT_POLICY));
            Date plannedFinish = reportDateValue(sourceRow.get(plannedFinishSelect));
            Date actualFinish = reportDateValue(sourceRow.get(actualFinishSelect));
            boolean complete = "Complete".equals(current);
            int delayDays = complete || plannedFinish == null || !plannedFinish.before(new Date())
                    ? 0 : reportDaysBetween(plannedFinish, new Date());
            int remainingDays = plannedFinish == null ? Integer.MAX_VALUE
                    : reportDaysBetween(new Date(), plannedFinish);
            String riskLevel = delayDays > 0 ? "danger"
                    : !complete && remainingDays <= 7 ? "warning" : "success";
            row.put("id", objectId);
            row.put("objectId", objectId);
            row.put("projectName", stringValue(sourceRow.get(projectNameSelect)));
            row.put("name", firstReportValue(sourceRow,
                    DomainConstants.SELECT_ATTRIBUTE_TITLE, DomainConstants.SELECT_NAME));
            row.put("milestone", firstReportValue(sourceRow,
                    milestoneSelect, DomainConstants.SELECT_TYPE));
            row.put("current", current);
            row.put("currentLabel", reportStateLabel(context, policy, current));
            row.put("progress", reportNumberValue(sourceRow.get(progressSelect), complete ? 100 : 0));
            row.put("riskLevel", riskLevel);
            row.put("owner", stringValue(sourceRow.get(DomainConstants.SELECT_OWNER)));
            row.put("plannedStart", reportDateText(reportDateValue(sourceRow.get(plannedStartSelect))));
            row.put("plannedFinish", reportDateText(plannedFinish));
            row.put("actualFinish", reportDateText(actualFinish));
            row.put("delayDays", delayDays);
            row.put("predecessor", stringValue(sourceRow.get(predecessorSelect)));
            row.put("_scope", complete ? "complete" : delayDays > 0 ? "delayed" : "active");
            rows.add(row);
        }
        return rows;
    }

    /**
     * 查询当前用户DA并生成变更执行报表行
     **
     * @param context 当前PLM登录上下文
     * @return List 当前用户拥有的DA变更执行数据
     * @throws Exception DA查询失败时抛出异常
     * @author caipan by codex
     * @date 2026/9/10 16:00
     */
    private List<Map<String, Object>> loadChangeExecutionRowsLowCode(Context context) throws Exception {
        Map queryParams = new HashMap();
        queryParams.put("clientSide", true);
        Map queryResult = getCurrentUserDAListLowCode(context, JPO.packArgs(queryParams));
        MapList source = (MapList) queryResult.get("items");
        List<Map<String, Object>> rows = new ArrayList<>();
        for (Object item : source) {
            Map sourceRow = (Map) item;
            Map<String, Object> row = new LinkedHashMap<>(sourceRow);
            String objectId = stringValue(sourceRow.get(DomainConstants.SELECT_ID));
            String current = stringValue(sourceRow.get(DomainConstants.SELECT_CURRENT));
            String changeType = stringValue(sourceRow.get("attribute[JFChangeType]"));
            Date startDate = reportDateValue(sourceRow.get("attribute[JFDAStartTime]"));
            Date closeDate = reportDateValue(sourceRow.get("attribute[JFDACloseTime]"));
            Date extensionDate = reportDateValue(sourceRow.get("attribute[JFDAExtensionTime]"));
            Date actualCloseDate = reportDateValue(sourceRow.get("state[Close].actual"));
            Date targetDate = extensionDate == null ? closeDate : extensionDate;
            boolean closed = "Close".equals(current);
            int overdueDays = closed || targetDate == null || !targetDate.before(new Date())
                    ? 0 : reportDaysBetween(targetDate, new Date());
            int remainingDays = targetDate == null ? Integer.MAX_VALUE
                    : reportDaysBetween(new Date(), targetDate);
            String riskLevel = overdueDays > 0 ? "danger"
                    : !closed && remainingDays <= 7 ? "warning" : "success";
            row.put("id", objectId);
            row.put("objectId", objectId);
            row.put("currentLabel", reportStateLabel(context, "JFDA", current));
            row.put("changeTypeLabel", reportRangeLabel(context, "JFChangeType", changeType));
            row.put("riskLevel", riskLevel);
            row.put("coordinator", stringValue(sourceRow.get(DomainConstants.SELECT_OWNER)));
            row.put("targetDate", reportDateText(targetDate));
            row.put("actualCloseDate", reportDateText(actualCloseDate));
            row.put("cycleDays", startDate == null ? 0
                    : reportDaysBetween(startDate, closed && actualCloseDate != null ? actualCloseDate : new Date()));
            row.put("overdueDays", overdueDays);
            row.put("_scope", closed ? "closed" : overdueDays > 0 ? "overdue"
                    : "Implement".equals(current) ? "implement" : "active");
            rows.add(row);
        }
        return rows;
    }

    /**
     * 根据通用报表请求执行scope及字段筛选
     **
     * @param reportCode 已校验的报表编码
     * @param row 当前报表行
     * @param params 请求参数
     * @return boolean 当前行是否命中筛选条件
     * @author caipan by codex
     * @date 2026/9/10 16:00
     */
    private boolean matchesReportFiltersLowCode(String reportCode, Map<String, Object> row, Map params) {
        String scope = stringValue(params.get("scope"));
        if (!UIUtil.isNullOrEmpty(scope) && !"all".equals(scope)) {
            String rowScope = stringValue(row.get("_scope"));
            if ("highRisk".equals(scope)) {
                if (!"danger".equals(stringValue(row.get("riskLevel")))) {
                    return false;
                }
            } else if (!scope.equals(rowScope)) {
                return false;
            }
        }
        if ("APPROVAL_TASK".equals(reportCode)) {
            return reportContains(row, params.get("keyword"), "name", "routeTitle", "businessObjectName")
                    && reportEquals(row, params.get("taskType"), "taskType")
                    && reportEquals(row, params.get("riskLevel"), "riskLevel")
                    && reportDateRangeMatches(row.get("dueDate"), params.get("dueDateRange"));
        }
        if ("PROJECT_TASK_STATUS".equals(reportCode)) {
            return reportContains(row, params.get("projectKeyword"), "projectName")
                    && reportContains(row, params.get("taskKeyword"), "name")
                    && reportEquals(row, params.get("current"), "current")
                    && reportEquals(row, params.get("owner"), "owner")
                    && reportDateRangeMatches(row.get("plannedFinish"), params.get("plannedRange"));
        }
        return reportContains(row, params.get("keyword"), "name", "attribute[Title]")
                && reportEquals(row, params.get("changeType"), "attribute[JFChangeType]")
                && reportEquals(row, params.get("current"), "current")
                && reportEquals(row, params.get("projectName"), "attribute[JFProjectName]")
                && reportDateRangeMatches(row.get("originated"), params.get("createdRange"));
    }

    /**
     * 对报表明细执行白名单字段排序
     **
     * @param reportCode 已校验的报表编码
     * @param rows 待排序报表行
     * @param params 请求排序参数
     * @author caipan by codex
     * @date 2026/9/10 16:00
     */
    private void sortReportRowsLowCode(String reportCode, List<Map<String, Object>> rows, Map params) {
        String orderBy = stringValue(params.get("orderBy"));
        Set<String> allowed = new LinkedHashSet<>();
        if ("APPROVAL_TASK".equals(reportCode)) {
            allowed.addAll(Arrays.asList("name", "routeTitle", "taskType", "taskTypeLabel", "current", "currentLabel",
                    "dueDate", "overdueDays", "owner", "originated"));
        } else if ("PROJECT_TASK_STATUS".equals(reportCode)) {
            allowed.addAll(Arrays.asList("projectName", "name", "milestone", "current", "currentLabel",
                    "progress", "owner", "plannedStart", "plannedFinish", "actualFinish", "delayDays"));
        } else {
            allowed.addAll(Arrays.asList("name", "attribute[Title]", "attribute[JFProjectName]",
                    "current", "currentLabel", "attribute[JFChangeType]", "changeTypeLabel", "owner", "targetDate", "actualCloseDate",
                    "cycleDays", "overdueDays", "originated"));
        }
        if (!allowed.contains(orderBy)) {
            return;
        }
        final String sortField = orderBy;
        final int direction = "desc".equalsIgnoreCase(stringValue(params.get("orderDir"))) ? -1 : 1;
        Collections.sort(rows, new Comparator<Map<String, Object>>() {
            public int compare(Map<String, Object> left, Map<String, Object> right) {
                Object leftValue = left.get(sortField);
                Object rightValue = right.get(sortField);
                if (leftValue instanceof Number && rightValue instanceof Number) {
                    return direction * Double.compare(((Number) leftValue).doubleValue(),
                            ((Number) rightValue).doubleValue());
                }
                return direction * stringValue(leftValue).compareToIgnoreCase(stringValue(rightValue));
            }
        });
    }

    /**
     * 生成通用报表指标卡
     **
     * @param context 当前PLM登录上下文
     * @param reportCode 已校验的报表编码
     * @param rows 全量报表行
     * @return List 动态指标卡数据
     * @author caipan by codex
     * @date 2026/9/10 16:00
     */
    private List<Map<String, Object>> buildReportMetricsLowCode(Context context, String reportCode,
                                                                List<Map<String, Object>> rows) {
        boolean zh = "zh".equalsIgnoreCase(context.getLocale().getLanguage());
        List<Map<String, Object>> metrics = new ArrayList<>();
        if ("APPROVAL_TASK".equals(reportCode)) {
            addReportMetric(metrics, "total", zh ? "待审核任务" : "Pending tasks", rows.size(), "all", "primary");
            addReportMetric(metrics, "overdue", zh ? "已逾期" : "Overdue", countReportScope(rows, "overdue"), "overdue", "danger");
            addReportMetric(metrics, "dueSoon", zh ? "三天内到期" : "Due in 3 days", countReportScope(rows, "dueSoon"), "dueSoon", "warning");
            addReportMetric(metrics, "highRisk", zh ? "高风险" : "High risk", countReportRisk(rows, "danger"), "highRisk", "danger");
        } else if ("PROJECT_TASK_STATUS".equals(reportCode)) {
            addReportMetric(metrics, "total", zh ? "项目任务" : "Project tasks", rows.size(), "all", "primary");
            addReportMetric(metrics, "delayed", zh ? "延期任务" : "Delayed", countReportScope(rows, "delayed"), "delayed", "danger");
            addReportMetric(metrics, "active", zh ? "进行中" : "Active", countReportScope(rows, "active"), "active", "warning");
            addReportMetric(metrics, "complete", zh ? "已完成" : "Complete", countReportScope(rows, "complete"), "complete", "success");
        } else {
            addReportMetric(metrics, "total", zh ? "变更总数" : "Changes", rows.size(), "all", "primary");
            addReportMetric(metrics, "overdue", zh ? "逾期变更" : "Overdue", countReportScope(rows, "overdue"), "overdue", "danger");
            addReportMetric(metrics, "implement", zh ? "执行中" : "Implementing", countReportScope(rows, "implement"), "implement", "warning");
            addReportMetric(metrics, "closed", zh ? "已关闭" : "Closed", countReportScope(rows, "closed"), "closed", "success");
        }
        return metrics;
    }

    /**
     * 生成通用报表图表配置
     **
     * @param context 当前PLM登录上下文
     * @param reportCode 已校验的报表编码
     * @param rows 全量报表行
     * @return List 两个可由AMIS直接渲染的ECharts配置
     * @author caipan by codex
     * @date 2026/9/10 16:00
     */
    private List<Map<String, Object>> buildReportChartsLowCode(Context context, String reportCode,
                                                               List<Map<String, Object>> rows) {
        boolean zh = "zh".equalsIgnoreCase(context.getLocale().getLanguage());
        String firstField = "currentLabel";
        String secondField = "APPROVAL_TASK".equals(reportCode) ? "taskTypeLabel"
                : "PROJECT_TASK_STATUS".equals(reportCode) ? "projectName" : "changeTypeLabel";
        String firstTitle = zh ? "状态分布" : "Status distribution";
        String secondTitle = "APPROVAL_TASK".equals(reportCode)
                ? (zh ? "任务类型分布" : "Task type distribution")
                : "PROJECT_TASK_STATUS".equals(reportCode)
                ? (zh ? "项目任务分布" : "Tasks by project")
                : (zh ? "变更类型分布" : "Change type distribution");
        List<Map<String, Object>> charts = new ArrayList<>();
        charts.add(reportChart(firstTitle, countReportValues(rows, firstField), "bar"));
        charts.add(reportChart(secondTitle, countReportValues(rows, secondField), "pie"));
        return charts;
    }

    /**
     * 生成报表智能提示
     **
     * @param context 当前PLM登录上下文
     * @param reportCode 已校验的报表编码
     * @param rows 全量报表行
     * @return Map AMIS alert所需level和message
     * @author caipan by codex
     * @date 2026/9/10 16:00
     */
    private Map<String, Object> buildReportInsightLowCode(Context context, String reportCode,
                                                          List<Map<String, Object>> rows) {
        boolean zh = "zh".equalsIgnoreCase(context.getLocale().getLanguage());
        int riskCount = countReportRisk(rows, "danger");
        Map<String, Object> insight = new LinkedHashMap<>();
        insight.put("level", riskCount > 0 ? "warning" : "success");
        insight.put("message", riskCount > 0
                ? (zh ? "当前有 " + riskCount + " 条高风险数据，请优先处理。"
                : riskCount + " high-risk item(s) require attention.")
                : (zh ? "当前未发现高风险数据。" : "No high-risk items were found."));
        return insight;
    }

    /**
     * 从报表行提取通用筛选选项
     **
     * @param rows 全量报表行
     * @return Map 各筛选字段的label/value选项
     * @author caipan by codex
     * @date 2026/9/10 16:00
     */
    private Map<String, Object> buildReportFilterOptionsLowCode(List<Map<String, Object>> rows) {
        Map<String, Object> options = new LinkedHashMap<>();
        options.put("taskType", reportOptions(rows, "taskType", "taskTypeLabel"));
        options.put("riskLevel", reportOptions(rows, "riskLevel", "riskLevel"));
        options.put("current", reportOptions(rows, "current", "currentLabel"));
        options.put("owner", reportOptions(rows, "owner", "owner"));
        options.put("changeType", reportOptions(rows, "attribute[JFChangeType]", "changeTypeLabel"));
        options.put("projectName", reportOptions(rows, "attribute[JFProjectName]", "attribute[JFProjectName]"));
        return options;
    }

    /**
     * 创建单个报表指标卡数据
     **
     * @param metrics 指标集合
     * @param code 指标编码
     * @param label 显示名称
     * @param value 指标值
     * @param scope 点击后明细范围
     * @param level AMIS按钮级别
     * @author caipan by codex
     * @date 2026/9/10 16:00
     */
    private void addReportMetric(List<Map<String, Object>> metrics, String code, String label,
                                 int value, String scope, String level) {
        Map<String, Object> metric = new LinkedHashMap<>();
        metric.put("code", code);
        metric.put("label", label);
        metric.put("value", value);
        metric.put("unit", "");
        metric.put("scope", scope);
        metric.put("level", level);
        metric.put("description", "");
        metrics.add(metric);
    }

    /**
     * 将分组计数转换为ECharts配置
     **
     * @param title 图表标题
     * @param values 分组计数
     * @param type bar或pie
     * @return Map 图表标题、通用数据和option
     * @author caipan by codex
     * @date 2026/9/10 16:00
     */
    private Map<String, Object> reportChart(String title, Map<String, Integer> values, String type) {
        Map<String, Object> chart = new LinkedHashMap<>();
        chart.put("title", title);
        chart.put("categories", new ArrayList<>(values.keySet()));
        chart.put("values", new ArrayList<>(values.values()));
        List<Map<String, Object>> chartData = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : values.entrySet()) {
            Map<String, Object> dataItem = new LinkedHashMap<>();
            dataItem.put("name", entry.getKey());
            dataItem.put("value", entry.getValue());
            chartData.add(dataItem);
        }
        chart.put("data", chartData);
        Map<String, Object> option = new LinkedHashMap<>();
        option.put("tooltip", new LinkedHashMap<>());
        List<Map<String, Object>> series = new ArrayList<>();
        Map<String, Object> seriesItem = new LinkedHashMap<>();
        seriesItem.put("type", type);
        if ("pie".equals(type)) {
            seriesItem.put("radius", Arrays.asList("35%", "65%"));
            seriesItem.put("data", chartData);
        } else {
            Map<String, Object> xAxis = new LinkedHashMap<>();
            xAxis.put("type", "category");
            xAxis.put("data", new ArrayList<>(values.keySet()));
            option.put("xAxis", xAxis);
            Map<String, Object> yAxis = new LinkedHashMap<>();
            yAxis.put("type", "value");
            option.put("yAxis", yAxis);
            seriesItem.put("data", new ArrayList<>(values.values()));
        }
        series.add(seriesItem);
        option.put("series", series);
        chart.put("option", option);
        return chart;
    }

    /**
     * 统计报表指定字段的分组数量
     **
     * @param rows 报表行
     * @param field 分组字段
     * @return Map 分组名称及数量
     * @author caipan by codex
     * @date 2026/9/10 16:00
     */
    private Map<String, Integer> countReportValues(List<Map<String, Object>> rows, String field) {
        Map<String, Integer> values = new LinkedHashMap<>();
        for (Map<String, Object> row : rows) {
            String key = stringValue(row.get(field));
            if (UIUtil.isNullOrEmpty(key)) {
                key = "-";
            }
            values.put(key, values.containsKey(key) ? values.get(key) + 1 : 1);
        }
        return values;
    }

    /**
     * 构造去重后的AMIS下拉选项
     **
     * @param rows 报表行
     * @param valueField 选项值字段
     * @param labelField 选项显示字段
     * @return List label/value选项
     * @author caipan by codex
     * @date 2026/9/10 16:00
     */
    private List<Map<String, String>> reportOptions(List<Map<String, Object>> rows,
                                                     String valueField, String labelField) {
        Map<String, String> unique = new LinkedHashMap<>();
        for (Map<String, Object> row : rows) {
            String value = stringValue(row.get(valueField));
            if (!UIUtil.isNullOrEmpty(value)) {
                String label = firstReportValue(row, labelField, valueField);
                unique.put(value, UIUtil.isNullOrEmpty(label) ? value : label);
            }
        }
        List<Map<String, String>> options = new ArrayList<>();
        for (Map.Entry<String, String> entry : unique.entrySet()) {
            Map<String, String> option = new LinkedHashMap<>();
            option.put("value", entry.getKey());
            option.put("label", entry.getValue());
            options.add(option);
        }
        return options;
    }

    /**
     * 判断报表字段是否包含关键字
     **
     * @param row 报表行
     * @param keyword 关键字
     * @param fields 参与匹配的字段
     * @return boolean 未输入关键字或任一字段命中时返回true
     * @author caipan by codex
     * @date 2026/9/10 16:00
     */
    private boolean reportContains(Map<String, Object> row, Object keyword, String... fields) {
        String expected = stringValue(keyword).toLowerCase();
        if (UIUtil.isNullOrEmpty(expected)) {
            return true;
        }
        for (String field : fields) {
            if (stringValue(row.get(field)).toLowerCase().contains(expected)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 判断报表字段是否等于筛选值
     **
     * @param row 报表行
     * @param expected 筛选值
     * @param field 字段名
     * @return boolean 未选择或值相等时返回true
     * @author caipan by codex
     * @date 2026/9/10 16:00
     */
    private boolean reportEquals(Map<String, Object> row, Object expected, String field) {
        String value = stringValue(expected);
        return UIUtil.isNullOrEmpty(value) || value.equals(stringValue(row.get(field)));
    }

    /**
     * 判断PLM日期是否落在AMIS日期范围内
     **
     * @param value 当前行日期
     * @param range 日期范围参数
     * @return boolean 未选择范围或日期在范围内时返回true
     * @author caipan by codex
     * @date 2026/9/10 16:00
     */
    private boolean reportDateRangeMatches(Object value, Object range) {
        if (!(range instanceof List) || ((List) range).size() < 2) {
            return true;
        }
        Date current = reportDateValue(value);
        Date start = reportDateValue(((List) range).get(0));
        Date end = reportDateValue(((List) range).get(1));
        return current != null && (start == null || !current.before(start))
                && (end == null || !current.after(end));
    }

    /**
     * 读取报表行中首个非空字段
     **
     * @param row 报表行
     * @param fields 候选字段
     * @return String 首个非空值
     * @author caipan by codex
     * @date 2026/9/10 16:00
     */
    private String firstReportValue(Map row, String... fields) {
        for (String field : fields) {
            String value = stringValue(row.get(field));
            if (!UIUtil.isNullOrEmpty(value)) {
                return value;
            }
        }
        return "";
    }

    /**
     * 解析报表整数参数并限制范围
     **
     * @param value 参数值
     * @param defaultValue 默认值
     * @param min 最小值
     * @param max 最大值
     * @return int 合法整数
     * @author caipan by codex
     * @date 2026/9/10 16:00
     */
    private int reportIntValue(Object value, int defaultValue, int min, int max) {
        try {
            int result = value instanceof Number ? ((Number) value).intValue()
                    : Integer.parseInt(stringValue(value));
            return Math.max(min, Math.min(max, result));
        } catch (Exception ignored) {
            return defaultValue;
        }
    }

    /**
     * 解析报表数值字段
     **
     * @param value PLM字段值
     * @param defaultValue 默认值
     * @return int 取整后的数值
     * @author caipan by codex
     * @date 2026/9/10 16:00
     */
    private int reportNumberValue(Object value, int defaultValue) {
        try {
            return (int) Math.round(Double.parseDouble(stringValue(value)));
        } catch (Exception ignored) {
            return defaultValue;
        }
    }

    /**
     * 解析Matrix日期或ISO日期
     **
     * @param value 日期值
     * @return Date 可计算日期，空值或格式不合法时返回null
     * @author caipan by codex
     * @date 2026/9/10 16:00
     */
    private Date reportDateValue(Object value) {
        String text = stringValue(value);
        if (UIUtil.isNullOrEmpty(text) || "-".equals(text)) {
            return null;
        }
        try {
            return eMatrixDateFormat.getJavaDate(text);
        } catch (Exception ignored) {
            for (String pattern : Arrays.asList("yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd")) {
                try {
                    return new SimpleDateFormat(pattern).parse(text);
                } catch (Exception parseIgnored) {
                    // 尝试下一种受支持格式
                }
            }
            return null;
        }
    }

    /**
     * 将日期统一为AMIS可识别格式
     **
     * @param value 日期值
     * @return String yyyy-MM-dd HH:mm:ss格式，空日期返回空字符串
     * @author caipan by codex
     * @date 2026/9/10 16:00
     */
    private String reportDateText(Date value) {
        return value == null ? "" : new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(value);
    }

    /**
     * 计算两个日期相差的自然天数
     **
     * @param start 开始日期
     * @param end 结束日期
     * @return int 非负天数
     * @author caipan by codex
     * @date 2026/9/10 16:00
     */
    private int reportDaysBetween(Date start, Date end) {
        if (start == null || end == null || !end.after(start)) {
            return 0;
        }
        long millis = end.getTime() - start.getTime();
        return (int) Math.ceil(millis / 86400000.0d);
    }

    /**
     * 获取PLM状态国际化名称
     **
     * @param context 当前PLM登录上下文
     * @param policyName 策略名称
     * @param stateName 状态名称
     * @return String 国际化名称，解析失败回退原值
     * @author caipan by codex
     * @date 2026/9/10 16:00
     */
    private String reportStateLabel(Context context, String policyName, String stateName) {
        if (UIUtil.isNullOrEmpty(policyName) || UIUtil.isNullOrEmpty(stateName)) {
            return stateName;
        }
        try {
            String label = EnoviaResourceBundle.getStateI18NString(
                    context, policyName, stateName, context.getLocale().getLanguage());
            return UIUtil.isNullOrEmpty(label) ? stateName : label;
        } catch (Exception ignored) {
            return stateName;
        }
    }

    /**
     * 获取PLM属性Range国际化名称
     **
     * @param context 当前PLM登录上下文
     * @param attributeName 属性名称
     * @param rangeValue Range原值
     * @return String 国际化名称，解析失败回退原值
     * @author caipan by codex
     * @date 2026/9/10 16:00
     */
    private String reportRangeLabel(Context context, String attributeName, String rangeValue) {
        if (UIUtil.isNullOrEmpty(rangeValue)) {
            return rangeValue;
        }
        try {
            String label = EnoviaResourceBundle.getRangeI18NString(
                    context, attributeName, rangeValue, context.getLocale().getLanguage());
            return UIUtil.isNullOrEmpty(label) ? rangeValue : label;
        } catch (Exception ignored) {
            return rangeValue;
        }
    }

    /**
     * 统计指定scope的报表行
     **
     * @param rows 报表行
     * @param scope scope值
     * @return int 命中数量
     * @author caipan by codex
     * @date 2026/9/10 16:00
     */
    private int countReportScope(List<Map<String, Object>> rows, String scope) {
        int count = 0;
        for (Map<String, Object> row : rows) {
            if (scope.equals(stringValue(row.get("_scope")))) {
                count++;
            }
        }
        return count;
    }

    /**
     * 统计指定风险级别的报表行
     **
     * @param rows 报表行
     * @param riskLevel 风险级别
     * @return int 命中数量
     * @author caipan by codex
     * @date 2026/9/10 16:00
     */
    private int countReportRisk(List<Map<String, Object>> rows, String riskLevel) {
        int count = 0;
        for (Map<String, Object> row : rows) {
            if (riskLevel.equals(stringValue(row.get("riskLevel")))) {
                count++;
            }
        }
        return count;
    }


 }
