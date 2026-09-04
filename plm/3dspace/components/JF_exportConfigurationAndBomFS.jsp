<%-- Idm_uploadInspectionPartCheckinDialogFS.jsp - used for Checkin of file into Document Object
   Copyright (c) 1992-2020 Dassault Systemes.
   All Rights Reserved.
   This program contains proprietary and trade secret information of MatrixOne,
   Inc.  Copyright notice is precautionary only
   and does not evidence any actual or intended publication of such program

   emxCommonDocumentMultiFileUploadFS.jsp
   static const char RCSID[] = "$Id: emxCommonDocumentCheckinDialogFS.jsp.rca 1.21 Wed Oct 22 16:18:21 2008 przemek Experimental przemek $"
--%>
<%@ page import="com.matrixone.apps.domain.util.EnoviaResourceBundle" %>
<%@ page import="com.matrixone.apps.common.util.ComponentsUtil"%>
<%@ page import="org.slf4j.LoggerFactory" %>
<%@ page import="org.slf4j.Logger" %>
<%@ page pageEncoding="utf-8" %>
<%@ include file = "../emxUICommonAppInclude.inc"%>
<%@ include file = "../emxUICommonHeaderBeginInclude.inc" %>
<%@include file = "../common/emxUIConstantsInclude.inc"%>
<script type="text/javascript" src="../common/scripts/emxUICoreMenu.js"></script>
<script language="javascript" type="text/javascript" src="../common/scripts/emxUICore.js"></script>
<script language="javascript" type="text/javascript" src="../common/scripts/emxUIModal.js"></script>
<script language="javascript" type="text/javascript" src="../common/scripts/emxUIFormHandler.js"></script>
<%
    String header = (String)emxGetParameter(request, "header");
    String content = (String)emxGetParameter(request, "content");
%>
<body>
<style>
    #divPageBody {
        position:absolute;
        top:79px;
        right:0;
        bottom:25px;
        left:0;
        padding:0;
        overflow:auto;
        background:#fff;
    }

    body.editable table.list tr th {
        min-height:26px;
        padding:10px 5px 10px 5px;
        background: #f5f6f7; /* Old browsers */
        background: -moz-linear-gradient(top, #f5f6f7 0%, #e2e4e3 100%); /* FF3.6+ */
        background: -webkit-gradient(linear, left top, left bottom, color-stop(0%,#f5f6f7), color-stop(100%,#e2e4e3)); /* Chrome,Safari4+ */
        background: -webkit-linear-gradient(top, #f5f6f7 0%,#e2e4e3 100%); /* Chrome10+,Safari5.1+ */
        background: -o-linear-gradient(top, #f5f6f7 0%,#e2e4e3 100%); /* Opera 11.10+ */
        background: -ms-linear-gradient(top, #f5f6f7 0%,#e2e4e3 100%); /* IE10+ */
        background: linear-gradient(to bottom, #f5f6f7 0%,#e2e4e3 100%); /* W3C */
        filter: progid:DXImageTransform.Microsoft.gradient( startColorstr='#f5f6f7', endColorstr='#e2e4e3',GradientType=0 ); /* IE6-9 */
        font-weight:bold;
        border-top:1px solid #288fd1;
        border-bottom:1px solid #288fd1;
    }
</style>
<div id="pageHeadDiv">
    <form>
        <table>
            <tbody>
            <tr>
                <td class="page-title"><h2 id="ph"><%=header%></h2></td>
                <td class="functions">
                    <table>
                        <tbody>
                        <tr>
                            <td class="progress-indicator">
                                <div id="imgProgressDiv" style="visibility: hidden">
                                </div>
                            </td>
                        </tr>
                        </tbody>
                    </table>
                </td>
            </tr>
            </tbody>
        </table>
    </form>
</div>

<div id='divPageBody' style="font-size: 20px;text-align: center;">
        <h3><%=content%></h3>
</div>
</body>
