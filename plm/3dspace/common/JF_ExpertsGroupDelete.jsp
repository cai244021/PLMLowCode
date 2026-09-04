<%@ page import="java.util.Map" %>
<%@ page import="java.util.logging.Logger" %>
<%@ page import="matrix.util.StringList" %>
<%@ page import="matrix.db.JPO" %>
<%@ page import="com.matrixone.apps.domain.Job" %>
<%@ page import="com.matrixone.apps.domain.DomainConstants" %>
<%@ page import="com.matrixone.apps.domain.util.EnoviaResourceBundle" %>
<%@ page import="com.matrixone.apps.domain.util.XSSUtil" %>
<%@ page import="com.matrixone.apps.domain.DomainObject" %>
<%@ page import="java.io.OutputStream" %>
<%@ page import="java.io.InputStream" %>
<%@ page import="org.apache.commons.io.IOUtils" %>
<%@ page import="java.net.URLEncoder" %>
<%@ page import="com.matrixone.apps.framework.ui.UIUtil" %>
<%@ page import="matrix.db.FileList" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="org.apache.poi.ss.usermodel.Workbook" %>
<%@ page import="com.matrixone.apps.domain.DomainRelationship" %>
<%@ page import="com.matrixone.apps.domain.util.MapList" %>
<%@ page import="com.matrixone.apps.domain.util.ContextUtil" %>
<%@ page pageEncoding="utf-8" %>
<%--  JDX_ManualConvertPdf.jsp   -
--%>
<%@include file="../emxUICommonAppInclude.inc" %>


<%
    String ids="";
    try {

        ContextUtil.pushContext(context);
        String[] selectIds = emxGetParameterValues(request, "emxTableRowId");
//        String flag = emxGetParameter(request, "flag");
//        String[] newId = com.matrixone.apps.common.util.ComponentsUIUtil.getSplitTableRowIds(emxGetParameterValues(request, "emxTableRowId"));

        System.out.println("selectIds---");
        System.out.println(selectIds[0]);

        StringList deleteRid = new StringList();
        for(int d=0;d<selectIds.length;d++){
            String deleteids=selectIds[d];
            String relid=deleteids.split("\\|")[0];
            deleteRid.add(relid);

        }
        System.out.println("deleteRid--->"+deleteRid);

        DomainRelationship.disconnect(context,deleteRid.toStringArray());

        System.out.println("delete--->end");
    } catch (Exception e) {
        e.printStackTrace();
	}finally {
        ContextUtil.popContext(context);
    }


%>
<script language="JavaScript" src="../common/scripts/emxUICore.js" type="text/javascript"></script>
<script language="JavaScript" src="../common/scripts/emxUIConstants.js" type="text/javascript"></script>
<script language="javascript">
    // command="Aer_EVAinfoTableCommand";
    // var sbframe	= findFrame(getTopWindow(),command);
    // console.log("sbframe",sbframe);
    // console.log(sbframe.length);
    // sbframe.refreshSBTable();
    // window.top.opener.location.href = window.top.opener.location.href;

    parent.location.href=parent.location.href;
</script>
