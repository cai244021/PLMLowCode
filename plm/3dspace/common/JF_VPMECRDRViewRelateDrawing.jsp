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
<%@ page import="matrix.util.StringList" %>
<%@ page import="com.matrixone.apps.domain.util.MapList" %>
<%@ page import="org.apache.commons.collections4.CollectionUtils" %>
<%@ page import="java.util.Map" %>
<%@ page import="static com.matrixone.apps.common.util.JSPUtil.emxGetParameterValues" %>
<%@ page import="com.matrixone.apps.framework.ui.UIUtil" %>
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
    private static final Logger LOGGER = LoggerFactory.getLogger("JF_VPMECRDRViewRelateDrawing.jsp");
%>
<%
    String strURL = "";
    String mess = "";
    try {
        String[] tableRowId = emxGetParameterValues(request, "emxTableRowId");
        LOGGER.info("JF_VPMViewTwoDimensionalDrawing.jsp======tableRowId:{}", Arrays.asList(tableRowId));
        String[] splitTableRowIds = ComponentsUIUtil.getSplitTableRowIds(tableRowId);
        LOGGER.info("JF_VPMViewTwoDimensionalDrawing.jsp======splitTableRowIds:{}",Arrays.asList(splitTableRowIds));
        String tableId = splitTableRowIds[0];
        String objectId = "";
        System.out.println("strObjectId:" + tableId);
        DomainObject domainObject = DomainObject.newInstance(context, tableId);
        MapList documentMapList = domainObject.getRelatedObjects(context, "Reference Document", "Document", StringList.create("id"), new StringList(),
                false, true, (short) 1, "attribute[JF_DocumentType].value==Drawing", "", 0);
        MapList drawingMapList = domainObject.getRelatedObjects(context, "XCADBaseDependency", "Drawing", StringList.create("id"), new StringList(),
                true, false, (short) 1, "", "", 0);
        if (CollectionUtils.isNotEmpty(documentMapList) && CollectionUtils.isNotEmpty(drawingMapList)){
            Map map = (Map) drawingMapList.get(0);
            objectId = UIUtil.getValue(map, "id");
        }else if (CollectionUtils.isEmpty(documentMapList) && CollectionUtils.isEmpty(drawingMapList)){
            mess = context.getLocale().toString().contains("zh") ? "没有图纸 无法预览!" : "Preview is impossible without drawing!";
        }else if (CollectionUtils.isNotEmpty(documentMapList)){
            Map map = (Map) documentMapList.get(0);
            objectId = UIUtil.getValue(map, "id");
        }else if (CollectionUtils.isNotEmpty(drawingMapList)){
            Map map = (Map) drawingMapList.get(0);
            objectId = UIUtil.getValue(map, "id");
        }
        String type = "";
        String physicalId = "";
        String vName = "";
        DomainObject drawingObj = new DomainObject();
        if (UIUtil.isNotNullAndNotEmpty(objectId)){
            drawingObj = DomainObject.newInstance(context, objectId);
            LOGGER.info("JF_VPMViewTwoDimensionalDrawing.jsp======objectId:{}",objectId);
            type = drawingObj.getInfo(context, "type");
            physicalId = drawingObj.getInfo(context, "physicalid");
            vName = drawingObj.getAttributeValue(context, "PLMEntity.V_Name");
        }
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
    var mess = "<%=mess%>";
    console.log("type:", type)
    if(type === "Drawing") {
        viewDrawing();
    } else if (type === "Document") {
        let searchUrl = "../components/CUS_DocumentViewEditActionContent.jsp";
        searchUrl += "?objectId="+"<%=objectId%>";
        showModalDialog(searchUrl);
    }else {
        alert(mess);
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