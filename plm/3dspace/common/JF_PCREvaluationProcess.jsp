<%@ page import="com.matrixone.apps.domain.DomainObject" %>
<%@ page import="com.matrixone.apps.domain.DomainRelationship" %>
<%@ page import="java.util.Enumeration" %>
<%@ page import="com.matrixone.apps.domain.util.EnoviaResourceBundle" %>
<%@ page import="com.matrixone.apps.domain.util.ContextUtil" %>
<%@ page import="org.slf4j.LoggerFactory" %>
<%@ page import="org.slf4j.Logger" %>
<%@ page import="matrix.db.JPO" %>
<%@ page import="com.matrixone.apps.framework.ui.UIUtil" %>
<%@ page import="java.util.Map" %>
<!--
ce删除ECR
-->
<%@include file="../common/emxNavigatorInclude.inc" %>
<%@include file="../common/emxNavigatorTopErrorInclude.inc" %>
<%@include file="../common/enoviaCSRFTokenValidation.inc" %>
<%!
    private static final Logger LOGGER = LoggerFactory.getLogger("JF_PCREvaluationProcess.jsp");
%>
<%
    String strObjectId = emxGetParameter(request, "objectId");
    String suiteKey = "emxComponentsStringResource";
//    String cmd = emxGetParameter(request, "cmd");
    String JFPCREvaluationConclusionCmdValue = emxGetParameter(request, "JFPCREvaluationConclusionCmd");
    String JFPCREvaluationInformCustomerCmdValue = emxGetParameter(request, "JFPCREvaluationInformCustomerCmd");
    String JFPCREvaluationMessage = emxGetParameter(request, "JFPCREvaluationMessage");
    String message = EnoviaResourceBundle.getProperty(context, suiteKey, context.getLocale(), "emxComponents.PCR.EvaluationConclusionCheck");
    DomainObject pcr = DomainObject.newInstance(context, strObjectId);
    LOGGER.info("JFPCREvaluationConclusionCmd:{} JFPCREvaluationInformCustomerCmd:{} JFPCREvaluationMessage:{}", JFPCREvaluationConclusionCmdValue, JFPCREvaluationInformCustomerCmdValue,JFPCREvaluationMessage);
    boolean flag = true;
    String strResult = "";

    //如果评估结论不可行，选择了客户通知的话，直接报错
    if("Fail".equals(JFPCREvaluationConclusionCmdValue)&&"Y".equals(JFPCREvaluationInformCustomerCmdValue)){
        strResult=message;
        flag = false;
    }else {

        //设置值
        if (UIUtil.isNotNullAndNotEmpty(JFPCREvaluationConclusionCmdValue)) {
            pcr.setAttributeValue(context, "JF_EvaluationConclusion", JFPCREvaluationConclusionCmdValue);
        }
        if (UIUtil.isNotNullAndNotEmpty(JFPCREvaluationMessage)) {
            pcr.setAttributeValue(context, "JF_EvaluateMessage", JFPCREvaluationMessage);
        }

        //设置值
        if (UIUtil.isNotNullAndNotEmpty(JFPCREvaluationInformCustomerCmdValue)) {
            pcr.setAttributeValue(context, "JF_IsEvaluationInformCustomer", JFPCREvaluationInformCustomerCmdValue);
            //不通知人，就去除责任人
            if ("N".equalsIgnoreCase(JFPCREvaluationInformCustomerCmdValue)) {
                pcr.setAttributeValue(context, "JF_PCREvaluationInformResponsible", "");
            }
        }
        String owner = context.getUser();
        JPO.invoke(context, "JF_PCR", new String[0], "agreeJF_PCRTask", new String[]{strObjectId, owner}, void.class);
    }
%>
<%@include file="../common/emxNavigatorBottomErrorInclude.inc" %>
<html>
<script>
    let flag = '<%=flag%>';
    if (flag == 'false') {
        alert("<%=strResult%>")
    } else {
        parent.location.href = parent.location.href;
    }
</script>
</html>