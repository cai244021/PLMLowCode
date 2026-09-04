
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.dscn.plm.util.NioJDUtils;
import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.DomainRelationship;
import com.matrixone.apps.domain.Job;
import com.matrixone.apps.domain.util.*;
import com.matrixone.apps.framework.ui.UICache;
import com.matrixone.apps.framework.ui.UIUtil;
import com.matrixone.apps.program.ProgramCentralConstants;
import com.matrixone.apps.program.ProgramCentralUtil;
import com.nomagic.esi.common.a.O;
import jakarta.mail.BodyPart;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMultipart;
import matrix.db.*;
import matrix.util.StringList;
import org.apache.commons.collections4.CollectionUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.MathContext;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.matrixone.apps.domain.DomainConstants.*;

public class JF_MBOM_mxJPO implements JF_PLMConstants_mxJPO{
    private static final Logger ThreadLog = LoggerFactory.getLogger("MY_CUSTOM_LOGGER");
    private static final Logger JF_LOGGER = LoggerFactory.getLogger(JF_MBOM_mxJPO.class);
    /** MBOM批量更新专用线程池，避免占用平台共享异步线程池。 */
    //20260807 update by caipan 将MBOM并发控制为8，降低大BOM展开时的单进程内存峰值
    private static final ThreadPoolExecutor MBOM_EXECUTOR = new ThreadPoolExecutor(
            8,
            8,
            60L,
            TimeUnit.SECONDS,
            new ArrayBlockingQueue<>(100),
            new ThreadPoolExecutor.CallerRunsPolicy());
    private static final DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("M/d/yyyy h:mm:ss a", Locale.US);
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
    private static final DateTimeFormatter formatterDate = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.ENGLISH);
    private static final DateTimeFormatter formatterTime = DateTimeFormatter.ofPattern("HH:mm:ss", Locale.ENGLISH);
    private static final SimpleDateFormat sdf = new SimpleDateFormat("M/d/yyyy h:mm:ss a");
    //20260819 update by ljr 面套BOM转换使用独立配置缓存和属性常量，避免影响原公共转换逻辑；
    private static final String MBOM_TYPE_CONTROL_CONFIG = "MBOM_Type_Control";
    private static final String CONFIG_KEY_DETAIL_TYPE = "Detail_Type";
    private static final String CONFIG_KEY_DETAIL_TYPE_T06 = "Detail_Type_T06";
    private static final String CONFIG_KEY_DETAIL_OTHER = "Detail_Other";
    private static final String CONFIG_KEY_DETAIL_TYPE_MAKE = "Detail_Type_Make";
    private static final String CONFIG_KEY_DETAIL_TYPE_UNIT_T06_Y = "Detail_Type_Unit_T06_Y";
    private static final String CONFIG_KEY_DETAIL_TYPE_UNIT_T06_N = "Detail_Type_Unit_T06_N";
    private static final String CONFIG_KEY_DETAIL_TYPE_UNIT_OTHER = "Detail_Type_Unit_Other";
    private static final String CONFIG_KEY_DETAIL_OTHER_UNIT = "Detail_Other_Unit";
    private static final String CONFIG_KEY_DETAIL_TYPE_USAGE_T06 = "Detail_Type_Usage_T06";
    private static final String CONFIG_KEY_DETAIL_TYPE_USAGE_OTHER = "Detail_Type_Usage_Other";
    private static final String CONFIG_KEY_DETAIL_OTHER_USAGE = "Detail_Other_Usage";
    private static final String CONFIG_KEY_ROLL_DETAIL_TYPE = "Roll_Detail_Type";
    private static final String DEFAULT_DETAIL_TYPE_USAGE_T06 =
            JF_PLMConstants_mxJPO.SELECT_ATTR_NET_AREA
                    + "/attribute[" + JF_PLMConstants_mxJPO.ATTR_JF_MBOM_WIDTH + "]"
                    + "/attribute[" + JF_PLMConstants_mxJPO.ATTR_JF_MBOM_UTILIZATION_RATE + "]";
    private static final String DEFAULT_DETAIL_TYPE_USAGE_OTHER =
            JF_PLMConstants_mxJPO.SELECT_ATTR_JFLength + "/1000";
    private static final String DEFAULT_DETAIL_OTHER_USAGE =
            JF_PLMConstants_mxJPO.SELECT_ATTR_JF_Dosage + "/1000";
    private static final String TRIM_ROLL_REVISION = "AA.1-000";
    private static final String MBOM_DISPLAY_PART_NUMBER = "MBOMDisplayPartNumber";
    private static final String MBOM_DISPLAY_REVISION = "MBOMDisplayRevision";
    private static final String TRIM_RESULT_DOSAGE = "dosage";
    private static final List<String> TRIM_DEFAULT_ATTRIBUTE_LIST = Collections.unmodifiableList(
            Arrays.asList(JF_PLMConstants_mxJPO.ATTR_JF_MBOM_WIDTH,
                    JF_PLMConstants_mxJPO.ATTR_JF_MBOM_UTILIZATION_RATE));
    private static final StringList TRIM_PART_SELECT_LIST;
    private static final StringList TRIM_ROLL_SELECT_LIST;
    private static final Map<String, String> TRIM_ATTRIBUTE_MAPPING;
    private static final Map<String, Map<String, String>> TRIM_ROLL_PART_CACHE = new ConcurrentHashMap<>();
    private static final Map<String, Object> TRIM_ROLL_PART_CACHE_LOCKS = new ConcurrentHashMap<>();
    private static volatile Map<String, String> MBOM_TYPE_CONTROL;
    private static final Object MBOM_TYPE_CONTROL_LOCK = new Object();
    //    public static final ConcurrentHashMap<String, String> MBOM_VERSION_CACHE = new ConcurrentHashMap<>();
    private static final StringList partAttribute= JF_Util_mxJPO.basicBolistSel();
    static {
        partAttribute.add("attribute[EnterpriseExtension.V_PartNumber]");
        partAttribute.add("attribute[JF_VPMReference.JF_PartNameEN]");
        partAttribute.add("attribute[JF_VPMReference.JF_PartNameCN]");
        partAttribute.add("attribute[PLMEntity.V_Name]");
        partAttribute.add("attribute[JF_VPMReference.JF_PartType]");
        partAttribute.add("attribute[JF_VPMReference.JF_ProcurementType]");
        partAttribute.add("attribute[JF_VPMReference.JF_Unit]");
        partAttribute.add("attribute[JF_VPMReference.JF_PartDes]");
        partAttribute.add("attribute[JF_VPMReference.JF_PartENDes]");

        //20260819 update by ljr 面套查询属性和映射关系静态初始化一次，避免每个节点重复构建。
        TRIM_PART_SELECT_LIST = JF_Util_mxJPO.basicBolistSel();
        TRIM_PART_SELECT_LIST.add(JF_PLMConstants_mxJPO.SELECT_ATTR_V_PART_NUMBER);
        TRIM_PART_SELECT_LIST.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JF_PartNameEN);
        TRIM_PART_SELECT_LIST.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JF_PartNameCN);
        TRIM_PART_SELECT_LIST.add(JF_PLMConstants_mxJPO.SELECT_ATTR_PLMEntity_V_Name);
        TRIM_PART_SELECT_LIST.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JFPartType);
        TRIM_PART_SELECT_LIST.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JF_ProcurementType);
        TRIM_PART_SELECT_LIST.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JF_VPMReferenceJF_Unit);
        TRIM_PART_SELECT_LIST.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JF_PartDes);
        TRIM_PART_SELECT_LIST.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JF_PartENDes);
        TRIM_PART_SELECT_LIST.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JF_Detail_CN);
        TRIM_PART_SELECT_LIST.add(JF_PLMConstants_mxJPO.SELECT_ATTR_RollPartNumber);
        TRIM_PART_SELECT_LIST.add(JF_PLMConstants_mxJPO.SELECT_ATTR_NET_AREA);
        TRIM_PART_SELECT_LIST.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JFLength);

        TRIM_ROLL_SELECT_LIST = new StringList();
        TRIM_ROLL_SELECT_LIST.add(JF_PLMConstants_mxJPO.SELECT_ATTR_V_PART_NUMBER);
        TRIM_ROLL_SELECT_LIST.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JF_PartNameCN);
        TRIM_ROLL_SELECT_LIST.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JF_PartNameEN);
        TRIM_ROLL_SELECT_LIST.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JF_FABRIC_MATERIAL_TYPE);
        TRIM_ROLL_SELECT_LIST.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JF_Detail_CN);

        Map<String, String> trimAttributeMapping = new HashMap<>();
        trimAttributeMapping.put(JF_PLMConstants_mxJPO.SELECT_ATTR_V_PART_NUMBER,
                JF_PLMConstants_mxJPO.ATTR_JF_MBOM_PART_NUMBER);
        trimAttributeMapping.put(JF_PLMConstants_mxJPO.SELECT_ATTR_JF_PartNameEN,
                JF_PLMConstants_mxJPO.ATTR_JF_MBOM_PART_NAME_EN);
        trimAttributeMapping.put(JF_PLMConstants_mxJPO.SELECT_ATTR_JF_PartNameCN,
                JF_PLMConstants_mxJPO.ATTR_JF_MBOM_PART_NAME_CN);
        trimAttributeMapping.put(JF_PLMConstants_mxJPO.SELECT_ATTR_PLMEntity_V_Name,
                DomainConstants.ATTRIBUTE_TITLE);
        trimAttributeMapping.put(JF_PLMConstants_mxJPO.SELECT_ATTR_JFPartType,
                JF_PLMConstants_mxJPO.ATTR_JF_MBOM_PART_TYPE);
        trimAttributeMapping.put(JF_PLMConstants_mxJPO.SELECT_ATTR_JF_ProcurementType,
                JF_PLMConstants_mxJPO.ATTR_JF_MBOM_PROCUREMENT_TYPE);
        trimAttributeMapping.put(JF_PLMConstants_mxJPO.Attr_JF_DirectBuy,
                JF_PLMConstants_mxJPO.Attr_JF_DirectBuy);
        trimAttributeMapping.put(JF_PLMConstants_mxJPO.SELECT_ATTR_JF_VPMReferenceJF_Unit,
                JF_PLMConstants_mxJPO.ATTR_JF_MBOM_UNIT);
        trimAttributeMapping.put(JF_PLMConstants_mxJPO.SELECT_ATTR_JF_PartDes,
                JF_PLMConstants_mxJPO.ATTR_JF_MBOM_PART_DES);
        trimAttributeMapping.put(JF_PLMConstants_mxJPO.SELECT_ATTR_JF_PartENDes,
                JF_PLMConstants_mxJPO.ATTR_JF_MBOM_PART_EN_DES);
        TRIM_ATTRIBUTE_MAPPING = Collections.unmodifiableMap(trimAttributeMapping);
    }

    /*
     * @description:获取MBOM清单
     * @author: caipan
     * @date: 2025/7/2 14:08:43
     * @param: * @param[1] context
     * @param[2] args
     * @return:
     **/
    public MapList getMBOMList(Context context, String[] args) throws Exception{
        try {
            StringList selList = JF_Util_mxJPO.basicBolistSel();
            //20260727 update by ljr MBOM Table按源零件关系显示DirectBuy，预取内部零件号减少逐行查询；
            selList.add("attribute[JF_PartNumber]");
            selList.add("attribute[" + JF_PLMConstants_mxJPO.ATTR_JF_ROLL_PART_NUMBER + "]");
            StringList relList = JF_Util_mxJPO.basicRellistSel();
            Map paramsMap = (Map) JPO.unpackArgs(args);
            ContextUtil.pushContext(context);
            String strObjectId = (String) paramsMap.get(STRING_OBJECTID);
            JF_LOGGER.info("strObjectId:{}",strObjectId);
            DomainObject objectProject = DomainObject.newInstance(context, strObjectId);
            MapList mbomList = objectProject.getRelatedObjects(context,
                    JF_PLMConstants_mxJPO.RELATIONSHIP_JF_relProject2MBOM, //pattern to match relationships
                    JF_PLMConstants_mxJPO.TYPE_JF_ManufacturedItem, //pattern to match types
                    selList, //the eMatrix StringList object that holds the list of select statement pertaining to Business Obejcts.
                    relList, //the eMatrix StringList object that holds the list of select statement pertaining to Relationships.
                    false, //get To relationships
                    true, //get From relationships
                    (short) 1, //the number of levels to expand, 0 equals expand all.
                    DomainConstants.EMPTY_STRING, //where clause to apply to objects, can be empty ""
                    null, //where clause to apply to relationship, can be empty ""
                    (short) 0); //limit
            setMBOMDisplayPartIdentity(context, mbomList);
            return mbomList;
        }catch (Exception e) {
            e.printStackTrace();
        }finally {
            ContextUtil.popContext(context);
        }
        return new MapList();
    }
	
	
    /*
     * @description:展开MBOM
     * @author: caipan
     * @date: 2025/7/4 10:14:57
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
            //20260727 update by ljr MBOM展开行预取内部零件号，供DirectBuy列反查源零件关系；
            boSel.add("attribute[JF_PartNumber]");
            boSel.add("attribute[" + JF_PLMConstants_mxJPO.ATTR_JF_ROLL_PART_NUMBER + "]");
            StringList relSel = new StringList();
            relSel.add(DomainRelationship.SELECT_ID);
            childPartList =  obj.getRelatedObjects(context,JF_PLMConstants_mxJPO.REL_JF_relManufacturedItem,JF_PLMConstants_mxJPO.TYPE_JF_ManufacturedItem,
                    boSel,relSel,false,true,Short.parseShort(expandLevel),"","",0);
            setMBOMDisplayPartIdentity(context, childPartList);
//            for(int i=0;i<childPartList.size();i++){
//                Map mangerMap = (Map) childPartList.get(i);
//                mangerMap.put("disableSelection","true");
//            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return childPartList;
    }

    /**
     * 计算MBOM页面零件号和版本显示值。样板包、松紧带、嵌条存在固定版本卷料零件时，
     * 页面显示卷料号和AA.1-000；其他情况显示MBOM自身零件号和版本。
     */
    private void setMBOMDisplayPartIdentity(Context context, MapList mbomList) throws Exception {
        if (CollectionUtils.isEmpty(mbomList)) {
            return;
        }
        Map<String, String> detailCategoryCache = new HashMap<>();
        Map<String, Map<String, String>> rollPartCache = new HashMap<>();
        for (Object item : mbomList) {
            Map mbomInfo = (Map) item;
            String partNumber = UIUtil.getValue(mbomInfo, "attribute[JF_PartNumber]");
            String revision = UIUtil.getValue(mbomInfo, SELECT_REVISION);
            mbomInfo.put(MBOM_DISPLAY_PART_NUMBER, partNumber);
            mbomInfo.put(MBOM_DISPLAY_REVISION, revision);
            try {
                String rollPartNumber = UIUtil.getValue(
                        mbomInfo, "attribute[" + JF_PLMConstants_mxJPO.ATTR_JF_ROLL_PART_NUMBER + "]").trim();
                if (UIUtil.isNullOrEmpty(rollPartNumber)) {
                    continue;
                }
                String sourcePartKey = partNumber + "|" + revision;
                String detailCategory = detailCategoryCache.get(sourcePartKey);
                if (detailCategory == null) {
                    detailCategory = getMBOMSourceDetailCategory(context, partNumber, revision);
                    detailCategoryCache.put(sourcePartKey, detailCategory);
                }
                if (!isConfiguredTrimDetail(context, CONFIG_KEY_DETAIL_TYPE, detailCategory)) {
                    continue;
                }

                String rollPartKey = rollPartNumber + "|" + TRIM_ROLL_REVISION;
                Map<String, String> rollPartInfo = rollPartCache.get(rollPartKey);
                if (rollPartInfo == null) {
                    rollPartInfo = findTrimRollPart(context, rollPartNumber);
                    rollPartCache.put(rollPartKey, rollPartInfo);
                }
                if (!rollPartInfo.isEmpty()) {
                    mbomInfo.put(MBOM_DISPLAY_PART_NUMBER, UIUtil.getValue(
                            rollPartInfo, JF_PLMConstants_mxJPO.SELECT_ATTR_V_PART_NUMBER));
                    mbomInfo.put(MBOM_DISPLAY_REVISION, TRIM_ROLL_REVISION);
                }
            } catch (Exception e) {
                //显示转换失败时保留MBOM自身值，不能影响整棵MBOM结构加载。
                JF_LOGGER.warn("MBOM页面卷料零件显示转换失败，partNumber:{}, revision:{}",
                        partNumber, revision, e);
            }
        }
    }

    /**
     * 按MBOM自身零件号和版本读取源零件详细分类。
     */
    private String getMBOMSourceDetailCategory(Context context, String partNumber,
                                                String revision) throws Exception {
        if (UIUtil.isNullOrEmpty(partNumber) || UIUtil.isNullOrEmpty(revision)) {
            return EMPTY_STRING;
        }
        String escapedPartNumber = partNumber.replace("\\", "\\\\").replace("'", "\\'");
        String escapedRevision = revision.replace("\\", "\\\\").replace("'", "\\'");
        String where = JF_PLMConstants_mxJPO.SELECT_ATTR_V_PART_NUMBER + "=='" + escapedPartNumber
                + "' && revision=='" + escapedRevision + "'";
        MapList sourcePartList = DomainObject.findObjects(context,
                JF_PLMConstants_mxJPO.TYPE_VPMReference, "*", where,
                StringList.create(JF_PLMConstants_mxJPO.SELECT_ATTR_JF_Detail_CN));
        if (sourcePartList.isEmpty()) {
            return EMPTY_STRING;
        }
        return UIUtil.getValue((Map) sourcePartList.get(0),
                JF_PLMConstants_mxJPO.SELECT_ATTR_JF_Detail_CN);
    }

    /**
     * MBOM页面零件号、版本程序列统一取值入口。
     */
    public Vector getMBOMDisplayPartIdentity(Context context, String[] args) throws Exception {
        Map programMap = JPO.unpackArgs(args);
        Map columnMap = (Map) programMap.get("columnMap");
        String columnName = UIUtil.getValue(columnMap, "name");
        boolean revisionColumn = "revision".equals(columnName);
        MapList objectList = (MapList) programMap.get("objectList");
        Vector result = new Vector();
        if (objectList == null) {
            return result;
        }
        for (Object item : objectList) {
            Map objectMap = (Map) item;
            if (revisionColumn) {
                String displayRevision = UIUtil.getValue(objectMap, MBOM_DISPLAY_REVISION);
                result.add(UIUtil.isNullOrEmpty(displayRevision)
                        ? UIUtil.getValue(objectMap, SELECT_REVISION) : displayRevision);
            } else {
                String displayPartNumber = UIUtil.getValue(objectMap, MBOM_DISPLAY_PART_NUMBER);
                result.add(UIUtil.isNullOrEmpty(displayPartNumber)
                        ? UIUtil.getValue(objectMap, "attribute[JF_PartNumber]") : displayPartNumber);
            }
        }
        return result;
    }


    /**
     * 建立MBOM实例关系并设置用量
     * @param context
     * @param parentId MBOM父对象ID
     * @param objectId MBOM子对象ID
     * @param usage 用量
     * @return java.lang.String 关系物理ID
     * @throws Exception
     * @author caipan
     * @date 2026/7/24 16:28
     */
    public String connectionMBOM(Context context,String parentId,String objectId,String usage) throws Exception{
        String id = EMPTY_STRING;
        try {
            ContextUtil.pushContext(context);
            RelationshipType relType  = new RelationshipType(REL_JF_relManufacturedItem);
            DomainObject parentObj = DomainObject.newInstance(context,parentId);
            DomainRelationship ship = parentObj.addToObject(context,relType,objectId);
            ship.setAttributeValue(context, "JF_Dosage", usage);
            ship.setAttributeValue(context, "JF_OriginateFlag",  "EBOM");
            id = ship.getPhysicalId(context);
        }catch (Exception e) {
            e.printStackTrace();
            //20260724 update by ljr MBOM关系创建失败必须向上抛出，由上层事务统一回滚；
            throw e;
        }finally {
            ContextUtil.popContext(context);
        }

        return id;
    }

    /**
     * 建立MBOM实例关系并且设置Usage和JF_OriginateFlag
     * 场景： 添加辅料功能 会同步辅料  以及更新制造件的时候会用到以及回滚MBOM  CreateMbomPartAndConnect, cloneMbomPart,connMbomStrucure
     * @param context
     * @param parentId
     * @param objectId
     * @param usage
     * @param strJFOriginateFlag
     * @author LIUJR
     * @throws
     * @return java.lang.String
     * @date 2025/12/23 15:27
     * @description
     */
    public String connectionMBOMSetFlag(Context context,String parentId,String objectId,String usage, String strJFOriginateFlag) throws Exception{
        RelationshipType relType  = new RelationshipType(REL_JF_relManufacturedItem);
        DomainObject parentObj = DomainObject.newInstance(context,parentId);
        DomainRelationship ship = parentObj.addToObject(context,relType,objectId);
        ship.setAttributeValue(context, "JF_Dosage", usage);
        if (UIUtil.isNullOrEmpty(strJFOriginateFlag)) {
            strJFOriginateFlag = "EBOM";
        }
        ship.setAttributeValue(context, "JF_OriginateFlag", strJFOriginateFlag);
        return ship.getPhysicalId(context);
    }

    /**
     * 每一层需要判断是否有面套或者发泡  初始化
     * @param context
     * @param mbomId
     * @param id
     * @param selList
     * @param relList
     * @param owner
     * @param owner
     * @param projectId 项目ID
     * @author LIUJR
     * @throws
     * @return com.matrixone.apps.domain.util.MapList
     * @date 09/07/2025 10:34
     * @description
     */
    public MapList expandAllAME(Context context,String mbomId,String id,StringList selList,StringList relList,String owner, HashMap<String, String> ownerMap, HashSet<String> emailSet,String projectId) throws Exception{
        DomainObject obj = DomainObject.newInstance(context,id);
        MapList subList = obj.getRelatedObjects(context,
                JF_PLMConstants_mxJPO.REL_Instance, //pattern to match relationships
                JF_PLMConstants_mxJPO.TYPE_VPMReference, //pattern to match types
                selList, //the eMatrix StringList object that holds the list of select statement pertaining to Business Obejcts.
                relList, //the eMatrix StringList object that holds the list of select statement pertaining to Relationships.
                false, //get To relationships
                true, //get From relationships
                (short) 1, //the number of levels to expand, 0 equals expand all.
                DomainConstants.EMPTY_STRING, //where clause to apply to objects, can be empty ""
                null, //where clause to apply to relationship, can be empty ""
                (short) 0); //limit
        for(int i=0;i<subList.size();i++){
            Map temp = (Map)subList.get(i);
            String subId = UIUtil.getValue(temp, DomainConstants.SELECT_ID);
            //生成MBOM的时候仅CAD件及其子级不生成。判断一个件是否仅CAD件需要判断关系属性：VPMInstance 关系属性 SynchroEBOMExt.V_InEBOMApplicative 为TRUE就是仅CAD件
            String cad = UIUtil.getValue(temp, JF_PLMConstants_mxJPO.SELECT_Attr_SynchroEBOMCAD);
            if ("FALSE".equalsIgnoreCase(cad)) {
                continue;
            }
            String strOwner = owner;
            if (owner.equalsIgnoreCase(ownerMap.get("A"))) {
                //如果是总装 需要判断是否是U 或者 T
                DomainObject supplyObj = DomainObject.newInstance(context, subId);
                String ownerPartType = supplyObj.getInfo(context, JF_PLMConstants_mxJPO.SELECT_ATTR_JFPartType);
                if(ownerPartType.equalsIgnoreCase("U")){
                    strOwner = ownerMap.get("U");
                }else if(ownerPartType.equalsIgnoreCase("T")){
                    strOwner = ownerMap.get("T");
                }
            }
            String usage = UIUtil.getValue(temp, JF_PLMConstants_mxJPO.SELECT_ATTR_JF_Dosage);
            String partType = UIUtil.getValue(temp, JF_PLMConstants_mxJPO.SELECT_ATTR_JFPartType);
            //20260819 update by ljr 初始化节点按零件类型路由面套专用创建方法，其他类型仍执行原公共逻辑；
            Map<String, String> createResult = createMBOMPartByType(
                    context, subId, partType, strOwner, projectId, usage);
            String suMbomId = UIUtil.getValue(createResult, SELECT_ID);
            usage = UIUtil.getValue(createResult, TRIM_RESULT_DOSAGE);
            emailSet.add(strOwner);
            connectionMBOM(context, mbomId, suMbomId, usage);
            expandAllAME(context, suMbomId,subId, selList, relList,strOwner, ownerMap, emailSet,projectId);
        }
        return subList;
    }

    /**
     * 每一层需要判断是否有面套或者发泡  ECR更新MBOM
     * @param context
     * @param mbomId
     * @param id
     * @param selList
     * @param relList
     * @param owner
     * @param owner
     * @param projectId 项目ID
     * @author LIUJR
     * @throws
     * @return com.matrixone.apps.domain.util.MapList
     * @date 09/07/2025 10:34
     * @description
     */
    public MapList expandECRAllAME(Context context,String ecrId, String mbomId,String id,StringList selList,StringList relList,String owner, HashMap<String, String> ownerMap, HashSet<String> emailSet, Map<String, String> partCadMap,String projectId) throws Exception{
        DomainObject obj = DomainObject.newInstance(context,id);
        MapList subList = obj.getRelatedObjects(context,
                JF_PLMConstants_mxJPO.REL_JFECRRoot2Item+ "," + JF_PLMConstants_mxJPO.REL_JFECRRoot2ItemBubble, //pattern to match relationships
                JF_PLMConstants_mxJPO.TYPE_VPMReference, //pattern to match types
                selList, //the eMatrix StringList object that holds the list of select statement pertaining to Business Obejcts.
                relList, //the eMatrix StringList object that holds the list of select statement pertaining to Relationships.
                false, //get To relationships
                true, //get From relationships
                (short) 1, //the number of levels to expand, 0 equals expand all.
                DomainConstants.EMPTY_STRING, //where clause to apply to objects, can be empty ""
                null, //where clause to apply to relationship, can be empty ""
                (short) 0); //limit
        Map mapListGroupingMap = getPartEbomStructure(context, obj, relList, selList, 1);
//        JF_LOGGER.info("subList:{}", subList);
        String fromId = EMPTY_STRING;
        for(int i=0;i<subList.size();i++){
            Map temp = (Map)subList.get(i);
            //生成MBOM的时候仅CAD件及其子级不生成。判断一个件是否仅CAD件需要判断关系属性：VPMInstance 关系属性 SynchroEBOMExt.V_InEBOMApplicative 为TRUE就是仅CAD件
            String parentId = UIUtil.getValue(temp, SELECT_FROM_ID);
            String subId = UIUtil.getValue(temp, DomainConstants.SELECT_ID);
            if (partCadMap.containsKey(subId)) {
                //仅cad件  不生成
                fromId = partCadMap.get(subId);
                if (parentId.equalsIgnoreCase(fromId)) {
                    continue;
                }
            }
            String jfEcrId = UIUtil.getValue(temp, SELECT_ATTRIBUTE_JF_ECRID);
            String count = UIUtil.getValue(temp, SELECT_ATTRIBUTE_JF_BOMQuantity);
//            JF_LOGGER.info("count:{}", count);
            Integer integer = Integer.valueOf(count);
//            JF_LOGGER.info("integer:{}", integer);
            if (!jfEcrId.contains(ecrId)) {
                continue;
            }
            Map ebomMap = new HashMap();
            if (mapListGroupingMap.containsKey(subId)) {
                List list = (List) mapListGroupingMap.get(subId);
                ebomMap = (Map) list.get(0);
            }
            String strOwner = owner;
            String usage = UIUtil.getValue(ebomMap, JF_PLMConstants_mxJPO.SELECT_ATTR_JF_Dosage);
            String partType = UIUtil.getValue(temp, JF_PLMConstants_mxJPO.SELECT_ATTR_JFPartType);
            if (owner.equalsIgnoreCase(ownerMap.get("A"))) {
                //如果是总装 需要判断是否是U 或者 T
                DomainObject supplyObj = DomainObject.newInstance(context, subId);
                String ownerPartType = supplyObj.getInfo(context, JF_PLMConstants_mxJPO.SELECT_ATTR_JFPartType);
                if(ownerPartType.equalsIgnoreCase("U")){
                    strOwner = ownerMap.get("U");
                }else if(ownerPartType.equalsIgnoreCase("T")){
                    strOwner = ownerMap.get("T");
                }
            }
            for (Integer integer1 = 0; integer1 < integer; integer1++) {
                //20260819 update by ljr 更新场景新增节点按零件类型路由面套专用创建方法；
                Map<String, String> createResult = createMBOMPartByType(
                        context, subId, partType, strOwner, projectId, usage);
                String suMbomId = UIUtil.getValue(createResult, SELECT_ID);
                String mbomUsage = UIUtil.getValue(createResult, TRIM_RESULT_DOSAGE);
                emailSet.add(strOwner);
                connectionMBOM(context, mbomId, suMbomId, mbomUsage);
                expandECRAllAME(context, ecrId, suMbomId,subId, selList, relList,strOwner, ownerMap, emailSet, partCadMap,projectId);
            }
        }
        return subList;
    }


    /*
     * @description:获取第一层结构是面套、发泡、总装的节点
     * @author: caipan
     * @date: 2025/7/3 14:02:35
     * @param: * @param[1] context
     * @param[2] id
     * @param[3] selList
     * @param[4] relList
     * @param[5] map
     * @return:
     **/
    public void gcExpand(Context context,String id,StringList selList,StringList relList,Map<String,StringList> map )throws Exception{
        DomainObject gcObj = DomainObject.newInstance(context, id);
        MapList subList = gcObj.getRelatedObjects(context,
                JF_PLMConstants_mxJPO.REL_Instance, //pattern to match relationships
                JF_PLMConstants_mxJPO.TYPE_VPMReference, //pattern to match types
                selList, //the eMatrix StringList object that holds the list of select statement pertaining to Business Obejcts.
                relList, //the eMatrix StringList object that holds the list of select statement pertaining to Relationships.
                false, //get To relationships
                true, //get From relationships
                (short) 1, //the number of levels to expand, 0 equals expand all.
                DomainConstants.EMPTY_STRING, //where clause to apply to objects, can be empty ""
                null, //where clause to apply to relationship, can be empty ""
                (short) 0); //limit
//        JF_LOGGER.info("subList:{}",subList);
        StringList tList = new StringList();
        StringList uList = new StringList();
        StringList oList = new StringList();
        for(int i=0;i<subList.size();i++){
            Map temp = (Map)subList.get(i);
            String type = UIUtil.getValue(temp, JF_PLMConstants_mxJPO.SELECT_ATTR_JFPartType);
            //生成MBOM的时候仅CAD件及其子级不生成。判断一个件是否仅CAD件需要判断关系属性：VPMInstance 关系属性 SynchroEBOMExt.V_InEBOMApplicative 为TRUE就是仅CAD件
            String cad = UIUtil.getValue(temp, JF_PLMConstants_mxJPO.SELECT_Attr_SynchroEBOMCAD);
            if ("FALSE".equalsIgnoreCase(cad)) {
                continue;
            }
            JF_LOGGER.info("type:{}",type);
            String subId =  UIUtil.getValue(temp, DomainConstants.SELECT_ID);
            String dosage =  UIUtil.getValue(temp, JF_PLMConstants_mxJPO.SELECT_ATTR_JF_Dosage);
            if(type.equals("U")){//发泡
                if(map.containsKey("U")){
                    uList =map.get("U");
                }
                uList.add(subId+"@"+dosage);
                map.put("U", uList);
            }else if(type.equals("T")){//面套
                if(map.containsKey("T")){
                    tList =map.get("T");
                }
                tList.add(subId+"@"+dosage);
                map.put("T", tList);
            }else{
                if(map.containsKey("O")){
                    oList =map.get("O");
                }
                oList.add(subId+"@"+dosage);
                map.put("O", oList);
            }

        }
    }


    /*
     * @description:创建MBOM根节点，并且和项目建立关系
     * @author: caipan
     * @date: 2025/7/3 11:22:49
     * @param: * @param[1] context
     * @param[2] project 项目对象
     * @return:
     **/
    public String createMBOMGX(Context context,DomainObject project,String owner) throws Exception{
        String mbomId = FrameworkUtil.autoName(context,  "type_JF_ManufacturedItem", "policy_JF_ManufacturedItem");
        DomainObject root = DomainObject.newInstance(context, mbomId);
        Map map = new HashMap();
        map.put("JF_PartNumber",project.getName(context));//设置GX命名
        map.put("Title",project.getDescription(context));
        map.put("JF_PartType","X");
        //初始化  设置节点的责任方为PM
        map.put("JF_Responsible","PM");
        root.setAttributeValues(context, map);
        //owner 设置给项目经理
        JF_Util_mxJPO.changeowner(context, mbomId, project.getObjectId(context),owner);
        RelationshipType relType  = new RelationshipType(RELATIONSHIP_JF_relProject2MBOM);
        project.addToObject(context,relType,mbomId);
        return mbomId;
    }

    /*
     * @description:EBOM和MBOM的key值对应
     * @author: caipan
     * @date: 2025/7/3 09:26:19
     * @param: * @param[1] context
     * @param[2] map
     * @return:
     **/
    public Map changeAttribute(Context context,Map<String,String> map){
        Map<String,String> mbomMap = new HashMap();
        mbomMap.put("attribute[EnterpriseExtension.V_PartNumber]","JF_PartNumber");
        mbomMap.put("attribute[JF_VPMReference.JF_PartNameEN]","JF_PartNameEN");
        mbomMap.put("attribute[JF_VPMReference.JF_PartNameCN]","JF_PartNameCN");
        mbomMap.put("attribute[PLMEntity.V_Name]","Title");
        mbomMap.put("attribute[JF_VPMReference.JF_PartType]","JF_PartType");
        mbomMap.put("attribute[JF_VPMReference.JF_ProcurementType]","JF_ProcurementType");
        mbomMap.put("JF_DirectBuy","JF_DirectBuy");
        mbomMap.put("attribute[JF_VPMReference.JF_Unit]","JF_Unit");
        mbomMap.put("attribute[JF_VPMReference.JF_PartDes]","JF_PartDes");
        mbomMap.put("attribute[JF_VPMReference.JF_PartENDes]","JF_PartENDes");

        Map<String, String> newMap = new HashMap<>();
        for (Map.Entry<String, String> entry : map.entrySet()) {
            String oldKey = entry.getKey();
            if (mbomMap.containsKey(oldKey)) {
                newMap.put(mbomMap.get(oldKey), entry.getValue());
            }
        }
        JF_LOGGER.info("newMap:{}", newMap);
        return newMap;
    }

    /**
     * ecr驱动MBOM初始化 ECR提交到会签状态，触发此方法   当前方法初始化MBOM只会初始化AA.1版本
     * 1. <JFNewECR>提升到Complete“Countersign”状态时触发trigger
     * @param context
     * @param args
     * @author LIUJR
     * @throws
     * @return void
     * @date 07/07/2025 13:15
     * @description
     */
    public void ecrDriverMBOMInitStructure(Context context, String[] args) throws Exception{
        try {
            JF_LOGGER.info("ecrDriverMBOMInitStructure .......start.........");
            String ecrId = args[0];
            DomainObject domainObject = DomainObject.newInstance(context);
            domainObject.setId(ecrId);
            JF_LOGGER.info("ecrId:{}", ecrId);
            String ecrName = domainObject.getInfo(context, DomainConstants.SELECT_NAME);
            String ecrTitle = domainObject.getInfo(context, SELECT_ATTRIBUTE_TITLE);
            //1.获取ecr所属的项目
            String projectId = domainObject.getInfo(context, "from[" + JF_PLMConstants_mxJPO.REL_JFChange2Project + "].to.id");
            JF_LOGGER.info("projectId:{}", projectId);
            //1.获取ecr的受影响清单
            StringList rootItemList = domainObject.getInfoList(context, "from[" + JF_PLMConstants_mxJPO.REL_JFECRRelateRoot + "].to.id");
            JF_LOGGER.info("rootItemList:{}", rootItemList);
            StringList zeroPartList = new StringList();
            //2.根据root关系查询<JFNewECR>关联的VPMReference，判断当前VPMReference与对应的<Project Space>是否存在[JFProject2RootPart]关系：
            StringList allRootPartNameList = new StringList();
            //拿取项目的
            DomainObject objectProject = DomainObject.newInstance(context, projectId);
            StringList selList = JF_Util_mxJPO.basicBolistSel();
            StringList relList = JF_Util_mxJPO.basicRellistSel();
            MapList partMapList = objectProject.getRelatedObjects(context,
                    JF_PLMConstants_mxJPO.rel_JFProject2RootPart, //pattern to match relationships
                    JF_PLMConstants_mxJPO.TYPE_VPMReference, //pattern to match types
                    selList, //the eMatrix StringList object that holds the list of select statement pertaining to Business Obejcts.
                    relList, //the eMatrix StringList object that holds the list of select statement pertaining to Relationships.
                    false, //get To relationships
                    true, //get From relationships
                    (short) 1, //the number of levels to expand, 0 equals expand all.
                    "", //where clause to apply to objects, can be empty ""
                    "attribute[JFZeroPart]==Y", //where clause to apply to relationship, can be empty ""
                    (short) 0);//limit
            StringList partList = (StringList) partMapList.stream().map(m -> {
                Map map = (Map) m;
                return UIUtil.getValue(map, SELECT_ID);
            }).collect(Collectors.toCollection(StringList::new));
            getCreateMBOMPartList(context, rootItemList, partList, zeroPartList, allRootPartNameList);
            //没有整椅 不执行
            if (zeroPartList.isEmpty()) {
                return;
            }
            Boolean flag = rootItemList.size() == zeroPartList.size() ? Boolean.TRUE : Boolean.FALSE;
            JF_LOGGER.info("zeroPartList:{}", zeroPartList);
            //  4. 触发一下MBOM初始化逻辑
            /*     4.1 通过<JFChange2Project>关系获取<JFNewECR>关联的<Project Space>；
                   4.2 判断对应的<Project Space>是否已经存在<jf_relProject2MBOM>关系，如果不存在，新建<JF_ManufacturedItem>对象，
                   <JF_ManufacturedItem>[V_Name]=<Project Space>.[Title]，Owner转移给项目经理，
                   并通过<jf_relProject2MBOM>关系与<JF_ManufacturedItem>对象关联；如果存在，获取<JF_ManufacturedItem>对象；
                   4.3 将整椅列表中的EBOM同步生成完整的MBOM结构”挂接到<JF_relProject2MBOM>关联的<JF_ManufacturedItem>下，
                   数据权限转移给<Project Space>关联的AME成员；
             */
            DomainObject projectObject = DomainObject.newInstance(context);
            projectObject.setId(projectId);
            String projectNameAndDesc = projectObject.getInfo(context, DomainConstants.SELECT_NAME) + "(" + projectObject.getDescription(context) + ")";
            StringList rootIdList = projectObject.getInfoList(context, "from[" + RELATIONSHIP_JF_relProject2MBOM + "].to.id");
            JF_LOGGER.info("rootIdList:{}", rootIdList);
            String projectManager = JF_Util_mxJPO.getProjectManager(context,new String[]{projectId});
            String rootId = "";
            ContextUtil.startTransaction(context, true);
            if (rootIdList.size() > 0) {
                rootId = rootIdList.get(0);
            } else {
                //无MBOM顶层节点 需要创建
                //创建Root根节点
                rootId = createMBOMGX(context, projectObject,projectManager);
            }
            HashSet<String> emailSet = initMBOM(context, projectId, zeroPartList, rootId);
            if (flag) {
                //生成变更记录 设置为已更新ECR
                //5.标记<JFNewECR>[JF_IsSyncMBOM]="Yes";  如果ecr是首次发放就需要设置为Yes
                domainObject.setAttributeValue(context, ATTR_JF_IsSyncMBOM, "Yes");
                //构造json文件
                Map<String, String> jsonFile = createChangeRecordMbomJsonFile(context, projectObject, DomainObject.newInstance(context, rootId), "首版初始化MBOM");
                JF_LOGGER.info("将当前现有的MBOM结构保存为JSON串");
                String ecrNameTitle = ecrName + (UIUtil.isNullOrEmpty(ecrTitle) ? "" : ":" + ecrTitle);
                String changeRecordId = createChangeRecord(context, StringList.create(ecrNameTitle), projectId, StringList.create(ecrId), context.getUser());
                domainObject.setId(changeRecordId);
                domainObject.setDescription(context, "首版初始化MBOM");
                domainObject.checkinFile(context, false, true, "", "generic", jsonFile.get("fileName"), UIUtil.getValue(jsonFile, "filePath"));
            }
            //6. 邮件通知项目中角色为“SDT-总装AME代表”、“SDT-发泡AME代表”及“SDT-面套AME代表”，MBOM已经创建。 对应的结构，才会发送邮件
            createMBOMSendEmail(context, emailSet, domainObject.getInfo(context, SELECT_NAME), projectId, allRootPartNameList, projectNameAndDesc);
            ContextUtil.commitTransaction(context);
        }catch (Exception e) {
            e.printStackTrace();
            ContextUtil.abortTransaction(context);
            throw e;
        }
        JF_LOGGER.info("ecrDriverMBOMInitStructure .......end.........");
    }

    /**
     * 获取整椅清单中符合条件的整椅 是项目中的整椅，同时是AA版本
     * @param context
     * @param rootItemList
     * @param partList
     * @param zeroPartList
     * @param allRootPartNameList
     * @author LIUJR
     * @throws
     * @return void
     * @date 2025/7/19 16:54
     * @description
     */
    public void getCreateMBOMPartList(Context context, StringList rootItemList, StringList partList, StringList zeroPartList, StringList allRootPartNameList) throws Exception{
        DomainObject partObject = DomainObject.newInstance(context);
        for (int i = 0; i < rootItemList.size(); i++) {
            String  partId = rootItemList.get(i);
            if (!partList.contains(partId)) {
                //不是项目的整椅
                continue;
            }
            partObject.setId(partId);
            //获取整椅列表[revision]属性，如果整椅的版本[revision]=="AA.*"，则触发一下MBOM初始化逻辑，否则不做处理：
            String revision = partObject.getInfo(context, DomainConstants.SELECT_REVISION);
            if (!revision.contains("AA")) {
                continue;
            }
            zeroPartList.add(partId);
            allRootPartNameList.add(partObject.getAttributeValue(context, JF_PLMConstants_mxJPO.ATTR_V_PART_NUMBER) + "_" + revision);
        }
    }

    /**
     * 初始化MBOM
     * @param context
     * @param projectId
     * @param zeroPartList
     * @param rootId
     * @author LIUJR
     * @throws
     * @return java.util.HashSet<java.lang.String>
     * @date 2025/7/17 10:19
     * @description
     */
    public HashSet<String>  initMBOM(Context context, String projectId, StringList zeroPartList, String rootId) throws Exception{
        HashSet<String> emailSet = new HashSet<>();
        try {
            JF_LOGGER.info("initMBOM .......start.........");
            JF_ECRProcess_mxJPO process = new JF_ECRProcess_mxJPO();
            String projectManager = JF_Util_mxJPO.getProjectManager(context,new String[]{projectId});
            Map AME = process.getProjectRole(context, projectId, "AME representative");
            Map Foam = process.getProjectRole(context, projectId, "Foam AME representative");
            Map Trim = process.getProjectRole(context, projectId, "Trim AME representative");
            String trimOwner=projectManager;
            String FoamOwner=projectManager;
            String AMEOwner=projectManager;
            if(Trim.size()>0){
                trimOwner = UIUtil.getValue(Trim,DomainConstants.SELECT_NAME);
            }
            if(Foam.size()>0){
                FoamOwner = UIUtil.getValue(Foam,DomainConstants.SELECT_NAME);
            }
            if(AME.size()>0){
                AMEOwner = UIUtil.getValue(AME,DomainConstants.SELECT_NAME);
            }
            HashMap<String, String> ownerMap = new HashMap<>();
            ownerMap.put("U", FoamOwner);
            ownerMap.put("T", trimOwner);
            ownerMap.put("A", AMEOwner);
            JF_LOGGER.info("ownerMap:{}", ownerMap);
            /*
                4.4 初始化MBOM结构
                    1. MBOM对象Name mass+9位流水号”，如mass-000000001;
                    2. EBOM对象属性映射至MBOM对象对应属性，映射关系见下表：
                    3. 所有MBOM制造件对象的“组织”默认为“制造工程部”，“合作区”默认为“JFSeat”
                    4. MBOM顶层节点权限默认转移给项目成员项目角色为“SDT-总装AME代表”的人员；
                    5. 整椅MBOM自顶向下，第一个“零件号”以“GU”开头的MBOM对象及其所有子级结构的权限，转移给项目成员项目角色为“SDT-发泡AME代表”的人员；
                    6. 整椅MBOM自顶向下，第一个“零件号”以“GT”开头的MBOM对象及其所有子级结构的权限，转移给项目成员项目角色为“SDT-面套AME代表”的人员；
                    7. 整椅MBOM下其他数据权限转移给“SDT-总装AME代表”的人员。
            */
            StringList selList = JF_Util_mxJPO.basicBolistSel();
            StringList relList = JF_Util_mxJPO.basicRellistSel();
            relList.add("attribute[JF_VPMInstance.JF_Dosage]");
            relList.add(JF_PLMConstants_mxJPO.SELECT_Attr_SynchroEBOMCAD);
            relList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JF_Dosage);
            selList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JFPartType);
            for (int i = 0; i < zeroPartList.size(); i++) {
                String partId = zeroPartList.get(i);
                String partType = DomainObject.newInstance(context, partId).getInfo(
                        context, JF_PLMConstants_mxJPO.SELECT_ATTR_JFPartType);
                //需要考虑供货件处理，如果是面套、发泡是供货件的话，需要直接整个结构都是给对应的AME人员
                //先创建GC
                //20260819 update by ljr 供货件初始化按零件类型路由面套专用创建方法；
                Map<String, String> rootCreateResult = createMBOMPartByType(
                        context, partId, partType, AMEOwner, projectId, "1");
                String mbomGCID = UIUtil.getValue(rootCreateResult, SELECT_ID);
                emailSet.add(AMEOwner);
                //GX和GC建立关系
                connectionMBOM(context, rootId, mbomGCID,
                        UIUtil.getValue(rootCreateResult, TRIM_RESULT_DOSAGE));
                Map<String,StringList> oneLevelMap = new HashMap<>();
                //拿到一级件的类型
                gcExpand(context,partId,selList,relList,oneLevelMap);
                StringList uList = oneLevelMap.get("U");//发泡
                StringList tList = oneLevelMap.get("T");//面套
                StringList oList = oneLevelMap.get("O");//总装
                JF_LOGGER.info("uList:{}", uList);
                JF_LOGGER.info("tList:{}", tList);
                JF_LOGGER.info("oList:{}", oList);
                if(uList!=null){
                    for(String temId:uList){
                        String id = FrameworkUtil.split(temId, "@").get(0);
                        String usage = FrameworkUtil.split(temId, "@").get(1);
                        //20260819 update by ljr 一级节点按零件类型路由面套专用创建方法；
                        Map<String, String> createResult = createMBOMPartByType(
                                context, id, "U", FoamOwner, projectId, usage);
                        String subId = UIUtil.getValue(createResult, SELECT_ID);
                        usage = UIUtil.getValue(createResult, TRIM_RESULT_DOSAGE);
                        emailSet.add(FoamOwner);
                        connectionMBOM(context, mbomGCID,subId,usage);
                        MapList uMapList = expandAllAME(context, subId,id, selList, relList,FoamOwner, ownerMap, emailSet,projectId);
                    }}
                if(tList!=null) {
                    for (String temId : tList) {
                        String id = FrameworkUtil.split(temId, "@").get(0);
                        String usage = FrameworkUtil.split(temId, "@").get(1);
                        //20260819 update by ljr 一级面套节点调用面套专用创建方法；
                        Map<String, String> createResult = createMBOMPartByType(
                                context, id, JF_PLMConstants_mxJPO.ATTR_JFPartType_RANGE_T,
                                trimOwner, projectId, usage);
                        String subId = UIUtil.getValue(createResult, SELECT_ID);
                        usage = UIUtil.getValue(createResult, TRIM_RESULT_DOSAGE);
                        emailSet.add(trimOwner);
                        connectionMBOM(context, mbomGCID, subId, usage);
                        MapList tMapList = expandAllAME(context, subId, id, selList, relList, trimOwner, ownerMap, emailSet,projectId);
                    }
                }
                if(oList!=null) {
                    for (String temId : oList) {
                        String id = FrameworkUtil.split(temId, "@").get(0);
                        String usage = FrameworkUtil.split(temId, "@").get(1);
                        //20260819 update by ljr 一级节点按零件类型路由面套专用创建方法；
                        Map<String, String> createResult = createMBOMPartByType(
                                context, id, "O", AMEOwner, projectId, usage);
                        String subId = UIUtil.getValue(createResult, SELECT_ID);
                        usage = UIUtil.getValue(createResult, TRIM_RESULT_DOSAGE);
                        emailSet.add(AMEOwner);
                        connectionMBOM(context, mbomGCID, subId, usage);
                        MapList oMapList = expandAllAME(context, subId, id, selList, relList, AMEOwner, ownerMap, emailSet,projectId);
                    }
                }
            }
        }catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
        return emailSet;
    }

    /**
     * 拿取项目的AME角色的人员
     * @param context
     * @param projectId
     * @author LIUJR
     * @throws
     * @return java.util.Map
     * @date 2025/8/16 13:30
     * @description
     */
    public HashMap<String, String> getProjectAMERolePerson(Context context, String projectId) throws Exception{
        HashMap<String, String> ownerMap = null;
        try {
            JF_ECRProcess_mxJPO process = new JF_ECRProcess_mxJPO();
            String projectManager = JF_Util_mxJPO.getProjectManager(context,new String[]{projectId});
            Map AME = process.getProjectRole(context, projectId, "AME representative");
            Map Foam = process.getProjectRole(context, projectId, "Foam AME representative");
            Map Trim = process.getProjectRole(context, projectId, "Trim AME representative");
            String trimOwner=projectManager;
            String FoamOwner=projectManager;
            String AMEOwner=projectManager;
            if(Trim.size()>0){
                trimOwner = UIUtil.getValue(Trim,DomainConstants.SELECT_NAME);
            }
            if(Foam.size()>0){
                FoamOwner = UIUtil.getValue(Foam,DomainConstants.SELECT_NAME);
            }
            if(AME.size()>0){
                AMEOwner = UIUtil.getValue(AME,DomainConstants.SELECT_NAME);
            }
            ownerMap = new HashMap<>();
            ownerMap.put("U", FoamOwner);
            ownerMap.put("T", trimOwner);
            ownerMap.put("A", AMEOwner);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
        return ownerMap;
    }

    /**
     * 初始化MBOM
     * @param context
     * @param projectId
     * @param zeroPartList
     * @param rootId
     * @param connectRoot 是否立即关联传入的MBOM父节点
     * @author LIUJR
     * @throws Exception
     * @return com.matrixone.apps.domain.util.MapList 新建的整椅MBOM信息
     * @date 2026/7/24 10:19
     * @description
     */
    public MapList  initECRMBOM(Context context,HashMap<String, String> ownerMap, String ecrId, String projectId, StringList zeroPartList, String rootId, Map<String, String> partCadMap, boolean connectRoot) throws Exception{
        MapList mapList = new MapList();
        HashSet<String> emailSet = new HashSet<>();
        try {
            JF_LOGGER.info("initECRMBOM .......start.........");
            JF_LOGGER.info("ownerMap:{}", ownerMap);
            String trimOwner=UIUtil.getValue(ownerMap, "T");
            String FoamOwner=UIUtil.getValue(ownerMap, "U");
            String AMEOwner=UIUtil.getValue(ownerMap, "A");
            /*
                4.4 初始化MBOM结构
                    1. MBOM对象Name mass+9位流水号”，如mass-000000001;
                    2. EBOM对象属性映射至MBOM对象对应属性，映射关系见下表：
                    3. 所有MBOM制造件对象的“组织”默认为“制造工程部”，“合作区”默认为“JFSeat”
                    4. MBOM顶层节点权限默认转移给项目成员项目角色为“SDT-总装AME代表”的人员；
                    5. 整椅MBOM自顶向下，第一个“零件号”以“GU”开头的MBOM对象及其所有子级结构的权限，转移给项目成员项目角色为“SDT-发泡AME代表”的人员；
                    6. 整椅MBOM自顶向下，第一个“零件号”以“GT”开头的MBOM对象及其所有子级结构的权限，转移给项目成员项目角色为“SDT-面套AME代表”的人员；
                    7. 整椅MBOM下其他数据权限转移给“SDT-总装AME代表”的人员。
            */
            StringList selList = JF_Util_mxJPO.basicBolistSel();
            StringList relList = JF_Util_mxJPO.basicRellistSel();
            relList.add("attribute[JF_VPMInstance.JF_Dosage]");
            relList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JF_Dosage);
            relList.add(SELECT_ATTRIBUTE_JF_ECRID);
            relList.add(JF_PLMConstants_mxJPO.SELECT_Attr_SynchroEBOMCAD);
            relList.add(SELECT_ATTRIBUTE_JF_BOMQuantity);
            relList.add(SELECT_FROM_ID);
            selList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JFPartType);
            selList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_V_PART_NUMBER);
            selList.add("attribute[JF_PartNumber]");
            DomainObject domainObject = DomainObject.newInstance(context);
            Map info = new HashMap();
            for (int i = 0; i < zeroPartList.size(); i++) {
                String owner = EMPTY_STRING;
                String partId = zeroPartList.get(i);
                //需要考虑供货件处理，如果是面套、发泡是供货件的话，需要直接整个结构都是给对应的AME人员
                domainObject.setId(partId);
                String partType = domainObject.getAttributeValue(context, JF_PLMConstants_mxJPO.ATTR_JFPartType);
                if ("T".equalsIgnoreCase(partType)) {
                    owner = trimOwner;
                } else if ("U".equalsIgnoreCase(partType)) {
                    owner = FoamOwner;
                } else {
                    owner = AMEOwner;
                }
                //先创建GC
                //20260819 update by ljr 更新场景新增供货件按零件类型路由面套专用创建方法；
                Map<String, String> rootCreateResult = createMBOMPartByType(
                        context, partId, partType, owner, projectId, "1");
                String mbomGCID = UIUtil.getValue(rootCreateResult, SELECT_ID);
                String connId = EMPTY_STRING;
                if (connectRoot) {
                    //GX和GC建立关系
                    connId = connectionMBOM(context, rootId, mbomGCID,
                            UIUtil.getValue(rootCreateResult, TRIM_RESULT_DOSAGE));
                } else {
                    //20260724 update by ljr 更新MBOM的整椅线程暂不关联公共根节点，待所有线程结束后由主线程串行关联；
                    JF_LOGGER.info("defer connect root MBOM, rootId:{}, mbomGCID:{}", rootId, mbomGCID);
                }
                domainObject.setId(mbomGCID);
                info = domainObject.getInfo(context, selList);
                info.put(DomainRelationship.SELECT_ID, connId);
                mapList.add(info);
                Map<String,StringList> oneLevelMap = new HashMap<>();
                //拿到一级件的类型
                gcExpand(context,partId,selList,relList,oneLevelMap);
                StringList uList = oneLevelMap.get("U");//发泡
                StringList tList = oneLevelMap.get("T");//面套
                StringList oList = oneLevelMap.get("O");//总装
                JF_LOGGER.info("uList:{}", uList);
                JF_LOGGER.info("tList:{}", tList);
                JF_LOGGER.info("oList:{}", oList);
                //20260825 update by ljr C场景新增节点的后续子树沿用初始化递归，按完整EBOM结构创建。
                if(uList!=null){
                    for(String temId:uList){
                        String id = FrameworkUtil.split(temId, "@").get(0);
                        String usage = FrameworkUtil.split(temId, "@").get(1);
                        //20260819 update by ljr 更新场景新增一级节点按零件类型路由面套专用创建方法；
                        Map<String, String> createResult = createMBOMPartByType(
                                context, id, "U", FoamOwner, projectId, usage);
                        String subId = UIUtil.getValue(createResult, SELECT_ID);
                        usage = UIUtil.getValue(createResult, TRIM_RESULT_DOSAGE);
                        connectionMBOM(context, mbomGCID,subId,usage);
                        MapList uMapList = expandAllAME(context, subId, id, selList, relList,
                                FoamOwner, ownerMap, emailSet, projectId);
                    }}
                if(tList!=null) {
                    for (String temId : tList) {
                        String id = FrameworkUtil.split(temId, "@").get(0);
                        String usage = FrameworkUtil.split(temId, "@").get(1);
                        //20260819 update by ljr 更新场景新增一级面套节点调用面套专用创建方法；
                        Map<String, String> createResult = createMBOMPartByType(
                                context, id, JF_PLMConstants_mxJPO.ATTR_JFPartType_RANGE_T,
                                trimOwner, projectId, usage);
                        String subId = UIUtil.getValue(createResult, SELECT_ID);
                        usage = UIUtil.getValue(createResult, TRIM_RESULT_DOSAGE);
                        connectionMBOM(context, mbomGCID, subId, usage);
                        MapList tMapList = expandAllAME(context, subId, id, selList, relList,
                                trimOwner, ownerMap, emailSet, projectId);
                    }
                }
                if(oList!=null) {
                    for (String temId : oList) {
                        String id = FrameworkUtil.split(temId, "@").get(0);
                        String usage = FrameworkUtil.split(temId, "@").get(1);
                        //20260819 update by ljr 更新场景新增一级节点按零件类型路由面套专用创建方法；
                        Map<String, String> createResult = createMBOMPartByType(
                                context, id, "O", AMEOwner, projectId, usage);
                        String subId = UIUtil.getValue(createResult, SELECT_ID);
                        usage = UIUtil.getValue(createResult, TRIM_RESULT_DOSAGE);
                        connectionMBOM(context, mbomGCID, subId, usage);
                        MapList oMapList = expandAllAME(context, subId, id, selList, relList,
                                AMEOwner, ownerMap, emailSet, projectId);
                    }
                }
            }
        }catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
        return mapList;
    }


    /**
     * 发送邮件
     * @param context
     * @param toAddressee
     * @param objectId
     * @param ecrName
     * @param zeroPartNames
     * @param projectName
     * @param tempName
     * @param zhTitle
     * @param enTitle
     * @author LIUJR
     * @throws
     * @return void
     * @date 2025/7/24 14:20
     * @description
     */
    public static void mbomStructureSendEmail(Context context, String toAddressee, String objectId,
                                              Object ecrName, String zeroPartNames, String projectName,
                                              String tempName, String zhTitle, String enTitle) throws Exception{
        JF_LOGGER.info("initMBOMStructureSendEmail ----------  start ----------------");
        // 创建多部分消息体
        MimeMultipart multipart = new MimeMultipart(); // 默认混合模式
        //邮件内容的html模板部分
        BodyPart msgBodyPart = new MimeBodyPart();
        //邮件内容的html模板部分
        //链接地址
        Properties prop = NioJDUtils.readPageObject(context, "JFJDConfig");
        String linkAddress = prop.getProperty("JF.3dspace.JFUrl").trim();
        linkAddress += objectId;
        //拿取html模板
        String html = JF_PublicMethodClass_mxJPO.getPageHTMLResourceFile(context, tempName, "zh");
        Document doc = Jsoup.parse(html);
        if (UIUtil.isNotNullAndNotEmpty(projectName)) {
            doc.getElementById("Project").append(projectName);
        }
        if (!"ResponsibleEmail".equalsIgnoreCase(tempName)) {
            doc.getElementById("partNum").append(zeroPartNames);
        }
        if (ecrName instanceof String) {
            String strEcrName = String.valueOf(ecrName);
            if (UIUtil.isNotNullAndNotEmpty(strEcrName)) {
                doc.getElementById("connectName").append(strEcrName);
            }
        } else if (ecrName instanceof MapList) {
            MapList crECRMapList = (MapList) ecrName;
            //写入table
            //开始构造table表
            StringList listAttr = new StringList();
            listAttr.add(SELECT_NAME);
            listAttr.add("date");
            listAttr.add("time");
            listAttr.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JF_UpdatePerson);
            listAttr.add(SELECT_DESCRIPTION);
            listAttr.add("ecr");
            for (int i = 0; i < crECRMapList.size(); i++) {
                Map map = (Map) crECRMapList.get(i);
                JF_SendEmailUtils_mxJPO.writeTableData(context, "ECRInfoListTbody", map, doc, listAttr);
            }
        }
        //设置链接地址
        Element address = doc.getElementById("Address");
        //链接href
        address.attr("href", linkAddress);
        //设置显示的值
        address.text("zh".equalsIgnoreCase("zh") ? "点击查看MBOM结构" : "Click to view MBOM structure");
        String htmlContent = doc.toString();
        msgBodyPart.setContent(htmlContent, "text/html;charset=utf-8");//html代码部分
        multipart.addBodyPart(msgBodyPart);
        Boolean aBoolean = JF_SendEmailUtils_mxJPO.SendEmail(context, toAddressee, "zh".equalsIgnoreCase("zh") ? zhTitle + " : " + projectName : enTitle + " : " + projectName, multipart);
    }

    /**
     * @Author Liuxg
     * @Description 添加辅料功能  创建MBOM对象，并关联父级对象
     * @Date 2025/7/7 15:15
     * @Param [context, args]
     * @return java.lang.String
     **/
    public Map CreateMbomPartAndConnect(Context context,String[]args)throws Exception{

        Map returnMap=new HashMap();
        String errorMsg="";
        StringList idlist=new StringList();
        Boolean flag = Boolean.FALSE;
        try {
            String loginUser=context.getUser();
            Map pramMap=JPO.unpackArgs(args);
            //update by ljr 20251024
            String projectid = (String) pramMap.get("objectid");
            String connParts = (String) pramMap.get("connParts");
            String strJF_Dosage = (String) pramMap.get("JF_Dosage");
            StringList vpmidList= (StringList) pramMap.get("vpmidList");
            JF_LOGGER.info("objectid--->"+projectid);
            JF_LOGGER.info("vpmidList--->"+vpmidList);
            JF_LOGGER.info("connParts--->"+connParts);
            JF_LOGGER.info("strJF_Dosage--->"+strJF_Dosage);
            ContextUtil.pushContext(context);
            ContextUtil.startTransaction(context, true);
            flag = Boolean.TRUE;
            String[] split = connParts.split(",");
            for (int i = 0; i < split.length; i++) {
                String objectid = split[i];
                for (String vpmid : vpmidList) {
                    String Mbomid = createMBOMPartFormal(context, vpmid, loginUser, projectid);
                    idlist.add(Mbomid);
                    //用量默认1
                    connectionMBOMSetFlag(context, objectid, Mbomid, strJF_Dosage, "ADD");
                }
                //增加辅料件  需要增加到相同number和版本下
                DomainObject domainObject = DomainObject.newInstance(context, objectid);
                String subType = domainObject.getAttributeValue(context, JF_PLMConstants_mxJPO.ATTR_JF_VPMReference_JF_PartSubType);
                if (!"T06".equalsIgnoreCase(subType)) {
                    //找到项目id
                    MapList childPartList = getMbomSameParts(context, projectid, objectid);
                    JF_LOGGER.info("childPartList--->" + childPartList);
                    JF_LOGGER.info("=============================");
                    if (!childPartList.isEmpty()) {
                        Iterator iterator = childPartList.iterator();
                        while (iterator.hasNext()) {
                            Map map = (Map) iterator.next();
                            String id = UIUtil.getValue(map, SELECT_ID);
                            for (String vpmid : vpmidList) {
                                String Mbomid = createMBOMPartFormal(context, vpmid, loginUser, projectid);
                                //用量默认1
                                connectionMBOMSetFlag(context, id, Mbomid, strJF_Dosage, "ADD");
                            }
                        }
                    }
                    JF_LOGGER.info("childPartList--->" + childPartList);
                }
            }
            ContextUtil.commitTransaction(context);
        }catch (Exception e){
            e.printStackTrace();
            ContextUtil.abortTransaction(context);
            errorMsg = e.getMessage();
            throw e;
        } finally {
            if (flag) {
                ContextUtil.popContext(context);
            }
        }
        returnMap.put("msg",errorMsg);
        returnMap.put("idlist",idlist);
        return returnMap;
    }

    /**
     * 获取MBOM件在整个MBOM中是否存在多个件
     * @param context
     * @param projectId
     * @param objectId
     * @author LIUJR
     * @throws
     * @return com.matrixone.apps.domain.util.MapList
     * @date 2025/9/3 10:12
     * @description
     */
    public MapList getMbomSameParts(Context context, String projectId, String objectId) {
        MapList childPartList = new MapList();
        try {
            DomainObject domainObject = DomainObject.newInstance(context);
            domainObject.setId(objectId);
            String revision = domainObject.getInfo(context, SELECT_REVISION);
            String number = domainObject.getAttributeValue(context, "JF_PartNumber");
            JF_LOGGER.info("number:{}", number);
            JF_LOGGER.info("revision:{}", revision);
            //项目MBOM中查找number+revision相同的结构 ，将辅料件挂上去
            domainObject.setId(projectId);
            StringList rootIdList = domainObject.getInfoList(context, "from[" + RELATIONSHIP_JF_relProject2MBOM + "].to.id");
            String rootId = EMPTY_STRING;
            if (rootIdList.size() > 0) {
                rootId = rootIdList.get(0);
            }
            JF_LOGGER.info("rootId:{}", rootId);
            domainObject.setId(rootId);
            StringList basicBolistSel = JF_Util_mxJPO.basicBolistSel();
            basicBolistSel.add("attribute[JF_PartNumber]");
            MapList mapList =  domainObject.getRelatedObjects(
                    context,
                    JF_PLMConstants_mxJPO.REL_JF_relManufacturedItem,
                    JF_PLMConstants_mxJPO.TYPE_JF_ManufacturedItem,
                    basicBolistSel,
                    new StringList(),
                    false,
                    true,
                    (short) 0,
                    "",
                    "",
                    0);
            JF_LOGGER.info("objectId:{}", objectId);
            childPartList = (MapList) mapList.stream().filter(m -> {
                Map map = (Map) m;
                String partNumber = UIUtil.getValue(map, "attribute[JF_PartNumber]");
                String rev = UIUtil.getValue(map, SELECT_REVISION);
                String id = UIUtil.getValue(map, SELECT_ID);
                if (partNumber.equalsIgnoreCase(number) && rev.equalsIgnoreCase(revision) && !id.equalsIgnoreCase(objectId)) {
                    JF_LOGGER.info("id:{}", id);
                    return true;
                } else {
                    return false;
                }
            }).collect(Collectors.toCollection(MapList::new));
            JF_LOGGER.info("childPartList:{}", childPartList);
        }catch (Exception e) {
            e.printStackTrace();
        }
        return childPartList;
    }


    /**
     * @Author Liuxg
     * @Description 检查勾选的VPMREFERENCE是否符合创建Mbom对象条件
     * @Date 2025/7/7 15:30
     * @Param [context, vpmidList]
     * @return java.lang.String
     **/
    public String CheckCreateMbomVpmidlist(Context context,StringList vpmidList,String loginUser)throws Exception{
        String errorMsg="";
        try {
            DomainObject vpmobj;

            for(String vpmid:vpmidList){
                vpmobj =DomainObject.newInstance(context,vpmid);
                String owner=vpmobj.getOwner(context).getName();
                if(!loginUser.equals(owner)){
                    //当前所勾选数据无编辑权限，请重新选择！
                    errorMsg = EnoviaResourceBundle.getProperty(context, "emxFrameworkStringResource", context.getLocale(), "emxFramework.common.CreateMbomPartAndConnectErrorMsg");

                }
            }
        }catch (Exception e){
            e.printStackTrace();
            throw e;
        }
        return errorMsg;
    }

    /**
     * @Author Liuxg
     * @Description 断开MBOM关系，并删除对象
     * @Date 2025/7/11 6:51
     * @Param [context, args]
     * @return java.lang.String
     **/
    public String DeleteMbomPart(Context context,String[]args)throws Exception{
        String errorMsg="";
        Boolean flag = Boolean.FALSE;
        try {
            String loginUser=context.getUser();
            Map pramMap=JPO.unpackArgs(args);
            StringList vpmidList= (StringList) pramMap.get("vpmidList");
            String objectid= (String) pramMap.get("objectid");

            JF_LOGGER.info("vpmidList--->"+vpmidList);
            JF_LOGGER.info("objectid--->"+objectid);

            errorMsg=CheckDeleteMbomVpmidlist(context,vpmidList,loginUser);
            DomainObject domainObject = DomainObject.newInstance(context);
            if (UIUtil.isNotNullAndNotEmpty(errorMsg)) {
                return errorMsg;
            }

            /*
             * 添加辅料、移除辅料的时候  同一个零件同一个版本，结构保持一致，
             * */
            ContextUtil.pushContext(context);
            ContextUtil.startTransaction(context, true);
            flag = Boolean.TRUE;
            for(String mbomids:vpmidList){
                StringList tableIds = FrameworkUtil.split(mbomids, "|");
                String relId=tableIds.get(0);
                String mbomId=tableIds.get(1);
                String parentId=tableIds.get(2);
                JF_LOGGER.info("tableIds--->"+tableIds);
                JF_LOGGER.info("relId--->"+relId);
                JF_LOGGER.info("mbomId--->"+mbomId);
                JF_LOGGER.info("parentId--->"+parentId);
                JF_LOGGER.info("===============================");
                //移除MBOM节点的 number和版本
                MapList childPartList = getMbomSameParts(context, objectid, parentId);
                JF_LOGGER.info("childPartList--->"+childPartList);
                if (!childPartList.isEmpty()) {
                    domainObject.setId(mbomId);
                    String revision = domainObject.getInfo(context, SELECT_REVISION);
                    String number = domainObject.getAttributeValue(context, "JF_PartNumber");
                    Iterator iterator = childPartList.iterator();
                    //移除件父级的相同件
                    while (iterator.hasNext()) {
                        Map map = (Map) iterator.next();
                        String id = UIUtil.getValue(map, SELECT_ID);
                        domainObject.setId(id);
                        MapList mapList = domainObject.getRelatedObjects(
                                context,
                                JF_PLMConstants_mxJPO.REL_JF_relManufacturedItem,
                                JF_PLMConstants_mxJPO.TYPE_JF_ManufacturedItem,
                                new StringList(SELECT_ID),
                                new StringList(DomainRelationship.SELECT_ID),
                                false,
                                true,
                                (short) 1,
                                "attribute[JF_PartNumber]==" + number + "&&revision==" + revision + "&&id!=" + mbomId,
                                "",
                                0);
                        JF_LOGGER.info("mapList--->"+mapList);
                        if (mapList.isEmpty()) {
                            continue;
                        }
                        Iterator iterator1 = mapList.iterator();
                        while (iterator1.hasNext()) {
                            Map map1 = (Map) iterator1.next();
                            String id1 = UIUtil.getValue(map1, SELECT_ID);
                            String connId = UIUtil.getValue(map1, DomainRelationship.SELECT_ID);
                            DomainRelationship.disconnect(context,connId);
                            DomainObject.deleteObjects(context,new String[]{id1});
                        }
                    }
                }
                DomainRelationship.disconnect(context,relId);
                DomainObject.deleteObjects(context,new String[]{mbomId});
            }
            ContextUtil.commitTransaction(context);
        }catch (Exception e){
            e.printStackTrace();
            ContextUtil.abortTransaction(context);
            errorMsg=e.getMessage();
            throw e;
        }finally {
            if (flag) {
                ContextUtil.popContext(context);
            }
        }
        return errorMsg;
    }

    /**
     * @Author Liuxg
     * @Description 检查需要移除的Mbom对象是否满足条件
     * @Date 2025/7/7 16:44
     * @Param [context, vpmidList, loginUser]
     * @return java.lang.String
     * @description  update bu ljr  20260403   MBOM移除辅料按钮需要限制，只允许移除手动添加的辅料
     **/
    public String CheckDeleteMbomVpmidlist(Context context,StringList vpmidList,String loginUser)throws Exception{
        String errorMsg="";
        try {
            StringList mbomidlist=new StringList();
            DomainRelationship domainRelationship;
            DomainObject vpmobj = DomainObject.newInstance(context);
            String accessMsg = EnoviaResourceBundle.getProperty(context, "emxFrameworkStringResource", context.getLocale(), "emxFramework.common.CreateMbomPartAndConnectErrorMsg");
            String rootMsg = EnoviaResourceBundle.getProperty(context, "emxFrameworkStringResource", context.getLocale(), "emxFramework.common.DeleteMbomPartRootErrorMsg");
            StringList accessList = new StringList();
            StringList rootList = new StringList();
            StringBuilder sb = new StringBuilder();
            for (String tableRowId : vpmidList) {
                Map rowMap = ProgramCentralUtil.parseTableRowId(context,tableRowId);
                String strVpmId = (String) rowMap.get("objectId");
                String strPartRid = (String) rowMap.get("relId");
                mbomidlist.add(strVpmId);
                vpmobj.setId(strVpmId);
                String strJF_PartNumber = vpmobj.getAttributeValue(context, "JF_PartNumber");
                String parentOwner= vpmobj.getInfo(context,"to["+REL_JF_relManufacturedItem+"].from.owner");
                JF_LOGGER.info("parentOwner--->"+parentOwner);
                if(UIUtil.isNotNullAndNotEmpty(parentOwner)){
                    if(!loginUser.equals(parentOwner)){
                        //当前所勾选数据无编辑权限，请重新选择！
                        accessList.add(strJF_PartNumber);
                        continue;
                    }
                    if (UIUtil.isNotNullAndNotEmpty(strPartRid)) {
                        //不等于空  开始判断是否有权限
                        domainRelationship = DomainRelationship.newInstance(context,strPartRid);
                        String jfOriginateFlag = domainRelationship.getAttributeValue(context, "JF_OriginateFlag");
                        if (!"ADD".equalsIgnoreCase(jfOriginateFlag)) {
                            accessList.add(strJF_PartNumber);
                        }
                    }
                }else {
                    //当前所勾选数据为MBOM顶层节点，请重新选择！
                    rootList.add(strJF_PartNumber);
                }
            }
            if (!accessList.isEmpty()) {
                sb.append(accessList.join(",")).append(accessMsg);
            }
            if (!rootList.isEmpty()) {
                sb.append(rootList.join(",")).append(rootMsg);
            }
            errorMsg = sb.toString();
        }catch (Exception e){
            e.printStackTrace();
            throw e;
        }
        return errorMsg;
    }


    /**
     * @Author Liuxg
     * @Description mobm对象在table中的编辑权限
     * @Date 2025/7/10 7:42
     * @Param [context, args]
     * @return matrix.util.StringList
     **/
    public StringList tableMbomEditAccess(Context context, String[] args) throws Exception {
        StringList stringList = new StringList();
        try {
            Map programMap = (Map) JPO.unpackArgs(args);
            JF_LOGGER.info("programMap--->"+programMap);
            MapList objList = (MapList) programMap.get("objectList");
            String loginUser=context.getUser();

//            JF_LOGGER.info("objList--->"+objList);
            Map requestMap = (Map) programMap.get("requestMap");
            String projectId = (String) requestMap.get("objectId");
            //判断当前的责任人是否是AME
            Boolean mbomRootResponsibleIsPM = getMBOMRootResponsibleIsPM(context, projectId);
            if (mbomRootResponsibleIsPM) {
                //当是责任人是PM的时候
                for (int i = 0; i < objList.size(); i++) {
                    stringList.add(Boolean.toString(false));
                }
            }else {
                for (int i = 0; i < objList.size(); i++) {
                    String id = (String) ((Map) objList.get(i)).get("id");
                    DomainObject mbomObj = DomainObject.newInstance(context, id);
                    String owner = mbomObj.getOwner(context).getName();
                    if (loginUser.equals(owner)) {
                        stringList.add(Boolean.toString(true));
                    } else {
                        stringList.add(Boolean.toString(false));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
        return stringList;
    }

    /**
     * MBOM table中的用量的编辑权限
     * @param context
     * @param args
     * @author LIUJR
     * @throws
     * @return matrix.util.StringList
     * @date 2025/12/23 15:43
     * @description
     */
    public StringList tableMbomEditAccessforParent(Context context, String[] args) throws Exception {
        StringList stringList = new StringList();
        try {
            Map programMap = (Map) JPO.unpackArgs(args);
            MapList objList = (MapList) programMap.get("objectList");
            String loginUser=context.getUser();

            JF_LOGGER.info("objList--->"+objList);
            Map requestMap = (Map) programMap.get("requestMap");
            String projectId = (String) requestMap.get("objectId");
            //判断当前的责任人是否是AME
            Boolean mbomRootResponsibleIsPM = getMBOMRootResponsibleIsPM(context, projectId);
            if (mbomRootResponsibleIsPM) {
                //当是责任人是PM的时候
                for (int i = 0; i < objList.size(); i++) {
                    stringList.add(Boolean.toString(false));
                }
            } else {
                for (int i = 0; i < objList.size(); i++) {
                    String id = (String) ((Map) objList.get(i)).get("id");
                    String relid = (String) ((Map) objList.get(i)).get("id[connection]");
                    DomainRelationship domainRelationship=DomainRelationship.newInstance(context,relid);
                    DomainObject mbomObj = DomainObject.newInstance(context, id);
                    String parentOwner = mbomObj.getInfo(context, "to[" + REL_JF_relManufacturedItem + "].from.owner");
                    String idLevel = (String) ((Map) objList.get(i)).get("id[level]");
                    String jfOriginateFlag = domainRelationship.getAttributeValue(context, "JF_OriginateFlag");
                    if (idLevel.split(",").length == 3) {
                        stringList.add(Boolean.toString(false));
                    } else if (UIUtil.isNotNullAndNotEmpty(parentOwner) && loginUser.equals(parentOwner)) {
                        if ("ADD".equalsIgnoreCase(jfOriginateFlag)) {
                            stringList.add(Boolean.toString(true));
                        } else {
                            stringList.add(Boolean.toString(false));
                        }
                    } else {
                        stringList.add(Boolean.toString(false));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
        return stringList;
    }

    /**
     * 获取当前项目中更新好的已经完成的ECR
     * @param context
     * @param args
     * @author LIUJR
     * @throws
     * @return com.matrixone.apps.domain.util.MapList
     * @date 2025/7/14 13:34
     * @description
     */
    public MapList getSyncMBOMECR(Context context, String[] args) throws Exception {
        /*
         *  "待更新ECR清单"
         *   1.默认视图根据项目、<JFNewECR>状态为“Countersign”及以后
         *   2.<JFNewECR>.[JF_IsSyncMBOM]="No"、
         *   3.关联“整椅”的<JFNewECR>进行过滤
         * */
        try {
            StringList selList = JF_Util_mxJPO.basicBolistSel();
            StringList relList = JF_Util_mxJPO.basicRellistSel();
            Map paramsMap = (Map) JPO.unpackArgs(args);
            ContextUtil.pushContext(context);
            String strObjectId = (String) paramsMap.get(STRING_OBJECTID);
            JF_LOGGER.info("strObjectId:{}",strObjectId);
            DomainObject objectProject = DomainObject.newInstance(context, strObjectId);
            MapList mapList = objectProject.getRelatedObjects(context,
                    JF_PLMConstants_mxJPO.REL_JFChange2Project, //pattern to match relationships
                    JF_PLMConstants_mxJPO.TYPE_JFNewECR, //pattern to match types
                    selList, //the eMatrix StringList object that holds the list of select statement pertaining to Business Obejcts.
                    relList, //the eMatrix StringList object that holds the list of select statement pertaining to Relationships.
                    true, //get To relationships
                    false, //get From relationships
                    (short) 1, //the number of levels to expand, 0 equals expand all.
                    SELECT_ATTR_JF_IsSyncMBOM + "==Yes&&current!=Create&&current!=Review", //where clause to apply to objects, can be empty ""
                    null, //where clause to apply to relationship, can be empty ""
                    (short) 0);//limit
            JF_LOGGER.info("mapList:{}", mapList);
            return mapList;
        }catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * 获取当前项目中未更新的已经完成的ECR
     * @param context
     * @param args
     * @author LIUJR
     * @throws
     * @return com.matrixone.apps.domain.util.MapList
     * @date 2025/7/14 13:34
     * @description
     */
    public MapList getNotSyncMBOMECR(Context context, String[] args) throws Exception {
        /*
         *  "待更新ECR清单"
         *   1.默认视图根据项目、<JFNewECR>状态为“Countersign”及以后
         *   2.<JFNewECR>.[JF_IsSyncMBOM]="No"、
         *   3.关联“整椅”的<JFNewECR>进行过滤
         * */
        try {
            StringList selList = JF_Util_mxJPO.basicBolistSel();
            StringList relList = JF_Util_mxJPO.basicRellistSel();
            Map paramsMap = (Map) JPO.unpackArgs(args);
            ContextUtil.pushContext(context);
            String strObjectId = (String) paramsMap.get(STRING_OBJECTID);
            JF_LOGGER.info("strObjectId:{}",strObjectId);
            DomainObject objectProject = DomainObject.newInstance(context, strObjectId);
            MapList mapList = objectProject.getRelatedObjects(context,
                    JF_PLMConstants_mxJPO.REL_JFChange2Project, //pattern to match relationships
                    JF_PLMConstants_mxJPO.TYPE_JFNewECR, //pattern to match types
                    selList, //the eMatrix StringList object that holds the list of select statement pertaining to Business Obejcts.
                    relList, //the eMatrix StringList object that holds the list of select statement pertaining to Relationships.
                    true, //get To relationships
                    false, //get From relationships
                    (short) 1, //the number of levels to expand, 0 equals expand all.
                    SELECT_ATTR_JF_IsSyncMBOM + "==No&&current!=Create&&current!=Review", //where clause to apply to objects, can be empty ""
                    null, //where clause to apply to relationship, can be empty ""
                    (short) 0);//limit
            JF_LOGGER.info("mapList:{}", mapList);
//            MapList partMapList = objectProject.getRelatedObjects(context,
//                    JF_PLMConstants_mxJPO.rel_JFProject2RootPart, //pattern to match relationships
//                    JF_PLMConstants_mxJPO.TYPE_VPMReference, //pattern to match types
//                    selList, //the eMatrix StringList object that holds the list of select statement pertaining to Business Obejcts.
//                    relList, //the eMatrix StringList object that holds the list of select statement pertaining to Relationships.
//                    false, //get To relationships
//                    true, //get From relationships
//                    (short) 1, //the number of levels to expand, 0 equals expand all.
//                    "", //where clause to apply to objects, can be empty ""
//                    "attribute[JFZeroPart]==Y", //where clause to apply to relationship, can be empty ""
//                    (short) 0);//limit
//            StringList partList = (StringList) partMapList.stream().map(m -> {
//                Map map = (Map) m;
//                return UIUtil.getValue(map, SELECT_ID);
//            }).collect(Collectors.toCollection(StringList::new));
//            DomainObject domainObject = DomainObject.newInstance(context);
//            MapList returnMapList = new MapList();
//            for (int i = 0; i < mapList.size(); i++) {
//                Map map = (Map) mapList.get(i);
//                //判断ECR中是否全是整椅，如果是就保留，不是就移除
//                String id = UIUtil.getValue(map, DomainConstants.SELECT_ID);
//                domainObject.setId(id);
//                StringList zeroPartList = new StringList();
//                //1.获取ecr的受影响清单
//                StringList rootItemList = domainObject.getInfoList(context, "from[" + JF_PLMConstants_mxJPO.REL_JFECRRelateRoot + "].to.id");
//                for (int i1 = 0; i1 < rootItemList.size(); i1++) {
//                    String  partId = rootItemList.get(i1);
//                    if (partList.contains(partId)) {
//                        zeroPartList.add(partId);
//                    }
//                }
//                JF_LOGGER.info("zeroPartList:{}", zeroPartList);
//                if (!zeroPartList.isEmpty()) {
//                    returnMapList.add(map);
//                }
//            }
            JF_LOGGER.info("returnMapList:{}", mapList);
            return mapList;
        }catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * 获取当前项目中已经完成的所有ECR
     * @param context
     * @param args
     * @author LIUJR
     * @throws
     * @return com.matrixone.apps.domain.util.MapList
     * @date 2025/7/14 13:43
     * @description
     */
    public MapList getAllMBOMECR(Context context, String[] args) throws Exception {
        /*
         *  "本项目所有的ECR视图"
         *   用户可以通过切换视图查看本项目关联的所有ECR清单；
         * */
        try {
            StringList selList = JF_Util_mxJPO.basicBolistSel();
            StringList relList = JF_Util_mxJPO.basicRellistSel();
            Map paramsMap = (Map) JPO.unpackArgs(args);
            ContextUtil.pushContext(context);
            String strObjectId = (String) paramsMap.get(STRING_OBJECTID);
            JF_LOGGER.info("strObjectId:{}",strObjectId);
            DomainObject objectProject = DomainObject.newInstance(context, strObjectId);
            MapList mapList = objectProject.getRelatedObjects(context,
                    JF_PLMConstants_mxJPO.REL_JFChange2Project, //pattern to match relationships
                    JF_PLMConstants_mxJPO.TYPE_JFNewECR, //pattern to match types
                    selList, //the eMatrix StringList object that holds the list of select statement pertaining to Business Obejcts.
                    relList, //the eMatrix StringList object that holds the list of select statement pertaining to Relationships.
                    true, //get To relationships
                    false, //get From relationships
                    (short) 1, //the number of levels to expand, 0 equals expand all.
                    "current!=Create&&current!=Review", //where clause to apply to objects, can be empty ""
                    null, //where clause to apply to relationship, can be empty ""
                    (short) 0);//limit
            JF_LOGGER.info("mapList:{}", mapList);
            DomainObject domainObject = DomainObject.newInstance(context);
            MapList returnMapList = new MapList();
            for (int i = 0; i < mapList.size(); i++) {
                Map map = (Map) mapList.get(i);
                //判断ECR中是否全是整椅，如果是就保留，不是就移除
                String id = UIUtil.getValue(map, DomainConstants.SELECT_ID);
                domainObject.setId(id);
                //判断ecr是不是同步过
                if ("Yes".equalsIgnoreCase(domainObject.getAttributeValue(context, ATTR_JF_IsSyncMBOM))) {
                    map.put("disableSelection", "true");
                }
                returnMapList.add(map);
            }
            JF_LOGGER.info("returnMapList:{}", returnMapList);
            return returnMapList;
        }catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * 获取ECR连接
     * @param context
     * @param args
     * @author LIUJR
     * @throws
     * @return matrix.util.StringList
     * @date 2025/7/14 14:25
     * @description
     */
    public StringList getECRDetailsLink(Context context,String[] args) throws Exception{
        Map argMaps = JPO.unpackArgs(args);
        StringList res = new StringList();
        MapList argMapList = (MapList) argMaps.get(JF_PLMConstants_mxJPO.STRING_OBJECTLIST);
        StringBuilder strBuilder = new StringBuilder();
        strBuilder.append("<a href=\"JavaScript:emxTableColumnLinkClick('../common/emxTree.jsp?mode=popup" +
                "&amp;DefaultCategory=JFNewAffectedItemsCmd" +
//                "&amp;portalMode=true" +
//                "&amp;defaultObjCategoryName=JFECRNewAffectedItemsParentCmd" +
//                "&amp;portalCmdName=JFECRNewAffectedItemsParentCmd" +
                "&amp;objectId=");
        strBuilder.append("strObjectId");
        strBuilder.append("', '700', '600', 'false', 'popup', '')\" >");
        strBuilder.append("<img border='0' src='../common/images/I_PPRSeqInterrupt.gif' alt=\"strName\" title=\"strName\"></img></a>");
        String strObjColHtml = strBuilder.toString();
        for (int i = 0; i < argMapList.size(); i++) {
            Map infoMap = (Map) argMapList.get(i);
            String strId = (String) infoMap.get(SELECT_ID);
            String strName = (String) infoMap.get(SELECT_NAME);
            String html = strObjColHtml.replaceAll("strObjectId", strId).replaceAll("strName", strName);
            JF_LOGGER.info("html:{}", html);
            res.add(html);
        }
        JF_LOGGER.info("res:{}", res);
        return res;
    }

    /**
     * MBOM更新正式版  第二版
     * MBOM 更新逻辑  （采用递归对比的方式）：
     * 1.拉取更新的ECR清单中的root层级的供货件为集合A, 拿取项目中的MBOM为集合B
     * 2.遍历供货件清单A，拿取其企业编码。如果零件有多个版本，不汇总，依次更新
     * 4.如果供货件零件不在MBOM集合B中，需要更新到MBOM（废弃：结构上如果为整椅件且为AA.1版本需要更新到MBOM结构上（解决回滚以后，AA.1版本的初始化问题，其他忽略，继续下一个）；
     * 5.如果供货件零件存在于MBOM集合B中，比对零件和MBOM的版本
     *   5.1. 零件版本>MBOM版本，将零件版本、属性同步到MBOM件上，继续比对子级（零件的子级取ecr中item关系，需要判断ecrId.MBOM子级直接取MBOM结构）
     *       5.1.1. 当零件子级版本=MBOM版本，判断是否是移除，如果是就从MBOM结构中删除，否就忽略
     *       5.1.2. 当零件子级版本不在MBOM中，新增，需要创建并关联MBOM结构
     *       5.1.3. 当MBOM子级版本不在零件清单中，忽略，说明是未变更。
     *       5.1.4. 当零件子级版本>MBOM子级版本，需要更新版本和子级版本，将零件版本、属性同步到MBOM件上
     *       5.1.5. 当零件子级版本<MBOM子级版本，MBOM不变，继续找子级，当有升版的时候，比对MBOM中的版本，当版本大于MBOM版本，将零件版本、属性同步到MBOM件上
     *    5.2. 零件版本<MBOM版本，走5.1.5的逻辑
     *
     *  update  by ljr   20251128
     *  新增逻辑： 生成MBOM的时候仅CAD件及其子级不生成。判断一个件是否仅CAD件需要判断关系属性：VPMInstance 关系属性 SynchroEBOMExt.V_InEBOMApplicative 为TRUE就是仅CAD件
     *           ECR里面涉及到数量变化时，需要考虑更新MBOM里面的实例数量
     *           选择了多个ECR更新，多个ECR含有同一供货件的多个版本，更新MBOM后将更新进同一供货件的多个版本，需要只保留最新版本的供货件结构即可
     *  20260724 update by ljr 新建整椅在线程中构建，所有线程完成后由主线程串行关联公共MBOM根节点
     * @param context
     * @param args
     * @author LIUJR
     * @throws
     * @return java.lang.Boolean
     * @date 2026/7/24 16:41
     * @description
     */
    public Boolean updateMBOMStructureSecond(Context context, String[] args) throws Exception {
        Boolean flag = Boolean.TRUE;
        String user = context.getUser();
        String projectId = EMPTY_STRING;
        DomainObject projectObject = null;
        try {
            ContextUtil.pushContext(context);
            Map paramsMap  = (Map) JPO.unpackArgs(args);
            String ecrIds = UIUtil.getValue(paramsMap, "ecrIdList");
            projectId = UIUtil.getValue(paramsMap, "projectId");
            String createCR = UIUtil.getValue(paramsMap, "createCR");
            String changeDesc = UIUtil.getValue(paramsMap, "changeDesc");
            String[] split = ecrIds.split(",");
            //所属项目
            projectObject = DomainObject.newInstance(context, projectId);
            ThreadLog.info("项目：{}开始更新MBOM结构，更新ECR共{}个, id分别为：{}", projectId + "|" + projectObject.getInfo(context, SELECT_NAME), split.length, ecrIds);
            //拿取项目的所有整椅件   attribute[JFZeroPart]==Y
            StringList projectZeroPartList = JF_PublicMethodClass_mxJPO.getProjectObjectZeroPart(context, projectObject);
            //实例化对象
            DomainObject domainObject = DomainObject.newInstance(context);
            DomainObject ecrObject = DomainObject.newInstance(context);
            //拉取更新的ECR清单中的root层级的供货件为集合A  弄成Map
            //k: ecrId v:partId  保存ecr需要更新的整椅
            HashMap<String, MapList> ecrPartMap = new HashMap<>();
            HashMap<String, Map<String, String>> ecrPartCadMap = new HashMap<>();
            //需要更新的ECRid
            MapList ecrIdMapList = new MapList();
            //bus / rel查询集合
            StringList basicBolistSel = JF_Util_mxJPO.basicBolistSel();
            basicBolistSel.add(JF_PLMConstants_mxJPO.SELECT_ATTR_V_PART_NUMBER);
            basicBolistSel.add("attribute[JF_PartNumber]");
            //20260819 update by ljr 更新场景预取零件类型，按面套和其他类型分别查询属性。
            basicBolistSel.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JFPartType);
            StringList basicRellistSel = JF_Util_mxJPO.basicRellistSel();
            basicRellistSel.add(JF_PLMConstants_mxJPO.SELECT_Attr_SynchroEBOMCAD);
            basicRellistSel.add(SELECT_FROM_ID);
            ThreadLog.info("遍历ECR  将ECR中root件与项目中的整椅件做交集，拿出应该更新的件  和 更新件下的仅CAD件 start...................");
            //遍历ECR  将ECR中root件与项目中的整椅件做交集，拿出应该更新的件  和 更新件下的仅CAD件
            StringList ecrSelList = StringList.create(SELECT_ID, "state[Countersign].actual", SELECT_NAME, SELECT_ATTRIBUTE_TITLE);
            for (int i = 0; i < split.length; i++) {
                //ecrId
                String ecrId = split[i];
                ecrObject.setId(ecrId);
                //获取ECR的会签提升状态
                ecrIdMapList.add(ecrObject.getInfo(context, ecrSelList));
                //获取ecr第一层级的受影响项
                StringList rootItemList = ecrObject.getInfoList(context, "from[" + JF_PLMConstants_mxJPO.REL_JFECRRelateRoot + "].to.id");
                rootItemList.addAll(ecrObject.getInfoList(context, "from[" + JF_PLMConstants_mxJPO.REL_JFECRRelateRootBubble + "].to.id"));
                //将受影响清单和项目供货件做交集，取出需要更新的整椅
                //找到项目中的整椅 做交集
                StringList changePartList = rootItemList.stream()
                        .filter(projectZeroPartList::contains)
                        //20260724 update by ljr 同一ECR中的整椅去重，避免重复线程创建同号整椅MBOM；
                        .distinct()
                        .collect(Collectors.toCollection(StringList::new));
                if (changePartList.isEmpty()) {
                    continue;
                }
                //保存ecr对应需要更新的整椅
                ecrPartMap.put(ecrId, DomainObject.getInfo(context, changePartList.toStringArray(), basicBolistSel));
                //拿取ECR中的需要更新整椅的结构中的仅CAD件
                for (String changePartId : changePartList) {
                    //拿取EBOM中的仅CAD的
                    domainObject.setId(changePartId);
                    MapList childPartList = domainObject.getRelatedObjects(
                            context,
                            JF_PLMConstants_mxJPO.REL_Instance,
                            JF_PLMConstants_mxJPO.TYPE_VPMReference,
                            basicBolistSel,
                            basicRellistSel,
                            false,
                            true,
                            (short) 0,
                            "",
                            "",
                            0
                    );
                    HashMap<String, String> cadMap = new HashMap<>();
                    childPartList.stream().forEach(m -> {
                        Map map = (Map) m;
                        String cad = UIUtil.getValue(map, JF_PLMConstants_mxJPO.SELECT_Attr_SynchroEBOMCAD);
                        String id = UIUtil.getValue(map, SELECT_ID);
                        String fromId = UIUtil.getValue(map, SELECT_FROM_ID);
                        if ("FALSE".equalsIgnoreCase(cad)) {
                            cadMap.put(id,fromId);
                        }
                    });
                    //保存cad件的
                    ecrPartCadMap.put(ecrId + "_" + changePartId, cadMap);
                }
            }
            ThreadLog.info("遍历ECR  将ECR中root件与项目中的整椅件做交集，拿出应该更新的件  和 更新件下的仅CAD件 end .................");
            ThreadLog.info("拿取项目中三个AME的人员信息 将当前现有的MBOM结构保存为JSON串 start .................");
            //拿取项目中三个AME的人员信息
            HashMap<String, String> ownerMap = getProjectAMERolePerson(context, projectId);
            HashSet<String> emailSet = new HashSet<>();   //保存需要发邮件的人员
            emailSet.add(UIUtil.getValue(ownerMap, "U"));
            emailSet.add(UIUtil.getValue(ownerMap, "T"));
            emailSet.add(UIUtil.getValue(ownerMap, "A"));
            //拿取项目中的MBOM为集合B 项目中的MBOM的root节点
            String firstMBOMId = projectObject.getInfo(context, "from["+RELATIONSHIP_JF_relProject2MBOM+"].to.id");
            if (UIUtil.isNullOrEmpty(firstMBOMId)) {
               String projectManager = JF_Util_mxJPO.getProjectManager(context, new String[]{projectId});
                firstMBOMId = createMBOMGX(context, projectObject, projectManager);
            }
            DomainObject firstMBOMObject = DomainObject.newInstance(context, firstMBOMId);
            //构造json文件
            Map<String, String> jsonFile = createChangeRecordMbomJsonFile(context, projectObject, firstMBOMObject, changeDesc);
            //开始更新
            ThreadLog.info("将当前现有的MBOM结构保存为JSON文件,jsonFile地址:{}", jsonFile);
            ThreadLog.info("拿取项目中三个AME的人员信息 将当前现有的MBOM结构保存为JSON串 end .................");
            //遍历需要更新ECR列表
            Map<String, Object> map = new HashMap<>();
            map.put("basicBolistSel", basicBolistSel);
            map.put("firstMBOMId", firstMBOMId);
            map.put("ecrPartCadMap", ecrPartCadMap);
            map.put("ownerMap", ownerMap);
            map.put("projectId", projectId);
            //ECR根据提升到会签的时间进行升序排序 先后顺序
            ThreadLog.info("ECR排序 start .................");
            ecrIdMapList.addSortKey("state[Countersign].actual", ProgramCentralConstants.ASCENDING_SORT, ProgramCentralConstants.SORTTYPE_DATE);
            ecrIdMapList.sort();
            ThreadLog.info("ECR顺序：{}", ecrIdMapList);
            ThreadLog.info("ECR排序 end .................");
            ThreadLog.info("开始遍历ECR 调用线程 更新MBOM结构 start .................");
            Map ecrMap = null;
            String ecrMess = EMPTY_STRING;
            String ecrName = EMPTY_STRING;
            String ecrTitle = EMPTY_STRING;
            String ecrId = EMPTY_STRING;
            StringList ecrIdLists = new StringList();   //保存ecr已经更新的id
            StringList partNameLists = new StringList(); //保存更新了的整椅信息
            HashSet<String> ecrNameAndTitleList = new HashSet<String>();  //保存ECR的信息
            HashSet<String> ecrNameList = new HashSet<String>();   //保存ecr的Name
            MapList rootPartMapList = new MapList();
            for (int i = 0; i < ecrIdMapList.size(); i++) {
                ecrMap = (Map) ecrIdMapList.get(i);
                ecrObject.setId(UIUtil.getValue(ecrMap, SELECT_ID));
                MapList rootConnectList = new MapList();
                boolean transactionStarted = false;
                try {
                    //ECR信息
                    ecrId = UIUtil.getValue(ecrMap, SELECT_ID);
                    ecrName = UIUtil.getValue(ecrMap, SELECT_NAME);
                    ecrTitle = UIUtil.getValue(ecrMap, DomainConstants.SELECT_ATTRIBUTE_TITLE);
                    if (UIUtil.isNullOrEmpty(ecrTitle)) {
                        ecrMess = ecrName;
                    } else {
                        ecrMess = ecrName + ":" + ecrTitle;
                    }
                    //ECR下的需要更新的整椅
                    rootPartMapList = ecrPartMap.get(ecrId);
                    if (rootPartMapList == null || rootPartMapList.isEmpty()) {
                        ThreadLog.info("ECR:{}没有需要更新的整椅，ecrId:{}", ecrName, ecrId);
                        continue;
                    }
                    ThreadLog.info("开始...更新第{}个ECR, ecr名称：{}, 需要更新的整椅数:{}", i+1, ecrName, rootPartMapList.size());
                    if (null != rootPartMapList) {
                        Map<String, Object> processMap = mainProcess(context, rootPartMapList, map, ecrId, "JF_MBOM", "compileChangeAndMbomStructure");
                        rootConnectList = (MapList) processMap.get("rootConnectList");
                        ThreadLog.info("第{}个ECR:{}更新结束，返回值：{}", i+1, ecrName, processMap);
                        ThreadLog.info("需要关联root节点：{}", rootConnectList);
                        if (!"Y".equalsIgnoreCase(String.valueOf(processMap.get("flag")))) {
//                            ThreadLog.info("ECR整椅MBOM线程更新失败，ecrId=" + ecrId);
//                            if (rootConnectList.isEmpty()) {
//
//                            }
                            throw new Exception("ECR整椅MBOM线程更新失败，ecrId=" + ecrId);
                        }
                        partNameLists = (StringList)processMap.get("numberAndRev");
                        ThreadLog.info("ECR:{}更新的整椅名称及版本：{}", ecrName, partNameLists);
                        ContextUtil.startTransaction(context, true);
                        transactionStarted = true;
                        //需要关联的整椅节点
                        if (rootConnectList != null && !rootConnectList.isEmpty()) {
                            ThreadLog.info("ECR更新的整椅关联root节点 start ................ ");
                            //20260724 update by ljr 所有整椅线程完成后，由主线程串行关联公共MBOM根节点，避免并发连接同一rootId；
                            Map latestFirstMbomPartList = getFirstMbomPartList(context, firstMBOMObject, basicBolistSel);
                            Map<String, String> latestMbomMap = (HashMap<String, String>)latestFirstMbomPartList.get("mbomMap");
                            StringList latestRootMbomIdList = firstMBOMObject.getInfoList(context, "from["+ REL_JF_relManufacturedItem +"].to.id");
                            for (int rootIndex = 0; rootIndex < rootConnectList.size(); rootIndex++) {
                                Map rootConnectMap = (Map) rootConnectList.get(rootIndex);
                                String rootMbomId = UIUtil.getValue(rootConnectMap, "rootMbomId");
                                String rootPartNumber = UIUtil.getValue(rootConnectMap, "rootPartNumber");
                                String rootPartRevision = UIUtil.getValue(rootConnectMap, "rootPartRevision");
                                if (UIUtil.isNullOrEmpty(rootMbomId)) {
                                    continue;
                                }
                                if (latestRootMbomIdList.contains(rootMbomId)) {
                                    JF_LOGGER.info("整椅MBOM已关联公共根节点，跳过重复关联，rootMbomId:{}", rootMbomId);
                                    continue;
                                }
                                if (latestMbomMap.containsKey(rootPartNumber)) {
                                    String existingMbomIdAndRev = latestMbomMap.get(rootPartNumber);
                                    if (UIUtil.isNotNullAndNotEmpty(existingMbomIdAndRev)) {
                                        String existingMbomRev = existingMbomIdAndRev.split("_")[1];
                                        if (rootPartRevision.equalsIgnoreCase(existingMbomRev)) {
                                            continue;
                                        }
                                    }
                                }
                                connectionMBOM(context, firstMBOMId, rootMbomId, "1");
                                latestRootMbomIdList.add(rootMbomId);
                                latestMbomMap.put(rootPartNumber, rootMbomId + "_" + rootPartRevision);
                            }
                            ThreadLog.info("ECR更新的整椅关联root节点 end ................ ");
                        }
                        ThreadLog.info("ECR更新的整椅关联root节点 start ................ ");
                        ecrObject.setAttributeValue(context, ATTR_JF_IsSyncMBOM, "Yes");
                        ContextUtil.commitTransaction(context);
                        transactionStarted = false;
                        ecrNameAndTitleList.add(ecrMess);
                        ecrIdLists.add(ecrId);
                        ecrNameList.add(ecrName);
                    }
                    ThreadLog.info("结束...更新第{}个ECR, ecr名称：{}, 需要更新的整椅数:{},更新的整椅名称及版本：{}", i+1, ecrName, rootPartMapList.size(), partNameLists);
                }catch (Exception e) {
                    ThreadLog.info("ECR:{}更新报错:{} .................", ecrName, Arrays.toString(e.getStackTrace()));
                    if (transactionStarted) {
                        ContextUtil.abortTransaction(context);
                    }

                }
            }
            ThreadLog.info("结束遍历ECR 调用线程 更新MBOM结构 end .................");
            String changeRecordId = EMPTY_STRING;
            ThreadLog.info("更新MBOM结构完成  开始创建变更记录 start ...................");
            ThreadLog.info("更新的ECR:{}", ecrNameAndTitleList);
            if (!ecrNameAndTitleList.isEmpty()) {
                //如果有更新才发送邮件和创建更新记录
                firstMBOMObject.setAttributeValue(context, "JF_PMChangeDesc", changeDesc);
                if ("Y".equalsIgnoreCase(createCR)) {
                    changeRecordId = createChangeRecord(context, StringList.create(ecrNameAndTitleList), projectId, ecrIdLists, user);
                    domainObject.setId(changeRecordId);
                    domainObject.setDescription(context, changeDesc);
                    domainObject.checkinFile(context, false, true, "", "generic", jsonFile.get("fileName"), UIUtil.getValue(jsonFile, "filePath"));
                    //将权限给AME
                    firstMBOMObject.setAttributeValue(context, "JF_Responsible", "AME");
                    //发送邮件
                    sendResponsibleEmail(context, projectId, "AME");
                }
            }
            ThreadLog.info("更新MBOM结构完成  结束创建变更记录 end ...................");
            if (UIUtil.isNotNullAndNotEmpty(changeRecordId)) {
                //创建xlsx表
                //导出并checkin文件
                Job job = new Job("JF_MBOM","exportSyncMBOMExcelInvoke",new String[]{projectId,changeRecordId});
                job.setTitle("export Sync MBOMExcel Invoke");
                job.createAndSubmit(context);
            }
        } catch (Exception e) {
            ThreadLog.info("本次更新日志:{}", Arrays.toString(e.getStackTrace()));
            e.printStackTrace();
            flag = Boolean.FALSE;
        } finally {
            try {
                projectObject = DomainObject.newInstance(context, projectId);
                //20260724 update by ljr 更新MBOM的JPO执行结束或发生异常时释放项目更新标识，JSP保留重复释放作为双保险；
                projectObject.setAttributeValue(context, "JF_MBOMUpdateAllow", "Y");
            } catch (Exception releaseException) {
                flag = Boolean.FALSE;
                JF_LOGGER.error("JPO release JF_MBOMUpdateAllow error, projectId:{}", projectId, releaseException);
            } finally {
                ContextUtil.popContext(context);
            }
        }
        JF_LOGGER.info("flag:{}", flag);
        return flag;
    }

    /**
     * MBOM更新子线程调用方法；新建整椅MBOM时返回整椅ID，由主线程统一关联公共根节点
     * @param context
     * @param args
     * @author LIUJR
     * @throws Exception
     * @return java.util.Map<java.lang.String,java.lang.String>
     * @date 2026/7/24 16:00
     * @description
     */
    public Map<String, String> compileChangeAndMbomStructure(Context context, String[] args) throws Exception{
        Map rootPartMap = null;
        Map firstMbomPartList = null;
        Map<String, String> mbomMap = null;
        StringList partNameLists = new StringList(); //保存更新了的整椅信息
        Map<String, String> partCadMap= null;
        HashMap<String, Map<String, String>> ecrPartCadMap = new HashMap<>();
        HashMap<String, String> ownerMap = new HashMap<>();
        DomainObject rootPartObject = null;
        Map<String, String> returnMap = new HashMap<>();
        String ecrId = "";
        DomainObject ecrObject = null;
        boolean transactionStarted = false;
        returnMap.put("flag", "N");
        returnMap.put("numberAndRev", EMPTY_STRING);
        returnMap.put("rootMbomId", EMPTY_STRING);
        returnMap.put("rootPartNumber", EMPTY_STRING);
        returnMap.put("rootPartRevision", EMPTY_STRING);
        returnMap.put("needConnectRoot", "N");
        try {
            ContextUtil.startTransaction(context, true);
            transactionStarted = true;
            Map paramsMap = (Map) JPO.unpackArgs(args);
            DomainObject partObj = DomainObject.newInstance(context);
            DomainObject mbomObj = DomainObject.newInstance(context);
            DomainObject firstMBOMObject = DomainObject.newInstance(context);
            rootPartMap = (Map) paramsMap.get("rootPartMap");
            ecrId = (String) paramsMap.get("ecrId");
            ecrObject = DomainObject.newInstance(context, ecrId);
            ownerMap = (HashMap) paramsMap.get("ownerMap");
            ecrPartCadMap = (HashMap) paramsMap.get("ecrPartCadMap");
            String firstMBOMId = (String) paramsMap.get("firstMBOMId");
            String projectId = (String) paramsMap.get("projectId");
            firstMBOMObject.setId(firstMBOMId);
            StringList basicBolistSel = (StringList) paramsMap.get("basicBolistSel");
            rootPartObject = DomainObject.newInstance(context, UIUtil.getValue(rootPartMap, SELECT_ID));
            ThreadLog.info("子线程 开始更新,ecr名称:{},part:{}..................",ecrObject.getInfoList(context, SELECT_NAME), rootPartObject.getInfo(context, SELECT_NAME));
            //获取MBOM下的整椅子节点   每一个ECR提交都需要查询一遍
            firstMbomPartList = getFirstMbomPartList(context, firstMBOMObject, basicBolistSel);
            mbomMap = (HashMap<String, String>)firstMbomPartList.get("mbomMap");
//            JF_LOGGER.info("MBOM下的整椅子节点:firstMbomPartList:{}", firstMbomPartList);
            String number = UIUtil.getValue(rootPartMap, JF_PLMConstants_mxJPO.SELECT_ATTR_V_PART_NUMBER);
            String revision = UIUtil.getValue(rootPartMap, SELECT_REVISION);
            returnMap.put("rootPartNumber", number);
            returnMap.put("rootPartRevision", revision);
            ThreadLog.info("开始 更新结构,number:{},revision:{} .................", number, revision);
            String id = UIUtil.getValue(rootPartMap, SELECT_ID);
            //当前总成下 仅cad件 和其父级    cadid=父级id
            partCadMap = ecrPartCadMap.get(ecrId + "_" + id);
            JF_LOGGER.info("mbomMap.containsKey(number):{}", mbomMap.containsKey(number));
            //现有MBOM是否包含
            //1.不包含，需要将当前版本得零件更新到MBOM结构上 -> 调用初始化的程序
            if (!mbomMap.containsKey(number)) {
                MapList newRootMbomList = initECRMBOM(context, ownerMap, ecrId, projectId, new StringList(id), firstMBOMId, partCadMap, false);
                if (newRootMbomList.isEmpty()) {
                    throw new Exception("创建整椅MBOM失败，未返回整椅MBOM信息，partId=" + id);
                }
                Map newRootMbomMap = (Map) newRootMbomList.get(0);
                String rootMbomId = UIUtil.getValue(newRootMbomMap, SELECT_ID);
                if (UIUtil.isNullOrEmpty(rootMbomId)) {
                    throw new Exception("创建整椅MBOM失败，未返回整椅MBOM ID，partId=" + id);
                }
                //20260724 update by ljr 新建整椅MBOM返回主线程，所有线程完成后再串行关联公共根节点；
                returnMap.put(SELECT_ID, rootMbomId);
                returnMap.put("rootMbomId", rootMbomId);
                returnMap.put("needConnectRoot", "Y");
                JF_LOGGER.info("调用初始化的程序");
            } else {
                //2.包含，需要比对版本 -> 如果供货件零件存在于MBOM集合B中，比对零件和MBOM的版本
                String mbomIdAndRev = mbomMap.get(number);  //获取MBOM的版本 和id
                JF_LOGGER.info("如果dddd供货件零件存在于MBOM集合B中，比对零件和MBOM的版本:{}");
                String[] mbomSplit = mbomIdAndRev.split("_");
                String mbomId = mbomSplit[0];
                String mbomRev = mbomSplit[1];
                returnMap.put("rootMbomId", mbomId);
                mbomObj.setId(mbomId);
                partObj.setId(id);
//            mbomRev = MBOM_VERSION_CACHE.containsKey(mbomId) ? MBOM_VERSION_CACHE.get(mbomId): mbomRev;
                String maxRevision = JF_Util_mxJPO.getMaxRevision(context, new String[]{revision, mbomRev});
                //2.1 版本相同时不处理升版和结构递归，仅刷新项目维度的DirectBuy。
                if (revision.equalsIgnoreCase(mbomRev)) {
                    //20260727 update by ljr 第一层级版本相同时仍同步关系DirectBuy，未维护客户零件/DB信息时写入non-DB；
                    String directBuy = JF_Util_mxJPO.getPartDirectBuyByProjectWithNonDBDefault(
                            context,
                            id,
                            projectId);
                    mbomObj.setAttributeValue(context, Attr_JF_DirectBuy, directBuy);
                } else if (revision.equalsIgnoreCase(maxRevision)) {
                    //零件版本>MBOM版本，将零件版本、属性同步到MBOM件上，继续比对子级（零件的子级取ecr中item关系，需要判断ecrId.MBOM子级直接取MBOM结构）
                    String partType = UIUtil.getValue(rootPartMap, JF_PLMConstants_mxJPO.SELECT_ATTR_JFPartType);
                    Map<String, String> partValue = JF_PLMConstants_mxJPO.ATTR_JFPartType_RANGE_T.equalsIgnoreCase(partType)
                            ? getTrimPartAttributeValues(context, id, null)
                            : partObj.getInfo(context, partAttribute);
                    //20260727 update by ljr MBOM更新时，未维护项目对应客户零件/DB信息的零件按non-DB同步；
                    partValue.put(JF_PLMConstants_mxJPO.Attr_JF_DirectBuy,
                            JF_Util_mxJPO.getPartDirectBuyByProjectWithNonDBDefault(
                                    context,
                                    id,
                                    projectId));
                    JF_LOGGER.info("MBOM属性同步");
                    //20260819 update by ljr 面套供货件升版调用独立属性转换方法，其他类型保留原公共逻辑；
                    if (JF_PLMConstants_mxJPO.ATTR_JFPartType_RANGE_T.equalsIgnoreCase(partType)) {
                        mbomObj.setAttributeValues(context, changeTrimAttribute(context, partValue));
                        setTrimRootMbomDosageAttribute(context, mbomObj, partValue);
                    } else {
                        mbomObj.setAttributeValues(context, changeAttribute(context, partValue));
                    }
                    //修改MBOM版本
//                    ThreadLog.info("ECR:{}", ecrObject.getInfo(context, SELECT_NAME));
//                    ThreadLog.info("整椅： partnumber:{}, revision:{}, mbomRev:{}", number, revision, mbomRev);
                    String mql = modNameRevMql.replace("Id", mbomId).replace("N", mbomObj.getInfo(context, SELECT_NAME)).replace("R", revision);
                    JF_LOGGER.info("mql:{}", mql);
                    MqlUtil.mqlCommand(context, false, mql, true);
//                MBOM_VERSION_CACHE.put(mbomId, revision);
                    updateMbomStrctureRecursion(context, ownerMap, ecrId, projectId, mbomObj, partObj, basicBolistSel, partCadMap);
                } else if (mbomRev.equalsIgnoreCase(maxRevision)) {
                    //当零件版本<MBOM版本，MBOM不变，继续找子级，当有升版的时候，比对MBOM中的版本，当版本大于MBOM版本，将零件版本、属性同步到MBOM件上
                    updateMbomStrctureRevision(context, ownerMap, ecrId, projectId, mbomObj, partObj, basicBolistSel, partCadMap);
                }
            }
            //如果更新了MBOM结构，保存name
            returnMap.put("flag", "Y");
            returnMap.put("numberAndRev", number + "_" + revision);
            ContextUtil.commitTransaction(context);
            transactionStarted = false;
        }catch (Exception e) {
            if (transactionStarted) {
                ContextUtil.abortTransaction(context);
            }
            ThreadLog.info("子线程异常:{},ECR:{}", e.getMessage(), ecrId);
            ThreadLog.info("子线程异常堆栈:{}", Arrays.toString(e.getStackTrace()));
            e.printStackTrace();
            throw e;
        }
        JF_LOGGER.info("子线程  结束,ecr:{}...................",ecrObject.getInfo(context, SELECT_NAME));
        return returnMap;
    }

    /**
     * 获取MBOM下的整椅结构
     * @param context
     * @param firstMBOMObject
     * @param basicBolistSel
     * @author LIUJR
     * @throws
     * @return java.util.Map
     * @date 2025/11/28 15:14
     * @description
     */
    public static Map getFirstMbomPartList(Context context, DomainObject firstMBOMObject, StringList basicBolistSel) throws Exception{
        HashMap<String, Object> returnMap = new HashMap<>();
        try {
            //获取现有MBOM的root下的整椅MBOM
            StringList mbomRootList = firstMBOMObject.getInfoList(context, "from["+ REL_JF_relManufacturedItem +"].to.id");
            //查询MBOM整椅节点的信息
            MapList rootMBOMMapList = DomainObject.getInfo(context, mbomRootList.toStringArray(), basicBolistSel);
//            JF_LOGGER.info("rootMBOMMapList:{}", rootMBOMMapList);
            //将MBOM整椅节点的信息 保存起来
            HashMap<String, String> mbomMap = new HashMap<>();   //保存PartNumber号对应的版本和id
            StringList mbomNumberList = new StringList();  //保存PartNumber号
            rootMBOMMapList.stream().forEach(m -> {
                Map map = (Map) m;
                String number = UIUtil.getValue(map, "attribute[JF_PartNumber]");
                String revision = UIUtil.getValue(map, SELECT_REVISION);
                String id = UIUtil.getValue(map, SELECT_ID);
                mbomMap.put(number, id + "_" + revision);
                mbomNumberList.add(number);
            });
            returnMap.put("mbomMap", mbomMap);
            returnMap.put("mbomNumberList", mbomNumberList);
        } catch (FrameworkException e) {
            e.printStackTrace();
            throw e;
        }
        return returnMap;
    }

    /**
     * job 调用MBOM的生成整椅，发泡，面套的EXCEL表
     * @param context
     * @param args
     * @author LIUJR
     * @throws
     * @return void
     * @date 2025/11/27 17:49
     * @description
     */
    public void exportSyncMBOMExcelInvoke(Context context, String[] args) {
        try {
            String projectId = args[0];
            String changeRecordId = args[1];
            JF_ExportMBOM_mxJPO jfExportMBOMMxJPO = new JF_ExportMBOM_mxJPO();
            jfExportMBOMMxJPO.exporSyncMBOMExcel(context,projectId,DomainObject.newInstance(context, changeRecordId));
        } catch (Exception e) {
            e.printStackTrace();
            JF_LOGGER.info(e.getMessage());
        }
    }

    /**
     * 更新MBOM时创建json文件
     * @param context
     * @param firstMBOMObject
     * @author LIUJR
     * @throws
     * @return java.util.Map<java.lang.String,java.lang.String>
     * @date 2025/10/31 14:53
     * @description
     */
    public static Map<String, String> createChangeRecordMbomJsonFile(Context context, DomainObject projectObject, DomainObject firstMBOMObject, String changeMess) throws Exception{
        Map<String, String> returnMap = new HashMap<>();
        JF_LOGGER.info("!!!!!!!更新MBOM时创建json文件!!!!!!!!!!!!!!!!!!");
        try {
            StringList mbomSelList = new StringList();
            mbomSelList.add(SELECT_CURRENT);
            mbomSelList.add(SELECT_PROJECT);
            mbomSelList.add(SELECT_ORGANIZATION);
            mbomSelList.add(SELECT_REVISION);
            mbomSelList.add(SELECT_OWNER);
            mbomSelList.add(SELECT_TYPE);
            mbomSelList.add("attribute[JF_PartNumber]");
            mbomSelList.add("attribute[JF_PartNameEN]");
            mbomSelList.add("attribute[JF_PartNameCN]");
            mbomSelList.add("attribute[Title]");
            mbomSelList.add("attribute[JF_PartType]");
            mbomSelList.add("attribute[JF_ProcurementType]");
            mbomSelList.add("attribute[JF_SpecialProcurementType]");
            mbomSelList.add("attribute[JF_DirectBuy]");
            mbomSelList.add("attribute[JF_Unit]");
            mbomSelList.add("attribute[JF_PartDes]");
            mbomSelList.add("attribute[JF_PartENDes]");
            mbomSelList.add("attribute[JF_Width]");
            mbomSelList.add("attribute[JF_Utilizationrate]");
            StringList mbomRelList = JF_Util_mxJPO.basicRellistSel();
            mbomRelList.add("attribute[JF_Dosage]");
            mbomRelList.add("attribute[JF_OriginateFlag]");
            StringList jsonKeyList = new StringList();
            jsonKeyList.addAll(mbomSelList);
            jsonKeyList.addAll(mbomRelList);
            mbomSelList.add(SELECT_ID);
            MapList mapList = firstMBOMObject.getRelatedObjects(
                    context,
                    REL_JF_relManufacturedItem,
                    TYPE_JF_ManufacturedItem,
                    mbomSelList,
                    mbomRelList,
                    false, true,
                    (short) 1,
                    "",
                    "",
                    0
            );
            JSONObject json = new JSONObject();
            JSONArray partsArray = new JSONArray();
            JSONArray mbomsArray = new JSONArray();
            String projectId = projectObject.getInfo(context, SELECT_ID);
            HashSet<String> hashSet = new HashSet<>();
            for (int i = 0; i < mapList.size(); i++) {
                Map map = (Map) mapList.get(i);
                //结构
                JSONObject mbomJson = new JSONObject();
                getMBOMItemArray(context, map, partsArray, mbomJson, mbomSelList, mbomRelList, jsonKeyList, projectId, hashSet);
                mbomsArray.add(mbomJson);
            }
            json.put("Parts", partsArray);
            json.put("MBOM", mbomsArray);
            //firstMbom节点要 保存其属性和状态
            JSONObject itemJson = new JSONObject();
            StringList firstSelList = StringList.create("attribute[JF_PMChangeDesc]", "attribute[JF_AMEChangeDesc]", "attribute[JF_Responsible]");
            mbomSelList.addAll(firstSelList);
            jsonKeyList.addAll(firstSelList);
            Map infoMap= firstMBOMObject.getInfo(context, mbomSelList);
            for (int i = 0; i < jsonKeyList.size(); i++) {
                String strKey = jsonKeyList.get(i);
                String strValue = UIUtil.getValue(infoMap, strKey);
                if (strKey.contains("attribute")) {
                    int startIndex = strKey.indexOf('[');
                    int endIndex = strKey.indexOf(']');
                    strKey = strKey.substring(startIndex + 1, endIndex);
                }
                itemJson.put(strKey, strValue);
            }
            json.put("firstMbom", itemJson);
            String filePath = context.createWorkspace()+ File.separator;
            //前台时间转换为数据库时间
            LocalDateTime now = LocalDateTime.now();
            // 格式化整个日期时间字符串
            String formattedDateTime = now.format(formatterDate);
            String fileName = projectObject.getInfo(context, SELECT_NAME) + "-" + changeMess + "-" + formattedDateTime +  ".json";
            String strFilePath = filePath  +  fileName;
            try (FileWriter fileWriter = new FileWriter(strFilePath)) {
                fileWriter.write(json.toJSONString());
                JF_LOGGER.info("JSON数据已成功写入文件。");
            } catch (IOException e) {
                e.printStackTrace();
                JF_LOGGER.info("写入文件时发生错误。");
            }
            returnMap.put("fileName", fileName);
            returnMap.put("filePath", filePath);
            JF_LOGGER.info("returnMap:{}", returnMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
        return returnMap;
    }

    /**
     * 遍历MBOM一层节点树结构
     * @param context
     * @param map
     * @param mbomSelList
     * @param mbomRelList
     * @author LIUJR
     * @throws
     * @return void
     * @date 2024/7/22 13:45
     * @description
     */
    private static void getMBOMItemArray(Context context, Map map, JSONArray partsArray,JSONObject itemJson, StringList mbomSelList, StringList mbomRelList, StringList jsonKeyList, String projectId, HashSet hashSet) throws Exception{
        try {
            //属性
            String strCParentID = UIUtil.getValue(map, "attribute[JF_PartNumber]");
            String strCrevision = UIUtil.getValue(map, SELECT_REVISION);
            if (!hashSet.contains(strCParentID + "_" + strCrevision)) {
                JSONObject partJson = new JSONObject();
                for (int i1 = 0; i1 < jsonKeyList.size(); i1++) {
                    String strKey = jsonKeyList.get(i1);
                    String strValue = UIUtil.getValue(map, strKey);
                    if (strKey.contains("attribute")) {
                        int startIndex = strKey.indexOf('[');
                        int endIndex = strKey.indexOf(']');
                        strKey = strKey.substring(startIndex + 1, endIndex);
                    }
                    partJson.put(strKey, strValue);
                }
                partsArray.add(partJson);
                hashSet.add(strCParentID + "_" + strCrevision);
            }
//            JF_LOGGER.info("hashSet:{}", hashSet);
            //结构
            itemJson.put("JF_PartNumber", strCParentID);
            itemJson.put("revision", strCrevision);
            itemJson.put("Dosage", UIUtil.getValue(map, "attribute[JF_Dosage]"));
            itemJson.put("JF_OriginateFlag", UIUtil.getValue(map, "attribute[JF_OriginateFlag]"));
            //拿取child node
            String mbomId = UIUtil.getValue(map, SELECT_ID);
            DomainObject domainObject = DomainObject.newInstance(context, mbomId);
            MapList mapList = domainObject.getRelatedObjects(
                    context,
                    REL_JF_relManufacturedItem,
                    TYPE_JF_ManufacturedItem,
                    mbomSelList,
                    mbomRelList,
                    false,
                    true,
                    (short) 1,
                    "",
                    "",
                    0
            );
            JSONArray childArray = new JSONArray();
            if (mapList.size() > 0) {
                Iterator iterator = mapList.iterator();
                while (iterator.hasNext()) {
                    Map childMap = (Map) iterator.next();
                    JSONObject childItemJson = new JSONObject();
                    getMBOMItemArray(context, childMap, partsArray, childItemJson, mbomSelList, mbomRelList, jsonKeyList, projectId, hashSet);
                    childArray.add(childItemJson);
                }
            }

            itemJson.put("child", childArray);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * 递归方法 更新MBOM
     *   5.1. 零件版本>MBOM版本，继续比对子级（零件的子级取ecr中item关系，需要判断ecrId.MBOM子级直接取MBOM结构）
     *       5.1.1. 当零件子级版本=MBOM版本，判断是否是移除，如果是就从MBOM结构中删除，否就忽略
     *       5.1.2. 当零件子级版本不在MBOM中，新增，需要创建并关联MBOM结构
     *       5.1.3. 当MBOM子级版本不在零件清单中，忽略，说明是未变更。
     *       5.1.4. 当零件子级版本>MBOM子级版本，需要更新版本和子级版本，将零件版本、属性同步到MBOM件上
     *       5.1.5. 当零件子级版本<MBOM子级版本，MBOM不变，继续找子级，当有升版的时候，比对MBOM中的版本，当版本大于MBOM版本，将零件版本、属性同步到MBOM件上
     *    5.2. 零件版本<MBOM版本，走5.1.5的逻辑
     * @param context
     * @param ownerMap  项目中AME 人员
     * @param ecrId  ecrId
     * @param projectId   项目id
     * @param mbomObject  父级MBOM对象
     * @param partObject  父级ebom对象
     * @param stringList  查询列表
     * @param partCadMap    邮件
     * @author LIUJR
     * @throws
     * @return void
     * @date 2025/8/14 14:33
     * @description
     */
    public void updateMbomStrctureRecursion(Context context, HashMap<String, String> ownerMap, String ecrId, String projectId, DomainObject mbomObject,
                                            DomainObject partObject, StringList stringList,
                                            Map<String, String> partCadMap) throws Exception{
        //如果供货件零件不在MBOM集合B中，需要更新到MBOM
        //拿取rootPartList中的企业编码和
        JF_LOGGER.info("updateMbomStrctureRecursion:{}");
        StringList relList = new StringList();
        relList.add(SELECT_ATTRIBUTE_JF_ECRID);
        relList.add(DomainRelationship.SELECT_ID);
        relList.add(SELECT_ATTRIBUTE_JF_BOMChangeDes);
        relList.add(SELECT_ATTRIBUTE_JF_BOMQuantity);  //数量
        relList.add(SELECT_FROM_ID);
//        ThreadLog.info("子线程-大于比对:part: {},mbom:{}",
//                partObject.getAttributeValue(context, JF_PLMConstants_mxJPO.SELECT_ATTR_V_PART_NUMBER) + partObject.getInfo(context, SELECT_REVISION),
//                mbomObject.getAttributeValue(context, "JF_PartNumber") + mbomObject.getInfo(context, SELECT_REVISION));
        try {
            //ECR受影响清单结构
            MapList rootPartMapList = (MapList) partObject.getRelatedObjects(context,
                    JF_PLMConstants_mxJPO.REL_JFECRRoot2Item + "," + JF_PLMConstants_mxJPO.REL_JFECRRoot2ItemBubble, // relationship pattern
                    JF_PLMConstants_mxJPO.TYPE_VPMReference,                                    // object pattern
                    stringList,                            // object selects
                    relList, // relationship selects
                    false,                                        // to direction
                    true,                                        // from direction
                    (short) 1,                                    // recursion level
                    "",                // object where clause
                    "",
                    (short) 0);
            Map mapListGroupingMap = getPartEbomStructure(context, partObject, relList, stringList, 1);
//            JF_LOGGER.info("mapListGroupingMap:{}", mapListGroupingMap);
            //MBOM结构 信息
            MapList rootMBOMMapList = (MapList) mbomObject.getRelatedObjects(context,
                    JF_PLMConstants_mxJPO.REL_JF_relManufacturedItem, // relationship pattern
                    JF_PLMConstants_mxJPO.TYPE_JF_ManufacturedItem,                                    // object pattern
                    stringList,                            // object selects
                    relList, // relationship selects
                    false,                                        // to direction
                    true,                                        // from direction
                    (short) 1,                                    // recursion level
                    "",                // object where clause
                    "",
                    (short) 0);
            HashMap<String, MapList> mbomMap = new HashMap<>();
            //20260819 update by ljr 使用HashSet索引当前层MBOM零件号，避免循环内线性contains查询。
            Set<String> mbomNumberSet = new HashSet<>();
            //可能有多个
//            String key = EMPTY_STRING;
            String number = EMPTY_STRING;
            String revision = EMPTY_STRING;
            MapList mapList = new MapList();
            for (int i = 0; i < rootMBOMMapList.size(); i++) {
                Map map = (Map) rootMBOMMapList.get(i);
                number = UIUtil.getValue(map, "attribute[JF_PartNumber]");
                revision = UIUtil.getValue(map, SELECT_REVISION);
                mapList = new MapList();
                mbomNumberSet.add(number);
                if (mbomMap.containsKey(number)) {
                    mapList = mbomMap.get(number);
                }
                mapList.add(map);
                mbomMap.put(number, mapList);
            }
            DomainObject partObj = DomainObject.newInstance(context);
            DomainObject mbomObj = DomainObject.newInstance(context);
//            JF_LOGGER.info("rootPartMapList:{}", rootPartMapList);
//            JF_LOGGER.info("mbomMap:{}", mbomMap);
//            JF_LOGGER.info("mbomNumberSet:{}", mbomNumberSet);
            String ecrIds = EMPTY_STRING;
            String changeDes = EMPTY_STRING;
            Integer bomQuantity = 0;
            String id = EMPTY_STRING;
            String parentId = EMPTY_STRING;
            String fromId = EMPTY_STRING;
            for (int i1 = 0; i1 < rootPartMapList.size(); i1++) {
                Map map = (Map) rootPartMapList.get(i1);
                JF_LOGGER.info("==================");
                JF_LOGGER.info("map:{}", map);
                //判断是否是这个ecr的结构
                ecrIds = UIUtil.getValue(map, SELECT_ATTRIBUTE_JF_ECRID);
                JF_LOGGER.info("changeDes:{}", changeDes);
                if (!ecrIds.contains(ecrId)) {
                    continue;
                }
                changeDes = UIUtil.getValue(map, SELECT_ATTRIBUTE_JF_BOMChangeDes);
                bomQuantity = Integer.valueOf(UIUtil.getValue(map, SELECT_ATTRIBUTE_JF_BOMQuantity));
                number = UIUtil.getValue(map, JF_PLMConstants_mxJPO.SELECT_ATTR_V_PART_NUMBER);
                revision = UIUtil.getValue(map, SELECT_REVISION);
                id = UIUtil.getValue(map, SELECT_ID);
                parentId = UIUtil.getValue(map, SELECT_FROM_ID);
                //生成MBOM的时候仅CAD件及其子级不生成。判断一个件是否仅CAD件需要判断关系属性：VPMInstance 关系属性 SynchroEBOMExt.V_InEBOMApplicative 为TRUE就是仅CAD件
                if (partCadMap.containsKey(id)) {
                    //仅cad件  不生成
                    fromId = partCadMap.get(id);
                    if (parentId.equalsIgnoreCase(fromId)) {
                        continue;
                    }
                }
                /*
                 * 5.1.1. 当零件子级版本=MBOM版本，判断是否是移除，如果是就从MBOM结构中删除，否就忽略
                 * 5.1.2. 当零件子级版本不在MBOM中，新增，需要创建并关联MBOM结构
                 * 5.1.3. 当MBOM子级版本不在零件清单中，忽略，说明是未变更。  //
                 * 5.1.4. 当零件子级版本>MBOM子级版本，需要更新版本和子级版本，将零件版本、属性同步到MBOM件上
                 * 5.1.5. 当零件子级版本<MBOM子级版本，MBOM不变，继续找子级，当有升版的时候，比对MBOM中的版本，当版本大于MBOM版本，将零件版本、属性同步到MBOM件上
                 * */
                JF_LOGGER.info("number:{}", number);
                //肯定不包含
                if (mbomNumberSet.contains(number)) {
                    mapList = mbomMap.get(number);
                    JF_LOGGER.info("mapList:{}", mapList);
                    Map mbomIdAndRev = (Map) mapList.get(0);
                    JF_LOGGER.info("mbomIdAndRev:{}", mbomIdAndRev);
                    String mbomRev = UIUtil.getValue(mbomIdAndRev, SELECT_REVISION);
                    String mbomId = UIUtil.getValue(mbomIdAndRev, SELECT_ID);
                    String mbomConnId = UIUtil.getValue(mbomIdAndRev, DomainRelationship.SELECT_ID);
                    mbomObj.setId(mbomId);
                    partObj.setId(id);
//                    mbomRev = MBOM_VERSION_CACHE.containsKey(mbomId) ? MBOM_VERSION_CACHE.get(mbomId): mbomRev;
                    String maxRevision = JF_Util_mxJPO.getMaxRevision(context, new String[]{revision, mbomRev});
                    //比对版本
                    //if (revision.compareTo(mbomRev) > 0) {
                    if (revision.equalsIgnoreCase(maxRevision)) {
                        JF_LOGGER.info("bomQuantity:{}", bomQuantity);
                        if (bomQuantity > mapList.size()) {
                            //比对ECR上的数量 和 MBOM的结构数量
                            //新增结构
                            JF_LOGGER.info("bomQuantity:{}", bomQuantity);
                            JF_LOGGER.info("mapList.size():{}", mapList.size());
                            JF_LOGGER.info("mapList:{}", mapList);
                            JF_LOGGER.info("Math.abs(bomQuantity - mapList.size():{}", Math.abs(bomQuantity - mapList.size()));
                            Integer num = Math.abs(bomQuantity - mapList.size());
                            for (int i = 0; i < num; i++) {
                                JF_LOGGER.info("bomQuantity:{}, i:{}", bomQuantity, i);
                                mapList.addAll(initECRMBOM(context, ownerMap, ecrId, projectId, new StringList(id), mbomObject.getId(context), partCadMap, true));
                            }
                        } else if (bomQuantity < mapList.size()) {
                            //移除
                            int start = mapList.size() - Math.abs(bomQuantity - mapList.size());
                            MapList result = new MapList();
                            result.addAll(mapList.subList(start, mapList.size()));
                            deleteMbomObject(context, result);
                            result.clear();
                            result.addAll(mapList.subList(0,start));
                            mapList = result;
                            if (mapList.isEmpty()) {
                                mbomNumberSet.remove(number);
                            }
                        }
                        String partType = UIUtil.getValue(map, JF_PLMConstants_mxJPO.SELECT_ATTR_JFPartType);
                        boolean isTrimPart = JF_PLMConstants_mxJPO.ATTR_JFPartType_RANGE_T.equalsIgnoreCase(partType);
                        //20260819 update by ljr 同一EBOM零件存在多个MBOM实例时，属性和DirectBuy只查询一次。
                        Map<String, String> partValue = isTrimPart
                                ? getTrimPartAttributeValues(context, id, null)
                                : partObj.getInfo(context, partAttribute);
                        partValue.put(JF_PLMConstants_mxJPO.Attr_JF_DirectBuy,
                                JF_Util_mxJPO.getPartDirectBuyByProjectWithNonDBDefault(
                                        context, id, projectId));
                        Map<String, String> mbomAttributeValue = isTrimPart
                                ? changeTrimAttribute(context, partValue)
                                : changeAttribute(context, partValue);
                        String rev = UIUtil.getValue(partValue, SELECT_REVISION);
                        for (int i2 = 0; i2 < mapList.size(); i2++) {
                            mbomIdAndRev = (Map) mapList.get(i2);
                            JF_LOGGER.info("mbomIdAndRev:{}", mbomIdAndRev);
                            mbomRev = UIUtil.getValue(mbomIdAndRev, SELECT_REVISION);
                            mbomId = UIUtil.getValue(mbomIdAndRev, SELECT_ID);
                            mbomConnId = UIUtil.getValue(mbomIdAndRev, DomainRelationship.SELECT_ID);
                            mbomObj.setId(mbomId);
                            JF_LOGGER.info("当零件子级版本>MBOM子级版本:{}");
                            //5.1.4. 当零件子级版本>MBOM子级版本，需要更新版本和子级版本，将零件版本、属性同步到MBOM件上
                            JF_LOGGER.info("MBOM属性同步");
                            JF_LOGGER.info("id:{}", id);
                            JF_LOGGER.info("mbomId:{}", mbomId);
                            JF_LOGGER.info("mbomIdAndRev:{}", mbomIdAndRev);
                            //20260819 update by ljr 面套子级升版调用独立属性和用量转换方法；
                            if (isTrimPart) {
                                mbomObj.setAttributeValues(context, mbomAttributeValue);
                                setTrimMbomDosageAttribute(
                                        context, mbomConnId, id, mapListGroupingMap, partValue);
                            } else {
                                mbomObj.setAttributeValues(context, mbomAttributeValue);
                                setMbomDosageAttribute(context, mbomConnId, id, mapListGroupingMap);
                            }
                            //修改MBOM版本
                            String mql = modNameRevMql.replace("Id", mbomId).replace("N", mbomObj.getInfo(context, SELECT_NAME)).replace("R", rev);
                            MqlUtil.mqlCommand(context, false, mql, true);
//                            MBOM_VERSION_CACHE.put(mbomId, rev);
                            //比对ECR上的数量 和 MBOM的结构数量 升版新增和移除
                            JF_LOGGER.info("mql:{}", mql);
                            //设置转换之后的值
                            updateMbomStrctureRecursion(context, ownerMap, ecrId, projectId, mbomObj, partObj, stringList, partCadMap);
                        }
                    } else if (revision.equalsIgnoreCase(mbomRev)) {
                        JF_LOGGER.info("当零件子级版本=MBOM版本:{}");
                        //5.1.1. 当零件子级版本=MBOM版本，判断是否是移除，如果是就从MBOM结构中删除，否就忽略
                        if (changeDes.contains("Remove")) {
                            //删除MBOM结构
                            deleteMbomObject(context, mapList);
                            mbomNumberSet.remove(number);
                        } else
                            //判断数量
                            if (bomQuantity > mapList.size()) {
                                //比对ECR上的数量 和 MBOM的结构数量
                                //新增结构
                                for (int i = 0; i < Math.abs(bomQuantity - mapList.size()); i++) {
                                    initECRMBOM(context, ownerMap, ecrId, projectId, new StringList(id), mbomObject.getId(context), partCadMap, true);
                                }
                            } else if (bomQuantity < mapList.size()) {
                                //移除
                                int start1 = mapList.size() - Math.abs(bomQuantity - mapList.size());
                                MapList result = new MapList();
                                result.addAll(mapList.subList(start1, mapList.size()));
                                deleteMbomObject(context, result);
                                if (bomQuantity == 0) {
                                    mbomNumberSet.remove(number);
                                }
                            }
                    } else {
                        JF_LOGGER.info("当零件子级版本<MBOM子级版本:{}");
                        //5.1.5. 当零件子级版本<MBOM子级版本，MBOM不变，继续找子级，当有升版的时候，比对MBOM中的版本，当版本大于MBOM版本，将零件版本、属性同步到MBOM件上
                        for (Integer integer = 0; integer < bomQuantity; integer++) {
                            updateMbomStrctureRevision(context, ownerMap, ecrId, projectId, mbomObj, partObj, stringList, partCadMap);
                        }
                    }
                }else {
                    //创建mbom结构  5.1.2. 当零件子级版本不在MBOM中，新增，需要创建并关联MBOM结构
                    for (Integer integer = 0; integer < bomQuantity; integer++) {
                        initECRMBOM(context, ownerMap, ecrId, projectId, new StringList(id), mbomObject.getId(context), partCadMap, true);
                    }
                }
            }
        } catch (FrameworkException e) {
            ThreadLog.info("子线程异常 大于比对:{}，part: {},mbom:{}", Arrays.toString(e.getStackTrace()),
                    partObject.getAttributeValue(context, JF_PLMConstants_mxJPO.SELECT_ATTR_V_PART_NUMBER) + partObject.getInfo(context, SELECT_REVISION),
                    mbomObject.getAttributeValue(context, "JF_PartNumber") + mbomObject.getInfo(context, SELECT_REVISION));
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * 删除MBOM结构
     * @param context
     * @param mapList
     * @author LIUJR
     * @throws
     * @return void
     * @date 2025/11/28 16:36
     * @description
     */
    //20260819 update by ljr 零件号集合由调用方按实际剩余实例维护，删除方法只处理对象删除。
    public static void deleteMbomObject(Context context, MapList mapList) throws Exception{
        try {
            HashSet<String> deleteIdSet = new HashSet<>();
            DomainObject mbomObject = DomainObject.newInstance(context);
            StringList stringList = new StringList();
            MapList rootMBOMMapList = new MapList();
            Map map = new HashMap();
            String id = EMPTY_STRING;
            for (int i = 0; i < mapList.size(); i++) {
                map = (Map) mapList.get(i);
                id = UIUtil.getValue(map, SELECT_ID);
                mbomObject.setId(id);
                deleteIdSet.add(id);
                rootMBOMMapList = (MapList) mbomObject.getRelatedObjects(context,
                        JF_PLMConstants_mxJPO.REL_JF_relManufacturedItem, // relationship pattern
                        JF_PLMConstants_mxJPO.TYPE_JF_ManufacturedItem,                                    // object pattern
                        JF_Util_mxJPO.basicBolistSel(),                            // object selects
                        JF_Util_mxJPO.basicRellistSel(), // relationship selects
                        false,                                        // to direction
                        true,                                        // from direction
                        (short) 0,                                    // recursion level
                        "",                // object where clause
                        "",
                        (short) 0);
                stringList = (StringList) rootMBOMMapList.stream().map(m -> {
                    Map map1 = (Map) m;
                    return UIUtil.getValue(map1, SELECT_ID);
                }).collect(Collectors.toCollection(StringList::new));
                deleteIdSet.addAll(stringList);
            }
            stringList = StringList.create(deleteIdSet);
            DomainObject.deleteObjects(context, stringList.toStringArray());
        }catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * 获取ebom零件下一层及的结构
     * @param context
     * @param partObject
     * @param relList
     * @param stringList
     * @param level
     * @author LIUJR
     * @throws
     * @return java.util.Map
     * @date 2025/8/20 10:20
     * @description
     */
    public Map getPartEbomStructure(Context context, DomainObject partObject, StringList relList, StringList stringList, int level) throws Exception{
        //EBOM结构
        StringList ebomRelSelectList = new StringList();
        ebomRelSelectList.addAll(relList);
        if (!ebomRelSelectList.contains("attribute[JF_VPMInstance.JF_Dosage]")) {
            ebomRelSelectList.add("attribute[JF_VPMInstance.JF_Dosage]");
        }
        if (!ebomRelSelectList.contains(JF_PLMConstants_mxJPO.SELECT_ATTR_JF_Dosage)) {
            ebomRelSelectList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JF_Dosage);
        }
        if (!ebomRelSelectList.contains(SELECT_ATTRIBUTE_JF_ECRID)) {
            ebomRelSelectList.add(SELECT_ATTRIBUTE_JF_ECRID);
        }
        MapList relatedObjects = partObject.getRelatedObjects(
                context,
                JF_PLMConstants_mxJPO.REL_Instance,
                JF_PLMConstants_mxJPO.TYPE_VPMReference,
                stringList,
                ebomRelSelectList,
                true,
                true,
                (short) level,
                "",
                "",
                0
        );
        Map mapListGroupingMap = JF_NewECRService_mxJPO.getMapListGroupingMap(context, relatedObjects, SELECT_ID);
        return mapListGroupingMap;
    }

    /**
     * 需要mbom的用量
     * @param context
     * @param mbomConnId
     * @param partId
     * @param mapListGroupingMap
     * @author LIUJR
     * @throws
     * @return void
     * @date 2025/8/20 10:16
     * @description
     */
    public void setMbomDosageAttribute(Context context, String mbomConnId, String partId, Map mapListGroupingMap) throws Exception{
        JF_LOGGER.info("setMbomDosageAttribute。。。。。。。。。。。");
        DomainRelationship domainRelationship;
        JF_LOGGER.info("mbomConnId:{}", mbomConnId);
        domainRelationship = DomainRelationship.newInstance(context, mbomConnId);
        String mbomDosage = domainRelationship.getAttributeValue(context, "JF_Dosage");
        JF_LOGGER.info("mbomDosage:{}", mbomDosage);
        String ebomDosage = EMPTY_STRING;
//        JF_LOGGER.info("mapListGroupingMap:{}", mapListGroupingMap);
        if (mapListGroupingMap.containsKey(partId)) {
            List list = (List) mapListGroupingMap.get(partId);
            Map ebomMap  = (Map) list.get(0);
            ebomDosage = UIUtil.getValue(ebomMap, JF_PLMConstants_mxJPO.SELECT_ATTR_JF_Dosage);
        }
        JF_LOGGER.info("ebomDosage:{}", ebomDosage);
        if (!mbomDosage.equalsIgnoreCase(ebomDosage)) {
            mbomDosage = ebomDosage;
            domainRelationship.setAttributeValue(context, "JF_Dosage", mbomDosage);
        }
        domainRelationship.setAttributeValue(context, "JF_OriginateFlag", "EBOM");
    }


    /**
     * 5.1.5. 当零件子级版本<MBOM子级版本，MBOM不变，继续找子级，当有升版的时候，比对MBOM中的版本，当版本大于MBOM版本，将零件版本、属性同步到MBOM件上
     *      *    5.2. 零件版本<MBOM版本，走5.1.5的逻辑
     * @param context
     * @param ownerMap  项目中AME 人员
     * @param ecrId  ecrId
     * @param projectId   项目id
     * @param mbomObject  父级MBOM对象
     * @param partObject  父级ebom对象
     * @param stringList  查询列表
     * @param partCadMap    邮件
     * @author LIUJR
     * @throws
     * @return void
     * @date 2025/8/16 13:58
     * @description
     */
    public void updateMbomStrctureRevision(Context context, HashMap<String, String> ownerMap, String ecrId, String projectId, DomainObject mbomObject,
                                           DomainObject partObject, StringList stringList,
                                           Map<String, String> partCadMap) throws Exception{
        //如果供货件零件不在MBOM集合B中，需要更新到MBOM
        //拿取rootPartList中的企业编码和
        StringList relList = new StringList();
        relList.add(SELECT_ATTRIBUTE_JF_ECRID);
        relList.add(DomainRelationship.SELECT_ID);
        relList.add(SELECT_ATTRIBUTE_JF_BOMChangeDes);
        relList.add(SELECT_ATTRIBUTE_JF_BOMQuantity);  //数量
        relList.add(SELECT_FROM_ID);
//        ThreadLog.info("子线程-小于比对:part: {},mbom:{}",
//                partObject.getAttributeValue(context, JF_PLMConstants_mxJPO.SELECT_ATTR_V_PART_NUMBER) + partObject.getInfo(context, SELECT_REVISION),
//                mbomObject.getAttributeValue(context, "JF_PartNumber") + mbomObject.getInfo(context, SELECT_REVISION));
        try {
            //ECR受影响清单结构
            MapList rootPartMapList = (MapList) partObject.getRelatedObjects(context,
                    JF_PLMConstants_mxJPO.REL_JFECRRoot2Item + "," + JF_PLMConstants_mxJPO.REL_JFECRRoot2ItemBubble, // relationship pattern
                    JF_PLMConstants_mxJPO.TYPE_VPMReference,                                    // object pattern
                    stringList,                            // object selects
                    relList, // relationship selects
                    false,                                        // to direction
                    true,                                        // from direction
                    (short) 1,                                    // recursion level
                    "",                // object where clause
                    "",
                    (short) 0);
            Map mapListGroupingMap = getPartEbomStructure(context, partObject, relList, stringList, 1);
            //MBOM结构
            MapList rootMBOMMapList = (MapList) mbomObject.getRelatedObjects(context,
                    JF_PLMConstants_mxJPO.REL_JF_relManufacturedItem, // relationship pattern
                    JF_PLMConstants_mxJPO.TYPE_JF_ManufacturedItem,                                    // object pattern
                    stringList,                            // object selects
                    relList, // relationship selects
                    false,                                        // to direction
                    true,                                        // from direction
                    (short) 1,                                    // recursion level
                    "",                // object where clause
                    "",
                    (short) 0);
            HashMap<String, MapList> mbomMap = new HashMap<>();
            //20260819 update by ljr 使用HashSet索引当前层MBOM零件号，避免循环内线性contains查询。
            Set<String> mbomNumberSet = new HashSet<>();
            //可能有多个
            String number = EMPTY_STRING;
            String revision = EMPTY_STRING;
            MapList mapList = new MapList();
            for (int i = 0; i < rootMBOMMapList.size(); i++) {
                Map map = (Map) rootMBOMMapList.get(i);
                number = UIUtil.getValue(map, "attribute[JF_PartNumber]");
                revision = UIUtil.getValue(map, SELECT_REVISION);
                mapList = new MapList();
                mbomNumberSet.add(number);
                if (mbomMap.containsKey(number)) {
                    mapList = mbomMap.get(number);
                }
                mapList.add(map);
                mbomMap.put(number, mapList);
            }
            DomainObject partObj = DomainObject.newInstance(context);
            DomainObject mbomObj = DomainObject.newInstance(context);
//            JF_LOGGER.info("rootPartMapList:{}", rootPartMapList);
//            JF_LOGGER.info("mbomMap:{}", mbomMap);
//            JF_LOGGER.info("mbomNumberSet:{}", mbomNumberSet);
            String ecrIds = EMPTY_STRING;
            String changeDes = EMPTY_STRING;
            Integer bomQuantity = 0;
            String id = EMPTY_STRING;
            String parentId = EMPTY_STRING;
            String fromId = EMPTY_STRING;
            for (int i1 = 0; i1 < rootPartMapList.size(); i1++) {
                Map map = (Map) rootPartMapList.get(i1);
                //判断是否是这个ecr的结构
                ecrIds = UIUtil.getValue(map, SELECT_ATTRIBUTE_JF_ECRID);
                changeDes = UIUtil.getValue(map, SELECT_ATTRIBUTE_JF_BOMChangeDes);
                if (!ecrIds.contains(ecrId)) {
                    continue;
                }
                number = UIUtil.getValue(map, JF_PLMConstants_mxJPO.SELECT_ATTR_V_PART_NUMBER);
                bomQuantity = Integer.valueOf(UIUtil.getValue(map, SELECT_ATTRIBUTE_JF_BOMQuantity));
                revision = UIUtil.getValue(map, SELECT_REVISION);
                id = UIUtil.getValue(map, SELECT_ID);
                parentId = UIUtil.getValue(map, SELECT_FROM_ID);
                //生成MBOM的时候仅CAD件及其子级不生成。判断一个件是否仅CAD件需要判断关系属性：VPMInstance 关系属性 SynchroEBOMExt.V_InEBOMApplicative 为TRUE就是仅CAD件
                if (partCadMap.containsKey(id)) {
                    //仅cad件  不生成
                    fromId = partCadMap.get(id);
                    if (parentId.equalsIgnoreCase(fromId)) {
                        continue;
                    }
                }
                /*
                 * 5.2. 零件版本<MBOM版本，走5.1.5的逻辑
                 * 5.1.5. 当零件子级版本<MBOM子级版本，MBOM不变，继续找子级，当有升版的时候，比对MBOM中的版本，当版本大于MBOM版本，将零件版本、属性同步到MBOM件上
                 * */
                if (mbomNumberSet.contains(number)) {
                    mapList = mbomMap.get(number);
                    Map mbomIdAndRev = (Map) mapList.get(0);
                    String mbomId = UIUtil.getValue(mbomIdAndRev, SELECT_ID);
                    String mbomRev = UIUtil.getValue(mbomIdAndRev, SELECT_REVISION);
                    String mbomConnId = UIUtil.getValue(mbomIdAndRev, DomainRelationship.SELECT_ID);
                    mbomObj.setId(mbomId);
                    partObj.setId(id);
//                        mbomRev = MBOM_VERSION_CACHE.containsKey(mbomId) ? MBOM_VERSION_CACHE.get(mbomId) : mbomRev;
                    String maxRevision = JF_Util_mxJPO.getMaxRevision(context, new String[]{revision, mbomRev});
                    if (!changeDes.contains("ReviseRelease")) {
                        updateMbomStrctureRevision(context, ownerMap, ecrId, projectId, mbomObj, partObj, stringList, partCadMap);
                        // } else if (revision.compareTo(mbomRev) > 0) {
                    } else if (revision.equalsIgnoreCase(maxRevision)) {
                        //5.1.4. 当零件子级版本>MBOM子级版本，需要更新版本和子级版本，将零件版本、属性同步到MBOM件上
                        //比对ECR上的数量 和 MBOM的结构数量 升版新增和移除
                        if (bomQuantity > mapList.size()) {
                            //比对ECR上的数量 和 MBOM的结构数量
                            //新增结构
                            Integer num = Math.abs(bomQuantity - mapList.size());
                            for (int i = 0; i < num; i++) {
                                mapList.addAll(initECRMBOM(context, ownerMap, ecrId, projectId, new StringList(id), mbomObject.getId(context), partCadMap, true));
                            }
                        } else if (bomQuantity < mapList.size()) {
                            //移除
                            int start = mapList.size() - Math.abs(bomQuantity - mapList.size());
                            MapList result = new MapList();
                            result.addAll(mapList.subList(start, mapList.size()));
                            deleteMbomObject(context, result);
                            result.clear();
                            result.addAll(mapList.subList(0, start));
                            mapList = result;
                            if (mapList.isEmpty()) {
                                mbomNumberSet.remove(number);
                            }
                        }
                        String partType = UIUtil.getValue(map, JF_PLMConstants_mxJPO.SELECT_ATTR_JFPartType);
                        boolean isTrimPart = JF_PLMConstants_mxJPO.ATTR_JFPartType_RANGE_T.equalsIgnoreCase(partType);
                        //20260819 update by ljr 同一EBOM零件存在多个MBOM实例时，属性和DirectBuy只查询一次。
                        Map<String, String> partValue = isTrimPart
                                ? getTrimPartAttributeValues(context, id, null)
                                : partObj.getInfo(context, partAttribute);
                        partValue.put(JF_PLMConstants_mxJPO.Attr_JF_DirectBuy,
                                JF_Util_mxJPO.getPartDirectBuyByProjectWithNonDBDefault(
                                        context, id, projectId));
                        Map<String, String> mbomAttributeValue = isTrimPart
                                ? changeTrimAttribute(context, partValue)
                                : changeAttribute(context, partValue);
                        String rev = UIUtil.getValue(partValue, SELECT_REVISION);
                        for (int i2 = 0; i2 < mapList.size(); i2++) {
                            mbomIdAndRev = (Map) mapList.get(i2);
                            mbomRev = UIUtil.getValue(mbomIdAndRev, SELECT_REVISION);
                            mbomId = UIUtil.getValue(mbomIdAndRev, SELECT_ID);
                            mbomConnId = UIUtil.getValue(mbomIdAndRev, DomainRelationship.SELECT_ID);
                            mbomObj.setId(mbomId);
                            JF_LOGGER.info("MBOM属性同步");
                            //20260819 update by ljr 面套子级升版调用独立属性和用量转换方法；
                            if (isTrimPart) {
                                mbomObj.setAttributeValues(context, mbomAttributeValue);
                                setTrimMbomDosageAttribute(
                                        context, mbomConnId, id, mapListGroupingMap, partValue);
                            } else {
                                mbomObj.setAttributeValues(context, mbomAttributeValue);
                                setMbomDosageAttribute(context, mbomConnId, id, mapListGroupingMap);
                            }
                            //修改MBOM版本
                            String mql = modNameRevMql.replace("Id", mbomId).replace("N", mbomObj.getInfo(context, SELECT_NAME)).replace("R", rev);
                            MqlUtil.mqlCommand(context, false, mql, true);
//                                MBOM_VERSION_CACHE.put(mbomId, rev);
                            JF_LOGGER.info("mql:{}", mql);
                            //设置转换之后的值
                            updateMbomStrctureRevision(context, ownerMap, ecrId, projectId, mbomObj, partObj, stringList, partCadMap);
                        }
                    }
                } else {
                    //创建mbom结构  5.1.2. 当零件子级版本不在MBOM中，新增，需要创建并关联MBOM结构
//                    for (Integer integer = 0; integer < bomQuantity; integer++) {
                    initECRMBOM(context, ownerMap, ecrId, projectId, new StringList(id), mbomObject.getId(context), partCadMap, true);
//                    }
                }
            }
        } catch (FrameworkException e) {
            ThreadLog.info("子线程异常 小于比对:{}，part: {},mbom:{}", Arrays.toString(e.getStackTrace()),
                    partObject.getAttributeValue(context, JF_PLMConstants_mxJPO.SELECT_ATTR_V_PART_NUMBER) + partObject.getInfo(context, SELECT_REVISION),
                    mbomObject.getAttributeValue(context, "JF_PartNumber") + mbomObject.getInfo(context, SELECT_REVISION));

            throw e;
        }
    }

    /**
     * 创建变更记录
     * @param context
     * @author LIUJR
     * @throws
     * @return java.lang.String
     * @date 2025/7/19 10:37
     * @description
     */
    public String createChangeRecord(Context context, StringList ecrNameAndTitleList, String projectId, StringList ecrIdList, String owner) throws Exception{
        DomainObject domainObject = DomainObject.newInstance(context);
        try {
            String sObjGeneratorName = UICache.getObjectGenerator(context, TYPE_type_JF_ChangeRecord, "");
            String sName = DomainObject.getAutoGeneratedName(context, sObjGeneratorName, "");
            String sPolicy = POLICY_JF_ChangeRecord;
            Policy policy = new Policy(sPolicy);
            String revision = policy.getFirstInSequence(context);
            domainObject.createObject(context, TYPE_JF_ChangeRecord, sName, revision, sPolicy, context.getVault().getName());
            //设置属性
            HashMap<String, String> attrMap = new HashMap<>();
            attrMap.put(ATTR_JF_UpdatePerson, owner);
            //前台时间转换为数据库时间
            LocalDateTime now = LocalDateTime.now();
            // 格式化整个日期时间字符串
            String formattedDateTime = now.format(inputFormatter);
            attrMap.put(ATTR_JF_UpdateDateTime, formattedDateTime);
            attrMap.put(DomainConstants.ATTRIBUTE_TITLE, ecrNameAndTitleList.join(";"));
            domainObject.setAttributeValues(context, attrMap);
            //与项目和ecr关联关系
            domainObject.addFromObject(context, new RelationshipType(REL_JF_relProject2ChangeRecord), projectId);
            DomainRelationship.connect(context, domainObject, new RelationshipType(REL_JF_relChangeRecord2ECR), true, ecrIdList.toStringArray());
            //修改变更记录的owner和协作区
            JF_Util_mxJPO.changeowner(context, domainObject.getId(context), projectId,owner);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
        return domainObject.getId(context);
    }


    /**
     * 变更结构发送邮件 以ecr为隔离
     * @param context
     * @param emailSet
     * @param projectId
     * @param ecrNames
     * @param partNameList
     * @author LIUJR
     * @throws
     * @return void
     * @date 2025/7/19 10:22
     * @description
     */
    public void rollBakeMBOMSendEmail(Context context, HashSet<String> emailSet, String projectId, StringList ecrNames, StringList partNameList, String projectNameAndDesc) throws Exception{
        StringList emails = StringList.create(emailSet);
        StringList toEmailList = new StringList();
        DomainObject domainObject = DomainObject.newInstance(context);
        for (int i = 0; i < emails.size(); i++) {
            String name = emails.get(i);
            if (UIUtil.isNullOrEmpty(name)) {
                continue;
            }
            domainObject = PersonUtil.getPersonObject(context, name);
            String emailAddress = domainObject.getAttributeValue(context, DomainObject.ATTRIBUTE_EMAIL_ADDRESS);
            toEmailList.add(emailAddress);
        }
        mbomStructureSendEmail(context, toEmailList.join(","), projectId, ecrNames.join(","), partNameList.join(","), projectNameAndDesc, "RollBakeMBOMEmail", "MBOM结构回滚通知", "MBOM structure rollBack Notification");
    }


    /**
     * 变更结构发送邮件 以ecr为隔离
     * @param context
     * @param emailSet
     * @param projectId
     * @param ecrNames
     * @param partNameList
     * @author LIUJR
     * @throws
     * @return void
     * @date 2025/7/19 10:22
     * @description
     */
    public void updateMBOMSendEmail(Context context, HashSet<String> emailSet, String projectId, StringList ecrNames, StringList partNameList, String projectNameAndDesc) throws Exception{
        try {
            StringList emails = StringList.create(emailSet);
            StringList toEmailList = new StringList();
            DomainObject domainObject = DomainObject.newInstance(context);
            for (int i = 0; i < emails.size(); i++) {
                String name = emails.get(i);
                domainObject = PersonUtil.getPersonObject(context, name);
                String emailAddress = domainObject.getAttributeValue(context, DomainObject.ATTRIBUTE_EMAIL_ADDRESS);
                toEmailList.add(emailAddress);
            }
            mbomStructureSendEmail(context, toEmailList.join(","), projectId, ecrNames.join(","), partNameList.join(","), projectNameAndDesc, "UpdateMBOMEmail", "MBOM结构更新通知", "MBOM structure update Notification");
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * 获取项目的变更记录
     * @param context
     * @param args
     * @author LIUJR
     * @throws
     * @return com.matrixone.apps.domain.util.MapList
     * @date 2025/7/19 13:23
     * @description
     */
    public MapList getPsChangeRecord(Context context, String[] args) throws Exception {
        try {
            StringList selList = JF_Util_mxJPO.basicBolistSel();
            StringList relList = JF_Util_mxJPO.basicRellistSel();
            Map paramsMap = (Map) JPO.unpackArgs(args);
            ContextUtil.pushContext(context);
            String strObjectId = (String) paramsMap.get(STRING_PARENTOID);
            JF_LOGGER.info("strObjectId:{}",strObjectId);
            DomainObject objectProject = DomainObject.newInstance(context, strObjectId);
            selList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JF_UpdateDate);
            MapList mapList = objectProject.getRelatedObjects(context,
                    JF_PLMConstants_mxJPO.REL_JF_relProject2ChangeRecord, //pattern to match relationships
                    JF_PLMConstants_mxJPO.TYPE_JF_ChangeRecord, //pattern to match types
                    selList, //the eMatrix StringList object that holds the list of select statement pertaining to Business Obejcts.
                    relList, //the eMatrix StringList object that holds the list of select statement pertaining to Relationships.
                    false, //get To relationships
                    true, //get From relationships
                    (short) 1, //the number of levels to expand, 0 equals expand all.
                    EMPTY_STRING, //where clause to apply to objects, can be empty ""
                    null, //where clause to apply to relationship, can be empty ""
                    (short) 0);//limit
            JF_LOGGER.info("mapList：{}", mapList);
            mapList.addSortKey(JF_PLMConstants_mxJPO.SELECT_ATTR_JF_UpdateDate, ProgramCentralConstants.ASCENDING_SORT, ProgramCentralConstants.SORTTYPE_DATE);
            mapList.sort();
            JF_LOGGER.info("mapList：{}", mapList);
            return mapList;
        }catch (Exception e) {
            e.printStackTrace();
        }finally {
            ContextUtil.popContext(context);
        }
        return new MapList();
    }

    /**
     * 获取项目的变更记录日期
     * @param context
     * @param args
     * @author LIUJR
     * @throws
     * @return matrix.util.StringList
     * @date 2025/7/19 13:25
     * @description
     */
    public StringList getChangeRecordDate(Context context,String[] args) throws Exception{
        StringList res = new StringList();
        try {
            JF_LOGGER.info("getChangeRecordDate:");
            Map argMaps = JPO.unpackArgs(args);
            JF_LOGGER.info("getChangeRecordDate:{}", argMaps);
            MapList argMapList = (MapList) argMaps.get(JF_PLMConstants_mxJPO.STRING_OBJECTLIST);
            Map columnMap = (Map) argMaps.get("columnMap");
            String colName = (String) columnMap.get(SELECT_NAME);
            JF_LOGGER.info("colName:{}", colName);
            DomainObject domainObject = DomainObject.newInstance(context);
            ContextUtil.pushContext(context);
            for (int i = 0; i < argMapList.size(); i++) {
                Map infoMap = (Map) argMapList.get(i);
                String strId = (String) infoMap.get(SELECT_ID);
                domainObject.setId(strId);
                String updateDateTime = domainObject.getAttributeValue(context, ATTR_JF_UpdateDateTime);
                String date="NA";
                String time="NA";
                if(UIUtil.isNotNullAndNotEmpty(updateDateTime)){
                    // 解析输入日期
                    LocalDateTime dateTime = LocalDateTime.parse(updateDateTime, inputFormatter);
                    // 格式化输出日期
                    // 拆分并格式化日期部分和时间部分
                    date = dateTime.format(formatterDate);
                    time = dateTime.format(formatterTime);
                }

                if ("ChangeDate".equalsIgnoreCase(colName)) {
                    res.add(date);
                } else if ("ChangeTime".equalsIgnoreCase(colName)){
                    res.add(time);
                }
            }
            JF_LOGGER.info(colName+"--res:{}", res);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        } finally {
            ContextUtil.popContext(context);
        }
        return res;
    }

    /**
     * @Author Liuxg
     * @Description
     * @Date 2025/9/25 2:51
     * @Param [context, args]
     * @return matrix.util.StringList
     **/
    public StringList getSyncRecordDate(Context context,String[] args) throws Exception{
        StringList res = new StringList();
        try {
            JF_LOGGER.info("getSyncRecordDate:");
            Map argMaps = JPO.unpackArgs(args);
            JF_LOGGER.info("getSyncRecordDate:{}", argMaps);
            MapList argMapList = (MapList) argMaps.get(JF_PLMConstants_mxJPO.STRING_OBJECTLIST);
            Map columnMap = (Map) argMaps.get("columnMap");
            String colName = (String) columnMap.get(SELECT_NAME);
            JF_LOGGER.info("colName:{}", colName);
            DomainObject domainObject = DomainObject.newInstance(context);
            ContextUtil.pushContext(context);
            for (int i = 0; i < argMapList.size(); i++) {
                Map infoMap = (Map) argMapList.get(i);
                String strId = (String) infoMap.get(SELECT_ID);
                domainObject.setId(strId);
                String updateDateTime = domainObject.getAttributeValue(context, "JF_SyncMBOMUpdateDateTime");
                // 解析输入日期
                LocalDateTime dateTime = LocalDateTime.parse(updateDateTime, inputFormatter);
                // 格式化输出日期
                // 拆分并格式化日期部分和时间部分
                String date = dateTime.format(formatterDate);
                String time = dateTime.format(formatterTime);
                if ("SyncDate".equalsIgnoreCase(colName)) {
                    res.add(date);
                } else if ("SyncTime".equalsIgnoreCase(colName)){
                    res.add(time);
                }
            }
            JF_LOGGER.info("res:{}", res);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        } finally {
            ContextUtil.popContext(context);
        }
        return res;
    }



    public Vector getDownload(Context context,String[] args) throws Exception{
        Vector res = new Vector();
        try {
            JF_LOGGER.info("getDownload:");
            Map argMaps = JPO.unpackArgs(args);
            JF_LOGGER.info("getDownload:{}", argMaps);
            MapList argMapList = (MapList) argMaps.get(JF_PLMConstants_mxJPO.STRING_OBJECTLIST);
            Map columnMap = (Map) argMaps.get("columnMap");
            String colName = (String) columnMap.get(SELECT_NAME);
            JF_LOGGER.info("colName:{}", colName);
            DomainObject domainObject = DomainObject.newInstance(context);
            ContextUtil.pushContext(context);
            for (int i = 0; i < argMapList.size(); i++) {
                Map infoMap = (Map) argMapList.get(i);
                String strId = (String) infoMap.get(SELECT_ID);
                domainObject.setId(strId);
                FileList fileList=domainObject.getFiles(context);
                String filename=fileList.get(0).getName();

                StringBuffer fileActionsStrBuff=new StringBuffer();


                String fileFormat="generic";
                String sTipDownload="下载文件副本，但要保持解锁状态";

                String downloadURL = "javascript:callCheckout('"+ XSSUtil.encodeForJavaScript(context, strId) +"','download', '"+ XSSUtil.encodeForJavaScript(context, filename)+ "', '" + XSSUtil.encodeForJavaScript(context, fileFormat) +"');";

                fileActionsStrBuff.append("<a href=\"" + downloadURL+"\">");  //iconActionDownload
                fileActionsStrBuff.append("<img border='0' src='../common/images/iconSmallDocument.gif' alt=\""+sTipDownload+"\" title=\""+sTipDownload+"\"></img></a>&#160;");

                res.add(fileActionsStrBuff.toString());
            }
            JF_LOGGER.info("res:{}", res);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        } finally {
            ContextUtil.popContext(context);
        }
        return res;
    }

    public StringList getDownloadSyncRecord(Context context,String[] args) throws Exception{
        StringList res = new StringList();
        try {
            JF_LOGGER.info("getDownloadSyncRecord:");
            Map argMaps = JPO.unpackArgs(args);
            JF_LOGGER.info("getDownloadSyncRecord:{}", argMaps);
            MapList argMapList = (MapList) argMaps.get(JF_PLMConstants_mxJPO.STRING_OBJECTLIST);
            Map columnMap = (Map) argMaps.get("columnMap");
            String colName = (String) columnMap.get(SELECT_NAME);
            JF_LOGGER.info("colName:{}", colName);
            DomainObject domainObject = DomainObject.newInstance(context);
            ContextUtil.pushContext(context);
            for (int i = 0; i < argMapList.size(); i++) {
                Map infoMap = (Map) argMapList.get(i);
                String strId = (String) infoMap.get(SELECT_ID);
                domainObject.setId(strId);
                //

            }
            JF_LOGGER.info("res:{}", res);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        } finally {
            ContextUtil.popContext(context);
        }
        return res;
    }

    /**
     * 创建MBOM发送邮件
     * @param context
     * @param emailSet
     * @param ecrName
     * @param projectId
     * @param allRootPartNameList
     * @param projectNameAndDesc
     * @author LIUJR
     * @throws
     * @return void
     * @date 2025/7/19 17:06
     * @description
     */
    public void createMBOMSendEmail(Context context, HashSet<String> emailSet, String ecrName, String projectId, StringList allRootPartNameList, String projectNameAndDesc) throws Exception{
        StringList emails = StringList.create(emailSet);
        StringList toEmailList = new StringList();
        for (int i = 0; i < emails.size(); i++) {
            String name = emails.get(i);
            DomainObject personObject = PersonUtil.getPersonObject(context, name);
            String emailAddress = personObject.getAttributeValue(context, DomainObject.ATTRIBUTE_EMAIL_ADDRESS);
            toEmailList.add(emailAddress);
        }
        //发邮件
        mbomStructureSendEmail(context, toEmailList.join(","), projectId, ecrName,
                allRootPartNameList.join(","), projectNameAndDesc, "InitMBOMEmail", "MBOM结构初始化通知", "MBOM structure initialization Notification");
    }


    /**
     * @Author Liuxg
     * @Description 更新制造件
     * @Date 2025/7/19 23:53
     * @Param [context, args]
     * @return java.lang.String
     **/
    public Map updateMbomPart(Context context,String[]args)throws Exception{
        Map resultmap=new HashMap();
        String errorMsg="";

        try {
            String loginUser=context.getUser();
            Map pramMap=JPO.unpackArgs(args);
            String mbomidold= (String) pramMap.get("mbomid");
            DomainObject mbomobjold=DomainObject.newInstance(context,mbomidold);

            String perentid=mbomobjold.getInfo(context,"to["+REL_JF_relManufacturedItem+"].from.id");
            if(UIUtil.isNullOrEmpty(perentid)){
                //根节点
                //当前所勾选数据为MBOM顶层节点，请重新选择！
                errorMsg = EnoviaResourceBundle.getProperty(context, "emxFrameworkStringResource", context.getLocale(), "emxFramework.common.updateMbomPartErrorMsg1");

            }else {
                //正常MBOM节点
//                DomainObject parentObj=DomainObject.newInstance(context,perentid);
//                String parentOwner=parentObj.getOwner(context).getName();
                String mbomowner=mbomobjold.getOwner(context).getName();
                if(!loginUser.equals(mbomowner)){
                    //当前所勾选数据无编辑权限，请重新选择！
                    errorMsg = EnoviaResourceBundle.getProperty(context, "emxFrameworkStringResource", context.getLocale(), "emxFramework.common.updateMbomPartErrorMsg2");

                }else {
                    //获取零件号
                    String mbomPartNumber=mbomobjold.getAttributeValue(context,"JF_PartNumber");
                    if(UIUtil.isNotNullAndNotEmpty(mbomPartNumber)){
                        System.out.println("mbomidold----->"+mbomidold);
                        //获取根节点
                        String rootid=getMbomRootid(context,mbomidold,"");
                        DomainObject rootobj=DomainObject.newInstance(context,rootid);
                        StringList boSel = JF_Util_mxJPO.basicBolistSel();
                        boSel.add("attribute[JF_PartNumber]");
                        StringList relSel = new StringList();
                        MapList childPartList =  rootobj.getRelatedObjects(context,JF_PLMConstants_mxJPO.REL_JF_relManufacturedItem,JF_PLMConstants_mxJPO.TYPE_JF_ManufacturedItem,
                                boSel,relSel,false,true,Short.parseShort("0"),"","",0);

                        StringList tagidlist=new StringList();
                        //是否包含当前勾选对象

                        for(int i=0;i<childPartList.size();i++){
                            Map mobmmap= (Map) childPartList.get(i);
                            String childPartnumber= (String) mobmmap.get("attribute[JF_PartNumber]");
                            String childPartid= (String) mobmmap.get(DomainConstants.SELECT_ID);
                            System.out.println("childPartid----->"+childPartid);
                            //同号制造件
                            if(UIUtil.isNotNullAndNotEmpty(childPartnumber)&&mbomPartNumber.equals(childPartnumber)&&!mbomidold.equals(childPartid)){
                                tagidlist.add(childPartid);
                            }
                        }
                        if(tagidlist.size()==0){
                            //当前项目MBOM清单中不存在同号的制造件！
                            errorMsg = EnoviaResourceBundle.getProperty(context, "emxFrameworkStringResource", context.getLocale(), "emxFramework.common.updateMbomPartErrorMsg3");
                        }else {
                            //找到源零件对象，更新目标零件属性（即同号零件清单中的MBOM对象）

                            for(String tagid:tagidlist){
                                //更新属性
                                updateMBOMPartAttr(context,mbomidold,loginUser,tagid);
                                //获取mbom下的所有旧结构，断开并删除
                                DeleteMbomPart(context,tagid);
                                //更新mbom结构
                                cloneMbomPart(context,mbomidold,tagid);

                            }
                        }

                    }else {
                        //零件号为空
                    }
                }

            }

            if(UIUtil.isNullOrEmpty(errorMsg)){
                //制造件更新已完成！
                errorMsg = EnoviaResourceBundle.getProperty(context, "emxFrameworkStringResource", context.getLocale(), "emxFramework.common.updateMbomPartErrorMsg4");
                resultmap.put("code","200");
            }

        }catch (Exception e){
            e.printStackTrace();
            errorMsg=e.getMessage();
            throw e;
        }
        resultmap.put("msg",errorMsg);
        return resultmap;
    }


    public String getMbomRootid(Context context,String id,String rootid)throws Exception{

        DomainObject childobj=DomainObject.newInstance(context,id);
        String perentid=childobj.getInfo(context,"to["+REL_JF_relManufacturedItem+"].from.id");
        if(UIUtil.isNotNullAndNotEmpty(perentid)){
            rootid=getMbomRootid(context,perentid,rootid);
        }else {
            rootid= id;
        }
        return rootid;
    }


    /**
     * @Author Liuxg
     * @Description 更新mbom制作件属性
     * @Date 2025/7/20 19:42
     * @Param [context, id, owner, mbomId]
     * @return java.lang.String
     **/
    public String  updateMBOMPartAttr(Context context,String id,String owner,String mbomId) throws Exception{
        JF_LOGGER.info("updateMBOMPartAttr start");
        DomainObject partObj = DomainObject.newInstance(context,id);
        StringList partAttribute= JF_Util_mxJPO.basicBolistSel();
        Map attrmap=partObj.getAttributeMap(context);

        DomainObject mbomObj = DomainObject.newInstance(context,mbomId);
        String partRevision = partObj.getInfo(context, DomainConstants.SELECT_REVISION);
        MqlUtil.mqlCommand(context, false, "mod bus "+mbomId+" revision '"+partRevision+"' name '"+mbomObj.getInfo(context, DomainConstants.SELECT_NAME)+"'", true);
        mbomObj.setAttributeValues(context,attrmap);
        JF_Util_mxJPO.changeowner(context, mbomId, partObj.getObjectId(context),owner);
        JF_LOGGER.info("updateMBOMPartAttr end {}",mbomId);
        return mbomId;
    }

    /**
     * @Author Liuxg
     * @Description 断开并删除MBOM制造件下的所有结构
     * @Date 2025/7/20 20:19
     * @Param [context, Mbomid]
     * @return void
     **/
    public void DeleteMbomPart(Context context,String Mbomid)throws Exception{
        try {
            DomainObject rootobj=DomainObject.newInstance(context,Mbomid);
            StringList selList=JF_Util_mxJPO.basicBolistSel();
            StringList relList=JF_Util_mxJPO.basicRellistSel();

            MapList subList = rootobj.getRelatedObjects(context,
                    JF_PLMConstants_mxJPO.REL_JF_relManufacturedItem, //pattern to match relationships
                    JF_PLMConstants_mxJPO.TYPE_JF_ManufacturedItem, //pattern to match types
                    selList, //the eMatrix StringList object that holds the list of select statement pertaining to Business Obejcts.
                    relList, //the eMatrix StringList object that holds the list of select statement pertaining to Relationships.
                    false, //get To relationships
                    true, //get From relationships
                    (short) 0, //the number of levels to expand, 0 equals expand all.
                    DomainConstants.EMPTY_STRING, //where clause to apply to objects, can be empty ""
                    null, //where clause to apply to relationship, can be empty ""
                    (short) 0); //limit

            StringList delObjidlist=new StringList();
            StringList delrelidlist=new StringList();
            for(int i=0;i<subList.size();i++){
                Map childmap= (Map) subList.get(i);
                String relid= (String) childmap.get(DomainConstants.SELECT_RELATIONSHIP_ID);
                String objid=(String) childmap.get(DomainConstants.SELECT_ID);
                delObjidlist.add(objid);
                delrelidlist.add(relid);

            }
            ContextUtil.pushContext(context);
            DomainRelationship.disconnect(context,delrelidlist.toStringArray());
            DomainObject.deleteObjects(context,delObjidlist.toStringArray());

        }catch (Exception e){
            e.printStackTrace();
            throw e;
        }finally {
            ContextUtil.popContext(context);
        }
    }
    public void cloneMbomPart(Context context,String Mbomid,String targid)throws Exception{
        try {
            DomainObject rootobj=DomainObject.newInstance(context,Mbomid);
            StringList selList=JF_Util_mxJPO.basicBolistSel();
            StringList relList=JF_Util_mxJPO.basicRellistSel();


            MapList subList = rootobj.getRelatedObjects(context,
                    JF_PLMConstants_mxJPO.REL_JF_relManufacturedItem, //pattern to match relationships
                    JF_PLMConstants_mxJPO.TYPE_JF_ManufacturedItem, //pattern to match types
                    selList, //the eMatrix StringList object that holds the list of select statement pertaining to Business Obejcts.
                    relList, //the eMatrix StringList object that holds the list of select statement pertaining to Relationships.
                    false, //get To relationships
                    true, //get From relationships
                    (short) 1, //the number of levels to expand, 0 equals expand all.
                    DomainConstants.EMPTY_STRING, //where clause to apply to objects, can be empty ""
                    null, //where clause to apply to relationship, can be empty ""
                    (short) 0); //limit

            System.out.println("--subList---"+subList);
            for(int i=0;i<subList.size();i++){
                Map childmap= (Map) subList.get(i);
                String relid= (String) childmap.get(DomainConstants.SELECT_RELATIONSHIP_ID);
                String objid=(String) childmap.get(DomainConstants.SELECT_ID);
                DomainObject childobj=DomainObject.newInstance(context,objid);


                String mbomId = FrameworkUtil.autoName(context,  "type_JF_ManufacturedItem", "policy_JF_ManufacturedItem");
                DomainObject newmbomObj = DomainObject.newInstance(context,mbomId);
                String partRevision = childobj.getInfo(context, DomainConstants.SELECT_REVISION);
                MqlUtil.mqlCommand(context, false, "mod bus "+mbomId+" revision '"+partRevision+"' name '"+newmbomObj.getInfo(context, DomainConstants.SELECT_NAME)+"'", true);
                newmbomObj.setAttributeValues(context,childobj.getAttributeMap(context) );
                JF_Util_mxJPO.changeowner(context, mbomId, childobj.getObjectId(context),childobj.getOwner(context).getName());


                DomainRelationship relobj=DomainRelationship.newInstance(context,relid);
                connectionMBOMSetFlag(context,targid,mbomId,relobj.getAttributeValue(context,"JF_Dosage"), relobj.getAttributeValue(context,"JF_OriginateFlag"));

                cloneMbomPart(context,objid,mbomId);

            }
            //ContextUtil.pushContext(context);

        }catch (Exception e){
            e.printStackTrace();
            throw e;
        }finally {
            //ContextUtil.popContext(context);
        }
    }

    /**
     * 获取ECR的待更新的整椅数据
     * @param context
     * @param ecrIdSplit
     * @param projectId
     * @param ecrRootPartMap
     * @param ecrPartMap
     * @param hasPartEcrIdList
     * @param mapList
     * @author LIUJR
     * @throws
     * @return void
     * @date 2025/7/24 13:52
     * @description
     */
    public void getECRPartUpdateMBOM(Context  context, String[] ecrIdSplit, String projectId,
                                     HashMap<String, StringList> ecrRootPartMap, HashMap<String, StringList> ecrPartMap,
                                     StringList hasPartEcrIdList, MapList mapList) throws Exception{
        try {
            DomainObject domainObject = DomainObject.newInstance(context);
            DomainObject partObject = DomainObject.newInstance(context);
            DomainRelationship domainRelationship;
            for (int i = 0; i < ecrIdSplit.length; i++) {
                //ecrId
                String ecrId = ecrIdSplit[i];
                domainObject.setId(ecrId);
                JF_LOGGER.info("ecrId:{}", ecrId);
                //获取ecr第一层级的受影响项
                StringList rootItemList = domainObject.getInfoList(context, "from[" + JF_PLMConstants_mxJPO.REL_JFECRRelateRoot + "].to.id");
                StringList stringList = new StringList();
                StringList PartList = new StringList();
                for (int i1 = 0; i1 < rootItemList.size(); i1++) {
                    //零件id
                    String  partId = rootItemList.get(i1);
                    partObject.setId(partId);
                    //获取整椅列表[revision]属性，如果整椅的版本[revision]=="AA.*"，不做处理，已经同步过了
                    String revision = partObject.getInfo(context, DomainConstants.SELECT_REVISION);
                    //判断当前零件是否与项目有关联关系
                    Map relMap = new HashMap<>();
                    relMap.put("relName", JF_PLMConstants_mxJPO.rel_JFProject2RootPart);
                    relMap.put("fromId", projectId);
                    relMap.put("toId", partId);
                    String connId = JF_PublicMethodClass_mxJPO.getTwoBusinessObjectConnId(context, JPO.packArgs(relMap));
                    //判断当前VPMReference与对应的<Project Space>是否存在[JFProject2RootPart]关系： 不存在不处理
                    if (UIUtil.isNullOrEmpty(connId)) {
                        continue;
                    }
                    //  3. 判断当前VPMReference与<Project Space>的[JFProject2RootPart.JFZeroPart]等于“Y”；
                    domainRelationship = DomainRelationship.newInstance(context, connId);
                    String strJFZeroPart = domainRelationship.getAttributeValue(context, "JFZeroPart");
                    if ("N".equalsIgnoreCase(strJFZeroPart)) {
                        //不是整椅，不做处理
                        continue;
                    }
                    //零件号
                    String number = partObject.getAttributeValue(context, JF_PLMConstants_mxJPO.ATTR_V_PART_NUMBER);
                    stringList.add(number + "|" + partId);
                    PartList.add(partId);
                    HashMap<String, String> partMap = new HashMap<>();
                    partMap.put(SELECT_NAME, number);
                    partMap.put(SELECT_ID, partId);
                    partMap.put(SELECT_REVISION, revision);
                    mapList.add(partMap);
                }
                if (!PartList.isEmpty()) {
                    ecrPartMap.put(ecrId, PartList);
                }
                if (!stringList.isEmpty()) {
                    ecrRootPartMap.put(ecrId, stringList);
                }
                if (!stringList.isEmpty() && !PartList.isEmpty()) {
                    hasPartEcrIdList.add(ecrId);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * 拿取整椅中最新版本的数据
     * @param context
     * @param hasPartEcrIdList
     * @param ecrPartMap
     * @param ecrRootPartMap
     * @param maxPartMap
     * @param ecrPartExecMap
     * @param ecrIdList
     * @author LIUJR
     * @throws
     * @return void
     * @date 2025/7/24 13:59
     * @description
     */
    public void getPartLatestVersion(Context context, StringList hasPartEcrIdList,
                                     HashMap<String, StringList> ecrPartMap,
                                     HashMap<String, StringList> ecrRootPartMap,
                                     HashMap<String, String> maxPartMap,
                                     HashMap<String, StringList> ecrPartExecMap,
                                     StringList ecrIdList
    ) throws Exception{
        try {
            for (int i = 0; i < hasPartEcrIdList.size(); i++) {
                //ecrId
                String ecrId = hasPartEcrIdList.get(i);
                StringList ecrPartList = ecrPartMap.get(ecrId);
                StringList ecrRootPartList = ecrRootPartMap.get(ecrId);
                for (int i1 = 0; i1 < ecrRootPartList.size(); i1++) {
                    String partIdAndRev = ecrRootPartList.get(i1);
                    String[] partIdAndRevSplit = partIdAndRev.split("\\|");
                    String number = partIdAndRevSplit[0];
                    String id = partIdAndRevSplit[1];
                    String maxPartId = maxPartMap.get(number);
                    if (!maxPartId.equalsIgnoreCase(id)) {
                        ecrPartList.remove(id);
                    }
                }
                if (!ecrPartList.isEmpty()) {
                    ecrPartExecMap.put(ecrId, ecrPartList);
                    ecrIdList.add(ecrId);
                }
            }
        }catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * 责任方、PM变更说明，AME变更说明的修改权限
     * @param context
     * @param args
     * @author LIUJR
     * @throws
     * @return java.lang.Boolean
     * @date 2025/8/29 14:20
     * @description
     */
    public StringList editResponsibleAccess(Context context, String[] args) throws Exception {
        StringList stringList = new StringList();
        try {
            Map programMap = (Map) JPO.unpackArgs(args);
            MapList objList = (MapList) programMap.get("objectList");
            Map requestMap = (Map) programMap.get("requestMap");
            String projectId = (String) requestMap.get("objectId");
            //获取项目的项目经理， 和总装AME
            String projectManagerId = JF_PublicMethodClass_mxJPO.getProjectWherePerson(context, projectId, SELECT_ID, "", "attribute[Project Role]=='Project manager'");
            String amePersonId = JF_PublicMethodClass_mxJPO.getProjectWherePerson(context, projectId, SELECT_ID, "", "attribute[Project Role]=='AME representative'");
            Map columnMap = (Map)programMap.get("columnMap");
            Map colAttrMap = (Map)columnMap.get("colAttrMap");
            //table列名称
            String strColName = (String)colAttrMap.get("name");
            String loginUser=context.getUser();
            String contextUserId = PersonUtil.getPersonObjectID(context, loginUser);
            JF_LOGGER.info("objList--->"+objList);
            DomainObject mbomObj = DomainObject.newInstance(context);
            for (int i = 0; i < objList.size(); i++) {
                String id = (String) ((Map) objList.get(i)).get("id");
                mbomObj.setId(id);
                String attributeValue = mbomObj.getAttributeValue(context, "JF_Responsible");
                String gx = mbomObj.getAttributeValue(context, "JF_PartType");
                //第一层
                if (i == 0 && "X".equalsIgnoreCase(gx)) {
                    //PM的时候
                    if ("PM".equalsIgnoreCase(attributeValue)) {
                        if (contextUserId.equalsIgnoreCase(projectManagerId)) {
                            if ("JF_AMEChangeDesc".equalsIgnoreCase(strColName)) {
                                stringList.add(Boolean.toString(false));
                            } else {
                                stringList.add(Boolean.toString(true));
                            }
                        } else {
                            stringList.add(Boolean.toString(false));
                        }
                    } else {
                        //AME
                        if (contextUserId.equalsIgnoreCase(amePersonId)) {
                            if ("JF_PMChangeDesc".equalsIgnoreCase(strColName)) {
                                stringList.add(Boolean.toString(false));
                            } else {
                                stringList.add(Boolean.toString(true));
                            }
                        } else {
                            stringList.add(Boolean.toString(false));
                        }
                    }
                } else {
                    stringList.add(Boolean.toString(false));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
        return stringList;
    }



    /**
     * 修改责任方
     * @param context
     * @param args
     * @author LIUJR
     * @throws
     * @return void
     * @date 2025/9/2 13:35
     * @description
     */
    public static void updateMBOMJFResponsible(Context context, String[] args) throws Exception {
        try {
            ContextUtil.pushContext(context);
            Map paramsMap = (Map) JPO.unpackArgs(args);
            JF_LOGGER.info("paramsMap:{}", paramsMap);
            Map columnMap = (Map) paramsMap.get(STRING_COLUMNMAP);
            String attrName = (String) columnMap.get(DomainConstants.SELECT_NAME);
            HashMap paramMap = (HashMap)paramsMap.get(STRING_PARAMMAP);
            HashMap requestMap = (HashMap)paramsMap.get(STRING_REQUESTMAP);
            String objectId = (String)paramMap.get(STRING_OBJECTID);
            String projectId = (String)requestMap.get(STRING_PARENTOID);
            String newValue = (String)paramMap.get(STRING_NEW_VALUE);
            DomainObject domainObject = DomainObject.newInstance(context, objectId);
            domainObject.setAttributeValue(context, attrName, newValue);
            sendResponsibleEmail(context, projectId, newValue);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        } finally {
            ContextUtil.popContext(context);
        }
    }


    /**
     * 修改责任方发邮件
     * @param context
     * @param projectId
     * @author LIUJR
     * @throws
     * @return void
     * @date 2025/12/16 9:36
     * @description
     */
    public static void  sendResponsibleEmail(Context context, String projectId, String responsible) throws Exception{
        try {
            DomainObject domainObject = DomainObject.newInstance(context);
            //项目
            domainObject.setId(projectId);
            String projectName = domainObject.getInfo(context, SELECT_NAME) + "(" + domainObject.getDescription(context) + ")";
            //获取ECR信息  通过项目找到变更记录，将变更记录进行排序，拿取ECR
            //项目关联的 所有的变更记录
            StringList selList = JF_Util_mxJPO.basicBolistSel();
            StringList relList = JF_Util_mxJPO.basicRellistSel();
            selList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JF_UpdateDate);
            selList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JFCHANGERESON);
            selList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JF_UpdatePerson);
            selList.add(SELECT_DESCRIPTION);
            MapList crMapList = domainObject.getRelatedObjects(context,
                    JF_PLMConstants_mxJPO.REL_JF_relProject2ChangeRecord, //pattern to match relationships
                    JF_PLMConstants_mxJPO.TYPE_JF_ChangeRecord, //pattern to match types
                    selList, //the eMatrix StringList object that holds the list of select statement pertaining to Business Obejcts.
                    relList, //the eMatrix StringList object that holds the list of select statement pertaining to Relationships.
                    false, //get To relationships
                    true, //get From relationships
                    (short) 1, //the number of levels to expand, 0 equals expand all.
                    EMPTY_STRING, //where clause to apply to objects, can be empty ""
                    null, //where clause to apply to relationship, can be empty ""
                    (short) 0);//limit
            //变更记录根据name倒叙排序
            crMapList.addSortKey(SELECT_NAME, ProgramCentralConstants.DESCENDING_SORT, ProgramCentralConstants.SORTTYPE_STRING);
            crMapList.sort();
            //找到变更记录关联的ECR
            DomainObject crObject = DomainObject.newInstance(context);
            String crid = EMPTY_STRING;
            String updateDateTime = EMPTY_STRING;
            String date = EMPTY_STRING;
            String time = EMPTY_STRING;
            StringList ecrIds = new StringList();
            MapList info = new MapList();
            for (int i = 0; i < crMapList.size(); i++) {
                Map map = (Map) crMapList.get(i);
                crid = UIUtil.getValue(map, SELECT_ID);
                updateDateTime = UIUtil.getValue(map, JF_PLMConstants_mxJPO.SELECT_ATTR_JF_UpdateDate);
                //获取update Date， Date time
                if(UIUtil.isNotNullAndNotEmpty(updateDateTime)){
                    // 解析输入日期
                    LocalDateTime dateTime = LocalDateTime.parse(updateDateTime, inputFormatter);
                    // 格式化输出日期
                    // 拆分并格式化日期部分和时间部分
                    date = dateTime.format(formatterDate);
                    time = dateTime.format(formatterTime);
                    map.put("date", date);
                    map.put("time", time);
                }
                crObject.setId(crid);
                //需要回滚的ecr
                ecrIds = crObject.getInfoList(context, "from[" + JF_PLMConstants_mxJPO.REL_JF_relChangeRecord2ECR + "].to.id");
                info = DomainObject.getInfo(context, ecrIds.toStringArray(), selList);
                StringBuilder stringBuilder = new StringBuilder();
                for (int i1 = 0; i1 < info.size(); i1++) {
                    Map map1 = (Map) info.get(i1);
                    stringBuilder.append(UIUtil.getValue(map1, SELECT_NAME) + " : " +UIUtil.getValue(map1, SELECT_ATTRIBUTE_TITLE));
                    stringBuilder.append("\n");
                }
                map.put("ecr", stringBuilder.toString());
            }
            //person 发邮件的人
            String personId = EMPTY_STRING;
            if ("PM".equalsIgnoreCase(responsible)) {
                //获取项目的项目经理， 和总装AME
                personId = JF_PublicMethodClass_mxJPO.getProjectWherePerson(context, projectId, SELECT_ID, "", "attribute[Project Role]=='Project manager'");
            } else if ("AME".equalsIgnoreCase(responsible)) {
                personId = JF_PublicMethodClass_mxJPO.getProjectWherePerson(context, projectId, SELECT_ID, "", "attribute[Project Role]=='AME representative'");
            }
            domainObject.setId(personId);
            String emailAddress = domainObject.getAttributeValue(context, DomainObject.ATTRIBUTE_EMAIL_ADDRESS);
            //发送邮件
            mbomStructureSendEmail(context, emailAddress, projectId, crMapList,
                    null,projectName, "ResponsibleEmail", "MBOM结构责任方设置通知", "Notice of MBOM Structure Responsibility Party Setting");

        }catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @Author Liuxg
     * @Description 获取MBOM同步记录
     * @Date 2025/9/24 9:46
     * @Param [context, args]
     * @return com.matrixone.apps.domain.util.MapList
     **/
    public MapList getSyncMBOMRecord(Context context, String[] args) throws Exception {
        try {
            StringList selList = JF_Util_mxJPO.basicBolistSel();
            StringList relList = JF_Util_mxJPO.basicRellistSel();
            Map paramsMap = (Map) JPO.unpackArgs(args);
            ContextUtil.pushContext(context);
            String strObjectId = (String) paramsMap.get(STRING_OBJECTID);
            JF_LOGGER.info("strObjectId:{}",strObjectId);
            DomainObject objectProject = DomainObject.newInstance(context, strObjectId);
            String MbomRootid=objectProject.getInfo(context,"from[JF_relProject2MBOM].to.id");
            if(UIUtil.isNotNullAndNotEmpty(MbomRootid)){
                DomainObject MbomRootObj=DomainObject.newInstance(context,MbomRootid);
                MapList mapList = MbomRootObj.getRelatedObjects(context,
//                    JF_PLMConstants_mxJPO.REL_JF_relProject2ChangeRecord, //pattern to match relationships
//                    JF_PLMConstants_mxJPO.TYPE_JF_ChangeRecord, //pattern to match types
                        "JF_relMBOM2SyncRecord",
                        "JF_SyncMBOMRecord",
                        selList, //the eMatrix StringList object that holds the list of select statement pertaining to Business Obejcts.
                        relList, //the eMatrix StringList object that holds the list of select statement pertaining to Relationships.
                        false, //get To relationships
                        true, //get From relationships
                        (short) 1, //the number of levels to expand, 0 equals expand all.
                        EMPTY_STRING, //where clause to apply to objects, can be empty ""
                        null, //where clause to apply to relationship, can be empty ""
                        (short) 0);//limit
                JF_LOGGER.info("mapList：{}", mapList);
                return mapList;
            }

        }catch (Exception e) {
            e.printStackTrace();
        }finally {
            ContextUtil.popContext(context);
        }
        return new MapList();
    }
    /*
     * @description: 特殊采购类型的编辑权限，只给总装AME
     * @author: caipan
     * @date: 2025/10/14 13:56:40
     * @param: * @param[1] context
     * @param[2] args
     * @return:
     **/
    public StringList editJF_SpecialProcurementTypeAccess(Context context, String[] args) throws Exception {
        StringList stringList = new StringList();
        try {
            Map programMap = (Map) JPO.unpackArgs(args);
            JF_LOGGER.info("programMap--->"+programMap);
            MapList objList = (MapList) programMap.get("objectList");
            Map requestMap = (Map) programMap.get("requestMap");
            String projectId = (String) requestMap.get("objectId");
            //判断当前的责任人是否是AME
            Boolean mbomRootResponsibleIsPM = getMBOMRootResponsibleIsPM(context, projectId);
            if (mbomRootResponsibleIsPM) {
                //当是责任人是PM的时候
                for (int i = 0; i < objList.size(); i++) {
                    stringList.add(Boolean.toString(false));
                }
            } else {
                //获取项目的项目经理， 和总装AME
//            String projectManagerId = JF_PublicMethodClass_mxJPO.getProjectWherePerson(context, projectId, SELECT_ID, "", "attribute[Project Role]=='Project manager'");
                String amePersonId = JF_PublicMethodClass_mxJPO.getProjectWherePerson(context, projectId, SELECT_ID, "", "attribute[Project Role]=='AME representative'");
                Map columnMap = (Map) programMap.get("columnMap");
                Map colAttrMap = (Map) columnMap.get("colAttrMap");
                //table列名称
                String strColName = (String) colAttrMap.get("name");
                String loginUser = context.getUser();
                String contextUserId = PersonUtil.getPersonObjectID(context, loginUser);
                JF_LOGGER.info("objList--->" + objList);
                DomainObject mbomObj = DomainObject.newInstance(context);
                for (int i = 0; i < objList.size(); i++) {
                    if ("JF_SpecialProcurementType".equalsIgnoreCase(strColName) && contextUserId.equalsIgnoreCase(amePersonId)) {
                        stringList.add(Boolean.toString(true));
                    } else {
                        stringList.add(Boolean.toString(false));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
        return stringList;
    }

    /**
     * 获取MBOM结构中的Root节点的责任方是否为PM，如果是 PM返回TRUE, 否则返回FALSE
     * @param context
     * @param projectId
     * @author LIUJR
     * @throws
     * @return java.lang.Boolean
     * @date 2025/12/2 15:26
     * @description
     */
    public Boolean getMBOMRootResponsibleIsPM(Context context, String projectId) throws Exception{
        Boolean flag = Boolean.TRUE;
        try {
            DomainObject domainObject = DomainObject.newInstance(context, projectId);
            String firstMBOMId = domainObject.getInfo(context, "from["+RELATIONSHIP_JF_relProject2MBOM+"].to.id");
            JF_LOGGER.info("firstMBOMId:{}", firstMBOMId);
            if (UIUtil.isNullOrEmpty(firstMBOMId)) {
                return flag;
            }
            domainObject.setId(firstMBOMId);
            String strJFResponsible = domainObject.getAttributeValue(context, "JF_Responsible");
            if (!"PM".equalsIgnoreCase(strJFResponsible)) {
                return Boolean.FALSE;
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
        return flag;
    }

    /*
     * @description:更新特殊采购类型，整个MBOM所有同号的零件
     * @author: caipan
     * @date: 2025/10/14 15:05:55
     * @param: * @param[1] context
     * @param[2] args
     * @return:
     **/
    public static void updateJF_SpecialProcurementTypeValue(Context context, String[] args) throws Exception {
        try {
            ContextUtil.pushContext(context);
            Map paramsMap = (Map) JPO.unpackArgs(args);
            Map columnMap = (Map) paramsMap.get(STRING_COLUMNMAP);
            String attrName = (String) columnMap.get(DomainConstants.SELECT_NAME);
            HashMap paramMap = (HashMap)paramsMap.get(STRING_PARAMMAP);
            String objectId = (String)paramMap.get(STRING_OBJECTID);
            String newValue = (String)paramMap.get(STRING_NEW_VALUE);
            DomainObject domainObject = DomainObject.newInstance(context, objectId);
//            domainObject.setAttributeValue(context, attrName, newValue);
            //拿到相同零件、相同版本的MBOM零件呢
            String revision = domainObject.getInfo(context, SELECT_REVISION);
            String name = domainObject.getInfo(context, "attribute[JF_PartNumber]");
            String where = "attribute[JF_PartNumber]=='"+name+"' && revision=='"+revision+"'";
            MapList list = DomainObject.findObjects(context,JF_PLMConstants_mxJPO.TYPE_JF_ManufacturedItem,"*",where,JF_Util_mxJPO.basicBolistSel());
//           JF_LOGGER.info("list:{}:{}",list,where);
            Map map = null;
            String id = "";
            for(int i=0;i<list.size();i++){
                map = (Map)list.get(i);
                id = UIUtil.getValue(map, SELECT_ID);
                domainObject.setId(id);
                domainObject.setAttributeValue(context, attrName, newValue);
            }

        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        } finally {
            ContextUtil.popContext(context);
        }
    }


    /**
     * 更新记录界面 获取MBOM 的json文件
     * @param context
     * @param args
     * @author LIUJR
     * @throws
     * @return java.util.Vector
     * @date 2025/10/31 11:15
     * @description
     */
    public Vector getChangeRecordMbomFile(Context context, String[] args) throws Exception{
        JF_LOGGER.info("method:getChangeRecordMbomJsonFile start...");

        Vector vector = new Vector();
        StringList strObjectIdList = JF_PublicMethodClass_mxJPO.getObjectIdList(args);
        try {
            Map paramsMap = (Map) JPO.unpackArgs(args);
            Map paramList = (Map) paramsMap.get("paramList");
            MapList objectList = (MapList)paramsMap.get(STRING_OBJECTLIST);
            DomainObject crObject = DomainObject.newInstance(context);
            if (!objectList.isEmpty()) {
                int iTemp = 0;
                for(int iSize = objectList.size(); iTemp < iSize; ++iTemp) {
                    Map objectMap = (Map)objectList.get(iTemp);
                    String id = UIUtil.getValue(objectMap, SELECT_ID);
                    crObject.setId(id);
                    FileList files = crObject.getFiles(context);
                    JF_LOGGER.info("id:{},files:{}", id, files);
                    if (CollectionUtils.isEmpty(files)) {
                        vector.add("");
                        continue;
                    }
                    String fileName = EMPTY_STRING;
                    for (int i = 0; i < files.size(); i++) {
                        matrix.db.File dbFile = files.get(i);
                        if (dbFile.getName().endsWith(".json")) {
                            fileName = dbFile.getName();
                            break;
                        }
                    }
                    JF_LOGGER.info("fileName:{}", fileName);
                    if (UIUtil.isNullOrEmpty(fileName)) {
                        vector.add("");
                        continue;
                    }
                    //拿到xlsx的文件后，开始显示
                    StringBuffer fileActionsStrBuff=new StringBuffer();
                    String downloadURL = "javascript:callCheckout('"+ XSSUtil.encodeForJavaScript(context, id) +"','download', '"+ XSSUtil.encodeForJavaScript(context, fileName)+ "', '" + XSSUtil.encodeForJavaScript(context, "generic") +"');";
                    fileActionsStrBuff.append("<a href=\"" + downloadURL+"\">"); //iconActionDownload.gif
                    fileActionsStrBuff.append("<img border='0' src='../common/images/iconSmallDocument.gif' alt=\""+XSSUtil.encodeForJavaScript(context, fileName)+"\" title=\""+XSSUtil.encodeForJavaScript(context, fileName)+"\"></img></a>&#160;");
                    vector.add(fileActionsStrBuff.toString());
                }
            }
        }catch (Exception e) {
            e.printStackTrace();
            for (String objectId : strObjectIdList) {
                vector.add(EMPTY_STRING);
            }
        }
        JF_LOGGER.info("method:getChangeRecordMbomJsonFile end...");
        return vector;
    }

    /**
     * 回滚 MBOM入口方法  JSON文件版
     * @param context
     * @param args
     * @author LIUJR
     * @throws
     * @return java.lang.Boolean
     * @date 2025/7/19 16:35
     * @description
     */
    public Boolean RollBakeMBOMStructureFromJson(Context context, String[] args) throws Exception {
        Boolean flag = Boolean.TRUE;
        Boolean isPop = Boolean.FALSE;
        try {
            Map paramsMap = (Map) JPO.unpackArgs(args);
            //变更记录id 每次单选
            String crId = UIUtil.getValue(paramsMap, "CRId");
            //项目id
            String projectId = UIUtil.getValue(paramsMap, "projectId");
            //查询集合
            StringList selList = JF_Util_mxJPO.basicBolistSel();
            StringList relList = JF_Util_mxJPO.basicRellistSel();
            selList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JF_UpdateDate);
            //提升权限
            ContextUtil.pushContext(context);
            isPop = Boolean.TRUE;
            //拿取变更记录关联的文件
            DomainObject crObject = DomainObject.newInstance(context);
            crObject.setId(crId);
            FileList files = crObject.getFiles(context);
            //没有文档
            if (CollectionUtils.isEmpty(files)){
                return Boolean.FALSE;
            }
            //取出json串文件
            String fileName = EMPTY_STRING;
            String fileFormat = EMPTY_STRING;
            String workspace = context.createWorkspace() + File.separator;
            for (int i = 0; i < files.size(); i++) {
                matrix.db.File dbFile = files.get(i);
                fileName = dbFile.getName();
                fileFormat = dbFile.getFormat();
                if (fileName.endsWith(".json")) {
                    break;
                }
            }
            //下载JSON文件
            crObject.open(context);
            crObject.checkoutFile(context, false, fileFormat, fileName, workspace);
            crObject.close(context);
            //打开json串文件
            String filePath = workspace + fileName;
            File file = new File(filePath);
            String jsonString = Files.readString(file.toPath(), StandardCharsets.UTF_8);
            JSONObject jsonObject = JSONObject.parseObject(jsonString);
            JSONObject firstMbom = (JSONObject) jsonObject.get("firstMbom");
            JSONArray parts = (JSONArray) jsonObject.get("Parts");
            JSONArray mbomObject = (JSONArray) jsonObject.get("MBOM");
            //将parts中改造成为： number_Rev : Json
            Map<String, JSONObject> resultMap = new HashMap<>();
            for (Object obj : parts) {
                JSONObject item = (JSONObject) obj;
                String partNumber = item.getString("JF_PartNumber");
                String revision = item.getString("revision");
                String key = partNumber + "_" + revision;
                resultMap.put(key, item);
            }
            //回滚的PartList
            StringList partNameList = new StringList();
            //所属项目
            DomainObject projectObject = DomainObject.newInstance(context, projectId);
            HashSet<String> emailSet = new HashSet<>();
            //获取项目中的AME人员
            emailSet.add(JF_PublicMethodClass_mxJPO.getProjectWherePerson(context, projectId, SELECT_NAME, "", "attribute[Project Role]=='AME representative'"));
            emailSet.add(JF_PublicMethodClass_mxJPO.getProjectWherePerson(context, projectId, SELECT_NAME, "", "attribute[Project Role]=='Foam AME representative'"));
            emailSet.add(JF_PublicMethodClass_mxJPO.getProjectWherePerson(context, projectId, SELECT_NAME, "", "attribute[Project Role]=='Trim AME representative'"));
            String projectNameAndDesc = projectObject.getInfo(context, DomainConstants.SELECT_NAME) + "(" + projectObject.getDescription(context) + ")";
            //获取项目中的MBOM的第一层级
            String firstMBOMId = projectObject.getInfo(context, "from["+RELATIONSHIP_JF_relProject2MBOM+"].to.id");
            DomainObject firstMBOMObject = DomainObject.newInstance(context, firstMBOMId);
            //删除第一层下的所有层级
            //获取MBOM的第二层级 整椅的层级
            HashSet allMbomList = (HashSet) firstMBOMObject.getRelatedObjects(
                    context,
                    REL_JF_relManufacturedItem,
                    TYPE_JF_ManufacturedItem,
                    selList,
                    relList,
                    false,
                    true,
                    (short) 0,
                    "",
                    "",
                    0
            ).stream().map(m -> {
                Map map = (Map) m;
                return UIUtil.getValue(map, SELECT_ID);
            }).collect(Collectors.toCollection(HashSet::new));
            HashMap<String, String> attributeMap = new HashMap<>();
            //遍历第一层级的所有 key  并记录属性  root节点的属性拿取
            for (Map.Entry<String, Object> entry : firstMbom.entrySet()) {
                String key = entry.getKey();
                if (DomainConstants.ATTRIBUTE_TITLE.equalsIgnoreCase(key) || key.startsWith("JF_") && !"JF_Dosage".equalsIgnoreCase(key) && !"JF_OriginateFlag".equalsIgnoreCase(key)) {
                    Object value = entry.getValue();
                    attributeMap.put(key,  value.toString());
                }
            }
            ContextUtil.startTransaction(context, true);
            //删除之前的整椅MBOM结构
            DomainObject.deleteObjects(context, StringList.create(allMbomList).toStringArray());
            //创建MBOM数据
//            Map newPartsMap = createRollBackPart(context, parts, projectId);
            //开始搭建结构
            connMbomStrucure(context, mbomObject, firstMBOMId, resultMap, partNameList, Boolean.TRUE);
            // 设置第一层级属性
            firstMBOMObject.setAttributeValues(context, attributeMap);
            //将回滚的ECR还原为未生成MBOM
            StringList ecrList = crObject.getInfoList(context, "from[" + JF_PLMConstants_mxJPO.REL_JF_relChangeRecord2ECR + "].to.id");
            HashSet<String> ecrSet = new HashSet<>();
            ecrSet.addAll(ecrList);
            //将当前Cr变更清单以后的清单全部拿出来，将其ECR改成待更新，删除更新清单
            Date updateDate = sdf.parse(crObject.getAttributeValue(context, ATTR_JF_UpdateDateTime));
            String crName = crObject.getInfo(context, SELECT_NAME);
            //项目关联的 所有的变更记录
            MapList crMapList = projectObject.getRelatedObjects(context,
                    JF_PLMConstants_mxJPO.REL_JF_relProject2ChangeRecord, //pattern to match relationships
                    JF_PLMConstants_mxJPO.TYPE_JF_ChangeRecord, //pattern to match types
                    selList, //the eMatrix StringList object that holds the list of select statement pertaining to Business Obejcts.
                    relList, //the eMatrix StringList object that holds the list of select statement pertaining to Relationships.
                    false, //get To relationships
                    true, //get From relationships
                    (short) 1, //the number of levels to expand, 0 equals expand all.
                    EMPTY_STRING, //where clause to apply to objects, can be empty ""
                    null, //where clause to apply to relationship, can be empty ""
                    (short) 0);//limit
            //变更记录根据name倒叙排序
            crMapList.addSortKey(SELECT_NAME, ProgramCentralConstants.DESCENDING_SORT, ProgramCentralConstants.SORTTYPE_STRING);
            crMapList.sort();
            JF_LOGGER.info("crMapList:{}", crMapList);
            //需要回滚的更新清单
            MapList collBackCRMapList = new MapList();
            for (int i = 0; i < crMapList.size(); i++) {
                Map map = (Map) crMapList.get(i);
                String datetime = UIUtil.getValue(map, JF_PLMConstants_mxJPO.SELECT_ATTR_JF_UpdateDate);
                Date date2 = sdf.parse(datetime);
                String name =  UIUtil.getValue(map, SELECT_NAME);
                //如果name在当前选择的变更记录后，并且更新日期大于当前选择的变更记录的更新日期，代表需要回滚
                if (date2.after(updateDate) && name.compareTo(crName) > 0) {
                    //需要回滚
                    collBackCRMapList.add(map);
                }
            }
            DomainObject domainObject = DomainObject.newInstance(context);
            for (int i = 0; i < collBackCRMapList.size(); i++) {
                Map map = (Map) collBackCRMapList.get(i);
                String crid = UIUtil.getValue(map, SELECT_ID);
                domainObject.setId(crid);
                //需要回滚的ecr
                StringList ecrIds = domainObject.getInfoList(context, "from[" + JF_PLMConstants_mxJPO.REL_JF_relChangeRecord2ECR + "].to.id");
                ecrSet.addAll(ecrIds);
                domainObject.deleteObject(context);
            }
            ecrList = StringList.create(ecrSet);
            for (int i = 0; i < ecrList.size(); i++) {
                String ecrId = ecrList.get(i);
                domainObject.setId(ecrId);
                domainObject.setAttributeValue(context, ATTR_JF_IsSyncMBOM, "No");
            }
            //删除Cr变更清单
            crObject.deleteObject(context);
//            rollBakeMBOMSendEmail(context, emailSet, projectId, ecrList, partNameList, projectNameAndDesc);
            ContextUtil.commitTransaction(context);
        }catch (Exception e) {
            e.printStackTrace();
            flag = Boolean.FALSE;
            ContextUtil.abortTransaction(context);
            throw e;
        } finally {
            if (isPop) {
                ContextUtil.popContext(context);
            }
        }
        return flag;
    }

    /**
     * 搭建回滚MBOM的结构
     * @param context
     * @param mbomObject
     * @param firstMBOMId
     * @param resultMap
     * @author LIUJR
     * @throws
     * @return void
     * @date 2025/11/20 11:02
     * @description
     */
    private void connMbomStrucure(Context context, JSONArray mbomObject, String firstMBOMId, Map<String, JSONObject> resultMap, StringList partNameList, Boolean flag) throws Exception{
        for (int i = 0; i < mbomObject.size(); i++) {
            JSONObject mbom = (JSONObject) mbomObject.get(i);
            String JF_PartNumber = (String) mbom.get("JF_PartNumber");
            String Dosage = (String) mbom.get("Dosage");
            String strJFOriginateFlag = (String) mbom.get("JF_OriginateFlag");
            String revision = (String) mbom.get("revision");
            if (flag) {
                partNameList.add(JF_PartNumber + "_" + revision);
            }
            JSONArray child = (JSONArray) mbom.get("child");
            String key = JF_PartNumber + "_" + revision;
            String mbomId =  createRollBackPart(context, resultMap.get(key));
            if (UIUtil.isNullOrEmpty(mbomId)) {
                continue;
            }
            //创建MBOM
            connectionMBOMSetFlag(context, firstMBOMId,mbomId,Dosage, strJFOriginateFlag);
            if (!child.isEmpty()) {
                connMbomStrucure(context, child, mbomId, resultMap, partNameList, Boolean.FALSE);
            }
        }
    }

    /**
     * mbom回滚新创建零件
     * @param context
     * @param part
     * @author LIUJR
     * @throws
     * @return java.util.Map
     * @date 2025/11/20 10:34
     * @description
     */
    public String createRollBackPart(Context context, JSONObject part) throws Exception{
        DomainObject mbomObj = DomainObject.newInstance(context);
        String mql2 = "mod bus $1 owner $2 organization $3 project $4;";
        HashMap<String, String> attributeMap = new HashMap<>();
        //遍历第一层级的所有 key  并记录属性
        if (part.isEmpty()) {
            return EMPTY_STRING;
        }
        for (Map.Entry<String, Object> entry : part.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            if (DomainConstants.ATTRIBUTE_TITLE.equalsIgnoreCase(key) || key.startsWith("JF_") && !"JF_Dosage".equalsIgnoreCase(key) && !"JF_OriginateFlag".equalsIgnoreCase(key)) {
                attributeMap.put(key,  value.toString());
            }
        }
        String owner = (String) part.get(SELECT_OWNER);
        String project = (String) part.get(SELECT_PROJECT);
        String revision = (String) part.get(SELECT_REVISION);
        String organization = (String) part.get(SELECT_ORGANIZATION);
        //开始创建
        String mbomId = FrameworkUtil.autoName(context,  "type_JF_ManufacturedItem", "policy_JF_ManufacturedItem");
        mbomObj.setId(mbomId);
        MqlUtil.mqlCommand(context, false, "mod bus "+mbomId+" revision '"+revision+"' name '"+mbomObj.getInfo(context, DomainConstants.SELECT_NAME)+"'", true);
        mbomObj.setAttributeValues(context, attributeMap);
        MqlUtil.mqlCommand(context,false,false,mql2,true,mbomId,owner,organization,project);
        return mbomId;
    }

    /**
     * 门幅 利用率同步
     * @param context
     * @param args
     * @author LIUJR
     * @throws
     * @return void
     * @date 2025/11/27 13:11
     * @description
     */
    public static void updateSynchronizeAttributesValue(Context context, String[] args) throws Exception {
        try {
            ContextUtil.pushContext(context);
            Map paramsMap = (Map) JPO.unpackArgs(args);
            JF_LOGGER.info("paramsMap:{}",paramsMap);
            Map columnMap = (Map) paramsMap.get(STRING_COLUMNMAP);
            String attrName = (String) columnMap.get(DomainConstants.SELECT_NAME);
            HashMap paramMap = (HashMap)paramsMap.get(STRING_PARAMMAP);
            String objectId = (String)paramMap.get(STRING_OBJECTID);
            String newValue = (String)paramMap.get(STRING_NEW_VALUE);
            Map requestMap = (Map) paramsMap.get("requestMap");
            //项目id
            String strParentOID = (String) requestMap.get("parentOID");
            DomainObject domainObject = DomainObject.newInstance(context, objectId);
            //拿到相同零件、相同版本的MBOM零件呢
            String revision = domainObject.getInfo(context, SELECT_REVISION);
            String name = domainObject.getInfo(context, "attribute[JF_PartNumber]");
            domainObject.setId(strParentOID);
            //root节点
            String firstMBOMId = domainObject.getInfo(context, "from["+RELATIONSHIP_JF_relProject2MBOM+"].to.id");
            if (UIUtil.isNullOrEmpty(firstMBOMId)) {
                return;
            }
            domainObject.setId(firstMBOMId);
            MapList mapList = domainObject.getRelatedObjects(
                    context,
                    REL_JF_relManufacturedItem,
                    TYPE_JF_ManufacturedItem,
                    StringList.create("attribute[JF_PartNumber]", SELECT_REVISION, SELECT_ID),
                    JF_Util_mxJPO.basicRellistSel(),
                    false,
                    true,
                    (short) 0,
                    "",
                    "",
                    0
            );
            JF_LOGGER.info("mapList:{}",mapList);
            mapList = (MapList) mapList.stream().filter(m -> {
                Map map = (Map) m;
                String number = UIUtil.getValue(map, "attribute[JF_PartNumber]");
                String rev = UIUtil.getValue(map, SELECT_REVISION);
                JF_LOGGER.info("number:{}",number);
                JF_LOGGER.info("rev:{}",rev);
                if (number.equalsIgnoreCase(name) && rev.equalsIgnoreCase(revision)) {
                    return Boolean.TRUE;
                } else {
                    return Boolean.FALSE;
                }
            }).collect(Collectors.toCollection(MapList::new));
            JF_LOGGER.info("mapList:{}",mapList);
            Map map = null;
            String id = "";
            for(int i=0;i<mapList.size();i++){
                map = (Map)mapList.get(i);
                id = UIUtil.getValue(map, SELECT_ID);
                domainObject.setId(id);
                domainObject.setAttributeValue(context, attrName, newValue);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        } finally {
            ContextUtil.popContext(context);
        }
    }


    /**
     * @Author Liuxg
     * @Description 获取工厂
     * @Date 2025/11/27 16:33
     * @Param [context, args]
     * @return matrix.util.StringList
     **/
    public StringList getSyncMBOMRecordFactory(Context context,String[] args) throws Exception{
        StringList res = new StringList();
        try {
            JF_LOGGER.info("getSyncRecordDate:");
            Map argMaps = JPO.unpackArgs(args);
            JF_LOGGER.info("getSyncRecordDate:{}", argMaps);
            MapList argMapList = (MapList) argMaps.get(JF_PLMConstants_mxJPO.STRING_OBJECTLIST);
            Map columnMap = (Map) argMaps.get("columnMap");
            String colName = (String) columnMap.get(SELECT_NAME);
            JF_LOGGER.info("colName:{}", colName);
            DomainObject domainObject = DomainObject.newInstance(context);
            ContextUtil.pushContext(context);
            for (int i = 0; i < argMapList.size(); i++) {
                Map infoMap = (Map) argMapList.get(i);
                String strId = (String) infoMap.get(SELECT_ID);
                domainObject.setId(strId);
                String updateDateTime = domainObject.getAttributeValue(context, "JF_SyncMBOMRecordFactory");

            }
            JF_LOGGER.info("res:{}", res);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        } finally {
            ContextUtil.popContext(context);
        }
        return res;
    }

    /**
     * 回滚MBOM的权限
     * @param context
     * @param args
     * @author LIUJR
     * @throws
     * @return java.lang.Boolean
     * @date 2025/12/2 17:06
     * @description
     */
    public Boolean RollBakeMBOMAccess(Context context, String[] args) {
        Boolean flag = Boolean.TRUE;
        try{
            JF_LOGGER.info("@@@@@@@@@@@@@@@@@@@");
            Map params = JPO.unpackArgs(args);
            String user = context.getUser().toString();
            //回滚按钮权限设置  20251205 回滚按钮权限调整：普通用户无法看见，仅管理员admin可以看见
            if (!"admin_platform".equalsIgnoreCase(user)) {
                flag = Boolean.FALSE;
            }
        } catch (Exception e) {
            e.printStackTrace();
            flag = Boolean.FALSE;
        }
        return flag;
    }


    /**
     * MBOM页面移除供货件的MBOM结构  需要判断勾选的整椅件是否在供货件清单中
     * @param context
     * @param args
     * @author LIUJR
     * @throws
     * @return java.lang.Boolean
     * @date 2025/12/25 16:50
     * @description
     */
    public Boolean getRemoveZeroPartMBOMAccess(Context context, String[] args) throws Exception{
        Boolean flag = Boolean.TRUE;
        try{
            JF_LOGGER.info("@@@@@@@@@@@@@@@@@@@");
            Map params = JPO.unpackArgs(args);
            String strObjectId = (String) params.get("objectId");
            DomainObject domainObject = DomainObject.newInstance(context, strObjectId);
            String firstMBOMId = domainObject.getInfo(context, "from["+RELATIONSHIP_JF_relProject2MBOM+"].to.id");
            JF_LOGGER.info("firstMBOMId:{}", firstMBOMId);
            if (UIUtil.isNullOrEmpty(firstMBOMId)) {
                return Boolean.FALSE;
            }
            domainObject.setId(firstMBOMId);
            String strJFResponsible = domainObject.getAttributeValue(context, "JF_Responsible");
            if ("PM".equalsIgnoreCase(strJFResponsible)) {
                return Boolean.FALSE;
            }
            //判断当前登录人是否是总装AME
            String contextUser = context.getUser().toString();
            String ameUser = JF_PublicMethodClass_mxJPO.getProjectWherePerson(context, strObjectId, SELECT_NAME, "", "attribute[Project Role]=='AME representative'");
            if (!contextUser.equalsIgnoreCase(ameUser)) {
                return Boolean.FALSE;
            }
            JF_LOGGER.info("params:{}",params);
            String user = context.getUser().toString();
        } catch (Exception e) {
            e.printStackTrace();
            flag = Boolean.FALSE;
        }
        return flag;
    }


    /*
     * @description:table 展示列 零件的详细类型JF_VPMReference.JF_Detail_CN
     * @author: caipan
     * @date: 2026/2/2 14:12:11
     * @param: * @param[1] context
     * @param[2] args
     * @return:
     **/
    public Vector tableViewColumnFromObjectMapList(Context context, String[] args)
            throws Exception {
        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        Map columnMap = (Map) programMap.get("columnMap");
        String columnName = (String)columnMap.get("name");
        MapList objectList = (MapList) programMap.get("objectList");
        Vector retVector = new Vector();
        if (objectList != null && objectList.size() > 0) {
            Map objectMap = null;
            String displayName = "";
            for (int i = 0; i < objectList.size(); i++) {
                objectMap  = (Map)objectList.get(i);
                String name = UIUtil.getValue(objectMap, "attribute[Title]");
                String revision = UIUtil.getValue(objectMap, SELECT_REVISION);
                //查询MBOM对应的零件类型
                String mql = "revision=='"+revision+"' && attribute[EnterpriseExtension.V_PartNumber]=='"+name+"'";
                MapList list = DomainObject.findObjects(context,JF_PLMConstants_mxJPO.TYPE_VPMReference,"*",mql,new StringList("attribute[JF_VPMReference.JF_Detail_CN]"));
                JF_LOGGER.info("mql:{} list：{}",mql,list);
                if(list.size()>0){
                    Map m = (Map)list.get(0);
                    displayName = UIUtil.getValue(m,"attribute[JF_VPMReference.JF_Detail_CN]");
                }
                JF_LOGGER.info("displayName:{}",displayName);
                retVector.add(displayName);
            }
        }
        return retVector;
    }

    /**
     * 变更记录  里面显示该ECR是否含有冒泡结构
     * @param context
     * @param args
     * @author LIUJR
     * @throws
     * @return com.matrixone.apps.domain.util.MapList
     * @date 2026/3/20 14:23
     * @description
     */
    public StringList getECRPartBubbleFlag(Context context, String[] args) throws Exception {
        Map argMaps = JPO.unpackArgs(args);
        StringList res = new StringList();
        Map paramListMap = (Map) argMaps.get(STRING_PARAMLIST);
        String strObjectId = (String) paramListMap.get(STRING_OBJECTID);
        MapList argMapList = (MapList) argMaps.get(JF_PLMConstants_mxJPO.STRING_OBJECTLIST);
        DomainObject domainObject = DomainObject.newInstance(context);
        domainObject.setId(strObjectId);
        StringList projectZeroPartList = JF_PublicMethodClass_mxJPO.getProjectObjectZeroPart(context, domainObject);
        String mess1 = EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", context.getLocale(), "emxComponents.BubbleFlag.AllZeroPart");
        String mess2 = EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", context.getLocale(), "emxComponents.BubbleFlag.HalfZeroPart");
        String mess3 = EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", context.getLocale(), "emxComponents.BubbleFlag.ZeroZeroPart");
        //获取该MBOM的项目 的供货件
        for (int i = 0; i < argMapList.size(); i++) {
            Map infoMap = (Map) argMapList.get(i);
            String strId = (String) infoMap.get(SELECT_ID);
            String strName = (String) infoMap.get(SELECT_NAME);
            String mess = EMPTY_STRING;
            domainObject.setId(strId);
            StringList rootItemList = domainObject.getInfoList(context, "from[" + JF_PLMConstants_mxJPO.REL_JFECRRelateRoot + "].to.id");
            StringList rootBubbleList = domainObject.getInfoList(context, "from[" + JF_PLMConstants_mxJPO.REL_JFECRRelateRootBubble + "].to.id");
            StringList  rootItemPartList = rootItemList.stream()
                    .filter(projectZeroPartList::contains)
                    .collect(Collectors.toCollection(StringList::new));
            StringList rootBubblePartList = rootBubbleList.stream()
                    .filter(projectZeroPartList::contains)
                    .collect(Collectors.toCollection(StringList::new));
            if (rootItemPartList.isEmpty() && !rootBubblePartList.isEmpty()) {
                mess = mess1;
            } else if (!rootItemPartList.isEmpty() && rootBubblePartList.isEmpty()) {
                mess = mess3;
            } else if (!rootItemPartList.isEmpty() && !rootBubblePartList.isEmpty()) {
                mess = mess2;
            }
            JF_LOGGER.info("mess:{}", mess);
            res.add(mess);
        }
        JF_LOGGER.info("res:{}", res);
        return res;
    }

    /**
     * 获取变更记录的Title
     * @param context
     * @param args
     * @author LIUJR
     * @throws
     * @return matrix.util.StringList
     * @date 2026/3/30 16:13
     * @description
     */
    public StringList getChangeRecordTitle(Context context, String[] args) throws Exception{
        StringList res = new StringList();
        try {
            JF_LOGGER.info("getChangeRecordTitle: start");
            Map argMaps = JPO.unpackArgs(args);
            MapList argMapList = (MapList) argMaps.get(JF_PLMConstants_mxJPO.STRING_OBJECTLIST);
            DomainObject domainObject = DomainObject.newInstance(context);
            ContextUtil.pushContext(context);
            for (int i = 0; i < argMapList.size(); i++) {
                Map infoMap = (Map) argMapList.get(i);
                String strId = (String) infoMap.get(SELECT_ID);
                domainObject.setId(strId);
                String title = domainObject.getAttributeValue(context, DomainConstants.ATTRIBUTE_TITLE);
                title = title.replaceAll(";", "<br/>");
                res.add(title);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        } finally {
            ContextUtil.popContext(context);
        }
        return res;
    }

    /**
     * 未/已更新ECR清单中显示每个ECR需要更新的整椅件（供货件）
     * @param context
     * @param args
     * @author LIUJR
     * @throws
     * @return matrix.util.StringList
     * @date 2026/03/31 14:25
     * @description
     */
    public StringList getECRRootPartList(Context context,String[] args) throws Exception{
        Map argMaps = JPO.unpackArgs(args);
        StringList res = new StringList();
        try {
            MapList argMapList = (MapList) argMaps.get(JF_PLMConstants_mxJPO.STRING_OBJECTLIST);
            Map paramListMap = (Map) argMaps.get(STRING_PARAMLIST);
            String strObjectId = (String) paramListMap.get(STRING_OBJECTID);
            DomainObject domainObject = DomainObject.newInstance(context);
            domainObject.setId(strObjectId);
            //找到改项目的所有的供货件
            StringList projectZeroPartList = JF_PublicMethodClass_mxJPO.getProjectObjectZeroPart(context, domainObject);
            String strId = EMPTY_STRING;
            JF_LOGGER.info("argMapList:{}", argMapList);
            for (int i = 0; i < argMapList.size(); i++) {
                Map infoMap = (Map) argMapList.get(i);
                strId = (String) infoMap.get(SELECT_ID);
                String mess = EMPTY_STRING;
                domainObject.setId(strId);
                StringList rootItemList = domainObject.getInfoList(context, "from[" + JF_PLMConstants_mxJPO.REL_JFECRRelateRoot + "].to.id");
                StringList rootBubbleList = domainObject.getInfoList(context, "from[" + JF_PLMConstants_mxJPO.REL_JFECRRelateRootBubble + "].to.id");
                StringList  rootItemPartList = rootItemList.stream()
                        .filter(projectZeroPartList::contains)
                        .collect(Collectors.toCollection(StringList::new));
                StringList rootBubblePartList = rootBubbleList.stream()
                        .filter(projectZeroPartList::contains)
                        .collect(Collectors.toCollection(StringList::new));
                rootItemPartList.addAll(rootBubblePartList);
                JF_LOGGER.info("rootItemPartList:{}", rootItemPartList);
                for (int i1 = 0; i1 < rootItemPartList.size(); i1++) {
                    strId = rootItemPartList.get(i1);
                    domainObject.setId(strId);
                    mess += domainObject.getAttributeValue(context, JF_PLMConstants_mxJPO.ATTR_V_PART_NUMBER) + "-" +domainObject.getInfo(context, SELECT_REVISION) + "<br/>";
                }
                JF_LOGGER.info("html:{}", mess);
                res.add(mess);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        JF_LOGGER.info("res:{}", res);
        return res;
    }

    /**
     * 主线程并行执行多个整椅更新任务，并汇总待关联公共根节点的新建整椅MBOM
     * @param context
     * @param mapList
     * @param map
     * @param ecrId
     * @param programJPO
     * @param method
     * @author LIUJR
     * @return java.util.Map<java.lang.String,java.lang.Object>
     * @date 2026/7/24 15:33
     * @description
     */
    public Map<String, Object> mainProcess(Context context, MapList mapList, Map<String, Object> map,  String ecrId, String programJPO, String method) {
        ThreadLog.info("=== 主线程启动 ===");
        ThreadLog.info("主线程：所有任务已提交，正在等待子线程返回结果...");
        Instant start = Instant.now();
        Map<String, Object> returnMap = new HashMap<>();
        MapList rootConnectList = new MapList();
        returnMap.put("flag", "N");
        returnMap.put("rootConnectList", rootConnectList);
        try {
            // 1. 准备一个列表，用来收集所有子线程的“未来结果” (Future)
            List<CompletableFuture<Map>> futureList = new ArrayList<>();
            // 2. 循环 ECR 列表
            for (int i = 0; i < mapList.size(); i++) {
                Map rootPartMap = (Map) mapList.get(i);
                Map safeEcrMap = new HashMap(rootPartMap);
                Map<String, Object> mapCopy = new HashMap<>(map);
                mapCopy.put("rootPartMap", safeEcrMap);
                mapCopy.put("ecrId", ecrId);
                // 调用子线程方法，并获取返回的 Future 对象
                // 注意：这里使用的是 supplyAsync，因为它支持有返回值
                CompletableFuture<Map> future = subThreadProcess(context,mapCopy, programJPO, method);
                // 将这个“凭证”收集起来
                futureList.add(future);
            }
            // 3. 等待所有子线程执行完毕，并获取返回值
            // 等待所有任务完成 (阻塞点)
            // 这里我们使用 join 来确保所有任务都跑完
            ThreadLog.info("主线程使用 join 来确保所有任务都跑完.................");
            CompletableFuture<Void> allFutures = CompletableFuture.allOf(
                    futureList.toArray(new CompletableFuture[0])
            );
            allFutures.join(); // 阻塞主线程，直到所有子线程结束

            // 4. 收集并判断返回值
            // 当代码走到这里，说明所有子线程都已经执行完了
            ThreadLog.info("主线程使用 join..所有子线程都已经执行完了...............");
            String numberAndRev = DomainConstants.EMPTY_STRING;
            String flag = DomainConstants.EMPTY_STRING;
            StringList stringList = new StringList();
            for (CompletableFuture<Map> future : futureList) {
                // 获取每个子线程的返回值 (因为已经 join 过了，这里 get 不会阻塞，只是取值)
                Map result = future.get();
                if (result == null || result.isEmpty()) {
                    continue;
                }
                numberAndRev = (String) result.get("numberAndRev");
                flag = (String) result.get("flag");
                if ("Y".equalsIgnoreCase(flag)) {
                    stringList.add(numberAndRev);
                    if ("Y".equalsIgnoreCase(UIUtil.getValue(result, "needConnectRoot"))) {
                        //20260724 update by ljr 汇总新建整椅MBOM，待所有线程成功后由主线程串行关联公共根节点；
                        rootConnectList.add(result);
                    }
                }
            }
            if (stringList.size() == mapList.size()) {
                returnMap.put("flag", "Y");
                returnMap.put("numberAndRev", stringList);
            }

        } catch (Exception e) {
            ThreadLog.info("等待过程中发生异常: " + Arrays.toString(e.getStackTrace()));
        }
        Instant end = Instant.now();
        Duration duration = Duration.between(start, end);
        ThreadLog.error("主线程中子线程耗时:{}", duration.getSeconds());
        ThreadLog.info("=== 主线程结束 ===");
        return returnMap;
    }

    /**
     * 子线程调用指定JPO方法并返回处理结果
     * @param context
     * @param mapCopy 子线程参数
     * @param programJPO JPO名称
     * @param method 方法名称
     * @return java.util.concurrent.CompletableFuture<java.util.Map>
     * @author LIUJR
     * @date 2026/7/24 16:00
     */
    private CompletableFuture<Map> subThreadProcess(Context context, Map<String, Object> mapCopy, String programJPO, String method) {
        ThreadLog.info("启动异步线程{}:{}", programJPO, method);
        //20260806 update by caipan 记录MBOM异步任务提交、开始和完成时间，便于分析线程池排队情况
        final Instant submittedAt = Instant.now();
        ThreadLog.info("MBOM异步任务【{}:{}】提交时间:{}", programJPO, method, submittedAt);
        // ===================== 【核心：统一处理参数 + 完美打印】 =====================
        final AtomicBoolean isSuccess = new AtomicBoolean(false);
        // 使用 supplyAsync 替代 runAsync，它允许返回泛型结果
        //20260806 update by caipan MBOM子任务使用独立线程池，避免占用平台共享线程池
        return CompletableFuture.supplyAsync(() -> {
            Instant startedAt = Instant.now();
            ThreadLog.info("MBOM异步任务【{}:{}】实际开始时间:{}，排队耗时:{}ms",
                    programJPO, method, startedAt,
                    Duration.between(submittedAt, startedAt).toMillis());
            Map<String, Object> paramMap = new HashMap<>(mapCopy);
            Context threadSafeContext = null;
            ThreadLog.info("子线程开始执行。。。。。。。。。。。。。。");
            try {
                threadSafeContext = context.getFrameContext();
                Map map = (Map) JPO.invoke(threadSafeContext, programJPO,  new String[]{}, method, JPO.packArgs(paramMap), Map.class);
                isSuccess.set(true);
                return map;
            } catch (Exception e) {
                ThreadLog.error("子线程 捕获到异常 处理数据时出错:{}:{}", programJPO, method, e);
                return new HashMap<>(); // 异常时返回 0
            } finally {
                if (threadSafeContext != null) {
                    try {
                        //20260724 update by ljr 子线程结束后关闭FrameContext，避免数据库会话长期占用；
                        threadSafeContext.close();
                    } catch (Exception closeException) {
                        ThreadLog.error("关闭子线程Context失败", closeException);
                    }
                }
                Instant completedAt = Instant.now();
                ThreadLog.info("MBOM异步任务【{}:{}】完成时间:{}，执行耗时:{}ms",
                        programJPO, method, completedAt,
                        Duration.between(startedAt, completedAt).toMillis());
            }
        }, MBOM_EXECUTOR).whenComplete((unused, throwable) -> {
            if (throwable == null && isSuccess.get()) {
                ThreadLog.info("异步线程【{}:{}】执行完成 - 状态：成功", programJPO, method);
            } else {
                ThreadLog.info("异步线程【{}:{}】执行完成 - 状态：失败", programJPO, method);
            }
        });
    }

    /**
     * 按MBOM所在项目创建制造件，完整同步零件属性及项目对应的DirectBuy
     **
     * @param context 上下文
     * @param partId 零件ID
     * @param owner MBOM对象owner
     * @param projectId MBOM所在项目ID
     * @return String 新建MBOM对象ID
     * @throws Exception
     * @author LIUJR
     * @date 2026/7/24
     */
    public String createMBOMPartFormal(Context context, String partId, String owner, String projectId) throws Exception {
        JF_LOGGER.info("createMBOMPart start");
        DomainObject partObj = DomainObject.newInstance(context, partId);
        StringList partAttribute = JF_Util_mxJPO.basicBolistSel();
        partAttribute.add("attribute[EnterpriseExtension.V_PartNumber]");
        partAttribute.add("attribute[JF_VPMReference.JF_PartNameEN]");
        partAttribute.add("attribute[JF_VPMReference.JF_PartNameCN]");
        partAttribute.add("attribute[PLMEntity.V_Name]");
        partAttribute.add("attribute[JF_VPMReference.JF_PartType]");
        partAttribute.add("attribute[JF_VPMReference.JF_ProcurementType]");
        partAttribute.add("attribute[JF_VPMReference.JF_Unit]");
        partAttribute.add("attribute[JF_VPMReference.JF_PartDes]");
        partAttribute.add("attribute[JF_VPMReference.JF_PartENDes]");
        Map partValue = partObj.getInfo(context, partAttribute);
        //20260727 update by ljr MBOM初始化按项目读取关系DirectBuy，未维护客户零件/DB信息时按non-DB写入；
        partValue.put("JF_DirectBuy",
                JF_Util_mxJPO.getPartDirectBuyByProjectWithNonDBDefault(
                        context,
                        partId,
                        projectId));
        JF_LOGGER.info("MBOM属性同步");
        JF_LOGGER.info("partValue：{}", partValue);
        String mbomId = FrameworkUtil.autoName(context, "type_JF_ManufacturedItem", "policy_JF_ManufacturedItem");
        DomainObject mbomObj = DomainObject.newInstance(context, mbomId);
        String partRevision = partObj.getInfo(context, DomainConstants.SELECT_REVISION);
        MqlUtil.mqlCommand(context, false,
                "mod bus " + mbomId + " revision '" + partRevision + "' name '"
                        + mbomObj.getInfo(context, DomainConstants.SELECT_NAME) + "'",
                true);
        // 零件对象属性和按项目获取的DirectBuy统一转换并一次性写入MBOM对象。
        mbomObj.setAttributeValues(context, changeAttribute(context, partValue));
        JF_Util_mxJPO.changeowner(context, mbomId, partObj.getObjectId(context), owner);
        JF_LOGGER.info("createMBOMPart end {}", mbomId);
        return mbomId;
    }

    /**
     * 根据项目中已发布的供货件获取最新发布版本（包含冒泡版本），并初始化缺失的MBOM结构
     **
     * @param context 上下文
     * @param args args[0] 项目ID
     * @return void
     * @throws Exception
     * @author LIUJR
     * @date 2026/7/27
     */
    public void initProjectLatestReleasedMBOM(Context context, String[] args) throws Exception {
        long startTime = System.currentTimeMillis();
        boolean transactionStarted = false;
        String projectId = args != null && args.length > 0 ? args[0] : EMPTY_STRING;
        String rootId = EMPTY_STRING;
        int releasedPartCount = 0;
        int latestReleasedPartCount = 0;
        int skippedPartCount = 0;
        int initializedPartCount = 0;
        try {
            if (UIUtil.isNullOrEmpty(projectId)) {
                return;
            }
            DomainObject projectObject = DomainObject.newInstance(context, projectId);
            String projectType = projectObject.getInfo(context, SELECT_TYPE);
            if (!DomainConstants.TYPE_PROJECT_SPACE.equals(projectType)) {
                return;
            }
            transactionStarted = true;
            //20260727 update by ljr 复用公共方法，仅查询项目关系上已发布的供货件并且是所属件，工作中、冻结等状态不参与MBOM初始化；
            MapList releasedPartMapList =
                    JF_PublicMethodClass_mxJPO.getProjectReleasedZeroBelongPart(context, projectObject);
            releasedPartCount = releasedPartMapList.size();

            //20260727 update by ljr 从每个已发布供货件的版本族中获取最新发布版本；使用非排除方法，确保冒泡版本也参与比较；
            Set<String> latestReleasedPartIdSet = new LinkedHashSet<>();
            for (int i = 0; i < releasedPartMapList.size(); i++) {
                Map partMap = (Map) releasedPartMapList.get(i);
                String partId = UIUtil.getValue(partMap, SELECT_ID);
                if (latestReleasedPartIdSet.contains(partId)) {
                    //已经是最新发布版本了
                    continue;
                }
                String latestReleasedPartId = JF_Util_mxJPO.getLastReleasedMajorid(context, partId);
                if (UIUtil.isNullOrEmpty(latestReleasedPartId)) {
                    continue;
                }
                latestReleasedPartIdSet.add(latestReleasedPartId);
            }
            latestReleasedPartCount = latestReleasedPartIdSet.size();
            if (latestReleasedPartIdSet.isEmpty()) {
                transactionStarted = false;
                JF_LOGGER.info(
                        "项目最新发布供货件初始化MBOM完成，result:SKIPPED，projectId:{}, releasedPartCount:{}, " +
                                "latestReleasedPartCount:0, initializedPartCount:0, cost:{}ms",
                        projectId, releasedPartCount, System.currentTimeMillis() - startTime);
                return;
            }
            ContextUtil.startTransaction(context, true);
            //判断MBOM根节点是否有，没有的话需要新建
            StringList rootIdList = projectObject.getInfoList(
                    context, "from[" + RELATIONSHIP_JF_relProject2MBOM + "].to.id");
            if (rootIdList.isEmpty()) {
                String projectManager = JF_Util_mxJPO.getProjectManager(context, new String[]{projectId});
                rootId = createMBOMGX(context, projectObject, projectManager);
            } else {
                rootId = rootIdList.get(0);
            }
            //获取当前MBOM根节点下的MBOM整椅
            DomainObject rootObject = DomainObject.newInstance(context, rootId);
            StringList mbomSelectList = JF_Util_mxJPO.basicBolistSel();
            mbomSelectList.add("attribute[JF_PartNumber]");
            MapList existingRootMBOMList = rootObject.getRelatedObjects(
                    context,
                    JF_PLMConstants_mxJPO.REL_JF_relManufacturedItem,
                    JF_PLMConstants_mxJPO.TYPE_JF_ManufacturedItem,
                    mbomSelectList,
                    new StringList(),
                    false,
                    true,
                    (short) 1,
                    EMPTY_STRING,
                    EMPTY_STRING,
                    (short) 0);
            Set<String> existingRootMBOMKeySet = new HashSet<>();
            for (int i = 0; i < existingRootMBOMList.size(); i++) {
                Map mbomMap = (Map) existingRootMBOMList.get(i);
                String partNumber = UIUtil.getValue(mbomMap, "attribute[JF_PartNumber]");
                String revision = UIUtil.getValue(mbomMap, SELECT_REVISION);
                existingRootMBOMKeySet.add(partNumber + "|" + revision);
            }

            mbomSelectList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_V_PART_NUMBER);
            MapList latestReleasedPartMapList = DomainObject.getInfo(
                    context,
                    latestReleasedPartIdSet.toArray(new String[latestReleasedPartIdSet.size()]),
                    mbomSelectList);
            StringList pendingInitPartIdList = new StringList();
            Set<String> pendingInitPartKeySet = new HashSet<>();
            for (int i = 0; i < latestReleasedPartMapList.size(); i++) {
                Map latestPartMap = (Map) latestReleasedPartMapList.get(i);
                String latestPartId = UIUtil.getValue(latestPartMap, SELECT_ID);
                String partNumber = UIUtil.getValue(
                        latestPartMap, JF_PLMConstants_mxJPO.SELECT_ATTR_V_PART_NUMBER);
                String revision = UIUtil.getValue(latestPartMap, SELECT_REVISION);
                if (UIUtil.isNullOrEmpty(partNumber) || UIUtil.isNullOrEmpty(revision)) {
                    continue;
                }
                String partKey = (partNumber + "|" + revision);
                //20260727 update by ljr 相同零件号和版本的整椅MBOM已存在时跳过，防止方法重复执行后生成重复结构；
                if (existingRootMBOMKeySet.contains(partKey) || !pendingInitPartKeySet.add(partKey)) {
                    skippedPartCount++;
                    continue;
                }
                pendingInitPartIdList.add(latestPartId);
            }
            //开始初始化
            int rootMBOMCountBefore = existingRootMBOMList.size();
            initializedPartCount = pendingInitPartIdList.size();
            if (!pendingInitPartIdList.isEmpty()) {
                initMBOM(context, projectId, pendingInitPartIdList, rootId);
            }
            ContextUtil.commitTransaction(context);
            JF_LOGGER.info(
                    "项目最新发布供货件初始化MBOM成功，result:SUCCESS，projectId:{}, rootId:{}, " +
                            "releasedPartCount:{}, latestReleasedPartCount:{}, skippedPartCount:{}, " +
                            "initializedPartCount:{}, cost:{}ms",
                    projectId, rootId, releasedPartCount, latestReleasedPartCount, skippedPartCount,
                    initializedPartCount, System.currentTimeMillis() - startTime);
        } catch (Exception e) {
            try {
                ContextUtil.abortTransaction(context);
            } catch (Exception abortException) {
                e.addSuppressed(abortException);
            }
            throw e;
        }
    }

    /**
     * 按零件类型路由公共或面套专用MBOM创建方法
     **
     * @param context 上下文
     * @param partId 零件ID
     * @param partType 零件类型
     * @param owner MBOM对象owner
     * @param projectId MBOM所在项目ID
     * @param originalDosage EBOM原始用量
     * @return Map 新建MBOM对象ID及关系用量
     * @throws Exception 创建失败时抛出异常
     * @author LIUJR
     * @date 2026/8/19
     */
    private Map<String, String> createMBOMPartByType(Context context, String partId, String partType, String owner,
                                                      String projectId, String originalDosage) throws Exception {
        if (JF_PLMConstants_mxJPO.ATTR_JFPartType_RANGE_T.equalsIgnoreCase(partType)) {
            return createTrimMBOMPartFormal(context, partId, owner, projectId, originalDosage);
        }
        Map<String, String> resultMap = new HashMap<>();
        resultMap.put(SELECT_ID, createMBOMPartFormal(context, partId, owner, projectId));
        resultMap.put(TRIM_RESULT_DOSAGE, originalDosage);
        return resultMap;
    }

    /**
     * 创建面套制造件并计算初始化关系用量
     **
     * @param context 上下文
     * @param partId 面套零件ID
     * @param owner MBOM对象owner
     * @param projectId MBOM所在项目ID
     * @param originalDosage EBOM原始用量
     * @return Map 新建MBOM对象ID及转换后的关系用量
     * @throws Exception 创建或属性转换失败时抛出异常
     * @author LIUJR
     * @date 2026/8/19
     */
    public Map<String, String> createTrimMBOMPartFormal(Context context, String partId, String owner,
                                                        String projectId, String originalDosage) throws Exception {
        JF_LOGGER.info("createTrimMBOMPartFormal start, partId:{}", partId);
        DomainObject partObj = DomainObject.newInstance(context, partId);
        Map<String, String> partValue = getTrimPartAttributeValues(context, partId, null);
        partValue.put(JF_PLMConstants_mxJPO.Attr_JF_DirectBuy,
                JF_Util_mxJPO.getPartDirectBuyByProjectWithNonDBDefault(context, partId, projectId));

        String mbomId = FrameworkUtil.autoName(context, "type_JF_ManufacturedItem", "policy_JF_ManufacturedItem");
        DomainObject mbomObj = DomainObject.newInstance(context, mbomId);
        String partRevision = partObj.getInfo(context, DomainConstants.SELECT_REVISION);
        MqlUtil.mqlCommand(context, false,
                "mod bus " + mbomId + " revision '" + partRevision + "' name '"
                        + mbomObj.getInfo(context, DomainConstants.SELECT_NAME) + "'",
                true);
        mbomObj.setAttributeValues(context, changeTrimAttribute(context, partValue));
        JF_Util_mxJPO.changeowner(context, mbomId, partObj.getObjectId(context), owner);

        Map<String, String> resultMap = new HashMap<>();
        resultMap.put(SELECT_ID, mbomId);
        resultMap.put(TRIM_RESULT_DOSAGE, calculateTrimMBOMDosage(context, partValue, originalDosage));
        JF_LOGGER.info("createTrimMBOMPartFormal end, mbomId:{}", mbomId);
        return resultMap;
    }

    /**
     * 将面套零件属性映射为MBOM属性并执行面套专用替换
     **
     * @param context 上下文
     * @param partValue 面套零件属性
     * @return Map MBOM对象属性
     * @throws Exception 卷料或配置查询失败时抛出异常
     * @author LIUJR
     * @date 2026/8/19
     */
    public Map<String, String> changeTrimAttribute(Context context, Map<String, String> partValue) throws Exception {
        Map<String, String> newMap = new HashMap<>();
        //20260819 update by ljr 复用静态面套属性映射，避免每次转换重复创建Map。
        for (Map.Entry<String, String> entry : partValue.entrySet()) {
            String oldKey = entry.getKey();
            String targetKey = TRIM_ATTRIBUTE_MAPPING.get(oldKey);
            if (targetKey != null) {
                newMap.put(targetKey, entry.getValue());
            }
        }
        if (!JF_PLMConstants_mxJPO.ATTR_JFPartType_RANGE_T.equalsIgnoreCase(
                UIUtil.getValue(partValue, JF_PLMConstants_mxJPO.SELECT_ATTR_JFPartType))) {
            return newMap;
        }

        setTrimDefaultAttributes(newMap, TRIM_DEFAULT_ATTRIBUTE_LIST);
        String detailType = UIUtil.getValue(partValue, JF_PLMConstants_mxJPO.SELECT_ATTR_JF_Detail_CN);
        boolean isSamplePackage = isConfiguredTrimDetail(context, CONFIG_KEY_DETAIL_TYPE_T06, detailType);
        String procurementType = UIUtil.getValue(partValue, JF_PLMConstants_mxJPO.SELECT_ATTR_JF_ProcurementType);
        if (isConfiguredTrimDetail(context, CONFIG_KEY_DETAIL_TYPE, detailType)
                && JF_PLMConstants_mxJPO.ATTR_JF_ProcurementType_RANGE_MAKE.equalsIgnoreCase(procurementType)) {
            String rollPartNumber = UIUtil.getValue(partValue, JF_PLMConstants_mxJPO.SELECT_ATTR_RollPartNumber);
            JF_LOGGER.info("!!!!!!!!!!!!rollPartNumber:{}", rollPartNumber);
            Map<String, String> rollPartMap = findTrimRollPart(context, rollPartNumber);
            JF_LOGGER.info("!!!!!!!!!!!!rollPartMap:{}", rollPartMap);
            if (!rollPartMap.isEmpty()) {
                newMap.put(JF_PLMConstants_mxJPO.ATTR_JF_ROLL_PART_NUMBER,
                        UIUtil.getValue(rollPartMap, JF_PLMConstants_mxJPO.SELECT_ATTR_V_PART_NUMBER));
                newMap.put(JF_PLMConstants_mxJPO.ATTR_JF_MBOM_PART_NAME_CN,
                        UIUtil.getValue(rollPartMap, JF_PLMConstants_mxJPO.SELECT_ATTR_JF_PartNameCN));
                newMap.put(JF_PLMConstants_mxJPO.ATTR_JF_MBOM_PART_NAME_EN,
                        UIUtil.getValue(rollPartMap, JF_PLMConstants_mxJPO.SELECT_ATTR_JF_PartNameEN));
                newMap.put(JF_PLMConstants_mxJPO.ATTR_JF_MBOM_PROCUREMENT_TYPE,
                        getMbomTypeControlValue(context, CONFIG_KEY_DETAIL_TYPE_MAKE,
                                JF_PLMConstants_mxJPO.ATTR_JF_ProcurementType_RANGE_BUY));
                if (isSamplePackage) {
                    String fabricMaterialType = UIUtil.getValue(
                            rollPartMap, JF_PLMConstants_mxJPO.SELECT_ATTR_JF_FABRIC_MATERIAL_TYPE);
                    if (JF_PLMConstants_mxJPO.STRING_Y.equalsIgnoreCase(fabricMaterialType)) {
                        newMap.put(JF_PLMConstants_mxJPO.ATTR_JF_MBOM_UNIT, getMbomTypeControlValue(
                                context, CONFIG_KEY_DETAIL_TYPE_UNIT_T06_Y, "M"));
                    } else if (JF_PLMConstants_mxJPO.STRING_N.equalsIgnoreCase(fabricMaterialType)) {
                        newMap.put(JF_PLMConstants_mxJPO.ATTR_JF_MBOM_UNIT, getMbomTypeControlValue(
                                context, CONFIG_KEY_DETAIL_TYPE_UNIT_T06_N, "M2"));
                    }
                } else {
                    newMap.put(JF_PLMConstants_mxJPO.ATTR_JF_MBOM_UNIT,
                            getMbomTypeControlValue(context, CONFIG_KEY_DETAIL_TYPE_UNIT_OTHER, "M"));
                }
                JF_LOGGER.info("!!!!!!!!!!!!newMap:{}", newMap);
            } else if (!isSamplePackage) {
                newMap.put(JF_PLMConstants_mxJPO.ATTR_JF_MBOM_UNIT,
                        getMbomTypeControlValue(context, CONFIG_KEY_DETAIL_TYPE_UNIT_OTHER, "M"));
            }
        } else if (isConfiguredTrimDetail(context, CONFIG_KEY_DETAIL_OTHER, detailType)) {
            newMap.put(JF_PLMConstants_mxJPO.ATTR_JF_MBOM_UNIT,
                    getMbomTypeControlValue(context, CONFIG_KEY_DETAIL_OTHER_UNIT, "M"));
        }
        JF_LOGGER.info("!!!!!!!!!!!!newMap:{}", newMap);
        return newMap;
    }

    /**
     * C场景更新面套MBOM关系用量
     **
     * @param context 上下文
     * @param mbomConnId MBOM关系ID
     * @param partId EBOM零件ID
     * @param mapListGroupingMap EBOM子级结构分组
     * @param partValue 已查询的面套零件属性
     * @return void
     * @throws Exception 属性读取或关系更新失败时抛出异常
     * @author LIUJR
     * @date 2026/8/19
     */
    public void setTrimMbomDosageAttribute(Context context, String mbomConnId, String partId,
                                            Map mapListGroupingMap,
                                            Map<String, String> partValue) throws Exception {
        String originalDosage = EMPTY_STRING;
        if (mapListGroupingMap.containsKey(partId)) {
            List list = (List) mapListGroupingMap.get(partId);
            if (!list.isEmpty()) {
                Map ebomMap = (Map) list.get(0);
                originalDosage = UIUtil.getValue(ebomMap, JF_PLMConstants_mxJPO.SELECT_ATTR_JF_Dosage);
            }
        }
        String trimDosage = calculateTrimMBOMDosage(context, partValue, originalDosage);
        DomainRelationship mbomRelationship = DomainRelationship.newInstance(context, mbomConnId);
        String currentDosage = mbomRelationship.getAttributeValue(
                context, JF_PLMConstants_mxJPO.ATTR_JF_MBOM_DOSAGE);
        if (!Objects.equals(currentDosage, trimDosage)) {
            mbomRelationship.setAttributeValue(
                    context, JF_PLMConstants_mxJPO.ATTR_JF_MBOM_DOSAGE, trimDosage);
        }
        mbomRelationship.setAttributeValue(context, JF_PLMConstants_mxJPO.ATTR_JF_ORIGINATE_FLAG,
                JF_PLMConstants_mxJPO.ATTR_JF_ORIGINATE_FLAG_RANGE_EBOM);
    }

    /**
     * C场景更新零级面套供货件关系用量
     **
     * @param context 上下文
     * @param mbomObject 零级面套MBOM对象
     * @param partValue 面套零件属性
     * @return void
     * @throws Exception 关系查询或更新失败时抛出异常
     * @author LIUJR
     * @date 2026/8/19
     */
    private void setTrimRootMbomDosageAttribute(Context context, DomainObject mbomObject,
                                                 Map<String, String> partValue) throws Exception {
        String mbomConnId = mbomObject.getInfo(
                context, "to[" + REL_JF_relManufacturedItem + "].id");
        if (UIUtil.isNullOrEmpty(mbomConnId)) {
            return;
        }
        String trimDosage = calculateTrimMBOMDosage(context, partValue, "1");
        DomainRelationship mbomRelationship = DomainRelationship.newInstance(context, mbomConnId);
        mbomRelationship.setAttributeValue(
                context, JF_PLMConstants_mxJPO.ATTR_JF_MBOM_DOSAGE, trimDosage);
        mbomRelationship.setAttributeValue(context, JF_PLMConstants_mxJPO.ATTR_JF_ORIGINATE_FLAG,
                JF_PLMConstants_mxJPO.ATTR_JF_ORIGINATE_FLAG_RANGE_EBOM);
    }

    /**
     * 设置面套件默认MBOM属性
     **
     * @param attributeMap MBOM属性Map
     * @param defaultAttributeList 默认值为1的属性列表
     * @return void
     * @author LIUJR
     * @date 2026/8/19
     */
    private void setTrimDefaultAttributes(Map<String, String> attributeMap,
                                          List<String> defaultAttributeList) {
        for (String attributeName : defaultAttributeList) {
            attributeMap.put(attributeName, "1");
        }
    }

    /**
     * 查询面套转换所需的源零件属性
     **
     * @param context 上下文
     * @param partId 零件ID
     * @param baseValue 已有属性Map，可为空
     * @return Map 面套转换源属性
     * @throws Exception 属性查询失败时抛出异常
     * @author LIUJR
     * @date 2026/8/19
     */
    private Map<String, String> getTrimPartAttributeValues(Context context, String partId,
                                                           Map<String, String> baseValue) throws Exception {
        Map<String, String> partValue = baseValue == null ? new HashMap<>() : baseValue;
        partValue.putAll(DomainObject.newInstance(context, partId).getInfo(context, TRIM_PART_SELECT_LIST));
        return partValue;
    }

    /**
     * 计算面套MBOM关系用量
     **
     * @param context 上下文
     * @param partValue 面套零件属性
     * @param originalDosage EBOM原始用量
     * @return String 转换后的MBOM用量
     * @throws Exception 配置读取失败时抛出异常
     * @author LIUJR
     * @date 2026/8/19
     */
    private String calculateTrimMBOMDosage(Context context, Map<String, String> partValue,
                                            String originalDosage) throws Exception {
        String detailType = UIUtil.getValue(partValue, JF_PLMConstants_mxJPO.SELECT_ATTR_JF_Detail_CN);
        boolean isSamplePackage = isConfiguredTrimDetail(context, CONFIG_KEY_DETAIL_TYPE_T06, detailType);
        String procurementType = UIUtil.getValue(partValue, JF_PLMConstants_mxJPO.SELECT_ATTR_JF_ProcurementType);
        if (isConfiguredTrimDetail(context, CONFIG_KEY_DETAIL_TYPE, detailType)
                && JF_PLMConstants_mxJPO.ATTR_JF_ProcurementType_RANGE_MAKE.equalsIgnoreCase(procurementType)) {
            String formulaKey = isSamplePackage
                    ? CONFIG_KEY_DETAIL_TYPE_USAGE_T06 : CONFIG_KEY_DETAIL_TYPE_USAGE_OTHER;
            String defaultFormula = isSamplePackage
                    ? DEFAULT_DETAIL_TYPE_USAGE_T06 : DEFAULT_DETAIL_TYPE_USAGE_OTHER;
            return calculateConfiguredTrimDosage(
                    context, formulaKey, defaultFormula, partValue, originalDosage, detailType);
        }
        if (isConfiguredTrimDetail(context, CONFIG_KEY_DETAIL_OTHER, detailType)
                && UIUtil.isNotNullAndNotEmpty(originalDosage)) {
            return calculateConfiguredTrimDosage(
                    context, CONFIG_KEY_DETAIL_OTHER_USAGE, DEFAULT_DETAIL_OTHER_USAGE,
                    partValue, originalDosage, detailType);
        }
        return originalDosage;
    }

    /**
     * 读取MBOM_Type_Control中的用量公式并计算。当前配置只允许数字或attribute[...]通过“/”串联，
     * 避免执行任意脚本；公式缺值或格式错误时保留EBOM原用量。
     */
    private String calculateConfiguredTrimDosage(Context context, String formulaKey, String defaultFormula,
                                                   Map<String, String> partValue, String originalDosage,
                                                   String detailType) throws Exception {
        String formula = getMbomTypeControlValue(context, formulaKey, defaultFormula);
        Map<String, String> formulaValueMap = new HashMap<>(partValue);
        formulaValueMap.put("attribute[" + JF_PLMConstants_mxJPO.ATTR_JF_MBOM_WIDTH + "]", "1");
        formulaValueMap.put("attribute[" + JF_PLMConstants_mxJPO.ATTR_JF_MBOM_UTILIZATION_RATE + "]", "1");
        formulaValueMap.put(JF_PLMConstants_mxJPO.SELECT_ATTR_JF_Dosage, originalDosage);
        try {
            String[] operands = formula.replace(" ", EMPTY_STRING).split("/", -1);
            if (operands.length == 0) {
                return originalDosage;
            }
            BigDecimal result = getTrimFormulaOperand(operands[0], formulaValueMap);
            for (int index = 1; index < operands.length; index++) {
                BigDecimal divisor = getTrimFormulaOperand(operands[index], formulaValueMap);
                if (BigDecimal.ZERO.compareTo(divisor) == 0) {
                    throw new ArithmeticException("用量公式除数不能为0");
                }
                result = result.divide(divisor, MathContext.DECIMAL128);
            }
            return result.stripTrailingZeros().toPlainString();
        } catch (RuntimeException e) {
            JF_LOGGER.warn(
                    "面套MBOM用量公式计算失败，保留EBOM原用量，detailType:{}, formulaKey:{}, formula:{}, originalDosage:{}",
                    detailType, formulaKey, formula, originalDosage, e);
            return originalDosage;
        }
    }

    /**
     * 解析受控用量公式中的数字或attribute[...]操作数。
     */
    private BigDecimal getTrimFormulaOperand(String operand, Map<String, String> formulaValueMap) {
        String value = operand;
        if (operand.startsWith("attribute[") && operand.endsWith("]")) {
            value = formulaValueMap.get(operand);
        }
        if (UIUtil.isNullOrEmpty(value)) {
            throw new IllegalArgumentException("用量公式操作数没有值：" + operand);
        }
        return new BigDecimal(value);
    }

    /**
     * 按卷料号、固定版本和配置的详细分类查询卷料零件
     **
     * @param context 上下文
     * @param rollPartNumber 卷料零件号
     * @return Map AA.1-000卷料属性，未找到时返回空Map
     * @throws Exception 查询失败时抛出异常
     * @author LIUJR
     * @date 2026/8/19
     */
    private Map<String, String> findTrimRollPart(Context context, String rollPartNumber) throws Exception {
        if (UIUtil.isNullOrEmpty(rollPartNumber)) {
            return Collections.emptyMap();
        }
        String cacheKey = rollPartNumber + "|" + TRIM_ROLL_REVISION;
        Map<String, String> cachedRollPart = TRIM_ROLL_PART_CACHE.get(cacheKey);
        if (cachedRollPart != null) {
            return cachedRollPart;
        }
        Object cacheLock = TRIM_ROLL_PART_CACHE_LOCKS.computeIfAbsent(cacheKey, key -> new Object());
        try {
            synchronized (cacheLock) {
                cachedRollPart = TRIM_ROLL_PART_CACHE.get(cacheKey);
                if (cachedRollPart != null) {
                    return cachedRollPart;
                }
                String escapedRollPartNumber = rollPartNumber.replace("\\", "\\\\").replace("'", "\\'");
                String where = JF_PLMConstants_mxJPO.SELECT_ATTR_V_PART_NUMBER + "=='" + escapedRollPartNumber
                        + "' && revision=='" + TRIM_ROLL_REVISION + "'";
                MapList rollPartList = DomainObject.findObjects(
                        context, JF_PLMConstants_mxJPO.TYPE_VPMReference, "*", where, TRIM_ROLL_SELECT_LIST);
                JF_LOGGER.info("!!!!!!!!!!!!rollPartList:{}", rollPartList);
                if (rollPartList.isEmpty()) {
                    return Collections.emptyMap();
                }
                for (Object rollPartItem : rollPartList) {
                    Map<String, String> rollPartValue = (Map<String, String>) rollPartItem;
                    String detailType = UIUtil.getValue(
                            rollPartValue, JF_PLMConstants_mxJPO.SELECT_ATTR_JF_Detail_CN);
                    JF_LOGGER.info("!!!!!!!!!!!!detailType:{}", detailType);
                    if (!isConfiguredTrimDetail(context, CONFIG_KEY_ROLL_DETAIL_TYPE, detailType)) {
                        continue;
                    }
                    JF_LOGGER.info("!!!!!!!!!!!!detailType:{}", detailType);
                    //固定版本且详细分类符合卷料配置时才缓存；未找到不缓存，避免后续新增卷料仍命中空结果。
                    Map<String, String> rollPartMap = Collections.unmodifiableMap(
                            new HashMap<>(rollPartValue));
                    JF_LOGGER.info("!!!!!!!!!!!!rollPartMap:{}", rollPartMap);
                    TRIM_ROLL_PART_CACHE.put(cacheKey, rollPartMap);
                    return rollPartMap;
                }
                return Collections.emptyMap();
            }
        } finally {
            TRIM_ROLL_PART_CACHE_LOCKS.remove(cacheKey, cacheLock);
        }
    }

    /**
     * 判断详细分类是否属于MBOM面套配置分组
     **
     * @param context 上下文
     * @param configKey 配置项ID
     * @param detailType 详细分类
     * @return boolean 属于配置分组时返回true
     * @throws Exception 配置读取失败时抛出异常
     * @author LIUJR
     * @date 2026/8/19
     */
    private boolean isConfiguredTrimDetail(Context context, String configKey, String detailType) throws Exception {
        if (UIUtil.isNullOrEmpty(detailType)) {
            return false;
        }
        String configValue = getMbomTypeControlValue(context, configKey, EMPTY_STRING);
        for (String configuredDetail : configValue.split(",")) {
            if (detailType.equals(configuredDetail.trim())) {
                return true;
            }
        }
        return false;
    }

    /**
     * 获取MBOM面套配置项value
     **
     * @param context 上下文
     * @param configKey 配置项ID
     * @param defaultValue 默认值
     * @return String 配置值
     * @throws Exception 配置读取失败时抛出异常
     * @author LIUJR
     * @date 2026/8/19
     */
    private String getMbomTypeControlValue(Context context, String configKey, String defaultValue) throws Exception {
        Map<String, String> configMap = getMbomTypeControl(context);
        String configValue = configMap.get(configKey);
        if (UIUtil.isNullOrEmpty(configValue)) {
            return defaultValue;
        }
        return configValue;
    }

    /**
     * 静态延迟读取MBOM面套转换配置
     **
     * @param context 上下文
     * @return Map MBOM_Type_Control配置
     * @throws Exception Page读取或XML解析失败时抛出异常
     * @author LIUJR
     * @date 2026/8/19
     */
    private static Map<String, String> getMbomTypeControl(Context context) throws Exception {
        Map<String, String> configCache = MBOM_TYPE_CONTROL;
        if (configCache != null) {
            return configCache;
        }
        synchronized (MBOM_TYPE_CONTROL_LOCK) {
            if (MBOM_TYPE_CONTROL != null) {
                return MBOM_TYPE_CONTROL;
            }
            Page configPage = new Page("SignTaskProperties_zh.xml");
            configPage.open(context);
            String configContent;
            try {
                configContent = configPage.getContents(context);
            } finally {
                configPage.close(context);
            }
            Document document = Jsoup.parse(configContent, EMPTY_STRING, Parser.xmlParser());
            Element configuration = document.selectFirst(
                    "configuration[id=" + MBOM_TYPE_CONTROL_CONFIG + "]");
            if (configuration == null) {
                throw new Exception("未找到配置：" + MBOM_TYPE_CONTROL_CONFIG);
            }
            Map<String, String> loadedConfig = new HashMap<>();
            for (Element configElement : configuration.children()) {
                if (!"config".equals(configElement.tagName())) {
                    continue;
                }
                String configId = configElement.attr("id");
                if (UIUtil.isNullOrEmpty(configId)) {
                    continue;
                }
                loadedConfig.put(configId, configElement.attr("value"));
            }
            MBOM_TYPE_CONTROL = Collections.unmodifiableMap(loadedConfig);
            return MBOM_TYPE_CONTROL;
        }
    }

}
