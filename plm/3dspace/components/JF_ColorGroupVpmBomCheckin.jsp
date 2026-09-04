<%@page import="com.matrixone.apps.domain.DomainObject"%>
<%@ page import="com.matrixone.apps.domain.DomainRelationship" %>
<%@ page import="matrix.util.StringList" %>
<%@ page import="matrix.db.JPO" %>
<%@ page import="java.util.Map" %>
<%@ page import="com.matrixone.apps.domain.DomainConstants" %>
<%@ page import="com.matrixone.apps.common.util.ComponentsUIUtil" %>
<%@ page import="com.matrixone.apps.framework.ui.UIUtil" %>
<%@ page import="static com.matrixone.apps.domain.DomainConstants.SELECT_ORIGINATED" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="org.slf4j.LoggerFactory" %>
<%@ page import="org.slf4j.Logger" %>
<%@ page import="java.util.Arrays" %>
<%@ page import="static com.matrixone.apps.common.util.JSPUtil.emxGetParameterValues" %>
<%@ page import="static com.matrixone.apps.domain.DomainConstants.context1" %>
<%@ page import="com.matrixone.apps.domain.util.*" %>
<%@ page import="com.matrixone.apps.program.ProgramCentralUtil" %>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="utf-8" %>
<script src="../common/scripts/emxUIModal.js" type="text/javascript"></script>
<script language="JavaScript" src="../common/scripts/emxUICore.js"></script>
<script language="JavaScript" src="../common/scripts/emxUIConstants.js"></script>
<script language="JavaScript" src="../common/scripts/emxUISearch.js"></script>
<%@include file="../emxUIFramesetUtil.inc"%>
<%!
    private static final Logger JF_LOGGER = LoggerFactory.getLogger("JF_ColorGroupVpmBomCheckin.jsp");
%>
<%
    String strMess = DomainConstants.EMPTY_STRING;
    String objectId = emxGetParameter(request, "objectId");
    String[] tableRowIds = emxGetParameterValues(request, "emxTableRowId");
    StringList tableRowIdList = StringList.create(tableRowIds);
    DomainObject objectVpm = DomainObject.newInstance(context, objectId);
//    String strPsId = objectVpm.getInfo(context, "to[JFProject2RootPart].from.id");
    String strPsId  =  JPO.invoke(context,"JF_VPMReferenceEBOM",null,"getPartBelongProject", new String[]{objectId}, String.class);
    String header = ComponentsUIUtil.getI18NString(context, context.getLocale().toString(), "emxComponents.Header.Message", new String[]{});
    StringBuffer sbUrl = new StringBuffer();
    StringList rowIdList = new StringList();
    StringList rowNameList = new StringList();
    if (UIUtil.isNullOrEmpty(strPsId)) {
        strMess = ComponentsUIUtil.getI18NString(context, context.getLocale().toString(), "emxComponents.Message.VpmNotConnectPs", new String[]{});
    } else {
        MapList partMapList = new MapList();
        StringList errorList = new StringList();
        for(int i=0; i < tableRowIdList.size(); i++)
        {
            //判断 前零件和整椅项目的关系属性JF_ColorGroupNameEdit 为freeze，表示不可以编辑
            Map rowMap = ProgramCentralUtil.parseTableRowId(context,tableRowIdList.get(i));
            String partId = (String) rowMap.get("objectId");
            Map paramsMap = new HashMap<>();
            paramsMap.put("relName", "JFProject2ColorGroup");
            paramsMap.put("fromId", strPsId);
            paramsMap.put("toId", partId);
            partMapList.add(rowMap);
            String connId = (String) JPO.invoke(context,"JF_PublicMethodClass",null,"getTwoBusinessObjectConnId", JPO.packArgs(paramsMap), String.class);
            if (UIUtil.isNotNullAndNotEmpty(connId)) {
                DomainRelationship domainRelationship = DomainRelationship.newInstance(context, connId);
                String strColorGroupNameEdit = domainRelationship.getAttributeValue(context, "JF_ColorGroupNameEdit");
                if ("freeze".equalsIgnoreCase(strColorGroupNameEdit)) {
                    //不能修改
                    DomainObject domainObject = DomainObject.newInstance(context, partId);
                    String vpmVName = domainObject.getAttributeValue(context, "EnterpriseExtension.V_PartNumber");
                    errorList.add(UIUtil.isNotNullAndNotEmpty(vpmVName) ? vpmVName : domainObject.getInfo(context, DomainConstants.SELECT_NAME));
                }
            }
        }
        if (!errorList.isEmpty()) {
            //如果又不能修改的提示
            strMess =  ComponentsUIUtil.getI18NString(context, context.getLocale().toString(), "emxComponents.Message.NotEditColorGroup", new String[]{});
            strMess += errorList.join(",");
        } else {
            //获取最新版发布的颜色矩阵
            HashMap params = new HashMap();
            params.put("objectId", strPsId);
            params.put("busWhere", "current==Release");
            MapList mapList = (MapList) JPO.invoke(context, "JF_ColorPart", JPO.packArgs (params), "getProjectSpaceNewColorMatrix", JPO.packArgs (params), MapList.class);
            if (!mapList.isEmpty()) {
                Map map = (Map) mapList.get(0);
                header += "   " + UIUtil.getValue(map, DomainConstants.SELECT_REVISION);
            } else {
                strMess =  ComponentsUIUtil.getI18NString(context, context.getLocale().toString(), "emxComponents.Message.NotHasColorMatrix", new String[]{});
            }
            if (UIUtil.isNullOrEmpty(strMess)) {
                StringList partList = new StringList();
                for (int i = 0; i < partMapList.size(); i++) {
                    Map rowMap = (Map) partMapList.get(i);
                    String partId = (String) rowMap.get("objectId");
                    String rowId = (String) rowMap.get("rowId");
                    //rowId
                    rowIdList.add(rowId);
                    if (UIUtil.isNotNullAndNotEmpty(partId)) {
                        //rowName
                        DomainObject domainObject = DomainObject.newInstance(context, partId);
                        String attributeValue = domainObject.getAttributeValue(context, "EnterpriseExtension.V_PartNumber");
                        if (UIUtil.isNullOrEmpty(attributeValue)) {
                            attributeValue = domainObject.getInfo(context, DomainConstants.SELECT_NAME);
                        }
                        rowNameList.add(attributeValue);
                        partList.add(partId);
                    }
                }
                sbUrl = new StringBuffer("../common/emxTable.jsp?table=JFColorInfoTable&program=JF_ColorPart:getVPMConnColorMatrixColorGroup");
                sbUrl.append("&strPsId=");
                sbUrl.append(strPsId);
                sbUrl.append("&selection=single&sortColumnName=Name&sortDirection=ascending");
                sbUrl.append("&showApply=true&header=");
                sbUrl.append(header);
                sbUrl.append("&SubmitLabel=emxFramework.Common.ok&CancelLabel=emxFramework.Button.Cancel&CancelButton=true");
                sbUrl.append("&submitAction=refreshCaller&SubmitURL=../components/JF_ColorGroupVpmBomSubmit.jsp?");
                JF_LOGGER.info("tableRowIdList:{}", tableRowIdList.toString());
                if (partList.size() > 0) {
                    String str = strPsId + ";" + objectId + ";" + partList.join(",");
                    sbUrl.append("ids=");
                    sbUrl.append(str);
                }
            }
        }
    }
    String rowIds = rowIdList.join("|");
    String rowNames = rowNameList.join(",");
    JF_LOGGER.info("rowIds:{}", rowIds);
    JF_LOGGER.info("rowNames:{}", rowNames);
%>
<html>
<script>
    const mess = "<%=strMess%>";
    if (mess.length > 0) {
        alert(mess);
    } else {
        const rowIdList = "<%=rowIds%>";
        const rowNameList = "<%=rowNames%>";
        let rowCodeList = "";
        let rowParentNameList = "";
        for (const x of rowIdList.split("|")) {
            if ("0" === x) {
                continue;
            } else {
                let rowId = parent.emxEditableTable.getParentRowId(x);
                let code = parent.emxEditableTable.getCellValueByRowId(rowId, "ColorGroup").value.current.actual;
                let name = parent.emxEditableTable.getCellValueByRowId(rowId, "Name").value.current.actual;
                console.log("code:", code);
                rowCodeList = rowCodeList + "," + code;
                rowParentNameList = rowParentNameList + "," + name;
            }
        };
        let sbUrl = "<%=sbUrl%>";
        sbUrl = sbUrl + ";" + rowNameList + ";" +  rowCodeList + ";" + rowParentNameList;
        console.log("sbUrl:", sbUrl);
        window.open(sbUrl, '', 'width=700,height=600,left=600,top=350,scrollbars=no,location=no,fullscreen=no', 'false');
    }
    // var refreshURL = window.parent.location.href;
    // window.parent.location.href = refreshURL;
</script>
</html>
