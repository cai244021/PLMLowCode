import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.matrixone.apps.common.InboxTask;
import com.matrixone.apps.common.util.ComponentsUIUtil;
import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.DomainRelationship;
import com.matrixone.apps.domain.util.*;
import com.matrixone.apps.framework.ui.ProgramCallable;
import com.matrixone.apps.framework.ui.UICache;
import com.matrixone.apps.framework.ui.UIUtil;
import ds.enovia.apps.msf.BusinessObjects.ExecuteJPO.args;
import javassist.compiler.ast.StringL;
import matrix.db.*;
import matrix.util.MatrixException;
import matrix.util.StringList;
import org.apache.poi.hslf.record.HSLFEscherClientDataRecord;
import org.jsoup.Jsoup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.mail.BodyPart;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMultipart;

import java.util.*;
import java.util.Set;
import java.util.stream.Collectors;

import static com.matrixone.apps.domain.DomainConstants.*;

public class JF_VPMReference_mxJPO {
    private final String SUITE_KEY = "emxComponentsStringResource";
    String ATTRIBUTE_PARTTYPE = "JF_VPMReference.JF_PartType";
    String TYPE_JFDATATRANSMISSION = "type_JFPartNumberG";
    String ATTRIBUTE_PARTNUMBER = "EnterpriseExtension.V_PartNumber";
    String ATTRIBUTE_JF_PARTTYPE = "JF_VPMReference.JF_PartType";
    String TYPE_JF_COMPETITIVEBOM = "JF_CompetitiveBOM";
    private static final Logger log = LoggerFactory.getLogger(JF_VPMReference_mxJPO.class);

    /**
     * 创建零部件后触发Trigger，通过属性零件类型生成企业编码并初始化缺图纸标识  注意:  还有一个地方有相同的代码 JF_VPMReferenceEBOM:setPartNumber
     *
     * @param context
     * @param args
     * @throws Exception
     * @author JJS
     * @date 2026/8/26
     */
    public void setPartNumber(Context context, String[] args) throws Exception {
        try {
            if (TYPE_JF_COMPETITIVEBOM.equals(args[1])) {
                return;
            }
            DomainObject part = DomainObject.newInstance(context, args[0]);
            String name = part.getInfo(context, DomainConstants.SELECT_NAME);
            String JF_ISXPDM = part.getAttributeValue(context, JF_DataOutSource_mxJPO.ATTRIBUTE_JF_ISXPDM);
            if ("N".equalsIgnoreCase(JF_ISXPDM)) {

                if (name.startsWith("G")) {
                    part.setAttributeValue(context, ATTRIBUTE_PARTNUMBER, name);
                } else {
                    String PartType = part.getAttributeValue(context, ATTRIBUTE_PARTTYPE);
//                if ("N".equalsIgnoreCase(JF_ISXPDM)) {
                    if (PartType.length() > 1) {
                        PartType = PartType.substring(0, 1);
                    }
                    log.info("id>>>>>>>>>>>" + args[0]);
                    AttributeList alist = part.getAttributeValues(context);
                    MapList NumberGenerator = DomainObject.findObjects(context, "eService Number Generator", "*", "name=='" + TYPE_JFDATATRANSMISSION + PartType + "'", new StringList("id"));
                    if (NumberGenerator.size() > 0 && UIUtil.isNotNullAndNotEmpty(PartType)) {
                        JF_VPMT_mxJPO jfVpmtMxJPO = JF_VPMT_mxJPO.getInstance(context);
                        String partName = jfVpmtMxJPO.autoName(context, part, PartType);
                        part.setAttributeValue(context, ATTRIBUTE_PARTNUMBER, partName);
                        part.setAttributeValue(context, JF_PLMConstants_mxJPO.PLMEntity_V_Name, partName);
                    }
                }
                //20260826 update by caipan 创建或复制零件后按详细分类初始化是否缺失图纸
                String detailType = part.getInfo(context, JF_PLMConstants_mxJPO.SELECT_ATTR_JF_Detail_CN);
                if (UIUtil.isNotNullAndNotEmpty(detailType)) {
                    boolean drawingRequired = new JF_VPMReferenceEBOM_mxJPO()
                            .isPartDrawingRequired(context, detailType);
                    part.setAttributeValue(context,
                            "JF_VPMReference.JF_IsThereALackOfDrawings",
                            drawingRequired ? "Yes" : "No");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setDocumentAccess(Context context, String[] args) throws Exception {
        log.info("setDocumentAccess Start");
        String objectId = args[0];
        String attName = args[1];
//        String oldValue = args[2];
        String newValue = args[3];
        String oldValue = args[4];
        String role = "";
        log.info("attName:{}", attName);
        if ("JF_DocSecurity".equalsIgnoreCase(attName)) {
            DomainObject docObj = DomainObject.newInstance(context);
            docObj.setId(objectId);
            String organization = docObj.getInfo(context, DomainConstants.SELECT_ORGANIZATION);
            StringList selList = JF_Util_mxJPO.basicBolistSel();
            selList.add(DomainConstants.SELECT_PROJECT);
            MapList objectList = docObj.getRelatedObjects(context,
                    DomainConstants.MVL_RELATIONSHIP_ACTIVE_VERSION, //pattern to match relationships
                    DomainConstants.TYPE_DOCUMENT, //pattern to match types
                    selList, //the eMatrix StringList object that holds the list of select statement pertaining to Business Obejcts.
                    JF_Util_mxJPO.basicRellistSel(), //the eMatrix StringList object that holds the list of select statement pertaining to Relationships.
                    false, //get To relationships
                    true, //get From relationships
                    (short) 1, //the number of levels to expand, 0 equals expand all.
                    DomainConstants.EMPTY_STRING, //where clause to apply to objects, can be empty ""
                    DomainConstants.EMPTY_STRING, //where clause to apply to relationship, can be empty ""
                    (short) 1 //limit
            );

            if ("Y".equalsIgnoreCase(newValue)) {
                role = context.getUser() + "_PRJ";
            } else {
                //获取文件version的协作区
                role = "JFSeat";
                if (objectList.size() > 0) {
                    role = (String) ((Map) (objectList.get(0))).get(DomainConstants.SELECT_PROJECT);
                }
            }
            try {
                log.info("role :{}", role);
                ContextUtil.startTransaction(context, true);
                docObj.setPrimaryOwnership(context, role, organization);
                ContextUtil.commitTransaction(context);
            } catch (Exception e) {
                e.printStackTrace();
                ContextUtil.abortTransaction(context);
            }
        }
    }

    /*
     * @description:修改图纸V_Name Trigger触发
     * @author: caipan
     * @date:
     * @param: * @param[1] context
     * @param[2] args
     * @return:
     **/
    public int modifyVName(Context context, String[] args) throws Exception {
        try {
            log.info("图纸关联数模的时候修改V_Name:modifyVName");
            ContextUtil.pushContext(context);
            String fromId = args[0];//图纸ID
            String toId = args[1];//数模ID
            DomainObject drwObj = DomainObject.newInstance(context);
            drwObj.setId(fromId);

            DomainObject vpmObj = DomainObject.newInstance(context);
            vpmObj.setId(toId);

            String fromType = drwObj.getInfo(context, DomainConstants.SELECT_TYPE);
            String toType = vpmObj.getInfo(context, DomainConstants.SELECT_TYPE);

            StringList bosel = JF_Util_mxJPO.basicBolistSel();
            StringList relsel = JF_Util_mxJPO.basicRellistSel();
            log.info("fromId:{},toId:{}", fromId, toId);
            if (JF_PLMConstants_mxJPO.TYPE_Drawing.equalsIgnoreCase(fromType) && toType.equalsIgnoreCase(JF_PLMConstants_mxJPO.TYPE_VPMReference)) {
                //第一次关联，也就是数量等于1
                MapList mapList = drwObj.getRelatedObjects(context, JF_PLMConstants_mxJPO.REL_XCADBaseDependency, JF_PLMConstants_mxJPO.TYPE_VPMReference, bosel, relsel,
                        false, true, (short) 1,
                        "", "", 0);//图纸关联的数模的数量
                log.info("mapList size:{}", mapList.size());
                //获取数模的企业编码，赋值到图纸
                if (mapList.size() == 1) {//第一次关联
                    //拿到数模的企业编码，赋值到图纸上面
                    String EIN = vpmObj.getInfo(context, JF_PLMConstants_mxJPO.SELECT_ATTR_V_PART_NUMBER);
                    if (UIUtil.isNotNullAndNotEmpty(EIN)) {
                        drwObj.setAttributeValue(context, JF_PLMConstants_mxJPO.PLMEntity_V_Name, EIN);
                        JF_VPMReference_mxJPO vpm = new JF_VPMReference_mxJPO();
                        updateDrawFileName(context, drwObj, EIN, fromId);
                    }
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage());
        } finally {
            ContextUtil.popContext(context);
        }
        return 0;

    }


    /*
     * @description: 修改图纸的文件名
     * @author: caipan
     * @date:
     * @param: * @param[1] context
     * @param[2] drwId
     * @return:
     **/
    public void updateDrawFileName(Context context, DomainObject drwObj, String newName, String objId) throws Exception {
        String fileString = drwObj.getInfo(context, "format[1].file");
        if (UIUtil.isNotNullAndNotEmpty(fileString) && fileString.startsWith(":")) {
            fileString = fileString.substring(1);
            if (fileString.contains("=")) {
                // 找到最后一个等号的位置
                int lastEqualIndex = fileString.lastIndexOf("=");
                String prefix = fileString.substring(0, lastEqualIndex + 1);
                String fileName = fileString.substring(lastEqualIndex + 1);
                System.out.println("fileName" + fileName);
                if (newName.equals(fileName)) {
                    System.out.println(fileName);
                    return;
                }
                String newFileName = prefix + newName;
                log.info("newFileName:{}", newFileName);
                String mql = "mod bus " + objId + "  rename format 1 file \"" + fileString + "\" \"" + newFileName + "\"";
                log.info("mql:{}", mql);
                MqlUtil.mqlCommand(context, false, mql, true);
                String streamDescriptors = drwObj.getAttributeValue(context, "StreamDescriptors");
                log.info("streamDescriptors start:{}", streamDescriptors);
                if (UIUtil.isNotNullAndNotEmpty(streamDescriptors)) {
                    streamDescriptors = streamDescriptors.replace("'" + fileName + "'", "'" + newName + "'");
                    log.info("streamDescriptors end:{}", streamDescriptors);
                    drwObj.setAttributeValue(context, "StreamDescriptors", streamDescriptors);
                }
            }
        }
        String type = drwObj.getInfo(context, DomainConstants.SELECT_TYPE);
        if (!("Drawing").equals(type)) {
            fileString = drwObj.getInfo(context, "format[2].file");
            if (UIUtil.isNotNullAndNotEmpty(fileString) && fileString.startsWith(":")) {
                fileString = fileString.substring(1);
                if (fileString.contains("=")) {
                    // 找到最后一个等号的位置
                    int lastEqualIndex = fileString.lastIndexOf("=");
                    String prefix = fileString.substring(0, lastEqualIndex + 1);
                    String fileName = fileString.substring(lastEqualIndex + 1);
                    System.out.println("fileName" + fileName);
                    if (newName.equals(fileName)) {
                        System.out.println(fileName);
                        return;
                    }
                    String newFileName = prefix + newName;
                    log.info("newFileName:{}", newFileName);
                    String mql = "mod bus " + objId + "  rename format 2 file \"" + fileString + "\" \"" + newFileName + "\"";
                    MqlUtil.mqlCommand(context, false, mql, true);
                    String streamDescriptors = drwObj.getAttributeValue(context, "StreamDescriptors");
                    streamDescriptors = streamDescriptors.replace("'" + fileName + "'", "'" + newName + "'");
                    if (UIUtil.isNotNullAndNotEmpty(streamDescriptors)) {
                        log.info("streamDescriptors end:{}", streamDescriptors);
                        drwObj.setAttributeValue(context, "StreamDescriptors", streamDescriptors);
                    }
                }
            }
        }
    }

    /**
     * - 整椅发布的时候，给颜色分组关系属性JF_ColorGroupNameEdit设置为freeze
     * - 物理产品冻结-发布的增加Trigger
     * - 如果当前零件是整椅:查询该整椅下面所有层级的零件是否有整椅关联的项目的颜色分组的关系，
     * 如果有设置关系属性JF_ColorGroupNameEdit设置为freeze---
     * 一个零件可以有很多条颜色分组关系(记得得找到整椅关联的项目这一条关系)
     *
     * @param context
     * @param args
     * @return void
     * @throws
     * @author LIUJR
     * @date 2025/4/17 13:19
     * @description
     */
    public void partReleaseSetEditAccess(Context context, String[] args) throws Exception {
        try {
            log.info("partReleaseSetEditAccess..................");
            ContextUtil.pushContext(context);
            String partId = args[0];
            DomainObject domainObject = DomainObject.newInstance(context);
            domainObject.setId(partId);
            String type = domainObject.getInfo(context, DomainConstants.SELECT_TYPE);
            if (!JF_PLMConstants_mxJPO.TYPE_VPMReference.equalsIgnoreCase(type)) {
                return;
            }
            String partType = domainObject.getAttributeValue(context, JF_PLMConstants_mxJPO.ATTR_JFPartType);
            log.info("partType:{}", partType);
           /* if (!("C".equalsIgnoreCase(partType) || "X".equalsIgnoreCase(partType))) {
                return;
            }*/
//            StringList psId = domainObject.getInfoList(context, "to[JFProject2RootPart].from.id");            //只能是整椅
            StringList psList = new JF_VPMReferenceEBOM_mxJPO().getPartZeroProject(context, partId);
            log.info("psList:{}", psList);
            if (psList.size() == 0) {
                return;
            }
            StringList basicBolistSel = JF_Util_mxJPO.basicBolistSel();
            StringList basicRellistSel = JF_Util_mxJPO.basicRellistSel();
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
            log.info("childPartList:{}", childPartList);
            Iterator iterator = childPartList.iterator();
            while (iterator.hasNext()) {
                Map map = (Map) iterator.next();
                String id = UIUtil.getValue(map, DomainConstants.SELECT_ID);
                Map map1 = new HashMap<>();
                for (String psId : psList) {
                    map1.put("relName", JF_PLMConstants_mxJPO.rel_JFProject2ColorGroup);
                    map1.put("fromId", psId);
                    map1.put("toId", id);
                    String connId = JF_PublicMethodClass_mxJPO.getTwoBusinessObjectConnId(context, JPO.packArgs(map1));
                    log.info("connId:{}", connId);
                    if (UIUtil.isNullOrEmpty(connId)) {
                        continue;
                    }
                    DomainRelationship domainRelationship = DomainRelationship.newInstance(context, connId);
                    domainRelationship.setAttributeValue(context, "JF_ColorGroupNameEdit", "freeze");
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
     * 原件变形件升版逻辑；  第一版本
     * 在零件升版（revision action）的时候创建trigger,逻辑如下:
     * 1. 冻结数据升版时，
     * 1.1. 若是原件且存在变形件关系，将升版出来的原件，替换原本变形件关系的FROM端；
     * 1.2. 若是变形件且存在变形件关系，将升版出来的变形件，替换原本变形件关系的TO端；
     * 2. 已发布数据升版时，判断是否原件/变形件
     * 2.1. 若是原件，升版原件，检查升版之前的原件关联的已发布变形件是否有更高版本，若有需要创建和最新版本变形件的关系；
     * 2.2. 若是变形件，检查关联的原件是否是最新版本
     * 2.2.1. 原件是最新版本，变形件升版；
     * 2.2.2. 原件不是最新版本，只升版所选变形件并与最新版原件创建变形件关系，创建关系时修改变形件版本与原件一致（大版本一致，默认从XX.1开始）。
     *
     * @param context
     * @param args
     * @return void
     * @throws
     * @author LIUJR
     * @date 09/06/2025 10:11
     * @description
     */
    public void originalPartRevisionAndConnOne(Context context, String[] args) throws Exception {
        try {
            log.info("originalPartRevisionAndConn start。。。。。。。。。。。。");
            String objectId = args[0];
            String newObjectId = args[1];
            log.info("objectId:{}", objectId);
            log.info("newObjectId:{}", newObjectId);
            //升版的零件
            DomainObject oldObject = new DomainObject(objectId);
            //判断升版的零件是升大版本还是小版本
            String oldCurrent = oldObject.getInfo(context, DomainConstants.SELECT_CURRENT);
            String original = oldObject.getAttributeValue(context, JF_PLMConstants_mxJPO.Attr_JF_OriginalPart);
            String flex = oldObject.getAttributeValue(context, JF_PLMConstants_mxJPO.Attr_JF_FlexiblePart);
            log.info("oldCurrent:{}", oldCurrent);
            log.info("original:{}", original);
            log.info("flex:{}", flex);
            //如果不是原件 也不是变形件 返回不操作
            if (("N".equalsIgnoreCase(original) && "N".equalsIgnoreCase(flex))) {
                log.info("return。。。。。。。。。。。。。");
                return;
            }
            DomainObject newObject = DomainObject.newInstance(context, newObjectId);
            if ("FROZEN".equalsIgnoreCase(oldCurrent)) {
                log.info("。。。。。。升小版本。start。。。。。。");
                 /*升小版本
                    1. 若是原件且存在变形件关系，将升版出来的原件，替换原本变形件关系的FROM端；
                    2. 若是变形件且存在变形件关系，将升版出来的变形件，替换原本变形件关系的TO端；*/
                //如果是原件升版
                MapList originalFlexMapList = new MapList();
                Boolean flag = Boolean.TRUE;
                if ("Y".equalsIgnoreCase(original)) {
                    log.info("。。。。。。获取原件的变形件  一个或者多个。。。。。。。");
                    //获取原件的变形件  一个或者多个
                    originalFlexMapList = getOriginalFlexMapList(context, oldObject, Boolean.TRUE);
                }
                //如果是变形件升版
                if ("Y".equalsIgnoreCase(flex)) {
                    //获取变形件的原件 一个
                    log.info("。。。。。。获取变形件的原件 一个。。。。。。。");
                    originalFlexMapList = getOriginalFlexMapList(context, oldObject, Boolean.FALSE);
                    flag = Boolean.FALSE;
                }
                log.info("originalFlexMapList：{}", originalFlexMapList);
                log.info("flag:{}", flag);
                log.info("。。。。。。关联关系 一个。。。。。。。");
                ContextUtil.pushContext(context);
                try {
                    for (int i = 0; i < originalFlexMapList.size(); i++) {
                        Map map = (Map) originalFlexMapList.get(i);
                        String connId = UIUtil.getValue(map, DomainRelationship.SELECT_ID);
                        if (flag) {
                            //原件    重新替换from端 将升版出来的原件，替换原本变形件关系的FROM端；
                            DomainRelationship.setFromObject(context, connId, newObject);
                        } else {
                            //变形件   重新替换to端  将升版出来的变形件，替换原本变形件关系的TO端
                            DomainRelationship.setToObject(context, connId, newObject);
                        }
                    }
                } catch (Exception e) {
                    throw e;
                } finally {
                    ContextUtil.popContext(context);
                }
                log.info("。。。。。。升小版本。end。。。。。。");
            } else if ("RELEASED".equalsIgnoreCase(oldCurrent)) {
                /*升大版本
                 * 1. 若是原件，升版原件，检查升版之前的原件关联的已发布变形件是否有更高版本，若有需要创建和最新版本变形件的关系；
                 * 2. 若是变形件，检查关联的原件是否是最新版本
                 *      2.1. 原件是最新版本，变形件升版；
                 *      2.2. 原件不是最新版本，只升版所选变形件并与最新版原件创建变形件关系，创建关系时修改变形件版本与原件一致（大版本一致，默认从XX.1开始）。
                 *
                 * */
                log.info("。。。。。。升大版本。start。。。。。。");
                DomainObject domainObject = DomainObject.newInstance(context);
                if ("Y".equalsIgnoreCase(original)) {
                    //获取原件的变形件  一个或者多个
                    log.info("。。。。。。获取原件的变形件  一个或者多个。。。。。。");
                    StringList flexList = oldObject.getInfoList(context, "from[" + JF_PLMConstants_mxJPO.REL_JFOriginalPart2Flexible + "].to.id");
                    log.info("flexList:{}", flexList);
                    //遍历变形件  检测已发布变形件是否有更高版本，若有需要创建和最新版本变形件的关系；
                    StringList newFlexList = new StringList();
                    for (int i = 0; i < flexList.size(); i++) {
                        String flexId = flexList.get(i);
                        domainObject.setId(flexId);
                        String isLastVersion = domainObject.getAttributeValue(context, JF_PLMConstants_mxJPO.ATTR_V_isLastVersion);
                        if ("FALSE".equalsIgnoreCase(isLastVersion)) {
                            String lastMajorid = JF_Util_mxJPO.getLastMajorId(context, flexId);
                            newFlexList.add(lastMajorid);
                        }
                    }
                    log.info("newFlexList:{}", newFlexList);
                    //关联关系
                    if (!newFlexList.isEmpty()) {
                        DomainRelationship.connect(context, newObject, new RelationshipType(JF_PLMConstants_mxJPO.REL_JFOriginalPart2Flexible), true, newFlexList.toStringArray());
                    }
                }
                if ("Y".equalsIgnoreCase(flex)) {
                    //获取变形件的原件  一个
                    /*
                     * 若是变形件，检查关联的原件是否是最新版本
                     * 2.1. 原件是最新版本，变形件升版；
                     * 2.2. 原件不是最新版本，只升版所选变形件并与最新版原件创建变形件关系，创建关系时修改变形件版本与原件一致（大版本一致，默认从XX.1开始）。
                     * */
                    String originalId = oldObject.getInfo(context, "to[" + JF_PLMConstants_mxJPO.REL_JFOriginalPart2Flexible + "].from.id");
                    log.info("originalId:{}", originalId);
                    if (UIUtil.isNotNullAndNotEmpty(originalId)) {
                        domainObject.setId(originalId);
                        String isLastVersion = domainObject.getAttributeValue(context, JF_PLMConstants_mxJPO.ATTR_V_isLastVersion);
                        log.info("isLastVersion:{}", isLastVersion);
                        if ("FALSE".equalsIgnoreCase(isLastVersion)) {
                            //获取升版后的原件
                            String lastMajorid = JF_Util_mxJPO.getLastMajorId(context, originalId);
                            log.info("lastMajorid:{}", lastMajorid);
                            newObject.addFromObject(context, new RelationshipType(JF_PLMConstants_mxJPO.REL_JFOriginalPart2Flexible), lastMajorid);
                        }
                    }
                }
                log.info("。。。。。。升大版本。end。。。。。。");
            }
        } catch (Exception e) {
            e.printStackTrace();
            log.warn(e.getMessage());
            throw e;
        }
        log.info("originalPartRevisionAndConn end。。。。。。。。。。。。");
    }

    /**
     * 变形件关系管理 第二版：  最新版本
     * 2. 任何状态数据升版时，判断是否原件/变形件
     * 1. 若是原件，升版原件，检查升版之前的原件关联的已发布变形件是否有更高版本，且与原件版本一致的变形件，若有需要创建和最新版本变形件的关系；
     * 2. 若是变形件，检查关联的原件是否是最新版本
     * 1. 原件是最新版本，变形件升版；
     * 2. 原件不是最新版本，只升版所选变形件并与最新版原件创建变形件关系。
     *
     * @param context
     * @param args
     * @return void
     * @throws
     * @author LIUJR
     * @date 2026/1/12 14:12
     * @description
     */
    public void originalPartRevisionAndConn(Context context, String[] args) throws Exception {
        try {
            log.info("originalPartRevisionAndConn start。。。。。。。。。。。。");
            String objectId = args[0];
            String newObjectId = args[1];
            log.info("objectId:{}", objectId);
            log.info("newObjectId:{}", newObjectId);
            //升版的零件
            DomainObject oldObject = new DomainObject(objectId);
            //判断升版的零件是原件还是变形件
            String original = oldObject.getAttributeValue(context, JF_PLMConstants_mxJPO.Attr_JF_OriginalPart);
            String flex = oldObject.getAttributeValue(context, JF_PLMConstants_mxJPO.Attr_JF_FlexiblePart);
            log.info("original:{}", original);
            log.info("flex:{}", flex);
            //如果不是原件 也不是变形件 返回不操作
            if (("N".equalsIgnoreCase(original) && "N".equalsIgnoreCase(flex))) {
                log.info("return。。。。。。。。。。。。。");
                return;
            }
            DomainObject newObject = DomainObject.newInstance(context, newObjectId);
            String newRev = newObject.getInfo(context, SELECT_REVISION);
            /*升大版本
             * 任何状态数据升版时，判断是否原件/变形件
             *     1. 若是原件，升版原件，检查升版之前的原件关联的已发布变形件是否有更高版本，且与原件版本一致的变形件，若有需要创建和最新版本变形件的关系；
             *     2. 若是变形件，检查关联的原件是否是最新版本
             *       1. 原件是最新版本，变形件升版；
             *       2. 原件不是最新版本，只升版所选变形件并与最新版原件创建变形件关系。
             * */
            log.info("。。。。。。关系管理.......start。。。。。。");
            DomainObject domainObject = DomainObject.newInstance(context);
            if ("Y".equalsIgnoreCase(original)) {
                //若是原件，升版原件，检查升版之前的原件关联的已发布变形件是否有更高版本，且与原件版本一致的变形件，若有需要创建和最新版本变形件的关系；
                log.info("。。。。。。获取原件的变形件  一个或者多个。。。。。。");
                StringList flexList = oldObject.getInfoList(context, "from[" + JF_PLMConstants_mxJPO.REL_JFOriginalPart2Flexible + "].to.id");
                log.info("flexList:{}", flexList);
                //遍历变形件  检测已发布变形件是否有更高版本，若有需要创建和最新版本变形件的关系；
                StringList newFlexList = new StringList();
                for (int i = 0; i < flexList.size(); i++) {
                    String flexId = flexList.get(i);
                    domainObject.setId(flexId);
                    String isLastVersion = domainObject.getAttributeValue(context, JF_PLMConstants_mxJPO.ATTR_V_isLastVersion);
                    if ("FALSE".equalsIgnoreCase(isLastVersion)) {
//                        getPartSameVersionLastRevision(context, flexId, newRev);
                        String lastMajorid = JF_Util_mxJPO.getLastMajorId(context, flexId);
                        if (UIUtil.isNullOrEmpty(lastMajorid)) {
                            continue;
                        }
                        domainObject.setId(lastMajorid);
                        if (newRev.equalsIgnoreCase(domainObject.getInfo(context, SELECT_REVISION))) {
                            newFlexList.add(lastMajorid);
                        }
                    }
                }
                log.info("newFlexList:{}", newFlexList);
                //关联关系
                if (!newFlexList.isEmpty()) {
                    DomainRelationship.connect(context, newObject, new RelationshipType(JF_PLMConstants_mxJPO.REL_JFOriginalPart2Flexible), true, newFlexList.toStringArray());
                }
            }
            if ("Y".equalsIgnoreCase(flex)) {
                /*
                 * 若是变形件，检查关联的原件是否是最新版本
                 *       1. 原件是最新版本，变形件升版；
                 *       2. 原件不是最新版本，只升版所选变形件并与最新版原件创建变形件关系。
                 * */
                String originalId = oldObject.getInfo(context, "to[" + JF_PLMConstants_mxJPO.REL_JFOriginalPart2Flexible + "].from.id");
                log.info("originalId:{}", originalId);
                if (UIUtil.isNotNullAndNotEmpty(originalId)) {
                    domainObject.setId(originalId);
                    String isLastVersion = domainObject.getAttributeValue(context, JF_PLMConstants_mxJPO.ATTR_V_isLastVersion);
                    log.info("isLastVersion:{}", isLastVersion);
                    if ("FALSE".equalsIgnoreCase(isLastVersion)) {
                        //获取升版后的原件
//                        JF_Util_mxJPO.getLastReleasedMajorid(context, originalId);
                        String lastMajorid = JF_Util_mxJPO.getLastMajorId(context, originalId);
                        log.info("lastMajorid:{}", lastMajorid);
                        if (UIUtil.isNotNullAndNotEmpty(lastMajorid)) {
                            domainObject.setId(lastMajorid);
                            log.info("newRev:{}", newRev);
                            log.info("domainObject.getInfo(context, SELECT_REVISION):{}", domainObject.getInfo(context, SELECT_REVISION));
                            if (newRev.equalsIgnoreCase(domainObject.getInfo(context, SELECT_REVISION))) {
                                newObject.addFromObject(context, new RelationshipType(JF_PLMConstants_mxJPO.REL_JFOriginalPart2Flexible), lastMajorid);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            log.warn(e.getMessage());
            throw e;
        }
        log.info("originalPartRevisionAndConn end。。。。。。。。。。。。");
    }

    /**
     * 获取原件的变形件活变形件的原件
     *
     * @param context
     * @param oldObject
     * @param isOriginal
     * @return com.matrixone.apps.domain.util.MapList
     * @throws
     * @author LIUJR
     * @date 2025/12/26 14:59
     * @description
     */
    public MapList getOriginalFlexMapList(Context context, DomainObject oldObject, Boolean isOriginal) throws Exception {
        MapList childPartList = oldObject.getRelatedObjects(
                context,
                JF_PLMConstants_mxJPO.REL_JFOriginalPart2Flexible,
                JF_PLMConstants_mxJPO.TYPE_VPMReference,
                JF_Util_mxJPO.basicBolistSel(),
                JF_Util_mxJPO.basicRellistSel(),
                !isOriginal,
                isOriginal,
                (short) 1,
                "",
                "",
                0
        );
        return childPartList;
    }


    /**
     * @return void
     * @Author Liuxg
     * @Description 创建，clone，复制的时候设置对象版本
     * @Date 2025/12/30 11:06
     * @Param [context, args]
     **/
    public void CreateSetRevision(Context context, String[] args) throws Exception {
        try {
            String id = args[0];

            DomainObject vpmobj = DomainObject.newInstance(context, id);
            String name = vpmobj.getName(context);
            log.info("CreateSetRevision---before->" + vpmobj.getRevision());

            String mql = "mod bus '" + id + "' name '" + name + "' revision 'AA.1-000'";
            MqlUtil.mqlCommand(context, false, mql, true);
            log.info("CreateSetRevision---after->" + vpmobj.getRevision());

            StringList shapelist = vpmobj.getInfoList(context, "from[VPMRepInstance].to.id");
            for (String shapeid : shapelist) {

                DomainObject shapeObj = DomainObject.newInstance(context, shapeid);
                String shapeName = shapeObj.getName(context);

                String mql2 = "mod bus '" + shapeid + "' name '" + shapeName + "' revision 'AA.1-000'";
                MqlUtil.mqlCommand(context, false, mql2, true);

            }


        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * @return void
     * @Author Liuxg
     * @Description 冻结手动升小版（中间的版本+1）
     * @Date 2025/12/30 11:20
     * @Param [context, args]
     **/
    public void SetRevisionForForzen(Context context, String[] args) throws Exception {
        try {
//            String id=args[0];
//            DomainObject obj=DomainObject.newInstance(context,id);
//            String name=obj.getName(context);
//            String rev=obj.getRevision();
//            StringList revlist=splitRev(rev);
//            if(revlist.size()==3){
//                String rev1=revlist.get(0);
//                String rev2=revlist.get(1);
//                String rev3=revlist.get(2);
//
//                int rev2int=Integer.parseInt(rev2);
//                rev2int++;
//                StringBuffer sb=new StringBuffer();
//                sb.append(rev1).append("\\.").append(rev2int).append("-").append("000");
//                log.info("new rev----->"+sb.toString());
//
//                String mql = "mod bus '" + id + "' name '" + name + "' revision '"+sb.toString()+"'";
//                MqlUtil.mqlCommand(context, false, mql, true);
//            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * @return void
     * @Author Liuxg
     * @Description 发布手动升大版（前面的版本+1）,冒泡升版的话就是流水+1（后面版本数字+1）
     * @Date 2025/12/30 11:20
     * @Param [context, args]
     **/
    public void SetRevisionForReleased(Context context, String[] args) throws Exception {
        try {
//            String user=context.getUser();
//            if(user.equals("admin_platform")){
//                //冒泡升版
//                //1,找到当前vpm的上一个发布版
//                //JF_Util_mxJPO.getLastReleasedMajoridExcludeOwner()
//
//            }else {
//                //其他发布升版（手动发布升版）
//                String id=args[0];
//                DomainObject obj=DomainObject.newInstance(context,id);
//                String name=obj.getName(context);
//                String rev=obj.getRevision();
//                String[]revs=rev.split("-");
//                String newRev=revs[0]+"-"+"000";
//                String mql = "mod bus '" + id + "' name '" + name + "' revision '"+newRev+"'";
//                MqlUtil.mqlCommand(context, false, mql, true);
//
//            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * @return void
     * @Author Liuxg
     * @Description 获取版本的三段结构
     * @Date 2026/1/4 8:58
     * @Param [rev]
     **/
    public StringList splitRev(String rev) throws Exception {
        StringList revlist = new StringList();
        try {
            //AA.1-000
            String revParts[] = rev.split("\\.");
            String rev1 = revParts[0];
            String revarray2 = revParts[1];
            String revParts2[] = revarray2.split("-");
            String rev2 = revParts2[0];
            String rev3 = revParts2[1];

            log.info("rev1---->" + rev1);
            log.info("rev2---->" + rev2);
            log.info("rev3---->" + rev3);
            revlist.add(rev1);
            revlist.add(rev2);
            revlist.add(rev3);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return revlist;
    }


    public void setVpmRevisionForMajor(Context context, String[] args) throws Exception {
        try {
            log.info("setVpmRevisionForMajor start。。。。。。。。。。。。");
            String objectId = args[0];
            String newObjectId = args[1];

            //升版的零件
            DomainObject oldObject = new DomainObject(objectId);
            //判断升版的零件是升大版本还是小版本
            String oldCurrent = oldObject.getInfo(context, DomainConstants.SELECT_CURRENT);
            log.info("oldCurrent--->" + oldCurrent);
//            if ("FROZEN".equalsIgnoreCase(oldCurrent)) {
//                //升小版
//                DomainObject newobj = DomainObject.newInstance(context, newObjectId);
//                String name = newobj.getName(context);
//                String rev = newobj.getRevision();
//                if (UIUtil.isNullOrEmpty(rev)) {
//                    rev = newobj.getRevision(context);
//                }
//                StringList revlist = splitRev(rev);
//                if (revlist.size() == 3) {
//                    String rev1 = revlist.get(0);
//                    String rev2 = revlist.get(1);
//                    String rev3 = revlist.get(2);
//
//                    int rev2int = Integer.parseInt(rev2);
//                    rev2int++;
//                    StringBuffer sb = new StringBuffer();
//                    sb.append(rev1).append(".").append(rev2int).append("-").append("000");
//                    log.info("new rev----->" + sb.toString());
//
//                    String mql = "mod bus '" + newObjectId + "' name '" + name + "' revision '" + sb.toString() + "'";
//                    MqlUtil.mqlCommand(context, false, mql, true);
//                }
//
//            } else if ("RELEASED".equalsIgnoreCase(oldCurrent)) {
                //冒泡升版
                String user = context.getUser();
                log.info("冒泡升版    user--->" + user);
                DomainObject newobj = DomainObject.newInstance(context, newObjectId);
                String isBubble = newobj.getAttributeValue(context, JF_PLMConstants_mxJPO.Attr_JF_IsBubbling);
                log.info("isBubble --->" + isBubble);
                log.info("newObjectId:{}",newObjectId);
                log.info("objectId:{}",objectId);
                String reallyRev="";
                if ("Y".equalsIgnoreCase(isBubble)) {
                    try {
                        ContextUtil.pushContext(context);
                        //冒泡升版
                        //1,找到当前vpm的上一个发布版
                        //JF_Util_mxJPO.getLastReleasedMajoridExcludeOwner()
                        String newobjName = newobj.getName(context);
                        //拿到旧对象版本
                        String rev = oldObject.getRevision();
                        if (UIUtil.isNullOrEmpty(rev)) {
                            rev = oldObject.getRevision(context);
                        }
                        StringList revlist = splitRev(rev);
                        if (revlist.size() == 3) {
                            String rev1 = revlist.get(0);
                            String rev2 = revlist.get(1);
                            String rev3 = revlist.get(2);

                            int rev3int = Integer.parseInt(rev3);
                            rev3int++;
                            // 格式化为三位，不足补前导零
                            if (rev3int > 999) {
                                rev3int = 999;
                            }
                            String newRev3 = String.format("%03d", rev3int);

                            StringBuffer sb = new StringBuffer();
                            //流水+1
                            sb.append(rev1).append(".").append(rev2).append("-").append(newRev3);
                            log.info("new rev----->" + sb.toString());
                            reallyRev=sb.toString();
                            String mql = "mod bus '" + newObjectId + "' name '" + newobjName + "' revision '" + sb.toString() + "'";
                            log.info("mql:{} ",mql);
                           String str1 = MqlUtil.mqlCommand(context, false, mql, true);
                            log.info("str1:{} ",str1);
//
//                            //修改到发布
//                            String mql2 = "mod bus '" + newObjectId + "' current RELEASED";
//                            String str = MqlUtil.mqlCommand(context, false, mql2, true);
//                            log.info("mql2:{} str:{}",mql2,str);
                        }
                        //排序后的所有版本
                        BusinessObjectList majorRevisionsBusObjList = JF_Util_mxJPO.sortMapListInRevision(context, new String[]{newObjectId});
                        BusinessObject majorRevObj = majorRevisionsBusObjList.get(majorRevisionsBusObjList.size() - 1);
                        newobj.setAttributeValue(context, "PLMReference.V_isLastVersion", "FALSE");
                        majorRevObj.setAttributeValue(context, "PLMReference.V_isLastVersion", "TRUE");
                        log.info("admin RELEASED--->" + newobj.getRevision());
                        //零件冒泡后重新设置最后一个版本和新版本的PLMReference.V_isLastVersion
                        //冒泡版本对象的数模文件对象
                        StringList shapelist = newobj.getInfoList(context, "from[VPMRepInstance].to.id");
                        StringList XCADAssemblylist = newobj.getInfoList(context, "from[XCADAssemblyRepInstance].to.id");
                        shapelist.addAll(XCADAssemblylist);
                        DomainObject domainObject = DomainObject.newInstance(context);
                        for (int i = 0; i < shapelist.size(); i++) {
                            domainObject.setId(shapelist.get(i));
                            domainObject.setAttributeValue(context, "PLMReference.V_isLastVersion", "FALSE");
                        }
                        shapelist.clear();
                        //最新版本对象的数模文件对象
                        domainObject.setId(majorRevObj.getObjectId(context));
                        shapelist = domainObject.getInfoList(context, "from[VPMRepInstance].to.id");
                        XCADAssemblylist = domainObject.getInfoList(context, "from[XCADAssemblyRepInstance].to.id");
                        shapelist.addAll(XCADAssemblylist);
                        for (int i = 0; i < shapelist.size(); i++) {
                            domainObject.setId(shapelist.get(i));
                            domainObject.setAttributeValue(context, "PLMReference.V_isLastVersion", "TRUE");
                        }
                    } finally {
                        ContextUtil.popContext(context);
                    }
                } else {
                    //非冒泡升版
                    //先获取非当前对象的最新版本对象，是否发布，然后基于其修改
                    try {

                        BusinessObjectList majorRevisionsBusObjList=JF_Util_mxJPO.sortMapListInRevisionExcludeTransmitPartId(context,new String[]{newObjectId});

                        ContextUtil.pushContext(context);

                        log.info("majorRevisionsBusObjList-->"+majorRevisionsBusObjList);
                        log.info("majorRevisionsBusObjList-->",majorRevisionsBusObjList);
                        log.info("majorRevisionsBusObjList.size-->"+majorRevisionsBusObjList.size());


                        //非当前对象的最新版本
                        BusinessObject majorRevObjbu = majorRevisionsBusObjList.get(majorRevisionsBusObjList.size() - 1);
                        String OLDid=majorRevObjbu.getObjectId(context);
                        DomainObject majorRevObj=DomainObject.newInstance(context,OLDid);
                        String OLDrev = majorRevObj.getRevision();
                        String OLDcurrent = majorRevObj.getCurrentState(context).getName();

                        String revbu=majorRevObjbu.getRevision();


                        log.info("majorRevObjbu-->"+majorRevObjbu);
                        log.info("revbu-->"+revbu);
                        log.info("OLDrev-->"+OLDrev);
                        log.info("OLDid-->"+OLDid);
                        log.info("OLDcurrent-->"+OLDcurrent);


                        if (UIUtil.isNullOrEmpty(OLDrev)) {
                            OLDrev = majorRevObj.getRevision(context);
                        }

                        String name = newobj.getName(context);
                        StringList revlist = splitRev(OLDrev);
                        if (revlist.size() == 3) {
                            String rev1 = revlist.get(0);
                            String rev2 = revlist.get(1);
                            String rev3 = revlist.get(2);
                            StringBuffer sb = new StringBuffer();
                            if ("RELEASED".equalsIgnoreCase(OLDcurrent)) {
                                //是发布  AB.1-000 > AC.1-000
                                rev1= increment(rev1);
                                rev2="1";
                            }else {
                                //不是发布  AB.1-000 >  AB.2-000
                                int rev2int = Integer.parseInt(rev2);
                                rev2int++;
                                rev2=rev2int+"";
                            }


                            sb.append(rev1).append(".").append(rev2).append("-").append("000");
                            log.info("new rev----->" + sb.toString());

                            String mql = "mod bus '" + newObjectId + "' name '" + name + "' revision '" + sb.toString() + "'";
                            MqlUtil.mqlCommand(context, false, mql, true);
                            log.info("not admin --->" + newobj.getRevision());

                            reallyRev= sb.toString();
                        }

                    } catch (Exception e2) {
                        e2.printStackTrace();
                    } finally {
                        ContextUtil.popContext(context);
                    }
                }


                //无论是冒泡还手动，3dshape和特殊类型均需要跟着变化版本
                try {
                    ContextUtil.pushContext(context);

                    String newRev=reallyRev;

                    log.info(" other RELEASED--newRev->" + newRev);
                    if(UIUtil.isNotNullAndNotEmpty(newRev)){
                        StringList shapelist = newobj.getInfoList(context, "from[VPMRepInstance].to.id");

                        StringList XCADAssemblylist = newobj.getInfoList(context, "from[XCADAssemblyRepInstance].to.id");

                        shapelist.addAll(XCADAssemblylist);

                        log.info("admin other RELEASED--shapelist->" + shapelist);

                        for (String shapeid : shapelist) {

                            DomainObject shapeObj = DomainObject.newInstance(context, shapeid);
                            String shapeName = shapeObj.getName(context);

                            String mql3 = "mod bus '" + shapeid + "' name '" + shapeName + "' revision '" + newRev + "'";
                            MqlUtil.mqlCommand(context, false, mql3, true);

                        }
                    }




                } catch (Exception e3) {
                    e3.printStackTrace();
                } finally {
                    ContextUtil.popContext(context);
                }

//            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static String increment(String input) {
        // 严格限定输入必须为两位大写字母
        if (input == null || input.length() != 2 ||
                !Character.isUpperCase(input.charAt(0)) ||
                !Character.isUpperCase(input.charAt(1))) {
            throw new IllegalArgumentException("ERROR");
        }

        char[] chars = input.toCharArray();
        // 只处理第二位字符进位
        if (chars[1] == 'Z') {
            chars[1] = 'A';
            // 处理第一位字符进位
            if (chars[0] == 'Z') {
                chars[0] = 'A'; // 循环回AA
            } else {
                chars[0]++;
            }
        } else {
            chars[1]++;
        }
        return new String(chars);
    }

    /**
     * @return void
     * @Author Liuxg
     * @Description 3Dshape创建和copy的时候设置版本
     * @Date 2026/1/7 12:41
     * @Param [context, args]
     **/
    public void CreateSetRevisionFor3Dshape(Context context, String[] args) throws Exception {
        try {
            String id = args[0];
            DomainObject shapeobj = DomainObject.newInstance(context, id);
            //和vpm保持一致
            String vpmrev = shapeobj.getInfo(context, "to[VPMRepInstance].from.revision");

            String name = shapeobj.getName(context);
            log.info("CreateSetRevisionFor3Dshape---before->" + shapeobj.getRevision());
            log.info("CreateSetRevisionFor3Dshape---vpmrev->" + vpmrev);
            if (UIUtil.isNotNullAndNotEmpty(vpmrev)) {
                String mql = "mod bus '" + id + "' name '" + name + "' revision '" + vpmrev + "'";
                MqlUtil.mqlCommand(context, false, mql, true);
            }
            log.info("CreateSetRevisionFor3Dshape---after->" + shapeobj.getRevision());


        } catch (Exception e) {
            e.printStackTrace();
        }
    }




    /*
     * @description: PLMReference.V_isLastVersion 保证对
     * @author: caipan
     * @date: 2026/1/23 16:57:42
     * @param: * @param[1] context
     * @param[2] args
     * @return: 
     **/
    public void modifyVPMReferenceV_isLastVersion(Context context,String[] args) throws Exception {
        //需要找出当前零件所有版本的是否最新版标识
        log.info("modifyVPMReferenceV_isLastVersion start");
        String objectId = args[0];
        String type = args[1];
        String name = args[2];
        log.info("type:{}",type);
        log.info("name:{}",name);
        try {
            int iReturn = 0;
//        if (JF_PLMConstants_mxJPO.ATTR_V_IsLastVersion.equalsIgnoreCase(modAttrName)) {
            String where = "name=='"+name+"'";
            ContextUtil.pushContext(context);
            MapList vpmList = DomainObject.findObjects(context,JF_PLMConstants_mxJPO.TYPE_VPMReference,"*",where,JF_Util_mxJPO.basicBolistSel());
            if(null!=vpmList&&vpmList.size()>1) {
                objectId = UIUtil.getValue((Map)vpmList.get(0), SELECT_ID);
                log.info("objectId:{}",objectId);
                BusinessObjectList list = JF_Util_mxJPO.sortMapListInRevision(context, new String[]{objectId});
                log.info("list:{}", list);
                if (list != null) {
                    //最新版
                    BusinessObject bus = list.get(list.size() - 1);
                    bus.setAttributeValue(context, JF_PLMConstants_mxJPO.ATTR_V_IsLastVersion, "true");
                    DomainObject part = DomainObject.newInstance(context, bus.getObjectId(context));
                    String V_usage = part.getAttributeValue(context,"PLMEntity.V_usage");
                    String rel ="";
                    if(UIUtil.isNullOrEmpty(V_usage)){
                        rel = "XCADAssemblyRepInstance";
                    }else{
                        rel = "VPMRepInstance";
                    }
                    StringList subList = part.getInfoList(context, "from["+rel+"].to.id");
                    log.info("subList:{}", subList);
                    for (int i = 0; i < subList.size(); i++) {
                        DomainObject sub = DomainObject.newInstance(context, subList.get(i));
                        sub.setAttributeValue(context, JF_PLMConstants_mxJPO.ATTR_V_IsLastVersion, "true");
                    }
                    //其他版本改成不是最新版
                    for (int i = 0; i < list.size() - 1; i++) {
                        bus = list.get(i);
                        bus.setAttributeValue(context, JF_PLMConstants_mxJPO.ATTR_V_IsLastVersion, "false");
                        part = DomainObject.newInstance(context, bus.getObjectId(context));
                        subList = part.getInfoList(context, "from["+rel+"].to.id");
                        log.info("subList2:{}", subList);
                        for (int j = 0; j < subList.size(); j++) {
                            DomainObject sub = DomainObject.newInstance(context, subList.get(j));
                            sub.setAttributeValue(context, JF_PLMConstants_mxJPO.ATTR_V_IsLastVersion, "false");
                        }

                    }
                }
//        }
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            ContextUtil.popContext(context);
        }
        log.info("modifyVPMReferenceV_isLastVersion end");
    }


    /**
     * @return void
     * @Author Liuxg
     * @Description
     * @Date 2026/1/13 8:50
     * @Param [context, args]
     **/
    public void getReviewECRReject(Context context, String[] args) throws Exception {
        Map pramap = JPO.unpackArgs(args);
        StringList oldidlist = (StringList) pramap.get("oldidlist");
        getReviewECRReject(context, oldidlist);
    }


    /**
     * @return void
     * @Author Liuxg
     * @Description 根据冒泡升版后的ECR的旧数据，查询出来关联的其他审核中的ECR并驳回
     * @Date 2026/1/10 12:05
     * @Param [context, oldidlist]
     **/
    public void getReviewECRReject(Context context, StringList oldidlist) throws Exception {
        try {

            Set<String> rejectEcrSet = new HashSet<String>();

            //遍历旧数据集合,查询出来的ECR需要去重
            for (String oldid : oldidlist) {
                DomainObject oldobj = DomainObject.newInstance(context, oldid);
                StringList ECRidlist = oldobj.getInfoList(context, "to[JFRelateItem].from.id");
                for (String ecrid : ECRidlist) {
                    DomainObject ecrobj = DomainObject.newInstance(context, ecrid);
                    String ecrCurrent = ecrobj.getCurrentState(context).getName();
                    if ("Review".equals(ecrCurrent)) {
                        rejectEcrSet.add(ecrid);
                    }
                }
            }

            StringList bul = JF_Util_mxJPO.basicBolistSel();
            //企业编码
            bul.add(JF_PLMConstants_mxJPO.SELECT_ATTR_V_PART_NUMBER);

            //旧数据详细信息
            Map oldpartMap = new HashMap();
            for (String oldpartid : oldidlist) {
                DomainObject oldpartObj = DomainObject.newInstance(context, oldpartid);
                Map partmap = oldpartObj.getInfo(context, bul);
                String LatestMajorid = JF_Util_mxJPO.getLatestMajorid_new(context, new String[]{oldpartid});
                log.info("LatestMajorid--->" + LatestMajorid);
                DomainObject LatestMajorObj = DomainObject.newInstance(context, LatestMajorid);
                String LatestRev = LatestMajorObj.getRevision(context);
                log.info("LatestRev--->" + LatestRev);
                partmap.put("LatestRev", LatestRev);

                oldpartMap.put(oldpartid, partmap);
            }

            //需要处理的ECR和相关的零件信息
            for (String ecrid : rejectEcrSet) {
                //查询每个ECR相关的已经升版过的零件号
                DomainObject ecrObj = DomainObject.newInstance(context, ecrid);
                MapList ecrErrorPartlist = new MapList();

                StringList partidlist = ecrObj.getInfoList(context, "from[JFRelateItem].to.id");
                for (String partid : partidlist) {
                    //Ecr关联的数据被包含在旧版本数据集合中
                    if (oldpartMap.containsKey(partid)) {
                        ecrErrorPartlist.add(oldpartMap.get(partid));
                    }
                }
//                errorEcrMap.put(ecrid,ecrErrorPartlist);
                //驳回审核中的ECR
                //设置ECR的属性JFAllowableReview为N，
                // 设置 xxx(所有的零件企业编码_版本号)零件不是最新发布版本，请撤回ECR、替换零件、重新发起ECR审核，保存到JFReviewMessage属性中
                //发送邮件通知ECRowner

                StringBuffer msgPartsb = new StringBuffer();
                for (int j = 0; j < ecrErrorPartlist.size(); j++) {
                    Map errorpartMap = (Map) ecrErrorPartlist.get(j);
                    String partNumber = (String) errorpartMap.get("attribute[EnterpriseExtension.V_PartNumber]");
                    String rev = (String) errorpartMap.get(DomainConstants.SELECT_REVISION);
                    String LatestRev = (String) errorpartMap.get("LatestRev");
                    msgPartsb.append(partNumber).append("_").append(rev).append(",");
                }

                String msgPartStr = msgPartsb.toString();
                String errormsg = (String) EnoviaResourceBundle.getFrameworkStringResourceProperty(context, "emxFramework.ErrorECR.ErrorMsg", context.getLocale());
                String errorMsg = "";

//                String errormsg="";


                rejectECR(context, ecrid);
                ecrObj.setAttributeValue(context, "JFAllowableReview", "N");
                ecrObj.setAttributeValue(context, "JFReviewMessage", msgPartStr + errormsg);

                //发送邮件通知ECROwner
                sendEmailForErrorEcr(context, ecrid, ecrErrorPartlist);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void TestEmail(Context context,String[]args){
        try {
            String oldpartid="14585.59252.40830.1408";
            String ecrid="35845.4994.40830.44250";

            MapList ecrErrorPartlist=new MapList();

            StringList bul = JF_Util_mxJPO.basicBolistSel();
            //企业编码
            bul.add(JF_PLMConstants_mxJPO.SELECT_ATTR_V_PART_NUMBER);
            DomainObject oldpartObj = DomainObject.newInstance(context, oldpartid);
            Map partmap = oldpartObj.getInfo(context, bul);
            String LatestMajorid = JF_Util_mxJPO.getLatestMajorid_new(context, new String[]{oldpartid});
            log.info("LatestMajorid--->" + LatestMajorid);
            DomainObject LatestMajorObj = DomainObject.newInstance(context, LatestMajorid);
            String LatestRev = LatestMajorObj.getRevision(context);
            log.info("LatestRev--->" + LatestRev);
            partmap.put("LatestRev", LatestRev);

            ecrErrorPartlist.add(partmap);

            sendEmailForErrorEcr(context, ecrid, ecrErrorPartlist);

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void sendEmailForErrorEcr(Context context, String ecrid, MapList ecrErrorPartlist) throws Exception {

        //拿取邮件文档
        // 创建多部分消息体
        MimeMultipart multipart = new MimeMultipart(); // 默认混合模式
        //邮件内容的html模板部分
        BodyPart msgBodyPart = new MimeBodyPart();
        //邮件内容的html模板部分
        String html = JF_PublicMethodClass_mxJPO.getPageHTMLResourceFile(context, "ECRErrorPartEmails", "zh");
        org.jsoup.nodes.Document doc = Jsoup.parse(html);

        DomainObject EcrObj = DomainObject.newInstance(context, ecrid);
        String ecrOwner = EcrObj.getOwner(context).getName();


        String ecrName=EcrObj.getInfo(context,"name");


        log.info("name--->" + ecrName);

        DomainObject personObject = PersonUtil.getPersonObject(context, ecrOwner);
        String sendEmails = personObject.getAttributeValue(context, DomainObject.ATTRIBUTE_EMAIL_ADDRESS);
//        String sendEmails = "liuxg@tecwin.com";

        StringList listAttr = new StringList();
        listAttr.add(JF_PLMConstants_mxJPO.SELECT_ATTR_V_PART_NUMBER);
        listAttr.add(SELECT_REVISION);
        listAttr.add("LatestRev");

        doc.getElementById("projectTask").append(ecrName);

        //构建table
        for (int i = 0; i < ecrErrorPartlist.size(); i++) {
            Map errorPartmap = (Map) ecrErrorPartlist.get(i);
            JF_SendEmailUtils_mxJPO.writeTableData(context, "AllPartListTbody", errorPartmap, doc, listAttr);
        }
        String htmlContent = doc.toString();
        msgBodyPart.setContent(htmlContent, "text/html;charset=utf-8");//html代码部分
        multipart.addBodyPart(msgBodyPart);

        log.info("htmlContent---->"+htmlContent);

        Boolean aBoolean = JF_SendEmailUtils_mxJPO.SendEmail(context, sendEmails, "zh".equalsIgnoreCase("zh") ? "ECR撤回通知 " : "ECR Revocation notification", multipart);

    }

    public void rejectECR(Context context, String ecrid) throws Exception {
        //审核中的ECR如果有流程，获取当前流程
        String routeObjectId = (String) JPO.invoke(context, "JF_ECRProcess", new String[0], "getCurrentRoute", new String[]{ecrid}, String.class);

        DomainObject routeObj = DomainObject.newInstance(context, routeObjectId);
        StringList inboxTaskidList = routeObj.getInfoList(context, "to[Route Task].from.id");

        //获取任一审批中的任务，驳回
        String InboxTaskid = "";
        for (String inboxTaskid : inboxTaskidList) {
            InboxTask taskObj = (InboxTask) DomainObject.newInstance(context, inboxTaskid);
            String InboxCurrent = taskObj.getCurrentState(context).getName();
            log.info("InboxCurrent:{} taskObj.getOwner(context).getName() : dd:{}", InboxCurrent, taskObj.getOwner(context).getName(), taskObj.getInfo(context, SELECT_OWNER));
            if (!"Complete".equalsIgnoreCase(InboxCurrent)) {
                InboxTaskid = inboxTaskid;
                break;
            }
        }

        //理论上不会为空，为空流程已经走完。
        if (UIUtil.isNotNullAndNotEmpty(InboxTaskid)) {
            rejectECRReviewTask(context, InboxTaskid, routeObjectId);
        }
    }


    public void rejectECRReviewTask(Context context, String taskId, String routeId) throws Exception {
        DomainObject taskpro = DomainObject.newInstance(context, taskId);
        DomainObject routeobj = DomainObject.newInstance(context, routeId);

        String taskowner = taskpro.getOwner(context).getName();

        StringList selectObjStmt = new StringList();
        selectObjStmt.add(DomainObject.SELECT_ID);
        selectObjStmt.add(DomainObject.SELECT_NAME);
        selectObjStmt.add(DomainConstants.SELECT_CURRENT);
        StringList selectRelStmt = new StringList();
        selectRelStmt.add(DomainRelationship.SELECT_ID);
        selectRelStmt.add("attribute[Route Node ID]");
        selectRelStmt.add("attribute[Route Sequence]");
        selectRelStmt.add("attribute[Comments]");
        selectRelStmt.add("attribute[Route Instructions]");

        String objWhere = "name == '" + taskowner + "'";
//            String objWhere = DomainObject.EMPTY_STRING;
        String relWhere = DomainObject.EMPTY_STRING;
        //oldNodeInfos信息用于设置
        MapList NodeInfos = routeobj.getRelatedObjects(context, // context
                "Route Node", // relationship pattern
                "Person", // object pattern
                selectObjStmt, // object selects
                selectRelStmt, // relationship selects
                Boolean.FALSE, // to direction
                Boolean.TRUE, // from direction
                (short) 1, // recursion level
                objWhere, // object where clause
                relWhere, // relationship where clause
                0);
        log.info("taskowner--->" + taskowner);
        log.info("NodeInfos--->" + NodeInfos);
        if (NodeInfos.size() > 0) {
            Map TaskInfos = (Map) NodeInfos.get(0);
            String Comments = "No";
            String connId = String.valueOf(TaskInfos.get(DomainRelationship.SELECT_ID));

            HashMap inboxTaskMap = new HashMap();
            inboxTaskMap.put("Approval Status", "Reject");
            inboxTaskMap.put("Task Comments Needed", "No");
            inboxTaskMap.put("Comments", "The system automatically rejected it because it contained an old release version.");
            taskpro.setAttributeValues(context, inboxTaskMap);
            try {
                ContextUtil.pushContext(context);
                taskpro.promote(context);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                ContextUtil.popContext(context);
            }


            if (UIUtil.isNotNullAndNotEmpty(Comments)) {
                String MQLstmt = "modify bus " + taskId + " Comments '" + Comments + "'";
                String MQLret = MqlUtil.mqlCommand(context, MQLstmt, false);
                String MQLstmts = "modify connection " + connId + " Comments '" + Comments + "'";
                String MQLrets = MqlUtil.mqlCommand(context, MQLstmts, false);
            }
            String MQLstmtss = "modify connection " + connId + " 'Approval Status'" + " Reject";
            String MQLretss = MqlUtil.mqlCommand(context, MQLstmtss, false);

        }
    }


    public void TestEcr(Context context, String args[]) throws Exception {
        try {
            StringList idlist = new StringList();
            idlist.add("14585.59252.44744.25198");
            getReviewECRReject(context, idlist);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 克隆入库之后去掉库分类的Interface
     * @param context
     * @param args
     * @throws Exception
     */
    public void removeInterface(Context context,String[] args) throws Exception {
        String id = args[0];
        DomainObject vpmObject = DomainObject.newInstance(context, id);
        StringList list = vpmObject.getInfoList(context, "interface");
        for (int i = 0; i < list.size(); i++) {
            String interfaceName = list.get(i);
            boolean flag = BusinessObject.exists(context, interfaceName);
            log.info("interface--->:{} flag:{}" , interfaceName, flag);
            if (flag) {
                MqlUtil.mqlCommand(context, false,"mod bus "+id+" remove interface "+interfaceName,true);
            }
        }
    }

    /**
     * 零件升版后复制客户零件号/DB主关系关联的项目关系
     **
     * @param context 上下文
     * @param args args[0]旧版本零件ID，args[1]新版本零件ID
     * @return void
     * @throws Exception 项目关系复制失败
     * @author ljr
     * @date 2026/7/23
     */
    public void copyCustomerPartsDBProjectRelation(Context context, String[] args)  {
        // Trigger参数依次为旧版本零件ID和新版本零件ID。
        String oldPartId = args[0];
        String newPartId = args[1];
        // 项目不是主关系属性，而是通过fromrel挂在JFVPMReference2CustomerParts主关系上。
        String projectIdSelect = "frommid["
                + JF_PLMConstants_mxJPO.RELATIONSHIP_JFVPMREFERENCE2CUSTOMERPARTS2PROJECT
                + "].to.id";
        boolean isPush = false;
        try {
            // 升版后置Trigger需要在后台上下文中读取并创建二层关系，finally中恢复原上下文。
            ContextUtil.pushContext(context);
            isPush = true;

            StringList objectSelectList = JF_Util_mxJPO.basicBolistSel();
            StringList relSelectList = JF_Util_mxJPO.basicRellistSel();
            relSelectList.add(projectIdSelect);

            DomainObject oldPartObject = DomainObject.newInstance(context, oldPartId);
            DomainObject newPartObject = DomainObject.newInstance(context, newPartId);
            // 查询旧版本全部客户零件号/DB主关系，并在同一次查询中带出每条主关系关联的项目。
            MapList oldRelationList = oldPartObject.getRelatedObjects(context,
                    JF_PLMConstants_mxJPO.RELATIONSHIP_JFVPMREFERENCE2CUSTOMERPARTS,
                    JF_PLMConstants_mxJPO.TYPE_JFCUSTOMERPARTS,
                    objectSelectList,
                    relSelectList,
                    true,
                    false,
                    (short) 1,
                    DomainConstants.EMPTY_STRING,
                    DomainConstants.EMPTY_STRING,
                    0);
            if (oldRelationList.isEmpty()) {
                return;
            }

            // 主关系由revision=replicate自动复制，此处查询升版后已经生成的新主关系。
            MapList newRelationList = newPartObject.getRelatedObjects(context,
                    JF_PLMConstants_mxJPO.RELATIONSHIP_JFVPMREFERENCE2CUSTOMERPARTS,
                    JF_PLMConstants_mxJPO.TYPE_JFCUSTOMERPARTS,
                    objectSelectList,
                    relSelectList,
                    true,
                    false,
                    (short) 1,
                    DomainConstants.EMPTY_STRING,
                    DomainConstants.EMPTY_STRING,
                    0);
            Map newRelationByCustomerPartsId = new HashMap();
            // 以客户零件对象ID建立新主关系映射，避免按关系顺序或客户零件号属性错误匹配。
            for (int i = 0; i < newRelationList.size(); i++) {
                Map newRelationMap = (Map) newRelationList.get(i);
                String customerPartsId = UIUtil.getValue(newRelationMap, DomainConstants.SELECT_ID);
                if (UIUtil.isNotNullAndNotEmpty(customerPartsId)) {
                    newRelationByCustomerPartsId.put(customerPartsId, newRelationMap);
                }
            }

            for (int i = 0; i < oldRelationList.size(); i++) {
                Map oldRelationMap = (Map) oldRelationList.get(i);
                String customerPartsId = UIUtil.getValue(oldRelationMap, DomainConstants.SELECT_ID);
                Object oldProjectIdObject = oldRelationMap.get(projectIdSelect);
                StringList oldProjectIdList = new StringList();
                // ENOVIA多值select可能返回Collection，也可能在单值场景返回String，需要同时兼容。
                if (oldProjectIdObject instanceof Collection) {
                    oldProjectIdList.addAll((Collection) oldProjectIdObject);
                } else if (oldProjectIdObject != null && UIUtil.isNotNullAndNotEmpty(String.valueOf(oldProjectIdObject))) {
                    oldProjectIdList.add(String.valueOf(oldProjectIdObject));
                }
                if (oldProjectIdList.isEmpty()) {
                    continue;
                }

                // 同一个JFCustomerParts对象在旧、新版本上分别对应一条主关系，用对象ID定位新主关系。
                Map newRelationMap = (Map) newRelationByCustomerPartsId.get(customerPartsId);
                if (newRelationMap == null) {
                    continue;
                }
                String newMainRelId = UIUtil.getValue(newRelationMap, DomainRelationship.SELECT_ID);
                if (UIUtil.isNullOrEmpty(newMainRelId)) {
                    continue;
                }

                Object newProjectIdObject = newRelationMap.get(projectIdSelect);
                StringList newProjectIdList = new StringList();
                // 读取新主关系已有项目，用于保证Trigger重复执行时不会创建重复关系。
                if (newProjectIdObject instanceof Collection) {
                    newProjectIdList.addAll((Collection) newProjectIdObject);
                } else if (newProjectIdObject != null && UIUtil.isNotNullAndNotEmpty(String.valueOf(newProjectIdObject))) {
                    newProjectIdList.add(String.valueOf(newProjectIdObject));
                }

                for (int j = 0; j < oldProjectIdList.size(); j++) {
                    String projectId = oldProjectIdList.get(j);
                    if (UIUtil.isNullOrEmpty(projectId) || newProjectIdList.contains(projectId)) {
                        continue;
                    }
                    //20260723 update by ljr 零件升版：将旧主关系关联的项目复制到对应的新主关系；
                    MqlUtil.mqlCommand(context,
                            "add connection $1 fromrel $2 to $3 select $4 dump",
                            JF_PLMConstants_mxJPO.RELATIONSHIP_JFVPMREFERENCE2CUSTOMERPARTS2PROJECT,
                            newMainRelId,
                            projectId,
                            DomainConstants.SELECT_ID);
                    newProjectIdList.add(projectId);
                    log.info("copyCustomerPartsDBProjectRelation oldPartId:{} newPartId:{} customerPartsId:{} projectId:{}",
                            oldPartId, newPartId, customerPartsId, projectId);
                }
            }
        } catch (Exception e) {
            log.info(Arrays.toString(e.getStackTrace()));
        } finally {
            if (isPush) {
                try {
                    ContextUtil.popContext(context);
                } catch (FrameworkException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
