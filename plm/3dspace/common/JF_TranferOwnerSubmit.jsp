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
    try {
        String strObjectId = emxGetParameter(request, "objectId");
        String[] tableRowIdList = emxGetParameterValues(request, "emxTableRowId");
        DomainObject color = DomainObject.newInstance(context, strObjectId);
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
        //拿取
        StringList typeSelectList = new StringList(DomainConstants.SELECT_ID);
        StringList relSelectList = new StringList(DomainRelationship.SELECT_ID);
        relSelectList.add(DomainRelationship.SELECT_TO_TYPE);
        MapList maps = color.getRelatedObjects(context, "JFColorMatrix2JFColorGroup,JFColorGroup2JFColorStyle" , // relationship pattern
                "JFColorGroup,JFColorStyle",                                    // object pattern
                typeSelectList,                            // object selects
                relSelectList, // relationship selects
                false,                                        // to direction
                true,                                        // from direction
                (short) 2,                                    // recursion level
                "",                // object where clause
                "",
                (short) 0);
        HashSet tranOwnerList = new HashSet<>();
        HashSet tranConnList = new HashSet<>();
        tranOwnerList.add(strObjectId);
        maps.stream().forEach(m -> {
            Map map = (Map) m;
            tranOwnerList.add(UIUtil.getValue(map, DomainConstants.SELECT_ID));
            String toType = UIUtil.getValue(map, DomainRelationship.SELECT_TO_TYPE);
            if (toType.equalsIgnoreCase("JFColorStyle")) {
                tranConnList.add(UIUtil.getValue(map, DomainRelationship.SELECT_ID));
            }
        });
        System.out.println("tranOwnerList:" +  tranOwnerList);
        System.out.println("tranConnList:" +  tranConnList);
        ContextUtil.startTransaction(context, true);
        //转移责任人
        StringList tranOwners = StringList.create(tranOwnerList);
        StringList tranConnOwners = StringList.create(tranConnList);
        ContextUtil.pushContext(context);
        isPush = true;
        for (String oid: tranOwners) {
            DomainObject object = DomainObject.newInstance(context, oid);
            // 设置owner
            object.setOwner(context, personName);
            // 设置ECO的协作区和组织
            if (UIUtil.isNotNullAndNotEmpty(organization)) {
                object.setPrimaryOwnership(context, "JFSeat", organization);
            }
        }
        for (String rid: tranConnOwners) {
            // 设置owner 组织和协作区；
            if (UIUtil.isNotNullAndNotEmpty(organization)) {
                MqlUtil.mqlCommand(context, "mod connection $1 owner $2 project  $3  organization $4;", rid , personName, "JFSeat", organization);//暂时注释 貌似可以自动增加
            } else {
                MqlUtil.mqlCommand(context, "mod connection $1 owner $2;", rid , personName);//暂时注释 貌似可以自动增加
            }
        }
        ContextUtil.commitTransaction(context);
    }catch (Exception e){
        ContextUtil.abortTransaction(context);
        if (isPush) {
            ContextUtil.popContext(context);
        }
        e.printStackTrace();
    }
%>
<%@include file = "../common/emxNavigatorBottomErrorInclude.inc"%>
<script>
    var refreshURL = getTopWindow().getWindowOpener().location.href;
    getTopWindow().getWindowOpener().location.href = refreshURL;
    getTopWindow().closeWindow();
</script>