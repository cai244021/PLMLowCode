import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.DomainRelationship;
import com.matrixone.apps.domain.util.*;
import com.matrixone.apps.framework.ui.PostProcessCallable;
import com.matrixone.apps.framework.ui.ProgramCallable;
import com.matrixone.apps.framework.ui.UIUtil;
import com.matrixone.apps.program.ProgramCentralConstants;
import matrix.db.Context;
import matrix.db.BusinessObjectList;
import matrix.db.JPO;
import matrix.db.Policy;
import matrix.db.RelationshipType;
import matrix.util.StringList;
import org.apache.commons.text.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import static com.matrixone.apps.domain.DomainConstants.*;

/**
 * @ClassName JF_Snapshot_mxJPO
 * @Author: LIUJR
 * @CreateDate: 2025/2/8 10:48
 * @UpdateRemark:
 * @Version: 1.0
 * @Description: 快照
 */
public class JF_Snapshot_mxJPO implements JF_PLMConstants_mxJPO{
    private static final Logger JF_LOGGER = LoggerFactory.getLogger(JF_Snapshot_mxJPO.class);

    private static final StringList busSelectsList = new StringList();
    private static final StringList relSelectsList = new StringList();
    private static final String SUITE_KEY = "emxComponentsStringResource";
    private static final String ATTR_JF_SNAPSHOT_SUB_TYPE = "JFSnapshotSubType";
    private static final String ATTR_FINAL_QUOTATION_VERSION = "JF_IsTheFinalQuotationVersionAvailable";
    private static final String TYPE_PRODUCT_CONFIG_TABLE = "JFProductConfigTable";
    private static final String REL_PROJECT_TO_PRODUCT_CONFIG = "JFProject2ProductConfigTable";
    private static final String REL_PRODUCT_CONFIG_TO_VPM = "JFProductConfigTable2VPMReference";
    private static final String REL_SNAPSHOT_TO_PRODUCT_CONFIG = "JFSnapshot2ProductConfigTable";
    private static final String SELECT_RELEASED_ACTUAL = "state[Released].actual";
    private static final String FIELD_SNAPSHOT_PRODUCT_CONFIG = "JFSnapshotProductConfig";
    private static final Set<String> RELEASED_OR_LATER_PRODUCT_CONFIG_STATES = new HashSet<>(Arrays.asList(
            "Released", "ManagerFillsIn", "Obsolete"
    ));
    private static final Set<String> PRODUCT_CONFIG_REQUIRED_SUB_TYPES = new HashSet<>(Arrays.asList(
            "Quote", "DV SOURCING", "PV SOURCING", "OTS", "SOP"
    ));
    private static final Set<String> FROZEN_FALLBACK_PART_SUB_TYPES = new HashSet<>(Arrays.asList(
            "Quote", "DV SOURCING", "PV SOURCING"
    ));
    private static final Set<String> COSTING_NOTICE_SUB_TYPES = new HashSet<>(Arrays.asList(
            "Quote", "DV SOURCING", "PV SOURCING", "DV TKO", "PV TKO", "SOP"
    ));
    private static final Set<String> PROJECT_MANAGER_NOTICE_SUB_TYPES = new HashSet<>(Arrays.asList(
            "DV SOURCING", "PV SOURCING"
    ));
    static {
        busSelectsList.add(DomainConstants.SELECT_ATTRIBUTE_TITLE);
        busSelectsList.add(DomainConstants.SELECT_DESCRIPTION);
        busSelectsList.add(DomainConstants.SELECT_REVISION);
        busSelectsList.add(DomainConstants.SELECT_NAME);
        busSelectsList.add(DomainConstants.SELECT_ID);
        busSelectsList.add(DomainConstants.SELECT_TYPE);
        busSelectsList.add(DomainConstants.SELECT_CURRENT);
        busSelectsList.add(DomainConstants.SELECT_OWNER);
        busSelectsList.add(DomainConstants.SELECT_PROJECT);
        busSelectsList.add(DomainConstants.SELECT_ORGANIZATION);
        busSelectsList.add(DomainConstants.SELECT_ORIGINATED);
        busSelectsList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_V_PART_NUMBER);
        relSelectsList.add(DomainRelationship.SELECT_ID);
        relSelectsList.add(SELECT_JF_InternalColorCode);
    }

    /**
    * 获取项目下的快照
    * @param context
	* @param args
    * @author LIUJR
    * @throws
    * @return com.matrixone.apps.domain.util.MapList
    * @date 2025/2/8 10:55
    * @description
    */
    public MapList getProjectSpaceSnapshotList(Context context, String[] args) throws Exception{
        try {
            Map paramsMap = (Map) JPO.unpackArgs(args);
            ContextUtil.pushContext(context);
            String strObjectId = (String) paramsMap.get(STRING_OBJECTID);
            DomainObject objectProject = DomainObject.newInstance(context, strObjectId);
            StringList SnapshotList = objectProject.getInfoList(context, "from[JFProject2Snapshot].to.id");
            JF_LOGGER.info("ColorMatrix:{}", SnapshotList);
            busSelectsList.add(SELECT_ATTR_JFSnapshotType);
            MapList mlPartInfoList = DomainObject.getInfo(context, SnapshotList.toStringArray(), busSelectsList);
            JF_LOGGER.info("mlPartInfoList:{}", mlPartInfoList);
            return mlPartInfoList;
        }catch (Exception e) {
            e.printStackTrace();
            ContextUtil.popContext(context);
        }
        return new MapList();
    }



    /**
    * 获取快照的数据清单
    * @param context
	* @param args
    * @author LIUJR
    * @throws
    * @return com.matrixone.apps.domain.util.MapList
    * @date 2025/2/10 15:36
    * @description
    */
    public MapList getSnapshotContentList(Context context, String[] args) throws Exception{
        try {
            Map paramsMap = (Map) JPO.unpackArgs(args);
            ContextUtil.pushContext(context);
            String strObjectId = (String) paramsMap.get(STRING_OBJECTID);
            DomainObject objectProject = DomainObject.newInstance(context, strObjectId);
            StringList SnapshotList = objectProject.getInfoList(context, "from[JFSnapshot2VPMReference].to.id");
            JF_LOGGER.info("SnapshotList:{}", SnapshotList);
            MapList mlPartInfoList = DomainObject.getInfo(context, SnapshotList.toStringArray(), busSelectsList);
            JF_LOGGER.info("mlPartInfoList:{}", mlPartInfoList);
            return mlPartInfoList;
        }catch (Exception e) {
            e.printStackTrace();
            ContextUtil.popContext(context);
        }
        return new MapList();
    }

    /**
     * 查询快照关联零件及其全部子件中的DB件
     **
     * @param context
     * @param args 表格请求参数
     * @return MapList 当前快照所属项目下Direct Buy为direct-buy或consignment的零件
     * @throws Exception
     * @author caipan
     * @date 2026/7/29 21:30
     */
    public MapList getSnapshotDBPartList(Context context, String[] args) throws Exception {
        Map paramMap = JPO.unpackArgs(args);
        String snapshotId = UIUtil.getValue(paramMap, STRING_OBJECTID);
        MapList resultList = new MapList();
        if (UIUtil.isNullOrEmpty(snapshotId)) {
            return resultList;
        }

        DomainObject snapshot = DomainObject.newInstance(context, snapshotId);
        String projectId = snapshot.getInfo(context, "to[" + rel_JFProject2Snapshot + "].from.id");
        StringList rootPartIds = snapshot.getInfoList(context,
                "from[" + rel_JFSnapshot2VPMReference + "].to.id");
        if (UIUtil.isNullOrEmpty(projectId) || rootPartIds == null || rootPartIds.isEmpty()) {
            return resultList;
        }

        Set<String> allPartIds = new LinkedHashSet<>();
        StringList partSelects = new StringList(SELECT_ID);
        for (Object rootPartIdObj : rootPartIds) {
            String rootPartId = String.valueOf(rootPartIdObj);
            if (UIUtil.isNullOrEmpty(rootPartId) || !allPartIds.add(rootPartId)) {
                continue;
            }
            MapList childPartList = DomainObject.newInstance(context, rootPartId).getRelatedObjects(
                    context,
                    REL_Instance,
                    TYPE_VPMReference,
                    partSelects,
                    null,
                    false,
                    true,
                    (short) 0,
                    "",
                    "",
                    0);
            for (Object childPartObj : childPartList) {
                Map childPartMap = (Map) childPartObj;
                String childPartId = UIUtil.getValue(childPartMap, SELECT_ID);
                if (UIUtil.isNotNullAndNotEmpty(childPartId)) {
                    allPartIds.add(childPartId);
                }
            }
        }

        StringList dbPartIds = new StringList();
        JF_DR_mxJPO dr = new JF_DR_mxJPO();
        for (String partId : allPartIds) {
            Map customerPartsDBInfo = dr.getDRCustomerPartsDBRelationInfo(context, partId, projectId);
            String directBuy = UIUtil.getValue(customerPartsDBInfo, Select_Attr_JF_DirectBuy);
            if ("direct-buy".equalsIgnoreCase(directBuy) || "consignment".equalsIgnoreCase(directBuy)) {
                dbPartIds.add(partId);
            }
        }
        if (!dbPartIds.isEmpty()) {
            resultList = DomainObject.getInfo(context, dbPartIds.toStringArray(), busSelectsList);
        }
        return resultList;
    }

    /**
     * 创建产品快照，校验Quote最终报价版本唯一性，并按快照子类型关联产品配置表及其实例化供货件
     **
     * @param context
     * @param args 创建表单请求参数
     * @return Map 创建结果，包含快照ID
     * @throws Exception
     * @author caipan
     * @date 2026/7/29 16:13
     */
    public Map createSnapshot(Context context, String args[]) throws Exception {
        Map map = new HashMap();
        Boolean isPush = Boolean.FALSE;
        Boolean isTransactionStarted = Boolean.FALSE;
        try {
            HashMap requestMap = (HashMap) JPO.unpackArgs(args);
//            JF_LOGGER.info("requestMap:{}",requestMap);
            String parentOID = (String) requestMap.get("parentOID");
            String strJFSnapshotType = (String) requestMap.get("JFSnapshotType");
            String strJFSnapshotSubType = (String) requestMap.get(ATTR_JF_SNAPSHOT_SUB_TYPE);
            String finalQuotationVersion = (String) requestMap.get(ATTR_FINAL_QUOTATION_VERSION);
            String productConfigId = (String) requestMap.get(FIELD_SNAPSHOT_PRODUCT_CONFIG);
            String strTitle = (String) requestMap.get("Title");
            String strDescription = (String) requestMap.get("Description");
            String JFSTDPersonSelect = (String) requestMap.get("JFSTDPerson");
            JF_LOGGER.info("JFSTDPersonSelect:{}",JFSTDPersonSelect);
            //20260806 update by caipan 非Quote快照保存空值；Quote允许空、Y、N，选择Y时校验项目内唯一性
            boolean isFinalQuoteSnapshot = "Quote".equals(strJFSnapshotSubType)
                    && "Y".equals(finalQuotationVersion);
            if (!"Quote".equals(strJFSnapshotSubType)) {
                finalQuotationVersion = EMPTY_STRING;
            } else if (!("Y".equals(finalQuotationVersion) || "N".equals(finalQuotationVersion))) {
                finalQuotationVersion = EMPTY_STRING;
            }
            if (isFinalQuoteSnapshot) {
                validateFinalQuotationVersionUnique(context, parentOID, EMPTY_STRING);
            }
            if (isProductConfigRequiredSubType(strJFSnapshotSubType)) {
                String requiredMessage = EnoviaResourceBundle.getProperty(context, SUITE_KEY, context.getLocale(),
                        "emxComponents.Snapshot.ProductConfigRequired");
                if (UIUtil.isNullOrEmpty(productConfigId)) {
                    throw new Exception(requiredMessage);
                }
                DomainObject productConfig = DomainObject.newInstance(context, productConfigId);
                String productConfigType = productConfig.getInfo(context, SELECT_TYPE);
                StringList projectProductConfigIds = DomainObject.newInstance(context, parentOID)
                        .getInfoList(context, "from[" + REL_PROJECT_TO_PRODUCT_CONFIG + "].to.id");
                if (!TYPE_PRODUCT_CONFIG_TABLE.equals(productConfigType)
                        || !projectProductConfigIds.contains(productConfigId)) {
                    throw new Exception(EnoviaResourceBundle.getProperty(context, SUITE_KEY, context.getLocale(),
                            "emxComponents.Snapshot.ProductConfigNotInProject"));
                }
            } else {
                productConfigId = EMPTY_STRING;
            }
            // 获取日期
            LocalDate today = LocalDate.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
            String formattedDate = today.format(formatter);
            //获取流水码
            StringList typeSelectList = JF_Util_mxJPO.basicBolistSel();
            typeSelectList.add(SELECT_ORIGINATED);
            String number = JF_PublicMethodClass_mxJPO.getObjectsKeySort(context, TYPE_JFSnapshot, "", typeSelectList, SELECT_ORIGINATED, ProgramCentralConstants.DESCENDING_SORT, ProgramCentralConstants.SORTTYPE_DATE);
            String snapshotName = STRING_SHT + formattedDate + number;
            //创建快照
            ContextUtil.startTransaction(context, true);
            isTransactionStarted = Boolean.TRUE;
            Policy policy = new Policy(POLICY_JFSnapshot);
            String snapshotRev = policy.getFirstInSequence(context);
            DomainObject snapshot = DomainObject.newInstance(context);
            snapshot.createObject(context, TYPE_JFSnapshot, snapshotName, snapshotRev, POLICY_JFSnapshot, context.getVault().getName());
            //设置属性
            Map<String, String> attributeMap = new HashMap<>();
            attributeMap.put(DomainConstants.ATTRIBUTE_TITLE, strTitle);
            attributeMap.put(ATTR_JF_JFSnapshotType, strJFSnapshotType);
            attributeMap.put(ATTR_JF_SNAPSHOT_SUB_TYPE, strJFSnapshotSubType);
            attributeMap.put(ATTR_FINAL_QUOTATION_VERSION, finalQuotationVersion);
            snapshot.setAttributeValues(context, attributeMap);
            snapshot.setDescription(context, strDescription);
            ContextUtil.pushContext(context);
            isPush = Boolean.TRUE;
            //关联项目
            String strSnapshotId = snapshot.getId(context);
            snapshot.addFromObject(context, new RelationshipType(JF_PLMConstants_mxJPO.rel_JFProject2Snapshot), parentOID);
            map.put("id",strSnapshotId );
            //同步STD项目成员 add by chenyan 2025/04/18
            //移除STD成员 update by ljr 20251212
//            synchronousSTDPerson(context,new String[]{strSnapshotId,parentOID,JFSTDPersonSelect});
            Set<String> personIdSet = new HashSet<>();
            if(UIUtil.isNotNullAndNotEmpty(JFSTDPersonSelect)){
                personIdSet  = Arrays.stream(JFSTDPersonSelect.split(","))
                        .map(String::trim)
                        .collect(Collectors.toSet());
            }
            DomainRelationship.connect(context, snapshot, "JFSnapshot2Member", true, StringList.create(personIdSet).toStringArray());
            if (UIUtil.isNotNullAndNotEmpty(productConfigId)) {
                snapshot.addToObject(context, new RelationshipType(REL_SNAPSHOT_TO_PRODUCT_CONFIG), productConfigId);
                connectSnapshotProductConfigSupplyParts(context, snapshot, productConfigId, strJFSnapshotSubType);
            }
            ContextUtil.commitTransaction(context);
            isTransactionStarted = Boolean.FALSE;
        }catch (Exception e){
            e.printStackTrace();
            if (isTransactionStarted) {
                ContextUtil.abortTransaction(context);
            }
            throw e;
        } finally {
            if (isPush) {
                ContextUtil.popContext(context);
            }
        }
        return  map;
    }

    /**
     * 将产品配置表供货件按快照类型实例化到快照
     **
     * @param context
     * @param snapshot 当前快照对象
     * @param productConfigId 产品配置表ID
     * @param snapshotSubType 快照子类型
     * @throws Exception
     * @author caipan
     * @date 2026/7/29 17:13
     */
    private void connectSnapshotProductConfigSupplyParts(Context context, DomainObject snapshot,
                                                         String productConfigId, String snapshotSubType) throws Exception {
        StringList supplyPartIds = DomainObject.newInstance(context, productConfigId)
                .getInfoList(context, "from[" + REL_PRODUCT_CONFIG_TO_VPM + "].to.id");
        Set<String> latestReleasedPartIds = new LinkedHashSet<>();
        for (Object supplyPartIdObj : supplyPartIds) {
            String supplyPartId = String.valueOf(supplyPartIdObj);
            if (UIUtil.isNullOrEmpty(supplyPartId)) {
                continue;
            }
            //20260806 update by caipan Quote、DV SOURCING、PV SOURCING无发布版本时允许回退最新冻结版本
            String initialPartId = getSnapshotInitialPartId(context, supplyPartId, snapshotSubType);
            if (UIUtil.isNotNullAndNotEmpty(initialPartId)) {
                latestReleasedPartIds.add(initialPartId);
            }
        }
        if (!latestReleasedPartIds.isEmpty()) {
            DomainRelationship.connect(context, snapshot, rel_JFSnapshot2VPMReference, true,
                    latestReleasedPartIds.toArray(new String[latestReleasedPartIds.size()]));
        }
    }

    /**
    * 根据快照的状态获取物理产品
    * @param context
	* @param args
    * @author LIUJR
    * @throws
    * @return matrix.util.StringList
    * @date 2025/2/13 15:34
    * @description
    */
    public StringList getSnapshotVPMReferenceId(Context context,String[] args)throws Exception{
        StringList result = new StringList();
        Map paramMap = (Map) JPO.unpackArgs(args);
        String objectId = (String)paramMap.get("objectId");
        DomainObject snapshot = DomainObject.newInstance(context, objectId);
        //获取快照关联的项目
        String projectId = snapshot.getInfo(context, "to[JFProject2Snapshot].from.id");
        JF_LOGGER.info("projectId:{}", projectId);
        if (UIUtil.isNullOrEmpty(projectId)) {
            return result;
        }
        //拿取项目关联的整椅及其下的子件
        DomainObject domainObject = DomainObject.newInstance(context, projectId);
        StringList bosel = JF_Util_mxJPO.basicBolistSel();
        String attributeValue = snapshot.getAttributeValue(context, ATTR_JF_JFSnapshotType);
        String strWhere = Range_JFSnapshotType_KeyShot.equalsIgnoreCase(attributeValue) ? "FROZEN,RELEASED" :DomainConstants.EMPTY_STRING;
        String where = "current==FROZEN||current==RELEASED";
        MapList partMapList = domainObject.getRelatedObjects(
                context,
                "JFProject2RootPart,VPMInstance",
                JF_PLMConstants_mxJPO.TYPE_VPMReference,
                bosel,
                new StringList(),
                false,
                true,
                (short) 3,//暂时给3层，正常是快照到一级件即可
                where,
                "",
                0
        );
        JF_LOGGER.info("VPMList:{}", partMapList.size());
        for(int i=0; i<partMapList.size(); i++){
            Map mangerMap = (Map) partMapList.get(i);
            String id = (String)mangerMap.get(DomainConstants.SELECT_ID);
            if(!result.contains(id)) {
                result.add(id);
            }
        }
        JF_LOGGER.info("result:{}", result.size());
        return result;
    }

    /**
    * 排除已经添加的物理产品
    * @param context
	* @param args
    * @author LIUJR
    * @throws
    * @return matrix.util.StringList
    * @date 2025/2/13 15:48
    * @description
    */
    public StringList excludeSnapshotVPMReferenceId(Context context,String[] args)throws Exception{
        Map paramMap = (Map) JPO.unpackArgs(args);
        String objectId = (String)paramMap.get("objectId");
        DomainObject snapshot = DomainObject.newInstance(context, objectId);
        StringList infoList = snapshot.getInfoList(context, "from[JFSnapshot2VPMReference].to.id");
        return infoList;
    }

    /**
     * 快照由草稿提升到审批中时校验快照内容及类型所需项目角色
     **
     * @param context
     * @param args Trigger参数
     * @return int 校验失败返回1，校验通过返回0
     * @throws Exception
     * @author caipan
     * @date 2026/7/29 20:40
     */
    @ProgramCallable
    public int checkJFSnapshotContent(Context context, String[] args) throws Exception {
        JF_LOGGER.info("method:checkJFSnapshotContent start...");
        int flag = 0;
        try {
            String objectId =args[0]; //审核对象Id
            String alertMess = DomainConstants.EMPTY_STRING;
            //快照内容
            DomainObject snapshotObject = DomainObject.newInstance(context, objectId);
            String strHasContext = snapshotObject.getInfo(context, "from[JFSnapshot2VPMReference]");
            if ("FALSE".equalsIgnoreCase(strHasContext)) {
                alertMess = EnoviaResourceBundle.getProperty(context, SUITE_KEY, context.getLocale(), "emxComponents.Snapshot.NotHasContext");
                emxContextUtil_mxJPO.mqlNotice(context, alertMess);
                flag = 1;
            }
            String snapshotSubType = snapshotObject.getAttributeValue(context, ATTR_JF_SNAPSHOT_SUB_TYPE);
            String projectId = snapshotObject.getInfo(context, "to[" + rel_JFProject2Snapshot + "].from.id");
            StringList missingRoleMessages = new StringList();
            if (COSTING_NOTICE_SUB_TYPES.contains(snapshotSubType)) {
                String costingPersonId = JF_PublicMethodClass_mxJPO.getProjectWherePerson(context, projectId,
                        SELECT_ID, EMPTY_STRING, "attribute[Project Role]=='Costing'");
                if (UIUtil.isNullOrEmpty(costingPersonId)) {
                    missingRoleMessages.add(EnoviaResourceBundle.getProperty(context, SUITE_KEY,
                            context.getLocale(), "emxComponents.Snapshot.CostingNotFound"));
                }
            }
            if (PROJECT_MANAGER_NOTICE_SUB_TYPES.contains(snapshotSubType)) {
                String projectManagerPersonId = JF_PublicMethodClass_mxJPO.getProjectWherePerson(context, projectId,
                        SELECT_ID, EMPTY_STRING, "attribute[Project Role]=='Project manager'");
                if (UIUtil.isNullOrEmpty(projectManagerPersonId)) {
                    missingRoleMessages.add(EnoviaResourceBundle.getProperty(context, SUITE_KEY,
                            context.getLocale(), "emxComponents.Snapshot.ProjectManagerNotFound"));
                }
            }
            if (!missingRoleMessages.isEmpty()) {
                emxContextUtil_mxJPO.mqlNotice(context, missingRoleMessages.join("\n"));
                flag = 1;
            }
            //直线经理id
//            String strLineManagerId = JF_PublicMethodClass_mxJPO.getPersonLineManager(context, null, snapshotObject.getOwner(context).getName());
//            if (UIUtil.isNullOrEmpty(strLineManagerId)) {
//                alertMess = EnoviaResourceBundle.getProperty(context, SUITE_KEY, context.getLocale(), "emxComponents.DataOutSource.FillInTheLineManage");
//                emxContextUtil_mxJPO.mqlNotice(context, alertMess);
//                flag = 1;
//            }
        }catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
        JF_LOGGER.info("method:checkJFSnapshotContent end...");
        return flag;
    }

    /**
    * 创建快照审批流程
    * @param context
	* @param args
    * @author LIUJR
    * @throws
    * @return int
    * @date 2025/2/13 16:12
    * @description
    */
    public void createJFSnapshotRouteAction(Context context, String[] args) throws Exception {
        JF_LOGGER.info("method:createJFSnapshotRouteAction -创建流程- start...");
        try {
            String objectId = args[0]; //审核对象Id
            ContextUtil.startTransaction(context, true);
            //人员
            DomainObject snapshotObject = DomainObject.newInstance(context, objectId);
            String attributeValue = snapshotObject.getAttributeValue(context, ATTR_JF_JFSnapshotType);
//            if (Range_JFSnapshotType_KeyShot.equalsIgnoreCase(attributeValue)) {
//                //只有关键快照才创建审批流程
//                // 获取直线经理名称
//                JF_PublicMethodClass_mxJPO jfPublicMethodClassMxJPO = new JF_PublicMethodClass_mxJPO();
//                String lineManagerID = jfPublicMethodClassMxJPO.getPersonLineManager(context, null, snapshotObject.getOwner(context).getName());
//                //获取快照所属的项目id
//                String projectId = snapshotObject.getInfo(context, "to[JFProject2Snapshot].from.id");
//                //获取项目经理id
//                String projectManagerId = PersonUtil.getPersonObjectID(context, JF_Util_mxJPO.getProjectManager(context, new String[]{projectId}));
//                //创建流程
//                JF_Route_mxJPO jf_route = new JF_Route_mxJPO(context, args);
//                //组装审批人员
//                MapList approveList = new MapList();
//                String tileMess = EnoviaResourceBundle.getProperty(context, SUITE_KEY, context.getLocale(), "emxComponents.SnapshotRouteInfo.TitleMessage");
//                String ProjectManage = EnoviaResourceBundle.getProperty(context, SUITE_KEY, context.getLocale(), "emxComponents.DARouteInfo.TitleMessage.ProjectManage");
//                String LineManage = EnoviaResourceBundle.getProperty(context, SUITE_KEY, context.getLocale(), "emxComponents.DARouteInfo.TitleMessage.LineManage");
//                //审批人 直线经理
//                Map nReceiverMapOne = (Map) JF_PublicMethodClass_mxJPO.getMapAnyOrAll(lineManagerID, tileMess + "-" + LineManage, "true", "1", "All");//设置审批信息 标题
//                approveList.add(nReceiverMapOne);
//                //审批人 项目经理
//                Map nReceiverMapTwo = (Map) JF_PublicMethodClass_mxJPO.getMapAnyOrAll(projectManagerId, tileMess + "-" + ProjectManage, "true", "2", "All");//设置审批信息 标题
//                approveList.add(nReceiverMapTwo);
//                String state = STATE_POLICY_JFSnapshot_APPROVE;
//                String policy = POLICY_POLICY_JFSnapshot;
//                String routeDescription = tileMess;
//                String routeId = jf_route.createAndStartRoute(context, approveList, objectId, state, policy, routeDescription);
//                JF_LOGGER.info("routeId:{}", routeId);
//            } else {
//                snapshotObject.promote(context);
//            }
            // add by chenyan 2025/04/18 整椅经理直接提升状态 非整椅经理创建流程 流程结点只有整椅经理
            String strLoginUser = context.getUser();
            String strMql = JF_PublicMethodClass_mxJPO.buildStringInStrings("print bus ", objectId, " select to[JFProject2Snapshot].from.from[Member|attribute[Project Role]=='Chair manager'].to.name dump ;");
            String strMqlRes =  MqlUtil.mqlCommand(context, false, strMql, true);
            JF_LOGGER.info("strLoginUser:{}",strLoginUser);
            JF_LOGGER.info("strMqlRes:{}",strMqlRes);
            if (strLoginUser.equals(strMqlRes)){
                snapshotObject.promote(context);
            }else {
                String tileMess = EnoviaResourceBundle.getProperty(context, SUITE_KEY, context.getLocale(), "emxComponents.SnapshotRouteInfo.ChairManagerApprove");
                //创建流程
                JF_Route_mxJPO jf_route = new JF_Route_mxJPO(context, args);
                //组装审批人员
                MapList approveList = new MapList();
                String strChairManagerId = PersonUtil.getPersonObjectID(context, strMqlRes);
                //审批人 整椅经理
                Map nReceiverMapTwo = (Map) JF_PublicMethodClass_mxJPO.getMapAnyOrAll(strChairManagerId, tileMess, "true", "1", "All");//设置审批信息 标题
                approveList.add(nReceiverMapTwo);
                String state = STATE_POLICY_JFSnapshot_APPROVE;
                String policy = POLICY_POLICY_JFSnapshot;
                String routeDescription = tileMess;
                String routeId = jf_route.createAndStartRoute(context, approveList, objectId, state, policy, routeDescription);
                JF_LOGGER.info("routeId:{}", routeId);
            }

            ContextUtil.commitTransaction(context);
        } catch (Exception e) {
            ContextUtil.abortTransaction(context);
            e.printStackTrace();
            throw e;
        }
        JF_LOGGER.info("method:createJFSnapshotRouteAction -创建流程- end...");
    }

    /**
    * 创建快照权限   不要轻易改 这个涉及到快照和颜色矩阵的创建权限 ，会互相影响
    * @param context
	* @param args
    * @author LIUJR
    * @throws
    * @return java.lang.Boolean
    * @date 2025/2/14 13:42
    * @description
    */
    public Boolean createSnapshotAccess(Context context, String[] args) {
        Boolean flag = Boolean.FALSE;
        StringList selList = JF_Util_mxJPO.basicBolistSel();
        selList.add(SELECT_ID);
        StringList relList = JF_Util_mxJPO.basicRellistSel();
        try{
            Map params = JPO.unpackArgs(args);
            String strObjectId = (String) params.get("objectId");
            DomainObject projectObject = DomainObject.newInstance(context, strObjectId);
            //判断当前用户是否是研发部成员
            DomainObject personObject = PersonUtil.getPersonObject(context);
            StringList infoList = personObject.getInfoList(context, "to[Member].from.id");
//            MapList departmentList = personObject.getRelatedObjects(context,
//                    DomainConstants.RELATIONSHIP_MEMBER, //pattern to match relationships
//                    DomainConstants.TYPE_DEPARTMENT, //pattern to match types
//                    selList, //the eMatrix StringList object that holds the list of select statement pertaining to Business Obejcts.
//                    relList, //the eMatrix StringList object that holds the list of select statement pertaining to Relationships.
//                    true, //get To relationships
//                    false, //get From relationships
//                    (short) 1, //the number of levels to expand, 0 equals expand all.
//                    DomainConstants.EMPTY_STRING, //where clause to apply to objects, can be empty ""
//                    DomainConstants.EMPTY_STRING, //where clause to apply to relationship, can be empty ""
//                    (short) 10 //limit
//            );
            String strRDCenterDepartmentId = JF_PublicMethodClass_mxJPO.getBasicUrl(context, new String[]{"JF_RDCenterDepartment.id"});
            String[] split = strRDCenterDepartmentId.split(",");
            strRDCenterDepartmentId = split[0];
            if (infoList.contains(strRDCenterDepartmentId)) {
                //是研发部人员 判断是否是项目成员
                StringList infoList1 = projectObject.getInfoList(context, "from[Member].to.name");
                if (infoList1.contains(context.getUser().toString())) {
                    //是项目成员
                    flag = Boolean.TRUE;
                } else {
                    flag = Boolean.FALSE;
                }
            } else {
                flag = Boolean.FALSE;
            }
//            //拿取项目的成员以及关系属性project role
//            MapList mapList = projectObject.getRelatedObjects(
//                    context,
//                    DomainRelationship.RELATIONSHIP_MEMBER,
//                    DomainConstants.TYPE_PERSON,
//                    new StringList(SELECT_NAME),
//                    new StringList(),
//                    false,
//                    true,
//                    (short) 1, // recursion level
//                    "", //object where clause
//                    "attribute[Project Role]=='Chair manager'", //relationship where clause
//                    0
//            );
//            if(mapList.size()>0) {
//                Map peresonMap = (Map) mapList.get(0);
//                String charMangerName = (String) peresonMap.get(SELECT_NAME);
//                if (charMangerName.equalsIgnoreCase(context.getUser())) {
//                    flag = Boolean.TRUE;
//                } else {
//                    flag = Boolean.FALSE;
//
//                }
//            }
        } catch (Exception e) {
            e.printStackTrace();
            flag = Boolean.FALSE;
        }
        return flag;
    }


    /**
    * 项目成员能操作权限
    * @param context
	* @param args
    * @author LIUJR
    * @throws
    * @return java.lang.Boolean
    * @date 2025/3/26 17:19
    * @description
    */
    public Boolean projectMemberAccess(Context context, String[] args) {
        Boolean flag = Boolean.TRUE;
        try{
            Map params = JPO.unpackArgs(args);
            String strObjectId = (String) params.get("objectId");
            DomainObject projectObject = DomainObject.newInstance(context, strObjectId);
            //拿取项目的成员以及关系属性project role
            MapList mapList = projectObject.getRelatedObjects(
                    context,
                    DomainRelationship.RELATIONSHIP_MEMBER,
                    DomainConstants.TYPE_PERSON,
                    new StringList(SELECT_NAME),
                    new StringList(),
                    false,
                    true,
                    (short) 1, // recursion level
                    "", //object where clause
                    "", //relationship where clause
                    0
            );
            StringList projectRoleList = (StringList) mapList.stream().map(m -> {
                Map map = (Map) m;
                return UIUtil.getValue(map, DomainConstants.SELECT_NAME);
            }).collect(Collectors.toCollection(StringList::new));
            if (projectRoleList.contains(context.getUser())) {
                flag = Boolean.TRUE;
            } else {
                flag = Boolean.FALSE;
            }
        } catch (Exception e) {
            e.printStackTrace();
            flag = Boolean.FALSE;
        }
        return flag;
    }

    /**
    * 获取项目成员id
    * @param context
	* @param args
    * @author LIUJR
    * @throws
    * @return matrix.util.StringList
    * @date 2025/2/19 14:31
    * @description
    */
    public StringList getSnapshotConnProjectPerson(Context context, String[] args) throws Exception{
        StringList resultList = new StringList();
        Boolean isPush = Boolean.FALSE;
        try {
            Map program = (Map)JPO.unpackArgs(args);
            ContextUtil.pushContext(context);
            JF_LOGGER.info("program:{}", program.toString());
            String strSnapshotId = (String)program.get("parentOID");
            JF_LOGGER.info("parentOID:{}", strSnapshotId.toString());
            DomainObject objectSnapshot = DomainObject.newInstance(context, strSnapshotId);
            String projectId = objectSnapshot.getInfo(context, "to[JFProject2Snapshot].from.id");
            if (UIUtil.isNotNullAndNotEmpty(projectId)) {
                resultList = JF_PublicMethodClass_mxJPO.getProjectAllPersons(context, projectId, SELECT_ID);
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (isPush) {
                ContextUtil.popContext(context);
            }
            throw new RuntimeException(e);
        }
        JF_LOGGER.info("resultList:{}", resultList.toString());
        return resultList;
    }

    /**
     * 编辑快照责任人、最终报价版本及产品配置表关系
     **
     * @param context
     * @param args 编辑表单请求参数
     * @return void
     * @throws Exception
     * @author caipan
     * @date 2026/7/29 18:13
     */
    public void editJFSnapshotOwner(Context context, String[] args) throws Exception {
        JF_LOGGER.info("method:editJFSnapshotOwner start...");
        boolean isPush = false;
        boolean isTransactionStarted = false;
        try {
            Map paramsMap = (Map) JPO.unpackArgs(args);
            Map requestMap = (Map) paramsMap.get(STRING_REQUESTMAP);
            Map paramMap = (Map) paramsMap.get(STRING_PARAMMAP);
            String objectId = (String) requestMap.get(STRING_OBJECTID);
            String strOwner = (String) paramMap.get("Owner");
            String snapshotSubType = (String) requestMap.get(ATTR_JF_SNAPSHOT_SUB_TYPE);
            String finalQuotationVersion = (String) requestMap.get(ATTR_FINAL_QUOTATION_VERSION);
            if (finalQuotationVersion == null) {
                finalQuotationVersion = (String) paramMap.get(ATTR_FINAL_QUOTATION_VERSION);
            }
            String productConfigId = (String) requestMap.get(FIELD_SNAPSHOT_PRODUCT_CONFIG);
            DomainObject snapshot = DomainObject.newInstance(context, objectId);
            Map snapshotAccessInfo = snapshot.getInfo(context, new StringList(new String[]{
                    SELECT_OWNER, SELECT_CURRENT, "attribute[" + ATTR_FINAL_QUOTATION_VERSION + "]",
                    "attribute[" + ATTR_JF_SNAPSHOT_SUB_TYPE + "]"
            }));
            String currentUser = context.getUser();
            String currentOwner = UIUtil.getValue(snapshotAccessInfo, SELECT_OWNER);
            String currentState = UIUtil.getValue(snapshotAccessInfo, SELECT_CURRENT);
            String currentSnapshotSubType = UIUtil.getValue(snapshotAccessInfo,
                    "attribute[" + ATTR_JF_SNAPSHOT_SUB_TYPE + "]");
            if (UIUtil.isNullOrEmpty(strOwner)) {
                strOwner = currentOwner;
            }
            String oldFinalQuotationVersion = UIUtil.getValue(snapshotAccessInfo,
                    "attribute[" + ATTR_FINAL_QUOTATION_VERSION + "]");
            boolean isOwner = currentUser.equals(currentOwner);
            boolean isOwnerInEditableState = isOwner
                    && ("DRAFT".equals(currentState)
                    || ("FROZEN".equals(currentState) && "Quote".equals(currentSnapshotSubType)));
            boolean isChairManager = isSnapshotProjectChairManager(context, objectId);
            //20260820 update by caipan Owner在草稿和冻结状态可编辑定点报价数据，项目整椅经理权限保持不变。
            if (!(isChairManager || isOwnerInEditableState)) {
                throw new Exception(EnoviaResourceBundle.getProperty(context, SUITE_KEY, context.getLocale(),
                        "emxComponents.Snapshot.EditAccessDenied"));
            }
            String oldProductConfigId = snapshot.getInfo(context,
                    "from[" + REL_SNAPSHOT_TO_PRODUCT_CONFIG + "].to.id");
            if (productConfigId == null) {
                productConfigId = oldProductConfigId;
            }
            if (UIUtil.isNullOrEmpty(snapshotSubType)) {
                snapshotSubType = snapshot.getAttributeValue(context, ATTR_JF_SNAPSHOT_SUB_TYPE);
            }
            if (finalQuotationVersion == null) {
                finalQuotationVersion = snapshot.getAttributeValue(context, ATTR_FINAL_QUOTATION_VERSION);
            }
            //20260806 update by caipan 编辑时按最终提交值校验Quote最终报价版本唯一性，并排除当前快照
            boolean isFinalQuoteSnapshot = "Quote".equals(snapshotSubType)
                    && "Y".equals(finalQuotationVersion);
            if (!"Quote".equals(snapshotSubType)) {
                finalQuotationVersion = EMPTY_STRING;
            } else if (!("Y".equals(finalQuotationVersion) || "N".equals(finalQuotationVersion))) {
                finalQuotationVersion = EMPTY_STRING;
            }
            //20260820 update by caipan 定点报价数据仅允许Owner编辑，移除项目整椅经理的字段编辑权限。
            boolean canEditFinalQuotationVersion = isOwnerInEditableState;
            if (!canEditFinalQuotationVersion
                    && !Objects.equals(oldFinalQuotationVersion, finalQuotationVersion)) {
                throw new Exception(EnoviaResourceBundle.getProperty(context, SUITE_KEY, context.getLocale(),
                        "emxComponents.Snapshot.EditAccessDenied"));
            }
            String projectId = snapshot.getInfo(context, "to[" + rel_JFProject2Snapshot + "].from.id");
            if (isFinalQuoteSnapshot) {
                //20260820 update by caipan 非草稿快照将定点报价数据改为是时，关联零件必须全部发布。
                if (!Objects.equals(oldFinalQuotationVersion, finalQuotationVersion)
                        && !"DRAFT".equals(currentState)) {
                    validateFinalQuotationPartsReleased(context, objectId);
                }
                validateFinalQuotationVersionUnique(context, projectId, objectId);
            }
            if (isProductConfigRequiredSubType(snapshotSubType)) {
                if (UIUtil.isNullOrEmpty(productConfigId)) {
                    throw new Exception(EnoviaResourceBundle.getProperty(context, SUITE_KEY, context.getLocale(),
                            "emxComponents.Snapshot.ProductConfigRequired"));
                }
                String productConfigType = DomainObject.newInstance(context, productConfigId)
                        .getInfo(context, SELECT_TYPE);
                StringList projectProductConfigIds = DomainObject.newInstance(context, projectId)
                        .getInfoList(context, "from[" + REL_PROJECT_TO_PRODUCT_CONFIG + "].to.id");
                if (!TYPE_PRODUCT_CONFIG_TABLE.equals(productConfigType)
                        || !projectProductConfigIds.contains(productConfigId)) {
                    throw new Exception(EnoviaResourceBundle.getProperty(context, SUITE_KEY, context.getLocale(),
                            "emxComponents.Snapshot.ProductConfigNotInProject"));
                }
            } else {
                productConfigId = EMPTY_STRING;
            }

            ContextUtil.startTransaction(context, true);
            isTransactionStarted = true;
            ContextUtil.pushContext(context);
            isPush = true;
            JF_Util_mxJPO jfUtilMxJPO = new JF_Util_mxJPO();
            // 设置ECO的owner项目经理
            snapshot.setOwner(context,strOwner);
            if (canEditFinalQuotationVersion) {
                snapshot.setAttributeValue(context, ATTR_FINAL_QUOTATION_VERSION, finalQuotationVersion);
            }
            String organization = jfUtilMxJPO.getPersonOrganization(context, JF_PLMConstants_mxJPO.USER_Admin_Platform);
            String project = snapshot.getProjectOwner(context).getName();
            // 设置ECO的项目和组织
            snapshot.setPrimaryOwnership(context, project, organization);
            if (!Objects.equals(oldProductConfigId, productConfigId)) {
                StringList productConfigRelIds = snapshot.getInfoList(context,
                        "from[" + REL_SNAPSHOT_TO_PRODUCT_CONFIG + "].id");
                StringList snapshotPartRelIds = snapshot.getInfoList(context,
                        "from[" + rel_JFSnapshot2VPMReference + "].id");
                if (!productConfigRelIds.isEmpty()) {
                    DomainRelationship.disconnect(context, productConfigRelIds.toStringArray());
                }
                if (!snapshotPartRelIds.isEmpty()) {
                    DomainRelationship.disconnect(context, snapshotPartRelIds.toStringArray());
                }
                if (UIUtil.isNotNullAndNotEmpty(productConfigId)) {
                    snapshot.addToObject(context, new RelationshipType(REL_SNAPSHOT_TO_PRODUCT_CONFIG), productConfigId);
                    connectSnapshotProductConfigSupplyParts(context, snapshot, productConfigId, snapshotSubType);
                }
            }
            ContextUtil.commitTransaction(context);
            isTransactionStarted = false;
        } catch (Exception e) {
            e.printStackTrace();
            if (isTransactionStarted) {
                ContextUtil.abortTransaction(context);
            }
            throw e;
        } finally {
            if (isPush) {
                ContextUtil.popContext(context);
            }
        }
        JF_LOGGER.info("method:editJFSnapshotOwner end...");
    }
    /**
     * 根据快照类型创建手动通知人、Costing和项目经理通知任务
     **
     * @param context
     * @param args 通知请求参数
     * @return Map 通知流程创建结果
     * @throws Exception
     * @author caipan
     * @date 2026/7/29 20:10
     */
    @PostProcessCallable
    public Map createInboxTaskAndSendNotice(Context context,String[] args) throws Exception{
        JF_LOGGER.info("--------------------------------- createInboxTaskAndSendNotice begin ---------------------------------------------");
        Map returnMap = new HashMap();
        Map argsMap = JPO.unpackArgs(args);
        String strCode = "200" ;
        String strMess = "" ;
        try {
            String strObjectId = (String) argsMap.get("objectId");
            String strPersonIds = (String) argsMap.get("JFSTDPerson");
            JF_LOGGER.info("strObjectId:{}",strObjectId);
            JF_LOGGER.info("strPersonIds:{}",strPersonIds);
            DomainObject snapshot = DomainObject.newInstance(context, strObjectId);
            StringList snapshotSelects = StringList.create(
                    "attribute[" + ATTR_JF_SNAPSHOT_SUB_TYPE + "]",
                    "to[" + rel_JFProject2Snapshot + "].from.id"
            );
            Map snapshotInfo = snapshot.getInfo(context, snapshotSelects);
            String snapshotSubType = UIUtil.getValue(snapshotInfo,
                    "attribute[" + ATTR_JF_SNAPSHOT_SUB_TYPE + "]");
            String projectId = UIUtil.getValue(snapshotInfo,
                    "to[" + rel_JFProject2Snapshot + "].from.id");
            boolean needCosting = COSTING_NOTICE_SUB_TYPES.contains(snapshotSubType);
            boolean needProjectManager = PROJECT_MANAGER_NOTICE_SUB_TYPES.contains(snapshotSubType);
            String costingPersonId = EMPTY_STRING;
            String projectManagerPersonId = EMPTY_STRING;
            if (needCosting) {
                costingPersonId = JF_PublicMethodClass_mxJPO.getProjectWherePerson(context, projectId,
                        SELECT_ID, EMPTY_STRING, "attribute[Project Role]=='Costing'");
                if (UIUtil.isNullOrEmpty(costingPersonId)) {
                    strMess = EnoviaResourceBundle.getProperty(context, SUITE_KEY, context.getLocale(),
                            "emxComponents.Snapshot.CostingNotFound");
                    throw new Exception(strMess);
                }
            }
            if (needProjectManager) {
                projectManagerPersonId = JF_PublicMethodClass_mxJPO.getProjectWherePerson(context, projectId,
                        SELECT_ID, EMPTY_STRING, "attribute[Project Role]=='Project manager'");
                if (UIUtil.isNullOrEmpty(projectManagerPersonId)) {
                    strMess = EnoviaResourceBundle.getProperty(context, SUITE_KEY, context.getLocale(),
                            "emxComponents.Snapshot.ProjectManagerNotFound");
                    throw new Exception(strMess);
                }
            }
            String strTitle = EnoviaResourceBundle.getProperty(context, SUITE_KEY, context.getLocale(),
                    "emxComponents.Notice.Title.SnapshotTitle");
            String costingTitle = EnoviaResourceBundle.getProperty(context, SUITE_KEY, context.getLocale(),
                    "emxComponents.Snapshot.Notice.CostingTitle");
            String projectManagerTitle = EnoviaResourceBundle.getProperty(context, SUITE_KEY, context.getLocale(),
                    "emxComponents.Snapshot.Notice.ProjectManagerTitle");
            Set<String> manualPersonIdSet = new LinkedHashSet<>();
            if (UIUtil.isNotNullAndNotEmpty(strPersonIds)) {
                for (String personId : strPersonIds.split(",")) {
                    String trimmedPersonId = personId.trim();
                    if (UIUtil.isNotNullAndNotEmpty(trimmedPersonId)) {
                        manualPersonIdSet.add(trimmedPersonId);
                    }
                }
            }
            Set<String> specialPersonIdSet = new HashSet<>();
            if (needCosting) {
                specialPersonIdSet.add(costingPersonId);
            }
            if (needProjectManager) {
                specialPersonIdSet.add(projectManagerPersonId);
            }
            MapList inboxTaskMapList = new MapList();
            for (String strPersonId : manualPersonIdSet) {
                if (specialPersonIdSet.contains(strPersonId)) {
                    continue;
                }
                Map inboxTaskMap = JF_PublicMethodClass_mxJPO.getInboxTaskMap(strPersonId, strTitle, "true", "1", "All", "Comment",strTitle);
                inboxTaskMapList.add(inboxTaskMap);
            }
            if (needCosting) {
                inboxTaskMapList.add(JF_PublicMethodClass_mxJPO.getInboxTaskMap(costingPersonId,
                        costingTitle, "true", "1", "All", "Comment", costingTitle));
            }
            if (needProjectManager) {
                inboxTaskMapList.add(JF_PublicMethodClass_mxJPO.getInboxTaskMap(projectManagerPersonId,
                        projectManagerTitle, "true", "1", "All", "Comment", projectManagerTitle));
            }
            //创建流程
            JF_Route_mxJPO jf_route = new JF_Route_mxJPO(context, args);
            String state = "state_FROZEN";//在哪个状态增加流程
            String policy = "policy_JFSnapshot";//哪个Policy上面
            jf_route.createAndStartRouteForNotify(context, inboxTaskMapList, strObjectId, state, policy, strTitle);
        } catch (Exception e) {
            JF_LOGGER.error(e.getMessage());
            strCode = "404";
            if (UIUtil.isNullOrEmpty(strMess)) {
                strMess = EnoviaResourceBundle.getProperty(context, SUITE_KEY, context.getLocale(),
                        "emxComponents.Mess.SnapShot.NoticeFail");
            }
        }
        JF_LOGGER.info("--------------------------------- createInboxTaskAndSendNotice end ---------------------------------------------");
        returnMap.put("code",strCode);
        returnMap.put("mess",strMess);
        return returnMap;
    }

    /**
     * 构造快照创建、查看和编辑页面的产品配置表字段
     **
     * @param context
     * @param args 表单程序参数
     * @return String 查看模式返回产品配置表名称，创建及草稿编辑模式返回选择字段HTML
     * @throws Exception
     * @author caipan
     * @date 2026/7/29 18:30
     */
    public String getCreateSnapshotProductConfigField(Context context, String[] args) throws Exception {
        Map programMap = JPO.unpackArgs(args);
        Map requestMap = (Map) programMap.get("requestMap");
        String objectId = requestMap == null ? EMPTY_STRING : (String) requestMap.get(STRING_OBJECTID);
        String mode = requestMap == null ? EMPTY_STRING : (String) requestMap.get("mode");
        String projectId = requestMap == null ? EMPTY_STRING : (String) requestMap.get("parentOID");
        String productConfigId = EMPTY_STRING;
        String productConfigDisplay = EMPTY_STRING;
        String snapshotCurrent = EMPTY_STRING;
        Map productConfigInfo = null;
        boolean isSnapshotObject = false;
        if (UIUtil.isNotNullAndNotEmpty(objectId)) {
            DomainObject contextObject = DomainObject.newInstance(context, objectId);
            String objectType = contextObject.getInfo(context, SELECT_TYPE);
            isSnapshotObject = TYPE_JFSnapshot.equals(objectType);
            //20260806 update by caipan 创建页objectId可能是项目ID，只有快照对象才进入查看、编辑分支
            if (isSnapshotObject) {
                StringList snapshotSelects = new StringList();
                snapshotSelects.add(SELECT_CURRENT);
                snapshotSelects.add("to[" + rel_JFProject2Snapshot + "].from.id");
                snapshotSelects.add("from[" + REL_SNAPSHOT_TO_PRODUCT_CONFIG + "].to.id");
                Map snapshotInfo = contextObject.getInfo(context, snapshotSelects);
                snapshotCurrent = UIUtil.getValue(snapshotInfo, SELECT_CURRENT);
                projectId = UIUtil.getValue(snapshotInfo, "to[" + rel_JFProject2Snapshot + "].from.id");
                productConfigId = UIUtil.getValue(snapshotInfo,
                        "from[" + REL_SNAPSHOT_TO_PRODUCT_CONFIG + "].to.id");
                if (UIUtil.isNotNullAndNotEmpty(productConfigId)) {
                    StringList productConfigSelects = new StringList();
                    productConfigSelects.add(SELECT_NAME);
                    productConfigSelects.add(SELECT_REVISION);
                    productConfigSelects.add(SELECT_ATTRIBUTE_TITLE);
                    productConfigInfo = DomainObject.newInstance(context, productConfigId)
                            .getInfo(context, productConfigSelects);
                } else if ("edit".equalsIgnoreCase(mode) && "DRAFT".equals(snapshotCurrent)) {
                    productConfigInfo = getLatestReleasedProductConfig(context, projectId);
                    if (productConfigInfo != null) {
                        productConfigId = UIUtil.getValue(productConfigInfo, SELECT_ID);
                    }
                }
            } else {
                if (UIUtil.isNullOrEmpty(projectId) && DomainConstants.TYPE_PROJECT_SPACE.equals(objectType)) {
                    projectId = objectId;
                }
                productConfigInfo = getLatestReleasedProductConfig(context, projectId);
                if (productConfigInfo != null) {
                    productConfigId = UIUtil.getValue(productConfigInfo, SELECT_ID);
                }
            }
        } else {
            productConfigInfo = getLatestReleasedProductConfig(context, projectId);
            if (productConfigInfo != null) {
                productConfigId = UIUtil.getValue(productConfigInfo, SELECT_ID);
            }
        }
        if (productConfigInfo != null) {
            String title = UIUtil.getValue(productConfigInfo, SELECT_ATTRIBUTE_TITLE);
            String name = UIUtil.getValue(productConfigInfo, SELECT_NAME);
            String revision = UIUtil.getValue(productConfigInfo, SELECT_REVISION);
            productConfigDisplay = (UIUtil.isNullOrEmpty(title) ? name : title) + " - " + revision;
        }
        if ("view".equalsIgnoreCase(mode)
                || (isSnapshotObject && !"DRAFT".equals(snapshotCurrent))) {
            return StringEscapeUtils.escapeHtml4(productConfigDisplay);
        }
        String requiredMessage = EnoviaResourceBundle.getProperty(context, SUITE_KEY, context.getLocale(),
                "emxComponents.Snapshot.ProductConfigRequired");
        String clearLabel = EnoviaResourceBundle.getProperty(context, "emxFrameworkStringResource", context.getLocale(),
                "emxFramework.Common.Clear");
        String escapedId = StringEscapeUtils.escapeHtml4(productConfigId);
        String escapedDisplay = StringEscapeUtils.escapeHtml4(productConfigDisplay);
        StringBuilder html = new StringBuilder();
        html.append("<input type=\"hidden\" id=\"JFSnapshotProductConfig\" name=\"JFSnapshotProductConfig\" value=\"")
                .append(escapedId).append("\"/>");
        html.append("<input type=\"hidden\" id=\"JFSnapshotDefaultProductConfig\" name=\"JFSnapshotDefaultProductConfig\" value=\"")
                .append(escapedId).append("\"/>");
        html.append("<input type=\"hidden\" id=\"JFSnapshotDefaultProductConfigDisplay\" name=\"JFSnapshotDefaultProductConfigDisplay\" value=\"")
                .append(escapedDisplay).append("\"/>");
        html.append("<input type=\"hidden\" id=\"JFSnapshotProductConfigProjectId\" name=\"JFSnapshotProductConfigProjectId\" value=\"")
                .append(StringEscapeUtils.escapeHtml4(projectId)).append("\"/>");
        html.append("<input type=\"hidden\" id=\"JFSnapshotProductConfigRequiredMessage\" name=\"JFSnapshotProductConfigRequiredMessage\" value=\"")
                .append(StringEscapeUtils.escapeHtml4(requiredMessage)).append("\"/>");
        html.append("<input type=\"text\" id=\"JFSnapshotProductConfigDisplay\" name=\"JFSnapshotProductConfigDisplay\" value=\"")
                .append(escapedDisplay).append("\" readonly=\"readonly\" style=\"width:260px;\"/>");
        html.append("<input type=\"button\" value=\"...\" onclick=\"showSnapshotProductConfigSelector();return false;\"/>");
        html.append("<input type=\"button\" value=\"").append(StringEscapeUtils.escapeHtml4(clearLabel))
                .append("\" onclick=\"clearSnapshotProductConfigSelection();return false;\"/>");
        //20260806 update by caipan 首次加载时复用类型联动逻辑，类型为空或无需产品配置表时隐藏整行
        html.append("<script type=\"text/javascript\">window.setTimeout(function(){")
                .append("if(typeof updateSnapshotProductConfigField==='function'){updateSnapshotProductConfigField();}")
                .append("},0);</script>");
        return html.toString();
    }

    /**
     * 获取项目中最近一次到达发布状态的产品配置表
     **
     * @param context
     * @param projectId 当前项目ID
     * @return Map 最新发布产品配置表信息；不存在时返回null
     * @throws Exception
     * @author caipan
     * @date 2026/7/29 16:13
     */
    private Map getLatestReleasedProductConfig(Context context, String projectId) throws Exception {
        if (UIUtil.isNullOrEmpty(projectId)) {
            return null;
        }
        StringList productConfigIds = DomainObject.newInstance(context, projectId)
                .getInfoList(context, "from[" + REL_PROJECT_TO_PRODUCT_CONFIG + "].to.id");
        if (productConfigIds == null || productConfigIds.isEmpty()) {
            return null;
        }
        //20260806 update by caipan 过滤关系查询返回的空ID，项目没有产品配置表时按无默认值处理，保证创建字段正常显示
        StringList validProductConfigIds = new StringList();
        for (Object productConfigIdObject : productConfigIds) {
            String currentProductConfigId = productConfigIdObject == null
                    ? EMPTY_STRING : productConfigIdObject.toString();
            if (UIUtil.isNotNullAndNotEmpty(currentProductConfigId)) {
                validProductConfigIds.add(currentProductConfigId);
            }
        }
        if (validProductConfigIds.isEmpty()) {
            return null;
        }
        StringList selects = new StringList();
        selects.add(SELECT_ID);
        selects.add(SELECT_NAME);
        selects.add(SELECT_REVISION);
        selects.add(SELECT_ATTRIBUTE_TITLE);
        selects.add(SELECT_CURRENT);
        selects.add(SELECT_RELEASED_ACTUAL);
        MapList productConfigInfoList = DomainObject.getInfo(context,
                validProductConfigIds.toStringArray(), selects);
        MapList releasedProductConfigList = new MapList();
        for (Object productConfigInfo : productConfigInfoList) {
            Map info = (Map) productConfigInfo;
            //20260806 update by caipan 产品配置表当前状态达到Released或后续状态时，才作为最新发布版本候选
            String current = UIUtil.getValue(info, SELECT_CURRENT);
            if (RELEASED_OR_LATER_PRODUCT_CONFIG_STATES.contains(current)) {
                releasedProductConfigList.add(info);
            }
        }
        if (releasedProductConfigList.isEmpty()) {
            return null;
        }
        releasedProductConfigList.addSortKey(SELECT_RELEASED_ACTUAL,
                ProgramCentralConstants.DESCENDING_SORT, ProgramCentralConstants.SORTTYPE_DATE);
        releasedProductConfigList.sort();
        return (Map) releasedProductConfigList.get(0);
    }

    /**
     * 判断快照子类型是否必须选择产品配置表
     **
     * @param snapshotSubType 快照子类型
     * @return boolean 指定五种快照子类型返回true
     * @author caipan
     * @date 2026/7/29 16:13
     */
    private boolean isProductConfigRequiredSubType(String snapshotSubType) {
        return PRODUCT_CONFIG_REQUIRED_SUB_TYPES.contains(snapshotSubType);
    }

    /**
     * 校验项目内Quote类型的最终报价版本快照唯一性
     **
     * @param context
     * @param projectId 项目ID
     * @param excludeSnapshotId 编辑时排除的当前快照ID，创建时为空
     * @return void
     * @throws Exception 已存在其他Quote最终报价版本快照时抛出国际化提示
     * @author caipan
     * @date 2026/8/6 16:13
     */
    private void validateFinalQuotationVersionUnique(Context context, String projectId,
                                                      String excludeSnapshotId) throws Exception {
        StringList projectSnapshotIds = DomainObject.newInstance(context, projectId)
                .getInfoList(context, "from[" + rel_JFProject2Snapshot + "].to.id");
        StringList validProjectSnapshotIds = new StringList();
        if (projectSnapshotIds != null) {
            for (Object projectSnapshotIdObject : projectSnapshotIds) {
                String projectSnapshotId = projectSnapshotIdObject == null
                        ? EMPTY_STRING : projectSnapshotIdObject.toString();
                if (UIUtil.isNotNullAndNotEmpty(projectSnapshotId)
                        && !projectSnapshotId.equals(excludeSnapshotId)) {
                    validProjectSnapshotIds.add(projectSnapshotId);
                }
            }
        }
        if (validProjectSnapshotIds.isEmpty()) {
            return;
        }
        StringList snapshotSelects = new StringList();
        snapshotSelects.add(SELECT_ATTRIBUTE_TITLE);
        snapshotSelects.add("attribute[" + ATTR_JF_SNAPSHOT_SUB_TYPE + "]");
        snapshotSelects.add("attribute[" + ATTR_FINAL_QUOTATION_VERSION + "]");
        MapList snapshotInfoList = DomainObject.getInfo(context,
                validProjectSnapshotIds.toStringArray(), snapshotSelects);
        for (Object snapshotInfoObject : snapshotInfoList) {
            Map snapshotInfo = (Map) snapshotInfoObject;
            if ("Quote".equals(UIUtil.getValue(snapshotInfo,
                    "attribute[" + ATTR_JF_SNAPSHOT_SUB_TYPE + "]"))
                    && "Y".equals(UIUtil.getValue(snapshotInfo,
                    "attribute[" + ATTR_FINAL_QUOTATION_VERSION + "]"))) {
                String existingSnapshotTitle = UIUtil.getValue(snapshotInfo, SELECT_ATTRIBUTE_TITLE);
                String uniqueMessage = EnoviaResourceBundle.getProperty(context, SUITE_KEY,
                        context.getLocale(), "emxComponents.Snapshot.FinalQuotationVersionUnique");
                throw new Exception(uniqueMessage + existingSnapshotTitle);
            }
        }
    }

    /**
    *
    *@description 构造多选人的html，JFViewSnapshotForm中冻结状态且当前用户为快照Owner时只读展示
    *@param context
	*@param args
    *@return java.lang.String
    *@throws
    *@author CHENYAN
    *@date 2025/4/11 15:07
    */
    public String buildMultipleChoiceSTDPersonHtml(Context context, String[] args) throws Exception {
        StringBuilder sb = new StringBuilder();
        //添加移除文字提示
        String strAddButton = EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", context.getLocale(), "emxComponents.Button.AddWCharDevEngineering");
        String strRemoveButton = EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", context.getLocale(), "emxComponents.Button.RemoveWCharDevEngineering");
        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        HashMap requestMap = (HashMap) programMap.get("requestMap");
        Map fieldMap = (Map) programMap.get("fieldMap");
        Map settings = fieldMap == null ? null : (Map) fieldMap.get("settings");
        JF_LOGGER.info("settings:{}",settings);
        String strMode = (String) requestMap.get("mode");
        String strObjectId = (String) requestMap.get("objectId");
        JF_LOGGER.info("strMode:{}", strMode);
        JF_LOGGER.info("strObjectId:{}", strObjectId);
        if (UIUtil.isNotNullAndNotEmpty(strObjectId)) {
            StringList personList ;
            boolean isFrozenOwnerReadOnly = false;
            //20260903 update by caipan 提权前保存页面登录用户，避免pushContext后用户身份变化导致Owner判断失败
            String loginUser = context.getUser();
            try {
                ContextUtil.pushContext(context);
                DomainObject bo = DomainObject.newInstance(context, strObjectId);
                String type = bo.getInfo(context, SELECT_TYPE);
                //20260903 update by caipan 通过字段配置识别JFViewSnapshotForm，避免依赖programHTMLOutput未稳定传入的form参数
                boolean frozenOwnerReadOnlyEnabled = settings != null
                        && "true".equalsIgnoreCase((String) settings.get("Frozen Owner Read Only"));
                JF_LOGGER.info("frozenOwnerReadOnlyEnabled:{}",frozenOwnerReadOnlyEnabled);
                if ("edit".equalsIgnoreCase(strMode) && frozenOwnerReadOnlyEnabled
                        && TYPE_JFSnapshot.equals(type)) {
                    Map snapshotInfo = bo.getInfo(context, new StringList(new String[]{SELECT_OWNER, SELECT_CURRENT}));
                    boolean isOwnerInFrozen = loginUser.equals(UIUtil.getValue(snapshotInfo, SELECT_OWNER))
                            && "FROZEN".equals(UIUtil.getValue(snapshotInfo, SELECT_CURRENT));
                    //20260903 update by caipan 冻结状态下快照Owner不允许编辑通知人员
                    isFrozenOwnerReadOnly = isOwnerInFrozen;
                }
                JF_LOGGER.info("isFrozenOwnerReadOnly:{}",isFrozenOwnerReadOnly);
                //查看视图返回name 编辑返回id
                if("view".equalsIgnoreCase(strMode)) {
                    personList = bo.getInfoList(context, "from[JFSnapshot2Member].to.name");
                }else if ("edit".equalsIgnoreCase(strMode)){
                    personList = bo.getInfoList(context, "from[JFSnapshot2Member].to.id");
                } else {
                    //拿取项目
                    String psId = EMPTY_STRING;
                    if (TYPE_PROJECT_SPACE.equalsIgnoreCase(type)) {
                        psId = strObjectId;
                    } else {
                        psId = bo.getInfo(context, "to[JFProject2Snapshot].from.id");
                    }
                    bo.setId(psId);
                    JF_LOGGER.info("psId:{}",psId);
                    personList = (StringList) bo.getRelatedObjects(
                            context,
                            RELATIONSHIP_MEMBER,
                            TYPE_PERSON,
                            new StringList(SELECT_ID),
                            new StringList("attribute[Project Role]"),
                            false,
                            true,
                            (short) 1,
                            "",
                            "attribute[Project Role]!=''",
                            0
                    ).stream().map(m -> {
                        Map map = (Map) m;
                        return  UIUtil.getValue(map, SELECT_ID);
                    }).collect(Collectors.toCollection(StringList::new));
                }
                JF_LOGGER.info("personList:{}",personList);
            } finally {
                ContextUtil.popContext(context);
            }
            //20260820 update by caipan 冻结状态Owner进入编辑表单时，通知人员保持只读并提交原值。
            if (isFrozenOwnerReadOnly) {
                StringList fullNameList = new StringList();
                for (String personId : personList) {
                    fullNameList.add(JF_PublicMethodClass_mxJPO.getPersonAllName(context, personId));
                }
                sb.append("<input type=\"hidden\" name=\"JFSTDPerson\" id=\"JFSTDPerson\" value=\"")
                        .append(personList.join(","))
                        .append("\" readonly=\"readonly\" />");
                sb.append(StringEscapeUtils.escapeHtml4(fullNameList.join(",")));
                return sb.toString();
            }
            if("view".equalsIgnoreCase(strMode)) {
                StringList fullNameList = new StringList();
                for (int i = 0; i < personList.size(); i++) {
                    String strPersonName = personList.get(i);
                    String strFullName = PersonUtil.getFullName(context, strPersonName);
                    fullNameList.add(strFullName);
                }
                JF_LOGGER.info("fullNameList:{}", fullNameList);
            return fullNameList.join(",");
            }
            //XSSOK
            sb.append("<input type=\"hidden\" name=\"JFSTDPersonFieldModified\" id=\"JFSTDPersonFieldModified\" value=\"false\" readonly=\"readonly\" />");
            //XSSOK
            sb.append("<input type=\"hidden\" name=\"JFSTDPerson\" id=\"JFSTDPerson\" value=\"" + personList.join(",") + "\" readonly=\"readonly\" />");
            sb.append("<table>");
            sb.append("<tr>");
            sb.append("<th rowspan=\"7\">");
            sb.append("<select name=\"JFSTDPersonSelect\" style=\"width:200px\" multiple=\"multiple\">");
            if (Objects.nonNull(personList) && personList.size() > 0) {
                for (int i = 0; i < personList.size(); i++) {
                    String strPersonId = personList.get(i);
                    sb.append("<option value=\"" + strPersonId  + "\" >");
                    //XSSOK
                    String strFullName = JF_PublicMethodClass_mxJPO.getPersonAllName(context,strPersonId) ;
                    strFullName = StringEscapeUtils.escapeHtml4(strFullName);
                    sb.append(strFullName);
                    sb.append("</option>");
                }
            }
            sb.append("</select>");
            sb.append("</th>");
            sb.append("<td>");
            sb.append("<a href=\"javascript:addJSSTDPerson()\">");
            sb.append("<img src=\"../common/images/iconStatusAdded.gif\" width=\"12\" height=\"12\" border=\"0\" />");
            sb.append("</a>");
            sb.append("<a href=\"javascript:addJSSTDPerson()\">");
            //XSSOK
            sb.append(strAddButton);
            sb.append("</a>");
            //sb.append("</div>");
            sb.append("</td>");
            sb.append("</tr>");
            sb.append("<tr>");
            sb.append("<td>");
            sb.append("<a href=\"javascript:removeJSSTDPerson()\">");
            sb.append("<img src=\"../common/images/iconStatusRemoved.gif\" width=\"12\" height=\"12\" border=\"0\" />");
            sb.append("</a>");
            sb.append("<a href=\"javascript:removeJSSTDPerson()\">");
            //XSSOK
            sb.append(strRemoveButton);
            sb.append("</a>");
            sb.append("</td>");
            sb.append("</tr>");
            sb.append("</table>");
//            sb.append("<script language=\"JavaScript\">");
//            sb.append("function checkJSSTDPersonIsNotNull(){\n" +
//                    "        const strContributorHidden = document.getElementById(\"JFSTDPerson\").value;\n" +
//                    "        if (strContributorHidden){\n" +
//                    "            return true;\n" +
//                    "        } else{" +
//                    "//拿到浏览器语言\n" +
//                    "    var language = navigator.language || navigator.userLanguage;\n" +
//                    "    var strMess = \"\";\n" +
//                    "    if (language.includes(\"zh\") || language.includes(\"zh-CN\")) {\n" +
//                    "        strMess = \"\u5fc5\u987b\u8f93\u5165\u6709\u6548\u503c\uff1a \u901a\u77e5\u4eba\u5458\u4e0d\u80fd\u4e3a\u7a7a\"" +
//                    "    }else {\n" +
//                    "        strMess = \"Valid value must be entered: Notify personnel cannot be empty\";\n" +
//                    "    }" +
//                    "alert(strMess)" +
//                    "}\n" +
//                    "        return false\n" +
//                    "    }");
//            sb.append(" // 使用 addEventListener 添加第三个 onload 函数\n" +
//                    "    window.addEventListener('load', function thirdOnloadHandler() {\n" +
//                    "        console.log(\"Third onload handler called.\");\n" +
//                    "        document.getElementById('JFSTDPerson').customValidate = checkJSSTDPersonIsNotNull\n" +
//                    "    }, false);");
//            sb.append(" </script>");
            //构造必填样式
            sb.append("<script>");
            sb.append("  var  dom = document.getElementById('");
            String strDomId = "calc_JFSTDPerson";
            sb.append(strDomId);
            sb.append("');");
            sb.append("  if (dom){");
            sb.append("    dom.querySelector('td:first-child').className = \"createLabelRequired\";");
            sb.append("  }");
            sb.append("</script>");
        }
        return sb.toString();
    }

    /**
    *
    *@description 获取快照关联项目的整椅经理
    *@param context
	*@param args
    *@return java.lang.String
    *@throws
    *@author CHENYAN
    *@date 2025/4/17 12:01
    */
    public String getSnapshotReviewPerson(Context context ,String[] args) throws Exception{
        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        HashMap requestMap = (HashMap) programMap.get("requestMap");
        String strObjectId = (String) requestMap.get("objectId");
        String strPersonName = EMPTY_STRING;
        JF_LOGGER.info("strObjectId:{}", strObjectId);
            String strMql = JF_PublicMethodClass_mxJPO.buildStringInStrings("print bus ",strObjectId," select to[JFProject2Snapshot].from.from[Member|attribute[Project Role]=='Chair manager'].to.name dump ;");
            String strMQLRes =  MqlUtil.mqlCommand(context,strMql ,false);
            if (UIUtil.isNotNullAndNotEmpty(strMQLRes)){
                String[] split = strMQLRes.split(";");
                strPersonName = split[0];
            }
        return strPersonName;
    }

    /**
     * 快照编辑按钮权限
     **
     * @param context
     * @param args Command请求参数
     * @return boolean 草稿Owner、冻结状态Quote快照Owner或关联项目整椅经理时返回true
     * @throws Exception
     * @author caipan
     * @date 2026/8/6 17:30
     */
    public boolean getSnapshotEditAccess(Context context, String[] args) throws Exception {
        Map argsMap = JPO.unpackArgs(args);
        String objectId = (String) argsMap.get(STRING_OBJECTID);
        if (UIUtil.isNullOrEmpty(objectId) && argsMap.get(STRING_REQUESTMAP) instanceof Map) {
            objectId = (String) ((Map) argsMap.get(STRING_REQUESTMAP)).get(STRING_OBJECTID);
        }
        if (UIUtil.isNullOrEmpty(objectId)) {
            return false;
        }
        DomainObject snapshot = DomainObject.newInstance(context, objectId);
        Map info = snapshot.getInfo(context, new StringList(new String[]{
                SELECT_OWNER, SELECT_CURRENT, "attribute[" + ATTR_JF_SNAPSHOT_SUB_TYPE + "]"
        }));
        String currentState = UIUtil.getValue(info, SELECT_CURRENT);
        String snapshotSubType = UIUtil.getValue(info, "attribute[" + ATTR_JF_SNAPSHOT_SUB_TYPE + "]");
        boolean isOwnerInEditableState = context.getUser().equals(UIUtil.getValue(info, SELECT_OWNER))
                && ("DRAFT".equals(currentState)
                || ("FROZEN".equals(currentState) && "Quote".equals(snapshotSubType)));
        //20260820 update by caipan 冻结状态Owner可进入编辑表单修改定点报价数据。
        return isOwnerInEditableState || isSnapshotProjectChairManager(context, objectId);
    }

    /**
     * 最终报价版本字段编辑权限
     **
     * @param context
     * @param args Form字段请求参数
     * @return boolean Owner在草稿或冻结状态Quote快照可编辑
     * @throws Exception
     * @author caipan
     * @date 2026/8/6 17:30
     */
    public boolean getSnapshotFinalQuotationEditAccess(Context context, String[] args) throws Exception {
        Map argsMap = JPO.unpackArgs(args);
        Map requestMap = (Map) argsMap.get(STRING_REQUESTMAP);
        String objectId = requestMap == null ? null : (String) requestMap.get(STRING_OBJECTID);
        if (UIUtil.isNullOrEmpty(objectId)) {
            return false;
        }
        DomainObject snapshot = DomainObject.newInstance(context, objectId);
        Map info = snapshot.getInfo(context, new StringList(new String[]{
                SELECT_OWNER, SELECT_CURRENT, "attribute[" + ATTR_JF_SNAPSHOT_SUB_TYPE + "]"
        }));
        String currentState = UIUtil.getValue(info, SELECT_CURRENT);
        String snapshotSubType = UIUtil.getValue(info, "attribute[" + ATTR_JF_SNAPSHOT_SUB_TYPE + "]");
        boolean isOwnerInEditableState = context.getUser().equals(UIUtil.getValue(info, SELECT_OWNER))
                && ("DRAFT".equals(currentState)
                || ("FROZEN".equals(currentState) && "Quote".equals(snapshotSubType)));
        //20260820 update by caipan 定点报价数据仅允许Owner编辑，冻结状态仅限Quote快照。
        return isOwnerInEditableState;
    }

    /**
     * 校验并提权保存快照最终报价版本字段
     **
     * @param context
     * @param args Form字段更新参数
     * @return void
     * @throws Exception 无编辑权限或同项目存在其他最终报价快照时抛出异常
     * @author caipan
     * @date 2026/8/6 17:30
     */
    public void updateSnapshotFinalQuotationVersion(Context context, String[] args) throws Exception {
        Map argsMap = JPO.unpackArgs(args);
        Map paramMap = (Map) argsMap.get(STRING_PARAMMAP);
        String objectId = paramMap == null ? null : (String) paramMap.get(STRING_OBJECTID);
        String newValue = paramMap == null ? null : (String) paramMap.get("New Value");
        if (UIUtil.isNullOrEmpty(objectId)) {
            return;
        }
        DomainObject snapshot = DomainObject.newInstance(context, objectId);
        Map info = snapshot.getInfo(context, new StringList(new String[]{
                SELECT_OWNER, SELECT_CURRENT, "attribute[" + ATTR_JF_SNAPSHOT_SUB_TYPE + "]",
                "attribute[" + ATTR_FINAL_QUOTATION_VERSION + "]",
                "to[" + rel_JFProject2Snapshot + "].from.id"
        }));
        String currentState = UIUtil.getValue(info, SELECT_CURRENT);
        String snapshotSubType = UIUtil.getValue(info, "attribute[" + ATTR_JF_SNAPSHOT_SUB_TYPE + "]");
        String oldValue = UIUtil.getValue(info, "attribute[" + ATTR_FINAL_QUOTATION_VERSION + "]");
        boolean isOwnerInEditableState = context.getUser().equals(UIUtil.getValue(info, SELECT_OWNER))
                && ("DRAFT".equals(currentState)
                || ("FROZEN".equals(currentState) && "Quote".equals(snapshotSubType)));
        //20260820 update by caipan 后台更新权限与字段权限保持一致，禁止非Owner更新定点报价数据。
        if (!isOwnerInEditableState) {
            throw new Exception(EnoviaResourceBundle.getProperty(context, SUITE_KEY, context.getLocale(),
                    "emxComponents.Snapshot.EditAccessDenied"));
        }
        if (!"Quote".equals(snapshotSubType)) {
            newValue = EMPTY_STRING;
        } else if (!("Y".equals(newValue) || "N".equals(newValue))) {
            newValue = EMPTY_STRING;
        }
        if ("Quote".equals(snapshotSubType) && "Y".equals(newValue)
                && !Objects.equals(oldValue, newValue)) {
            //20260820 update by caipan 字段更新入口同步校验非草稿快照关联零件的发布状态。
            if (!"DRAFT".equals(currentState)) {
                validateFinalQuotationPartsReleased(context, objectId);
            }
            validateFinalQuotationVersionUnique(context,
                    UIUtil.getValue(info, "to[" + rel_JFProject2Snapshot + "].from.id"), objectId);
        }
        boolean isPush = false;
        try {
            ContextUtil.pushContext(context);
            isPush = true;
            snapshot.setAttributeValue(context, ATTR_FINAL_QUOTATION_VERSION, newValue);
        } finally {
            if (isPush) {
                ContextUtil.popContext(context);
            }
        }
    }

    /**
     * 判断当前用户是否为快照关联项目的整椅经理
     **
     * @param context
     * @param snapshotId 快照ID
     * @return boolean 是整椅经理时返回true
     * @throws Exception
     * @author caipan
     * @date 2026/8/6 17:30
     */
    private boolean isSnapshotProjectChairManager(Context context, String snapshotId) throws Exception {
        DomainObject snapshot = DomainObject.newInstance(context, snapshotId);
        String projectId = snapshot.getInfo(context, "to[" + rel_JFProject2Snapshot + "].from.id");
        if (UIUtil.isNullOrEmpty(projectId)) {
            return false;
        }
        StringList objectSelects = new StringList(SELECT_NAME);
        MapList chairManagerList = JF_PublicMethodClass_mxJPO.getProjectPersonByRoleName(context,
                projectId, ATTR_PROJECTROLE_RANGE_Chairmanager, objectSelects, new StringList());
        for (Object chairManagerObj : chairManagerList) {
            Map chairManagerMap = (Map) chairManagerObj;
            if (context.getUser().equals(UIUtil.getValue(chairManagerMap, SELECT_NAME))) {
                return true;
            }
        }
        return false;
    }

    /**
     * 根据快照类型获取初始化零件版本
     **
     * @param context
     * @param partId 产品配置表关联零件ID
     * @param snapshotSubType 快照子类型
     * @return String 优先返回最新发布版本；指定类型无发布版本时返回最新冻结版本；均不存在时返回空
     * @throws Exception
     * @author caipan
     * @date 2026/8/6 18:10
     */
    private String getSnapshotInitialPartId(Context context, String partId, String snapshotSubType) throws Exception {
        String latestReleasedPartId = JF_Util_mxJPO.getLastReleasedMajorid(context, partId);
        if (UIUtil.isNotNullAndNotEmpty(latestReleasedPartId)
                || !FROZEN_FALLBACK_PART_SUB_TYPES.contains(snapshotSubType)) {
            return latestReleasedPartId;
        }
        return getLastFrozenMajorid(context, partId);
    }

    /**
     * 获取零件所有大版本中的最新冻结版本
     **
     * @param context
     * @param partId 零件ID
     * @return String 最新冻结版本ID；不存在时返回空
     * @throws Exception
     * @author caipan
     * @date 2026/8/6 18:10
     */
    private String getLastFrozenMajorid(Context context, String partId) throws Exception {
        BusinessObjectList majorRevisionList = DomainObject.newInstance(context, partId).getMajorRevisions(context);
        BusinessObjectList frozenRevisionList = new BusinessObjectList();
        for (Object majorRevisionObj : majorRevisionList) {
            DomainObject majorRevision = (DomainObject) majorRevisionObj;
            if ("FROZEN".equals(majorRevision.getInfo(context, SELECT_CURRENT))) {
                frozenRevisionList.add(majorRevision);
            }
        }
        String latestFrozenPartId = JF_Util_mxJPO.getMaxRevisionIdStream2(context, frozenRevisionList);
        return latestFrozenPartId == null ? EMPTY_STRING : latestFrozenPartId;
    }

    /**
    *
    *@description 快照相关属性允许Owner在草稿状态编辑，项目整椅经理不限制状态
    *@param context
	*@param args
    *@return boolean
    *@throws
    *@author CHENYAN
    *@date 2025/4/17 15:00
    */
    public boolean getSnapshotFieldEditAccess(Context context,String[] args) throws Exception{
        boolean flag = true;
        try {
            Map argsMap = (Map) JPO.unpackArgs(args);
            Map requestMap = (Map) argsMap.get(STRING_REQUESTMAP);
            String strObjectId = (String) requestMap.get(STRING_OBJECTID);
            DomainObject bo = DomainObject.newInstance(context, strObjectId);
            Map info = bo.getInfo(context, new StringList(new String[]{SELECT_OWNER, SELECT_CURRENT}));
            boolean isOwnerInDraft = context.getUser().equals(UIUtil.getValue(info, SELECT_OWNER))
                    && "DRAFT".equals(UIUtil.getValue(info, SELECT_CURRENT));
            boolean isChairManager = isSnapshotProjectChairManager(context, strObjectId);
            //20260806 update by caipan Owner仅草稿状态可编辑，项目整椅经理不限制状态
            flag = isOwnerInDraft || isChairManager;
            JF_LOGGER.info("flag:{} isChairManager:{}",flag,isChairManager);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
        return flag;
    }

    @ProgramCallable
    public int checkChairPartPerson(Context context, String[] args) throws Exception {
        JF_LOGGER.info("method:checkChairPartPerson start...");
        int flag = 0;
        try {
            String strLoginUser = context.getUser();
            String objectId =args[0]; //审核对象Id
            String alertMess = DomainConstants.EMPTY_STRING;
            String strMql = JF_PublicMethodClass_mxJPO.buildStringInStrings("print bus ", objectId, " select to[JFProject2Snapshot].from.from[Member|attribute[Project Role]=='Chair manager'].to.name dump ;");
            String strMqlRes =  MqlUtil.mqlCommand(context, false, strMql, true);
            JF_LOGGER.info("strLoginUser:{}",strLoginUser);
            JF_LOGGER.info("strMqlRes:{}",strMqlRes);
            //如果项目整椅经理没维护需要提示
            if (UIUtil.isNullOrEmpty(strMqlRes) &&(!strLoginUser.equals(strMqlRes))){
                alertMess = EnoviaResourceBundle.getProperty(context, SUITE_KEY, context.getLocale(), "emxComponents.Snapshot.ChairManagerNotMaintenance");
                emxContextUtil_mxJPO.mqlNotice(context, alertMess);
                flag = 1;
            }

        }catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
        JF_LOGGER.info("method:checkChairPartPerson end...");
        return flag;
    }
    /**
     * 快照状态到冻结时自动创建手动通知人及类型角色通知任务
     **
     * @param context
     * @param args Trigger参数
     * @return void
     * @throws Exception
     * @author caipan
     * @date 2026/7/29 20:10
     */
    public void sendNotificationInFrozen(Context context, String[] args) throws Exception {
        String strObjectId =args[0];
        DomainObject bo = DomainObject.newInstance(context, strObjectId);
        StringList personIdList = bo.getInfoList(context, "from[JFSnapshot2Member].to.id");
        String snapshotSubType = bo.getAttributeValue(context, ATTR_JF_SNAPSHOT_SUB_TYPE);
        if (!personIdList.isEmpty() || COSTING_NOTICE_SUB_TYPES.contains(snapshotSubType)
                || PROJECT_MANAGER_NOTICE_SUB_TYPES.contains(snapshotSubType)) {
            String strPersonIds = personIdList.join(",");
            HashMap<Object, Object> argsMap = new HashMap<>();
            argsMap.put("objectId",strObjectId);
            argsMap.put("JFSTDPerson",strPersonIds);
            String[] packArgs = JPO.packArgs(argsMap);
            try {
                ContextUtil.startTransaction(context,true);
                Map result = createInboxTaskAndSendNotice(context,packArgs);
                if (!"200".equals(UIUtil.getValue(result, "code"))) {
                    throw new Exception(UIUtil.getValue(result, "mess"));
                }
                ContextUtil.commitTransaction(context);
            } catch (Exception e) {
                ContextUtil.abortTransaction(context);
                throw new RuntimeException(e.getMessage(), e);
            }
        }

    }
    /**
    *
    *@description 快照创建时同步项目STD人员
    *@param context
	*@param args
    *@return void
    *@throws
    *@author CHENYAN
    *@date 2025/4/18 14:27
    */
    public void synchronousSTDPerson(Context context, String[] args) throws Exception {
        String strObjectId =args[0];
        String strProjectId =args[1];
        String JFSTDPersonSelect =args[2];

        Set<String> personIdSet = new HashSet<>();
        if(UIUtil.isNotNullAndNotEmpty(JFSTDPersonSelect)){
            personIdSet  = Arrays.stream(JFSTDPersonSelect.split(","))
                    .map(String::trim)
                    .collect(Collectors.toSet());
        }
        DomainObject bo = DomainObject.newInstance(context, strObjectId);
        String strMql = JF_PublicMethodClass_mxJPO.buildStringInStrings("print bus ", strProjectId, " select from[Member|attribute[Project Role]!=''].to.id dump |");
        String strMqlRes =  MqlUtil.mqlCommand(context, false, strMql, true);
        JF_LOGGER.info("strMqlRes:{}",strMqlRes);
        if (UIUtil.isNotNullAndNotEmpty(strMqlRes)){
            StringTokenizer strTokenizer = new StringTokenizer(strMqlRes,"|");
            while (strTokenizer.hasMoreTokens()) {
                String strPersonId = strTokenizer.nextToken();
                personIdSet.add(strPersonId);
            }
            if (personIdSet.size() > 0){
                try {
                    ContextUtil.startTransaction(context,true);
                    DomainRelationship.connect(context, bo, "JFSnapshot2Member", true, StringList.create(personIdSet).toStringArray());
                    ContextUtil.commitTransaction(context);
                } catch (Exception e) {
                    ContextUtil.abortTransaction(context);
                    throw new RuntimeException(e);
                }
            }

        }

    }
    /*
     * @description:展开快照下面的BOM结构
     * @author: caipan
     * @date: 2025/9/10 16:41:41
     * @param: * @param[1] context
     * @param[2] args
     * @return:
     **/
    public MapList getExpand(Context context, String[] args)  {
        MapList childPartList = new MapList();
        try {
            Map paramMap = JPO.unpackArgs(args);
            String objId = (String)paramMap.get("objectId");
            JF_LOGGER.info("objId:{}",objId);
            String expandLevel = (String) paramMap.get("expandLevel");
            if ("All".equals(expandLevel)) {
                expandLevel = "0";
            }
            DomainObject obj = DomainObject.newInstance(context,objId);
            StringList boSel = JF_Util_mxJPO.basicBolistSel();
            StringList relSel = new StringList();
            relSel.add(DomainRelationship.SELECT_ID);
            childPartList =  obj.getRelatedObjects(context,JF_PLMConstants_mxJPO.REL_Instance,JF_PLMConstants_mxJPO.TYPE_VPMReference,
                    boSel,relSel,false,true,Short.parseShort(expandLevel),"","",0);
        }catch (Exception e){
            e.printStackTrace();
        }
        return childPartList;
    }

    /**
     * 查询当前任务所属项目的全部快照ID
     **
     * @param context Matrix上下文
     * @param args objectId为当前任务ID
     * @return StringList 当前项目的快照ID
     * @throws Exception 查询失败时抛出异常
     * @author caipan by codex
     * @date 2026/8/20 17:00
     */
    public StringList getTaskProjectSnapshotIds(Context context, String[] args) throws Exception {
        Map paramMap = JPO.unpackArgs(args);
        String taskId = UIUtil.getValue(paramMap, STRING_OBJECTID);
        boolean hasAccess = new JF_PublicProjectQuery_mxJPO().getTechnicalDocumentAccess(context, args);
        if (!hasAccess) {
            return new StringList();
        }
        String projectId = JF_PublicProjectQuery_mxJPO.getProjectIdByTaskId(context, taskId);
        return DomainObject.newInstance(context, projectId)
                .getInfoList(context, "from[" + rel_JFProject2Snapshot + "].to.id");
    }

    /**
     * 将当前项目选中的快照添加为任务交付物，已存在的关系直接跳过
     **
     * @param context Matrix上下文
     * @param args taskId为当前任务ID，snapshotIds为选中的快照ID
     * @return Map code为200表示处理成功
     * @throws Exception 权限或快照归属校验失败时抛出异常
     * @author caipan by codex
     * @date 2026/8/20 17:00
     */
    public Map addTaskDeliverableSnapshots(Context context, String[] args) throws Exception {
        Map paramMap = JPO.unpackArgs(args);
        String taskId = UIUtil.getValue(paramMap, "taskId");
        String[] snapshotIds = (String[]) paramMap.get("snapshotIds");
        Map accessMap = new HashMap();
        accessMap.put(STRING_OBJECTID, taskId);
        boolean hasAccess = new JF_PublicProjectQuery_mxJPO().getTechnicalDocumentAccess(
                context, JPO.packArgs(accessMap));
        if (!hasAccess) {
            throw new Exception(EnoviaResourceBundle.getProperty(context, SUITE_KEY,
                    context.getLocale(), "emxComponents.Snapshot.EditAccessDenied"));
        }

        String projectId = JF_PublicProjectQuery_mxJPO.getProjectIdByTaskId(context, taskId);
        DomainObject task = DomainObject.newInstance(context, taskId);
        boolean transactionStarted = false;
        try {
            ContextUtil.startTransaction(context, true);
            transactionStarted = true;
            StringList existingDeliverableIds = task.getInfoList(context,
                    "from[Task Deliverable].to.id");
            StringList connectSnapshotIds = new StringList();
            Set<String> selectedSnapshotIds = new HashSet<>();
            if (snapshotIds != null) {
                selectedSnapshotIds.addAll(Arrays.asList(snapshotIds));
            }
            String snapshotProjectSelect = "to[" + rel_JFProject2Snapshot + "].from.id";
            for (String snapshotId : selectedSnapshotIds) {
                if (UIUtil.isNullOrEmpty(snapshotId) || existingDeliverableIds.contains(snapshotId)) {
                    continue;
                }
                DomainObject snapshot = DomainObject.newInstance(context, snapshotId);
                Map snapshotInfo = snapshot.getInfo(context, StringList.create(
                        DomainConstants.SELECT_TYPE, snapshotProjectSelect));
                if (!TYPE_JFSnapshot.equals(UIUtil.getValue(snapshotInfo, DomainConstants.SELECT_TYPE))
                        || !projectId.equals(UIUtil.getValue(snapshotInfo, snapshotProjectSelect))) {
                    throw new Exception(EnoviaResourceBundle.getProperty(context,
                            "emxProgramCentralStringResource", context.getLocale(),
                            "emxProgramCentral.TaskDeliverable.InvalidSnapshot"));
                }
                connectSnapshotIds.add(snapshotId);
            }
            if (!connectSnapshotIds.isEmpty()) {
                DomainRelationship.connect(context, task, "Task Deliverable", true,
                        connectSnapshotIds.toStringArray());
            }
            ContextUtil.commitTransaction(context);
            transactionStarted = false;
            Map result = new HashMap();
            result.put("code", "200");
            return result;
        } catch (Exception e) {
            if (transactionStarted) {
                ContextUtil.abortTransaction(context);
            }
            throw e;
        }
    }

    /**
     * 校验定点报价快照关联零件是否全部发布
     **
     * @param context
     * @param snapshotId 快照ID
     * @return void
     * @throws Exception 存在非发布状态零件时抛出国际化提示
     * @author caipan by codex
     * @date 2026/8/20 15:39
     */
    private void validateFinalQuotationPartsReleased(Context context, String snapshotId) throws Exception {
        StringList partCurrentList = DomainObject.newInstance(context, snapshotId)
                .getInfoList(context, "from[" + rel_JFSnapshot2VPMReference + "].to.current");
        if (partCurrentList == null) {
            return;
        }
        for (Object partCurrentObject : partCurrentList) {
            String partCurrent = partCurrentObject == null ? EMPTY_STRING : partCurrentObject.toString();
            if (!"RELEASED".equals(partCurrent)) {
                throw new Exception(EnoviaResourceBundle.getProperty(context, SUITE_KEY,
                        context.getLocale(), "emxComponents.Snapshot.FinalQuotationPartsMustBeReleased"));
            }
        }
    }

    /**
     * 快照从草稿提升到审批中时校验定点报价关联零件发布状态
     **
     * @param context
     * @param args Trigger参数，args[0]为快照ID
     * @return int 校验失败返回1，校验通过返回0
     * @throws Exception
     * @author caipan by codex
     * @date 2026/8/20 15:39
     */
    @ProgramCallable
    public int checkJFSnapshotFinalQuotationPartsReleased(Context context, String[] args) throws Exception {
        String snapshotId = args[0];
        DomainObject snapshot = DomainObject.newInstance(context, snapshotId);
        Map snapshotInfo = snapshot.getInfo(context, new StringList(new String[]{
                "attribute[" + ATTR_JF_SNAPSHOT_SUB_TYPE + "]",
                "attribute[" + ATTR_FINAL_QUOTATION_VERSION + "]"
        }));
        boolean isFinalQuotationSnapshot = "Quote".equals(UIUtil.getValue(snapshotInfo,
                "attribute[" + ATTR_JF_SNAPSHOT_SUB_TYPE + "]"))
                && "Y".equals(UIUtil.getValue(snapshotInfo,
                "attribute[" + ATTR_FINAL_QUOTATION_VERSION + "]"));
        if (!isFinalQuotationSnapshot) {
            return 0;
        }
        try {
            validateFinalQuotationPartsReleased(context, snapshotId);
            return 0;
        } catch (Exception e) {
            JF_LOGGER.error("快照定点报价关联零件发布状态校验失败，snapshotId:{}", snapshotId, e);
            emxContextUtil_mxJPO.mqlNotice(context, e.getMessage());
            return 1;
        }
    }

    /**
     * 快照冻结后异步同步关联零件清单。
     **
     * @param context 上下文
     * @param args Trigger参数，args[0]为快照ID
     * @return void
     * @throws Exception 异常
     * @author caipan by codex
     * @date 2026/8/27
     */
    public void syncSnapshotPartListInFrozen(Context context, String[] args) throws Exception {
        if (args == null || args.length == 0 || UIUtil.isNullOrEmpty(args[0])) {
            JF_LOGGER.error("快照冻结后同步零件清单失败：未传入快照ID");
            return;
        }
        JF_Util_mxJPO.runAsyncWithJsonResult(context, new String[]{args[0]}, "JF_BIInterface",
                "syncSnapshotPartList");
    }


}
