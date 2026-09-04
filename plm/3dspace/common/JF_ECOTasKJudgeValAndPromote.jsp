<%@page import="com.matrixone.apps.domain.DomainObject"%>
<%@ page import="static com.matrixone.apps.common.util.JSPUtil.emxGetParameterValues" %>
<%@ page import="com.matrixone.apps.domain.DomainRelationship" %>
<%@ page import="matrix.util.StringList" %>
<%@ page import="com.matrixone.apps.domain.util.FrameworkUtil" %>
<%@ page import="java.util.Map" %>
<%@ page import="com.matrixone.apps.domain.util.MapList" %>
<%@ page import="com.matrixone.apps.domain.util.EnoviaResourceBundle" %>
<%@ page import="com.matrixone.apps.domain.util.ContextUtil" %>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="utf-8" %>
<script src="../common/scripts/emxUIModal.js" type="text/javascript"></script>
<script language="JavaScript" src="../common/scripts/emxUICore.js"></script>
<script language="JavaScript" src="../common/scripts/emxUIConstants.js"></script>
<script language="JavaScript" src="../common/scripts/emxUISearch.js"></script>
<%@include file="../emxUIFramesetUtil.inc"%>
<%
    String suiteKey = "emxComponentsStringResource";
    String RELATIONSHIP_REFERENCE_DOCUMENT = "Reference Document";
    StringBuffer stringBuffer = new StringBuffer();
    StringList bosel = new StringList();
    bosel.add(DomainObject.SELECT_ID);
    bosel.add(DomainObject.SELECT_NAME);
    bosel.add(DomainObject.SELECT_CURRENT);
    String alertMess = "";
    String alertDocMess = "";
    Boolean flag = true;
    try {
        String objectId = emxGetParameter(request, "objectId");
        DomainObject ecoTaskObj = DomainObject.newInstance(context, objectId);
        MapList mapList = ecoTaskObj.getRelatedObjects(context, RELATIONSHIP_REFERENCE_DOCUMENT, "Document", bosel, null, false, true, (short) 1, "", "", 0);
        String value = ecoTaskObj.getAttributeValue(context, "JF_DAActualFinishTime");
        alertMess = EnoviaResourceBundle.getProperty(context, suiteKey, context.getLocale(), "emxComponents.action.noticeDAActualTime");
        alertDocMess = EnoviaResourceBundle.getProperty(context, suiteKey, context.getLocale(), "emxComponents.action.noticeDATaskDoc");
        if("".equals(value)){
            stringBuffer.append(alertMess).append("\n");
        }

        if(mapList.isEmpty()){
            stringBuffer.append(alertDocMess);
        }

        if(stringBuffer.length() == 0){
            flag = false;
            ecoTaskObj.promote(context);
        }

//        int num = 0;
//        Map map = ecoTaskObj.getRelatedObject(context, "JFCO2ECOTask", false, bosel, null);
//        if( map != null){
//            String ecoId = (String) map.get(DomainObject.SELECT_ID);
//            DomainObject ecoObj = DomainObject.newInstance(context, ecoId);
//            MapList mapList = ecoObj.getRelatedObjects(context, "JFCO2ECOTask", "JF_ECOTask", bosel, null, false, true, (short) 0, "", "", 0);
//            for (int i = 0; i < mapList.size(); i++) {
//                Map ecoMap = (Map) mapList.get(i);
//                String current = (String) ecoMap.get("current");
//                if("Review".equals(current)){
//                    num++;
//                }
//            }
//
//            if(num == mapList.size()){
//                ContextUtil.pushContext(context);
//                ecoObj.promote(context);
//                ContextUtil.popContext(context);
//            }
//        }
    }catch (Exception e) {
        e.printStackTrace();
    }

%>
<html>
<script >
    if(<%=flag%>) {
        alert("<%=stringBuffer.toString()%>");
    }
    history.go(-1);
</script>
</html>
