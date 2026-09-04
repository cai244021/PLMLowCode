<%@ page import="com.matrixone.apps.framework.ui.framesetObject" %>
<%@ page import="org.slf4j.LoggerFactory" %>
<%@ page import="org.slf4j.Logger" %>
<%@ page import="com.matrixone.apps.domain.util.EnoviaResourceBundle" %>
<%@include file="../emxUIFramesetUtil.inc"%>
<%!
    //日志
    private static final Logger _logger =  LoggerFactory.getLogger("JF_CreateFlexiblePartFS");
%>
<%
    _logger.info("----------------------------------- JF_CreateFlexiblePartFS begin ------------------------------------");
    String PageHeading = EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", context.getLocale(), "emxComponent.Common.createFlexPart");
    String HelpMarker = "";
    //零件id
    String strObjectId = emxGetParameter(request,"objectId");
    _logger.info("strObjectId:{}",strObjectId);
    StringBuffer sbContentURL =  new StringBuffer();
    sbContentURL.append("JF_CreateFlexiblePartPre.jsp?");
    sbContentURL.append("objectId=");
    sbContentURL.append(strObjectId);
    framesetObject fs = new framesetObject();
    fs.initFrameset(PageHeading,HelpMarker,sbContentURL.toString(),false,true,false,false);
    fs.useCache(false);
    fs.setStringResourceFile("emxFrameworkStringResource");
    fs.removeDialogWarning();

    fs.createCommonLink("emxFramework.Common.Done",
            "createPart()",
            "role_GlobalUser",
            false,
            true,
            "common/images/buttonDialogAdd.gif",
            false,
            1);

    fs.createCommonLink("emxFramework.Common.Cancel",
            "parent.window.close()",
            "role_GlobalUser",
            false,
            true,
            "common/images/buttonDialogCancel.gif",
            false,
            3);
    fs.writePage(out);
%>

<script>
</script>


