<%@include file = "./emxNavigatorInclude.inc"%>
<%
    String tab = emxGetParameter(request, "tab");
    if (tab == null) {
        tab = "";
    }
%>
<!DOCTYPE html>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <title><%=tab%></title>
    <style>
        body { margin: 0; font-family: Arial, "Microsoft YaHei", sans-serif; color: #333; }
        .placeholder { padding: 24px; font-size: 14px; }
    </style>
</head>
<body>
    <div class="placeholder">This tab is not implemented yet.</div>
</body>
</html>
