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
<%@ page import="static com.matrixone.apps.common.util.JSPUtil.emxGetParameterValues" %>

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
    String rowId = emxGetParameter(request, "rowId");
    StringBuilder sBuff = new StringBuilder();
    try {

        Enumeration<String> paramNames = request.getParameterNames();
        while (paramNames.hasMoreElements()) {
            String paramName = paramNames.nextElement();
            String[] paramValues = request.getParameterValues(paramName);
            System.out.println("key: " + paramName );
            for (String value : paramValues) {
                System.out.println("vaule: " + value );
            }
        }

        String strObjectId = emxGetParameter(request, "objectId");
        String strParentId = emxGetParameter(request, "parentOID");
        String[] tableRowIdList = emxGetParameterValues(request, "emxTableRowId");
        StringList vpmidlist=new StringList();

        for (String tableRowId : tableRowIdList) {
            StringList tableIds = FrameworkUtil.split(tableRowId, "|");
            String vpmId = tableIds.get(0);
            vpmidlist.add(vpmId);
        }
        Map packMap=new HashMap();
        packMap.put("objectid",strObjectId);
        packMap.put("ParentId",strParentId);
        packMap.put("vpmidList",vpmidlist);
//        errorMessage = JPO.invoke(context, "JF_MBOM", null, "CreateMbomPartAndConnect",  JPO.packArgs(packMap), Map.class);
        Map resultMap = JPO.invoke(context, "JF_MBOM", null, "CreateMbomPartAndConnect",  JPO.packArgs(packMap), Map.class);
        errorMessage= (String) resultMap.get("msg");
        StringList idlist= (StringList) resultMap.get("idlist");
        sBuff.append("<mxRoot>");
        for(String mbomid:idlist){
            DomainObject mobmobj=DomainObject.newInstance(context,mbomid);
            String fromid=mobmobj.getInfo(context,"to[JF_relManufacturedItem].from.id");
            String relid=mobmobj.getInfo(context,"to[JF_relManufacturedItem].id");
            sBuff.append("<action><![CDATA[add]]></action>");
            sBuff.append("<data status=\"committed\" ");
            sBuff.append(">");
            sBuff.append("<item oid=\"" + mbomid + "\" relId=\"" + relid + "\" pid=\"" + fromid
                    + "\"  direction=\"" + "from" + "\" />");
            sBuff.append("</data>");
        }
        sBuff.append("</mxRoot>");


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


        var sbifrom=window.parent.parent;
        var  topFrame=sbifrom.openerFindFrame(sbifrom,"detailsDisplay");


        // var eidtTable=sbifrom.openerFindFrame(sbifrom,"detailsDisplay").emxEditableTable;
        // getTopWindow().openerFindFrame(getTopWindow(),"detailsDisplay").emxEditableTable.refreshSelectedRows();
        // eidtTable.refreshSelectedRows();
        // var addNewRowId=eidtTable.getCheckedRows()[0].id
        // var arrRowIds = [];
        // arrRowIds.push(addNewRowId);
        // eidtTable.refreshRowByRowId(arrRowIds);
        // //主动展开选中行，
        // console.log(addNewRowId);
        // // eidtTable.expand(arrRowIds, "1");
        // eidtTable.refreshStructure();
        //

        topFrame.rebuildViewInProcess = true;
        topFrame.syncSBInProcess = true;
        topFrame.emxEditableTable.addToSelected('<%=sBuff.toString()%>');
        topFrame.rebuildViewInProcess = false;
        topFrame.syncSBInProcess = false;
        topFrame.emxEditableTable.refreshStructureWithOutSort();



        console.log("end");

    }else{
        alert('<%=errorMessage%>');
    }
</script>
