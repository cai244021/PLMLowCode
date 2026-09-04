
<%@ page import="org.slf4j.Logger" %>
<%@ page import="org.slf4j.LoggerFactory" %>
<%@ page import="matrix.db.JPO" %>
<%@ page import="matrix.util.MatrixException" %>
<%@ page import="com.matrixone.apps.domain.DomainObject" %>
<%@ page import="java.util.Arrays" %>
<%@ page import="com.matrixone.apps.common.util.ComponentsUIUtil" %>
<%@ page import="static com.matrixone.apps.common.util.JSPUtil.emxGetParameterValues" %>
<%@ page import="com.matrixone.apps.framework.ui.UIUtil" %>
<%@ page import="com.matrixone.apps.domain.util.XSSUtil" %>
<%@ page import="matrix.util.StringList" %>
<%@ page import="com.matrixone.apps.domain.util.i18nNow" %>
<%@ page import="com.matrixone.apps.common.util.ComponentsUtil" %>
<%@ page pageEncoding="utf-8" %>
<%@include file = "../common/emxNavigatorInclude.inc"%>
<%@include file = "../common/emxNavigatorTopErrorInclude.inc"%>
<%!
    private static final Logger JF_LOGGER = LoggerFactory.getLogger("JF_ChiefEngineerConfirmDialogFS.jsp");
%>
<%
    String requiredText = ComponentsUtil.i18nStringNow("emxComponents.Commom.RequiredText",request.getHeader("Accept-Language"));
    String content1 = context.getLocale().toString().contains("zh") ? "总工审核人员" : "Chief Engineer";
    String footDone = context.getLocale().toString().contains("zh") ? "确定" : "Done";
    String footCancel = context.getLocale().toString().contains("zh") ? "取消" : "Cancel";

    String strHeader = "\u53d1\u8d77\u603b\u5de5\u786e\u8ba4";
    String strObjectId = emxGetParameter(request, "objectId");
    JF_LOGGER.info("JF_ChiefEngineerConfirm.jsp----strObjectId::{}",strObjectId);
    //构造提交请求的url
    String AMP  = "&";
    StringBuilder url = new StringBuilder();
    url.append("./JF_ChiefEngineerConfirmProcess.jsp?");
    url.append("objectId=");
    url.append(strObjectId);
    url.append(AMP);
    url.append("ProgramSuiteKey=");
    url.append("ProgramCentral");
    url.append(AMP);
    url.append("SuiteKey=");
    url.append("Components");
    String sURL =  url.toString();

%>
<html>
<head>
    <script language="JavaScript" src="../common/scripts/emxUIConstants.js"></script>
    <script language="JavaScript" src="../common/scripts/emxUICore.js"></script>
    <script language="JavaScript" src="../common/scripts/emxUICoreMenu.js"></script>
    <script language="JavaScript" src="../common/scripts/emxUIToolbar.js"></script>
    <script language="JavaScript" src="../common/scripts/emxUIFilterUtility.js"></script>
    <script language="JavaScript" src="../common/scripts/emxUIActionbar.js"></script>
    <script language="JavaScript" src="../common/scripts/emxUIModal.js"></script>
    <script language="JavaScript" src="../common/scripts/emxNavigatorHelp.js"></script>
    <script language="JavaScript" src="../emxUIPageUtility.js"></script>
    <script language="JavaScript" src="../common/scripts/emxUIBottomPageJavaScriptInclude.js"></script>
    <link rel="stylesheet" type="text/css" href="../common/styles/emxUIDefault.css">
    <link rel="stylesheet" type="text/css" href="../common/styles/emxUIList.css">
    <link rel="stylesheet" type="text/css" href="../common/styles/emxUIForm.css">
    <script language="JavaScript" type="text/JavaScript">
        addStyleSheet("emxUIList");
        addStyleSheet("emxUIDefault");
        addStyleSheet("emxUIToolbar");
        addStyleSheet("emxUIMenu");
        addStyleSheet("emxUIDOMLayout");
        addStyleSheet("emxUIDialog");

        function doneProcess() {
            if (document.frmMain.JF_ChiefEngineerReviewPerson.value){
                document.frmMain.submit();
            }else {
                alert("请选择总工审核人员 ")
            }
        }

        function showPersonFolderSelector(){
            emxShowModalDialog("../common/emxFullSearch.jsp?field=TYPES=type_Person&groupName=ChiefEngineer&includeOIDprogram=JF_PublicMethodClass:getUserGroupPersons&table=PMCCommonPersonSearchTable&form=PMCCommonPersonSearchForm&showInitialResults=true&selection=single&fieldNameActual=JF_ChiefEngineerReviewPersonName&fieldNameOID=JF_ChiefEngineerReviewPersonOID&fieldNameDisplay=JF_ChiefEngineerReviewPerson&suiteKey=Framework&submitURL=./JF_AEFSearchUtil.jsp&','600','600','true','','JF_ChiefEngineerReviewPerson");
        }

    </script>
    <link rel="shortcut icon" href="../favicon.ico" type="image/x-icon" />
</head>
<body>
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

        <div class="toolbar-container" id="divToolbarContainer">
            <div id="divToolbar" class="toolbar-frame">
                <div class="toolbar">
                    <table>
                        <tbody>
                        <tr>
                            <td  title="工具" nowrap="" class="icon-button menu-button">
                                <img src="../common/images/iconSmallAdministration.png">
                            </td>
                        </tr>
                        </tbody>
                    </table>
                </div>
            </div>
        </div>
    </form>
</div>
<div id='divPageBody'>
    <form name="frmMain" method="post" action="<%= XSSUtil.encodeForHTML(context, sURL) %>" target="_parent" onsubmit="doneProcess(); return false">
        <table>
            <tr>
                <td class="requiredNotice"><%=requiredText%></td>
            </tr>
        </table>
        <table>
            <tr>
                <td class="labelRequired">
                    <%=content1%>
                </td>
                <td class="inputField" >
                    <input id="" value="" type="text" name="JF_ChiefEngineerReviewPerson" size="20" title="<%=content1%>" readonly />
                    <input type="hidden" name="JF_ChiefEngineerReviewPersonName" value=""/>
                    <input type="hidden" name="JF_ChiefEngineerReviewPersonNameOID" value=""/>
                    <input type="button" name="JF_ChiefEngineerReviewPersonButton" value=".." size="5" onClick="showPersonFolderSelector()"/>
                </td>
            </tr>
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
                            <button class="btn-primary" onclick="doneProcess()"><%=footDone%></button>
                            <a onclick="javascript:window.close()">
                                <button class="btn-default"><%=footCancel%></button>
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
</body>
</html>
<%@include file = "../common/emxNavigatorBottomErrorInclude.inc"%>

