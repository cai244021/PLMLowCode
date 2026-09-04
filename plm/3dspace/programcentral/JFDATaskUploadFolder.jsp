<%--  emxProjectManagementDragAndDrop.jsp

   Copyright (c) Dassault Systemes, 1992-2020 .All rights reserved

--%>
<%@page import="jakarta.json.JsonObjectBuilder"%>
<%@page import="jakarta.json.Json"%>
<%@page import="jakarta.json.JsonObject"%>
<%@page import="java.lang.reflect.Method"%>
<%@page import="java.util.List"%>
<%@page import="java.util.Enumeration"%>
<%@page import="com.matrixone.apps.domain.DomainObject"%>
<%@page import="com.matrixone.apps.domain.DomainConstants"%>

<%@page import="com.matrixone.apps.program.ProgramCentralConstants"%>
<%@page import="com.matrixone.apps.domain.util.XSSUtil"%>
<%@page import="matrix.util.StringList" %>

<%@include file = "../common/emxNavigatorTopErrorInclude.inc"%>

<%@include file = "../emxUICommonHeaderBeginInclude.inc" %>
<%@include file = "../emxUICommonAppInclude.inc"%>
<%@include file = "../common/emxUIConstantsInclude.inc"%>

<title><xss:encodeForHTML>TITLE</xss:encodeForHTML></title>

<script language="javascript" src="../common/scripts/emxUICore.js"></script>
<script language="javascript" src="../common/scripts/emxUIConstants.js"></script>
<script language="javascript" src="../common/scripts/emxUIFormUtil.js"></script>


<%@include file = "../emxUICommonHeaderEndInclude.inc" %>

<link rel="stylesheet" href="../common/styles/emxUIForm.css">
<link rel="stylesheet" href="./styles/emxProjectManagementDragAndDrop.css">
<script src="../common/scripts/enoJSValidationUtil.js"></script>
<script src="../common/scripts/emxUIFreezePane.js"></script>
<script src="./script/emxProjectManagementDragAndDrop.js"></script>


<%
    String AMP                  = "&";
    String sLang                = request.getHeader("Accept-Language");
    String sLabelClose          = i18nNow.getI18nString("emxCommon.Common.Close",              "emxComponentsStringResource", sLang);
    String sFormTitle           = i18nNow.getI18nString("emxProgramCentral.Common.FormDragDrop",    "emxProgramCentralStringResource", sLang);
    String sDropHere            = i18nNow.getI18nString("emxProgramCentral.Common.FormDropHere",    "emxProgramCentralStringResource", sLang);

    String sLabelTitle           = i18nNow.getI18nString("emxFramework.Attribute.Title",            "emxFrameworkStringResource", sLang);
    String sLabelDesc            = i18nNow.getI18nString("emxFramework.Basic.Description",          "emxFrameworkStringResource", sLang);

    String sLabelDeliverable     = i18nNow.getI18nString("emxFramework.Relationship.Task_Deliverable",   "emxFrameworkStringResource", sLang);
    String sLabelReference       = i18nNow.getI18nString("emxFramework.Attribute.Attachments", "emxFrameworkStringResource", sLang);



//    String objectId = (String)emxGetParameter(request, "objectId");
    String emxTableRowId = (String)emxGetParameter(request, "emxTableRowId");
    String objectId = emxTableRowId.split("\\|")[1];
    String openerFrame = (String) emxGetParameter(request,"openerFrame");


    String sMode = emxGetParameter(request, "mode");
    if (sMode == null) sMode = "deliverable";
    sMode = XSSUtil.encodeURLForServer(context, sMode);

    DomainObject dom = DomainObject.newInstance(context, objectId);
    StringList slSelect = new StringList();
    slSelect.add(DomainConstants.SELECT_TYPE);
    slSelect.add(DomainConstants.SELECT_NAME);
    slSelect.add("interface[ProxyItem]");
    slSelect.add("attribute[ProxyItem.Proxy_URL]");
    Map mapInfo = dom.getInfo(context, slSelect);
    String sObjType   = (String) mapInfo.get(DomainConstants.SELECT_TYPE);
    String sObjName   = (String) mapInfo.get(DomainConstants.SELECT_NAME);
    String sProxy     = (String) mapInfo.get("interface[ProxyItem]");
    String sProxyURL  = (String) mapInfo.get("attribute[ProxyItem.Proxy_URL]");
    String sProxyURLx = XSSUtil.encodeForURL(context, sProxyURL);

    String dDocType = "Document";
    String sRel = "Task Deliverable";
    String sWebService = "/deliverables";


    if ("deliverable".equalsIgnoreCase(sMode)) {
        dDocType = "Document";
        sRel = "Task Deliverable";
        sFormTitle = sLabelDeliverable;

    } else if ("reference".equalsIgnoreCase(sMode)){
        dDocType = "Document";
        sRel = "Reference Document";
        sWebService = "/references";
        sFormTitle = sLabelReference;
    }

    StringBuilder url = new StringBuilder();
    url.append("../common/emxFileUpload.jsp?");
    url.append("objectId=");
    url.append(objectId);
    url.append(AMP);
    url.append("type=");
    url.append( XSSUtil.encodeForURL(context,dDocType));
    url.append(AMP);
    url.append("relationship=");
    url.append( XSSUtil.encodeForURL(context,sRel));
    String sURL =  url.toString();

    StringBuilder urlx = new StringBuilder();
    urlx.append("../resources/v1/modeler/tasks/");
    urlx.append(objectId);
    urlx.append(sWebService);
    String sURLProxy = urlx.toString();


    if(false) {
        String strURL = "../common/emxIndentedTable.jsp?program=emxTask:getURLDeliverables&table=PMCBookmarkSummary&toolbar=PMCDeliverablesBookmarkToolBar&selection=multiple&Export=false&sortColumnName=Name&sortDirection=ascending&header=emxProgramCentral.Common.Bookmarks&HelpMarker=emxhelpdeliverables&hideLaunchButton=true&showRMB=false&postProcessJPO=emxProgramCentralUtil:postProcessPortalCmdSBRefresh&customize=false&displayView=details&autoFilter=false&showPageURLIcon=false&rowGrouping=false&findMxLink=false&multiColumnSort=false&objectCompare=false&showClipboard=false";
        Enumeration requestParams = emxGetParameterNames(request);
        StringBuilder _url = new StringBuilder();

        if (requestParams != null) {
            while (requestParams.hasMoreElements()) {
                String param = (String) requestParams.nextElement();
                String value = emxGetParameter(request, param);
                _url.append("&" + param);
                _url.append("=" + XSSUtil.encodeForURL(context, value));
            }
            strURL += _url.toString();
        }
    }
%>


<div id="pageHeadDiv">
    <table>
        <tbody><tr><td class="page-title"><h2 title="<%=sFormTitle%>"><%=sFormTitle%></h2><h3 title="<%=sObjName%>"><%=sObjName%></h3></td>
                   <td class="functions">
                    <table>
                    <tbody><tr><td class="progress-indicator">
                            <div style="visibility: hidden;" id="imgProgressDiv"></div>
                    </td></tr></tbody>
                    </table>
                    </td>
        </tr></tbody>
    </table>
    <div id="divToolbarContainer" class="toolbar-container"></div>
</div>
<div id="divPageBody" style="top: 79px;">

<table> <tbody> <tr>

<form id=formDrag action=<%=sURL%> method="post" enctype="multipart/form-data" name="formDrag" >

 <table>
    <tbody>
     <tr>
       <br/>
       <div id="divDrag" class="dropArea"
            ondrop="FileSelectHandlerForm(event, '<%=objectId%>', 'formDrag', 'divDrag', '<%=openerFrame%>', '<%=sRel%>',  '<%=sDropHere%>')"
            ondragover="FileDragHover(event, 'divDrag')"
            ondragleave="FileDragHover(event, 'divDrag')">
       <br/>
           <%=sDropHere%>
       </div>
    </tr>

<%--

    <tr>
        <td align="center">
            &nbsp;
        </td>
    </tr>
    <tr id="calc_Title">
        <td></td>
    </tr>
    <tr>
        <td class="createLabel" width="150" valign="middle">
            <label for="Title">Title</label>
        </td>
    </tr>
    <tr>
        <td class="createInputField" colspan="0" valign="middle">
            <table class="">
                <tbody>
                    <tr>
                        <td>
                            <input value="" id="Title" name="Title" fieldlabel="Title" title="Title" size="20" type="text">
                        </td>
                    </tr>
                </tbody>
            </table>
            <script language="JavaScript">
                 myValidationRoutines1.push(['assignValidateMethod', 'Title', 'isBadNameChars']);
            </script>
        </td>
    </tr>


    <tr id="calc_Description">
        <td></td>
    </tr>
    <tr>
        <td class="createLabel" width="150" valign="middle">
            <label for="Description">Description</label>
        </td>
    </tr>
    <tr>
        <td class="createInputField" colspan="0" valign="middle">
            <table class="textarea">
                <tbody>
                    <tr>
                        <td>
                            <textarea cols="25" rows="5" name="Description" id="DescriptionId" fieldlabel="Description" title="Description"></textarea>
                        </td>
                    </tr>
                </tbody>
            </table>
        </td>
    </tr>
--%>

    </tbody>
 </table>
</form>

 <tr>
   <form id=proxyForm action=<%=sURLProxy%> method="post" enctype="application/json" name="proxyForm" >
   <%@include file = "../common/enoviaCSRFTokenInjection.inc"%>
   </form>
</tr><tr>
       <div id=divMessage></div>

</tr>
</tbody>
</table>


</div>
<div id="divPageFoot">
    <div id="divDialogButtons">
        <table><tbody>
            <tr><td class="functions"></td><td class="buttons">
            <table><tbody><tr>
                 <td><div id="cancelImage">
                    <a onclick="closeSlideinWindow(getTopWindow())" href="javascript:void(0)" class="footericon">
                        <img alt="Cancel" border="0" src="../common/images/buttonDialogCancel.gif"></a>
                  </div></td>
                 <td><div id="cancelText">
                    <a class="button" onclick="closeSlideinWindow(getTopWindow())" href="javascript:void(0)">
                    <button type="button" class="btn-primary"><%=sLabelClose%></button></a>
                 </div></td>
          </tr></tbody></table>
          </td></tr>
     </tbody></table>
    </div>
</div>


    </body>
</html>
