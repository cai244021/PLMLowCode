<%-- emxCommonDocumentCheckinDialogFS.jsp - used for Checkin of file into Document Object
   Copyright (c) 1992-2020 Dassault Systemes.
   All Rights Reserved.
   This program contains proprietary and trade secret information of MatrixOne,
   Inc.  Copyright notice is precautionary only
   and does not evidence any actual or intended publication of such program

   emxCommonDocumentMultiFileUploadFS.jsp
   static const char RCSID[] = "$Id: emxCommonDocumentCheckinDialogFS.jsp.rca 1.21 Wed Oct 22 16:18:21 2008 przemek Experimental przemek $"
--%>
<%@page import="com.matrixone.apps.domain.util.EnoviaBrowserUtility.Browsers"%>
<%@page import="com.matrixone.apps.common.util.DocumentUtil"%>
<%@ page import="java.util.Map" %>
<%@ page import="java.util.Enumeration" %>
<%@ page import="com.matrixone.apps.common.CommonDocument" %>
<%@ page import="com.matrixone.apps.domain.util.*" %>
<%@ page import="com.matrixone.apps.common.VCDocument" %>
<%@ page import="matrix.db.JPO" %>
<%@ page import="com.matrixone.apps.domain.DomainObject" %>
<%@ page import="com.matrixone.apps.domain.DomainConstants" %>
<%@ page import="com.matrixone.apps.common.CommonDocumentable" %>
<%@ page import="com.matrixone.apps.framework.ui.UICache" %>
<%@ page import="com.matrixone.apps.framework.ui.UIMenu" %>
<%@ page import="java.util.Iterator" %>
<%@ page import="com.matrixone.apps.common.util.ComponentsUtil" %>
<%@ page import="java.util.HashMap" %>
<link rel="stylesheet" type="text/css" href="../common/mobile/styles/emxUIMobile.css">
<%@include file = "../emxUICommonAppInclude.inc"%>
<%@include file = "emxComponentsUtil.inc"%>
<%
  Map emxCommonDocumentCheckinData = (Map) session.getAttribute("emxCommonDocumentCheckinData");

  if(emxCommonDocumentCheckinData == null)
  {
    emxCommonDocumentCheckinData = new HashMap();
    session.setAttribute("emxCommonDocumentCheckinData", emxCommonDocumentCheckinData);
  }
  String hostName  = request.getLocalName();
    hostName = "https://" + hostName + ":443/fcs/servlet/fcs/checkin";


  String objectAction =  (String)emxCommonDocumentCheckinData.get("objectAction");
  String objectId = (String) emxCommonDocumentCheckinData.get("objectId");
//  if ( objectAction == null || objectAction.equals("image") || objectAction.equals(CommonDocument.OBJECT_ACTION_CREATE_MASTER) )
  {
    Enumeration enumParam = request.getParameterNames();

    // Loop through the request elements and
    // stuff into emxCommonDocumentCheckinData
    String storeFromBL = DocumentUtil.getStoreFromBL(context, "Document");
	System.out.println("L48 Collab & approve emxCommonDocumentPreCheckin getStoreFromBL : " + storeFromBL);
	boolean storeFound = false;	
    while (enumParam.hasMoreElements())
    {
        String name  = (String) enumParam.nextElement();
        String value = emxGetParameter(request,name);
        if(name.equals("store"))
        {
      	  storeFound = true;
      	    if(storeFromBL != null && !"".equals(storeFromBL) && !"null".equals(storeFromBL))
				value = storeFromBL;	  
        }
        emxCommonDocumentCheckinData.put(name, value);
    }
    if(!storeFound)
    {
    	emxCommonDocumentCheckinData.put("store", storeFromBL);
    }
  }
  if ( objectAction == null )
  {
     objectAction =  (String)emxCommonDocumentCheckinData.get("objectAction");
  }
  String documentType = (String) emxCommonDocumentCheckinData.get("realType");
  emxCommonDocumentCheckinData.put("type", documentType);

  if(documentType == null)
  {
      documentType = CommonDocument.TYPE_DOCUMENT;
  }

  // put the document attribute values into formBean
  // since JPO expects the attributes in a map, stuff the formBean with attribute map
  // get the list of Attribute names
  MapList attributeMapList = mxType.getAttributes( context, documentType);

  Iterator i = attributeMapList.iterator();
  String attributeName = null;
  String attrValue = "";
  String attrType = "";
  double tz = Double.parseDouble((String) session.getAttribute ( "timeZone" ));
  Map attributeMap = new HashMap();
  while(i.hasNext())
  {
      Map attrMap = (Map)i.next();
      attributeName = (String)attrMap.get("name");
      attrValue = (String) emxCommonDocumentCheckinData.get(attributeName);
      attrType = (String)attrMap.get("type");
      if ( attrValue != null && !"".equals(attrValue) && !"null".equals(attrValue) )
      {
          if("timestamp".equals(attrType))
          {
             attrValue = eMatrixDateFormat.getFormattedInputDate(context, attrValue, tz,request.getLocale());
          }
          attributeMap.put( attributeName, attrValue);
      }
  }
  String accessType = (String)emxCommonDocumentCheckinData.get("AccessType");
  String accessAttrStr = PropertyUtil.getSchemaProperty(context, "attribute_AccessType");
  if ( accessType != null && !"".equals(accessType) && !"null".equals(accessType))
  {
      attributeMap.put( accessAttrStr, accessType);
  }

  // stuff the formBean with attribute map
  emxCommonDocumentCheckinData.put( "attributeMap", attributeMap);

  if (  objectAction.equalsIgnoreCase(VCDocument.OBJECT_ACTION_COPY_FROM_VC) ||
        objectAction.equalsIgnoreCase(VCDocument.OBJECT_ACTION_CREATE_VC_FILE_FOLDER) ||
        objectAction.equalsIgnoreCase(VCDocument.OBJECT_ACTION_STATE_SENSITIVE_CONNECT_VC_FILE_FOLDER) ||
        objectAction.equalsIgnoreCase(VCDocument.OBJECT_ACTION_CONNECT_VC_FILE_FOLDER) ||
        objectAction.equalsIgnoreCase(VCDocument.OBJECT_ACTION_CONVERT_CHECKIN_VC_FILE_FOLDER) ||
        objectAction.equalsIgnoreCase(VCDocument.OBJECT_ACTION_CONVERT_VC_FILE_FOLDER) ||
        objectAction.equalsIgnoreCase(VCDocument.OBJECT_ACTION_CREATE_VC_ZIP_TAR_GZ) ||
        objectAction.equalsIgnoreCase(VCDocument.OBJECT_ACTION_CREATE_VC_ON_DEMAND))
    {
      //save type ahead values
      String typeAheadFormName = emxGetParameter(request, "typeAheadFormName");
      String tagDisplayValue = emxGetParameter(request, "path");
%>
      <emxUtil:saveTypeAheadValues
        context="<%= context %>"
        form="<%= typeAheadFormName%>"
        field="path"
        displayFieldValue="<%= XSSUtil.encodeForHTML(context, tagDisplayValue) %>"
        />
<%
      tagDisplayValue = emxGetParameter(request, "selector");
%>
      <emxUtil:saveTypeAheadValues
        context="<%= context %>"
        form="<%= typeAheadFormName%>"
        field="selector"
        displayFieldValue="<%= XSSUtil.encodeForXML(context, tagDisplayValue) %>"
        />
      <emxUtil:commitTypeAheadValues context="<%= context %>" />
<%     
      emxCommonDocumentCheckinData.put("showFormat", "false");
      String jpoName = (String)emxCommonDocumentCheckinData.get("JPOName");
      String methodName = (String)emxCommonDocumentCheckinData.get("vcMethodName");
      String selector = (String)emxCommonDocumentCheckinData.get("selector");
      if (selector == null || "".equals(selector) || "null".equals(selector) )
      {
        emxCommonDocumentCheckinData.put("selector","Trunk:Latest");
      }
		String storeFromBL = DocumentUtil.getStoreFromBL(context, "Document");
		System.out.println("L48 Collab & approve emxCommonDocumentCheckinDialogFS getStoreFromBL : " + storeFromBL);

      String server = (String)emxCommonDocumentCheckinData.get("server");
      if ((server != null) && !("".equals(server)) && !("null".equals(server)))
      {
        String symbolicName = FrameworkUtil.getAliasForAdmin(context, "store", server, true);
		if(!storeFromBL.isEmpty())
			symbolicName = storeFromBL;
			
        emxCommonDocumentCheckinData.put("store", symbolicName);
      }
      else
      	emxCommonDocumentCheckinData.put("store", storeFromBL);
      
      String[] args = JPO.packArgs(emxCommonDocumentCheckinData);
      if ( "".equals(objectId) || "null".equals(objectId) )
      {
        objectId = null;
      }
      if (jpoName == null || "".equals(jpoName) || "null".equals(jpoName) )
      {
        jpoName = "emxVCDocument";
      }
      if (methodName == null || "".equals(methodName) || "null".equals(methodName) )
      {
        methodName = "vcDocumentConnectCheckin";
      }
      
      // store the parameter when accessed from the previous page in the wizard
      String fromPage = (String)emxCommonDocumentCheckinData.get("fromPage");
      if(fromPage == null){
        fromPage = "";
      }
      String sDesignSyncError = (String)session.getAttribute("DesignSync.error");
      if(!fromPage.equals("previous") || "true".equals(sDesignSyncError))
      {
        if("true".equals(sDesignSyncError)) {
            session.removeAttribute("DesignSync.error");
        }
		FrameworkUtil.validateMethodBeforeInvoke(context, jpoName, methodName, "Program");
        Map objectMap = (Map)JPO.invoke(context, jpoName, null, methodName, args, Map.class);
        objectId = (String)objectMap.get("objectId");
        emxCommonDocumentCheckinData.put( "objectId", objectId);
      }
      else if ("previous".equalsIgnoreCase(fromPage) && objectId!= null){
        DomainObject object = new DomainObject();
        object.setId(objectId);
        String prevType = (String) object.getInfo(context, DomainConstants.SELECT_TYPE);
		String type = (String) emxCommonDocumentCheckinData.get("type");
        if(!prevType.equals(type)){
            object.deleteObject(context);
			FrameworkUtil.validateMethodBeforeInvoke(context, jpoName, methodName, "Program");
            Map objectMap = (Map)JPO.invoke(context, jpoName, null, methodName, args, Map.class);
            objectId = (String)objectMap.get("objectId");
            emxCommonDocumentCheckinData.put( "objectId", objectId);
        }
        else{
			FrameworkUtil.validateMethodBeforeInvoke(context, jpoName, "modifyObject", "Program");
            Map objectMap = (Map)JPO.invoke(context, jpoName, null, "modifyObject", args, Map.class);
        }
      }
  }


  String actionCommand = null;
  boolean isVersionable = true;
  if ( documentType != null && !"".equals(documentType) && !"null".equals(documentType) )
  {
      if( CommonDocument.TYPE_DOCUMENTS.equals(CommonDocument.getParentType(context, documentType)) )
      {
        CommonDocumentable commonDocument = (CommonDocumentable)DomainObject.newInstance(context,documentType);
        actionCommand = commonDocument.getCheckinCommand(context);
        if ( (objectId != null && !"".equals(objectId) && !"null".equals(objectId)) )
        {
            isVersionable = CommonDocument.allowFileVersioning(context, objectId);
        } else {
            isVersionable = CommonDocument.checkVersionableType(context, documentType);
        }
      } else
      {
          isVersionable = false;
      }
  }
  if( !isVersionable )
  {
      emxCommonDocumentCheckinData.put("isVersionable", Boolean.valueOf(isVersionable));
  }
  if ( !isVersionable && !CommonDocument.OBJECT_ACTION_CHECKIN_WITHOUT_VERSION.equals(objectAction) && !"image".equals(objectAction) )
  {
        emxCommonDocumentCheckinData.put("objectAction", CommonDocument.OBJECT_ACTION_CREATE_CHECKIN);
  }
      if ( actionCommand != null )
      {
          Map commandMap  = UICache.getCommand(context, actionCommand);
          String actionURL = UIMenu.getHRef(commandMap);
%>
              <!-- //XSSOK -->
          <form name="integration" action="<%=actionURL%>" >
            <table>
<%
          java.util.Set set = emxCommonDocumentCheckinData.keySet();
          Iterator itr = set.iterator();
          // Loop through the request elements and
          // stuff into emxCommonDocumentCheckinData
          while (itr.hasNext())
          {
              String name  = (String) itr.next();
              Object value = (Object)emxCommonDocumentCheckinData.get(name);
%>
              <input type="hidden" name="<%=name%>" value="<xss:encodeForHTMLAttribute><%=value%></xss:encodeForHTMLAttribute>" />
<%
          }
%>
            </table>
          </form>
          <script language="javascript">
            document.integration.submit();
          </script>
<%
  } else {

  // check for this request parameter, this is set if the required version of
  // Java plug in is not found on the client machine
  String plugInNotFoundAlert = request.getParameter("plugInNotFoundAlert");
  if("true".equalsIgnoreCase(plugInNotFoundAlert))
  {
      emxCommonDocumentCheckinData.put("plugInNotFoundAlert", "true");
  }
  request.setAttribute("contentPageIsDialog", "true");
  String sHelpMarker = "emxhelpfileuploadnoapplet";
  String heading = (String) emxCommonDocumentCheckinData.get("header");
  if(heading == null || "".equals(heading) || "null".equals(heading)) {
      if (objectAction.equalsIgnoreCase(VCDocument.OBJECT_ACTION_COPY_FROM_VC) ||
          objectAction.equalsIgnoreCase(VCDocument.OBJECT_ACTION_CREATE_VC_FILE_FOLDER) ||
          objectAction.equalsIgnoreCase(VCDocument.OBJECT_ACTION_CREATE_VC_ZIP_TAR_GZ) ||
	        objectAction.equalsIgnoreCase(VCDocument.OBJECT_ACTION_CHECKIN_VC_FILE) ||
	        objectAction.equalsIgnoreCase(VCDocument.OBJECT_ACTION_CHECKIN_VC_FOLDER))
      {
        sHelpMarker = "emxhelpdsfacheckinpage";
      }

      if ( CommonDocument.OBJECT_ACTION_CREATE_MASTER.equalsIgnoreCase(objectAction) )
      {
          heading = "emxComponents.CommonDocument.Step2UploadFiles";
      } else if ( CommonDocument.OBJECT_ACTION_UPDATE_MASTER.equalsIgnoreCase(objectAction) ) {
          heading = "emxComponents.CommonDocument.UpdateFiles";
          sHelpMarker = "emxhelpfileupdate";
      } else if ( CommonDocument.OBJECT_ACTION_CREATE_MASTER_PER_FILE.equalsIgnoreCase(objectAction) ) {
          heading = "emxComponents.CommonDocument.UploadFilesToIndividualDocuments";
      } else if ( CommonDocument.OBJECT_ACTION_CHECKIN_WITH_VERSION.equalsIgnoreCase(objectAction) ) {
          heading = "emxComponents.CommonDocument.CheckinFiles";
      } else if (CommonDocument.OBJECT_ACTION_UPDATE_HOLDER.equalsIgnoreCase(objectAction) ) {
          heading = "emxComponents.Common.UpdateDocuments";
          sHelpMarker = "emxhelpfileupdate";
      } else if ("image".equalsIgnoreCase(objectAction) ) {
          heading = "emxComponents.ImageManager.UploadImages";
          sHelpMarker = "emxhelpfileupdate";
      } else {
          heading = "emxComponents.Common.CheckinDocuments";
      }
  }
  String pageHeading = ComponentsUtil.i18nStringNow(heading,request.getHeader("Accept-Language"));
  boolean showPrev = (CommonDocument.OBJECT_ACTION_CREATE_MASTER.equalsIgnoreCase(objectAction) || 
          			  CommonDocument.OBJECT_ACTION_CREATE_CHECKIN.equalsIgnoreCase(objectAction) || 
          			  VCDocument.OBJECT_ACTION_CREATE_VC_FILE_FOLDER.equalsIgnoreCase(objectAction));

      System.out.println("JF_CommonDocumentCheckinDialogFS.jsp=====emxCommonDocumentCheckinData==:"+emxCommonDocumentCheckinData);
      String selectFolderId = "";
      if (emxCommonDocumentCheckinData.containsKey("selectFolderId")){
          selectFolderId = (String) emxCommonDocumentCheckinData.get("selectFolderId");
      }
  
%>
	<html>
    	<head>
		  	<script language="JavaScript" src="../common/scripts/emxUIConstants.js"></script>
			<script language="JavaScript" src="../common/scripts/emxUICore.js"></script>
			<script language="JavaScript" src="../common/scripts/emxUICoreMenu.js"></script>
			<script language="JavaScript" src="../common/scripts/emxUIToolbar.js"></script>
			<script language="JavaScript" src="../common/scripts/emxUIFilterUtility.js"></script>
			<script language="JavaScript" src="../common/scripts/emxUIActionbar.js"></script>
			<script language="JavaScript" src="../common/scripts/emxUIModal.js"></script>
			<script language="JavaScript" src="../common/scripts/emxNavigatorHelp.js"></script>
			<script language="JavaScript" src="../emxUIPageUtility.js"></script>
			<script language="JavaScript" src="../common/scripts/emxUIBottomPageJavaScriptInclude.js"></script>
            <link rel="stylesheet" type="text/css" href="../common/styles/emxUIDefault.css">
            <link rel="stylesheet" type="text/css" href="../common/styles/emxUIList.css">
			<script language="JavaScript" type="text/JavaScript">
                addStyleSheet("emxUIList");
				addStyleSheet("emxUIDefault");    
				addStyleSheet("emxUIToolbar");    
				addStyleSheet("emxUIMenu");    
				addStyleSheet("emxUIDOMLayout");
				addStyleSheet("emxUIDialog");
				function toggleMaxInactiveInterval(){
					jQuery.ajax({
						url: '../components/emxCommonDocumentSetMaxInactiveInterval.jsp',
						type: 'GET',
						cache: false,
						contentType: false,
						processData: false
					});  
				}

                function goBack(){
                    document.miscAction.action="JF_CommonDocumentCreateDialogFS.jsp?fromAction=previous";
                    document.miscAction.submit();
                }
                function checkinCancel(){
                    getTopWindow().closeWindow();
                }

                function checkinFile(button) {
                    // 获取表单元素
                    var form = document.getElementById('checkinForm');

                    // 检查是否有文件被上传
                    var fileInputs = form.querySelectorAll('input[type="file"]');
                    var hasFile = false;
                    // 遍历所有文件输入框，检查是否有文件被选择
                    for (var i = 0; i < fileInputs.length; i++) {
                        if (fileInputs[i].files.length > 0) {
                            hasFile = true;
                            break; // 如果找到一个文件，跳出循环
                        }
                    }
                    // 如果没有文件被上传，弹出提示
                    if (!hasFile) {
                        alert("\u672a\u4e0a\u4f20\u6587\u4ef6\uff0c\u8bf7\u9009\u62e9\u6587\u4ef6\u540e\u518d\u63d0\u4ea4.");
                        return; // 终止函数执行
                    }

                    // 创建遮罩层
                    var overlay = document.createElement('div');
                    overlay.style.position = 'fixed';
                    overlay.style.top = '0';
                    overlay.style.left = '0';
                    overlay.style.width = '100%';
                    overlay.style.height = '100%';
                    overlay.style.backgroundColor = 'rgba(0, 0, 0, 0.5)';
                    overlay.style.zIndex = '9999';
                    overlay.style.display = 'flex';
                    overlay.style.justifyContent = 'center';
                    overlay.style.alignItems = 'center';
                    overlay.innerHTML = '<div style="color: white; font-size: 20px;"><%=XSSUtil.encodeForHTML(context, "\u6b63\u5728\u4e0a\u4f20\uff0c\u8bf7\u52ff\u91cd\u590d\u70b9\u51fb...")%></div>';
                    document.body.appendChild(overlay);

                    // 禁用当前按钮
                    button.disabled = true;

                    // 创建 FormData 对象
                    var formData = new FormData(form);
                    console.log('Form Data:', Array.from(formData.entries())); // 打印表单数据
                    // 使用 fetch 发送数据到后端
                    fetch("JF_CommonDocumentCheckinProcess.jsp", {
                        method: "POST",
                        body: formData
                    })
                        .then(response => response.json())
                        .then(result => {
                            console.log("result-->", result);
                            const  code=result.code;
                            const  message=result.message;
                            console.log("code-->", code);
                            console.log("message-->", message);
                            if (code==="200"){
                                alert("successfully");
                            }else {
                                alert(message);
                            }
                            // 刷新页面
                            // getTopWindow().getWindowOpener().refreshSBTable(getTopWindow().getWindowOpener().configuredTableName);

                            // window.top.getWindowOpener().top.refreshTablePage();

                            var selectFolderId = "<%=selectFolderId%>";
                            if (selectFolderId){
                                getTopWindow().closeWindow();
                                var sbifrom=window.parent.parent;
                                var eidtTable=sbifrom.openerFindFrame(sbifrom,"detailsDisplay").emxEditableTable;
                                var addNewRowId=eidtTable.getCheckedRows()[0].id
                                var arrRowIds = [];
                                arrRowIds.push(addNewRowId);
                                eidtTable.refreshRowByRowId(arrRowIds);
                                //主动展开选中行，
                                console.log(addNewRowId);
                                eidtTable.expand(arrRowIds, "All");
                            }else {
                                getTopWindow().closeWindow();
                                window.top.getWindowOpener().top.refreshTablePage();
                            }

                            // window.parent.opener.location.reload();
                            // window.top.close();
                        })
                        .catch(err => {
                            alert("fail");
                            // 刷新页面
                            console.log("error-->", err);
                            getTopWindow().closeWindow();
                            window.top.getWindowOpener().top.refreshTablePage();
                            // window.parent.opener.location.reload();
                            // window.top.close();
                        });
                }
			</script>
			<link rel="shortcut icon" href="../favicon.ico" type="image/x-icon" />
  		</head>
    	<%
		boolean isIE = EnoviaBrowserUtility.is(request,Browsers.IE);
		if(isIE){%>
			<body onload=turnOffProgress(); onunload=toggleMaxInactiveInterval();>	
		<%} else {%>
			<body onload=turnOffProgress(); onBeforeunload=toggleMaxInactiveInterval();>	
		<%}%>		
	    	<div id="pageHeadDiv">
	    		<form>
	    			<table>
						<tr>
							<td class="page-title"><h2 id="ph"><%=XSSUtil.encodeForHTML(context, pageHeading)%></h2></td>
							<td class="functions">
            					<table>
            						<tr>
           					 			<td class="progress-indicator"><div id="imgProgressDiv"></div></td>
									</tr>
								</table>
							</td>
						</tr>
					</table>
					<jsp:include page = "../common/emxToolbar.jsp" flush="true">
					    <jsp:param name="toolbar" value=""/>
					    <jsp:param name="suiteKey" value="Components"/>
					    <jsp:param name="PrinterFriendly" value="false"/>
					    <jsp:param name="helpMarker" value="<%=sHelpMarker%>"/>
					    <jsp:param name="export" value="false"/>
					</jsp:include>
				</form>							
			</div>
			
			<div id='divPageBody'>
<%--				<iframe name='checkinFrame' id='checkinFrame' src="emxCommonDocumentCheckinCharsetUTF8.jsp" width='100%' height='100%' frameborder='0' border='0' scrolling="auto"></iframe>--%>

    <form>
        <table class="list">
            <tr>
                <th class="required" width="100%">
                    <emxUtil:i18n localize="i18nId">emxComponents.Common.File</emxUtil:i18n>
                </th>
            </tr>
            <tr>
                <td>
                    <input type="file" name="bbfile" size="30" onpaste="return false;" onKeyDown="this.blur()"  onkeypress="displaymessage(this);return false;" class="filemul" multiple/>
                </td>
            </tr>
        </table>
    </form>

                <form id="checkinForm" name="checkinForm" class="myclass" method="post" enctype="multipart/form-data" action="">
                    <table class="list">
                        <tbody>
                            <tr>
                                <th class="required" width="75%" style="font-style: italic;color: #b90404">
                                    <emxUtil:i18n localize="i18nId">emxComponents.Common.File</emxUtil:i18n>
                                    | <emxUtil:i18n localize="i18nId">emxComponents.Common.Format</emxUtil:i18n>
                                </th>
                                <th class="" width="25%">
                                    <emxUtil:i18n localize="i18nId">emxComponents.Common.Comments</emxUtil:i18n>
                                </th>
                            </tr>
                        <tr class="even">
                            <td width="75%">
                                <table>
                                    <tbody>
                                    <tr>
                                        <td style="font-weight:bold;">
                                            <input type="hidden" name="oldFileName0" value="">
                                            <input type="hidden" name="oid0" value="">
                                            <!-- //XSSOK -->
                                            <input type="hidden" name="__fcs___comment_0" value="">
                                        </td>
                                    </tr>
                                    <tr>
                                        <td>
                                            <!-- For Bug 345330 - Empty the value of the text field after some charcter is typed in manually. -->
                                            <input type="file" name="bfile0"  size="30" onpaste="return false;" onkeydown="this.blur()" onkeypress="displaymessage(this);return false;" value="" class="filesign">
                                            <input type="hidden" name="fileName0" value="">
                                            <!-- //XSSOK -->
                                        </td>
                                    </tr>
                                    <tr>
                                        <td style="padding:3px;">
                                            <select name="format0" size="1">
                                                <option value="generic" selected="">generic</option>
                                                <option value="doc">doc</option>
                                                <option value="dot">dot</option>
                                                <option value="xls">xls</option>
                                                <option value="xla">xla</option>
                                                <option value="ppt">ppt</option>
                                                <option value="ppa">ppa</option>
                                                <option value="msg">msg</option>
                                                <option value="Image">Image</option>
                                                <option value="JT">JT</option>
                                            </select>
                                        </td>
                                    </tr>
                                    </tbody>
                                </table>
                            </td>
                            <td width="25%">
                                <textarea rows="3" name="comments0" cols="40" wrap=""></textarea>
                            </td>
                        </tr>

                        <tr class="odd">
                            <td width="75%">
                                <table>
                                    <tbody>
                                    <tr>
                                        <td style="font-weight:bold;">
                                            <input type="hidden" name="oldFileName1" value="">
                                            <input type="hidden" name="oid1" value="">
                                            <!-- //XSSOK -->
                                            <input type="hidden" name="__fcs___comment_1" value="">
                                        </td>
                                    </tr>
                                    <tr>
                                        <td>
                                            <!-- For Bug 345330 - Empty the value of the text field after some charcter is typed in manually. -->
                                            <input type="file" name="bfile1"  size="30" onpaste="return false;" onkeydown="this.blur()" onkeypress="displaymessage(this);return false;" value="" class="filesign">
                                            <input type="hidden" name="fileName1" value="">
                                            <!-- //XSSOK -->
                                        </td>
                                    </tr>
                                    <tr>
                                        <td style="padding:3px;">
                                            <select name="format1" size="1">
                                                <option value="generic" selected="">generic</option>
                                                <option value="doc">doc</option>
                                                <option value="dot">dot</option>
                                                <option value="xls">xls</option>
                                                <option value="xla">xla</option>
                                                <option value="ppt">ppt</option>
                                                <option value="ppa">ppa</option>
                                                <option value="msg">msg</option>
                                                <option value="Image">Image</option>
                                                <option value="JT">JT</option>
                                            </select>
                                        </td>
                                    </tr>
                                    </tbody>
                                </table>
                            </td>
                            <td width="25%">
                                <textarea rows="3" name="comments1" cols="40" wrap=""></textarea>
                            </td>
                        </tr>

                        <tr class="even">
                            <td width="75%">
                                <table>
                                    <tbody>
                                    <tr>
                                        <td style="font-weight:bold;">
                                            <input type="hidden" name="oldFileName2" value="">
                                            <input type="hidden" name="oid2" value="">
                                            <!-- //XSSOK -->
                                            <input type="hidden" name="__fcs___comment_2" value="">
                                        </td>
                                    </tr>
                                    <tr>
                                        <td>
                                            <!-- For Bug 345330 - Empty the value of the text field after some charcter is typed in manually. -->
                                            <input type="file" name="bfile2"  size="30" onpaste="return false;" onkeydown="this.blur()" onkeypress="displaymessage(this);return false;" value="" class="filesign">
                                            <input type="hidden" name="fileName2" value="">
                                            <!-- //XSSOK -->
                                        </td>
                                    </tr>
                                    <tr>
                                        <td style="padding:3px;">
                                            <select name="format2" size="1">
                                                <option value="generic" selected="">generic</option>
                                                <option value="doc">doc</option>
                                                <option value="dot">dot</option>
                                                <option value="xls">xls</option>
                                                <option value="xla">xla</option>
                                                <option value="ppt">ppt</option>
                                                <option value="ppa">ppa</option>
                                                <option value="msg">msg</option>
                                                <option value="Image">Image</option>
                                                <option value="JT">JT</option>
                                            </select>
                                        </td>
                                    </tr>
                                    </tbody>
                                </table>
                            </td>
                            <td width="25%">
                                <textarea rows="3" name="comments0" cols="40" wrap=""></textarea>
                            </td>
                        </tr>

                        <tr class="odd">
                            <td width="75%">
                                <table>
                                    <tbody>
                                    <tr>
                                        <td style="font-weight:bold;">
                                            <input type="hidden" name="oldFileName3" value="">
                                            <input type="hidden" name="oid3" value="">
                                            <!-- //XSSOK -->
                                            <input type="hidden" name="__fcs___comment_3" value="">
                                        </td>
                                    </tr>
                                    <tr>
                                        <td>
                                            <!-- For Bug 345330 - Empty the value of the text field after some charcter is typed in manually. -->
                                            <input type="file" name="bfile3"  size="30" onpaste="return false;" onkeydown="this.blur()" onkeypress="displaymessage(this);return false;" value="" class="filesign">
                                            <input type="hidden" name="fileName3" value="">
                                            <!-- //XSSOK -->
                                        </td>
                                    </tr>
                                    <tr>
                                        <td style="padding:3px;">
                                            <select name="format3" size="1">
                                                <option value="generic" selected="">generic</option>
                                                <option value="doc">doc</option>
                                                <option value="dot">dot</option>
                                                <option value="xls">xls</option>
                                                <option value="xla">xla</option>
                                                <option value="ppt">ppt</option>
                                                <option value="ppa">ppa</option>
                                                <option value="msg">msg</option>
                                                <option value="Image">Image</option>
                                                <option value="JT">JT</option>
                                            </select>
                                        </td>
                                    </tr>
                                    </tbody>
                                </table>
                            </td>
                            <td width="25%">
                                <textarea rows="3" name="comments3" cols="40" wrap=""></textarea>
                            </td>
                        </tr>

                        <tr class="even">
                            <td width="75%">
                                <table>
                                    <tbody>
                                    <tr>
                                        <td style="font-weight:bold;">
                                            <input type="hidden" name="oldFileName4" value="">
                                            <input type="hidden" name="oid4" value="">
                                            <!-- //XSSOK -->
                                            <input type="hidden" name="__fcs___comment_4" value="">
                                        </td>
                                    </tr>
                                    <tr>
                                        <td>
                                            <!-- For Bug 345330 - Empty the value of the text field after some charcter is typed in manually. -->
                                            <input type="file" name="bfile4"  size="30" onpaste="return false;" onkeydown="this.blur()" onkeypress="displaymessage(this);return false;" value="" class="filesign">
                                            <input type="hidden" name="fileName4" value="">
                                            <!-- //XSSOK -->
                                        </td>
                                    </tr>
                                    <tr>
                                        <td style="padding:3px;">
                                            <select name="format4" size="1">
                                                <option value="generic" selected="">generic</option>
                                                <option value="doc">doc</option>
                                                <option value="dot">dot</option>
                                                <option value="xls">xls</option>
                                                <option value="xla">xla</option>
                                                <option value="ppt">ppt</option>
                                                <option value="ppa">ppa</option>
                                                <option value="msg">msg</option>
                                                <option value="Image">Image</option>
                                                <option value="JT">JT</option>
                                            </select>
                                        </td>
                                    </tr>
                                    </tbody>
                                </table>
                            </td>
                            <td width="25%">
                                <textarea rows="3" name="comments4" cols="40" wrap=""></textarea>
                            </td>
                        </tr>
                        </tbody>
                    </table>
                </form>
                <form name="miscAction" class="myclass" method="post" target="_parent">
                 <input type="hidden" name=fromAction value="previous" />
                    <input type="hidden" name=objectId value=<%=XSSUtil.encodeForHTMLAttribute(context, objectId)%> />
                 <div id="divFileInformation"></div>
                </form>
			</div>
			<div id="divPageFoot">
				  <table width="100%" border="0" align="center" cellspacing="2" cellpadding="3">
					<tr>
					  <td class="buttons" align="right">
						<table border="0" cellspacing="0">
						  <tr>
						    <framework:ifExpr expr="<%=showPrev%>">
								<td>
										<a href="javascript:goBack()" class="button"><button class="btn-default" type="button">
											<emxUtil:i18n localize="i18nId">emxComponents.Button.Previous</emxUtil:i18n></button>
										</a>
								</td>
						    </framework:ifExpr>
							<td>
									<a href="javascript:void(0)" class="button" onclick="checkinFile(this)"><button class="btn-primary" type="button">
										<emxUtil:i18n localize="i18nId">emxComponents.Button.Done</emxUtil:i18n></button>
									</a>
							</td>
							<td>
									<a href="javascript:checkinCancel()" class="button"><button class="btn-default" type="button">
										<emxUtil:i18n localize="i18nId">emxComponents.Button.Cancel</emxUtil:i18n></button>
									</a>
							</td>
						  </tr>
						</table>
					  </td>
					</tr>
				  </table>
				</div>			
    	</body>
   	</html>
<%
  }
%>
