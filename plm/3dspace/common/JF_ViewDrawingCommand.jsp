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
<%
    String alertMess = DomainConstants.EMPTY_STRING;
    String strFlushTableName = DomainConstants.EMPTY_STRING;
    String suiteKey = "emxComponentsStringResource";
    String strURL = "";
    String title = context.getLocale().toString().contains("zh") ? "图纸预览" : "Drawing Preview";
    String strHeader = context.getLocale().toString().contains("zh") ? "图纸预览" : "Drawing Preview";

    try {
        String objectId = (String) emxGetParameter(request, "objectId");
        System.out.println("strObjectId:" + objectId);
        DomainObject domainObject = DomainObject.newInstance(context, objectId);
        String physicalId = domainObject.getInfo(context, "physicalid");
        String vName = domainObject.getAttributeValue(context, "PLMEntity.V_Name");
        NotificationService notificationService = new NotificationService(context);
        String dashboardURL =notificationService.get3DDashboardURL();
        System.out.println("dashboardURL:" + dashboardURL);

        String strURL1 ="{\"data\":{\"items\":[{\"objectId\":\""+physicalId+"\",\"objectType\":\"Drawing\",\"envId\":\"OnPremise\",\"serviceId\":\"3DSpace\",\"displayName\":\""+vName+"\",\"displayType\":\"VPMReference\",\"contextId\":\"ctx::VPLMProjectLeader.Company Name.Common Space\",\"objectTaxonomies\":[\"PLMEntity\",\"PLMReference\",\"PLMCoreReference\",\"LPAbstractReference\",\"PHYSICALAbstractReference\",\"Drawing\"]}]}}";
        System.out.println("strURL1:" + strURL1);

        strURL = dashboardURL+"/#/tab:New%20Tab/app:X3DPLAW_AP/content:X3DContentId=" + URLEncoder.encode(strURL1, "utf-8");
        //ENXDISC_AP   - 3D Navigate
        //ENOR3D_AP - 3D MarkUp
        //ENOSCEN_AP - Product Explorer
        //X3DPLAY_AP

        System.out.println("strURL:" + strURL);
%>
<script language="JavaScript">
    function viewDrawing() {
        var strURL = "<%=strURL%>";
        console.log("strURL:", strURL)
        window.open(strURL);
    }
</script>
<%@include file = "../emxUICommonHeaderEndInclude.inc"%>
<body>
<style>
    /*#divPageBody {*/
    /*    margin: 0;*/
    /*    position:absolute;*/
    /*    top:200px;*/
    /*    right:0;*/
    /*    bottom:25px;*/
    /*    left:500px;*/
    /*    padding:0;*/
    /*    overflow:auto;*/
    /*    background:#fff;*/
    /*    display: flex;*/
    /*    justify-content: center;*/
    /*    align-items: center;*/
    /*}*/

    body,div{
        box-sizing:border-box;
        margin:0;
        padding:0;
    }
    .fa{
        width:80vw;
        height: 80vh;
        /*border:5px solid tomato;*/
        position:relative;
    }
    .son{
        width:80px;
        height: 80px;
        /*border:5px solid black;*/
        position:absolute;
        top:60%;
        left:60%;
        transform:translate(-50%,-50%);
    }
    /*table.list tr th*/
    body.editable  {
        min-height:26px;
        padding:10px 5px 10px 5px;
        background: #f5f6f7; /* Old browsers */
        background: -moz-linear-gradient(top, #f5f6f7 0%, #e2e4e3 100%); /* FF3.6+ */
        background: -webkit-gradient(linear, left top, left bottom, color-stop(0%,#f5f6f7), color-stop(100%,#e2e4e3)); /* Chrome,Safari4+ */
        background: -webkit-linear-gradient(top, #f5f6f7 0%,#e2e4e3 100%); /* Chrome10+,Safari5.1+ */
        background: -o-linear-gradient(top, #f5f6f7 0%,#e2e4e3 100%); /* Opera 11.10+ */
        background: -ms-linear-gradient(top, #f5f6f7 0%,#e2e4e3 100%); /* IE10+ */
        background: linear-gradient(to bottom, #f5f6f7 0%,#e2e4e3 100%); /* W3C */
        filter: progid:DXImageTransform.Microsoft.gradient( startColorstr='#f5f6f7', endColorstr='#e2e4e3',GradientType=0 ); /* IE6-9 */
        font-weight:bold;
        border-top:1px solid #288fd1;
        border-bottom:1px solid #288fd1;
    }
</style>
<div id="pageHeadDiv">
    <form>
        <table>
            <tbody>
            <tr>
                <td class="page-title"><h2 id="ph"><%=strHeader%></h2></td>
                <td class="functions">
                    <table>
                        <tbody>
                        <tr>
                            <td class="progress-indicator">
                                <div id="imgProgressDiv" style="visibility: hidden">

                                </div>
                            </td>
                        </tr>
                        </tbody>
                    </table>
                </td>
            </tr>
            </tbody>
        </table>
    </form>
</div>

<div id='divPageBody' class="fa">
    <table class="son" width="100%" border="0" align="center" cellspacing="2" cellpadding="3">
        <tbody>
        <tr>
            <td class="buttons" align="right">
                <table border="0" cellspacing="0">
                    <tbody>
                    <tr>
                        <td>
                            <button class="btn-primary" onclick="viewDrawing()" target="listHidden"><emxUtil:i18n localize="i18nId"><%=title%></emxUtil:i18n></button>
                        </td>
                    </tr>
                    </tbody>
                </table>
            </td>
        </tr>
        </tbody>
    </table>
</div>
<div id="divPageFoot">

</div>
</body>

<%
    }catch (Exception e) {
        e.printStackTrace();
        throw  e;
    }
%>
<%@include file = "../emxUICommonEndOfPageInclude.inc" %>