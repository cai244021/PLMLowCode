<%--  JF_ProjectMBOMAddMakeVPMProcess.jsp -
   Copyright (c) 1992-2020 Dassault Systemes.
   All Rights Reserved.
   This program contains proprietary and trade secret information of MatrixOne,
   Inc.  Copyright notice is precautionary only
   and does not evidence any actual or intended publication of such program

   static const char RCSID[] = "$Id: emxCommonDocumentCheckinProcess.jsp.rca 1.22 Wed Oct 22 16:18:50 2008 przemek Experimental przemek $"
--%>
<%@ page import="matrix.db.JPO" %>
<%@ page import="org.apache.commons.fileupload.DiskFileUpload" %>
<%@ page import="org.slf4j.Logger" %>
<%@ page import="org.slf4j.LoggerFactory" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="com.google.gson.Gson" %>
<%@ page import="com.matrixone.apps.domain.DomainObject" %>
<%@ page import="com.matrixone.apps.common.util.ComponentsUIUtil" %>
<%@ page import="matrix.util.StringList" %>
<%@ page import="com.matrixone.apps.domain.DomainConstants" %>
<%@include file = "../emxUICommonAppInclude.inc"%>
<%@ include file = "../emxUICommonHeaderBeginInclude.inc" %>
<%@include file = "../common/emxUIConstantsInclude.inc"%>
<%--<%@include file = "../common/emxNavigatorBottomErrorInclude.inc"%>--%>
<%@ include file = "../emxJSValidation.inc"%>
<%@include file = "../components/emxComponentsUtil.inc"%>
<%@include file = "../components/emxComponentsSetCompanyKeyInRPE.inc"%>
<jsp:useBean id="formBean" scope="page" class="com.matrixone.apps.common.util.FormBean" />
<jsp:useBean id="requestBean" scope="page" class="com.matrixone.apps.domain.util.Request"/>
<script language="JavaScript" src="../common/scripts/emxUIConstants.js" type="text/javascript"></script>
<script language="JavaScript" src="../common/scripts/emxUICore.js" type="text/javascript"></script>
<%!
        private static final Logger _logger = LoggerFactory.getLogger("JF_ProjectMBOMAddMakeVPMProcess.jsp");
%>
<%
        StringList errorList = new StringList();
        String errorMessage="";
        String rowId = emxGetParameter(request, "rowId");
        StringBuilder sBuff = new StringBuilder();
        String actionType = DomainConstants.EMPTY_STRING;
        String strObjectId=DomainConstants.EMPTY_STRING;
        String connParts =DomainConstants.EMPTY_STRING;
        try {
                strObjectId = emxGetParameter(request, "objectId");
                connParts = emxGetParameter(request, "connParts");
                _logger.info("--------------------FileUpload begin -----------------------------");
                Gson gson = new Gson();
                formBean.processForm(session, request);
                String JF_AddMakeVPM = (String) formBean.getElementValue("JF_AddMakeVPMNameOID");
                String JF_Dosage = (String) formBean.getElementValue("JF_Dosage");
                actionType = (String) formBean.getElementValue("actionType");

                _logger.info("JF_AddMakeVPM:{}", JF_AddMakeVPM);
                _logger.info("JF_Dosage:{}", JF_Dosage);
                _logger.info("strObjectId:{}", strObjectId);
                _logger.info("connParts:{}", connParts);
                StringList vpmidlist = new StringList();
                String[] tableRowIdList = JF_AddMakeVPM.split("\\|");
                for (String tableRowId : tableRowIdList) {
                        vpmidlist.add(tableRowId);
                }
                Boolean res = Boolean.FALSE;
                Map packMap = new HashMap();
                packMap.put("objectid", strObjectId);
                packMap.put("connParts",connParts);
                packMap.put("vpmidList",vpmidlist);
                packMap.put("JF_Dosage",JF_Dosage);
                Map resultMap = JPO.invoke(context, "JF_MBOM", null, "CreateMbomPartAndConnect", JPO.packArgs(packMap), Map.class);
                errorMessage = (String) resultMap.get("msg");
                StringList idlist = (StringList) resultMap.get("idlist");
                sBuff.append("<mxRoot>");
                for(String mbomid:idlist){
                        DomainObject mobmobj=DomainObject.newInstance(context,mbomid);
                        String fromid=mobmobj.getInfo(context,"to[JF_relManufacturedItem].from.id");
                        String relid=mobmobj.getInfo(context,"to[JF_relManufacturedItem].id");
                        sBuff.append("<action><![CDATA[add]]></action>");
                        sBuff.append("<data status=\"committed\" ");
                        sBuff.append(">");
                        sBuff.append("<item oid=\"" + mbomid + "\" relId=\"" + relid + "\" pid=\"" + fromid
                                + "\"  direction=\"" + "from" + "\" />");
                        sBuff.append("</data>");
                }
                sBuff.append("</mxRoot>");
                _logger.info("sBuff:{}", sBuff);
                if( UIUtil.isNotNullAndNotEmpty(errorMessage)){
                        errorList.add(errorMessage);
                }
        }catch (Exception e) {
                e.printStackTrace();
        }
        _logger.info("--------------------FileUpload end -----------------------------");
%>
<%--<script>--%>
<%--        let flag = <%=errorList.size()==0%>;--%>
<%--        // window.close();--%>
<%--        if(flag) {--%>
<%--                var sbifrom=window.parent.parent;--%>
<%--                var  topFrame=sbifrom.openerFindFrame(sbifrom,"detailsDisplay");--%>
<%--                topFrame.rebuildViewInProcess = true;--%>
<%--                topFrame.syncSBInProcess = true;--%>
<%--                topFrame.emxEditableTable.addToSelected('<%=sBuff.toString()%>');--%>
<%--                topFrame.rebuildViewInProcess = false;--%>
<%--                topFrame.syncSBInProcess = false;--%>
<%--                topFrame.emxEditableTable.refreshStructureWithOutSort();--%>
<%--                console.log("end");--%>
<%--        }else{--%>
<%--                alert('<%=errorMessage%>');--%>
<%--        }--%>
<%--</script>--%>



<script>
        let flag = <%=errorList.size()==0%>;
        // window.close();
        // 获取 opener（即打开此弹窗的主页面）
        var opener = window.opener;
        if(flag) {
                // 成功：通知主页面刷新表格
                var sbifrom=window.parent.parent;
                var  topFrame=sbifrom.openerFindFrame(sbifrom,"detailsDisplay");
                topFrame.rebuildViewInProcess = true;
                topFrame.syncSBInProcess = true;
                topFrame.emxEditableTable.addToSelected('<%=sBuff.toString()%>');
                topFrame.rebuildViewInProcess = false;
                topFrame.syncSBInProcess = false;
                topFrame.emxEditableTable.refreshStructureWithOutSort();
                // 根据 actionType 决定是否关闭窗口
                if ("<%= actionType %>" === "close") {
                        // 关闭当前窗口
                        window.close();
                } else if ("<%= actionType %>" === "continue") {
                        // 重新加载当前页面（即回到填写界面）
                        window.location.href = "./JF_ProjectMBOMAddMakeVPMDialogFS.jsp?objectId=<%= strObjectId %>&selectParts=<%= connParts %>";
                }
        }else{
                alert('<%=errorMessage%>');
        }
</script>