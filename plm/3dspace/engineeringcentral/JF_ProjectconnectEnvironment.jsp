<%@ page import="java.util.Map" %>
<%@ page import="com.matrixone.apps.domain.DomainObject" %>
<%@ page import="matrix.db.RelationshipType" %>
<%@ page import="com.matrixone.apps.domain.util.FrameworkUtil" %>
<%@ page import="matrix.util.StringList" %>
<%@ page import="com.matrixone.apps.domain.DomainRelationship" %>
<%@ page import="com.matrixone.apps.domain.util.MqlUtil" %>
<%@ page import="com.matrixone.apps.domain.util.ContextUtil" %>
<%@ page import="com.matrixone.apps.framework.ui.UIUtil" %>
<%@ page import="com.matrixone.apps.domain.util.EnoviaResourceBundle" %>
<!--
DR添加物理产品
-->
<%@include file = "../common/emxNavigatorInclude.inc"%>
<%@include file = "../common/emxNavigatorTopErrorInclude.inc"%>
<%@include file = "../common/enoviaCSRFTokenValidation.inc"%>
<%
    StringList errorList = new StringList();
    String errorMessage="";
    boolean flag = false;
    try {
        String strObjectId = emxGetParameter(request, "objectId");
        String[] tableRowIdList = emxGetParameterValues(request, "emxTableRowId");
        DomainObject project = DomainObject.newInstance(context, strObjectId);
        StringList EnvironmentIds = project.getInfoList(context, "from[JFProject2Environment].to.id");
        for (String tableRowId : tableRowIdList) {
            StringList tableIds = FrameworkUtil.split(tableRowId, "|");
            String vpmId = tableIds.get(0);
            DomainObject vpmObj = DomainObject.newInstance(context, vpmId);
            if(EnvironmentIds.contains(vpmId)){
                String name = vpmObj.getInfo(context, "attribute[EnterpriseExtension.V_PartNumber]");
                if (UIUtil.isNullOrEmpty(name)) {
                    name = vpmObj.getName();
                }
                errorList.add(name);
            }
        }
        String msg = EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", context.getLocale(), "emxComponents.JFProject2Environment.connect");
     errorMessage = errorList.toString()+""+msg;
        if (errorList.size() == 0){
            ContextUtil.pushContext(context);
            flag = true;
            for (String tableRowId : tableRowIdList) {
                StringList tableIds = FrameworkUtil.split(tableRowId, "|");
                String vpmId = tableIds.get(0);
                    DomainRelationship rel = project.addToObject(context, new RelationshipType("JFProject2Environment"), vpmId);
            }
    }
    }catch (Exception e){
        e.printStackTrace();

    }finally {
        if(flag){
            ContextUtil.popContext(context);
        }
    }
%>
<%@include file = "../common/emxNavigatorBottomErrorInclude.inc"%>
<script>
    let flag = <%=errorList.size()==0%>;
    if(flag) {
        var refreshURL = getTopWindow().getWindowOpener().location.href;
        getTopWindow().getWindowOpener().location.href = refreshURL;
        getTopWindow().closeWindow();
    }else{
        alert('<%=errorMessage%>');
    }
</script>
