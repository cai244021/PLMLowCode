<%@ page import="com.matrixone.apps.domain.DomainObject" %>
<%@ page import="com.matrixone.apps.domain.DomainRelationship" %>
<%@ page import="com.matrixone.apps.domain.util.ContextUtil" %>
<%@ page import="com.matrixone.apps.domain.util.EnoviaResourceBundle" %>
<%@ page import="com.matrixone.apps.framework.ui.UIUtil" %>
<%@ page import="matrix.util.StringList" %>
<%@ page import="com.matrixone.apps.domain.util.PropertyUtil" %>
<%@ page import="com.goterl.lazysodium.interfaces.Hash" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="matrix.db.JPO" %>
<%@ page import="com.nomagic.esi.emf.a.B" %>
<%@ page import="com.matrixone.apps.domain.DomainConstants" %>
<%@ page import="com.matrixone.apps.domain.util.MapList" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.util.stream.Collectors" %>
<%@ page import="org.slf4j.Logger" %>
<%@ page import="org.slf4j.LoggerFactory" %>
<%@ include file="../programcentral/emxProgramGlobals2.inc" %>
<%@include file = "../emxUICommonAppInclude.inc"%>
<%@include file = "../common/enoviaCSRFTokenValidation.inc"%>

<script src="../common/scripts/emxUICore.js" type="text/javascript"></script>
<%!
    private  static final Logger logger = LoggerFactory.getLogger("JF_RemoveAndDeleteObject.jsp");
%>
<%
    String alertMess = DomainConstants.EMPTY_STRING;
    String strFlushTableName = DomainConstants.EMPTY_STRING;
    String suiteKey = "emxComponentsStringResource";
    try {
        logger.info("@@@@@@@@@@@@@@@@@");
        String[] strSelectIds = emxGetParameterValues(request, "emxTableRowId");
        String objectId = (String) emxGetParameter(request, "objectId");
        logger.info("@@@@@@@@@@@@@@@@@objectId:{}", objectId);
        logger.info("@@@@@@@@@@@@@@@@@strSelectIds:{}", strSelectIds.length);
        String mode = (String) emxGetParameter(request, "mode");
        String strLanguage = request.getHeader("Accept-Language");   //PRG:RG6:R212:1-Jun-2011:IR-111810V6R2012x
        String relationship = (String) emxGetParameter(request, "relationship");
        String relationshipName = DomainConstants.EMPTY_STRING;
        HashMap<String, String> oidAndConnId = new HashMap<>();
        DomainObject domainObject = DomainObject.newInstance(context);
        if ("remove".equalsIgnoreCase(mode)) {
            if (UIUtil.isNotNullAndNotEmpty(objectId)) {
                domainObject.setId(objectId);
                relationshipName = PropertyUtil.getSchemaProperty(relationship);
                MapList relatedObjects = domainObject.getRelatedObjects(
                        context,
                        relationshipName,
                        "*",
                        new StringList(DomainConstants.SELECT_ID),
                        new StringList(DomainRelationship.SELECT_ID),
                        true,
                        true,
                        (short) 1,
                        "",
                        "",
                        0
                );

                relatedObjects.stream().map(m -> {
                    Map map = (Map) m;
                    String oid = (String) map.get(DomainConstants.SELECT_ID);
                    String rid = (String) map.get(DomainRelationship.SELECT_ID);
                    oidAndConnId.put(oid, rid);
                    return "";
                }).collect(Collectors.toCollection(StringList::new));
            }
        }
        StringList actionOid = new StringList();
        StringList actionRid = new StringList();
        logger.info("@@@@@@@@@@@@@@@@@");
        //拿取选择对象
        for (int i = 0; i < strSelectIds.length;i++) {
            String[] split = strSelectIds[i].split("\\|");
            System.out.println("@@@@@@@@@@@@@@@@@:strSelectIds:" + strSelectIds[i]);
            actionOid.add(split[1].trim());
            if ("remove".equalsIgnoreCase(mode)) {
                if (UIUtil.isNullOrEmpty(split[0].trim())) {
                    actionRid.add(oidAndConnId.get(split[1].trim()));
                } else {
                    actionRid.add(split[0].trim());
                }
            }
        }
        logger.info("@@@@@@@@@@@@@@@@@:actionOid:{}" , actionOid.toString());
        logger.info("@@@@@@@@@@@@@@@@@:actionRid:{}" , actionRid.toString());

        String initargs[] = {};
        StringList strMess = new StringList();
        //基本对象
        if ("remove".equalsIgnoreCase(mode)) {
            //移除的话 需要校验选择的对象是否与基础对象有关系
            for (String strId : actionOid) {
                DomainObject selectObject = DomainObject.newInstance(context, strId);
                HashMap<String, String> paramsMap = new HashMap<>();
                paramsMap.put("relName", relationshipName);
                paramsMap.put("fromId", objectId);
                paramsMap.put("toId", strId);
                Boolean flag = (Boolean) JPO.invoke(context, "JF_PublicMethodClass", initargs, "getTwoBusinessObject", JPO.packArgs(paramsMap), Boolean.class);
                if (!flag) {
                    //无关联关系
                    strMess.add(selectObject.getInfo(context, DomainConstants.SELECT_NAME));
                }
            }
            if (strMess.isEmpty()) {
                //全部符合条件 开始移除关系
                ContextUtil.pushContext(context);
                DomainRelationship.disconnect(context, actionRid.toStringArray());
                ContextUtil.popContext(context);
                alertMess = EnoviaResourceBundle.getProperty(context, suiteKey, context.getLocale(), "emxComponents.Remove.Successful");
            } else {
                alertMess = EnoviaResourceBundle.getProperty(context, suiteKey, context.getLocale(), "emxComponents.Remove.fail");
                alertMess += strMess.join(",");
            }
            if ("JFRelPart2Raw".equalsIgnoreCase(relationshipName)) {
//                domainObject.setAttributeValue(context, "PLMEntity.V_Name", "");
                domainObject.setAttributeValue(context, "PLMEntity.V_description", "");
            }
        } else if ("delete".equalsIgnoreCase(mode)) {
            String checkType = (String) emxGetParameter(request, "checkType");
            logger.info("@@@@@@@@@@@@@@@@@:checkType:{}" , checkType.toString());
            if ("Route".equalsIgnoreCase(checkType)) {
                //校验对象的流程是否有 或者被拒绝
                for (String strId : actionOid) {
                    DomainObject object = DomainObject.newInstance(context, strId);
                    String current = object.getInfo(context, DomainObject.SELECT_CURRENT);
                    if (!"In_Work".equalsIgnoreCase(current)) {
                        strMess.add(object.getAttributeValue(context, DomainConstants.ATTRIBUTE_TITLE));
                    }
                }
            }
            logger.info("strMess:{}" , strMess.toString());
            if (strMess.isEmpty()) {
                String id = actionOid.get(0).trim();
                DomainObject object = DomainObject.newInstance(context, id);
                String type = object.getInfo(context, DomainConstants.SELECT_TYPE);
                ContextUtil.pushContext(context);
                DomainObject.deleteObjects(context, actionOid.toStringArray());
                ContextUtil.popContext(context);
                if ("JFDataOutSource".equalsIgnoreCase(type)) {
                    strFlushTableName = "JFDataOutSourceTable";
                }
                alertMess = EnoviaResourceBundle.getProperty(context, suiteKey, context.getLocale(), "emxComponents.Delete.Successful");
            } else {
                alertMess = EnoviaResourceBundle.getProperty(context, suiteKey, context.getLocale(), "emxComponents.delete.fail");
                alertMess += strMess.join(",");
            }
        }

    }catch (Exception e) {
        e.printStackTrace();
        alertMess = EnoviaResourceBundle.getProperty(context, suiteKey, context.getLocale(), "emxComponents.action.error");
    }
%>

<html>
<script >
    alert("<%=alertMess%>");
    var table = "<%=strFlushTableName%>";
    if (table == "") {
        parent.getTopWindow().findFrame(parent.getTopWindow(),"detailsDisplay").location.href = parent.getTopWindow().findFrame(parent.getTopWindow(),"detailsDisplay").location.href.replace("persist=true", "persist=false");
    } else {
        parent.refreshSBTable(table, "Name", "ascending");
    }
</script>
</html>