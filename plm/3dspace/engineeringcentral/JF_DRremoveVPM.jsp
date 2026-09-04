<%@ page import="matrix.util.StringList" %>
<%@ page import="com.matrixone.apps.domain.DomainConstants" %>
<%@ page import="com.matrixone.apps.domain.DomainRelationship" %>
<%@ page import="com.matrixone.apps.domain.util.MapList" %>
<%@ page import="matrix.db.JPO" %>
<%@ page import="com.matrixone.apps.domain.DomainObject" %>
<%@ page import="com.matrixone.apps.domain.util.FrameworkUtil" %>
<%@ page import="java.util.Arrays" %>
<%@ page import="com.matrixone.apps.common.util.ComponentsUIUtil" %>
<%@ page import="com.matrixone.apps.framework.ui.UIUtil" %>
<%@ page import="org.slf4j.LoggerFactory" %>
<%@ page import="org.slf4j.Logger" %>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="utf-8" %>
<!--
    dr移除关联的物理产品
-->
<%@include file = "../common/emxNavigatorInclude.inc"%>
<%@include file = "../common/emxNavigatorTopErrorInclude.inc"%>
<%@include file = "../common/enoviaCSRFTokenValidation.inc"%>
<%!
    //日志
    private static final Logger LOGGER = LoggerFactory.getLogger("JF_DRremoveVPM.jsp");
%>

<%
    String mess = "";
    try {
        String RELATIONSHIP_JFDR2VPMREFERENCE = "JFDR2VPMReference";
        String TYPE_VPMREFERENCE = "VPMReference";
        String strObjectId = emxGetParameter(request, "objectId");
        String[] tableRowIdList = emxGetParameterValues(request, "emxTableRowId");
        DomainObject dr = DomainObject.newInstance(context, strObjectId);
        StringList disconnectVPMId = new StringList();
        String[] splitTableRowIds = ComponentsUIUtil.getSplitTableRowIds(tableRowIdList);
        for (String splitTableRowId : splitTableRowIds) {
            DomainObject domainObject = DomainObject.newInstance(context,splitTableRowId);
            String isRoot = domainObject.getInfo(context,"to[JFDR2VPMReference]");
            if (!"TRUE".equals(isRoot)){
                mess = "只可以移除第一层级的零件";
                break;
            }
        }
        if (UIUtil.isNullOrEmpty(mess)){
            for(String tableRowId : tableRowIdList) {
                LOGGER.info("JF_DRremoveVPM.jsp-------::{}",tableRowId);
                StringList tableIds = FrameworkUtil.split(tableRowId,"|");
                DomainRelationship.disconnect(context,tableIds.get(0));
            }
            JPO.invoke(context, "JF_DR", null, "drDisVpmRemoveChangeControl", tableRowIdList, void.class);
        }

    }catch (Exception e){
        LOGGER.info("JF_DRremoveVPM.jsp======error",e);
    }
%>
<%@include file = "../common/emxNavigatorBottomErrorInclude.inc"%>
<script>
    var strMess = "<%=mess%>";
    if (strMess !== "" && strMess != null){
        alert("<%=mess%>");
    }else {
        var refreshURL = window.parent.location.href;
        window.parent.location.href = refreshURL;
        console.log('refreshURL='+refreshURL);
    }
</script>