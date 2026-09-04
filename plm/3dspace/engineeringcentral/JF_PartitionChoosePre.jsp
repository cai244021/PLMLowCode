<%@ page import="com.matrixone.apps.domain.util.FrameworkUtil" %>
<%@ page import="matrix.util.StringList" %>
<%@ page import="matrix.util.StringList" %>
<%@ page import="com.matrixone.apps.domain.DomainConstants" %>
<%@ page import="com.matrixone.apps.domain.util.MapList" %>
<%@ page import="com.matrixone.apps.domain.DomainObject" %>
<%@ page import="java.util.Map" %>
<%@ page import="com.matrixone.apps.framework.ui.UIUtil" %>
<%@ page import="com.matrixone.apps.domain.util.EnoviaResourceBundle" %>
<%@ page import="java.util.HashSet" %>
<%@ page import="java.util.Set" %>
<%@include file = "../common/emxNavigatorInclude.inc"%>
<%@include file = "../common/emxNavigatorTopErrorInclude.inc"%>
<%
    String[] tableRowId = emxGetParameterValues(request, "emxTableRowId");
    StringBuffer objectId = new StringBuffer();
    String[] selectIds= new String[tableRowId.length];
    for (int i = 0; i < tableRowId.length; i++) {
        String tableId = tableRowId[i];
        if(tableId.startsWith("|")){
            selectIds[i]=FrameworkUtil.split(tableId,"|").get(0);
            objectId.append(FrameworkUtil.split(tableId,"|").get(0)).append(",");
        }else{
            selectIds[i]=FrameworkUtil.split(tableId,"|").get(1);
            objectId.append(FrameworkUtil.split(tableId,"|").get(1)).append(",");
        }
    }
    //只有工作中的数据可以修改零件中英文
    StringList boSel = new StringList();
    boSel.add(DomainConstants.SELECT_ID);
    boSel.add(DomainConstants.SELECT_CURRENT);
    boSel.add(DomainConstants.SELECT_OWNER);
    boSel.add("attribute[PLMEntity.V_usage]");
    boSel.add("attribute[JF_VPMReference.JF_PartType]");
    boSel.add("attribute[EnterpriseExtension.V_PartNumber]");
//    StringList objectLists = FrameworkUtil.split(objectId.toString(), ",");
    MapList results = DomainObject.getInfo(context, selectIds, boSel);
    Map temp =null;
    String current = "";
    boolean flag = true;
    HashSet<String> v_UsageSet = new HashSet<>();
    String strLoginUser = context.getUser();
    String strMess = "";
    String errorNotice = EnoviaResourceBundle.getProperty(context, "emxProgramCentralStringResource",context.getLocale(), "emxProgramCentral.Command.PartitionNotice");
    String strErrorPartTypeMess = EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource",context.getLocale(), "emxComponents.Mess.PartTypeNotUnique");
    Set<String> partTypeSet =  new HashSet<>();
    for(int i=0;i<results.size();i++){
        temp = (Map)results.get(i);
        current = UIUtil.getValue(temp, DomainConstants.SELECT_CURRENT);
        String strPartNum = UIUtil.getValue(temp, "attribute[EnterpriseExtension.V_PartNumber]");
        String strOwner = UIUtil.getValue(temp, DomainConstants.SELECT_OWNER);
        //标识零件类型
        String strPartType = UIUtil.getValue(temp, "attribute[JF_VPMReference.JF_PartType]");
//        if (UIUtil.isNotNullAndNotEmpty(strPartNum)){
//            flag  = false ;
//            strMess = strErrorPartNumMess.replace("{}",strPartNum);
//            break;
//        }else {
//            strPartNum = UIUtil.getValue(temp, DomainConstants.SELECT_NAME);
//        }
        partTypeSet.add(strPartType);
        if (partTypeSet.size() > 1){
            flag = false;
            strMess = strErrorPartTypeMess.replace("{}",strPartNum);
            break;
        }

        if((!"IN_WORK".equalsIgnoreCase(current)) || (!strLoginUser.equals(strOwner))){
            flag = false;
            strMess = errorNotice.replace("{}",strPartNum);
            break;
        }
    }
    String strV_PartType =  "" ;
    for (String strPartType : partTypeSet) {
        strV_PartType = strPartType;
        break;
    }

%>
<script>
    var flag = <%=flag%>;
    if(flag) {
        var objectId = "<%=objectId.toString()%>"
        console.log(objectId);
        var url = "../common/emxIndentedTable.jsp?table=AEFGeneralSearchResults&program=JF_Library:getPartition&typeName=" + "<%=strV_PartType%>" + "&expandProgram=JF_Library:getChild&submitURL=../engineeringcentral/JF_PartitionChooser.jsp&selection=single&selectId=" + objectId + "&typeName=" + "<%=strV_PartType%>";
        console.log(url);
        window.location.href = url;
    }else{
        alert('<%=strMess%>');
        window.close();
    }
</script>