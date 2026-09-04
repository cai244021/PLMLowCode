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

<%@include file = "../common/emxNavigatorInclude.inc"%>
<%@include file = "../common/emxNavigatorTopErrorInclude.inc"%>
<%@include file = "../common/enoviaCSRFTokenValidation.inc"%>


<script src="../common/scripts/emxUIModal.js" type="text/javascript"></script>
<script language="JavaScript" src="../common/scripts/emxUICore.js"></script>
<script language="JavaScript" src="../common/scripts/emxUIConstants.js"></script>
<script language="JavaScript" src="../common/scripts/emxUISearch.js"></script>
<script language="JavaScript" src="../common/scripts/emxUIFreezePane.js"></script>
<%
    StringList errorList = new StringList();
    String errorMessage="";
    try {

        String[] tableRowIdList = emxGetParameterValues(request, "emxTableRowId");
        String strObjectId = emxGetParameter(request, "objectId");

        StringList vpmidlist=new StringList();

        for (String tableRowId : tableRowIdList) {

            vpmidlist.add(tableRowId);
        }
        Map packMap=new HashMap();
        packMap.put("vpmidList",vpmidlist);
        packMap.put("objectid",strObjectId);
         errorMessage = JPO.invoke(context, "JF_MBOM", null, "DeleteMbomPart",  JPO.packArgs(packMap), String.class);
        if( UIUtil.isNotNullAndNotEmpty(errorMessage)){
            errorList.add(errorMessage);
        }
    }catch (Exception e){
        e.printStackTrace();

    }
%>
<%@include file = "../common/emxNavigatorBottomErrorInclude.inc"%>
<script>
    let flag = <%=errorList.size()==0%>;
    if(flag) {
        // var sbframe=findFrame(getTopWindow(),"detailsDisplay").emxEditableTable;
        // var checkRows=sbframe.getCheckedRows();
        // var arrRowIds = [];
        // for(var i=0;i<checkRows.length;i++){
        //     var checkid=checkRows[i].id;
        //     var pid=sbframe.getParentRowId(checkid);
        //
        //     if (!arrRowIds.includes(pid)) {
        //         arrRowIds.push(pid);
        //     }
        //
        // }
        // console.log(arrRowIds);
        //
        // sbframe.refreshRowByRowId(arrRowIds);
        // sbframe.expand(arrRowIds, "All");
        // sbframe.refreshStructure();
        var sbframe=findFrame(getTopWindow(),"detailsDisplay");
        sbframe.refreshSBTable()
        console.log("end");
    }else{
        alert('<%=errorMessage%>');
    }
</script>
