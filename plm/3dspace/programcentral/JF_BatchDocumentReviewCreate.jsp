<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="com.matrixone.apps.domain.DomainConstants" %>
<%@ page import="com.matrixone.apps.domain.DomainObject" %>
<%@ page import="com.matrixone.apps.domain.util.EnoviaResourceBundle" %>
<%@ page import="com.matrixone.apps.domain.util.XSSUtil" %>
<script src="../common/scripts/emxUIModal.js" type="text/javascript"></script>
<script language="JavaScript" src="../common/scripts/emxUICore.js"></script>
<script language="JavaScript" src="../common/scripts/emxUIConstants.js"></script>
<script language="JavaScript" src="../common/scripts/emxUISearch.js"></script>
<script type="text/javascript" src="../common/scripts/jquery-latest.js"></script>
<%@include file="../common/emxNavigatorInclude.inc"%>
<%
    String documentIds = emxGetParameter(request, "docIds");
    String projectId = emxGetParameter(request, "projectId");
    String department = emxGetParameter(request, "Department");
    String projectName = "";
    if (projectId != null && projectId.length() > 0) {
        DomainObject projectObject = DomainObject.newInstance(context, projectId);
        projectName = projectObject.getInfo(context, "attribute[Title]");
        if (projectName == null || projectName.length() == 0) {
            projectName = projectObject.getInfo(context, DomainConstants.SELECT_NAME);
        }
    }
    String pageTitle = EnoviaResourceBundle.getProperty(context, "emxProgramCentralStringResource", context.getLocale(), "emxProgramCentral.BatchDocumentReview.CreateTitle");
    String requiredMessage = EnoviaResourceBundle.getProperty(context, "emxProgramCentralStringResource", context.getLocale(), "emxProgramCentral.BatchDocumentReview.TitleRequired");
    String requiredNotice = EnoviaResourceBundle.getProperty(context, "emxProgramCentralStringResource", context.getLocale(), "emxProgramCentral.BatchDocumentReview.RequiredNotice");
    String nameLabel = EnoviaResourceBundle.getProperty(context, "emxProgramCentralStringResource", context.getLocale(), "emxProgramCentral.BatchDocumentReview.Name");
    String projectNameLabel = EnoviaResourceBundle.getProperty(context, "emxProgramCentralStringResource", context.getLocale(), "emxProgramCentral.BatchDocumentReview.ProjectName");
    //20260811 update by ljr 批量审批创建页面增加会签人员选择，复用Components已有国际化。
    String signPersonLabel = EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", context.getLocale(), "emxComponents.Common.Signatories");
    String departPersonLabel = EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", context.getLocale(), "emxComponents.CommonDocument.JF_DepartmentManger");
    String departPersonError = EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", context.getLocale(), "emxComponents.Document.DepManagerError");
    String remarkLabel = EnoviaResourceBundle.getProperty(context, "emxProgramCentralStringResource", context.getLocale(), "emxProgramCentral.BatchDocumentReview.Remark");
%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8" />
    <title><%=XSSUtil.encodeForHTML(context, pageTitle)%></title>
    <link rel="stylesheet" href="../common/styles/emxUIDefault.css" type="text/css" />
    <link rel="stylesheet" href="../common/styles/emxUIForm.css" type="text/css" />
    <style>
        body { margin: 0; overflow: hidden; background: #fff; }
        #formBody { padding-bottom: 48px; }
        input[readonly] { background: #e7e7e7; color: #2176b9; }
        #footer { position: fixed; left: 0; right: 0; bottom: 0; padding: 8px 14px; text-align: right; background: #f2f2f2; border-top: 1px solid #ccc; }
        #footer input { margin-left: 8px; }
    </style>
    <script type="text/javascript">
        function submitBatchReview(submitReview) {
            //保存只创建审批单；提交审批会在同一事务中创建后直接提升。
            var title = document.getElementById("title").value.replace(/^\s+|\s+$/g, "");
            if (!title) {
                alert("<%=XSSUtil.encodeForJavaScript(context, requiredMessage)%>");
                return;
            }
            var depart = "<%=department%>";
            if ("Y" === depart) {
                var departPersonNameOID = document.getElementById("departPersonNameOID").value;
                if (!departPersonNameOID) {
                    alert("<%=XSSUtil.encodeForJavaScript(context, departPersonError)%>");
                    return;
                }
            }
            document.getElementById("submitReview").value = submitReview ? "true" : "false";
            var buttons = document.querySelectorAll("#footer input");
            for (var i = 0; i < buttons.length; i++) {
                buttons[i].disabled = true;
            }
            document.getElementById("batchReviewForm").submit();
        }

        //20260811 update by ljr 会签人员允许从全部活动人员中多选，返回人员名称、显示名称和对象ID。
        function selectSignPersons() {
            var searchUrl = "../common/emxFullSearch.jsp?field=TYPES=type_Person:CURRENT=policy_Person.state_Active"
                    + "&table=AEFGeneralSearchResults&showInitialResults=true&selection=multiple"
                    + "&fieldNameActual=signPersonName&fieldNameDisplay=signPersonDisplay"
                    + "&suiteKey=Framework&submitURL=./JF_AEFSearchUtil.jsp";
            // emxShowModalDialog(searchUrl, 850, 630, true);
            showChooser(searchUrl, 700, 500);
        }

        function selectDepartPersons() {
            var searchUrl = "../common/emxFullSearch.jsp?field=TYPES=type_Person:CURRENT=policy_Person.state_Active"
                + "&groupName=department_manager&includeOIDprogram=JF_PublicMethodClass:getUserGroupPersons"
                + "&table=AEFGeneralSearchResults&showInitialResults=true&selection=multiple"
                + "&fieldNameActual=departPersonName&fieldNameDisplay=departPersonDisplay"
                + "&suiteKey=Framework&submitURL=./JF_AEFSearchUtil.jsp";
            // emxShowModalDialog(searchUrl, 850, 630, true);
            showChooser(searchUrl, 700, 500);
        }

    </script>
</head>
<body>
<div id="formBody">
    <form id="batchReviewForm" method="post" action="JF_BatchDocumentReviewCreateProcess.jsp">
        <%@include file="../common/enoviaCSRFTokenInjection.inc"%>
        <input type="hidden" name="docIds" value="<%=XSSUtil.encodeForHTMLAttribute(context, documentIds == null ? "" : documentIds)%>" />
        <input type="hidden" name="projectId" value="<%=XSSUtil.encodeForHTMLAttribute(context, projectId)%>" />
        <input type="hidden" id="submitReview" name="submitReview" value="false" />
        <table>

            <tr>
                <td class="requiredNotice"><%=XSSUtil.encodeForHTML(context, requiredNotice)%></td>
            </tr>
        </table>
        <%--20260811 update by ljr 使用公共文档创建页面相同的标准Form布局。--%>
        <table>
            <tbody>
            <tr>
                <td class="labelRequired"><%=XSSUtil.encodeForHTML(context, nameLabel)%></td>
                <td class="inputField"><input type="text" id="title" name="title" size="20" maxlength="128" /></td>
            </tr>
            <tr>
                <td class="label"><%=XSSUtil.encodeForHTML(context, projectNameLabel)%></td>
                <td class="inputField"><input type="text" name="projectName" size="20" value="<%=XSSUtil.encodeForHTMLAttribute(context, projectName)%>" readonly="readonly" /></td>
            </tr>
            <tr>
                <td class="label"><%=XSSUtil.encodeForHTML(context, signPersonLabel)%></td>
                <td class="inputField">
                    <input type="text" id="signPersonDisplay" name="signPersonDisplay" size="20" readonly="readonly" />
                    <input type="hidden" id="signPersonName" name="signPersonName" />
                    <input type="hidden" id="signPersonNameOID" name="signPersonNameOID" />
                    <input type="button" value=".." onclick="selectSignPersons();" />
                </td>
            </tr>
            <%
                if ("Y".equalsIgnoreCase(department)) {

            %>
            <tr>
                <td class="labelRequired"><%=XSSUtil.encodeForHTML(context, departPersonLabel)%></td>
                <td class="inputField">
                    <input type="text" id="departPersonDisplay" name="departPersonDisplay" size="20" readonly="readonly" />
                    <input type="hidden" id="departPersonName" name="departPersonName" />
                    <input type="hidden" id="departPersonNameOID" name="departPersonNameOID" />
                    <input type="button" value=".." onclick="selectDepartPersons();" />
                </td>
            </tr>
            <%
                }

            %>
            <tr>
                <td class="label"><%=XSSUtil.encodeForHTML(context, remarkLabel)%></td>
                <td class="inputField"><textarea name="description" rows="5" cols="36" wrap></textarea></td>
            </tr>
            </tbody>
        </table>
    </form>
</div>
<div id="footer">
    <input type="button" class="button primary" value="<%=XSSUtil.encodeForHTMLAttribute(context, EnoviaResourceBundle.getProperty(context, "emxProgramCentralStringResource", context.getLocale(), "emxProgramCentral.Common.Save"))%>" onclick="submitBatchReview(false);" />
    <input type="button" class="button" value="<%=XSSUtil.encodeForHTMLAttribute(context, EnoviaResourceBundle.getProperty(context, "emxProgramCentralStringResource", context.getLocale(), "emxProgramCentral.BatchDocumentReview.SubmitReview"))%>" onclick="submitBatchReview(true);" />
    <input type="button" class="button" value="<%=XSSUtil.encodeForHTMLAttribute(context, EnoviaResourceBundle.getProperty(context, "emxFrameworkStringResource", context.getLocale(), "emxFramework.Common.Cancel"))%>" onclick="getTopWindow().closeWindow();" />
</div>
</body>
</html>
