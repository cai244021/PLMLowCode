<%--  ProductConfigurationCreateDialog.jsp

   Copyright (c) 1999-2020 Dassault Systemes.

  All Rights Reserved.
  This program contains proprietary and trade secret information
  of MatrixOne, Inc.  Copyright notice is precautionary only and
  does not evidence any actual or intended publication of such program

  static const char RCSID[] = "$Id: /ENOVariantConfigurationBase/CNext/webroot/configuration/ProductConfigurationCreateDialog.jsp 1.35.2.9.1.1 Wed Oct 29 22:27:16 2008 GMT przemek Experimental$";

--%>
<%@include file="../emxUICommonAppInclude.inc"%>
<%-- Top error page in emxNavigator --%>
<%@include file="../common/emxNavigatorTopErrorInclude.inc"%>
<%--Common Include File --%>
<%@include file="emxProductCommonInclude.inc"%>
<%@include file="../emxUICommonHeaderBeginInclude.inc"%>
<%@include file="GlobalSettingInclude.inc"%>
<%@include file="emxValidationInclude.inc"%>
<%@include file="../common/emxUIConstantsInclude.inc"%>

<%@page import="com.matrixone.apps.domain.DomainObject"%>
<%@page	import="com.matrixone.apps.productline.*,com.matrixone.apps.domain.DomainConstants"%>
<%@page import="com.matrixone.apps.configuration.*"%>
<%@page import="com.matrixone.apps.configuration.ProductConfiguration"%>
<%@page import="com.matrixone.apps.productline.ProductLineUtil"%>
<%@page import="com.matrixone.apps.domain.util.eMatrixDateFormat"%>
<%@page import="com.matrixone.apps.framework.ui.UIUtil"%>
<%@page import="com.matrixone.apps.framework.ui.UIMenu"%>
<%@page import="matrix.util.StringList"%>
<%@page import="java.util.Map"%>
<%@ page import="com.matrixone.apps.domain.util.i18nNow" %>

<SCRIPT language="javascript" src="../common/scripts/emxUICore.js"></SCRIPT>
<script language="javascript" type="text/javascript" src="../components/emxComponentsJSFunctions.js"></script>
<SCRIPT language="javascript" src="../common/scripts/emxUIModal.js"></SCRIPT>
<SCRIPT language="javascript" src="../emxUIPageUtility.js"></SCRIPT>
<SCRIPT language="javascript" src="../common/scripts/emxUICoreMenu.js"></SCRIPT>
<SCRIPT language="javascript" src="../common/scripts/emxUIConstants.js"></SCRIPT>
<SCRIPT language="javascript" src="../common/scripts/emxUICalendar.js"></SCRIPT>
<script language="Javascript" src="../common/scripts/emxUITreeUtil.js"></script>
<script src="scripts/emxUICategoryTab.js" type="text/javascript"></script>
<script type="text/javascript" src="../webapps/c/UWA/js/UWA_Standalone_Alone.js"></script>
<script src="../webapps/ConfiguratorWebClient/ConfiguratorWebClient.js" type="text/javascript"></script>
<script type="text/javascript" src="../common/scripts/jquery-latest.js"></script>
<style>
.moduleFooter {
    display:none
}

.module {
    width:100%;
    height: 100%;
    margin: 0;
    background-color:transparent;
    border: 0;
}

.module > .moduleHeader {
    display:none
}

.module .moduleContent {
    padding:0;
    background:none;
    background-color:transparent;
    height:100%;
}

.module .moduleWrapper{
	height:100%;
}
</style>
<%
String strTableRowId = emxGetParameter(request,"emxTableRowId");
String language = context.getSession().getLanguage();
String strRootNodeErrorMsg = i18nNow.getI18nString("emxProduct.Feature.RootNodeError","emxConfigurationStringResource", language);
String  timeZone = (String) session.getAttribute("timeZone");
boolean showMilestoneField = false;

String ParentProductId = null;
String strModelId = null;
String strModelPhyId = null;
boolean isRootNode = false;
String isLFType = "false";

if (ProductLineCommon.isNotNull(strTableRowId)) {
	java.util.StringTokenizer stTk = new java.util.StringTokenizer(strTableRowId, "|");
	if(stTk.countTokens() == 2) {
	    isRootNode = true;
	}

 }
	    ParentProductId = (String)session.getAttribute("parentOID");
if(!isRootNode) {
			session.setAttribute("ParentProductId", ParentProductId);
            String strProduct = "";
            String jsTreeId = emxGetParameter(request, "jsTreeID");
            String relId = emxGetParameter(request, "relId");
            String strFromContext = null;
            String strPurpose = emxGetParameter(request,
                    "radProductConfigurationPurposeValue");

            String topLevelPart = emxGetParameter(request, "topLevelPart");
            String topLevelPartDisplay = emxGetParameter(request,
                    "topLevelPartDisplay");

            if (topLevelPart == null)
                topLevelPart = "";
            if (topLevelPartDisplay == null)
                topLevelPartDisplay = "";

            String startEffValue = emxGetParameter(request,"startEffValue");
		    if (startEffValue == null)
		    	startEffValue = "";


            boolean isOrder = false;
            if (ProductLineCommon.isNotNull(strPurpose) && strPurpose.equals("Order")) {
                isOrder = true;
            }

            boolean isAccessory = false;
            if (ProductLineCommon.isNotNull(strPurpose) && strPurpose.equals("Accessory")) {
                isAccessory = true;
            }

           try {

                ProductConfiguration productConfiguration = null;
                String objectId = emxGetParameter(request, "objectId");
                String strMarketingText = ConfigurationConstants.EMPTY_STRING;
                if(ProductLineCommon.isNotNull(objectId)){
                	DomainObject domContext =  DomainObject.newInstance(context,objectId);
                    if(domContext.isKindOf(context, ConfigurationConstants.TYPE_LOGICAL_FEATURE)){
                    	strMarketingText = domContext.getInfo(context, "attribute["+ConfigurationConstants.ATTRIBUTE_DISPLAY_TEXT+"]");
                    } else {
                    	strMarketingText = domContext.getInfo(context, "attribute["+ConfigurationConstants.ATTRIBUTE_MARKETING_TEXT+"]");
                    }
                }


                String strType = ConfigurationConstants.EMPTY_STRING;
                String strDerivedFrom = ConfigurationConstants.EMPTY_STRING;
                String derivedFromId = ConfigurationConstants.EMPTY_STRING;
                String startEffectivity = ConfigurationConstants.EMPTY_STRING;
                String strFunctionality = (String) request
                        .getParameter("functionality");
                String strProductContextId = ConfigurationConstants.EMPTY_STRING;
                String txtProductContext = ConfigurationConstants.EMPTY_STRING;

                // Milestone Related
                String txtMilestone = ConfigurationConstants.EMPTY_STRING;



                /* mode can be createnew or getfromsession.
                 * if mode=getfromsession, the page has been reached when tehe
                 * user pressed the previous button on the second page.
                 * In that case , populate the form Objects from the ProductConfiguration
                 * available in the session.
                 */
                String mode = emxGetParameter(request,"PRCFSParam3");
                StringBuffer strBuffMilestone = new StringBuffer();
                if (ProductLineCommon.isNotNull(mode) && (mode.equals("getfromsession"))) {
                    productConfiguration = (ProductConfiguration) session
                            .getAttribute("productconfiguration");
                    strUserVault = productConfiguration.getVault();
                    derivedFromId = productConfiguration.getDerivedFromId();
                    objectId = productConfiguration.getContextId();
					strProductContextId = productConfiguration.getParentProductId();
					startEffectivity = productConfiguration.getStartEffectivity();

					// to get the Milestone Details - Start
					txtMilestone = productConfiguration.get_MILESTONE();

					if(productConfiguration != null && strMarketingText.isEmpty()) {
	                	strMarketingText = productConfiguration.getMarketingText();
	                }
                    if(txtMilestone!=null && !txtMilestone.equals("")){
                    	StringTokenizer strTokenizer = new StringTokenizer(txtMilestone , "-");
                    	String strMilestone1 = strTokenizer.nextToken();
                    	strBuffMilestone.append("[");
                   		String strMilestoneName = new DomainObject(strMilestone1).getInfo(context,DomainObject.SELECT_NAME);
                   		strBuffMilestone.append(strMilestoneName);
                       	strBuffMilestone.append("]");
    					// to get the Milestone Details - Ends
                    }
					if(!UIUtil.isNullOrEmpty(startEffectivity)){
				          double clientTZOffset = (new Double(timeZone)).doubleValue();
				          startEffectivity = eMatrixDateFormat.getFormattedDisplayDate(startEffectivity, clientTZOffset, context.getLocale());
					}

                    strProduct = (UINavigatorUtil.getParsedHeaderWithMacros(
                            context, acceptLanguage, "NAME", objectId)).trim();
                    if(ProductLineCommon.isNotNull(objectId))
                        strType = (String)(new DomainObject(objectId)).getInfo(context,ConfigurationConstants.SELECT_TYPE);

                    strDerivedFrom = (UINavigatorUtil
                            .getParsedHeaderWithMacros(context, acceptLanguage,
                                    "NAME", derivedFromId)).trim();
                    txtProductContext = (UINavigatorUtil
                            .getParsedHeaderWithMacros(context, acceptLanguage,
                                    "NAME", strProductContextId)).trim();


                } else {
                    strProductContextId = ParentProductId;
                    strProduct = (UINavigatorUtil.getParsedHeaderWithMacros(
                            context, acceptLanguage, "NAME", objectId)).trim();
                    if(ProductLineCommon.isNotNull(objectId))
                        strType = (String)(new DomainObject(objectId)).getInfo(context,ConfigurationConstants.SELECT_TYPE);
                    strDerivedFrom = (UINavigatorUtil
                            .getParsedHeaderWithMacros(context, acceptLanguage,
                                    "NAME", derivedFromId)).trim();
                    txtProductContext = (UINavigatorUtil
                            .getParsedHeaderWithMacros(context, acceptLanguage,
                                    "NAME", ParentProductId)).trim();
                    // cleanup the session
                    session.removeAttribute("productConfiguration");
                }
				isLFType = (ProductLineUtil.getChildrenTypes(context, ConfigurationConstants.TYPE_LOGICAL_STRUCTURES).contains(strType))?"true":"false";

                DomainObject productConfigurationDom = DomainObject.newInstance(
                        context,
                        ProductLineConstants.TYPE_PRODUCT_CONFIGURATION,
                        "Configuration");
                //The default revision for the product object is obtained from the bean.
                String strDefaultRevision = productConfigurationDom
                        .getDefaultRevision(
                                context,
                                ProductLineConstants.POLICY_PRODUCT_CONFIGURATION);
                MapList policyList = com.matrixone.apps.domain.util.mxType
                        .getPolicies(
                                context,
                                ProductLineConstants.TYPE_PRODUCT_CONFIGURATION,
                                false);
                String strDefaultPolicy = (String) ((HashMap) policyList.get(0))
                        .get(DomainConstants.SELECT_NAME);
                String strLocale = context.getSession().getLanguage();
                String strPolicy = ProductLineConstants.POLICY;
                String i18nPolicy = i18nNow.getMXI18NString(strDefaultPolicy, "",
                        strLocale.toString(), strPolicy);

                String strFromContextTemp = emxGetParameter(request,
                        "fromcontext");
                if (strFromContextTemp != null || "".equalsIgnoreCase(strFromContextTemp)
                        || "null".equalsIgnoreCase(strFromContextTemp)) {
                    strFromContext = strFromContextTemp;
                } else if (!ProductLineCommon.isNotNull(objectId)) {
                    strFromContext = "fromProductConfiguration";
                } else {
                    strFromContext = "fromProduct";
                }


                // Milestone related
                //boolean showMilestoneField = false;

				String strExpression = "MilestoneActivation";
				String strExprValue = ProductLineUtil.getExpressionValue(context, strExpression);

                if("Enabled".equalsIgnoreCase(strExprValue))
                	showMilestoneField = true;
                if(mxType.isOfParentType(context,strType,ConfigurationConstants.TYPE_PRODUCTS)
                		&& !mxType.isOfParentType(context,strType,ConfigurationConstants.TYPE_PRODUCT_VARIANT)
                		&& !mxType.isOfParentType(context,strType,ConfigurationConstants.TYPE_LOGICAL_FEATURE)){
                	StringList strModelSelect = new StringList();
                	strModelSelect.addElement("to["+ConfigurationConstants.RELATIONSHIP_MAIN_PRODUCT+"].from.id");
                	strModelSelect.addElement("to["+ConfigurationConstants.RELATIONSHIP_PRODUCTS+"].from.id");
                	strModelSelect.addElement("to["+ConfigurationConstants.RELATIONSHIP_MAIN_PRODUCT+"].from.physicalid");
                	strModelSelect.addElement("to["+ConfigurationConstants.RELATIONSHIP_PRODUCTS+"].from.physicalid");
                	Map modelDetais = (Map)(new DomainObject(objectId)).getInfo(context,strModelSelect);
                	strModelId = (String)modelDetais.get("to["+ConfigurationConstants.RELATIONSHIP_MAIN_PRODUCT+"].from.id");
                	strModelPhyId = (String)modelDetais.get("to["+ConfigurationConstants.RELATIONSHIP_MAIN_PRODUCT+"].from.physicalid");

                	if(strModelId==null || strModelId.equals("")){
                		strModelId = (String)modelDetais.get("to["+ConfigurationConstants.RELATIONSHIP_PRODUCTS+"].from.id");
                		strModelPhyId = (String)modelDetais.get("to["+ConfigurationConstants.RELATIONSHIP_PRODUCTS+"].from.physicalid");
                	}
                }
                %>
<%@include file="../emxUICommonHeaderEndInclude.inc"%>

<form name="ProductConfigurationCreate" method="post"
	onsubmit="moveNext(); return false" />
	<%@include file = "../common/enoviaCSRFTokenInjection.inc" %>

<input type="hidden" name="functionality" value="<xss:encodeForHTMLAttribute><%=strFunctionality%></xss:encodeForHTMLAttribute>" />
<input type="hidden" name="fromcontext" value="<xss:encodeForHTML><%=strFromContext%></xss:encodeForHTML>" />
<table border="0" cellpadding="5" cellspacing="2" width="100%">
	<tr>
		<td>
			<input type="hidden" name="hidProductId" id="hidProductId" value="<xss:encodeForHTMLAttribute><%=objectId%></xss:encodeForHTMLAttribute>" />
			<input type="hidden" name="hidModelId" id="hidModelId"/>
			<input type="hidden" name="hidDefaultRevision" value="<xss:encodeForHTMLAttribute><%=strDefaultRevision%></xss:encodeForHTMLAttribute>" />
			<input type="hidden" name="topLevelPart" id="topLevelPart" value="<xss:encodeForHTMLAttribute><%=topLevelPart%></xss:encodeForHTMLAttribute>" />
			<input type="hidden" name="derivedFromId" id="derivedFromId" value="<xss:encodeForHTML><%=derivedFromId%></xss:encodeForHTML>" />
			<input type="hidden" name="strProductContextId" id="strProductContextId" value="<xss:encodeForHTMLAttribute><%=strProductContextId%></xss:encodeForHTMLAttribute>" />

			<input type="hidden" name="strMilestoneId" id="strMilestoneId" value="<xss:encodeForHTMLAttribute><%=txtMilestone%></xss:encodeForHTMLAttribute>">

		</td>
	</tr>
	<%-- Display the input fields. --%>
	<%/*
                 *This logic defines if the name field is to be made visible to the user or not
                 *These setting are based on the global settings for each module made in the
                 *application property file.
                 */
                if (strAutoNamer.equalsIgnoreCase("False")
                        || strAutoNamer.equalsIgnoreCase("")) {

                    %>
	<tr>
		<td width="150" nowrap="nowrap" class="labelRequired"><emxUtil:i18n
			localize="i18nId">
            emxFramework.Basic.Name
        </emxUtil:i18n></td>
		<td nowrap="nowrap" class="field"><input type="text"
			name="txtProductConfigurationName" size="20"
			value="<% if(productConfiguration != null){ out.println(productConfiguration.getName());}%>"
			 onChange="setMarketingName()" />&nbsp; <%if (strAutoNamer.equalsIgnoreCase("")) {
%> <%--<input type="checkbox" name="chkAutoName"
            <%if((productConfiguration != null) &&(productConfiguration.getName().equals(""))){%>
            checked <%}%> onclick="nameDisabled()"><emxUtil:i18n
            localize="i18nId">emxProduct.Form.Label.Autoname</emxUtil:i18n>
            <input type="hidden" name="hiddenPCName" value="" />--%>
             <%}

                %></td>
	</tr>
	<%} else {

                    %>
	<input type="hidden" name="txtProductConfigurationName"
		value="<% if(productConfiguration != null){ out.println(productConfiguration.getName());}%>" />
	<%}
%>

	<tr>
		<td width="150" nowrap="nowrap" class="labelRequired"><emxUtil:i18n
			localize="i18nId">
            emxProduct.Label.Based_On
        </emxUtil:i18n></td>

		<td nowrap="nowrap" class="field">
			<input type="text" name="txtProductConfigurationProduct" size="20" readonly value="<xss:encodeForHTMLAttribute><%=strProduct%></xss:encodeForHTMLAttribute>" />
<%
			if (strFromContext.equalsIgnoreCase("fromProductConfiguration")) {
                %> <input type="button"
			name="btnProductConfigurationProduct" value="..."
			onclick="javascript:showBasedOnChooser();" />&nbsp; <%}

                %></td>
	</tr>
	<!-- added for the CR no. 371091 start-->
	<%
			//if (!(strType != null && ! "".equals(strType)) || com.matrixone.apps.domain.util.mxType.isOfParentType(context,strType,ProductLineConstants.TYPE_FEATURES))
		if (!(ProductLineCommon.isNotNull(strType)) ||ProductLineUtil.getChildrenTypes(context, ConfigurationConstants.TYPE_LOGICAL_STRUCTURES).contains(strType)
			     		|| strFromContext.equalsIgnoreCase("fromProductConfiguration"))

			{
            %>
	<tr>
		<td width="150" nowrap="nowrap" class="labelRequired"><emxUtil:i18n
			localize="i18nId">
            emxProduct.Label.Product_Context
        </emxUtil:i18n></td>
		<td nowrap="nowrap" class="field"><input type="text" name="txtProductContext" id="txtProductContext" size="20" readonly="readonly" value=""/>
            <input type="button"
			name="btnProductContext" id="btnProductContext" value="..."
			onclick="javascript:showProductContextChooser('<%=XSSUtil.encodeForJavaScript(context,objectId)%>');"%>
			<a name="ancClear" href="#ancClear" class="dialogClear"
                onclick="document.ProductConfigurationCreate.txtProductContext.value='';document.ProductConfigurationCreate.topLevelPart.value=''">
              <emxUtil:i18n localize="i18nId">emxProduct.Button.Clear</emxUtil:i18n>
            </a>  &nbsp;
		</td>
	</tr>
	<%}%>
	<!-- added for the CR no. 371091 end-->
	<tr>
		<td width="150" nowrap="nowrap" class="label"><emxUtil:i18n
			localize="i18nId">
            emxProduct.Label.Derived_From
        </emxUtil:i18n></td>

		<td nowrap="nowrap" class="field">
			<input type="text" name="txtDerivedFromProductConfiguration" id="txtDerivedFromProductConfiguration" size="20" readonly="readonly" value="<xss:encodeForHTMLAttribute><%=strDerivedFrom%></xss:encodeForHTMLAttribute>" />
			<input type="button"
			name="btnDerivedFromProductConfiguration" value="..."
			onclick="javascript:showProductConfigurationChooser('<%=XSSUtil.encodeForJavaScript(context,strModelPhyId)%>');"%>
			<a name="ancClear" href="#ancClear" class="dialogClear"
                onclick="document.ProductConfigurationCreate.txtDerivedFromProductConfiguration.value='';document.ProductConfigurationCreate.topLevelPart.value='';document.ProductConfigurationCreate.derivedFromId.value=''">
              <emxUtil:i18n localize="i18nId">emxProduct.Button.Clear</emxUtil:i18n>
            </a>  &nbsp;

		</td>
	</tr>
	<tr>
		<td width="150" nowrap="nowrap" class="label"><emxUtil:i18n
			localize="i18nId">
            emxProduct.Label.Top_Level_Part
        </emxUtil:i18n></td>

        <td nowrap="nowrap" class="field"><input type="text" name="topLevelPartDisplay" id="topLevelPartDisplay" size="20" readonly="readonly" value="<xss:encodeForHTMLAttribute><%=topLevelPartDisplay%></xss:encodeForHTMLAttribute>" /> <input type="button"
            name="btnTopLevelPart" value="..."
            onclick="javascript:showTopLevelPartChooser();" />
            <a name="ancClear" href="#ancClear" class="dialogClear"
                onclick="document.ProductConfigurationCreate.topLevelPartDisplay.value='';document.ProductConfigurationCreate.topLevelPart.value=''">
              <emxUtil:i18n localize="i18nId">emxProduct.Button.Clear</emxUtil:i18n>
            </a>
        </td>
    </tr>
    <tr>
        <td class="label" nowrap="nowrap" width="150">
            <emxUtil:i18n localize="i18nId">emxConfiguration.Form.Label.DateEffectivity</emxUtil:i18n>
        </td>
        <td nowrap="nowrap" class="field">
            <input type="text" name="txtStartEffectivity" size="20" readonly="readonly" value="<xss:encodeForHTMLAttribute><%=startEffectivity%></xss:encodeForHTMLAttribute>" />
            <input type="hidden" name="txtStartEffectivity_msvalue" value="<xss:encodeForHTMLAttribute><%=startEffValue%></xss:encodeForHTMLAttribute>" />
            <a href="javascript:showCalendar('ProductConfigurationCreate','txtStartEffectivity','')" >
             <img src="../common/images/iconSmallCalendar.gif" border="0" valign="absmiddle" name="img5" />
            </a>
            <a name="ancClear" href="#ancClear" class="dialogClear"
            onclick="document.ProductConfigurationCreate.txtStartEffectivity.value='';document.ProductConfigurationCreate.txtStartEffectivity_msvalue.value=''">
            <emxUtil:i18n localize="i18nId">emxProduct.Button.Clear</emxUtil:i18n>
            </a>
        </td>
    </tr>
    <!-- Milestone Related  Start-->
	<%
		if(showMilestoneField)
		{
            %>
	<tr>
		<td width="150" nowrap="nowrap" class="label"><emxUtil:i18n
			localize="i18nId">
            emxConfiguration.Label.Milestone
        </emxUtil:i18n></td>
		<td nowrap="nowrap" class="field"><input type="text" name="txtMilestone" id="txtMilestone" size="20" readonly="readonly" value="<xss:encodeForHTMLAttribute><%=strBuffMilestone.toString()%></xss:encodeForHTMLAttribute>">
            <input type="button"
			name="btnMilestone" id="btnMilestone" value="..."
			onclick="javascript:showMilestoneChooser('<%=XSSUtil.encodeForJavaScript(context,strModelId)%>');"%>&nbsp;
            <a name="ancClear" href="#ancClear" class="dialogClear"
                onclick="document.ProductConfigurationCreate.txtMilestone.value='';document.ProductConfigurationCreate.strMilestoneId.value=''">
              <emxUtil:i18n localize="i18nId">emxProduct.Button.Clear</emxUtil:i18n>
            </a>
		</td>
	</tr>
	<%}%>
    <!-- Milestone Related End-->

    <tr>
        <td width="150" class="label" valign="center"><emxUtil:i18n
            localize="i18nId">
            emxFramework.Basic.Description
        </emxUtil:i18n></td>
		<td class="field"><textarea name="txtProductConfigurationDescription"
			rows="5" cols="25"><xss:encodeForHTML><%if (productConfiguration != null) {%><%=productConfiguration.getDescription()%><%}%></xss:encodeForHTML></textarea>
		</td>
	</tr>
	   <tr>
        <td width="150" class="labelRequired" valign="center"><emxUtil:i18n
            localize="i18nId">
            emxProduct.Table.MarketingName
        </emxUtil:i18n></td>
        <td nowrap="nowrap" class="field"><input type="text" name="txtProductConfigurationMarketingName" id="txtProductConfigurationMarketingName" value="<xss:encodeForHTMLAttribute><% if(productConfiguration != null){%><%=productConfiguration.getMarketingName()%><%}%></xss:encodeForHTMLAttribute>" />
        </td>
    </tr>
	 <tr>
        <td width="150" class="label" valign="center"><emxUtil:i18n
            localize="i18nId">
            emxConfiguration.Label.PCCreate.MarketingText
        </emxUtil:i18n></td>
        <td class="field"><textarea name="txtProductConfigurationMarketingText" id="txtProductConfigurationMarketingText"
		rows="5" cols="25"><xss:encodeForHTML><%=strMarketingText%></xss:encodeForHTML></textarea>
        </td>

	<tr>
		<td width="150" nowrap="nowrap" class="label"><emxUtil:i18n
			localize="i18nId">
          emxFramework.Basic.Owner
        </emxUtil:i18n></td>
		<td nowrap="nowrap" class="field"><input type="text"
			name="txtProductConfigurationOwner" size="20"
			value="<% if(productConfiguration != null){ out.println(productConfiguration.getOwnerName());} else { out.println(context.getUser());}%>"
			readonly="readonly" /> <input class="button" type="button"
			name="btnProductConfigurationOwner" size="200" value="..." alt=""
			onClick="javascript:showPersonSelector();" /> <input type="hidden"
			name="hidProductConfigurationOwnerId" value="" /></td>
	</tr>
	<%/**
                 *
                 * Checks if the context user belongs to the Host Company, if yes then the SalesIntent field is displayed
                 * otherwise the SalesIntent field is hidden.
                 *
                 */
                if (ProductLineUtil.checkPersonIsHostCompanyEmployee(context,
                        context.getUser())) {

                    %>
	<tr>
		<td width="150" class="label" valign="center"><emxUtil:i18n
			localize="i18nId">
                  emxProduct.Form.Label.SalesIntent
                </emxUtil:i18n></td>
		<td nowrap="nowrap" class="field">
		<table border="0">
			<tr>
				<td><input type="radio"
					name="radProductConfigurationSalesIntentValue"
					value="<xss:encodeForHTMLAttribute><%=ConfigurationConstants.RANGE_VALUE_STANDARD_CONFIGURATION%></xss:encodeForHTMLAttribute>"
					<% if((productConfiguration != null) && (productConfiguration.getSalesIntent().equals(ConfigurationConstants.RANGE_VALUE_STANDARD_CONFIGURATION))){%>
					checked <% } else {%> checked <%}%> />
					</td>
				<td><emxUtil:i18n localize="i18nId">
                                emxProduct.Value.StandardConfiguration
                            </emxUtil:i18n></td>
			</tr>
			<tr>
				<td width="20"><input type="radio" name="radProductConfigurationSalesIntentValue" value="<xss:encodeForHTMLAttribute><%=ConfigurationConstants.RANGE_VALUE_CUSTOM_CONFIGURATION%></xss:encodeForHTMLAttribute>" <% if(productConfiguration != null){ if(productConfiguration.getSalesIntent().equals(ConfigurationConstants.RANGE_VALUE_CUSTOM_CONFIGURATION)){%>
					checked <%}}%> /></td>
				<td><emxUtil:i18n localize="i18nId">
                                emxProduct.Value.CustomConfiguration
                            </emxUtil:i18n></td>
			</tr>
		</table>
		</td>
	</tr>
	<tr>
		<td width="150" class="label" valign="center"><emxUtil:i18n
			localize="i18nId">
                    emxProduct.Form.Label.Purpose
                  </emxUtil:i18n></td>
		<td nowrap="nowrap" class="field">
		<table border="0">
			<% if (ProductLineUtil.getChildrenTypes(context, ConfigurationConstants.TYPE_LOGICAL_STRUCTURES).contains(strType)) {%>
			<tr>
				<td><input type="hidden" name="radProductConfigurationPurposeValue"
					value="Accessory"
					<% if((productConfiguration != null) && (productConfiguration.getPurpose().equals("Accessory"))){%>
					checked <% } else {%> checked <%}%> /></td>
				<td><emxUtil:i18n localize="i18nId">
                         emxProduct.Value.Accessory
                      </emxUtil:i18n></td>
			</tr>
			<%} else {%>
			<tr>
				<td width="20"><input type="radio" name="radProductConfigurationPurposeValue"
					value="Evaluation"
					<% if((productConfiguration != null) && (productConfiguration.getPurpose().equals("Evaluation"))){%>
					checked <% } else {%> checked <%}%> /></td>
				<td><emxUtil:i18n localize="i18nId">
                          emxProduct.Value.Evaluation
                        </emxUtil:i18n></td>
			</tr>
			<tr>
				<td><input type="radio" name="radProductConfigurationPurposeValue"
					value="Order"
					<% if(productConfiguration != null){ if(productConfiguration.getPurpose().equals("Order") || isOrder){%>
					checked <%}}%> /></td>
				<td><emxUtil:i18n localize="i18nId">
                          emxProduct.Value.Order
                        </emxUtil:i18n></td>
			</tr>
			<tr>
				<td><input type="radio" name="radProductConfigurationPurposeValue"
					value="Accessory"
					<% if(productConfiguration != null){ if(productConfiguration.getPurpose().equals("Accessory") || isAccessory){%>
					checked <%}}%> /></td>
				<td><emxUtil:i18n localize="i18nId">
                          emxProduct.Value.Accessory
                        </emxUtil:i18n></td>
			</tr>
			<%}

                %>
		</table>
		</td>
	</tr>
	<%} else {

                %>
	<input type="hidden" name="radProductConfigurationSalesIntentValue" value="<xss:encodeForHTMLAttribute><%=ConfigurationConstants.RANGE_VALUE_CUSTOM_CONFIGURATION%></xss:encodeForHTMLAttribute>" />
	<input type="hidden" name="radProductConfigurationPurposeValue"
		value="Order" />
	<%}

                if (!bPolicyAwareness) {
                    if (policyList.size() > 1) {
%>

	<tr>
		<td width="150" class="label" valign="top"><emxUtil:i18n
			localize="i18nId">
                    emxFramework.Basic.Policy
                </emxUtil:i18n></td>
		<td class="field"><select name="txtProductConfigurationPolicy" id="txtProductConfigurationPolicy">
			<framework:optionList optionMapList="<xss:encodeForHTMLAttribute><%= policyList%></xss:encodeForHTMLAttribute>"
				optionKey="<xss:encodeForHTMLAttribute><%=DomainConstants.SELECT_NAME%></xss:encodeForHTMLAttribute>"
				valueKey="<xss:encodeForHTMLAttribute><%=DomainConstants.SELECT_NAME%></xss:encodeForHTMLAttribute>" selected="" />
		</select></td>
	</tr>
	<%} else {
%>
	<tr>
		<td width="150" class="label" valign="top"><emxUtil:i18n
			localize="i18nId">
                    emxFramework.Basic.Policy
                </emxUtil:i18n></td>
		<td class="field"><%=XSSUtil.encodeForHTML(context,i18nPolicy)%> <input type="hidden"
			name="txtProductConfigurationPolicy" id="txtProductConfigurationPolicy" value="" /></td>
	</tr>
	<%}
                } else {

                %>
	<input type="hidden" name="txtProductConfigurationPolicy" id="txtProductConfigurationPolicy" value="" />
	<% } %>

	<input type="hidden" name="txtParentFrameUrl" id="txtParentFrameUrl" value="" />
</table>
 <!-- NextGen UI Adoption : Commented below image-->
 <!-- Modified for removing unnecessary link on Page  -->
 <!-- <input type="image" value="" height="1" width="1" border="0" /></form>  -->
<%} catch (Exception e) {
                session.putValue("error.message", e.getMessage());
            }

            %>

<script language="javascript"
	src="../common/scripts/emxJSValidationUtil.js"></script>
<script language="javascript" src="../common/scripts/emxUIModal.js"></script>


<script language="javascript" type="text/javaScript">
        var  formName = document.ProductConfigurationCreate;
        var autoNameValue = "<%= XSSUtil.encodeForJavaScript(context,strAutoNamer) %>";

        function valueCheck() {
          if(autoNameValue == '') {
            if (document.ProductConfigurationCreate.chkAutoName.checked) {
              document.ProductConfigurationCreate.txtProductConfigurationDescription.focus();
            }
          }
        }

        function nameDisabled() {
          if(document.ProductConfigurationCreate.chkAutoName != null) {
            if (document.ProductConfigurationCreate.chkAutoName.checked){
            document.ProductConfigurationCreate.txtProductConfigurationName.value="";
            document.ProductConfigurationCreate.chkAutoName.value="true";
            document.ProductConfigurationCreate.txtProductConfigurationDescription.focus();
            } else {
            document.ProductConfigurationCreate.txtProductConfigurationName.focus();
            }
        }
        }

        function closeSlideInDialog() {
        	getTopWindow().document.getElementById("rightSlideIn").style.width = "";
	     	getTopWindow().closeSlideInDialog();
        }

        function moveNext() {

            if( !validateForm() ) {
	     		return;
	     	}

	        if(isDuplicateName()) {
	     		return;
	     	}

        	if(isTopLevelPart()) {
        		document.ProductConfigurationCreate.topLevelPart.value = '';;
        	    document.ProductConfigurationCreate.topLevelPartDisplay.value= '';
        		return;
        	}

	     	if(!hasValidCFs()){
	     		return;
	     	}

	     	closeSlideInDialog();
	     	//document.ProductConfigurationCreate.target="_top";
			//document.ProductConfigurationCreate.target="detailsDisplay";
			var parentFrameObj = findFrame(getTopWindow(),"detailsDisplay");
			document.ProductConfigurationCreate.target="detailsDisplay";
			if(parentFrameObj == null)
			{
				parentFrameObj = findFrame(getTopWindow(),"content");
				document.ProductConfigurationCreate.target="content";
			}

	     	var topLevelPart = document.ProductConfigurationCreate.topLevelPart.value;
    	    var topLevelPartDisplay = document.ProductConfigurationCreate.topLevelPartDisplay.value;
    	    var fromcontext = document.ProductConfigurationCreate.fromcontext.value;

            var parentWindowHref = parentFrameObj.location.href;

            //parentWindowHref = encodeURIComponent(parentWindowHref);

            document.ProductConfigurationCreate.txtParentFrameUrl.value = parentWindowHref;

            document.ProductConfigurationCreate.action="../configuration/ProductConfigurationCreateNextPreProcess.jsp?mode=featureSelect&functionality=ProductConfigurationCreateFlatViewContentFSInstance&jsTreeID=<%=XSSUtil.encodeForURL(context,jsTreeId)%>&relId=<%=XSSUtil.encodeForURL(context,relId)%>&topLevelPart=" + topLevelPart + "&topLevelPartDisplay=" + topLevelPartDisplay+"&fromcontext="+fromcontext;

        	document.ProductConfigurationCreate.submit();

        }

	// Check wheather PC name is duplicate
	function isDuplicateName() {
        var isDuplicateName = false;
		var sTextValue =  trimWhitespace(document.ProductConfigurationCreate.txtProductConfigurationName.value);
		if(sTextValue != null && sTextValue != "")
		{

			var encodedText = encodeURIComponent(sTextValue);
			var checkForDuplicatePCName = "checkForDuplicatePCName";
			var url="../configuration/ProductConfigurationCreateNextPreProcess.jsp?mode="+checkForDuplicatePCName+ "&name=" +encodedText+ "&randomCheckForIe=" +Math.random();
			var jsonStringVar = emxUICore.getData(url);
			var dupInfo = emxUICore.parseJSON(jsonStringVar);
			isDuplicateName = dupInfo["duplicate"];
		}
		if(isDuplicateName) {
			var msg = "<%=i18nNow.getI18nString("emxProduct.Message.PCNameNotUnique", bundle,acceptLanguage)%> ";
        	alert(msg);
		}
		return isDuplicateName;
	}

	function hasValidCFs() {
            var hasValidCFs = false;
		    var hostUrl = this.getHostURL();
		    var vRes;
		    <%
		    String parenProductId = emxGetParameter(request,"parentContextId");
		          	%>

        	var contextid 	= document.ProductConfigurationCreate.hidProductId.value;/* trimWhitespace(document.ProductConfigurationCreate.strProductContextId.value); */
        	/* contextid=document.ProductConfigurationCreate.hidProductId.value;
			if(contextid==""){
        		contextid=document.ProductConfigurationCreate.hidProductId.value;
        	} */
        	var strStartEffectivityDate 	 = trimWhitespace(document.ProductConfigurationCreate.txtStartEffectivity.value);
        	var strMilestoneId="";
        	 if(<%=showMilestoneField%>){
			 strMilestoneId = trimWhitespace(document.ProductConfigurationCreate.strMilestoneId.value);
        	 }
			var configCriteria = "";

			if(strStartEffectivityDate != "" || strMilestoneId != "")
			{
				//configCriteria = "{date:{startDate:\"" + strStartEffectivityDate + "\"}, milestone:{id:\""+ strMilestoneId + "\"}, productState:{productRev:\"\"}}";
				configCriteria = "{\"date\":{\"startDate\":\"" + strStartEffectivityDate + "\"}, \"milestone\":{\"id\":\""+ strMilestoneId + "\"}, \"productState\":{\"productRev\":\"\"}}"
			}

			if(configCriteria == "")
				configCriteria = "{}";

		    var url=hostUrl + "/resources/cfg/configurator/checkValidCFs/"+contextid;


        	url = url + "?parentContextId="+'<%=XSSUtil.encodeForJavaScript(context,parenProductId)%>'



        	//var queryString = "configurationCriteria="+encodeURIComponent(configurationCriteria)+"";
           // var vRes = emxUICore.getDataPost(url,queryString);
			UWA.Data.request(url, {
						method : "POST",
						type : "json",
						async : false,
						onComplete : h,
						onFailure : j,
						data : {
							configurationCriteria : configCriteria,
							}
					});
            function h(o) {
            	vRes = o;
				console.log("Passed");
			}
			function j(o) {
				console.log("failed");
				//alert(arguments[1].message)
			}
			/* emxUICore.parseJSON(vRes) */
            //var dupInfo =JSON.parse(vRes);

			/* if(vRes.dictionary.features.length > 0){
				hasValidCFs = true;
			} */

			if(vRes.hasValidCF){
				hasValidCFs = true;
			}

            //var dupInfo = emxUICore.parseJSON(vRes);

			//if(dupInfo.dictionary.features.length > 0){
			//	hasValidCFs = true;
		//	}

		if(!hasValidCFs) {
			var msg = "<%=i18nNow.getI18nString("emxProduct.Message.NoValidCF", bundle,acceptLanguage)%> ";
        	alert(msg);
		}
		return hasValidCFs;
	}

	function getHostURL()
	{
		var contextname = location.pathname.substring(0,location.pathname.indexOf('/',1));

		if (!window.location.origin) {
			window.location.origin = window.location.protocol + "//" +
											window.location.hostname +
											(window.location.port ? ':' + window.location.port: '');

			return window.location.origin.concat(contextname);
		}
		else
			return location.origin.concat(contextname);

				}

	// Validates the form fields
	function validateForm() {
                var isValidForm = true;
                var msg = "";
                var sTextValue =  trimWhitespace(document.ProductConfigurationCreate.txtProductConfigurationName.value);
                document.ProductConfigurationCreate.txtProductConfigurationName.value = sTextValue;
                var topLevelPart = document.ProductConfigurationCreate.topLevelPart.value;
                var sStartDateEff =  trimWhitespace(document.ProductConfigurationCreate.txtStartEffectivity_msvalue.value);

    	    	//XSSOK
    	    	if (!(<%=strAutoNamer.equalsIgnoreCase("True")%> == true)) {
     	    	//XSSOK
    	    	if (<%=strAutoNamer.equalsIgnoreCase("False")%> == true) {
		     if (isValidForm) {
                        var fieldName = "<%=i18nNow.getI18nString("emxFramework.Basic.Name", bundle,acceptLanguage)%> ";
                        var field = document.ProductConfigurationCreate.txtProductConfigurationName;
		        isValidForm = basicValidation(document.ProductConfigurationCreate,field,fieldName,true,true,true,false,false,false,false);
                      }
                }else {
                  if (true) {
		          if (isValidForm) {
                           //     var fieldName = "<%=i18nNow.getI18nString("emxFramework.Basic.Name", bundle,acceptLanguage)%> ";
                           //     var field = document.ProductConfigurationCreate.txtProductConfigurationName;
		                //isValidForm = basicValidation(document.ProductConfigurationCreate,field,fieldName,true,true,true,false,false,false,false);
                             //   document.ProductConfigurationCreate.hiddenPCName.value = document.ProductConfigurationCreate.txtProductConfigurationName.value;
                            }
                    }
                }
              }

		    if (isValidForm && (!isValidLength(document.ProductConfigurationCreate.hidProductId.value,1,100) || (document.ProductConfigurationCreate.hidProductId.value == "null")) ) {
                    msg = "<%=i18nNow.getI18nString("emxProduct.Alert.ReqProductAlert", bundle, acceptLanguage)%>";
                    document.ProductConfigurationCreate.btnProductConfigurationProduct.focus();
                    alert(msg);
		                isValidForm = false;
        }
			if (isValidForm) {
						
			var fieldProductContext = document.ProductConfigurationCreate.txtProductContext;
			if(fieldProductContext != undefined || fieldProductContext != null) {
				if(!isValidLength(document.ProductConfigurationCreate.txtProductContext.value,1,100) || (
					document.ProductConfigurationCreate.txtProductContext.value == "null")) {
					msg = "<%=i18nNow.getI18nString("emxProduct.Alert.ReqProductContextAlert", bundle, acceptLanguage)%>";
					document.ProductConfigurationCreate.txtProductContext.focus();
					alert(msg);
					isValidForm = false;
				}
			}
		}


  //validation for special chars in the description field - The sixth(true/false) and last parameter 'checkBadChars' specifies which characters have to be blocked (all bad chars, common illegal characters are now Restricted Characters)
		    if (isValidForm) {
                    //var fieldName = "<%=i18nNow.getI18nString("emxFramework.Basic.Description", bundle,acceptLanguage)%> ";
                    //var field = document.ProductConfigurationCreate.txtProductConfigurationDescription;
			    //isValidForm = basicValidation(document.ProductConfigurationCreate,field,fieldName,false,false,false,false,false,false,checkBadChars);

			    // isValidForm = basicValidation(document.ProductConfigurationCreate,field,fieldName,true,true,true,false,false,false,false);
                }
		    if (isValidForm)
        {
            var fieldName = "<%=i18nNow.getI18nString("emxProduct.Table.MarketingName", bundle,acceptLanguage)%> ";
            var field = document.ProductConfigurationCreate.txtProductConfigurationMarketingName;
		        isValidForm = basicValidation(document.ProductConfigurationCreate,field,fieldName,true,true,true,false,false,false,false);
        }

		    return isValidForm;
        }

        function closeWindow() {
                document.ProductConfigurationCreate.action="../configuration/ProductConfigurationResponse.jsp?mode=cleanupsession"
                document.ProductConfigurationCreate.submit();
                getTopWindow().window.closeWindow();
            }


        function showBasedOnChooser() {
          //This function is for popping the Product chooser.
           /*modified for the CR no. 371091
             added one more param 'value' for facilitating the logic (in searchUtil.jsp)for
             Product Context i.e. chooser button enable, disable as per the 'Based On'
             value choosen by the user for the PC creation in independent context
           */
		  showChooser('../common/emxFullSearch.jsp?field=TYPES=type_Products,type_LogicalFeature:Type!=type_MasterFeature:CURRENT!=policy_Product.state_Obsolete,policy_LogicalFeature.state_Obsolete&table=PLCSearchProductsTable&selection=single&submitAction=refreshCaller&hideHeader=true&HelpMarker=emxhelpfullsearch&mode=Chooser&chooserType=CustomChooser&value=BasedOn&fieldNameActual=hidProductId&fieldNameDisplay=txtProductConfigurationProduct&formName=ProductConfigurationCreate&showInitialResults=false&frameName=pagecontent&suiteKey=Configuration&submitURL=../configuration/SearchUtil.jsp', 850, 630);
        }
   		//added for the CR no. 371091 start
        function showProductContextChooser(strFeatureId) {

        if(document.ProductConfigurationCreate.txtProductConfigurationProduct.value == "")
 	 {
 	    msg = "<%=i18nNow.getI18nString("emxProduct.Alert.ReqProductAlert", bundle, acceptLanguage)%>";
 	    document.ProductConfigurationCreate.btnProductConfigurationProduct.focus();
 	    alert(msg);
 	 }else{
        if(strFeatureId == "null")
        {

        contextBusId = document.ProductConfigurationCreate.hidProductId.value;

        }
        else
        {
        contextBusId = strFeatureId

        }

                 //This function is for popping the Product context chooser.
		  showChooser('../common/emxFullSearch.jsp?field=TYPES=type_Products,type_Product Variant:CURRENT!=policy_Product.state_Obsolete:PRODUCT_CONTEXT_ID=' + contextBusId +'&table=PLCSearchProductsTable&selection=single&submitAction=refreshCaller&hideHeader=true&showInitialResults=false&HelpMarker=emxhelpfullsearch&mode=Chooser&chooserType=CustomChooser&fieldNameActual=strProductContextId&fieldNameDisplay=txtProductContext&formName=ProductConfigurationCreate&frameName=pagecontent&suiteKey=Configuration&submitURL=../configuration/SearchUtil.jsp', 850, 630);
        }
        }
         //added for the CR no. 371091 end


        function showProductConfigurationChooser(modelId) //added the variable fieldNameDisplay
		{

	 	 	if(document.ProductConfigurationCreate.txtProductContext != 'undefined' && document.ProductConfigurationCreate.txtProductContext != null &&document.ProductConfigurationCreate.txtProductContext.value == "")
	 	 	{
	 	    	msg = "<%=i18nNow.getI18nString("emxProduct.Alert.ReqProductContextAlert", bundle, acceptLanguage)%>";
	 	    	if(document.ProductConfigurationCreate.btnProductContext != 'undefined' && document.ProductConfigurationCreate.btnProductContext != null)
	 	    	{
	 	    		document.ProductConfigurationCreate.btnProductContext.focus();
	 	    		alert(msg);
	 	    	}
	 	    }

 			else
 			{
			 	var contextBusId = document.ProductConfigurationCreate.hidProductId.value;
			 	var prodContextId = document.ProductConfigurationCreate.strProductContextId.value;
				var modelPhyId ="";
 					if(modelId == "" || modelId == "null"){
 						modelPhyId = document.ProductConfigurationCreate.hidModelId.value;
					 } else{
					    modelPhyId = modelId;
					 }
                                 var isTypeLF = "<%=XSSUtil.encodeForJavaScript(context,isLFType)%>";
			 if(prodContextId == "null" || prodContextId == "")
			 {
              showChooser('../common/emxFullSearch.jsp?field=TYPES=type_ProductConfiguration:BASED_ON_MODEL_PID='+modelPhyId+'&table=FTRSearchProductConfigurationsTable&showInitialResults=false&selection=single&submitAction=refreshCaller&hideHeader=true&HelpMarker=emxhelpfullsearch&mode=Chooser&chooserType=CustomChooser&fieldNameActual=derivedFromId&fieldNameDisplay=txtDerivedFromProductConfiguration&formName=ProductConfigurationCreate&frameName=pagecontent&suiteKey=Configuration&submitURL=../configuration/SearchUtil.jsp&contextBusId=' + contextBusId, 850, 630);

             }else
             {
				if(isTypeLF == "true" || contextBusId != prodContextId)	
				{
					showChooser('../common/emxFullSearch.jsp?field=TYPES=type_ProductConfiguration:BASED_ON_ID='+contextBusId+'&prodContextId='+prodContextId+'&table=FTRSearchProductConfigurationsTable&showInitialResults=false&selection=single&submitAction=refreshCaller&hideHeader=true&HelpMarker=emxhelpfullsearch&mode=Chooser&chooserType=CustomChooser&fieldNameActual=derivedFromId&fieldNameDisplay=txtDerivedFromProductConfiguration&formName=ProductConfigurationCreate&frameName=pagecontent&suiteKey=Configuration&submitURL=../configuration/SearchUtil.jsp&contextBusId=' + contextBusId, 850, 630);
				}else {
					
					showChooser('../common/emxFullSearch.jsp?field=TYPES=type_ProductConfiguration:BASED_ON_MODEL_PID='+modelPhyId+'&prodContextId='+prodContextId+'&table=FTRSearchProductConfigurationsTable&showInitialResults=false&selection=single&submitAction=refreshCaller&hideHeader=true&HelpMarker=emxhelpfullsearch&mode=Chooser&chooserType=CustomChooser&fieldNameActual=derivedFromId&fieldNameDisplay=txtDerivedFromProductConfiguration&formName=ProductConfigurationCreate&frameName=pagecontent&suiteKey=Configuration&submitURL=../configuration/SearchUtil.jsp&contextBusId=' + contextBusId, 850, 630);
				}
             }

        }

      }
		 function showTopLevelPartChooser() {
			if(document.ProductConfigurationCreate.txtProductConfigurationProduct.value == "")
 	 {
 	    msg = "<%=i18nNow.getI18nString("emxProduct.Alert.ReqProductAlert", bundle, acceptLanguage)%>";
 	   document.ProductConfigurationCreate.btnProductConfigurationProduct.focus();
 	    alert(msg);
 	 }
 	 else{

            var contextBusId = document.ProductConfigurationCreate.hidProductId.value;
            //Code change for IR-018831V6R2011- excluded Parts which has "Configured Part" Policy
			var partSearchURL = "../common/emxFullSearch.jsp?field=TYPES=type_Part:HAS_EBOM_CONNECTED=false:IS_TOP_LEVEL_PART=false:CURRENT!=policy_ECPart.state_Obsolete:POLICY!=policy_StandardPart,policy_DevelopmentPart,policy_ConfiguredPart&ParentProductId="+contextBusId+"&table=PLCSearchPartsTable&showInitialResults=false&selection=single&submitAction=refreshCaller&hideHeader=true&HelpMarker=emxhelpfullsearch&mode=Chooser&chooserType=CustomChooser&fieldNameActual=topLevelPart&fieldNameDisplay=topLevelPartDisplay&formName=ProductConfigurationCreate&frameName=pagecontent&suiteKey=Configuration&submitURL=../configuration/SearchUtil.jsp";

			showChooser(partSearchURL, 850, 630);
			}
		 }
        // Replace vault dropdown box with vault chooser.
            var txtVault = null;
            var bVaultMultiSelect = false;
            var strTxtVault = "document.forms['ProductConfigurationCreate'].txtProductConfigurationVault";

            function showVaultSelector() {
                //This function is for popping the Vault chooser.
                txtVault = eval(strTxtVault);
    showChooser('../common/emxVaultChooser.jsp?fieldNameActual=txtProductConfigurationVault&fieldNameDisplay=txtProductConfigurationVaultDisplay&incCollPartners=false&multiSelect=false');
        }

        function showPersonSelector()
		{
			var objCommonAutonomySearch = new emxCommonAutonomySearch();
			objCommonAutonomySearch.txtType = "type_Person";
			objCommonAutonomySearch.selection = "single";
			objCommonAutonomySearch.onSubmit = "getTopWindow().getWindowOpener().submitAutonomySearchOwner";
			objCommonAutonomySearch.open();
       }
	 function submitAutonomySearchOwner(arrSelectedObjects)
	{
		var objForm = document.forms["ProductConfigurationCreate"];
		var hiddenElement = objForm.elements["hidProductConfigurationOwnerId"];
		var displayElement = objForm.elements["txtProductConfigurationOwner"];

		for (var i = 0; i < arrSelectedObjects.length; i++)
		{
			var objSelection = arrSelectedObjects[i];
			hiddenElement.value = objSelection.name;
			displayElement.value = objSelection.name;
			break;
      }
            }

	 function setMarketingName()
	 {
		 var  formName = document.ProductConfigurationCreate
		 var pcName = formName.txtProductConfigurationName.value;
		 if(pcName != "")
		  formName.txtProductConfigurationMarketingName.value = pcName;
	 }
        function showMilestoneChooser(){
            var strModelId = "<%=XSSUtil.encodeForJavaScript(context,strModelId)%>";
            var txtBasedONID = document.getElementById("hidProductId").value;
            if((strModelId == "" ||strModelId == "null") && txtBasedONID !=""){
            var getModelID = "getModelID";
			var url="../configuration/ProductConfigurationCreateNextPreProcess.jsp?mode="+getModelID+ "&txtBasedONID="+txtBasedONID+ "&randomCheckForIe=" +Math.random();
			var jsonStringVar = emxUICore.getData(url);
			var dupInfo = emxUICore.parseJSON(jsonStringVar);
			strModelId = dupInfo["strModelId"];
            }
   		  showChooser('../common/emxFullSearch.jsp?field=TYPES=type_Milestone&&includeOIDprogram=emxProductConfiguration:includeMilestones&strModelId='+strModelId+'&table=CFFMilestoneDefinitionTable&selection=single&submitAction=refreshCaller&hideHeader=true&showInitialResults=true&HelpMarker=emxhelpfullsearch&mode=Chooser&chooserType=Milestone&fieldNameActual=strMilestoneId&fieldNameDisplay=txtMilestone&formName=ProductConfigurationCreate&frameName=pagecontent&suiteKey=Configuration&submitURL=../configuration/SearchUtil.jsp', 850, 630);
        }

        function doneAction()
        {
			if( !validateForm() ) {
        		return;
        	}

        	// first validate the form fields
        	if(isDuplicateName()) {
        		return;
        	}
        	if(isTopLevelPart()) {
        		document.ProductConfigurationCreate.topLevelPart.value = '';;
        	    document.ProductConfigurationCreate.topLevelPartDisplay.value= '';
        		return;
        	}

			if(!hasValidCFs()){
	     		return;
	     	}

        	<%
        	String strAction = emxGetParameter(request,"strAction");
        	String contextId = emxGetParameter(request,"contextId");
        	%>
        	var strAction 			= '<%=XSSUtil.encodeForJavaScript(context,strAction)%>';
        	var contextid 			= '<%=XSSUtil.encodeForJavaScript(context,contextId)%>';
        	var strName				= document.ProductConfigurationCreate.txtProductConfigurationName.value;
        	var strMarketingName	= document.ProductConfigurationCreate.txtProductConfigurationMarketingName.value;
        	var strMarketingText	= document.ProductConfigurationCreate.txtProductConfigurationMarketingText.value;
        	var strRevision			= document.ProductConfigurationCreate.hidDefaultRevision.value;

        	var strDescription		= document.ProductConfigurationCreate.txtProductConfigurationDescription.value;
        	var strDerivedFromId	= document.ProductConfigurationCreate.derivedFromId.value;
        	var topLevelPart		= document.ProductConfigurationCreate.topLevelPart.value;

        	var strParentProductId	= document.ProductConfigurationCreate.strProductContextId.value;

        	//IR-564966-3DEXPERIENCER2019x

        	 var elemSaleslen = document.ProductConfigurationCreate.radProductConfigurationSalesIntentValue.length;
         	var strSalesIntent = "";
         	if(elemSaleslen > 1) {
         		strSalesIntent		= getRadioCheckedValue('radProductConfigurationSalesIntentValue');
         	}
         	else
         		strSalesIntent		= document.ProductConfigurationCreate.radProductConfigurationSalesIntentValue.value;


         	var purposeLen = document.ProductConfigurationCreate.radProductConfigurationPurposeValue.length;
         	var strPurpose = "";
         	if(purposeLen > 1){
         		strPurpose		= getRadioCheckedValue('radProductConfigurationPurposeValue');
         	}
         	else
         		strPurpose		= document.ProductConfigurationCreate.radProductConfigurationPurposeValue.value;

        	var strStartEffectivityDate 	 = document.ProductConfigurationCreate.txtStartEffectivity.value;
        	var strProductConfigurationOwner = document.ProductConfigurationCreate.txtProductConfigurationOwner.value;
        	var strMilestoneId 				 = document.ProductConfigurationCreate.strMilestoneId.value;
        	var basedOnId		 			= document.ProductConfigurationCreate.hidProductId.value;
        	var pcId ='';

        	if(contextid == "null")
        		contextid = basedOnId;
        	var pcParams = {
        			"contextid":contextid,
        			"strName":strName,
        			"strProductConfigurationOwner":strProductConfigurationOwner,
        			"strMarketingName":strMarketingName,
        			"strMarketingText":strMarketingText,
        			"strRevision":strRevision,
        			"strDescription":strDescription,
        			"strDerivedFromId":strDerivedFromId,
        			"strTopLevelPart":topLevelPart,
        			"strPolicy":'',
        			"strParentProductId":strParentProductId,
        			"strVault":'',
        			"strSalesIntent":strSalesIntent,
        			"strPurpose":strPurpose,
        			"strStartEffectivityDate":strStartEffectivityDate,
        			"strMilestoneId":strMilestoneId,
        			"pcId":pcId,
        			"strListPriceValue" : '0.0',
        			"strAction":strAction
        	};

        	var emptyPcCreated = function (result){
            	if(result == true){
                	closeSlideInDialog();
                	var parentFrameObj = findFrame(getTopWindow(),"detailsDisplay");
        			if(parentFrameObj == null)
        			{
        				parentFrameObj = findFrame(getTopWindow(),"content");
        			}
        			parentFrameObj.location.href  = parentFrameObj.location.href;
            	}else{
            		return;
            	}
        	}

        	createEmptyProductConfiguration(JSON.stringify(pcParams),emptyPcCreated);


        }


        function getRadioCheckedValue(radio_name)
        {
           var oRadio = document.ProductConfigurationCreate[radio_name];

           for(var i = 0; i < oRadio.length; i++)
           {
              if(oRadio[i].checked)
              {
                 return oRadio[i].value;
              }
           }
        }

        function isTopLevelPart()
        {
	        var isTopLevelPart = false;
	        var partName = document.ProductConfigurationCreate.topLevelPartDisplay.value;
			var sPartId =  trimWhitespace(document.ProductConfigurationCreate.topLevelPart.value);
			if(sPartId != null && sPartId != "")
			{
				var checkForTopLevelPartRel= "toplevelPartRel";
				var url="../configuration/ProductConfigurationCreateNextPreProcess.jsp?mode="+checkForTopLevelPartRel+ "&toplevelpartid="+sPartId+ "&randomCheckForIe=" +Math.random();
				var jsonStringVar = emxUICore.getData(url);
				var relInfo = emxUICore.parseJSON(jsonStringVar);
				isTopLevelPart = relInfo["toplevel"];
			}
			if(isTopLevelPart) {
				var msg = partName + " : " + "<%=i18nNow.getI18nString("emxProductConfiguration.TopLevelPart.Exists", bundle,acceptLanguage)%> ";
	        	alert(msg);
			}
			return isTopLevelPart;

        }
    </script>

<%@include file="../common/emxNavigatorBottomErrorInclude.inc"%>
<%
} else {
%>
<script language = "JavaScript">
    alert("<%=XSSUtil.encodeForJavaScript(context,strRootNodeErrorMsg)%>");
    window.parent.closeWindow();
</script>
<%
}
%>
