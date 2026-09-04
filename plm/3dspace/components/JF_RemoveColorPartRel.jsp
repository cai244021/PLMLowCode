<%@page import="com.matrixone.apps.domain.DomainObject"%>
<%@ page import="static com.matrixone.apps.common.util.JSPUtil.emxGetParameterValues" %>
<%@ page import="com.matrixone.apps.domain.DomainRelationship" %>
<%@ page import="matrix.util.StringList" %>
<%@ page import="com.matrixone.apps.domain.util.FrameworkUtil" %>
<%@ page import="matrix.db.JPO" %>
<%@ page import="com.matrixone.apps.domain.util.ContextUtil" %>
<%@ page import="java.util.Map" %>
<%@ page import="com.matrixone.apps.domain.util.EnoviaResourceBundle" %>
<%@ page import="com.matrixone.apps.domain.DomainConstants" %>
<%@ page import="com.matrixone.apps.domain.util.MapList" %>
<%@ page import="com.matrixone.apps.framework.ui.UIUtil" %>
<%@ page import="java.util.stream.Collectors" %>
<%@ page import="java.util.HashSet" %>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="utf-8" %>
<script src="../common/scripts/emxUIModal.js" type="text/javascript"></script>
<script language="JavaScript" src="../common/scripts/emxUICore.js"></script>
<script language="JavaScript" src="../common/scripts/emxUIConstants.js"></script>
<script language="JavaScript" src="../common/scripts/emxUISearch.js"></script>
<%@include file="../emxUIFramesetUtil.inc"%>
<%
    String strMess = EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", context.getLocale(), "emxComponents.ColorPart.DeleteSuccess");
    try {
        String objectId = emxGetParameter(request, "objectId");
        String mode = emxGetParameter(request, "mode");
        String[] strSelectIds = emxGetParameterValues(request, "emxTableRowId");
        String contextUser = context.getUser().toString();
        if("delete".equals(mode)){
            HashSet deleteIds = new HashSet<>();
            StringList errorIds = new StringList();
            StringList connCIds = new StringList();
            for(String tableRowId : strSelectIds) {
                StringList tableIds = FrameworkUtil.split(tableRowId,"|");
                String oid = DomainConstants.EMPTY_STRING;
                if (tableIds.size() == 3) {
                    oid = tableIds.get(0);
                } else if (tableIds.size() == 4) {
                    oid = tableIds.get(1);
                }
                DomainObject domainObject = DomainObject.newInstance(context, oid);
                String owner = domainObject.getInfo(context, DomainConstants.SELECT_OWNER);
                String current = domainObject.getInfo(context, DomainConstants.SELECT_CURRENT);
                String name = domainObject.getInfo(context, DomainConstants.SELECT_NAME);
                String result = domainObject.getInfo(context, "to[JFVPMReference2JFColorMatrix]");
                //查询是否关联了整椅 判断该颜色矩阵是否关联了整椅 emxComponents.Color.ConnectWC
                if (contextUser.equalsIgnoreCase(owner) && "Create".equalsIgnoreCase(current) && "FALSE".equalsIgnoreCase(result)) {
                    deleteIds.add(oid);
                    MapList mapList = domainObject.getRelatedObjects(
                            context,
                            "JFColorMatrix2JFColorGroup,JFColorGroup2JFColorStyle",
                            "JFColorGroup,JFColorStyle",
                            new StringList(DomainConstants.SELECT_ID),
                            new StringList(),
                            false,
                            true,
                            (short) 0,
                            "",
                            "",
                            0
                    );
                    StringList allChildren = (StringList) mapList.stream().map(m -> {
                        Map map = (Map) m;
                        return UIUtil.getValue(map, DomainConstants.SELECT_ID);
                    }).collect(Collectors.toCollection(StringList::new));
                    deleteIds.addAll(allChildren);
                } else if ("TRUE".equalsIgnoreCase(result)) {
                    connCIds.add(name);
                } else {
                    errorIds.add(name);
                }
            }
            if (!deleteIds.isEmpty()) {
                StringList strings = StringList.create(deleteIds);
                String[] stringArray = strings.toStringArray();
                ContextUtil.pushContext(context);
                DomainObject.deleteObjects(context, stringArray);
                ContextUtil.popContext(context);
            }
            if (errorIds.size() > 0 || connCIds.size() > 0) {
                strMess = EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", context.getLocale(), "emxComponents.ColorPart.DeleteFailed");
            }
            if (!errorIds.isEmpty()){
                strMess += EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", context.getLocale(), "emxComponents.ColorDelete.OwnerAndCurrent");
                strMess += ":" + errorIds.join(",");
            }
            if (!connCIds.isEmpty()) {
                strMess += EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", context.getLocale(), "emxComponents.Color.ConnectWC");
                strMess += ":" + connCIds.join(",");
            }
        }
    }catch (Exception e) {
        strMess = EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", context.getLocale(), "emxComponents.ColorPart.DeleteFailed");
        e.printStackTrace();
    }

%>
<html>
<script>
    alert("<%=strMess%>");
    var refreshURL = window.parent.location.href;
    window.parent.location.href = refreshURL;
</script>
</html>
