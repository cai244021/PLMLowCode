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
<%@ page import="matrix.db.JPO" %>

<%@include file = "../common/emxNavigatorInclude.inc"%>
<%@include file = "../common/emxNavigatorTopErrorInclude.inc"%>
<%!
    private static final Logger NIO_LOG = LoggerFactory.getLogger("JF_TrimCreateProcess.jsp");
%>
<%
    NIO_LOG.info("JF_TrimCreateProcess start");
    String CreateQuanlity = emxGetParameter(request, "CreateQuanlity");
    String JFPartitionChooserCmdOID = emxGetParameter(request, "JFPartitionChooserCmdOID");
    String JFProjectNameOID = emxGetParameter(request, "ProjectNameOID");
    NIO_LOG.info("JFProjectNameOID:{}", JFProjectNameOID.toString());
    String JFPartitionChooserDisplay = emxGetParameter(request, "JFPartitionChooserCmdDisplay");
    String JFPartitionChooserValue = emxGetParameter(request, "JFPartitionChooserCmd");
    String JF_DirectBuy = emxGetParameter(request, "JF_DirectBuy");
    String JF_ProcurementType = emxGetParameter(request, "JF_ProcurementType");
    String description = emxGetParameter(request, "description");
    String PartNameCN = emxGetParameter(request, "PartNameCN");
    String PartNameEN = emxGetParameter(request, "PartNameEN");
    String JF_Detail_CN = emxGetParameter(request, "JF_Detail_CN");
    String JF_Detail_EN = emxGetParameter(request, "JF_Detail_EN");
    Map map = new HashMap<>();
    map.put("HttpServletRequest", request);
    map.put("CreateQuanlity", CreateQuanlity);
    map.put("JFPartitionChooserCmdOID",JFPartitionChooserCmdOID);
    map.put("JFPartitionChooserDisplay",JFPartitionChooserDisplay);
    map.put("JFPartitionChooserValue",JFPartitionChooserValue );
    map.put("JF_DirectBuy", JF_DirectBuy);
    map.put("JF_PartNameCN", PartNameCN);
    map.put("JF_PartNameEN", PartNameEN);
    map.put("JF_Detail_CN", JF_Detail_CN);
    map.put("JF_Detail_EN", JF_Detail_EN);
    map.put("JF_ProcurementType", JF_ProcurementType);
    map.put("description", description);
    map.put("JFProjectNameOID", JFProjectNameOID);
    String[] restParams = JPO.packArgs(map);
    boolean flag = JPO.invoke(context, "JF_Trim", null, "BatchCreateTrim",restParams, boolean.class);
    NIO_LOG.info("JF_TrimCreateProcess end:{}",flag);

%>
<script>

</script>
