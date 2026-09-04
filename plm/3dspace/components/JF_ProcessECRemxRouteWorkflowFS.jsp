<%@ page import="com.matrixone.apps.domain.DomainObject" %>
<%@ page import="com.matrixone.apps.framework.ui.UIUtil" %>
<%@ page import="org.slf4j.Logger" %>
<%@ page import="org.slf4j.LoggerFactory" %>
<%@ page import="matrix.util.StringList" %><%-- emxRouteWorkflowFS.jsp --
  Copyright (c) 1992-2020 Dassault Systemes.
  All Rights Reserved.
  This program contains proprietary and trade secret information of MatrixOne, Inc.
  Copyright notice is precautionary only and does not evidence any actual or intended publication of such program

  static const char RCSID[] = $Id: emxRouteWorkflowFS.jsp.rca 1.7 Wed Oct 22 16:18:08 2008 przemek Experimental przemek $
--%>

<%@include file  = "../emxUIFramesetUtil.inc"%>
<%@include file = "emxRouteInclude.inc"%>
<%!
  private static final Logger JF_LOGGER = LoggerFactory.getLogger("JF_ProcessECRemxRouteWorkflowFS.jsp");
%>
<%
  framesetObject fs = new framesetObject();

  String initSource = emxGetParameter(request,"initSource");
  if (initSource == null){
    initSource = "";
  }

  String jsTreeID  = emxGetParameter(request,"jsTreeID");
  String suiteKey  = emxGetParameter(request,"suiteKey");
  String objectId  = emxGetParameter(request,"objectId");
  String parentId = objectId;
  DomainObject obj = DomainObject.newInstance(context,objectId);
  StringList ids =obj.getInfoList(context, "from[Object Route].to.id");
  if(ids.size()>0){
  objectId =ids.get(ids.size()-1);
  }
  fs.setDirectory(appDirectory);

  String contentURL = "JF_ECRemxRouteWorkflow.jsp";

  contentURL += "?suiteKey=" + suiteKey + "&initSource=" + initSource + "&jsTreeID=" + jsTreeID + "&parentId=" + parentId;
  contentURL += "&objectId= "+ objectId;


  String PageHeading = "emxComponents.Route.TasksGraphical";
  String HelpMarker = "emxhelproutetasksgraphical";

//(String pageHeading,String helpMarker, String middleFrameURL, boolean UsePrinterFriendly,
//boolean IsDialogPage, boolean ShowPagination, boolean ShowConversion)
  fs.initFrameset(PageHeading,HelpMarker,contentURL,true,false,false,false);

  fs.setStringResourceFile("emxComponentsStringResource");
  fs.setObjectId(objectId);
  fs.setCategoryTree(emxGetParameter(request, "categoryTreeName"));
  fs.writePage(out);
%>
