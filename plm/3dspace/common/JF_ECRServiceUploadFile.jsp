<%@ page import="org.slf4j.Logger" %>
<%@ page import="org.slf4j.LoggerFactory" %>
<%@page import="org.apache.commons.fileupload.*,java.util.*,java.io.*"%>
<%@ page import="com.matrixone.servlet.Framework" %>
<%@ page import="matrix.db.Environment" %>
<%@ page import="matrix.db.JPO" %>
<%!
    //日志
    private static final Logger _logger =  LoggerFactory.getLogger("Idm_ECRServiceUploadFile");
%>
<%
    matrix.db.Context context 	= Framework.getFrameContext(session);
    String sFolderWIN 	= "c:\\temp\\";
    String sFolderUNIX 	= "/tmp/";
    String sLanguage 	= request.getHeader("Accept-Language");
    String sOID 		= com.matrixone.apps.domain.util.Request.getParameter(request, "objectId");
    String sRelType 		= com.matrixone.apps.domain.util.Request.getParameter(request, "relationship");
    String sType 		= com.matrixone.apps.domain.util.Request.getParameter(request, "type");
    String sHostUrl 		= com.matrixone.apps.domain.util.Request.getParameter(request, "hostUrl");
    String sCommand 	= com.matrixone.apps.domain.util.Request.getParameter(request, "documentCommand");
    String sOSName 		= System.getProperty ( "os.name" );
    String sFolder		= sOSName.contains("Windows") ? sFolderWIN : sFolderUNIX ;
    String separator 	= sOSName.contains("Windows") ? "\\" : "/";
    String sTmpDir		= Environment.getValue(context, "TMPDIR");
    if(null != sTmpDir && !sTmpDir.trim().isEmpty())
    {
        sFolder = sTmpDir;
        if(!sFolder.substring(sFolder.length() - 1).equals(separator))
            sFolder = sFolder + separator;
    }
    // Save file on disk
    DiskFileUpload upload	= new DiskFileUpload();
    //List items 				= upload.parseRequest(request);
    List files = upload.parseRequest(request);
    _logger.info("files:{}",files);
    String initargs[] = {};
    HashMap params = new HashMap();
    params.put("language",sLanguage);
    params.put("objectId",sOID);
    params.put("type",sType);
    params.put("hostUrl",sHostUrl);
    params.put("relationship",sRelType);
    params.put("documentCommand",sCommand);
    params.put("folder",sFolder);
    params.put("files",files);
    params.put("objectAction","create");
    params.put("timezone", (String)session.getAttribute("timeZone"));
    String sResult = (String)JPO.invoke(context, "JF_FileUtils", initargs, "checkinFile", JPO.packArgs (params), String.class);
    _logger.info("file checkin result:{}",sResult);
%>
<%=sResult%>