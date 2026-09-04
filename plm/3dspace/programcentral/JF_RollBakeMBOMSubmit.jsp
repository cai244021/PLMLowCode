<%@ page import="java.util.Map" %>
<%@ page import="com.matrixone.apps.domain.DomainObject" %>
<%@ page import="matrix.db.RelationshipType" %>
<%@ page import="com.matrixone.apps.domain.util.FrameworkUtil" %>
<%@ page import="matrix.util.StringList" %>
<%@ page import="com.matrixone.apps.domain.DomainRelationship" %>
<%@ page import="com.matrixone.apps.domain.DomainConstants" %>
<%@ page import="org.slf4j.Logger" %>
<%@ page import="org.slf4j.LoggerFactory" %>
<%@ page import="com.matrixone.apps.domain.util.ContextUtil" %>
<%@ page import="com.matrixone.apps.framework.ui.UIUtil" %>
<%@ page import="com.matrixone.apps.common.util.ComponentsUIUtil" %>
<%@ page import="matrix.db.JPO" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="static com.matrixone.apps.common.util.JSPUtil.emxGetParameterValues" %>
<!--
    DR添加物理产品
-->
<%@include file = "../common/emxNavigatorInclude.inc"%>
<%@include file = "../common/emxNavigatorTopErrorInclude.inc"%>
<%!
    private static final Logger JF_LOGGER = LoggerFactory.getLogger("JF_ColorGroupVpmBomSubmit.jsp");
%>
<%
    Boolean isPush = Boolean.FALSE;
    String mess = DomainConstants.EMPTY_STRING;
    String flag = "Y";
    try {
        String[] tableRowIdList = emxGetParameterValues(request, "emxTableRowId");
        String strObjectId = emxGetParameter(request, "objectId");
        JF_LOGGER.info("tableRowIdList:{}", tableRowIdList);
        DomainObject domainObject = DomainObject.newInstance(context);
        StringList stringList = new StringList();
        String tableRowId = tableRowIdList[0];
        StringList tableIds = FrameworkUtil.split(tableRowId,"|");
        String oid = DomainConstants.EMPTY_STRING;
        if (tableIds.size() == 3) {
            oid = tableIds.get(0);
        } else{
            oid = tableIds.get(1);
        }
        //调用jpo
        HashMap<String, String> paramsMap = new HashMap<>();
        paramsMap.put("CRId", oid);
        paramsMap.put("projectId", strObjectId);
        //第一版
//        Boolean res = (Boolean) JPO.invoke(context, "JF_MBOM", JPO.packArgs (paramsMap), "RollBakeMBOMStructure", JPO.packArgs (paramsMap), Boolean.class);
        //第二版 json文件版本
        Boolean res = (Boolean) JPO.invoke(context, "JF_MBOM", JPO.packArgs (paramsMap), "RollBakeMBOMStructureFromJson", JPO.packArgs (paramsMap), Boolean.class);
        if (res) {
            mess = EnoviaResourceBundle.getProperty(context, "emxProgramCentralStringResource", context.getLocale(), "emxProgramCentral.RollBakeMBOM.Successful");

        } else {
            flag = "N";
            mess = EnoviaResourceBundle.getProperty(context, "emxProgramCentralStringResource", context.getLocale(), "emxProgramCentral.RollBakeMBOM.Failed");
        }
    }catch (Exception e){
        e.printStackTrace();
        flag = "N";
        mess = EnoviaResourceBundle.getProperty(context, "emxProgramCentralStringResource", context.getLocale(), "emxProgramCentral.RollBakeMBOM.Failed");
    } finally {

    }
%>
<%@include file = "../common/emxNavigatorBottomErrorInclude.inc"%>
<script>
    const flag = "<%=flag%>";
    alert("<%=mess%>");
    if ("Y" === flag) {
        // var pmsWBS = findFrame(getTopWindow(), "content");
        // const spinnerid = pmsWBS.document.getElementById("spinner_div1");
        // spinnerid.style.display="none";
        getTopWindow().closeWindow();
        // getTopWindow().openerFindFrame(getTopWindow(),"detailsDisplay").emxEditableTable.refreshSelectedRows();
        // getTopWindow().getWindowOpener().parent.document.location.href = getTopWindow().getWindowOpener().parent.document.location.href;
        var refreshURL = getTopWindow().getWindowOpener().location.href;
        getTopWindow().getWindowOpener().location.href = refreshURL;
    }
</script>
