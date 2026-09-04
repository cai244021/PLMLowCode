<%@ page import="com.matrixone.apps.program.ProgramCentralUtil" %>
<%@ page import="java.util.Map" %>
<%@ page import="com.matrixone.apps.domain.util.PersonUtil" %>
<%@ page import="com.matrixone.apps.domain.DomainObject" %>
<%@ page import="com.matrixone.apps.domain.DomainConstants" %>
<%@ page import="com.matrixone.apps.domain.util.MapList" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="com.google.gson.Gson" %>
<%@ page import="matrix.util.StringList" %>
<%@include file = "emxNavigatorInclude.inc"%>
<%
    MapList res = new MapList();
    boolean isPerson = false;
    try {
        String emxTableRowId[] = emxGetParameterValues(request, "emxTableRowId");
        String strInputFieldHidden = emxGetParameter(request,"inputFieldHidden");
        System.out.println("#####################################################");
        System.out.println("#####################################################");
        System.out.println("strInputFieldHidden:"+strInputFieldHidden);
        System.out.println("#####################################################");
        if (null != emxTableRowId && emxTableRowId.length > 0){
            for (int i = 0; i < emxTableRowId.length; i++) {
                Map personMap = new HashMap<>();
                Map rowMap = ProgramCentralUtil.parseTableRowId(context,emxTableRowId[i]);
                String strPersonId = (String) rowMap.get("objectId");
                DomainObject person = DomainObject.newInstance(context,strPersonId);
                StringList selectList = new StringList();
                selectList.add(DomainConstants.SELECT_NAME);
                selectList.add(DomainConstants.SELECT_TYPE);
                Map infoMap = person.getInfo(context, selectList);
                String strObjType = (String) infoMap.get(DomainConstants.SELECT_TYPE);
                String strObjName = (String) infoMap.get(DomainConstants.SELECT_NAME);
                if (DomainConstants.TYPE_PERSON.equals(strObjType)){
                    isPerson = true;
                    String strFullName = PersonUtil.getFullName(context, strObjName);
                    personMap.put("fullName",strFullName);
                }else {
                    personMap.put("name",strObjName);
                }
                personMap.put("id",strPersonId);
                res.add(personMap);
            }
        }
        //需要关闭搜索框
        if ("JFSTDPerson".equals(strInputFieldHidden)){
            isPerson = false;
        }
    }catch (Exception e){
       throw  e;
    }

    Gson gson = new Gson();
    String strRes = gson.toJson(res);
%>
<script>
    //请求成功 往请求页面发送新增数据
    const channel = new BroadcastChannel("JF_ECR_EDIT_DEV_NOTICE");
    channel.postMessage(`<%=strRes%>`);
    if (!<%=isPerson%>){
      parent.close();
    }
</script>