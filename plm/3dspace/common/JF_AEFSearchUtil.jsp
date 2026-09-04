<%--  AEFSearchUtil.jsp

   Copyright (c) 1992-2020 Dassault Systemes.
   All Rights Reserved.
   This program contains proprietary and trade secret information of MatrixOne,Inc.
   Copyright notice is precautionary only
   and does not evidence any actual or intended publication of such program

   static const char RCSID[] = $Id: AEFSearchUtil.jsp.rca 1.1 Wed Jan 14 05:57:06 2009 ds-smourougayan Experimental przemek $
--%>

<%@include file = "emxNavigatorInclude.inc"%>
<%
response.setHeader("Cache-Control", "no-cache"); //HTTP 1.1
response.setHeader("Pragma", "no-cache"); //HTTP 1.0
String fieldNameActual = emxGetParameter(request, "fieldNameActual");
String uiType = emxGetParameter(request, "uiType");
String typeAhead = emxGetParameter(request, "typeAhead");
String frameName = emxGetParameter(request, "frameName");
String frameNameForField = emxGetParameter(request, "frameNameForField");
String fieldNameDisplay = emxGetParameter(request, "fieldNameDisplay");
String emxTableRowId[] = emxGetParameterValues(request, "emxTableRowId");
StringBuffer actualValue = new StringBuffer();
StringBuffer displayValue = new StringBuffer();
StringBuffer OIDValue = new StringBuffer();
StringBuffer PIDValue = new StringBuffer();
StringBuffer JF_CountersignDValue = new StringBuffer();
String JFProjectLevelCN  = "";
String JFProjectLevel  = "";
//文件格式要求
String docFileFormatRequirements = DomainConstants.EMPTY_STRING;
String unitRange = DomainConstants.EMPTY_STRING;
String unit = DomainConstants.EMPTY_STRING;
String fieldNameUnit = "JF_Units";
for(int i=0;i<emxTableRowId.length;i++) {
    StringTokenizer strTokenizer = new StringTokenizer(emxTableRowId[i] , "|");
    String strObjectId = strTokenizer.nextToken() ;                         
    DomainObject objContext = new DomainObject(strObjectId);
    StringList strList = new StringList();
    strList.addElement(DomainConstants.SELECT_NAME);
    strList.addElement(DomainConstants.SELECT_TYPE);
    strList.addElement(DomainConstants.SELECT_ATTRIBUTE_TITLE);
    strList.addElement("type.kindof["+ DomainConstants.TYPE_ORGANIZATION +"]");
    strList.addElement("type.kindof["+ DomainConstants.TYPE_WORKSPACE +"]");
    strList.addElement("type.kindof["+ DomainConstants.TYPE_WORKSPACE_VAULT +"]");
    strList.addElement(DomainConstants.SELECT_PHYSICAL_ID);
    strList.addElement(DomainConstants.SELECT_DESCRIPTION);
    strList.addElement("attribute[JF_FileFormatRequirements]");
    strList.addElement("attribute[JF_VPMReference.JF_Unit]");
    strList.add("attribute[JF_Countersign]");
    strList.add("attribute[JF_SpecialistReview]");
    strList.add("attribute[JF_ReceiptConfirmation]");
    strList.add("attribute[JFProjectLevel]");
    Map resultList = objContext.getInfo(context, strList);
    System.out.println("resultList--"+resultList);
    String isOrg = (String)resultList.get("type.kindof["+ DomainConstants.TYPE_ORGANIZATION +"]");
    String isWorkspace = (String)resultList.get("type.kindof["+ DomainConstants.TYPE_WORKSPACE +"]");
    String isWorkspaceVault = (String)resultList.get("type.kindof["+ DomainConstants.TYPE_WORKSPACE_VAULT +"]");
    String strContextObjectName = (String)resultList.get("name");
    String strContextObjectType = (String)resultList.get("type");
    String strContextObjectTitle = (String)resultList.get(DomainConstants.SELECT_ATTRIBUTE_TITLE);
    String strContextObjectPhysicalId = (String)resultList.get(DomainConstants.SELECT_PHYSICAL_ID);
    String SELECT_DESCRIPTION = (String)resultList.get(DomainConstants.SELECT_DESCRIPTION);
    String JF_Countersign = (String)resultList.get("attribute[JF_Countersign]");
    String JF_SpecialistReview = (String)resultList.get("attribute[JF_SpecialistReview]");
    String JF_DocReceiptConfirmation = (String)resultList.get("attribute[JF_ReceiptConfirmation]");
    JFProjectLevel = (String)resultList.get("attribute[JFProjectLevel]");

    docFileFormatRequirements = (String)resultList.get("attribute[JF_FileFormatRequirements]");
    unitRange = (String)resultList.get("attribute[JF_VPMReference.JF_Unit]");
    unit = EnoviaResourceBundle.getRangeI18NString(context, "JF_VPMReference.JF_Unit", unitRange, context.getSession().getLanguage());
    System.out.println("docFileFormatRequirements--"+docFileFormatRequirements);

    JF_CountersignDValue.append(JF_Countersign);
    JF_CountersignDValue.append(",");
    JF_CountersignDValue.append(JF_SpecialistReview);
    JF_CountersignDValue.append(",");
    JF_CountersignDValue.append(JF_DocReceiptConfirmation);

    if(UIUtil.isNullOrEmpty(strContextObjectTitle)) {
    	strContextObjectTitle = strContextObjectName;
    }
    String strTypeSymbolicName  = UICache.getSymbolicName(context, strContextObjectType, "type");
    OIDValue.append(strObjectId);
    PIDValue.append(strContextObjectPhysicalId);
    if("type_Group".equals(strTypeSymbolicName)){
        actualValue.append(strContextObjectTitle);
    }else{
        actualValue.append(strContextObjectName);
    }
    //update by ljr ： When type=type_GeneraClass, the value of displayValue is attribute title
    if ("type_GeneralClass".equals(strTypeSymbolicName)) {
        displayValue.append(strContextObjectTitle);
    } else if ("type_JFSnapshot".equals(strTypeSymbolicName)) {
        displayValue.append(strContextObjectTitle);
    }else if(!UIUtil.isNullOrEmpty(strTypeSymbolicName) && "type_Person".equals(strTypeSymbolicName)) {
        displayValue.append(PersonUtil.getFullName(context, strContextObjectName));
    } else if((UIUtil.isNotNullAndNotEmpty(isOrg) && isOrg.equalsIgnoreCase("true")) || (UIUtil.isNotNullAndNotEmpty(isWorkspace) && "true".equalsIgnoreCase(isWorkspace)) || (UIUtil.isNotNullAndNotEmpty(isWorkspaceVault) && "true".equalsIgnoreCase(isWorkspaceVault)) 
    		|| ("type_Group".equals(strTypeSymbolicName) || "type_GroupProxy".equals(strTypeSymbolicName))) {
    	 displayValue.append(strContextObjectTitle);
    }else if ("type_ProjectSpace".equals(strTypeSymbolicName)) {//add by caipan 20250319 如果是项目就显示项目的名称
        strContextObjectTitle=SELECT_DESCRIPTION;
        displayValue.append(strContextObjectTitle);
        //mod by liuxg 新增如果选择的是项目，获取当前项目的阶段并给项目阶段input赋值
        if(UIUtil.isNotNullAndNotEmpty(JFProjectLevel)){
            JFProjectLevelCN = EnoviaResourceBundle.getRangeI18NString(context, "JFProjectLevel", JFProjectLevel, context.getSession().getLanguage());
        }
    }
    else if ("JF_AddMakeVPMName".equalsIgnoreCase(fieldNameActual)){
        displayValue.append(objContext.getAttributeValue(context, "EnterpriseExtension.V_PartNumber"));
    } else {
        displayValue.append(strContextObjectName);
    }
      System.out.println("displayValue--"+displayValue);  
      System.out.println("fieldNameActual--"+fieldNameActual);
    if(i!=emxTableRowId.length-1){
        actualValue.append("|");
        displayValue.append("|");
        OIDValue.append("|");
        PIDValue.append("|");
        JF_CountersignDValue.append("|");
    }
}                       
%>

<%@page import="matrix.util.StringList,
                java.util.HashMap,
                com.matrixone.apps.domain.DomainObject,
                java.util.StringTokenizer,
                com.matrixone.apps.domain.DomainConstants,
                java.util.Map,
                com.matrixone.apps.domain.util.PersonUtil,
                com.matrixone.apps.framework.ui.UICache,
                com.matrixone.apps.framework.ui.UIUtil"
                %>
<%@ page import="com.matrixone.apps.domain.util.XSSUtil" %>
<%@ page import="com.matrixone.apps.domain.util.EnoviaResourceBundle" %>
<script src="scripts/emxUICore.js" type="text/javascript"></script>
<script language="javascript" type="text/javaScript">
debugger;
var typeAhead = "<%=XSSUtil.encodeForJavaScript(context, typeAhead)%>";
var targetWindow = null;
var uiType = "<%=XSSUtil.encodeForJavaScript(context, uiType)%>";
if(typeAhead == "true") {
    var frameName = "<%=XSSUtil.encodeForJavaScript(context, frameName)%>";
    if(frameName == null || frameName == "null" || frameName == "") {
        targetWindow = window.parent;
    } else {
        targetWindow = getTopWindow().findFrame(window.parent, frameName);
    }
    
    if(!targetWindow){
    	targetWindow = window.parent;
    }
} else {

	var frameName = "<%=XSSUtil.encodeForJavaScript(context, frameNameForField)%>";
	 if(frameName == null || frameName == "null" || frameName == "") {
		targetWindow = getTopWindow().getWindowOpener();
	 } else {
	      targetWindow = getTopWindow().findFrame(getTopWindow().getWindowOpener().getTopWindow(), frameName);
	 }
	 if(!targetWindow){
		 targetWindow = getTopWindow().getWindowOpener();
	 }
}

var tmpFieldNameActual = "<%=XSSUtil.encodeForJavaScript(context, fieldNameActual)%>";
var tmpFieldNameDisplay = "<%=XSSUtil.encodeForJavaScript(context, fieldNameDisplay)%>";
var tmpFieldNameOID = tmpFieldNameActual + "OID";
var tmpFieldNamePID = tmpFieldNameActual + "PID";


var temProjectLevelFieldNameDisplay = "calc_JFProjectLevel";


var vfieldNameActual = targetWindow.document.getElementById(tmpFieldNameActual) ? targetWindow.document.getElementById(tmpFieldNameActual) : targetWindow.parent.document.getElementById(tmpFieldNameActual);
var vfieldNameDisplay = targetWindow.document.getElementById(tmpFieldNameDisplay) ? targetWindow.document.getElementById(tmpFieldNameDisplay) : targetWindow.parent.document.getElementById(tmpFieldNameDisplay);
var vfieldNameOID = targetWindow.document.getElementById(tmpFieldNameOID) ? targetWindow.document.getElementById(tmpFieldNameOID) : targetWindow.parent.document.getElementById(tmpFieldNameOID);
var vfieldNamePID = targetWindow.document.getElementById(tmpFieldNamePID) ? targetWindow.document.getElementById(tmpFieldNamePID) : targetWindow.parent.document.getElementById(tmpFieldNamePID);
var vProjectLevelFieldNameDisplay = targetWindow.document.getElementById(temProjectLevelFieldNameDisplay) ? targetWindow.document.getElementById(temProjectLevelFieldNameDisplay) : targetWindow.parent.document.getElementById(temProjectLevelFieldNameDisplay);

if (vfieldNameActual==null && vfieldNameDisplay==null) {
	vfieldNameActual=targetWindow.document.forms[0][tmpFieldNameActual];
	vfieldNameDisplay=targetWindow.document.forms[0][tmpFieldNameDisplay];
	vfieldNameOID=targetWindow.document.forms[0][tmpFieldNameOID];
	vfieldNamePID=targetWindow.document.forms[0][tmpFieldNamePID];
    if(!vProjectLevelFieldNameDisplay){
        vProjectLevelFieldNameDisplay=targetWindow.document.forms[0][temProjectLevelFieldNameDisplay];
    }

}

if (vfieldNameActual==null && vfieldNameDisplay==null) {
	if(targetWindow.parent.document.forms[0]){
		vfieldNameActual=targetWindow.parent.document.forms[0][tmpFieldNameActual] ? targetWindow.parent.document.forms[0][tmpFieldNameActual] : targetWindow.document.forms[0][tmpFieldNameActual];
		vfieldNameDisplay=targetWindow.parent.document.forms[0][tmpFieldNameDisplay] ? targetWindow.parent.document.forms[0][tmpFieldNameDisplay] : targetWindow.document.forms[0][tmpFieldNameDisplay];
		vfieldNameOID=targetWindow.parent.document.forms[0][tmpFieldNameOID] ? targetWindow.parent.document.forms[0][tmpFieldNameOID] : targetWindow.document.forms[0][tmpFieldNameOID];
		vfieldNamePID=targetWindow.parent.document.forms[0][tmpFieldNamePID] ? targetWindow.parent.document.forms[0][tmpFieldNamePID] : targetWindow.document.forms[0][tmpFieldNamePID];
        if(!vProjectLevelFieldNameDisplay) {
            vProjectLevelFieldNameDisplay = targetWindow.parent.document.forms[0][temProjectLevelFieldNameDisplay] ? targetWindow.parent.document.forms[0][temProjectLevelFieldNameDisplay] : targetWindow.document.forms[0][temProjectLevelFieldNameDisplay];
        }
    }
}

if (vfieldNameActual==null && vfieldNameDisplay==null) {
vfieldNameActual=targetWindow.document.getElementsByName(tmpFieldNameActual)[0] ? targetWindow.document.getElementsByName(tmpFieldNameActual)[0] : targetWindow.parent.document.getElementsByName(tmpFieldNameActual)[0];
vfieldNameDisplay=targetWindow.document.getElementsByName(tmpFieldNameDisplay)[0] ? targetWindow.document.getElementsByName(tmpFieldNameDisplay)[0] : targetWindow.parent.document.getElementsByName(tmpFieldNameDisplay)[0];
vfieldNameOID=targetWindow.document.getElementsByName(tmpFieldNameOID)[0] ? targetWindow.document.getElementsByName(tmpFieldNameOID)[0] : targetWindow.parent.document.getElementsByName(tmpFieldNameOID)[0];
vfieldNamePID=targetWindow.document.getElementsByName(tmpFieldNamePID)[0] ? targetWindow.document.getElementsByName(tmpFieldNamePID)[0] : targetWindow.parent.document.getElementsByName(tmpFieldNamePID)[0];
    if(!vProjectLevelFieldNameDisplay){
        vProjectLevelFieldNameDisplay=targetWindow.document.getElementsByName(temProjectLevelFieldNameDisplay)[0] ? targetWindow.document.getElementsByName(temProjectLevelFieldNameDisplay)[0] : targetWindow.parent.document.getElementsByName(temProjectLevelFieldNameDisplay)[0];
    }
}

/*
   FIX IR-088125V6R2012 
   In IE8, for some use-cases, getElementsByName doesn't work when 
   accessing URL with its full name. Below code address the issue.
 */
if (vfieldNameActual==null && vfieldNameDisplay==null) {
     var elem = targetWindow.document.getElementsByTagName("input");
     var att;
     var iarr;
     for(i = 0,iarr = 0; i < elem.length; i++) {
        att = elem[i].getAttribute("name");
        if(tmpFieldNameDisplay == att) {
            vfieldNameDisplay = elem[i];
            iarr++;
        }
        if(tmpFieldNameActual == att) {
            vfieldNameActual = elem[i];
            iarr++;
        }
        if(iarr == 2) {
            break;
        }
    }
}
/* FIX - END */

vfieldNameDisplay.value ="<%=displayValue%>" ;
vfieldNameActual.value ="<%=actualValue%>" ;

if(vProjectLevelFieldNameDisplay){
    console.log("vProjectLevelFieldNameDisplay");
    console.log(vProjectLevelFieldNameDisplay);
    var child=vProjectLevelFieldNameDisplay.children[1];
    console.log(child);
    child.innerHTML="<xss:encodeForJavaScript><%=JFProjectLevelCN%></xss:encodeForJavaScript>";
    child.value="<xss:encodeForJavaScript><%=JFProjectLevel%></xss:encodeForJavaScript>";
}

if(vfieldNameOID != null) {
vfieldNameOID.value ="<xss:encodeForJavaScript><%=OIDValue%></xss:encodeForJavaScript>" ;
} else if(uiType === "createForm" || uiType === "form") {
vfieldNameActual.value ="<xss:encodeForJavaScript><%=OIDValue%></xss:encodeForJavaScript>" ;
}
if(vfieldNamePID != null) {
	vfieldNamePID.value ="<xss:encodeForJavaScript><%=PIDValue%></xss:encodeForJavaScript>" ;
}

if ("JF_ProjectDocType"==='<%=fieldNameDisplay%>'){
    //update by ljr 20250926

    vfieldNamePID.value = "<%=docFileFormatRequirements%>"
    <%--vfieldNameOID.value = "<%=JF_CountersignDValue%>"--%>
    // var value = targetWindow.document.getElementsByName("JF_ProjectDocTypeNameOID")[0].value;
    // console.log("value:", value);
    // var signatoriesPerson = targetWindow.document.getElementById("SignatoriesPerson"); // 使用 id 获取元素
    // if ("Y" === value) {
    //     signatoriesPerson.style.display = ''; // 显示
    // } else {
    //     signatoriesPerson.style.display = 'none'; // 隐藏
    // }
}
if ("JF_AddMakeVPM" === '<%=fieldNameDisplay%>') {
    vfieldNamePID.value = "<%=unit%>";
    var JF_Units = targetWindow.document.getElementsByName("JF_Units")[0];
    JF_Units.value = "<%=unit%>";
    JF_Units.title = "<%=unitRange%>";
}
/*Below function should be invoked to update the filter values */
if(targetWindow.updateFilters) {
targetWindow.updateFilters("<xss:encodeForJavaScript><%=fieldNameDisplay%></xss:encodeForJavaScript>","<xss:encodeForJavaScript><%=displayValue%></xss:encodeForJavaScript>",true);
}

if(typeAhead != "true") {
getTopWindow().closeWindow();
}
</script>
