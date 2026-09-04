import com.dassault_systemes.pprRestServices.utils.MQLUtil;
import com.dscn.plm.util.NioJDUtils;
import com.matrixone.apps.common.util.ComponentsUIUtil;
import com.matrixone.apps.common.util.ComponentsUtil;
import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.DomainRelationship;
import com.matrixone.apps.domain.Job;
import com.matrixone.apps.domain.util.*;
import com.matrixone.apps.framework.ui.UIUtil;
import com.matrixone.apps.program.ProgramCentralUtil;
import com.nomagic.esi.common.a.M;
import jakarta.activation.DataHandler;
import jakarta.activation.DataSource;
import jakarta.activation.FileDataSource;
import jakarta.mail.BodyPart;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMultipart;
import jakarta.mail.internet.MimeUtility;
import javassist.compiler.ast.StringL;
import matrix.db.*;
import matrix.util.SelectList;
import matrix.util.StringList;
import org.apache.axis.encoding.ser.MapSerializer;
import org.apache.commons.lang3.SerializationUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellReference;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;
import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.dassault_systemes.product.common.services.utility.Value.VPMInstance;
import static com.matrixone.apps.domain.DomainConstants.*;
import static com.matrixone.apps.domain.DomainConstants.SELECT_ID;

/**
 * @author CHENYAN
 * @version v1.0.0
 * 创建时间：2025/4/23 09:47
 * @description 新类型ECR业务处理类
 */
public class JF_NewECRService_mxJPO implements JF_PLMConstants_mxJPO {
    private static final Logger _logger = LoggerFactory.getLogger(JF_NewECRService_mxJPO.class);
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    /**
     * @param context
     * @param args
     * @return com.matrixone.apps.domain.util.MapList
     * @throws
     * @description 获取受影响零件 Table数据
     * @author CHENYAN
     * @date 2024/7/25 15:24
     */
    public MapList getECRAffectedItems(Context context, String[] args) throws Exception {
        _logger.info("-----------------------------------getECRAffectedItems begin ---------------------------------------------");
        Map parameters = JPO.unpackArgs(args);
        String strObjectId = (String) parameters.get("objectId");
        DomainObject ecr = DomainObject.newInstance(context, strObjectId);
        StringList reSelectList = new StringList(SELECT_RELATIONSHIP_ID);
        StringList typeSelectList = new StringList(SELECT_ID);
        typeSelectList.add(SELECT_REVISION);
        typeSelectList.add(SELECT_CURRENT);
        reSelectList.add(SELECT_ATTR_JFCHANGESOURCE);
        reSelectList.add(SELECT_ATTRIBUTE_JF_ECRID);
        reSelectList.add(SELECT_ATTRIBUTE_JF_ChangeBeforeRev);
        reSelectList.add(SELECT_ATTRIBUTE_JF_BOMChangeDes);
        reSelectList.add(SELECT_ATTRIBUTE_JF_BOMQuantity);
        String relWhere = "";
//        String relWhere = SELECT_ATTRIBUTE_JF_ECRID + "==" + strObjectId;
        MapList maps = ecr.getRelatedObjects(context, REL_JFECRRelateRoot, // relationship pattern
                JF_PLMConstants_mxJPO.TYPE_VPMReference,                                    // object pattern
                typeSelectList,                            // object selects
                reSelectList, // relationship selects
                false,                                        // to direction
                true,                                        // from direction
                (short) 1,                                    // recursion level
                "",                // object where clause
                relWhere,
                (short) 0);
        MapList mapList = (MapList) maps.stream().filter(m -> {
            Map map = (Map) m;
            String value = UIUtil.getValue(map, SELECT_ATTRIBUTE_JF_ECRID);
            if (value.contains(strObjectId)) {
                return true;
            } else {
                return false;
            }
        }).collect(Collectors.toCollection(MapList::new));
                _logger.info("-----------------------------------getECRAffectedItems end ---------------------------------------------");

        //查出后面需要的数据
//        return mapList;
        mapList = getPartItemAttrMapList(context, mapList, strObjectId, typeSelectList);
        _logger.info("mapList:{}", mapList);
        return mapList;
    }



    /**
    *
    * @param context
	* @param mapList
    * @author LIUJR
    * @throws
    * @return com.matrixone.apps.domain.util.MapList
    * @date 2025/7/31 15:31
    * @description
    */
    public static  MapList getPartItemAttrMapList(Context context, MapList mapList, String ecrId, StringList typeSelectList) throws Exception{
        MapList returnMapList = new MapList();
        Map ecrChangeMess = getECRChangeMess(context, new String[]{});
        JF_Util_mxJPO jfUtilMxJPO = new JF_Util_mxJPO();
        DomainObject domainObject = DomainObject.newInstance(context);
        domainObject.setId(ecrId);
        MapList itemMapList = domainObject.getRelatedObjects(context, REL_JFRELATEITEM, // relationship pattern
                JF_PLMConstants_mxJPO.TYPE_VPMReference,                                    // object pattern
                typeSelectList,                            // object selects
                StringList.create(SELECT_ATTR_JJFIsFollow, SELECT_ATTR_JFCHANGESOURCE), // relationship selects
                false,                                        // to direction
                true,                                        // from direction
                (short) 1,                                    // recursion level
                "",                // object where clause
                "",
                (short) 0);
        Map mapListGroupingMap = getMapListGroupingMap(context, itemMapList, SELECT_ID);
        for (int i = 0; i < mapList.size(); i++) {
            Map map = (Map) mapList.get(i);
            HashMap<String, Object> hashMap = new HashMap<>();
            String rev = UIUtil.getValue(map, SELECT_REVISION);
            String id = UIUtil.getValue(map, SELECT_ID);
            String level = UIUtil.getValue(map, SELECT_LEVEL);
            String connId = UIUtil.getValue(map, SELECT_RELATIONSHIP_ID);
            String changeBeforeRev = UIUtil.getValue(map, SELECT_ATTRIBUTE_JF_ChangeBeforeRev);
            String bomChangeDes = UIUtil.getValue(map, SELECT_ATTRIBUTE_JF_BOMChangeDes);
            String bomQuantity = UIUtil.getValue(map, SELECT_ATTRIBUTE_JF_BOMQuantity);
            //变更前版本
            String jf_BeforeRev = EMPTY_STRING;
            if (bomChangeDes.equalsIgnoreCase(UIUtil.getValue(ecrChangeMess, "Add"))
                    || bomChangeDes.contains(UIUtil.getValue(ecrChangeMess, "AddRelease"))
                    || bomChangeDes.contains(UIUtil.getValue(ecrChangeMess, "FirstRelease"))) {
                jf_BeforeRev = EMPTY_STRING;
            } else if (bomChangeDes.contains("数量") || bomChangeDes.contains(UIUtil.getValue(ecrChangeMess, "Delete"))) {
                //为当前版本
                jf_BeforeRev = (String) id;
            } else {
//                jf_BeforeRev = jfUtilMxJPO.getPreviousReleasedMajorId(context, id);
                if (UIUtil.isNullOrEmpty(changeBeforeRev)) {
                    jf_BeforeRev = jfUtilMxJPO.getPreviousReleasedMajorId(context, id);
                } else {
                    jf_BeforeRev = changeBeforeRev;
                }
            }
            hashMap.put("JF_BeforeRev", jf_BeforeRev);
            String strRevision = EMPTY_STRING;
            if (bomChangeDes.contains(UIUtil.getValue(ecrChangeMess, "Delete"))) {
                strRevision = "";
            } else {
                strRevision = rev;
            }
            hashMap.put("revision", strRevision);
            hashMap.put("JF_ChangeBeforeRev", changeBeforeRev);
            hashMap.put(SELECT_ID, id);
            hashMap.put(SELECT_LEVEL, level);
            hashMap.put(SELECT_RELATIONSHIP_ID, connId);
            String  changeSource = "";
            String isFollow = "";
            if (mapListGroupingMap.containsKey(id)) {
                List infoList = (List) mapListGroupingMap.get(id);
                Map map1  = (Map) infoList.get(0);
                isFollow = UIUtil.getValue(map1, SELECT_ATTR_JJFIsFollow);
                changeSource = UIUtil.getValue(map1, SELECT_ATTR_JFCHANGESOURCE);
            }
            //图纸
            domainObject.setId(id);
            MapList documentMapList = domainObject.getRelatedObjects(context, "Reference Document", "Document", StringList.create("id","name","attribute[Title]", "current"), new StringList(),
                    false, true, (short) 1, "attribute[JF_DocumentType].value==Drawing", "", 0);
            MapList drawingMapList = domainObject.getRelatedObjects(context, "XCADBaseDependency", "Drawing", StringList.create("id","attribute[PLMEntity.V_Name]", "current"), new StringList(),
                    true, false, (short) 1, "", "", 0);
            documentMapList.addAll(drawingMapList);
            hashMap.put("JFChangeResource", changeSource);
            hashMap.put("JFIsFollow", isFollow);
            hashMap.put("JF_BOMChangeDes", bomChangeDes);
            hashMap.put("JF_BOMQuantity", bomQuantity);
            hashMap.put("Drawing", documentMapList);
            returnMapList.add(hashMap);
        }
        return returnMapList;
    }

    /**
    * 获取ecr的受影响层级
    * @param context
	* @param args
    * @author LIUJR
    * @throws
    * @return com.matrixone.apps.domain.util.MapList
    * @date 14/05/2025 11:35
    * @description
    */
    public MapList getExpandECRAffectedItems(Context context, String[] args)  {
        MapList mapList = new MapList();
        try {
            Map paramMap = JPO.unpackArgs(args);
//            _logger.info("paramMap:{}", paramMap.toString());
            String objId = (String)paramMap.get("objectId");
            String ecrId = (String)paramMap.get("parentOID");
            String expandLevel = (String) paramMap.get("expandLevel");
            if ("All".equals(expandLevel)) {
                expandLevel = "0";
            }
            DomainObject obj = DomainObject.newInstance(context,objId);
            StringList boSel = JF_Util_mxJPO.basicBolistSel();
            StringList relSel = new StringList();
            relSel.add(SELECT_RELATIONSHIP_ID);
            relSel.add(SELECT_ATTRIBUTE_JF_ECRID);
            relSel.add(SELECT_ATTRIBUTE_JF_ChangeBeforeRev);
            relSel.add(SELECT_ATTRIBUTE_JF_BOMChangeDes);
            relSel.add(SELECT_ATTRIBUTE_JF_BOMQuantity);
            MapList childPartList = new MapList();
            childPartList =  obj.getRelatedObjects(context,
                    JF_PLMConstants_mxJPO.REL_JFECRRoot2Item,
                    JF_PLMConstants_mxJPO.TYPE_VPMReference,
                    boSel,
                    relSel,
                    false,
                    true,
                    Short.parseShort(expandLevel),
                    "",
                    "",
                    0);
            _logger.info("childPartList:{}", childPartList.toString());
            mapList = (MapList) childPartList.stream().filter(m -> {
                Map map = (Map) m;
                String value = UIUtil.getValue(map, SELECT_ATTRIBUTE_JF_ECRID);
                if (value.contains(ecrId)) {
                    return true;
                } else {
                    return false;
                }
            }).collect(Collectors.toCollection(MapList::new));
            mapList =  getPartItemAttrMapList(context, mapList, ecrId, boSel);
            _logger.info("mapList:{}", mapList.toString());
        }catch (Exception e){
            e.printStackTrace();
        }
        _logger.info("mapList:{}", mapList);
        return mapList;
    }


    public void createRouteInReview(Context context, String[] args) throws Exception {
        _logger.info("------------------------ createRouteInSubmit begin  -----------------------------------");
        String strObjectId = args[0];
        String strCurrent = args[1];
        String strNextState = args[2];
        _logger.info("strObjectId:{}", strObjectId);
        _logger.info("strCurrent:{}", strCurrent);
        _logger.info("strNextState:{}", strNextState);
        StringList typeSelectList = new StringList(SELECT_ID);
        typeSelectList.add(SELECT_NAME);
        typeSelectList.add("attribute["+ATTR_JFIsLastQuote+"]");
        typeSelectList.add("attribute["+ATTR_JFIsTKOData+"]");
        typeSelectList.add("attribute["+ATTR_JFProjectPhase+"]");
        StringList reSelectList = new StringList(SELECT_RELATIONSHIP_ID);
        DomainObject ecr = DomainObject.newInstance(context, strObjectId);
        //此处修改为直线经理 mod by chenyan 2024/09/05
        String strLineMess = EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", context.getLocale(), "emxComponents.ECR.Review.LineManager");
        String strChairMess = EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", context.getLocale(), "emxComponents.ECR.Review.ChairManager");
        String strDFXManagerMess = EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", context.getLocale(), "emxComponents.ECR.Review.DFXManager");
        MapList approveList = new MapList();
        Integer approveIndex = 1;
        // id
        String strLinkManager = JF_PublicMethodClass_mxJPO.getPersonLineManager(context, "", context.getUser());
        if (UIUtil.isNotNullAndNotEmpty(strLinkManager)) {
            Map managerMap = JF_PublicMethodClass_mxJPO.getMapAnyOrAll(strLinkManager, strLineMess, "true", approveIndex.toString(), "All");
            approveList.add(managerMap);
            approveIndex++;
        }

        //获取整椅经理
        String strProjectSpaceId = ecr.getInfo(context, "from[JFChange2Project].to.id");
        MapList chairManager = JF_PublicMethodClass_mxJPO.getProjectPersonByRoleName(context, strProjectSpaceId, "Chair manager", typeSelectList, reSelectList);
        // add by chenyan 2025/05/07 新增获取影响项目的整椅经理添加到流程中
        //获取受影响项目的整椅经理
        Set<String> affectedProjectWholeChairPersonSet = getAffectedProjectWholeChairPerson(context, strObjectId);

        if (chairManager.size() > 0) {
            for (int i = 0; i < chairManager.size(); i++) {
                Map person = (Map) chairManager.get(i);
                String strPersonId = (String) person.get(SELECT_ID);
                // 移除掉重复的人员
                if (affectedProjectWholeChairPersonSet.size() > 0){
                    if (affectedProjectWholeChairPersonSet.contains(strPersonId)) {
                        affectedProjectWholeChairPersonSet.remove(strPersonId);
                    }
                }
                Map managerMap = JF_PublicMethodClass_mxJPO.getMapAnyOrAll(strPersonId, strChairMess, "true", approveIndex.toString(), "All");
                approveList.add(managerMap);
            }
            if (affectedProjectWholeChairPersonSet.size() > 0){
                for (String strPersonId : affectedProjectWholeChairPersonSet) {
                    Map managerMap = JF_PublicMethodClass_mxJPO.getMapAnyOrAll(strPersonId, strChairMess, "true", approveIndex.toString(), "All");
                    approveList.add(managerMap);
                }
            }
            approveIndex++;
        }else if (affectedProjectWholeChairPersonSet.size() > 0){
            for (String strPersonId : affectedProjectWholeChairPersonSet) {
                Map managerMap = JF_PublicMethodClass_mxJPO.getMapAnyOrAll(strPersonId, strChairMess, "true", approveIndex.toString(), "All");
                approveList.add(managerMap);
            }
            approveIndex++;
        }
        //DFX
        Map attrmap=ecr.getInfo(context,typeSelectList);
        String JFIsLastQuote= (String) attrmap.get("attribute["+ATTR_JFIsLastQuote+"]");
        String JFIsTKOData= (String) attrmap.get("attribute["+ATTR_JFIsTKOData+"]");
        String JFProjectPhase= (String) attrmap.get("attribute["+ATTR_JFProjectPhase+"]");


        _logger.info("JFIsLastQuote---lxg->"+JFIsLastQuote);
        _logger.info("JFIsTKOData---lxg->"+JFIsTKOData);
        _logger.info("JFProjectPhase---lxg->"+JFProjectPhase);

        DomainObject projectSpaceObj=DomainObject.newInstance(context,strProjectSpaceId);
        String JFProjectLevel=projectSpaceObj.getAttributeValue(context,"JFProjectLevel");
        _logger.info("JFProjectLevel---lxg->"+JFProjectLevel);
        //当项目阶段=Phase1时，【是否最后一轮报价】选项为“是”且项目等级=C类项目or衍生项目时，必须要有【DFX审批】节点；
        //当项目阶段=Phase2-Phase5时，【是否TKO数据】选项为“是”时，必须要有【DFX审批】节点；
        if((("C".equalsIgnoreCase(JFProjectLevel)||"Derive".equalsIgnoreCase(JFProjectLevel))&&"phase1".equalsIgnoreCase(JFProjectPhase)&&"Yes".equalsIgnoreCase(JFIsLastQuote))
            ||((!"phase1".equalsIgnoreCase(JFProjectPhase))&&"Yes".equalsIgnoreCase(JFIsTKOData))){


            String  groupName =JF_PublicMethodClass_mxJPO.getBasicUrl(context, new String[]{"3dspace.GroupName.DFX_manager"});
            String objectId = DomainConstants.EMPTY_STRING;
            StringList busSel = new StringList();
            busSel.add(DomainConstants.SELECT_ID);
            busSel.add(DomainConstants.SELECT_NAME);
            busSel.add(DomainConstants.SELECT_REVISION);
            MapList partnerMapList = DomainObject.findObjects(context,"Group",DomainConstants.QUERY_WILDCARD,"name=='" + groupName + "'",busSel);
            if(!partnerMapList.isEmpty()){
                Map partnerMap = (Map)partnerMapList.get(0);
                objectId = (String)partnerMap.get(DomainConstants.SELECT_ID);
            }

            if (UIUtil.isNotNullAndNotEmpty(objectId)) {
                DomainObject domainObject = DomainObject.newInstance(context, objectId);
                StringList infoList = domainObject.getInfoList(context, "from[Group Member].to.id");

                for (String strPersonId : infoList) {
                    Map managerMap = JF_PublicMethodClass_mxJPO.getMapAnyOrAll(strPersonId, strDFXManagerMess, "true", approveIndex.toString(), "All");
                    approveList.add(managerMap);
                }
                approveIndex++;

            }
        }

        //

        String strDepartmentMess = EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", context.getLocale(), "emxComponents.ECR.Review.DepartmentManager");
        String strProjectManagerMess = EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", context.getLocale(), "emxComponents.ECR.Review.ProjectManager");
        String strProjectOwner = JF_Util_mxJPO.getProjectManager(context, new String[]{strProjectSpaceId});
        reSelectList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JFROUTENOTE_APPROVALAROLE);
        reSelectList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JFROUTEOBJECTSTATUS);
        //部门经理审批人
        MapList departManagerPersonList = ecr.getRelatedObjects(context, JF_PLMConstants_mxJPO.REL_ECR2PERSON, // relationship pattern
                TYPE_PERSON,                                    // object pattern
                typeSelectList,                            // object selects
                reSelectList, // relationship selects
                false,                                        // to direction
                true,                                        // from direction
                (short) 1,                                    // recursion level
                "",                // object where clause
                "",
                (short) 0);

        //可能会存在多个
        if (departManagerPersonList.size() > 0) {
            for (int i = 0; i < departManagerPersonList.size(); i++) {
                Map person = (Map) departManagerPersonList.get(i);
                String strPersonId = (String) person.get(SELECT_ID);
                Map managerMap = JF_PublicMethodClass_mxJPO.getMapAnyOrAll(strPersonId, strDepartmentMess, "true", approveIndex.toString(), "All");
                approveList.add(managerMap);
            }
            approveIndex++;
        }
        //项目经理
        DomainObject PMObj = PersonUtil.getPersonObject(context, strProjectOwner);
        String strPMId = PMObj.getInfo(context, SELECT_ID);
        Map managerMap = JF_PublicMethodClass_mxJPO.getMapAnyOrAll(strPMId, strProjectManagerMess, "true", approveIndex.toString(), "All");
        approveList.add(managerMap);
        //创建流程
        JF_Route_mxJPO jf_route = new JF_Route_mxJPO(context, args);
        String state = "state_Review";//在哪个状态增加流程
        String policy = "policy_JFNewECR";//哪个Policy上面
        String routeDescription = "ECR提交审核";//流程描述
        String routeId = jf_route.createAndStartRoute(context, approveList, strObjectId, state, policy, routeDescription);
        _logger.info("------------------------ createRouteInSubmit end  -----------------------------------");
    }

    public int checkNewECRHasDocument(Context context, String[] args) throws Exception {
        _logger.info("------------------------ checkECRHasDocument begin  -----------------------------------");
        String strObjectId = args[0];
        String strCurrent = args[1];
        String strNextState = args[2];
        _logger.info("strObjectId:{}", strObjectId);
        _logger.info("strCurrent:{}", strCurrent);
        _logger.info("strNextState:{}", strNextState);
        DomainObject ecr = DomainObject.newInstance(context, strObjectId);
        StringList reSelectList = new StringList(SELECT_RELATIONSHIP_ID);
        StringList typeSelectList = new StringList(SELECT_ID);
        int iRes = 0;
        if ("Create".equals(strCurrent)) {
            MapList maps = ecr.getRelatedObjects(context, RELATIONSHIP_REFERENCE_DOCUMENT, // relationship pattern
                    TYPE_DOCUMENT,                                    // object pattern
                    typeSelectList,                            // object selects
                    reSelectList, // relationship selects
                    false,                                        // to direction
                    true,                                        // from direction
                    (short) 1,                                    // recursion level
                    "",                // object where clause
                    "to.owner==from.owner&&attribute[Project Role]==''",
                    (short) 0);
            if (maps.size() <= 0) {
                iRes = 1;
                String strMess = EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", context.getLocale(), "emxComponents.Mess.ECR.DocumentIsRequire");
                emxContextUtilBase_mxJPO.mqlWarning(context, strMess);
            }
        }
        _logger.info("------------------------ checkECRHasDocument end  -----------------------------------");
        return iRes;
    }

    /**
    * New ECR的提交到审核状态的校验
    * @param context
	* @param args
    * @author
    * @throws
    * @return int
    * @date 13/05/2025 13:44
    * @description
    */
    public int checkNewECRHasPromoteAccess(Context context, String[] args) throws Exception {
        try {
            String strObjectId = args[0];
            DomainObject ecr = DomainObject.newInstance(context, strObjectId);
            //获取ecr的第一层受影响项
            StringList basicRellistSel = JF_Util_mxJPO.basicRellistSel();
            basicRellistSel.add(JF_PLMConstants_mxJPO.SELECT_ATTRIBUTE_JF_BOMChangeDes);
            HashMap<String, String> oneAffectedMap = new HashMap<>();
            StringList oneAffectedList = (StringList) ecr.getRelatedObjects(context, REL_JFECRRelateRoot, // relationship pattern
                    TYPE_VPMReference,                                    // object pattern
                    JF_Util_mxJPO.basicBolistSel(),                            // object selects
                    basicRellistSel, // relationship selects
                    false,                                        // to direction
                    true,                                        // from direction
                    (short) 1,                                    // recursion level
                    "",                // object where clause
                    "",
                    (short) 0
            ).stream().map(m -> {
                Map map = (Map) m;
                oneAffectedMap.put(UIUtil.getValue(map, SELECT_ID), UIUtil.getValue(map, JF_PLMConstants_mxJPO.SELECT_ATTRIBUTE_JF_BOMChangeDes));
                return UIUtil.getValue(map, SELECT_ID);
            }).collect(Collectors.toCollection(StringList::new));
            String unChanged = ComponentsUIUtil.getI18NString(context, context.getLocale().toString(), "emxComponents.ECRChange.UnChanged", new String[]{});
            StringList reSelectList = new StringList(SELECT_RELATIONSHIP_ID);
            StringList typeSelectList = new StringList(SELECT_ID);
            typeSelectList.add(SELECT_CURRENT);
            typeSelectList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JFDIRECT_BUY);
            typeSelectList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JF_ProcurementType);
            typeSelectList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JFPART_NUMBER);
            typeSelectList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_V_PART_NUMBER);
            typeSelectList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_V_CADOrigin);
//            typeSelectList.add(JF_PLMConstants_mxJPO.SELECT_TO_INSTANCE_ATTR_V_CADOrigin);
//            typeSelectList.add(JF_PLMConstants_mxJPO.SELECT_TO_INSTANCE_ATTR_V_CADOrigin);
//            typeSelectList.add(JF_PLMConstants_mxJPO.SELECT_TO_INSTANCE_FROM_ID);
            //标识是总成还是part
            typeSelectList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_V_USAGE);
            reSelectList.add(SELECT_ATTR_JFCHANGESOURCE);
            reSelectList.add(SELECT_FROM_ID);
            reSelectList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JFSubIFReplace);
//            _logger.info("");
            MapList maps = ecr.getRelatedObjects(context, REL_JFRELATEITEM + "," + JF_PLMConstants_mxJPO.REL_JFRelateItemParent, // relationship pattern
                    TYPE_VPMReference,                                    // object pattern
                    typeSelectList,                            // object selects
                    reSelectList, // relationship selects
                    false,                                        // to direction
                    true,                                        // from direction
                    (short) 1,                                    // recursion level
                    "",                // object where clause
                    "",
                    (short) 0);
            if (maps.size() > 0) {
                //未维护 JF_VPMReference.JF_DirectBuy 属性 零件号集合
                //未维护 采购类型 零件号集合
                StringList notWritePartList = new StringList();
                Map<String, Set<String>> notContainPartMap = new HashMap<>();
                //分组
                Map groupMap = (Map) maps.stream().collect(Collectors.groupingBy(m -> {
                    Map info = (Map) m;
                    return info.get("relationship");
                }));
//                _logger.info("groupMap:{}", groupMap);
                boolean isAlert = false;
                if (groupMap.containsKey(REL_JFRELATEITEM)) {
                    List affectedItemsList = (List) groupMap.get(REL_JFRELATEITEM);
                    Set affectedItemsIdSet = (Set) affectedItemsList.stream().map(m -> {
                        Map info = (Map) m;
                        return info.get(SELECT_ID);
                    }).collect(Collectors.toSet());
                    //add by 需要校验一级件的这两个属性是否维护 2.如果是子件需要校验一级件中的这两个属性是否维护
                    for (int i = 0; i < affectedItemsList.size(); i++) {
                        Map affectedItem = (Map) affectedItemsList.get(i);
                        String strDirectBuy = (String) affectedItem.get(JF_PLMConstants_mxJPO.SELECT_ATTR_JFDIRECT_BUY);
                        String strProcurementType = (String) affectedItem.get(JF_PLMConstants_mxJPO.SELECT_ATTR_JF_ProcurementType);
                        String strAffectedItemId = (String) affectedItem.get(SELECT_ID);
                        String strAffectedItemName = (String) affectedItem.get(SELECT_NAME);
                        String strPartNum = (String) affectedItem.get(JF_PLMConstants_mxJPO.SELECT_ATTR_V_PART_NUMBER);
                        //未维护 JF_VPMReference.JF_DirectBuy
                        if (UIUtil.isNullOrEmpty(strDirectBuy) || UIUtil.isNullOrEmpty(strProcurementType)) {
                            isAlert = true;
                            notWritePartList.add(strPartNum);
                        }
//                        if (affectedItem.containsKey(JF_PLMConstants_mxJPO.SELECT_ATTR_V_CADOrigin)){
//                            String strFlagOnePart = (String) affectedItem.get(JF_PLMConstants_mxJPO.SELECT_ATTR_V_CADOrigin);
//                            Object strFromFlagOnePart = UIUtil.getValue(affectedItem,JF_PLMConstants_mxJPO.SELECT_TO_INSTANCE_ATTR_V_CADOrigin);
//
//                            //父件还是 CATIAV5 标识为一级件
//                            if ("CATIAV5".equals(strFlagOnePart) && "CATIAV5".equals(strFromFlagOnePart)){
//                                String strFromId = UIUtil.getValue(affectedItem,JF_PLMConstants_mxJPO.SELECT_TO_INSTANCE_FROM_ID);
//                                DomainObject fromObj = DomainObject.newInstance(context, strFromId);
//                                StringList fromTypeSelect = new StringList();
//                                fromTypeSelect.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JFDIRECT_BUY);
//                                fromTypeSelect.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JF_ProcurementType);
//                                fromTypeSelect.add(JF_PLMConstants_mxJPO.SELECT_ATTR_V_PART_NUMBER);
//                                Map fromObjInfo = fromObj.getInfo(context, fromTypeSelect);
//                                String strFromDirectBuy = (String) fromObjInfo.get(JF_PLMConstants_mxJPO.SELECT_ATTR_JFDIRECT_BUY);
//                                String strFromProcurementType = (String) fromObjInfo.get(JF_PLMConstants_mxJPO.SELECT_ATTR_JF_ProcurementType);
//                                String strFromPartNum = (String) fromObjInfo.get(JF_PLMConstants_mxJPO.SELECT_ATTR_V_PART_NUMBER);
//                                //未维护 JF_VPMReference.JF_DirectBuy
//                                if (UIUtil.isNullOrEmpty(strFromDirectBuy) || UIUtil.isNullOrEmpty(strFromDirectBuy)){
//                                    isAlert = true;
//                                    notWritePartList.add(strFromPartNum);
//                                }
//                            }
//                        }

                        //零件清单中如果存在装配，需要校验该装配实际关联的子件已经发布或装配下的未发布零件已经存在于该CR的零件清单中。
                        //装配和零件标识
                        //增加逻辑  2025-05-13  判断当前这个装配是否替换过来 是 不需要校验
                        String strV_Usage = (String) affectedItem.get(JF_PLMConstants_mxJPO.SELECT_ATTR_V_USAGE);
                        if (UIUtil.isNullOrEmpty(strV_Usage)) {
                            MapList checkMapList = new MapList();
                            if (oneAffectedList.contains(strAffectedItemId)) {
                                String strBOMChangeDes = oneAffectedMap.get(strAffectedItemId);
                                if (!unChanged.equalsIgnoreCase(strBOMChangeDes)) {
                                    //不用校验了
                                    DomainObject usAgeBo = DomainObject.newInstance(context, strAffectedItemId);
                                    StringList usAgeTypeSelectList = JF_Util_mxJPO.basicBolistSel();
                                    usAgeTypeSelectList.add(SELECT_CURRENT);
                                    usAgeTypeSelectList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JFPART_NUMBER);
                                    usAgeTypeSelectList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_V_PART_NUMBER);
                                    //优化逻辑，只有是总成的时候才查询
                                    checkMapList = usAgeBo.getRelatedObjects(context, VPMInstance, // relationship pattern
                                            TYPE_VPMReference,                                    // object pattern
                                            usAgeTypeSelectList,                            // object selects
                                            JF_Util_mxJPO.basicRellistSel(), // relationship selects
                                            false,                                        // to direction
                                            true,                                        // from direction
                                            (short) 0,                                    // recursion level
                                            "",                // object where clause
                                            "",
                                            (short) 0);
                                }
                            }
//                            MapList checkMapList = checkMapValueContainByMapList(VPMInstanceList, SELECT_FROM_ID, strAffectedItemId);
                            if (null != checkMapList && checkMapList.size() > 0) {
                                //装配下存在零件
                                for (int i1 = 0; i1 < checkMapList.size(); i1++) {
                                    Map checkMap = (Map) checkMapList.get(i1);
                                    String strCurrent = (String) checkMap.get(SELECT_CURRENT);
                                    String strCheckId = (String) checkMap.get(SELECT_ID);
                                    String strSubPartNum = (String) checkMap.get(JF_PLMConstants_mxJPO.SELECT_ATTR_V_PART_NUMBER);
                                    if (UIUtil.isNullOrEmpty(strSubPartNum)) {
                                        strSubPartNum = (String) checkMap.get(SELECT_NAME);
                                    }
                                    if ((!"RELEASED".equals(strCurrent)) && (!affectedItemsIdSet.contains(strCheckId))) {
                                        isAlert = true;
                                        //临时的零件名称
                                        String strTempName = "";
                                        if (UIUtil.isNullOrEmpty(strSubPartNum)) {
                                            strTempName = strSubPartNum;
                                        } else {
                                            strTempName = strSubPartNum;
                                        }
                                        if (notContainPartMap.containsKey(strPartNum)) {
                                            Set<String> alertSubParts = notContainPartMap.get(strPartNum);
                                            alertSubParts.add(strTempName);
                                        } else {
                                            Set alertSubParts = new HashSet<String>();
                                            alertSubParts.add(strTempName);
                                            notContainPartMap.put(strPartNum, alertSubParts);
                                        }
                                        break;
                                    }
                                }
                            }
                        }
                    }

                }
                StringList parentNotMaintain = new StringList();
                if (groupMap.containsKey(JF_PLMConstants_mxJPO.REL_JFRelateItemParent)) {
                    //需要校验该零件是否关联受影响的父件，如果关联了父件，替换规则是否维护，不允许为空。否则不允许提交状态。
                    List parentItemList = (List) groupMap.get(JF_PLMConstants_mxJPO.REL_JFRelateItemParent);
                    for (int i = 0; i < parentItemList.size(); i++) {
                        Map parentItemMap = (Map) parentItemList.get(i);
                        String strIsReplace = (String) parentItemMap.get(JF_PLMConstants_mxJPO.SELECT_ATTR_JFSubIFReplace);
                        String strPartNum = (String) parentItemMap.get(JF_PLMConstants_mxJPO.SELECT_ATTR_V_PART_NUMBER);

                        if (UIUtil.isNullOrEmpty(strIsReplace)) {
                            //没有维护
                            parentNotMaintain.add(strPartNum);
                            isAlert = true;
                        }
                    }
                }
                //校验直线经理是否存在
                String strLinkManager = JF_PublicMethodClass_mxJPO.getPersonLineManager(context, "", context.getUser());
                if (UIUtil.isNullOrEmpty(strLinkManager)) {
                    String strMess = EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", context.getLocale(), "emxComponents.Mess.ECR.LineManagerIsNull");
                    emxContextUtilBase_mxJPO.mqlWarning(context, strMess);
                    return 1;
                }
                String strProjectSpaceId = ecr.getInfo(context, "from[JFChange2Project].to.id");
                if (UIUtil.isNotNullAndNotEmpty(strProjectSpaceId)) {
                    Set projectIdSet = new HashSet<String>();
                    projectIdSet.add(strProjectSpaceId);
                    if (!projectIdSet.isEmpty()) {
                        StringList projectSpaceList = StringList.create(projectIdSet);
                        HashMap<String, Object> paramsMap = new HashMap<>();
                        paramsMap.put("projectName", projectSpaceList);
                        paramsMap.put("type", ecr.getTypeName(context));
                        paramsMap.put("ecrId", strObjectId);
                        int iRes = JF_SignTask_mxJPO.checkProjectPersonRole(context, JPO.packArgs(paramsMap));
                        if (iRes == 1) {
                            return iRes;
                        }
                    }
                } else {
                    String strMess = EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", context.getLocale(), "emxComponents.Mess.ECR.AffectedItemIsNull");
                    emxContextUtilBase_mxJPO.mqlWarning(context, strMess);
                    return 1;
                }
                //提示信息
                if (isAlert) {
//                    _logger.info("notWritePartList:{}", notWritePartList);
//                    _logger.info("notContainPartMap:{}", notContainPartMap);
//                    _logger.info("parentNotMaintain:{}", parentNotMaintain);
                    if (notContainPartMap.size() > 0) {
                        String strMess = EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", context.getLocale(), "emxComponents.Mess.ECR.SubItemStatusError");
                        StringBuilder sb = new StringBuilder();
                        for (Object oEntry : notContainPartMap.entrySet()) {
                            Map.Entry entry = (Map.Entry) oEntry;
                            String strKey = (String) entry.getKey();
                            Set subPart = (Set) entry.getValue();
                            String strMess1 = strMess.replace("$1", strKey).replace("$2", StringList.create(subPart).join(","));
                            sb.append(strMess1);
                            sb.append("\n");
                        }
//                        strMess = strMess.replace("{}",StringList.create(notContainPartList).join(","));
                        emxContextUtilBase_mxJPO.mqlWarning(context, sb.toString());
                    }
                    if (notWritePartList.size() > 0) {
                        String strMess = EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", context.getLocale(), "emxComponents.Mess.ECR.NotWriteDirectBuy");
                        strMess = strMess.replace("{}", notWritePartList.join(","));
                        emxContextUtilBase_mxJPO.mqlWarning(context, strMess);
                    }
                    if (parentNotMaintain.size() > 0) {
                        String strMess = EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", context.getLocale(), "emxComponents.Mess.ECR.ParentNotWriteError");
                        strMess = strMess.replace("{}", parentNotMaintain.join(","));
                        emxContextUtilBase_mxJPO.mqlWarning(context, strMess);
                    }
                    return 1;
                } else {
                    return 0;
                }
            } else {
                String strMess = EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", context.getLocale(), "emxComponents.Mess.ECR.AffectedItemIsNull");
                emxContextUtilBase_mxJPO.mqlWarning(context, strMess);
                return 1;
            }
        } catch (Exception e) {
            _logger.error(e.getMessage());
            throw e;
        }

    }

    /**
     * 获取ECR受影响清单的列信息
     *
     * @param context
     * @param args
     * @return java.util.Vector
     * @throws
     * @author LIUJR
     * @date 2025/4/25 10:03
     * @description
     */
    public static StringList getECRItemColumns(Context context, String[] args) throws Exception {
        _logger.info("getECRItemColumns 。。。。。。。。。。。。。。。。");
        StringList res = new StringList();
        try {
            Map paramsMap = JPO.unpackArgs(args);
            _logger.info("paramsMap:{}", paramsMap);
            //表格中的零件
            MapList objectList = (MapList) paramsMap.get(STRING_OBJECTLIST);
            Map columnMap = (Map) paramsMap.get("columnMap");
            Map colAttrMap = (Map) columnMap.get("colAttrMap");
            String colName = (String) colAttrMap.get(SELECT_NAME);
            DomainObject domainObject = DomainObject.newInstance(context);
            String mess = "<a href=\"JavaScript:emxTableColumnLinkClick('../common/emxTree.jsp?mode=popup&amp;objectId=id', '700', '600', 'false', 'popup', '')\" >Name</a>";
            for (int i = 0; i < objectList.size(); i++) {
                Map objectMap = (Map) objectList.get(i);
                _logger.info("objectMap", objectMap);
                String value = UIUtil.getValue(objectMap, colName);
                if ("JF_ChangeBeforeRev".equalsIgnoreCase(colName)) {
                    //判断如果是ECR的root节点  就不会显示整椅变更前版本
                    String idLevel = (String) objectMap.get("id[level]");
                    String[] split = idLevel.split(",");
                    if (split.length == 2) {
                        value = EMPTY_STRING;
                    } else if (UIUtil.isNotNullAndNotEmpty(value)){
                        domainObject.setId(value);
                        String revision = domainObject.getInfo(context, SELECT_REVISION);
                        value = JF_PublicMethodClass_mxJPO.getConstructDataString(context, value, revision);
                    }
                }
                if("JF_BeforeRev".equalsIgnoreCase(colName)) {
                    if (UIUtil.isNotNullAndNotEmpty(value)) {
                        domainObject.setId(value);
                        String revision = domainObject.getInfo(context, SELECT_REVISION);
                        value = JF_PublicMethodClass_mxJPO.getConstructDataString(context, value, revision);
                    } else {
                        value = EMPTY_STRING;
                    }
                }
                MapList documentMapList = (MapList) objectMap.get("Drawing");
                StringList urlList = new StringList();
                if ("ConnectionDrawing".equalsIgnoreCase(colName) || "ConnectionDrawingStatus".equalsIgnoreCase(colName)) {
                    if (!documentMapList.isEmpty()) {
                        for (Object o1 : documentMapList) {
                            Map map = (Map) o1;
                            String current = UIUtil.getValue(map, "current");
                            String id = UIUtil.getValue(map, "id");
                            String name = map.containsKey("attribute[Title]")
                                    ? UIUtil.getValue(map, "attribute[Title]")
                                    : UIUtil.getValue(map, "attribute[PLMEntity.V_Name]");
                            String let = "ConnectionDrawing".equalsIgnoreCase(colName) ? mess.replace("id", id).replace("Name", name) : current;
                            urlList.add(let);
                        }
                        value = String.join("<br/>",urlList);
                    }
                }
                res.add(value);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
        return res;
    }


    /**
     * 获取ECR变更零件的变更信息
     *
     * @param context
     * @param args
     * @return java.util.Vector
     * @throws
     * @author LIUJR
     * @date 2025/4/25 10:03
     * @description
     */
    public static StringList getECRPartChangeDetails(Context context, String[] args) throws Exception {
        _logger.info("getECRPartChangeDetails 。。。。。。。。。。。。。。。。");
        StringList res = new StringList();
        try {
            ContextUtil.pushContext(context);
            Map paramsMap = JPO.unpackArgs(args);
//            _logger.info("paramsMap:{}", paramsMap);
            //表格中的零件
            MapList objectList = (MapList) paramsMap.get(STRING_OBJECTLIST);
            Map paramList = (Map) paramsMap.get("paramList");
            Map columnMap = (Map) paramsMap.get("columnMap");
            Map colAttrMap = (Map) columnMap.get("colAttrMap");
            String colName = (String) colAttrMap.get(SELECT_NAME);
            DomainObject domainObject = DomainObject.newInstance(context);
            JF_Util_mxJPO jfUtilMxJPO = new JF_Util_mxJPO();
            Map ecrChangeMess = getECRChangeMess(context, new String[]{});
            for (int i = 0; i < objectList.size(); i++) {
                Map objectMap = (Map) objectList.get(i);
                String changeDes = UIUtil.getValue(objectMap, SELECT_ATTRIBUTE_JF_BOMChangeDes);
                String changeBeforeRev = UIUtil.getValue(objectMap, SELECT_ATTRIBUTE_JF_ChangeBeforeRev);
                String current = UIUtil.getValue(objectMap, SELECT_CURRENT);
                String strRevision = UIUtil.getValue(objectMap, SELECT_REVISION);
                if ("JF_BeforeRev".equalsIgnoreCase(colName)) {
                    //上一个版本
                    String previousReleasedId = EMPTY_STRING;
                    if (changeDes.equalsIgnoreCase(UIUtil.getValue(ecrChangeMess, "Add"))
                            || changeDes.contains(UIUtil.getValue(ecrChangeMess, "AddRelease"))
                            || changeDes.contains(UIUtil.getValue(ecrChangeMess, "FirstRelease"))) {
                        previousReleasedId = EMPTY_STRING;
                    } else if (changeDes.contains("数量") || changeDes.contains(UIUtil.getValue(ecrChangeMess, "Delete"))) {
                        //为当前版本
                        previousReleasedId = (String) objectMap.get(SELECT_ID);
                    } else {
                        String oid = (String) objectMap.get(SELECT_ID);
                        if (UIUtil.isNullOrEmpty(changeBeforeRev)) {
                            previousReleasedId = jfUtilMxJPO.getPreviousReleasedMajorId(context, oid);
                        } else {
                            previousReleasedId = changeBeforeRev;
                        }
                    }
                    if (UIUtil.isNotNullAndNotEmpty(previousReleasedId)) {
                        domainObject.setId(previousReleasedId);
                        String revision = domainObject.getInfo(context, SELECT_REVISION);
                        res.add(JF_PublicMethodClass_mxJPO.getConstructDataString(context, previousReleasedId, revision));
                    } else {
                        res.add("");
                    }
                    continue;
                }
                if ("revision".equalsIgnoreCase(colName)) {
                    if (changeDes.contains(UIUtil.getValue(ecrChangeMess, "Delete"))) {
                        res.add("");
                    } else {
                        res.add(strRevision);
                    }
                    continue;
                }
                if (null != objectMap && objectMap.containsKey("id[connection]")) {
                    String connId = (String) objectMap.get("id[connection]");
                    DomainRelationship domainRelationship = DomainRelationship.newInstance(context, connId);
                    String value = domainRelationship.getAttributeValue(context, colName);
                    if ("JF_ChangeBeforeRev".equalsIgnoreCase(colName)) {
                        if (UIUtil.isNotNullAndNotEmpty(value)) {
                            domainObject.setId(value);
                            String revision = domainObject.getInfo(context, SELECT_REVISION);
                            value = JF_PublicMethodClass_mxJPO.getConstructDataString(context, value, revision);
                        }
                    }
                    res.add(value);
                } else {
                    res.add("");
                }
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
     * 添加受影响项并对比结构
     *
     * @param context
     * @param args
     * @return void
     * @throws
     * @author LIUJR
     * @date 2025/4/22 13:34
     * @description
     */
    public static void AddAffectedItemsAndCompareStructures(Context context, String[] args) throws Exception {
        try {
            Map map = (Map) JPO.unpackArgs(args);
            ContextUtil.pushContext(context);
            ContextUtil.startTransaction(context, true);
            String objectId = UIUtil.getValue(map, STRING_OBJECTID);
            StringList strObjectIdList = (StringList) map.get("partList");
            DomainObject ecrObject = DomainObject.newInstance(context);
            ecrObject.setId(objectId);
            //ecr关联的第一层受影响项
            StringList ecrPartList = ecrObject.getInfoList(context, "from[" + REL_JFECRRelateRoot + "].to.id");
//            _logger.info("ecrPartList:{}", ecrPartList);
            DomainObject oldPart = DomainObject.newInstance(context);
            DomainObject newPart = DomainObject.newInstance(context);
            HashMap<String, String> compareMap = new HashMap<>();
            HashSet allChangePartSet = new HashSet();
            Map<String, String> ecrChangeMessMap = getECRChangeMess(context, new String[]{});
            String add = ecrChangeMessMap.get("Add");
            String addNum = ecrChangeMessMap.get("AddNum");
            String addRelease = ecrChangeMessMap.get("AddRelease");
            String firstRelease = ecrChangeMessMap.get("FirstRelease");
            for (String partId : strObjectIdList) {
                JF_Util_mxJPO jfUtilMxJPO = new JF_Util_mxJPO();
                String preReleasedMajorId = jfUtilMxJPO.getPreviousReleasedMajorId(context, partId);
//                _logger.info("preReleasedMajorId:{}", preReleasedMajorId);
                if (UIUtil.isNullOrEmpty(preReleasedMajorId)) {
                    //没有上一个版本比较  整个作为新增进去
                    if (!ecrPartList.contains(partId)) {
                        //已经关联过了相同的第一层结构  不会在关联
                        //关联第一个层级
                        DomainRelationship domainRelationship = ecrObject.addToObject(context, new RelationshipType(REL_JFECRRelateRoot), partId);
                        allChangePartSet.add(partId);
                        DomainObject domainObject = DomainObject.newInstance(context, partId);
                        HashMap attributeMap = new HashMap<>();
                        attributeMap.put(ATTRIBUTE_JF_BOMQuantity, "1");
                        attributeMap.put(ATTRIBUTE_JF_BOMChangeDes, firstRelease);
                        attributeMap.put(ATTRIBUTE_JF_ECRID, objectId);
                        attributeMap.put(ATTRIBUTE_JF_BOMChangeQuantity, "1");
                        domainRelationship.setAttributeValues(context, attributeMap);
                        //开始遍历子级节点
                        getFirstPartStructures(context, domainObject, add, addNum, addRelease, allChangePartSet, objectId);
                    }
                    continue;
                }
                //是升版的情况，开始遍历结构 并对比结构
                oldPart.setId(preReleasedMajorId);
                newPart.setId(partId);
                //第一个整椅节点  先关联
                //开始遍历以下节点
                HashMap<String, String> attributeMap = new HashMap<>();
                if (ecrPartList.contains(partId)) {
                    //已经关联过了  不会关联
                } else {
                    //父节点
                    DomainRelationship domainRelationship = ecrObject.addToObject(context, new RelationshipType(REL_JFECRRelateRoot), partId);
                    allChangePartSet.add(partId);
                    attributeMap.put(ATTRIBUTE_JF_BOMQuantity, "1");
                    attributeMap.put(ATTRIBUTE_JF_BOMChangeDes, ecrChangeMessMap.get("ReviseRelease"));
                    attributeMap.put(ATTRIBUTE_JF_ECRID, objectId);
                    attributeMap.put(ATTRIBUTE_JF_ChangeBeforeRev, preReleasedMajorId);
                    attributeMap.put(ATTRIBUTE_JF_BOMBeforeQuantity, "1");
                    domainRelationship.setAttributeValues(context, attributeMap);
                }
//                _logger.info("newPartId:{}; oldPartId", partId, preReleasedMajorId);
                getPartCompareStructures(context, oldPart, newPart, ecrChangeMessMap, objectId, allChangePartSet);
            }
//            HashMap<String, String> attrMap = new HashMap<>();
//            attrMap.put(ATTR_JFCHANGESOURCE, ecrObject.getAttributeValue(context, ATTR_JFCHANGESOURCE));
            //所有的节点建立  原受影响件的关系 并设置变更来源  JFRelateItem
            StringList allChangePartList = StringList.create(allChangePartSet);
            StringList ecrItemList = ecrObject.getInfoList(context, "from[" + REL_JFRELATEITEM + "].to.id");
            _logger.info("allChangePartList:{}", allChangePartList);
//            _logger.info("allChangePartList:{}", allChangePartList);
            StringList stringList = new StringList();
            for (String id : allChangePartList) {
//                Map paramMap = new HashMap<>();
//                paramMap.put("relName", REL_JFRelateItem);
//                paramMap.put("fromId", objectId);
//                paramMap.put("toId", id);
//                String connId = JF_PublicMethodClass_mxJPO.getTwoBusinessObjectConnId(context, JPO.packArgs(paramMap));

//                if (UIUtil.isNullOrEmpty(connId)) {
                if (!ecrItemList.contains(id)) {
                    stringList.add(id);
//                    domainRelationship.setAttributeValues(context, attrMap);
//                    DomainRelationship domainRelationship = ecrObject.addToObject(context, new RelationshipType(REL_JFRelateItem), id);
                }
            }
            //关联
            DomainRelationship.connect(context, ecrObject, new RelationshipType(REL_JFRELATEITEM), true, stringList.toStringArray());
            _logger.info("compareMap:{}", compareMap.toString());
//            _logger.info("compareMap:{}", compareMap.toString());
            ContextUtil.commitTransaction(context);
        } catch (Exception e) {
            e.printStackTrace();
            ContextUtil.abortTransaction(context);
            throw e;
        } finally {
            ContextUtil.popContext(context);
        }
    }

    /**
    * 首版搭建结构
    * @param context
	* @param domainObject
	* @param add
	* @param allChangePartSet
    * @author LIUJR
    * @throws
    * @return void
    * @date 09/05/2025 17:03
    * @description
    */
    public static void getFirstPartStructures(Context context, DomainObject domainObject, String add,String addNum, String addRelease, HashSet allChangePartSet, String ecrId) throws Exception {
        StringList boSel = JF_Util_mxJPO.basicBolistSel();
        boSel.add(LOGICAL_ID);
        boSel.add(SELECT_ATTR_V_PART_NUMBER);
        boSel.add("attribute[PLMEntity.V_Name]");
        StringList relSel = JF_Util_mxJPO.basicRellistSel();
        MapList partChildMapList = domainObject.getRelatedObjects(context, REL_Instance, TYPE_VPMReference,
                boSel, relSel, false, true, (short) 1, "", "", 0);
        Map partChildGroupingMap = getMapListGroupingMap(context, partChildMapList, SELECT_ID);
        DomainObject partObject = DomainObject.newInstance(context);
        for (Object k : partChildGroupingMap.keySet()) {
            String partId = String.valueOf(k);
            List infoList = (List) partChildGroupingMap.get(partId);
            MapList mapList = new MapList();
            mapList.addAll(infoList);
            partObject.setId(partId);
            String current = partObject.getInfo(context, SELECT_CURRENT);
            Map paramMap = new HashMap<>();
            paramMap.put("relName", REL_JFECRRoot2Item);
            paramMap.put("fromId", domainObject.getInfo(context, SELECT_ID));
            paramMap.put("toId", partId);
            String connId = JF_PublicMethodClass_mxJPO.getTwoBusinessObjectConnId(context, JPO.packArgs(paramMap));
            if (UIUtil.isNullOrEmpty(connId)) {
                DomainRelationship domainRelationship = domainObject.addToObject(context, new RelationshipType(REL_JFECRRoot2Item), partId);
                HashMap attributeMap = new HashMap<>();
                attributeMap.put(ATTRIBUTE_JF_BOMQuantity, String.valueOf(mapList.size()));
                if ("FROZEN".equalsIgnoreCase(current)) {
                    attributeMap.put(ATTRIBUTE_JF_BOMChangeDes, addRelease + "; " + addNum.replaceAll("1", String.valueOf(mapList.size())));
                } else {
                    attributeMap.put(ATTRIBUTE_JF_BOMChangeDes, add + "; " + addNum.replaceAll("1", String.valueOf(mapList.size())));
                }
                attributeMap.put(ATTRIBUTE_JF_ECRID, ecrId);
                attributeMap.put(ATTRIBUTE_JF_BOMChangeQuantity, String.valueOf(mapList.size()));
                domainRelationship.setAttributeValues(context, attributeMap);
                allChangePartSet.add(partId);
            } else {
                //有了关系 需要加数量  还要获取其ecrid 拼接进去
                DomainRelationship domainRelationship = DomainRelationship.newInstance(context, connId);
                String strEcrId = domainRelationship.getAttributeValue(context, ATTRIBUTE_JF_ECRID);
                if (UIUtil.isNotNullAndNotEmpty(strEcrId) && !strEcrId.contains(ecrId)) {
                    strEcrId += "," + ecrId;
                    domainRelationship.setAttributeValue(context, ATTRIBUTE_JF_ECRID, strEcrId);
                    domainRelationship.setAttributeValue(context, ATTRIBUTE_JF_BOMChangeQuantity, String.valueOf(mapList.size()));
                    allChangePartSet.add(partId);
                } else {
                    String attributeValue = domainRelationship.getAttributeValue(context, ATTRIBUTE_JF_BOMQuantity);
                    Integer integer = Integer.valueOf(attributeValue);
                    if (integer != mapList.size()) {
                        HashMap attributeMap = new HashMap<>();
                        attributeMap.put(ATTRIBUTE_JF_BOMQuantity, String.valueOf(mapList.size()));
                        attributeMap.put(ATTRIBUTE_JF_BOMChangeQuantity, String.valueOf(mapList.size()));
                        domainRelationship.setAttributeValues(context, attributeMap);
                    }
                }
            }
            getFirstPartStructures(context, DomainObject.newInstance(context, partId), add, addNum, addRelease, allChangePartSet, ecrId);
        }
    }

    /**
     * 获取ecr的变更信息
     *
     * @param context
     * @param args
     * @return java.util.Map
     * @throws
     * @author LIUJR
     * @date 2025/4/25 14:08
     * @description
     */
    public static Map getECRChangeMess(Context context, String[] args) {
        HashMap<String, String> returnMap = new HashMap<>();
        try {
            String Add = ComponentsUIUtil.getI18NString(context, context.getLocale().toString(), "emxComponents.ECRChange.Add", new String[]{});
            String AddRelease = ComponentsUIUtil.getI18NString(context, context.getLocale().toString(), "emxComponents.ECRChange.AddRelease", new String[]{});
            String Delete = ComponentsUIUtil.getI18NString(context, context.getLocale().toString(), "emxComponents.ECRChange.Delete", new String[]{});
            String Revise = ComponentsUIUtil.getI18NString(context, context.getLocale().toString(), "emxComponents.ECRChange.Revise", new String[]{});
            String ReviseRelease = ComponentsUIUtil.getI18NString(context, context.getLocale().toString(), "emxComponents.ECRChange.ReviseRelease", new String[]{});
            String VersionReplace = ComponentsUIUtil.getI18NString(context, context.getLocale().toString(), "emxComponents.ECRChange.VersionReplace", new String[]{});
            String AddNum = ComponentsUIUtil.getI18NString(context, context.getLocale().toString(), "emxComponents.ECRChange.AddNum", new String[]{});
            String DelNum = ComponentsUIUtil.getI18NString(context, context.getLocale().toString(), "emxComponents.ECRChange.DelNum", new String[]{});
            String FirstRelease = ComponentsUIUtil.getI18NString(context, context.getLocale().toString(), "emxComponents.ECRChange.FirstRelease", new String[]{});
            String UnChanged = ComponentsUIUtil.getI18NString(context, context.getLocale().toString(), "emxComponents.ECRChange.UnChanged", new String[]{});
            returnMap.put("Add", Add);
            returnMap.put("AddRelease", AddRelease);
            returnMap.put("Delete", Delete);
            returnMap.put("Revise", Revise);
            returnMap.put("ReviseRelease", ReviseRelease);
            returnMap.put("VersionReplace", VersionReplace);
            returnMap.put("AddNum", AddNum);
            returnMap.put("DelNum", DelNum);
            returnMap.put("FirstRelease", FirstRelease);
            returnMap.put("UnChanged", UnChanged);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return returnMap;
    }

    /**
     * 对比结构
     *
     * @param context
     * @param oldPart
     * @param newPart
     * @return void
     * @throws
     * @author LIUJR
     * @date 2025/4/22 14:06
     * @description
     */
    public static void getPartCompareStructures(Context context, DomainObject oldPart, DomainObject newPart, Map<String, String> ecrChangeMessMap, String objectId, HashSet allChangePartSet) throws Exception {
        StringList boSel = JF_Util_mxJPO.basicBolistSel();
        boSel.add(LOGICAL_ID);
        boSel.add(SELECT_ATTR_V_PART_NUMBER);
        boSel.add("attribute[PLMEntity.V_Name]");
        StringList relSel = JF_Util_mxJPO.basicRellistSel();
        relSel.add(DomainRelationship.SELECT_ID);
        MapList oldPartChildMapList = oldPart.getRelatedObjects(context, REL_Instance, TYPE_VPMReference,
                boSel, relSel, false, true, (short) 1, "", "", 0);
        MapList newPartChildMapList = newPart.getRelatedObjects(context, REL_Instance, TYPE_VPMReference,
                boSel, relSel, false, true, (short) 1, "", "", 0);
        if (oldPartChildMapList.isEmpty() && newPartChildMapList.isEmpty()) {
            return;
        }
        //各自分组 并提取logicalid进行并集交集的计算
        Map oldPartChildGroupingMap = getMapListGroupingMap(context, oldPartChildMapList, LOGICAL_ID);
        Map newPartChildGroupingMap = getMapListGroupingMap(context, newPartChildMapList, LOGICAL_ID);
        StringList oldPartChildList = (StringList) oldPartChildMapList.stream().map(m -> {
            Map map = (Map) m;
            return UIUtil.getValue(map, LOGICAL_ID);
        }).collect(Collectors.toCollection(StringList::new));
        StringList newPartChildList = (StringList) newPartChildMapList.stream().map(m -> {
            Map map = (Map) m;
            return UIUtil.getValue(map, LOGICAL_ID);
        }).collect(Collectors.toCollection(StringList::new));
        //求交集和差集
        // 求交集
        StringList intersection = oldPartChildList.stream()
                .filter(newPartChildList::contains)
                .collect(Collectors.toCollection(StringList::new));
        // 求对称差集
        HashSet differenceSet = Stream.concat(
                oldPartChildList.stream().filter(e -> !newPartChildList.contains(e)),
                newPartChildList.stream().filter(e -> !oldPartChildList.contains(e))
        ).collect(Collectors.toCollection(HashSet::new));
        StringList difference = StringList.create(differenceSet);
        /*
         *   通过MapList 集合去拿取版本等信息进行交集集合的变更判断，升版还是无变更，并对比数量  ,这里也要对比是否有新增和删除
         *   6. 通过MapList 集合去拿数据等进行，差集合的变更判断， 新增/删除， 并对比数量
         *   7. 将上述的变更 + 数量 进行记录
         */
        for (String id : intersection) {
            List oldInfoList = (List) oldPartChildGroupingMap.get(id);
            List newInfoList = (List) newPartChildGroupingMap.get(id);
            MapList oldMapList = new MapList();
            oldMapList.addAll(oldInfoList);
            MapList newMapList = new MapList();
            newMapList.addAll(newInfoList);
            //两个版本的都有的情况： 1.升版中有新增删除，升版新增删除 2.
            //这里要考虑 是不是升版 如果是 再比对数量，是否有升版添加或者删除； 如果没有升版，需要判断添加删除的个数
            String result = getChangeResultsMap(context, oldMapList, newMapList, ecrChangeMessMap, objectId, allChangePartSet);
            if (UIUtil.isNullOrEmpty(result)) {
                continue;
            }
            _logger.info("!!!!!!!!!!!!!!!!!!result:{}", result);
            String[] split = result.split("\\|");
            String partId = UIUtil.getValue((Map) newMapList.get(0), DomainConstants.SELECT_ID);
            Map map = new HashMap<>();
            map.put("relName", REL_JFECRRoot2Item);
            map.put("fromId", newPart.getId(context));
            map.put("toId", partId);
            String connId = JF_PublicMethodClass_mxJPO.getTwoBusinessObjectConnId(context, JPO.packArgs(map));
            if (UIUtil.isNullOrEmpty(connId)) {
                DomainRelationship domainRelationship = newPart.addToObject(context, new RelationshipType(REL_JFECRRoot2Item), partId);
                HashMap attributeMap = new HashMap<>();
                attributeMap.put(ATTRIBUTE_JF_BOMQuantity, split[1]);
                attributeMap.put(ATTRIBUTE_JF_BOMChangeDes, split[0]);
                attributeMap.put(ATTRIBUTE_JF_ECRID, objectId);
                if (split[2] != "0") {
                    attributeMap.put(ATTRIBUTE_JF_BOMChangeQuantity, split[2]);
                }
//                if (split[0].contains(ecrChangeMessMap.get("Revise"))
//                        || split[0].contains(ecrChangeMessMap.get("ReviseRelease"))
//                        || split[0].contains(ecrChangeMessMap.get("VersionReplace"))
//                        || split[0].contains("数量")
//                        || split[0].contains(ecrChangeMessMap.get("AddNum"))
//                        || split[0].contains(ecrChangeMessMap.get("Delete"))
//                        || split[0].contains(ecrChangeMessMap.get("DelNum"))) {
                    if (!split[0].contains(ecrChangeMessMap.get("Add"))
                            && !split[0].contains(ecrChangeMessMap.get("AddRelease"))
                            && !split[0].contains(ecrChangeMessMap.get("UnChanged"))
                            && !split[0].contains("FirstRelease")) {
                    Map map1 = (Map) oldMapList.get(0);
                    attributeMap.put(ATTRIBUTE_JF_ChangeBeforeRev, UIUtil.getValue(map1, SELECT_ID));
                    attributeMap.put(ATTRIBUTE_JF_BOMBeforeQuantity, String.valueOf(oldMapList.size()));
                    _logger.info("!!!!!!!!!!!!!!!!!!比对有值:{}", oldMapList.size());
                    _logger.info("!!!!!!!!!!!!!!!!!!ATTRIBUTE_JF_BOMBeforeQuantity:{}", oldMapList.size());
                }
                domainRelationship.setAttributeValues(context, attributeMap);
                allChangePartSet.add(partId);
            } else {
                DomainRelationship domainRelationship = DomainRelationship.newInstance(context, connId);
                String attributeValue = domainRelationship.getAttributeValue(context, ATTRIBUTE_JF_ECRID);
                if (!attributeValue.contains(objectId)) {
                    attributeValue += "," + objectId;
                    domainRelationship.setAttributeValue(context, ATTRIBUTE_JF_ECRID, attributeValue);
                    allChangePartSet.add(partId);
                }
            }
        }
        for (String id : difference) {
            //需要根据id分组 可能会存在 逻辑id一样 id不一样的情况 对称差集
            List newInfoList = null;
            MapList newMapList = new MapList();
            if (oldPartChildGroupingMap.containsKey(id)) {
                newInfoList = (List) oldPartChildGroupingMap.get(id);
                newMapList.addAll(newInfoList);
                //根据id进行分组
                Map newMap = getMapListGroupingMap(context, newMapList, SELECT_ID);
                StringList newList = (StringList) newMapList.stream().map(m -> {
                    Map map = (Map) m;
                    return UIUtil.getValue(map, SELECT_ID);
                }).collect(Collectors.toCollection(StringList::new));
                setFirstLevelItemRelAndAttr(context, newMap, newList, ecrChangeMessMap.get("Delete"), ecrChangeMessMap, newPart, objectId, allChangePartSet);
            } else if (newPartChildGroupingMap.containsKey(id)) {
                newInfoList = (List) newPartChildGroupingMap.get(id);
                newMapList.addAll(newInfoList);
                Map newMap = getMapListGroupingMap(context, newMapList, SELECT_ID);
                StringList newList = (StringList) newMapList.stream().map(m -> {
                    Map map = (Map) m;
                    return UIUtil.getValue(map, SELECT_ID);
                }).collect(Collectors.toCollection(StringList::new));
                setFirstLevelItemRelAndAttr(context, newMap, newList, "", ecrChangeMessMap, newPart, objectId, allChangePartSet);
            }
        }
    }

    /**
    * 新增删除
    * @param context
	* @param newMap
	* @param newList
	* @param mess
	* @param ecrChangeMessMap
	* @param newPart
	* @param ecrId
	* @param allChangePartSet
    * @author LIUJR
    * @throws
    * @return void
    * @date 06/06/2025 11:00
    * @description
    */
    public static  void setFirstLevelItemRelAndAttr(Context context, Map newMap, StringList newList, String mess, Map<String, String> ecrChangeMessMap, DomainObject newPart, String ecrId, HashSet allChangePartSet) throws Exception{
        List newInfoList = null;
        MapList newMapList = new MapList();
        int quantity = 0;
        String result = EMPTY_STRING;
//        _logger.info("newList:{}", newList);
//        _logger.info("newMap:{}", newMap);
        for (int i = 0; i < newList.size(); i++) {
            String id = newList.get(i);
            newInfoList = (List) newMap.get(id);
            newMapList.addAll(newInfoList);
            quantity = -newInfoList.size();
            String partId = UIUtil.getValue((Map) newMapList.get(i), DomainConstants.SELECT_ID);
            if (UIUtil.isNotNullAndNotEmpty(mess)) {
                result = mess + "; " + ecrChangeMessMap.get("DelNum").replaceAll("1", String.valueOf(newInfoList.size())) + "|" + 0;
            } else {
                DomainObject partObject = DomainObject.newInstance(context, partId);
                String current = partObject.getInfo(context, SELECT_CURRENT);
                if ("FROZEN".equalsIgnoreCase(current)) {
                    result = ecrChangeMessMap.get("AddRelease") + "; " + ecrChangeMessMap.get("AddNum").replaceAll("1", String.valueOf(newInfoList.size()))  + "|" + newInfoList.size();
                } else {
                    result = ecrChangeMessMap.get("Add") + "; " + ecrChangeMessMap.get("AddNum").replaceAll("1", String.valueOf(newInfoList.size())) + "|" + newInfoList.size();
                }
                quantity = newInfoList.size();
            }
            if (UIUtil.isNullOrEmpty(result)) {
                continue;
            }
            String[] split = result.split("\\|");
            Map map = new HashMap<>();
            map.put("relName", REL_JFECRRoot2Item);
            map.put("fromId", newPart.getId(context));
            map.put("toId", partId);
            String connId = JF_PublicMethodClass_mxJPO.getTwoBusinessObjectConnId(context, JPO.packArgs(map));
            if (UIUtil.isNullOrEmpty(connId)) {
                DomainRelationship domainRelationship = newPart.addToObject(context, new RelationshipType(REL_JFECRRoot2Item), partId);
                HashMap attributeMap = new HashMap<>();
                attributeMap.put(ATTRIBUTE_JF_BOMQuantity, split[1]);
                attributeMap.put(ATTRIBUTE_JF_BOMChangeDes, split[0]);
                attributeMap.put(ATTRIBUTE_JF_ECRID, ecrId);
                attributeMap.put(ATTRIBUTE_JF_BOMChangeQuantity, String.valueOf(quantity));
                if (mess.equalsIgnoreCase(ecrChangeMessMap.get("Delete"))) {
                    _logger.info("!!!!!!!!!!!!!!!!!!mess:{}", mess);
                    _logger.info("!!!!!!!!!!!!!!!!!!ATTRIBUTE_JF_BOMBeforeQuantity:{}", newInfoList.size());
                    attributeMap.put(ATTRIBUTE_JF_BOMBeforeQuantity, String.valueOf(newInfoList.size()));
                    attributeMap.put(ATTRIBUTE_JF_ChangeBeforeRev, id);
                }
                domainRelationship.setAttributeValues(context, attributeMap);
                allChangePartSet.add(partId);
            } else {
                DomainRelationship domainRelationship = DomainRelationship.newInstance(context, connId);
                String attributeValue = domainRelationship.getAttributeValue(context, ATTRIBUTE_JF_ECRID);
                if (!attributeValue.contains(ecrId)) {
                    attributeValue += "," + ecrId;
                    domainRelationship.setAttributeValue(context, ATTRIBUTE_JF_ECRID, attributeValue);
                    allChangePartSet.add(partId);
                }
            }
            if (result.contains(ecrChangeMessMap.get("Add")) || result.contains(ecrChangeMessMap.get("AddRelease"))) {
//                _logger.info("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
//                _logger.info("result:{}", result);
                //如果是新增子级也需要
                getFirstPartStructures(context, DomainObject.newInstance(context, partId), ecrChangeMessMap.get("Add"), ecrChangeMessMap.get("AddNum"), ecrChangeMessMap.get("AddRelease"), allChangePartSet, ecrId);
            }
        }
    }

    /**
     * 将MapList 进行特殊字段的分组 并组成Map返回
     *
     * @param context
     * @param mapList
     * @param groupAttr
     * @return java.util.Map
     * @throws
     * @author LIUJR
     * @date 2025/4/23 9:30
     * @description
     */
    public static Map getMapListGroupingMap(Context context, MapList mapList, String groupAttr) throws Exception {
        Map groupMap = (Map) mapList.stream().collect(Collectors.groupingBy(m -> {
            Map map = (Map) m;
            return map.get(groupAttr);
        }));
        return groupMap;
    }

    /**
     * 求交集结果   中的升版数据  和新增删除数据
     *
     * @param context
     * @param oldMapList
     * @param newMapList
     * @param ecrChangeMessMap
     * @return java.lang.String
     * @throws
     * @author LIUJR
     * @date 2025/4/25 14:23
     * @description
     */
    public static String getChangeResultsMap(Context context, MapList oldMapList, MapList newMapList, Map<String, String> ecrChangeMessMap, String objectId, HashSet allChangePartSet) throws Exception {
        String result = DomainConstants.EMPTY_STRING;
        String quantity = "0";
        /*
         * 逻辑id相同 ：
         * 1. 数量相同 比较版本
         * 2. 数量不同 比较版本 和新增
         * 2. 数量不同 比较新增和减少
         *
         * */
        if (oldMapList.size() == newMapList.size()) {
            Map oldMap = (Map) oldMapList.get(0);
            Map newMap = (Map) newMapList.get(0);
            if (!UIUtil.getValue(oldMap, DomainConstants.SELECT_REVISION)
                    .equalsIgnoreCase(UIUtil.getValue(newMap, DomainConstants.SELECT_REVISION))) {
                String current = UIUtil.getValue(newMap, SELECT_CURRENT);
                String mess = "FROZEN".equalsIgnoreCase(current) ? "ReviseRelease" : "RELEASED".equalsIgnoreCase(current) ? "VersionReplace" : "Revise";
                result = ecrChangeMessMap.get(mess) + "|" + newMapList.size() + "|0" ;
                //调用递归
                String oldId = UIUtil.getValue(oldMap, DomainConstants.SELECT_ID);
                String newId = UIUtil.getValue(newMap, DomainConstants.SELECT_ID);
                getPartCompareStructures(context, DomainObject.newInstance(context, oldId), DomainObject.newInstance(context, newId), ecrChangeMessMap, objectId, allChangePartSet);
//                _logger.info("升版：result", result);
            }
        } else {
            //将old和new 进行id分组 拿取id的交集和并集
            //如果交集有值  并集没有值  就是新增
            Map oldMap = getMapListGroupingMap(context, oldMapList, SELECT_ID);
            Map newMap = getMapListGroupingMap(context, newMapList, SELECT_ID);
            StringList oldList = (StringList) oldMapList.stream().map(m -> {
                Map map = (Map) m;
                return UIUtil.getValue(map, SELECT_ID);
            }).collect(Collectors.toCollection(StringList::new));
            StringList newList = (StringList) newMapList.stream().map(m -> {
                Map map = (Map) m;
                return UIUtil.getValue(map, SELECT_ID);
            }).collect(Collectors.toCollection(StringList::new));
            // 求交集
            StringList intersection = oldList.stream()
                    .filter(newList::contains)
                    .collect(Collectors.toCollection(StringList::new));
//            _logger.info("id交集: {}", intersection);
            // 求对称差集
            HashSet differenceSet = Stream.concat(
                    oldList.stream().filter(e -> !newList.contains(e)),
                    newList.stream().filter(e -> !oldList.contains(e))
            ).collect(Collectors.toCollection(HashSet::new));
            StringList difference = StringList.create(differenceSet);
//            _logger.info("id对称差集: {}", difference);
            if (!intersection.isEmpty()) {
                //如果交集有值  对称差集没有值  就是新增
                int size = oldMapList.size() - newMapList.size();
                if (size > 0) {
                    result = ecrChangeMessMap.get("DelNum");
                    quantity = "-" + Math.abs(size);
                } else {
                    result = ecrChangeMessMap.get("AddNum");
                    quantity = "" + Math.abs(size);
                }
                result = result.replaceAll("1", String.valueOf(Math.abs(size)));
                result +=  "|" + newMapList.size() +  "|" + quantity;
//                _logger.info("交集数量变更：result", result);
            }
            String delNum = ecrChangeMessMap.get("DelNum");
            String addNum = ecrChangeMessMap.get("AddNum");
            if (!difference.isEmpty()) {
                if (difference.size() != 2) {
                    //只有一个有的情况
                    List infoList;
                    MapList mapList = new MapList();
                    if (oldMap.containsKey(difference.get(0))) {
                        infoList = (List) oldMap.get(difference.get(0));
                        quantity = "-" + infoList.size();
                        result = ecrChangeMessMap.get("Delete")  + "; " + delNum.replaceAll("1", quantity) + "|" + infoList.size() + "|" + quantity;
                    } else if (newMap.containsKey(difference.get(0))) {
                        infoList = (List) newMap.get(difference.get(0));
                        mapList.addAll(infoList);
                        String partId = UIUtil.getValue((Map) mapList.get(0), DomainConstants.SELECT_ID);
                        DomainObject partObject = DomainObject.newInstance(context, partId);
                        String current = partObject.getInfo(context, SELECT_CURRENT);
                        if ("FROZEN".equalsIgnoreCase(current)) {
                            result = ecrChangeMessMap.get("AddRelease") + "; " + addNum.replaceAll("1", String.valueOf(infoList.size())) + "|" + infoList.size() + "|" + infoList.size();
                        } else {
                            result = ecrChangeMessMap.get("Add") + "; " + addNum.replaceAll("1", String.valueOf(infoList.size())) + "|" + infoList.size() + "|" + infoList.size();
                        }
                        if (result.contains(ecrChangeMessMap.get("Add")) || result.contains(ecrChangeMessMap.get("AddRelease"))) {
//                            _logger.info("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
//                            _logger.info("result:{}", result);
                            //如果是新增子级也需要
                            getFirstPartStructures(context, DomainObject.newInstance(context, partId), ecrChangeMessMap.get("Add"), addNum, ecrChangeMessMap.get("AddRelease"), allChangePartSet, objectId);
                        }
                    }
                } else {
                    List oldInfoList;
                    List newInfoList;
                    if (oldMap.containsKey(difference.get(0))) {
                        oldInfoList = (List) oldMap.get(difference.get(0));
                        newInfoList = (List) newMap.get(difference.get(1));
                    } else {
                        oldInfoList = (List) oldMap.get(difference.get(1));
                        newInfoList = (List) newMap.get(difference.get(0));
                    }
//                    _logger.info("判断版本:.。。。。。。。。。。。。。");
//                    _logger.info("oldInfoList:{}", oldInfoList.size());
//                    _logger.info("newInfoList:{}", newInfoList.size());
                    //判断版本
                    MapList oldMapList1 = new MapList();
                    oldMapList1.addAll(oldInfoList);
                    MapList newMapList1 = new MapList();
                    newMapList1.addAll(newInfoList);
                    Map oldMap1 = (Map) oldMapList.get(0);
                    Map newMap1 = (Map) newMapList.get(0);
                    int size = oldMapList1.size() - newMapList1.size();
                    if (!UIUtil.getValue(oldMap1, DomainConstants.SELECT_REVISION)
                            .equalsIgnoreCase(UIUtil.getValue(newMap1, DomainConstants.SELECT_REVISION))) {
                        String current = UIUtil.getValue(newMap1, SELECT_CURRENT);
                        String mess = "FROZEN".equalsIgnoreCase(current) ? "ReviseRelease" : "RELEASED".equalsIgnoreCase(current) ? "VersionReplace" : "Revise";
                        result = ecrChangeMessMap.get(mess);
                        if (size > 0) {
                            if (result.length() > 0) {
                                quantity = "-" + Math.abs(size);
                                result += "; " + delNum.replaceAll("1", String.valueOf(Math.abs(size))) + "|" + newMapList.size();
                            }
                        } else {
                            quantity = "" + Math.abs(size);
                            result += "; " + addNum.replaceAll("1", String.valueOf(Math.abs(size))) + "|" + newMapList.size();
                        }
                        result += "|" + quantity;
//                        _logger.info("升版数量变更：result", result);
                        //调用递归
                        String oldId = UIUtil.getValue(oldMap1, DomainConstants.SELECT_ID);
                        String newId = UIUtil.getValue(newMap1, DomainConstants.SELECT_ID);
                        getPartCompareStructures(context, DomainObject.newInstance(context, oldId), DomainObject.newInstance(context, newId), ecrChangeMessMap, objectId, allChangePartSet);
                    } else {
                        if (size > 0) {
                            quantity = "-" + Math.abs(size);
                            result = ecrChangeMessMap.get("DelNum");
                        } else {
                            result = ecrChangeMessMap.get("AddNum");
                            quantity = "" + Math.abs(size);
                        }
                        result = result.replaceAll("1", String.valueOf(Math.abs(size)));
                        result += "|" + newMapList.size() + "|" + quantity;
//                        _logger.info("数量变更：result", result);
                    }
                }
            }
        }
//        _logger.info("result:{}", result);
        return result;
    }
    /**
    *
    *@description 设置游离一级件的编辑全为false
    *@param itemMap 根节点map
	*@param startIndex  根节点下标
	*@param endIndex  结束下标
	*@param dataMapList
    *@return void
    *@throws
    *@author CHENYAN
    *@date 2025/5/15 10:55
    */
    public void setFreeStateEditFlagByMapList(Map itemMap ,int startIndex,int endIndex ,MapList dataMapList,Set<Integer> filterFreeIndex){
        String strFreeState = (String) itemMap.get(SELECT_ATTR_JFFREEState);
        String strType = (String) itemMap.get(SELECT_TYPE);
        String strProcurementType = (String) itemMap.get(SELECT_ATTR_JF_ProcurementType);
        String strRelName = (String) itemMap.get("relationship");
//        _logger.info("itemMap:{}",itemMap);
        boolean isFilter = (!"make".equals(strProcurementType)) && TYPE_VPMReference.equals(strType);
        //必须是根节点
        if (REL_JFECRRelateRoot.equals(strRelName)){
            if ("Y".equals(strFreeState)) {
                for (; startIndex < endIndex; startIndex++) {
                    Map tempMap = (Map) dataMapList.get(startIndex);
                    tempMap.put("freeFlag","1");
                    if (isFilter && Objects.nonNull(filterFreeIndex)){
                        filterFreeIndex.add(startIndex);
                    }
                }
            }
        }
    }

    /**
     * @param context
     * @param args
     * @return com.matrixone.apps.domain.util.MapList
     * @throws
     * @description 获取采购件清单数据
     * @author CHENYAN
     * @date 2025/4/29 14:44
     */
        public MapList getNewECRBuyTableData(Context context, String[] args) throws Exception {
        _logger.info("----------------------------- getNewECRBuyTableData begin ------------------------------------------------");
        Map parameters = JPO.unpackArgs(args);
        String strObjectId = (String) parameters.get("objectId");
        DomainObject ecr = DomainObject.newInstance(context, strObjectId);
        String strName = ecr.getInfo(context, SELECT_NAME);
        StringList relSelectList = new StringList(SELECT_RELATIONSHIP_ID);
        relSelectList.add(SELECT_FROM_ID);
        relSelectList.add(SELECT_ATTR_JFFREEState);
        relSelectList.add(SELECT_ATTRIBUTE_JF_BOMQuantity);
        relSelectList.add(SELECT_ATTRIBUTE_JF_BOMChangeQuantity);
        relSelectList.add(SELECT_ATTRIBUTE_JF_ChangeBeforeRev);
        relSelectList.add(SELECT_ATTRIBUTE_JF_BOMChangeDes);
        relSelectList.addAll(getPriceAttrListByTableName("JFNewECRCosting",strObjectId,TYPE_JFNewECR));
        StringList typeSelectList = JF_Util_mxJPO.basicBolistSel();
        typeSelectList.add(JF_PublicMethodClass_mxJPO.buildStringInStrings("to[JFRelateItem|from.id==",strObjectId,"].attribute[JFChangeSource]"));
        typeSelectList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_V_PART_NUMBER);
        typeSelectList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JFDIRECT_BUY);
        typeSelectList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JFPartType);
        typeSelectList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JFConnectECR);
        typeSelectList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_V_isLastVersion);
        typeSelectList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JF_ProcurementType);
        typeSelectList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_V_PART_NUMBER);
        typeSelectList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JF_PartNameCN);
        typeSelectList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JF_PartNameEN);

        MapList newECRAffectItemList = null ;
        newECRAffectItemList = getNewECRAffectItemTableData(context, ecr, typeSelectList, relSelectList,strObjectId);
        HashSet<Integer> removeIndexSet = new HashSet<>();
        for (int i = 0; i < newECRAffectItemList.size();) {
            int nextLevel = findEndOfSubtree(newECRAffectItemList, i); // 找到以i为根的子树结束位置
            // add by chenyan 2025/05/15 设置游离一级件编辑权限标识
            Map itemMap = (Map) newECRAffectItemList.get(i);
            setFreeStateEditFlagByMapList(itemMap,i,nextLevel,newECRAffectItemList,null);
            recursionFilterTree(newECRAffectItemList, i, nextLevel, removeIndexSet);
            i = nextLevel;
        }
        // 转换为 ArrayList
        List<Integer> indexList = new ArrayList<>(removeIndexSet);
        // 倒序排序
        Collections.sort(indexList, Collections.reverseOrder());
        for (Integer index : indexList) {
            newECRAffectItemList.remove(index.intValue());
        }
        //  先确定行能不能编辑 然后再确定列能不能编辑
        for (int i = 0; i < newECRAffectItemList.size();) {
            int nextLevel = findEndOfSubtree(newECRAffectItemList, i); // 找到以i为根的子树结束位置
            // add by chenyan 2025/05/15 设置游离一级件编辑权限标识
            Map partMap = (Map) newECRAffectItemList.get(i);
            String strType = (String)partMap.get(SELECT_TYPE);
            String strProcurementType = (String)partMap.get(SELECT_ATTR_JF_ProcurementType);
            if ((!"make".equalsIgnoreCase(strProcurementType)) && TYPE_VPMReference.equals(strType)){
//                if ((!partMap.containsKey("freeFlag"))) {
//                    partMap.put("rowEditFlag","1");
//                    i = nextLevel;
//                }else {
//                    i++;
//                }
                partMap.put("rowEditFlag","1");
                i = nextLevel;
            }else {
                i++;
            }
        }


//        _logger.info("newECRAffectItemList before :{}",newECRAffectItemList);
        //重排结构
        newECRAffectItemList = rearrangeStructureByFreeState(newECRAffectItemList);
//        _logger.info("newECRAffectItemList after :{}",newECRAffectItemList);
        _logger.info("----------------------------- getNewECRBuyTableData end ------------------------------------------------");
        return newECRAffectItemList;
    }

    /**
     * @param newECRAffectItemList 全部数据集合
     * @param startIndex           当前节点下标
     * @param endIndex             结束下标
     * @param removeIndexSet       保存移除下标集合
     * @return java.lang.Boolean
     * @throws
     * @description 递归判断当前节点是否需要过滤
     * @author CHENYAN
     * @date 2025/4/29 12:12
     */
    public Boolean recursionFilterTree(MapList newECRAffectItemList, int startIndex, int endIndex, Set<Integer> removeIndexSet) {
        Boolean isShow = Boolean.FALSE;
        Map partMap = (Map) newECRAffectItemList.get(startIndex);
        String strLevel = (String) partMap.get(SELECT_LEVEL);
        Integer currentLevel = Integer.parseInt(strLevel);
        //叶子结点标识
//            String strLeafNodeFlag = (String) partMap.get("from[JFECRRoot2Item]");
        //已经是叶子结点
//            if ("FALSE".equalsIgnoreCase(strLeafNodeFlag)){
//
//            }

        // 查找直接子节点范围 [startIndex+1, endIndex)
        for (int i = startIndex + 1; i < endIndex; i++) {
            Map childNode = (Map) newECRAffectItemList.get(i);
            Integer childLevel = Integer.parseInt((String) childNode.get(SELECT_LEVEL));

            if (childLevel > currentLevel) {
                // 下一层级 -> 子节点
                boolean childIsShow = recursionFilterTree(newECRAffectItemList, i, endIndex, removeIndexSet);
                if (childIsShow) {
                    isShow = true; // 任意子节点显示，则父节点也要显示
                }
            } else {
                // 当前节点之后没有子节点了，跳出
                break;
            }
        }
        // 如果不是叶子节点，还需要看自己的 ProcurementType
        String strProcurementType = (String) partMap.get(SELECT_ATTR_JF_ProcurementType);
        if (!"make".equalsIgnoreCase(strProcurementType)) {
            isShow = true;
        }
        // 设置 showFlag
        partMap.put("showFlag", isShow);
        // 记录需要移除的下标
        if (!isShow) {
            removeIndexSet.add(startIndex);
        }
        return isShow;
    }

    /**
     * @param partList 零件集合
     * @param startIdx 当前节点下标
     * @return int
     * @throws
     * @description 获取当前节点树的边界 用于减少重复遍历
     * @author CHENYAN
     * @date 2025/4/29 13:09
     */
    public static int findEndOfSubtree(MapList partList, int startIdx) {
        Map partMap = (Map) partList.get(startIdx);
        int level = Integer.parseInt((String) partMap.get(SELECT_LEVEL));
        int endIdx = startIdx + 1;
        int size = partList.size();
        while (endIdx < size) {
            Map endPartMap = (Map) partList.get(endIdx);
            int nextLevel = Integer.parseInt((String) endPartMap.get(SELECT_LEVEL));
            if (nextLevel <= level) {
                break;
            }
            endIdx++;
        }
        return endIdx;
    }
    /**
    *
    *@description 获取自制件清单数据
    *@param context
	*@param args
    *@return com.matrixone.apps.domain.util.MapList
    *@throws
    *@author CHENYAN
    *@date 2025/5/8 14:02
    */
    public MapList getNewECRMakeTableData(Context context, String[] args) throws Exception {
        _logger.info("----------------------------- getNewECRMakeTableData begin ------------------------------------------------");
        Map parameters = JPO.unpackArgs(args);
        _logger.info("parameters:{}", parameters);
        String strObjectId = (String) parameters.get("objectId");
        _logger.info("strObjectId:{}", strObjectId);
        String strExpandLevel = (String) parameters.get("expandLevel");
        short nExpandLevel = ProgramCentralUtil.getExpandLevel(strExpandLevel);
        _logger.info("strExpandLevel:{}", strExpandLevel);
        _logger.info("nExpandLevel:{}", nExpandLevel);
        DomainObject ecr = DomainObject.newInstance(context, strObjectId);
        String strLoginUser = context.getUser();
        //获取ECR关联项目
        String strProjectId = ecr.getInfo(context, "from[JFChange2Project].to.id");
        String strCurrent = ecr.getInfo(context, SELECT_CURRENT);
        StringList reSelectList = new StringList(SELECT_RELATIONSHIP_ID);
        StringList typeSelectList = new StringList(SELECT_ID);
        typeSelectList.add(SELECT_TYPE);
        typeSelectList.add(SELECT_REVISION);
        typeSelectList.add(SELECT_CURRENT);
        typeSelectList.add(SELECT_NAME);
        typeSelectList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_V_PART_NUMBER);
        typeSelectList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JFDIRECT_BUY);
        typeSelectList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JFPartType);
        typeSelectList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JFConnectECR);
        typeSelectList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JF_ProcurementType);
        typeSelectList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_V_PART_NUMBER);
        typeSelectList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JF_PartNameCN);
        typeSelectList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JF_PartNameEN);
        typeSelectList.add(JF_PublicMethodClass_mxJPO.buildStringInStrings("to[JFRelateItem|from.id==",strObjectId,"].attribute[JFChangeSource]"));
        reSelectList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JFECRName);
        reSelectList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JFWholeChair);
        reSelectList.add(SELECT_FROM_ID);
        reSelectList.add(SELECT_ATTR_JFFREEState);
        reSelectList.add(SELECT_RELATIONSHIP_ID);
        reSelectList.add(SELECT_ATTRIBUTE_JF_BOMQuantity);
        reSelectList.add(SELECT_ATTRIBUTE_JF_BOMChangeQuantity);
        reSelectList.add(SELECT_ATTRIBUTE_JF_ChangeBeforeRev);
        reSelectList.add(SELECT_ATTRIBUTE_JF_BOMChangeDes);
        reSelectList.addAll(getPriceAttrListByTableName("JFNewECRController",strObjectId,TYPE_JFNewECR));
        MapList res = getNewECRAffectItemTableData(context, ecr, typeSelectList, reSelectList,strObjectId);
        HashSet<Integer> filterFreeSet = new HashSet<>();
        //注释标识游离件树
        for (int i = 0; i < res.size();) {
            int nextLevel = findEndOfSubtree(res, i); // 找到以i为根的子树结束位置
            // add by chenyan 2025/05/15 设置游离一级件编辑权限标识
            Map itemMap = (Map) res.get(i);
            setFreeStateEditFlagByMapList(itemMap,i,nextLevel,res,filterFreeSet);
            i = nextLevel;
        }
        // 转换为 ArrayList
        _logger.info("filterFreeSet:{}",filterFreeSet);
        List<Integer> indexList = new ArrayList<>(filterFreeSet);
        // 倒序排序
        Collections.sort(indexList, Collections.reverseOrder());
        for (Integer index : indexList) {
            res.remove(index.intValue());
        }
        boolean isReject = false ;
        if ("APR".equals(strCurrent)){
            boolean isAPR = false ;

            StringList APROwnerList = ecr.getInfoList(context, "from[JFECR2Task|to.type==JF_APRTask].to.owner");
            _logger.info("APROwnerList:{}",APROwnerList);
            if (APROwnerList.size() > 0){
                String strAPROwner = APROwnerList.get(0);
                if (UIUtil.isNotNullAndNotEmpty(strAPROwner)){
                    String[] split = strAPROwner.split("=");
                    if (split.length > 1){
                        String strAPRTaskOwner = split[1];
                        strAPRTaskOwner = strAPRTaskOwner.trim();
                        if (strAPRTaskOwner.equals(strLoginUser)){
                            isAPR = true;
                        }
                    }
                }
            }
            if (!isAPR){
                MapList maps = ecr.getRelatedObjects(context, RELATIONSHIP_JF_ECR_TASK , // relationship pattern
                        TYPE_JS_SIGN_TASK,                                    // object pattern
                        JF_Util_mxJPO.basicBolistSel(),                            // object selects
                        JF_Util_mxJPO.basicRellistSel(), // relationship selects
                        false,                                        // to direction
                        true,                                        // from direction
                        (short) 1,                                    // recursion level
                        "current==Active",                // object where clause
                        "",
                        (short) 0);
                isReject = maps.size() > 0 ;
            }
        }
        //会签编辑逻辑
//        if ("Countersign".equals(strCurrent)){
            //确定可以编辑的行
            DomainObject part = DomainObject.newInstance(context);
            for (int i = 0; i < res.size();) {
                Map partInfo = (Map) res.get(i);
                String strPartId = (String)partInfo.get(SELECT_ID);
                part.setId(strPartId);
                String strPartType = UIUtil.getValue(partInfo, SELECT_ATTR_JFPartType);
                String strRelationship = UIUtil.getValue(partInfo, RELATIONSHIP);
                String strProcurementType = UIUtil.getValue(partInfo, SELECT_ATTR_JF_ProcurementType);
                int nextLevel = findEndOfSubtree(res, i); // 找到以i为根的子树结束位置
                if (REL_JFECRRelateRoot.equalsIgnoreCase(strRelationship)){
                    String strFreeFlag = UIUtil.getValue(partInfo, "freeFlag");
                    //游离非GU发泡件只能根节点行可以编辑，发泡件及其子集 行可以编辑
                    // rowEditFlag 1 代表 自制件工时变更 自制件成本变化 能编辑
                    // rowEditFlag 2 代表 采购模具费变更 单价变更(成本) 模具费变更(成本) 能编辑
                    if ("1".equals(strFreeFlag)){
                        //发泡下面的所有子集行可以编辑
                        if (ATTR_JF_ProcurementType_RANGE_MAKE.equals(strProcurementType)){
//                            if ("U".equals(strPartType)){
//                                for (int i1 = i + 1; i1 < nextLevel; i1++) {
//                                    Map sunPartInfo = (Map)res.get(i1);
//                                    String strSunProcurementType = UIUtil.getValue(sunPartInfo, SELECT_ATTR_JF_ProcurementType);
//                                    if (ATTR_JF_ProcurementType_RANGE_MAKE.equals(strSunProcurementType)){
//                                        sunPartInfo.put("rowEditFlag","2");
//                                    }
//                                }
//                            }
                            boolean isZeroPart =  false;
                            isZeroPart = checkPartIsZeroPart(context, strPartId, strProjectId);
                            if(isZeroPart || "T".equals(strPartType)||"U".equals(strPartType)){
                                partInfo.put("rowEditFlag","1");
                            }
                        }

                    }else {
                        //非游离件
                        //如果是供货件 一级件是面套发泡 可以编辑 如果是发泡的话下面的所有子集也可以编辑
                        boolean isZeroPart =  false;
                        isZeroPart = checkPartIsZeroPart(context, strPartId, strProjectId);
                        partInfo.put("isZeroPart",isZeroPart);
                        if (isZeroPart){
                            //供货件是GU的话下面的所有子集都能编辑
                            if ("U".equals(strPartType)){
                                for (int i1 = i + 1; i1 < nextLevel;i1++) {
                                    Map sunPartInfo = (Map)res.get(i1);
                                    String strSunProcurementType = UIUtil.getValue(sunPartInfo, SELECT_ATTR_JF_ProcurementType);
                                    String strSunPartType = UIUtil.getValue(sunPartInfo, SELECT_ATTR_JFPartType);
                                    String strLevel = UIUtil.getValue(sunPartInfo, SELECT_LEVEL);
                                    if (ATTR_JF_ProcurementType_RANGE_MAKE.equals(strSunProcurementType)){
                                        //发泡供货件下面的面套一级件可以编辑
                                        if ("T".equals(strSunPartType)){
                                            sunPartInfo.put("rowEditFlag","1");
                                        }
                                    }
                                }

                            }else {
                                for (int i1 = i + 1; i1 < nextLevel;) {
                                    Map sunPartInfo = (Map)res.get(i1);
                                    String strLevel = UIUtil.getValue(sunPartInfo, SELECT_LEVEL);
                                    String strSunPartType = UIUtil.getValue(sunPartInfo, SELECT_ATTR_JFPartType);
                                    String strSunProcurementType = UIUtil.getValue(partInfo, SELECT_ATTR_JF_ProcurementType);

                                    int nextSunLevel = findEndOfSubtree(res, i1); // 找到以i为根的子树结束位置
                                    if ("2".equals(strLevel)){
                                        //一级件发泡，下面的子集都可以编辑
                                        if ("U".equals(strSunPartType)){
                                            if (ATTR_JF_ProcurementType_RANGE_MAKE.equals(strSunProcurementType)){
                                                sunPartInfo.put("rowEditFlag","1");
                                            }
                                            //父级不能是GT
                                        }else if ("T".equals(strSunPartType) && (!"T".equals(strPartType))){
                                            if (ATTR_JF_ProcurementType_RANGE_MAKE.equals(strSunProcurementType)){
                                                sunPartInfo.put("rowEditFlag","1");
                                            }
                                        }
                                    }
                                    if (i1 != nextSunLevel){
                                        i1 = nextSunLevel;
                                    }else {
                                        i1++ ;
                                    }

                                }
                            }
                            if (ATTR_JF_ProcurementType_RANGE_MAKE.equals(strProcurementType)){
                                partInfo.put("rowEditFlag","1");
                            }
                        }else {
                            //非供货件 GU 及其子集可以编辑 GT 根节点可以编辑
                            if ("U".equals(strPartType)){
                                if (ATTR_JF_ProcurementType_RANGE_MAKE.equals(strProcurementType)){
                                    partInfo.put("rowEditFlag","1");
                                }

                            }else if ("T".equals(strPartType)){
                                if (ATTR_JF_ProcurementType_RANGE_MAKE.equals(strProcurementType)){
                                    partInfo.put("rowEditFlag","1");
                                }
                            }
                        }

                    }
                }
                if (i != nextLevel){
                    i = nextLevel;
                }else {
                    i++ ;
                }

            }
            //设置采购和costing可以编辑的行
            for (int i = 0; i < res.size(); i++) {
                Map partInfo = (Map) res.get(i);
                String strProcurementType = UIUtil.getValue(partInfo, SELECT_ATTR_JF_ProcurementType);
                if (ATTR_JF_ProcurementType_RANGE_MAKE.equals(strProcurementType)){
                    String strRowEditFlag = (String) partInfo.get("rowEditFlag");
                    if (UIUtil.isNotNullAndNotEmpty(strRowEditFlag)){
                        partInfo.put("rowEditFlag",JF_PublicMethodClass_mxJPO.buildStringInStrings(strRowEditFlag,",2"));
                    }else {
                        partInfo.put("rowEditFlag","2");
                    }
                }
            }
//        }else if ("APR".equals(strCurrent)){
//            for (int i = 0; i < res.size();i++) {
//                Map partInfo = (Map) res.get(i);
//                String strPartId = (String) partInfo.get(SELECT_ID);
//                String strPartType = UIUtil.getValue(partInfo, SELECT_ATTR_JFPartType);
//                String strProcurementType = UIUtil.getValue(partInfo, SELECT_ATTR_JF_ProcurementType);
//                String strRelationship = UIUtil.getValue(partInfo, RELATIONSHIP);
//                if (JF_PLMConstants_mxJPO.REL_JFECRRelateRoot.equals(strRelationship)) {
////                    String strFreeFlag = UIUtil.getValue(partInfo, "freeFlag");
//                    if (!partInfo.containsKey("freeFlag")){
//                        boolean isZeroPart = checkPartIsZeroPart(context, strPartId, strProjectId);
//                        if ( isZeroPart && ATTR_JF_ProcurementType_RANGE_MAKE.equals(strProcurementType)){
//                            partInfo.put("rowEditFlag","1");
//                        }
//                    }
//
//                }
//            }
//        }


        res = rearrangeStructureByFreeState(res);
        _logger.info("----------------------------- getNewECRMakeTableData end ------------------------------------------------");
        return res;
    }

    /**
    *
    *@description ECR受影响项通用方法
    *@param context
	*@param newECR 新ECR对象
	*@param typeSelectList 
	*@param relSelectList
    *@return com.matrixone.apps.domain.util.MapList
    *@throws
    *@author CHENYAN
    *@date 2025/5/9 10:50
    */
    public MapList getNewECRAffectItemTableData(Context context, DomainObject newECR, StringList typeSelectList, StringList relSelectList,String strECRId) throws Exception {
        String strWhere = "attribute[JF_ECRID]~~*" + strECRId + "*";
        MapList maps = null ;
        try {
            ContextUtil.pushContext(context);
            maps = newECR.getRelatedObjects(context, REL_JFECRRelateRoot + "," + REL_JFECRRoot2Item, // relationship pattern
                    TYPE_VPMReference,                                    // object pattern
                    typeSelectList,                            // object selects
                    relSelectList, // relationship selects
                    false,                                        // to direction
                    true,                                        // from direction
                    (short) 0,                                    // recursion level
                    "",                // object where clause
                    strWhere,
                    (short) 0);
        } finally {
            ContextUtil.popContext(context);
        }

//        MapList filterList = (MapList) maps.stream().filter(m -> {
//            Map partMap = (Map) m;
//            String strECRIds = (String) partMap.get(SELECT_ATTR_JFECRID);
//            return strECRIds.contains(strECRId);
//        }).collect(Collectors.toCollection(MapList::new));
        return maps;
    }


    /**
     * @param context
     * @param args
     * @return StringList 返回列编辑权限
     * @throws
     * @description 获取cost table 列的编辑权限
     * @author CHENYAN
     * @date 2024/7/29 10:17
     */
    public StringList getJFNewECRAffectedItemsCostTableEditAccess(Context context, String[] args) throws Exception {
        _logger.info("-------------------------------------------- getJFNewECRAffectedItemsCostTableEditAccess begin -----------------------------------------------------------");
        StringList res = new StringList();
        Map tableSettingMap = JPO.unpackArgs(args);
        MapList objectList = (MapList) tableSettingMap.get("objectList");
        Map columnMap = (Map) tableSettingMap.get("columnMap");
        String strFieldName = (String) columnMap.get("name");
        Map requestMap = (Map) tableSettingMap.get("requestMap");
        String strECRId = (String) requestMap.get("objectId");
        _logger.info("size:{}", res.size());
        MapList roleList = JF_PublicMethodClass_mxJPO.getLoginUserSignTaskRoleByECR(context, new String[]{strECRId});
//        _logger.info("objectList：{}", objectList);
        _logger.info("roleList：{}", roleList);
        _logger.info("strFieldName：{}", strFieldName);
        //不可编辑的id集合

        Set notEditAccessIdSet = new HashSet<String>();
        for (int i = 0; i < objectList.size();i++) {
            Map partMap = (Map) objectList.get(i);
            String strProcurementType = (String)partMap.get(SELECT_ATTR_JF_ProcurementType);
            String strPartId = (String)partMap.get(SELECT_ID);
            String strIsEdit = "false" ;
                //第一次出现ICO和buy才可编辑
//                if ((!"make".equalsIgnoreCase(strProcurementType)) && TYPE_VPMReference.equals(strType)){
//                    //  根据children获取到该节点的所有子集节点
//                    Set<String> sunNodeIdSet = getSunNodeIdByRootMap(partMap,"");
//                    notEditAccessIdSet.addAll(sunNodeIdSet);
//                    Boolean isEdit = getFieldEditAccessByChangeSourceAndRoleList(roleList, strChangeSource, strFieldName, strDirectBuy, strProcurementType, strPartType, true);
//                    if ((!partMap.containsKey("freeFlag"))) {
//                        strIsEdit =  isEdit.toString();
//                    }
//                }
            if (partMap.containsKey("rowEditFlag")){
                String strChangeSource = (String)partMap.get("to[JFRelateItem].attribute[JFChangeSource]");
                _logger.info("strChangeSource:{}",strChangeSource);
                //设置默认值
                if (UIUtil.isNullOrEmpty(strChangeSource)){
                    strChangeSource = ATTR_JFCHANGESOURCE_RANGE_BOTH;
                }
                String strDirectBuy = (String)partMap.get(SELECT_ATTR_JFDIRECT_BUY);
                String strPartType = (String)partMap.get(SELECT_ATTR_JFPartType);
                //  根据children获取到该节点的所有子集节点
                Boolean isEdit = getFieldEditAccessByChangeSourceAndRoleList(roleList, strChangeSource, strFieldName, strDirectBuy, strProcurementType, strPartType, true,"");
//                if ((!partMap.containsKey("freeFlag"))) {
//                    strIsEdit =  isEdit.toString();
//                }
                strIsEdit =  isEdit.toString();
            }
            res.add(strIsEdit);
        }
//        for (int i = 0; i < objectList.size(); i++) {
//            Map partMap = (Map) objectList.get(i);
//            String strIsEdit = "false" ;
//            if ((!partMap.containsKey("freeFlag")) &&partMap.containsKey("isEdit")) {
//                strIsEdit =  (String) partMap.get("isEdit");
//            }
//            res.add(strIsEdit);
//        }
//        res = getTableEditAccessInLoginRole(context, objectList, roleList, strECRId, new StringList(), strFieldName);
        _logger.info("res:{}", res);
        _logger.info("-------------------------------------------- getJFNewECRAffectedItemsCostTableEditAccess end -----------------------------------------------------------");

        return res;
    }

    /**
    *
    *@description 获取节点下面的所有子节点id
    *@param partMap
    *@param strFilterPartType 指定零件类型
    *@return java.util.Set<java.lang.String>
    *@throws
    *@author CHENYAN
    *@date 2025/5/21 16:52
    */

    public static Set<String> getSunNodeIdByRootMap(Map partMap,String strFilterPartType,int beginIndex,MapList dataMapList){
        Set sunIdSet = new HashSet<String>();
        if (partMap.containsKey("children")) {
            List sunList = (List) partMap.get("children");
            for (int i = 0; i < sunList.size(); i++) {
                Map sunMap = (Map) sunList.get(i);
                String strPartId = (String) sunMap.get(SELECT_ID);
                String strPartType = (String) sunMap.get(SELECT_ATTR_JFPartType);
                //获取指定零件类型的id
                if (UIUtil.isNullOrEmpty(strFilterPartType)){
                    sunIdSet.add(strPartId);
                }else if (UIUtil.isNotNullAndNotEmpty(strPartType) && strPartType.equals(strFilterPartType)){
                    sunIdSet.add(strPartId);
                }
                Set<String> sunNodeIdSet = getSunNodeIdByRootMap(sunMap,strFilterPartType,i,dataMapList);
                sunIdSet.addAll(sunNodeIdSet);
            }
        }else {
            //兼容excel导入时没有children
            int endOfSubtree = JF_NewECRService_mxJPO.findEndOfSubtree(dataMapList, beginIndex);
            //防止下标越界
            if (endOfSubtree > dataMapList.size()){
                endOfSubtree = dataMapList.size() ;
            }
            int nextIndex = beginIndex + 1;
            for( ;nextIndex < endOfSubtree ;nextIndex++){
                Map sunMap = (Map) dataMapList.get(nextIndex);
                String strPartId = (String) sunMap.get(SELECT_ID);
                String strPartType = (String) sunMap.get(SELECT_ATTR_JFPartType);
                //获取指定零件类型的id
                if (UIUtil.isNullOrEmpty(strFilterPartType)){
                    sunIdSet.add(strPartId);
                }else if (UIUtil.isNotNullAndNotEmpty(strPartType) && strPartType.equals(strFilterPartType)){
                    sunIdSet.add(strPartId);
                }
            }
        }
        return sunIdSet;
    }
    /**
    *
    *@description 自制件表格属性编辑通用方法
    *@param context
	*@param args
    *@return matrix.util.StringList
    *@throws
    *@author CHENYAN
    *@date 2025/5/9 10:49
    */
    public StringList getJFNewECRAffectedItemsMakeTableEditAccess(Context context, String[] args) throws Exception {
        StringList res = new StringList();
        Map tableSettingMap = JPO.unpackArgs(args);
        MapList objectList = (MapList) tableSettingMap.get("objectList");
        Map columnMap = (Map) tableSettingMap.get("columnMap");
        String strFieldName = (String) columnMap.get("name");
        Map requestMap = (Map) tableSettingMap.get("requestMap");
        String strECRId = (String) requestMap.get("objectId");
        DomainObject ecr = DomainObject.newInstance(context, strECRId);
        //获取ECR关联项目
        String strProjectId = ecr.getInfo(context, "from[JFChange2Project].to.id");
        MapList roleList = JF_PublicMethodClass_mxJPO.getLoginUserSignTaskRoleByECR(context, new String[]{strECRId});
        _logger.info("roleList：{}", roleList);
        _logger.info("strFieldName：{}", strFieldName);

        //add by chenyan 2-25/04/14 新增APR 阶段时财务BP可以编辑整椅卷积价格
        if ("JFChangeWholeSeatPrice".equals(strFieldName) || "JFChangeWholeSeatPriceExternal".equals(strFieldName)) {
            String strCurrent = ecr.getInfo(context, SELECT_CURRENT);
            String strChangeSource = ecr.getInfo(context, SELECT_ATTR_JFCHANGESOURCE);
            _logger.info("strChangeSource:{}",strChangeSource);
            boolean prrAccess = false;
            if ("APR".equals(strCurrent)) {
                for (int i = 0; i < roleList.size(); i++) {
                    Map roleMap = (Map) roleList.get(i);
                    String strRoleName = (String) roleMap.get("role");
                    if (JF_PLMConstants_mxJPO.ATTR_PROJECT_ROLE_Range_FinancialBP.equals(strRoleName)) {
                        prrAccess = false;
                        if (ATTR_JFCHANGESOURCE_RANGE_InternalChanges.equals(strChangeSource) && "JFChangeWholeSeatPrice".equals(strFieldName)){
                            prrAccess = true;
                        }else if (ATTR_JFCHANGESOURCE_RANGE_ExternalChanges.equals(strChangeSource) && "JFChangeWholeSeatPriceExternal".equals(strFieldName)){
                            prrAccess = true;
                        }else if (ATTR_JFCHANGESOURCE_RANGE_BOTH.equals(strChangeSource)) {
                            prrAccess = true;
                        }
                        break;
                    }
                }
            }
            _logger.info("prrAccess:{}", prrAccess);
            //只要是 REL_JFECRRelateRoot 就可以编辑 mod by chenyan 2025/08/25
//            DomainObject part = DomainObject.newInstance(context);

            for (int i = 0; i < objectList.size(); i++) {
                Boolean isEdit = Boolean.FALSE;
                if (prrAccess) {
                    Map partMap = (Map) objectList.get(i);
                    String strRelationship = UIUtil.getValue(partMap, RELATIONSHIP);

//                    String strPartId = (String)partMap.get(SELECT_ID);
//                    part.setId(strPartId);
//                    String strRootPartFlag = (String) partMap.get(JF_PLMConstants_mxJPO.SELECT_ATTRIBUTE_JF_freeState);
                    if (REL_JFECRRelateRoot.equals(strRelationship)) {
                        //卷积属性只能  JFECRRelateRoot 且是非游离供货件才可以编辑
                        if (partMap.containsKey("rowEditFlag") && partMap.containsKey("isZeroPart") && !partMap.containsKey("freeFlag")){
                            isEdit = Boolean.TRUE;
                        }
                    }
                }
                res.add(isEdit.toString());
            }
        } else {
            try {
                res = getNewMakeTableEditAccessInLoginRole(context, objectList, roleList, strECRId, strFieldName,strProjectId);
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }
        return res;
    }
    /**
    *
    *@description 判断零件是否是指定项目的供货件
    *@param context
	*@param strPartId
	*@param strProjectId
    *@return boolean
    *@throws
    *@author CHENYAN
    *@date 2025/8/25 16:00
    */

    public static boolean checkPartIsZeroPart(Context context,String strPartId ,String strProjectId) throws Exception{
        String strSelectZeroPart = "print bus %s select to[JFProject2RootPart|from.id=='%s'&&attribute[JFZeroPart]=='Y'] dump";
        
        String strIsZeroPart = String.format(strSelectZeroPart, strPartId,strProjectId);
        _logger.info("strIsZeroPart:{}",strIsZeroPart);
        //判断零件是否是指定项目供货件
        String strIsZeroPartRes = MqlUtil.mqlCommand(context, Boolean.FALSE, strIsZeroPart, Boolean.TRUE);
        _logger.info("strIsZeroPartRes:{}",strIsZeroPartRes);
        return "TRUE".equalsIgnoreCase(strIsZeroPartRes);
    }
    /**
    *
    *@description 获取自制件清单的编辑权限
    *@param context
	*@param objectList 表格数据
	*@param roleList 角色集合
	*@param strECRId ECR id
	*@param strFieldName 属性名
    *@return matrix.util.StringList
    *@throws
    *@author CHENYAN
    *@date 2025/5/9 10:49
    */
    public static StringList getNewMakeTableEditAccessInLoginRole(Context context, MapList objectList, MapList roleList, String strECRId, String strFieldName,String strProjectId) throws Exception {
        StringList res = new StringList(objectList.size());
        //角色列表为空全返回false
        DomainObject ecr = DomainObject.newInstance(context, strECRId);
        StringList ecrBusSelectList = new StringList();
        ecrBusSelectList.add(SELECT_CURRENT);
        ecrBusSelectList.add(SELECT_OWNER);
        ecrBusSelectList.add(SELECT_ATTR_JFCHANGESOURCE);
        Map ecrInfo = ecr.getInfo(context, ecrBusSelectList);
        String strCurrent = (String) ecrInfo.get(SELECT_CURRENT);
        //ECR的状态是否可编辑
        boolean hasECREditAccess = "APR".equals(strCurrent) || "Countersign".equals(strCurrent);
//        String strChangeSource = (String) ecrInfo.get(SELECT_ATTR_JFCHANGESOURCE);
//        _logger.info("strChangeSource:{}",strChangeSource);
        if (null == roleList || roleList.size() == 0 || (!hasECREditAccess)) {
            for (int i = 0; i < objectList.size(); i++) {
                res.add(Boolean.FALSE.toString());
            }
            return res;
        }
        //不可编辑的id集合
        boolean isFinancialBP = false ;
        Set notEditAccessIdSet = new HashSet<String>();

        for (int i = 0; i < roleList.size(); i++) {
            Map roleMap = (Map) roleList.get(i);
            //角色名称
            String strRoleName = (String) roleMap.get("role");
           if (ATTR_PROJECT_ROLE_Range_FinancialBP.equals(strRoleName)){
               isFinancialBP = true;
               break;
            }
        }

        //第一个结点是否是ECR
//         boolean isFirstECR = TYPE_JFNewECR.equals(strIndex0Type) ;
        // 1.ECR的变更来源来控制权限
        for (int i = 0; i < objectList.size();i++) {
            Boolean isEdit = Boolean.FALSE;
            Map objMap = (Map) objectList.get(i);
            String strDirectBuy = (String) objMap.get(SELECT_ATTR_JFDIRECT_BUY);
            String strPartType = (String) objMap.get(SELECT_ATTR_JFPartType);
            String strPartId = (String) objMap.get(SELECT_ID);
            String strProcurementType = (String) objMap.get(SELECT_ATTR_JF_ProcurementType);
            String strChangeSource = (String)objMap.get("to[JFRelateItem].attribute[JFChangeSource]");
            String strRelationship = (String) objMap.get(RELATIONSHIP);
            //先判断该零件是否是自制件且是整椅、面套、发泡
            //面套发泡一级件才可编辑
//            Boolean proEditAccess = checkPartIsMakeAndFoamCoating(context, objMap, "");
//            if (proEditAccess && (!objMap.containsKey("freeFlag"))) {
            /**add by chenyan 移除游离件不可编辑逻辑*/
            if (objMap.containsKey("rowEditFlag")) {
                //根据角色判断是否跳过
                //标识编辑
                String strEditFlag = (String) objMap.get("rowEditFlag");
                if (isFinancialBP){
                    if (REL_JFECRRelateRoot.equals(strRelationship)){
                        boolean isSkip = checkSkipSunTreeByRoleAndPartType(roleList, strPartType);

                        if (isSkip){
//                    Set<String> sunNodeIdSet = getSunNodeIdByRootMap(objMap,strPartType,i,objectList);
//                    notEditAccessIdSet.addAll(sunNodeIdSet);
                            isEdit = getFieldEditAccessByChangeSourceAndRoleList(roleList, strChangeSource, strFieldName, strDirectBuy, strProcurementType, strPartType, false,strEditFlag);
                        }
                    }

                }else {
                    boolean isSkip = checkSkipSunTreeByRoleAndPartType(roleList, strPartType);

                    if (isSkip){
//                    Set<String> sunNodeIdSet = getSunNodeIdByRootMap(objMap,strPartType,i,objectList);
//                    notEditAccessIdSet.addAll(sunNodeIdSet);
                        isEdit = getFieldEditAccessByChangeSourceAndRoleList(roleList, strChangeSource, strFieldName, strDirectBuy, strProcurementType, strPartType, false,strEditFlag);
                    }
                }

            }
            res.add(isEdit.toString());
        }
//        for (int i = 0; i < objectList.size(); i++) {
//            Map partMap = (Map) objectList.get(i);
//            String strIsEdit = "false" ;
//            if ((!partMap.containsKey("freeFlag")) &&partMap.containsKey("isEdit")) {
//                strIsEdit =  (String) partMap.get("isEdit");
//            }
//            res.add(strIsEdit);
//        }
        _logger.info("res:{}", res);
        return res;
    }

    /**
    *
    *@description 根据角色和零件类型判断是否跳过该树
    *@param roleList
	*@param strPartType
    *@return boolean
    *@throws
    *@author CHENYAN
    *@date 2025/5/21 10:58
    */
    public static boolean checkSkipSunTreeByRoleAndPartType(MapList roleList ,String strPartType){
        boolean res = false ;
        if (Objects.nonNull(roleList) && roleList.size() > 0){
            for (int i = 0; i < roleList.size(); i++) {
                Map roleMap = (Map) roleList.get(i);
                //角色名称
                String strRoleName = (String) roleMap.get("role");
                if ((ATTR_PROJECT_ROLE_Range_AME.equals(strRoleName)) && !("U".equals(strPartType) || "T".equals(strPartType))){
                    res = true;
                }else if (ATTR_PROJECT_ROLE_Range_Foam_AME.equals(strRoleName) && "U".equals(strPartType)){
                    res = true;
                }else if (ATTR_PROJECT_ROLE_Range_Trim_AME.equals(strRoleName) && "T".equals(strPartType)){
                    res = true;
                }else if (ATTR_PROJECT_ROLE_Range_FinancialBP.equals(strRoleName) && FoamCoatingFlagList.contains(strPartType)){
                    res = true;
                }if (ATTR_PROJECT_ROLE_Range_PRR.equals(strRoleName) || ATTR_PROJECT_ROLE_Range_Costing.equals(strRoleName) || ATTR_PROJECT_ROLE_Range_FinancialBP.equals(strRoleName)){
                    res = true;
                }
            }
        }
        return res ;
    }
    /**
     * @param partMap   要么给我已经查询好的数据 要么给我id
     * @param strPartId
     * @return java.lang.Boolean
     * @throws
     * @description 检查该零件是否是自制件且是整椅、面套和发泡
     * @author CHENYAN
     * @date 2025/4/27 10:33
     */

    public static Boolean checkPartIsMakeAndFoamCoating(Context context, Map partMap, String strPartId) throws Exception {
        Boolean isFlag = Boolean.FALSE;
        if (Objects.isNull(partMap) && UIUtil.isNotNullAndNotEmpty(strPartId)) {
            DomainObject part = DomainObject.newInstance(context, strPartId);
            partMap = part.getInfo(context, StringList.create(SELECT_ATTR_JF_PartType, SELECT_ATTR_JF_ProcurementType));
        }
        if (partMap.containsKey(SELECT_ATTR_JF_PartType) && partMap.containsKey(SELECT_ATTR_JF_ProcurementType)) {
            String strPartType = (String) partMap.get(SELECT_ATTR_JF_PartType);
            String strProcurementType = (String) partMap.get(SELECT_ATTR_JF_ProcurementType);
            if (ATTR_JF_ProcurementType_RANGE_MAKE.equals(strProcurementType) && FoamCoatingFlagList.contains(strPartType)) {
                isFlag = Boolean.TRUE;
            }
        }
        return isFlag;
    }

    /**
     * @param roleList
     * @param strChangeSource
     * @param strFieldName
     * @return java.lang.Boolean
     * @throws
     * @description 通过用户会签任务和变更来源、判断该属性是否编辑
     * @author CHENYAN
     * @date 2025/4/27 14:10
     */

    public static Boolean getFieldEditAccessByChangeSourceAndRoleList(MapList roleList, String strChangeSource, String strFieldName, String strDirectBuy, String strProcurementType, String strPartType, boolean isBuyFlag,String strEditFlag) {
        Boolean isEdit = Boolean.FALSE;
        for (int i = 0; i < roleList.size(); i++) {
            Map roleMap = (Map) roleList.get(i);
            String strCurrent = (String) roleMap.get(SELECT_CURRENT);
            //角色名称
            String strRoleName = (String) roleMap.get("role");
            //会签任务状态为审核和已完成直接跳过
            if ("Review".equals(strCurrent) || "Complete".equals(strCurrent)) {
                continue;
            }
            //变更来源和属性名必须存在
            if (UIUtil.isNotNullAndNotEmpty(strChangeSource) && UIUtil.isNotNullAndNotEmpty(strFieldName)) {
                _logger.info("strRoleName:{} strChangeSource:{} strFieldName:{} strDirectBuy:{} strProcurementType:{} strPartType:{} isBuyFlag:{} strEditFlag:{}",strRoleName,strChangeSource,strFieldName,strDirectBuy,strProcurementType,strPartType,isBuyFlag,strEditFlag);
                Boolean hasEditAccess = getFieldEditAccessByChangeSourceAndRoleName(strRoleName, strChangeSource, strFieldName, strDirectBuy, strProcurementType, strPartType, isBuyFlag,strEditFlag);
                //可以编辑时直接返回
                if (hasEditAccess) {
                    isEdit = hasEditAccess;
                    break;
                }
            }
        }
        return isEdit;
    }

    /**
     * @param strRoleName        角色名
     * @param strChangeSource    变更来源 自制件清单是ECR的变更来源， 采购件是零件的关系属性上维护的变更来源
     * @param strFieldName       需要校验属性名
     * @param strDirectBuy       DirectBuy
     * @param strProcurementType 采购类型
     * @param strPartType        零件类型
     * @param isBuyFlag          是否采购件清单
     * @return java.lang.Boolean
     * @throws
     * @description 通过属性名判断，该角色在该零件行中是否拥有该属性的编辑权限
     * @author CHENYAN
     * @date 2025/4/27 15:12
     */

    public static Boolean getFieldEditAccessByChangeSourceAndRoleName(String strRoleName, String strChangeSource, String strFieldName, String strDirectBuy, String strProcurementType, String strPartType, boolean isBuyFlag,String strEditFlag) {
        Boolean isEdit = Boolean.FALSE;
        //变更来源和属性名必须存在
        if (UIUtil.isNotNullAndNotEmpty(strChangeSource) && UIUtil.isNotNullAndNotEmpty(strFieldName)) {
            StringList canEditFieldList = getRequireEditFieldInRoleAndLinkField(isBuyFlag, strRoleName, strDirectBuy, strProcurementType, strPartType, strChangeSource,strEditFlag);
            _logger.info("canEditFieldList:{}",canEditFieldList);
            isEdit = canEditFieldList.contains(strFieldName);
        }
        return isEdit;
    }

    /**
     * @param isBuyFlag          是否采购件清单
     * @param strRole            角色名
     * @param strDirectBuy       DirectBuy
     * @param strProcurementType 采购类型
     * @param strPartType        零件类型
     * @param strChangeSource    变更来源 自制件清单是ECR的变更来源， 采购件是零件的关系属性上维护的变更来源
     * @return matrix.util.StringList
     * @throws
     * @description 根据各个条件判断出每一个零件中得必填属性是哪些
     * @author CHENYAN
     * @date 2025/4/27 15:06
     */

    public static StringList getRequireEditFieldInRoleAndLinkField(boolean isBuyFlag, String strRole, String strDirectBuy, String strProcurementType, String strPartType, String strChangeSource,String strEditFlag) {
        StringList res = new StringList();
        switch (strRole) {
            //财务BP
            case ATTR_PROJECT_ROLE_Range_FinancialBP: {
                if ((!isBuyFlag)) {
                    if (ATTR_JFCHANGESOURCE_RANGE_InternalChanges.equals(strChangeSource)) {
                        res.add(ATTR_JFChangeSeatCost);
                        res.add(ATTR_JFChangeTargetPrice);
                    } else if (ATTR_JFCHANGESOURCE_RANGE_ExternalChanges.equals(strChangeSource)) {
                        res.add(ATTR_JFChangeTargetPrice);
                        res.add(ATTR_JFChangeSeatCostExternal);
                    } else if (ATTR_JFCHANGESOURCE_RANGE_BOTH.equals(strChangeSource)) {
                        res.add(ATTR_JFChangeSeatCost);
                        res.add(ATTR_JFChangeTargetPrice);
                        res.add(ATTR_JFChangeSeatCostExternal);
                    }
                }
                break;
            }
            // mod by chenyan 商务代表必填属性转移给采购
            //商务代表
            case ATTR_PROJECT_ROLE_Range_BU: {
                break;
            }
            // 整椅AME
            case ATTR_PROJECT_ROLE_Range_AME: {
                if (strEditFlag.contains("1")){
                    if ((!isBuyFlag) && (!("U".equals(strPartType) || "T".equals(strPartType))) && ATTR_JF_ProcurementType_RANGE_MAKE.equals(strProcurementType)) {
                        if (ATTR_JFCHANGESOURCE_RANGE_InternalChanges.equals(strChangeSource)) {
                            res.add(ATTR_JFChangeManHour);
                        } else if (ATTR_JFCHANGESOURCE_RANGE_ExternalChanges.equals(strChangeSource)) {
                            res.add(ATTR_JFChangeManHour_External);
                        } else if (ATTR_JFCHANGESOURCE_RANGE_BOTH.equals(strChangeSource)) {
                            res.add(ATTR_JFChangeManHour);
                            res.add(ATTR_JFChangeManHour_External);
                        }
                    }
                }

                break;
            }
            //发泡AME
            case ATTR_PROJECT_ROLE_Range_Foam_AME: {
                if (strEditFlag.contains("1")) {
                    if ((!isBuyFlag) && "U".equals(strPartType) && ATTR_JF_ProcurementType_RANGE_MAKE.equals(strProcurementType)) {
                        if (ATTR_JFCHANGESOURCE_RANGE_InternalChanges.equals(strChangeSource)) {
                            res.add(ATTR_JFChangeManHour);
                        } else if (ATTR_JFCHANGESOURCE_RANGE_ExternalChanges.equals(strChangeSource)) {
                            res.add(ATTR_JFChangeManHour_External);
                        } else if (ATTR_JFCHANGESOURCE_RANGE_BOTH.equals(strChangeSource)) {
                            res.add(ATTR_JFChangeManHour);
                            res.add(ATTR_JFChangeManHour_External);
                        }
                    }
                }
                break;
            }
            //面套AME
            case ATTR_PROJECT_ROLE_Range_Trim_AME: {
                if (strEditFlag.contains("1")) {
                    if ((!isBuyFlag) && "T".equals(strPartType) && ATTR_JF_ProcurementType_RANGE_MAKE.equals(strProcurementType)) {
                        if (ATTR_JFCHANGESOURCE_RANGE_InternalChanges.equals(strChangeSource)) {
                            res.add(ATTR_JFChangeManHour);
                        } else if (ATTR_JFCHANGESOURCE_RANGE_ExternalChanges.equals(strChangeSource)) {
                            res.add(ATTR_JFChangeManHour_External);
                        } else if (ATTR_JFCHANGESOURCE_RANGE_BOTH.equals(strChangeSource)) {
                            res.add(ATTR_JFChangeManHour);
                            res.add(ATTR_JFChangeManHour_External);
                        }
                    }
                }
                break;
            }
            //Costing
            case ATTR_PROJECT_ROLE_Range_Costing: {
                //采购件table中属性 废弃
                //无需判断采购类型
                if (isBuyFlag){
                    if (ATTR_JF_ProcurementType_RANGE_BUY.equals(strProcurementType)) {
                        if (ATTR_JFCHANGESOURCE_RANGE_InternalChanges.equals(strChangeSource)) {
                            res.add(ATTR_JFChangeUnitPriceCost);
                            res.add(ATTR_JFChangeMoldCost);
                        } else if (ATTR_JFCHANGESOURCE_RANGE_ExternalChanges.equals(strChangeSource)) {
                            res.add(ATTR_JFChangeUnitPriceCostExternal);
                            res.add(ATTR_JFChangeMoldPriceCostExternal);
                        } else if (ATTR_JFCHANGESOURCE_RANGE_BOTH.equals(strChangeSource)) {
                            res.add(ATTR_JFChangeUnitPriceCost);
                            res.add(ATTR_JFChangeMoldCost);
                            res.add(ATTR_JFChangeUnitPriceCostExternal);
                            res.add(ATTR_JFChangeMoldPriceCostExternal);
                        }
                    }
                }else {
                        if (strEditFlag.contains("2")) {
                            if (ATTR_JFCHANGESOURCE_RANGE_InternalChanges.equals(strChangeSource)) {
                                res.add(ATTR_JFChangeUnitPriceCost);
                                res.add(ATTR_JFChangeMoldCost);
                            } else if (ATTR_JFCHANGESOURCE_RANGE_ExternalChanges.equals(strChangeSource)) {
                                res.add(ATTR_JFChangeUnitPriceCostExternal);
                                res.add(ATTR_JFChangeMoldPriceCostExternal);
                            } else if (ATTR_JFCHANGESOURCE_RANGE_BOTH.equals(strChangeSource)) {
                                res.add(ATTR_JFChangeUnitPriceCost);
                                res.add(ATTR_JFChangeMoldCost);
                                res.add(ATTR_JFChangeUnitPriceCostExternal);
                                res.add(ATTR_JFChangeMoldPriceCostExternal);
                            }
                        }
                }
                break;
            }
            //采购代表
            case ATTR_PROJECT_ROLE_Range_PRR: {
                if (isBuyFlag) {
                    if ((ATTR_ATTR_JFDIRECT_BUY_RANGE_N.equals(strDirectBuy) && ATTR_JF_ProcurementType_RANGE_BUY.equals(strProcurementType))
                            || ((ATTR_ATTR_JFDIRECT_BUY_RANGE_Y.equals(strDirectBuy) ||ATTR_ATTR_JFDIRECT_BUY_RANGE_consignment.equals(strDirectBuy))  && (ATTR_JF_ProcurementType_RANGE_BUY.equals(strProcurementType) || ATTR_JF_ProcurementType_RANGE_ICO.equals(strProcurementType)))
                    ) {
                        if (ATTR_JFCHANGESOURCE_RANGE_InternalChanges.equals(strChangeSource)) {
                            res.add(ATTR_JFChangeUnitPrice);
                            res.add(ATTR_JFChangeMold);
                            res.add(ATTR_JFStagnationOfSuppliersInternal);
                            res.add(ATTR_JFChangesTrialExpensesManHours);
                        } else if (ATTR_JFCHANGESOURCE_RANGE_ExternalChanges.equals(strChangeSource)) {
                            res.add(ATTR_JFChangeUnitPriceExternal);
                            res.add(ATTR_JFChangeMoldCostExternal);
                            res.add(ATTR_JFStagnationOfSuppliersExternal);
                            res.add(ATTR_JFChangesTrialExpensesManHoursExternal);
                        } else if (ATTR_JFCHANGESOURCE_RANGE_BOTH.equals(strChangeSource)) {
                            res.add(ATTR_JFChangeUnitPrice);
                            res.add(ATTR_JFChangeMold);
                            res.add(ATTR_JFStagnationOfSuppliersInternal);
                            res.add(ATTR_JFChangesTrialExpensesManHours);
                            res.add(ATTR_JFChangeUnitPriceExternal);
                            res.add(ATTR_JFChangeMoldCostExternal);
                            res.add(ATTR_JFStagnationOfSuppliersExternal);
                            res.add(ATTR_JFChangesTrialExpensesManHoursExternal);
                        }
                    }
                    //新增自制件采购填写属性
                }else {
                    if (strEditFlag.contains("2")) {
                        if (ATTR_JFCHANGESOURCE_RANGE_InternalChanges.equals(strChangeSource)) {
                            res.add(ATTR_JFChangeMold);
                        } else if (ATTR_JFCHANGESOURCE_RANGE_ExternalChanges.equals(strChangeSource)) {
                            res.add(ATTR_JFChangeMoldCostExternal);
                        } else if (ATTR_JFCHANGESOURCE_RANGE_BOTH.equals(strChangeSource)) {
                            res.add(ATTR_JFChangeMold);
                            res.add(ATTR_JFChangeMoldCostExternal);
                        }
                    }

                }
                break;
            }
            case ATTR_PROJECT_ROLE_Range_InternalSupplier: {
                if (ATTR_JF_ProcurementType_RANGE_ICO.equals(strProcurementType) && isBuyFlag && (!"Y".equals(strDirectBuy))) {
                    if (ATTR_JFCHANGESOURCE_RANGE_InternalChanges.equals(strChangeSource)) {
                        res.add(ATTR_JFChangeUnitPrice);
                        res.add(ATTR_JFChangeMold);
                        res.add(ATTR_JFStagnationOfSuppliersInternal);
                        res.add(ATTR_JFChangesTrialExpensesManHours);
                    } else if (ATTR_JFCHANGESOURCE_RANGE_ExternalChanges.equals(strChangeSource)) {
                        res.add(ATTR_JFChangeUnitPriceExternal);
                        res.add(ATTR_JFChangeMoldCostExternal);
                        res.add(ATTR_JFStagnationOfSuppliersExternal);
                        res.add(ATTR_JFChangesTrialExpensesManHoursExternal);
                    } else if (ATTR_JFCHANGESOURCE_RANGE_BOTH.equals(strChangeSource)) {
                        res.add(ATTR_JFChangeUnitPrice);
                        res.add(ATTR_JFChangeMold);
                        res.add(ATTR_JFStagnationOfSuppliersInternal);
                        res.add(ATTR_JFChangesTrialExpensesManHours);
                        res.add(ATTR_JFChangeUnitPriceExternal);
                        res.add(ATTR_JFChangeMoldCostExternal);
                        res.add(ATTR_JFStagnationOfSuppliersExternal);
                        res.add(ATTR_JFChangesTrialExpensesManHoursExternal);
                    }
                }
                break;
            }
            default: {
                break;
            }
        }
        return res;
    }

    /**
    *
    *@description 修改新ECR采购件和自制件属性
    *@param context
	*@param args
    *@return void
    *@throws
    *@author CHENYAN
    *@date 2025/5/8 17:25
    */
    public void updateNewAffectedItemsCostTableField(Context context, String[] args) throws Exception {
        _logger.info("------------------------------------------  updateNewAffectedItemsCostTableField  begin ------------------------------------------");
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
        _logger.info("strTableName:{}", strTableName);
        _logger.info("strParentOID:{}", strParentOID);
        _logger.info("strAttrName:{}", strAttrName);
        _logger.info("strObjectId:{}", strObjectId);
        _logger.info("strRelId:{}", strRelId);
        _logger.info("strNewValue:{}", strNewValue);
        String strRelName = "";
        //根据Table获取不同的关系 修复Bug 如果自定义了table有增加自定义的名称
        if (strTableName.contains("JFNewECRCosting")) {
            strRelName = "JFECR2PartPrice";
        } else if (strTableName.contains("JFNewECRController")) {
            strRelName = "JFECR2MakePartPrice";
        }
        try {
            ContextUtil.pushContext(context);
//            DomainRelationship rel = DomainRelationship.newInstance(context, strRelId);
            //add by chenyan 2025/05/13 可能存在多条价格关系需要用ECRId过滤
            String strSelectPriceId = JF_PublicMethodClass_mxJPO.buildStringInStrings("tomid[", strRelName,"|from.id==",strParentOID, "].id");
            //查询不到数据
//            String strPriceId = rel.select(context, strSelectPriceId);
            strSelectPriceId =  JF_PublicMethodClass_mxJPO.buildStringInStrings("print connection " ,strRelId, " select ",strSelectPriceId ," dump ");
            _logger.info("strSelectPriceId:{}",strSelectPriceId);
            String strPriceId = MqlUtil.mqlCommand(context, Boolean.FALSE,strSelectPriceId, Boolean.TRUE);
            //判断零件和价格关系是否已经创建 如果新建则修改关系属性 ，如果没有需要新建
            _logger.info("strPriceId:{}", strPriceId);
            try {
                ContextUtil.startTransaction(context, true);
                if (UIUtil.isNotNullAndNotEmpty(strPriceId)) {
                    DomainRelationship priceRel = DomainRelationship.newInstance(context, strPriceId);
                    priceRel.setAttributeValue(context, strAttrName, strNewValue);
                } else {
                    //创建关系设置属性
                    String strMql = JF_PublicMethodClass_mxJPO.buildStringInStrings("add connection " ,strRelName , " from ",strParentOID ," torel " ,strRelId," ",strAttrName," ",strNewValue);
                    _logger.info("strMql:{}",strMql);
                    MqlUtil.mqlCommand(context, Boolean.FALSE,strMql, Boolean.TRUE);
                }
                ContextUtil.commitTransaction(context);
            } catch (Exception e) {
                ContextUtil.abortTransaction(context);
                _logger.error(e.getMessage());
                throw e;
            }

        } finally {
            ContextUtil.popContext(context);
        }
        _logger.info("------------------------------------------  updateNewAffectedItemsCostTableField  end ------------------------------------------");

    }

    /**
    *
    *@description 获取采购件和自制件清单的价格属性值
    *@param context
	*@param args
    *@return matrix.util.StringList
    *@throws
    *@author CHENYAN
    *@date 2025/5/9 10:48
    */

    public StringList getNewAffectedItemsCostTableField(Context context, String[] args) throws Exception {
        _logger.info("------------------------------------------  getNewAffectedItemsCostTableField  begin ------------------------------------------");
        try {
            Map paramsMap = (Map) JPO.unpackArgs(args);
            Map paramList = (Map) paramsMap.get("paramList");
            String strTableName = (String) paramList.get("selectedTable");
            String strParentOID = (String) paramList.get("parentOID");
            Map columnMap = (Map) paramsMap.get("columnMap");
            String strColName = (String) columnMap.get("name");
            _logger.info("strColName:{}", strColName);
            //id 集合
            MapList objectList = (MapList) paramsMap.get("objectList");
            StringList tableIdList = _getListOfKeys(args, SELECT_ID);
            boolean isBuy = false;
            boolean isMake = false;
            // rid 集合
            String strRelName = "";
            if (strTableName.contains("JFNewECRCosting")) {
                strRelName = "JFECR2PartPrice";
                isBuy = true;
            }else if (strTableName.contains("JFNewECRController")) {
                strRelName = "JFECR2MakePartPrice";
                isMake = true;
            }
            DomainObject ecr = DomainObject.newInstance(context, strParentOID);
            String strECRChangeSource = ecr.getInfo(context, SELECT_ATTR_JFCHANGESOURCE);
            StringList res = new StringList();
            try {
                ContextUtil.pushContext(context);
                switch (strColName) {
                    case "JFChangeUnitPrice", "JFChangeMold",
                            "JFChangeUnitPriceExternal", "JFChangeMoldCostExternal",
                            "JFChangeUnitPriceCost", "JFChangeMoldCost",
                            "JFChangeUnitPriceCostExternal", "JFChangeMoldPriceCostExternal",
                            "JFStagnationOfSuppliersInternal", "JFStagnationOfSuppliersExternal",
                            "JFChangesTrialExpensesManHours", "JFChangesTrialExpensesManHoursExternal": {
                        String strSelectFieldKey = JF_PublicMethodClass_mxJPO.buildStringInStrings("tomid[", strRelName, "].", "attribute[", strColName, "]");
                        for (int i = 0; i < objectList.size(); i++) {
                            Map partMap = (Map) objectList.get(i);
                            String strValue = "";
                            if (partMap.containsKey(strSelectFieldKey)){
                                strValue = (String) partMap.get(strSelectFieldKey);
                            }
                            res.add(strValue);
                        }
                        break;
                    }
                    case "JFChangeMan-hour", "JFChangeMan-hourExternal",
                            "JFChangeSeatCost", "JFChangeSeatCostExternal",
                            "JFChangeTargetPrice", "JFChangeTargetPriceExternal",
                            "JFChangeWholeSeatPrice", "JFChangeWholeSeatPriceExternal": {
                        String strSelectFieldKey = JF_PublicMethodClass_mxJPO.buildStringInStrings("tomid[", strRelName, "].", "attribute[", strColName, "]");
                        for (int i = 0; i < objectList.size(); i++) {
                            Map partMap = (Map) objectList.get(i);
                            String strValue = "";
                            if (partMap.containsKey(strSelectFieldKey)){
                                strValue = (String) partMap.get(strSelectFieldKey);
                            }
                            res.add(strValue);
                        }
                        break;

                    }
                    case "JFChangeResource": {
                            for (int i = 0; i < objectList.size(); i++) {
                                Map partMap = (Map) objectList.get(i);
                                String strValue = "";
                                String strNlsValue = "";
                                if (partMap.containsKey("to[JFRelateItem].attribute[JFChangeSource]")){
                                    strValue = (String) partMap.get("to[JFRelateItem].attribute[JFChangeSource]");
                                }
                                if (UIUtil.isNullOrEmpty(strValue)){
                                    strValue = strECRChangeSource;
                                }
                                if (UIUtil.isNotNullAndNotEmpty(strValue)) {
                                    StringList nlsRanges = EnoviaResourceBundle.getAttrRangeI18NStringList(context, ATTR_JFCHANGESOURCE, new StringList(strValue), context.getLocale().toString());
                                    if (nlsRanges.size() > 0) {
                                        strNlsValue = nlsRanges.get(0);
                                    }
                                }
                                res.add(strNlsValue);
                            }
                        break;
                    }
                    case "id": {
                        // mod by chenyan 2025/06/12 id改为 partid,relid
                        for (int i = 0; i < objectList.size(); i++) {
                            StringBuilder sbValue = new StringBuilder();
                            Map partMap = (Map) objectList.get(i);
                            String strPartId = (String) partMap.get(SELECT_ID);
                            String strPartRelId = (String) partMap.get(SELECT_RELATIONSHIP_ID);
                            if (UIUtil.isNotNullAndNotEmpty(strPartId)){
                                sbValue.append(strPartId);
                            }
                            if (UIUtil.isNotNullAndNotEmpty(strPartRelId)){
                                sbValue.append(",");
                                sbValue.append(strPartRelId);
                            }
                            res.add(sbValue.toString());
                        }
//                        res  = _getListOfKeys(args, SELECT_ID);
                        break;
                    }
                    case "id[connection]": {
                        String strSelectReId = JF_PublicMethodClass_mxJPO.buildStringInStrings("tomid[", strRelName, "].id");
                        for (int i = 0; i < objectList.size(); i++) {
                            String strValue = "";
                            Map partMap = (Map) objectList.get(i);
                            if (partMap.containsKey(strSelectReId)){
                                strValue = (String) partMap.get(strSelectReId);
                            }
                            res.add(strValue);
                        }
                       break;
                    }
                    case "revision": {
                        res = _getListOfKeys(args, SELECT_REVISION);
                        break;
                    }
                    case "level": {
                        res = _getListOfKeys(args, SELECT_LEVEL);
                        break;
                    }
                    case "JF_freeState": {
                        for (int i = 0; i < objectList.size(); i++) {
                            String strValue = "";
                            Map oMap = (Map)objectList.get(i);
                            String strRootRelName = (String) oMap.get(RELATIONSHIP);
                            if (REL_JFECRRelateRoot.equals(strRootRelName) || REL_JFECRRelateRootBubble.equals(strRootRelName)){
                                String strFreeState = (String)oMap.get(SELECT_ATTR_JFFREEState);
                                if (UIUtil.isNotNullAndNotEmpty(strFreeState)){
                                    StringList nlsRanges = EnoviaResourceBundle.getAttrRangeI18NStringList(context, ATTR_JFFREEState, new StringList(strFreeState), context.getLocale().toString());
                                    strValue = nlsRanges.get(0);
                                }
                            }
                            res.add(strValue);
                        }
                        break;
                    }
                    case "PartNumber", "PartType", "PartNameCN", "PartNameEN", "ProcurementType", "DirectBuy": {
                        String strColSelectName = (String) columnMap.get("selectName");
                        String strAttributeName = (String) columnMap.get("attributeName");
                        StringList tableSelectList = _getListOfKeys(args, strColSelectName);
                        if (strColName.equals("PartType") || strColName.equals("ProcurementType")) {
                            for (int i = 0; i < tableSelectList.size(); i++) {
                                String strFieldValue = tableSelectList.get(i);
                                StringList nlsRanges = EnoviaResourceBundle.getAttrRangeI18NStringList(context, strAttributeName, new StringList(strFieldValue), context.getLocale().toString());
                                _logger.info("nlsRanges:{}", nlsRanges);
                                res.add(nlsRanges.get(0));
                            }
                        }else {
                            res = tableSelectList;
                        }
                        break;
                    }
                    case "OnePartQuantity": {
                        for (int i = 0; i < objectList.size(); i++) {
                            res.add("1");
                        }
                        break;
                    } default:{
                        break;
                    }
                }
            } finally {
                ContextUtil.popContext(context);
            }
            _logger.info("------------------------------------------  getNewAffectedItemsCostTableField  end ------------------------------------------");
            return res;
        } catch (Exception e) {
            _logger.info(e.getMessage());
            throw e;
        }
    }

    public static StringList _getListOfKeys(String[] var0, String strKey) throws Exception {
        StringList var1 = new StringList();
        Map var2 = (Map) JPO.unpackArgs(var0);
        MapList var3 = (MapList) var2.get("objectList");
        if (var3 != null) {
            int var4 = 0;

            for (int var5 = var3.size(); var4 < var5; ++var4) {
                Map var6 = (Map) var3.get(var4);
                if (null != var6 && var6.containsKey(strKey)) {
                    String var7 = (String) var6.get(strKey);
                    var1.add(var7);
                } else {
                    var1.add("");
                }
            }
        }
        return var1;
    }

    /**
     * @param context
     * @param args
     * @return com.matrixone.apps.domain.util.MapList
     * @throws
     * @description 获取受影响父件Table数据
     * @author CHENYAN
     * @date 2024/7/25 15:25
     */
    public MapList getECRParentAffectedItems(Context context, String[] args) throws Exception {
        _logger.info("-----------------------------------getECRParentAffectedItems begin ---------------------------------------------");
        Map parameters = JPO.unpackArgs(args);
        String strObjectId = (String) parameters.get("objectId");
        DomainObject ecr = DomainObject.newInstance(context, strObjectId);
        StringList reSelectList = new StringList(SELECT_RELATIONSHIP_ID);
        StringList typeSelectList = new StringList(SELECT_ID);
        reSelectList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JFSubIFReplace);
        reSelectList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JFSubPartId);
        reSelectList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JFSubConnectId);
        MapList maps = ecr.getRelatedObjects(context, JF_PLMConstants_mxJPO.REL_JFRelateItemParent, // relationship pattern
                JF_PLMConstants_mxJPO.TYPE_VPMReference,                                    // object pattern
                typeSelectList,                            // object selects
                reSelectList, // relationship selects
                false,                                        // to direction
                true,                                        // from direction
                (short) 1,                                    // recursion level
                "",                // object where clause
                "",
                (short) 0);
        _logger.info("-----------------------------------getECRParentAffectedItems end ---------------------------------------------");
        return maps;
    }

    /**
     * 父件是否替换
     * 1. 点击确定为Y时候，遍历所有的，全部设置
     * 2. 如果该受影响件加入到了快照中，是否替换设置成N，其他的就设置Y
     * 3. 如果为Y的话，在设置完成之后，受影响父替换逻辑选择是的时候，只替换一级件的JFECRRelateRoot 关系，不在进行对比了
     *
     * @param context
     * @param args
     * @return java.lang.Boolean
     * @throws
     * @author LIUJR
     * @date 2025/4/28 14:58
     * @description
     */
    public Boolean ecrSubPartIFReplaceProcess(Context context, String[] args) throws Exception {
        _logger.info("-----------------------------------ecrSubPartIFReplaceProcess start ---------------------------------------------");
        Boolean flag = Boolean.TRUE;
        try {
            Map params = JPO.unpackArgs(args);
            ContextUtil.pushContext(context);
            ContextUtil.startTransaction(context, true);
            String strObjectId = (String) params.get("objectId");
            String ifReplace = (String) params.get("IFReplace");
            _logger.info("ifReplace:{}", ifReplace);
            //需要替换 如果该受影响父件加入到了快照中，是否替换设置成N，其他的就设置Y
            DomainObject ecrObject = DomainObject.newInstance(context);
            ecrObject.setId(strObjectId);
            StringList basicBolistSel = JF_Util_mxJPO.basicBolistSel();
            StringList basicRellistSel = JF_Util_mxJPO.basicRellistSel();
            basicRellistSel.add(SELECT_ATTR_JFSubPartId);
            basicRellistSel.add(SELECT_ATTR_JFSubConnectId);
            basicRellistSel.add(SELECT_ATTR_JFIslocked);
            basicRellistSel.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JFSubIFReplace);
            //获取受影响父件
            MapList mapList = ecrObject.getRelatedObjects(context,
                    REL_JFRelateItemParent,
                    TYPE_VPMReference,
                    basicBolistSel,
                    basicRellistSel,
                    false,
                    true,
                    (short) 1,
                    "",
                    "",
                    (short) 0);
            _logger.info("mapList:{}", mapList);
            DomainObject domainObject = DomainObject.newInstance(context);
            MapList replaceMapList = new MapList();
            for (int i = 0; i < mapList.size(); i++) {
                Map map = (Map) mapList.get(i);
                String partId = UIUtil.getValue(map, SELECT_ID);
                String connId = UIUtil.getValue(map, DomainConstants.SELECT_RELATIONSHIP_ID);
                DomainRelationship domainRelationship = DomainRelationship.newInstance(context, connId);
                String strIfReplace = UIUtil.getValue(map, JF_PLMConstants_mxJPO.SELECT_ATTR_JFSubIFReplace);
                String strIsLocked= UIUtil.getValue(map, JF_PLMConstants_mxJPO.SELECT_ATTR_JFIslocked);
                domainObject.setId(partId);
                String snapshotCurrent = domainObject.getInfo(context, "to[" + JF_PLMConstants_mxJPO.rel_JFSnapshot2VPMReference + "].from.current");
                Boolean isSnapshot = Boolean.FALSE;
                if ("FROZEN".equalsIgnoreCase(snapshotCurrent) || "Close".equalsIgnoreCase(snapshotCurrent)) {
                    isSnapshot = Boolean.TRUE;
                }
                String value = EMPTY_STRING;
                if ("N".equalsIgnoreCase(ifReplace)) {
                    //是否替换为否  需要将变更的受影响结构还原
                    if (UIUtil.isNullOrEmpty(strIfReplace)) {
                        //为空 是首次 可以直接设置
                        value = "N";
                    } else if ("Y".equalsIgnoreCase(strIfReplace)) {
                        //需要移除
                        value = "N";
                        replaceMapList.add(map);
                    }
                } else if ("Y".equalsIgnoreCase(ifReplace)) {
                    //替换为是，需要将父件加入受影响项结构
                    if ("Y".equalsIgnoreCase(strIsLocked)) {
                        //锁定的不管
                        continue;
                    } else if (UIUtil.isNullOrEmpty(strIfReplace) || "N".equalsIgnoreCase(strIfReplace)){
                        if (isSnapshot) {
                            //有快照 设置为N
                            value = "N";
                        } else {
                            replaceMapList.add(map);
                            value = "Y";
                        }
                    }
                }
                if (UIUtil.isNotNullAndNotEmpty(value)) {
                    domainRelationship.setAttributeValue(context, JF_PLMConstants_mxJPO.ATTR_JFSubIFReplace, value);
                }
            }
            String unChanged = ComponentsUIUtil.getI18NString(context, context.getLocale().toString(), "emxComponents.ECRChange.UnChanged", new String[]{});
            //需要替换结构
            StringList ecrItemList = new StringList();
            DomainRelationship domainRelationship;
            for (int i = 0; i < replaceMapList.size(); i++) {
                Map map = (Map) replaceMapList.get(i);
                _logger.info("map:{}", map);
                String partId = UIUtil.getValue(map, SELECT_ID);
                String subPartId = UIUtil.getValue(map, SELECT_ATTR_JFSubPartId);
                domainObject.setId(partId);
                _logger.info("ecrItemList:{}", ecrItemList);
                if ("Y".equalsIgnoreCase(ifReplace)) {
                    //断开子件与ECR的关系   关联父件与ecr的关系 关联子件与父件的关系
                    map.put("relName", JF_PLMConstants_mxJPO.REL_JFECRRelateRoot);
                    map.put("fromId", strObjectId);
                    map.put("toId", subPartId);
                    String connId = JF_PublicMethodClass_mxJPO.getTwoBusinessObjectConnId(context, JPO.packArgs(map));
                    HashMap attributeMap = new HashMap<>();
                    if (UIUtil.isNotNullAndNotEmpty(connId)) {
                        domainRelationship = DomainRelationship.newInstance(context, connId);
                        attributeMap.put(ATTRIBUTE_JF_BOMQuantity, domainRelationship.getAttributeValue(context, ATTRIBUTE_JF_BOMQuantity));
                        attributeMap.put(ATTRIBUTE_JF_BOMChangeDes, domainRelationship.getAttributeValue(context, ATTRIBUTE_JF_BOMChangeDes));
                        attributeMap.put(ATTRIBUTE_JF_ECRID, domainRelationship.getAttributeValue(context, ATTRIBUTE_JF_ECRID));
                        attributeMap.put(ATTRIBUTE_JF_ChangeBeforeRev, domainRelationship.getAttributeValue(context, ATTRIBUTE_JF_ChangeBeforeRev));
                        attributeMap.put(ATTRIBUTE_JF_BOMChangeQuantity, domainRelationship.getAttributeValue(context, ATTRIBUTE_JF_BOMChangeQuantity));
                        DomainRelationship.disconnect(context, connId);
                    }
                    //将子件关联到父级件上面  需要判断是否有这个关系 有就需要加ECRid
                    map.clear();
                    map.put("relName", JF_PLMConstants_mxJPO.REL_JFECRRoot2Item);
                    map.put("fromId", partId);
                    map.put("toId", subPartId);
                    connId = JF_PublicMethodClass_mxJPO.getTwoBusinessObjectConnId(context, JPO.packArgs(map));
                    if (UIUtil.isNullOrEmpty(connId)) {
                        domainRelationship = domainObject.addToObject(context, new RelationshipType(JF_PLMConstants_mxJPO.REL_JFECRRoot2Item), subPartId);
                        domainRelationship.setAttributeValues(context, attributeMap);
                    } else {
                        domainRelationship = DomainRelationship.newInstance(context, connId);
                        String ecrId = domainRelationship.getAttributeValue(context, ATTRIBUTE_JF_ECRID);
                        if (!ecrId.contains(strObjectId)) {
                            ecrId += "," + strObjectId;
                            domainRelationship.setAttributeValue(context, ATTRIBUTE_JF_ECRID, ecrId);
                        }
                    }
                    //将父件替换为受影响件 关联到ECR
                    if (ecrItemList.contains(partId)) {
                        continue;
                    }
                    domainRelationship = ecrObject.addToObject(context, new RelationshipType(JF_PLMConstants_mxJPO.REL_JFECRRelateRoot), partId);
                    attributeMap.clear();
                    attributeMap.put(ATTRIBUTE_JF_BOMQuantity, "1");
                    attributeMap.put(ATTRIBUTE_JF_BOMChangeDes, unChanged);
                    attributeMap.put(ATTRIBUTE_JF_ECRID, strObjectId);
                    domainRelationship.setAttributeValues(context, attributeMap);
                    //将父件关联到受影响件关系  JFRelateItem 并将变更来源设置为 ecr的变更来源
                    domainRelationship = ecrObject.addToObject(context, new RelationshipType(JF_PLMConstants_mxJPO.REL_JFRELATEITEM), partId);
//                    domainRelationship.setAttributeValue(context, ATTR_JFCHANGESOURCE, ecrObject.getAttributeValue(context, ATTR_JFCHANGESOURCE));
                    ecrItemList.add(partId);
                } else if ("N".equalsIgnoreCase(ifReplace)) {
                    //移除结构
                    //断开父级与子级的关系   关联子件与ecr的关系
                    map.put("relName", JF_PLMConstants_mxJPO.REL_JFECRRoot2Item);
                    map.put("fromId", partId);
                    map.put("toId", subPartId);
                    String connId = JF_PublicMethodClass_mxJPO.getTwoBusinessObjectConnId(context, JPO.packArgs(map));
                    HashMap attributeMap = new HashMap<>();
                    if (UIUtil.isNotNullAndNotEmpty(connId)) {
                        domainRelationship = DomainRelationship.newInstance(context, connId);
                        String ecrId = domainRelationship.getAttributeValue(context, ATTRIBUTE_JF_ECRID);
                        if (ecrId.equalsIgnoreCase(strObjectId)) {
                            attributeMap.put(ATTRIBUTE_JF_BOMQuantity, domainRelationship.getAttributeValue(context, ATTRIBUTE_JF_BOMQuantity));
                            attributeMap.put(ATTRIBUTE_JF_BOMChangeDes, domainRelationship.getAttributeValue(context, ATTRIBUTE_JF_BOMChangeDes));
                            attributeMap.put(ATTRIBUTE_JF_BOMChangeQuantity, domainRelationship.getAttributeValue(context, ATTRIBUTE_JF_BOMChangeQuantity));
                            attributeMap.put(ATTRIBUTE_JF_ECRID, ecrId);
                            attributeMap.put(ATTRIBUTE_JF_ChangeBeforeRev, domainRelationship.getAttributeValue(context, ATTRIBUTE_JF_ChangeBeforeRev));
                            DomainRelationship.disconnect(context, connId);
                        } else if (ecrId.contains(strObjectId)) {
                                String[] split = ecrId.split(",");
                                StringList ecrIdList = new StringList();
                                for (int i1 = 0; i1 < split.length; i1++) {
                                    if (split[i1].equalsIgnoreCase(strObjectId)) {
                                        continue;
                                    }
                                    ecrIdList.add(split[i1]);
                                }
                                domainRelationship.setAttributeValue(context, JF_PLMConstants_mxJPO.ATTRIBUTE_JF_ECRID, ecrIdList.join(","));
                        }
                    }
                    //将子件关联到ecr级件上面
                    map.clear();
                    map.put("relName", JF_PLMConstants_mxJPO.REL_JFECRRoot2Item);
                    map.put("fromId", partId);
                    map.put("toId", subPartId);
                    connId = JF_PublicMethodClass_mxJPO.getTwoBusinessObjectConnId(context, JPO.packArgs(map));
                    if (UIUtil.isNullOrEmpty(connId)) {
                        domainRelationship = ecrObject.addToObject(context, new RelationshipType(JF_PLMConstants_mxJPO.REL_JFECRRelateRoot), subPartId);
                        domainRelationship.setAttributeValues(context, attributeMap);
                    }
                    if (ecrItemList.contains(partId)) {
                        continue;
                    }
                    //将父件的受影响件关系从ECR上移除
                    ecrObject.disconnect(context, new RelationshipType(JF_PLMConstants_mxJPO.REL_JFECRRelateRoot), true, new BusinessObject(partId));
                    //将父件关联到受影响件关系  JFRelateItem 并将变更来源设置为 ecr的变更来源
                    ecrObject.disconnect(context, new RelationshipType(JF_PLMConstants_mxJPO.REL_JFRELATEITEM), true, new BusinessObject(partId));
                    ecrItemList.add(partId);
                }
            }
            ContextUtil.commitTransaction(context);
        } catch (Exception e) {
            e.printStackTrace();
            ContextUtil.abortTransaction(context);
            throw e;
        }finally {
            ContextUtil.popContext(context);
        }
        _logger.info("-----------------------------------ecrSubPartIFReplaceProcess end ---------------------------------------------");
        return flag;
    }


    public void test(Context context ,String[] args) throws Exception{
        DomainObject ecr = DomainObject.newInstance(context, "35845.4994.54572.8459");
        String strName = ecr.getInfo(context, SELECT_NAME);
        StringList reSelectList = new StringList(SELECT_RELATIONSHIP_ID);
        reSelectList.add(SELECT_FROM_ID);
        StringList typeSelectList = new StringList(SELECT_ID);
        typeSelectList.add(SELECT_ATTR_JF_ProcurementType);
        MapList newECRAffectItemList = getNewECRAffectItemTableData(context, ecr, typeSelectList, reSelectList,"35845.4994.54572.8459");
        HashSet<Integer> removeIndexSet = new HashSet<>();
        _logger.info("newECRAffectItemList before:{}",newECRAffectItemList);
        for (int i = 0; i < newECRAffectItemList.size(); i++) {
            int nextLevel = findEndOfSubtree(newECRAffectItemList, i); // 找到以i为根的子树结束位置
            recursionFilterTree(newECRAffectItemList, i, nextLevel, removeIndexSet);
            i = nextLevel;
        }
        _logger.info("removeIndexSet:{}",removeIndexSet);
        for (Integer index :removeIndexSet){
            newECRAffectItemList.remove(index.intValue());
        }
        _logger.info("newECRAffectItemList after:{}",newECRAffectItemList);
    }


    /*
     * @description:ECR和受影响项目建立关系
     * @author: caipan
     * @date: 2025/5/6 14:11:35
     * @param: * @param[1] context
     * @param[2] args
     * @return:
     **/
    public void connectAffectedProject(Context context, String[] args) throws Exception {
        String ecrId = args[0];
        DomainObject ecrObj = DomainObject.newInstance(context, ecrId);
        try {
            ContextUtil.pushContext(context);
            StringList selList = JF_Util_mxJPO.basicBolistSel();
            StringList relList = JF_Util_mxJPO.basicRellistSel();
            Map<String, MapList> projectList = new HashMap(); //受影响项目和整椅节点的账号的ID
            String projectId = ecrObj.getInfo(context, "from[" + REL_JFChange2Project + "].to.id");
            // ECR已经关联受影响项目
            StringList affectProjectList = ecrObj.getInfoList(context, "from[" + REL_JFECR2AffectedProject + "].to.id");
            String where = "current==FROZEN";
            MapList maps = ecrObj.getRelatedObjects(context, REL_JFRelateItem, // relationship pattern
                    TYPE_VPMReference,                                    // object pattern
                    selList,                            // object selects
                    relList, // relationship selects
                    false,                                        // to direction
                    true,                                        // from direction
                    (short) 0,                                    // recursion level
                    where,                // object where clause
                    "",
                    (short) 0);
            Set affectedItemsIdSet = (Set) maps.stream().map(m -> {
                Map info = (Map) m;
                return info.get(SELECT_ID);
            }).collect(Collectors.toSet());
            _logger.info("affectedItemsIdSet:{}", affectedItemsIdSet);
            //ECR关联的受影响的冻结状态的对象，反查到整椅节点
            JF_Util_mxJPO util = new JF_Util_mxJPO();
            Iterator<String> it = affectedItemsIdSet.iterator();
            String[] array = new String[2];
            while (it.hasNext()) {
                String value = it.next();
                array[0] = value;
                array[1] = projectId;
                projectList.putAll(util.getAffectProject(context, array));
            }
            _logger.info("projectList:{}", projectList);
            RelationshipType ship = new RelationshipType(REL_JFECR2AffectedProject);
            //ECR实际关联受影响项目
            boolean flag = false;
            Set projectSet = new HashSet();
            for (String partId : projectList.keySet()) {
                MapList mapList = projectList.get(partId);
                for (int i = 0; i < mapList.size(); i++) {
                    Map info = (Map) mapList.get(i);
                    //todo 改成
                    String gId = UIUtil.getValue(info, SELECT_ID);
                   StringList pIdList = new JF_VPMReferenceEBOM_mxJPO().getPartZeroProject(context, gId);
//                    String pId = (String) info.get("to[" + JF_PLMConstants_mxJPO.rel_JFProject2RootPart + "].from.id");
                    for(String pId:pIdList) {
                        //同一个项目，不同的零件编号怎么处理 todo 拼接处理吗 两个关系属性,那table需要也一起处理
                        if (!affectProjectList.contains(pId) && !projectId.equalsIgnoreCase(pId) && UIUtil.isNotNullAndNotEmpty(pId) && !projectSet.contains(pId)) {
                            projectSet.add(pId);
                            _logger.info("成功进来:{}:{} : {}", pId, partId, gId);
                            DomainRelationship domainRelationship = ecrObj.addToObject(context, ship, pId);
                            domainRelationship.setAttributeValue(context, JF_PLMConstants_mxJPO.ATTR_JF_ECR2AffectedPartId, getObjectPartNumber(context, partId));
                            domainRelationship.setAttributeValue(context, JF_PLMConstants_mxJPO.ATTR_JF_ECR2AffectedGCPartId, getObjectPartNumber(context, gId));
                            flag = true;
                        }
                    }
                }
            }
            _logger.info("flag:{}", flag);
            // add by chenyan 新增移除受影响项目逻辑 2025/05/27
            _logger.info("affectProjectList:{}", affectProjectList);
            for (String strProjectId : affectProjectList) {
                //不包含表示新的结构已经没有该项目需要移除
                if (!projectSet.contains(strProjectId)) {
                    _logger.info("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
                    ecrObj.disconnect(context, ship, true, DomainObject.newInstance(context, strProjectId));
                }
            }
            if (flag) {
                ecrObj.setAttributeValue(context, "JFAssociatedOtherProjects", "Y");
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            ContextUtil.popContext(context);
        }
    }

    /**
    * 获取受影响零件变更来源
    * @param context
	* @param args
    * @author LIUJR
    * @throws
    * @return matrix.util.StringList
    * @date 2025/5/7 9:46
    * @description
    */
    public StringList getJFChangeResource(Context context, String[] args) throws Exception {
        _logger.info("------------------------------------------  getJFChangeResource  begin ------------------------------------------");
        Map paramsMap = (Map) JPO.unpackArgs(args);
        Map paramList = (Map) paramsMap.get("paramList");
        String strParentOID = (String) paramList.get("parentOID");
        Map columnMap = (Map) paramsMap.get("columnMap");
        String strColName = (String) columnMap.get("name");
        _logger.info("strColName:{}", strColName);
        StringList tableIdList = _getListOfKeys(args, SELECT_ID);
//        _logger.info("tableIdList:{}", tableIdList);
        StringList res = new StringList();
        StringList nlsRanges = new StringList();
        for (int i = 0; i < tableIdList.size(); i++) {
            Map map = new HashMap<>();
            map.put("relName", REL_JFRelateItem);
            map.put("fromId", strParentOID);
            map.put("toId", tableIdList.get(i));
            String connId = JF_PublicMethodClass_mxJPO.getTwoBusinessObjectConnId(context, JPO.packArgs(map));
            if (UIUtil.isNullOrEmpty(connId)) {
                res.add(EMPTY_STRING);
            } else {
                DomainRelationship rel = DomainRelationship.newInstance(context, connId);
                String strChangeResource = "";
                String strTmpAttr = "";
                if ("JFIsFollow".equals(strColName)) {
                    strTmpAttr = ATTR_JFIsFollow;
                } else if ("JFChangeResource".equals(strColName)) {
                    strTmpAttr = ATTR_JFCHANGESOURCE;
                }
                strChangeResource = rel.getAttributeValue(context, strTmpAttr);
                nlsRanges = EnoviaResourceBundle.getAttrRangeI18NStringList(context, strTmpAttr, StringList.create(strChangeResource), context.getLocale().toString());
                if (nlsRanges.size() > 0) {
                    res.add(nlsRanges.get(0));
                } else {
                    res.add(strChangeResource);
                }
            }
        }
        return res;
    }

    /**
    * 修改New ECR的变更来源  是否跟随发布  关系属性
    * @param context
	* @param args
    * @author LIUJR
    * @throws
    * @return void
    * @date 2025/5/7 10:18
    * @description
    */
    public static void updateNewECRAttrValue(Context context, String[] args) throws Exception {
        try {
            Map paramsMap = (Map) JPO.unpackArgs(args);
            Map columnMap = (Map) paramsMap.get(STRING_COLUMNMAP);
            String attrName = (String) columnMap.get(DomainConstants.SELECT_NAME);
            if(attrName.equalsIgnoreCase("JFChangeResource")){
                attrName = "JFChangeSource";
            }
            HashMap paramMap = (HashMap)paramsMap.get(STRING_PARAMMAP);
            String objectId = (String)paramMap.get(STRING_OBJECTID);
            Map requestMap = (Map) paramsMap.get(STRING_REQUESTMAP);
            String strParentOID = (String) requestMap.get("parentOID");
            String newValue = (String)paramMap.get(STRING_NEW_VALUE);
            Map map = new HashMap<>();
            map.put("relName", REL_JFRelateItem);
            map.put("fromId", strParentOID);
            map.put("toId", objectId);
            String connId = JF_PublicMethodClass_mxJPO.getTwoBusinessObjectConnId(context, JPO.packArgs(map));
            if (UIUtil.isNotNullAndNotEmpty(connId)) {
                DomainRelationship rel = DomainRelationship.newInstance(context, connId);
                rel.setAttributeValue(context, attrName, newValue);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
    *
    *@description 获取ECR受影响项目的整椅经理
    *@param context
	*@param strECRId
    *@return java.util.Set<java.lang.String>
    *@throws
    *@author CHENYAN
    *@date 2025/5/7 11:38
    */
    public Set<String> getAffectedProjectWholeChairPerson(Context context ,String strECRId) throws Exception{
        HashSet<String> res = new HashSet<>();
        try {
            ContextUtil.pushContext(context);
            String strMqlRes = MqlUtil.mqlCommand(context, "print bus $1 select $2 dump $3 ", strECRId, "from[JFECR2AffectedProject].to.from[Member|attribute[Project Role]=='Chair manager'].to.id", " @");
            if (UIUtil.isNotNullAndNotEmpty(strMqlRes)) {
                String[] split = strMqlRes.split("@");
                for (int i = 0; i < split.length; i++) {
                    res.add(split[i].trim());
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            ContextUtil.popContext(context);
        }
        return res ;
    }

    /**
    *
    *@description 会签状态生成会签任务和ECO
    *@param context
	*@param args
    *@return void
    *@throws
    *@author CHENYAN
    *@date 2025/5/7 16:53
    */
    public void createCountersignTaskAndECOInCountersign(Context context, String[] args) throws Exception {
        _logger.info("---------------------------------- createCountersignTaskAndECOInCountersign begin -------------------------------------------");
        try {
            ContextUtil.startTransaction(context,true);
            //add by ljr 20240801 生成会签任务
            JF_SignTask_mxJPO jfSignTaskMxJPO = new JF_SignTask_mxJPO();
            jfSignTaskMxJPO.newECRGenerateCountersignatureTask(context, args);
            //end
            //add by lsa 20240805 生成ECO
            JF_ChangeExecutionECOSource_mxJPO ecoMxJPO = new JF_ChangeExecutionECOSource_mxJPO();
            ecoMxJPO.createECOObject(context, args);
            //end
            ContextUtil.commitTransaction(context);
        } catch (Exception e) {
            ContextUtil.abortTransaction(context);
            throw new RuntimeException(e);
        }
        //20260827 update by codex caipan 会签创建完成后异步同步正式ECR的最终制造件清单
        try {
            String ecrId = args[0];
            DomainObject ecrObject = DomainObject.newInstance(context, ecrId);
            String ecrType = ecrObject.getInfo(context, SELECT_TYPE);
            if (TYPE_JFFormalECR.equalsIgnoreCase(ecrType)) {
                JF_Util_mxJPO.runAsyncWithJsonResult(context, new String[]{ecrId}, "JF_BIInterface",
                        "syncECRConnectMakePartList");
            }
        } catch (Exception e) {
            _logger.error("正式ECR制造件清单异步任务提交异常，不影响会签任务和ECO创建", e);
        }
        _logger.info("---------------------------------- createCountersignTaskAndECOInCountersign end -------------------------------------------");

    }

    /**
    *
    *@description 获取两个表格的价格关系属性集合
    *@param strTableName
    *@return matrix.util.StringList
    *@throws
    *@author CHENYAN
    *@date 2025/5/8 14:30
    */
    public static  StringList getPriceAttrListByTableName(String strTableName,String strECRId ,String strECRType){
        StringList attrList = new StringList();
        String strPreMql = "" ;
        if (strTableName.contains("JFNewECRCosting") || strTableName.contains("JFECRCosting")) {
            if (TYPE_JFNewECR.equals(strECRType)){
                strPreMql  = "tomid[JFECR2PartPrice|from.id==" + strECRId;
            }else if (TYPE_JFECR.equals(strECRType)){
                strPreMql  = "to[JFECR2PartPrice|from.id==" + strECRId;
            }
            attrList.add(strPreMql + "].id");
            attrList.add(strPreMql + "].attribute[JFChangeUnitPrice]");
            attrList.add(strPreMql + "].attribute[JFChangeMold]");
            attrList.add(strPreMql + "].attribute[JFChangeUnitPriceExternal]");
            attrList.add(strPreMql + "].attribute[JFChangeMoldCostExternal]");
            attrList.add(strPreMql + "].attribute[JFChangeUnitPriceCost]");
            attrList.add(strPreMql + "].attribute[JFChangeMoldCost]");
            attrList.add(strPreMql + "].attribute[JFChangeUnitPriceCostExternal]");
            attrList.add(strPreMql + "].attribute[JFChangeMoldPriceCostExternal]");
            attrList.add(strPreMql + "].attribute[JFStagnationOfSuppliersInternal]");
            attrList.add(strPreMql + "].attribute[JFStagnationOfSuppliersExternal]");
            attrList.add(strPreMql + "].attribute[JFChangesTrialExpensesManHours]");
            attrList.add(strPreMql + "].attribute[JFChangesTrialExpensesManHoursExternal]");
        }else if (strTableName.contains("JFNewECRController") || strTableName.contains("JFECRController")) {
            if (TYPE_JFNewECR.equals(strECRType)){
                strPreMql = "tomid[JFECR2MakePartPrice|from.id==" + strECRId;
            }else if (TYPE_JFECR.equals(strECRType)){
                strPreMql = "to[JFECR2MakePartPrice|from.id==" + strECRId;
            }
            attrList.add(strPreMql + "].id");
            attrList.add(strPreMql + "].attribute[JFChangeMan-hour]");
            attrList.add(strPreMql + "].attribute[JFChangeMan-hourExternal]");
            attrList.add(strPreMql + "].attribute[JFChangeSeatCost]");
            attrList.add(strPreMql + "].attribute[JFChangeSeatCostExternal]");
            attrList.add(strPreMql + "].attribute[JFChangeTargetPrice]");
            attrList.add(strPreMql + "].attribute[JFChangeTargetPriceExternal]");
            attrList.add(strPreMql + "].attribute[JFChangeMold]");
            attrList.add(strPreMql + "].attribute[JFChangeMoldCostExternal]");
            attrList.add(strPreMql + "].attribute[JFChangeMoldCost]");
            attrList.add(strPreMql + "].attribute[JFChangeMoldPriceCostExternal]");
            attrList.add(strPreMql + "].attribute[JFChangeUnitPriceCost]");
            attrList.add(strPreMql + "].attribute[JFChangeUnitPriceCostExternal]");
            attrList.add(strPreMql + "].attribute[JFChangeWholeSeatPrice]");
            attrList.add(strPreMql + "].attribute[JFChangeWholeSeatPriceExternal]");
            attrList.add(strPreMql + "].attribute[JFWholeChair]");
        }
        return attrList;
    }

    /**
    *  20251225 去除校验
     * 	2. NewECR变更类型为【首次发放】时校验受影响零件清单第一层级必须全部为AA版，
     * 	   反过来，受影响零件清单全部为AA版时变更类型应该为【首次发放】
     * 	3. 这个里面的逻辑是所有的冻结状态的受影响对象， 发布的状态，如果不是AA版本，忽略掉，如果不满足提示，不让发起流程
     * 	4. NewECR校验的时候，只关注受影响的件状态是冻结的
    * @param context
	* @param args
    * @author LIUJR
    * @throws
    * @return int
    * @date 09/05/2025 10:02
    * @description
    */
    public int checkNewECRAffectedItemsRevision(Context context, String[] args) throws Exception {
        _logger.info("------------------------ checkECRHasDocument begin  -----------------------------------");
        String strObjectId = args[0];
        String strCurrent = args[1];
        String strNextState = args[2];
        _logger.info("strObjectId:{}", strObjectId);
        _logger.info("strCurrent:{}", strCurrent);
        _logger.info("strNextState:{}", strNextState);
        DomainObject ecr = DomainObject.newInstance(context, strObjectId);
        String strJFChangeType = ecr.getAttributeValue(context, "JFECRChangeType");
        _logger.info("strJFChangeType:{}", strJFChangeType);
        StringList reSelectList = new StringList(SELECT_RELATIONSHIP_ID);
        StringList typeSelectList = new StringList(SELECT_ID);
        typeSelectList.add(SELECT_REVISION);
        int iRes = 0;
        MapList mapList = new MapList();
        mapList = ecr.getRelatedObjects(context, JF_PLMConstants_mxJPO.REL_JFECRRelateRoot, // relationship pattern
                JF_PLMConstants_mxJPO.TYPE_VPMReference,                                    // object pattern
                typeSelectList,                            // object selects
                reSelectList, // relationship selects
                false,                                        // to direction
                true,                                        // from direction
                (short) 1,                                    // recursion level
                "current==FROZEN",                // object where clause
                "",
                (short) 0);

        if (!mapList.isEmpty()) {
            /*
            * NewECR变更类型为【首次发放】时校验受影响零件清单必须全部为AA版，
            * 反过来，受影响零件清单全部为AA版时变更类型应该为【首次发放】
            * */
            //零件的policy的第一个版本
            Policy policy = new Policy("VPLM_SMB_Definition_MajorRev");
//            String firstRevision = policy.getFirstInSequence(context);
            String firstRevision = "AA";
            _logger.info("firstRevision:{}", firstRevision);
            //获取受影响项中的零件是否全部是AA 版本
            MapList firstRevisionMapList = (MapList) mapList.stream().filter(m -> {
                Map map = (Map) m;
                String revision = UIUtil.getValue(map, SELECT_REVISION);
                if (revision.contains(firstRevision)) {
                    return true;
                } else {
                    return false;
                }
            }).collect(Collectors.toCollection(MapList::new));
            _logger.info("firstRevisionMapList:{}", firstRevisionMapList);
            //如果是相等  那都是首版 需要校验
            String strMess = EMPTY_STRING;
            if (firstRevisionMapList.size() == mapList.size()) {
                if (!"First Distribution".equalsIgnoreCase(strJFChangeType)) {
                    //受影响项是首版  变更类型不是首版
                    strMess = EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", context.getLocale(), "emxComponents.NewECR.AffectedItemsRevision1");
                }
            } else if ("First Distribution".equalsIgnoreCase(strJFChangeType)) {
                //受影响项不是首版  变更类型是首版
                strMess = EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", context.getLocale(), "emxComponents.NewECR.AffectedItemsRevision2");
            }
            _logger.info("strMess:{}", strMess);
            if (UIUtil.isNotNullAndNotEmpty(strMess)) {
                iRes = 1;
                emxContextUtilBase_mxJPO.mqlWarning(context, strMess);
            }
        }
        _logger.info("------------------------ checkECRHasDocument end  -----------------------------------");
        return iRes;
    }


    /**
     * @param context
     * @param args
     * @return void
     * @throws
     * @description 修改ECR FORM属性
     * @author CHENYAN
     * @date 2024/8/9 0:31
     */
    public void updateNewECRFormField(Context context, String[] args) throws Exception {
        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        Map paramMap = (Map) programMap.get("paramMap");
        Map fieldMap = (Map) programMap.get("fieldMap");
        String strNewId = (String) paramMap.get("New OID");
        String strNewValue = (String) paramMap.get("New Value");
        String strFieldName = (String) fieldMap.get("name");
        String strObjectId = (String) paramMap.get("objectId");
        _logger.info("strNewId:{}", strNewId);
        _logger.info("strNewValue:{}", strNewValue);
        _logger.info("strFieldName:{}", strFieldName);
        _logger.info("strObjectId:{}", strObjectId);
        try {
            ContextUtil.pushContext(context);
            ContextUtil.startTransaction(context, true);
            DomainObject ecr = DomainObject.newInstance(context, strObjectId);
            String current = ecr.getInfo(context, SELECT_CURRENT);
            if ("JFProjectName".equals(strFieldName)) {
                String strRelId = ecr.getInfo(context, "from[JFChange2Project].id");
                if (UIUtil.isNotNullAndNotEmpty(strRelId)) {
                    DomainRelationship.setToObject(context, strRelId, DomainObject.newInstance(context, strNewId));
                }
            } else if ("JFChangeEventType".equals(strFieldName)) {
                String strRelId = ecr.getInfo(context, "to[JFChangeEventECR].id");
                _logger.info("strRelId:{}", strRelId);
                if (UIUtil.isNotNullAndNotEmpty(strRelId)) {
                    if (UIUtil.isNotNullAndNotEmpty(strNewId)) {
                        DomainRelationship.setFromObject(context, strRelId, DomainObject.newInstance(context, strNewId));
                    } else {
                        //断开关系
                        DomainRelationship.disconnect(context, strRelId);
                    }
                } else {
                    DomainRelationship.connect(context, DomainObject.newInstance(context, strNewId), "JFChangeEventECR", ecr);
                }
            } else if ("JFChangesDeveExpensesManHours".equals(strFieldName) || "JFChangesTrialExpensesManHours".equals(strFieldName) || "JFAffectsFactory".equals(strFieldName)
                    || "JFChangesDeveExpensesManHoursExternal".equals(strFieldName) || "JFChangesTrialExpensesManHoursExternal".equals(strFieldName) || "JFAssociatedOtherProjects".equals(strFieldName)
            ) {
                //列名和属性名不一致
                if ("JFAffectsFactory".equals(strFieldName)) {
                    String[] strNewValues = (String[]) paramMap.get("New Values");
                    String strJFAffectsFactory = "";
                    Attribute attributeValues = ecr.getAttributeValues(context, ATTR_JFAFFECTEDFACTORY);
                    StringList newValueList = new StringList();
                    if (null != strNewValues && strNewValues.length > 0) {
                        newValueList = StringList.create(strNewValues);
                        _logger.info("newValueList:{}", newValueList);
                    }
                    _logger.info("newValueList:{}", newValueList.size());
                    if (newValueList.size() > 0) {
                        //为空时传入数组有空串
                        if (newValueList.size() == 1 && newValueList.contains("")) {
                            strJFAffectsFactory = "\"\"";
                        } else {
                            strJFAffectsFactory = newValueList.join(",");
                        }
                    } else {
                        strJFAffectsFactory = "\"\"";
                    }
                    _logger.info("strJFAffectsFactory:{}", strJFAffectsFactory);
                    if("Create".equalsIgnoreCase(current)){
                        String strMql = JF_PublicMethodClass_mxJPO.buildStringInStrings("mod bus ", strObjectId, " ", ATTR_JFAFFECTEDFACTORY, " ", strJFAffectsFactory, ";");
                    _logger.info("strMql:{}", strMql);
                    MqlUtil.mqlCommand(context, strMql);
                    }
//                         ecr.setAttributeValue(context,"JFAffectedFactory",strJFAffectsFactory);
                } else {
                    String strCurrent = ecr.getInfo(context, SELECT_CURRENT);
                    if (!(
                            "Review".equals(strCurrent) &&
                                    ("JFChangesDeveExpensesManHoursExternal".equals(strFieldName) || "JFChangesDeveExpensesManHours".equals(strFieldName))
                    )) {
                        ecr.setAttributeValue(context, strFieldName, strNewValue);
                    }
                }
            } else if ("ManagerReview".equals(strFieldName)) {
                String strRelId = ecr.getInfo(context, "from[JFECR2Person].id");
                if (UIUtil.isNotNullAndNotEmpty(strRelId)) {
                    if (UIUtil.isNotNullAndNotEmpty(strNewId)) {
                        DomainRelationship.setToObject(context, strRelId, DomainObject.newInstance(context, strNewId));
                    } else {
                        DomainRelationship.disconnect(context, strRelId);
                    }
                } else {
                    if (UIUtil.isNotNullAndNotEmpty(strNewId)) {
                        DomainRelationship.connect(context, ecr, "JFECR2Person", DomainObject.newInstance(context, strNewId));
                    }
                }
            }
            ContextUtil.commitTransaction(context);
        } catch (Exception e) {
            ContextUtil.abortTransaction(context);
            _logger.error(e.getMessage());
            throw e;
        } finally {
            ContextUtil.popContext(context);
        }

    }
    /**
     * @param context
     * @param args
     * @return boolean
     * @throws
     * @description 获取ECR 编辑Table的各个列的编辑权限
     * @author CHENYAN
     * @date 2024/7/30 13:47
     */
    public boolean getNewECRFormEditAccess(Context context, String[] args) throws Exception {
        Map formSettingMap = JPO.unpackArgs(args);
        Map fieldMap = (Map) formSettingMap.get("field");
        Map requestMap = (Map) formSettingMap.get("requestMap");
        String strFieldName = (String) fieldMap.get("name");
        String strObjectId = (String) requestMap.get("objectId");
        DomainObject ecr = DomainObject.newInstance(context, strObjectId);
        StringList typeSelectList = new StringList();
        typeSelectList.add(SELECT_CURRENT);
        typeSelectList.add(SELECT_OWNER);
        typeSelectList.add("from[JFChange2Project].to.id");
        String strLoginUser = context.getUser();
        Map filedMap = ecr.getInfo(context, typeSelectList);
        String strECROwner = (String) filedMap.get(SELECT_OWNER);
        String strCurrent = (String) filedMap.get(SELECT_CURRENT);
        String strProjectId = (String) filedMap.get("from[JFChange2Project].to.id");
        boolean res = false;
        switch (strFieldName) {
            case "JFProjectName", "JFProjectPhase", "Title", "JFECRChangeType","JFCustomerChangeNum","JFExpectedLaunchTime",
                    "JFChangeSource", "JFIsPlatformPart", "JFAffectsFactory", "JFChangeEventType",
                    "JFChangesDeveExpensesManHours", "JFChangesTrialExpensesManHours", "JFChangesDeveExpensesManHoursExternal", "JFChangesTrialExpensesManHoursExternal",
                    "JFChangeReson", "Description", "ManagerReview", "WCharDevEngineering", "JFAssociatedOtherProjects","JFIsLastQuote","JFIsTKOData": {
                if (strECROwner.equals(strLoginUser) && ("Create".equals(strCurrent))) {
                    res = true;
                }
                break;
            }
            case "JFBreakpointMode", "JFECRType": {
                String strProjectManager = JF_Util_mxJPO.getProjectManager(context, new String[]{strProjectId});
                if (strProjectManager.equals(strLoginUser) && "Review".equals(strCurrent)) {
                    _logger.info("@@@@@@@@@@@@@@@@@@@@@");
                    res = true;
                }
                break;
            }
        }
        return res;
    }

    /**
     * @param context
     * @param args
     * @return java.lang.String
     * @throws
     * @description 构造ECR中联动列
     * @author CHENYAN
     * @date 2024/8/9 0:30
     */
    public String buildNewECRFormLinkageAttributeHtml(Context context, String[] args) throws Exception {
        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        HashMap requestMap = (HashMap) programMap.get("requestMap");
        HashMap fieldMap = (HashMap) programMap.get("fieldMap");
        String strFieldName = (String) fieldMap.get("name");
        String strMode = (String) requestMap.get("mode");
        String strObjectId = (String) requestMap.get("objectId");
        if ("view".equals(strMode)) {
            DomainObject ecr = DomainObject.newInstance(context, strObjectId);
            if ("JFAffectsFactory".equals(strFieldName)) {
                Attribute attributeValues = ecr.getAttributeValues(context, ATTR_JFAFFECTEDFACTORY);
                StringList valueList = attributeValues.getValueList();
                StringList rangeNlsList = i18nNow.getAttrRangeI18NStringList("JFAffectedFactory", valueList, context.getSession().getLanguage());
                return rangeNlsList.join("<br/>");
            } else if ("JFChangesDeveExpensesManHours".equals(strFieldName)) {
                return ecr.getInfo(context, SELECT_ATTR_JFCHANGESDEVEEXPENSESMANHOURS);

            } else if ("JFChangesTrialExpensesManHours".equals(strFieldName)) {
                return ecr.getInfo(context, SELECT_ATTR_JFCHANGESTRIALEXPENSESMANHOURS);

            } else if ("JFChangesDeveExpensesManHoursExternal".equals(strFieldName)) {
                return ecr.getInfo(context, SELECT_ATTR_JFCHANGESDEVEEXPENSESMANHOURSEXTERNAL);

            } else if ("JFChangesTrialExpensesManHoursExternal".equals(strFieldName)) {
                return ecr.getInfo(context, SELECT_ATTR_JFCHANGESTRIALEXPENSESMANHOURSEXTERNAL);
            }
        } else if ("edit".equals(strMode)) {
            boolean isReadOnly = false;
            boolean isRequire = false;
            String strFieldValue = "";
            String strFieldNls = "";
            DomainObject ecr = DomainObject.newInstance(context, strObjectId);
            //判断是否可以编辑
            StringList ecrSelectList = new StringList();
            ecrSelectList.add(SELECT_CURRENT);
            ecrSelectList.add(SELECT_OWNER);
            Map ecrInfo = ecr.getInfo(context, ecrSelectList);
            String strCurrent = (String) ecrInfo.get(SELECT_CURRENT);
            String strOwner = (String) ecrInfo.get(SELECT_OWNER);
            String strLoginUser = context.getUser();
            if (strLoginUser.equals(strOwner) && "Create".equals(strCurrent)) {
                if ("JFAffectsFactory".equals(strFieldName)) {
                    StringList typeSelectList = new StringList();
                    typeSelectList.add(SELECT_ATTR_JFISPLATFORMPART);
                    typeSelectList.add(SELECT_ATTR_JFAFFECTEDFACTORY);
                    Map ecrMap = ecr.getInfo(context, typeSelectList);
                    String strIsPlatformPart = (String) ecrMap.get(SELECT_ATTR_JFISPLATFORMPART);
                    Attribute attributeValues = ecr.getAttributeValues(context, ATTR_JFAFFECTEDFACTORY);
                    StringList valueList = attributeValues.getValueList();
                    strFieldValue = valueList.join(",");
                    isReadOnly = "No".equals(strIsPlatformPart);
                    isRequire = !isReadOnly;
                } else if ("JFChangesDeveExpensesManHours".equals(strFieldName)) {
                    StringList typeSelectList = new StringList();
                    typeSelectList.add(SELECT_ATTR_JFCHANGESOURCE);
                    typeSelectList.add(SELECT_ATTR_JFCHANGESDEVEEXPENSESMANHOURS);
                    Map ecrMap = ecr.getInfo(context, typeSelectList);
                    String strChangeSource = (String) ecrMap.get(SELECT_ATTR_JFCHANGESOURCE);
                    strFieldValue = (String) ecrMap.get(SELECT_ATTR_JFCHANGESDEVEEXPENSESMANHOURS);
                    isReadOnly = false;
                    isRequire =false;
                } else if ("JFChangesTrialExpensesManHours".equals(strFieldName)) {
                    StringList typeSelectList = new StringList();
                    typeSelectList.add(SELECT_ATTR_JFCHANGESOURCE);
                    typeSelectList.add(SELECT_ATTR_JFCHANGESTRIALEXPENSESMANHOURS);
                    Map ecrMap = ecr.getInfo(context, typeSelectList);
                    String strChangeSource = (String) ecrMap.get(SELECT_ATTR_JFCHANGESOURCE);
                    strFieldValue = (String) ecrMap.get(SELECT_ATTR_JFCHANGESTRIALEXPENSESMANHOURS);
                    isReadOnly = "External Changes".equals(strChangeSource);
                    isRequire = !isReadOnly;
                } else if ("JFChangesDeveExpensesManHoursExternal".equals(strFieldName)) {
                    StringList typeSelectList = new StringList();
                    typeSelectList.add(SELECT_ATTR_JFCHANGESOURCE);
                    typeSelectList.add(SELECT_ATTR_JFCHANGESDEVEEXPENSESMANHOURSEXTERNAL);
                    Map ecrMap = ecr.getInfo(context, typeSelectList);
                    String strChangeSource = (String) ecrMap.get(SELECT_ATTR_JFCHANGESOURCE);
                    strFieldValue = (String) ecrMap.get(SELECT_ATTR_JFCHANGESDEVEEXPENSESMANHOURSEXTERNAL);
                    isReadOnly = false;
                    isRequire = false;
                } else if ("JFChangesTrialExpensesManHoursExternal".equals(strFieldName)) {
                    StringList typeSelectList = new StringList();
                    typeSelectList.add(SELECT_ATTR_JFCHANGESOURCE);
                    typeSelectList.add(SELECT_ATTR_JFCHANGESTRIALEXPENSESMANHOURSEXTERNAL);
                    Map ecrMap = ecr.getInfo(context, typeSelectList);
                    String strChangeSource = (String) ecrMap.get(SELECT_ATTR_JFCHANGESOURCE);
                    strFieldValue = (String) ecrMap.get(SELECT_ATTR_JFCHANGESTRIALEXPENSESMANHOURSEXTERNAL);
                    isReadOnly = "Internal Changes".equals(strChangeSource);
                    isRequire = !isReadOnly;
                }
                strFieldNls = EnoviaResourceBundle.getAttributeI18NString(context, strFieldName, context.getLocale().toString());
                return JF_ECRService_mxJPO.buildFormFieldHtml(context, strFieldName, strFieldValue, strFieldNls, isReadOnly, isRequire);
            } else {
                if ("JFAffectsFactory".equals(strFieldName)) {
                    Attribute attributeValues = ecr.getAttributeValues(context, ATTR_JFAFFECTEDFACTORY);
                    StringList valueList = attributeValues.getValueList();
                    StringList rangeNlsList = i18nNow.getAttrRangeI18NStringList("JFAffectedFactory", valueList, context.getSession().getLanguage());
                    return rangeNlsList.join("<br/>");
                } else if ("JFChangesDeveExpensesManHours".equals(strFieldName)) {
                    return ecr.getInfo(context, SELECT_ATTR_JFCHANGESDEVEEXPENSESMANHOURS);
                } else if ("JFChangesTrialExpensesManHours".equals(strFieldName)) {
                    return ecr.getInfo(context, SELECT_ATTR_JFCHANGESTRIALEXPENSESMANHOURS);

                } else if ("JFChangesDeveExpensesManHoursExternal".equals(strFieldName)) {
                    return ecr.getInfo(context, SELECT_ATTR_JFCHANGESDEVEEXPENSESMANHOURSEXTERNAL);

                } else if ("JFChangesTrialExpensesManHoursExternal".equals(strFieldName)) {
                    return ecr.getInfo(context, SELECT_ATTR_JFCHANGESTRIALEXPENSESMANHOURSEXTERNAL);
                }
            }
        }
        return "";
    }

    /**
     * @param context
     * @param args
     * @return java.lang.String
     * @throws
     * @description 构造ECR 项目和变更事件 属性列  已失效
     * @author CHENYAN
     * @date 2024/7/31 9:42
     */
    @Deprecated
    @com.matrixone.apps.framework.ui.ProgramCallable
    public String buildNewECRProjectName(Context context, String[] args) throws Exception {
        String strLoginUser = context.getUser();
        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        _logger.info("programMap:{}", programMap);
        HashMap requestMap = (HashMap) programMap.get("requestMap");
        HashMap fieldMap = (HashMap) programMap.get("fieldMap");
        String strFieldName = (String) fieldMap.get("name");
        String strMode = (String) requestMap.get("mode");
        String strObjectId = (String) requestMap.get("objectId");
        String strLanguage = context.getSession().getLanguage();
        DomainObject ecr = DomainObject.newInstance(context, strObjectId);
        StringList typeSelectList = new StringList();
        String strSearchUrl = "";
        if ("JFProjectName".equals(strFieldName)) {
            strSearchUrl = "../common/emxFullSearch.jsp?field=TYPES=type_ProjectSpace:CURRENT=policy_ProjectSpace.state_Active&cancelLabel=emxFramework.Common.Close&HelpMarker=emxhelpselectorganization&table=ENCAddExistingGeneralSearchResults&selection=single&showInitialResults=true&submitURL=./AEFSearchUtil.jsp";
            typeSelectList.add("from[JFChange2Project].to.name");
            typeSelectList.add("from[JFChange2Project].to.id");
        } else if ("JFChangeEventType".equals(strFieldName)) {
            strSearchUrl = "../common/emxFullSearch.jsp?field=TYPES=type_JFChangeEventType&cancelLabel=emxFramework.Common.Close&table=ENCAddExistingGeneralSearchResults&selection=single&submitURL=./AEFSearchUtil.jsp";
            typeSelectList.add("to[JFChangeEventECR].from.id");
            typeSelectList.add("to[JFChangeEventECR].from.name");
        }
        typeSelectList.add(SELECT_CURRENT);
        typeSelectList.add(SELECT_OWNER);
        Map ecrMap = ecr.getInfo(context, typeSelectList);
        String strBuildName = "";
        String strBuildId = "";
        if ("JFProjectName".equals(strFieldName)) {
            strBuildName = (String) ecrMap.get("from[JFChange2Project].to.name");
            strBuildId = (String) ecrMap.get("from[JFChange2Project].to.id");
        } else if ("JFChangeEventType".equals(strFieldName)) {
            strBuildId = (String) ecrMap.get("to[JFChangeEventECR].from.id");
            strBuildName = (String) ecrMap.get("to[JFChangeEventECR].from.name");
        }
        String strECROwner = (String) ecrMap.get(SELECT_OWNER);
        String strCurrent = (String) ecrMap.get(SELECT_CURRENT);
        boolean hasEditAccess = false;
        if (strECROwner.equals(strLoginUser) && ("Create".equals(strCurrent) || "Submit".equals(strCurrent))) {
            hasEditAccess = true;
        }
        return JF_PublicMethodClass_mxJPO.buildFieldHtml(context, strMode, strFieldName, strBuildId, strBuildName, strSearchUrl, hasEditAccess);
    }

    public String buildECRQQHtml(Context context, String[] args) throws Exception {
        _logger.info("--------------------- buildECRQQHtml begin --------------------------");
        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        HashMap requestMap = (HashMap) programMap.get("requestMap");
        HashMap fieldMap = (HashMap) programMap.get("fieldMap");
        String strMode = (String) requestMap.get("mode");
        String strLoginUser = context.getUser();
        String strLang = context.getSession().getLanguage();
        String strFileUploadNls = ComponentsUtil.i18nStringNow("emxComponents.Common.JFECRQQFileUpload", strLang);
        String strChangeSource = "";
        String strIsPlatForm = "";
        String objectId = null;
        if (requestMap == null) {
            objectId = (String) programMap.get("objectId");
        } else {
            objectId = (String) requestMap.get("objectId");
        }
        StringBuffer sbDocDown = new StringBuffer();

        _logger.info("objectId:{}", objectId);
        if (UIUtil.isNotNullAndNotEmpty(objectId)) {
            DomainObject ECR = DomainObject.newInstance(context, objectId);
            StringList ECRSelectList = new StringList();
            ECRSelectList.add(SELECT_ATTR_JFQQID);
            ECRSelectList.add(SELECT_CURRENT);
            ECRSelectList.add(SELECT_OWNER);
            ECRSelectList.add(SELECT_ATTR_JFCHANGESOURCE);
            ECRSelectList.add(SELECT_ATTR_JFISPLATFORMPART);
            Map ECRInfoMap = ECR.getInfo(context, ECRSelectList);
            String strQQID = (String) ECRInfoMap.get(SELECT_ATTR_JFQQID);
            String strCurrent = (String) ECRInfoMap.get(SELECT_CURRENT);
            String strOwner = (String) ECRInfoMap.get(SELECT_OWNER);
            strChangeSource = (String) ECRInfoMap.get(SELECT_ATTR_JFCHANGESOURCE);
            strChangeSource="";
            strIsPlatForm = (String) ECRInfoMap.get(SELECT_ATTR_JFISPLATFORMPART);
            boolean isEdit = strLoginUser.equals(strOwner) && "Create".equals(strCurrent) ? true : false;
            StringList selectTypeList = new StringList();
            selectTypeList.add(SELECT_ID);
            selectTypeList.add(SELECT_ATTRIBUTE_TITLE);
            selectTypeList.add(SELECT_TYPE);
            selectTypeList.add(SELECT_NAME);
            selectTypeList.add(SELECT_REVISION);
            MapList docList = ECR.getRelatedObjects(context, RELATIONSHIP_REFERENCE_DOCUMENT, // relationship pattern
                    TYPE_DOCUMENT,                                    // object pattern
                    selectTypeList,                            // object selects
                    new StringList(SELECT_RELATIONSHIP_ID), // relationship selects
                    false,                                        // to direction
                    true,                                        // from direction
                    (short) 1,                                    // recursion level
                    "",                // object where clause
                    "attribute[Project Role]=='QQ'",
                    (short) 0);
            StringList docIDList = (StringList) docList.stream().map(m -> {
                Map doc = (Map) m;
                return doc.get(SELECT_ID);
            }).collect(Collectors.toCollection(StringList::new));
            sbDocDown.append("<table");
            sbDocDown.append("><tr>");
            sbDocDown.append("<td>");
            strQQID = StringEscapeUtils.escapeHtml4(strQQID);
            strFileUploadNls = StringEscapeUtils.escapeHtml4(strFileUploadNls);
            if ("view".equals(strMode)) {
                sbDocDown.append(JF_PublicMethodClass_mxJPO.buildStringInStrings("<span>", strQQID, "</span>&nbsp;"));
            } else if ("edit".equals(strMode)) {
                if (isEdit) {
                    sbDocDown.append("<input value=\"" + strQQID + "\" id=\"JFQQ\" name=\"JFQQ\" type=\"text\"  size=\"20\">");
                } else {
                    sbDocDown.append(JF_PublicMethodClass_mxJPO.buildStringInStrings("<span>", strQQID, "</span>&nbsp;"));
                }
            }
            sbDocDown.append("</td>");
            StringList QQFileIdList = new StringList();
            if (isEdit && "edit".equals(strMode)) {
                sbDocDown.append("<td id=\"fileTd\">");
                sbDocDown.append("<input  id=\"JFECRQQFileId\" name=\"JFECRQQFileId\" value=\"" + docIDList.join(",") + "\" type=\"hidden\">");
                sbDocDown.append("<input onclick=\"javascript:showModalDialog('../components/JF_ECRQQFilePreCheckin.jsp?objectAction=checkin&msfBypass=true&");
                sbDocDown.append("objectId=");
                sbDocDown.append("");
                sbDocDown.append("','730','450')\"" +
                        "        value=\"");
                sbDocDown.append(strFileUploadNls);
                sbDocDown.append("\" type=\"button\">");
                sbDocDown.append("</td>");
            }
            sbDocDown.append("<td id=\"JFReplace\">");
            //Show Counter Link
            for (int i = 0; i < docList.size(); i++) {
                sbDocDown.append("<div ");
                Map docMap = (Map) docList.get(i);
                Map docObjMap = new HashMap<>();
                String strFileId = (String) docMap.get(SELECT_ID);
                QQFileIdList.add(strFileId);
                sbDocDown.append("style='vertical-align:middle;padding-left:1px;cursor:pointer;display:inline-block' ");
                sbDocDown.append("onClick=\"javascript:callCheckout('").append(strFileId).append("',");
                sbDocDown.append("'download', '', '', 'null', 'null', 'structureBrowser', 'PMCPendingDeliverableSummary', 'null')\">");
                sbDocDown.append("<img style='vertical-align:middle;' src='../common/images/").append("iconSmallDocument.gif").append("'");
                sbDocDown.append(" title=\"");
                String strDocTitle = (String) docMap.get(SELECT_ATTRIBUTE_TITLE);
                strDocTitle = StringEscapeUtils.escapeHtml4(strDocTitle);
                sbDocDown.append(strDocTitle);
                sbDocDown.append("\" />");
                sbDocDown.append("</div>");
            }
            sbDocDown.append("</td>");
        } else {
            sbDocDown.append("<table");
            sbDocDown.append("><tr>");
            sbDocDown.append("<td>");
            sbDocDown.append("<input value=\"\" id=\"JFQQ\" name=\"JFQQ\" type=\"text\"  size=\"20\"/>");
            sbDocDown.append("</td>");
            sbDocDown.append("<td id=\"fileTd\">");
            sbDocDown.append("<input id=\"JFECRQQFileId\" name=\"JFECRQQFileId\" type=\"hidden\"/>");
            sbDocDown.append("<input onclick=\"javascript:showModalDialog('../components/JF_ECRQQFilePreCheckin.jsp?objectAction=checkin&amp;msfBypass=true&amp;objectId=;");
            sbDocDown.append("','730','450')\"" +
                    "  value=\"");
            _logger.info("strFileUploadNls:{}", strFileUploadNls);
            sbDocDown.append(strFileUploadNls);
            sbDocDown.append("\" type=\"button\"/>");
            sbDocDown.append("</td>");
            sbDocDown.append("<td id=\"JFReplace\">");
            sbDocDown.append("</td>");
        }
        sbDocDown.append("</tr></table>");

        //
        if ("edit".equals(strMode)) {
            if ("Both".equals(strChangeSource) || "External Changes".equals(strChangeSource)) {
                sbDocDown.append("<script>");
                sbDocDown.append("  var  dom = document.getElementById('");
                String strDomId = "calc_JFQQ";
                sbDocDown.append(strDomId);
                sbDocDown.append("');");
                sbDocDown.append("  if (dom){");
                sbDocDown.append("    dom.querySelector('td:first-child').className = \"createLabelRequired\";");
                sbDocDown.append("  }");
                sbDocDown.append("</script>");

            }
        } else if ("create".equals(strMode)) {
          /*  sbDocDown.append("<script>");
            sbDocDown.append("  var  dom = document.getElementById('");
            String strDomId = "calc_JFQQ";
            sbDocDown.append(strDomId);
            sbDocDown.append("');");
            sbDocDown.append("  if (dom){");
            sbDocDown.append("    dom.querySelector('td:first-child').className = \"createLabelRequired\";");
            sbDocDown.append("  }");
            sbDocDown.append("</script>");*/
        }
        sbDocDown.append("<script language=\"JavaScript\">");
        sbDocDown.append("function  checkJFQQ1(){\n" +
                "    var strChangeSource = ''\n" +
                "    if (strChangeSource === \"Both\" || strChangeSource === \"External Changes\"){\n" +
                "        const  domJFQQ = document.getElementById('JFQQ');\n" +
                "        const domJFQQFileId=  document.getElementById(\"JFECRQQFileId\");\n" +
                "        if ((!domJFQQ.value) || (!domJFQQFileId.value)){\n" +
//                "        if (!(domJFQQ.value && domJFQQFileId.value)){\n" +
                "            sendMess1(\"\u5fc5\u987b\u8f93\u5165\u6709\u6548\\u503c\uff1a QQ\u7f16\u53f7\u53ca\u9644\u4ef6\",\"Valid values must be entered: QQ ID and attachment\");\n" +
                "        return false ;\n" +
                "        }\n" +
                "    }\n" +
                "        return true ;\n" +
                "}");
        sbDocDown.append("function sendMess1(strCNMess,strENMess){\n" +
                "    var language = navigator.language || navigator.userLanguage;\n" +
                "    var strMess = \"\";\n" +
                "    if (language.includes(\"zh\") || language.includes(\"zh-CN\")) {\n" +
                "        strMess = strCNMess;\n" +
                "    }else {\n" +
                "        strMess = strENMess;\n" +
                "    }\n" +
                "    alert(strMess);\n" +
                "}");
        sbDocDown.append(" // 使用 addEventListener 添加第三个 onload 函数\n" +
                "    window.addEventListener('load', function JFQQOnloadHandler() {\n" +
                "        console.log(\"Third onload handler called.\");\n" +
                "        console.log(\"@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@.\");\n" +
                "        document.getElementById('JFQQ').customValidate = checkJFQQ1\n" +
                "    }, false);");
        sbDocDown.append(" </script>");
        _logger.info("sbDocDown:{}", sbDocDown);
        //加载影响工厂样式
        if (!"view".equals(strMode)) {
            if (!"Yes".equals(strIsPlatForm)) {
                sbDocDown.append("<script language=\"JavaScript\">");
                if ("create".equals(strMode)) {
                    sbDocDown.append("    window.addEventListener('load', function JFJFAffectsFactoryOnloadHandler() {\n" +
                            " FormHandler.GetField(\"JFAffectsFactory\").HandlerField[0].customValidate = checkInputIsNull;" +
                            "emxFormSetFieldEditable(\"JFAffectsFactory\",false);" +
                            "    }, false);");
                }
                sbDocDown.append(" </script>");


            }
        }
        _logger.info("--------------------- buildECRQQHtml end --------------------------");
        return sbDocDown.toString();
    }
    /**
     * @param context
     * @param args
     * @return java.lang.String
     * @throws
     * @description 构建ECR受影响项目列
     * @author CHENYAN
     * @date 2024/9/6 16:17
     */

    public String buildMultipleChoiceProjectHtml(Context context, String[] args) throws Exception {
        _logger.info("--------------------------------- buildMultipleChoiceProjectHtml begin --------------------------------------------");
        StringBuilder sb = new StringBuilder();
        //添加移除文字提示
        String strAddButton = EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", context.getLocale(), "emxComponents.Button.UpdateProjectBtn");
//        emxComponents.Button.UpdateProjectBtn
        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        HashMap requestMap = (HashMap) programMap.get("requestMap");
        String strMode = (String) requestMap.get("mode");
        String strObjectId = (String) requestMap.get("objectId");
        _logger.info("strMode:{}", strMode);
        _logger.info("strObjectId:{}", strObjectId);
        String strLoginUser = context.getUser();
        if (UIUtil.isNotNullAndNotEmpty(strObjectId)) {
            DomainObject ecr = DomainObject.newInstance(context, strObjectId);
            StringList busSelectList = new StringList();
            busSelectList.add(SELECT_CURRENT);
            busSelectList.add(SELECT_OWNER);
            busSelectList.add(SELECT_ATTR_JFAssociatedOtherProjects);
            Map ecrInfo = ecr.getInfo(context, busSelectList);
            String strCurrent = (String) ecrInfo.get(SELECT_CURRENT);
            String strOwner = (String) ecrInfo.get(SELECT_OWNER);
            String strAssociatedOtherProjects = (String) ecrInfo.get(SELECT_ATTR_JFAssociatedOtherProjects);
            StringList reSelectList = new StringList();
            reSelectList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JFROUTENOTE_APPROVALAROLE);
            StringList typeSelectList = new StringList();
            typeSelectList.add(SELECT_ID);
            typeSelectList.add(SELECT_NAME);
            typeSelectList.add(SELECT_DESCRIPTION);
            MapList maps;
            try {
                ContextUtil.pushContext(context);
                maps = ecr.getRelatedObjects(context, "JFECR2AffectedProject", // relationship pattern
                        TYPE_PROJECT_SPACE,                                    // object pattern
                        typeSelectList,                            // object selects
                        reSelectList, // relationship selects
                        false,                                        // to direction
                        true,                                        // from direction
                        (short) 1,                                    // recursion level
                        "",                // object where clause
                        "",
                        (short) 0);
            } finally {
                ContextUtil.popContext(context);
            }
            sb.append("<table>");
            sb.append("<tr>");
            if (UIUtil.isNotNullAndNotEmpty(strMode)) {
                sb.append("<td id='JFProjectReplace'>");
                sb.append(buildLinkProjectHtml(maps));
                sb.append("</td>");
                if ("edit".equalsIgnoreCase(strMode)) {
                    if (("Create".equals(strCurrent)) && (strLoginUser.equals(strOwner))) {
                        sb.append("<td>");
                        sb.append("<input onclick=\"javascript:updateJFAffectedProject(");
                        sb.append("'");
                        sb.append(strObjectId);
                        sb.append("'");
                        sb.append(")\"  value=\"");
                        _logger.info("strFileUploadNls:{}", strAddButton);
                        sb.append(strAddButton);
                        sb.append("\" type=\"button\" style=\"margin-left: 5px;\"/>");
                        sb.append("</td>");
                    }
                }
            }
            sb.append("</tr>");
            sb.append("</table>");
        }
        _logger.info("--------------------------------- buildMultipleChoiceProjectHtml end --------------------------------------------");

        return sb.toString();
    }

    /**
     * @param projectList
     * @return java.lang.String
     * @throws
     * @description 够着带链接的受影响项目
     * @author CHENYAN
     * @date 2024/9/6 16:18
     */
    public String buildLinkProjectHtml(MapList projectList) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < projectList.size(); i++) {
            Map project = (Map) projectList.get(i);
            String strProjectId = (String) project.get(SELECT_ID);
            String strProjectName = (String) project.get(SELECT_DESCRIPTION);
            sb.append(" <a href=\"JavaScript:emxFormLinkClick(&quot;../common/emxTree.jsp?objectId=");
            sb.append(strProjectId);
            sb.append("&amp;relId=null&quot;, &quot;content&quot;, &quot;&quot;, &quot;&quot;, &quot;&quot;, &quot;");
            sb.append(strProjectName);
            sb.append("&quot;, &quot;&quot;, &quot;&quot;)\" class=\"object\">" + strProjectName + "</a>");
            if (i != (projectList.size() - 1)) {
                sb.append("<br>");
            }
        }
        return sb.toString();
    }

    /**
    *
    *@description 卷积自制件清单整椅价格
    *@param context
	*@param args
    *@return void
    *@throws
    *@author CHENYAN
    *@date 2025/5/19 15:43
    */
    public void rollupNewECRRootPartPrice(Context context ,String[] args ) throws Exception{
//        Job job = new Job("JF_VPMReferenceEBOM", "convolutionReletedItem", args);
//        job.setTitle("convolution ChairPart price in NewECR");
//        job.createAndSubmit(context);
        String strObjectId = args[0];
        DomainObject ecrObj = DomainObject.newInstance(context,strObjectId);
        String type = ecrObj.getInfo(context, SELECT_TYPE);
        if(type.equalsIgnoreCase(JF_PLMConstants_mxJPO.TYPE_JFNewECR)){
            rollupNewECRRootPartPrice_NewECR(context,args);
        }else {
            String initargs[] = {};
            Map parameters = new HashMap<>();
            _logger.info("-------------------------------- rollupNewECRRootPartPrice begin -----------------------------------------------");
            parameters.put("objectId", strObjectId);
            parameters.put("expandLevel", "All");
            parameters.put("verifyUnitPrice", "true");
            parameters.put(STRING_SELECT_TABLE, "JFFormalECRCosting");
            MapList makeTableMapList = (MapList) JPO.invoke(context, "JF_FormalECRService", initargs, "getFormalECRMakeTableData", JPO.packArgs(parameters), MapList.class);
            DomainObject ecr = DomainObject.newInstance(context, strObjectId);
            String strProjectId = ecr.getInfo(context, "from[JFChange2Project].to.id");
            try {
                ContextUtil.startTransaction(context, true);
                for (int i = 0; i < makeTableMapList.size(); ) {
                    Map partMap = (Map) makeTableMapList.get(i);
                    String strPartId = (String) partMap.get(SELECT_ID);
                    String strRelName = (String) partMap.get(RELATIONSHIP);
                    //根节点
                    if (REL_JFECRRelateRoot.equals(strRelName) || REL_JFECRRelateRootBubble.equals(strRelName)) {
                        int nextLevel = findEndOfSubtree(makeTableMapList, i); // 找到以i为根的子树结束位置
                        String strJFFreeState = (String) partMap.get(SELECT_ATTR_JFFREEState);
                        boolean isZeroPart = checkPartIsZeroPart(context, strPartId, strProjectId);
                        //不是游离 且是供货件
                        if ("N".equals(strJFFreeState) && isZeroPart) {
                            DomainObject part = DomainObject.newInstance(context, strPartId);
                            // 获取卷积价格
                            Map attrMap = getRootPartTreePrice(context, makeTableMapList, i + 1, nextLevel, strProjectId);
                            part.setAttributeValues(context, attrMap);
                        }
                        i = nextLevel;
                    } else {
                        //防止死循环
                        i++;
                    }
                }
                _logger.info("-------------------------------- rollupNewECRRootPartPrice end -----------------------------------------------");

                ContextUtil.commitTransaction(context);
            } catch (Exception e) {
                e.printStackTrace();
                _logger.error(e.getMessage());
                ContextUtil.abortTransaction(context);
                throw e;
            }
        }
    }

    public Map getRootPartTreePrice(Context context,MapList dataMapList, int startIndex ,int endIndex ,String strProjectId) throws Exception{
        Map attrMap = new HashMap<>();
        BigDecimal sumPrice = new BigDecimal(0.0);
        BigDecimal sumPriceExternal = new BigDecimal(0.0);
        StringList typeSelectList = new StringList();
        typeSelectList.add(SELECT_ATTR_JF_VPMReferenceCost_JF_UnitPriceCost);
        typeSelectList.add(SELECT_ATTR_JF_VPMReferenceCost_JF_ChangeUnitPriceCostExternal);
        typeSelectList.add(SELECT_ATTR_JF_VPMReferenceCost_JF_UnitPrice);
        typeSelectList.add(SELECT_ATTR_JF_VPMReferenceCost_JF_ChangeUnitPrice);
        String strSelectLogisticsFees = String.format(JF_FormalECRStaticMethod_mxJPO.STRING_SELECT_JF_LogisticsFeesWhere, "from.id==" + strProjectId);
        for (; startIndex < endIndex; ) {
            Map partMap = (Map) dataMapList.get(startIndex);
            //采购类型
            String strProcurementType = (String) partMap.get(SELECT_ATTR_JF_ProcurementType);
//            String strQuantity = (String) partMap.get(SELECT_ATTRIBUTE_JF_BOMQuantity);
//            String strChangeQuantity = (String) partMap.get(SELECT_ATTRIBUTE_JF_BOMChangeQuantity);
            //保存变更前版本id
            String strBOMBeforeRev = (String) partMap.get(SELECT_ATTRIBUTE_JF_ChangeBeforeRev);
            String strBOMBeforeQuantity = (String) partMap.get(SELECT_ATTRIBUTE_JF_BOMBeforeQuantity);

            //默认设置为1
            //变更数量为空或者为0.0取实际数量，实际  数量为空默认为1

//            BigDecimal bQuantity =JF_FormalECRStaticMethod_mxJPO.newInstanceBigdecimal(strChangeQuantity) ;
//            //  初始成本
//            String strUnitPrice = (String) partMap.get(SELECT_ATTR_JF_VPMReferenceCost_JF_UnitPriceCost);
//            String strChangeUnitPriceExternal = (String) partMap.get(SELECT_ATTR_JF_VPMReferenceCost_JF_ChangeUnitPriceCostExternal);
//            //外部 (采购单件成本)
//            String strBuyUnitPrice = (String) partMap.get(SELECT_ATTR_JF_VPMReferenceCost_JF_UnitPrice);
//            String strChangeBuyUnitPrice = (String) partMap.get(SELECT_ATTR_JF_VPMReferenceCost_JF_ChangeUnitPrice);
//            //物流费
//            String strLogisticPrice = (String) partMap.get(JF_FormalECRStaticMethod_mxJPO.STRING_SELECT_JF_LogisticsFees);
//            _logger.info("strChangeQuantity:{}",strChangeQuantity);
//            _logger.info("strUnitPrice:{}",strUnitPrice);
//            _logger.info("strChangeUnitPriceExternal:{}",strChangeUnitPriceExternal);
//            _logger.info("strBuyUnitPrice:{}",strBuyUnitPrice);
//            _logger.info("strChangeBuyUnitPrice:{}",strChangeBuyUnitPrice);
//            _logger.info("strLogisticPrice:{}",strLogisticPrice);
//            BigDecimal unitPriceDecimal = JF_FormalECRStaticMethod_mxJPO.newInstanceBigdecimal(strUnitPrice);
//            BigDecimal unitChangePriceDecimal = JF_FormalECRStaticMethod_mxJPO.newInstanceBigdecimal(strChangeUnitPriceExternal);
//            sumPrice = unitPriceDecimal.subtract(unitChangePriceDecimal).multiply(bQuantity).add(sumPrice);
//            //外部
//            BigDecimal buyUnitPriceDecimal =JF_FormalECRStaticMethod_mxJPO.newInstanceBigdecimal(strBuyUnitPrice);
//            BigDecimal buyUnitChangePriceDecimal = JF_FormalECRStaticMethod_mxJPO.newInstanceBigdecimal(strChangeBuyUnitPrice);
//            BigDecimal logisticPriceDecimal = JF_FormalECRStaticMethod_mxJPO.newInstanceBigdecimal(strLogisticPrice);
//            sumPriceExternal = buyUnitPriceDecimal.subtract(buyUnitChangePriceDecimal).add(logisticPriceDecimal).multiply(bQuantity).add(sumPriceExternal);
            //当前版本的价格
            Map<String, BigDecimal> currentPriceMap = JF_FormalECRStaticMethod_mxJPO.calcZeroRollupPrice(partMap);
            Map<String, BigDecimal> beforePriceMap = null;
            if (UIUtil.isNotNullAndNotEmpty(strBOMBeforeRev)){
                //变更如果只存在数量变化 ，需要读取前一个版本的数量
                //前一个版本
                DomainObject beforePart = DomainObject.newInstance(context, strBOMBeforeRev);
                Map breforPriceMap = beforePart.getInfo(context, typeSelectList);
                _logger.info("strSelectLogisticsFees:{}",strSelectLogisticsFees);
                StringList logisticsFeesList = beforePart.getInfoList(context, strSelectLogisticsFees);
                String strLogisticsFees = EMPTY_STRING;
                if (logisticsFeesList.size() > 0){
                    strLogisticsFees = logisticsFeesList.get(0);
                    String[] split = strLogisticsFees.split("=");
                    strLogisticsFees = split[split.length - 1].trim();
                }
                breforPriceMap.put(JF_FormalECRStaticMethod_mxJPO.STRING_SELECT_JF_LogisticsFees,strLogisticsFees);
                //把前一个版本的数量设置进去
                breforPriceMap.put(SELECT_ATTRIBUTE_JF_BOMQuantity,strBOMBeforeQuantity);

                beforePriceMap = JF_FormalECRStaticMethod_mxJPO.calcZeroRollupPrice(breforPriceMap);

            }
            //减去前一个版本的价格
            if (Objects.nonNull(beforePriceMap) && beforePriceMap.size() > 0){
                for (Map.Entry<String, BigDecimal> entry : beforePriceMap.entrySet()) {
                    String strKey = entry.getKey();
                    BigDecimal beforePrice = entry.getValue();
                    BigDecimal currentPrice = currentPriceMap.get(strKey);
                    currentPrice = currentPrice.subtract(beforePrice);
                    currentPriceMap.put(strKey,currentPrice);
                }
            }
            for (Map.Entry<String, BigDecimal> entry : currentPriceMap.entrySet()) {
                String strKey = entry.getKey();
                BigDecimal currentPrice = entry.getValue();
                if (ATTR_JF_VPMReferenceCost_JF_ChangeWholeSeatPrice.equals(strKey)){
                    sumPrice = sumPrice.add(currentPrice);
                }else if (ATTR_JF_VPMReferenceCost_JF_ChangeWholeSeatPriceExternal.equals(strKey)){
                    sumPriceExternal = sumPriceExternal.add(currentPrice);
                }
            }
            //前一个版本的价格
            if (ATTR_JF_ProcurementType_RANGE_BUY.equals(strProcurementType) || ATTR_JF_ProcurementType_RANGE_ICO.equals(strProcurementType)){
                int nextLevel = findEndOfSubtree(dataMapList, startIndex); // 找到以i为根的子树结束位置
                startIndex = nextLevel;
            }else {
                startIndex++;
            }
        }

        sumPrice = sumPrice.setScale(2, RoundingMode.HALF_UP);
        sumPriceExternal = sumPriceExternal.setScale(2, RoundingMode.HALF_UP);
        _logger.info("sumPrice:{}",sumPrice);
        _logger.info("sumPriceExternal:{}",sumPriceExternal);
        attrMap.put(ATTR_JF_VPMReferenceCost_JF_ChangeWholeSeatPrice,sumPrice.toString());
        attrMap.put(ATTR_JF_VPMReferenceCost_JF_ChangeWholeSeatPriceExternal,sumPriceExternal.toString());
        return attrMap;
    }

    /**
    *
    *@description 通过游离状态对结构进行冲重排序 非游离状态显示在前
    *@param dataMapList
    *@return com.matrixone.apps.domain.util.MapList
    *@throws
    *@author CHENYAN
    *@date 2025/5/20 14:25
    */

    public MapList rearrangeStructureByFreeState(MapList dataMapList){
        MapList res = new MapList();
        MapList freeMapList = new MapList();
        for (int i = 0; i < dataMapList.size(); ) {
            Map partMap = (Map) dataMapList.get(i);
            String strType = (String)partMap.get(SELECT_TYPE);
            String strRelName = (String)partMap.get(RELATIONSHIP);
            if (TYPE_VPMReference.equals(strType)){
                int nextLevel = findEndOfSubtree(dataMapList, i); // 找到以i为根的子树结束位置
                int j = i ;
                //游离
                if (partMap.containsKey("freeFlag") && REL_JFECRRelateRoot.equals(strRelName)) {
                    for (; j < nextLevel ; j++) {
                        Map tmpPartMap = (Map) dataMapList.get(j);
                        freeMapList.add(tmpPartMap);
                    }
                }else {

                    for (; j < nextLevel ; j++) {
                        Map tmpPartMap = (Map) dataMapList.get(j);
                        res.add(tmpPartMap);
                    }
                }
                i = nextLevel;
            }else {
                res.add(partMap);
                i++;
            }
        }
        res.addAll(freeMapList);
        //如果重排结构后数量不一致返回原数据
        if (res.size() != dataMapList.size()){
            res = dataMapList ;
        }
        return  res ;
    }
    /**
     * @param context
     * @param args
     * @return java.util.Map
     * @throws
     * @description 是否平台间 Range值重新排序
     * @author CHENYAN
     * @date 2024/7/22 17:21
     */
    public Map getJFIsPlatformPartRanges(Context context, String[] args) throws Exception {
        HashMap<Object, Object> res = new HashMap<>();
        StringList ranges = FrameworkUtil.getRanges(context, "JFIsPlatformPart");
        _logger.info("ranges:{}", ranges);
        StringList sortRanges = new StringList(ranges.size());
        if (ranges.contains("Yes")) {
            sortRanges.add("Yes");
            ranges.remove("Yes");
        }
        if (ranges.contains("No")) {
            sortRanges.add("No");
            ranges.remove("No");
        }
        if (ranges.size() > 0) {
            sortRanges.addAll(ranges);
        }
        StringList nlsRanges = EnoviaResourceBundle.getAttrRangeI18NStringList(context, "JFIsPlatformPart", sortRanges, context.getLocale().toString());
        res.put("field_choices", sortRanges);
        res.put("field_display_choices", nlsRanges);
        _logger.info("ranges:{}", sortRanges);
        return res;
    }
    public StringList getJFChangeResourceEditAccess(Context context, String[] args) throws Exception {
        _logger.info("------------------------- getJFChangeResourceEditAccess begin ------------------------------------");
        Map paramMap = (Map) JPO.unpackArgs(args);
        Map requestMap = (Map) paramMap.get("requestMap");
        String strParentId = (String) requestMap.get("parentOID");
        StringList tableIdList = _getListOfKeys(args, SELECT_ID);
        DomainObject ecr = DomainObject.newInstance(context, strParentId);
        String current = ecr.getInfo(context, SELECT_CURRENT);
        String owner = ecr.getInfo(context, SELECT_OWNER);
        String loginUser = context.getUser();
        StringList res = new StringList(tableIdList.size());
        Boolean isEdit = Boolean.FALSE;
        if ( "Create".equalsIgnoreCase(current)&&owner.equalsIgnoreCase(loginUser)) {
            isEdit = Boolean.TRUE;
        }
        for (int i = 0; i < tableIdList.size(); i++) {
            res.add(isEdit.toString());
        }
        _logger.info("res:{}", res);
        _logger.info("------------------------- getJFChangeResourceEditAccess end ------------------------------------");
        return res;
    }

    /**
    *
    *@description 获取ECR 自制件和采购件清单的表格上传权限
    *@param context
	*@param args
    *@return boolean
    *@throws
    *@author CHENYAN
    *@date 2025/6/10 14:32
    */

    public boolean getUploadNewECRTableAccess(Context context, String[] args) throws Exception {
        _logger.info("-----------------------------------------getUploadNewECRTableAccess begin ----------------------------------------------------");
        boolean res = false;
        Map requestMap = JPO.unpackArgs(args);
        _logger.info("requestMap:{}", requestMap);
//        String strObjectId  = (String)requestMap.get("parentOID");
        String strObjectId = (String) requestMap.get("objectId");
        String strSelectedTable = (String) requestMap.get("table");
        _logger.info("strSelectedTable:{}", strSelectedTable);
        String strLoginUser = context.getUser();
        DomainObject ecr = DomainObject.newInstance(context, strObjectId);
        Map ecrInfo = ecr.getInfo(context, StringList.create(SELECT_CURRENT,SELECT_TYPE));
        String strECRCurrent = (String) ecrInfo.get(SELECT_CURRENT);
        String strECRType = (String) ecrInfo.get(SELECT_TYPE);
        if ( TYPE_JFNewECR.equals(strECRType)&&("Countersign".equals(strECRCurrent) || "APR".equals(strECRCurrent))) {
            //角色不为空的会签任务
            MapList loginUserSignTaskRole = new MapList();
            loginUserSignTaskRole = JF_PublicMethodClass_mxJPO.getLoginUserSignTaskRoleByECRAndUser(context, new String[]{strObjectId, strLoginUser});
            if (loginUserSignTaskRole.size() > 0) {
                for (int i = 0; i < loginUserSignTaskRole.size(); i++) {
                    Map signTask = (Map) loginUserSignTaskRole.get(i);
                    String strCurrent = (String) signTask.get("current");
                    _logger.info("strCurrent:{}", strCurrent);
                    if (!("Complete".equals(strCurrent) || "Review".equals(strCurrent))) {
                        String strRole = (String) signTask.get("role");
                        _logger.info("strRole:{}", strRole);
                        strRole = JF_NewECRRESTService_mxJPO.getRoleKeyByValue(strRole);
                        _logger.info("strRole:{}", strRole);
                        if (strSelectedTable.contains("JFNewECRCosting")) {
                            if (JF_NewECRRESTService_mxJPO.EDIT_ECRCosting_TABLE_ROLE_LIST.contains(strRole)) {
                                res = true;
                                break;
                            }
                        } else if (strSelectedTable.contains("JFNewECRController")) {
                            if (JF_NewECRRESTService_mxJPO.EDIT_ECRController_TABLE_ROLE_LIST.contains(strRole)) {
                                res = true;
                                break;
                            }
                        }
                    }
                }
            }
        }
        _logger.info("res:{}", res);
        _logger.info("-----------------------------------------getUploadNewECRTableAccess end ----------------------------------------------------");
        return res;
    }
    /**
    *
    *@description 获取ECR 自制件和采购件清单的表格下载权限
    *@param context
	*@param args
    *@return boolean
    *@throws
    *@author CHENYAN
    *@date 2025/6/10 14:33
    */
    public boolean getDownloadNewECRTableAccess(Context context, String[] args) throws Exception {
        boolean res = false;
        Map requestMap = JPO.unpackArgs(args);
//        String strObjectId  = (String)requestMap.get("parentOID");
        String strObjectId = (String) requestMap.get("objectId");
        String strSelectedTable = (String) requestMap.get("table");
        String strLoginUser = context.getUser();
        DomainObject ecr = DomainObject.newInstance(context, strObjectId);
        Map ecrInfo = ecr.getInfo(context, StringList.create(SELECT_CURRENT,SELECT_TYPE));
        String strECRCurrent = (String) ecrInfo.get(SELECT_CURRENT);
        String strECRType = (String) ecrInfo.get(SELECT_TYPE);
        _logger.info("strSelectedTable:{}", strSelectedTable);
        if ( TYPE_JFNewECR.equals(strECRType) &&("Countersign".equals(strECRCurrent) || "APR".equals(strECRCurrent) ||
                "Quotation".equals(strECRCurrent) || "Complete".equals(strECRCurrent))) {
            //角色不为空的会签任务
            MapList loginUserSignTaskRole = new MapList();
            loginUserSignTaskRole = JF_PublicMethodClass_mxJPO.getLoginUserSignTaskRoleByECRAndUser(context, new String[]{strObjectId, strLoginUser});
            if (loginUserSignTaskRole.size() > 0) {
                for (int i = 0; i < loginUserSignTaskRole.size(); i++) {
                    Map signTask = (Map) loginUserSignTaskRole.get(i);
                    String strCurrent = (String) signTask.get("current");
                    _logger.info("strCurrent:{}", strCurrent);
                    String strRole = (String) signTask.get("role");
                    strRole = JF_NewECRRESTService_mxJPO.getRoleKeyByValue(strRole);
                    _logger.info("strRole:{}", strRole);
//                    if ("JFECRCosting".equals(strSelectedTable)) {
                    if (strSelectedTable.contains("JFNewECRCosting")) {
                        if (JF_NewECRRESTService_mxJPO.EDIT_ECRCosting_TABLE_ROLE_LIST.contains(strRole)) {
                            res = true;
                            break;
                        }
//                    } else if ("JFECRController".equals(strSelectedTable)) {
                    } else if (strSelectedTable.contains("JFNewECRController")) {
                        if (JF_NewECRRESTService_mxJPO.EDIT_ECRController_TABLE_ROLE_LIST.contains(strRole)) {
                            res = true;
                            break;
                        }
                    }
                }
            }
        }
        _logger.info("res:{}", res);
        return res;
    }

    public Map NewECRDownload(Context context, String[] args) throws Exception {
        Map res = new HashMap<String, String>();
        Map paramMap = (Map) JPO.unpackArgs(args);
        String strObjectId = (String) paramMap.get("objectId");
        //选择的表格
        String strSelectTable = (String) paramMap.get("type");
        _logger.info("strSelectTable:{}", strSelectTable);
        //服务路径
        String strPath = (String) paramMap.get("path");
        //模板excel路径
        String strTemplatePath = JF_ECRService_mxJPO.getTemplatePath(strSelectTable, strPath);
        _logger.info("strTemplatePath:{}", strTemplatePath);
        InputStream inputStream = null;
        FileOutputStream fos = null;
        try {
            inputStream = new FileInputStream(strTemplatePath);
            Workbook workbook = WorkbookFactory.create(inputStream);
            //设置可编辑单元格背景色
            CellStyle cellStyle = workbook.createCellStyle();
            //设置背景色
            cellStyle.setFillForegroundColor(IndexedColors.YELLOW.getIndex());
            //必须设置 否则背景色不生效
            cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            cellStyle.setBorderTop(BorderStyle.THIN);
            cellStyle.setBorderBottom(BorderStyle.THIN);
            cellStyle.setBorderLeft(BorderStyle.THIN);
            cellStyle.setBorderRight(BorderStyle.THIN);
            //设置单元格边框
            CellStyle cellBorderStyle = workbook.createCellStyle();
            cellBorderStyle.setBorderTop(BorderStyle.THIN);
            cellBorderStyle.setBorderBottom(BorderStyle.THIN);
            cellBorderStyle.setBorderLeft(BorderStyle.THIN);
            cellBorderStyle.setBorderRight(BorderStyle.THIN);
            //读取第一个sheet
            Sheet sheet = workbook.getSheetAt(0);

            //从第二行遍历sheet，并拿取其中每行的数据
            Row row = sheet.getRow(0);
            Map excelTitleMap = new HashMap<String, Integer>();
            for (int i = row.getFirstCellNum(); i < row.getLastCellNum(); i++) {
                String strCell = row.getCell(i, Row.MissingCellPolicy.RETURN_NULL_AND_BLANK) == null ? "" : JF_ECRService_mxJPO.getCellValue(row, i);
                if (UIUtil.isNotNullAndNotEmpty(strCell)) {
                    excelTitleMap.put(strCell, i);
                }
            }
            _logger.info("excelTitleMap:{}", excelTitleMap);
            HashMap<String, String> stringStringHashMap = new HashMap<>();
            stringStringHashMap.put("objectId", strObjectId);
            stringStringHashMap.put("expandLevel", "0");
            MapList ecrCostingTableData = new MapList();
            //costing 表格数据
            if ("JFNewECRCosting".equals(strSelectTable)) {
                ecrCostingTableData = getNewECRBuyTableData(context, JPO.packArgs(stringStringHashMap));
            } else if ("JFNewECRController".equals(strSelectTable)) {
                ecrCostingTableData = getNewECRMakeTableData(context, JPO.packArgs(stringStringHashMap));
            }
            _logger.info("ecrCostingTableData size :{}", ecrCostingTableData.size());

            MapList CostingMappingList = JF_PublicMethodClass_mxJPO.getECRTableMapping(context, strSelectTable);
            _logger.info("CostingMappingList:{}",CostingMappingList);
            updateTableData(context, strSelectTable, strObjectId, ecrCostingTableData, CostingMappingList);
            _logger.info("ecrCostingTableData:{}", ecrCostingTableData);
            //编辑table 数据写入excel
            for (int i = 0; i < ecrCostingTableData.size(); i++) {
                Row createRow = sheet.createRow(i + 2);
                Map map = (Map) ecrCostingTableData.get(i);
                createRowValue(excelTitleMap, map, createRow, cellStyle, cellBorderStyle);
            }
            String strTmpPath = context.createWorkspace();
            strTmpPath = strTmpPath.endsWith("/") ? strTmpPath : JF_PublicMethodClass_mxJPO.buildStringInStrings(strTmpPath, "/");
            String strFileName = JF_PublicMethodClass_mxJPO.buildStringInStrings(String.valueOf(System.currentTimeMillis()), ".xlsx");
            String strFullPath = JF_PublicMethodClass_mxJPO.buildStringInStrings(strTmpPath, strFileName);
            _logger.info("strFullPath:{}", strFullPath);
            fos = new FileOutputStream(strFullPath);
            workbook.write(fos);
            workbook.close();
            res.put("path", strFullPath);
            res.put("filename", strFileName);
            res.put("code", "200");
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
            if (fos != null) {
                fos.close();
            }

        }
        return res;
    }

    /**
     * @param context
     * @param strSelectedTable JFECRCosting 和 JFECRController
     * @param strECRId         ECR ID
     * @param tableDataList    表格数据
     * @return void
     * @throws
     * @description 更新表格Map的属性值和是否可编辑
     * @author CHENYAN
     * @date 2024/9/29 16:45
     */
    public static void updateTableData(Context context, String strSelectedTable, String strECRId, MapList tableDataList, MapList xmlMappingMapList) throws Exception {
        try {
            for (int i = 0; i < xmlMappingMapList.size(); i++) {
                //获取列 key
                Map xmlFieldMap = (Map) xmlMappingMapList.get(i);
                String strFieldKey = (String) xmlFieldMap.get("id");
                //获取属性key
                String strFieldSelectKey = (String) xmlFieldMap.get("selectValue");
                String strFieldValueKey = (String) xmlFieldMap.get("value");
                _logger.info("strFieldKey:{}", strFieldKey);
                //构造 参数 调用table中方法
                HashMap<Object, Object> paramsMap = new HashMap<>();
                HashMap<Object, Object> paramList = new HashMap<>();
                HashMap<Object, Object> columnMap = new HashMap<>();
                paramList.put("selectedTable", strSelectedTable);
                paramList.put("parentOID", strECRId);
                paramList.put("objectId", strECRId);
                columnMap.put("name", strFieldKey);
                columnMap.put("selectName", strFieldSelectKey);
                columnMap.put("attributeName", strFieldValueKey);
                paramsMap.put("paramList", paramList);
                paramsMap.put("requestMap", paramList);
                paramsMap.put("columnMap", columnMap);
                paramsMap.put("objectList", tableDataList);
                String strGetFieldValueMethodName = "";
                String strGetFieldAccessMethodName = "";
//                if ("JFECRCosting".equals(strSelectedTable)) {
                if (strSelectedTable.contains("JFNewECRCosting")) {
                    strGetFieldValueMethodName = "getNewAffectedItemsCostTableField";
                    strGetFieldAccessMethodName = "getJFNewECRAffectedItemsCostTableEditAccess";
//                } else if ("JFECRController".equals(strSelectedTable)) {
                } else if (strSelectedTable.contains("JFNewECRController")) {
                    strGetFieldValueMethodName = "getNewAffectedItemsCostTableField";
                    strGetFieldAccessMethodName = "getJFNewECRAffectedItemsMakeTableEditAccess";
                }
                //列显示信息
                StringList fieldValueList = JPO.invoke(context, "JF_NewECRService", null, strGetFieldValueMethodName, JPO.packArgs(paramsMap), StringList.class);
                //列可编辑信息
                StringList fieldIsEditList = JPO.invoke(context, "JF_NewECRService", null, strGetFieldAccessMethodName, JPO.packArgs(paramsMap), StringList.class);
                _logger.info("fieldValueList:{}", fieldValueList);
                _logger.info("fieldIsEditList:{}", fieldIsEditList);
                //更新 表格数据
                for (int i1 = 0; i1 < tableDataList.size(); i1++) {
                    Map rowData = (Map) tableDataList.get(i1);
                    rowData.put(JF_PublicMethodClass_mxJPO.buildStringInStrings(strFieldKey, "disPlayValue"), fieldValueList.get(i1));
                    rowData.put(JF_PublicMethodClass_mxJPO.buildStringInStrings(strFieldKey, "isEdit"), fieldIsEditList.get(i1));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            _logger.error(e.getMessage());
            throw e;
        }
    }
    public Row createRowValue(Map excelTitleMap, Map dbMap, Row createRow, CellStyle cellStyle, CellStyle cellBorderStyle) throws Exception {
        for (Object entry : excelTitleMap.entrySet()) {
            Map.Entry cellKey = (Map.Entry) entry;
            //列的key
            String strCellKey = (String) cellKey.getKey();
            //列的下标
            Integer iCellIndex = (Integer) cellKey.getValue();
            Cell createCell = createRow.createCell(iCellIndex);
            createCell.setCellValue((String) dbMap.get(JF_PublicMethodClass_mxJPO.buildStringInStrings(strCellKey, "disPlayValue")));
            String strIsEdit = (String) dbMap.get(JF_PublicMethodClass_mxJPO.buildStringInStrings(strCellKey, "isEdit"));
            if (UIUtil.isNotNullAndNotEmpty(strIsEdit)) {
                if ("true".equalsIgnoreCase(strIsEdit)) {
                    createCell.setCellStyle(cellStyle);
                } else {
                    if (!("id".equals(strCellKey) || "id[connection]".equals(strCellKey))) {
                        createCell.setCellStyle(cellBorderStyle);
                    }
                }
            }
        }
        return createRow;
    }

    /**
     * 1.读取Excel中数据保存为MapList
     * 校验excel中修改数据是否为正实数 不符合要求直接返回
     * 2.获取该ECR关联的采购件、自制件清单数据
     * 3.遍历ECR关联的采购件、自制件清单数据，获取每一行可编辑的属性
     * 4.拿到可编辑的属性根据id去一一匹配更新属性 如果有关系直接更新 ，如果不存在关系需要新建
     */
    public Map readNewECRExcelAndUpdateData(Context context, String[] args) throws Exception {
        _logger.info("----------------------------------  readNewECRExcelAndUpdateData begin --------------------------------------------------------");
        Map res = new HashMap<String, String>();
        Map paramMap = (Map) JPO.unpackArgs(args);
        List files = (List) paramMap.get("files");
        String strObjectId = (String) paramMap.get("objectId");
        String strSelectTable = (String) paramMap.get("type");
        java.io.File file = (java.io.File) files.get(0);
        String strMess = "";
        if (!file.exists()) {
            _logger.info("======================================文件不存在，请核对文件位置");
            strMess = EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", context.getLocale(), "emxComponents.Mess.ECRUploadNullError");
            res.put("code", "404");
            res.put("mess", strMess);
            return res;
        }
        InputStream inputStream = new FileInputStream(file);
        Workbook workbook = WorkbookFactory.create(inputStream);
        //读取第一个sheet
        Sheet sheet = workbook.getSheetAt(0);
        //拿取sheet行信息
        int iFirstRowNum = sheet.getFirstRowNum();
        int iLastRowNum = sheet.getLastRowNum();
        if (iFirstRowNum == iLastRowNum) {
            _logger.info("======================================excel 内容错误");
            res.put("code", "404");
            strMess = EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", context.getLocale(), "emxComponents.Mess.ECRUploadContentError");
            res.put("mess", strMess);
            return res;
        }
        DomainObject ecr = DomainObject.newInstance(context, strObjectId);
        String strLevel = "0";
        HashMap<String, String> stringStringHashMap = new HashMap<>();
        stringStringHashMap.put("objectId", strObjectId);
        stringStringHashMap.put("expandLevel", strLevel);
        //costing xml 映射
        MapList CostingMappingList = JF_PublicMethodClass_mxJPO.getECRTableMapping(context, strSelectTable);
        //excel 标题和对应下标
        Map excelHeadInfoMap = getExcelHeadInfo(sheet);
        //校验excel数据
        Map checkMap = checkExcelData(sheet, excelHeadInfoMap, CostingMappingList);
        _logger.info("checkMap:{}", checkMap);
        boolean isCheckSuccess = (boolean) checkMap.get("checkResult");
        StringList errorRowIndex = (StringList) checkMap.get("errorRowIndex");
        if ((!isCheckSuccess) && errorRowIndex.size() > 0) {
            res.put("code", "404");
            strMess = EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", context.getLocale(), "emxComponents.Mess.ECRUploadCheckError");
            strMess = strMess.replace("{}", errorRowIndex.join(","));
            res.put("mess", strMess);
            return res;
        }
        MapList ecrCostingTableData = new MapList();


        //costing 表格数据
        if ("JFNewECRCosting".equals(strSelectTable)) {
            ecrCostingTableData = getNewECRBuyTableData(context, JPO.packArgs(stringStringHashMap));
        } else if ("JFNewECRController".equals(strSelectTable)) {
            ecrCostingTableData = getNewECRMakeTableData(context, JPO.packArgs(stringStringHashMap));
        }
        _logger.info("ecrCostingTableData:{}", ecrCostingTableData.size());
        //登录人会签角色
        MapList loginUserSignTaskRole = JF_PublicMethodClass_mxJPO.getLoginUserSignTaskRoleByECRAndUser(context, new String[]{strObjectId, context.getUser()});
        _logger.info("loginUserSignTaskRole:{}", loginUserSignTaskRole);
        //将role value 映射为 前端值 好直接调用之前方法
//        Set roleNameSet = JF_ECRRESTService_mxJPO.getRoleKeyListByMapList(loginUserSignTaskRole);
        //获取excel中除标题数据
        List<Row> excelDataInfo = getExcelDataInfo(sheet);
        try {
            ContextUtil.pushContext(context);
            ContextUtil.startTransaction(context, true);
            //  先确定具体的行能不能编辑再确定该编辑哪些属性 、 最后匹配Excel中得数据
                Map editInfoMap = JF_NewECRRESTService_mxJPO.getEditAttributeRowInExcel(context,ecrCostingTableData, strSelectTable, ecr, loginUserSignTaskRole);
                _logger.info("editInfoMap:{}",editInfoMap);
                //根据Map中的id 跟excel中的id 对应找到excel中修改的行,并获取修改的值转换为Map 进行更新
                updateECRTableData(context, editInfoMap, excelDataInfo, CostingMappingList, excelHeadInfoMap, strObjectId, strSelectTable);
            ContextUtil.commitTransaction(context);
        } catch (Exception e) {
            _logger.error("e:{}", e.getMessage());
            ContextUtil.abortTransaction(context);
            throw new RuntimeException(e);
        } finally {
            ContextUtil.popContext(context);
        }
        res.put("code", "200");
        strMess = EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", context.getLocale(), "emxComponents.Mess.ECRUploadSuccess");
        res.put("mess", strMess);
        _logger.info("----------------------------------  readNewECRExcelAndUpdateData end --------------------------------------------------------");

        return res;
    }

    /**
     * @param sheet
     * @return java.util.List<org.apache.poi.ss.usermodel.Row>
     * @throws
     * @description 获取excel中修改数据
     * @author CHENYAN
     * @date 2024/10/10 16:33
     */

    public static List<Row> getExcelDataInfo(Sheet sheet) {
        List<Row> rowList = new ArrayList<Row>();

        //拿取sheet最后行信息
        int iLastRowNum = sheet.getLastRowNum();
        //从第3行开始
        for (int i = 2; i <= iLastRowNum; i++) {
            rowList.add(sheet.getRow(i));
        }
        return rowList;
    }

    /**
     * @param context
     * @param dataMap             可以修改的数据集合
     * @param excelDataList       所有excel行
     * @param xmlMapping          属性映射
     * @param headMap             excel头信息
     * @param strECRId            ecr id
     * @param strTableName        excel 标识
     * @return void
     * @throws
     * @description 根据数据库中数据找到对应excel中数据更新
     * @author CHENYAN
     * @date 2024/11/26 14:00
     */
    public static void updateECRTableData(Context context, Map dataMap, List<Row> excelDataList, MapList xmlMapping, Map headMap, String strECRId, String strTableName) throws Exception {
        if (dataMap != null && dataMap.size() > 0) {
            String strRelName = "";
            //根据Table获取不同的关系
            if ("JFNewECRCosting".equals(strTableName)) {
                strRelName = "JFECR2PartPrice";
            } else if ("JFNewECRController".equals(strTableName)) {
                strRelName = "JFECR2MakePartPrice";
            }

            for (Object oEntry : dataMap.entrySet()) {
                Map.Entry entry = (Map.Entry) oEntry;
                String strUniqueId = (String) entry.getKey();
                StringList canEditAttrList = (StringList) entry.getValue();
                //编辑属性不为空且大于0 才保存
                if (Objects.nonNull(canEditAttrList) && canEditAttrList.size() > 0) {
                    List<Row> mappingDataList = getExcelRowDataByMap(strUniqueId, excelDataList, headMap, xmlMapping);
                    _logger.info("mappingDataList:{}", mappingDataList);
                    Map updateAttributeValueMap = new HashMap<String, String>();
                    String strUpdateRelId = "";
                    if (mappingDataList != null && mappingDataList.size() > 0) {
                        for (int i = 0; i < mappingDataList.size(); i++) {
                            Row updateRow = mappingDataList.get(i);
                            for (int i1 = 0; i1 < canEditAttrList.size(); i1++) {
                                //获取更新的关系id
                                Map relMap = getRowIndexByRequiredAttribute(headMap, xmlMapping, SELECT_RELATIONSHIP_ID);
                                String strRelKey = (String) relMap.get("id");
                                if (UIUtil.isNotNullAndNotEmpty(strRelKey)) {
                                    int iRelIndex = (int) relMap.get("index");
                                    strUpdateRelId = getCellValue(updateRow, iRelIndex);
                                }
                                String strRequireAttributeName = canEditAttrList.get(i1);
                                Map requiredAttributeMap = getRowIndexByRequiredAttribute(headMap, xmlMapping, strRequireAttributeName);
                                String strAttributeName = (String) requiredAttributeMap.get("id");
                                //防止没有找到映射属性
                                if (UIUtil.isNotNullAndNotEmpty(strAttributeName)) {
                                    int rowIndex = (int) requiredAttributeMap.get("index");
                                    String strCellValue = getCellValue(updateRow, rowIndex);
                                    _logger.info("strCellValue:{}", strCellValue);
                                    //获取到要更新的属性值
                                    if (UIUtil.isNotNullAndNotEmpty(strCellValue)) {
                                        //默认保留两位小数
                                        String str2FCellValue = String.format("%.2f", Double.parseDouble(strCellValue));
                                        updateAttributeValueMap.put(strAttributeName, str2FCellValue);
                                    }
                                }
                            }
                        }
                        _logger.info("strUpdateRelId:{}", strUpdateRelId);
                        String strMql = "";
                        //add by chenyan 2024/10/22 判断是否已经新建了如果已经新建了查询到关系id
                        if (UIUtil.isNullOrEmpty(strUpdateRelId)) {
                            String[] split = strUniqueId.split(",");
                            if (split.length == 2){
                                String strToId = split[1];
                                strToId = strToId.trim();
                                strUpdateRelId = JF_ECRService_mxJPO.checkECRIsConnectPricePart(context, strECRId, strToId, strRelName);
                                if (UIUtil.isNullOrEmpty(strUpdateRelId)) {
                                    strMql = JF_PublicMethodClass_mxJPO.buildStringInStrings("add connection " ,strRelName , " from ",strECRId ," torel " ,strToId," ");
                                }
                            }else {
                                continue;
                            }
                        } else {
                            strMql = JF_PublicMethodClass_mxJPO.buildStringInStrings("mod connection " ,strUpdateRelId ," ");
                        }
                        _logger.info("updateAttributeValueMap:{}", updateAttributeValueMap);
                        StringBuilder sbAttrMql = new StringBuilder();
                        if (updateAttributeValueMap.size() > 0) {
                            for (Object oAttrEntry : updateAttributeValueMap.entrySet()) {
                                Map.Entry attrEntry = (Map.Entry)oAttrEntry;
                                String strAttrName = (String)attrEntry.getKey();
                                String strAttrValue = (String)attrEntry.getValue();
                                sbAttrMql.append(" '");
                                sbAttrMql.append(strAttrName.trim());
                                sbAttrMql.append("' ");
                                sbAttrMql.append(" '");
                                sbAttrMql.append(strAttrValue.trim());
                                sbAttrMql.append("' ");
                            }
                            strMql = JF_PublicMethodClass_mxJPO.buildStringInStrings(strMql,sbAttrMql.toString());
                            _logger.info("strMql:{}",strMql);

                            MqlUtil.mqlCommand(context, Boolean.FALSE, strMql, Boolean.TRUE).trim();
                        }
                    }
                }


            }
        }
    }


    public static String getCellValue(Row row, int cellIndex) {
        Cell cell = row.getCell(cellIndex);
        cell.setCellType(CellType.STRING);
        return String.valueOf(cell.getStringCellValue().trim());
    }

    /**
    *
    *@description 获取ECR的表格头信息
    *@param sheet
    *@return java.util.Map
    *@throws
    *@author CHENYAN
    *@date 2025/6/25 13:39
    */
    public static Map getExcelHeadInfo(Sheet sheet) {
        Map headMap = new HashMap<String, String>();
        Row row = sheet.getRow(0);
        //拿取sheet行信息
        int iFirstRowNum = row.getFirstCellNum();
        int iLastRowNum = row.getLastCellNum();
        for (int i = iFirstRowNum; i < iLastRowNum; i++) {
            String strCell = row.getCell(i, Row.MissingCellPolicy.RETURN_NULL_AND_BLANK) == null ? "" : getCellValue(row, i);
            if (UIUtil.isNotNullAndNotEmpty(strCell)) {
                headMap.put(strCell, i);
            }
        }
        return headMap;
    }

    /**
     * @param sheet      表格
     * @param headMap    表格第一行
     * @param xmlMapping 表格映射信息
     * @return void
     * @throws
     * @description 检查Excel中数据是否符合
     * @author CHENYAN
     * @date 2024/10/10 11:07
     */
    public static Map checkExcelData(Sheet sheet, Map headMap, MapList xmlMapping) {
        HashMap res = new HashMap<>();
        res.put("checkResult", true);
        int lastRowNum = sheet.getLastRowNum();
        //错误行统计
        StringList errorRowIndex = new StringList();
        //从第三行开始
        for (int i = 2; i <= lastRowNum; i++) {
            Row row = sheet.getRow(i);
            for (int i1 = 0; i1 < xmlMapping.size(); i1++) {
                Map xmlMap = (Map) xmlMapping.get(i1);
                //标识该属性是否可编辑
                String strIsEdit = (String) xmlMap.get("isEdit");
                String strFieldId = (String) xmlMap.get("id");
                //表格第一行信息中保存
                if (headMap.containsKey(strFieldId) && "true".equalsIgnoreCase(strIsEdit)) {
                    //拿到行下标
                    Integer rowIndex = (Integer) headMap.get(strFieldId);
                    if (rowIndex != null && rowIndex != -1) {
                        Cell cell = row.getCell(rowIndex);
                        //先保存为字符串
                        CellType cellType = cell.getCellType();
                        String strCellValue = "";
                        if (CellType.STRING.equals(cellType)) {
                            strCellValue = cell.getStringCellValue();
                        } else if (CellType.NUMERIC.equals(cellType)) {
                            strCellValue = String.valueOf(cell.getNumericCellValue());
                        }
                        if (UIUtil.isNotNullAndNotEmpty(strCellValue)) {
                            try {
                                double dCellValue = Double.parseDouble(strCellValue);
                                // add by chenyan 解除不能输入负数校验 2025/05/19
//                                if (dCellValue < 0) {
//                                    _logger.error("strCellValue:{}", strCellValue);
//                                    errorRowIndex.add(String.valueOf(i + 1));
//                                    res.put("checkResult", false);
//                                }
                            } catch (NumberFormatException e) {
                                _logger.error("strCellValue:{}", strCellValue);
                                // 如果转换失败，说明不是有效的数字
                                errorRowIndex.add(String.valueOf(i + 1));
                                res.put("checkResult", false);
                            }
                        }
                    }
                }
            }
        }
        res.put("errorRowIndex", errorRowIndex);
        return res;
    }

    /**
     * @param excelDataList excel 所有行
     * @param headMap       头信息
     * @param xmlMapping    xml excel 映射
     * @return java.util.List<org.apache.poi.ss.usermodel.Row>
     * @throws
     * @description 根据数据库 id 过滤出excel中id相同行
     * @author CHENYAN
     * @date 2024/11/26 14:03
     */
    public static List<Row> getExcelRowDataByMap(String strUniqueId, List<Row> excelDataList, Map headMap, MapList xmlMapping) {
        //获取ID的下标映射
        Map idIndexMap = getRowIndexByRequiredAttribute(headMap, xmlMapping, SELECT_ID);
        int iIndex = (int) idIndexMap.get("index");
        List<Row> filterMapList = (List) excelDataList.stream().filter(excelMap -> {
            Row excelMapData = (Row) excelMap;
            Cell idCell = excelMapData.getCell(iIndex);
            String strExcelDataId = (String) idCell.getStringCellValue();
            return strUniqueId.equals(strExcelDataId);
        }).collect(Collectors.toList());
        return filterMapList;
    }

    /**
     * @param headMap
     * @param xmlMapping
     * @param strRequiredAttribute
     * @return Map   key index  行下标
     * @return key id  表示
     * @return key value  属性名
     * @throws
     * @description 根据必填属性名称获取表格中行对应下标
     * @author CHENYAN
     * @date 2024/10/9 17:08
     */

    public static Map getRowIndexByRequiredAttribute(Map headMap, MapList xmlMapping, String strRequiredAttribute) {
        Map res = new HashMap<>();
        //防止属性和 xml中id不一致时使用
//        for (int i = 0; i < xmlMapping.size(); i++) {
//            Map xmlMap = (Map) xmlMapping.get(i);
//            String strFieldName = (String)xmlMap.get("id");
//            if (strFieldName.equals(strRequiredAttribute) && headMap.containsKey(strRequiredAttribute)){
//                int iRowIndex = (int) headMap.get(strRequiredAttribute);
//                res.put("index",iRowIndex);
//                res.put("id",strRequiredAttribute);
//                res.put("value",xmlMap.get("value"));
//            }
//        }
        if (headMap.containsKey(strRequiredAttribute)) {
            int iRowIndex = (int) headMap.get(strRequiredAttribute);
            res.put("index", iRowIndex);
            res.put("id", strRequiredAttribute);
        }
        return res;
    }


    /**
     * 需求：DR审核校验：在同一个EBOM中，不能存在同样的零件号，不同版本的数据
     * 需求：同一个ECR中，添加校验：即ECR中所有节点只能存在一个版本的数据。
     * DR要校验所有层级中inwork必须是本项目的
     * ECR零件清单中需要校验所有层级中非发布的数据必须是本项目的
     * @param context
     * @param args
     * @author LIUJR
     * @throws
     * @return int
     * @date 11/06/2025 15:01
     * @description
     */
    public int checkPartRevisionAndConnProject(Context context, String[] args) throws Exception{
        _logger.info("checkPartRevisionAndConnProject................................");
        try {
            String ecrOrDaId = args[0];
            DomainObject domainObject = DomainObject.newInstance(context);
            domainObject.setId(ecrOrDaId);
            String strType = domainObject.getInfo(context, DomainConstants.SELECT_TYPE);
            _logger.info("strType:{}", strType);
            //拿取项目
            String projectId = domainObject.getInfo(context, "from[JFChange2Project].to.id");
            _logger.info("projectId:{}", projectId);
            String name = domainObject.getInfo(context, DomainConstants.SELECT_NAME);
            _logger.info("name:{}", name);
            StringList selectBusList = new StringList();
            selectBusList.add(DomainConstants.SELECT_NAME);
            selectBusList.add(DomainConstants.SELECT_ID);
            selectBusList.add(DomainConstants.SELECT_REVISION);
            selectBusList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_V_PART_NUMBER);
            selectBusList.add(DomainConstants.SELECT_CURRENT);
            StringBuffer stringBuffer = new StringBuffer();
            String saveMess = ComponentsUIUtil.getI18NString(context, context.getLocale().toString(), "emxComponent.Mess.ECRPartSave", new String[]{});
            String partConnMess = ComponentsUIUtil.getI18NString(context, context.getLocale().toString(), "emxComponent.Mess.PartConnProject", new String[]{});
            if (JF_PLMConstants_mxJPO.TYPE_JFNewECR.equalsIgnoreCase(strType) || JF_PLMConstants_mxJPO.TYPE_JFFormalECR.equalsIgnoreCase(strType)) {
                StringList relSelList = new StringList();
                relSelList.add(JF_PLMConstants_mxJPO.SELECT_ATTRIBUTE_JF_ECRID);
                relSelList.add(JF_PLMConstants_mxJPO.SELECT_ATTRIBUTE_JF_BOMChangeDes);
                //ECR校验逻辑修改:"ECR零件清单中不允许存在同一零件号的多个修订版"改为:ECR中数据的EBOM中不允许存在同一零件号的多个修订版  20260306 ljr
//                MapList mapList = domainObject.getRelatedObjects(context, JF_PLMConstants_mxJPO.REL_JFECRRelateRoot + "," + JF_PLMConstants_mxJPO.REL_JFECRRoot2Item, // relationship pattern
                MapList mapList = domainObject.getRelatedObjects(context, JF_PLMConstants_mxJPO.REL_JFECRRelateRoot + "," + JF_PLMConstants_mxJPO.REL_Instance, // relationship pattern
                        JF_PLMConstants_mxJPO.TYPE_VPMReference,                                    // object pattern
                        selectBusList,                            // object selects
                        relSelList, // relationship selects
                        false,                                        // to direction
                        true,                                        // from direction
                        (short) 0,                                    // recursion level
                        "",                // object where clause
                        "",
                        (short) 0);
                _logger.info("partMapList:{}", mapList);
                String s = checkPartMapNameAndRevision(context, mapList, JF_PLMConstants_mxJPO.SELECT_ATTR_V_PART_NUMBER);
                _logger.info("s:{}", s);
                if (UIUtil.isNotNullAndNotEmpty(s)) {
                    stringBuffer.append(
                                    ComponentsUIUtil.getI18NString(context, context.getLocale().toString(), "emxComponent.Mess.ECRPartRevision", new String[]{}))
                            .append(s)
                            .append(saveMess);
                }
                MapList mapList1 = domainObject.getRelatedObjects(context, JF_PLMConstants_mxJPO.REL_JFECRRelateRoot + "," + JF_PLMConstants_mxJPO.REL_JFECRRoot2Item, // relationship pattern
                        JF_PLMConstants_mxJPO.TYPE_VPMReference,                                    // object pattern
                        selectBusList,                            // object selects
                        relSelList, // relationship selects
                        false,                                        // to direction
                        true,                                        // from direction
                        (short) 0,                                    // recursion level
                        "",                // object where clause
                        "",
                        (short) 0);
                _logger.info("partMapList:{}", mapList1);
                MapList partMapList = (MapList) mapList1.stream().filter(m -> {
                    Map map = (Map) m;
                    String ecrId = UIUtil.getValue(map, JF_PLMConstants_mxJPO.SELECT_ATTRIBUTE_JF_ECRID);
                    String changeDesc = UIUtil.getValue(map, JF_PLMConstants_mxJPO.SELECT_ATTRIBUTE_JF_BOMChangeDes);
                    if (ecrId.contains(ecrOrDaId)) {
                        Boolean flag = Boolean.TRUE;
                        if (changeDesc.contains("Remove")) {
                            flag = Boolean.FALSE;
                        }
                        return flag;
                    } else {
                        return Boolean.FALSE;
                    }
                }).collect(Collectors.toCollection(MapList::new));
                _logger.info("partMapList:{}", partMapList);
                //分组  将逻辑id 进行分组
                StringList checkPartProjectSpaceMess = checkPartProjectSpace(context, partMapList, projectId, strType);
                _logger.info("checkPartProjectSpaceMess:{}", checkPartProjectSpaceMess);
                if (!checkPartProjectSpaceMess.isEmpty()) {
                    stringBuffer.append(partConnMess.replace("1", name) + checkPartProjectSpaceMess.join(","));
                }
            } else if ("JFDR".equalsIgnoreCase(strType)) {
                StringList zeroIdList = domainObject.getInfoList(context, "from[" + JF_PLMConstants_mxJPO.REL_JFDR2VPMREFERENCE + "].to.id");
                String i18NString = ComponentsUIUtil.getI18NString(context, context.getLocale().toString(), "emxComponent.Mess.EBOMPartRevision", new String[]{});
                StringList hasConnMess = new StringList();
                for (int i = 0; i < zeroIdList.size(); i++) {
                    String partId = zeroIdList.get(i);
                    domainObject.setId(partId);
                    MapList childPartList = domainObject.getRelatedObjects(
                            context,
                            JF_PLMConstants_mxJPO.REL_Instance,
                            JF_PLMConstants_mxJPO.TYPE_VPMReference,
                            selectBusList,
                            new StringList(),
                            false,
                            true,
                            (short) 0,
                            "",
                            "",
                            0
                    );
                    MapList partMapList = DomainObject.getInfo(context, new String[]{partId}, selectBusList);
                    partMapList.addAll(childPartList);
                    String mess = checkPartMapNameAndRevision(context, partMapList, JF_PLMConstants_mxJPO.SELECT_ATTR_V_PART_NUMBER);
                    if (UIUtil.isNotNullAndNotEmpty(mess)) {
                        stringBuffer.append(i18NString.replaceAll("1",domainObject.getAttributeValue(context, JF_PLMConstants_mxJPO.ATTR_V_PART_NUMBER)));
                        stringBuffer.append(mess).append(saveMess);
                    }
                    StringList checkPartProjectSpaceMess = checkPartProjectSpace(context, partMapList, projectId, strType);
                    if (!checkPartProjectSpaceMess.isEmpty()) {
                        hasConnMess.addAll(checkPartProjectSpaceMess);
                    }
                }
                if (!hasConnMess.isEmpty()) {
                    stringBuffer.append(partConnMess.replace("1", name) + hasConnMess.join(","));
                }
            }
            String message = stringBuffer.toString();
            if (UIUtil.isNotNullAndNotEmpty(message)) {
                //提示信息
                _logger.info("message:{}", message);
                emxContextUtil_mxJPO.mqlNotice(context, message);
                return 1;
            } else {
                return 0;
            }
        }catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * DR要校验所有层级中inwork必须是本项目的
     * ECR零件清单中需要校验所有层级中非发布的数据必须是本项目的
     * @param context
     * @param partMapList
     * @param projectId
     * @param strType
     * @author LIUJR
     * @throws
     * @return java.lang.String
     * @date 12/06/2025 09:52
     * @description
     */
    public static StringList checkPartProjectSpace(Context context, MapList partMapList, String projectId, String strType) throws Exception{
        _logger.info("checkPartProjectSpace......................");
        StringList returnMess = new StringList();
        MapList mapList = (MapList) partMapList.stream().filter(m -> {
            Map map = (Map) m;
            Boolean flag = Boolean.FALSE;
            String current = UIUtil.getValue(map, DomainConstants.SELECT_CURRENT);
            if (JF_PLMConstants_mxJPO.TYPE_JFNewECR.equalsIgnoreCase(strType) || JF_PLMConstants_mxJPO.TYPE_JFFormalECR.equalsIgnoreCase(strType)) {
                flag = current.equalsIgnoreCase("RELEASED") ? Boolean.FALSE : Boolean.TRUE;
            } else if ("JFDR".equalsIgnoreCase(strType)) {
                flag = current.equalsIgnoreCase("IN_WORK") ? Boolean.TRUE : Boolean.FALSE;
            }
            return flag;
        }).collect(Collectors.toCollection(MapList::new));
        _logger.info("mapList:{}", mapList);
        DomainObject domainObject = DomainObject.newInstance(context);
        for (int i = 0; i < mapList.size(); i++) {
            Map map = (Map) mapList.get(i);
            String id = UIUtil.getValue(map, DomainConstants.SELECT_ID);
            domainObject.setId(id);
            //查询关联项目
//            String partProjectId = "";//domainObject.getInfo(context, "to[JFProject2RootPart].from.id");
            String partProjectId  = new JF_VPMReferenceEBOM_mxJPO().getPartBelongProject(context, new String[]{id});

            _logger.info("partProjectId:{}", partProjectId);
            if (UIUtil.isNullOrEmpty(partProjectId) || !partProjectId.equalsIgnoreCase(projectId)) {
                String number = UIUtil.getValue(map, JF_PLMConstants_mxJPO.SELECT_ATTR_V_PART_NUMBER);
                returnMess.add(number);
            }
        }
        _logger.info("returnMess:{}", returnMess);
        return returnMess;
    }


    /**
     * 判断一个结构中是否有相同的零件，不同版本的数据
     * @param context
     * @param childPartList
     * @param strInfo
     * @author LIUJR
     * @throws
     * @return java.lang.String
     * @date 11/06/2025 15:43
     * @description
     */
    public static String checkPartMapNameAndRevision(Context context, MapList childPartList, String strInfo) throws Exception{
        _logger.info("checkPartMapNameAndRevision......................");
        String strMess = DomainConstants.EMPTY_STRING;
        try {
            StringBuffer stringBuffer = new StringBuffer();
            Map mapListGroupingMap = JF_NewECRService_mxJPO.getMapListGroupingMap(context, childPartList, strInfo);
            _logger.info("mapListGroupingMap:{}", mapListGroupingMap);
            for (Object oEntry : mapListGroupingMap.entrySet()) {
                Map.Entry entry = (Map.Entry) oEntry;
                String strPartName = (String) entry.getKey();
                List relIdList = (List) entry.getValue();
                _logger.info("relIdList:{}", relIdList);
                _logger.info("strPartName:{}", strPartName);
                if (relIdList.size() > 1) {
                    MapList mapList = new MapList();
                    mapList.addAll(relIdList);
                    Map mapListMap = JF_NewECRService_mxJPO.getMapListGroupingMap(context, mapList, SELECT_NAME);
                    for (Object oEntry1 : mapListMap.entrySet()) {
                        Map.Entry entry1 = (Map.Entry) oEntry1;
                        String strPartName1 = (String) entry1.getKey();
                        List relIdList1 = (List) entry1.getValue();
                        if (relIdList1.size() > 1) {
                            //多个版本
                            HashSet partSet = new HashSet();
                            HashSet revisionSet = (HashSet) relIdList1.stream().map(m -> {
                                Map map = (Map) m;
                                String revision = UIUtil.getValue(map, DomainConstants.SELECT_REVISION);
                                String numAndRev = UIUtil.getValue(map, JF_PLMConstants_mxJPO.SELECT_ATTR_V_PART_NUMBER) + "_" + UIUtil.getValue(map, DomainConstants.SELECT_REVISION);
                                partSet.add(numAndRev);
                                return revision;
                            }).collect(Collectors.toCollection(HashSet::new));
                            _logger.info("revisionSet:{}", revisionSet);
                            _logger.info("partSet:{}", partSet);
                            if (revisionSet.size() > 1) {
                                StringList strings = StringList.create(partSet);
                                stringBuffer.append(strPartName).append(":").append(strings.join(",")).append(";");
                            }
                        }
                    }
                }
            }
            if (!stringBuffer.isEmpty()) {
                strMess = stringBuffer.toString();
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        return strMess;
    }

    /**
    *
    *@description 新ECR和旧ECR整合后项目成本履历下载
    *@param context
	*@param args
    *@return java.util.Map
    *@throws
    *@author CHENYAN
    *@date 2025/6/13 15:13
    */
    public static Map projectCostingDownLoad(Context context, String[] args) throws Exception {
        Map res = new HashMap<>();
        String strProjectId = args[0];
        String strPath = args[1];
        _logger.info("strPath:{}",strPath);
        String strFullTemplatePath = JF_ECRService_mxJPO.getTemplatePath("CostChangeHistoryTemplate", strPath);
        _logger.info("strFullTemplatePath:{}",strFullTemplatePath);
        InputStream inputStream = new FileInputStream(strFullTemplatePath);
        Workbook workbook = WorkbookFactory.create(inputStream);
        DomainObject project = DomainObject.newInstance(context, strProjectId);
        String strProjectName = project.getInfo(context, "name");
        String strFileName = EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", context.getLocale(), "emxComponents.ECRFileName.CostHistory");
        //设置单元格边框
        CellStyle cellBorderStyle = workbook.createCellStyle();
        cellBorderStyle.setBorderTop(BorderStyle.THIN);
        cellBorderStyle.setBorderBottom(BorderStyle.THIN);
        cellBorderStyle.setBorderLeft(BorderStyle.THIN);
        cellBorderStyle.setBorderRight(BorderStyle.THIN);
        try {
            //导出所有的ECR信息
            MapList allECRInfoList = getAllECRInfoByProjectBo(context, project);
            Map projectInfo = project.getInfo(context, StringList.create(SELECT_NAME, SELECT_NAME, SELECT_DESCRIPTION));
//            ContextUtil.pushContext(context);
            writeExcelSheet0(context, workbook, projectInfo, allECRInfoList,cellBorderStyle);
            writeExcelSheet1(context, workbook, allECRInfoList,cellBorderStyle);
            writeExcelSheet2(context, workbook, allECRInfoList,cellBorderStyle);
            //保存原始数据 ，不在二次查询  key ECRId  value MapList
            Map<String, MapList> ecrPartMap = getPartMapListByECRList(context, allECRInfoList);
            writeExcelSheet3(context, workbook, allECRInfoList,cellBorderStyle,ecrPartMap);
            writeExcelSheet4(context, workbook, allECRInfoList,cellBorderStyle,ecrPartMap);
//            workbook = getProjectAllECRCostDataByPId(context, workbook, project, cellBorderStyle);
//            workbook = getProjectOneMakePartDataByPId(context, workbook, project, cellBorderStyle);
//            workbook = getProjectAllPartCostDataByPId(context, workbook, project, cellBorderStyle);
//            workbook = getProjectOnePartCostDataByPId(context, workbook, project, cellBorderStyle);
        }catch (Exception e){
            e.printStackTrace();
            _logger.error(e.getMessage());
        }finally {
//            ContextUtil.popContext(context);
        }
        //获取当前时间
        LocalDateTime now = LocalDateTime.now();
        // 将 LocalDateTime 对象转换成指定格式的字符串
        String strFormattedDate = now.format(formatter);
        res.put("file", workbook);
        res.put("fileName", JF_PublicMethodClass_mxJPO.buildStringInStrings("【", strProjectName, "】#", strFileName, "#【", strFormattedDate, "】.xlsx"));
        return res;
    }

    public static MapList getAllECRInfoByProjectBo(Context context ,DomainObject project) throws Exception{
        StringList typeSelectList = JF_Util_mxJPO.basicBolistSel();
        //第一个sheet
        typeSelectList.add(SELECT_ATTRIBUTE_TITLE);
        typeSelectList.add(SELECT_ATTR_JFECRTYPE);
        typeSelectList.add(SELECT_ATTR_JFCHANGESOURCE);
        typeSelectList.add(SELECT_ATTR_JFISPLATFORMPART);
        typeSelectList.add(SELECT_ATTR_JFProjectPhase);
        typeSelectList.add(SELECT_ATTR_JFAFFECTEDFACTORY);
        typeSelectList.add(SELECT_ATTR_JFCHANGESDEVEEXPENSESMANHOURS);
        typeSelectList.add(SELECT_ATTR_JFCHANGESDEVEEXPENSESMANHOURSEXTERNAL);
        typeSelectList.add(SELECT_ATTR_JFCHANGESTRIALEXPENSESMANHOURS);
        typeSelectList.add(SELECT_ATTR_JFCHANGESTRIALEXPENSESMANHOURSEXTERNAL);
        typeSelectList.add(SELECT_ATTR_JFCHANGERESON);
        typeSelectList.add(SELECT_DESCRIPTION);
        typeSelectList.add(SELECT_ATTR_JFECRTYPE);
        typeSelectList.add(SELECT_OWNER);
        typeSelectList.add(SELECT_ORIGINATED);
        typeSelectList.add(SELECT_MODIFIED);
        typeSelectList.add("to[JFChangeEventECR].from.name");
        typeSelectList.add("from[JFECR2CO].to.name");

        //第二个sheet信息
        typeSelectList.add(SELECT_ATTRIBUTE_TITLE);
        //Launch_Manager
        typeSelectList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JFMaterialsFinishedProductsRework);
        //APR
        typeSelectList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JFChangesInvestment);
        typeSelectList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JFChangesDevelopment);
        typeSelectList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JFChangesMould);
        typeSelectList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JFSumInventoryScrapAmount);
        typeSelectList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JFChangesInvestmentExternal);
        typeSelectList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JFChangesDevelopmentExternal);
        typeSelectList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JFChangesMouldExternal);
        typeSelectList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JFSumInventoryScrapAmountExternal);
        //AQE
        typeSelectList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JFChangesFixtures);
        typeSelectList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JFCostOfQuality);
        typeSelectList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JFCostOfTrial);
        typeSelectList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JFChangesFixturesExternal);
        typeSelectList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JFCostOfQualityExternal);
        typeSelectList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JFCostOfTrialExternal);
        //AME
        typeSelectList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JFChangesInvestmentsWholeChair);
        typeSelectList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JFChangesInvestmentsFoaming);
        typeSelectList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JFChangesInvestmentsFaceCovers);
        typeSelectList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JFChangesInvestmentsWholeChairExternal);
        typeSelectList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JFChangesInvestmentsFoamingExternal);
        typeSelectList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JFChangesInvestmentsFaceCoversExternal);
        StringList reSelectList = JF_Util_mxJPO.basicRellistSel();
        MapList maps = project.getRelatedObjects(context,
                JF_PLMConstants_mxJPO.REL_JFChange2Project, // relationship pattern
                JF_PLMConstants_mxJPO.TYPE_JFECR + "," + JF_PLMConstants_mxJPO.TYPE_JFNewECR,                                    // object pattern
                typeSelectList,                            // object selects
                reSelectList, // relationship selects
                true,                                        // to direction
                false,                                        // from direction
                (short) 1,                                    // recursion level
                "current!=Create",                // object where clause
                "",
                (short) 0);
        _logger.info("maps:{}", maps);
        return maps;
    }

    /**
    *
    *@description 写入第一个sheet
    *@param context
	*@param workbook excel对象
	*@param projectInfo 项目信息集合
	*@param ECRInfoList 所有ECR信息
	*@param cellStyle
    *@return void
    *@throws
    *@author CHENYAN
    *@date 2025/6/16 15:44
    */
    public static void  writeExcelSheet0(Context context, Workbook workbook, Map projectInfo ,MapList ECRInfoList,CellStyle cellStyle) throws Exception{
        _logger.info("-------------------------------------------- writeExcelSheet0 begin --------------------------------------------------------");
        Sheet sheet = workbook.getSheetAt(0);
        //获取xml表格首页映射
        MapList costChangeHistoryMapping0 = JF_PublicMethodClass_mxJPO.getJFCostChangeHistoryTemplateMapping(context, "CostChangeHistory_0");
        _logger.info("costChangeHistoryMapping0:{}", costChangeHistoryMapping0);
        for (int i = 1; i <= ECRInfoList.size(); i++) {
            Row row = sheet.createRow(i);
            Map rowData = (Map) ECRInfoList.get(i - 1);
            //往 mapdata中添加项目数据
            rowData.put("projectCode", projectInfo.get(SELECT_NAME));
            rowData.put("projectName", projectInfo.get(SELECT_DESCRIPTION));
            for (Object entry : rowData.entrySet()) {
                Map.Entry entryMap = (Map.Entry) entry;
                String strKey = (String) entryMap.getKey();
                String strIndex = "";
                String strCellValue = "";
                //初始化单元格值 防止属性中存在range值多选的情况
                Object oCellValue = entryMap.getValue();
                boolean isMul = false;
                if (oCellValue instanceof StringList) {
                    isMul = true;
                } else {
                    strCellValue = (String) oCellValue;
                }
                //获取excel中对应下标
                for (int i1 = 0; i1 < costChangeHistoryMapping0.size(); i1++) {
                    Map mappingMap = (Map) costChangeHistoryMapping0.get(i1);
                    String strMappingId = (String) mappingMap.get("id");
                    if (mappingMap.containsValue(strKey)) {
                        strIndex = (String) mappingMap.get("value");
                        //格式化处理
                        String strFormat = (String) mappingMap.get("format");
                        if (UIUtil.isNotNullAndNotEmpty(strFormat)) {
                            String strNlsKey = (String) mappingMap.get("nlsKey");
                            if ("date".equals(strFormat)) {

                            } else if ("range".equals(strFormat)) {
                                if (isMul) {
                                    StringList mulValueList = (StringList) oCellValue;
                                    if (mulValueList.size() > 0) {
                                        StringList nlsRanges = EnoviaResourceBundle.getAttrRangeI18NStringList(context, strNlsKey, mulValueList, context.getLocale().toString());
                                        strCellValue = nlsRanges.join("\n");
                                    }
                                } else {
                                    if (UIUtil.isNotNullAndNotEmpty(strCellValue)) {
                                        strCellValue = EnoviaResourceBundle.getRangeI18NString(context, strNlsKey, strCellValue, context.getLocale().getLanguage());
                                    }
                                }
                            } else if ("policy".equals(strFormat)) {
                                strCellValue = EnoviaResourceBundle.getStateI18NString(context, strNlsKey, strCellValue, context.getLocale().toString());
                            } else if ("user".equals(strFormat)) {
                                strCellValue = PersonUtil.getFullName(context, strCellValue);
                                _logger.info("strCellValue:{}", strCellValue);
                            }
                        }
                        break;
                    }
                }

                if (UIUtil.isNotNullAndNotEmpty(strIndex)) {
                    Cell cell = row.createCell(Integer.valueOf(strIndex));
                    cell.setCellValue(strCellValue);
                }
            }
        }
        _logger.info("-------------------------------------------- writeExcelSheet0 end --------------------------------------------------------");

    }

    /**
    *
    *@description 写入第二个sheet页
    *@param context
	*@param workbook excel对象
	*@param ECRInfoList 所有ECR信息
	*@param cellStyle
    *@return void
    *@throws
    *@author CHENYAN
    *@date 2025/6/16 15:43
    */
    public static void  writeExcelSheet1(Context context, Workbook workbook ,MapList ECRInfoList,CellStyle cellStyle) throws Exception{
        _logger.info("-------------------------------------------- writeExcelSheet1 begin --------------------------------------------------------");

        Sheet sheet = workbook.getSheetAt(1);
        //获取xml表格首页映射
        MapList costChangeHistoryMapping0 = JF_PublicMethodClass_mxJPO.getJFCostChangeHistoryTemplateMapping(context, "CostChangeHistory_1");
        _logger.info("costChangeHistoryMapping1:{}", costChangeHistoryMapping0);
        for (int i = 2; i <= (ECRInfoList.size() + 1); i++) {
            Row row = sheet.createRow(i);
            Map rowData = (Map) ECRInfoList.get(i - 2);
            //往 mapdata中添加项目数据
            for (Object entry : rowData.entrySet()) {
                Map.Entry entryMap = (Map.Entry) entry;
                String strKey = (String) entryMap.getKey();
                String strIndex = "";
                String strCellValue = "";
                //初始化单元格值 防止属性中存在range值多选的情况
                Object oCellValue = entryMap.getValue();
                boolean isMul = false;
                if (oCellValue instanceof StringList) {
                    isMul = true;
                } else {
                    strCellValue = (String) oCellValue;
                }
                //获取excel中对应下标
                for (int i1 = 0; i1 < costChangeHistoryMapping0.size(); i1++) {
                    Map mappingMap = (Map) costChangeHistoryMapping0.get(i1);
                    if (mappingMap.containsValue(strKey)) {
                        strIndex = (String) mappingMap.get("value");
                        break;
                    }
                }
                if (UIUtil.isNotNullAndNotEmpty(strIndex)) {
                    Cell cell = row.createCell(Integer.valueOf(strIndex));
                    cell.setCellValue(strCellValue);
                }
            }
        }
        _logger.info("-------------------------------------------- writeExcelSheet1 end --------------------------------------------------------");

    }

    /**
    *
    *@description
    *@param context
	*@param workbook
	*@param ECRInfoList
	*@param cellStyle
    *@return void
    *@throws
    *@author CHENYAN
    *@date 2025/6/18 14:40
    */
    public static void  writeExcelSheet2(Context context, Workbook workbook ,MapList ECRInfoList,CellStyle cellStyle) throws Exception{
        _logger.info("-------------------------------------------- writeExcelSheet2 begin --------------------------------------------------------");

        String strLevel = "0";
        HashMap<String, String> argsMap = new HashMap<>();
        argsMap.put("expandLevel", strLevel);
        DomainObject ECR = DomainObject.newInstance(context);
        Map writeRowMap = new HashMap<String,Map>();
        for (int i = 0; i < ECRInfoList.size(); i++) {
            Map ECRInfo = (Map) ECRInfoList.get(i);
            String strECRId = (String) ECRInfo.get(SELECT_ID);
            String strCurrentECRName = (String) ECRInfo.get(SELECT_NAME);
            String strCurrentState = (String) ECRInfo.get(SELECT_CURRENT);
            ECR.setId(strECRId);
            //根据ECR类型去查询不同的自制件清单逻辑
            String strECRType = (String) ECRInfo.get(SELECT_TYPE);
            if (TYPE_JFECR.equals(strECRType)){
                StringList reSelectList = new StringList(SELECT_RELATIONSHIP_ID);
                StringList typeSelectList = new StringList(SELECT_ID);
                typeSelectList.add(SELECT_TYPE);
                typeSelectList.add(SELECT_NAME);
                typeSelectList.add(SELECT_REVISION);
                typeSelectList.add(SELECT_CURRENT);
                typeSelectList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JFDIRECT_BUY);
                typeSelectList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JFPartType);
                typeSelectList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JF_ProcurementType);
                typeSelectList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_V_PART_NUMBER);
                typeSelectList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JF_PartNameCN);
                typeSelectList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JF_PartNameEN);
                typeSelectList.addAll(getPriceAttrListByTableName("JFECRController",strECRId,strECRType));
                reSelectList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JFECRName);
                reSelectList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JFWholeChair);
                reSelectList.add(SELECT_FROM_ID);
                reSelectList.add(SELECT_RELATIONSHIP_ID);
                MapList maps = ECR.getRelatedObjects(context, JF_PLMConstants_mxJPO.REL_JFECR2Manufacturing  + "," + JF_PLMConstants_mxJPO.RELATIONSHIP_JFRootPart2OnePart, // relationship pattern
                        TYPE_VPMReference,                                    // object pattern
                        typeSelectList,                            // object selects
                        reSelectList, // relationship selects
                        false,                                        // to direction
                        true,                                        // from direction
                        (short) 2,                                    // recursion level
                        "",                // object where clause
                        "",
                        (short) 0);
                MapList filterMapList = (MapList) maps.stream().filter(m -> {
                    Map infoMap = (Map) m;
                    String strRelationship = UIUtil.getValue(infoMap, "relationship");
                    //如果是一级件下面零件需要判断关系属性ECRName是否保存的当前ECR信息
                    if (JF_PLMConstants_mxJPO.RELATIONSHIP_JFRootPart2OnePart.equals(strRelationship)) {
                        String strECRName = UIUtil.getValue(infoMap, JF_PLMConstants_mxJPO.SELECT_ATTR_JFECRName);
                        return strCurrentECRName.equals(strECRName);
                    }
                    return true;
                }).collect(Collectors.toCollection(MapList::new));

                if (Objects.nonNull(filterMapList) && filterMapList.size() > 0){
                    for (int i1 = 0; i1 < filterMapList.size(); i1++) {
                        Map partInfoMap = (Map) filterMapList.get(i1);
                        String strPartName = (String) partInfoMap.get(SELECT_NAME);
                        String strPartRevision = (String) partInfoMap.get(SELECT_REVISION);
                        String strUniqueKey  = strPartName+strPartRevision ;
                        if (!writeRowMap.containsKey(strUniqueKey)){
                            // 写入ECR信息
                            partInfoMap.put("ECRName", strCurrentECRName);
                            partInfoMap.put("ECRCurrent", strCurrentState);
                            partInfoMap.put("ECRType", strECRType);
                            writeRowMap.put(strUniqueKey,partInfoMap);
                        }
                    }
                }
            }else if (TYPE_JFNewECR.equals(strECRType)){
                argsMap.put("objectId", strECRId);
                MapList newECRPartMapList = JPO.invoke(context, "JF_NewECRService", null, "getNewECRMakeTableData", JPO.packArgs(argsMap), MapList.class);
                //新ECR的自制件显示的是全部结构需要过滤 0级件整椅和一级件面套发泡
                MapList filterMapList = (MapList) newECRPartMapList.stream().filter(m ->{
                    boolean isRowEdit = false ;
                    Map partInfoMap = (Map)m;
                    String strPartType = (String) partInfoMap.get(SELECT_ATTR_JFPartType);
                    String strPartLevel = (String) partInfoMap.get(SELECT_LEVEL);
                    Boolean proEditAccess = null;
                    try {
                        proEditAccess = checkPartIsMakeAndFoamCoating(context, partInfoMap, "");
                    } catch (Exception e) {
                        _logger.error(e.getMessage());
                        proEditAccess = Boolean.FALSE ;
                    }
                    _logger.info("proEditAccess:{}",proEditAccess);
                    if (proEditAccess){
                        _logger.info("strECRId:{}",strECRId);
                        _logger.info("partInfoMap:{}",partInfoMap);
                    }
                    if (proEditAccess && (!partInfoMap.containsKey("freeFlag"))) {
                        if (("C".equals(strPartType) || "X".equals(strPartType)) && "1".equals(strPartLevel)){
                            isRowEdit = true;
                        }else if (("T".equals(strPartType) || "U".equals(strPartType)) && "2".equals(strPartLevel)){
                            isRowEdit = true;
                        }
                    }
                    _logger.info("isRowEdit:{}",isRowEdit);
                    return isRowEdit ;
                }).collect(Collectors.toCollection(MapList::new));
                _logger.info("filterMapList:{}",filterMapList);
                if (Objects.nonNull(filterMapList) && filterMapList.size() > 0){
                    for (int i1 = 0; i1 < filterMapList.size(); i1++) {
                        Map partInfoMap = (Map) filterMapList.get(i1);
                        String strPartName = (String) partInfoMap.get(SELECT_NAME);
                        String strReleasedECRId = (String) partInfoMap.get(SELECT_ATTR_JFConnectECR);
                        String strPartRevision = (String) partInfoMap.get(SELECT_REVISION);
                        String strUniqueKey  = strPartName+strPartRevision ;
                        //发布id为空 或者 发布版id和当前ECRId一直时才放进去
                        if (UIUtil.isNullOrEmpty(strReleasedECRId)){
                            if (!writeRowMap.containsKey(strUniqueKey)){
                                // 写入ECR信息
                                partInfoMap.put("ECRName", strCurrentECRName);
                                partInfoMap.put("ECRCurrent", strCurrentState);
                                partInfoMap.put("ECRType", strECRType);
                                writeRowMap.put(strUniqueKey,partInfoMap);
                            }
                        }else if (UIUtil.isNotNullAndNotEmpty(strReleasedECRId)){
                            if (strECRId.equals(strReleasedECRId)){
                                partInfoMap.put("findReleasedECR",true);
//                                if (!writeRowMap.containsKey(strUniqueKey)){
//                                    // 写入ECR信息
//                                    partInfoMap.put("ECRName", strCurrentECRName);
//                                    partInfoMap.put("ECRCurrent", strCurrentState);
//                                    partInfoMap.put("ECRType", strECRType);
//                                    writeRowMap.put(strUniqueKey,partInfoMap);
//                                }else {
                                    //覆盖之前ECR信息和价格信息
                                partInfoMap.put("ECRName", strCurrentECRName);
                                partInfoMap.put("ECRCurrent", strCurrentState);
                                partInfoMap.put("ECRType", strECRType);
                                writeRowMap.put(strUniqueKey,partInfoMap);
//                                }
                            }else {
                                //当 当前ECR和发布ECR不匹配时价格应当全部设为 0
                                DomainObject ecr = DomainObject.newInstance(context, strReleasedECRId);
                                Map ecrMap = ecr.getInfo(context, StringList.create(SELECT_NAME, SELECT_CURRENT, SELECT_TYPE));
                                //第一次放入
                                if (!writeRowMap.containsKey(strUniqueKey)){
                                    // 写入ECR信息
                                    partInfoMap.put("ECRName", ecrMap.get(SELECT_NAME));
                                    partInfoMap.put("ECRCurrent", ecrMap.get(SELECT_CURRENT));
                                    partInfoMap.put("ECRType", ecrMap.get(SELECT_TYPE));
                                    writeRowMap.put(strUniqueKey,partInfoMap);
                                }else {
                                    //已经存在时需要判断是否已经找到发布的ECR
                                    Map oldMap = (Map) writeRowMap.get(strUniqueKey);
                                    if (!oldMap.containsKey("findReleasedECR")) {
                                        partInfoMap.put("ECRName", ecrMap.get(SELECT_NAME));
                                        partInfoMap.put("ECRCurrent", ecrMap.get(SELECT_CURRENT));
                                        partInfoMap.put("ECRType", ecrMap.get(SELECT_TYPE));
                                    }
                                }
                            }

                        }
                    }
                }
            }
        }

        // 写入Excel
        //获取xml表格首页映射
        MapList costChangeHistoryMapping0 = JF_PublicMethodClass_mxJPO.getJFCostChangeHistoryTemplateMapping(context, "CostChangeHistory_2");
        _logger.info("costChangeHistoryMapping2:{}", costChangeHistoryMapping0);
        _logger.info("writeRowMap:{}", writeRowMap);
        //excel写入
        Sheet sheet = workbook.getSheetAt(2);
        int beginIndex = 2 ;
        for (Object oEntry : writeRowMap.entrySet()) {
            Map.Entry entry = (Map.Entry)oEntry;
            Map partInfoMap = (Map)entry.getValue();
            String strECRType = (String) partInfoMap.get("ECRType");
            Row row = sheet.createRow(beginIndex);
            for (Object partEntry : partInfoMap.entrySet()) {
                Map.Entry entryMap = (Map.Entry) partEntry;
                String strKey = (String) entryMap.getKey();
                //标识符跳过
                if ("findReleasedECR".equals(strKey) || "isZeroPart".equals(strKey)){
                    continue;
                }
                //重组价格
                boolean isPriceAttrFlag = false ;
                if (TYPE_JFNewECR.equals(strECRType)){
                    if (strKey.contains("tomid[JFECR2MakePartPrice].")){
                        strKey = strKey.replace("tomid[JFECR2MakePartPrice].","");
                        isPriceAttrFlag = true;
                    }
                }else if (TYPE_JFECR.equals(strECRType)){
                    if (strKey.contains("to[JFECR2MakePartPrice].")){
                        strKey = strKey.replace("to[JFECR2MakePartPrice].","");
                        isPriceAttrFlag = true;
                    }
                }
                //解决强转报错
                Object oCellValue = (Object) entryMap.getValue();
                String strCellValue = "";
                if (oCellValue instanceof String){
                    strCellValue = (String) oCellValue;
                }else {
                    strCellValue = oCellValue.toString();
                }
                Map foramtterMap = formatterCellValue(context, strKey, strCellValue, costChangeHistoryMapping0);
                strCellValue = (String) foramtterMap.get("value");
                String strIndex = (String) foramtterMap.get("index");
                String strCellFormatterValue = (String) foramtterMap.get("value");
                Boolean isFun = (Boolean) foramtterMap.get("isFun");
                if (UIUtil.isNotNullAndNotEmpty(strIndex)) {
                    Cell cell = row.createCell(Integer.valueOf(strIndex));
                    if (isFun) {
                        // 将列下标转换为列字母
                        //String colLetter1 = new CellReference(0, Integer.parseInt(strIndex)).
                        //funValue="VALUE(|{11}|)+VALUE(|{17}|)"  以 | 切割 然后找到 以{ 开始 }结束 的字符串 获取中间下标值 转换为列字母 拼接行信息 组装为完整函数
                        StringTokenizer strTokenizer = new StringTokenizer(strCellFormatterValue, "|");
                        //公式拼接
                        StringBuffer sb = new StringBuffer();
                        while (strTokenizer.hasMoreTokens()) {
                            String strTmpFunValue = strTokenizer.nextToken();
                            if (strTmpFunValue.startsWith("{") && strTmpFunValue.endsWith("}")) {
                                //获取中间列下标
                                strTmpFunValue = strTmpFunValue.substring(1, strTmpFunValue.length() - 1);
                                //转换为字母加行数 A1 C2 这种
                                strTmpFunValue = new CellReference(beginIndex, Integer.parseInt(strTmpFunValue)).formatAsString();
                            }
                            sb.append(strTmpFunValue);
                        }
                        cell.setCellFormula(sb.toString());
                    } else {
                        //当 该零件的发布ECR（NewECR）不是在改项目中时 价格要置空 add by chenyan
                        if (isPriceAttrFlag && (!partInfoMap.containsKey("findReleasedECR")) && TYPE_JFNewECR.equals(partInfoMap.get("ECRType"))){
                            strCellValue = "";
                        }
                        cell.setCellValue(strCellValue);
                    }
                }
            }
            beginIndex++ ;
        }
        _logger.info("-------------------------------------------- writeExcelSheet2 end --------------------------------------------------------");

    }


    /**
    *
    *@description TODO
    *@param context
	*@param workbook
	*@param ECRInfoList
	*@param cellStyle
    *@return void
    *@throws
    *@author CHENYAN
    *@date 2025/6/19 13:56
    */

    public static void  writeExcelSheet3(Context context, Workbook workbook ,MapList ECRInfoList,CellStyle cellStyle, Map<String, MapList> ecrPartMap) throws Exception{
        _logger.info("-------------------------------------------- writeExcelSheet3 begin --------------------------------------------------------");
        Map writeRowMap = new HashMap<String,Map>();
        for (int i = 0; i < ECRInfoList.size(); i++) {
            Map ECRInfo = (Map) ECRInfoList.get(i);
            String strECRId = (String) ECRInfo.get(SELECT_ID);
            String strCurrentECRName = (String) ECRInfo.get(SELECT_NAME);
            String strCurrentState = (String) ECRInfo.get(SELECT_CURRENT);
            MapList partMapList =  ecrPartMap.get(strECRId);
            //根据ECR类型去查询不同的自制件清单逻辑
            String strECRType = (String) ECRInfo.get(SELECT_TYPE);
            if (TYPE_JFECR.equals(strECRType)){
                partMapList = (MapList) partMapList.stream().filter(m -> {
                    Map infoMap = (Map) m;
                    String strRelationship = UIUtil.getValue(infoMap, "relationship");
                    //如果是一级件下面零件需要判断关系属性ECRName是否保存的当前ECR信息
                    if (JF_PLMConstants_mxJPO.REL_JFOneLevelPart2Part.equals(strRelationship)) {
                        String strECRName = UIUtil.getValue(infoMap, JF_PLMConstants_mxJPO.SELECT_ATTR_JFECRName);
                        return strCurrentECRName.equals(strECRName);
                    }
                    return true;
                }).collect(Collectors.toCollection(MapList::new));
            }else if (TYPE_JFNewECR.equals(strECRType)){
                //需要过滤出行可以编辑的零件
            }
            //深拷贝MapList
            MapList cloneList = SerializationUtils.clone(partMapList);
            setLastVersionAndAddPrice(context,cloneList,writeRowMap,ECRInfo,"0");
        }

        // 写入Excel
        //获取xml表格首页映射
        MapList costChangeHistoryMapping0 = JF_PublicMethodClass_mxJPO.getJFCostChangeHistoryTemplateMapping(context, "CostChangeHistory_3");
        _logger.info("costChangeHistoryMapping3:{}", costChangeHistoryMapping0);
        //excel写入
        Sheet sheet = workbook.getSheetAt(3);
        int beginIndex = 2 ;
        for (Object oEntry : writeRowMap.entrySet()) {
            Map.Entry entry = (Map.Entry)oEntry;
            Object strName = entry.getKey();
            Map partInfoMap = (Map)entry.getValue();
            // add by chenyan 2025/06/30 添加求和列
            JF_ECRService_mxJPO.addSumKeyByMapData(partInfoMap);
            String strECRType = (String) partInfoMap.get("ECRType");
            Row row = sheet.createRow(beginIndex);
            for (Object partEntry : partInfoMap.entrySet()) {
                boolean isPriceAttrFlag = false ;
                Map.Entry entryMap = (Map.Entry) partEntry;
                String strKey = (String) entryMap.getKey();
                //标识符跳过
                if ("findReleasedECR".equals(strKey)){
                    continue;
                }
                //重组价格
                if (TYPE_JFNewECR.equals(strECRType)){
                    if (strKey.contains("tomid[JFECR2PartPrice].")){
                        strKey = strKey.replace("tomid[JFECR2PartPrice].","");
                        isPriceAttrFlag = true;
                    }
                }else if (TYPE_JFECR.equals(strECRType)){
                    if (strKey.contains("to[JFECR2PartPrice].")){
                        strKey = strKey.replace("to[JFECR2PartPrice].","");
                        isPriceAttrFlag = true;
                    }
                }
                String strCellValue =  entryMap.getValue().toString();
                Map foramtterMap = formatterCellValue(context, strKey, strCellValue, costChangeHistoryMapping0);
                String strIndex = (String) foramtterMap.get("index");
                String strCellFormatterValue = (String) foramtterMap.get("value");
                strCellValue = strCellFormatterValue ;
                Boolean isFun = (Boolean) foramtterMap.get("isFun");
                if (UIUtil.isNotNullAndNotEmpty(strIndex)) {
                    Cell cell = row.createCell(Integer.valueOf(strIndex));
                    if (isFun) {
                        // 将列下标转换为列字母
                        //String colLetter1 = new CellReference(0, Integer.parseInt(strIndex)).
                        //funValue="VALUE(|{11}|)+VALUE(|{17}|)"  以 | 切割 然后找到 以{ 开始 }结束 的字符串 获取中间下标值 转换为列字母 拼接行信息 组装为完整函数
                        StringTokenizer strTokenizer = new StringTokenizer(strCellFormatterValue, "|");
                        //公式拼接
                        StringBuffer sb = new StringBuffer();
                        while (strTokenizer.hasMoreTokens()) {
                            String strTmpFunValue = strTokenizer.nextToken();
                            if (strTmpFunValue.startsWith("{") && strTmpFunValue.endsWith("}")) {
                                //获取中间列下标
                                strTmpFunValue = strTmpFunValue.substring(1, strTmpFunValue.length() - 1);
                                //转换为字母加行数 A1 C2 这种
                                strTmpFunValue = new CellReference(beginIndex, Integer.parseInt(strTmpFunValue)).formatAsString();
                            }
                            sb.append(strTmpFunValue);
                        }
                        cell.setCellFormula(sb.toString());
                    } else {
                        //当 该零件的发布ECR不是在改项目中时 价格要置空 add by chenyan
                        if (isPriceAttrFlag && (!partInfoMap.containsKey("findReleasedECR"))){
                            strCellValue = "";
                        }
                        cell.setCellValue(strCellValue);
                    }
                }
            }
            beginIndex++ ;
        }
        _logger.info("-------------------------------------------- writeExcelSheet3 end --------------------------------------------------------");

    }



    public static void  writeExcelSheet4(Context context, Workbook workbook ,MapList ECRInfoList,CellStyle cellStyle ,Map<String, MapList> ecrPartMap) throws Exception{
        _logger.info("-------------------------------------------- writeExcelSheet4 begin --------------------------------------------------------");
        Map writeRowMap = new HashMap<String,Map>();
        for (int i = 0; i < ECRInfoList.size(); i++) {
            Map ECRInfo = (Map) ECRInfoList.get(i);
            String strECRId = (String) ECRInfo.get(SELECT_ID);
            String strCurrentECRName = (String) ECRInfo.get(SELECT_NAME);
            MapList partMapList =  ecrPartMap.get(strECRId);
            //根据ECR类型去查询不同的自制件清单逻辑
            String strECRType = (String) ECRInfo.get(SELECT_TYPE);
            if (TYPE_JFECR.equals(strECRType)){
                partMapList = (MapList) partMapList.stream().filter(m -> {
                    Map infoMap = (Map) m;
                    String strRelationship = UIUtil.getValue(infoMap, "relationship");
                    //如果是一级件下面零件需要判断关系属性ECRName是否保存的当前ECR信息
                    if (JF_PLMConstants_mxJPO.REL_JFOneLevelPart2Part.equals(strRelationship)) {
                        String strECRName = UIUtil.getValue(infoMap, JF_PLMConstants_mxJPO.SELECT_ATTR_JFECRName);
                        return strCurrentECRName.equals(strECRName);
                    }
                    return true;
                }).collect(Collectors.toCollection(MapList::new));
            }
            //深拷贝MapList
            MapList cloneList = SerializationUtils.clone(partMapList);
            setLastVersionAndAddPrice(context,cloneList,writeRowMap,ECRInfo,"1");
        }
        // 写入Excel
        //获取xml表格首页映射
        MapList costChangeHistoryMapping0 = JF_PublicMethodClass_mxJPO.getJFCostChangeHistoryTemplateMapping(context, "CostChangeHistory_4");
        _logger.info("costChangeHistoryMapping4:{}", costChangeHistoryMapping0);
//        _logger.info("writeRowMap:{}", writeRowMap);
        //excel写入
        Sheet sheet = workbook.getSheetAt(4);
        int beginIndex = 2 ;
        for (Object oEntry : writeRowMap.entrySet()) {
            Map.Entry entry = (Map.Entry)oEntry;
            Map partInfoMap = (Map)entry.getValue();
            String strECRType = (String) partInfoMap.get("ECRType");
            Row row = sheet.createRow(beginIndex);
            for (Object partEntry : partInfoMap.entrySet()) {
                Map.Entry entryMap = (Map.Entry) partEntry;
                String strKey = (String) entryMap.getKey();
                //标识符跳过
                if ("findReleasedECR".equals(strKey)){
                    continue;
                }
                //重组价格
                if (TYPE_JFNewECR.equals(strECRType)){
                    if (strKey.contains("tomid[JFECR2PartPrice].")){
                        strKey = strKey.replace("tomid[JFECR2PartPrice].","");
                    }
                }else if (TYPE_JFECR.equals(strECRType)){
                    if (strKey.contains("to[JFECR2PartPrice].")){
                        strKey = strKey.replace("to[JFECR2PartPrice].","");
                    }
                }

                String strCellValue =  entryMap.getValue().toString();
                Map foramtterMap = formatterCellValue(context, strKey, strCellValue, costChangeHistoryMapping0);
                String strIndex = (String) foramtterMap.get("index");
                String strCellFormatterValue = (String) foramtterMap.get("value");
                strCellValue = strCellFormatterValue ;
                Boolean isFun = (Boolean) foramtterMap.get("isFun");
                if (UIUtil.isNotNullAndNotEmpty(strIndex)) {
                    Cell cell = row.createCell(Integer.valueOf(strIndex));
                    if (isFun) {
                        // 将列下标转换为列字母
                        //String colLetter1 = new CellReference(0, Integer.parseInt(strIndex)).
                        //funValue="VALUE(|{11}|)+VALUE(|{17}|)"  以 | 切割 然后找到 以{ 开始 }结束 的字符串 获取中间下标值 转换为列字母 拼接行信息 组装为完整函数
                        StringTokenizer strTokenizer = new StringTokenizer(strCellFormatterValue, "|");
                        //公式拼接
                        StringBuffer sb = new StringBuffer();
                        while (strTokenizer.hasMoreTokens()) {
                            String strTmpFunValue = strTokenizer.nextToken();
                            if (strTmpFunValue.startsWith("{") && strTmpFunValue.endsWith("}")) {
                                //获取中间列下标
                                strTmpFunValue = strTmpFunValue.substring(1, strTmpFunValue.length() - 1);
                                //转换为字母加行数 A1 C2 这种
                                strTmpFunValue = new CellReference(beginIndex, Integer.parseInt(strTmpFunValue)).formatAsString();
                            }
                            sb.append(strTmpFunValue);
                        }
                        cell.setCellFormula(sb.toString());
                    } else {
                        cell.setCellValue(strCellValue);
                    }
                }
            }
            beginIndex++ ;
        }
        _logger.info("-------------------------------------------- writeExcelSheet4 end --------------------------------------------------------");

    }

    public static Map formatterCellValue(Context context ,String strKey ,String strCellValue, MapList mappingList ) throws Exception{
        Map res = new HashMap();
        boolean isFun = Boolean.FALSE ;
        String strIndex = "";
        for (int i1 = 0; i1 < mappingList.size(); i1++) {
            Map mappingMap = (Map) mappingList.get(i1);
            if (mappingMap.containsValue(strKey)) {
                strIndex = (String) mappingMap.get("value");
                //格式化处理
                String strFormat = (String) mappingMap.get("format");
                if (UIUtil.isNotNullAndNotEmpty(strFormat)) {
                    String strNlsKey = (String) mappingMap.get("nlsKey");
                    if ("date".equals(strFormat)) {
                        if (UIUtil.isNotNullAndNotEmpty(strCellValue)) {
                            //转换时间
                            Date javaDate = eMatrixDateFormat.getJavaDate(strCellValue, context.getLocale());
                            LocalDateTime localDateTime = javaDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
                            strCellValue = formatter.format(localDateTime);
                        }
                    } else if ("range".equals(strFormat)) {
                        if (UIUtil.isNotNullAndNotEmpty(strCellValue)) {
                            strCellValue = EnoviaResourceBundle.getRangeI18NString(context, strNlsKey, strCellValue, context.getLocale().getLanguage());
                        }
                    } else if ("policy".equals(strFormat)) {
                        if (UIUtil.isNotNullAndNotEmpty(strCellValue)) {
                            strCellValue = EnoviaResourceBundle.getStateI18NString(context, strNlsKey, strCellValue, context.getLocale().toString());

                        }
                    } else if ("user".equals(strFormat)) {
                        strCellValue = PersonUtil.getFullName(context, strCellValue);
                    } else if ("function".equals(strFormat)) {
                        isFun = Boolean.TRUE;
                        strCellValue = (String) mappingMap.get("funValue");
                    }
                }
                break;
            }
        }
        res.put("isFun",isFun);
        res.put("index",strIndex);
        res.put("value",strCellValue);
        return res ;
    }

    /**
    *
    *@description 累加价格
    *@param oldMap
	*@param newMap
    *@return void
    *@throws
    *@author CHENYAN
    *@date 2025/6/19 15:59
    */
    public static void addPrice(Map oldMap ,Map newMap){
        HashMap<String, String> savePriceMap = new HashMap<>();
        for (Object partEntry : newMap.entrySet()) {
            Map.Entry entry= (Map.Entry)partEntry;
            String strKey = (String) entry.getKey();
            String strValue =  entry.getValue().toString();
            if(UIUtil.isNotNullAndNotEmpty(strValue)){
                String strAttrName = "";
                if ( (!"tomid[JFECR2PartPrice].id".equals(strKey)) && strKey.contains("tomid[JFECR2PartPrice].")){
                    strAttrName = strKey.replace("tomid[JFECR2PartPrice].","");
                }else if ((!"to[JFECR2PartPrice].id".equals(strKey)) && strKey.contains("to[JFECR2PartPrice].")){
                    strAttrName = strKey.replace("to[JFECR2PartPrice].","");
                }
                if (UIUtil.isNotNullAndNotEmpty(strAttrName)){
                    savePriceMap.put(strAttrName,strValue);
                }
            }
        }
        _logger.info("savePriceMap:{}",savePriceMap);
        for (Object partEntry : savePriceMap.entrySet()) {
            Map.Entry entry = (Map.Entry)partEntry;
            String strKey = (String) entry.getKey();
            String strValue = (String) entry.getValue();
            if (UIUtil.isNotNullAndNotEmpty(strValue) && strValue.startsWith("+")){
                strValue = strValue.replace("+","");
            }
            String strFullKey1 = JF_PublicMethodClass_mxJPO.buildStringInStrings("tomid[JFECR2PartPrice].", strKey);
            String strFullKey2 = JF_PublicMethodClass_mxJPO.buildStringInStrings("to[JFECR2PartPrice].", strKey);
            try {
                if (oldMap.containsKey(strFullKey1)) {
                    String strOldPrice = (String) oldMap.get(strFullKey1);
                    try {
                        if (UIUtil.isNotNullAndNotEmpty(strOldPrice)){
                            BigDecimal oldPrice = new BigDecimal(strOldPrice);
                            BigDecimal newPrice = new BigDecimal(strValue);
                            strValue = oldPrice.add(newPrice).toString();
                        }
                    } catch (Exception e) {
                        _logger.error(e.getMessage());
                        strValue = "0.0";
                    }
                    oldMap.put(strFullKey1,strValue);
                    continue;
                }
                if (oldMap.containsKey(strFullKey2)) {
                    String strOldPrice = (String) oldMap.get(strFullKey2);
                    try {
                        if (UIUtil.isNotNullAndNotEmpty(strOldPrice)){
                            if (strOldPrice.startsWith("+")){
                                strOldPrice = strOldPrice.replace("+","");
                            }
                            BigDecimal oldPrice = null;
                            try {
                                oldPrice = new BigDecimal(strOldPrice);
                            } catch (NumberFormatException e) {
                                oldPrice = new BigDecimal(0);
                            }
                            BigDecimal newPrice = null;
                            try {
                                newPrice = new BigDecimal(strValue);
                            } catch (NumberFormatException e) {
                                newPrice = new BigDecimal(0);
                            }
                            strValue = oldPrice.add(newPrice).toString();
                        }
                    } catch (Exception e) {
                        _logger.error(e.getMessage());
                        strValue = "0.0";
                    }
                    oldMap.put(strFullKey2,strValue);
                    continue;
                }
            } catch (Exception e) {
                // 存在异常数据
               _logger.error(e.getMessage());
            }
            // 可能存在第一个没有填写价格 add by chenyan 2025/06/24
            if (!(oldMap.containsKey(strFullKey1) || oldMap.containsKey(strFullKey2))){
                oldMap.put(strFullKey1,strValue);
            }
        }
    }

    /**
    *
    *@description 对不同版本零件价格进行求和
    *@param partMapList
	*@param writeRowMap
	*@param ECRInfo
    *@return void
    *@throws
    *@author CHENYAN
    *@date 2025/6/24 22:51
    */

    public static void setLastVersionAndAddPrice(Context context,MapList partMapList, Map writeRowMap,Map ECRInfo,String strUnique) throws Exception{
        String strCurrentECRName = (String) ECRInfo.get(SELECT_NAME);
        String strCurrentState = (String) ECRInfo.get(SELECT_CURRENT);
        String strECRType = (String) ECRInfo.get(SELECT_TYPE);
        String strECRId = (String) ECRInfo.get(SELECT_ID);
        String strECRModified = (String) ECRInfo.get(SELECT_MODIFIED);
        if (Objects.nonNull(partMapList) && partMapList.size() > 0){
            for (int i1 = 0; i1 < partMapList.size(); i1++) {
                Map partInfoMap = (Map) partMapList.get(i1);
                String strPartName = (String) partInfoMap.get(SELECT_NAME);
                String strPartRevision = (String) partInfoMap.get(SELECT_REVISION);
                String strIsLastVersion = (String) partInfoMap.get(SELECT_ATTR_V_isLastVersion);
                String strReleasedECRId = (String) partInfoMap.get(SELECT_ATTR_JFConnectECR);
                StringBuilder sbUniqueFlag = new StringBuilder();
                if ("0".equals(strUnique)){
                    sbUniqueFlag.append(strPartName);
                }else if ("1".equals(strUnique)){
                    sbUniqueFlag.append(strPartName);
                    sbUniqueFlag.append(strPartRevision);
                }
                String strUniqueFlag = sbUniqueFlag.toString();
                // todo 零件号+版本
                //不存在直接写入
                if (TYPE_JFECR.equals(strECRType) || (TYPE_JFNewECR.equals(strECRType) && strECRId.equals(strReleasedECRId))){
                    if (!writeRowMap.containsKey(strUniqueFlag)){
                        //标识当前ECR是发布版
                        partInfoMap.put("findReleasedECR",true);
                        if ("0".equals(strUnique)){
                            // 第一次直接写入ECR信息 后续通过版本判断 大于保存的版本就覆盖ECR信息
                            partInfoMap.put("ECRName", strCurrentECRName);
                            partInfoMap.put("ECRCurrent", strCurrentState);
                            partInfoMap.put("ECRType", strECRType);
                            partInfoMap.put("ECRModified", strECRModified);
                        }else if ("1".equals(strUnique)){
                            partInfoMap.put("ECRName", strCurrentECRName);
                            partInfoMap.put("ECRCurrent", strCurrentState);
                            partInfoMap.put("ECRType", strECRType);
                            partInfoMap.put("ECRModified", strECRModified);
                        }
                        writeRowMap.put(strUniqueFlag,partInfoMap);
                    }else {
                        //存在累加价格
                        Map oldMap = (Map) writeRowMap.get(strUniqueFlag);

//                        if ("0".equals(strUnique)){
//                            // 第一次直接写入ECR信息 后续通过版本判断 大于保存的版本就覆盖ECR信息
//                            oldMap.put("ECRName", strCurrentECRName);
//                            oldMap.put("ECRCurrent", strCurrentState);
//                            oldMap.put("ECRType", strECRType);
//                            oldMap.put("ECRModified", strECRModified);
//                        }else if ("1".equals(strUnique)){
//                            oldMap.put("ECRName", strCurrentECRName);
//                            oldMap.put("ECRCurrent", strCurrentState);
//                            oldMap.put("ECRType", strECRType);
//                            oldMap.put("ECRModified", strECRModified);
//                        }
                        String strOldRevision = (String) oldMap.get(SELECT_REVISION);
                        //最新版直接ECR信息 或者 大于保存的版本写入
                        if ("TRUE".equalsIgnoreCase(strIsLastVersion) || strPartRevision.compareTo(strOldRevision) > 0){
                            _logger.info("strCurrentECRName:{}",strCurrentECRName);
                            // 写入ECR信息
                            oldMap.put("ECRName", strCurrentECRName);
                            oldMap.put("ECRCurrent", strCurrentState);
                            oldMap.put("ECRType", strECRType);
                            oldMap.put("ECRModified", strECRModified);
                            //更新最新版零件信息
                            oldMap.put(SELECT_ATTR_V_PART_NUMBER,partInfoMap.get(SELECT_ATTR_V_PART_NUMBER));
                            oldMap.put(SELECT_ATTR_JFDIRECT_BUY,partInfoMap.get(SELECT_ATTR_JFDIRECT_BUY));
                            oldMap.put(SELECT_ATTR_JFPartType,partInfoMap.get(SELECT_ATTR_JFPartType));
                            oldMap.put(SELECT_ATTR_JF_ProcurementType,partInfoMap.get(SELECT_ATTR_JF_ProcurementType));
                            oldMap.put(SELECT_ATTR_JF_PartNameCN,partInfoMap.get(SELECT_ATTR_JF_PartNameCN));
                            oldMap.put(SELECT_ATTR_JF_PartNameEN,partInfoMap.get(SELECT_ATTR_JF_PartNameEN));
                            oldMap.put(SELECT_REVISION,partInfoMap.get(SELECT_REVISION));
                            oldMap.put(SELECT_CURRENT,partInfoMap.get(SELECT_CURRENT));
                        }else {
                            //  partMap 低版本发布版  old 高版本引用版
                            partInfoMap.put(SELECT_ATTR_V_PART_NUMBER,oldMap.get(SELECT_ATTR_V_PART_NUMBER));
                            partInfoMap.put(SELECT_ATTR_JFDIRECT_BUY,oldMap.get(SELECT_ATTR_JFDIRECT_BUY));
                            partInfoMap.put(SELECT_ATTR_JFPartType,oldMap.get(SELECT_ATTR_JFPartType));
                            partInfoMap.put(SELECT_ATTR_JF_ProcurementType,oldMap.get(SELECT_ATTR_JF_ProcurementType));
                            partInfoMap.put(SELECT_ATTR_JF_PartNameCN,oldMap.get(SELECT_ATTR_JF_PartNameCN));
                            partInfoMap.put(SELECT_ATTR_JF_PartNameEN,oldMap.get(SELECT_ATTR_JF_PartNameEN));
                            partInfoMap.put(SELECT_REVISION,oldMap.get(SELECT_REVISION));
                            partInfoMap.put(SELECT_CURRENT,oldMap.get(SELECT_CURRENT));
                        }
                        //只有是ECR是发布版ECR才能累加价格 add by chenyan 2025/07/03
                        if (oldMap.containsKey("findReleasedECR")){
                            addPrice(oldMap,partInfoMap);
                        }else {
                            partInfoMap.put("findReleasedECR",true);
                            partInfoMap.put("ECRName", strCurrentECRName);
                            partInfoMap.put("ECRCurrent", strCurrentState);
                            partInfoMap.put("ECRType", strECRType);
                            partInfoMap.put("ECRModified", strECRModified);
                            writeRowMap.put(strUniqueFlag,partInfoMap);
                        }
                    }
                }else if ((TYPE_JFNewECR.equals(strECRType)) && UIUtil.isNotNullAndNotEmpty(strReleasedECRId)){
                    DomainObject ecr = DomainObject.newInstance(context, strReleasedECRId);
                    Map ecrMap = ecr.getInfo(context, StringList.create(SELECT_NAME, SELECT_CURRENT, SELECT_TYPE,SELECT_MODIFIED));
                    if (!writeRowMap.containsKey(strUniqueFlag)){
                        partInfoMap.put("ECRName", ecrMap.get(SELECT_NAME));
                        partInfoMap.put("ECRCurrent", ecrMap.get(SELECT_CURRENT));
                        partInfoMap.put("ECRType", ecrMap.get(SELECT_TYPE));
                        partInfoMap.put("ECRModified", ecrMap.get(SELECT_MODIFIED));
                        writeRowMap.put(strUniqueFlag,partInfoMap);
                    }else {
                        Map oldMap = (Map) writeRowMap.get(strUniqueFlag);
                        String strOldRevision = (String) oldMap.get(SELECT_REVISION);
                        if ("TRUE".equalsIgnoreCase(strIsLastVersion) || strPartRevision.compareTo(strOldRevision) > 0){
                            //更新最新版零件信息
                            oldMap.put(SELECT_ATTR_V_PART_NUMBER,partInfoMap.get(SELECT_ATTR_V_PART_NUMBER));
                            oldMap.put(SELECT_ATTR_JFDIRECT_BUY,partInfoMap.get(SELECT_ATTR_JFDIRECT_BUY));
                            oldMap.put(SELECT_ATTR_JFPartType,partInfoMap.get(SELECT_ATTR_JFPartType));
                            oldMap.put(SELECT_ATTR_JF_ProcurementType,partInfoMap.get(SELECT_ATTR_JF_ProcurementType));
                            oldMap.put(SELECT_ATTR_JF_PartNameCN,partInfoMap.get(SELECT_ATTR_JF_PartNameCN));
                            oldMap.put(SELECT_ATTR_JF_PartNameEN,partInfoMap.get(SELECT_ATTR_JF_PartNameEN));
                            oldMap.put(SELECT_REVISION,partInfoMap.get(SELECT_REVISION));
                            oldMap.put(SELECT_CURRENT,partInfoMap.get(SELECT_CURRENT));
                            oldMap.put("ECRName", ecrMap.get(SELECT_NAME));
                            oldMap.put("ECRCurrent", ecrMap.get(SELECT_CURRENT));
                            oldMap.put("ECRType", ecrMap.get(SELECT_TYPE));
                            oldMap.put("ECRModified", ecrMap.get(SELECT_MODIFIED));
                        }
                    }
                }
            }
        }
    }

    public static  Map<String,MapList> getPartMapListByECRList(Context context, MapList ECRInfoList) throws Exception {
        Map<String,MapList> res = new HashMap<>();
        HashMap<String, String> argsMap = new HashMap<>();
        argsMap.put("expandLevel", "0");
        for (int i = 0; i < ECRInfoList.size(); i++) {
            DomainObject ECR = DomainObject.newInstance(context);
                Map ECRInfo = (Map) ECRInfoList.get(i);
                String strECRId = (String) ECRInfo.get(SELECT_ID);
                String strECRType = (String) ECRInfo.get(SELECT_TYPE);
                String strECRName = (String) ECRInfo.get(SELECT_NAME);
                ECR.setId(strECRId);
                MapList partList = new MapList();
            if (TYPE_JFECR.equals(strECRType)) {
                StringList reSelectList = new StringList(SELECT_RELATIONSHIP_ID);
                StringList typeSelectList = new StringList(SELECT_ID);
                typeSelectList.add(SELECT_TYPE);
                typeSelectList.add(SELECT_NAME);
                typeSelectList.add(SELECT_REVISION);
                typeSelectList.add(SELECT_CURRENT);
                typeSelectList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_V_PART_NUMBER);
                typeSelectList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_V_isLastVersion);
                typeSelectList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JFDIRECT_BUY);
                typeSelectList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JFPartType);
                typeSelectList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JF_ProcurementType);
                typeSelectList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JFConnectECR);
                typeSelectList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JF_PartNameCN);
                typeSelectList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JF_PartNameEN);
                typeSelectList.addAll(getPriceAttrListByTableName("JFECRCosting", strECRId, strECRType));
                reSelectList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JFECRName);
                reSelectList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JFWholeChair);
                reSelectList.add(SELECT_FROM_ID);
                reSelectList.add(SELECT_RELATIONSHIP_ID);
                partList = ECR.getRelatedObjects(context, JF_PLMConstants_mxJPO.REL_JFECR2OnelevelPart + "," + JF_PLMConstants_mxJPO.REL_JFOneLevelPart2Part, // relationship pattern
                        TYPE_VPMReference,                                    // object pattern
                        typeSelectList,                            // object selects
                        reSelectList, // relationship selects
                        false,                                        // to direction
                        true,                                        // from direction
                        (short) 2,                                    // recursion level
                        "",                // object where clause
                        "",
                        (short) 0);
            } else if (TYPE_JFNewECR.equals(strECRType)) {
                argsMap.put("objectId", strECRId);
                partList  = JPO.invoke(context, "JF_NewECRService", null, "getNewECRBuyTableData", JPO.packArgs(argsMap), MapList.class);
                // add by chenyan 2025/06/24 新增过滤出采购件中只能编辑的行
                partList = (MapList) partList.stream().filter( m->{
                    Map partInfoMap = (Map)m ;
                    String strPartName = (String) partInfoMap.get(SELECT_NAME);
                    return  partInfoMap.containsKey("rowEditFlag");
                }).collect(Collectors.toCollection(MapList::new));
            }
            res.put(strECRId,partList);
        }

        return res;
    }
    /*
     * @description:ECR受影响零件的共用件查询
     * @author: caipan
     * @date: 2025/7/9 15:08:09
     * @param: * @param[1] context
     * @param[2] args
     * @return:
     **/
    public MapList getAffectPublicList(Context context, String[] args) throws Exception{
        try {
            StringList selList = JF_Util_mxJPO.basicBolistSel();
            StringList relList = JF_Util_mxJPO.basicRellistSel();
            relList.add("attribute["+ATTR_JF_ECR2AffectedPartId+"]");
            relList.add("attribute["+ATTR_JF_ECR2AffectedGCPartId+"]");
            Map paramsMap = (Map) JPO.unpackArgs(args);
            ContextUtil.pushContext(context);
            String strObjectId = (String) paramsMap.get(STRING_OBJECTID);
            _logger.info("strObjectId:{}",strObjectId);
            DomainObject objectProject = DomainObject.newInstance(context, strObjectId);
            MapList list =   objectProject.getRelatedObjects(context,
                    JF_PLMConstants_mxJPO.REL_JFECR2AffectedProject, //pattern to match relationships
                    TYPE_PROJECT_SPACE, //pattern to match types
                    selList, //the eMatrix StringList object that holds the list of select statement pertaining to Business Obejcts.
                    relList, //the eMatrix StringList object that holds the list of select statement pertaining to Relationships.
                    false, //get To relationships
                    true, //get From relationships
                    (short) 1, //the number of levels to expand, 0 equals expand all.
                    DomainConstants.EMPTY_STRING, //where clause to apply to objects, can be empty ""
                    null, //where clause to apply to relationship, can be empty ""
                    (short) 0); //limit
            _logger.info("list:{}",list);
        return list;
        }catch (Exception e) {
            e.printStackTrace();
            ContextUtil.popContext(context);
        }
        return new MapList();
    }
    /*
     * @description:在Table中显示项目经理和整椅经理
     * @author: caipan
     * @date: 2025/7/9 17:06:24
     * @param: * @param[1] context
     * @param[2] args
     * @return:
     **/
    public Vector getPublicPartName(Context context, String[] args)
            throws Exception {
        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        Map columnMap = (Map) programMap.get("columnMap");
        String columnName = (String)columnMap.get("name");
        MapList objectList = (MapList) programMap.get("objectList");
        Vector retVector = new Vector();
        _logger.info("columnName:{}",columnName);
        JF_ECRProcess_mxJPO process = new JF_ECRProcess_mxJPO();
        if (objectList != null && objectList.size() > 0) {
            Map objectMap = null;
            for (int i = 0; i < objectList.size(); i++) {
                objectMap  = (Map)objectList.get(i);
                String projectId = UIUtil.getValue(objectMap, SELECT_ID);
                String manager = "";
               if("ProjectManager".equalsIgnoreCase(columnName)){
                   manager = JF_Util_mxJPO.getProjectManager(context,new String[]{projectId});
               }else if("ChairManager".equalsIgnoreCase(columnName)){
                   manager = (String)process.getProjectRole(context, projectId, "Chair manager").get(DomainConstants.SELECT_NAME);
               }
                retVector.add(manager);
            }
        }
        return retVector;
    }
    /*
     * @description:获取零件的企业编码和版本  GC000001_AB
     * @author: caipan
     * @date: 2025/7/9 16:44:12
     * @param: * @param[1] context
     * @param[2] id
     * @return:
     **/
    public String getObjectPartNumber(Context context,String id) throws Exception{
        DomainObject domainObject = DomainObject.newInstance(context, id);
        String partName = domainObject.getInfo(context, JF_PLMConstants_mxJPO.SELECT_ATTR_V_PART_NUMBER);
        if(UIUtil.isNullOrEmpty(partName)){
            partName = domainObject.getInfo(context, SELECT_NAME);
        }
        String revision = domainObject.getInfo(context, "revision");
        return partName+"_"+revision;
    }
    /*
     * @description:ECR发起之前，先校验供货件清单是否确认,需要整椅经理来确认
     * @author: caipan
     * @date: 2025/7/24 14:06:29
     * @param: * @param[1] context
     * @param[2] args
     * @return:
     **/
    public int checkSupplyConfirm(Context context, String[] args) throws Exception{
        _logger.info("checkSupplyConfirm................................");
        try {
            String ecrOrDaId = args[0];
            DomainObject domainObject = DomainObject.newInstance(context);
            domainObject.setId(ecrOrDaId);
            //拿取项目
            String projectId = domainObject.getInfo(context, "from[JFChange2Project].to.id");
            //获取到整椅经理
            domainObject.setId(projectId);
            String SupplyAffirm = domainObject.getAttributeValue(context, JF_PLMConstants_mxJPO.ATTR_JF_SupplyAffirm);
            if ("N".equalsIgnoreCase(SupplyAffirm)) {
                JF_ECRProcess_mxJPO process = new JF_ECRProcess_mxJPO();
                Map Chairmanager = process.getProjectRole(context, projectId, "Chair manager");
                String ChairmanagerStr = UIUtil.getValue(Chairmanager, SELECT_NAME);
                if (UIUtil.isNotNullAndNotEmpty(ChairmanagerStr)) {
                    ChairmanagerStr = PersonUtil.getPersonObject(context, ChairmanagerStr).getAttributeValue(context, "First Name");
                    emxContextUtil_mxJPO.mqlNotice(context, JF_Util_mxJPO.getMessage(context, "Mess.JF_SupplyAffirm", ChairmanagerStr));
                    return 1;
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return 0;
      }

    /**
    * 导入ecr的受影响项的零件
    * @param context
	* @param args
    * @author LIUJR
    * @throws
    * @return java.lang.String
    * @date 2025/7/30 14:35
    * @description
    */
    public static void createECRAffectedItemsPart(Context context, String[] args) throws Exception{

        _logger.info("createECRAffectedItemsPart。。。。。。。。。。。。。。。。。。。。");
        FileOutputStream outputStream = null;
        try {
            String objectId = args[0];
            String outFilePath = args[1];
            _logger.info("objectId:{}",objectId);
            _logger.info("outFilePath:{}",outFilePath);
            String strMess = "";
            DomainObject domainObject = DomainObject.newInstance(context);
            domainObject.setId(objectId);
            //获取ecr的项目id
            String psId = domainObject.getInfo(context, "from[JFChange2Project].to.id");
            _logger.info("psId:{}",psId);
            DomainObject projectObject = DomainObject.newInstance(context, psId);
            //拿取项目中的关联的所有的零件信息
            StringList selList = JF_Util_mxJPO.basicBolistSel();
            selList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_V_PART_NUMBER);
            StringList relList = JF_Util_mxJPO.basicRellistSel();
            relList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JFZeroPart);
            relList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JF_BelongPart);
            MapList mapList = projectObject.getRelatedObjects(
                    context,
                    JF_PLMConstants_mxJPO.rel_JFProject2RootPart,
                    JF_PLMConstants_mxJPO.TYPE_VPMReference,
                    selList,
                    relList,
                    false,
                    true,
                    (short) 1,
                    "",
                    "",
                    0
            );
            _logger.info("mapList:{}",mapList.size());
            //将项目中的零件信息进行分组
            HashMap<String, Map<String, String>> psPartMap = new HashMap<>();
            for (int i = 0; i < mapList.size(); i++) {
                Map map = (Map) mapList.get(i);
                String id = UIUtil.getValue(map, SELECT_ID);
                psPartMap.put(id, map);
            }
            /*
            * 1.根据企业编码,去查找该系统中的最新冻结版本，如果没有忽略
            * 2.拿取到最新冻结版本后，查看与ecr的项目是否关联，否，忽略
                  1,2的具体校验：
                  * - Type == VPMReference
                  * - current=='FROZEN'
                  * - to[JFECRRelateRoot]==FALSE
                  * - to[JFRelateItem]==FALSE
                  * - attribute[PLMReference.V_isLastVersion]==TRUE
            * 3.拿取到最新冻结版本零件后，查看该零件是否为整椅，是，保存为整椅集合；否，保存为其他集合
            * 4.将整椅集合和其他件集合合并，整椅在前，其他件在后，遍历集合
            * 5.校验：   - 数据结构中处在“已发布”的数据必须是最新已发布版本（规避加入了历史版本问题）
                        - 数据结构中处在“冻结”的数据必须是最新修订版本
            * 6.校验不过，提示吗？
            * 7.校验通过后，开始搭建结构，先整椅，再其他件
            *
            * */
            JF_NewECRProcess_mxJPO processMxJPO = new JF_NewECRProcess_mxJPO();
            //零件企业编码
            String partNum = EMPTY_STRING;
            //保存每一个受影响项的子级信息
            HashMap<String, StringList> partMap = new HashMap<>();
            //整椅的零件
            StringList zeroPartList = new StringList();
            //校验不通过的零件
            StringList errorPartList = new StringList();
            //所属项目的零件
            StringList belongPartList = new StringList();
            //保存零件对应的excel在第几行
            HashMap<String, Integer> hashMap = new HashMap<>();
            //错误信息
            //无满足条件的零件信息  未查到可导入的零件
            String noPart = ComponentsUIUtil.getI18NString(context, context.getLocale().toString(), "emxComponents.Mess.NoPart", new String[]{});
            //零件不属于ecr项目的零件信息
            String noPsPart = ComponentsUIUtil.getI18NString(context, context.getLocale().toString(), "emxComponents.Mess.NoPsPart", new String[]{});
            //版本校验不过的零件信息
            String revisionError = ComponentsUIUtil.getI18NString(context, context.getLocale().toString(), "emxComponents.Mess.RevisionError", new String[]{});
            //零件包含在其他导入零件信息
            String containsError = ComponentsUIUtil.getI18NString(context, context.getLocale().toString(), "emxComponents.Mess.ContainsError", new String[]{});
            _logger.info("noPart:{}",noPart);
            _logger.info("noPsPart:{}",noPsPart);
            _logger.info("revisionError:{}",revisionError);
            _logger.info("containsError:{}",containsError);
            //读取excel文件中的数据
            java.io.File outfile = null;
            outfile = new File(outFilePath);
            _logger.info("size:{}", outfile.length());
            InputStream inputStream = new FileInputStream(outFilePath);
            Workbook workbook = WorkbookFactory.create(inputStream);
            Sheet sheet = workbook.getSheetAt(0);
            int lastRowNum = sheet.getLastRowNum();
            _logger.info("lastRowNum:{}",lastRowNum);
            //字体 风格
            CellStyle style = workbook.createCellStyle();
            Font font = workbook.createFont();
            font.setColor(IndexedColors.RED.getIndex());
            style.setFont(font);
            StringList itemList = new StringList();
            for (int i = 1; i <= lastRowNum; i++) {
                Row row = sheet.getRow(i);
                Cell cell = row.getCell(0);
                //校验异常信息写入单元格
                Cell cell1 = row.createCell(1, CellType.STRING);
                partNum = cell.getStringCellValue().trim();
                _logger.info("partNum:{}",partNum);
                //获取系统中属于该项目的零件的最新冻结版本
                /* 1.Type == VPMReference;2.current=='FROZEN';3.to[JFECRRelateRoot]==FALSE;4.to[JFRelateItem]==FALSE;5.attribute[PLMReference.V_isLastVersion]==TRUE
                * */
                MapList res = DomainObject.findObjects(context, JF_PLMConstants_mxJPO.TYPE_VPMReference, "*",
                        "current=='FROZEN'" + "&&to[JFECRRelateRoot]==FALSE" + "&&to[JFRelateItem]==FALSE" +
                        "&&attribute[PLMReference.V_isLastVersion]==TRUE" +
                        "&&attribute[EnterpriseExtension.V_PartNumber]=='" + partNum +"'", new StringList(SELECT_ID));
                _logger.info("res:{}",res);
                if (res.isEmpty()) {
                    cell1.setCellValue(noPart);
                    cell1.setCellStyle(style);
                    errorPartList.add(partNum);
                    continue;
                }
                //获取零件信息 id
                Map map = (Map) res.get(0);
                String partId = UIUtil.getValue(map, SELECT_ID);
                _logger.info("partId:{}",partId);
                //判断零件是否属于该项目
                if (!psPartMap.containsKey(partId)) {
                    //判断是否是ecr项目的零件，不是跳过
                    cell1.setCellValue(noPsPart);
                    cell1.setCellStyle(style);
                    errorPartList.add(partNum);
                    continue;
                }
                //校验数据结构中处在“已发布”的数据必须是最新已发布版本（规避加入了历史版本问题）
                //数据结构中处在“冻结”的数据必须是最新修订版本
                strMess = processMxJPO.checkVPMReferenceStateIsLastRevision(context, new StringList(partId), partMap);
                _logger.info("strMess:{}",strMess);
                if (UIUtil.isNotNullAndNotEmpty(strMess)) {
                    //判断是否是ecr项目的零件，不是跳过
                    cell1.setCellValue(revisionError + strMess);
                    cell1.setCellStyle(style);
                    partMap.remove(partMap);
                    errorPartList.add(partNum);
                    continue;
                }
                //将供货件和所属项目件分开保存
                Map<String, String> partMap1 = psPartMap.get(partId);
                String zeroPart = UIUtil.getValue(partMap1, JF_PLMConstants_mxJPO.SELECT_ATTR_JFZeroPart);
                String belongPart = UIUtil.getValue(partMap1, JF_PLMConstants_mxJPO.SELECT_ATTR_JF_BelongPart);
                if ("Y".equalsIgnoreCase(zeroPart)) {
                    zeroPartList.add(partId);
                } else if ("Y".equalsIgnoreCase(belongPart)) {
                    belongPartList.add(partId);
                }
                //记录行信息 保存行数，后续校验需要
                hashMap.put(partId, i);
                //目前所有满足条件的partid
                itemList.add(partId);
            }
            _logger.info("belongPartList:{}",belongPartList);
            _logger.info("zeroPartList:{}",zeroPartList);
            //搭建结构 变更对象
            zeroPartList.addAll(belongPartList);
            //开始做包含校验，零件包含在其他导入零件信息
            for (Map.Entry<String, StringList> entry : partMap.entrySet()) {
                String number = entry.getKey();
                StringList value = entry.getValue();
                // 处理键值对  将需要导入为受影响项目的零件与每一个map中的list做一个交集
                // 求交集
                StringList intersection = itemList.stream()
                        .filter(value::contains)
                        .collect(Collectors.toCollection(StringList::new));
                //交集中数据在整椅key中存在，需要移除
                for (int i = 0; i < intersection.size(); i++) {
                    String partId = intersection.get(i);
                    Integer integer = hashMap.get(partId);
                    Cell cell = sheet.getRow(integer).createCell(1);
                    cell.setCellValue(containsError.replace("1", number));
                    cell.setCellStyle(style);
                    errorPartList.add(partId);
                    zeroPartList.remove(partId);
                }
            }
            _logger.info("zeroPartList:{}",zeroPartList);
            outputStream = new FileOutputStream(outFilePath);
            workbook.write(outputStream);
            //开始导入,下面两个方法不要轻易改,需要兼容页面上的添加受影响项
            if (!zeroPartList.isEmpty()) {
                //添加受影响项
                Map pack2Map = new HashMap<>();
                pack2Map.put("objectId", objectId);
                pack2Map.put("partList", zeroPartList);
                JPO.invoke(context, "JF_NewECRService", null, "AddAffectedItemsAndCompareStructures", JPO.packArgs(pack2Map), null);
                // 添加相关受影响父件  去除该功能   update by  ljr 20260317
//                Map packMap = new HashMap<>();
//                packMap.put("ecrId", objectId);
//                packMap.put("partList", zeroPartList);
//                JPO.invoke(context, "JF_NewECRProcess", null, "processAffectedItemAdd", JPO.packArgs(packMap), null);
            }
            //发送邮件
            sendECRImportPartResultEmail(context, objectId, zeroPartList.size() == lastRowNum, outFilePath, zeroPartList, errorPartList);
            _logger.info("import end.................");
        }catch (Exception e) {
            e.printStackTrace();
            throw e;
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close(); // 关闭流
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
    * ECR导入零件结果，发送邮件
    * @param context
	* @param objectId
	* @param flag
	* @param outFilePath
	* @param zeroPartList
	* @param errorPartList
    * @author LIUJR
    * @throws
    * @return void
    * @date 2025/8/4 15:41
    * @description
    */
    public static void sendECRImportPartResultEmail(Context context, String objectId, Boolean flag, String outFilePath, StringList zeroPartList, StringList errorPartList) throws Exception{
        try {
            DomainObject domainObject = DomainObject.newInstance(context);
            domainObject.setId(objectId);
            String name = domainObject.getInfo(context, SELECT_NAME);
            String owner = domainObject.getInfo(context, SELECT_OWNER);
            domainObject = PersonUtil.getPersonObject(context, owner);
            String emailAddress = domainObject.getAttributeValue(context, DomainObject.ATTRIBUTE_EMAIL_ADDRESS);
            // 创建多部分消息体
            MimeMultipart multipart = new MimeMultipart();
            //邮件内容的html模板部分
            BodyPart msgBodyPart = new MimeBodyPart();
            //邮件内容的html模板部分
            //拿取html模板
            String html = JF_PublicMethodClass_mxJPO.getPageHTMLResourceFile(context, "ECRImportPartEmail", "zh");
            Document doc = Jsoup.parse(html);
            doc.getElementById("ECRName").append(name);
            StringList successList = new StringList();
            StringList errorList = new StringList();
            for (int i = 0; i < zeroPartList.size(); i++) {
                domainObject.setId(zeroPartList.get(i));
                successList.add(domainObject.getAttributeValue(context, ATTR_V_PART_NUMBER));
            }
            for (int i = 0; i < errorPartList.size(); i++) {
                String id = errorPartList.get(i);
                if (id.contains(".")) {
                    domainObject.setId(id);
                    errorList.add(domainObject.getAttributeValue(context, ATTR_V_PART_NUMBER));
                } else {
                    errorList.add(id);
                }
            }
            doc.getElementById("Success").append(successList.join(","));
            if (flag) {
                Element failed = doc.getElementById("Failed");
                //成功
//                failed.attr("style", "display: none;");
                //将异常的元素设置为不可见
                String currentStyle = failed.attr("style");
                String newStyle = currentStyle.replace("display: flex;", "display: none;");
                failed.attr("style", newStyle); // 更新style属性
            } else {
                Element error = doc.getElementById("Error");
                error.append(errorList.join(","));
                //附件
                // 创建附件部分（使用jakarta包）
                MimeBodyPart attachmentPart = new MimeBodyPart();
                java.io.File file = new java.io.File(outFilePath);
                // 2. 添加文件附件部分
                // 使用jakarta.activation.DataHandler
                DataSource source = new FileDataSource(file);
                attachmentPart.setDataHandler(new DataHandler(source));
                // 设置附件文件名（处理中文文件名编码）
                attachmentPart.setFileName(MimeUtility.encodeText(file.getName()));
                multipart.addBodyPart(attachmentPart);
            }
            Properties prop = NioJDUtils.readPageObject(context, "JFJDConfig");
            String linkAddress = prop.getProperty("JF.3dspace.JFUrl").trim();
            linkAddress += objectId;
            //设置链接地址
            Element address = doc.getElementById("Address");
            //链接href
            address.attr("href", linkAddress);
            //设置显示的值
            address.text(name);
            String htmlContent = doc.toString();
            msgBodyPart.setContent(htmlContent, "text/html;charset=utf-8");//html代码部分
            multipart.addBodyPart(msgBodyPart);
            String strSubject = "ECR零件导入结果通知";
            //开始构造邮件
            Boolean aBoolean = JF_SendEmailUtils_mxJPO.SendEmail(context, emailAddress, strSubject, multipart);
            _logger.info("邮件发送状态： {}",aBoolean);
        } catch (FrameworkException e) {
            throw e;
        }
    }


    public String buildQuestListHtml(Context context, String[] args) throws Exception {
        _logger.info("--------------------- buildQuestListHtml begin --------------------------");
        String remove = EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", context.getLocale(), "emxComponents.Common.SignatoriesRemovePerson");

        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        HashMap requestMap = (HashMap) programMap.get("requestMap");
        HashMap fieldMap = (HashMap) programMap.get("fieldMap");
        String strMode = (String) requestMap.get("mode");
        String strLoginUser = context.getUser();
        String strLang = context.getSession().getLanguage();
        String strFileUploadNls = ComponentsUtil.i18nStringNow("emxComponents.Common.JFECRQQFileUpload", strLang);
        String strChangeSource = "";
        String strIsPlatForm = "";
        String objectId = null;
        if (requestMap == null) {
            objectId = (String) programMap.get("objectId");
        } else {
            objectId = (String) requestMap.get("objectId");
        }
        StringBuffer sbDocDown = new StringBuffer();

        _logger.info("objectId:{}", objectId);
        if (UIUtil.isNotNullAndNotEmpty(objectId)) {
            DomainObject ECR = DomainObject.newInstance(context, objectId);
            StringList ECRSelectList = new StringList();
            ECRSelectList.add(SELECT_ATTR_JFQQID);
            ECRSelectList.add(SELECT_CURRENT);
            ECRSelectList.add(SELECT_OWNER);
            ECRSelectList.add(SELECT_ATTR_JFCHANGESOURCE);
            ECRSelectList.add(SELECT_ATTR_JFISPLATFORMPART);
            Map ECRInfoMap = ECR.getInfo(context, ECRSelectList);
            String strQQID = (String) ECRInfoMap.get(SELECT_ATTR_JFQQID);
            String strCurrent = (String) ECRInfoMap.get(SELECT_CURRENT);
            String strOwner = (String) ECRInfoMap.get(SELECT_OWNER);
            strChangeSource = (String) ECRInfoMap.get(SELECT_ATTR_JFCHANGESOURCE);
            strChangeSource="";
            strIsPlatForm = (String) ECRInfoMap.get(SELECT_ATTR_JFISPLATFORMPART);
            boolean isEdit = strLoginUser.equals(strOwner) && "Create".equals(strCurrent) ? true : false;
            StringList selectTypeList = new StringList();
            selectTypeList.add(SELECT_ID);
            selectTypeList.add(SELECT_ATTRIBUTE_TITLE);
            selectTypeList.add(SELECT_TYPE);
            selectTypeList.add(SELECT_NAME);
            selectTypeList.add(SELECT_REVISION);
            MapList docList = ECR.getRelatedObjects(context, RELATIONSHIP_REFERENCE_DOCUMENT, // relationship pattern
                    TYPE_DOCUMENT,                                    // object pattern
                    selectTypeList,                            // object selects
                    new StringList(SELECT_RELATIONSHIP_ID), // relationship selects
                    false,                                        // to direction
                    true,                                        // from direction
                    (short) 1,                                    // recursion level
                    "",                // object where clause
                    "attribute[Project Role]=='Questions List'",
                    (short) 0);
            StringList docIDList = (StringList) docList.stream().map(m -> {
                Map doc = (Map) m;
                return doc.get(SELECT_ID);
            }).collect(Collectors.toCollection(StringList::new));
            sbDocDown.append("<table");
            sbDocDown.append("><tr>");
            sbDocDown.append("<td>");
            strQQID = "";
            strFileUploadNls = StringEscapeUtils.escapeHtml4(strFileUploadNls);
            if ("view".equals(strMode)) {
                sbDocDown.append(JF_PublicMethodClass_mxJPO.buildStringInStrings("<span>", strQQID, "</span>&nbsp;"));
            } else if ("edit".equals(strMode)) {
                if (isEdit) {
                    sbDocDown.append("<input value=\"" + strQQID + "\" id=\"JFQuestionsList\" name=\"JFQuestionsList\" type=\"text\"  size=\"20\">");
                } else {
                    sbDocDown.append(JF_PublicMethodClass_mxJPO.buildStringInStrings("<span>", strQQID, "</span>&nbsp;"));
                }
            }
            sbDocDown.append("</td>");
            StringList QQFileIdList = new StringList();
            if (isEdit && "edit".equals(strMode)) {
                sbDocDown.append("<td id=\"fileTd\">");
                sbDocDown.append("<input  id=\"JFECRQQFileId2\" name=\"JFECRQQFileId2\" value=\"" + docIDList.join(",") + "\" type=\"hidden\">");
                sbDocDown.append("<input onclick=\"javascript:showModalDialog('../components/JF_ECRQuestionsListPreCheckin.jsp?objectAction=checkin&msfBypass=true&");
                sbDocDown.append("objectId=");
                sbDocDown.append("");
                sbDocDown.append("','730','450')\"" +
                        "        value=\"");
                sbDocDown.append(strFileUploadNls);
                sbDocDown.append("\" type=\"button\">");
                sbDocDown.append("</td>");
            }
            sbDocDown.append("<td id=\"JFReplace2\">");
            //Show Counter Link
            for (int i = 0; i < docList.size(); i++) {
                sbDocDown.append("<div ");
                Map docMap = (Map) docList.get(i);
                Map docObjMap = new HashMap<>();
                String strFileId = (String) docMap.get(SELECT_ID);
                QQFileIdList.add(strFileId);
                sbDocDown.append("style='vertical-align:middle;padding-left:1px;cursor:pointer;display:inline-block' ");
                sbDocDown.append("onClick=\"javascript:callCheckout('").append(strFileId).append("',");
                sbDocDown.append("'download', '', '', 'null', 'null', 'structureBrowser', 'PMCPendingDeliverableSummary', 'null')\">");
                sbDocDown.append("<img style='vertical-align:middle;' src='../common/images/").append("iconSmallDocument.gif").append("'");
                sbDocDown.append(" title=\"");
                String strDocTitle = (String) docMap.get(SELECT_ATTRIBUTE_TITLE);
                strDocTitle = StringEscapeUtils.escapeHtml4(strDocTitle);
                sbDocDown.append(strDocTitle);
                sbDocDown.append("\" />");
                sbDocDown.append("</div>");
            }
            sbDocDown.append("</td>");

            if (isEdit && "edit".equals(strMode)) {
                sbDocDown.append("<td>");
                sbDocDown.append("<a href=\"javascript:removeQuestionsListDocument()\">");
                sbDocDown.append("<img src=\"../common/images/iconStatusRemoved.gif\" width=\"12\" height=\"12\" border=\"0\" />");
                sbDocDown.append("</a>");
                sbDocDown.append("<a href=\"javascript:removeQuestionsListDocument()\">");
                //XSSOK
//                sbDocDown.append(remove);
                sbDocDown.append("</a>");
                sbDocDown.append("</td>");
            }
        } else {
            sbDocDown.append("<table");
            sbDocDown.append("><tr>");
            sbDocDown.append("<td>");
            sbDocDown.append("<input value=\"\" id=\"JFQuestionsList\" name=\"JFQuestionsList\" type=\"text\"  size=\"20\"/>");
            sbDocDown.append("</td>");
            sbDocDown.append("<td id=\"fileTd2\">");
            sbDocDown.append("<input id=\"JFECRQQFileId2\" name=\"JFECRQQFileId2\" type=\"hidden\"/>");
            sbDocDown.append("<input onclick=\"javascript:showModalDialog('../components/JF_ECRQuestionsListPreCheckin.jsp?objectAction=checkin&amp;msfBypass=true&amp;objectId=;");
            sbDocDown.append("','730','450')\"" +
                    "  value=\"");
            _logger.info("strFileUploadNls:{}", strFileUploadNls);
            sbDocDown.append(strFileUploadNls);
            sbDocDown.append("\" type=\"button\"/>");
            sbDocDown.append("</td>");
            sbDocDown.append("<td id=\"JFReplace2\">");
            sbDocDown.append("</td>");
        }
        sbDocDown.append("</tr></table>");

        //
        if ("edit".equals(strMode)) {
//            if ("Both".equals(strChangeSource) || "External Changes".equals(strChangeSource)) {
//                sbDocDown.append("<script>");
//                sbDocDown.append("  var  dom = document.getElementById('");
//                String strDomId = "calc_JFQQ";
//                sbDocDown.append(strDomId);
//                sbDocDown.append("');");
//                sbDocDown.append("  if (dom){");
//                sbDocDown.append("    dom.querySelector('td:first-child').className = \"createLabelRequired\";");
//                sbDocDown.append("  }");
//                sbDocDown.append("</script>");
//
//            }
        } else if ("create".equals(strMode)) {
          /*  sbDocDown.append("<script>");
            sbDocDown.append("  var  dom = document.getElementById('");
            String strDomId = "calc_JFQQ";
            sbDocDown.append(strDomId);
            sbDocDown.append("');");
            sbDocDown.append("  if (dom){");
            sbDocDown.append("    dom.querySelector('td:first-child').className = \"createLabelRequired\";");
            sbDocDown.append("  }");
            sbDocDown.append("</script>");*/
        }
        sbDocDown.append("<script language=\"JavaScript\">");
        sbDocDown.append("function  checkJFQuestionsList1(){\n" +
                "    var strChangeSource = ''\n" +
                "    const domJFQQFileId=  document.getElementById(\"JFECRQQFileId2\");\n" +
                "    const strJFProjectPhase = document.getElementById(\"JFProjectPhaseId\").value;\n" +
                "    const strJFIsTKOData = document.getElementById(\"JFIsTKODataId\").value;\n"+
//                "    if (strJFProjectPhase == \"phase2\" || strJFProjectPhase == \"phase3\" ){\n" +
                //20260519 add ljr 新增phase 2+3 阶段判断
                "    if (strJFProjectPhase == \"phase2\" || strJFProjectPhase == \"phase3\" || strJFProjectPhase == \"phase2+3\"){\n" +
                "    if (strJFIsTKOData === \"Yes\"){\n" +
                "        if (!(domJFQQFileId.value)){\n" +
                "            sendMess1(\"\u5fc5\u987b\u4e0a\u4f20\u95ee\u9898\u6e05\u5355\u9644\u4ef6\",\"The attachment of the problem list must be uploaded\");\n" +
                "        return false ;\n" +
                "        }\n" +
                "    }\n" +
                "    }\n" +
                "        return true ;\n" +
                "}");
        sbDocDown.append("function sendMess1(strCNMess,strENMess){\n" +
                "    var language = navigator.language || navigator.userLanguage;\n" +
                "    var strMess = \"\";\n" +
                "    if (language.includes(\"zh\") || language.includes(\"zh-CN\")) {\n" +
                "        strMess = strCNMess;\n" +
                "    }else {\n" +
                "        strMess = strENMess;\n" +
                "    }\n" +
                "    alert(strMess);\n" +
                "}");
        sbDocDown.append(" // 使用 addEventListener 添加第三个 onload 函数\n" +
                "    window.addEventListener('load', function JFQQOnloadHandler() {\n" +
                "        console.log(\"Third onload.\");\n" +
                "        console.log(\"gggg.\");\n" +
                "        document.getElementById('JFQuestionsList').customValidate = checkJFQuestionsList1\n" +
                "    }, false);");

        sbDocDown.append(" </script>");
        _logger.info("sbDocDown:{}", sbDocDown);
        //加载影响工厂样式
//        if (!"view".equals(strMode)) {
//            if (!"Yes".equals(strIsPlatForm)) {
//                sbDocDown.append("<script language=\"JavaScript\">");
//                if ("create".equals(strMode)) {
//                    sbDocDown.append("    window.addEventListener('load', function JFJFAffectsFactoryOnloadHandler() {\n" +
//                            " FormHandler.GetField(\"JFAffectsFactory\").HandlerField[0].customValidate = checkInputIsNull;" +
//                            "emxFormSetFieldEditable(\"JFAffectsFactory\",false);" +
//                            "    }, false);");
//                }
//                sbDocDown.append(" </script>");
//
//
//            }
//        }
        _logger.info("--------------------- buildQuestListHtml end --------------------------");
        return sbDocDown.toString();
    }

    public String getJFProjectLevel(Context context, String args[]) throws Exception {
        String JFProjectLevelCN="";

        try {
            Map prammap=JPO.unpackArgs(args);
            _logger.info("getJFProjectLevel---->");
            _logger.info("getJFProjectLevel---->");
            _logger.info("getJFProjectLevel---->");
            _logger.info("getJFProjectLevel---->"+prammap);
            Map requestMap= (Map) prammap.get("requestMap");
            String strObjectId = (String) requestMap.get("objectId");
            if(UIUtil.isNotNullAndNotEmpty(strObjectId)){
                DomainObject ecr = DomainObject.newInstance(context, strObjectId);
                String strProjectSpaceId = ecr.getInfo(context, "from[JFChange2Project].to.id");
                if(UIUtil.isNotNullAndNotEmpty(strProjectSpaceId)){
                    DomainObject projectSpaceObj=DomainObject.newInstance(context,strProjectSpaceId);
                    String JFProjectLevel =projectSpaceObj.getAttributeValue(context,"JFProjectLevel");
                    if(UIUtil.isNotNullAndNotEmpty(JFProjectLevel)){
                        JFProjectLevelCN = EnoviaResourceBundle.getRangeI18NString(context, "JFProjectLevel", JFProjectLevel, context.getSession().getLanguage());
                    }
                }
            }
        } catch (Exception exp) {
            exp.printStackTrace();
        }
        return JFProjectLevelCN;
    }

    /**
    *
     * 第一版本：
     * 原件发起ECR中，系统检查原件是否存在对应版本的变形件，如果变形件未添加到ECR中，则不允许发起ECR，
     * 并提示“变更零件XXXXXX存在变形件，变形件需与原件同时发布，请将变形件同时添加到ECR中进行发布！”
     *
     * DR：“变更零件XXXXXX存在变形件，变形件需与原件同时冻结，请将变形件同时添加到DR中进行冻结！“
    * @param context
	* @param args
    * @author LIUJR
    * @throws
    * @return int
    * @date 2025/12/11 15:33
    * @description
    */
    public int checkFlexPartAndSourcePartOneRev(Context context, String[] args) throws Exception {
        _logger.info("checkFlexPartAndSourcePart-----------start--------------------");
        int ischeck = 0;
        try {
            String objectId = args[0];
            StringBuilder stringBuffer = new StringBuilder();
            DomainObject object = DomainObject.newInstance(context,objectId);
            String strType = object.getInfo(context, SELECT_TYPE);
            StringList basicBolistSel = JF_Util_mxJPO.basicBolistSel();
            basicBolistSel.add(SELECT_Attr_JF_OriginalPart);
            basicBolistSel.add(SELECT_Attr_JF_FlexiblePart);
            basicBolistSel.add(SELECT_ATTR_V_PART_NUMBER);
            StringList basicRellistSel = JF_Util_mxJPO.basicRellistSel();
            HashSet originalPartSet = new HashSet<String>();
            HashSet flexiblePartSet = new HashSet<String>();
            HashSet originalPartNumber = new HashSet<String>();
            HashSet flexiblePartNumber = new HashSet<String>();
            HashMap<String, String> hashMap = new HashMap<>();
            String strOriginalPartMess = EMPTY_STRING;
            String strFlexiblePartMess = EMPTY_STRING;
            if (JF_PLMConstants_mxJPO.TYPE_JFDR.equalsIgnoreCase(strType)) {
                _logger.info("dr-----------start--------------------");
                //DR：“变更零件XXXXXX存在变形件，变形件需与原件同时冻结，请将变形件同时添加到DR中进行冻结！“
                //获取所有的DR数据
                MapList mapList = object.getRelatedObjects(context,
                        JF_PLMConstants_mxJPO.REL_JFDR2VPMREFERENCE + "," + JF_PLMConstants_mxJPO.REL_Instance,
                        JF_PLMConstants_mxJPO.TYPE_VPMReference,
                        basicBolistSel,
                        basicRellistSel,
                        false,
                        true,
                        (short) 0,
                        "",
                        "",
                        0);
                _logger.info("dr-----------mapList:{}", mapList);
                for (int i = 0; i < mapList.size(); i++) {
                    Map map = (Map) mapList.get(i);
                    String id = UIUtil.getValue(map, SELECT_ID);
                    String number = UIUtil.getValue(map, SELECT_ATTR_V_PART_NUMBER);
                    hashMap.put(id, number);
                    if ("Y".equalsIgnoreCase(UIUtil.getValue(map, SELECT_Attr_JF_OriginalPart))) {
                        originalPartSet.add(id);
                        originalPartNumber.add(number);
                    }
                    if ("Y".equalsIgnoreCase(UIUtil.getValue(map, SELECT_Attr_JF_FlexiblePart))) {
                        flexiblePartSet.add(id);
                        flexiblePartNumber.add(number);
                    }
                }
                _logger.info("dr-----------originalPartSet:{}", originalPartSet);
                _logger.info("dr-----------originalPartNumber:{}", originalPartNumber);
                _logger.info("dr-----------flexiblePartSet:{}", flexiblePartSet);
                _logger.info("dr-----------flexiblePartNumber:{}", flexiblePartNumber);
                strOriginalPartMess = ComponentsUIUtil.getI18NString(context, context.getLocale().toString(), "emxComponents.DRPartMess.OriginalPart", new String[]{});
                strFlexiblePartMess = ComponentsUIUtil.getI18NString(context, context.getLocale().toString(), "emxComponents.DRPartMess.FlexiblePart", new String[]{});
            } else if (JF_PLMConstants_mxJPO.TYPE_JFNewECR.equalsIgnoreCase(strType)) {
                //原件发起ECR中，系统检查原件是否存在对应版本的变形件，
                //如果变形件未添加到ECR中，则不允许发起ECR，并提示“变更零件XXXXXX存在变形件，变形件需与原件同时发布，请将变形件同时添加到ECR中进行发布！”
                //获取所有冻结的ECR数据
                MapList mapList = object.getRelatedObjects(context,
                        JF_PLMConstants_mxJPO.REL_JFRELATEITEM,
                        JF_PLMConstants_mxJPO.TYPE_VPMReference,
                        basicBolistSel,
                        basicRellistSel,
                        false,
                        true,
                        (short) 1,
                        "current==FROZEN",
                        "",
                        0);
                for (int i = 0; i < mapList.size(); i++) {
                    Map map = (Map) mapList.get(i);
                    String id = UIUtil.getValue(map, SELECT_ID);
                    String number = UIUtil.getValue(map, SELECT_ATTR_V_PART_NUMBER);
                    hashMap.put(id, number);
                    if ("Y".equalsIgnoreCase(UIUtil.getValue(map, SELECT_Attr_JF_OriginalPart))) {
                        originalPartSet.add(id);
                        originalPartNumber.add(number);
                    }
                    if ("Y".equalsIgnoreCase(UIUtil.getValue(map, SELECT_Attr_JF_FlexiblePart))) {
                        flexiblePartSet.add(id);
                        flexiblePartNumber.add(number);
                    }
                }
                strOriginalPartMess = ComponentsUIUtil.getI18NString(context, context.getLocale().toString(), "emxComponents.ECRPartMess.OriginalPart", new String[]{});
                strFlexiblePartMess = ComponentsUIUtil.getI18NString(context, context.getLocale().toString(), "emxComponents.ECRPartMess.FlexiblePart", new String[]{});
            }
            StringList originalPartList = StringList.create(originalPartSet);
            StringList flexiblePartList = StringList.create(flexiblePartSet);
            DomainObject partObject = DomainObject.newInstance(context);
            StringBuilder sb = new StringBuilder();
            if (!originalPartList.isEmpty() || !flexiblePartList.isEmpty()) {
                //原件
                if (flexiblePartList.isEmpty()) {
                    //原件有，变形件没有，将原件及其变形件零件号作为提示
                    //以下原件的变形件未加入清单，请加入后提交！
                    sb.append(String.format(strOriginalPartMess, StringList.create(originalPartNumber).join(",")));
                } else if (originalPartList.isEmpty()) {
                    //以下变形件的原件未加入清单，请加入后提交！
                    //原件没有，变形件有，将变形件及其原件零件号作为提示
                    sb.append(String.format(strFlexiblePartMess, StringList.create(flexiblePartNumber).join(",")));
                } else {
                    //都有值
                    //先遍历原件，判断原件的变形件是否在这个结构中
                    HashSet originals = new HashSet<String>();
                    for (int i = 0; i < originalPartList.size(); i++) {
                        String partId = originalPartList.get(i);
                        partObject.setId(partId);
                        //获取原件关联的变形件
                        StringList flexList = partObject.getInfoList(context, "from[" + REL_JFOriginalPart2Flexible + "].to.id");
                        //判断变形件是否在结构中
                        _logger.info("原件的变形件是否在这个结构中flexList:{}", flexList);
                        StringList collect = flexList.stream()
                                .filter(item -> item != null && !flexiblePartSet.contains(item))
                                .collect(Collectors.toCollection(StringList::new));
                        _logger.info("原件的变形件是否在这个结构中collect:{}", collect);
                        if (!collect.isEmpty()) {
                            originals.add(hashMap.get(partId));
                        }
                    }
                    if (!originals.isEmpty()) {
                        sb.append(String.format(strOriginalPartMess, StringList.create(originals).join(",")));
                        sb.append("\n");
                    }
                    HashSet flexbles = new HashSet<String>();
                    for (int i = 0; i < flexiblePartList.size(); i++) {
                        String partId = flexiblePartList.get(i);
                        partObject.setId(partId);
                        //获取原件关联的变形件
                        StringList originalsList = partObject.getInfoList(context, "to[" + REL_JFOriginalPart2Flexible + "].from.id");
                        //判断变形件是否在结构中
                        _logger.info("判断变形件是否在结构中originalsList:{}", originalsList);
                        StringList collect = originalsList.stream()
                                .filter(item -> item != null && !originalPartSet.contains(item))
                                .collect(Collectors.toCollection(StringList::new));
                        _logger.info("判断变形件是否在结构中collect:{}", collect);
                        if (!collect.isEmpty()) {
                            flexbles.add(hashMap.get(partId));
                        }
                    }
                    if (!flexbles.isEmpty()) {
                        sb.append(String.format(strFlexiblePartMess, StringList.create(flexbles).join(",")));
                    }
                }
            }
            _logger.info("dr-----------sb:{}", sb);
            if (sb.length() > 0) {
                ischeck = 1;
                emxContextUtilBase_mxJPO.mqlWarning(context, sb.toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
        return ischeck;
    }

    /**
     *第二版：增加ECR校验（DR不再校验）：
     * 1. 当提升ECR到审批状态的时候，获取ECR变更清单中的冻结状态的原件清单，对冻结原件清单进行去重
     * 2. 遍历原件，获取原件关联的非发布状态的变形件
     *     1. 如果存在非发布状态的变形件，判断均是否在ECR变更清单中
     *       1. 若均在当前ECR中，则检查下一个原件；
     *       2. 若有不存在的非发布变形件，记录原件零件号+版本，记为“问题原件”，检查下一个原件；
     *     2. 如果不存在关联的变形件，检查下一个原件；
     * 3. 根据记录的问题原件判断是否通过
     *     1. 若存在“问题原件”，校验不通过，将“问题原件”合并为清单，提示报错“{问题原件}存在未发布的变形件，请在ECR中一起发布或提前发布变形件”
     *     2. 若不存在“问题原件”，则校验通过；
     *
     * 20251226  增加校验
     * 4. 增加ECR校验：检查变形件必须关联原件
     *   1. 当提升ECR到审批状态的时候，获取ECR变更清单中的冻结状态的变形件清单，对变形件清单进行去重；
     *   2. 遍历变形件，检查变形件是否关联原件：
     *     1. 有关联原件，校验通过，检查下一个变形件；
     *     2. 没有关联原件，记录变形件Name+版本，记入为“问题变形件”清单，检查下一个变形件
     *   3. 根据记录的问题变形件判断是否通过
     *     1. 若存在“问题变形件”，校验不通过，将“问题变形件”合并为清单，提示报错“变形件{问题变形件}没有关联的原件，请检查原件是否未升版”
     *     2. 若不存在“问题变形件”，则校验通过；
     * @param context
     * @param args
     * @author LIUJR
     * @throws
     * @return int
     * @date 2025/12/11 15:33
     * @description
     */
    public int checkFlexPartAndSourcePartSecondRev(Context context, String[] args) throws Exception {
        _logger.info("checkFlexPartAndSourcePart-----------start--------------------");
        int ischeck = 0;
        try {
            String objectId = args[0];
            DomainObject object = DomainObject.newInstance(context,objectId);
            StringList basicBolistSel = JF_Util_mxJPO.basicBolistSel();
            basicBolistSel.add(SELECT_Attr_JF_OriginalPart);
            basicBolistSel.add(SELECT_Attr_JF_FlexiblePart);
            basicBolistSel.add(SELECT_ATTR_V_PART_NUMBER);
            StringList basicRellistSel = JF_Util_mxJPO.basicRellistSel();
            String strOriginalPartMess = ComponentsUIUtil.getI18NString(context, context.getLocale().toString(), "emxComponents.ECRPartMess.OriginalPart", new String[]{});
            String strFlexiblePartMess = ComponentsUIUtil.getI18NString(context, context.getLocale().toString(), "emxComponents.ECRPart.FlexiblePartMess", new String[]{});
            //获取ECR中的所有更改项
            MapList mapList = object.getRelatedObjects(context,
                    JF_PLMConstants_mxJPO.REL_JFRELATEITEM,
                    JF_PLMConstants_mxJPO.TYPE_VPMReference,
                    basicBolistSel,
                    basicRellistSel,
                    false,
                    true,
                    (short) 1,
                    "",
                    "",
                    0);
            HashSet originalPartSet = new HashSet<String>();
            HashSet flexiblePartSet = new HashSet<String>();
            HashSet flexiblePartFROZENSet = new HashSet<String>();
            HashMap<String, String> partNumberRevMap = new HashMap<>();
            for (int i = 0; i < mapList.size(); i++) {
                Map map = (Map) mapList.get(i);
                String id = UIUtil.getValue(map, SELECT_ID);
                String current = UIUtil.getValue(map, SELECT_CURRENT);
                String number = UIUtil.getValue(map, SELECT_ATTR_V_PART_NUMBER);
                String rev = UIUtil.getValue(map, SELECT_REVISION);
                if ("Y".equalsIgnoreCase(UIUtil.getValue(map, SELECT_Attr_JF_OriginalPart)) && "FROZEN".equalsIgnoreCase(current)) {
                    originalPartSet.add(id);
                    partNumberRevMap.put(id, number + "_" + rev);
                }
                if ("Y".equalsIgnoreCase(UIUtil.getValue(map, SELECT_Attr_JF_FlexiblePart)) && "IN_WORK,FROZEN".contains(current)) {
                    flexiblePartSet.add(id);
                    partNumberRevMap.put(id, number + "_" + rev);
                }
                if ("Y".equalsIgnoreCase(UIUtil.getValue(map, SELECT_Attr_JF_FlexiblePart)) && "FROZEN".contains(current)) {
                    flexiblePartFROZENSet.add(id);
                }
            }
            StringList originalPartList = StringList.create(originalPartSet);
            StringList flexiblePartList = StringList.create(flexiblePartSet);
            DomainObject partObject = DomainObject.newInstance(context);
            StringBuilder sb = new StringBuilder();
            /*
            * 原件不为空
            * 2. 遍历原件，获取原件关联的非发布状态的变形件
            *     1. 如果存在非发布状态的变形件，判断均是否在ECR变更清单中
            *       1. 若均在当前ECR中，则检查下一个原件；
            *       2. 若有不存在的非发布变形件，记录原件零件号+版本，记为“问题原件”，检查下一个原件；
            *     2. 如果不存在关联的变形件，检查下一个原件；
            * */
            StringList originalErrorList = new StringList();
            if (!originalPartList.isEmpty()) {
                for (int i = 0; i < originalPartList.size(); i++) {
                    String partId = originalPartList.get(i);
                    partObject.setId(partId);
                    //获取原件关联的非发布状态的变形件
                    StringList flexList = (StringList) partObject.getRelatedObjects(context,
                            JF_PLMConstants_mxJPO.REL_JFOriginalPart2Flexible,
                            JF_PLMConstants_mxJPO.TYPE_VPMReference,
                            basicBolistSel,
                            basicRellistSel,
                            false,
                            true,
                            (short) 1,
                            "current=='IN_WORK' || current=='FROZEN'",
                            "",
                            0
                    ).stream().map(m -> {
                                Map map = (Map) m;
                                return UIUtil.getValue(map, SELECT_ID);
                    }).collect(Collectors.toCollection(SelectList::new));
                    //判断变形件是否在结构中
                    _logger.info("原件的变形件是否在这个结构中flexList:{}", flexList);
                    StringList collect = flexList.stream()
                            .filter(item -> item != null && !flexiblePartList.contains(item))
                            .collect(Collectors.toCollection(StringList::new));
                    _logger.info("原件的变形件是否在这个结构中collect:{}", collect);
                    if (!collect.isEmpty()) {
                        originalErrorList.add(partNumberRevMap.get(partId));
                    }
                }
            }
            //新增变形件校验逻辑 校验变形件必须关联一个原件才允许被发起ECR 不用校验关联原件的状态
            StringList flexiblePartFROZENList = StringList.create(flexiblePartFROZENSet);
            StringList flexibleErrorList = new StringList();
            if (!flexiblePartFROZENList.isEmpty()) {
                for (int i = 0; i < flexiblePartFROZENList.size(); i++) {
                    String partId = flexiblePartFROZENList.get(i);
                    partObject.setId(partId);
                    StringList infoList = partObject.getInfoList(context, "to[JFOriginalPart2Flexible].from.id");
                    if (infoList.isEmpty()) {
                        flexibleErrorList.add(partNumberRevMap.get(partId));
                    }
                }
            }
            if (!originalErrorList.isEmpty()) {
                sb.append(String.format(strOriginalPartMess, originalErrorList.join(",")));
            }
            if (!flexibleErrorList.isEmpty()) {
                sb.append(String.format(strFlexiblePartMess, flexibleErrorList.join(",")));
            }
            if (!sb.isEmpty()) {
                ischeck = 1;
                emxContextUtilBase_mxJPO.mqlWarning(context, sb.toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
        return ischeck;
    }


    /**
    * 第三版本 最新版本   变形件原件加入DR ECR校验
     * DR校验
     *   1. 检查DR中是否有工作中的原件
     *     1. 若不存在，则进入下一条
     *     2. 若存在，则校验
     *       1. （1）原件关联的变形件是否在同一个DR中，关联的变形件（工作中，草稿）需要在当前的DR中一起冻结，如果不满足，则弹出报错“原件{问题原件}存在未发布的变形件，请在DR中一起冻结”；
     *       2. （2）原件的上一版本是否有未升版的变形件，检查上一版本原件是否有未升版的变形件，若存在未升版变形件，则弹出报错“变形件{零件号+版本（名称）}未随原件升版，请升版并一起冻结”；
     *   2. 检查DR中是否有工作中的变形件
     *     1. 若不存在，则校验结束
     *     2. 若存在，则需要校验变形件关联的原件的状态
     *       1. 若变形件（游离）不存在关联的原件，则报错“变形件{零件号+版本（名称）}关联的原件未升版，请一同升版原件并冻结”；
     *       2. 若变形件关联的原件是工作中、私有（特殊情况）的，检查原件和变形件是否在同一个DR中
     *         1. 若不在，则报错“变形件{零件号+版本（名称）}关联的原件未冻结，请在DR中一起冻结”；
     *         2. 若在，则校验通过；
     *       3. 若变形件关联的原件是冻结、已发布的，允许DR提升。
     *   3. 检查DR中工作中，草稿的变形件和关联的原件版本是否一致，如果不一致，则报错“原件{name+版本}和变形件{name+版本}的版本不一致，不允许发起DR”
     *
     * ECR校验
     *  增加ECR校验：校验原件关联的变形件是否加入ECR变更清单
     *   1. 当提升ECR到审批状态的时候，获取ECR变更清单中的冻结状态的原件清单，对冻结原件清单进行去重，遍历原件，获取原件关联的非发布状态的变形件
     *     1. 如果存在非发布状态的变形件，判断均是否在ECR变更清单中
     *       1. 若均在当前ECR中，则检查下一个原件；
     *       2. 若有不在当前ECR中的非发布变形件，记录原件零件号+版本，记为“问题原件”，检查下一个原件；
     *     2. 如果不存在关联的变形件，检查下一个原件；（有三种情况，变形件还没升版或者原件删除小版本导致的变形件关系丢失，原件创建变形件后删除变形件）；
     *   2. 新增： 当提升ECR到审批状态的时候，获取ECR变更清单中的冻结状态的变形件清单，对变形件清单进行去重；遍历变形件，检查变形件关联原件的状态：
     *     1. 没有关联原件，记录变形件Name+版本，记入为“问题变形件1”清单，检查下一个变形件（特殊情况）
     *     2. 关联的原件状态为私有(特殊情况)、工作中（特殊情况）、冻结，检查原件和变形件是否在同一个ECR中：
     *       1. 不在同一个ECR中，记录变形件Name+版本，记录到“问题变形件2”清单，检查下一个变形件；
     *       2. 在同一个ECR中，通过校验，检查下一个变形件；
     *     3. 关联的原件状态为已发布，通过校验，检查下一个变形件；
     *   3. 根据记录的问题原件判断是否通过
     *     1. 若存在“问题原件”，校验不通过，将“问题原件”合并为清单，提示报错“原件{问题原件}存在未发布的变形件，请在ECR中一起发布或提前发布变形件”
     *     2. 若不存在“问题原件”，则校验通过；
    * @param context
	* @param args
    * @author LIUJR
    * @throws
    * @return int
    * @date 2026/1/12 15:11
    * @description
    */
    public int checkFlexPartAndSourcePart(Context context, String[] args) throws Exception {
        _logger.info("checkFlexPartAndSourcePart-----------start--------------------");
        int ischeck = 0;
        try {
            String objectId = args[0];
            DomainObject object = DomainObject.newInstance(context,objectId);
            String strType = object.getInfo(context, SELECT_TYPE);
            StringList basicBolistSel = JF_Util_mxJPO.basicBolistSel();
            basicBolistSel.add(SELECT_Attr_JF_OriginalPart);
            basicBolistSel.add(SELECT_Attr_JF_FlexiblePart);
            basicBolistSel.add(SELECT_ATTR_V_PART_NUMBER);
            StringList basicRellistSel = JF_Util_mxJPO.basicRellistSel();
            HashSet originalPartSet = new HashSet<String>();
            HashSet flexiblePartSet = new HashSet<String>();
            HashMap<String, String> hashMap = new HashMap<>();
            HashMap<String, StringList> originalFlexiblePartMap = new HashMap<>();
            MapList mapList = new MapList();
            String id = EMPTY_STRING;
            String rev = EMPTY_STRING;
            String name = EMPTY_STRING;
            String current = EMPTY_STRING;
            String number = EMPTY_STRING;
            Map map = new HashMap();
            StringList list = new StringList();
            StringList list1 = new StringList();
            DomainObject partObject = DomainObject.newInstance(context);
            StringBuilder sb = new StringBuilder();
            if (JF_PLMConstants_mxJPO.TYPE_JFDR.equalsIgnoreCase(strType)) {
                HashSet originalPartNumberSet1 = new HashSet<String>();
                HashSet originalPartNumberSet2 = new HashSet<String>();
                HashSet flexiblePartNumberSet1 = new HashSet<String>();
                HashSet flexiblePartNumberSet2 = new HashSet<String>();
                //获取所有的DR数据
                mapList = object.getRelatedObjects(context,
                        JF_PLMConstants_mxJPO.REL_JFDR2VPMREFERENCE + "," + JF_PLMConstants_mxJPO.REL_Instance,
                        JF_PLMConstants_mxJPO.TYPE_VPMReference,
                        basicBolistSel,
                        basicRellistSel,
                        false,
                        true,
                        (short) 0,
                        "",
                        "",
                        0);
                //遍历DR的所有数据
                for (int i = 0; i < mapList.size(); i++) {
                    map = (Map) mapList.get(i);
                    id = UIUtil.getValue(map, SELECT_ID);
                    current = UIUtil.getValue(map, SELECT_CURRENT);
                    name = UIUtil.getValue(map, SELECT_NAME);
                    rev = UIUtil.getValue(map, SELECT_REVISION);
                    number = UIUtil.getValue(map, SELECT_ATTR_V_PART_NUMBER);
                    hashMap.put(id, number + "_" + rev + " (" + name + ")");
                    partObject.setId(id);
                    //是工作中的原件
                    if ("IN_WORK".equalsIgnoreCase(current) && "Y".equalsIgnoreCase(UIUtil.getValue(map, SELECT_Attr_JF_OriginalPart))) {
                        originalPartSet.add(id);
                        //找到原件关联的变形件（草稿，工作中）
                        list = (StringList) partObject.getRelatedObjects(context,
                                JF_PLMConstants_mxJPO.REL_JFOriginalPart2Flexible,
                                JF_PLMConstants_mxJPO.TYPE_VPMReference,
                                basicBolistSel,
                                basicRellistSel,
                                false,
                                true,
                                (short) 1,
                                "(current==PRIVATE||current==IN_WORK)",
                                "",
                                0
                        ).stream().map(m -> {
                            return UIUtil.getValue((Map)m, SELECT_ID);
                        }).collect(Collectors.toCollection(StringList::new));
                        //如果有需要存起来
                        if (!list.isEmpty()) {
                            originalFlexiblePartMap.put(id, list);
                        }
                    }
                    //如果是工作中或者草稿的变形件
                    if (("IN_WORK".equalsIgnoreCase(current) || "PRIVATE".equalsIgnoreCase(current)) && "Y".equalsIgnoreCase(UIUtil.getValue(map, SELECT_Attr_JF_FlexiblePart))) {
                        flexiblePartSet.add(id);
                    }
                }
                //拿到变形件和原件的集合
                StringList originalPartList = StringList.create(originalPartSet);
                StringList flexiblePartList = StringList.create(flexiblePartSet);
                //遍历原件
                for (int i = 0; i < originalPartList.size(); i++) {
                    id = originalPartList.get(i);
                    partObject.setId(id);
                    //获取原件关联的变形件
                    if (originalFlexiblePartMap.containsKey(id)) {
                        list = originalFlexiblePartMap.get(id);
                        //判断这些变形件是否都存在DR的变形中
                        list1 = list.stream()
                                .filter(item -> !flexiblePartList.contains(item))
                                .collect(Collectors.toCollection(StringList::new));
                        if (!list1.isEmpty()) {
                            originalPartNumberSet1.add(hashMap.get(id));
                            continue;
                        }
                    }
                    //判断原件的上一个版本是否存在未升版的变形件
                    BusinessObjectList majorRevisionsBusObjList = partObject.getMajorRevisions(context);
                    if (majorRevisionsBusObjList.isEmpty()) {
                        continue;
                    }
                    //检查原件的当前版本及之前的所有版本是否有未升版的变形件 （废弃状态的变形件不考虑）
                    for (int i1 = 0; i1 <= majorRevisionsBusObjList.size() - 1;  i1++) {
                        partObject = (DomainObject) majorRevisionsBusObjList.get(i1);
                        if (id.equalsIgnoreCase(partObject.getId(context))) {
                            break;
                        }
                        mapList = partObject.getRelatedObjects(context,
                                JF_PLMConstants_mxJPO.REL_JFOriginalPart2Flexible,
                                JF_PLMConstants_mxJPO.TYPE_VPMReference,
                                basicBolistSel,
                                basicRellistSel,
                                false,
                                true,
                                (short) 1,
                                SELECT_ATTR_V_IsLastVersion + "=='TRUE'&&current!='OBSOLETE'",
                                "",
                                0
                        );
                        if (!mapList.isEmpty()) {
                            for (int i2 = 0; i2 < mapList.size(); i2++) {
                                map = (Map) mapList.get(i2);
                                originalPartNumberSet2.add(UIUtil.getValue(map, SELECT_ATTR_V_PART_NUMBER) + "_" + UIUtil.getValue(map, SELECT_REVISION) + " (" + UIUtil.getValue(map, SELECT_NAME) + ")");
                            }
                        }
                    }
                }
                String strDRFlexiblePartMess = ComponentsUIUtil.getI18NString(context, context.getLocale().toString(), "emxComponents.DRPartMess.FlexiblePart", new String[]{});
                for (int i = 0; i < flexiblePartList.size(); i++) {
                    id = flexiblePartList.get(i);
                    partObject.setId(id);
                    rev = partObject.getInfo(context, SELECT_REVISION);
                    name = partObject.getInfo(context, SELECT_NAME);
//                    name = UIUtil.getValue(map, SELECT_NAME);
                    number = partObject.getInfo(context, SELECT_ATTR_V_PART_NUMBER);
                    id = partObject.getInfo(context, "to[" + REL_JFOriginalPart2Flexible + "].from.id");
                    if (UIUtil.isNullOrEmpty(id)) {
                        flexiblePartNumberSet1.add(number + "_" + rev + " (" + name + ")");
                        continue;
                    }
                    partObject.setId(id);
                    current = partObject.getInfo(context, SELECT_CURRENT);
                    if ((current.equalsIgnoreCase("PRIVATE")||current.equalsIgnoreCase("IN_WORK")) && !originalPartList.contains(id)) {
                        flexiblePartNumberSet2.add(number + "_" + rev + " (" + name + ")");
                        continue;
                    }
                    //判断变形件和原件的版本是否一致
                    if (!rev.equalsIgnoreCase(partObject.getInfo(context, SELECT_REVISION))) {
                        sb.append(String.format(strDRFlexiblePartMess, number + "_" +
                                rev, partObject.getInfo(context, SELECT_ATTR_V_PART_NUMBER) + "_" +
                                partObject.getInfo(context, SELECT_REVISION) + " (" + partObject.getInfo(context, SELECT_NAME) + ")"));
                    }
                }
                if (!originalPartNumberSet1.isEmpty()) {
                    String strDROriginalPartMess1 = ComponentsUIUtil.getI18NString(context, context.getLocale().toString(), "emxComponents.DRPartMess.OriginalPart1", new String[]{});
                    sb.append(String.format(strDROriginalPartMess1, StringList.create(originalPartNumberSet1).join(",")));
                }
                if (!originalPartNumberSet2.isEmpty()) {
                    String strDROriginalPartMess2 = ComponentsUIUtil.getI18NString(context, context.getLocale().toString(), "emxComponents.DRPartMess.OriginalPart2", new String[]{});
                    sb.append(String.format(strDROriginalPartMess2, StringList.create(originalPartNumberSet2).join(",")));
                }
                if (!flexiblePartNumberSet1.isEmpty()) {
                    String strDRFlexiblePartMess1 = ComponentsUIUtil.getI18NString(context, context.getLocale().toString(), "emxComponents.DRPartMess.FlexiblePart1", new String[]{});
                    sb.append(String.format(strDRFlexiblePartMess1, StringList.create(flexiblePartNumberSet1).join(",")));
                }
                if (!flexiblePartNumberSet2.isEmpty()) {
                    String strDRFlexiblePartMess2 = ComponentsUIUtil.getI18NString(context, context.getLocale().toString(), "emxComponents.DRPartMess.FlexiblePart2", new String[]{});
                    sb.append(String.format(strDRFlexiblePartMess2, StringList.create(flexiblePartNumberSet2).join(",")));
                }
            } else if (JF_PLMConstants_mxJPO.TYPE_JFNewECR.equalsIgnoreCase(strType) || "JFFormalECR".equalsIgnoreCase(strType)) {
                HashSet originalPartNumberECRSet1 = new HashSet<String>();
                HashSet originalPartNumberECRSet2 = new HashSet<String>();
                HashSet flexiblePartNumberECRSet1 = new HashSet<String>();
                HashSet flexiblePartNumberECRSet2 = new HashSet<String>();
                StringList originalList = new StringList();
                mapList = object.getRelatedObjects(context,
                        JF_PLMConstants_mxJPO.REL_JFRELATEITEM,
                        JF_PLMConstants_mxJPO.TYPE_VPMReference,
                        basicBolistSel,
                        basicRellistSel,
                        false,
                        true,
                        (short) 1,
                        "",
                        "",
                        0);
                //遍历ECR的所有数据
                for (int i = 0; i < mapList.size(); i++) {
                    map = (Map) mapList.get(i);
                    id = UIUtil.getValue(map, SELECT_ID);
                    current = UIUtil.getValue(map, SELECT_CURRENT);
                    rev = UIUtil.getValue(map, SELECT_REVISION);
                    number = UIUtil.getValue(map, SELECT_ATTR_V_PART_NUMBER);
                    hashMap.put(id, number + "_" + rev);
                    partObject.setId(id);
                    //是工作中的原件
                    if ("FROZEN".equalsIgnoreCase(current) && "Y".equalsIgnoreCase(UIUtil.getValue(map, SELECT_Attr_JF_OriginalPart))) {
                        originalPartSet.add(id);
                        //找到原件关联的变形件（未发布）
                        list = (StringList) partObject.getRelatedObjects(context,
                                JF_PLMConstants_mxJPO.REL_JFOriginalPart2Flexible,
                                JF_PLMConstants_mxJPO.TYPE_VPMReference,
                                basicBolistSel,
                                basicRellistSel,
                                false,
                                true,
                                (short) 1,
                                "!current==RELEASED",
                                "",
                                0
                        ).stream().map(m -> {
                            return UIUtil.getValue((Map)m, SELECT_ID);
                        }).collect(Collectors.toCollection(StringList::new));
                        //如果有需要存起来
                        if (!list.isEmpty()) {
                            originalFlexiblePartMap.put(id, list);
                        }
                    }
                    //如果是冻结变形件
                    if ("FROZEN".equalsIgnoreCase(current) && "Y".equalsIgnoreCase(UIUtil.getValue(map, SELECT_Attr_JF_FlexiblePart))) {
                        flexiblePartSet.add(id);
                    }
                    if ((current.equalsIgnoreCase("PRIVATE")||current.equalsIgnoreCase("IN_WORK") || "FROZEN".equalsIgnoreCase(current)) && "Y".equalsIgnoreCase(UIUtil.getValue(map, SELECT_Attr_JF_OriginalPart))) {
                        originalList.add(id);
                    }
                }
                //拿到变形件和原件的集合
                StringList originalPartList = StringList.create(originalPartSet);
                StringList flexiblePartList = StringList.create(flexiblePartSet);
                //遍历原件
                for (int i = 0; i < originalPartList.size(); i++) {
                    id = originalPartList.get(i);
                    partObject.setId(id);
                    //获取原件关联的变形件    存在非发布状态的变形件，判断均是否在ECR变更清单中
                    if (originalFlexiblePartMap.containsKey(id)) {
                        list = originalFlexiblePartMap.get(id);
                        //判断这些变形件是否都存在DR的变形中
                        list1 = list.stream()
                                .filter(item -> !flexiblePartList.contains(item))
                                .collect(Collectors.toCollection(StringList::new));
                        if (!list1.isEmpty()) {
                            originalPartNumberECRSet1.add(hashMap.get(id));
                            continue;
                        }
                    }
                    //判断原件的上一个版本是否存在未升版的变形件
                    BusinessObjectList majorRevisionsBusObjList = partObject.getMajorRevisions(context);
                    if (majorRevisionsBusObjList.isEmpty()) {
                        continue;
                    }
                    //检查原件的当前版本及之前的所有版本是否有未升版的变形件  （废弃状态的变形件不考虑）
                    for (int i1 = 0; i1 <= majorRevisionsBusObjList.size() - 1;  i1++) {
                        partObject = (DomainObject) majorRevisionsBusObjList.get(i1);
                        if (id.equalsIgnoreCase(partObject.getId(context))) {
                            break;
                        }
                        mapList = partObject.getRelatedObjects(context,
                                JF_PLMConstants_mxJPO.REL_JFOriginalPart2Flexible,
                                JF_PLMConstants_mxJPO.TYPE_VPMReference,
                                basicBolistSel,
                                basicRellistSel,
                                false,
                                true,
                                (short) 1,
                                SELECT_ATTR_V_IsLastVersion + "=='TRUE'&&current!='OBSOLETE'",
                                "",
                                0
                        );
                        if (!mapList.isEmpty()) {
                            for (int i2 = 0; i2 < mapList.size(); i2++) {
                                map = (Map) mapList.get(i2);
                                originalPartNumberECRSet2.add(UIUtil.getValue(map, SELECT_ATTR_V_PART_NUMBER) + "_"
                                        + UIUtil.getValue(map, SELECT_REVISION) + " (" + UIUtil.getValue(map, SELECT_NAME) + ")");
                            }
                        }
                    }
                }
                //遍历变形件
                for (int i = 0; i < flexiblePartList.size(); i++) {
                    id = flexiblePartList.get(i);
                    partObject.setId(id);
                    rev = partObject.getInfo(context, SELECT_REVISION);
                    name = partObject.getInfo(context, SELECT_NAME);
                    number = partObject.getInfo(context, SELECT_ATTR_V_PART_NUMBER);
                    id = partObject.getInfo(context, "to[" + REL_JFOriginalPart2Flexible + "].from.id");
                    //没有关联原件
                    if (UIUtil.isNullOrEmpty(id)) {
                        flexiblePartNumberECRSet1.add(number + "_" + rev + " (" + name + ")");
                        continue;
                    }
                    partObject.setId(id);
                    //有原件是否一起在ECR中
                    current = partObject.getInfo(context, SELECT_CURRENT);
                    if ((current.equalsIgnoreCase("FROZEN")||current.equalsIgnoreCase("PRIVATE")||current.equalsIgnoreCase("IN_WORK")) && !originalList.contains(id)) {
                        flexiblePartNumberECRSet2.add(number + "_" + rev + " (" + name + ")");
                        continue;
                    }
                }
                if (!originalPartNumberECRSet1.isEmpty()) {
                    String strECROriginalPartMess1 = ComponentsUIUtil.getI18NString(context, context.getLocale().toString(), "emxComponents.ECRPartMess.OriginalPart", new String[]{});
                    sb.append(String.format(strECROriginalPartMess1, StringList.create(originalPartNumberECRSet1).join(",")));
                }
                if (!originalPartNumberECRSet2.isEmpty()) {
                    String strECRFlexiblePartMess2 = ComponentsUIUtil.getI18NString(context, context.getLocale().toString(), "emxComponents.ECRPartMess.OriginalPart1", new String[]{});
                    sb.append(String.format(strECRFlexiblePartMess2, StringList.create(originalPartNumberECRSet2).join(",")));
                }
                if (!flexiblePartNumberECRSet1.isEmpty()) {
                    String strECRFlexiblePartMess1 = ComponentsUIUtil.getI18NString(context, context.getLocale().toString(), "emxComponents.ECRPartMess.FlexiblePart1", new String[]{});
                    sb.append(String.format(strECRFlexiblePartMess1, StringList.create(flexiblePartNumberECRSet1).join(",")));
                }
                if (!flexiblePartNumberECRSet2.isEmpty()) {
                    String strECRFlexiblePartMess2 = ComponentsUIUtil.getI18NString(context, context.getLocale().toString(), "emxComponents.ECRPartMess.FlexiblePart2", new String[]{});
                    sb.append(String.format(strECRFlexiblePartMess2, StringList.create(flexiblePartNumberECRSet2).join(",")));
                }
            }
            if (sb.length() > 0) {
                ischeck = 1;
                emxContextUtilBase_mxJPO.mqlWarning(context, sb.toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
        return ischeck;
    }

    /**
    * 【ECR 变更类型】增加" QBOM 发放" (仅项目阶段为Phase 1时可选）
    * @param context
	* @param args
    * @author LIUJR
    * @throws
    * @return java.util.Map
    * @date 2025/12/25 10:36
    * @description
    */
    public Map getJFECRChangeTypeReloadRange(Context context, String[] args) throws Exception {
        Map returnMap = new HashMap();
        try {
            Map map = (Map) JPO.unpackArgs(args);
            _logger.info("map:{}", map);
            Map requestMap = (Map) map.get("requestMap");
            Map fieldMap = (Map) map.get("fieldMap");
            HashMap fieldValues = (HashMap)map.get("fieldValues");   //里面是驱动他变更的fieldName And Value
            String objectId = (String) requestMap.get(JF_PLMConstants_mxJPO.STRING_OBJECTID);
            String name = (String) fieldMap.get(SELECT_NAME);
            StringList ranges = FrameworkUtil.getRanges(context, name);
            String strJFProjectPhase = (String) fieldValues.get("JFProjectPhase");
            _logger.info("ranges:{}", ranges);
            if (!"phase1".equalsIgnoreCase(strJFProjectPhase)) {
                for (int i = 0; i < ranges.size(); i++) {
                    String range = ranges.get(i);
                    if ("QBOM Distribution".equalsIgnoreCase(range)) {
                        ranges.remove(i);
                    }
                }
            }
            _logger.info("strJFProjectPhase:{}", strJFProjectPhase);
            _logger.info("ranges:{}", ranges);

            StringList nlsRanges = EnoviaResourceBundle.getAttrRangeI18NStringList(context, name, ranges, context.getLocale().toString());
            returnMap.put("RangeValues", ranges);
            returnMap.put("RangeDisplayValues", nlsRanges);
        }catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
        return returnMap;
    }

    /**
     * 【ECR 变更类型】编辑的时候如果不为phase 1的话没有QBOM发放
     * @param context
     * @param args
     * @author LIUJR
     * @throws
     * @return java.util.Map
     * @date 2025/12/25 10:36
     * @description
     */
    public Map getJFECRChangeTypeRange(Context context, String[] args) throws Exception {
        Map returnMap = new HashMap();
        try {
            Map map = (Map) JPO.unpackArgs(args);
            _logger.info("map:{}", map);
            _logger.info("getJFECRChangeTypeRange.。。。。。。。。。。。。");
            Map requestMap = (Map) map.get("requestMap");
            Map fieldMap = (Map) map.get("fieldMap");
            String objectId = (String) requestMap.get(JF_PLMConstants_mxJPO.STRING_OBJECTID);
            DomainObject domainObject = DomainObject.newInstance(context, objectId);
            String strJFProjectPhase = domainObject.getAttributeValue(context, "JFProjectPhase");
            String name = (String) fieldMap.get(SELECT_NAME);
            StringList ranges = FrameworkUtil.getRanges(context, name);
            _logger.info("ranges:{}", ranges);
            if (!"phase1".equalsIgnoreCase(strJFProjectPhase)) {
                for (int i = 0; i < ranges.size(); i++) {
                    String range = ranges.get(i);
                    if ("QBOM Distribution".equalsIgnoreCase(range)) {
                        ranges.remove(i);
                    }
                }
            }
            _logger.info("strJFProjectPhase:{}", strJFProjectPhase);
            _logger.info("ranges:{}", ranges);
            StringList nlsRanges = EnoviaResourceBundle.getAttrRangeI18NStringList(context, name, ranges, context.getLocale().toString());
            _logger.info("nlsRanges:{}", nlsRanges);
            returnMap.put("field_choices", ranges);
            returnMap.put("field_display_choices", nlsRanges);
        }catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
        return returnMap;
    }

    /**
    *   1. ECR审核任务的时候，每个审核任务审批的时候，都需要校验
     *     1. 校验当前Instance关系下是否有不是最新版本的数据---和发起流程的校验一致
     *       1. 数据结构中处在“已发布”的数据必须是最新已发布版本
     *       2. 如果存在不是最新发布:审核任务不让通过，弹出提示信息当前ECR下面的哪些零件不是最新发布版本，请拒绝该审核任务，让ECROwner更新结构重新发起审核
     *   2. 校验当前项目下的ECR，是否还有正在进行冒泡的数据
     *     1. 只要最后一个审核节点--项目经理审核的时候触发校验
     *     2. 根据当前ECR找到关联的项目，看项目是否正在冒泡的标识JF_BubblingFlag是否为N
     *       1. 如果为N的情况下，弹出提示，请等待上一个ECR冒泡完成，上一个ECR冒泡完成之后，会邮件通知下继续审核下一个ECR
     * @param context
	* @param args
    * @author LIUJR
    * @throws
    * @return int
    * @date 2026/1/27 10:16
    * @description
    */
    public int checkInboxTaskECRPartStateAndRevision(Context context, String[] args) throws Exception {
        //拿取ECR所属的project
        //是否是ECR 状态Review 的项目经理审核任务
        boolean isPush = false;
        String strEcrId = "";
        try {
            String strLoginUser = context.getUser();
            _logger.info("strLoginUser：{}", strLoginUser);

            String strObjectId = args[0];
            _logger.info("strObjectId:{}", strObjectId);
            DomainObject inBoxTask = DomainObject.newInstance(context, strObjectId);
            String title = inBoxTask.getAttributeValue(context, DomainConstants.ATTRIBUTE_TITLE);
            // add by chenyan 修复当审核任务驳回时也校验了必填属性 驳回时不需要校验
            String strApproveState = inBoxTask.getInfo(context, "attribute[Approval Status].value");
            _logger.info("strApproveState：{}", strApproveState);
            if ("Reject".equals(strApproveState) || "None".equalsIgnoreCase(strApproveState)) {
                return 0;
            }
            ContextUtil.pushContext(context);
            isPush = true;
            StringList typeSelectList = JF_Util_mxJPO.basicBolistSel();
            StringList reSelectList = JF_Util_mxJPO.basicRellistSel();
            reSelectList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_ROUTE_BASE_POLICY);
            reSelectList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_ROUTE_BASE_STATE);
            reSelectList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_ROUTE_BASE_PURPOSE);
            MapList maps = inBoxTask.getRelatedObjects(context, RELATIONSHIP_ROUTE_TASK + "," + RELATIONSHIP_OBJECT_ROUTE, // relationship pattern
                    TYPE_ROUTE + "," + TYPE_JFECR + "," + TYPE_JFNewECR + "," + JF_PLMConstants_mxJPO.TYPE_JFFormalECR,                                    // object pattern
                    typeSelectList,                            // object selects
                    reSelectList, // relationship selects
                    true,                                        // to direction
                    true,                                        // from direction
                    (short) 2,                                    // recursion level
                    "",                // object where clause
                    "",
                    (short) 0);
            _logger.info("maps:{}", maps);
            Map groupMap = (Map) maps.stream().collect(Collectors.groupingBy(m -> {
                Map info = (Map) m;
                return info.get("relationship");
            }));
            _logger.info("groupMap:{}", groupMap);
            if (groupMap.containsKey(RELATIONSHIP_OBJECT_ROUTE)) {
                List ecrList = (List) groupMap.get(RELATIONSHIP_OBJECT_ROUTE);
                //理应只绑定一个ECR
                for (int i = 0; i < ecrList.size(); i++) {
                    Map ecrInfo = (Map) ecrList.get(i);
                    String strRouteBindPolicy = (String) ecrInfo.get(JF_PLMConstants_mxJPO.SELECT_ATTR_ROUTE_BASE_POLICY);
                    String strRouteBindState = (String) ecrInfo.get( JF_PLMConstants_mxJPO.SELECT_ATTR_ROUTE_BASE_STATE);
                    _logger.info("strRouteBindPolicy:{}", strRouteBindPolicy);
                    _logger.info("strRouteBindState:{}", strRouteBindState);
                    if (("policy_JFECR".equals(strRouteBindPolicy) || "policy_JFNewECR".equals(strRouteBindPolicy)) && "state_Review".equals(strRouteBindState)) {
                        strEcrId = (String) ecrInfo.get(SELECT_ID);
                        break;
                    }
                }
            }
            if (UIUtil.isNullOrEmpty(strEcrId)) {
                return 0;
            }
            //开始校验
            String mess = JF_NewECRProcess_mxJPO.checkVPMReferenceReleaseIsLastRevision(context, strEcrId);
            if (UIUtil.isNotNullAndNotEmpty(mess)) {
                emxContextUtil_mxJPO.mqlNotice(context, mess);
                return 1;
            }
            //校验项目经理节点
            _logger.info("title:{}", title);
            String titleZh = EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", new Locale("zh_CN"), "emxComponents.ECR.Review.ProjectManager");
            String titleen = EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", new Locale("en"), "emxComponents.ECR.Review.ProjectManager");
            //需要增加兼容 liujr 20260624  兼容旧的和新的流程title设置
            String titleZh_old = EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", Locale.CHINA, "emxComponents.ECR.Review.ProjectManager_Old");
            String titleUs_old = EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", Locale.US, "emxComponents.ECR.Review.ProjectManager_Old");
            _logger.info("titleZh:{}", titleZh);
            _logger.info("titleen:{}", titleen);
            //需要增加兼容 liujr 20260624  兼容旧的和新的流程title设置
            if (title.equalsIgnoreCase(titleZh) || title.equalsIgnoreCase(titleen) || title.equalsIgnoreCase(titleZh_old) || title.equalsIgnoreCase(titleUs_old)) {
                //是项目经理审核节点
                mess = checkECRPartIsBubbleFlag(context, new String[]{strEcrId});
                if (UIUtil.isNotNullAndNotEmpty(mess)) {
                    emxContextUtil_mxJPO.mqlNotice(context, mess);
                    return 1;
                }
            }
        } finally {
            if (isPush) {
                ContextUtil.popContext(context);
            }
        }
        return 0;
    }

    /**
    *   2. 校验当前项目下的ECR，是否还有正在进行冒泡的数据
     *     1. 只要最后一个审核节点--项目经理审核的时候触发校验
     *     2. 根据当前ECR找到关联的项目，看项目是否正在冒泡的标识JF_BubblingFlag是否为N
     *       1. 如果为N的情况下，弹出提示，请等待上一个ECR冒泡完成，上一个ECR冒泡完成之后，会邮件通知下继续审核下一个ECR
    * @param context
	* @param args
    * @author LIUJR
    * @throws
    * @return int
    * @date 2026/1/27 14:23
    * @description
    */
    public static String checkECRPartIsBubbleFlag(Context context, String[] args) throws Exception{
        String mess = EMPTY_STRING;
        try {
            String strEcrId = args[0];
            DomainObject ecrObject = DomainObject.newInstance(context, strEcrId);
            //拿取ecr的项目
            String strProjectId = ecrObject.getInfo(context, "from[" + JF_PLMConstants_mxJPO.REL_JFChange2Project + "].to.id");
            DomainObject projectObject = DomainObject.newInstance(context, strProjectId);
            String strJFBubblingFlag = projectObject.getAttributeValue(context, Attr_JFBubblingFlag);
            String strJFBubblingECR = projectObject.getAttributeValue(context, Attr_JF_BubblingECR);
            String strJFJoinBubbling = projectObject.getAttributeValue(context, Attr_JF_JoinBubbling);
            _logger.info("项目是否冒泡中:{}", strJFBubblingFlag);
            //是否参与冒泡  不参与返回
            if ("N".equalsIgnoreCase(strJFJoinBubbling)||ecrObject.getInfo(context,SELECT_TYPE).equalsIgnoreCase("JFNewECR")) {
                return mess;
            }
            //项目参与冒泡，判断项目是否正在冒泡，如果是不让提交到会签
            if ("N".equalsIgnoreCase(strJFBubblingFlag)) {
                String strPS = projectObject.getInfo(context, SELECT_NAME) + "(" + projectObject.getDescription(context) + ")";
                String strPar = ComponentsUIUtil.getI18NString(context, context.getLocale().toString(), "emxComponent.ECRMess.CheckBubble", new String[]{});
                mess = String.format(strPar, strPS, strJFBubblingECR);
            }
        }catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
        return mess;
    }

    /**
     *
     *@description 卷积自制件清单整椅价格
     *@param context
     *@param args
     *@return void
     *@throws
     *@author CHENYAN
     *@date 2025/5/19 15:43
     */
    public void rollupNewECRRootPartPrice_NewECR(Context context ,String[] args ) throws Exception{
//        Job job = new Job("JF_VPMReferenceEBOM", "convolutionReletedItem", args);
//        job.setTitle("convolution ChairPart price in NewECR");
//        job.createAndSubmit(context);
        _logger.info("rollupNewECRRootPartPrice_NewECR:{}",args);
        String strObjectId = args[0];
        String initargs[] = {};
        Map parameters = new HashMap<>();
        _logger.info("-------------------------------- rollupNewECRRootPartPrice begin -----------------------------------------------");
        parameters.put("objectId", strObjectId);
        parameters.put("expandLevel", "All");
        MapList buyTableMapList = (MapList) JPO.invoke(context, "JF_NewECRService", initargs, "getNewECRBuyTableData", JPO.packArgs(parameters), MapList.class);
        String strSelectPriceId = JF_PublicMethodClass_mxJPO.buildStringInStrings("tomid[", REL_JFECR2MakePartPrice,"|from.id==",strObjectId, "].id");
        DomainObject ecr = DomainObject.newInstance(context, strObjectId);
        String strProjectId = ecr.getInfo(context, "from[JFChange2Project].to.id");
        try {
            ContextUtil.startTransaction(context,true);
            for (int i = 0; i < buyTableMapList.size();) {
                Map partMap = (Map) buyTableMapList.get(i);
                String strRelId = (String) partMap.get(SELECT_RELATIONSHIP_ID);
                String strPartId = (String) partMap.get(SELECT_ID);
                String strRelName = (String) partMap.get(RELATIONSHIP);
                String strPartType = (String) partMap.get(SELECT_ATTR_JFPartType);
                //根节点
                if (REL_JFECRRelateRoot.equals(strRelName)){
                    int nextLevel = findEndOfSubtree(buyTableMapList, i); // 找到以i为根的子树结束位置
                    String strJFFreeState = (String) partMap.get(SELECT_ATTR_JFFREEState);
                    boolean isZeroPart = checkPartIsZeroPart(context, strPartId, strProjectId);
                    //不是游离 且是供货件
                    if ("N".equals(strJFFreeState) && isZeroPart){
                        // 获取卷积价格
                        Map attrMap = getRootPartTreePrice_NewECR(buyTableMapList, i+1 ,nextLevel);
                        _logger.info("attrMap:{}",attrMap);
                        String strSelectResMql =  JF_PublicMethodClass_mxJPO.buildStringInStrings("print connection " ,strRelId, " select ",strSelectPriceId ," dump ");
                        _logger.info("strSelectPriceId:{}",strSelectPriceId);
                        String strPriceId = MqlUtil.mqlCommand(context, Boolean.FALSE,strSelectResMql, Boolean.TRUE);
                        StringBuilder sbAttrMql = new StringBuilder();
                        _logger.info("strPriceId:{}",strPriceId);
                        //设置标识整椅
//                        attrMap.put("JFWholeChair","Y");
                        if (UIUtil.isNullOrEmpty(strPriceId)){
                            for (Object oEntry : attrMap.entrySet()) {
                                sbAttrMql.append(" ");
                                Map.Entry entry = (Map.Entry)oEntry;
                                String strAttrName = (String) entry.getKey();
                                String strAttrValue = (String) entry.getValue();
                                sbAttrMql.append(strAttrName);
                                sbAttrMql.append(" ");
                                sbAttrMql.append(strAttrValue);
                            }
                            String strAddMql = JF_PublicMethodClass_mxJPO.buildStringInStrings("add connection " ,REL_JFECR2MakePartPrice , " from ",strObjectId ," torel " ,strRelId ," ",sbAttrMql.toString());
                            _logger.info("strMql:{}",strAddMql);
                            MqlUtil.mqlCommand(context, Boolean.FALSE,strAddMql, Boolean.TRUE);
                        }else {
                            DomainRelationship rel = DomainRelationship.newInstance(context, strPriceId);
                            rel.setAttributeValues(context,attrMap);
                        }
                    }
                    i = nextLevel;
                }else {
                    //防止死循环
                    i++;
                }
            }
            _logger.info("-------------------------------- rollupNewECRRootPartPrice end -----------------------------------------------");

            ContextUtil.commitTransaction(context);
        }catch (Exception e){
            _logger.error(e.getMessage());
            ContextUtil.abortTransaction(context);
            throw e ;
        }
    }

    public Map getRootPartTreePrice_NewECR(MapList dataMapList, int startIndex ,int endIndex ){
        Map attrMap = new HashMap<>();
        BigDecimal sumPrice = new BigDecimal(0.0);
        BigDecimal sumPriceExternal = new BigDecimal(0.0);
        for (; startIndex < endIndex; ) {
            Map partMap = (Map) dataMapList.get(startIndex);
            String strQuantity = (String) partMap.get(SELECT_ATTRIBUTE_JF_BOMQuantity);
            String strChangeQuantity = (String) partMap.get(SELECT_ATTRIBUTE_JF_BOMChangeQuantity);
            //默认设置为1
            //变更数量为空或者为0.0取实际数量，数量数量为空默认为1
            if (UIUtil.isNullOrEmpty(strChangeQuantity) || "0.0".equals(strChangeQuantity)){
                strChangeQuantity = strQuantity ;
                if (UIUtil.isNullOrEmpty(strQuantity)){
                    strChangeQuantity = "1";
                }
            }

            BigDecimal bQuantity = new BigDecimal(strChangeQuantity);
            String strJFChangeUnitPrice = (String) partMap.get("tomid[JFECR2PartPrice].attribute[JFChangeUnitPrice]");
            String strJFChangeUnitPriceExternal = (String) partMap.get("tomid[JFECR2PartPrice].attribute[JFChangeUnitPriceExternal]");
            _logger.info("strChangeQuantity:{}",strChangeQuantity);
            _logger.info("strJFChangeUnitPrice:{}",strJFChangeUnitPrice);
            _logger.info("strJFChangeUnitPriceExternal:{}",strJFChangeUnitPriceExternal);
            //一个节点下面只会维护一次
            if (UIUtil.isNotNullAndNotEmpty(strJFChangeUnitPrice) || UIUtil.isNotNullAndNotEmpty(strJFChangeUnitPriceExternal)){
                if (UIUtil.isNotNullAndNotEmpty(strJFChangeUnitPrice)){
                    BigDecimal bJFChangeUnitPrice = new BigDecimal(strJFChangeUnitPrice);
                    sumPrice = bQuantity.multiply(bJFChangeUnitPrice).add(sumPrice);
                }
                if (UIUtil.isNotNullAndNotEmpty(strJFChangeUnitPriceExternal)){
                    BigDecimal bJFChangeUnitPriceExternal = new BigDecimal(strJFChangeUnitPriceExternal);
                    sumPriceExternal = bQuantity.multiply(bJFChangeUnitPriceExternal).add(sumPriceExternal);
                }
                int nextLevel = findEndOfSubtree(dataMapList, startIndex); // 找到以i为根的子树结束位置
                startIndex = nextLevel;
            }else {
                startIndex++;
            }
        }
        attrMap.put(ATTR_JFChangeWholeSeatPrice,sumPrice.toString());
        attrMap.put(ATTR_JFChangeWholeSeatPriceExternal,sumPriceExternal.toString());
        return attrMap;
    }
    /*
     * @description:正式ECR上线之后，JFNewECR不可以在使用
     * @author: caipan
     * @date: 2026/3/24 15:21:54
     * @param: * @param[1] context
     * @param[2] args
     * @return:
     **/
    public  int checkJFNewECRType(Context context,String[] args) throws Exception{
        String objectId = args[0];
        DomainObject ecr= DomainObject.newInstance(context,objectId);
        String type = ecr.getInfo(context, SELECT_TYPE);
        String message = EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", context.getLocale(), "emxComponents.Mess.checkJFNewECR");
        if("JFNewECR".equalsIgnoreCase(type)){
            emxContextUtil_mxJPO.mqlNotice(context, message);
            return 1;
        }
        return 0;
    }

    /**
     * 1. JFProjectPhase初始化
     * 2. 项目空间选择后驱动变化
     * 【ECR创建页面/属性编辑页面
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
    public Map getJFProjectPhaseReloadRange(Context context, String[] args) throws Exception {
        Map returnMap = new HashMap();
        try {
            Map map = (Map) JPO.unpackArgs(args);
            _logger.info("map:{}", map);
            Map requestMap = (Map) map.get("requestMap");
            String objectId = (String) requestMap.get(JF_PLMConstants_mxJPO.STRING_OBJECTID);
            Map fieldMap = (Map) map.get("fieldMap");
            _logger.info("fieldMap:{}", fieldMap);
            _logger.info("objectId:{}", objectId);
            String name = (String) fieldMap.get(SELECT_NAME);
            _logger.info("name:{}", name);
            StringList ranges = FrameworkUtil.getRanges(context, name);
            ranges.remove("");
            _logger.info("ranges:{}", ranges);
            if (!map.containsKey("fieldValues")) {
                if (UIUtil.isNullOrEmpty(objectId)) {
                    //初始化 创建页面
                    _logger.info("创建页面:{}", ranges);
                    ranges.remove("phase2+3");
                    ranges.sort();
                    returnMap.put("field_choices", ranges);
                    returnMap.put("field_display_choices", ranges);
                } else {
                    _logger.info("详情界面:{}", ranges);
                    //修改界面
                    DomainObject object = DomainObject.newInstance(context, objectId);
                    String psId = object.getInfo(context, "from[JFChange2Project].to.id");
                    _logger.info("psId:{}", psId);
                    Boolean noDVFlag = JF_ESO_mxJPO.projectSpaceHasDVFlag(context, DomainObject.newInstance(context, psId));
                    _logger.info("noDVFlag:{}", noDVFlag);
                    if (noDVFlag) {
                        ranges.remove("phase2");
                        ranges.remove("phase3");
                    } else {
                        ranges.remove("phase2+3");
                    }
                    ranges.sort();
                    returnMap.put("field_choices", ranges);
                    returnMap.put("field_display_choices", ranges);
                }
                _logger.info("1111111111111requestMap:{}", returnMap);
                return returnMap;
            }
            _logger.info("onchange handler 事件:{}", ranges);
            HashMap fieldValues = (HashMap)map.get("fieldValues");   //里面是驱动他变更的fieldName And Value
            _logger.info("fieldValues:{}", fieldValues);
            String strJFProjectNameOID = (String) fieldValues.get("JFProjectNameOID");
//            String strJFProjectName = (String) fieldValues.get("JFProjectName");
            _logger.info("ranges:{}", ranges);
            Boolean noDVFlag = JF_ESO_mxJPO.projectSpaceHasDVFlag(context, DomainObject.newInstance(context, strJFProjectNameOID));
            if (noDVFlag) {
                ranges.remove("phase2");
                ranges.remove("phase3");
            } else {
                ranges.remove("phase2+3");
            }
            ranges.sort();
            _logger.info("ranges:{}", ranges);
            returnMap.put("RangeValues", ranges);
            returnMap.put("RangeDisplayValues", ranges);
        }catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
        return returnMap;
    }
}
