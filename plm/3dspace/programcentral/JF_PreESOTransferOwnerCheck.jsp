<%@page import="com.matrixone.apps.domain.DomainObject"%>
<%@ page import="static com.matrixone.apps.common.util.JSPUtil.emxGetParameterValues" %>
<%@ page import="com.matrixone.apps.domain.DomainRelationship" %>
<%@ page import="matrix.util.StringList" %>
<%@ page import="com.matrixone.apps.domain.util.FrameworkUtil" %>
<%@ page import="com.matrixone.apps.domain.DomainConstants" %>
<%@ page import="com.matrixone.apps.domain.util.MapList" %>
<%@ page import="java.util.Iterator" %>
<%@ page import="java.util.Map" %>
<%@ page import="com.matrixone.apps.domain.util.EnoviaResourceBundle" %>
<%@ page import="matrix.db.JPO" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="java.util.StringTokenizer" %>
<%@ page import="com.matrixone.apps.framework.ui.UIUtil" %>
<%@ page import="matrix.db.AttributeList" %>
<%@ page import="matrix.db.Attribute" %>
<%@ page import="java.util.stream.Collectors" %>
<%@ page import="static com.matrixone.apps.domain.DomainConstants.SELECT_NAME" %>
<%@ page import="com.matrixone.apps.program.ProgramCentralConstants" %>
<%@ page import="org.slf4j.Logger" %>
<%@ page import="org.slf4j.LoggerFactory" %>
<%@ page import="static com.matrixone.apps.domain.DomainConstants.context1" %>
<%@ page import="org.apache.myfaces.shared.util.ComponentUtils" %>
<%@ page import="com.matrixone.apps.common.util.ComponentsUIUtil" %>
<%@ page import="com.matrixone.apps.program.ProgramCentralUtil" %>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="utf-8" %>
<script src="../common/scripts/emxUIModal.js" type="text/javascript"></script>
<script language="JavaScript" src="../common/scripts/emxUICore.js"></script>
<script language="JavaScript" src="../common/scripts/emxUIConstants.js"></script>
<script language="JavaScript" src="../common/scripts/emxUISearch.js"></script>
<script type="text/javascript" src="../common/scripts/jquery-latest.js"></script>
<%@include file="../common/emxNavigatorInclude.inc" %>
<%!
    private static final Logger _logger = LoggerFactory.getLogger("JF_PreCreateEsoTaskCheck.jsp");
%>

<%
    String strMess = DomainConstants.EMPTY_STRING;
    String strURL = DomainConstants.EMPTY_STRING;
    try {
        String[] strContextObjectId = emxGetParameterValues(request,"emxTableRowId");
        String strObjectId = emxGetParameter(request, "objectId");
        DomainObject domainObject = DomainObject.newInstance(context, strObjectId);
        String projectId = DomainConstants.EMPTY_STRING;
        if (!DomainConstants.TYPE_PROJECT_SPACE.equalsIgnoreCase(domainObject.getTypeName(context))) {
            projectId = domainObject.getInfo(context,"to[Project Access Key].from.from[Project Access List].to.id");
        } else {
            projectId = strObjectId;
        }

        //选中数据oid
        StringList selectedIds = new StringList();
        for(int i=0; i<strContextObjectId.length; i++){
            _logger.info("strContextObjectId[i]:{}", strContextObjectId[i]);
            Map rowMap = ProgramCentralUtil.parseTableRowId(context,strContextObjectId[i]);
            selectedIds.add((String) rowMap.get("objectId"));
        }
        _logger.info("selectedIds:{}", selectedIds);
        String user = context.getUser();
        StringList typeError = new StringList();
        StringList ownerError = new StringList();
        String typeMess = ComponentsUIUtil.getI18NString(context, context.getLocale().toString(), "emxComponents.ESOTransfer.TypeMess", new String[]{});
        String ownerMess = ComponentsUIUtil.getI18NString(context, context.getLocale().toString(), "emxComponents.ESOTransfer.OwnerMess", new String[]{});

        for (int i = 0; i < selectedIds.size(); i++) {
            String objectId = selectedIds.get(i);
            domainObject.setId(objectId);
            //判断该任务的类型
            String type = domainObject.getInfo(context, DomainConstants.SELECT_TYPE);
            String owner = domainObject.getInfo(context, DomainConstants.SELECT_OWNER);
            String name = domainObject.getInfo(context, DomainConstants.SELECT_NAME);
            if (!"JF_ESOTask".equalsIgnoreCase(type) && !"Task".equalsIgnoreCase(type)) {
                typeError.add(name);
            }
            if (!owner.equalsIgnoreCase(user)) {
                ownerError.add(name);
            }
        }
        _logger.info("typeError:{}", typeError);
        _logger.info("ownerError:{}", ownerError);

        StringBuilder stringBuilder = new StringBuilder();
        if (!typeError.isEmpty()) {
            stringBuilder.append(typeMess.replace("{1}", typeError.join(",")));
        }
        if (!ownerError.isEmpty()) {
            stringBuilder.append(ownerMess.replace("{1}", ownerError.join(",")));
        }
        //当任务名称是ESO和ESO签发的时候，获取ESO任务为CS的时候
        if (stringBuilder.length() > 0) {
            strMess = stringBuilder.toString();
 %>
<script language="Javascript">
    alert("<%=strMess%>");
</script>
<%
            return;
        }
        strURL = "../common/emxFullSearch.jsp?type=PERSON_CHOOSER&field=TYPES=type_Person&includeOIDprogram=JF_ESO:getESOTransferOwnerToChairManager" +
                "&projectId=" + projectId +
                "&table=AEFGeneralSearchResults&selection=single&submitAction=refreshCaller&submitURL=../common/JF_ESOTranferOwnerSubmit.jsp";
        strURL += "&task=" + selectedIds.join(",");
    }catch (Exception e) {
        e.printStackTrace();
    }

%>
<html>
<body>
<script language="Javascript">
    <%--showModalDialog("<%=strURL%>");--%>
    <%--showWizard("<%=strURL%>");--%>
    showChooser("<%=strURL%>", 400, 530);
    <%--window.open("<%=strURL%>", '', 'width=700,height=600,left=400,top=150,scrollbars=no,location=no,fullscreen=no', 'false');--%>
    // findFrame(getTopWindow(),"PMCWBS").emxEditableTable.refreshSelectedRows();
</script>
</body>
</html>
