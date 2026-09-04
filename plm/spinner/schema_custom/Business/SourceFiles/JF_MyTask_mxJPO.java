import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.util.*;
import com.matrixone.apps.framework.ui.ProgramCallable;
import com.matrixone.apps.framework.ui.UIUtil;
import com.matrixone.apps.program.ProgramCentralConstants;
import matrix.db.*;
import matrix.util.List;
import matrix.util.StringList;
import com.matrixone.apps.program.Task;
import org.apache.commons.text.StringEscapeUtils;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;


public class JF_MyTask_mxJPO implements DomainConstants{

    private static final String sAttrReviewTask = PropertyUtil.getSchemaProperty(context1, "attribute_ReviewTask");
    private static final String sAttrReviewersComments = PropertyUtil.getSchemaProperty(context1, "attribute_ReviewersComments");
    public static final String STATE_PROJECT_TASK_COMPLETE = PropertyUtil.getSchemaProperty("policy", POLICY_PROJECT_TASK, "state_Complete");
    public static final String STATE_PROJECT_TASK_CREATE = PropertyUtil.getSchemaProperty("policy", POLICY_PROJECT_TASK, "state_Create");
    public static final String STATE_PROJECT_TASK_ARCHIVE = PropertyUtil.getSchemaProperty("policy", POLICY_PROJECT_SPACE, "state_Archive");

    private static final String STRING_MQL_RELATIONSHIP_FROM = "to[%s].from.%s";

    /**
     * 返回所有项目任务
     * @param context
     * @param args
     * @return
     * @throws FrameworkException
     */
    public MapList getAllProjectTask(Context context, String[] args) throws Exception {
        MapList mapList = new MapList();
        String user = context.getUser();
        StringList objectSelects = basicBolistSel();
        String strConfigTaskType = JF_PublicMethodClass_mxJPO.getBasicUrl(context, new String[]{"Notification.Configuration.TaskType"});
        objectSelects.add("attribute[Task Actual Finish Date]");
        objectSelects.add("attribute[JF_DAActualFinishTime]");
        mapList = DomainObject.findObjects(context, strConfigTaskType, "*", "owner == " + user, objectSelects);
        return mapList;
    }

    /**
     * 返回活动中项目任务
     * @param context
     * @param args
     * @return
     * @throws FrameworkException
     */
    public MapList getActiveProjectTask(Context context, String[] args) throws Exception {
        MapList mapList = new MapList();
        String user = context.getUser();
        StringList objectSelects = basicBolistSel();
        String strConfigTaskType = JF_PublicMethodClass_mxJPO.getBasicUrl(context, new String[]{"Notification.Configuration.TaskType"});
        objectSelects.add("attribute[Task Actual Finish Date]");
        objectSelects.add("attribute[JF_DAActualFinishTime]");
        objectSelects.add("attribute[Title]");
        mapList = DomainObject.findObjects(context, strConfigTaskType, "*", "(current==Assign||current==Active)&&owner==" + user, objectSelects);
        return mapList;
    }


    /**
     * 返回项目任务关联数据
     * @param context
     * @param args
     */
    public StringList getAssociatedData(Context context, String[] args) throws Exception {
        StringList strings = new StringList();
        Map paramsMap = (Map) JPO.unpackArgs(args);
        MapList objectList = (MapList)paramsMap.get("objectList");
        Map paramList = (Map) paramsMap.get("paramList");
        String reportFormat = (String)paramList.get("reportFormat");
        Iterator iterator = objectList.iterator();
        while (iterator.hasNext()){
            Map map = (Map) iterator.next();
            String id = (String) map.get("id");
            String type = (String) map.get("type");
            DomainObject dr = DomainObject.newInstance(context,id);
            String strRelName = DomainConstants.EMPTY_STRING;
            switch (type) {
                case "JF_DATask" : {
                    strRelName = "JFDA2JFDATask";
                    break;
                }
                case "JF_ECOTask" : {
                    strRelName = "JFCO2ECOTask";
                    break;
                }
                case "JF_APRTask" :
                case "JF_CustomerTask" :
                case "JF_SignTask" : {
                    strRelName = "JFECR2Task";
                    break;
                }
                case "Task" : {
                    strRelName = "Subtask";
                    break;
                }
            }
            if (UIUtil.isNullOrEmpty(strRelName))  {
                strings.add("-");
                continue;
            }
            StringList strBusSelectList = new StringList();
            strBusSelectList.add(String.format(STRING_MQL_RELATIONSHIP_FROM, strRelName, DomainConstants.SELECT_ID));
            strBusSelectList.add(String.format(STRING_MQL_RELATIONSHIP_FROM, strRelName, DomainConstants.SELECT_NAME));
            strBusSelectList.add(String.format(STRING_MQL_RELATIONSHIP_FROM, strRelName, DomainConstants.SELECT_TYPE));
            strBusSelectList.add(String.format(STRING_MQL_RELATIONSHIP_FROM, strRelName, DomainConstants.SELECT_DESCRIPTION));
            strBusSelectList.add(String.format(STRING_MQL_RELATIONSHIP_FROM, strRelName, DomainConstants.SELECT_ATTRIBUTE_TITLE));
            Map infoMap = dr.getInfo(context, strBusSelectList);
            StringBuffer stringBuffer = new StringBuffer();
            String strId = UIUtil.getValue(infoMap, String.format(STRING_MQL_RELATIONSHIP_FROM, strRelName, DomainConstants.SELECT_ID));
            String strName = UIUtil.getValue(infoMap, String.format(STRING_MQL_RELATIONSHIP_FROM, strRelName, DomainConstants.SELECT_NAME));
            String strDesc = UIUtil.getValue(infoMap, String.format(STRING_MQL_RELATIONSHIP_FROM, strRelName, DomainConstants.SELECT_DESCRIPTION));
            String strType = UIUtil.getValue(infoMap, String.format(STRING_MQL_RELATIONSHIP_FROM, strRelName, DomainConstants.SELECT_TYPE));
            String strTitle = UIUtil.getValue(infoMap, String.format(STRING_MQL_RELATIONSHIP_FROM, strRelName, DomainConstants.SELECT_ATTRIBUTE_TITLE));
            if (UIUtil.isNotNullAndNotEmpty(strId)) {
                String strViewName = DomainConstants.EMPTY_STRING;
                if (DomainConstants.TYPE_PROJECT_SPACE.equalsIgnoreCase(strType)) {
                    strViewName = UIUtil.isNotNullAndNotEmpty(strDesc) ? strDesc : UIUtil.isNotNullAndNotEmpty(strTitle) ? strTitle : strName;
                } else {
                    strViewName = UIUtil.isNotNullAndNotEmpty(strTitle) ? strTitle : strName;
                }
                String strShowTitle = strViewName.equalsIgnoreCase(strName) ? strViewName : strName + " / " + strViewName;
                strShowTitle =encode(strShowTitle);
                if(reportFormat!=null && !reportFormat.equals("")){
                    stringBuffer.append(strShowTitle);
                }else {

                    stringBuffer.append("<table><td><a href=\"/3dspace/common/emxNavigator.jsp?objectId=");
                    stringBuffer.append(strId).append("\" target=\"_blank>\">");
                    stringBuffer.append(strShowTitle);
                    stringBuffer.append("</a></td></table>");
                }
            } else {
                stringBuffer.append("");
            }
            strings.add(stringBuffer.toString());
        }
        return strings;
    }

    public StringList basicBolistSel(){
        StringList boSel = new StringList();
        boSel.add(DomainConstants.SELECT_ID);
        boSel.add(DomainConstants.SELECT_NAME);
        boSel.add(DomainConstants.SELECT_TYPE);
        boSel.add(DomainConstants.SELECT_REVISION);
        boSel.add(DomainConstants.SELECT_CURRENT);
        boSel.add(DomainConstants.SELECT_OWNER);
        return boSel;
    }

    /**
     * 返回审核任务关系from[Route Task].to.to[Object Route].from.name
     * @param context
     * @param args
     */
    public StringList getRelationshipAssociated(Context context, String[] args) throws Exception {

        StringList strings = new StringList();
        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        Map paramList = (Map) programMap.get("paramList");
        String reportFormat = (String)paramList.get("reportFormat");
        StringList strObjectIdList = getObjectIdList(args);
        String strRel = "from[Route Task].to.to[Object Route].from.";
        StringList strBusSelectList = new StringList();
        strBusSelectList.add(strRel + DomainConstants.SELECT_ID);
        strBusSelectList.add(strRel + DomainConstants.SELECT_NAME);
        strBusSelectList.add(strRel + DomainConstants.SELECT_TYPE);
        strBusSelectList.add(strRel + "attribute[Synopsis]");
        strBusSelectList.add(strRel + DomainConstants.SELECT_ATTRIBUTE_TITLE);

        for (String strObjectId : strObjectIdList) {
            StringBuffer stringBuffer = new StringBuffer();
            DomainObject dr = DomainObject.newInstance(context,strObjectId);
            Map infoMap = dr.getInfo(context, strBusSelectList);
            String strId = UIUtil.getValue(infoMap,   strRel +   DomainConstants.SELECT_ID);
            String strName = UIUtil.getValue(infoMap, strRel +   DomainConstants.SELECT_NAME);
            String strType = UIUtil.getValue(infoMap, strRel +   DomainConstants.SELECT_TYPE);
            String strTitle = UIUtil.getValue(infoMap,strRel +   DomainConstants.SELECT_ATTRIBUTE_TITLE);
            String strSynopsis = UIUtil.getValue(infoMap,strRel +   "attribute[Synopsis]");
            if ("JF_DRW".equalsIgnoreCase(strType)) {

                String strShowTitle = UIUtil.isNullOrEmpty(strSynopsis)||strSynopsis.equalsIgnoreCase(strName) ? strName : strName + " / " + strSynopsis;
                strShowTitle =  encode(strShowTitle);
                if(reportFormat!=null && !reportFormat.equals("")){
                    stringBuffer.append(strShowTitle);
                }else {
                    strShowTitle = StringEscapeUtils.escapeHtml4(strShowTitle);
                    stringBuffer.append("<table><td><a href=\"/3dspace/common/emxNavigator.jsp?objectId=");
                    stringBuffer.append(strId).append("\" target=\"_blank>\">");
                    stringBuffer.append(strName);
                    stringBuffer.append("</a></td></table>");
                }
            } else {
                String strShowTitle = UIUtil.isNullOrEmpty(strTitle)||strTitle.equalsIgnoreCase(strName) ? strName : strName + " / " + strTitle;
                strShowTitle =  encode(strShowTitle);
                if(reportFormat!=null && !reportFormat.equals("")){
                    stringBuffer.append(strShowTitle);
                }else {
                    stringBuffer.append("<table><td><a href=\"/3dspace/common/emxNavigator.jsp?objectId=");
                    stringBuffer.append(strId).append("\" target=\"_blank>\">");
                    strShowTitle = StringEscapeUtils.escapeHtml4(strShowTitle);
                    stringBuffer.append(strName);
                    stringBuffer.append("</a></td></table>");
                }
            }
            strings.add(stringBuffer.toString());
        }
        return strings;
    }
    /*
     * @description:显示关联数据的Title
     * @author: caipan
     * @date: 2025/3/14 14:01:30
     * @param: * @param[1] context
     * @param[2] args
     * @return:
     **/
    public StringList getRelationshipAssociatedTitle(Context context, String[] args) throws Exception {

        StringList strings = new StringList();
        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        Map paramList = (Map) programMap.get("paramList");
        String reportFormat = (String)paramList.get("reportFormat");
        StringList strObjectIdList = getObjectIdList(args);
        String strRel = "from[Route Task].to.to[Object Route].from.";
        StringList strBusSelectList = new StringList();
        strBusSelectList.add(strRel + DomainConstants.SELECT_ID);
        strBusSelectList.add(strRel + DomainConstants.SELECT_NAME);
        strBusSelectList.add(strRel + DomainConstants.SELECT_TYPE);
        strBusSelectList.add(strRel + "attribute[Synopsis]");
        strBusSelectList.add(strRel + DomainConstants.SELECT_ATTRIBUTE_TITLE);

        for (String strObjectId : strObjectIdList) {
            StringBuffer stringBuffer = new StringBuffer();
            DomainObject dr = DomainObject.newInstance(context,strObjectId);
            Map infoMap = dr.getInfo(context, strBusSelectList);
            String strType = UIUtil.getValue(infoMap, strRel +   DomainConstants.SELECT_TYPE);
            String strTitle = UIUtil.getValue(infoMap,strRel +   DomainConstants.SELECT_ATTRIBUTE_TITLE);
            String strSynopsis = UIUtil.getValue(infoMap,strRel +   "attribute[Synopsis]");
            if ("JF_DRW".equalsIgnoreCase(strType)) {
                stringBuffer.append(strSynopsis);
            } else if ("JFESOReview".equalsIgnoreCase(strType)){
                String strId = UIUtil.getValue(infoMap, strRel +   DomainConstants.SELECT_ID);
                DomainObject object= DomainObject.newInstance(context,strId);
                stringBuffer.append(object.getInfo(context,"to[JFESOTask2ESOReview].from.name"));
            } else {
                stringBuffer.append(strTitle);
            }
            strings.add(stringBuffer.toString());
        }
        return strings;
    }
    /**
     * 获取table中的所有的对象id
     * @param args
     * @throws
     * @return matrix.util.StringList
     * @date 2024/7/9 16:35
     * @description
     */
    public static StringList getObjectIdList(String[] args) throws Exception {
        StringList objectIdList = new StringList();
        Map paramsMap = (Map) JPO.unpackArgs(args);
        MapList objectList = (MapList)paramsMap.get("objectList");
        if (objectList.isEmpty()) {
            return objectIdList;
        }else {
            int iTemp = 0;
            for(int iSize = objectList.size(); iTemp < iSize; ++iTemp) {
                Map objectMap = (Map)objectList.get(iTemp);
                if (null != objectMap && objectMap.containsKey(DomainConstants.SELECT_ID)) {
                    String objectId = (String)objectMap.get(DomainConstants.SELECT_ID);
                    objectIdList.add(objectId);
                }
            }
        }
        return objectIdList;
    }

    /**
    * 任务界面改写-已经分派任务视图
    * @param paramContext
	* @param paramArrayOfString
    * @author LIUJR
    * @throws
    * @return java.lang.Object
    * @date 2024/9/4 10:05
    * @description
    */
    @ProgramCallable
    public Object getAssignedWBSTask(Context paramContext, String[] paramArrayOfString) throws Exception {
        Task task = (Task)DomainObject.newInstance(paramContext, DomainConstants.TYPE_TASK, "PROGRAM");
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("current");
        stringBuffer.append("!='");
        stringBuffer.append(STATE_PROJECT_TASK_COMPLETE);
        stringBuffer.append("'");
        if (!"".equals(STATE_PROJECT_TASK_CREATE)) {
            stringBuffer.append(" && ");
            stringBuffer.append("current");
            stringBuffer.append("!='");
            stringBuffer.append(STATE_PROJECT_TASK_CREATE);
            stringBuffer.append("'");
        }
        if (!"".equalsIgnoreCase(STATE_PROJECT_TASK_ARCHIVE)) {
            stringBuffer.append(" && ");
            stringBuffer.append("current");
            stringBuffer.append("!='");
            stringBuffer.append(STATE_PROJECT_TASK_ARCHIVE);
            stringBuffer.append("'");
        }
        //add by ljr
        stringBuffer.append(" && ");
        stringBuffer.append("to[Project Access Key]=='TRUE'");
        //end
        emxTaskBase_mxJPO emxTaskBaseMxJPO = new emxTaskBase_mxJPO(paramContext, paramArrayOfString);
        return emxTaskBaseMxJPO.getMyTasks(paramContext, paramArrayOfString, stringBuffer.toString());
    }

    /**
    * 任务界面改写 - 全部任务视图
    * @param paramContext
	* @param paramArrayOfString
    * @author LIUJR
    * @throws
    * @return java.lang.Object
    * @date 2024/9/4 10:06
    * @description
    */
    @ProgramCallable
    public Object getAllWBSTask(Context paramContext, String[] paramArrayOfString) throws Exception {
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("to[" + DomainConstants.RELATIONSHIP_PROJECT_ACCESS_KEY + "].from.from[" + DomainConstants.RELATIONSHIP_PROJECT_ACCESS_LIST + "].to.current!='" + ProgramCentralConstants.STATE_PROJECT_SPACE_HOLD_CANCEL_HOLD + "'");
        stringBuffer.append(" && ");
        stringBuffer.append("to[" + DomainConstants.RELATIONSHIP_PROJECT_ACCESS_KEY + "].from.from[" + DomainConstants.RELATIONSHIP_PROJECT_ACCESS_LIST + "].to.current!='" + ProgramCentralConstants.STATE_PROJECT_SPACE_HOLD_CANCEL_CANCEL + "'");
        //add by ljr
        stringBuffer.append(" && ");
        stringBuffer.append("to[Project Access Key]=='TRUE'");
        //end
        emxTaskBase_mxJPO emxTaskBaseMxJPO = new emxTaskBase_mxJPO(paramContext, paramArrayOfString);
        return emxTaskBaseMxJPO.getMyTasks(paramContext, paramArrayOfString, stringBuffer.toString());
    }


    /**
    * 任务界面改写-已完成任务的视图
    * @param paramContext
	* @param paramArrayOfString
    * @author LIUJR
    * @throws
    * @return java.lang.Object
    * @date 2024/9/4 10:06
    * @description
    */
    @ProgramCallable
    public Object getCompletedWBSTask(Context paramContext, String[] paramArrayOfString) throws Exception {
        Task task = (Task)DomainObject.newInstance(paramContext, DomainConstants.TYPE_TASK, "PROGRAM");
        String str = "current" + "=='" + "Complete" + "'";
        //add by ljr
        str += " && " + "to[Project Access Key]=='TRUE'";
        //end
        emxTaskBase_mxJPO emxTaskBaseMxJPO = new emxTaskBase_mxJPO(paramContext, paramArrayOfString);
        return emxTaskBaseMxJPO.getMyTasks(paramContext, paramArrayOfString, str);
    }

    /**
    * 任务界面改写-已接受的任务视图
    * @param paramContext
	* @param paramArrayOfString
    * @author LIUJR
    * @throws
    * @return java.lang.Object
    * @date 2024/9/4 10:07
    * @description
    */
    @ProgramCallable
    public Object getCandidateTask(Context paramContext, String[] paramArrayOfString) throws Exception {
        String str = paramContext.getUser();
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("current");
        stringBuffer.append("!='");
        stringBuffer.append(STATE_PROJECT_TASK_COMPLETE);
        stringBuffer.append("'");
        stringBuffer.append(" && ");
        stringBuffer.append("current");
        stringBuffer.append("!='");
        stringBuffer.append(STATE_PROJECT_TASK_CREATE);
        stringBuffer.append("'");
        stringBuffer.append(" && ");
        stringBuffer.append("current");
        stringBuffer.append("!='");
        stringBuffer.append(STATE_PROJECT_TASK_ARCHIVE);
        stringBuffer.append("'");
        //add by ljr
        stringBuffer.append(" && ");
        stringBuffer.append("to[Project Access Key]=='TRUE'");
        //end
        emxTaskBase_mxJPO emxTaskBaseMxJPO = new emxTaskBase_mxJPO(paramContext, paramArrayOfString);
        return emxTaskBaseMxJPO.getMyTasks(paramContext, paramArrayOfString, stringBuffer.toString(), ProgramCentralConstants.RELATIONSHIP_ASSIGNED_TASKS_CANDIDATE);
    }

    /**
     * Range Values for Appproval Status in Inbox Task form
     * @param context
     * @param args
     * @return
     * @throws FrameworkException
     */

    public Map getTaskApprovalStatusOptions(Context context, String[] args) throws FrameworkException {

        try {
            Map programMap = (Map)JPO.unpackArgs(args);
            Map requestMap = (Map)programMap.get("requestMap");
            Map paramMap   = (Map)programMap.get("paramMap");
            String sLanguage = (String) paramMap.get("languageStr");
            String objectId = (String) requestMap.get("objectId");

            Map returnMap = new HashMap(2);
            StringList rangeDisplay = new StringList(3);
            StringList rangeActual = new StringList(3);
            String showAbstain = FrameworkUtil.getshowAbstainOptionValue(context);
            showAbstain = UIUtil.isNullOrEmpty(showAbstain) ? "true" : showAbstain;

            DomainObject taskObj = DomainObject.newInstance(context, objectId);
            StringList selects = new StringList();
            selects.add("attribute[" + sAttrReviewTask + "]");
            selects.add("attribute[" + sAttrReviewersComments + "]");

            //get the details required
            Map taskMap = taskObj.getInfo(context, selects);
            String reviewTask = (String) taskMap.get("attribute[" + sAttrReviewTask + "]");
            String reviewComments = (String) taskMap.get("attribute[" + sAttrReviewersComments + "]");

            if("Yes".equals(reviewTask) && UIUtil.isNotNullAndNotEmpty(reviewComments)) {

                String promote= (String) EnoviaResourceBundle.getFrameworkStringResourceProperty(context, "emxFramework.Lifecycle.Promote", context.getLocale());
                String demote= (String) EnoviaResourceBundle.getFrameworkStringResourceProperty(context, "emxFramework.Lifecycle.Demote", context.getLocale());
                rangeActual.add("promote");
                rangeDisplay.add(promote);
                rangeActual.add("demote");
                rangeDisplay.add(demote);
                returnMap.put("field_choices", rangeActual);
                returnMap.put("field_display_choices", rangeDisplay);

            } else {
                matrix.db.AttributeType attribName = new matrix.db.AttributeType(
                        DomainConstants.ATTRIBUTE_APPROVAL_STATUS);
                attribName.open(context);
                // actual range values
                matrix.util.List attributeRange = attribName.getChoices();

                attribName.close(context);
                List attributeDisplayRange = i18nNow.getAttrRangeI18NStringList(
                        DomainConstants.ATTRIBUTE_APPROVAL_STATUS, (StringList) attributeRange, sLanguage);
                attributeDisplayRange.remove(attributeRange.indexOf("Ignore"));
                attributeRange.remove("Ignore");
                attributeDisplayRange.remove(attributeRange.indexOf("Signature Reset"));
                attributeRange.remove("Signature Reset");
                attributeDisplayRange.remove(attributeRange.indexOf("None"));
                attributeRange.remove("None");
                attributeDisplayRange.remove(attributeRange.indexOf("Auto Complete"));
                attributeRange.remove("Auto Complete");
                attributeDisplayRange.remove(attributeRange.indexOf("Abstain"));
                attributeRange.remove("Abstain");
                if ("FALSE".equalsIgnoreCase(showAbstain)) {
                    attributeDisplayRange.remove(attributeRange.indexOf("Abstain"));
                    attributeRange.remove("Abstain");
                }
                rangeActual.addAll(attributeRange);
                rangeDisplay.addAll(attributeDisplayRange);
                returnMap.put("field_choices", rangeActual);
                returnMap.put("field_display_choices", rangeDisplay);
            }
            return returnMap;

        } catch (Exception e) {
            throw new FrameworkException(e);
        }
    }
public String encode(String title){
    title = title.trim().replaceAll(" ", " ");
    title = title.trim().replaceAll("±", "\u6B63\u8D1F");
    title = title.trim().replaceAll("-", "");
    title = title.trim().replaceAll("—", "");
    title = title.trim().replaceAll("&", "");
    return title;
}
/*
 * @description:关联类型
 * @author: caipan
 * @date: 2025/11/12 10:10:39
 * @param: * @param[1] context
 * @param[2] args
 * @return:
 **/
    public StringList getRelationshipType(Context context, String[] args) throws Exception {

        StringList strings = new StringList();
        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        Map paramList = (Map) programMap.get("paramList");
        String reportFormat = (String)paramList.get("reportFormat");
        StringList strObjectIdList = getObjectIdList(args);
        String strRel = "from[Route Task].to.to[Object Route].from.";
        StringList strBusSelectList = new StringList();
        strBusSelectList.add(strRel + DomainConstants.SELECT_ID);
        strBusSelectList.add(strRel + DomainConstants.SELECT_NAME);
        strBusSelectList.add(strRel + DomainConstants.SELECT_TYPE);
        strBusSelectList.add(strRel + "attribute[Synopsis]");
        strBusSelectList.add(strRel + DomainConstants.SELECT_ATTRIBUTE_TITLE);
        String language = context.getLocale().getLanguage();
        for (String strObjectId : strObjectIdList) {
            StringBuffer stringBuffer = new StringBuffer();
            DomainObject dr = DomainObject.newInstance(context,strObjectId);
            Map infoMap = dr.getInfo(context, strBusSelectList);
            String strType = UIUtil.getValue(infoMap, strRel +   DomainConstants.SELECT_TYPE);
            if(UIUtil.isNotNullAndNotEmpty(strType)) {
                stringBuffer.append(EnoviaResourceBundle.getTypeI18NString(context, strType, language));
            }
            strings.add(stringBuffer.toString());
        }
        return strings;
    }
    /*
     * @description:关联项目
     * update by ljr 审核任务需要增加对应的关联项目信息显示
     *              （测试数据：回执专家审核IT-sit-0005287；数据外发单审核IT-sit-0005291；
     *               ESO审核IT-sit-0005227；文档审批：IT-sit-0005292）
     * @author: caipan
     * @date: 2025/11/12 10:10:58
     * @param: * @param[1] context
     * @param[2] args
     * @return:
     **/
    public StringList getRelationshipProjectSpace(Context context, String[] args) throws Exception {
        StringList strings = new StringList();
        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        Map paramList = (Map) programMap.get("paramList");
        String reportFormat = (String)paramList.get("reportFormat");
        try {
            String language = context.getLocale().getLanguage();
            ContextUtil.pushContext(context);
            StringList strObjectIdList = getObjectIdList(args);
            String strRel = "from[Route Task].to.to[Object Route].from.";
            StringList strBusSelectList = new StringList();
            strBusSelectList.add(strRel + DomainConstants.SELECT_ID);
            strBusSelectList.add(strRel + DomainConstants.SELECT_NAME);
            strBusSelectList.add(strRel + DomainConstants.SELECT_TYPE);
            strBusSelectList.add(strRel + "attribute[Synopsis]");
            strBusSelectList.add(strRel + DomainConstants.SELECT_ATTRIBUTE_TITLE);
            String rel = "JFChange2Project,JFProject2Snapshot,JF_relProject2ChangeRecord,JFProject2PartList,JFPCR2Project";
//            String rel = "JFChange2Project,JFProject2Snapshot,JF_relProject2ChangeRecord,JFProject2PartList,JFPCR2Project";
            StringList selList = JF_Util_mxJPO.basicBolistSel();
            selList.add(DomainConstants.SELECT_DESCRIPTION);
            for (String strObjectId : strObjectIdList) {
                StringBuffer stringBuffer = new StringBuffer();
                DomainObject dr = DomainObject.newInstance(context, strObjectId);
                StringList routeList = dr.getInfoList(context, "from[Route Task].to.id");
                if (routeList.size() > 0) {
                    String routeId = routeList.get(0);
                    dr.setId(routeId);
                    StringList objectList = dr.getInfoList(context, "to[Object Route].from.id");
                    if (objectList.size() > 0) {
                        String objId = objectList.get(0);
                        dr.setId(objId);
                        String type = dr.getInfo(context, DomainConstants.SELECT_TYPE);
                        if ("JFDataOutSource".equalsIgnoreCase(type)) {
                            //数据外发单审核
                            String jsConnectProject = dr.getAttributeValue(context, "JSConnectProject");
                            if (UIUtil.isNotNullAndNotEmpty(jsConnectProject)) {
                                dr.setId(jsConnectProject);
                                stringBuffer.append(dr.getDescription(context));
                            } else {
                                stringBuffer.append("");
                            }
                        } else if (DomainConstants.TYPE_DOCUMENT.equalsIgnoreCase(type)) {
                            //文档审批
                            stringBuffer.append(dr.getAttributeValue(context, JF_PLMConstants_mxJPO.ATTR_JF_ConnProjectName));
                        } else if ("JFESOReview".equalsIgnoreCase(type)){
                            // ESO审核
                            String taskId = dr.getInfo(context, "to[JFESOTask2ESOReview].from.id");
                            if (UIUtil.isNotNullAndNotEmpty(taskId)) {
                                dr.setId(taskId);
                                stringBuffer.append(dr.getInfo(context, "to[Project Access Key].from.from[Project Access List].to.description"));
                            } else {
                                stringBuffer.append("");
                            }
                        }else {
                            MapList projectList = dr.getRelatedObjects(context,
                                    rel, //pattern to match relationships
                                    DomainConstants.TYPE_PROJECT_SPACE, //pattern to match types
                                    selList, //the eMatrix StringList object that holds the list of select statement pertaining to Business Obejcts.
                                    JF_Util_mxJPO.basicRellistSel(), //the eMatrix StringList object that holds the list of select statement pertaining to Relationships.
                                    true, //get To relationships
                                    true, //get From relationships
                                    (short) 1, //the number of levels to expand, 0 equals expand all.
                                    DomainConstants.EMPTY_STRING, //where clause to apply to objects, can be empty ""
                                    null, //where clause to apply to relationship, can be empty ""
                                    (short) 0); //limit
                            if (projectList.size() > 0) {
                                Map map = (Map) projectList.get(0);
                                stringBuffer.append(UIUtil.getValue(map, DomainConstants.SELECT_DESCRIPTION));
                            }
                        }
                    }
                }
                strings.add(stringBuffer.toString());
            }
        }finally {
            ContextUtil.popContext(context);
        }
        return strings;
    }
    /*
     * @description:项目阶段
     * @author: caipan
     * @date: 2025/11/12 11:26:38
     * @param: * @param[1] context
     * @param[2] args
     * @return:
     **/
    public StringList getRelationshipPhase(Context context, String[] args) throws Exception {
        StringList strings = new StringList();
        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        Map paramList = (Map) programMap.get("paramList");
        String reportFormat = (String)paramList.get("reportFormat");
        try {
            String language = context.getLocale().getLanguage();
            ContextUtil.pushContext(context);
            StringList strObjectIdList = getObjectIdList(args);
            String strRel = "from[Route Task].to.to[Object Route].from.";
            StringList strBusSelectList = new StringList();
            strBusSelectList.add(strRel + DomainConstants.SELECT_ID);
            strBusSelectList.add(strRel + DomainConstants.SELECT_NAME);
            strBusSelectList.add(strRel + DomainConstants.SELECT_TYPE);
            strBusSelectList.add(strRel + "attribute[Synopsis]");
            strBusSelectList.add(strRel + DomainConstants.SELECT_ATTRIBUTE_TITLE);
            String rel = "JFChange2Project,JFProject2Snapshot,JF_relProject2ChangeRecord,JFProject2PartList,JFPCR2Project";
            StringList selList = JF_Util_mxJPO.basicBolistSel();
            selList.add(DomainConstants.SELECT_DESCRIPTION);
            for (String strObjectId : strObjectIdList) {
                StringBuffer stringBuffer = new StringBuffer();
                DomainObject dr = DomainObject.newInstance(context, strObjectId);
                StringList routeList = dr.getInfoList(context, "from[Route Task].to.id");
                if (routeList.size() > 0) {
                    String routeId = routeList.get(0);
                    dr.setId(routeId);
                    StringList objectList = dr.getInfoList(context, "to[Object Route].from.id");
                    if (objectList.size() > 0) {
                        String objId = objectList.get(0);
                        dr.setId(objId);
                        //获取阶段
                        String str = dr.getAttributeValue(context,"JFProjectPhase");
                        if(UIUtil.isNullOrEmpty(str)){
                         str = dr.getAttributeValue(context,"JF_PCRProjectPhase");
                        }
                        stringBuffer.append(str);
                    }
                }
                strings.add(stringBuffer.toString());
            }
        }finally {
            ContextUtil.popContext(context);
        }
        return strings;
    }
}
