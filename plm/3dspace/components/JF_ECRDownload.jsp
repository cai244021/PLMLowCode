<%@ page import="com.matrixone.apps.domain.util.EnoviaResourceBundle" %>
<%@ page import="com.matrixone.apps.common.util.ComponentsUtil"%>
<%@ page import="org.slf4j.LoggerFactory" %>
<%@ page import="org.slf4j.Logger" %>
<%@ page import="java.util.Map" %>
<%@ page import="com.matrixone.apps.framework.ui.UIUtil" %>
<%@ page import="com.matrixone.servlet.Framework" %>
<%@ page import="java.io.FileInputStream" %>
<%@ page import="java.io.OutputStream" %>
<%@ page import="java.io.IOException" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="matrix.db.JPO" %>
<%@ page pageEncoding="utf-8" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%!
	private static final Logger _logger =  LoggerFactory.getLogger("JF_ECRDownload");
%>
<%
	//清空响应体
	response.reset();
	// 【必须加这两行！关闭 JSP 默认的 getWriter()】
	out.clear();
	out = pageContext.pushBody();

	matrix.db.Context context 	= Framework.getFrameContext(session);
	String strObjectId 		= com.matrixone.apps.domain.util.Request.getParameter(request, "objectId");
	String strUploadType 		= com.matrixone.apps.domain.util.Request.getParameter(request, "type");
	String strPath = request.getRealPath("");
	_logger.info("objectId:{}",strObjectId);
	HashMap params = new HashMap();
	params.put("objectId", strObjectId);
	params.put("type", strUploadType);
	params.put("path", strPath);
	String initargs[] = {};
	String strJpoName = "";
	String strFunName = "";
	if ("JFNewECRCosting".equals(strUploadType) || "JFNewECRController".equals(strUploadType)){
		strJpoName = "JF_NewECRService";
		strFunName = "NewECRDownload";
	}else if ("JFECRCosting".equals(strUploadType) || "JFECRController".equals(strUploadType)){
		strJpoName = "JF_ECRService";
		strFunName = "ECRDownload";
	} else if ("JFFormalECRCosting".equals(strUploadType) || "JFFormalECRController".equals(strUploadType)){
		strJpoName = "JF_FormalECRService";
		strFunName = "FormalECRDownload";
	}else if ("JFFormalInitialCostBuy".equals(strUploadType) || "JFFormalInitialCostMake".equals(strUploadType)){
		strJpoName = "JF_FormalECRService";
		strFunName = "FormalInitialCostDownload";
	}
	Map res = (Map) JPO.invoke(context, strJpoName, initargs, strFunName, JPO.packArgs (params), Map.class);
	String strFullPath = (String) res.get("path");
	String strFileName = (String) res.get("filename");
	if (UIUtil.isNotNullAndNotEmpty(strFullPath)){
		// 设置响应头
		response.setContentType("application/octet-stream");
		response.setHeader("Content-Disposition", "attachment; filename=\"" + strFileName + "\"");
		// 读取文件内容
		FileInputStream fis = null;
		OutputStream os = null;
		try {
			fis = new FileInputStream(strFullPath);
			os = response.getOutputStream();
			byte[] buffer = new byte[4096];
			int length;
			while ((length = fis.read(buffer)) > 0) {
				os.write(buffer, 0, length);
			}
			os.flush();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (fis != null) {
				try {
					fis.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (os != null) {
				try {
					os.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
%>
