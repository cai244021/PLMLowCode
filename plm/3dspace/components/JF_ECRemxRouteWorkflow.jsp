 <%--   emxRouteWorkflow.jsp  -  Display the Workflow view of all the Tasks in a Route
  Copyright (c) 1992-2020 Dassault Systemes.All Rights Reserved.
  This program contains proprietary and trade secret information of MatrixOne,Inc.
  Copyright notice is precautionary only and does not evidence any actual or intended publication of such program
--%>
<%@page import="java.util.HashMap"%>
<%@page import="com.matrixone.servlet.Framework"%>
<%@page import="matrix.db.Context"%>
<%@page import="com.matrixone.apps.common.RouteWorkflow"%>
 <%@ page import="org.slf4j.Logger" %>
 <%@ page import="org.slf4j.LoggerFactory" %>
 <%@ page import="java.util.Map" %>
 <%@ page import="matrix.db.JPO" %>
 <%@include file = "../emxContentTypeInclude.inc"%>
 <%!
	 private static final Logger JF_LOGGER = LoggerFactory.getLogger("JF_ECRemxRouteWorkflow.jsp");
 %>
<% 	
	Context context = Framework.getFrameContext(session);
    String timeZone = (String)session.getAttribute("timeZone");
	String sOID = com.matrixone.apps.domain.util.Request.getParameter(request, "objectId");
	String parentId = com.matrixone.apps.domain.util.Request.getParameter(request, "parentId");

	boolean isPrinterFriendly = false;
	String printerFriendly = com.matrixone.apps.domain.util.Request.getParameter(request, "PrinterFriendly");
	if (printerFriendly != null && !"null".equals(printerFriendly) && !"".equals(printerFriendly)) {
		isPrinterFriendly = "true".equals(printerFriendly);
		}
	  
//	RouteWorkflow rwObject = new RouteWorkflow(sOID);
//	HashMap<String, StringBuffer> nodeMap = rwObject.getRouteTaskNodes(context, isPrinterFriendly, timeZone);

	//add by caipan
	Map<String,StringBuffer> nodeMap = (Map) JPO.invoke(context, "JF_ECRProcess", null, "showTasksGraphical", new String[]{parentId},Map.class);
	StringBuffer sbTasks 		= nodeMap.get("sbTasks");
	StringBuffer sbConnections 	= nodeMap.get("sbConnections");


	/*StringBuffer sbConnections = new StringBuffer();

	sbConnections.append("jsPlumb.connect({ source:'start', target:'task0' });");
//	sbConnections.append("jsPlumb.connect({ source:'task0', target:'node2' });");
//	sbConnections.append("jsPlumb.connect({ source:'node2', target:'task2',hasSplitTask:false });");
	sbConnections.append("jsPlumb.connect({ source:'task0', target:'task2' });");
	sbConnections.append("jsPlumb.connect({ source:'task0', target:'task1'});");
//	sbConnections.append("jsPlumb.connect({ source:'node2', target:'task1',hasSplitTask:false });");

	sbConnections.append("jsPlumb.connect({ source:'task1', target:'task3' });");
	sbConnections.append("jsPlumb.connect({ source:'task2', target:'task3' });");
	sbConnections.append("jsPlumb.connect({ source:'task3', target:'end' });");

	StringBuffer sbTasks = new StringBuffer();
	sbTasks.append("<div class='start completed' id=\"start\" style='top:75px'></div>");
	sbTasks.append("<div class='end' id=\"end\" style='top:75px;left:940px;'></div>");
//	sbTasks.append("<div class=\"node\" id=\"node2\" style=\"top:90px; left:370px;\"><label>全部</label></div>");
	sbTasks.append("<div class=\"task active my-task\" id=\"task0\" style=\"top:60px; left:150px;\"><span class=\"action\">批准</span><label class=\"object\" title=\"1\" onClick=\"showModalDialog('../common/emxTree.jsp?objectId=35845.4994.8309.61844')\">1</label><span class=\"assignee\" title=\"Admin Platform\"onClick=\"showModalDialog('../common/emxTree.jsp?objectId=35845.4994.14586.18580')\">Admin Platform</span><span class=\"due-date\">2025年3月6日</span></div>");
	sbTasks.append("<div class=\"task completed approved\" id=\"task1\" style=\"top:0px; left:500px;\"><span class=\"action\">批准</span><label class=\"object\" title=\"2\" onClick=\"showModalDialog('../common/emxForm.jsp?form=APPRouteNodeTask&toolbar=APPRoleNodeTaskActionsToolBar&relId=73A18B56D888210067C6AC18000030A6&objectId=35845.4994.8309.61828')\">2</label><span class=\"assignee\" title=\"&#x6574;&#x6905; &#x7ecf;&#x7406;\"onClick=\"showModalDialog('../common/emxTree.jsp?objectId=35845.4994.31611.26536')\">&#x6574;&#x6905; &#x7ecf;&#x7406;</span><span class=\"due-date\">2025年3月6日</span></div>");
	sbTasks.append("<div class=\"task\" id=\"task2\" style=\"top:120px; left:500px;\"><span class=\"action\">批准</span><label class=\"object\" title=\"2\" onClick=\"showModalDialog('../common/emxForm.jsp?form=APPRouteNodeTask&toolbar=APPRoleNodeTaskActionsToolBar&relId=73A18B56D888210067C6AC18000030B0&objectId=35845.4994.8309.61828')\">2</label><span class=\"assignee\" title=\"&#x7269;&#x6d41; &#x4ee3;&#x8868;\"onClick=\"showModalDialog('../common/emxTree.jsp?objectId=35845.4994.31611.26843')\">&#x7269;&#x6d41; &#x4ee3;&#x8868;</span><span class=\"due-date\">2025年3月7日</span></div>");
	sbTasks.append("<div class=\"task\" id=\"task3\" style=\"top:60px; left:720px;\"><span class=\"action\">批准</span><label class=\"object\" title=\"3\" onClick=\"showModalDialog('../common/emxForm.jsp?form=APPRouteNodeTask&toolbar=APPRoleNodeTaskActionsToolBar&relId=73A18B56D888210067C6AC1800003092&objectId=35845.4994.8309.61828')\">3</label><span class=\"assignee\" title=\"Mike Wang\"onClick=\"showModalDialog('../common/emxTree.jsp?objectId=35845.4994.64313.1912')\">Mike Wang</span><span class=\"due-date\">2025年3月7日</span></div>");
*/
//task completed approved  完成
//task active my-task 活动中的任务
//task 还没有到的任务
//如果不是并排任务的话 top 从0px开始，不是的话 每一个增加120PX
// 第一个 Left 150px 后面 200PX一个
%>

<!DOCTYPE html>
<html>
	<head>
		<script type="text/javascript" src="../common/scripts/jquery.min-xparam.js"></script>
		<script type="text/javascript" src="../common/scripts/jquery-ui.min-xparam.js"></script>
		<script type="text/javascript" src="../plugins/jsPlumb/1.4.1/jquery.jsPlumb-xparam.js"></script>
		<script type="text/javascript" src="../common/scripts/emxUICore.js"></script>
		<script type="text/javascript" src="../common/scripts/emxUIModal.js"></script>
			
        <link rel="stylesheet" type="text/css" href="../common/styles/emxUIDefault.css"/>
        		
		<script type="text/javascript">
			jsPlumb.bind("ready", function() {			
				jsPlumb.importDefaults({
					Anchors  : ["RightMiddle", "LeftMiddle"],
					EndpointStyles : [{ fillStyle:'#55636b' }, { fillStyle:'#55636b' }],
					Endpoints : [ [ "Rectangle", {width:1, height:1} ], [ "Rectangle", { width:1, height:1 } ]],
					ConnectionOverlays	: [
						[ "Arrow", { location: -2 , width: 15, length: 15 } ]
					],
					Connector:[ "Flowchart", { stub: 10, gap:10 } ],								
					PaintStyle 	: {
						lineWidth:2,
						strokeStyle:"#55636b",
						joinstyle:"round"
					}
				});

			//XSSOK
			<%=sbConnections.toString()%>		
			});
		</script>
	</head>
	<body class="route-graphical">
	<div class="route-container">
				<%=sbTasks.toString()%>
				</div>
	</body>
</html>
