import com.google.gson.Gson;
import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.DomainRelationship;
import com.matrixone.apps.domain.util.*;
import com.matrixone.apps.framework.ui.UIUtil;
import com.matrixone.jdl.MatrixSession;
import matrix.db.*;
import matrix.util.StringList;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItem;
import org.apache.commons.text.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Set;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static com.dassault_systemes.vplm.config.util.ConfigMDD.TYPE_VPMReference;
import static com.matrixone.apps.domain.DomainConstants.*;
import static com.matrixone.apps.domain.MultiValueSelects.RELATIONSHIP_REFERENCE_DOCUMENT;

/**
 * @author CHENYAN
 * @version v1.0.0
 * 创建时间：2025/5/9 11:02
 * @description 新ECR的REST相关方法
 */
public class JF_NewECRRESTService_mxJPO {
    private static final Logger _logger = LoggerFactory.getLogger(JF_NewECRRESTService_mxJPO.class);
    public static final String FolderWIN = "c:\\temp\\";
    public static final String FolderUNIX = "/tmp/";
    public StringList typeSelectList = new StringList();
    public static final StringList EDIT_ECRCosting_TABLE_ROLE_LIST = new StringList();
    public static final StringList EDIT_ECRController_TABLE_ROLE_LIST = new StringList();

    //AME编辑form是否展示零件属性值
    public static final StringList EDIT_AME_FORM_FIELD_LIST = new StringList();

    //ECR 会签状态之前所有状态
    public static final StringList ECR_STATE_SIGN_BEFORE_List = new StringList();

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    public static Map<String, String> ROLE_MAPPING_MAP = new HashMap();
    private ZoneId zoneId;

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
        EDIT_ECRCosting_TABLE_ROLE_LIST.add("PRR");
        EDIT_ECRCosting_TABLE_ROLE_LIST.add("Costing");
        EDIT_ECRCosting_TABLE_ROLE_LIST.add("ICO");
//        EDIT_ECRCosting_TABLE_ROLE_LIST.add("APR");
        EDIT_ECRController_TABLE_ROLE_LIST.add("APR");
        EDIT_ECRController_TABLE_ROLE_LIST.add("AME");
        EDIT_ECRController_TABLE_ROLE_LIST.add("FoamAME");
        EDIT_ECRController_TABLE_ROLE_LIST.add("TrimAME");
        EDIT_ECRController_TABLE_ROLE_LIST.add("PRR");
        EDIT_ECRController_TABLE_ROLE_LIST.add("Costing");
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

    public static String getRoleValueByKey(String strRole) {
        //转换角色的key
        if (ROLE_MAPPING_MAP.containsKey(strRole)) {
            return ROLE_MAPPING_MAP.get(strRole);
        }
        return "";
    }

    public static String getRoleKeyByValue(String strRoleValue) {
        //转换角色的key
        if (ROLE_MAPPING_MAP.containsValue(strRoleValue)) {
            for (Map.Entry<String, String> entry : ROLE_MAPPING_MAP.entrySet()) {
                String strKey = entry.getKey();
                String strValue = entry.getValue();
                if (strRoleValue.equals(strValue)) {
                    return strKey;
                }
            }
        }
        return "";
    }

    /**
     * @param context
     * @param args
     * @return java.lang.String
     * @throws
     * @description 测试上传文件
     * @author CHENYAN
     * @date 2024/8/1 9:28
     */
    public String uploadFile(Context context, String[] args) throws Exception {
        _logger.info("----------------------- uploadFile begin -----------------------------------");
        Map reqMap = (Map) JPO.unpackArgs(args);
        //ECRId
        String strObjectId = (String) reqMap.get("objectId");
        _logger.info("strObjectId:{}", strObjectId);
        //关联文档和对象 关系属性 标识8个表单上传
        String strType = (String) reqMap.get("type");
        String strRelType = (String) reqMap.get("relationship");
        //请求基地址
        String strHostUrl = (String) reqMap.get("hostUrl");
        List files = (List) reqMap.get("fileList");
        List<FileItem> fileItems = new ArrayList<>();
        //将文件写入
        for (int i = 0; i < files.size(); i++) {
            String strTmpPath = context.getWorkspacePath();
            _logger.info("strTmpPath:{}", strTmpPath);
            Map fileMap = (Map) files.get(i);
            InputStream inputStream = (InputStream) fileMap.get("fileInputStream");
            String fileName = (String) fileMap.get("fileName");
            String targetPath = "/tmp/" + fileName;
            File tempFile = File.createTempFile(targetPath, null);
            FileOutputStream fos = new FileOutputStream(tempFile);
            //将FileInputStream的内容写入临时文件
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                fos.write(buffer, 0, bytesRead);
            }
            inputStream.close();
            fos.close();
            //创建FileItem对象
            FileItem fileItem = new DiskFileItem("file", "application/octet-stream",
                    false, fileName, (int) tempFile.length(), tempFile.getParentFile());
            fileItems.add(fileItem);
        }
        _logger.info("fileItem:{}", fileItems);
        _logger.info("files:{}", files);
        _logger.info("strObjectId:{}", strObjectId);
        _logger.info("strRelType:{}", strRelType);
        _logger.info("strObjectId:{}", strObjectId);
        _logger.info("strHostUrl:{}", strHostUrl);
        Map parameterMap = initParameterMap(context, strObjectId, strRelType, strHostUrl, strType, fileItems);
        String initargs[] = {};
        String sResult = (String) JPO.invoke(context, "JF_FileUtils", initargs, "checkinFile", JPO.packArgs(parameterMap), String.class);
        _logger.info("----------------------- uploadFile end  -----------------------------------");
        return sResult;
    }

    /**
     * @param context
     * @param args
     * @return java.lang.String
     * @throws
     * @description 获取ECR的基础信息
     * @author CHENYAN
     * @date 2024/8/1 14:27
     */
    public String getECRInfo(Context context, String[] args) throws Exception {
        Map reqMap = (Map) JPO.unpackArgs(args);
        String strObjectId = (String) reqMap.get("objectId");
        String strHostUrl = (String) reqMap.get("hostUrl");
        _logger.info("strObjectId:{}", strObjectId);
        _logger.info("strHostUrl:{}", strHostUrl);
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
                _logger.info("loginUserSignTaskRole:{}", loginUserSignTaskRole);
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
                _logger.info("files:{}", files);
                //ECR上传附件分组
                Map group = (Map) files.stream().filter(m -> {
                    Map file = (Map) m;
                    return UIUtil.isNotNullAndNotEmpty((String) file.get(JF_PLMConstants_mxJPO.SELECT_ATTR_PROJECT_ROLE));
                }).collect(Collectors.groupingBy(m -> {
                    Map file = (Map) m;
                    return file.get(JF_PLMConstants_mxJPO.SELECT_ATTR_PROJECT_ROLE);
                }));
                _logger.info("group:{}", group);
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
            MapList makeTableList = (MapList) JPO.invoke(context, "JF_ECRService", initargs, "getECRMakeTableData", JPO.packArgs(stringStringHashMap), MapList.class);
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
            _logger.info(e.getMessage());
            res.put("code", "404");
        } finally {
            ContextUtil.popContext(context);
        }
        _logger.info("map:{}", res);
        Gson gson = new Gson();
        String strGson = gson.toJson(res);
        return strGson;
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
        _logger.info("strTimeZone:{}", strTimeZone);
        //获取到时区相对偏移量 -8 , 8
        double dTimeZoneId = convertOffsetToDouble(strTimeZone);
        _logger.info("dTimeZoneId:{}", dTimeZoneId);
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
            _logger.info("strRoleName:{}", strRoleName);
            for (Map.Entry<String, String> entry : ROLE_MAPPING_MAP.entrySet()) {
                if (entry.getValue().equals(strRoleName)) {
                    res.add(entry.getKey());
                }
            }
        }
        return res;
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
                    _logger.info("isExecCustomer:{}", isExecCustomer);
                    if (!isExecCustomer) {
                        // 标识客户报价权限已经处理 APR报价任务处理后直接退出
                        if (JF_PLMConstants_mxJPO.TYPE_JF_CustomerTask.equals(strTaskType)) {
                            isExecCustomer = true;
                            if ((!("Complete".equals(strTaskCurrent) || "Review".equals(strTaskCurrent)))) {
                                formMap.put("editAccess", Boolean.TRUE);
                                _logger.info("strTaskId:{}", strTaskId);
                                _logger.info("strTaskCurrent:{}", strTaskCurrent);
                                formMap.put("signTaskId", strTaskId);
                                formMap.put("signTaskCurrent", strTaskCurrent);
                            } else {
                                formMap.put("editAccess", Boolean.FALSE);
                            }
                            _logger.info("formMap:{}", formMap);
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
                        _logger.info("strTaskId:{}", strTaskId);
                        _logger.info("strTaskCurrent:{}", strTaskCurrent);
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
     * @param strObjectId 需要连接对象id
     * @param strRelType  关系名
     * @param files       文档关联文件
     * @return java.util.Map
     * @throws
     * @description 初始化调用创建文档和连接关系参数
     * @author CHENYAN
     * @date 2024/8/1 13:17
     */
    public static Map initParameterMap(Context context, String strObjectId, String strRelType, String strHostUrl, String strType, List files) throws Exception {
        String sOSName = System.getProperty("os.name");
        String sFolder = sOSName.contains("Windows") ? FolderWIN : FolderUNIX;
        String sTmpDir = Environment.getValue(context, "TMPDIR");
        String separator = sOSName.contains("Windows") ? "\\" : "/";
        if (null != sTmpDir && !sTmpDir.trim().isEmpty()) {
            sFolder = sTmpDir;
            if (!sFolder.substring(sFolder.length() - 1).equals(separator))
                sFolder = sFolder + separator;
        }
        MatrixSession session = context.getSession();
        HashMap params = new HashMap();
        params.put("language", session.getLanguage());
        params.put("objectId", strObjectId);
        params.put("type", strType);
        params.put("hostUrl", strHostUrl);
        params.put("relationship", strRelType);
        params.put("documentCommand", "");
        params.put("folder", sFolder);
        params.put("files", files);
        params.put("objectAction", "create");
        params.put("timezone", "");
        return params;
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
        _logger.info("strObjectId:{}", strObjectId);
        _logger.info("strType:{}", strType);
        _logger.info("strRelId:{}", strRelId);
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
                _logger.error(e.getMessage());
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
     * @param context
     * @param args
     * @return java.lang.String
     * @throws
     * @description ECR表单保存或者提交按钮
     * @author CHENYAN
     * @date 2024/8/6 17:31
     */
    public String saveOrSubmitInECR(Context context, String[] args) throws Exception {
        _logger.info("----------------------------- saveOrSubmitInECR begin ------------------------------------");
        HashMap<Object, Object> res = new HashMap<>();
        Map reqMap = (Map) JPO.unpackArgs(args);
        String strAction = (String) reqMap.get("action");
        String strObjectId = (String) reqMap.get("objectId");
        _logger.info("strObjectId:{}", strObjectId);
        _logger.info("strAction:{}", strAction);
        HashMap attrMap = new HashMap();
        Set<String> signTaskSet = new HashSet<>();
        //复制请求中ECR属性
        copyECRAttribute(attrMap, reqMap, signTaskSet);
        _logger.info("attrMap:{}", attrMap);
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
//                    防止重复提交，执行多次 add by caipan 20260318
                    ecr.setState(context,"APR");
                } else if ("APR".equals(strCurrent) || "Quotation".equals(strCurrent)) {
                    boolean stateIsAllComplete = checkSignTaskIsAllComplete(context, strObjectId);
                    // 提升ECR的状态
                    if (stateIsAllComplete) {
                        ecr.promote(context);
                    }
                }
                ContextUtil.commitTransaction(context);
                _logger.info("state :{}", ecr.getInfo(context, SELECT_CURRENT));
                //只要是提交就需要刷新
                res.put("isRefresh", true);
                res.put("code", "200");
            } catch (Exception e) {
                res.put("code", "404");
                ContextUtil.abortTransaction(context);
                _logger.info(e.getMessage());
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
                _logger.info(e.getMessage());
            } finally {
                ContextUtil.popContext(context);
            }
        }
        Gson gson = new Gson();
        _logger.info("----------------------------- saveOrSubmitInECR end ------------------------------------");
        return gson.toJson(res);
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
        _logger.info("----------------------------- APRReviewSignTask begin ------------------------------------");
        HashMap res = new HashMap<>();
        Map reqMap = (Map) JPO.unpackArgs(args);
        List signTaskIdList = (List) reqMap.get("signTaskIds");
        String strAction = (String) reqMap.get("action");
        String strObjectId = (String) reqMap.get("objectId");
        String strNotes = (String) reqMap.get("Notes");
        _logger.info("strObjectId:{}", strObjectId);
        _logger.info("strAction:{}", strAction);
        _logger.info("Notes:{}", strNotes);
        _logger.info("strSignTaskId:{}", signTaskIdList);
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
                    // add by chenyan 2025/08/29 新增拒绝通知
                    JF_SignTask_mxJPO.projectTaskRejectSendEmail(context,new  String[]{strSignTaskId});
                } else if ("approve".equals(strAction)) {
                    signTask.promote(context);
                }
            }
            ContextUtil.commitTransaction(context);
            //获取最新的会签任务
            MapList allSignTaskInECR = getAllSignTaskInECR(context, ecr);
            res.put("signTaskList", allSignTaskInECR);
            res.put("code", "200");

        } catch (Exception e) {
            _logger.info(e.getMessage());
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
     * @description 根据角色获取到ECRtable中必填属性，校验有没有必填 没有必填不能提交
     * @author CHENYAN
     * @date 2024/8/13 15:34
     */
    public String validateNewECRTableRequireField(Context context, String[] args) throws Exception {
        _logger.info("----------------------------- validateNewECRTableRequireField begin ------------------------------------");
        HashMap res = new HashMap<>();
        Map reqMap = (Map) JPO.unpackArgs(args);
        List roleList = (List) reqMap.get("roleList");
        StringList roleNameStringList = StringList.create(roleList);
        String strObjectId = (String) reqMap.get("objectId");
        _logger.info("strObjectId:{}", strObjectId);
        _logger.info("roleList:{}", roleList);
        String initargs[] = {};
        Map parameters = new HashMap<>();
        parameters.put("objectId", strObjectId);
        parameters.put("expandLevel", "All");
        DomainObject ecr = DomainObject.newInstance(context, strObjectId);
        Map checkRes1 = new HashMap();
        Map checkRes2 = new HashMap();
        try {
            ContextUtil.pushContext(context);
            //自制件 table数据
            // add by 新增的采购和costing 不校验必填
            if (roleList.contains("AME") || roleList.contains("APR") || roleList.contains("FoamAME") || roleList.contains("TrimAME") || roleList.contains("PRR") || roleList.contains("Costing")) {
                MapList makeTableMapList = (MapList) JPO.invoke(context, "JF_NewECRService", initargs, "getNewECRMakeTableData", JPO.packArgs(parameters), MapList.class);
                checkRes1 = getEditRowInTable(context, makeTableMapList, "JFNewECRController", ecr, roleNameStringList, strObjectId);
            } else {
                checkRes1.put("result", true);
            }
            if (roleList.contains("ICO") || roleList.contains("BUR") || roleList.contains("PRR") || roleList.contains("Costing")) {
                //采购件table数据
                MapList buyTableMapList = (MapList) JPO.invoke(context, "JF_NewECRService", initargs, "getNewECRBuyTableData", JPO.packArgs(parameters), MapList.class);
                checkRes2 = getEditRowInTable(context, buyTableMapList, "JFNewECRCosting", ecr, roleNameStringList, strObjectId);
            } else {
                checkRes2.put("result", true);
            }
        } finally {
            ContextUtil.popContext(context);
        }
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
        _logger.info("----------------------------- validateNewECRTableRequireField end ------------------------------------");
        return gson.toJson(res);
    }

    /**
     * @param tableList
     * @param strTable
     * @param ecr
     * @param roleList
     * @return com.matrixone.apps.domain.util.MapList
     * @throws
     * @description 获取Table数据中的可编辑行
     * @author CHENYAN
     * @date 2024/8/13 16:42
     */
    public Map getEditRowInTable(Context context, MapList tableList, String strTable, DomainObject ecr, StringList roleList, String strECRId) throws Exception {
        // 制造件table
        boolean isAlert = true;
        Set requireRowList = new HashSet();
        Map res = new HashMap<>();
        try {
            if ("JFNewECRController".equals(strTable)) {
                StringList ecrSelectList = new StringList();
                ecrSelectList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JFCHANGESOURCE);
                ecrSelectList.add(SELECT_CURRENT);
                Map ecrMap = ecr.getInfo(context, ecrSelectList);
//                String strChangeSource = (String) ecrMap.get(JF_PLMConstants_mxJPO.SELECT_ATTR_JFCHANGESOURCE);
                String strCurrent = (String) ecrMap.get(SELECT_CURRENT);
                AtomicReference<Boolean> isFinancialBP = new AtomicReference<>(false);
                MapList roleMapList = (MapList) roleList.stream().map( strRoleValue ->{
                    String strTmpRoleValue = getRoleValueByKey(strRoleValue);
                    Map roleMap = new HashMap<>();
                    roleMap.put("role",strTmpRoleValue);
                    if (JF_PLMConstants_mxJPO.ATTR_PROJECT_ROLE_Range_FinancialBP.equals(strTmpRoleValue)){
                        isFinancialBP.set(true);
                    }
                    return roleMap ;
                }).collect(Collectors.toCollection(MapList::new));
                for (int i = 0; i < tableList.size(); i++) {
                    Map row = (Map) tableList.get(i);
                    String strDirectBuy = (String) row.get(JF_PLMConstants_mxJPO.SELECT_ATTR_JFDIRECT_BUY);
                    String strPartType = (String) row.get(JF_PLMConstants_mxJPO.SELECT_ATTR_JFPartType);
                    String strPartLevel = (String) row.get(SELECT_LEVEL);
                    String strProcurementType = (String) row.get(JF_PLMConstants_mxJPO.SELECT_ATTR_JF_ProcurementType);
                    String strPartNum = (String) row.get(JF_PLMConstants_mxJPO.SELECT_ATTR_V_PART_NUMBER);
//                    Boolean proEditAccess = JF_NewECRService_mxJPO.checkPartIsMakeAndFoamCoating(context, row, "");
                    String strChangeSource = (String)row.get("to[JFRelateItem].attribute[JFChangeSource]");
                    String strRelationship = (String) row.get(JF_PLMConstants_mxJPO.RELATIONSHIP);

                    String strEditFlag = (String) row.get("rowEditFlag");
//                    if (UIUtil.isNotNullAndNotEmpty(strEditFlag) && (!strEditFlag.equals("2"))) {
                    boolean isSkip = false;
                    if (UIUtil.isNotNullAndNotEmpty(strEditFlag)) {
                        if (isFinancialBP.get()) {
                            if (JF_PLMConstants_mxJPO.REL_JFECRRelateRoot.equals(strRelationship)) {
                                isSkip = JF_NewECRService_mxJPO.checkSkipSunTreeByRoleAndPartType(roleMapList, strPartType);
                            }
                        }else {
                            isSkip = JF_NewECRService_mxJPO.checkSkipSunTreeByRoleAndPartType(roleMapList, strPartType);
                        }
                        if (isSkip){
                            //role 循环标签
                            roleFor:
                            for (int i1 = 0; i1 < roleList.size(); i1++) {
                                String strRole = roleList.get(i1);
                                String strRoleValue = getRoleValueByKey(strRole);
                                // 获取table里面的必填属性
                                StringList canEditFieldList = JF_NewECRService_mxJPO.getRequireEditFieldInRoleAndLinkField(false, strRoleValue, strDirectBuy, strProcurementType, strPartType,strChangeSource,strEditFlag);
                                _logger.info("canEditFieldList1:{}", canEditFieldList);
                                if (null != canEditFieldList && canEditFieldList.size() > 0) {
                                    fieldFor:
                                    for (int i2 = 0; i2 < canEditFieldList.size(); i2++) {
                                        String strFieldName = canEditFieldList.get(i2);
                                        String strSelectFieldKey = JF_PublicMethodClass_mxJPO.buildStringInStrings("tomid[JFECR2MakePartPrice].", "attribute[", strFieldName, "]");
                                        String strPriceValue = (String) row.get(strSelectFieldKey);
                                        //必填属性没有维护
                                        if (UIUtil.isNullOrEmpty(strPriceValue)){
                                            //跳转 role 循环遍历下一个part
                                            requireRowList.add(strPartNum);
                                            isAlert = false;
                                            break roleFor ;
                                        }
                                    }
                                }
                            }
                        }

                    }
                }
            } else if ("JFNewECRCosting".equals(strTable)) {
//                StringList ecrSelectList = new StringList();
//                ecrSelectList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JFCHANGESOURCE);
//                ecrSelectList.add(SELECT_CURRENT);
//                Map ecrMap = ecr.getInfo(context, ecrSelectList);
                for (int i = 0; i < tableList.size();) {
                    Map partMap = (Map) tableList.get(i);
                    //游离的直接跳过校验 add by chenyan 2025/05/15
//                    if (partMap.containsKey("freeFlag")) {
//                        i++;
//                        continue;
//                    }
                    String strPartNum = (String) partMap.get(JF_PLMConstants_mxJPO.SELECT_ATTR_V_PART_NUMBER);
                    String strProcurementType = (String)partMap.get(JF_PLMConstants_mxJPO.SELECT_ATTR_JF_ProcurementType);
                    String strChangeSource = (String)partMap.get("to[JFRelateItem].attribute[JFChangeSource]");
                    //设置默认值
                    if (UIUtil.isNullOrEmpty(strChangeSource)){
                        strChangeSource = JF_PLMConstants_mxJPO.ATTR_JFCHANGESOURCE_RANGE_BOTH;
                    }
                    String strDirectBuy = (String)partMap.get(JF_PLMConstants_mxJPO.SELECT_ATTR_JFDIRECT_BUY);
                    String strPartType = (String)partMap.get(JF_PLMConstants_mxJPO.SELECT_ATTR_JFPartType);
                    String strType = (String)partMap.get(SELECT_TYPE);
                    int nextLevel = JF_NewECRService_mxJPO.findEndOfSubtree(tableList, i); // 找到以i为根的子树结束位置
                    _logger.info(" start : {} end :{}" ,i,nextLevel);
                    //第一次出现ICO和buy才可编辑
                    if ((!"make".equalsIgnoreCase(strProcurementType)) && TYPE_VPMReference.equals(strType)){
                        _logger.info("partMap:{}",partMap);
                        //for循环标签
                        roleFor:
                        for (int i1 = 0; i1 < roleList.size(); i1++) {
                            String strRole = roleList.get(i1);
                            String strRoleValue = getRoleValueByKey(strRole);
                            // 获取table里面的必填属性
                            StringList canEditFieldList = JF_NewECRService_mxJPO.getRequireEditFieldInRoleAndLinkField(true, strRoleValue, strDirectBuy, strProcurementType, strPartType,strChangeSource,"");
                            _logger.info("canEditFieldList:{}", canEditFieldList);
                            if (null != canEditFieldList && canEditFieldList.size() > 0) {
                                fieldFor:
                                for (int i2 = 0; i2 < canEditFieldList.size(); i2++) {
                                    String strFieldName = canEditFieldList.get(i2);
                                    String strSelectFieldKey = JF_PublicMethodClass_mxJPO.buildStringInStrings("tomid[JFECR2PartPrice].", "attribute[", strFieldName, "]");
                                    String strPriceValue = (String) partMap.get(strSelectFieldKey);
                                    _logger.info("strPriceValue:{}",strPriceValue);
                                    //必填属性没有维护
                                    if (UIUtil.isNullOrEmpty(strPriceValue)){
                                        //跳转 role 循环遍历下一个part
                                        requireRowList.add(strPartNum);
                                        isAlert = false;
                                        break roleFor ;
                                    }
                                }
                            }
                        }
                        i = nextLevel;
                    }else {
                        // 没有继续下一个结点
                        i++;
                    }

                }
            }
        } catch (Exception e) {
            _logger.error(e.getMessage());
            throw e;
        }
        res.put("result", isAlert);
        String strMess = "";
        if (requireRowList.size() > 0) {
            strMess = StringList.create(requireRowList).join(",");
        }
        res.put("mess", strMess);
        return res;
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
            _logger.info("roleList:{}", roleList);
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
     * @param context
     * @param args
     * @return java.lang.String
     * @throws
     * @description 获取当前登录人是否拥有标准件库管理员权限
     * @author CHENYAN
     * @date 2024/9/4 13:22
     */
    public String getCurrentUserHasStandardRole(Context context, String[] args) {
        Map res = new HashMap<>();
        try {
            String strLoginUser = context.getUser();
            String strMQLRes = MqlUtil.mqlCommand(context, "print role $1 select person dump ;", true, new String[]{"JfStandardAdmin"});
            _logger.info("strMQLRes :{}", strMQLRes);
            String[] splitPerson = strMQLRes.split(",");
            //需要移除的属性range值
            Map attrMap = new HashMap<>();
            boolean hasRole = StringList.create(splitPerson).contains(strLoginUser);
            if (!hasRole) {
                attrMap.put("JF_PartType", StringList.create("F"));
            }
            res.put("hasRole", hasRole);
            res.put("attributes", attrMap);
            res.put("status", "success");
        } catch (Exception e) {
            _logger.error(e.getMessage());
            res.put("status", "fail");
        }
        Gson gson = new Gson();
        return gson.toJson(res);
    }

    /**
     * @param context
     * @param tableList OOTB表格Maplist
     * @param strTable  表名
     * @param ecr       ecr 对象
     * @param roleList  登录人角色集合
     * @return matrix.util.StringList
     * @throws
     * @description 获取table具体行中的必填属性
     * @author CHENYAN
     * @date 2024/10/10 10:15
     */
    public static Map<String,StringList> getEditAttributeRowInExcel(Context context,  MapList tableList, String strTable, DomainObject ecr, MapList roleList) throws Exception {
        Map<String,StringList> res = new HashMap<>();
        try {
            if ("JFNewECRController".equals(strTable)) {
                StringList ecrSelectList = new StringList();
                ecrSelectList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JFCHANGESOURCE);
                ecrSelectList.add(SELECT_CURRENT);
                ecrSelectList.add("from[JFChange2Project].to.id");
                Map ecrMap = ecr.getInfo(context, ecrSelectList);
//                String strChangeSource = (String) ecrMap.get(JF_PLMConstants_mxJPO.SELECT_ATTR_JFCHANGESOURCE);
                String strCurrent = (String) ecrMap.get(SELECT_CURRENT);
                String strProjectId = (String) ecrMap.get("from[JFChange2Project].to.id");
                //不可编辑的id集合
//                Set notEditAccessIdSet = new HashSet<String>();
                // 1.ECR的变更来源来控制权限
                for (int i = 0; i < tableList.size();i++) {
                    Map objMap = (Map) tableList.get(i);
                    String strChangeSource = (String)objMap.get("to[JFRelateItem].attribute[JFChangeSource]");
                    String strDirectBuy = (String) objMap.get(JF_PLMConstants_mxJPO.SELECT_ATTR_JFDIRECT_BUY);
                    String strPartType = (String) objMap.get(JF_PLMConstants_mxJPO.SELECT_ATTR_JFPartType);
                    String strPartId = (String) objMap.get(SELECT_ID);
                    String strPartRelId = (String) objMap.get(SELECT_RELATIONSHIP_ID);
                    String strProcurementType = (String) objMap.get(JF_PLMConstants_mxJPO.SELECT_ATTR_JF_ProcurementType);
                    String strLevel = (String) objMap.get(SELECT_LEVEL);
                    StringBuilder sbUniqueId = new StringBuilder() ;
                    if (UIUtil.isNotNullAndNotEmpty(strPartId)){
                        sbUniqueId.append(strPartId);
                    }
                    if (UIUtil.isNotNullAndNotEmpty(strPartRelId)){
                        sbUniqueId.append(",");
                        sbUniqueId.append(strPartRelId);
                    }
                    String strUniqueId = sbUniqueId.toString();
                    //先判断该零件是否是自制件且是整椅、面套、发泡
                    //面套发泡一级件才可编辑
//                    Boolean proEditAccess = JF_NewECRService_mxJPO.checkPartIsMakeAndFoamCoating(context, objMap, "");
                    if (objMap.containsKey("rowEditFlag")) {
                        String strEditFlag = (String) objMap.get("rowEditFlag");
                        boolean isSkip = JF_NewECRService_mxJPO.checkSkipSunTreeByRoleAndPartType(roleList, strPartType);
                        if (isSkip){
                            roleFor:
                            for (int i1 = 0; i1 < roleList.size(); i1++) {
                                Map taskRoleMap = (Map) roleList.get(i1);
                                String strRoleValue = (String) taskRoleMap.get("role");
                                Boolean isEdit = Boolean.FALSE;
                                // 获取table里面的必填属性
                                StringList canEditFieldList = JF_NewECRService_mxJPO.getRequireEditFieldInRoleAndLinkField(false, strRoleValue, strDirectBuy, strProcurementType, strPartType,strChangeSource,strEditFlag);
                                // add by chenyan 2025/06/17  如果是财务BP的话添加卷积的属性  不往 getRequireEditFieldInRoleAndLinkField添加 防止提交校验失败
                                String strRelationship = UIUtil.getValue(objMap, JF_PLMConstants_mxJPO.RELATIONSHIP);
                                if (JF_PLMConstants_mxJPO.REL_JFECRRelateRoot.equals(strRelationship)) {
                                    //卷积属性只能  JFECRRelateRoot 且是供货件才可以编辑
                                    if (objMap.containsKey("isZeroPart")){
                                        boolean isZeroPart = (boolean)objMap.get("isZeroPart");
                                        isEdit = isZeroPart ;
                                    }
                                }
                                if (strRoleValue.equals(JF_PLMConstants_mxJPO.ATTR_PROJECT_ROLE_Range_FinancialBP) && isEdit){
                                    if (JF_PLMConstants_mxJPO.ATTR_JFCHANGESOURCE_RANGE_InternalChanges.equals(strChangeSource)){
                                        canEditFieldList.add(JF_PLMConstants_mxJPO.ATTR_JFChangeWholeSeatPrice);
                                    }else if (JF_PLMConstants_mxJPO.ATTR_JFCHANGESOURCE_RANGE_ExternalChanges.equals(strChangeSource)){
                                        canEditFieldList.add(JF_PLMConstants_mxJPO.ATTR_JFChangeWholeSeatPriceExternal);
                                    }else if (JF_PLMConstants_mxJPO.ATTR_JFCHANGESOURCE_RANGE_BOTH.equals(strChangeSource)){
                                        canEditFieldList.add(JF_PLMConstants_mxJPO.ATTR_JFChangeWholeSeatPrice);
                                        canEditFieldList.add(JF_PLMConstants_mxJPO.ATTR_JFChangeWholeSeatPriceExternal);
                                    }
                                }
                                _logger.info("canEditFieldList1:{}", canEditFieldList);
                                StringList canEditAttrList  = null;
                                if (res.containsKey(strUniqueId)){
                                    canEditAttrList = res.get(strUniqueId);
                                }
                                if (Objects.isNull(canEditAttrList)){
                                    canEditAttrList = canEditFieldList ;
                                    res.put(strUniqueId,canEditAttrList);
                                }else {
                                    canEditAttrList.addAll(canEditFieldList);
                                }
                            }
                        }
                    }
                }

            } else if ("JFNewECRCosting".equals(strTable)) {
                for (int i = 0; i < tableList.size();i++) {
                    Map objMap = (Map) tableList.get(i);
                    String strDirectBuy = (String) objMap.get(JF_PLMConstants_mxJPO.SELECT_ATTR_JFDIRECT_BUY);
                    String strPartType = (String) objMap.get(JF_PLMConstants_mxJPO.SELECT_ATTR_JFPartType);
                    String strPartId = (String) objMap.get(SELECT_ID);
                    String strPartRelId = (String) objMap.get(SELECT_RELATIONSHIP_ID);
                    String strProcurementType = (String) objMap.get(JF_PLMConstants_mxJPO.SELECT_ATTR_JF_ProcurementType);
                    String strChangeSource = (String)objMap.get("to[JFRelateItem].attribute[JFChangeSource]");
                    StringBuilder sbUniqueId = new StringBuilder() ;
                    if (UIUtil.isNotNullAndNotEmpty(strPartId)){
                        sbUniqueId.append(strPartId);
                    }
                    if (UIUtil.isNotNullAndNotEmpty(strPartRelId)){
                        sbUniqueId.append(",");
                        sbUniqueId.append(strPartRelId);
                    }
                    String strUniqueId = sbUniqueId.toString();
                    if (objMap.containsKey("rowEditFlag")){
                        roleFor:
                        for (int i1 = 0; i1 < roleList.size(); i1++) {
                            Map taskRoleMap = (Map) roleList.get(i1);
                            String strRoleValue = (String) taskRoleMap.get("role");
                            _logger.info("strRoleValue:{}",strRoleValue);
                            // 获取table里面的必填属性
                            StringList canEditFieldList = JF_NewECRService_mxJPO.getRequireEditFieldInRoleAndLinkField(true, strRoleValue, strDirectBuy, strProcurementType, strPartType,strChangeSource,"");
                            _logger.info("canEditFieldList1:{}", canEditFieldList);
                            StringList canEditAttrList  = null;
                            if (res.containsKey(strUniqueId)){
                                canEditAttrList = res.get(strUniqueId);
                            }
                            if (Objects.isNull(canEditAttrList)){
                                canEditAttrList = canEditFieldList ;
                                res.put(strUniqueId,canEditAttrList);
                            }else {
                                canEditAttrList.addAll(canEditFieldList);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            _logger.error(e.getMessage());
            throw e;
        }
        return res;
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
            _logger.error("时区转换异常 返回 默认值 0 :{}", e.getMessage());
            dRes = 0;
        }
        return dRes;
    }

    /**
     * @param locale                 时区
     * @param strDisplayDate         显示时间
     * @param iDateTimeDisplayFormat 格式化样式
     * @return java.lang.String
     * @throws
     * @description 根据显示时间转换为数据库时间
     * @author CHENYAN
     * @date 2024/12/25 20:07
     */
    public static String getDbDateByDisplayDate(Locale locale, String strDisplayDate, int iDateTimeDisplayFormat) throws Exception {
        String strDbDate = strDisplayDate;
        try {
            //不符合获取达索的格式化值
            if (iDateTimeDisplayFormat > 3 || iDateTimeDisplayFormat < 0) {
                iDateTimeDisplayFormat = eMatrixDateFormat.iDateTimeDisplayFormat;
            }
            DateFormat dateFormat = DateFormat.getDateTimeInstance(iDateTimeDisplayFormat, iDateTimeDisplayFormat, locale);
            Date date = dateFormat.parse(strDisplayDate);
            SimpleDateFormat matrixSdf = new SimpleDateFormat(eMatrixDateFormat.getEMatrixDateFormat());
            strDbDate = matrixSdf.format(date);
        } catch (Exception e) {
            _logger.error("显示时间转换数据库时间异常：{}", e.getMessage());
            throw e;
        }
        return strDbDate;
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
}


