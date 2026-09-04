<%-- JF_SubIFReplaceFS.jsp - used for Checkin of file into Document Object
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
<%@ page import="com.matrixone.apps.domain.util.FrameworkUtil" %>
<%@ page import="matrix.util.StringList" %>
<%@ page import="com.matrixone.apps.domain.DomainObject" %>
<%@ page import="com.matrixone.apps.domain.util.MapList" %>
<%@ page import="com.matrixone.apps.domain.DomainConstants" %>
<%@ page import="java.util.Map" %>
<%@ page import="com.dassault_systemes.dssourcing.common.util.UIUtil" %>
<%@ page import="java.util.stream.Collectors" %>
<%@ page import="com.nomagic.esi.common.a.M" %>
<%@ page import="static com.matrixone.apps.common.util.JSPUtil.emxGetParameterValues" %>
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
	private static final Logger _logger =  LoggerFactory.getLogger("JF_DuplicateESOTaskFS.jsp");
%>
<%
	try {
	//emxProgramCentral.Command.JFDuplicateESOTask
	String strHeader = EnoviaResourceBundle.getProperty(context, "emxProgramCentralStringResource",context.getLocale(), "emxProgramCentral.Command.JFDuplicateESOTask");
	String mess = EnoviaResourceBundle.getProperty(context, "emxProgramCentralStringResource",context.getLocale(), "emxProgramCentral.Mess.SelectPhase");
	_logger.info("strHeader:{}", strHeader);
	//当前的页面的对象id
	String parentId = (String)emxGetParameter(request, "parentId");
	_logger.info("parentId:{}", parentId);
	//选择复制的id
	String objectId = (String)emxGetParameter(request, "objectId");
	_logger.info("objectId:{}", objectId);
		//找到当前选择对象所属的第一层级的阶段
	String phaseId = (String) JPO.invoke(context, "JF_ESO", null, "getESOTaskPhaseId", new String[]{objectId, DomainConstants.SELECT_ID}, String.class);
	_logger.info("phaseId:{}", phaseId);
	DomainObject domainObject = DomainObject.newInstance(context);
	//找到当前对象所属的项目
	domainObject.setId(objectId);
	String projectId = domainObject.getInfo(context, "to[Project Access Key].from.from[Project Access List].to.id");
	_logger.info("projectId:{}", projectId);
	//拿取项目下第一个层级的阶段对象
	MapList tempPhaseList = (MapList) JPO.invoke(context, "JF_ESO", null, "getProjectSpaceFirstPhase", new String[]{projectId}, MapList.class);
	_logger.info("tempPhaseList:{}", tempPhaseList);
	//构造提交请求的url
	String AMP  = "&";
	StringBuilder url = new StringBuilder();
	url.append("./JF_DuplicateESOTaskProcess.jsp?");
	url.append("parentId=");
	url.append(parentId);
	url.append(AMP);
	url.append("objectId=");
	url.append(objectId);
	url.append(AMP);
	url.append("phaseId=");
	url.append(phaseId);
	url.append(AMP);
	url.append("projectId=");
	url.append(projectId);
	String sURL =  url.toString();

%>
<script language="JavaScript">
	function checkinCancel() {
		getTopWindow().closeWindow();
	}
	function checkin(){
		const checkboxes = document.querySelectorAll('input[type="checkbox"]:checked');
		// 将NodeList转换为值数组
		const values = Array.from(checkboxes).map(checkbox => checkbox.value);
		// 以逗号分隔
		const result = values.join(', ');
		if(result.length === 0) {
			alert('<%=mess%>');
			e.preventDefault();
		} else {
			var selectPhaseIds = document.getElementById("selectPhaseIds");
			selectPhaseIds.value = result;
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
								<input name="parentId" id="parentId" maxlength="" size="20" title="parentId" value="<%=parentId%>" type="text" style="display:none">
								<input name="objectId" id="objectId" maxlength="" size="20" title="objectId" value="<%=objectId%>" type="text" style="display:none">
								<input name="phaseId" id="phaseId" maxlength="" size="20" title="phaseId" value="<%=phaseId%>" type="text" style="display:none">
								<input name="projectId" id="projectId" maxlength="" size="20" title="projectId" value="<%=projectId%>" type="text" style="display:none">
								<input name="selectPhaseIds" id="selectPhaseIds" maxlength="" size="20" title="selectPhaseIds" value="" type="text" style="display:none">
								<!-- //XSSOK -->
								<input type="hidden" name="__fcs___comment_0" value="">
							</td>
						</tr>
						<tr style="top: 100px;">
							<td class="label" width="100%">
								<!-- For Bug 345330 - Empty the value of the text field after some charcter is typed in manually. -->
								<%
									if (tempPhaseList != null && !tempPhaseList.isEmpty()) {
										for (int i = 0; i < tempPhaseList.size(); i++) {
											Map map = (Map)tempPhaseList.get(i);
											String id = UIUtil.getValue(map, DomainConstants.SELECT_ID);
											String type = UIUtil.getValue(map, DomainConstants.SELECT_TYPE);
											String name = UIUtil.getValue(map, DomainConstants.SELECT_NAME);
											boolean checked = id.equalsIgnoreCase(phaseId) ? Boolean.FALSE : Boolean.TRUE;
//											Boolean css = id.equalsIgnoreCase(phaseId) ? Boolean.TRUE : Boolean.FALSE;
											Boolean css = Boolean.FALSE;

								%>
								<div class="phase-item" style="margin-left: 50px">
									<input type="checkbox"
										   id="<%= id %>"
										   name="<%=name%>"
										   value="<%= id %>"
										   class="task-checkbox"
										<%= checked ? "" : "" %>
										<%= css ? "style=\"pointer-events: none; opacity: 0.5;\"" : "" %>
									><%= name %>
								</div>
								<%
									}
								}
								%>
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