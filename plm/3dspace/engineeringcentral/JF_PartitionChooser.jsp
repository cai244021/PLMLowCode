<%@ page import="com.dscn.plm.util.NioJDUtils" %>
<%@ page import="java.util.Properties" %>
<%@ page import="com.matrixone.apps.domain.DomainObject" %>
<%@ page import="matrix.util.StringList" %>
<%@ page import="com.matrixone.apps.domain.DomainConstants" %>
<%@ page import="com.matrixone.apps.domain.util.MapList" %>
<%@ page import="java.util.Map" %>
<%@ page import="com.matrixone.apps.domain.util.FrameworkUtil" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="matrix.db.RelationshipType" %>
<%@ page import="com.matrixone.apps.domain.DomainRelationship" %>
<%@ page import="matrix.db.Context" %>
<%@ page import="matrix.db.JPO" %>
<%@ page import="com.matrixone.apps.framework.ui.UIUtil" %>
<%@ page import="static com.matrixone.apps.domain.DomainConstants.SELECT_LEVEL" %>
<%@ page import="com.matrixone.apps.program.ProgramCentralConstants" %>
<%@ page import="static com.matrixone.apps.domain.DomainConstants.SELECT_ATTRIBUTE_TITLE" %>
<%@ page import="com.matrixone.apps.domain.util.ContextUtil" %>
<%@ page import="static com.matrixone.apps.domain.DomainConstants.*" %>
<%@ page import="org.slf4j.LoggerFactory" %>
<%@ page import="org.slf4j.Logger" %>

<%@include file = "../common/emxNavigatorInclude.inc"%>
<%@include file = "../common/emxNavigatorTopErrorInclude.inc"%>
<%!
    private static final Logger LOGGER = LoggerFactory.getLogger("JF_PartitionChooser.jsp");
    public void disconnectPartion(Context context, DomainObject obj,MapList libMapList,StringList classIdList)throws Exception{
        StringList boSel = new StringList();
        boSel.add(DomainConstants.SELECT_ID);
        boSel.add("attribute[Title]");
        StringList relSel = new StringList();
        relSel.add(DomainRelationship.SELECT_ID);
        DomainObject libBO = DomainObject.newInstance(context);
        for (int i = 0; i < libMapList.size(); i++) {
            Map libMap = (Map) libMapList.get(i);
            String strLibId = (String) libMap.get(SELECT_ID);
            libBO.setId(strLibId);
            MapList classMapList = libBO.getRelatedObjects(context,"Subclass","*",boSel,relSel,false,true,(short)0,"","",0);

            if (classIdList.size() > 0){
                for (int i1 = 0; i1 < classIdList.size(); i1++) {
                    String strConnClassId = classIdList.get(i1);
                    for (int i2 = 0; i2 < classMapList.size(); i2++) {
                        Map classMap = (Map)classMapList.get(i2);
                        String strClassId = (String) classMap.get(SELECT_ID);
                        if (strClassId.equals(strConnClassId)){
                            LOGGER.info("disconnection begin ");
                            obj.disconnect(context,new RelationshipType("Classified Item"),false,DomainObject.newInstance(context,strConnClassId));
                            LOGGER.info("disconnection end  ");
                            break;
                        }
                    }
                }
            }
        }
    }
%>
<%
    try {
        String ATTRIBUTE_PARTITIONCN = "JF_VPMReference.JF_PartNameCN";
        String ATTRIBUTE_PARTITIONEN = "JF_VPMReference.JF_PartNameEN";
        String strObjectId = emxGetParameter(request, "selectId");
        String strTypeName = emxGetParameter(request, "typeName");
        StringList selectIds = FrameworkUtil.split(strObjectId,",");
        String tableRowId = emxGetParameter(request, "emxTableRowId");
        String classId = FrameworkUtil.split(tableRowId, "|").get(0);
        //20260825 update by ljr 后台再次校验标准件分类权限，防止绕过分类树直接提交。
        Boolean isPartitionClassAllowed = (Boolean) JPO.invoke(context, "JF_Library", null,
                "checkPartitionClassAllowed", new String[]{classId}, Boolean.class);
        if (!Boolean.TRUE.equals(isPartitionClassAllowed)) {
            throw new Exception("当前用户无权选择该标准件分类");
        }
//        if(UIUtil.isNotNullAndNotEmpty(classId)){
//            DomainObject classObj = DomainObject.newInstance(context,classId);
//
//            //中文
//            String title = classObj.getInfo(context, DomainConstants.SELECT_ATTRIBUTE_TITLE);
//            //英文
//            String description = classObj.getInfo(context, DomainConstants.SELECT_DESCRIPTION);
//            StringList typeSelectList = new StringList();
//            typeSelectList.add(DomainConstants.SELECT_ID);
//            typeSelectList.add(DomainConstants.SELECT_NAME);
//            typeSelectList.add(DomainConstants.SELECT_TYPE);
//            typeSelectList.add(DomainConstants.SELECT_REVISION);
//            typeSelectList.add(DomainConstants.SELECT_CURRENT);
//            typeSelectList.add(DomainConstants.SELECT_ATTRIBUTE_TITLE);
//            typeSelectList.add(DomainConstants.SELECT_OWNER);
//            StringList relSelectList = new StringList();
//            MapList objectList = classObj.getRelatedObjects(context, "Subclass", // relationship pattern
//                    "General Library,General Class",                                    // object pattern
//                    typeSelectList,                            // object selects
//                    relSelectList, // relationship selects
//                    true,                                        // to direction
//                    false,                                        // from direction
//                    (short) 0,                                    // recursion level
//                    "",                // object where clause
//                    "",
//                    (short) 0);
//            objectList.addSortKey(SELECT_LEVEL, ProgramCentralConstants.DESCENDING_SORT, ProgramCentralConstants.SORTTYPE_INTEGER);
//            objectList.sort();
//            // add by chenyan 新增 创建零件时只能选择单件 创建 总成时只能选择组合件 component(零件) assembly(装配) 2025/07/22
//
//            if (objectList.size() > 0) {
//                Map rootNodeMap = (Map) objectList.get(0);
//                String strLibTitle = (String) rootNodeMap.get(SELECT_ATTRIBUTE_TITLE);
//                String  strLibRange = strLibTitle.split("-")[0].substring(strLibTitle.split("-")[0].length() - 1);
//            }
//        }
        MapList librarys = null ;
        if (UIUtil.isNotNullAndNotEmpty(strTypeName)){
            StringBuilder sbWhere = new StringBuilder();
            sbWhere.append("attribute[Title]~~'G");
            sbWhere.append(strTypeName);
            sbWhere.append("*'");
            StringList boSel = new StringList();
            boSel.add(DomainConstants.SELECT_ID);
            boSel.add(DomainConstants.SELECT_DESCRIPTION);
            boSel.add(DomainConstants.SELECT_NAME);
            boSel.add("attribute[Title]");
            try {
                ContextUtil.pushContext(context);
                librarys = DomainObject.findObjects(context,"General Library","*",sbWhere.toString(),boSel);
                LOGGER.info("librarys:{}",librarys);
            }finally {
                ContextUtil.popContext(context);
            }


        }
        for (String selectId : selectIds) {

            if(UIUtil.isNotNullAndNotEmpty(selectId)) {
                DomainObject selectObj = DomainObject.newInstance(context, selectId);
                StringList boSel = new StringList();
                boSel.add(DomainConstants.SELECT_ID);
                boSel.add("attribute[Title]");
                StringList relSel = new StringList();
                relSel.add(DomainRelationship.SELECT_ID);
                StringList classIdList = selectObj.getInfoList(context,"to[Classified Item].from.id");
                LOGGER.info("classIdList:{}",classIdList);
                boolean isSkip = false ;
                if (classIdList.size() > 0){
                    for (int i = 0; i < classIdList.size(); i++) {
                        String strConnClassId = classIdList.get(i);
                        if (strConnClassId.equals(classId)){
                            isSkip = true;
                            break;
                        }
                    }
                }
                //可能会同一个
                if (!isSkip){
                    selectObj.addFromObject(context,new RelationshipType("Classified Item"),classId);
                }
//                //查询已经关联的分区并移除
                if (UIUtil.isNotNullAndNotEmpty(strTypeName) && (!isSkip)){
                    disconnectPartion(context, selectObj,librarys,classIdList);
                }
            }

        }
    }catch (Exception e){
        e.printStackTrace();
    }
%>
<script>
var refreshURL = window.top.opener.document.location.href;
window.top.opener.document.location.href = refreshURL;
window.top.close();
</script>
