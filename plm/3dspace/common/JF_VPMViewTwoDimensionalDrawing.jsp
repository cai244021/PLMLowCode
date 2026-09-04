<%-- JF_ViewDrawingCommand.jsp - used for Checkin of file into Document Object
   Copyright (c) 1992-2020 Dassault Systemes.
   All Rights Reserved.
   This program contains proprietary and trade secret information of MatrixOne,
   Inc.  Copyright notice is precautionary only
   and does not evidence any actual or intended publication of such program

   emxCommonDocumentMultiFileUploadFS.jsp
   static const char RCSID[] = "$Id: emxCommonDocumentCheckinDialogFS.jsp.rca 1.21 Wed Oct 22 16:18:21 2008 przemek Experimental przemek $"
--%>
<%@ page pageEncoding="utf-8" %>
<%@ page import="com.matrixone.apps.domain.DomainConstants" %>
<%@ page import="com.matrixone.enovia.bps.notifications.NotificationService" %>
<%@ page import="com.matrixone.apps.domain.DomainObject" %>
<%@ page import="java.net.URLEncoder" %>
<%@ page import="java.util.Arrays" %>
<%@ page import="com.matrixone.apps.common.util.ComponentsUIUtil" %>
<%@ page import="org.slf4j.LoggerFactory" %>
<%@ page import="org.slf4j.Logger" %>
<%@ include file = "../emxUICommonAppInclude.inc"%>
<%@ include file = "../emxUICommonHeaderBeginInclude.inc" %>
<%@ include file = "../emxJSValidation.inc"%>
<%@include file = "../common/emxUIConstantsInclude.inc"%>
<%@include file = "../components/emxComponentsUtil.inc"%>

<script type="text/javascript" src="../common/scripts/emxUICoreMenu.js"></script>
<script language="javascript" type="text/javascript" src="../common/scripts/emxUICore.js"></script>
<script language="javascript" type="text/javascript" src="../common/scripts/emxUIFormUtil.js"></script>
<script language="javascript" type="text/javascript" src="../common/scripts/emxUIModal.js"></script>
<script language="javascript" type="text/javascript" src="../common/scripts/emxUICalendar.js"></script>
<script language="javascript" type="text/javascript" src="../common/scripts/emxUICreate.js"></script>
<script language="javascript" type="text/javascript" src="../common/scripts/emxTypeAhead.js"></script>
<script language="javascript" type="text/javascript" src="../common/scripts/emxUIFormHandler.js"></script>
<script language="javascript" type="text/javascript" src="../common/scripts/emxQuery.js"></script>
<script language="javascript" type="text/javascript" src="../common/scripts/jquery.min.js"></script>
<script language="javascript" type="text/javascript" src="../components/emxComponentsJSFunctions.js"></script>
<jsp:useBean id="formBean" scope="page" class="com.matrixone.apps.common.util.FormBean" />
<%!
    //日志
    private static final Logger LOGGER = LoggerFactory.getLogger("JF_VPMViewTwoDimensionalDrawing.jsp");
%>
<%
    String strURL = "";

    try {
        String[] tableRowId = emxGetParameterValues(request, "emxTableRowId");
        LOGGER.info("JF_VPMViewTwoDimensionalDrawing.jsp======tableRowId:{}", Arrays.asList(tableRowId));
        String[] splitTableRowIds = ComponentsUIUtil.getSplitTableRowIds(tableRowId);
        LOGGER.info("JF_VPMViewTwoDimensionalDrawing.jsp======splitTableRowIds:{}",Arrays.asList(splitTableRowIds));
        String objectId = splitTableRowIds[0];
//        String objectId = (String) emxGetParameter(request, "objectId");
        System.out.println("strObjectId:" + objectId);
        DomainObject domainObject = DomainObject.newInstance(context, objectId);
        String type = domainObject.getInfo(context, "type");
        String physicalId = domainObject.getInfo(context, "physicalid");
        String vName = domainObject.getAttributeValue(context, "PLMEntity.V_Name");
        NotificationService notificationService = new NotificationService(context);
        String dashboardURL =notificationService.get3DDashboardURL();
        System.out.println("dashboardURL:" + dashboardURL);

        String strURL1 ="{\"data\":{\"items\":[{\"objectId\":\""+physicalId+"\",\"objectType\":\"Drawing\",\"envId\":\"OnPremise\",\"serviceId\":\"3DSpace\",\"displayName\":\""+vName+"\",\"displayType\":\"VPMReference\",\"contextId\":\"ctx::VPLMProjectLeader.Company Name.Common Space\",\"objectTaxonomies\":[\"PLMEntity\",\"PLMReference\",\"PLMCoreReference\",\"LPAbstractReference\",\"PHYSICALAbstractReference\",\"Drawing\"]}]}}";
        System.out.println("strURL1:" + strURL1);

        strURL = dashboardURL+"/#/tab:New%20Tab/app:X3DPLAW_AP/content:X3DContentId=" + URLEncoder.encode(strURL1, "utf-8");

        System.out.println("strURL:" + strURL);
%>
<script language="JavaScript">
    var type = "<%=type%>";
    console.log("type:", type)
    if(type === "Drawing") {
        viewDrawing();
    } else if (type === "Document") {
        let searchUrl = "../components/CUS_DocumentViewEditActionContent.jsp";
        searchUrl += "?objectId="+"<%=objectId%>";
        showModalDialog(searchUrl);
    }

    function viewDrawing() {
        var strURL = "<%=strURL%>";
        console.log("strURL:", strURL)
        window.open(strURL);
    }
</script>
<%
    }catch (Exception e) {
        e.printStackTrace();
        throw  e;
    }
%>
<%@include file = "../emxUICommonEndOfPageInclude.inc" %>