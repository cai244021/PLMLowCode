<%-- JF_ProjectMBOMAddMakeVPMDialogFS.jsp - used for Checkin of file into Document Object
   Copyright (c) 1992-2020 Dassault Systemes.
   All Rights Reserved.
   This program contains proprietary and trade secret information of MatrixOne,
   Inc.  Copyright notice is precautionary only
   and does not evidence any actual or intended publication of such program

   emxCommonDocumentMultiFileUploadFS.jsp
   static const char RCSID[] = "$Id: emxCommonDocumentCheckinDialogFS.jsp.rca 1.21 Wed Oct 22 16:18:21 2008 przemek Experimental przemek $"
--%>
<%@ page import="com.matrixone.apps.domain.util.EnoviaResourceBundle" %>
<%@ page import="com.matrixone.apps.common.util.ComponentsUtil"%>
<%@ page import="org.slf4j.LoggerFactory" %>
<%@ page import="org.slf4j.Logger" %>
<%@ page pageEncoding="utf-8" %>
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
<script language="javascript" type="text/javascript" src="../components/emxComponentsJSFunctions.js"></script>
<jsp:useBean id="formBean" scope="page" class="com.matrixone.apps.common.util.FormBean" />
<%!
    private static final Logger _logger =  LoggerFactory.getLogger("JF_ProjectMBOMAddMakeVPMDialogFS.jsp");
%>
<%
    String strLang = context.getSession().getLanguage();
    String strHeader = ComponentsUtil.i18nStringNow("emxComponents.Common.AddMakeVPM", strLang);

    String strObjectId = (String)emxGetParameter(request, "objectId");
    String selectParts = (String)emxGetParameter(request, "selectParts");
    //构造提交请求的url
    String AMP  = "&";
    StringBuilder url = new StringBuilder();
    url.append("./JF_ProjectMBOMAddMakeVPMProcess.jsp?");
    url.append("objectId=");
    url.append(strObjectId);
    url.append(AMP);
    url.append("&connParts=");
    url.append(selectParts);
    url.append(AMP);
    url.append("SuiteKey=");
    url.append("Components");
    String sURL =  url.toString();
    try {
%>
<script language="JavaScript">
    function checkinCancel() {
        getTopWindow().closeWindow();
    }

    function showMakeVPMSelector(){
                                      // emxFullSearch.jsp?field=TYPES=type_VPMReference&table=AEFGeneralSearchResults&showInitialResults=true&selection=multiple&submitURL=../engineeringcentral/JF_ProjectMbomAddMakeVPM.jsp&submitAction=refreshCaller
        emxShowModalDialog("../common/emxFullSearch.jsp?field=TYPES=type_VPMReference:CURRENT=policy_VPLM_SMB_Definition_MajorRev.state_Released&table=AEFGeneralSearchResults&showInitialResults=true&selection=single&fieldNameActual=JF_AddMakeVPMName&fieldNamePID=JF_AddMakeVPMPID&fieldNameOID=JF_AddMakeVPMOID&fieldNameDisplay=JF_AddMakeVPM&suiteKey=Framework&submitURL=./JF_AEFSearchUtil.jsp&','600','600','true','','GeneralClassName");
    }

    function validateAndSetAction(action) {
        // 验证
        if (document.checkinForm.JF_AddMakeVPMNameOID.value === "") {
            alert("<emxUtil:i18nScript localize='i18nId'>emxComponents.Mess.JF_AddMakeVPMNameOID</emxUtil:i18nScript>");
            return false;
        }
        if (document.checkinForm.JF_Dosage.value === "") {
            alert("<emxUtil:i18nScript localize='i18nId'>emxComponents.Mess.JF_Dosage</emxUtil:i18nScript>");
            return false;
        }// 正实数或0的正则表达式：可以是整数或小数，包括0和0.0
        // const regex1 = /^[+-]?\d+(\.\d+)?$/;  //正实数
        const regex1 = /^(?!0+(\.0+)?$)\d+(\.\d+)?$/;  //正实数 不包含0.0 0
        const regex2 = /^[1-9]\d*$/;  //正整数
        const JF_Dosage  = document.checkinForm.JF_Dosage.value;
        const unitRange  = document.checkinForm.JF_Units.title;
        if ("PC" === unitRange) {
            if (!regex2.test(JF_Dosage)) {
                alert("<emxUtil:i18nScript localize="i18nId">emxComponents.Mess.JF_DosagePCError</emxUtil:i18nScript>");
                return;
            }
        } else {
            if (!regex1.test(JF_Dosage)) {
                alert("<emxUtil:i18nScript localize="i18nId">emxComponents.Mess.JF_DosageError</emxUtil:i18nScript>");
                return;
            }
        }
        // 设置 actionType
        document.getElementById("actionType").value = action;
        return true;
    }

    function submitAndContinue() {
        if (validateAndSetAction('continue')) {
            document.checkinForm.submit();
        }
    }

    function submitAndClose() {
        if (validateAndSetAction('close')) {
            document.checkinForm.submit();
        }
    }
</script>
<%@include file = "../emxUICommonHeaderEndInclude.inc"%>
<body>
<style>
    #divPageBody {
        position:absolute;
        top:5px;
        right:0;
        bottom:25px;
        left:0;
        padding:0;
        overflow:auto;
        background:#fff;
    }

    body.editable table.list tr th {
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

<div id='divPageBody'>
    <form name="checkinForm" enctype="multipart/form-data"  method="post" onsubmit="return validateAndSetAction();" action="<%=sURL%>" >
        <%@include file = "../common/enoviaCSRFTokenInjection.inc"%>
        <input type="hidden" id="actionType" name="actionType" value="" />
        <table class="list">
            <tbody>
            <tr>

                <framework>
                    <th class="" width="100%">
                        <emxUtil:i18n localize="i18nId"><%=strHeader%></emxUtil:i18n>
                    </th>
                </framework>
            </tr>


            <tr class="even">
                <!-- //XSSOK -->
                <td width="100%">
                    <table>
                        <tbody>
                        <%--选择辅料--%>
                        <tr style="height: 20px"></tr>
                        <tr>
                            <td class="labelRequired" style="color: darkred;font-style: italic;">
                                <emxUtil:i18n localize="i18nId">emxComponents.Common.SelectMakeVPM</emxUtil:i18n>&nbsp;
                            </td>
                            <td class = "inputField" >
                                <input value="" type="text" id="JF_AddMakeVPM" name="JF_AddMakeVPM" size="35" title="<emxUtil:i18n localize = "i18nId">emxComponents.Common.SelectMakeVPM</emxUtil:i18n>" readonly />
                                <input type="hidden" id="JF_AddMakeVPMName"   name="JF_AddMakeVPMName"    value=""/>
                                <input type="hidden" id="JF_AddMakeVPMNameOID"name="JF_AddMakeVPMNameOID" value=""/>
                                <input type="hidden" id="JF_AddMakeVPMNamePID"name="JF_AddMakeVPMNamePID" value=""/>
                                <input type="button" id="JF_AddMakeVPMButton" name="JF_AddMakeVPMButton"  value=".." size="5" onClick="showMakeVPMSelector()"/>
                            </td>
                        </tr>
                        <tr style="height: 20px"></tr>
                        <%--单位--%>
                        <tr>
                            <td class="labelRequired" style="color: darkred;font-style: italic;">
                                <emxUtil:i18n localize="i18nId">emxComponents.Common.Units</emxUtil:i18n>
                            </td>
                            <td class = "inputField">
                                <input style="color: black" type="text" id="JF_Units" name="JF_Units" disabled size="30" value="" title="">
                            </td>
                        </tr>
                        <tr style="height: 20px"></tr>
                        <%--用量--%>
                        <tr>
                            <td class="labelRequired" style="color: darkred;font-style: italic;">
                                <emxUtil:i18n localize="i18nId">emxComponents.Common.JF_Dosage</emxUtil:i18n>
                            </td>
                            <td class = "inputField">
<%--                                <input type="text" name="JF_DocFileFormatRequirements" size="20" value="" />--%>
                                <input type="text" id="JF_Dosage" name="JF_Dosage" size="30"  value="">
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
<div id="divPageFoot">
    <table width="100%" border="0" align="center" cellspacing="2" cellpadding="3">
        <tbody>
        <tr>
            <td class="buttons" align="right">
                <table border="0" cellspacing="0">
                    <tbody>
                    <tr>
                        <td>

<%--                            <button class="btn-primary" onclick="checkin()"><emxUtil:i18n localize="i18nId">emxComponents.ColorMatrix.UpgradeVersionSure</emxUtil:i18n></button>--%>
<%--                            <button class="btn-primary" onclick="checkin()"><emxUtil:i18n localize="i18nId">emxComponents.ColorMatrix.UpgradeVersionSure</emxUtil:i18n></button>--%>
                            <button class="btn-primary" onclick="submitAndContinue()"><emxUtil:i18n localize="i18nId">emxComponents.Command.ApplyContinue</emxUtil:i18n></button>
                            <button class="btn-primary" onclick="submitAndClose()"><emxUtil:i18n localize="i18nId">emxComponents.Command.ApplyClose</emxUtil:i18n></button>

                            <a onclick="javascript:window.close()">
                                <button class="btn-default"><emxUtil:i18n localize="i18nId">emxComponents.Button.Cancel</emxUtil:i18n></button>
                            </a>

                        </td>
                    </tr>
                    </tbody>
                </table>
            </td>
        </tr>
        </tbody>
    </table>
</div>
<iframe name="hiddenFrame" style="display:none;"></iframe>
</body>

<%
    }catch (Exception e) {
        e.printStackTrace();
        throw  e;
    }
%>
<%@include file = "../emxUICommonEndOfPageInclude.inc" %>