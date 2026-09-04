<%@page import="com.matrixone.apps.domain.DomainObject"%>
<%@ page import="static com.matrixone.apps.common.util.JSPUtil.emxGetParameterValues" %>
<%@ page import="com.matrixone.apps.domain.DomainRelationship" %>
<%@ page import="matrix.util.StringList" %>
<%@ page import="com.matrixone.apps.domain.util.FrameworkUtil" %>
<%@ page import="com.matrixone.apps.domain.DomainConstants" %>
<%@ page import="com.matrixone.apps.domain.util.MapList" %>
<%@ page import="java.util.Iterator" %>
<%@ page import="java.util.Map" %>
<%@ page import="com.matrixone.apps.domain.util.EnoviaResourceBundle" %>
<%@ page import="matrix.db.JPO" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="com.matrixone.apps.framework.ui.UIUtil" %>
<%@ page import="org.slf4j.Logger" %>
<%@ page import="org.slf4j.LoggerFactory" %>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="utf-8" %>
<script src="../common/scripts/emxUIModal.js" type="text/javascript"></script>
<script language="JavaScript" src="../common/scripts/emxUICore.js"></script>
<script language="JavaScript" src="../common/scripts/emxUIConstants.js"></script>
<script language="JavaScript" src="../common/scripts/emxUISearch.js"></script>
<script type="text/javascript" src="../common/scripts/jquery-latest.js"></script>
<%@include file="../common/emxNavigatorInclude.inc" %>
<%@include file="../common/emxNavigatorTopErrorInclude.inc" %>
<%!

    private static final Logger JF_LOGGER = LoggerFactory.getLogger("JF_FasteningPiecePromote.jsp");
%>
<%
    String strMess = DomainConstants.EMPTY_STRING;
    String strURL = DomainConstants.EMPTY_STRING;
    try {
        String[] strSelectIds = emxGetParameterValues(request, "emxTableRowId");
        String strObjectId = emxGetParameter(request, "objectId");
        String strSuiteKey = "emxComponentsStringResource";
        StringList objectIdList = new StringList();
        StringList errorTypeList = new StringList();
        StringList errorCurrentList = new StringList();
        StringList errorOwnerList = new StringList();
        StringList hasApplicationList = new StringList();
        StringList inWorkCurrentList = new StringList();
        //add by ljr 20250613
//        StringList hasJFSPRPList = new StringList();
        //end
        Boolean flag = Boolean.TRUE;
        String strContextUser = context.getUser();
        for (String strId : strSelectIds) {
            StringList strIdList = FrameworkUtil.split(strId, "|");
            String objectId = strIdList.get(0);
            objectIdList.add(objectId);
        }
        String[] strOIds = objectIdList.toStringArray();
        StringList selectList = new StringList();
        selectList.add(DomainConstants.SELECT_ID);
        selectList.add(DomainConstants.SELECT_NAME);
        selectList.add(DomainConstants.SELECT_TYPE);
        selectList.add(DomainConstants.SELECT_REVISION);
        selectList.add(DomainConstants.SELECT_CURRENT);
        selectList.add(DomainConstants.SELECT_OWNER);
        selectList.add("attribute[JF_VPMReference.JF_PartType]");
        selectList.add("attribute[JF_VPMReference.JF_PartSubType]");
        selectList.add("attribute[JF_VPMReference.JF_Detail_CN]");
        selectList.add("attribute[EnterpriseExtension.V_PartNumber]");
        selectList.add("to[JFSPartsApplication2VPMReference]");
        MapList mapList = DomainObject.getInfo(context, strOIds, selectList);
        Iterator iterator = mapList.iterator();
        //开始校验
        while (iterator.hasNext()) {
            Map map = (Map) iterator.next();
            String type = (String) map.get(DomainConstants.SELECT_TYPE);
            String oid = (String) map.get(DomainConstants.SELECT_ID);
            String name = (String) map.get(DomainConstants.SELECT_NAME);
            String revision = UIUtil.getValue(map, DomainConstants.SELECT_REVISION);
            String current= (String) map.get(DomainConstants.SELECT_CURRENT);
            String owner = UIUtil.getValue(map, DomainConstants.SELECT_OWNER);
            String partNumber = UIUtil.getValue(map, "attribute[EnterpriseExtension.V_PartNumber]");
            String hasApplication = UIUtil.getValue(map, "to[JFSPartsApplication2VPMReference]");
            String partDisplay = UIUtil.isNotNullAndNotEmpty(partNumber) ? partNumber + "_" + revision : name;
            if ("IN_WORK".equalsIgnoreCase(current)) {
                inWorkCurrentList.add(oid);
            }
            if (!"VPMReference".equalsIgnoreCase(type)) {
                errorTypeList.add(partDisplay);
                if (flag) {
                    flag = Boolean.FALSE;
                }
                continue;
            }
            if (!"IN_WORK".equalsIgnoreCase(current) && !"FROZEN".equalsIgnoreCase(current)) {
                errorCurrentList.add(partDisplay);
                if (flag) {
                    flag = Boolean.FALSE;
                }
            }
            if (!strContextUser.equalsIgnoreCase(owner)) {
                errorOwnerList.add(partDisplay);
                if (flag) {
                    flag = Boolean.FALSE;
                }
            }
            if ("TRUE".equalsIgnoreCase(hasApplication)) {
                hasApplicationList.add(partDisplay);
                if (flag) {
                    flag = Boolean.FALSE;
                }
            }
        }
        if (!flag) {
            //有报错信息
            if (errorTypeList.size() > 0) {
                strMess = EnoviaResourceBundle.getProperty(context, strSuiteKey, context.getLocale(), "emxComponents.FasteningPiecePromote.TypeError");
                strMess += errorTypeList.join(",");
            }
            if (errorCurrentList.size() > 0) {
                if (strMess.length() != 0) {
                    strMess += ";";
                }
                strMess += EnoviaResourceBundle.getProperty(context, strSuiteKey, context.getLocale(), "emxComponents.FasteningPiecePromote.CurrentError");
                strMess += errorCurrentList.join(",");
            }
            if (errorOwnerList.size() > 0) {
                if (strMess.length() != 0) {
                    strMess += ";";
                }
                strMess += EnoviaResourceBundle.getProperty(context, strSuiteKey, context.getLocale(), "emxComponents.JFSPartsApplication.CreateOwnerError");
                strMess += errorOwnerList.join(",");
            }
            if (hasApplicationList.size() > 0) {
                if (strMess.length() != 0) {
                    strMess += ";";
                }
                strMess += EnoviaResourceBundle.getProperty(context, strSuiteKey, context.getLocale(), "emxComponents.JFSPartsApplication.CreateHasApplicationError");
                strMess += hasApplicationList.join(",");
            }
        } else {
            //add by ljr 20250613 打开标准件发布流程 form 填写流程信息  并发起  紧固件
            String header = EnoviaResourceBundle.getProperty(context, strSuiteKey, context.getLocale(), "emxComponents.JFSPartsApplication.CreateNew");
            strURL = "../common/emxCreate.jsp?nameField=autoName&autoNameChecked=true&type=type_JFSPartsApplication&policy=policy_JFSPartsApplication" +
                    "&form=JFSPartsApplicationCreateForm&submitAction=treeContent&postProcessJPO=JF_FasteningPiece:createSPartsApplicationPost&suiteKey=Components&StringResourceFileId=emxComponentsStringResource&SuiteDirectory=components&header=emxComponents.JFSPartsApplication.CreateNew";
            strURL += "&objectIdList=" + objectIdList.join(",");
            //end
        }
        JF_LOGGER.info("strMess:{}", strMess);
    }catch (Exception e) {
        e.printStackTrace();
        throw e;
    }

%>
<html>
<body>
<script language="JavaScript" src="../common/scripts/emxUICore.js" type="text/javascript"></script>
<script language="JavaScript" src="../common/scripts/emxUIConstants.js" type="text/javascript"></script>
<script language="Javascript">
    var mess = "<%=strMess%>";
    if (mess != "" && mess !== null) {
        alert(mess);
    } else {
        // alert("!!!!!!!!!");
        <%--window.open("<%=strURL%>");--%>
        window.open("<%=strURL%>", '', 'width=700,height=600,left=300,top=350,scrollbars=no,location=no,fullscreen=no', 'false');
    }
    findFrame(getTopWindow(),"detailsDisplay").emxEditableTable.refreshSelectedRows();
</script>
</body>
</html>
