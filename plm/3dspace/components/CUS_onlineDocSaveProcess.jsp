

<%@page pageEncoding="utf-8" %>
<%@include file="/common/emxNavigatorInclude.inc"%>
<%@include file="/common/emxNavigatorTopErrorInclude.inc"%>
<%@ page import="java.io.PrintWriter" %>
<%@page import="com.matrixone.apps.domain.util.MqlUtil"%>
<%@ page import="com.matrixone.apps.domain.DomainObject" %>
<%@ page import="com.matrixone.apps.framework.ui.UIUtil" %>
<%@ page import="matrix.db.BusinessObject" %>
<%@ page import="matrix.db.Context" %>
<%@ page import="com.matrixone.apps.domain.DomainRelationship" %>
<%@ page import="matrix.util.StringList" %>
<%@ page import="java.util.Map" %>
<%@ page import="matrix.db.JPO" %>
<%@ page import="java.util.logging.Logger" %>
<emxUtil:localize id="i18nId" bundle="emxProgramCentralStringResource" locale='<%= request.getHeader("Accept-Language") %>' />
<head>
<script language="JavaScript" src="../common/scripts/emxUIConstants.js"></script>
<script language="javascript" src="../common/scripts/emxUIModal.js"></script>
<script language="javascript" src="../common/scripts/emxUIFormUtil.js"></script>

</head>

<%
 Logger _logger= Logger.getLogger("CUS_onlineDocSaveProcess.jsp");
	try {
		_logger.info("CUS_onlineDocSaveProcess.jsp.jsp start  ");
		//	url: '../components/JD_onlineDocSaveProcess.jsp?objectId='+oid+'&file3deName='+file3deNameVar+'&onlineDocFileName='+onlineDocFileName,

		String strDocId = emxGetParameter(request, "objectId");
		String  file3deName= emxGetParameter(request, "file3deName");
		_logger.info("save ..file3deName=======>>>>>>>>>>"+file3deName);
		String  onlineDocFileName= emxGetParameter(request, "onlineDocFileName");
		_logger.info("save ..onlineDocFileName=======>>>>>>>>>>"+onlineDocFileName);

		if (!UIUtil.isNullOrEmpty(strDocId)) {


			Map onlineDocMap = (Map) JPO.invoke(context, "CUS_DocCustomAction", new String[]{strDocId,file3deName,onlineDocFileName}, "saveOnlineDocAction", new String[]{strDocId,file3deName,onlineDocFileName}, Map.class);
			String flags= onlineDocMap.get("flags").toString();
			_logger.info("flags"+flags);
			response.getWriter().write("flags@"+flags);
			_logger.info(" CUS_onlineDocSaveProcess.jsp. end  ");
		}else{
			//throw new Exception("升版异常：Invalid strProId");
		}

	} catch (Exception ex)
	{
		ex.printStackTrace();
		session.setAttribute("error.message" , ex.toString());
		throw ex;
	}





%>
<script>

	//window.parent.document.checkinForm.submit();

	//window.parent.document.checkinForm.submit();
	//window.close();
</script>
