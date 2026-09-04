<%--  emxComponentsEditPeople.jsp   -   Processing Edit Details 

   Copyright (c) 1992-2020 Dassault Systemes.
   All Rights Reserved.
   This program contains proprietary and trade secret information of MatrixOne, Inc.
   Copyright notice is precautionary only and does not evidence any actual or intended publication of such program

   static const char RCSID[] = $Id: emxComponentsEditPeople.jsp.rca 1.27 Wed Oct 22 16:17:57 2008 przemek Experimental przemek $
--%>

<%@include file = "emxComponentsDesignTopInclude.inc"%>
<%@include file = "../emxUICommonHeaderBeginInclude.inc"%>

<%@include file = "../emxUICommonHeaderEndInclude.inc"%>
<%@include file = "../common/enoviaCSRFTokenValidation.inc"%>

<%
  com.matrixone.apps.common.Person person = (com.matrixone.apps.common.Person)DomainObject.newInstance(context,
                      DomainConstants.TYPE_PERSON);


  String strPersonType              = PropertyUtil.getSchemaProperty(context, "type_Person");
  String strCompanyType             = PropertyUtil.getSchemaProperty(context, "type_Company");
  String strBusinessUnitType        = PropertyUtil.getSchemaProperty(context, "type_BusinessUnit");

  String strWorkPlaceRel            = PropertyUtil.getSchemaProperty(context, "relationship_WorkPlace");
  String strLocationRel             = PropertyUtil.getSchemaProperty(context, "relationship_OrganizationLocation");
  String strCompanyRepresentativeRel= PropertyUtil.getSchemaProperty(context, "relationship_CompanyRepresentative");
  String strBusinessUnitEmployeeRel = PropertyUtil.getSchemaProperty(context, "relationship_BusinessUnitEmployee");  

  String strEmployeeRole            = PropertyUtil.getSchemaProperty(context, "role_Employee");
  String strOrganizationManagerRole = PropertyUtil.getSchemaProperty(context, "role_OrganizationManager");
  

  String strFirstNameAttr           = PropertyUtil.getSchemaProperty(context, "attribute_FirstName");
  String strMiddleNameAttr          = PropertyUtil.getSchemaProperty(context, "attribute_MiddleName");
  String strLastNameAttr            = PropertyUtil.getSchemaProperty(context, "attribute_LastName");
  String strWorkPhoneNumberAttr     = PropertyUtil.getSchemaProperty(context, "attribute_WorkPhoneNumber");
  String strHomePhoneNumberAttr     = PropertyUtil.getSchemaProperty(context, "attribute_HomePhoneNumber");
  String strPagerNumberAttr         = PropertyUtil.getSchemaProperty(context, "attribute_PagerNumber");
  String strFaxNumberAttr           = PropertyUtil.getSchemaProperty(context, "attribute_FaxNumber");
  String strEmailAddressAttr        = PropertyUtil.getSchemaProperty(context, "attribute_EmailAddress");
  String strWebSiteAttr             = PropertyUtil.getSchemaProperty(context, "attribute_WebSite");
  
  String strLoginTypeAttr           = PropertyUtil.getSchemaProperty(context, "attribute_LoginType");
  String strHostMeetingsAttr        = PropertyUtil.getSchemaProperty(context, "attribute_HostMeetings");
  String strJTViewerTypeAttr          = PropertyUtil.getSchemaProperty(context, "attribute_JTViewerType");  

  String strAddressAttr             = PropertyUtil.getSchemaProperty(context, "attribute_Address");
  String strCityAttr                = PropertyUtil.getSchemaProperty(context, "attribute_City");
  String strStateRegionAttr         = PropertyUtil.getSchemaProperty(context, "attribute_StateRegion");
  String strPostalCodeAttr          = PropertyUtil.getSchemaProperty(context, "attribute_PostalCode");
  String strCountryAttr             = PropertyUtil.getSchemaProperty(context, "attribute_Country");
 
  String strAbsenceStartDateAttr    = PropertyUtil.getSchemaProperty(context, "attribute_AbsenceStartDate");
  String strAbsenceEndDateAttr      = PropertyUtil.getSchemaProperty(context, "attribute_AbsenceEndDate");
  String strAbsenceDelegateAttr     = PropertyUtil.getSchemaProperty(context, "attribute_AbsenceDelegate");
  String sIMDSContactID         = PropertyUtil.getSchemaProperty(context,"attribute_IMDSContactID");
   //Added for Organization Person Feature
  String strMailCodeAttr   = PropertyUtil.getSchemaProperty(context, "attribute_MailCode");
  String strTitleAttr           = PropertyUtil.getSchemaProperty(context, "attribute_Title");
    String JF_Line_ManagerAttr           = PropertyUtil.getSchemaProperty(context, "attribute_JF_Line_Manager");
  //End
  // Taking Parameters From request of Edit Form
  String jsTreeID         = emxGetParameter(request,"jsTreeID");
  String suiteKey         = emxGetParameter(request,"suiteKey");
  
  String strPersonId          = emxGetParameter(request, "objectId");
  // Check for a person id.
  if (strPersonId == null) {
    throw new MatrixException(ComponentsUtil.i18nStringNow("emxComponents.Common.MatrixException",request.getHeader("Accept-Language")));
  }
  
  person.setId( strPersonId );
  
  String relId   = emxGetParameter(request, "relId");
  if(relId == null) { relId = "";}
  // if relId == null or "" then the page is displayed through Inactive People Summary
 
  String strFirstName         = emxGetParameter(request, "firstname");
  String strMiddleName        = emxGetParameter(request, "middlename");
  String strLastName          = emxGetParameter(request, "lastname");
  
  String strCompanyId         = emxGetParameter(request, "companyId");
  
  String strBusinessUnit      = emxGetParameter(request, "businessUnit");
  String strLocation          = emxGetParameter(request, "location");
  String strCompRep           = emxGetParameter(request, "companyrep");
  String strWorkPhoneNumber   = emxGetParameter(request, "workphonenumber");
  String strHomePhoneNumber   = emxGetParameter(request, "homephonenumber");
  String strPagerNumber       = emxGetParameter(request, "pagernumber");
  String strFaxNumber         = emxGetParameter(request, "faxnumber");
  String strEmailAddress      = emxGetParameter(request, "emailaddress");
  String strLanguagePref      = emxGetParameter(request, "languagePreference");
  String strWebSite           = emxGetParameter(request, "website");
  String strLoginType         = emxGetParameter(request, "logintype");
  String strHostMeetings      = emxGetParameter(request, "hostmeetings");
  String meetingUsername      = emxGetParameter(request, "meetingUsername");
  String meetingPassword      = emxGetParameter(request, "meetingPassword");
  String strAddress           = emxGetParameter(request, "address");
  String strCity              = emxGetParameter(request, "city");
  String strStateRegion       = emxGetParameter(request, "stateRegion");
  String strPostalCode        = emxGetParameter(request, "postalCode");
  String strCountry           = emxGetParameter(request, strCountryAttr);
  String strAbsenceStartDate  = emxGetParameter(request, "AbsenceStartDate");
  String strAbsenceEndDate    = emxGetParameter(request, "AbsenceEndDate");
  String strDelegate          = emxGetParameter(request, "person");  
  String strVault             = emxGetParameter(request, "Vault");
  String strDateFormat        = emxGetParameter(request,"dateFormat");
  String strListSeparator     = emxGetParameter(request,"listSeparator");

  String strSite              = emxGetParameter(request, "Site");
  
  String strOriginalLocation        = emxGetParameter(request, "originalLocation");
  String strOriginalWorkPlace       = emxGetParameter(request, "originalWorkPlace");
  String strOriginalBusinessUnit    = emxGetParameter(request, "originalBusinessUnit");
  String strOriginalBUEmployee      = emxGetParameter(request, "originalBUEmployee");
  String strOriginalMeetingUserName = emxGetParameter(request, "originalMeetingUserName");
  String strOriginalVault           = emxGetParameter(request, "originalVault");
  String originalUsername           = emxGetParameter(request,"originalUsername");
  String strContextUserEditProfile  = emxGetParameter(request,"contextusereditprofile");
  //Added for Organization Person Feature
  String strMailCode           = emxGetParameter(request, "mailCode");
  String strTitle              = emxGetParameter(request, "title");
    String JF_Line_Manager              = emxGetParameter(request, "JF_Line_Manager");//add by caipan
System.out.println("JF_Line_Manager="+JF_Line_Manager);
  String imdsId              = emxGetParameter(request, "imdsId");
  //End
  //strHostMeetings = (strHostMeetings == null) ? emxGetParameter(request, "hostmeetings1")    : strHostMeetings;
  //meetingUsername = (meetingUsername == null) ? emxGetParameter(request, "meetingUsername1") : meetingUsername;
  //meetingPassword = (meetingPassword == null) ? emxGetParameter(request, "meetingPassword1") : meetingPassword;
  String strPassword                = emxGetParameter(request,"password");
  //Added for bug 320306 rev0
  String strMemberRel               = PropertyUtil.getSchemaProperty(context, "relationship_Member");
  AttributeList attrListPerson = new AttributeList();
  attrListPerson.add( new Attribute( new AttributeType(strFirstNameAttr)  , strFirstName ) );
  attrListPerson.add( new Attribute( new AttributeType(strMiddleNameAttr)  , strMiddleName ) );
  attrListPerson.add( new Attribute( new AttributeType(strLastNameAttr)  , strLastName ) );
  attrListPerson.add( new Attribute( new AttributeType(strWorkPhoneNumberAttr)  , strWorkPhoneNumber ) );
  attrListPerson.add( new Attribute( new AttributeType(strHomePhoneNumberAttr)  , strHomePhoneNumber ) );  
  attrListPerson.add( new Attribute( new AttributeType(strPagerNumberAttr)  , strPagerNumber) );
  attrListPerson.add( new Attribute( new AttributeType(strFaxNumberAttr)  , strFaxNumber ) );
  attrListPerson.add( new Attribute( new AttributeType(strEmailAddressAttr)  , strEmailAddress ) );
  attrListPerson.add( new Attribute( new AttributeType(strWebSiteAttr)  , strWebSite ) );
  attrListPerson.add( new Attribute( new AttributeType(strLoginTypeAttr)  , strLoginType ) );

  attrListPerson.add( new Attribute( new AttributeType(strHostMeetingsAttr)  , strHostMeetings ) );
  attrListPerson.add( new Attribute( new AttributeType(strAddressAttr)  , strAddress ) );
  attrListPerson.add( new Attribute( new AttributeType(strCityAttr)  , strCity ) );
  attrListPerson.add( new Attribute( new AttributeType(strStateRegionAttr)  , strStateRegion ) );
  attrListPerson.add( new Attribute( new AttributeType(strPostalCodeAttr)  , strPostalCode ) );
  attrListPerson.add( new Attribute( new AttributeType(strCountryAttr)  , strCountry ) );
  //Added for Organization Person Feature
  attrListPerson.add( new Attribute( new AttributeType(strMailCodeAttr)  , strMailCode ) );
  attrListPerson.add( new Attribute( new AttributeType(strTitleAttr)  , strTitle  ) );
    attrListPerson.add( new Attribute( new AttributeType(JF_Line_ManagerAttr)  , JF_Line_Manager  ) );
  
  
  if(!UIUtil.isNullOrEmpty(imdsId)) {
  attrListPerson.add( new Attribute( new AttributeType(sIMDSContactID)  , imdsId  ) );
  }
  //End
  
  double tz = (new Double((String)session.getValue("timeZone"))).doubleValue();
  String strStartInputTime = "00:00:00 AM";
  String strEndInputTime = "11:59:59 PM";
  if(strAbsenceStartDate != null && !"".equals(strAbsenceStartDate) && !"null".equals(strAbsenceStartDate))
  {
    strAbsenceStartDate = eMatrixDateFormat.getFormattedInputDateTime(context,strAbsenceStartDate,strStartInputTime,tz,request.getLocale());
  }
  if(strAbsenceEndDate != null && !"".equals(strAbsenceEndDate) && !"null".equals(strAbsenceEndDate))
  {
    strAbsenceEndDate =   eMatrixDateFormat.getFormattedInputDateTime(context,strAbsenceEndDate,strEndInputTime,tz,request.getLocale());
  }
  
  //Added for IR-025506V6R2011
  if(strDelegate.equals(context.getUser()))
  {
%>

<%@page import="com.matrixone.apps.framework.ui.UIUtil"%>
<script language ="JavaScript">
alert("<emxUtil:i18nScript localize="i18nId">emxComponents.Common.CannotDelegateToSelf</emxUtil:i18nScript>");
var url = "emxComponentsEditPeopleDialogFS.jsp?objectId=<%=XSSUtil.encodeForURL(context, strPersonId)%>&parentId=<%=XSSUtil.encodeForURL(context, strCompanyId)%>";
document.location.href=url;
</script>
<% 	  
  }
  //ends IR-025506V6R2011   
  // Absence Start, Absence end dates and Absence delegation shall not be added at all if the condition is not satisfied.
  else{
	attrListPerson.add( new Attribute( new AttributeType(strAbsenceStartDateAttr)  , strAbsenceStartDate ) );
	attrListPerson.add( new Attribute( new AttributeType(strAbsenceEndDateAttr)  , strAbsenceEndDate ) );
	attrListPerson.add( new Attribute( new AttributeType(strAbsenceDelegateAttr)  , strDelegate ) );
 }

  BusinessObject busPerson = new BusinessObject(strPersonId);
  busPerson.open(context);

  String strUserName = busPerson.getName();
  // Modify the business object's attributes.
  busPerson.setAttributes( context , attrListPerson);

  if(strDateFormat == null || "None".equals(strDateFormat))
  {
    strDateFormat = "";
  }
  if(strListSeparator == null || "None".equals(strListSeparator))
  {
    strListSeparator = "";
  }
  String personDateFormat = person.getExportImportDateFormat(context,strUserName);
  String personListSeparator = person.getExportImportDelimiter(context,strUserName);
  
  if(!strDateFormat.equals(personDateFormat))
  { 
    strDateFormat = strDateFormat.trim();
    person.setExportImportDateFormat(context, strUserName, strDateFormat);
  }
  if(!strListSeparator.equals(personListSeparator))
  {
    strListSeparator = strListSeparator.trim();
    person.setExportImportDelimiter(context, strUserName, strListSeparator);
  }
  
  if(strLanguagePref == null || "None".equals(strLanguagePref))
  {
    strLanguagePref = "";
  }
  String personLanguagePref = Person.getLanguagePreference(context,strUserName);
  //Added for VPM language preference issue
  if(!personLanguagePref.equalsIgnoreCase("kernel"))
  {
	  if(!strLanguagePref.equals(personLanguagePref))
	  {
		  strLanguagePref = strLanguagePref.trim();
          Person.setLanguagePreference(context,strUserName,strLanguagePref);
      }
  }  //Addition ends for VPM language preference issue
  // update the site
  if (strSite == null || "null".equalsIgnoreCase(strSite) || "None".equalsIgnoreCase(strSite))
  {
      strSite = "";
  }
  person.setSite(context, strSite);
  
  /*
  * update the viewer property
  */

  FormatUtil formatUtil = new FormatUtil();

  String formatsStr = EnoviaResourceBundle.getProperty(context,"emxFramework.FormatsWithViewerPreference");

  if( formatsStr != null )
  {
    StringTokenizer formatsTokenizer = new StringTokenizer(formatsStr,",");
    
    person.open(context);
    String formatToken = "";
    String format = "";
    while(formatsTokenizer.hasMoreTokens())
    {
      formatToken = (String)formatsTokenizer.nextToken();
      format = PropertyUtil.getSchemaProperty( context,formatToken );

      if( format != null && !"".equals(format))
      {
        formatUtil.setFormatName( formatToken );

        String requestParameter =  format + "viewerPreference";
        String requestParameterValue = emxGetParameter(request, requestParameter);

        if(requestParameterValue != null && !"".equals(requestParameterValue))
        {
          if("None".equals(requestParameterValue))
          {
            formatUtil.removeViewerPreference( context, person.getName());
          }
          else
          {
            formatUtil.setViewerPreference( context, person.getName(), requestParameterValue);
          }
        }
      }
    }
    person.close(context);
  }


  // Get the company we represent.
  BusinessObject busRepOrg = Company.getCompanyForRep(context,busPerson);
  String strCompRepObjId = "";
  if ( busRepOrg != null )
  {
    busRepOrg.open(context);
    strCompRepObjId = busRepOrg.getObjectId();
    busRepOrg.close(context);
  }

  // Determine if we are a company rep.
  boolean isCompanyRep = (busRepOrg != null);
  String userName = busPerson.getName().toString();
  String loginUsername = context.getUser();
  String strLoginPassword = context.getPassword();
  boolean sameUser=false;
  if(loginUsername.equals(userName))
  {
    sameUser=true;
  }
%>

<%

//start addition for bug 320306 rev0
  String sProjectUserSubRoles = EnoviaResourceBundle.getProperty(context,"emxComponents.UserSubRoles.ProjectUser");
  String sExternalProjectUserSubRoles  =   EnoviaResourceBundle.getProperty(context,"emxComponents.UserSubRoles.ExternalProjectUser");  
  String strProjectUserAlias           = "role_ProjectUser";
  String strExternalProjectUserAlias   = "role_ExternalProjectUser";  
  String strRol="";
  String sRole ="";
  String sRoleAlias ="";
  StringList strListOfRoles = new StringList(); 
  StringList projectUserSubRolesList = new StringList();
  StringList externalProjectUserSubRolesList = new StringList();
  boolean isProjectUser = false;
  boolean isExternalProjectUser = false;
  User userObjEdit = null;
  if(sProjectUserSubRoles!=null && !"null".equals(sProjectUserSubRoles))
  {
    projectUserSubRolesList = FrameworkUtil.split(sProjectUserSubRoles,",");
  }
  if(sExternalProjectUserSubRoles!=null && !"null".equals(sExternalProjectUserSubRoles))
  {
    externalProjectUserSubRolesList = FrameworkUtil.split(sExternalProjectUserSubRoles,",");  
  }
  DomainObject dom = new DomainObject(strCompanyId);
  String companyName = dom.getInfo(context,"name");
  if(Company.getHostCompany(context).equals(companyName))
  {
    strRol = FrameworkUtil.getAliasForAdmin(context, "role", strEmployeeRole, true);
    strListOfRoles.addElement(strRol);
  } 
  matrix.db.Person personObjEdit = new matrix.db.Person(strUserName);
  personObjEdit.open(context);
  UserItr userItrEdit = new UserItr(personObjEdit.getAssignments(context));
  while(userItrEdit.next()) 
  {
    userObjEdit = userItrEdit.obj();
    if(userObjEdit instanceof matrix.db.Role)
    {
      sRole = userObjEdit.getName().trim();
    }
    else
    {
      continue;
    }
    sRoleAlias = FrameworkUtil.getAliasForAdmin(context, "role", sRole, true);
    if(sRoleAlias != null)
    {
    	if(!isProjectUser && projectUserSubRolesList.contains(sRoleAlias))
        {
          isProjectUser = true;           
          strListOfRoles.addElement(strProjectUserAlias);
        }
       if(!isExternalProjectUser && externalProjectUserSubRolesList.contains(sRoleAlias))
         {
          isExternalProjectUser = true;            
          strListOfRoles.addElement(strExternalProjectUserAlias);
         }
       strListOfRoles.addElement(sRoleAlias);  
    }    
  }
  //start addition for bug 320306 rev0

  /*
   * Update the person's business unit.
   */

  // If the business unit was passed and changed, then disconnect
  // the original business unit and connect the new business unit.
  if ( strOriginalBusinessUnit != null && !strOriginalBusinessUnit.equals(strBusinessUnit)) 
  {
    // If there was an original business unit relationship, then disconnect it.
    if (! "".equals(strOriginalBUEmployee)) 
    {
         BusinessUnit businessUnitObj = new BusinessUnit(strOriginalBusinessUnit);
    	 String[] selectedPerson= {strPersonId};
         //businessUnitObj.disconnectPerson(context, selectedPerson);
         //businessUnitObj.removeMemberPersons(context, selectedPerson);
         businessUnitObj.removeEmployeePersons(context, selectedPerson);
    }

    // If a new business unit object was selected, then connect it.
    if (strBusinessUnit != null && ! "".equals(strBusinessUnit)) 
    {
      busPerson.connect(context, new RelationshipType(strBusinessUnitEmployeeRel),false, new BusinessObject(strBusinessUnit));
     // Start.addition for bug 320306,333101 rev0
	  boolean businessUnitAlreadyConnected=false;
     
      SelectList busSelect = new SelectList(1);
      busSelect.add(DomainConstants.SELECT_ID);   
      
      MapList businessUnitresultList=new MapList();
      businessUnitresultList =  new DomainObject(busPerson).getRelatedObjects(context,
    		                                      strMemberRel,         //java.lang.String relationshipPattern,
    		                                      DomainConstants.TYPE_BUSINESS_UNIT,          //java.lang.String typePattern,
                                                  busSelect,           //matrix.util.StringList objectSelects,
                                                  null,           //matrix.util.StringList relationshipSelects,
                                                  true,                //boolean getTo,
                                                  false,               //boolean getFrom,
                                                 (short)1,             //short recurseToLevel,
                                                  null,           //java.lang.String objectWhere,
                                                  null);               //java.lang.String relationshipWhere)
        if(businessUnitresultList.size() > 0){
        	  Iterator buIter = businessUnitresultList.iterator();
        	  while (buIter.hasNext()) {
    		  Map mapBU = (Map) buIter.next();
    		   String businessUnitObjId = (String)mapBU.get(DomainConstants.SELECT_ID);
             
    		   if (strBusinessUnit.equals(businessUnitObjId)){
    			   businessUnitAlreadyConnected=true;
    			   break;
    			   }
            	 }
        	   }
       
        Relationship rel=null;
        
        if(!businessUnitAlreadyConnected){
         rel = busPerson.connect(context, new RelationshipType(strMemberRel),false, new BusinessObject(strBusinessUnit));            
		
		 if(strListOfRoles.size() > 0 ) {
			String strMemberRelIdBusUnit = rel.toString();
			AttributeUtil.setAttributeList(context
                                      ,new DomainRelationship(strMemberRelIdBusUnit)
                                      ,DomainObject.ATTRIBUTE_PROJECT_ROLE
                                      ,strListOfRoles
                                      ,false
                                      ,
                                      "~");
//end addition for bug 320306,333101 rev0
        }
      }
    }
  }

  /*
   * Update the person's workplace location.
   */

  // If the location was passed and changed, then disconnect
  // the original location and connect the new location.
  if (strOriginalLocation != null && ! strOriginalLocation.equals(strLocation)) 
  {
    // If there was an original Location relationship, then disconnect it.
    if (! "".equals(strOriginalWorkPlace)) 
    {
      busPerson.disconnect(context, new Relationship(strOriginalWorkPlace));
    }
    // If a new Location object was selected, then connect it.
    if (strLocation != null && !"".equals(strLocation) ) 
    {
      busPerson.connect(context, new RelationshipType(strWorkPlaceRel),true, new BusinessObject(strLocation));
    }
  }

  // Determine if we are a host rep.
  boolean isHostRep = Company.isHostRep(context, busPerson);

  String busId = "";
  if ( strCompanyType.equals(strCompRep) ) 
  {
    busId = strCompanyId;
  }
  else if ( strBusinessUnitType.equals(strCompRep) ) 
  {
    busId = strBusinessUnit;
  }

  if( busId != null && !"".equals(busId))
  {
	// START Bug 364050
	//If the login person is Host Company Representative then only he can assign Company Representative Role to this Person
    com.matrixone.apps.common.Person busLoggedPerson = com.matrixone.apps.common.Person.getPerson(context);   
    boolean busLoggedPersonIsHostRep = com.matrixone.apps.common.Company.isHostRep(context, busLoggedPerson);
    if(busLoggedPersonIsHostRep) {
	    if ( !isCompanyRep)
	    {
	        person.addRoles(context, new StringList(DomainConstants.ROLE_COMPANY_REPRESENTATIVE));
	        if( strBusinessUnitType.equals(strCompRep)){
				BusinessObject busRepOrg1 = Company.getCompanyForRep(context,busPerson);
				busPerson.disconnect(context, new RelationshipType(strCompanyRepresentativeRel),false, busRepOrg1);
				busPerson.connect(context, new RelationshipType(strCompanyRepresentativeRel),false, new   BusinessObject(busId));
		  }
		  
	    } 
	    else if( isCompanyRep && !strCompRepObjId.equals(busId))
	    {
	      // changing of comp rep from one BU to another BU or Company
	      busPerson.disconnect(context, new RelationshipType(strCompanyRepresentativeRel),false, busRepOrg);
	
	      busPerson.connect(context, new RelationshipType(strCompanyRepresentativeRel),false, new   BusinessObject(busId));
	    }
    } else {
    	if(!isCompanyRep){
        %>
		<script language="JavaScript">
			alert("<emxUtil:i18nScript localize="i18nId">emxComponents.PeopleRoleSummary.RoleAlertMessage</emxUtil:i18nScript>");
		</script>
<% 
	  //End Bug 364050
    	}
    }
  }
  else if ( isCompanyRep )
  {
      String[] roles = {DomainConstants.ROLE_COMPANY_REPRESENTATIVE};
      person.removeRoles(context, roles);
  }

  // find the User Role
  String userRole = null;

  if (isHostRep){
    userRole = "hostRep";
  }else if (!isHostRep && isCompanyRep){
    userRole = "nonHostRep";
  }else{
    userRole = "user";
  }

  // Update the person's Vault  

  String URL = UINavigatorUtil.getCommonDirectory(context)+ "/emxTree.jsp?AppendParameters=true&emxSuiteDirectory="+XSSUtil.encodeForURL(context, appDirectory) + "&relId="+XSSUtil.encodeForURL(context, relId);

  if(!"".equals(relId)) { // if coming from Inactive People then we need replace mode.
    URL += "&mode=insert";
  } else {
    URL += "&mode=replace";
  }


  BusinessObject newObj = null;
  String newObjectId = "";
  try 
  {
	//strVault will be empty when the multi vault is enabled.
	if (UIUtil.isNotNullAndNotEmpty(strVault) && strOriginalVault != null && !strOriginalVault.equals(strVault))
	{
    	  Policy policy = busPerson.getPolicy();
   		  newObj = busPerson.change(context, busPerson.getTypeName(), strUserName, busPerson.getRevision(), strVault, policy.getName());
	      if(strUserName.equals(loginUsername))
	      {
	        context.resetVault(strVault); //because of kernel api changes, changed from setVault() to resetVault()
	      }

     	 newObj.open(context);
      	 newObjectId = newObj.getObjectId(context);
     	 newObj.close(context);

      	 URL += "&objectId=" + XSSUtil.encodeForURL(context, newObjectId);

     	 com.matrixone.apps.common.Person.refreshPerson(context,strUserName);
     }
  }
  catch (Exception ex) 
  {	 
      throw new Exception(ex.toString());
  }

  busPerson.close(context);
    if (strPassword != null && !"".equals(strPassword) && !"null".equals(strPassword)){

      MqlUtil.mqlCommand(context, "modify person $1 password $2", true, strUserName,strPassword);
      boolean exception=false;
      if(sameUser){
       try{
           context.resetContext(strUserName, strPassword, context.getVault().toString()); //because of kernel api changes, changed from setPassword() to resetContext()
           context.connect();
        }
        catch(Exception exx){
          exception=true;
        }
        if(!exception){
          Framework.setContext(session, context);
        }
      }
    }
  if ( meetingUsername != null  && !meetingUsername.equals( strOriginalMeetingUserName) ) {
%>
    <form name="personForm" method="post" action="emxComponentsCreateMeetingId.jsp">
      <input type= hidden  name="personId" value="<xss:encodeForHTMLAttribute><%=strPersonId%></xss:encodeForHTMLAttribute>"/>
      <input type= hidden  name="projectId" value=""/>
      <input type="hidden" name="userRole" value="<xss:encodeForHTMLAttribute><%=userRole%></xss:encodeForHTMLAttribute>"/>
      <input type="hidden" name="meetingUsername" value="<xss:encodeForHTMLAttribute><%=meetingUsername%></xss:encodeForHTMLAttribute>"/>
      <input type="hidden" name="meetingPassword" value="<xss:encodeForHTMLAttribute><%=meetingPassword%></xss:encodeForHTMLAttribute>"/>
      <input type= hidden  name="jsTreeID" value="<xss:encodeForHTMLAttribute><%=jsTreeID%></xss:encodeForHTMLAttribute>"/>
      <input type="hidden" name="suiteKey" value="<xss:encodeForHTMLAttribute><%=suiteKey%></xss:encodeForHTMLAttribute>"/>
      <input type= hidden  name="objectId" value="<xss:encodeForHTMLAttribute><%=strCompanyId%></xss:encodeForHTMLAttribute>"/>
      <input type= hidden  name="fromPage" value="EditPeople"/>
    </form>
    <script language="javascript">
      document.personForm.submit();
    </script>
<%
  } else {
%>
    <script language="javascript">
      if(getTopWindow().getWindowOpener().getTopWindow().modalDialog)
      {
        getTopWindow().getWindowOpener().getTopWindow().modalDialog.releaseMouse();
      }//XSSOK
      if(<%=(!"true".equals(XSSUtil.encodeForJavaScript(context, strContextUserEditProfile)))%>)
      {
        var tree = parent.window.getWindowOpener().getTopWindow().objStructureTree;
        //XSSOK
        if(tree && <%= (!"".equals(XSSUtil.encodeForJavaScript(context, newObjectId)))%>)
        {
        	//XSSOK
          if( <%=(!"".equals(XSSUtil.encodeForJavaScript(context, relId))) %>)
          {
            tree.deleteObject("<%=XSSUtil.encodeForJavaScript(context, strPersonId)%>");
          }
          var contentFrameObj = findFrame(getWindowOpener().getTopWindow(), "content");
          //XSSOK
          contentFrameObj.document.location.href= "<%= URL %>&jsTreeID=" + tree.getSelectedNode().nodeID;
        }
        else
        {
          getWindowOpener().parent.document.location.href=getWindowOpener().parent.document.location.href;
        }
      }
      window.closeWindow();
    </script>
<%
  }
     PersonUtil.clearPersonCache(context,strUserName,null);
%>
<%@include file = "../emxUICommonEndOfPageInclude.inc"%>
