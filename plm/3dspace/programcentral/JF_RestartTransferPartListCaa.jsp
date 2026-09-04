<%@ page import="com.matrixone.apps.domain.DomainObject" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="java.util.Map" %>
<%@ page import="com.matrixone.apps.program.ProgramCentralUtil" %>
<%@ page import="com.matrixone.apps.domain.DomainRelationship" %>
<%@ page import="matrix.util.StringList" %>
<%@ page import="com.matrixone.apps.domain.util.MapList" %>
<%@ page import="matrix.db.JPO" %>
<%@ page import="com.matrixone.apps.common.util.ComponentsUIUtil" %>
<%@ page import="com.matrixone.apps.domain.DomainConstants" %>
<%@ page import="java.util.Iterator" %>
<%@ page import="com.matrixone.apps.framework.ui.UIUtil" %>
<%@ page import="org.slf4j.LoggerFactory" %>
<%@ page import="org.slf4j.Logger" %>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="utf-8" %>
<%@ include file="../programcentral/emxProgramGlobals2.inc" %>
<%@include file = "../emxUICommonAppInclude.inc"%>
<%@include file = "../common/enoviaCSRFTokenValidation.inc"%>
<script src="../common/scripts/emxUICore.js" type="text/javascript"></script>
<%!
    private static final Logger _logger = LoggerFactory.getLogger("JF_RestartTransferPartListCaa.jsp");
%>
<%
    _logger.info("==============================================");
    String mode = (String) emxGetParameter(request, "mode");
    _logger.info("mode:{}", mode);
    String objectId = (String) emxGetParameter(request, "objectId");
    _logger.info("objectId:{}", objectId);
    DomainObject domainObject = DomainObject.newInstance(context);
    StringList boSel = new StringList();
    boSel.add(DomainConstants.SELECT_ID);
    boSel.add(DomainConstants.SELECT_OWNER);
    boSel.add("physicalid");
    StringList reSel = new StringList();
    reSel.add(DomainRelationship.SELECT_ID);
    reSel.add("attribute[JSdataProcessProgress]");
    reSel.add("attribute[JF_SyncStatus]");
    reSel.add(DomainRelationship.SELECT_ID);
    domainObject.setId(objectId);
    MapList partListMapList = domainObject.getRelatedObjects(context,
            "JFPartList2VPMReference", // relationship pattern
            "VPMReference",                                    // object pattern
            boSel,                            // object selects
            reSel, // relationship selects
            false,                                        // to direction
            true,                                        // from direction
            (short) 1,                                    // recursion level
            "",                // object where clause
            "",
            (short) 0
    );
    _logger.info("partListMapList:{}", partListMapList);

    Iterator iterator = partListMapList.iterator();
    MapList mapList = new MapList();
    while (iterator.hasNext()) {
        Map map = (Map)iterator.next();
        String progress = UIUtil.getValue(map, "attribute[JSdataProcessProgress]");
        String syncStatus = UIUtil.getValue(map, "attribute[JF_SyncStatus]");
        if ("Transfer".equalsIgnoreCase(mode)) {
            if ("CAAProcessingFailed".equalsIgnoreCase(progress)) {
                mapList.add(map);
            }
        }
//        else if ("Resynchronize".equalsIgnoreCase(mode)) {
//            if ("CAAProcessingComplete".equalsIgnoreCase(progress) && "unsynchronized".equalsIgnoreCase(syncStatus)) {
//                mapList.add(map);
//            }
//        }
    }
    _logger.info("mapList:{}", mapList);
    Map<String, Object> map = new HashMap<>();
    map.put("data", mapList);
    map.put("objectId", objectId);
    _logger.info("map:{}", map);
    if ("Transfer".equalsIgnoreCase(mode)) {
        JPO.invoke(context, "JF_PartList", new String[]{}, "restartTransferPartListAgain", JPO.packArgs(map), void.class);
    } else if ("Resynchronize".equalsIgnoreCase(mode)) {
        //重新同步
        JPO.invoke(context, "JF_ProjectSpace", new String[]{}, "getPartListinfoSendSRM", new String[]{objectId}, void.class);
    }

    %>
<html>
<script>
    alert("\u5df2\u91cd\u65b0\u53d1\u8d77PartList\u6570\u636e\u540c\u6b65\uff01...");
    parent.getTopWindow().findFrame(parent.getTopWindow(),"detailsDisplay").location.href = parent.getTopWindow().findFrame(parent.getTopWindow(),"detailsDisplay").location.href.replace("persist=true", "persist=false");
</script>
</html>