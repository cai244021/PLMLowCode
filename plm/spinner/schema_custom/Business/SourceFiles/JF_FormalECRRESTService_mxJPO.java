import com.google.gson.Gson;
import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.DomainRelationship;
import com.matrixone.apps.domain.util.*;
import com.matrixone.apps.framework.ui.UIUtil;
import matrix.db.BusinessObject;
import matrix.db.BusinessObjectList;
import matrix.db.Context;
import matrix.db.JPO;
import matrix.util.MatrixException;
import matrix.util.StringList;
import org.apache.commons.text.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.MessageFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;
import static com.matrixone.apps.domain.DomainConstants.*;
import static com.matrixone.apps.domain.MultiValueSelects.RELATIONSHIP_REFERENCE_DOCUMENT;

/**
 * @author CHENYAN
 * @version v1.0.0
 * 创建时间：2026/1/21 10:58
 * @description
 */
public class JF_FormalECRRESTService_mxJPO implements JF_PLMConstants_mxJPO {
    private static final Gson GSON = new Gson();
    private static final Logger JF_LOGGER = LoggerFactory.getLogger(JF_FormalECRRESTService_mxJPO.class);
    //ECR 会签状态之前所有状态
    public static final StringList ECR_STATE_SIGN_BEFORE_List = new StringList();
    public static Map<String, String> ROLE_MAPPING_MAP = new HashMap();
    public StringList typeSelectList = new StringList();

    //AME编辑form是否展示零件属性值
    public static final StringList EDIT_AME_FORM_FIELD_LIST = new StringList();

    //初始化 range值映射表
    static {
        ROLE_MAPPING_MAP.put("PRR", "Purchasing representative");
        ROLE_MAPPING_MAP.put("AME", "AME representative");
        ROLE_MAPPING_MAP.put("FoamAME", "Foam AME representative");
        ROLE_MAPPING_MAP.put("TrimAME", "Trim AME representative");
        ROLE_MAPPING_MAP.put("AQE", "AQE representative/PQL");
        ROLE_MAPPING_MAP.put("LOR", "Logistics representative");
        ROLE_MAPPING_MAP.put("Launch_Manager", "Launch manager");
        ROLE_MAPPING_MAP.put("Costing", "Costing");
        ROLE_MAPPING_MAP.put("BUR", "Business manager");
        ROLE_MAPPING_MAP.put("APR", "Financial BP");
        ROLE_MAPPING_MAP.put("ICO", "Internal Supplier");
        ECR_STATE_SIGN_BEFORE_List.add("Create");
        ECR_STATE_SIGN_BEFORE_List.add("Review");
        //初始化可以下载和上传ECR表格清单角色
//        EDIT_ECRCosting_TABLE_ROLE_LIST.add("PRR");
//        EDIT_ECRCosting_TABLE_ROLE_LIST.add("Costing");
//        EDIT_ECRCosting_TABLE_ROLE_LIST.add("ICO");
////        EDIT_ECRCosting_TABLE_ROLE_LIST.add("APR");
//        EDIT_ECRController_TABLE_ROLE_LIST.add("APR");
//        EDIT_ECRController_TABLE_ROLE_LIST.add("AME");
//        EDIT_ECRController_TABLE_ROLE_LIST.add("FoamAME");
//        EDIT_ECRController_TABLE_ROLE_LIST.add("TrimAME");
//        EDIT_ECRController_TABLE_ROLE_LIST.add("PRR");
//        EDIT_ECRController_TABLE_ROLE_LIST.add("Costing");
        //发泡
        EDIT_AME_FORM_FIELD_LIST.add("U");
        //整椅
        EDIT_AME_FORM_FIELD_LIST.add("C");
        //面套
        EDIT_AME_FORM_FIELD_LIST.add("T");
    }
    //初始化 成员变量
    {
        typeSelectList.add(SELECT_ID);
        typeSelectList.add(SELECT_CURRENT);
        typeSelectList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JFChangeCyclesOutsourcing);
        typeSelectList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JFChangeDevelopmentCostsOutsourcing);
        typeSelectList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JFSupplierOrScrapDepotOutsourcing);
        typeSelectList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JFChangeCyclesSelfMake);
        typeSelectList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JFChangeDevelopmentCostsSelfMake);
        typeSelectList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JFSupplierOrScrapDepotSelfMake);
        typeSelectList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JFChangesInvestmentsWholeChair);
        typeSelectList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JFChangesInvestmentsFoaming);
        typeSelectList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JFChangesInvestmentsFaceCovers);
        typeSelectList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JFChangesInvestmentsWholeChairExternal);
        typeSelectList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JFChangesInvestmentsFoamingExternal);
        typeSelectList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JFChangesInvestmentsFaceCoversExternal);
        typeSelectList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JFChangesFixtures);
        typeSelectList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JFCostOfQuality);
        typeSelectList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JFCostOfTrial);
        typeSelectList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JFChangesFixturesExternal);
        typeSelectList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JFCostOfQualityExternal);
        typeSelectList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JFCostOfTrialExternal);
        typeSelectList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JFCostPackagingLogistics);
        typeSelectList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JFCostPackagingLogisticsExternal);
        typeSelectList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JFMaterialsFinishedProductsRework);
        typeSelectList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JFChangesMouldCost);
        typeSelectList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JFChangesUnitPriceCostExternal);
        typeSelectList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JFChangesMouldCostExternal);
        typeSelectList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JFChangesInvestment);
        typeSelectList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JFChangesDevelopment);
        typeSelectList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JFChangesMould);
        typeSelectList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JFSumInventoryScrapAmount);
        typeSelectList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JFChangesInvestmentExternal);
        typeSelectList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JFChangesDevelopmentExternal);
        typeSelectList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JFChangesMouldExternal);
        typeSelectList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JFSumInventoryScrapAmountExternal);
        typeSelectList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JFAcknowledgmentContentAmount);
        typeSelectList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JFOtherFee);
        typeSelectList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JFAPRNotes);
        typeSelectList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JFTestFee);

        // add by chenyan 新增AME 备注 2025/05/12
        typeSelectList.add(JF_PLMConstants_mxJPO.SELECT_ATTRIBUTE_JFAMENotes);
        typeSelectList.add(JF_PLMConstants_mxJPO.SELECT_ATTRIBUTE_JFFoamAMENotes);
        typeSelectList.add(JF_PLMConstants_mxJPO.SELECT_ATTRIBUTE_JFTrimAMENotes);

    }


    public String getECRInfo(Context context, String[] args) throws Exception {
        Map reqMap = (Map) JPO.unpackArgs(args);
        String strObjectId = (String) reqMap.get("objectId");
        String strHostUrl = (String) reqMap.get("hostUrl");
        JF_LOGGER.info("strObjectId:{}", strObjectId);
        JF_LOGGER.info("strHostUrl:{}", strHostUrl);
        Map res = new HashMap<String, Object>();
        try {
            String strLoginUser = context.getUser();
            ContextUtil.pushContext(context);
            DomainObject ecr = DomainObject.newInstance(context, strObjectId);
            //ECR 当前状态
            StringList ECRSelectList = new StringList();
            ECRSelectList.add(SELECT_CURRENT);
            ECRSelectList.add(SELECT_OWNER);
            ECRSelectList.add("from[JFChange2Project].to.id");
            ECRSelectList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JFCHANGESOURCE);
            Map ECRInfo = ecr.getInfo(context, ECRSelectList);
            String strCurrent = (String) ECRInfo.get(SELECT_CURRENT);
            String strOwner = (String) ECRInfo.get(SELECT_OWNER);
            String strProjectId = (String) ECRInfo.get("from[JFChange2Project].to.id");
            //方法里面已经push
            ContextUtil.popContext(context);
            String strProjectOwner = JF_Util_mxJPO.getProjectManager(context, new String[]{strProjectId});
            ContextUtil.pushContext(context);
            String strChangeSource = (String) ECRInfo.get(JF_PLMConstants_mxJPO.SELECT_ATTR_JFCHANGESOURCE);
            HashMap<String, String> ECR = new HashMap<>();
            ECR.put("current", strCurrent);
            ECR.put("JFChangeSource", strChangeSource);
            //SDT-采购代表
            HashMap<String, Object> PRR = new HashMap<>();
            //SDT-AME代表
            HashMap<String, Object> AME = new HashMap<>();
            //发泡AME
            HashMap<String, Object> FoamAME = new HashMap<>();
            //面套AME
            HashMap<String, Object> TrimAME = new HashMap<>();
            //SDT-AQE代表
            HashMap<String, Object> AQE = new HashMap<>();
            //SDT-物流代表
            HashMap<String, Object> LOR = new HashMap<>();
            //SDT-Launch Manager
            HashMap<String, Object> Launch_Manager = new HashMap<>();
            //SDT-Costing
            HashMap<String, Object> Costing = new HashMap<>();
            //SDT-商务代表
            HashMap<String, Object> BUR = new HashMap<>();
            //财务BP（APR）
            HashMap<String, Object> APR = new HashMap<>();
            //内部
            HashMap<String, Object> ICO = new HashMap<>();
            MapList loginUserSignTaskRole = new MapList();
            //标识是否生成AME会签任务
            boolean isCreateAMTSignTask = false;
            MapList allSignTaskInECR  = null;
            //还没有生成会签任务
            if (ECR_STATE_SIGN_BEFORE_List.contains(strCurrent)) {
                initECRAttrValue(PRR, null, "PRR");
                initECRAttrValue(AME, null, "AME");
                initECRAttrValue(FoamAME, null, "FoamAME");
                initECRAttrValue(TrimAME, null, "TrimAME");
                initECRAttrValue(AQE, null, "AQE");
                initECRAttrValue(LOR, null, "LOR");
                initECRAttrValue(Launch_Manager, null, "Launch_Manager");
                initECRAttrValue(Costing, null, "Costing");
                initECRAttrValue(BUR, null, "BUR");
                initECRAttrValue(APR, null, "APR");
                initECRFormEditAccess("PRR", PRR, null);
                initECRFormEditAccess("AME", AME, null);
                initECRFormEditAccess("FoamAME", FoamAME, null);
                initECRFormEditAccess("TrimAME", TrimAME, null);
                initECRFormEditAccess("AQE", AQE, null);
                initECRFormEditAccess("LOR", LOR, null);
                initECRFormEditAccess("Launch_Manager", Launch_Manager, null);
                initECRFormEditAccess("Costing", Costing, null);
                initECRFormEditAccess("BUR", BUR, null);
                initECRFormEditAccess("APR", APR, null);
                initECRFormEditAccess("ICO", ICO, null);
                res.put("login_role", new HashSet<>());
            } else {
                //获取所有的会签任务
                allSignTaskInECR = getAllSignTaskInECR(context, ecr);
                //角色不为空的任务
                loginUserSignTaskRole = JF_PublicMethodClass_mxJPO.getLoginUserSignTaskRoleByECRAndUser(context, new String[]{strObjectId, strLoginUser});
                JF_LOGGER.info("loginUserSignTaskRole:{}", loginUserSignTaskRole);
                Map ecrInfo = ecr.getInfo(context, typeSelectList);
                initECRAttrValue(PRR, ecrInfo, "PRR");
                initECRAttrValue(AME, ecrInfo, "AME");
                initECRAttrValue(FoamAME, ecrInfo, "FoamAME");
                initECRAttrValue(TrimAME, ecrInfo, "TrimAME");
                initECRAttrValue(AQE, ecrInfo, "AQE");
                initECRAttrValue(LOR, ecrInfo, "LOR");
                initECRAttrValue(Launch_Manager, ecrInfo, "Launch_Manager");
                initECRAttrValue(Costing, ecrInfo, "Costing");
                initECRAttrValue(BUR, ecrInfo, "BUR");
                initECRAttrValue(APR, ecrInfo, "APR");
                StringList relSelectList = new StringList();
                relSelectList.add(SELECT_RELATIONSHIP_ID);
                relSelectList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_PROJECT_ROLE);
                StringList busSelectList = new StringList();
                busSelectList.add(SELECT_ID);
                busSelectList.add(SELECT_CURRENT);
                busSelectList.add(SELECT_OWNER);
                busSelectList.add(SELECT_ATTRIBUTE_TITLE);
                //整理 文件
                MapList files = ecr.getRelatedObjects(context, RELATIONSHIP_REFERENCE_DOCUMENT, // relationship pattern
                        TYPE_DOCUMENT,                                    // object pattern
                        busSelectList,                            // object selects
                        relSelectList, // relationship selects
                        false,                                        // to direction
                        true,                                        // from direction
                        (short) 1,                                    // recursion level
                        "",                // object where clause
                        "",
                        (short) 0);
                JF_LOGGER.info("files:{}", files);
                //ECR上传附件分组
                Map group = (Map) files.stream().filter(m -> {
                    Map file = (Map) m;
                    return UIUtil.isNotNullAndNotEmpty((String) file.get(JF_PLMConstants_mxJPO.SELECT_ATTR_PROJECT_ROLE));
                }).collect(Collectors.groupingBy(m -> {
                    Map file = (Map) m;
                    return file.get(JF_PLMConstants_mxJPO.SELECT_ATTR_PROJECT_ROLE);
                }));
                JF_LOGGER.info("group:{}", group);
                Set<String> fileKeySet = ROLE_MAPPING_MAP.keySet();
                for (String strKey : fileKeySet) {
                    MapList fileMapList = classFilesInECRFiles(context, group, strKey, strHostUrl, loginUserSignTaskRole);
                    if ("PRR".equals(strKey)) {
                        PRR.put("files", fileMapList);
                        initECRFormEditAccess(strKey, PRR, loginUserSignTaskRole);
                    } else if ("AME".equals(strKey)) {
                        AME.put("files", fileMapList);
                        initECRFormEditAccess(strKey, AME, loginUserSignTaskRole);
                        boolean isShow = checkHasExistTaskInProjectRole("AME", allSignTaskInECR);
                        AME.put("isShow",isShow);
                    } else if ("FoamAME".equals(strKey)) {
                        FoamAME.put("files", fileMapList);
                        initECRFormEditAccess(strKey, FoamAME, loginUserSignTaskRole);
                        boolean isShow = checkHasExistTaskInProjectRole("FoamAME", allSignTaskInECR);
                        FoamAME.put("isShow",isShow);
                    }else if ("TrimAME".equals(strKey)) {
                        TrimAME.put("files", fileMapList);
                        initECRFormEditAccess(strKey, TrimAME, loginUserSignTaskRole);
                        boolean isShow = checkHasExistTaskInProjectRole("TrimAME", allSignTaskInECR);
                        TrimAME.put("isShow",isShow);
                    }else if ("AQE".equals(strKey)) {
                        AQE.put("files", fileMapList);
                        initECRFormEditAccess(strKey, AQE, loginUserSignTaskRole);
                    } else if ("LOR".equals(strKey)) {
                        LOR.put("files", fileMapList);
                        initECRFormEditAccess(strKey, LOR, loginUserSignTaskRole);
                    } else if ("Launch_Manager".equals(strKey)) {
                        Launch_Manager.put("files", fileMapList);
                        initECRFormEditAccess(strKey, Launch_Manager, loginUserSignTaskRole);
                    } else if ("Costing".equals(strKey)) {
                        Costing.put("files", fileMapList);
                        initECRFormEditAccess(strKey, Costing, loginUserSignTaskRole);
                    } else if ("BUR".equals(strKey)) {
                        BUR.put("files", fileMapList);
                        initECRFormEditAccess(strKey, BUR, loginUserSignTaskRole);
                    } else if ("APR".equals(strKey)) {
                        APR.put("files", fileMapList);
                        initECRFormEditAccess(strKey, APR, loginUserSignTaskRole);
                    } else if ("ICO".equals(strKey)) {
                        ICO.put("files", new MapList());
                        initECRFormEditAccess(strKey, ICO, loginUserSignTaskRole);
                    }

                }
                Set roleSet = getRoleKeyListByMapList(loginUserSignTaskRole);
                res.put("login_role", roleSet);
                isCreateAMTSignTask = true;
            }
            //获取ECR底部按钮权限
            Map footerMenuAccess = getFooterMenuAccess(loginUserSignTaskRole, strLoginUser, strOwner, strCurrent, strProjectOwner);
            res.put("footerMenuAccess", footerMenuAccess);

            //构造获取采购件清单参数
            HashMap<String, String> stringStringHashMap = new HashMap<>();
            stringStringHashMap.put("objectId", strObjectId);
            stringStringHashMap.put("expandLevel", "0");
            String initargs[] = {};
            //获取自制件清单
            MapList makeTableList = (MapList) JPO.invoke(context, "JF_FormalECRService", initargs, "getFormalECRMakeTableData", JPO.packArgs (stringStringHashMap), MapList.class);
            Map ameEditFieldAccess = getAMEEditFieldAccess(isCreateAMTSignTask, makeTableList);
            res.put("AMEEditFiled", ameEditFieldAccess);
            res.put("signTaskList", allSignTaskInECR);
            res.put("PRR", PRR);
            res.put("AME", AME);
            res.put("FoamAME", FoamAME);
            res.put("TrimAME", TrimAME);
            res.put("AQE", AQE);
            res.put("LOR", LOR);
            res.put("Launch_Manager", Launch_Manager);
            res.put("Costing", Costing);
            res.put("BUR", BUR);
            res.put("APR", APR);
            res.put("ECR", ECR);
            res.put("ICO", ICO);
            res.put("code", "200");
        } catch (Exception e) {
            JF_LOGGER.info(e.getMessage());
            res.put("code", "404");
        } finally {
            ContextUtil.popContext(context);
        }
        JF_LOGGER.info("map:{}", res);
        Gson gson = new Gson();
        String strGson = gson.toJson(res);
        return strGson;
    }





    public static Map getAMEEditFieldAccess(boolean hasAMESignTask, MapList makeTableList) {
        Map AMEEditFiledMap = new HashMap<String, Boolean>();
        if (hasAMESignTask) {
            for (int i = 0; i < makeTableList.size(); i++) {
                Map makeMap = (Map) makeTableList.get(i);
                String strPartType = (String) makeMap.get(JF_PLMConstants_mxJPO.SELECT_ATTR_JFPartType);
                if (EDIT_AME_FORM_FIELD_LIST.contains(strPartType)) {
                    AMEEditFiledMap.put(strPartType, true);
                }
            }
        } else {
            for (int i = 0; i < EDIT_AME_FORM_FIELD_LIST.size(); i++) {
                String editField = EDIT_AME_FORM_FIELD_LIST.get(i);
                AMEEditFiledMap.put(editField, true);
            }
        }
        //防止有不存在零件类型
        for (int i = 0; i < EDIT_AME_FORM_FIELD_LIST.size(); i++) {
            String editField = EDIT_AME_FORM_FIELD_LIST.get(i);
            if (!AMEEditFiledMap.containsKey(editField)) {
                AMEEditFiledMap.put(editField, false);
            }
        }
        return AMEEditFiledMap;
    }
    /**
     * @param context
     * @param ecr
     * @return com.matrixone.apps.domain.util.MapList
     * @throws
     * @description 获取所有的会签任务
     * @author CHENYAN
     * @date 2024/8/16 10:32
     */
    public static MapList getAllSignTaskInECR(Context context, DomainObject ecr) throws Exception {
        StringList typeSelectList = new StringList();
        typeSelectList.add(DomainConstants.SELECT_ID);
        typeSelectList.add(DomainConstants.SELECT_CURRENT);
        typeSelectList.add(SELECT_ATTRIBUTE_TITLE);
        typeSelectList.add(SELECT_OWNER);
        typeSelectList.add("state[Review].actual");
        typeSelectList.add("state[Complete].actual");
        typeSelectList.add(SELECT_DESCRIPTION);
        StringList relSelectList = new StringList(SELECT_RELATIONSHIP_ID);
        relSelectList.add(JF_PLMConstants_mxJPO.SELECT_ATTRIBUTE_PROJECT_TYPE);
        Locale local = context.getLocale();
        //时区
        String strTimeZone = context.getTimezone();
        JF_LOGGER.info("strTimeZone:{}", strTimeZone);
        //获取到时区相对偏移量 -8 , 8
        double dTimeZoneId = convertOffsetToDouble(strTimeZone);
        JF_LOGGER.info("dTimeZoneId:{}", dTimeZoneId);
        MapList maps = new MapList();
        //只获取会签状态状态的会签任务，排除掉APR和商务经理
        maps = ecr.getRelatedObjects(context, JF_PLMConstants_mxJPO.RELATIONSHIP_JF_ECR_TASK, // relationship pattern
                JF_PLMConstants_mxJPO.TYPE_JS_SIGN_TASK,                                    // object pattern
                typeSelectList,                            // object selects
                relSelectList, // relationship selects
                false,                                        // to direction
                true,                                        // from direction
                (short) 1,                                    // recursion level
                "",                // object where clause
                "",
                (short) 0);
        for (int i = 0; i < maps.size(); i++) {
            Map signTaskMap = (Map) maps.get(i);
            //重构Map
            signTaskMap.put("title", (String) signTaskMap.get(SELECT_ATTRIBUTE_TITLE));
            //项目角色
            signTaskMap.put("projectRole", (String) signTaskMap.get(JF_PLMConstants_mxJPO.SELECT_ATTRIBUTE_PROJECT_TYPE));
            String strOwner = PersonUtil.getFullName(context, (String) signTaskMap.get(SELECT_OWNER));
            //设置全名
            if (UIUtil.isNotNullAndNotEmpty(strOwner)) {
                signTaskMap.put(SELECT_OWNER, strOwner);
            }
            String strCurrent = (String) signTaskMap.get(SELECT_CURRENT);
            String strNlsCurrent;
            if ("Review".equals(strCurrent)) {
                String strNls = EnoviaResourceBundle.getProperty(context, "emxFrameworkStringResource", context.getLocale(), "emxFramework.Policy.ECR.APR.Review");
                strNlsCurrent = strNls;
            } else {
                strNlsCurrent = EnoviaResourceBundle.getStateI18NString(context, "Project Task", strCurrent, context.getLocale().toString());
            }
            signTaskMap.put("currentNls", strNlsCurrent);
            signTaskMap.put("APRReviewNotes", UIUtil.getValue(signTaskMap, SELECT_DESCRIPTION));
            if ("Complete".equals(strCurrent) || "Review".equals(strCurrent)) {
                String strSubmitTime = UIUtil.getValue(signTaskMap, "state[Review].actual");
                String strCompleteTime = UIUtil.getValue(signTaskMap, "state[Complete].actual");
                strSubmitTime = eMatrixDateFormat.getFormattedDisplayDateTime(strSubmitTime, true, 2, dTimeZoneId, local);
                strCompleteTime = eMatrixDateFormat.getFormattedDisplayDateTime(strCompleteTime, true, 2, dTimeZoneId, local);
                //时间戳
                signTaskMap.put("submitTime", strSubmitTime);
                signTaskMap.put("completeTime", strCompleteTime);
            } else {
                signTaskMap.put("submitTime", "");
                signTaskMap.put("completeTime", "");
            }
        }
        return maps;
    }
    /**
     * @param strTimeZone strTimeZone Asia/Shanghai
     * @return double 偏移量作为 double 类型的值（小时） 东八区为 -8 西八区为 +8
     * @throws
     * @description 将时区偏移量字符串转换为 double 类型的偏移量（小时为单位
     * @author CHENYAN
     * @date 2024/12/24 19:33
     */
    public static double convertOffsetToDouble(String strTimeZone) {
        double dRes;
        try {
            ZoneOffset offset = ZoneId.of(strTimeZone).getRules().getOffset(Instant.now());
            dRes = -(offset.getTotalSeconds() / 3600.0);
        } catch (Exception e) {
            JF_LOGGER.error("时区转换异常 返回 默认值 0 :{}", e.getMessage());
            dRes = 0;
        }
        return dRes;
    }
    
    /**
     * @param roleList
     * @return boolean
     * @throws
     * @description 获取底部按钮的权限
     * @author CHENYAN
     * @date 2024/8/19 13:39
     */
    public Map getFooterMenuAccess(MapList roleList, String strLoginUser, String strOwner, String strCurrent, String strProjectOwner) {
        //
        Map res = new HashMap();
        res.put("footerShow", false);
        res.put("submit", false);
        res.put("save", false);
        res.put("cancel", false);
        //表单权限
        if (roleList.size() > 0) {
            JF_LOGGER.info("roleList:{}", roleList);
            for (int i = 0; i < roleList.size(); i++) {
                Map role = (Map) roleList.get(i);
                String strSignTaskCurrent = (String) role.get(SELECT_CURRENT);
                if (!("Complete".equals(strSignTaskCurrent) || "Review".equals(strSignTaskCurrent))) {
                    res.put("footerShow", true);
                    res.put("submit", true);
                    res.put("save", true);
                    res.put("cancel", true);
                    break;
                }
            }
        }
        //ECR的状态优先级高 (可能会存在会签任务的指派人创建ECR)
        if ("Create".equals(strCurrent)) {
            if (strLoginUser.equals(strOwner)) {
                res.put("footerShow", true);
                res.put("submit", false);
                res.put("save", true);
                res.put("cancel", true);
            }
        } else if ("Review".equals(strCurrent)) {
            if (strLoginUser.equals(strProjectOwner)) {
                res.put("footerShow", true);
                res.put("submit", false);
                res.put("save", true);
                res.put("cancel", true);
            }
        }
        return res;
    }
    /**
     * @param roleList signTask 集合
     * @return java.util.Set
     * @throws
     * @description 获取到当前登录人的会签角色
     * @author CHENYAN
     * @date 2024/8/7 10:58
     */
    public static Set getRoleKeyListByMapList(MapList roleList) {
        Set<String> res = new HashSet();
        for (int i = 0; i < roleList.size(); i++) {
            Map roleMap = (Map) roleList.get(i);
            String strRoleName = (String) roleMap.get("role");
            JF_LOGGER.info("strRoleName:{}", strRoleName);
            for (Map.Entry<String, String> entry : ROLE_MAPPING_MAP.entrySet()) {
                if (entry.getValue().equals(strRoleName)) {
                    res.add(entry.getKey());
                }
            }
        }
        return res;
    }
    /**
     * @param targetMap 初始化map
     * @param sourceMap 元数据map
     * @param strType   表单类型
     * @return void
     * @throws
     * @description 初始化ECR的属性
     * @author CHENYAN
     * @date 2024/8/6 17:29
     */
    public static void initECRAttrValue(Map targetMap, Map sourceMap, String strType) {
        switch (strType) {
            case "PRR": {
                if (null != sourceMap) {
                    targetMap.put("JFChangeCyclesOutsourcing", UIUtil.getValue(sourceMap, JF_PLMConstants_mxJPO.SELECT_ATTR_JFChangeCyclesOutsourcing));
                    targetMap.put("JFChangeDevelopmentCostsOutsourcing", UIUtil.getValue(sourceMap, JF_PLMConstants_mxJPO.SELECT_ATTR_JFChangeDevelopmentCostsOutsourcing));
                    targetMap.put("JFSupplierOrScrapDepotOutsourcing", UIUtil.getValue(sourceMap, JF_PLMConstants_mxJPO.SELECT_ATTR_JFSupplierOrScrapDepotOutsourcing));
                    targetMap.put("JFChangeCyclesSelfMake", UIUtil.getValue(sourceMap, JF_PLMConstants_mxJPO.SELECT_ATTR_JFChangeCyclesSelfMake));
                    targetMap.put("JFChangeDevelopmentCostsSelfMake", UIUtil.getValue(sourceMap, JF_PLMConstants_mxJPO.SELECT_ATTR_JFChangeDevelopmentCostsSelfMake));
                    targetMap.put("JFSupplierOrScrapDepotSelfMake", UIUtil.getValue(sourceMap, JF_PLMConstants_mxJPO.SELECT_ATTR_JFSupplierOrScrapDepotSelfMake));
                } else {
                    targetMap.put("JFChangeCyclesOutsourcing", "");
                    targetMap.put("JFChangeDevelopmentCostsOutsourcing", "");
                    targetMap.put("JFSupplierOrScrapDepotOutsourcing", "");
                    targetMap.put("JFChangeCyclesSelfMake", "");
                    targetMap.put("JFChangeDevelopmentCostsSelfMake", "");
                    targetMap.put("JFSupplierOrScrapDepotSelfMake", "");
                }
                break;
            }
            case "AME": {
                if (null != sourceMap) {
                    targetMap.put("JFChangesInvestmentsWholeChair", UIUtil.getValue(sourceMap, JF_PLMConstants_mxJPO.SELECT_ATTR_JFChangesInvestmentsWholeChair));
                    targetMap.put("JFChangesInvestmentsWholeChairExternal", sourceMap.get(JF_PLMConstants_mxJPO.SELECT_ATTR_JFChangesInvestmentsWholeChairExternal));
                    targetMap.put("JFAMENotes", sourceMap.get(JF_PLMConstants_mxJPO.SELECT_ATTRIBUTE_JFAMENotes));
                } else {
                    targetMap.put("JFChangesInvestmentsWholeChair", "");
                    targetMap.put("JFChangesInvestmentsWholeChairExternal", "");
                    targetMap.put("JFAMENotes", "");
                }
                break;
            }
            case "FoamAME": {
                if (null != sourceMap) {
                    targetMap.put("JFChangesInvestmentsFoaming", UIUtil.getValue(sourceMap, JF_PLMConstants_mxJPO.SELECT_ATTR_JFChangesInvestmentsFoaming));
                    targetMap.put("JFChangesInvestmentsFoamingExternal", sourceMap.get(JF_PLMConstants_mxJPO.SELECT_ATTR_JFChangesInvestmentsFoamingExternal));
                    targetMap.put("JFFoamAMENotes", sourceMap.get(JF_PLMConstants_mxJPO.SELECT_ATTRIBUTE_JFFoamAMENotes));
                } else {
                    targetMap.put("JFChangesInvestmentsFoaming", "");
                    targetMap.put("JFChangesInvestmentsFoamingExternal", "");
                    targetMap.put("JFFoamAMENotes", "");
                }
                break;
            }
            case "TrimAME": {
                if (null != sourceMap) {
                    targetMap.put("JFChangesInvestmentsFaceCovers", UIUtil.getValue(sourceMap, JF_PLMConstants_mxJPO.SELECT_ATTR_JFChangesInvestmentsFaceCovers));
                    targetMap.put("JFChangesInvestmentsFaceCoversExternal", sourceMap.get(JF_PLMConstants_mxJPO.SELECT_ATTR_JFChangesInvestmentsFaceCoversExternal));
                    targetMap.put("JFTrimAMENotes", sourceMap.get(JF_PLMConstants_mxJPO.SELECT_ATTRIBUTE_JFTrimAMENotes));
                } else {
                    targetMap.put("JFChangesInvestmentsFaceCovers", "");
                    targetMap.put("JFChangesInvestmentsFaceCoversExternal", "");
                    targetMap.put("JFTrimAMENotes", "");
                }
                break;
            }
            case "AQE": {
                if (null != sourceMap) {
                    targetMap.put("JFChangesFixtures", UIUtil.getValue(sourceMap, JF_PLMConstants_mxJPO.SELECT_ATTR_JFChangesFixtures));
                    targetMap.put("JFCostOfQuality", UIUtil.getValue(sourceMap, JF_PLMConstants_mxJPO.SELECT_ATTR_JFCostOfQuality));
                    targetMap.put("JFCostOfTrial", UIUtil.getValue(sourceMap, JF_PLMConstants_mxJPO.SELECT_ATTR_JFCostOfTrial));
                    targetMap.put("JFChangesFixturesExternal", UIUtil.getValue(sourceMap, JF_PLMConstants_mxJPO.SELECT_ATTR_JFChangesFixturesExternal));
                    targetMap.put("JFCostOfQualityExternal", UIUtil.getValue(sourceMap, JF_PLMConstants_mxJPO.SELECT_ATTR_JFCostOfQualityExternal));
                    targetMap.put("JFCostOfTrialExternal", UIUtil.getValue(sourceMap, JF_PLMConstants_mxJPO.SELECT_ATTR_JFCostOfTrialExternal));
                } else {
                    targetMap.put("JFChangesFixtures", "");
                    targetMap.put("JFCostOfQuality", "");
                    targetMap.put("JFCostOfTrial", "");
                    targetMap.put("JFChangesFixturesExternal", "");
                    targetMap.put("JFCostOfQualityExternal", "");
                    targetMap.put("JFCostOfTrialExternal", "");
                }
                break;
            }
            case "LOR": {
                if (null != sourceMap) {
                    targetMap.put("JFCostPackagingLogistics", UIUtil.getValue(sourceMap, JF_PLMConstants_mxJPO.SELECT_ATTR_JFCostPackagingLogistics));
                    targetMap.put("JFCostPackagingLogisticsExternal", UIUtil.getValue(sourceMap, JF_PLMConstants_mxJPO.SELECT_ATTR_JFCostPackagingLogisticsExternal));
                } else {
                    targetMap.put("JFCostPackagingLogistics", "");
                    targetMap.put("JFCostPackagingLogisticsExternal", "");
                }
                break;
            }
            case "Launch_Manager": {
                if (null != sourceMap) {
                    targetMap.put("JFMaterialsFinishedProductsRework", UIUtil.getValue(sourceMap, JF_PLMConstants_mxJPO.SELECT_ATTR_JFMaterialsFinishedProductsRework));
                } else {
                    targetMap.put("JFMaterialsFinishedProductsRework", "");
                }
                break;
            }
            case "Costing": {
                if (null != sourceMap) {
                    targetMap.put("JFChangesUnitPriceCost", UIUtil.getValue(sourceMap, JF_PLMConstants_mxJPO.SELECT_ATTR_JFChangeCyclesOutsourcing));
                    targetMap.put("JFChangesMouldCost", UIUtil.getValue(sourceMap, JF_PLMConstants_mxJPO.SELECT_ATTR_JFChangesMouldCost));
                    targetMap.put("JFChangesUnitPriceCostExternal", UIUtil.getValue(sourceMap, JF_PLMConstants_mxJPO.SELECT_ATTR_JFChangesUnitPriceCostExternal));
                    targetMap.put("JFChangesMouldCostExternal", UIUtil.getValue(sourceMap, JF_PLMConstants_mxJPO.SELECT_ATTR_JFChangesMouldCostExternal));
                } else {
                    targetMap.put("JFChangesUnitPriceCost", "");
                    targetMap.put("JFChangesMouldCost", "");
                    targetMap.put("JFChangesUnitPriceCostExternal", "");
                    targetMap.put("JFChangesMouldCostExternal", "");
                }
                break;
            }
            case "BUR": {
                if (null != sourceMap) {
                    targetMap.put("JFAcknowledgmentContentAmount", UIUtil.getValue(sourceMap, JF_PLMConstants_mxJPO.SELECT_ATTR_JFAcknowledgmentContentAmount));
                } else {
                    targetMap.put("JFAcknowledgmentContentAmount", "");
                }
                break;
            }
            case "APR": {
                if (null != sourceMap) {
                    targetMap.put("JFChangesInvestment", UIUtil.getValue(sourceMap, JF_PLMConstants_mxJPO.SELECT_ATTR_JFChangesInvestment));
                    targetMap.put("JFChangesDevelopment", UIUtil.getValue(sourceMap, JF_PLMConstants_mxJPO.SELECT_ATTR_JFChangesDevelopment));
                    targetMap.put("JFChangesMould", UIUtil.getValue(sourceMap, JF_PLMConstants_mxJPO.SELECT_ATTR_JFChangesMould));
                    targetMap.put("JFSumInventoryScrapAmount", UIUtil.getValue(sourceMap, JF_PLMConstants_mxJPO.SELECT_ATTR_JFSumInventoryScrapAmount));
                    targetMap.put("JFChangesInvestmentExternal", UIUtil.getValue(sourceMap, JF_PLMConstants_mxJPO.SELECT_ATTR_JFChangesInvestmentExternal));
                    targetMap.put("JFChangesDevelopmentExternal", UIUtil.getValue(sourceMap, JF_PLMConstants_mxJPO.SELECT_ATTR_JFChangesDevelopmentExternal));
                    targetMap.put("JFChangesMouldExternal", UIUtil.getValue(sourceMap, JF_PLMConstants_mxJPO.SELECT_ATTR_JFChangesMouldExternal));
                    targetMap.put("JFSumInventoryScrapAmountExternal", UIUtil.getValue(sourceMap, JF_PLMConstants_mxJPO.SELECT_ATTR_JFSumInventoryScrapAmountExternal));
                    targetMap.put("JFTestFee", UIUtil.getValue(sourceMap, JF_PLMConstants_mxJPO.SELECT_ATTR_JFTestFee));
                    targetMap.put("JFAPRNotes", UIUtil.getValue(sourceMap, JF_PLMConstants_mxJPO.SELECT_ATTR_JFAPRNotes));
                    targetMap.put("JFOtherFee", UIUtil.getValue(sourceMap, JF_PLMConstants_mxJPO.SELECT_ATTR_JFOtherFee));

                } else {
                    targetMap.put("JFChangesInvestment", "");
                    targetMap.put("JFChangesDevelopment", "");
                    targetMap.put("JFChangesMould", "");
                    targetMap.put("JFSumInventoryScrapAmount", "");
                    targetMap.put("JFChangesInvestmentExternal", "");
                    targetMap.put("JFChangesDevelopmentExternal", "");
                    targetMap.put("JFChangesMouldExternal", "");
                    targetMap.put("JFSumInventoryScrapAmountExternal", "");
                    targetMap.put("JFTestFee", "");
                    targetMap.put("JFAPRNotes", "");
                    targetMap.put("JFOtherFee", "");
                }
                break;
            }
            default:{
                break;
            }
        }
    }
    /**
     * @param strType         表单类型
     * @param formMap         表单map
     * @param currentRoleList 当前用户的对象角色会签任务
     * @return void
     * @throws
     * @description 初始化表单权限
     * @author CHENYAN
     * @date 2024/8/5 9:13
     */
    public void initECRFormEditAccess(String strType, Map formMap, MapList currentRoleList) {
        if (ROLE_MAPPING_MAP.containsKey(strType)) {
            String strRoleValue = ROLE_MAPPING_MAP.get(strType);
            if (null == currentRoleList || currentRoleList.size() == 0) {
                formMap.put("editAccess", Boolean.FALSE);
                return;
            }
            boolean isExecCustomer = false;
            for (int i = 0; i < currentRoleList.size(); i++) {
                Map signTaskMap = (Map) currentRoleList.get(i);
                String strRole = (String) signTaskMap.get("role");
                String strTaskId = (String) signTaskMap.get("signTaskId");
                String strTaskCurrent = (String) signTaskMap.get(SELECT_CURRENT);
                String strTaskType = (String) signTaskMap.get(SELECT_TYPE);
                //商务经理单独处理 存在会签任务和报价任务都是商务经理
                if (ROLE_MAPPING_MAP.get("BUR").equals(strRole) && strRoleValue.equals(strRole)) {
                    JF_LOGGER.info("isExecCustomer:{}", isExecCustomer);
                    if (!isExecCustomer) {
                        // 标识客户报价权限已经处理 APR报价任务处理后直接退出
                        if (JF_PLMConstants_mxJPO.TYPE_JF_CustomerTask.equals(strTaskType)) {
                            isExecCustomer = true;
                            if ((!("Complete".equals(strTaskCurrent) || "Review".equals(strTaskCurrent)))) {
                                formMap.put("editAccess", Boolean.TRUE);
                                JF_LOGGER.info("strTaskId:{}", strTaskId);
                                JF_LOGGER.info("strTaskCurrent:{}", strTaskCurrent);
                                formMap.put("signTaskId", strTaskId);
                                formMap.put("signTaskCurrent", strTaskCurrent);
                            } else {
                                formMap.put("editAccess", Boolean.FALSE);
                            }
                            JF_LOGGER.info("formMap:{}", formMap);
                            break;
                        } else {
                            formMap.put("editAccess", Boolean.FALSE);
                            formMap.put("signTaskId", strTaskId);
                            formMap.put("signTaskCurrent", strTaskCurrent);
                        }

                    }
                } else {
                    if (strRoleValue.equals(strRole) && (!("Complete".equals(strTaskCurrent) || "Review".equals(strTaskCurrent)))) {
                        formMap.put("editAccess", Boolean.TRUE);
                        JF_LOGGER.info("strTaskId:{}", strTaskId);
                        JF_LOGGER.info("strTaskCurrent:{}", strTaskCurrent);
                        formMap.put("signTaskId", strTaskId);
                        formMap.put("signTaskCurrent", strTaskCurrent);
                    } else {
                        formMap.put("editAccess", Boolean.FALSE);
                    }
                }

            }
        }
    }
    /**
     * @param context
     * @param groupFiles      分钟后的权限
     * @param strType         ECR 表单类型 PPR BUR 等
     * @param strHostUrl      请求基地址
     * @param currentRoleList 用户当前会签任务角色
     * @return com.matrixone.apps.domain.util.MapList
     * @throws
     * @description 根据 ECR附件，和当前登录人角色 分类文件供前端显示和操作
     * @author CHENYAN
     * @date 2024/8/4 14:48
     */
    public MapList classFilesInECRFiles(Context context, Map groupFiles, String strType, String strHostUrl, MapList currentRoleList) {
        MapList res = new MapList();
        if (ROLE_MAPPING_MAP.containsKey(strType)) {
            String strRoleValue = ROLE_MAPPING_MAP.get(strType);
            if (groupFiles.containsKey(strType)) {
                List files = (List) groupFiles.get(strType);
                if (null != files && files.size() > 0) {
                    //找到当前角色的文件
                    //1.判断当前文件状态是否之前删除
                    for (int i = 0; i < files.size(); i++) {
                        //返回Map
                        HashMap resMap = new HashMap<>();
                        Map file = (Map) files.get(i);
                        String strCurrent = (String) file.get(SELECT_CURRENT);
                        //前端文档操作权限
                        Boolean fileActionAccess = Boolean.TRUE;
                        if (checkRoleContain(strRoleValue, currentRoleList)) {
                            //发布和冻结文档不可操作
                            if ("RELEASED".equals(strCurrent) || "OBSOLETE".equals(strCurrent)) {
                                fileActionAccess = Boolean.FALSE;
                            }
                        }
                        resMap.put("delAccess", fileActionAccess);
                        resMap.put("objectId", file.get(SELECT_ID));
                        resMap.put("relId", file.get(SELECT_RELATIONSHIP_ID));
                        resMap.put("current", file.get(SELECT_CURRENT));
                        resMap.put("title", StringEscapeUtils.escapeHtml4((String) file.get(SELECT_ATTRIBUTE_TITLE)));
                        StringBuffer sbHref = new StringBuffer();
                        sbHref.append(strHostUrl);
                        sbHref.append("/3dspace/common/emxTree.jsp?mode=insert&relId=null&parentOID=null&jsTreeID=null&objectId=");
                        sbHref.append(file.get(SELECT_ID));
                        sbHref.append("&suiteKey=Components&emxSuiteDirectory=components");
                        resMap.put("href", sbHref.toString());
                        res.add(resMap);
                    }
                }
            }
        }
        return res;
    }
    public static boolean checkRoleContain(String strRoleValue, MapList currentRoleList) {
        if (null != currentRoleList && currentRoleList.size() > 0) {
            for (int i = 0; i < currentRoleList.size(); i++) {
                Map currentRole = (Map) currentRoleList.get(i);
                String strRole = (String) currentRole.get("role");
                return strRole.equals(strRoleValue);
            }
        }
        return false;
    }
    /**
     * @param context
     * @param args
     * @return java.lang.String
     * @throws
     * @description APR状态 同意或者拒绝 会签任务
     * @author CHENYAN
     * @date 2024/8/7 10:19
     */
    public String APRReviewSignTask(Context context, String[] args) throws Exception {
        JF_LOGGER.info("----------------------------- APRReviewSignTask begin ------------------------------------");
        HashMap res = new HashMap<>();
        Map reqMap = (Map) JPO.unpackArgs(args);
        List signTaskIdList = (List) reqMap.get("signTaskIds");
        String strAction = (String) reqMap.get("action");
        String strObjectId = (String) reqMap.get("objectId");
        String strNotes = (String) reqMap.get("Notes");
        JF_LOGGER.info("strObjectId:{}", strObjectId);
        JF_LOGGER.info("strAction:{}", strAction);
        JF_LOGGER.info("Notes:{}", strNotes);
        JF_LOGGER.info("strSignTaskId:{}", signTaskIdList);
        DomainObject ecr = DomainObject.newInstance(context, strObjectId);
        try {
            ContextUtil.pushContext(context);
            ContextUtil.startTransaction(context, true);
            for (int i = 0; i < signTaskIdList.size(); i++) {
                String strSignTaskId = (String) signTaskIdList.get(i);
                DomainObject signTask = DomainObject.newInstance(context, strSignTaskId);
                signTask.setDescription(context, strNotes);
                if ("reject".equals(strAction)) {
                    signTask.demote(context);
                    signTask.setState(context,"Active");
                    // add by chenyan 2025/08/29 新增拒绝通知
                    JF_SignTask_mxJPO.projectTaskRejectSendEmail(context,new  String[]{strSignTaskId});
                } else if ("approve".equals(strAction)) {
//                    signTask.promote(context);
                    signTask.setState(context,"Complete");
                }
            }
            ContextUtil.commitTransaction(context);
            //获取最新的会签任务
            MapList allSignTaskInECR = getAllSignTaskInECR(context, ecr);
            res.put("signTaskList", allSignTaskInECR);
            res.put("code", "200");

        } catch (Exception e) {
            JF_LOGGER.info(e.getMessage());
            ContextUtil.abortTransaction(context);
        } finally {
            ContextUtil.popContext(context);
        }
        Gson gson = new Gson();
        return gson.toJson(res);
    }
    /**
     * @param context
     * @param args
     * @return java.lang.String
     * @throws
     * @description ECR表单保存或者提交按钮
     * @author CHENYAN
     * @date 2024/8/6 17:31
     */
    public String saveOrSubmitInECR(Context context, String[] args) throws Exception {
        JF_LOGGER.info("----------------------------- saveOrSubmitInECR begin ------------------------------------");
        HashMap<Object, Object> res = new HashMap<>();
        Map reqMap = (Map) JPO.unpackArgs(args);
        String strAction = (String) reqMap.get("action");
        String strObjectId = (String) reqMap.get("objectId");
        JF_LOGGER.info("strObjectId:{}", strObjectId);
        JF_LOGGER.info("strAction:{}", strAction);
        HashMap attrMap = new HashMap();
        Set<String> signTaskSet = new HashSet<>();
        //复制请求中ECR属性
        copyECRAttribute(attrMap, reqMap, signTaskSet);
        JF_LOGGER.info("attrMap:{}", attrMap);
        if ("submit".equals(strAction)) {
            try {
                ContextUtil.pushContext(context);
                DomainObject ecr = DomainObject.newInstance(context, strObjectId);
                String strCurrent = ecr.getInfo(context, SELECT_CURRENT);
                ContextUtil.startTransaction(context, true);
                ecr.setAttributeValues(context, attrMap);
                //提升状态到审核中
                for (String strSignTaskId : signTaskSet) {
                    DomainObject signTask = DomainObject.newInstance(context, strSignTaskId);
                    StringList signTaskList = new StringList();
                    signTaskList.add(SELECT_TYPE);
                    signTaskList.add(SELECT_CURRENT);
                    Map signTaskInfo = signTask.getInfo(context, signTaskList);
                    String strTaskCurrent = (String) signTaskInfo.get(SELECT_CURRENT);
                    String strType = (String) signTaskInfo.get(SELECT_TYPE);
                    // APR 任务和 客户报价任务提交直接已完成  会签任务变成审核中
                    if (!("Complete".equals(strTaskCurrent) || "Review".equals(strTaskCurrent))) {
                        if (JF_PLMConstants_mxJPO.TYPE_JF_APRTask.equals(strType) || JF_PLMConstants_mxJPO.TYPE_JF_CustomerTask.equals(strType)) {
                            signTask.setState(context, "Complete");
                        } else {
                            signTask.setState(context, "Review");
                        }
                    }
                }
                //判断会签任务是不是已经全部到审核中
                boolean stateIsAllReview = checkSignTaskIsAllReview(context, strObjectId);
                //只有会签状态才提升
                if (stateIsAllReview && "Countersign".equals(strCurrent)) {
                    // 提升ECR的状态
//                    ecr.promote(context);
                    ecr.setState(context,"APR");
                } else if ("APR".equals(strCurrent) || "Quotation".equals(strCurrent)) {
                    boolean stateIsAllComplete = checkSignTaskIsAllComplete(context, strObjectId);
                    // 提升ECR的状态
                    if (stateIsAllComplete) {
                        ecr.promote(context);
                    }
                }
                ContextUtil.commitTransaction(context);
                JF_LOGGER.info("state :{}", ecr.getInfo(context, SELECT_CURRENT));
                //只要是提交就需要刷新
                res.put("isRefresh", true);
                res.put("code", "200");
            } catch (Exception e) {
                res.put("code", "404");
                ContextUtil.abortTransaction(context);
                JF_LOGGER.info(e.getMessage());
            } finally {
                ContextUtil.popContext(context);
            }
        } else if ("save".equals(strAction)) {
            try {
                ContextUtil.pushContext(context);
                DomainObject ecr = DomainObject.newInstance(context, strObjectId);
                ContextUtil.startTransaction(context, true);
                ecr.setAttributeValues(context, attrMap);
                ContextUtil.commitTransaction(context);
                res.put("code", "200");
                res.put("isRefresh", false);
            } catch (Exception e) {
                ContextUtil.abortTransaction(context);
                res.put("code", "404");
                JF_LOGGER.info(e.getMessage());
            } finally {
                ContextUtil.popContext(context);
            }
        }
        Gson gson = new Gson();
        JF_LOGGER.info("----------------------------- saveOrSubmitInECR end ------------------------------------");
        return gson.toJson(res);
    }
    public static boolean checkSignTaskIsAllComplete(Context context, String strECRId) throws Exception {
        DomainObject ecr = DomainObject.newInstance(context, strECRId);
        StringList typeSelectList = new StringList();
        typeSelectList.add(DomainConstants.SELECT_ID);
        typeSelectList.add(DomainConstants.SELECT_CURRENT);
        typeSelectList.add(SELECT_ATTRIBUTE_TITLE);
        typeSelectList.add(SELECT_OWNER);
        StringList relSelectList = new StringList();
        relSelectList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_PROJECT_ROLE);
        MapList maps = new MapList();
        maps = ecr.getRelatedObjects(context, JF_PLMConstants_mxJPO.RELATIONSHIP_JF_ECR_TASK, // relationship pattern
                JF_PLMConstants_mxJPO.TYPE_JS_SIGN_TASK,                                    // object pattern
                typeSelectList,                            // object selects
                relSelectList, // relationship selects
                false,                                        // to direction
                true,                                        // from direction
                (short) 1,                                    // recursion level
                "!(current== Complete)",                // object where clause
                "",
                (short) 0);
        return maps.size() > 0 ? false : true;
    }
    public static boolean checkSignTaskIsAllReview(Context context, String strECRId) throws Exception {
        DomainObject ecr = DomainObject.newInstance(context, strECRId);
        StringList typeSelectList = new StringList();
        typeSelectList.add(DomainConstants.SELECT_ID);
        typeSelectList.add(DomainConstants.SELECT_CURRENT);
        typeSelectList.add(SELECT_ATTRIBUTE_TITLE);
        typeSelectList.add(SELECT_OWNER);
        StringList relSelectList = new StringList();
        relSelectList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_PROJECT_ROLE);
        MapList maps = new MapList();
        maps = ecr.getRelatedObjects(context, JF_PLMConstants_mxJPO.RELATIONSHIP_JF_ECR_TASK, // relationship pattern
                JF_PLMConstants_mxJPO.TYPE_JS_SIGN_TASK,                                    // object pattern
                typeSelectList,                            // object selects
                relSelectList, // relationship selects
                false,                                        // to direction
                true,                                        // from direction
                (short) 1,                                    // recursion level
                "!(current==Review || current== Complete)",                // object where clause
                "",
                (short) 0);
        return maps.size() > 0 ? false : true;
    }
    /**
     * @param targetMap 复制目标
     * @param resMap    请求修改参数
     * @return void
     * @throws
     * @description 复制请求中的ECR属性到新的Map中
     * @author CHENYAN
     * @date 2024/8/6 18:29
     */
    public static void copyECRAttribute(Map targetMap, Map resMap, Set<String> signTaskIdSet) {
        Set<String> keySet = ROLE_MAPPING_MAP.keySet();

        for (String strKey : keySet) {
            if (resMap.containsKey(strKey)) {
                Map formMap = (Map) resMap.get(strKey);
                //获取表单对呀的会签任务Id
                String strSignTaskId = UIUtil.getValue(formMap, "signTaskId");
                if (UIUtil.isNotNullAndNotEmpty(strSignTaskId)) {
                    signTaskIdSet.add(strSignTaskId);
                }
                switch (strKey) {
                    case "PRR": {
                        String strJFChangeCyclesOutsourcing = UIUtil.getValue(formMap, "JFChangeCyclesOutsourcing");
                        String strJFChangeDevelopmentCostsOutsourcing = UIUtil.getValue(formMap, "JFChangeDevelopmentCostsOutsourcing");
                        String strJFSupplierOrScrapDepotOutsourcing = UIUtil.getValue(formMap, "JFSupplierOrScrapDepotOutsourcing");
                        String strJFChangeCyclesSelfMake = UIUtil.getValue(formMap, "JFChangeCyclesSelfMake");
                        String strJFChangeDevelopmentCostsSelfMake = UIUtil.getValue(formMap, "JFChangeDevelopmentCostsSelfMake");
                        String strJFSupplierOrScrapDepotSelfMake = UIUtil.getValue(formMap, "JFSupplierOrScrapDepotSelfMake");
                        targetMap.put("JFChangeCyclesOutsourcing", strJFChangeCyclesOutsourcing);
                        targetMap.put("JFChangeDevelopmentCostsOutsourcing", strJFChangeDevelopmentCostsOutsourcing);
                        targetMap.put("JFSupplierOrScrapDepotOutsourcing", strJFSupplierOrScrapDepotOutsourcing);
                        targetMap.put("JFChangeCyclesSelfMake", strJFChangeCyclesSelfMake);
                        targetMap.put("JFChangeDevelopmentCostsSelfMake", strJFChangeDevelopmentCostsSelfMake);
                        targetMap.put("JFSupplierOrScrapDepotSelfMake", strJFSupplierOrScrapDepotSelfMake);
                        break;
                    }
                    case "AME": {
                        String strJFChangesInvestmentsWholeChair = UIUtil.getValue(formMap, "JFChangesInvestmentsWholeChair");
                        String strJFChangesInvestmentsWholeChairExternal = UIUtil.getValue(formMap, "JFChangesInvestmentsWholeChairExternal");
                        String strJFAMENotes = UIUtil.getValue(formMap, "JFAMENotes");
                        targetMap.put("JFChangesInvestmentsWholeChair", strJFChangesInvestmentsWholeChair);
                        targetMap.put("JFChangesInvestmentsWholeChairExternal", strJFChangesInvestmentsWholeChairExternal);
                        targetMap.put("JFAMENotes", strJFAMENotes);
                        break;
                    }
                    case "FoamAME": {
                        String strJFChangesInvestmentsFoaming = UIUtil.getValue(formMap, "JFChangesInvestmentsFoaming");
                        String strJFChangesInvestmentsFoamingExternal = UIUtil.getValue(formMap, "JFChangesInvestmentsFoamingExternal");
                        String strJFFoamAMENotes = UIUtil.getValue(formMap, "JFFoamAMENotes");
                        targetMap.put("JFChangesInvestmentsFoaming", strJFChangesInvestmentsFoaming);
                        targetMap.put("JFChangesInvestmentsFoamingExternal", strJFChangesInvestmentsFoamingExternal);
                        targetMap.put("JFFoamAMENotes", strJFFoamAMENotes);
                        break;
                    }
                    case "TrimAME": {
                        String strJFChangesInvestmentsFaceCovers = UIUtil.getValue(formMap, "JFChangesInvestmentsFaceCovers");
                        String strJFChangesInvestmentsFaceCoversExternal = UIUtil.getValue(formMap, "JFChangesInvestmentsFaceCoversExternal");
                        String strJFTrimAMENotes = UIUtil.getValue(formMap, "JFTrimAMENotes");
                        targetMap.put("JFChangesInvestmentsFaceCovers", strJFChangesInvestmentsFaceCovers);
                        targetMap.put("JFChangesInvestmentsFaceCoversExternal", strJFChangesInvestmentsFaceCoversExternal);
                        targetMap.put("JFTrimAMENotes", strJFTrimAMENotes);
                        break;
                    }
                    case "AQE": {
                        String strJFChangesFixtures = UIUtil.getValue(formMap, "JFChangesFixtures");
                        String strJFCostOfQuality = UIUtil.getValue(formMap, "JFCostOfQuality");
                        String strJFCostOfTrial = UIUtil.getValue(formMap, "JFCostOfTrial");
                        String strJFChangesFixturesExternal = UIUtil.getValue(formMap, "JFChangesFixturesExternal");
                        String strJFCostOfQualityExternal = UIUtil.getValue(formMap, "JFCostOfQualityExternal");
                        String strJFCostOfTrialExternal = UIUtil.getValue(formMap, "JFCostOfTrialExternal");
                        targetMap.put("JFChangesFixtures", strJFChangesFixtures);
                        targetMap.put("JFCostOfQuality", strJFCostOfQuality);
                        targetMap.put("JFCostOfTrial", strJFCostOfTrial);
                        targetMap.put("JFChangesFixturesExternal", strJFChangesFixturesExternal);
                        targetMap.put("JFCostOfQualityExternal", strJFCostOfQualityExternal);
                        targetMap.put("JFCostOfTrialExternal", strJFCostOfTrialExternal);
                        break;
                    }
                    case "LOR": {
                        String strJFCostPackagingLogistics = UIUtil.getValue(formMap, "JFCostPackagingLogistics");
                        String strJFCostPackagingLogisticsExternal = UIUtil.getValue(formMap, "JFCostPackagingLogisticsExternal");
                        targetMap.put("JFCostPackagingLogistics", strJFCostPackagingLogistics);
                        targetMap.put("JFCostPackagingLogisticsExternal", strJFCostPackagingLogisticsExternal);

                        break;
                    }
                    case "Launch_Manager": {
                        String strJFMaterialsFinishedProductsRework = UIUtil.getValue(formMap, "JFMaterialsFinishedProductsRework");
                        targetMap.put("JFMaterialsFinishedProductsRework", strJFMaterialsFinishedProductsRework);
                        break;
                    }
                    case "Costing": {
                        String strJFChangesUnitPriceCost = UIUtil.getValue(formMap, "JFChangesUnitPriceCost");
                        String strJFChangesMouldCost = UIUtil.getValue(formMap, "JFChangesMouldCost");
                        String strJFChangesUnitPriceCostExternal = UIUtil.getValue(formMap, "JFChangesUnitPriceCostExternal");
                        String strJFChangesMouldCostExternal = UIUtil.getValue(formMap, "JFChangesMouldCostExternal");
                        targetMap.put("JFChangesUnitPriceCost", strJFChangesUnitPriceCost);
                        targetMap.put("JFChangesMouldCost", strJFChangesMouldCost);
                        targetMap.put("JFChangesUnitPriceCostExternal", strJFChangesUnitPriceCostExternal);
                        targetMap.put("JFChangesMouldCostExternal", strJFChangesMouldCostExternal);
                        break;
                    }
                    case "BUR": {
                        String strJFAcknowledgmentContentAmount = UIUtil.getValue(formMap, "JFAcknowledgmentContentAmount");
                        targetMap.put("JFAcknowledgmentContentAmount", strJFAcknowledgmentContentAmount);
                        break;
                    }
                    case "APR": {
                        String strJFChangesInvestment = UIUtil.getValue(formMap, "JFChangesInvestment");
                        String strJFChangesDevelopment = UIUtil.getValue(formMap, "JFChangesDevelopment");
                        String strJFChangesMould = UIUtil.getValue(formMap, "JFChangesMould");
                        String strJFSumInventoryScrapAmount = UIUtil.getValue(formMap, "JFSumInventoryScrapAmount");
                        String strJFChangesInvestmentExternal = UIUtil.getValue(formMap, "JFChangesInvestmentExternal");
                        String strJFChangesDevelopmentExternal = UIUtil.getValue(formMap, "JFChangesDevelopmentExternal");
                        String strJFChangesMouldExternal = UIUtil.getValue(formMap, "JFChangesMouldExternal");
                        String strJFSumInventoryScrapAmountExternal = UIUtil.getValue(formMap, "JFSumInventoryScrapAmountExternal");
                        String strJFAPRNotes = UIUtil.getValue(formMap, "JFAPRNotes");
                        String strJFOtherFee = UIUtil.getValue(formMap, "JFOtherFee");
                        String strJFTestFee = UIUtil.getValue(formMap, "JFTestFee");
                        targetMap.put("JFChangesInvestment", strJFChangesInvestment);
                        targetMap.put("JFChangesDevelopment", strJFChangesDevelopment);
                        targetMap.put("JFChangesMould", strJFChangesMould);
                        targetMap.put("JFSumInventoryScrapAmount", strJFSumInventoryScrapAmount);
                        targetMap.put("JFChangesInvestmentExternal", strJFChangesInvestmentExternal);
                        targetMap.put("JFChangesDevelopmentExternal", strJFChangesDevelopmentExternal);
                        targetMap.put("JFChangesMouldExternal", strJFChangesMouldExternal);
                        targetMap.put("JFSumInventoryScrapAmountExternal", strJFSumInventoryScrapAmountExternal);
                        targetMap.put("JFAPRNotes", strJFAPRNotes);
                        targetMap.put("JFOtherFee", strJFOtherFee);
                        targetMap.put("JFTestFee", strJFTestFee);
                        break;
                    } default:{
                        break;
                    }
                }
            }

        }

    }
    /**
     * @param context
     * @param args
     * @return java.lang.String
     * @throws
     * @description 删除文档
     * @author CHENYAN
     * @date 2024/8/6 17:29
     */
    public String delDocument(Context context, String[] args) throws Exception {
        Map reqMap = (Map) JPO.unpackArgs(args);
        String strObjectId = (String) reqMap.get("objectId");
        String strType = (String) reqMap.get("type");
        String strRelId = (String) reqMap.get("relId");
        JF_LOGGER.info("strObjectId:{}", strObjectId);
        JF_LOGGER.info("strType:{}", strType);
        JF_LOGGER.info("strRelId:{}", strRelId);
        HashMap<String, String> res = new HashMap<>();
        Gson gson = new Gson();
        if (UIUtil.isNotNullAndNotEmpty(strRelId)) {
            try {
                ContextUtil.pushContext(context);
                ContextUtil.startTransaction(context, true);
                DomainRelationship.disconnect(context, strRelId);
                ContextUtil.commitTransaction(context);
                res.put("code", "200");
            } catch (Exception e) {
                JF_LOGGER.error(e.getMessage());
                ContextUtil.abortTransaction(context);
                res.put("code", "404");
            } finally {
                ContextUtil.popContext(context);
                return gson.toJson(res);
            }
        }
        return gson.toJson(res);
    }

    /**
     * @param context
     * @param args
     * @return java.lang.String
     * @throws
     * @description 更新自制件成本信息
     * @author CHENYAN
     * @date 2026/1/13 14:30
     */
    public String updatePartInfoByECRBOMPriceModel(Context context, String[] args) {
        JF_LOGGER.info("-------------------------------------updatePartInfoByECRBOMPriceModel begin --------------------------------------------------");
        Map resMap = new HashMap<>();
        String strCode = "200";
        String strMessage = "更新成功";
        try {
            Map argsMap = JPO.unpackArgs(args);
            JF_LOGGER.info("argsMap:{}", argsMap);
            String strObjectId = UIUtil.getValue(argsMap, STRING_OBJECTID);
            String strTableName = UIUtil.getValue(argsMap, STRING_SELECT_TABLE);
            Object tableDataObj = argsMap.get("tableData");

            if (Objects.isNull(tableDataObj)) {
                strCode = "400";
                strMessage = "表格数据不能为空";
                resMap.put("code", strCode);
                resMap.put("message", strMessage);
                return GSON.toJson(resMap);
            }
            List tableDataList = (List) tableDataObj;
            //更新初始版本数据
            try {
                Map<String, String> attrUpdateMap = new HashMap<>();
                ContextUtil.pushContext(context);
                ContextUtil.startTransaction(context,true);
                // 遍历表格数据，更新属性
                String firstRevision = "AA.1.000";
                for (int i = 0; i < tableDataList.size(); i++) {
                    //只取第一个
                    if (i >= 1) {
                        break;
                    }
                    Map rowDataMap = (Map) tableDataList.get(i);
                    String strPartId = (String) rowDataMap.get(SELECT_ID);
                    JF_LOGGER.info("strPartId:{}",strPartId);
                    if (UIUtil.isNullOrEmpty(strPartId)) {
                        continue;
                    }

                    DomainObject partObj = DomainObject.newInstance(context, strPartId);
                    firstRevision=partObj.getInfo(context, SELECT_REVISION);
                    // 获取 isEdit 标识，只更新标记为可编辑的属性
                    Object isEditObj = rowDataMap.get("isEdit");
                    if (Objects.isNull(isEditObj) || !(isEditObj instanceof Map)) {
                        continue;
                    }
                    Map<String, Boolean> isEditMap = (Map<String, Boolean>) isEditObj;

                    // 遍历 isEdit 中标记为 true 的属性
                    for (Map.Entry<String, Boolean> entry : isEditMap.entrySet()) {
                        String strKey = entry.getKey();
                        Boolean isEditable = entry.getValue();

                        // 只处理标记为可编辑的属性
                        if (isEditable == null || !isEditable) {
                            continue;
                        }

                        // 获取该属性的值
                        String strValue = (String) rowDataMap.get(strKey);
                        if (UIUtil.isNullOrEmpty(strValue)) {
                            strValue = EMPTY_STRING;
                        }

                        // 构建完整的属性名
                        String strFullAttrName = JF_PublicMethodClass_mxJPO.buildStringInStrings(JF_FormalECRStaticMethod_mxJPO.STRING_TXO_PRICE_PACKAGE_NAME, strKey);
                        attrUpdateMap.put(strFullAttrName, strValue);
                    }

                    JF_LOGGER.info("strPartId:{}, attrUpdateMap:{}", strPartId, attrUpdateMap);

                    // 批量更新属性
                    if (!attrUpdateMap.isEmpty()) {
                        partObj.setAttributeValues(context, attrUpdateMap);
                    }
                }
                StringList typeSelectList = new StringList();
                //构造select属性集合
                JF_FormalECRStaticMethod_mxJPO.buildSelectAttrByTableName(strTableName, typeSelectList);
                //更新其他版本数据
                if (UIUtil.isNotNullAndNotEmpty(strObjectId)) {
                    BusinessObjectList allRevisionObjList = JF_Util_mxJPO.sortMapListFilterRevision(context,new String[]{strObjectId,firstRevision});//to do
                    int allRevisionSize = allRevisionObjList.size();
                    if (Objects.nonNull(allRevisionObjList) && allRevisionSize > 1) {
                        for (int i = 1; i < allRevisionSize; i++) {
                            //同步其他版本属性的集合
                            Map saveAttrMap = new HashMap<>();
                            BusinessObject obj = allRevisionObjList.get(i);
                            DomainObject domainObj = DomainObject.newInstance(context, obj);
                            Map info = domainObj.getInfo(context, typeSelectList);
                            for (Map.Entry entry : attrUpdateMap.entrySet()) {
                                //前一个版本的属性和属性值
                                String strAttrName = (String) entry.getKey();
                                String strAttrValue = (String) entry.getValue();
                                //当前版本的增量属性
                                StringList incrementAttrList = JF_FormalECRStaticMethod_mxJPO.getChangeAttrNameByUnitCostingAttr(strAttrName);
                                BigDecimal beforeDecimal = JF_FormalECRStaticMethod_mxJPO.newInstanceBigdecimal(strAttrValue);
                                JF_LOGGER.info("incrementAttrList:{}",incrementAttrList);
                                for (int i1 = 0; i1 < incrementAttrList.size(); i1++) {
                                    String strIncrementAttrName = incrementAttrList.get(i1);
                                    String strIncrementAttrValue = UIUtil.getValue(info, strIncrementAttrName);
                                    if (UIUtil.isNotNullAndNotEmpty(strIncrementAttrValue)){
                                        beforeDecimal = beforeDecimal.add(JF_FormalECRStaticMethod_mxJPO.newInstanceBigdecimal(strIncrementAttrValue));
                                        //四舍五入保留两位
                                        beforeDecimal = beforeDecimal.setScale(2, RoundingMode.HALF_UP);
                                    }
                                }
                                saveAttrMap.put(strAttrName, beforeDecimal.toString());
                            }
                            domainObj.setAttributeValues(context, saveAttrMap);
                            //复制当前版本的属性值供下一个版本使用
                            attrUpdateMap = saveAttrMap;
                        }
                    }
                }
                ContextUtil.commitTransaction(context);
            }catch (Exception e){
                ContextUtil.abortTransaction(context);
                JF_LOGGER.error("e:{}",e.getMessage());
            }finally {
                ContextUtil.popContext(context);
            }
        } catch (Exception e) {
            e.printStackTrace();
            JF_LOGGER.error(e.getMessage(), e);
            strCode = "500";
            strMessage = "更新失败：" + e.getMessage();
        }

        resMap.put("code", strCode);
        resMap.put("message", strMessage);
        String strRes = GSON.toJson(resMap);
        JF_LOGGER.info("strRes:{}", strRes);
        JF_LOGGER.info("-------------------------------------updatePartInfoByECRBOMPriceModel end --------------------------------------------------");
        return strRes;
    }




    /**
     * @param context
     * @param args
     * @return java.util.Map
     * @throws
     * @description 单件成本模态框
     * @author CHENYAN
     * @date 2026/1/12 10:02
     */
    public String getPartInfoByECRBOMPriceModel(Context context, String[] args) {
        JF_LOGGER.info("-------------------------------------getPartInfoByECRBOMPriceModel begin --------------------------------------------------");
        Map resMap = new HashMap<>();
        String strCode = "200";
        try {
            Map argsMap = JPO.unpackArgs(args);
            JF_LOGGER.info("argsMap:{}", argsMap);
            String strECRId  =  (String) argsMap.get("ECRId");
            DomainObject ecr = DomainObject.newInstance(context, strECRId);
            String strECRProjectId = ecr.getInfo(context, "from[JFChange2Project].to.id");
            String strObjectId = UIUtil.getValue(argsMap, STRING_OBJECTID);
            boolean isZeroPart = JF_FormalECRStaticMethod_mxJPO.checkPartIsZeroPart(context, strObjectId, strECRProjectId);
            MapList loginUserSignTaskRole = JF_PublicMethodClass_mxJPO.getLoginUserSignTaskRoleByECRAndUser(context, new String[]{strECRId,context.getUser()});

            String strRelId = UIUtil.getValue(argsMap, STRING_RELID);
            String strTableName = UIUtil.getValue(argsMap, STRING_SELECT_TABLE);
            boolean isBuyTable = JF_FormalECRStaticMethod_mxJPO.checkIsBuyTable(strTableName);
            boolean isMakeTable = JF_FormalECRStaticMethod_mxJPO.checkIsMakeTable(strTableName);
            StringList typeSelectList = new StringList();
            //可以编辑的属性
            StringList canEditAttrList = new StringList();
            typeSelectList.add(SELECT_ID);
            typeSelectList.add(SELECT_NAME);
            typeSelectList.add(SELECT_REVISION);
            typeSelectList.add(SELECT_ATTR_V_PART_NUMBER);
            typeSelectList.add(SELECT_ATTR_JF_PartNameCN);
            typeSelectList.add(SELECT_ATTR_JF_PartType);
            typeSelectList.add(SELECT_ATTR_JF_ProcurementType);
            JF_FormalECRStaticMethod_mxJPO.buildSelectAttrByTableName(strTableName, typeSelectList);
            //获取可以编辑的属性
            canEditAttrList.addAll(JF_FormalECRStaticMethod_mxJPO.getCanEditUnitPartPriceByRoleName(JF_FormalECRStaticMethod_mxJPO.STRING_ROLE_ALL,isBuyTable,isMakeTable));
            //关联ECR 发布数据的ECR
            typeSelectList.add(SELECT_ATTR_JFConnectECR);
            //关联项目
            typeSelectList.add(JF_FormalECRStaticMethod_mxJPO.STRING_SELECT_CONNECT_PROJECT);

            //表单数据
            String strPrintMql = MessageFormat.format("print connection {0} select {1} {2} {3} dump @", strRelId, SELECT_ATTRIBUTE_JF_BOMBeforeQuantity, SELECT_ATTRIBUTE_JF_BOMQuantity, SELECT_ATTRIBUTE_JF_ChangeBeforeRev);
            JF_LOGGER.info("strPrintMql:{}", strPrintMql);
            String strSelectRelRes = MqlUtil.mqlCommand(context, Boolean.FALSE, strPrintMql, Boolean.TRUE);
            JF_LOGGER.info("strSelectRelRes:{}", strSelectRelRes);
            //防止尾部结果为空
            String[] split = strSelectRelRes.split("@", -1);
            //变更前版本 实际保存的是id
            String strBeforeRevision = EMPTY_STRING;
            Map formDataMap = new HashMap<>();
            Map beforeForm = new HashMap<>();
            Map afterForm = new HashMap<>();
            String strAfterQuantity = EMPTY_STRING;
            String strBeforeQuantity = EMPTY_STRING;
            JF_LOGGER.info("split.length:{}", split.length);
            if (split.length >= 3) {
                strAfterQuantity = split[1];
                strBeforeRevision = split[2];
                strBeforeQuantity = split[0];
                if (UIUtil.isNotNullAndNotEmpty(strBeforeRevision)){
                    DomainObject beforeObj = DomainObject.newInstance(context, strBeforeRevision);
                    if (beforeObj.exists(context)) {
                        String strActualBeforeRevision = beforeObj.getInfo(context, SELECT_REVISION);
                        strBeforeRevision = strActualBeforeRevision;
                    }
                }
                beforeForm.put(SELECT_REVISION, strBeforeRevision);
                beforeForm.put("quantity", strBeforeQuantity);
                afterForm.put("quantity", strAfterQuantity);
            }
            //表格数据
            MapList allRevisionMapList = new MapList();
            Map currentInfoMap = null;
            Map beforeInfoMap = null;
            if (UIUtil.isNotNullAndNotEmpty(strObjectId)) {
                BusinessObjectList allRevisionObjList = JF_Util_mxJPO.sortMapListInRevisionCurrent(context, new String[]{strObjectId});
                JF_LOGGER.info("allRevisionObjList:{}",allRevisionObjList);
                int allRevisionSize = allRevisionObjList.size();
                if (Objects.nonNull(allRevisionObjList) && allRevisionSize > 0) {
                    for (int i = 0; i < allRevisionSize; i++) {
                        BusinessObject obj = allRevisionObjList.get(i);
                        DomainObject domainObj = DomainObject.newInstance(context, obj);
                        Map info = domainObj.getInfo(context, typeSelectList);
                        String strPartId = UIUtil.getValue(info, SELECT_ID);
                        String strPartRevision = UIUtil.getValue(info, SELECT_REVISION);
                        //当前对象的集合
                        if (strPartId.equals(strObjectId)) {
                            currentInfoMap = info;
                        }
                        //匹配到变更前版本
                        if (UIUtil.isNotNullAndNotEmpty(strBeforeRevision) && strBeforeRevision.equals(strPartRevision)) {
                            beforeInfoMap = info;
                        }
                        Map resAttrMap;
                        //只有第一版可以编辑
                        if (i == 0) {
                            resAttrMap = JF_FormalECRStaticMethod_mxJPO.initPartTableAttrValue(context, info, canEditAttrList,loginUserSignTaskRole,isZeroPart);
                        } else {
                            resAttrMap = JF_FormalECRStaticMethod_mxJPO.initPartTableAttrValue(context, info, null,loginUserSignTaskRole,isZeroPart);
                        }
                        allRevisionMapList.add(resAttrMap);
                    }
                }
            }
            //变更后表单数据
            String strCurrentId = EMPTY_STRING;
            String strCurrentRevision = EMPTY_STRING;
            String strCurrentPartName = EMPTY_STRING;
            String strCurrentPartNum = EMPTY_STRING;
            String strCurrentPartUnitPrice = EMPTY_STRING;
            String strCurrentPartChangeUnitPrice = EMPTY_STRING;
            //变更前表单数据
            String strBeforeId = EMPTY_STRING;
            String strBeforePartUnitPrice = EMPTY_STRING;
            String strBeforePartChangeUnitPrice = EMPTY_STRING;
            if (Objects.isNull(currentInfoMap)) {
                DomainObject currentObj = DomainObject.newInstance(context, strObjectId);
                StringList currentSelectList = new StringList();
                currentSelectList.add(SELECT_ID);
                currentSelectList.add(SELECT_NAME);
                currentSelectList.add(SELECT_REVISION);
                currentSelectList.add(SELECT_ATTR_V_PART_NUMBER);
                currentSelectList.add(SELECT_ATTR_JF_PartNameCN);
                //单件成本
                currentSelectList.add(SELECT_ATTR_JF_VPMReferenceCost_JF_UnitPriceCost);
                //采购单件成本
                currentSelectList.add(SELECT_ATTR_JF_VPMReferenceCost_JF_UnitPrice);
                Map curretnInfoMap = currentObj.getInfo(context, currentSelectList);
                strCurrentRevision = UIUtil.getValue(curretnInfoMap, SELECT_REVISION);
                strCurrentId = UIUtil.getValue(curretnInfoMap, SELECT_ID);
                strCurrentPartNum = UIUtil.getValue(curretnInfoMap, SELECT_ATTR_V_PART_NUMBER);
                if (UIUtil.isNullOrEmpty(strCurrentPartName)) {
                    strCurrentPartNum = UIUtil.getValue(currentInfoMap, SELECT_NAME);
                }
                //单件成本
                strCurrentPartUnitPrice = UIUtil.getValue(curretnInfoMap, SELECT_ATTR_JF_VPMReferenceCost_JF_UnitPrice);
                //采购单件成本
                strCurrentPartChangeUnitPrice = UIUtil.getValue(curretnInfoMap, SELECT_ATTR_JF_VPMReferenceCost_JF_ChangeUnitPrice);

            } else {
                strCurrentRevision = UIUtil.getValue(currentInfoMap, SELECT_REVISION);
                strCurrentId = UIUtil.getValue(currentInfoMap, SELECT_ID);
                strCurrentPartNum = UIUtil.getValue(currentInfoMap, SELECT_ATTR_V_PART_NUMBER);
                strCurrentPartName = UIUtil.getValue(currentInfoMap, SELECT_ATTR_JF_PartNameCN);
                if (UIUtil.isNullOrEmpty(strCurrentPartName)) {
                    strCurrentPartNum = UIUtil.getValue(currentInfoMap, SELECT_NAME);
                }
                //单件成本
                strCurrentPartUnitPrice = UIUtil.getValue(currentInfoMap, SELECT_ATTR_JF_VPMReferenceCost_JF_UnitPriceCost);
                //采购单件成本
                strCurrentPartChangeUnitPrice = UIUtil.getValue(currentInfoMap, SELECT_ATTR_JF_VPMReferenceCost_JF_UnitPrice);
            }
            //存在变更前版本
            if (Objects.nonNull(beforeInfoMap)) {
                strBeforeId = UIUtil.getValue(beforeInfoMap, SELECT_ID);
                //单件成本
                strBeforePartUnitPrice = UIUtil.getValue(beforeInfoMap, SELECT_ATTR_JF_VPMReferenceCost_JF_UnitPriceCost);
                //采购单件成本
                strBeforePartChangeUnitPrice = UIUtil.getValue(beforeInfoMap, SELECT_ATTR_JF_VPMReferenceCost_JF_UnitPrice);
            }
            beforeForm.put(SELECT_ID, strBeforeId);
            beforeForm.put(SELECT_REVISION, strBeforeRevision);
            beforeForm.put(JF_FormalECRStaticMethod_mxJPO.replaceAttrPreAndSubAndPackage(ATTR_JF_VPMReferenceCost_JF_UnitPriceCost), strBeforePartUnitPrice);
            beforeForm.put(JF_FormalECRStaticMethod_mxJPO.replaceAttrPreAndSubAndPackage(ATTR_JF_VPMReferenceCost_JF_UnitPrice), strBeforePartChangeUnitPrice);

            afterForm.put(SELECT_ID, strCurrentId);
            afterForm.put(SELECT_REVISION, strCurrentRevision);
            afterForm.put(JF_FormalECRStaticMethod_mxJPO.replaceAttrPreAndSubAndPackage(ATTR_JF_VPMReferenceCost_JF_UnitPriceCost), strCurrentPartUnitPrice);
            afterForm.put(JF_FormalECRStaticMethod_mxJPO.replaceAttrPreAndSubAndPackage(ATTR_JF_VPMReferenceCost_JF_UnitPrice), strCurrentPartChangeUnitPrice);

            //变更前总成本
            String strBeforeTotalPrice = EMPTY_STRING;
            String strBeforeTotalChangePrice = EMPTY_STRING;
            if (UIUtil.isNotNullAndNotEmpty(strBeforePartUnitPrice) && UIUtil.isNotNullAndNotEmpty(strBeforeQuantity)) {
                BigDecimal beforeQuantity =JF_FormalECRStaticMethod_mxJPO.newInstanceBigdecimal(strAfterQuantity);
                BigDecimal beforePrice = JF_FormalECRStaticMethod_mxJPO.newInstanceBigdecimal(strCurrentPartUnitPrice);
                strBeforeTotalPrice = beforeQuantity.multiply(beforePrice).toString();
            }
            //变更前采购单件总成本
            if (UIUtil.isNotNullAndNotEmpty(strBeforePartChangeUnitPrice) && UIUtil.isNotNullAndNotEmpty(strBeforeQuantity)) {
                BigDecimal beforeQuantity = JF_FormalECRStaticMethod_mxJPO.newInstanceBigdecimal(strAfterQuantity);
                BigDecimal beforePrice = JF_FormalECRStaticMethod_mxJPO.newInstanceBigdecimal(strCurrentPartChangeUnitPrice);
                strBeforeTotalChangePrice = beforeQuantity.multiply(beforePrice).toString();
            }
            //变更后总成本
            String strTotalPrice = EMPTY_STRING;
            //变更后采购单件总成本
            String strTotalChangePrice = EMPTY_STRING;
            if (UIUtil.isNotNullAndNotEmpty(strCurrentPartUnitPrice) && UIUtil.isNotNullAndNotEmpty(strAfterQuantity)) {
                BigDecimal afterQuantity = JF_FormalECRStaticMethod_mxJPO.newInstanceBigdecimal(strAfterQuantity);
                BigDecimal afterPrice = JF_FormalECRStaticMethod_mxJPO.newInstanceBigdecimal(strCurrentPartUnitPrice);
                strTotalPrice = afterQuantity.multiply(afterPrice).toString();
            }
            //变更后采购单件总成本
            if (UIUtil.isNotNullAndNotEmpty(strCurrentPartChangeUnitPrice) && UIUtil.isNotNullAndNotEmpty(strAfterQuantity)) {
                BigDecimal afterQuantity = JF_FormalECRStaticMethod_mxJPO.newInstanceBigdecimal(strAfterQuantity);
                BigDecimal afterPrice = JF_FormalECRStaticMethod_mxJPO.newInstanceBigdecimal(strCurrentPartChangeUnitPrice);
                strTotalChangePrice = afterQuantity.multiply(afterPrice).toString();
            }
            beforeForm.put("total_" + JF_FormalECRStaticMethod_mxJPO.replaceAttrPreAndSubAndPackage(ATTR_JF_VPMReferenceCost_JF_UnitPriceCost), strBeforeTotalPrice);
            beforeForm.put("total_" + JF_FormalECRStaticMethod_mxJPO.replaceAttrPreAndSubAndPackage(ATTR_JF_VPMReferenceCost_JF_UnitPrice), strBeforeTotalChangePrice);
            afterForm.put("total_" + JF_FormalECRStaticMethod_mxJPO.replaceAttrPreAndSubAndPackage(ATTR_JF_VPMReferenceCost_JF_UnitPriceCost), strTotalPrice);
            afterForm.put("total_" + JF_FormalECRStaticMethod_mxJPO.replaceAttrPreAndSubAndPackage(ATTR_JF_VPMReferenceCost_JF_UnitPrice), strTotalChangePrice);
            formDataMap.put("partName", strCurrentPartName);
            formDataMap.put("partNumber", strCurrentPartNum);
            formDataMap.put("beforeChange", beforeForm);
            formDataMap.put("afterChange", afterForm);
            Map dataMap = new HashMap<>();
            dataMap.put("formData", formDataMap);
            dataMap.put("tableData", allRevisionMapList);
            resMap.put("data", dataMap);

        } catch (Exception e) {
            e.printStackTrace();
            JF_LOGGER.error(e.getMessage());
            strCode = "404";
        }
        resMap.put("code", strCode);
        String strRes = GSON.toJson(resMap);
        JF_LOGGER.info("strRes:{}", strRes);
        JF_LOGGER.info("-------------------------------------getPartInfoByECRBOMPriceModel end --------------------------------------------------");
        return strRes;
    }

    public String validateFormalECRTableRequireField(Context context, String[] args) throws Exception {
        JF_LOGGER.info("----------------------------- validateFormalECRTableRequireField begin ------------------------------------");
        HashMap res = new HashMap<>();
        Map reqMap = (Map) JPO.unpackArgs(args);
        List roleList = (List) reqMap.get("roleList");
        StringList roleNameStringList = StringList.create(roleList);
        String strLang = context.getSession().getLanguage();
        String strObjectId = (String) reqMap.get(STRING_OBJECTID);
        JF_LOGGER.info("strObjectId:{}", strObjectId);
        JF_LOGGER.info("roleList:{}", roleList);
        String initargs[] = {};
        Map parameters = new HashMap<>();
        parameters.put("objectId", strObjectId);
        parameters.put("expandLevel", "All");
        DomainObject ecr = DomainObject.newInstance(context, strObjectId);
        Map checkRes1 = new HashMap();
        Map checkRes2 = new HashMap();
        //初始成本属性
        StringList canEditUnitAttrList = JF_FormalECRStaticMethod_mxJPO.getCanEditUnitPartPriceByRoleName(JF_FormalECRStaticMethod_mxJPO.STRING_ROLE_ALL, true, true);

        //自制件 table数据
        if (roleList.contains("AME") || roleList.contains("APR") || roleList.contains("FoamAME") || roleList.contains("TrimAME") || roleList.contains("PRR") || roleList.contains("Costing")) {
            parameters.put(STRING_SELECT_TABLE, "JFFormalECRController");
            //设置校验单件价格key
//            parameters.put(JF_FormalECRStaticMethod_mxJPO.STRING_VERIFY_UNIT_PRICE, "true");
            //获取自制件数据
            MapList makeTableMapList = (MapList) JPO.invoke(context, "JF_FormalECRService", initargs, "getFormalECRMakeTableData", JPO.packArgs(parameters), MapList.class);

            checkRes1 = checkTableDataPriceIsWrite(context, makeTableMapList, strObjectId,strLang,canEditUnitAttrList);
        } else {
            checkRes1.put("result", true);
        }
        if (roleList.contains("ICO") || roleList.contains("BUR") || roleList.contains("PRR") || roleList.contains("Costing")) {
            parameters.put(STRING_SELECT_TABLE, "JFFormalECRCosting");
            //设置校验单件价格key
//            parameters.put(JF_FormalECRStaticMethod_mxJPO.STRING_VERIFY_UNIT_PRICE, "true");
            //采购件table数据
            MapList buyTableMapList = (MapList) JPO.invoke(context, "JF_FormalECRService", initargs, "getFormalECRBuyTableData", JPO.packArgs(parameters), MapList.class);

            checkRes2 = checkTableDataPriceIsWrite(context, buyTableMapList,  strObjectId,strLang,canEditUnitAttrList);
        } else {
            checkRes2.put("result", true);
        }
        JF_LOGGER.info("checkRes1:{}", checkRes1);
        JF_LOGGER.info("checkRes2:{}", checkRes2);
        boolean res1 = (boolean) checkRes1.get("result");
        boolean res2 = (boolean) checkRes2.get("result");
        if (res1 && res2) {
            res.put("result", true);
            res.put("code", "200");
        } else {
            res.put("result", false);
            res.put("code", "404");
            String strMakeMess = (String) checkRes1.get("mess");
            String strBuyMess = (String) checkRes2.get("mess");
            res.put("makeMess", strMakeMess);
            res.put("buyMess", strBuyMess);
        }
        Gson gson = new Gson();
        JF_LOGGER.info("----------------------------- validateFormalECRTableRequireField end ------------------------------------");
        return gson.toJson(res);
    }
    public Map checkTableDataPriceIsWrite(Context context ,MapList tableMapList , String strECRId,String strLang,StringList canEditUnitAttrList){
        HashMap<Object, Object> resMap = new HashMap<>();
        boolean checkResult = true;
        //未填写的属性nls集合
        StringList notWriteAttrNameList = new StringList();
        firstFor:
        for (int i = 0; i < tableMapList.size(); i++) {
            Map tableMap = (Map) tableMapList.get(i);
            if (tableMap.containsKey(JF_FormalECRStaticMethod_mxJPO.STRING_ISEDIT)){
                Map editMap = (Map) tableMap.get(JF_FormalECRStaticMethod_mxJPO.STRING_ISEDIT);
                String strPartNum = (String) tableMap.get(SELECT_ATTR_V_PART_NUMBER);
                if (Objects.nonNull(editMap) && editMap.size() > 0){
                    JF_LOGGER.info("editMap:{}",editMap);
                    Set keySet = editMap.keySet();
                    //检查增量属性
                    secondFor:
                    for (Object oAttrName : keySet) {
                        String strAttrName = (String) oAttrName;
                        //必填校验排除掉物流费
                        if (ATTR_JF_LogisticsFees.equals(strAttrName) || canEditUnitAttrList.contains(strAttrName)){
                            continue ;
                        }
                        String strSelectPriceAttr =  JF_FormalECRStaticMethod_mxJPO.complementSelectAttrNameByAttrName(strAttrName);

                        String strPriceValue = (String) tableMap.get(strSelectPriceAttr);
                        // 初始成本不能为0.0
                        if (UIUtil.isNullOrEmpty(strPriceValue)) {
                            String strAttrNls = EMPTY_STRING;
                            try {
                                //未填写的属性值
                                strAttrNls = i18nNow.getAttributeI18NString(strAttrName, strLang);
                                notWriteAttrNameList.add(strAttrNls);
                            } catch (MatrixException e) {
                                //吃掉异常不阻止主逻辑
                                JF_LOGGER.error(e.getMessage());
                            }
                            checkResult = false;

                        }

                    }
                    if (!checkResult){
                        String strMess = EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", context.getLocale(), "emxComponents.FormalECR.VALIDATE.ERROR");
                        String strFullMess = String.format(strMess, strPartNum, notWriteAttrNameList.join(","));
                        resMap.put("mess", strFullMess);
                        break firstFor;
                    }

                }
            }
        }
        resMap.put("result", checkResult);
        return resMap;
    }
    /**
     *
     *@description 判断指定角色的key 是否存在会签任务
     *@param strRoleKey role key
     *@param allTaskList 所有的会签任务
     *@return boolean
     *@throws
     *@author CHENYAN
     *@date 2025/5/9 14:58
     */
    public boolean checkHasExistTaskInProjectRole(String strRoleKey ,MapList allTaskList){
        String strRoleValue = getRoleValueByKey(strRoleKey);
        long count = allTaskList.stream().filter(m -> {
            Map taskMap = (Map) m;
            String strTaskRoleValue = (String) taskMap.get("projectRole");
            return strRoleValue.equals(strTaskRoleValue);
        }).count();
        return  count > 0 ;
    }
    public static String getRoleValueByKey(String strRole) {
        //转换角色的key
        if (ROLE_MAPPING_MAP.containsKey(strRole)) {
            return ROLE_MAPPING_MAP.get(strRole);
        }
        return "";
    }

    public String getTableData (Context context ,String[] args) throws Exception{
        Map reqMap = (Map) JPO.unpackArgs(args);
        String strObjectId = (String) reqMap.get(STRING_OBJECTID);
        HashMap<Object, Object> jpoMap = new HashMap<>();
        jpoMap.put(STRING_OBJECTID,strObjectId);
        jpoMap.put(STRING_SELECT_TABLE,"JFFormalECRCosting");
        jpoMap.put(JF_FormalECRStaticMethod_mxJPO.STRING_VERIFY_UNIT_PRICE, "true");
        //采购件table数据
        String initargs[] = {};
        MapList buyTableMapList = (MapList) JPO.invoke(context, "JF_FormalECRService", initargs, "getFormalECRBuyTableData", JPO.packArgs(jpoMap), MapList.class);
        HashMap<Object, Object> objectObjectHashMap = new HashMap<>();
        objectObjectHashMap.put("data",buyTableMapList);
        objectObjectHashMap.put("code","200");
        return GSON.toJson(objectObjectHashMap);
    }

}
