import com.dassault_systemes.enovia.bps.widget.jaxb.Ui;
import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.DomainRelationship;
import com.matrixone.apps.domain.Job;
import com.matrixone.apps.domain.util.*;
import com.matrixone.apps.framework.ui.UIUtil;
import com.matrixone.apps.program.ProgramCentralConstants;
import matrix.db.*;
import matrix.util.StringList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.*;
import java.util.Set;
import java.util.stream.Collectors;
import org.w3c.dom.Document;

import static com.matrixone.apps.domain.DomainConstants.*;

public class JF_VPMReferenceEBOM_mxJPO implements JF_PLMConstants_mxJPO {
    private static final Logger log = LoggerFactory.getLogger(JF_VPMReferenceEBOM_mxJPO.class);
    String RELATIONSHIP_VPMINSTANCE = "VPMInstance";
    String TYPE_VPMREFERENCE = "VPMReference";
    String RELATIONSHIP_JFECR2ONELEVELPART = "JFECR2OnelevelPart";
    String RELATIOSNHIP_JFONELEVELPART2PART = "JFOneLevelPart2Part";
    String RELATIONSHIP_JFRELATEITEM = "JFRelateItem";
    String RELATIONSHIP_JFECR2MANUFACTURING = "JFECR2Manufacturing";
    String RELATIONSHIP_JFMANUFACTURING2PART = "JFManufacturing2Part";
    String RELATIONSHIP_JFCHANGE2PROJECT = "JFChange2Project";
    String RELATIONSHIP_JFPROJECT2ROOTPART = "JFProject2RootPart";
    String RELATIONSHIP_JFVPMREFERENCE2CUSTOMERPARTS = "JFVPMReference2CustomerParts";
    String RELATIONSHIP_JFVPMREFERENCE2CUSTOMERPARTS2PROJECT = "JFVPMReference2CustomerParts2Project";
    String TYPE_JFCUSTOMERPARTS = "JFCustomerParts";
    String RELATIONSHIP_JFECR2PARTPRICE = "JFECR2PartPrice";
    String RELATIONSHIP_JFECR2MAKEPARTPRICE = "JFECR2MakePartPrice";
    String ATTRIBUTE_JFCHANGEWHOLESEATPRICE = "JFChangeWholeSeatPrice";
    String ATTRIBUTE_JFCHANGEWHOLESEATPRICEEXTERNAL = "JFChangeWholeSeatPriceExternal";
    String ATTRIBUTE_JFCHANGEUNITPRICE = "JFChangeUnitPrice";
    String ATTRIBUTE_JFCHANGEUNITPRICEEXTERNAL = "JFChangeUnitPriceExternal";
    String ATTRIBUTE_PARTTYPE = "JF_VPMReference.JF_PartType";
    String ATTRIBUTE_PARTNUMBER = "EnterpriseExtension.V_PartNumber";
    String ATTRIBUTE_JFECRNAME = "JFECRName";
    String SELECT_ATTRIBUTE_JFECRNAME = "attribute["+ATTRIBUTE_JFECRNAME+"]";
    String v5_Select = "attribute[XCADExtension.V_CADOrigin]==CATIAV5";
    String v6_Select = "attribute[XCADExtension.V_CADOrigin]!=CATIAV5";
    String TYPE_JFDATATRANSMISSION = "type_JFPartNumberG";
    String ATTRIBUTE_JFWHOLECHAIR = "JFWholeChair";
    String SELECT_WEIGTH = "attribute[JF_VPMReference.JF_Weight]";

    /**
     * 创建客户零件号/DB信息后连接零件和项目
     **
     * @param context 上下文
     * @param args 请求参数
     * @return void
     * @throws Exception 异常
     * @author LIUJR
     * @date 2026/7/10 18:30
     */
    public void connectCustomerPartsAfterCreate(Context context, String[] args) throws Exception {
        Map paramsMap = (Map) JPO.unpackArgs(args);
        Map requestMap = (Map) paramsMap.get("requestMap");
        Map paramMap = (Map) paramsMap.get("paramMap");
        String customerPartsId = getRequestValue(paramMap, "newObjectId");
        String projectId = getRequestValue(requestMap, "JFProjectNameOID");
        String partId = getSelectedPartId(requestMap);
        String customerPartNumber = getRequestValue(requestMap, "JFCustomerPartNumber");
        String customerPartName = getRequestValue(requestMap, "JFCustomerPartName");
        String customerPartRevision = getRequestValue(requestMap, "JFCustomerPartRevision");
        String directBuy = getRequestValue(requestMap, "JF_DirectBuy");
        String relDescription = getRequestValue(requestMap, "description");
        log.info("customerPartsId:{} projectId:{} partId:{}", customerPartsId,projectId, partId);
        if (UIUtil.isNullOrEmpty(customerPartsId)) {
            throw new Exception("客户零件号/DB信息对象创建失败");
        }
        //20260817 update by liujr 创建入口与行编辑、删除使用同一项目级权限，流程中及冻结/发布后禁止维护所属项目数据。
        if (!hasCustomerPartsDBProjectModifyAccess(context, partId, projectId)) {
            throw new Exception(EnoviaResourceBundle.getProperty(context,
                    "emxComponentsStringResource",
                    context.getLocale(),
                    "emxComponents.JFCustomerPartsDB.NoModifyAccess"));
        }
        ContextUtil.startTransaction(context, true);
        boolean isPush = false;
        try {
            ContextUtil.pushContext(context);
            isPush = true;
            DomainObject customObj = DomainObject.newInstance(context,customerPartsId);
            // 创建零件与客户零件号/DB对象的关系
            DomainRelationship relationsihpObj = customObj.addToObject(context,new RelationshipType(RELATIONSHIP_JFVPMREFERENCE2CUSTOMERPARTS),partId);
            // 将创建页面填写的客户零件号/DB信息写入关系属性
            Map relationshipAttributeMap = new HashMap();
            relationshipAttributeMap.put(JF_PLMConstants_mxJPO.Attr_JFCustomerPartNumber, customerPartNumber);
            relationshipAttributeMap.put(JF_PLMConstants_mxJPO.Attr_JFCustomerPartName, customerPartName);
            relationshipAttributeMap.put(JF_PLMConstants_mxJPO.Attr_JFCustomerPartRevision, customerPartRevision);
            relationshipAttributeMap.put(JF_PLMConstants_mxJPO.Attr_JF_DirectBuy, directBuy);
            relationshipAttributeMap.put(JF_PLMConstants_mxJPO.Attr_JF_RelDescription, relDescription);
            relationsihpObj.setAttributeValues(context, relationshipAttributeMap);
            String partCustomerRelId =relationsihpObj.getPhysicalId(context);
            log.info("partCustomerRelId:{}",partCustomerRelId);
            // 在零件与客户零件号/DB关系上关联项目
           String matrixRelId = MqlUtil.mqlCommand(context,
                    "add connection $1 fromrel $2 to $3 select $4 dump",
                   RELATIONSHIP_JFVPMREFERENCE2CUSTOMERPARTS2PROJECT,
                   partCustomerRelId,
                   projectId,
                    "id");
            ContextUtil.commitTransaction(context);
        } catch (Exception e) {
            ContextUtil.abortTransaction(context);
            throw e;
        } finally {
            if (isPush) {
                ContextUtil.popContext(context);
            }
        }
    }

    /**
     * 获取零件关联的客户零件号/DB列表
     **
     * @param context 上下文
     * @param args 请求参数
     * @return MapList 客户零件号/DB列表
     * @throws Exception
     * @author caipan
     * @date 2026/7/10 19:30
     */
    public MapList getCustomerPartsDBList(Context context, String[] args) throws Exception {
        Map paramMap = JPO.unpackArgs(args);
        String objectId = getRequestValue(paramMap, "objectId");
        if (UIUtil.isNullOrEmpty(objectId)) {
            return new MapList();
        }
        StringList objectSelectList = JF_Util_mxJPO.basicBolistSel();
        StringList relSelectList = JF_Util_mxJPO.basicRellistSel();
        relSelectList.add(JF_PLMConstants_mxJPO.Select_Attr_JFCustomerPartNumber);
        relSelectList.add(JF_PLMConstants_mxJPO.Select_Attr_JFCustomerPartName);
        relSelectList.add(JF_PLMConstants_mxJPO.Select_Attr_JFCustomerPartRevision);
        relSelectList.add(JF_PLMConstants_mxJPO.Select_Attr_JF_DirectBuy);
        relSelectList.add(JF_PLMConstants_mxJPO.Select_Attr_JF_RelDescription);
        String customerPartsProjectIdSelect = "frommid[" + RELATIONSHIP_JFVPMREFERENCE2CUSTOMERPARTS2PROJECT + "].to.id";
        relSelectList.add(customerPartsProjectIdSelect);
        relSelectList.add("frommid[" + RELATIONSHIP_JFVPMREFERENCE2CUSTOMERPARTS2PROJECT + "].to.type");
        relSelectList.add("frommid[" + RELATIONSHIP_JFVPMREFERENCE2CUSTOMERPARTS2PROJECT + "].to.name");
        relSelectList.add("frommid[" + RELATIONSHIP_JFVPMREFERENCE2CUSTOMERPARTS2PROJECT + "].to.description");
        relSelectList.add("frommid[" + RELATIONSHIP_JFVPMREFERENCE2CUSTOMERPARTS2PROJECT + "].to.attribute[JF_ProjectCustomers]");
        DomainObject partObject = DomainObject.newInstance(context, objectId);
        MapList customerPartsDBList = partObject.getRelatedObjects(context,
                RELATIONSHIP_JFVPMREFERENCE2CUSTOMERPARTS,
                TYPE_JFCUSTOMERPARTS,
                objectSelectList,
                relSelectList,
                true,
                false,
                (short) 1,
                "",
                "",
                0);
        String belongProjectId = getPartBelongProject(context, new String[]{objectId, DomainConstants.SELECT_ID});
        //20260729 update by ljr 仅所属项目行在客户零件号为空且DirectBuy为non-DB时隐藏，其他项目的客户零件号/DB数据继续展示。
        for (int i = customerPartsDBList.size() - 1; i >= 0; i--) {
            Map customerPartsDBMap = (Map) customerPartsDBList.get(i);
            boolean isBelongProjectRow = isSameEBOMCustomerPartsDBProjectValue(
                    belongProjectId,
                    customerPartsDBMap.get(customerPartsProjectIdSelect));
            if (!isBelongProjectRow){
                continue;
            }
            String directBuy = UIUtil.getValue(customerPartsDBMap, JF_PLMConstants_mxJPO.Select_Attr_JF_DirectBuy);
            if (!JF_PLMConstants_mxJPO.ATTR_ATTR_JFDIRECT_BUY_RANGE_N.equalsIgnoreCase(directBuy.trim())) {
                continue;
            }
            String customerPartNumber = UIUtil.getValue(customerPartsDBMap, JF_PLMConstants_mxJPO.Select_Attr_JFCustomerPartNumber);
            if (UIUtil.isNotNullAndNotEmpty(customerPartNumber)) {
                continue;
            }
            customerPartsDBList.remove(i);
        }
        return customerPartsDBList;
    }

    /**
     * 删除零件关联的客户零件号/DB
     **
     * @param context 上下文
     * @param args 请求参数
     * @return void
     * @throws Exception 异常
     * @author LIUJR
     * @date 2026/7/10 19:30
     */
    public void removeCustomerPartsDB(Context context, String[] args) throws Exception {
        Map requestMap = JPO.unpackArgs(args);
        String objectId = getRequestValue(requestMap, "objectId");
        StringList customerPartIds = (StringList) requestMap.get("customerPartIds");

        // JPO只处理实际删除逻辑，页面可直接判断的选择数据、owner等校验在JSP中完成。
        if (UIUtil.isNullOrEmpty(objectId) || customerPartIds == null || customerPartIds.isEmpty()) {
            return;
        }

        // 查询当前零件下的客户零件号/DB关系，后续只处理JSP传入对象ID命中的关系。
        StringList objectSelectList = JF_Util_mxJPO.basicBolistSel();
        StringList relSelectList = JF_Util_mxJPO.basicRellistSel();
        DomainObject partObject = DomainObject.newInstance(context, objectId);
        MapList relatedList = partObject.getRelatedObjects(context,
                RELATIONSHIP_JFVPMREFERENCE2CUSTOMERPARTS,
                TYPE_JFCUSTOMERPARTS,
                objectSelectList,
                relSelectList,
                true,
                false,
                (short) 1,
                "",
                "",
                0);
        StringList disconnectRelIds = new StringList();
        StringList deleteObjectIds = new StringList();
        StringList infoList = new StringList();
        DomainObject object = DomainObject.newInstance(context);
        for (int i = 0; i < relatedList.size(); i++) {
            Map relatedMap = (Map) relatedList.get(i);
            String customerPartId = (String) relatedMap.get(DomainConstants.SELECT_ID);
            String relId = (String) relatedMap.get(DomainRelationship.SELECT_ID);
            if (customerPartIds.contains(customerPartId) && UIUtil.isNotNullAndNotEmpty(relId)) {
                //20260817 update by Codex 删除落库前再次校验所选行，防止绕过页面按钮权限删除所属项目数据。
                if (!hasCustomerPartsDBRowEditAccess(context, objectId, customerPartId, relId)) {
                    throw new Exception(EnoviaResourceBundle.getProperty(context,
                            "emxComponentsStringResource",
                            context.getLocale(),
                            "emxComponents.JFCustomerPartsDB.NoModifyAccess"));
                }
                disconnectRelIds.add(relId);
                object.setId(customerPartId);
                infoList = object.getInfoList(context, "from[" + RELATIONSHIP_JFVPMREFERENCE2CUSTOMERPARTS + "].to.id");
                if (infoList.size() == 1) {
                    deleteObjectIds.add(customerPartId);
                }
            }
        }
        if (disconnectRelIds.isEmpty()) {
            return;
        }
        ContextUtil.startTransaction(context, true);
        boolean isPush = false;
        try {
            ContextUtil.pushContext(context);
            isPush = true;
            for (int i = 0; i < disconnectRelIds.size(); i++) {
                String relId = (String) disconnectRelIds.get(i);
                // 先断开关系到项目的 fromrel 连接，再断开零件到客户零件号/DB对象的主关系。
                String projectRelId = MqlUtil.mqlCommand(context,
                        "print connection $1 select $2 dump",
                        relId,
                        "frommid[" + RELATIONSHIP_JFVPMREFERENCE2CUSTOMERPARTS2PROJECT + "].id");
                if (UIUtil.isNotNullAndNotEmpty(projectRelId)) {
                    DomainRelationship.disconnect(context, projectRelId);
                }
            }
            // 最后删除客户零件号/DB业务对象，确保关系和对象一起清理。
            DomainRelationship.disconnect(context, disconnectRelIds.toStringArray());
            DomainObject.deleteObjects(context, deleteObjectIds.toStringArray());
            ContextUtil.commitTransaction(context);
        } catch (Exception e) {
            ContextUtil.abortTransaction(context);
            throw e;
        } finally {
            if (isPush) {
                ContextUtil.popContext(context);
            }
        }
    }

    /**
     * 从提交参数中解析选中的零件ID
     **
     * @param requestMap 请求参数
     * @return String 零件ID
     * @author caipan
     * @date 2026/7/10 18:30
     */
    private String getSelectedPartId(Map requestMap) {
        String tableRowId = getRequestValue(requestMap, "emxTableRowId");
        if (UIUtil.isNullOrEmpty(tableRowId)) {
            tableRowId = getRequestValue(requestMap, "objectId");
        }
        if (UIUtil.isNullOrEmpty(tableRowId)) {
            return "";
        }
        StringList tableIds = FrameworkUtil.split(tableRowId, "|");
        if (tableIds.size() > 0) {
            String partId = tableIds.size() > 1 ? (String) tableIds.get(1) : (String) tableIds.get(0);
            StringList objectRelIds = FrameworkUtil.split(partId, ":");
            if (objectRelIds.size() > 0) {
                return (String) objectRelIds.get(0);
            }
            return partId;
        }
        return tableRowId;
    }

    /**
     * 获取请求参数中的单值
     **
     * @param requestMap 请求参数
     * @param key 参数名
     * @return String 参数值
     * @author caipan
     * @date 2026/7/10 18:30
     */
    private String getRequestValue(Map requestMap, String key) {
        if (requestMap == null) {
            return "";
        }
        Object value = requestMap.get(key);
        if (value instanceof String[]) {
            String[] values = (String[]) value;
            return values.length > 0 ? values[0] : "";
        }
        return value == null ? "" : String.valueOf(value);
    }
    String SELECT_WEIGHTTARGET = "attribute[JF_VPMReference.JF_WeightTarget]";
    String ATTRIBUTE_WEIGHTTARGET = "JF_VPMReference.JF_WeightTarget";
    String ATTRIBUTE_WEIGHT = "JF_VPMReference.JF_Weight";
    String RELATIONSHIP_SUBCLASS = "Subclass";
    String ATTRIBUTE_PARTITIONCN = "JF_VPMReference.JF_PartNameCN";
    String ATTRIBUTE_PARTITIONEN = "JF_VPMReference.JF_PartNameEN";
    String busWhere ="attribute[PLMReference.V_isLastVersion]=='TRUE'";

    public MapList getVPM(Context context, String[] args) throws Exception {
        MapList result = new MapList();
        try {
            Map paramMap = JPO.unpackArgs(args);
            String objId = (String) paramMap.get("objectId");
            Map map = new HashMap<>();
            map.put(DomainConstants.SELECT_ID, objId);
            result.add(map);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }


    public MapList getExpand(Context context, String[] args) {
        MapList result = new MapList();
        try {
            Map paramMap = JPO.unpackArgs(args);
            String objId = (String) paramMap.get("objectId");
            String expandLevel = (String) paramMap.get("expandLevel");
            String mode = (String) paramMap.get("Mode");
            if ("All".equals(expandLevel)) {
                expandLevel = "0";
            }
            DomainObject obj = DomainObject.newInstance(context, objId);
            StringList boSel = basicBolistSel();
            boSel.add(SELECT_WEIGTH);
            boSel.add(SELECT_WEIGHTTARGET);
            boSel.add("attribute["+ATTR_V_PART_NUMBER+"]");
            StringList relSel = new StringList();
            relSel.add("attribute[JF_VPMInstance.JF_Dosage]");
            relSel.add(DomainRelationship.SELECT_ID);
            relSel.add("from.id");
            MapList childPartList = obj.getRelatedObjects(context, RELATIONSHIP_VPMINSTANCE, TYPE_VPMREFERENCE,
                    boSel, relSel, false, true, Short.parseShort(expandLevel), "", "", 0);
            log.info("childPartList:{}",childPartList);
            if ("Merge".equals(mode)) {
                Map<String, Object> sameBomeMap = new HashMap<>();
                int level = Integer.MAX_VALUE;
                for (int i = 0; i < childPartList.size(); i++) {
                    Map child = (Map) childPartList.get(i);
                    String childId = (String) child.get("id");
                    String fromId = (String) child.get("from.id");
                    String dosageStr1 = (String) child.get("attribute[JF_VPMInstance.JF_Dosage]");
                    int childLevel = Integer.parseInt((String) child.get("level"));
                    if (childLevel > level) {
                        //childLevel大于level的,说明该层的父级已经重复，不用合并用量
                        continue;
                    }
                    level = Integer.MAX_VALUE;
                    if (!sameBomeMap.containsKey(fromId + childId)) {
                        sameBomeMap.put(fromId + childId, child);
                        result.add(child);
                    } else {
                        level = childLevel;
                        Map bommap = (Map) sameBomeMap.get(fromId + childId);
                        String dosageStr = (String) bommap.get("attribute[JF_VPMInstance.JF_Dosage]");
                        double dosage = Double.parseDouble(dosageStr) + Double.parseDouble(dosageStr1);
                        bommap.put("attribute[JF_VPMInstance.JF_Dosage]", String.valueOf(dosage));
                    }
                }
            } else {
                result = childPartList;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        log.info("result:{}",result);
        return result;
    }

    /**
     * @description: ebom实际重量列
     * @param: context
     * args
     * @return: java.util.Vector
     * @author JJS
     * @date: 14:55
     */
    public Vector getWeigthTarget(Context context, String[] args) throws Exception {
        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        MapList objectList = (MapList) programMap.get("objectList");
        Vector ValuesVector = new Vector(objectList.size());
        Iterator ListItr = objectList.iterator();
        Map map = (Map) ListItr.next();
        String id = (String) map.get(DomainConstants.SELECT_ID);
        DomainObject obj = DomainObject.newInstance(context, id);
        String weidth = obj.getAttributeValue(context, ATTRIBUTE_WEIGHT);
        String weightTarget = obj.getAttributeValue(context, ATTRIBUTE_WEIGHTTARGET);
        if (UIUtil.isNullOrEmpty(weightTarget)) {
            ValuesVector.add(weidth);
        } else {
            ValuesVector.add(weightTarget);
        }
        while (ListItr.hasNext()) {
            map = (Map) ListItr.next();
            weidth = (String) map.get(SELECT_WEIGTH);
            weightTarget = (String) map.get(SELECT_WEIGHTTARGET);
            if (UIUtil.isNullOrEmpty(weightTarget)) {
                ValuesVector.add(weidth);
            } else {
                ValuesVector.add(weightTarget);
            }
        }
        return ValuesVector;
    }

    /**
     * @description: 更新实际重量
     * @param: context
     * args
     * @return: void
     * @author JJS
     * @date: 15:31
     */
    public void updateWeigthTarget(Context context, String[] args) throws Exception {
        Map requsetMap = (Map) JPO.unpackArgs(args);
        Map paramMap = (Map) requsetMap.get("paramMap");
        Map columnMap = (Map) requsetMap.get("columnMap");
        String objectId = (String) paramMap.get("objectId");
        String newValue = (String) paramMap.get("New Value");
        DomainObject part = DomainObject.newInstance(context, objectId);
        //attName = attName.substring("attribute_".length());
        //part.setAttributeValue(context, attName, newValue);
        log.info("newValue=" + newValue);
        //
        part.setAttributeValue(context, ATTRIBUTE_WEIGHTTARGET, newValue);
    }

    /**
     * @description: 获取用量
     * @param: context
     * args
     * @return: java.util.Vector
     * @author JJS
     * @date: 13:58
     */
    public Vector getDosage(Context context, String[] args) throws Exception {
        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        MapList objectList = (MapList) programMap.get("objectList");
        Vector ValuesVector = new Vector(objectList.size());
        Iterator ListItr = objectList.iterator();
        while (ListItr.hasNext()) {
            Map map = (Map) ListItr.next();
          
            String dosage = (String) map.get("attribute[JF_VPMInstance.JF_Dosage]");
            if (UIUtil.isNullOrEmpty(dosage)) {
                ValuesVector.add("");
            } else {
                ValuesVector.add(dosage);
            }
        }
      
        return ValuesVector;
    }

    public StringList basicBolistSel() {
        StringList boSel = new StringList();
        boSel.add(DomainConstants.SELECT_ID);
        boSel.add(DomainConstants.SELECT_NAME);
        boSel.add(DomainConstants.SELECT_TYPE);
        boSel.add(DomainConstants.SELECT_REVISION);
        return boSel;
    }

    public Vector getProjectRel(Context context, String[] args) throws Exception {
        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        MapList objectList = (MapList) programMap.get("objectList");
        Vector result = new Vector();
        ContextUtil.pushContext(context);
        try {
            for (Object o : objectList) {
                Map objMap = (Map) o;
                String objId = (String) objMap.get("id");
                DomainObject obj = DomainObject.newInstance(context, objId);
                String Id = getPartBelongProject(context, new String[]{objId,SELECT_ID});
                String urlName = "";
                if(UIUtil.isNotNullAndNotEmpty(Id)) {
                    DomainObject project = DomainObject.newInstance(context, Id);
                    String projectName = project.getInfo(context, SELECT_DESCRIPTION);
                    String projectId = project.getInfo(context, SELECT_ID);
                    if (UIUtil.isNotNullAndNotEmpty(projectName)) {
                        urlName = JF_Util_mxJPO.buildHtml(context, projectId, projectName);
                    }
                }
                result.add(urlName);
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            ContextUtil.popContext(context);
        }
        return result;
    }

    /**
     * 根据属性修改table的属性
     *
     * @param context
     * @param args
     * @throws Exception
     */
    public void updateProjectRel(Context context, String[] args) throws Exception {
        Boolean isPush = false;
        try {
            Map requsetMap = (Map) JPO.unpackArgs(args);
            log.info("requsetMap:{}",requsetMap);
            Map paramMap = (Map) requsetMap.get("paramMap");
            Map columnMap = (Map) requsetMap.get("columnMap");
            String objectId = (String) paramMap.get("objectId");
            String newValue = (String) paramMap.get("New Value");
            DomainObject part = DomainObject.newInstance(context, objectId);
            Map settingMap = (Map) columnMap.get("settings");
            String attName = (String) settingMap.get("Admin Type");
            //attName = attName.substring("attribute_".length());
            //part.setAttributeValue(context, attName, newValue);
            log.info("newValue=" + newValue);
            //
            isPush = true;
            ContextUtil.pushContext(context);
            String relId = getPartBelongProject(context, new String[]{objectId,SELECT_RELATIONSHIP_ID});//拿到所属项目
            log.info("relId:{}",relId);
             if (UIUtil.isNotNullAndNotEmpty(relId)) {
                    //如果为Y的情况下不可以断开，所属项目都不是的情况下应该直接断开
//                    DomainRelationship ship = new DomainRelationship(relId);
             /*       String ATTR_JFZeroPart = ship.getAttributeValue(context, JF_PLMConstants_mxJPO.ATTR_JFZeroPart);//供货件标识
                    if(ATTR_JFZeroPart.equalsIgnoreCase("Y")){
                        ship.setAttributeValue(context, JF_PLMConstants_mxJPO.ATTR_JF_BelongPart, "N");
                    }else {*/
                        // 项目列Update Function执行时旧所属项目关系还存在，先同步客户零件号/DB主关系上的项目端关系。
                        syncEBOMCustomerPartsDBProjectRelationAfterProjectRelUpdate(context, objectId, relId, newValue);
                        DomainRelationship.disconnect(context, relId);
//                    }
            }

            if(UIUtil.isNotNullAndNotEmpty(newValue)) {
                StringList result = part.getInfoList(context, "to[JFProject2RootPart].from.physicalid");//MqlUtil.mqlCommand(context, false, "print bus '" + objectId + "' select to[JFProject2RootPart].from.id dump", true);
                log.info("result:{} {}",result,newValue);
                if (!result.contains(newValue)) {
                    DomainRelationship ship = part.addFromObject(context, new RelationshipType(RELATIONSHIP_JFPROJECT2ROOTPART), newValue);
                    ship.setAttributeValue(context, ATTR_JF_BelongPart, "Y");//设置所属项目
                    //设置零件上的所属项目
                    part.setAttributeValue(context, JF_PLMConstants_mxJPO.ATTR_JF_ProjectRel, DomainObject.newInstance(context, newValue).getDescription(context));
                }else{
                    //如果相等 就等于 他目前是供货件，但是不属于该项目需要拿到关系ID，设置成属于该项目
                    Map map = new HashMap();
                    map.put("relName",JF_PLMConstants_mxJPO.rel_JFProject2RootPart);
                    map.put("fromId",newValue);
                    map.put("toId",objectId);
                    String connectId = JF_PublicMethodClass_mxJPO.getTwoBusinessObjectConnId(context, JPO.packArgs(map));
                    if(UIUtil.isNotNullAndNotEmpty(connectId)) {
                        DomainRelationship ship = new DomainRelationship(connectId);
                        ship.setAttributeValue(context, ATTR_JF_BelongPart, "Y");//设置所属项目
                        part.setAttributeValue(context, JF_PLMConstants_mxJPO.ATTR_JF_ProjectRel, DomainObject.newInstance(context, newValue).getDescription(context));
                    }

                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (isPush) {
                ContextUtil.popContext(context);
            }
        }

    }

    /**
     * @description: 获取企业编码
     * @param: context
     * args
     * @return: void
     * @author JJS
     * @date: 15:38
     */
    public void setPartNumber(Context context, String[] args) throws Exception {
        Map paramMap = (Map) JPO.unpackArgs(args);
        StringList objectIds = (StringList) paramMap.get("objectIds");
        String type = (String) paramMap.get("type");
        if (type.length() > 1) {
            type = type.substring(0, 1);
        }
        MapList NumberGenerator = DomainObject.findObjects(context, "eService Number Generator", "*", "name=='" + TYPE_JFDATATRANSMISSION + type + "'", new StringList("id"));
        if (NumberGenerator.size() > 0 && UIUtil.isNotNullAndNotEmpty(type)) {
            JF_VPMT_mxJPO jfVpmtMxJPO = JF_VPMT_mxJPO.getInstance(context);
            for (int i = 0; i < objectIds.size(); i++) {
                DomainObject part = DomainObject.newInstance(context, objectIds.get(i));
                String partName = jfVpmtMxJPO.autoName(context, part, type);
                part.setAttributeValue(context, ATTRIBUTE_PARTNUMBER, partName);
                part.setAttributeValue(context, JF_PLMConstants_mxJPO.PLMEntity_V_Name, partName);
            }

        }
    }

    /**
     * @description: 采用异步方式获取eco的一级件和制造件
     * @param: context
     * args
     * @return: void
     * @author JJS
     * @date: 14:27
     */
    public void findBuyAndMakePart(Context context, String[] args) throws Exception {
        Job job = new Job("JF_VPMReferenceEBOM", "findBuyPartOneLevel", args);
        job.setTitle("find One Level Part and Make Part in ECO");
        job.createAndSubmit(context);
    }

    /**
     * @description: 异步卷积ecr整椅价格
     * @param: context
     * args
     * @return: void
     * @author JJS
     * @date: 17:01
     */
    public void convolutionChiarPrice(Context context, String[] args) throws Exception {
        Job job = new Job("JF_VPMReferenceEBOM", "convolutionReletedItem", args);
        job.setTitle("convolution ChairPart price in ECO");
        job.createAndSubmit(context);
    }

    /**
     * @description: 当ecr已经到APR, 会签任务驳回后全部重新提交后触发卷积
     * @param: context
     * args
     * @return: void
     * @author JJS
     * @date: 16:54
     */
    public void convolutionChiarPriceTask(Context context, String[] args) throws Exception {
        log.info("convolutionChiarPriceTask>>>>>" + args[0]);
        String taskId = (String) args[0];
        DomainObject task = DomainObject.newInstance(context, taskId);
        String type = task.getInfo(context, "type");
        log.info("type=" + type);
        //只考虑会签任务
        if (!"JF_SignTask".equals(type)) {
            return;
        }
        String crId = task.getInfo(context, "to[JFECR2Task].from.id");
        log.info("crId=" + crId);
        DomainObject cr = DomainObject.newInstance(context, crId);
        String crCurrent = cr.getInfo(context, "current");
        String strType = cr.getInfo(context, "type");
        log.info("crCurrent=" + crCurrent);
        //只有cr已经到APR状态
        if ("APR".equals(crCurrent)) {
            StringList boSel = JF_Util_mxJPO.basicBolistSel();
            MapList taskMapList = cr.getRelatedObjects(context, "JFECR2Task", "JF_SignTask", boSel, null, false, true,
                    (short) 1, "", "", 0);
            for (Object o : taskMapList) {
                Map objMap = (Map) o;
                String current = (String) objMap.get(DomainConstants.SELECT_CURRENT);
                String id = (String) objMap.get(DomainConstants.SELECT_ID);
                //不比较本身
                if (taskId.equals(id)) {
                    continue;
                }
                //只要有一个没完成就不卷积
                if (!"Review".equals(current)) {
                    return;
                }
            }
            // add by chenyan 新增NewECR卷积逻辑 2025/05/19
            if ("JFNewECR".equals(strType)){
                Job job = new Job("JF_NewECRService", "rollupNewECRRootPartPrice", new String[]{crId});
                job.setTitle("reconvolution ChairPart price in New ECR ");
                job.createAndSubmit(context);
            }else {
                Job job = new Job("JF_VPMReferenceEBOM", "convolutionReletedItem", new String[]{crId});
                job.setTitle("reconvolution ChairPart price in ECO");
                job.createAndSubmit(context);
            }
        }
    }

    /**
     * @description: 查询cr受影响buy件的一级件，关联一几件和buy件、cr的关系
     * @param: context
     * CRId
     * ProjectId
     * @return: void
     * @author JJS
     * @date: 10:59
     */
    public void findBuyPartOneLevel(Context context, String[] args) throws Exception {
        log.info("start findBuyPartOneLevel>>>>>{}", args[0]);
        String strECRId = args[0] ;
        ContextUtil.startTransaction(context, true);
        try {
            DomainObject cr = DomainObject.newInstance(context, args[0]);
            String crName = cr.getInfo(context, "name");
            JF_Util_mxJPO jfUtilMxJPO = new JF_Util_mxJPO();
            StringList bosel = jfUtilMxJPO.basicBolistSel();
            bosel.add(SELECT_LEVEL);
            bosel.add("attribute[XCADExtension.V_CADOrigin]");
            bosel.add(SELECT_ATTR_JFPartType);
            bosel.add(SELECT_ATTR_JF_ProcurementType);
            //获取当前ECR关联的项目的整椅件  JFProject2RootPart 关系属性 attribute[JFZeroPart]==Y
            StringList rootIds = getRootPart(context, cr, bosel);
            log.info("rootIds:{}",rootIds);
            //制造件里面的整椅件
            StringList newRootIds = cr.getInfoList(context, "from[" + RELATIONSHIP_JFECR2MANUFACTURING + "].to.id");
            //获取ECR关联的受影响对象
            MapList RelatedItemMapList = cr.getRelatedObjects(context, RELATIONSHIP_JFRELATEITEM, TYPE_VPMREFERENCE, bosel, null,
                    false, true, (short) 1, "", "", 0);
            //ECR已经关联的一级件
            StringList onelevelIds = cr.getInfoList(context, "from[" + RELATIONSHIP_JFECR2ONELEVELPART + "].to.id");
            String relwhere = "attribute[JFECRName]=='"+crName+"'";
            //查询类型是cativa5的父级
            for (int i = 0; i < RelatedItemMapList.size(); i++) {
                Map relatedItemMap = (Map) RelatedItemMapList.get(i);
                String relatedItemId = (String) relatedItemMap.get(DomainConstants.SELECT_ID);
                log.info("relatedItemId:{}",relatedItemId);
                String strPartType  = (String) relatedItemMap.get(SELECT_ATTR_JFPartType);
                String strProcurementType  = (String) relatedItemMap.get(SELECT_ATTR_JF_ProcurementType);
                log.info("当前受影响对象 strPartType:{}",strPartType);
                log.info("当前受影响对象 strProcurementType:{}",strProcurementType);
                //受影响项就是整椅
                if (rootIds.contains(relatedItemId)) {
                    if (!newRootIds.contains(relatedItemId)) {
                        //GX不应该关联到自制件清单
                        if (!"X".equals(strPartType)){
                            DomainRelationship rel = cr.addToObject(context, new RelationshipType(RELATIONSHIP_JFECR2MANUFACTURING), relatedItemId);//建立CR和头制造件关系
                            rel.setAttributeValue(context, ATTRIBUTE_JFWHOLECHAIR, "Y");//标识是整椅
                            newRootIds.add(relatedItemId);
                        }
                    }
                    continue;
                }
                String relatedName = (String) relatedItemMap.get(DomainConstants.SELECT_NAME);
                DomainObject relatedItem = DomainObject.newInstance(context, relatedItemId);
                String relatedType = relatedItem.getAttributeValue(context, "XCADExtension.V_CADOrigin");//判断受影响对象的类型 是V5 还是V6
                StringList selectRelList = JF_Util_mxJPO.basicRellistSel();
                selectRelList.add(SELECT_TO_ID);
                selectRelList.add(SELECT_FROM_ID);
                //受影响对象的所有父
                MapList CAD5MapList = relatedItem.getRelatedObjects(context, RELATIONSHIP_VPMINSTANCE, TYPE_VPMREFERENCE, bosel, selectRelList,
                        true, false, (short) 0, "", "", 0);//子查询父 ---查所有层级
                // add by chenyan 新增超级BOM 会存在找到多个整椅需要过滤切找到关联的最新整椅
                log.info("filter before CAD5MapList:{}",CAD5MapList);
                Set<MapList> rootPartSet = filterNewRevisionRootPart(context, CAD5MapList,rootIds);
                log.info("rootPartSet:{}",rootPartSet);
                //存在整椅不匹配
                if (rootPartSet.size() > 0){
                    for(MapList rootPartList : rootPartSet){
                        CAD5MapList = rootPartList ;
                        if (CAD5MapList != null && CAD5MapList.size() > 0){
                            //StringList oneLevel2partIds = relatedItem.getInfoList(context, "to[" + RELATIOSNHIP_JFONELEVELPART2PART + "].from.id");
                            StringList oneLevel2partIds = new StringList();//当前受影响对象本身关联的一级件
                            MapList oneLevel2partMapList = relatedItem.getRelatedObjects(context, RELATIOSNHIP_JFONELEVELPART2PART, TYPE_VPMREFERENCE, bosel, null,
                                    true, false, (short) 1, "", relwhere, 0);//查询当前受影响对象和一级件的关系

                            log.info("oneLevel2partMapList:{}",oneLevel2partMapList);
                            for(int x=0;x<oneLevel2partMapList.size();x++){
                                Map oneLevel2partMap = (Map) oneLevel2partMapList.get(x);
                                String oneLevel2partId = (String) oneLevel2partMap.get(DomainConstants.SELECT_ID);
                                oneLevel2partIds.add(oneLevel2partId);
                            }
                            int j = CAD5MapList.size() - 1;
                            boolean isOnelevelpart = false; //判断当前线路是否是在0级件下
                            boolean iscatv6 = false; //判断当前0级件是否是V6
                            //整椅id
                            String strRootPartId = "";
                            //从顶层开始往下
                            while (j >= 0) {
                                Map cad5Map = (Map) CAD5MapList.get(j);
                                int lev = Integer.parseInt((String) cad5Map.get(SELECT_LEVEL));
                                String cadId = (String) cad5Map.get(DomainConstants.SELECT_ID);
                                String cadName = (String) cad5Map.get(DomainConstants.SELECT_NAME);
                                String cadType = (String) cad5Map.get("attribute[XCADExtension.V_CADOrigin]");
                                if (rootIds.contains(cadId)) {
                                    strRootPartId = cadId ;
                                    if (!newRootIds.contains(cadId)) {
                                        boolean isConnection = JF_PublicMethodClass_mxJPO.checkTwoBusHasConnection(context,strECRId , cadId, RELATIONSHIP_JFECR2MANUFACTURING, false, true, "", "");
                                        if (!isConnection){
                                            DomainRelationship rel = cr.addToObject(context, new RelationshipType(RELATIONSHIP_JFECR2MANUFACTURING), cadId);
                                            rel.setAttributeValue(context, ATTRIBUTE_JFWHOLECHAIR, "Y");
                                        }
                                        newRootIds.add(cadId);
                                    }
                                    onelevelIds.add(cadId);  //加到一级件列表，防止整椅件也创建一级件
                                    isOnelevelpart = true;
                                    iscatv6 = !"CATIAV5".equalsIgnoreCase(cadType);
                                }
                                log.info("iscatv6:{}",iscatv6);
                                log.info("是否找到整椅:{}",isOnelevelpart);
                                //已经找到0级件，开始寻找1级件
                                if (isOnelevelpart) {//存在0级件-就找一级件，如果不存在0级件就直接跳过了
                                    if (!iscatv6) {
                                        //防止只有0级件会进入
                                        if(!(rootIds.contains(cadId))){
                                            log.info("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
                                            //0级件是v5的，他的下一级就是1级件
                                            connectOneLevelPart(context, cadId, cr, onelevelIds, oneLevel2partIds, relatedItem, crName,iscatv6,DomainObject.newInstance(context,strRootPartId));
                                            isOnelevelpart = false;
                                            j -= lev;
                                            log.info("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
                                            continue;
                                        }
                                    }
                                    //0级件是v6的，中间存在虚层，找到子件中第一个cad类型是CATIAV5的就是1级件
                                    if ("CATIAV5".equalsIgnoreCase(cadType)&&iscatv6) {
                                        log.info("！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！");
                                        connectOneLevelPart(context, cadId, cr, onelevelIds, oneLevel2partIds, relatedItem, crName,iscatv6,DomainObject.newInstance(context,strRootPartId));
                                        isOnelevelpart = false;
                                        j -= lev;
                                        log.info("！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！");
                                        continue;
                                    }
                                    //当前的层级为1并且是v5件，受影响项是v5件，说明受影响项就是骨架，发泡
                                    if(lev == 1){
                                        log.info("###########################################################################");
                                        isOnelevelpart = false;
                                        if ("CATIAV5".equalsIgnoreCase(relatedType) && !onelevelIds.contains(relatedItemId)) {
                                            boolean isConnection = JF_PublicMethodClass_mxJPO.checkTwoBusHasConnection(context,strECRId , relatedItemId, RELATIONSHIP_JFECR2ONELEVELPART, true, true, "", "");
                                            if (!isConnection){
                                                cr.addToObject(context, new RelationshipType(RELATIONSHIP_JFECR2ONELEVELPART), relatedItemId);
                                            }
                                            //制造件关联cr
                                            connectMakePart(context, relatedItem, DomainObject.newInstance(context,strRootPartId),iscatv6,crName);
                                            onelevelIds.add(relatedItemId);
                                        }
                                        log.info("###########################################################################");
                                    }
                                }else {
                                    //不存在于当前项目的整椅下当做一级件处理 不包含GX
                                    log.info("没有找到整椅---------------------------------------------------------------");
                                    if (!"X".equals(strPartType)){
//                                        if ((("make".equals(strProcurementType) && ("U".equalsIgnoreCase(strPartType) || "T".equalsIgnoreCase(strPartType)))  || "C".equals(strPartType))){
//                                            boolean isConnection = JF_PublicMethodClass_mxJPO.checkTwoBusHasConnection(context,strECRId , relatedItemId, RELATIONSHIP_JFECR2MANUFACTURING, true, true, "", "");
//                                            if (!isConnection){
//                                                cr.addToObject(context, new RelationshipType(RELATIONSHIP_JFECR2MANUFACTURING), relatedItemId);
//                                            }
//                                        }else if ("buy".equals(strProcurementType) || "ICO".equals(strProcurementType)){
                                        if ("buy".equals(strProcurementType) || "ICO".equals(strProcurementType)){
                                            boolean isConnection = JF_PublicMethodClass_mxJPO.checkTwoBusHasConnection(context,strECRId , relatedItemId, RELATIONSHIP_JFECR2ONELEVELPART, true, true, "", "");
                                            if (!isConnection){
                                                cr.addToObject(context, new RelationshipType(RELATIONSHIP_JFECR2ONELEVELPART), relatedItemId);
                                            }
                                        }
                                    }
                                    log.info("没有找到整椅---------------------------------------------------------------");
                                    //退出while 循环 执行下一个受影响项
                                    break;
                                }
                                j--;
                            }
                        }else {
                            if (!"X".equals(strPartType)){
//                                if ((("make".equals(strProcurementType) && ("U".equalsIgnoreCase(strPartType) || "T".equalsIgnoreCase(strPartType)))  || "C".equals(strPartType))){
//                                    boolean isConnection = JF_PublicMethodClass_mxJPO.checkTwoBusHasConnection(context,strECRId , relatedItemId, RELATIONSHIP_JFECR2MANUFACTURING, true, true, "", "");
//                                    if (!isConnection){
//                                        cr.addToObject(context, new RelationshipType(RELATIONSHIP_JFECR2MANUFACTURING), relatedItemId);
//                                    }
//                                }else if ("buy".equals(strProcurementType) || "ICO".equals(strProcurementType)){
                                if ("buy".equals(strProcurementType) || "ICO".equals(strProcurementType)){
                                    boolean isConnection = JF_PublicMethodClass_mxJPO.checkTwoBusHasConnection(context,strECRId , relatedItemId, RELATIONSHIP_JFECR2ONELEVELPART, true, true, "", "");
                                    if (!isConnection){
                                        cr.addToObject(context, new RelationshipType(RELATIONSHIP_JFECR2ONELEVELPART), relatedItemId);
                                    }
                                }
                            }
                        }

                    }
                }else {
                    //不存在于当前项目的整椅下当做一级件处理 不包含GX
                    log.info("不存在整椅---------------------------------------------------------------");
                    if (!"X".equals(strPartType)){
                        // 2025/04/08 chenyan 取消自制件清单对游离件的挂载
//                        if ((("make".equals(strProcurementType) && ("U".equalsIgnoreCase(strPartType) || "T".equalsIgnoreCase(strPartType)))  || "C".equals(strProcurementType))){
//                            boolean isConnection = JF_PublicMethodClass_mxJPO.checkTwoBusHasConnection(context,strECRId , relatedItemId, RELATIONSHIP_JFECR2MANUFACTURING, true, true, "", "");
//                            if (!isConnection){
//                                cr.addToObject(context, new RelationshipType(RELATIONSHIP_JFECR2MANUFACTURING), relatedItemId);
//                            }
//                        }else if ("buy".equals(strProcurementType) || "ICO".equals(strProcurementType)){
                        if ("buy".equals(strProcurementType) || "ICO".equals(strProcurementType)){
                            boolean isConnection = JF_PublicMethodClass_mxJPO.checkTwoBusHasConnection(context,strECRId , relatedItemId, RELATIONSHIP_JFECR2ONELEVELPART, true, true, "", "");
                            if (!isConnection){
                                cr.addToObject(context, new RelationshipType(RELATIONSHIP_JFECR2ONELEVELPART), relatedItemId);
                            }
                        }
                    }
                    log.info("没有找到整椅---------------------------------------------------------------");
                }

            }
            //add by ljr 20240801 生成会签任务
            JF_SignTask_mxJPO jfSignTaskMxJPO = new JF_SignTask_mxJPO();
            jfSignTaskMxJPO.GenerateCountersignatureTask(context, args);
            //end

            //add by lsa 20240805 生成ECO
            JF_ChangeExecutionECOSource_mxJPO ecoMxJPO = new JF_ChangeExecutionECOSource_mxJPO();
            ecoMxJPO.createECOObject(context, args);
            //end

            ContextUtil.commitTransaction(context);
        } catch (Exception e) {
            log.info(e.getMessage());
            e.printStackTrace();
            ContextUtil.abortTransaction(context);
        }
        log.info("end findBuyPartOneLevel>>>>{}", args[0]);
    }

    public void connectOneLevelPart(Context context, String cadId, DomainObject cr, StringList onelevelIds, StringList oneLevel2partIds, DomainObject relatedItem, String crName,boolean isv6,DomainObject root) throws Exception {
        log.info("cadId:{}",cadId);
        log.info("onelevelIds:{}",onelevelIds);
        log.info("oneLevel2partIds:{}",oneLevel2partIds);
        if (!onelevelIds.contains(cadId)) {
            boolean isConnection = JF_PublicMethodClass_mxJPO.checkTwoBusHasConnection(context,cr.getId(context) , cadId, RELATIONSHIP_JFECR2ONELEVELPART, false, true, "", "");
            if (!isConnection){
                cr.addToObject(context, new RelationshipType(RELATIONSHIP_JFECR2ONELEVELPART), cadId);
            }
            onelevelIds.add(cadId);
            DomainObject cad = DomainObject.newInstance(context, cadId);
            //判断是不是发泡，面套类型的如果是就和cr关联制造件关系
            connectMakePart(context, cad, root,isv6,crName);
            onelevelIds.add(cadId);
        }else {
            // add by chenyan 2024/11/29
            //防止 一级件已经关联导致 整椅和一级件关联不上
            DomainObject cad = DomainObject.newInstance(context, cadId);
            connectMakePart(context, cad, root,isv6,crName);
        }
        if (!oneLevel2partIds.contains(cadId)) {
            boolean isConnection = JF_PublicMethodClass_mxJPO.checkTwoBusHasConnection(context,relatedItem.getId(context) , cadId, RELATIOSNHIP_JFONELEVELPART2PART, false, true, "", "");
            if (!isConnection){
                DomainRelationship rel = relatedItem.addFromObject(context, new RelationshipType(RELATIOSNHIP_JFONELEVELPART2PART), cadId);
                log.info("rel:{}",rel);
                try {
                    ContextUtil.pushContext(context);
                    rel.setAttributeValue(context, ATTRIBUTE_JFECRNAME, crName);
                }finally {
                    ContextUtil.popContext(context);
                }
            }
            oneLevel2partIds.add(cadId);
        }
    }

    /**
     * @description: 关联面套/发泡，并且采购类型是自制件的一级件为制造件
     * @param: context
     * relatedItem
     * @return: void
     * @author JJS
     * @date: 9:45
     */
    public void connectMakePart(Context context, DomainObject relatedItem, DomainObject root,boolean isv6,String strECRName) throws Exception {
        log.info("---------------------------------------connectMakePart begin ----------------------------------------------");
        //判断是否已经关联
        String strRootId = root.getId(context);
        String strItemId = relatedItem.getId(context);
        String strRelWhere = JF_PublicMethodClass_mxJPO.buildStringInStrings("",SELECT_ATTRIBUTE_JFECRNAME,"=='",strECRName,"'");
        if(isv6) {
            StringList v6partTypes = relatedItem.getInfoList(context, "to[" + RELATIONSHIP_VPMINSTANCE + "].from.attribute[" + ATTRIBUTE_PARTTYPE + "]");
            String ProcurementType = relatedItem.getAttributeValue(context, ATTR_JF_ProcurementType);
            for (String v6partType : v6partTypes) {
                if (("U".equalsIgnoreCase(v6partType) || "T".equalsIgnoreCase(v6partType))
                        && "make".equals(ProcurementType)) {
                    boolean isConnection = JF_PublicMethodClass_mxJPO.checkTwoBusHasConnection(context, strRootId, strItemId, RELATIONSHIP_JFRootPart2OnePart, false, true, "", strRelWhere);
                    if (!isConnection){
                        DomainRelationship rel  =  DomainRelationship.connect(context,root,RELATIONSHIP_JFRootPart2OnePart,relatedItem);
//                        DomainRelationship rel = relatedItem.addToObject(context, new RelationshipType(RELATIONSHIP_JFRootPart2OnePart), root.getId(context));
                        try{
                            ContextUtil.pushContext(context);
                            rel.setAttributeValue(context,ATTRIBUTE_JFECRNAME,strECRName);
                        }finally {
                            ContextUtil.popContext(context);
                        }
                    }
                    break;
                }
            }
        }else{
            String partType = relatedItem.getAttributeValue(context,ATTRIBUTE_PARTTYPE);
            String ProcurementType = relatedItem.getAttributeValue(context, ATTR_JF_ProcurementType);
            if(("U".equalsIgnoreCase(partType) || "T".equalsIgnoreCase(partType))
                    && "make".equals(ProcurementType)){
                boolean isConnection = JF_PublicMethodClass_mxJPO.checkTwoBusHasConnection(context, strRootId, strItemId, RELATIONSHIP_JFRootPart2OnePart, false, true, "", strRelWhere);
                if (!isConnection){
                    DomainRelationship rel  =  DomainRelationship.connect(context,root,RELATIONSHIP_JFRootPart2OnePart,relatedItem);
//                    DomainRelationship rel = root.addToObject(context, new RelationshipType(RELATIONSHIP_JFECR2MANUFACTURING), relatedItem.getInfo(context, "id"));
                    try{
                        ContextUtil.pushContext(context);
                        rel.setAttributeValue(context,ATTRIBUTE_JFECRNAME,strECRName);
                    }finally {
                        ContextUtil.popContext(context);
                    }
                }
//                root.addToObject(context, new RelationshipType(RELATIONSHIP_JFRootPart2OnePart), relatedItem.getInfo(context, "id"));
            }
        }
        log.info("---------------------------------------connectMakePart end ----------------------------------------------");
    }

    /**
     * @description: ECR中卷积整椅下的受影响项的价格
     * @param: context
     * args
     * @return: void
     * @author JJS
     * @date: 10:44
     */
    public Map<String, double[]> convolutionReletedItem(Context context, String[] args) throws Exception {
        String objId = args[0];
        DomainObject ecr = DomainObject.newInstance(context, objId);


        StringList bosel = JF_Util_mxJPO.basicBolistSel();
        bosel.add(SELECT_LEVEL);
        StringList pricerel = new StringList();
        pricerel.add("attribute[" + ATTRIBUTE_JFCHANGEUNITPRICE + "]");
        pricerel.add("attribute[" + ATTRIBUTE_JFCHANGEUNITPRICEEXTERNAL + "]");
        StringList relsel = new StringList();
        relsel.add("attribute[JF_VPMInstance.JF_Dosage]");

        //获取整椅件
        StringList rootPartIds = getRootPart(context, ecr, bosel);
        Map<String, double[]> topLevelPart2Price = getoneLevelPart2Price(context, ecr, bosel, pricerel);
        Map<String, double[]> rootPart2Price = new HashMap<>();
        try {
            StringList v5Ids = new StringList();
            StringList convolutionOneLevelPartIds = new StringList(); //已经计算过的一级件
            //受影响项v5件
            MapList v5RelatedItemMapList = ecr.getRelatedObjects(context, RELATIONSHIP_JFRELATEITEM, TYPE_VPMREFERENCE, bosel, pricerel,
                    false, true, (short) 1, "", "", 0);
            for (int i = 0; i < v5RelatedItemMapList.size(); i++) {
                Map relatedItemMap = (Map) v5RelatedItemMapList.get(i);
                String relatedItemId = (String) relatedItemMap.get(DomainConstants.SELECT_ID);
                v5Ids.add(relatedItemId);
            }
            for (int i = 0; i < v5RelatedItemMapList.size(); i++) {
                Map relatedItemMap = (Map) v5RelatedItemMapList.get(i);
                String relatedItemId = (String) relatedItemMap.get(DomainConstants.SELECT_ID);
                String relatedItemName = (String) relatedItemMap.get(DomainConstants.SELECT_NAME);
                DomainObject relatedItem = DomainObject.newInstance(context, relatedItemId);
                double[] v5sum = new double[2];
                double[] prices = topLevelPart2Price.getOrDefault(relatedItemId, new double[]{0.0, 0.0});
                //价格没维护不用计算
//            if(prices[0]<=0.0){
//                continue;
//            }
                MapList VPMMaplist = relatedItem.getRelatedObjects(context, RELATIONSHIP_VPMINSTANCE, TYPE_VPMREFERENCE, bosel, relsel,
                        true, false, (short) 0, "", "", 0);
                boolean isconvolution = true;    //内部价格
                boolean exisconvolution = true;  //外部价格

                for (int j = 0; j < VPMMaplist.size(); j++) {
                    Map VPMMap = (Map) VPMMaplist.get(j);
                    String VPMId = (String) VPMMap.get(DomainConstants.SELECT_ID);
                    int level = Integer.parseInt((String) VPMMap.get(SELECT_LEVEL));
                    double count = Double.parseDouble((String) VPMMap.get("attribute[JF_VPMInstance.JF_Dosage]"));
                    if (level == 1) {
                        v5sum[0] = count * prices[0];
                        v5sum[1] = count * prices[1];
                        isconvolution = true;
                    }
                    //判断受影响项中是否有父级
                    if (v5Ids.contains(VPMId)) {
                        //清空卷积值，等对应的受影响项再卷，
                        double[] topLevelPrice = topLevelPart2Price.getOrDefault(VPMId, new double[]{0.0, 0.0});
                        if (topLevelPrice[0] > 0.0) {
                            v5sum[0] = 0.0;
                            isconvolution = false;
                        }
                        if (topLevelPrice[1] > 0.0) {
                            v5sum[1] = 0.0;
                            exisconvolution = false;
                        }
                    }
                    if (topLevelPart2Price.containsKey(VPMId)) {
                        //使用一级件维护的的价格
                        double[] topLevelPrice = topLevelPart2Price.get(VPMId);
                        //一级件维护了价格，并且是第一次出现
                        if (!convolutionOneLevelPartIds.contains(VPMId)) {
                            if (topLevelPrice[0] > 0.0 && isconvolution) {
                                v5sum[0] = topLevelPrice[0];
                            }
                            if (topLevelPrice[1] > 0.0 && exisconvolution) {
                                v5sum[1] = topLevelPrice[1];
                            }
                            convolutionOneLevelPartIds.add(VPMId);
                        }
                    }
                    if ((rootPartIds.contains(VPMId) || rootPartIds.contains(relatedItemId)) && isconvolution) {
                        double[] rootPrice = rootPart2Price.getOrDefault(VPMId, new double[]{0.0, 0.0});
                        rootPrice[0] += v5sum[0];
                        rootPrice[1] += v5sum[1];
                        rootPart2Price.put(VPMId, rootPrice);
                    }

                }

            }
            //把价格存在整椅和ecr的价格关系上
            for (Map.Entry<String, double[]> entry : rootPart2Price.entrySet()) {
                String rootId = entry.getKey();
                log.info(entry.getKey());
                double[] rootPrice = entry.getValue();
                log.info(rootPrice[0] + "," + rootPrice[1]);
                Map paramMap = new HashMap<>();
                paramMap.put("relName", RELATIONSHIP_JFECR2MAKEPARTPRICE);
                paramMap.put("fromId", objId);
                paramMap.put("toId", rootId);
                DomainRelationship connection = null;
                String relId = JF_PublicMethodClass_mxJPO.getTwoBusinessObjectConnId(context, JPO.packArgs(paramMap));
                log.info("relId===" + relId);
                if (UIUtil.isNullOrEmpty(relId)) {
                    connection = ecr.addToObject(context, new RelationshipType(RELATIONSHIP_JFECR2MAKEPARTPRICE), rootId);
                } else {
                    connection = DomainRelationship.newInstance(context, relId);
                    log.info(connection.toString());
                }
                Map<String, String> attMap = new HashMap();
                log.info("11>>" + rootPrice[0]);
                log.info("11>>" + rootPrice[1]);
                attMap.put(ATTRIBUTE_JFCHANGEWHOLESEATPRICE, String.valueOf(rootPrice[0]));
                attMap.put(ATTRIBUTE_JFCHANGEWHOLESEATPRICEEXTERNAL, String.valueOf(rootPrice[1]));
                connection.setAttributeValues(context, attMap);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return rootPart2Price;
    }

    /**
     * @description: 获取ecr相关的整椅件id
     * @param: context
     * ecr
     * @return: matrix.util.StringList
     * @author JJS
     * @date: 16:46
     */
    public StringList getRootPart(Context context, DomainObject ecr, StringList bosel) throws Exception {
        //临时整椅集合
        Set tempRootIdSet = new HashSet<String>();
        MapList projectMapList = ecr.getRelatedObjects(context, RELATIONSHIP_JFCHANGE2PROJECT, DomainConstants.TYPE_PROJECT_SPACE, bosel, null,
                false, true, (short) 1, "", "", 0);
        for (int i = 0; i < projectMapList.size(); i++) {
            Map projectMap = (Map) projectMapList.get(i);
            String projectId = (String) projectMap.get(DomainConstants.SELECT_ID);
            DomainObject project = DomainObject.newInstance(context, projectId);
            String relWhere = "attribute[JFZeroPart]==Y";
            MapList rootMaplist = project.getRelatedObjects(context, RELATIONSHIP_JFPROJECT2ROOTPART, TYPE_VPMREFERENCE, bosel, null,
                    false, true, (short) 1, "", relWhere, 0);
            for (int j = 0; j < rootMaplist.size(); j++) {
                Map rootMap = (Map) rootMaplist.get(j);
                String rootId = (String) rootMap.get(DomainConstants.SELECT_ID);
                String strPartType = (String) rootMap.get(SELECT_ATTR_JFPartType);
                // add by chenyan 如果整椅是GX 需要把GX下面的GC也加入进来
                tempRootIdSet.add(rootId);
                if ("X".equals(strPartType)){
                    DomainObject root = DomainObject.newInstance(context, rootId);
                    StringList sgcIdList = root.getInfoList(context, "form[VPMInstance].to.id");
                    tempRootIdSet.addAll(sgcIdList);
                }
            }
        }
        return StringList.create(tempRootIdSet);
    }

    /**
     * @description: 获取一级件维护的价格
     * @param: context
     * ecr
     * bosel
     * pricerel
     * @return: java.util.Map<java.lang.String, java.lang.Double>
     * @author JJS
     * @date: 14:16
     */
    public Map<String, double[]> getoneLevelPart2Price(Context context, DomainObject ecr, StringList bosel, StringList pricerel) throws Exception {
        Map<String, double[]> topLevelPart2Price = new HashMap<>(); //需要卷积的线路的最高级对象以及价格
        try {
            //获取ecr相关的一级件
            StringList oneLevelPartIds = ecr.getInfoList(context, "from[" + RELATIONSHIP_JFECR2ONELEVELPART + "].to.id");
            MapList oneLevelMapList = ecr.getRelatedObjects(context, RELATIONSHIP_JFECR2PARTPRICE, TYPE_VPMREFERENCE, bosel, pricerel,
                    false, true, (short) 1, DomainConstants.EMPTY_STRING, DomainConstants.EMPTY_STRING, 0);
            //过滤不需要计算的,
            for (int j = 0; j < oneLevelMapList.size(); j++) {
                Map onelMap = (Map) oneLevelMapList.get(j);
                String onelId = (String) onelMap.get(DomainConstants.SELECT_ID);
                String price = (String) onelMap.get("attribute[" + ATTRIBUTE_JFCHANGEUNITPRICE + "]");
                String exitPrice = (String) onelMap.get("attribute[" + ATTRIBUTE_JFCHANGEUNITPRICEEXTERNAL + "]"); //外部价格
                if (UIUtil.isNullOrEmpty(price)) {
                    price = "0.0";
                }
                if (UIUtil.isNullOrEmpty(exitPrice)) {
                    exitPrice = "0.0";
                }
                DomainObject onel = DomainObject.newInstance(context, onelId);
                topLevelPart2Price.put(onelId, new double[]{Double.parseDouble(price), Double.parseDouble(exitPrice)});
                oneLevelPartIds.remove(onelId);
            }
            for (String oneLevelPartId : oneLevelPartIds) {
                topLevelPart2Price.put(oneLevelPartId, new double[]{0.0, 0.0});
            }
            for (Map.Entry<String, double[]> entry : topLevelPart2Price.entrySet()) {
                log.info(entry.getKey());
                double[] rootPrice = entry.getValue();
                log.info(rootPrice[0] + "," + rootPrice[1]);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return topLevelPart2Price;
    }


    public void testOnelevel(Context context, String[] args) throws Exception {
        log.info("testonelevel>>>>>>");
        findBuyPartOneLevel(context, args);
    }

    public void testrool(Context context, String[] args) throws Exception {
        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("objectId", args[0]);
        convolutionReletedItem(context, JPO.packArgs(paramMap));
    }

    /**
     * @description: 获取零件得到所有子件
     * @param: context
     * args
     * @return: com.matrixone.apps.domain.util.MapList
     * @author JJS
     * @date: 11:23
     */
    public MapList getAllpart(Context context, String[] args) throws Exception {
        MapList mapList = new MapList();
        try {
            StringList allpartIds = new StringList();
            Map paramMap = JPO.unpackArgs(args);
            String objId = (String) paramMap.get("objectId");
            Map rootMap = new HashMap();
            rootMap.put("id",objId);
            mapList.add(rootMap);
            DomainObject object = DomainObject.newInstance(context, objId);
            StringList bosel = basicBolistSel();
            bosel.add("attribute[XCADExtension.V_CADOrigin]");
            MapList childmapList = object.getRelatedObjects(context, RELATIONSHIP_VPMINSTANCE, TYPE_VPMREFERENCE, bosel, null, false, true, (short) 0, "", "", 0);
            for (int j = 0; j < childmapList.size(); j++) {
                Map childMap = (Map) childmapList.get(j);
                String childId = (String) childMap.get(DomainConstants.SELECT_ID);
                String cadType = (String) childMap.get("attribute[XCADExtension.V_CADOrigin]");
                if (!allpartIds.contains(childId) && UIUtil.isNotNullAndNotEmpty(cadType)) {
                    Map<String, String> map = new HashMap<>();
                    map.put("id", childId);
                    mapList.add(map);
                    allpartIds.add(childId);
                }
            }
            mapList.sort(DomainConstants.SELECT_NAME, "descending", "string");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return mapList;
    }

    /**
     * @description: 获取项目相关联的整椅零件
     * @param: context
     * args
     * @return: com.matrixone.apps.domain.util.MapList
     * @author JJS
     * @date: 13:21
     */
    public MapList getZeroPart(Context context, String[] args) throws Exception {
        Map paramMap = JPO.unpackArgs(args);
        String objId = (String) paramMap.get("objectId");
        DomainObject object = DomainObject.newInstance(context, objId);
        StringList bosel = basicBolistSel();
        bosel.add(JF_PLMConstants_mxJPO.SELECT_ATTR_V_PART_NUMBER);
        StringList relSel = JF_Util_mxJPO.basicRellistSel();
        String relWhere = "attribute[JFZeroPart]==Y";
        MapList zeroPartMapList = object.getRelatedObjects(context, RELATIONSHIP_JFPROJECT2ROOTPART, TYPE_VPMREFERENCE, bosel, relSel, false, true, (short) 1, busWhere, relWhere, 0);
        return zeroPartMapList;
    }

    public MapList getGXpart(Context context, String[] args) throws Exception {
        Map paramMap = JPO.unpackArgs(args);
        String objId = (String) paramMap.get("objectId");
        DomainObject object = DomainObject.newInstance(context, objId);
        StringList bosel = basicBolistSel();
        bosel.add(JF_PLMConstants_mxJPO.SELECT_ATTR_V_PART_NUMBER);
        StringList relSel = JF_Util_mxJPO.basicRellistSel();
        String relWhere = "attribute[JFZeroPart]==Y";
        String strWhere  = busWhere + "&&attribute[JF_VPMReference.JF_PartType].value==X" ;
        MapList zeroPartMapList = object.getRelatedObjects(context, RELATIONSHIP_JFPROJECT2ROOTPART, TYPE_VPMREFERENCE, bosel, relSel, false, true, (short) 1, strWhere, relWhere, 0);
        return zeroPartMapList;
    }

    public MapList getGCpart(Context context, String[] args) throws Exception {
        Map paramMap = JPO.unpackArgs(args);
        String objId = (String) paramMap.get("objectId");
        DomainObject object = DomainObject.newInstance(context, objId);
        StringList bosel = basicBolistSel();
        bosel.add(JF_PLMConstants_mxJPO.SELECT_ATTR_V_PART_NUMBER);
        StringList relSel = JF_Util_mxJPO.basicRellistSel();
        String relWhere = "attribute[JFZeroPart]==Y";
        String strWhere  = busWhere + "&&attribute[JF_VPMReference.JF_PartType].value==C" ;
        MapList zeroPartMapList = object.getRelatedObjects(context, RELATIONSHIP_JFPROJECT2ROOTPART, TYPE_VPMREFERENCE, bosel, relSel, false, true, (short) 1, strWhere, relWhere, 0);
        return zeroPartMapList;
    }
    /**
     * @description: 零件列表页面只有连零件owner或者关联项目的owner可见
     * @param: context
     * args
     * @return: boolean
     * @author JJS
     * @date: 16:40
     */
    public boolean JFVPMPartListCmdAccess(Context context, String[] args) throws Exception {
        Map paramMap = JPO.unpackArgs(args);
        String objId = (String) paramMap.get("objectId");
        String loginer = context.getUser();
        String objOwner = "";
        StringList projectOwner = new StringList();
        try {
            ContextUtil.pushContext(context);
            DomainObject object = DomainObject.newInstance(context, objId);
             projectOwner = object.getInfoList(context, "to[" + RELATIONSHIP_JFPROJECT2ROOTPART + "].from.owner");
            log.info("projectOwner:{}", projectOwner);
             objOwner = (String) object.getInfo(context, "owner");
        }catch (Exception e){
        }finally {
            ContextUtil.popContext(context);
        }
        return objOwner.equalsIgnoreCase(loginer) || projectOwner.contains(loginer);
    }

    public StringList getGCPart(Context context, String[] args) throws Exception {
        StringList result = new StringList();
        try {
            String where = "attribute[JF_VPMReference.JF_PartType]==C";
            MapList CPartMapList = DomainObject.findObjects(context, TYPE_VPMREFERENCE, "*", where, new StringList("id"));
            log.info("CPartMapList=" + CPartMapList.size());
            for (int j = 0; j < CPartMapList.size(); j++) {
                Map CMap = (Map) CPartMapList.get(j);
                String cId = (String) CMap.get(DomainConstants.SELECT_ID);
                result.add(cId);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * @description: 设置ebom界面列表可修改范围
     * @param: context
     * args
     * @return: matrix.util.StringList
     * @author JJS
     * @date: 16:43
     */
    public StringList isEdit(Context context, String[] args) throws Exception {
        StringList result = new StringList();
        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        MapList objectList = (MapList) programMap.get("objectList");
        String loginer = context.getUser();
        for (Object o : objectList) {
            Map objMap = (Map) o;
            String objId = (String) objMap.get("id");
            DomainObject obj = DomainObject.newInstance(context, objId);
            String owner = obj.getInfo(context, "owner");
            String current = obj.getInfo(context, "current");
            if (("PRIVATE".equalsIgnoreCase(current) || "IN_WORK".equalsIgnoreCase(current))
                    && loginer.equalsIgnoreCase(owner)) {
                result.add("true");
            } else {
                result.add("false");
            }
        }
        return result;
    }

    /**
     * @description: ebom界面零件类型可编辑逻辑
     * @param: context
     * args
     * @return: matrix.util.StringList
     * @author JJS
     * @date: 18:09
     */
    public StringList isPartTypeEdit(Context context, String[] args) throws Exception {
        StringList result = new StringList();
        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        Map columnMap = (Map) programMap.get("columnMap");
        String strFieldName = (String) columnMap.get("name");
        boolean isSubPartFlag = "JF_PartSubType".equals(strFieldName);
        MapList objectList = (MapList) programMap.get("objectList");
        String loginer = context.getUser();
        for (Object o : objectList) {
            Map objMap = (Map) o;
            String objId = (String) objMap.get("id");
            DomainObject obj = DomainObject.newInstance(context, objId);
            String current = obj.getInfo(context, "current");
            String owner = obj.getInfo(context, "owner");
            String partNumber = obj.getAttributeValue(context, ATTRIBUTE_PARTNUMBER);
            String strPartType = obj.getAttributeValue(context, ATTRIBUTE_PARTTYPE);
            if (("PRIVATE".equalsIgnoreCase(current) || "IN_WORK".equalsIgnoreCase(current)) && UIUtil.isNullOrEmpty(partNumber)
                    && loginer.equalsIgnoreCase(owner)) {
                //新增零件子类型只有面套和电气件或者为空才可编辑
                    if(isSubPartFlag){
                        if ("T".equals(strPartType) || "E".equals(strPartType)){
                            result.add("true");
                        }else {
                            result.add("false");
                        }
                    }else {
                        result.add("true");
                    }
            } else {
                result.add("false");
            }
        }
        return result;
    }

    /**
     * @description: 零件清单的可修改逻辑
     * @param: context
     * args
     * @return: matrix.util.StringList
     * @author JJS
     * @date: 15:34
     */
    public StringList isEditPartList(Context context, String[] args) throws Exception {
        StringList result = new StringList();
        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        Map requestMap = (Map) programMap.get("requestMap");
        MapList objectList = (MapList) programMap.get("objectList");
        String objectId = (String) requestMap.get("objectId");
        DomainObject object = DomainObject.newInstance(context, objectId);
        StringList projectList = object.getInfoList(context, "to[" + RELATIONSHIP_JFPROJECT2ROOTPART + "].from.id");
        StringList manger = new StringList();
        for(int i=0;i<projectList.size();i++) {
            manger.add(JF_Util_mxJPO.getProjectManager(context,new String[]{projectList.get(i)}));
        }
        String loginer = context.getUser();
        for (Object o : objectList) {
            Map objMap = (Map) o;
            String objId = (String) objMap.get("id");
            DomainObject obj = DomainObject.newInstance(context, objId);
            String current = obj.getInfo(context, "current");
            String owner = obj.getInfo(context, "owner");
            if ((("PRIVATE".equalsIgnoreCase(current) || "IN_WORK".equalsIgnoreCase(current) || "FROZEN".equalsIgnoreCase(current)) &&
                    loginer.equalsIgnoreCase(owner))||
                    (manger.contains(loginer) && ("IN_WORK".equalsIgnoreCase(current) || "FROZEN".equalsIgnoreCase(current)))) {
                result.add("true");
            } else {
                result.add("false");
            }
        }
        return result;
    }
    /**
     * @description: 获取零件的变更来源
     * @param: context
     * args
     * @return: java.util.Vector<java.lang.String>
     * @author JJS
     * @date: 16:51
     */
    public Vector<String> getChangeSource(Context context, String[] args) throws Exception {
        log.info("getChangeSource>>>>>>>");
        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        MapList objectList = (MapList) programMap.get("objectList");
        Vector result = new Vector();
        try {
            for (Object o : objectList) {
                Map objMap = (Map) o;
                String objId = (String) objMap.get("id");
                DomainObject obj = DomainObject.newInstance(context, objId);
                String value = obj.getInfo(context, "to[JFRelateItem].attribute[JFChangeSource].value");
                result.add(value);
            }
            log.info("sfsff====" + result);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public void updateAttribute(Context context, String[] args) throws Exception {
        try {
            log.info("updateAttribute>>>>>>>");
            Map requsetMap = (Map) JPO.unpackArgs(args);
            Map paramMap = (Map) requsetMap.get("paramMap");
            Map columnMap = (Map) requsetMap.get("columnMap");
            String objectId = (String) paramMap.get("objectId");
            String newValue = (String) paramMap.get("New Value");
            DomainObject part = DomainObject.newInstance(context, objectId);
            Map settingMap = (Map) columnMap.get("settings");
            String attName = (String) settingMap.get("Admin Type");
            attName = attName.substring("attribute_".length());
            ContextUtil.pushContext(context);
            part.setAttributeValue(context, attName, newValue);
            ContextUtil.popContext(context);
            log.info("newValue=" + newValue);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * @description: 获取零件所有被引用的变更对象
     * @param: context
     * args
     * @return: java.util.Map
     * @author JJS
     * @date: 13:48
     */
    public MapList getVPMWhereUsed(Context context, String[] args) throws Exception {
        Map programMap = (Map) JPO.unpackArgs(args);
        String objId = (String) programMap.get("objectId");
        MapList changeMapList = new MapList();
        DomainObject obj = DomainObject.newInstance(context, objId);
        BusinessObjectList busList = obj.getMajorRevisions(context);
        for(int i=0;i<busList.size();i++){
            BusinessObject bus = busList.get(i);
            log.info("bus.getObjectId(context):{}",bus.getObjectId(context));
            MapList list = getChangeList(context,bus.getObjectId(context));
            //log.info("list:{}",list);
            if(list!=null){
                changeMapList.addAll(list);
            }
        }
        return changeMapList;
    }

    public MapList getChangeList(Context context,String objId) throws Exception{
        DomainObject obj = DomainObject.newInstance(context, objId);
        StringList boSel = JF_Util_mxJPO.basicBolistSel();
        String ecrId = obj.getAttributeValue(context, "JF_VPMReference.JF_ConnectECR");
        String drId =  obj.getAttributeValue(context, "JF_VPMReference.JF_ConnectDR");
        String hasSPR = obj.getInfo(context, "to[JFSPartsApplication2VPMReference]");
        String relName = "";
        relName = REL_JFDA2VPMREFERENCE;
        String current = obj.getInfo(context, SELECT_CURRENT);
        if(UIUtil.isNullOrEmpty(ecrId)&&!"RELEASED".equalsIgnoreCase(current)){
            relName = relName + "," + REL_JFRelateItem;
        }if(UIUtil.isNullOrEmpty(drId)){
            relName =relName + "," + "JFDR2VPMReference";
        }
        if ("TRUE".equalsIgnoreCase(hasSPR)) {
            relName =relName + "," + JF_PLMConstants_mxJPO.REL_JFS_PARTS_APPLICATION_2_VPM;
        }
        MapList changeMapList = obj.getRelatedObjects(context, relName,
                DomainConstants.QUERY_WILDCARD,
                boSel,
                null,
                true,
                false,
                (short) 1,
                DomainConstants.EMPTY_STRING,
                DomainConstants.EMPTY_STRING,
                0);

        if(UIUtil.isNotNullAndNotEmpty(drId)){
            BusinessObject bs = new BusinessObject(drId);
            if(bs.exists(context)) {
                obj.setId(drId);
                Map map = obj.getInfo(context, boSel);
                map.put("level", "1");
                changeMapList.add(map);
            }
        }
        if(UIUtil.isNotNullAndNotEmpty(ecrId)){
            BusinessObject bs = new BusinessObject(ecrId);
            if(bs.exists(context)) {
                obj.setId(ecrId);
                Map map = obj.getInfo(context, boSel);
                map.put("level", "1");
                changeMapList.add(map);
            }
        }
        StringList attrList = new StringList();
        attrList.add(SELECT_NAME);
        attrList.add(SELECT_REVISION);
        attrList.add("attribute[EnterpriseExtension.V_PartNumber]");
        obj.setId(objId);
        Map attrValue = obj.getInfo(context, attrList);
        String partNo =UIUtil.isNotNullAndNotEmpty(UIUtil.getValue(attrValue, "attribute[EnterpriseExtension.V_PartNumber]"))?
                UIUtil.getValue(attrValue, "attribute[EnterpriseExtension.V_PartNumber]"):UIUtil.getValue(attrValue,SELECT_NAME);
        partNo = partNo+"_"+UIUtil.getValue(attrValue, SELECT_REVISION);
        for(int i=0;i<changeMapList.size();i++) {
         Map map = (Map)changeMapList.get(i);
         map.put("partNo",partNo);
        }
        return changeMapList;
    }
    public Vector getPartNo(Context context, String[] args)
            throws Exception {
        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        MapList objectList = (MapList) programMap.get("objectList");
        Vector retVector = new Vector();
        if (objectList != null && objectList.size() > 0) {
            Map objectMap = null;
            String partNo="";
            for (int i = 0; i < objectList.size(); i++) {
                objectMap = (Map) objectList.get(i);
                 partNo = UIUtil.getValue(objectMap, "partNo");
                retVector.add(partNo);
            }
        }
        return retVector;
    }
    /**
     * @description: 更新用量
     * @param: context
     * args
     * @return: void
     * @author JJS
     * @date: 9:29
     */
    public void updateDosage(Context context, String[] args) throws Exception {
        Map programMap = (Map) JPO.unpackArgs(args);
        Map paramMap = (Map) programMap.get("paramMap");
        String relId = (String) paramMap.get("relId");
        String newValue = (String) paramMap.get("New Value");
        if(UIUtil.isNotNullAndNotEmpty(relId)) {
            DomainRelationship relationship = DomainRelationship.newInstance(context, relId);
            relationship.setAttributeValue(context, "JF_VPMInstance.JF_Dosage", newValue);
        }
    }

    /**
     * @description: 零件分类关系创建时同步分类库属性并更新缺图纸标识
     * @param: context
     * args
     * @return: void
     * @author CHENYAN
     * @date: 10:12
     */
    public void updatePartiton(Context context, String[] args) throws Exception {
        try {
            ContextUtil.pushContext(context);
            ContextUtil.startTransaction(context,true);
            log.info("updatePartiton>>>>>>>");
            String fromId = (String) args[0];
            String toId = (String) args[1];
            log.info("to>>>" + toId);
            DomainObject from = DomainObject.newInstance(context, fromId);
            DomainObject to = DomainObject.newInstance(context, toId);
            Map toInfoMap = to.getInfo(context, StringList.create(SELECT_TYPE,SELECT_ATTR_V_PART_NUMBER,SELECT_ATTR_JFPartType));
            String to_type = (String) toInfoMap.get( SELECT_TYPE);
            String strPartNum = (String) toInfoMap.get(SELECT_ATTR_V_PART_NUMBER);
            String strPartType = (String) toInfoMap.get(SELECT_ATTR_JFPartType);
            log.info("strPartNum:{}",strPartNum);
            StringList typeSelectList = JF_Util_mxJPO.basicBolistSel();
            typeSelectList.add(SELECT_DESCRIPTION);
            Map rootLibMap = JF_Library_mxJPO.getRootLibByClassId(context, typeSelectList, JF_Util_mxJPO.basicRellistSel(), from);
            String strLibTitle = (String) rootLibMap.get(SELECT_ATTRIBUTE_TITLE);
            String  strLibRange = strLibTitle.split("-")[0].substring(strLibTitle.split("-")[0].length() - 1);
            Properties properties = JF_Util_mxJPO.readPageObject(context, "JFJDConfig");
            String partitionLibraryTitle = (String) properties.get("partitionLibrary.ContainStandardTitle");
            log.info("toType>>" + to_type);
            log.info("strLibTitle>>>" + strLibTitle);
            log.info("partitionLibraryTitle>>>" + partitionLibraryTitle);
            if (!"VPMReference".equals(to_type) || !partitionLibraryTitle.contains(strLibTitle)) {
                return;
            }
            boolean notPromise = UIUtil.isNullOrEmpty(strPartNum) ;
            boolean partTypeAccess = UIUtil.isNullOrEmpty(strPartType);
            //平台创建零件只同步 JF_VPMReference_JF_ProcurementGroup

            //移除在平台创建的零件
            Map<String, String> attributeMap = new HashMap<>();
            Map classInfoMap =  from.getInfo(context, StringList.create(JF_PLMConstants_mxJPO.select_attr_JF_Code,SELECT_ATTRIBUTE_TITLE,SELECT_DESCRIPTION,SELECT_ATTR_JF_ProcurementGroup,SELECT_ATTR_JF_PartSubType,Select_Attr_JF_LibUnit));//是否整椅标识
            String iswhole =  (String) classInfoMap.get(JF_PLMConstants_mxJPO.select_attr_JF_Code);//是否整椅标识
            String strDetailCN =  (String) classInfoMap.get(SELECT_ATTRIBUTE_TITLE);
            String strDetailEN =  (String) classInfoMap.get(SELECT_DESCRIPTION);
            StringBuffer sbFullName = new StringBuffer();
            if (UIUtil.isNotNullAndNotEmpty(strDetailCN)){
                sbFullName.append(strDetailCN);
                if(UIUtil.isNotNullAndNotEmpty(strDetailEN)){
                    sbFullName.append("|");
                    sbFullName.append(strDetailEN);
                }
            }else {
                if(UIUtil.isNotNullAndNotEmpty(strDetailEN)){
                    sbFullName.append(strDetailEN);
                }
            }
            attributeMap.put(ATTR_JF_Detail_EN, strDetailEN);
            attributeMap.put(ATTR_JF_Detail_CN, strDetailCN);
//            attributeMap.put(PLMEntity_V_Name, sbFullName.toString());//暂时注释，刷完历史数据在放开
            if(!notPromise){

                attributeMap.put(PLMEntity_V_Name, strPartNum);
            }
            if (partTypeAccess&&notPromise){
                attributeMap.put(ATTR_JFPartType, strLibRange);
            }
            //库属性同步到零件属性
            String strProcurementGroup =  (String) classInfoMap.get(SELECT_ATTR_JF_ProcurementGroup);
            String strPartSubType =  (String) classInfoMap.get(SELECT_ATTR_JF_PartSubType);
            String JF_LibUnit =  (String) classInfoMap.get(Select_Attr_JF_LibUnit);
            attributeMap.put(ATTR_JF_VPMReference_JF_ProcurementGroup, strProcurementGroup);
            if(UIUtil.isNotNullAndNotEmpty(JF_LibUnit)) {//如果库分类的单位维护了值，需要同步到关联的零件的单位上面
                attributeMap.put(ATTR_JF_VPMReferenceJF_Unit, JF_LibUnit);
            }
            if (UIUtil.isNotNullAndNotEmpty(strPartSubType)){
                attributeMap.put(ATTR_JF_VPMReference_JF_PartSubType, strPartSubType);
            }
//            String partType = to.getInfo(context, JF_PLMConstants_mxJPO.SELECT_ATTR_JFPartType);
//            if(!partType.equalsIgnoreCase("C")&&!partType.equalsIgnoreCase("X")){
//                iswhole ="";
//            }
//            attributeMap.put(attr_JF_ISWholeChair, iswhole);
            log.info("attributeMap:{}",attributeMap);
            to.setAttributeValues(context, attributeMap);
            if (notPromise&&partTypeAccess&&UIUtil.isNotNullAndNotEmpty(strLibRange)){
                Map<String,Object> paramMap = new HashMap<>();
                paramMap.put("objectIds",StringList.create(toId));
                paramMap.put("type",strLibRange);
                JPO.invoke(context,"JF_VPMReferenceEBOM",new String[]{},"setPartNumber",JPO.packArgs(paramMap),void.class);
            }
            //20260903 update by caipan 更换或清空详细分类后统一重算缺图纸标识，详细分类为空时清空属性
            updatePartIsThereALackOfDrawings(context, toId);
            ContextUtil.commitTransaction(context);
            //如果零件关联了项目，并且标识是WholeChair，需要把关系同步到整椅上 add by caipan 20241202
//            if("WholeChair".equalsIgnoreCase(iswhole)){
//                StringList boSel = JF_Util_mxJPO.basicBolistSel();
//                StringList roSel = JF_Util_mxJPO.basicRellistSel();
//                MapList projectList = to.getRelatedObjects(context, "JFProject2RootPart",
//                        TYPE_PROJECT_SPACE,
//                        boSel,
//                        roSel,
//                        true,
//                        false,
//                        (short) 1,
//                        DomainConstants.EMPTY_STRING,
//                        DomainConstants.EMPTY_STRING,
//                        0);
//                //设置关系属性 JFZeroPart 为Y
//                for (int i=0;i<projectList.size();i++){
//                    Map temp = (Map)projectList.get(i);
//                    String relId = UIUtil.getValue(temp, DomainConstants.SELECT_RELATIONSHIP_ID);
//                    DomainRelationship ship = new DomainRelationship(relId);
//                    ship.setAttributeValue(context, "JFZeroPart", "Y");
//                }
//
//            }
        }catch (Exception e){
            ContextUtil.abortTransaction(context);
            throw new FrameworkException(e);
        }finally {
            ContextUtil.popContext(context);
        }
        log.info("------------------------------------ updatePartiton end------------------------------------------");
    }
    /**
     * @description: 查询出所有库节点
     * @param: context
	obj
     * @return: com.matrixone.apps.domain.util.MapList
     * @author JJS
     * @date:  10:27
     */
    public MapList findpartitionLibrary(Context context, DomainObject obj) throws Exception {
        StringList boSel = JF_Util_mxJPO.basicBolistSel();
        boSel.add("attribute[Title]");
        boSel.add(DomainConstants.SELECT_DESCRIPTION);
        return obj.getRelatedObjects(context, RELATIONSHIP_SUBCLASS, DomainConstants.QUERY_WILDCARD, boSel, null,
                true, false, (short) 0, "", "", 0);
    }

    public void disonelev(Context context, String[] args) throws Exception {
        DomainObject ecr = DomainObject.newInstance(context, args[0]);
        StringList relList = ecr.getInfoList(context,"from[JFECR2OnelevelPart].id");
        if(relList.size()>0){
            DomainRelationship.disconnect(context,relList.toStringArray());
        }
        StringList relList1 = ecr.getInfoList(context,"from[JFECR2Manufacturing].id");
        if(relList1.size()>0){
            DomainRelationship.disconnect(context,relList1.toStringArray());
        }
    }

/*
 * @description: 如果有企业编码就显示企业编码，没有就显示Name
 * @author: caipan
 * @date:
 * @param: * @param[1] context
 * @param[2] args
 * @return:
 **/
    public Vector getName(Context context, String[] args) throws Exception {
        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        MapList objectList = (MapList) programMap.get("objectList");
        Vector ValuesVector = new Vector(objectList.size());
        try {
            Iterator ListItr = objectList.iterator();
            while (ListItr.hasNext()) {
                Map map = (Map) ListItr.next();
//                log.info("map:{}", map);
                String V_PART_NUMBER = (String) map.get("attribute[" + ATTR_V_PART_NUMBER + "]");
                String strLevel = (String) map.get(SELECT_LEVEL);
                if ("0".equals(strLevel) && UIUtil.isNullOrEmpty(V_PART_NUMBER)){
                    String strLevel0Id = (String)map.get(SELECT_ID);
                    DomainObject root = DomainObject.newInstance(context,strLevel0Id);
                    String strPartNumber = root.getInfo(context, SELECT_ATTR_V_PART_NUMBER);
                    V_PART_NUMBER = strPartNumber;
                }
                String name = (String) map.get(DomainConstants.SELECT_NAME);
                if (UIUtil.isNullOrEmpty(V_PART_NUMBER)) {
                    ValuesVector.add(name);
                } else {
                    ValuesVector.add(V_PART_NUMBER);
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return ValuesVector;
    }

    public MapList getRootEBOM(Context context, String[] args)  throws Exception {
        MapList mlResult = new MapList();
        try {
            Map paramMap = JPO.unpackArgs(args);
            String objectId = (String)paramMap.get("objectId");
            log.info("paramMap:{}",paramMap);
            DomainObject obj = DomainObject.newInstance(context);
            obj.setId(objectId);
            StringList selList = JF_Util_mxJPO.basicBolistSel();
            selList.add("attribute["+JF_PLMConstants_mxJPO.ATTR_V_PART_NUMBER+"]");
            Map attValue= obj.getInfo(context, selList);
            String partNumber = UIUtil.getValue(attValue, "attribute["+JF_PLMConstants_mxJPO.ATTR_V_PART_NUMBER+"]");
            if(UIUtil.isNotNullAndNotEmpty(partNumber)){
                attValue.put("name", partNumber);

            }
            attValue.put("level", "2");
            attValue.put("expand", "true");
            attValue.put("emxExpandFilter", "2");
//            attValue.put("expandMultiLevelsJPO", "true");
            mlResult.add(attValue);

        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
        return mlResult;
    }


    public StringList isEditUsage(Context context, String[] args) throws Exception {
        log.info("isEditUsage start");
        StringList result = new StringList();
        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        MapList objectList = (MapList) programMap.get("objectList");
        Map columnMap = (Map) programMap.get("columnMap");
        String strFieldName = (String) columnMap.get("name");
        String loginer = context.getUser();
        for (Object o : objectList) {
            Map objMap = (Map) o;
            String objId = (String) objMap.get("id");
            log.info("objMap:{}",objMap);
            String parentId = (String) objMap.get("from.id");
            log.info("parentId:{} objId :{}",parentId,objId);
            if(UIUtil.isNotNullAndNotEmpty(parentId)) {
                DomainObject obj = DomainObject.newInstance(context, parentId);
                Map objMapInfo = obj.getInfo(context,StringList.create(SELECT_OWNER,SELECT_CURRENT,SELECT_ATTR_JF_VPMReferenceJF_Unit));
                String owner = (String) objMapInfo.get("owner");
                String current = (String)objMapInfo.get("current");
                if (("PRIVATE".equalsIgnoreCase(current) || "IN_WORK".equalsIgnoreCase(current))
                        && loginer.equalsIgnoreCase(owner)) {
                    String strJFUnit = (String)objMapInfo.get(SELECT_ATTR_JF_VPMReferenceJF_Unit);
                    //add by chenyan 2-25/04/07 新增如果单位是个时用量不允许编辑
                    if("Dosage".equals(strFieldName)) {
                        obj.setId(objId);
                        strJFUnit = obj.getInfo(context, SELECT_ATTR_JF_VPMReferenceJF_Unit);
                    }
                    if ("Dosage".equals(strFieldName) && "PC".equals(strJFUnit)){
                        result.add("false");
                    }else {
                        result.add("true");
                    }

                } else {
                    result.add("false");
                }
            }else{
                result.add("false");
            }
        }
        return result;
    }
    /**
    *
    *@description 如果存在多个整椅的话过滤出最新的整椅
    *@param context
	*@param partList
    *@return com.matrixone.apps.domain.util.MapList
    *@throws
    *@author CHENYAN
    *@date 2024/11/18 10:29
    */
    public Set<MapList> filterNewRevisionRootPart(Context context ,MapList partList ,StringList rootIdList){
        Set<MapList> res =  new HashSet<>();
        Map groupInfo  = (Map) partList.stream().collect(Collectors.groupingBy(m -> {
            Map partInfo = (Map) m;
            return Integer.valueOf((String) partInfo.get(SELECT_LEVEL));
        }));
        Integer maxKey = null ;
        MapList allRootMapList = new MapList() ;
        // 通过项目关联整椅排除非项目整椅节点获取实际使用整椅
        partList.stream().forEach(m ->{
            Map partMap = (Map) m;
            String strPartId = (String) partMap.get(SELECT_ID);
            //如果整椅包含直接加入
            if (rootIdList.contains(strPartId)){
                allRootMapList.add(partMap);
            }else {
                //如果不包含且改零件类型是GX 需要判断下层GC是否在项目关联整椅中
                String strPartType = (String) partMap.get(SELECT_ATTR_JFPartType);
                if ("X".equals(strPartType)){
                    String strToId = (String) partMap.get(SELECT_TO_ID);
                    if (rootIdList.contains(strToId)){
                        allRootMapList.add(partMap);
                    }
                }
            }
        });
        //找到整椅
        // 存在两种结构  GX-GC-GM-Part  GX标识为零件类型属性值为 X 当根节点为GX时 整椅为GX的下一个节点
        //            GC-GM-GM-Part  当根节点直接是GC是就是整椅
        //获取到根节点
//        for (Object oEntry : groupInfo.entrySet()) {
//            Map.Entry entry = (Map.Entry)oEntry ;
//            Object oKey = entry.getKey();
//            Integer iKey = (Integer) oKey;
//            if (maxKey == null || iKey.compareTo(maxKey) > 0) {
//                maxKey = iKey;
//                rootMapList = (List) entry.getValue();
//            }
//        }
        // add by chenyan 会出现多整椅的情况 先通过整椅的名称分组 然后在排序
        Map rootGroupMap = (Map) allRootMapList.stream().collect(Collectors.groupingBy(m -> {
            Map rootMap = (Map) m;
            return rootMap.get(SELECT_NAME);
        }));
        log.info("rootGroupMap:{}",rootGroupMap);
        Set keySet = rootGroupMap.entrySet();
        for (Object oEntry : keySet){
            MapList rootMapList = new MapList() ;
            Map.Entry entry = (Map.Entry)oEntry;
            List rootList = (List) entry.getValue();
            rootMapList.addAll(rootList);
            MapList filterPartList = partList;
            //过滤id
            Set<String> filterIdSet = new HashSet();

            if (rootMapList != null && rootMapList.size() > 0){
                //多个整椅的时候才会过滤
                if (rootMapList.size() > 1){
                    //按照最新版倒序
                    rootMapList.addSortKey(SELECT_REVISION,
                            ProgramCentralConstants.DESCENDING_SORT,
                            ProgramCentralConstants.SORTTYPE_STRING);
                    rootMapList.sort();
                }
                //最新版整椅
                Map rootMap  = (Map) rootMapList.get(0);
                // 判断是GX 还是GC
                String strPartType = (String) rootMap.get(SELECT_ATTR_JFPartType);
                log.info("strPartType:{}",strPartType);
                String strRootId = (String) rootMap.get(SELECT_ID);
                String strToId = (String) rootMap.get(SELECT_TO_ID);
                maxKey = Integer.valueOf((String) rootMap.get(SELECT_LEVEL));
                //真实整椅件id
                String strRealRootId = "";
                // GX 时 整椅是下一级  不为GX时当前就是整椅
                if ("X".equalsIgnoreCase(strPartType)){
                    filterIdSet.add(strToId);
                    strRealRootId = strToId ;
                }else {
                    filterIdSet.add(strRootId);
                    strRealRootId = strRootId ;
                }
                log.info("filterIdSet:{}",filterIdSet);
                //依次减level 拿to端id 加入到 filterIdList
                while (maxKey > 0){
                    if (groupInfo.containsKey(maxKey)){
                        List partInfoList = (List) groupInfo.get(maxKey);
                        for (int i = 0; i < partInfoList.size(); i++) {
                            Map partInfo = (Map) partInfoList.get(i);
                            String strObjectId = (String) partInfo.get(SELECT_ID);
                            String strRelToId = (String) partInfo.get(SELECT_TO_ID);
                            if (filterIdSet.contains(strObjectId)) {
                                filterIdSet.add(strRelToId);
                            }
                        }
                    }
                    maxKey-- ;
                }
                String strFinalStrRealRootId = strRealRootId;
                filterPartList = (MapList)partList.stream().filter(m -> {
                    Map partInfo = (Map) m;
                    String strObjectId = (String) partInfo.get(SELECT_ID);
                    if (strObjectId.equals(strFinalStrRealRootId)){
                        partInfo.put("isRoot",Boolean.TRUE);
                    }
                    return filterIdSet.contains(strObjectId);
                }).collect(Collectors.toCollection(MapList::new));
                res.add(filterPartList);
            };
        }

        return  res ;
    }
    /*
     * @description: Trigger 废弃 数模和项目的create Trigger，如果数模有整椅标识，就设置关系上也有整椅标识
     * @author: caipan
     * @date:
     * @param: * @param[1] context
     * @param[2] args
     * @return:
     **/
    public void setJFZeroPart(Context context,String[] args){
        log.info("setJFZeroPart");
        String toId = args[1];
        String fromId = args[0];
        String relId = args[3];
        try{
            ContextUtil.pushContext(context);
            DomainObject toObj = DomainObject.newInstance(context,toId);
            log.info("toId:{}",toId);
            String iswholeChair = toObj.getInfo(context, JF_PLMConstants_mxJPO.select_attr_JF_ISWholeChair);
             relId = toObj.getInfo(context, "to[" + RELATIONSHIP_JFPROJECT2ROOTPART + "].id");
            String partType = toObj.getInfo(context, JF_PLMConstants_mxJPO.SELECT_ATTR_JFPartType);
            toObj.setAttributeValue(context, JF_PLMConstants_mxJPO.ATTR_JF_ProjectRel, DomainObject.newInstance(context, fromId).getDescription(context));
            log.info("relId:{}",relId);
            log.info("iswholeChair:{}",iswholeChair);
          /*  if("WholeChair".equalsIgnoreCase(iswholeChair)&&(partType.equalsIgnoreCase("C")||partType.equalsIgnoreCase("X"))){
                DomainRelationship ship = new DomainRelationship(relId);
                ship.setAttributeValue(context, "JFZeroPart", "Y");
            }*/

        }catch (Exception e){
            e.printStackTrace();
        }finally {
            try {
                ContextUtil.popContext(context);
            } catch (FrameworkException e) {
                throw new RuntimeException(e);
            }
        }
    }
    /**
     * @description: 查询cr受影响buy件的一级件，关联一几件和buy件、cr的关系
     * @param: context
     * CRId
     * ProjectId
     * @return: void
     * @author JJS
     * @date: 10:59
     */
    public void test(Context context, String[] args) throws Exception {
        DomainObject relatedItem = DomainObject.newInstance(context, "14585.59252.12589.38640");
        StringList bosel = JF_Util_mxJPO.basicBolistSel();
        bosel.add(SELECT_ATTR_JFPartType);
        StringList selectRelList = JF_Util_mxJPO.basicRellistSel();
        selectRelList.add(SELECT_TO_ID);
        selectRelList.add(SELECT_FROM_ID);
        //受影响对象的所有父
        MapList CAD5MapList = relatedItem.getRelatedObjects(context, RELATIONSHIP_VPMINSTANCE, TYPE_VPMREFERENCE, bosel, selectRelList,
                true, false, (short) 0, "", "", 0);//子查询父 ---查所有层级
        // add by chenyan 新增超级BOM 会存在找到多个整椅需要过滤切找到关联的最新整椅
        log.info("filter before CAD5MapList:{}",CAD5MapList);
//        CAD5MapList = filterNewRevisionRootPart(context, CAD5MapList);
        log.info("filter after CAD5MapList:{}",CAD5MapList);
    }
    public MapList getVPMList(Context context, String[] args)throws Exception{
        Map requestMap = JPO.unpackArgs(args);
        String objectId = (String)requestMap.get("objectId");
        String strSelectedTable = (String)requestMap.get("selectedTable");
        log.info("strSelectedTable:{}",strSelectedTable);
        DomainObject obj = DomainObject.newInstance(context);
        MapList mapList ;
        obj.setId(objectId);
        if (strSelectedTable.contains("JFEBOMCostingTable")){
            StringList typeSelectList = JF_Util_mxJPO.basicBolistSel();
            typeSelectList.add(SELECT_ATTR_V_PART_NUMBER);
            typeSelectList.add("from[JFVPMReference2CostAnalysis]");
            typeSelectList.add("from[JFVPMReference2CostAnalysis].to.attribute[JFRollUpCost]");
            typeSelectList.add("from[JFVPMReference2CostAnalysis].to.attribute[JFActualcCost]");
            StringList rellistSel = JF_Util_mxJPO.basicRellistSel();
            rellistSel.add(SELECT_ATTR_JF_Dosage);
            mapList = obj.getRelatedObjects(context,  REL_Instance, // relationship pattern
                    TYPE_VPMReference,                                    // object pattern
                    typeSelectList,                            // object selects
                    rellistSel, // relationship selects
                    false,                                        // to direction
                    true,                                        // from direction
                    (short) 0,                                    // recursion level
                    "",                // object where clause
                    "",
                    (short) 0);
        }else {
           /* String strExpandLevel = (String) requestMap.get("expandLevel");
            short nExpandLevel = ProgramCentralUtil.getExpandLevel(strExpandLevel);
            StringList boSel = basicBolistSel();
            boSel.add(SELECT_WEIGTH);
            boSel.add(SELECT_WEIGHTTARGET);
            boSel.add("attribute["+ATTR_V_PART_NUMBER+"]");
            StringList relSel = new StringList();
            relSel.add("attribute[JF_VPMInstance.JF_Dosage]");
            relSel.add(DomainRelationship.SELECT_ID);
            relSel.add("from.id");
            mapList = obj.getRelatedObjects(context, RELATIONSHIP_VPMINSTANCE, TYPE_VPMREFERENCE,
                    boSel, relSel, false, true, nExpandLevel, "", "", 0);*/
            mapList = getExpand(context, args);
        }
        return mapList;
    }

    /*
     * @description:标准件工程师才可以看到标准件类型
     * @author: caipan
     * @date: 2025/1/17 15:29:12
     * @param: * @param[1] context
     * @param[2] args
     * @return:
     **/
    public Map getSubTypeRange(Context context, String[] args) throws Exception {
        HashMap<Object, Object> res = new HashMap<>();
        StringList ranges = FrameworkUtil.getRanges(context, JF_PLMConstants_mxJPO.ATTR_JFPartType);
        //是否标准件工程师
        Map requestMap = new HashMap();
        requestMap.put("roleName", "JfStandardAdmin");
        requestMap.put("userName", context.getUser());
       boolean flag = JF_Util_mxJPO.isIncludeRole(context, JPO.packArgs(requestMap));
       if(!flag){
           ranges.remove("F");
       }
        ranges.remove("");
        log.info("ranges{}:",ranges);
           StringList nlsRanges = EnoviaResourceBundle.getAttrRangeI18NStringList(context, JF_PLMConstants_mxJPO.ATTR_JFPartType, ranges, context.getLocale().toString());
           res.put("field_choices", ranges);
           res.put("field_display_choices", nlsRanges);

        return res;
    }

    /**
    *
    *@description TODO
    *@param context
	*@param args
    *@return java.util.Map
    *@throws
    *@author CHENYAN
    *@date 2025/4/11 15:05
    */
    public Map getReloadSubTypeRange(Context context, String[] args) throws Exception {
        Map argsMap = (Map) JPO.unpackArgs(args);
        //标识是否前台重载
        boolean isFlushFlag = false ;
        Map columnValuesMap  = null ;
        if (argsMap.containsKey("columnValues")) {
            isFlushFlag = true ;
            columnValuesMap = (Map) argsMap.get("columnValues");
        }
        String strPartType = "" ;
        if (isFlushFlag){
            strPartType = (String) columnValuesMap.get("PartType");
        }
        log.info("columnValuesMap{}:",columnValuesMap);
        log.info("strPartType{}:",strPartType);
        HashMap<Object, Object> res = new HashMap<>();
        StringList ranges = FrameworkUtil.getRanges(context, "JF_VPMReference.JF_PartSubType");
        log.info("ranges000{}:",ranges);
        if ("T".equals(strPartType)){
            ranges = StringList.create("T01","T02","T03","T04","T05","T06");
        }else if ("E".equals(strPartType)){
            ranges = StringList.create("E01","E02");
        }
        ranges = ranges.stream().filter(s->UIUtil.isNotNullAndNotEmpty(s)).collect(Collectors.toCollection(StringList::new));
        log.info("ranges{}:",ranges);
        log.info("isFlushFlag:{}",isFlushFlag);
        StringList nlsRanges = EnoviaResourceBundle.getAttrRangeI18NStringList(context, "JF_VPMReference.JF_PartSubType", ranges, context.getLocale().toString());
        ranges.add("");
        nlsRanges.add("");
        log.info("nlsRanges{}:",nlsRanges);
        if (isFlushFlag){
            res.put("RangeValues", ranges);
            res.put("RangeDisplayValue", nlsRanges);
        }else {
            res.put("field_choices", ranges);
            res.put("field_display_choices", nlsRanges);
        }
        return res;
    }
    /**
    *
    *@description 获取最新发布版整椅
    *@param context
	*@param args
    *@return com.matrixone.apps.domain.util.MapList
    *@throws
    *@author CHENYAN
    *@date 2025/2/19 14:09
    */
    public MapList getLastRevisionZeroPart(Context context, String[] args) throws Exception {
        Map paramMap = JPO.unpackArgs(args);
        String objId = (String) paramMap.get("objectId");
        DomainObject object = DomainObject.newInstance(context, objId);
        StringList bosel = JF_Util_mxJPO.basicBolistSel();
        bosel.add(JF_PLMConstants_mxJPO.SELECT_ATTR_V_PART_NUMBER);
        //限制最新版发布状态
        String strBusWhere = JF_PublicMethodClass_mxJPO.buildStringInStrings(busWhere, "&&current==RELEASED");
        StringList relSel = JF_Util_mxJPO.basicRellistSel();
        String relWhere = "attribute[JFZeroPart]==Y";
        MapList zeroPartMapList = object.getRelatedObjects(context, RELATIONSHIP_JFPROJECT2ROOTPART, TYPE_VPMREFERENCE, bosel, relSel, false, true, (short) 1, strBusWhere, relWhere, 0);
        return zeroPartMapList;
    }
    /*
     * @description:显示最新发布版本
     * @author: caipan
     * @date: 2025/4/9 15:46:14
     * @param: * @param[1] context
     * @param[2] args
     * @return:
     **/
    public Vector getLasterRelase(Context context ,String[] args) throws Exception{
        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        MapList objectList = (MapList) programMap.get("objectList");
        Vector retVector = new Vector();
        boolean ispush = false;
        try {
            ContextUtil.pushContext(context);
            ispush = true;
            if (objectList != null && objectList.size() > 0) {
                Map objectMap = null;
                DomainObject domainObject = DomainObject.newInstance(context);
                String displayName = "";
                for (int i = 0; i < objectList.size(); i++) {
                    objectMap = (Map) objectList.get(i);
                    String objectId = (String) objectMap.get("id");
                    String lastId = JF_Util_mxJPO.getLastReleasedMajorid(context, objectId);
                    domainObject.setId(lastId);
                    if (UIUtil.isNullOrEmpty(lastId)) {
                        displayName = "";
                    } else {
                        displayName = FrameworkUtil.split(domainObject.getInfo(context, "revision"), ".").get(0);
                    }
                    retVector.add(displayName);
                }
            }
        }catch (Exception e){
            log.error(e.getMessage());
        }finally {
            if(ispush) {
                ContextUtil.popContext(context);
            }
        }
        return retVector;

    }

    /**
     * 显示零件关联的图纸
     * @param context
     * @param args
     * @return
     * @throws Exception
     */
    public Vector getVPMRConnectionDrawing(Context context, String[] args) throws Exception {
        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        MapList objectList = (MapList) programMap.get("objectList");
        Vector result = new Vector();
        ContextUtil.pushContext(context);
        try {
            for (Object o : objectList) {
                Map objMap = (Map) o;
                String objId = (String) objMap.get("id");
                DomainObject obj = DomainObject.newInstance(context, objId);
                MapList documentMapList = obj.getRelatedObjects(context, "Reference Document", "Document", StringList.create("id","name","attribute[Title]"), new StringList(),
                        false, true, (short) 1, "attribute[JF_DocumentType].value==Drawing", "", 0);
                MapList drawingMapList = obj.getRelatedObjects(context, "XCADBaseDependency", "Drawing", StringList.create("id","attribute[PLMEntity.V_Name]"), new StringList(),
                        true, false, (short) 1, "", "", 0);
                String urlName = "";
                StringList urlList = new StringList();
                for (Object o1 : documentMapList) {
                    Map map = (Map) o1;
                    String id = UIUtil.getValue(map, "id");
                    String name = UIUtil.getValue(map, "attribute[Title]");
                    urlList.add("<a href=\"JavaScript:emxTableColumnLinkClick('../common/emxTree.jsp?mode=popup&amp;objectId="
                            + id + "', '700', '600', 'false', 'popup', '')\" >" + name
                            + "</a>");
                }
                for (Object o1 : drawingMapList) {
                    Map map = (Map) o1;
                    String id = UIUtil.getValue(map, "id");
                    String V_Name = UIUtil.getValue(map, "attribute[PLMEntity.V_Name]");
                    urlList.add("<a href=\"JavaScript:emxTableColumnLinkClick('../common/emxTree.jsp?mode=popup&amp;objectId="
                            + id + "', '700', '600', 'false', 'popup', '')\" >" + V_Name
                            + "</a>");
                }
                result.add(String.join("<br/>",urlList));
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            ContextUtil.popContext(context);
        }
        return result;
    }

    public Vector getVPMRConnectionDrawingCurrent(Context context, String[] args) throws Exception {
        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        MapList objectList = (MapList) programMap.get("objectList");
        Vector result = new Vector();
        ContextUtil.pushContext(context);
        try {
            for (Object o : objectList) {
                Map objMap = (Map) o;
                String objId = (String) objMap.get("id");
                DomainObject obj = DomainObject.newInstance(context, objId);
                MapList documentMapList = obj.getRelatedObjects(context, "Reference Document", "Document", StringList.create("id","name","current"), new StringList(),
                        false, true, (short) 1, "attribute[JF_DocumentType].value==Drawing", "", 0);
                MapList drawingMapList = obj.getRelatedObjects(context, "XCADBaseDependency", "Drawing", StringList.create("id","attribute[PLMEntity.V_Name]","current"), new StringList(),
                        true, false, (short) 1, "", "", 0);
                StringList urlList = new StringList();
                for (Object o1 : documentMapList) {
                    Map map = (Map) o1;
                    String current = UIUtil.getValue(map, "current");
                    urlList.add(current);
                }
                for (Object o1 : drawingMapList) {
                    Map map = (Map) o1;
                    String current = UIUtil.getValue(map, "current");
                    urlList.add(current);
                }
                result.add(String.join("<br/>",urlList));
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            ContextUtil.popContext(context);
        }
        return result;
    }

    public Vector getVPMRConnectionAttachments(Context context, String[] args) throws Exception {
        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        MapList objectList = (MapList) programMap.get("objectList");
        Vector result = new Vector();
        ContextUtil.pushContext(context);
        try {
            for (Object o : objectList) {
                Map objMap = (Map) o;
                String objId = (String) objMap.get("id");
                DomainObject obj = DomainObject.newInstance(context, objId);
                MapList documentMapList = obj.getRelatedObjects(context, "Reference Document", "Document", StringList.create("id","name"), new StringList(),
                        false, true, (short) 1, "attribute[JF_DocumentType].value!=Drawing", "", 0);
                StringList urlList = new StringList();
                for (Object o1 : documentMapList) {
                    Map map = (Map) o1;
                    String id = UIUtil.getValue(map, "id");
                    String name = UIUtil.getValue(map, "name");
                    urlList.add("<a href=\"JavaScript:emxTableColumnLinkClick('../common/emxTree.jsp?mode=popup&amp;objectId="
                            + id + "', '700', '600', 'false', 'popup', '')\" >" + name
                            + "</a>");
                }
                result.add(String.join("<br/>",urlList));
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            ContextUtil.popContext(context);
        }
        return result;
    }


    public boolean isShowMerge(Context context,String[] args) throws Exception{
        log.info("isShowMerge start");
        Map request = JPO.unpackArgs(args);
        String mode = (String)request.get("Mode");
        log.info("request:{}",mode);
        if(UIUtil.isNotNullAndNotEmpty(mode)) {
            if (mode.equalsIgnoreCase("Merge")) {
                return false;
            }
        }
        log.info("isShowMerge end");
        return true;
    }
    public boolean isShowExpand(Context context,String[] args) throws Exception{
        log.info("isShowExpand start");
        Map request = JPO.unpackArgs(args);
        String mode = (String)request.get("Mode");
        log.info("request:{}",mode);
        if(UIUtil.isNotNullAndNotEmpty(mode)) {
            if (mode.equalsIgnoreCase("Merge")) {
                return true;
            }
        }
        return false;
    }

    /*
     * @description:物理产品升版的时候，触发关联上一个版本的关联的JFProject2ColorGroup(项目和零件的关系)
     *  并且把颜色分组等信息保存下来
     * @author: caipan
     * @date: 2025/5/27 15:16:34
     * @param: * @param[1] context
     * @param[2] args
     * @return:
     **/
    public void connJFProject2ColorGroup(Context context,String[] args) throws Exception{
        String oldObject = args[0];
        String newObject = args[1];

        DomainObject oldObj = DomainObject.newInstance(context,oldObject);
        DomainObject newObj = DomainObject.newInstance(context,newObject);
        String oldType = oldObj.getInfo(context, SELECT_TYPE);
        StringList selList = JF_Util_mxJPO.basicBolistSel();
        StringList relList = JF_Util_mxJPO.basicRellistSel();
        relList.add("attribute[JF_ColorGroupName]");
        relList.add("attribute[JF_ColorMatrixName]");
        if(oldType.equals("VPMReference")){
            log.info("oldObject:{},newObject:{}",oldObject,newObject);
        //查询项目
            try {
                ContextUtil.pushContext(context);
                MapList list = oldObj.getRelatedObjects(context,
                        JF_PLMConstants_mxJPO.rel_JFProject2ColorGroup, //pattern to match relationships
                        TYPE_PROJECT_SPACE, //pattern to match types
                        selList, //the eMatrix StringList object that holds the list of select statement pertaining to Business Obejcts.
                        relList, //the eMatrix StringList object that holds the list of select statement pertaining to Relationships.
                        true, //get To relationships
                        false, //get From relationships
                        (short) 1, //the number of levels to expand, 0 equals expand all.
                        EMPTY_STRING, //where clause to apply to objects, can be empty ""
                        null, //where clause to apply to relationship, can be empty ""
                        (short) 0); //limit
                RelationshipType relationshipType = new RelationshipType(JF_PLMConstants_mxJPO.rel_JFProject2ColorGroup);
                for (int i = 0; i < list.size(); i++) {
                    Map map = (Map) list.get(i);
                    String projectId = UIUtil.getValue(map, SELECT_ID);
                    String groupName = UIUtil.getValue(map, "attribute[JF_ColorGroupName]");
                    String ColorMatrixName = UIUtil.getValue(map, "attribute[JF_ColorMatrixName]");
                    DomainRelationship ship = newObj.addFromObject(context, relationshipType, projectId);
                    ship.setAttributeValue(context, "JF_ColorGroupName", groupName);
                    ship.setAttributeValue(context, "JF_ColorMatrixName", ColorMatrixName);
                }
            }catch (Exception e){
                e.printStackTrace();
            }finally {
                ContextUtil.popContext(context);
            }
        }
    }
    /*
     * @description:添加整椅/移除整椅按钮是否显示
     * @author: caipan
     * @date: 2025/6/13 09:33:24
     * @param: * @param[1] context
     * @param[2] args
     * @return:
     **/
    public boolean isShowAddWholeChair(Context context,String[] args) throws Exception{
        Map request = JPO.unpackArgs(args);
        log.info("request:{}",request);
        String objectId = (String)request.get("objectId");
        DomainObject project = DomainObject.newInstance(context,objectId);
        StringList relList = JF_Util_mxJPO.basicRellistSel();
        relList.add("attribute[Project Role]");
        String where = "attribute[Project Role]=='Project manager' || attribute[Project Role]=='Chair manager'";
        MapList personList = project.getRelatedObjects(context,
                RELATIONSHIP_MEMBER, //pattern to match relationships
                TYPE_PERSON, //pattern to match types
                JF_Util_mxJPO.basicBolistSel(), //the eMatrix StringList object that holds the list of select statement pertaining to Business Obejcts.
                relList, //the eMatrix StringList object that holds the list of select statement pertaining to Relationships.
                false, //get To relationships
                true, //get From relationships
                (short) 1, //the number of levels to expand, 0 equals expand all.
                EMPTY_STRING, //where clause to apply to objects, can be empty ""
                where, //where clause to apply to relationship, can be empty ""
                (short) 0); //limit
        String contextUser = context.getUser();
        log.info("objectId:{},{}",objectId,personList);
        Set oidSet = (Set) personList.stream().map(m -> {
            Map info = (Map) m;
            return info.get(SELECT_NAME);
        }).collect(Collectors.toSet());
        if(oidSet.contains(contextUser)||project.getOwner(context).equals(contextUser)){
            return true;
        }
        return false;
    }


    /**
    *
    *@description 递归卷积价格
    *@param context
	*@param args
    *@return matrix.util.StringList
    *@throws
    *@author CHENYAN
    *@date 2025/7/1 14:36
    */

    public StringList getRollUpCostingPrice(Context context , String[] args) throws Exception{
        log.info("--------------------------- getRollUpCostingPrice begin ---------------------------------------------------");
        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        MapList objectList = (MapList) programMap.get("objectList");
        //根据level分组
        Map groupMap = (Map) objectList.stream().collect(Collectors.groupingBy(m -> {
            Map partMap = (Map) m;
            return partMap.get(SELECT_LEVEL);
        }));
        Set keySet = groupMap.keySet();
        List<String> sortedList = new ArrayList<>(keySet);
        sortedList.sort(Comparator.comparingInt(Integer::parseInt));
        if (sortedList.size() > 0){
            String strRootLevel = sortedList.get(0);
            List rootList = (List) groupMap.get(strRootLevel);
            for (int i = 0; i < rootList.size(); i++) {
                // 使用 tableMenu=JFEBOMTableMenu 会调用两次 第一次渲染根节点以下的节点，一次渲染根节点
                Map rootMap = (Map) objectList.get(i);
                recursionRollUpPrice(context,rootMap);
            }
        }
        StringList res = new StringList();
        log.info("objectList:{}",objectList.size());
        for (int i = 0; i < objectList.size(); i++) {
            Map partMap = (Map) objectList.get(i);
            String strPrice = "";
            if (partMap.containsKey("JFSumActualcCost")){
                strPrice = (String) partMap.get("JFSumActualcCost");
            }
            res.add(strPrice);
        }
        log.info("--------------------------- getRollUpCostingPrice end ---------------------------------------------------");
        return res;
    }
    public void recursionRollUpPrice(Context context ,Map partMap)throws Exception{
        String strLevel = (String) partMap.get(SELECT_LEVEL);
        if ("0".equals(strLevel)){
            //零件节点需要重新查询有OOTB调用
            String strPartId = (String) partMap.get(SELECT_ID);
            DomainObject part = DomainObject.newInstance(context,strPartId);
            Map rootMap = part.getInfo(context, StringList.create("from[JFVPMReference2CostAnalysis]", "from[JFVPMReference2CostAnalysis].to.attribute[JFRollUpCost]", "from[JFVPMReference2CostAnalysis].to.attribute[JFActualcCost]"));
            partMap.putAll(rootMap);
        }
        String dosage = (String) partMap.get(SELECT_ATTR_JF_Dosage);
        if (UIUtil.isNullOrEmpty(dosage) || "0.0".equalsIgnoreCase(dosage) || "0".equalsIgnoreCase(dosage)) {
            dosage = "1";
        }
        double dosageValue = Double.parseDouble(dosage);
        String strHasCreateFlag = (String) partMap.get("from[JFVPMReference2CostAnalysis]");
        String strRollPrice = (String) partMap.get("from[JFVPMReference2CostAnalysis].to.attribute[JFRollUpCost]");
        String strActualPrice = (String) partMap.get("from[JFVPMReference2CostAnalysis].to.attribute[JFActualcCost]");
        //当前节点价格
        String strPrice = "0.0";
        boolean isCreate = "TRUE".equalsIgnoreCase(strHasCreateFlag);
        if (isCreate){
            //实际成本不为空使用实际成本 、为空使用自身卷积成本
            if (UIUtil.isNotNullAndNotEmpty(strActualPrice)){
                strPrice = strActualPrice;
            }else {
                if (UIUtil.isNotNullAndNotEmpty(strRollPrice)){
//                    strPrice = strRollPrice;
                    //卷积成本  * 用量  update by ljr 20260402
                    double dosageValue1 = Double.parseDouble(strRollPrice);
                    strPrice = String.valueOf(dosageValue * dosageValue1);
                }
            }
        }
        if (partMap.containsKey("children")){
            List sunList = (List) partMap.get("children");
            BigDecimal sum = new BigDecimal( 0);
            for (int i = 0; i < sunList.size(); i++) {
                Map sunMap = (Map) sunList.get(i);
                recursionRollUpPrice(context,sunMap);
                //累加下一层级
                if (isCreate){
                    String strSumPrice = (String) sunMap.get("JFSumActualcCost");
                    if (UIUtil.isNotNullAndNotEmpty(strSumPrice)){
                        sum = sum.add(new BigDecimal(strSumPrice));
                    }
                }
            }
            //累加当前节点
            if (isCreate){
                strPrice = sum.add(new BigDecimal(strPrice)).toString();
            }
        }
        if (isCreate){
            partMap.put("JFSumActualcCost",strPrice);
        }
    }
    /*
     * @description:获取零件的所属项目
     * @author: caipan
     * @date: 2025/7/18 13:54:08
     * @param: * @param[1] context
     * @param[2] id
     * @return:
     **/
    public String getPartBelongProject(Context context,String[] args) throws Exception{
        StringList selList = JF_Util_mxJPO.basicBolistSel();
        StringList relList =JF_Util_mxJPO.basicRellistSel();
        relList.add(JF_PLMConstants_mxJPO.ATTR_JF_BelongPart);
        relList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JFZeroPart);
        DomainObject partObj = DomainObject.newInstance(context,args[0]);
        String key= SELECT_ID;
        if(args.length==2) {
             key = args[1];
        }

        String where =SELECT_ATTR_JF_BelongPart +" == Y";
       MapList list =  partObj.getRelatedObjects(context,
                RELATIONSHIP_JFPROJECT2ROOTPART, //pattern to match relationships
                TYPE_PROJECT_SPACE, //pattern to match types
                selList, //the eMatrix StringList object that holds the list of select statement pertaining to Business Obejcts.
                relList, //the eMatrix StringList object that holds the list of select statement pertaining to Relationships.
                true, //get To relationships
                false, //get From relationships
                (short) 1, //the number of levels to expand, 0 equals expand all.
                EMPTY_STRING, //where clause to apply to objects, can be empty ""
               where, //where clause to apply to relationship, can be empty ""
                (short) 1); //limit
        StringList projectList = JF_Util_mxJPO.mapList2StringList(list, key);
        if(projectList.size()>0){
            return projectList.get(0);
        }else {
            return "";
        }
    }

    /*
     * @description:获取零件是供货件的项目
     * @author: caipan
     * @date: 2025/7/18 13:55:11
     * @param: * @param[1] context
     * @param[2] id
     * @return:
     **/
    public StringList getPartZeroProject(Context context,String id)throws Exception{
        StringList selList = JF_Util_mxJPO.basicBolistSel();
        StringList relList =JF_Util_mxJPO.basicRellistSel();
        relList.add(JF_PLMConstants_mxJPO.ATTR_JF_BelongPart);
        relList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JFZeroPart);
        DomainObject partObj = DomainObject.newInstance(context,id);
        String where =SELECT_ATTR_JFZeroPart +" == Y";
        MapList list =  partObj.getRelatedObjects(context,
                RELATIONSHIP_JFPROJECT2ROOTPART, //pattern to match relationships
                TYPE_PROJECT_SPACE, //pattern to match types
                selList, //the eMatrix StringList object that holds the list of select statement pertaining to Business Obejcts.
                relList, //the eMatrix StringList object that holds the list of select statement pertaining to Relationships.
                true, //get To relationships
                false, //get From relationships
                (short) 1, //the number of levels to expand, 0 equals expand all.
                EMPTY_STRING, //where clause to apply to objects, can be empty ""
                where, //where clause to apply to relationship, can be empty ""
                (short) 0); //limit
        return JF_Util_mxJPO.mapList2StringList(list, SELECT_ID);

    }
    /**
    *
    *@description 重载便于JPO调用
    *@param context
	*@param args
    *@return matrix.util.StringList
    *@throws
    *@author CHENYAN
    *@date 2025/8/11 16:26
    */
    public StringList getPartZeroProject(Context context,String[] args)throws Exception{
        String strPartId = args[0];
        return getPartZeroProject(context,strPartId);
    }
    /*
     * @description:零件的所有版本和项目进行供货件关联
     *   如果不存在就新建关联，如果存在就直接设置关系属性
     * @author: caipan
     * @date: 2025/7/18 15:42:15
     * @param: * @param[1] context
     * @param[2] args
     * @return:
     **/
    public void JFProject2RootPartConnect(Context context,String[] args) throws Exception {
        Map requestMap = JPO.unpackArgs(args);
        try {
            DomainObject doObj;
            String partId = UIUtil.getValue(requestMap, "partId");
            String projectId = UIUtil.getValue(requestMap, "projectId");
            DomainObject projectObj = DomainObject.newInstance(context,projectId);
            StringList partList = projectObj.getInfoList(context,"from[JFProject2RootPart].to.id");//项目关联的零件
            DomainObject partObj = DomainObject.newInstance(context, partId);
            BusinessObjectList majorRevisionsBusObjList = partObj.getMajorRevisions(context);
            for (int i = 0; i < majorRevisionsBusObjList.size(); i++) {
                doObj = (DomainObject) majorRevisionsBusObjList.get(i);
                String id = doObj.getInfo(context, SELECT_ID);
                if(!partList.contains(id)){//新建关系
                    DomainRelationship rel = projectObj.addToObject(context, new RelationshipType("JFProject2RootPart"), id);
                    rel.setAttributeValue(context, "JFZeroPart", "Y");
                    rel.setAttributeValue(context, "JF_BelongPart", "N");//默认为Y，所以改成N
                }else{//已经存在关系，那就只需要设置关系属性JFZeroPart 为Y
                    String mql = "print connection bus " + projectId + " to " + id + " relationship JFProject2RootPart select id dump";
                    String relId = MqlUtil.mqlCommand(context, mql);
                    if(UIUtil.isNotNullAndNotEmpty(relId)) {
                        DomainRelationship rel = DomainRelationship.newInstance(context, relId);
                        rel.setAttributeValue(context, "JFZeroPart", "Y");
                    }
                }
                doObj.setAttributeValue(context, "JF_VPMReference.JF_ISWholeChair", "WholeChair");
            }

        }catch (Exception e){
            e.printStackTrace();
        }finally {
        }
    }
    /*
     * @description:零件的所有版本和项目进行供货件断开
     *  设置关系属性为N
     * @author: caipan
     * @date: 2025/7/18 15:42:15
     * @param: * @param[1] context
     * @param[2] args
     * @return:
     **/
    public void JFProject2RootPartDelete(Context context,String[] args) throws Exception {
        Map requestMap = JPO.unpackArgs(args);
        try {
            ContextUtil.pushContext(context);
            DomainObject doObj;
            String partId = UIUtil.getValue(requestMap, "partId");
            String projectId = UIUtil.getValue(requestMap, "projectId");
            DomainObject projectObj = DomainObject.newInstance(context,projectId);
            DomainObject partObj = DomainObject.newInstance(context, partId);
            BusinessObjectList majorRevisionsBusObjList = partObj.getMajorRevisions(context);
            Map request= new HashMap();
            request.put("relName","JFProject2RootPart");
            request.put("fromId",projectId);

            for (int i = 0; i < majorRevisionsBusObjList.size(); i++) {
                doObj = (DomainObject) majorRevisionsBusObjList.get(i);
                String id = doObj.getInfo(context, SELECT_ID);
                //如果没有关系的话，直接报错执行这句mql语句
                request.put("toId",id);
               Boolean flag = JF_PublicMethodClass_mxJPO.getTwoBusinessObject(context,JPO.packArgs(request));
               if(flag) {
                   String mql = "print connection bus " + projectId + " to " + id + " relationship JFProject2RootPart select id dump";
                   String relId = MqlUtil.mqlCommand(context, mql);
                   //如果所属项目也为N的话，需要把关系断开
                   if (UIUtil.isNotNullAndNotEmpty(relId)) {
                       DomainRelationship rel = DomainRelationship.newInstance(context, relId);
                       String JF_BelongPart = rel.getAttributeValue(context, "JF_BelongPart");
                       if (JF_BelongPart.equalsIgnoreCase("N")) {
                           //断开关系
                           DomainRelationship.disconnect(context, relId);
                       } else {
                           rel.setAttributeValue(context, "JFZeroPart", "N");
                       }
                   }
               }
                doObj.setAttributeValue(context, "JF_VPMReference.JF_ISWholeChair", "");
            }
            //设置状态未确认
            projectObj.setAttributeValue(context,JF_PLMConstants_mxJPO.ATTR_JF_SupplyAffirm,"N");
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            ContextUtil.popContext(context);
        }
    }
    /*
     * @description:获取供货件的所有版本
     * @author: caipan
     * @date: 2025/7/18 16:52:07
     * @param: * @param[1] context
     * @param[2] args
     * @return:
     **/
    public MapList getZeroALLPart(Context context, String[] args) throws Exception {
        Map paramMap = JPO.unpackArgs(args);
        String objId = (String) paramMap.get("objectId");
        DomainObject object = DomainObject.newInstance(context, objId);
        StringList bosel = basicBolistSel();
        bosel.add(JF_PLMConstants_mxJPO.SELECT_ATTR_V_PART_NUMBER);
        StringList relSel = JF_Util_mxJPO.basicRellistSel();
        String relWhere = "attribute[JFZeroPart]==Y";
        MapList zeroPartMapList = object.getRelatedObjects(context, RELATIONSHIP_JFPROJECT2ROOTPART, TYPE_VPMREFERENCE, bosel, relSel, false, true, (short) 1, null, relWhere, 0);
        return zeroPartMapList;
    }

    /*
     * @description:物理产品升版的时候，触发关联上一个版本的关联的JFProject2RootPart(项目和零件的关系)
     *  把供货件和所属项目保存下来
     * @author: caipan
     * @date: 2025/5/27 15:16:34
     * @param: * @param[1] context
     * @param[2] args
     * @return:
     **/
    public void connJFProject2RootPart(Context context,String[] args) throws Exception{
        String oldObject = args[0];
        String newObject = args[1];

        DomainObject oldObj = DomainObject.newInstance(context,oldObject);
        DomainObject newObj = DomainObject.newInstance(context,newObject);
        String oldType = oldObj.getInfo(context, SELECT_TYPE);
        StringList selList = JF_Util_mxJPO.basicBolistSel();
        StringList relList = JF_Util_mxJPO.basicRellistSel();
        relList.add("attribute[JFZeroPart]");
        relList.add("attribute[JF_BelongPart]");
        if(oldType.equals("VPMReference")){
            log.info("oldObject:{},newObject:{}",oldObject,newObject);
            //查询项目
            try {
                ContextUtil.pushContext(context);
                MapList list = oldObj.getRelatedObjects(context,
                        JF_PLMConstants_mxJPO.rel_JFProject2RootPart, //pattern to match relationships
                        TYPE_PROJECT_SPACE, //pattern to match types
                        selList, //the eMatrix StringList object that holds the list of select statement pertaining to Business Obejcts.
                        relList, //the eMatrix StringList object that holds the list of select statement pertaining to Relationships.
                        true, //get To relationships
                        false, //get From relationships
                        (short) 1, //the number of levels to expand, 0 equals expand all.
                        EMPTY_STRING, //where clause to apply to objects, can be empty ""
                        null, //where clause to apply to relationship, can be empty ""
                        (short) 0); //limit
                RelationshipType relationshipType = new RelationshipType(JF_PLMConstants_mxJPO.rel_JFProject2RootPart);
                for (int i = 0; i < list.size(); i++) {
                    Map map = (Map) list.get(i);
                    String projectId = UIUtil.getValue(map, SELECT_ID);
                    String JFZeroPart = UIUtil.getValue(map, "attribute[JFZeroPart]");
                    String JF_BelongPart = UIUtil.getValue(map, "attribute[JF_BelongPart]");
                    DomainRelationship ship = newObj.addFromObject(context, relationshipType, projectId);
                    ship.setAttributeValue(context, "JFZeroPart", JFZeroPart);
                    ship.setAttributeValue(context, "JF_BelongPart", JF_BelongPart);
                }
            }catch (Exception e){
                e.printStackTrace();
            }finally {
                ContextUtil.popContext(context);
            }
        }
    }
    /*
     * @description:获取该零件的供货件和所属项目都为Y的
     * @author: caipan
     * @date: 2025/7/24 18:59:27
     * @param: * @param[1] context
     * @param[2] id
     * @return:
     **/
    public String getPartZeroBelowProject(Context context,String id)throws Exception{
        StringList selList = JF_Util_mxJPO.basicBolistSel();
        StringList relList =JF_Util_mxJPO.basicRellistSel();
        relList.add(JF_PLMConstants_mxJPO.ATTR_JF_BelongPart);
        relList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JFZeroPart);
        DomainObject partObj = DomainObject.newInstance(context,id);
        String where =SELECT_ATTR_JFZeroPart +" == Y && "+SELECT_ATTR_JF_BelongPart +" == Y";
        MapList list =  partObj.getRelatedObjects(context,
                RELATIONSHIP_JFPROJECT2ROOTPART, //pattern to match relationships
                TYPE_PROJECT_SPACE, //pattern to match types
                selList, //the eMatrix StringList object that holds the list of select statement pertaining to Business Obejcts.
                relList, //the eMatrix StringList object that holds the list of select statement pertaining to Relationships.
                true, //get To relationships
                false, //get From relationships
                (short) 1, //the number of levels to expand, 0 equals expand all.
                EMPTY_STRING, //where clause to apply to objects, can be empty ""
                where, //where clause to apply to relationship, can be empty ""
                (short) 0); //limit
        StringList zeroList =  JF_Util_mxJPO.mapList2StringList(list, SELECT_ID);
        if(zeroList.size()>0){
            return zeroList.get(0);
        }
        return "";

    }
    /*
     * @description:判断当前零件是否是供货件,必须是所属项目的供货件
     * @author: caipan
     * @date: 2025/7/28 18:15:15
     * @param: * @param[1] context
     * @param[2] args
     * @return:
     **/
    public boolean getPartIsSupplyPart(Context context,String[] args) throws Exception{
        String id = args[0];
        String psId = args[1];
        DomainObject obj = DomainObject.newInstance(context);
        obj.setId(id);
        StringList selList = JF_Util_mxJPO.basicBolistSel();
        StringList relList = JF_Util_mxJPO.basicRellistSel();
        String relwhere = SELECT_ATTR_JF_BelongPart+"==Y && "+SELECT_ATTR_JFZeroPart+"==Y";
        String where = "from.id=='"+psId+"'";
           MapList list =  obj.getRelatedObjects(context,
                JF_PLMConstants_mxJPO.rel_JFProject2RootPart, //pattern to match relationships
                TYPE_PROJECT_SPACE, //pattern to match types
                selList, //the eMatrix StringList object that holds the list of select statement pertaining to Business Obejcts.
                relList, //the eMatrix StringList object that holds the list of select statement pertaining to Relationships.
                true, //get To relationships
                false, //get From relationships
                (short) 1, //the number of levels to expand, 0 equals expand all.
                    where, //where clause to apply to objects, can be empty ""
                    relwhere, //where clause to apply to relationship, can be empty ""
                (short) 1); //limit
        if(list.size()>0){
            return true;
        }else{
            return false;
        }
    }

    /**
    * EBOM界面 显示当前这个件是不是仅CAD件
    * @param context
	* @param args
    * @author LIUJR
    * @throws
    * @return java.util.Vector
    * @date 2025/12/18 10:50
    * @description
    */
    public Vector getVpmInEBOMUser(Context context, String[] args) throws Exception {
        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        MapList objectList = (MapList) programMap.get("objectList");
        Vector ValuesVector = new Vector(objectList.size());
        Iterator ListItr = objectList.iterator();
        while (ListItr.hasNext()) {
            Map map = (Map) ListItr.next();
            //获取关系id
            String relid = (String) map.get("id[connection]");
            DomainRelationship domainRelationship=DomainRelationship.newInstance(context,relid);
            String strSynchroEBOMExtV_InEBOMUser=domainRelationship.getAttributeValue(context,JF_PLMConstants_mxJPO.Attr_SynchroEBOMCAD);
            if ("FALSE".equalsIgnoreCase(strSynchroEBOMExtV_InEBOMUser)) {
                ValuesVector.add("Yes");
            } else {
                ValuesVector.add("");
            }
        }
        return ValuesVector;
    }

    /**
     * 客户零件号/DB添加、移除按钮权限
     * 工作中状态仅零件Owner可操作，冻结和发布状态仅库管理员可操作
     * @param context
     * @param args
     * @author LIUJR
     * @throws Exception
     * @return boolean
     * @date 2026/7/13 16:00
     * @description
     */
    public boolean getCustomerPartsDBModifyAccess(Context context, String[] args) throws Exception {
        Map programMap = JPO.unpackArgs(args);
        String partId = getRequestValue(programMap, "objectId");
        if (UIUtil.isNullOrEmpty(partId)) {
            Map requestMap = (Map) programMap.get("requestMap");
            partId = getRequestValue(requestMap, "objectId");
        }
        return hasCustomerPartsDBModifyAccess(context, partId);
    }

    /**
     * 校验当前用户是否有权维护零件的客户零件号/DB信息
     * @param context
     * @param partId 零件ID
     * @author LIUJR
     * @throws Exception
     * @return boolean
     * @date 2026/7/13 16:00
     * @description
     */
    private boolean hasCustomerPartsDBModifyAccess(Context context, String partId) throws Exception {
        if (UIUtil.isNullOrEmpty(partId)) {
            return false;
        }
        StringList selectList = new StringList(DomainConstants.SELECT_CURRENT);
        selectList.add(DomainConstants.SELECT_OWNER);
        Map partInfoMap = DomainObject.newInstance(context, partId).getInfo(context, selectList);
        String current = UIUtil.getValue(partInfoMap, DomainConstants.SELECT_CURRENT);
        String owner = UIUtil.getValue(partInfoMap, DomainConstants.SELECT_OWNER);
        if ("IN_WORK".equalsIgnoreCase(current)) {
            return context.getUser().equals(owner);
        }
        if ("FROZEN".equalsIgnoreCase(current) || "RELEASED".equalsIgnoreCase(current)) {
            Vector assignments = PersonUtil.getAssignments(context, context.getUser());
            return assignments.contains("jfLibAdmin");
        }
        return false;
    }

    /**
     * 客户零件号/DB创建页面项目搜索排除已添加的项目
     * @param context
     * @param args
     * @author LIUJR
     * @throws Exception
     * @return matrix.util.StringList
     * @date 2026/7/14 10:00
     * @description
     */
    @com.matrixone.apps.framework.ui.ExcludeOIDProgramCallable
    public StringList getCustomerPartsDBUsedProjectIds(Context context, String[] args) throws Exception {
        Map programMap = JPO.unpackArgs(args);
        String partId = getRequestValue(programMap, "parentOID");
        if (UIUtil.isNullOrEmpty(partId)) {
            partId = getSelectedPartId(programMap);
        }
        String currentCustomerPartId = getRequestValue(programMap, "objectId");
        StringList usedProjectIdList = new StringList();
        if (UIUtil.isNullOrEmpty(partId)) {
            return usedProjectIdList;
        }

        // 查询当前零件已创建的客户零件号/DB记录及其关联项目
        String projectIdSelect = "frommid[" + RELATIONSHIP_JFVPMREFERENCE2CUSTOMERPARTS2PROJECT + "].to.id";
        StringList relationshipSelectList = JF_Util_mxJPO.basicRellistSel();
        relationshipSelectList.add(projectIdSelect);
        DomainObject partObject = DomainObject.newInstance(context, partId);
        MapList customerPartsList = partObject.getRelatedObjects(context,
                RELATIONSHIP_JFVPMREFERENCE2CUSTOMERPARTS,
                TYPE_JFCUSTOMERPARTS,
                JF_Util_mxJPO.basicBolistSel(),
                relationshipSelectList,
                true,
                false,
                (short) 1,
                DomainConstants.EMPTY_STRING,
                DomainConstants.EMPTY_STRING,
                0);

        // 合并已使用的项目ID，交给Full Search执行排除
        for (Object customerPartsObj : customerPartsList) {
            Map customerPartsMap = (Map) customerPartsObj;
            String customerPartId = UIUtil.getValue(customerPartsMap, DomainConstants.SELECT_ID);
            String projectId = UIUtil.getValue(customerPartsMap, projectIdSelect);
            if (UIUtil.isNotNullAndNotEmpty(currentCustomerPartId) && currentCustomerPartId.equals(customerPartId)) {
                continue;
            }
            if (UIUtil.isNotNullAndNotEmpty(projectId) && !usedProjectIdList.contains(projectId)) {
                usedProjectIdList.add(projectId);
            }
        }
        String belongProjectId = getPartBelongProject(context, new String[]{partId, DomainConstants.SELECT_ID});
        //20260817 update by Codex 流程中Owner及冻结/发布后的库管理员新增时，只能选择共用项目。
        if (UIUtil.isNotNullAndNotEmpty(belongProjectId)
                && !hasCustomerPartsDBProjectModifyAccess(context, partId, belongProjectId)
                && !usedProjectIdList.contains(belongProjectId)) {
            usedProjectIdList.add(belongProjectId);
        }
        return usedProjectIdList;
    }

    /**
     * Project customer parts list - all versions
     * @param context
     * @param args
     * @author LIUJR
     * @throws Exception
     * @return com.matrixone.apps.domain.util.MapList
     * @date 2026/7/14 16:00
     * @description Query all customer part information belonging to the current project.
     */
    public MapList getProjectCustomerPartsAllVersions(Context context, String[] args) throws Exception {
        return getProjectCustomerPartsByView(context, args, "all");
    }

    /**
     * Project customer parts list - latest version
     * @param context
     * @param args
     * @author LIUJR
     * @throws Exception
     * @return com.matrixone.apps.domain.util.MapList
     * @date 2026/7/14 16:00
     * @description Query the latest version of customer part information belonging to the current project.
     */
    public MapList getProjectCustomerPartsLatestVersion(Context context, String[] args) throws Exception {
        return getProjectCustomerPartsByView(context, args, "latest");
    }

    /**
     * Project customer parts list - latest released version
     * @param context
     * @param args
     * @author LIUJR
     * @throws Exception
     * @return com.matrixone.apps.domain.util.MapList
     * @date 2026/7/14 16:00
     * @description Query the latest released customer part information belonging to the current project.
     */
    public MapList getProjectCustomerPartsLatestReleasedVersion(Context context, String[] args) throws Exception {
        return getProjectCustomerPartsByView(context, args, "latestReleased");
    }

    /**
     * 获取项目客户零件表的供应商
     **
     * @param context 上下文
     * @param args 表格参数
     * @return Vector 每行零件的库分类Supplier属性值，未入库零件返回空
     * @throws Exception 异常
     * @author caipan
     * @date 2026/8/13 16:13
     */
    public Vector getProjectCustomerPartsSupplier(Context context, String[] args) throws Exception {
        Map programMap = JPO.unpackArgs(args);
        MapList objectList = (MapList) programMap.get("objectList");
        Vector result = new Vector();
        if (objectList == null || objectList.isEmpty()) {
            return result;
        }
        String[] objectIds = new String[objectList.size()];
        for (int i = 0; i < objectList.size(); i++) {
            objectIds[i] = UIUtil.getValue((Map) objectList.get(i), SELECT_ID);
        }
        MapList supplierInfoList = DomainObject.getInfo(context, objectIds,
                new StringList(JF_PLMConstants_mxJPO.SELECT_ATTR_Supplier));
        for (Object supplierInfoObj : supplierInfoList) {
            result.add(UIUtil.getValue((Map) supplierInfoObj,
                    JF_PLMConstants_mxJPO.SELECT_ATTR_Supplier));
        }
        return result;
    }

    /**
     * Query project customer parts by version view
     * @param context
     * @param args
     * @param viewMode Version view: all, latest or latestReleased
     * @author LIUJR
     * @throws Exception
     * @return com.matrixone.apps.domain.util.MapList
     * @date 2026/7/14 16:00
     * @description Read the part and relationship information in batches and filter the DB list and version view.
     */
    private MapList getProjectCustomerPartsByView(Context context, String[] args, String viewMode) throws Exception {
        Map programMap = JPO.unpackArgs(args);
        String projectId = getRequestValue(programMap, "objectId");
        String listType = getRequestValue(programMap, "listType");
        MapList resultList = new MapList();
        if (UIUtil.isNullOrEmpty(projectId)) {
            return resultList;
        }

        // Query all customer-part relationship IDs connected to the current project.
        String projectRelationshipSelect = "to[" + RELATIONSHIP_JFVPMREFERENCE2CUSTOMERPARTS2PROJECT + "].fromrel.id";
        DomainObject projectObject = DomainObject.newInstance(context, projectId);
        StringList customerPartRelationshipIdList = projectObject.getInfoList(context, projectRelationshipSelect);
        if (customerPartRelationshipIdList == null || customerPartRelationshipIdList.isEmpty()) {
            return resultList;
        }
        String[] customerPartRelationshipIds = new String[customerPartRelationshipIdList.size()];
        for (int i = 0; i < customerPartRelationshipIdList.size(); i++) {
            customerPartRelationshipIds[i] = (String) customerPartRelationshipIdList.get(i);
        }

        // Read the VPMReference fields and the customer-part relationship attributes in one batch.
        String toIdSelect = "to." + SELECT_ID;
        String toTypeSelect = "to." + SELECT_TYPE;
        String toNameSelect = "to." + SELECT_NAME;
        String toRevisionSelect = "to." + SELECT_REVISION;
        String toCurrentSelect = "to." + SELECT_CURRENT;
        String toOwnerSelect = "to." + SELECT_OWNER;
        String toPartNumberSelect = "to." + JF_PLMConstants_mxJPO.SELECT_ATTR_V_PART_NUMBER;
        String toPartNameCNSelect = "to." + JF_PLMConstants_mxJPO.SELECT_ATTR_JF_PartNameCN;
        String toPartNameENSelect = "to." + JF_PLMConstants_mxJPO.SELECT_ATTR_JF_PartNameEN;
        String toDetailCNSelect = "to." + JF_PLMConstants_mxJPO.SELECT_ATTR_JF_Detail_CN;
        String toLastVersionSelect = "to." + JF_PLMConstants_mxJPO.SELECT_ATTR_V_IsLastVersion;
        String toSupplierSelect = "to." + JF_PLMConstants_mxJPO.SELECT_ATTR_Supplier;
        StringList relationshipSelectList = new StringList(SELECT_ID);
        relationshipSelectList.add(toIdSelect);
        relationshipSelectList.add(toTypeSelect);
        relationshipSelectList.add(toNameSelect);
        relationshipSelectList.add(toRevisionSelect);
        relationshipSelectList.add(toCurrentSelect);
        relationshipSelectList.add(toOwnerSelect);
        relationshipSelectList.add(toPartNumberSelect);
        relationshipSelectList.add(toPartNameCNSelect);
        relationshipSelectList.add(toPartNameENSelect);
        relationshipSelectList.add(toDetailCNSelect);
        relationshipSelectList.add(toLastVersionSelect);
        relationshipSelectList.add(toSupplierSelect);
        relationshipSelectList.add(JF_PLMConstants_mxJPO.Select_Attr_JFCustomerPartNumber);
        relationshipSelectList.add(JF_PLMConstants_mxJPO.Select_Attr_JFCustomerPartName);
        relationshipSelectList.add(JF_PLMConstants_mxJPO.Select_Attr_JFCustomerPartRevision);
        relationshipSelectList.add(JF_PLMConstants_mxJPO.Select_Attr_JF_DirectBuy);
        relationshipSelectList.add(JF_PLMConstants_mxJPO.Select_Attr_JF_RelDescription);
        MapList relationshipInfoList = DomainRelationship.getInfo(context, customerPartRelationshipIds, relationshipSelectList);

        Map latestReleasedIdMap = new HashMap();
        for (Object relationshipInfoObj : relationshipInfoList) {
            Map relationshipInfoMap = (Map) relationshipInfoObj;
            String partId = UIUtil.getValue(relationshipInfoMap, toIdSelect);
            String directBuy = UIUtil.getValue(relationshipInfoMap, JF_PLMConstants_mxJPO.Select_Attr_JF_DirectBuy);
            if (UIUtil.isNullOrEmpty(partId)) {
                continue;
            }

            // The DB list only contains direct-buy and consignment parts.
            if ("db".equalsIgnoreCase(listType)
                    && !"direct-buy".equalsIgnoreCase(directBuy)
                    && !"consignment".equalsIgnoreCase(directBuy)) {
                continue;
            }

            // Filter by the selected version view.
            if ("latest".equals(viewMode)
                    && !"TRUE".equalsIgnoreCase(UIUtil.getValue(relationshipInfoMap, toLastVersionSelect))) {
                continue;
            }
            if ("latestReleased".equals(viewMode)) {
                String partSeriesKey = UIUtil.getValue(relationshipInfoMap, toTypeSelect) + "|"
                        + UIUtil.getValue(relationshipInfoMap, toNameSelect);
                String latestReleasedId = UIUtil.getValue(latestReleasedIdMap, partSeriesKey);
                if (!latestReleasedIdMap.containsKey(partSeriesKey)) {
                    latestReleasedId = JF_Util_mxJPO.getLastReleasedMajorid(context, partId);
                    latestReleasedIdMap.put(partSeriesKey, latestReleasedId);
                }
                if (UIUtil.isNullOrEmpty(latestReleasedId) || !partId.equalsIgnoreCase(latestReleasedId)) {
                    continue;
                }
            }

            // Convert the relationship selects to the row keys consumed by the table columns.
            Map rowMap = new HashMap();
            rowMap.put(SELECT_ID, partId);
            rowMap.put("id[connection]", UIUtil.getValue(relationshipInfoMap, SELECT_ID));
            rowMap.put(SELECT_TYPE, UIUtil.getValue(relationshipInfoMap, toTypeSelect));
            rowMap.put(SELECT_NAME, UIUtil.getValue(relationshipInfoMap, toNameSelect));
            rowMap.put(SELECT_REVISION, UIUtil.getValue(relationshipInfoMap, toRevisionSelect));
            rowMap.put(SELECT_CURRENT, UIUtil.getValue(relationshipInfoMap, toCurrentSelect));
            rowMap.put(SELECT_OWNER, UIUtil.getValue(relationshipInfoMap, toOwnerSelect));
            rowMap.put(JF_PLMConstants_mxJPO.SELECT_ATTR_V_PART_NUMBER, UIUtil.getValue(relationshipInfoMap, toPartNumberSelect));
            rowMap.put(JF_PLMConstants_mxJPO.SELECT_ATTR_JF_PartNameCN, UIUtil.getValue(relationshipInfoMap, toPartNameCNSelect));
            rowMap.put(JF_PLMConstants_mxJPO.SELECT_ATTR_JF_PartNameEN, UIUtil.getValue(relationshipInfoMap, toPartNameENSelect));
            rowMap.put(JF_PLMConstants_mxJPO.SELECT_ATTR_JF_Detail_CN, UIUtil.getValue(relationshipInfoMap, toDetailCNSelect));
            rowMap.put(JF_PLMConstants_mxJPO.SELECT_ATTR_Supplier, UIUtil.getValue(relationshipInfoMap, toSupplierSelect));
            rowMap.put(JF_PLMConstants_mxJPO.Select_Attr_JFCustomerPartNumber,
                    UIUtil.getValue(relationshipInfoMap, JF_PLMConstants_mxJPO.Select_Attr_JFCustomerPartNumber));
            rowMap.put(JF_PLMConstants_mxJPO.Select_Attr_JFCustomerPartName,
                    UIUtil.getValue(relationshipInfoMap, JF_PLMConstants_mxJPO.Select_Attr_JFCustomerPartName));
            rowMap.put(JF_PLMConstants_mxJPO.Select_Attr_JFCustomerPartRevision,
                    UIUtil.getValue(relationshipInfoMap, JF_PLMConstants_mxJPO.Select_Attr_JFCustomerPartRevision));
            rowMap.put(JF_PLMConstants_mxJPO.Select_Attr_JF_DirectBuy, directBuy);
            rowMap.put(JF_PLMConstants_mxJPO.Select_Attr_JF_RelDescription,
                    UIUtil.getValue(relationshipInfoMap, JF_PLMConstants_mxJPO.Select_Attr_JF_RelDescription));
            resultList.add(rowMap);
        }
        log.info("resultList:{}",resultList);
        return resultList;
    }

    /**
     * Customer parts DB table project link output
     *    使用项目对象ID生成项目名称链接，避免关系再关联对象时由表格自动链接解析失败
     * @param context
     * @param args
     * @author LIUJR
     * @throws Exception
     * @return Vector
     * @date 2026/7/15
     * @description
     */
    public Vector getCustomerPartsDBProjectName(Context context, String[] args) throws Exception {
        Map programMap = JPO.unpackArgs(args);
        MapList objectList = (MapList) programMap.get("objectList");
        Vector result = new Vector();
        String projectIdSelect = "frommid[" + RELATIONSHIP_JFVPMREFERENCE2CUSTOMERPARTS2PROJECT + "].to.id";
        String projectNameSelect = "frommid[" + RELATIONSHIP_JFVPMREFERENCE2CUSTOMERPARTS2PROJECT + "].to.description";
        for (Object object : objectList) {
            Map objectMap = (Map) object;
            String projectId = UIUtil.getValue(objectMap, projectIdSelect);
            String projectName = UIUtil.getValue(objectMap, projectNameSelect);
            result.add(UIUtil.isNullOrEmpty(projectId) || UIUtil.isNullOrEmpty(projectName)
                    ? "   "
                    : JF_Util_mxJPO.buildHtml(context, projectId, projectName));
        }
        return result;
    }

    /**
     * 获取客户零件号/DB DirectBuy字段范围值
     * @param context 上下文
     * @param args 请求参数
     * @author LIUJR
     * @throws Exception 异常
     * @return java.util.HashMap 范围值和显示值
     * @date 2026/7/16
     * @description 从DirectBuy属性中获取完整范围，供客户零件号/DB创建表单和EBOM编辑列共用。
     */
    public Map getCustomerPartsDirectBuyRange(Context context, String[] args) throws Exception {
        HashMap<Object, Object> res = new HashMap<>();
        StringList ranges = FrameworkUtil.getRanges(context, JF_PLMConstants_mxJPO.Attr_JF_DirectBuy);
        res.put("field_choices", ranges);
        res.put("field_display_choices", ranges);
        return res;
    }

    /**
     * 获取客户零件号/DB表格操作列编辑图标
     * @param context 上下文
     * @param args 表格参数
     * @author LIUJR
     * @throws Exception 异常
     * @return java.util.Vector 操作列HTML
     * @date 2026/7/16
     * @description 工作中由零件Owner维护；进入DR/标准件发布流程后仅可维护共用项目；冻结/发布后仅库管理员可维护共用项目。
     */
    public Vector getCustomerPartsDBEditIcon(Context context, String[] args) throws Exception {
        Map programMap = JPO.unpackArgs(args);
        Map paramList = (Map) programMap.get("paramList");
        String partId = getRequestValue(programMap, "objectId");
        if (UIUtil.isNullOrEmpty(partId)) {
            partId = getRequestValue(paramList, "objectId");
        }
        if (UIUtil.isNullOrEmpty(partId)) {
            partId = getRequestValue(paramList, "parentOID");
        }

        MapList objectList = (MapList) programMap.get("objectList");
        Vector result = new Vector();
        if (objectList == null || objectList.isEmpty()) {
            return result;
        }
        if (UIUtil.isNullOrEmpty(partId)) {
            for (int i = 0; i < objectList.size(); i++) {
                result.add(DomainConstants.EMPTY_STRING);
            }
            return result;
        }

        DomainObject object = DomainObject.newInstance(context, partId);
        String current = DomainConstants.EMPTY_STRING;
        String partOwner = DomainConstants.EMPTY_STRING;
        if (UIUtil.isNotNullAndNotEmpty(partId)) {
            StringList partSelectList = new StringList(DomainConstants.SELECT_CURRENT);
            partSelectList.add(DomainConstants.SELECT_OWNER);
            Map partInfoMap = object.getInfo(context, partSelectList);
            current = UIUtil.getValue(partInfoMap, DomainConstants.SELECT_CURRENT);
            partOwner = UIUtil.getValue(partInfoMap, DomainConstants.SELECT_OWNER);
        }
        boolean isInWork = "IN_WORK".equalsIgnoreCase(current);
        boolean isFrozenOrReleased = "FROZEN".equalsIgnoreCase(current) || "RELEASED".equalsIgnoreCase(current);
        boolean isPartOwner = context.getUser().equals(partOwner);
        boolean isLibAdmin = PersonUtil.getAssignments(context, context.getUser()).contains("jfLibAdmin");
        boolean isInReleaseProcess = isCustomerPartsDBInReleaseProcess(context, object);
        String belongProjectId = getPartBelongProject(context, new String[]{partId, DomainConstants.SELECT_ID});
        String rowProjectIdSelect = "frommid[" + RELATIONSHIP_JFVPMREFERENCE2CUSTOMERPARTS2PROJECT + "].to.id";
        String editTitle = EnoviaResourceBundle.getProperty(context,
                "emxFrameworkStringResource",
                context.getLocale(),
                "emxFramework.TableEdit.Edit");

        for (int i = 0; i < objectList.size(); i++) {
            Map objectMap = (Map) objectList.get(i);
            String customerPartId = UIUtil.getValue(objectMap, DomainConstants.SELECT_ID);
            String relId = UIUtil.getValue(objectMap, DomainRelationship.SELECT_ID);
            String actionHtml = DomainConstants.EMPTY_STRING;
            Object rowProjectValue = objectMap.get(rowProjectIdSelect);
            boolean isBelongProjectRow = isSameEBOMCustomerPartsDBProjectValue(
                    belongProjectId,
                    rowProjectValue);
            boolean hasRowProject = rowProjectValue != null
                    && (!(rowProjectValue instanceof List) || !((List) rowProjectValue).isEmpty())
                    && UIUtil.isNotNullAndNotEmpty(String.valueOf(rowProjectValue));
            //20260817 update by Codex 工作中按零件Owner授权；流程中Owner及冻结/发布后的库管理员只能编辑共用项目行。
            boolean hasEditAccess = (isInWork
                    && isPartOwner
                    && (!isInReleaseProcess || (hasRowProject && !isBelongProjectRow)))
                    || (isFrozenOrReleased
                    && isLibAdmin
                    && hasRowProject
                    && !isBelongProjectRow);
            if (hasEditAccess && UIUtil.isNotNullAndNotEmpty(customerPartId) && UIUtil.isNotNullAndNotEmpty(relId)) {
                    String editUrl = "../common/emxForm.jsp?form=JFCustomerPartsDBEditForm"
                            + "&mode=edit"
                            + "&objectId=" + XSSUtil.encodeForURL(context, customerPartId)
                            + "&relId=" + XSSUtil.encodeForURL(context, relId)
                            + "&parentOID=" + XSSUtil.encodeForURL(context, partId)
                            + "&formHeader=emxFramework.TableEdit.Edit"
                            + "&submitAction=doNothing"
//                        + "&targetLocation=slidein"
                            + "&postProcessURL=../common/JF_CustomerPartsDBEditPostProcess.jsp";
                    actionHtml = "<a href=\"javascript:emxTableColumnLinkClick('"
                            + XSSUtil.encodeForJavaScript(context, editUrl)
                            + "','450','600','false','slidein','','','true','','450');\">"
                            + "<img border='0' src='../common/images/iconActionEdit.png' alt=\""
                            + XSSUtil.encodeForHTMLAttribute(context, editTitle)
                            + "\" title=\""
                            + XSSUtil.encodeForHTMLAttribute(context, editTitle)
                            + "\"></img></a>";
            }
            result.add(actionHtml);
        }
        return result;
    }

    /**
     * 更新客户零件号/DB编辑表单字段
     * @param context 上下文
     * @param args 表单提交参数
     * @author LIUJR
     * @throws Exception 异常
     * @return void
     * @date 2026/7/16
     * @description 表单负责编辑当前行关系属性，提交时再次校验当前用户是否仍有该行编辑权限；
     *              所属项目行的DirectBuy发生修改时，同时同步到零件属性。
     */
    public void updateCustomerPartsDBForm(Context context, String[] args) throws Exception {
        Map programMap = JPO.unpackArgs(args);
        Map paramMap = (Map) programMap.get("paramMap");
        Map requestMap = (Map) programMap.get("requestMap");
        Map fieldMap = (Map) programMap.get("fieldMap");
        String relId = getRequestValue(paramMap, "relId");
        if (UIUtil.isNullOrEmpty(relId)) {
            relId = getRequestValue(requestMap, "relId");
        }
        String partId = getRequestValue(requestMap, "parentOID");
        String customerPartId = getRequestValue(paramMap, "objectId");
        if (UIUtil.isNullOrEmpty(customerPartId)) {
            customerPartId = getRequestValue(requestMap, "objectId");
        }
        if (!hasCustomerPartsDBRowEditAccess(context, partId, customerPartId, relId)) {
            throw new Exception(EnoviaResourceBundle.getProperty(context,
                    "emxComponentsStringResource",
                    context.getLocale(),
                    "emxComponents.JFCustomerPartsDB.NoModifyAccess"));
        }

        String fieldName = UIUtil.getValue(fieldMap, DomainConstants.SELECT_NAME);
        if ("JFProjectName".equals(fieldName)) {
            updateCustomerPartsDBProject(context, partId, relId, paramMap);
            return;
        }
        String attrName = getCustomerPartsDBRelationshipAttribute(fieldName);
        if (UIUtil.isNullOrEmpty(relId) || UIUtil.isNullOrEmpty(attrName)) {
            return;
        }
        String newValue = getRequestValue(paramMap, "New Value");
        String oldValue = getRequestValue(paramMap, "Old Value");
        if (newValue == null) {
            newValue = DomainConstants.EMPTY_STRING;
        }
        if (oldValue == null) {
            oldValue = DomainConstants.EMPTY_STRING;
        }
        if (!newValue.equals(oldValue)) {
            boolean isPush = false;
            boolean transactionStarted = false;
            try {
                ContextUtil.pushContext(context);
                isPush = true;
                ContextUtil.startTransaction(context, true);
                transactionStarted = true;
                DomainRelationship.newInstance(context, relId).setAttributeValue(context, attrName, newValue);
                if (JF_PLMConstants_mxJPO.Attr_JF_DirectBuy.equals(attrName)) {
                    String belongProjectId = getPartBelongProject(
                            context,
                            new String[]{partId, DomainConstants.SELECT_ID});
                    String rowProjectIds = MqlUtil.mqlCommand(context,
                            "print connection $1 select $2 dump $3",
                            relId,
                            "frommid[" + RELATIONSHIP_JFVPMREFERENCE2CUSTOMERPARTS2PROJECT + "].to.id",
                            "|");
                    if (isSameEBOMCustomerPartsDBProjectValue(
                            belongProjectId,
                            FrameworkUtil.split(rowProjectIds, "|"))) {
                        //20260903 update by liujr 编辑所属项目DB信息的DirectBuy时，同步到零件属性。
                        DomainObject.newInstance(context, partId).setAttributeValue(
                                context,
                                JF_PLMConstants_mxJPO.ATTR_JFDIRECT_BUY,
                                newValue);
                    }
                }
                ContextUtil.commitTransaction(context);
                transactionStarted = false;
            } catch (Exception e) {
                if (transactionStarted) {
                    ContextUtil.abortTransaction(context);
                }
                throw e;
            } finally {
                if (isPush) {
                    ContextUtil.popContext(context);
                }
            }
        }
    }

    /**
     * 判断当前用户是否可以编辑指定客户零件号/DB行
     * @param context 上下文
     * @param partId 零件ID
     * @param customerPartId 客户零件号/DB对象ID
     * @param relId 零件与客户零件号/DB关系ID
     * @author LIUJR
     * @throws Exception 异常
     * @return boolean 是否可编辑
     * @date 2026/7/16
     * @description 工作中由零件Owner维护；流程中Owner及冻结/发布后的库管理员只能维护共用项目数据。
     */
    private boolean hasCustomerPartsDBRowEditAccess(Context context, String partId, String customerPartId, String relId) throws Exception {
        if (UIUtil.isNullOrEmpty(partId) || UIUtil.isNullOrEmpty(customerPartId)) {
            return false;
        }
        if (UIUtil.isNullOrEmpty(relId)) {
            return false;
        }
        String rowConnectionInfo = MqlUtil.mqlCommand(context,
                "print connection $1 select $2 $3 $4 dump $5",
                relId,
                "from.id",
                "to.id",
                "frommid[" + RELATIONSHIP_JFVPMREFERENCE2CUSTOMERPARTS2PROJECT + "].to.id",
                "|");
        StringList rowConnectionInfoList = FrameworkUtil.split(rowConnectionInfo, "|");
        if (rowConnectionInfoList.size() < 3
                || !customerPartId.equals(rowConnectionInfoList.get(0))
                || !partId.equals(rowConnectionInfoList.get(1))) {
            return false;
        }
        String rowProjectIds = (String) rowConnectionInfoList.get(2);
        //20260817 update by Codex 表单提交与删除统一按零件状态、流程、零件Owner/库管理员和项目类型校验。
        return hasCustomerPartsDBProjectModifyAccess(
                context,
                partId,
                FrameworkUtil.split(rowProjectIds, "|"));
    }

    /**
     * 判断当前用户是否可以维护指定项目的客户零件号/DB信息
     **
     * @param context 上下文
     * @param partId 零件ID
     * @param projectValue 客户零件号/DB行关联的项目ID或项目ID列表
     * @return boolean 是否可以维护
     * @throws Exception 异常
     * @author Codex
     * @date 2026/8/17
     */
    private boolean hasCustomerPartsDBProjectModifyAccess(Context context, String partId, Object projectValue) throws Exception {
        boolean hasProject = projectValue != null
                && (!(projectValue instanceof List) || !((List) projectValue).isEmpty())
                && UIUtil.isNotNullAndNotEmpty(String.valueOf(projectValue));
        if (UIUtil.isNullOrEmpty(partId) || !hasProject) {
            return false;
        }
        DomainObject partObject = DomainObject.newInstance(context, partId);
        StringList partSelectList = new StringList(DomainConstants.SELECT_CURRENT);
        partSelectList.add(DomainConstants.SELECT_OWNER);
        Map partInfoMap = partObject.getInfo(context, partSelectList);
        String current = UIUtil.getValue(partInfoMap, DomainConstants.SELECT_CURRENT);
        String partOwner = UIUtil.getValue(partInfoMap, DomainConstants.SELECT_OWNER);
        String belongProjectId = getPartBelongProject(context, new String[]{partId, DomainConstants.SELECT_ID});
        boolean isBelongProject = isSameEBOMCustomerPartsDBProjectValue(belongProjectId, projectValue);

        if ("IN_WORK".equalsIgnoreCase(current) && context.getUser().equals(partOwner)) {
            return !isCustomerPartsDBInReleaseProcess(context, partObject) || !isBelongProject;
        }
        if (("FROZEN".equalsIgnoreCase(current) || "RELEASED".equalsIgnoreCase(current))
                && PersonUtil.getAssignments(context, context.getUser()).contains("jfLibAdmin")) {
            return !isBelongProject;
        }
        return false;
    }

    /**
     * 判断零件是否处于DR或标准件发布流程
     **
     * @param context 上下文
     * @param partObject 零件对象
     * @return boolean 存在Change Control接口时返回true
     * @throws Exception 异常
     * @author Codex
     * @date 2026/8/17
     */
    private boolean isCustomerPartsDBInReleaseProcess(Context context, DomainObject partObject) throws Exception {
        BusinessInterfaceList businessInterfaces = partObject.getBusinessInterfaces(context);
        for (int i = 0; i < businessInterfaces.size(); i++) {
            BusinessInterface businessInterface = businessInterfaces.get(i);
            if ("Change Control".equals(businessInterface.getName())) {
                return true;
            }
        }
        return false;
    }

    /**
     * 更新客户零件号/DB当前行关联的项目
     * @param context 上下文
     * @param partId 零件ID
     * @param relId 零件与客户零件号/DB关系ID
     * @param paramMap 表单字段参数
     * @author LIUJR
     * @throws Exception 异常
     * @return void
     * @date 2026/7/16
     * @description 项目不是关系属性，需要更新挂在主关系上的JFVPMReference2CustomerParts2Project关系。
     *              当前行关联项目为零件所属项目时禁止替换项目端关系；其他项目行仍允许修改项目。
     */
    private void updateCustomerPartsDBProject(Context context, String partId, String relId, Map paramMap) throws Exception {
        if (UIUtil.isNullOrEmpty(partId) || UIUtil.isNullOrEmpty(relId)) {
            return;
        }
        String newProjectId = getRequestValue(paramMap, "New OID");
        String oldProjectId = getRequestValue(paramMap, "Old OID");
        if (newProjectId == null) {
            newProjectId = DomainConstants.EMPTY_STRING;
        }
        if (oldProjectId == null) {
            oldProjectId = DomainConstants.EMPTY_STRING;
        }
        if (newProjectId.equals(oldProjectId)) {
            return;
        }

        // 所属项目对应的客户零件号/DB行只允许修改关系属性，禁止从编辑表单切换项目。
        String partProjectId = getPartBelongProject(context, new String[]{partId, DomainConstants.SELECT_ID});
        String rowProjectIds = MqlUtil.mqlCommand(context,
                "print connection $1 select $2 dump $3",
                relId,
                "frommid[" + RELATIONSHIP_JFVPMREFERENCE2CUSTOMERPARTS2PROJECT + "].to.id",
                "|");
        if (UIUtil.isNotNullAndNotEmpty(partProjectId)
                && UIUtil.isNotNullAndNotEmpty(rowProjectIds)
                && FrameworkUtil.split(rowProjectIds, "|").contains(partProjectId)) {
            return;
        }

        // 目标项目已被当前零件的其他客户零件号/DB行使用时不重复连接，正常项目搜索会提前排除该项目。
        if (UIUtil.isNotNullAndNotEmpty(newProjectId)) {
            //20260817 update by Codex 保存时校验目标项目，禁止通过构造请求把共用项目行切换成受保护的所属项目行。
            if (!hasCustomerPartsDBProjectModifyAccess(context, partId, newProjectId)) {
                throw new Exception(EnoviaResourceBundle.getProperty(context,
                        "emxComponentsStringResource",
                        context.getLocale(),
                        "emxComponents.JFCustomerPartsDB.NoModifyAccess"));
            }
            Map targetCustomerPartsDBMap = getEBOMCustomerPartsDBRelationInfo(context, partId, newProjectId);
            String targetCustomerPartsRelId = UIUtil.getValue(targetCustomerPartsDBMap, DomainRelationship.SELECT_ID);
            if (UIUtil.isNotNullAndNotEmpty(targetCustomerPartsRelId) && !relId.equals(targetCustomerPartsRelId)) {
                return;
            }
        }

        boolean isPush = false;
        try {
            ContextUtil.pushContext(context);
            isPush = true;
            ContextUtil.startTransaction(context, true);
            String oldProjectRelId = MqlUtil.mqlCommand(context,
                    "print connection $1 select $2 dump",
                    relId,
                    "frommid[" + RELATIONSHIP_JFVPMREFERENCE2CUSTOMERPARTS2PROJECT + "].id");
            if (UIUtil.isNotNullAndNotEmpty(oldProjectRelId)) {
                DomainRelationship.disconnect(context, oldProjectRelId);
            }
            if (UIUtil.isNotNullAndNotEmpty(newProjectId)) {
                MqlUtil.mqlCommand(context,
                        "add connection $1 fromrel $2 to $3 select $4 dump",
                        RELATIONSHIP_JFVPMREFERENCE2CUSTOMERPARTS2PROJECT,
                        relId,
                        newProjectId,
                        "id");
            }
            ContextUtil.commitTransaction(context);
        } catch (Exception e) {
            ContextUtil.abortTransaction(context);
            throw e;
        } finally {
            if (isPush) {
                ContextUtil.popContext(context);
            }
        }
    }

    /**
     * 根据表单字段获取客户零件号/DB关系属性
     * @param fieldName 字段名称
     * @author LIUJR
     * @return java.lang.String 关系属性名称
     * @date 2026/7/16
     * @description 编辑表单字段与关系属性一一映射。
     */
    private String getCustomerPartsDBRelationshipAttribute(String fieldName) {
        if ("JFCustomerPartNumber".equals(fieldName)) {
            return JF_PLMConstants_mxJPO.Attr_JFCustomerPartNumber;
        }
        if ("JFCustomerPartName".equals(fieldName)) {
            return JF_PLMConstants_mxJPO.Attr_JFCustomerPartName;
        }
        if ("JFCustomerPartRevision".equals(fieldName)) {
            return JF_PLMConstants_mxJPO.Attr_JFCustomerPartRevision;
        }
        if ("JF_DirectBuy".equals(fieldName)) {
            return JF_PLMConstants_mxJPO.Attr_JF_DirectBuy;
        }
        if ("description".equals(fieldName)) {
            return JF_PLMConstants_mxJPO.Attr_JF_RelDescription;
        }
        return DomainConstants.EMPTY_STRING;
    }

    /**
     * 获取EBOM编辑页面客户零件号/DB列显示值
     * @param context 上下文
     * @param args 表格参数
     * @author LIUJR
     * @throws Exception 异常
     * @return java.util.Vector 客户零件号/DB关系属性值
     * @date 2026/7/17
     * @description 按当前零件所属项目查找客户零件号/DB关系，只显示该项目下维护的数据。
     */
    public Vector getEBOMCustomerPartsDBValue(Context context, String[] args) throws Exception {
        Map programMap = JPO.unpackArgs(args);
        Map columnMap = (Map) programMap.get("columnMap");
        String columnName = UIUtil.getValue(columnMap, DomainConstants.SELECT_NAME);
        String attrName = getEBOMCustomerPartsDBAttribute(columnName);
        String selectName = UIUtil.isNullOrEmpty(attrName) ? DomainConstants.EMPTY_STRING : "attribute[" + attrName + "]";
        String defaultValue = DomainConstants.EMPTY_STRING;
        MapList objectList = (MapList) programMap.get("objectList");
        Vector result = new Vector();
        if (objectList == null || objectList.isEmpty()) {
            return result;
        }
        for (int i = 0; i < objectList.size(); i++) {
            Map objectMap = (Map) objectList.get(i);
            String partId = UIUtil.getValue(objectMap, DomainConstants.SELECT_ID);
            String projectId = getPartBelongProject(context, new String[]{partId, DomainConstants.SELECT_ID});
            if (UIUtil.isNullOrEmpty(projectId) || UIUtil.isNullOrEmpty(selectName)) {
                result.add(defaultValue);
                continue;
            }
            Map customerPartsDBMap = getEBOMCustomerPartsDBRelationInfo(context, partId, projectId);
            String value = UIUtil.getValue(customerPartsDBMap, selectName);
            result.add(UIUtil.isNullOrEmpty(value) ? defaultValue : value);
        }
        return result;
    }

    /**
     * EBOM编辑页面客户零件号/DB列编辑权限
     * @param context 上下文
     * @param args 表格参数
     * @author LIUJR
     * @throws Exception 异常
     * @return matrix.util.StringList 每行是否可编辑
     * @date 2026/7/17
     * @description 只有工作中状态且owner为当前登录人的零件允许维护客户零件号/DB关系属性。
     */
    public StringList getEBOMCustomerPartsDBEditAccess(Context context, String[] args) throws Exception {
        Map programMap = JPO.unpackArgs(args);
        MapList objectList = (MapList) programMap.get("objectList");
        StringList result = new StringList();
        if (objectList == null || objectList.isEmpty()) {
            return result;
        }
        for (int i = 0; i < objectList.size(); i++) {
            Map objectMap = (Map) objectList.get(i);
            String partId = UIUtil.getValue(objectMap, DomainConstants.SELECT_ID);
            result.add(hasEBOMCustomerPartsDBEditAccess(context, partId) ? "true" : "false");
        }
        return result;
    }

    /**
     * 更新EBOM编辑页面客户零件号/DB关系属性
     * @param context 上下文
     * @param args 表格更新参数
     * @author LIUJR
     * @throws Exception 异常
     * @return void
     * @date 2026/7/17
     * @description 保存当前单元格时，按零件所属项目查找客户零件号/DB关系；不存在且当前值非空时创建对象和关系。
     */
    public void updateEBOMCustomerPartsDBValue(Context context, String[] args) throws Exception {
        Map programMap = JPO.unpackArgs(args);
        Map paramMap = (Map) programMap.get("paramMap");
        Map columnMap = (Map) programMap.get("columnMap");
        String partId = getRequestValue(paramMap, "objectId");
        String columnName = UIUtil.getValue(columnMap, DomainConstants.SELECT_NAME);
        String attrName = getEBOMCustomerPartsDBAttribute(columnName);
        if (UIUtil.isNullOrEmpty(partId) || UIUtil.isNullOrEmpty(attrName)) {
            return;
        }
        if (!hasEBOMCustomerPartsDBEditAccess(context, partId)) {
            throw new Exception(EnoviaResourceBundle.getProperty(context,
                    "emxComponentsStringResource",
                    context.getLocale(),
                    "emxComponents.JFCustomerPartsDB.NoModifyAccess"));
        }
        String projectId = getPartBelongProject(context, new String[]{partId, DomainConstants.SELECT_ID});
        if (UIUtil.isNullOrEmpty(projectId)) {
            return;
        }
        String newValue = getRequestValue(paramMap, "New Value");
        if (newValue == null) {
            newValue = DomainConstants.EMPTY_STRING;
        }
        Map customerPartsDBMap = getEBOMCustomerPartsDBRelationInfo(context, partId, projectId);
        String relId = UIUtil.getValue(customerPartsDBMap, DomainRelationship.SELECT_ID);
        if (UIUtil.isNullOrEmpty(relId) && UIUtil.isNullOrEmpty(newValue)) {
            return;
        }

        boolean isPush = false;
        try {
            ContextUtil.startTransaction(context, true);
            String customerPartsId = UIUtil.getValue(customerPartsDBMap, DomainConstants.SELECT_ID);
            if (UIUtil.isNullOrEmpty(relId)) {
                // 对象创建保持当前登录人为owner，后续连接和关系属性刷写使用后台上下文。
                customerPartsId = FrameworkUtil.autoName(context, "type_JFCustomerParts", "policy_JFCustomerParts");
            }
            ContextUtil.pushContext(context);
            isPush = true;
            if (UIUtil.isNullOrEmpty(relId)) {
                relId = connectEBOMCustomerPartsDBObject(context, partId, projectId, customerPartsId);
            }
            DomainRelationship.newInstance(context, relId).setAttributeValue(context, attrName, newValue);
            ContextUtil.commitTransaction(context);
        } catch (Exception e) {
            ContextUtil.abortTransaction(context);
            throw e;
        } finally {
            if (isPush) {
                ContextUtil.popContext(context);
            }
        }
    }

    /**
     * 判断EBOM客户零件号/DB列是否可编辑
     * @param context 上下文
     * @param partId 零件ID
     * @author LIUJR
     * @throws Exception 异常
     * @return boolean 是否可编辑
     * @date 2026/7/17
     * @description 仅工作中、当前登录人为零件Owner且未进入DR/标准件发布流程时可编辑所属项目数据。
     */
    private boolean hasEBOMCustomerPartsDBEditAccess(Context context, String partId) throws Exception {
        if (UIUtil.isNullOrEmpty(partId)) {
            return false;
        }
        StringList selectList = new StringList(DomainConstants.SELECT_CURRENT);
        selectList.add(DomainConstants.SELECT_OWNER);
        DomainObject partObject = DomainObject.newInstance(context, partId);
        Map partInfoMap = partObject.getInfo(context, selectList);
        String current = UIUtil.getValue(partInfoMap, DomainConstants.SELECT_CURRENT);
        String owner = UIUtil.getValue(partInfoMap, DomainConstants.SELECT_OWNER);
        //20260817 update by Codex EBOM客户零件号/DB列对应所属项目，进入流程后必须只读。
        return "IN_WORK".equalsIgnoreCase(current)
                && context.getUser().equals(owner)
                && !isCustomerPartsDBInReleaseProcess(context, partObject);
    }

    /**
     * 查询零件所属项目下的客户零件号/DB关系信息
     * @param context 上下文
     * @param partId 零件ID
     * @param projectId 项目ID
     * @author LIUJR
     * @throws Exception 异常
     * @return java.util.Map 客户零件号/DB关系信息
     * @date 2026/7/17
     * @description 同一个零件按项目维护客户零件号/DB信息，因此读取和更新都以所属项目为匹配条件。
     */
    private Map getEBOMCustomerPartsDBRelationInfo(Context context, String partId, String projectId) throws Exception {
        if (UIUtil.isNullOrEmpty(partId) || UIUtil.isNullOrEmpty(projectId)) {
            return new HashMap();
        }
        String projectIdSelect = "frommid[" + RELATIONSHIP_JFVPMREFERENCE2CUSTOMERPARTS2PROJECT + "].to.id";
        StringList objectSelectList = JF_Util_mxJPO.basicBolistSel();
        StringList relSelectList = JF_Util_mxJPO.basicRellistSel();
        relSelectList.add(projectIdSelect);
        relSelectList.add(JF_PLMConstants_mxJPO.Select_Attr_JFCustomerPartNumber);
        relSelectList.add(JF_PLMConstants_mxJPO.Select_Attr_JFCustomerPartName);
        relSelectList.add(JF_PLMConstants_mxJPO.Select_Attr_JFCustomerPartRevision);
        relSelectList.add(JF_PLMConstants_mxJPO.Select_Attr_JF_DirectBuy);
        DomainObject partObject = DomainObject.newInstance(context, partId);
        MapList relatedList = partObject.getRelatedObjects(context,
                RELATIONSHIP_JFVPMREFERENCE2CUSTOMERPARTS,
                TYPE_JFCUSTOMERPARTS,
                objectSelectList,
                relSelectList,
                true,
                false,
                (short) 1,
                "",
                "",
                0);
        for (int i = 0; i < relatedList.size(); i++) {
            Map relatedMap = (Map) relatedList.get(i);
            if (projectId.equals(UIUtil.getValue(relatedMap, projectIdSelect))) {
                return relatedMap;
            }
        }
        return new HashMap();
    }

    /**
     * 创建EBOM客户零件号/DB对象与零件、项目的关系
     * @param context 上下文
     * @param partId 零件ID
     * @param projectId 项目ID
     * @param customerPartsId 客户零件号/DB对象ID
     * @author LIUJR
     * @throws Exception 异常
     * @return java.lang.String 零件与客户零件号/DB关系ID
     * @date 2026/7/17
     * @description 创建主关系后，再在主关系上通过fromrel关联所属项目。
     */
    private String connectEBOMCustomerPartsDBObject(Context context, String partId, String projectId, String customerPartsId) throws Exception {
        DomainObject customerPartsObject = DomainObject.newInstance(context, customerPartsId);
        DomainRelationship relationshipObject = customerPartsObject.addToObject(context,
                new RelationshipType(RELATIONSHIP_JFVPMREFERENCE2CUSTOMERPARTS),
                partId);
        String relId = relationshipObject.getPhysicalId(context);
        MqlUtil.mqlCommand(context,
                "add connection $1 fromrel $2 to $3 select $4 dump",
                RELATIONSHIP_JFVPMREFERENCE2CUSTOMERPARTS2PROJECT,
                relId,
                projectId,
                "id");
        return relId;
    }

    /**
     * 根据EBOM表列名获取客户零件号/DB关系属性
     * @param columnName 表列名
     * @author LIUJR
     * @return java.lang.String 关系属性名
     * @date 2026/7/17
     * @description 新增EBOM列使用独立列名，保存时映射到客户零件号/DB关系属性。
     */
    private String getEBOMCustomerPartsDBAttribute(String columnName) {
        if ("CustomerPartsDBRelDirectBuy".equals(columnName) || "CustomerPartsDBDirectBuy".equals(columnName)) {
            return JF_PLMConstants_mxJPO.Attr_JF_DirectBuy;
        }
        if ("CustomerPartsDBRelNumber".equals(columnName) || "CustomerPartsDBNumber".equals(columnName)) {
            return JF_PLMConstants_mxJPO.Attr_JFCustomerPartNumber;
        }
        if ("CustomerPartsDBRelName".equals(columnName) || "CustomerPartsDBName".equals(columnName)) {
            return JF_PLMConstants_mxJPO.Attr_JFCustomerPartName;
        }
        if ("CustomerPartsDBRelRevision".equals(columnName) || "CustomerPartsDBRevision".equals(columnName)) {
            return JF_PLMConstants_mxJPO.Attr_JFCustomerPartRevision;
        }
        return DomainConstants.EMPTY_STRING;
    }

    /**
     * EBOM编辑保存后统一处理客户零件号/DB列数据
     * @param context 上下文
     * @param args 表格保存参数
     * @author LIUJR
     * @throws Exception 异常
     * @return java.util.Map post处理返回值
     * @date 2026/7/17
     * @description 这四列不走单元格Update Function，统一在post中处理，避免第一次保存时关系还不存在导致Update Function无法刷写。
     *              处理场景：
     *              1、只修改客户零件号/DB属性：按当前所属项目定位已有主关系，只刷写主关系属性；
     *              2、只修改所属项目：新项目已有主关系时直接使用目标数据，否则把旧主关系切换到新项目；
     *              3、同时修改所属项目和客户零件号/DB属性：新项目已有主关系时更新目标关系，否则迁移旧主关系后刷写属性；
     *              4、首次维护所属项目和客户零件号/DB属性：旧项目为空、原主关系为空，创建客户零件号/DB对象、主关系、项目关系并刷写属性；
     *              5、只维护所属项目但客户零件号/DB属性为空：不创建客户零件号/DB对象和关系。
     */
    @com.matrixone.apps.framework.ui.PostProcessCallable
    public Map postProcessEBOMCustomerPartsDB(Context context, String[] args) throws Exception {
        Map returnMap = new HashMap();
        boolean needRefresh = false;
        try {
            Map programMap = (Map) JPO.unpackArgs(args);
            com.matrixone.jdom.Document xmlDoc = (com.matrixone.jdom.Document) programMap.get("XMLDoc");
            if (xmlDoc == null) {
                return returnMap;
            }

            // post只处理本次保存XML中的所属项目列和客户零件号/DB列，普通EBOM字段仍按原有逻辑保存。
            com.matrixone.jdom.Element rootElement = xmlDoc.getRootElement();
            List objectElementList = rootElement.getChildren("object");
            for (int i = 0; i < objectElementList.size(); i++) {
                com.matrixone.jdom.Element objectElement = (com.matrixone.jdom.Element) objectElementList.get(i);
                String partId = objectElement.getAttributeValue("objectId");
                if (UIUtil.isNullOrEmpty(partId)) {
                    partId = objectElement.getAttributeValue("id");
                }
                if (UIUtil.isNullOrEmpty(partId)) {
                    continue;
                }

                Map changedValueMap = getEBOMCustomerPartsDBChangedValuesFromXML(objectElement);
                Map projectChangeMap = getEBOMCustomerPartsDBProjectChangeFromXML(context, partId, objectElement);
                boolean isProjectChanged = Boolean.TRUE.equals(projectChangeMap.get("changed"));
                if (changedValueMap.isEmpty() && !isProjectChanged) {
                    // 本行既没有客户零件号/DB属性变更，也没有所属项目变更，post不参与处理。
                    continue;
                }

                // 编辑权限沿用列上的规则：工作中、当前登录人为零件Owner且未进入DR/标准件发布流程。
                if (!hasEBOMCustomerPartsDBEditAccess(context, partId)) {
                    continue;
                }

                // 项目列变更后，updateProjectRel通常已经把零件所属项目切到新项目；
                // 因此这里的新项目优先取当前零件所属项目，旧项目保留用于定位原来的客户零件号/DB主关系。
                String projectId = (String) projectChangeMap.get("newProjectId");
                String oldProjectValue = (String) projectChangeMap.get("oldProjectValue");
                if (UIUtil.isNullOrEmpty(projectId)) {
                    projectId = getPartBelongProject(context, new String[]{partId, DomainConstants.SELECT_ID});
                }
                if (UIUtil.isNullOrEmpty(projectId) && !isProjectChanged) {
                    // 未维护所属项目时，不创建客户零件号/DB；前端OnFocus已经提示，这里只做保存兜底。
                    continue;
                }

                // 项目变更时必须先查新项目：
                // 1、新项目已有客户零件号/DB主关系时直接使用目标关系，旧项目关系保持不变；
                // 2、新项目没有主关系时，再用XML旧项目值定位原主关系并迁移项目端关系；
                // 3、新旧项目都没有主关系且本次维护了客户零件号/DB属性时，后续进入创建逻辑。
                Map customerPartsDBMap = new HashMap();
                boolean isTargetProjectRelation = false;
                if (isProjectChanged && UIUtil.isNotNullAndNotEmpty(projectId)) {
                    customerPartsDBMap = getEBOMCustomerPartsDBRelationInfo(context, partId, projectId);
                    isTargetProjectRelation = UIUtil.isNotNullAndNotEmpty(
                            UIUtil.getValue(customerPartsDBMap, DomainRelationship.SELECT_ID));
                }
                if (isProjectChanged && !isTargetProjectRelation) {
                    customerPartsDBMap = getEBOMCustomerPartsDBRelationInfoByProjectValue(context, partId, oldProjectValue);
                } else if (!isProjectChanged) {
                    customerPartsDBMap = getEBOMCustomerPartsDBRelationInfo(context, partId, projectId);
                }
                String relId = UIUtil.getValue(customerPartsDBMap, DomainRelationship.SELECT_ID);
                if (UIUtil.isNullOrEmpty(relId) && !hasEBOMCustomerPartsDBValue(changedValueMap)) {
                    // 没有原主关系，且本次客户零件号/DB四列都没有实际内容时，不创建客户零件号/DB对象和关系。
                    continue;
                }
                if (UIUtil.isNullOrEmpty(relId) && UIUtil.isNullOrEmpty(projectId)) {
                    // 没有原主关系且当前无所属项目时，不创建孤立客户零件号/DB数据。
                    continue;
                }

                ContextUtil.startTransaction(context, true);
                boolean isPush = false;
                try {
                    String customerPartsId = UIUtil.getValue(customerPartsDBMap, DomainConstants.SELECT_ID);

                    // 进入事务后再次确认目标项目是否已经存在主关系，避免并发或前置Update Function造成重复创建。
                    if (UIUtil.isNotNullAndNotEmpty(projectId)
                            && ((isProjectChanged && !isTargetProjectRelation) || UIUtil.isNullOrEmpty(relId))) {
                        Map latestTargetMap = getEBOMCustomerPartsDBRelationInfo(context, partId, projectId);
                        String latestTargetRelId = UIUtil.getValue(latestTargetMap, DomainRelationship.SELECT_ID);
                        if (UIUtil.isNotNullAndNotEmpty(latestTargetRelId)) {
                            customerPartsDBMap = latestTargetMap;
                            relId = latestTargetRelId;
                            customerPartsId = UIUtil.getValue(latestTargetMap, DomainConstants.SELECT_ID);
                            isTargetProjectRelation = true;
                        }
                    }
                    boolean isNewCustomerPartsDB = UIUtil.isNullOrEmpty(relId);
                    if (isNewCustomerPartsDB) {
                        // 首次维护所属项目和客户零件号/DB时会走到这里：
                        // 对象创建使用当前登录人上下文，保证owner仍为维护人；连接关系和关系属性刷写再切后台上下文。
                        customerPartsId = FrameworkUtil.autoName(context, "type_JFCustomerParts", "policy_JFCustomerParts");
                    }
                    ContextUtil.pushContext(context);
                    isPush = true;
                    if (isNewCustomerPartsDB) {
                        // 新建客户零件号/DB对象后，创建零件->客户零件号/DB主关系，再在主关系上挂所属项目关系。
                        relId = connectEBOMCustomerPartsDBObject(context, partId, projectId, customerPartsId);
                    } else if (isProjectChanged && !isTargetProjectRelation) {
                        // 只有目标项目不存在客户零件号/DB数据时，才把旧主关系的项目端切换到新项目。
                        updateEBOMCustomerPartsDBProjectRelation(context, relId, projectId);
                    }
                    if (!changedValueMap.isEmpty()) {
                        // 只有本次客户零件号/DB四列有变更时才刷写属性，避免覆盖未编辑字段。
                        DomainRelationship.newInstance(context, relId).setAttributeValues(context, changedValueMap);
                    }
                    boolean isDirectBuyChanged = changedValueMap.containsKey(JF_PLMConstants_mxJPO.Attr_JF_DirectBuy);
                    if (isNewCustomerPartsDB || isDirectBuyChanged) {
                        //20260903 update by liujr 新建所属项目DB信息或修改DirectBuy时，直接同步到零件属性。
                        String directBuy = isDirectBuyChanged
                                ? UIUtil.getValue(changedValueMap, JF_PLMConstants_mxJPO.Attr_JF_DirectBuy)
                                : "";
                        DomainObject.newInstance(context, partId).setAttributeValue(
                                context,
                                JF_PLMConstants_mxJPO.ATTR_JFDIRECT_BUY,
                                directBuy);
                    }
                    ContextUtil.commitTransaction(context);
                    needRefresh = true;
                } catch (Exception e) {
                    ContextUtil.abortTransaction(context);
                    throw e;
                } finally {
                    if (isPush) {
                        ContextUtil.popContext(context);
                    }
                }
            }
        } catch (Exception e) {
            log.error("postProcessEBOMCustomerPartsDB error", e);
            throw e;
        }

        if (needRefresh) {
            //20260902 update by caipan 使用Structure Browser原生refresh清理保存状态，避免关闭编辑模式时仍提示存在未保存修改。
            returnMap.put("Action", "refresh");
        }
        return returnMap;
    }

    /**
     * 从保存XML中提取客户零件号/DB四列的修改值
     * @param objectElement 保存XML中的对象节点
     * @author LIUJR
     * @return java.util.Map 关系属性和值，key为关系属性名称，value为本次保存的新值
     * @date 2026/7/17
     * @description 只提取当前行本次被修改的客户零件号/DB四列，避免post保存时覆盖未编辑字段。
     *              所属项目列不放入该Map，项目变更由getEBOMCustomerPartsDBProjectChangeFromXML单独处理。
     */
    private Map getEBOMCustomerPartsDBChangedValuesFromXML(com.matrixone.jdom.Element objectElement) {
        Map changedValueMap = new LinkedHashMap();
        List columnElementList = objectElement.getChildren("column");
        for (int i = 0; i < columnElementList.size(); i++) {
            com.matrixone.jdom.Element columnElement = (com.matrixone.jdom.Element) columnElementList.get(i);
            String columnName = getEBOMCustomerPartsDBXMLColumnName(columnElement);
            String attrName = getEBOMCustomerPartsDBAttribute(columnName);
            if (UIUtil.isNullOrEmpty(attrName) || !isEBOMCustomerPartsDBChangedColumn(columnElement)) {
                continue;
            }
            changedValueMap.put(attrName, getEBOMCustomerPartsDBXMLValue(columnElement));
        }
        return changedValueMap;
    }

    /**
     * 判断XML列节点是否为本次保存修改列
     * @param columnElement 保存XML中的列节点
     * @author LIUJR
     * @return boolean 是否为本次保存发生变化的列
     * @date 2026/7/17
     * @description 兼容表格XML中常见的edited、changed、modified、status标识；
     *              如果XML同时带oldValue和newValue，也会通过新旧值差异判断该列是否修改。
     */
    private boolean isEBOMCustomerPartsDBChangedColumn(com.matrixone.jdom.Element columnElement) {
        String edited = columnElement.getAttributeValue("edited");
        String changed = columnElement.getAttributeValue("changed");
        String modified = columnElement.getAttributeValue("modified");
        String columnStatus = columnElement.getAttributeValue("status");
        String[] oldValueNameArray = new String[]{
                "oldValue",
                "oldActualValue",
                "oldDisplayValue",
                "oldOID",
                "oldOid",
                "oldObjectId",
                "oldObjectID",
                "oldId",
                "oldID"};
        String[] newValueNameArray = new String[]{
                "newValue",
                "value",
                "actualValue",
                "displayValue",
                "newOID",
                "newOid",
                "newObjectId",
                "newObjectID",
                "newId",
                "newID"};
        String oldValue = getEBOMCustomerPartsDBXMLFirstValue(columnElement, oldValueNameArray);
        String newValue = getEBOMCustomerPartsDBXMLFirstValue(columnElement, newValueNameArray);
        return "true".equalsIgnoreCase(edited)
                || "true".equalsIgnoreCase(changed)
                || "true".equalsIgnoreCase(modified)
                || "changed".equalsIgnoreCase(columnStatus)
                || (hasEBOMCustomerPartsDBXMLValueName(columnElement, oldValueNameArray)
                        && hasEBOMCustomerPartsDBXMLValueName(columnElement, newValueNameArray)
                        && !oldValue.equals(newValue));
    }

    /**
     * 获取保存XML中的列新值
     * @param columnElement 保存XML中的列节点
     * @author LIUJR
     * @return java.lang.String 列新值
     * @date 2026/7/17
     * @description 不同版本表格XML可能使用newValue、value或节点文本存放新值，这里按优先级统一兼容。
     */
    private String getEBOMCustomerPartsDBXMLValue(com.matrixone.jdom.Element columnElement) {
        String value = columnElement.getAttributeValue("newValue");
        if (value == null) {
            value = columnElement.getAttributeValue("value");
        }
        if (value == null) {
            value = columnElement.getText();
        }
        return value == null ? DomainConstants.EMPTY_STRING : value;
    }

    /**
     * 判断客户零件号/DB修改值是否有实际内容
     * @param changedValueMap 本次修改的关系属性和值
     * @author LIUJR
     * @return boolean 是否存在有效客户零件号/DB值
     * @date 2026/7/17
     * @description 用于判断是否需要创建客户零件号/DB对象：
     *              1、四列都为空时不创建；
     *              2、任意一列有值时认为本次维护了客户零件号/DB信息，可以创建对象和关系；
     *              3、DirectBuy为non-DB也属于客户零件号/DB维护值，是否显示到页签由页签查询条件控制。
     */
    private boolean hasEBOMCustomerPartsDBValue(Map changedValueMap) {
        for (Object valueObject : changedValueMap.values()) {
            String value = valueObject == null ? DomainConstants.EMPTY_STRING : String.valueOf(valueObject);
            if (UIUtil.isNotNullAndNotEmpty(value.trim())) {
                return true;
            }
        }
        return false;
    }

    /**
     * 从保存XML中提取所属项目变更信息
     * @param context 上下文
     * @param partId 零件ID
     * @param objectElement 保存XML中的对象节点
     * @author LIUJR
     * @throws Exception 异常
     * @return java.util.Map 所属项目变更信息，包含changed、oldProjectValue、newProjectId三个key
     * @date 2026/7/20
     * @description 项目列变更由ProjectRel列的Update Function先处理零件所属项目关系；
     *              post中再读取XML旧项目值定位原客户零件号/DB主关系，并读取当前零件所属项目作为新项目。
     *              如果旧项目为空，说明可能是首次维护所属项目；此时不会定位到原主关系，后续会根据客户零件号/DB属性是否有值决定是否创建新对象和关系。
     */
    private Map getEBOMCustomerPartsDBProjectChangeFromXML(Context context, String partId, com.matrixone.jdom.Element objectElement) throws Exception {
        Map projectChangeMap = new HashMap();
        projectChangeMap.put("changed", Boolean.FALSE);
        projectChangeMap.put("oldProjectValue", DomainConstants.EMPTY_STRING);
        projectChangeMap.put("newProjectId", DomainConstants.EMPTY_STRING);
        List columnElementList = objectElement.getChildren("column");
        for (int i = 0; i < columnElementList.size(); i++) {
            com.matrixone.jdom.Element columnElement = (com.matrixone.jdom.Element) columnElementList.get(i);
            String columnName = getEBOMCustomerPartsDBXMLColumnName(columnElement);
            if (!isEBOMCustomerPartsDBProjectColumn(columnName) || !isEBOMCustomerPartsDBChangedColumn(columnElement)) {
                continue;
            }
            String oldProjectValue = getEBOMCustomerPartsDBXMLFirstValue(columnElement, new String[]{
                    "oldOID",
                    "oldOid",
                    "oldObjectId",
                    "oldObjectID",
                    "oldId",
                    "oldID",
                    "oldValue"});
            String currentProjectId = getPartBelongProject(context, new String[]{partId, DomainConstants.SELECT_ID});
            String newProjectId = UIUtil.isNotNullAndNotEmpty(currentProjectId)
                    ? currentProjectId
                    : getEBOMCustomerPartsDBXMLFirstValue(columnElement, new String[]{
                            "newOID",
                            "newOid",
                            "newObjectId",
                            "newObjectID",
                            "newId",
                            "newID",
                            "newValue",
                            "value"});
            projectChangeMap.put("changed", Boolean.TRUE);
            projectChangeMap.put("oldProjectValue", oldProjectValue);
            projectChangeMap.put("newProjectId", newProjectId);
            return projectChangeMap;
        }
        return projectChangeMap;
    }

    /**
     * 按项目旧值查询客户零件号/DB主关系信息
     * @param context 上下文
     * @param partId 零件ID
     * @param projectValue 项目旧值，可能是ID、物理ID、名称或描述
     * @author LIUJR
     * @throws Exception 异常
     * @return java.util.Map 客户零件号/DB关系信息，未匹配到时返回空Map
     * @date 2026/7/20
     * @description 零件可能存在多条客户零件号/DB主关系，项目变更时必须用旧项目定位原主关系，不能直接取第一条。
     *              projectValue可能来自XML中的oldOID、oldValue或前端显示文本，因此同时按项目id、physicalid、name、description做兼容匹配。
     */
    private Map getEBOMCustomerPartsDBRelationInfoByProjectValue(Context context, String partId, String projectValue) throws Exception {
        if (UIUtil.isNullOrEmpty(partId) || UIUtil.isNullOrEmpty(projectValue)) {
            return new HashMap();
        }
        String projectIdSelect = "frommid[" + RELATIONSHIP_JFVPMREFERENCE2CUSTOMERPARTS2PROJECT + "].to.id";
        String projectPhysicalIdSelect = "frommid[" + RELATIONSHIP_JFVPMREFERENCE2CUSTOMERPARTS2PROJECT + "].to.physicalid";
        String projectNameSelect = "frommid[" + RELATIONSHIP_JFVPMREFERENCE2CUSTOMERPARTS2PROJECT + "].to.name";
        String projectDescriptionSelect = "frommid[" + RELATIONSHIP_JFVPMREFERENCE2CUSTOMERPARTS2PROJECT + "].to.description";
        StringList objectSelectList = JF_Util_mxJPO.basicBolistSel();
        StringList relSelectList = JF_Util_mxJPO.basicRellistSel();
        relSelectList.add(projectIdSelect);
        relSelectList.add(projectPhysicalIdSelect);
        relSelectList.add(projectNameSelect);
        relSelectList.add(projectDescriptionSelect);
        relSelectList.add(JF_PLMConstants_mxJPO.Select_Attr_JFCustomerPartNumber);
        relSelectList.add(JF_PLMConstants_mxJPO.Select_Attr_JFCustomerPartName);
        relSelectList.add(JF_PLMConstants_mxJPO.Select_Attr_JFCustomerPartRevision);
        relSelectList.add(JF_PLMConstants_mxJPO.Select_Attr_JF_DirectBuy);
        DomainObject partObject = DomainObject.newInstance(context, partId);
        MapList relatedList = partObject.getRelatedObjects(context,
                RELATIONSHIP_JFVPMREFERENCE2CUSTOMERPARTS,
                TYPE_JFCUSTOMERPARTS,
                objectSelectList,
                relSelectList,
                true,
                false,
                (short) 1,
                "",
                "",
                0);
        for (int i = 0; i < relatedList.size(); i++) {
            Map relatedMap = (Map) relatedList.get(i);
            if (isSameEBOMCustomerPartsDBProjectValue(projectValue, relatedMap.get(projectIdSelect))
                    || isSameEBOMCustomerPartsDBProjectValue(projectValue, relatedMap.get(projectPhysicalIdSelect))
                    || isSameEBOMCustomerPartsDBProjectValue(projectValue, relatedMap.get(projectNameSelect))
                    || isSameEBOMCustomerPartsDBProjectValue(projectValue, relatedMap.get(projectDescriptionSelect))) {
                return relatedMap;
            }
        }
        return new HashMap();
    }

    /**
     * 更新客户零件号/DB主关系上的所属项目关系
     * @param context 上下文
     * @param relId 零件与客户零件号/DB主关系ID
     * @param newProjectId 新所属项目ID
     * @author LIUJR
     * @throws Exception 异常
     * @return void
     * @date 2026/7/20
     * @description 主关系保持不变，只替换挂在主关系上的JFVPMReference2CustomerParts2Project关系。
     *              用于“客户零件号/DB不变、所属项目切换”的场景，避免创建重复客户零件号/DB对象或误改其它主关系。
     */
    private void updateEBOMCustomerPartsDBProjectRelation(Context context, String relId, String newProjectId) throws Exception {
        if (UIUtil.isNullOrEmpty(relId)) {
            return;
        }
        String oldProjectRelIds = MqlUtil.mqlCommand(context,
                "print connection $1 select $2 dump $3",
                relId,
                "frommid[" + RELATIONSHIP_JFVPMREFERENCE2CUSTOMERPARTS2PROJECT + "].id",
                "|");
        if (UIUtil.isNotNullAndNotEmpty(oldProjectRelIds)) {
            StringList oldProjectRelIdList = FrameworkUtil.split(oldProjectRelIds, "|");
            for (int i = 0; i < oldProjectRelIdList.size(); i++) {
                String oldProjectRelId = (String) oldProjectRelIdList.get(i);
                if (UIUtil.isNotNullAndNotEmpty(oldProjectRelId)) {
                    DomainRelationship.disconnect(context, oldProjectRelId);
                }
            }
        }
        if (UIUtil.isNotNullAndNotEmpty(newProjectId)) {
            MqlUtil.mqlCommand(context,
                    "add connection $1 fromrel $2 to $3 select $4 dump",
                    RELATIONSHIP_JFVPMREFERENCE2CUSTOMERPARTS2PROJECT,
                    relId,
                    newProjectId,
                    "id");
        }
    }

    /**
     * 获取保存XML列节点中的列名
     * @param columnElement 保存XML中的列节点
     * @author LIUJR
     * @return java.lang.String 列名
     * @date 2026/7/20
     * @description 不同版本表格XML中列名可能放在name、columnName、id等不同属性中，这里统一取值，避免post识别不到ProjectRel或客户零件号/DB列。
     */
    private String getEBOMCustomerPartsDBXMLColumnName(com.matrixone.jdom.Element columnElement) {
        return getEBOMCustomerPartsDBXMLFirstValue(columnElement, new String[]{
                "name",
                "columnName",
                "column",
                "columnId",
                "columnID",
                "id",
                "attrName",
                "attribute",
                "adminName",
                "fieldName"});
    }

    /**
     * 判断保存XML列是否为所属项目列
     * @param columnName XML列名
     * @author LIUJR
     * @return boolean 是否所属项目列
     * @date 2026/7/20
     * @description ProjectRel列在XML中可能使用列名、属性名或Admin Type保存，统一兼容判断。
     */
    private boolean isEBOMCustomerPartsDBProjectColumn(String columnName) {
        if (UIUtil.isNullOrEmpty(columnName)) {
            return false;
        }
        String normalColumnName = columnName.trim();
        return "ProjectRel".equals(normalColumnName)
                || "attribute_JF_VPMReference.JF_ProjectRel".equals(normalColumnName)
                || JF_PLMConstants_mxJPO.ATTR_JF_ProjectRel.equals(normalColumnName)
                || normalColumnName.indexOf("JF_ProjectRel") > -1;
    }

    /**
     * 判断XML列节点是否存在指定名称的值
     * @param columnElement 保存XML中的列节点
     * @param nameArray 候选属性名或子节点名
     * @author LIUJR
     * @return boolean 是否存在指定字段
     * @date 2026/7/20
     * @description 用于区分“XML没有旧值字段”和“XML带了旧值字段但旧值为空”的情况，首次维护所属项目时旧值为空也应识别为变更。
     */
    private boolean hasEBOMCustomerPartsDBXMLValueName(com.matrixone.jdom.Element columnElement, String[] nameArray) {
        for (int i = 0; i < nameArray.length; i++) {
            if (columnElement.getAttributeValue(nameArray[i]) != null) {
                return true;
            }
        }
        List attributeList = columnElement.getAttributes();
        for (int i = 0; i < nameArray.length; i++) {
            for (int j = 0; j < attributeList.size(); j++) {
                com.matrixone.jdom.Attribute attribute = (com.matrixone.jdom.Attribute) attributeList.get(j);
                if (nameArray[i].equalsIgnoreCase(attribute.getName())) {
                    return true;
                }
            }
        }
        List childElementList = columnElement.getChildren();
        for (int i = 0; i < nameArray.length; i++) {
            for (int j = 0; j < childElementList.size(); j++) {
                com.matrixone.jdom.Element childElement = (com.matrixone.jdom.Element) childElementList.get(j);
                if (nameArray[i].equalsIgnoreCase(childElement.getName())) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 从XML列节点中按名称顺序取值
     * @param columnElement 保存XML中的列节点
     * @param nameArray 候选属性名或子节点名
     * @author LIUJR
     * @return java.lang.String XML中第一个有效值
     * @date 2026/7/20
     * @description 兼容不同版本表格XML中OID、新旧值字段名称不一致的情况。
     *              先按属性精确取值，再忽略大小写匹配属性名，最后匹配同名子节点文本。
     */
    private String getEBOMCustomerPartsDBXMLFirstValue(com.matrixone.jdom.Element columnElement, String[] nameArray) {
        for (int i = 0; i < nameArray.length; i++) {
            String value = columnElement.getAttributeValue(nameArray[i]);
            if (UIUtil.isNotNullAndNotEmpty(value)) {
                return value;
            }
        }
        List attributeList = columnElement.getAttributes();
        for (int i = 0; i < nameArray.length; i++) {
            for (int j = 0; j < attributeList.size(); j++) {
                com.matrixone.jdom.Attribute attribute = (com.matrixone.jdom.Attribute) attributeList.get(j);
                if (nameArray[i].equalsIgnoreCase(attribute.getName()) && UIUtil.isNotNullAndNotEmpty(attribute.getValue())) {
                    return attribute.getValue();
                }
            }
        }
        List childElementList = columnElement.getChildren();
        for (int i = 0; i < nameArray.length; i++) {
            for (int j = 0; j < childElementList.size(); j++) {
                com.matrixone.jdom.Element childElement = (com.matrixone.jdom.Element) childElementList.get(j);
                if (nameArray[i].equalsIgnoreCase(childElement.getName()) && UIUtil.isNotNullAndNotEmpty(childElement.getText())) {
                    return childElement.getText();
                }
            }
        }
        return DomainConstants.EMPTY_STRING;
    }

    /**
     * 判断两个项目值是否指向同一个项目
     * @param projectValue XML中的项目值
     * @param compareObject 关系select取到的项目值
     * @author LIUJR
     * @return boolean 是否匹配同一个项目值
     * @date 2026/7/20
     * @description select可能返回单值或列表，统一展开后比较。
     *              XML旧项目值可能是id、physicalid、name、description中的任意一种，调用方会分别传入这些select值进行匹配。
     */
    private boolean isSameEBOMCustomerPartsDBProjectValue(String projectValue, Object compareObject) {
        if (compareObject instanceof StringList) {
            StringList compareList = (StringList) compareObject;
            for (int i = 0; i < compareList.size(); i++) {
                if (isSameEBOMCustomerPartsDBProjectValue(projectValue, compareList.get(i))) {
                    return true;
                }
            }
            return false;
        }
        if (compareObject instanceof List) {
            List compareList = (List) compareObject;
            for (int i = 0; i < compareList.size(); i++) {
                if (isSameEBOMCustomerPartsDBProjectValue(projectValue, compareList.get(i))) {
                    return true;
                }
            }
            return false;
        }
        String normalProjectValue = normalizeEBOMCustomerPartsDBProjectValue(projectValue);
        String normalCompareValue = normalizeEBOMCustomerPartsDBProjectValue(compareObject);
        return UIUtil.isNotNullAndNotEmpty(normalProjectValue) && normalProjectValue.equals(normalCompareValue);
    }

    /**
     * 标准化项目比较值
     * @param valueObject 项目值
     * @author LIUJR
     * @return java.lang.String 标准化后的项目比较值
     * @date 2026/7/20
     * @description 去掉前端HTML展示标签和首尾空格，避免用项目描述、带链接HTML的显示值定位旧关系时匹配失败。
     */
    private String normalizeEBOMCustomerPartsDBProjectValue(Object valueObject) {
        if (valueObject == null) {
            return DomainConstants.EMPTY_STRING;
        }
        return String.valueOf(valueObject).replaceAll("<[^>]*>", DomainConstants.EMPTY_STRING).trim();
    }

    /**
     * 所属项目列保存时同步客户零件号/DB项目端关系
     * @param context 上下文
     * @param partId 零件ID
     * @param oldPartProjectRelId 零件原所属项目关系ID
     * @param newProjectValue 新所属项目值
     * @author LIUJR
     * @throws Exception 异常
     * @return void
     * @date 2026/7/20
     * @description ProjectRel列的Update Function会先于post执行，并且会断开零件原所属项目关系；
     *              因此这里在断开旧关系前，用旧所属项目关系ID取得旧项目，再定位旧项目下的客户零件号/DB主关系。
     *              新项目已有客户零件号/DB主关系时不迁移旧关系，避免同一项目出现两条客户零件号/DB数据；
     *              新项目没有主关系时，才替换旧主关系上的JFVPMReference2CustomerParts2Project项目端关系。
     */
    private void syncEBOMCustomerPartsDBProjectRelationAfterProjectRelUpdate(Context context, String partId, String oldPartProjectRelId, String newProjectValue) throws Exception {
        if (UIUtil.isNullOrEmpty(partId) || UIUtil.isNullOrEmpty(oldPartProjectRelId)) {
            return;
        }
        String oldProjectId = MqlUtil.mqlCommand(context,
                "print connection $1 select $2 dump",
                oldPartProjectRelId,
                "from.id");
        if (UIUtil.isNullOrEmpty(oldProjectId)) {
            return;
        }
        String newProjectId = DomainConstants.EMPTY_STRING;
        if (UIUtil.isNotNullAndNotEmpty(newProjectValue)) {
            newProjectId = DomainObject.newInstance(context, newProjectValue).getInfo(context, DomainConstants.SELECT_ID);
        }

        // 新项目已有客户零件号/DB数据时直接使用目标项目数据，旧项目数据继续保留在原项目。
        if (UIUtil.isNotNullAndNotEmpty(newProjectId)) {
            Map targetCustomerPartsDBMap = getEBOMCustomerPartsDBRelationInfo(context, partId, newProjectId);
            String targetCustomerPartsRelId = UIUtil.getValue(targetCustomerPartsDBMap, DomainRelationship.SELECT_ID);
            if (UIUtil.isNotNullAndNotEmpty(targetCustomerPartsRelId)) {
                return;
            }
        }

        Map customerPartsDBMap = getEBOMCustomerPartsDBRelationInfo(context, partId, oldProjectId);
        String customerPartsRelId = UIUtil.getValue(customerPartsDBMap, DomainRelationship.SELECT_ID);
        if (UIUtil.isNullOrEmpty(customerPartsRelId)) {
            return;
        }
        updateEBOMCustomerPartsDBProjectRelation(context, customerPartsRelId, newProjectId);
    }

    /**
     * 获取客户零件号/DB编辑表单所属项目字段的编辑权限
     * @param context 上下文
     * @param args 表单字段参数
     * @author LIUJR
     * @throws Exception 异常
     * @return boolean 所属项目字段是否可编辑
     * @date 2026/7/20
     * @description 当前行关联项目为零件所属项目时，所属项目字段只读，但不影响客户零件号等其他字段编辑；
     *              当前行关联其他项目时，允许通过项目搜索重新选择项目。
     */
    public boolean getCustomerPartsDBProjectEditAccess(Context context, String[] args) throws Exception {
        Map formMap = JPO.unpackArgs(args);
        Map requestMap = (Map) formMap.get("requestMap");
        String partId = getRequestValue(requestMap, "parentOID");
        String relId = getRequestValue(requestMap, "relId");
        if (UIUtil.isNullOrEmpty(partId) || UIUtil.isNullOrEmpty(relId)) {
            return false;
        }

        String partProjectId = getPartBelongProject(context, new String[]{partId, DomainConstants.SELECT_ID});
        if (UIUtil.isNullOrEmpty(partProjectId)) {
            return true;
        }
        String rowProjectIds = MqlUtil.mqlCommand(context,
                "print connection $1 select $2 dump $3",
                relId,
                "frommid[" + RELATIONSHIP_JFVPMREFERENCE2CUSTOMERPARTS2PROJECT + "].to.id",
                "|");
        if (UIUtil.isNullOrEmpty(rowProjectIds)) {
            return true;
        }
        return !FrameworkUtil.split(rowProjectIds, "|").contains(partProjectId);
    }

    /**
     * 按页面项目上下文显示零件DirectBuy
     **
     * @param context 上下文
     * @param args Table列参数
     * @return Vector 每行DirectBuy显示值
     * @throws Exception
     * @author LIUJR
     * @date 2026/7/24
     */
    public Vector getDirectBuyByProject(Context context, String[] args) throws Exception {
        Map programMap = JPO.unpackArgs(args);
        MapList objectList = (MapList) programMap.get("objectList");
        Vector result = new Vector();
        if (objectList == null || objectList.isEmpty()) {
            return result;
        }

        Map columnMap = (Map) programMap.get("columnMap");
        Map settingMap = columnMap == null ? null : (Map) columnMap.get("settings");
        String projectSource = settingMap == null
                ? DomainConstants.EMPTY_STRING
                : UIUtil.getValue(settingMap, "DirectBuy Project Source");
        if (UIUtil.isNullOrEmpty(projectSource)) {
            projectSource = "Legacy";
        }

        Map paramList = (Map) programMap.get("paramList");
        String contextObjectId = getRequestValue(paramList, "objectId");
        if (UIUtil.isNullOrEmpty(contextObjectId)) {
            contextObjectId = getRequestValue(paramList, "parentOID");
        }

        // 共享Table按当前页面根对象识别项目来源；未命中本次需求范围时保留原零件属性显示方式。
        if ("Auto".equalsIgnoreCase(projectSource)) {
            projectSource = "Legacy";
            if (UIUtil.isNotNullAndNotEmpty(contextObjectId)) {
                String contextType = DomainObject.newInstance(context, contextObjectId)
                        .getInfo(context, DomainConstants.SELECT_TYPE);
                if (DomainConstants.TYPE_PROJECT_SPACE.equals(contextType)) {
                    projectSource = "Project";
                } else if (TYPE_JFSnapshot.equals(contextType)) {
                    projectSource = "Snapshot";
                } else if (TYPE_JFPartList.equals(contextType)) {
                    projectSource = "PartList";
                } else if (TYPE_JFECR.equals(contextType)
                        || TYPE_JFNewECR.equals(contextType)
                        || TYPE_JFFormalECR.equals(contextType)) {
                    projectSource = "ECR";
                } else if (TYPE_JFS_PARTS_APPLICATION.equals(contextType)
                        || TYPE_VPMREFERENCE.equals(contextType)) {
                    projectSource = "Part";
                }else if ("JFProductConfigTable".equals(contextType)
                        || TYPE_VPMREFERENCE.equals(contextType)) {
                    projectSource = "JFProductConfigTable";
                }else if ("JFServicePartsList".equals(contextType)
                        || TYPE_VPMREFERENCE.equals(contextType)) {
                    projectSource = "JFServicePartsList";
                }
            }
        }

        String contextProjectId = DomainConstants.EMPTY_STRING;
        if ("Project".equalsIgnoreCase(projectSource)
                || "MBOM".equalsIgnoreCase(projectSource)) {
            contextProjectId = contextObjectId;
        } else if ("ECR".equalsIgnoreCase(projectSource) && UIUtil.isNotNullAndNotEmpty(contextObjectId)) {
            contextProjectId = DomainObject.newInstance(context, contextObjectId)
                    .getInfo(context, "from[" + REL_JFChange2Project + "].to.id");
        } else if ("Snapshot".equalsIgnoreCase(projectSource) && UIUtil.isNotNullAndNotEmpty(contextObjectId)) {
            contextProjectId = DomainObject.newInstance(context, contextObjectId)
                    .getInfo(context, "to[" + rel_JFProject2Snapshot + "].from.id");
        } else if ("PartList".equalsIgnoreCase(projectSource) && UIUtil.isNotNullAndNotEmpty(contextObjectId)) {
            contextProjectId = DomainObject.newInstance(context, contextObjectId)
                    .getInfo(context, "to[" + rel_JFProject2PartList + "].from.id");
        }else if ("JFProductConfigTable".equalsIgnoreCase(projectSource) && UIUtil.isNotNullAndNotEmpty(contextObjectId)) {
            contextProjectId = DomainObject.newInstance(context, contextObjectId)
                    .getInfo(context, "to[JFProject2ProductConfigTable].from.id");
        }else if ("JFServicePartsList".equalsIgnoreCase(projectSource) && UIUtil.isNotNullAndNotEmpty(contextObjectId)) {
            contextProjectId = DomainObject.newInstance(context, contextObjectId)
                    .getInfo(context, "to[JFProject2ServicePartsList].from.id");
            log.info("objectID:{}",contextObjectId);
            log.info("contextProjectId:{}",contextProjectId);
        }

        Map<String, String> projectCache = new HashMap<>();
        Map<String, String> directBuyCache = new HashMap<>();
        Map<String, String> mbomPartCache = new HashMap<>();
        for (int i = 0; i < objectList.size(); i++) {
            Map objectMap = (Map) objectList.get(i);
            String partId = UIUtil.getValue(objectMap, DomainConstants.SELECT_ID);

            //20260727 update by ljr MBOM对象上的non-DB可能是业务默认值，Table改查源零件项目关系，未维护时显示为空；
            if ("MBOM".equalsIgnoreCase(projectSource)) {
                String mbomPartNumber = UIUtil.getValue(objectMap, "attribute[JF_PartNumber]");
                String mbomRevision = UIUtil.getValue(objectMap, DomainConstants.SELECT_REVISION);
                if ((UIUtil.isNullOrEmpty(mbomPartNumber) || UIUtil.isNullOrEmpty(mbomRevision))
                        && UIUtil.isNotNullAndNotEmpty(partId)) {
                    StringList mbomSelectList = new StringList("attribute[JF_PartNumber]");
                    mbomSelectList.add(DomainConstants.SELECT_REVISION);
                    Map mbomInfo = DomainObject.newInstance(context, partId).getInfo(context, mbomSelectList);
                    mbomPartNumber = UIUtil.getValue(mbomInfo, "attribute[JF_PartNumber]");
                    mbomRevision = UIUtil.getValue(mbomInfo, DomainConstants.SELECT_REVISION);
                }
                if (UIUtil.isNullOrEmpty(mbomPartNumber) || UIUtil.isNullOrEmpty(mbomRevision)) {
                    result.add(DomainConstants.EMPTY_STRING);
                    continue;
                }
                String sourcePartCacheKey = mbomPartNumber + "|" + mbomRevision;
                String sourcePartId = mbomPartCache.get(sourcePartCacheKey);
                if (!mbomPartCache.containsKey(sourcePartCacheKey)) {
                    String partWhere = SELECT_ATTR_V_PART_NUMBER + "=='" + mbomPartNumber
                            + "'&&" + DomainConstants.SELECT_REVISION + "=='" + mbomRevision + "'";
                    MapList sourcePartList = DomainObject.findObjects(
                            context,
                            TYPE_VPMREFERENCE,
                            DomainConstants.QUERY_WILDCARD,
                            partWhere,
                            new StringList(DomainConstants.SELECT_ID));
                    sourcePartId = sourcePartList.isEmpty()
                            ? DomainConstants.EMPTY_STRING
                            : UIUtil.getValue((Map) sourcePartList.get(0), DomainConstants.SELECT_ID);
                    mbomPartCache.put(sourcePartCacheKey, sourcePartId);
                }
                String mbomDirectBuyCacheKey = sourcePartId + "|" + contextProjectId;
                String mbomDirectBuy = directBuyCache.get(mbomDirectBuyCacheKey);
                if (mbomDirectBuy == null) {
                    mbomDirectBuy = JF_Util_mxJPO.getPartDirectBuyByProject(
                            context,
                            sourcePartId,
                            contextProjectId);
                    directBuyCache.put(mbomDirectBuyCacheKey, mbomDirectBuy);
                }
                result.add(mbomDirectBuy);
                continue;
            }

            // 未纳入本次改造的共享Table继续读取原零件对象属性，避免扩大修改范围。
            if ("Legacy".equalsIgnoreCase(projectSource)) {
                String directBuy = UIUtil.getValue(objectMap, SELECT_ATTR_JFDIRECT_BUY);
                if (UIUtil.isNullOrEmpty(directBuy) && UIUtil.isNotNullAndNotEmpty(partId)) {
                    directBuy = DomainObject.newInstance(context, partId)
                            .getInfo(context, SELECT_ATTR_JFDIRECT_BUY);
                }
                result.add(directBuy);
                continue;
            }

            String projectId = contextProjectId;
            if ("Part".equalsIgnoreCase(projectSource)) {
                projectId = projectCache.get(partId);
                if (projectId == null) {
                    projectId = getPartBelongProject(context, new String[]{partId, DomainConstants.SELECT_ID});
                    projectCache.put(partId, projectId);
                }
            }
            String cacheKey = partId + "|" + projectId;
            String directBuy = directBuyCache.get(cacheKey);
            if (directBuy == null) {
                directBuy = JF_Util_mxJPO.getPartDirectBuyByProject(context, partId, projectId);
                directBuyCache.put(cacheKey, directBuy);
            }
            result.add(directBuy);
        }
        return result;
    }

    /**
     * 客户零件号/DB关系DirectBuy修改时同步零件是否缺失图纸
     **
     * @param context 上下文
     * @param args Trigger参数：关系ID、关系类型、from对象ID、to零件ID、属性名、原值、新值
     * @throws Exception 异常
     * @author caipan
     * @date 2026/8/12 10:59
     */
    public void setJF_IsThereALackOfDrawingsAttr(Context context,String[] args) throws Exception {
        //20260812 update by caipan 客户零件号/DB关系的DirectBuy变更时，同步零件是否缺失图纸标识
        if (args == null || args.length < 7 || !Attr_JF_DirectBuy.equals(args[4])) {
            return;
        }
        String partId = args[3];
        updatePartIsThereALackOfDrawings(context, partId);
    }

    /**
     * 图纸关系创建或删除时同步零件是否缺失图纸
     **
     * @param context 上下文
     * @param args Trigger参数：关系类型、from对象ID、to对象ID
     * @throws Exception 异常
     * @author caipan
     * @date 2026/8/12 15:30
     */
    public void updateJF_IsThereALackOfDrawingsByDrawingRelationship(Context context, String[] args) throws Exception {
        if (args == null || args.length < 3) {
            return;
        }
        String relationshipType = args[0];
        String fromObjectId = args[1];
        String toObjectId = args[2];
        DomainObject fromObject = DomainObject.newInstance(context, fromObjectId);
        DomainObject toObject = DomainObject.newInstance(context, toObjectId);
        String fromType = fromObject.getInfo(context, SELECT_TYPE);
        String toType = toObject.getInfo(context, SELECT_TYPE);
        String partId = EMPTY_STRING;
        log.info("attribute:{}",toObject.getAttributeMap(context));
        log.info("fromType:{} toType:{} docType:{}",fromType,toType,toObject.getInfo(context, "attribute[JF_DocumentType]"));
        if ("XCADBaseDependency".equals(relationshipType)
                && TYPE_Drawing.equals(fromType)
                && TYPE_VPMREFERENCE.equals(toType)) {
            partId = toObjectId;
        } else if ("Reference Document".equals(relationshipType)
                && TYPE_VPMREFERENCE.equals(fromType)
                && TYPE_Document.equals(toType)) {
            //20260813 update by caipan 新建文档时类型尚未写入，等待Document属性Trigger；已有图纸文档关联或断开时直接同步
            if ("Drawing".equals(toObject.getInfo(context, SELECT_ATTR_JF_DocumentType))) {
                partId = fromObjectId;
            }
        }
        if (UIUtil.isNotNullAndNotEmpty(partId)) {
            updatePartIsThereALackOfDrawings(context, partId);
        }
    }

    /**
     * 文档类型修改后同步该文档关联零件的缺失图纸标识
     **
     * @param context 上下文
     * @param args Trigger参数：文档ID、属性名、新属性值
     * @throws Exception 异常
     * @author caipan
     * @date 2026/8/13 10:30
     */
    public void updateJF_IsThereALackOfDrawingsByDocumentType(Context context, String[] args) throws Exception {
        if (args == null || args.length < 2 || !ATTR_JF_DocumentType.equals(args[1])) {
            return;
        }
        DomainObject documentObject = DomainObject.newInstance(context, args[0]);
        if (!TYPE_Document.equals(documentObject.getInfo(context, SELECT_TYPE))) {
            return;
        }
        MapList partList = documentObject.getRelatedObjects(context,
                "Reference Document",
                TYPE_VPMREFERENCE,
                new StringList(SELECT_ID),
                new StringList(),
                true,
                false,
                (short) 1,
                "",
                "",
                0);
        for (Object partObject : partList) {
            String partId = UIUtil.getValue((Map) partObject, SELECT_ID);
            if (UIUtil.isNotNullAndNotEmpty(partId)) {
                updatePartIsThereALackOfDrawings(context, partId);
            }
        }
    }

    /**
     * 按零件归属项目对应客户件/DB关系、详细分类及实际图纸关联状态更新缺失图纸标识，详细分类为空时清空标识
     * 无对应客户件/DB关系，或JF_DirectBuy为non-DB、空值时校验是否缺失图纸
     **
     * @param context 上下文
     * @param partId 零件ID
     * @return void
     * @throws Exception 异常
     * @author caipan
     * @date 2026/8/12 15:30
     */
    //20260903 update by caipan 开放缺失图纸重算方法供历史数据全量初始化复用
    public void updatePartIsThereALackOfDrawings(Context context, String partId) throws Exception {
        String isThereALackOfDrawings = EMPTY_STRING;
        DomainObject partObject = DomainObject.newInstance(context, partId);
        String detailType = partObject.getInfo(context, SELECT_ATTR_JF_Detail_CN);
        String directBuy = EMPTY_STRING;
        //20260903 update by caipan 详细分类为空时缺失图纸标识保持为空，非空时再按现有规则计算Yes或No
        if (UIUtil.isNotNullAndNotEmpty(detailType)) {
            isThereALackOfDrawings = "No";
            String belongProjectId = getPartBelongProject(context, new String[]{partId});
            Map customerPartsDBInfo = new JF_DR_mxJPO().getDRCustomerPartsDBRelationInfo(
                    context, partId, belongProjectId);
            directBuy = UIUtil.getValue(customerPartsDBInfo, Select_Attr_JF_DirectBuy);
            if ((UIUtil.isNullOrEmpty(directBuy)
                        || ATTR_ATTR_JFDIRECT_BUY_RANGE_N.equalsIgnoreCase(directBuy))
                    && isPartDrawingRequired(context, detailType)
                    && !new JF_DR_mxJPO().getVPMReferenceHasDRW(context, partId)) {
                isThereALackOfDrawings = "Yes";
            }
        }
        ContextUtil.pushContext(context);
        try {
            partObject.setAttributeValue(
                    context, "JF_VPMReference.JF_IsThereALackOfDrawings", isThereALackOfDrawings);
        } finally {
            ContextUtil.popContext(context);
        }
        log.info("updatePartIsThereALackOfDrawings partId:{} directBuy:{} result:{}",
                partId, directBuy, isThereALackOfDrawings);
    }

    /**
     * 判断零件详细分类是否配置图纸必填
     **
     * @param context 上下文
     * @param detailType 零件详细分类
     * @return boolean 配置connectdrw时返回true，详细分类未配置时返回false
     * @throws Exception 异常
     * @author caipan
     * @date 2026/8/12 15:30
     */
    //20260826 update by caipan 开放详细分类图纸必填判断供零件创建Trigger复用
    public boolean isPartDrawingRequired(Context context, String detailType) throws Exception {
        Page pageAttributePopulation = new Page("PartAttributeProperties.xml");
        pageAttributePopulation.open(context);
        String strProperties;
        try {
            strProperties = pageAttributePopulation.getContents(context);
        } finally {
            pageAttributePopulation.close(context);
        }
        InputStream inputStream = new ByteArrayInputStream(strProperties.getBytes("UTF-8"));
        DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document document = documentBuilder.parse(inputStream);
        StringList mandatoryAttributeList = JF_DR_mxJPO.findPartAttributeWithXPath(
                document, "MandatoryAttributePart", detailType);
        return mandatoryAttributeList != null && mandatoryAttributeList.contains("connectdrw");
    }

    /**
     * 将零件历史DB/客户零件号对象属性迁移到新的客户零件号对象和关系属性
     **
     * @param context 上下文
     * @param args JPO参数
     * @throws Exception 任意版本迁移失败时抛出异常并回滚本次全部数据
     * @author Codex
     * @date 2026/9/2
     */
    public void migrateCustomerPartsDBHistoryData(Context context, String[] args) throws Exception {
        boolean isPush = false;
        boolean transactionStarted = false;
        try {
            ContextUtil.pushContext(context);
            isPush = true;
            // 一次性取得本次迁移快照，后续不再按“无关系”条件循环查询，避免数据变化导致死循环。
            StringList partSelectList = new StringList(DomainConstants.SELECT_ID);
            partSelectList.add(DomainConstants.SELECT_NAME);
            partSelectList.add(DomainConstants.SELECT_REVISION);
            partSelectList.add(DomainConstants.SELECT_OWNER);
            partSelectList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_CustomerPartNumber);
            partSelectList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_CustomerPartRevision);
            partSelectList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JFDIRECT_BUY);
            String where = "to[" + RELATIONSHIP_JFVPMREFERENCE2CUSTOMERPARTS + "]==FALSE";
            MapList historyPartList = new MapList();
            if (args.length != 0) {
                String id = args[0];
                DomainObject object = DomainObject.newInstance(context, id);
                String type = object.getInfo(context, SELECT_TYPE);
                if (TYPE_PROJECT_SPACE.equalsIgnoreCase(type)) {
                    historyPartList = object.getRelatedObjects(
                            context,
                            rel_JFProject2RootPart,
                            TYPE_VPMReference,
                            JF_Util_mxJPO.basicBolistSel(),
                            JF_Util_mxJPO.basicRellistSel(),
                            false,
                            true,
                            (short) 1,
                            where,
                            "attribute[JF_BelongPart]==Y",
                            0
                    );
                } else {
                    historyPartList = object.getRelatedObjects(context,
                            JF_PLMConstants_mxJPO.REL_Instance, //pattern to match relationships
                            JF_PLMConstants_mxJPO.TYPE_VPMReference, //pattern to match types
                            partSelectList, //the eMatrix StringList object that holds the list of select statement pertaining to Business Obejcts.
                            JF_Util_mxJPO.basicRellistSel(), //the eMatrix StringList object that holds the list of select statement pertaining to Relationships.
                            false, //get To relationships
                            true, //get From relationships
                            (short) 0, //the number of levels to expand, 0 equals expand all.
                            where, //where clause to apply to objects, can be empty ""
                            null, //where clause to apply to relationship, can be empty ""
                            (short) 0); //limit
                    historyPartList.add(object.getInfo(context, partSelectList));
                }
            } else {
                historyPartList = DomainObject.findObjects(context, TYPE_VPMREFERENCE, DomainConstants.QUERY_WILDCARD, where, partSelectList);
            }
            // 同一个name代表同一零件版本序列，每个版本序列只创建一个客户零件号对象。
            Map<String, List<Map>> versionGroupMap = JF_PublicMethodClass_mxJPO.getMapListGroupingMap(context, historyPartList, DomainConstants.SELECT_NAME);
            if (versionGroupMap.isEmpty()) {
                log.info("migrateCustomerPartsDBHistoryData no history data need migrate");
                return;
            }
            // 整次历史迁移共用一个事务；任意版本报错时，已创建的对象、关系和属性全部回滚。
            ContextUtil.startTransaction(context, true);
            transactionStarted = true;
            int createdCustomerPartsCount = 0;
            int createdPartRelationshipCount = 0;
            JF_Util_mxJPO jfUtilMxJPO = new JF_Util_mxJPO();
            String project = JF_PLMConstants_mxJPO.PROJECT_JFSeat;
            for (Map.Entry<String, List<Map>> groupEntry : versionGroupMap.entrySet()) {
                String partName = groupEntry.getKey();
                List<Map> versionList = groupEntry.getValue();
                MapList mapList = new MapList();
                mapList.addAll(versionList);
                mapList.addSortKey(SELECT_REVISION, ProgramCentralConstants.ASCENDING_SORT, ProgramCentralConstants.SORTTYPE_STRING);
                mapList.sort();
                Map<String, Map<String, String>> connMap = new HashMap<>();
                for (int i = 0; i < mapList.size(); i++) {
                    Map versionMap = (Map) mapList.get(i);
                    String partId = UIUtil.getValue(versionMap, DomainConstants.SELECT_ID);
                    String projectId = getPartBelongProject(context, new String[]{partId, DomainConstants.SELECT_ID});
                    if (UIUtil.isNullOrEmpty(projectId)) {
                        continue;
                    }
                    Map<String, String> relationshipAttributeMap = new HashMap();
                    relationshipAttributeMap.put("projectId", projectId);
                    relationshipAttributeMap.put(JF_PLMConstants_mxJPO.Attr_JFCustomerPartNumber, UIUtil.getValue(versionMap, JF_PLMConstants_mxJPO.SELECT_ATTR_CustomerPartNumber));
                    relationshipAttributeMap.put(JF_PLMConstants_mxJPO.Attr_JFCustomerPartRevision, UIUtil.getValue(versionMap, JF_PLMConstants_mxJPO.SELECT_ATTR_CustomerPartRevision));
                    relationshipAttributeMap.put(JF_PLMConstants_mxJPO.Attr_JF_DirectBuy, UIUtil.getValue(versionMap, JF_PLMConstants_mxJPO.SELECT_ATTR_JFDIRECT_BUY));
                    connMap.put(partId, relationshipAttributeMap);
                }
                if (connMap.isEmpty()) {
                    continue;
                }
                Map ownerSourcePartMap = (Map) mapList.get(0);
                String partOwner = UIUtil.getValue(ownerSourcePartMap, DomainConstants.SELECT_OWNER);
                // 获取组织
                String organization = jfUtilMxJPO.getPersonOrganization(context, partOwner);
                //创建DB/客户零件号对象
                String customerPartsId = FrameworkUtil.autoName(context, "type_JFCustomerParts", "policy_JFCustomerParts");
                DomainObject customerPartsObject = DomainObject.newInstance(context, customerPartsId);
                //设置owner的组织协作区
                customerPartsObject.setOwner(context, partOwner);
                customerPartsObject.setPrimaryOwnership(context, project, organization);
                createdCustomerPartsCount++;
                for (Map.Entry<String, Map<String, String>> connEntry : connMap.entrySet()) {
                    String partId = connEntry.getKey();
                    Map<String, String> attrMap = connEntry.getValue();
                    String projectId = UIUtil.getValue(attrMap,"projectId");
                    // 复用页面维护所属项目DB/客户信息的关系创建方式，保持主关系和项目关系Owner规则一致。
                    String relationshipId = connectEBOMCustomerPartsDBObject(
                            context,
                            partId,
                            projectId,
                            customerPartsId);
                    attrMap.remove("projectId");
                    DomainRelationship.newInstance(context, relationshipId).setAttributeValues(context, attrMap);
                    createdPartRelationshipCount++;
                }
            }
            ContextUtil.commitTransaction(context);
            transactionStarted = false;
            log.info("migrateCustomerPartsDBHistoryData success candidatePartCount:{} versionGroupCount:{} "
                            + "createdCustomerPartsCount:{} createdPartRelationshipCount:{}",
                    historyPartList.size(),
                    versionGroupMap.size(),
                    createdCustomerPartsCount,
                    createdPartRelationshipCount);
        } catch (Exception e) {
            if (transactionStarted) {
                ContextUtil.abortTransaction(context);
            }
            log.error("migrateCustomerPartsDBHistoryData failed, all changes rolled back", e);
            throw e;
        } finally {
            if (isPush) {
                ContextUtil.popContext(context);
            }
        }
    }

}




    
