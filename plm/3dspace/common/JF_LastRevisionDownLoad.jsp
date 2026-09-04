<%--<!DOCTYPE html>--%>
<%--<html>--%>
<%@ page import="matrix.db.JPO" %>
<%@ page import="org.apache.poi.ss.usermodel.Workbook" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.net.URLEncoder" %>
<%@ page import="java.util.HashSet" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="matrix.util.MatrixException" %>
<%@ page import="com.matrixone.apps.program.ProgramCentralUtil" %>
<%@ page import="org.slf4j.LoggerFactory" %>
<%@ page import="org.slf4j.Logger" %>
<%@ page import="static com.matrixone.apps.common.util.JSPUtil.emxGetParameterValues" %>
<%@ page import="com.matrixone.apps.framework.ui.UIUtil" %>
<%@ page import="com.matrixone.apps.domain.DomainObject" %>
<%@ page import="matrix.util.StringList" %>
<%@ page import="static com.matrixone.apps.domain.DomainConstants.*" %>
<%@ page import="com.matrixone.apps.domain.util.EnoviaResourceBundle" %>
<%@ page import="com.google.gson.Gson" %>
<%@ page import="matrix.db.BusinessObject" %>
<%@ page import="com.matrixone.apps.domain.util.MqlUtil" %>
<%@ page import="java.io.OutputStream" %>
<%@ page import="java.io.IOException" %>
<%--登录检查--%>
<%@include file = "emxNavigatorInclude.inc"%>
<script src="./scripts/emxUIModal.js" type="text/javascript"></script>
<script language="JavaScript" src="./scripts/emxUICore.js"></script>
<script language="JavaScript" src="./scripts/emxUIConstants.js"></script>
<script language="JavaScript" src="./scripts/emxUISearch.js"></script>
<script language="JavaScript" src="../webapps\ENOAEFCore\webroot\common\scripts/bpsTagNavSBInit.js"></script>
<%!
    //日志
    private static final Logger _logger =  LoggerFactory.getLogger("JF_LastRevisionDownLoad");
%>
<%
    Map resMap = new HashMap<>();
    Gson gson = new Gson();
    OutputStream outStream = null;
    try {
        //获取请求路径
        String strPath = request.getRealPath("");
        String strMode = emxGetParameter(request, "mode");
        String strDownloadType = emxGetParameter(request, "downType");
        String strExportType = emxGetParameter(request, "exportType");
        String strObjectId = emxGetParameter(request, "objectId");
        String strSelectRowIds = emxGetParameter(request, "selectRowIds");
        _logger.info("strMode:{}",strMode);
        _logger.info("strDownloadType:{}",strDownloadType);
        _logger.info("strExportType:{}",strExportType);
        _logger.info("strObjectId:{}",strObjectId);
        _logger.info("strSelectRowIds:{}",strSelectRowIds);
        HashSet<String> idSet = new HashSet<>();
        String strMess = EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", context.getLocale(), "emxComponents.Mess.Snapshot.SelectPartOnlyAllowRootPart");
        String strReleased = EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", context.getLocale(), "emxComponents.BomMess.Released");
        String strLoading = EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", context.getLocale(), "emxComponents.Export.Loading");
        String strNoSelectedPart = EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", context.getLocale(), "emxComponents.Export.BOM.NoSelectedPart");
        String strFirstPartNoProject = EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", context.getLocale(), "emxComponents.Export.BOM.FirstPartNoProject");
        String strProjectRequired = EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", context.getLocale(), "emxComponents.Export.BOM.ProjectRequired");
        String strProjectInvalid = EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", context.getLocale(), "emxComponents.Export.BOM.ProjectInvalid");

        boolean isAlert = false ;
        StringList releaseErrorList = new StringList();
        HashSet<String> selectedSupplyPartIds = new HashSet<>();
        DomainObject bo = DomainObject.newInstance(context, strObjectId);
        boolean isSelectedPartExport = "snapshot".equals(strMode) || "dr".equals(strMode) || "ecr".equals(strMode) || "da".equals(strMode) || "partList".equals(strMode) || "ebom".equals(strMode) || "servicePartsList".equals(strMode);
        boolean isSelectedPartRevisionExport = "servicePartsList".equals(strMode);
        //20260813 update by caipan 指定导出入口使用当前DR/DA等对象编号作为下载文件名。
        boolean isExportObjectNameFile = UIUtil.isNullOrEmpty(strMode) || "snapshot".equals(strMode)
                || "dr".equals(strMode) || "ecr".equals(strMode) || "da".equals(strMode)
                || "partList".equals(strMode) || "servicePartsList".equals(strMode);
        String strExportProjectId = strObjectId;
        if ("snapshot".equals(strMode)){
            //获取到快照关联项目id
            strExportProjectId = bo.getInfo(context,"to[JFProject2Snapshot].from.id");
        } else if ("dr".equals(strMode) || "ecr".equals(strMode) || "da".equals(strMode)) {
            //获取到DR/ECR/DA关联项目id
            strExportProjectId = bo.getInfo(context,"from[JFChange2Project].to.id");
        } else if ("partList".equals(strMode)) {
            //获取到PartList关联项目id
            strExportProjectId = bo.getInfo(context,"to[JFProject2PartList].from.id");
        } else if ("servicePartsList".equals(strMode)) {
            //获取到售后件清单关联项目id
            strExportProjectId = bo.getInfo(context,"to[JFProject2ServicePartsList].from.id");
        } else if (!"ebom".equals(strMode)) {
            //产品配置表页面的objectId是产品配置表ID，导出客户零件号/DB时需要使用其所属项目
            String strProductConfigProjectId = bo.getInfo(context,
                    "to[JFProject2ProductConfigTable].from.id");
            if (UIUtil.isNotNullAndNotEmpty(strProductConfigProjectId)) {
                strExportProjectId = strProductConfigProjectId;
            }
        }
        if (isSelectedPartExport){
            String[] tableRowIdList = emxGetParameterValues(request, "emxTableRowId");
            //20260903 update by caipan EBOM导出始终以页面根节点为导出根件，忽略表格勾选行
            if ("ebom".equals(strMode)) {
                tableRowIdList = new String[]{"|" + strObjectId + "||"};
            } else if ("ajax".equals(strDownloadType)){
                String[] split = strSelectRowIds.split(",");
                for (int i = 0; i < split.length; i++) {
                    split[i] = "|"+split[i]+"||";
                }
                tableRowIdList = split;
            }
            _logger.info("tableRowIdList:{}", tableRowIdList == null ? "null" : tableRowIdList.length);
            if (tableRowIdList == null || tableRowIdList.length == 0) {
                strMess = strNoSelectedPart;
                isAlert = true;
            }
            if (!isAlert && "ebom".equals(strMode)) {
                strExportProjectId = "";
            }
            if (!isAlert) {
                for (String tableRowId : tableRowIdList) {
                    _logger.info("tableRowId:{}", tableRowId);
                    Map rowMap = ProgramCentralUtil.parseTableRowId(context,tableRowId);
                    String strPartId = (String) rowMap.get("objectId");
                    _logger.info("strPartId:{}", strPartId);
                    if (UIUtil.isNullOrEmpty(strPartId)) {
                        strMess = strNoSelectedPart;
                        isAlert = true;
                        break;
                    }
                    if (isSelectedPartRevisionExport) {
                        DomainObject selectedPartObj = DomainObject.newInstance(context, strPartId);
                        if ("LastRevision".equals(strExportType)) {
                            if (!selectedPartObj.isLastRevision(context)) {
                                BusinessObject lastRevisionBO = selectedPartObj.getLastRevision(context);
                                strPartId = lastRevisionBO.getObjectId(context);
                            }
                        } else if ("LastRevisionRelease".equals(strExportType)) {
                            String strLastReleaseId = JPO.invoke(context, "JF_Util", null, "getLastReleasedMajorid", new String[]{strPartId}, String.class);
                            _logger.info("strLastReleaseId:{}", strLastReleaseId);
                            if (UIUtil.isNotNullAndNotEmpty(strLastReleaseId)) {
                                strPartId = strLastReleaseId;
                            } else {
                                isAlert = true;
                                releaseErrorList.add(selectedPartObj.getAttributeValue(context, "EnterpriseExtension.V_PartNumber"));
                                continue;
                            }
                        }
                    }
                    if ("ebom".equals(strMode) && idSet.isEmpty()) {
                        _logger.info("strMode:{} idSet:{}", strMode, idSet);
                        strExportProjectId = JPO.invoke(context, "JF_Util", new String[0], "getPartBelongProject", new String[]{strPartId}, String.class);
                        if (UIUtil.isNullOrEmpty(strExportProjectId)) {
                            strMess = strFirstPartNoProject;
                            isAlert = true;
                            break;
                        }
                    }
                    idSet.add(strPartId);
                    selectedSupplyPartIds.add(strPartId);
                }
            }
            if (isSelectedPartRevisionExport && releaseErrorList.size() > 0) {
                strMess = strReleased.replace("$1", releaseErrorList.join(","));
                isAlert = true;
            }
            //数量为空直接提示
            if (idSet.size() <= 0) {
                strMess = strMess.replace("$1","");
                isAlert = true;
            }
            if (!isAlert && UIUtil.isNullOrEmpty(strExportProjectId)) {
                strMess = strProjectRequired;
                isAlert = true;
            }
            _logger.info("strExportProjectId:{}", strExportProjectId);
            if (!isAlert) {
                try {
                    DomainObject.newInstance(context, strExportProjectId).getInfo(context, SELECT_ID);
                } catch (Exception e) {
                    _logger.error("导出BOM项目ID无效:{}", strExportProjectId, e);
                    strMess = strProjectInvalid;
                    isAlert = true;
                }
            }
        }else {
            String[] tableRowIdList = emxGetParameterValues(request, "emxTableRowId");
            //转换为通用的
            if ("ajax".equals(strDownloadType)){
                String[] split = strSelectRowIds.split(",");
                for (int i = 0; i < split.length; i++) {
                    split[i] = "|"+split[i]+"||";
                }
                tableRowIdList = split;
            }
            DomainObject BO = DomainObject.newInstance(context);
            for (String tableRowId : tableRowIdList) {
                Map rowMap = ProgramCentralUtil.parseTableRowId(context,tableRowId);
                String strPartId = (String) rowMap.get("objectId");
                BO.setId(strPartId);
                boolean isLastRevision = BO.isLastRevision(context);
                _logger.info("isLastRevision:{}",isLastRevision);
                if ("LastRevision".equals(strExportType)){
                    if (!isLastRevision){
                        BusinessObject lastRevisionBO = BO.getLastRevision(context);
                        //获取最新版ID
                        strPartId = lastRevisionBO.getObjectId(context);
                    }
                }else if ("LastRevisionRelease".equals(strExportType)){
                    //获取最新发布版id
                    String strLastReleaseId = JPO.invoke(context, "JF_Util", null, "getLastReleasedMajorid", new String[]{strPartId}, String.class);
                    _logger.info("strLastReleaseId:{}",strLastReleaseId);
                    if (UIUtil.isNotNullAndNotEmpty(strLastReleaseId)){
                        strPartId = strLastReleaseId ;
                    } else {
                        isAlert = true;
                        releaseErrorList.add(BO.getAttributeValue(context, "EnterpriseExtension.V_PartNumber"));
                    }
                }
                idSet.add(strPartId);
            }
            strMess = strReleased.replace("$1",releaseErrorList.join(","));
        }
        _logger.info("isAlert:{}",isAlert);
        if (!isAlert) {
            Map argsMap = new HashMap<>();
            argsMap.put("ids",idSet);
            argsMap.put("path",strPath);
            argsMap.put("projectId",strExportProjectId);
            //20260820 update by caipan 传递导出入口模式，EBOM导出按每个零件所属项目读取客户零件号和Direct buy。
            argsMap.put("mode",strMode);
            if (isExportObjectNameFile) {
                argsMap.put("exportObjectName", bo.getInfo(context, SELECT_NAME));
            }
            if (isSelectedPartExport) {
                argsMap.put("selectedSupplyPartIds", selectedSupplyPartIds);
            }
            String[] args = JPO.packArgs(argsMap);
            String methodName = isSelectedPartExport ? "downLoadSelectedPartBOMInfoList" : "downLoadLastRevisionBOMInfoListSecond";
            Map res = JPO.invoke(context, "JF_LastRevisionBOMService", null, methodName, args, Map.class);
//            Map res = JPO.invoke(context, "JF_LastRevisionBOMService", null, "downLoadLastRevisionBOMInfoList", args, Map.class);
            Workbook workbook =  (Workbook)res.get("file");
            String strFileName =  (String)res.get("fileName");
            // 处理文件名编码
            strFileName = URLEncoder.encode(strFileName, "UTF-8").replaceAll("\\+", "%20");
            // 设置 Content-Disposition 头
            StringBuffer sb = new StringBuffer();
            sb.append( "attachment; filename=\"");
            sb.append(strFileName);
            sb.append("\"; filename*=UTF-8''");
            sb.append(strFileName);
            out.clear();
            //清空响应体
            response.reset();
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition",sb.toString());
//            workbook.write(response.getOutputStream());
            outStream = response.getOutputStream();
            workbook.write(outStream);
            outStream.flush(); // 必须刷新，确保数据推送到 Nginx/客户端
            return;
        }else {
            if ("ajax".equalsIgnoreCase(strDownloadType) && "snapshot".equals(strMode)){
                resMap.put("code","404");
                resMap.put("mess",strMess);
%>
<%=gson.toJson(resMap)%>
<%
}else {
    _logger.info("strMess:{}",strMess);
%>
<script>
    alert("<%=strMess%>");
    <%--getTopWindow().showTransientMessage("<%=strLoading%>", 'info', 'alert-right-search');--%>
</script>
<%
            return;
        }
    }
} catch (MatrixException e) {
    _logger.error(e.getMessage());
    e.printStackTrace();
    resMap.put("code","404");
    String strRequestError = EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", context.getLocale(), "emxComponents.Export.BOM.RequestError");
    resMap.put("mess",strRequestError.replace("$1", String.valueOf(e.getMessage())));
%>
<%=gson.toJson(resMap)%>
<%
    } finally {
        if (outStream != null) {
            try { outStream.close(); } catch (IOException e) { /* ignore */ }
        }
    }


%>


