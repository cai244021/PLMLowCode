<%--  JF_SubIFReplaceProcess.jsp -
   Copyright (c) 1992-2020 Dassault Systemes.
   All Rights Reserved.
   This program contains proprietary and trade secret information of MatrixOne,
   Inc.  Copyright notice is precautionary only
   and does not evidence any actual or intended publication of such program

   static const char RCSID[] = "$Id: emxCommonDocumentCheckinProcess.jsp.rca 1.22 Wed Oct 22 16:18:50 2008 przemek Experimental przemek $"
--%>
<%@ page import="matrix.db.JPO" %>
<%@ page import="org.apache.commons.fileupload.DiskFileUpload" %>
<%@ page import="org.slf4j.Logger" %>
<%@ page import="org.slf4j.LoggerFactory" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="com.google.gson.Gson" %>
<%@ page import="com.matrixone.apps.domain.DomainObject" %>
<%@ page import="com.matrixone.apps.common.util.ComponentsUIUtil" %>
<%@ page import="matrix.util.StringList" %>
<%@ page import="com.matrixone.apps.domain.util.MapList" %>
<%@ page import="com.matrixone.apps.domain.DomainConstants" %>
<%@ page import="com.dassault_systemes.dssourcing.common.util.UIUtil" %>
<%@ page import="com.matrixone.apps.domain.util.EnoviaResourceBundle" %>
<%@ page import="com.matrixone.apps.domain.DomainRelationship" %>
<%@ page import="static com.matrixone.apps.domain.DomainConstants.SELECT_NAME" %>
<%@ page import="static com.matrixone.apps.domain.DomainConstants.context1" %>
<%@include file = "../emxUICommonAppInclude.inc"%>
<%@ include file = "../emxUICommonHeaderBeginInclude.inc" %>
<%@include file = "../common/emxUIConstantsInclude.inc"%>
<%@ include file = "../emxJSValidation.inc"%>
<%@include file = "../components/emxComponentsUtil.inc"%>
<%@include file = "../components/emxComponentsSetCompanyKeyInRPE.inc"%>
<jsp:useBean id="formBean" scope="page" class="com.matrixone.apps.common.util.FormBean" />
<jsp:useBean id="requestBean" scope="page" class="com.matrixone.apps.domain.util.Request"/>
<script language="JavaScript" src="../common/scripts/emxUIConstants.js" type="text/javascript"></script>
<script language="JavaScript" src="../common/scripts/emxUICore.js" type="text/javascript"></script>
<%!
        private static final Logger _logger = LoggerFactory.getLogger("JF_DuplicateESOTaskProcess.jsp");
%>
<%
        String parentId = (String)emxGetParameter(request, "parentId");
        String strObjectId = (String)emxGetParameter(request, "objectId");
        String phaseId = (String)emxGetParameter(request, "phaseId");
        String projectId = (String)emxGetParameter(request, "projectId");
        formBean.processForm(session, request);
        String selectPhaseIds = (String) formBean.getElementValue("selectPhaseIds");
        _logger.info("dddd；{}", selectPhaseIds);
        //获取项目中的整椅经理
        //拿取项目的成员以及关系属性project role
        DomainObject projectObject = DomainObject.newInstance(context, projectId);
        MapList mapList = projectObject.getRelatedObjects(
                context,
                DomainRelationship.RELATIONSHIP_MEMBER,
                DomainConstants.TYPE_PERSON,
                new StringList(SELECT_NAME),
                new StringList(),
                false,
                true,
                (short) 1, // recursion level
                "", //object where clause
                "attribute[Project Role]=='Chair manager'", //relationship where clause
                0
        );
        String chairManager = DomainConstants.EMPTY_STRING;
        if (!mapList.isEmpty()) {
                Map map = (Map) mapList.get(0);
                chairManager = UIUtil.getValue(map, DomainConstants.SELECT_NAME);
        }
        //获取模板中的第一层阶段的name
        StringList tempPhaseList  = JPO.invoke(context, "JF_ESO", null, "getTemplateESOTaskFirstPhases", new String[]{DomainConstants.SELECT_NAME}, StringList.class);
        _logger.info("tempPhaseList:{}", tempPhaseList);
        _logger.info("chairManager:{}", chairManager);
        //校验
        //1. 先校验当前项目下的Phase是否和ESO模版的Phase匹配--根据Name进行匹配   不匹配报错，告诉哪个Phase匹配不上
        //2. 校验选择的阶段下是否含有ESO名字的任务
        DomainObject domainObject = DomainObject.newInstance(context);
        StringList errorList = new StringList();
        StringList errorESOList = new StringList();
        String[] split = selectPhaseIds.split(",");
        StringList errorPhaseList = new StringList();
        _logger.info("selectPhaseIds:{}", selectPhaseIds);
        HashMap<String, String> phaseAndEsoMap = new HashMap<>();
        for (int i = 0; i < split.length; i++) {
                String id = split[i];
                domainObject.setId(id);
                String name = domainObject.getInfo(context, DomainObject.SELECT_NAME);
                _logger.info("name:{}", name);
                if (!tempPhaseList.contains(name)) {
                        errorList.add(name);
                }
                //获取阶段的任务
                MapList  allTask = domainObject.getRelatedObjects(
                        context, DomainRelationship.RELATIONSHIP_SUBTASK,"Phase,Task",
                        StringList.create(DomainConstants.SELECT_NAME,DomainConstants.SELECT_ID, DomainConstants.SELECT_TYPE),
                        new StringList(),
                        false,
                        true,
                        (short)0,
                        "","",0);
                String esoTaskId = DomainConstants.EMPTY_STRING;
                for (int i1 = 0; i1 < allTask.size(); i1++) {
                        Map map = (Map) allTask.get(i1);
                        String strName = UIUtil.getValue(map, DomainConstants.SELECT_NAME);
                        String strId = UIUtil.getValue(map, DomainConstants.SELECT_ID);
                        String strType = UIUtil.getValue(map, DomainConstants.SELECT_TYPE);
                        _logger.info("strName:{}", strName);

                        if (DomainConstants.TYPE_TASK.equalsIgnoreCase(strType) && ((strName.equalsIgnoreCase("ESO") || strName.equalsIgnoreCase("CS") || strName.equalsIgnoreCase("ESO\u7b7e\u53d1")))) {
                                esoTaskId = strId;
                                //如果有多个ESO的task,只需要拿取第一个就好,其他的不管！！！
                                break;
                        }
                }
                System.out.println("任务id:" +  esoTaskId);
                _logger.info("esoTaskId:{}", esoTaskId);

                if (UIUtil.isNotNullAndNotEmpty(esoTaskId)) {
                        phaseAndEsoMap.put(name, esoTaskId);
                        //获取判断ESO任务是否有整椅经理的委派人
                        domainObject.setId(esoTaskId);
                        StringList assignedPersonList = domainObject.getInfoList(context, "to[Assigned Tasks].from.name");
                        assignedPersonList.add(domainObject.getInfo(context, DomainConstants.SELECT_OWNER));
                        _logger.info("assignedPersonList:{}", assignedPersonList);

                        int i1 = 0;
                        for (; i1 < assignedPersonList.size(); i1++) {
                                String person = assignedPersonList.get(i1);
                                if (chairManager.equalsIgnoreCase(person)) {
                                     break;
                                }
                        }
                        if (i1 == assignedPersonList.size()) {
                                errorPhaseList.add(name);
                        }
                } else {
                        errorESOList.add(name);
                }
        }
        StringBuilder stringBuilder = new StringBuilder();
        if (!errorList.isEmpty()) {
                //报错
                String strMess = EnoviaResourceBundle.getProperty(context, "emxProgramCentralStringResource", context.getLocale(), "emxProgramCentral.Mess.PhaseAbsent");
                strMess = strMess.replace("1", tempPhaseList.join(",")).replace("2", errorList.join(","));
                stringBuilder.append(strMess);
        }
        if (!errorESOList.isEmpty()) {
                //报错
                String strMess = EnoviaResourceBundle.getProperty(context, "emxProgramCentralStringResource", context.getLocale(), "emxProgramCentral.Mess.ESOAbsent");
                strMess = strMess.replace("1", errorESOList.join(","));
                stringBuilder.append(strMess);
        }
        if (!errorPhaseList.isEmpty()) {
                //报错
                String strMess = EnoviaResourceBundle.getProperty(context, "emxProgramCentralStringResource", context.getLocale(), "emxProgramCentral.Mess.NoAccess");
                strMess = strMess.replace("1", errorPhaseList.join(","));
                stringBuilder.append(strMess);
        }
        String mess = stringBuilder.toString();
        if (UIUtil.isNotNullAndNotEmpty(mess)) {
%>
<script language="Javascript">
        alert("<%=mess%>");
        getTopWindow().closeWindow();
</script>
<%
                return;
        }
        HashMap params = new HashMap();
        params.put("parentId", parentId);  //当前点击复制ESO任务页面的对象id
        params.put("phaseId", phaseId); //当前选择任务所属于的阶段id
        params.put("objectId", strObjectId);    //选择的ESO任务id
        params.put("projectId", projectId);     //所属的项目id
        params.put("selectPhaseIds", selectPhaseIds); //点击选择复制的阶段id
        params.put("phaseAndEsoMap", phaseAndEsoMap);  //点击选择复制的阶段中的ESO 任务的id；复制的时候，就是这个复制到这个任务下
        _logger.info("objectId:{}", strObjectId);
        _logger.info("selectPhaseIds:{}", selectPhaseIds);
        _logger.info("phaseAndEsoMap:{}", phaseAndEsoMap);

        //返回信息
        //开始赋值任务
        Boolean res = (Boolean) JPO.invoke(context, "JF_ESO", JPO.packArgs(params), "duplicateESOTaskFilter", JPO.packArgs(params), Boolean.class);
        _logger.info("res:{}", res);
        String strMess = EnoviaResourceBundle.getProperty(context, "emxProgramCentralStringResource", context.getLocale(), "emxProgramCentral.Mess.CopySuccess");
        if (!res) {
                strMess = EnoviaResourceBundle.getProperty(context, "emxProgramCentralStringResource", context.getLocale(), "emxProgramCentral.Mess.CopyFailed");
        }
        _logger.info("--------------------copy end -----------------------------");
%>
<html>
<body>
<script>
        <%--var parentDoc = getTopWindow().getWindowOpener().document;--%>
        <%--console.log(parentDoc.getElementById("JFECRQQFileId").value);--%>
        <%--parentDoc.getElementById("JFECRQQFileId").value = "<%=res.get("newId")%>";--%>
        <%--parentDoc.getElementById("JFReplace").innerHTML = `<%=res.get("html")%>`;--%>
        alert("<%=strMess%>");
        // alert("");
        getTopWindow().closeWindow();
        // window.top.getWindowOpener().top.refreshTablePage();
        getTopWindow().getWindowOpener().parent.document.location.href = getTopWindow().getWindowOpener().parent.document.location.href;

        // getTopWindow().findFrame(parent.getTopWindow(),"detailsDisplay").location.href = parent.getTopWindow().findFrame(parent.getTopWindow(),"detailsDisplay").location.href.replace("persist=true", "persist=false");
</script>
</body>
</html>
