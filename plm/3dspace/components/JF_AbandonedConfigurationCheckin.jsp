<%@page import="com.matrixone.apps.domain.DomainObject"%>
<%@ page import="static com.matrixone.apps.common.util.JSPUtil.emxGetParameterValues" %>
<%@ page import="com.matrixone.apps.domain.DomainRelationship" %>
<%@ page import="matrix.util.StringList" %>
<%@ page import="matrix.db.JPO" %>
<%@ page import="java.util.Map" %>
<%@ page import="com.matrixone.apps.domain.DomainConstants" %>
<%@ page import="com.matrixone.apps.framework.ui.UIUtil" %>
<%@ page import="com.matrixone.apps.domain.util.*" %>
<%@ page import="java.util.stream.Collectors" %>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="utf-8" %>
<script src="../common/scripts/emxUIModal.js" type="text/javascript"></script>
<script language="JavaScript" src="../common/scripts/emxUICore.js"></script>
<script language="JavaScript" src="../common/scripts/emxUIConstants.js"></script>
<script language="JavaScript" src="../common/scripts/emxUISearch.js"></script>
<%@include file="../emxUIFramesetUtil.inc"%>
<%
    String strMess = EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", context.getLocale(), "emxComponents.Alert.AbandonedConfigurationSuccess");
    String  flag = "Y";
    try {
        String objectId = emxGetParameter(request, "objectId");
        String[] strSelectIds = emxGetParameterValues(request, "emxTableRowId");
        StringList tableRowIdList = new StringList();
        StringList errorConfigList = new StringList();
        for(String tableRowId : strSelectIds) {
            StringList tableIds = FrameworkUtil.split(tableRowId,"|");
            System.out.println("len:" + tableIds.size());
            String oid = DomainConstants.EMPTY_STRING;
            if (tableIds.size() == 3) {
                oid = tableIds.get(0);
            } else if (tableIds.size() == 4) {
                oid = tableIds.get(1);
            }
            DomainObject domainObject = DomainObject.newInstance(context, oid);
            String current = domainObject.getInfo(context, DomainConstants.SELECT_CURRENT);
            if ("Inactive".equalsIgnoreCase(current)) {
                errorConfigList.add(domainObject.getInfo(context, DomainConstants.SELECT_NAME));
            }
            tableRowIdList.add(oid);
        }
        ContextUtil.startTransaction(context, true);
        if (errorConfigList.size() > 0) {
            //有废弃的配置，不能选择
            strMess = EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", context.getLocale(), "emxComponents.Alert.hasAbandonedConfiguration");
            strMess += errorConfigList.join(",");
            flag = "N";
        } else {
            StringList strings = new StringList();
            strings.add(DomainConstants.SELECT_ID);
            strings.add(DomainConstants.SELECT_NAME);
            strings.add("attribute[EnterpriseExtension.V_PartNumber].value");
            for (String tableRowId : tableRowIdList) {
                String oid = tableRowId;
                //本身作废
                DomainObject domainObject = DomainObject.newInstance(context, oid);
                String mql = "mod bus " + oid + " current Inactive";
                MqlUtil.mqlCommand(context, false, mql, true);
                //寻找该产品下的整椅
                StringList infoList = domainObject.getInfoList(context, "from[JFPC2EBOM].to.id");
                for (int i = 0; i < infoList.size(); i++) {
                    String vid = infoList.get(i);
                    DomainObject domainObject1 = DomainObject.newInstance(context, vid);
                    String attributeValue = domainObject1.getAttributeValue(context, "EnterpriseExtension.V_PartNumber");
                    if (UIUtil.isNotNullAndNotEmpty(attributeValue)) {
                        if (attributeValue.startsWith("GX")) {
                            String mql1 = "mod bus " + vid + " current OBSOLETE";
                            MqlUtil.mqlCommand(context, false, mql1, true);
                            MapList mlPartInfoList = domainObject1.getRelatedObjects(
                                    context,
                                    "VPMInstance", // relationship pattern
                                    "VPMReference", // object pattern
                                    strings,// object selects
                                    new StringList(),  // relationship selects
                                    false, // to direction
                                    true, // from direction
                                    (short) 1, // recursion level
                                    "attribute[JF_VPMReference.JF_PartType].value==C&&attribute[JF_VPMReference.JF_ISWholeChair]=='WholeChair'", //object where clause
                                    "", //relationship where clause
                                    0
                            );
                            StringList gcIds = (StringList) mlPartInfoList.stream().map(m -> {
                                Map map = (Map) m;
                                return UIUtil.getValue(map, DomainConstants.SELECT_ID);
                            }).collect(Collectors.toCollection(StringList::new));
                            for (int j = 0; j < gcIds.size(); j++) {
                                String mql2 = "mod bus " + gcIds.get(j) + " current OBSOLETE";
                                MqlUtil.mqlCommand(context, false, mql2, true);
                            }
                        } else if (attributeValue.startsWith("GC")) {
                            String mql2 = "mod bus " + vid + " current OBSOLETE";
                            MqlUtil.mqlCommand(context, false, mql2, true);
                        }
                    }
                }
            }
        }
        ContextUtil.commitTransaction(context);
    }catch (Exception e) {
        strMess = EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", context.getLocale(), "Alert.AbandonedConfigurationFailed");
        flag = "N";
        e.printStackTrace();
        ContextUtil.abortTransaction(context);
    }

%>
<html>
<script>

    alert("<%=strMess%>");
    if ("Y" === "<%=flag%>") {
        var refreshURL = window.parent.location.href;
        window.parent.location.href = refreshURL;
    }
</script>
</html>
