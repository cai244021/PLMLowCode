<%--<!DOCTYPE html>--%>
<%--<html>--%>
<%@ page import="org.slf4j.Logger" %>
<%@ page import="org.slf4j.LoggerFactory" %>
<%@ page import="matrix.util.StringList" %>
<%@ page import="java.util.StringTokenizer" %>
<%@ page import="com.matrixone.apps.domain.util.XSSUtil" %>
<%@ page import="matrix.db.JPO" %>
<%@ page import="java.util.Map" %>
<%@ page import="com.matrixone.apps.domain.util.EnoviaResourceBundle" %>
<%@ page import="com.matrixone.apps.domain.DomainRelationship" %>
<%@ page import="com.matrixone.apps.domain.DomainObject" %>
<%@ page import="com.matrixone.apps.program.ProgramCentralUtil" %>
<%@ page import="static com.matrixone.apps.common.util.JSPUtil.emxGetParameterValues" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="matrix.db.Attribute" %>
<%--登录检查--%>
<%@include file = "emxNavigatorInclude.inc"%>
<%!
    //日志
    private static final Logger _logger =  LoggerFactory.getLogger("JF_CreateECRProcess");
%>
<%

    String JFAffectsFactoryValues[] = emxGetParameterValues(request, "JFAffectsFactory");
    String strObjectId = emxGetParameter(request, "newObjectId");
    _logger.info("strObjectId:{}",strObjectId);
    DomainObject ecr = DomainObject.newInstance(context,strObjectId);
    Attribute attributeValues = ecr.getAttributeValues(context, "JFAffectedFactory");
    if (null != JFAffectsFactoryValues && JFAffectsFactoryValues.length > 0){
        StringList newValueList = StringList.create(JFAffectsFactoryValues);
        _logger.info("strJFAffectsFactory:{}",JFAffectsFactoryValues);
        attributeValues.setValueList(newValueList);
    }

%>
