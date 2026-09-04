<%--<!DOCTYPE html>--%>
<%--<html>--%>
<%@ page import="org.slf4j.Logger" %>
<%@ page import="org.slf4j.LoggerFactory" %>
<%@ page import="matrix.util.StringList" %>
<%@ page import="java.util.StringTokenizer" %>
<%@ page import="matrix.db.JPO" %>
<%@ page import="java.util.Arrays" %>
<%@ page import="com.matrixone.apps.common.util.ComponentsUIUtil" %>
<%@ page import="com.matrixone.apps.domain.DomainObject" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.util.HashMap" %>
<
<%--????--%>
<%@include file = "emxNavigatorInclude.inc"%>
<script src="./scripts/emxUIModal.js" type="text/javascript"></script>
<script language="JavaScript" src="./scripts/emxUICore.js"></script>
<script language="JavaScript" src="./scripts/emxUIConstants.js"></script>
<script language="JavaScript" src="./scripts/emxUISearch.js"></script>
<script language="JavaScript" src="../webapps\ENOAEFCore\webroot\common\scripts/bpsTagNavSBInit.js"></script>

<%!
    //??
    private static final Logger _logger =  LoggerFactory.getLogger("JF_AddSnapshotContent.jsp");
%>
<%
    //?? oid
    String strObjectId = emxGetParameter(request, "objectId");
    DomainObject snapshot = DomainObject.newInstance(context,strObjectId);
    String JF_ProjectRel = snapshot.getInfo(context, "to[JFProject2Snapshot].from.description");
    //可能涉及到转义的情况
    Map ESCAPE_MAP = new HashMap();
    ESCAPE_MAP.put('/', 47);    // 斜杠
    ESCAPE_MAP.put('\\', 92);   // 反斜杠
    ESCAPE_MAP.put(':', 58);    // 冒号
    ESCAPE_MAP.put('*', 42);    // 星号
    ESCAPE_MAP.put('?', 63);    // 问号
    if (JF_ProjectRel == null || JF_ProjectRel.isEmpty()) {
    }else {
        StringBuilder sb = new StringBuilder();
        // 遍历原始字符串的每个字符
        for (char c : JF_ProjectRel.toCharArray()) {
            // 如果是需要转义的字符，替换为 _ASCII码_ 形式
            if (ESCAPE_MAP.containsKey(c)) {
                sb.append("_").append(ESCAPE_MAP.get(c)).append("_");
            } else {
                // 非特殊字符直接拼接
                sb.append(c);
            }
        }
        JF_ProjectRel =sb.toString();
    }
    _logger.info("JF_ProjectRel:{}",JF_ProjectRel);
    JF_ProjectRel = trimMultiSpace(JF_ProjectRel);
    _logger.info("JF_ProjectRel:{}",JF_ProjectRel);
    StringBuffer sbUrl = new StringBuffer("../common/emxFullSearch.jsp?field=TYPES=type_VPMReference");
//    StringBuffer sbUrl = new StringBuffer("../common/emxFullSearch.jsp?field=TYPES=type_VPMReference:itf.JF_VPMReference.JF_ProjectRel=");
//    sbUrl.append(JF_ProjectRel);
    sbUrl.append(":CURRENT=policy_VPLM_SMB_Definition_MajorRev.state_Approved,policy_VPLM_SMB_Definition_MajorRev.state_Released&showInitialResults=false&selection=multiple&submitURL=../common/JF_postPrecessAddExistingWithRel.jsp&rel=relationship_JFSnapshot2VPMReference&submitAction=refreshCaller");
    sbUrl.append("&objectId=");
    sbUrl.append(strObjectId);
    _logger.info("url:{}",sbUrl);


%>
<%!
    /**
     * 多个空格保留一个（正则版，推荐日常使用）
     * @param str 原始字符串
     * @return 处理后的字符串
     */
    public static String trimMultiSpace(String str) {
        if (str == null || str.isEmpty()) {
            return str; // 空值保护，避免NPE
        }
        // \\s+ 匹配任意空白符（空格、制表符等），若只想匹配空格用 " +"
        return str.replaceAll("\\s+", " ");
    }
%>
<script>
    var strUrl = "<%=sbUrl%>";
    showWizard(strUrl);
</script>
<%--MQL??--%>
<jsp:include  page="../components/emxMQLNotice.jsp" flush="true"/>
</html>