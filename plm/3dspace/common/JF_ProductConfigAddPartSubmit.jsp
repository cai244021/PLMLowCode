<%@include file = "./emxNavigatorInclude.inc"%>
<%@ page import="matrix.db.JPO" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="java.util.Map" %>
<%@ page import="matrix.util.StringList" %>
<%@ page import="java.util.StringTokenizer" %>
<%@ page import="com.matrixone.apps.domain.util.XSSUtil" %>
<%@ page import="com.matrixone.apps.domain.DomainObject" %>
<%@ page import="com.matrixone.apps.framework.ui.UIUtil" %>
<script src="scripts/emxUICore.js" type="text/javascript"></script>
<%
    response.setHeader("Cache-Control", "no-cache");
    response.setHeader("Pragma", "no-cache");

    String objectId = emxGetParameter(request, "objectId");
    String positionId = emxGetParameter(request, "positionId");
    String positionIds = emxGetParameter(request, "positionIds");
    String[] emxTableRowIds = emxGetParameterValues(request, "emxTableRowId");
    StringList partIds = new StringList();
    if (emxTableRowIds != null) {
        for (int i = 0; i < emxTableRowIds.length; i++) {
            String rowId = emxTableRowIds[i];
            if (rowId != null && rowId.length() > 0) {
                StringTokenizer tokenizer = new StringTokenizer(rowId, "|");
                if (tokenizer.hasMoreTokens()) {
                    while (tokenizer.hasMoreTokens()) {
                        String candidateId = tokenizer.nextToken();
                        if (UIUtil.isNullOrEmpty(candidateId) || partIds.contains(candidateId)) {
                            continue;
                        }
                        try {
                            DomainObject candidateObj = DomainObject.newInstance(context, candidateId);
                            if ("VPMReference".equals(candidateObj.getInfo(context, DomainObject.SELECT_TYPE))) {
                                partIds.add(candidateObj.getInfo(context, DomainObject.SELECT_ID));
                                break;
                            }
                        } catch (Exception ignore) {
                        }
                    }
                }
            }
        }
    }

    Map params = new HashMap();
    params.put("objectId", objectId);
    params.put("positionId", positionId);
    params.put("positionIds", positionIds);
    params.put("partIds", partIds);
    Map result = (Map) JPO.invoke(context, "JF_ProductConfig", null, "addPartsToPosition", JPO.packArgs(params), Map.class);
    String code = String.valueOf(result.get("code"));
    String mess = result.get("mess") == null ? "" : String.valueOf(result.get("mess"));
    String debugMessage = "Selected parts: " + partIds.toString() + "; result: " + code + "; " + mess;
%>
<script type="text/javascript">
    var code = "<%=XSSUtil.encodeForJavaScript(context, code)%>";
    var mess = "<%=XSSUtil.encodeForJavaScript(context, mess)%>";
    var debugMessage = "<%=XSSUtil.encodeForJavaScript(context, debugMessage)%>";
    if (code !== "200" && mess) {
        alert(debugMessage);
    }
    var postMessageText = code === "200" ? mess : (mess || debugMessage);
    var openerWindow = null;
    try {
        openerWindow = getTopWindow().getWindowOpener ? getTopWindow().getWindowOpener() : window.opener;
    } catch (e) {
        openerWindow = window.opener;
    }
    if (openerWindow && openerWindow.postMessage) {
        openerWindow.postMessage({type: "JF_PRODUCT_CONFIG_PARTS_CHANGED", code: code, message: postMessageText}, "*");
    }
    getTopWindow().closeWindow();
</script>
