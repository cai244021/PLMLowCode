<%@page import="com.matrixone.apps.domain.DomainObject"%>
<%@ page import="static com.matrixone.apps.common.util.JSPUtil.emxGetParameterValues" %>
<%@ page import="matrix.db.RelationshipType" %>
<%@ page import="org.slf4j.Logger" %>
<%@ page import="com.matrixone.apps.domain.util.PropertyUtil" %>
<%@ page import="com.matrixone.apps.program.ProgramCentralUtil" %>
<%@ page import="java.util.Map" %>
<%@ page import="matrix.util.StringList" %>
<%@ page import="com.matrixone.apps.domain.util.FrameworkUtil" %>
<%@ page import="com.matrixone.apps.domain.DomainRelationship" %>
<%@ page import="static com.matrixone.apps.domain.DomainConstants.SELECT_ID" %>
<%@ page import="com.google.gson.Gson" %>
<%@ page import="static com.matrixone.apps.domain.DomainConstants.context1" %>
<%@ page import="static com.matrixone.apps.domain.MultiValueSelects.ATTRIBUTE_TITLE" %>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="utf-8" %>
<script language="Javascript" src="../common/scripts/emxUITableUtil.js"></script>
<script language="Javascript" src="../common/scripts/emxUICore.js"></script>
<script language="Javascript" src="../common/scripts/emxUIConstants.js"></script>
<%@include file="../emxUIFramesetUtil.inc"%>
<%
    String relationship = emxGetParameter(request, "rel");
    String relationshipName = PropertyUtil.getSchemaProperty(relationship);
    String strType = emxGetParameter(request, "Type");
    String strPolicy = emxGetParameter(request, "policy");
    String strSelectRowId = emxGetParameter(request, "selectId");
    String strLanguage = request.getHeader("Accept-Language");   //PRG:RG6:R212:1-Jun-2011:IR-111810V6R2012x
    //添加内容
    String[] tableRowIdList = emxGetParameterValues(request, "emxTableRowId");
//    StringList res  = new StringList();
    if (tableRowIdList != null) {
        //整椅
        DomainObject domainObject = DomainObject.newInstance(context, strSelectRowId);
        //分解选择的对象id
//        StringList allContent = new StringList();
        DomainObject cos = DomainObject.newInstance(context);
//        String strMxl = "<mxRoot><action><![CDATA[add]]></action> <data status=\"committed\"> <item oid=\"$1\" relId= \"$2\" pid=\"$3\" direction=\"from\"/> </data></mxRoot>";
        for (int i = 0 ; i < tableRowIdList.length; i++) {
            Map rowMap = ProgramCentralUtil.parseTableRowId(context,tableRowIdList[i]);
            String strCosLibId = (String) rowMap.get("objectId");
            cos.setId(strCosLibId);
            String strDes = cos.getDescription(context);
            //获取成本库属性
            String strTitle = cos.getAttributeValue(context,ATTRIBUTE_TITLE);
            //创建参考项目
            String strNewOid = FrameworkUtil.autoName(context, strType, strPolicy);
            DomainObject newObj = DomainObject.newInstance(context, strNewOid);
            newObj.setDescription(context,strDes);
            newObj.setAttributeValue(context,ATTRIBUTE_TITLE,strTitle);
            //整椅和参考项目关系
            DomainRelationship rel = DomainRelationship.connect(context, domainObject, relationshipName,newObj );
            //参考项目和原始成本关系
            DomainRelationship.connect(context,newObj , "JFCostReferConst2Cost",cos );
//            String strRelId = rel.select(context,SELECT_ID);
//            String strFullXml = strMxl.replace("$1", strNewOid).replace("$2", strRelId).replace("$3", strSelectRowId);
//            res.add(strFullXml);
        }

    }
%>
<script >
    const  iFra = getTopWindow().getWindowOpener();
    if (iFra){
        iFra.refreshSBTable();
    }
    parent.closeWindow();

</script>


