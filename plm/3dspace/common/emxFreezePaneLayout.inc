<!DOCTYPE html>
<html>
	<head>	
	<title></title>
		
<link rel="shortcut icon" href="../favicon.ico" type="image/x-icon" />
		<link rel="stylesheet" href="styles/emxUIExtendedHeader.css"/>
        <link rel="stylesheet" href="styles/emxUIDefault.css"/>
        <link rel="stylesheet" href="styles/emxUIToolbar.css"/>
        <link rel="stylesheet" href="styles/emxUIMenu.css"/>
        <link rel="stylesheet" href="styles/emxUIStructureBrowser.css"/>
        <link rel="stylesheet" href="../common/emxUserDefinedStyleForSB.jsp"/>
        <script src="../webapps/ENOAEFCore/webroot/common/scripts/emxUICore.js?v=20250929" type="text/javascript"></script> 
        <script src="../webapps/ENOAEFCore/webroot/common/scripts/emxUIConstants.js" type="text/javascript"></script> 
        		<%
        		String LoadingLable = "";
        		if(!freezePaneLayoutData.containsKey("Loading")){
        			LoadingLable = String.valueOf(freezePaneLayoutData.get("Loading"));
        		}
        		
        		%>
		<script>		
		var freezePaneLayoutData = <%= UIUtil.isNotNullAndNotEmpty(fPLayoutDataString) ? fPLayoutDataString : "" %>;
        if( freezePaneLayoutData.isUnix  == "true"){
        	emxUICore.addStyleSheet("emxUICalendar_Unix");
        }else{
        	emxUICore.addStyleSheet("emxUICalendar");
        }
        </script>        
        <link rel="stylesheet" href="styles/MiniMap.css"/>
        <link rel="stylesheet" href="../webapps/UIKIT/UIKIT.css"/>  
    	<link rel="stylesheet" href="../common/styles/emxUIImageManagerInPlace.css"/>
    	<link rel="stylesheet" href="../plugins/libs/jqueryui/1.10.3/css/jquery-ui.css"/>
    	<script>
    	if(freezePaneLayoutData.portalMode != "false"){
			emxUICore.addStyleSheet("emxUIChannelDefault");
		}
		if(freezePaneLayoutData.hideFooter != "true" && ((freezePaneLayoutData.submitLabel && freezePaneLayoutData.submitLabel != "") || (freezePaneLayoutData.cancelLabel && freezePaneLayoutData.cancelLabel !="")  || (freezePaneLayoutData.submitURL && freezePaneLayoutData.submitURL != "")  || (freezePaneLayoutData.callbackFunction && freezePaneLayoutData.callbackFunction != "" )) && freezePaneLayoutData.fullTextSearch != "true") {
			emxUICore.addStyleSheet("emxUIDialog");
		}
    	</script>    	
        <script type="text/javascript">adjustLastColumn = new Function();</script>
        <script src="../webapps/VENENOFrameworkPlugins/jQuery.js" type="text/javascript"></script>
        <script src="../webapps/AmdLoader/AmdLoader.js"></script>
        <script type="text/javascript">window.dsDefaultWebappsBaseUrl = "../webapps/";</script>
        <script src="../webapps/WebappsUtils/WebappsUtils.js"></script>
        <script>
          	require(['DS/WebappsUtils/WebappsUtils'], function (WebappsUtils){WebappsUtils._setWebappsBaseUrl('../webapps/');});
        </script>
        <script src="../webapps/c/UWA/js/UWA_W3C_Alone.js"></script>
        <script src="../webapps/TagNavigatorProxy/TagNavigatorProxy.js"></script>
        <script src="../webapps/VENENOFrameworkPlugins/jshashtable/HashTable.js" type="text/javascript"></script>
        <script src="../webapps/VENENOFrameworkPlugins/jquery-numberformatter/jquery.formatNumber.js" type="text/javascript"></script>
        <script src="../common/scripts/emxJSValidationUtil.js" type="text/javascript"></script>
        <script src="../webapps/ENOAEFCore/webroot/common/scripts/emxExtendedPageHeaderFreezePaneValidation.js" type="text/javascript"></script>
        <script src="../webapps/VENENOFrameworkPlugins/jquerystorage/jquery.storageapi.js"></script>
        
        <script src="../webapps/ENOAEFCore/webroot/common/scripts/emxUIModal.js" type="text/javascript"></script>
        <script src="../webapps/ENOAEFCore/webroot/common/scripts/emxQuery.js" type="text/javascript"></script>
        <script src="../webapps/ENOAEFCore/webroot/common/scripts/emxUICoreMenu.js" type="text/javascript"></script>
        <script src="../webapps/ENOAEFCore/webroot/common/scripts/emxUIToolbar.js" type="text/javascript"></script>
        <script src="../webapps/VENENOFrameworkPlugins/jshtml2canvas/html2canvas.js"></script>
        <script src="../webapps/VENENOFrameworkPlugins/jscanvg/canvg.js"></script>
        <script src="../webapps/ENOAEFStructureBrowser/webroot/common/scripts/emxFindInCntrl.js" type="text/javascript"></script>
		<script src="scripts/crosshl3DPlay.js"></script>
        <script src="../webapps/ENOAEFStructureBrowser/webroot/common/scripts/emxUIFreezePane.js" type="text/javascript"></script>
        <script src="../webapps/ENOAEFStructureBrowser/webroot/common/scripts/emxUIFreezePaneEdit.js" type="text/javascript"></script>
        <script src="../webapps/ENOAEFCore/webroot/common/scripts/emxNavigatorHelp.js" type="text/javascript"></script>
        <script src="../webapps/ENOAEFCore/webroot/common/scripts/emxUITableUtil.js" type="text/javascript"></script>
        <script src="../webapps/ENOAEFCore/webroot/common/scripts/emxUICalendar.js" type="text/javascript"></script>
        <script src="../webapps/ENOAEFCore/webroot/common/scripts/emxUIPopups.js" type="text/javascript"></script>
        <script src="../webapps/ENOAEFCore/webroot/common/scripts/emxTypeAhead.js" type="text/javascript"></script>
        <script src="../webapps/ENOAEFCore/webroot/common/scripts/bpsTagNavConnector.js" type="text/javascript"></script>
        <script src="../webapps/ENOAEFCore/webroot/common/scripts/bpsTagNavSBInit.js" type="text/javascript"></script>
        <script src="../webapps/ENOAEFCore/webroot/common/scripts/bpsSBFilter.js" type="text/javascript"></script>
        <script src="../webapps/ENOAEFCore/webroot/common/scripts/dsTouchEvents.js" type="text/javascript"></script>
        
        <script src="scripts/util.js" ></script>
        <script src="scripts/Callbacks.js"></script>
        <script src="scripts/Deferred.js"></script>
        <script src="../webapps/ENOAEFStructureBrowser/GraphicalSB/html.js" ></script>   
        <script src="scripts/svg.js" ></script>
        <script src="scripts/animation.js" ></script>
        <script src="scripts/Tree.js"></script>
        <script src="scripts/VisualTree.js"></script>
        <script src="scripts/HybridVisualTree.js"></script>
        <script src="scripts/SVGVisualTree.js"></script>
        <script src="scripts/VisualLayout.js"></script>
        <script src="scripts/Camera.js"></script>
        <script src="../webapps/ENOAEFStructureBrowser/GraphicalSB/CoreController.js"></script>
        <script src="scripts/State.js"></script>
        <script src="scripts/History.js"></script>
        <script src="scripts/ContextMenu.js"></script>
        <script src="scripts/MiniMap.js"></script>
        <script src="../webapps/ENOAEFStructureBrowser/GraphicalSB/slider.js"></script>
        
        <script src="../webapps/ENOAEFStructureBrowser/GraphicalSB/UIAction.js"></script>
        <script src="../webapps/ENOAEFStructureBrowser/GraphicalSB/UIActionCamera.js"></script>
        <script src="../webapps/ENOAEFStructureBrowser/GraphicalSB/UIActionCameraLayout.js"></script>
        <script src="../webapps/ENOAEFStructureBrowser/GraphicalSB/UIActionExpand.js"></script>
        <script src="../webapps/ENOAEFStructureBrowser/GraphicalSB/UIActionExpandGlobal.js"></script>
        <script src="../webapps/ENOAEFStructureBrowser/GraphicalSB/UIActionDblClickExpand.js"></script>
        <script src="../webapps/ENOAEFStructureBrowser/GraphicalSB/UIActionIsolate.js"></script>
        <script src="../webapps/ENOAEFStructureBrowser/GraphicalSB/UIActionLoadTree.js"></script>
        <script src="../webapps/ENOAEFStructureBrowser/GraphicalSB/UIActionSearch.js"></script>
	    <script src="../webapps/ENOAEFStructureBrowser/GraphicalSB/NodeDragManager.js"></script>
        <script src="../webapps/ENOAEFStructureBrowser/GraphicalSB/SampleTree.js"></script>
    	<script src="../webapps/ENOAEFStructureBrowser/GraphicalSB/LoadTree.js"></script>
    	<script src="../webapps/ENOAEFStructureBrowser/GraphicalSB/Controller.js"></script>
    	<script src="../webapps/UIKIT/UIKIT.js"></script>
    	<script src="../plugins/libs/jqueryui/1.10.3/js/jquery-ui.js"></script>
    	<script>
		jQuery(document).attr('title', freezePaneLayoutData.PageHeader);
    	if(freezePaneLayoutData.isIE == "false"){
			document.write("<scri" + "pt language=\"JavaScript\" type=\"text/javascript\" src=\"../webapps/VENENOFrameworkPlugins/jquerywaitforimages/jquery-waitforimages.js\"></scr" + "ipt>");
		}
    	</script>    	
	    <script src="../webapps/ENOAEFCore/webroot/common/scripts/emxUIRTE.js" type="text/javascript"></script>
	    <script src="../webapps/ENOAEFCore/webroot/common/scripts/emxUIRTEToolbar.js" type="text/javascript"></script>
	    <script src="../webapps/ENOAEFCore/webroot/common/scripts/setdragimage-ie.js" type="text/javascript"></script>
    	<script>
 		if(freezePaneLayoutData.hasNumericColumns == "true"){    
 			document.write("<scri" + "pt language=\"JavaScript\" type=\"text/javascript\" src=\"../webapps/ENOAEFStructureBrowser/webroot/common/scripts/emxUIFreezePaneCalculations.js\"></scr" + "ipt>");
   		}
   		if(freezePaneLayoutData.spellCheckerURL && freezePaneLayoutData.spellCheckerURL.length >0){
   			document.write("<scri" + "pt language=\"JavaScript\" type=\"text/javascript\" src="+ freezePaneLayoutData.spellCheckerURL +"></scr" + "ipt>");
       		scaytConfig = {
              defaultLang: navigator.userLanguage || navigator.language || "en_US"
            };
         }

    	var persistSort = "<%=persistSort%>";
		var uiType = "structureBrowser";
     	var timeStamp = freezePaneLayoutData.timeStamp;
     	var DEC_PREC = parseInt(freezePaneLayoutData.DecimalPrecision);
     	var calRowsPresent = freezePaneLayoutData.hasNumericColumns;
     	var rowHeight = freezePaneLayoutData.rowHeight;
     	var thumbnailFieldCount = parseInt(freezePaneLayoutData.thumbnailFieldCount);
     	var tileFieldCount = parseInt(freezePaneLayoutData.tileFieldCount);
     	var noImageFile = freezePaneLayoutData.noImageFile;
     	var freezePaneMode = freezePaneLayoutData.mode;
     	var objectId = freezePaneLayoutData.objectId;
     	if(!freezePaneLayoutData.objectId){
     		objectId = ""; 
     	}
     	if(!freezePaneLayoutData.relId){
     		freezePaneLayoutData.relId = ""; 
     	}
     	var totalRows = freezePaneLayoutData.totalRows;
     	var urlParameters = freezePaneLayoutData.urlParameters;
     	jQuery(document).ready(function() {
     		urlParameters = decodeURIComponent(urlParameters);
     	});
     
	     var split = freezePaneLayoutData.split;
	     var gblMaxCellsToDraw = freezePaneLayoutData.maxCellsToDraw;
	     var gblScrollPageSize = freezePaneLayoutData.scrollPageSize;  
	     var colMap = new columnMap();
	     var objCol = null;
	     var preProcessJavaScript = freezePaneLayoutData.preProcessJavaScript;
	     var preProcessJPO = freezePaneLayoutData.preProcessJPO;
	     var preProcessURL = freezePaneLayoutData.preProcessURL;
	     var postProcessJPO = freezePaneLayoutData.postProcessJPO;
	     var postProcessURL = freezePaneLayoutData.postProcessURL;
	     var cancelProcessJPO = freezePaneLayoutData.cancelProcessJPO;
	     var cancelProcessURL = freezePaneLayoutData.cancelProcessURL;
	     var portalMode = freezePaneLayoutData.portalMode;
	     var isStructureCompare = freezePaneLayoutData.IsStructureCompare;
	     var structureCompareLevel = freezePaneLayoutData.compareLevel;
	     var parentOID = freezePaneLayoutData.parentOID;
	     var multiColumnSort = freezePaneLayoutData.multiColumnSort;
	     var sortColumnName = "";	 
	     var sortDirection = "";
    	 if(freezePaneLayoutData.userTable === "true"){
    		sortColumnName = freezePaneLayoutData.customSortColumns;
    	    sortDirection = freezePaneLayoutData.customSortDirections;
    	 }else{
    		sortColumnName = freezePaneLayoutData.sortColumnName;
    	    sortDirection = freezePaneLayoutData.sortDirection;
    	 }
    	 if(!sortColumnName){
    		 sortColumnName = "";
    	 }
    	 if(!sortDirection){
    		 sortDirection = "";
    	 }
	   	var objectString = freezePaneLayoutData.objectString;
	   	var objectsString = freezePaneLayoutData.objectsString;
	   	var selectedString = freezePaneLayoutData.selectedString;
	   	var consolidatedSearch = freezePaneLayoutData.searchType;
	   	var enterValueString = freezePaneLayoutData.enterValueString;
	   	var clearString = freezePaneLayoutData.clearString;
	   	var jsTreeID = freezePaneLayoutData.jsTreeID;
	   	var logPerformance = freezePaneLayoutData.logPerformance;
	   	buildColumnMap(freezePaneLayoutData.columns);
   		</script>
   		
    <script type="text/javascript">
    //g1f <xsl:value-of disable-output-escaping="yes" select="toolbarCode"/>
    <%= freezePaneLayoutData.getString("toolbarCode") %> 

          //overridden common methods
          function exportData(){
          fpExportData();
          }

          function openPrinterFriendlyPage(){
          fpOpenPrinterFriendlyPage();
          }
          
          function openRenderPDFPage(){
          fpOpenRenderPDFPage();
          } 
          
          function showTableCalcOptions(){
          fpShowTableCalcOptions();
          }

     	  for(var indexCustomFile=0; indexCustomFile < freezePaneLayoutData.customValidationFiles.length ; indexCustomFile++){
			document.write("<scri" + "pt language=\"JavaScript\" type=\"text/javascript\" src="+ freezePaneLayoutData.customValidationFiles[indexCustomFile] +"></scr" + "ipt>");			
		  } 
     </script>     

        <link rel="stylesheet" type="text/css" href="styles/emxUIThumbnailGallery.css"/>
      <script>
	     if(freezePaneLayoutData.isMobile == "true" || freezePaneLayoutData.isPCTouch == "true"){
	          addStyleSheet("emxUIMobile","../common/mobile/styles/");
	     }
      </script>  
	 </head>
	 
	 <body onload="editableTable.init()" onresize="editableTable.adjustTableLastColumn()">
	 <script>
		 if(freezePaneLayoutData.isIE == "true"){
		 	jQuery('body').attr("onunload","cleanupFPSession()");
		 }else{
		 	jQuery('body').attr("onbeforeunload","cleanupFPSession()");
		 }
		 if(freezePaneLayoutData.hideFooter != "true" && ((freezePaneLayoutData.submitLabel && freezePaneLayoutData.submitLabel != "") || (freezePaneLayoutData.cancelLabel && freezePaneLayoutData.cancelLabel !="")  || (freezePaneLayoutData.submitURL && freezePaneLayoutData.submitURL != "")  || (freezePaneLayoutData.callbackFunction && freezePaneLayoutData.callbackFunction != "" ))) {
		 	jQuery('body').addClass("structure-browser dialog");
		 }else{
		 	jQuery('body').addClass("structure-browser");
		 }
	 </script>
        <script src="../webapps/ENOAEFCore/webroot/common/scripts/emxUITooltips.js" type="text/javascript"></script>
        			<div id="spinner_div1" class="spinner-mask">
					  <div class="spinner spinning fade in">
						<span class="spinner-bar"></span>
						<span class="spinner-bar spinner-bar1"></span>
						<span class="spinner-bar spinner-bar2"></span>
						<span class="spinner-bar spinner-bar3"></span>
						<span class="">&#160;<%=LoadingLable%> </span>
					  </div>
					</div>
        <form name="emxTableForm">
        	<div class="pageHead" id="pageHeadDiv"></div> 		 								
 		   	<div id="mx_divBody" style="display:none">
		 		<div id="spinner_div" class="spinner-mask">		 
				  <div class="spinner spinning fade in">
					<span class="spinner-bar"></span>
					<span class="spinner-bar spinner-bar1"></span>
					<span class="spinner-bar spinner-bar2"></span>
					<span class="spinner-bar spinner-bar3"></span>
				  </div>
				</div>
 		   	</div>
 		   	<div id="mx_divThumbnailBody" style="display:none"></div>
          	<div id="mx_divTreeGraphBody" style="display:none"></div>
          	<input type="hidden" name="rowIds" />
          	<input type="hidden" name="groupData" />
          	<input type="hidden" name="groupRowIndex" />
          	<input type="hidden" name="bGroupingRows" />
          	<input type="hidden" name="csvData" />
          	<input type="hidden" name="postDataXML" />
          	<input type="hidden" name="timeStamp"></input>
        </form>
                
        <form name="emxSubmitForm" style="display:none">   
        </form>
        <script>
	        drawHeaderTollbarItems();
			drawFreezePanetTreeAndTableLayout();
			drawFooterItems();          	
			apeendDivPostDataInForm(); 
        	apeendParametersInSubmitForm(objectId, timeStamp);
     	</script>
        
        <iframe id="listHidden" class="hidden-frame" name="listHidden" width="0%" height="0%"/>
      </body>
    </html>
