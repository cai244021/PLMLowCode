<%-- emxCustomizedTable.jsp
  Copyright (c) 1992-2020 Dassault Systemes.
  This program contains proprietary and trade secret information of
  Dassault Systemes.
  Copyright notice is precautionary only and does not evidence any actual
  or intended publication of such program

  static const char RCSID[] = $Id: emxCustomizedTable.jsp.rca 1.22.3.2 Wed Oct 22 15:47:48 2008 przemek Experimental przemek $
--%>
<%@include file="emxNavigatorInclude.inc"%>
<%@include file="emxNavigatorTopErrorInclude.inc"%>
<%@include file="emxUITableCustomJavaScriptInclude.inc"%>


<%@ page import="com.matrixone.apps.domain.util.FrameworkProperties,
                 com.matrixone.apps.domain.util.XSSUtil"%>

<jsp:useBean id="tableBean" class="com.matrixone.apps.framework.ui.UITable" scope="session"/>
<jsp:useBean id="indentedTableBean" class="com.matrixone.apps.framework.ui.UITableIndented" scope="session"/>

<%
String languageStr          = request.getHeader("Accept-Language");
String strTableName         = (String) emxGetParameter(request,"customTable");
Locale locale               = new Locale(languageStr);
String emxSelectorBadChars  = "";

try {
    emxSelectorBadChars     = EnoviaResourceBundle.getProperty(context, "emxFramework.CustomTable.NameBadChars");
}
catch(Exception e){
    throw (new FrameworkException(e));
}
%>

<html>
<head>
<script type="text/javascript" language="JavaScript" src="../common/scripts/emxUICore.js"></script>
<script type="text/javascript" language="javascript" src="../common/scripts/emxUIConstants.js"></script>
<script type="text/javascript" language="javascript" src="../common/scripts/emxJSValidationUtil.js"></script>
<script type="text/javascript" language="javascript" src="../common/scripts/emxUICoreMenu.js"></script>

<script type="text/javascript" language="JavaScript">
var strColNames = new Array();
strColNames.push('None');
</script>

<script type="text/javascript" language="javascript" src="../common/scripts/emxUIMultiColumnSortUtils.js"></script>
<script type="text/javascript" language="javascript" src="../common/scripts/emxUITableCustom.js"></script>

<script type="text/javascript">
            addStyleSheet("emxUIDialog");
            addStyleSheet("emxChannelList");
            addStyleSheet("emxUIForm");
            addStyleSheet("emxUIDefault");
</script>
<script type="text/javascript" language="javascript">
function checkField(field)
{
    var strText= trimWhitespace(field.value);
    var STR_SELECTOR_BAD_CHARS = "<%=emxSelectorBadChars.trim()%>";
    var ARR_SELECTOR_BAD_CHARS = "";
    if (STR_SELECTOR_BAD_CHARS != "")
    {
        ARR_SELECTOR_BAD_CHARS = STR_SELECTOR_BAD_CHARS.split(" ");
    }
    badCharacters = checkStringForChars(strText,ARR_SELECTOR_BAD_CHARS,false);
    if(badCharacters.length != 0) {
        alert(INVALID_CHAR_MSG  +"\n"
             + STR_SELECTOR_BAD_CHARS);
        getTopWindow().submitInProgress = false;
        return "false";
    }
    return "true";
}

function validateNameField(uiType)
 {
    var textBox = document.getElementById("txtCustomTextBox");
    // Modified for bug no 345339
    textBox.value = textBox.value.trim();
    if(textBox.value!=null)
    {
        var customTableName = textBox.value;
        if(trimWhitespace(customTableName).length == 0){
            alert("<emxUtil:i18nScript localize="i18nId">emxFramework.UITable.TableName.Alert</emxUtil:i18nScript>");
            getTopWindow().submitInProgress = false;
            return;
        }
        var returnValue = checkField(textBox);
        var found="false";
        if(returnValue=="true"){
            var nameField =textBox.value;
            var defaultTableName = '<xss:encodeForJavaScript><%=strTableName%></xss:encodeForJavaScript>';
            if(customTableName!=defaultTableName){
                for(var i=0;i<derivedTablelist.length;i++){
                    if(derivedTablelist[i]==nameField){
                        found="true";
                        break;
                    }
                }
            }
            if(found=="true"){
            	getTopWindow().submitInProgress = false;
                alert("<emxUtil:i18nScript localize="i18nId">emxFramework.CustomizeTable.View.Name.Alert</emxUtil:i18nScript>");
                return;
            }
            else if(!submitForm(uiType)){
					getTopWindow().submitInProgress = false;
			}
         }
     }
     // Till here

 }
</script>

</head>
<body>

<%
        HashMap hmpAvailableColumns             = null;
        MapList mplVisibleColumns               = null;
        MapList mplBasicColumns                 = null;
        MapList mplHiddenColumns                = null;
        MapList mplAttributeColumns             = null;
        MapList mplRelationshipAttributeColumns = null;
        MapList mplExpressionColumns            = null;
        MapList mapInterfaceAttributes          = null;
        HashMap requestMap                      = null;
        HashMap hmpTableData                    = null;
        HashMap hmpTableControlMap              = null;
        String timeStamp                        = emxGetParameter(request, "timeStamp");
        String strCurrentTable                  = emxGetParameter(request,"table");
        String strMultiColumnSort               = (String) emxGetParameter(request,"multiColumnSort");
        String strUIType                        = emxGetParameter(request, "uiType");
        String strMode                          = emxGetParameter(request, "mode");
        String strSortDirection                 = emxGetParameter(request,"sortDirection");
        String strSortColumnName                = emxGetParameter(request,"sortColumnName");
        boolean canShowSnippets = false;
        if(strUIType!=null && "table".equalsIgnoreCase(strUIType))
        {
            requestMap          = (HashMap)tableBean.getRequestMap(timeStamp);
            hmpTableData        = tableBean.getTableData(timeStamp);
            boolean isUserTable = ((Boolean)requestMap.get("userTable")).booleanValue();
            hmpTableControlMap  = tableBean.getControlMap(hmpTableData);
            if(strMode.equalsIgnoreCase("New")&& !isUserTable)
            {
                strSortColumnName   = (String) hmpTableControlMap.get("SortColumnName");
                strSortDirection    = (String) hmpTableControlMap.get("SortDirection");
            }
            if(isUserTable && strMode.equalsIgnoreCase("Edit"))
            {
                strSortColumnName   = (String) requestMap.get("customSortColumns");
                strSortDirection    = (String) requestMap.get("customSortDirections");

            }
        }
        else if(strUIType!=null && "structureBrowser".equalsIgnoreCase(strUIType))
        {
            requestMap          = (HashMap)indentedTableBean.getRequestMap(timeStamp);
            hmpTableData        = indentedTableBean.getTableData(timeStamp);
            boolean isUserTable = ((Boolean)requestMap.get("userTable")).booleanValue();
            hmpTableControlMap  = indentedTableBean.getControlMap(hmpTableData);
            if(strMode.equalsIgnoreCase("New")&& !isUserTable)
            {
                strSortColumnName   = (String) hmpTableControlMap.get("SortColumnName");
                strSortDirection    = (String) hmpTableControlMap.get("SortDirection");
            }
            if(isUserTable && strMode.equalsIgnoreCase("Edit"))
            {
                strSortColumnName   = (String) requestMap.get("customSortColumns");
                strSortDirection    = (String) requestMap.get("customSortDirections");

            }
			canShowSnippets = UISearchUtil.canShowSnippets(context, requestMap);
        }
        StringList strlSortDirection    = new StringList();
        StringList strlSortColumnName   = new StringList();
        if(strSortDirection!=null && strSortDirection.contains(","))
             strlSortDirection = FrameworkUtil.split(strSortDirection,",");
        else if(strSortDirection!=null && strSortDirection.length()>0)
            strlSortDirection.add(strSortDirection);

        if(strSortColumnName!=null && strSortColumnName.contains(","))
            strlSortColumnName = FrameworkUtil.split(strSortColumnName,",");
        else if(strSortColumnName!=null && strSortColumnName.length()>0)
            strlSortColumnName.add(strSortColumnName);

        String strColumnText            = "";
        String strColumnName            = "";
        String strColumnExpr            = "";
        String strColumnWidth           = "";
        String strColumnLabel           = "";
        int iWidth                      = 0;
        String strSortable              = "";
        String strNameLabel             = "";
        String strAvailableColumnsLabel = "";
        String strVisibleColumnsLabel   = "";
        String strWidthLabel            = "";
        String strRegSuite              = "";
        String strWidthLegendLabel      = "";
        
        java.util.List derivedTableNamesList    = com.matrixone.apps.framework.ui.UITableCustom.getDerivedTableNames(context,strCurrentTable);
        int iFreezePane = 0;
		String fullTextSearch = "";

        try
        {
            strNameLabel            = EnoviaResourceBundle.getProperty(context, "emxFrameworkStringResource", locale, "emxFramework.CustomTable.Name.Label");
            strAvailableColumnsLabel= EnoviaResourceBundle.getProperty(context, "emxFrameworkStringResource", locale, "emxFramework.CustomTable.AvailableColumns.Label");
            strVisibleColumnsLabel  = EnoviaResourceBundle.getProperty(context, "emxFrameworkStringResource", locale, "emxFramework.CustomTable.VisibleColumns.Label");
            strWidthLabel           = EnoviaResourceBundle.getProperty(context, "emxFrameworkStringResource", locale, "emxFramework.CustomTable.Width.Label");
            strWidthLegendLabel     = EnoviaResourceBundle.getProperty(context, "emxFrameworkStringResource", locale, "emxFramework.UITable.SortBy.Label");

            if(!"".equalsIgnoreCase(strUIType) && (strUIType!=null))
            {
                if("structureBrowser".equalsIgnoreCase(strUIType))
                {
                    hmpAvailableColumns = indentedTableBean.getAvailableColumns(context,timeStamp,strMode);
                    mplVisibleColumns   = indentedTableBean.getCurrentTableVisibleColumns(context,timeStamp,strMode);
                    boolean isUserTable = ((Boolean)requestMap.get("userTable")).booleanValue();
                    iFreezePane         = (Integer.valueOf((String)requestMap.get("customSplit"))).intValue();
					HashMap sbReqMap = indentedTableBean.getRequestMap(timeStamp);
                    fullTextSearch = (String) sbReqMap.get("fullTextSearch");
                }
                else if("table".equalsIgnoreCase(strUIType))
                {
                    hmpAvailableColumns = tableBean.getAvailableColumns(context,timeStamp,strMode);
                    mplVisibleColumns   = tableBean.getCurrentTableVisibleColumns(context,timeStamp,strMode);
                }
                mplHiddenColumns                = (MapList) hmpAvailableColumns.get("hiddenColumn");
                mplBasicColumns                 = (MapList) hmpAvailableColumns.get("basicList");
                mplExpressionColumns            = (MapList) hmpAvailableColumns.get("expressionList");
                mplAttributeColumns             = (MapList) hmpAvailableColumns.get("currenTableAttributeList");
                mplRelationshipAttributeColumns = (MapList) hmpAvailableColumns.get("currentTableRelationshipAttributeList");
				if(canShowSnippets && "true".equals(fullTextSearch)){
                	if("New".equals(strMode) || ("Edit".equals(strMode) && 
                			          !UITableCustom.hasColumn(context, mplVisibleColumns, "system@Snippets") && 
                			          !UITableCustom.hasColumn(context, mplHiddenColumns, "hidden@Snippets"))){
                		mplVisibleColumns.add(UISearchUtil.getSnippetsColumn(context, new Locale(languageStr), true));
                	}
                }

            }
        }
        catch(Exception e)
        {
            throw(new FrameworkException(e));
        }

    if(derivedTableNamesList!=null &&derivedTableNamesList.size()>0)
    {
        Iterator itr = derivedTableNamesList.iterator();

        while(itr.hasNext())
        {
            String strDerivedTable = (String)itr.next();
            if(strDerivedTable.indexOf("~")>0)
                strDerivedTable = strDerivedTable.substring(0,strDerivedTable.lastIndexOf("~"));

%>
 <script language="JavaScript" type="text/JavaScript">
        derivedTablelist.push('<%=XSSUtil.encodeForJavaScript(context, strDerivedTable)%>');
</script>
<%
        }
    }

%>
<form name="customtable" id='customtable' action="emxCustomizeTableProcess.jsp" method="post">
<input type="hidden" name="table"  value='<xss:encodeForHTMLAttribute><%=strCurrentTable%></xss:encodeForHTMLAttribute>' />
<input type="hidden" name="uiType"  value='<xss:encodeForHTMLAttribute><%=strUIType%></xss:encodeForHTMLAttribute>' />
<input type="hidden" name="timeStamp"  value='<xss:encodeForHTMLAttribute><%=timeStamp%></xss:encodeForHTMLAttribute>' />
<input type="hidden" name="customTableColValue"  value='' />
<input type="hidden" name="columnsText"  value='' />
<input type="hidden" name="mode"  value='<xss:encodeForHTMLAttribute><%=strMode%></xss:encodeForHTMLAttribute>' />
<input type="hidden" name="multiColumnSort"  value='<xss:encodeForHTMLAttribute><%=strMultiColumnSort%></xss:encodeForHTMLAttribute>' />
<input type="hidden" name="hdnFirstColumnDirection"  value='' />
<input type="hidden" name="hdnThirdColumnDirection"  value='' />
<input type="hidden" name="hdnSecondColumnDirection"  value='' />
<input type="hidden" name="hdnFirstColumn"  value='' />
<input type="hidden" name="hdnThirdColumn"  value='' />
<input type="hidden" name="hdnSecondColumn"  value='' />

<table name="Table3" id="Table3" width="100%" height="5%" cellspacing="2" cellpadding="5%">
    <tr>
        <td width = "30%" class="label" colspan ="1%" ><%=strNameLabel%></td>
        <td width = "70%" class="inputField" colspan ="1%" >
        <input name="txtCustomTextBox" id ="txtCustomTextBox" size ="40%" maxlength=60 value='<%=XSSUtil.encodeForHTMLAttribute(context, strTableName)%>'  onKeypress = "javascript:submitFunction(event)" />
        </td>
    </tr>

</table>
<table name="Table1" id="Table1" height="6%" width="100%" cellspacing="0" cellpadding="5%">
    <tr>
        <td class = "label"  colspan ="1%" width="320" nowrap="nowrap"><%=strAvailableColumnsLabel%></td>
        <td class = "label"  colspan ="1%" align="left"width="100%" nowrap="nowrap" ><%=strVisibleColumnsLabel%></td>

    </tr>
</table>
<table id="parentTable" cellspacing="0" >
<tr>
<td class = "inputField" >
<table id ="Table6"  cellspacing="0" cellpadding="5%" >
<tr>
<td class = "inputField" width="270px;" valign="top" >
<select name="AvailableColumn" id="AvailableColumn" size="25" style="width:270px;" multiple>
        <%
                    if((mplHiddenColumns != null)&& (mplHiddenColumns.size()>0))
                    {
                        for (int i = 0, size = mplHiddenColumns.size(); i < size; i++)
                        {
                            Map hiddenMap   = (Map) mplHiddenColumns.get(i);
                            strColumnName   = (String) hiddenMap.get("name");
							//if showsnippets is false, then dont list snippet column in available columns list
                            if("hidden@Snippets".equals(strColumnName) && !canShowSnippets){
                            	continue;
                            }
                            strColumnExpr   = (String) hiddenMap.get("expression");
                            strColumnText   = (String) hiddenMap.get("displayLabel");
                            strColumnWidth  = (String) hiddenMap.get("width");
                            strSortable     = (String) hiddenMap.get("sortable");
                            String label    = "";
                            
                            if("default".equalsIgnoreCase(strColumnWidth))
                                iWidth = 0;
                            else
                            {
                                if("structureBrowser".equalsIgnoreCase(strUIType))
                                    iWidth = (Integer.parseInt(strColumnWidth)) / 8;
                                else
                                    iWidth = (Integer.parseInt(strColumnWidth));
                            }

%>
                    <option title='<xss:encodeForHTML><%=strColumnText%></xss:encodeForHTML>' value='<%=strColumnName+"|"+strColumnExpr+"|"+label+"|"+strSortable+"~"+iWidth%>'><xss:encodeForHTML><%=strColumnText%></xss:encodeForHTML></option>
<%                  }
                    }
                    if((mplBasicColumns != null)&& (mplBasicColumns.size()>0))
                    {
                        for (int i = 0, size = mplBasicColumns.size(); i < size; i++) {
                            Map basicMap    = (Map) mplBasicColumns.get(i);
                            strColumnName   = (String) basicMap.get("name");
                            strColumnExpr   = (String) basicMap.get("expression");
                            strColumnText   = (String) basicMap.get("displayLabel");
                            strSortable     = (String) basicMap.get("sortable");
                            strColumnWidth  = (String) basicMap.get("width");
                            String label    = (String) basicMap.get("label");
                            
                            if("default".equalsIgnoreCase(strColumnWidth) && strColumnWidth!=null || "0".equalsIgnoreCase(strColumnWidth))
                                iWidth = 0;
                            else
                            {
                                if("structureBrowser".equalsIgnoreCase(strUIType))
                                    iWidth = (Integer.parseInt(strColumnWidth)) / 8;
                                else
                                    iWidth = (Integer.parseInt(strColumnWidth));
                            }

%>
                <option title='<xss:encodeForHTML><%=strColumnText%></xss:encodeForHTML>' value='<%=strColumnName+"|"+strColumnExpr+"|"+label+"|"+strSortable+"~"+iWidth%>'><xss:encodeForHTML><%=strColumnText%></xss:encodeForHTML></option>
                <%
                        }
                    }
                    if((mplExpressionColumns != null)&& (mplExpressionColumns.size()>0))
                    {
                        for (int i = 0, size = mplExpressionColumns.size(); i < size; i++) {
                            Map expressionMap   = (Map) mplExpressionColumns.get(i);
                            strColumnName       = (String) expressionMap.get("name");
                            strColumnExpr       = (String) expressionMap.get("expression");
                            strColumnLabel      = (String) expressionMap.get("displayLabel");
                            String label        = (String)expressionMap.get("label");
                            strSortable         = (String) expressionMap.get("sortable");
                            strColumnWidth      = (String) expressionMap.get("width");
                            if("default".equalsIgnoreCase(strColumnWidth) && strColumnWidth!=null || "0".equalsIgnoreCase(strColumnWidth))
                                iWidth = 0;
                            else
                            {
                                if("structureBrowser".equalsIgnoreCase(strUIType))
                                    iWidth = (Integer.parseInt(strColumnWidth)) / 8;
                                else
                                    iWidth = (Integer.parseInt(strColumnWidth));
                            }


%>
                <option title='<xss:encodeForHTML><%=strColumnLabel%></xss:encodeForHTML>' value='<%=strColumnName+"|"+strColumnExpr+"|"+label+"|"+strSortable+"~"+iWidth%>'><xss:encodeForHTML><%=strColumnLabel%></xss:encodeForHTML></option>
                <%
                        }
                    }
                    strColumnText = EnoviaResourceBundle.getProperty(context, "emxFrameworkStringResource", locale, "emxFramework.Basic.Separator");
%>
                    <option value='<%= "Separator|Separator||"+"null~0"%>'><xss:encodeForHTML><%= strColumnText%></xss:encodeForHTML></option>
<%
                    if((mplAttributeColumns != null)&& (mplAttributeColumns.size()>0))
                    {

                        for (int i = 0, size = mplAttributeColumns.size(); i < size; i++) {
                            Map attributeMap    = (Map) mplAttributeColumns.get(i);
                            strColumnName       = (String) attributeMap.get("name");
                            strColumnLabel      = (String) attributeMap.get("displayLabel");
                            strSortable         = (String) attributeMap.get("sortable");
                            strColumnText       = (String) attributeMap.get("expression");
                            strColumnWidth      = (String) attributeMap.get("width");
                            String label        = (String) attributeMap.get("label");
							 if(strColumnName.endsWith("PLMEntity.PLM_ExternalID")){
                                continue;
                            }
                            if("default".equalsIgnoreCase(strColumnWidth) && strColumnWidth!=null || "0".equalsIgnoreCase(strColumnWidth))
                                iWidth = 0;
                            else
                            {
                                if("structureBrowser".equalsIgnoreCase(strUIType))
                                    iWidth = (Integer.parseInt(strColumnWidth)) / 8;
                                else
                                    iWidth = (Integer.parseInt(strColumnWidth));
                            }

%>
                <option title='<xss:encodeForHTML><%=strColumnLabel%></xss:encodeForHTML>' value='<%=strColumnName+"|"+strColumnText+"|"+label+"|"+strSortable+"~"+iWidth%>'><xss:encodeForHTML><%=strColumnLabel%></xss:encodeForHTML></option>
<%
                        }
                    }

                if((mplRelationshipAttributeColumns != null)&& (mplRelationshipAttributeColumns.size()>0))
                    {

                        for (int i = 0, size = mplRelationshipAttributeColumns.size(); i < size; i++) {
                            Map relAttrMap  = (Map) mplRelationshipAttributeColumns.get(i);
                            strColumnName   = (String) relAttrMap.get("name");
                            strColumnLabel  = (String) relAttrMap.get("displayLabel");
                            strSortable     = (String) relAttrMap.get("sortable");
                            strColumnText   = (String) relAttrMap.get("expression");
                            strColumnWidth  = (String) relAttrMap.get("width");
                            String label    = (String) relAttrMap.get("label");
                            if("default".equalsIgnoreCase(strColumnWidth) && strColumnWidth!=null || "0".equalsIgnoreCase(strColumnWidth))
                                iWidth = 0;
                            else
                            {
                                if("structureBrowser".equalsIgnoreCase(strUIType))
                                    iWidth = (Integer.parseInt(strColumnWidth)) / 8;
                                else
                                    iWidth = (Integer.parseInt(strColumnWidth));
                            }

%>
                <option title='<xss:encodeForHTML><%=strColumnLabel%></xss:encodeForHTML>' value='<%=strColumnName+"|"+strColumnText+"|"+label+"|"+strSortable+"~"+iWidth%>'><xss:encodeForHTML><%=strColumnLabel%></xss:encodeForHTML></option>
<%
        }
                    }

%>

</select>
</td>
<td class="inputField" valign="top" width="80">
<table id="Table2" name="Table2" align="center" width="100%">
<tr>
    <td class="inputField" align="center"><a href="javascript:moveRight()"> <img id="Rightmove" border="0" src="images/buttonRight.gif" /></a></td>
</tr>
<tr>
    <td class="inputField" align="center"><a href="JavaScript:moveLeft()"> <img id="Leftmove" border="0" src="images/buttonLeft.gif" /> </a></td>
</tr>
<tr>
<td class="inputField" align="center"><a href="JavaScript:remove()"> <img id="Remove" border="0" src="images/buttonRemove.gif" /> </a></td>
</tr>
</table>
</td>
<td class="inputField" valign="top" width="320">
<table>
            <tr>
                <td class="inputField" width="80%"">
                <select name="VisibleColumn" id="VisibleColumn" size="13" style="WIDTH: 270px;"  multiple onclick="displayWidth()" onchange="displayWidth()">
<%
            for (int i = 0, size = mplVisibleColumns.size(); i < size; i++)
            {
                HashMap hmpColumnData   = (HashMap) mplVisibleColumns.get(i);
                strColumnName           = (String) hmpColumnData.get("name");
				//if showsnippets is false, then dont list snippet column in visible columns list
                if("system@Snippets".equals(strColumnName) && !canShowSnippets){
                	continue;
                }
                strColumnWidth          = (String) hmpColumnData.get("width");
                strColumnLabel          = (String) hmpColumnData.get("displayLabel");
                strColumnExpr           = (String) hmpColumnData.get("expression");
				if (strColumnExpr != null && !"".equalsIgnoreCase(strColumnExpr)) {
                	strColumnExpr = strColumnExpr.contains(",") ? strColumnExpr.replaceAll(",", "CO::MM::A") : strColumnExpr;
                }
                strSortable             = (String) hmpColumnData.get("sortable");
                String label            = "";
                if("default".equalsIgnoreCase(strColumnWidth) && strColumnWidth!=null || "0".equalsIgnoreCase(strColumnWidth))
                    iWidth = 0;
                else
                {
                    if("structureBrowser".equalsIgnoreCase(strUIType))
                        iWidth = (Integer.parseInt(strColumnWidth)) / 8;
                    else
                        iWidth = (Integer.parseInt(strColumnWidth));
                }

%>
                <option title='<xss:encodeForHTML><%=strColumnLabel%></xss:encodeForHTML>' value="<%=strColumnName+"|"+strColumnExpr+"|"+label+"|"+strSortable+"~"+iWidth%>" ><xss:encodeForHTML><%=strColumnLabel%></xss:encodeForHTML></option>
<%
                if("structureBrowser".equalsIgnoreCase(strUIType) && (strUIType!=null))
                {
                    strColumnText = EnoviaResourceBundle.getProperty(context, "emxFrameworkStringResource", locale, "emxFramework.Basic.Pane_Separator");
                    if((iFreezePane<=0) && (i==0))
                    {
%>
                        <option title='<xss:encodeForHTML><%=strColumnText%></xss:encodeForHTML>' value="paneSeparator@SB|paneSeparator|freezePane|false~0"><xss:encodeForHTML><%=strColumnText %></xss:encodeForHTML></option>
<%
                    }
                    if((iFreezePane>0) && (i==iFreezePane-1))
                    {
%>
                        <option title='<xss:encodeForHTML><%=strColumnText%></xss:encodeForHTML>' value="paneSeparator@SB|paneSeparator|freezePane|false~0"><xss:encodeForHTML><%=strColumnText%></xss:encodeForHTML> </option>
<%
                    }
                }
            }
%>
    </select>
    </td>
    <td valign="top">
    <table align="center">
    <tr>
    <td class="inputField" valign="top"><a href="JavaScript:moveUp()"> <img src="images/buttonArrangeUp.gif" border="0" /> </a></td>
    </tr>
    <tr>
    <td class="inputField" valign="top"><a href="JavaScript:moveDown()"> <img src="images/buttonArrangeDown.gif" border="0" /> </a></td>
    </tr>
    </table>
    </td>
    </tr>
</table>

        <table width="100%">
                <tr id ="widthRow" >
                <td>
                    <table width="100%">
                    <tr>
                    <td class="inputField" align="left" width="72%"><%=strWidthLabel%></td>
                    <td class="inputField" width="30%"><input type="text" size="3" name="colWidth" id="colWidth"
                    maxlength=3 align="center" onfocus="javascript:validate()" onchange="javascript:setWidth(this)"
                    onkeypress="javascript:return validateWidth(event);" oncontextmenu="return false"
                    onpaste="return false" ondrop="return false" /></td>
                    </tr>
                    </table>
                    </td>
                </tr>

                <tr>
                    <td class="inputField" valign="top" width="100%">
                    <fieldset style="WIDTH: 100%" >
                    <legend><%=strWidthLegendLabel %></legend>
                    <table name="multiColSort" id="multiColSort" width="100%" cellpadding="2" cellspacing="3" align="center">
                        <tr>
                            <td class="inputField" >&nbsp;</td>
                            <td class="inputField"><img src="images/iconSortAscending.gif" border="0" /></td>
                            <td class="inputField"><img src="images/iconSortDescending.gif" border="0" /></td>
                        </tr>
                        <tr>

                            <td class="inputField">                            
                            <select name="firstColumn" id="firstColumn" style="max-width: 270px;" onchange="javascript:populateSecondOption();javascript:populateThirdOption();populate();conflict()">
                            </select>
                            </td>
                            <td class="inputField"><input type="radio" name="firstSortDirection" value ="ascending" checked /></td>
                            <td class="inputField"><input type="radio" name="firstSortDirection" value ="descending" /></td>
                        </tr>
                        <tr>

                            <td class="inputField"><select name="secondColumn" id="secondColumn" style="max-width: 270px;"  onchange="javascript:populateThirdOption();populate();conflict()">
                            </select></td>
                            <td class="inputField"><input type="radio" name="secondSortDirection" value ="ascending" checked /></td>
                            <td class="inputField"><input type="radio" name="secondSortDirection" value ="descending" /></td>
                        </tr>
                        <tr>

                            <td class="inputField"><select name="thirdColumn" id="thirdColumn" style="max-width: 270px;" onchange="javascript:populate();conflict()">
                            </select></td>
                            <td class="inputField"><input type="radio" name="thirdSortDirection"  value ="ascending" checked /></td>
                            <td class="inputField"><input type="radio" name="thirdSortDirection" value ="descending" /></td>
                        </tr>
                    </table>
                    </fieldset>
        </td>
        </tr>
        </table>
        </td>

    </tr>
    </table>
    </td>
    <td width="100%" class="inputField">
    </td>
    </tr>
    </table>
<%

            if("false".equalsIgnoreCase(strMultiColumnSort))
            {
%>
                <script type="text/javascript" language="JavaScript">
                    disableSort('multiColSort');
                </script>
<%
            }
            else
            {
                if(strSortColumnName!=null)
                {
                    for(int i=0;i<strlSortColumnName.size();i++)
                    {
                        String sortColumnName = (String)strlSortColumnName.get(i);
                        if(!"".equals(sortColumnName))
                        {

%>
                        <script type="text/javascript" language="JavaScript">
                            selectedColumns.push('<xss:encodeForJavaScript><%=sortColumnName%></xss:encodeForJavaScript>');
                        </script>
<%
                        }
                    }
                }
                if(strSortDirection!=null)
                {
                    for(int i=0;i<strlSortDirection.size();i++)
                    {

%>
                        <script type="text/javascript" language="JavaScript">
                            selectedDirection.push('<xss:encodeForJavaScript><%=(String) strlSortDirection.get(i)%></xss:encodeForJavaScript>');
                        </script>
<%
                    }
                }
%>
             <script type="text/javascript" language="JavaScript">
                document.getElementById("txtCustomTextBox").focus();
                setColumn();
                var newcustomtable = document.customtable;
                loadData(selectedColumns,selectedDirection,newcustomtable.firstSortDirection,newcustomtable.secondSortDirection,newcustomtable.thirdSortDirection);
            </script>
<%
            }
%>
</form>
</body>
</html>

