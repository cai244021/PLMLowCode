<%--  emxComponentsPeopleDetails.jsp   -   Display Details of BusinessUnits 

   Copyright (c) 1992-2020 Dassault Systemes.All Rights Reserved.
--%>
<!DOCTYPE html>
<%@page import="com.matrixone.apps.domain.util.EnoviaBrowserUtility.Browsers"%>
<%@include file = "emxComponentsDesignTopInclude.inc"%>
<%@include file = "emxComponentsVisiblePageInclude.inc"%>
<%@include file = "../emxUICommonHeaderBeginInclude.inc"%>

<%@ include file = "../emxJSValidation.inc" %>

<%
  com.matrixone.apps.common.Person personBean = (com.matrixone.apps.common.Person)DomainObject.newInstance(context,
                      DomainConstants.TYPE_PERSON);
  boolean isMCCELVInstalled = FrameworkUtil.isSuiteRegistered(context,"appVersionELVDataManagementOption",false,null,null);
  String jsTreeID         = emxGetParameter(request,"jsTreeID");
  String suiteKey         = emxGetParameter(request,"suiteKey");
  String personId         = emxGetParameter(request,"objectId"); // this is the person object id
  // added to refresh the summary page with error message if meeting username & passwd not there
  String errorMessage     = emxGetParameter(request,"errorMessage");
  String strLanguage      = request.getHeader("Accept-Language");

  // Error if the person id is not passed in.
  if (personId == null) {
    throw new MatrixException(ComponentsUtil.i18nStringNow("emxComponents.Common.MatrixException",strLanguage));
  }
  
  if (personId.indexOf("~")!= -1) {
      personId = personId.substring(0,personId.lastIndexOf("~"));      
    }

  // load lookup strings
  String sFirstName       = PropertyUtil.getSchemaProperty(context,"attribute_FirstName");
  String sMiddleName      = PropertyUtil.getSchemaProperty(context,"attribute_MiddleName");
  String sLastName        = PropertyUtil.getSchemaProperty(context,"attribute_LastName");
  String sWorkPhoneNumber = PropertyUtil.getSchemaProperty(context,"attribute_WorkPhoneNumber");
  String sHomePhoneNumber = PropertyUtil.getSchemaProperty(context,"attribute_HomePhoneNumber");
  String sPagerNumber     = PropertyUtil.getSchemaProperty(context,"attribute_PagerNumber");
  String sFaxNumber       = PropertyUtil.getSchemaProperty(context,"attribute_FaxNumber");
  String sEmailAddress    = PropertyUtil.getSchemaProperty(context,"attribute_EmailAddress");
  String sWebSite         = PropertyUtil.getSchemaProperty(context,"attribute_WebSite");

  String sLoginType       = PropertyUtil.getSchemaProperty(context,"attribute_LoginType");
  String sHostMeetings    = PropertyUtil.getSchemaProperty(context,"attribute_HostMeetings");

  String sAddress         = PropertyUtil.getSchemaProperty(context,"attribute_Address");
  String sCity            = PropertyUtil.getSchemaProperty(context,"attribute_City");
  String sStateRegion     = PropertyUtil.getSchemaProperty(context,"attribute_StateRegion");
  String sPostalCode      = PropertyUtil.getSchemaProperty(context,"attribute_PostalCode");
  String sCountry         = PropertyUtil.getSchemaProperty(context,"attribute_Country");

  String strAbsenceStartDate    = PropertyUtil.getSchemaProperty(context,"attribute_AbsenceStartDate");
  String strAbsenceEndDate      = PropertyUtil.getSchemaProperty(context,"attribute_AbsenceEndDate");
  String strAbsenceDelegate     = PropertyUtil.getSchemaProperty(context,"attribute_AbsenceDelegate");

  String sJTViewerType    = PropertyUtil.getSchemaProperty(context,"attribute_JTViewerType");

  String sWorkPlace       = PropertyUtil.getSchemaProperty(context,"relationship_WorkPlace");
  String sLocation        = PropertyUtil.getSchemaProperty(context,"type_Location");
  String sTypePerson      = PropertyUtil.getSchemaProperty(context,"type_Person");
  String sBusinessUnit    = PropertyUtil.getSchemaProperty(context,"type_BusinessUnit");
  String strCompanyType   = PropertyUtil.getSchemaProperty(context, "type_Company");
  String sRelBusUnitEmp   = PropertyUtil.getSchemaProperty(context,"relationship_Member");

  String sNotCompRep      = ComponentsUtil.i18nStringNow("emxComponents.Common.No",strLanguage);
  String sRelCompRep      = ComponentsUtil.i18nStringNow("emxComponents.Common.Yes",strLanguage);

  //Added for Organization Person Feature 
  String strMailCodeAttr			  = PropertyUtil.getSchemaProperty(context, "attribute_MailCode");
  String strTitleAttr				  = PropertyUtil.getSchemaProperty(context, "attribute_Title");
  String JF_Line_Manager				  = PropertyUtil.getSchemaProperty(context, "attribute_JF_Line_Manager");//add by caipan
  String sIMDSContactID         = PropertyUtil.getSchemaProperty(context,"attribute_IMDSContactID");
  boolean userflag = false;
  //Determine if we should use printer friendly version

  boolean isPrinterFriendly = false;
  String printerFriendly    = emxGetParameter(request, "PrinterFriendly");

  if (printerFriendly != null && "true".equals(printerFriendly) ) {
     isPrinterFriendly = true;
  }

	boolean isMSIE = true;
  if(!EnoviaBrowserUtility.is(request,Browsers.IE)) {
    isMSIE = false;
  }
  
  //String for internationalization for Unknown
  Locale locale1					    = context.getLocale();
  String strUnknownFirstName            = EnoviaResourceBundle.getFrameworkStringResourceProperty(context, "emxFramework.Default.First_Name", locale1);
  String strUnknownLastName             = EnoviaResourceBundle.getFrameworkStringResourceProperty(context, "emxFramework.Default.Last_Name", locale1);
  String strUnknownMiddleName           = EnoviaResourceBundle.getFrameworkStringResourceProperty(context, "emxFramework.Default.Middle_Name", locale1);
  String strUnknownWorkPhoneNumber      = EnoviaResourceBundle.getFrameworkStringResourceProperty(context, "emxFramework.Default.Work_Phone_Number", locale1);
  String strUnknownHomePhoneNumber      = EnoviaResourceBundle.getFrameworkStringResourceProperty(context, "emxFramework.Default.Home_Phone_Number", locale1);
  String strUnknownPagerNumber          = EnoviaResourceBundle.getFrameworkStringResourceProperty(context, "emxFramework.Default.Pager_Number", locale1);
  String strUnknownFaxNumber            = EnoviaResourceBundle.getFrameworkStringResourceProperty(context, "emxFramework.Default.Fax_Number", locale1);
  String strUnknownEmailAddress         = EnoviaResourceBundle.getFrameworkStringResourceProperty(context, "emxFramework.Default.Email_Address", locale1);
  String strUnknownAddress              = EnoviaResourceBundle.getFrameworkStringResourceProperty(context, "emxFramework.Default.Address", locale1);
    
  String strUnknown  = "Unknown";
  //

  String sUnitName        = "";

  String jtViewerType = null;

  // Get page parameters.
  String relId          = emxGetParameter(request, "relId");
  if(relId == null){ relId = "";}

  ExpansionWithSelect personSelect    = null;
  ExpansionWithSelect projectFolder   = null;
  RelationshipWithSelectItr relItr    = null;
  BusinessObject boGeneric            = null;

  String sId         = null;
  String popTreeUrl  = "";
  String sOrgId      = null;
  boolean isRepresentativeForComp = false;
  boolean isRepresentativeForBusUnit = false;

  Pattern relPattern  = new Pattern(sRelBusUnitEmp);
  Pattern typePattern = new Pattern(sBusinessUnit);

  // setting the person ID
  personBean.setId( personId );
  personBean.open(context);

  // build select params
  SelectList selectStmts  = new SelectList();
  selectStmts.addName();
  selectStmts.addDescription();

  // build select params for Relationship
  SelectList selectRelStmts = new SelectList();
  boGeneric = new BusinessObject(personId);
  boGeneric.open(context);
  personSelect = boGeneric.expandSelect(context,relPattern.getPattern(),typePattern.getPattern(),
                                             selectStmts, selectRelStmts, true, false, (short)1);
  // check the folder Member relations
  relItr = new RelationshipWithSelectItr(personSelect.getRelationships());
  boGeneric.close(context);

  // loop thru the rels and get the folder object
  // get all the document objects connected to project vault
  BusinessObject boUnit = null;
  while (relItr != null && relItr.next())
  {
   boUnit  = relItr.obj().getFrom();
   sId = (String)boUnit.getObjectId();
   boGeneric = new BusinessObject(sId);
   boGeneric.open(context);
   sUnitName = (new DomainObject(boGeneric)).getInfo(context,DomainConstants.SELECT_ATTRIBUTE_TITLE);
   boGeneric.close(context);
  }

  // Get the name of the person business object.
  String personName = personBean.getName();
   // Get the Current state of the Person object
  String sCurrentState = personBean.getCurrentState(context).getName();
  // Get the vault of the Person Business Object
  String sVault = personBean.getVault();

  // Get the Site of the person admin.
  String sSite = personBean.getSite(context);
  if (sSite==null || "".equals(sSite) || "null".equals(sSite))
  {
      sSite = "";
  }
  else
  {
      sSite = i18nNow.getAdminI18NString("Site",sSite,strLanguage);
  }


  String strDateFormat = personBean.getExportImportDateFormat(context, personName);
  String strListSeparator = personBean.getExportImportDelimiter(context, personName);

  // Get the name of the current person's organization business object.
  BusinessObject orgBo = (BusinessObject) JSPUtil.getOrganization(context,session, personBean);
  String orgName = null;

  if (orgBo != null)
  {
    orgBo.open(context);
    sOrgId = orgBo.getObjectId();
    orgName = (new DomainObject(orgBo)).getInfo(context,DomainConstants.SELECT_ATTRIBUTE_TITLE);
    orgBo.close(context);
  }

  // Find what is Company Rep (Company or Business Unit)
  BusinessObject busRepOrg = Company.getCompanyForRep(context,personBean);
  String sCompRepType = "";
  if (busRepOrg!=null)
  {
    busRepOrg.open(context);
    sCompRepType = busRepOrg.getTypeName();
    busRepOrg.close(context);
  }

  if(strCompanyType.equals(sCompRepType))
  {
    isRepresentativeForComp = true;
  }
  else if(sBusinessUnit.equals(sCompRepType))
  {
    isRepresentativeForBusUnit = true;
  }


  // To get Organizational Roles
  String roles = "";
  if(!"".equals(relId))
  {
      StringList strListOfOrgRoles = Organization.getMemberRoles(context,relId);
      int strListSize = 0;
      if(strListOfOrgRoles != null)
      {
        String strrole = "";
        for ( int i = 0 ; i < (strListSize = strListOfOrgRoles.size());i++)
        {
            strrole = strListOfOrgRoles.elementAt(i).toString();
            strrole = PropertyUtil.getSchemaProperty(context,strrole);
            strrole = i18nNow.getAdminI18NString("Role", strrole.trim() ,strLanguage);
            roles += strrole + ", ";
        }
      }
      if (!roles.equals(""))
      {
        roles = roles.substring(0,(roles.length())-2);
      }

  }

  // Create a map of the current person's attributes.
  HashMap perAttrs = ComponentsUtil.getAttributesIntoHashMap(context, personBean);
  boolean isOnPremise = FrameworkUtil.isOnPremise(context);

  String absenceStartDate   = (String)perAttrs.get(strAbsenceStartDate);
  String absenceEndDate     = (String)perAttrs.get(strAbsenceEndDate); 
  String timeZone                       = (String)session.getValue("timeZone");
  if((absenceStartDate != null && !absenceStartDate.equals("")) && (absenceEndDate != null && !absenceEndDate.equals(""))){
    long startDate               = eMatrixDateFormat.getJavaDate(absenceStartDate).getTime();
    long endDate                 = eMatrixDateFormat.getJavaDate(absenceEndDate).getTime();
    double clientTZOffset   = (new Double(timeZone)).doubleValue();
    absenceStartDate     = eMatrixDateFormat.getFormattedDisplayDate(absenceStartDate,clientTZOffset,request.getLocale());
    absenceEndDate       = eMatrixDateFormat.getFormattedDisplayDate(absenceEndDate,clientTZOffset,request.getLocale());
  }

  // Get the work place location of the current person.
  String perLoc = null;
  String sLocationId = null;

  HashMap hm = JSPUtil.expandIntoHashMap(context, personBean, sWorkPlace, sLocation, false, true, 1);
  if (! hm.isEmpty())
  {
    perLoc = (String) hm.keySet().iterator().next();
    sLocationId = (String) hm.values().iterator().next();
  }

  // Added another parameter "personName" to fix Bug # 313887
  String searchVault = personBean.getPropertySearchVaults(context,personName);
  String vaultPreference = "";
  String selectedVaults = "";
  if(searchVault != null && !"".equals(searchVault))
  {
    if(searchVault.equals(personBean.SEARCH_ALL_VAULTS)) {
      vaultPreference = "emxComponents.Common.All";
    }
    else if(searchVault.equals(personBean.SEARCH_LOCAL_VAULTS)) {
      vaultPreference = "emxComponents.Common.Local";
    }
    else if(searchVault.equals(personBean.SEARCH_DEFAULT_VAULT)) {
      vaultPreference = "emxComponents.Common.Default";
      selectedVaults = ": " + personBean.getSearchDefaultVaults(context);
    }
    else {
      vaultPreference = "emxComponents.Common.Selected";
      selectedVaults = ": " + personBean.getSearchDefaultVaults(context).replace(',', ';');
    }
  }
  else // defaults to All
  {
    vaultPreference = "emxComponents.Common.All";
  }

%>

<script language="JavaScript">


function switchUser(licensedHours){
	document.formSubmitChanges.action="emxComponentsPeopleActionProcess.jsp?action=SwitchUser&licensedHours="+licensedHours;
    document.formSubmitChanges.submit();	
}

function showEditDialogPopup()
{
    showModalDialog('emxComponentsEditPeopleDialogFS.jsp?objectId=<%=XSSUtil.encodeForURL(context, personId)%>&relId=<%=XSSUtil.encodeForURL(context, relId)%>', 575, 575);
}

function activatedeactivatePerson()
{
	
<%
    DomainObject domObject = DomainObject.newInstance(context,personId);
    String currentUser = context.getUser();
    String strUserName = domObject.getInfo(context,DomainConstants.SELECT_NAME);
    if(strUserName.equalsIgnoreCase(currentUser))
    {
    	userflag = true;
    }
    if(userflag)
    {

%>
      alert("<emxUtil:i18n localize="i18nId">emxComponents.Alert.DeactivatePeopleErrorMessage</emxUtil:i18n>"+": <%=XSSUtil.encodeForJavaScript(context, currentUser)%>");
<%
    }
    else
    {
%>
    document.formSubmitChanges.action="emxComponentsPeopleActionProcess.jsp";
    document.formSubmitChanges.submit();
    return;
<%
}
%>
}

</script>
<%@include file = "../emxUICommonHeaderEndInclude.inc" %>

<%
  // added code to display the error message
  if (errorMessage != null && !errorMessage.equals("null"))
  {
%>
    <table width="90%" border="0"  cellspacing="0" cellpadding="3"  class="formBG" align="center" >
      <tr >
        <td class="errorHeader"><%=XSSUtil.encodeForHTML(context, errorMessage)%></td>
      </tr>
    </table>
<%
  }
 
%>
<body class="properties">
<script type="text/javascript">
	addStyleSheet("emxUIDefault");
	addStyleSheet("emxUIProperties");
	addStyleSheet("emxUIList");
	addStyleSheet("emxUIMenu");
	
</script>
<form name="formSubmitChanges" method="post" action="">
  <input type="hidden" name="objectId" value="<%= XSSUtil.encodeForHTMLAttribute(context,personId) %>" />
<table class="form">

<tr id="calc_Name">
    <td class="label" width="200"">
    	<emxUtil:i18n localize="i18nId">emxComponents.PersonDetails.UserName</emxUtil:i18n>
    </td>
    <td width="" class="field"><span class="object" ><%= XSSUtil.encodeForHTML(context, personName) %>&nbsp;</span></td>
</tr>

  <tr>
    <td class="label"><emxUtil:i18n localize="i18nId">emxComponents.Common.State</emxUtil:i18n></td>
    <td class="field">
<%
        if((Person.STATE_PERSON_ACTIVE).equals(sCurrentState))
        {
%>
            <framework:i18n localize="i18nId">emxComponents.PersonDetails.Active</framework:i18n>
<%
        }
        else
        {
%>
            <framework:i18n localize="i18nId">emxComponents.PersonDetails.Inactive</framework:i18n>
<%
        }
%>
    &nbsp;</td>
  </tr>

  <tr>
    <td class="label"><emxUtil:i18n localize="i18nId">emxComponents.PersonDetails.FirstName</emxUtil:i18n></td>
    <%--//XSSOK--%>
    <td class="field"><%= (strUnknown.equals((String)perAttrs.get(sFirstName)))?XSSUtil.encodeForHTML(context, strUnknownFirstName):perAttrs.get(sFirstName) %>&nbsp;</td>
  </tr>

  <tr>
    <td class="label"><emxUtil:i18n localize="i18nId">emxComponents.PersonDetails.MiddleName</emxUtil:i18n></td>
    <%--//XSSOK--%>
    <td class="field" align="left"><%= (strUnknown.equals((String)perAttrs.get(sMiddleName)))?XSSUtil.encodeForHTML(context, strUnknownMiddleName):perAttrs.get(sMiddleName) %>&nbsp;</td>
  </tr>

  <tr>
      <td class="label"><emxUtil:i18n localize="i18nId">emxComponents.PersonDetails.LastName</emxUtil:i18n></td>
      <%--//XSSOK--%>
      <td class="field"><%= (strUnknown.equals((String)perAttrs.get(sLastName)))?XSSUtil.encodeForHTML(context, strUnknownLastName):perAttrs.get(sLastName) %>&nbsp;</td>
  </tr>

  <tr>
    <td class="label"><emxUtil:i18n localize="i18nId">emxComponents.PersonDetails.Company</emxUtil:i18n></td>
    <td class="field">
<%
    if (orgBo != null)
    {
        popTreeUrl = UINavigatorUtil.getCommonDirectory(context) + "/emxTree.jsp?objectId=" + XSSUtil.encodeForURL(context, sOrgId);
        popTreeUrl  ="javascript:showModalDialog('" + popTreeUrl + "', 800,575)";

%>
      <img src="../common/images/iconCompany16.png" />
<%
            if(!isPrinterFriendly){
%>
      <%--//XSSOK--%>
      <a href="<%=popTreeUrl%>"><%= XSSUtil.encodeForHTML(context, orgName) %></a>&nbsp;

<%
            }  else{
%>
            <%= XSSUtil.encodeForJavaScript(context, orgName) %>&nbsp;</td>
<%
            }

    } else {
%>
      &nbsp;
<%
    }
%>
    </td>
  </tr>

  <tr>
      <td class="label"><emxUtil:i18n localize="i18nId">emxComponents.CreatePeopleDialog.LicensedHours</emxUtil:i18n></td>
      <%--//XSSOK--%>
      <td class="field"><%= (strUnknown.equals((String)perAttrs.get(DomainObject.ATTRIBUTE_LICENSED_HOURS)))?"0":XSSUtil.encodeForHTML(context,(String)perAttrs.get(DomainObject.ATTRIBUTE_LICENSED_HOURS)) %>&nbsp;</td>
  </tr>

  <tr>
    <td class="label"><emxUtil:i18n localize="i18nId">emxComponents.PersonDetails.BusinessUnit</emxUtil:i18n></td>
    <td class="field">
<%
  if ( !sUnitName.equals("") )
  {
    popTreeUrl = UINavigatorUtil.getCommonDirectory(context) + "/emxTree.jsp?AppendParameters=true&objectId=" + XSSUtil.encodeForURL(context, sId) + "&emxSuiteDirectory=" + XSSUtil.encodeForURL(context, appDirectory) + "&companyId=" + XSSUtil.encodeForURL(context, sOrgId);
    if ( isMSIE )
    {
      popTreeUrl = com.matrixone.apps.domain.util.XSSUtil.encodeForURL( popTreeUrl );
    }
    popTreeUrl  ="javascript:showModalDialog('" + popTreeUrl + "', 800,575)";
%>
    <img src="../common/images/iconBusinessUnit16.png" name="imgBusinessUnit" id="imgBusinessUnit" />
<%
            if(!isPrinterFriendly){
%>
<%--//XSSOK--%>
    <a href="<%=popTreeUrl%>" ><%= XSSUtil.encodeForHTML(context, sUnitName) %></a>&nbsp;
<%
            }  else{
%>
            <%= XSSUtil.encodeForHTML(context, sUnitName) %>&nbsp;</td>
<%
            }
  } else {
%>
    &nbsp;
<%
  }
%>
    </td>
  </tr>

  <tr>
    <td class="label"><emxUtil:i18n localize="i18nId">emxComponents.PersonDetails.Location</emxUtil:i18n></td>
    <td class="field">
<%
  if ( perLoc != null ) {
    popTreeUrl = UINavigatorUtil.getCommonDirectory(context) + "/emxTree.jsp?AppendParameters=true&objectId=" + XSSUtil.encodeForURL(context, sLocationId) + "&emxSuiteDirectory=" + XSSUtil.encodeForURL(context, appDirectory);
    if ( isMSIE )
    {
      popTreeUrl = com.matrixone.apps.domain.util.XSSUtil.encodeForURL( popTreeUrl );
    }
    popTreeUrl  ="javascript:showModalDialog('" + popTreeUrl + "', 800,575)";
%>
    <img src="../common/images/iconLocation16.png" name="imgLocation" id="imgLocation" />
<%
            if(!isPrinterFriendly){
%>
<%--//XSSOK--%>
    <a href="<%=popTreeUrl%>" ><%= XSSUtil.encodeForHTML(context, perLoc) %></a>&nbsp;
<%
            }  else{
%>
            <%= XSSUtil.encodeForJavaScript(context, perLoc) %>&nbsp;</td>
<%
            }
  } else {
%>
    &nbsp;
<%
  }
%>
    </td>
  </tr>

  <tr>
    <td class="label"><emxUtil:i18n localize="i18nId">emxComponents.PersonDetails.CompanyRep</emxUtil:i18n></td>
    <td class="field">
<%
    if (isRepresentativeForComp )
    {
%>
        <emxUtil:i18n localize="i18nId">emxComponents.PersonDialog.ForCompany</emxUtil:i18n>&nbsp;
<%
    }
    else if(isRepresentativeForBusUnit)
    {
%>
        <emxUtil:i18n localize="i18nId">emxComponents.PersonDialog.ForBusinessUnit</emxUtil:i18n>&nbsp;
<%
    }
    else
    {
%>
        <emxUtil:i18n localize="i18nId">emxComponents.PersonDialog.NotCompanyRep</emxUtil:i18n>&nbsp;
<%
    }
%>
    </td>
  </tr>


  <tr>
    <td class="label"><emxUtil:i18n localize="i18nId">emxComponents.PersonDetails.WorkPhoneNumber</emxUtil:i18n></td>
    <!-- //XSSOK -->
	<td class="field"><%= (strUnknown.equals((String)perAttrs.get(sWorkPhoneNumber)))?XSSUtil.encodeForHTML(context, strUnknownWorkPhoneNumber):XSSUtil.encodeForHTML(context, (String)perAttrs.get(sWorkPhoneNumber)) %>&nbsp;</td>
  </tr>

  <tr>
    <td class="label"><emxUtil:i18n localize="i18nId">emxComponents.PersonDetails.HomePhoneNumber</emxUtil:i18n></td>
    <!-- //XSSOK -->
	<td class="field"><%= (strUnknown.equals((String)perAttrs.get(sHomePhoneNumber)))?XSSUtil.encodeForHTML(context, strUnknownHomePhoneNumber):XSSUtil.encodeForHTML(context, (String)perAttrs.get(sHomePhoneNumber)) %>&nbsp;</td>
  </tr>

  <tr>
    <td class="label"><emxUtil:i18n localize="i18nId">emxComponents.PersonDetails.PagerNumber</emxUtil:i18n></td>
    <!-- //XSSOK -->
	<td class="field"><%= (strUnknown.equals((String)perAttrs.get(sPagerNumber)))?XSSUtil.encodeForHTML(context, strUnknownPagerNumber):XSSUtil.encodeForHTML(context, (String)perAttrs.get(sPagerNumber)) %>&nbsp;</td>
  </tr>

  <tr>
    <td class="label"><emxUtil:i18n localize="i18nId">emxComponents.PersonDetails.FaxNumber</emxUtil:i18n></td>
    <!-- //XSSOK -->
	<td class="field"><%= (strUnknown.equals((String)perAttrs.get(sFaxNumber)))?XSSUtil.encodeForHTML(context, strUnknownFaxNumber):XSSUtil.encodeForHTML(context, (String)perAttrs.get(sFaxNumber)) %>&nbsp;</td>
  </tr>

  <tr>
    <td class="label"><emxUtil:i18n localize="i18nId">emxComponents.PersonDetails.EmailAddress</emxUtil:i18n></td>
    <td class="field">
<%
            if(!isPrinterFriendly){
%>
        <!-- //XSSOK -->
		<a href="mailto:<%= (strUnknown.equals((String)perAttrs.get(sEmailAddress)))?XSSUtil.encodeForHTML(context, strUnknownEmailAddress):XSSUtil.encodeForHTML(context, (String)perAttrs.get(sEmailAddress))%>"><%= (strUnknown.equals((String)perAttrs.get(sEmailAddress)))?XSSUtil.encodeForHTML(context, strUnknownEmailAddress):XSSUtil.encodeForHTML(context, (String)perAttrs.get(sEmailAddress)) %></a>&nbsp;
<%
            }  else{
%>
            <!-- //XSSOK -->
			<%= (strUnknown.equals((String)perAttrs.get(sEmailAddress)))?XSSUtil.encodeForJavaScript(context, strUnknownEmailAddress):XSSUtil.encodeForHTML(context, (String)perAttrs.get(sEmailAddress)) %>&nbsp;</td>
<%
            }
%>
  </tr>

<%
if(isOnPremise){

    //get the language preference for the person
    String personLanguagePreference = personBean.getLanguagePreference(context, personName);
    String languagePref        = FrameworkProperties.getProperty(context,"emxFramework.IconMailLanguagePreference");
    StringTokenizer strToken = null;
    String language = "";
    String locale = "";
    String i18nLanguage = "";

    if(personLanguagePreference == null) {
      personLanguagePreference = "";
    }
  %>
 <tr>
    <td class="label"><emxUtil:i18n localize="i18nId">emxComponents.PersonDetails.LanguagePreference</emxUtil:i18n></td>
 <%
      strToken = new StringTokenizer(languagePref, " ");

      while(strToken.hasMoreElements())
      {
        language = strToken.nextToken();
        locale   = EnoviaResourceBundle.getProperty(context,"emxFramework.IconMailLanguageLocale"+language);
        if(locale.equals(personLanguagePreference))
        {
            i18nLanguage = i18nNow.getI18nString("emxFramework.IconMailLanguage"+language,"emxFrameworkStringResource",request.getHeader("Accept-Language"));
            break;
        }
      }

      if(i18nLanguage.length() == 0)
      {
%>
        <td class="field"><emxUtil:i18n localize="i18nId">emxComponents.Common.None</emxUtil:i18n></td>
<%
      }
      else
      {
%>
          <td class="field"><%=XSSUtil.encodeForHTML(context, i18nLanguage)%></td>
<%
      }
%>
  </tr>
<%}%>
  <tr>
    <td class="label"><emxUtil:i18n localize="i18nId">emxComponents.PersonDetails.WebSite</emxUtil:i18n></td>
    <td class="field">
<%
            if (perAttrs.get(sWebSite).equals("Unknown"))
           {
%>
                <%= XSSUtil.encodeForHTML(context, (String)perAttrs.get(sWebSite)) %>&nbsp;

        <%} else {
                        if(!isPrinterFriendly)
                            {
                        %>
                            <a target="_blank" href="<%= XSSUtil.encodeForHTML(context, (String)perAttrs.get(sWebSite))%>"><%= XSSUtil.encodeForHTML(context, (String)perAttrs.get(sWebSite)) %></a>&nbsp;
                        <%}
                        else {
                            %>
                                <%= XSSUtil.encodeForHTML(context, (String)perAttrs.get(sWebSite)) %>&nbsp;
                            <%
                               }
                        }%>

  </tr>
<%
  if(!"".equals(relId))
  {
%>
    <tr>
      <td class="label"><emxUtil:i18n localize="i18nId">emxComponents.Common.OrganizationalRoles</emxUtil:i18n></td>
      <td class="field"><%=XSSUtil.encodeForHTML(context, roles)%>&nbsp;</td>
    </tr>
<%
  }
  
%>

  <tr>
    <td class="label"><emxUtil:i18n localize="i18nId">emxComponents.PersonDetails.LoginType</emxUtil:i18n></td>
    <td class="field"><%=XSSUtil.encodeForHTML(context, i18nNow.getRangeI18NString(sLoginType,(String)perAttrs.get(sLoginType),strLanguage))%>&nbsp;</td>
  </tr>

  <tr>
    <td class="label"><emxUtil:i18n localize="i18nId">emxComponents.PersonDetails.HostMeetings</emxUtil:i18n></td>
    <td class="field"><%= XSSUtil.encodeForHTML(context, i18nNow.getRangeI18NString(sHostMeetings,(String)perAttrs.get(sHostMeetings),strLanguage))%>&nbsp;</td>
  </tr>
  <tr>
    <td class="label"><emxUtil:i18n localize="i18nId">emxComponents.PersonDetails.Address</emxUtil:i18n></td>
    <!-- //XSSOK -->
	<td class="field"><%= (strUnknown.equals((String)perAttrs.get(sAddress)))?XSSUtil.encodeForHTML(context, strUnknownAddress):XSSUtil.encodeForHTML(context, (String)perAttrs.get(sAddress))%>&nbsp;</td>
  </tr>

  <tr>
    <td class="label"><emxUtil:i18n localize="i18nId">emxComponents.PersonDetails.City</emxUtil:i18n></td>
    <td class="field"><%= XSSUtil.encodeForHTML(context, (String)perAttrs.get(sCity))%>&nbsp;</td>
  </tr>

  <tr>
    <td class="label"><emxUtil:i18n localize="i18nId">emxComponents.PersonDetails.StateRegion</emxUtil:i18n></td>
    <td class="field"><%= XSSUtil.encodeForHTML(context, (String)perAttrs.get(sStateRegion))%>&nbsp;</td>
  </tr>

  <tr>
    <td class="label"><emxUtil:i18n localize="i18nId">emxComponents.PersonDetails.PostalCode</emxUtil:i18n></td>
    <td class="field"><%= XSSUtil.encodeForHTML(context, (String)perAttrs.get(sPostalCode))%>&nbsp;</td>
  </tr>

  <tr>
    <td class="label"><emxUtil:i18n localize="i18nId">emxComponents.PersonDetails.Country</emxUtil:i18n></td>
    <td class="field">
        <%
            String strAttrName = FrameworkUtil.findAndReplace(sCountry, " ", "_");
            String strAttrValue = FrameworkUtil.findAndReplace((String)perAttrs.get(sCountry), " ", "_");
            String sKey = "emxFramework.Range." + strAttrName + "." + strAttrValue; 
            String sValue = EnoviaResourceBundle.getProperty(context, "emxFrameworkStringResource", context.getLocale(), sKey);
            if(sValue.contains("emxFramework"))
           {
        	   sValue =  (String)perAttrs.get(sCountry);
           }
        %>
        <%= sValue %>&nbsp;
    </td>
  </tr>

  <tr>
    <td class="label"><emxUtil:i18n localize="i18nId">emxComponents.PersonDetails.AbsenceStartDate</emxUtil:i18n></td>
    <!-- //XSSOK -->
	<td class="field"><%= absenceStartDate%>&nbsp;</td>
  </tr>

  <tr>
    <td class="label"><emxUtil:i18n localize="i18nId">emxComponents.PersonDetails.AbsenceEndDate</emxUtil:i18n></td>
    <!-- //XSSOK -->
	<td class="field"><%= absenceEndDate%>&nbsp;</td>
  </tr>

  <tr>
    <td class="label"><emxUtil:i18n localize="i18nId">emxComponents.PersonDetails.AbsenceDelegate</emxUtil:i18n></td>
    <!-- //XSSOK -->
	<td class="field"><%= perAttrs.get(strAbsenceDelegate)%>&nbsp;</td>
  </tr>
<%if(isOnPremise){%>
  
  <tr>
    <td class="label"><emxUtil:i18n localize="i18nId">emxComponents.Common.Site</emxUtil:i18n></td>
    <td class="field"><%=XSSUtil.encodeForHTML(context, sSite)%>&nbsp;</td>
  </tr>
  
<%

  FormatUtil formatUtil = new FormatUtil();

  //Get the list of formats for which user can switch viewer prefernce
  String formatsStr = EnoviaResourceBundle.getProperty(context,"emxFramework.FormatsWithViewerPreference");  if( formatsStr != null)
  {
    String viewerPref = null;
    StringTokenizer formatsTokenizer = new StringTokenizer(formatsStr,",");

    while(formatsTokenizer.hasMoreTokens())
    {
      String formatToken = (String)formatsTokenizer.nextToken();
      String format = PropertyUtil.getSchemaProperty(context, formatToken );

      if( format != null && !"".equals(format))
      {
        formatUtil.setFormatName( formatToken );
        viewerPref = formatUtil.getViewerPreference( context, personName);
        String viewerName = "";
        // get the ViewerName to display from Servlet Name
        if( viewerPref != null && !"".equals(viewerPref))
        {
            viewerName = formatUtil.getViewerName( context, viewerPref);
        }
        if(viewerName == null || "".equals(viewerName))
        {
            viewerName = "None";
        }
%>
  <tr>
    <!-- //XSSOK -->
	<td class="label"><%=format%>&nbsp;<emxUtil:i18n localize="i18nId">emxComponents.PersonDetails.ViewerPreference</emxUtil:i18n>&nbsp;</td>
    <td class="field">
      <%=XSSUtil.encodeForHTML(context, i18nNow.getRangeI18NString(sJTViewerType,viewerName,strLanguage))%>
    &nbsp;</td>
  </tr>
<%
      }
    }

  }
}


  personBean.close(context);
%>




  <tr>
    <td class="label"><emxUtil:i18n localize="i18nId">emxComponents.PersonDetails.DateFormat</emxUtil:i18n> &nbsp;</td>
    <td class="field"><%=XSSUtil.encodeForHTML(context, strDateFormat)%>&nbsp;</td>
  </tr>
  
  <tr>
    <td class="label"><emxUtil:i18n localize="i18nId">emxComponents.PersonDetails.ListSeparator</emxUtil:i18n> &nbsp;</td>
    <td class="field"><%=XSSUtil.encodeForHTML(context, strListSeparator)%>&nbsp;</td>
  </tr>
  <!--Added for Organization Person Feature -->
  <tr>
    <td class="label"><emxUtil:i18n localize="i18nId">emxComponents.PersonDetails.MailCode</emxUtil:i18n> &nbsp;</td>
    <td class="field"><%=XSSUtil.encodeForHTML(context, (String)perAttrs.get(strMailCodeAttr))%>&nbsp;</td>
  </tr>
  
    <tr>
    <td class="label"><emxUtil:i18n localize="i18nId">emxComponents.Common.Title</emxUtil:i18n> &nbsp;</td>
    <td class="field"><%=XSSUtil.encodeForHTML(context, (String)perAttrs.get(strTitleAttr))%>&nbsp;</td>
  </tr>

    <tr>
    <td class="label"><emxUtil:i18n localize="i18nId">Line Manager</emxUtil:i18n> &nbsp;</td>
    <td class="field"><%=XSSUtil.encodeForHTML(context, (String)perAttrs.get(JF_Line_Manager))%>&nbsp;</td>
  </tr>
  <% if(isMCCELVInstalled) { %>
  
    <tr><td class="label"><emxUtil:i18n localize="i18nId">emxComponents.Common.IMDSContactID</emxUtil:i18n> &nbsp;</td>
    <td class="field"><%=XSSUtil.encodeForHTML(context, (String)perAttrs.get(sIMDSContactID))%>&nbsp;</td>
  </tr> 
  <%
  	} 
  %>
  <!--End -->


</table>
</form>

<%@include file = "../emxUICommonEndOfPageInclude.inc" %>
