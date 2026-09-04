import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.DomainRelationship;
import com.matrixone.apps.domain.util.*;
import com.matrixone.apps.framework.ui.UIUtil;
import matrix.db.*;
import matrix.util.StringList;
import org.apache.commons.text.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static com.matrixone.apps.domain.DomainConstants.*;

public class JF_ChangeExecutionECOSource_mxJPO implements JF_PLMConstants_mxJPO{
    private static final String TYPE_JFECO = "JFECO";
    private static final String SYMBOLIC_TYPE_JF_ECOTASK = "type_JF_ECOTask";
    private static final String SYMBOLIC_TYPE_JFECO = "type_JFECO";
    private static final String TYPE_JF_ECOTASK = "JF_ECOTask";
    private static final String SYMBOLIC_POLICY_JFECO = "policy_JFECO";
    private static final String RELATIONSHIP_JFRELATEITEM = "JFRelateItem";
    private static final String RELATIONSHIP_SUBCLASS = "Subclass";
    private static final String TYPE_VPMREFERENCE = "VPMReference";
    private static final String TYPE_DRAWING = "Drawing";
    private static final String TYPE_GENERALCLASS = "General Class";
    private static final String SUITE_KEY = "emxFrameworkStringResource";
    private static final String STATE_RELEASED = "RELEASED";
    private static final String LIBRARY_KEY = "ECOPlanTemplateLibrary";
    private static final String RELATIONSHIP_JFECR2CO = "JFECR2CO";
    private static final String RELATIONSHIP_JFCHANGE2PROJECT = "JFChange2Project";
    private static final String RELATIONSHIP_JFCO2ECOTASK = "JFCO2ECOTask";
    private static final String RELATIONSHIP_XCADBASEDEPENDENCY = "XCADBaseDependency";
    private static final String STATE_JFDELAYEDFILING_REVIEW = "state_Review";
    private static final String DELAY_FILING_ROUTE_TITLE = "ECO Execution Plan Delay Request";
    private static final String[] stateIndex = new String[]{"Create", "Assign", "Active", "Review", "Complete"};
    private static final String BREAKPOINT_MODE_NA = "NA";
    private static final String BREAKPOINT_PLAN_TASK_TITLE_CN = "\u6574\u6905\u5207\u6362/\u65AD\u70B9\u8BA1\u5212\u53D1\u5E03\u53D1\u5E03\u7ED9\u9879\u76EE\u7EC4";
    private static final String BREAKPOINT_PLAN_TASK_TITLE_EN = "Seat switch/break point plan release";
    private static final String BREAKPOINT_PLAN_TASK_TITLE_CN_EN = BREAKPOINT_PLAN_TASK_TITLE_CN + "/" + BREAKPOINT_PLAN_TASK_TITLE_EN;
    private static JF_Util_mxJPO jfUtilMxJPO = new JF_Util_mxJPO();
    private static StringList bosel = jfUtilMxJPO.basicBolistSel();
    private static StringList relsel = new StringList();

    private static final Logger logger = LoggerFactory.getLogger(JF_ChangeExecutionECOSource_mxJPO.class);
    public JF_ChangeExecutionECOSource_mxJPO() {
        relsel.add(DomainRelationship.SELECT_ID);
    }

    /**
     * @description 获取所有的JFECO对象
     * @param context
     * @param args
     * @author lsa
     * @throws
     * @return void
     */
    public MapList getAllJFECOContent(Context context, String[] args) throws FrameworkException {
        //新增DA管理员可以查看所有的ECO add by chenyan 2025/04/03
        String strLoginUser = context.getUser();
        Vector assignments = PersonUtil.getAssignments(context,strLoginUser );
        boolean isShowAll = false;
        if (assignments.contains(JF_PLMConstants_mxJPO.ROLE_ECOADMIN)) {
            isShowAll = true;
        }
        String strWhere = "";
        if (!isShowAll) {
            strWhere = JF_PublicMethodClass_mxJPO.buildStringInStrings("owner=='", strLoginUser, "'");
        }
        MapList mapList = DomainObject.findObjects(context, TYPE_JFECO, "*" ,strWhere, bosel);
        return mapList;
    }

    /**
     * @description 通过ECR创建的ECO对象
     * @param context
     * @param args String args[]
     * @author lsa
     * @throws
     * @return void
     */
    public void createECOObject(Context context, String[] args) throws FrameworkException {
        String objectId = args[0];
        String owner = "";
        try {
            ContextUtil.pushContext(context);
            DomainObject ecrObj = new DomainObject(objectId);
            StringList ecoTaskList = ecrObj.getInfoList(context, "from[" + RELATIONSHIP_JFECR2CO + "].to.id");
            logger.info("ecoTaskList:{}", ecoTaskList.size());
            if (ecoTaskList.size() > 0) {
                //如果已经生成任务 不再次生成！
                return;
            }
            bosel.add(SELECT_OWNER);
            Map map = ecrObj.getRelatedObject(context, RELATIONSHIP_JFCHANGE2PROJECT, true, bosel, relsel);
            String description = ecrObj.getInfo(context, "description");
            if(map != null){
                owner = (String) map.get(SELECT_OWNER);
                String projectId = (String) map.get(SELECT_ID);
                 owner =  JF_Util_mxJPO.getProjectManager(context,new String[]{projectId});
                // 获取ECR对象的属性
                Map attributeMap = ecrObj.getAttributeMap(context);
                // mod by chenyan 修改ECO同步ECR属性 只同步指定属性
                Map syncAttributeMap = getECRSyncAttributeList(attributeMap);
                String ecrType = (String) attributeMap.get("JFECRType");
                String JFProjectPhase = (String) attributeMap.get("JFProjectPhase");
                JF_Util_mxJPO jfUtilMxJPO = new JF_Util_mxJPO();
                if("Urgent".equalsIgnoreCase(ecrType)&&!"phase1".equalsIgnoreCase(JFProjectPhase)){
                    String sProductionVault = PropertyUtil.getSchemaProperty(context, "vault_eServiceProduction");
                    String newObjId = FrameworkUtil.autoName(context, SYMBOLIC_TYPE_JFECO,"", SYMBOLIC_POLICY_JFECO, sProductionVault);
                    DomainObject ECOObject = DomainObject.newInstance(context);
                    ECOObject.setId(newObjId);
                    ECOObject.setDescription(context,description);
                    ECOObject.setAttributeValues(context, syncAttributeMap);
                    // 设置ECO的owner项目经理
                    ECOObject.setOwner(context,owner);
                    // 获取组织
                    String organization = jfUtilMxJPO.getPersonOrganization(context, owner);
                    // 获取项目
                    String project = jfUtilMxJPO.getObjectProject(context, objectId);
                    // 设置ECO的项目和组织
                    ECOObject.setPrimaryOwnership(context, project, organization);
                    // ECO与ECR关联关系
                    ECOObject.addRelatedObject(context, new RelationshipType(RELATIONSHIP_JFECR2CO), true, objectId);
                    // 创建成功后提升ECO状态
                    ECOObject.promote(context);
                    //创建成功发送邮件通知给owner
                    JF_NotificationUtils_mxJPO notification = new JF_NotificationUtils_mxJPO();
                    notification.sendECOEmail(context, new String[]{newObjId});

                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            ContextUtil.popContext(context);
        }
    }
    public void createECOObjectAfterJFECRComplete(Context context, String[] args) throws FrameworkException {
        String objectId = args[0];
        String owner = "";
        try {
            ContextUtil.pushContext(context);
            DomainObject ecrObj = new DomainObject(objectId);
            bosel.add(SELECT_OWNER);
            Map map = ecrObj.getRelatedObject(context, RELATIONSHIP_JFCHANGE2PROJECT, true, bosel, relsel);
            String description = ecrObj.getInfo(context, "description");
            if(map != null){
                owner = (String) map.get(SELECT_OWNER);
                String projectId = (String) map.get(SELECT_ID);
                owner =  JF_Util_mxJPO.getProjectManager(context,new String[]{projectId});
                // 获取ECR对象的属性
                Map attributeMap = ecrObj.getAttributeMap(context);
                // mod by chenyan 修改ECO同步ECR属性 只同步指定属性
                Map syncAttributeMap = getECRSyncAttributeList(attributeMap);
                String ecrType = (String) attributeMap.get("JFECRType");
                String JFProjectPhase = (String) attributeMap.get("JFProjectPhase");
                JF_Util_mxJPO jfUtilMxJPO = new JF_Util_mxJPO();
                if("Ordinary".equalsIgnoreCase(ecrType)&&!"phase1".equals(JFProjectPhase)){
                    String sProductionVault = PropertyUtil.getSchemaProperty(context, "vault_eServiceProduction");
                    String newObjId = FrameworkUtil.autoName(context, SYMBOLIC_TYPE_JFECO,"", SYMBOLIC_POLICY_JFECO, sProductionVault);
                    DomainObject ECOObject = DomainObject.newInstance(context);
                    ECOObject.setId(newObjId);
                    ECOObject.setDescription(context,description);
                    ECOObject.setAttributeValues(context, syncAttributeMap);
                    // 设置ECO的owner项目经理
                    ECOObject.setOwner(context,owner);
                    // 获取组织
                    String organization = jfUtilMxJPO.getPersonOrganization(context, owner);
                    // 获取项目
                    String project = jfUtilMxJPO.getObjectProject(context, objectId);
                    // 设置ECO的项目和组织
                    ECOObject.setPrimaryOwnership(context, project, organization);
                    // ECO与ECR关联关系
                    ECOObject.addRelatedObject(context, new RelationshipType(RELATIONSHIP_JFECR2CO), true, objectId);
                    // 创建成功后提升ECO状态
                    ECOObject.promote(context);
                    JF_NotificationUtils_mxJPO notification = new JF_NotificationUtils_mxJPO();
                    notification.sendECOEmail(context, new String[]{newObjId});

                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            ContextUtil.popContext(context);
        }
    }

    /**
     * @description 获取DA所关联的所有的物理对象
     * @param context
     * @param args
     * @author lsa
     */
    public MapList getAllVPMReference(Context context, String[] args){
        MapList allVPMReferenceMapList = getAllVPMReferenceMapList(context, args);
        return allVPMReferenceMapList;
    }

    /**
     * @description 获取零件清单集合
     * @param context
     * @param args
     * @author lsa
     */
    public MapList getAllVPMReferenceMapList(Context context, String[] args){
        MapList resMapList = new MapList();
        try {
            Map paramMap = JPO.unpackArgs(args);
            String objectId = (String) paramMap.get(STRING_OBJECTID);
            String ecrId = getECO2ECRObject(context, objectId);
            if(!"".equals(ecrId)){
                DomainObject ecrObj = new DomainObject(ecrId);
                //获取零件清单集合
                resMapList = ecrObj.getRelatedObjects(context, RELATIONSHIP_JFRELATEITEM, TYPE_VPMREFERENCE, bosel, relsel, false, true, (short) 0, "", "", 0);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return resMapList;
    }

    /**
     *
     *@description 获取图纸状态
     * @param context
     * @param args
     * @author lsa
     */
    public StringList getConnectionDrawingStatus(Context context ,String[] args) throws Exception{
        Map paramMap = (Map)JPO.unpackArgs(args);
        StringList tableIdList = _getListOfKeys(args,SELECT_ID);
        StringList res = new StringList();
        for (int i = 0; i < tableIdList.size(); i++) {
            String strID = tableIdList.get(i);
            DomainObject obj = DomainObject.newInstance(context, strID);
            StringList toCurrentList = obj.getInfoList(context, "to[XCADBaseDependency].from.current");
            if (!toCurrentList.isEmpty()){
                String strToCurrent = toCurrentList.get(0);
                String strNlsCurrent = EnoviaResourceBundle.getStateI18NString(context, "VPLM_SMB_Definition_MajorRev",strToCurrent, context.getLocale().toString());
                res.add(strNlsCurrent);
            }else {
                res.add("");
            }
        }
        return res;
    }


    /**
     * @description 通过ecoId获取ECR的id
     * @author lsa
     */
    public String getECO2ECRObject(Context context, String objectId) throws Exception {
        String resultId = "";
        DomainObject ecoObj = DomainObject.newInstance(context, objectId);
        String ecrId = ecoObj.getInfo(context, "to[JFECR2CO].from.id");
        if(null != ecrId){
            resultId = ecrId;
        }
        return resultId;
    }

    /**
     *
     *@description 获取Table中request 中行中1 指定key value
     * @param var0
     * @param strKey
     * @author lsa
     */
    public static StringList _getListOfKeys(String[] var0,String strKey) throws Exception {
        StringList var1 = new StringList();
        Map var2 = (Map)JPO.unpackArgs(var0);
        MapList var3 = (MapList)var2.get(STRING_OBJECTLIST);
        if (var3 != null) {
            int var4 = 0;

            for(int var5 = var3.size(); var4 < var5; ++var4) {
                Map var6 = (Map)var3.get(var4);
                if (null != var6 && var6.containsKey(strKey)) {
                    String var7 = (String)var6.get(strKey);
                    var1.add(var7);
                }
            }
        }
        return var1;
    }

    /**
     * @description 获取ECO执行计划模板库
     * @param context
     * @param args
     * @author lsa
     */
    public StringList getAllGeneralClass(Context context, String[] args){
        StringList resList = new StringList();
        String basicUrl = JF_PublicMethodClass_mxJPO.getBasicUrl(context, new String[]{LIBRARY_KEY});
        try {
            String projectSpaceId = MqlUtil.mqlCommand(context, "temp query bus * " + basicUrl + " * select id dump |");
            DomainObject object = DomainObject.newInstance(context, projectSpaceId.split("\\|")[3]);
            // 获取到所有的ECO执行计划模板库下面所有的分类对象，叶子节点
//            MapList mapList = object.getRelatedObjects(context, RELATIONSHIP_SUBCLASS, TYPE_GENERALCLASS, bosel, relsel, false, true, (short) 0, "", "", (short) 0,false,true,(short)0,null,null,null,"end");
            bosel.add("from[Subclass]");
            MapList mapList = object.getRelatedObjects(context, RELATIONSHIP_SUBCLASS, TYPE_GENERALCLASS, bosel, relsel, false, true, (short) 0, "", "", (short) 0);
            logger.info("mapList.size{}",mapList.size());
            for (int i = 0; i < mapList.size(); i++) {
                Map map = (Map) mapList.get(i);
                String hasSubclass = (String)map.get("from[Subclass]");
                if("FALSE".equalsIgnoreCase(hasSubclass)) {
                    String id = (String) map.get(SELECT_ID);
                    resList.add(id);
                }
            }
        } catch (FrameworkException e) {
            throw new RuntimeException(e);
        }
        return  resList;
    }


    /**
     * @description 创建ECOTask任务
     * @param context
     * @param args
     * @author lsa
     */
    public void createECOTaskObject(Context context, String[] args) throws FrameworkException {
        try {
            ArrayList argMaps = JPO.unpackArgs(args);
            // 排除重复的命名
            ArrayList<String> uniqueName = new ArrayList<>();
            // 获取ECO的id
            String ECOId = (String) argMaps.get(argMaps.size()-1);
            DomainObject ecoObj = new DomainObject(ECOId);
            //获取项目ID
            StringList ecrList = ecoObj.getInfoList(context, "to[JFECR2CO].from.id");
            String projectId = "";
            if(ecrList.size()>0){
                String ecrId = ecrList.get(0);
                DomainObject ecrObj = DomainObject.newInstance(context,ecrId);
               StringList ProjectList = ecrObj.getInfoList(context, "from[JFChange2Project].to.id");
               if(ProjectList.size()>0) {
                   projectId =ProjectList.get(0);
               }
            }
            MapList mapList = ecoObj.getRelatedObjects(context, RELATIONSHIP_JFCO2ECOTASK, TYPE_JF_ECOTASK, bosel, relsel, false, true, (short) 1, "", "", 0);
            if(!mapList.isEmpty()){
                for (int i = 0; i < mapList.size(); i++) {
                    Map map = (Map) mapList.get(i);
                    String ecoTaskId = (String) map.get(SELECT_ID);
                    DomainObject ecoTaskObj = DomainObject.newInstance(context, ecoTaskId);
                    String value = ecoTaskObj.getAttributeValue(context, "Title");
                    uniqueName.add(value);
                }
            }

            for (int i = 0; i < argMaps.size()-1; i++) {
                String id = (String) argMaps.get(i);
                DomainObject object = new DomainObject(id);
                String ecoTaskName = object.getInfo(context, "attribute[Title]");
                String projectRole = object.getInfo(context, "attribute[Project Role]");
                String  taskowner= context.getUser();
                boolean flag = true;
                logger.info("projectId:{},projectRole:{}",projectId,projectRole);
                if(UIUtil.isNotNullAndNotEmpty(projectId)&&UIUtil.isNotNullAndNotEmpty(projectRole)){
                    String stdname = JF_Util_mxJPO.getProjectRoleName(context, new String[]{projectId,projectRole});
                    if(UIUtil.isNotNullAndNotEmpty(stdname)){
                        taskowner = stdname;
                        flag = false;
                    }
                }
                // 如果已经创建了同名的ECOTask,就过滤掉不再新建同名任务
                if(!uniqueName.contains(ecoTaskName)){
                    Map map = new HashMap();
                    map.put("taskName",ecoTaskName);//任务标题
                    map.put("taskType",SYMBOLIC_TYPE_JF_ECOTASK);//任务类型 注册名
                    map.put("taskOwner",taskowner);  //ECO的owner
                    map.put("parentId",ECOId);
                    if(flag) {
                        map.put("assign", "false");
                    }
                    //创建JFDATask任务实体
                    JF_Util_mxJPO jfUtilMxJPO = new JF_Util_mxJPO();
                    String taskId = jfUtilMxJPO.createTask(context, JPO.packArgs(map));
                    DomainObject taskObj = new DomainObject(taskId);
                    taskObj.setAttributeValue(context, "Project Role", projectRole);
                    // ECO与创建的ECOTask连接关系
                    DomainRelationship.connect(context, ecoObj,RELATIONSHIP_JFCO2ECOTASK,taskObj);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * Create ECO execution plan delay filing after confirming that all previous delay filings are complete.
     **
     * @param context
     * @param args
     * @throws Exception
     * @author caipan
     * @date 2026/6/9 17:35
     */
    public void createECOExecutionPlanDelayFiling(Context context, String[] args) throws Exception {
        Map paramsMap = (Map) JPO.unpackArgs(args);
        Map requestMap = (Map) paramsMap.get(STRING_REQUESTMAP);
        Map paramMap = (Map) paramsMap.get(STRING_PARAMMAP);
        logger.info("paramsMap:{}",paramsMap.toString());
        logger.info("requestMap:{}",requestMap.toString());
        logger.info("paramMap:{}",paramMap.toString());
        String filingId = (String) paramMap.get(STRING_OBJECTID);
        if (UIUtil.isNullOrEmpty(filingId)) {
            filingId = (String) paramMap.get("newObjectId");
        }
        if (UIUtil.isNullOrEmpty(filingId)) {
            filingId = (String) requestMap.get(STRING_OBJECTID);
        }
        String parentId = (String)requestMap.get("parentId");
        String taskId =parentId;
        if (UIUtil.isNullOrEmpty(taskId)) {
            String message = EnoviaResourceBundle.getProperty(context, SUITE_KEY, context.getLocale(), "emxFramework.Notice.SelectOneECOTaskForDelayRequest");
//            emxContextUtil_mxJPO.mqlNotice(context, message);
            throw  new Exception(message);
        }
        //延期对象
        DomainObject filingObj = DomainObject.newInstance(context, filingId);
        //任务对象
        DomainObject taskObj = DomainObject.newInstance(context, taskId);
        //20260828 update by caipan 上一次延期流程完成后允许再次发起，存在未完成流程时禁止创建
        StringList delayFilingStates = taskObj.getInfoList(context,
                "to[" + RELATIONSHIP_JFDELAYEDFILING2TASK + "].from.current");
        for (String delayFilingState : delayFilingStates) {
            if (!"Complete".equalsIgnoreCase(delayFilingState)) {
                throw new Exception(EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource",
                        context.getLocale(), "emxComponents.DelayRequest.repetition"));
            }
        }
        String type = taskObj.getInfo(context, SELECT_TYPE);
        String delayDate = filingObj.getAttributeValue(context, ATTR_JFDELAYTHEPLANNEDDATE);//延迟时间
        //20260820 update by caipan 创建延期申请时统一校验ECO、DA执行任务的计划完成日期
        validateDelayPlannedDate(context, taskObj, delayDate);
        //20260828 update by caipan 保存创建延期申请时执行任务的原计划完成时间
        String originalPlannedDate = taskObj.getAttributeValue(context, ATTR_Task_Estimated_Finish_Date);
        filingObj.setAttributeValue(context, ATTR_JFORIGINALPLANNEDDATE, originalPlannedDate);
        if("JF_ECOTask".equalsIgnoreCase(type)) {//ECO任务
            taskObj.setAttributeValue(context, ATTR_WHETHERTOPOSTPONE, "Process");//设置延期
        }else if("JF_DATask".equalsIgnoreCase(type)) {//DA任务
            taskObj.setAttributeValue(context, ATTR_DAWHETHERTOPOSTPONE, "Process");//设置延期
        }
        DomainRelationship.connect(context, filingObj, RELATIONSHIP_JFDELAYEDFILING2TASK, taskObj);
        filingObj.promote(context);//自动提升
    }

    /**
     * 获取延期请求创建页的原计划完成时间
     **
     * @param context Matrix上下文
     * @param args 表单参数
     * @return String 所选执行任务的计划完成时间，未选择任务时返回空字符串
     * @throws Exception 读取执行任务失败时抛出异常
     * @author caipan by codex
     * @date 2026/8/28 10:12
     */
    public String getOriginalPlannedDate(Context context, String[] args) throws Exception {
        Map programMap = JPO.unpackArgs(args);
        Map requestMap = (Map) programMap.get(STRING_REQUESTMAP);
        if (requestMap == null) {
            return EMPTY_STRING;
        }
        String taskId = UIUtil.getValue(requestMap, "parentId");
        if (UIUtil.isNullOrEmpty(taskId)) {
            return EMPTY_STRING;
        }
        return DomainObject.newInstance(context, taskId)
                .getAttributeValue(context, ATTR_Task_Estimated_Finish_Date);
    }

    /**
     * 三个时间比较大小,T1小于等于T2 T3的最大时间
     **
     * @param t1
     * @param t2
     * @param t3
     * @return
     * @author caipan
     * @date 2026/6/11 15:01
     */
    private static boolean checkTime(LocalDateTime t1, LocalDateTime t2, LocalDateTime t3) {
        // t1、t2为空直接不通过
        if (t1 == null || t2 == null) {
            return false;
        }
        LocalDateTime maxTime;
        if (t3 == null) {
            maxTime = t2;
        } else {
            maxTime = t2.isAfter(t3) ? t2 : t3;
        }
        // t1小于等于最大值
        return t1.isBefore(maxTime) || t1.isEqual(maxTime);
    }

    /**
     * 统一校验ECO、DA执行任务的延期计划日期
     **
     * @param context Matrix上下文
     * @param taskObj 当前执行任务
     * @param delayDate 延迟计划日期
     * @return String 标准化后的达索日期
     * @throws Exception 日期缺失、格式错误或不满足延期范围时抛出国际化提示
     * @author caipan by codex
     * @date 2026/8/20 17:30
     */
    private String validateDelayPlannedDate(Context context, DomainObject taskObj, String delayDate) throws Exception {
        String taskDateMessage = EnoviaResourceBundle.getProperty(context, SUITE_KEY,
                context.getLocale(), "emxFramework.Notice.delayDateBeforeTaskDate");
        String estimatedFinishDate = taskObj.getAttributeValue(context, ATTR_Task_Estimated_Finish_Date);
        String matrixDelayDate;
        String matrixEstimatedFinishDate;
        try {
            matrixDelayDate = JF_Util_mxJPO.convertDateToMatrixFormat(delayDate);
            matrixEstimatedFinishDate = JF_Util_mxJPO.convertDateToMatrixFormat(estimatedFinishDate);
            if (UIUtil.isNullOrEmpty(matrixDelayDate) || UIUtil.isNullOrEmpty(matrixEstimatedFinishDate)) {
                throw new Exception(taskDateMessage);
            }
        } catch (Exception e) {
            throw new Exception(taskDateMessage, e);
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("M/d/uuuu h:mm:ss a", Locale.US);
        LocalDateTime delayDateTime;
        LocalDateTime estimatedFinishDateTime;
        try {
            delayDateTime = LocalDateTime.parse(matrixDelayDate, formatter);
            estimatedFinishDateTime = LocalDateTime.parse(matrixEstimatedFinishDate, formatter);
        } catch (Exception e) {
            throw new Exception(taskDateMessage, e);
        }
        if (!delayDateTime.isAfter(estimatedFinishDateTime)) {
            throw new Exception(taskDateMessage);
        }

        String taskType = taskObj.getInfo(context, SELECT_TYPE);
        if ("JF_DATask".equalsIgnoreCase(taskType)) {
            String closeDateMessage = EnoviaResourceBundle.getProperty(context, SUITE_KEY,
                    context.getLocale(), "emxFramework.Notice.delayDateBeforeCloseDate");
            String closeDate = taskObj.getInfo(context,
                    "to[JFDA2JFDATask].from.attribute[JFDACloseTime]");
            String extensionDate = taskObj.getInfo(context,
                    "to[JFDA2JFDATask].from.attribute[JFDAExtensionTime]");
            try {
                String matrixCloseDate = JF_Util_mxJPO.convertDateToMatrixFormat(closeDate);
                String matrixExtensionDate = JF_Util_mxJPO.convertDateToMatrixFormat(extensionDate);
                LocalDateTime closeDateTime = LocalDateTime.parse(matrixCloseDate, formatter);
                LocalDateTime extensionDateTime = UIUtil.isNullOrEmpty(matrixExtensionDate) ? null
                        : LocalDateTime.parse(matrixExtensionDate, formatter);
                if (!checkTime(delayDateTime, closeDateTime, extensionDateTime)) {
                    throw new Exception(closeDateMessage);
                }
            } catch (Exception e) {
                if (closeDateMessage.equals(e.getMessage())) {
                    throw e;
                }
                throw new Exception(closeDateMessage, e);
            }
        }
        return matrixDelayDate;
    }

    /**
     * 创建延期申请流程
     **
     * @param context
     * @param args
     * @throws Exception
     * @author caipan
     * @date 2026/6/10 13:53
     */
    public void createDelayedFilingRoute(Context context, String[] args) throws Exception {
        String filingId = args[0];
        DomainObject filingObj = DomainObject.newInstance(context, filingId);
        String taskId = filingObj.getInfo(context, "from[" + RELATIONSHIP_JFDELAYEDFILING2TASK + "].to.id");
        if (UIUtil.isNullOrEmpty(taskId)) {
            return;
        }
        DomainObject taskObj = DomainObject.newInstance(context, taskId);
        String type = taskObj.getInfo(context, SELECT_TYPE);
        String projectId = "";
        if("JF_ECOTask".equalsIgnoreCase(type)) {
            projectId = taskObj.getInfo(context, "to[" + RELATIONSHIP_JFCO2ECOTASK + "].from.to[" + RELATIONSHIP_JFECR2CO + "].from.from[" + RELATIONSHIP_JFCHANGE2PROJECT + "].to.id");
        }else if("JF_DATask".equalsIgnoreCase(type)) {
            projectId=taskObj.getInfo(context,"to[JFDA2JFDATask].from.from[JFChange2Project].to.id");
        }
        logger.info("projectId:{}",projectId);
        JF_PublicMethodClass_mxJPO publicMethod = new JF_PublicMethodClass_mxJPO();
        String lineManagerId = publicMethod.getPersonLineManager(context, null, filingObj.getOwner(context).getName());
        String projectManager = JF_Util_mxJPO.getProjectManager(context, new String[]{projectId});
        projectManager = PersonUtil.getPersonObjectID(context, projectManager);
        if(UIUtil.isNullOrEmpty(lineManagerId)) {
            String message = EnoviaResourceBundle.getProperty(context, SUITE_KEY, context.getLocale(), "emxFramework.Notice.DelayRequestApproverRequired");
            emxContextUtil_mxJPO.mqlNotice(context, message);
            throw new Exception(message);
        }
        if(UIUtil.isNullOrEmpty(projectManager)) {
            String message = EnoviaResourceBundle.getProperty(context, SUITE_KEY, context.getLocale(), "emxFramework.Notice.DelayRequestProjectManagerRequired");
            emxContextUtil_mxJPO.mqlNotice(context, message);
            throw new Exception(message);
        }
        ArrayList<String> approverIds = new ArrayList<>();
        MapList approveList = new MapList();

       String JFECOExecutionPlanDelayRequest = EnoviaResourceBundle.getProperty(context, SUITE_KEY, context.getLocale(), "emxFramework.Command.JFECOExecutionPlanDelayRequest");
       String LineManageTitle = JFECOExecutionPlanDelayRequest+":"+EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", context.getLocale(), "emxComponents.DARouteInfo.TitleMessage.LineManage");
       String projectManagerTitle = JFECOExecutionPlanDelayRequest+":"+EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", context.getLocale(), "emxComponents.DARouteInfo.TitleMessage.ProjectManage");
       Map receiverMap = (Map) JF_PublicMethodClass_mxJPO.getMapAnyOrAll(lineManagerId, LineManageTitle, "true", "1", "All");
       approveList.add(receiverMap);
       if(!lineManagerId.equalsIgnoreCase(projectManager)) {
                Map receiverMap2 = (Map) JF_PublicMethodClass_mxJPO.getMapAnyOrAll(projectManager, projectManagerTitle, "true", "2", "All");
                approveList.add(receiverMap2);
            }
        JF_Route_mxJPO routeJPO = new JF_Route_mxJPO(context, args);
        String routeId = routeJPO.createAndStartRoute(context, approveList, filingId, STATE_JFDELAYEDFILING_REVIEW, SYMBOLIC_POLICY_JFDELAYEDFILING, DELAY_FILING_ROUTE_TITLE);
        logger.info("DelayFilingRouteId:{}", routeId);
        //20260819 update by codex 根据执行任务类型写入对应的是否延期属性，避免ECO任务误用DA属性
        if ("JF_ECOTask".equalsIgnoreCase(type)) {
            taskObj.setAttributeValue(context, ATTR_WHETHERTOPOSTPONE, "Process");//设置延期
        } else if ("JF_DATask".equalsIgnoreCase(type)) {
            taskObj.setAttributeValue(context, ATTR_DAWHETHERTOPOSTPONE, "Process");//设置延期
        }
    }

    /**
     * @description 获取所有自己创建的执行计划
     * @param context
     * @param args
     * @author lsa
     */
    public MapList getAllExecutionPlanTask(Context context, String[] args){
        MapList mapList = new MapList();
        try {
            Map argsMap = (Map) JPO.unpackArgs(args);
            String objectId = (String) argsMap.get("objectId");
            logger.info("objectId:{}",objectId);
            DomainObject daObj = DomainObject.newInstance(context, objectId);
            relsel.add(DomainRelationship.SELECT_ID);
            bosel.add("to[Assigned Tasks].from.name");
            mapList = daObj.getRelatedObjects(context, RELATIONSHIP_JFCO2ECOTASK, TYPE_JF_ECOTASK, bosel, relsel, false, true, (short) 0, "", "", 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return mapList;
    }

    /**
     * @description 生成ECO执行计划表单中最后一栏按钮
     * @param context
     * @param args
     * @author lsa
     */
    public StringList confirmTaskContent(Context context, String[] args){
        StringList resList = new StringList();
        String submit = EnoviaResourceBundle.getProperty(context, SUITE_KEY, context.getLocale(), "emxFramework.ECO.submitButton");
        String Submitted = EnoviaResourceBundle.getProperty(context, SUITE_KEY, context.getLocale(), "emxFramework.ECO.submittedButton");
        try {
            HashMap argMaps = JPO.unpackArgs(args);
            HashMap paramList = (HashMap) argMaps.get(STRING_PARAMLIST);
            MapList objectList = (MapList) argMaps.get(STRING_OBJECTLIST);
            String objectId = (String) paramList.get(STRING_OBJECTID);
            StringBuffer buffer = new StringBuffer();
            for (int i = 0; i < objectList.size(); i++) {
                Map map = (Map) objectList.get(i);
                String id = (String) map.get(DomainConstants.SELECT_ID);
                String parentId = (String) map.get("id[parent]");
                String ecrId = MqlUtil.mqlCommand(context, "print bus " + parentId + " select to[JFECR2CO].from.id dump |");
                String owner = MqlUtil.mqlCommand(context, "print bus "+ ecrId +" select from[JFChange2Project].to.owner dump |");
                DomainObject object = DomainObject.newInstance(context, id);
                State currentState = object.getCurrentState(context);
                Map responseMap = object.getRelatedObject(context, "Assigned Tasks", false, bosel, relsel);
                if(null != responseMap){
                    String taskResponseName = (String) responseMap.get(DomainConstants.SELECT_NAME);
                    String contextUser = context.getUser();
                    int index = findIndex(stateIndex, currentState.getName());
                    if(contextUser.equals(taskResponseName) && index == 2){
                        buffer.append("<table><td><a target='listHidden' href=\"/3dspace/common/JF_TasKJudgeValAndPromote.jsp?objectId=");
                        buffer.append(id).append("\" >");
                        buffer.append(submit);
                        buffer.append("</a></td></table>");
//                    }else if((contextUser.equals(taskResponseName) && index > 2) || (contextUser.equals(owner) && index > 2)){
                    }else if(index > 2){
                        buffer.append("<table><td>");
                        buffer.append(Submitted);
                        buffer.append("</td></table>");
                    }else {
                        buffer.append("");
                    }
                }else {
                    buffer.append("");
                }
                resList.add(buffer.toString());
                buffer.setLength(0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return resList;
    }

    /**
     * @description 获取执行计划任务的附件信息
     * @param context
     * @param args
     * @author lsa
     */
    public StringList getAttachmentContent(Context context, String[] args){
        StringList resultList = new StringList();
        try {
            Map argMaps = JPO.unpackArgs(args);
            MapList argMap = (MapList) argMaps.get(STRING_OBJECTLIST);
            Iterator iterator = argMap.iterator();
            while (iterator.hasNext()){
                Map map = (Map) iterator.next();
                String objectId = (String) map.get("id");
                StringBuffer buffer = new StringBuffer();
                DomainObject object = DomainObject.newInstance(context, objectId);
                bosel.add(SELECT_ATTRIBUTE_TITLE);
                MapList mapList = object.getRelatedObjects(context, "Reference Document", "Document", bosel, null, false, true, (short) 0, "", "", 0);
                if(mapList.size()>0){
                    for (int i = 0; i < mapList.size(); i++) {
                        buffer.append("<table><td><a href=\"/3dspace/common/emxNavigator.jsp?objectId=");
                        Map projectMap = (Map) mapList.get(i);
                        String folderId = (String) projectMap.get("id");
                        buffer.append(folderId).append("\" target=\"_blank>\">");
                        String folderName = (String) projectMap.get(SELECT_ATTRIBUTE_TITLE);
                        folderName = StringEscapeUtils.escapeHtml4(folderName);
                        folderName =folderName.replace("&", "");
                        buffer.append(folderName);
                        buffer.append("</a></td></table>");
                    }
                }
                resultList.add(buffer.toString());
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return resultList;
    }


    /**
     * @description ECO工作中提升到执行反馈校验ECO关联零件下图纸是否发布
     * @param context
     * @param args
     * @author lsa
     */
    public int promoteECOTaskState(Context context, String[] args){
        String ECOId = args[0];
        String ecrId = "";
        String ecrName = "";
        StringBuffer buffer = new StringBuffer();
        //图纸不是发布状态的集合
        StringList drawNameList = new StringList();
        String drawingIsReleased = EnoviaResourceBundle.getProperty(context, SUITE_KEY, context.getLocale(), "emxFramework.Notice.DrawingIsReleased");
        String judgeResponseAndDate = EnoviaResourceBundle.getProperty(context, SUITE_KEY, context.getLocale(), "emxFramework.Notice.JudgeResponseAndDate");
        String ecr2ECORelation = EnoviaResourceBundle.getProperty(context, SUITE_KEY, context.getLocale(), "emxFramework.Notice.ECR2ECORelation");
        try {
            //责任人和计划完成时间已经维护完成才能提升状态
            int resNum = judgeResponseAndCompleteTime(context, ECOId);
            if(1 == resNum){
                emxContextUtil_mxJPO.mqlNotice(context, judgeResponseAndDate);
                return resNum;
            }
            if (isBreakpointPlanTaskRequired(context, ECOId) && !hasBreakpointPlanTask(context, ECOId)) {
                String breakpointTaskRequired = EnoviaResourceBundle.getProperty(context, SUITE_KEY, context.getLocale(), "emxFramework.Notice.ECOBreakpointPlanTaskRequired");
                emxContextUtil_mxJPO.mqlNotice(context, breakpointTaskRequired);
                return 1;
            }
            //取消校验数模关联的图纸是否发布的校验
           /* DomainObject ecoObj = new DomainObject(ECOId);
            Map map = ecoObj.getRelatedObject(context, RELATIONSHIP_JFECR2CO, false, bosel, relsel);
            if(null != map){
                ecrId = (String) map.get(SELECT_ID);
                ecrName = (String) map.get(SELECT_NAME);
            }else {
                emxContextUtil_mxJPO.mqlNotice(context, ecr2ECORelation);
                return 1;
            }
            DomainObject ecrObj = DomainObject.newInstance(context, ecrId);
            // 获取ecr下面的零件清单所有的对象
            MapList mapList = ecrObj.getRelatedObjects(context, RELATIONSHIP_JFRELATEITEM, TYPE_VPMREFERENCE, bosel, null, false, true, (short) 0, "", "", 0);
            if(!mapList.isEmpty()){
                for (int i = 0; i < mapList.size(); i++) {
                    Map ecrMap = (Map) mapList.get(i);
                    DomainObject object = DomainObject.newInstance(context, (String) ecrMap.get(SELECT_ID));
                    //获取零件清单下面的图纸对象
                    MapList drawingMaps = object.getRelatedObjects(
                            context,
                            RELATIONSHIP_XCADBASEDEPENDENCY,
                            TYPE_DRAWING,
                            bosel,
                            null,
                            true,
                            false,
                            (short) 1,
                            "",
                            "",
                            0);
                    if (!drawingMaps.isEmpty()){
                        for (int j = 0; j < drawingMaps.size(); j++) {
                            Map drawingMap = (Map) drawingMaps.get(j);
                            String current = (String) drawingMap.get(SELECT_CURRENT);
                            String strName = (String) drawingMap.get(SELECT_NAME);
                            logger.info("current:{}",current);
                            //如果图纸不是发布状态阻止提升
                            if(!STATE_RELEASED.equals(current)){
                                drawNameList.add(strName);
                            }
                        }
                    }
                }
            }
            //  零件清单的物理产品下的图纸需全部发才能提升状态
            if(drawNameList.size() > 0){
                emxContextUtil_mxJPO.mqlNotice(context, drawingIsReleased.replace("{}",drawNameList.join(",")));
                return 1;
            }*/
            // 所有条件都符合提升eco任务的状态
            promoteAllEcoTaskState(context,ECOId);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return 0;
    }


    /**
     * @description ECO从执行反馈提升到完成提升eco下所有的task的状态
     * @param context
     * @param args
     * @author lsa
     */
    public void promoteExecuteECOTaskState(Context context, String[] args) throws FrameworkException {
        String ECOId = args[0];
        promoteAllEcoTaskState(context,ECOId);
    }

    /**
     * @description 判断责任人和计划完成时间是否已经维护
     * @param context
     * @param ECOId
     * @author lsa
     */
    public int judgeResponseAndCompleteTime(Context context, String ECOId){
        try {
            DomainObject ecoObj = new DomainObject(ECOId);
            bosel.add("attribute[Task Estimated Finish Date]");
            bosel.add("to[Assigned Tasks].from.id");
            //ECO下所有的Task
            MapList mapList = ecoObj.getRelatedObjects(context, RELATIONSHIP_JFCO2ECOTASK, TYPE_JF_ECOTASK, bosel, null, false, true, (short) 0, "", "", 0);
            if(!mapList.isEmpty()){
                for (int i = 0; i < mapList.size(); i++) {
                    Map ecoTaskMap = (Map) mapList.get(i);
                    String taskFinishDate = (String) ecoTaskMap.get("attribute[Task Estimated Finish Date]");
                    Object response = ecoTaskMap.get("to[Assigned Tasks].from.id");
                    String responseId ="";
                    if(response instanceof StringList ){
                        responseId = "have";
                    }else{
                        responseId = (String)response;
                    }
                    if( "".equals(taskFinishDate) || "".equals(responseId)){
                        return  1;
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return 0;
    }

    /**
     * @description 提升eco下面的所有的task的状态
     * @param context
     * @param ECOId
     * @author lsa
     */
    public void promoteAllEcoTaskState(Context context, String ECOId) throws FrameworkException {
        Boolean isPop = false;
        try {
            DomainObject ecoObj = new DomainObject(ECOId);
            String current = ecoObj.getInfo(context, SELECT_CURRENT);
            MapList mapList = ecoObj.getRelatedObjects(context, RELATIONSHIP_JFCO2ECOTASK, TYPE_JF_ECOTASK, bosel, null, false, true, (short) 0, "", "", 0);
            if(!mapList.isEmpty()){
                ContextUtil.pushContext(context);
                for (int i = 0; i < mapList.size(); i++) {
                    Map map = (Map) mapList.get(i);
                    String id = (String) map.get(SELECT_ID);
                    DomainObject ecoTask = DomainObject.newInstance(context, id);
                    if("In_Work".equalsIgnoreCase(current)){
                        MqlUtil.mqlCommand(context, true, "mod bus "+id+" current Active", false);//去掉触发发邮件
                        JF_SignTask_mxJPO.projectTaskToActiveSendEmail(context, new String[]{id});
                        //
                    }else {
                        ecoTask.promote(context);
                        JPO.invoke(context, "JF_ChangeExecutionECOSource", new String[0], "setJF_TaskFileToPromote", new String[]{id,"RELEASED"}, void.class);
                    }
                }
                isPop = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            if(isPop){
                ContextUtil.popContext(context);
            }
        }
    }

    /**
     * @description 只有项目项目经理才能维护计划完成时间和责任人
     * @param context
     * @param args
     * @author lsa
     */
    // Set ECO breakpoint time from the breakpoint plan task estimated finish date.
    public void setECOBreakpointTimeFromPlanTask(Context context, String[] args) throws FrameworkException {
        try {
            String ECOId = args[0];
            if (!isBreakpointPlanTaskRequired(context, ECOId)) {
                return;
            }

            String breakpointTime = getBreakpointPlanTaskFinishDate(context, ECOId);
            if (UIUtil.isNotNullAndNotEmpty(breakpointTime)) {
                DomainObject ecoObj = DomainObject.newInstance(context, ECOId);
                ecoObj.setAttributeValue(context, ATTR_JFBREAKPOINTTIME, breakpointTime);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private boolean isBreakpointPlanTaskRequired(Context context, String ECOId) throws Exception {
        DomainObject ecoObj = DomainObject.newInstance(context, ECOId);
        String breakpointMode = ecoObj.getAttributeValue(context, ATTR_JFBREAKPOINTMODE);
        return UIUtil.isNotNullAndNotEmpty(breakpointMode) && !BREAKPOINT_MODE_NA.equalsIgnoreCase(breakpointMode);
    }

    private boolean hasBreakpointPlanTask(Context context, String ECOId) throws FrameworkException {
        return UIUtil.isNotNullAndNotEmpty(getBreakpointPlanTaskFinishDate(context, ECOId));
    }

    private String getBreakpointPlanTaskFinishDate(Context context, String ECOId) throws FrameworkException {
        DomainObject ecoObj = DomainObject.newInstance(context, ECOId);
        StringList taskSelects = new StringList();
        taskSelects.add(SELECT_ATTRIBUTE_TITLE);
        taskSelects.add("attribute[Task Estimated Finish Date]");
        MapList taskList = ecoObj.getRelatedObjects(context, RELATIONSHIP_JFCO2ECOTASK, TYPE_JF_ECOTASK, taskSelects, null, false, true, (short) 1, "", "", 0);
        for (int i = 0; i < taskList.size(); i++) {
            Map taskMap = (Map) taskList.get(i);
            String taskTitle = (String) taskMap.get(SELECT_ATTRIBUTE_TITLE);
            if (isBreakpointPlanTaskTitle(taskTitle)) {
                return (String) taskMap.get("attribute[Task Estimated Finish Date]");
            }
        }
        return "";
    }

    private boolean isBreakpointPlanTaskTitle(String taskTitle) {
        if (UIUtil.isNullOrEmpty(taskTitle)) {
            return false;
        }
        String normalizedTitle = taskTitle.trim();
        return BREAKPOINT_PLAN_TASK_TITLE_CN_EN.equalsIgnoreCase(normalizedTitle)
                || BREAKPOINT_PLAN_TASK_TITLE_CN.equalsIgnoreCase(normalizedTitle)
                || BREAKPOINT_PLAN_TASK_TITLE_EN.equalsIgnoreCase(normalizedTitle);
    }

    // Only the project manager can maintain the responsible person and planned completion time.
    public StringList whetherEditByResponse(Context context, String[] args){
        StringList result = new StringList();
        try {
            Map argsMaps = JPO.unpackArgs(args);
            MapList argMap = (MapList) argsMaps.get(STRING_OBJECTLIST);
            for (int i = 0; i < argMap.size(); i++) {
                Map map = (Map) argMap.get(i);
                String id = (String) map.get("id[parent]");
                String objectId = (String) map.get("id");
                DomainObject ecoObj = new DomainObject(id);
                State currentState = ecoObj.getCurrentState(context);
                String ecrId = getECO2ECRObject(context, id);
                DomainObject ecrObj = new DomainObject(ecrId);
                DomainObject taskObj = DomainObject.newInstance(context, objectId);
                String taskCurrent = taskObj.getInfo(context, SELECT_CURRENT);
                Map projectMap = ecrObj.getRelatedObject(context, RELATIONSHIP_JFCHANGE2PROJECT, true, bosel, relsel);
                if(null != projectMap){
                    String projectId = (String) projectMap.get(DomainConstants.SELECT_ID);
                    DomainObject projectObj = DomainObject.newInstance(context, projectId);
//                    User user = projectObj.getOwner(context);
                    String projectManagerName =  JF_Util_mxJPO.getProjectManager(context,new String[]{projectId});
                    String contextUser = context.getUser();
                    // ECO状态工作中、执行反馈并且当前登录用户是项目经理给予编辑权限,执行任务只有工作中才可以
                    logger.info("projectManagerName:{} contextUser:{} currentState:{}",projectManagerName,contextUser,currentState.getName());
                    String stateStr = "Assign,Active";
                    result.add((stateStr.contains(taskCurrent)&&contextUser.equals(projectManagerName)&& ("In_Work".equals(currentState.getName())||"ExecuteFeedback".equals(currentState.getName())))?"true":"false");
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return result;
    }

    /**
     * @description ECO执行任务需要项目经理提升状态校验检查任务是否完成
     * @param context
     * @param args
     * @author lsa
     */
    public int promoteProjectManagerCheck(Context context, String[] args){
        try {
            String objectId = args[0];
            DomainObject object = new DomainObject(objectId);
            String whetherHasTask = EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", context.getLocale(), "emxComponents.Notice.whetherHasTask");
            String whetherSubTask = EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", context.getLocale(), "emxComponents.Notice.whetherSubTask");
            MapList mapList = object.getRelatedObjects(context, RELATIONSHIP_JFCO2ECOTASK, TYPE_JF_ECOTASK, bosel, relsel, false, true, (short) 0, "", "", 0);

            if(mapList.isEmpty()){
                emxContextUtil_mxJPO.mqlNotice(context, whetherHasTask);
                return 1;
            }

            int num = 0;
            for (int i = 0; i < mapList.size(); i++) {
                Map taskMap = (Map) mapList.get(i);
                String current = (String) taskMap.get("current");
                if("Review".equals(current)) num++;
            }
            // 如果任务没完成则不能提升
            if(num != mapList.size()) {
                emxContextUtil_mxJPO.mqlNotice(context, whetherSubTask);
                return 1;
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return 0;
    }




    /**
     * 新建执行计划项目经理能创建
     * @param context
     * @param args
     * @author lsa
     * @throws
     * @return void
     */
    public Boolean isProjectManager(Context context, String[] args) throws Exception {
        Boolean flag = false;
        try {
            Map argsMaps = JPO.unpackArgs(args);
            String ecoObjectId = (String) argsMaps.get(STRING_OBJECTID);
            DomainObject ecoObj = new DomainObject(ecoObjectId);
            State currentState = ecoObj.getCurrentState(context);
            String ecrObjectId = getECO2ECRObject(context, ecoObjectId);
            if(!"".equals(ecrObjectId)) {
                DomainObject ecrObj = new DomainObject(ecrObjectId);
                Map projectMap = ecrObj.getRelatedObject(context, RELATIONSHIP_JFCHANGE2PROJECT, true, bosel, relsel);
                if (null != projectMap) {
                    String projectId = (String) projectMap.get(DomainConstants.SELECT_ID);
                    BusinessObject businessObject = new BusinessObject(projectId);
                    if(businessObject.exists(context)){
                        // 获取项目经理id
                        String projectManagerName =  JF_Util_mxJPO.getProjectManager(context,new String[]{projectId});
                    // 获取当前登录用户
                    String contextUser = context.getUser();
                    //只有状态在执行计划并且项目经理能创建删除任务
                    if (contextUser.equals(projectManagerName) && "In_Work".equals(currentState.getName())) {
                        flag = true;
                    }
                }
            }
            }

        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
        return flag;
    }

    /**
     * @description 只有项目责任人才能维护计划实际完成时间
     * @param context
     * @param args
     * @author lsa
     */
    public StringList whetherEditByOwner(Context context, String[] args){
        StringList result = new StringList();
        try {
            Map argsMaps = JPO.unpackArgs(args);
            Map requestMap = (Map) argsMaps.get(STRING_REQUESTMAP);
            MapList objectList = (MapList) argsMaps.get(STRING_OBJECTLIST);
            if(!objectList.isEmpty()){
                for (int i = 0; i < objectList.size(); i++) {
                    Map ecoTask = (Map) objectList.get(i);
                    String current = (String) ecoTask.get("current");
                    String currentUser = (String) ecoTask.get("to[Assigned Tasks].from.name");
                    String contextUser = context.getUser();
                    result.add((contextUser.equals(currentUser) && "Active".equals(current))?"true":"false");
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return result;
    }


    /**
     * @description 获取数组中的下标
     * @param arr
     * @param target
     * @author lsa
     */
    public static int findIndex(String[] arr, String target) {
        for (int i = 0; i < arr.length; i++) {
            if (arr[i].equals(target)) {
                return i;
            }
        }
        return -1; // 未找到元素时返回-1
    }

    public static Map getECRSyncAttributeList(Map ecrAttrMap){
        Map synMap = new HashMap<>();
        StringList ecrAttributeList = new StringList();
        ecrAttributeList.add("JFProjectPhase");
        ecrAttributeList.add("JFECRChangeType");
        ecrAttributeList.add("JFChangeSource");
        ecrAttributeList.add("JFIsPlatformPart");
        ecrAttributeList.add("JFAffectedFactory");
        ecrAttributeList.add("JFChangesDeveExpensesManHours");
        ecrAttributeList.add("JFChangesTrialExpensesManHours");
        ecrAttributeList.add("JFChangesDeveExpensesManHoursExternal");
        ecrAttributeList.add("JFChangesTrialExpensesManHoursExternal");
        ecrAttributeList.add("JFChangeReson");
        ecrAttributeList.add("JFBreakpointMode");
        for (int i = 0; i < ecrAttributeList.size(); i++) {
            String strAttrName = ecrAttributeList.get(i);
            if (ecrAttrMap.containsKey(strAttrName)) {
                synMap.put(strAttrName,ecrAttrMap.get(strAttrName));
            }
        }
        return synMap;
    }


    /**
    *
    *@description 钩爪ECO 执行任务动态列
     * update by  ljr 20260204
    *@param context
	*@param args
    *@return java.util.List
    *@throws
    *@author CHENYAN
    *@date 2024/10/29 16:02
    */

    public List getDynamicFieldByECO(Context context,String[] args) throws Exception{
        HashMap inputMap = (HashMap) JPO.unpackArgs(args);
        HashMap requestMap    = (HashMap) inputMap.get("requestMap");
        String strLoginUser = context.getUser();
        //ECO ID
        String strObjectId = null;
        if(requestMap == null){
            strObjectId   = (String)inputMap.get("objectId");
        }else{
            strObjectId   = (String)requestMap.get("objectId");
        }
        StringList fieldList = new StringList();
        fieldList.add("JFResponsible");
        fieldList.add("JFTaskEstimatedFinishDate");
        fieldList.add("JFDAActualFinishTime");
        fieldList.add("JF_ECPwhetherToPostpone");
        fieldList.add("JF_ECPwhetherToPostponeRoute");
        fieldList.add("JFAttachment");
        fieldList.add("originated");
        DomainObject ECO = DomainObject.newInstance(context, strObjectId);
        Map ecoInfo = ECO.getInfo(context, StringList.create(SELECT_CURRENT, SELECT_OWNER, SELECT_ORIGINATED));
        String strCurrent = (String)ecoInfo.get(SELECT_CURRENT);
        String originated = (String)ecoInfo.get(SELECT_ORIGINATED);
        String strOwner = (String)ecoInfo.get(SELECT_OWNER);

         MapList fieldMapList = new MapList();
        for (int i = 0; i < fieldList.size(); i++) {
            Map<Object, Object> colMap = new HashMap<>();
            Map settingsMap = new HashMap<>();
            String strFieldName = fieldList.get(i);
            String strUpdateFunction = "";
            String strUpdateProgram = "";
            String strEditAccessFunction = "";
            String strEditAccessProgram = "";
            String strEditable = "";
            String strRequired = "";
            String strAlternateTypeExpression = "";
            String strAlternateOIDExpression = "";
            String strExpressionBusinessObject = "";
            String strName = "";
            String strLabel = "";
            String strRange = "";
            String strRegisteredSuite = "ProgramCentral";
            String strInputType = "";
            String strFormat = "";
            String strOnChangeHandler = "";
            if ("originated".equalsIgnoreCase(strFieldName)) {
                //add  by  ljr 20260205
                strLabel = "emxFramework.Attribute.JFCreateTime";
                strFormat = "date";
                strEditable = "false";
                strRegisteredSuite = "Framework";
                strName = "Originated";
                strExpressionBusinessObject = "originated";
                settingsMap.put("Field Type","basic");
//                settingsMap.put("hidden","true");
            } else if("JF_ECPwhetherToPostpone".equalsIgnoreCase(strFieldName)) {
                //add  by  ljr 20260205
                strLabel = "emxFramework.Attribute.JF_ECPwhetherToPostpone";
                strEditable = "false";
                strRegisteredSuite = "Framework";
                strName = "JF_ECPwhetherToPostpone";
                strExpressionBusinessObject = "attribute[JF_ECPwhetherToPostpone]";
                settingsMap.put("Field Type","attribute");
                settingsMap.put("Admin Type","attribute_JF_ECPwhetherToPostpone");
//                settingsMap.put("hidden","true");
            } else if("JF_ECPwhetherToPostponeRoute".equalsIgnoreCase(strFieldName)) {
                //add  by  ljr 20260205
                strLabel = "emxFramework.JF_ECPwhetherToPostpone.Route";
                strEditable = "false";
                strRegisteredSuite = "Framework";
                strName = "JF_ECPwhetherToPostponeRoute";
                strInputType = "textbox";
                settingsMap.put("Column Type","programHTMLOutput");
                settingsMap.put("Width","200");
                settingsMap.put("function","getPostponeRoute");
                settingsMap.put("program","JF_ChangeExecutionECOSource");
            }else
            if ("JFResponsible".equals(strFieldName)){
                if (("Create".equals(strCurrent) || "In_Work".equals(strCurrent) && strLoginUser.equals(strOwner))){
                    strRequired = "true";
                }else {
                    strRequired = "false";
                }
                // startSetting.put("Show Clear Button", "true");
                strUpdateFunction = "updateResponsibleFunc";
                strUpdateProgram = "JF_DeviationApplicationSource";
                strEditAccessFunction = "whetherEditByResponse";
                strEditAccessProgram = "JF_ChangeExecutionECOSource";
                strEditable = "true";
                strAlternateTypeExpression = "$<to[relationship_AssignedTasks].from.type>";
                strAlternateOIDExpression = "$<to[relationship_AssignedTasks].from.id>";
                strName = "JFResponsible";
                strLabel = "emxProgramCentral.Attribute.responsible";
                strRange = "${COMMON_DIR}/emxFullSearch.jsp?table=PMCCommonPersonSearchTable&field=TYPES=type_Person:CURRENT=policy_Person.state_Active&searchMode=GeneralPeopleTypeMode&form=PMCCommonPersonSearchForm&selection=multiple&suiteKey=ProgramCentral&HelpMarker=emxhelpriskassign&submitURL=./AEFSearchUtil.jsp";
                strExpressionBusinessObject = "to[Assigned Tasks].from[Person].name";
                strFormat = "user";
                strInputType = "textbox";
            }else if ("JFTaskEstimatedFinishDate".equals(strFieldName)){
                if (("Create".equals(strCurrent) || "In_Work".equals(strCurrent) && strLoginUser.equals(strOwner))){
                    strRequired = "true";
                }else {
                    strRequired = "false";
                }
//                 startSetting.put("Show Clear Button", "true");
//                settingsMap.put("Field Type", "attribute");
                strEditAccessFunction = "whetherEditByResponse";
                strEditAccessProgram = "JF_ChangeExecutionECOSource";
                strEditable = "true";
                strName = "JFTaskEstimatedFinishDate";
                strLabel = "emxProgramCentral.Attribute.TaskEstimatedFinishDate";
                strExpressionBusinessObject = "attribute[Task Estimated Finish Date].value";
                strFormat = "date";
                strInputType = "textbox";
                strUpdateFunction = "updateExecTaskAttrFunc";
                strUpdateProgram = "JF_DeviationApplicationSource";
                //add  by  ljr 20260204
                settingsMap.put("Validate","validateJFTaskEstimatedFinishDate");
//                settingsMap.put("TaskOriginated",originated);
                //如果是时间属性必须加这个,不加英文环境会报错
                settingsMap.put("Field Type","attribute");
                settingsMap.put("IgnoreTimeZone","true");
            }else if ("JFDAActualFinishTime".equals(strFieldName)){
                if ("ExecuteFeedback".equals(strCurrent)){
                    strRequired = "true";
                }else {
                    strRequired = "false";
                }
//                 startSetting.put("Show Clear Button", "true");
                strEditAccessFunction = "whetherEditByOwner";
                strEditAccessProgram = "JF_ChangeExecutionECOSource";
                strEditable = "true";
                strName = "JFDAActualFinishTime";
                strLabel = "emxProgramCentral.Lable.JFDAActualFinishTime";
                strExpressionBusinessObject = "attribute[JF_DAActualFinishTime].value";
                strFormat = "date";
                strInputType = "textbox";
                strUpdateFunction = "updateExecTaskAttrFunc";
                strUpdateProgram = "JF_DeviationApplicationSource";
                settingsMap.put("Field Type","attribute");
            }else if ("JFAttachment".equals(strFieldName)){
                if ("ExecuteFeedback".equals(strCurrent)){
                    strRequired = "true";
                }else {
                    strRequired = "false";
                }
//                 startSetting.put("Show Clear Button", "true");
                strEditAccessFunction = "whetherEditByOwner";
                strEditAccessProgram = "JF_ChangeExecutionECOSource";
                strEditable = "false";
                strName = "JFAttachment";
                strLabel = "emxProgramCentral.Attribute.Attachment";
                strInputType = "textbox";
                settingsMap.put("Column Type","programHTMLOutput");
                settingsMap.put("Width","280");
                settingsMap.put("function","getAttachmentContent");
                settingsMap.put("program","JF_ChangeExecutionECOSource");
            }
//            settingsMap.put("Column Type","attribute");
            //更新方法
            if (UIUtil.isNotNullAndNotEmpty(strUpdateFunction)){
                settingsMap.put("Update Function",strUpdateFunction);
            }
            if (UIUtil.isNotNullAndNotEmpty(strUpdateProgram)){
                settingsMap.put("Update Program",strUpdateProgram);
            }
            if (UIUtil.isNotNullAndNotEmpty(strEditAccessFunction)){
                settingsMap.put("Edit Access Function",strEditAccessFunction);
            }
            if (UIUtil.isNotNullAndNotEmpty(strEditAccessProgram)){
                settingsMap.put("Edit Access Program",strEditAccessProgram);
            }
            settingsMap.put("Editable",strEditable);
            settingsMap.put("Required",strRequired);
            if (UIUtil.isNotNullAndNotEmpty(strAlternateTypeExpression)){
                settingsMap.put("Alternate Type expression",strAlternateTypeExpression);
            }
            if (UIUtil.isNotNullAndNotEmpty(strAlternateOIDExpression)){
                settingsMap.put("Alternate OID expression",strAlternateOIDExpression);
            }
            //format
            if (UIUtil.isNotNullAndNotEmpty(strFormat)){
                settingsMap.put("format",strFormat);
            }
            settingsMap.put("Registered Suite",strRegisteredSuite);
            settingsMap.put("Input Type",strInputType);
            colMap.put("settings",settingsMap);
            colMap.put("name",strName);
            colMap.put("label",strLabel);
            if (UIUtil.isNotNullAndNotEmpty(strRange)){
                colMap.put("range",strRange);
            }
            if (UIUtil.isNotNullAndNotEmpty(strExpressionBusinessObject)){
                colMap.put("expression_businessobject", strExpressionBusinessObject);
            }
            fieldMapList.add(colMap);
        }
        logger.info("fieldMapList：{}",fieldMapList);
        return fieldMapList ;
    }


    public List getDynamicFieldByDA(Context context,String[] args) throws Exception{
        HashMap inputMap = (HashMap) JPO.unpackArgs(args);
        HashMap requestMap    = (HashMap) inputMap.get("requestMap");
        String strLoginUser = context.getUser();
        //ECO ID
        String strObjectId = null;
        if(requestMap == null){
            strObjectId   = (String)inputMap.get("objectId");
        }else{
            strObjectId   = (String)requestMap.get("objectId");
        }
        StringList fieldList = new StringList();
//        fieldList.add("Title");
//        fieldList.add("description");
        fieldList.add("responsible");
        fieldList.add("TaskEstimatedFinishDate");
        fieldList.add("JF_DAActualFinishTime");
        fieldList.add("JF_DAwhetherToPostpone");
        fieldList.add("JF_ECPwhetherToPostponeRoute");
        fieldList.add("JF_CostChanges");
        fieldList.add("JFAttachment");
        DomainObject ECO = DomainObject.newInstance(context, strObjectId);
        Map ecoInfo = ECO.getInfo(context, StringList.create(SELECT_CURRENT, SELECT_OWNER));
        String strCurrent = (String)ecoInfo.get(SELECT_CURRENT);
        String strOwner = (String)ecoInfo.get(SELECT_OWNER);

        MapList fieldMapList = new MapList();
        for (int i = 0; i < fieldList.size(); i++) {
            Map<Object, Object> colMap = new HashMap<>();
            Map settingsMap = new HashMap<>();
            String strFieldName = fieldList.get(i);
            String strUpdateFunction = "";
            String strUpdateProgram = "";
            String strEditAccessFunction = "";
            String strEditAccessProgram = "";
            String strEditable = "";
            String strRequired = "";
            String strAlternateTypeExpression = "";
            String strAlternateOIDExpression = "";
            String strExpressionBusinessObject = "";
            String strName = "";
            String strLabel = "";
            String strRange = "";
            String strRegisteredSuite = "ProgramCentral";
            String strInputType = "";
            String strFormat = "";
            if ("Title".equals(strFieldName)){
                if (("In_Work".equals(strCurrent))){
                    strRequired = "true";
                }else {
                    strRequired = "false";
                }
                // startSetting.put("Show Clear Button", "true");
                strUpdateFunction = "updateExecTaskAttrFunc";
                strUpdateProgram = "JF_DeviationApplicationSource";
                strEditAccessFunction = "IsEditByProjectManager";
                strEditAccessProgram = "JF_DeviationApplicationSource";
                strEditable = "false";
                strName = "Title";
                strLabel = "emxProgramCentral.Attribute.Title";
                strExpressionBusinessObject = "attribute[Title].value";
                strInputType = "textbox";
                settingsMap.put("Admin Type","attribute_Title");
                settingsMap.put("Field Type","attribute");
//                settingsMap.put("Range Function","getAllRangeWorkTask");
//                settingsMap.put("Range Program","JF_DeviationApplicationSource");
//                settingsMap.put("function","getAllWorkTaskContent");
//                settingsMap.put("program","JF_DeviationApplicationSource");
            }else if ("description".equals(strFieldName)){
                if (("In_Work".equals(strCurrent))){
                    strRequired = "true";
                }else {
                    strRequired = "false";
                }
                // startSetting.put("Show Clear Button", "true");
                strUpdateFunction = "updateExecTaskAttrFunc";
                strUpdateProgram = "JF_DeviationApplicationSource";
                strEditAccessFunction = "IsEditByProjectManager";
                strEditAccessProgram = "JF_DeviationApplicationSource";
                strEditable = "true";
                strName = "description";
                strLabel = "emxProgramCentral.Attribute.JFWorkContent";
                strExpressionBusinessObject = "description";
                strInputType = "textbox";
                settingsMap.put("Field Type","basic");
            } else if ("responsible".equals(strFieldName)){
                if (("In_Work".equals(strCurrent))){
                    strRequired = "true";
                }else {
                    strRequired = "false";
                }
                // startSetting.put("Show Clear Button", "true");
                strUpdateFunction = "updateResponsibleFunc";
                strUpdateProgram = "JF_DeviationApplicationSource";
                strEditAccessFunction = "IsEditByProjectManagerforDate";
                strEditAccessProgram = "JF_DeviationApplicationSource";
                strEditable = "true";
                strName = "responsible";
                strLabel = "emxProgramCentral.Attribute.responsible";
                strRange = "${COMMON_DIR}/emxFullSearch.jsp?table=PMCCommonPersonSearchTable&field=TYPES=type_Person:CURRENT=policy_Person.state_Active&searchMode=GeneralPeopleTypeMode&form=PMCCommonPersonSearchForm&selection=multiple&suiteKey=ProgramCentral&HelpMarker=emxhelpriskassign&submitURL=./AEFSearchUtil.jsp";
                strFormat = "user";
                strInputType = "textbox";
//                strExpressionBusinessObject = "empty";
                strExpressionBusinessObject = "to[Assigned Tasks].from[Person].name";
                strAlternateTypeExpression = "$<to[relationship_AssignedTasks].from.type>";
                strAlternateOIDExpression = "$<to[relationship_AssignedTasks].from.id>";
//                settingsMap.put("Admin Type","program");
//                settingsMap.put("function","getResponsible");
//                settingsMap.put("program","JF_DeviationApplicationSource");
                colMap.put("number", "4");
            }
            else if ("TaskEstimatedFinishDate".equals(strFieldName)){
                colMap.put("number", "5");
                if (("In_Work".equals(strCurrent))){
                    strRequired = "true";
                }else {
                    strRequired = "false";
                }
//                 startSetting.put("Show Clear Button", "true");
//                settingsMap.put("Field Type", "attribute");
                strEditAccessFunction = "IsEditByProjectManagerforDate";
                strEditAccessProgram = "JF_DeviationApplicationSource";
                strEditable = "true";
                strName = "TaskEstimatedFinishDate";
                strLabel = "emxProgramCentral.Attribute.TaskEstimatedFinishDate";
                strExpressionBusinessObject = "attribute[Task Estimated Finish Date].value";
                strFormat = "date";
                strInputType = "textbox";
                strUpdateFunction = "updateExecTaskAttrFunc";
                strUpdateProgram = "JF_DeviationApplicationSource";
                settingsMap.put("Field Type","attribute");
            }else if ("JF_DAActualFinishTime".equals(strFieldName)){
                colMap.put("number", "6");
                if ("Implement".equals(strCurrent)){
                    strRequired = "true";
                }else {
                    strRequired = "false";
                }
//                 startSetting.put("Show Clear Button", "true");
                strEditAccessFunction = "IsEditByTask";
                strEditAccessProgram = "JF_DeviationApplicationSource";
                strEditable = "true";
                strName = "JF_DAActualFinishTime";
                strLabel = "emxProgramCentral.Lable.JFDAActualFinishTime";
                strExpressionBusinessObject = "attribute[JF_DAActualFinishTime].value";
                strFormat = "date";
                strInputType = "textbox";
                strUpdateFunction = "updateExecTaskAttrFunc";
                strUpdateProgram = "JF_DeviationApplicationSource";
                settingsMap.put("Field Type","attribute");
            }else if ("JF_CostChanges".equals(strFieldName)){
                colMap.put("number", "7");
                strRequired = "false";
                strEditAccessFunction = "IsEditByTask";
                strEditAccessProgram = "JF_DeviationApplicationSource";
                strEditable = "true";
                strName = "JF_CostChanges";
                strLabel = "emxProgramCentral.Attribute.JF_CostChanges";
                strExpressionBusinessObject = "attribute[JF_CostChanges].value";
                strInputType = "textbox";
                strUpdateFunction = "updateExecTaskAttrFunc";
                strUpdateProgram = "JF_DeviationApplicationSource";
                settingsMap.put("Field Type","attribute");
            }else if ("JFAttachment".equals(strFieldName)){
                colMap.put("number", "8");
                if ("Implement".equals(strCurrent)){
                    strRequired = "true";
                }else {
                    strRequired = "false";
                }
                strEditAccessFunction = "IsEditByTask";
                strEditAccessProgram = "JF_DeviationApplicationSource";
                strEditable = "false";
                strName = "JFAttachment";
                strLabel = "emxProgramCentral.Attribute.Attachment";
                strInputType = "textbox";
                settingsMap.put("Column Type","programHTMLOutput");
                settingsMap.put("Width","280");
                settingsMap.put("function","getAttachmentContent");
                settingsMap.put("program","JF_ChangeExecutionECOSource");
            } else if("JF_DAwhetherToPostpone".equalsIgnoreCase(strFieldName)) {
                //add  by  ljr 20260205
                strLabel = "emxFramework.Attribute.JF_DAwhetherToPostpone";
                strEditable = "false";
                strRegisteredSuite = "Framework";
                strName = "JF_DAwhetherToPostpone";
                strExpressionBusinessObject = "attribute[JF_DAwhetherToPostpone]";
                settingsMap.put("Field Type","attribute");
                settingsMap.put("Admin Type","attribute_JF_DAwhetherToPostpone");
//                settingsMap.put("hidden","true");
            } else if("JF_ECPwhetherToPostponeRoute".equalsIgnoreCase(strFieldName)) {
                //add  by  ljr 20260205
                strLabel = "emxFramework.JF_ECPwhetherToPostpone.Route";
                strEditable = "false";
                strRegisteredSuite = "Framework";
                strName = "JF_ECPwhetherToPostponeRoute";
                strInputType = "textbox";
                settingsMap.put("Column Type","programHTMLOutput");
                settingsMap.put("Width","200");
                settingsMap.put("function","getPostponeRoute");
                settingsMap.put("program","JF_ChangeExecutionECOSource");
            }
//            settingsMap.put("Column Type","attribute");
            //更新方法
            if (UIUtil.isNotNullAndNotEmpty(strUpdateFunction)){
                settingsMap.put("Update Function",strUpdateFunction);
            }
            if (UIUtil.isNotNullAndNotEmpty(strUpdateProgram)){
                settingsMap.put("Update Program",strUpdateProgram);
            }
            if (UIUtil.isNotNullAndNotEmpty(strEditAccessFunction)){
                settingsMap.put("Edit Access Function",strEditAccessFunction);
            }
            if (UIUtil.isNotNullAndNotEmpty(strEditAccessProgram)){
                settingsMap.put("Edit Access Program",strEditAccessProgram);
            }
            settingsMap.put("Editable",strEditable);
            settingsMap.put("Required",strRequired);
            if (UIUtil.isNotNullAndNotEmpty(strAlternateTypeExpression)){
                settingsMap.put("Alternate Type expression",strAlternateTypeExpression);
            }
            if (UIUtil.isNotNullAndNotEmpty(strAlternateOIDExpression)){
                settingsMap.put("Alternate OID expression",strAlternateOIDExpression);
            }
            //format
            if (UIUtil.isNotNullAndNotEmpty(strFormat)){
                settingsMap.put("format",strFormat);
            }
            settingsMap.put("Registered Suite",strRegisteredSuite);
            settingsMap.put("Input Type",strInputType);
            colMap.put("settings",settingsMap);
            colMap.put("name",strName);
            colMap.put("label",strLabel);
            if (UIUtil.isNotNullAndNotEmpty(strRange)){
                colMap.put("range",strRange);
            }
            if (UIUtil.isNotNullAndNotEmpty(strExpressionBusinessObject)){
                colMap.put("expression_businessobject", strExpressionBusinessObject);
            }
            fieldMapList.add(colMap);
        }
        logger.info("fieldMapList：{}",fieldMapList);
        return fieldMapList ;
    }
    /*
     * @description:任务到审核状态的时候带动附件升级
     * @author: caipan
     * @date: 2026/2/4 10:30:48
     * @param: * @param[1] context
     * @param[2] args[0] 任务ID args[1] 文档的状态
     * @return:
     **/
    public void setJF_TaskFileToPromote(Context context,String[] args) throws Exception{
        logger.info("setJF_TaskFileToPromote start");
        DomainObject pcrObj = DomainObject.newInstance(context,args[0]);
        String fileState = args[1];
        StringList taskList = pcrObj.getInfoList(context,"from[Reference Document].to.id");
        for (int i = 0; i <taskList.size() ; i++) {
                MqlUtil.mqlCommand(context, false, "mod bus " + taskList.get(i) + " current "+fileState, true);
        }
        logger.info("setJF_TaskFileToPromote end");
    }

    /**
     * 获取延期流程在执行任务界面展示出来
     **
     * @param context
     * @param args
     * @return
     * @author caipan
     * @date 2026/6/10 14:33
     */
    public StringList getPostponeRoute(Context context, String[] args){
        logger.info("getPostponeRoute start");
        StringList resultList = new StringList();
        try {
            Map argMaps = JPO.unpackArgs(args);
            MapList argMap = (MapList) argMaps.get(STRING_OBJECTLIST);
            Iterator iterator = argMap.iterator();
            while (iterator.hasNext()){
                Map map = (Map) iterator.next();
                String objectId = (String) map.get("id");
                StringBuffer buffer = new StringBuffer();
                DomainObject object = DomainObject.newInstance(context, objectId);
                logger.info("objectIDRoute:{}",objectId);
                StringList selList = new StringList();
                selList.add("attribute[Title]");
                selList.add(DomainConstants.SELECT_ID);
                StringList relList = new StringList();
                relList.add(DomainRelationship.SELECT_ID);
                //20260811 update by codex 从任务的from端获取延期流程
                MapList mapList = object.getRelatedObjects(context, RELATIONSHIP_JFDELAYEDFILING2TASK, "JFDelayedFiling", selList, relList, true, false, (short) 1, "", "", 0);
                logger.info("mapList:{}",mapList);
                if(mapList.size()>0){
                    for (int i = 0; i < mapList.size(); i++) {
                        buffer.append("<table><td><a href=\"/3dspace/common/emxNavigator.jsp?objectId=");
                        Map projectMap = (Map) mapList.get(i);
                        logger.info("projectMap:{}",projectMap);
                        String folderId = (String) projectMap.get("id");
                        buffer.append(folderId).append("\" target=\"_blank\">");
                        String folderName = (String) projectMap.get("attribute[Title]");
                        folderName = StringEscapeUtils.escapeHtml4(folderName);
                        folderName =folderName.replace("&", "");
                        buffer.append(folderName);
                        buffer.append("</a></td></table>");
                    }
                }
                logger.info("getPostponeRoute:{}",resultList);
                resultList.add(buffer.toString());
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return resultList;
    }

    /**
     * 设置是否延期为Yes
     **
     * @param context
     * @param args
     * @throws Exception
     * @author caipan
     * @date 2026/6/10 14:53
     */
    public void setJF_ECPwhetherToPostpone(Context context, String[] args) throws Exception {
        String filingId = args[0];
        DomainObject Obj = DomainObject.newInstance(context,filingId);
       String delayDate =  Obj.getAttributeValue(context,ATTR_JFDELAYTHEPLANNEDDATE);
        StringList lists = Obj.getInfoList(context,"from["+RELATIONSHIP_JFDELAYEDFILING2TASK+"].to.id");
        for(int i=0;i<lists.size();i++) {
            DomainObject taskObj = DomainObject.newInstance(context, lists.get(i));
            String type = taskObj.getInfo(context,SELECT_TYPE);
            if("JF_ECOTask".equalsIgnoreCase(type)) {//ECO任务
                taskObj.setAttributeValue(context, ATTR_WHETHERTOPOSTPONE, "Yes");//设置延期
            }else if("JF_DATask".equalsIgnoreCase(type)) {
                taskObj.setAttributeValue(context, ATTR_DAWHETHERTOPOSTPONE, "Yes");//设置延期
            }
            taskObj.setAttributeValue(context, ATTR_Task_Estimated_Finish_Date, delayDate);//设置延期日期
        }
    }


    /**
     * 校验ECO、DA延期计划日期并更新延期申请
     **
     * @param context
     * @param args
     * @throws Exception
     * @author caipan
     * @date 2026/6/16 09:37
     */
    public int updateDelayDate(Context context, String[] args) throws Exception {
        try {
            Map requestMap = JPO.unpackArgs(args);
            logger.info("------------------------updateDelayDate ---------------------------requestMap:"+requestMap);
            Map paramMap = (Map) requestMap.get("paramMap");
            String newValue = (String)paramMap.get("New Value");
            String newOID = (String)paramMap.get("New OID");
            String filingId = (String) paramMap.get("objectId");

            Map modeRequest = (Map)requestMap.get("requestMap");
            String[] mode = (String[])modeRequest.get("mode");
            logger.info("mode:{}",mode);
            String modes = Arrays.stream(mode).findFirst().get();
            if("create".equalsIgnoreCase(modes)) {
                return 0;
            }
       /*     logger.info("------------------------updateEvaluatePrice ---------------------------newOID:"+newOID);
            logger.info("------------------------updateEvaluatePrice ---------------------------newValue:"+newValue);

            DomainObject taskDomain = DomainObject.newInstance(context, taskId);
            taskDomain.setAttributeValue(context,"JF_EvaluatePrice",newValue);*/
            DomainObject filingObj = DomainObject.newInstance(context,filingId);
            String relatedTaskId = filingObj.getInfo(context,
                    "from[" + RELATIONSHIP_JFDELAYEDFILING2TASK + "].to.id");
            if (UIUtil.isNullOrEmpty(relatedTaskId)) {
                throw new Exception(EnoviaResourceBundle.getProperty(context, SUITE_KEY,
                        context.getLocale(), "emxFramework.Notice.SelectOneECOTaskForDelayRequest"));
            }
            DomainObject taskObj = DomainObject.newInstance(context, relatedTaskId);
            //20260820 update by caipan 编辑延期申请时按关联执行任务类型统一校验计划日期
            String delayDate = validateDelayPlannedDate(context, taskObj, newValue);
            filingObj.setAttributeValue(context,ATTR_JFDELAYTHEPLANNEDDATE,delayDate);
        }catch (Exception e){
            logger.error("updateDelayDate------error",e);
            emxContextUtil_mxJPO.mqlNotice(context, e.getMessage());
            return 1;
        }
        return 0;
    }

    /**
     * 是否断点失效字段编辑权限
     **
     * @param context
     * @param args Form字段权限参数
     * @return boolean 非完成状态返回true
     * @throws Exception 获取ECO状态失败时抛出异常
     * @author caipan
     * @date 2026/8/14 14:06
     */
    public boolean getECOBreakpointInvalidEditAccess(Context context, String[] args) throws Exception {
        Map programMap = JPO.unpackArgs(args);
        Map requestMap = (Map) programMap.get("requestMap");
        String objectId = requestMap == null ? DomainConstants.EMPTY_STRING : (String) requestMap.get("objectId");
        if (UIUtil.isNullOrEmpty(objectId)) {
            return false;
        }

        String current = DomainObject.newInstance(context, objectId).getInfo(context, SELECT_CURRENT);
        return !"Complete".equals(current);
    }
    /**
     * 如果整椅切换/断点计划发布发布给项目组这个ECO任务更新了计划完成时间需要同步更新ECO的计划断点时间
     **
     * @param context
     * @param args
     * @throws Exception
     * @author caipan
     * @date 2026/8/18 14:00
     */
    public void modifyAttribute(Context context,String[] args) throws Exception{
        logger.info("modifyAttribute start");
        String objectId = args[0];
        String attributeName = args[1];
        String newValue = args[3];
        logger.info("newValue 转换之前:{}",newValue);
        DomainObject object = DomainObject.newInstance(context,objectId);
        String title = object.getAttributeValue(context,DomainConstants.ATTRIBUTE_TITLE);
        if("Task Estimated Finish Date".equalsIgnoreCase(attributeName)&&isBreakpointPlanTaskTitle(title)){
            //获取ECO对象，并且设置ECO的 ATTR_JFBREAKPOINTTIME
            DomainObject ecoObj = DomainObject.newInstance(context, object.getInfo(context,"to[JFCO2ECOTask].from.id"));
            if(UIUtil.isNotNullAndNotEmpty(newValue)){
            /*    TimeZone tz = TimeZone.getTimeZone(context.getSession().getTimezone());
                double dbMilisecondsOffset = (double)(-1)*tz.getRawOffset();
                double dClientTimeZoneOffset = (Double.valueOf(dbMilisecondsOffset / (1000 * 60 * 60))).doubleValue();
                newValue = newValue.trim();
                int iDateFormat = eMatrixDateFormat.getEMatrixDisplayDateFormat();
                String strInputTime = eMatrixDateFormat.adjustTimeStringForInputFormat("");
                newValue = eMatrixDateFormat.getFormattedInputDateTime(newValue, strInputTime, iDateFormat, dClientTimeZoneOffset, context.getLocale());*/
                //20260818 update by codex caipan 兼容中文、英文及达索系统日期格式
                newValue = JF_Util_mxJPO.convertDateToMatrixFormat(newValue);
                logger.info("newValue 转换之后:{}",newValue);
                ecoObj.setAttributeValue(context, ATTR_JFBREAKPOINTTIME, newValue);
                logger.info("modifyAttribute end");
            }

        }
    }

    /**
     * 发起流程的时候需要检查，执行任务的状态，必须是工作中(Active)才可以
     **
     * @param context
     * @param args
     * @return
     * @throws Exception
     * @author caipan
     * @date 2026/8/19 13:45
     */
    public int checkTaskStatus(Context context,String[] args) throws Exception{
        String objectId = args[0];
        DomainObject object = DomainObject.newInstance(context,objectId);
        String taskStatus = object.getInfo(context,"from["+JF_PLMConstants_mxJPO.RELATIONSHIP_JFDELAYEDFILING2TASK+"].to.current");
        if(taskStatus.equalsIgnoreCase("Active")){
            return 0;
        }else{
            String message = EnoviaResourceBundle.getProperty(context, SUITE_KEY, context.getLocale(), "emxFramework.Notice.delayPromoteCheck");
            emxContextUtil_mxJPO.mqlNotice(context, message);
            return 1;
        }
    }

}
