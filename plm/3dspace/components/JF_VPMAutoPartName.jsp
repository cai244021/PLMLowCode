<%@ page import="matrix.util.StringList" %>
<%@ page import="com.matrixone.apps.domain.DomainConstants" %>
<%@ page import="com.matrixone.apps.domain.DomainRelationship" %>
<%@ page import="com.matrixone.apps.domain.util.MapList" %>
<%@ page import="com.matrixone.apps.domain.DomainObject" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="matrix.db.JPO" %>
<%@ page import="com.matrixone.apps.domain.Job" %>
<%@ page import="com.matrixone.apps.framework.ui.UIUtil" %>
<%@ page import="com.matrixone.apps.domain.util.EnoviaResourceBundle" %>
<!--
dr移除关联的物理产品
-->
<%@include file = "../common/emxNavigatorInclude.inc"%>
<%@include file = "../common/emxNavigatorTopErrorInclude.inc"%>
<%@include file = "../common/enoviaCSRFTokenValidation.inc"%>
<%
    StringBuffer msg = new StringBuffer();
    try {
        String RELATIONSHIP_JFDR2VPMREFERENCE = "JFDR2VPMReference";
        String TYPE_VPMREFERENCE = "VPMReference";
        String ATTRIBUTE_PARTTYPE = "JF_VPMReference.JF_PartType";
        String ATTRIBUTE_PARTNUMBER ="EnterpriseExtension.V_PartNumber";
        String strObjectId = emxGetParameter(request, "objectId");
        String[] tableRowIdList = emxGetParameterValues(request, "emxTableRowId");
        Map<String,StringList> partTyep2partId = new HashMap<String,StringList>();
        StringList disconnectVPMId = new StringList();
        StringList updatePartNumberIds = new StringList();
        //把需要修改企业编码的零件安装零件类型分类
        for(String tableRowId : tableRowIdList) {
            System.out.println("JF_VPMAutoPartName.jsp>>>>>>>>>>>>>"+tableRowId+">>>>>>>>>>>>>");
            StringList tableIds = FrameworkUtil.split(tableRowId,"|");
            String tableId = tableIds.get(1);
            if(UIUtil.isNullOrEmpty(tableId)){
                tableId = tableIds.get(0);
            }
            DomainObject part = DomainObject.newInstance(context,tableId);
            String name = part.getInfo(context,"name");
            String number = part.getAttributeValue(context,ATTRIBUTE_PARTNUMBER);
            if(UIUtil.isNotNullAndNotEmpty(number)||updatePartNumberIds.contains(tableId)){
                continue;
            }
            updatePartNumberIds.add(tableId);
            String partType = part.getAttributeValue(context,ATTRIBUTE_PARTTYPE);
            if(UIUtil.isNotNullAndNotEmpty(partType)){
                StringList ids = partTyep2partId.getOrDefault(partType,new StringList());
                ids.add(tableId);
                partTyep2partId.put(partType,ids);
            }else{
                msg.append(",").append(name);
            }

        }
        if(msg.length()>0){
            String msgValue = EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", context.getLocale(), "emxComponents.autoName.noPartType");
            msg.append(msgValue);
        }
        //按照零件类型分类获取企业编码，
        for(Map.Entry<String,StringList> entry : partTyep2partId.entrySet()) {
            Map<String,Object> paramMap = new HashMap<>();
            paramMap.put("objectIds",entry.getValue());
            paramMap.put("type",entry.getKey());
//            Job job = new Job("JF_VPMReference","setPartNumber",JPO.packArgs(paramMap));
//            job.setTitle("VPMReference get PartNumber");
//            job.createAndSubmit(context);
            JPO.invoke(context,"JF_VPMReferenceEBOM",new String[]{},"setPartNumber",JPO.packArgs(paramMap),void.class);
        }
    }catch (Exception e){
        e.printStackTrace();
    }
%>
<%@include file = "../common/emxNavigatorBottomErrorInclude.inc"%>
<script>
    var msg = "<%=msg.toString()%>"
    if(msg.length>0){
        alert(msg);
    }
    var refreshURL = window.parent.location.href;
    window.parent.location.href = refreshURL;
    console.log('refreshURL='+refreshURL);
</script>