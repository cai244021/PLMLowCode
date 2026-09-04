<%@ page import="java.util.Map" %>
<%@ page import="com.matrixone.apps.domain.DomainObject" %>
<%@ page import="matrix.db.RelationshipType" %>
<%@ page import="com.matrixone.apps.domain.util.FrameworkUtil" %>
<%@ page import="matrix.util.StringList" %>
<%@ page import="com.matrixone.apps.domain.DomainRelationship" %>
<%@ page import="com.matrixone.apps.domain.DomainConstants" %>
<%@ page import="org.slf4j.Logger" %>
<%@ page import="org.slf4j.LoggerFactory" %>
<%@ page import="com.matrixone.apps.domain.util.ContextUtil" %>
<%@ page import="com.matrixone.apps.framework.ui.UIUtil" %>
<%@ page import="com.matrixone.apps.common.util.ComponentsUIUtil" %>
<%@ page import="matrix.db.JPO" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="static com.matrixone.apps.common.util.JSPUtil.emxGetParameterValues" %>
<!--
    DR添加物理产品
-->
<%@include file = "../common/emxNavigatorInclude.inc"%>
<%@include file = "../common/emxNavigatorTopErrorInclude.inc"%>
<%!
    private static final Logger JF_LOGGER = LoggerFactory.getLogger("JF_ColorGroupVpmBomSubmit.jsp");
%>
<%
    Boolean isPush = Boolean.FALSE;
    String mess = DomainConstants.EMPTY_STRING;
    String flag = "Y";
    try {
        String ids = emxGetParameter(request, "ids");
        String[] split = ids.split(";");
        String strPsId = split[0];
        JF_LOGGER.info("strPsId:{}", strPsId);
        String objectId = split[1];
        JF_LOGGER.info("objectId:{}", objectId);
        String partLists = split[2];
        JF_LOGGER.info("partLists:{}", partLists);
        String selectNames = split[3];
        StringList rowParentNameList = new StringList();
        StringList parentColorGroupList = new StringList();
        if (split.length > 4) {
            String rowParentNames = split[5];
            String parentNames = rowParentNames.substring(1);
            rowParentNameList = StringList.create(parentNames.split(","));
            String parentColorGroups = split[4];
            JF_LOGGER.info("parentColorGroups:{}", parentColorGroups);
            String substring = parentColorGroups.substring(1);
            parentColorGroupList = StringList.create(substring.split(","));
        }
        JF_LOGGER.info("selectNames:{}", selectNames);
        String[] tableRowIdList = emxGetParameterValues(request, "emxTableRowId");
        String selectColorId = DomainConstants.EMPTY_STRING;
        StringList sList = FrameworkUtil.split(tableRowIdList[0],"|");
        if(sList.size() == 2) {
            selectColorId = (String) sList.get(1);
        } else {
            selectColorId = (String)sList.get(0);
        }
        DomainObject domainObject = DomainObject.newInstance(context, selectColorId);
        String selectColorGroup = domainObject.getAttributeValue(context, DomainConstants.ATTRIBUTE_TITLE);
        //颜色矩阵
        String strColorMatrixId = domainObject.getInfo(context, "to[JFColorMatrix2JFColorGroup].from.id");
        DomainObject domainObject1 = DomainObject.newInstance(context, strColorMatrixId);
        String strColorMatrixName = domainObject1.getInfo(context, DomainConstants.SELECT_NAME);
        JF_LOGGER.info("selectColorGroup:{}", selectColorGroup);
        JF_LOGGER.info("selectColorId:{}", selectColorId);
        JF_LOGGER.info("parentColorGroupList:{}", parentColorGroupList);
        StringList selectNameList = StringList.create(selectNames.split(","));
        JF_LOGGER.info("selectNameList:{}", selectNameList);
        JF_LOGGER.info("rowParentNameList:{}", rowParentNameList);
        //校验：
        StringList errorList = new StringList();
        for (int i = 0; i < rowParentNameList.size(); i++) {
            if (selectNameList.contains(rowParentNameList.get(i))) {
                continue;
            } else {
                String colorGroup = parentColorGroupList.get(i);
                if ("NA".equalsIgnoreCase(selectColorGroup)) {
                    continue;
                } else {
                    if ("NA".equalsIgnoreCase(colorGroup)) {
                        if (selectNameList.size() > rowParentNameList.size()) {
                            errorList.add(selectNameList.get(i + 1));
                        } else {
                            errorList.add(selectNameList.get(i));
                        }
                    } else {
                        continue;
                    }
                }
            }
        }
//        for (int i = 0; i < parentColorGroupList.size(); i++) {
//            String colorGroup = parentColorGroupList.get(i);
//            if ("UA".equalsIgnoreCase(colorGroup)) {
//                continue;
//            } else if ("NA".equalsIgnoreCase(selectColorGroup)) {
//                continue;
//            } else {
//                if ("NA".equalsIgnoreCase(colorGroup)) {
//                    errorList.add(selectNameList.get(i));
//                } else {
//                    continue;
//                }
//            }
//        }
        JF_LOGGER.info("errorList:{}", errorList);
        if (errorList.size() > 0) {
            flag = "N";
            mess = ComponentsUIUtil.getI18NString(context, context.getLocale().toString(), "emxComponents.ColorGroup.errorMess", new String[]{});
            mess += errorList.join(",");
        } else {
            StringList partList = StringList.create(partLists.split(","));
            JF_LOGGER.info("partList:{}", partList);
            String backUsername = context.getUser();
            ContextUtil.pushContext(context);
            isPush = Boolean.TRUE;
            DomainObject partObject = DomainObject.newInstance(context);
            String rel = "JFProject2ColorGroup";
            for (int i = 0; i < partList.size(); i++) {
                String partId = partList.get(i);
                partObject.setId(partId);
                Map paramsMap = new HashMap<>();
                paramsMap.put("relName", rel);
                paramsMap.put("fromId", strPsId);
                paramsMap.put("toId", partId);
                String connId = (String) JPO.invoke(context,"JF_PublicMethodClass",null,"getTwoBusinessObjectConnId", JPO.packArgs(paramsMap), String.class);
                DomainRelationship domainRelationship;
                if (UIUtil.isNullOrEmpty(connId)) {
                    domainRelationship = partObject.addFromObject(context, new RelationshipType("JFProject2ColorGroup"), strPsId);
                } else {
                    domainRelationship = DomainRelationship.newInstance(context, connId);
                }
                domainRelationship.setAttributeValue(context, "JF_ColorGroupName", selectColorGroup);
                domainRelationship.setAttributeValue(context, "JF_ColorGroupNameEdit", "work");
                domainRelationship.setAttributeValue(context, "JF_ColorMatrixName", strColorMatrixName);
                String[] array = new String[]{"connection",connId,backUsername};
                String reslut = (String) JPO.invoke(context, "JF_Util", array, "addHistory", array, String.class);
            }
            //关联整椅和颜色矩阵的关系
            DomainObject objectVPM = DomainObject.newInstance(context, objectId);
            String vpmColorMatrixId = objectVPM.getInfo(context, "from[JFVPMReference2JFColorMatrix].to.id");

        /*
            2. 查询下当前整椅和颜色矩阵的关系，
                1. 如果是同一个就忽略，
                2. 如果不存在就建立整椅和颜色矩阵关系
                3. 如果整椅存在颜色矩阵关系、但是是其他的颜色矩阵，就直接替换
        */
            if (UIUtil.isNullOrEmpty(vpmColorMatrixId)) {
                //如果不存在就建立整椅和颜色矩阵关系
                objectVPM.addToObject(context, new RelationshipType("JFVPMReference2JFColorMatrix"), strColorMatrixId);
            } else if (!strColorMatrixId.equalsIgnoreCase(vpmColorMatrixId)) {
                //如果整椅存在颜色矩阵关系、但是是其他的颜色矩阵，就直接替换
                String vpmColorMatrixConnId = objectVPM.getInfo(context, "from[JFVPMReference2JFColorMatrix].id");
                DomainRelationship.setToObject(context, vpmColorMatrixConnId, DomainObject.newInstance(context, strColorMatrixId));
            }
            mess = ComponentsUIUtil.getI18NString(context, context.getLocale().toString(), "emxComponents.ColorGroup.Successful", new String[]{});
        }
    }catch (Exception e){
        e.printStackTrace();
        flag = "N";
        mess =   ComponentsUIUtil.getI18NString(context, context.getLocale().toString(), "emxComponents.ColorGroup.Failed", new String[]{});
    } finally {
        if (isPush) {
            ContextUtil.popContext(context);
        }
    }
%>
<%@include file = "../common/emxNavigatorBottomErrorInclude.inc"%>
<script>
    const flag = "<%=flag%>";
    alert("<%=mess%>");
    if ("Y" === flag) {

        getTopWindow().closeWindow();
        getTopWindow().openerFindFrame(getTopWindow(),"detailsDisplay").emxEditableTable.refreshSelectedRows();
        // getTopWindow().getWindowOpener().parent.document.location.href = getTopWindow().getWindowOpener().parent.document.location.href;
        // var refreshURL = getTopWindow().getWindowOpener().location.href;
        // getTopWindow().getWindowOpener().location.href = refreshURL;
    }
</script>
