<%@ page contentType="application/json; charset=UTF-8" pageEncoding="UTF-8" import="java.util.Locale,java.util.MissingResourceException,java.util.ResourceBundle" %>
<%!
    private static final String BUNDLE_NAME = "emxFrameworkStringResource";

    private static final String[] KEYS = {
            "emxFramework.JFProductConfigParts.Title",
            "emxFramework.JFProductConfigParts.CreatePositionProduct",
            "emxFramework.JFProductConfigParts.DeletePositionProduct",
            "emxFramework.JFProductConfigParts.CreateVehicleConfig",
            "emxFramework.JFProductConfigParts.DeleteVehicleConfig",
            "emxFramework.JFProductConfigParts.AddParts",
            "emxFramework.JFProductConfigParts.RemoveParts",
            "emxFramework.JFProductConfigParts.ProductConfigTableIdEmpty",
            "emxFramework.JFProductConfigParts.NoPositionProducts",
            "emxFramework.JFProductConfigParts.SeatPosition",
            "emxFramework.JFProductConfigParts.VehicleConfig1",
            "emxFramework.JFProductConfigParts.VehicleConfig2",
            "emxFramework.JFProductConfigParts.VehicleConfig3",
            "emxFramework.JFProductConfigParts.VehicleConfig4",
            "emxFramework.JFProductConfigParts.VehicleConfigName",
            "emxFramework.JFProductConfigParts.VehicleConfigInfo",
            "emxFramework.JFProductConfigParts.CopyFromVehicleConfig",
            "emxFramework.JFProductConfigParts.NoCopy",
            "emxFramework.JFProductConfigParts.VehicleConfigNameRequired",
            "emxFramework.JFProductConfigParts.VehicleConfigNameExists",
            "emxFramework.JFProductConfigParts.SelectVehicleConfigFirst",
            "emxFramework.JFProductConfigParts.DeleteVehicleConfigConfirm",
            "emxFramework.JFProductConfigParts.DeletePositionProductConfirm",
            "emxFramework.JFProductConfigParts.Quantity",
            "emxFramework.JFProductConfigParts.OptionalOrNot",
            "emxFramework.JFProductConfigParts.PartNumber",
            "emxFramework.JFProductConfigParts.PartCNName",
            "emxFramework.JFProductConfigParts.CustomerPartNumber",
            "emxFramework.Attribute.JFCustomerPartName",
            "emxFramework.JFProductConfigParts.AssemblyLevel",
            "emxFramework.JFProductConfigParts.Domestic1Brazil",
            "emxFramework.JFProductConfigParts.Domestic2Uzbekistan",
            "emxFramework.JFProductConfigParts.NoParts",
            "emxFramework.JFProductConfigParts.RequiredNote",
            "emxFramework.JFProductConfigParts.PositionProductName",
            "emxFramework.JFProductConfigParts.Description",
            "emxFramework.JFProductConfigParts.OK",
            "emxFramework.JFProductConfigParts.Apply",
            "emxFramework.JFProductConfigParts.Cancel",
            "emxFramework.JFProductConfigParts.Processing",
            "emxFramework.JFProductConfigParts.LoadFailed",
            "emxFramework.JFProductConfigParts.CreateFailed",
            "emxFramework.JFProductConfigParts.PositionProductNameRequired",
            "emxFramework.JFProductConfigParts.SelectPositionProductFirst",
            "emxFramework.JFProductConfigParts.SelectPartsFirst",
            "emxFramework.JFProductConfigParts.Type",
            "emxFramework.JFProductConfigParts.PositionProductNameExists",
            "emxFramework.JFProductConfigParts.PositionProductNameNotEditable",
            "emxFramework.JFProductConfigParts.ObjectCountUnit",
            "emxFramework.JFProductConfigParts.BatchUpdate",
            "emxFramework.JFProductConfigParts.BatchField",
            "emxFramework.JFProductConfigParts.BatchValue",
            "emxFramework.JFProductConfigParts.DragSort",
            "emxFramework.JFProductConfigParts.Import",
            "emxFramework.JFProductConfigParts.Export",
            "emxFramework.JFProductConfigParts.ImportFailed",
            "emxFramework.JFProductConfigParts.DownloadErrorExcel",
            "emxFramework.JFProductConfigParts.NoEditAccess"
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
