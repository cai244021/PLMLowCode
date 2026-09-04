<%--<!DOCTYPE html>--%>
<%--<html>--%>
<%@ page import="org.slf4j.Logger" %>
<%@ page import="org.slf4j.LoggerFactory" %>
<%@ page import="com.matrixone.apps.domain.DomainObject" %>
<%@ page import="matrix.util.StringList" %>
<%@ page import="static com.matrixone.apps.domain.DomainRelationship.SELECT_ID" %>
<%@ page import="static com.matrixone.apps.domain.DomainConstants.SELECT_NAME" %>
<%@ page import="static com.matrixone.apps.domain.DomainConstants.SELECT_TYPE" %>
<%@ page import="com.matrixone.apps.domain.util.*" %>
<%@ page import="java.util.stream.Collectors" %>
<%@ page import="static com.matrixone.apps.common.util.JSPUtil.emxGetParameterValues" %>
<%@ page import="java.util.Set" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.HashSet" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.util.*" %>
<%@ page import="static com.matrixone.apps.domain.DomainConstants.*" %>
<%--登录检查--%>
<%@include file = "emxNavigatorInclude.inc"%>
<%--<script language="JavaScript" src="scripts/emxUIConstants.js" type="text/javascript"></script>--%>
<%--<script language="javascript" src="scripts/emxUICore.js"></script>--%>
<%--<script language="javascript" src="scripts/emxUIModal.js"></script>--%>
<%--<script language="javascript" src="scripts/emxUIFormUtil.js"></script>--%>
<%!
    //日志
    private static final Logger _logger =  LoggerFactory.getLogger("JF_RapidOfferService");
%>
<%
        String strMode = emxGetParameter(request, "mode");
        if ("del".equalsIgnoreCase(strMode)) {
            String[] tableRowIdList = emxGetParameterValues(request, "emxTableRowId");
            Set<String> selectIdSet = new HashSet<String>();
            for (int i = 0; i < tableRowIdList.length; i++) {
                String strChooseData = tableRowIdList[i];
                StringTokenizer strtk = new StringTokenizer(strChooseData, "|");
                int size = strtk.countTokens();
                String strOId = "";
                if (size == 4) {
                    strtk.nextToken();
                    strOId = strtk.nextToken();
                    strOId = XSSUtil.encodeForJavaScript(context, strOId);
                    selectIdSet.add(strOId);
                } else {
                    strOId = strtk.nextToken();
                    strOId = XSSUtil.encodeForJavaScript(context, strOId);
                    selectIdSet.add(strOId);
                }
            }
            StringList typeSelectList = new StringList();
            typeSelectList.add(SELECT_ID);
            typeSelectList.add(SELECT_NAME);
            typeSelectList.add(SELECT_TYPE);
            MapList infoMapList = DomainObject.getInfo(context,StringList.create(selectIdSet).toStringArray(),typeSelectList);
            Map groupMap = (Map) infoMapList.stream().collect(Collectors.groupingBy(m ->{
                Map info = (Map)m;
                return info.get(SELECT_TYPE);
            }));
            _logger.info("groupMap:{}",groupMap);
            Set delIdSet = new HashSet<String>();
            Set keySet = groupMap.entrySet();
            for (Object oEntry:keySet){
                Map.Entry  entry = (Map.Entry)oEntry;
                String strType = (String) entry.getKey();
                List groupList = (List) entry.getValue();
                String strRelName = "";
                String strTypeName = "";
                boolean isExpand = false;
                int expandLevel = 1;
                //获取选中根节点一下所有对象删除
                if ("JFCostConfig".equals(strType)){
                    strRelName = "JFOffer2JFCostConfig,JFModule2ReferConst,JFCostReferConst2Function";
                    strTypeName =  "JFModules,JFCostReferConst,JFFunctionModule" ;
                    expandLevel = 3;
                    isExpand = true;
                }else if ("JFModules".equals(strType)){
                    strRelName = "JFModule2ReferConst,JFCostReferConst2Function";
                    strTypeName =  "JFCostReferConst,JFFunctionModule" ;
                    expandLevel = 2;
                    isExpand = true;
                }else if ("JFCostReferConst".equals(strType)){
                    strRelName = "JFCostReferConst2Function";
                    strTypeName =  "JFFunctionModule" ;
                    expandLevel = 1;
                    isExpand = true;
                }
                for (int i = 0; i < groupList.size(); i++) {
                    Map info = (Map) groupList.get(i);
                    String  strId = (String) info.get(SELECT_ID);
                    if (!delIdSet.contains(strId)){
                        delIdSet.add(strId);
                        if (isExpand){
                            DomainObject bo = DomainObject.newInstance(context, strId);
                            MapList maps = bo.getRelatedObjects(context, strRelName , // relationship pattern
                                    strTypeName,                                    // object pattern
                                    typeSelectList,                            // object selects
                                    new StringList(SELECT_RELATIONSHIP_ID), // relationship selects
                                    true,                                        // to direction
                                    false,                                        // from direction
                                    (short) expandLevel,                                    // recursion level
                                    "",                // object where clause
                                    "",
                                    (short) 0);

                            for (int i1 = 0; i1 < maps.size(); i1++) {
                                Map tempMap = (Map)maps.get(i1);
                                String strTempId = (String) tempMap.get(SELECT_ID);
                                delIdSet.add(strTempId);
                            }
                        }
                    }
                }
            }
            _logger.info("delIdSet:{}",delIdSet);
            if (delIdSet.size() > 0){
                try {
                    ContextUtil.startTransaction(context,true);
                    DomainObject.deleteObjects(context,StringList.create(delIdSet).toStringArray());
                    ContextUtil.commitTransaction(context);
                } catch (Exception e) {
                    ContextUtil.abortTransaction(context);
                    _logger.error(e.getMessage());
                    throw e;
                }
            }
        }

%>
<script>
    parent.refreshSBTable();
</script>

