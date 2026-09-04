<%@page
		import="com.matrixone.apps.domain.*,com.matrixone.apps.common.Person,com.matrixone.apps.domain.util.*,matrix.util.*,matrix.db.Context,com.matrixone.servlet.Framework"%>
<%@include file="../common/emxUIConstantsInclude.inc"%>
<%@include file="../common/emxNavigatorInclude.inc"%>
<%@include file="../emxStyleDefaultInclude.inc"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<%@ page import="com.matrixone.apps.common.util.ComponentsUtil" %>
<link rel="stylesheet" href="../common/styles/emxUIForm.css">
<script src="../common/scripts/emxUICore.js" type="text/javascript"></script>
<script type="text/javascript" src="../common/scripts/jquery-latest.js"></script>
<script type="text/javascript" src="../common/scripts/emxUICore.js"></script>
<%
	String objectId = emxGetParameter(request, "objectId");
	System.out.println("objectId:" + objectId);
	String itemURL = "../common/emxIndentedTable.jsp?" +
			"table=JFECRUpdateMBOMTable&program=JF_MBOM:getNotSyncMBOMECR,JF_MBOM:getAllMBOMECR&" +
			"programLabel=emxFramework.TableLabel.UpdateMBOMECR,emxFramework.TableLabel.AllUpdateMBOMECR&" +
			"selection=multiple&sortColumnName=Name&sortDirection=ascending&showApply=false&objectId=" + objectId;
	String mess = EnoviaResourceBundle.getProperty(context, "emxFrameworkStringResource", context.getLocale(), "emxFramework.Mess.ChangeDesc");
	String strHeader = EnoviaResourceBundle.getProperty(context, "emxFrameworkStringResource", context.getLocale(), "emxFramework.Command.JFUpdateMBOMCmd");
	String selectMess = ComponentsUtil.i18nStringNow("emxComponents.SelectOne.Mess",request.getHeader("Accept-Language"));
	String labelRequired = ComponentsUtil.i18nStringNow("emxComponents.labelRequired.ChangeDesc",request.getHeader("Accept-Language"));
//	String strURL = "../programcentral/JF_UpdateMBOMSubmit.jsp?objectId=" + objectId;
%>

<html>
<head>
	<meta charset="UTF-8">
	<title></title>
	<link rel="stylesheet" href="PPAPQueryResource/styles/bootstrap.min.css">
	<link rel="stylesheet" href="PPAPQueryResource/styles/jquery-ui.css">
	<script src="PPAPQueryResource/js/jquery-3.1.1.min.js"></script>
	<script src="PPAPQueryResource/js/jquery-ui.min.js"></script>
	<script src="PPAPQueryResource/js/bootstrap.js"></script>
	<script language="JavaScript" type="text/JavaScript">
		function checkSubmit() {
			//DOM对象
			const tableContent = document.getElementById("TableContent");
			//转换为window对象
			const table = tableContent.contentWindow;
			//拿取选中的行
			const  selectRowArr = table.emxEditableTable.getCheckedRows();
			const cdElement = document.getElementById("changeMess");
			const changeMess = cdElement.value;
			if (selectRowArr.length == 0) {
				alert("<%=selectMess%>");
			} else if ("" === changeMess || null === changeMess) {
				alert("<%=labelRequired%>");
			}else {
				//变更说明不允许编辑
				cdElement.disabled=true;
				const ChangeDesc = document.getElementById("ChangeDesc");
				ChangeDesc.value =  changeMess;
				//ECR选择的id
				let selectECRId = "" ;
				for (let i = 0; i < selectRowArr.length; i++) {
					const row = selectRowArr[i];
					const strOid = row.getAttribute("o");
					selectECRId += strOid;
					if (i < selectRowArr.length - 1) {
						selectECRId += ",";
					}
				}
				const ECRIds = document.getElementById("ECRIds");
				ECRIds.value = selectECRId;
				//发起请求
				// 禁用当前按钮
				const button = document.getElementsByTagName("button");
				for (var i = 0; i < button.length; i++) {
					button[i].disabled = true;
				}
				//table表不允许编辑
				const tableDoc = table.document;
				const spinner_div1 = tableDoc.getElementById("spinner_div1");
				spinner_div1.style.display = "block";
				const strURL = "../programcentral/JF_UpdateMBOMSubmit.jsp?objectId=" + "<%=objectId%>" + "&changeDesc=" + changeMess
				+ "&selectECRId=" + selectECRId;
				updateMBOM(strURL, spinner_div1);
			}
		}
		//发起请求
		function updateMBOM(strURL, spinner_div1) {
			//发请求更新MBOM
			jQuery.ajax({
				url: strURL,
				type: "GET",
				async: true,
				timeout: 60000000,
				success: function (res) {
					res = res.trim();
					console.log("res:", res);
					let words = res.split("{");
					let data = JSON.parse("{" + words[1]);
					let flag = data.flag;
					console.log("flag:", flag);
					let mess = data.mess;
					alert(mess);
					if ("Y" === flag) {
						getTopWindow().closeWindow();
						// getTopWindow().openerFindFrame(getTopWindow(),"detailsDisplay").emxEditableTable.refreshSelectedRows();
						getTopWindow().getWindowOpener().parent.document.location.href = getTopWindow().getWindowOpener().parent.document.location.href;
						// var refreshURL = getTopWindow().getWindowOpener().location.href;
						// getTopWindow().getWindowOpener().location.href = refreshURL;
					} else if ("W" === flag) {
						spinner_div1.style.display = "none";
					} else {
						getTopWindow().closeWindow();
					}
				}
			});
		}
	</script>
</head>
<body>
<style>
	textarea {
		height: 50px; /* 设置高度 */
		overflow-y: auto; /* 垂直方向显示滚动条 */
	}
	#divPageBody {
		position:absolute;
		top:40px;
		right:0;
		/*bottom:25px;*/
		left:0;
		padding:0;
		overflow:auto;
		background:#fff;
	}
</style>

<div id="pageHeadDiv">
	<form>
		<table>
			<tbody>
			<tr>
				<td class="page-title"><h2 id="ph"><%=strHeader%></h2></td>
			</tr>
			</tbody>
		</table>
	</form>
</div>
<div id='divPageBody'>
<%--	action="<%=strURL%>"--%>
	<form name="checkinForm" enctype="multipart/form-data" target="_parent" method="post" onsubmit="checkSubmit();"  scrolling="auto">
		<%@include file = "../common/enoviaCSRFTokenInjection.inc"%>
		<table class="list">
			<tbody>
			<tr id="calc_1">
				<ul id="code-content-ul">
<%--					style="height: 336px;"--%>
					<iframe id="TableContent"  src="<%=itemURL%>" style="height: 80%;" width="100%"  frameborder="no" border="0" marginwidth="0" marginheight="0" scrolling="no" allowtransparency="yes"></iframe>
				</ul>
			</tr>
			</tbody>
			<tbody>
			<tr class="even">
				<!-- //XSSOK -->
				<td width="75%">
					<table>
						<tbody>
						<tr>
							<td style="font-weight:bold;">
								<input name="objectId" id="objectId" maxlength="" size="20" title="objectId" value="<%=objectId%>" type="hidden" style="display:none">
								<input type="hidden" name="ECRIds" id="ECRIds" value="">
								<input type="hidden" name="ChangeDesc" id="ChangeDesc" value="">
							</td>
						</tr>
						</tbody>
					</table>
				</td>
			</tr>
			</tbody>
		</table>
		<table>
			<tbody>
			<tr id="calc_2">
				<td class="labelRequired" style="text-align: center"><label><%=mess%></label></td>
				<td class="inputField">
					<textarea value="" name="changeMess" id="changeMess"></textarea>
				</td>
			</tr>
			</tbody>
		</table>
	</form>
</div>
<div id="divPageFoot">
	<table width="100%" border="0" align="center">
		<tbody>
		<tr>
			<td class="buttons" align="right">
				<table border="0" cellspacing="0">
					<tbody>
					<tr>
						<td>
							<button class="btn-primary" onclick="checkSubmit()"><emxUtil:i18n localize="i18nId">emxFramework.Button.Submit</emxUtil:i18n></button>

							<a onclick="javascript:window.close()">
								<button class="btn-default"><emxUtil:i18n localize="i18nId">emxFramework.SecurityContextSelection.Cancel</emxUtil:i18n></button>
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