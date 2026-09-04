<%@ page import="java.util.Map" %>
<%@ page import="com.matrixone.apps.domain.DomainObject" %>
<%@ page import="matrix.db.RelationshipType" %>
<%@ page import="com.matrixone.apps.domain.util.FrameworkUtil" %>
<%@ page import="matrix.util.StringList" %>
<%@ page import="com.matrixone.apps.domain.DomainRelationship" %>
<%@ page import="com.matrixone.apps.domain.util.MqlUtil" %>
<%@ page import="com.matrixone.apps.domain.util.ContextUtil" %>
<%@ page import="com.matrixone.apps.framework.ui.UIUtil" %>
<%@ page import="com.matrixone.apps.domain.util.EnoviaResourceBundle" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="matrix.db.JPO" %>
<%@ page import="java.util.Enumeration" %>

<%@include file = "../common/emxNavigatorInclude.inc"%>
<%@include file = "../common/emxNavigatorTopErrorInclude.inc"%>
<%@include file = "../common/enoviaCSRFTokenValidation.inc"%>

<script src="../common/scripts/emxUIModal.js" type="text/javascript"></script>
<script language="JavaScript" src="../common/scripts/emxUICore.js"></script>
<script language="JavaScript" src="../common/scripts/emxUIConstants.js"></script>
<script language="JavaScript" src="../common/scripts/emxUISearch.js"></script>
<script language="JavaScript" src="../common/scripts/emxUIFreezePane.js"></script>
<%
    StringList errorList=new StringList();
    String errorMessage="";
    try {

        String[] tableRowIdList = emxGetParameterValues(request, "emxTableRowId");
        StringList vpmidlist=new StringList();

        for (String tableRowId : tableRowIdList) {
            StringList tableIds = FrameworkUtil.split(tableRowId, "|");
            String vpmId = tableIds.get(1);
            vpmidlist.add(vpmId);
            System.out.println("tableIds");
            System.out.println(tableIds);
        }
        Map packMap=new HashMap();
        packMap.put("mbomid",vpmidlist.get(0));
        Map resultmap = JPO.invoke(context, "JF_MBOM", null, "updateMbomPart",  JPO.packArgs(packMap), Map.class);
        if(!resultmap.containsKey("code")){
            errorList.add("1");
        }
        errorMessage= (String) resultmap.get("msg");

    }catch (Exception e){
        e.printStackTrace();

    }
%>
<%@include file = "../common/emxNavigatorBottomErrorInclude.inc"%>
<script>
    let flag = <%=errorList.size()==0%>;
    if(flag){
        alert('<%=errorMessage%>');
        var sbframe=findFrame(getTopWindow(),"detailsDisplay").emxEditableTable;
        var checkRows=sbframe.getCheckedRows();
        var arrRowIds = [];
        for(var i=0;i<checkRows.length;i++){
            var checkid=checkRows[i].id;
            var pid=sbframe.getParentRowId(checkid);

            if (!arrRowIds.includes(pid)) {
                arrRowIds.push(pid);
            }

        }
        console.log(arrRowIds);

        // sbframe.refreshRowByRowId(arrRowIds);
        // sbframe.expand(arrRowIds, "All");
        // sbframe.refreshStructure();

        console.log("end");
        // var iframe=findFrame(getTopWindow(),"detailsDisplay")
        // iframe.location.href = iframe.location.href;

        parent.location.href = parent.location.href;
    }else {
        alert('<%=errorMessage%>');
    }


</script>
