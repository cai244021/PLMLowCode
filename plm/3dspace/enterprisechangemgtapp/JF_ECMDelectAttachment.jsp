

<%@ page import="matrix.db.FileList" %>
<%@ page import="com.matrixone.apps.domain.DomainObject" %>
<%@ page import="com.matrixone.apps.domain.util.MqlUtil" %>

<%@ page import="com.matrixone.apps.domain.util.ContextUtil" %>
<%@ page import="com.matrixone.apps.domain.DomainRelationship" %>
<%@ page import="org.json.JSONObject" %>
<%@ page import="matrix.util.StringList" %>
<%@ page import="com.matrixone.apps.framework.ui.UIUtil" %>

<%@include file = "../common/emxNavigatorTopErrorInclude.inc"%>
<%@include file = "ECMDesignTopInclude.inc"%>
<%@include file = "../common/enoviaCSRFTokenValidation.inc"%>


<jsp:useBean id="indentedTableBean" class="com.matrixone.apps.framework.ui.UITableIndented" scope="session"/>
<jsp:useBean id="formBean" scope="session" class="com.matrixone.apps.common.util.FormBean"/>

<%
    //Context contextDB = (matrix.db.Context)request.getAttribute("context");
    JSONObject josn =new JSONObject();
    try {
        String objectId        = emxGetParameter(request, "objectId");
        String docid        = emxGetParameter(request, "docid");
        System.out.println("objectId---->"+objectId);
        System.out.println("docid---->"+docid);
        ContextUtil.pushContext(context);
        DomainObject DocEcr= DomainObject.newInstance(context,objectId);
        FileList allFile= DocEcr.getFiles(context);

        DomainObject docobj=DomainObject.newInstance(context,docid);
        String doctitle=docobj.getAttributeValue(context,"Title");
        System.out.println("docid-->"+docid);
        System.out.println("doctitle-->"+doctitle);
        if(!allFile.isEmpty()){
            for (int i = 0; i < allFile.size(); i++) {
                matrix.db.File tempFile=allFile.get(i);
                String strFileName=tempFile.getName();
                System.out.println("strFileName strFileName >>>>>>>>>>strFileName"+strFileName);
                if(strFileName.equals(doctitle)){
                    //DocEcr.deleteFile(context,strFileName,"generic");
                    String var4 = "delete bus $1 format $2 file $3";
                    //MqlUtil.mqlCommand(context, var4, objectId, strFileName, var2});
                    MqlUtil.mqlCommand(context, var4, new String[]{objectId,"generic",strFileName});
                }
            }
            if(UIUtil.isNotNullAndNotEmpty(docid)){
//                DomainObject docobj=DomainObject.newInstance(context,docid);

                String relid=docobj.getInfo(context,"to[Active Version].id");
                String rel2id=docobj.getInfo(context,"to[Latest Version].id");

                DomainRelationship.disconnect(context,relid);
                DomainRelationship.disconnect(context,rel2id);
                DomainObject.deleteObjects(context,new String[]{docid});
            }

            josn.put("state","0");
        }else{
            josn.put("state","1");
            josn.put("error","\u6ca1\u6709\u9644\u4ef6\uff01");
        }


    } catch (Exception e) {
        e.printStackTrace();
        josn.put("state","1");
        josn.put("error",e.getMessage());
    }finally {
        ContextUtil.popContext(context);
    }
    out.clear();
    out.write( josn.toString());
%>