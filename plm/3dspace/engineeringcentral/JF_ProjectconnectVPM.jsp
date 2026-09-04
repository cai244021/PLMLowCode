<%@ page import="java.util.Map" %>
<%@ page import="com.matrixone.apps.domain.DomainObject" %>
<%@ page import="matrix.db.RelationshipType" %>
<%@ page import="matrix.util.StringList" %>
<%@ page import="com.matrixone.apps.domain.DomainRelationship" %>
<%@ page import="com.matrixone.apps.framework.ui.UIUtil" %>
<%@ page import="org.slf4j.Logger" %>
<%@ page import="org.slf4j.LoggerFactory" %>
<%@ page import="com.matrixone.apps.domain.util.*" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="matrix.db.JPO" %>
<%@ page import="com.matrixone.apps.domain.DomainConstants" %>
<!--
DR添加物理产品
-->
<%@include file = "../common/emxNavigatorInclude.inc"%>
<%@include file = "../common/emxNavigatorTopErrorInclude.inc"%>
<%@include file = "../common/enoviaCSRFTokenValidation.inc"%>
<%!
    private static final Logger log = LoggerFactory.getLogger("JF_ProjectconnectVPM.jsp");
%>
<%
    StringList errorList = new StringList();
    String errorMessage="";
    boolean flag = false;
    try {
        String strObjectId = emxGetParameter(request, "objectId");
        String[] tableRowIdList = emxGetParameterValues(request, "emxTableRowId");
        DomainObject project = DomainObject.newInstance(context, strObjectId);
        StringList partList = project.getInfoList(context,"from[JFProject2RootPart].to.id");
        StringList zeroPartIds = new StringList();
        HttpServletRequest req = (HttpServletRequest)pageContext.getRequest();
        Map requestMap = new HashMap();
        requestMap.put("objectId",strObjectId);


        //供货件清单
        MapList zeroList = (MapList) JPO.invoke(context, "JF_VPMReferenceEBOM", new String[0], "getZeroALLPart", JPO.packArgs(requestMap), MapList.class);
        Map map = null;
        for(int i = 0;i<zeroList.size();i++){
            map = (Map)zeroList.get(i);
            String partId = UIUtil.getValue(map, DomainConstants.SELECT_ID);
            zeroPartIds.add(partId);
        }
        log.info("zeroPartIds:{}",zeroPartIds);
        for (String tableRowId : tableRowIdList) {
            StringList tableIds = FrameworkUtil.split(tableRowId, "|");
            String vpmId = tableIds.get(0);
            DomainObject vpmObj = DomainObject.newInstance(context, vpmId);
            //todo 添加的零件需要先有所属项目
            String BelongStr =  JPO.invoke(context, "JF_VPMReferenceEBOM", new String[0], "getPartBelongProject", new String[]{vpmId,"id"}, String.class);
            log.info("BelongStr:{}",BelongStr);
            String name = vpmObj.getInfo(context, "attribute[EnterpriseExtension.V_PartNumber]");
                    if (zeroPartIds.contains(vpmId)) {//当前项目已经添加过的零件不允许添加
                        if (UIUtil.isNullOrEmpty(name)) {
                            name = vpmObj.getName();
                        }
                        if(!errorList.contains(name)) {
                            errorList.add(name);
                        }
                    }

                 if (UIUtil.isNullOrEmpty(BelongStr)) {//该零件还没有所属项目，需要先添加到所属项目中才可以添加供货件
                if (UIUtil.isNullOrEmpty(name)) {
                    name = vpmObj.getName();
                }
                     if(!errorList.contains(name)) {
                         errorList.add(name);
                     }
            }
            }
        String msg = EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", context.getLocale(), "emxComponents.wholeChair.connect");
        errorMessage = errorList.toString()+""+msg;
        log.info("errorList:{}",errorList);
        if (errorList.size() == 0){
            ContextUtil.pushContext(context);
            flag = true;
            Map requestMap2 = new HashMap();
            requestMap2.put("projectId",strObjectId);
            for (String tableRowId : tableRowIdList) {
                StringList tableIds = FrameworkUtil.split(tableRowId, "|");
                String vpmId = tableIds.get(0);
                requestMap2.put("partId",vpmId);
                JPO.invoke(context, "JF_VPMReferenceEBOM", new String[0], "JFProject2RootPartConnect", JPO.packArgs(requestMap2), MapList.class);

            }
            project.setAttributeValue(context, "JF_SupplyAffirm", "N");//项目供货件是否确认设置为否
    }
    }catch (Exception e){
        e.printStackTrace();

    }finally {
        if(flag){
            ContextUtil.popContext(context);
        }
    }
%>
<%@include file = "../common/emxNavigatorBottomErrorInclude.inc"%>
<script>
    let flag = <%=errorList.size()==0%>;
    if(flag) {
        var refreshURL = getTopWindow().getWindowOpener().location.href;
        getTopWindow().getWindowOpener().location.href = refreshURL;
        getTopWindow().closeWindow();
    }else{
        alert(`<%=errorMessage%>`);
    }
</script>
