<%@page import="com.matrixone.apps.domain.DomainObject"%>
<%@ page import="static com.matrixone.apps.common.util.JSPUtil.emxGetParameterValues" %>
<%@ page import="com.matrixone.apps.domain.DomainRelationship" %>
<%@ page import="matrix.util.StringList" %>
<%@ page import="com.matrixone.apps.domain.util.FrameworkUtil" %>
<%@ page import="com.matrixone.apps.domain.util.ContextUtil" %>
<%@ page import="com.matrixone.apps.domain.util.EnoviaResourceBundle" %>
<%@ page import="com.matrixone.apps.domain.DomainConstants" %>
<%@ page import="com.matrixone.apps.framework.ui.UIUtil" %>
<%@ page import="java.util.HashSet" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="matrix.db.JPO" %>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="utf-8" %>
<script src="../common/scripts/emxUIModal.js" type="text/javascript"></script>
<script language="JavaScript" src="../common/scripts/emxUICore.js"></script>
<script language="JavaScript" src="../common/scripts/emxUIConstants.js"></script>
<script language="JavaScript" src="../common/scripts/emxUISearch.js"></script>
<%@include file="../emxUIFramesetUtil.inc"%>
<%
    Boolean isPush = Boolean.FALSE;
    String mess = DomainConstants.EMPTY_STRING;
    String flag = "Y";
    String strFailedMess = EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", context.getLocale(), "emxComponents.ColorPart.DeleteFailed");
    try {
        String objectId = emxGetParameter(request, "objectId");
        String mode = emxGetParameter(request, "mode");
        String type = emxGetParameter(request, "type");
        String[] strSelectIds = emxGetParameterValues(request, "emxTableRowId");
        StringList selectIds = new StringList();
        String strSuccessMess = EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", context.getLocale(), "emxComponents.ColorPart.DeleteSuccess");
        String strFailedDeleteCostAnalysis = EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", context.getLocale(), "emxComponents.MessFailed.DeleteCostAnalysis");
        for(String tableRowId : strSelectIds) {
            StringList tableIds = FrameworkUtil.split(tableRowId,"|");
            String oid = DomainConstants.EMPTY_STRING;
            if (tableIds.size() == 3) {
                oid = tableIds.get(0);
            } else if (tableIds.size() == 4) {
                oid = tableIds.get(1);
            }
            selectIds.add(oid);
        }
        HashSet deleteIds = new HashSet<>();
        HashSet removeIds = new HashSet();
        HashSet errorIds = new HashSet();
        String contextUser = context.getUser().toString();
        if("delete".equals(mode)){
            switch (type){
                case "JFCostAnalysis" :{
                    for(String oid : selectIds) {
                        DomainObject domainObject = DomainObject.newInstance(context, oid);
                        String strOwner  = domainObject.getInfo(context, DomainConstants.SELECT_OWNER);
                        String strType  = domainObject.getInfo(context, DomainConstants.SELECT_TYPE);
                        String strName = domainObject.getInfo(context, DomainConstants.SELECT_NAME);
                        if (!contextUser.equalsIgnoreCase(strOwner)) {
                            errorIds.add(strName);
                            mess = strFailedDeleteCostAnalysis;
                            continue;
                        }
                        if (type.equalsIgnoreCase(strType)) {
                            StringList costAnalysis2CostItemList = domainObject.getInfoList(context, "from[JFCostAnalysis2CostItem].to.id");
                            deleteIds.addAll(costAnalysis2CostItemList);
                            deleteIds.add(oid);
                        } else if ("JFCostAnalysisItem".equalsIgnoreCase(strType)) {
                            deleteIds.add(oid);
                        }
                    }
                    break;
                }
                case "EsoReviewRecord" : {
                    HashMap<String, Object> deleteParamsMap = new HashMap<>();
                    deleteParamsMap.put("reviewIds", selectIds);
                    JPO.invoke(
                            context,
                            "JF_ESO",
                            new String[]{},
                            "deleteESOReviewRecords",
                            JPO.packArgs(deleteParamsMap),
                            Boolean.class);
                    break;
                }
                default:{

                }
            }

        } else if ("remove".equals(mode)){

        }
        //判断
        if (UIUtil.isNullOrEmpty(mess)) {
            //无异常
            ContextUtil.pushContext(context);
            isPush = Boolean.TRUE;
            if (deleteIds.size() > 0) {
                StringList strings = StringList.create(deleteIds);
                String[] stringArray = strings.toStringArray();
                DomainObject.deleteObjects(context, stringArray);
            }
            if (removeIds.size() > 0) {
                StringList strings = StringList.create(removeIds);
                String[] stringArray = strings.toStringArray();
                DomainRelationship.disconnect(context, stringArray);
            }
            mess = strSuccessMess;
        } else {
            mess = strFailedMess + mess + errorIds.toString();
            flag = "N";
        }
    }catch (Exception e) {
        mess = strFailedMess;
        e.printStackTrace();
    } finally {
        if (isPush) {
            ContextUtil.popContext(context);
        }
    }

%>
<html>
<script>
    alert("<%=mess%>");
    if ("Y" === "<%=flag%>") {
        var refreshURL = window.parent.location.href;
        window.parent.location.href = refreshURL;
    }
</script>
</html>
