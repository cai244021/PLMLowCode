<%@ page pageEncoding="UTF-8" %>
<%@ page import="matrix.db.JPO" %>
<%@ page import="com.matrixone.apps.domain.util.XSSUtil" %>
<%@include file="./emxNavigatorInclude.inc"%>
<%
    response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
    response.setHeader("Pragma", "no-cache");
    response.setDateHeader("Expires", 0);

    try {
        // 创建命令不显示表单，后台创建专家组后直接进入新对象Tree页面。
        String expertGroupId = JPO.invoke(
                context,
                "JF_ExpertsGroupPeopleService",
                null,
                "createTechDocExpertGroup",
                JPO.packArgs(new java.util.HashMap()),
                String.class);
        response.sendRedirect("emxTree.jsp?objectId=" + XSSUtil.encodeForURL(context, expertGroupId));
        return;
    } catch (Exception e) {
        e.getMessage();
    }
%>