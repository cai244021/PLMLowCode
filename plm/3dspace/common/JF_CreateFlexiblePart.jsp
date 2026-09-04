<%@ page import="java.util.HashMap" %>
<%@ page import="matrix.util.StringList" %>
<%@ page import="com.matrixone.apps.domain.DomainConstants" %>
<%@ page import="matrix.db.JPO" %>
<%@ page import="java.util.Map" %>
<%@ page import="com.google.gson.Gson" %>
<%@ page import="org.slf4j.LoggerFactory" %>
<%@ page import="org.slf4j.Logger" %>
<%@ page import="com.matrixone.apps.domain.util.EnoviaResourceBundle" %>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="utf-8" %>
<%@ include file="../programcentral/emxProgramGlobals2.inc" %>
<%@include file = "../emxUICommonAppInclude.inc"%>
<%@include file = "../common/enoviaCSRFTokenValidation.inc"%>
<script src="../common/scripts/emxUICore.js" type="text/javascript"></script>
<%!
    //日志
    private static final Logger _logger =  LoggerFactory.getLogger("JF_CreateFlexiblePart");
%>
<%
    _logger.info("@@@@@@@@@@@@@@@@@@@@@@@@@");
    String suiteKey = "emxComponentsStringResource";
    String strFlushTableName = "JFVPMReferenceListTable";
    String mess = DomainConstants.EMPTY_STRING;
    Map res = new HashMap<>();
    Gson gson = new Gson();
    try {
        String attrJFTransformationPlan = "JF_VPMReference.JF_TransformationPlan";
        String attrCode = "code";
        String objectId = (String) emxGetParameter(request, "objectId");
        String deformablePart = (String) emxGetParameter(request, attrJFTransformationPlan);
        String deformablePartCode = (String) emxGetParameter(request, attrCode);
        HashMap<String, Object> paramsMap = new HashMap<>();
        paramsMap.put("objectId", objectId);
        paramsMap.put(attrJFTransformationPlan, deformablePart);
        paramsMap.put(attrCode, deformablePartCode);
        Boolean flag = (Boolean) JPO.invoke(context, "JF_DeformablePartsManagement", new String[]{}, "preCloneDeformableParts", JPO.packArgs(paramsMap), Boolean.class);
        if (flag) {
            mess = EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", context.getLocale(), "emxComponents.ColorPart.Success");
        } else {
            mess = EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", context.getLocale(), "emxComponents.ColorPart.CreateFailed");
        }
        res.put("mess", mess);
    }catch (Exception e) {
        e.printStackTrace();
    }
    %>
<%=gson.toJson(res)%>