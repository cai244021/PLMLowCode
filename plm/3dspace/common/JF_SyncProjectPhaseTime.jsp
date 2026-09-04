<%@ page import="org.slf4j.LoggerFactory" %>
<%@ page import="org.slf4j.Logger" %>
<%@ page import="com.matrixone.apps.domain.util.ContextUtil" %>
<%@ page import="com.matrixone.apps.domain.DomainObject" %>
<%@ page import="matrix.util.StringList" %>
<%@ page import="com.matrixone.apps.domain.util.MapList" %>
<%@ page import="java.util.Map" %>
<%@ page import="com.matrixone.apps.framework.ui.UIUtil" %>
<%@ page import="java.util.HashMap" %>
<%@ page pageEncoding="utf-8" %>
<%@include file = "emxNavigatorInclude.inc"%>
<script src="./scripts/emxUIModal.js" type="text/javascript"></script>
<script language="JavaScript" src="./scripts/emxUICore.js"></script>
<script language="JavaScript" src="./scripts/emxUIConstants.js"></script>
<script language="JavaScript" src="./scripts/emxUISearch.js"></script>
<script type="text/javascript" src="../common/scripts/jquery-latest.js"></script>


<%!
    private static final Logger JF_LOGGER = LoggerFactory.getLogger("JF_SyncProjectPhaseTime.jsp");
%>
<%
    String strObjectId = (String)emxGetParameter(request, "objectId");
    Map setAttrMap = new HashMap();
    String mess = "";
    try {
        ContextUtil.pushContext(context);
        DomainObject projectObj = DomainObject.newInstance(context,strObjectId);
        MapList allPhase = projectObj.getRelatedObjects(context,"Subtask","Phase",StringList.create("type","id","name","attribute[Task Estimated Finish Date]"),new StringList(),
                false,true,(short)1,"","",0);
        for (Object o : allPhase) {
            Map map = (Map) o;
            String name = UIUtil.getValue(map,"name");
            String finishDate = UIUtil.getValue(map,"attribute[Task Estimated Finish Date]");
            if (name.startsWith("P-1")){
                setAttrMap.put("JF_Phase1PlannedCompletionTime",finishDate);
            } else if (name.startsWith("P-2+3")){
                setAttrMap.put("JF_Phase2_3PlannedCompletionTime",finishDate);
            }else if (name.startsWith("P-2")){
                setAttrMap.put("JF_Phase2PlannedCompletionTime",finishDate);
            }else if (name.startsWith("P-3")){
                setAttrMap.put("JF_Phase3PlannedCompletionTime",finishDate);
            }else if (name.startsWith("P-4")){
                setAttrMap.put("JF_Phase4PlannedCompletionTime",finishDate);
            }else if (name.startsWith("P-5")){
                setAttrMap.put("JF_Phase5PlannedCompletionTime",finishDate);
            }
        }
        JF_LOGGER.info("----JF_SyncProjectPhaseTime.jsp--setAttrMap--"+setAttrMap);
        projectObj.setAttributeValues(context,setAttrMap);
    }catch (Exception e){
        JF_LOGGER.error("--JF_SyncProjectPhaseTime.jsp--error",e);
        mess = e.getMessage();
    }finally {
        ContextUtil.popContext(context);
    }
%>
<html>
<script>

    var mess = "<%=mess%>";
    if (mess != ""){
        alert(mess);
    }else {
        alert("阶段时间同步成功");
    }
    getTopWindow().openerFindFrame(getTopWindow(),"detailsDisplay").location.reload()
</script>
</html>