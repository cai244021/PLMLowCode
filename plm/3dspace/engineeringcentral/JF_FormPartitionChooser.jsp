<%@ page import="com.dscn.plm.util.NioJDUtils" %>
<%@ page import="java.util.Properties" %>
<%@ page import="com.matrixone.apps.domain.DomainObject" %>
<%@ page import="matrix.util.StringList" %>
<%@ page import="com.matrixone.apps.domain.DomainConstants" %>
<%@ page import="com.matrixone.apps.domain.util.MapList" %>
<%@ page import="java.util.Map" %>
<%@ page import="com.matrixone.apps.domain.util.FrameworkUtil" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="matrix.db.RelationshipType" %>
<%@ page import="com.matrixone.apps.domain.DomainRelationship" %>
<%@ page import="matrix.db.Context" %>
<%@ page import="com.matrixone.apps.framework.ui.UIUtil" %>
<%@ page import="java.util.Enumeration" %>
<%@ page import="org.slf4j.Logger" %>
<%@ page import="org.slf4j.LoggerFactory" %>
<%@ page import="com.aspose.pdf.operators.Do" %>

<%@include file = "../common/emxNavigatorInclude.inc"%>
<%@include file = "../common/emxNavigatorTopErrorInclude.inc"%>
<%!
    private static final Logger NIO_LOG = LoggerFactory.getLogger("JF_FormPartitionChooser.jsp");
%>
<%
    String title = "";
    String classId="";
    String description="";
    try {
        String ATTRIBUTE_PARTITIONCN = "JF_VPMReference.JF_PartNameCN";
        String ATTRIBUTE_PARTITIONEN = "JF_VPMReference.JF_PartNameEN";
        String tableRowId = emxGetParameter(request, "emxTableRowId");
         classId = FrameworkUtil.split(tableRowId, "|").get(0);//选择的对象
        DomainObject obj = DomainObject.newInstance(context);

        if(UIUtil.isNotNullAndNotEmpty(classId)){
            obj.setId(classId);
            title =   obj.getInfo(context, DomainConstants.SELECT_ATTRIBUTE_TITLE);
            description =   obj.getInfo(context, DomainConstants.SELECT_DESCRIPTION);
        }

    }catch (Exception e){
        e.printStackTrace();
    }
%>
<script>
// var refreshURL = window.top.opener.document.location.href;
var targetWindow = window.top.opener;
var vfieldNameDisplay=targetWindow.document.forms[0]["JFPartitionChooserCmdDisplay"];
var vfieldNameActual=targetWindow.document.forms[0]["JFPartitionChooserCmd"];
var vfieldNameOID=targetWindow.document.forms[0]["JFPartitionChooserCmdOID"];
vfieldNameActual.value='<%=description%>';
vfieldNameDisplay.value='<%=title%>';
vfieldNameOID.value='<%=classId%>';
//window.top.opener.document.location.href = refreshURL;
window.top.close();
</script>
