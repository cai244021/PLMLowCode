<!--Begin:Addition:Form Single Page Properties and Files-->
<%
            final String ATTRIBUTE_IS_VERSION_OBJECT = PropertyUtil.getSchemaProperty(context, "attribute_IsVersionObject");
            final String SELECT_ATTRIBUTE_IS_VERSION_OBJECT = "attribute[" + ATTRIBUTE_IS_VERSION_OBJECT + "]";

            StringList slBusSelect = new StringList();
            slBusSelect.add("vcfile");
            slBusSelect.add("vcfolder");
            slBusSelect.add(DomainObject.SELECT_TYPE);
            slBusSelect.add(SELECT_ATTRIBUTE_IS_VERSION_OBJECT);
            
            DomainObject dmoObject = new DomainObject(objectId);
            Map mapObjectInfo = dmoObject.getInfo(context, slBusSelect);

            String vcfile = (String)mapObjectInfo.get("vcfile");
            String vcfolder = (String)mapObjectInfo.get("vcfile");
            String busType = (String)mapObjectInfo.get(DomainObject.SELECT_TYPE);
            String strIsVersionObject = (String)mapObjectInfo.get(SELECT_ATTRIBUTE_IS_VERSION_OBJECT);

            String languageStr = request.getHeader("Accept-Language");
			String othertoolbarparams = emxGetParameter(request,"otherTollbarParams");
			String toBeAppendparams = "";
			if(othertoolbarparams != null && !"".equals(othertoolbarparams)) {
	        	StringList paramList = FrameworkUtil.split(othertoolbarparams, ",");
	            for(int ii =0; ii < paramList.size(); ii++){
          			String paramName = (String)paramList.get(ii);
          			if(paramName.equals("treeLabel") || paramName.equals("isStructure")) {
          				continue;
          			}
          			toBeAppendparams += "&"+ XSSUtil.encodeForURL(context,paramName) + "=" + XSSUtil.encodeForURL(context,(String) requestMap.get(paramName));
	           }
	        }

            // Is the page invoked in Printer Friendly mode ?
            boolean isPrinterFriendly = "true".equalsIgnoreCase(emxGetParameter(request, "PFmode"));

            //
            // Following code will show the file summary table for the document object which is not DSFA document.
            // Depending on if the document is versionable or non-versionable the corresponding file summary will be shown.
            // To show the file summary existing UI table objects will be used. The emxTable.jsp page will be rendered inside iframe tag.
            //

            // Check if it is not DesignSync File
            if ("false".equalsIgnoreCase(vcfile) && "false".equalsIgnoreCase(vcfolder))
            {
                // Decide if versioning is allowed
                boolean allowVersioning = false;
                String parentType = com.matrixone.apps.common.CommonDocument.getParentType(context, busType);

                if( com.matrixone.apps.common.CommonDocument.TYPE_DOCUMENTS.equals(parentType) )
                {
                    String property = PropertyUtil.getAdminProperty(context, "Type", busType, "DISALLOW_VERSIONING");
                    allowVersioning = !("true".equalsIgnoreCase(property));
                }

                String strTableURL = null;
                String usertable="";
                if(allowVersioning)
                {
                    if (!"True".equalsIgnoreCase(strIsVersionObject))
                    {
                        if (!isPrinterFriendly) {
                        
                        	String strDisplayCDMFileSummaryTable = emxGetParameter(request, "displayCDMFileSummaryTable");
                        	if(null!=strDisplayCDMFileSummaryTable && ""!=strDisplayCDMFileSummaryTable)
                        		usertable=strDisplayCDMFileSummaryTable;
                        	else
                        		usertable="APPFileSummary";
                        	
			// strTableURL = "../common/emxIndentedTable.jsp?program=emxCommonFileUI:getFiles&table="+usertable+"&otherTollbarParams="+XSSUtil.encodeForURL(context,othertoolbarparams)+toBeAppendparams+"&selection=multiple&sortColumnName=Name&sortDirection=ascending&HelpMarker=emxhelpdocumentfilelist&objectId="+ XSSUtil.encodeForURL(context,objectId) + "&StringResourceFileId=emxComponentsStringResource&pagination=0&Export=false&objectCompare=false&PrinterFriendly=false&suiteKey=Components&FilterFramePage=../components/emxCommonDocumentCheckoutUtil.jsp&FilterFrameSize=1&header=emxComponents.Menu.Files&treePopup=true&showClipboard=false&navButton=false";
                         strTableURL = "../common/emxIndentedTable.jsp?program=emxCommonFileUI:getFiles&toolbar=CUS_DocShowOnlyOfficeActionToolbar&table="+usertable+"&otherTollbarParams="+XSSUtil.encodeForURL(context,othertoolbarparams)+toBeAppendparams+"&selection=multiple&sortColumnName=Name&sortDirection=ascending&HelpMarker=emxhelpdocumentfilelist&objectId="+ XSSUtil.encodeForURL(context,objectId) + "&StringResourceFileId=emxComponentsStringResource&pagination=0&Export=false&objectCompare=false&PrinterFriendly=false&suiteKey=Components&FilterFramePage=../components/emxCommonDocumentCheckoutUtil.jsp&FilterFrameSize=1&header=emxComponents.Menu.Files&treePopup=true&showClipboard=false&navButton=false";
			}
                        else {
                            strTableURL = "";
                        }
                    }
                    else
                    {
                        // Do nothing
                        // No files table will be shown
                    }
                }
                else
                {
                    if (!isPrinterFriendly) {
                        strTableURL = "../common/emxIndentedTable.jsp?program=emxCommonFileUI:getNonVersionableFiles&otherTollbarParams="+XSSUtil.encodeForURL(context,othertoolbarparams)+toBeAppendparams+"&table=APPNonVersionableFileSummary&selection=multiple&HelpMarker=emxhelpdocumentfilelist&objectId="+ XSSUtil.encodeForURL(context,objectId) + "&StringResourceFileId=emxComponentsStringResource&pagination=0&Export=false&objectCompare=false&PrinterFriendly=false&objectBased=false&suiteKey=Components&FilterFramePage=../components/emxCommonDocumentCheckoutUtil.jsp&AppendFileName=true&FilterFrameSize=1&header=emxComponents.Menu.Files&sortColumnName=Name&sortDirection=ascending&showClipboard=false&navButton=false";
                    }
                    else {
                        strTableURL = "";
                    }
                }

                // If table url is formed then show the table
                if (strTableURL != null)
                {
        %>
                    <!-- //XSSOK -->
                    <iframe src="<%=strTableURL%>" name="frameFilesTable" id="frameFilesTable" frameborder="0"></iframe>
        <%
                }
        %>
                <SCRIPT LANGUAGE="JavaScript">
                <!--
                    //
                    // The command handler functions required when user clicks "Delete All Versions", "Delete This Version" and "Delete Selected Files" commands
                    // on document properties page.
                    //

                    function checkinUpload_onclick() {
                        //XSSOK
                        showModalDialog("<%=Framework.encodeURL(response, "../components/emxCommonDocumentPreCheckin.jsp?objectAction=checkin&objectId=" + XSSUtil.encodeForURL(context, objectId) +"&suiteKey=" +  XSSUtil.encodeForURL(context, suiteKey))%>" , 780, 570, true);
                    }

                    function updateFiles_onclick() {
                        //XSSOK
                        showModalDialog("<%=Framework.encodeURL(response, "../components/emxCommonDocumentPreCheckin.jsp?objectAction=update&showFormat=readonly&showComments=required&objectId=" + XSSUtil.encodeForURL(context, objectId) +"&suiteKey=" + XSSUtil.encodeForURL(context, suiteKey))%>" , 875, 525, true);
                    }

                    function download_onclick() {
                       return checkoutSelectedFiles("download");
                    }

                    function checkout_onclick() {
                       return checkoutSelectedFiles("checkout");
                    }

                    function deleteThisVersion_onclick() {
                        commonDeleteFileUtil('deleteVersion');
                    }

                    function deleteAllVersions_onclick() {
                        commonDeleteFileUtil('deleteFile');
                    }

                    function deleteSelectedFiles_onclick() {
                        commonDeleteFileUtil('deleteNonVersionableFile');
                    }

                    function getFilesTableForm() {
                        var frameFilesTable = findFrame(window, 'frameFilesTable');
                        if (frameFilesTable) {

                            // Find the frame in which the files listing is shown
                            emxTableForm = frameFilesTable.document.forms['emxTableForm'];
                                if (emxTableForm) {
                                    return emxTableForm;
                                }
                            }

                        return null;
                    }
                    function checkoutSelectedFiles(action) {
						// Find the name of the form in table
                        var emxTableForm = getFilesTableForm();
                        if (emxTableForm != null) {
                            //Find if user has selected at least one file
                            var frameFilesTable = findFrame(window, 'frameFilesTable');
                            
                            var selectedIds = "";
                            
                            for(i = 0; i<frameFilesTable.emxEditableTable.getCheckedRows().length; i++){
                            	selectedIds = selectedIds + frameFilesTable.emxEditableTable.getCheckedRows()[i].getAttribute("o")+ ",";
                            }
                            // If no file is selected then show error
                            if (frameFilesTable.emxEditableTable.getCheckedRows().length <= 0) {
                                alert("<%=i18nNow.getI18nString("emxComponents.Common.SelectFile", "emxComponentsStringResource", languageStr)%>");
                                return;
                            }
						    var contentStoreURL = "../components/emxComponentsStoreFormDataToSession.jsp" ;
						    var queryString = "emxTableRowId="+selectedIds+"&action="+action+"&parentOID=<%=XSSUtil.encodeForURL(context,objectId)%>&objectId=<%=XSSUtil.encodeForURL(context,objectId)%>"+
						    	"&StringResourceFileId=emxComponentsStringResource&SuiteDirectory=components&suiteKey=Components";
						    var xmlResult = emxUICore.getXMLDataPost(contentStoreURL, queryString);
					        var root = xmlResult.documentElement;
							var sessionKey = emxUICore.getText(emxUICore.selectSingleNode(root, "/mxFormData/sessionKey"));
							require(["DS/MSFDocumentManagement/MSFDocumentClient"], function(MSFDocumentClient) {
								if(MSFDocumentClient.isConnectedWithMSF() === "true") {
									callCheckout("<%=XSSUtil.encodeForJavaScript(context,objectId)%>",action,"","","","","","","","","","","","","","", "", "","", selectedIds);
								}
								else
									callCheckout("","","","","","","","","","","","","","","","true",sessionKey);
							}, function() {
								callCheckout("","","","","","","","","","","","","","","","true",sessionKey);
							});
                        } else {
                			callCheckout("<%=XSSUtil.encodeForJavaScript(context,objectId)%>","checkout");
                		}
            		}

                    function commonDeleteFileUtil(sAction) {

                        // Find the name of the form in table
                        var emxTableForm = getFilesTableForm();
                        if (emxTableForm != null) {
                            // If no file is selected then show error
                            var frameFilesTable = findFrame(window, 'frameFilesTable');
                            if (frameFilesTable.emxEditableTable.getCheckedRows().length <= 0) {
                                alert("<%=i18nNow.getI18nString("emxComponents.Common.SelectFile", "emxComponentsStringResource", languageStr)%>");
                                return;
                            }

                            // Confirm for deletion and then delete
                            if (window.confirm("<%=i18nNow.getI18nString( "emxComponents.Alert.RemoveFile", "emxComponentsStringResource", languageStr)%>")) {
                                // Submit the form in hidden frame
                                emxTableForm.action = "../components/emxCommonDocumentRemove.jsp?action="+sAction;
                                emxTableForm.method="post";
                                emxTableForm.target = "formViewHidden";
                                addSecureToken(emxTableForm);
                                emxTableForm.submit();
                                removeSecureToken(emxTableForm);
                            }
                        }
                    }

        <%
                    // If the page is opened in printer friendly mode, get the timestamp of the ui table
                    // from the already opened table page and then load the printer friendly table in the iframe.
                    if (isPrinterFriendly) {
        %>
                        var strTimestamp = "";

                        // Find the reference of the window where the document properties are displayed
                        var objPageContentWnd = findFrame(window.getWindowOpener().parent, "formViewDisplay");
                        if (objPageContentWnd) {
                            // Find the reference of the iframe where the document files are displayed
                            var objIframe = objPageContentWnd.frames["frameFilesTable"];
                            if (objIframe) {
                                // Find the reference of the window where the document files table is displayed
                                var objListDisplayWnd = objIframe.frames['listDisplay'];
                                if (objListDisplayWnd) {
                                    // Find the reference of the form emxTableForm in window where the document files table is displayed
                                    var emxTableForm = objListDisplayWnd.document.forms['emxTableForm'];
                                    if (emxTableForm) {
                                        // Find the value of timestamp input element from the form found
                                        for (var i = 0; i < emxTableForm.elements.length; i++) {
                                            if (emxTableForm.elements[i].name == 'timeStamp') {
                                                strTimestamp = emxTableForm.elements[i].value;
                                                break;
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        var iFrameFilesTable = window.frames["frameFilesTable"];
                        if (iFrameFilesTable && strTimestamp != "") {
                            iFrameFilesTable.location.href = "../common/emxTableReportView.jsp?timeStamp=" + strTimestamp + "&reportFormat=HTML&TransactionType=read";
                        }
        <%
                    }
        %>
                //-->
                </SCRIPT>
        <%
            }
            else {
                // Do nothing
                // No files table will be shown
                // DSFA summary is not supported
            }
        %>
        <script language="JavaScript">
        <!--
            turnOffProgress();
        //-->
        </script>

<div id="checkoutdiv">
  <form name="commonDocumentCheckout" id="commonDocumentCheckout" method="post" action="../components/emxCommonDocumentPreCheckout.jsp">
      <input type="hidden" name="objectId" />
      <input type="hidden" name="action" />
      <input type="hidden" name="format" />
      <input type="hidden" name="fileName" />
      <input type="hidden" name="appName" />
      <input type="hidden" name="appDir" />
      <input type="hidden" name="closeWindow" />
      <input type="hidden" name="refresh" />
      <input type="hidden" name="trackUsagePartId" />
      <input type="hidden" name="version" />
      <input type="hidden" name="customSortColumns" />
      <input type="hidden" name="customSortDirections" />
      <input type="hidden" name="uiType" />
      <input type="hidden" name="table" />
      <input type="hidden" name="getCheckoutMapFromSession" />
      <input type="hidden" name="fromDataSessionKey" />
      <input type="hidden" name="parentOID" />
      <input type="hidden" name="appProcessPage" />
      <input type="hidden" name="portalMode" />      
      <input type="hidden" name="frameName" />
      <input type="hidden" name="id" />
      <input type="hidden" name="fileAction" />
      <input type="hidden" name="file" />
      <input type="hidden" name="versionId" />
  </form>
</div>

