import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import com.matrixone.apps.domain.util.*;
import ds.enovia.apps.msf.BusinessObjects.ExecuteJPO.args;
import matrix.db.*;

import matrix.util.SelectList;
import matrix.util.StringList;

import com.matrixone.apps.common.Route;
import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.DomainRelationship;
import com.matrixone.apps.framework.ui.UIUtil;
import org.slf4j.LoggerFactory;

import static com.matrixone.apps.domain.DomainConstants.*;

public class JF_Route_mxJPO extends emxRouteBase_mxJPO {
    protected static final String ATTRIBUTE_ROUTE_COMPLETION_ACTION = PropertyUtil.getSchemaProperty("attribute_RouteCompletionAction");

    protected static final String ATTRIBUTE_PARALLEL_NODE_PROCESSION_RULE = PropertyUtil.getSchemaProperty("attribute_ParallelNodeProcessionRule");
    String sScheduledCompletionDateAttr = PropertyUtil.getSchemaProperty( "attribute_ScheduledCompletionDate");

    protected static final String SELECT_ATTRIBUTE_ROUTE_BASE_STATE = (new StringBuilder()
            .append("attribute[").append(DomainObject.ATTRIBUTE_ROUTE_BASE_STATE)
            .append("]").toString());

    private static final String SELECT_ATTRIBUTE_APPROVAL_STATUS = (new StringBuilder()).append("attribute[").append(DomainObject.ATTRIBUTE_APPROVAL_STATUS).append("]").toString();

    private static final String SELECT_ATTRIBUTE_COMMENTS = (new StringBuilder()).append("attribute[").append(DomainObject.ATTRIBUTE_COMMENTS).append("]").toString();

    private static final String ATTRIBUTE_ACTUAL_COMPLETION_DATE = PropertyUtil.getSchemaProperty("attribute_ActualCompletionDate");

    private static final String SELECT_ATTRIBUTE_ACTUAL_COMPLETION_DATE = (new StringBuilder()).append("attribute[").append(ATTRIBUTE_ACTUAL_COMPLETION_DATE).append("]").toString();

    private static final String SELECT_ATTRIBUTE_ROUTE_SEQUENCE = (new StringBuilder()).append("attribute[").append(DomainObject.ATTRIBUTE_ROUTE_SEQUENCE).append("]").toString();

    private static final String STRING_OBJECTIDS = "objectIds";

    private static final String STRING_LIST_NEEDBEENNOTIFY = "needbeennotify";

    /**
     * String ",".
     */
    private static final String STRING_SYMB_COMMA = ",";

    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(JF_Route_mxJPO.class);


    public JF_Route_mxJPO(Context context, String[] args) throws Exception {
        super(context, args);
        // TODO Auto-generated constructor stub
    }
    /**
     * Create Route.
     *
     * @param context               the eMatrix <code>Context</code> object.
     * @param mlMembers             The persons those need to be added into the Route.
     *                              map("id") --- person id;
     *                              map("Route Instructions") --- Task instruction;
     *                              map("Route Action") --- Approve (default)
     *                              Comment
     *                              Notify Only
     *                              map("Title") --- task title;
     *                              map("Allow Delegation") --- False
     *                              True
     *                              map("Route Sequence") --- the order of the task;
     *                              map("Parallel Node Procession Rule") --- Any
     *                              All
     * @param sContentId            The related object id.
     * @param sBaseState            The related object state.
     * @param sPolicy               The related object policy.
     * @param sRouteDescription     The Route description.
     * @param sRouteOwner           The Route owner.
     * @param RouteCompletionAction attribute[Route Completion Action].Aa
     * @param sRestrictMembers      The scope of route.
     * @return The route id.
     * @throws Exception If has error.
     */
    public static String createRoute(Context context, MapList mlMembers,
                                     String sContentId, String sBaseState, String sPolicy,
                                     String sRouteDescription, String sRouteOwner,
                                     String RouteCompletionAction, String sRestrictMembers) throws Exception {
logger.info("createRoutestart");
        String sRouteId = DomainConstants.EMPTY_STRING;
        boolean isPushCtx = false;
        try {
            String sAttrRouteBasePurpose = PropertyUtil.getSchemaProperty(context, "attribute_RouteBasePurpose");
            MapList mlPersons = new MapList();
            mlPersons.addAll(mlMembers);
            logger.info("mlPersons:{}",mlPersons);
            sRouteId = FrameworkUtil.autoName(context, "type_Route", DomainConstants.EMPTY_STRING, "policy_Route", context.getVault().getName());
            Route route = new Route();
            route.setId(sRouteId);
            route.open(context);
            route.setDescription(context, sRouteDescription);
            StringList slPersonIds = getStringListIds(mlPersons);
            String[] sMembers = slPersonIds.toArray(new String[0]);
            route.AddMembers(context, sMembers);
            route.setOwner(context, sRouteOwner);
            RelationshipType relTypeObjectRoute = new RelationshipType(DomainConstants.RELATIONSHIP_OBJECT_ROUTE);
            DomainRelationship newRel = route.addFromObject(context, relTypeObjectRoute, sContentId);

            Map routeAttrMap = new HashMap();
            routeAttrMap.put(ATTRIBUTE_ROUTE_COMPLETION_ACTION, RouteCompletionAction);
            routeAttrMap.put(DomainConstants.ATTRIBUTE_RESTRICT_MEMBERS, sRestrictMembers);
            route.setAttributeValues(context, routeAttrMap);

            Map relAttrMap = new HashMap();
            relAttrMap.put(DomainConstants.ATTRIBUTE_ROUTE_BASE_STATE, sBaseState);
            relAttrMap.put(DomainConstants.ATTRIBUTE_ROUTE_BASE_POLICY, sPolicy);
            relAttrMap.put(sAttrRouteBasePurpose, "Review");
            ContextUtil.pushContext(context);
            isPushCtx = true;
            newRel.setAttributeValues(context, relAttrMap);
            ContextUtil.popContext(context);
            isPushCtx = false;

            SelectList relProductSelects = new SelectList(1);
            relProductSelects.add(DomainConstants.SELECT_RELATIONSHIP_ID);
            String nodeTypePattern = DomainConstants.TYPE_PERSON + STRING_SYMB_COMMA + DomainConstants.TYPE_ROUTE_TASK_USER;

            // get all the tasks in the route
            MapList routeNodeList = route.getRelatedObjects(context, DomainConstants.RELATIONSHIP_ROUTE_NODE, nodeTypePattern, new StringList(), relProductSelects,
                    false, true, (short) 1, DomainConstants.EMPTY_STRING, DomainConstants.EMPTY_STRING, 0);
            DomainObject object = DomainObject.newInstance(context, sContentId);
            String type = object.getInfo(context, SELECT_TYPE);

            //add by JD_JS for PRM-5210 End
            for (Iterator itrRouteNodeList = routeNodeList.iterator(); itrRouteNodeList.hasNext(); ) {
                Map currentMap = (Map) itrRouteNodeList.next();
                String routeNodeRelId = (String) currentMap
                        .get(DomainConstants.SELECT_RELATIONSHIP_ID);
                DomainRelationship routeNodeRel = new DomainRelationship(
                        routeNodeRelId);
                String sPersonId = MqlUtil.mqlCommand(context, false, "print connection " + routeNodeRelId + " select to.id dump", true);
                Map mPerson = getPerson(mlPersons, sPersonId);
                Map routeNodeRelAttriMap = new HashMap();
                String sRouteInstructions = (String) mPerson.get(DomainConstants.ATTRIBUTE_ROUTE_INSTRUCTIONS);
                routeNodeRelAttriMap.put(DomainConstants.ATTRIBUTE_ROUTE_INSTRUCTIONS, sRouteInstructions);

                String sRouteAction = (String) mPerson.get(DomainConstants.ATTRIBUTE_ROUTE_ACTION);
                if (sRouteAction == null || DomainConstants.EMPTY_STRING.equals(sRouteAction)) {
                    sRouteAction = "Approve";
                }
                routeNodeRelAttriMap.put(DomainConstants.ATTRIBUTE_ROUTE_ACTION, sRouteAction);
//                routeNodeRelAttriMap.put(DomainConstants.ATTRIBUTE_ASSIGNEE_SET_DUEDATE, "Yes");


                String sTitle = (String) mPerson.get(DomainConstants.ATTRIBUTE_TITLE);
                routeNodeRelAttriMap.put(DomainConstants.ATTRIBUTE_TITLE, sTitle);

                String sAllowDelegation = (String) mPerson.get(DomainConstants.ATTRIBUTE_ALLOW_DELEGATION);
                routeNodeRelAttriMap.put(DomainConstants.ATTRIBUTE_ALLOW_DELEGATION, sAllowDelegation);

                String sRouteSequence = (String) mPerson.get(DomainConstants.ATTRIBUTE_ROUTE_SEQUENCE);
                routeNodeRelAttriMap.put(DomainConstants.ATTRIBUTE_ROUTE_SEQUENCE, sRouteSequence);

                String sParallelNodeProcessionRule = (String) mPerson.get(ATTRIBUTE_PARALLEL_NODE_PROCESSION_RULE);
                routeNodeRelAttriMap.put(ATTRIBUTE_PARALLEL_NODE_PROCESSION_RULE, sParallelNodeProcessionRule);

                //add by jiangliang , to udpate the due date for Inbox Task.
                String sMql = "print type '" + DomainConstants.TYPE_INBOX_TASK + "' select property[dueDateOffset].value dump";
                String sDueDateOffset = MqlUtil.mqlCommand(context, false, sMql, true);
                logger.info("sDueDateOffset:{}",sDueDateOffset);
                routeNodeRelAttriMap.put(DomainObject.ATTRIBUTE_DATE_OFFSET_FROM, "Task Create Date");
                //PRM-2268 by niumr
                    routeNodeRelAttriMap.put(DomainObject.ATTRIBUTE_DUEDATE_OFFSET, sDueDateOffset);
                //add by jiangliang end


                routeNodeRel.setAttributeValues(context, routeNodeRelAttriMap);
            }
        } catch (Exception e) {
            logger.error(Arrays.toString(e.getStackTrace()));
            throw e;
        } finally {
            if (isPushCtx) {
                ContextUtil.popContext(context);
            }
        }

        return sRouteId;
    }
    /**
     * @description 创建一个流程，关联多个对象
     * @author caipan
     * @param[1] context
     * @param[2] mlMembers
     * @param[3] sContentId
     * @param[4] sBaseState
     * @param[5] sPolicy
     * @param[6] sRouteDescription
     * @param[7] sRouteOwner
     * @param[8] RouteCompletionAction
     * @param[9] sRestrictMembers
     * @throws

     * @time 2024/1/30 16:47
     */
    public static String createRouteForMultipleObject(Context context, MapList mlMembers,
                                     StringList sContentId, String sBaseState, String sPolicy,
                                     String sRouteDescription, String sRouteOwner,
                                     String RouteCompletionAction, String sRestrictMembers) throws Exception {

        String sRouteId = DomainConstants.EMPTY_STRING;
        boolean isPushCtx = false;
        try {
            String sAttrRouteBasePurpose = PropertyUtil.getSchemaProperty(context, "attribute_RouteBasePurpose");
            MapList mlPersons = new MapList();
            mlPersons.addAll(mlMembers);
            sRouteId = FrameworkUtil.autoName(context, "type_Route", DomainConstants.EMPTY_STRING, "policy_Route", context.getVault().getName());
            Route route = new Route();
            route.setId(sRouteId);
            route.open(context);
            route.setDescription(context, sRouteDescription);
            StringList slPersonIds = getStringListIds(mlPersons);
            String[] sMembers = slPersonIds.toArray(new String[0]);
            route.AddMembers(context, sMembers);
            route.setOwner(context, sRouteOwner);
            RelationshipType relTypeObjectRoute = new RelationshipType(DomainConstants.RELATIONSHIP_OBJECT_ROUTE);
            for(int i=0;i<sContentId.size();i++){
                DomainRelationship newRel = route.addFromObject(context, relTypeObjectRoute, sContentId.get(i));
            Map relAttrMap = new HashMap();
            relAttrMap.put(DomainConstants.ATTRIBUTE_ROUTE_BASE_STATE, sBaseState);
            relAttrMap.put(DomainConstants.ATTRIBUTE_ROUTE_BASE_POLICY, sPolicy);
            relAttrMap.put(sAttrRouteBasePurpose, "Review");
            ContextUtil.pushContext(context);
            isPushCtx = true;
            newRel.setAttributeValues(context, relAttrMap);
            ContextUtil.popContext(context);
            isPushCtx = false;
            }
            Map routeAttrMap = new HashMap();
            routeAttrMap.put(ATTRIBUTE_ROUTE_COMPLETION_ACTION, RouteCompletionAction);
            routeAttrMap.put(DomainConstants.ATTRIBUTE_RESTRICT_MEMBERS, sRestrictMembers);
            route.setAttributeValues(context, routeAttrMap);

            SelectList relProductSelects = new SelectList(1);
            relProductSelects.add(DomainConstants.SELECT_RELATIONSHIP_ID);
            String nodeTypePattern = DomainConstants.TYPE_PERSON + STRING_SYMB_COMMA + DomainConstants.TYPE_ROUTE_TASK_USER;

            // get all the tasks in the route
            MapList routeNodeList = route.getRelatedObjects(context, DomainConstants.RELATIONSHIP_ROUTE_NODE, nodeTypePattern, new StringList(), relProductSelects,
                    false, true, (short) 1, DomainConstants.EMPTY_STRING, DomainConstants.EMPTY_STRING, 0);
//            DomainObject object = DomainObject.newInstance(context, sContentId);
//            String type = object.getInfo(context, DomainConstants.SELECT_TYPE);

            //add by JD_JS for PRM-5210 End
            for (Iterator itrRouteNodeList = routeNodeList.iterator(); itrRouteNodeList.hasNext(); ) {
                Map currentMap = (Map) itrRouteNodeList.next();
                String routeNodeRelId = (String) currentMap
                        .get(DomainConstants.SELECT_RELATIONSHIP_ID);
                DomainRelationship routeNodeRel = new DomainRelationship(
                        routeNodeRelId);
                String sPersonId = MqlUtil.mqlCommand(context, false, "print connection " + routeNodeRelId + " select to.id dump", true);
                Map mPerson = getPerson(mlPersons, sPersonId);
                Map routeNodeRelAttriMap = new HashMap();
                String sRouteInstructions = (String) mPerson.get(DomainConstants.ATTRIBUTE_ROUTE_INSTRUCTIONS);
                routeNodeRelAttriMap.put(DomainConstants.ATTRIBUTE_ROUTE_INSTRUCTIONS, sRouteInstructions);

                String sRouteAction = (String) mPerson.get(DomainConstants.ATTRIBUTE_ROUTE_ACTION);
                if (sRouteAction == null || DomainConstants.EMPTY_STRING.equals(sRouteAction)) {
                    sRouteAction = "Approve";
                }
                routeNodeRelAttriMap.put(DomainConstants.ATTRIBUTE_ROUTE_ACTION, sRouteAction);
//                routeNodeRelAttriMap.put(DomainConstants.ATTRIBUTE_ASSIGNEE_SET_DUEDATE, "Yes");
                String sTitle = (String) mPerson.get(DomainConstants.ATTRIBUTE_TITLE);
                routeNodeRelAttriMap.put(DomainConstants.ATTRIBUTE_TITLE, sTitle);

                String sAllowDelegation = (String) mPerson.get(DomainConstants.ATTRIBUTE_ALLOW_DELEGATION);
                routeNodeRelAttriMap.put(DomainConstants.ATTRIBUTE_ALLOW_DELEGATION, sAllowDelegation);

                String sRouteSequence = (String) mPerson.get(DomainConstants.ATTRIBUTE_ROUTE_SEQUENCE);
                routeNodeRelAttriMap.put(DomainConstants.ATTRIBUTE_ROUTE_SEQUENCE, sRouteSequence);

                String sParallelNodeProcessionRule = (String) mPerson.get(ATTRIBUTE_PARALLEL_NODE_PROCESSION_RULE);
                routeNodeRelAttriMap.put(ATTRIBUTE_PARALLEL_NODE_PROCESSION_RULE, sParallelNodeProcessionRule);

                //add by jiangliang , to udpate the due date for Inbox Task.
                String sMql = "print type '" + DomainConstants.TYPE_INBOX_TASK + "' select property[dueDateOffset].value dump";
                String sDueDateOffset = MqlUtil.mqlCommand(context, false, sMql, true);
                routeNodeRelAttriMap.put(DomainObject.ATTRIBUTE_DATE_OFFSET_FROM, "Task Create Date");
                //PRM-2268 by niumr
                routeNodeRelAttriMap.put(DomainObject.ATTRIBUTE_DUEDATE_OFFSET, sDueDateOffset);
                //add by jiangliang end
                routeNodeRel.setAttributeValues(context, routeNodeRelAttriMap);
            }
        } catch (Exception e) {
            logger.error(Arrays.toString(e.getStackTrace()));
            throw e;
        } finally {
            if (isPushCtx) {
                ContextUtil.popContext(context);
            }
        }

        return sRouteId;
    }
    public static int startRoute(Context context, String sObjectId, String sState) {
        int i = 0;
        boolean isOpenObj = false;
        DomainObject objObject = new DomainObject();
        try {
            objObject.setId(sObjectId);
            objObject.open(context);
            isOpenObj = true;
            StringList slObjRoute = new StringList();
            slObjRoute.add(DomainConstants.SELECT_ID);
            slObjRoute.add(DomainConstants.SELECT_CURRENT);

            StringList slRelRoute = new StringList();
            slRelRoute.add(SELECT_ATTRIBUTE_ROUTE_BASE_STATE);

            String where = SELECT_ATTRIBUTE_ROUTE_BASE_STATE +JF_PLMConstants_mxJPO.STRING_SYMB_SPACE + JF_PLMConstants_mxJPO.STRING_SYMB_EQUAL
                    + JF_PLMConstants_mxJPO.STRING_SYMB_SPACE + sState;
            MapList routeMapList = objObject.getRelatedObjects(context, DomainConstants.RELATIONSHIP_OBJECT_ROUTE, DomainConstants.TYPE_ROUTE, slObjRoute, slRelRoute,
                    false, true, (short) 0, null, where, 0);

            String current = objObject.getInfo(context, DomainObject.SELECT_CURRENT);//PRM-3791
            if (routeMapList != null && routeMapList.size() > 0) {
                Map routeMap;
                for (int j = 0; j < routeMapList.size(); j++) {
                    routeMap = (Map) routeMapList.get(j);
                    String sRouteId = (String) routeMap.get(DomainConstants.SELECT_ID);
                    if (isInProcessStopped(context, sRouteId)) {
                        reStartRoute(context, sRouteId);
                    } else {
                        String sCurrent = (String) routeMap.get(DomainConstants.SELECT_CURRENT);
                        if (DomainConstants.STATE_ROUTE_DEFINE.equals(sCurrent)) {
                            new emxChange_mxJPO(context, null).startRoute(context, sRouteId);
                        }
                    }

/*                    if (!checkHasGeneral || !"In Work".equals(current)) {
                        break;
                    }*/
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage(),e);
        } finally {
            if (isOpenObj) {
                objObject.closeObject(context, objObject.isOpen());
            }
        }

        return i;
    }
    public static boolean isInProcessStopped(Context context, String routeID) {
        boolean bl = false;
        DomainObject obj = null;
        try {
            obj = newInstance(context);
            obj.setId(routeID);
            obj.open(context);
            String rCurrent = obj.getInfo(context, "current");
            if ("In Process".equalsIgnoreCase(rCurrent)) {
                String routeStatus = obj.getAttributeValue(context, "Route Status");
                if ("Stopped".equalsIgnoreCase(routeStatus)) {
                    bl = true;
                }
            }
        } catch (Exception e) {
            logger.error(Arrays.toString(e.getStackTrace()));
        } finally {
            try {
                if (obj != null) {
                    obj.closeObject(context, obj.isOpen());
                }
            } catch (Exception e) {
                logger.error(Arrays.toString(e.getStackTrace()));
            }
        }
        return bl;
    }
    public static void reStartRoute(Context context, String routeID) throws Exception {
        try {
            Route route = new Route(routeID);
            route.reStart(context);
        } catch (Exception e) {
            logger.error(Arrays.toString(e.getStackTrace()));
            throw e;
        }
    }

    /**
     * @description 测试创建流程
     * @author caipan
     * @param[1] context
     * @param[2] args
     * @throws

     * @time 2024/1/5 13:37
     */
    public void testCreateRoute(Context context, String[] args) throws Exception {
        try {
            String objectId =args[0]; //审核对象Id
            MapList approveList = new MapList();//组装审批人员
            String personId = args[1];;//人员ID
            Map nReceiverMap = (Map) getMap(personId, "testApprove");//设置审批信息，testApprove 任务的标题
            nReceiverMap.put(DomainObject.ATTRIBUTE_ROUTE_SEQUENCE, "1");//设置审批顺序
            approveList.add(nReceiverMap);
            String state = "state_IN_WORK";//在哪个状态增加流程
            String policy = "policy_Document";//哪个Policy上面
            String routeDescription = "testCreateRoute";//流程描述
            String routeId = createAndStartRoute(context, approveList, objectId,state,policy,routeDescription);
            logger.info("routeId:{}",routeId);
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
            throw e;
        }
    }
    /**
     * @description 测试一个流程关联多个对象的方法，对象必须是同一种类型
     * @author caipan
     * @param[1] context
     * @param[2] args
     * @throws

     * @time 2024/1/30 16:44
     */
    public void testCreateRouteMultiple(Context context, String[] args) throws Exception {
        try {
            StringList objectList = new SelectList();
            objectList.add("23628.17342.22328.61976");
            objectList.add("23628.17342.22328.62447");
            MapList approveList = new MapList();//组装审批人员
            String personId = args[0];;//人员ID
            Map nReceiverMap = (Map) getMap(personId, "testApprove");//设置审批信息，testApprove 任务的标题
            nReceiverMap.put(DomainObject.ATTRIBUTE_ROUTE_SEQUENCE, "1");//设置审批顺序
            approveList.add(nReceiverMap);
            String state = "state_FROZEN";//在哪个状态增加流程
            String policy = "policy_Document";//哪个Policy上面
            String routeDescription = "testCreateRoute";//流程描述
            String routeId = createAndStartRouteMultipleObject(context, approveList, objectList,state,policy,routeDescription);
            logger.info("routeId:{}",routeId);
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
            throw e;
        }
    }
    /**
     * @description
     * @author caipan
     * @param[1] context
     * @param[2] mlApprovers
     * @param[3] icdId
     * @param[4] state state_InObsoleting
     * @param[5] policy policy_NioICDProcess
     * @param[6] routeDescription ICD Process Approve
     * @throws

     * @time 2024/1/5 13:27
     */
    public String createAndStartRoute(Context context, MapList mlApprovers, String icdId,String state,String policy,String routeDescription) throws Exception {
        String sCurrentUser = context.getUser();

        //考虑是否委托功能
        StringList selList = new StringList();
        selList.add("attribute[Absence Delegate]");
        selList.add("attribute[Absence End Date]");
        selList.add("attribute[Absence Start Date]");
        DomainObject personObj = DomainObject.newInstance(context);
        logger.info("考虑是否委托功能");
        for(int i=0;i<mlApprovers.size();i++){
            Map map = (Map)mlApprovers.get(i);
            String id = UIUtil.getValue(map, DomainConstants.SELECT_ID);
            personObj.setId(id);
            Map infoMap = personObj.getInfo(context, selList);
            String Delegate= UIUtil.getValue(infoMap, "attribute[Absence Delegate]");
            String StartDate= UIUtil.getValue(infoMap, "attribute[Absence Start Date]");
            String EndDate= UIUtil.getValue(infoMap, "attribute[Absence End Date]");
            if(UIUtil.isNotNullAndNotEmpty(Delegate)&&UIUtil.isNotNullAndNotEmpty(StartDate)&&UIUtil.isNotNullAndNotEmpty(EndDate)){
                logger.info("有一个符号要求");
                if(compileDate(context,StartDate,EndDate)){//时间符合要求
                    String DelegateId = PersonUtil.getPersonObjectID(context, Delegate);
                    BusinessObject bus = new BusinessObject(DelegateId);
                    if(bus.exists(context)){
                        map.put(DomainObject.SELECT_ID, DelegateId);
                    }
                }
            }
        }


        try {
            String sRouteId = "";
            if (mlApprovers != null && !mlApprovers.isEmpty()) {
                DomainObject domObj = DomainObject.newInstance(context, icdId);
                StringList objSel = new StringList();
                objSel.addElement(DomainObject.SELECT_ID);
                StringList relSel = new StringList();
                relSel.addElement(DomainRelationship.SELECT_ID);

                MapList list = domObj.getRelatedObjects(context,
                        "Object Route",     // relationship pattern
                        DomainConstants.TYPE_ROUTE,                                             // object pattern
                        objSel,                         // object selects
                        relSel,                         // relationship selects
                        false,                                                 // to direction
                        true,                                                 // from direction
                        (short) 1,                                             // recursion level
                        "current != Complete",                                                 // object where clause
                        null);
                for (int i = 0; i < list.size(); i++) {
                    Map routeMap = (Map) list.get(i);
                    String routeRelId = (String) routeMap.get(DomainRelationship.SELECT_ID);
                    DomainRelationship.disconnect(context, routeRelId);
                }
                sRouteId = createRoute(context,
                        mlApprovers,
                        icdId,
                        state,
                        policy,
                        routeDescription,
                        sCurrentUser,
                        JF_PLMConstants_mxJPO.ATTRIBUTE_ROUTE_COMPLETION_ACTION_RANGE_PROMOTE_CONNECTED_OBJECT,
                        JF_PLMConstants_mxJPO.STRING_All);
            }
            if (sRouteId != null && !DomainObject.EMPTY_STRING.equals(sRouteId)) {
                startRoute(context, icdId, state);
            } else {
                throw new Exception("No Approvers and no route id.");
            }
            return sRouteId;
        } catch (Exception e) {
            throw e;
        }
    }
    /*
     * @description: 创建流程不Promote对象
     * @author: caipan
     * @date:
     * @param: * @param[1] context
     * @param[2] mlApprovers
     * @param[3] icdId
     * @param[4] state
     * @param[5] policy
     * @param[6] routeDescription
     * @return:
     **/
    public String createAndStartRouteForNotify(Context context, MapList mlApprovers, String icdId,String state,String policy,String routeDescription) throws Exception {
        String sCurrentUser = context.getUser();
        try {
            String sRouteId = "";
            if (mlApprovers != null && !mlApprovers.isEmpty()) {
                DomainObject domObj = DomainObject.newInstance(context, icdId);
                StringList objSel = new StringList();
                objSel.addElement(DomainObject.SELECT_ID);
                StringList relSel = new StringList();
                relSel.addElement(DomainRelationship.SELECT_ID);

                MapList list = domObj.getRelatedObjects(context,
                        "Object Route",     // relationship pattern
                        DomainConstants.TYPE_ROUTE,                                             // object pattern
                        objSel,                         // object selects
                        relSel,                         // relationship selects
                        false,                                                 // to direction
                        true,                                                 // from direction
                        (short) 1,                                             // recursion level
                        "current != Complete",                                                 // object where clause
                        null);
                for (int i = 0; i < list.size(); i++) {
                    Map routeMap = (Map) list.get(i);
                    String routeRelId = (String) routeMap.get(DomainRelationship.SELECT_ID);
                    DomainRelationship.disconnect(context, routeRelId);
                }
                sRouteId = createRoute(context,
                        mlApprovers,
                        icdId,
                        state,
                        policy,
                        routeDescription,
                        sCurrentUser,
                        JF_PLMConstants_mxJPO.ATTRIBUTE_ROUTE_COMPLETION_ACTION_RANGE_Notify_Route_Owner,
                        JF_PLMConstants_mxJPO.STRING_All);
            }
            if (sRouteId != null && !DomainObject.EMPTY_STRING.equals(sRouteId)) {
                startRoute(context, icdId, state);
            } else {
                throw new Exception("No Approvers and no route id.");
            }
            return sRouteId;
        } catch (Exception e) {
            throw e;
        }
    }
    /**
     * @description 创建一个流程，关联多个对象
     * @author caipan
     * @param[1] context
     * @param[2] mlApprovers
     * @param[3] icdList
     * @param[4] state
     * @param[5] policy
     * @param[6] routeDescription
     * @throws

     * @time 2024/1/30 16:46
     */
    public String createAndStartRouteMultipleObject(Context context, MapList mlApprovers, StringList icdList,String state,String policy,String routeDescription) throws Exception {
        String sCurrentUser = context.getUser();
        try {
            //如果存在流程，就先断开流程
            if (mlApprovers != null && !mlApprovers.isEmpty()) {
                boolean isPush = false;
                for (int j = 0; j < icdList.size(); j++) {
                    try {
                        ContextUtil.pushContext(context);
                        isPush = true;
                        DomainObject domObj = DomainObject.newInstance(context, icdList.get(j));
                        StringList objSel = new StringList();
                        objSel.addElement(DomainObject.SELECT_ID);
                        StringList relSel = new StringList();
                        relSel.addElement(DomainRelationship.SELECT_ID);
                        MapList list = domObj.getRelatedObjects(context,
                                "Object Route",     // relationship pattern
                                DomainConstants.TYPE_ROUTE,                                             // object pattern
                                objSel,                         // object selects
                                relSel,                         // relationship selects
                                false,                                                 // to direction
                                true,                                                 // from direction
                                (short) 1,                                             // recursion level
                                "current != Complete",                                                 // object where clause
                                null);
                        for (int i = 0; i < list.size(); i++) {
                            Map routeMap = (Map) list.get(i);
                            String routeRelId = (String) routeMap.get(DomainRelationship.SELECT_ID);
                            DomainRelationship.disconnect(context, routeRelId);
                        }
                    }catch (Exception e){
                        logger.error("disconnection Route error:{}",e.getMessage());
                    }finally {
                        if(isPush) {
                            ContextUtil.popContext(context);
                        }
                    }
                }
            }
            String sRouteId = "";
            if (mlApprovers != null && !mlApprovers.isEmpty()) {
                sRouteId = createRouteForMultipleObject(context,
                        mlApprovers,
                        icdList,
                        state,
                        policy,
                        routeDescription,
                        sCurrentUser,
                        JF_PLMConstants_mxJPO.ATTRIBUTE_ROUTE_COMPLETION_ACTION_RANGE_PROMOTE_CONNECTED_OBJECT,
                        JF_PLMConstants_mxJPO.STRING_All);
            }
            if (sRouteId != null && !DomainObject.EMPTY_STRING.equals(sRouteId)) {
                startRoute(context, icdList.get(0), state);
            } else {
                throw new Exception("No Approvers and no route id.");
            }
            return sRouteId;
        } catch (Exception e) {
            throw e;
        }
    }
    public static Map getMap(String objectId, String title) {
        if (objectId != null && !"".equals(objectId)) {
            Map map = new HashMap();
            map.put("id", objectId);
            map.put("Route Instructions", "Route task");
            map.put("Route Action", "Approve");
            map.put("Title", title);//任务标题
            map.put("Allow Delegation", "true");//是否让委托
            map.put("Route Sequence", "1");//任务顺序
            map.put("Parallel Node Procession Rule", "All");

            return map;
        }
        return null;
    }

    public static MapList mlMembers(Context context, String icdId) throws Exception {
        MapList mapList = new MapList();
        try {
            DomainObject icdObj = DomainObject.newInstance(context, icdId);
            StringList objSel = new StringList();
            objSel.add(DomainConstants.SELECT_ID);
            objSel.add(DomainConstants.SELECT_NAME);
            MapList personPackagerList = icdObj.getRelatedObjects(context,
                    "NioICDPackager",     // relationship pattern
                    DomainConstants.TYPE_PERSON,                                             // object pattern
                    objSel,                         // object selects
                    null,                         // relationship selects
                    false,                                                 // to direction
                    true,                                                 // from direction
                    (short) 1,                                             // recursion level
                    null,                                                 // object where clause
                    null);

            for (int i = 0; i < personPackagerList.size(); i++) {
                Map personMap = (Map) personPackagerList.get(i);
                String personId = (String) personMap.get(DomainConstants.SELECT_ID);
                Map packagerMap = (Map) getMap(personId, "Packager");
                mapList.add(packagerMap);
            }
            MapList personReceiverList = icdObj.getRelatedObjects(context,
                    "NioICDReceiver",     // relationship pattern
                    DomainConstants.TYPE_PERSON,                                             // object pattern
                    objSel,                         // object selects
                    null,                         // relationship selects
                    false,                                                 // to direction
                    true,                                                 // from direction
                    (short) 1,                                             // recursion level
                    null,                                                 // object where clause
                    null);
            for (int i = 0; i < personReceiverList.size(); i++) {
                Map personMap = (Map) personReceiverList.get(i);
                String personId = (String) personMap.get(DomainConstants.SELECT_ID);
                Map nReceiverMap = (Map) getMap(personId, "Receiver");
                nReceiverMap.put(DomainObject.ATTRIBUTE_ROUTE_SEQUENCE, "2");
                mapList.add(nReceiverMap);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
        return mapList;
    }
    public static StringList getStringListIds(MapList ml) {

        StringList sl = new StringList();
        for (Iterator iter = ml.iterator(); iter.hasNext();) {
            Map map = (Map) iter.next();
            String sObjId = (String) map.get(DomainObject.SELECT_ID);
            sl.addElement(sObjId);
        }

        return sl;
    }
    private static Map getPerson(MapList mlPersons, String sPersonId) {
        Map map = null;
        for (int i = 0; i < mlPersons.size(); i++) {
            map = (Map) mlPersons.get(i);
            String sPId = (String) map.get(DomainConstants.SELECT_ID);
            sPId = sPId.trim();
            if (sPId.equals(sPersonId)) {
                mlPersons.remove(i);
                i--;
                break;
            }
        }
        return map;
    }


    public void createRoute(Context context,String[] args) throws Exception{
        com.matrixone.apps.common.Person person = (com.matrixone.apps.common.Person)DomainObject.newInstance(context,DomainConstants.TYPE_PERSON);
        Route route = (Route)DomainObject.newInstance(context,DomainConstants.TYPE_ROUTE);
        String revision = new Policy(DomainConstants.POLICY_ROUTE).getSequence(context);
        final boolean flag = true ;// 是否自动命名
        String routeId ;
        if(flag) {
            String typeAlias = FrameworkUtil.getAliasForAdmin(context, "type", DomainConstants.TYPE_ROUTE, true);
            String policyAlias = FrameworkUtil.getAliasForAdmin(context, "policy", DomainConstants.POLICY_ROUTE, true);
            routeId = FrameworkUtil.autoName(context, typeAlias, "", policyAlias);
        }else{
            String name = args[0];
                route.createObject(context, DomainConstants.TYPE_ROUTE, name, revision, DomainConstants.POLICY_ROUTE, null);
                routeId = route.getObjectId(context);
            }
        Map routeDetails = new HashMap();
        routeDetails.put("routeBasePurpose", "Standard");
        BusinessObject personObject = (BusinessObject)person.getPerson(context);
        route.connect(context,new RelationshipType(DomainObject.RELATIONSHIP_PROJECT_ROUTE),true, personObject);
    }


    /**
     * 流程停止后退回关联对象状态
     **
     * @param context
     * @param args 流程ID、修改属性名称和修改属性值
     * @throws Exception
     * @author caipan
     * @date 2026/7/16 16:13
     */
    public void routeStopReturnObjectState(Context context,String[] args) throws Exception{
        String objectId = args[0];
        String modAttrName = args[1];
        String modAttrValue = args[2];
        logger.info("routeStopReturnObjectState start: {} modAttrValue :{}" ,objectId,modAttrValue);
        StringList objSel = new StringList();
        objSel.add(DomainConstants.SELECT_ID);
        objSel.add(SELECT_TYPE);
        objSel.add(SELECT_NAME);
        objSel.add(DomainConstants.SELECT_CURRENT);
        objSel.add(DomainConstants.SELECT_ATTRIBUTE_TITLE);
        objSel.add(SELECT_OWNER);
        boolean isPush = false;
        try{
            StringList relSel = new StringList();
            if ("Route Status".equalsIgnoreCase(modAttrName) && "Stopped".equalsIgnoreCase(modAttrValue)) {
                DomainObject routeObj = DomainObject.newInstance(context);
                routeObj.setId(objectId);
                MapList reviewList = routeObj.getRelatedObjects(context,
                        DomainConstants.RELATIONSHIP_OBJECT_ROUTE,     // relationship pattern
                        DomainConstants.QUERY_WILDCARD,                                             // object pattern
                        objSel,                         // object selects
                        null,                         // relationship selects
                        true,                                                 // to direction
                        false,                                                 // from direction
                        (short) 1,                                             // recursion level
                        null,                                                 // object where clause
                        null);
                //审核对象列表
                Map temp = new HashMap();
                DomainObject reviewObj = DomainObject.newInstance(context);
                String reviewId = "";
                String title = "";
                String name = "";
                String current = "";
                for (int i = 0; i < reviewList.size(); i++) {
                    temp = (Map) reviewList.get(i);
                    String type = UIUtil.getValue(temp, SELECT_TYPE);
                    reviewId = UIUtil.getValue(temp, DomainConstants.SELECT_ID);
                    title = UIUtil.getValue(temp, DomainConstants.SELECT_ATTRIBUTE_TITLE);
                    name = UIUtil.getValue(temp, SELECT_NAME);
                    current = UIUtil.getValue(temp, SELECT_CURRENT);
                    reviewObj.setId(reviewId);
                    String delayAtt = reviewObj.getAttributeValue(context, "JFDAIsDelay");
                    if("JFDR".equalsIgnoreCase(type) || "JF_DRW".equalsIgnoreCase(type) || "Document".equals(type)){
                        ContextUtil.pushContext(context);
                        isPush = true;
                        reviewObj.demote(context);
                        if ("Document".equals(type)) {
                            //20260827 update by caipan 单文档审批驳回后释放Change Control，恢复工作中文档的正常维护权限。
                            JF_DocumentTrigger_mxJPO.removeDocumentChangeControlForDocuments(
                                    context, StringList.create(reviewId));
                        }
                        if("JFDR".equalsIgnoreCase(type)) {
                            //移除掉Change Control
                            JF_DR_mxJPO dr = new JF_DR_mxJPO();
                            dr.drRefuseRemoveChangeControl(context, new String[]{reviewId});
                        }
                    }
                    //延期流程不用降级
                    if("JFDA".equalsIgnoreCase(type) && "FALSE".equals(delayAtt)){
                        ContextUtil.pushContext(context);
                        isPush = true;
                        reviewObj.demote(context);
                    }

                    relSel.add(DomainRelationship.SELECT_ID);
                    relSel.add("attribute[Route Base State]");
                    //延期流程降级后修改属性并且断开延期流程关系
                    if("JFDA".equalsIgnoreCase(type) && "TRUE".equals(delayAtt)){
                        //20260819 update by caipan 第二次延期驳回时恢复首次延期时间
                        String delayCount = reviewObj.getAttributeValue(context, "JFDADelayCount");
                        if (UIUtil.isNullOrEmpty(delayCount) || "0".equals(delayCount)) {
                            reviewObj.setAttributeValue(context,"JFDAIsDelay","FALSE");
                        }
                        if ("1".equals(delayCount)) {
                            String extensionTimeBak = reviewObj.getAttributeValue(context, "JFDAExtensionTimeBak");
                            reviewObj.setAttributeValue(context, "JFDAExtensionTime", extensionTimeBak);
                        } else {
                            reviewObj.setAttributeValue(context,"JFDAExtensionTime","");
                        }
                        MapList mapList = reviewObj.getRelatedObjects(context, "Object Route", "Route", objSel, relSel, false, true, (short) 0, "", "", 0);
                        if(!mapList.isEmpty()){
                            for (int j = 0; j < mapList.size(); j++) {
                                Map map = (Map) mapList.get(j);
                                String routeBaseState = (String) map.get("attribute[Route Base State]");
                                String relatedRouteId = (String) map.get(DomainConstants.SELECT_ID);
                                //20260819 update by caipan 只断开本次驳回延期流程与DA的关系
                                if("state_Implement".equals(routeBaseState) && objectId.equals(relatedRouteId)){
                                    String connectId = (String) map.get("id[connection]");
                                    ContextUtil.pushContext(context);
                                    DomainRelationship.disconnect(context,connectId);
                                    ContextUtil.popContext(context);
                                    break;
                                }
                            }
                        }
                    }

                    //单独处理 update by ljr
                    if("JFDataOutSource".equalsIgnoreCase(type)){
                        MqlUtil.mqlCommand(context,false,"mod bus " + reviewId + " current In_Work;",true);
                    }

                    //单独处理 update by ljr
                    if("JFSnapshot".equalsIgnoreCase(type)){
                        MqlUtil.mqlCommand(context,false,"mod bus " + reviewId + " current DRAFT;",true);
                    }
                    //单独处理 update by ljr
                    if("JFColorMatrix".equalsIgnoreCase(type)){
                        MqlUtil.mqlCommand(context,false,"mod bus " + reviewId + " current Create;",true);
                    }

                    if("JFESOReview".equalsIgnoreCase(type)){
                        MqlUtil.mqlCommand(context,false,"mod bus " + reviewId + " current Create;",true);
                    }

                    if("JFECR".equalsIgnoreCase(type)||"JFFormalECR".equalsIgnoreCase(type)){
                        ContextUtil.pushContext(context);
                        isPush = true;
                        reviewObj.setState(context,"Create");
                    }
                    // add by chenyan 2025/05/07 新增JFNewECR 退回到草稿
                    if("JFNewECR".equalsIgnoreCase(type)){
                        ContextUtil.pushContext(context);
                        isPush = true;
                        reviewObj.setState(context,"Create");
                    }
                    if("JFConfigTableRoute".equalsIgnoreCase(type)){
                        String productConfigId = reviewObj.getInfo(context, "from[JFConfigTableRoute2ProductConfigTable].to.id");
                        String servicePartsListId = reviewObj.getInfo(context, "from[JFConfigTableRoute2ServicePartsList].to.id");
                        ContextUtil.pushContext(context);
                        try {
                            if (UIUtil.isNotNullAndNotEmpty(productConfigId)) {
                                MqlUtil.mqlCommand(context, false, "mod bus " + productConfigId + " current InWork;", true);
                            }
                            if (UIUtil.isNotNullAndNotEmpty(servicePartsListId)) {
                                MqlUtil.mqlCommand(context, false, "mod bus " + servicePartsListId + " current InWork;", true);
                            }
                            MqlUtil.mqlCommand(context, false, "mod bus " + reviewId + " current InWork;", true);
                        } finally {
                            ContextUtil.popContext(context);
                        }
                    }
                    // add by chenyan 2025/08/20 新增JFPartList 退回到工作中

                    if(JF_PLMConstants_mxJPO.TYPE_JFPartList.equalsIgnoreCase(type)){
                        ContextUtil.pushContext(context);
                        isPush = true;
                        reviewObj.setState(context,"InWork");
                        //获取生成EBOM文档
                        StringBuilder sbSelectMQL = new StringBuilder();
                        sbSelectMQL.append("print bus ");
                        sbSelectMQL.append(reviewId);
                        sbSelectMQL.append(" select from[Reference Document|attribute[Project Role]=='EBOMFile'].to.id dump");
                        String strEBOMDocId = MqlUtil.mqlCommand(context, Boolean.FALSE, sbSelectMQL.toString(), Boolean.TRUE);
                        logger.info("strDocId:{}",strEBOMDocId);
                        //将partlist关联的EBOM文档删除
                        if (UIUtil.isNotNullAndNotEmpty(strEBOMDocId)){
                            DomainObject.deleteObjects(context,new String[]{strEBOMDocId});
                        }
                    }     //add by caipan JF_PCR 20251126
                    if(JF_PLMConstants_mxJPO.Type_JF_PCR.equalsIgnoreCase(type)){
                        ContextUtil.pushContext(context);
                        isPush = true;
                        if("IN_Approve".equalsIgnoreCase(current)){//批准
                            reviewObj.setState(context,"Draft");
                        }else if("IN_Evaluation".equalsIgnoreCase(current)){//评估
//                            reviewObj.setState(context,"Draft");
                        }else if("Verification".equalsIgnoreCase(current)){//验证
                            //并通知PCR发起人
                            Map requestMap = new HashMap();
                            requestMap.put("objectId",reviewId);
                            requestMap.put("taskName",title);
                            requestMap.put("connectName",name);
                            String email =  JF_NotificationUtils_mxJPO.getPersonEmail(context, UIUtil.getValue(temp, SELECT_OWNER), null);
                            requestMap.put("email",email);
                            JF_SendEmailUtils_mxJPO.sendProjectTasRejectEmailToPortal(context, requestMap);//发邮件
                            //是否还需要处理其他逻辑
                            //把断点切换时间清空
                            reviewObj.setAttributeValue(context,JF_PLMConstants_mxJPO.attr_JF_BreakpointSwitchingDate,"");
                        }else if("Execution".equalsIgnoreCase(current)){//更改执行

                        }
                    }  if("JFDelayedFiling".equalsIgnoreCase(type)){
                        MqlUtil.mqlCommand(context,false,"mod bus " + reviewId + " current Draft;",true);
                        DomainObject Obj = DomainObject.newInstance(context,reviewId);
                        StringList lists = Obj.getInfoList(context,"from["+JF_PLMConstants_mxJPO.RELATIONSHIP_JFDELAYEDFILING2TASK+"].to.id");
                        for(int j=0;j<lists.size();j++) {
                            DomainObject taskObj = DomainObject.newInstance(context, lists.get(j));
                            String tasktype = taskObj.getInfo(context,SELECT_TYPE);
                            if("JF_ECOTask".equalsIgnoreCase(tasktype)) {//ECO任务
                                taskObj.setAttributeValue(context, JF_PLMConstants_mxJPO.ATTR_WHETHERTOPOSTPONE, "No");//设置延期
                            }else if("JF_DATask".equalsIgnoreCase(tasktype)) {
                                taskObj.setAttributeValue(context, JF_PLMConstants_mxJPO.ATTR_DAWHETHERTOPOSTPONE, "No");//设置延期
                            }
//                            taskObj.setAttributeValue(context, JF_PLMConstants_mxJPO.ATTR_WHETHERTOPOSTPONE, "No");//设置延期
                        }
                    }
                    // 20260721 add by ljr 增加标准件发布的驳回流程
                    if("JFSPartsApplication".equalsIgnoreCase(type)){
                        MqlUtil.mqlCommand(context,false,"mod bus " + reviewId + " current Draft;",true);
                        //移除掉Change Control
                        JF_FasteningPiece_mxJPO.removeChangeInterface(context, new String[]{reviewId});
                    }
                    if("JFBatchDocumentReview".equalsIgnoreCase(type)){
                        //撤回签发表总工审批后，清除本次选择的总工人员并退回创建状态。
                        StringList docIds = reviewObj.getInfoList(
                                context,
                                "from[Reference Document].to.id");
                        for (String docId : docIds) {
                            MqlUtil.mqlCommand(context, false,"mod bus "+ docId +" current 'IN_WORK'", true);
                        }
                        //20260827 update by caipan 批量审批驳回后统一释放全部关联文档的Change Control。
                        JF_DocumentTrigger_mxJPO.removeDocumentChangeControlForDocuments(context, docIds);
                        MqlUtil.mqlCommand(context,false,"mod bus " + reviewId + " current InWork;",true);
                    }
                }
            }
            logger.info("routeStopReturnObjectState end");
        }catch (Exception e){
            logger.error("demote Object error routeStopReturnObjectState:{}",e.getMessage());
        }finally {
            if (isPush){
                ContextUtil.popContext(context);
                logger.info("Route user:{}",context.getUser());
            }
        }
    }

    /*
     * @description:日期比较大小
     * @author: caipan
     * @date: 2025/4/27 14:32:40
     * @param: * @param[1] context
     * @param[2] start
     * @param[3] end
     * @return:
     **/
 public boolean compileDate(Context context,String start ,String end){
     try {
         DateTimeFormatter formatter = DateTimeFormatter.ofPattern("M/d/yyyy h:mm:ss a");
        DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("M/d/yyyy");
         LocalDateTime currentDateTime = LocalDate.now().atStartOfDay();
         LocalDate today = LocalDate.now();
         LocalDateTime endOfDay = LocalDateTime.of(today, LocalTime.MAX);
         logger.info("currentDateTime:{}",currentDateTime);
        //开始时间
         LocalDateTime startDate = LocalDateTime.parse(start, formatter);
        //结束时间
         LocalDateTime endDate = LocalDateTime.parse(end, formatter);
         //开始时间小于等于当前时间、结束时间大于等于当前时间
         if (startDate.isBefore(endOfDay) && endDate.isAfter(currentDateTime)) {
             logger.info("开始时间小于等于当前时间、结束时间大于等于当前时间");
             return true;
         }else{
             logger.info("时间不符合要求");
         }
     }catch (Exception e){
         e.printStackTrace();
     }
     return false;
 }

}
