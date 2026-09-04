import com.alibaba.fastjson.JSON;
import com.google.gson.Gson;
import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.DomainRelationship;
import com.matrixone.apps.domain.util.*;
import com.matrixone.apps.framework.ui.UIUtil;
import com.matrixone.apps.program.ProgramCentralConstants;
import com.matrixone.jdl.MatrixSession;
import matrix.db.*;
import matrix.util.StringList;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItem;
import org.apache.commons.text.StringEscapeUtils;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.io.File;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Set;
import java.util.*;
import java.util.stream.Collectors;

import static com.matrixone.apps.domain.DomainConstants.*;
import static com.matrixone.apps.domain.MultiValueSelects.RELATIONSHIP_REFERENCE_DOCUMENT;

/**
 * @author CHENYAN
 * @version v1.0.0
 * 创建时间：2024/8/1 09:23
 * @description  ECR接口相关
 */
public class JF_ECRRESTService_mxJPO {
    private static final Logger _logger = LoggerFactory.getLogger(JF_ECRRESTService_mxJPO.class);
    public static final String FolderWIN 	= "c:\\temp\\";
    public static final String FolderUNIX 	= "/tmp/";
    public  StringList typeSelectList = new StringList();
    public static final StringList EDIT_ECRCosting_TABLE_ROLE_LIST = new StringList();
    public static final StringList EDIT_ECRController_TABLE_ROLE_LIST = new StringList();

    //AME编辑form是否展示零件属性值
    public static final StringList EDIT_AME_FORM_FIELD_LIST = new StringList();

    //ECR 会签状态之前所有状态
    public static final  StringList ECR_STATE_SIGN_BEFORE_List = new StringList();

    private  static  final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    public static Map<String,String> ROLE_MAPPING_MAP = new HashMap();
    private ZoneId zoneId ;
    //初始化 range值映射表
    static {
        ROLE_MAPPING_MAP.put("PRR","Purchasing representative");
        ROLE_MAPPING_MAP.put("AME","AME representative");
        ROLE_MAPPING_MAP.put("AQE","AQE representative/PQL");
        ROLE_MAPPING_MAP.put("LOR","Logistics representative");
        ROLE_MAPPING_MAP.put("Launch_Manager","Launch manager");
        ROLE_MAPPING_MAP.put("Costing","Costing");
        ROLE_MAPPING_MAP.put("BUR","Business manager");
        ROLE_MAPPING_MAP.put("APR","Financial BP");
        ROLE_MAPPING_MAP.put("ICO","Internal Supplier");
        ECR_STATE_SIGN_BEFORE_List.add("Create");
        ECR_STATE_SIGN_BEFORE_List.add("Submit");
        ECR_STATE_SIGN_BEFORE_List.add("Review");
        //初始化可以下载和上传ECR表格清单角色
        EDIT_ECRCosting_TABLE_ROLE_LIST.add("PRR");
        EDIT_ECRCosting_TABLE_ROLE_LIST.add("Costing");
        EDIT_ECRCosting_TABLE_ROLE_LIST.add("ICO");
//        EDIT_ECRCosting_TABLE_ROLE_LIST.add("APR");
        EDIT_ECRController_TABLE_ROLE_LIST.add("APR");
        EDIT_ECRController_TABLE_ROLE_LIST.add("AME");
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

    }
    public static String getRoleValueByKey(String strRole){
        //转换角色的key
        if (ROLE_MAPPING_MAP.containsKey(strRole)){
            return ROLE_MAPPING_MAP.get(strRole);
        }
        return  "";
    }
    public static String getRoleKeyByValue(String strRoleValue){
        //转换角色的key
        if (ROLE_MAPPING_MAP.containsValue(strRoleValue)){
            for (Map.Entry<String,String>  entry : ROLE_MAPPING_MAP.entrySet()){
                String strKey = entry.getKey();
                String strValue = entry.getValue();
                if (strRoleValue.equals(strValue)){
                    return strKey;
                }
            }
        }
        return  "";
    }
    /**
    *
    *@description 测试上传文件
    *@param context
	*@param args
    *@return java.lang.String
    *@throws
    *@author CHENYAN
    *@date 2024/8/1 9:28
    */
    public String uploadFile(Context context,String[] args) throws Exception{
        _logger.info("----------------------- uploadFile begin -----------------------------------");
        Map reqMap = (Map) JPO.unpackArgs(args);
        //ECRId
        String strObjectId = (String)reqMap.get("objectId");
        _logger.info("strObjectId:{}",strObjectId);
        //关联文档和对象 关系属性 标识8个表单上传
        String strType = (String)reqMap.get("type");
        String strRelType = (String)reqMap.get("relationship");
        //请求基地址
        String strHostUrl = (String)reqMap.get("hostUrl");
        List files = (List) reqMap.get("fileList");
        List<FileItem> fileItems = new ArrayList<>();
        //将文件写入
        for(int i=0;i<files.size();i++) {
            String strTmpPath = context.getWorkspacePath();
            _logger.info("strTmpPath:{}",strTmpPath);
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
        _logger.info("fileItem:{}",fileItems);
        _logger.info("files:{}",files);
        _logger.info("strObjectId:{}",strObjectId);
        _logger.info("strRelType:{}",strRelType);
        _logger.info("strObjectId:{}",strObjectId);
        _logger.info("strHostUrl:{}",strHostUrl);
        Map parameterMap = initParameterMap(context, strObjectId, strRelType, strHostUrl, strType, fileItems);
        String initargs[] = {};
        String sResult = (String)JPO.invoke(context, "JF_FileUtils", initargs, "checkinFile", JPO.packArgs (parameterMap), String.class);
        _logger.info("----------------------- uploadFile end  -----------------------------------");
        return sResult;
    }
    /**
    *
    *@description 获取ECR的基础信息
    *@param context
	*@param args
    *@return java.lang.String
    *@throws
    *@author CHENYAN
    *@date 2024/8/1 14:27
    */
    public String getECRInfo(Context context,String[] args) throws Exception{
        Map reqMap = (Map) JPO.unpackArgs(args);
        String strObjectId = (String) reqMap.get("objectId");
        String strHostUrl = (String) reqMap.get("hostUrl");
        _logger.info("strObjectId:{}",strObjectId);
        _logger.info("strHostUrl:{}",strHostUrl);
        Map res = new HashMap<String,Object>();
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
            ECR.put("current",strCurrent);
            ECR.put("JFChangeSource",strChangeSource);
            //SDT-采购代表
            HashMap<String, Object> PRR = new HashMap<>();
            //SDT-AME代表
            HashMap<String, Object> AME = new HashMap<>();
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
            MapList loginUserSignTaskRole = new MapList() ;
            //标识是否生成AME会签任务
            boolean isCreateAMTSignTask = false;
            //还没有生成会签任务
            if (ECR_STATE_SIGN_BEFORE_List.contains(strCurrent)){
                initECRAttrValue(PRR,null,"PRR");
                initECRAttrValue(AME,null,"AME");
                initECRAttrValue(AQE,null,"AQE");
                initECRAttrValue(LOR,null,"LOR");
                initECRAttrValue(Launch_Manager,null,"Launch_Manager");
                initECRAttrValue(Costing,null,"Costing");
                initECRAttrValue(BUR,null,"BUR");
                initECRAttrValue(APR,null,"APR");
                initECRFormEditAccess("PRR",PRR,null);
                initECRFormEditAccess("AME",AME,null);
                initECRFormEditAccess("AQE",AQE,null);
                initECRFormEditAccess("LOR",LOR,null);
                initECRFormEditAccess("Launch_Manager",Launch_Manager,null);
                initECRFormEditAccess("Costing",Costing,null);
                initECRFormEditAccess("BUR",BUR,null);
                initECRFormEditAccess("APR",APR,null);
                initECRFormEditAccess("ICO",ICO,null);
                res.put("login_role",new HashSet<>());
            }else {
                //角色不为空的会签任务
                loginUserSignTaskRole = JF_PublicMethodClass_mxJPO.getLoginUserSignTaskRoleByECRAndUser(context, new String[]{strObjectId,strLoginUser});
                _logger.info("loginUserSignTaskRole:{}",loginUserSignTaskRole);
                Map ecrInfo = ecr.getInfo(context, typeSelectList);
                initECRAttrValue(PRR,ecrInfo,"PRR");
                initECRAttrValue(AME,ecrInfo,"AME");
                initECRAttrValue(AQE,ecrInfo,"AQE");
                initECRAttrValue(LOR,ecrInfo,"LOR");
                initECRAttrValue(Launch_Manager,ecrInfo,"Launch_Manager");
                initECRAttrValue(Costing,ecrInfo,"Costing");
                initECRAttrValue(BUR,ecrInfo,"BUR");
                initECRAttrValue(APR,ecrInfo,"APR");
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
                _logger.info("files:{}",files);
                //ECR上传附件分组
                Map group = (Map) files.stream().filter(m ->{
                    Map file = (Map)m;
                    return UIUtil.isNotNullAndNotEmpty((String) file.get(JF_PLMConstants_mxJPO.SELECT_ATTR_PROJECT_ROLE));
                }).collect(Collectors.groupingBy(m ->{
                    Map file = (Map)m;
                    return file.get(JF_PLMConstants_mxJPO.SELECT_ATTR_PROJECT_ROLE);
                }));
                _logger.info("group:{}",group);
                Set<String> fileKeySet = ROLE_MAPPING_MAP.keySet();
                for (String strKey : fileKeySet) {
                    MapList fileMapList = classFilesInECRFiles(context, group, strKey, strHostUrl, loginUserSignTaskRole);
                    if ("PRR".equals(strKey)){
                        PRR.put("files",fileMapList);
                        initECRFormEditAccess(strKey,PRR,loginUserSignTaskRole);
                    }else if ("AME".equals(strKey)){
                        AME.put("files",fileMapList);
                        initECRFormEditAccess(strKey,AME,loginUserSignTaskRole);
                    }else if ("AQE".equals(strKey)){
                        AQE.put("files",fileMapList);
                        initECRFormEditAccess(strKey,AQE,loginUserSignTaskRole);
                    }else if ("LOR".equals(strKey)){
                        LOR.put("files",fileMapList);
                        initECRFormEditAccess(strKey,LOR,loginUserSignTaskRole);
                    }else if ("Launch_Manager".equals(strKey)){
                        Launch_Manager.put("files",fileMapList);
                        initECRFormEditAccess(strKey,Launch_Manager,loginUserSignTaskRole);
                    }else if ("Costing".equals(strKey)){
                        Costing.put("files",fileMapList);
                        initECRFormEditAccess(strKey,Costing,loginUserSignTaskRole);
                    }else if ("BUR".equals(strKey)){
                        BUR.put("files",fileMapList);
                        initECRFormEditAccess(strKey,BUR,loginUserSignTaskRole);
                    }else if ("APR".equals(strKey)){
                        APR.put("files",fileMapList);
                        initECRFormEditAccess(strKey,APR,loginUserSignTaskRole);
                    }else if ("ICO".equals(strKey)){
                        ICO.put("files",new MapList());
                        initECRFormEditAccess(strKey,ICO,loginUserSignTaskRole);
                    }
                }
                Set roleSet = getRoleKeyListByMapList(loginUserSignTaskRole);
                res.put("login_role",roleSet);
                isCreateAMTSignTask = true;
            }
            //获取ECR底部按钮权限
            Map footerMenuAccess = getFooterMenuAccess(loginUserSignTaskRole,strLoginUser,strOwner,strCurrent,strProjectOwner);
            res.put("footerMenuAccess",footerMenuAccess);
            //获取所有的会签任务
            MapList allSignTaskInECR = getAllSignTaskInECR(context, ecr);
            //构造获取采购件清单参数
            HashMap<String, String> stringStringHashMap = new HashMap<>();
            stringStringHashMap.put("objectId",strObjectId);
            stringStringHashMap.put("expandLevel","0");
            String initargs[] = {};
            //获取自制件清单
            MapList makeTableList = (MapList) JPO.invoke(context, "JF_ECRService", initargs, "getECRMakeTableData", JPO.packArgs (stringStringHashMap), MapList.class);
            Map ameEditFieldAccess = getAMEEditFieldAccess(isCreateAMTSignTask, makeTableList);
            res.put("AMEEditFiled",ameEditFieldAccess);
            res.put("signTaskList",allSignTaskInECR);
            res.put("PRR",PRR);
            res.put("AME",AME);
            res.put("AQE",AQE);
            res.put("LOR",LOR);
            res.put("Launch_Manager",Launch_Manager);
            res.put("Costing",Costing);
            res.put("BUR",BUR);
            res.put("APR",APR);
            res.put("ECR",ECR);
            res.put("ICO",ICO);
            res.put("code","200");
        }catch (Exception e){
            _logger.info(e.getMessage());
            res.put("code","404");
        }finally {
            ContextUtil.popContext(context);
        }
        _logger.info("map:{}",res);
        Gson gson = new Gson();
        String strGson = gson.toJson(res);
        return strGson;
    }

    /**
    *
    *@description 获取所有的会签任务
    *@param context
	*@param ecr
    *@return com.matrixone.apps.domain.util.MapList
    *@throws
    *@author CHENYAN
    *@date 2024/8/16 10:32
    */
    public static MapList getAllSignTaskInECR(Context context ,DomainObject ecr) throws Exception{
        StringList typeSelectList = new StringList();
        typeSelectList.add(DomainConstants.SELECT_ID);
        typeSelectList.add(DomainConstants.SELECT_CURRENT);
        typeSelectList.add(SELECT_ATTRIBUTE_TITLE);
        typeSelectList.add(SELECT_OWNER);
        typeSelectList.add("state[Review].actual");
        typeSelectList.add("state[Complete].actual");
        typeSelectList.add(SELECT_DESCRIPTION);
        StringList relSelectList = new StringList(SELECT_RELATIONSHIP_ID);
        Locale local = context.getLocale();
        //时区
        String strTimeZone = context.getTimezone();
        _logger.info("strTimeZone:{}",strTimeZone);
        //获取到时区相对偏移量 -8 , 8
        double dTimeZoneId = convertOffsetToDouble(strTimeZone);
        _logger.info("dTimeZoneId:{}",dTimeZoneId);
        MapList maps = new MapList();
        //只获取会签状态状态的会签任务，排除掉APR和商务经理
        maps = ecr.getRelatedObjects(context, JF_PLMConstants_mxJPO.RELATIONSHIP_JF_ECR_TASK , // relationship pattern
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
            signTaskMap.put("title",(String) signTaskMap.get(SELECT_ATTRIBUTE_TITLE));
            String strOwner = PersonUtil.getFullName(context,(String) signTaskMap.get(SELECT_OWNER));
            //设置全名
            if (UIUtil.isNotNullAndNotEmpty(strOwner)){
                signTaskMap.put(SELECT_OWNER,strOwner);
            }
            String strCurrent  = (String) signTaskMap.get(SELECT_CURRENT);
            String strNlsCurrent;
            if ("Review".equals(strCurrent)){
                String strNls = EnoviaResourceBundle.getProperty(context, "emxFrameworkStringResource", context.getLocale(), "emxFramework.Policy.ECR.APR.Review");
                strNlsCurrent = strNls;
            }else {
                strNlsCurrent = EnoviaResourceBundle.getStateI18NString(context, "Project Task",strCurrent, context.getLocale().toString());
            }
            signTaskMap.put("currentNls",strNlsCurrent);
            signTaskMap.put("APRReviewNotes",UIUtil.getValue(signTaskMap,SELECT_DESCRIPTION));
            if ("Complete".equals(strCurrent) || "Review".equals(strCurrent)){
                String strSubmitTime = UIUtil.getValue(signTaskMap, "state[Review].actual");
                String strCompleteTime = UIUtil.getValue(signTaskMap, "state[Complete].actual");
                strSubmitTime = eMatrixDateFormat.getFormattedDisplayDateTime( strSubmitTime, true, 2, dTimeZoneId,local);
                strCompleteTime = eMatrixDateFormat.getFormattedDisplayDateTime( strCompleteTime, true, 2, dTimeZoneId,local);
                //时间戳
                signTaskMap.put("submitTime",strSubmitTime);
                signTaskMap.put("completeTime",strCompleteTime);
            }else {
                signTaskMap.put("submitTime","");
                signTaskMap.put("completeTime","");
            }
        }
        return maps;
    }

    /**
    *
    *@description 获取到当前登录人的会签角色
    *@param roleList signTask 集合
    *@return java.util.Set
    *@throws
    *@author CHENYAN
    *@date 2024/8/7 10:58
    */
    public static Set getRoleKeyListByMapList(MapList roleList){
        Set<String> res = new HashSet();
        for (int i = 0; i < roleList.size(); i++) {
            Map roleMap = (Map) roleList.get(i);
            String strRoleName = (String) roleMap.get("role");
            _logger.info("strRoleName:{}",strRoleName);
            for (Map.Entry<String, String> entry : ROLE_MAPPING_MAP.entrySet()) {
                if (entry.getValue().equals(strRoleName)){
                    res.add(entry.getKey());
                }
            }
        }
        return res;
    }
    /**
    *
    *@description 初始化表单权限
    *@param strType  表单类型
	*@param formMap  表单map
	*@param currentRoleList 当前用户的对象角色会签任务
    *@return void
    *@throws
    *@author CHENYAN
    *@date 2024/8/5 9:13
    */
    public void initECRFormEditAccess(String strType ,Map formMap ,MapList currentRoleList){
        if (ROLE_MAPPING_MAP.containsKey(strType)) {
            String strRoleValue = ROLE_MAPPING_MAP.get(strType);
            if (null== currentRoleList || currentRoleList.size() == 0){
                formMap.put("editAccess",Boolean.FALSE);
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
                if (ROLE_MAPPING_MAP.get("BUR").equals(strRole) && strRoleValue.equals(strRole)){
                        _logger.info("isExecCustomer:{}",isExecCustomer);
                    if (!isExecCustomer){
                        // 标识客户报价权限已经处理 APR报价任务处理后直接退出
                        if (JF_PLMConstants_mxJPO.TYPE_JF_CustomerTask.equals(strTaskType)){
                            isExecCustomer = true;
                            if ((!("Complete".equals(strTaskCurrent) || "Review".equals(strTaskCurrent)))){
                                formMap.put("editAccess",Boolean.TRUE);
                                _logger.info("strTaskId:{}",strTaskId);
                                _logger.info("strTaskCurrent:{}",strTaskCurrent);
                                formMap.put("signTaskId",strTaskId);
                                formMap.put("signTaskCurrent",strTaskCurrent);
                            }else {
                                formMap.put("editAccess",Boolean.FALSE);
                            }
                            _logger.info("formMap:{}",formMap);
                            break;
                        }else {
                            formMap.put("editAccess",Boolean.FALSE);
                            formMap.put("signTaskId",strTaskId);
                            formMap.put("signTaskCurrent",strTaskCurrent);
                        }

                    }
                }else {
                    if (strRoleValue.equals(strRole) && (!("Complete".equals(strTaskCurrent) || "Review".equals(strTaskCurrent)))){
                        formMap.put("editAccess",Boolean.TRUE);
                        _logger.info("strTaskId:{}",strTaskId);
                        _logger.info("strTaskCurrent:{}",strTaskCurrent);
                        formMap.put("signTaskId",strTaskId);
                        formMap.put("signTaskCurrent",strTaskCurrent);
                    }else {
                        formMap.put("editAccess",Boolean.FALSE);
                    }
                }

            }
        }
    }
    /**
    *
    *@description 根据 ECR附件，和当前登录人角色 分类文件供前端显示和操作
    *@param context
	*@param groupFiles  分钟后的权限
	*@param strType ECR 表单类型 PPR BUR 等
	*@param strHostUrl 请求基地址
	*@param currentRoleList 用户当前会签任务角色
    *@return com.matrixone.apps.domain.util.MapList
    *@throws
    *@author CHENYAN
    *@date 2024/8/4 14:48
    */
    public MapList classFilesInECRFiles(Context context ,Map groupFiles ,String strType ,String strHostUrl,MapList currentRoleList){
        MapList res = new MapList();
        if (ROLE_MAPPING_MAP.containsKey(strType)) {
            String strRoleValue = ROLE_MAPPING_MAP.get(strType);
            if (groupFiles.containsKey(strType)){
                List files = (List) groupFiles.get(strType);
                if (null != files && files.size() > 0){
                    //找到当前角色的文件
                    //1.判断当前文件状态是否之前删除
                    for (int i = 0; i < files.size(); i++) {
                        //返回Map
                        HashMap resMap = new HashMap<>();
                        Map file = (Map) files.get(i);
                        String strCurrent = (String) file.get(SELECT_CURRENT);
                        //前端文档操作权限
                        Boolean fileActionAccess = Boolean.TRUE;
                        if (checkRoleContain(strRoleValue,currentRoleList)) {
                            //发布和冻结文档不可操作
                            if ("RELEASED".equals(strCurrent) || "OBSOLETE".equals(strCurrent)){
                                fileActionAccess = Boolean.FALSE;
                            }
                        }
                        resMap.put("delAccess",fileActionAccess);
                        resMap.put("objectId",file.get(SELECT_ID));
                        resMap.put("relId",file.get(SELECT_RELATIONSHIP_ID));
                        resMap.put("current",file.get(SELECT_CURRENT));
                        resMap.put("title", StringEscapeUtils.escapeHtml4((String) file.get(SELECT_ATTRIBUTE_TITLE)));
                        StringBuffer sbHref = new StringBuffer();
                        sbHref.append(strHostUrl);
                        sbHref.append("/3dspace/common/emxTree.jsp?mode=insert&relId=null&parentOID=null&jsTreeID=null&objectId=");
                        sbHref.append(file.get(SELECT_ID));
                        sbHref.append("&suiteKey=Components&emxSuiteDirectory=components");
                        resMap.put("href",sbHref.toString());
                        res.add(resMap);
                    }
                }
            }
        }
        return res;
    }
    public static boolean checkRoleContain(String strRoleValue ,MapList currentRoleList){
        if (null != currentRoleList && currentRoleList.size() > 0){
            for (int i = 0; i < currentRoleList.size(); i++) {
                Map currentRole = (Map) currentRoleList.get(i);
                String strRole = (String) currentRole.get("role");
                return strRole.equals(strRoleValue);
            }
        }
        return false;
    }

    /**
    *
    *@description 初始化调用创建文档和连接关系参数
    *@param context
	*@param strObjectId  需要连接对象id
	*@param strRelType 关系名
	*@param files 文档关联文件
    *@return java.util.Map
    *@throws
    *@author CHENYAN
    *@date 2024/8/1 13:17
    */
    public  static  Map  initParameterMap(Context context, String strObjectId, String strRelType ,String strHostUrl ,String strType, List files) throws Exception{
        String sOSName 		= System.getProperty ( "os.name" );
        String sFolder		= sOSName.contains("Windows") ? FolderWIN : FolderUNIX ;
        String sTmpDir		= Environment.getValue(context, "TMPDIR");
        String separator 	= sOSName.contains("Windows") ? "\\" : "/";
        if(null != sTmpDir && !sTmpDir.trim().isEmpty())
        {
            sFolder = sTmpDir;
            if(!sFolder.substring(sFolder.length() - 1).equals(separator))
                sFolder = sFolder + separator;
        }
        MatrixSession session = context.getSession();
        HashMap params = new HashMap();
        params.put("language",session.getLanguage());
        params.put("objectId",strObjectId);
        params.put("type",strType);
        params.put("hostUrl",strHostUrl);
        params.put("relationship",strRelType);
        params.put("documentCommand","");
        params.put("folder",sFolder);
        params.put("files",files);
        params.put("objectAction","create");
        params.put("timezone", "");
        return  params;
    }


    /**
    *
    *@description 删除文档
    *@param context
	*@param args
    *@return java.lang.String
    *@throws
    *@author CHENYAN
    *@date 2024/8/6 17:29
    */
    public String delDocument(Context context,String[] args) throws Exception{
        Map reqMap = (Map) JPO.unpackArgs(args);
        String strObjectId = (String) reqMap.get("objectId");
        String strType = (String) reqMap.get("type");
        String strRelId = (String) reqMap.get("relId");
        _logger.info("strObjectId:{}",strObjectId);
        _logger.info("strType:{}",strType);
        _logger.info("strRelId:{}",strRelId);
        HashMap<String, String> res = new HashMap<>();
        Gson gson = new Gson();
        if (UIUtil.isNotNullAndNotEmpty(strRelId)){
            try{
                ContextUtil.pushContext(context);
                ContextUtil.startTransaction(context,true);
                DomainRelationship.disconnect(context,strRelId);
                ContextUtil.commitTransaction(context);
                res.put("code","200");
            }catch (Exception e){
                _logger.error(e.getMessage());
                ContextUtil.abortTransaction(context);
                res.put("code","404");
            }finally {
                ContextUtil.popContext(context);
                return gson.toJson(res);
            }
        }
        return gson.toJson(res);
    }

    /**
    *
    *@description 初始化ECR的属性
    *@param targetMap  初始化map
	*@param sourceMap 元数据map
	*@param strType 表单类型
    *@return void
    *@throws
    *@author CHENYAN
    *@date 2024/8/6 17:29
    */
    public static void initECRAttrValue(Map targetMap ,Map sourceMap ,String strType){
        switch (strType){
            case "PRR" :{
                if (null != sourceMap){
                    targetMap.put("JFChangeCyclesOutsourcing",UIUtil.getValue(sourceMap,JF_PLMConstants_mxJPO.SELECT_ATTR_JFChangeCyclesOutsourcing));
                    targetMap.put("JFChangeDevelopmentCostsOutsourcing",UIUtil.getValue(sourceMap,JF_PLMConstants_mxJPO.SELECT_ATTR_JFChangeDevelopmentCostsOutsourcing));
                    targetMap.put("JFSupplierOrScrapDepotOutsourcing",UIUtil.getValue(sourceMap,JF_PLMConstants_mxJPO.SELECT_ATTR_JFSupplierOrScrapDepotOutsourcing));
                    targetMap.put("JFChangeCyclesSelfMake",UIUtil.getValue(sourceMap,JF_PLMConstants_mxJPO.SELECT_ATTR_JFChangeCyclesSelfMake));
                    targetMap.put("JFChangeDevelopmentCostsSelfMake",UIUtil.getValue(sourceMap,JF_PLMConstants_mxJPO.SELECT_ATTR_JFChangeDevelopmentCostsSelfMake));
                    targetMap.put("JFSupplierOrScrapDepotSelfMake",UIUtil.getValue(sourceMap,JF_PLMConstants_mxJPO.SELECT_ATTR_JFSupplierOrScrapDepotSelfMake));
                }else {
                    targetMap.put("JFChangeCyclesOutsourcing","");
                    targetMap.put("JFChangeDevelopmentCostsOutsourcing","");
                    targetMap.put("JFSupplierOrScrapDepotOutsourcing","");
                    targetMap.put("JFChangeCyclesSelfMake","");
                    targetMap.put("JFChangeDevelopmentCostsSelfMake","");
                    targetMap.put("JFSupplierOrScrapDepotSelfMake","");
                }
               break;
            }
            case "AME" :{
                if (null != sourceMap){
                    targetMap.put("JFChangesInvestmentsWholeChair",UIUtil.getValue(sourceMap,JF_PLMConstants_mxJPO.SELECT_ATTR_JFChangesInvestmentsWholeChair));
                    targetMap.put("JFChangesInvestmentsFoaming",UIUtil.getValue(sourceMap,JF_PLMConstants_mxJPO.SELECT_ATTR_JFChangesInvestmentsFoaming));
                    targetMap.put("JFChangesInvestmentsFaceCovers",UIUtil.getValue(sourceMap,JF_PLMConstants_mxJPO.SELECT_ATTR_JFChangesInvestmentsFaceCovers));
                    targetMap.put("JFChangesInvestmentsWholeChairExternal",sourceMap.get(JF_PLMConstants_mxJPO.SELECT_ATTR_JFChangesInvestmentsWholeChairExternal));
                    targetMap.put("JFChangesInvestmentsFoamingExternal",sourceMap.get(JF_PLMConstants_mxJPO.SELECT_ATTR_JFChangesInvestmentsFoamingExternal));
                    targetMap.put("JFChangesInvestmentsFaceCoversExternal",sourceMap.get(JF_PLMConstants_mxJPO.SELECT_ATTR_JFChangesInvestmentsFaceCoversExternal));
                }else {
                    targetMap.put("JFChangesInvestmentsWholeChair","");
                    targetMap.put("JFChangesInvestmentsFoaming","");
                    targetMap.put("JFChangesInvestmentsFaceCovers","");
                    targetMap.put("JFChangesInvestmentsWholeChairExternal","");
                    targetMap.put("JFChangesInvestmentsFoamingExternal","");
                    targetMap.put("JFChangesInvestmentsFaceCoversExternal","");
                }
                break;
            }
            case "AQE" :{
                if (null != sourceMap){
                    targetMap.put("JFChangesFixtures",UIUtil.getValue(sourceMap,JF_PLMConstants_mxJPO.SELECT_ATTR_JFChangesFixtures));
                    targetMap.put("JFCostOfQuality",UIUtil.getValue(sourceMap,JF_PLMConstants_mxJPO.SELECT_ATTR_JFCostOfQuality));
                    targetMap.put("JFCostOfTrial",UIUtil.getValue(sourceMap,JF_PLMConstants_mxJPO.SELECT_ATTR_JFCostOfTrial));
                    targetMap.put("JFChangesFixturesExternal",UIUtil.getValue(sourceMap,JF_PLMConstants_mxJPO.SELECT_ATTR_JFChangesFixturesExternal));
                    targetMap.put("JFCostOfQualityExternal",UIUtil.getValue(sourceMap,JF_PLMConstants_mxJPO.SELECT_ATTR_JFCostOfQualityExternal));
                    targetMap.put("JFCostOfTrialExternal",UIUtil.getValue(sourceMap,JF_PLMConstants_mxJPO.SELECT_ATTR_JFCostOfTrialExternal));
                }else {
                    targetMap.put("JFChangesFixtures","");
                    targetMap.put("JFCostOfQuality","");
                    targetMap.put("JFCostOfTrial","");
                    targetMap.put("JFChangesFixturesExternal","");
                    targetMap.put("JFCostOfQualityExternal","");
                    targetMap.put("JFCostOfTrialExternal","");
                }
                break;
            }
            case "LOR" :{
                if (null != sourceMap){
                    targetMap.put("JFCostPackagingLogistics",UIUtil.getValue(sourceMap,JF_PLMConstants_mxJPO.SELECT_ATTR_JFCostPackagingLogistics));
                    targetMap.put("JFCostPackagingLogisticsExternal",UIUtil.getValue(sourceMap,JF_PLMConstants_mxJPO.SELECT_ATTR_JFCostPackagingLogisticsExternal));
                }else {
                    targetMap.put("JFCostPackagingLogistics","");
                    targetMap.put("JFCostPackagingLogisticsExternal","");
                }
              break;
            }
            case "Launch_Manager" :{
                if (null != sourceMap){
                    targetMap.put("JFMaterialsFinishedProductsRework",UIUtil.getValue(sourceMap,JF_PLMConstants_mxJPO.SELECT_ATTR_JFMaterialsFinishedProductsRework));
                }else {
                    targetMap.put("JFMaterialsFinishedProductsRework","");
                }
              break;
            }
            case "Costing" :{
                if (null != sourceMap){
                    targetMap.put("JFChangesUnitPriceCost",UIUtil.getValue(sourceMap,JF_PLMConstants_mxJPO.SELECT_ATTR_JFChangeCyclesOutsourcing));
                    targetMap.put("JFChangesMouldCost",UIUtil.getValue(sourceMap,JF_PLMConstants_mxJPO.SELECT_ATTR_JFChangesMouldCost));
                    targetMap.put("JFChangesUnitPriceCostExternal",UIUtil.getValue(sourceMap,JF_PLMConstants_mxJPO.SELECT_ATTR_JFChangesUnitPriceCostExternal));
                    targetMap.put("JFChangesMouldCostExternal",UIUtil.getValue(sourceMap,JF_PLMConstants_mxJPO.SELECT_ATTR_JFChangesMouldCostExternal));
                }else {
                    targetMap.put("JFChangesUnitPriceCost","");
                    targetMap.put("JFChangesMouldCost","");
                    targetMap.put("JFChangesUnitPriceCostExternal","");
                    targetMap.put("JFChangesMouldCostExternal","");
                }
              break;
            }
            case "BUR" :{
                if (null != sourceMap){
                    targetMap.put("JFAcknowledgmentContentAmount",UIUtil.getValue(sourceMap,JF_PLMConstants_mxJPO.SELECT_ATTR_JFAcknowledgmentContentAmount));
                }else {
                    targetMap.put("JFAcknowledgmentContentAmount","");
                }
               break;
            }
            case "APR" :{
                if (null != sourceMap){
                    targetMap.put("JFChangesInvestment",UIUtil.getValue(sourceMap,JF_PLMConstants_mxJPO.SELECT_ATTR_JFChangesInvestment));
                    targetMap.put("JFChangesDevelopment",UIUtil.getValue(sourceMap,JF_PLMConstants_mxJPO.SELECT_ATTR_JFChangesDevelopment));
                    targetMap.put("JFChangesMould",UIUtil.getValue(sourceMap,JF_PLMConstants_mxJPO.SELECT_ATTR_JFChangesMould));
                    targetMap.put("JFSumInventoryScrapAmount",UIUtil.getValue(sourceMap,JF_PLMConstants_mxJPO.SELECT_ATTR_JFSumInventoryScrapAmount));
                    targetMap.put("JFChangesInvestmentExternal",UIUtil.getValue(sourceMap,JF_PLMConstants_mxJPO.SELECT_ATTR_JFChangesInvestmentExternal));
                    targetMap.put("JFChangesDevelopmentExternal",UIUtil.getValue(sourceMap,JF_PLMConstants_mxJPO.SELECT_ATTR_JFChangesDevelopmentExternal));
                    targetMap.put("JFChangesMouldExternal",UIUtil.getValue(sourceMap,JF_PLMConstants_mxJPO.SELECT_ATTR_JFChangesMouldExternal));
                    targetMap.put("JFSumInventoryScrapAmountExternal",UIUtil.getValue(sourceMap,JF_PLMConstants_mxJPO.SELECT_ATTR_JFSumInventoryScrapAmountExternal));
                    targetMap.put("JFTestFee",UIUtil.getValue(sourceMap,JF_PLMConstants_mxJPO.SELECT_ATTR_JFTestFee));
                    targetMap.put("JFAPRNotes",UIUtil.getValue(sourceMap,JF_PLMConstants_mxJPO.SELECT_ATTR_JFAPRNotes));
                    targetMap.put("JFOtherFee",UIUtil.getValue(sourceMap,JF_PLMConstants_mxJPO.SELECT_ATTR_JFOtherFee));

                }else {
                    targetMap.put("JFChangesInvestment","");
                    targetMap.put("JFChangesDevelopment","");
                    targetMap.put("JFChangesMould","");
                    targetMap.put("JFSumInventoryScrapAmount","");
                    targetMap.put("JFChangesInvestmentExternal","");
                    targetMap.put("JFChangesDevelopmentExternal","");
                    targetMap.put("JFChangesMouldExternal","");
                    targetMap.put("JFSumInventoryScrapAmountExternal","");
                    targetMap.put("JFTestFee","");
                    targetMap.put("JFAPRNotes","");
                    targetMap.put("JFOtherFee","");
                }
               break;
            }
        }
    }

    /**
    *
    *@description ECR表单保存或者提交按钮
    *@param context
	*@param args
    *@return java.lang.String
    *@throws
    *@author CHENYAN
    *@date 2024/8/6 17:31
    */
    public String saveOrSubmitInECR(Context context,String[] args) throws Exception{
        _logger.info("----------------------------- saveOrSubmitInECR begin ------------------------------------");
        HashMap<Object, Object> res = new HashMap<>();
        Map reqMap = (Map) JPO.unpackArgs(args);
        String strAction = (String) reqMap.get("action");
        String strObjectId = (String) reqMap.get("objectId");
        _logger.info("strObjectId:{}",strObjectId);
        _logger.info("strAction:{}",strAction);
        HashMap attrMap = new HashMap();
        Set<String> signTaskSet = new HashSet<>();
        //复制请求中ECR属性
        copyECRAttribute(attrMap,reqMap,signTaskSet);
        _logger.info("attrMap:{}",attrMap);
        if ("submit".equals(strAction)){
            try {
                ContextUtil.pushContext(context);
                DomainObject ecr = DomainObject.newInstance(context, strObjectId);
                String strCurrent = ecr.getInfo(context, SELECT_CURRENT);
                ContextUtil.startTransaction(context,true);
                ecr.setAttributeValues(context,attrMap);
                //提升状态到审核中
                for (String strSignTaskId : signTaskSet) {
                    DomainObject signTask = DomainObject.newInstance(context, strSignTaskId);
                    StringList signTaskList = new StringList();
                    signTaskList.add(SELECT_TYPE);
                    signTaskList.add(SELECT_CURRENT);
                    Map signTaskInfo  = signTask.getInfo(context, signTaskList);
                    String strTaskCurrent = (String)signTaskInfo.get(SELECT_CURRENT);
                    String strType = (String)signTaskInfo.get(SELECT_TYPE);
                    // APR 任务和 客户报价任务提交直接已完成  会签任务变成审核中
                    if ( !("Complete".equals(strTaskCurrent) || "Review".equals(strTaskCurrent))){
                        if (JF_PLMConstants_mxJPO.TYPE_JF_APRTask.equals(strType) || JF_PLMConstants_mxJPO.TYPE_JF_CustomerTask.equals(strType)){
                            signTask.setState(context,"Complete");
                        }else {
                            signTask.setState(context,"Review");
                        }
                    }
                }
                //判断会签任务是不是已经全部到审核中
                boolean stateIsAllReview = checkSignTaskIsAllReview(context, strObjectId);
                //只有会签状态才提升
                if (stateIsAllReview && "Countersign".equals(strCurrent)){
                    // 提升ECR的状态
//                    ecr.promote(context);
                    ecr.setState(context,"APR");
                }else if ("APR".equals(strCurrent) || "Quotation".equals(strCurrent)){
                    boolean stateIsAllComplete = checkSignTaskIsAllComplete(context, strObjectId);
                    // 提升ECR的状态
                    if (stateIsAllComplete){
                        ecr.promote(context);
                    }
                }
                ContextUtil.commitTransaction(context);
                _logger.info("state :{}",ecr.getInfo(context,SELECT_CURRENT));
                //只要是提交就需要刷新
                res.put("isRefresh",true);
                res.put("code","200");
            }catch (Exception e){
                res.put("code","404");
                ContextUtil.abortTransaction(context);
                _logger.info(e.getMessage());
            }finally {
                ContextUtil.popContext(context);
            }
        }else if ("save".equals(strAction)){
            try {
                ContextUtil.pushContext(context);
                DomainObject ecr = DomainObject.newInstance(context, strObjectId);
                ContextUtil.startTransaction(context,true);
                ecr.setAttributeValues(context,attrMap);
                ContextUtil.commitTransaction(context);
                res.put("code","200");
                res.put("isRefresh",false);
            }catch (Exception e){
                ContextUtil.abortTransaction(context);
                res.put("code","404");
                _logger.info(e.getMessage());
            }finally {
                ContextUtil.popContext(context);
            }
        }
        Gson gson = new Gson();
        _logger.info("----------------------------- saveOrSubmitInECR end ------------------------------------");
        return gson.toJson(res);
    }

    /**
    *
    *@description 复制请求中的ECR属性到新的Map中
    *@param targetMap 复制目标
	*@param resMap 请求修改参数
    *@return void
    *@throws
    *@author CHENYAN
    *@date 2024/8/6 18:29
    */
    public static void copyECRAttribute(Map targetMap ,Map resMap,Set<String> signTaskIdSet){
        Set<String> keySet = ROLE_MAPPING_MAP.keySet();

        for (String strKey : keySet) {
            if (resMap.containsKey(strKey)) {
                Map formMap = (Map) resMap.get(strKey);
                //获取表单对呀的会签任务Id
                String strSignTaskId = UIUtil.getValue(formMap, "signTaskId");
                if (UIUtil.isNotNullAndNotEmpty(strSignTaskId)){
                    signTaskIdSet.add(strSignTaskId);
                }
                switch (strKey){
                    case "PRR" :{
                        String strJFChangeCyclesOutsourcing = UIUtil.getValue(formMap, "JFChangeCyclesOutsourcing");
                        String strJFChangeDevelopmentCostsOutsourcing = UIUtil.getValue(formMap, "JFChangeDevelopmentCostsOutsourcing");
                        String strJFSupplierOrScrapDepotOutsourcing = UIUtil.getValue(formMap, "JFSupplierOrScrapDepotOutsourcing");
                        String strJFChangeCyclesSelfMake = UIUtil.getValue(formMap, "JFChangeCyclesSelfMake");
                        String strJFChangeDevelopmentCostsSelfMake = UIUtil.getValue(formMap, "JFChangeDevelopmentCostsSelfMake");
                        String strJFSupplierOrScrapDepotSelfMake = UIUtil.getValue(formMap, "JFSupplierOrScrapDepotSelfMake");
                        targetMap.put("JFChangeCyclesOutsourcing",strJFChangeCyclesOutsourcing);
                        targetMap.put("JFChangeDevelopmentCostsOutsourcing",strJFChangeDevelopmentCostsOutsourcing);
                        targetMap.put("JFSupplierOrScrapDepotOutsourcing",strJFSupplierOrScrapDepotOutsourcing);
                        targetMap.put("JFChangeCyclesSelfMake",strJFChangeCyclesSelfMake);
                        targetMap.put("JFChangeDevelopmentCostsSelfMake",strJFChangeDevelopmentCostsSelfMake);
                        targetMap.put("JFSupplierOrScrapDepotSelfMake",strJFSupplierOrScrapDepotSelfMake);
                        break;
                    }
                    case "AME" :{
                        String strJFChangesInvestmentsWholeChair = UIUtil.getValue(formMap, "JFChangesInvestmentsWholeChair");
                        String strJFChangesInvestmentsFoaming = UIUtil.getValue(formMap, "JFChangesInvestmentsFoaming");
                        String strJFChangesInvestmentsFaceCovers = UIUtil.getValue(formMap, "JFChangesInvestmentsFaceCovers");
                        String strJFChangesInvestmentsWholeChairExternal = UIUtil.getValue(formMap, "JFChangesInvestmentsWholeChairExternal");
                        String strJFChangesInvestmentsFoamingExternal = UIUtil.getValue(formMap, "JFChangesInvestmentsFoamingExternal");
                        String strJFChangesInvestmentsFaceCoversExternal = UIUtil.getValue(formMap, "JFChangesInvestmentsFaceCoversExternal");
                        targetMap.put("JFChangesInvestmentsWholeChair",strJFChangesInvestmentsWholeChair);
                        targetMap.put("JFChangesInvestmentsFoaming",strJFChangesInvestmentsFoaming);
                        targetMap.put("JFChangesInvestmentsFaceCovers",strJFChangesInvestmentsFaceCovers);
                        targetMap.put("JFChangesInvestmentsWholeChairExternal",strJFChangesInvestmentsWholeChairExternal);
                        targetMap.put("JFChangesInvestmentsFoamingExternal",strJFChangesInvestmentsFoamingExternal);
                        targetMap.put("JFChangesInvestmentsFaceCoversExternal",strJFChangesInvestmentsFaceCoversExternal);
                        break;
                    }
                    case "AQE" :{
                        String strJFChangesFixtures = UIUtil.getValue(formMap, "JFChangesFixtures");
                        String strJFCostOfQuality = UIUtil.getValue(formMap, "JFCostOfQuality");
                        String strJFCostOfTrial = UIUtil.getValue(formMap, "JFCostOfTrial");
                        String strJFChangesFixturesExternal = UIUtil.getValue(formMap, "JFChangesFixturesExternal");
                        String strJFCostOfQualityExternal = UIUtil.getValue(formMap, "JFCostOfQualityExternal");
                        String strJFCostOfTrialExternal = UIUtil.getValue(formMap, "JFCostOfTrialExternal");
                        targetMap.put("JFChangesFixtures",strJFChangesFixtures);
                        targetMap.put("JFCostOfQuality",strJFCostOfQuality);
                        targetMap.put("JFCostOfTrial",strJFCostOfTrial);
                        targetMap.put("JFChangesFixturesExternal",strJFChangesFixturesExternal);
                        targetMap.put("JFCostOfQualityExternal",strJFCostOfQualityExternal);
                        targetMap.put("JFCostOfTrialExternal",strJFCostOfTrialExternal);
                        break;
                    }
                    case "LOR" :{
                        String strJFCostPackagingLogistics = UIUtil.getValue(formMap, "JFCostPackagingLogistics");
                        String strJFCostPackagingLogisticsExternal = UIUtil.getValue(formMap, "JFCostPackagingLogisticsExternal");
                        targetMap.put("JFCostPackagingLogistics",strJFCostPackagingLogistics);
                        targetMap.put("JFCostPackagingLogisticsExternal",strJFCostPackagingLogisticsExternal);

                        break;
                    }
                    case "Launch_Manager" :{
                        String strJFMaterialsFinishedProductsRework = UIUtil.getValue(formMap, "JFMaterialsFinishedProductsRework");
                        targetMap.put("JFMaterialsFinishedProductsRework",strJFMaterialsFinishedProductsRework);
                        break;
                    }
                    case "Costing" :{
                        String strJFChangesUnitPriceCost = UIUtil.getValue(formMap, "JFChangesUnitPriceCost");
                        String strJFChangesMouldCost = UIUtil.getValue(formMap, "JFChangesMouldCost");
                        String strJFChangesUnitPriceCostExternal = UIUtil.getValue(formMap, "JFChangesUnitPriceCostExternal");
                        String strJFChangesMouldCostExternal = UIUtil.getValue(formMap, "JFChangesMouldCostExternal");
                        targetMap.put("JFChangesUnitPriceCost",strJFChangesUnitPriceCost);
                        targetMap.put("JFChangesMouldCost",strJFChangesMouldCost);
                        targetMap.put("JFChangesUnitPriceCostExternal",strJFChangesUnitPriceCostExternal);
                        targetMap.put("JFChangesMouldCostExternal",strJFChangesMouldCostExternal);
                        break;
                    }
                    case "BUR" :{
                        String strJFAcknowledgmentContentAmount = UIUtil.getValue(formMap, "JFAcknowledgmentContentAmount");
                        targetMap.put("JFAcknowledgmentContentAmount",strJFAcknowledgmentContentAmount);
                        break;
                    }
                    case "APR" :{
                        String strJFChangesInvestment = UIUtil.getValue(formMap, "JFChangesInvestment");
                        String strJFChangesDevelopment = UIUtil.getValue(formMap, "JFChangesDevelopment");
                        String strJFChangesMould = UIUtil.getValue(formMap, "JFChangesMould");
                        String strJFSumInventoryScrapAmount = UIUtil.getValue(formMap, "JFSumInventoryScrapAmount");
                        String strJFChangesInvestmentExternal = UIUtil.getValue(formMap, "JFChangesInvestmentExternal");
                        String strJFChangesDevelopmentExternal = UIUtil.getValue(formMap, "JFChangesDevelopmentExternal");
                        String strJFChangesMouldExternal = UIUtil.getValue(formMap, "JFChangesMouldExternal");
                        String strJFSumInventoryScrapAmountExternal = UIUtil.getValue(formMap, "JFSumInventoryScrapAmountExternal");
                        String strJFAPRNotes = UIUtil.getValue(formMap, "JFAPRNotes");
                        String strJFOtherFee	 = UIUtil.getValue(formMap, "JFOtherFee");
                        String strJFTestFee	 = UIUtil.getValue(formMap, "JFTestFee");
                        targetMap.put("JFChangesInvestment",strJFChangesInvestment);
                        targetMap.put("JFChangesDevelopment",strJFChangesDevelopment);
                        targetMap.put("JFChangesMould",strJFChangesMould);
                        targetMap.put("JFSumInventoryScrapAmount",strJFSumInventoryScrapAmount);
                        targetMap.put("JFChangesInvestmentExternal",strJFChangesInvestmentExternal);
                        targetMap.put("JFChangesDevelopmentExternal",strJFChangesDevelopmentExternal);
                        targetMap.put("JFChangesMouldExternal",strJFChangesMouldExternal);
                        targetMap.put("JFSumInventoryScrapAmountExternal",strJFSumInventoryScrapAmountExternal);
                        targetMap.put("JFAPRNotes",strJFAPRNotes);
                        targetMap.put("JFOtherFee",strJFOtherFee);
                        targetMap.put("JFTestFee",strJFTestFee);
                        break;
                    }
                }
            }

        }

    }

    public static boolean checkSignTaskIsAllReview(Context context , String strECRId) throws Exception{
        DomainObject ecr = DomainObject.newInstance(context, strECRId);
        StringList typeSelectList = new StringList();
        typeSelectList.add(DomainConstants.SELECT_ID);
        typeSelectList.add(DomainConstants.SELECT_CURRENT);
        typeSelectList.add(SELECT_ATTRIBUTE_TITLE);
        typeSelectList.add(SELECT_OWNER);
        StringList relSelectList = new StringList();
        relSelectList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_PROJECT_ROLE);
        MapList maps = new MapList();
        maps = ecr.getRelatedObjects(context, JF_PLMConstants_mxJPO.RELATIONSHIP_JF_ECR_TASK , // relationship pattern
                JF_PLMConstants_mxJPO.TYPE_JS_SIGN_TASK,                                    // object pattern
                typeSelectList,                            // object selects
                relSelectList, // relationship selects
                false,                                        // to direction
                true,                                        // from direction
                (short) 1,                                    // recursion level
                "!(current==Review || current== Complete)",                // object where clause
                "",
                (short) 0);
        return maps.size() > 0 ? false :true;
    }
    public static boolean checkSignTaskIsAllComplete(Context context , String strECRId) throws Exception{
        DomainObject ecr = DomainObject.newInstance(context, strECRId);
        StringList typeSelectList = new StringList();
        typeSelectList.add(DomainConstants.SELECT_ID);
        typeSelectList.add(DomainConstants.SELECT_CURRENT);
        typeSelectList.add(SELECT_ATTRIBUTE_TITLE);
        typeSelectList.add(SELECT_OWNER);
        StringList relSelectList = new StringList();
        relSelectList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_PROJECT_ROLE);
        MapList maps = new MapList();
        maps = ecr.getRelatedObjects(context, JF_PLMConstants_mxJPO.RELATIONSHIP_JF_ECR_TASK , // relationship pattern
                JF_PLMConstants_mxJPO.TYPE_JS_SIGN_TASK,                                    // object pattern
                typeSelectList,                            // object selects
                relSelectList, // relationship selects
                false,                                        // to direction
                true,                                        // from direction
                (short) 1,                                    // recursion level
                "!(current== Complete)",                // object where clause
                "",
                (short) 0);
        return maps.size() > 0 ? false :true;
    }

    /**
    *
    *@description APR状态 同意或者拒绝 会签任务
    *@param context
	*@param args
    *@return java.lang.String
    *@throws
    *@author CHENYAN
    *@date 2024/8/7 10:19
    */
    public String APRReviewSignTask(Context context ,String[] args) throws Exception{
        _logger.info("----------------------------- APRReviewSignTask begin ------------------------------------");
        HashMap res = new HashMap<>();
        Map reqMap = (Map) JPO.unpackArgs(args);
        List signTaskIdList = (List) reqMap.get("signTaskIds");
        String strAction = (String) reqMap.get("action");
        String strObjectId = (String) reqMap.get("objectId");
        String strNotes = (String) reqMap.get("Notes");
        _logger.info("strObjectId:{}",strObjectId);
        _logger.info("strAction:{}",strAction);
        _logger.info("Notes:{}",strNotes);
        _logger.info("strSignTaskId:{}",signTaskIdList);
        DomainObject ecr = DomainObject.newInstance(context,strObjectId);
        try {
            ContextUtil.pushContext(context);
            ContextUtil.startTransaction(context,true);
            for (int i = 0; i < signTaskIdList.size(); i++) {
                String strSignTaskId = (String) signTaskIdList.get(i);
                DomainObject signTask = DomainObject.newInstance(context, strSignTaskId);
                signTask.setDescription(context,strNotes);
                if ("reject".equals(strAction)){
                    signTask.demote(context);
                }else if ("approve".equals(strAction)){
                    signTask.promote(context);
                }
            }
            ContextUtil.commitTransaction(context);
            //获取最新的会签任务
            MapList allSignTaskInECR = getAllSignTaskInECR(context, ecr);
            res.put("signTaskList",allSignTaskInECR);
            res.put("code","200");

        }catch (Exception e){
            _logger.info(e.getMessage());
            ContextUtil.abortTransaction(context);
        }finally {
            ContextUtil.popContext(context);
        }
        Gson gson = new Gson();
        return gson.toJson(res);
    }


    /**
    *
    *@description 根据角色获取到ECRtable中必填属性，校验有没有必填 没有必填不能提交
    *@param context
	*@param args
    *@return java.lang.String
    *@throws
    *@author CHENYAN
    *@date 2024/8/13 15:34
    */
    public  String validateECRTableRequireField(Context context ,String[] args) throws Exception{
        _logger.info("----------------------------- validateECRTableRequireField begin ------------------------------------");
        HashMap res = new HashMap<>();
        Map reqMap = (Map) JPO.unpackArgs(args);
        List roleList = (List) reqMap.get("roleList");
        StringList roleNameStringList = StringList.create(roleList);
        String strObjectId = (String) reqMap.get("objectId");
        _logger.info("strObjectId:{}",strObjectId);
        _logger.info("roleList:{}",roleList);
        String initargs[] = {};
        Map parameters = new HashMap<>();
        parameters.put("objectId",strObjectId);
        parameters.put("expandLevel","All");
        DomainObject ecr = DomainObject.newInstance(context, strObjectId);
        Map checkRes1 = new HashMap();
        Map checkRes2 = new HashMap();
        try {
            ContextUtil.pushContext(context);
            //自制件 table数据
            if (roleList.contains("AME") || roleList.contains("APR")){
                MapList makeTableMapList = (MapList)JPO.invoke(context, "JF_ECRService", initargs, "getECRMakeTableData", JPO.packArgs (parameters), MapList.class);
                checkRes1 = getEditRowInTable(context, makeTableMapList, "JFECRController", ecr, roleNameStringList,strObjectId);
            }else {
                checkRes1.put("result",true);
            }
            if (roleList.contains("ICO") || roleList.contains("BUR") || roleList.contains("PRR") || roleList.contains("Costing")){
                //采购件table数据
                MapList buyTableMapList = (MapList)JPO.invoke(context, "JF_ECRService", initargs, "getECRCostingTableData", JPO.packArgs (parameters), MapList.class);
                checkRes2 = getEditRowInTable(context, buyTableMapList, "JFECRCosting", ecr, roleNameStringList,strObjectId);
            }else {
                checkRes2.put("result",true);
            }
        }finally {
            ContextUtil.popContext(context);
        }
        boolean res1 = (boolean)checkRes1.get("result");
        boolean res2 = (boolean)checkRes2.get("result");
        if (res1 && res2){
            res.put("result",true);
            res.put("code","200");
        }else {
            res.put("result",false);
            res.put("code","404");
            String strMakeMess = (String) checkRes1.get("mess");
            String strBuyMess = (String) checkRes2.get("mess");
            res.put("makeMess",strMakeMess);
            res.put("buyMess",strBuyMess);
        }
        Gson gson = new Gson();
        _logger.info("----------------------------- validateECRTableRequireField end ------------------------------------");
        return gson.toJson(res);
    }

    /**
    *
    *@description 获取Table数据中的可编辑行
    *@param tableList
	*@param strTable
	*@param ecr
	*@param roleList
    *@return com.matrixone.apps.domain.util.MapList
    *@throws
    *@author CHENYAN
    *@date 2024/8/13 16:42
    */
    public  Map getEditRowInTable(Context context,MapList tableList ,String strTable,DomainObject ecr ,StringList roleList,String strECRId) throws Exception {
        // 制造件table
        boolean isAlert = true;
        Set requireRowList = new HashSet();
        Map res = new HashMap<>();
        try {
            if ("JFECRController".equals(strTable)) {
                StringList ecrSelectList = new StringList();
                ecrSelectList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JFCHANGESOURCE);
                ecrSelectList.add(SELECT_CURRENT);
                Map ecrMap = ecr.getInfo(context, ecrSelectList);
                String strChangeSource = (String) ecrMap.get(JF_PLMConstants_mxJPO.SELECT_ATTR_JFCHANGESOURCE);
                String strCurrent = (String) ecrMap.get(SELECT_CURRENT);
                for (int i = 0; i < tableList.size(); i++) {
                    Map row = (Map) tableList.get(i);
                    // 是否是零级件
                    String strIsZeroPart = (String) row.get(JF_PLMConstants_mxJPO.SELECT_ATTR_JFWholeChair);
                    //只校验0级件中属性需要必填
                    if ("Y".equals(strIsZeroPart)) {
                        for (int i1 = 0; i1 < roleList.size(); i1++) {
                            String strRole = roleList.get(i1);
                            String strRoleValue = getRoleValueByKey(strRole);
                            // 获取table里面的必填属性
                            StringList canEditFieldList = JF_ECRService_mxJPO.getRequireEditFieldInRoleAndLinkField(strCurrent, strRoleValue, "", "", strChangeSource, strTable);
                            _logger.info("canEditFieldList1:{}",canEditFieldList);
                            if (null != canEditFieldList && canEditFieldList.size() > 0) {
                                String strId = (String) row.get(SELECT_ID);
                                DomainObject part = DomainObject.newInstance(context, strId);
                                MapList maps = part.getRelatedObjects(context, "JFECR2MakePartPrice" , // relationship pattern
                                        JF_PLMConstants_mxJPO.TYPE_JFECR,                                    // object pattern
                                        new StringList(SELECT_ID),                            // object selects
                                        new StringList(SELECT_RELATIONSHIP_ID), // relationship selects
                                        true,                                        // to direction
                                        false,                                        // from direction
                                        (short) 1,                                    // recursion level
                                        JF_PublicMethodClass_mxJPO.buildStringInStrings("id==",strECRId),                // object where clause
                                        "",
                                        (short) 0);
                                String strRelId = "";
                               if (maps.size() > 0){
                                   Map info = (Map) maps.get(0);
                                   strRelId = UIUtil.getValue(info,SELECT_RELATIONSHIP_ID);
                               }
                                if (UIUtil.isNotNullAndNotEmpty(strRelId)) {
                                    DomainRelationship rel = DomainRelationship.newInstance(context, strRelId);
                                    AttributeList attributeValues = rel.getAttributeValues(context, canEditFieldList);
                                    for (int i2 = 0; i2 < attributeValues.size(); i2++) {
                                        Attribute attribute = attributeValues.get(i2);
                                        if (UIUtil.isNullOrEmpty(attribute.getValue())) {
                                            isAlert = false;
                                            String strPartNum = (String) row.get(JF_PLMConstants_mxJPO.SELECT_ATTR_V_PART_NUMBER);
                                            requireRowList.add(strPartNum);
                                        }
                                    }
                                    //该关系不存在直接返回
                                }else {
                                    isAlert = false;
                                    String strPartNum = (String) row.get(JF_PLMConstants_mxJPO.SELECT_ATTR_V_PART_NUMBER);
                                    requireRowList.add(strPartNum);
                                }
                            }
                        }
                    }
                }
            } else if ("JFECRCosting".equals(strTable)) {
                StringList ecrSelectList = new StringList();
                ecrSelectList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JFCHANGESOURCE);
                ecrSelectList.add(SELECT_CURRENT);
                Map ecrMap = ecr.getInfo(context, ecrSelectList);
                String strCurrent = (String) ecrMap.get(SELECT_CURRENT);
                Map oneLevelMap = new HashMap();
                Map group = (Map) tableList.stream().filter(m -> {
                    Map infoMap = (Map) m;
                    String strRelationship = UIUtil.getValue(infoMap, "relationship");
                    String strId = UIUtil.getValue(infoMap, SELECT_ID);
                    //如果是一级件下面零件需要判断关系属性ECRName是否保存的当前ECR信息
                    if (JF_PLMConstants_mxJPO.REL_JFECR2OnelevelPart.equals(strRelationship)) {
                        oneLevelMap.put(strId, m);
                    }
                    return JF_PLMConstants_mxJPO.REL_JFOneLevelPart2Part.equals(strRelationship) ? true : false;
                }).collect(Collectors.groupingBy(m -> {
                    Map infoMap = (Map) m;
                    return infoMap.get(SELECT_FROM_ID);
                }));
                //  oneLevelMap 和 group 的key oneLevelMap 大于等于 group的key ，一个保存的是 一级件Map ，一个保存一级件下面子件
                // 遍历所有一级件
                for (Object entry : oneLevelMap.entrySet()) {
                    Map.Entry entryMap = (Map.Entry) entry;
                    String strKey = (String) entryMap.getKey();
                    Map onePart = (Map) entryMap.getValue();
                    String strProcurementType = (String) onePart.get(JF_PLMConstants_mxJPO.SELECT_ATTR_JF_ProcurementType);
                    //获取ECR的采购类型

                    //采购类型为空走make的逻辑
                    if (UIUtil.isNullOrEmpty(strProcurementType)){
                        strProcurementType = JF_PLMConstants_mxJPO.ATTR_JF_ProcurementType_RANGE_MAKE;
                    }
                    String strDirectBuy = (String) onePart.get(JF_PLMConstants_mxJPO.SELECT_ATTR_JFDIRECT_BUY);
                    //默认值给Both
                    String strChangeSource = "";
                    if (group.containsKey(strKey)) {
                        //一级件存在受影响对象 变更来源取子件
                        List sunList = (List) group.get(strKey);
                        Set changeSourceSet = (Set) sunList.stream().map(m -> {
                            Map sun = (Map) m;
                            return sun.get(JF_PLMConstants_mxJPO.TO_REL_JFRELATEITEM_JFChangeSource);
                        }).collect(Collectors.toSet());
                        if (changeSourceSet.contains(JF_PLMConstants_mxJPO.ATTR_JFCHANGESOURCE_RANGE_BOTH)) {
                            strChangeSource = JF_PLMConstants_mxJPO.ATTR_JFCHANGESOURCE_RANGE_BOTH;
                        } else if (changeSourceSet.size() == 1) {
                            for (Object changeSource : changeSourceSet) {
                                String strChangeSourceTemp = (String) changeSource;
                                strChangeSource = strChangeSourceTemp;
                            }
                        }
                    } else {
                        //一级件不存在受影响对象 变更来源取本身
                        String strChangeSourceTemp = (String) onePart.get(JF_PLMConstants_mxJPO.TO_REL_JFRELATEITEM_JFChangeSource);
                        strChangeSource = strChangeSourceTemp;
                    }
                    //匹配不到给默认值
                    if (UIUtil.isNullOrEmpty(strChangeSource)) {
                        _logger.info("---------------------------- 授予默认值 -------------------------------");
                        strChangeSource = JF_PLMConstants_mxJPO.ATTR_JFCHANGESOURCE_RANGE_BOTH;
                    }
                    onePart.put(JF_PLMConstants_mxJPO.SELECT_ATTR_JFCHANGESOURCE,strChangeSource);
                }
                _logger.info("tableList.size():{}",tableList.size());
                for (int i = 0; i < tableList.size(); i++) {
                    Map obj = (Map) tableList.get(i);
                    String strId = (String) obj.get(SELECT_ID);
                    String strFromId = (String) obj.get(SELECT_FROM_ID);
                    String strRelName = (String) obj.get("relationship");
                    // 判断一级件属性是 Buy ICO 如果是 那么一级件才能编辑 采取判断能不能编辑
                    // 判断一级件属性是 Make 一级件不能编辑 自件可以编辑 采取判断能不能编辑
                    if (JF_PLMConstants_mxJPO.REL_JFECR2OnelevelPart.equals(strRelName)) {
                        String strProcurementType = (String) obj.get(JF_PLMConstants_mxJPO.SELECT_ATTR_JF_ProcurementType);
                        //采购类型为空走make的逻辑
                        if (UIUtil.isNullOrEmpty(strProcurementType)){
                            strProcurementType = JF_PLMConstants_mxJPO.ATTR_JF_ProcurementType_RANGE_MAKE;
                        }
                        if ((JF_PLMConstants_mxJPO.ATTR_JF_ProcurementType_RANGE_BUY.equals(strProcurementType) ||
                                JF_PLMConstants_mxJPO.ATTR_JF_ProcurementType_RANGE_ICO.equals(strProcurementType))) {
                            //获取编辑属性
                            String strDirectBuy = (String) obj.get(JF_PLMConstants_mxJPO.SELECT_ATTR_JFDIRECT_BUY);
                            String strChangeSource = (String) obj.get(JF_PLMConstants_mxJPO.SELECT_ATTR_JFCHANGESOURCE);
                            for (int i1 = 0; i1 < roleList.size(); i1++) {
                                String strRole = roleList.get(i1);
                                // 获取table里面的必填属性
                                String strRoleValue = getRoleValueByKey(strRole);
                                StringList canEditFieldList = JF_ECRService_mxJPO.getRequireEditFieldInRoleAndLinkField(strCurrent, strRoleValue, strDirectBuy, strProcurementType, strChangeSource, strTable);
                                _logger.info("canEditFieldList2:{}",canEditFieldList);
                                if (null != canEditFieldList && canEditFieldList.size() > 0) {
                                    DomainObject part = DomainObject.newInstance(context, strId);
                                    MapList maps = part.getRelatedObjects(context, "JFECR2PartPrice" , // relationship pattern
                                            JF_PLMConstants_mxJPO.TYPE_JFECR,                                    // object pattern
                                            new StringList(SELECT_ID),                            // object selects
                                            new StringList(SELECT_RELATIONSHIP_ID), // relationship selects
                                            true,                                        // to direction
                                            false,                                        // from direction
                                            (short) 1,                                    // recursion level
                                            JF_PublicMethodClass_mxJPO.buildStringInStrings("id==",strECRId),                // object where clause
                                            "",
                                            (short) 0);
                                    String strRelId = "";
                                    if (maps.size() > 0){
                                        Map info = (Map) maps.get(0);
                                        strRelId = UIUtil.getValue(info,SELECT_RELATIONSHIP_ID);
                                    }
                                    if (UIUtil.isNotNullAndNotEmpty(strRelId)) {
                                        DomainRelationship rel = DomainRelationship.newInstance(context, strRelId);
                                        AttributeList attributeValues = rel.getAttributeValues(context, canEditFieldList);
                                        for (int i2 = 0; i2 < attributeValues.size(); i2++) {
                                            Attribute attribute = attributeValues.get(i2);
                                            if (UIUtil.isNullOrEmpty(attribute.getValue())) {
                                                isAlert = false;
                                                String strPartNum = (String) obj.get(JF_PLMConstants_mxJPO.SELECT_ATTR_V_PART_NUMBER);
                                                requireRowList.add(strPartNum);
                                            }
                                        }
                                    }else {
                                        isAlert = false;
                                        String strPartNum = (String) obj.get(JF_PLMConstants_mxJPO.SELECT_ATTR_V_PART_NUMBER);
                                        requireRowList.add(strPartNum);
                                    }
                                }
                            }
                        }
                    } else if (JF_PLMConstants_mxJPO.REL_JFOneLevelPart2Part.equals(strRelName)) {
                        Map onePart = (Map) oneLevelMap.get(strFromId);
                        String strProcurementType = (String) onePart.get(JF_PLMConstants_mxJPO.SELECT_ATTR_JF_ProcurementType);
                        //采购类型为空走make的逻辑
                        if (UIUtil.isNullOrEmpty(strProcurementType)){
                            strProcurementType = JF_PLMConstants_mxJPO.ATTR_JF_ProcurementType_RANGE_MAKE;
                        }
                        //子件采购类型
                        String strObjProcurementType = (String) obj.get(JF_PLMConstants_mxJPO.SELECT_ATTR_JF_ProcurementType);
                        //采购类型为空走make的逻辑
                        if (UIUtil.isNullOrEmpty(strObjProcurementType)){
                            strObjProcurementType = JF_PLMConstants_mxJPO.ATTR_JF_ProcurementType_RANGE_MAKE;
                        }
                        if (JF_PLMConstants_mxJPO.ATTR_JF_ProcurementType_RANGE_MAKE.equals(strProcurementType)) {
                            if (!JF_PLMConstants_mxJPO.ATTR_JF_ProcurementType_RANGE_MAKE.equals(strObjProcurementType)){
                                //获取编辑属性
                                String strDirectBuy = (String) obj.get(JF_PLMConstants_mxJPO.SELECT_ATTR_JFDIRECT_BUY);
                                String strChangeSource = (String) obj.get(JF_PLMConstants_mxJPO.TO_REL_JFRELATEITEM_JFChangeSource);
                                _logger.info("strDirectBuy:{} strChangeSource:{}",strDirectBuy,strChangeSource);
                                for (int i1 = 0; i1 < roleList.size(); i1++) {
                                    String strRole = roleList.get(i1);
                                    String strRoleValue = getRoleValueByKey(strRole);
                                    // 获取table里面的必填属性
                                    StringList canEditFieldList = JF_ECRService_mxJPO.getRequireEditFieldInRoleAndLinkField(strCurrent, strRoleValue, strDirectBuy, strObjProcurementType, strChangeSource, strTable);
                                    _logger.info("canEditFieldList3:{}",canEditFieldList);
                                    if (null != canEditFieldList && canEditFieldList.size() > 0) {
                                        DomainObject part = DomainObject.newInstance(context, strId);
                                        MapList maps = part.getRelatedObjects(context, "JFECR2PartPrice" , // relationship pattern
                                                JF_PLMConstants_mxJPO.TYPE_JFECR,                                    // object pattern
                                                new StringList(SELECT_ID),                            // object selects
                                                new StringList(SELECT_RELATIONSHIP_ID), // relationship selects
                                                true,                                        // to direction
                                                false,                                        // from direction
                                                (short) 1,                                    // recursion level
                                                JF_PublicMethodClass_mxJPO.buildStringInStrings("id==",strECRId),                // object where clause
                                                "",
                                                (short) 0);
                                        String strRelId = "";
                                        if (maps.size() > 0){
                                            Map info = (Map) maps.get(0);
                                            strRelId = UIUtil.getValue(info,SELECT_RELATIONSHIP_ID);
                                        }
                                        if (UIUtil.isNotNullAndNotEmpty(strRelId)) {
                                            DomainRelationship rel = DomainRelationship.newInstance(context, strRelId);
                                            AttributeList attributeValues = rel.getAttributeValues(context, canEditFieldList);
                                            for (int i2 = 0; i2 < attributeValues.size(); i2++) {
                                                Attribute attribute = attributeValues.get(i2);
                                                if (UIUtil.isNullOrEmpty(attribute.getValue())) {
                                                    isAlert = false;
                                                    String strPartNum = (String) obj.get(JF_PLMConstants_mxJPO.SELECT_ATTR_V_PART_NUMBER);
                                                    requireRowList.add(strPartNum);
                                                }
                                            }
                                        }else {
                                            isAlert = false;
                                            String strPartNum = (String) obj.get(JF_PLMConstants_mxJPO.SELECT_ATTR_V_PART_NUMBER);
                                            requireRowList.add(strPartNum);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }catch (Exception e){
            _logger.error(e.getMessage());
            throw e;
        }
        res.put("result", isAlert);
        String strMess = "";
        if (requireRowList.size() > 0){
            strMess = StringList.create(requireRowList).join(",");
        }
        res.put("mess", strMess);
        return res;
    }


    /**
    *
    *@description 获取底部按钮的权限
    *@param roleList
    *@return boolean
    *@throws
    *@author CHENYAN
    *@date 2024/8/19 13:39
    */
    public Map getFooterMenuAccess(MapList roleList,String strLoginUser,String strOwner,String strCurrent,String strProjectOwner){
        //
        Map res = new HashMap();
        res.put("footerShow",false);
        res.put("submit",false);
        res.put("save",false);
        res.put("cancel",false);
        //表单权限
        if (roleList.size() > 0){
            _logger.info("roleList:{}",roleList);
            for (int i = 0; i < roleList.size(); i++) {
                Map role = (Map) roleList.get(i);
                String strSignTaskCurrent = (String) role.get(SELECT_CURRENT);
                if (!("Complete".equals(strSignTaskCurrent) || "Review".equals(strSignTaskCurrent))){
                    res.put("footerShow",true);
                    res.put("submit",true);
                    res.put("save",true);
                    res.put("cancel",true);
                    break;
                }
            }
        }
        //ECR的状态优先级高 (可能会存在会签任务的指派人创建ECR)
        if ("Create".equals(strCurrent)){
            if (strLoginUser.equals(strOwner)){
                res.put("footerShow",true);
                res.put("submit",false);
                res.put("save",true);
                res.put("cancel",true);
            }
        }else if ("Review".equals(strCurrent)){
          if (strLoginUser.equals(strProjectOwner)){
              res.put("footerShow",true);
              res.put("submit",false);
              res.put("save",true);
              res.put("cancel",true);
          }
        }
        return  res;
    }
    /**
     *
     *@description 获取当前登录人是否拥有标准件库管理员权限
     *@param context
     *@param args
     *@return java.lang.String
     *@throws
     *@author CHENYAN
     *@date 2024/9/4 13:22
     */
    public String getCurrentUserHasStandardRole(Context context, String[] args){
        Map res = new HashMap<>();
        try {
            String strLoginUser = context.getUser();
            String strMQLRes = MqlUtil.mqlCommand(context, "print role $1 select person dump ;", true, new String[]{"JfStandardAdmin"});
            _logger.info("strMQLRes :{}",strMQLRes);
            String[] splitPerson = strMQLRes.split(",");
            //需要移除的属性range值
            Map attrMap = new HashMap<>();
            boolean hasRole = StringList.create(splitPerson).contains(strLoginUser);
            if (!hasRole){
                attrMap.put("JF_PartType",StringList.create("F"));
            }
            res.put("hasRole",hasRole);
            res.put("attributes",attrMap);
            res.put("status","success");
        }catch (Exception e){
            _logger.error(e.getMessage());
            res.put("status","fail");
        }
        Gson gson = new Gson();
        return gson.toJson(res);
    }

    /**
    *
    *@description 获取table具体行中的必填属性
    *@param context
	*@param tableMap  OOTB表格Maplist 具体一个
	*@param tableList OOTB表格Maplist
	*@param strTable 表名
	*@param ecr ecr 对象
	*@param roleList 登录人角色集合
    *@return matrix.util.StringList
    *@throws
    *@author CHENYAN
    *@date 2024/10/10 10:15
    */
    public static  StringList getEditAttributeRowInExcel(Context context,Map tableMap,MapList tableList ,String strTable,DomainObject ecr ,StringList roleList) throws Exception {
        StringList res = new StringList();
        try {
            if ("JFECRController".equals(strTable)) {
                StringList ecrSelectList = new StringList();
                ecrSelectList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JFCHANGESOURCE);
                ecrSelectList.add(SELECT_CURRENT);
                Map ecrMap = ecr.getInfo(context, ecrSelectList);
                String strChangeSource = (String) ecrMap.get(JF_PLMConstants_mxJPO.SELECT_ATTR_JFCHANGESOURCE);
                String strCurrent = (String) ecrMap.get(SELECT_CURRENT);
                // 是否是零级件
                String strIsZeroPart = (String) tableMap.get(JF_PLMConstants_mxJPO.SELECT_ATTR_JFWholeChair);
                String strRelName = (String) tableMap.get("relationship");
                //只有0级件和一级件才可以编辑
                if (JF_PLMConstants_mxJPO.REL_JFECR2Manufacturing.equals(strRelName) || JF_PLMConstants_mxJPO.RELATIONSHIP_JFRootPart2OnePart.equals(strRelName)){
                    for (int i1 = 0; i1 < roleList.size(); i1++) {
                        String strRole = roleList.get(i1);
                        String strRoleValue = getRoleValueByKey(strRole);
                        // 获取table里面的必填属性
                        StringList canEditFieldList = JF_ECRService_mxJPO.getRequireEditFieldInRoleAndLinkField(strCurrent, strRoleValue, "", "", strChangeSource, strTable);
                        res.addAll(canEditFieldList);
                    }
                }
            } else if ("JFECRCosting".equals(strTable)) {
                StringList ecrSelectList = new StringList();
                ecrSelectList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JFCHANGESOURCE);
                ecrSelectList.add(SELECT_CURRENT);
                Map ecrMap = ecr.getInfo(context, ecrSelectList);
                String strCurrent = (String) ecrMap.get(SELECT_CURRENT);
                Map oneLevelMap = new HashMap();
                Map group = (Map) tableList.stream().filter(m -> {
                    Map infoMap = (Map) m;
                    String strRelationship = UIUtil.getValue(infoMap, "relationship");
                    String strId = UIUtil.getValue(infoMap, SELECT_ID);
                    //如果是一级件下面零件需要判断关系属性ECRName是否保存的当前ECR信息
                    if (JF_PLMConstants_mxJPO.REL_JFECR2OnelevelPart.equals(strRelationship)) {
                        oneLevelMap.put(strId, m);
                    }
                    return JF_PLMConstants_mxJPO.REL_JFOneLevelPart2Part.equals(strRelationship) ? true : false;
                }).collect(Collectors.groupingBy(m -> {
                    Map infoMap = (Map) m;
                    return infoMap.get(SELECT_FROM_ID);
                }));
                //  oneLevelMap 和 group 的key oneLevelMap 大于等于 group的key ，一个保存的是 一级件Map ，一个保存一级件下面子件
                // 遍历所有一级件
                for (Object entry : oneLevelMap.entrySet()) {
                    Map.Entry entryMap = (Map.Entry) entry;
                    String strKey = (String) entryMap.getKey();
                    Map onePart = (Map) entryMap.getValue();
                    String strProcurementType = (String) onePart.get(JF_PLMConstants_mxJPO.SELECT_ATTR_JF_ProcurementType);
                    //获取ECR的采购类型

                    //采购类型为空走make的逻辑
                    if (UIUtil.isNullOrEmpty(strProcurementType)){
                        strProcurementType = JF_PLMConstants_mxJPO.ATTR_JF_ProcurementType_RANGE_MAKE;
                    }
                    String strDirectBuy = (String) onePart.get(JF_PLMConstants_mxJPO.SELECT_ATTR_JFDIRECT_BUY);
                    //默认值给Both
                    String strChangeSource = "";
                    if (group.containsKey(strKey)) {
                        //一级件存在受影响对象 变更来源取子件
                        List sunList = (List) group.get(strKey);
                        Set changeSourceSet = (Set) sunList.stream().map(m -> {
                            Map sun = (Map) m;
                            return sun.get(JF_PLMConstants_mxJPO.TO_REL_JFRELATEITEM_JFChangeSource);
                        }).collect(Collectors.toSet());
                        if (changeSourceSet.contains(JF_PLMConstants_mxJPO.ATTR_JFCHANGESOURCE_RANGE_BOTH)) {
                            strChangeSource = JF_PLMConstants_mxJPO.ATTR_JFCHANGESOURCE_RANGE_BOTH;
                        } else if (changeSourceSet.size() == 1) {
                            for (Object changeSource : changeSourceSet) {
                                String strChangeSourceTemp = (String) changeSource;
                                strChangeSource = strChangeSourceTemp;
                            }
                        }
                    } else {
                        //一级件不存在受影响对象 变更来源取本身
                        String strChangeSourceTemp = (String) onePart.get(JF_PLMConstants_mxJPO.TO_REL_JFRELATEITEM_JFChangeSource);
                        strChangeSource = strChangeSourceTemp;
                    }
                    //匹配不到给默认值
                    if (UIUtil.isNullOrEmpty(strChangeSource)) {
                        _logger.info("---------------------------- 授予默认值 -------------------------------");
                        strChangeSource = JF_PLMConstants_mxJPO.ATTR_JFCHANGESOURCE_RANGE_BOTH;
                    }
                    onePart.put(JF_PLMConstants_mxJPO.SELECT_ATTR_JFCHANGESOURCE,strChangeSource);
                }
                String strId = (String) tableMap.get(SELECT_ID);
                String strFromId = (String) tableMap.get(SELECT_FROM_ID);
                String strRelName = (String) tableMap.get("relationship");
                // 判断一级件属性是 Buy ICO 如果是 那么一级件才能编辑 采取判断能不能编辑
                // 判断一级件属性是 Make 一级件不能编辑 自件可以编辑 采取判断能不能编辑
                if (JF_PLMConstants_mxJPO.REL_JFECR2OnelevelPart.equals(strRelName)) {
                    String strProcurementType = (String) tableMap.get(JF_PLMConstants_mxJPO.SELECT_ATTR_JF_ProcurementType);
                    //采购类型为空走make的逻辑
                    if (UIUtil.isNullOrEmpty(strProcurementType)){
                        strProcurementType = JF_PLMConstants_mxJPO.ATTR_JF_ProcurementType_RANGE_MAKE;
                    }
                    if ((JF_PLMConstants_mxJPO.ATTR_JF_ProcurementType_RANGE_BUY.equals(strProcurementType) ||
                            JF_PLMConstants_mxJPO.ATTR_JF_ProcurementType_RANGE_ICO.equals(strProcurementType))) {
                        //获取编辑属性
                        String strDirectBuy = (String) tableMap.get(JF_PLMConstants_mxJPO.SELECT_ATTR_JFDIRECT_BUY);
                        String strChangeSource = (String) tableMap.get(JF_PLMConstants_mxJPO.SELECT_ATTR_JFCHANGESOURCE);
                        for (int i1 = 0; i1 < roleList.size(); i1++) {
                            String strRole = roleList.get(i1);
                            // 获取table里面的必填属性
                            String strRoleValue = getRoleValueByKey(strRole);
                            StringList canEditFieldList = JF_ECRService_mxJPO.getRequireEditFieldInRoleAndLinkField(strCurrent, strRoleValue, strDirectBuy, strProcurementType, strChangeSource, strTable);
                            res.addAll(canEditFieldList);
                        }
                    }
                } else if (JF_PLMConstants_mxJPO.REL_JFOneLevelPart2Part.equals(strRelName)) {
                    Map onePart = (Map) oneLevelMap.get(strFromId);
                    String strProcurementType = (String) onePart.get(JF_PLMConstants_mxJPO.SELECT_ATTR_JF_ProcurementType);
                    //采购类型为空走make的逻辑
                    if (UIUtil.isNullOrEmpty(strProcurementType)){
                        strProcurementType = JF_PLMConstants_mxJPO.ATTR_JF_ProcurementType_RANGE_MAKE;
                    }
                    //子件采购类型
                    String strObjProcurementType = (String) tableMap.get(JF_PLMConstants_mxJPO.SELECT_ATTR_JF_ProcurementType);
                    //采购类型为空走make的逻辑
                    if (UIUtil.isNullOrEmpty(strObjProcurementType)){
                        strObjProcurementType = JF_PLMConstants_mxJPO.ATTR_JF_ProcurementType_RANGE_MAKE;
                    }
                    if (JF_PLMConstants_mxJPO.ATTR_JF_ProcurementType_RANGE_MAKE.equals(strProcurementType)) {
                        if (!JF_PLMConstants_mxJPO.ATTR_JF_ProcurementType_RANGE_MAKE.equals(strObjProcurementType)){
                            //获取编辑属性
                            String strDirectBuy = (String) tableMap.get(JF_PLMConstants_mxJPO.SELECT_ATTR_JFDIRECT_BUY);
                            String strChangeSource = (String) tableMap.get(JF_PLMConstants_mxJPO.TO_REL_JFRELATEITEM_JFChangeSource);
                            for (int i1 = 0; i1 < roleList.size(); i1++) {
                                String strRole = roleList.get(i1);
                                String strRoleValue = getRoleValueByKey(strRole);
                                // 获取table里面的必填属性
                                StringList canEditFieldList = JF_ECRService_mxJPO.getRequireEditFieldInRoleAndLinkField(strCurrent, strRoleValue, strDirectBuy, strObjProcurementType, strChangeSource, strTable);
                                res.addAll(canEditFieldList);
                            }
                        }
                    }
                }
            }
        }catch (Exception e){
            _logger.error(e.getMessage());
            throw e;
        }
        return res;
    }
    public static Map getAMEEditFieldAccess(boolean hasAMESignTask,MapList makeTableList){
        Map AMEEditFiledMap = new HashMap<String,Boolean>();
        if (hasAMESignTask){
            for (int i = 0; i < makeTableList.size(); i++) {
                Map makeMap = (Map) makeTableList.get(i);
                String strPartType = (String)makeMap.get(JF_PLMConstants_mxJPO.SELECT_ATTR_JFPartType);
                if (EDIT_AME_FORM_FIELD_LIST.contains(strPartType)){
                    AMEEditFiledMap.put(strPartType,true);
                }
            }
        }else {
            for (int i = 0; i < EDIT_AME_FORM_FIELD_LIST.size(); i++) {
                String editField = EDIT_AME_FORM_FIELD_LIST.get(i);
                AMEEditFiledMap.put(editField,true);
            }
        }
        //防止有不存在零件类型
        for (int i = 0; i < EDIT_AME_FORM_FIELD_LIST.size(); i++) {
            String editField = EDIT_AME_FORM_FIELD_LIST.get(i);
            if (!AMEEditFiledMap.containsKey(editField)) {
                AMEEditFiledMap.put(editField,false);
            }
        }
        return AMEEditFiledMap;
    }
    /**
    *
    *@description 检查指定Id是否是叶子结点
    *@param context
	*@param args
    *@return java.lang.String
    *@throws
    *@author CHENYAN
    *@date 2025/7/21 22:30
    */
    public String checkClassIsLeafNodeById(Context context, String[] args) throws FrameworkException {
        Map res = new HashMap<>();
        try {
            Map reqMap = (Map) JPO.unpackArgs(args);
            ContextUtil.pushContext(context);
            String strObjectId = (String) reqMap.get("objectId");
            //创建总成还是零件
            String strCreateType = (String) reqMap.get("createType");
            StringList infoSelectList = new StringList();
            infoSelectList.add("from[Subclass]");
            infoSelectList.add(SELECT_DESCRIPTION);
            infoSelectList.add(SELECT_ATTRIBUTE_TITLE);
            DomainObject classBo = DomainObject.newInstance(context, strObjectId);
            Map classInfoMap = classBo.getInfo(context, infoSelectList);
            String strIsLeafNodeFlag = (String) classInfoMap.get("from[Subclass]");
            String strNameEN = (String) classInfoMap.get(SELECT_DESCRIPTION);
            String strNameCN = (String) classInfoMap.get(SELECT_ATTRIBUTE_TITLE);
            StringList typeSelectList = JF_Util_mxJPO.basicBolistSel();
            typeSelectList.add(SELECT_ATTRIBUTE_TITLE);
            boolean selectClassTypeIsRight = false ;
            Properties properties = JF_Util_mxJPO.readPageObject(context,"JFJDConfig");
            //标准库标题
            String strGFLibraryName = properties.getProperty("partitionLibrary.GF.Title");
            res.put("isLeaf",UIUtil.isNotNullAndNotEmpty(strIsLeafNodeFlag) && "FALSE".equalsIgnoreCase(strIsLeafNodeFlag));
            //返回了两个 不知道为啥会有bug chenyan 2025/07/21
//            MapList objectList = classBo.getRelatedObjects(context,
//                    "Subclass", //pattern to match relationships
//                    "General Library,General Class", //pattern to match types
//                    typeSelectList, //the eMatrix StringList object that holds the list of select statement pertaining to Business Obejcts.
//                    JF_Util_mxJPO.basicRellistSel(), //the eMatrix StringList object that holds the list of select statement pertaining to Relationships.
//                    true, //get To relationships
//                    false, //get From relationships
//                    (short) 0, //the number of levels to expand, 0 equals expand all.
//                    DomainConstants.EMPTY_STRING, //where clause to apply to objects, can be empty ""
//                    DomainConstants.EMPTY_STRING, //where clause to apply to relationship, can be empty ""
//                    (short)0, //limit
//                    false, //checkHidden
//                    true, //preventDuplicates
//                    (short)0, //pageSize
//                    null,
//                    null,
//                    null,
//                    "end") ;// end(返回叶子节点) relationship(返回关系pattern中最后一个关系) all(全返回);
            //业务规定不会存在引用 从下往上只会有条线
            MapList objectList = classBo.getRelatedObjects(context, "Subclass", // relationship pattern
                    "General Library,General Class",                                    // object pattern
                    typeSelectList,                            // object selects
                    JF_Util_mxJPO.basicRellistSel(), // relationship selects
                    true,                                        // to direction
                    false,                                        // from direction
                    (short) 0,                                    // recursion level
                    "",                // object where clause
                    "",
                    (short) 0);
            objectList.addSortKey(SELECT_LEVEL, ProgramCentralConstants.DESCENDING_SORT, ProgramCentralConstants.SORTTYPE_INTEGER);
            objectList.sort();
            // add by chenyan 新增 创建零件时只能选择单件 创建 总成时只能选择组合件 component(零件) assembly(装配) 2025/07/22
            _logger.info("objectList:{}",objectList);
            res.put("objectId",strObjectId);
            res.put("DetailEN",strNameEN);
            res.put("DetailCN",strNameCN);
            if (objectList.size() > 0){
                Map rootNodeMap  = (Map)objectList.get(0);
                String strLibTitle = (String) rootNodeMap.get(SELECT_ATTRIBUTE_TITLE);
                String  strLibRange = strLibTitle.split("-")[0].substring(strLibTitle.split("-")[0].length() - 1);

                res.put("rootRange",strLibRange);
//                if (objectList.size() >= 2 ) {
//                    //标准件range值为 F 标准件只能是零件
//
//                    //标识是总成还是零件
//                    Map partTypeMap  = (Map)objectList.get(1);
//                    String strClassTitle = (String) partTypeMap.get(SELECT_ATTRIBUTE_TITLE);
////                    "数据类型创建错误, 请确认组合件和单件信息是否准确!"
//                    if (JF_PLMConstants_mxJPO.CLASS_TITLE_ASSEMBLY_RANGE.equals(strCreateType) && JF_PLMConstants_mxJPO.CLASS_TITLE_ASSEMBLY.equals(strClassTitle)){
//                        selectClassTypeIsRight = true;
//                    }else if (JF_PLMConstants_mxJPO.CLASS_TITLE_COMPONENT_RANGE.equals(strCreateType) && JF_PLMConstants_mxJPO.CLASS_TITLE_COMPONENT.equals(strClassTitle)){
//                        selectClassTypeIsRight = true;
//                    }else if (strGFLibraryName.equals(strLibTitle) && JF_PLMConstants_mxJPO.CLASS_TITLE_COMPONENT_RANGE.equals(strCreateType)){
//                        selectClassTypeIsRight = true;
//                    }else {
//                        //获取国际化
//                        String strMess = EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", context.getLocale(), "emxComponents.Mess.XENAlert.SelectClassTypeError");
//                        res.put("mess",strMess);
//                    }
//                    res.put("selectClassTypeIsRight",selectClassTypeIsRight);
//                }

            }

        }catch (Exception e){
            _logger.error(e.getMessage());
            res.put("status","fail");
        }finally {
            ContextUtil.popContext(context);
        }
        Gson gson = new Gson();
        return gson.toJson(res);
    }
    /**
    *
    *@description 将时区偏移量字符串转换为 double 类型的偏移量（小时为单位
    *@param strTimeZone strTimeZone Asia/Shanghai
    *@return double 偏移量作为 double 类型的值（小时） 东八区为 -8 西八区为 +8
    *@throws 
    *@author CHENYAN
    *@date 2024/12/24 19:33
    */
    public static double convertOffsetToDouble(String strTimeZone) {
        double dRes ;
        try {
            ZoneOffset offset =  ZoneId.of(strTimeZone).getRules().getOffset(Instant.now());
            dRes =  - (offset.getTotalSeconds() / 3600.0);
        }catch (Exception e){
            _logger.error("时区转换异常 返回 默认值 0 :{}",e.getMessage());
            dRes = 0 ;
        }
       return dRes ;
    }

    /**
    *
    *@description 根据显示时间转换为数据库时间
    *@param locale 时区
	*@param strDisplayDate 显示时间
	*@param iDateTimeDisplayFormat 格式化样式
    *@return java.lang.String
    *@throws
    *@author CHENYAN
    *@date 2024/12/25 20:07
    */
    public static String getDbDateByDisplayDate(Locale locale , String strDisplayDate ,int iDateTimeDisplayFormat) throws Exception{
        String strDbDate = strDisplayDate ;
       try {
           //不符合获取达索的格式化值
           if (iDateTimeDisplayFormat > 3 || iDateTimeDisplayFormat < 0 ){
               iDateTimeDisplayFormat = eMatrixDateFormat.iDateTimeDisplayFormat;
           }
           DateFormat dateFormat = DateFormat.getDateTimeInstance(iDateTimeDisplayFormat,iDateTimeDisplayFormat, locale);
           Date date = dateFormat.parse(strDisplayDate);
           SimpleDateFormat matrixSdf = new SimpleDateFormat(eMatrixDateFormat.getEMatrixDateFormat());
           strDbDate = matrixSdf.format(date);
       }catch (Exception e){
           _logger.error("显示时间转换数据库时间异常：{}",e.getMessage());
           throw e;
       }
       return strDbDate;
    }

    public String checkColorGroupNameIsUniqueOnColorMatrix(Context context ,String[] args) throws Exception{
        Map reqMap = (Map) JPO.unpackArgs(args);
        String strObjectId = (String) reqMap.get("objectId");
        String strGroupName = (String) reqMap.get("groupNumber");
        DomainObject bo = DomainObject.newInstance(context, strObjectId);
        StringList groupNameList = bo.getInfoList(context, "from[JFColorMatrix2JFColorGroup].to.attribute[Title].value");
        Map resMap = new HashMap<>();
        if (!groupNameList.contains(strGroupName)) {
            resMap.put("code","200");
        }else {
            resMap.put("code","404");
            String strMess = EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", context.getLocale(), "emxComponents.Mess.ColorPartMess.GroupNameIsNotUnique");
            resMap.put("mess",strMess);
        }
        Gson gson = new Gson();
        return gson.toJson(resMap);
    }

    /**
    *
    *@description 判断颜色分组是否被EBOM引用 无色件和座椅不允许删除
    *@param context
	*@param args
    *@return java.lang.String
    *@throws
    *@author CHENYAN
    *@date 2025/2/10 20:22
    */
    public String checkColorGroupNameHasQuoteBOM(Context context ,String[] args) throws Exception{
        Map reqMap = (Map) JPO.unpackArgs(args);
        String strObjectId = (String) reqMap.get("objectId");
        DomainObject bo = DomainObject.newInstance(context, strObjectId);
        Map resMap = new HashMap<>();
        String strMess = EMPTY_STRING;
//        StringList groupNameList = bo.getInfoList(context, "from[JFColorMatrix2JFColorGroup].to.attribute[Title].value");
//        String strMess = EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", context.getLocale(), "emxComponents.ColorPartMess.ConfirmMess");
        strMess = EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", context.getLocale(), "emxComponents.ColorPartMess.ConfirmMessBOM");
        resMap.put("mess",strMess);
        Gson gson = new Gson();
        return gson.toJson(resMap);
    }
    /**
    *
    *@description 删除颜色分组
    *@param context
	*@param args
    *@return java.lang.String
    *@throws
    *@author CHENYAN
    *@date 2025/3/4 11:17
    */
    public String deleteColorGroupByGroupId(Context context ,String[] args) throws Exception{
        Map reqMap = (Map) JPO.unpackArgs(args);
        String strObjectId = (String) reqMap.get("objectId");
        Map resMap = new HashMap<>();
        String strMess = EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", context.getLocale(), "emxComponents.Mess.ColorPartMess.DelColorGroupSuccess");
        try {
            ContextUtil.startTransaction(context,true);
            DomainObject bo = DomainObject.newInstance(context, strObjectId);
            bo.deleteObject(context);
            ContextUtil.commitTransaction(context);
            resMap.put("code","200");
        } catch (Exception e) {
            ContextUtil.abortTransaction(context);
            _logger.error(e.getMessage());
            resMap.put("code","404");
            strMess = EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", context.getLocale(), "emxComponents.Mess.ColorPartMess.DelColorGroupError");
        }

        resMap.put("mess",strMess);
        Gson gson = new Gson();
        return gson.toJson(resMap);
    }

    public String checkColorStyleTitleIsUniqueOnColorMatrix(Context context ,String[] args) throws Exception{
        Map reqMap = (Map) JPO.unpackArgs(args);
        Map resMap = new HashMap<>();
        String strObjectId = (String) reqMap.get("objectId");
        String strStyleName = (String) reqMap.get("styleName");
        DomainObject bo = DomainObject.newInstance(context, strObjectId);
        StringList typeSelectList = JF_Util_mxJPO.basicBolistSel();
        typeSelectList.add(SELECT_ATTRIBUTE_TITLE);
        String strCode = "200";
        MapList maps = bo.getRelatedObjects(context, "JFColorMatrix2JFColorGroup,JFColorGroup2JFColorStyle" , // relationship pattern
                "JFColorGroup,JFColorStyle",                                    // object pattern
                typeSelectList,                            // object selects
                JF_Util_mxJPO.basicRellistSel(), // relationship selects
                false,                                        // to direction
                true,                                        // from direction
                (short) 2,                                    // recursion level
                "",                // object where clause
                "",
                (short) 0);
        for (int i = 0; i < maps.size(); i++) {
            Map info = (Map)maps.get(i);
            String strType = (String) info.get(SELECT_TYPE);
            if ("JFColorStyle".equals(strType)){
                String strExistStyleName = (String) info.get(SELECT_ATTRIBUTE_TITLE);
                if (strStyleName.equals(strExistStyleName)){
                    strCode = "404";
                    String strMess = EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", context.getLocale(), "emxComponents.Mess.ColorPartMess.StyleNameIsNotUnique");
                    resMap.put("mess",strMess);
                    break;
                }
            }
        }
        resMap.put("code",strCode);
        Gson gson = new Gson();
        return gson.toJson(resMap);
    }
    public String createCostAnalysisByBOM(Context context ,String[] args) throws Exception{
        Map reqMap = (Map) JPO.unpackArgs(args);
        Map resMap = new HashMap<>();
        String strSelectPartList = (String) reqMap.get("selectPartList");
        String[] selectPartIdArr = strSelectPartList.split(",");
        List selectLibMapList =  (List) reqMap.get("selectLibList");
        _logger.info("strSelectPartList:{}",strSelectPartList);
        _logger.info("strStyleName:{}",selectLibMapList);
//        DomainObject bo = DomainObject.newInstance(context, strObjectId);
        StringList typeSelectList = JF_Util_mxJPO.basicBolistSel();
        typeSelectList.add(SELECT_ATTRIBUTE_TITLE);
        String strCode = "200";
        String strMess = "";
        DomainObject partBO = DomainObject.newInstance(context);
        //选中的分类库id
        StringList selectPartList = StringList.create(SELECT_NAME, SELECT_REVISION, JF_PLMConstants_mxJPO.SELECT_ATTR_V_PART_NUMBER);
        // 定义格式为至少两位数字
        DecimalFormat df = new DecimalFormat("00");
        //前端已经过滤出选择最上层的level 只需要依次展开就行
        //实际需要创建的分类库集合
        MapList actualLibMapList = new MapList();
        // 获取到有默认值的属性 add by chenyan 2025/05/29
        Map attrSettingMap = JF_CostAnalysis_mxJPO.getCostAnalysisAttrSettingByPage(context);
        _logger.info("attrSettingMap :{}",attrSettingMap);
        Map defaultMap = new HashMap();
        for (Object oEntry : attrSettingMap.entrySet()){
            Map.Entry entry = (Map.Entry)oEntry;
            Map settingMap = (Map) entry.getValue();
            String strAttrName = (String) entry.getKey();
            String strDefault = (String) settingMap.get("default");
            if (UIUtil.isNotNullAndNotEmpty(strDefault)){
                defaultMap.put(strAttrName,strDefault);
            }
        }
        _logger.info("defaultMap filter before :{}",defaultMap);
        DomainObject libBO = DomainObject.newInstance(context);
        for (int i = 0; i < selectLibMapList.size(); i++) {
            Map libMap = (Map) selectLibMapList.get(i);
            String strLibId = (String) libMap.get("objectId");
            libBO.setId(strLibId);
            String strIsOneLevelClass = (String) libMap.get("isOneLevelClass");
//            if ("0".equalsIgnoreCase(strIsOneLevelClass)){
//                Map libInfo = libBO.getInfo(context, typeSelectList);
//                actualLibMapList.add(libInfo);
//            }
            //  add by chenyan 2025/05/26  选中结点需要加入
            Map libInfo = libBO.getInfo(context, typeSelectList);
            actualLibMapList.add(libInfo);
            MapList maps = libBO.getRelatedObjects(context, "Subclass" , // relationship pattern
                    "General Class",                                    // object pattern
                    typeSelectList,                            // object selects
                    JF_Util_mxJPO.basicRellistSel(), // relationship selects
                    false,                                        // to direction
                    true,                                        // from direction
                    (short) 0,                                    // recursion level
                    "",                // object where clause
                    "",
                    (short) 0);
            actualLibMapList.addAll(maps);
        }
        _logger.info("actualLibMapList:{}",actualLibMapList);
        try {
            ContextUtil.startTransaction(context,true);
            for (int i = 0; i < selectPartIdArr.length; i++) {
                StringBuffer sbCosName = new StringBuffer();
                String strPartId = selectPartIdArr[i];
                partBO.setId(strPartId);
                Map partBOInfo = partBO.getInfo(context, selectPartList);
                String strPartName = (String) partBOInfo.get("attribute[EnterpriseExtension.V_PartNumber]");
                String strPartRevision = (String) partBOInfo.get(SELECT_REVISION);
                strPartName = UIUtil.isNullOrEmpty(strPartName) ? (String) partBOInfo.get(SELECT_NAME) : strPartName;
                //零件号
                sbCosName.append(strPartName);
                sbCosName.append("_");
                //零件版本
                sbCosName.append(strPartRevision);
                sbCosName.append("_");
                //流水号
                String strSerialnumber = df.format(i+1);
                sbCosName.append(strSerialnumber);
                //add by ljr
                //判断是否有成本分析
                String strCostId = partBO.getInfo(context, "from[JFVPMReference2CostAnalysis].to.id");
                DomainObject newObj = DomainObject.newInstance(context);
                StringList hasCostItemList = new StringList();
                Boolean isCreate = Boolean.TRUE;
                if (UIUtil.isNullOrEmpty(strCostId)) {
                    //没有,新建成本分析
                    newObj.createObject(context, JF_PLMConstants_mxJPO.TYPE_JFCostAnalysis, sbCosName.toString(), "", JF_PLMConstants_mxJPO.POLICY_JFCostAnalysis, context.getVault().getName());
                    newObj.setAttributeValue(context,"Title",strPartName+"成本分析");
                } else {
                    newObj.setId(strCostId);
                    String strMql = "from[JFCostAnalysis2CostItem].to." + SELECT_ATTRIBUTE_TITLE;
                    hasCostItemList = newObj.getInfoList(context, strMql);
                    isCreate = Boolean.FALSE;
                }
                //end
                //新建的成本分析项id集合
                StringList newItemIdList = new StringList();
                DomainObject newItemBO = DomainObject.newInstance(context);
                for (int i1 = 0; i1 < actualLibMapList.size(); i1++) {
                    Map actualInfoMap = (Map) actualLibMapList.get(i1);
                    String strLibTitle = (String) actualInfoMap.get(SELECT_ATTRIBUTE_TITLE);
                    String strClassId = (String) actualInfoMap.get(SELECT_ID);
                    if (hasCostItemList.contains(strLibTitle)) {
                        continue;
                    }
                    //新建成本分析项
                    String strCosItemId = FrameworkUtil.autoName(context,  "type_JFCostAnalysisItem", "policy_JFCostAnalysisItem");
                    newItemBO.setId(strCosItemId);
                    newItemBO.setAttributeValue(context,ATTRIBUTE_TITLE,strLibTitle);
                    newItemIdList.add(strCosItemId);
                    // 入库
                    DomainRelationship.connect(context, strClassId, "Classified Item", strCosItemId, true);

                    //初始化成本项的有默认值的属性
//
                    if (defaultMap.size() > 0) {
                        //移除默认属性中不存在的属性
                        defaultMap.entrySet().removeIf(oEntry ->{
                                    Map.Entry entry = (Map.Entry)oEntry;
                                    String strAttrName = (String) entry.getKey();
                                    boolean isExist = JF_PublicMethodClass_mxJPO.checkBOHasAttribute(context, newItemBO,strAttrName);
                                     return !isExist;
                        });

                    }
                    _logger.info("defaultMap filter after:{}",defaultMap);
                    if (defaultMap.size() > 0){
                        newItemBO.setAttributeValues(context,defaultMap);
                    }

                }
                //连接成本细分和成本分析项
                if (newItemIdList.size() > 0){
                    DomainRelationship.connect(context,newObj, JF_PLMConstants_mxJPO.rel_JFCostAnalysis2CostItem, true, newItemIdList.toStringArray());
                }
                if (isCreate) {
                    //连接零件和成本分析
                    try{
                        ContextUtil.pushContext(context);
                        DomainRelationship.connect(context, strPartId, JF_PLMConstants_mxJPO.rel_JFVPMReference2CostAnalysis, newObj.getId(context), true);
                    }finally {
                        ContextUtil.popContext(context);
                    }
                }

            }
            ContextUtil.commitTransaction(context);
            strMess = EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", context.getLocale(), "emxComponents.Mess.CostAnalysis.CreateSuccess");

        } catch (Exception e){
            strCode = "404";
            strMess = EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", context.getLocale(), "emxComponents.Mess.CostAnalysis.CreateFail");
            _logger.error(e.getMessage());
            e.printStackTrace();
            ContextUtil.abortTransaction(context);
        }
        resMap.put("code",strCode);
        resMap.put("mess",strMess);
        Gson gson = new Gson();
        return gson.toJson(resMap);
    }

    /**
    *
    *@description 计算报价价格
    *@param context
	*@param args
    *@return java.lang.String
    *@throws
    *@author CHENYAN
    *@date 2025/3/31 16:39
    */
    public String calcEvaluatePriceProcess(Context context ,String[] args) throws Exception {
        Map reqMap = (Map) JPO.unpackArgs(args);
        Map resMap = new HashMap<>();
        //属性集合
        List arrFields = (List) reqMap.get("arrFields");
        //计算的属性集合
        MapList calcAttrMapList = new MapList();
        FileInputStream fis = null;
        Gson gson = null;
        try {
            String strClassPath = Thread.currentThread().getContextClassLoader().getResource("").getPath();
            _logger.info("strClassPath:{}",strClassPath);
            String strSPacePath = JF_PublicMethodClass_mxJPO.get3DspaceServicePath("lib");
            strSPacePath = JF_PublicMethodClass_mxJPO.buildStringInStrings(strSPacePath,"webapps/3dspace/jf_template/CalcEvaluatePriceTemple.xlsx");
            _logger.info("strFilePath:{}",strSPacePath);
            fis = new FileInputStream(strSPacePath);
            Workbook workbook = new XSSFWorkbook(fis);
            // 创建公式评估器并评估单元格
            FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();
            //根据interface分组
            Map groupMap = (Map)arrFields.stream().filter(m ->{
                Map infoMap = (Map) m;
                return UIUtil.isNotNullAndNotEmpty((String) infoMap.get("interfaceName"));
            }).collect(Collectors.groupingBy(m -> {
                Map infoMap = (Map) m;
                return infoMap.get("interfaceName");
            }));
            _logger.info("groupMap:{}",groupMap);
            for(Object oEntry : groupMap.entrySet()){
                Map.Entry entry = (Map.Entry)oEntry;
                String strKey = (String) entry.getKey();
                List attrList = (List) entry.getValue();
                //获取到excel中得属性
                Map<String, List<Row>> excelAttrMap = getExcelAttrListByInterface(workbook, strKey);
                List<Row> inputAttrList = excelAttrMap.get("inputAttrList");
                //先写入input输入属性值
                if (Objects.nonNull(inputAttrList) && inputAttrList.size() > 0){
                    for (int i = 0; i < inputAttrList.size(); i++) {
                        Row row = inputAttrList.get(i);
                        writeExcelByAttrList(row,attrList);
                    }
                }
                List<Row> formulaAttrList = excelAttrMap.get("formulaAttrList");
                if (Objects.nonNull(formulaAttrList) && formulaAttrList.size() > 0){
                    for (int i = 0; i < formulaAttrList.size(); i++) {
                        Row row = formulaAttrList.get(i);
                        Cell attrNamCell = row.getCell(1);
                        Cell formulaCell = row.getCell(2);
                        String strAttrName = attrNamCell.getStringCellValue();
                        CellValue cellValue = evaluator.evaluate(formulaCell);
                        String strCalcValue = cellValue.formatAsString();
                        _logger.info("strCalcValue:{}",strCalcValue);
                        HashMap<String, String> calcAttrMap = new HashMap<>();
                        calcAttrMap.put("attrName",strAttrName);
                        calcAttrMap.put("calcValue",strCalcValue);
                        calcAttrMapList.add(calcAttrMap);
                    }
                }
            }
            //防止多interface 理应只有一个
            String strObjectId = (String) reqMap.get("objectId");
            _logger.info("arrFields:{}",arrFields);
            _logger.info("strObjectId:{}",strObjectId);
            resMap.put("calcAttrMapList",calcAttrMapList);
            resMap.put("code","200");
            resMap.put("mess","success");
            workbook.close();
            gson = new Gson();
        } catch (FileNotFoundException e) {
            _logger.error(e.getMessage());
        }finally {
            if (Objects.nonNull(fis)){
                fis.close();
            }
        }
        return gson.toJson(resMap);
    }


    /**
     * 计算报价价格 直接计算 不通过Excel
     * @param context
     * @param args
     * @return
     * @throws Exception
     */
    public String calcEvaluatePriceProcessNewFunction(Context context ,String[] args) throws Exception {
        Map reqMap = (Map) JPO.unpackArgs(args);
        Map resMap = new HashMap<>();
        //属性集合
        List arrFields = (List) reqMap.get("arrFields");
        Map arrFieldsMap = (Map) arrFields.stream().collect(Collectors.toMap(
                field-> ((Map)field).get("fieldName"),
                field-> ((Map)field).get("fieldValue")
        ));
        String strObjectId = (String) reqMap.get("objectId");
        //计算的属性集合
        MapList calcAttrMapList = new MapList();
        Gson gson = null;
        try{
            String interfaceDump = MqlUtil.mqlCommand(context, false, "pri bus '" + strObjectId + "' select interface dump", true);
            Map calcAttrMap = calculatePrice(context,arrFieldsMap,interfaceDump);
            _logger.info("calcEvaluatePriceProcessNewFunction------calcAttrMap::{}", JSON.toJSONString(calcAttrMap));
            calcAttrMapList.add(calcAttrMap);
            resMap.put("calcAttrMapList",calcAttrMapList);
            resMap.put("code","200");
            resMap.put("mess","success");
            gson = new Gson();
        }catch (Exception e){
            _logger.info("JF_ECRRESTService-----------calcEvaluatePriceProcessNewFunction--error:",e);
        }
        return gson.toJson(resMap);
    }

    public Map calculatePrice(Context context, Map arrFieldsMap, String interfaceDump) {
        Map<String,String> map = new HashMap<>();
        double price = 0;
        double referencePrice = getDoubleValue(arrFieldsMap, "JF_ReferPrice");
        double A1 = 0;
        double A2 = 0;
        double B1 = 0;
        double B2 = 0;
        double C1 = 0;
        double D1 = 0;
        double E1 = 0;
        double F1 = 0;
        switch (interfaceDump){
            case "JFCostFoamMaterials":
                //发泡
                A1 = getDoubleValue(arrFieldsMap, "JF_FucFoamNumberMoldFrames");
                A2 = getDoubleValue(arrFieldsMap, "JF_FucFoamCostPerMold");
                B1 = getDoubleValue(arrFieldsMap, "JF_FucFoamTotalWeight1");
                B2 = getDoubleValue(arrFieldsMap, "JF_FucFoamTotalWeight");
                C1 = getDoubleValue(arrFieldsMap, "JF_FucFoamUnitPricePerKilogram");
                price = referencePrice + C1 * (B2 - B1) + A1 * A2;
                break;
            case "JFCostCommonPlasticParts":
                //塑料件
                A1  =getDoubleValue(arrFieldsMap, "JF_FucComPlasticWeight1");
                A2 = getDoubleValue(arrFieldsMap, "JF_FucComPlasticWeight");
                B1 = getDoubleValue(arrFieldsMap, "JF_FucComPlasticNumber1");
                B2 = getDoubleValue(arrFieldsMap, "JF_FucComPlasticNumber");
                C1  =getDoubleValue(arrFieldsMap, "JF_FucComPlasticAverageUnitPrice");
                D1 = getDoubleValue(arrFieldsMap, "JF_FucComPlasticCompensationCoefficient");
                if (B1==B2){
                    price = referencePrice + C1 * (A2 - A1) /0.9*1.1;
                }else {
                    price = referencePrice + C1 * (A2 - A1) /D1;

                }
                break;
            case "JFCostOtherMetalParts":
                //钣金
                A1 = getDoubleValue(arrFieldsMap, "JF_FucOthMetalWeight1");
                A2 = getDoubleValue(arrFieldsMap, "JF_FucOthMetalWeight");
                C1 = getDoubleValue(arrFieldsMap, "JF_FucOthMetalUnitPricePerKilogram");
                price = referencePrice+C1*(A2-A1);
                break;
            case "JFCostMainCover":
                //面套
                A1 =  getDoubleValue(arrFieldsMap, "JF_FucCoverUnitPrice");
                B1 =  getDoubleValue(arrFieldsMap, "JF_FucCoverNetarea");
                C1 =  getDoubleValue(arrFieldsMap, "JF_FucCoverUtilization");
                D1 =  getDoubleValue(arrFieldsMap, "JF_FucSewingHours");
                E1 =  getDoubleValue(arrFieldsMap, "JF_FucCoverUnitPriceLabor");
                F1 =  getDoubleValue(arrFieldsMap, "JF_FucCoverAxuPrices");
                price = (A1*B1/C1+F1+0.6*D1*E1)*1.1;
                break;
            case "JFCostWiringHarness":
                //线束
                A1 = getDoubleValue(arrFieldsMap, "JF_FucHarnessLoopsNB1");
                B1 = getDoubleValue(arrFieldsMap, "JF_FucHarnessLoopsNB");
                C1 = getDoubleValue(arrFieldsMap, "JF_FucHarnessLoopUnitPrice");
                if (A1==B1){
                    price = referencePrice;
                }else {
                    price = referencePrice+C1*(B1-A1)*1.1;
                }
                break;
            default:
                double JF_OfferUsage= getDoubleValue(arrFieldsMap, "JF_OfferUsage");
                double JF_ReferPrice= getDoubleValue(arrFieldsMap, "JF_ReferPrice");
                double JF_ReferUsage= getDoubleValue(arrFieldsMap, "JF_ReferUsage");
                price = JF_OfferUsage*(JF_ReferPrice/JF_ReferUsage);
                break;
        }
        map.put("attrName","JF_EvaluatePrice");
        DecimalFormat df = new DecimalFormat("#.00");
        map.put("calcValue",String.valueOf(df.format(price)));
        return map;
    }

    public double getDoubleValue(Map<String, Object> arrFieldsMap, String key) {
        Object value = arrFieldsMap.get(key);
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        } else if (value instanceof String) {
            String strValue = ((String) value).trim();
            // 使用正则表达式去掉所有非数字和小数点的字符
            strValue = strValue.replaceAll("[^\\d.]", "");
            try {
                return Double.parseDouble(strValue);
            } catch (NumberFormatException e) {
                // 处理字符串无法解析为 double 的情况
                _logger.info("Invalid number format for key: " + key + ", value: " + strValue);
                return 0;
            }
        }
        return 0;
    }

    /**
    *
    *@description 获取excel模版中得interface对应的属性
    *@param workbook
	*@param strInterfaceName
    *@return java.util.Map<java.lang.String,java.util.List<org.apache.poi.ss.usermodel.Row>>
    *@throws
    *@author CHENYAN
    *@date 2025/4/1 15:45
    */

    public static Map<String,List<Row>>getExcelAttrListByInterface(Workbook workbook, String strInterfaceName){
        // 获取所有Sheet的数量
        int sheetCount = workbook.getNumberOfSheets();
        HashMap<String, List<Row>> res = new HashMap<>();
//        //输入属性集合
        ArrayList<Row> inputAttrList = new ArrayList<>();
        //公式集合
        ArrayList<Row> formulaAttrList = new ArrayList<>();
        res.put("inputAttrList",inputAttrList);
        res.put("formulaAttrList",formulaAttrList);
        for (int i = 0; i < sheetCount; i++) {
            Sheet sheet = workbook.getSheetAt(i);
            //获取sheet名称
            String strSheetName = sheet.getSheetName();
            if (strInterfaceName.equals(strSheetName)){
                int lastRowNum = sheet.getLastRowNum();
                for (int i1 = 1; i1 <= lastRowNum; i1++) {
                    Row row = sheet.getRow(i1);
                    if (JF_CostAnalysis_mxJPO.checkAttrIsFormula(row,2)) {
                        formulaAttrList.add(row);
                    }else {
                        inputAttrList.add(row);
                    }
                }
            }
        }
        return res;
    }


    public void writeExcelByAttrList(Row row ,List attrList ){
        Cell cell0 = row.getCell(1);
        Cell cell1 = row.getCell(2);
        if (Objects.isNull(cell1)){
            cell1 = row.createCell(2);
        }
        String strAttrName = cell0.getStringCellValue();
        for (int i = 0; i < attrList.size(); i++) {
            Map attrInfo = (Map) attrList.get(i);
            String strFieldName = (String) attrInfo.get("fieldName");
            if (strAttrName.equals(strFieldName)){
                String strFieldValue = (String) attrInfo.get("fieldValue");
                try {
                    if (UIUtil.isNotNullAndNotEmpty(strFieldValue)){
                        double dFieldValue = Double.parseDouble(strFieldValue);
                        _logger.info("strAttrName :{} dFieldValue :{}",strAttrName,strFieldValue);
                        //设置到excel
                        cell1.setCellValue(dFieldValue);
                    }
                    break;
                } catch (NumberFormatException e) {
                    _logger.info(e.getMessage());
                }

            }
        }
    }

    /**
    *
    *@description 更新受影响项目
    *@param context
	*@param args
    *@return java.lang.String
    *@throws
    *@author CHENYAN
    *@date 2025/6/5 16:56
    */

    public String updateJFAffectedProject( Context context ,String[] args) throws Exception{
        Gson gson = new Gson();
        Map reqMap = (Map) JPO.unpackArgs(args);
        Map resMap = new HashMap();
        String strECRId = (String) reqMap.get("objectId");
        DomainObject ecr = DomainObject.newInstance(context,strECRId);
        StringList reSelectList = new StringList();
        reSelectList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JFROUTENOTE_APPROVALAROLE);
        StringList typeSelectList = new StringList();
        typeSelectList.add(SELECT_ID);
        typeSelectList.add(SELECT_NAME);
        typeSelectList.add(SELECT_DESCRIPTION);
        JPO.invoke(context, "JF_NewECRService",new String[]{} , "connectAffectedProject", new String[]{strECRId}, void.class);
        MapList maps;
        try {
            ContextUtil.pushContext(context);
            maps = ecr.getRelatedObjects(context, "JFECR2AffectedProject", // relationship pattern
                    TYPE_PROJECT_SPACE,                                    // object pattern
                    typeSelectList,                            // object selects
                    reSelectList, // relationship selects
                    false,                                        // to direction
                    true,                                        // from direction
                    (short) 1,                                    // recursion level
                    "",                // object where clause
                    "",
                    (short) 0);
        } finally {
            ContextUtil.popContext(context);
        }
        JF_NewECRService_mxJPO jfNewECRServiceMxJPO = new JF_NewECRService_mxJPO();
        String strReplaceHtml = jfNewECRServiceMxJPO.buildLinkProjectHtml(maps);
        resMap.put("html",strReplaceHtml);
        resMap.put("code","200");
        String strMess = EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", context.getLocale(), "emxComponents.Button.UpdateProjectSuccess");
        resMap.put("mess",strMess);
        String strResHtml = gson.toJson(resMap);
        return strResHtml;
    }

    /**
    *
    *@description 获取查询零件库的条件
    *@param context
	*@param args
    *@return java.lang.String
    *@throws
    *@author CHENYAN
    *@date 2025/7/17 16:07
    */

    public String getLibQueryByXENApp(Context context ,String[] args) {
        Gson gson = new Gson();
        HashMap res = new HashMap<>();
        try {
            Properties properties = JF_Util_mxJPO.readPageObject(context,"JFJDConfig");
            String strQueryLabel = "Library.XENQuery.NotContainStandardName" ;
            String strLoginUser = context.getUser();
            String strMQLRes = MqlUtil.mqlCommand(context, "print role $1 select person dump ;", true, new String[]{"JfStandardAdmin"});
            _logger.info("strMQLRes :{}",strMQLRes);
            String[] splitPerson = strMQLRes.trim().split(",");
            //  标准件管理员才有GF-标准件库
            boolean hasRole = StringList.create(splitPerson).contains(strLoginUser);
            if (hasRole){
                strQueryLabel = "Library.XENQuery.ContainStandardName";
            }
            String strQuery = properties.getProperty(strQueryLabel);
            res.put("query",strQuery);
            res.put("code","200");
        } catch (Exception e) {
            res.put("code","404");
        }
        return gson.toJson(res);
    }

    /**
    *
    *@description 获取零件中未填写的属性；XEN可跳过不再使用的DirectBuy、客户零件号、客户零件版本属性
    *@param context
	*@param args
    *@return java.lang.String
    *@throws
    *@author CHENYAN
    *@date 2025/9/5 11:21
    */
    public String getNotWriteAttrByPart(Context context ,String[] args) {
        _logger.info("------------------------------- getNotWriteAttrByPart begin ---------------------------------------------");
        Gson gson = new Gson();
        HashMap res = new HashMap<>();
        try {
            Map  argsMap = JPO.unpackArgs(args);
            _logger.info("argsMap:{}",argsMap);
            List partIdList = (List) argsMap.get("partIdList");
            String strMess = "";
            if (Objects.nonNull(partIdList) && partIdList.size() > 0){
                DomainObject part = DomainObject.newInstance(context);
                StringList messList = new StringList(partIdList.size());
                for (int i = 0; i < partIdList.size(); i++) {
                    String strPartId = (String) partIdList.get(i);
                    part.setId(strPartId);
                    Map partMap = part.getInfo(context, StringList.create(JF_PLMConstants_mxJPO.SELECT_ATTR_JF_Detail_CN,JF_PLMConstants_mxJPO.SELECT_ATTR_V_PART_NUMBER,SELECT_NAME,SELECT_TYPE));
                    String  strPartType = (String)partMap.get(SELECT_TYPE);
                    //只能是物理产品才走此逻辑
                    if (JF_PLMConstants_mxJPO.TYPE_VPMReference.equals(strPartType)){
                        String strTempMess = EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", context.getLocale(), "emxComponents.XENWidgetMess.CheckRequireAttr");

                        String  strDetailCN = (String)partMap.get(JF_PLMConstants_mxJPO.SELECT_ATTR_JF_Detail_CN);
                        String  strPartNum = (String)partMap.get(JF_PLMConstants_mxJPO.SELECT_ATTR_V_PART_NUMBER);
                        _logger.info("strDetailCN:{}",strDetailCN);
                        _logger.info("strPartId:{}",strPartId);
                        if (UIUtil.isNullOrEmpty(strPartNum)){
                            strPartNum = (String)partMap.get(SELECT_NAME);
                        }
                        JF_DR_mxJPO jfDrMxJPO = new JF_DR_mxJPO();
                        StringList requireAttribute = jfDrMxJPO.getRequireAttribute(context, strPartId, strDetailCN, new StringList());
                        //20260720 update by ljr XEN创建后不再校验DirectBuy、客户零件号、客户零件版本。
                        requireAttribute.removeAll(StringList.create(
                                JF_PLMConstants_mxJPO.ATTR_JFDIRECT_BUY,
                                JF_PLMConstants_mxJPO.ATTR_CustomerPartNumber,
                                JF_PLMConstants_mxJPO.ATTR_CustomerPartRevision,
                                "JF_VPMReference.JF_CustomerPartNumber",
                                "JF_VPMReference.JF_CustomerPartRevision"
                        ));
                        if(requireAttribute.contains("connectdrw")){
                            requireAttribute.remove("connectdrw");
                        }
                        _logger.info("requireAttribute:{}",requireAttribute);
                        StringList nlsAttrList = new StringList(requireAttribute.size());
                        if (requireAttribute.size() > 0){
                            for (int j = 0; j < requireAttribute.size(); j++) {
                                nlsAttrList.add(EnoviaResourceBundle.getAttributeI18NString(context,requireAttribute.get(j),context.getLocale().getLanguage()));
                            }
                        }
                        if (nlsAttrList.size() > 0){
                            String strPartMess = strTempMess.replace("{0}", nlsAttrList.join(",")).replace("{1}", strPartNum);
                            messList.add(strPartMess);
                        }
                    }

                }
                strMess = messList.join("<br>");
            }


            res.put("mess",strMess);
            res.put("code","200");
        } catch (Exception e) {
            res.put("code","404");
        }
        _logger.info("------------------------------- getNotWriteAttrByPart end ---------------------------------------------");

        return gson.toJson(res);
    }

    /**
    *
    *@description 递归查询分类是在组合件分类还是单件分类下
    *@param dataMapList
	*@param strToObjectId
    *@return java.util.Map
    *@throws
    *@author CHENYAN
    *@date 2025/7/22 22:22
    */
//    public Map recursionFindPartTypeByClass(MapList dataMapList ,String strToObjectId){
//        dataMapList.stream().filter(m ->{
//            Map infoMap = (Map)m;
//            return strToObjectId.equals(infoMap.get(SELECT_TO_ID));
//        }).collect(Collectors.groupingBy( m ->{
//            Map infoMap = (Map)m;
//            return infoMap.get(SELECT_TO_ID);
//        }));
//    }

    /**
     * 树结构展开时按登录角色获取过滤UQL
     * @param context
     * @param args
     * @return
     * @author caipan
     */
    public String getExpandQueryByRole(Context context ,String[] args) {
        Gson gson = new Gson();
        HashMap res = new HashMap<>();
        try {
            Properties properties = JF_Util_mxJPO.readPageObject(context,"JFJDConfig");
            String strQueryLabel = getExpandQueryConfigKey(context);
            String strQuery = "";
            if (UIUtil.isNotNullAndNotEmpty(strQueryLabel)) {
                strQuery = normalizeExpandUql(properties.getProperty(strQueryLabel));
            }
            _logger.info("getExpandQueryByRole strQueryLabel:{}, strQuery:{}",strQueryLabel,strQuery);
            res.put("query",strQuery);
            res.put("code","200");
        } catch (Exception e) {
            _logger.error("getExpandQueryByRole error:",e);
            res.put("code","404");
        }
        return gson.toJson(res);
    }

    private String getExpandQueryConfigKey(Context context) throws Exception {
        String strLoginUser = context.getUser();
        _logger.info("getExpandQueryConfigKey strLoginUser:{}",strLoginUser);
        if (hasRolePerson(context,"JfStandardAdmin_Foaming",strLoginUser)) {
            return "project.JfStandardAdmin_Foaming";
        } else if (hasRolePerson(context,"JfStandardAdmin_Trim",strLoginUser)) {
            return "project.JfStandardAdmin_Trim";
        } else if (hasRolePerson(context,"JfStandardAdmin_Harness",strLoginUser)) {
            return "project.JfStandardAdmin_Harness";
        }
        return "project.JfStandardAdmin_Default";
    }

    private boolean hasRolePerson(Context context,String strRole,String strLoginUser) throws Exception {
        if (UIUtil.isNullOrEmpty(strRole) || UIUtil.isNullOrEmpty(strLoginUser)) {
            return false;
        }
        String strMQLRes = MqlUtil.mqlCommand(context, "print role $1 select person dump ;", true, new String[]{strRole});
        _logger.info("hasRolePerson strRole:{}, strMQLRes:{}",strRole,strMQLRes);
        if (UIUtil.isNullOrEmpty(strMQLRes)) {
            return false;
        }
        String[] splitPerson = strMQLRes.trim().split(",");
        return StringList.create(splitPerson).contains(strLoginUser);
    }

    private String normalizeExpandUql(String strQuery) {
        if (UIUtil.isNullOrEmpty(strQuery)) {
            return "";
        }
        strQuery = strQuery.trim();
        if (strQuery.startsWith("\"uql\"")) {
            int iColonIndex = strQuery.indexOf(":");
            if (iColonIndex > -1) {
                strQuery = strQuery.substring(iColonIndex + 1).trim();
            }
            if (strQuery.startsWith("\"") && strQuery.endsWith("\"") && strQuery.length() > 1) {
                strQuery = strQuery.substring(1,strQuery.length() - 1);
            }
        }
        strQuery = strQuery.replace("\\\\\"","\"");
        strQuery = strQuery.replace("\\\"","\"");
        return strQuery;
    }

    /**
     * XEN创建物理产品选择书签的时候，去掉项目文档库
     * @param context
     * @param args
     * @return
     * @author caipan
     */
    public String getWorkspaceQueryByXENApp(Context context ,String[] args) {
        Gson gson = new Gson();
        HashMap res = new HashMap<>();
        try {
            Properties properties = JF_Util_mxJPO.readPageObject(context,"JFJDConfig");
            String strQueryLabel = "Workspace.XENQuery" ;
            //"flattenedtaxonomies:\"types/Workspace\" AND (NOT([label]:(\"项目文档库\")))";
            String strQuery = properties.getProperty(strQueryLabel);
            _logger.info("strQuery:{}",strQuery);
            res.put("query",strQuery);
            res.put("code","200");
        } catch (Exception e) {
            res.put("code","404");
        }
        return gson.toJson(res);
    }
}
