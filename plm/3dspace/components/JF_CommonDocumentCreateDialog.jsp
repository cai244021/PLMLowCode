<%--  emxCommonDocumentCreateDialog.jsp
    Copyright (c) 1992-2020 Dassault Systemes.
    All Rights Reserved  This program contains proprietary and trade secret
    information of MatrixOne, Inc.
    Copyright notice is precautionary only and does not evidence any
    actual or intended publication of such program

    Description : Document Create Wizard, Step 1

    static const char RCSID[] = "$Id: emxCommonDocumentCreateDialog.jsp.rca 1.41.2.1 Tue Dec 23 05:40:19 2008 ds-hkarthikeyan Experimental $";
--%>

<%
  // This is added because adding emxUICommonHeaderEndInclude.inc add
  request.setAttribute("warn", "false");
%>

<%@ page import="matrix.db.JPO" %>
<%@ page import="matrix.util.StringList" %>
<%@ page import="org.apache.commons.text.StringEscapeUtils" %>
<%@ page import="com.matrixone.apps.common.VCDocument" %>
<%@ page import="matrix.db.BusinessType" %>
<%@ page import="com.matrixone.apps.domain.DomainObject" %>
<%@ page import="com.matrixone.apps.domain.util.*" %>
<%@ page import="com.matrixone.apps.common.util.ComponentsUtil" %>
<%@ page import="com.matrixone.apps.framework.ui.UIUtil" %>
<%@ page import="java.util.*" %>
<%@ page import="org.slf4j.Logger" %>
<%@ page import="org.slf4j.LoggerFactory" %>
<%@ page import="com.matrixone.apps.domain.DomainConstants" %>
<%@ page import="com.matrixone.apps.domain.DomainRelationship" %>
<%@include file = "../emxUICommonAppInclude.inc"%>
<%@include file = "emxComponentsNoCache.inc"%>
<%@include file = "emxComponentsCommonUtilAppInclude.inc"%>
<%@include file = "../emxJSValidation.inc" %>
<%@include file = "../common/emxUIConstantsInclude.inc"%>
<script language="javascript" type="text/javascript" src="../common/scripts/emxUICalendar.js"></script>
<%@include file = "../emxUICommonHeaderBeginInclude.inc" %>

<script language="javascript" type="text/javascript" src="../components/emxComponentsJSFunctions.js"></script>
<%@include file = "../emxUICommonHeaderEndInclude.inc" %>
<%!
    private static final Logger _logger = LoggerFactory.getLogger("JF_CommonDocumentCreateDialog.jsp");
%>
<%

  Map emxCommonDocumentCheckinData = (Map) session.getAttribute("emxCommonDocumentCheckinData");

  if(emxCommonDocumentCheckinData == null)
  {
    emxCommonDocumentCheckinData = new HashMap();
  }

  String objectId = emxGetParameter(request,"parentId");
  Enumeration enumParam = request.getParameterNames();

  // Loop through the request elements and
  // stuff into emxCommonDocumentCheckinData
  while (enumParam.hasMoreElements())
  {
    String name  = (String) enumParam.nextElement();
    String value = emxGetParameter(request,name);
    emxCommonDocumentCheckinData.put(name, value);
  }

    System.out.println("JF_CommonDocumentCreateDialog.jsp======emxCommonDocumentCheckinData:"+emxCommonDocumentCheckinData);

    // retrive previously entered values, if any, which are stored in FormBean
  String documentName        = (String) emxCommonDocumentCheckinData.get("name");
  String documentAutoName    = (String) emxCommonDocumentCheckinData.get("AutoName");
  String documentType        = (String) emxCommonDocumentCheckinData.get("documentType");
  String documentPolicy      = (String) emxCommonDocumentCheckinData.get("policy");
  String documentDescription = (String) emxCommonDocumentCheckinData.get("description");
  String JF_DocFileFormatRequirements = (String) emxCommonDocumentCheckinData.get("JF_DocFileFormatRequirements");
  // Bug 301712 fix - previously entered folder name is not retained.
  String wsFolderId          = (String) emxCommonDocumentCheckinData.get("folderId");

  //  Reading request parameters and storing into variables
  String showName            = (String) emxCommonDocumentCheckinData.get("showName");
  String showDescription     = (String) emxCommonDocumentCheckinData.get("showDescription");
  String showType            = (String) emxCommonDocumentCheckinData.get("showType");
  String documentTitle       = (String) emxCommonDocumentCheckinData.get("title");
    // added for the Bug 344426
  String showAccessType      = (String) emxCommonDocumentCheckinData.get("showAccessType");
  String showFolder          = (String) emxCommonDocumentCheckinData.get("showFolder");
  String folderURL           = (String) emxCommonDocumentCheckinData.get("folderURL");
  String defaultType         = (String) emxCommonDocumentCheckinData.get("defaultType");
  String reloadPage          = (String) emxCommonDocumentCheckinData.get("reloadPage");
  String typeChanged         = (String) emxCommonDocumentCheckinData.get("typeChanged");
  String objectAction = (String) emxCommonDocumentCheckinData.get("objectAction");
  String JF_ProjectDocType = (String) emxCommonDocumentCheckinData.get("JF_ProjectDocType");
  String JF_ProjectDocTypeName = (String) emxCommonDocumentCheckinData.get("JF_ProjectDocTypeName");
  String JF_ProjectDocTypeNameOID = (String) emxCommonDocumentCheckinData.get("JF_ProjectDocTypeNameOID");
  String JF_ProjectDocTypeNamePID = DomainConstants.EMPTY_STRING;
  String IsContributorFieldModified = (String) emxCommonDocumentCheckinData.get("IsContributorFieldModified");
  String ContributorHidden = (String) emxCommonDocumentCheckinData.get("ContributorHidden");
  String JF_DocSpecialty = (String) emxCommonDocumentCheckinData.get("JF_DocSpecialty");
  String selectFolderId = (String) emxCommonDocumentCheckinData.get("selectFolderId");
  String typeName = (String) emxCommonDocumentCheckinData.get("typeName");

    String disableFileFolder   = "false";


    _logger.info("emxCommonDocumentCheckinData:{}",emxCommonDocumentCheckinData);

  String routeId = (String) emxCommonDocumentCheckinData.get("routeId");

  String fromPage = (String)emxCommonDocumentCheckinData.get("fromPage");

  // Bug 303724 fix, list of coma delimited symbolic type names only included in type chooser
  String includeTypes        = (String) emxCommonDocumentCheckinData.get("includeTypes");
  String excludePolicies     = (String) emxCommonDocumentCheckinData.get("excludePolicies");

  if (showName == null || "".equals(showName) || "null".equals(showName) || "true".equalsIgnoreCase(showName))
  {
      showName = "required";
  }

  if (showDescription == null || showDescription.equals("") )
  {
      showDescription = "true";
  }


  if (showType == null || showType.equals("") )
  {
      showType = "false";
  }


  if (showAccessType == null || showAccessType.equals("") )
  {
      showAccessType = "false";
  }

  if (showFolder == null || showFolder.equals("") )
  {
        showFolder = "false";
  }

  if( documentName == null || documentName.equals("null"))
  {
      documentName = "";
  }

  if( documentAutoName == null || documentAutoName.equals("null"))
  {
      documentAutoName = "";
  }




  // Bug 301712 fix - previously entered folder name is not retained.
  if (wsFolderId == null || "".equals(wsFolderId) || "null".equals(wsFolderId))
  {
      wsFolderId="";
  }

  // Bug 303724 fix, prepare string list of excluded policies
  StringList listExcludePolicies = new StringList();
  if( excludePolicies != null && !"null".equals(excludePolicies) && !"".equals(excludePolicies.trim()))
  {
      StringList listSymExcludePolicies = FrameworkUtil.split(excludePolicies, ",");
      Iterator itr = listSymExcludePolicies.iterator();
      while(itr.hasNext())
      {
        // get the aboslute policy name
        listExcludePolicies.add(PropertyUtil.getSchemaProperty(context, (String)itr.next()));
      }
  }

  boolean bTypeChanged = false;
  if(typeChanged != null && "true".equals(typeChanged))
  {
    bTypeChanged = true;
  }

  // default to defaultType, first time
  if( documentType == null || documentType.equals("null"))
  {
    if( defaultType != null)
    {
      try
      {
        documentType = PropertyUtil.getSchemaProperty(context, defaultType);
      }
      catch (Exception exp)
      {
        // if there is any error default to "Document" type
        documentType = PropertyUtil.getSchemaProperty(context, "type_Document");
      }
    }
    else
    {
        documentType = PropertyUtil.getSchemaProperty(context, "type_Document");
    }
  }
  String actualType  = PropertyUtil.getSchemaProperty(context, documentType);
  documentType       = !com.matrixone.apps.framework.ui.UIUtil.isNullOrEmpty(actualType)?actualType:documentType;
    System.out.println("JF_CommonDocumentCreateDialog.jsp======documentType"+documentType);
  BusinessType bType = new BusinessType(documentType, context.getVault());
  boolean isAbstract = bType.isAbstract(context);
    System.out.println("JF_CommonDocumentCreateDialog.jsp======isAbstract"+isAbstract);
  String defaultTitle = MqlUtil.mqlCommand(context, "print attribute $1 select $2 dump $3", "Title","default","|");
    System.out.println("JF_CommonDocumentCreateDialog.jsp======defaultTitle"+defaultTitle);


  String symbolicDocumentType = "";
  if( defaultType != null)
  {
    symbolicDocumentType = FrameworkUtil.getAliasForAdmin(context, "type", PropertyUtil.getSchemaProperty(context,defaultType), true);
  }
  else
  {
    symbolicDocumentType = FrameworkUtil.getAliasForAdmin(context, "type", PropertyUtil.getSchemaProperty(context, "type_DOCUMENTS"), true);
  }

  if( documentPolicy == null || "null".equals(documentPolicy) || "".equals(documentPolicy) || bTypeChanged)
  {
      // If no policy passed then read the default policy (symbolic name) defined for the current type
      // in properties
      try
      {
        documentPolicy = EnoviaResourceBundle.getProperty(context,"emxComponents.DefaultPolicy." + symbolicDocumentType);

        if( documentPolicy != null && !"".equals(documentPolicy.trim()))
        {
          documentPolicy = PropertyUtil.getSchemaProperty(context, documentPolicy);
        }
        else
        {
          documentPolicy = null;
        }
      }
      catch (Exception e)
      {
        documentPolicy = null;
      }
  }

  // Bug 303724 fix
  // if Inclusion list is not passed then include DOCUMENTS type by default
  if( includeTypes == null || includeTypes.equals("null") || "".equals(includeTypes.trim()))
  {
      includeTypes = symbolicDocumentType;
  }
    String actionURL = (String) emxCommonDocumentCheckinData.get("actionURL");
    DomainObject domainObject = DomainObject.newInstance(context,routeId);
    String taskType = domainObject.getInfo(context,"type");
    if (actionURL == null )
    {
//      actionURL = "JF_CommonDocumentCheckinDialogFS.jsp";
        //update by ljr  OOTB改造多个文件上传
        actionURL = "JF_emxCommonDocumentCheckinDialogFS.jsp?taskType=" + taskType;
    }
    String requiredText = ComponentsUtil.i18nStringNow("emxComponents.Commom.RequiredText",request.getHeader("Accept-Language"));
    String projectDescription = "";
    String projectSpaceId = "";
    String user = context.getUser();
    ContextUtil.pushContext(context);
    if ("Project Space".equals(taskType)){
        projectDescription= MqlUtil.mqlCommand(context, false, "pri bus "+routeId+" select description dump", true);
        projectSpaceId = routeId;
    }else if ("Task".equals(taskType)){
        try {
            MapList allProject = domainObject.getRelatedObjects(context,"Subtask","*",StringList.create("id","name","description"),new StringList(),true,false,(short)0,"","",0);
            for (Object o : allProject) {
                Map map = (Map) o;
                String type = (String) map.get("type");
                if ("Project Space".equals(type)) {
                    projectDescription = (String) map.get("description");
                    projectSpaceId = (String) map.get("id");
                    break;
                }
            }
        } catch (FrameworkException e) {
            e.printStackTrace();
        }
    }else if ("Workspace Vault".equals(taskType)){
        com.dassault_systemes.enovia.workspace.modeler.WorkspaceVault workspaceVault =
                (com.dassault_systemes.enovia.workspace.modeler.WorkspaceVault) DomainObject.newInstance(context,
                        DomainConstants.TYPE_WORKSPACE_VAULT, DomainConstants.WORKSPACEMDL);
        workspaceVault.setId(routeId);
        Map<String,String> topLevelVault = (Map<String, String>) workspaceVault.getTopLevelVault(context, StringList.create("id", "to[Data Vaults].from.id"));
        String projectId = UIUtil.getValue(topLevelVault, "to[Data Vaults].from.id");
        projectSpaceId = UIUtil.getValue(topLevelVault, "to[Data Vaults].from.id");
        projectDescription= MqlUtil.mqlCommand(context, false, "pri bus "+projectId+" select description dump", true);
    }
    System.out.println("JF_CommonDocumentCreateDialog.jsp======projectDescription"+projectDescription);
    //update by ljr 20250926
    emxCommonDocumentCheckinData.put("projectId", projectSpaceId);
    System.out.println("JF_CommonDocumentCreateDialog.jsp======user"+user);
    String connDepartment = DomainConstants.EMPTY_STRING;
    String connDepartmentId = DomainConstants.EMPTY_STRING;
    DomainObject personObject = PersonUtil.getPersonObject(context, user);
    //获取部门
    MapList mapList = personObject.getRelatedObjects(context,
            DomainRelationship.RELATIONSHIP_MEMBER,
            DomainConstants.TYPE_DEPARTMENT,
            StringList.create(DomainConstants.SELECT_ATTRIBUTE_TITLE, DomainConstants.SELECT_ID),
            new StringList(DomainRelationship.SELECT_ID),
            true,
            false,
            (short) 1,
            "",
            "",
            0);
    System.out.println("JF_CommonDocumentCreateDialog.jsp======connDepartment"+connDepartment);
    if (!mapList.isEmpty()) {
        Map map = (Map) mapList.get(0);
        connDepartment = UIUtil.getValue(map, DomainConstants.SELECT_ATTRIBUTE_TITLE);
        connDepartmentId = UIUtil.getValue(map, DomainConstants.SELECT_ID);
    }
    System.out.println("JF_CommonDocumentCreateDialog.jsp======connDepartment"+connDepartment);
    //拿取研发部门
    //判断是不是研发部门
    String tempId = JPO.invoke(context, "JF_PublicMethodClass", null, "getBasicUrl", new String[]{"JF_RDCenterDepartment.id"}, String.class);
    String[] split1 = tempId.split(",");
    String depDRId = split1[0];
    String folderFlag = "true";
    if ("Task".equals(taskType) &&  !connDepartmentId.equalsIgnoreCase(depDRId) ){
        folderFlag = "false";
    }
    String classFlag = "true";
    if (!connDepartmentId.equalsIgnoreCase(depDRId)) {
        classFlag = "false";
    }
    //获取其他文件库的id
    String otherDocClassId = JPO.invoke(context, "JF_PublicMethodClass", null, "getBasicUrl", new String[]{"otherDoc.ClassId"}, String.class);
    String className = "";
    if (UIUtil.isNullOrEmpty(JF_ProjectDocTypeNameOID)) {
        className = "labelRequired";
    } else {
        if (JF_ProjectDocTypeNameOID.equalsIgnoreCase(otherDocClassId) && "false".equalsIgnoreCase(classFlag)) {
            className = "label";
        } else {
            className = "labelRequired";
        }
    }
    System.out.println("className:"+className);
    emxCommonDocumentCheckinData.put("folderFlag", folderFlag);
    //end

    java.util.List<Map<String, String>> personList = new java.util.ArrayList<>();
    if (UIUtil.isNotNullAndNotEmpty(ContributorHidden)){
        String[] split = ContributorHidden.split(",");
        for (String s : split) {
            DomainObject personObj = DomainObject.newInstance(context,s);
            String personName = personObj.getInfo(context,"name");
            String fullName = PersonUtil.getFullName(context, personName);
            System.out.println("JF_CommonDocumentCreateDialog.jsp======fullName"+fullName);
            Map map = new HashMap();
            map.put("id",s);
            map.put("fullName",fullName);
            personList.add(map);
        }
    }
    System.out.println("JF_CommonDocumentCreateDialog.jsp======personList"+personList);
    ContextUtil.pushContext(context);
%>
<script language="javascript">


  // function to close the window and refresh the parent window.
  // function closeWindow()
  // {
  //   window.location.href = "emxCommonDocumentCancelCreateProcess.jsp";
  // }

  // function to truncate the blank values.
  function trim (textBox) {
    while (textBox.charAt(textBox.length-1) == ' ' || textBox.charAt(textBox.length-1) == "\r" || textBox.charAt(textBox.length-1) == "\n" )
      textBox = textBox.substring(0,textBox.length - 1);
    while (textBox.charAt(0) == ' ' || textBox.charAt(0) == "\r" || textBox.charAt(0) == "\n")
      textBox = textBox.substring(1,textBox.length);
      return textBox;
  }



  function submitForm()
  {
<%
     if ( isAbstract )
     {
%>
        alert("<emxUtil:i18nScript localize="i18nId">emxComponents.Common.InValidType</emxUtil:i18nScript>");
        return;
<%
     }

    if((showName.equalsIgnoreCase("required")) || (showName.equalsIgnoreCase("true")))
    {
%>
      var checkedAutoname = false;
     // var namebadCharName = checkForNameBadCharsList(document.frmMain.name);
      //if (namebadCharName.length != 0)
      var namebadCharName = checkForUnifiedNameBadChars(document.frmMain.name,true);
      if (namebadCharName.length != 0)      
      {
      	var nameAllBadCharName = getAllNameBadChars(document.frmMain.name);
      	var name = document.frmMain.name.name;
      	alert("<emxUtil:i18nScript localize="i18nId">emxComponents.ErrorMsg.InvalidInputMsg</emxUtil:i18nScript>"+namebadCharName+"<emxUtil:i18nScript localize="i18nId">emxComponents.Common.AlertInvalidInput</emxUtil:i18nScript>"+nameAllBadCharName+"<emxUtil:i18nScript localize="i18nId">emxComponents.Alert.RemoveInvalidChars</emxUtil:i18nScript> "+name+" <emxUtil:i18nScript localize="i18nId">emxComponents.Alert.Field</emxUtil:i18nScript>");
		//alert("<emxUtil:i18nScript localize="i18nId">emxComponents.Common.SpecialCharacters</emxUtil:i18nScript>"+namebadCharName+"<emxUtil:i18nScript localize="i18nId">emxComponents.Common.AlertRemoveInValidChars</emxUtil:i18nScript>");
        document.frmMain.name.focus();
        return;
      }
      else if (!document.frmMain.AutoName.checked )
      {
        if(document.frmMain.name.value == "")
        {
          alert("<emxUtil:i18nScript localize="i18nId">emxComponents.Common.EnterDocumentName</emxUtil:i18nScript>");
          document.frmMain.name.focus();
          return;
        }
        else if(!(checkForNameLength128(trim(document.frmMain.name.value)+"-0000000000")))
        {
        	alert("<%=EnoviaResourceBundle.getProperty(context, "emxFrameworkStringResource",
                    new Locale(request.getHeader("Accept-Language")),"emxFramework.Create.NameColumn")%>");
          document.frmMain.name.value = trim(document.frmMain.name.value);
          document.frmMain.name.focus();
          return;
        }
        else if ( !(isAlphanumeric(trim(document.frmMain.name.value), true)) || trim(document.frmMain.name.value) == "")
        {
          alert("<emxUtil:i18nScript localize="i18nId">emxComponents.Common.AlertValidName</emxUtil:i18nScript>");
          document.frmMain.name.focus();
          return;
        }
        document.frmMain.name.value = trim(document.frmMain.name.value);
      }
<%
    }

    if ( showType.equalsIgnoreCase("required") )
    {
%>
      if ( document.frmMain.type.value == "" ) {
        document.frmMain.type.focus();
        alert ("<emxUtil:i18nScript localize="i18nId">emxComponents.Common.TypeError</emxUtil:i18nScript>");
        return;
      }
<%
    }
%>

      if ( document.frmMain.title.value === "" ) {
        document.frmMain.title.focus();
        alert("<emxUtil:i18nScript localize="i18nId">emxComponents.Common.TitleError</emxUtil:i18nScript>");
        return;
      }


      if ( document.frmMain.JF_ProjectDocType.value === "" ) {
          alert("\u8bf7\u9009\u62e9\u5206\u7c7b");
          <%--alert("<emxUtil:i18nScript localize="i18nId">emxComponents.Common.TitleError</emxUtil:i18nScript>");--%>
          return;
      }

      if(document.frmMain.JF_DocSpecialty.value ==="" && document.getElementById('JF_DocSpecialty').className=="labelRequired"){
          alert("\u8bf7\u9009\u62e9\u4e13\u4e1a");
          return;
      }

      if ( document.frmMain.JF_ConnProjectName.value === "" ) {
          alert("\u8bf7\u586b\u5199\u6240\u5c5e\u9879\u76ee");
          <%--alert("<emxUtil:i18nScript localize="i18nId">emxComponents.Common.TitleError</emxUtil:i18nScript>");--%>
          return;
      }

      <%--if (document.getElementById('SignatoriesPerson').style.display !== 'none'){--%>
      <%--    if (document.getElementsByName('Contributor')[0].options.length === 0) {--%>
      <%--        alert("\u8bf7\u9009\u62e9\u4f1a\u7b7e\u4eba\u5458");--%>
      <%--        &lt;%&ndash;alert("<emxUtil:i18nScript localize="i18nId">emxComponents.Common.TitleError</emxUtil:i18nScript>");&ndash;%&gt;--%>
      <%--        return;--%>
      <%--    }--%>
      <%--}--%>

      if ( document.frmMain.description.value == "" && document.getElementById('description').className=="labelRequired")
      {
          alert("<emxUtil:i18nScript localize="i18nId">emxComponents.Common.FillDescription</emxUtil:i18nScript>");
          document.frmMain.description.focus();
          return;
      }



     if (jsDblClick())
  {
      startProgressBar(false);
      document.frmMain.submit();
      return;
  }
  }
  // yb end 2024.7.31
  // original
  /**
   *       if (jsDblClick())
   {
          startProgressBar(false);
          document.frmMain.submit();
          return;
      }
   */


  <!--  yb  start  2024.7.30  -->
  function showPersonFolderSelector(){
      emxShowModalDialog("../common/emxFullSearch.jsp?field=TYPES=type_Person&table=PMCCommonPersonSearchTable&form=PMCCommonPersonSearchForm&showInitialResults=true&selection=single&fieldNameActual=personName&groupName=department_manager&includeOIDprogram=JF_PublicMethodClass:getUserGroupPersons&fieldNameOID=personNameOID&fieldNameDisplay=PersoNDirector&suiteKey=Framework&submitURL=./JF_AEFSearchUtil.jsp&','600','600','true','','personName");
  }

  function showGeneralClassFolderSelector2(){
      var classFlag = "<%=classFlag%>";
      emxShowModalDialog("../common/emxFullSearch.jsp?field=TYPES=type_GeneralClass:CURRENT=policy_Classification.state_Active&table=AEFGeneralSearchResults&showInitialResults=true&includeOIDprogram=JF_DocumentLibrary:selectGeneralLibraryDoc&classFlag="+ classFlag + "&selection=single&fieldNameActual=JF_ProjectDocTypeName&fieldNamePID=JF_ProjectDocTypePID&fieldNameOID=JF_ProjectDocTypeOID&fieldNameDisplay=JF_ProjectDocType&suiteKey=Framework&submitURL=./JF_AEFSearchUtil.jsp&','600','600','true','','GeneralClassName");
  }

  // 等待 DOM 加载完成
  document.addEventListener('DOMContentLoaded', function () {
      // 获取 hidden input xxx 和目标 input yy
      const hidden = document.querySelector('input[name="JF_ProjectDocTypeNamePID"]');
      const oid = document.querySelector('input[name="JF_ProjectDocTypeNameOID"]');
      const target  = document.querySelector('input[name="JF_DocFileFormatRequirements"]');

      // 创建 MutationObserver，监听 value 属性变化
      const observer = new MutationObserver(function (mutationsList) {
          for (let mutation of mutationsList) {
              console.log("@@@@@@@@@@@@@@@@@@@@@@@@@@@")
              if (mutation.type === 'attributes' && mutation.attributeName === 'value') {
                  const newValue = hidden.value;
                  const newValue1 = oid.value;
                  console.log("newValue:", newValue);
                  console.log("mutation.oldValue:", mutation.oldValue);

                  // 防止空值或无变化时重复执行
                  if (newValue !== mutation.oldValue) {
                      //将 xxx 的新值赋给 yy
                      target.value = newValue;
                  }
                  if (newValue1 !== mutation.oldValue) {
                      console.log("mutation.oldValue:", mutation.oldValue);
                      console.log("newValue1:", newValue1);
                      console.log("classFlag:", "<%=classFlag%>");
                      console.log("flag:", newValue1 === "<%=otherDocClassId%>" && "false" === "<%=classFlag%>");
                      if (newValue1 === "<%=otherDocClassId%>" && "false" === "<%=classFlag%>") {
                          console.log("mutation.oldValue:", mutation.oldValue);
                          document.getElementById('JF_DocSpecialty').className = 'label';
                          document.getElementById('description').className = 'label';
                      }else {
                          console.log("mutation.oldValue:", mutation.oldValue);
                          document.getElementById('JF_DocSpecialty').className = 'labelRequired';
                          document.getElementById('description').className = 'labelRequired';
                      }
                  }
              }
          }
      });
      // 开始监听 hiddenXXX 的 value 属性
      observer.observe(hidden, {
          attributes: true,           // 监听属性变化
          attributeFilter: ['value'], // 只监听 value
          attributeOldValue: true     // 记录旧值，用于比较
      });
  });


  //add by ljr 当文件类别为XSO 或者技术标准 时候 显示审批
  function handleInputChange() {
      var value = document.getElementsByName("JF_DocumentType")[0].value;
      console.log("value:{}", value);
      if("XSO"==value || "<emxUtil:i18n localize = "i18nId">emxComponents.CommonDocument.JF_technicalStandard</emxUtil:i18n>"==value) {
          document.getElementsByName("personHeading")[0].style.display = '';
          document.getElementsByName("DepartmentDirector1")[0].style.display = '';
      } else {
          document.getElementsByName("personHeading")[0].style.display = 'none';
          document.getElementsByName("DepartmentDirector1")[0].style.display = 'none';
      }
  }
  //end
  <!--  yb  end  2024.7.30  -->

    function changeSignatoriesField() {
        var value = document.getElementsByName("JF_ProjectDocTypeNameOID")[0].value;
        console.log("value:", value); // 注意这里的格式
        var signatoriesPerson = document.getElementById("SignatoriesPerson"); // 使用 id 获取元素
        if ("Y" === value) {
            signatoriesPerson.style.display = ''; // 显示
        } else {
            signatoriesPerson.style.display = 'none'; // 隐藏
        }
    }

    function addDocumentPerson(){
        var contributorHidden = document.getElementById("ContributorHidden");
        var sURL=  '../common/emxFullSearch.jsp?field=TYPES=type_Person:CURRENT=policy_Person.state_Active&txtExcludeOIDs='+contributorHidden.value+'&table=AEFGeneralSearchResults&selection=multiple&hideHeader=true&submitURL=../enterprisechangemgtapp/ECMUtil.jsp?mode=searchUtilPerson&targetTag=select&selectName=Contributor&inputFieldHidden=ContributorHidden';
        showChooser(sURL, 850, 630);
    }

    function removeDocumentPerson(){
        var selectTag = document.getElementsByName("Contributor");
        var selectedOptionsValue = "";
        var bIsContributorFieldModified = "false";
        for (var i=selectTag[0].options.length-1;i>=0;i--) {
            if (selectTag[0].options[i].selected) {
                if (selectedOptionsValue!="") {
                    selectedOptionsValue += ",";
                }
                selectedOptionsValue += selectTag[0].options[i].value;
                selectTag[0].remove(i);
                bIsContributorFieldModified = "true";
            }
        }
        //To make the decision of calling connect/disconnect method only on field modification.
        if(bIsContributorFieldModified==="true"){
            var isContributorFieldModified = document.getElementById("IsContributorFieldModified");
            isContributorFieldModified.value = "true";
        }
        var contributorHidden = document.getElementById("ContributorHidden");
        var contributorHiddenValues = contributorHidden.value.split(",");
        var selectedOptionsValues = selectedOptionsValue.split(",");
        var contributorHiddenNewValue = "";
        for (var j=0;j<contributorHiddenValues.length;j++) {
            var contributorHiddenValue = contributorHiddenValues[j];
            var contains = "false";
            for (var k=0;k<selectedOptionsValues.length;k++) {
                var selectedOptionValue = selectedOptionsValues[k];
                if (contributorHiddenValue == selectedOptionValue) {
                    contains = "true";
                }
            }
            if (contains == "false") {
                if (contributorHiddenNewValue!="") {
                    contributorHiddenNewValue += ",";
                }
                contributorHiddenNewValue += contributorHiddenValue;
            }
        }
        contributorHidden.value = contributorHiddenNewValue;
    }
    // this function is called by type chooser, everytime a type is selected
  // this reloads the page, and populates the policy chooser correctly
  // function reload() {
  //     document.frmMain.target="";
  //     document.frmMain.action="../components/emxCommonDocumentCreateDialog.jsp?reloadPage=true&contentPageIsDialog=true";
  //     document.frmMain.submit();
  // }

  //function  to move the focus from AutoName to Name, when AutoName checkBox is Unchecked.
  function txtNameFocus()
  {
    if(!document.frmMain.AutoName.checked )
    {
      document.frmMain.AutoName.value = "";
      document.frmMain.name.focus();
    }
    else
    {
      autoNameValue();
    }
    return;
  }

  function autoNameValue()
  {
    if(document.frmMain.AutoName.checked )
    {
      document.frmMain.name.value = "";
      document.frmMain.AutoName.value = "checked";
      document.frmMain.AutoName.focus();
    }
    return;
  }

  function  folderlist() {
    emxShowModalDialog("../common/emxIndentedTable.jsp?objectId=<%=XSSUtil.encodeForURL(context, routeId)%>&expandProgram=emxWorkspace:getWorkspaceVaults&table=TMCSelectFolder&program=emxWorkspace:getRouteScopeWorkspaces&displayView=details&header=emxFramework.IconMail.Common.SelectOneFolder&submitURL=../components/emxCommonSelectWorkspaceFolderProcess.jsp&cancelLabel=emxFramework.Button.Cancel&submitLabel=emxFramework.FormComponent.Done",575,575);
  }

  </script>
<form name="frmMain" method="post" action="<%= XSSUtil.encodeForHTML(context, actionURL) %>" target="_parent" onsubmit="submitForm(); return false">
  <input type="hidden" name="folderId" value="<xss:encodeForHTMLAttribute><%=wsFolderId%></xss:encodeForHTMLAttribute>"/>

<table>
  <tr>     
    <!-- //XSSOK -->  
    <td class="requiredNotice"><%=requiredText%></td>
  </tr>
</table>
   <table>
       <tbody>
           <%--自动命名--%>
           <tr id="nameRow">
               <td class="labelRequired">
                   <emxUtil:i18n localize="i18nId">emxComponents.Common.Name</emxUtil:i18n>
               </td>
               <td class="inputField" >
                   <input type="text" name="name" size="20" value="<xss:encodeForHTMLAttribute><%=documentName%></xss:encodeForHTMLAttribute>"   onFocus="autoNameValue()" onKeyPress="autoNameValue()" onClick="autoNameValue()" onSelect="autoNameValue()" onKeyDown="autoNameValue()" onChange="autoNameValue()" readonly />
                   <input type="checkbox" name="AutoName" value="<xss:encodeForHTMLAttribute><%=documentAutoName%></xss:encodeForHTMLAttribute>" onClick="txtNameFocus()" checked />&nbsp;
                   <emxUtil:i18n localize="i18nId">emxComponents.Common.AutoName</emxUtil:i18n>
               </td>
           </tr>
           <%--标题--%>
           <tr>
               <td class="labelRequired" >
                   <emxUtil:i18n localize="i18nId">emxComponents.Common.Title</emxUtil:i18n>
               </td>
               <td class="inputField" >
                   <input type="text" name="title" size="20" value="<xss:encodeForHTMLAttribute><%=UIUtil.isNullOrEmpty(documentTitle) ? (UIUtil.isNullOrEmpty(defaultTitle) ? "" : defaultTitle):documentTitle%></xss:encodeForHTMLAttribute>" />
               </td>
           </tr>
           <%--是否项目文档--%>
            <tr>
                <td class="labelRequired">
                    <emxUtil:i18n localize="i18nId">emxComponents.Common.JF_ProjectDoc</emxUtil:i18n>
                </td>
                <td class="inputField">
                    <select name="JF_ProjectDoc" disabled>
                        <%
                            String selectedValue = ""; // 用于保存选中的值
                            StringList list = com.matrixone.apps.domain.util.mxAttr.getChoices(context, "JF_ProjectDoc");
                            for (int i = 0; i < list.size(); i++) {
                                String temp = i18nNow.getRangeI18NString("JF_ProjectDoc", list.get(i), sLanguage);
                                _logger.info("list--temp:{}",temp);
                                String key = list.get(i);
                                if ("Y".equalsIgnoreCase(key)) {
                                    selectedValue = key; // 保存选中的值
                        %>
                        <option selected value="<%= key %>"><%= temp %></option>
                        <%
                        } else {
                        %>
                        <option value="<%= key %>"><%= temp %></option>
                        <%
                                }
                            }
                        %>
                    </select>
                    <input type="hidden" name="JF_ProjectDoc" value="<%= selectedValue %>" /> <!-- 隐藏输入框 -->
                </td>
            </tr>

           <%--所属部门--%>
           <tr>
               <td class="labelRequired">
                   <emxUtil:i18n localize="i18nId">emxComponents.Common.JF_ConnDepartment</emxUtil:i18n>
               </td>
               <td class = "inputField">
                   <input type="text" name="JF_ConnDepartment" size="20" value="<xss:encodeForHTMLAttribute><%=connDepartment%></xss:encodeForHTMLAttribute>" readonly/>
               </td>
           </tr>

           <%--分类--%>
           <tr>
                <td class="labelRequired">
                    <emxUtil:i18n localize="i18nId">emxComponents.Common.JF_ProjectDocType</emxUtil:i18n>
                </td>
                <td class = "inputField" >
                    <input id="" value="<%= UIUtil.isNullOrEmpty(JF_ProjectDocType) ?"" : JF_ProjectDocType %>" type="text" name="JF_ProjectDocType" size="20" title="<emxUtil:i18n localize = "i18nId">emxComponents.Common.JF_ProjectDocType</emxUtil:i18n>" readonly />
                    <input type="hidden" name="JF_ProjectDocTypeName" value="<%= UIUtil.isNullOrEmpty(JF_ProjectDocTypeName) ?"" : JF_ProjectDocTypeName %>"/>
                    <input type="hidden" name="JF_ProjectDocTypeNameOID" value="<%= UIUtil.isNullOrEmpty(JF_ProjectDocTypeNameOID) ?"" : JF_ProjectDocTypeNameOID %>"/>
                    <input type="hidden" name="JF_ProjectDocTypeNamePID" value="<%= UIUtil.isNullOrEmpty(JF_ProjectDocTypeNamePID) ?"" : JF_ProjectDocTypeNamePID %>"/>
                    <input type="button" name="JF_ProjectDocTypeButton" value=".." size="5" onClick="showGeneralClassFolderSelector2()"/>
                </td>
           </tr>

           <%--文件格式要求--%>
           <tr>
               <td class="label">
<%--               <td class="labelRequired">--%>
                   <emxUtil:i18n localize="i18nId">emxComponents.Common.JF_DocFileFormatRequirements</emxUtil:i18n>
               </td>
               <td class = "inputField">
                   <input type="text" name="JF_DocFileFormatRequirements" size="20" value="<xss:encodeForHTMLAttribute><%=UIUtil.isNullOrEmpty(JF_DocFileFormatRequirements) ? "" : JF_DocFileFormatRequirements%></xss:encodeForHTMLAttribute>" readonly/>
               </td>
           </tr>

           <%--专业 update by ljr 20251010 显示--%>
           <tr>
<%--               class="labelRequired"--%>
                <td  id="JF_DocSpecialty" class="<%=className%>">
                    <emxUtil:i18n localize="i18nId">emxComponents.Common.JF_DocSpecialty</emxUtil:i18n>
                </td>
                <td class="inputField">
                    <select name="JF_DocSpecialty">
                        <%
                            StringList listSpecialty = com.matrixone.apps.domain.util.mxAttr.getChoices(context, "JF_DocSpecialty");
                            String defaultValue = JF_DocSpecialty; // 获取传入的默认值
                            _logger.info("listSpecialty--listSpecialty:{}",listSpecialty);
                            for (int i = 0; i < listSpecialty.size(); i++) {
                                String temp = i18nNow.getRangeI18NString("JF_DocSpecialty", listSpecialty.get(i), context.getSession().getLanguage());
                                _logger.info("listSpecialty--temp:{}",temp);
                                if ("GC".equals(temp)){
                                    temp = "\u6574\u6905";
                                }else if ("GM".equals(temp)){
                                    temp = "\u9aa8\u67b6";
                                }else if ("GU".equals(temp)){
                                    temp = "\u53d1\u6ce1";
                                }else if ("GT".equals(temp)){
                                    temp = "\u9762\u5957";
                                }else if ("GP".equals(temp)){
                                    temp = "\u5851\u6599\u4ef6";
                                }else if ("GK".equals(temp)){
                                    temp = "\u529f\u80fd\u4ef6";
                                }else if ("GE".equals(temp)){
                                    temp = "\u7535\u5668\u4ef6";
                                }
                                String key = listSpecialty.get(i);
                        %>
<%--                                <option value="<%= key %>"><%= temp %></option>--%>
                        <option value="<%= key %>" <%= key.equals(defaultValue) ? "selected" : "" %>><%= temp %></option>

                        <%
                            }
                        %>
                    </select>
                </td>
           </tr>

           <%--所属项目--%>
           <tr>
               <td class="labelRequired">
                   <emxUtil:i18n localize="i18nId">emxComponents.Common.JF_ConnProjectName</emxUtil:i18n>
               </td>
               <td class = "inputField">
                   <input type="text" name="JF_ConnProjectName" size="20" value="<xss:encodeForHTMLAttribute><%=projectDescription%></xss:encodeForHTMLAttribute>" readonly/>
               </td>
           </tr>

           <%--所属阶段--%>
           <tr>
               <td class="labelRequired">
                   <emxUtil:i18n localize="i18nId">emxComponents.Common.JF_ConnProjectPhase</emxUtil:i18n>
               </td>
               <td class = "inputField">
                   <select name = "JF_ConnProjectPhase" id="JF_ConnProjectPhase">
                       <script>
                           // 发送请求获取下拉值
                           $.ajax({
                               url: 'JF_GetTaskPhase.jsp',
                               type: 'GET',
                               dataType: 'json',
                               data:{objectId:'<%=routeId%>', type:'phase'},
                               success: function (result) {
                                   // 将下拉选项添加到 select 元素中
                                   $.each(result, function (index, item) {
                                       $("#JF_ConnProjectPhase").append(
                                           "<option value=\"" + item.name + "\">" + item.name + "</option>"
                                       );
                                   });
                               },
                               error: function (error) {
                                   console.error(error);
                               }
                           });
                       </script>
                   </select>
               </td>
           </tr>

           <%--项目文件夹--%>
           <%
                if ("true".equalsIgnoreCase(folderFlag)) {
           %>
           <tr>
               <td class="labelRequired">
                   <emxUtil:i18n localize="i18nId">emxComponents.Common.JF_Folder</emxUtil:i18n>
               </td>
               <td class = "inputField">
                   <select name="selectFolderId" id="selectFolderId">
                       <%
                       if (DomainConstants.TYPE_PROJECT_SPACE.equalsIgnoreCase(typeName)) {
                           if (UIUtil.isNotNullAndNotEmpty(selectFolderId)) {
                               DomainObject folder = DomainObject.newInstance(context, selectFolderId);
                               String title = folder.getAttributeValue(context, DomainConstants.ATTRIBUTE_TITLE);
                       %>
                       <option value="<%=selectFolderId%>"> <%=title%></option>
                       <%
                            } else {
                               String title = "\u7814\u53d1\u6280\u672f\u6587\u6863";
                       %>
                       <script>
                           // 发送请求获取下拉值
                           $.ajax({
                               url: 'JF_GetTaskPhase.jsp',
                               type: 'GET',
                               dataType: 'json',
                               data:{
                                   objectId:'<%=routeId%>', type: 'folder', connDepartmentId: '<%=connDepartmentId%>'},
                               success: function (result) {
                                   console.log("result:", result);
                                   if (result.length === 0) {
                                       $("#selectFolderId").append(
                                           "<option value=''>" + "<%=title%>" + "</option>"
                                       );
                                       <%--<option value=""> <%=title%></option>--%>
                                   } else {
                                        // 将下拉选项添加到 select 元素中
                                        $.each(result, function (index, item) {
                                            $("#selectFolderId").append(
                                                "<option value=\"" + item.id + "\">" + item.name + "</option>"
                                            );
                                        });
                                   }
                               },
                               error: function (error) {
                                   console.error(error);
                               }
                           });
                       </script>
                       <%
                           }
                       }else {
                               String title = "\u7814\u53d1\u6280\u672f\u6587\u6863";
                       %>
                       <script>
                           // 发送请求获取下拉值
                           $.ajax({
                               url: 'JF_GetTaskPhase.jsp',
                               type: 'GET',
                               dataType: 'json',
                               data:{objectId:'<%=routeId%>', type:'folder', connDepartmentId:'<%=connDepartmentId%>'},
                               success: function (result) {
                                   // 将下拉选项添加到 select 元素中
                                   if (result.length === 0) {
                                       $("#selectFolderId").append(
                                           "<option value=''>" + "<%=title%>" + "</option>"
                                       );
                                       <%--<option value=""> <%=title%></option>--%>
                                   } else {
                                       // 将下拉选项添加到 select 元素中
                                       $.each(result, function (index, item) {
                                           $("#selectFolderId").append(
                                               "<option value=\"" + item.id + "\">" + item.name + "</option>"
                                           );
                                       });
                                   }
                               },
                               error: function (error) {
                                   console.error(error);
                               }
                           });
                       </script>
                       <%
                           }
                       %>
                   </select>
               </td>
           </tr>
           <%
               } else {
           %>
           <tr  style="display: none;">
               <td class="labelRequired">
                   <emxUtil:i18n localize="i18nId">emxComponents.Common.JF_Folder</emxUtil:i18n>
               </td>
               <td class = "inputField">
                   <select name="selectFolderId" id="selectFolderId1">
                       <option value=""> </option>
                   </select>
               </td>
           </tr>
           <%
               }
           %>
           <%--是否保密--%>
           <tr>
                <td class="labelRequired">
                    <emxUtil:i18n localize="i18nId">emxComponents.Common.JF_DocSecurity</emxUtil:i18n>
                </td>
                <td class="inputField">
                    <select name = "JF_DocSecurity" >
                        <%
                            StringList list1 =  com.matrixone.apps.domain.util.mxAttr.getChoices(context,"JF_DocSecurity");

                            for(int i=0;i<list1.size();i++) {
                                String temp =  i18nNow.getRangeI18NString("JF_DocSecurity",list1.get(i),sLanguage);
                                _logger.info("list1--temp:{}",temp);
                                String key = list1.get(i);
                                if("N".equalsIgnoreCase(key)){
                        %>
                        <!-- //XSSOK -->
                        <option selected value="<%=key%>" ><%=temp%></option>
                        <%
                        }
                        else {
                        %>
                        <!-- //XSSOK -->
                        <option  value="<%=key%>" ><%=temp%></option>
                        <%
                                }
                            }
                        %>
                    </select>
                </td>
           </tr>
<%--       <tr id="SignatoriesPerson">--%>
<%--           <td class="labelRequired">--%>
<%--               <emxUtil:i18n localize="i18nId">emxComponents.Common.Signatories</emxUtil:i18n>--%>
<%--           </td>--%>
<%--           <td class="inputField">--%>
<%--               <input type="hidden" name="IsContributorFieldModified" id="IsContributorFieldModified" value="<%= UIUtil.isNullOrEmpty(IsContributorFieldModified) ? "false" : IsContributorFieldModified%>" readonly="readonly" />--%>
<%--               <input type="hidden" name="ContributorHidden" id="ContributorHidden" value="<%= UIUtil.isNullOrEmpty(ContributorHidden) ?"" : ContributorHidden %>" readonly="readonly" />--%>
<%--               <table>--%>
<%--                   <tr>--%>
<%--                       <th rowspan="2" style="width:200px">--%>
<%--                           <select name="Contributor" style="width:200px" multiple="multiple">--%>
<%--                               <%--%>
<%--                                   for (Map<String,String> map : personList) {--%>
<%--                                       String id = UIUtil.getValue(map, "id");--%>
<%--                                       String fullName = UIUtil.getValue(map, "fullName");--%>

<%--                               %>--%>
<%--                               <option  value="<%=id%>" ><%=fullName%></option>--%>
<%--                               <%--%>
<%--                                   }--%>
<%--                               %>--%>
<%--                           </select>--%>
<%--                       </th>--%>
<%--                       <td>--%>
<%--                           <a href="javascript:addDocumentPerson()">--%>
<%--                               <img src="../common/images/iconStatusAdded.gif" width="12" height="12" border="0" />--%>
<%--                           </a>--%>
<%--                           <a href="javascript:addDocumentPerson()">--%>
<%--                               <emxUtil:i18n localize="i18nId">emxComponents.Common.SignatoriesAddPerson</emxUtil:i18n>--%>
<%--                           </a>--%>
<%--                       </td>--%>
<%--                   </tr>--%>
<%--                   <tr>--%>
<%--                       <td>--%>
<%--                           <a href="javascript:removeDocumentPerson()">--%>
<%--                               <img src="../common/images/iconStatusRemoved.gif" width="12" height="12" border="0" />--%>
<%--                           </a>--%>
<%--                           <a href="javascript:removeDocumentPerson()">--%>
<%--                               <emxUtil:i18n localize="i18nId">emxComponents.Common.SignatoriesRemovePerson</emxUtil:i18n>--%>
<%--                           </a>--%>
<%--                       </td>--%>
<%--                   </tr>--%>
<%--               </table>--%>
<%--           </td>--%>
<%--       </tr>--%>

           <%--说明--%>
           <tr>
               <td id="description" class="<%=className%>">
                   <emxUtil:i18n localize="i18nId">emxComponents.Common.Description</emxUtil:i18n>
               </td>
               <td class="inputField">
                   <textarea name="description" rows="5" cols="36" wrap><xss:encodeForHTML><%=documentDescription==null?"":documentDescription%></xss:encodeForHTML></textarea>
               </td>
           </tr>
       </tbody>
   </table>
</form>
<script language="javascript">
    function toggleSignatoriesPersonVisibility() {
        var signatoriesPerson = document.getElementById("SignatoriesPerson");
        var JF_ProjectDocTypeNameOID = "<%= UIUtil.isNullOrEmpty(JF_ProjectDocTypeNameOID) ? "Y" : JF_ProjectDocTypeNameOID %>";

        // 根据 IsContributorFieldModified 的值来决定是否隐藏
        if (JF_ProjectDocTypeNameOID === "N") {
            signatoriesPerson.style.display = "none"; // 隐藏
        } else {
            signatoriesPerson.style.display = ""; // 显示
        }
    }
    // 调用函数以设置初始状态
    // toggleSignatoriesPersonVisibility();
    document.getElementById("nameRow").style.display = "none"; // 隐藏name行
</script>

<%@include file = "../emxUICommonEndOfPageInclude.inc" %>

