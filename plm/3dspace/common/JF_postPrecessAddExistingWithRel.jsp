<%@page import="com.matrixone.apps.domain.DomainObject"%>
<%@ page import="static com.matrixone.apps.common.util.JSPUtil.emxGetParameterValues" %>
<%@ page import="matrix.db.RelationshipType" %>
<%@ page import="org.slf4j.Logger" %>
<%@ page import="org.slf4j.LoggerFactory" %>
<%@ page import="com.matrixone.apps.domain.DomainConstants" %>
<%@ page import="com.matrixone.apps.domain.DomainRelationship" %>
<%@ page import="com.matrixone.apps.domain.util.PropertyUtil" %>
<%@ page import="java.util.Enumeration" %>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="utf-8" %>
<script src="../common/scripts/emxUIModal.js" type="text/javascript"></script>
<script language="JavaScript" src="../common/scripts/emxUICore.js"></script>
<script language="JavaScript" src="../common/scripts/emxUIConstants.js"></script>
<script language="JavaScript" src="../common/scripts/emxUISearch.js"></script>
<%@include file="../emxUIFramesetUtil.inc"%>
<%!
    private static final Logger _logger =  LoggerFactory.getLogger("JF_postPrecessAddExistingWithRel.jsp");
%>
<%
    _logger.info("JF_postPrecessAddExistingWithRel start");

    Enumeration em = request.getParameterNames();
    while(em.hasMoreElements()){
        String strName = (String)em.nextElement();
        String [] values = request.getParameterValues(strName);
        System.out.print(strName+" = ");
        for(String str : values){
            System.out.print(str+"  ");
        }
        System.out.println();
    }

    String relationship = emxGetParameter(request, "rel");
    String relationshipName = PropertyUtil.getSchemaProperty(relationship);
    String type = emxGetParameter(request, "Type");
    String objectId = emxGetParameter(request, "objectId");
    String strLanguage = request.getHeader("Accept-Language");   //PRG:RG6:R212:1-Jun-2011:IR-111810V6R2012x
    //添加内容
    String[] tableRowIdList = emxGetParameterValues(request, "emxTableRowId");
    try {
        if (tableRowIdList != null) {
            //分解选择的对象id
            String[] allContent = new String[tableRowIdList.length];
            for (int i = 0; i < tableRowIdList.length; i++) {
                String[] split = tableRowIdList[i].split("\\|");
                String strSelectId = split[1].trim();
                allContent[i] = strSelectId;
            }
            _logger.info("allContent:{} ,objectId :{}",allContent,objectId);
            DomainObject domainObject = DomainObject.newInstance(context, objectId);
            domainObject.addRelatedObjects(context, new RelationshipType(relationshipName), true, allContent);
        }
    }catch (Exception e){
        e.printStackTrace();
    }
%>
<html>
<script >
    getTopWindow().closeWindow();
    window.top.getWindowOpener().top.refreshTablePage();
</script>
</html>

