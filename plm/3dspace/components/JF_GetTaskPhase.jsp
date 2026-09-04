<%@ page language="java" contentType="text/html; charset=utf-8"%>
<%@page import="com.matrixone.json.JSONArray"%>
<%@page import="com.matrixone.servlet.Framework"%>
<%@page import="java.io.PrintWriter"%>
<%@page import="matrix.db.Context"%>
<%@page import="matrix.db.JPO"%>
<%@page import="java.util.List"%>
<%@page import="java.util.ArrayList"%>
<%@ page import="matrix.util.StringList" %>
<%@ page import="java.util.Map" %>
<%@ page import="com.matrixone.apps.framework.ui.UIUtil" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="com.matrixone.apps.domain.DomainObject" %>
<%@ page import="com.matrixone.apps.domain.util.MapList" %>
<%
    PrintWriter print = null;
    try {
        String objectId= request.getParameter("objectId");
        String type= request.getParameter("type");
        String connDepartmentId= request.getParameter("connDepartmentId");
        String getPhase = "";
        Context cxt = Framework.getContext(request);
        System.out.println("type:" + type);
        if ("phase".equalsIgnoreCase(type)) {
            getPhase = JPO.invoke(cxt, "JF_PublicProjectQuery", null, "getTaskPhase", new String[]{objectId, type}, String.class);
            System.out.println("*****************object:" + getPhase);
        } else if ("folder".equalsIgnoreCase(type)) {
            getPhase = JPO.invoke(cxt, "JF_PublicProjectQuery", null, "getWorkspaceVault", new String[]{objectId, connDepartmentId}, String.class);
            System.out.println("*****************object:" + getPhase);
        }
        JSONArray json = new JSONArray(getPhase);
        print = response.getWriter();
        print.write(json.toString());
        print.flush();
    }catch (Exception e) {
        e.printStackTrace();
    }

%>