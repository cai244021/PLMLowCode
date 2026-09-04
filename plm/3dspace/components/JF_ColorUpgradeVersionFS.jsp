<%-- Idm_uploadInspectionPartCheckinDialogFS.jsp - used for Checkin of file into Document Object
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
	private static final Logger _logger =  LoggerFactory.getLogger("JF_ColorUpgradeVersionFS.jsp");
%>
<%
	String strLang = context.getSession().getLanguage();
	String strHeader = ComponentsUtil.i18nStringNow("Components.Command.ColorMatrixUVersion", strLang);
	strHeader += ComponentsUtil.i18nStringNow("emxComponents.AssignTasksDialog.Instructions", strLang);

	String strObjectId = (String)emxGetParameter(request, "objectId");
	_logger.info("FS-----------------------objectId:{}",strObjectId);
	//构造提交请求的url
	String AMP  = "&";
	StringBuilder url = new StringBuilder();
	url.append("./JF_ColorUpgradeVersionProcess.jsp?");
	url.append("objectId=");
	url.append(strObjectId);
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
	function checkin(){
		var rev;
		if (document.getElementById("Minor").checked){
			rev = "Minor";
		} else if (document.getElementById("Major").checked) {
			rev = "Major";
		}
		var input =  document.getElementById("revision");
		input.value = rev;
		document.checkinForm.submit();
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
	<form name="checkinForm" enctype="multipart/form-data" target="_parent" method="post" onsubmit="checkin();" action="<%=sURL%>" scrolling="auto">
		<%@include file = "../common/enoviaCSRFTokenInjection.inc"%>
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
						<tr>
							<td style="font-weight:bold;">
								<input name="objectId" id="objectId" maxlength="" size="20" title="objectId" value="<%=strObjectId%>" type="text" style="display:none">
								<input name="revision" id="revision" maxlength="" size="20" title="revision" value="" type="text" style="display:none">
								<!-- //XSSOK -->
								<input type="hidden" name="__fcs___comment_0" value="">
							</td>
						</tr>
						<tr style="top: 30px;">
							<td class="requiredlabel" width="100%">
								<p style="color: darkred"><emxUtil:i18n localize="i18nId">emxComponents.ColorMatrix.UpgradeVersionNotes</emxUtil:i18n></p>
							</td>
						</tr>
						<tr style="top: 100px;">
							<td class="label" width="100%">
								<br>
								<!-- For Bug 345330 - Empty the value of the text field after some charcter is typed in manually. -->
								<input type="radio" id="Major" name="revision"> <emxUtil:i18n localize="i18nId">emxComponents.ColorMatrix.UpgradeVersionMajor</emxUtil:i18n></input>
								<br><br>
								<input type="radio" id="Minor" name="revision"> <emxUtil:i18n localize="i18nId">emxComponents.ColorMatrix.UpgradeVersionMinor</emxUtil:i18n></input>
								<!-- //XSSOK -->
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

							<button class="btn-primary" onclick="checkin()"><emxUtil:i18n localize="i18nId">emxComponents.ColorMatrix.UpgradeVersionsSure</emxUtil:i18n></button>

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
</body>

<%
	}catch (Exception e) {
		e.printStackTrace();
		throw  e;
	}
%>
<%@include file = "../emxUICommonEndOfPageInclude.inc" %>