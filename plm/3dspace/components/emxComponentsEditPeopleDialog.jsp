<%--
   Copyright (c) 1992-2020 Dassault Systemes.
   All Rights Reserved.
--%>
<%@include file = "emxCalendarInclude.inc" %>
<%@include file = "emxComponentsDesignTopInclude.inc"%>
<%@include file = "../emxUICommonHeaderBeginInclude.inc"%>
<%@include file = "emxComponentsVisiblePageInclude.inc"%>

<%@ include file = "../emxJSValidation.inc" %>
<%@page import="com.matrixone.apps.domain.util.FrameworkUtil"%> 

<%
  com.matrixone.apps.common.Person busEditPerson = (com.matrixone.apps.common.Person)DomainObject.newInstance(context,
                      DomainConstants.TYPE_PERSON);

  // Load lookup strings.

  String strPersonType                = PropertyUtil.getSchemaProperty(context, "type_Person");
  String strCompanyType               = PropertyUtil.getSchemaProperty(context, "type_Company");
  String strOrganizationType          = PropertyUtil.getSchemaProperty(context, "type_Organization");
  String strBusinessUnitType          = PropertyUtil.getSchemaProperty(context, "type_BusinessUnit");
  String strLocationType              = PropertyUtil.getSchemaProperty(context, "type_Location");

  String strEmployeeRel               = PropertyUtil.getSchemaProperty(context, "relationship_Employee");
  String strDivisionRel               = PropertyUtil.getSchemaProperty(context, "relationship_Division");
  String strWorkPlaceRel              = PropertyUtil.getSchemaProperty(context, "relationship_WorkPlace");
  String strLocationRel               = PropertyUtil.getSchemaProperty(context, "relationship_OrganizationLocation");
  String strBusinessUnitEmployeeRel   = PropertyUtil.getSchemaProperty(context, "relationship_BusinessUnitEmployee");

  String strFirstNameAttr             = PropertyUtil.getSchemaProperty(context, "attribute_FirstName");
  String strMiddleNameAttr            = PropertyUtil.getSchemaProperty(context, "attribute_MiddleName");
  String strLastNameAttr              = PropertyUtil.getSchemaProperty(context, "attribute_LastName");

  String strWorkPhoneNumberAttr       = PropertyUtil.getSchemaProperty(context, "attribute_WorkPhoneNumber");
  String strHomePhoneNumberAttr       = PropertyUtil.getSchemaProperty(context, "attribute_HomePhoneNumber");
  String strPagerNumberAttr           = PropertyUtil.getSchemaProperty(context, "attribute_PagerNumber");
  String strFaxNumberAttr             = PropertyUtil.getSchemaProperty(context, "attribute_FaxNumber");

  String strEmailAddressAttr          = PropertyUtil.getSchemaProperty(context, "attribute_EmailAddress");
  String strWebSiteAttr               = PropertyUtil.getSchemaProperty(context, "attribute_WebSite");

  String strLoginTypeAttr             = PropertyUtil.getSchemaProperty(context, "attribute_LoginType");
  String strHostMeetingsAttr          = PropertyUtil.getSchemaProperty(context, "attribute_HostMeetings");
  String strJTViewerTypeAttr          = PropertyUtil.getSchemaProperty(context, "attribute_JTViewerType");


  String strAddressAttr               = PropertyUtil.getSchemaProperty(context, "attribute_Address");
  String strCityAttr                  = PropertyUtil.getSchemaProperty(context, "attribute_City");
  String strStateRegionAttr           = PropertyUtil.getSchemaProperty(context, "attribute_StateRegion");
  String strPostalCodeAttr            = PropertyUtil.getSchemaProperty(context, "attribute_PostalCode");
  String strCountryAttr               = PropertyUtil.getSchemaProperty(context, "attribute_Country");
  String strAbsenseStartDateAttr      = PropertyUtil.getSchemaProperty(context, "attribute_AbsenceStartDate");
  String strAbsenseEndDateAttr        = PropertyUtil.getSchemaProperty(context, "attribute_AbsenceEndDate");
  String strAbsenseDelegateAttr       = PropertyUtil.getSchemaProperty(context, "attribute_AbsenceDelegate");
    String owner = context.getUser();
  String strContextUserEditProfile  = emxGetParameter(request,"contextusereditprofile");
  String timeZone                       = (String)session.getValue("timeZone");
   //Added for Organization Person Feature
  String strMailCodeAttr  = PropertyUtil.getSchemaProperty(context, "attribute_MailCode");
  String strTitleAttr          = PropertyUtil.getSchemaProperty(context, "attribute_Title");
    String JF_Line_Manager				  = PropertyUtil.getSchemaProperty(context, "attribute_JF_Line_Manager");//add by caipan
  String sIMDSContactID         = PropertyUtil.getSchemaProperty(context,"attribute_IMDSContactID");
  String sIMDSCompanyID         = PropertyUtil.getSchemaProperty(context,"attribute_IMDSCompanyID");
  //End
  // Do not edit below code added by MCC
  boolean isMCCELVInstalled = FrameworkUtil.isSuiteRegistered(context,"appVersionELVDataManagementOption",false,null,null);

  String vaultAwareness ="true";
  String requiredClass ="label";
  long  startDate      = 0;
  long  endDate      = 0;
  vaultAwareness=JSPUtil.getApplicationProperty(context,application,"eServiceSuiteComponents.VaultAwareness","emxComponentsProperties");
  requiredClass=(vaultAwareness.equals("true") || ("true".equals(strContextUserEditProfile)))?"label":"labelRequired";

  i18nNow loc = new i18nNow();
  int intTabIndexCounter = 1;

  // Get the possible passed in parameters.
  String strPersonId               = emxGetParameter(request, "objectId");
  String relId                     = emxGetParameter(request, "relId");
  if(relId == null) { relId = "";}
  String strLanguage               = request.getHeader("Accept-Language");

  busEditPerson.setId(strPersonId);
  busEditPerson.open(context);

  HashMap hm = JSPUtil.expandIntoHashMap(context, busEditPerson, strWorkPlaceRel, strLocationType, false, true, 1);
  HashMap objectMap = new HashMap();
  if (! hm.isEmpty())
  {
    Iterator keys = hm.keySet().iterator();
    while(keys.hasNext())
    {
      String name = (String) keys.next();
      objectMap.put("Location",name );
    }
  }

  String strCurrentState = busEditPerson.getCurrentState(context).getName();

  String strUserName = busEditPerson.getName();

  String strDateFormat = busEditPerson.getExportImportDateFormat(context, strUserName);

  if(strDateFormat == null)
  {
    strDateFormat = "";
  }
  strDateFormat = strDateFormat.trim();
  String strListSeparator = busEditPerson.getExportImportDelimiter(context, strUserName);
  if(strListSeparator == null)
  {
    strListSeparator = "";
  }
  strListSeparator = strListSeparator.trim();

  // Create a map of the current person's attributes.
  HashMap perAttrs = ComponentsUtil.getAttributesIntoHashMap(context, busEditPerson);

  String strLoginType          = (String)perAttrs.get(strLoginTypeAttr);
  String strHostMeetings       = (String)perAttrs.get(strHostMeetingsAttr);
  String strAbsenseStartDate   = (String)perAttrs.get(strAbsenseStartDateAttr);
  String strAbsenseEndDate     = (String)perAttrs.get(strAbsenseEndDateAttr);
  String strAbsenseDelegate    = (String)perAttrs.get(strAbsenseDelegateAttr);

if((strAbsenseStartDate != null && !strAbsenseStartDate.equals("")) && (strAbsenseEndDate != null && !strAbsenseEndDate.equals(""))){
    startDate               = eMatrixDateFormat.getJavaDate(strAbsenseStartDate).getTime();
    endDate                 = eMatrixDateFormat.getJavaDate(strAbsenseEndDate).getTime();
    double clientTZOffset   = (new Double(timeZone)).doubleValue();
    strAbsenseStartDate     = eMatrixDateFormat.getFormattedDisplayDate(strAbsenseStartDate,clientTZOffset,request.getLocale());
    strAbsenseEndDate       = eMatrixDateFormat.getFormattedDisplayDate(strAbsenseEndDate,clientTZOffset,request.getLocale());
  }

  String strSiteValue = busEditPerson.getSite(context);

  Pattern patternType = new Pattern ( strOrganizationType );
  patternType.addPattern(strLocationType);

  // This is to get all project connected to Project Member
  Pattern patternRel = new Pattern ( strEmployeeRel );
  patternRel.addPattern(strWorkPlaceRel);
  patternRel.addPattern(strBusinessUnitEmployeeRel);

  SelectList selListType = new SelectList();
  selListType.addName();
  selListType.addAttribute(DomainConstants.ATTRIBUTE_TITLE);
  SelectList selListRel = new SelectList();

  ExpansionWithSelect expWSelectPersonDetails = busEditPerson.expandSelect(context, patternRel.getPattern(), patternType.getPattern(), selListType, selListRel, true, true, (short)1);

  RelationshipWithSelectItr relWSelectItrPerson = new RelationshipWithSelectItr(expWSelectPersonDetails.getRelationships());
  BusinessObject busLocation = null;
  BusinessObject busCompany = null;
  BusinessObject busBusinessUnit = null;
  BusinessObjectList busListLocations = new BusinessObjectList();
  BusinessObjectList busListBusinessUnits = new BusinessObjectList();
  StringList strListBUNames = new StringList();
  StringList strListLocationNames = new StringList();

  String strOriginalLocation = "";
  String strEditPersonCompany = "";
  String strOriginalBusinessUnit = "";
  String strBusinessUnitId = "";
  String strCompanyId = "";
  String strWorkLocationId = "";
  String strOriginalWorkPlace = "";
  String strOriginalBUEmployee = "";
  String imdsCmyId="";
  String imdsContactChooserUrl = "" ; 
  while (relWSelectItrPerson.next())
  {
    RelationshipWithSelect relWSelectPerson = relWSelectItrPerson.obj();

    if ( strWorkPlaceRel.equals(relWSelectPerson.getTypeName()) ) {
      strOriginalWorkPlace = relWSelectPerson.getName();
      busLocation = relWSelectPerson.getTo();
      Hashtable hashTypeData = relWSelectPerson.getTargetData();
      strOriginalLocation = ((String)hashTypeData.get("name"));
      strWorkLocationId = busLocation.getObjectId();

    } else if ( strBusinessUnitEmployeeRel.equals(relWSelectPerson.getTypeName()) ) {
      strOriginalBUEmployee = relWSelectPerson.getName();
      busBusinessUnit = relWSelectPerson.getFrom();
      Hashtable hashTypeData = relWSelectPerson.getTargetData();
      strOriginalBusinessUnit = ((String)hashTypeData.get(DomainConstants.SELECT_ATTRIBUTE_TITLE));
      strBusinessUnitId = busBusinessUnit.getObjectId();

    } else if ( strEmployeeRel.equals(relWSelectPerson.getTypeName()) ) {
      busCompany = relWSelectPerson.getFrom();
      Hashtable hashTypeData = relWSelectPerson.getTargetData();
      strEditPersonCompany = ((String)hashTypeData.get(DomainConstants.SELECT_ATTRIBUTE_TITLE));
      strCompanyId = busCompany.getObjectId();
      if(isMCCELVInstalled) {
      	imdsCmyId = busCompany.getAttributeValues(context, sIMDSCompanyID).getValue();
      	if(UIUtil.isNullOrEmpty(imdsCmyId)) {
      		imdsCmyId ="" ;
    }
      	imdsContactChooserUrl ="../common/emxTable.jsp?imdsOid="+imdsCmyId+"&table=MCCIMDSContactSummary&program=jpo.materialscompliance.IMDSOrganization:getImdsContactsForCompany&header=emxMaterialComplianceCentral.Header.SelectIMDSCompanyContact&SubmitLabel=emxMaterialComplianceCentral.Button.Select&CancelLabel=emxMaterialComplianceCentral.Button.Cancel&CancelButton=true&SubmitURL=../materialscompliance/emxMCCIMDSCompanyChooserProcess.jsp&selection=single&HelpMarker=emxhelpimdscompanyselect&objectBased=false&toolbar=MCCIMDSCompanyToolbar&TransactionType=update&suiteKey=MaterialsComplianceCentral&SuiteDirectory=materialscompliance&StringResourceFileId=emxMaterialsComplianceCentralStringResource";
  }
  }
  }

  
  

  patternRel = new Pattern ( strLocationRel );
  patternRel.addPattern(strDivisionRel);

  if ( busCompany == null ) {
   String sRelAttrDivision = PropertyUtil.getSchemaProperty(context, "relationship_Division" );
   Pattern relPattern = new Pattern(sRelAttrDivision);
   Pattern typePattern = new Pattern(strCompanyType);
   busCompany = ComponentsUtil.getConnectedObject(context,busBusinessUnit,relPattern.getPattern(),typePattern.getPattern(),
                true, false);
  }

  busCompany.open(context);

  if ( strEditPersonCompany.equals("") ) {
    strEditPersonCompany  = (new DomainObject(busCompany)).getInfo(context,DomainConstants.SELECT_ATTRIBUTE_TITLE);
  }


  ExpansionWithSelect expWSelectCompanyDetails = busCompany.expandSelect(context, patternRel.getPattern(), patternType.getPattern(), selListType, selListRel, false, true, (short)0);
  RelationshipWithSelectItr relWSelectItrCompany = new RelationshipWithSelectItr(expWSelectCompanyDetails.getRelationships());
  while (relWSelectItrCompany.next())
  {
    RelationshipWithSelect relWSelectPerson = relWSelectItrCompany.obj();

    if (strLocationRel.equals(relWSelectPerson.getTypeName()) ) {
      busListLocations.addElement(relWSelectPerson.getTo());
      Hashtable hashTypeData = relWSelectPerson.getTargetData();
      strListLocationNames.addElement((String)hashTypeData.get("name"));
    } else if ( strDivisionRel.equals(relWSelectPerson.getTypeName()) ) {
      busListBusinessUnits.addElement(relWSelectPerson.getTo());
      Hashtable hashTypeData = relWSelectPerson.getTargetData();
      strListBUNames.addElement((String)hashTypeData.get(DomainConstants.SELECT_ATTRIBUTE_TITLE));
    }
  }
  busCompany.close(context);

  BusinessObject company = new BusinessObject(strCompanyId);
  company.open(context);

  String primaryVault = company.getVault();
  String secondaryVaults = new Company(strCompanyId).getSecondaryVaults(context);
  StringList vaultList = FrameworkUtil.split(secondaryVaults,",");
  Iterator itr = vaultList.iterator();

  company.close(context);
  /*
   * Determine the current user's privileges (not the person being edited).
   */
  // Get the current person from the context.
  BusinessObject busLoggedPerson = JSPUtil.getPerson(context,session);
  busLoggedPerson.open(context);
  boolean isHostRep = Company.isHostRep(context, busLoggedPerson);
  boolean isCompanyRep = false;
  boolean isBusUnitRep = false;
  boolean isHostOrCompRep = false;

  // Get the company we represent.
  BusinessObject busLoggedRep = Company.getCompanyForRep(context,busLoggedPerson);
  // Determine if we are a company rep.
  String strRepId = "";
  if ( busLoggedRep != null)
  {
    strRepId = busLoggedRep.getObjectId();
    if(strRepId.equals(strCompanyId))
    {
        isCompanyRep = true;
    }
    else if(strRepId.equals(strBusinessUnitId))
    {
        isBusUnitRep = true;
    }
  }


  if ( isHostRep || isCompanyRep || isBusUnitRep)
  {
    isHostOrCompRep = true;
  }

  String strRepOrgType = null;
  BusinessObject busEditRep = Company.getCompanyForRep(context, busEditPerson);
  if ( busEditRep != null )
  {
    busEditRep.open(context);
    strRepOrgType = busEditRep.getTypeName();
    busEditRep.close(context);
  }

  String strSelected = "";
  boolean isLoggedInPerson = false;
  if ( busLoggedPerson.getObjectId().equals(busEditPerson.getObjectId()) ) {
    isLoggedInPerson = true;
  }

  String vaultName = busEditPerson.getVault();

  //Get the list of Sites
  MQLCommand cmd = new MQLCommand();
  cmd.open(context);
  cmd.executeCommand(context, "list site");
  String sResult = cmd.getResult();
  cmd.close(context);
  StringTokenizer siteToken = new StringTokenizer(sResult, "\n");

  boolean is3DPassportServerInUse = FrameworkUtil.is3DPassportServerInUse(context);

%>
<%@include file = "../common/emxUIConstantsInclude.inc"%>
<%@page import="com.matrixone.apps.framework.ui.UIUtil"%>
<%@page import="com.matrixone.apps.common.Company"%><script language="javascript" type="text/javascript" src ="../common/scripts/emxUICalendar.js"></script>
<script language = javascript>

  function trimval(str){
    while(str.length != 0 && str.substring(0,1) == ' '){
      str = str.substring(1);
    }
    while(str.length != 0 && str.substring(str.length -1) == ' '){
      str = str.substring(0, str.length -1);
    }
    return str;
  }
  function validateDate()
  {

     var milli1 = Number(document.personForm.AbsenceStartDate_msvalue.value);
     var milli2 = Number(document.personForm.AbsenceEndDate_msvalue.value);
     var date1 = new Date(milli1);
     var date2 = new Date(milli2);
     var temptoday = new Date();
     //converting JS date to a date1 & date 2 format.
     var today = new Date((temptoday.getMonth() + 1)+"/"+temptoday.getDate()+"/"+temptoday.getFullYear());


    //  Addressing  Y2K.
       if (today.getFullYear()< 1950)
     {
       // add 100 years.
        today.setFullYear(date1.getFullYear() + 100);
     }
       if (date1.getFullYear()< 1950)
     {
       // add 100 years.
        date1.setFullYear(date1.getFullYear() + 100);
     }
       if (date2.getFullYear()< 1950)
     {
       // add 100 years.
        date2.setFullYear(date2.getFullYear() + 100);
     }
      // comparing the dates

      if(Date.parse(date1.toGMTString()) >= Date.parse(today.toGMTString())){
           if ( Date.parse(date1.toGMTString()) > Date.parse(date2.toGMTString())) {
               alert("<emxUtil:i18nScript localize="i18nId">emxComponents.PersonDialog.EndDateAlert</emxUtil:i18nScript>");
                return false;

           }else{
                return true;
           }
      }else{
         alert("<emxUtil:i18nScript localize="i18nId">emxComponents.PersonDialog.StartDateAlert</emxUtil:i18nScript>");
         return false;

      }
  }
  //checks whether AbsenceStartDate, AbsenceEndDate, Delegate person fields has values or not.
  function hasAllValue(){

      var startDate = document.personForm.AbsenceStartDate.value;
      var endDate   = document.personForm.AbsenceEndDate.value;
      var person    = document.personForm.person_display.value;
       //Bug fixed 332361
         if(trimval(startDate)== "" && trimval(endDate) == "" && trimval(person) == "")
          {
            return true;
          }

         else if(trimval(startDate)== "" || trimval(endDate) == "" || trimval(person) == "")
            {
                alert("<emxUtil:i18nScript localize="i18nId">emxComponents.PersonDialog.ValueMissAlert</emxUtil:i18nScript>");
               return false;
             }
         else
         {
                return validateDate();
         }
         //Bug End

  }
  function isIMDSIdPresent()
  { 
	  <%--//XSSOK--%>
	  if("<%=imdsCmyId%>" == null || "<%=imdsCmyId%>" =='')
	  {
  	        var imdsDisplay =document.getElementById("Imdsbtn");
    	        imdsDisplay.disabled=true;
       		alert("<emxUtil:i18nScript localize="i18nId">emxComponents.alert.SelectIMDSCompanyId</emxUtil:i18nScript>");       
    }else { 
	 	    showModalDialog('<%=XSSUtil.encodeForHTML(context,(String)imdsContactChooserUrl)%>',575,575);
	 }
  }
  
  function submitForm()
  {

    // Create an alias for the form.
    var personForm = document.personForm;
    var businessUnit = "<%=XSSUtil.encodeForHTML(context,(String)strBusinessUnitType)%>";
    personForm.workphonenumber.value = trimval ( personForm.workphonenumber.value);

	<%if(!is3DPassportServerInUse){%>
    personForm.firstname.value = trimval (personForm.firstname.value);
    personForm.lastname.value = trimval (personForm.lastname.value);

    personForm.emailaddress.value = trimval( personForm.emailaddress.value);
    
    var firstNameBadChars = checkForNameBadCharsList(personForm.firstname);
    var lastNameBadChars = checkForNameBadCharsList(personForm.lastname);
    var middleNameBadChars = checkForNameBadCharsList(personForm.middlename);


    if ( personForm.firstname.value == "" ) {
      personForm.firstname.focus();
      alert ("<emxUtil:i18nScript localize="i18nId">emxComponents.PersonDialog.FirstNameRequired</emxUtil:i18nScript>");
      return;
    } else if (firstNameBadChars.length != 0) {
          alert("<emxUtil:i18nScript localize="i18nId">emxComponents.Common.AlertValidName</emxUtil:i18nScript>");
          personForm.firstname.focus();
          return;
    } else if (middleNameBadChars.length != 0) {
          alert("<emxUtil:i18nScript localize="i18nId">emxComponents.Common.AlertValidName</emxUtil:i18nScript>");
          personForm.middlename.focus();
          return;
    } else if ( personForm.lastname.value == "" ) {
      personForm.lastname.focus();
      alert ("<emxUtil:i18nScript localize="i18nId">emxComponents.PersonDialog.LastNameRequired</emxUtil:i18nScript>");
      return;
    } else if (lastNameBadChars.length != 0) {
          alert("<emxUtil:i18nScript localize="i18nId">emxComponents.Common.AlertValidName</emxUtil:i18nScript>");
          personForm.lastname.focus();
          return;
    }
  <% if (isHostRep || isCompanyRep){ %>
    else if ( personForm.confirmpassword.value != personForm.password.value  ) {
      personForm.confirmpassword.focus();
      alert("<emxUtil:i18nScript localize="i18nId">emxComponents.PersonDialog.PasswordNotMatch</emxUtil:i18nScript>");
      return;
    }
  <% } %>
  
  	<%}%>
  
    if ( personForm.companyrep.value == businessUnit && "" == personForm.businessUnit.value ) {
      alert("<emxUtil:i18nScript localize="i18nId">emxComponents.PersonDialog.SelectBusUnit</emxUtil:i18nScript>");
      return;
    } 
	<%if(!is3DPassportServerInUse){%>
		if ( personForm.emailaddress.value == "" || personForm.emailaddress.value.indexOf("@") < 0 || personForm.emailaddress.value.lastIndexOf(".") < personForm.emailaddress.value.indexOf("@") ) {
      personForm.emailaddress.focus();
      personForm.emailaddress.select();
      alert ("<emxUtil:i18nScript localize="i18nId">emxComponents.CreatePeopleDialog.InvalidEmail</emxUtil:i18nScript>");
      return;
    }
    //<!--Added for the Bug No: 340487 09/06/2007 3:00 PM Start -->
    else if (!checkemail()){
      personForm.emailaddress.focus();
     alert ("<emxUtil:i18nScript localize="i18nId">emxComponents.CreatePeopleDialog.validEmailId</emxUtil:i18nScript>");
     return;
    }
	<%}%>
    //<!--Ended for the Bug No: 340487 09/06/2007 3:00 PM End -->

       if(hasAllValue())
       {
          var namebadCharactersMailCode = checkForNameBadCharsList(document.personForm.mailCode);

          if(namebadCharactersMailCode.length != 0)
          {
            alert("<emxUtil:i18nScript localize="i18nId">emxComponents.Common.AlertInValidChars</emxUtil:i18nScript>"+namebadCharactersMailCode);
            return;
          }
          var namebadCharactersTitle = checkForNameBadCharsList(document.personForm.title);

          if(namebadCharactersTitle.length != 0)
          {
            alert("<emxUtil:i18nScript localize="i18nId">emxComponents.Common.AlertInValidChars</emxUtil:i18nScript>"+namebadCharactersTitle);
            return;
          }

          //added for 294348
          if(personForm.person_display.value=="")
           {
              personForm.person.value="";
           }
           //till here
          personForm.action =  "emxComponentsEditPeople.jsp";
          personForm.submit();
       }
      return ;
    }


  function closeProfileWindow() {
    window.closeWindow();
    return;
  }

  function clearField(formName,fieldName,idName)
  {

    var operand = "document." + formName + "." + fieldName+".value = \"\";";
    eval (operand);

    if(idName != null){
        var operand1 = "document." + formName + "." + idName+".value = \"\";";

        eval (operand1);
    }
    return;
  }

   //<!--Added for the Bug No: 340487 09/06/2007 3:00 PM Start -->
  var testresults
  function checkemail(){
  var str=document.personForm.emailaddress.value
  var filter=/^[a-zA-Z0-9][\w\.-]*[a-zA-Z0-9]*@[a-zA-Z0-9][\w\.-]*[a-zA-Z0-9]*\.[a-zA-Z][a-zA-Z\.]*[a-zA-Z]$/
  if (filter.test(str))
 testresults=true
 else{
 testresults=false
 }
 return (testresults)
 }
//<!--Ended for the Bug No: 340487 09/06/2007 3:00 PM End -->
</script>

<%@include file = "../emxUICommonHeaderEndInclude.inc" %>
 <body class="editable">
<form name="personForm" method="post" action="" target="_parent" onSubmit="submitForm(); return false;" >

  <input type="hidden" name="objectId"                value="<xss:encodeForHTMLAttribute><%= strPersonId %></xss:encodeForHTMLAttribute>"/>
  <input type="hidden" name="relId"                   value="<xss:encodeForHTMLAttribute><%=relId%></xss:encodeForHTMLAttribute>"/>
  <input type="hidden" name="companyId"               value="<xss:encodeForHTMLAttribute><%= strCompanyId %></xss:encodeForHTMLAttribute>"/>
  <!-- Modified below param value to fix bug 295781 -->
  <input type="hidden" name="originalLocation"        value="<xss:encodeForHTMLAttribute><%= strWorkLocationId %></xss:encodeForHTMLAttribute>"/>
  <input type="hidden" name="originalWorkPlace" value="<xss:encodeForHTMLAttribute><%= strOriginalWorkPlace %></xss:encodeForHTMLAttribute>"/>
  <input type="hidden" name="originalBusinessUnit"    value="<xss:encodeForHTMLAttribute><%=strBusinessUnitId%></xss:encodeForHTMLAttribute>"/>
  <input type="hidden" name="originalBUEmployee" value="<xss:encodeForHTMLAttribute><%=strOriginalBUEmployee%></xss:encodeForHTMLAttribute>"/>
 
  <input type="hidden" name="originalVault" value="<xss:encodeForHTMLAttribute><%=vaultName%></xss:encodeForHTMLAttribute>"/>
  <input type="hidden" name="originalUsername" value="<xss:encodeForHTMLAttribute><%=strUserName%></xss:encodeForHTMLAttribute>"/>
  <input type="hidden" name="contextusereditprofile" value="<xss:encodeForHTMLAttribute><%=strContextUserEditProfile%></xss:encodeForHTMLAttribute>"/>


<table>
  <tr>
    <td class="label"><label for="UserName"><emxUtil:i18n localize="i18nId">emxComponents.PersonDialog.UserName</emxUtil:i18n></label>&nbsp;</td>
    <td class="inputField"><%= XSSUtil.encodeForHTML(context, strUserName) %></td>
  </tr>
  <% if ((isHostRep || isCompanyRep) && !is3DPassportServerInUse){ %>
  <tr>
    <td class="label"><label for="PassWord"><emxUtil:i18n localize="i18nId">emxComponents.CreatePeopleDialog.PassWord</emxUtil:i18n></label>&nbsp;</td>
    <td class="inputField">
      <input type="password"  size="30" name="password"value=""/>
    </td>
  </tr>

  <tr>
    <td class="label"><label for="ConfirmPassWord"><emxUtil:i18n localize="i18nId">emxComponents.CreatePeopleDialog.ConfirmPassWord</emxUtil:i18n></label>&nbsp;</td>
    <td class="inputField">
      <input type="password"  size="30" name="confirmpassword" value=""/>
    </td>
  </tr>
  <% } %>
  <tr>
    <td class="label"><emxUtil:i18n localize="i18nId">emxComponents.Common.State</emxUtil:i18n></td>
    <td class="inputField">
<%
        if((Person.STATE_PERSON_ACTIVE).equals(strCurrentState))
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
    <td class="labelRequired"><label for="FirstName"><emxUtil:i18n localize="i18nId">emxComponents.PersonDialog.FirstName</emxUtil:i18n></lable>&nbsp;</td>
    <td class="inputField">
	<%if(!is3DPassportServerInUse){%>
      <input type="text" name="firstname" id="firstname" size="30" value="<xss:encodeForHTMLAttribute><%= perAttrs.get(strFirstNameAttr) %></xss:encodeForHTMLAttribute>" />
    	<%}else{%>
		    <input type="hidden" name="firstname" id="firstname" size="30" value="<xss:encodeForHTMLAttribute><%= perAttrs.get(strFirstNameAttr) %></xss:encodeForHTMLAttribute>" />
			<xss:encodeForHTMLAttribute><%= perAttrs.get(strFirstNameAttr) %></xss:encodeForHTMLAttribute>
	<%}%>
    </td>
  </tr>

  <tr>
    <td class="label"><label for="MiddleName"><emxUtil:i18n localize="i18nId">emxComponents.PersonDialog.MiddleName</emxUtil:i18n></label></td>
    <td class="inputField">
	<%if(!is3DPassportServerInUse){%>
      <input type="text" id="middlename" name="middlename" size="30" value="<xss:encodeForHTMLAttribute><%= perAttrs.get(strMiddleNameAttr) %></xss:encodeForHTMLAttribute>" />
       <%}else{%>			
            <input type="hidden" id="middlename" name="middlename" size="30" value="<xss:encodeForHTMLAttribute><%= perAttrs.get(strMiddleNameAttr) %></xss:encodeForHTMLAttribute>" />	   
			<xss:encodeForHTMLAttribute><%= perAttrs.get(strMiddleNameAttr) %></xss:encodeForHTMLAttribute>
	<%}%>
    </td>
  </tr>

  <tr>
    <td class="labelRequired"><label for="LastName"><emxUtil:i18n localize="i18nId">emxComponents.PersonDialog.LastName</emxUtil:i18n></label></td>
    <td class="inputField">
	<%if(!is3DPassportServerInUse){%>
      <input type="text" id="lastname" name="lastname" size="30" value="<xss:encodeForHTMLAttribute><%= perAttrs.get(strLastNameAttr) %></xss:encodeForHTMLAttribute>" />
	<%}else{%>
	    <input type="hidden" id="lastname" name="lastname" size="30" value="<xss:encodeForHTMLAttribute><%= perAttrs.get(strLastNameAttr) %></xss:encodeForHTMLAttribute>" />
		<xss:encodeForHTMLAttribute><%= perAttrs.get(strLastNameAttr) %></xss:encodeForHTMLAttribute>
	<%}%>   
    </td>
  </tr>

  <tr>
    <td class="label"><label for="Company"><emxUtil:i18n localize="i18nId">emxComponents.PersonDialog.Company</emxUtil:i18n></label>&nbsp;</td>
    <td class="inputField"><img src="../common/images/iconCompany16.png" alt="*" />&nbsp;<%= XSSUtil.encodeForHTML(context, strEditPersonCompany) %></td>
  </tr>
  <tr>
    <td class="label"><label for="businessUnit"><emxUtil:i18n localize="i18nId">emxComponents.PersonDialog.BusinessUnit</emxUtil:i18n></label>&nbsp;</td>
    <td class="inputField">
<%
// If this is add mode or we are a company rep, and a company
// is selected and there is at least one business unit in the list,
// then show the business units.
if ( (isHostRep || isCompanyRep) &&  busListBusinessUnits.size() > 0 )
    {
      BusinessObjectItr busItrBU = new BusinessObjectItr(busListBusinessUnits);
      StringItr strItrBUNames = new StringItr (strListBUNames);
%>
<select name="businessUnit" size="1" >
        <option value=""></option>
<%
// Loop through the sorted business unit names
// and create the select options.
while (busItrBU.next() && strItrBUNames.next())
        {
          String strBUName = (String) strItrBUNames.obj();
          String strBUId = busItrBU.obj().getObjectId();
          strSelected = strBUId.equals(strBusinessUnitId) ? "selected" : "";
%>
<option value="<%= XSSUtil.encodeForHTMLAttribute(context, strBUId) %>" <%= XSSUtil.encodeForHTML(context, strSelected) %>><%= XSSUtil.encodeForHTML(context, strBUName) %></option>
<%
}
%>
</select>
<%
} else {

        if(!"".equals(strOriginalBusinessUnit))
        {
%>
	<img src="../common/images/iconSmallBusinessUnit.gif" border="0" />
<%
        }
%>
      <%= XSSUtil.encodeForHTML(context, strOriginalBusinessUnit) %>

            <img src="../common/images/iconBusinessUnit16.png" border="0" />&nbsp;<%= XSSUtil.encodeForHTML(context, strOriginalBusinessUnit) %>
<%
    	}
%>
    </td>
  </tr>

  <tr>
    <td class="label"><label for="location"><emxUtil:i18n localize="i18nId">emxComponents.PersonDialog.Location</emxUtil:i18n></label>&nbsp;</td>
    <td class="inputField">
<%
    // If this is add mode or we are a company rep, and a company
    // is selected and there is at least on location in the list,
    // then show the locations.

    //295781. Added below code to display location as editables.
    boolean hasToDisconnectAccess = true;
    if(strWorkLocationId != null && !"".equals(strWorkLocationId) && !"null".equals(strWorkLocationId)) {
        DomainObject dom = new DomainObject(strWorkLocationId);
        hasToDisconnectAccess = dom.checkAccess(context, (short)AccessConstants.cToDisconnect);
    }

    if ( busListLocations.size() > 0 && hasToDisconnectAccess)
    {
      BusinessObjectItr busItrLocation = new BusinessObjectItr(busListLocations);
      StringItr strItrLocationNames = new StringItr (strListLocationNames);
%>
      <select name="location" size="1" >
        <option value=""></option>
<%
        // Loop through the sorted location names
        // and create the select options.
        while (busItrLocation.next() && strItrLocationNames.next())
        {
          String strLocationName = (String) strItrLocationNames.obj();
          String strLocationId = busItrLocation.obj().getObjectId();
          if(UIUtil.isNotNullAndNotEmpty(strLocationId)) {
    		  if("Active".equalsIgnoreCase(DomainObject.newInstance(context, strLocationId).getCurrentState(context).getName())) {
          		strSelected = strLocationId.equals(strWorkLocationId) ? "selected" : "";
%>
          		<option value="<%= XSSUtil.encodeForHTMLAttribute(context, strLocationId) %>" <%= XSSUtil.encodeForHTML(context, strSelected) %>><%= XSSUtil.encodeForHTML(context, strLocationName) %></option>
<%
    		  }
    	  }
       }
%>
      </select>
<%
    } else {
%>
      <input type="hidden" name="location" value="<xss:encodeForHTMLAttribute><%= strWorkLocationId %></xss:encodeForHTMLAttribute>"/>
      <%= XSSUtil.encodeForHTML(context, strOriginalLocation) %>
<%
    }
%>
    </td>
  </tr>

<tr>
    <td class="label"><label for="CompanyRep"><emxUtil:i18n localize="i18nId">emxComponents.PersonDialog.CompanyRep</emxUtil:i18n></label>&nbsp;</td>
    <td class="inputField">

<%
    // Determine the selected item.
    boolean isCompSel = false;
    boolean isUnitSel = false;
    boolean isNoSel = false;

    if (strCompanyType.equals(strRepOrgType))
    {
      isCompSel = true;
    }
    else if (strBusinessUnitType.equals(strRepOrgType))
    {
      isUnitSel = true;
    }
    else
    {
      isNoSel = true;
    }

    if (isHostOrCompRep)
    {
      // If the looged in user is a Company Rep and is trying to modify his own Profile
      // then he cannot demote himself to a BU Rep or remove the Company Rep role totally
      if (context.getUser().equals(strUserName))
      {
        if(isBusUnitRep)
        {
%>
            <input type="hidden" name="companyrep" value="<xss:encodeForHTMLAttribute><%= strBusinessUnitType %></xss:encodeForHTMLAttribute>"/>
            <emxUtil:i18n localize="i18nId">emxComponents.PersonDialog.ForBusinessUnit</emxUtil:i18n>
<%
        }
        else
        {
%>
            <input type="hidden" name="companyrep" value="<xss:encodeForHTMLAttribute><%= strCompanyType %></xss:encodeForHTMLAttribute>"/>
            <emxUtil:i18n localize="i18nId">emxComponents.PersonDialog.ForCompany</emxUtil:i18n>
<%
        }

      }
      else
      {
          if(isBusUnitRep){
%>
            <select name="companyrep" size="1" onselect="" >
            <option value="" <%= isNoSel ? "selected" : "" %>><emxUtil:i18n localize="i18nId">emxComponents.PersonDialog.NotCompanyRep</emxUtil:i18n></option>
            <option value="<%= XSSUtil.encodeForHTMLAttribute(context, strBusinessUnitType) %>" <%= isUnitSel ? "selected" : "" %>><emxUtil:i18n localize="i18nId">emxComponents.PersonDialog.ForBusinessUnit</emxUtil:i18n></option>
<%
          }
          else
          {
%>
            <select name="companyrep" size="1" onselect="" >
            <option value="" <%= isNoSel ? "selected" : "" %>><emxUtil:i18n localize="i18nId">emxComponents.PersonDialog.NotCompanyRep</emxUtil:i18n></option>
            <option value="<%= XSSUtil.encodeForHTMLAttribute(context, strCompanyType) %>" <%= isCompSel ? "selected" : "" %>><emxUtil:i18n localize="i18nId">emxComponents.PersonDialog.ForCompany</emxUtil:i18n></option>
            <option value="<%= XSSUtil.encodeForHTMLAttribute(context, strBusinessUnitType) %>" <%= isUnitSel ? "selected" : "" %>><emxUtil:i18n localize="i18nId">emxComponents.PersonDialog.ForBusinessUnit</emxUtil:i18n></option>
            </select>
<%
         }
      }
    } else {
%>
      <input type="hidden" name="companyrep" value=""/>
      <input type="text" size="30" READONLY value="<emxUtil:i18n localize='i18nId'>emxComponents.PersonDialog.NotCompanyRep</emxUtil:i18n>"/>
<%
    }

%>
    </td>
  </tr>


  <tr>
    <td class="label"><label for="WorkPhone"><emxUtil:i18n localize="i18nId">emxComponents.PersonDialog.WorkPhone</emxUtil:i18n></label>&nbsp;</td>
    <td class="inputField" align="left">
      <input type="text" name="workphonenumber" id="workphonenumber" size="30" value="<xss:encodeForHTMLAttribute><%= perAttrs.get(strWorkPhoneNumberAttr) %></xss:encodeForHTMLAttribute>" />
    </td>
  </tr>

  <tr>
    <td class="label"><label for="OfficePhone"><emxUtil:i18n localize="i18nId">emxComponents.PersonDialog.HomePhone</emxUtil:i18n></label></td>
    <td class="inputField">
      <input type="text" name="homephonenumber" id="homephonenumber" size="30" value="<xss:encodeForHTMLAttribute><%= perAttrs.get(strHomePhoneNumberAttr) %></xss:encodeForHTMLAttribute>" />
    </td>
  </tr>

  <tr>
    <td class="label"><label for="Pager"><emxUtil:i18n localize="i18nId">emxComponents.PersonDialog.Pager</emxUtil:i18n></label></td>
    <td class="inputField">
      <input type="text" name="pagernumber" id="pagernumber" size="30" value="<xss:encodeForHTMLAttribute><%= perAttrs.get(strPagerNumberAttr) %></xss:encodeForHTMLAttribute>" />
    </td>
  </tr>

  <tr>
    <td class="label"><label for="Fax"><emxUtil:i18n localize="i18nId">emxComponents.PersonDialog.Fax</emxUtil:i18n></label></td>
    <td class="inputField">
      <input type="text" name="faxnumber" id="faxnumber" size="30" value="<xss:encodeForHTMLAttribute><%= perAttrs.get(strFaxNumberAttr) %></xss:encodeForHTMLAttribute>" />
    </td>
  </tr>

  <tr>
    <td class="labelRequired"><label for="Email"><emxUtil:i18n localize="i18nId">emxComponents.PersonDialog.E-mail</emxUtil:i18n></label>&nbsp;</td>
    <td class="inputField">
	<%if(!is3DPassportServerInUse){%>
      <input type="text" name="emailaddress" id="emailaddress" size="30" value="<xss:encodeForHTMLAttribute><%= perAttrs.get(strEmailAddressAttr) %></xss:encodeForHTMLAttribute>" />
    	<%}else{%>
		    <input type="hidden" name="emailaddress" id="emailaddress" size="30" value="<xss:encodeForHTMLAttribute><%= perAttrs.get(strEmailAddressAttr) %></xss:encodeForHTMLAttribute>" />
			<xss:encodeForHTMLAttribute><%= perAttrs.get(strEmailAddressAttr) %></xss:encodeForHTMLAttribute>
	<%}%>
    </td>
  </tr>

<%
  //get the language preference for the person
  String personLanguagePreference = busEditPerson.getLanguagePreference(context, strUserName);
  if(personLanguagePreference == null)
  {
    personLanguagePreference = "";
  }
  String languagePref       = EnoviaResourceBundle.getProperty(context,"emxFramework.IconMailLanguagePreference");
  StringTokenizer strToken  = null;
  String language = "";
  String locale = "";
  String i18nLanguage = "";

    %>
    <tr>
      <td class="label"><emxUtil:i18n localize="i18nId">emxComponents.PersonDialog.LanguagePreference</emxUtil:i18n> &nbsp;</td>
        <td class="inputField">
        <select name="languagePreference" >
    <%
           strToken = new StringTokenizer(languagePref, " ");
           while(strToken.hasMoreElements())
           {
            language = strToken.nextToken();
            locale   = EnoviaResourceBundle.getProperty(context,"emxFramework.IconMailLanguageLocale"+language);
            if(personLanguagePreference.equals(locale))
            {
              i18nLanguage = i18nNow.getI18nString("emxFramework.IconMailLanguage"+language,"emxFrameworkStringResource",request.getHeader("Accept-Language"));
              break;
            }
           }
           if(i18nLanguage.length() == 0)
           {
    %>
               <option value="None"><emxUtil:i18n localize="i18nId">emxComponents.Common.None</emxUtil:i18n></option>
    <%
           } else {
    %>
              <option value="<%=XSSUtil.encodeForHTMLAttribute(context, locale)%>"><%=XSSUtil.encodeForHTML(context, i18nLanguage)%></option>
    <%
           }
           strToken = new StringTokenizer(languagePref, " ");
           while(strToken.hasMoreElements())
           {
              language = strToken.nextToken();
              locale   = EnoviaResourceBundle.getProperty(context,"emxFramework.IconMailLanguageLocale"+language);
              if(!personLanguagePreference.equals(locale))
              {
                i18nLanguage = i18nNow.getI18nString("emxFramework.IconMailLanguage"+language,"emxFrameworkStringResource",request.getHeader("Accept-Language"));
    %>
                <option value="<%=XSSUtil.encodeForHTMLAttribute(context, locale)%>"><%=XSSUtil.encodeForHTML(context, i18nLanguage)%></option>
    <%
              }
           }
    %>
         </select>
       </td>
     </tr>


  <tr>
    <td class="label"><label for="WebSite"><emxUtil:i18n localize="i18nId">emxComponents.PersonDialog.WebSite</emxUtil:i18n></label></td>
    <td class="inputField">
      <input type="text" name="website" id="website" size="30" value="<xss:encodeForHTMLAttribute><%= perAttrs.get(strWebSiteAttr) %></xss:encodeForHTMLAttribute>" />
    </td>
  </tr>

  <tr>
    <td class="label"><emxUtil:i18n localize="i18nId">emxComponents.PersonDialog.LoginType</emxUtil:i18n>&nbsp;</td>
    <td class="inputField">
<%

        // If this is add mode or we are a company rep, then let user edit LoginType
        if (isHostOrCompRep)
        {
          StringList strListLoginTypeRange = FrameworkUtil.getRanges(context , strLoginTypeAttr);
          StringItr strItrLoginTypeRange   = new StringItr (strListLoginTypeRange);
%>
        <table>
<%
          String strLoginTypeRange = "";
          String i18NstrLoginTypeRange = "";
          // Loop through the LoginType range and create the select options
          while (strItrLoginTypeRange.next())
          {
            strLoginTypeRange = strItrLoginTypeRange.obj();
            strSelected = strLoginTypeRange.equals(strLoginType) ? "checked" : "";
            i18NstrLoginTypeRange = i18nNow.getRangeI18NString(strLoginTypeAttr, strLoginTypeRange, request.getHeader("Accept-Language"));
%>
            <tr>
              <%--//XSSOK--%>
              <td><input type="radio" name="logintype" id = "logintype" value="<%=XSSUtil.encodeForHTMLAttribute(context, strLoginTypeRange)%>" <%= strSelected%> /> &nbsp;<%= i18NstrLoginTypeRange %></td>
            </tr>
<%
          }
%>
        </table>
<%
      } else {
%>
          <input type="hidden" name="logintype" value="<xss:encodeForHTMLAttribute><%= strLoginType %></xss:encodeForHTMLAttribute>"/>
          <input type="text" size="30" READONLY value="<xss:encodeForHTMLAttribute><%= strLoginType %></xss:encodeForHTMLAttribute>"/>
<%
      }
%>
    </td>
  </tr>

  <tr>
    <td class="label"><emxUtil:i18n localize="i18nId">emxComponents.PersonDialog.HostMeetings</emxUtil:i18n></td>
    <td class="inputField" align="left">
    <table>
        <tr>
          <td>
          	<input type="radio" name="hostmeetings" id="hostmeetings" value="Yes" <%= ("no".equalsIgnoreCase(strHostMeetings)) ? "":"checked" %> />
          	&nbsp;
          	<emxUtil:i18n localize="i18nId">emxComponents.PersonDialog.Yes</emxUtil:i18n>
          </td>
        </tr>
        <tr>
          <td>
          		<input type="radio" name="hostmeetings" id="hostmeetings" value="No" <%= ("no".equalsIgnoreCase(strHostMeetings)) ? "checked": "" %> />
          		&nbsp;
          		<emxUtil:i18n localize="i18nId">emxComponents.PersonDialog.No</emxUtil:i18n>
          </td>
        </tr>
    </table>
    </td>
    </td>
 </tr>
<tr>
 <td align="left" class="label" valign="top">
   <framework:i18n localize="i18nId">emxComponents.PersonDialog.Address</framework:i18n>
 </td>
 <td class="field" align="left">
   <textarea name="address" rows="3" cols="30" wrap="wrap" ><xss:encodeForHTML><%= perAttrs.get(strAddressAttr) %></xss:encodeForHTML></textarea>
 </td>
</tr>
<tr>
 <td align="left" class="label">
   <framework:i18n localize="i18nId">emxComponents.PersonDialog.City</framework:i18n>
 </td>
 <td class="field" align="left"><input type="hidden" name="city" size="36" value="<xss:encodeForHTMLAttribute><%= perAttrs.get(strCityAttr) %></xss:encodeForHTMLAttribute>" /><xss:encodeForHTMLAttribute><%= perAttrs.get(strCityAttr) %></xss:encodeForHTMLAttribute></td>
</tr>
<tr>
  <td align="left" class="label">
    <framework:i18n localize="i18nId">emxComponents.PersonDialog.StateRegion</framework:i18n>
  </td>
  <td class="field" align="left"><input type="text" name="stateRegion" size="36" value="<xss:encodeForHTMLAttribute><%= perAttrs.get(strStateRegionAttr) %></xss:encodeForHTMLAttribute>" /></td>
</tr>
<tr>
  <td align="left" class="label">
    <framework:i18n localize="i18nId">emxComponents.PersonDialog.PostalCode</framework:i18n>
  </td>
  <td class="field" align="left"><input type="text" name="postalCode" size="36" value="<xss:encodeForHTMLAttribute><%= perAttrs.get(strPostalCodeAttr) %></xss:encodeForHTMLAttribute>" /></td>
</tr>
<tr>
  <td align="left" class="label">
    <framework:i18n localize="i18nId">emxComponents.PersonDialog.Country</framework:i18n>
  </td>
  <td class="field" align="left"><%
	 String countryDefaultValue = (String)perAttrs.get(strCountryAttr);
     if(!is3DPassportServerInUse){%>
<%             
	java.util.Map countryChoiceDetails = (java.util.Map) matrix.db.JPO.invoke(context, "emxOrganization", null, "getCountryChooserDetailsForHTMLDisplay", new String[]{request.getHeader("Accept-Language")}, java.util.HashMap.class);
	java.util.List optionList =  (java.util.List)countryChoiceDetails.get("optionList");
	java.util.List valueList =  (java.util.List)countryChoiceDetails.get("valueList");
	java.util.List manualEntryList =  (java.util.List)countryChoiceDetails.get("manualEntryList");  	    
%>
         <!-- //XSSOK -->
		 <framework:editOptionList disabled="false" name="<%=XSSUtil.encodeForHTML(context,(String)strCountryAttr)%>" optionList="<%=optionList%>" valueList="<%=valueList%>" sortDirection="ascending" selected="<%=countryDefaultValue%>" manualEntryList="<%=manualEntryList%>"/>
     <%}else{%>   
			<input type="hidden" name="<xss:encodeForHTMLAttribute><%=strCountryAttr %></xss:encodeForHTMLAttribute>" id="strCountryAttr" value="<xss:encodeForHTMLAttribute><%=countryDefaultValue %></xss:encodeForHTMLAttribute>" />
			<xss:encodeForHTMLAttribute><%=countryDefaultValue %></xss:encodeForHTMLAttribute>
     <%}%>  
    </td>
</tr>
<tr>
    <td class="label"><label for="Absence Start Date"><emxUtil:i18n localize="i18nId">emxComponents.PersonDialog.AbsenceStartDate</emxUtil:i18n></label></td>
    <td class="inputField">
    <input type="hidden" name="AbsenceStartDate_msvalue" value="<xss:encodeForHTMLAttribute><%=(strAbsenseStartDate != null && !strAbsenseStartDate.equals(""))?startDate:(long)0%></xss:encodeForHTMLAttribute>"/>
    <input type="text" name="AbsenceStartDate" id="AbsenceStartDate" size="20" readonly value="<xss:encodeForHTMLAttribute><%=strAbsenseStartDate%></xss:encodeForHTMLAttribute>"/>&nbsp;<a href="javascript:showCalendar('personForm','AbsenceStartDate','')"><img src="../common/images/iconSmallCalendar.gif" border=0 width="16" height="16" /></a>&nbsp;
    <a href="JavaScript:clearField('personForm','AbsenceStartDate','AbsenceStartDate')" ><emxUtil:i18n localize="i18nId">emxComponents.Common.Clear</emxUtil:i18n></a>
    </td>
  </tr>
  <tr>
    <td class="label"><label for="Absence End Date"><emxUtil:i18n localize="i18nId">emxComponents.PersonDialog.AbsenceEndDate</emxUtil:i18n></label></td>
    <td class="inputField">
    <input type="hidden" name="AbsenceEndDate_msvalue" value="<xss:encodeForHTMLAttribute><%=(strAbsenseEndDate != null && !strAbsenseEndDate.equals(""))?endDate:(long)0%></xss:encodeForHTMLAttribute>"/>
    <input type="text" name="AbsenceEndDate" id="AbsenceEndDate" readonly size="20" value="<xss:encodeForHTMLAttribute><%=strAbsenseEndDate%></xss:encodeForHTMLAttribute>"/>&nbsp;<a href="javascript:showCalendar('personForm','AbsenceEndDate','')"><img src="../common/images/iconSmallCalendar.gif" border=0 width="16" height="16" /></a>&nbsp;
    <a href="JavaScript:clearField('personForm','AbsenceEndDate','AbsenceEndDate')" ><emxUtil:i18n localize="i18nId">emxComponents.Common.Clear</emxUtil:i18n></a>
    </td>
  </tr>
<%
  String addMembersURL =  "../common/emxFullSearch.jsp?showInitialResults=false&table=AEFPersonChooserDetails&selection=single&form=AEFSearchPersonForm&submitURL=../components/emxPersonSearchProcess.jsp&field=TYPES=type_Person:CURRENT=policy_Person.state_Active&HelpMarker=emxhelpsearch&header=emxComponents.Common.SelectPerson&suiteKey=Components&StringResourceFileId=emxComponentsStringResource";
%>
  <tr>
    <td class="label"><label for="Absence Delegate"><emxUtil:i18n localize="i18nId">emxComponents.PersonDialog.AbsenceDelegate</emxUtil:i18n></label></td>
    <td class="inputField">
<!--modified for 294348 -->
        <input type="hidden" name="person" id="person" value="<xss:encodeForHTMLAttribute><%=strAbsenseDelegate%></xss:encodeForHTMLAttribute>" />
<!--till here -->
<!--added for 294348-->
        <input type="text" name="person_display" id="person_display" readonly size="25" value="<xss:encodeForHTMLAttribute><%=strAbsenseDelegate==null || "".equals(strAbsenseDelegate) || "null".equals(strAbsenseDelegate)?"" : PersonUtil.getFullName(context,strAbsenseDelegate)%></xss:encodeForHTMLAttribute>" />
<!--till here -->
<!--modified for 294348 -->
        <input type="button" value="..." name="btn" id="btn" onclick="javascript:showModalDialog('<%=XSSUtil.encodeForHTML(context, addMembersURL)%>',575,575);" />&nbsp;
        <a href="JavaScript:clearField('personForm','person_display','person_display')" ><emxUtil:i18n localize="i18nId">emxComponents.Common.Clear</emxUtil:i18n></a>
<!--till here -->
    </td>

  </tr>

  <tr>
    <td class="label"><emxUtil:i18n localize="i18nId">emxComponents.Common.Site</emxUtil:i18n></td>
    <td class="inputField">
        <select name="Site" >
           <%--//XSSOK--%>
           <option value="None" <%=("".equals(strSiteValue))?"selected": ""%>><emxUtil:i18n localize="i18nId">emxComponents.Common.None</emxUtil:i18n></option>
<%
        while(siteToken.hasMoreElements())
        {
          String strSite = siteToken.nextToken();
%>
            <%--//XSSOK--%>
            <option value="<%=XSSUtil.encodeForHTMLAttribute(context, strSite)%>" <%=(strSite.equals(strSiteValue))?"selected": ""%>><%=XSSUtil.encodeForHTML(context, i18nNow.getAdminI18NString("Site",strSite,strLanguage))%></option>
<%      }
%>
        </select>

   </td>
  </tr>
<%
  // display all Viewers for each format,
  // Get the list of formats for which user can switch viewer prefernce
  String viewerPref = null;
  String formatsStr=null;
	try{
		formatsStr = EnoviaResourceBundle.getProperty(context,"emxFramework.FormatsWithViewerPreference");
	}catch(Exception ex){
	}
  if( formatsStr != null )
  {
    StringTokenizer formatsTokenizer = new StringTokenizer(formatsStr,",");
    FormatUtil formatUtil = new FormatUtil();

    String formatToken = "";
    String format = "";
    while(formatsTokenizer.hasMoreTokens())
    {
      formatToken = (String)formatsTokenizer.nextToken();
      format = PropertyUtil.getSchemaProperty( context,formatToken );

      if( format != null && !"".equals(format))
      {
        formatUtil.setFormatName( formatToken );

        viewerPref = formatUtil.getViewerPreference( context, strUserName);

         //get all the Options for the viewer
         StringList viewerList = formatUtil.getViewerOptions( context );
         Iterator i = viewerList.iterator();
%>
       <tr>
        <td class="label"><%=XSSUtil.encodeForHTML(context,(String)format)%>&nbsp;<emxUtil:i18n localize="i18nId">emxComponents.PersonDetails.ViewerPreference</emxUtil:i18n>&nbsp;</td>
        <td class="inputField">
          <select name = <%= format + "viewerPreference"%> >
            <option value="None"><emxUtil:i18n localize="i18nId">emxComponents.Common.None</emxUtil:i18n></option>
<%
         String viewerURL = "";
         String viewerName = "";
         while( i.hasNext())
         {

           viewerURL = (String)i.next();
           viewerName = "";

           // get the ViewerName to display from Servlet Name
           if( viewerURL != null && !"".equals(viewerURL))
           {
              viewerName = formatUtil.getViewerName( context, viewerURL);
           }

           if( viewerURL.equals( viewerPref ))
           {
%>
             <option selected value = "<%=XSSUtil.encodeForHTML(context, viewerURL)%>" ><%=XSSUtil.encodeForHTML(context, viewerName)%></option>
<%
           } else {
%>
             <option value = "<%=XSSUtil.encodeForHTML(context, viewerURL)%>" ><%=XSSUtil.encodeForHTML(context, viewerName)%></option>
<%
           }
         }
%>
        </select>
       </td>
      </tr>
<%
      }
    }
  }

    busEditPerson.close(context);
    busLoggedPerson.close(context);


  String dateFormats = EnoviaResourceBundle.getProperty(context,"eServiceSuiteComponents.ExportImportDateFormats");
  StringTokenizer strFormatToken  = null;
  String format = "";
%>
    <tr>
      <td class="label"><emxUtil:i18n localize="i18nId">emxComponents.PersonDetails.DateFormat</emxUtil:i18n> &nbsp;</td>
      <td class="inputField">
        <select name="dateFormat" >
           <option value="None"><emxUtil:i18n localize="i18nId">emxComponents.Common.None</emxUtil:i18n></option>
<%
            strFormatToken = new StringTokenizer(dateFormats, ",");
            while(strFormatToken.hasMoreElements())
            {
                format = strFormatToken.nextToken();
%>
                <option value="<%=XSSUtil.encodeForHTML(context, format)%>" <%=XSSUtil.encodeForHTML(context, (format.equals(strDateFormat))?"selected" : "")%>><%=XSSUtil.encodeForHTML(context, format)%></option>
<%
            }
%>
        </select>
      </td>
    </tr>

<%
  String listSeparator = EnoviaResourceBundle.getProperty(context,"eServiceSuiteComponents.ExportImportDelimiters");
  StringTokenizer strDelimiterToken  = null;
  String separator = "";
%>
    <tr>
      <td class="label"><emxUtil:i18n localize="i18nId">emxComponents.PersonDetails.ListSeparator</emxUtil:i18n> &nbsp;</td>
      <td class="inputField">
        <select name="listSeparator" >
           <option value="None"><emxUtil:i18n localize="i18nId">emxComponents.Common.None</emxUtil:i18n></option>
<%
            strDelimiterToken = new StringTokenizer(listSeparator, " ");
            while(strDelimiterToken.hasMoreElements())
            {
                separator = strDelimiterToken.nextToken();
%>

                <option value="<%=XSSUtil.encodeForHTML(context, separator)%>" <%=XSSUtil.encodeForHTML(context, (separator.equals(strListSeparator))?"selected" : "")%>><%=XSSUtil.encodeForHTML(context, separator)%></option>
<%
            }
%>
        </select>
      </td>
    </tr>
    <!--Added for Organization Person Feature -->
<tr>
  <td align="left" class="label">
    <framework:i18n localize="i18nId">emxComponents.PersonDetails.MailCode</framework:i18n>
  </td>
  <td class="field" align="left"><input type="text" name="mailCode" size="36" value="<xss:encodeForHTMLAttribute><%= perAttrs.get(strMailCodeAttr) %></xss:encodeForHTMLAttribute>" /></td>
</tr>
<tr>
  <td align="left" class="label">
    <framework:i18n localize="i18nId">emxComponents.Common.Title</framework:i18n>
  </td>
  <td class="field" align="left"><input type="text" name="title" size="36" value="<xss:encodeForHTMLAttribute><%= perAttrs.get(strTitleAttr) %></xss:encodeForHTMLAttribute>" /></td>
</tr>
<tr>
  <td align="left" class="label">
    <framework:i18n localize="i18nId">Line Manager</framework:i18n>
  </td>
    <% if("admin_platform".equals(owner)){ %>
    <td class="field" align="left"><input type="text" name="JF_Line_Manager" size="36" value="<xss:encodeForHTMLAttribute><%= perAttrs.get(JF_Line_Manager) %></xss:encodeForHTMLAttribute>" /></td>
    <%} else{%>
    <td class="field" align="left"><input type="text" readonly="readonly" name="JF_Line_Manager" size="36" value="<xss:encodeForHTMLAttribute><%= perAttrs.get(JF_Line_Manager) %></xss:encodeForHTMLAttribute>" /></td>
    <%}%>
</tr>
<% if(isMCCELVInstalled){ %>
	  <tr><td class="label"><emxUtil:i18n localize="i18nId">emxComponents.Common.IMDSContactID</emxUtil:i18n> &nbsp;</td>
	  <td class="field" align="left"><input type="text"  readonly name="imdsId" size="36" value="<xss:encodeForHTMLAttribute><%= perAttrs.get(sIMDSContactID) %></xss:encodeForHTMLAttribute>"/>
	   <input type="button" value="..." name="Imdsbtn" id="Imdsbtn" onclick="javascript:isIMDSIdPresent();" />&nbsp;  <a href="JavaScript:clearField('personForm','imdsId','imdsId')" ><emxUtil:i18n localize="i18nId">emxComponents.Common.Clear</emxUtil:i18n></a></td>
	  </tr>
 <%}%>
</table>
</form>
</body>

<%@include file = "../emxUICommonEndOfPageInclude.inc" %>
