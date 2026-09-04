import com.aspose.pdf.operators.Do;
import com.dassault_systemes.enovia.tskv2.ProjectSequence;
import com.dscn.plm.util.NioJDUtils;
import com.matrixone.apps.common.CommonDocument;
import com.matrixone.apps.domain.*;
import com.matrixone.apps.domain.util.*;
import com.matrixone.apps.framework.ui.PostProcessCallable;
import com.matrixone.apps.framework.ui.UIUtil;
import com.matrixone.apps.program.ProgramCentralConstants;
import ds.enovia.apps.msf.BusinessObjects.ExecuteJPO.args;
import jakarta.mail.BodyPart;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMultipart;
import matrix.db.*;
import matrix.util.StringList;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jsoup.Jsoup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.*;
import java.util.Set;
import java.util.stream.Collectors;

import static com.matrixone.apps.domain.DomainConstants.*;

/**
 * @author CHENYAN
 * @version v1.0.0
 * 创建时间：2025/7/31 14:37
 * @description
 */
public class JF_PartList_mxJPO implements JF_PLMConstants_mxJPO {
    private static final Logger LOGGER = LoggerFactory.getLogger(JF_PartList_mxJPO.class);
    private static final StringList PARTLIST_CHECK_PROJECT_ROLE_LIST = new StringList();
    private static final String key1 ="Root";
    private static final String key2="RootartNameCN";
    static {
        PARTLIST_CHECK_PROJECT_ROLE_LIST.add("Project manager");
        PARTLIST_CHECK_PROJECT_ROLE_LIST.add("Business manager");
        PARTLIST_CHECK_PROJECT_ROLE_LIST.add("Chair manager");
        PARTLIST_CHECK_PROJECT_ROLE_LIST.add("Financial BP");
        PARTLIST_CHECK_PROJECT_ROLE_LIST.add("SQD Representative");
    }

    /**
     * @param context
     * @param args
     * @return boolean
     * @throws
     * @description 获取创建partlist权限 只有项目经理才能创建
     * @author CHENYAN
     * @date 2025/7/31 14:41
     */
    public Boolean getCreatePartListCmdAccess(Context context, String[] args) throws Exception {
        Boolean isAccess = Boolean.FALSE;
        Map paramsMap = (Map) JPO.unpackArgs(args);
        String strObjectId = (String) paramsMap.get(STRING_OBJECTID);
        String strProjectManager = JF_Util_mxJPO.getProjectManager(context, new String[]{strObjectId});
        if (UIUtil.isNotNullAndNotEmpty(strProjectManager)) {
            String strLoginUser = context.getUser();
            isAccess = strLoginUser.equals(strProjectManager);
        }
        return isAccess;
    }

    /**
     * @param context
     * @param args
     * @return com.matrixone.apps.domain.util.MapList
     * @throws
     * @description 获取项目下关联的partlist
     * @author CHENYAN
     * @date 2025/8/1 15:18
     */
    public MapList getProjectPartListList(Context context, String[] args) throws Exception {
        try {
            Map paramsMap = (Map) JPO.unpackArgs(args);
            ContextUtil.pushContext(context);
            String strObjectId = (String) paramsMap.get(STRING_OBJECTID);
            StringList typeSelectList = JF_Util_mxJPO.basicBolistSel();
            typeSelectList.add(SELECT_ATTR_JFSyncSRM);
            typeSelectList.add(SELECT_ATTR_JFIsInit);
            typeSelectList.add(SELECT_ATTR_JFPartListType);
            DomainObject objectProject = DomainObject.newInstance(context, strObjectId);
            MapList partListMapList = objectProject.getRelatedObjects(context,
                    JF_PLMConstants_mxJPO.rel_JFProject2PartList, // relationship pattern
                    JF_PLMConstants_mxJPO.TYPE_JFPartList,                                    // object pattern
                    typeSelectList,                            // object selects
                    JF_Util_mxJPO.basicRellistSel(), // relationship selects
                    false,                                        // to direction
                    true,                                        // from direction
                    (short) 1,                                    // recursion level
                    "",                // object where clause
                    "",
                    (short) 0);
            return partListMapList;
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
        } finally {
            ContextUtil.popContext(context);
        }
        return new MapList();
    }

    /**
     * @param context
     * @param args
     * @return matrix.util.StringList
     * @throws
     * @description 获取项目中冻结的快照
     * @author CHENYAN
     * @date 2025/8/1 15:30
     */
    public StringList getFreezeSnapshotByProjectId(Context context, String[] args) throws Exception {
        Map parameterMap = JPO.unpackArgs(args);
        String strObjectId = (String) parameterMap.get(STRING_OBJECTID);
        DomainObject objectProject = DomainObject.newInstance(context, strObjectId);
        StringList snapshotIdList;
        try {
            ContextUtil.pushContext(context);
            String strType = objectProject.getInfo(context, SELECT_TYPE);
            //兼容编辑界面搜索快照
            if (TYPE_JFPartList.equals(strType)) {
                String strProjectId = objectProject.getInfo(context, "to[JFProject2PartList].from.id");
                objectProject.setId(strProjectId);
            }
            //PartList创建时、编辑时只能选择4~8类型（DV SOURCING,DV TKO,PV SOURCING,PV TKO,SOP）的快照
            String where = "current==FROZEN && (attribute[JFSnapshotSubType]=='PV SOURCING' ||  attribute[JFSnapshotSubType]=='DV SOURCING' ||  attribute[JFSnapshotSubType]=='DV TKO')";
            MapList snapshotMapList = objectProject.getRelatedObjects(context,
                    JF_PLMConstants_mxJPO.rel_JFProject2Snapshot, // relationship pattern
                    JF_PLMConstants_mxJPO.TYPE_JFSnapshot,                                    // object pattern
                    JF_Util_mxJPO.basicBolistSel(),                            // object selects
                    JF_Util_mxJPO.basicRellistSel(), // relationship selects
                    false,                                        // to direction
                    true,                                        // from direction
                    (short) 1,                                    // recursion level
                    where,                // object where clause
                    "",
                    (short) 0);
            snapshotIdList = (StringList) snapshotMapList.stream().map(m -> {
                Map snapshotMap = (Map) m;
                return snapshotMap.get(SELECT_ID);
            }).collect(Collectors.toCollection(StringList::new));
        } finally {
            ContextUtil.popContext(context);
        }
        return snapshotIdList;
    }

    /**
     * @param context
     * @param args
     * @return void
     * @throws
     * @description 创建partlist后连接项目和快照同时初始化零件清单
     * @author CHENYAN
     * @date 2025/8/6 15:57
     */
    @PostProcessCallable
    public void connectionProjectAndSnapshot(Context context, String[] args) throws Exception {
        Map parameter = (Map) JPO.unpackArgs(args);
        Map requestMap = (Map) parameter.get("requestMap");
        Map paramMap = (Map) parameter.get("paramMap");
        //快照id
        String strConnectionSnapShotOID = (String) requestMap.get("ConnectionSnapShotOID");
        String strProjectId = (String) requestMap.get(STRING_PARENTOID);
        String strNewObjectId = (String) paramMap.get("newObjectId");
        LOGGER.info("strConnectionSnapShotOID:{}", strConnectionSnapShotOID);
        LOGGER.info("strProjectId:{}", strProjectId);
        LOGGER.info("strNewObjectId:{}", strNewObjectId);
        try {
            ContextUtil.startTransaction(context, true);
            DomainObject newPartListBO = DomainObject.newInstance(context, strNewObjectId);
            DomainObject snapshotBO = DomainObject.newInstance(context, strConnectionSnapShotOID);
            StringList snapshotList = null;
            HashSet<String> snapshotRootPartIdSet = null;
            try {
                ContextUtil.pushContext(context);
                snapshotRootPartIdSet = getSnapshotConnectRootPart(context, strProjectId, strConnectionSnapShotOID);
            } finally {
                ContextUtil.popContext(context);
            }
            DomainRelationship.connect(context, DomainObject.newInstance(context, strProjectId), JF_PLMConstants_mxJPO.rel_JFProject2PartList, newPartListBO);
            DomainRelationship.connect(context, newPartListBO, JF_PLMConstants_mxJPO.rel_JFPartList2Snapshot, snapshotBO);
            //初始化partlist零件清单
            initializationPartInfoByPartList(context, snapshotRootPartIdSet, strProjectId, strNewObjectId);
            ContextUtil.commitTransaction(context);
        } catch (Exception e) {
            ContextUtil.abortTransaction(context);
            throw new RuntimeException(e);
        }
    }

    /**
     * @param context
     * @param snapRootPartSet 快照整椅集合
     * @param strProjectId    项目id
     * @param strPartListId   partlist id
     * @return void
     * @throws
     * @description 1.获取快照中整椅清单
     * @ 2.遍历整椅清单中所有一级件 如果一级件是GX 那么把GX当做整椅重复一次前面逻辑 如果是GU和 GT 面套和发泡那么获取所有子集加入清单中 如果是其他直接加入
     * @ 3. 卷积单车用量 和 初始化基础属性 设置关系属性
     * @author CHENYAN
     * @date 2025/8/4 16:46
     */
    public void initializationPartInfoByPartList(Context context, HashSet<String> snapRootPartSet, String strProjectId, String strPartListId) throws Exception {
//        DomainObject snapshotBO = DomainObject.newInstance(context, strSnapshotId);
        DomainObject projectBO = DomainObject.newInstance(context, strProjectId);
        //查询当前项目下所有的零件
        StringList partConnProjectList = getProjectAllPart(context,new String[]{strProjectId});
        LOGGER.info("partList size:{}",partConnProjectList);
        LOGGER.info("snapRootPartSet size:{}",snapRootPartSet);
        //项目量产地
        String strProjectArea = projectBO.getInfo(context, SELECT_ATTR_JFProducingArea);
        DomainObject partListBO = DomainObject.newInstance(context, strPartListId);
        String strPartListType = partListBO.getInfo(context, SELECT_ATTR_JFPartListType);
        String strJFPartListProfessional = partListBO.getInfo(context, SELECT_ATTR_JFPartListProfessional);//专业类型
        strJFPartListProfessional= strJFPartListProfessional.replace("G","");
        //获取整椅下面的一级件
        DomainObject rootPartBO = DomainObject.newInstance(context);
        //partlist零件总清单（PS为去重）
        MapList allPartMapList = new MapList(500);
        if (Objects.nonNull(snapRootPartSet)) {
            for (String strRootPartId : snapRootPartSet) {
                rootPartBO.setId(strRootPartId);
                String partType = rootPartBO.getInfo(context, SELECT_ATTR_JFPartType);
                MapList oneRootPartMapList=new MapList();
                if("C".equalsIgnoreCase(partType)) {
                    //如果是GC类型走当前逻辑，专业类型是整椅
                     oneRootPartMapList = getOnePartListByRootPart(context, rootPartBO, "0", strJFPartListProfessional);
                     LOGGER.info("oneRootPartMapList:{}",oneRootPartMapList);
                     setLevelZeroItem(context,rootPartBO,oneRootPartMapList);
                }
                //如果专业类型是面套或者发泡、并且类型供货件的类型是面套或者发泡走下面的逻辑
                else if("U".equalsIgnoreCase(partType)&&"U".equalsIgnoreCase(strJFPartListProfessional)){
                    oneRootPartMapList =getOnePartListByRootPartForUOT(context,rootPartBO);
                    setLevelZeroItem(context,rootPartBO,oneRootPartMapList);
                } else if("T".equalsIgnoreCase(partType)&&"T".equalsIgnoreCase(strJFPartListProfessional)){
                    oneRootPartMapList= getOnePartListByRootPartForUOT(context,rootPartBO);
                    setLevelZeroItem(context,rootPartBO,oneRootPartMapList);
                }else if("X".equalsIgnoreCase(partType)){//&&"C".equalsIgnoreCase(strJFPartListProfessional)
                    //获取下面所有的整椅
                   MapList gcList = getOnePartListByRootPartForGX(context,rootPartBO, strJFPartListProfessional);
                   for(int i=0;i<gcList.size();i++) {
                       Map map = (Map)gcList.get(i);
                       rootPartBO.setId(UIUtil.getValue(map, SELECT_ID));
                       String subType = UIUtil.getValue(map, SELECT_ATTR_JFPartType);
                       LOGGER.info("GX击穿之后的GC:{} {}",UIUtil.getValue(map, SELECT_ID),strJFPartListProfessional);
                       if("C".equalsIgnoreCase(subType)) {
                           oneRootPartMapList = getOnePartListByRootPart(context, rootPartBO, "0", strJFPartListProfessional);
                           setLevelZeroItem(context,rootPartBO,oneRootPartMapList);
                       }else if("U".equalsIgnoreCase(subType)||"T".equalsIgnoreCase(subType)) {
                           oneRootPartMapList= getOnePartListByRootPartForUOT(context,rootPartBO);
                           setLevelZeroItem(context,rootPartBO,oneRootPartMapList);
                       }
                         LOGGER.info("GX下面GX的子件oneRootPartMapList:{}",oneRootPartMapList);
                       allPartMapList.addAll(oneRootPartMapList);
                   }
                }

                if(!"X".equalsIgnoreCase(partType)) {
                    allPartMapList.addAll(oneRootPartMapList);
                }
            }
            LOGGER.info("===========================================");
            LOGGER.info("allPartMapList:{}", allPartMapList.toString());
        }
        Map groupMap = (Map) allPartMapList.stream().collect(Collectors.groupingBy(m -> {
            Map patMap = (Map) m;
            String strPartName = (String) patMap.get(SELECT_NAME);
            String strPartRevision = (String) patMap.get(SELECT_REVISION);
            return strPartName + strPartRevision;
        }));
        LOGGER.info("groupMap:{}", groupMap);
        DomainObject partBO = DomainObject.newInstance(context);
//        StringList defaultAttrList =  new StringList();
//        defaultAttrList.add(SELECT_ATTR_JFDIRECT_BUY);
//        defaultAttrList.add(SELECT_ATTR_JF_ProcurementType);
        HashMap setAttrMap = new HashMap<>();
        JF_VPMReferenceEBOM_mxJPO jfVpmReferenceEBOMMxJPO = new JF_VPMReferenceEBOM_mxJPO();
        HashMap setRelAttrMap = new HashMap<>();
        for (Object oEntry : groupMap.entrySet()) {
            Map.Entry entry = (Map.Entry) oEntry;
            //去重和卷积
            BigDecimal allDosage = new BigDecimal(0);
            StringList rootList = new StringList();
            StringList rootDescList = new StringList();
            List samePartMapList = (List) entry.getValue();
            String strPartObjectId = "";
            Map partMap = null;
            for (int i = 0; i < samePartMapList.size(); i++) {
                Map partInfoMap = (Map) samePartMapList.get(i);
                partMap = partInfoMap;
                if (UIUtil.isNullOrEmpty(strPartObjectId)) {
                    strPartObjectId = (String) partInfoMap.get(SELECT_ID);
                }
                String strDosage = (String) partInfoMap.get(SELECT_ATTR_JF_Dosage);
                String root = (String) partInfoMap.get(key1);
                String rootDescription = (String) partInfoMap.get(key2);
                //拿到0级件
                if(UIUtil.isNotNullAndNotEmpty(root)&&!rootList.contains(root)){
                    rootList.add(root);
                    rootDescList.add(rootDescription);
                }/*if(UIUtil.isNotNullAndNotEmpty(rootDescription)&&!rootList.contains(root)){
                }*/
                try {
                    LOGGER.info("strDosage:{}", strDosage);
                    if (UIUtil.isNotNullAndNotEmpty(strDosage)) {
                        BigDecimal dosage = new BigDecimal(strDosage);
                        allDosage = allDosage.add(dosage);
                    }
                } catch (Exception e) {
                    LOGGER.error(e.getMessage());
                }
            }
            //设置默认属性 和连接partList
            if (UIUtil.isNotNullAndNotEmpty(strPartObjectId)) {
                partBO.setId(strPartObjectId);
                String strDirectBuy = "";
                String JF_ProjectRel = "";
                String JFProcurementType = "";
                if (Objects.nonNull(partMap)) {
                    strDirectBuy = (String) partMap.get(SELECT_ATTR_JFDIRECT_BUY);
                }
                if (Objects.nonNull(partMap)) {
                    JF_ProjectRel = (String) partMap.get(SELECT_ATTR_JF_ProjectRel);
                } if (Objects.nonNull(partMap)) {
                    JFProcurementType = (String) partMap.get(SELECT_ATTR_JF_ProcurementType);
                }

                if (UIUtil.isNullOrEmpty(strDirectBuy)) {
                    setAttrMap.put(ATTR_JFDIRECT_BUY, ATTR_ATTR_JFDIRECT_BUY_RANGE_N);
                }
                //设置采购类型和  DIRECT_BUY 并清空修改属性集合
                if (setAttrMap.size() > 0) {
                    partBO.setAttributeValues(context, setAttrMap);
                    setAttrMap.clear();
                }
                //看当前零件的关联项目，和PartList关联项目是否一致，如果是一致就是New，否则是沿用件
//                StringList partConnProjectList = jfVpmReferenceEBOMMxJPO.getPartZeroProject(context, strPartObjectId);
LOGGER.info("strPartObjectId：{}",strPartObjectId);
                if (partConnProjectList.contains(strPartObjectId)) {
                    setRelAttrMap.put(ATTR_JFCarryOver, ATTR_JFCarryOver_RANGE_New);
                } else {
                    //零件的项目属性为空的话，或者属于其他项目都为沿用件
//                    if(UIUtil.isNotNullAndNotEmpty(JF_ProjectRel)) {
                        setRelAttrMap.put(ATTR_JFCarryOver, ATTR_JFCarryOver_RANGE_CarryOver);
//                    }
                }
                //单车用量
                setRelAttrMap.put(ATTR_JFBicycleUsage, allDosage.toString());
                //量产地
                setRelAttrMap.put(ATTR_JFOutputLocation, strProjectArea);
                //交付阶段
                setRelAttrMap.put(ATTR_JFDeliveryPhase, strPartListType);
                //设置采购类型
                setRelAttrMap.put(ATTR_PartList_JFProcurementType, JFProcurementType);

                //设置零级件
                setRelAttrMap.put("JFRootPartNumber",  rootList.join(","));
                //设置0级件的中文描述
                setRelAttrMap.put("JFRootPartDescription", rootDescList.join(","));
                //连接partlist
                DomainRelationship rel = DomainRelationship.connect(context, partListBO, JF_PLMConstants_mxJPO.rel_JFPartList2VPMReference, partBO);
                rel.setAttributeValues(context, setRelAttrMap);
            }
        }
        // 设置 JFIsInit 为 Y
        partListBO.setAttributeValue(context, ATTR_JFIsInit, "Y");
    }


    /**
     * @param context
     * @param rootPartBO
     * @return com.matrixone.apps.domain.util.MapList
     * @throws
     * @description //获取整椅件下面的一级件、如果一级件是GX把GX当整椅继续往下面找一层 、如果是面套和发泡则获取下面的所有子集
     * 2025年10月23日更新的逻辑
     *       整椅（供货件）:整椅下的一级件，如果一级件是GX击穿找下层级的一级件；
     *       发泡（供货件）:先查询一级件是发泡，并将发泡下的二级件添加到PartList中；---不包含一级件发泡
     *       面套（供货件）:先查询一级件是面套，并将面套下的二级件添加到PartList中；---不包含一级件面套
     * @author CHENYAN
     * @date 2025/8/4 15:50
     */
    public MapList getOnePartListByRootPart(Context context, DomainObject rootPartBO, String strPartLevel,String Professional) throws Exception {
        MapList allPartMapList = new MapList();
        StringList typeSelectList = JF_Util_mxJPO.basicBolistSel();
        typeSelectList.add(SELECT_ATTR_JFPartType);
        typeSelectList.add(SELECT_ATTR_JFDIRECT_BUY);
        typeSelectList.add(SELECT_ATTR_JF_VPMReference_JF_ProcurementGroup);
        typeSelectList.add(SELECT_ATTR_JF_ProcurementType);
        typeSelectList.add(SELECT_ATTR_JF_ProjectRel);
        typeSelectList.add(SELECT_ATTR_V_PART_NUMBER);

        StringList relSelectList = JF_Util_mxJPO.basicRellistSel();
        relSelectList.add(SELECT_ATTR_JF_Dosage);
        String where="";
        if(!Professional.equalsIgnoreCase("C")){
            where = SELECT_ATTR_JFPartType + "==" + Professional;//发泡和面套
        }
        String rootType = rootPartBO.getInfo(context, SELECT_TYPE);
        //采购组
        MapList onePartMapList = rootPartBO.getRelatedObjects(context,
                JF_PLMConstants_mxJPO.REL_Instance, // relationship pattern
                JF_PLMConstants_mxJPO.TYPE_VPMReference,                                    // object pattern
                typeSelectList,                            // object selects
                relSelectList, // relationship selects
                false,                                        // to direction
                true,                                        // from direction
                (short) 1,                                    // recursion level
                where,                // object where clause
                "",
                (short) 0);
        for (int i = 0; i < onePartMapList.size(); i++) {
            Map onePartMap = (Map) onePartMapList.get(i);
            String strOnePartId = (String) onePartMap.get(SELECT_ID);
            String strPartType = (String) onePartMap.get(SELECT_ATTR_JFPartType);
            String number = (String) onePartMap.get(SELECT_ATTR_V_PART_NUMBER);
            LOGGER.info("strPartType:{}", strPartType);
            LOGGER.info("number:{}", number);
            LOGGER.info("=========================");
            //添加一级件
            if (!"X".equals(strPartType)) {
                if(Professional.equalsIgnoreCase("C")) {
                    allPartMapList.add(onePartMap);
                }
            }
            //面套发泡需要添加的第一层子集
            if ("U".equals(Professional) || "T".equals(Professional)) {
                //重置id为一级件id
                rootPartBO.setId(strOnePartId);
                MapList sunPartMapList = rootPartBO.getRelatedObjects(context,
                        JF_PLMConstants_mxJPO.REL_Instance, // relationship pattern
                        JF_PLMConstants_mxJPO.TYPE_VPMReference,                                    // object pattern
                        typeSelectList,                            // object selects
                        relSelectList, // relationship selects
                        false,                                        // to direction
                        true,                                        // from direction
                        (short) 1,                                    // recursion level
                        "",                // object where clause
                        "",
                        (short) 0);
                allPartMapList.addAll(sunPartMapList);
            } else if ("X".equals(strPartType) && "0".equals(strPartLevel)) {
                rootPartBO.setId(strOnePartId);
                //一级件下面的GX不在找
                LOGGER.info("GX。。。。。。。。。");
                LOGGER.info("number:{}", number);
                LOGGER.info("GX。。。。。。。。。");
                allPartMapList.addAll(getOnePartListByRootPart(context, rootPartBO, "1",Professional));
            }
        }
        return allPartMapList;
    }

    /**
     * @param context
     * @param args
     * @return com.matrixone.apps.domain.util.MapList
     * @throws
     * @description 获取partlist关联零件清单
     * @author CHENYAN
     * @date 2025/8/6 15:49
     */
    public  MapList getPartListConnectionParts(Context context, String[] args) throws Exception {
        Map paramsMap = (Map) JPO.unpackArgs(args);
        String strObjectId = (String) paramsMap.get(STRING_OBJECTID);
        String strRoleFlag = (String) paramsMap.get("roleFlag");
        LOGGER.info("paramsMap:{}", paramsMap);
        LOGGER.info("strRoleFlag:{}", strRoleFlag);
        StringList typeSelectList = JF_Util_mxJPO.basicBolistSel();
        typeSelectList.add(SELECT_ATTR_JF_VPMReference_JF_ProcurementGroup);
        String strLoginUser = context.getUser();
        String strProjectRole = "";
        String strTypeWhere = "";
        String strRoleEditState = "";
        switch (strRoleFlag) {
            case "PM": {
                strProjectRole = ATTR_PROJECT_ROLE_Range_PM;
                strRoleEditState = "InWork";
                break;
            }
            case "RootChair": {
                strProjectRole = ATTR_PROJECTROLE_RANGE_Chairmanager;
                //研发只关注零件类型为buy
                strTypeWhere = "attribute[JF_VPMReference.JF_ProcurementType]=='buy'";
                break;
            }
            case "CaiwuBP": {
                strProjectRole = ATTR_PROJECTROLE_RANGE_FinancialBP;
                strRoleEditState = "Review";
                break;
            }
            case "Business": {
                strProjectRole = ATTR_PROJECT_ROLE_Range_BU;
                //商务需要过滤DirectBuy为Y的零件
                strTypeWhere = "attribute[JF_VPMReference.JF_DirectBuy]=='consignment' || attribute[JF_VPMReference.JF_DirectBuy]=='direct-buy'";
                strRoleEditState = "Review";
                break;
            }
            case "SQD": {
                strProjectRole = ATTR_PROJECTROLE_RANGE_SQDRepresentative;
                strRoleEditState = "Review";
                break;
            }
            case "All": {
                strProjectRole = "All";
                break;
            }
            default: {

            }
        }
        LOGGER.info("strProjectRole:{}", strProjectRole);
        LOGGER.info("strRoleEditState:{}", strRoleEditState);
        DomainObject partListBO = DomainObject.newInstance(context, strObjectId);
        MapList partListMapList = partListBO.getRelatedObjects(context,
                JF_PLMConstants_mxJPO.rel_JFPartList2VPMReference, // relationship pattern
                JF_PLMConstants_mxJPO.TYPE_VPMReference,                                    // object pattern
                typeSelectList,                            // object selects
                JF_Util_mxJPO.basicRellistSel(), // relationship selects
                false,                                        // to direction
                true,                                        // from direction
                (short) 1,                                    // recursion level
                strTypeWhere,                // object where clause
                "",
                (short) 0);
        if (UIUtil.isNotNullAndNotEmpty(strProjectRole)) {
            String strProjectId = partListBO.getInfo(context, "to[JFProject2PartList].from.id");
            Boolean isEdit = Boolean.FALSE;
            if (!"All".equals(strProjectRole)) {
//                String strProjectManager = JF_Util_mxJPO.getProjectRoleName(context, new String[]{strProjectId, strProjectRole});
                Map partListBOInfo = partListBO.getInfo(context, StringList.create(SELECT_OWNER, SELECT_CURRENT));
                String strOwner = (String) partListBOInfo.get(SELECT_OWNER);
                String strCurrent = (String) partListBOInfo.get(SELECT_CURRENT);
                LOGGER.info("strOwner:{}", strOwner);
                LOGGER.info("strCurrent:{}", strCurrent);
                if (strRoleEditState.equals(strCurrent)) {
                    isEdit = Boolean.TRUE;
                    LOGGER.info("strRoleEditState : {},strCurrent):{}", strRoleEditState, strCurrent);
                }
                if (ATTR_PROJECT_ROLE_Range_PM.equals(strProjectRole)) {
                    if (strLoginUser.equals(strOwner)) {
                        isEdit = Boolean.TRUE;
                        LOGGER.info("strLoginUser : {},strOwner):{}", strLoginUser, strOwner);
                    }
                }
            }
            LOGGER.info("isEdit:{}", isEdit);
            for (int i = 0; i < partListMapList.size(); i++) {
                Map partListMap = (Map) partListMapList.get(i);
                String strRelId = (String) partListMap.get(SELECT_RELATIONSHIP_ID);
                if (ATTR_PROJECT_ROLE_Range_PM.equals(strProjectRole) || ATTR_PROJECTROLE_RANGE_Chairmanager.equals(strProjectRole)) {
                    StringList docIdList = getPartConnectDocByLoginUser(context, strRelId, strObjectId, strProjectRole);
                    partListMap.put("docIdList",docIdList);
                }
                partListMap.put("isEdit", isEdit.toString());
            }
        }
        LOGGER.info("partListMapList:{}", partListMapList);
        LOGGER.info("==============================================================");
        return partListMapList;

    }
    public static MapList getPartListConnectionPartsByCheckRoute(Context context, String strObjectId, String strRoleFlag,StringList typeSelectList ,StringList relSelectList) throws Exception {
        String strTypeWhere = "";
        switch (strRoleFlag) {
            case ATTR_PROJECT_ROLE_Range_PM: {
                break;
            }
            case ATTR_PROJECTROLE_RANGE_Chairmanager: {
                //研发只关注零件类型为buy
                strTypeWhere = "attribute[JF_VPMReference.JF_ProcurementType]=='buy'";
                break;
            }
            case ATTR_PROJECTROLE_RANGE_FinancialBP: {
                break;
            }
            case ATTR_PROJECT_ROLE_Range_BU: {
                //商务需要过滤DirectBuy为Y的零件
                strTypeWhere = "attribute[JF_VPMReference.JF_DirectBuy]=='consignment' || attribute[JF_VPMReference.JF_DirectBuy]=='direct-buy'";
                break;
            }
            case ATTR_PROJECTROLE_RANGE_SQDRepresentative: {
                break;
            }

            default: {

            }
        }
        DomainObject partListBO = DomainObject.newInstance(context, strObjectId);
        MapList partListMapList = partListBO.getRelatedObjects(context,
                JF_PLMConstants_mxJPO.rel_JFPartList2VPMReference, // relationship pattern
                JF_PLMConstants_mxJPO.TYPE_VPMReference,                                    // object pattern
                typeSelectList,                            // object selects
                relSelectList, // relationship selects
                false,                                        // to direction
                true,                                        // from direction
                (short) 1,                                    // recursion level
                strTypeWhere,                // object where clause
                "",
                (short) 0);
        return partListMapList;

    }
    /**
    *
    *@description 构造关联文件列
    *@param context
	*@param args
    *@return matrix.util.StringList
    *@throws
    *@author CHENYAN
    *@date 2025/8/24 16:57
    */


    public StringList getPartConnectionDocument(Context context ,String[] args) throws Exception{
        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        MapList objectList = (MapList) programMap.get("objectList");
        StringList res = new StringList(objectList.size());
        for (int i = 0; i < objectList.size(); i++) {
            Map partListInfoMap = (Map) objectList.get(i);
            String strColValue = "";
            Object obj =partListInfoMap.get("docIdList");
            if(!Objects.isNull(obj)) {
                StringList docIdList = (StringList)obj;
         /*   if (Objects.nonNull(docIdList) && docIdList.size() > 0){
                MapList docInfoList = DomainObject.getInfo(context, docIdList.toStringArray(), StringList.create(SELECT_ATTRIBUTE_TITLE));
                StringList docTitleList = (StringList)docInfoList.stream().map(m ->{
                    Map docMap = (Map)m;
                    return docMap.get(SELECT_ATTRIBUTE_TITLE);
                }).collect(Collectors.toCollection(StringList::new));
                strColValue = docTitleList.join(",");
            }*/
                for (int j = 0; j < docIdList.size(); j++) {
                    String docId = docIdList.get(j);
                    DomainObject docObj = DomainObject.newInstance(context, docId);
                    String title = docObj.getInfo(context, SELECT_ATTRIBUTE_TITLE);
                    if (UIUtil.isNotNullAndNotEmpty(strColValue)) {
                        strColValue = strColValue + "\n";
                    }
                    strColValue = strColValue + JF_Util_mxJPO.buildHtml(context, docId, title);
                }
            }
            res.add(strColValue);
        }
        return res ;
    }

    /**
     * partList状态审批提升到完成trigger
     * PartList通知零件至SRM  打包，改名-与数据外发类似
     *
     * @param context
     * @param args
     * @return void
     * @throws
     * @author LIUJR
     * @date 2025/8/6 15:43
     * @description
     */
    public void partListReviewPromoteAction(Context context, String[] args) {
        String strFileName = DomainConstants.EMPTY_STRING;
        String fileOtherDocPath = DomainConstants.EMPTY_STRING;
        String fileSendData = DomainConstants.EMPTY_STRING;
        String fileDownloadV5FilePath = DomainConstants.EMPTY_STRING;
        try {
            String partListId = args[0];
            DomainObject domainObject = DomainObject.newInstance(context);
            DomainObject partListObject = DomainObject.newInstance(context);
            partListObject.setId(partListId);
            partListObject.setAttributeValue(context, "JSdataProcessProgress", "EnoviaProcess");
            String partListName = partListObject.getInfo(context, SELECT_NAME);
            //获取数模版本
            String rev = partListObject.getInfo(context, "to[JFProject2PartList].from.attribute[JSOutSourceRev]");
            String strBasicUrl = JF_PublicMethodClass_mxJPO.getBasicUrl(context, new String[]{"dataOutSource.share.path"});
            //拿取partList下的零件数据
            StringList typeSelectList = JF_Util_mxJPO.basicBolistSel();
            typeSelectList.add("physicalid");
            MapList partListMapList = partListObject.getRelatedObjects(context,
                    JF_PLMConstants_mxJPO.rel_JFPartList2VPMReference, // relationship pattern
                    JF_PLMConstants_mxJPO.TYPE_VPMReference,                                    // object pattern
                    typeSelectList,                            // object selects
                    JF_Util_mxJPO.basicRellistSel(), // relationship selects
                    false,                                        // to direction
                    true,                                        // from direction
                    (short) 1,                                    // recursion level
                    "",                // object where clause
                    "attribute[JSdataProcessProgress]!=OutsourceDataComplete" +
                            "&&attribute[JSdataProcessProgress]!=CAAProcessingComplete" +
                            "&&attribute[JSdataProcessProgress]!=SentEmail",
                    (short) 0);
            DomainObject partObject = DomainObject.newInstance(context);
            String strPrintTmpMql = "print connection {0} select tomid[JFDocument2VPM].from.id dump ,";
            //遍历选择零件关系
            //遍历partList的数据
            DomainRelationship domainRelationship;
            for (int i2 = 0; i2< partListMapList.size(); i2++) {
                Map map1 = (Map) partListMapList.get(i2);
                String partId = UIUtil.getValue(map1, SELECT_ID);
                //partList与零件的关系
                String connId = UIUtil.getValue(map1, DomainRelationship.SELECT_ID);
                domainRelationship = DomainRelationship.newInstance(context, connId);
                domainRelationship.setAttributeValue(context, "JSdataProcessProgress", "EnoviaProcess");
                String physicalid = UIUtil.getValue(map1, "physicalid");
                //零件的物理id  作为文件名称
                String strName = partListName + "_" + physicalid;
                partObject.setId(partId);
                //一级件下的结构
                MapList partMapList = partObject.getRelatedObjects(context,
                        JF_PLMConstants_mxJPO.REL_Instance, // relationship pattern
                        JF_PLMConstants_mxJPO.TYPE_VPMReference,                                    // object pattern
                        typeSelectList,                            // object selects
                        JF_Util_mxJPO.basicRellistSel(), // relationship selects
                        false,                                        // to direction
                        true,                                        // from direction
                        (short) 0,                                    // recursion level
                        "",                // object where clause
                        "",
                        (short) 0
                );
                //将零件与结构一起组合集合
                partMapList.add(map1);
                //分类型
                StringList vpmReferenceV6List = new StringList();
                StringList relList = JF_Util_mxJPO.basicRellistSel();
                Iterator iterator = partMapList.iterator();
                StringList vpmDrawIds = new StringList();
                HashSet vpmDocumentSet = new HashSet<String>();
                //所有的数模和图纸
                while (iterator.hasNext()) {
                    Map map = (Map) iterator.next();
                    String strOId = (String) map.get(DomainConstants.SELECT_ID);
                    //挑出v5 情况
                    vpmReferenceV6List.add(strOId);
                    //找到2D 3D文件
                    partObject.setId(strOId);
                    //找到2D/3D图纸
                    MapList drwList = partObject.getRelatedObjects(context, JF_PLMConstants_mxJPO.REL_XCADBaseDependency, JF_PLMConstants_mxJPO.TYPE_Drawing, JF_Util_mxJPO.basicBolistSel(), relList, true, false, (short) 1, "", "", 0);
                    if (drwList.size() > 0) {
                        for (int j = 0; j < drwList.size(); j++) {
                            Map temp = (Map) drwList.get(j);
                            vpmDrawIds.addAll(UIUtil.getValue(temp, DomainConstants.SELECT_ID));
                        }
                    }
                    StringList infoList = partObject.getInfoList(context, " from[Reference Document].to");
                    vpmDocumentSet.addAll(infoList);
                }
                //开始下载文件和数模
                //创建申请单文件夹目录
                StringList createDirList = new StringList();
                fileOtherDocPath = strBasicUrl + strName + File.separator + STRING_OTHER_DOC;
                fileSendData = strBasicUrl + strName + File.separator + STRING_SEND_DATA;
                fileDownloadV5FilePath = strBasicUrl + strName + File.separator + STRING_DOWNLOAD_V5_FILE + File.separator;
                createDirList.add(fileOtherDocPath + File.separator);
                createDirList.add(fileDownloadV5FilePath);
                createDirList.add(fileSendData + File.separator);
                Boolean hasJsonWithCatia = Boolean.FALSE;
                for (String filePath : createDirList) {
                    JF_PublicMethodClass_mxJPO.createDirFilePath(context, filePath);
                }
                //开始操作数模 生成json文件
                MapList vpmReferenceV5DownList = new MapList();
                MapList drawingV5DownList = new MapList();
                StringList repeatIdList = new StringList();
                if (vpmReferenceV6List.size() > 0) {
                    LOGGER.info("vpmReferenceV6List:{}", vpmReferenceV6List);
                    //转换版本
                    Map<String, String> paramsMap = new HashMap<>();
                    paramsMap.put("strOutSourceRev", rev);
                    paramsMap.put("strDirName", strName);
                    paramsMap.put("strBasicUrl", strBasicUrl);
                    paramsMap.put("vpmReferenceV6Ids", vpmReferenceV6List.join(","));
                    paramsMap.put("drawingV5Ids", "");
                    paramsMap.put("drawingV6Ids", "");
                    Map map = JF_DataOutSource_mxJPO.constructJSONFileForDigifax(context, JPO.packArgs(paramsMap));
                    vpmReferenceV5DownList = (MapList) map.get("vpmReferenceV5IdMapList");
                    drawingV5DownList = (MapList) map.get("drawingV5IdMapList");
                    repeatIdList = (StringList) map.get("repeatIdList");
                    //是否包含数模和图纸
                    hasJsonWithCatia = Boolean.TRUE;
                }
                //创建文件夹 下载v5数模到共享盘
                if (vpmReferenceV5DownList.size() > 0) {
                    //需要创建DownloadV5File文件夹
                    //创建好了文件夹后, 开始下载V5数模
                    //下载数模  返回数模id 和路径地址
                    MapList mapList = JF_PublicMethodClass_mxJPO.downloadDigifaxModel(context, "Part", vpmReferenceV5DownList, fileDownloadV5FilePath, repeatIdList);
                }
                //创建文件夹 下载图纸的衍生物到共享盘
                if (drawingV5DownList.size() > 0) {
                    //下载图纸 返回图纸id 和路径地址
                    MapList mapList = JF_PublicMethodClass_mxJPO.downloadDigifaxModel(context, "Draw", drawingV5DownList, fileDownloadV5FilePath, repeatIdList);
                }
                if (vpmDrawIds.size() > 0) {
                    //下载图纸下面的衍生物
                    //下载pdf文件到系统目录
                    JF_Util_mxJPO jfUtilMxJPO = new JF_Util_mxJPO();
                    for (int i = 0; i < vpmDrawIds.size(); i++) {
                        String id = vpmDrawIds.get(i);
                        partObject.setId(id);
                        String pdfId = partObject.getInfo(context, "from[DerivedOutputRelationship].to.id");
                        domainObject.setId(pdfId);
                        //获取图纸的最新版本的衍生物
                        String physicalid1 = partObject.getInfo(context, SELECT_PHYSICAL_ID);
                        StringList derivedOutputFileListMapList = jfUtilMxJPO.getDerivedOutputFileListName(context, new String[]{physicalid1});
                        for (int i1 = 0; i1 < derivedOutputFileListMapList.size(); i1++) {
                            String fileName = derivedOutputFileListMapList.get(i1);
                            jfUtilMxJPO.checkOutFile(context, domainObject, fileName, fileOtherDocPath);
                        }
                    }
                }
                //下载partList 与零件关系上的 关联的文档
                String strPrintMql = MessageFormat.format(strPrintTmpMql, connId);
                LOGGER.info("strPrintMql:{}", strPrintMql);
                String strSelectRes = MqlUtil.mqlCommand(context, Boolean.FALSE, strPrintMql, Boolean.TRUE);
                if (UIUtil.isNotNullAndNotEmpty(strSelectRes)) {
                    StringList documentList = new StringList();
                    String[] split = strSelectRes.split(",");
                    for (int i = 0; i < split.length; i++) {
                        String strDocId = split[i].trim();
                        documentList.add(strDocId);
                    }
                    for (String docId : documentList) {
                        String[] params = new String[3];
                        params[0] = docId;
                        params[1] = strName;
                        params[2] = fileOtherDocPath;
                        Map<String, Object> map = JF_WaterMarkUtils_mxJPO.fileCheckout(context, params);
                    }
                }
                //下载零件的附件文档
                StringList vpmDocumentList  = StringList.create(vpmDocumentSet);
                for (int i = 0; i < vpmDocumentList.size(); i++) {
                    String[] params = new String[3];
                    params[0] = vpmDocumentList.get(i);
                    params[1] = strName;
                    params[2] = fileOtherDocPath;
                    Map<String, Object> map = JF_WaterMarkUtils_mxJPO.fileCheckout(context, params);
                }
                //数模下载后 开始做后续操作。
                //需要caa处理  将状态改为待caa处理
                domainRelationship.setAttributeValue(context, "JSdataProcessProgress", "PendingCAAProcess");
                partListObject.setAttributeValue(context, "JSdataProcessProgress", "PendingCAAProcess");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
    * 修改partList 属性 和关系属性
    * @param context
	* @param partList
	* @param partLists
    * @author LIUJR
    * @throws
    * @return void
    * @date 2025/9/1 14:44
    * @description
    */
    public void modifyPartListPartSynchronized(Context context, String partList, StringList partLists) throws Exception {
        try {
            ContextUtil.pushContext(context);
            DomainObject domainObject = DomainObject.newInstance(context, partList);
            StringList typeSelectList = JF_Util_mxJPO.basicBolistSel();
            typeSelectList.add("physicalid");
            StringList basicRellistSel = JF_Util_mxJPO.basicRellistSel();
            MapList partListMapList = domainObject.getRelatedObjects(context,
                    JF_PLMConstants_mxJPO.rel_JFPartList2VPMReference, // relationship pattern
                    JF_PLMConstants_mxJPO.TYPE_VPMReference,                                    // object pattern
                    typeSelectList,                            // object selects
                    basicRellistSel, // relationship selects
                    false,                                        // to direction
                    true,                                        // from direction
                    (short) 1,                                    // recursion level
                    "",                // object where clause
                    "",
                    (short) 0
            );
            Map mapListGroupingMap = JF_NewECRService_mxJPO.getMapListGroupingMap(context, partListMapList, "physicalid");
            DomainRelationship domainRelationship;
            for (int i = 0; i < partLists.size(); i++) {
                String partPhysicalId = partLists.get(i);
                if (mapListGroupingMap.containsKey(partPhysicalId)) {
                    List relIdList = (List) mapListGroupingMap.get(partPhysicalId);
                    Map map1 = (Map) relIdList.get(0);
                    String connId = UIUtil.getValue(map1, DomainRelationship.SELECT_ID);
                    domainRelationship = DomainRelationship.newInstance(context, connId);
                    domainRelationship.setAttributeValue(context, ATTR_JSdataProcessProgress, RANGE_PROCESS_PROGRESS_OUTSOURCEDATACOMPLETE);
                    domainRelationship.setAttributeValue(context, ATTR_JF_SyncStatus, RANGE_synchronized);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        } finally {
            ContextUtil.popContext(context);
        }
    }

    /**
    * 发送邮件
     *     * 定时器的那个也一样的，但是你需要把所有的有问题的PartList 和有问题的零件  ，发在一封邮件中
     *      * 类似的写一个table 放在邮件中
    * @param context
	* @param args
    * @author LIUJR
    * @throws
    * @return void
    * @date 2025/9/12 15:11
    * @description
    */
    public void problematicPartListData(Context context, String[] args) throws Exception {
        try {
            String oneOrAll = args[0];
            StringList partList = new StringList();
            StringList basicBolistSel = JF_Util_mxJPO.basicBolistSel();
            basicBolistSel.add(JF_PLMConstants_mxJPO.SELECT_ATTR_V_PART_NUMBER);
            basicBolistSel.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JF_PartType);
            basicBolistSel.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JF_PartNameCN);
            basicBolistSel.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JF_PartNameEN);
            basicBolistSel.add(SELECT_POLICY);
            StringList basicRellistSel = JF_Util_mxJPO.basicRellistSel();
            basicRellistSel.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JSdataProcessProgress);
            basicRellistSel.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JF_SyncStatus);
            String strWhere = EMPTY_STRING;
            String sendEmails = "";
            DomainObject domainObject = DomainObject.newInstance(context);
            StringList listAttr = new StringList();
            //根据传入的模式,拿取数据
            if ("all".equalsIgnoreCase(oneOrAll)) {
                //系统中全部的数据
                //查找系统中，所有有问题的partList
                String where = SELECT_ATTR_JFSyncSRM + "==NotFullsynchronized||" + SELECT_ATTR_JFSyncSRM + "==fail";
                MapList partnerMapList = DomainObject.findObjects(context,JF_PLMConstants_mxJPO.TYPE_JFPartList,
                        DomainConstants.QUERY_WILDCARD,where,basicBolistSel);
                LOGGER.info("partnerMapList:{}", partnerMapList);
                partList = (StringList) partnerMapList.stream().map(m -> {
                    Map map = (Map) m;
                    return UIUtil.getValue(map, SELECT_ID);
                }).collect(Collectors.toCollection(StringList::new));
                LOGGER.info("partList:{}", partList);
                //找到旗下 转换成功，未同步；转换失败； 同步失败的情况
                strWhere = JF_PLMConstants_mxJPO.SELECT_ATTR_JSdataProcessProgress + "=="
                        + JF_PLMConstants_mxJPO.RANGE_PROCESS_PROGRESS_CAAPROCEFAILD
                        + " || "
                        + JF_PLMConstants_mxJPO.SELECT_ATTR_JF_SyncStatus + "=="  + RANGE_unsynchronized;
                sendEmails = JF_PublicMethodClass_mxJPO.getBasicUrl(context, new String[]{"partList.IT.Email"});
                listAttr.add("partListNameId");
                LOGGER.info("listAttr:{}", listAttr);
            } else {
                //同步后发送信息
                partList.add(oneOrAll);
                strWhere = JF_PLMConstants_mxJPO.SELECT_ATTR_JF_SyncStatus + "=="  + RANGE_unsynchronized;
                //获取partList关联的项目的项目经理
                domainObject.setId(oneOrAll);
                String projectId = domainObject.getInfo(context, "to[JFProject2PartList].from.id");
                if (UIUtil.isNotNullAndNotEmpty(projectId)) {
                    //项目经理
                    String projectManagerName = JF_PublicMethodClass_mxJPO.getProjectWherePerson(context, projectId, SELECT_NAME, "", "attribute[Project Role]=='Project manager'");
                    DomainObject personObject = PersonUtil.getPersonObject(context, projectManagerName);
                    sendEmails = personObject.getAttributeValue(context, DomainObject.ATTRIBUTE_EMAIL_ADDRESS);
                }
            }
            LOGGER.info("sendEmails:{}", sendEmails);
            LOGGER.info("strWhere:{}", strWhere);
            LOGGER.info("partList:{}", partList);
            //partList
            HashMap<String, MapList> listHashMap = new HashMap<>();
            for (int i = 0; i < partList.size(); i++) {
                String partListId = partList.get(i);
                domainObject.setId(partListId);
                //查找零件未同步，转换失败的
                MapList partListMapList = domainObject.getRelatedObjects(context,
                        JF_PLMConstants_mxJPO.rel_JFPartList2VPMReference, // relationship pattern
                        JF_PLMConstants_mxJPO.TYPE_VPMReference,                                    // object pattern
                        basicBolistSel,                            // object selects
                        basicRellistSel, // relationship selects
                        false,                                        // to direction
                        true,                                        // from direction
                        (short) 1,                                    // recursion level
                        "",                // object where clause
                        strWhere,
                        (short) 0
                );
                //所有的放到一起
                if (partListMapList.isEmpty()) {
                    continue;
                }
                listHashMap.put(partListId, partListMapList);
            }
            LOGGER.info("listHashMap:{}", listHashMap);
            //开始构造table表 邮件 调用构造邮件的方法
            if (UIUtil.isNotNullAndNotEmpty(sendEmails)) {
                listAttr.add(JF_PLMConstants_mxJPO.SELECT_ATTR_V_PART_NUMBER);
                listAttr.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JF_PartType);
                listAttr.add(SELECT_REVISION);
                listAttr.add(SELECT_CURRENT);
                listAttr.add(SELECT_OWNER);
                listAttr.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JF_PartNameCN);
                listAttr.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JF_PartNameEN);
                listAttr.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JSdataProcessProgress);
                listAttr.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JF_SyncStatus);
                LOGGER.info("listAttr:{}", listAttr);
                SendPartListEmails(context, listHashMap, oneOrAll, sendEmails, listAttr);
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * partList 问题数据发送邮件
     * @param context
     * @param listHashMap
     * @param oneOrAll
     * @author LIUJR
     * @throws
     * @return void
     * @date 2025/9/15 13:35
     * @description
     */
    public void SendPartListEmails(Context context, HashMap<String, MapList> listHashMap, String oneOrAll, String sendEmails, StringList listAttr) throws Exception{
        try {
            //拿取邮件文档
            // 创建多部分消息体
            MimeMultipart multipart = new MimeMultipart(); // 默认混合模式
            //邮件内容的html模板部分
            BodyPart msgBodyPart = new MimeBodyPart();
            //邮件内容的html模板部分
            String html = JF_PublicMethodClass_mxJPO.getPageHTMLResourceFile(context, "PartListEmails", "zh");
            org.jsoup.nodes.Document doc = Jsoup.parse(html);
            //邮件标识
            String emailFlag = "";
            Map map = new HashMap();
            DomainObject domainObject = DomainObject.newInstance(context);
            if (!"All".equalsIgnoreCase(oneOrAll)) {
                //单个PartList
                //判断是否有数据 如果有发邮件2 如果没有发邮件1
                domainObject.setId(oneOrAll);
                MapList mapList = listHashMap.get(oneOrAll);
                if (null == mapList ||mapList.isEmpty()) {
                    LOGGER.info("邮件1");
                    //邮件1
                    emailFlag = "OneSync";
                    doc.getElementById("OneSyncName").append(domainObject.getInfo(context, SELECT_NAME));
                } else {
                    //邮件2
                    LOGGER.info("邮件2");
                    doc.getElementById("OneNotFullSyncName").append(domainObject.getInfo(context, SELECT_NAME));
                    emailFlag = "OneNotFullSync";
                    //开始构造table表
                    for (int i = 0; i < mapList.size(); i++) {
                        map = (Map) mapList.get(i);
                        JF_SendEmailUtils_mxJPO.writeTableData(context, "OnePartListTbody", map, doc, listAttr);
                    }
                }
                LOGGER.info("emailFlag:{}", emailFlag);
                //链接地址
                //链接地址
                Properties prop = NioJDUtils.readPageObject(context, "JFJDConfig");
                String linkAddress = prop.getProperty("JF.3dspace.JFUrl").trim();
                linkAddress += oneOrAll;
                //设置链接地址
                org.jsoup.nodes.Element address = doc.getElementById("Address");
                //链接href
                address.attr("href", linkAddress);
                //设置显示的值
                address.text("点击查看PartList详情(Click to view PartList Details)");
                //将链接地址模块设置为显示
                org.jsoup.nodes.Element oneSync = doc.getElementById("LinkText");
                //将元素设置为可见
                String currentStyle = oneSync.attr("style");
                String newStyle = currentStyle.replace("display: none;", "display: block;");
                oneSync.attr("style", newStyle); // 更新style属性
                LOGGER.info("emailFlag:{}", emailFlag);
            } else  {
                //系统中所有有异常的partList集合
                emailFlag = "AllSysPartList";
                Set<Map.Entry<String, MapList>> entries = listHashMap.entrySet();
                for (Map.Entry<String, MapList> oEntry : entries) {
                    String partListId = oEntry.getKey();
                    domainObject.setId(partListId);
                    MapList mapList = oEntry.getValue();
                    //开始构造table表
                    for (int i = 0; i < mapList.size(); i++) {
                        map = (Map) mapList.get(i);
                        if (i == 0) {
                            //只有partList的第一组有
                            map.put("partListNameId", domainObject.getInfo(context, SELECT_NAME) + "@" + partListId);
                            map.put("size", String.valueOf(mapList.size()));
                        }
                        JF_SendEmailUtils_mxJPO.writeTableData(context, "AllPartListTbody", map, doc, listAttr);
                    }
                }
            }
            //将模块设置为显示
            org.jsoup.nodes.Element oneSync = doc.getElementById(emailFlag);
            //将元素设置为可见
            String currentStyle = oneSync.attr("style");
            String newStyle = currentStyle.replace("display: none;", "display: block;");
            oneSync.attr("style", newStyle); // 更新style属性
            String htmlContent = doc.toString();
            msgBodyPart.setContent(htmlContent, "text/html;charset=utf-8");//html代码部分
            multipart.addBodyPart(msgBodyPart);
            Boolean aBoolean = JF_SendEmailUtils_mxJPO.SendEmail(context, sendEmails, "zh".equalsIgnoreCase("zh") ? "PartList同步状态通知" : "PartList synchronization status notification", multipart);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @param context
     * @param args
     * @return int
     * @throws
     * @description partlist提交审核时校验零件清单不能为空 零件清单必填属性
     * @author CHENYAN
     * @date 2025/8/13 15:38
     */
    public int checkPartsInfoInPromoteReview(Context context, String[] args) throws Exception {
        LOGGER.info("------------------------------- checkPartsInfoInPromoteReview begin ------------------------------------------------");

        int iRes = 1;
        String strObjectId = args[0];
        DomainObject partListBo = DomainObject.newInstance(context, strObjectId);
        Map partlistMap = partListBo.getInfo(context, StringList.create(SELECT_ATTR_JFIsInit, "to[JFProject2PartList].from.id", "to[JFProject2PartList].from.name"));
        String strIsInit = (String) partlistMap.get(SELECT_ATTR_JFIsInit);
        String strProjectId = (String) partlistMap.get("to[JFProject2PartList].from.id");
        String strProjectName = (String) partlistMap.get("to[JFProject2PartList].from.name");
        String strMess = "";
        HashMap<Object, Object> argsMap = new HashMap<>();
        argsMap.put("isPush", Boolean.FALSE);
        argsMap.put("projectRole", PARTLIST_CHECK_PROJECT_ROLE_LIST);
        argsMap.put("projectId", strProjectId);
        Map checkResMap = JF_Util_mxJPO.checkProjectRoleIsMaintenanceByProjectIDAndRoleName(context, JPO.packArgs(argsMap));
        Boolean isMaintenance = (Boolean) checkResMap.get("isPass");
        StringList notInitProjectRoleList = (StringList) checkResMap.get("notInitProjectRole");
        if ("Y".equals(strIsInit) && isMaintenance) {
            StringList typeSelectList = JF_Util_mxJPO.basicBolistSel();
            typeSelectList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_V_PART_NUMBER);
            typeSelectList.add(SELECT_ATTR_JF_VPMReference_JF_ProcurementGroup);
            StringList relSelectList = JF_Util_mxJPO.basicRellistSel();
            StringList requireAttrList = new StringList();
            requireAttrList.add(SELECT_ATTR_JFCarryOver);
            requireAttrList.add(SELECT_ATTR_JFBicycleUsage);
            requireAttrList.add(SELECT_ATTR_JFTotalDemand);
            requireAttrList.add(SELECT_ATTR_JFDeliveryDate);
            requireAttrList.add(SELECT_ATTR_JFPartRequirementPlanDate);
            requireAttrList.add(SELECT_ATTR_JFDrawDataCompletionPlanDate);
            requireAttrList.add(SELECT_ATTR_JF3DReleasedDesignatedPlanDate);
            relSelectList.addAll(requireAttrList);
            requireAttrList.add(SELECT_ATTR_JF_VPMReference_JF_ProcurementGroup);
            MapList partListMapList = partListBo.getRelatedObjects(context,
                    JF_PLMConstants_mxJPO.rel_JFPartList2VPMReference, // relationship pattern
                    JF_PLMConstants_mxJPO.TYPE_VPMReference,                                    // object pattern
                    typeSelectList,                            // object selects
                    relSelectList, // relationship selects
                    false,                                        // to direction
                    true,                                        // from direction
                    (short) 1,                                    // recursion level
                    "",                // object where clause
                    "",
                    (short) 0);
            if (partListMapList.size() > 0) {
                StringList alertPartList = new StringList();
                for (int i = 0; i < partListMapList.size(); i++) {
                    Map partInfoMap = (Map) partListMapList.get(i);
                    String strTotalDemand = (String) partInfoMap.get(SELECT_ATTR_JFTotalDemand);
                    String strProcurementGroup = (String) partInfoMap.get(SELECT_ATTR_JF_VPMReference_JF_ProcurementGroup);
                    String strDeliveryDate = (String) partInfoMap.get(SELECT_ATTR_JFDeliveryDate);
                    String strCarryOver = (String) partInfoMap.get(SELECT_ATTR_JFCarryOver);
                    String strBicycleUsage = (String) partInfoMap.get(SELECT_ATTR_JFBicycleUsage);
                    String strPartRequirementPlanDate = (String) partInfoMap.get(SELECT_ATTR_JFPartRequirementPlanDate);
                    String strDrawDataCompletionPlanDate = (String) partInfoMap.get(SELECT_ATTR_JFDrawDataCompletionPlanDate);
                    String str3DReleasedDesignatedPlanDate = (String) partInfoMap.get(SELECT_ATTR_JF3DReleasedDesignatedPlanDate);
                    LOGGER.info("partInfoMap:{}",partInfoMap);
                    if (UIUtil.isNullOrEmpty(strTotalDemand)
                            || UIUtil.isNullOrEmpty(strProcurementGroup)
                            || UIUtil.isNullOrEmpty(strDeliveryDate) || UIUtil.isNullOrEmpty(strBicycleUsage) || UIUtil.isNullOrEmpty(strCarryOver)|| UIUtil.isNullOrEmpty(strPartRequirementPlanDate)
                            || UIUtil.isNullOrEmpty(strDrawDataCompletionPlanDate) || UIUtil.isNullOrEmpty(str3DReleasedDesignatedPlanDate)) {
                        String strPartName = (String) partInfoMap.get(SELECT_ATTR_V_PART_NUMBER);
                        if (UIUtil.isNullOrEmpty(strPartName)) {
                            strPartName = (String) partInfoMap.get(SELECT_NAME);
                        }
                        alertPartList.add(strPartName);
                        iRes = 1;
                    }
                }
                if (alertPartList.size() > 0) {
                    strMess = EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", context.getLocale(), "emxComponents.PartList.PartsAttrNotWrite");
                    StringList attrNlsAttrName = new StringList();
                    for (int i = 0; i < requireAttrList.size(); i++) {
                        String strAttrName = requireAttrList.get(i);
                        String strActualAttrName = strAttrName.replace("attribute[", "").replace("]", "");
                        String strNlsName = EnoviaResourceBundle.getAttributeI18NString(context, strActualAttrName, context.getLocale().toString());
                        attrNlsAttrName.add(strNlsName);
                    }
                    strMess = strMess.replace("{0}", alertPartList.join(",")).replace("{1}", attrNlsAttrName.join(","));
                } else {
                    iRes = 0;
                }
            } else {
                strMess = EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", context.getLocale(), "emxComponents.PartList.PartsNotEmpty");
            }
        } else {
            if (!isMaintenance) {
                StringList nlsRanges = EnoviaResourceBundle.getAttrRangeI18NStringList(context, ATTR_PROJECT_ROLE, notInitProjectRoleList, context.getLocale().toString());
                strMess = EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", context.getLocale(), "emxComponents.PartList.ProjectRoleNotInit");
                strMess = strMess.replace("{0}", strProjectName).replace("{1}", nlsRanges.join(","));

            } else {
                strMess = EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", context.getLocale(), "emxComponents.PartList.PartsNotInit");
            }
        }

        if (iRes != 0) {
            emxContextUtilBase_mxJPO.mqlWarning(context, strMess);
        }
        LOGGER.info("------------------------------- checkPartsInfoInPromoteReview end ------------------------------------------------");
        return iRes;
    }

    /**
     * @param context
     * @param args
     * @return void
     * @throws
     * @description partlist提升到审核中创建STD流程
     * @author CHENYAN
     * @date 2025/8/13 16:10
     */
    public void createRouteInReview(Context context, String[] args) throws Exception {
        LOGGER.info("------------------------------- createRouteInReview begin ------------------------------------------------");
        String strObjectId = args[0];
        DomainObject partListBo = DomainObject.newInstance(context, strObjectId);
        String strProjectId = partListBo.getInfo(context, "to[JFProject2PartList].from.id");
        String strRouteTitle = EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", context.getLocale(), "emxComponents.PartList.RouteTitle");
        String strRouteDescription = EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", context.getLocale(), "emxComponents.PartList.RouteDescription");
//把JFPartRequirementPlanDate 零件(T0)需求计划日期  属性的值同步到 JFPartRequirementCommitmentDate 零件(T0)供应商承诺日期
        StringList typeSelectList = JF_Util_mxJPO.basicBolistSel();
        StringList relSelectList1 = JF_Util_mxJPO.basicRellistSel();
        relSelectList1.add(SELECT_ATTR_JFPartRequirementPlanDate);
        MapList partListMapList = partListBo.getRelatedObjects(context,
                JF_PLMConstants_mxJPO.rel_JFPartList2VPMReference, // relationship pattern
                JF_PLMConstants_mxJPO.TYPE_VPMReference,                                    // object pattern
                typeSelectList,                            // object selects
                relSelectList1, // relationship selects
                false,                                        // to direction
                true,                                        // from direction
                (short) 1,                                    // recursion level
                "",                // object where clause
                "",
                (short) 0);

        if (partListMapList.size() > 0) {
            try {
                ContextUtil.pushContext(context);
                for (int i = 0; i < partListMapList.size(); i++) {
                    Map partInfoMap = (Map) partListMapList.get(i);
                    String relId = (String) partInfoMap.get(SELECT_RELATIONSHIP_ID);
                    DomainRelationship relObj = DomainRelationship.newInstance(context, relId);
                    String strPartRequirementPlanDate = (String) partInfoMap.get(SELECT_ATTR_JFPartRequirementPlanDate);
                    relObj.setAttributeValue(context, ATTR_JFPartRequirementCommitmentDate, strPartRequirementPlanDate);
                }
            }finally {
                ContextUtil.popContext(context);
            }
        }

            //end
        partListBo.setId(strProjectId);
        StringBuilder relWhereSb = new StringBuilder();
        StringList relSelectList = JF_Util_mxJPO.basicRellistSel();
        relSelectList.add("attribute[Project Role]");
        relWhereSb.append("attribute[Project Role] ");
        relWhereSb.append(" matchlist '");
        relWhereSb.append(PARTLIST_CHECK_PROJECT_ROLE_LIST.join(","));
        relWhereSb.append("'");
        relWhereSb.append(" ','");
        MapList mapList = partListBo.getRelatedObjects(
                context,
                DomainRelationship.RELATIONSHIP_MEMBER,
                DomainConstants.TYPE_PERSON,
                JF_Util_mxJPO.basicBolistSel(),
                relSelectList,
                false,
                true,
                (short) 1, // recursion level
                "", //object where clause
                relWhereSb.toString(), //relationship where clause
                0
        );
        LOGGER.info("mapList:{}", mapList);
        Integer approveIndex = 1;
        MapList approveList = new MapList();
        for (int i = 0; i < mapList.size(); i++) {
            Map personMap = (Map) mapList.get(i);
            String strPeronId = (String) personMap.get(SELECT_ID);
            String strProjectRoleName = (String) personMap.get("attribute[Project Role]");
            StringList nlsRanges = EnoviaResourceBundle.getAttrRangeI18NStringList(context, ATTR_PROJECT_ROLE, StringList.create(strProjectRoleName), context.getLocale().toString());
            String strNewTitle = strRouteTitle.replace("{}", nlsRanges.get(0));
            Map managerMap = JF_PublicMethodClass_mxJPO.getMapAnyOrAll(strPeronId, strNewTitle, "true", approveIndex.toString(), "All");
            approveList.add(managerMap);
        }
        JF_Route_mxJPO jf_route = new JF_Route_mxJPO(context, args);
        String state = "state_Review";//在哪个状态增加流程
        String policy = "policy_JFPartList";//哪个Policy上面
        String routeId = jf_route.createAndStartRoute(context, approveList, strObjectId, state, policy, strRouteDescription);
        //起 Job 下载EBOM清单
        Job job = new Job("JF_PartList", "downLoadSnapShowEBOM", args, false);
        job.setContextObject(strObjectId);
        job.setTitle("partlist生成EBOM清单Job");
        job.createAndSubmit(context);
        LOGGER.info("------------------------------- createRouteInReview end ------------------------------------------------");
    }

    /**
     * @param context
     * @param args
     * @return void
     * @throws
     * @description 下载partlist关联快照的EBOM清单并生成文档对象关联到partlist
     * @author CHENYAN
     * @date 2025/8/20 16:46
     */
    public void downLoadSnapShowEBOM(Context context, String[] args) throws Exception {
        String strPartListId = args[0];
        DomainObject partListBo = DomainObject.newInstance(context, strPartListId);
        String strProjectId = partListBo.getInfo(context, "to[JFProject2PartList].from.id");
        String strSnapshotId = partListBo.getInfo(context, "from[JFPartList2Snapshot].to.id");
        HashSet<String> snapshotRootPartIdSet = getSnapshotConnectRootPart(context, strProjectId, strSnapshotId);
        LOGGER.info("snapshotRootPartIdSet:{}", snapshotRootPartIdSet);
        Map argsMap = new HashMap<>();
        argsMap.put("ids", snapshotRootPartIdSet);
        String strSPacePath = JF_PublicMethodClass_mxJPO.get3DspaceServicePath("WEB-INF");
        LOGGER.info("strSPacePath:{}", strSPacePath);
        argsMap.put("path", strSPacePath);
        argsMap.put("projectId", strProjectId);
        String[] jpoArgs = JPO.packArgs(argsMap);
        Map res = JPO.invoke(context, "JF_LastRevisionBOMService", null, "downLoadLastRevisionBOMInfoList", jpoArgs, Map.class);
        Workbook workbook = (Workbook) res.get("file");
        String strFileName = (String) res.get("fileName");
        String strPrePath = context.createWorkspace();
        strPrePath = strPrePath.endsWith("/") ? strPrePath : JF_PublicMethodClass_mxJPO.buildStringInStrings(strPrePath, "/");
        strPrePath = strPrePath + strFileName;
        LOGGER.info("strPrePath:{}", strPrePath);
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(strPrePath);
            workbook.write(fos);
            File file = new File(strPrePath);
            Map createDocMap = new HashMap();
            createDocMap.put("file", file);
            ContextUtil.startTransaction(context, true);
            CommonDocument newDoc = JF_FileUtils_mxJPO.createDoc(context, JPO.packArgs(createDocMap));
            DomainObject partListBO = DomainObject.newInstance(context, strPartListId);
            DomainRelationship rel = DomainRelationship.connect(context, partListBO, JF_PLMConstants_mxJPO.REL_ReferenceDocument, newDoc);
            //标识为EBOM清单
            ContextUtil.pushContext(context);
            rel.setAttributeValue(context, ATTR_PROJECT_ROLE, ATTR_PROJECT_ROLE_RANGE_EBOMFILE);
            //关联partlist
            ContextUtil.commitTransaction(context);
            file.delete();
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
            ContextUtil.abortTransaction(context);
        } finally {
            ContextUtil.popContext(context);
            if (Objects.nonNull(fos)) {
                fos.close();
            }
        }

    }

    /**
     * @param context
     * @param strPartListId
     * @return void
     * @throws
     * @description 断开partlist和零件的关系
     * @author CHENYAN
     * @date 2025/8/8 10:36
     */
    public void disconnectPartByPartList(Context context, String strPartListId) throws Exception {
        DomainObject partListBO = DomainObject.newInstance(context, strPartListId);
        StringList relIdList = partListBO.getInfoList(context, "from[JFPartList2VPMReference].id");
        if (relIdList.size() > 0) {
            try {
                ContextUtil.startTransaction(context, true);
                DomainRelationship.disconnect(context, relIdList.toStringArray());
                ContextUtil.commitTransaction(context);
            } catch (FrameworkException e) {
                ContextUtil.abortTransaction(context);
                throw e;
            }
        }
    }

    /**
     * @param context
     * @param args
     * @return matrix.util.StringList
     * @throws
     * @description partlist中新增零件查询过滤掉已经关联零件
     * @author CHENYAN
     * @date 2025/8/8 15:29
     */
    public StringList getProjectPartByPartListId(Context context, String[] args) throws Exception {
        Map parameterMap = JPO.unpackArgs(args);
        String strObjectId = (String) parameterMap.get(STRING_OBJECTID);
//        StringList alreadyConnPartIdList ;
        Set<String> searchPartIdSet = new HashSet<>(100);
        try {
            ContextUtil.pushContext(context);
            DomainObject objectProject = DomainObject.newInstance(context, strObjectId);
            String strProjectId = objectProject.getInfo(context, "to[JFProject2PartList].from.id");
            String strSnapshotId = objectProject.getInfo(context, "from[JFPartList2Snapshot].to.id");
            HashSet<String> snapshotRootPartIdSet = getSnapshotConnectRootPart(context, strProjectId, strSnapshotId);
            for (String strRootPartId : snapshotRootPartIdSet) {
                objectProject.setId(strRootPartId);
                MapList partIdList = objectProject.getRelatedObjects(context,
                        JF_PLMConstants_mxJPO.REL_Instance, // relationship pattern
                        JF_PLMConstants_mxJPO.TYPE_VPMReference,                                    // object pattern
                        JF_Util_mxJPO.basicBolistSel(),                            // object selects
                        JF_Util_mxJPO.basicRellistSel(), // relationship selects
                        false,                                        // to direction
                        true,                                        // from direction
                        (short) 0,                                    // recursion level
                        "",                // object where clause
                        "",
                        (short) 0);
                searchPartIdSet.addAll((Set<String>) partIdList.stream().map(m -> {
                    Map partInfo = (Map) m;
                    return partInfo.get(SELECT_ID);
                }).collect(Collectors.toSet()));
            }
        } finally {
            ContextUtil.popContext(context);
        }
        LOGGER.info("searchPartIdSet:{}", searchPartIdSet);
        return StringList.create(searchPartIdSet);
    }

    /**
     * @param context
     * @param args
     * @return matrix.util.StringList
     * @throws
     * @description 获取partlist关联的文档
     * @author CHENYAN
     * @date 2025/8/19 11:01
     */
    public StringList getDocumentByPartListId(Context context, String[] args) throws Exception {
        Map parameterMap = JPO.unpackArgs(args);
        String strObjectId = (String) parameterMap.get(STRING_OBJECTID);
        String strLoginUser = context.getUser();
//        StringList alreadyConnPartIdList ;
        DomainObject partlist = DomainObject.newInstance(context, strObjectId);
        MapList docIdMapList = partlist.getRelatedObjects(context,
                JF_PLMConstants_mxJPO.REL_ReferenceDocument, // relationship pattern
                JF_PLMConstants_mxJPO.TYPE_Document,                                    // object pattern
                JF_Util_mxJPO.basicBolistSel(),                            // object selects
                JF_Util_mxJPO.basicRellistSel(), // relationship selects
                false,                                        // to direction
                true,                                        // from direction
                (short) 0,                                    // recursion level
                JF_PublicMethodClass_mxJPO.buildStringInStrings("owner=='", strLoginUser, "'"),                // object where clause
                "attribute[Project Role]!='EBOMFile'",
                (short) 0);
        StringList docIdList = (StringList) docIdMapList.stream().map(m -> {
            Map docMap = (Map) m;
            return docMap.get(SELECT_ID);
        }).collect(Collectors.toCollection(StringList::new));
        LOGGER.info("docIdList:{}", docIdList);
        return docIdList;
    }

    /**
    *
    *@description 获取登录人在partlist中上传的附件
    *@param context
	*@param args
    *@return com.matrixone.apps.domain.util.MapList
    *@throws
    *@author CHENYAN
    *@date 2025/8/24 16:58
    */
    public MapList getPartListDocByLoginUser(Context context, String[] args) throws Exception {
        LOGGER.info("----------------------------- getPartListDocByLoginUser begin --------------------------------------------");
        Map parameterMap = JPO.unpackArgs(args);
        LOGGER.info("parameterMap:{}", parameterMap);
        String strObjectId = (String) parameterMap.get(STRING_OBJECTID);
        String strLoginUser = context.getUser();
//        StringList alreadyConnPartIdList ;
        DomainObject partlist = DomainObject.newInstance(context, strObjectId);
        MapList docIdMapList = partlist.getRelatedObjects(context,
                JF_PLMConstants_mxJPO.REL_ReferenceDocument, // relationship pattern
                JF_PLMConstants_mxJPO.TYPE_Document,                                    // object pattern
                JF_Util_mxJPO.basicBolistSel(),                            // object selects
                JF_Util_mxJPO.basicRellistSel(), // relationship selects
                false,                                        // to direction
                true,                                        // from direction
                (short) 0,                                    // recursion level
               "",                // object where clause
                "attribute[Project Role]!='EBOMFile' && owner=='"+strLoginUser+"'",
                (short) 0);
        LOGGER.info("docIdMapList:{}", docIdMapList);
        LOGGER.info("----------------------------- getPartListDocByLoginUser end --------------------------------------------");

        return docIdMapList;
    }

    /**
     * @param context
     * @param args
     * @return matrix.util.StringList
     * @throws
     * @description 获取partlist表格中处理人
     * @author CHENYAN
     * @date 2025/8/8 16:23
     */
    public StringList getPartListProcessingPerson(Context context, String[] args) throws Exception {
        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        Map columnMap = (Map) programMap.get("columnMap");
        MapList objectList = (MapList) programMap.get("objectList");
        StringList processingPersonList = new StringList(objectList.size());
        DomainObject partList = DomainObject.newInstance(context);

        for (int i = 0; i < objectList.size(); i++) {
            Map partListInfoMap = (Map) objectList.get(i);
            String strColValue = "";
            String strCurrent = (String) partListInfoMap.get(SELECT_CURRENT);
            if (!"InWork".equals(strCurrent)){
                String strPartListId = (String) partListInfoMap.get(SELECT_ID);
                partList.setId(strPartListId);
                //获取partlist神审核流程流程
                MapList personMapList = partList.getRelatedObjects(context,
                        "Object Route,Route Node", // relationship pattern
                        "Person,Route",                                    // object pattern
                        JF_Util_mxJPO.basicBolistSel(),                            // object selects
                        JF_Util_mxJPO.basicRellistSel(), // relationship selects
                        false,                                        // to direction
                        true,                                        // from direction
                        (short) 2,                                    // recursion level
                        "",                // object where clause
                        "",
                        (short) 0);
                StringList fullNameList = new StringList();
                for (int i1 = 0; i1 < personMapList.size(); i1++) {
                    Map personMap = (Map) personMapList.get(i1);
                    String strPersonName = (String) personMap.get(SELECT_NAME);
                    String strFullName = PersonUtil.getFullName(context, strPersonName);
                    fullNameList.add(strFullName);
                }
                strColValue = fullNameList.join(",");
                fullNameList.clear();
            }
            processingPersonList.add(strColValue);
        }
        return processingPersonList;
    }

    /**
     * @param context
     * @param args
     * @return matrix.util.StringList
     * @throws
     * @description partlist表格编辑权限
     * @author CHENYAN
     * @date 2025/8/8 16:26
     */
    public StringList getPartListEditTableAccess(Context context, String[] args) throws Exception {
        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        String strLoginUser = context.getUser();
        MapList objectList = (MapList) programMap.get("objectList");
        StringList editAccessList = new StringList(objectList.size());
        for (int i = 0; i < objectList.size(); i++) {
            Boolean isEdit = Boolean.FALSE;
            Map partListInfoMap = (Map) objectList.get(i);
            String strCurrent = (String) partListInfoMap.get(SELECT_CURRENT);
            String strOwner = (String) partListInfoMap.get(SELECT_OWNER);
            if ("InWork".equals(strCurrent) && strLoginUser.equals(strOwner)) {
                isEdit = Boolean.TRUE;
            }
            editAccessList.add(isEdit.toString());
        }
        return editAccessList;
    }

    /**
     * @param context
     * @param args
     * @return void
     * @throws
     * @description 修改partlist属性
     * @author CHENYAN
     * @date 2025/8/11 13:06
     */
    public void updatePartListTable(Context context, String[] args) throws Exception {
        Map paramsMap = (Map) JPO.unpackArgs(args);
        Map columnMap = (Map) paramsMap.get(JF_PLMConstants_mxJPO.STRING_COLUMNMAP);
        Map requestMap = (Map) paramsMap.get("requestMap");
        String strTableName = (String) requestMap.get("selectedTable");
        String strParentOID = (String) requestMap.get("parentOID");
        String strAttrName = (String) columnMap.get(DomainConstants.SELECT_NAME);
        HashMap paramMap = (HashMap) paramsMap.get(JF_PLMConstants_mxJPO.STRING_PARAMMAP);
        String strObjectId = (String) paramMap.get(JF_PLMConstants_mxJPO.STRING_OBJECTID);
        String strNewValue = (String) paramMap.get(STRING_NEW_VALUE);
        String strRelId = (String) paramMap.get(STRING_RELID);
        LOGGER.info("strTableName:{}", strTableName);
        LOGGER.info("strParentOID:{}", strParentOID);
        LOGGER.info("strAttrName:{}", strAttrName);
        LOGGER.info("strObjectId:{}", strObjectId);
        LOGGER.info("strRelId:{}", strRelId);
        LOGGER.info("strNewValue:{}", strNewValue);

        try {
            ContextUtil.startTransaction(context, true);
            DomainObject partListBO = DomainObject.newInstance(context, strObjectId);
            if ("ConnectionSnapShot".equals(strAttrName)) {
                try {
                    String strSnapshotRelId = partListBO.getInfo(context, "from[JFPartList2Snapshot].id");
                    partListBO.getRevisions(context);
                    if (UIUtil.isNotNullAndNotEmpty(strSnapshotRelId)) {
                        DomainRelationship.disconnect(context, strSnapshotRelId);
                    }
                    DomainRelationship.connect(context, partListBO, JF_PLMConstants_mxJPO.rel_JFPartList2Snapshot, DomainObject.newInstance(context, strNewValue));
                    //设置初始化为 N
                    partListBO.setAttributeValue(context, ATTR_JFIsInit, "N");
                } catch (Exception e) {
                    ContextUtil.abortTransaction(context);
                    throw e;
                }
            } else {
                partListBO.setAttributeValue(context, strAttrName, strNewValue);
            }
            ContextUtil.commitTransaction(context);
        } catch (Exception e) {
            ContextUtil.abortTransaction(context);
            throw new RuntimeException(e);
        }
    }

    /**
     * @param context
     * @param args
     * @return void
     * @throws
     * @description 更新partlist关联的快照
     * @author CHENYAN
     * @date 2025/8/6 14:08
     */
    public void updateConnectionSnapshot(Context context, String[] args) throws Exception {
        LOGGER.info("------------------------------ updateConnectionSnapshot begin -----------------------------------------");
        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        Map paramMap = (Map) programMap.get("paramMap");
        Map fieldMap = (Map) programMap.get("fieldMap");
        String strNewId = (String) paramMap.get("New OID");
        String strNewValue = (String) paramMap.get("New Value");
        String strFieldName = (String) fieldMap.get("name");
        String strObjectId = (String) paramMap.get("objectId");
        LOGGER.info("strNewId:{}", strNewId);
        try {
            ContextUtil.startTransaction(context, true);
            DomainObject partListBO = DomainObject.newInstance(context, strObjectId);
            String strRelId = partListBO.getInfo(context, "from[JFPartList2Snapshot].id");
            if (UIUtil.isNotNullAndNotEmpty(strRelId)) {
                DomainRelationship.disconnect(context, strRelId);
            }
            DomainRelationship.connect(context, partListBO, JF_PLMConstants_mxJPO.rel_JFPartList2Snapshot, DomainObject.newInstance(context, strNewId));
            //设置初始化为 N
            partListBO.setAttributeValue(context, ATTR_JFIsInit, "N");
            ContextUtil.commitTransaction(context);
        } catch (Exception e) {
            ContextUtil.abortTransaction(context);
            throw e;
        }
        LOGGER.info("------------------------------ updateConnectionSnapshot end -----------------------------------------");

    }

    /**
     * @param context
     * @param strProjectId
     * @param strSnapshotId
     * @return java.util.HashSet<java.lang.String>
     * @throws
     * @description 获取快照下的整椅
     * @author CHENYAN
     * @date 2025/8/11 16:04
     */
    public HashSet<String> getSnapshotConnectRootPart(Context context, String strProjectId, String strSnapshotId) throws Exception {
        DomainObject snapshotBO = DomainObject.newInstance(context, strSnapshotId);
        StringList snapshotList = snapshotBO.getInfoList(context, "from[JFSnapshot2VPMReference].to.id");
        //项目下面整椅清单
        HashMap<Object, Object> argsMap = new HashMap<>();
        argsMap.put(STRING_OBJECTID, strProjectId);
        //快照中整椅集合
        HashSet<String> snapRootPartSet = new HashSet<>();
        MapList rootpartMapList = JPO.invoke(context, "JF_VPMReferenceEBOM", null, "getZeroALLPart", JPO.packArgs(argsMap), MapList.class);
        for (int i = 0; i < rootpartMapList.size(); i++) {
            Map rootMap = (Map) rootpartMapList.get(i);
            String strRootPartId = (String) rootMap.get(SELECT_ID);
            if (snapshotList.contains(strRootPartId)) {
                snapRootPartSet.add(strRootPartId);
            }
        }
        return snapRootPartSet;
    }

    /**
     * @param context
     * @param args
     * @return java.util.Map
     * @throws
     * @description partlist添加零件逻辑
     * @author CHENYAN
     * @date 2025/8/18 11:42
     */
    public Map partListAddPart(Context context, String[] args) throws Exception {
        LOGGER.info("--------------------------- partListAddPart begin -----------------------------------------");
        Map resMap = new HashMap();
        Map argsMap = JPO.unpackArgs(args);
        String strCode = "200";
        Set partIdSet = (Set) argsMap.get("selectPartId");
        String strPartListId = (String) argsMap.get("partListId");
        LOGGER.info("argsMap:{}", argsMap);
        DomainObject partListBO = DomainObject.newInstance(context, strPartListId);
        String strProjectId = partListBO.getInfo(context, "to[JFProject2PartList].from.id");
        DomainObject projectObj = DomainObject.newInstance(context,strProjectId);
        String projectDescription = projectObj.getDescription(context);
        String ATTR_JFProducingArea = projectObj.getAttributeValue(context,JF_PLMConstants_mxJPO.ATTR_JFProducingArea);
        String ATTR_JFPartListType = partListBO.getAttributeValue(context,JF_PLMConstants_mxJPO.ATTR_JFPartListType);
        LOGGER.info("projectDescription:{}",projectDescription);
        JF_VPMReferenceEBOM_mxJPO jfVpmReferenceEBOMMxJPO = new JF_VPMReferenceEBOM_mxJPO();
        DomainObject partBO = DomainObject.newInstance(context, strPartListId);
        StringList defaultAttrList = new StringList();
        defaultAttrList.add(SELECT_ATTR_JFDIRECT_BUY);
        defaultAttrList.add(SELECT_ATTR_JF_ProcurementType);
        defaultAttrList.add(SELECT_ATTR_JF_ProjectRel);
        HashMap setAttrMap = new HashMap<>();
        HashMap setRelAttrMap = new HashMap<>();
        // 零件{}已经在零件清单中，请移除后重新添加！
        StringList alreadyConnPartIdList = partBO.getInfoList(context, "from[JFPartList2VPMReference].to.id");
        LOGGER.info("alreadyConnPartIdList:{}", alreadyConnPartIdList);
        //过滤出已经关联过零件id
        Set connPartIdSet = (Set) partIdSet.stream().filter(oPartId -> {
            String strSelectPartId = (String) oPartId;
            return alreadyConnPartIdList.contains(strSelectPartId);
        }).collect(Collectors.toSet());
        StringList partInfoSelectList = new StringList(2);
        partInfoSelectList.add(SELECT_NAME);
        partInfoSelectList.add(SELECT_ATTR_V_PART_NUMBER);
        StringList alertPartNameList = new StringList();
        if (connPartIdSet.size() > 0) {
            MapList alertPartInfoList = DomainObject.getInfo(context, (String[]) connPartIdSet.toArray(new String[0]), partInfoSelectList);
            for (int i = 0; i < alertPartInfoList.size(); i++) {
                Map partInfo = (Map) alertPartInfoList.get(i);
                String strPartName = (String) partInfo.get(SELECT_ATTR_V_PART_NUMBER);
                if (UIUtil.isNullOrEmpty(strPartName)) {
                    strPartName = (String) partInfo.get(SELECT_NAME);
                }
                alertPartNameList.add(strPartName);
            }
            String strFailMess = EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", context.getLocale(), "emxComponents.PartList.AddPartRepeatFail");
            strFailMess += alertPartNameList.join(",");
            resMap.put("mess", strFailMess);
            strCode = "404";
            resMap.put("code", strCode);
            return resMap;
        }
        try {
            ContextUtil.startTransaction(context, true);
            for (Object oPartId : partIdSet) {
                String strPartId = (String) oPartId;
                partBO.setId(strPartId);
//                StringList partConnProjectList = jfVpmReferenceEBOMMxJPO.getPartZeroProject(context, strPartId);
                Map defaultAttrMap = partBO.getInfo(context, defaultAttrList);
                String strDirectBuy = (String) defaultAttrMap.get(SELECT_ATTR_JFDIRECT_BUY);
                String strProcurementType = (String) defaultAttrMap.get(SELECT_ATTR_JF_ProcurementType);
                String strJF_ProjectRel = (String) defaultAttrMap.get(SELECT_ATTR_JF_ProjectRel);
                LOGGER.info("strJF_ProjectRel222:{}",strJF_ProjectRel);
                if (UIUtil.isNullOrEmpty(strProcurementType)) {
                    setAttrMap.put(ATTR_JF_ProcurementType, ATTR_JF_ProcurementType_RANGE_BUY);
                }
                if (UIUtil.isNullOrEmpty(strDirectBuy)) {
                    setAttrMap.put(ATTR_JFDIRECT_BUY, ATTR_ATTR_JFDIRECT_BUY_RANGE_N);
                }
                //设置采购类型和  DIRECT_BUY 并清空修改属性集合
                if (setAttrMap.size() > 0) {
                    partBO.setAttributeValues(context, setAttrMap);
                    setAttrMap.clear();
                }
                if (projectDescription.equalsIgnoreCase(strJF_ProjectRel)) {
                    setRelAttrMap.put(ATTR_JFCarryOver, ATTR_JFCarryOver_RANGE_New);
                } else{
                    setRelAttrMap.put(ATTR_JFCarryOver, ATTR_JFCarryOver_RANGE_CarryOver);
                }
                //设置量产地
                setRelAttrMap.put(JF_PLMConstants_mxJPO.ATTR_JFOutputLocation,ATTR_JFProducingArea);

                //设置采购类型
                setRelAttrMap.put(ATTR_PartList_JFProcurementType, strProcurementType);
                //设置交付类型
                setRelAttrMap.put(JF_PLMConstants_mxJPO.ATTR_JFDeliveryPhase,ATTR_JFPartListType);
                //连接partlist
                DomainRelationship rel = DomainRelationship.connect(context, partListBO, JF_PLMConstants_mxJPO.rel_JFPartList2VPMReference, partBO);
                rel.setAttributeValues(context, setRelAttrMap);
            }
            ContextUtil.commitTransaction(context);
        } catch (Exception e) {
            ContextUtil.abortTransaction(context);
            strCode = "404";
            LOGGER.error(e.getMessage());
            String strFailMess = EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", context.getLocale(), "emxComponents.PartList.AddPartFail");

            resMap.put("mess", strFailMess);
        }
        resMap.put("code", strCode);
        return resMap;
    }


    /**
     * @param context
     * @param args
     * @return java.util.Map
     * @throws
     * @description 重新初始化partlist零件清单
     * @author CHENYAN
     * @date 2025/8/12 15:13
     */
    public Map partListInitializationPart(Context context, String[] args) throws Exception {
        LOGGER.info("-------------------------- partListInitializationPart begin ----------------------------------------------------");
        Map resMap = new HashMap();
        Map argsMap = JPO.unpackArgs(args);
        String strCode = "200";
        String strPartListId = (String) argsMap.get("partListId");
        DomainObject partListBO = DomainObject.newInstance(context, strPartListId);
        String strProjectId = partListBO.getInfo(context, "to[JFProject2PartList].from.id");
        String strSnapshotId = partListBO.getInfo(context, "from[JFPartList2Snapshot].to.id");
        LOGGER.info("strProjectId:{}", strProjectId);
        LOGGER.info("strSnapshotId:{}", strSnapshotId);

        try {
            ContextUtil.startTransaction(context, true);
            DomainObject newPartListBO = DomainObject.newInstance(context, strPartListId);
            StringList relIdList = newPartListBO.getInfoList(context, "from[JFPartList2VPMReference].id");
            //移除原有partlist和零件关系
            if (relIdList.size() > 0) {
                DomainRelationship.disconnect(context, relIdList.toStringArray());
            }
            DomainObject snapshotBO = DomainObject.newInstance(context, strSnapshotId);
            HashSet<String> snapshotRootPartIdSet = null;
            try {
                ContextUtil.pushContext(context);
                snapshotRootPartIdSet = getSnapshotConnectRootPart(context, strProjectId, strSnapshotId);
            } finally {
                ContextUtil.popContext(context);
            }
            LOGGER.info("snapshotRootPartIdSet:{}", snapshotRootPartIdSet);
//            DomainRelationship.connect(context, DomainObject.newInstance(context,strProjectId), JF_PLMConstants_mxJPO.rel_JFProject2PartList, newPartListBO);
//            DomainRelationship.connect(context, newPartListBO, JF_PLMConstants_mxJPO.rel_JFPartList2Snapshot,snapshotBO );
            //初始化partlist零件清单
            initializationPartInfoByPartList(context, snapshotRootPartIdSet, strProjectId, strPartListId);
            ContextUtil.commitTransaction(context);
        } catch (Exception e) {
            ContextUtil.abortTransaction(context);
            strCode = "404";
            LOGGER.error(e.getMessage());
            String strFailMess = EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", context.getLocale(), "emxComponents.PartList.InitializationPartFail");
            resMap.put("mess", strFailMess);
        }
        resMap.put("code", strCode);
        LOGGER.info("-------------------------- partListInitializationPart end ----------------------------------------------------");
        return resMap;
    }

    /**
     * @param context
     * @param args
     * @return void
     * @throws
     * @description 更新partlist中零件清单属性
     * @author CHENYAN
     * @date 2025/8/13 15:24
     */
    public void updatePartListPartsTable(Context context, String[] args) throws Exception {
        LOGGER.info("------------------------------ updatePartListPartsTable begin -----------------------------------------");
        Map paramsMap = (Map) JPO.unpackArgs(args);
        LOGGER.info("paramsMap:{}", paramsMap);
        Map columnMap = (Map) paramsMap.get(JF_PLMConstants_mxJPO.STRING_COLUMNMAP);
        Map requestMap = (Map) paramsMap.get(STRING_REQUESTMAP);
        String strTableName = (String) requestMap.get("selectedTable");
        String strParentOID = (String) requestMap.get(STRING_PARENTOID);
        String strAttrName = (String) columnMap.get(DomainConstants.SELECT_NAME);
        HashMap paramMap = (HashMap) paramsMap.get(JF_PLMConstants_mxJPO.STRING_PARAMMAP);
        String strObjectId = (String) paramMap.get(JF_PLMConstants_mxJPO.STRING_OBJECTID);
        String strNewValue = (String) paramMap.get(STRING_NEW_VALUE);
        String strRelId = (String) paramMap.get(STRING_RELID);
        LOGGER.info("strTableName:{}", strTableName);
        LOGGER.info("strParentOID:{}", strParentOID);
        LOGGER.info("strAttrName:{}", strAttrName);
        LOGGER.info("strObjectId:{}", strObjectId);
        LOGGER.info("strRelId:{}", strRelId);
        boolean isRelAttr = true;
        switch (strAttrName) {
            case "JF3DReleasedDesignatedPlanDate","JFDrawDataCompletionPlanDate", "JFSupplierDesignatedPlanDate", "JFSSOWIssueDate", "JFDeliveryDate","JFPartRequirementPlanDate","JFPartRequirementCommitmentDate": {
                double iClientTimeOffset = (Double.parseDouble((String) requestMap.get("timeZone")));
                strNewValue = eMatrixDateFormat.getFormattedInputDate(strNewValue, iClientTimeOffset, context.getLocale());
                break;
            }
            case "JF_VPMReference.JF_ProcurementGroup": {
                strAttrName = "JF_VPMReference.JF_ProcurementGroup";
                isRelAttr = false;
                break;
            }
            case "ProcurementType": {
                strAttrName = "JF_VPMReference.JF_ProcurementType";
                isRelAttr = false;
                break;
            }
            case "DirectBuy": {
                strAttrName = "JF_VPMReference.JF_DirectBuy";
                isRelAttr = false;
                break;
            }
            case "Description" : {
                strAttrName = "JF_Remark";
                break;
            } case "JFProcurementType": {
                strAttrName = "JFProcurementType";
                isRelAttr = true;
                break;
            }
            default: {
                break;
            }
        }
        LOGGER.info("strNewValue:{}", strNewValue);
        strAttrName = strAttrName.trim();
        DomainRelationship rel = null;
        try {
            ContextUtil.pushContext(context);
            ContextUtil.startTransaction(context, true);
            if (isRelAttr) {
                rel = DomainRelationship.newInstance(context, strRelId);
                rel.setAttributeValue(context, strAttrName, strNewValue);
            } else {
                DomainObject part = DomainObject.newInstance(context, strObjectId);
                part.setAttributeValue(context, strAttrName, strNewValue);
            }
            ContextUtil.commitTransaction(context);
        } catch (FrameworkException e) {
            ContextUtil.abortTransaction(context);
            throw e;
        } finally {
            ContextUtil.popContext(context);
        }
        LOGGER.info("------------------------------ updatePartListPartsTable end -----------------------------------------");
    }

    /**
     * @param context
     * @param args
     * @return matrix.util.StringList
     * @throws
     * @description 获取partlist中零件清单编辑权限
     * @author CHENYAN
     * @date 2025/8/13 15:25
     */
    public StringList getPartListPartsEditTableAccess(Context context, String[] args) throws Exception {
        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        Map requestMap = (Map) programMap.get(STRING_REQUESTMAP);
        String strLoginUser = context.getUser();
        MapList objectList = (MapList) programMap.get("objectList");
        String strObjectId = (String) requestMap.get(STRING_OBJECTID);
        StringList editAccessList = new StringList(objectList.size());
        for (int i = 0; i < objectList.size(); i++) {
            Map objectMap = (Map) objectList.get(i);
            String isEdit = (String) objectMap.get("isEdit");
            LOGGER.info("isEdit:{}", isEdit);
            editAccessList.add(isEdit);
        }
        return editAccessList;
    }

    /**
    * 构造partList 采购分组属性
    * @param context
	* @param args
    * @author LIUJR
    * @throws
    * @return java.lang.String
    * @date 2025/9/11 11:09
    * @description
    */
    public StringList buildJFProcurementGroupHtml(Context context,String[] args) throws Exception{
        StringList res = new StringList();
        Map argMaps = JPO.unpackArgs(args);
        LOGGER.info("argMaps;{}",argMaps);
        String strRapidOfferId = (String) argMaps.get("objectId");
        if (UIUtil.isNullOrEmpty(strRapidOfferId)){
            Map paramList = (Map) argMaps.get("paramList");
            LOGGER.info("paramList;{}",paramList);
            strRapidOfferId = (String) paramList.get("parentOID");
        }
        MapList argMapList = (MapList) argMaps.get(JF_PLMConstants_mxJPO.STRING_OBJECTLIST);
        DomainObject domainObject = DomainObject.newInstance(context);
        String strLanguage = context.getSession().getLanguage();
        for (int i = 0; i < argMapList.size(); i++) {
            Map infoMap = (Map) argMapList.get(i);
            String strId = (String) infoMap.get(SELECT_ID);
            domainObject.setId(strId);
            String procurementGroup = domainObject.getAttributeValue(context, JF_PLMConstants_mxJPO.ATTR_JF_VPMReference_JF_ProcurementGroup);
            //国际化
            String value = EMPTY_STRING;
            if (UIUtil.isNotNullAndNotEmpty(procurementGroup)) {
                value = EnoviaResourceBundle.getRangeI18NString(context, JF_PLMConstants_mxJPO.ATTR_JF_VPMReference_JF_ProcurementGroup, procurementGroup, strLanguage);
            }
            res.add(value);
        }
        return res;
    }


    /**
     * @param context
     * @param args
     * @return java.lang.Boolean
     * @throws
     * @description 获取零件清单中需要显示的command table
     * @author CHENYAN
     * @date 2025/8/18 15:22
     */
    public Boolean getPartsCmdAccess(Context context, String[] args) throws Exception {
        LOGGER.info("getPartsCmdAccess start");
        Boolean isAccess = Boolean.FALSE;
        Map paramMap = JPO.unpackArgs(args);
        String strObjectId = (String) paramMap.get(STRING_OBJECTID);
        Map seetingMap = (Map) paramMap.get("SETTINGS");
        DomainObject partlistBO = DomainObject.newInstance(context, strObjectId);
        String strProjectId = partlistBO.getInfo(context, "to[JFProject2PartList].from.id");
        partlistBO.setId(strProjectId);
        LOGGER.info("strProjectId:{}",strProjectId);
        String strProjectRole = (String) seetingMap.get("Project Role");
        String strLoginUser = context.getUser();
        LOGGER.info("strProjectRole:{}strLoginUser:{}",strProjectRole,strLoginUser);
        //
        StringBuilder relWhereBuild = new StringBuilder();
        relWhereBuild.append("attribute[Project Role]==");
        relWhereBuild.append("'");
        relWhereBuild.append(strProjectRole);
        relWhereBuild.append("'");
        StringBuilder busWhereBuild = new StringBuilder();
        busWhereBuild.append("owner=='");
        busWhereBuild.append(strLoginUser);
        busWhereBuild.append("'");
        LOGGER.info("busWhereBuild:{} relWhereBuild:{}",busWhereBuild,relWhereBuild);
        ContextUtil.pushContext(context);
        String mql = "print bus  "+strProjectId+" select from[Member|.to.name=='"+strLoginUser+"' && attribute[Project Role]=='"+strProjectRole+"'].to.name dump @";
        LOGGER.info("mql:{}",mql);
        String resultMessage = MqlUtil.mqlCommand(context,false,mql,true);
        LOGGER.info("resultMessage:{}",resultMessage);
        DomainObject projectObj = DomainObject.newInstance(context,strProjectId);
        MapList mapList = projectObj.getRelatedObjects(
                context,
                DomainRelationship.RELATIONSHIP_MEMBER,
                DomainConstants.TYPE_PERSON,
                JF_Util_mxJPO.basicBolistSel(),
                JF_Util_mxJPO.basicRellistSel(),
                false,
                true,
                (short) 1, // recursion level
                busWhereBuild.toString(), //object where clause
                relWhereBuild.toString(), //relationship where clause
                1
        );
        LOGGER.info("mapList size:{} mapList:{}",mapList.size(),mapList);
        isAccess = mapList.size() > 0 || UIUtil.isNotNullAndNotEmpty(resultMessage);
        ContextUtil.popContext(context);
        LOGGER.info("getPartsCmdAccess end");
        return isAccess;
    }

    /**
     * @param context
     * @param args
     * @return java.lang.Boolean
     * @throws
     * @description 获取零件清单中关联文件和移除文件权限
     * @author CHENYAN
     * @date 2025/8/21 11:42
     */
    public Boolean getPartsDocumentCmdAccess(Context context, String[] args) throws Exception {
        LOGGER.info("getPartsDocumentCmdAccess");
        Boolean isAccess = Boolean.FALSE;
        Map paramMap = JPO.unpackArgs(args);
        String strObjectId = (String) paramMap.get(STRING_OBJECTID);
        DomainObject partlistBO = DomainObject.newInstance(context, strObjectId);
        String current = partlistBO.getInfo(context, SELECT_CURRENT);
        String strProjectId = partlistBO.getInfo(context, "to[JFProject2PartList].from.id");
        partlistBO.setId(strProjectId);
        String strLoginUser = context.getUser();
        DomainObject project = DomainObject.newInstance(context, strProjectId);
        MapList maps = project.getRelatedObjects(context, DomainConstants.RELATIONSHIP_MEMBER, // relationship pattern
                DomainConstants.TYPE_PERSON,                                    // object pattern
                JF_Util_mxJPO.basicBolistSel(),                            // object selects
                JF_Util_mxJPO.basicRellistSel(), // relationship selects
                false,                                        // to direction
                true,                                        // from direction
                (short) 1,                                    // recursion level
                JF_PublicMethodClass_mxJPO.buildStringInStrings("name=='", strLoginUser, "'"),                // object where clause
                JF_PublicMethodClass_mxJPO.buildStringInStrings("attribute[Project Role].value=='", JF_PLMConstants_mxJPO.ATTR_PROJECTROLE_RANGE_Chairmanager, "'", " || attribute[Project Role].value=='", JF_PLMConstants_mxJPO.ATTR_PROJECTROLE_RANGE_Projectmanager, "'"),
                (short) 0);
//        isAccess = maps.size() > 0;
        //1 角色有权限 2 状态对 3 审核完成就没有权限
        //PM 工作中 审核中(并且没有审批完成)
        //整椅经理 审核中(并且没有审核完成)
//        LOGGER.info("current:{} maps:{}",current,maps);
        if(strLoginUser.equalsIgnoreCase(JF_Util_mxJPO.getProjectManager(context,new String[]{strProjectId}))&&current.equalsIgnoreCase("InWork")){
            isAccess=isAccess = Boolean.TRUE;
        }else if(current.equalsIgnoreCase("Review")){
            //审核状态
            boolean flag = isProcessUnderApproval(context,new String[]{strObjectId});
            isAccess = flag &&maps.size() > 0;;
        }

        return isAccess;
    }

    /**
     * @param context
     * @param args
     * @return java.util.Map
     * @throws
     * @description 检查和连接partlist和零件关系和文档
     * @author CHENYAN
     * @date 2025/8/21 15:03
     */
    public Map checkAndAddDocByRelId(Context context, String[] args) throws Exception {
        LOGGER.info("------------------------------- checkAndAddDocByRelId begin -----------------------------------------");
        Map res = new HashMap<>();
        Map paramMap = JPO.unpackArgs(args);
        boolean flag = false;
        Boolean isAlert = Boolean.FALSE;
        StringList docIdList = (StringList) paramMap.get("docIdList");
        String strRelIds = (String) paramMap.get("relIds");
        String strPartListId = (String) paramMap.get("objectId");
        LOGGER.info("paramMap:{}", paramMap);
        MapList memberList = getPartListProjectRoleByPartListId(context, strPartListId);
        Set projectRoleSet = (Set) memberList.stream().map(m -> {
            Map infoMap = (Map) m;
            return infoMap.get(SELECT_ATTR_PROJECT_ROLE);
        }).collect(Collectors.toSet());
        StringBuilder sbSelectWhere = new StringBuilder();
        //过滤条件
        String strSelectWhere = "";
        //登录人partlist项目角色
        String strProjectRole = "";
        //提示的part信息集合
        StringList alertPartList = new StringList();
        //构造过滤条件
        if (projectRoleSet.size() > 0) {
            sbSelectWhere.append("|");
            //只需要一个
            for (Object oProjectRole : projectRoleSet) {
                strProjectRole = (String) oProjectRole;
                strProjectRole = strProjectRole.trim();
                sbSelectWhere.append("attribute[Project Role]=='");
                sbSelectWhere.append(strProjectRole);
                sbSelectWhere.append("'");
                break;
            }

        }
        strSelectWhere = sbSelectWhere.toString();
        LOGGER.info("strSelectWhere:{}", strSelectWhere);
        try {
            ContextUtil.startTransaction(context, true);
            ContextUtil.pushContext(context);
            flag = true;
            if (UIUtil.isNotNullAndNotEmpty(strRelIds)) {
                String[] relArr = strRelIds.split(",");
                //查询关联文档
                String strPrintTmpMql = "print connection {0} select tomid[JFDocument2VPM{1}].from.id dump @@";
                StringList addConnectionMql = new StringList();
                //遍历选择零件关系
                for (int i = 0; i < relArr.length; i++) {
                    String strRelId = relArr[i];
                    String strPrintMql = MessageFormat.format(strPrintTmpMql, strRelId, strSelectWhere);
                    LOGGER.info("strPrintMql:{}", strPrintMql);
                    String strSelectPrdName = "print connection " + strRelId + " select to.name dump ";
                    String strPrdName = MqlUtil.mqlCommand(context, Boolean.FALSE, strSelectPrdName, Boolean.TRUE);
                    strPrdName = strPrdName.trim();
                    LOGGER.info("strPrdName:{}", strPrdName);
                    String strSelectRes = MqlUtil.mqlCommand(context, Boolean.FALSE, strPrintMql, Boolean.TRUE);
                    LOGGER.info("strSelectRes:{}", strSelectRes);

                    //遍历文档
                    for (int i1 = 0; i1 < docIdList.size(); i1++) {
                        String strDocId = docIdList.get(i1);
                        if (strSelectRes.contains(strDocId)) {
                            isAlert = Boolean.TRUE;
                            DomainObject doc = DomainObject.newInstance(context, strDocId);
                            String strDocName = doc.getInfo(context, SELECT_NAME);
                            String strFailMess = EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", context.getLocale(), "emxComponents.Mess.PartList.AddDocFail");
                            strFailMess = MessageFormat.format(strFailMess, strPrdName, strDocName);
                            alertPartList.add(strFailMess);
                        } else {
                            //保存新增关系MQL
                            String strAddMql = JF_PublicMethodClass_mxJPO.buildStringInStrings("add connection ", "JFDocument2VPM", " from ", strDocId, " torel ", relArr[i], " 'Project Role' '", strProjectRole, "'");
                            addConnectionMql.add(strAddMql);
                        }
                    }
                }
                if (!isAlert) {
                    LOGGER.info("addConnectionMql:{}", addConnectionMql);
                    for (int i = 0; i < addConnectionMql.size(); i++) {
                        MqlUtil.mqlCommand(context, Boolean.FALSE, addConnectionMql.get(i), Boolean.FALSE);
                    }
                } else {
                    res.put("mess", alertPartList.join(","));
                }
            }
            ContextUtil.commitTransaction(context);
        } catch (FrameworkException e) {
            e.printStackTrace();
            ContextUtil.abortTransaction(context);
            LOGGER.error(e.getMessage());
            isAlert = Boolean.TRUE;
            res.put("mess", e.getMessage());
        }finally {
            if(flag) {
                ContextUtil.popContext(context);
            }
        }
        res.put("isAlert", isAlert);
        LOGGER.info("------------------------------- checkAndAddDocByRelId end -----------------------------------------");
        return res;
    }

    /**
     * @param context
     * @param args
     * @return java.util.Map
     * @throws
     * @description 移除选择的文档
     * @author CHENYAN
     * @date 2025/8/22 11:13
     */
    public Map removeDocByRelId(Context context, String[] args) throws Exception {
        LOGGER.info("------------------------------- removeDocByRelId begin -----------------------------------------");
        Map res = new HashMap<>();
        Map paramMap = JPO.unpackArgs(args);
        Boolean isAlert = Boolean.FALSE;
        StringList docIdList = (StringList) paramMap.get("docIdList");
//        String strRelId = (String) paramMap.get("relId");
        String strPartListId = (String) paramMap.get("objectId");
        MapList mapList = (MapList) paramMap.get("list");
        LOGGER.info("paramMap:{}", paramMap);
        String strProjectRole = "";
        //获取登录人partlist项目角色
        MapList memberList = getPartListProjectRoleByPartListId(context, strPartListId);
        if (memberList.size() > 0) {
            Map memberMap = (Map) memberList.get(0);
            strProjectRole = (String) memberMap.get(SELECT_ATTR_PROJECT_ROLE);
            strProjectRole = strProjectRole.trim();
        }
        try {
            ContextUtil.pushContext(context);
            ContextUtil.startTransaction(context, true);
            StringBuilder sbSelectMQL = new StringBuilder();
            StringList removeIdList = new StringList();
            //根据doc找到关系id
            StringBuffer sbSelectWhere = new StringBuffer();
            sbSelectWhere.append("|");
            sbSelectWhere.append("attribute[Project Role]=='");
            sbSelectWhere.append(strProjectRole);
            sbSelectWhere.append("'");
            String strSelectWhere = sbSelectWhere.toString();
            LOGGER.info("strSelectWhere:{}", strSelectWhere);
        //查询关联文档
            for(int k=0;k<mapList.size();k++) {
                Map map = (Map) mapList.get(k);
                String strRelId = UIUtil.getValue(map, "relId");
                String strPrintTmpMql = "print connection {0} select tomid[JFDocument2VPM{1}].id dump @@";
                String strPrintMql = MessageFormat.format(strPrintTmpMql, strRelId, strSelectWhere);
                LOGGER.info("strPrintMql:{}", strPrintMql);
                String strSelectRes = MqlUtil.mqlCommand(context, Boolean.FALSE, strPrintMql, Boolean.TRUE);
                LOGGER.info("strSelectRes:{}", strSelectRes);
                if (UIUtil.isNotNullAndNotEmpty(strSelectRes)) {
                    String[] split = strSelectRes.split("@@");
                    for (int i = 0; i < split.length; i++) {
                        String strDocId = split[i].trim();
                        removeIdList.add(strDocId);
                    }
                }
            }
    /*        for (int i = 0; i < docIdList.size(); i++) {
                sbSelectMQL.append("print bus ");
                sbSelectMQL.append(docIdList.get(i));
                sbSelectMQL.append(" select from[JFDocument2VPM|attribute[Project Role]=='");
                sbSelectMQL.append(strProjectRole);
                sbSelectMQL.append("'");
                sbSelectMQL.append("&&torel.id==");
                sbSelectMQL.append(strRelId);
                sbSelectMQL.append("].id dump");
                LOGGER.info("sbSelectMQL:{}",sbSelectMQL);
                String strDocRelId = MqlUtil.mqlCommand(context, Boolean.FALSE, sbSelectMQL.toString(), Boolean.FALSE);
                LOGGER.info("strDocRelId:{}",strDocRelId);
                if (UIUtil.isNotNullAndNotEmpty(strDocRelId)) {
                    removeIdList.add(strDocRelId);
                }
                //清空字符build
                sbSelectMQL.setLength(0);
            }*/
            if (removeIdList.size() > 0) {
                DomainRelationship.disconnect(context, removeIdList.toStringArray());
            }
            ContextUtil.commitTransaction(context);
        } catch (FrameworkException e) {
            ContextUtil.abortTransaction(context);
            LOGGER.error(e.getMessage());
            isAlert = Boolean.TRUE;
            res.put("mess", e.getMessage());
        } finally {
            ContextUtil.popContext(context);
        }
        LOGGER.info("------------------------------- removeDocByRelId end -----------------------------------------");
        res.put("isAlert", isAlert);
        return res;
    }

    /**
     * @param context
     * @param args
     * @return java.util.Map
     * @throws
     * @description 移除文件时检查选择零件当前登录人是否关联过文档
     * @author CHENYAN
     * @date 2025/8/22 10:31
     */
    public Map checkDocHasConnectionByRelId(Context context, String[] args) throws Exception {
        LOGGER.info("------------------------------- checkAndAddDocByRelId begin -----------------------------------------");
        Map res = new HashMap<>();
        Map paramMap = JPO.unpackArgs(args);
        Boolean isAlert = Boolean.FALSE;
        String strRelId = (String) paramMap.get("relId");
        String strPartListId = (String) paramMap.get("objectId");
        LOGGER.info("paramMap:{}", paramMap);

        StringList docIdList = getPartConnectDocByLoginUser(context, strRelId, strPartListId,"");
        if (docIdList.size() == 0) {
            isAlert = Boolean.TRUE;
        }
        if (isAlert) {
            String strFailMess = EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", context.getLocale(), "emxComponents.Mess.PartList.RemoveDocFail");
            res.put("mess", strFailMess);
        }
        res.put("isAlert", isAlert);
        return res;
    }

    /**
     * @param context
     * @param strPartListId
     * @return com.matrixone.apps.domain.util.MapList
     * @throws
     * @description 获取登录人在partlist关联项目的项目角色
     * @author CHENYAN
     * @date 2025/8/21 16:41
     */
    public static MapList getPartListProjectRoleByPartListId(Context context, String strPartListId) throws Exception {
        DomainObject partlistBO = DomainObject.newInstance(context, strPartListId);
        String strProjectId = partlistBO.getInfo(context, "to[JFProject2PartList].from.id");
        partlistBO.setId(strProjectId);
        String strLoginUser = context.getUser();
        StringList relSelectList = JF_Util_mxJPO.basicRellistSel();
        relSelectList.add(SELECT_ATTR_PROJECT_ROLE);
        DomainObject project = DomainObject.newInstance(context, strProjectId);
        MapList maps = project.getRelatedObjects(context, DomainConstants.RELATIONSHIP_MEMBER, // relationship pattern
                DomainConstants.TYPE_PERSON,                                    // object pattern
                JF_Util_mxJPO.basicBolistSel(),                            // object selects
                relSelectList, // relationship selects
                false,                                        // to direction
                true,                                        // from direction
                (short) 1,                                    // recursion level
                JF_PublicMethodClass_mxJPO.buildStringInStrings("name=='", strLoginUser, "'"),                // object where clause
                JF_PublicMethodClass_mxJPO.buildStringInStrings("attribute[Project Role].value=='", JF_PLMConstants_mxJPO.ATTR_PROJECTROLE_RANGE_Chairmanager, "'", " || attribute[Project Role].value=='", JF_PLMConstants_mxJPO.ATTR_PROJECTROLE_RANGE_Projectmanager, "'"),
                (short) 0);
        return maps;
    }

    /**
     * @param context
     * @param strRelId
     * @param strPartListId
     * @return matrix.util.StringList
     * @throws
     * @description 获取零件当前登录人关联文档
     * @author CHENYAN
     * @date 2025/8/22 10:44
     */
    public static StringList getPartConnectDocByLoginUser(Context context, String strRelId, String strPartListId,String strProjectRole) throws Exception {
        StringList docIdList = new StringList();

        //过滤条件
        String strSelectWhere = "";
        //登录人partlist项目角色
        //提示的part信息集合
        StringBuilder sbSelectWhere = new StringBuilder();
        if (UIUtil.isNullOrEmpty(strProjectRole)){
            MapList memberList = getPartListProjectRoleByPartListId(context, strPartListId);
            Set projectRoleSet = (Set) memberList.stream().map(m -> {
                Map infoMap = (Map) m;
                return infoMap.get(SELECT_ATTR_PROJECT_ROLE);
            }).collect(Collectors.toSet());

            //构造过滤条件
            if (projectRoleSet.size() > 0) {
                sbSelectWhere.append("|");
                //只需要一个
                for (Object oProjectRole : projectRoleSet) {
                    strProjectRole = (String) oProjectRole;
                    strProjectRole = strProjectRole.trim();
                    sbSelectWhere.append("attribute[Project Role]=='");
                    sbSelectWhere.append(strProjectRole);
                    sbSelectWhere.append("'");
                    break;
                }
            }
        }else {
            sbSelectWhere.append("|");
            sbSelectWhere.append("attribute[Project Role]=='");
            sbSelectWhere.append(strProjectRole);
            sbSelectWhere.append("'");
        }
        strSelectWhere = sbSelectWhere.toString();
        LOGGER.info("strSelectWhere:{}", strSelectWhere);
        //查询关联文档
        String strPrintTmpMql = "print connection {0} select tomid[JFDocument2VPM{1}].from.id dump @@";
        String strPrintMql = MessageFormat.format(strPrintTmpMql, strRelId, strSelectWhere);
        LOGGER.info("strPrintMql:{}", strPrintMql);
        String strSelectRes = MqlUtil.mqlCommand(context, Boolean.FALSE, strPrintMql, Boolean.TRUE);
        LOGGER.info("strSelectRes:{}", strSelectRes);
        if (UIUtil.isNotNullAndNotEmpty(strSelectRes)) {
            String[] split = strSelectRes.split("@@");
            for (int i = 0; i < split.length; i++) {
                String strDocId = split[i].trim();
                docIdList.add(strDocId);
            }
        }
        return docIdList;
    }

    /**
     * @param context
     * @param args
     * @return com.matrixone.apps.domain.util.MapList
     * @throws
     * @description 表格显示关联文档
     * @author CHENYAN
     * @date 2025/8/22 10:50
     */
    public MapList getPartConnectDocByLoginUser(Context context, String[] args) throws Exception {
        Map parameterMap = JPO.unpackArgs(args);
        LOGGER.info("parameterMap:{}", parameterMap);
        String strObjectId = (String) parameterMap.get(STRING_OBJECTID);
        String strPartRelId = (String) parameterMap.get("partRelId");
        StringList docIdList = getPartConnectDocByLoginUser(context, strPartRelId, strObjectId,"");
        MapList docInfoList = DomainObject.getInfo(context, docIdList.toStringArray(), JF_Util_mxJPO.basicBolistSel());
        return docInfoList;
    }

    /**
    *
    *@description partlist审核各个角色校验
    *@param context
	*@param strPartListId
	*@param strLoginUser
    *@return int
    *@throws
    *@author CHENYAN
    *@date 2025/8/23 19:56
    */
    public static int checkPartListPartRequireByPartListId(Context context, String strPartListId, String strLoginUser) throws Exception {
        //
        LOGGER.info("---------------------------------------- checkPartListPartRequireByPartListId begin ---------------------------------------------------------");
        int iRes = 0 ;
        LOGGER.info("strLoginUser:{}",strLoginUser);
        DomainObject partListBo = DomainObject.newInstance(context, strPartListId);
        Map partlistInfo = partListBo.getInfo(context, StringList.create("to[JFProject2PartList].from.id", SELECT_NAME,SELECT_ATTR_JFPartListType));
        String strProjectId = UIUtil.getValue(partlistInfo, "to[JFProject2PartList].from.id");
        String strPartListName = UIUtil.getValue(partlistInfo, SELECT_NAME);
        String strProjectType = UIUtil.getValue(partlistInfo, SELECT_ATTR_JFPartListType);
        DomainObject project = DomainObject.newInstance(context, strProjectId);
        StringList relSelectList = JF_Util_mxJPO.basicRellistSel();
        relSelectList.add(SELECT_ATTR_PROJECT_ROLE);
        MapList mapList = project.getRelatedObjects(
                context,
                DomainRelationship.RELATIONSHIP_MEMBER,
                DomainConstants.TYPE_PERSON,
                JF_Util_mxJPO.basicBolistSel(),
                relSelectList,
                false,
                true,
                (short) 1, // recursion level
                "name=='" + strLoginUser + "'", //object where clause
                "", //relationship where clause
                0
        );
        LOGGER.info("mapList:{}",mapList);
        StringList typeSelectList = JF_Util_mxJPO.basicBolistSel();
        StringList drawList = new StringList();
        StringList DVPList = new StringList();
        StringList SSOWList = new StringList();
        StringList BPList = new StringList();
        StringList SQDWList = new StringList();
        StringList ProcurementGroupList = new StringList();
        String catiaNotice = "";
        projectRole:
        for (int i1 = 0; i1 < mapList.size(); i1++) {
            Map personMap = (Map) mapList.get(i1);
            String strProjectRole = (String) personMap.get(SELECT_ATTR_PROJECT_ROLE);
            StringList partTypeSelectList = JF_Util_mxJPO.basicBolistSel();
            partTypeSelectList.add(SELECT_ATTR_V_PART_NUMBER);
            partTypeSelectList.add(SELECT_ATTR_JF_VPMReference_JF_ProcurementGroup);
            partTypeSelectList.add(SELECT_ATTR_V_PART_NUMBER);
            //零件文档
//            partTypeSelectList.add("from[Reference Document|to.attribute[JF_DocumentType]==Drawing].attribute[Title]");
//            partTypeSelectList.add("from[Reference Document|to.attribute[JF_DocumentType]==Drawing].revision");
            StringList partRelSelectList = JF_Util_mxJPO.basicRellistSel();
            boolean isLoadXml = false;
            switch (strProjectRole) {
                case JF_PLMConstants_mxJPO.ATTR_PROJECTROLE_RANGE_Chairmanager: {
                    break;
                }
                case JF_PLMConstants_mxJPO.ATTR_PROJECTROLE_RANGE_FinancialBP: {
                    partTypeSelectList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JFDIRECT_BUY);
                    partTypeSelectList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JFPartType);
                    break;
                }
                case JF_PLMConstants_mxJPO.ATTR_PROJECTROLE_RANGE_Projectmanager: {
                    break;
                }
                case JF_PLMConstants_mxJPO.ATTR_PROJECTROLE_RANGE_SQDRepresentative: {
                    partRelSelectList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JFPartRequirementCommitmentDate);
                    break;
                }
                default: {

                }

            }

            //项目的CATIA版本是必填，整椅经理角色
            if(strProjectRole.equalsIgnoreCase(ATTR_PROJECTROLE_RANGE_Chairmanager)){
                if(UIUtil.isNullOrEmpty(project.getAttributeValue(context,"JSOutSourceRev"))){
                    catiaNotice = EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", context.getLocale(), "emxComponents.Mess.PartList.NoCatiaRev");
                    iRes=1;
                }
            }

            MapList partListMapList = getPartListConnectionPartsByCheckRoute(context,strPartListId,strProjectRole,partTypeSelectList,partRelSelectList );
            //获取partlist关联零件
//            MapList partListMapList = partListBo.getRelatedObjects(context,
//                    JF_PLMConstants_mxJPO.rel_JFPartList2VPMReference, // relationship pattern
//                    JF_PLMConstants_mxJPO.TYPE_VPMReference,                                    // object pattern
//                    partTypeSelectList,                            // object selects
//                    partRelSelectList, // relationship selects
//                    false,                                        // to direction
//                    true,                                        // from direction
//                    (short) 1,                                    // recursion level
//                    "",                // object where clause
//                    "",
//                    (short) 0);
            LOGGER.info("partListMapList:{}",partListMapList);

            switch (strProjectRole) {
                case ATTR_PROJECTROLE_RANGE_Chairmanager ,ATTR_PROJECTROLE_RANGE_Projectmanager : {
                        Map groupMap = (Map) partListMapList.stream().collect(Collectors.groupingBy( m ->{
                            Map partInfoMap = (Map) m;
                            return partInfoMap.get(SELECT_ATTR_JF_VPMReference_JF_ProcurementGroup);
                        }));
                        Page pageAttributePopulation = new Page("SignTaskProperties_zh.xml");
                        pageAttributePopulation.open(context);
                        String strProperties = pageAttributePopulation.getContents(context);
                        pageAttributePopulation.close(context);
                        // 使用 ByteArrayInputStream 将字符串转为输入流
                        InputStream inputStream = new ByteArrayInputStream(strProperties.getBytes("UTF-8"));
                        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                        DocumentBuilder builder = factory.newDocumentBuilder();
                        Document document = builder.parse(inputStream);
                        LOGGER.info("document:{}",document);
                    Set groupSet = groupMap.entrySet();
                    for (Object oEntry : groupSet) {
                        Map.Entry entry = (Map.Entry)oEntry;
                        String strKey = (String) entry.getKey();
                        if (UIUtil.isNotNullAndNotEmpty(strKey)){
                            //获取图纸、SSOW、DPV 是否必填信息
                            Map<String, String> requireMap = findProcurementGroupWithXPath(document, "PartListDocCheck", strProjectType, strKey);
                            String strRequireType = "";
                            List parts = (List) entry.getValue();
                            String strDocTypeKey = "";
                            if (Objects.nonNull(requireMap)){
                                if (ATTR_PROJECTROLE_RANGE_Chairmanager.equals(strProjectRole)){
                                    strDocTypeKey = "DVP";
                                }else if (ATTR_PROJECTROLE_RANGE_Projectmanager.equals(strProjectRole)){
                                    strDocTypeKey = "SSOW";
                                }
                                strRequireType  = requireMap.get(strDocTypeKey);

                            }
                            String strDrawRequireType  = requireMap.get("Draw");
                            //图纸必须存在
                            if (ATTR_PROJECTROLE_RANGE_Chairmanager.equals(strProjectRole)) {
                                if ("require".equals(strDrawRequireType)) {
                                    DomainObject part = DomainObject.newInstance(context);
                                    for (int i = 0; i < parts.size(); i++) {
                                        Map partInfo = (Map) parts.get(i);
                                        String strPartId = (String) partInfo.get(SELECT_ID);
                                        part.setId(strPartId);
//                                    Boolean hasDraw = getDrawInfoByPart(context, part, partInfo);
                                        Boolean hasDraw = new JF_DR_mxJPO().getVPMReferenceHasDRW(context, strPartId);
                                        if (!hasDraw) {
                                            String strPartNum = UIUtil.getValue(partInfo, SELECT_ATTR_V_PART_NUMBER);
                                            String revision = UIUtil.getValue(partInfo, SELECT_REVISION);
                                            if (UIUtil.isNullOrEmpty((strPartNum))) {
                                                strPartNum = UIUtil.getValue(partInfo, SELECT_NAME);
                                            }
                                            strPartNum = strPartNum + "_" + revision;
                                            drawList.add(strPartNum);
//                                        String strFailMess = EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", context.getLocale(), "emxComponents.Mess.PartList.PartDrawTNotExist");
//                                        strFailMess = strFailMess.replace("{}",strPartNum);
//                                        emxContextUtilBase_mxJPO.mqlWarning(context, strFailMess);
                                            //退出最外层循环
                                            iRes = 1;
//                                        break projectRole ;
                                        }
                                    }
                                }
                            }
                            //校验SSOW或者DVP
                            if ("require".equals(strRequireType)){
                                for (int i = 0; i < parts.size(); i++) {
                                    Map partInfo = (Map) parts.get(i);
                                    String strPartRelId = (String) partInfo.get(SELECT_RELATIONSHIP_ID);
                                    //零件 SSOW或者DVP附件
                                    StringList SSOWOrDVPDoc = getPartConnectDocByLoginUser(context, strPartRelId, strPartListId, strProjectRole);
                                    if (Objects.isNull(SSOWOrDVPDoc) || SSOWOrDVPDoc.size() == 0){
                                        String strPartNum = UIUtil.getValue(partInfo, SELECT_ATTR_V_PART_NUMBER);
                                        String revision = UIUtil.getValue(partInfo, SELECT_REVISION);
                                        if (UIUtil.isNullOrEmpty((strPartNum) )){
                                            strPartNum = UIUtil.getValue(partInfo, SELECT_NAME);
                                        }
                                        strPartNum=strPartNum+"_"+revision;
                                        if("DVP".equalsIgnoreCase(strDocTypeKey)) {
                                            DVPList.add(strPartNum);
                                        }
                                        if("SSOW".equalsIgnoreCase(strDocTypeKey)) {
                                            SSOWList.add(strPartNum);
                                        }
//                                        String strFailMess = EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", context.getLocale(), "emxComponents.Mess.PartList.PartDocTNotExist");
//                                        strFailMess = strFailMess.replace("{}",strPartNum).replace("{1}",strDocTypeKey);
//                                        emxContextUtilBase_mxJPO.mqlWarning(context, strFailMess);
                                        iRes = 1;
                                        //退出最外层循环
//                                        break projectRole ;
                                    }
                                }
                            }
                        }else {
                            String strFailMess = EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", context.getLocale(), "emxComponents.Mess.PartList.PartJF_ProcurementGroupNotWrite");
                            emxContextUtilBase_mxJPO.mqlWarning(context, strFailMess);
//                            ProcurementGroupList.add();
                            iRes = 1;
                        }

                    }
                    break;
                }
                case JF_PLMConstants_mxJPO.ATTR_PROJECTROLE_RANGE_FinancialBP: {
                    for (int i = 0; i < partListMapList.size(); i++) {
                        Map partInfo = (Map) partListMapList.get(i);
                        String strDirectBuy = UIUtil.getValue(partInfo, SELECT_ATTR_JFDIRECT_BUY);
                        //校验direct
                        if (UIUtil.isNullOrEmpty(strDirectBuy) ){
                            String strPartNum = UIUtil.getValue(partInfo, SELECT_ATTR_V_PART_NUMBER);
                            String revision = UIUtil.getValue(partInfo, SELECT_REVISION);
                            if (UIUtil.isNullOrEmpty((strPartNum) )){
                                strPartNum = UIUtil.getValue(partInfo, SELECT_NAME);
                            }
                            strPartNum=strPartNum+"_"+revision;
                            BPList.add(strPartNum);
//                            String strDirectBuyNls = EnoviaResourceBundle.getAttributeI18NString(context, ATTR_JFDIRECT_BUY, context.getLocale().toString());
//                            String strPartTypeNls = EnoviaResourceBundle.getAttributeI18NString(context, SELECT_ATTR_JFPartType, context.getLocale().toString());
//                            String strFailMess = EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", context.getLocale(), "emxComponents.Mess.PartList.FinancialBPRequireAttrNotWrite");
//                            strFailMess = strFailMess.replace("{}",strPartNum).replace("{1}",strDirectBuyNls + "," + strPartTypeNls);
//                            emxContextUtilBase_mxJPO.mqlWarning(context, strFailMess);
                            iRes = 1;
                            //退出最外层循环
//                            break projectRole ;
                        }
                    }
                    break;
                }
                case JF_PLMConstants_mxJPO.ATTR_PROJECTROLE_RANGE_SQDRepresentative: {
                    for (int i = 0; i < partListMapList.size(); i++) {
                        Map partInfo = (Map) partListMapList.get(i);
                        String strCommitData = UIUtil.getValue(partInfo, SELECT_ATTR_JFPartRequirementCommitmentDate);
                        //校验 JFPartRequirementCommitmentDate
                        if (UIUtil.isNullOrEmpty(strCommitData)){
                            String strPartNum = UIUtil.getValue(partInfo, SELECT_ATTR_V_PART_NUMBER);
                            String revision = UIUtil.getValue(partInfo, SELECT_REVISION);
                            if (UIUtil.isNullOrEmpty((strPartNum) )){
                                strPartNum = UIUtil.getValue(partInfo, SELECT_NAME);
                            }
                            strPartNum=strPartNum+"_"+revision;
                            SQDWList.add(strPartNum);
//                            String strAttrNameNls = EnoviaResourceBundle.getAttributeI18NString(context, ATTR_JFPartRequirementCommitmentDate, context.getLocale().toString());
//                            String strFailMess = EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", context.getLocale(), "emxComponents.Mess.PartList.SQDRequireAttrNotWrite");
//                            strFailMess = strFailMess.replace("{}",strPartNum).replace("{1}",strAttrNameNls);
//                            emxContextUtilBase_mxJPO.mqlWarning(context, strFailMess);
                            iRes = 1;
                            //退出最外层循环
//                            break projectRole ;
                        }
                    }
                    break;
                }
                default: {
                }
            }
//            if (isAlert) {
//                emxContextUtilBase_mxJPO.mqlWarning(context, strMess);
//                return 1;
//            }
        }
        StringBuffer noticeMessage = new StringBuffer();
        String drawMessage = EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", context.getLocale(), "emxComponents.Mess.PartList.PartDrawTNotExist");
        String SSOWMessage = EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", context.getLocale(), "emxComponents.Mess.PartList.PartDocTNotExist");
        String dvpMessage = EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", context.getLocale(), "emxComponents.Mess.PartList.ParDVPtDocTNotExist");
        String BPMessage = EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", context.getLocale(), "emxComponents.Mess.PartList.BPNotExist");
        String SQDMessage = EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", context.getLocale(), "emxComponents.Mess.PartList.SQDRequireAttrNotWrite");
        if(drawList.size()>0){
            noticeMessage.append(drawMessage);
            noticeMessage.append("\n");
            noticeMessage.append(drawList.toString());
        }
        if(DVPList.size()>0){
            if(!noticeMessage.isEmpty()){
                noticeMessage.append("\n");
            }
            noticeMessage.append(dvpMessage);
            noticeMessage.append("\n");
            noticeMessage.append(DVPList.toString());
        }
        if(SSOWList.size()>0){
            if(!noticeMessage.isEmpty()){
                noticeMessage.append("\n");
            }
            noticeMessage.append(SSOWMessage);
            noticeMessage.append("\n");
            noticeMessage.append(SSOWList.toString());
        }
        if(BPList.size()>0){
            if(!noticeMessage.isEmpty()){
                noticeMessage.append("\n");
            }
            noticeMessage.append(BPMessage);
            noticeMessage.append("\n");
            noticeMessage.append(BPList.toString());
        }
        if(SQDWList.size()>0){
            if(!noticeMessage.isEmpty()){
                noticeMessage.append("\n");
            }
            noticeMessage.append(SQDMessage);
            noticeMessage.append("\n");
            noticeMessage.append(SQDWList.toString());
        }
        if(UIUtil.isNotNullAndNotEmpty(catiaNotice)){
            if(!noticeMessage.isEmpty()){
                noticeMessage.append("\n");
            }
            noticeMessage.append(catiaNotice);
        }

        LOGGER.info("iRes:{}",iRes);
        if(UIUtil.isNotNullAndNotEmpty(noticeMessage.toString())) {
            emxContextUtilBase_mxJPO.mqlWarning(context, noticeMessage.toString());
        }
        LOGGER.info("---------------------------------------- checkPartListPartRequireByPartListId end ---------------------------------------------------------");

        return iRes;
    }

    public static void test(Context context, String[]args) throws Exception {

    }

    /**
    *
    *@description 使用Xpath过滤出指定的
    *@param doc
	*@param configId
	*@param partListTypeId
	*@param procurementGroupId
    *@return java.util.Map<java.lang.String,java.lang.String>
    *@throws
    *@author CHENYAN
    *@date 2025/8/22 16:42
    */
    public static Map<String, String> findProcurementGroupWithXPath(Document doc, String configId, String partListTypeId, String procurementGroupId) throws XPathExpressionException {

        // 创建 XPath 工厂和解析器
        XPathFactory xPathFactory = XPathFactory.newInstance();
        XPath xpath = xPathFactory.newXPath();
        // 构建 XPath 表达式
        String expression = String.format("/configurations/configuration[@id='%s']/partListType[@id='%s']/procurementGroup[@id='%s']", configId, partListTypeId, procurementGroupId);
        LOGGER.info("expression:{}",expression);
        // 执行查询
        NodeList nodes = (NodeList) xpath.compile(expression).evaluate(doc, XPathConstants.NODESET);
        // 检查是否找到节点
        if (nodes.getLength() == 0) {
            return null;
        }
        Element pg = (Element) nodes.item(0); // 取第一个匹配项
        Map<String, String> attrMap = new HashMap<>();
        attrMap.put("Draw", pg.getAttribute("Draw"));
        attrMap.put("DVP", pg.getAttribute("DVP"));
        attrMap.put("SSOW", pg.getAttribute("SSOW"));
        return attrMap;
    }

    /**
    *
    *@description 判断零件是否关联图纸
    *@param context
	*@param part
	*@param partMap
    *@return java.lang.Boolean
    *@throws
    *@author CHENYAN
    *@date 2025/8/23 19:14
    */
    public static Boolean getDrawInfoByPart(Context context ,DomainObject part,Map partMap) throws Exception{
        Boolean hasDoc = Boolean.FALSE ;
        Map res = new HashMap<String,Set<String>>();
        StringList drawNameSet = new StringList();
        StringList drawRevisionSet = new StringList();
        if (partMap.containsKey("from[Reference Document].to.attribute[Title]")){
            Object oDocName = partMap.get("from[Reference Document].to.attribute[Title]");
            if (oDocName instanceof String){
                String strDocName = (String) oDocName;
                if (UIUtil.isNotNullAndNotEmpty(strDocName)){
                    hasDoc = Boolean.TRUE ;
                }
            }else if (oDocName instanceof StringList){
                hasDoc = Boolean.TRUE ;
            }
        }
        LOGGER.info("partMap:{}",partMap);
        if (!hasDoc){
            StringList typeSelectList = JF_Util_mxJPO.basicBolistSel();
            typeSelectList.add("attribute[PLMEntity.V_Name]");
            MapList maps = part.getRelatedObjects(context, "XCADBaseDependency" , // relationship pattern
                    "Drawing",                                    // object pattern
                    typeSelectList,                            // object selects
                    JF_Util_mxJPO.basicRellistSel(), // relationship selects
                    true,                                        // to direction
                    false,                                        // from direction
                    (short) 1,                                    // recursion level
                    "",                // object where clause
                    "",
                    (short) 0);

            hasDoc = maps.size() > 0 ;
        }
        return hasDoc ;
    }


    /**
    * 重新同步的权限  - 项目经理
     *  caa转换成功 - 未同步
    * @param context
	* @param args
    * @author LIUJR
    * @throws
    * @return java.lang.Boolean
    * @date 2025/9/1 15:47
    * @description
    */
    public Boolean getResynchronizePartListAccess(Context context, String[] args) throws Exception {
        LOGGER.info("getResynchronizePartListAccess........start............");
        Boolean isAccess = Boolean.FALSE;
        Map paramsMap = (Map) JPO.unpackArgs(args);
        //partList
        String strObjectId = (String) paramsMap.get(STRING_OBJECTID);
        DomainObject partListObject = DomainObject.newInstance(context, strObjectId);
        //找到项目
        String projectId = partListObject.getInfo(context, "to[JFProject2PartList].from.id");
        StringList typeSelectList = JF_Util_mxJPO.basicBolistSel();
        typeSelectList.add("physicalid");
        MapList partListMapList = partListObject.getRelatedObjects(context,
                JF_PLMConstants_mxJPO.rel_JFPartList2VPMReference, // relationship pattern
                JF_PLMConstants_mxJPO.TYPE_VPMReference,                                    // object pattern
                typeSelectList,                            // object selects
                JF_Util_mxJPO.basicRellistSel(), // relationship selects
                false,                                        // to direction
                true,                                        // from direction
                (short) 1,                                    // recursion level
                "",                // object where clause
                "attribute[JSdataProcessProgress]==CAAProcessingComplete && attribute[JF_SyncStatus]==unsynchronized",
                (short) 0);
        LOGGER.info("partListMapList:{}", partListMapList.toString());
        if (partListMapList.isEmpty()) {
            isAccess =  false;
        } else {
            //找到零件是否同步完成 是否又同步失败的零件
            String strProjectManager = JF_Util_mxJPO.getProjectManager(context, new String[]{projectId});
            if (UIUtil.isNotNullAndNotEmpty(strProjectManager)) {
                String strLoginUser = context.getUser();
                isAccess = strLoginUser.equals(strProjectManager);
            }
        }
        LOGGER.info("isAccess:{}", isAccess);
        return isAccess;
    }


    /**
    * 重新提交caa转换权限 - admin
    * @param context
	* @param args
    * @author LIUJR
    * @throws
    * @return java.lang.Boolean
    * @date 2025/9/1 15:47
    * @description
    */
    public Boolean getRestartTransferCaaAccess(Context context, String[] args) throws Exception {
        LOGGER.info("getRestartTransferCaaAccess........start............");
        Boolean isAccess = Boolean.FALSE;
        Map paramsMap = (Map) JPO.unpackArgs(args);
        //partList
        String strObjectId = (String) paramsMap.get(STRING_OBJECTID);
        DomainObject partListObject = DomainObject.newInstance(context, strObjectId);
        //找到项目
        StringList typeSelectList = JF_Util_mxJPO.basicBolistSel();
        typeSelectList.add("physicalid");
        MapList partListMapList = partListObject.getRelatedObjects(context,
                JF_PLMConstants_mxJPO.rel_JFPartList2VPMReference, // relationship pattern
                JF_PLMConstants_mxJPO.TYPE_VPMReference,                                    // object pattern
                typeSelectList,                            // object selects
                JF_Util_mxJPO.basicRellistSel(), // relationship selects
                false,                                        // to direction
                true,                                        // from direction
                (short) 1,                                    // recursion level
                "",                // object where clause
                "attribute[JSdataProcessProgress] == CAAProcessingFailed",
                (short) 0);
        LOGGER.info("partListMapList:{}", partListMapList.toString());
        if (partListMapList.isEmpty()) {
            isAccess =  false;
        }
        //找到零件是否同步完成 是否又同步失败的零件
        LOGGER.info("context.getUser():{}", context.getUser());
        if (USER_Admin_Platform.equalsIgnoreCase(context.getUser())) {
            isAccess =  true;
        }
        LOGGER.info("isAccess:{}", isAccess);
        return isAccess;
    }


    /**
    * 重新生成json文件
    * @param context
	* @param args
    * @author LIUJR
    * @throws
    * @return void
    * @date 2025/8/28 17:45
    * @description
    */
    public void restartTransferPartListAgain(Context context, String[] args) throws Exception {
        try {
            Map<String, Object> paramMap =  (Map) JPO.unpackArgs(args);
            MapList mapList = (MapList) paramMap.get("data");   //选择零件的数据
            if (mapList.isEmpty()) {
                return;
            }
            String objectId = UIUtil.getValue(paramMap, "objectId");
            DomainObject domainObject = DomainObject.newInstance(context);
            DomainObject partListObject = DomainObject.newInstance(context);
            partListObject.setId(objectId);
            partListObject.setAttributeValue(context, "JSdataProcessProgress", "EnoviaProcess");
            String partListName = partListObject.getInfo(context, SELECT_NAME);
            //获取数模版本
            String rev = partListObject.getInfo(context, "to[JFProject2PartList].from.attribute[JSOutSourceRev]");
            String strBasicUrl = JF_PublicMethodClass_mxJPO.getBasicUrl(context, new String[]{"dataOutSource.share.path"});
            DomainRelationship domainRelationship;
            StringList typeSelectList = JF_Util_mxJPO.basicBolistSel();
            typeSelectList.add("physicalid");
            for (int i = 0; i < mapList.size(); i++) {
                Map map = (Map) mapList.get(i);
                String strPartId = (String) map.get(SELECT_ID);
                String strPartRid = (String) map.get(DomainRelationship.SELECT_ID);
                domainObject.setId(strPartId);
                domainRelationship = DomainRelationship.newInstance(context, strPartRid);
                domainRelationship.setAttributeValue(context, "JSdataProcessProgress", "EnoviaProcess");
                String physicalid = domainObject.getInfo(context, "physicalid");
                //零件的物理id  作为文件名称
                String strName = partListName + "_" + physicalid;
                //一级件下的结构
                MapList partMapList = domainObject.getRelatedObjects(context,
                        JF_PLMConstants_mxJPO.REL_Instance, // relationship pattern
                        JF_PLMConstants_mxJPO.TYPE_VPMReference,                                    // object pattern
                        typeSelectList,                            // object selects
                        JF_Util_mxJPO.basicRellistSel(), // relationship selects
                        false,                                        // to direction
                        true,                                        // from direction
                        (short) 0,                                    // recursion level
                        "",                // object where clause
                        "",
                        (short) 0
                );
                //将零件与结构一起组合集合
                partMapList.add(domainObject.getInfo(context, typeSelectList));
                //分类型
                StringList vpmReferenceV6List = new StringList();
                Iterator iterator = partMapList.iterator();
                //所有的数模和图纸
                while (iterator.hasNext()) {
                    Map map2 = (Map) iterator.next();
                    String strOId = (String) map2.get(DomainConstants.SELECT_ID);
                    //挑出v5 情况
                    vpmReferenceV6List.add(strOId);
                    //找到2D 3D文件
                }
                //开始下载文件和数模
                //开始操作数模 生成json文件
                if (vpmReferenceV6List.size() > 0) {
                    LOGGER.info("vpmReferenceV6List:{}", vpmReferenceV6List);
                    //转换版本
                    Map<String, String> paramsMap = new HashMap<>();
                    paramsMap.put("strOutSourceRev", rev);
                    paramsMap.put("strDirName", strName);
                    paramsMap.put("strBasicUrl", strBasicUrl);
                    paramsMap.put("vpmReferenceV6Ids", vpmReferenceV6List.join(","));
                    paramsMap.put("drawingV5Ids", "");
                    paramsMap.put("drawingV6Ids", "");
                    Map map1 = JF_DataOutSource_mxJPO.constructJSONFileForDigifax(context, JPO.packArgs(paramsMap));
                    //是否包含数模和图纸
                }
                //需要caa处理  将状态改为待caa处理
                domainRelationship.setAttributeValue(context, "JSdataProcessProgress", "PendingCAAProcess");
                domainRelationship.setAttributeValue(context, "JSReason", "");
            }
            partListObject.setAttributeValue(context, "JSdataProcessProgress", "PendingCAAProcess");
        }catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @Author Liuxg
     * @Description partlist属性JSdataProcessProgress变化后触发
     * @Date 2025/9/1 16:30
     * @Param [context, args]
     * @return void
     **/
    public void triggerJSdataProcessProgressModAction(Context context,String[]args)throws Exception{

        try {

            String OBJECTID=args[0];
            String ATTRNAME=args[1];
            String ATTRVALUE=args[2];
            String NEWATTRVALUE=args[3];
            String OLDATTRVALUE=args[4];


            LOGGER.info("JSdataProcessProgressModAction---->");
            LOGGER.info("OBJECTID---->"+OBJECTID);
            LOGGER.info("ATTRNAME---->"+ATTRNAME);
            LOGGER.info("ATTRVALUE---->"+ATTRVALUE);
            LOGGER.info("NEWATTRVALUE---->"+NEWATTRVALUE);
            LOGGER.info("OLDATTRVALUE---->"+OLDATTRVALUE);

            //此处OBJECTID可能为空？

            if("CAAProcessingComplete".equalsIgnoreCase(NEWATTRVALUE)&&"JSdataProcessProgress".equalsIgnoreCase(ATTRNAME)){
                DomainObject obj=DomainObject.newInstance(context,OBJECTID);
                String type=obj.getTypeName(context);
                if("JFPartList".equals(type)){
                    JPO.invoke(context,"JF_ProjectSpace",null,"getPartListinfoSendSRM", new String[]{OBJECTID});
                }
            }

        }catch (Exception e){
            e.printStackTrace();
        }
    }


    /*
     * @description:获取当前项目下所有的零件
     * @author: caipan
     * @date: 2025/9/11 14:24:15
     * @param: * @param[1] context
     * @param[2] args
     * @return:
     **/
    public StringList getProjectAllPart(Context context,String[] args) throws Exception{
        String projectId = args[0];
        String where = SELECT_ATTR_JF_BelongPart +" ==Y";
        DomainObject projectObj = DomainObject.newInstance(context,projectId);
            MapList list = projectObj.getRelatedObjects(context,
                JF_PLMConstants_mxJPO.rel_JFProject2RootPart, //pattern to match relationships
                JF_PLMConstants_mxJPO.TYPE_VPMReference, //pattern to match types
                JF_Util_mxJPO.basicBolistSel(), //the eMatrix StringList object that holds the list of select statement pertaining to Business Obejcts.
                    JF_Util_mxJPO.basicRellistSel(), //the eMatrix StringList object that holds the list of select statement pertaining to Relationships.
                false, //get To relationships
                true, //get From relationships
                (short) 1, //the number of levels to expand, 0 equals expand all.
                EMPTY_STRING, //where clause to apply to objects, can be empty ""
                    where, //where clause to apply to relationship, can be empty ""
                (short) 0); //limit
       return  JF_Util_mxJPO.mapList2StringList(list, SELECT_ID);
    }


    /**
     * @Author Liuxg
     * @Description
     * @Date 2025/9/17 11:10
     * @Param [context, args]
     * @return void
    **/
    public void modifyPartListPartSynchronizedInvoke(Context context,String []args)throws Exception{
        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        String pratlistid = (String) programMap.get("id");
        StringList pratlistidlist = (StringList) programMap.get("idlist");
        modifyPartListPartSynchronized(context,pratlistid,pratlistidlist);
    }

    /*
     * @description:控制采购编辑权限，如果采购分组有值就不允许编辑
     * @author: caipan
     * @date: 2025/9/23 18:01:32
     * @param: * @param[1] context
     * @param[2] args
     * @return:
     **/
    public StringList procurementGroupEditAccess(Context context,String[] args) throws Exception{
        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        Map requestMap = (Map) programMap.get(STRING_REQUESTMAP);
        String strLoginUser = context.getUser();
        MapList objectList = (MapList) programMap.get("objectList");
        String strObjectId = (String) requestMap.get(STRING_OBJECTID);
        StringList editAccessList = new StringList(objectList.size());
        Map columnMap = (Map)programMap.get("columnMap");
        String columnName = UIUtil.getValue(columnMap, SELECT_NAME);
        LOGGER.info("columnName:{}",columnName);
        DomainObject obj = DomainObject.newInstance(context);
        for (int i = 0; i < objectList.size(); i++) {
            Map objectMap = (Map) objectList.get(i);
//            LOGGER.info("objectMap:{}", objectMap);
            String isEdit = (String) objectMap.get("isEdit");
            String JF_ProcurementGroup = (String) objectMap.get(SELECT_ATTR_JF_VPMReference_JF_ProcurementGroup);
                if(columnName.equals(ATTR_JF_VPMReference_JF_ProcurementGroup)){
                    if(UIUtil.isNullOrEmpty(JF_ProcurementGroup)&&isEdit.equalsIgnoreCase("true")) {
                        editAccessList.add("true");
                    }else{
                        editAccessList.add("false");
                    }
                }else {
                    editAccessList.add(isEdit);
                }
        }
//        LOGGER.info("editAccessList:{}", editAccessList);
        return editAccessList;
    }
/*
 * @description:文档上传、添加现有、移除的权限,目前只对快照做了权限控制，后续可以往后面增加权限
 * @author: caipan
 * @date:  14:58:56
 * @param: * @param[1] context
 * @param[2] args
 * @return:
 **/
 public boolean isUploadFile(Context context,String[] args) throws Exception{
        Map paramMap = JPO.unpackArgs(args);
        String strObjectId = (String) paramMap.get(STRING_OBJECTID);
        DomainObject obj = DomainObject.newInstance(context,strObjectId);
        Map attrMap = obj.getInfo(context,JF_Util_mxJPO.basicBolistSel());
        String type =UIUtil.getValue(attrMap, SELECT_TYPE);
        String current =UIUtil.getValue(attrMap, SELECT_CURRENT);
        String owner =UIUtil.getValue(attrMap, SELECT_OWNER);
        String loginUser = context.getUser();
        if("JFSnapshot".equalsIgnoreCase(type)){
            if(current.equalsIgnoreCase("DRAFT")&&loginUser.equalsIgnoreCase(owner)){
                return true;
            }else{
                return  false;
            }
        }
        return true;
    }

    /**
    * partList 清单导入导出权限
    * @param context
	* @param args
    * @author LIUJR
    * @throws
    * @return java.lang.Boolean
    * @date 2025/10/27 14:04
    * @description
    */
    public Boolean importAndExportPartListAccess(Context context, String[] args) {
        /*
        * 项目经理：  登录用户为项目中的项目经理角色，并且partList是工作中
        * 商务：登录用户为项目中的商务经理角色，并且partList在审核中，并且清单中存在db件
        * sqd: 登录用户为项目中的sqd角色，并且partList在审核中
        *
        * */
        Boolean access = Boolean.TRUE;
        try {
            Map map = (Map) JPO.unpackArgs(args);
            LOGGER.info("map:{}", map.toString());
            DomainObject domainObject = DomainObject.newInstance(context);
            //partListId
            String parentOID = (String) map.get("parentOID");
            String roleFlag = (String) map.get("roleFlag");
            String user = context.getUser();
            domainObject.setId(parentOID);
            String current = domainObject.getInfo(context, SELECT_CURRENT);
            String owner = domainObject.getInfo(context, SELECT_OWNER);
            boolean flag = isProcessUnderApproval(context,new String[]{parentOID});
            switch (roleFlag) {
                case "PM": {
                    if ("InWork".equalsIgnoreCase(current) && user.equals(owner)) {
                           access = Boolean.TRUE;
                    } else {
                        access = Boolean.FALSE;
                    }
                    break;
                }
                case "Business": {
                    if ("Review".equalsIgnoreCase(current)) {
                        MapList partListMapList = domainObject.getRelatedObjects(context,
                                JF_PLMConstants_mxJPO.rel_JFPartList2VPMReference, // relationship pattern
                                JF_PLMConstants_mxJPO.TYPE_VPMReference,                                    // object pattern
                                StringList.create(SELECT_ATTR_JF_VPMReference_JF_ProcurementGroup),                            // object selects
                                JF_Util_mxJPO.basicRellistSel(), // relationship selects
                                false,                                        // to direction
                                true,                                        // from direction
                                (short) 1,                                    // recursion level
                                "attribute[JF_VPMReference.JF_DirectBuy]=='consignment' || attribute[JF_VPMReference.JF_DirectBuy]=='direct-buy'",                // object where clause
                                "",
                                (short) 0);
                        if (partListMapList.isEmpty()) {
                            access = Boolean.FALSE;
                        } else {
                            access = Boolean.TRUE&&flag;
                        }
                    } else {
                        access = Boolean.FALSE;
                    }
                    break;
                }
                case "SQD": {
                    if ("Review".equalsIgnoreCase(current)) {
                        access = Boolean.TRUE&&flag;
                    } else {
                        access = Boolean.FALSE;
                    }
                    break;
                }
                default: {
                    access = Boolean.FALSE;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return access;
    }

    /*
     * @description:项目经理或者财务有权限编辑
     * @author: caipan
     * @date: 2025/10/24 14:38:10
     * @param: * @param[1] null
     * @return:
     *
    public StringList procurementTypeEditAccess(Context context,String[] args) throws Exception{
        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        Map requestMap = (Map) programMap.get(STRING_REQUESTMAP);
        String strLoginUser = context.getUser();
        MapList objectList = (MapList) programMap.get("objectList");
        String strObjectId = (String) requestMap.get(STRING_OBJECTID);
        StringList editAccessList = new StringList(objectList.size());
        Map columnMap = (Map)programMap.get("columnMap");
        String columnName = UIUtil.getValue(columnMap, SELECT_NAME);
        LOGGER.info("columnName:{}",columnName);
        DomainObject obj = DomainObject.newInstance(context);
        for (int i = 0; i < objectList.size(); i++) {
            Map objectMap = (Map) objectList.get(i);
//            LOGGER.info("objectMap:{}", objectMap);
            String isEdit = (String) objectMap.get("isEdit");
            String JF_ProcurementGroup = (String) objectMap.get(SELECT_ATTR_JF_VPMReference_JF_ProcurementGroup);
            if(columnName.equals(ATTR_JF_VPMReference_JF_ProcurementGroup)){
                if(UIUtil.isNullOrEmpty(JF_ProcurementGroup)&&isEdit.equalsIgnoreCase("true")) {
                    editAccessList.add("true");
                }else{
                    editAccessList.add("false");
                }
            }else {
                editAccessList.add(isEdit);
            }
        }
//        LOGGER.info("editAccessList:{}", editAccessList);
        return editAccessList;
    }*/
    /*
     * @description:获取面套或者发泡的子件
     * @author: caipan
     * @date: 2025/10/30 16:23:53
     * @param: * @param[1] context
     * @param[2] rootPartBO
     * @param[3] strPartLevel
     * @param[4] Professional
     * @return:
     **/
    public MapList getOnePartListByRootPartForUOT(Context context, DomainObject rootPartBO) throws Exception {
        MapList allPartMapList = new MapList();
        StringList typeSelectList = JF_Util_mxJPO.basicBolistSel();
        typeSelectList.add(SELECT_ATTR_JFPartType);
        typeSelectList.add(SELECT_ATTR_JFDIRECT_BUY);
        typeSelectList.add(SELECT_ATTR_JF_VPMReference_JF_ProcurementGroup);
        typeSelectList.add(SELECT_ATTR_JF_ProcurementType);
        typeSelectList.add(SELECT_ATTR_JF_ProjectRel);
        typeSelectList.add(SELECT_ATTR_V_PART_NUMBER);

        StringList relSelectList = JF_Util_mxJPO.basicRellistSel();
        relSelectList.add(SELECT_ATTR_JF_Dosage);
        String where="";
         allPartMapList = rootPartBO.getRelatedObjects(context,
                JF_PLMConstants_mxJPO.REL_Instance, // relationship pattern
                JF_PLMConstants_mxJPO.TYPE_VPMReference,                                    // object pattern
                typeSelectList,                            // object selects
                relSelectList, // relationship selects
                false,                                        // to direction
                true,                                        // from direction
                (short) 1,                                    // recursion level
                where,                // object where clause
                "",
                (short) 0);

        return allPartMapList;
    }
    /*
     * @description:获取GX下面所有的整椅件
     * @author: caipan
     * @date: 2025/10/30 16:32:08
     * @param: * @param[1] context
     * @param[2] rootPartBO
     * @return:
     **/
    public MapList getOnePartListByRootPartForGX(Context context, DomainObject rootPartBO,String type) throws Exception {
        MapList allPartMapList = new MapList();
        StringList typeSelectList = JF_Util_mxJPO.basicBolistSel();
        typeSelectList.add(SELECT_ATTR_JFPartType);
        StringList relSelectList = JF_Util_mxJPO.basicRellistSel();
        String where=JF_PLMConstants_mxJPO.SELECT_ATTR_JF_PartType+"==C || "+JF_PLMConstants_mxJPO.SELECT_ATTR_JF_PartType+"=="+type;
        LOGGER.info("where:{}",where);
        //采购组
        allPartMapList = rootPartBO.getRelatedObjects(context,
                JF_PLMConstants_mxJPO.REL_Instance, // relationship pattern
                JF_PLMConstants_mxJPO.TYPE_VPMReference,                                    // object pattern
                typeSelectList,                            // object selects
                relSelectList, // relationship selects
                false,                                        // to direction
                true,                                        // from direction
                (short) 1,                                    // recursion level
                where,                // object where clause
                "",
                (short) 0);

        return allPartMapList;
    }
    /*
     * @description:SQD时间编辑权限
     * @author: caipan
     * @date: 2025/11/11 16:11:56
     * @param: * @param[1] context
     * @param[2] args
     * @return:
     **/
    public StringList getPartListPartsEditJFPartRequirementCommitmentDateAccess(Context context, String[] args) throws Exception {
        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        Map requestMap = (Map) programMap.get(STRING_REQUESTMAP);
        String strLoginUser = context.getUser();
        MapList objectList = (MapList) programMap.get("objectList");
        String strObjectId = (String) requestMap.get(STRING_OBJECTID);
        LOGGER.info("strObjectId:{}",strObjectId);
        DomainObject pl = DomainObject.newInstance(context,strObjectId);
        StringList routeList = pl.getInfoList(context,"from[Object Route].to.id");
        DomainObject routeObj = DomainObject.newInstance(context);
        String where = "owner=='"+strLoginUser+"' && current!=Complete";
LOGGER.info("where:{} routeList:{}",where,routeList);
        MapList tasklist = new MapList();
         for(int i=0;i<routeList.size();i++){
             routeObj.setId(routeList.get(i));
            MapList list = routeObj.getRelatedObjects(context,
                     RELATIONSHIP_ROUTE_TASK, //pattern to match relationships
                     TYPE_INBOX_TASK, //pattern to match types
                     JF_Util_mxJPO.basicBolistSel(), //the eMatrix StringList object that holds the list of select statement pertaining to Business Obejcts.
                     JF_Util_mxJPO.basicRellistSel(), //the eMatrix StringList object that holds the list of select statement pertaining to Relationships.
                     true, //get To relationships
                     false, //get From relationships
                     (short) 1, //the number of levels to expand, 0 equals expand all.
                     where, //where clause to apply to objects, can be empty ""
                     null, //where clause to apply to relationship, can be empty ""
                     (short) 0); //limit
             if(list.size()>0){
                 tasklist.addAll(list);
             }
         }
         LOGGER.info("tasklist:{}",tasklist);
        StringList editAccessList = new StringList(objectList.size());
        for (int i = 0; i < objectList.size(); i++) {
            Map objectMap = (Map) objectList.get(i);
            String isEdit = (String) objectMap.get(SELECT_ID);
            if(tasklist.size()>0){
                editAccessList.add("true");
            }else{
                editAccessList.add("false");
            }
        }
        return editAccessList;
    }
    public StringList getPartListPartsEditPMAccess(Context context, String[] args) throws Exception {
        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        Map requestMap = (Map) programMap.get(STRING_REQUESTMAP);
        String strLoginUser = context.getUser();
        MapList objectList = (MapList) programMap.get("objectList");
        String strObjectId = (String) requestMap.get(STRING_OBJECTID);
        LOGGER.info("strObjectId:{}",strObjectId);
        DomainObject pl = DomainObject.newInstance(context,strObjectId);
        StringList routeList = pl.getInfoList(context,"from[Object Route].to.id");
        String plcurrent = pl.getInfo(context, SELECT_CURRENT);
        DomainObject routeObj = DomainObject.newInstance(context);
        String where = "owner=='"+strLoginUser+"' && current!=Complete";
        LOGGER.info("where:{} routeList:{}",where,routeList);
        MapList tasklist = new MapList();
        for(int i=0;i<routeList.size();i++){
            routeObj.setId(routeList.get(i));
            MapList list = routeObj.getRelatedObjects(context,
                    RELATIONSHIP_ROUTE_TASK, //pattern to match relationships
                    TYPE_INBOX_TASK, //pattern to match types
                    JF_Util_mxJPO.basicBolistSel(), //the eMatrix StringList object that holds the list of select statement pertaining to Business Obejcts.
                    JF_Util_mxJPO.basicRellistSel(), //the eMatrix StringList object that holds the list of select statement pertaining to Relationships.
                    true, //get To relationships
                    false, //get From relationships
                    (short) 1, //the number of levels to expand, 0 equals expand all.
                    where, //where clause to apply to objects, can be empty ""
                    null, //where clause to apply to relationship, can be empty ""
                    (short) 0); //limit
            if(list.size()>0){
                tasklist.addAll(list);
            }
        }
        LOGGER.info("tasklist:{}",tasklist);
        StringList editAccessList = new StringList(objectList.size());
        for (int i = 0; i < objectList.size(); i++) {
            Map objectMap = (Map) objectList.get(i);
            String isEdit = (String) objectMap.get(SELECT_ID);
            //如果是工作中，PM有权限编辑
            //如果是审核中,需要看Route
            if(plcurrent.equalsIgnoreCase("InWork")){
                editAccessList.add("true");
            }
            else {
                if (tasklist.size() > 0) {
                    editAccessList.add("true");
                } else {
                    editAccessList.add("false");
                }
            }
        }
        return editAccessList;
    }

    /*
     * @description:是否有正在审批的流程
     * @author: caipan
     * @date: 2025/11/13 10:11:19
     * @param: * @param[1] null
     * @return:
     **/
    public boolean isProcessUnderApproval(Context context,String[] args) throws Exception{
        //审核状态
        String id = args[0];
        String strLoginUser = context.getUser();
        DomainObject partlistBO = DomainObject.newInstance(context,id);
        StringList routeList = partlistBO.getInfoList(context,"from[Object Route].to.id");
        DomainObject routeObj = DomainObject.newInstance(context);
        String where = "owner=='"+strLoginUser+"' && current!=Complete";
        LOGGER.info("where:{} routeList:{}",where,routeList);
        MapList tasklist = new MapList();
        for(int i=0;i<routeList.size();i++){
            routeObj.setId(routeList.get(i));
            MapList list = routeObj.getRelatedObjects(context,
                    RELATIONSHIP_ROUTE_TASK, //pattern to match relationships
                    TYPE_INBOX_TASK, //pattern to match types
                    JF_Util_mxJPO.basicBolistSel(), //the eMatrix StringList object that holds the list of select statement pertaining to Business Obejcts.
                    JF_Util_mxJPO.basicRellistSel(), //the eMatrix StringList object that holds the list of select statement pertaining to Relationships.
                    true, //get To relationships
                    false, //get From relationships
                    (short) 1, //the number of levels to expand, 0 equals expand all.
                    where, //where clause to apply to objects, can be empty ""
                    null, //where clause to apply to relationship, can be empty ""
                    (short) 0); //limit
            if(list.size()>0){
                tasklist.addAll(list);
            }
        }
        return tasklist.size()>0;
    }
    /*
     * @description:设置该批零件的0级件
     * @author: caipan
     * @date: 2025/11/17 14:47:11
     * @param: * @param[1] context
     * @param[2] root
     * @param[3] list
     * @return:
     **/
    public MapList setLevelZeroItem(Context context,DomainObject root,MapList list) throws Exception{
        LOGGER.info("setLevelZeroItem");
        StringList selList = new StringList();
        selList.add(SELECT_NAME);
        selList.add(SELECT_REVISION);
        selList.add(SELECT_ATTR_JF_PartNameCN);
        selList.add(SELECT_ATTR_V_PART_NUMBER);

        Map attributeMap = root.getInfo(context,selList);
        String V_PartNumber = UIUtil.getValue(attributeMap,JF_PLMConstants_mxJPO.SELECT_ATTR_V_PART_NUMBER);
        String partNameCN = UIUtil.getValue(attributeMap,JF_PLMConstants_mxJPO.SELECT_ATTR_JF_PartNameCN);
        String name = UIUtil.getValue(attributeMap,SELECT_NAME);
        String revision = UIUtil.getValue(attributeMap,SELECT_REVISION);
        String partNumber = UIUtil.isNotNullAndNotEmpty(V_PartNumber)?V_PartNumber:name;
        partNumber = partNumber+"_"+revision;
        for(int i=0;i<list.size();i++){
            Map map = (Map)list.get(i);
            map.put(key1, partNumber);
            map.put(key2, partNameCN);
        }
        return list;
    }

    /**
     * 1. JFProjectPhase初始化/编辑
     * 【partList创建页面/属性编辑页面
     * 【项目阶段】属性根据项目是否有DV阶段显示对应的range选项值，即：
     * （1）有DV项目计划：选项值为Phase1至Phase5完整选项；
     * （2）无DV项目计划：选项值为Phase1、Phase2+3、
     * @param context
     * @param args
     * @author LIUJR
     * @throws
     * @return java.util.Map
     * @date 2026/5/19 15:47
     * @description
     */
    public Map getJFProjectPhaseRange(Context context, String[] args) throws Exception {
        Map returnMap = new HashMap();
        try {
            Map map = (Map) JPO.unpackArgs(args);
            Map requestMap = (Map) map.get("requestMap");
            String objectId = (String) requestMap.get(JF_PLMConstants_mxJPO.STRING_OBJECTID);
            Map fieldMap = (Map) map.get("fieldMap");
            String name = (String) fieldMap.get(SELECT_NAME);
            StringList ranges = FrameworkUtil.getRanges(context, name);
            ranges.remove("");
            LOGGER.info("ranges:{}", ranges);
            DomainObject object = DomainObject.newInstance(context, objectId);
            Boolean noDVFlag  = JF_ESO_mxJPO.projectSpaceHasDVFlag(context, object);
            if (noDVFlag) {
                ranges.remove("phase2");
                ranges.remove("phase3");
            } else {
                ranges.remove("phase2+3");
            }
            ranges.sort();
            returnMap.put("field_choices", ranges);
            returnMap.put("field_display_choices", ranges);
        }catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
        return returnMap;
    }

}
