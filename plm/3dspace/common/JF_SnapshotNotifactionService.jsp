<%@ page import="com.matrixone.apps.domain.DomainObject" %>
<%@ page import="matrix.db.RelationshipType" %>
<%@ page import="com.matrixone.apps.domain.util.FrameworkUtil" %>
<%@ page import="matrix.util.StringList" %>
<%@ page import="com.matrixone.apps.domain.DomainRelationship" %>
<%@ page import="com.matrixone.apps.domain.DomainConstants" %>
<%@ page import="org.slf4j.Logger" %>
<%@ page import="org.slf4j.LoggerFactory" %>
<%@ page import="com.matrixone.apps.domain.util.ContextUtil" %>
<%@ page import="com.matrixone.apps.framework.ui.UIUtil" %>
<%@ page import="com.matrixone.apps.common.util.ComponentsUIUtil" %>
<%@ page import="matrix.db.JPO" %>
<%@ page import="com.matrixone.apps.domain.util.MapList" %>
<%@ page import="static com.matrixone.apps.domain.DomainConstants.TYPE_PERSON" %>
<%@ page import="static com.matrixone.apps.domain.DomainConstants.SELECT_RELATIONSHIP_ID" %>
<%@ page import="static com.matrixone.apps.domain.DomainConstants.*" %>
<%@ page import="java.util.Set" %>
<%@ page import="java.util.HashSet" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.util.HashMap" %>
<!--
    DR添加物理产品
-->
<%@include file = "../common/emxNavigatorInclude.inc"%>
<%@include file = "../common/emxNavigatorTopErrorInclude.inc"%>
<%!
    private static final Logger JF_LOGGER = LoggerFactory.getLogger("JF_SnapshotNotifactionService.jsp");
%>
<%
    boolean isAlert = false ;
    String strObjectId = emxGetParameter(request, "objectId");
    String strJFSTDPerson = emxGetParameter(request, "JFSTDPerson");
    String strForm = emxGetParameter(request, "form");
    JF_LOGGER.info("strObjectId:{}",strObjectId);
    JF_LOGGER.info("strJFSTDPerson:{}",strJFSTDPerson);
    JF_LOGGER.info("strForm:{}",strForm);
    DomainObject bo = DomainObject.newInstance(context, strObjectId);
    StringList typeSelectList = new StringList();
    typeSelectList.add(SELECT_ID);
    StringList relSelectList = new StringList();
    relSelectList.add(SELECT_RELATIONSHIP_ID);
    MapList maps = bo.getRelatedObjects(context, "JFSnapshot2Member", // relationship pattern
            TYPE_PERSON,                                    // object pattern
            typeSelectList,                            // object selects
            relSelectList, // relationship selects
            false,                                        // to direction
            true,                                        // from direction
            (short) 1,                                    // recursion level
            "",                // object where clause
            "",
            (short) 0);
    Map argsMap = new HashMap<>();
    argsMap.put("objectId", strObjectId);
    argsMap.put("JFSTDPerson", strJFSTDPerson);
    String[] argsArr = JPO.packArgs(argsMap);
    //对比原来关系
    String[] personIdArr = strJFSTDPerson.split(",");
    StringList newPersonIdList = StringList.create(personIdArr);
    //需要关联的id集合
    Set connIdSet = new HashSet<String>();
    //需要断开的id集合
    Set disConnRelIdSet = new HashSet<String>();
    StringList alreadyIdList = new StringList();
    if (UIUtil.isNullOrEmpty(strJFSTDPerson)) {
        //清空
        for (int i = 0; i < maps.size(); i++) {
            Map personMap = (Map) maps.get(i);
            String strAlreadyId = (String) personMap.get(SELECT_ID);
            alreadyIdList.add(strAlreadyId);
            String strRelId = (String) personMap.get(SELECT_RELATIONSHIP_ID);
            disConnRelIdSet.add(strRelId);
        }
    } else {
        for (int i = 0; i < maps.size(); i++) {
            Map personMap = (Map) maps.get(i);
            String strAlreadyId = (String) personMap.get(SELECT_ID);
            alreadyIdList.add(strAlreadyId);
            String strRelId = (String) personMap.get(SELECT_RELATIONSHIP_ID);
            if (!newPersonIdList.contains(strAlreadyId)) {
                disConnRelIdSet.add(strRelId);
            }
        }
        for (int i = 0; i < newPersonIdList.size(); i++) {
            String strPersonId = newPersonIdList.get(i);
            if (!alreadyIdList.contains(strPersonId)) {
                connIdSet.add(strPersonId);
            }
        }
    }
    try {
        ContextUtil.startTransaction(context,true);
        //断开
        JF_LOGGER.info("disConnRelIdSet:{}",disConnRelIdSet);
        if (disConnRelIdSet.size() > 0){
            DomainRelationship.disconnect(context,StringList.create(disConnRelIdSet).toStringArray());
        }
        JF_LOGGER.info("connIdSet:{}",connIdSet);
        if (connIdSet.size() > 0) {
            DomainRelationship.connect(context, bo, "JFSnapshot2Member", true, StringList.create(connIdSet).toStringArray());
        }
        if ("JFSnapshotNoticeForm".equals(strForm)){
            //创建评论任务
            Map res = JPO.invoke(context, "JF_Snapshot", null, "createInboxTaskAndSendNotice",argsArr, Map.class);
            String strCode = (String) res.get("code");
            String strMess = (String) res.get("mess");
            isAlert = "404".equals(strCode) ;
%>
<script>
    if (<%=isAlert%>){
        alert("<%=strMess%>")
    }else {
        const openerWin = getTopWindow().getWindowOpener().parent;
        if (openerWin){
            const command1Win = openerWin.findFrame(openerWin,"JFAPPTasksGraphical");
            const command2Win = openerWin.findFrame(openerWin,"AEFLifecycleTaskSignatures");
            const command3Win = openerWin.findFrame(openerWin,"AEFLifecycleApprovals");
            if (command1Win.location.href !== "about:blank"){
                command1Win.location.href = command1Win.location.href ;
            }
            if (command2Win.location.href !== "about:blank"){
                command2Win.location.href = command2Win.location.href ;
            }
            if (command3Win.location.href !== "about:blank"){
                command3Win.location.href = command3Win.location.href ;
            }
        }
    }

</script>
<%
    }
        ContextUtil.commitTransaction(context);
    }catch (Exception e){
        JF_LOGGER.error(e.getMessage());
        ContextUtil.abortTransaction(context);
    }

%>
<%@include file = "../common/emxNavigatorBottomErrorInclude.inc"%>

