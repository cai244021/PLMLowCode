<%@ page import="com.matrixone.apps.domain.DomainObject" %>
<%@ page import="matrix.db.RelationshipType" %>
<%@ page import="com.matrixone.apps.domain.util.FrameworkUtil" %>
<%@ page import="matrix.util.StringList" %>
<%@ page import="com.matrixone.apps.framework.ui.UIUtil" %>
<%@ page import="com.matrixone.apps.domain.DomainConstants" %>
<%@ page import="static com.matrixone.apps.common.util.JSPUtil.emxGetParameterValues" %>
<%@ page import="com.matrixone.apps.domain.util.MapList" %>
<%@ page import="java.util.HashSet" %>
<%@ page import="java.util.Map" %>
<%@ page import="com.matrixone.apps.domain.util.MqlUtil" %>
<%@ page import="com.matrixone.apps.domain.util.ContextUtil" %>
<%@ page import="com.matrixone.apps.domain.DomainRelationship" %>
<!--
CE添加ECR
-->
<%@include file = "../common/emxNavigatorInclude.inc"%>
<%@include file = "../common/emxNavigatorTopErrorInclude.inc"%>
<%@include file = "../common/enoviaCSRFTokenValidation.inc"%>
<%
    Boolean isPush = Boolean.FALSE;
    String mess = EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", context.getLocale(), "emxComponents.TransferOwner.Successful");
    try {
        String selectIds = emxGetParameter(request, "selectIds");
        System.out.println("selectIds:" +  selectIds);
        StringList partList = StringList.create(selectIds.split(","));
        String[] tableRowIdList = emxGetParameterValues(request, "emxTableRowId");
        String tableRowId = tableRowIdList[0];
        StringList tableIds = FrameworkUtil.split(tableRowId,"|");
        String newOwnerId = tableIds.get(0);
        DomainObject domainObject = DomainObject.newInstance(context, newOwnerId);
        String personName = domainObject.getInfo(context, DomainConstants.SELECT_NAME);
        //当人员都没有部门和业务单位的时候 获取他的property
        String mql = "print Person $1 select $2 dump $3;";
        String result = MqlUtil.mqlCommand(context, true, false, mql, true, personName, "assignment", "@");
        String[] split = result.split("@");
        String organization = DomainConstants.EMPTY_STRING;
        for (int j = 0; j < split.length; j++) {
            String assignment = split[j];
            if (assignment.contains("ctx::VPLMCreator") || assignment.contains("ctx::VPLMProjectLeader")) {
                String[] split1 = assignment.split("\\.");
                organization = split1[1];
                break;
            }
        }
        String project = domainObject.getInfo(context, "project");
        //拿取
        StringList typeSelectList = new StringList(DomainConstants.SELECT_ID);
        StringList relSelectList = new StringList(DomainRelationship.SELECT_ID);
        DomainObject partObject = DomainObject.newInstance(context);
        HashSet tranOwnerList = new HashSet<>();
        HashSet tranConnList = new HashSet<>();
        for (int i = 0; i < partList.size(); i++) {
            partObject.setId(partList.get(i));
            //查询成本分析/ 成本分析项
            MapList maps = partObject.getRelatedObjects(context, "JFVPMReference2CostAnalysis,JFCostAnalysis2CostItem", // relationship pattern
                    "JFCostAnalysis,JFCostAnalysisItem",                                    // object pattern
                    typeSelectList,                            // object selects
                    relSelectList, // relationship selects
                    false,                                        // to direction
                    true,                                        // from direction
                    (short) 2,                                    // recursion level
                    "",                // object where clause
                    "",
                    (short) 0);
            for (int i1 = 0; i1 < maps.size(); i1++) {
                Map map = (Map) maps.get(i1);
                String id = UIUtil.getValue(map, DomainConstants.SELECT_ID);
                String rid = UIUtil.getValue(map, DomainRelationship.SELECT_ID);
                tranOwnerList.add(id);
                tranConnList.add(rid);
            }
        }
        System.out.println("tranOwnerList:" +  tranOwnerList);
        System.out.println("tranConnList:" +  tranConnList);
        ContextUtil.startTransaction(context, true);
        //转移责任人
        StringList tranOwners = StringList.create(tranOwnerList);
        StringList tranConnOwners = StringList.create(tranConnList);
        String  mql1 = "mod bus $1 owner $2 organization $3 project $4;";
        String  mql2 = "mod connection $1 owner $2 organization $3 project $4;";
        ContextUtil.pushContext(context);
        isPush = true;
        for (String oid: tranOwners) {
            MqlUtil.mqlCommand(context,false,false, mql1,true, oid, personName, organization, project);
        }
        for (String rid: tranConnOwners) {
            MqlUtil.mqlCommand(context,false,false, mql2,true, rid, personName, organization, project);
        }
        ContextUtil.commitTransaction(context);
    }catch (Exception e){
        ContextUtil.abortTransaction(context);
        if (isPush) {
            ContextUtil.popContext(context);
        }
        mess = EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", context.getLocale(), "emxComponents.TransferOwner.Failed");
        e.printStackTrace();
    }
%>
<%@include file = "../common/emxNavigatorBottomErrorInclude.inc"%>
<script>
    alert("<%=mess%>");
    var refreshURL = getTopWindow().getWindowOpener().location.href;
    getTopWindow().getWindowOpener().location.href = refreshURL;
    getTopWindow().closeWindow();
</script>