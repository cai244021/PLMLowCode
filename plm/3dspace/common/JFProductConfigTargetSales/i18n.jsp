<%@ page contentType="application/json; charset=UTF-8" pageEncoding="UTF-8" import="java.util.Locale,java.util.MissingResourceException,java.util.ResourceBundle" %>
<%!
    private static final String BUNDLE_NAME = "emxFrameworkStringResource";

    private static final String[] KEYS = {
            "emxFramework.JFProductConfigTargetSales.Title",
            "emxFramework.JFProductConfigTargetSales.AnnualTargetSales",
            "emxFramework.JFProductConfigTargetSales.ConfigurationRatio",
            "emxFramework.JFProductConfigTargetSales.Summary",
            "emxFramework.JFProductConfigTargetSales.BasicInfo",
            "emxFramework.Attribute.JFProjectLevelAPRDiscount",
            "emxFramework.JFProductConfigTargetSales.Save",
            "emxFramework.JFProductConfigTargetSales.AddYear",
            "emxFramework.JFProductConfigTargetSales.Delete",
            "emxFramework.JFProductConfigTargetSales.CalculateSummary",
            "emxFramework.JFProductConfigTargetSales.ExportModify",
            "emxFramework.JFProductConfigTargetSales.Import",
            "emxFramework.JFProductConfigTargetSales.ImportFailed",
            "emxFramework.JFProductConfigTargetSales.Submit",
            "emxFramework.JFProductConfigTargetSales.SubmitSuccess",
            "emxFramework.JFProductConfigTargetSales.ManagerTask",
            "emxFramework.JFProductConfigTargetSales.Sequence",
            "emxFramework.JFProductConfigTargetSales.TaskName",
            "emxFramework.JFProductConfigTargetSales.Status",
            "emxFramework.JFProductConfigTargetSales.CreateTime",
            "emxFramework.JFProductConfigTargetSales.CompleteTime",
            "emxFramework.JFProductConfigTargetSales.Owner",
            "emxFramework.JFProductConfigTargetSales.NoManagerTasks",
            "emxFramework.JFProductConfigTargetSales.ApprovalDialogTitle",
            "emxFramework.JFProductConfigTargetSales.ApprovalComment",
            "emxFramework.JFProductConfigTargetSales.ApprovalCommentPlaceholder",
            "emxFramework.JFProductConfigTargetSales.ApprovalCommentRequired",
            "emxFramework.JFProductConfigTargetSales.Agree",
            "emxFramework.JFProductConfigTargetSales.CalculateModePrompt",
            "emxFramework.JFProductConfigTargetSales.Overwrite",
            "emxFramework.JFProductConfigTargetSales.FillBlankOnly",
            "emxFramework.JFProductConfigTargetSales.Confirm",
            "emxFramework.JFProductConfigTargetSales.Cancel",
            "emxFramework.JFProductConfigTargetSales.Year",
            "emxFramework.JFProductConfigTargetSales.Overseas",
            "emxFramework.JFProductConfigTargetSales.Domestic",
            "emxFramework.JFProductConfigTargetSales.Other",
            "emxFramework.JFProductConfigTargetSales.OverseasPercent",
            "emxFramework.JFProductConfigTargetSales.DomesticPercent",
            "emxFramework.JFProductConfigTargetSales.OtherPercent",
            "emxFramework.JFProductConfigTargetSales.Configuration",
            "emxFramework.JFProductConfigTargetSales.PartNumber",
            "emxFramework.JFProductConfigTargetSales.PartName",
            "emxFramework.JFProductConfigTargetSales.CustomerPartNumber",
            "emxFramework.Attribute.JFCustomerPartName",
            "emxFramework.JFProductConfigTargetSales.PartInfo",
            "emxFramework.JFProductConfigTargetSales.Ratio",
            "emxFramework.JFProductConfigTargetSales.NoYears",
            "emxFramework.JFProductConfigTargetSales.NoVehicleConfigs",
            "emxFramework.JFProductConfigTargetSales.NoParts",
            "emxFramework.JFProductConfigParts.ObjectCountUnit",
            "emxFramework.JFProductConfigTargetSales.ProductConfigTableIdEmpty",
            "emxFramework.JFProductConfigTargetSales.YearRequired",
            "emxFramework.JFProductConfigTargetSales.YearInvalid",
            "emxFramework.JFProductConfigTargetSales.YearExists",
            "emxFramework.JFProductConfigTargetSales.SelectYearFirst",
            "emxFramework.JFProductConfigTargetSales.RatioRangeInvalid",
            "emxFramework.JFProductConfigTargetSales.Processing",
            "emxFramework.JFProductConfigTargetSales.LoadFailed",
            "emxFramework.JFProductConfigTargetSales.SaveSuccess",
            "emxFramework.JFProductConfigTargetSales.CreateFailed",
            "emxFramework.JFProductConfigParts.QuantityNumberOnly"
    };

    private static String getText(ResourceBundle bundle, String key) {
        try {
            return bundle.getString(key);
        } catch (MissingResourceException e) {
            return key;
        }
    }

    private static String escapeJson(String value) {
        if (value == null) {
            return "";
        }
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < value.length(); i++) {
            char ch = value.charAt(i);
            if (ch == '"') {
                result.append("\\\"");
            } else if (ch == '\\') {
                result.append("\\\\");
            } else if (ch == '\b') {
                result.append("\\b");
            } else if (ch == '\f') {
                result.append("\\f");
            } else if (ch == '\n') {
                result.append("\\n");
            } else if (ch == '\r') {
                result.append("\\r");
            } else if (ch == '\t') {
                result.append("\\t");
            } else {
                result.append(ch);
            }
        }
        return result.toString();
    }
%>
<%
    response.setHeader("Cache-Control", "no-cache");
    ResourceBundle bundle = ResourceBundle.getBundle(BUNDLE_NAME, request.getLocale());
%>
{
<%
    for (int i = 0; i < KEYS.length; i++) {
        String key = KEYS[i];
        String value = getText(bundle, key);
%>  "<%=key%>": "<%=escapeJson(value)%>"<%=i + 1 < KEYS.length ? "," : ""%>
<%
    }
%>}
