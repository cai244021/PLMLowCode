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
     * @throws Exception DA查询或国际化转换失败时抛出异常
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
        String language = context.getLocale().getLanguage();
        List<Map<String, Object>> items = new ArrayList<>();
        for (int index = fromIndex; index < toIndex; index++) {
            Map sourceRow = (Map) source.get(index);
            String current = UIUtil.getValue(sourceRow, DomainConstants.SELECT_CURRENT);
            String policy = UIUtil.getValue(sourceRow, DomainConstants.SELECT_POLICY);
            String changeType = UIUtil.getValue(sourceRow, changeTypeSelect);
            String projectPhase = UIUtil.getValue(sourceRow, projectPhaseSelect);
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("objectId", UIUtil.getValue(sourceRow, DomainConstants.SELECT_ID));
            row.put("name", UIUtil.getValue(sourceRow, DomainConstants.SELECT_NAME));
            row.put("projectName", UIUtil.getValue(sourceRow, projectNameSelect));
            row.put("title", UIUtil.getValue(sourceRow, titleSelect));
            row.put("current", current);
            row.put("currentLabel", UIUtil.isNullOrEmpty(current) ? ""
                    : EnoviaResourceBundle.getStateI18NString(context, policy, current, language));
            row.put("changeType", changeType);
            row.put("changeTypeLabel", UIUtil.isNullOrEmpty(changeType) ? ""
                    : EnoviaResourceBundle.getRangeI18NString(context, "JFChangeType", changeType, language));
            row.put("projectPhase", projectPhase);
            row.put("projectPhaseLabel", UIUtil.isNullOrEmpty(projectPhase) ? ""
                    : EnoviaResourceBundle.getRangeI18NString(context, "JFProjectPhase", projectPhase, language));
            row.put("affectedPlant", UIUtil.getValue(sourceRow, affectedPlantSelect));
            row.put("deviationReason", UIUtil.getValue(sourceRow, deviationReasonSelect));
            row.put("beforeChange", UIUtil.getValue(sourceRow, beforeChangeSelect));
            row.put("afterChange", UIUtil.getValue(sourceRow, afterChangeSelect));
            row.put("daStartTime", UIUtil.getValue(sourceRow, startTimeSelect));
            row.put("daCloseTime", UIUtil.getValue(sourceRow, closeTimeSelect));
            row.put("extendedCloseTime", UIUtil.getValue(sourceRow, extendedCloseTimeSelect));
            row.put("creator", UIUtil.getValue(sourceRow, DomainConstants.SELECT_OWNER));
            row.put("createdAt", UIUtil.getValue(sourceRow, DomainConstants.SELECT_ORIGINATED));
            items.add(row);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("items", items);
        result.put("total", total);
        return result;
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


 }
