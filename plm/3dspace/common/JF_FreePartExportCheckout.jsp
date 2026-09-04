
<%@ page import="java.util.Map" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="org.slf4j.Logger" %>
<%@ page import="org.slf4j.LoggerFactory" %>
<%@ page import="com.matrixone.apps.domain.DomainConstants" %>
<%@ page import="com.matrixone.apps.domain.util.EnoviaResourceBundle" %>
<%@ page import="java.util.Objects" %>
<%@ page import="matrix.util.StringList" %>
<%@ page import="com.matrixone.apps.domain.util.FrameworkUtil" %>
<%@ page import="com.matrixone.apps.framework.ui.UIUtil" %>
<%@ page import="com.google.gson.Gson" %>
<%@ page import="org.apache.poi.ss.usermodel.Workbook" %>
<%@ page import="java.io.ByteArrayOutputStream" %>
<%@ page import="java.util.Base64" %>
<%@ page import="matrix.db.JPO" %>
<%@include file = "../emxUICommonAppInclude.inc"%>
<%@include file = "../components/emxComponentsNoCache.inc"%>
<%@include file = "../components/emxComponentsUtil.inc"%>
<jsp:useBean id="indentedTableBean" class="com.matrixone.apps.framework.ui.UITableIndented" scope="session"/>
<jsp:useBean id="tableBean" class="com.matrixone.apps.framework.ui.UITable" scope="session"/>
<script type="text/javascript" src="../common/scripts/jquery-latest.js"></script>
<script language="JavaScript" src="../common/scripts/emxUIConstants.js" type="text/javascript"></script>
<script language="JavaScript" src="../common/scripts/emxUICore.js" type="text/javascript"></script>

<%
    Gson gson = new Gson();
    HashMap<String, Object> map = new HashMap<>();
    String notesY = EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", context.getLocale(), "emxComponents.Mess.FreePartSuccess");
    String notesN = EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", context.getLocale(), "emxComponents.Mess.FreePartFailed");
    try {

        String[] tableRowIdList = emxGetParameterValues(request, "emxTableRowId");

        String projectid="";
        System.out.println("tableRowIdList---->"+tableRowIdList[0]);
        for (String tableRowId : tableRowIdList) {
            StringList tableIds = FrameworkUtil.split(tableRowId, "|");
            System.out.println(tableIds);
            System.out.println(tableIds.get(0));
            System.out.println(tableIds.get(1));
            projectid = tableIds.get(0);

        }
        System.out.println("projectid-11-->"+projectid);

        // 返回参数
        Map<String, Object> res = JPO.invoke(context, "JF_ProjectSpace", null, "freePartExportExcel", new String[]{projectid}, Map.class);
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
//            System.out.println("map--->"+map);
        } else {
            map.put("fileName", "");
            map.put("error","");
        }
    } catch (Exception e) {
        e.printStackTrace();
        map.put("error", "An error occurred: " + e.getMessage()); // 添加错误信息
    }
%>



<script language="JavaScript">
    const res=<%=gson.toJson(map)%>;
    console.log("res:", res);
    fun(res);

    function fun(res) {
        // console.log("res:", res);
        // res = res.trim();
        // let words = res.split("{");
        // let data = JSON.parse("{" + words[1]);
        let data=res;
        let fileName = data.fileName;
        let base64File = data.base64File;
        if (fileName.length > 0) {
            let bstr = atob(base64File),//解析buse-64编码的字符串
                n = bstr.length,
                u8arr = new Uint8Array(n);//创建初始化为0的,包含Length个元素的无符号整型数组
            while (n--) {
                u8arr[n] = bstr.charCodeAt(n);//返回字符串第一个字符的Unicode 编码
            }
            let blob = new Blob([u8arr]);
            console.log("blob:", blob);
            //创建一个a标签并设置href属性,之后模拟人为点击下载文件
            let link = document.createElement('a');
            link.href = window.URL.createObjectURL(blob);
            link.download = fileName;//设置下载文件名
            link.click();//模拟点击
            //释放资源并删除创建的a标签
            // window.URL.revokeObjectURL(url);
            mess = "<%=notesY%>";
            // mess = "success";
        } else {
            mess = "<%=notesN%>";
            // mess = "error";
        }

        setTimeout(function() {
            alert(mess);
        }, 1000);
    }


</script>


