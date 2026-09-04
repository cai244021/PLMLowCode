<%@ page import="com.matrixone.apps.framework.ui.framesetObject" %>
<%@ page import="com.matrixone.apps.common.VCDocument" %>
<%@ page import="com.matrixone.apps.domain.util.EnoviaResourceBundle" %>
<%@ page import="java.util.Map" %><%--
   emxCommonDocumentCreateDialogFS.jsp   -   Create Frameset for Route Wizard
   Copyright (c) 1992-2020 Dassault Systemes.
   All Rights Reserved.
   This program contains proprietary and trade secret information of MatrixOne, Inc.
   Copyright notice is precautionary only and does not evidence any actual or intended publication of such program

   static const char RCSID[] = $Id: emxCommonDocumentCreateDialogFS.jsp.rca 1.12 Wed Oct 22 16:18:22 2008 przemek Experimental przemek $
--%>

<%@include file = "../emxUIFramesetUtil.inc"%>
<%@include file = "emxComponentsNoCache.inc"%>
<%@include file = "emxComponentsCommonUtilAppInclude.inc"%>

<%
  //Added:24-Feb-09:NZF:R207:Bug:368948
  String strIsAccessFieldRequired = request.getParameter("showAccessType");
    System.out.println("JF_CommonDocumentCreateDialogFS.jsp===strIsAccessFieldRequired::"+strIsAccessFieldRequired);
  //End:R207:PRG:R207:Bug:368948

  if (strIsAccessFieldRequired == null) {
      strIsAccessFieldRequired = "false";
  }

  Map emxCommonDocumentCheckinData = (Map) session.getAttribute("emxCommonDocumentCheckinData");

  framesetObject fs = new framesetObject();
  String initSource = (String)emxCommonDocumentCheckinData.get("initSource");

  if (initSource == null) {
      initSource = "";
  }

  String jsTreeID   =  (String)emxCommonDocumentCheckinData.get("jsTreeID");
  String suiteKey   =  (String)emxCommonDocumentCheckinData.get("suiteKey");
  String portalMode =  (String)emxCommonDocumentCheckinData.get("portalMode");
  String parentOID  =  (String)emxCommonDocumentCheckinData.get("parentId");
  String objectAction = (String) emxCommonDocumentCheckinData.get("objectAction");
  String routeId = (String) emxCommonDocumentCheckinData.get("routeId");
  String partListId = (String) emxCommonDocumentCheckinData.get("partListId");
    System.out.println("partListId FS"+partListId);
  // store the parameter when accessed from the previous page in the wizard
  String fromPage = emxGetParameter(request,"fromAction");
    System.out.println("JF_CommonDocumentCreateDialogFS.jsp===fromPage::"+fromPage);
  if (fromPage!= null)
      emxCommonDocumentCheckinData.put("fromPage",fromPage);

  // Begin : Bug 347223 code modification
  if ("previous".equals(fromPage)) {
      String strPrevNoOfFiles = emxGetParameter(request, "prev_noOfFiles");
      int nNoOfFiles = 0;
      if (strPrevNoOfFiles != null && !"".equals(strPrevNoOfFiles.trim())) {
          try {
              nNoOfFiles = Integer.parseInt(strPrevNoOfFiles);
          } catch (NumberFormatException nfe) {}
      }

      Map mapPreviousCheckinFormData = new HashMap();
      String strParamName = "";
      for (int i = 0; i < nNoOfFiles; i++) {
          strParamName = "prev_bfile" + i;
          mapPreviousCheckinFormData.put(strParamName, emxGetParameter(request, strParamName));
          strParamName = "prev_comments" + i;
          mapPreviousCheckinFormData.put(strParamName, emxGetParameter(request, strParamName));
          strParamName = "prev_format" + i;
          mapPreviousCheckinFormData.put(strParamName, emxGetParameter(request, strParamName));
      }
      emxCommonDocumentCheckinData.put("mapPreviousCheckinFormData", mapPreviousCheckinFormData);
  }
  //End : Bug 347223 code modification


  fs.setDirectory(appDirectory);

  // Specify URL to come in middle of frameset
  StringBuffer contentURL = new StringBuffer(175);
  contentURL.append("JF_CommonDocumentCreateDialog.jsp");

  // add these parameters to each content URL, and any others the App needs
  contentURL.append("?suiteKey=");
  contentURL.append(suiteKey);
  contentURL.append("&initSource=");
  contentURL.append(initSource);
  contentURL.append("&jsTreeID=");
  contentURL.append(jsTreeID);
  contentURL.append("&parentId=");
  contentURL.append(parentOID);
  contentURL.append("&warn=false");
  contentURL.append("&routeId=");
  contentURL.append(routeId);
    contentURL.append("&partListId=");
    contentURL.append(partListId);

  //Added:24-Feb-09:NZF:R207:Bug:368948
  if ("true".equalsIgnoreCase(strIsAccessFieldRequired)) {
      contentURL.append("&showAccessType=required");
  }
  //End:R207:PRG:R207:Bug:368948

  fs.setStringResourceFile("emxComponentsStringResource");

  String heading = (String) emxCommonDocumentCheckinData.get("stepOneHeader");
    System.out.println("JF_CommonDocumentCreateDialogFS.jsp===heading::"+heading);
  // Page Heading - Internationalized
  String PageHeading = "emxComponents.DocumentCreateDialog.SpecifyDetails";
  if ( heading != null && !"".equals(heading) && !"null".equals(heading) ) {
      PageHeading = heading;
  }

  // Set the help marker. the help icon launches
  // a new window with the help frameset inside
  String HelpMarker = "emxhelpdocumentcreate1" ;

  if (objectAction.equalsIgnoreCase(VCDocument.OBJECT_ACTION_CREATE_VC_FILE_FOLDER) ||
      objectAction.equalsIgnoreCase(VCDocument.OBJECT_ACTION_CREATE_VC_ZIP_TAR_GZ) )
  {
      HelpMarker = "emxhelpdsfacheckindetails";
  }
  else if (objectAction.equalsIgnoreCase(VCDocument.OBJECT_ACTION_COPY_FROM_VC) )
  {
      HelpMarker = "emxhelpdsfacopy";
  }
  else if (objectAction.equalsIgnoreCase(VCDocument.OBJECT_ACTION_STATE_SENSITIVE_CONNECT_VC_FILE_FOLDER) )
  {
      HelpMarker = "emxhelpdsfastatesensitive";
  }
  else if (objectAction.equalsIgnoreCase(VCDocument.OBJECT_ACTION_CONNECT_VC_FILE_FOLDER))
  {
      HelpMarker = "emxhelpdsfaconnection";
  }

  fs.initFrameset(PageHeading,HelpMarker,contentURL.toString(),false,true,false,false);
  String pageTitle = EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", context.getLocale(), "emxComponents.CreateDocument");
  fs.setPageTitle(pageTitle);

  if ( heading != null && !"".equals(heading) && !"null".equals(heading) )
  {
      fs.createFooterLink("emxComponents.Button.Done",
                          "submitForm()",
                          "role_GlobalUser",
                          false,
                          true,
                          "common/images/buttonDialogDone.gif",
                          3);
  } else {
      fs.createFooterLink("emxComponents.Button.Next",
                          "submitForm()",
                          "role_GlobalUser",
                          false,
                          true,
                          "common/images/buttonDialogNext.gif",
                          3);
  }

  fs.createFooterLink("emxComponents.Button.Cancel",
                      "closeWindow()",
                      "role_GlobalUser",
                      false,
                      true,
                      "common/images/buttonDialogCancel.gif",
                      3);

  // ----------------- Do Not Edit Below ------------------------------
  fs.writePage(out);
%>


