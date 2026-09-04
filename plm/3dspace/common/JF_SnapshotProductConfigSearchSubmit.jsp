<%@include file = "emxNavigatorInclude.inc"%>
<%@page import="com.matrixone.apps.domain.DomainConstants,
                com.matrixone.apps.domain.DomainObject,
                com.matrixone.apps.domain.util.XSSUtil,
                com.matrixone.apps.framework.ui.UIUtil,
                java.util.Map,
                java.util.StringTokenizer,
                matrix.util.StringList"%>
<%
response.setHeader("Cache-Control", "no-cache");
response.setHeader("Pragma", "no-cache");

String productConfigId = "";
String productConfigDisplay = "";
String[] selectedRows = emxGetParameterValues(request, "emxTableRowId");
if (selectedRows != null && selectedRows.length > 0) {
    StringTokenizer tokenizer = new StringTokenizer(selectedRows[0], "|");
    if (tokenizer.hasMoreTokens()) {
        productConfigId = tokenizer.nextToken();
        DomainObject productConfig = DomainObject.newInstance(context, productConfigId);
        StringList selects = new StringList();
        selects.add(DomainConstants.SELECT_NAME);
        selects.add(DomainConstants.SELECT_REVISION);
        selects.add(DomainConstants.SELECT_ATTRIBUTE_TITLE);
        Map productConfigInfo = productConfig.getInfo(context, selects);
        String title = (String) productConfigInfo.get(DomainConstants.SELECT_ATTRIBUTE_TITLE);
        String name = (String) productConfigInfo.get(DomainConstants.SELECT_NAME);
        String revision = (String) productConfigInfo.get(DomainConstants.SELECT_REVISION);
        productConfigDisplay = (UIUtil.isNullOrEmpty(title) ? name : title) + " - " + revision;
    }
}
%>
<script src="scripts/emxUICore.js" type="text/javascript"></script>
<script type="text/javascript">
var targetWindow = getTopWindow().getWindowOpener();
if (targetWindow) {
    var actualField = targetWindow.document.getElementById("JFSnapshotProductConfig");
    var displayField = targetWindow.document.getElementById("JFSnapshotProductConfigDisplay");
    if (actualField) {
        actualField.value = "<%=XSSUtil.encodeForJavaScript(context, productConfigId)%>";
    }
    if (displayField) {
        displayField.value = "<%=XSSUtil.encodeForJavaScript(context, productConfigDisplay)%>";
    }
}
getTopWindow().closeWindow();
</script>
