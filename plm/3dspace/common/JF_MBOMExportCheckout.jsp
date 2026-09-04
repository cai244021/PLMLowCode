<%@ page import="matrix.db.JPO" %>
<%@ page import="org.apache.poi.ss.usermodel.Workbook" %>
<%@ page import="java.util.Map" %>
<%@ page import="com.google.gson.Gson" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="java.io.ByteArrayOutputStream" %>
<%@ page import="java.util.Base64" %>
<%@ page import="java.io.File" %>
<%@ page import="java.io.FileInputStream" %>
<%@ page import="org.slf4j.LoggerFactory" %>
<%@ page import="org.slf4j.Logger" %>
<%@include file = "../common/emxNavigatorNoDocTypeInclude.inc"%>
<jsp:useBean id="formBean" scope="page" class="com.matrixone.apps.common.util.FormBean" />
<jsp:useBean id="requestBean" scope="page" class="com.matrixone.apps.domain.util.Request"/>
<%!
    private static final Logger _logger = LoggerFactory.getLogger("JF_MBOMExportCheckout.jsp");
%>
<%
    Gson gson = new Gson();
    HashMap<String, Object> map = new HashMap<>();
    formBean.processForm(session, request);
    String mbomType = (String) formBean.getElementValue("mbomType");
    String projectId = (String) formBean.getElementValue("objectId");
    _logger.info("mbomType：projectId：{} {}", mbomType,projectId);
    try {
        // 返回参数
//        Map<String, Object> res = JPO.invoke(context, "JF_ExportMBOM", null, "exportMBOMExcel", new String[]{projectId,mbomType}, Map.class);
        Map<String, Object> res = JPO.invoke(context, "JF_FormalExportImportMBOM", null, "exportFormalMBOMExcel", new String[]{projectId,mbomType}, Map.class);
        String flag = (String) res.get("flag");
        if ("Y".equalsIgnoreCase(flag)) {
            Workbook workbook = (Workbook) res.get("file");
            String strFileName = (String) res.get("fileName");

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            workbook.write(bos);
            //转换为字节流数组
            byte[] bFileArr = bos.toByteArray();
            String strB64 = Base64.getEncoder().encodeToString(bFileArr);
            map.put("base64File", strB64);
            //清空workbook`
            map.put("fileName", strFileName);
        } else {
            map.put("fileName", "");
        }
    } catch (Exception e) {
        e.printStackTrace();
        map.put("error", "An error occurred: " + e.getMessage()); // 添加错误信息
    }
%>
<%=gson.toJson(map)%>