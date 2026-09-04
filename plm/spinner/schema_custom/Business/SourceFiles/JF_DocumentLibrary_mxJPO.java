import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.DomainRelationship;
import com.matrixone.apps.domain.util.ContextUtil;
import com.matrixone.apps.domain.util.EnoviaResourceBundle;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.util.PersonUtil;
import com.matrixone.apps.domain.util.PropertyUtil;
import com.matrixone.apps.domain.util.XSSUtil;
import com.matrixone.apps.program.ProgramCentralConstants;
import com.matrixone.apps.program.ProgramCentralUtil;
import matrix.db.Context;
import matrix.db.JPO;
import matrix.util.StringList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

public class JF_DocumentLibrary_mxJPO {
    private static final Logger JF_LOGGER = LoggerFactory.getLogger(JF_DocumentLibrary_mxJPO.class);
    private final String STATE_Document_FROZEN="state_FROZEN";
    private final String POLICY_Document = "policy_Document";
    private final String SUITE_KEY = "emxComponentsStringResource";

    /**
     * 过滤出去不需要的分类-- 除技术文档外
     * @param context
     * @param args
     * @return
     * @throws Exception
     */
    public StringList selectGeneralLibrary2(Context context, String[] args) throws Exception {
        StringList strings = new StringList();
        String[] key = {"GeneralLibrary"};
        String[] key1 = {"technicalStandard"};
        String title = JF_PublicMethodClass_mxJPO.getBasicUrl(context, key);
        String technicalStandard = JF_PublicMethodClass_mxJPO.getBasicUrl(context, key1);
        StringList objectSelects = new StringList();
        objectSelects.add("id");
        objectSelects.add("name");
        objectSelects.add("revision");
        objectSelects.add("attribute[Title]");
        MapList mapList = DomainObject.findObjects(context, "General Library", "*", "attribute[Title]==" + title, objectSelects);
        Iterator iterator = mapList.iterator();
        while (iterator.hasNext()){
            Map map = (Map) iterator.next();
            String id = (String) map.get("id");
            DomainObject domainObject = DomainObject.newInstance(context, id);
            MapList mapList1 = domainObject.getRelatedObjects(context, "Subclass", "*", objectSelects, null, false, true, (short) 0, "", "", 0);
            Iterator iterator1 = mapList1.iterator();
            while (iterator1.hasNext()){
                Map map1 = (Map) iterator1.next();
                String id1 = (String) map1.get("id");
                String classTitle = (String) map1.get("attribute[Title]");
                if (!technicalStandard.equals(classTitle)){
                    if (!strings.contains(id1)){
                        strings.add(id1);
                    }
                }
            }
        }
        return strings;
    }


    /**
     * 技术文档提升状态时的启动流程
     * @param context
     * @param args
     * @throws Exception
     */
    public void PromoteDocumentToRELEASED(Context context, String[] args) throws Exception {
        boolean flag = false;
        try {
            String objectId = args[0];
            DomainObject domainObject = DomainObject.newInstance(context, objectId);
            String contextUser = context.getUser();
            String JF_DocumentType = domainObject.getInfo(context, "attribute[JF_DocumentType]");
            String[] key1 = {"technicalStandard"};
            String technicalStandard = JF_PublicMethodClass_mxJPO.getBasicUrl(context, key1);
            if (technicalStandard.equals(JF_DocumentType) || "XSO".equals(JF_DocumentType) || "Technical Standard".equals(JF_DocumentType)){
            JF_Route_mxJPO jf_route = new JF_Route_mxJPO(context, args);
            String lineManagerId = JF_PublicMethodClass_mxJPO.getPersonLineManager(context, null, contextUser);
            String DepartmentDirectorId = domainObject.getInfo(context, "relationship[JFDocument2Person].to.id");
            MapList approveList = new MapList();
            ContextUtil.pushContext(context);
                flag =true;
                if (technicalStandard.equals(JF_DocumentType)){
                    if (lineManagerId != null && !"".equals(lineManagerId)){
                        String tileMess = EnoviaResourceBundle.getProperty(context, SUITE_KEY, context.getLocale(), "emxComponents.DRWManger.ROUTE.lineManager");
                        Map nReceiverMapOne = (Map) jf_route.getMap(lineManagerId, tileMess);//设置审批信息 标题
                        nReceiverMapOne.put(DomainObject.ATTRIBUTE_ROUTE_SEQUENCE, "1");
                        approveList.add(nReceiverMapOne);
                    }
                }
                if (DepartmentDirectorId != null && !"".equals(DepartmentDirectorId)){
                    String tileMess = EnoviaResourceBundle.getProperty(context, SUITE_KEY, context.getLocale(), "emxComponents.DRWManger.ROUTE.MESS");
                    Map nReceiverMapOne = (Map) jf_route.getMap(DepartmentDirectorId, tileMess);//设置审批信息 标题
                    nReceiverMapOne.put(DomainObject.ATTRIBUTE_ROUTE_SEQUENCE, "2");
                    approveList.add(nReceiverMapOne);
                }
                String routeId = jf_route.createAndStartRoute(context, approveList, objectId, STATE_Document_FROZEN, POLICY_Document, "DocumentPromoteTitle");
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            if(flag) {
                ContextUtil.popContext(context);
            }
        }
    }

    /**
     * 创建交付物： 非研发 选择SSOW  其他，研发：所有
     * @param context
     * @param args
     * @return
     * @throws Exception
     */
    public StringList selectGeneralLibraryDoc(Context context, String[] args) throws Exception {
        Map paramMap = (Map)JPO.unpackArgs(args);
        JF_LOGGER.info("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
        JF_LOGGER.info("paramMap:{}", paramMap);
        String classFlag = (String) paramMap.get("classFlag");
        StringList strings = new StringList();
        String[] key = {"GeneralLibrary"};
        String[] key1 = {"technicalStandard"};
        String[] key2 = {"DepartmentGeneralLibrary"};
        String title = JF_PublicMethodClass_mxJPO.getBasicUrl(context, key);
        String technicalStandard = JF_PublicMethodClass_mxJPO.getBasicUrl(context, key1);
        String departmentGeneralLibrary = JF_PublicMethodClass_mxJPO.getBasicUrl(context, key2);
        JF_LOGGER.info("departmentGeneralLibrary:{}", departmentGeneralLibrary.toString());
        StringList objectSelects = new StringList();
        objectSelects.add("id");
        objectSelects.add("name");
        objectSelects.add("revision");
        objectSelects.add("attribute[Title]");
        MapList mapList = DomainObject.findObjects(context, "General Library", "*", "attribute[Title]==" + title, objectSelects);
        Iterator iterator = mapList.iterator();
        while (iterator.hasNext()){
            Map map = (Map) iterator.next();
            String id = (String) map.get("id");
            DomainObject domainObject = DomainObject.newInstance(context, id);
            MapList mapList1 = domainObject.getRelatedObjects(context, "Subclass", "*", objectSelects, null, false, true, (short) 0, "", "", 0);
            Iterator iterator1 = mapList1.iterator();
            while (iterator1.hasNext()){
                Map map1 = (Map) iterator1.next();
                String id1 = (String) map1.get("id");
                String classTitle = (String) map1.get("attribute[Title]");
                JF_LOGGER.info("classTitle:{}", classTitle.toString());
                if ("false".equalsIgnoreCase(classFlag) && !departmentGeneralLibrary.contains(classTitle)) {
                    continue;
                }
                if (!technicalStandard.equals(classTitle)){
                    if (!strings.contains(id1)){
                        strings.add(id1);
                    }
                }
            }
        }
        return strings;
    }

    /**
     * 查询项目文件夹中所选文档版本引用的业务对象。
     *
     * @param context Matrix上下文
     * @param args 表格请求参数，包含校验后的selectedDocumentIds或选中的emxTableRowId
     * @return MapList 按文档号、版本号分组排序的文档引用数据
     * @throws Exception 查询文档或引用关系失败时抛出异常
     * @author LIUJR
     * @date 2026/8/10
     */
    @com.matrixone.apps.framework.ui.ProgramCallable
    public MapList getDocumentReferenceQueryList(Context context, String[] args) throws Exception {
        Map programMap = (Map) JPO.unpackArgs(args);
        Object selectedDocumentIdsValue = programMap.get("selectedDocumentIds");
        if (selectedDocumentIdsValue == null && programMap.get("RequestValuesMap") instanceof Map) {
            selectedDocumentIdsValue = ((Map) programMap.get("RequestValuesMap")).get("selectedDocumentIds");
        }
        if (selectedDocumentIdsValue == null && programMap.get("requestMap") instanceof Map) {
            Map requestMap = (Map) programMap.get("requestMap");
            selectedDocumentIdsValue = requestMap.get("selectedDocumentIds");
            if (selectedDocumentIdsValue == null && requestMap.get("RequestValuesMap") instanceof Map) {
                selectedDocumentIdsValue = ((Map) requestMap.get("RequestValuesMap")).get("selectedDocumentIds");
            }
        }

        //校验JSP将完整的多选文档ID作为独立参数传入，避免表格二次请求时丢失emxTableRowId。
        Set<String> selectedDocumentIdSet = new LinkedHashSet<>();
        if (selectedDocumentIdsValue instanceof String[]) {
            for (String selectedDocumentIds : (String[]) selectedDocumentIdsValue) {
                if (selectedDocumentIds != null) {
                    for (String selectedDocumentId : selectedDocumentIds.split(",")) {
                        if (selectedDocumentId.length() > 0) {
                            selectedDocumentIdSet.add(selectedDocumentId);
                        }
                    }
                }
            }
        } else if (selectedDocumentIdsValue != null) {
            for (String selectedDocumentId : String.valueOf(selectedDocumentIdsValue).split(",")) {
                if (selectedDocumentId.length() > 0) {
                    selectedDocumentIdSet.add(selectedDocumentId);
                }
            }
        }

        //兼容从其他入口直接调用表格时传入的标准Structure Browser行标识。
        if (selectedDocumentIdSet.isEmpty()) {
            Object selectedRowValue = programMap.get("emxTableRowId");
            if (selectedRowValue == null && programMap.get("RequestValuesMap") instanceof Map) {
                selectedRowValue = ((Map) programMap.get("RequestValuesMap")).get("emxTableRowId");
            }
            if (selectedRowValue == null && programMap.get("requestMap") instanceof Map) {
                Map requestMap = (Map) programMap.get("requestMap");
                selectedRowValue = requestMap.get("emxTableRowId");
                if (selectedRowValue == null && requestMap.get("RequestValuesMap") instanceof Map) {
                    selectedRowValue = ((Map) requestMap.get("RequestValuesMap")).get("emxTableRowId");
                }
            }

            StringList selectedRowIdList = new StringList();
            if (selectedRowValue instanceof String[]) {
                for (String selectedRowId : (String[]) selectedRowValue) {
                    selectedRowIdList.add(selectedRowId);
                }
            } else if (selectedRowValue instanceof StringList) {
                selectedRowIdList.addAll((StringList) selectedRowValue);
            } else if (selectedRowValue != null) {
                selectedRowIdList.add(String.valueOf(selectedRowValue));
            }

            for (String selectedRowId : selectedRowIdList) {
                if (selectedRowId == null || selectedRowId.length() == 0) {
                    continue;
                }
                Map parsedRowMap = ProgramCentralUtil.parseTableRowId(context, selectedRowId);
                String selectedObjectId = (String) parsedRowMap.get("objectId");
                if (selectedObjectId != null && selectedObjectId.length() > 0) {
                    selectedDocumentIdSet.add(selectedObjectId);
                }
            }
        }

        MapList referenceResultList = new MapList();
        if (selectedDocumentIdSet.isEmpty()) {
            return referenceResultList;
        }

        String titleSelect = DomainObject.getAttributeSelect(DomainConstants.ATTRIBUTE_TITLE);
        StringList documentSelectList = new StringList();
        documentSelectList.add(DomainConstants.SELECT_ID);
        documentSelectList.add(DomainConstants.SELECT_NAME);
        documentSelectList.add(DomainConstants.SELECT_REVISION);
        documentSelectList.add(titleSelect);
        documentSelectList.add(ProgramCentralConstants.SELECT_IS_DOCUMENTS);

        //批量读取所选行，文件夹或其他非文档行不参与引用查询。
        MapList selectedDocumentInfoList = DomainObject.getInfo(
                context,
                selectedDocumentIdSet.toArray(new String[selectedDocumentIdSet.size()]),
                documentSelectList);

        //只要所选对象中包含非文档对象，整次查询均不返回引用数据。
        if (selectedDocumentInfoList.size() != selectedDocumentIdSet.size()) {
            return referenceResultList;
        }
        for (Object selectedObjectItem : selectedDocumentInfoList) {
            Map selectedObjectMap = (Map) selectedObjectItem;
            if (!"true".equalsIgnoreCase((String) selectedObjectMap.get(ProgramCentralConstants.SELECT_IS_DOCUMENTS))) {
                return referenceResultList;
            }
        }

        String relationshipPattern = PropertyUtil.getSchemaProperty("relationship_ReferenceDocument")
                + ",JFESOReview2Document,"
                + DomainConstants.RELATIONSHIP_TASK_DELIVERABLE;
        StringList referenceObjectSelectList = new StringList();
        referenceObjectSelectList.add(DomainConstants.SELECT_ID);
        referenceObjectSelectList.add(DomainConstants.SELECT_TYPE);
        referenceObjectSelectList.add(DomainConstants.SELECT_NAME);
        referenceObjectSelectList.add(DomainConstants.SELECT_REVISION);
        referenceObjectSelectList.add(DomainConstants.SELECT_OWNER);
        referenceObjectSelectList.add(DomainConstants.SELECT_DESCRIPTION);
        referenceObjectSelectList.add(titleSelect);
        StringList referenceRelationshipSelectList = new StringList(DomainRelationship.SELECT_ID);
        for (Object documentItem : selectedDocumentInfoList) {
            Map documentMap = (Map) documentItem;
            String documentId = (String) documentMap.get(DomainConstants.SELECT_ID);
            String documentName = (String) documentMap.get(DomainConstants.SELECT_NAME);
            String documentTitle = (String) documentMap.get(titleSelect);
            DomainObject documentObject = DomainObject.newInstance(context, documentId);

            //文档处于三种关系的To端，从文档反查附件、外发内容、ESO签发表及任务交付物来源对象。
            MapList referenceObjectList = documentObject.getRelatedObjects(
                    context,
                    relationshipPattern,
                    DomainConstants.QUERY_WILDCARD,
                    referenceObjectSelectList,
                    referenceRelationshipSelectList,
                    true,
                    false,
                    (short) 1,
                    DomainConstants.EMPTY_STRING,
                    DomainConstants.EMPTY_STRING,
                    0);
            referenceObjectList.addSortKey(DomainConstants.SELECT_TYPE, ProgramCentralConstants.ASCENDING_SORT,
                    ProgramCentralConstants.SORTTYPE_STRING);
            referenceObjectList.addSortKey(DomainConstants.SELECT_NAME, ProgramCentralConstants.ASCENDING_SORT,
                    ProgramCentralConstants.SORTTYPE_STRING);
            referenceObjectList.sort();

            //每个文档版本只返回一行，多个关联对象在对应列中按相同顺序换行显示。
            StringBuilder referenceObjectTypeHtml = new StringBuilder();
            StringBuilder referenceObjectNameHtml = new StringBuilder();
            StringBuilder referenceObjectTitleHtml = new StringBuilder();
            StringBuilder referenceObjectOwnerHtml = new StringBuilder();
            StringBuilder referenceObjectDescriptionHtml = new StringBuilder();
            Set<String> referenceObjectIdSet = new HashSet<>();
            int referenceLineCount = 0;
            for (Object referenceItem : referenceObjectList) {
                Map referenceMap = (Map) referenceItem;
                String referenceObjectId = (String) referenceMap.get(DomainConstants.SELECT_ID);
                if (!referenceObjectIdSet.add(referenceObjectId)) {
                    continue;
                }
                if (referenceLineCount > 0) {
                    referenceObjectTypeHtml.append("<br/>");
                    referenceObjectNameHtml.append("<br/>");
                    referenceObjectTitleHtml.append("<br/>");
                    referenceObjectOwnerHtml.append("<br/>");
                    referenceObjectDescriptionHtml.append("<br/>");
                }

                String referenceObjectType = (String) referenceMap.get(DomainConstants.SELECT_TYPE);
                String referenceObjectName = (String) referenceMap.get(DomainConstants.SELECT_NAME);
                String referenceObjectTitle = (String) referenceMap.get(titleSelect);
                String referenceObjectOwner = (String) referenceMap.get(DomainConstants.SELECT_OWNER);
                String referenceObjectDescription = (String) referenceMap.get(DomainConstants.SELECT_DESCRIPTION);
                String referenceObjectTypeDisplay = EnoviaResourceBundle.getTypeI18NString(
                        context,
                        referenceObjectType,
                        context.getSession().getLanguage());

                referenceObjectTypeHtml.append(XSSUtil.encodeForHTML(context, referenceObjectTypeDisplay));
                referenceObjectNameHtml.append("<a href=\"javascript:showModalDialog('emxTree.jsp?objectId=")
                        .append(XSSUtil.encodeForURL(context, referenceObjectId))
                        .append("', 1000, 700, true, 'Large');\">")
                        .append(XSSUtil.encodeForHTML(context, referenceObjectName))
                        .append("</a>");
                referenceObjectTitleHtml.append(XSSUtil.encodeForHTML(
                        context,
                        referenceObjectTitle == null || referenceObjectTitle.length() == 0
                                ? referenceObjectName
                                : referenceObjectTitle));
                referenceObjectOwnerHtml.append(XSSUtil.encodeForHTML(
                        context,
                        referenceObjectOwner == null ? DomainConstants.EMPTY_STRING : referenceObjectOwner));
                referenceObjectDescriptionHtml.append(XSSUtil.encodeForHTML(
                        context,
                        referenceObjectDescription == null
                                ? DomainConstants.EMPTY_STRING
                                : referenceObjectDescription));
                referenceLineCount++;
            }

            Map documentReferenceMap = new HashMap();
            documentReferenceMap.put(DomainConstants.SELECT_ID, documentId);
            documentReferenceMap.put("DocumentName", documentName);
            documentReferenceMap.put("DocumentRevision", documentMap.get(DomainConstants.SELECT_REVISION));
            documentReferenceMap.put("DocumentTitle",
                    documentTitle == null || documentTitle.length() == 0 ? documentName : documentTitle);
            documentReferenceMap.put("ReferenceObjectType", referenceObjectTypeHtml.toString());
            documentReferenceMap.put("ReferenceObjectName", referenceObjectNameHtml.toString());
            documentReferenceMap.put("ReferenceObjectTitle", referenceObjectTitleHtml.toString());
            documentReferenceMap.put("ReferenceObjectOwner", referenceObjectOwnerHtml.toString());
            documentReferenceMap.put("ReferenceObjectDescription", referenceObjectDescriptionHtml.toString());
            referenceResultList.add(documentReferenceMap);
        }

        //同一文档的不同版本连续展示，并按版本号稳定排序。
        referenceResultList.addSortKey("DocumentName", ProgramCentralConstants.ASCENDING_SORT,
                ProgramCentralConstants.SORTTYPE_STRING);
        referenceResultList.addSortKey("DocumentRevision", ProgramCentralConstants.ASCENDING_SORT,
                ProgramCentralConstants.SORTTYPE_STRING);
        referenceResultList.sort();
        return referenceResultList;
    }

    /**
     * 返回文档引用查询表中由数据方法预先填充的文档字段。
     *
     * @param context Matrix上下文
     * @param args 表格列及对象列表参数
     * @return Vector 当前列对应的显示值
     * @throws Exception 参数解析失败时抛出异常
     * @author LIUJR
     * @date 2026/8/10
     */
    public Vector getDocumentReferenceColumnData(Context context, String[] args) throws Exception {
        Map programMap = (Map) JPO.unpackArgs(args);
        Map columnMap = (Map) programMap.get("columnMap");
        MapList objectList = (MapList) programMap.get("objectList");
        String columnName = (String) columnMap.get("name");
        Vector result = new Vector(objectList.size());
        for (Object objectItem : objectList) {
            Map objectMap = (Map) objectItem;
            Object value = objectMap.get(columnName);
            result.add(value == null ? DomainConstants.EMPTY_STRING : String.valueOf(value));
        }
        return result;
    }

    /**
     * 校验文档引用查询的选中对象，并返回去重后的文档ID。
     *
     * @param context Matrix上下文
     * @param args 选中对象ID参数
     * @return Map code为200时返回selectedDocumentIds，否则返回国际化提示信息
     * @throws Exception 查询选中对象失败时抛出异常
     * @author LIUJR
     * @date 2026/8/10
     */
    public Map validateDocumentReferenceQuerySelection(Context context, String[] args) throws Exception {
        Map paramMap = (Map) JPO.unpackArgs(args);
        Object selectedObjectIdsValue = paramMap.get("selectedObjectIds");
        Set<String> selectedObjectIdSet = new LinkedHashSet<>();
        if (selectedObjectIdsValue instanceof String[]) {
            for (String selectedObjectId : (String[]) selectedObjectIdsValue) {
                if (selectedObjectId != null && selectedObjectId.length() > 0) {
                    selectedObjectIdSet.add(selectedObjectId);
                }
            }
        } else if (selectedObjectIdsValue instanceof StringList) {
            selectedObjectIdSet.addAll((StringList) selectedObjectIdsValue);
        } else if (selectedObjectIdsValue != null && String.valueOf(selectedObjectIdsValue).length() > 0) {
            selectedObjectIdSet.add(String.valueOf(selectedObjectIdsValue));
        }

        Map result = new HashMap();
        String invalidSelectionMessage = EnoviaResourceBundle.getProperty(
                context,
                "emxProgramCentralStringResource",
                context.getLocale(),
                "emxProgramCentral.DocumentReference.InvalidSelection");
        if (selectedObjectIdSet.isEmpty()) {
            result.put("code", "400");
            result.put("mess", invalidSelectionMessage);
            return result;
        }

        StringList selectList = new StringList();
        selectList.add(DomainConstants.SELECT_ID);
        selectList.add(ProgramCentralConstants.SELECT_IS_DOCUMENTS);
        MapList selectedObjectInfoList = DomainObject.getInfo(
                context,
                selectedObjectIdSet.toArray(new String[selectedObjectIdSet.size()]),
                selectList);

        //选中项必须全部存在且全部为文档，否则不进入引用信息展示页面。
        if (selectedObjectInfoList.size() != selectedObjectIdSet.size()) {
            result.put("code", "400");
            result.put("mess", invalidSelectionMessage);
            return result;
        }
        for (Object selectedObjectItem : selectedObjectInfoList) {
            Map selectedObjectMap = (Map) selectedObjectItem;
            if (!"true".equalsIgnoreCase((String) selectedObjectMap.get(ProgramCentralConstants.SELECT_IS_DOCUMENTS))) {
                result.put("code", "400");
                result.put("mess", invalidSelectionMessage);
                return result;
            }
        }

        StringBuilder selectedDocumentIds = new StringBuilder();
        for (String selectedObjectId : selectedObjectIdSet) {
            if (selectedDocumentIds.length() > 0) {
                selectedDocumentIds.append(',');
            }
            selectedDocumentIds.append(selectedObjectId);
        }
        result.put("code", "200");
        result.put("selectedDocumentIds", selectedDocumentIds.toString());
        return result;
    }

}
