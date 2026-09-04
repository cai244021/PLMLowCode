import com.dassault_systemes.enovia.e6wv2.foundation.ServiceBase;
import com.dassault_systemes.enovia.e6wv2.foundation.jaxb.Dataobject;
import com.dassault_systemes.enovia.e6wv2.foundation.jaxb.ServiceParameters;
import com.dassault_systemes.enovia.e6wv2.foundation.jaxb.Servicedata;
import com.dassault_systemes.enovia.e6wv2.foundation.jaxb.UpdateActions;
import com.dassault_systemes.enovia.e6wv2.foundation.jaxbext.ArgMap;
import com.dassault_systemes.enovia.e6wv2.foundation.jaxbext.DataelementMapAdapter;
import com.dassault_systemes.enovia.e6wv2.foundation.jaxbext.RelateddataMapAdapter;
import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.DomainRelationship;
import com.matrixone.apps.domain.util.*;
import com.matrixone.apps.framework.ui.PostProcessCallable;
import com.matrixone.apps.framework.ui.UIUtil;
import com.matrixone.apps.program.ProgramCentralConstants;
import com.matrixone.apps.program.ProgramCentralUtil;
import com.matrixone.apps.program.Question;
import matrix.db.AttributeList;
import matrix.db.Context;
import matrix.db.JPO;
import matrix.db.RelationshipType;
import matrix.util.StringList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static com.matrixone.apps.domain.DomainConstants.*;
import static com.matrixone.apps.domain.DomainConstants.TYPE_TASK;

/**
 * @ClassName JF_TaskUtils_mxJPO
 * @Author: LIUJR
 * @CreateDate: 23/06/2025 14:33
 * @UpdateRemark:
 * @Version: 1.0
 * @Description:   创建项目任务的工具方法类
 */
public class JF_ProjectTaskUtils_mxJPO {
    private static final Logger JF_LOGGER = LoggerFactory.getLogger(JF_ProjectTaskUtils_mxJPO.class);

    /**
    * 创建项目任务 挂到任务下面
    * @param context
	* @param objectId  挂接到的对象
	* @param projectMap
	* @param type 新建任务类型
	* @param addLevel  子级 平级 还是上一级
    * @author LIUJR
    * @throws
    * @return java.lang.String
    * @date 23/06/2025 14:34
    * @description
    */
    public static  String createTask(Context context, String objectId, Map projectMap, String type, String addLevel) throws Exception{
        Map basicTaskInfoMap = new HashMap();
        basicTaskInfoMap.put("ParentId", UIUtil.getValue(projectMap, DomainConstants.SELECT_ID));//挂接到哪个下面的上一级
        basicTaskInfoMap.put("AutoName","false");//是否自动命名
        basicTaskInfoMap.put("name",projectMap.get("taskName"));//是否自动命名
        basicTaskInfoMap.put("HowMany","1");//创建数量
        basicTaskInfoMap.put("AddTask",addLevel);//addTaskBelow 子任务 addTaskAbove 上一级 平级任务
        basicTaskInfoMap.put("selectedObjectId",objectId);//挂接到哪个下面
        basicTaskInfoMap.put("description",UIUtil.getValue(projectMap, "description"));//description
        basicTaskInfoMap.put("type",type);//type 类型Task  Phase阶段
        basicTaskInfoMap.put("policy","Project Task");//policy 类型Project Task
        Map taskAttributeMap = new HashMap();
        taskAttributeMap.put("DeliverablesInheritance", "Yes");
        taskAttributeMap.put("EstimatedStartDate", "");//计划开始时间  11/3/2023  2023年12月28日 11/3/2023 12:00:00 AM
        taskAttributeMap.put("EstimatedEndDate", "");//计划结束时间
        taskAttributeMap.put("DurationUnit", "d");
        taskAttributeMap.put("TaskRequirement", "Optional");
        taskAttributeMap.put("NeedsReview", "Yes");
        taskAttributeMap.put("Project Role", "");
        taskAttributeMap.put("TaskConstraintType", "Finish No Earlier Than");
        taskAttributeMap.put("Duration", "1");//持续时间
        taskAttributeMap.put("TaskConstraintDate", "");//持续时间
        Map relatedInfoMap = new HashMap();
        if (projectMap.containsKey("personId")) {
            String personId = UIUtil.getValue(projectMap, "personId");
            relatedInfoMap.put("Assignee", personId);//分配人员ID 22874.14184.63457.52720|22874.14184.5029.30504
        }
        relatedInfoMap.put("isExperimentAssignedTasksRelFlag", "false");
//        relatedInfoMap.put("Calendar", getCalendar(context, "", true));//日历ID 暂时为空
        relatedInfoMap.put("deliverableId", "");//
        Map requestMap = new HashMap();
        requestMap.put("localeObj", context.getLocale());
        com.matrixone.apps.program.Task task =  new com.matrixone.apps.program.Task();
        task.createTask(context,
                basicTaskInfoMap,
                taskAttributeMap,
                relatedInfoMap,
                requestMap);
        String taskId = task.getObjectId(context);
//        com.matrixone.apps.program.Task programTask = new com.matrixone.apps.program.Task();
//        programTask.setId(taskId);
//        Date today = new Date();
//        programTask.updateStartDate(context, today);
        return task.getObjectId(context);
    }


    /**
     * 复制任务
     * @param context
     * @param firstESOTaskId
     * @param noESOTaskList
     * @author LIUJR
     * @throws
     * @return void
     * @date 24/06/2025 10:49
     * @description
     */
    public static void duplicateFilterESOTask(Context context, String firstESOTaskId, StringList noESOTaskList) throws Exception{
        try {
            if (noESOTaskList.isEmpty()) {
                return;
            }
            StringBuffer stringBuffer = new StringBuffer();
            for (int i = 0; i < noESOTaskList.size(); i++) {
                String noESOTaskId = noESOTaskList.get(i);
                if (i != 0) {
                    stringBuffer.append("|").append(noESOTaskId);
                } else {
                    stringBuffer.append(noESOTaskId);
                }
            }
            String noESOTaskIds = stringBuffer.toString();
            copyTask(context, new String[]{firstESOTaskId, noESOTaskIds});
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
    * 复制任务到某一个项目下面
    * @param context
	* @param args
    * @author LIUJR
    * @throws
    * @return void
    * @date 24/06/2025 11:03
    * @description
    */
    public  static void copyTask(Context context, String[] args) throws Exception {
        try{
            Map paramMap = new HashMap(1);
            paramMap.put("objectId", args[0]);//挂在那个下面 选择的节点
            paramMap.put("SeachProjectOID", args[1]);//复制来源对象ID 选择的任务ID  | 分隔
            String[] methodArgs = JPO.packArgs(paramMap);
            //todo
            String[] result = JPO.invoke(context, "emxProjectSpace", null, "copyPartialScheduleProcessTask", methodArgs, String[].class);
        }catch (Exception e){
            throw e;
        }
    }

    /**
    * WBS修改按钮的权限   仅限项目经理  整椅子经理  ESO审核员
    * @param context
	* @param args
    * @author LIUJR
    * @throws
    * @return java.lang.Boolean
    * @date 11/07/2025 13:20
    * @description
    */
    public static Boolean editProjectTaskAccess(Context context, String[] args) throws Exception {
        JF_LOGGER.info("!!!!!!!!!!!!!1");
        Boolean flag = Boolean.FALSE;
        HashMap map = (HashMap) JPO.unpackArgs(args);
        String objectId = (String) map.get("objectId");
        DomainObject domainObject = DomainObject.newInstance(context, objectId);
        StringList stringList = new StringList();
        stringList.add("to[" + DomainConstants.RELATIONSHIP_PROJECT_ACCESS_KEY + "].from.from[" + DomainConstants.RELATIONSHIP_PROJECT_ACCESS_LIST + "].to.current");
        stringList.add(DomainConstants.SELECT_CURRENT);
        stringList.add(DomainConstants.SELECT_TYPE);
        JF_PublicMethodClass_mxJPO publicMethodClassMxJPO = new JF_PublicMethodClass_mxJPO();
        try {
            Map var9 = domainObject.getInfo(context, stringList);
            String type = (String) var9.get(DomainConstants.SELECT_TYPE);
            String projectId = objectId;
            if (!DomainConstants.TYPE_PROJECT_SPACE.equalsIgnoreCase(type)) {
                projectId = (String) var9.get("to[" + DomainConstants.RELATIONSHIP_PROJECT_ACCESS_KEY + "].from.from[" + DomainConstants.RELATIONSHIP_PROJECT_ACCESS_LIST + "].to.current");
            }
            JF_LOGGER.info("projectId:{}", projectId);
            if (UIUtil.isNotNullAndNotEmpty(projectId)) {
                HashMap<String, String> map1 = new HashMap<>();
                map1.put("objectId", projectId);
                Boolean esoAccess = publicMethodClassMxJPO.createESOAccess(context, JPO.packArgs(map1));
                JF_LOGGER.info("esoAccess:{}", esoAccess);
                if (esoAccess) {
                    flag = Boolean.TRUE;
                } else {
                    //判断是否有JFESOAdmin权限
                    Vector assignments = PersonUtil.getAssignments(context, context.getUser());
                    if (assignments.contains("JfESOAdmin")) {
                        flag = Boolean.TRUE;
                    }
                }
            }
        }catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
        JF_LOGGER.info("!!!!!!!!!!!!flag:{}", flag);
        return flag;
    }


    /**
     * 创建和复制ESO按钮 整椅子经理  ESO审核员
     * @param context
     * @param args
     * @author LIUJR
     * @throws
     * @return java.lang.Boolean
     * @date 11/07/2025 13:20
     * @description
     */
    public static Boolean createDuplicateESOTaskAccess(Context context, String[] args) throws Exception {
        JF_LOGGER.info("!!!!!!!!!!!!!1");
        Boolean flag = Boolean.FALSE;
        HashMap map = (HashMap) JPO.unpackArgs(args);
        String objectId = (String) map.get("objectId");
        DomainObject domainObject = DomainObject.newInstance(context, objectId);
        StringList stringList = new StringList();
        stringList.add("to[" + DomainConstants.RELATIONSHIP_PROJECT_ACCESS_KEY + "].from.from[" + DomainConstants.RELATIONSHIP_PROJECT_ACCESS_LIST + "].to.id");
        stringList.add(DomainConstants.SELECT_CURRENT);
        stringList.add(DomainConstants.SELECT_TYPE);
        stringList.add(SELECT_NAME);
        JF_PublicMethodClass_mxJPO publicMethodClassMxJPO = new JF_PublicMethodClass_mxJPO();
        try {
            Map var9 = domainObject.getInfo(context, stringList);
            String type = (String) var9.get(DomainConstants.SELECT_TYPE);
            String name = (String) var9.get(DomainConstants.SELECT_NAME);
            JF_LOGGER.info("type:{}", type);
            JF_LOGGER.info("name:{}", name);
            if (TYPE_PROJECT_SPACE.equalsIgnoreCase(type) || JF_PLMConstants_mxJPO.TYPE_JF_ESOTask.equalsIgnoreCase(type) ||
                    (TYPE_TASK.equalsIgnoreCase(type) && name.contains("ESO"))) {
                String projectId = objectId;
                if (!DomainConstants.TYPE_PROJECT_SPACE.equalsIgnoreCase(type)) {
                    JF_LOGGER.info("projectId:{}", projectId);
                    projectId = (String) var9.get("to[" + DomainConstants.RELATIONSHIP_PROJECT_ACCESS_KEY + "].from.from[" + DomainConstants.RELATIONSHIP_PROJECT_ACCESS_LIST + "].to.id");
                }
                JF_LOGGER.info("projectId:{}", projectId);
                if (UIUtil.isNotNullAndNotEmpty(projectId)) {
                    HashMap<String, String> map1 = new HashMap<>();
                    map1.put("objectId", projectId);
                    Boolean esoAccess = publicMethodClassMxJPO.createESOAccess(context, JPO.packArgs(map1));
                    JF_LOGGER.info("esoAccess:{}", esoAccess);
                    if (esoAccess) {
                        flag = Boolean.TRUE;
                    } else {
                        //判断是否有JFESOAdmin权限
                        Vector assignments = PersonUtil.getAssignments(context, context.getUser());
                        if (assignments.contains("JfESOAdmin")) {
                            flag = Boolean.TRUE;
                        }
                    }
                }
            }
        }catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
        JF_LOGGER.info("!!!!!!!!!!!!flag:{}", flag);
        return flag;
    }
}
