<%@ page import="com.matrixone.apps.domain.DomainConstants" %>
<%@ page import="com.matrixone.apps.domain.DomainObject" %>
<%@ page import="com.matrixone.apps.domain.util.EnoviaResourceBundle" %>
<%@ page import="com.matrixone.apps.domain.util.FrameworkUtil" %>
<%@ page import="com.matrixone.apps.domain.util.MqlUtil" %>
<%@ page import="com.matrixone.apps.domain.util.PersonUtil" %>
<%@ page import="com.matrixone.apps.framework.ui.UIUtil" %>
<%@ page import="com.matrixone.apps.domain.util.XSSUtil" %>
<%@ page import="matrix.db.JPO" %>
<%@ page import="matrix.db.BusinessInterface" %>
<%@ page import="matrix.db.BusinessInterfaceList" %>
<%@ page import="matrix.util.StringList" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.util.Vector" %>
<%@ page import="com.matrixone.apps.program.ProgramCentralUtil" %>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="utf-8" %>
<%@include file="../emxUICommonAppInclude.inc"%>
<%@include file="../common/enoviaCSRFTokenValidation.inc"%>
<script src="../common/scripts/emxUICore.js" type="text/javascript"></script>
<%
    String alertMess = DomainConstants.EMPTY_STRING;
    String suiteKey = "emxComponentsStringResource";
    String frameworkSuiteKey = "emxFrameworkStringResource";
    try {
        String objectId = emxGetParameter(request, "objectId");
        String[] tableRowIdList = emxGetParameterValues(request, "emxTableRowId");
        StringList customerPartIds = new StringList();
        boolean hasNoRemoveAccess = false;
        DomainObject partObject = DomainObject.newInstance(context, objectId);
        StringList partSelectList = new StringList(DomainConstants.SELECT_CURRENT);
        partSelectList.add(DomainConstants.SELECT_OWNER);
        Map partInfoMap = partObject.getInfo(context, partSelectList);
        String partCurrent = UIUtil.getValue(partInfoMap, DomainConstants.SELECT_CURRENT);
        String partOwner = UIUtil.getValue(partInfoMap, DomainConstants.SELECT_OWNER);
        boolean isInWork = "IN_WORK".equalsIgnoreCase(partCurrent);
        boolean isFrozenOrReleased = "FROZEN".equalsIgnoreCase(partCurrent) || "RELEASED".equalsIgnoreCase(partCurrent);
        boolean isPartOwner = context.getUser().equals(partOwner);
        Vector assignments = PersonUtil.getAssignments(context, context.getUser());
        boolean isLibAdmin = assignments.contains("jfLibAdmin");
        boolean isInReleaseProcess = false;
        BusinessInterfaceList businessInterfaces = partObject.getBusinessInterfaces(context);
        for (int i = 0; i < businessInterfaces.size(); i++) {
            BusinessInterface businessInterface = businessInterfaces.get(i);
            if ("Change Control".equals(businessInterface.getName())) {
                isInReleaseProcess = true;
                break;
            }
        }
        String belongProjectId = JPO.invoke(context,
                "JF_VPMReferenceEBOM",
                null,
                "getPartBelongProject",
                new String[]{objectId, DomainConstants.SELECT_ID},
                String.class);
        if (tableRowIdList != null) {
            for (String tableRowId : tableRowIdList) {
                Map rowMap = ProgramCentralUtil.parseTableRowId(context,tableRowId);
                String customerPartId = (String) rowMap.get("objectId");
                if (UIUtil.isNotNullAndNotEmpty(customerPartId) && !customerPartIds.contains(customerPartId)) {
                    String relId = (String) rowMap.get("relId");
                    String rowCustomerPartId = DomainConstants.EMPTY_STRING;
                    String rowPartId = DomainConstants.EMPTY_STRING;
                    String rowProjectIds = DomainConstants.EMPTY_STRING;
                    if (UIUtil.isNotNullAndNotEmpty(relId)) {
                        String rowConnectionInfo = MqlUtil.mqlCommand(context,
                                "print connection $1 select $2 $3 $4 dump $5",
                                relId,
                                "from.id",
                                "to.id",
                                "frommid[JFVPMReference2CustomerParts2Project].to.id",
                                "|");
                        StringList rowConnectionInfoList = FrameworkUtil.split(rowConnectionInfo, "|");
                        if (rowConnectionInfoList.size() >= 3) {
                            rowCustomerPartId = (String) rowConnectionInfoList.get(0);
                            rowPartId = (String) rowConnectionInfoList.get(1);
                            rowProjectIds = (String) rowConnectionInfoList.get(2);
                        }
                    }
                    boolean isSelectedRowRelationship = customerPartId.equals(rowCustomerPartId)
                            && objectId.equals(rowPartId);
                    boolean hasRowProject = UIUtil.isNotNullAndNotEmpty(rowProjectIds);
                    boolean isBelongProjectData = UIUtil.isNotNullAndNotEmpty(belongProjectId)
                            && hasRowProject
                            && FrameworkUtil.split(rowProjectIds, "|").contains(belongProjectId);
                    //20260817 update by Codex 工作中按零件Owner授权；流程中Owner及冻结/发布后的库管理员只能移除共用项目行。
                    boolean hasRemoveAccess = isSelectedRowRelationship
                            && hasRowProject
                            && ((isInWork
                            && isPartOwner
                            && (!isInReleaseProcess || !isBelongProjectData))
                            || (isFrozenOrReleased
                            && isLibAdmin
                            && !isBelongProjectData));
                    if (!hasRemoveAccess) {
                        hasNoRemoveAccess = true;
                        break;
                    }
                    customerPartIds.add(customerPartId.trim());
                }
            }
        }
        if (hasNoRemoveAccess) {
            alertMess = EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", context.getLocale(), "emxComponents.JFCustomerPartsDB.NoModifyAccess");
        } else {
            Map requestMap = new HashMap();
            requestMap.put("objectId", objectId);
            requestMap.put("customerPartIds", customerPartIds);
            JPO.invoke(context, "JF_VPMReferenceEBOM", new String[0], "removeCustomerPartsDB", JPO.packArgs(requestMap));
            alertMess = EnoviaResourceBundle.getProperty(context, suiteKey, context.getLocale(), "emxComponents.Remove.Successful");
        }
    } catch (Exception e) {
        e.printStackTrace();
        alertMess = EnoviaResourceBundle.getProperty(context, suiteKey, context.getLocale(), "emxComponents.action.error");
    }
%>
<html>
<script>
    alert("<%=XSSUtil.encodeForJavaScript(context, alertMess)%>");
    parent.getTopWindow().findFrame(parent.getTopWindow(), "detailsDisplay").location.href = parent.getTopWindow().findFrame(parent.getTopWindow(), "detailsDisplay").location.href.replace("persist=true", "persist=false");
</script>
</html>
