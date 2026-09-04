import com.dassault_systemes.system_cockpit.fl.preview.model.Interface;
import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.DomainRelationship;
import com.matrixone.apps.domain.util.*;
import com.matrixone.apps.framework.ui.ProgramCallable;
import com.matrixone.apps.framework.ui.UIUtil;
import com.matrixone.apps.library.LibraryUtil;
import com.matrixone.apps.program.ProgramCentralConstants;
import matrix.db.*;
import matrix.util.StringList;
import org.antlr.v4.runtime.misc.IntegerList;
import org.apache.poi.ss.usermodel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.matrixone.apps.domain.DomainConstants.*;
import static com.matrixone.apps.domain.DomainConstants.SELECT_CURRENT;

/**
 * @author CHENYAN
 * @version v1.0.0
 * 创建时间：2025/02/19 14:16
 * @description EBOM 业务处理相关
 */
public class JF_LastRevisionBOMService_mxJPO implements JF_PLMConstants_mxJPO{
    private static final Logger JF_LOGGER = LoggerFactory.getLogger(JF_LastRevisionBOMService_mxJPO.class);
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    /**
     * 导出多整椅BOM旧版附件，客户零件号和Direct buy按传入项目读取客户零件号/DB关系
     **
     * @param context
     * @param args 请求参数，包含ids、path、projectId
     * @return java.util.Map 导出的Workbook和文件名
     * @throws Exception
     * @author CHENYAN
     * @date 2026/7/22 00:00
     */
    public Map downLoadLastRevisionBOMInfoList(Context context, String[] args) throws Exception {
        Map res = new HashMap<>();
        Map argsMap = JPO.unpackArgs(args);
        String strPath = (String) argsMap.get("path");
        //项目id
        String strProjectId = (String) argsMap.get("projectId");
        //选中整椅id
        Set<String> idSet = (Set) argsMap.get("ids");
        DomainObject project = DomainObject.newInstance(context, strProjectId);
        String strProjectTitle = project.getInfo(context, SELECT_NAME);
        String strFullTemplatePath = JF_ECRService_mxJPO.getTemplatePath("lastRevisionBOMTemplate.xlsx", strPath);
        InputStream inputStream = new FileInputStream(strFullTemplatePath);
        Workbook workbook = WorkbookFactory.create(inputStream);
        StringList typeSelectList = JF_Util_mxJPO.basicBolistSel();
        typeSelectList.add(SELECT_PHYSICAL_ID);
        typeSelectList.add(SELECT_LEVEL);
        typeSelectList.add(SELECT_ATTR_V_PART_NUMBER);
        typeSelectList.add(SELECT_ATTR_JF_PartNameCN);
        typeSelectList.add(SELECT_ATTR_JF_PartNameEN);
        typeSelectList.add(SELECT_ATTR_SurfaceTreatment);
        typeSelectList.add(SELECT_ATTR_JF_Material);
        typeSelectList.add(SELECT_ATTR_JF_Weight);
        typeSelectList.add(SELECT_ATTR_JFLength);
        typeSelectList.add(SELECT_ATTR_JFWidth);
        typeSelectList.add(SELECT_ATTR_JF_WeightTarget);
        typeSelectList.add(SELECT_ATTR_JFHeight);
        typeSelectList.add(SELECT_ATTR_JF_ProcurementType);
        typeSelectList.add("from[Reference Document|to.attribute[JF_DocumentType]==Drawing].to.attribute[Title]");
        typeSelectList.add("from[Reference Document|to.attribute[JF_DocumentType]==Drawing].to.revision");
        typeSelectList.add("to[JFProject2RootPart|attribute[JF_BelongPart]==Y].from.description");
        typeSelectList.add(SELECT_ATTR_CustomerPartRevision);
        typeSelectList.add("attribute[PLMEntity.V_description]");
        typeSelectList.add(SELECT_ATTR_JFPartType);
        StringList relSelectList = JF_Util_mxJPO.basicRellistSel();
        relSelectList.add(SELECT_FROM_ID);
        relSelectList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JF_Dosage);

        JF_ExportEBOM_mxJPO jfExportEBOMMxJPO = new JF_ExportEBOM_mxJPO();

        //获取表格映射
        MapList EBOMMappingList = JF_PublicMethodClass_mxJPO.getJFCostChangeHistoryTemplateMapping(context, "EBOM_2");
        Sheet sheet = workbook.getSheetAt(2); // 获取工作表
        //获取表头最大列便于设置边框
        Row threeRow = sheet.getRow(3);
        short lastCellNum = threeRow.getLastCellNum();
        //创建单元格 style
        CellStyle basicCellStyle = JF_ExportEBOM_mxJPO.setCellStyleTitle(workbook, 2);
        //初始下标
        int writeRowIndex = 5 ;
        //去重一级件
        Map oneLevelMapInfo = new HashMap<String,Map>();
        //实际写入数据
        MapList actualDataList = new MapList();
        DomainObject BO = DomainObject.newInstance(context);
        for (String strGCId : idSet) {
            BO.setId(strGCId);
            Map GCInfo = BO.getInfo(context, typeSelectList);
            JF_LOGGER.info("GCInfo:{}",GCInfo);
            //设置PC列数据
           GCInfo.put(JF_PLMConstants_mxJPO.select_attr_Marketing_Name,GCInfo.get(JF_PLMConstants_mxJPO.SELECT_ATTR_JF_PartNameCN));
           String strPartNum = (String) GCInfo.get(JF_PLMConstants_mxJPO.SELECT_ATTR_V_PART_NUMBER);
           if (UIUtil.isNullOrEmpty(strPartNum)){
               strPartNum = (String) GCInfo.get(SELECT_NAME);
           }
           GCInfo.put(JF_PLMConstants_mxJPO.SELECT_PC_CONNECTION_PC,strPartNum);
            //展开一级件整椅件
            MapList partMapList = BO.getRelatedObjects(context,
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
            for (int i = 0; i < partMapList.size(); i++) {
                Map oneLevelMap = (Map) partMapList.get(i);
                String strOneLevelId = (String) oneLevelMap.get(SELECT_ID);
                String strAddDosage = (String) oneLevelMap.get(JF_PLMConstants_mxJPO.SELECT_ATTR_JF_Dosage);
                //一级件重复
                if (oneLevelMapInfo.containsKey(strOneLevelId)) {
                    //保存的一级件Map
                    Map tempOneLevelMap = (Map)oneLevelMapInfo.get(strOneLevelId);
                    //保存的上层GC的求和用量
                    String strSaveDosage = (String) tempOneLevelMap.get(strGCId);
                    //计算重复GC下一级件用量
                    if (UIUtil.isNotNullAndNotEmpty(strSaveDosage)){
                        if (UIUtil.isNotNullAndNotEmpty(strAddDosage)){
                            BigDecimal aBigDecimal = new BigDecimal(strAddDosage) ;
                            BigDecimal bBigDecimal = new BigDecimal(strSaveDosage) ;
                            BigDecimal sum = aBigDecimal.add(bBigDecimal);
                            strAddDosage = sum.toString();
                        }else {
                            strAddDosage = strSaveDosage;
                        }
                    }
                    tempOneLevelMap.put(strGCId,strAddDosage);
                }else {
                    oneLevelMap.put(strGCId,strAddDosage);
                    oneLevelMapInfo.put(strOneLevelId,oneLevelMap);
                }
            }
            //手动设置0级
            GCInfo.put(SELECT_LEVEL,"0");
            //设置本身用量为1
            GCInfo.put(strGCId,"1");
            actualDataList.add(GCInfo);
        }
        JF_LOGGER.info("actualDataList:{}",actualDataList);
//        JF_LOGGER.info("oneLevelMapInfo:{}",oneLevelMapInfo);
        //初始化表头
        JF_ExportEBOM_mxJPO.initConfigurationHeadInfo(workbook,sheet,actualDataList,EBOMMappingList);
//        JF_LOGGER.info("EBOMMappingList:{}",EBOMMappingList);
        //遍历一级件及其子集
        for ( Object oEntry :oneLevelMapInfo.entrySet()){
            Map.Entry entry = (Map.Entry)oEntry;
            Map oneLevelInfo = (Map) entry.getValue();
            String strOneLevelId = (String) entry.getKey();
            //添加一级件
            actualDataList.add(oneLevelInfo);
            //设置一级件id
            DomainObject oneLevel = DomainObject.newInstance(context,strOneLevelId);
            MapList partMapList = oneLevel.getRelatedObjects(context,
                    JF_PLMConstants_mxJPO.REL_Instance, // relationship pattern
                    JF_PLMConstants_mxJPO.TYPE_VPMReference,                                    // object pattern
                    typeSelectList,                            // object selects
                    relSelectList, // relationship selects
                    false,                                        // to direction
                    true,                                        // from direction
                    (short) 0,                                    // recursion level
                    "",                // object where clause
                    "",
                    (short) 0);
            //一级件下面的自件也需要添加GCIdkey保存用量
            //子件在具体的GC下面的 GCId
            StringList existGCIdList = getExistGCId(oneLevelInfo, idSet);
            int level = Integer.MAX_VALUE;
            Map<String, Object> sameBomeMap = new HashMap<>();
            //合并一级件下面的用量
            for (int i = 0; i < partMapList.size(); i++) {
                Map partInfoMap = (Map) partMapList.get(i);
                String strChildId = (String) partInfoMap.get(SELECT_ID);
                String strFromId = (String) partInfoMap.get(SELECT_FROM_ID);
                String strChildDosage = (String) partInfoMap.get(SELECT_ATTR_JF_Dosage);
                int childLevel = Integer.parseInt((String) partInfoMap.get(SELECT_LEVEL));
                if (childLevel > level) {
                    //childLevel大于level的,说明该层的父级已经重复，不用合并用量
                    continue;
                }
                level = Integer.MAX_VALUE;
                StringBuilder sb = new StringBuilder();
                sb.append(strFromId);
                sb.append(strChildId);
                String strUniqueKey = sb.toString();
                if (!sameBomeMap.containsKey(strUniqueKey)) {
                    sameBomeMap.put(strUniqueKey, partInfoMap);
                    //将用量同步到GC
                    String dosageStr = (String) partInfoMap.get(SELECT_ATTR_JF_Dosage);
                    for (int i1 = 0; i1 < existGCIdList.size(); i1++) {
                        String strGCId = existGCIdList.get(i1);
                        partInfoMap.put(strGCId,dosageStr);
                    }
                    String strPartLevel = UIUtil.getValue(partInfoMap, SELECT_LEVEL);
                    //标识一级件下面子件
                    partInfoMap.put("isSunPart",true);
                    JF_LOGGER.info("@@@@@@@@@@@@@@@@@@添加到集合的子集:{}",partInfoMap);
                    actualDataList.add(partInfoMap);
                } else {
                    level = childLevel;
                    Map bommap = (Map) sameBomeMap.get(strUniqueKey);
                    String dosageStr = (String) bommap.get(SELECT_ATTR_JF_Dosage);
                    if (UIUtil.isNullOrEmpty(dosageStr)){
                        dosageStr = "0" ;
                    }
                    if (UIUtil.isNullOrEmpty(strChildDosage)){
                        strChildDosage = "0" ;
                    }
                    JF_LOGGER.info("dosageStr:{}strChildDosage：{}",dosageStr,strChildDosage);
                    double dosage = Double.parseDouble(dosageStr) + Double.parseDouble(strChildDosage);
                    String strSumDosage = String.valueOf(dosage);
                    //将用量同步到GC
                    for (int i1 = 0; i1 < existGCIdList.size(); i1++) {
                        String strGCId = existGCIdList.get(i1);
                        bommap.put(strGCId,strSumDosage);
                    }
                    bommap.put(SELECT_ATTR_JF_Dosage, strSumDosage);
                }
            }
        }
        //图纸保存地址
        String strPrePath = JF_PublicMethodClass_mxJPO.getBasicUrl(context, new String[]{"DownloadVPM.Img.Path"});

        Map configMap = new HashMap<>();
        //设置多单椅EBOM导出调用标识
        configMap.put("EBOMExport","");
        JF_DR_mxJPO dr = new JF_DR_mxJPO();
        Map<String, Map> customerPartsDBInfoCache = new HashMap<>();
        //GC及其下面所有数据
        for (int i = 0; i < actualDataList.size(); i++) {
            Map partInfoMap = (Map) actualDataList.get(i);
            partInfoMap.put("ITEM",String.valueOf(i+1));
            String strPartId = (String) partInfoMap.get(SELECT_ID);
            if (!customerPartsDBInfoCache.containsKey(strPartId)) {
                customerPartsDBInfoCache.put(strPartId, dr.getDRCustomerPartsDBRelationInfo(context, strPartId, strProjectId));
            }
            Map customerPartsDBInfo = customerPartsDBInfoCache.get(strPartId);
            partInfoMap.put(SELECT_ATTR_CustomerPartNumber, UIUtil.getValue(customerPartsDBInfo, Select_Attr_JFCustomerPartNumber));
            //20260727 update by ljr BOM导出时，未维护项目对应客户零件/DB信息的零件按non-DB输出；
            partInfoMap.put(SELECT_ATTR_JFDIRECT_BUY,
                    JF_Util_mxJPO.getPartDirectBuyByProjectWithNonDBDefault(
                            context,
                            strPartId,
                            strProjectId));
            String strLevel = (String) partInfoMap.get(SELECT_LEVEL);
            //子件层级+1
            if (partInfoMap.containsKey("isSunPart")){
                int iActualLevel = Integer.parseInt(strLevel) + 1;
                strLevel = String.valueOf(iActualLevel);
            }
            // add by chenyan 实际重量为空时去参考重量 2025/07/30
             String strWeightTarget = (String) partInfoMap.get(JF_PLMConstants_mxJPO.SELECT_ATTR_JF_WeightTarget);
            if (UIUtil.isNullOrEmpty(strWeightTarget)){
                partInfoMap.put(SELECT_ATTR_JF_WeightTarget,partInfoMap.get(SELECT_ATTR_JF_Weight));
            }
            partInfoMap.put(JF_PublicMethodClass_mxJPO.buildStringInStrings(SELECT_LEVEL,"_",strLevel),strLevel);
            BO.setId(strPartId);
            String strPartPhyId = (String) partInfoMap.get(DomainRelationship.SELECT_PHYSICAL_ID);
            //获取缩略图
            String strImgId = jfExportEBOMMxJPO.getDownLoadPictureByPartId(context, strPartPhyId, strPrePath);
            partInfoMap.put("picture",strImgId);
            //没有取号时设置为name
            if (UIUtil.isNullOrEmpty((String) partInfoMap.get(JF_PLMConstants_mxJPO.SELECT_ATTR_V_PART_NUMBER))){
                partInfoMap.put(JF_PLMConstants_mxJPO.SELECT_ATTR_V_PART_NUMBER,UIUtil.getValue(partInfoMap,SELECT_NAME));
            }
            //获取图纸信息
            Map drawInfoMap = jfExportEBOMMxJPO.getDrawInfoByPart(context, BO,partInfoMap);
            //将图纸信息添加到数据集合中
            partInfoMap.putAll(drawInfoMap);
            //PC已在前面计算数量
            partInfoMap.put("PCDescription","");
            JF_LOGGER.info("partInfoMap:{}",partInfoMap);
           jfExportEBOMMxJPO.writeDataInExcelRow(context,workbook,sheet,writeRowIndex,EBOMMappingList,partInfoMap,basicCellStyle,lastCellNum,true,configMap);
            writeRowIndex ++;
        }
        //获取当前时间
        LocalDateTime now = LocalDateTime.now();
        // 将 LocalDateTime 对象转换成指定格式的字符串
        String strFormattedDate = now.format(formatter);
        res.put("file",workbook);
        res.put("fileName",JF_PublicMethodClass_mxJPO.buildStringInStrings(strProjectTitle,"_",strFormattedDate,".xlsx"));
        return res;
    }

    /**
     * 根据勾选零件导出BOM报表，勾选根件在本次导出中按供货件处理
     * @param context
     * @param args 请求参数，包含ids、path、projectId、selectedSupplyPartIds、exportObjectName，其中projectId为颜色分组和颜色风格取值的项目上下文
     * @return java.util.Map 导出的Workbook和文件名
     * @throws Exception
     * @author Codex
     * @date 2026/7/7 00:00
     */
    public Map downLoadSelectedPartBOMInfoList(Context context, String[] args) throws Exception {
        return downLoadLastRevisionBOMInfoListSecond(context, args);
    }

    /**
     * 导出多整椅BOM，一级件按PartType.order配置排序，客户零件号/DB关系统一按传入项目读取
     **
     * @param context
     * @param args 请求参数，包含ids、path、projectId、mode、selectedSupplyPartIds
     * @return java.util.Map 导出的Workbook和文件名
     * @throws Exception
     * @author LIUJR
     * @date 2026/7/22 00:00
     */
    public Map downLoadLastRevisionBOMInfoListSecond(Context context, String[] args) throws Exception {
        Map res = new HashMap<>();
        try {
            ContextUtil.pushContext(context);
            Map argsMap = JPO.unpackArgs(args);
            String strPath = (String) argsMap.get("path");
            //项目id
            String strProjectId = (String) argsMap.get("projectId");
            //20260813 update by caipan 指定导出入口按当前流程对象编号生成文件名。
            String strExportObjectName = (String) argsMap.get("exportObjectName");
            //选中整椅id
            Set<String> idSet = (Set) argsMap.get("ids");
            Set<String> selectedSupplyPartIds = (Set) argsMap.get("selectedSupplyPartIds");
            if (selectedSupplyPartIds == null) {
                selectedSupplyPartIds = new HashSet<>();
            }
            //项目名称
            DomainObject project = DomainObject.newInstance(context, strProjectId);
            String strProjectTitle = project.getInfo(context, SELECT_NAME);
            //BUS查询信息- 以下信息是根据XML配置添加,请勿随意移除
            StringList typeSelectList = JF_Util_mxJPO.basicBolistSel();
            typeSelectList.add(SELECT_PHYSICAL_ID);
            typeSelectList.add(SELECT_LEVEL);
            typeSelectList.add(SELECT_ATTR_V_PART_NUMBER);
            typeSelectList.add(SELECT_CURRENT);
            typeSelectList.add(SELECT_ATTR_JF_PartNameCN);
            typeSelectList.add(SELECT_ATTR_JF_PartNameEN);
            typeSelectList.add(SELECT_ATTR_JFPartType);
            typeSelectList.add(SELECT_ATTR_JF_Detail_CN);
            typeSelectList.add("to[JFProject2RootPart|attribute[JF_BelongPart]==Y].from.description");
            typeSelectList.add("to[JFProject2RootPart|attribute[JF_BelongPart]==Y].from.name");
            typeSelectList.add(SELECT_ATTR_CustomerDrawingNumber);
            typeSelectList.add(SELECT_ATTR_Supplier);
            typeSelectList.add(SELECT_ATTR_SupplierPartNumber);
            typeSelectList.add(SELECT_ATTR_JF_ProcurementType);
            typeSelectList.add(SELECT_ATTR_JF_Material);
            typeSelectList.add(SELECT_ATTR_JF_MaterialStar);
            typeSelectList.add(SELECT_ATTR_JF_MaterialDensity);
            typeSelectList.add(SELECT_ATTR_SurfaceTreatment);
            typeSelectList.add(SELECT_ATTR_SurfaceTreatmentGrade);
            typeSelectList.add(SELECT_ATTR_SurfaceTreatmentArea);
            typeSelectList.add(SELECT_ATTR_JFLength);
            typeSelectList.add(SELECT_ATTR_JFWidth);
            typeSelectList.add(SELECT_ATTR_JFHeight);
            typeSelectList.add(SELECT_ATTR_JFThickness);
            typeSelectList.add(SELECT_ATTR_Diam);
            typeSelectList.add(SELECT_ATTR_InsideDiameter);
            typeSelectList.add(SELECT_ATTR_JF_WeightTarget);
            typeSelectList.add(SELECT_ATTR_JF_Weight);
            typeSelectList.add("attribute[PLMEntity.V_description]");
            typeSelectList.add("attribute[Function]");
            //REL 查询信息- 以下信息是根据XML配置添加,请勿随意移除
            StringList relSelectList = JF_Util_mxJPO.basicRellistSel();
            relSelectList.add(SELECT_FROM_ID);
            relSelectList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JF_Dosage);
            relSelectList.add(SELECT_Attr_SynchroEBOMCAD);
            //获取XML配置的单元格列映射，获取每个内容放置的列
            MapList EBOMMappingList = JF_PublicMethodClass_mxJPO.getJFCostChangeHistoryTemplateMapping(context, "EBOM_Export");
            //拿取导出模板
            String strFullTemplatePath = JF_ECRService_mxJPO.getTemplatePath("lastRevisionBOMTemplateSecond.xlsx", strPath);
            InputStream inputStream = new FileInputStream(strFullTemplatePath);
            Workbook workbook = WorkbookFactory.create(inputStream);
            JF_LOGGER.info("strFullTemplatePath:{}", strFullTemplatePath);
            //获取工作表index和写入数据行数XML 配置 add by ljr 20251114
            MapList EBOMConfList = JF_PublicMethodClass_mxJPO.getJFCostChangeHistoryTemplateMapping(context, "EBOM_Export_CONF");
            HashMap<String, Object> EBOMConfMap = new HashMap<>();
            for (int i = 0; i < EBOMConfList.size(); i++) {
                Map map = (Map) EBOMConfList.get(i);
                String id = UIUtil.getValue(map, SELECT_ID);
                String value = String.valueOf(map.get("value"));
                if (value.length() > 2) {
                    EBOMConfMap.put(id, value);
                } else {
                    EBOMConfMap.put(id, Integer.valueOf(value));
                }
            }
            // 获取工作表
//            Sheet sheet = workbook.getSheetAt((Integer) EBOMConfMap.get("sheetIndex"));
            Sheet sheet = workbook.getSheet((String) EBOMConfMap.get("sheetIndexName"));
            //写入数据初始下标
            int writeRowIndex = (Integer)EBOMConfMap.get("writeRowIndex") ;
            //拿取项目中的最新发布颜色矩阵中的颜色分组 及其 颜色风格 内部颜色码  客户颜色码 add by ljr 20251114
            Map colorMatrixGroupAndStyleMap = JF_ColorPart_mxJPO.getColorMatrixGroupAndStyle(context, strProjectId);
            JF_LOGGER.info("拿取项目中的最新发布颜色矩阵中的颜色分组 及其 颜色风格 内部颜色码  客户颜色码:{}", colorMatrixGroupAndStyleMap);
            StringList styleList = new StringList();
            Map<String, Map<String, Map<String, String>>> colorGroupCodeMap = new HashMap<>();
            if (!colorMatrixGroupAndStyleMap.isEmpty()) {
                styleList = (StringList) colorMatrixGroupAndStyleMap.get("styleList");
                colorGroupCodeMap = (Map) colorMatrixGroupAndStyleMap.get("colorGroupCodeMap");
            }
            //写入颜色风格头信息 add by ljr 20251114
            JF_ExportEBOM_mxJPO.initColorStyleHeadInfo(workbook, context,sheet,styleList,EBOMMappingList, EBOMConfMap);
            //调整列宽
            IntegerList str16List = (IntegerList) EBOMConfMap.get("str16List");
            IntegerList str20List = (IntegerList) EBOMConfMap.get("str20List");
            IntegerList str40List = (IntegerList) EBOMConfMap.get("str40List");
            Integer lastColIndex = (Integer) EBOMConfMap.get("lastColIndex");
            JF_LOGGER.info("xml的MapList修改后:{}",EBOMMappingList);
            JF_LOGGER.info("EBOMConfMap:{}",EBOMConfMap);
            //去重一级件Map
            Map oneLevelMapInfo = new HashMap<String,Map>();
            //实际写入数据List
            MapList actualDataList = new MapList();
            //开始遍历
            DomainObject partObject = DomainObject.newInstance(context);
            List relGroupIdList = null;
            Map groupMap = new HashMap();
            JF_ExportEBOM_mxJPO jfExportEBOMMxJPO = new JF_ExportEBOM_mxJPO();
            JF_VPMReferenceEBOM_mxJPO ebom = new JF_VPMReferenceEBOM_mxJPO();
            String colorGroup = EMPTY_STRING;
            boolean flag = Boolean.TRUE;
            //获取项目中所有关联零件的颜色分组信息  用于后续拿取零件的颜色分组  add by ljr 20251114
            MapList mapList = project.getRelatedObjects(context,
                    "JFProject2ColorGroup",
                    "VPMReference",
                    new StringList(SELECT_ID),
                    StringList.create("attribute[JF_ColorGroupName]", "attribute[JF_ColorMatrixName]"),
                    false,
                    true,
                    (short) 1,
                    "",
                    "",
                    0);
            Map mapListGroupingMap = JF_NewECRService_mxJPO.getMapListGroupingMap(context, mapList, SELECT_ID);
            JF_LOGGER.info("@@@@@@@@@@@@@@@@@@mapListGroupingMap:{}",mapListGroupingMap);
            String strCAD = EMPTY_STRING;
            for (String strGCId : idSet) {
                partObject.setId(strGCId);
                Map GCInfo = partObject.getInfo(context, typeSelectList);

                String strChildName = (String) GCInfo.get(SELECT_ATTR_V_PART_NUMBER);
                //设置PC列数据
                GCInfo.put(JF_PLMConstants_mxJPO.select_attr_Marketing_Name,GCInfo.get(JF_PLMConstants_mxJPO.SELECT_ATTR_JF_PartNameCN));
                String strPartNum = (String) GCInfo.get(JF_PLMConstants_mxJPO.SELECT_ATTR_V_PART_NUMBER);
                if (UIUtil.isNullOrEmpty(strPartNum)){
                    strPartNum = (String) GCInfo.get(SELECT_NAME);
                }
                GCInfo.put(JF_PLMConstants_mxJPO.SELECT_PC_CONNECTION_PC,strPartNum);
                //颜色分组
                if (mapListGroupingMap.containsKey(strGCId)) {
                    relGroupIdList= (List) mapListGroupingMap.get(strGCId);
                    groupMap = (Map) relGroupIdList.get(0);
                    colorGroup = UIUtil.getValue(groupMap, "attribute[JF_ColorGroupName]");
                } else {
                    //没有设置的情况,根据当前零件类型来确定
                    flag = selectedSupplyPartIds.contains(strGCId) || ebom.getPartIsSupplyPart(context, new String[]{strGCId,strProjectId});
                    colorGroup = flag ? "UA" : "NA";
                }
                GCInfo.put("attribute[JF_VPMInstance.JF_ColorGroup]", colorGroup);
                //手动设置0级
                //改动点3: 装配层级：增加“-1”，用于标记GX虚拟装配； add by ljr 20251114
                if (strChildName.startsWith("GX")) {
                    GCInfo.put(SELECT_LEVEL,"-1");
                } else {
                    GCInfo.put(SELECT_LEVEL, "0");
                }
                //展开一级件整椅件
                MapList partMapList = partObject.getRelatedObjects(context,
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
                for (int i = 0; i < partMapList.size(); i++) {
                    Map oneLevelMap = (Map) partMapList.get(i);
                    strCAD = (String) oneLevelMap.get(SELECT_Attr_SynchroEBOMCAD);
                    if ("FALSE".equalsIgnoreCase(strCAD)) {
                        //改动点1: 仅CAD件不导出到EBOM清单中   20251112 ljr
                        continue;
                    }
                    String strOneName = (String) oneLevelMap.get(SELECT_ATTR_V_PART_NUMBER);
                    String strOneLevelId = (String) oneLevelMap.get(SELECT_ID);
                    String strAddDosage = (String) oneLevelMap.get(JF_PLMConstants_mxJPO.SELECT_ATTR_JF_Dosage);
                    //颜色分组
                    if (mapListGroupingMap.containsKey(strOneLevelId)) {
                        relGroupIdList= (List) mapListGroupingMap.get(strOneLevelId);
                        groupMap = (Map) relGroupIdList.get(0);
                        colorGroup = UIUtil.getValue(groupMap, "attribute[JF_ColorGroupName]");
                    } else {
                        //没有设置的情况,根据当前零件类型来确定
                        flag = selectedSupplyPartIds.contains(strOneLevelId) || ebom.getPartIsSupplyPart(context, new String[]{strOneLevelId,strProjectId});
                        colorGroup = flag ? "UA" : "NA";
                    }
                    oneLevelMap.put("attribute[JF_VPMInstance.JF_ColorGroup]", colorGroup);
                    String key = strOneName + "_" + colorGroup;
                    if (strChildName.startsWith("GX")) {
                        oneLevelMap.put(SELECT_LEVEL,"0");
                    } else {
                        oneLevelMap.put(SELECT_LEVEL, "1");
                    }
                    //一级件重复
                    if (oneLevelMapInfo.containsKey(key)) {
                        //保存的一级件Map
                        Map tempOneLevelMap = (Map)oneLevelMapInfo.get(key);
                        String strColorGroup = UIUtil.getValue(tempOneLevelMap, "attribute[JF_VPMInstance.JF_ColorGroup]");
                        //当是 同一个零件 或者变形件和原件为同一个颜色分组的时候 合并
                        if (strOneLevelId.equalsIgnoreCase(UIUtil.getValue(tempOneLevelMap, SELECT_ID)) || strColorGroup.equalsIgnoreCase(colorGroup)) {
                            //是同一个件的情况
                            //保存的上层GC的求和用量
                            String strSaveDosage = (String) tempOneLevelMap.get(strGCId);
                            //计算重复GC下一级件用量
                            if (UIUtil.isNotNullAndNotEmpty(strSaveDosage)) {
                                if (UIUtil.isNotNullAndNotEmpty(strAddDosage)) {
                                    BigDecimal aBigDecimal = new BigDecimal(strAddDosage);
                                    BigDecimal bBigDecimal = new BigDecimal(strSaveDosage);
                                    BigDecimal sum = aBigDecimal.add(bBigDecimal);
                                    strAddDosage = sum.toString();
                                } else {
                                    strAddDosage = strSaveDosage;
                                }
                            }
                            tempOneLevelMap.put(strGCId, strAddDosage);
                        } else {
                            //是变形件不同的情况
                            oneLevelMap.put(strGCId,strAddDosage);
                            oneLevelMapInfo.put(key,oneLevelMap);
                        }
                    }else {
                        oneLevelMap.put(strGCId,strAddDosage);
                        oneLevelMapInfo.put(key,oneLevelMap);
                    }
                }
                //设置本身用量为1
                GCInfo.put(strGCId,"1");
                actualDataList.add(GCInfo);
            }
            JF_LOGGER.info("actualDataList:{}",actualDataList);
            JF_LOGGER.info("oneLevelMapInfo:{}",oneLevelMapInfo);
            //初始化表头  配置描述表头写入
            JF_ExportEBOM_mxJPO.initConfigurationHeadInfoSecond(workbook,sheet,actualDataList,EBOMMappingList);
            //新增列宽列
            if (actualDataList.size() > 1 ) {
                str40List.addAll(IntStream.rangeClosed(lastColIndex + 1, lastColIndex + actualDataList.size())
                        .boxed()
                        .collect(Collectors.toList()));
            }
            //20260901 update by caipan 一级件按PartType.order配置排序，一级件以下子级保持原有顺序
            String strPartTypeOrder = JF_PublicMethodClass_mxJPO.getBasicUrl(context, new String[]{"PartType.order"});
            Map<String, Integer> partTypeOrderMap = new HashMap<>();
            if (UIUtil.isNotNullAndNotEmpty(strPartTypeOrder)) {
                String[] partTypeOrderArray = strPartTypeOrder.split(",");
                for (int i = 0; i < partTypeOrderArray.length; i++) {
                    String partType = partTypeOrderArray[i].trim();
                    if (UIUtil.isNotNullAndNotEmpty(partType) && !partTypeOrderMap.containsKey(partType)) {
                        partTypeOrderMap.put(partType, i);
                    }
                }
            }
            List<Map> oneLevelInfoList = new ArrayList<>(oneLevelMapInfo.values());
            oneLevelInfoList.sort(Comparator.comparingInt(oneLevelInfo -> {
                String partType = UIUtil.getValue(oneLevelInfo, SELECT_ATTR_JFPartType);
                Integer order = partTypeOrderMap.get(partType);
                return order == null ? Integer.MAX_VALUE : order;
            }));
            //遍历一级件及其子集
            JF_LOGGER.info("!!!!!!!!!!!!!!!!!!!遍历一级件及其子集");
            for (Map oneLevelInfo : oneLevelInfoList){
                String strLevel = (String) oneLevelInfo.get(SELECT_LEVEL);
                String strName = (String) oneLevelInfo.get(SELECT_ATTR_V_PART_NUMBER);
//                String strOneLevelId = (String) entry.getKey();
                String strOneLevelId = (String) oneLevelInfo.get(SELECT_ID);
                //添加一级件
                actualDataList.add(oneLevelInfo);
                //设置一级件id
                DomainObject oneLevel = DomainObject.newInstance(context,strOneLevelId);
                MapList partMapList = new MapList();
                //过滤出结构中不是仅CAD件的零件
                getNotCADPartList(context, oneLevel, typeSelectList, relSelectList, Integer.valueOf(strLevel), partMapList);
                JF_LOGGER.info("strName:{}, partMapList:{}", strName, partMapList);
                //一级件下面的自件也需要添加GCIdkey保存用量
                //子件在具体的GC下面的 GCId
                StringList existGCIdList = getExistGCId(oneLevelInfo, idSet);
                int level = Integer.MAX_VALUE;
                Map<String, Object> sameBomeMap = new HashMap<>();
                //合并一级件下面的用量
                for (int i = 0; i < partMapList.size(); i++) {
                    Map partInfoMap = (Map) partMapList.get(i);
                    String strChildId = (String) partInfoMap.get(SELECT_ID);
                    String strChildName = (String) partInfoMap.get(SELECT_ATTR_V_PART_NUMBER);
                    String strFromId = (String) partInfoMap.get(SELECT_FROM_ID);
                    String strChildDosage = (String) partInfoMap.get(SELECT_ATTR_JF_Dosage);
                    int childLevel = Integer.parseInt((String) partInfoMap.get(SELECT_LEVEL));
                    if (childLevel > level) {
                        //childLevel大于level的,说明该层的父级已经重复，不用合并用量
                        continue;
                    }
                    level = Integer.MAX_VALUE;
                    StringBuilder sb = new StringBuilder();
                    //获取颜色分组
                    if (mapListGroupingMap.containsKey(strChildId)) {
                        relGroupIdList= (List) mapListGroupingMap.get(strChildId);
                        groupMap = (Map) relGroupIdList.get(0);
                        colorGroup = UIUtil.getValue(groupMap, "attribute[JF_ColorGroupName]");
                    } else {
                        //没有设置的情况,根据当前零件类型来确定
                        flag = selectedSupplyPartIds.contains(strChildId) || ebom.getPartIsSupplyPart(context, new String[]{strChildId,strProjectId});
                        colorGroup = flag ? "UA" : "NA";
                    }
                    partInfoMap.put("attribute[JF_VPMInstance.JF_ColorGroup]", colorGroup);
                    //改动点2： 变形件导出为原件零件号（同一个层级下如果同时存在变形件和原件，数量需要合并计算）
                    sb.append(strFromId);
                    sb.append(strChildName);//sb.append(strChildId); 原代码
                    sb.append(colorGroup);
                    String strUniqueKey = sb.toString();
                    if (!sameBomeMap.containsKey(strUniqueKey)) {
                        sameBomeMap.put(strUniqueKey, partInfoMap);
                        //将用量同步到GC
                        String dosageStr = (String) partInfoMap.get(SELECT_ATTR_JF_Dosage);
                        for (int i1 = 0; i1 < existGCIdList.size(); i1++) {
                            String strGCId = existGCIdList.get(i1);
                            partInfoMap.put(strGCId,dosageStr);
                        }
                        String strPartLevel = UIUtil.getValue(partInfoMap, SELECT_LEVEL);
                        //标识一级件下面子件
                        partInfoMap.put("isSunPart",true);
                        actualDataList.add(partInfoMap);
                    } else {
                        level = childLevel;
                        Map bommap = (Map) sameBomeMap.get(strUniqueKey);
                        String dosageStr = (String) bommap.get(SELECT_ATTR_JF_Dosage);
                        if (UIUtil.isNullOrEmpty(dosageStr)) {
                            dosageStr = "0";
                        }
                        if (UIUtil.isNullOrEmpty(strChildDosage)) {
                            strChildDosage = "0";
                        }
                        double dosage = Double.parseDouble(dosageStr) + Double.parseDouble(strChildDosage);
                        String strSumDosage = String.valueOf(dosage);
                        //将用量同步到GC
                        for (int i1 = 0; i1 < existGCIdList.size(); i1++) {
                            String strGCId = existGCIdList.get(i1);
                            bommap.put(strGCId, strSumDosage);
                        }
                        bommap.put(SELECT_ATTR_JF_Dosage, strSumDosage);
                    }
                }
            }
            JF_LOGGER.info("!!!!!!!!!!!!!!!!!!!遍历一级件及其子集完成");
            //图纸保存地址
            String strPrePath = JF_PublicMethodClass_mxJPO.getBasicUrl(context, new String[]{"DownloadVPM.Img.Path"});
            JF_LOGGER.info("图纸保存地址");
            //获取拿取库分类属性的xml配置
            String classAttr = (String) EBOMConfMap.get("classAttr");
            StringList classAttrList = StringList.create(classAttr.split(","));
//            MapList EBOMAttrList = JF_PublicMethodClass_mxJPO.getJFCostChangeHistoryTemplateMapping(context, "EBOM_Class_Attr");
//            Map EBOMAttrMap = JF_NewECRService_mxJPO.getMapListGroupingMap(context, EBOMMappingList, SELECT_ID);
            Map configMap = new HashMap<>();
            //设置多单椅EBOM导出调用标识
            configMap.put("EBOMExport","");
            JF_LOGGER.info("创建风格！！！！！！！！！！");
            DomainObject classObject = DomainObject.newInstance(context);
            String strLabel = EMPTY_STRING;
            String nlsAttr = EMPTY_STRING;
            HashMap<String, String> nlsMap = new HashMap<>();
            String strLevel = EMPTY_STRING;
            String strPartId = EMPTY_STRING;
            String strChildName = EMPTY_STRING;
            String strPartPhyId = EMPTY_STRING;
            Map partInfoMap = new HashMap();
            //创建单元格风格
            CellStyle basicCellStyle = JF_ExportEBOM_mxJPO.setCellStyleTitle(workbook, 2);
            CellStyle wrapTextCellStyle = JF_ExportEBOM_mxJPO.setCellStyleTitle(workbook, 3);
            //获取表头最大列便于设置边框
            Row threeRow = sheet.getRow((Integer)EBOMConfMap.get("titleIndex"));
            short lastCellNum = threeRow.getLastCellNum();
            //GC及其下面所有数据
            CellStyle leftStyle = JF_ESO_mxJPO.getLeftBorderStyle(workbook);
            leftStyle.setWrapText(false);
            JF_FasteningPiece_mxJPO jfFasteningPieceMxJPO = new JF_FasteningPiece_mxJPO();
            //单元格填充红色
            // 创建样式
            CellStyle redStyle = workbook.createCellStyle();
            // 设置填充颜色为红色
            redStyle.setFillForegroundColor(IndexedColors.RED.getIndex()); // 使用预定义红色
            redStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);     // 实心填充
            redStyle.setBorderTop(BorderStyle.THIN);
            redStyle.setBorderBottom(BorderStyle.THIN);
            redStyle.setBorderLeft(BorderStyle.THIN);
            redStyle.setBorderRight(BorderStyle.THIN);
            JF_LOGGER.info("!!!!!!!!!!!!!!!!!!!");
            JF_LOGGER.info("actualDataList:{}", actualDataList);
            JF_DR_mxJPO dr = new JF_DR_mxJPO();
            Map<String, Map> customerPartsDBInfoCache = new HashMap<>();
            for (int i = 0; i < actualDataList.size(); i++) {
                partInfoMap = (Map) actualDataList.get(i);
                partInfoMap.put("ITEM",String.valueOf(i+1));
                strPartId = (String) partInfoMap.get(SELECT_ID);
                //20260903 update by caipan EBOM入口传入根节点归属项目，全部零件按该项目读取客户零件号和DirectBuy
                String strCustomerPartsProjectId = strProjectId;
                String strCustomerPartsDBCacheKey = strPartId + "|" + strCustomerPartsProjectId;
                if (!customerPartsDBInfoCache.containsKey(strCustomerPartsDBCacheKey)) {
                    customerPartsDBInfoCache.put(strCustomerPartsDBCacheKey, dr.getDRCustomerPartsDBRelationInfo(context, strPartId, strCustomerPartsProjectId));
                }
                Map customerPartsDBInfo = customerPartsDBInfoCache.get(strCustomerPartsDBCacheKey);
                partInfoMap.put(SELECT_ATTR_CustomerPartNumber, UIUtil.getValue(customerPartsDBInfo, Select_Attr_JFCustomerPartNumber));
                //20260727 update by ljr BOM导出时，未维护项目对应客户零件/DB信息的零件按non-DB输出；
                partInfoMap.put(SELECT_ATTR_JFDIRECT_BUY,
                        JF_Util_mxJPO.getPartDirectBuyByProjectWithNonDBDefault(
                                context,
                                strPartId,
                                strCustomerPartsProjectId));
                partObject.setId(strPartId);
                strLevel = (String) partInfoMap.get(SELECT_LEVEL);
                strChildName = (String) partInfoMap.get(SELECT_ATTR_V_PART_NUMBER);
                strPartPhyId = (String) partInfoMap.get(DomainRelationship.SELECT_PHYSICAL_ID);
                //子件层级+1
                if (partInfoMap.containsKey("isSunPart")){
                    int iActualLevel = Integer.parseInt(strLevel) + 1;
                    strLevel = String.valueOf(iActualLevel);
                }
                partInfoMap.put(JF_PublicMethodClass_mxJPO.buildStringInStrings(SELECT_LEVEL,"_",strLevel),strLevel);
                // add by chenyan 实际重量为空时去参考重量 2025/07/30
                String strWeightTarget = (String) partInfoMap.get(JF_PLMConstants_mxJPO.SELECT_ATTR_JF_WeightTarget);
                if (UIUtil.isNullOrEmpty(strWeightTarget)){
                    partInfoMap.put(SELECT_ATTR_JF_WeightTarget,partInfoMap.get(SELECT_ATTR_JF_Weight));
                }
                //获取缩略图
                String strImgId = jfExportEBOMMxJPO.getDownLoadPictureByPartId(context, strPartPhyId, strPrePath);
                partInfoMap.put("picture",strImgId);
                //零件号码 - 没有取号时设置为name
                if (UIUtil.isNullOrEmpty((String) partInfoMap.get(JF_PLMConstants_mxJPO.SELECT_ATTR_V_PART_NUMBER))){
                    partInfoMap.put(JF_PLMConstants_mxJPO.SELECT_ATTR_V_PART_NUMBER,UIUtil.getValue(partInfoMap,SELECT_NAME));
                }
                //获取图纸信息 新增了2D状态列  add by ljr 20251114
                //改动点7：2D状态：3中对应的图纸状态，包括“工作中”、“已冻结”、“已发布”； 图纸版本：3D模型关联的2D图纸的版本，包括关联的文档
                JF_LOGGER.info("strChildName :{}, partInfoMap:{}",strChildName, partInfoMap);
                Map drawInfoMap = jfExportEBOMMxJPO.getDrawInfoByPartSecond(context, partObject,partInfoMap);
                JF_LOGGER.info("strChildName :{}, drawInfoMap:{}",strChildName, drawInfoMap);
                //将图纸信息添加到数据集合中
                partInfoMap.putAll(drawInfoMap);
                //PC已在前面计算数量
                partInfoMap.put("PCDescription","");
                //零件与项目的关系上的颜色分组信息  add by ljr 20251114
                if (mapListGroupingMap.containsKey(strPartId)) {
                    relGroupIdList= (List) mapListGroupingMap.get(strPartId);
                    groupMap = (Map) relGroupIdList.get(0);
                    colorGroup = UIUtil.getValue(groupMap, "attribute[JF_ColorGroupName]");
                } else {
                    //没有设置的情况,根据当前零件类型来确定
                    flag = selectedSupplyPartIds.contains(strPartId) || ebom.getPartIsSupplyPart(context, new String[]{strPartId,strProjectId});
                    colorGroup = flag ? "UA" : "NA";
                }
                JF_LOGGER.info("颜色信息： strChildName:{};colorGroup{}",strChildName, colorGroup);
                partInfoMap.put("attribute[JF_VPMInstance.JF_ColorGroup]", colorGroup);
                if (!colorGroupCodeMap.containsKey(colorGroup)) {
                    partInfoMap.put("JF_CustormColorCode", new HashMap<String, Map<String, String>>());
                    partInfoMap.put("JF_InternalColorCode", new HashMap<String, Map<String, String>>());
                } else {
                    Map<String, Map<String, String>> stringMapMap = colorGroupCodeMap.get(colorGroup);
                    partInfoMap.putAll(stringMapMap);
                }
                //产品分类属性获取当前零件所属的库  然后拿取库的属性组  add by ljr 20251114
                //改动点6： 14.产品分类属性
                //将对应的产品分类属性合并到同一个单元格中，格式为：属性名：属性值；属性名：属性值.....，例如：净面积：25；周长：16.7；打孔：165
                AttributeList attributeValues = partObject.getAttributeValues(context);
                StringList attributeList = new StringList();
                for (int i1 = 0; i1 < attributeValues.size(); i1++) {
                    Attribute attribute = attributeValues.get(i1);
                    String name = attribute.getName();
                    String value = attribute.getValue();
                    if (classAttrList.contains(name)) {
                        if (nlsMap.containsKey(name)) {
                                nlsAttr = nlsMap.get(name);
                        } else {
                            nlsAttr = EnoviaResourceBundle.getProperty(context, "emxFrameworkStringResource", context.getLocale(), "emxFramework.Attribute."+ name.replaceAll(" ", "_"));
                            nlsMap.put(name, nlsAttr);
                            //属性类型
                        }
                        attributeList.add(nlsAttr + ":" + value);
                    }
                }
                partInfoMap.put("classAttribute", attributeList.join("\n"));
//                String classId = partObject.getInfo(context, "to[Classified Item].from.physicalid");
//                if (UIUtil.isNotNullAndNotEmpty(classId)) {
//                    MapList classificationAttributes = jfFasteningPieceMxJPO.getClassClassificationAttributes(context, classId, false);
//                    StringList attributeList = new StringList();
//                    for(int i2=0; i2<classificationAttributes.size(); i2++) {
//                        HashMap attributeGroup = (HashMap) classificationAttributes.get(i2);
//                        String attributeGroupName = (String) attributeGroup.get("name");
//                        MapList attributes = (MapList) attributeGroup.get("attributes");
//                        for (int j = 0; j < attributes.size(); j++) {
//                            HashMap attribute = (HashMap) attributes.get(j);
//                            String attributeName = (String) attribute.get("name");
//                            if (UIUtil.isNullOrEmpty(attributeName)) {
//                                continue;
//                            }
//                            if (!classAttrList.contains("attribute[" + attributeName + "]")) {
//                                continue;
//                            }
//                            if (nlsMap.containsKey(attributeName)) {
//                                nlsAttr = nlsMap.get(attributeName);
//                            } else {
//                                nlsAttr = EnoviaResourceBundle.getProperty(context, "emxFrameworkStringResource", context.getLocale(), "emxFramework.Attribute."+ attributeName.replaceAll(" ", "_"));
//                                nlsMap.put(attributeName, nlsAttr);
//                                //属性类型
//                            }
//                            attributeList.add(nlsAttr + ":" + partObject.getAttributeValue(context, attributeName));
//                        }
//                    }
//                    partInfoMap.put("classAttribute", attributeList.join("\n"));
//                }
                //项目名称如果为空 设置为name
                if (UIUtil.isNullOrEmpty(UIUtil.getValue(partInfoMap, "to[JFProject2RootPart].from.description"))) {
                    partInfoMap.put("to[xz].from.description",UIUtil.getValue(partInfoMap, "to[JFProject2RootPart].from.name"));
                }
                //拿取owner 获取全名
                String strOwner = (String) partInfoMap.get(SELECT_OWNER);
                strOwner = PersonUtil.getFullName(context, strOwner);
                partInfoMap.put(SELECT_OWNER,strOwner);
                        //拿取3D 状态 国际化
                String strCurrent = (String) partInfoMap.get(SELECT_CURRENT);
                JF_LOGGER.info("partInfoMap:{}",partInfoMap);
                partInfoMap.put(SELECT_CURRENT, EnoviaResourceBundle.getStateI18NString(context, "VPLM_SMB_Definition_MajorRev", strCurrent, context.getLocale().toString()));
                jfExportEBOMMxJPO.writeDataInExcelRowSecond(context,workbook,sheet,writeRowIndex,EBOMMappingList,partInfoMap,basicCellStyle,lastCellNum,true,configMap, styleList, wrapTextCellStyle, redStyle, leftStyle);
                writeRowIndex ++;
            }
            JF_LOGGER.info("写入完成！！！！！！！！！:{}");
            //调整特殊行的宽度
            for (int i = 0; i < str16List.size(); i++) {
                sheet.setColumnWidth(str16List.get(i), 16 * 256);
            }
            for (int i = 0; i < str20List.size(); i++) {
                sheet.setColumnWidth(str20List.get(i), 20 * 256);
            }
            for (int i = 0; i < str40List.size(); i++) {
                sheet.setColumnWidth(str40List.get(i), 40 * 256);
            }
            //获取当前时间
            LocalDateTime now = LocalDateTime.now();
            // 将 LocalDateTime 对象转换成指定格式的字符串
            String strFormattedDate = now.format(formatter);
            String strFileNameBase = UIUtil.isNotNullAndNotEmpty(strExportObjectName) ? strExportObjectName : strProjectTitle;
            JF_LOGGER.info("fileName:{}", JF_PublicMethodClass_mxJPO.buildStringInStrings(strFileNameBase,"_",strFormattedDate,".xlsx"));
            res.put("file",workbook);
            res.put("fileName",JF_PublicMethodClass_mxJPO.buildStringInStrings(strFileNameBase,"_",strFormattedDate,".xlsx"));
            return res;
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        } finally {
            ContextUtil.popContext(context);
        }
    }

    /**
    * 获取结构中 不是仅cad件的结构
    * @param context
	* @param oneLevel
	* @param typeSelectList
	* @param relSelectList
	* @param level
	* @param partMapList
    * @author LIUJR
    * @throws
    * @return void
    * @date 2025/11/19 10:54
    * @description
    */
    private void getNotCADPartList(Context context, DomainObject oneLevel, StringList typeSelectList, StringList relSelectList, Integer level, MapList partMapList) throws Exception{
        try {
            MapList mapList = oneLevel.getRelatedObjects(context,
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
            for (int i = 0; i < mapList.size(); i++) {
                Map map = (Map) mapList.get(i);
                String strCAD = (String) map.get(SELECT_Attr_SynchroEBOMCAD);
                if ("FALSE".equalsIgnoreCase(strCAD)) {
                    continue;
                }
                map.put(SELECT_LEVEL, String.valueOf(level));
                partMapList.add(map);
                String id = UIUtil.getValue(map, SELECT_ID);
                oneLevel.setId(id);
                getNotCADPartList(context, oneLevel, typeSelectList, relSelectList, level + 1, partMapList);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    public StringList getExistGCId(Map oneLevelMap ,Set<String> GCIDSet){
        StringList idList = new StringList();
        for (String strGCId : GCIDSet) {
            if (oneLevelMap.containsKey(strGCId)) {
                idList.add(strGCId);
            }
        }
        return idList;
    }
}
