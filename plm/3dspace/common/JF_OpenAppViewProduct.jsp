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
<%@ include file="../programcentral/emxProgramGlobals2.inc" %>
<%@include file = "../emxUICommonAppInclude.inc"%>
<%@include file = "../common/enoviaCSRFTokenValidation.inc"%>

<script src="../common/scripts/emxUICore.js" type="text/javascript"></script>
<%
    String alertMess = DomainConstants.EMPTY_STRING;
    String strFlushTableName = DomainConstants.EMPTY_STRING;
    String suiteKey = "emxComponentsStringResource";
    String strURL = "";
    try {
        String strObjectId = DomainConstants.EMPTY_STRING;
        String[] strSelectIds = emxGetParameterValues(request, "emxTableRowId");
        String objectId = (String) emxGetParameter(request, "objectId");
        System.out.println("objectId:" + objectId);
        System.out.println("strSelectIds:" + strSelectIds);
        String flag = (null == strSelectIds) ? "N" : "Y";

        if ("Y".equalsIgnoreCase(flag)) {
            System.out.println("@@@@@@@@@@@@@@@@@@@");
            if (strSelectIds.length > 1) {
            %>
<script >
            alert("\u8bf7\u9009\u62e9\u4e00\u4e2a\u6570\u6a21\u5bf9\u8c61\u9884\u89c8\u6a21\u578b\uff01");
</script>

<%
                return;
            }
        }

        if ("N".equalsIgnoreCase(flag)) {
            strObjectId = objectId;
        } else {
            System.out.println("strSelectIds:" + Arrays.stream(strSelectIds).toList());
            String[] split = strSelectIds[0].split("\\|");
//            StringList strIdList = FrameworkUtil.split(strSelectIds[0], "|");
//            strObjectId = strIdList.get(0);
            strObjectId = split[1];
        }
        System.out.println("strObjectId:" + strObjectId);
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