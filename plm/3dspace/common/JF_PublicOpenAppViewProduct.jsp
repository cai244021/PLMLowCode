<%@ page import="com.matrixone.apps.domain.DomainObject" %>
<%@ page import="com.matrixone.apps.domain.DomainRelationship" %>
<%@ page import="com.matrixone.apps.framework.ui.UIUtil" %>
<%@ page import="matrix.util.StringList" %>
<%@ page import="com.goterl.lazysodium.interfaces.Hash" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="matrix.db.JPO" %>
<%@ page import="com.nomagic.esi.emf.a.B" %>
<%@ page import="com.matrixone.apps.domain.DomainConstants" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.util.stream.Collectors" %>
<%@ page import="com.matrixone.enovia.bps.notifications.NotificationService" %>
<%@ page import="java.net.URLEncoder" %>
<%@ page import="java.util.Arrays" %>
<%@ page import="com.matrixone.apps.domain.util.*" %>
<%@ page import="com.matrixone.apps.common.util.ComponentsUIUtil" %>
<%@ include file="../programcentral/emxProgramGlobals2.inc" %>
<%@include file = "../emxUICommonAppInclude.inc"%>
<%@include file = "../common/enoviaCSRFTokenValidation.inc"%>

<script src="../common/scripts/emxUICore.js" type="text/javascript"></script>
<%
    String strURL = "";
    try {
        String strObjectId = DomainConstants.EMPTY_STRING;
        String[] strSelectIds = emxGetParameterValues(request, "emxTableRowId");
        String[] splitTableRowIds = ComponentsUIUtil.getSplitTableRowIds(strSelectIds);
        strObjectId=splitTableRowIds[0];
        DomainObject domainObject = DomainObject.newInstance(context, strObjectId);
        String physicalId = domainObject.getInfo(context, "physicalid");
        String vName = domainObject.getAttributeValue(context, "PLMEntity.V_Name");
        NotificationService notificationService = new NotificationService(context);
        String dashboardURL =notificationService.get3DDashboardURL();
        System.out.println("dashboardURL:" + dashboardURL);

        String strURL1 ="{\"data\":{\"items\":[{\"objectId\":\""+physicalId+"\",\"objectType\":\"VPMReference\",\"envId\":\"OnPremise\",\"serviceId\":\"3DSpace\",\"displayName\":\""+vName+"\",\"displayType\":\"VPMReference\",\"contextId\":\"ctx::VPLMProjectLeader.Company Name.Common Space\",\"objectTaxonomies\":[\"PLMEntity\",\"PLMReference\",\"PLMCoreReference\",\"LPAbstractReference\",\"PHYSICALAbstractReference\",\"VPMReference\"]}]}}";
        System.out.println("strURL1:" + strURL1);

        strURL = dashboardURL+"/#/tab:New%20Tab/app:ENOSCEN_AP/content:X3DContentId=" + URLEncoder.encode(strURL1, "utf-8");
        //ENXDISC_AP   - 3D Navigate
        //ENOR3D_AP - 3D MarkUp
        //ENOSCEN_AP - Product Explorer

        System.out.println("strURL:" + strURL);
    }catch (Exception e) {
        e.printStackTrace();
    }
%>

<html>
<script >
        var strURL = "<%=strURL%>";
        console.log("strURL:", strURL)
        window.open(strURL, "_blank");
</script>
</html>