<%@ page import="java.util.Map" %>
<%@ page import="com.matrixone.apps.domain.DomainObject" %>
<%@ page import="matrix.db.RelationshipType" %>
<%@ page import="com.matrixone.apps.domain.util.FrameworkUtil" %>
<%@ page import="matrix.util.StringList" %>
<%@ page import="com.matrixone.apps.domain.DomainRelationship" %>
<%@ page import="com.matrixone.apps.domain.DomainConstants" %>
<%@ page import="org.slf4j.Logger" %>
<%@ page import="org.slf4j.LoggerFactory" %>
<%@ page import="com.matrixone.apps.domain.util.ContextUtil" %>
<%@ page import="com.matrixone.apps.framework.ui.UIUtil" %>
<%@ page import="com.matrixone.apps.common.util.ComponentsUIUtil" %>
<%@ page import="matrix.db.JPO" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="static com.matrixone.apps.common.util.JSPUtil.emxGetParameterValues" %>
<%@ page import="com.matrixone.apps.common.util.ComponentsUtil" %>
<%@ page import="com.google.gson.Gson" %>
<!--
    DR添加物理产品
-->
<%@include file = "../common/emxNavigatorInclude.inc"%>
<%@include file = "../common/emxNavigatorTopErrorInclude.inc"%>
<%!
    private static final Logger JF_LOGGER = LoggerFactory.getLogger("JF_UpdateMBOMSubmit.jsp");
    private static final String ATTRIBUTE_JF_MBOM_UPDATE_ALLOW = "JF_MBOMUpdateAllow";
%>
<%
    Boolean isPush = Boolean.FALSE;
    Boolean mbomUpdateOccupied = Boolean.FALSE;
    DomainObject projectObject = null;
    String mess = DomainConstants.EMPTY_STRING;
    String flag = "Y";
    try {
        String strObjectId = emxGetParameter(request, "objectId");
        String changeDesc = emxGetParameter(request, "changeDesc");
        String selectECRId = emxGetParameter(request, "selectECRId");
        JF_LOGGER.info("strObjectId:{}", strObjectId);
        JF_LOGGER.info("changeDesc:{}", changeDesc);
        JF_LOGGER.info("selectECRId:{}", selectECRId);
        projectObject = DomainObject.newInstance(context, strObjectId);
        String strMBOMUpdateAllow = projectObject.getAttributeValue(context, ATTRIBUTE_JF_MBOM_UPDATE_ALLOW);
        if ("N".equalsIgnoreCase(strMBOMUpdateAllow)) {
            flag = "W";
            mess = EnoviaResourceBundle.getProperty(context, "emxProgramCentralStringResource", context.getLocale(), "emxProgramCentral.UpdateMBOM.InProgress");
        } else {
            //20260724 update by ljr MBOM更新开始前占用项目更新标识，避免上一次更新未结束时再次提交；
            projectObject.setAttributeValue(context, ATTRIBUTE_JF_MBOM_UPDATE_ALLOW, "N");
            mbomUpdateOccupied = Boolean.TRUE;
            //调用jpo
            HashMap<String, String> paramsMap = new HashMap<>();
            paramsMap.put("ecrIdList", selectECRId);
            paramsMap.put("changeDesc", changeDesc);
            paramsMap.put("projectId", strObjectId);
            paramsMap.put("createCR", "Y");
            Boolean res = (Boolean) JPO.invoke(context, "JF_MBOM", JPO.packArgs (paramsMap), "updateMBOMStructureSecond", JPO.packArgs (paramsMap), Boolean.class);
            //        Boolean res = (Boolean) JPO.invoke(context, "JF_MBOM", JPO.packArgs (paramsMap), "updateMBOMStructure", JPO.packArgs (paramsMap), Boolean.class);
            if (res) {
                mess = EnoviaResourceBundle.getProperty(context, "emxProgramCentralStringResource", context.getLocale(), "emxProgramCentral.UpdateMBOM.Successful");
            } else {
                flag = "N";
                mess = EnoviaResourceBundle.getProperty(context, "emxProgramCentralStringResource", context.getLocale(), "emxProgramCentral.UpdateMBOM.Failed");
            }
        }
    }catch (Exception e){
        e.printStackTrace();
        flag = "N";
        mess = EnoviaResourceBundle.getProperty(context, "emxProgramCentralStringResource", context.getLocale(), "emxProgramCentral.UpdateMBOM.Failed");
    } finally {
        if (mbomUpdateOccupied && projectObject != null) {
            try {
                //20260724 update by ljr MBOM更新结束或异常时释放项目更新标识；
                projectObject.setAttributeValue(context, ATTRIBUTE_JF_MBOM_UPDATE_ALLOW, "Y");
            } catch (Exception releaseException) {
                JF_LOGGER.error("release JF_MBOMUpdateAllow error", releaseException);
                flag = "N";
                mess = EnoviaResourceBundle.getProperty(context, "emxProgramCentralStringResource", context.getLocale(), "emxProgramCentral.UpdateMBOM.Failed");
            }
        }
    }
    Gson gson = new Gson();
    HashMap<String, String> map = new HashMap<>();
    map.put("flag", flag);
    map.put("mess", mess);
%>
<%=gson.toJson(map)%>