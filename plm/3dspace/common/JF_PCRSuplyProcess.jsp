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
<%@ page import="matrix.util.StringList" %>
<%@ page import="com.matrixone.apps.domain.util.MqlUtil" %>
<!--
ce删除ECR
-->
<%@include file = "../common/emxNavigatorInclude.inc"%>
<%@include file = "../common/emxNavigatorTopErrorInclude.inc"%>
<%@include file = "../common/enoviaCSRFTokenValidation.inc"%>
<%!
    private static final Logger LOGGER = LoggerFactory.getLogger("JF_PCRSuplyProcess.jsp");
%>
<%
    String strObjectId = emxGetParameter(request, "objectId");
    String cmd = emxGetParameter(request, "cmd");
    String supplyValue = emxGetParameter(request, "JFPCRNeedValidationCmd");
    String conclusionSupplyValue = emxGetParameter(request, "JFPCRValidateConclusionCmd");
    String JFPCRBreakpointSwitchingDateCmd = emxGetParameter(request, "JFPCRBreakpointSwitchingDateCmd");
    String JFPCRSwitchingMethodCmd = emxGetParameter(request, "JFPCRSwitchingMethodCmd");
    DomainObject pcr = DomainObject.newInstance(context,strObjectId);
    LOGGER.info("supplyValue:{} conclusionSupplyValue:{} JFPCRBreakpointSwitchingDateCmd :{}",supplyValue,conclusionSupplyValue,JFPCRBreakpointSwitchingDateCmd);
    boolean flag = true;
    String strResult = "";
    String message= (String) EnoviaResourceBundle.getFrameworkStringResourceProperty(context, "emxFramework.PCR.JFSupplySubmitMessage", context.getLocale());
    String message1= (String) EnoviaResourceBundle.getFrameworkStringResourceProperty(context, "emxFramework.PCR.ExistTask", context.getLocale());
    String message2= (String) EnoviaResourceBundle.getFrameworkStringResourceProperty(context, "emxFramework.PCR.ValidateTask3", context.getLocale());
    String message3= (String) EnoviaResourceBundle.getFrameworkStringResourceProperty(context, "emxFramework.PCR.ValidateTask4", context.getLocale());
    String message4= (String) EnoviaResourceBundle.getFrameworkStringResourceProperty(context, "emxFramework.PCR.NoticeSubmit", context.getLocale());
    if("JFPCRSupplySubmitCmd".equalsIgnoreCase(cmd)){
        //选择了一次之后就不让修改了
        String strNeed = pcr.getAttributeValue(context,"JF_NeedValidation");
//        if(UIUtil.isNullOrEmpty(strNeed)) {
            if ("Y".equalsIgnoreCase(supplyValue)) {
                    JPO.invoke(context, "JF_PCR", null, "createPCRVerificationTask", new String[]{strObjectId});

            } else if ("N".equalsIgnoreCase(supplyValue)) {
                //直接生成Route
                //如果存在验证关系，需要清除掉
                DomainObject pcrObj = DomainObject.newInstance(context,strObjectId);
                StringList verificaList = pcrObj.getInfoList(context,"from[JF_PCR2VerificationTask].to.id");
                try {
                    ContextUtil.pushContext(context);
                    if(verificaList.size()>0) {
                        DomainObject.deleteObjects(context, verificaList.toStringArray());
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }finally {
                    ContextUtil.popContext(context);
                }
                JPO.invoke(context, "JF_PCR", null, "createRouteInReview", new String[]{strObjectId});
            } else {
                flag = false;
                strResult = message;
            }
            //设置值
            if(UIUtil.isNotNullAndNotEmpty(supplyValue)) {
                pcr.setAttributeValue(context, "JF_NeedValidation", supplyValue);
            }
      /*  }else{
            flag = false;
            strResult = message1;
        }*/
    } else if("JFPCRValidateConclusionSubmitCmd".equalsIgnoreCase(cmd)){
        if(UIUtil.isNotNullAndNotEmpty(conclusionSupplyValue)) {
            Map map = (Map) JPO.invoke(context, "JF_PCR", null, "checkJFPCRValidateConclusionSubmitCmd", new String[]{strObjectId}, Map.class);
            flag = (boolean) map.get("flag");
            strResult = UIUtil.getValue(map, "message");
            if (flag) {
                //直接生成Route
                //提升生命验证任务的生命周期


                JPO.invoke(context, "JF_PCR", null, "setJF_PCR2VerificationTaskReview", new String[]{strObjectId,"Review","FROZEN"});
                JPO.invoke(context, "JF_PCR", null, "createRouteInReview", new String[]{strObjectId});
            }
            if (UIUtil.isNotNullAndNotEmpty(conclusionSupplyValue)) {
                pcr.setAttributeValue(context, "JF_validateConclusion", conclusionSupplyValue);
            }
        }else{
            flag = false;
            strResult = message2;
        }
    } else if("JFPCRBreakpointSwitchingDateSubmitCmd".equalsIgnoreCase(cmd)){
        if(UIUtil.isNotNullAndNotEmpty(JFPCRBreakpointSwitchingDateCmd)) {
            LOGGER.info("JFPCRBreakpointSwitchingDateCmd===={}",JFPCRBreakpointSwitchingDateCmd);
                //完成任务
            if (UIUtil.isNotNullAndNotEmpty(JFPCRBreakpointSwitchingDateCmd)) {
                LOGGER.info("JFPCRBreakpointSwitchingDateCmd====2222{}",JFPCRBreakpointSwitchingDateCmd);
//                pcr.setAttributeValue(context, "JF_BreakpointSwitchingDate", JFPCRBreakpointSwitchingDateCmd);
                JPO.invoke(context, "JF_PCR", null, "setDateAttributeValue", new String[]{strObjectId,JFPCRBreakpointSwitchingDateCmd,JFPCRSwitchingMethodCmd});
                String owner = context.getUser();
                JPO.invoke(context, "JF_PCR", new String[0], "reviewInbox", new String[]{strObjectId, owner,"Approve"}, void.class);
            }
        }else{
            flag = false;
            strResult = message3;
        }
    }
    //emxFramework.Label.JFSupplySubmitMessage


%>
<%@include file = "../common/emxNavigatorBottomErrorInclude.inc"%>
<html>
<script >
    let flag = '<%=flag%>';
    if(flag=='false'){
    alert("<%=strResult%>")
    }else {
        alert("<%=message4%>")
        parent.location.href = parent.location.href;
    }
</script>
</html>