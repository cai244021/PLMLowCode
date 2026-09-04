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
<%@ page import="java.util.Map" %>
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
<script type="text/javascript" src="../common/scripts/jquery-latest.js"></script>
<jsp:useBean id="formBean" scope="page" class="com.matrixone.apps.common.util.FormBean" />
<%!
	private static final Logger NIO_LOG = LoggerFactory.getLogger("JF_uploadPartCheckinDialogFS.jsp");
%>
<%
	Map emxCommonDocumentCheckinData = (Map) session.getAttribute("emxCommonDocumentCheckinData");
	//ecrid
	String strObjectId = (String) emxCommonDocumentCheckinData.get("objectId");
	//上传的清单类型： 检具(InspectionPart) 模具(MouldPart) 产线(ProdLinePart)
	String strType = (String) emxCommonDocumentCheckinData.get("type");
	String strLanguage = (String) emxCommonDocumentCheckinData.get("strLanguage");
	String mode = (String) emxCommonDocumentCheckinData.get("mode");
	NIO_LOG.info("mode:{}",mode);
	String strHeader = null;
	//form上传文件页面的Header
	switch (mode) {
		case "project" : strHeader = "ProjectHeader"; break;
		case "NewECR" : strHeader = "NewECRHeader"; break;
		case "Trim" : strHeader = "TrimHeader"; break;
		case "ProdLinePart" : strHeader = "ProdLinePartHeader"; break;
		case "TierX" : strHeader = "TierXHeader"; break;
		case "ChairManager" : strHeader = "ProjectHeader"; break;
		case "PartList" : strHeader="PartListHeader"; break;

	}
	strHeader = EnoviaResourceBundle.getProperty(context, "ProgramCentral", "emxProgramCentral.UploadListMess." + strHeader, strLanguage);
	//没有选择文件的提示信息
	String strNoSelectionFile = EnoviaResourceBundle.getProperty(context, "ProgramCentral", "emxProgramCentral.UploadListMess.NoSelectionFile", strLanguage);

	//构造提交请求的url
	String AMP                  = "&";
	StringBuilder url = new StringBuilder();
	url.append("./JF_uploadPartCheckinProcess.jsp?");
	url.append("objectId=");
	url.append(strObjectId);
	url.append(AMP);
	url.append("type=");
	url.append(strType);
	url.append(AMP);
	url.append("ProgramSuiteKey=");
	url.append("ProgramCentral");
	url.append(AMP);
	url.append("SuiteKey=");
	url.append("Components");
	url.append(AMP);
	url.append("strLanguage=");
	url.append(strLanguage);
	url.append(AMP);
	url.append("mode=");
	url.append(mode);
	String sURL =  url.toString();
	try {
%>
<script language="JavaScript">

	function checkinCancel() {
		getTopWindow().closeWindow();
	}
	function checkin(){
		var file;
		for (var i = 0; i < document.checkinForm.elements.length; i++) {
			console.log(document.checkinForm.elements[i].name);
			console.log(document.checkinForm.elements[i].value);
			var ei = document.checkinForm.elements[i].name;
			if (ei.substring(0,5)== "bfile") {
				file = document.checkinForm.elements[i].value;
				break;
			}
		}
		if (file == "" || file == null) {
			alert("<%=strNoSelectionFile%>")
		} else {
			document.checkinForm.submit();
		}
		// getTopWindow().callCheckout("62210.22193.44026.34827", 'download', null, 'generic', null, null, null, null, 'null', '1', null, null, null, null, null, null, null, null, null, null);
	}

	function downloadTemplate() {
		//发请求下载模板文件
		var strURL = "../programcentral/JF_ExportTemplateCheckout.jsp?download=ECRPart";
		jQuery.ajax({
			url: strURL,
			type: "GET",
			async: true,
			timeout: 60000000,
			success: function (res) {
				res = res.trim();
				let words = res.split("{");
				let data = JSON.parse("{" + words[1]);
				let fileName = data.fileName;
				let base64File = data.base64File;
				console.log("fileName：", fileName);
				if (fileName.length > 0) {
					let bstr = atob(base64File),
							n = bstr.length,
							u8arr = new Uint8Array(n);
					while (n--) {
						u8arr[n] = bstr.charCodeAt(n);
					}
					let blob = new Blob([u8arr]);
					console.log("blob:", blob);
					let link = document.createElement('a');
					link.href = window.URL.createObjectURL(blob);
					link.download = fileName;
					link.click();
					window.URL.revokeObjectURL(link.href); // 修正这里，释放链接
				}
			}
		});
	}
</script>
<%@include file = "../emxUICommonHeaderEndInclude.inc"%>
<body>
<style>
	#divPageBody {
		position:absolute;
		top:79px;
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
								<%--                                <img src="../common/images/utilMenuArrow.gif">--%>
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
	<form name="checkinForm" enctype="multipart/form-data" target="_parent" method="post" onsubmit="checkin();" action="<%=sURL%>" scrolling="auto">
		<%@include file = "../common/enoviaCSRFTokenInjection.inc"%>
		<table class="list">
			<tbody>
			<tr>
				<!-- //XSSOK -->
				<th class="requiredlabel" width="75%">
					<framework>
						<emxUtil:i18n localize="i18nId">emxComponents.Common.File</emxUtil:i18n>
					</framework>
					<framework>
						| <emxUtil:i18n localize="i18nId">emxComponents.Common.Format</emxUtil:i18n>
					</framework>
				</th>

				<framework>
					<th class="" width="25%">
						<emxUtil:i18n localize="i18nId">emxComponents.Common.Comments</emxUtil:i18n>
					</th>
				</framework>
			</tr>


			<tr class="even">
				<!-- //XSSOK -->
				<td width="75%">
					<table>
						<tbody>
						<tr>
							<td style="font-weight:bold;">
								<input name="objectId" id="objectId" maxlength="" size="20" title="objectId" value="<%=strObjectId%>" type="text" style="display:none">
								<input type="hidden" name="oldFileName" value="">
								<input type="hidden" name="oid" value="">
								<!-- //XSSOK -->
								<input type="hidden" name="__fcs___comment_0" value="">
							</td>
						</tr>
						<tr>
							<td>

								<!-- For Bug 345330 - Empty the value of the text field after some charcter is typed in manually. -->
								<input type="file" name="bfile" size="30" onpaste="return false;" onkeydown="this.blur()" onkeypress="displaymessage(this);return false;" value="">
								<input type="hidden" name="fileName" value="">
								<!-- //XSSOK -->

							</td>
						</tr>
						<tr>
							<td style="padding:3px;">

								<select name="format" size="1">
									<!-- //XSSOK -->
									<option value="xlsx">xlsx</option>

									<!-- //XSSOK -->
									<option value="xls">xls</option>

								</select>

							</td>
						</tr>
						</tbody>
					</table>
				</td>
				<framework>
					<td width="25%">
						<textarea rows="3" name="comments" cols="40" wrap></textarea>
					</td>
				</framework>
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
							<%
								if ("NewECR".equalsIgnoreCase(mode)) {
							%>
							<button class="btn-primary" onclick="downloadTemplate()"><emxUtil:i18n localize="i18nId">emxComponents.download.template</emxUtil:i18n></button>
							<%
								}
							%>
							<button class="btn-primary" onclick="checkin()"><emxUtil:i18n localize="i18nId">emxComponents.Button.Done</emxUtil:i18n></button>

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