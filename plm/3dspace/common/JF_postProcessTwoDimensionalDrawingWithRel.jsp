<%@page import="com.matrixone.apps.domain.DomainObject"%>
<%@ page import="static com.matrixone.apps.common.util.JSPUtil.emxGetParameterValues" %>
<%@ page import="matrix.db.RelationshipType" %>
<%@ page import="org.slf4j.Logger" %>
<%@ page import="org.slf4j.LoggerFactory" %>
<%@ page import="com.matrixone.apps.domain.DomainConstants" %>
<%@ page import="com.matrixone.apps.domain.DomainRelationship" %>
<%@ page import="com.matrixone.apps.domain.util.PropertyUtil" %>
<%@ page import="java.util.Enumeration" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="matrix.db.JPO" %>
<%@ page import="com.matrixone.apps.domain.util.MapList" %>
<%@ page import="java.util.Collections" %>
<%@ page import="java.util.stream.Collectors" %>
<%@ page import="matrix.util.StringList" %>
<%@ page import="com.matrixone.apps.domain.util.ContextUtil" %>
<%@ page import="com.matrixone.apps.domain.util.EnoviaResourceBundle" %>
<%@ page import="com.matrixone.apps.framework.ui.MqlNoticeUtil" %>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="utf-8" %>
<script src="../common/scripts/emxUIModal.js" type="text/javascript"></script>
<script language="JavaScript" src="../common/scripts/emxUICore.js"></script>
<script language="JavaScript" src="../common/scripts/emxUIConstants.js"></script>
<script language="JavaScript" src="../common/scripts/emxUISearch.js"></script>
<%@include file="../emxUIFramesetUtil.inc"%>
<%!
    private static final Logger _logger =  LoggerFactory.getLogger("JF_postProcessTwoDimensionalDrawingWithRel.jsp");
%>
<%
    _logger.info("JF_postProcessTwoDimensionalDrawingWithRel start");
    String objectId = emxGetParameter(request, "objectId");
    DomainObject domainObject = DomainObject.newInstance(context, objectId);
    //获取当前对象已经获取到的图纸
    Map temMap = new HashMap();
    temMap.put("objectId", objectId);
    MapList reslutList = (MapList) JPO.invoke(context, "JF_PublicProjectQuery", new String[0], "getAllReferenceDrawing", JPO.packArgs(temMap), MapList.class);
    StringList existList = (StringList)reslutList.stream().map(m->{
        Map map = (Map)m;
       return map.get(DomainConstants.SELECT_ID);
    }).collect(Collectors.toCollection(StringList::new));

    //添加内容
    String[] tableRowIdList = emxGetParameterValues(request, "emxTableRowId");
//    boolean isPush = false;
    boolean flag = true;
    String message = EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", context.getLocale(), "emxComponents.VPMReference.noticeExists");
    StringList resultList = new StringList();
    try {
//        ContextUtil.pushContext(context);
//        isPush  = true;
        if (tableRowIdList != null) {
            //分解选择的对象id
            String[] allContent = new String[tableRowIdList.length];
            for (int i = 0; i < tableRowIdList.length; i++) {
                String[] split = tableRowIdList[i].split("\\|");
                String strSelectId = split[1].trim();
                allContent[i] = strSelectId;
                DomainObject object = DomainObject.newInstance(context, strSelectId);
                if(!existList.contains(strSelectId)) {
                    String type = object.getInfo(context, "type");
                    if ("Document".equals(type)) {
                        domainObject.addToObject(context, new RelationshipType("Reference Document"), strSelectId);
                        object.setAttributeValue(context, "JF_DocumentType", "Drawing");
                    } else if ("Drawing".equals(type)) {
                        domainObject.addFromObject(context, new RelationshipType("XCADBaseDependency"), strSelectId);
                    }
                }else{
                    //
                    flag=false;
                    resultList.add(object.getInfo(context,DomainConstants.SELECT_NAME));
                    continue;
                }
            }
            message = message+" "+resultList.join(",");
            _logger.info("allContent:{} ,objectId :{}",allContent,objectId);
        }
    }catch (Exception e){
        e.printStackTrace();
    }finally {
       /* if(isPush){
            ContextUtil.popContext(context);
        }*/
    }
%>
<html>
<script >
    let flag ='<%=flag%>'
    if(flag==='false'){
        alert('<%=message%>')
    }
    getTopWindow().closeWindow();
    window.top.getWindowOpener().top.refreshTablePage();
</script>
</html>

