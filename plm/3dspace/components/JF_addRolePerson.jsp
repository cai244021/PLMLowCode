<%@ page import="matrix.util.StringList" %>
<%@ page import="com.matrixone.apps.domain.DomainConstants" %>
<%@ page import="com.matrixone.apps.domain.DomainRelationship" %>
<%@ page import="com.matrixone.apps.domain.util.MapList" %>
<%@ page import="com.matrixone.apps.domain.util.FrameworkUtil" %>
<%@ page import="com.matrixone.apps.framework.ui.UIUtil" %>
<%@ page import="com.matrixone.apps.domain.DomainObject" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="com.matrixone.apps.domain.util.EnoviaResourceBundle" %>
<%@ page import="matrix.db.JPO" %>
<!--
根据角色给任务关联人员
-->
<%@include file = "../common/emxNavigatorInclude.inc"%>
<%@include file = "../common/emxNavigatorTopErrorInclude.inc"%>
<%@include file = "../common/enoviaCSRFTokenValidation.inc"%>
<%
    try {
        String RELATIONSHIP_JFDR2VPMREFERENCE = "JFDR2VPMReference";
        String TYPE_VPMREFERENCE = "VPMReference";
        String strObjectId = emxGetParameter(request, "objectId");
        String[] tableRowIdList = emxGetParameterValues(request, "emxTableRowId");
        DomainObject project = DomainObject.newInstance(context, strObjectId);
        StringList relSel = new StringList();
        relSel.add("attribute[Project Role]");
        StringList boSel = new StringList();;
        boSel.add(DomainConstants.SELECT_ID);
        MapList personMapList = project.getRelatedObjects(context,DomainConstants.RELATIONSHIP_MEMBER,DomainConstants.TYPE_PERSON,boSel,relSel,
                false,true,(short)1,DomainConstants.EMPTY_STRING,DomainConstants.EMPTY_STRING,0);
        Map<String,StringList> taskId2personIds = new HashMap<>();
        Map<String,StringList> role2personMap = new HashMap<String,StringList>();
        for (int i = 0; i < personMapList.size(); i++) {
            Map personMap = (Map)personMapList.get(i);
            String role = (String)personMap.get("attribute[Project Role]");
            String personId = (String)personMap.get(DomainConstants.SELECT_ID);
            StringList role2personIds = role2personMap.getOrDefault(role,new StringList());
            role2personIds.add(personId);
            role2personMap.put(role,role2personIds);
        }
        StringList rangeList = FrameworkUtil.getRanges(context,"Project Role");
        Map<String,String> rangeMap = new HashMap<>();
        for(String range:rangeList){
            if(UIUtil.isNotNullAndNotEmpty(range)){
                rangeMap.put(range,EnoviaResourceBundle.getRangeI18NString(context, "Project Role", range, context.getLocale().getLanguage()));
            }
        }
        String taskErrorMsg = EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", context.getLocale(), "emxComponents.taskErrorMsg");
        String personErrorMsg = EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", context.getLocale(), "emxComponents.personErrorMsg");

        StringBuffer taskError = new StringBuffer();
        StringBuffer personError = new StringBuffer();
        String rangeValue = "";
        for (String tableRowId : tableRowIdList) {
            StringList temp = FrameworkUtil.split(tableRowId,"|");
            String taskId = temp.get(1);
            if(UIUtil.isNotNullAndNotEmpty(taskId)){
                DomainObject task = DomainObject.newInstance(context,taskId);
                String taskName = task.getInfo(context,"name");
                String projectRole = task.getAttributeValue(context,"Project Role");
                if(UIUtil.isNullOrEmpty(projectRole)){
                    //任务未明确角色
                    taskError.append(taskErrorMsg.replace("$1",taskName)).append("\\n");
                }else if(!role2personMap.containsKey(projectRole)){
                    rangeValue = rangeMap.get(projectRole);
                    //项目未关联对应角色的人员
                    personError.append(personErrorMsg.replace("$1",taskName).replace("$2",rangeValue)).append("\\n");
                }else{
                    //找到角色对应人员
                    taskId2personIds.put(taskId,role2personMap.get(projectRole));
                }
            }
        }
        if(taskError.length()<=0&&personError.length()<=0){
            //没有报错，关联人员
            JPO.invoke(context,"JF_ProjectSpace",null,"connectRolePerson",JPO.packArgs(taskId2personIds),void.class);
            %>
<%@include file = "../common/emxNavigatorBottomErrorInclude.inc"%>
<script>
    var refreshURL = window.parent.location.href;
    window.parent.location.href = refreshURL;
</script>
<%
        }else{
%>
<script>
    var msg = "<%=taskError.toString()+personError.toString()%>";
    alert(msg);
</script>
<%
        }
    }catch (Exception e){
        e.printStackTrace();
    }
%>
