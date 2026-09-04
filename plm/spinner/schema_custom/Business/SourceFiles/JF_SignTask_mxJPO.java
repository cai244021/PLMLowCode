import com.dscn.plm.util.NioJDUtils;
import com.matrixone.apps.common.Task;
import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.DomainRelationship;
import com.matrixone.apps.domain.util.*;
import com.matrixone.apps.framework.ui.ProgramCallable;
import com.matrixone.apps.framework.ui.UIUtil;
import matrix.db.Context;
import matrix.db.JPO;
import matrix.db.Page;
import matrix.db.RelationshipType;
import matrix.util.MatrixException;
import matrix.util.StringList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import org.w3c.dom.*;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import java.io.*;
import java.util.stream.Collectors;

/**
 * @ClassName mxJPO
 * @Author: LIUJR
 * @CreateDate: 2024/7/31 10:54
 * @UpdateRemark:
 * @Version: 1.0
 * @Description: ECR会签任务   ERC关联项目的人员角色校验
 */
public class JF_SignTask_mxJPO {
    private static final Logger JF_LOGGER = LoggerFactory.getLogger(JF_SignTask_mxJPO.class);
    private static final String TYPE_JS_SIGN_TASK = "type_JF_SignTask";
    private static final String TYPE_JS_CUSTOMER_TASK = "type_JF_CustomerTask";
    private static final String TYPE_JS_APR_TASK = "type_JF_APRTask";
    private static final String TYPE_JS_SIGN_TASK_TYPE = "JF_SignTask";
    private static final String TYPE_JS_CUSTOMER_TASK_TYPE = "JF_CustomerTask";
    private static final String TYPE_JS_APR_TASK_TYPE = "JF_APRTask";
    private static final String STRING_MQL_RELATIONSHIP_FROM = "from[%s].to.%s";
    private static final String RELATIONSHIP_JF_CHANGE_PROJECT = "JFChange2Project";
    private static final String RELATIONSHIP_JF_ECR_PART = "JFECR2OnelevelPart";
    private static final String ATTRIBUTE_PROCUREMENT_TYPE = "JF_VPMReference.JF_ProcurementType";
    private static final String ATTRIBUTE_DIRECT_BUY = "JF_VPMReference.JF_DirectBuy";
    private static final String ATTRIBUTE_CHANGE_SOURCE = "JFChangeSource";
    private static final String RELATIONSHIP_JF_PART_PART = "JFOneLevelPart2Part";
    private static final String RELATIONSHIP_JF_ECR_TASK = "JFECR2Task";
    private static final String STRING_MQL_ATTRIBUTE = "attribute[%s].value";
    private static final String STRING_MQL_RELATIONSHIP_TO = "to[%s].from.%s";
    private static final String SUITE_KEY = "emxComponentsStringResource";
//    private static final String STRING_SIGN_TASK_PROPERTIES = "SignTaskProperties.xml";
    private static final String STRING_SIGN_TASK_PROPERTIES_ZH = "SignTaskProperties_zh.xml";
    private static final StringList strBusSelectList = new StringList();
    private static final StringList strRelSelectList = new StringList();
    private static final String ProjectSpace_PersonRole = "ProjectSpace.Person.ProjectRole";
    private static final String ProjectSpace_PersonRole_NewECR = "ProjectSpace.NewECR.ProjectRole";
    private static final String Foam_AME_representative = "Foam AME representative";
    private static final String Trim_AME_representative = "Trim AME representative";
    static {
        strBusSelectList.add(DomainConstants.SELECT_ID);
        strBusSelectList.add(DomainConstants.SELECT_NAME);
        strBusSelectList.add(DomainConstants.SELECT_TYPE);
        strBusSelectList.add(DomainConstants.SELECT_ATTRIBUTE_TITLE);
        strBusSelectList.add(DomainConstants.SELECT_OWNER);

        strRelSelectList.add(DomainRelationship.SELECT_ID);
    }


    /**
     * 生成会签任务
     * @param context
     * @param args
     * @author LIUJR
     * @throws
     * @return void
     * @date 2024/7/31 9:28
     * @description
     */
    public void GenerateCountersignatureTask(Context context, String[] args) throws Exception {
        String strPush = "N";
        try {
            JF_LOGGER.info("#################GenerateCountersignatureTask#######################");
            //ECR id
            String strECRId = args[0];
            DomainObject domainObject = DomainObject.newInstance(context, strECRId);
            String strECRName = domainObject.getInfo(context, DomainConstants.SELECT_NAME);
            StringList signTaskList = domainObject.getInfoList(context, "from[" + RELATIONSHIP_JF_ECR_TASK + "|to.type=='" + TYPE_JS_SIGN_TASK_TYPE + "'].to.id");
            if (signTaskList.size() > 0) {
                //如果已经生成任务 不再次生成！
                return;
            }
            String strProjectId = domainObject.getInfo(context, String.format(STRING_MQL_RELATIONSHIP_FROM, RELATIONSHIP_JF_CHANGE_PROJECT, DomainConstants.SELECT_ID));
            //拿取项目成员中的人员的project role
            Map projectPersonAndProjectRole = getProjectPersonAndProjectRole(context, strProjectId, "NA", DomainConstants.EMPTY_STRING, DomainConstants.EMPTY_STRING);
            //该项目所有的角色列表
            StringList projectRoleList = (StringList) projectPersonAndProjectRole.get("projectRoleList");
            //该项目角色列表对应的人员
            Map personAndRoleMap = (Map) projectPersonAndProjectRole.get("personAndRoleMap");
            JF_LOGGER.info("personAndRoleMap:{}", personAndRoleMap.toString());
            JF_LOGGER.info("projectRoleList:{}", projectRoleList.toString());
            //获取该ECR关联的影响件中是否有ICO
            //获取该ECR关联的
            StringList subSelectList = new StringList();
            subSelectList.add(String.format(STRING_MQL_ATTRIBUTE, ATTRIBUTE_PROCUREMENT_TYPE));
            subSelectList.add(String.format(STRING_MQL_ATTRIBUTE, ATTRIBUTE_DIRECT_BUY));
            MapList resICOMapList = domainObject.getRelatedObjects(
                    context,
                    JF_PLMConstants_mxJPO.REL_JFECRRelateRoot + "," + JF_PLMConstants_mxJPO.REL_JFECRRoot2Item,
                    DomainConstants.QUERY_WILDCARD,
                    subSelectList,
//                    new StringList("attribute[JFECRName].value"),
                    new StringList(),
                    false,
                    true,
                    (short) 0, // recursion level
                    "", //object where clause
                    "", //relationship where clause
                    0
            );
            JF_LOGGER.info("resICOMapList:{}", resICOMapList.toString());
            //"attribute[JFECRName].value=='" + strECRName + "'"
//            MapList mapList1 = new MapList();
//            resICOMapList.stream().map(m -> {
//                Map map = (Map) m;
//                String strRelECRName = UIUtil.getValue(map, "attribute[JFECRName].value");
//                if (strRelECRName.equalsIgnoreCase(strECRName)) {
//                    mapList1.add(map);
//                }
//                return strRelECRName;
//            }).collect(Collectors.toCollection(StringList::new));
//            JF_LOGGER.info("mapList1:{}", mapList1.toString());
            boolean icoFlag = resICOMapList.toString().contains("ICO");
//            String strWhere = String.format(STRING_MQL_ATTRIBUTE, ATTRIBUTE_DIRECT_BUY) + "=Y";
//            boolean directBuyFlag = mapList1.toString().contains(strWhere);
            JF_LOGGER.info("icoFlag:{}", icoFlag);
//            JF_LOGGER.info("directBuyFlag:{}", directBuyFlag);
            //读取配置文件
            MapList mapList = readSignTaskPropertiesFile(context, STRING_SIGN_TASK_PROPERTIES_ZH, "signTask");
            JF_LOGGER.info("mapList:{}", mapList.toString());
            //工具类
            //创建任务数据
            MapList requestMap = new MapList();
            JF_Util_mxJPO jfUtilMxJPO = new JF_Util_mxJPO();
            Iterator iterator = mapList.iterator();
            while (iterator.hasNext()) {
                Map map = (Map) iterator.next();
                String id = UIUtil.getValue(map, DomainConstants.SELECT_ID);
                String name = UIUtil.getValue(map, DomainConstants.SELECT_NAME);
                String check = UIUtil.getValue(map, "check");
                String role = UIUtil.getValue(map, "role");
                if ("ICO".equalsIgnoreCase(check)) {
                    //需要拿取ICO
                    //需要看供应单
                    if (!icoFlag) {
                        //如果没有ICO,就不需要生成任务
                        continue;
                    }
                }
                if ("DirectBuy".equalsIgnoreCase(check)) {
                    //拿取 Direct Buy
//                    if (!directBuyFlag) {
//                        //如果没有Direct Buy,就不需要生成任务
//
//                    }
                    continue;
                }
                if (check.contains("attribute[JF_VPMReference.JF_PartType]")) {
                    continue;
                }
                //匹配人员
                if (UIUtil.isNotNullAndNotEmpty(role) && projectRoleList.contains(role)) {
                    //包含人
                    String person = UIUtil.getValue(personAndRoleMap, role);
                    //构造参数
                    HashMap<String, String> paramsMap = new HashMap<>();
                    paramsMap.put("taskName", name);
                    paramsMap.put("taskType", TYPE_JS_SIGN_TASK);
                    paramsMap.put("taskOwner", person);
                    paramsMap.put("parentId", strECRId);
                    paramsMap.put("role", role);
                    requestMap.add(paramsMap);
                }
            }
            ContextUtil.startTransaction(context, true);
            JF_LOGGER.info("requestMap:{}", requestMap.toString());
            Iterator iterator1 = requestMap.iterator();
            MapList taskIdMapList = new MapList();
            while (iterator1.hasNext()) {
                Map paramsMap = (Map) iterator1.next();
                JF_LOGGER.info("paramsMap:{}", paramsMap.toString());
                String taskId = jfUtilMxJPO.createTask(context, JPO.packArgs(paramsMap));
                paramsMap.put("taskId", taskId);
                taskIdMapList.add(paramsMap);
            }
            ContextUtil.pushContext(context);
            strPush = "Y";
            Iterator iterator2 = taskIdMapList.iterator();
            while (iterator2.hasNext()) {
                Map paramsMap  = (Map) iterator2.next();
                String taskId = UIUtil.getValue(paramsMap, "taskId");
                //提升到工作中
                DomainObject signTask = DomainObject.newInstance(context, taskId);
//                signTask.promote(context);
                MqlUtil.mqlCommand(context, true, "mod bus "+taskId+" current Active", false);//去掉触发发邮件
                //关联ecr
                DomainRelationship domainRelationship = domainObject.addToObject(context, new RelationshipType(RELATIONSHIP_JF_ECR_TASK), taskId);
                projectTaskToActiveSendEmail(context, new String[]{taskId});
                domainRelationship.setAttributeValue(context, DomainConstants.ATTRIBUTE_PROJECT_ROLE, UIUtil.getValue(paramsMap, "role") );
            }
            ContextUtil.commitTransaction(context);
        } catch (Exception e) {
            ContextUtil.abortTransaction(context);
            e.printStackTrace();
            throw e;
        }finally {
            if ("Y".equalsIgnoreCase(strPush)) {
                ContextUtil.popContext(context);
            }
        }
    }

    /**
     * New  ECR生成会签任务
     * @param context
     * @param args
     * @author LIUJR
     * @throws
     * @return void
     * @date 2024/7/31 9:28
     * @description
     */
    public void newECRGenerateCountersignatureTask(Context context, String[] args) throws Exception {
        String strPush = "N";
        try {
            JF_LOGGER.info("#################GenerateCountersignatureTask#######################");
            //ECR id
            String strECRId = args[0];
            DomainObject domainObject = DomainObject.newInstance(context, strECRId);
            StringList signTaskList = domainObject.getInfoList(context, "from[" + RELATIONSHIP_JF_ECR_TASK + "|to.type=='" + TYPE_JS_SIGN_TASK_TYPE + "'].to.id");
            if (signTaskList.size() > 0) {
                //如果已经生成任务 不再次生成！
                return;
            }
            //获取ecr的自制的发泡 面套
            StringList relateItemList = getECRItemHadFormAndTrim(context, strECRId);
            String strProjectId = domainObject.getInfo(context, String.format(STRING_MQL_RELATIONSHIP_FROM, RELATIONSHIP_JF_CHANGE_PROJECT, DomainConstants.SELECT_ID));
            //拿取项目成员中的人员的project role
            Map projectPersonAndProjectRole = getProjectPersonAndProjectRole(context, strProjectId, "NA", DomainConstants.EMPTY_STRING, DomainConstants.EMPTY_STRING);
            //该项目所有的角色列表
            StringList projectRoleList = (StringList) projectPersonAndProjectRole.get("projectRoleList");
            //该项目角色列表对应的人员
            Map personAndRoleMap = (Map) projectPersonAndProjectRole.get("personAndRoleMap");
            JF_LOGGER.info("personAndRoleMap:{}", personAndRoleMap.toString());
            JF_LOGGER.info("projectRoleList:{}", projectRoleList.toString());
            //获取该ECR关联的影响件中是否有ICO
            //获取该ECR关联的
            StringList subSelectList = new StringList();
            subSelectList.add(String.format(STRING_MQL_ATTRIBUTE, ATTRIBUTE_PROCUREMENT_TYPE));
            subSelectList.add(String.format(STRING_MQL_ATTRIBUTE, ATTRIBUTE_DIRECT_BUY));
            //获取不是游离的第一层级
            DomainObject partObject = DomainObject.newInstance(context);
            StringList ecrRootList = domainObject.getInfoList(context, "from[JFECRRelateRoot|" + JF_PLMConstants_mxJPO.SELECT_ATTRIBUTE_JF_freeState + "==N].to.id");
            ecrRootList.addAll(domainObject.getInfoList(context, "from[JFECRRelateRootBubble|" + JF_PLMConstants_mxJPO.SELECT_ATTRIBUTE_JF_freeState + "==N].to.id"));
            JF_LOGGER.info("ecrRootList:{}", ecrRootList.toString());
            MapList resICOMapList = new MapList();
            for (int i = 0; i < ecrRootList.size(); i++) {
                String rootId = ecrRootList.get(i);
                if (rootId.contains("=")) {
                    String[] split = rootId.split("=");
                    rootId = split[1].trim();
                }
                partObject.setId(rootId);
                MapList resMapList = partObject.getRelatedObjects(
                        context,
                        JF_PLMConstants_mxJPO.REL_JFECRRoot2Item + "," + JF_PLMConstants_mxJPO.REL_JFECRRoot2ItemBubble,
                        JF_PLMConstants_mxJPO.TYPE_VPMReference,
                        subSelectList,
                        new StringList(JF_PLMConstants_mxJPO.SELECT_ATTRIBUTE_JF_ECRID),
                        false,
                        true,
                        (short) 0, // recursion level
                        "", //object where clause
                        "", //relationship where clause
                        0
                );
                resICOMapList.addAll((MapList)resMapList.stream().filter(m -> {
                    Map map = (Map) m;
                    String strRelECRId = UIUtil.getValue(map, JF_PLMConstants_mxJPO.SELECT_ATTRIBUTE_JF_ECRID);
                    if (strRelECRId.contains(strECRId)) {
                        return true;
                    } else {
                        return false;
                    }
                }).collect(Collectors.toCollection(MapList::new)));
            }
            JF_LOGGER.info("resICOMapList:{}", resICOMapList.toString());
            boolean icoFlag = resICOMapList.toString().contains("ICO");
            JF_LOGGER.info("icoFlag:{}", icoFlag);
            //读取配置文件
            MapList mapList = readSignTaskPropertiesFile(context, STRING_SIGN_TASK_PROPERTIES_ZH, "signTask");
            JF_LOGGER.info("mapList:{}", mapList.toString());
            //工具类
            //创建任务数据
            MapList requestMap = new MapList();
            JF_Util_mxJPO jfUtilMxJPO = new JF_Util_mxJPO();
            Iterator iterator = mapList.iterator();
            while (iterator.hasNext()) {
                Map map = (Map) iterator.next();
                String id = UIUtil.getValue(map, DomainConstants.SELECT_ID);
                String name = UIUtil.getValue(map, DomainConstants.SELECT_NAME);
                String check = UIUtil.getValue(map, "check");
                String role = UIUtil.getValue(map, "role");
                if ("ICO".equalsIgnoreCase(check)) {
                    //需要拿取ICO
                    //需要看供应单
                    if (!icoFlag) {
                        //如果没有ICO,就不需要生成任务
                        continue;
                    }
                }
                if ("DirectBuy".equalsIgnoreCase(check)) {
                    //拿取 Direct Buy
//                    if (!directBuyFlag) {
//                        //如果没有Direct Buy,就不需要生成任务
//
//                    }
                    continue;
                }
                if (check.contains("attribute[JF_VPMReference.JF_PartType]")) {
                    //JFNewECR 会生成发泡件和面套件 生成会签任务
                    String[] split = check.split("=");
                    if (!relateItemList.contains(split[1])) {
                        continue;
                    }
                }
                //匹配人员
                if (UIUtil.isNotNullAndNotEmpty(role) && projectRoleList.contains(role)) {
                    //包含人
                    String person = UIUtil.getValue(personAndRoleMap, role);
                    //构造参数
                    HashMap<String, String> paramsMap = new HashMap<>();
                    paramsMap.put("taskName", name);
                    paramsMap.put("taskType", TYPE_JS_SIGN_TASK);
                    paramsMap.put("taskOwner", person);
                    paramsMap.put("parentId", strECRId);
                    paramsMap.put("role", role);
                    requestMap.add(paramsMap);
                }
            }
            ContextUtil.startTransaction(context, true);
            JF_LOGGER.info("requestMap:{}", requestMap.toString());
            Iterator iterator1 = requestMap.iterator();
            MapList taskIdMapList = new MapList();
            while (iterator1.hasNext()) {
                Map paramsMap = (Map) iterator1.next();
                JF_LOGGER.info("paramsMap:{}", paramsMap.toString());
                String taskId = jfUtilMxJPO.createTask(context, JPO.packArgs(paramsMap));
                paramsMap.put("taskId", taskId);
                taskIdMapList.add(paramsMap);
            }
            ContextUtil.pushContext(context);
            strPush = "Y";
            Iterator iterator2 = taskIdMapList.iterator();
            while (iterator2.hasNext()) {
                Map paramsMap  = (Map) iterator2.next();
                String taskId = UIUtil.getValue(paramsMap, "taskId");
                //提升到工作中
                DomainObject signTask = DomainObject.newInstance(context, taskId);
//                signTask.promote(context);
                MqlUtil.mqlCommand(context, true, "mod bus "+taskId+" current Active", false);//去掉触发发邮件
                //关联ecr
                DomainRelationship domainRelationship = domainObject.addToObject(context, new RelationshipType(RELATIONSHIP_JF_ECR_TASK), taskId);
                projectTaskToActiveSendEmail(context, new String[]{taskId});
                domainRelationship.setAttributeValue(context, DomainConstants.ATTRIBUTE_PROJECT_ROLE, UIUtil.getValue(paramsMap, "role") );
            }
            ContextUtil.commitTransaction(context);
        } catch (Exception e) {
            ContextUtil.abortTransaction(context);
            e.printStackTrace();
            throw e;
        }finally {
            if ("Y".equalsIgnoreCase(strPush)) {
                ContextUtil.popContext(context);
            }
        }
    }

    /**
    * 获取项目的person 与 project role
    * @param context
	* @param strProjectId
    * @author LIUJR
    * @throws
    * @return void
    * @date 2024/7/31 11:15
    * @description
    */
    public static Map getProjectPersonAndProjectRole(Context context, String strProjectId, String flag, String strBusWhere, String strRelWhere) {
        Map<String, Object> returnMap = new HashMap<>();
        try {
            DomainObject projectObject = DomainObject.newInstance(context, strProjectId);
            strRelSelectList.add(String.format(STRING_MQL_ATTRIBUTE, DomainConstants.ATTRIBUTE_PROJECT_ROLE));
            strBusSelectList.add(DomainConstants.SELECT_CURRENT);
            //拿取项目的成员以及关系属性project role
            MapList mapList = projectObject.getRelatedObjects(
                    context,
                    DomainRelationship.RELATIONSHIP_MEMBER,
                    DomainConstants.TYPE_PERSON,
                    strBusSelectList,
                    strRelSelectList,
                    false,
                    true,
                    (short) 1, // recursion level
                    strBusWhere, //object where clause
                    strRelWhere, //relationship where clause
                    0
            );
            HashMap<String, String> personAndRoleMap = new HashMap<>();
            if ("PS".equalsIgnoreCase(flag)) {
                returnMap.put("mapList", mapList);
                return returnMap;
            }
            StringList projectRoleList = (StringList) mapList.stream().map(m -> {
                Map map = (Map) m;
                String name = UIUtil.getValue(map, DomainConstants.SELECT_NAME);
                String projectRole = UIUtil.getValue(map, String.format(STRING_MQL_ATTRIBUTE, DomainConstants.ATTRIBUTE_PROJECT_ROLE));
                personAndRoleMap.put(projectRole, name);
                return projectRole;
            }).collect(Collectors.toCollection(StringList::new));
            returnMap.put("projectRoleList", projectRoleList);
            returnMap.put("personAndRoleMap", personAndRoleMap);
        } catch (FrameworkException e) {
            throw new RuntimeException(e);
        }
        return returnMap;
    }

    /**
    * 拿取xml的内容
    * @param context
	* @param pageName
    * @author LIUJR
    * @throws
    * @return com.matrixone.apps.domain.util.MapList
    * @date 2024/7/31 11:15
    * @description
    */
    public MapList readSignTaskPropertiesFile(Context context, String pageName, String flag) throws Exception{
        MapList mapList = new MapList();
        Page pageAttributePopulation = new Page(pageName);
        pageAttributePopulation.open(context);
        String strProperties = pageAttributePopulation.getContents(context);
        pageAttributePopulation.close(context);
        String createTaskType = flag;
        // 使用 ByteArrayInputStream 将字符串转为输入流
        try (InputStream inputStream = new ByteArrayInputStream(strProperties.getBytes("UTF-8"))) {
            // 创建 DocumentBuilder
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(inputStream);
            // 获取所有 config 元素
            NodeList configurationNodes = document.getElementsByTagName("configuration");
            for (int i = 0; i < configurationNodes.getLength(); i++) {
                Element configurationElement = (Element) configurationNodes.item(i);
                System.out.println("configurationElement:" + configurationElement.toString());
                if (createTaskType.equals(configurationElement.getAttribute("id"))) {
                    // 找到匹配的 configuration, 获取它下的 config 元素
                    NodeList configNodes = configurationElement.getElementsByTagName("config");
                    for (int j = 0; j < configNodes.getLength(); j++) {
                        Element configElement = (Element) configNodes.item(j);
                        String id = configElement.getAttribute("id");
                        String check = configElement.getAttribute("check");
                        String name = configElement.getAttribute("name");
                        String role = configElement.getAttribute("role");
                        HashMap<String, String> map = new HashMap<>();
                        map.put("id", id);
                        map.put("check", check);
                        map.put("name", name);
                        map.put("role", role);
                        mapList.add(map);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return mapList;
    }


    /**
    * 判断项目中的人员角色是否配置完整
    * @param context
	* @param args  项目
    * @author LIUJR
    * @throws
    * @return void
    * @date 2024/7/31 16:45
    * @description
    */
    public static int checkProjectPersonRole(Context context, String[] args) throws Exception{
        int iReturn = 0;
        JF_LOGGER.info("checkProjectPersonRole```````````````````````````````````");
        try {
            Map resultMap = new HashMap<String, String>();
            Map personMap = new HashMap<String, String>();
            StringList checkProjectResult = new StringList();
            Map paramsMap = (Map) JPO.unpackArgs(args);
            StringList projectSpaceList = (StringList) paramsMap.get("projectName");
            String type = (String) paramsMap.get("type");
            String ecrId = (String) paramsMap.get("ecrId");
            StringList relateItemList = getECRItemHadFormAndTrim(context, ecrId);
            JF_LOGGER.info("relateItemList:{}", relateItemList);
//            StringList projectSpaceList = StringList.create(args[0]);
            for (String strId : projectSpaceList) {
                DomainObject projectObject = DomainObject.newInstance(context, strId);
                String strProjectName = projectObject.getInfo(context, DomainConstants.SELECT_NAME);
                Map projectPersonAndProjectRole = getProjectPersonAndProjectRole(context, strId, "PS", DomainConstants.EMPTY_STRING, DomainConstants.EMPTY_STRING);
                //该项目所有的角色列表
                MapList mapList = (MapList) projectPersonAndProjectRole.get("mapList");
                //需要按照project role分组
                Map projectRoleMap = (Map) mapList.stream().collect(Collectors.groupingBy(m -> {
                    Map map = (Map) m;
                    String projectRole = UIUtil.getValue(map, String.format(STRING_MQL_ATTRIBUTE, DomainConstants.ATTRIBUTE_PROJECT_ROLE));
                    return projectRole;
                }));
                //拿取project Role 判断是否填写完整
                // 获取 key
                // 获取 value
                StringList projectAllRoleList = new StringList();
                projectRoleMap.forEach((key, value) -> {
                    String projectRole = (String) key;
                    projectAllRoleList.add(projectRole);
                });
                JF_LOGGER.info("projectAllRoleList:{}", projectAllRoleList);
                //判断projectAllRole是否必填了
                String[] projectArrays = projectAllRoleList.toStringArray();
                //修改
                String  strProjectSpacePersonRole = JF_PublicMethodClass_mxJPO.getBasicUrl(context, new String[]{ProjectSpace_PersonRole});
                JF_LOGGER.info("type:{}", type);
                if ("JFNewECR".equalsIgnoreCase(type)) {
                    if (relateItemList.contains("U")) {
                        strProjectSpacePersonRole += "," + Foam_AME_representative;
                    }
                    if (relateItemList.contains("T")) {
                        strProjectSpacePersonRole += "," + Trim_AME_representative;
                    }
                }
                JF_LOGGER.info("strProjectSpacePersonRole:{}", strProjectSpacePersonRole);
                String[] split = strProjectSpacePersonRole.split(",");
                Set<String> baseSet = new HashSet<>(Arrays.asList(projectArrays));
                // 使用 Stream 和 Lambda 表达式筛选未包含的项
                Set<String> checkResultSet = Arrays.stream(split)
                        .map(String::trim) // 去除项前后的空白字符
                        .filter(item -> !baseSet.contains(item)) // 只保留未包含的项
                        .collect(Collectors.toSet()); // 收集结果到 Set
                JF_LOGGER.info("checkResultSet:{}", checkResultSet);
                if (checkResultSet.size() > 0) {
                    StringList checkResultStringList = StringList.create(checkResultSet);
                    resultMap.put(strProjectName, checkResultStringList.join(","));
                    checkProjectResult.add(strProjectName);
                }
                //判断人员账号是否是激活状态
                JF_LOGGER.info("判断人员账号是否是激活状态```````````````````````````````````");
                Iterator iterator = mapList.iterator();
                StringList personInActiveList = new StringList();
                while (iterator.hasNext()) {
                    Map map = (Map) iterator.next();
                    String projectRole = UIUtil.getValue(map, String.format(STRING_MQL_ATTRIBUTE, DomainConstants.ATTRIBUTE_PROJECT_ROLE));
                    if (!strProjectSpacePersonRole.contains(projectRole) ||  UIUtil.isNullOrEmpty(projectRole)) {
                        continue;
                    }
                    String strName = UIUtil.getValue(map, DomainConstants.SELECT_NAME);
                    String strCurrent = UIUtil.getValue(map, DomainConstants.SELECT_CURRENT);
                    if ("Inactive".equalsIgnoreCase(strCurrent)) {
                        personInActiveList.add(strName);
                    }
                }
                JF_LOGGER.info("personInActiveList:{}", personInActiveList);
                if (!personInActiveList.isEmpty()) {
                    personMap.put(strProjectName, personInActiveList.join(","));
                }
            }
            String alertMess = DomainConstants.EMPTY_STRING;
            if (resultMap.isEmpty()) {
                iReturn = 0;
            } else {
                //弹出提示信息
                alertMess += EnoviaResourceBundle.getProperty(context, SUITE_KEY, context.getLocale(), "emxComponents.projectRoleCheck.signTaskBefore");
                StringBuilder sb = new StringBuilder();
                resultMap.forEach((key, value) -> {
                    String projectName = (String) key;
                    sb.append(projectName).append(":");
                    String projectRole = (String) value;
                    String[] split = projectRole.split(",");
                    StringBuilder stringBuilder = new StringBuilder();
                    for (String range : split) {
                        try {
                            stringBuilder.append(EnoviaResourceBundle.getRangeI18NString(context, DomainConstants.ATTRIBUTE_PROJECT_ROLE, range, context.getSession().getLanguage()));
                            stringBuilder.append(",");
                        } catch (MatrixException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    sb.append(stringBuilder.substring(0, stringBuilder.length() - 1));
                    sb.append(";");
                });
                alertMess += sb.substring(0, sb.length() - 1).toString();

            }
            if ("JFNewECR".equalsIgnoreCase(type)) {
                if (personMap.isEmpty()) {
                    iReturn = 0;
                } else {
                    if (UIUtil.isNotNullAndNotEmpty(alertMess)) {
                        alertMess += "\n";
                    }
                    alertMess += EnoviaResourceBundle.getProperty(context, SUITE_KEY, context.getLocale(), "emxComponents.Login.ErrorIncativePersonState");
                    StringBuilder sb = new StringBuilder();
                    personMap.forEach((key, value) -> {
                        String projectName = (String) key;
                        sb.append(projectName).append(":");
                        String personName = (String) value;
                        sb.append(personName);
                        sb.append(";");
                    });
                    alertMess += sb.substring(0, sb.length() - 1).toString();
                }
            }
            JF_LOGGER.info("alertMess:{}", alertMess);
            if (UIUtil.isNotNullAndNotEmpty(alertMess)) {
                emxContextUtil_mxJPO.mqlNotice(context, alertMess);
                iReturn = 1;
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        JF_LOGGER.info("iReturn:{}", iReturn);
        return iReturn;
    }

    /**
    * 获取ecr下是否有面套和发泡
    * @param context
	* @param ecrId
    * @author LIUJR
    * @throws
    * @return matrix.util.StringList
    * @date 2025/6/6 22:20
    * @description
    */
    public static StringList getECRItemHadFormAndTrim(Context context, String ecrId) throws Exception {
        StringList relateItemList = new StringList();
        DomainObject domainObject = DomainObject.newInstance(context);
        domainObject.setId(ecrId);
        String strProjectId = domainObject.getInfo(context, String.format(STRING_MQL_RELATIONSHIP_FROM, RELATIONSHIP_JF_CHANGE_PROJECT, DomainConstants.SELECT_ID));
        DomainObject projectObject = DomainObject.newInstance(context, strProjectId);
        StringList  zeroPartList = (StringList) projectObject.getRelatedObjects(context,
                JF_PLMConstants_mxJPO.rel_JFProject2RootPart, //pattern to match relationships
                JF_PLMConstants_mxJPO.TYPE_VPMReference, //pattern to match types
                new StringList(DomainConstants.SELECT_ID), //the eMatrix StringList object that holds the list of select statement pertaining to Business Obejcts.
                new StringList(), //the eMatrix StringList object that holds the list of select statement pertaining to Relationships.
                false, //get To relationships
                true, //get From relationships
                (short) 1, //the number of levels to expand, 0 equals expand all.
                "", //where clause to apply to objects, can be empty ""
                "attribute[JFZeroPart]==Y", //where clause to apply to relationship, can be empty ""
                (short) 0//limit
        ).stream().map(m -> {
                    Map map = (Map) m;
                    return UIUtil.getValue(map, DomainConstants.SELECT_ID);
        }).collect(Collectors.toCollection(StringList::new));
        //获取ecr的自制的发泡 面套 有整椅的，那就是整椅的下一层 面套 发泡 采购类型是make生成 面套和发泡任务
        MapList relatedObjects = domainObject.getRelatedObjects(
                context,
                JF_PLMConstants_mxJPO.REL_JFECRRelateRoot,
                JF_PLMConstants_mxJPO.TYPE_VPMReference,
                StringList.create(DomainConstants.SELECT_ID, JF_PLMConstants_mxJPO.SELECT_ATTR_JF_PartType, JF_PLMConstants_mxJPO.SELECT_ATTR_JF_ProcurementType),
                new StringList(JF_PLMConstants_mxJPO.SELECT_ATTRIBUTE_JF_ECRID),
                false,
                true,
                (short) 1,
//                JF_PLMConstants_mxJPO.select_attr_JF_ISWholeChair + "==WholeChair",
//                    JF_PLMConstants_mxJPO.SELECT_ATTR_JF_ProcurementType + "==make",
                "",
                "",
                0
        );
        JF_LOGGER.info("relatedObjects:{}", relatedObjects);
        for (int i = 0; i < relatedObjects.size(); i++) {
            Map map = (Map) relatedObjects.get(i);
            String partId = UIUtil.getValue(map, DomainConstants.SELECT_ID);
            domainObject.setId(partId);
            //判断当前零件是否是整椅 如果是拿取一级件下的是否有自制件的发泡和面套
            if (zeroPartList.contains(partId)) {
                //获取整椅下的第一层的发泡和面套
                MapList partIds = domainObject.getRelatedObjects(
                        context,
                        JF_PLMConstants_mxJPO.REL_JFECRRoot2Item,
                        JF_PLMConstants_mxJPO.TYPE_VPMReference,
                        new StringList(JF_PLMConstants_mxJPO.SELECT_ATTR_JF_PartType),
                        new StringList(JF_PLMConstants_mxJPO.SELECT_ATTRIBUTE_JF_ECRID),
                        false,
                        true,
                        (short) 1,
                        JF_PLMConstants_mxJPO.SELECT_ATTR_JF_ProcurementType + "==make",
                        "",
                        0
                );
                for (int i1 = 0; i1 < partIds.size(); i1++) {
                    Map map1 = (Map) partIds.get(i1);
                    String type = (String) map1.get(JF_PLMConstants_mxJPO.SELECT_ATTR_JF_PartType);
                    String partEcrId = (String) map1.get(JF_PLMConstants_mxJPO.SELECT_ATTRIBUTE_JF_ECRID);
                    if (!partEcrId.contains(ecrId)) {
                        continue;
                    }
                    //判断类型
                    relateItemList.add(type);
                }
            } else {
                //如果是一级件 就不拿取一级件判断当前的件是不是自制的发泡面套件
                String type = (String) map.get(JF_PLMConstants_mxJPO.SELECT_ATTR_JF_PartType);
                String procurementType = (String) map.get(JF_PLMConstants_mxJPO.SELECT_ATTR_JF_ProcurementType);
                if (!"make".equalsIgnoreCase(procurementType)) {
                    continue;
                }
                relateItemList.add(type);
            }
        }
        return relateItemList;
    }

    /**
     * 生成APR会签任务
     * @param context
     * @param args
     * @author LIUJR
     * @throws
     * @return void
     * @date 2024/7/31 9:28
     * @description
     */
    @ProgramCallable
    public void JFECRCountersignEndGenerateAPRTask(Context context, String[] args) throws Exception {
        try {
            JF_LOGGER.info("#################JFECRCountersignEndGenerateAPRTask#######################");
            //ECR id
            String strECRId = args[0];
            DomainObject domainObject = DomainObject.newInstance(context, strECRId);
            StringList signTaskList = domainObject.getInfoList(context, "from[" + RELATIONSHIP_JF_ECR_TASK + "|to.type=='" + TYPE_JS_APR_TASK_TYPE + "'].to.id");
            if (signTaskList.size() > 0) {
                //如果已经生成任务 不再次生成！
                return;
            }
            createECRAPRTaskOrCustomerTask(context, domainObject, strECRId, "Financial BP", "signTask-BP-APR", TYPE_JS_APR_TASK);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }


    /**
    * 生成客户报价任务
    * @param context
	* @param args
    * @author LIUJR
    * @throws
    * @return void
    * @date 2024/8/8 15:29
    * @description
    */
    @ProgramCallable
    public void JFECRAPREndGenerateCustomerTask(Context context, String[] args) throws Exception {
        try {
            JF_LOGGER.info("#################JFECRAPREndGenerateCustomerTask#######################");
            //ECR id
            String strECRId = args[0];
            DomainObject domainObject = DomainObject.newInstance(context, strECRId);
            //拿取ecr的内外部变更
            String attributeValue = domainObject.getAttributeValue(context, ATTRIBUTE_CHANGE_SOURCE);
            //如果是内部变更 提交到已完成
            JF_LOGGER.info("stata :{}", domainObject.getInfo(context,DomainConstants.SELECT_CURRENT));
            if ("Internal Changes".equalsIgnoreCase(attributeValue)) {
                //提交到完成
                ContextUtil.pushContext(context);
                domainObject.promote(context);
                ContextUtil.popContext(context);
                return;
            }
            //如果不是  创建任务
            StringList signTaskList = domainObject.getInfoList(context, "from[" + RELATIONSHIP_JF_ECR_TASK + "|to.type=='" + TYPE_JS_CUSTOMER_TASK_TYPE + "'].to.id");
            if (signTaskList.size() > 0) {
                //如果已经生成任务 不再次生成！
                return;
            }
            createECRAPRTaskOrCustomerTask(context, domainObject, strECRId, "Business manager", "customerTask", TYPE_JS_CUSTOMER_TASK);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    /**
    * 获取ecr的相关任务
    * @param context
	* @param domainObject
    * @author LIUJR
    * @throws
    * @return com.matrixone.apps.domain.util.MapList
    * @date 2024/8/8 16:57
    * @description
    */
    public MapList getECRTask(Context context, DomainObject domainObject) throws Exception{
        MapList mapList = domainObject.getRelatedObjects(context,
                RELATIONSHIP_JF_ECR_TASK,
                TYPE_JS_SIGN_TASK_TYPE,
                strBusSelectList,
                strRelSelectList,
                false,
                true,
                (short) 1,
                "",
                "",
                0);
        return mapList;
    }

    /**
    * 创建ecr的报价或者APR任务
    * @param context
	* @param domainObject
	* @param strECRId
	* @param where
    * @author LIUJR
    * @throws
    * @return void
    * @date 2024/8/8 16:58
    * @description
    */
    public void createECRAPRTaskOrCustomerTask(Context context, DomainObject domainObject, String strECRId, String where, String taskXmlId, String taskType) throws Exception{
        String strPush = "N";
        try {
            String strProjectId = domainObject.getInfo(context, String.format(STRING_MQL_RELATIONSHIP_FROM, RELATIONSHIP_JF_CHANGE_PROJECT, DomainConstants.SELECT_ID));
            //拿取项目成员中的人员的project role
            String strRelWhere = String.format(STRING_MQL_ATTRIBUTE, DomainConstants.ATTRIBUTE_PROJECT_ROLE) + "== '" + where +"'";
            Map projectPersonAndProjectRole = getProjectPersonAndProjectRole(context, strProjectId, "NA", DomainConstants.EMPTY_STRING,strRelWhere);
            //该项目所有的角色列表
            StringList projectRoleList = (StringList) projectPersonAndProjectRole.get("projectRoleList");
            //该项目角色列表对应的人员
            Map personAndRoleMap = (Map) projectPersonAndProjectRole.get("personAndRoleMap");
            JF_LOGGER.info("personAndRoleMap:{}", personAndRoleMap.toString());
            JF_LOGGER.info("projectRoleList:{}", projectRoleList.toString());

            //读取配置文件
            MapList mapList = readSignTaskPropertiesFile(context, STRING_SIGN_TASK_PROPERTIES_ZH, taskXmlId);
            JF_LOGGER.info("mapList:{}", mapList.toString());
            //工具类
            //创建任务数据
            MapList requestMap = new MapList();
            JF_Util_mxJPO jfUtilMxJPO = new JF_Util_mxJPO();
            Iterator iterator = mapList.iterator();
            while (iterator.hasNext()) {
                Map map = (Map) iterator.next();
                String name = UIUtil.getValue(map, DomainConstants.SELECT_NAME);
                String role = UIUtil.getValue(map, "role");
                //匹配人员
                if (UIUtil.isNotNullAndNotEmpty(role) && projectRoleList.contains(role)) {
                    //包含人
                    String person = UIUtil.getValue(personAndRoleMap, role);
                    //构造参数
                    HashMap<String, String> paramsMap = new HashMap<>();
                    paramsMap.put("taskName", name);
                    paramsMap.put("taskType", taskType);
                    paramsMap.put("taskOwner", person);
                    paramsMap.put("parentId", strECRId);
                    paramsMap.put("role", role);
                    requestMap.add(paramsMap);
                }
            }
            ContextUtil.startTransaction(context, true);
            JF_LOGGER.info("requestMap:{}", requestMap.toString());
            Iterator iterator1 = requestMap.iterator();
            ContextUtil.pushContext(context);
            strPush = "Y";
            while (iterator1.hasNext()) {
                Map paramsMap = (Map) iterator1.next();
                JF_LOGGER.info("paramsMap:{}", paramsMap.toString());
                String taskId = jfUtilMxJPO.createTask(context, JPO.packArgs(paramsMap));
                //提升到工作中
                DomainObject signTask = DomainObject.newInstance(context, taskId);
//                signTask.promote(context);
                MqlUtil.mqlCommand(context, true, "mod bus "+taskId+" current Active", false);//去掉触发发邮件
                //关联ecr
                DomainRelationship domainRelationship = domainObject.addToObject(context, new RelationshipType(RELATIONSHIP_JF_ECR_TASK), taskId);
                projectTaskToActiveSendEmail(context, new String[]{taskId});
                domainRelationship.setAttributeValue(context, DomainConstants.ATTRIBUTE_PROJECT_ROLE, UIUtil.getValue(paramsMap, "role") );
            }
            ContextUtil.commitTransaction(context);
        }catch (Exception e) {
            e.printStackTrace();
            ContextUtil.abortTransaction(context);
            throw new RuntimeException(e);
        }finally {
            if ("Y".equalsIgnoreCase(strPush)) {
                ContextUtil.popContext(context);
            }
        }
    }

    /*
     * @description:项目任务(ECR会签、ECO执行任务、DA执行计划)到工作中状态的时候发邮件给分配人，如果没有分配人就发给owner
     * @author: caipan
     * @date: 2025/4/8 14:18:38
     * @param: * @param[1] context
     * @param[2] args
     * @return:
     **/
    public static  void projectTaskToActiveSendEmail(Context context,String[] args) throws Exception {
        JF_LOGGER.info("projectTaskToActiveSendEmail start");
        String id = args[0];
        DomainObject taskObj = DomainObject.newInstance(context, id);
        StringList includeList = new StringList();
        includeList.add("JF_DATask");
        includeList.add("JF_APRTask");
        includeList.add("JF_CustomerTask");
        includeList.add("JF_SignTask");
        includeList.add("JF_ECOTask");
        includeList.add("JF_PCRTask");
        includeList.add("JF_PCRExecuteTask");
        includeList.add("JF_PCRVerificationTask");
        includeList.add("JF_ProductConfigTask");
        StringList assignList = taskObj.getInfoList(context, "to[Assigned Tasks].from.name");
       Map basiMap = taskObj.getInfo(context,strBusSelectList);
        String owner = UIUtil.getValue(basiMap, DomainConstants.SELECT_OWNER);
        String type = UIUtil.getValue(basiMap, DomainConstants.SELECT_TYPE);
        String name = UIUtil.getValue(basiMap, DomainConstants.SELECT_NAME);
        if (includeList.contains(type)) {
            if (assignList.size() > 0) {
                owner = assignList.get(0);
            }
           String email =  JF_NotificationUtils_mxJPO.getPersonEmail(context, owner, null);
            String rel = "JFCO2ECOTask,JFECR2Task,JFDA2JFDATask,JF_PCR2Task";
            MapList mapList = taskObj.getRelatedObjects(context,
                    rel,
                    DomainConstants.QUERY_WILDCARD,
                    strBusSelectList,
                    strRelSelectList,
                    true,
                    false,
                    (short) 1,
                    "",
                    "",
                    1);
            //JF_LOGGER.info("mapList:{}",mapList);
            Map requestMap = new HashMap();
            for (int i = 0;i<mapList.size();i++ ){
                Map temp =(Map) mapList.get(0);
                requestMap.put("objectId",UIUtil.getValue(temp, DomainConstants.SELECT_ID));
                requestMap.put("taskName",name);
                requestMap.put("connectName",UIUtil.getValue(temp, DomainConstants.SELECT_NAME));
                requestMap.put("email",email);
            JF_SendEmailUtils_mxJPO.sendProjectTaskEmailToPortal(context, requestMap);//发邮件
            }

        }
    }

    /*
     * @description:ECR会签任务APR拒绝到会签人的时候发邮件给分配人，如果没有分配人就发给owner
     * @author: caipan
     * @date: 2025/4/8 14:18:38
     * @param: * @param[1] context
     * @param[2] args[0]  会签任务ID
     * @return:
     **/
    public static  void projectTaskRejectSendEmail(Context context,String[] args) throws Exception {
        JF_LOGGER.info("projectTaskRejectSendEmail start");
        String id = args[0];
        DomainObject taskObj = DomainObject.newInstance(context, id);
        StringList includeList = new StringList();
//        includeList.add("JF_DATask");
//        includeList.add("JF_APRTask");
//        includeList.add("JF_CustomerTask");
        includeList.add("JF_SignTask");
//        includeList.add("JF_ECOTask");
        StringList assignList = taskObj.getInfoList(context, "to[Assigned Tasks].from.name");
        Map basiMap = taskObj.getInfo(context,strBusSelectList);
        String owner = UIUtil.getValue(basiMap, DomainConstants.SELECT_OWNER);
        String type = UIUtil.getValue(basiMap, DomainConstants.SELECT_TYPE);
        String name = UIUtil.getValue(basiMap, DomainConstants.SELECT_NAME);
        if (includeList.contains(type)) {
            if (assignList.size() > 0) {
                owner = assignList.get(0);
            }
            String email =  JF_NotificationUtils_mxJPO.getPersonEmail(context, owner, null);
//            email="caip@tecwin.com";
//            String rel = "JFCO2ECOTask,JFECR2Task,JFDA2JFDATask";
            String rel = "JFECR2Task";
            MapList mapList = taskObj.getRelatedObjects(context,
                    rel,
                    DomainConstants.QUERY_WILDCARD,
                    strBusSelectList,
                    strRelSelectList,
                    true,
                    false,
                    (short) 1,
                    "",
                    "",
                    1);
            //JF_LOGGER.info("mapList:{}",mapList);
            Map requestMap = new HashMap();
            for (int i = 0;i<mapList.size();i++ ){
                Map temp =(Map) mapList.get(0);
                requestMap.put("objectId",UIUtil.getValue(temp, DomainConstants.SELECT_ID));
                requestMap.put("taskName",name);
                requestMap.put("connectName",UIUtil.getValue(temp, DomainConstants.SELECT_NAME));
                requestMap.put("email",email);
                JF_SendEmailUtils_mxJPO.sendProjectTasRejectEmailToPortal(context, requestMap);//发邮件
            }

        }
    }
}
