<%@page
		import="com.matrixone.apps.domain.*,com.matrixone.apps.common.Person,com.matrixone.apps.domain.util.*,matrix.util.*,matrix.db.Context,com.matrixone.servlet.Framework"%>
<%@include file="../common/emxUIConstantsInclude.inc"%>
<%@include file="../common/emxNavigatorInclude.inc"%>
<%@include file="../emxStyleDefaultInclude.inc"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<%@ page import="static com.matrixone.apps.domain.DomainConstants.RELATIONSHIP_MEMBER" %>
<%@ page import="static com.matrixone.apps.domain.DomainConstants.TYPE_PERSON" %>
<%@ page import="com.matrixone.apps.framework.ui.UIUtil" %>
<link rel="stylesheet" href="../common/styles/emxUIForm.css">
<script src="../common/scripts/emxUICore.js" type="text/javascript"></script>
<script type="text/javascript" src="../common/scripts/jquery-latest.js"></script>
<script type="text/javascript" src="../common/scripts/emxUICore.js"></script>
<%
	String objectId = emxGetParameter(request, "objectId");
	//判断项目的面套和发泡的AME人员是否设置了
	System.out.println("objectId:" + objectId);
	DomainObject domainObject = DomainObject.newInstance(context, objectId);
	MapList mapList = domainObject.getRelatedObjects(
			context,
			RELATIONSHIP_MEMBER,
			TYPE_PERSON,
			new StringList(DomainConstants.SELECT_NAME),
			new StringList("attribute[Project Role]"),
			false,
			true,
			(short) 1,
			"",
			"attribute[Project Role]=='Foam AME representative' || attribute[Project Role]=='Trim AME representative'",
			0
	);
	//只能选择ESO任务进行复制
	if (mapList.size() < 2) {
		String strMess = EnoviaResourceBundle.getProperty(context, "emxProgramCentralStringResource", context.getLocale(), "emxProgramCentral.ProjectNotHas.FormAndTrimRoleMess");
		if (mapList.size() == 1) {
			//面套和发泡都不存在
			Map map = (Map) mapList.get(0);
			String role = UIUtil.getValue(map, "attribute[Project Role]");
			//需要判断是哪一个不存在
			if ("Foam AME representative".equalsIgnoreCase(role)) {
				strMess = EnoviaResourceBundle.getProperty(context, "emxProgramCentralStringResource", context.getLocale(), "emxProgramCentral.ProjectNotHas.TrimRoleMess");
			} else {
				strMess = EnoviaResourceBundle.getProperty(context, "emxProgramCentralStringResource", context.getLocale(), "emxProgramCentral.ProjectNotHas.FormRoleMess");
			}
		}
%>
<script type="text/javascript">
		var userConfirmed = confirm("<%=strMess%>");
		// var userConfirmed = confirm("当前项目的SDT角色：面套AME 与 发泡 AME未维护，确定要更新MBOM吗？");
		if (userConfirmed) {
			window.open("./JF_UpdateMbomDialogFs.jsp?objectId=<%=objectId%>", "", "width=800,height=500,left=300,top=200,scrollbars=no,location=no,fullscreen=no");
		} else {
		// 如果点击“取消”，直接关闭当前窗口（可选）
			window.close();
		}
</script>
<%
		return;
	}
%>
<html>
<script>
	window.open("./JF_UpdateMbomDialogFs.jsp?objectId=<%=objectId%>", "", "width=800,height=500,left=300,top=200,scrollbars=no,location=no,fullscreen=no");
	// var refreshURL = window.parent.location.href;
	// window.parent.location.href = refreshURL;
</script>
</html>