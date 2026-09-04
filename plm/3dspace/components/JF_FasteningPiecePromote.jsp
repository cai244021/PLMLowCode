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
//    String strURL = DomainConstants.EMPTY_STRING;
    try {
        String[] strSelectIds = emxGetParameterValues(request, "emxTableRowId");
        String strObjectId = emxGetParameter(request, "objectId");
        String strSuiteKey = "emxComponentsStringResource";
        StringList objectIdList = new StringList();
        StringList errorTypeList = new StringList();
        StringList errorPartTypeList = new StringList();
        StringList errorCurrentList = new StringList();
        StringList inWorkCurrentList = new StringList();
        //add by ljr 20250613
//        StringList hasJFSPRPList = new StringList();
        //end
        Boolean flag = Boolean.TRUE;
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
        selectList.add(DomainConstants.SELECT_CURRENT);
        selectList.add("attribute[JF_VPMReference.JF_PartType]");
        selectList.add("attribute[JF_VPMReference.JF_PartSubType]");
        selectList.add("attribute[JF_VPMReference.JF_Detail_CN]");
        selectList.add("attribute[EnterpriseExtension.V_PartNumber]");
        MapList mapList = DomainObject.getInfo(context, strOIds, selectList);
        Iterator iterator = mapList.iterator();
        //开始校验
//        DomainObject domainObject = DomainObject.newInstance(context);
        while (iterator.hasNext()) {
            Map map = (Map) iterator.next();
            String type = (String) map.get(DomainConstants.SELECT_TYPE);
            String oid = (String) map.get(DomainConstants.SELECT_ID);
            String name = (String) map.get(DomainConstants.SELECT_NAME);
            String current= (String) map.get(DomainConstants.SELECT_CURRENT);
            String value = UIUtil.getValue(map, "attribute[JF_VPMReference.JF_PartType].value");
            String subValue = UIUtil.getValue(map, "attribute[JF_VPMReference.JF_PartSubType].value");
            if ("IN_WORK".equalsIgnoreCase(current)) {
                inWorkCurrentList.add(oid);
            }
            if (!"VPMReference".equalsIgnoreCase(type)) {
                errorTypeList.add(name);
                if (flag) {
                    flag = Boolean.FALSE;
                }
                continue;
            }

            //不是电器件和紧固件的时候，不允许发布|
//            if (((!"F".equalsIgnoreCase(value)) && ("E".equalsIgnoreCase(value) && !"E02".equalsIgnoreCase(subValue))
//                || UIUtil.isNullOrEmpty(value))) {
//                errorPartTypeList.add(name);
//                if (flag) {
//                    flag = Boolean.FALSE;
//                }
//            }

            if (!"IN_WORK".equalsIgnoreCase(current) && !"FROZEN".equalsIgnoreCase(current)) {
                errorCurrentList.add(name);
                if (flag) {
                    flag = Boolean.FALSE;
                }
            }
            //add by ljr 20250613
//            domainObject.setId(oid);
            //当前零件没有在其他标准件发布流程中
//            String hasJFSPRP = domainObject.getInfo(context, "to[JFSPRP2VPM]");
//            if ("TRUE".equals(hasJFSPRP)) {
//                hasJFSPRPList.add(name);
//                if (flag) {
//                    flag = Boolean.FALSE;
//                }
//            }
            //end
        }
        String strAttrCheckMess = DomainConstants.EMPTY_STRING;
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("mapList", mapList);
        paramMap.put("objectId", strObjectId);
        if (flag) {
            //当以上得校验通过才会去做属性校验
            //校验必填属性
//            String strResultMess = JPO.invoke(context, "JF_FasteningPiece", new String[]{}, "verifyPartAttributesBased", JPO.packArgs(paramMap), String.class);
            String strResultMess = JPO.invoke(context, "JF_FasteningPiece", new String[]{}, "partsReleaseVerification", JPO.packArgs(paramMap), String.class);
            JF_LOGGER.info("strResultMess:{}", strResultMess);
            if (!"success".equalsIgnoreCase(strResultMess)) {
                strAttrCheckMess = strResultMess;
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
            if (errorPartTypeList.size() > 0) {
                strMess = EnoviaResourceBundle.getProperty(context, strSuiteKey, context.getLocale(), "emxComponents.FasteningPiecePromote.PartTypeError");
                strMess += errorPartTypeList.join(",");
            }
            if (errorCurrentList.size() > 0) {
                if (strMess.length() != 0) {
                    strMess += ";";
                }
                strMess += EnoviaResourceBundle.getProperty(context, strSuiteKey, context.getLocale(), "emxComponents.FasteningPiecePromote.CurrentError");
                strMess += errorCurrentList.join(",");
            }
            if (!strAttrCheckMess.isEmpty()) {
                if (strMess.length() != 0) {
                    strMess += ";";
                }
//                strMess += EnoviaResourceBundle.getProperty(context, strSuiteKey, context.getLocale(), "emxComponents.FasteningPiecePromote.AttrCheckError");
                strMess += strAttrCheckMess;
            }
            //add by ljr 20250613
//            if (hasJFSPRPList.size() > 0) {
//                if (strMess.length() != 0) {
//                    strMess += ";";
//                }
//                strMess += EnoviaResourceBundle.getProperty(context, strSuiteKey, context.getLocale(), "emxComponents.FasteningPiecePromote.hasJFSPRPError");
//                strMess += hasJFSPRPList.join(",");
//            }
            //end
        } else {
            //add by ljr 20250613 打开标准件发布流程 form 填写流程信息  并发起  紧固件
//            strURL = "../common/emxCreate.jsp?nameField=both&autoNameChecked=true&type=type_JFSPRP&policy=policy_JFSPRP&form=JFSPRPCreateForm&submitAction=treeContent&postProcessJPO=JF_FasteningPiece:createJFSPRPPost&header=emxFramework.JFSPRP.CreateNew";
//            strURL += "inWorkCurrentList=" + inWorkCurrentList.join(",") + ";";
//            strURL += "objectIdList=" + objectIdList.join(",");
            //end
            //校验通过 开始调用方法
            paramMap.put("inWorkCurrentList", inWorkCurrentList);
            paramMap.put("objectIdList", objectIdList);
            Boolean flagSuccess = JPO.invoke(context, "JF_FasteningPiece", new String[]{}, "FasteningPiecePromote", JPO.packArgs(paramMap), Boolean.class);
            if (flagSuccess) {
                strMess = EnoviaResourceBundle.getProperty(context, strSuiteKey, context.getLocale(), "emxComponents.FasteningPiecePromote.Successful");
            } else {
                strMess = EnoviaResourceBundle.getProperty(context, strSuiteKey, context.getLocale(), "emxComponents.FasteningPiecePromote.failed");
            }
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
    console.log("<%=strMess%>");
    var mess = "<%=strMess%>";
    //window.open(strURL);
    alert(mess);
    findFrame(getTopWindow(),"detailsDisplay").emxEditableTable.refreshSelectedRows();
</script>
</body>
</html>
