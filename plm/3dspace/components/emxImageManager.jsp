  <%-- emxImageManager.jsp
   Copyright (c) 1992-2020 Dassault Systemes.
   All Rights Reserved.
   This program contains proprietary and trade secret information of MatrixOne,Inc.
   Copyright notice is precautionary only
   and does not evidence any actual or intended publication of such program

   static const char RCSID[] = $Id: emxImageManager.jsp.rca 1.2.2.5.1.5 Wed Jul  2 11:55:31 2008 msikhinam Experimental $
--%>

<%@include file = "../common/emxNavigatorInclude.inc"%>
<%@include file = "../common/emxNavigatorTopErrorInclude.inc"%>
<%@include file = "../emxTagLibInclude.inc"%>

<emxUtil:localize id="i18nId" bundle="emxComponentsStringResource" locale='<%= XSSUtil.encodeForHTMLAttribute(context, request.getHeader("Accept-Language")) %>' />

<html>
<head>


<script language="javascript" src="../common/scripts/emxUIConstants.js"></script>
<script language="javascript" src="../common/scripts/emxUICore.js"></script>
<script language="javascript" src="../common/scripts/emxUICoreMenu.js"></script>
<script language="JavaScript" src="../common/scripts/emxUIToolbar.js"></script>
<script language="javascript" src="../common/scripts/emxNavigatorHelp.js"></script>
<script language="javascript" src="../common/scripts/emxUIModal.js"></script>
<script language="JavaScript" src="emxUIImageManager.js"></script>
<script language="javascript" src="ENOVIA3DLiveExamine.js"></script>
<script language="javascript" src="ENOVIA3DLiveExamineExtension.js"></script>
<script type="text/javascript" src="../webapps/AmdLoader/AmdLoader.js"></script>
<script src="../webapps/c/UWA/js/UWA_W3C_Alone.js"></script>

<%@include file = "emxImageManager.inc"%>

<script language="JavaScript" type="text/JavaScript">
    function ImageInfo( index, fileName, imageURL, fileExtn, physicalID,dType,serverUrl) {  
        this.index = index;  
        this.fileName = fileName;  
        this.imageURL = imageURL;
        this.fileExtn = fileExtn;
        this.physicalId = physicalID;
		if("cgr" == fileExtn && "INV Component" == dType){
                this.dType = fileExtn;
        }else{
                this.dType = dType;
        }
        this.serverUrl = serverUrl;
    }
    addStyleSheet("emxUIDefault"); 
    addStyleSheet("emxUIDOMLayout");
    addStyleSheet("emxUIToolbar");
    addStyleSheet("emxUIMenu");
    addStyleSheet("emxUIDOMImageViewer");
    <framework:ifExpr expr="<%=isPopUpWin%>">
        addStyleSheet("emxUIDialog");
    </framework:ifExpr>     

    var backImage = new Array();
    <framework:ifExpr expr="<%=hasImages%>">
<%

	String physicalId = "";
	String dType="";
	String serverUrl = Framework.getFullClientSideURL(request,null,"");	
	
	String type_MCADAssembly       = PropertyUtil.getSchemaProperty("type_MCADAssembly");
	String type_MCADComponent       = PropertyUtil.getSchemaProperty("type_MCADComponent");
	String type_MCADRepresentation       = PropertyUtil.getSchemaProperty("type_MCADRepresentation");
	String type_MCADDrawing       = PropertyUtil.getSchemaProperty("type_MCADDrawing");

	
	if(object.isKindOf(context, type_MCADAssembly) || object.isKindOf(context, type_MCADComponent) || object.isKindOf(context, type_MCADRepresentation)|| object.isKindOf(context, type_MCADDrawing)) {

    	StringList selects = new StringList();
    	selects.add("physicalid");
    	selects.add("type");
		
    	Map objMap = object.getInfo(context, selects);
		physicalId = (String)objMap.get("physicalid");
		dType=(String)objMap.get("type");
		
	}else if (object.isKindOf(context, TYPE_PART)) {
		
		physicalId = (String)partSpecMap.get("physicalid");
		dType=(String)partSpecMap.get("type");	
		
	}


        for(int i=0; i < imageInfoList.size(); i++) {
            ImageManagerImageInfo imageInfo = (ImageManagerImageInfo) imageInfoList.get(i);
%>          
			// XSSOK
            backImage["<%=i%>"] = new ImageInfo("<%=i%>", "<%=imageInfo.getImageName()%>" , "<%=imageInfo.getImageURL()%>", "<%=imageInfo.getFileExtension()%>", "<%=physicalId%>", "<%=dType%>", "<%=serverUrl%>");
<%          
        }
%>
    </framework:ifExpr>
</script>

<!-- XSSOK -->
<title><%=XSSUtil.encodeForHTML(context, header)%></title>
</head>

<!-- XSSOK -->
<body class="<%=strBodyClass%>" onload="showImage(<%=primaryImageIndex%>);adjustImageStyles();">
  <div id="pageHeadDiv">
        <table>
            <tr>
                <td class="page-title">
		    <!-- XSSOK -->
                    <h2><%=XSSUtil.encodeForHTML(context, header)%></h2>
                </td>
            </tr>
        </table>    
        <jsp:include page = "../common/emxToolbar.jsp" flush="true">
        <jsp:param name="toolbar" value="<%=toolbar%>"/>
        <jsp:param name="objectId" value="<%=objectId%>"/>
        <jsp:param name="parentOID" value="<%=objectId%>"/>
        <jsp:param name="suiteKey" value="<%=suiteKey%>"/>
        <jsp:param name="PrinterFriendly" value="false"/>
        <jsp:param name="export" value="false"/>
        <jsp:param name="helpMarker" value="<%=helpMarker%>"/>
        <jsp:param name="isImageManager" value="true"/>
        </jsp:include>
  </div> <!-- #pageHeadDiv -->

  <div id="divPageBody" class="image-viewer">
  <iframe class="hidden-frame" name="listDisplay" src="../common/emxBlank.jsp" HEIGHT="0" WIDTH="0"></iframe> 
    <form name="ImageViewerForm" method="post" >
        <framework:ifExpr expr="<%=!hasImages%>">
            <table>
                <tr>
                    <td align="center" colspan="13" style="height: 40px;font-size:12px;font-weight:bold;font-style:italic;color:#000;background-color:transparent;border:none;">
                        <emxUtil:i18n localize="i18nId">emxComponents.Image.NoImageAssociated</emxUtil:i18n>
                    </td>
                </tr>           
            </table>
        </framework:ifExpr>
        <framework:ifExpr expr="<%=hasImages%>">
            <div id="divThumbnails">
                <table id="thubmbnailsTable">
<%
            for(int i=0 ; i < imageInfoList.size(); i++ ) {
                com.matrixone.apps.common.util.ImageManagerImageInfo imageInfo = 
                       (com.matrixone.apps.common.util.ImageManagerImageInfo)imageInfoList.get(i);
                    
                String fileName = imageInfo.getImageName();
                String icon3dXML = imageInfo.get3dImageIcon();
                String primaryImageIcon = (i == primaryImageIndex) ?
                                          com.matrixone.apps.common.util.ImageManagerUtil.getPrimaryImageIcon(strLanguage) : "";
                String imageThumnailURL = imageInfo.getThumbnailURL(); 
                String checkBoxVal = showCheckbox ?  imageInfo.getCheckBoxValue() : null;
%>
                    <tr>
                        <framework:ifExpr expr="<%=showCheckbox%>">
                            <td>
                                <table>
                                    <tr>
                                        <td>
                                        	<!-- XSSOK -->
                                            <table><tr><td><%=icon3dXML%></td><td><%=primaryImageIcon%></td></tr></table>
                                        </td>
                                        <td>
                                        	<!-- XSSOK -->
                                            <input type="checkbox" name="emxTableRowId" value="<%=checkBoxVal%>" onclick="checkToolbarStatus(this)" />
                                        </td>
                                    </tr>
                                </table>
                             </td>
                        </framework:ifExpr>
                            <td class="thumbnail" id="tdthumbnail">
                                <a href="javaScript:showImage(<%=i%>)">
                                	<!-- XSSOK -->
                                	<%if(!("../common/images/brokenimage.png".equals(imageThumnailURL))) { %>
                                    <img height="<%=imageHeight%>" src="<%=imageThumnailURL%>" id="img<%=i%>" alt="<%=fileName%>" title="<%=fileName%>" />
                                    <%} else { %>
                                    <img height="<%=imageHeight%>" src="<%=imageThumnailURL%>" id="img<%=i%>" />
                                    <%} %>
                                </a>
                                <p><%=fileName%></p>
                            </td>
                    </tr>
<% 
            }
%>                  
                </table>
            </div> <!-- #divThumbnails -->                      

        </framework:ifExpr>
        <iframe class="hidden-frame" name="imageHidden" src="../common/emxBlank.jsp" HEIGHT="0" WIDTH="0"></iframe> 
    </form> 
    <div id="divImage" name="divImage" style="overflow:hidden;right:180px"></div>  
   </div><!-- #divPageBody -->
   
   <!-- Footer Section Start    -->    
    <div id="divPageFoot">   
        <table>
            <tr>  
                <td class="functions">
                    <table>
                        <tr>
                        	<!-- XSSOK -->
                            <td><a href="javascript:chgImg(-1)" class="<%= prevButtonClass %>"><span></span></a></td>
                            <td><a href="javaScript:chgImg(1)" class="<%= nextButtonClass %>"><span></span></a></td>
                        </tr>
                    </table>
                </td>
                <framework:ifExpr expr="<%=isPopUpWin%>">
                    <td class="buttons" id="tdButtons">
                        <table>
                            <tr>
                                <td>
                                    <div id="CloseImage">
                                    	<a class="action-close" href="javascript:getTopWindow().closeWindow()"><button type="button" class="btn-primary"><emxUtil:i18n localize="i18nId">emxComponents.Button.Close</emxUtil:i18n></button></a>
                                    </div>
                                </td>
                            </tr>
                        </table>
                    </td>
                </framework:ifExpr>
            </tr>
        </table>                            
    </div><!-- /#divPageFoot -->    
</body>
</html>
