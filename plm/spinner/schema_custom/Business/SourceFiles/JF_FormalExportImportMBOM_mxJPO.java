import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.DomainRelationship;
import com.matrixone.apps.domain.util.*;
import com.matrixone.apps.framework.ui.UIUtil;
import matrix.db.Context;
import matrix.db.JPO;
import matrix.db.Page;
import matrix.util.StringList;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.fileupload.FileItem;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static com.matrixone.apps.domain.DomainConstants.*;

public class JF_FormalExportImportMBOM_mxJPO implements JF_PLMConstants_mxJPO{
    private static final DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("M/d/yyyy h:mm:ss a", Locale.US);
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
    private static final DateTimeFormatter formatterDate = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.ENGLISH);
    private static final DateTimeFormatter formatterTime = DateTimeFormatter.ofPattern("HH:mm:ss", Locale.ENGLISH);
    private static final SimpleDateFormat sdf = new SimpleDateFormat("M/d/yyyy h:mm:ss a");

    private static final Logger JF_LOGGER = LoggerFactory.getLogger(JF_FormalExportImportMBOM_mxJPO.class);
    private static final Object FORMAL_COLUMN_MAPPING_LOCK = new Object();
    private static final Object FORMAL_MBOM_EXPAND_CONFIG_LOCK = new Object();
    private static final Object FORMAL_POSITION_ORDER_LOCK = new Object();
    private static final Map<String, MapList> FORMAL_COLUMN_MAPPING_CACHE = new LinkedHashMap<>();
    private static final Map<String, String> FORMAL_MBOM_EXPAND_CONFIG_CACHE = new LinkedHashMap<>();
    private static final List<String> FORMAL_POSITION_ORDER_CACHE = new ArrayList<>();
    private static final Set<String> FORMAL_TRIM_ROLL_DETAIL_CATEGORY_CACHE = new LinkedHashSet<>();
    private static volatile boolean FORMAL_COLUMN_MAPPING_INITIALIZED;
    private static volatile boolean FORMAL_MBOM_EXPAND_CONFIG_INITIALIZED;
    private static volatile boolean FORMAL_POSITION_ORDER_INITIALIZED;

    /**
     * 导出MBOM清单
     * @param context
     * @param args
     * @return
     * @throws Exception
     */
    public Map exportMBOMExcel(Context context, String[] args) throws Exception {
        Map resMap = new HashMap();
        String projectId = args[0];
        String mbomType = args[1];
        try {
            ContextUtil.pushContext(context);
            String  classPath = JF_PublicMethodClass_mxJPO.getBasicUrl(context, new String[]{"Space.WEBINFO.Path"});
            JF_LOGGER.info("classPath:{}", classPath);
            int webInfIndex = classPath.indexOf("WEB-INF");
            if (webInfIndex == -1) {
                throw new IllegalStateException("WEB-INF not found in classPath.");
            }
            String fileTemPath = classPath.substring(0, webInfIndex);
            String filePath = JF_ECRService_mxJPO.getTemplatePath("MBOMExportTemplate.xlsx", fileTemPath);
            //打开文件
            InputStream inputStream = new FileInputStream(filePath);
            Workbook workbook = WorkbookFactory.create(inputStream);
            String fileName = "";
            DomainObject projectObj = DomainObject.newInstance(context, projectId);
            String projectName = projectObj.getInfo(context, "name");
            fileName = "MBOM制造物料清单_"+projectName+"_";
            Sheet sheet = workbook.getSheet("MBOM");
            getExportMBOMInfo(context,workbook,sheet,projectId,mbomType);
            //获取当前时间
            LocalDateTime now = LocalDateTime.now();
            // 将 LocalDateTime 对象转换成指定格式的字符串
            String strFormattedDate = now.format(formatter);
            resMap.put("file", workbook);
            resMap.put("flag", "Y");
            resMap.put("fileName", JF_PublicMethodClass_mxJPO.buildStringInStrings(fileName, strFormattedDate, ".xlsx"));
        }catch (Exception e){
            resMap.put("flag", "N");
            JF_LOGGER.error("JF_ExportMBOM------exportMBOMExcel error", e);
            return resMap;
        }finally {
            ContextUtil.popContext(context);
        }
        return resMap;
    }


    public void getExportMBOMInfo(Context context, Workbook workbook,Sheet sheet , String projectId, String target) throws Exception{
        StringList typeSelectList = JF_Util_mxJPO.basicBolistSel();
        typeSelectList.add(SELECT_PHYSICAL_ID);
        typeSelectList.add(SELECT_LEVEL);
        typeSelectList.add("attribute[JF_PartNameEN]");
        typeSelectList.add("attribute[JF_PartNameCN]");
        //类别
        typeSelectList.add("attribute[JF_PartType]");
        //零件采购类型
        typeSelectList.add("attribute[JF_ProcurementType]");
        //是否DB
        typeSelectList.add("attribute[JF_DirectBuy]");
        typeSelectList.add("attribute[JF_Unit]");
        typeSelectList.add("attribute[JF_PartDes]");
        typeSelectList.add("attribute[JF_PartENDes]");
        typeSelectList.add("attribute[JF_Width]");
        typeSelectList.add("attribute[JF_Utilizationrate]");
        typeSelectList.add("attribute[JF_PartNumber]");
        //特殊采购类型
        typeSelectList.add("attribute[JF_SpecialProcurementType]");
        StringList relSelectList = JF_Util_mxJPO.basicRellistSel();
        relSelectList.add(SELECT_FROM_ID);
        relSelectList.add("attribute[JF_Dosage]");
        StringList EBomTypeSelectList = JF_Util_mxJPO.basicBolistSel();
        EBomTypeSelectList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_V_PART_NUMBER);
        EBomTypeSelectList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JF_PartNameCN);
        EBomTypeSelectList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JF_PartNameEN);
        EBomTypeSelectList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JF_ProcurementType);
        EBomTypeSelectList.add("attribute[PLMEntity.V_description]");
        EBomTypeSelectList.add(SELECT_ATTR_JFPartType);
        EBomTypeSelectList.add(SELECT_PHYSICAL_ID);
        //长宽高
        EBomTypeSelectList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JF_Lon);
        EBomTypeSelectList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JF_Wid);
        EBomTypeSelectList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JF_Hig);
        //参考重量
        EBomTypeSelectList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JF_Weight);
        //实际重量
        EBomTypeSelectList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JF_WeightTarget);

        EBomTypeSelectList.add("attribute[JF_VPMReference.JF_Detail_CN]");
        EBomTypeSelectList.add("attribute[JF_VPMReference.JF_Material]");
        EBomTypeSelectList.add("attribute[Function]");
        EBomTypeSelectList.add("attribute[NetArea]");
        EBomTypeSelectList.add("attribute[Circumference]");



        JF_ExportEBOM_mxJPO jfExportEBOMMxJPO = new JF_ExportEBOM_mxJPO();
        JF_VPMReferenceEBOM_mxJPO ebom = new JF_VPMReferenceEBOM_mxJPO();
        JF_DR_mxJPO dr = new JF_DR_mxJPO();
        Map<String, Map> customerPartsDBInfoCache = new HashMap<>();
        DomainObject projectObj = DomainObject.newInstance(context,projectId);
        String topMbomId = projectObj.getInfo(context, "from[JF_relProject2MBOM].to.id");
//        Sheet sheet = workbook.getSheet("MBOM");
        DomainObject topMbomObj = DomainObject.newInstance(context, topMbomId);
        StringList zeroLevelPart = topMbomObj.getInfoList(context, "from[JF_relManufacturedItem].to.id");
        int lastColumnIndex = sheet.getRow(3).getLastCellNum()-1;
        lastColumnIndex = lastColumnIndex + zeroLevelPart.size();

        //获取项目关联的最新发布版颜色矩阵
        String latestMatrixId = getLatestMatrixId(context,projectObj,relSelectList);
        JF_LOGGER.info("target------:{}",target);
        JF_LOGGER.info("latestMatrixId------:{}",latestMatrixId);
        Map colorStyle = null;
        //获取颜色分组对应的颜色风格
        if (UIUtil.isNotNullAndNotEmpty(latestMatrixId)){
            colorStyle = getColorStyleMap(context,latestMatrixId,relSelectList);
        }
//        JF_LOGGER.info("colorStyle------:{}",colorStyle);

        //获取配置文件 根据MBOM制造件“零件号”前两位，配置MBOM导出时半成品展开层级 如果配置文件里面对应零件类型的expand为false,只导出一级件，不展开子级。如果是true就和现在的处理一样

        JF_ExportMBOM_mxJPO jfExportMBOMMxJPO = new JF_ExportMBOM_mxJPO();
        Map<String, String> mbomConfigMap = jfExportMBOMMxJPO.getMBOMConfig(context);
        //获取表格映射
        MapList MBOMMappingList = JF_PublicMethodClass_mxJPO.getJFCostChangeHistoryTemplateMapping(context, "MBOM");
        //表头添加列
        createHeaderColumn(context,workbook,sheet,zeroLevelPart,MBOMMappingList,latestMatrixId,EBomTypeSelectList,ebom,projectId);
//        JF_LOGGER.info("MBOMMappingList----:{}",MBOMMappingList);
        //实际写入数据
        MapList actualDataList = new MapList();
        //去重一级件
        Map oneLevelMapInfo = new HashMap<String,Map>();
        Map<String, String> oneLevelMapInfoMiddle = new HashMap<>();
        for (String gcId : zeroLevelPart) {
            DomainObject gcObj = DomainObject.newInstance(context, gcId);
            Map GCInfo = gcObj.getInfo(context, typeSelectList);
            //手动设置0级
            GCInfo.put(SELECT_LEVEL,"0");
            //设置本身用量为1
            GCInfo.put(gcId,"1");
            actualDataList.add(GCInfo);
            //一级件
            MapList partMapList = gcObj.getRelatedObjects(context,
                    JF_PLMConstants_mxJPO.REL_JF_relManufacturedItem, // relationship pattern
                    JF_PLMConstants_mxJPO.TYPE_JF_ManufacturedItem,                                    // object pattern
                    typeSelectList,                            // object selects
                    relSelectList, // relationship selects
                    false,                                        // to direction
                    true,                                        // from direction
                    (short) 1,                                    // recursion level
                    "",                // object where clause
                    "",
                    (short) 0);
            JF_LOGGER.info("partMapList246:{}",partMapList);
            for (Object o : partMapList) {
                Map oneLevelMap = (Map) o;
                String oneLevelId = UIUtil.getValue(oneLevelMap, "id");
                String JF_Dosage = UIUtil.getValue(oneLevelMap, "attribute[JF_Dosage]");
                String strRevision = (String) oneLevelMap.get(SELECT_REVISION);
                String JF_PartNumber = (String) oneLevelMap.get("attribute[JF_PartNumber]");
                if (JF_PartNumber.startsWith("GX")) {
                    //GX多拿一层一级件
                    DomainObject oneLevel = DomainObject.newInstance(context, oneLevelId);
                    MapList gxpartMapList = oneLevel.getRelatedObjects(context,
                            JF_PLMConstants_mxJPO.REL_JF_relManufacturedItem, // relationship pattern
                            JF_PLMConstants_mxJPO.TYPE_JF_ManufacturedItem,    // object pattern
                            typeSelectList,                                     // object selects
                            relSelectList,                                     // relationship selects
                            false,                                             // to direction
                            true,                                              // from direction
                            (short) 1,                                         // recursion level
                            "",                                                // object where clause
                            "",
                            (short) 0);

                    // 遍历 gxpartMapList
                    for (Object gxPartObj : gxpartMapList) {
                        Map gxPartMap = (Map) gxPartObj;
                        String gxPartId = UIUtil.getValue(gxPartMap, "id");
                        String gxJF_Dosage = UIUtil.getValue(gxPartMap, "attribute[JF_Dosage]");
                        String gxStrRevision = (String) gxPartMap.get(SELECT_REVISION);
                        String gxJF_PartNumber = (String) gxPartMap.get("attribute[JF_PartNumber]");

                        getExtractOneLevelInfo(oneLevelMapInfo, oneLevelMapInfoMiddle, gcId, gxPartMap, gxPartId, gxJF_Dosage, gxStrRevision, gxJF_PartNumber);
                    }
                    continue;
                }

                getExtractOneLevelInfo(oneLevelMapInfo, oneLevelMapInfoMiddle, gcId, oneLevelMap, oneLevelId, JF_Dosage, strRevision, JF_PartNumber);
            }
        }
        //整椅MBOM：仅保留整椅下的1级别件，如果1级件存在GX件，则把展示GX下级件；
        //面套MBOM：过滤整椅下的发泡件（GT），展示面套件下级所有零件；
        //发泡MBOM ：过滤整椅下的发泡件（GU），展示发泡件下级所有零件。


        //遍历一级件及其子集
        JF_LOGGER.info("oneLevelMapInfo292:{}",oneLevelMapInfo);
        for (Object o : oneLevelMapInfo.entrySet()) {
            Map.Entry entry = (Map.Entry)o;
            String strOneLevelId = (String) entry.getKey();
            Map oneLevelInfo = (Map) entry.getValue();
            //添加一级件
//            actualDataList.add(oneLevelInfo);
            String onePartNumber = UIUtil.getValue(oneLevelInfo,"attribute[JF_PartNumber]").trim();
            String firstTwoCharacters = onePartNumber.substring(0, 2);
            DomainObject oneLevel = DomainObject.newInstance(context,strOneLevelId);

            MapList partMapList=new MapList();


            //配置中fasle或者是选择的导出整椅件(如果配置是true但是选择的是整椅，应该怎么处理，以哪个为准，目前是以选择为准)
            //bug点，面套和发泡目前配置种是true;当选择整椅的时候，面套和发泡也会导出
            if ("false".equals(mbomConfigMap.get(firstTwoCharacters))&&"GC".equals(target)){
//            if ("GC".equals(target)) {
                //此处需要单独处理
                //新需求 如果是false还是要展开，但是需要遍历完所有节点的新属性JF_SpecialProcurementType/不为NA的时候需要向上单向展开(双经销/外协/NA)
                //为NA的时候不需要保留
                JF_LOGGER.info("313if");
                //判断是否是自制件 如果是才会要第二级  add by ljr 20260303
                partMapList = checkOnePart(context, oneLevelInfo, typeSelectList, relSelectList);
//                continue;
            }else{
                JF_LOGGER.info("316else");
                //一级件子件
                partMapList = oneLevel.getRelatedObjects(context,
                        JF_PLMConstants_mxJPO.REL_JF_relManufacturedItem, // relationship pattern
                        JF_PLMConstants_mxJPO.TYPE_JF_ManufacturedItem,                                    // object pattern
                        typeSelectList,                            // object selects
                        relSelectList, // relationship selects
                        false,                                        // to direction
                        true,                                        // from direction
                        (short) 0,                                    // recursion level
                        "",                // object where clause
                        "",
                        (short) 0);
            }
//            JF_LOGGER.info("firstTwoCharacters------:{}",firstTwoCharacters);
//            JF_LOGGER.info("oneLevelInfo------:{}",oneLevelInfo);
            JF_LOGGER.info("partMapList329------:{}",partMapList);
            JF_LOGGER.info("target 330:{} firstTwoCharacters:{}",target,firstTwoCharacters);

            if(!"GC".equals(target)){
                //未选择整椅,选择面套(发泡)
                if(!target.equals(firstTwoCharacters)){
                    //当前遍历的件不是面套(发泡)
                    continue;
                }else{
                    //面套（发泡），添加一级件
                    actualDataList.add(oneLevelInfo);
//                    partMapList = oneLevel.getRelatedObjects(context,
//                            JF_PLMConstants_mxJPO.REL_JF_relManufacturedItem, // relationship pattern
//                            JF_PLMConstants_mxJPO.TYPE_JF_ManufacturedItem,                                    // object pattern
//                            typeSelectList,                            // object selects
//                            relSelectList, // relationship selects
//                            false,                                        // to direction
//                            true,                                        // from direction
//                            (short) 0,                                    // recursion level
//                            "",                // object where clause
//                            "",
//                            (short) 0);
                }
            }else{
                //整椅，添加一级件
                actualDataList.add(oneLevelInfo);
            }


            //子件对应的gc id集合 因为用量需要和GCid对应起来
            StringList mappingGCId = getMappingGCId(oneLevelInfo, zeroLevelPart);

//            JF_LOGGER.info("---lxg-mappingGCId->"+mappingGCId);
            int level = Integer.MAX_VALUE;
            Map<String, Object> sameBomeMap = new HashMap<>();
            //合并一级件下面的用量
            for (Object o1 : partMapList) {
                Map partInfoMap = (Map) o1;


                String childId = UIUtil.getValue(partInfoMap, "id");
                String fromId = UIUtil.getValue(partInfoMap, SELECT_FROM_ID);
                String JF_Dosage = UIUtil.getValue(partInfoMap, "attribute[JF_Dosage]");
                String strRevision = (String) partInfoMap.get(SELECT_REVISION);
                String JF_PartNumber = (String) partInfoMap.get("attribute[JF_PartNumber]");
                int childLevel = Integer.parseInt(UIUtil.getValue(partInfoMap, SELECT_LEVEL));

                if(JF_PartNumber.equals("GM0004029")||JF_PartNumber.equals("GM0004030")){
//                    JF_LOGGER.info("--lxg--partInfoMap-->:{}",partInfoMap);
                    JF_LOGGER.info("--lxg--fromId-->:{}",fromId);
                    JF_LOGGER.info("--lxg--JF_PartNumber-->:{}",JF_PartNumber);
                    JF_LOGGER.info("--lxg--strRevision-->:{}",strRevision);
                    JF_LOGGER.info("--lxg--childLevel-->:{}",childLevel);
                    JF_LOGGER.info("--lxg--level-->:{}",level);
                }

                if (childLevel > level) {
                    //childLevel大于level的,说明该层的父级已经重复，不用合并用量
                    continue;
                }
                level = Integer.MAX_VALUE;
                StringBuilder sb = new StringBuilder();
                sb.append(fromId);
                sb.append(JF_PartNumber);
                sb.append(strRevision);
                //因为MBOM结构相同的JF_PartNumber和revision对应的id不一样 所以不能用id判断去重合并
                String strUniqueKey = sb.toString();
                if (!sameBomeMap.containsKey(strUniqueKey)){
                    sameBomeMap.put(strUniqueKey, partInfoMap);
                    for (String strGCId : mappingGCId) {
                        partInfoMap.put(strGCId,JF_Dosage);
                    }
                    //标识一级件下面子件
                    partInfoMap.put("isSunPart",true);
                    JF_LOGGER.info("@@@@@@@@@@@@@@@@@@添加到集合的子集:{}",partInfoMap);
                    actualDataList.add(partInfoMap);
                }else {
                    level = childLevel;
                    Map bommap = (Map) sameBomeMap.get(strUniqueKey);
                    String dosageStr = (String) bommap.get("attribute[JF_Dosage]");
                    if (UIUtil.isNullOrEmpty(dosageStr)){
                        dosageStr = "0" ;
                    }
                    if (UIUtil.isNullOrEmpty(JF_Dosage)){
                        JF_Dosage = "0" ;
                    }
                    JF_LOGGER.info("dosageStr:{}strChildDosage：{}",dosageStr,JF_Dosage);
                    double dosage = Double.parseDouble(dosageStr) + Double.parseDouble(JF_Dosage);
                    String strSumDosage = String.valueOf(dosage);
                    for (String strGCId : mappingGCId) {
                        bommap.put(strGCId,strSumDosage);
                    }
                    bommap.put("attribute[JF_Dosage]", strSumDosage);
                }
            }


        }
        //缩略图保存地址
        String strPrePath = JF_PublicMethodClass_mxJPO.getBasicUrl(context, new String[]{"DownloadVPM.Img.Path"});

//        JF_LOGGER.info("-----lxg-actualDataList-->"+actualDataList);
        //初始下标
        int writeRowIndex = 4 ;
        CellStyle basicCellStyle = getBasicCellStyle(workbook);
        //开始写入数据
        JF_LOGGER.info("actualDataList:{}",actualDataList);
        for (int i = 0; i < actualDataList.size(); i++) {
            Map partInfoMap = (Map) actualDataList.get(i);
            partInfoMap.put("ITEM",String.valueOf(i+1));
            String strPartId = (String) partInfoMap.get(SELECT_ID);
            String strLevel = (String) partInfoMap.get(SELECT_LEVEL);
            String strRevision = (String) partInfoMap.get(SELECT_REVISION);
            String JF_PartNumber = (String) partInfoMap.get("attribute[JF_PartNumber]");
            if(JF_PartNumber.equals("GU0005767")){
                JF_LOGGER.info("-----lxg-GU0005767-->"+partInfoMap);
            }

            DomainObject partObj = DomainObject.newInstance(context, strPartId);
            //穿透“零件号”以“GX”开头的层级
            if (JF_PartNumber.startsWith("GX")){
                MapList partMapList = partObj.getRelatedObjects(context,
                        JF_PLMConstants_mxJPO.REL_JF_relManufacturedItem, // relationship pattern
                        JF_PLMConstants_mxJPO.TYPE_JF_ManufacturedItem,                                    // object pattern
                        typeSelectList,                            // object selects
                        relSelectList, // relationship selects
                        false,                                        // to direction
                        true,                                        // from direction
                        (short) 0,                                    // recursion level
                        "",                // object where clause
                        "",
                        (short) 0);
                for (Object o : partMapList) {
                    Map map = (Map) o;
                    String id = UIUtil.getValue(map, "id");
                    String fromId = UIUtil.getValue(map, SELECT_FROM_ID);
                    for (int i1 = i+1; i1 < actualDataList.size(); i1++) {
                        Map map1 = (Map) actualDataList.get(i1);
                        String id1 = UIUtil.getValue(map1, "id");
                        String fromId1 = UIUtil.getValue(map1, SELECT_FROM_ID);
                        if (Objects.equals(id,id1) && Objects.equals(fromId,fromId1)){
                            String level = UIUtil.getValue(map1, SELECT_LEVEL);
                            int iLevel = Integer.parseInt(level) - 1;
                            String sLevel = String.valueOf(iLevel);
                            map1.put("level",sLevel);
                            break;
                        }
                    }
                }
                continue;
            }
            //子件层级+1
            if (partInfoMap.containsKey("isSunPart")){
                int iActualLevel = Integer.parseInt(strLevel) + 1;
                strLevel = String.valueOf(iActualLevel);
            }
            partInfoMap.put(JF_PublicMethodClass_mxJPO.buildStringInStrings(SELECT_LEVEL,"_",strLevel),strLevel);

            //找到对应的EBOM的属性
            Map ebomInfo = getEbomInfo(context, JF_PartNumber, strRevision, EBomTypeSelectList);
            if (MapUtils.isNotEmpty(ebomInfo)){
                JF_LOGGER.info("JF_PartNumber:{} strRevision:{} latestMatrixId:{}",JF_PartNumber,strRevision,latestMatrixId);
                String ebomPhysicalId = UIUtil.getValue(ebomInfo, SELECT_PHYSICAL_ID);
                String ebomId = UIUtil.getValue(ebomInfo, SELECT_ID);
                // DirectBuy、客户零件号和客户零件版本已改为关系属性，按当前项目读取对应的客户零件号关系。
                if (!customerPartsDBInfoCache.containsKey(ebomId)) {
                    customerPartsDBInfoCache.put(ebomId,
                            dr.getDRCustomerPartsDBRelationInfo(context, ebomId, projectId));
                }
                Map customerPartsDBInfo = customerPartsDBInfoCache.get(ebomId);
                //20260727 update by ljr MBOM导出时，未维护项目对应客户零件/DB信息的零件按non-DB输出；
                String directBuy = JF_Util_mxJPO.getPartDirectBuyByProjectWithNonDBDefault(
                        context,
                        ebomId,
                        projectId);
                String ebomCustomerPartNumber = UIUtil.getValue(
                        customerPartsDBInfo,
                        Select_Attr_JFCustomerPartNumber);
                String ebomCustomerPartRevision = UIUtil.getValue(
                        customerPartsDBInfo,
                        Select_Attr_JFCustomerPartRevision);

                String JFLength = UIUtil.getValue(ebomInfo, JF_PLMConstants_mxJPO.SELECT_ATTR_JF_Lon);
                String JFWidth = UIUtil.getValue(ebomInfo, JF_PLMConstants_mxJPO.SELECT_ATTR_JF_Wid);
                String JFHeight = UIUtil.getValue(ebomInfo, JF_PLMConstants_mxJPO.SELECT_ATTR_JF_Hig);
                String JF_Weight = UIUtil.getValue(ebomInfo, JF_PLMConstants_mxJPO.SELECT_ATTR_JF_Weight);
                String JF_WeightTarget = UIUtil.getValue(ebomInfo, JF_PLMConstants_mxJPO.SELECT_ATTR_JF_WeightTarget);

                partInfoMap.put(JF_PLMConstants_mxJPO.SELECT_ATTR_JF_Lon,JFLength);
                partInfoMap.put(JF_PLMConstants_mxJPO.SELECT_ATTR_JF_Wid,JFWidth);
                partInfoMap.put(JF_PLMConstants_mxJPO.SELECT_ATTR_JF_Hig,JFHeight);
                partInfoMap.put(JF_PLMConstants_mxJPO.SELECT_ATTR_JF_Weight,JF_Weight);
                partInfoMap.put(JF_PLMConstants_mxJPO.SELECT_ATTR_JF_WeightTarget,JF_WeightTarget);
                //
                partInfoMap.put("attribute[JF_VPMReference.JF_Detail_CN]", UIUtil.getValue(ebomInfo, "attribute[JF_VPMReference.JF_Detail_CN]"));
                partInfoMap.put("attribute[JF_VPMReference.JF_Material]", UIUtil.getValue(ebomInfo, "attribute[JF_VPMReference.JF_Material]"));
                partInfoMap.put("attribute[Function]", UIUtil.getValue(ebomInfo, "attribute[Function]"));
                partInfoMap.put("attribute[NetArea]", UIUtil.getValue(ebomInfo, "attribute[NetArea]"));
                partInfoMap.put("attribute[Circumference]", UIUtil.getValue(ebomInfo, "attribute[Circumference]"));
                //获取缩略图
                String strImgId = jfExportEBOMMxJPO.getDownLoadPictureByPartId(context, ebomPhysicalId, strPrePath);
                partInfoMap.put("picture",strImgId);
                partInfoMap.put("attribute[JF_DirectBuy]", directBuy);
                partInfoMap.put("attribute[CustomerPartNumber]",ebomCustomerPartNumber);
                partInfoMap.put("attribute[JF_VPMReference.JF_CustomerPartRevision]",ebomCustomerPartRevision);

                if (UIUtil.isNotNullAndNotEmpty(latestMatrixId)){
                    //有颜色矩阵才去写关于颜色的信息
                    DomainObject ebomObj = DomainObject.newInstance(context, ebomId);
                    MapList projectMapList = ebomObj.getRelatedObjects(context, "JFProject2ColorGroup", "Project Space", StringList.create("id"),
                            StringList.create("id[connection]","attribute[JF_ColorGroupName]","attribute[JF_ColorMatrixName]"), true, false, (short) 1, "", "", (short) 0);
                    JF_LOGGER.info("projectMapList:{}",projectMapList);
                    if (CollectionUtils.isEmpty(projectMapList)){
                        //判断是否是供货价
                        boolean flag = ebom.getPartIsSupplyPart(context, new String[]{ebomId,projectId});
                        String attributeValue = flag ? "UA" : "NA";
                        writeRowIndex = getWriteRowIndex(context, workbook, sheet, lastColumnIndex, MBOMMappingList, colorStyle, writeRowIndex, basicCellStyle, partInfoMap, strRevision, JF_PartNumber, attributeValue);
                    }else {
                        boolean isColor = false;//add by caipan
                        for (Object o : projectMapList) {
                            Map projectMap = (Map) o;
                            String id = UIUtil.getValue(projectMap, "id");
                            if (Objects.equals(id,projectId)){//add by caipan 可能有问题
                                isColor = true;
                                String JF_ColorGroupName = UIUtil.getValue(projectMap, "attribute[JF_ColorGroupName]");
                                writeRowIndex = getWriteRowIndex(context, workbook, sheet, lastColumnIndex, MBOMMappingList, colorStyle, writeRowIndex, basicCellStyle, partInfoMap, strRevision, JF_PartNumber, JF_ColorGroupName);
                            }
                        }
                        //add by caipan
                        if(!isColor) {
                            boolean flag = ebom.getPartIsSupplyPart(context, new String[]{ebomId, projectId});
                            String attributeValue = flag ? "UA" : "NA";
                            writeRowIndex = getWriteRowIndex(context, workbook, sheet, lastColumnIndex, MBOMMappingList, colorStyle, writeRowIndex, basicCellStyle, partInfoMap, strRevision, JF_PartNumber, attributeValue);
                        }
                    }
                }else {
//                    partInfoMap.put("JF_InternalColorCode","NUL");
//                    partInfoMap.put("JF_CustormColorCode","NUL");
//                    partInfoMap.put("InternalFullPartNumber","NUL");
                    partInfoMap.put("JF_InternalColorCode","000");
                    partInfoMap.put("JF_CustormColorCode","000");
                    partInfoMap.put("InternalFullPartNumber","000");

                    partInfoMap.put("usage","");
                    partInfoMap.put("styleTitle","");
                    writeDataInExcel(context,workbook,sheet,writeRowIndex,MBOMMappingList,partInfoMap,basicCellStyle,lastColumnIndex);
                    writeRowIndex ++;
                }
            }

        }


    }

    public Map  checkOnePartChild(Context context,String projectId)throws Exception{
        StringList typeSelectList = JF_Util_mxJPO.basicBolistSel();
        typeSelectList.add(SELECT_PHYSICAL_ID);
        typeSelectList.add(SELECT_LEVEL);
        typeSelectList.add("attribute[JF_PartNameEN]");
        typeSelectList.add("attribute[JF_PartNameCN]");
        typeSelectList.add("attribute[JF_PartType]");
        typeSelectList.add("attribute[JF_ProcurementType]");
        typeSelectList.add("attribute[JF_DirectBuy]");
        typeSelectList.add("attribute[JF_Unit]");
        typeSelectList.add("attribute[JF_PartDes]");
        typeSelectList.add("attribute[JF_PartENDes]");
        typeSelectList.add("attribute[JF_Width]");
        typeSelectList.add("attribute[JF_Utilizationrate]");
        typeSelectList.add("attribute[JF_PartNumber]");
        typeSelectList.add("attribute[JF_SpecialProcurementType]");
        StringList relSelectList = JF_Util_mxJPO.basicRellistSel();
        relSelectList.add(SELECT_FROM_ID);
        relSelectList.add("attribute[JF_Dosage]");

        DomainObject projectObj = DomainObject.newInstance(context,projectId);
        String topMbomId = projectObj.getInfo(context, "from[JF_relProject2MBOM].to.id");

        DomainObject topMbomObj = DomainObject.newInstance(context, topMbomId);
        StringList zeroLevelPart = topMbomObj.getInfoList(context, "from[JF_relManufacturedItem].to.id");

        Map errorId=new HashMap();

        Map<String, String> oneLevelMapInfoMiddle = new HashMap<>();
        for (String gcId : zeroLevelPart) {
            DomainObject gcObj = DomainObject.newInstance(context, gcId);
            Map GCInfo = gcObj.getInfo(context, typeSelectList);
            //手动设置0级
            GCInfo.put(SELECT_LEVEL,"0");
            //设置本身用量为1
            GCInfo.put(gcId,"1");

            //一级件
            MapList partMapList = gcObj.getRelatedObjects(context,
                    JF_PLMConstants_mxJPO.REL_JF_relManufacturedItem, // relationship pattern
                    JF_PLMConstants_mxJPO.TYPE_JF_ManufacturedItem,                                    // object pattern
                    typeSelectList,                            // object selects
                    relSelectList, // relationship selects
                    false,                                        // to direction
                    true,                                        // from direction
                    (short) 1,                                    // recursion level
                    "",                // object where clause
                    "",
                    (short) 0);
            for (Object o : partMapList) {
                Map oneLevelMap = (Map) o;
                String oneLevelId = UIUtil.getValue(oneLevelMap, "id");
                String JF_Dosage = UIUtil.getValue(oneLevelMap, "attribute[JF_Dosage]");
                String strRevision = (String) oneLevelMap.get(SELECT_REVISION);
                String JF_PartNumber = (String) oneLevelMap.get("attribute[JF_PartNumber]");
                String JF_fromid = (String) oneLevelMap.get(SELECT_FROM_ID);
                if (JF_PartNumber.startsWith("GX")) {
                    //GX多拿一层一级件
                    DomainObject oneLevel = DomainObject.newInstance(context, oneLevelId);
                    MapList gxpartMapList = oneLevel.getRelatedObjects(context,
                            JF_PLMConstants_mxJPO.REL_JF_relManufacturedItem, // relationship pattern
                            JF_PLMConstants_mxJPO.TYPE_JF_ManufacturedItem,    // object pattern
                            typeSelectList,                                     // object selects
                            relSelectList,                                     // relationship selects
                            false,                                             // to direction
                            true,                                              // from direction
                            (short) 1,                                         // recursion level
                            "",                                                // object where clause
                            "",
                            (short) 0);

                    // 遍历 gxpartMapList
                    for (Object gxPartObj : gxpartMapList) {
                        Map gxPartMap = (Map) gxPartObj;
                        String gxPartId = UIUtil.getValue(gxPartMap, "id");
                        String gxJF_Dosage = UIUtil.getValue(gxPartMap, "attribute[JF_Dosage]");
                        String gxStrRevision = (String) gxPartMap.get(SELECT_REVISION);
                        String gxJF_PartNumber = (String) gxPartMap.get("attribute[JF_PartNumber]");
                        String gxfromid = (String) gxPartMap.get(SELECT_FROM_ID);
                        getCheckonePartChild(context, oneLevelMapInfoMiddle,  gxPartId, gxStrRevision, gxJF_PartNumber,gxfromid,errorId);

                    }
                    continue;
                }

                getCheckonePartChild(context, oneLevelMapInfoMiddle,oneLevelId, strRevision, JF_PartNumber,JF_fromid,errorId);

            }
        }
        return errorId;
    }

    public void getCheckonePartChild(Context context, Map oneLevelMapInfoMiddle, String oneLevelId, String strRevision, String JF_PartNumber,String fromid,Map errorId)throws Exception {
        //统计一级件的用量
        String uniqueKey = fromid+JF_PartNumber+strRevision;
        if (oneLevelMapInfoMiddle.containsKey(uniqueKey)){
            // 如果 uniqueKey 已存在
            StringList existingChildPartlist= (StringList) oneLevelMapInfoMiddle.get(uniqueKey);
            JF_LOGGER.info("existingChildPartlist:{}", existingChildPartlist);
            DomainObject oneLevel = DomainObject.newInstance(context, oneLevelId);
            StringList childPartlist = oneLevel.getInfoList(context, "from[JF_relManufacturedItem].to.attribute[JF_PartNumber]");
            JF_LOGGER.info("childPartlist:{}", childPartlist);
//            if(childPartlist.compareTo(existingChildPartlist)!=0){
//                if(!errorId.containsKey(JF_PartNumber)){
//                    errorId.put(JF_PartNumber,"");
//                }
//
//            }
            if (!existingChildPartlist.containsAll(childPartlist) || !childPartlist.containsAll(existingChildPartlist)) {
                if(!errorId.containsKey(JF_PartNumber)){
                    errorId.put(JF_PartNumber,"");
                }
                // 进到这里说明：要么多了，要么少了，要么内容变了
            }
        }else {
            DomainObject oneLevel = DomainObject.newInstance(context, oneLevelId);
            StringList childPartlist = oneLevel.getInfoList(context, "from[JF_relManufacturedItem].to.attribute[JF_PartNumber]");
            oneLevelMapInfoMiddle.put(uniqueKey, childPartlist);
        }
    }


    public MapList checkOnePart(Context context,Map onelevelMap,StringList typeSelectList,StringList relSelectList)throws Exception{
//        JF_LOGGER.info("---lxg--->checkOnePart--->start");
        MapList partMapList=new MapList();
        MapList childMapList=new MapList();
        //JF_SpecialProcurementType
        try {

            //1，遍历所有子集，放入集合，id为key，value为partmap
            //2，

            String oneid= (String) onelevelMap.get("id");
            DomainObject oneLevel=DomainObject.newInstance(context,oneid);

            Map childMap=new HashMap();
            childMapList = oneLevel.getRelatedObjects(context,
                    JF_PLMConstants_mxJPO.REL_JF_relManufacturedItem, // relationship pattern
                    JF_PLMConstants_mxJPO.TYPE_JF_ManufacturedItem,                                    // object pattern
                    typeSelectList,                            // object selects
                    relSelectList, // relationship selects
                    false,                                        // to direction
                    true,                                        // from direction
                    (short) 0,                                    // recursion level
                    "",                // object where clause
                    "",
                    (short) 0);

            for(int i=0;i<childMapList.size();i++){
                Map childmap= (Map) childMapList.get(i);
                String phyid= (String) childmap.get(SELECT_PHYSICAL_ID);
                childMap.put(phyid,childmap);
            }
//           Map onelevelMap=  oneLevel.getInfo(context,typeSelectList);
            String rootid= (String) onelevelMap.get(SELECT_PHYSICAL_ID);
            Set result=new HashSet();

//            JF_LOGGER.info("--checkOnePart-lxg--childMap->"+childMap);
            if(childMap.size()>0){
                findFlaggedNodes(context,onelevelMap,result,typeSelectList,relSelectList,rootid);
            }
            //add by ljr 20260303 二级件 非发泡面套的自制件的下级
            String jfProcurementType = oneLevel.getAttributeValue(context, "JF_ProcurementType");
            String onePartNumber = UIUtil.getValue(onelevelMap,"attribute[JF_PartNumber]");
            String firstTwoCharacters = onePartNumber.substring(0, 2);
            if("make".equalsIgnoreCase(jfProcurementType)&&(!"GT".equals(firstTwoCharacters)&&!"GU".equals(firstTwoCharacters))){
//                if ("make".equalsIgnoreCase(jfProcurementType)) {
                //二级件 非发泡面套的自制件的下级
                MapList makeMapList = oneLevel.getRelatedObjects(context,
                        JF_PLMConstants_mxJPO.REL_JF_relManufacturedItem, // relationship pattern
                        JF_PLMConstants_mxJPO.TYPE_JF_ManufacturedItem,                                    // object pattern
                        typeSelectList,                            // object selects
                        relSelectList, // relationship selects
                        false,                                        // to direction
                        true,                                        // from direction
                        (short) 1,                                    // recursion level
                        "",                // object where clause
                        "",
                        (short) 0);
                for(int i=0;i<makeMapList.size();i++){
                    Map childmap= (Map) makeMapList.get(i);
                    result.add((String) childmap.get(SELECT_PHYSICAL_ID).toString().trim());
                }
            }

            Iterator iterator=childMapList.iterator();
            while (iterator.hasNext()) {
                Map childmap= (Map)iterator.next();
                String phyid= (String) childmap.get(SELECT_PHYSICAL_ID);
                if(!result.contains(phyid)){
                    iterator.remove();
                }
            }

            for(Object resobj:result){
                String phyids= (String) resobj;
//                JF_LOGGER.info("--checkOnePart-lxg--phyids->"+phyids);
                if(childMap.containsKey(phyids)){
                    partMapList.add(childMap.get(phyids));
                }

            }
            partMapList.sort(SELECT_LEVEL, "ascending", "string");
            JF_LOGGER.info("--checkOnePart-lxg--partMapList->"+partMapList);
            JF_LOGGER.info("--checkOnePart-lxg--childMapList->"+childMapList);


        }catch (Exception e){
            e.printStackTrace();
            throw e;
        }
        return childMapList;
    }


    private static void findFlaggedNodes(Context context,Map nodemap, Set result,StringList typeSelectList,StringList relSelectList,String rootid) throws Exception{
        if (nodemap == null) return;
        String JF_SpecialProcurementType= (String) nodemap.get("attribute[JF_SpecialProcurementType]");
        String phyid= (String) nodemap.get(SELECT_PHYSICAL_ID);
//        JF_LOGGER.info("---lxg--nodemap->"+nodemap);
        // 如果当前节点标记不为NA，添加该节点及其所有父节点
        //新增外协总成后，修改调整，为DualDistribution或outsource
//        if (!"NA".equals(JF_SpecialProcurementType)) {

        if ("DualDistribution".equals(JF_SpecialProcurementType)||"outsource".equals(JF_SpecialProcurementType)) {
            Map current = nodemap;
            while (current != null) {
                JF_LOGGER.info("---lxg--current1->"+current);
                result.add(current.get(SELECT_PHYSICAL_ID).toString().trim());
                //获取父级，
                String parentid= (String) current.get(SELECT_FROM_ID);
                JF_LOGGER.info("---lxg--current1--parentid-->"+parentid);
                if(UIUtil.isNotNullAndNotEmpty(parentid)){
                    DomainObject parentObj=DomainObject.newInstance(context,parentid);
                    String parentphyid=parentObj.getInfo(context,"physicalid").trim();
                    current=  parentObj.getInfo(context,typeSelectList);
                    StringList fromid=parentObj.getInfoList(context,"to[JF_relManufacturedItem].from.id");
                    JF_LOGGER.info("---lxg--fromid->"+fromid);
                    current.put(SELECT_FROM_ID,fromid.size()>0?fromid.get(0):"");
                    JF_LOGGER.info("---lxg--current2->"+current);
                    JF_LOGGER.info("---lxg--parentphyid->"+parentphyid);
                    JF_LOGGER.info("---lxg--rootid->"+rootid);
                    if(rootid.equalsIgnoreCase(parentphyid)){
                        current=null;
                    }
                }else {
                    current=null;
                }
            }
        }

        DomainObject nodemapObj=DomainObject.newInstance(context,phyid);

        MapList childMapList = nodemapObj.getRelatedObjects(context,
                JF_PLMConstants_mxJPO.REL_JF_relManufacturedItem, // relationship pattern
                JF_PLMConstants_mxJPO.TYPE_JF_ManufacturedItem,                                    // object pattern
                typeSelectList,                            // object selects
                relSelectList, // relationship selects
                false,                                        // to direction
                true,                                        // from direction
                (short) 1,                                    // recursion level
                "",                // object where clause
                "",
                (short) 0);



        // 递归处理子节点
        for (Object child : childMapList) {
            Map childmap= (Map) child;
            findFlaggedNodes(context,childmap,result,typeSelectList,relSelectList,rootid);
        }
    }

    /**
     *
     * @param oneLevelMapInfo
     * @param oneLevelMapInfoMiddle
     * @param gcId
     * @param oneLevelMap
     * @param oneLevelId
     * @param JF_Dosage
     * @param strRevision
     * @param JF_PartNumber
     */
    public void getExtractOneLevelInfo(Map oneLevelMapInfo, Map<String, String> oneLevelMapInfoMiddle, String gcId, Map oneLevelMap, String oneLevelId, String JF_Dosage, String strRevision, String JF_PartNumber) {
        //统计一级件的用量
        //bug 如果是同一个part但是在不同的BOM里面，应该是不同的才对
        String uniqueKey = JF_PartNumber+strRevision;
        if (oneLevelMapInfoMiddle.containsKey(uniqueKey)){
            // 如果 uniqueKey 已存在，获取已保存的 oneLevelId
            String existingOneLevelId = oneLevelMapInfoMiddle.get(uniqueKey);
            // 更新对应的数量
            Map<String, String> existingMap = (Map<String, String>) oneLevelMapInfo.get(existingOneLevelId);
            String strSaveDosage = UIUtil.getValue(existingMap, gcId);
            if (UIUtil.isNotNullAndNotEmpty(strSaveDosage)) {
                BigDecimal aBigDecimal = new BigDecimal(JF_Dosage);
                BigDecimal bBigDecimal = new BigDecimal(strSaveDosage);
                BigDecimal sum = aBigDecimal.add(bBigDecimal);
                JF_Dosage = sum.toString();
            }
            existingMap.put(gcId, JF_Dosage); // 更新数量
        }else {
            // 如果 uniqueKey 不存在，则保存 oneLevelId 和对应的 Map
            oneLevelMap.put(gcId, JF_Dosage);
            oneLevelMapInfo.put(oneLevelId, oneLevelMap);
            oneLevelMapInfoMiddle.put(uniqueKey, oneLevelId);
        }
    }


    public void getExtractOneLevelInfoforSync(Map oneLevelMapInfo, Map<String, String> oneLevelMapInfoMiddle, String gcId, Map oneLevelMap, String oneLevelId, String JF_Dosage, String strRevision, String JF_PartNumber,String fromid) {
        //统计一级件的用量
        String uniqueKey = fromid+JF_PartNumber+strRevision;
        if (oneLevelMapInfoMiddle.containsKey(uniqueKey)){
            // 如果 uniqueKey 已存在，获取已保存的 oneLevelId
            String existingOneLevelId = oneLevelMapInfoMiddle.get(uniqueKey);
            // 更新对应的数量
            Map<String, String> existingMap = (Map<String, String>) oneLevelMapInfo.get(existingOneLevelId);
            String strSaveDosage = UIUtil.getValue(existingMap, gcId);
            if (UIUtil.isNotNullAndNotEmpty(strSaveDosage)) {
                BigDecimal aBigDecimal = new BigDecimal(JF_Dosage);
                BigDecimal bBigDecimal = new BigDecimal(strSaveDosage);
                BigDecimal sum = aBigDecimal.add(bBigDecimal);
                JF_Dosage = sum.toString();
            }
            existingMap.put(gcId, JF_Dosage); // 更新数量
        }else {
            // 如果 uniqueKey 不存在，则保存 oneLevelId 和对应的 Map
            oneLevelMap.put(gcId, JF_Dosage);
            oneLevelMapInfo.put(oneLevelId, oneLevelMap);
            oneLevelMapInfoMiddle.put(uniqueKey, oneLevelId);
        }
    }

    public int getWriteRowIndex(Context context, Workbook workbook, Sheet sheet, int lastColumnIndex, MapList MBOMMappingList, Map colorStyle, int writeRowIndex, CellStyle basicCellStyle, Map partInfoMap, String strRevision, String JF_PartNumber, String JF_ColorGroupName) throws Exception {
        JF_LOGGER.info("JF_PartNumber1348:{}",JF_PartNumber);
        MapList styleMapList = (MapList) colorStyle.get(JF_ColorGroupName);
        // 使用 Map 来存储已经处理过的 JF_InternalColorCode 和对应的 styleTitle
        Map<String,String> processedColorCodes = new HashMap<>();
        Map<String,String> processedColorCodes2 = new HashMap<>();
        Map<String,String> processedColorCodes3 = new HashMap<>();
        if (CollectionUtils.isNotEmpty(styleMapList)){
            for (Object o1 : styleMapList) {
                Map styleMap = (Map) o1;
                String JF_InternalColorCode = UIUtil.getValue(styleMap, "attribute[JF_InternalColorCode]");
                String JF_CustormColorCode = UIUtil.getValue(styleMap, "attribute[JF_CustormColorCode]");
                String JF_ColorStyleName = UIUtil.getValue(styleMap, "attribute[JF_ColorStyleName]");
                String Title = UIUtil.getValue(styleMap, "attribute[Title]");
//                String CustomerFullPartNumber = JF_PartNumber + strRevision.split("\\.")[0]+JF_CustormColorCode;
                if (processedColorCodes.containsKey(JF_InternalColorCode)){
                    String existCode = processedColorCodes.get(JF_InternalColorCode);
                    existCode = existCode+","+Title;
                    processedColorCodes.put(JF_InternalColorCode,existCode);
                }else {
                    // 如果没有处理过，添加到 processedColorCodes
                    processedColorCodes.put(JF_InternalColorCode, Title);
                    processedColorCodes2.put(JF_InternalColorCode, JF_CustormColorCode);
                    processedColorCodes3.put(JF_InternalColorCode, JF_ColorStyleName);
                    //此处bug，不应该在此处加入
//                    partInfoMap.put("JF_InternalColorCode",JF_InternalColorCode);
//                    partInfoMap.put("JF_CustormColorCode",JF_CustormColorCode);
//                partInfoMap.put("CustomerFullPartNumber",CustomerFullPartNumber);
//                    JF_LOGGER.info("partInfoMap:{}",partInfoMap);
                    partInfoMap.put("usage","");
                }
            }
            // 在循环结束后，写入数据到 Excel
            for (Map.Entry<String, String> entry : processedColorCodes.entrySet()) {
                String colorCode = entry.getKey();
                String accumulatedTitle = entry.getValue();
//                String InternalFullPartNumber = JF_PartNumber + strRevision.split("\\.")[0]+colorCode;
                String InternalFullPartNumber = JF_PartNumber +colorCode+ strRevision.split("\\.")[0]+"0";
                String JF_CustormColorCode=processedColorCodes2.get(colorCode);
                String JF_ColorStyleName=processedColorCodes3.get(colorCode);
                partInfoMap.put("InternalFullPartNumber",InternalFullPartNumber.substring(1));//内部完整零件号规则更新，取消第一位的G，如GC0002945AAS09变为C0002945AAS09
                partInfoMap.put("styleTitle", accumulatedTitle);
                partInfoMap.put("JF_CustormColorCode",JF_CustormColorCode);
                partInfoMap.put("JF_InternalColorCode",colorCode);
                partInfoMap.put("JF_ColorStyleName",JF_ColorStyleName);
                writeDataInExcel(context, workbook, sheet, writeRowIndex, MBOMMappingList, partInfoMap, basicCellStyle, lastColumnIndex);
                writeRowIndex++;
            }
        }else {
            JF_LOGGER.info("partInfoMap:{}",partInfoMap);
            partInfoMap.put("usage","");
            partInfoMap.put("styleTitle","");
            writeDataInExcel(context,workbook,sheet,writeRowIndex,MBOMMappingList,partInfoMap,basicCellStyle,lastColumnIndex);
            writeRowIndex ++;
        }

        return writeRowIndex;
    }


    public void getMbomjson(Context context, MapList MBOMMappingList, Map colorStyle, Map partInfoMap, String strRevision, String JF_PartNumber, String JF_ColorGroupName,String strTime,JSONArray partsArray,JSONArray bomArray,Set partSet,Map fullNumberMap,StringList zeroLevelPart,Map relMap) throws Exception {
        MapList styleMapList = (MapList) colorStyle.get(JF_ColorGroupName);


        // 使用 Map 来存储已经处理过的 JF_InternalColorCode 和对应的 styleTitle
        Map<String,String> processedColorCodes = new HashMap<>();
        Map<String,String> processedColorCodes2 = new HashMap<>();
        Map<String,String> processedColorCodes3 = new HashMap<>();
        Map<String,String> processedColorCodes4 = new HashMap<>();
        if (CollectionUtils.isNotEmpty(styleMapList)){
            //mod,bug,不同的风格，可能会有相同的颜色码，在处理父子结构的时候，需要注意
            for (Object o1 : styleMapList) {
                Map styleMap = (Map) o1;
                String JF_InternalColorCode = UIUtil.getValue(styleMap, "attribute[JF_InternalColorCode]");
                String JF_CustormColorCode = UIUtil.getValue(styleMap, "attribute[JF_CustormColorCode]");
                String JF_ColorStyleName = UIUtil.getValue(styleMap, "attribute[JF_ColorStyleName]");
                String styleName = UIUtil.getValue(styleMap, "name");
                String Title = UIUtil.getValue(styleMap, "attribute[Title]");
//                String CustomerFullPartNumber = JF_PartNumber + strRevision.split("\\.")[0]+JF_CustormColorCode;
                if (processedColorCodes.containsKey(JF_InternalColorCode)){
                    String existCode = processedColorCodes.get(JF_InternalColorCode);
                    existCode = existCode+","+Title;
                    processedColorCodes.put(JF_InternalColorCode,existCode);
                }else {
                    // 如果没有处理过，添加到 processedColorCodes
                    processedColorCodes.put(JF_InternalColorCode, Title);

                    processedColorCodes4.put(JF_InternalColorCode, JF_ColorStyleName);
                    partInfoMap.put("usage","");
                }
                //不同的风格，可能会有相同的内部码
                processedColorCodes3.put(styleName, JF_InternalColorCode);
                //20260724 update by ljr 整椅客户颜色码按颜色风格保存，避免不同风格共用内部颜色码时取错；
                processedColorCodes2.put(styleName, JF_CustormColorCode);

            }
            // 在循环结束后，写入数据到 Excel
            for (Map.Entry<String, String> entry : processedColorCodes3.entrySet()) {
//                String colorCode = entry.getKey();
//                String accumulatedTitle = entry.getValue();
                String styleName= entry.getKey();
                String colorCode = entry.getValue();
                String accumulatedTitle = processedColorCodes.get(colorCode);
//                String InternalFullPartNumber = JF_PartNumber + strRevision.split("\\.")[0]+colorCode;
                String InternalFullPartNumber = JF_PartNumber + colorCode+strRevision.split("\\.")[0]+"0";
                String JF_CustormColorCode=processedColorCodes2.get(styleName);
                String JF_ColorStyleName=processedColorCodes4.get(colorCode);
                JF_LOGGER.info("JF_ColorStyleName----->"+JF_ColorStyleName);
                partInfoMap.put("InternalFullPartNumber",InternalFullPartNumber.substring(1));//内部完整零件号规则更新，取消第一位的G，如GC0002945AAS09变为C0002945AAS09
                partInfoMap.put("styleTitle", accumulatedTitle);
                partInfoMap.put("JF_CustormColorCode",JF_CustormColorCode);
                partInfoMap.put("JF_InternalColorCode",colorCode);
                partInfoMap.put("styleName",styleName);
                partInfoMap.put("JF_ColorStyleName",JF_ColorStyleName);

//                JF_LOGGER.info("partInfoMap---lxg>:{}",partInfoMap);
                getPartInfoMapJson(context,partsArray,bomArray,partInfoMap,strRevision,strTime,partSet,fullNumberMap,zeroLevelPart,relMap);

            }
        }else {
//            JF_LOGGER.info("partInfoMap:{}",partInfoMap);
            partInfoMap.put("usage","");
            partInfoMap.put("styleTitle","");
//            writeDataInExcel(context,workbook,sheet,writeRowIndex,MBOMMappingList,partInfoMap,basicCellStyle,lastColumnIndex);
            getPartInfoMapJson(context,partsArray,bomArray,partInfoMap,strRevision,strTime,partSet,fullNumberMap,zeroLevelPart,relMap);

        }

        String partId = UIUtil.getValue(partInfoMap, "id");
        String FullNumber=UIUtil.getValue(partInfoMap, "InternalFullPartNumber");
        if(FullNumber.equals("F0001137AANUL")){
            JF_LOGGER.info("partId--lxg-->"+partId);
            JF_LOGGER.info("map--lxg-->"+partInfoMap);
        }


    }


    public void getPartInfoMapJson(Context context,JSONArray partsArray,JSONArray bomArray,Map map,String strRevision,String strTime,Set partSet,Map fullNumberMap,StringList zeroLevelPart,Map relMap)throws Exception{
        JSONObject partJson = new JSONObject();


        String partId = UIUtil.getValue(map, "id");
        String JF_PartDes = UIUtil.getValue(map, "attribute[JF_PartNameCN]");
        String JF_PartENDes = UIUtil.getValue(map, "attribute[JF_PartNameEN]");
        String JF_Unit = UIUtil.getValue(map, "attribute[JF_Unit]");
        String JF_ProcurementType = UIUtil.getValue(map, "attribute[JF_ProcurementType]");
        String JF_DirectBuy = UIUtil.getValue(map, "attribute[JF_DirectBuy]");
        String JF_PartNumber = UIUtil.getValue(map, "attribute[JF_PartNumber]");
        String JF_SpecialProcurementType  = UIUtil.getValue(map, "attribute[JF_SpecialProcurementType]");
        String JF_PartType  = UIUtil.getValue(map, "attribute[JF_PartType]");
        String JF_Dosage  = UIUtil.getValue(map, "attribute[JF_Dosage]");
        String ebomCustomerPartNumber  = UIUtil.getValue(map, "attribute[CustomerPartNumber]");
        String ebomCustomerPartRevision  = UIUtil.getValue(map, "attribute[JF_VPMReference.JF_CustomerPartRevision]");
        String JF_InternalColorCode  = UIUtil.getValue(map, "JF_InternalColorCode");
        String JF_CustormColorCode  = UIUtil.getValue(map, "JF_CustormColorCode");
        String detailCategory  = UIUtil.getValue(map, JF_PLMConstants_mxJPO.SELECT_ATTR_JF_Detail_CN);
        String styleName  = UIUtil.getValue(map, "styleName");
        String JF_ColorStyleName  = UIUtil.getValue(map, "JF_ColorStyleName");
        String styleTitle  = UIUtil.getValue(map, "styleTitle");
        String fromid  = UIUtil.getValue(map, "from.id");
//        String revision = UIUtil.getValue(map, "revision");
        String FullNumber=UIUtil.getValue(map, "InternalFullPartNumber");
        partJson.put("FullNumber", FullNumber );
        partJson.put("Description",JF_ColorStyleName.equals("NUL")||JF_ColorStyleName.equals("000")?JF_PartDes:JF_PartDes+"_"+JF_ColorStyleName);
        //如果 英文名称的 长度大于 30，则截取 0 到 30；否则保留原样。  add  by ljr 20260415
        String strDescriptionEN  = JF_PartENDes.substring(0, Math.min(JF_PartENDes.length(), 30));
        if(UIUtil.isNotNullAndNotEmpty(JF_ColorStyleName)&&!"NUL".equalsIgnoreCase(JF_ColorStyleName)&&!"null".equalsIgnoreCase(JF_ColorStyleName)&&!"000".equalsIgnoreCase(JF_ColorStyleName)){
            strDescriptionEN=strDescriptionEN+"_"+JF_ColorStyleName;
        }
        partJson.put("DescriptionEN", strDescriptionEN);
        partJson.put("Unit", JF_Unit);
        partJson.put("ProcurementType", JF_ProcurementType);
        partJson.put("DirectBuy", JF_DirectBuy);
        partJson.put("SellableItem", "N");
        partJson.put("PartNumber", JF_PartNumber);
        partJson.put("Version", strRevision.split("\\.")[0]);
        partJson.put("ColorCode",JF_InternalColorCode );
//        partJson.put("ColorDescription",styleTitle);
        partJson.put("ColorDescription",JF_ColorStyleName.equals("NUL")||JF_ColorStyleName.equals("000")?"":JF_ColorStyleName);
        partJson.put("CustomerPartNumber", ebomCustomerPartNumber);
        partJson.put("CustomerPartRevision", ebomCustomerPartRevision);
        partJson.put("ECNNumber", strTime);
        partJson.put("Customer", "");
        partJson.put("SpecialType", JF_SpecialProcurementType);
        partJson.put("Type", JF_PartType);
        //20260724 update by ljr MDM零件数据新增整椅客户颜色码和零件详细分类中文；
        partJson.put("CustormColorCode", JF_CustormColorCode);
        partJson.put("DetailClsCN", detailCategory);
        String Dosage=getDosage(map,zeroLevelPart);


        if(!partSet.contains(FullNumber)){
            partSet.add(FullNumber);
            partsArray.add(partJson);
        }

        //有父级
        if(UIUtil.isNotNullAndNotEmpty(fromid)){
            Map fullmap = (Map) fullNumberMap.get(fromid);

            String pfullNumber= (String) fullmap.get(styleName);

            if(!relMap.containsKey(pfullNumber+FullNumber)){
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("ParentID", pfullNumber);
                jsonObject.put("ChildID", FullNumber);
                jsonObject.put("IsVirtual", "N");
                jsonObject.put("Dosage", Dosage);
                bomArray.add(jsonObject);
                relMap.put(pfullNumber+FullNumber,"");
            }

        }

    }


    public String getDosage(Map map,StringList zeroLevelPart)throws Exception{
        String Dosage="";
        for (String gcId : zeroLevelPart) {
            if(map.containsKey(gcId)){
                Dosage=map.get(gcId)+"";
                break;
            }
        }
        return Dosage;
    }

    /**
     * @Author Liuxg
     * @Description 获取所有父级节点
     * @Date 2025/12/2 21:37
     * @Param [context, id]
     * @return java.lang.String
     **/
    public String getRootid(Context context,String id)throws Exception{
        try {
            StringList pidlist=new StringList();
            DomainObject MbomObj=DomainObject.newInstance(context,id);
            StringList bul=new StringList();
            bul.add(SELECT_ID);
            MapList relatedObjects = MbomObj.getRelatedObjects(
                    context,
                    "JF_relManufacturedItem",
                    "JF_ManufacturedItem",
                    bul,
                    DomainConstants.EMPTY_STRINGLIST,
                    true,
                    false,
                    (short) 0,
                    "",
                    "",
                    0
            );

            for(int i=0;i<relatedObjects.size();i++){
                Map map= (Map) relatedObjects.get(i);
                String fromid= (String) map.get(SELECT_ID);
                pidlist.add(fromid);
            }
            //


        }catch (Exception e){
            e.printStackTrace();
        }
        return id;
    }

    /**
     * 获取颜色分组对应的颜色风格
     * @param context
     * @param latestMatrixId
     * @param relSelectList
     * @return
     * @throws Exception
     */
    private Map getColorStyleMap(Context context, String latestMatrixId, StringList relSelectList) throws Exception{
        DomainObject latestMatrixObj = DomainObject.newInstance(context, latestMatrixId);
        MapList colorGroupMapList = latestMatrixObj.getRelatedObjects(context, "JFColorMatrix2JFColorGroup", "JFColorGroup", StringList.create("id","attribute[Title]"),
                relSelectList, false, true, (short) 1, "", "", (short) 0);
        Map colorStyle = new HashMap();
        for (Object o : colorGroupMapList) {
            Map colorGroupMap = (Map) o;
            String colorGroupId = UIUtil.getValue(colorGroupMap, "id");
            String colorGroupTitle = UIUtil.getValue(colorGroupMap, "attribute[Title]");
            DomainObject colorGroupObj = DomainObject.newInstance(context, colorGroupId);
            MapList colorStyleMapList = colorGroupObj.getRelatedObjects(context, "JFColorGroup2JFColorStyle", "JFColorStyle", StringList.create("id","attribute[Title]","name"),
                    StringList.create("id[connection]","attribute[JF_InternalColorCode]","attribute[JF_CustormColorCode]","attribute[JF_ColorStyleName]"), false, true, (short) 1, "", "", (short) 0);
            colorStyle.put(colorGroupTitle,colorStyleMapList);
        }
        return colorStyle;
    }

    /**
     * 获取最新发布版颜色矩阵
     * @param context
     * @param projectObj
     * @param relSelectList
     * @return
     */
    private String getLatestMatrixId(Context context, DomainObject projectObj, StringList relSelectList) throws Exception{
        //项目关联的颜色矩阵
        MapList matrixMapList = projectObj.getRelatedObjects(context, "JFProject2JFColorMatrix", "JFColorMatrix", StringList.create("id","current","revision"),
                relSelectList, false, true, (short) 1, "", "", (short) 0);
        String latestMatrixId = "";
        String latestMatrixRevision = "";
        for (Object o : matrixMapList) {
            Map matrixMap = (Map) o;
            String matrixId = UIUtil.getValue(matrixMap, "id");
            String matrixCurrent = UIUtil.getValue(matrixMap, "current");
            String matrixRevision = UIUtil.getValue(matrixMap, "revision");
            // 检查当前版本是否是最新发布版
            if ("Release".equals(matrixCurrent)){
                if (UIUtil.isNullOrEmpty(latestMatrixRevision) ||
                        (UIUtil.isNotNullAndNotEmpty(matrixCurrent ) && matrixRevision.compareTo(latestMatrixRevision) > 0)) {
                    latestMatrixRevision = matrixRevision;
                    latestMatrixId = matrixId;
                }
            }
        }
        return latestMatrixId;
    }

    /**
     * 写入数据行
     * @param context
     * @param workbook
     * @param sheet
     * @param writeRowIndex
     * @param mbomMappingList
     * @param dataMap
     * @param basicCellStyle
     * @param lastColumnIndex
     * @throws Exception
     */
    private void writeDataInExcel(Context context, Workbook workbook, Sheet sheet, int writeRowIndex, MapList mbomMappingList, Map dataMap, CellStyle basicCellStyle, int lastColumnIndex) throws Exception{
        Row row = sheet.createRow(writeRowIndex);
//        JF_LOGGER.info("mbomMappingList:{} dataMap：{}",mbomMappingList,dataMap);
        for (int i = 0; i < mbomMappingList.size(); i++) {
            Map mappingMap = (Map) mbomMappingList.get(i);
            String strKey = (String)mappingMap.get("id");
            String strColIndex = (String)mappingMap.get("value");
            if (dataMap.containsKey(strKey)){
                Cell cell = null ;
                if ("picture".equals(strKey)){
                    String strImgPath = (String) dataMap.get(strKey);
                    if (UIUtil.isNotNullAndNotEmpty(strImgPath)){
                        JF_ExportEBOM_mxJPO.insertPictureInCell(workbook, sheet, strImgPath, writeRowIndex, Integer.parseInt(strColIndex), 100, 50);
                    }
                    //设置边框
                    cell = row.createCell(Integer.parseInt(strColIndex));
                }else if("attribute[JF_VPMReference.JF_WeightTarget]".equalsIgnoreCase(strKey)) {
                    String strValue = (String) dataMap.get(strKey);
                    if(UIUtil.isNullOrEmpty(strValue)){
                        strValue=(String) dataMap.get("attribute[JF_VPMReference.JF_Weight]");
                    }
                    cell = row.createCell(Integer.parseInt(strColIndex));
                    if (JF_PublicMethodClass_mxJPO.isNumeric(strValue)) {
                        double dValue = Double.parseDouble(strValue);
                        cell.setCellValue(dValue);
                        cell.setCellStyle(basicCellStyle);
                    }else {
                        cell.setCellValue(strValue);
                        cell.setCellStyle(basicCellStyle);
                    }

                } else if("attribute[JF_PartNameCN]".equalsIgnoreCase(strKey)) {
                    String strValue = (String) dataMap.get(strKey);
                    String colorCode=(String) dataMap.get("JF_ColorStyleName");
                    if(UIUtil.isNotNullAndNotEmpty(colorCode)&&!"NUL".equalsIgnoreCase(colorCode)&&!"null".equalsIgnoreCase(colorCode)&&!"000".equalsIgnoreCase(colorCode)){
                        strValue=strValue+"_"+colorCode;
                    }
                    cell = row.createCell(Integer.parseInt(strColIndex));
                    if (JF_PublicMethodClass_mxJPO.isNumeric(strValue)) {
                        double dValue = Double.parseDouble(strValue);
                        cell.setCellValue(dValue);
                        basicCellStyle.setAlignment(HorizontalAlignment.LEFT);
                        cell.setCellStyle(basicCellStyle);
                        basicCellStyle.setAlignment(HorizontalAlignment.CENTER);
                    }else {
                        cell.setCellValue(strValue);
                        basicCellStyle.setAlignment(HorizontalAlignment.LEFT);
                        cell.setCellStyle(basicCellStyle);
                        basicCellStyle.setAlignment(HorizontalAlignment.CENTER);
                    }

                }else if("attribute[JF_PartNameEN]".equalsIgnoreCase(strKey)) {
                    String strValue = (String) dataMap.get(strKey);
                    //如果 英文名称的 长度大于 30，则截取 0 到 30；否则保留原样。  add  by ljr 20260415
                    strValue = strValue.substring(0, Math.min(strValue.length(), 30));
                    String colorCode=(String) dataMap.get("JF_ColorStyleName");
                    if(UIUtil.isNotNullAndNotEmpty(colorCode)&&!"NUL".equalsIgnoreCase(colorCode)&&!"null".equalsIgnoreCase(colorCode)&&!"000".equalsIgnoreCase(colorCode)){
                        strValue=strValue+"_"+colorCode;
                    }
                    cell = row.createCell(Integer.parseInt(strColIndex));
                    if (JF_PublicMethodClass_mxJPO.isNumeric(strValue)) {
                        double dValue = Double.parseDouble(strValue);
                        cell.setCellValue(dValue);
                        basicCellStyle.setAlignment(HorizontalAlignment.LEFT);
                        cell.setCellStyle(basicCellStyle);
                        basicCellStyle.setAlignment(HorizontalAlignment.CENTER);
                    }else {
                        cell.setCellValue(strValue);
                        basicCellStyle.setAlignment(HorizontalAlignment.LEFT);
                        cell.setCellStyle(basicCellStyle);
                        basicCellStyle.setAlignment(HorizontalAlignment.CENTER);
                    }

                }else if ("usage".equals(strKey)){
                    String strCellValue  = "";
                    cell = row.createCell(Integer.parseInt(strColIndex));
                    String strPCId = (String) mappingMap.get("objectId");
                    String styleTitle = (String) mappingMap.get("styleTitle");
                    if (dataMap.containsKey(strPCId) && dataMap.containsKey("styleTitle")){
                        String dataTitle = (String) dataMap.get("styleTitle");
                        if (dataTitle.contains(styleTitle)){
                            String usage = (String) dataMap.get(strPCId);
                            strCellValue = usage;
                        }
                    }
                    strCellValue = "0".equals(strCellValue) ? "" : strCellValue;
                    if (JF_PublicMethodClass_mxJPO.isNumeric(strCellValue)) {
                        double dValue = Double.parseDouble(strCellValue);
                        cell.setCellValue(dValue);
                        cell.setCellStyle(basicCellStyle);
                    }else {
                        cell.setCellValue(strCellValue);
                        cell.setCellStyle(basicCellStyle);
                    }
                }else if("JF_InternalColorCode".equals(strKey) || "JF_CustormColorCode".equals(strKey)){
                    //20260724 update by ljr 内部色号和整椅客户颜色码沿用一致的写入规则，000按文本保留；
                    String strValue = (String) dataMap.get(strKey);
                    if(strValue.equals("000")){
                        strValue=strValue+" ";
                    }
                    cell = row.createCell(Integer.parseInt(strColIndex));
                    if (JF_PublicMethodClass_mxJPO.isNumeric(strValue)) {
                        double dValue = Double.parseDouble(strValue);
                        cell.setCellValue(dValue);
                        cell.setCellStyle(basicCellStyle);
                    }else {
                        cell.setCellValue(strValue);
                        cell.setCellStyle(basicCellStyle);
                    }
                }else if("attribute[JF_PartType]".equalsIgnoreCase(strKey)) {//获取翻译值
                    String strValue = (String) dataMap.get(strKey);
                    //翻译
                    strValue = EnoviaResourceBundle.getRangeI18NString(context, "JF_PartType", strValue, "zh_cn");
                    JF_LOGGER.info("strKey:{} strValue：{}",strKey,strValue);
                    cell = row.createCell(Integer.parseInt(strColIndex));
                    if (JF_PublicMethodClass_mxJPO.isNumeric(strValue)) {
                        double dValue = Double.parseDouble(strValue);
                        cell.setCellValue(dValue);
                        cell.setCellStyle(basicCellStyle);
                    }else {
                        cell.setCellValue(strValue);
                        cell.setCellStyle(basicCellStyle);
                    }
                }else {
                    String strValue = (String) dataMap.get(strKey);
                    cell = row.createCell(Integer.parseInt(strColIndex));
                    if (JF_PublicMethodClass_mxJPO.isNumeric(strValue)) {
                        double dValue = Double.parseDouble(strValue);
                        cell.setCellValue(dValue);
                        cell.setCellStyle(basicCellStyle);
                    }else {
                        cell.setCellValue(strValue);
                        cell.setCellStyle(basicCellStyle);
                    }
                }
            }else{
                JF_LOGGER.info("elsestrKey:{}",strKey);
            }
        }

        //单元格样式
        for (int i = 0; i < lastColumnIndex ;i++){
            Cell cell = row.getCell(i);
            if (cell == null){
                cell = row.createCell(i);
            }
            cell.setCellStyle(basicCellStyle);
        }

    }

    /**
     * 找对应的EBOM
     * @param context
     * @param PartNumber
     * @param strRevision
     * @param EBomTypeSelectList
     * @return
     * @throws Exception
     */
    public Map getEbomInfo(Context context, String PartNumber, String strRevision,StringList EBomTypeSelectList) throws Exception{
        Map dateMap = null;
        String where = "attribute[EnterpriseExtension.V_PartNumber]=='"+PartNumber+"'&&revision=='"+strRevision+"'";
        MapList ebomMapList = DomainObject.findObjects(context, "VPMReference", "*", where, EBomTypeSelectList);
        if (ebomMapList.size()>0){
            dateMap = (Map) ebomMapList.get(0);
        }
        return dateMap;
    }

    public StringList getMappingGCId(Map oneLevelMap , StringList GCIDSet){
        StringList idList = new StringList();
        for (String strGCId : GCIDSet) {
            if (oneLevelMap.containsKey(strGCId)) {
                idList.add(strGCId);
            }
        }
        return idList;
    }

    public void createHeaderColumn(Context context, Workbook workbook, Sheet sheet, StringList zeroLevelPart, MapList MBOMMappingList, String latestMatrixId, StringList EBomTypeSelectList, JF_VPMReferenceEBOM_mxJPO ebom, String projectId) throws Exception{

        if (UIUtil.isNotNullAndNotEmpty(latestMatrixId)){
            DomainObject latestMatrixObj = DomainObject.newInstance(context, latestMatrixId);
            MapList colorGroupMapList = latestMatrixObj.getRelatedObjects(context, "JFColorMatrix2JFColorGroup", "JFColorGroup", StringList.create("id","attribute[Title]"),
                    StringList.create("id[connection]"), false, true, (short) 1, "", "", (short) 0);
            Map colorStyle = new HashMap();
            MapList colorStyleCountList = new MapList();
            for (Object o : colorGroupMapList) {
                Map colorGroupMap = (Map) o;
                String colorGroupId = UIUtil.getValue(colorGroupMap, "id");
                String colorGroupTitle = UIUtil.getValue(colorGroupMap, "attribute[Title]");
                DomainObject colorGroupObj = DomainObject.newInstance(context, colorGroupId);
                MapList colorStyleMapList = colorGroupObj.getRelatedObjects(context, "JFColorGroup2JFColorStyle", "JFColorStyle", StringList.create("id","attribute[Title]"),
                        StringList.create("id[connection]","attribute[JF_InternalColorCode]","attribute[JF_CustormColorCode]"), false, true, (short) 1, "", "", (short) 0);
                colorStyle.put(colorGroupTitle,colorStyleMapList);
                colorStyleCountList = colorStyleMapList;
            }
            // 获取最后一列的索引
            int lastColumnIndex = sheet.getRow(3).getLastCellNum()-1;
            JF_LOGGER.info("getExportMBOMInfo-----lastColumnIndex:{}",lastColumnIndex);
            Row rowOne = sheet.getRow(1);
            Row rowTwo = sheet.getRow(2);
            Row headerRow = sheet.getRow(3);
            // 计算当前列的索引
            int currentColumnIndex = lastColumnIndex;
            for (int j = 0; j < colorStyleCountList.size(); j++) {
                Map colorStyleMap = (Map) colorStyleCountList.get(j);
                String styleTitle = UIUtil.getValue(colorStyleMap, "attribute[Title]");
                for (int i = 0; i < zeroLevelPart.size(); i++) {
                    String partId = zeroLevelPart.get(i);
                    DomainObject partObj = DomainObject.newInstance(context, partId);
                    String JF_PartNameCN =partObj.getAttributeValue(context,"JF_PartNameCN");
                    String JF_PartNumber =partObj.getAttributeValue(context,"JF_PartNumber");
                    String strRevision = partObj.getInfo(context, "revision");
                    JF_LOGGER.info("JF_PartNumber---->"+JF_PartNumber);
                    JF_LOGGER.info("strRevision---->"+strRevision);
                    Map ebomInfo = getEbomInfo(context, JF_PartNumber, strRevision, EBomTypeSelectList);
                    JF_LOGGER.info("ebomInfo---->"+ebomInfo);
                    String ebomId = UIUtil.getValue(ebomInfo, SELECT_ID);
                    DomainObject ebomObj = DomainObject.newInstance(context, ebomId);
                    MapList projectMapList = ebomObj.getRelatedObjects(context, "JFProject2ColorGroup", "Project Space", StringList.create("id"),
                            StringList.create("id[connection]","attribute[JF_ColorGroupName]","attribute[JF_ColorMatrixName]"), true, false, (short) 1, "", "", (short) 0);
                    String JF_ColorGroupName = "";
                    if (CollectionUtils.isEmpty(projectMapList)){
                        //判断是否是供货价
                        boolean flag = ebom.getPartIsSupplyPart(context, new String[]{ebomId,projectId});
                        JF_ColorGroupName = flag ? "UA" : "NA";
                    }else {
                        boolean isColor = false;//add by caipan
                        for (Object o : projectMapList) {
                            Map projectMap = (Map) o;
                            String id = UIUtil.getValue(projectMap, "id");
                            if (Objects.equals(id,projectId)){
                                isColor = true;
                                JF_ColorGroupName = UIUtil.getValue(projectMap, "attribute[JF_ColorGroupName]");
                            }
                        }
                        //add by caipan
                        if(!isColor) {
                            boolean flag = ebom.getPartIsSupplyPart(context, new String[]{ebomId,projectId});
                            JF_ColorGroupName = flag ? "UA" : "NA";
                        }
                    }

                    // 创建单元格
                    Cell oneDestCell = rowOne.createCell(currentColumnIndex);
                    MapList styleMapList = (MapList) colorStyle.get(JF_ColorGroupName);
                    if (CollectionUtils.isNotEmpty(styleMapList)){
                        for (Object o1 : styleMapList) {
                            Map styleMap = (Map) o1;
                            String JF_InternalColorCode = UIUtil.getValue(styleMap, "attribute[JF_InternalColorCode]");
                            String Title = UIUtil.getValue(styleMap, "attribute[Title]");
//                            String InternalFullPartNumber = JF_PartNumber + strRevision.split("\\.")[0]+JF_InternalColorCode;
                            String InternalFullPartNumber =  JF_PartNumber.substring(1) +JF_InternalColorCode+ strRevision.split("\\.")[0]+"0";
                            if (Objects.equals(Title,styleTitle)){
                                oneDestCell.setCellValue(InternalFullPartNumber);
                                oneDestCell.setCellStyle(herderStyle(workbook));
                            }
                        }
                    }
                    Cell twoDestCell = rowTwo.createCell(currentColumnIndex);
                    twoDestCell.setCellStyle(herderStyle(workbook)); // 设置样式
                    Cell headerDestCell = headerRow.createCell(currentColumnIndex);
                    headerDestCell.setCellValue(JF_PartNameCN); // 设置值
                    headerDestCell.setCellStyle(herderStyle(workbook)); // 设置样式
                    // 记录新增的列的索引和0层的映射
                    Map<String, String> mappingMap = new HashMap<>();
                    mappingMap.put("objectId", partId);
                    mappingMap.put("value", String.valueOf(currentColumnIndex));
                    mappingMap.put("id", "usage");
                    mappingMap.put("styleTitle", styleTitle);
                    MBOMMappingList.add(mappingMap);
                    // 增加列索引
                    currentColumnIndex++;
                }
                // 合并单元格，合并颜色风格的单元格
                int startColumn = lastColumnIndex + (j * zeroLevelPart.size());
                int endColumn = startColumn + zeroLevelPart.size() - 1;
                JF_LOGGER.info("startColumn--->"+startColumn);
                JF_LOGGER.info("endColumn--->"+endColumn);
                if(startColumn!=endColumn){
                    sheet.addMergedRegion(new CellRangeAddress(2, 2, startColumn, endColumn));
                }
                // 为合并的单元格赋值（可以在这里设置合并单元格的值）
                Cell mergedCell = rowTwo.getCell(startColumn);
                mergedCell.setCellValue(styleTitle);
            }
        } else {
            // 获取最后一列的索引
            int lastColumnIndex = sheet.getRow(3).getLastCellNum() - 1;
            JF_LOGGER.info("getExportMBOMInfo-----lastColumnIndex:{}", lastColumnIndex);
            Row rowOne = sheet.getRow(1);
            Row rowTwo = sheet.getRow(2);
            Row headerRow = sheet.getRow(3);
            for (int i = 0; i < zeroLevelPart.size(); i++) {
                String partId = zeroLevelPart.get(i);
                DomainObject partObj = DomainObject.newInstance(context, partId);
                String JF_PartNameCN = partObj.getAttributeValue(context, "JF_PartNameCN");
                Cell oneDestCell;
                Cell twoDestCell;
                Cell headerDestCell;
                oneDestCell = rowOne.createCell(lastColumnIndex + i);
                oneDestCell.setCellStyle(herderStyle(workbook));
                twoDestCell = rowTwo.createCell(lastColumnIndex + i);
                twoDestCell.setCellStyle(herderStyle(workbook));
                headerDestCell = headerRow.createCell(lastColumnIndex + i);
                headerDestCell.setCellValue(JF_PartNameCN);
                headerDestCell.setCellStyle(herderStyle(workbook));
                //记录新增的列的索引和0层的映射
                Map mappingMap = new HashMap();
                mappingMap.put("objectId", partId);
                mappingMap.put("value", String.valueOf(lastColumnIndex + i));
                mappingMap.put("id", "usage");
                mappingMap.put("styleTitle", "");
                MBOMMappingList.add(mappingMap);
            }
        }
    }

    public CellStyle  herderStyle(Workbook workbook){
        // 创建一个样式
        CellStyle style = workbook.createCellStyle();
        // 设置垂直居中
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        // 设置水平居中
        style.setAlignment(HorizontalAlignment.CENTER);
        // 设置自动换行
        style.setWrapText(true);
        // 创建字体
        Font font = workbook.createFont();
        font.setColor(IndexedColors.BLACK.getIndex());
        font.setBold(true);
        // 将字体应用到样式
        style.setFont(font);
        // 设置背景色为黑色
//        style.setFillForegroundColor(IndexedColors.BLACK.getIndex());
//        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        // 设置边框（粗边框）
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        // 设置边框颜色为白色
        style.setTopBorderColor(IndexedColors.BLACK.getIndex());
        style.setBottomBorderColor(IndexedColors.BLACK.getIndex());
        style.setLeftBorderColor(IndexedColors.BLACK.getIndex());
        style.setRightBorderColor(IndexedColors.BLACK.getIndex());

        return style;
    }

    public static CellStyle getBasicCellStyle(Workbook workbook) {
        CellStyle cellBorderStyle = workbook.createCellStyle();
        cellBorderStyle.setBorderTop(BorderStyle.THIN);
        cellBorderStyle.setBorderBottom(BorderStyle.THIN);
        cellBorderStyle.setBorderLeft(BorderStyle.THIN);
        cellBorderStyle.setBorderRight(BorderStyle.THIN);
        cellBorderStyle.setAlignment(HorizontalAlignment.CENTER); // 水平居中
        cellBorderStyle.setVerticalAlignment(VerticalAlignment.CENTER); // 垂直居中
        return cellBorderStyle;
    }



    public MapList getPsChangeRecord(Context context, String strObjectId) throws Exception {
        try {
            StringList selList = JF_Util_mxJPO.basicBolistSel();
            StringList relList = JF_Util_mxJPO.basicRellistSel();
            selList.add(SELECT_ORIGINATED);
            ContextUtil.pushContext(context);
            JF_LOGGER.info("strObjectId:{}",strObjectId);
            DomainObject objectProject = DomainObject.newInstance(context, strObjectId);
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

            mapList.sort(SELECT_ORIGINATED, "descending", "date");
            JF_LOGGER.info("mapList：{}", mapList);
            return mapList;
        }catch (Exception e) {
            e.printStackTrace();
        }finally {
            ContextUtil.popContext(context);
        }
        return new MapList();
    }

    // 正式MBOM导出新流程使用的类型、缓存和行内辅助Key。现有复制方法仅保留参考，不参与新流程。
    private static final String FORMAL_TYPE_ZERO = "GC";
    private static final String FORMAL_TYPE_TRIM = "GT";
    private static final String FORMAL_TYPE_FOAM = "GU";
    private static final String FORMAL_SHEET_CHANGE_LOG = "change log（内容）";
    private static final String FORMAL_SHEET_CHANGE_RECORD = "变更记录";
    private static final String FORMAL_SHEET_ZERO = "Zero";
    private static final String FORMAL_SHEET_TRIM = "Trim";
    private static final String FORMAL_SHEET_FOAM = "Foam";
    private static final String FORMAL_MAPPING_ZERO = "Export_MBOM_Zero";
    private static final String FORMAL_MAPPING_TRIM = "Export_MBOM_Trim";
    private static final String FORMAL_MAPPING_FOAM = "Export_MBOM_Foam";
    private static final String FORMAL_MAPPING_CHANGE_RECORD = "MBOM_Change_Record";
    private static final String FORMAL_CONFIG_MBOM_TYPE_CONTROL = "MBOM_Type_Control";
    private static final String FORMAL_CONFIG_TRIM_ROLL_DETAIL = "Detail_Type";
    private static final String FORMAL_USAGE_MAP = "formalUsageMap";
    private static final String FORMAL_AXIS_KEY = "formalAxisKey";
    private static final String FORMAL_ASSEMBLY_AXIS_KEY = "formalAssemblyAxisKey";
    private static final String FORMAL_AXIS_ORDER = "formalAxisOrder";
    private static final String FORMAL_STYLE_TITLE = "formalStyleTitle";
    private static final String FORMAL_COLUMN_INDEX = "formalColumnIndex";
    private static final String FORMAL_POSITION_TITLE = "formalPositionTitle";
    private static final String FORMAL_POSITION_RANK = "formalPositionRank";
    private static final String FORMAL_EBOM_ID = "formalEbomId";
    private static final String FORMAL_OCCURRENCE_KEY = "formalOccurrenceKey";
    private static final String FORMAL_ROOT_AXIS_KEY = "formalRootAxisKey";
    private static final String FORMAL_TRIM_ROLL_REVISION = "AA.1-000";
    private static final int FORMAL_IMPORT_HEADER_CN_ROW = 2;
    private static final int FORMAL_IMPORT_HEADER_EN_ROW = 3;
    private static final int FORMAL_IMPORT_DATA_START_ROW = 4;
    private static final int FORMAL_IMPORT_DOSAGE_SCALE = 6;
    private static final String FORMAL_IMPORT_PART_NUMBER = "attribute[JF_PartNumber]";
    private static final String FORMAL_IMPORT_COLOR_CODE = "JF_InternalColorCode";
    private static final String FORMAL_IMPORT_REVISION = "revision";
    private static final String FORMAL_IMPORT_WIDTH = "attribute[JF_Width]";
    private static final String FORMAL_IMPORT_UTILIZATION = "attribute[JF_Utilizationrate]";
    private static final String FORMAL_IMPORT_ROW_NUMBER = "formalImportRowNumber";
    private static final String FORMAL_IMPORT_KEY = "formalImportKey";
    private static final String FORMAL_IMPORT_TARGETS = "formalImportTargets";
    private static final String FORMAL_IMPORT_NET_AREA = "formalImportNetArea";
    private static final String FORMAL_IMPORT_PART_SUB_TYPE =
            JF_PLMConstants_mxJPO.SELECT_ATTR_JF_VPMReference_JF_PartSubType;
    private static final String FORMAL_IMPORT_TEMPLATE_PACKAGE_SUB_TYPE = "T06";

    /**
     * 正式MBOM导出新入口
     **
     * @param context ENOVIA上下文
     * @param args projectId、MBOM类型（GC/GT/GU）
     * @return Map 导出结果、Workbook及文件名
     * @throws Exception 模板读取或业务数据处理失败时抛出
     * @author caipan by codex
     * @date 2026/8/20 16:13
     */
    public Map exportFormalMBOMExcel(Context context, String[] args) throws Exception {
        Map result = new HashMap();
        boolean pushed = false;
        try {
            String projectId = args[0];
            String mbomType = args[1];
            formalValidateExportType(mbomType);
            ContextUtil.pushContext(context);
            pushed = true;

            String classPath = JF_PublicMethodClass_mxJPO.getBasicUrl(context, new String[]{"Space.WEBINFO.Path"});
            int webInfIndex = classPath.indexOf("WEB-INF");
            String templatePath = JF_ECRService_mxJPO.getTemplatePath(
                    "MBOMExportTemplate.xlsx",
                    classPath.substring(0, webInfIndex));
            JF_LOGGER.info("templatePath:{}", templatePath);
            Workbook workbook;
            try (InputStream inputStream = new FileInputStream(templatePath)) {
                workbook = WorkbookFactory.create(inputStream);
            }

            formalKeepRequiredSheets(workbook, mbomType);
            JF_LOGGER.info("templatePath:{}", templatePath);
            //创建一次导出范围内复用的查询上下文及缓存
            Map exportContext = formalBuildExportContext(context, projectId, mbomType);
            JF_LOGGER.info("exportContext:{}", exportContext);
            MapList fixedMapping = formalGetColumnMapping(context, formalGetMappingName(mbomType));
            JF_LOGGER.info("fixedMapping:{}", fixedMapping);
            Map structureResult;
            if (FORMAL_TYPE_ZERO.equals(mbomType)) {
                structureResult = formalCollectZeroStructure(context, exportContext);
            } else if (FORMAL_TYPE_TRIM.equals(mbomType)) {
                structureResult = formalCollectTrimOrFoamStructure(context, exportContext, FORMAL_TYPE_TRIM);
            } else {
                structureResult = formalCollectTrimOrFoamStructure(context, exportContext, FORMAL_TYPE_FOAM);
            }

            MapList sourceRows = (MapList) structureResult.get("rows");
            MapList sourceAxes = (MapList) structureResult.get("axes");
            if (FORMAL_TYPE_TRIM.equals(mbomType)) {
                //20260821 update by codex 面套严格按实际所属一级总成重定向用量，不使用发泡的公共配置覆盖规则
                Map topAssemblyStructure = formalMergeTrimTopAssemblyStructure(sourceRows, sourceAxes);
                sourceRows = (MapList) topAssemblyStructure.get("rows");
                sourceAxes = (MapList) topAssemblyStructure.get("axes");
            } else if (FORMAL_TYPE_FOAM.equals(mbomType)) {
                //20260821 update by codex 发泡使用一级总成配置轴，并保留公共子件配置覆盖规则
                Map topAssemblyStructure = formalMergeTopAssemblyStructure(sourceRows, sourceAxes);
                sourceRows = (MapList) topAssemblyStructure.get("rows");
                sourceAxes = (MapList) topAssemblyStructure.get("axes");
            } else if (FORMAL_TYPE_ZERO.equals(mbomType)) {
                //20260821 update by codex 整椅在颜色展开前按导出层级、零件号和完整版本统一合并BOM及配置用量
                sourceRows = formalMergeStandardRows(sourceRows);
            }
            MapList exportRows = formalExpandRowsByColor(context, exportContext, sourceRows, false);
            MapList exportAxes = formalExpandRowsByColor(context, exportContext, sourceAxes, true);
            if (FORMAL_TYPE_TRIM.equals(mbomType)) {
                //20260821 update by codex 面套颜色展开后按内部零件号、内部色号和大版本合并BOM及配置用量
                exportRows = formalMergeTrimRows(exportRows);
            }
            if (FORMAL_TYPE_ZERO.equals(mbomType)) {
                //20260824 update by codex 整椅中的面套卷料身份在颜色展开后统一收口，保留不同内部色号行
                exportRows = formalMergeColoredStandardRows(exportRows);
                formalSortZeroAxes(context, exportContext, exportAxes);
            }
            formalRemoveVirtualGXRows(exportRows);
            formalRemoveVirtualGXRows(exportAxes);
            Sheet mbomSheet = workbook.getSheet(formalGetSheetName(mbomType));
            formalWriteMBOMSheet(context, workbook, mbomSheet, mbomType, fixedMapping, exportAxes, exportRows);

            if (FORMAL_TYPE_ZERO.equals(mbomType)) {
                MapList changeRecordMapping = formalGetColumnMapping(context, FORMAL_MAPPING_CHANGE_RECORD);
                formalWriteChangeRecordSheet(context, workbook.getSheet(FORMAL_SHEET_CHANGE_RECORD), projectId, changeRecordMapping);
            }

            DomainObject projectObj = DomainObject.newInstance(context, projectId);
            String projectName = projectObj.getInfo(context, SELECT_NAME);
            String fileName = JF_PublicMethodClass_mxJPO.buildStringInStrings(
                    "MBOM制造物料清单_",
                    projectName,
                    "_",
                    LocalDateTime.now().format(formatter),
                    ".xlsx");
            result.put("file", workbook);
            result.put("flag", "Y");
            result.put("fileName", fileName);
        } catch (Exception e) {
            e.printStackTrace();
            result.put("flag", "N");
            result.put("message", e.getMessage());
            JF_LOGGER.error("exportFormalMBOMExcel error", e.getStackTrace());
        } finally {
            if (pushed) {
                ContextUtil.popContext(context);
            }
        }
        return result;
    }

    /**
     * 校验正式MBOM导出类型
     **
     * @param mbomType GC、GT或GU
     * @throws Exception 类型不支持时抛出
     * @author caipan by codex
     * @date 2026/8/20 16:13
     */
    private void formalValidateExportType(String mbomType) throws Exception {
        if (!FORMAL_TYPE_ZERO.equals(mbomType)
                && !FORMAL_TYPE_TRIM.equals(mbomType)
                && !FORMAL_TYPE_FOAM.equals(mbomType)) {
            throw new IllegalArgumentException("Unsupported MBOM type: " + mbomType);
        }
    }

    /**
     * 根据导出类型保留模板需要的Sheet
     **
     * @param workbook 模板工作簿
     * @param mbomType GC、GT或GU
     * @throws Exception Sheet处理失败时抛出
     * @author caipan by codex
     * @date 2026/8/20 16:13
     */
    private void formalKeepRequiredSheets(Workbook workbook, String mbomType) throws Exception {
        Set<String> keepSheetNames = new LinkedHashSet<>();
        keepSheetNames.add(FORMAL_SHEET_CHANGE_LOG);
        if (FORMAL_TYPE_ZERO.equals(mbomType)) {
            keepSheetNames.add(FORMAL_SHEET_CHANGE_RECORD);
            keepSheetNames.add(FORMAL_SHEET_ZERO);
        } else if (FORMAL_TYPE_TRIM.equals(mbomType)) {
            keepSheetNames.add(FORMAL_SHEET_TRIM);
        } else {
            keepSheetNames.add(FORMAL_SHEET_FOAM);
        }
        for (String keepSheetName : keepSheetNames) {
            if (workbook.getSheet(keepSheetName) == null) {
                throw new Exception("Missing template sheet: " + keepSheetName);
            }
        }
        for (int i = workbook.getNumberOfSheets() - 1; i >= 0; i--) {
            if (!keepSheetNames.contains(workbook.getSheetName(i))) {
                workbook.removeSheetAt(i);
            }
        }
        workbook.setActiveSheet(0);
    }

    /**
     * 创建一次导出范围内复用的查询上下文及缓存
     **
     * @param context ENOVIA上下文
     * @param projectId 项目ID
     * @param mbomType GC、GT或GU
     * @return Map 导出公共数据和查询缓存
     * @throws Exception 项目或MBOM数据不存在时抛出
     * @author caipan by codex
     * @date 2026/8/20 16:13
     */
    private Map formalBuildExportContext(Context context, String projectId, String mbomType) throws Exception {
        DomainObject projectObj = DomainObject.newInstance(context, projectId);
        String topMbomId = projectObj.getInfo(context, "from[JF_relProject2MBOM].to.id");
        if (UIUtil.isNullOrEmpty(topMbomId)) {
            throw new Exception("Project has no MBOM root: " + projectId);
        }
        DomainObject topMbomObj = DomainObject.newInstance(context, topMbomId);
        StringList rootPartIds = topMbomObj.getInfoList(context, "from[JF_relManufacturedItem].to.id");
        StringList relSelects = formalGetMBOMRelationshipSelects();
        String latestMatrixId = formalGetLatestColorMatrixId(context, projectObj, relSelects);

        Map exportContext = new HashMap();
        exportContext.put("projectId", projectId);
        exportContext.put("mbomType", mbomType);
        exportContext.put("projectObj", projectObj);
        exportContext.put("topMbomObj", topMbomObj);
        exportContext.put("rootPartIds", rootPartIds);
        exportContext.put("mbomSelects", formalGetMBOMObjectSelects());
        exportContext.put("relSelects", relSelects);
        exportContext.put("ebomSelects", formalGetEBOMObjectSelects());
        exportContext.put("latestMatrixId", latestMatrixId);
        exportContext.put("colorStyleMap", UIUtil.isNullOrEmpty(latestMatrixId)
                ? new HashMap()
                : formalGetColorStyleMap(context, latestMatrixId, relSelects));
        exportContext.put("expandConfig", formalGetMBOMExpandConfig(context));
        exportContext.put("ebomCache", new HashMap());
        exportContext.put("customerCache", new HashMap());
        exportContext.put("colorGroupCache", new HashMap());
        exportContext.put("pictureCache", new HashMap());
        exportContext.put("positionCache", new HashMap());
        exportContext.put("positionOrder", formalGetPositionOrder(context));
        exportContext.put("imagePath", JF_PublicMethodClass_mxJPO.getBasicUrl(
                context,
                new String[]{"DownloadVPM.Img.Path"}));
        return exportContext;
    }

    /**
     * 获取正式MBOM对象查询字段
     **
     * @return StringList MBOM对象select列表
     * @author caipan by codex
     * @date 2026/8/20 16:13
     */
    private StringList formalGetMBOMObjectSelects() {
        StringList selects = JF_Util_mxJPO.basicBolistSel();
        selects.add(SELECT_PHYSICAL_ID);
        selects.add(SELECT_LEVEL);
        selects.add("attribute[JF_PartNameEN]");
        selects.add("attribute[JF_PartNameCN]");
        selects.add("attribute[JF_PartType]");
        selects.add("attribute[JF_ProcurementType]");
        selects.add("attribute[JF_DirectBuy]");
        selects.add("attribute[JF_Unit]");
        selects.add("attribute[JF_PartDes]");
        selects.add("attribute[JF_PartENDes]");
        selects.add("attribute[JF_Width]");
        selects.add("attribute[JF_Utilizationrate]");
        selects.add("attribute[JF_PartNumber]");
        selects.add("attribute[" + JF_PLMConstants_mxJPO.ATTR_JF_ROLL_PART_NUMBER + "]");
        selects.add("attribute[JF_SpecialProcurementType]");
        return selects;
    }

    /**
     * 获取正式MBOM关系查询字段
     **
     * @return StringList MBOM关系select列表
     * @author caipan by codex
     * @date 2026/8/20 16:13
     */
    private StringList formalGetMBOMRelationshipSelects() {
        StringList selects = JF_Util_mxJPO.basicRellistSel();
        selects.add(SELECT_RELATIONSHIP_ID);
        selects.add(SELECT_FROM_ID);
        selects.add("attribute[JF_Dosage]");
        return selects;
    }

    /**
     * 获取正式MBOM匹配EBOM时需要的字段
     **
     * @return StringList EBOM对象select列表
     * @author caipan by codex
     * @date 2026/8/20 16:13
     */
    private StringList formalGetEBOMObjectSelects() {
        StringList selects = JF_Util_mxJPO.basicBolistSel();
        selects.add(JF_PLMConstants_mxJPO.SELECT_ATTR_V_PART_NUMBER);
        selects.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JF_PartNameCN);
        selects.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JF_PartNameEN);
        selects.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JF_ProcurementType);
        selects.add("attribute[PLMEntity.V_description]");
        selects.add(SELECT_ATTR_JFPartType);
        selects.add(SELECT_PHYSICAL_ID);
        selects.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JF_Lon);
        selects.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JF_Wid);
        selects.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JF_Hig);
        selects.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JF_Weight);
        selects.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JF_WeightTarget);
        selects.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JF_MaterialDensity);
        selects.add("attribute[JF_VPMReference.JF_Detail_CN]");
        selects.add("attribute[JF_VPMReference.JF_Material]");
        selects.add("attribute[Function]");
        selects.add("attribute[NetArea]");
        selects.add("attribute[Circumference]");
        return selects;
    }

    /**
     * 获取导出类型对应的模板Sheet名称
     **
     * @param mbomType GC、GT或GU
     * @return String Sheet名称
     * @author caipan by codex
     * @date 2026/8/20 16:13
     */
    private String formalGetSheetName(String mbomType) {
        if (FORMAL_TYPE_ZERO.equals(mbomType)) {
            return FORMAL_SHEET_ZERO;
        }
        if (FORMAL_TYPE_TRIM.equals(mbomType)) {
            return FORMAL_SHEET_TRIM;
        }
        return FORMAL_SHEET_FOAM;
    }

    /**
     * 获取导出类型对应的XML列配置名称
     **
     * @param mbomType GC、GT或GU
     * @return String XML configuration id
     * @author caipan by codex
     * @date 2026/8/20 16:13
     */
    private String formalGetMappingName(String mbomType) {
        if (FORMAL_TYPE_ZERO.equals(mbomType)) {
            return FORMAL_MAPPING_ZERO;
        }
        if (FORMAL_TYPE_TRIM.equals(mbomType)) {
            return FORMAL_MAPPING_TRIM;
        }
        return FORMAL_MAPPING_FOAM;
    }

    /**
     * 从静态缓存获取指定正式MBOM列配置
     **
     * @param context ENOVIA上下文
     * @param mappingName XML configuration id
     * @return MapList 固定列映射
     * @throws Exception XML读取或配置缺失时抛出
     * @author caipan by codex
     * @date 2026/8/20 16:13
     */
    private MapList formalGetColumnMapping(Context context, String mappingName) throws Exception {
        Map<String, MapList> mappings = formalGetAllColumnMappings(context);
        MapList mapping = mappings.get(mappingName);
        if (mapping == null || mapping.isEmpty()) {
            throw new Exception("Missing MBOM export column mapping: " + mappingName);
        }
        return mapping;
    }

    /**
     * 首次使用时一次解析并缓存三类MBOM及变更记录列配置
     **
     * @param context ENOVIA上下文
     * @return Map XML配置名到固定列映射
     * @throws Exception Page读取或XML解析失败时抛出
     * @author caipan by codex
     * @date 2026/8/20 16:13
     */
    private static Map<String, MapList> formalGetAllColumnMappings(Context context) throws Exception {
        if (FORMAL_COLUMN_MAPPING_INITIALIZED) {
            return FORMAL_COLUMN_MAPPING_CACHE;
        }
        synchronized (FORMAL_COLUMN_MAPPING_LOCK) {
            if (FORMAL_COLUMN_MAPPING_INITIALIZED) {
                return FORMAL_COLUMN_MAPPING_CACHE;
            }
            Set<String> targetNames = new LinkedHashSet<>(Arrays.asList(
                    FORMAL_MAPPING_ZERO,
                    FORMAL_MAPPING_TRIM,
                    FORMAL_MAPPING_FOAM,
                    FORMAL_MAPPING_CHANGE_RECORD));
            Page configPage = new Page("SignTaskProperties_zh.xml");
            configPage.open(context);
            String configContent;
            try {
                configContent = configPage.getContents(context);
            } finally {
                configPage.close(context);
            }
            Map<String, MapList> loadedMappings = new LinkedHashMap<>();
            Set<String> loadedTrimRollDetails = new LinkedHashSet<>();
            try (InputStream inputStream = new ByteArrayInputStream(configContent.getBytes("UTF-8"))) {
                DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
                Document document = builder.parse(inputStream);
                NodeList configurationNodes = document.getElementsByTagName("configuration");
                for (int i = 0; i < configurationNodes.getLength(); i++) {
                    Element configuration = (Element) configurationNodes.item(i);
                    String configurationId = configuration.getAttribute("id");
                    if (FORMAL_CONFIG_MBOM_TYPE_CONTROL.equals(configurationId)) {
                        NodeList configNodes = configuration.getElementsByTagName("config");
                        for (int j = 0; j < configNodes.getLength(); j++) {
                            Element configElement = (Element) configNodes.item(j);
                            if (!FORMAL_CONFIG_TRIM_ROLL_DETAIL.equals(configElement.getAttribute("id"))) {
                                continue;
                            }
                            String configuredDetails = configElement.getAttribute("value");
                            for (String configuredDetail : configuredDetails.split(",")) {
                                if (UIUtil.isNotNullAndNotEmpty(configuredDetail.trim())) {
                                    loadedTrimRollDetails.add(configuredDetail.trim());
                                }
                            }
                        }
                        continue;
                    }
                    if (!targetNames.contains(configurationId)) {
                        continue;
                    }
                    MapList mappingList = new MapList();
                    NodeList configNodes = configuration.getElementsByTagName("config");
                    for (int j = 0; j < configNodes.getLength(); j++) {
                        Element configElement = (Element) configNodes.item(j);
                        Map mapping = new HashMap();
                        mapping.put("id", configElement.getAttribute("id"));
                        mapping.put("value", configElement.getAttribute("value"));
                        mappingList.add(Collections.unmodifiableMap(mapping));
                    }
                    Collections.sort(mappingList, new Comparator() {
                        @Override
                        public int compare(Object first, Object second) {
                            int firstIndex = Integer.parseInt(UIUtil.getValue((Map) first, "value"));
                            int secondIndex = Integer.parseInt(UIUtil.getValue((Map) second, "value"));
                            return Integer.compare(firstIndex, secondIndex);
                        }
                    });
                    loadedMappings.put(configurationId, mappingList);
                }
            }
            if (!loadedMappings.keySet().containsAll(targetNames)) {
                throw new Exception("MBOM export column mapping is incomplete: " + loadedMappings.keySet());
            }
            if (loadedTrimRollDetails.isEmpty()) {
                throw new Exception("Missing MBOM trim roll detail configuration: "
                        + FORMAL_CONFIG_MBOM_TYPE_CONTROL + "." + FORMAL_CONFIG_TRIM_ROLL_DETAIL);
            }
            FORMAL_COLUMN_MAPPING_CACHE.clear();
            FORMAL_COLUMN_MAPPING_CACHE.putAll(loadedMappings);
            FORMAL_TRIM_ROLL_DETAIL_CATEGORY_CACHE.clear();
            FORMAL_TRIM_ROLL_DETAIL_CATEGORY_CACHE.addAll(loadedTrimRollDetails);
            FORMAL_COLUMN_MAPPING_INITIALIZED = true;
            return FORMAL_COLUMN_MAPPING_CACHE;
        }
    }

    /**
     * 首次使用时读取并缓存原MBOM层级展开配置
     **
     * @param context ENOVIA上下文
     * @return Map 零件号前缀到是否完整展开
     * @throws Exception Page读取或XML解析失败时抛出
     * @author caipan by codex
     * @date 2026/8/20 16:13
     */
    private static Map<String, String> formalGetMBOMExpandConfig(Context context) throws Exception {
        if (FORMAL_MBOM_EXPAND_CONFIG_INITIALIZED) {
            return FORMAL_MBOM_EXPAND_CONFIG_CACHE;
        }
        synchronized (FORMAL_MBOM_EXPAND_CONFIG_LOCK) {
            if (FORMAL_MBOM_EXPAND_CONFIG_INITIALIZED) {
                return FORMAL_MBOM_EXPAND_CONFIG_CACHE;
            }
            Page configPage = new Page("MBOMConfigProperties.xml");
            configPage.open(context);
            String configContent;
            try {
                configContent = configPage.getContents(context);
            } finally {
                configPage.close(context);
            }
            Map<String, String> loadedConfig = new HashMap<>();
            try (InputStream inputStream = new ByteArrayInputStream(configContent.getBytes("UTF-8"))) {
                Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(inputStream);
                NodeList categoryNodes = document.getElementsByTagName("Category");
                for (int i = 0; i < categoryNodes.getLength(); i++) {
                    Element category = (Element) categoryNodes.item(i);
                    loadedConfig.put(category.getAttribute("id"), category.getAttribute("expanded"));
                }
            }
            FORMAL_MBOM_EXPAND_CONFIG_CACHE.clear();
            FORMAL_MBOM_EXPAND_CONFIG_CACHE.putAll(loadedConfig);
            FORMAL_MBOM_EXPAND_CONFIG_INITIALIZED = true;
            return FORMAL_MBOM_EXPAND_CONFIG_CACHE;
        }
    }

    /**
     * 查找项目关联的最新已发布颜色矩阵
     **
     * @param context ENOVIA上下文
     * @param projectObj 项目对象
     * @param relSelects 关系查询字段
     * @return String 颜色矩阵ID，未找到时为空
     * @throws Exception 颜色矩阵查询失败时抛出
     * @author caipan by codex
     * @date 2026/8/20 17:05
     */
    private String formalGetLatestColorMatrixId(Context context, DomainObject projectObj, StringList relSelects) throws Exception {
        MapList matrixList = projectObj.getRelatedObjects(
                context,
                "JFProject2JFColorMatrix",
                "JFColorMatrix",
                StringList.create(SELECT_ID, SELECT_CURRENT, SELECT_REVISION),
                relSelects,
                false,
                true,
                (short) 1,
                EMPTY_STRING,
                EMPTY_STRING,
                (short) 0);
        String latestId = EMPTY_STRING;
        String latestRevision = EMPTY_STRING;
        for (Object matrixObject : matrixList) {
            Map matrix = (Map) matrixObject;
            String current = UIUtil.getValue(matrix, SELECT_CURRENT);
            String revision = UIUtil.getValue(matrix, SELECT_REVISION);
            if ("Release".equals(current)
                    && (UIUtil.isNullOrEmpty(latestRevision) || revision.compareTo(latestRevision) > 0)) {
                latestId = UIUtil.getValue(matrix, SELECT_ID);
                latestRevision = revision;
            }
        }
        return latestId;
    }

    /**
     * 按颜色分组标题组装颜色风格映射
     **
     * @param context ENOVIA上下文
     * @param matrixId 颜色矩阵ID
     * @param relSelects 关系查询字段
     * @return Map 颜色分组标题到颜色风格列表
     * @throws Exception 颜色分组或风格查询失败时抛出
     * @author caipan by codex
     * @date 2026/8/20 17:05
     */
    private Map formalGetColorStyleMap(Context context, String matrixId, StringList relSelects) throws Exception {
        DomainObject matrixObj = DomainObject.newInstance(context, matrixId);
        MapList colorGroups = matrixObj.getRelatedObjects(
                context,
                "JFColorMatrix2JFColorGroup",
                "JFColorGroup",
                StringList.create(SELECT_ID, "attribute[Title]"),
                relSelects,
                false,
                true,
                (short) 1,
                EMPTY_STRING,
                EMPTY_STRING,
                (short) 0);
        Map result = new LinkedHashMap();
        for (Object colorGroupObject : colorGroups) {
            Map colorGroup = (Map) colorGroupObject;
            DomainObject colorGroupObj = DomainObject.newInstance(context, UIUtil.getValue(colorGroup, SELECT_ID));
            MapList styles = colorGroupObj.getRelatedObjects(
                    context,
                    "JFColorGroup2JFColorStyle",
                    "JFColorStyle",
                    StringList.create(SELECT_ID, "attribute[Title]", SELECT_NAME),
                    StringList.create(
                            SELECT_RELATIONSHIP_ID,
                            "attribute[JF_InternalColorCode]",
                            "attribute[JF_CustormColorCode]",
                            "attribute[JF_ColorStyleName]"),
                    false,
                    true,
                    (short) 1,
                    EMPTY_STRING,
                    EMPTY_STRING,
                    (short) 0);
            result.put(UIUtil.getValue(colorGroup, "attribute[Title]"), styles);
        }
        return result;
    }

    /**
     * 采集整椅MBOM层级和以整椅为横轴的用量数据
     **
     * @param context ENOVIA上下文
     * @param exportContext 单次导出上下文
     * @return Map rows为展示行，axes为整椅配置列
     * @throws Exception MBOM层级查询失败时抛出
     * @author caipan by codex
     * @date 2026/8/20 17:05
     */
    private Map formalCollectZeroStructure(Context context, Map exportContext) throws Exception {
        MapList rows = new MapList();
        MapList axes = new MapList();
        Map<String, Map> mergedOneLevel = new LinkedHashMap<>();
        Map<String, List<Map>> oneLevelOccurrences = new LinkedHashMap<>();
        StringList rootIds = (StringList) exportContext.get("rootPartIds");
        StringList objectSelects = (StringList) exportContext.get("mbomSelects");
        int axisOrder = 0;
        for (String rootId : rootIds) {
            DomainObject rootObj = DomainObject.newInstance(context, rootId);
            Map root = new HashMap(rootObj.getInfo(context, objectSelects));
            root.put(SELECT_LEVEL, "0");
            String axisKey = "ZERO|" + rootId;
            root.put(FORMAL_AXIS_KEY, axisKey);
            root.put(FORMAL_AXIS_ORDER, axisOrder++);
            root.put(FORMAL_OCCURRENCE_KEY, axisKey);
            formalPutUsage(root, axisKey, BigDecimal.ONE);
            rows.add(root);
            axes.add(formalCopyRow(root));

            MapList directChildren = formalGetMBOMChildren(context, exportContext, rootId, (short) 1);
            MapList effectiveChildren = formalFlattenDirectGX(context, exportContext, directChildren);
            for (Object childObject : effectiveChildren) {
                Map child = formalCopyRow((Map) childObject);
                child.put(SELECT_LEVEL, "1");
                child.put(FORMAL_OCCURRENCE_KEY, formalBuildOccurrenceKey(child));
                BigDecimal dosage = formalDecimal(UIUtil.getValue(child, "attribute[JF_Dosage]"));
                String mergeKey = formalPartRevisionKey(child);
                Map merged = mergedOneLevel.get(mergeKey);
                if (merged == null) {
                    merged = formalCopyRow(child);
                    merged.put(FORMAL_USAGE_MAP, new LinkedHashMap<String, BigDecimal>());
                    mergedOneLevel.put(mergeKey, merged);
                    oneLevelOccurrences.put(mergeKey, new ArrayList<Map>());
                }
                formalPutUsage(merged, axisKey, dosage);
                child.put("formalRootAxisKey", axisKey);
                oneLevelOccurrences.get(mergeKey).add(child);
            }
        }

        for (Map.Entry<String, Map> entry : mergedOneLevel.entrySet()) {
            Map mergedOne = entry.getValue();
            rows.add(mergedOne);
            Map<String, Map> mergedDescendants = new LinkedHashMap<>();
            List<Map> occurrences = oneLevelOccurrences.get(entry.getKey());
            if (CollectionUtils.isNotEmpty(occurrences)) {
                Map occurrence = occurrences.get(0);
                MapList descendants = formalGetZeroDescendants(context, exportContext, occurrence);
                int duplicatedParentLevel = Integer.MAX_VALUE;
                for (Object descendantObject : descendants) {
                    Map descendant = formalCopyRow((Map) descendantObject);
                    int sourceLevel = formalInt(UIUtil.getValue(descendant, SELECT_LEVEL), 0);
                    if (sourceLevel > duplicatedParentLevel) {
                        continue;
                    }
                    duplicatedParentLevel = Integer.MAX_VALUE;
                    descendant.put(SELECT_LEVEL, String.valueOf(sourceLevel + 1));
                    descendant.put(FORMAL_OCCURRENCE_KEY, formalBuildOccurrenceKey(descendant));
                    String mergeKey = formalPartRevisionParentKey(descendant);
                    Map mergedDescendant = mergedDescendants.get(mergeKey);
                    if (mergedDescendant == null) {
                        mergedDescendant = formalCopyRow(descendant);
                        mergedDescendant.put(FORMAL_USAGE_MAP, new LinkedHashMap<String, BigDecimal>());
                        mergedDescendants.put(mergeKey, mergedDescendant);
                    } else {
                        duplicatedParentLevel = sourceLevel;
                    }
                    BigDecimal dosage = formalDecimal(UIUtil.getValue(descendant, "attribute[JF_Dosage]"));
                    Map<String, BigDecimal> oneLevelUsages = (Map<String, BigDecimal>) mergedOne.get(FORMAL_USAGE_MAP);
                    if (oneLevelUsages != null) {
                        for (String rootAxisKey : oneLevelUsages.keySet()) {
                            formalPutUsage(mergedDescendant, rootAxisKey, dosage);
                        }
                    }
                }
            }
            rows.addAll(mergedDescendants.values());
        }
        Map result = new HashMap();
        result.put("rows", rows);
        result.put("axes", axes);
        return result;
    }

    /**
     * 采集面套或发泡MBOM，排除整椅并从一级GT/GU总成开始展示
     **
     * @param context ENOVIA上下文
     * @param exportContext 单次导出上下文
     * @param targetType GT面套或GU发泡
     * @return Map rows为展示行，axes为面套或发泡一级总成
     * @throws Exception MBOM层级查询失败时抛出
     * @author caipan by codex
     * @date 2026/8/20 17:05
     */
    private Map formalCollectTrimOrFoamStructure(Context context, Map exportContext, String targetType) throws Exception {
        MapList rows = new MapList();
        MapList axes = new MapList();
        StringList rootIds = (StringList) exportContext.get("rootPartIds");
        int axisOrder = 0;
        for (String rootId : rootIds) {
            MapList directChildren = formalGetMBOMChildren(context, exportContext, rootId, (short) 1);
            MapList effectiveChildren = formalFlattenDirectGX(context, exportContext, directChildren);
            for (Object childObject : effectiveChildren) {
                Map assembly = formalCopyRow((Map) childObject);
                String partNumber = UIUtil.getValue(assembly, "attribute[JF_PartNumber]");
                if (!partNumber.startsWith(targetType)) {
                    continue;
                }
                String assemblyAxisKey = "ASSEMBLY|" + formalBuildOccurrenceKey(assembly);
                assembly.put(SELECT_LEVEL, "1");
                assembly.put(FORMAL_OCCURRENCE_KEY, formalBuildOccurrenceKey(assembly));
                assembly.put(FORMAL_ASSEMBLY_AXIS_KEY, assemblyAxisKey);
                //20260821 update by codex 保留一级总成所在整椅配置，用于还原旧导出的配置覆盖关系
                assembly.put(FORMAL_ROOT_AXIS_KEY, rootId);
                //20260821 update by codex 面套和发泡配置头均使用一级总成，不再将发泡下层Make件作为配置轴
                assembly.put(FORMAL_AXIS_KEY, assemblyAxisKey);
                assembly.put(FORMAL_AXIS_ORDER, axisOrder++);
                formalPutUsage(assembly, assemblyAxisKey, BigDecimal.ONE);
                axes.add(formalCopyRow(assembly));
                rows.add(assembly);

                MapList descendants = formalGetMBOMChildren(
                        context,
                        exportContext,
                        UIUtil.getValue(assembly, SELECT_ID),
                        (short) 0);
                for (Object descendantObject : descendants) {
                    Map descendant = formalCopyRow((Map) descendantObject);
                    int level = formalInt(UIUtil.getValue(descendant, SELECT_LEVEL), 0) + 1;
                    descendant.put(SELECT_LEVEL, String.valueOf(level));
                    String occurrenceKey = formalBuildOccurrenceKey(descendant);
                    descendant.put(FORMAL_OCCURRENCE_KEY, occurrenceKey);
                    descendant.put(FORMAL_ASSEMBLY_AXIS_KEY, assemblyAxisKey);
                    descendant.put(FORMAL_ROOT_AXIS_KEY, rootId);
                    BigDecimal dosage = formalDecimal(UIUtil.getValue(descendant, "attribute[JF_Dosage]"));
                    //20260821 update by codex 所有后代用量均归属其一级总成配置列，同列重复由后续BOM合并累加
                    formalPutUsage(descendant, assemblyAxisKey, dosage);
                    rows.add(descendant);
                }
            }
        }
        Map result = new HashMap();
        result.put("rows", rows);
        result.put("axes", axes);
        return result;
    }

    /**
     * 查询指定MBOM节点的直接子级或全部后代
     **
     * @param context ENOVIA上下文
     * @param exportContext 单次导出上下文
     * @param parentId 父节点ID
     * @param level 1为直接子级，0为全层级
     * @return MapList MBOM节点
     * @throws Exception MBOM查询失败时抛出
     * @author caipan by codex
     * @date 2026/8/20 17:05
     */
    private MapList formalGetMBOMChildren(Context context, Map exportContext, String parentId, short level) throws Exception {
        DomainObject parentObj = DomainObject.newInstance(context, parentId);
        return parentObj.getRelatedObjects(
                context,
                JF_PLMConstants_mxJPO.REL_JF_relManufacturedItem,
                JF_PLMConstants_mxJPO.TYPE_JF_ManufacturedItem,
                (StringList) exportContext.get("mbomSelects"),
                (StringList) exportContext.get("relSelects"),
                false,
                true,
                level,
                EMPTY_STRING,
                EMPTY_STRING,
                (short) 0);
    }

    /**
     * 将整椅直接子级中的GX虚拟层替换为GX的直接子级
     **
     * @param context ENOVIA上下文
     * @param exportContext 单次导出上下文
     * @param directChildren 整椅直接子级
     * @return MapList 穿透GX后的一级节点
     * @throws Exception GX子级查询失败时抛出
     * @author caipan by codex
     * @date 2026/8/20 17:05
     */
    private MapList formalFlattenDirectGX(Context context, Map exportContext, MapList directChildren) throws Exception {
        MapList result = new MapList();
        for (Object childObject : directChildren) {
            Map child = (Map) childObject;
            String partNumber = UIUtil.getValue(child, "attribute[JF_PartNumber]");
            if (partNumber.startsWith("GX")) {
                result.addAll(formalGetMBOMChildren(
                        context,
                        exportContext,
                        UIUtil.getValue(child, SELECT_ID),
                        (short) 1));
            } else {
                result.add(child);
            }
        }
        return result;
    }

    /**
     * 按原导出规则获取整椅一级件下需导出的节点
     **
     * @param context ENOVIA上下文
     * @param exportContext 单次导出上下文
     * @param oneLevel 整椅一级件
     * @return MapList 筛选后的后代节点
     * @throws Exception MBOM查询失败时抛出
     * @author caipan by codex
     * @date 2026/8/20 17:05
     */
    private MapList formalGetZeroDescendants(Context context, Map exportContext, Map oneLevel) throws Exception {
        String partNumber = UIUtil.getValue(oneLevel, "attribute[JF_PartNumber]");
        String prefix = partNumber.length() >= 2 ? partNumber.substring(0, 2) : partNumber;
        Map expandConfig = (Map) exportContext.get("expandConfig");
        if (!"false".equals(expandConfig.get(prefix))) {
            return formalGetMBOMChildren(
                    context,
                    exportContext,
                    UIUtil.getValue(oneLevel, SELECT_ID),
                    (short) 0);
        }
        return formalGetSpecialZeroDescendants(context, exportContext, oneLevel);
    }

    /**
     * 保留原逻辑中make二级件和特殊采购节点的向上路径
     **
     * @param context ENOVIA上下文
     * @param exportContext 单次导出上下文
     * @param oneLevel 整椅一级件
     * @return MapList 特殊展开结果
     * @throws Exception MBOM查询失败时抛出
     * @author caipan by codex
     * @date 2026/8/20 17:05
     */
    private MapList formalGetSpecialZeroDescendants(Context context, Map exportContext, Map oneLevel) throws Exception {
        MapList allDescendants = formalGetMBOMChildren(
                context,
                exportContext,
                UIUtil.getValue(oneLevel, SELECT_ID),
                (short) 0);
        Map<String, Map> occurrenceByObjectId = new LinkedHashMap<>();
        for (Object descendantObject : allDescendants) {
            Map descendant = (Map) descendantObject;
            occurrenceByObjectId.put(UIUtil.getValue(descendant, SELECT_ID), descendant);
        }
        Set<String> retainedIds = new LinkedHashSet<>();
        String oneLevelId = UIUtil.getValue(oneLevel, SELECT_ID);
        for (Object descendantObject : allDescendants) {
            Map descendant = (Map) descendantObject;
            String specialType = UIUtil.getValue(descendant, "attribute[JF_SpecialProcurementType]");
            if (!"DualDistribution".equals(specialType) && !"outsource".equals(specialType)) {
                continue;
            }
            Map current = descendant;
            while (current != null) {
                retainedIds.add(UIUtil.getValue(current, SELECT_ID));
                String parentId = UIUtil.getValue(current, SELECT_FROM_ID);
                if (UIUtil.isNullOrEmpty(parentId) || oneLevelId.equals(parentId)) {
                    break;
                }
                current = occurrenceByObjectId.get(parentId);
            }
        }
        String procurementType = UIUtil.getValue(oneLevel, "attribute[JF_ProcurementType]");
        String partNumber = UIUtil.getValue(oneLevel, "attribute[JF_PartNumber]");
        if ("make".equalsIgnoreCase(procurementType)
                && !partNumber.startsWith(FORMAL_TYPE_TRIM)
                && !partNumber.startsWith(FORMAL_TYPE_FOAM)) {
            MapList directChildren = formalGetMBOMChildren(context, exportContext, oneLevelId, (short) 1);
            for (Object directChildObject : directChildren) {
                retainedIds.add(UIUtil.getValue((Map) directChildObject, SELECT_ID));
            }
        }
        MapList result = new MapList();
        for (Object descendantObject : allDescendants) {
            Map descendant = (Map) descendantObject;
            if (retainedIds.contains(UIUtil.getValue(descendant, SELECT_ID))) {
                result.add(descendant);
            }
        }
        return result;
    }

    /**
     * 创建包含关系维度的MBOM发生项键
     **
     * @param row MBOM行
     * @return String 发生项键
     * @author caipan by codex
     * @date 2026/8/20 17:05
     */
    private String formalBuildOccurrenceKey(Map row) {
        String relationshipId = UIUtil.getValue(row, SELECT_RELATIONSHIP_ID);
        if (UIUtil.isNotNullAndNotEmpty(relationshipId)) {
            return relationshipId;
        }
        return UIUtil.getValue(row, SELECT_FROM_ID) + "|" + UIUtil.getValue(row, SELECT_ID);
    }

    /**
     * 创建一级件的零件号和版本合并键
     **
     * @param row MBOM行
     * @return String 合并键
     * @author caipan by codex
     * @date 2026/8/20 17:05
     */
    private String formalPartRevisionKey(Map row) {
        return UIUtil.getValue(row, "attribute[JF_PartNumber]") + "|" + UIUtil.getValue(row, SELECT_REVISION);
    }

    /**
     * 创建带父级维度的子件合并键
     **
     * @param row MBOM行
     * @return String 合并键
     * @author caipan by codex
     * @date 2026/8/20 17:05
     */
    private String formalPartRevisionParentKey(Map row) {
        return UIUtil.getValue(row, SELECT_FROM_ID)
                + "|"
                + UIUtil.getValue(row, "attribute[JF_PartNumber]")
                + "|"
                + UIUtil.getValue(row, SELECT_REVISION);
    }

    /**
     * 判断MBOM节点是否为自制件
     **
     * @param row MBOM行
     * @return boolean make时为true
     * @author caipan by codex
     * @date 2026/8/20 17:05
     */
    private boolean formalIsMake(Map row) {
        return "make".equalsIgnoreCase(UIUtil.getValue(row, "attribute[JF_ProcurementType]"));
    }

    /**
     * 深复制一行及其用量映射，避免合并时污染源数据
     **
     * @param source 源行
     * @return Map 行副本
     * @author caipan by codex
     * @date 2026/8/20 17:05
     */
    private Map formalCopyRow(Map source) {
        Map copy = new HashMap(source);
        Map usage = (Map) source.get(FORMAL_USAGE_MAP);
        if (usage != null) {
            copy.put(FORMAL_USAGE_MAP, new LinkedHashMap(usage));
        }
        return copy;
    }

    /**
     * 按配置列键累加行用量
     **
     * @param row MBOM行
     * @param axisKey 配置列键
     * @param dosage 待累加用量
     * @author caipan by codex
     * @date 2026/8/20 17:05
     */
    private void formalPutUsage(Map row, String axisKey, BigDecimal dosage) {
        if (UIUtil.isNullOrEmpty(axisKey)) {
            return;
        }
        Map<String, BigDecimal> usageMap = (Map<String, BigDecimal>) row.get(FORMAL_USAGE_MAP);
        if (usageMap == null) {
            usageMap = new LinkedHashMap<>();
            row.put(FORMAL_USAGE_MAP, usageMap);
        }
        BigDecimal current = usageMap.get(axisKey);
        usageMap.put(axisKey, (current == null ? BigDecimal.ZERO : current).add(dosage));
    }

    /**
     * 将用量文本安全转换为BigDecimal
     **
     * @param value 用量文本
     * @return BigDecimal 空值或非数值返回0
     * @author caipan by codex
     * @date 2026/8/20 17:05
     */
    private BigDecimal formalDecimal(String value) {
        try {
            return UIUtil.isNullOrEmpty(value) ? BigDecimal.ZERO : new BigDecimal(value.trim());
        } catch (NumberFormatException e) {
            return BigDecimal.ZERO;
        }
    }

    /**
     * 将文本安全转换为整数
     **
     * @param value 整数文本
     * @param defaultValue 默认值
     * @return int 转换结果
     * @author caipan by codex
     * @date 2026/8/20 17:05
     */
    private int formalInt(String value, int defaultValue) {
        try {
            return Integer.parseInt(value);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    /**
     * 将MBOM行补齐EBOM、客户件、DB、图片和颜色属性后按颜色拆行
     **
     * @param context ENOVIA上下文
     * @param exportContext 单次导出上下文
     * @param sourceRows 结构行或配置轴行
     * @param axisRows 是否为配置轴行
     * @return MapList 按内部色号展开的行
     * @throws Exception EBOM、颜色或项目关系查询失败时抛出
     * @author caipan by codex
     * @date 2026/8/20 17:42
     */
    private MapList formalExpandRowsByColor(Context context, Map exportContext, MapList sourceRows, boolean axisRows) throws Exception {
        MapList result = new MapList();
        for (Object sourceObject : sourceRows) {
            Map source = formalCopyRow((Map) sourceObject);
            Map ebomInfo = formalFindEBOMInfo(context, exportContext, source);
            if (MapUtils.isNotEmpty(ebomInfo)) {
                formalApplyEBOMAttributes(context, exportContext, source, ebomInfo);
                formalApplyRollPartIdentity(context, exportContext, source, ebomInfo);
            }
            MapList variants = formalGetColorVariants(context, exportContext, source, ebomInfo);
            int styleOrder = 0;
            for (Object variantObject : variants) {
                Map variant = (Map) variantObject;
                Map expanded = formalCopyRow(source);
                expanded.putAll(variant);
                expanded.put("formalStyleOrder", styleOrder++);
                expanded.put("InternalFullPartNumber", formalBuildInternalFullPartNumber(expanded));
                if (axisRows && UIUtil.isNullOrEmpty(UIUtil.getValue(expanded, FORMAL_AXIS_KEY))) {
                    continue;
                }
                result.add(expanded);
            }
        }
        return result;
    }

    /**
     * 样板包、松紧带、嵌条在正式导出时优先使用系统中存在的卷料号和AA.1-000版本。
     * 仅修改导出行身份；EBOM属性、颜色和原MBOM对象关系仍按原零件获取。
     */
    private void formalApplyRollPartIdentity(Context context, Map exportContext, Map row,
                                               Map sourceEbomInfo) throws Exception {
        String exportType = UIUtil.getValue(exportContext, "mbomType");
        if (!FORMAL_TYPE_TRIM.equals(exportType) && !FORMAL_TYPE_ZERO.equals(exportType)) {
            return;
        }
        String detailCategory = UIUtil.getValue(
                sourceEbomInfo, JF_PLMConstants_mxJPO.SELECT_ATTR_JF_Detail_CN).trim();
        formalGetAllColumnMappings(context);
        if (!FORMAL_TRIM_ROLL_DETAIL_CATEGORY_CACHE.contains(detailCategory)) {
            return;
        }
        String rollPartNumber = UIUtil.getValue(
                row, "attribute[" + JF_PLMConstants_mxJPO.ATTR_JF_ROLL_PART_NUMBER + "]").trim();
        if (UIUtil.isNullOrEmpty(rollPartNumber)) {
            return;
        }

        Map rollLookup = new HashMap();
        rollLookup.put("attribute[JF_PartNumber]", rollPartNumber);
        rollLookup.put(SELECT_REVISION, FORMAL_TRIM_ROLL_REVISION);
        Map rollPartInfo = formalFindEBOMInfo(context, exportContext, rollLookup);
        if (MapUtils.isEmpty(rollPartInfo)) {
            return;
        }
        String existingRollPartNumber = UIUtil.getValue(
                rollPartInfo, JF_PLMConstants_mxJPO.SELECT_ATTR_V_PART_NUMBER);
        row.put("attribute[JF_PartNumber]", formalDefaultValue(existingRollPartNumber, rollPartNumber));
        row.put(SELECT_REVISION, FORMAL_TRIM_ROLL_REVISION);
    }

    /**
     * 按MBOM零件号和完整版本查找对应VPMReference，并在单次导出内缓存
     **
     * @param context ENOVIA上下文
     * @param exportContext 单次导出上下文
     * @param row MBOM行
     * @return Map 对应EBOM对象数据，未匹配时为空Map
     * @throws Exception VPMReference查询失败时抛出
     * @author caipan by codex
     * @date 2026/8/20 17:42
     */
    private Map formalFindEBOMInfo(Context context, Map exportContext, Map row) throws Exception {
        String partNumber = UIUtil.getValue(row, "attribute[JF_PartNumber]");
        String revision = UIUtil.getValue(row, SELECT_REVISION);
        String cacheKey = partNumber + "|" + revision;
        Map cache = (Map) exportContext.get("ebomCache");
        if (cache.containsKey(cacheKey)) {
            return (Map) cache.get(cacheKey);
        }
        String safePartNumber = partNumber.replace("'", "\\'");
        String safeRevision = revision.replace("'", "\\'");
        String where = "attribute[EnterpriseExtension.V_PartNumber]=='"
                + safePartNumber
                + "'&&revision=='"
                + safeRevision
                + "'";
        MapList matches = DomainObject.findObjects(
                context,
                "VPMReference",
                "*",
                where,
                (StringList) exportContext.get("ebomSelects"));
        Map value = matches.isEmpty() ? Collections.emptyMap() : new HashMap((Map) matches.get(0));
        cache.put(cacheKey, value);
        return value;
    }

    /**
     * 将EBOM及项目关系属性别名转换到MBOM导出行
     **
     * @param context ENOVIA上下文
     * @param exportContext 单次导出上下文
     * @param row 待补齐MBOM行
     * @param ebomInfo 对应VPMReference属性
     * @throws Exception 客户件、DB或图片查询失败时抛出
     * @author caipan by codex
     * @date 2026/8/20 17:42
     */
    private void formalApplyEBOMAttributes(Context context, Map exportContext, Map row, Map ebomInfo) throws Exception {
        String ebomId = UIUtil.getValue(ebomInfo, SELECT_ID);
        String physicalId = UIUtil.getValue(ebomInfo, SELECT_PHYSICAL_ID);
        row.put(FORMAL_EBOM_ID, ebomId);
        formalCopyValue(ebomInfo, row, JF_PLMConstants_mxJPO.SELECT_ATTR_JF_Lon, "attribute[JFLength]");
        formalCopyValue(ebomInfo, row, JF_PLMConstants_mxJPO.SELECT_ATTR_JF_Wid, "attribute[JFWidth]");
        formalCopyValue(ebomInfo, row, JF_PLMConstants_mxJPO.SELECT_ATTR_JF_Hig, "attribute[JFHeight]");
        formalCopyValue(ebomInfo, row, JF_PLMConstants_mxJPO.SELECT_ATTR_JF_Weight, "attribute[JF_VPMReference.JF_Weight]");
        formalCopyValue(ebomInfo, row, JF_PLMConstants_mxJPO.SELECT_ATTR_JF_WeightTarget, "attribute[JF_VPMReference.JF_WeightTarget]");
        formalCopyValue(ebomInfo, row, JF_PLMConstants_mxJPO.SELECT_ATTR_JF_MaterialDensity, "attribute[JF_VPMReference.JF_MaterialDensity]");
        formalCopyValue(ebomInfo, row, "attribute[JF_VPMReference.JF_Detail_CN]", "attribute[JF_VPMReference.JF_Detail_CN]");
        formalCopyValue(ebomInfo, row, "attribute[JF_VPMReference.JF_Material]", "attribute[JF_VPMReference.JF_Material]");
        formalCopyValue(ebomInfo, row, "attribute[Function]", "attribute[Function]");
        formalCopyValue(ebomInfo, row, "attribute[NetArea]", "attribute[NetArea]");
        formalCopyValue(ebomInfo, row, "attribute[Circumference]", "attribute[Circumference]");

        Map customerCache = (Map) exportContext.get("customerCache");
        Map customerInfo = (Map) customerCache.get(ebomId);
        if (customerInfo == null) {
            customerInfo = new JF_DR_mxJPO().getDRCustomerPartsDBRelationInfo(
                    context,
                    ebomId,
                    UIUtil.getValue(exportContext, "projectId"));
            if (customerInfo == null) {
                customerInfo = Collections.emptyMap();
            }
            customerCache.put(ebomId, customerInfo);
        }
        row.put("attribute[CustomerPartNumber]", UIUtil.getValue(customerInfo, Select_Attr_JFCustomerPartNumber));
        row.put("attribute[CustomerPartRevision]", UIUtil.getValue(customerInfo, Select_Attr_JFCustomerPartRevision));
        row.put(
                "attribute[JF_DirectBuy]",
                JF_Util_mxJPO.getPartDirectBuyByProjectWithNonDBDefault(
                        context,
                        ebomId,
                        UIUtil.getValue(exportContext, "projectId")));

        Map pictureCache = (Map) exportContext.get("pictureCache");
        if (!pictureCache.containsKey(physicalId)) {
            String picture = new JF_ExportEBOM_mxJPO().getDownLoadPictureByPartId(
                    context,
                    physicalId,
                    UIUtil.getValue(exportContext, "imagePath"));
            pictureCache.put(physicalId, picture == null ? EMPTY_STRING : picture);
        }
        row.put("picture", pictureCache.get(physicalId));
    }

    /**
     * 复制属性值并允许输出别名
     **
     * @param source 源Map
     * @param target 目标Map
     * @param sourceKey 源属性key
     * @param targetKey 导出属性key
     * @author caipan by codex
     * @date 2026/8/20 17:42
     */
    private void formalCopyValue(Map source, Map target, String sourceKey, String targetKey) {
        target.put(targetKey, UIUtil.getValue(source, sourceKey));
    }

    /**
     * 获取零件在当前项目颜色组下的所有颜色风格
     **
     * @param context ENOVIA上下文
     * @param exportContext 单次导出上下文
     * @param row MBOM行
     * @param ebomInfo 对应VPMReference信息
     * @return MapList 颜色变体，无颜色时返回000默认变体
     * @throws Exception 颜色组查询失败时抛出
     * @author caipan by codex
     * @date 2026/8/20 17:42
     */
    private MapList formalGetColorVariants(Context context, Map exportContext, Map row, Map ebomInfo) throws Exception {
        MapList variants = new MapList();
        if (MapUtils.isEmpty(ebomInfo) || UIUtil.isNullOrEmpty(UIUtil.getValue(exportContext, "latestMatrixId"))) {
            variants.add(formalDefaultColorVariant());
            return variants;
        }
        String ebomId = UIUtil.getValue(ebomInfo, SELECT_ID);
        String colorGroupName = formalGetProjectColorGroupName(context, exportContext, ebomId);
        Map colorStyleMap = (Map) exportContext.get("colorStyleMap");
        MapList styles = (MapList) colorStyleMap.get(colorGroupName);
        if (CollectionUtils.isEmpty(styles)) {
            variants.add(formalDefaultColorVariant());
            return variants;
        }
        Map<String, Map> uniqueByInternalCode = new LinkedHashMap<>();
        for (Object styleObject : styles) {
            Map style = (Map) styleObject;
            String internalCode = UIUtil.getValue(style, "attribute[JF_InternalColorCode]");
            if (UIUtil.isNullOrEmpty(internalCode)) {
                internalCode = "000";
            }
            if (uniqueByInternalCode.containsKey(internalCode)) {
                continue;
            }
            Map variant = new HashMap();
            variant.put("JF_InternalColorCode", internalCode);
            variant.put("JF_CustormColorCode", formalDefaultValue(
                    UIUtil.getValue(style, "attribute[JF_CustormColorCode]"),
                    "000"));
            variant.put("JF_ColorStyleName", UIUtil.getValue(style, "attribute[JF_ColorStyleName]"));
            variant.put(FORMAL_STYLE_TITLE, UIUtil.getValue(style, "attribute[Title]"));
            uniqueByInternalCode.put(internalCode, variant);
        }
        variants.addAll(uniqueByInternalCode.values());
        if (variants.isEmpty()) {
            variants.add(formalDefaultColorVariant());
        }
        return variants;
    }

    /**
     * 查找零件在当前项目下的颜色分组，未维护时按NA处理
     **
     * @param context ENOVIA上下文
     * @param exportContext 单次导出上下文
     * @param ebomId VPMReference ID
     * @return String 颜色分组名
     * @throws Exception 颜色组查询失败时抛出
     * @author caipan by codex
     * @date 2026/8/20 17:42
     */
    private String formalGetProjectColorGroupName(Context context, Map exportContext, String ebomId) throws Exception {
        Map colorGroupCache = (Map) exportContext.get("colorGroupCache");
        if (colorGroupCache.containsKey(ebomId)) {
            return UIUtil.getValue(colorGroupCache, ebomId);
        }
        DomainObject ebomObj = DomainObject.newInstance(context, ebomId);
        MapList projectRelations = ebomObj.getRelatedObjects(
                context,
                "JFProject2ColorGroup",
                "Project Space",
                StringList.create(SELECT_ID),
                StringList.create(
                        SELECT_RELATIONSHIP_ID,
                        "attribute[JF_ColorGroupName]",
                        "attribute[JF_ColorMatrixName]"),
                true,
                false,
                (short) 1,
                EMPTY_STRING,
                EMPTY_STRING,
                (short) 0);
        String projectId = UIUtil.getValue(exportContext, "projectId");
        String colorGroupName = EMPTY_STRING;
        for (Object projectRelationObject : projectRelations) {
            Map projectRelation = (Map) projectRelationObject;
            if (projectId.equals(UIUtil.getValue(projectRelation, SELECT_ID))) {
                colorGroupName = UIUtil.getValue(projectRelation, "attribute[JF_ColorGroupName]");
                break;
            }
        }
        if (UIUtil.isNullOrEmpty(colorGroupName)) {
            colorGroupName = "NA";
        }
        colorGroupCache.put(ebomId, colorGroupName);
        return colorGroupName;
    }

    /**
     * 创建无颜色矩阵或未匹配颜色组时的000默认变体
     **
     * @return Map 默认颜色变体
     * @author caipan by codex
     * @date 2026/8/20 17:42
     */
    private Map formalDefaultColorVariant() {
        Map variant = new HashMap();
        variant.put("JF_InternalColorCode", "000");
        variant.put("JF_CustormColorCode", "000");
        variant.put("JF_ColorStyleName", EMPTY_STRING);
        variant.put(FORMAL_STYLE_TITLE, EMPTY_STRING);
        return variant;
    }

    /**
     * 构造内部完整零件号：去首字符零件号+内部色号+大版本+0
     **
     * @param row 已展开颜色的MBOM行
     * @return String 内部完整零件号
     * @author caipan by codex
     * @date 2026/8/20 17:42
     */
    private String formalBuildInternalFullPartNumber(Map row) {
        String partNumber = UIUtil.getValue(row, "attribute[JF_PartNumber]");
        String colorCode = formalDefaultValue(UIUtil.getValue(row, "JF_InternalColorCode"), "000");
        String majorRevision = formalGetMajorRevision(UIUtil.getValue(row, SELECT_REVISION));
        String numberWithoutPrefix = partNumber.length() > 1 ? partNumber.substring(1) : partNumber;
        return numberWithoutPrefix + colorCode + majorRevision + "0";
    }

    /**
     * 获取版本号小数点前的大版本
     **
     * @param revision 完整版本号
     * @return String 大版本
     * @author caipan by codex
     * @date 2026/8/20 17:42
     */
    private String formalGetMajorRevision(String revision) {
        int dotIndex = revision.indexOf('.');
        return dotIndex < 0 ? revision : revision.substring(0, dotIndex);
    }

    /**
     * 空值时返回默认文本
     **
     * @param value 原文本
     * @param defaultValue 默认文本
     * @return String 非空原值或默认值
     * @author caipan by codex
     * @date 2026/8/20 17:42
     */
    private String formalDefaultValue(String value, String defaultValue) {
        return UIUtil.isNullOrEmpty(value) ? defaultValue : value;
    }

    /**
     * 按面套唯一键合并最终BOM行，并按配置列累加用量
     **
     * @param expandedRows 颜色展开后的面套行
     * @return MapList 每个内部零件号、内部色号和大版本仅保留首行的面套结果
     * @author caipan by codex
     * @date 2026/8/21 16:10
     */
    private MapList formalMergeTrimRows(MapList expandedRows) {
        Map<String, Map> mergedByFullNumber = new LinkedHashMap<>();
        for (Object rowObject : expandedRows) {
            Map row = (Map) rowObject;
            String internalFullPartNumber = UIUtil.getValue(row, "InternalFullPartNumber");
            String partColorRevisionKey = UIUtil.isNotNullAndNotEmpty(internalFullPartNumber)
                    ? internalFullPartNumber
                    : formalBuildInternalFullPartNumber(row);
            //20260821 update by codex 面套在相同导出层级内按内部零件号、内部色号和大版本判定唯一
            String mergeKey = "LEVEL|"
                    + formalInt(UIUtil.getValue(row, SELECT_LEVEL), 0)
                    + "|"
                    + partColorRevisionKey;
            Map merged = mergedByFullNumber.get(mergeKey);
            if (merged == null) {
                mergedByFullNumber.put(mergeKey, formalCopyRow(row));
                continue;
            }
            Map<String, BigDecimal> usages = (Map<String, BigDecimal>) row.get(FORMAL_USAGE_MAP);
            if (usages != null) {
                for (Map.Entry<String, BigDecimal> usage : usages.entrySet()) {
                    formalPutUsage(merged, usage.getKey(), usage.getValue());
                }
            }
        }
        MapList result = new MapList();
        result.addAll(mergedByFullNumber.values());
        return result;
    }

    /**
     * 按旧导出结果的层级范围合并整椅或发泡结构行，并按配置列累加用量
     **
     * @param sourceRows 整椅或发泡结构行
     * @return MapList 保持原顺序并累加配置用量的行
     * @author caipan by codex
     * @date 2026/8/21 16:52
     */
    private MapList formalMergeStandardRows(MapList sourceRows) {
        Map<String, Map> mergedByKey = new LinkedHashMap<>();
        for (Object rowObject : sourceRows) {
            Map row = (Map) rowObject;
            String key = formalBuildStandardMergeKey(row);
            Map merged = mergedByKey.get(key);
            if (merged == null) {
                mergedByKey.put(key, formalCopyRow(row));
                continue;
            }
            Map<String, BigDecimal> usages = (Map<String, BigDecimal>) row.get(FORMAL_USAGE_MAP);
            if (usages != null) {
                for (Map.Entry<String, BigDecimal> usage : usages.entrySet()) {
                    formalPutUsage(merged, usage.getKey(), usage.getValue());
                }
            }
        }
        MapList result = new MapList();
        result.addAll(mergedByKey.values());
        return result;
    }

    /**
     * 对颜色展开后的整椅行按当前导出身份收口合并。卷料替换后的不同原零件在零件号、
     * 完整版本、内部色号和层级相同时合并，并按各整椅配置列累加用量。
     */
    private MapList formalMergeColoredStandardRows(MapList expandedRows) {
        Map<String, Map> mergedByKey = new LinkedHashMap<>();
        for (Object rowObject : expandedRows) {
            Map row = (Map) rowObject;
            int level = formalInt(UIUtil.getValue(row, SELECT_LEVEL), 0);
            String colorCode = formalDefaultValue(
                    UIUtil.getValue(row, "JF_InternalColorCode"), "000");
            String key;
            if (level <= 0) {
                key = "ROOT|" + UIUtil.getValue(row, FORMAL_OCCURRENCE_KEY) + "|" + colorCode;
            } else {
                key = "LEVEL|" + level + "|" + formalPartRevisionKey(row) + "|" + colorCode;
            }
            Map merged = mergedByKey.get(key);
            if (merged == null) {
                mergedByKey.put(key, formalCopyRow(row));
                continue;
            }
            Map<String, BigDecimal> usages = (Map<String, BigDecimal>) row.get(FORMAL_USAGE_MAP);
            if (usages != null) {
                for (Map.Entry<String, BigDecimal> usage : usages.entrySet()) {
                    formalPutUsage(merged, usage.getKey(), usage.getValue());
                }
            }
        }
        MapList result = new MapList();
        result.addAll(mergedByKey.values());
        return result;
    }

    /**
     * 合并面套一级总成配置轴和BOM行，用量只归属实际所在的一级总成配置列
     **
     * @param sourceRows 面套结构行
     * @param sourceAxes 面套一级总成配置轴
     * @return Map rows为合并后的面套结构行，axes为合并后的一级总成配置轴
     * @author caipan by codex
     * @date 2026/8/21 19:05
     */
    private Map formalMergeTrimTopAssemblyStructure(MapList sourceRows, MapList sourceAxes) {
        Map<String, Map> mergedAxesByKey = new LinkedHashMap<>();
        Map<String, String> canonicalAxisKeyBySourceKey = new HashMap<>();
        for (Object axisObject : sourceAxes) {
            Map axis = (Map) axisObject;
            String sourceAxisKey = UIUtil.getValue(axis, FORMAL_AXIS_KEY);
            String mergeKey = formalBuildStandardMergeKey(axis);
            Map mergedAxis = mergedAxesByKey.get(mergeKey);
            if (mergedAxis == null) {
                mergedAxis = formalCopyRow(axis);
                mergedAxesByKey.put(mergeKey, mergedAxis);
            }
            String canonicalAxisKey = UIUtil.getValue(mergedAxis, FORMAL_AXIS_KEY);
            if (UIUtil.isNotNullAndNotEmpty(sourceAxisKey)
                    && UIUtil.isNotNullAndNotEmpty(canonicalAxisKey)) {
                canonicalAxisKeyBySourceKey.put(sourceAxisKey, canonicalAxisKey);
            }
        }

        Map<String, Map> mergedRowsBySourceAssembly = new LinkedHashMap<>();
        for (Object rowObject : sourceRows) {
            Map row = formalCopyRow((Map) rowObject);
            String sourceAssemblyAxisKey = UIUtil.getValue(row, FORMAL_ASSEMBLY_AXIS_KEY);
            if (UIUtil.isNullOrEmpty(sourceAssemblyAxisKey)) {
                continue;
            }
            Map<String, BigDecimal> sourceUsageMap = (Map<String, BigDecimal>) row.get(FORMAL_USAGE_MAP);
            BigDecimal sourceAssemblyDosage = sourceUsageMap == null
                    ? BigDecimal.ZERO
                    : sourceUsageMap.get(sourceAssemblyAxisKey);
            if (sourceAssemblyDosage == null) {
                sourceAssemblyDosage = BigDecimal.ZERO;
            }
            Map<String, BigDecimal> isolatedUsageMap = new LinkedHashMap<>();
            isolatedUsageMap.put(sourceAssemblyAxisKey, sourceAssemblyDosage);
            row.put(FORMAL_USAGE_MAP, isolatedUsageMap);

            String bomMergeKey = formalBuildStandardMergeKey(row);
            String sourceStructureKey = sourceAssemblyAxisKey + "\u001F" + bomMergeKey;
            Map mergedInSourceAssembly = mergedRowsBySourceAssembly.get(sourceStructureKey);
            if (mergedInSourceAssembly == null) {
                mergedRowsBySourceAssembly.put(sourceStructureKey, row);
            } else {
                //20260821 update by codex 面套只累加同一一级总成发生项内重复的同一BOM行
                formalPutUsage(mergedInSourceAssembly, sourceAssemblyAxisKey, sourceAssemblyDosage);
            }
        }

        Map<String, Map> representativeRowsByCanonicalAxis = new LinkedHashMap<>();
        for (Map sourceAssemblyRow : mergedRowsBySourceAssembly.values()) {
            String sourceAssemblyAxisKey = UIUtil.getValue(sourceAssemblyRow, FORMAL_ASSEMBLY_AXIS_KEY);
            String canonicalAssemblyAxisKey = canonicalAxisKeyBySourceKey.get(sourceAssemblyAxisKey);
            if (UIUtil.isNullOrEmpty(canonicalAssemblyAxisKey)) {
                canonicalAssemblyAxisKey = sourceAssemblyAxisKey;
            }
            String bomMergeKey = formalBuildStandardMergeKey(sourceAssemblyRow);
            String canonicalStructureKey = canonicalAssemblyAxisKey + "\u001F" + bomMergeKey;
            Map representativeRow = representativeRowsByCanonicalAxis.get(canonicalStructureKey);
            Map<String, BigDecimal> sourceUsageMap = (Map<String, BigDecimal>) sourceAssemblyRow.get(FORMAL_USAGE_MAP);
            BigDecimal sourceAssemblyDosage = sourceUsageMap == null
                    ? BigDecimal.ZERO
                    : sourceUsageMap.get(sourceAssemblyAxisKey);
            if (sourceAssemblyDosage == null) {
                sourceAssemblyDosage = BigDecimal.ZERO;
            }
            if (representativeRow != null) {
                //同一一级件配置轴下，同层级、同零件和版本的用量继续累加，不能丢弃后续发生项
                formalPutUsage(representativeRow, canonicalAssemblyAxisKey, sourceAssemblyDosage);
                continue;
            }

            representativeRow = formalCopyRow(sourceAssemblyRow);
            Map<String, BigDecimal> remappedUsageMap = new LinkedHashMap<>();
            remappedUsageMap.put(
                    canonicalAssemblyAxisKey,
                    sourceAssemblyDosage);
            //20260821 update by codex 面套配置轴只重定向当前实际父级，禁止向其他一级总成配置列扩散
            representativeRow.put(FORMAL_USAGE_MAP, remappedUsageMap);
            representativeRow.put(FORMAL_ASSEMBLY_AXIS_KEY, canonicalAssemblyAxisKey);
            representativeRowsByCanonicalAxis.put(canonicalStructureKey, representativeRow);
        }

        MapList representativeRows = new MapList();
        representativeRows.addAll(representativeRowsByCanonicalAxis.values());
        MapList mergedAxes = new MapList();
        mergedAxes.addAll(mergedAxesByKey.values());
        Map result = new HashMap();
        result.put("rows", formalMergeStandardRows(representativeRows));
        result.put("axes", mergedAxes);
        return result;
    }

    /**
     * 按层级唯一规则同步合并发泡明细与一级总成配置轴，并将用量重定向到合并后的配置轴
     **
     * @param sourceRows 发泡结构行
     * @param sourceAxes 发泡一级总成配置轴
     * @return Map rows为合并后的结构行，axes为合并后的配置轴
     * @author caipan by codex
     * @date 2026/8/21 17:25
     */
    private Map formalMergeTopAssemblyStructure(MapList sourceRows, MapList sourceAxes) {
        Map<String, Map> mergedAxesByKey = new LinkedHashMap<>();
        Map<String, String> canonicalAxisKeyBySourceKey = new HashMap<>();
        Map<String, Set<String>> rootKeysByCanonicalAxis = new LinkedHashMap<>();
        for (Object axisObject : sourceAxes) {
            Map axis = (Map) axisObject;
            String sourceAxisKey = UIUtil.getValue(axis, FORMAL_AXIS_KEY);
            String mergeKey = formalBuildStandardMergeKey(axis);
            Map mergedAxis = mergedAxesByKey.get(mergeKey);
            if (mergedAxis == null) {
                mergedAxis = formalCopyRow(axis);
                mergedAxesByKey.put(mergeKey, mergedAxis);
            }
            String canonicalAxisKey = UIUtil.getValue(mergedAxis, FORMAL_AXIS_KEY);
            if (UIUtil.isNotNullAndNotEmpty(sourceAxisKey)
                    && UIUtil.isNotNullAndNotEmpty(canonicalAxisKey)) {
                canonicalAxisKeyBySourceKey.put(sourceAxisKey, canonicalAxisKey);
            }
            String rootAxisKey = UIUtil.getValue(axis, FORMAL_ROOT_AXIS_KEY);
            if (UIUtil.isNotNullAndNotEmpty(canonicalAxisKey)
                    && UIUtil.isNotNullAndNotEmpty(rootAxisKey)) {
                rootKeysByCanonicalAxis
                        .computeIfAbsent(canonicalAxisKey, key -> new LinkedHashSet<>())
                        .add(rootAxisKey);
            }
        }
        Set<String> allRootKeys = new LinkedHashSet<>();
        for (Set<String> axisRootKeys : rootKeysByCanonicalAxis.values()) {
            allRootKeys.addAll(axisRootKeys);
        }

        Map<String, Map> mergedRowsBySourceAssembly = new LinkedHashMap<>();
        for (Object rowObject : sourceRows) {
            Map row = formalCopyRow((Map) rowObject);
            String sourceAssemblyAxisKey = UIUtil.getValue(row, FORMAL_ASSEMBLY_AXIS_KEY);
            if (UIUtil.isNullOrEmpty(sourceAssemblyAxisKey)) {
                continue;
            }
            //20260821 update by codex 每个BOM行只保留所属一级总成的用量，禁止携带或累计其他一级总成配置列的数量
            Map<String, BigDecimal> sourceUsageMap = (Map<String, BigDecimal>) row.get(FORMAL_USAGE_MAP);
            BigDecimal sourceAssemblyDosage = sourceUsageMap == null
                    ? BigDecimal.ZERO
                    : sourceUsageMap.get(sourceAssemblyAxisKey);
            Map<String, BigDecimal> isolatedUsageMap = new LinkedHashMap<>();
            isolatedUsageMap.put(
                    sourceAssemblyAxisKey,
                    sourceAssemblyDosage == null ? BigDecimal.ZERO : sourceAssemblyDosage);
            row.put(FORMAL_USAGE_MAP, isolatedUsageMap);
            String bomMergeKey = formalBuildStandardMergeKey(row);
            String sourceStructureKey = sourceAssemblyAxisKey + "\u001F" + bomMergeKey;
            Map mergedInSourceAssembly = mergedRowsBySourceAssembly.get(sourceStructureKey);
            if (mergedInSourceAssembly == null) {
                mergedRowsBySourceAssembly.put(sourceStructureKey, row);
                continue;
            }
            //20260821 update by codex 仅同一一级总成下同一BOM结构行重复出现时累加，颜色行在后续展开后分别取该数量
            formalPutUsage(
                    mergedInSourceAssembly,
                    sourceAssemblyAxisKey,
                    sourceAssemblyDosage == null ? BigDecimal.ZERO : sourceAssemblyDosage);
        }

        Map<String, Map> representativeRowsByCanonicalAxis = new LinkedHashMap<>();
        for (Map sourceAssemblyRow : mergedRowsBySourceAssembly.values()) {
            String sourceAssemblyAxisKey = UIUtil.getValue(sourceAssemblyRow, FORMAL_ASSEMBLY_AXIS_KEY);
            String canonicalAssemblyAxisKey = canonicalAxisKeyBySourceKey.get(sourceAssemblyAxisKey);
            if (UIUtil.isNullOrEmpty(canonicalAssemblyAxisKey)) {
                canonicalAssemblyAxisKey = sourceAssemblyAxisKey;
            }
            String bomMergeKey = formalBuildStandardMergeKey(sourceAssemblyRow);
            String canonicalStructureKey = canonicalAssemblyAxisKey + "\u001F" + bomMergeKey;
            //20260821 update by codex 同一配置头的重复发生结构取一份数量，不按上层整椅出现次数累加；缺少的子件关系仍可补入
            if (representativeRowsByCanonicalAxis.containsKey(canonicalStructureKey)) {
                continue;
            }
            Map representativeRow = formalCopyRow(sourceAssemblyRow);
            Map<String, BigDecimal> sourceUsageMap = (Map<String, BigDecimal>) representativeRow.get(FORMAL_USAGE_MAP);
            Map<String, BigDecimal> remappedUsageMap = new LinkedHashMap<>();
            BigDecimal sourceAssemblyDosage = sourceUsageMap == null
                    ? BigDecimal.ZERO
                    : sourceUsageMap.get(sourceAssemblyAxisKey);
            //20260821 update by codex 先保留当前一级总成自己的数量，配置覆盖关系在BOM行合并阶段统一展开
            remappedUsageMap.put(
                    canonicalAssemblyAxisKey,
                    sourceAssemblyDosage == null ? BigDecimal.ZERO : sourceAssemblyDosage);
            representativeRow.put(FORMAL_USAGE_MAP, remappedUsageMap);
            representativeRow.put(FORMAL_ASSEMBLY_AXIS_KEY, canonicalAssemblyAxisKey);
            representativeRowsByCanonicalAxis.put(canonicalStructureKey, representativeRow);
        }

        Map<String, Map> mergedRowsByBomKey = new LinkedHashMap<>();
        Map<String, Map<String, Integer>> usagePriorityByBomKey = new HashMap<>();
        for (Map representativeRow : representativeRowsByCanonicalAxis.values()) {
            String bomMergeKey = formalBuildStandardMergeKey(representativeRow);
            Map mergedRow = mergedRowsByBomKey.get(bomMergeKey);
            if (mergedRow == null) {
                mergedRow = formalCopyRow(representativeRow);
                mergedRow.put(FORMAL_USAGE_MAP, new LinkedHashMap<String, BigDecimal>());
                mergedRowsByBomKey.put(bomMergeKey, mergedRow);
                usagePriorityByBomKey.put(bomMergeKey, new HashMap<String, Integer>());
            }

            String ownerAxisKey = UIUtil.getValue(representativeRow, FORMAL_ASSEMBLY_AXIS_KEY);
            Map<String, BigDecimal> ownerUsageMap = (Map<String, BigDecimal>) representativeRow.get(FORMAL_USAGE_MAP);
            BigDecimal ownerDosage = ownerUsageMap == null ? null : ownerUsageMap.get(ownerAxisKey);
            if (ownerDosage == null) {
                ownerDosage = BigDecimal.ZERO;
            }
            Set<String> ownerRootKeys = rootKeysByCanonicalAxis.get(ownerAxisKey);
            if (ownerRootKeys == null) {
                ownerRootKeys = Collections.emptySet();
            }
            int exportLevel = formalInt(UIUtil.getValue(representativeRow, SELECT_LEVEL), 0);
            boolean detailRow = exportLevel > 1;
            boolean commonAssemblyDetail = detailRow
                    && !allRootKeys.isEmpty()
                    && ownerRootKeys.containsAll(allRootKeys);

            Map<String, BigDecimal> mergedUsageMap = (Map<String, BigDecimal>) mergedRow.get(FORMAL_USAGE_MAP);
            Map<String, Integer> usagePriority = usagePriorityByBomKey.get(bomMergeKey);
            for (String targetAxisKey : rootKeysByCanonicalAxis.keySet()) {
                boolean ownAxis = Objects.equals(ownerAxisKey, targetAxisKey);
                //20260821 update by codex 一级总成行永远只写自身列；只有覆盖全部整椅配置的公共一级总成子件才补齐其他配置列
                boolean coversTargetConfiguration = commonAssemblyDetail;
                if (!ownAxis && !coversTargetConfiguration) {
                    continue;
                }

                //20260821 update by codex 自身一级总成用量优先；公共一级总成只补齐其覆盖的配置列，禁止与其他一级总成数量相加
                int priority = ownAxis ? 0 : ownerRootKeys.size() + 1;
                Integer currentPriority = usagePriority.get(targetAxisKey);
                if (currentPriority == null || priority < currentPriority) {
                    mergedUsageMap.put(targetAxisKey, ownerDosage);
                    usagePriority.put(targetAxisKey, priority);
                }
            }
            if (!mergedUsageMap.containsKey(ownerAxisKey)) {
                mergedUsageMap.put(ownerAxisKey, ownerDosage);
                usagePriority.put(ownerAxisKey, 0);
            }
        }

        MapList mergedRows = new MapList();
        mergedRows.addAll(mergedRowsByBomKey.values());
        MapList mergedAxes = new MapList();
        mergedAxes.addAll(mergedAxesByKey.values());
        Map result = new HashMap();
        result.put("rows", mergedRows);
        result.put("axes", mergedAxes);
        return result;
    }

    /**
     * 生成旧导出结果对应的MBOM合并键：根节点保留发生项，其余节点在相同导出层级按零件号和完整版本合并
     **
     * @param row MBOM结构行或配置轴行
     * @return String 相同导出层级内的MBOM唯一键
     * @author caipan by codex
     * @date 2026/8/21 16:52
     */
    private String formalBuildStandardMergeKey(Map row) {
        int level = formalInt(UIUtil.getValue(row, SELECT_LEVEL), 0);
        if (level <= 0) {
            return "ROOT|" + UIUtil.getValue(row, FORMAL_OCCURRENCE_KEY);
        }
        return "LEVEL|" + level + "|" + formalPartRevisionKey(row);
    }

    /**
     * 按位置产品JFPPTitle对整椅配置列排序，未匹配位置放入Others
     **
     * @param context ENOVIA上下文
     * @param exportContext 单次导出上下文
     * @param axes 整椅配置轴行
     * @throws Exception 位置产品反查失败时抛出
     * @author caipan by codex
     * @date 2026/8/20 17:42
     */
    private void formalSortZeroAxes(Context context, Map exportContext, MapList axes) throws Exception {
        Map positionCache = (Map) exportContext.get("positionCache");
        List<String> positionOrder = (List<String>) exportContext.get("positionOrder");
        for (Object axisObject : axes) {
            Map axis = (Map) axisObject;
            String ebomId = UIUtil.getValue(axis, FORMAL_EBOM_ID);
            String positionTitle = UIUtil.getValue(positionCache, ebomId);
            if (!positionCache.containsKey(ebomId)) {
                positionTitle = formalGetPositionTitle(context, ebomId, positionOrder);
                positionCache.put(ebomId, positionTitle);
            }
            axis.put(FORMAL_POSITION_TITLE, positionTitle);
            axis.put(FORMAL_POSITION_RANK, formalPositionRank(positionOrder, positionTitle));
        }
        Collections.sort(axes, new Comparator() {
            @Override
            public int compare(Object firstObject, Object secondObject) {
                Map first = (Map) firstObject;
                Map second = (Map) secondObject;
                int firstPosition = formalGetMapInt(first, FORMAL_POSITION_RANK, Integer.MAX_VALUE);
                int secondPosition = formalGetMapInt(second, FORMAL_POSITION_RANK, Integer.MAX_VALUE);
                if (firstPosition != secondPosition) {
                    return Integer.compare(firstPosition, secondPosition);
                }
                int firstAxisOrder = formalGetMapInt(first, FORMAL_AXIS_ORDER, Integer.MAX_VALUE);
                int secondAxisOrder = formalGetMapInt(second, FORMAL_AXIS_ORDER, Integer.MAX_VALUE);
                if (firstAxisOrder != secondAxisOrder) {
                    return Integer.compare(firstAxisOrder, secondAxisOrder);
                }
                return Integer.compare(
                        formalGetMapInt(first, "formalStyleOrder", 0),
                        formalGetMapInt(second, "formalStyleOrder", 0));
            }
        });
    }

    /**
     * 从Map中读取内部整数辅助字段，避免UIUtil对Integer强制转String
     **
     * @param data 内部数据Map
     * @param key 整数字段key
     * @param defaultValue 空值或无效值时的默认值
     * @return int Map中的整数值
     * @author caipan by codex
     * @date 2026/8/21 13:28
     */
    private int formalGetMapInt(Map data, String key, int defaultValue) {
        Object value = data.get(key);
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        return value == null ? defaultValue : formalInt(String.valueOf(value), defaultValue);
    }

    /**
     * 通过VPMReference反查位置产品并取JFPPTitle
     **
     * @param context ENOVIA上下文
     * @param ebomId VPMReference ID
     * @param positionOrder 产品配置入口返回的位置顺序
     * @return String 排序使用的位置名，未匹配时为Others
     * @throws Exception 位置产品查询失败时抛出
     * @author caipan by codex
     * @date 2026/8/20 17:42
     */
    private String formalGetPositionTitle(Context context, String ebomId, List<String> positionOrder) throws Exception {
        if (UIUtil.isNullOrEmpty(ebomId)) {
            return "Others";
        }
        DomainObject ebomObj = DomainObject.newInstance(context, ebomId);
        StringList titles = ebomObj.getInfoList(
                context,
                "to[JFPositionProduct2VPMReference].from.attribute[JFPPTitle]");
        String matchedTitle = "Others";
        int matchedRank = Integer.MAX_VALUE;
        for (String title : titles) {
            int rank = formalPositionRank(positionOrder, title);
            if (rank < matchedRank) {
                matchedTitle = title;
                matchedRank = rank;
            }
        }
        return matchedTitle;
    }

    /**
     * 将位置名转换为业务排序值
     **
     * @param positionOrder 产品配置入口返回的位置顺序
     * @param positionTitle JFPPTitle
     * @return int 标准位置序号，Others为末位
     * @author caipan by codex
     * @date 2026/8/20 17:42
     */
    private int formalPositionRank(List<String> positionOrder, String positionTitle) {
        int rank = positionOrder.indexOf(positionTitle);
        return rank < 0 ? positionOrder.size() : rank;
    }

    /**
     * 首次调用产品配置现有入口获取JFPPTitle顺序并缓存到JPO级
     **
     * @param context ENOVIA上下文
     * @return List 位置产品名称range顺序
     * @throws Exception 产品配置元数据入口调用失败时抛出
     * @author caipan by codex
     * @date 2026/8/20 19:16
     */
    private static List<String> formalGetPositionOrder(Context context) throws Exception {
        if (FORMAL_POSITION_ORDER_INITIALIZED) {
            return FORMAL_POSITION_ORDER_CACHE;
        }
        synchronized (FORMAL_POSITION_ORDER_LOCK) {
            if (FORMAL_POSITION_ORDER_INITIALIZED) {
                return FORMAL_POSITION_ORDER_CACHE;
            }
            String metaJson = new JF_ProductConfig_mxJPO().getPositionProductCreateMeta(context, new String[0]);
            JSONObject meta = JSON.parseObject(metaJson);
            JSONArray titleOptions = meta.getJSONArray("ppTitles");
            List<String> positionOrder = new ArrayList<>();
            if (titleOptions != null) {
                for (Object optionObject : titleOptions) {
                    JSONObject option = (JSONObject) optionObject;
                    String value = option.getString("value");
                    if (UIUtil.isNotNullAndNotEmpty(value)) {
                        positionOrder.add(value);
                    }
                }
            }
            FORMAL_POSITION_ORDER_CACHE.clear();
            FORMAL_POSITION_ORDER_CACHE.addAll(positionOrder);
            FORMAL_POSITION_ORDER_INITIALIZED = true;
            return FORMAL_POSITION_ORDER_CACHE;
        }
    }

    /**
     * 从导出行与配置轴中移除GX虚拟层，并对其后代层级做穿透修正
     **
     * @param rows 待修正行
     * @author caipan by codex
     * @date 2026/8/20 17:42
     */
    private void formalRemoveVirtualGXRows(MapList rows) {
        Map<String, Map> rowById = new HashMap<>();
        for (Object rowObject : rows) {
            Map row = (Map) rowObject;
            String id = UIUtil.getValue(row, SELECT_ID);
            if (!rowById.containsKey(id)) {
                rowById.put(id, row);
            }
        }
        for (Object rowObject : rows) {
            Map row = (Map) rowObject;
            int removedLevels = 0;
            Set<String> visited = new HashSet<>();
            String parentId = UIUtil.getValue(row, SELECT_FROM_ID);
            while (UIUtil.isNotNullAndNotEmpty(parentId) && visited.add(parentId)) {
                Map parent = rowById.get(parentId);
                if (parent == null) {
                    break;
                }
                if (UIUtil.getValue(parent, "attribute[JF_PartNumber]").startsWith("GX")) {
                    removedLevels++;
                }
                parentId = UIUtil.getValue(parent, SELECT_FROM_ID);
            }
            if (removedLevels > 0) {
                int level = Math.max(0, formalInt(UIUtil.getValue(row, SELECT_LEVEL), 0) - removedLevels);
                row.put(SELECT_LEVEL, String.valueOf(level));
            }
        }
        Iterator iterator = rows.iterator();
        while (iterator.hasNext()) {
            Map row = (Map) iterator.next();
            if (UIUtil.getValue(row, "attribute[JF_PartNumber]").startsWith("GX")) {
                iterator.remove();
            }
        }
    }

    /**
     * 将固定属性和动态配置用量写入对应MBOM Sheet
     **
     * @param context ENOVIA上下文
     * @param workbook 导出工作簿
     * @param sheet Zero、Trim或Foam Sheet
     * @param mbomType GC、GT或GU
     * @param fixedMapping 缓存的固定列映射
     * @param axes 动态配置列数据
     * @param rows 导出明细行
     * @throws Exception 工作表不存在或图片写入失败时抛出
     * @author caipan by codex
     * @date 2026/8/20 18:16
     */
    private void formalWriteMBOMSheet(
            Context context,
            Workbook workbook,
            Sheet sheet,
            String mbomType,
            MapList fixedMapping,
            MapList axes,
            MapList rows) throws Exception {
        if (sheet == null) {
            throw new Exception("Missing template sheet: " + formalGetSheetName(mbomType));
        }
        int dynamicStartColumn = formalGetDynamicStartColumn(fixedMapping);
        CellStyle headerStyle = formalCreateHeaderStyle(workbook);
        CellStyle basicStyle = formalCreateBasicStyle(workbook, HorizontalAlignment.CENTER);
        CellStyle leftStyle = formalCreateBasicStyle(workbook, HorizontalAlignment.LEFT);
        Row fullNumberRow = formalGetOrCreateRow(sheet, 1);
        Row styleRow = formalGetOrCreateRow(sheet, 2);
        Row nameRow = formalGetOrCreateRow(sheet, 3);
        for (int i = 0; i < axes.size(); i++) {
            Map axis = (Map) axes.get(i);
            int columnIndex = dynamicStartColumn + i;
            axis.put(FORMAL_COLUMN_INDEX, columnIndex);
            formalSetStringCell(fullNumberRow, columnIndex, UIUtil.getValue(axis, "InternalFullPartNumber"), headerStyle);
            formalSetStringCell(styleRow, columnIndex, UIUtil.getValue(axis, FORMAL_STYLE_TITLE), headerStyle);
            formalSetStringCell(nameRow, columnIndex, UIUtil.getValue(axis, "attribute[JF_PartNameCN]"), headerStyle);
            sheet.setColumnWidth(columnIndex, 16 * 256);
        }

        int rowIndex = 4;
        Map<String, Integer> itemBySequenceKey = new LinkedHashMap<>();
        int nextItem = 1;
        for (int i = 0; i < rows.size(); i++) {
            Map sourceRow = (Map) rows.get(i);
            String sequenceKey = formalPartRevisionKey(sourceRow);
            if (FORMAL_TYPE_TRIM.equals(mbomType)) {
                int level = formalInt(UIUtil.getValue(sourceRow, SELECT_LEVEL), 0);
                sequenceKey = "LEVEL|" + level + "|" + sequenceKey;
            }
            Integer item = itemBySequenceKey.get(sequenceKey);
            if (item == null) {
                item = nextItem++;
                itemBySequenceKey.put(sequenceKey, item);
            }
            //面套同层级、零件号和完整版本的颜色展开行共用序号，不同层级分别编号；其他类型保持原规则
            Map rowData = formalBuildMappedRowData(mbomType, sourceRow, item);
            Row excelRow = formalGetOrCreateRow(sheet, rowIndex);
            //20260821 update by codex 数据行高度沿用模板设置，不再强制调整
            for (Object mappingObject : fixedMapping) {
                Map mapping = (Map) mappingObject;
                String key = UIUtil.getValue(mapping, "id");
                int columnIndex = formalInt(UIUtil.getValue(mapping, "value"), 0);
                formalWriteMappedCell(
                        context,
                        workbook,
                        sheet,
                        excelRow,
                        rowIndex,
                        columnIndex,
                        key,
                        rowData,
                        basicStyle,
                        leftStyle);
            }
            for (int iAxis = 0; iAxis < axes.size(); iAxis++) {
                Map axis = (Map) axes.get(iAxis);
                int columnIndex = dynamicStartColumn + iAxis;
                Cell usageCell = excelRow.getCell(columnIndex);
                if (usageCell == null) {
                    usageCell = excelRow.createCell(columnIndex);
                }
                BigDecimal dosage = formalGetAxisDosage(rowData, axis);
                if (dosage.compareTo(BigDecimal.ZERO) != 0) {
                    usageCell.setCellValue(dosage.doubleValue());
                } else {
                    usageCell.setCellValue(EMPTY_STRING);
                }
                usageCell.setCellStyle(basicStyle);
            }
            int lastColumnExclusive = dynamicStartColumn + axes.size();
            for (int columnIndex = 0; columnIndex < lastColumnExclusive; columnIndex++) {
                Cell cell = excelRow.getCell(columnIndex);
                if (cell == null) {
                    cell = excelRow.createCell(columnIndex);
                    cell.setCellStyle(basicStyle);
                }
            }
            rowIndex++;
        }
    }

    /**
     * 将通用属性与Zero/Foam/Trim专有属性组装成固定列数据
     **
     * @param mbomType GC、GT或GU
     * @param sourceRow 已完成颜色展开的行
     * @param item 序号
     * @return Map 可直接按XML列映射写入的行
     * @author caipan by codex
     * @date 2026/8/20 18:16
     */
    private Map formalBuildMappedRowData(String mbomType, Map sourceRow, int item) {
        Map data = formalCopyRow(sourceRow);
        data.put("ITEM", String.valueOf(item));
        data.put("d", EMPTY_STRING);
        String level = UIUtil.getValue(data, SELECT_LEVEL);
        data.put("level_" + formalInt(level, 0), formalDefaultValue(level, "0"));
        data.put(
                "attribute[JF_VPMReference.JF_WeightTarget]",
                formalDefaultValue(
                        UIUtil.getValue(data, "attribute[JF_VPMReference.JF_WeightTarget]"),
                        UIUtil.getValue(data, "attribute[JF_VPMReference.JF_Weight]")));
        if (FORMAL_TYPE_FOAM.equals(mbomType)) {
            data.put(
                    "attribute[JF_VPMReference.JF_MaterialDensity]",
                    UIUtil.getValue(data, "attribute[JF_VPMReference.JF_MaterialDensity]"));
        } else if (FORMAL_TYPE_TRIM.equals(mbomType)) {
            data.put("attribute[NetArea]", UIUtil.getValue(data, "attribute[NetArea]"));
            data.put("attribute[Circumference]", UIUtil.getValue(data, "attribute[Circumference]"));
            data.put("attribute[JF_Width]", UIUtil.getValue(data, "attribute[JF_Width]"));
            data.put("attribute[JF_Utilizationrate]", UIUtil.getValue(data, "attribute[JF_Utilizationrate]"));
        }
        return data;
    }

    /**
     * 根据XML映射将一个固定属性写入单元格
     **
     * @param context ENOVIA上下文
     * @param workbook 导出工作簿
     * @param sheet 目标Sheet
     * @param excelRow 目标行
     * @param rowIndex 目标行索引
     * @param columnIndex 目标列索引
     * @param key XML属性key
     * @param data 行数据
     * @param basicStyle 默认样式
     * @param leftStyle 名称列样式
     * @throws Exception 图片或翻译写入失败时抛出
     * @author caipan by codex
     * @date 2026/8/20 18:16
     */
    private void formalWriteMappedCell(
            Context context,
            Workbook workbook,
            Sheet sheet,
            Row excelRow,
            int rowIndex,
            int columnIndex,
            String key,
            Map data,
            CellStyle basicStyle,
            CellStyle leftStyle) throws Exception {
        Cell cell = excelRow.getCell(columnIndex);
        if (cell == null) {
            cell = excelRow.createCell(columnIndex);
        }
        if ("picture".equals(key)) {
            String imagePath = UIUtil.getValue(data, key);
            if (UIUtil.isNotNullAndNotEmpty(imagePath)) {
                JF_ExportEBOM_mxJPO.insertPictureInCell(
                        workbook,
                        sheet,
                        imagePath,
                        rowIndex,
                        columnIndex,
                        100,
                        50);
            }
            cell.setCellStyle(basicStyle);
            return;
        }
        String value = UIUtil.getValue(data, key);
        CellStyle selectedStyle = basicStyle;
        if ("attribute[JF_PartType]".equals(key) && UIUtil.isNotNullAndNotEmpty(value)) {
            value = EnoviaResourceBundle.getRangeI18NString(context, "JF_PartType", value, "zh_cn");
        } else if ("attribute[JF_PartNameCN]".equals(key) || "attribute[JF_PartNameEN]".equals(key)) {
            if ("attribute[JF_PartNameEN]".equals(key)) {
                value = value.substring(0, Math.min(value.length(), 30));
            }
            String colorStyleName = UIUtil.getValue(data, "JF_ColorStyleName");
            if (formalHasNamedColor(colorStyleName)) {
                value = value + "_" + colorStyleName;
            }
            selectedStyle = leftStyle;
        }
        if (formalMustWriteAsText(key)) {
            cell.setCellValue(value);
        } else if (JF_PublicMethodClass_mxJPO.isNumeric(value)) {
            cell.setCellValue(Double.parseDouble(value));
        } else {
            cell.setCellValue(value);
        }
        cell.setCellStyle(selectedStyle);
    }

    /**
     * 判断颜色风格名是否需追加到中英文名称
     **
     * @param colorStyleName 颜色风格名
     * @return boolean 非空且非NUL/null/000时为true
     * @author caipan by codex
     * @date 2026/8/20 18:16
     */
    private boolean formalHasNamedColor(String colorStyleName) {
        return UIUtil.isNotNullAndNotEmpty(colorStyleName)
                && !"NUL".equalsIgnoreCase(colorStyleName)
                && !"null".equalsIgnoreCase(colorStyleName)
                && !"000".equalsIgnoreCase(colorStyleName);
    }

    /**
     * 判断列是否必须以文本写入，防止零件号和色号丢失前导0
     **
     * @param key XML属性key
     * @return boolean 标识类字段返回true
     * @author caipan by codex
     * @date 2026/8/20 18:16
     */
    private boolean formalMustWriteAsText(String key) {
        return "attribute[JF_PartNumber]".equals(key)
                || "JF_InternalColorCode".equals(key)
                || SELECT_REVISION.equals(key)
                || "InternalFullPartNumber".equals(key)
                || "attribute[CustomerPartNumber]".equals(key)
                || "JF_CustormColorCode".equals(key)
                || "attribute[CustomerPartRevision]".equals(key)
                || "d".equals(key);
    }

    /**
     * 取得当前行在指定配置轴和颜色风格下的用量
     **
     * @param rowData 明细行
     * @param axis 配置轴行
     * @return BigDecimal 用量，颜色风格不匹配时为0
     * @author caipan by codex
     * @date 2026/8/20 18:16
     */
    private BigDecimal formalGetAxisDosage(Map rowData, Map axis) {
        String rowStyle = UIUtil.getValue(rowData, FORMAL_STYLE_TITLE);
        String axisStyle = UIUtil.getValue(axis, FORMAL_STYLE_TITLE);
        if (!Objects.equals(rowStyle, axisStyle)) {
            return BigDecimal.ZERO;
        }
        Map<String, BigDecimal> usageMap = (Map<String, BigDecimal>) rowData.get(FORMAL_USAGE_MAP);
        if (usageMap == null) {
            return BigDecimal.ZERO;
        }
        BigDecimal dosage = usageMap.get(UIUtil.getValue(axis, FORMAL_AXIS_KEY));
        return dosage == null ? BigDecimal.ZERO : dosage;
    }

    /**
     * 根据固定列映射最大下标计算动态配置列起点
     **
     * @param fixedMapping XML固定列映射
     * @return int 动态配置列起始下标
     * @author caipan by codex
     * @date 2026/8/20 18:16
     */
    private int formalGetDynamicStartColumn(MapList fixedMapping) {
        int maxColumn = -1;
        for (Object mappingObject : fixedMapping) {
            maxColumn = Math.max(
                    maxColumn,
                    formalInt(UIUtil.getValue((Map) mappingObject, "value"), -1));
        }
        return maxColumn + 1;
    }

    /**
     * 写入整椅导出的变更记录Sheet，并完成XML语义别名转换
     **
     * @param context ENOVIA上下文
     * @param sheet 变更记录Sheet
     * @param projectId 项目ID
     * @param mapping 变更记录固定列映射
     * @throws Exception 变更记录查询或Sheet写入失败时抛出
     * @author caipan by codex
     * @date 2026/8/20 18:16
     */
    private void formalWriteChangeRecordSheet(
            Context context,
            Sheet sheet,
            String projectId,
            MapList mapping) throws Exception {
        if (sheet == null) {
            throw new Exception("Missing template sheet: " + FORMAL_SHEET_CHANGE_RECORD);
        }
        StringList objectSelects = JF_Util_mxJPO.basicBolistSel();
        objectSelects.add("attribute[JF_UpdateDateTime]");
        objectSelects.add("attribute[JF_UpdatePerson]");
        objectSelects.add(SELECT_DESCRIPTION);
        objectSelects.add("attribute[Title]");
        DomainObject projectObj = DomainObject.newInstance(context, projectId);
        MapList records = projectObj.getRelatedObjects(
                context,
                JF_PLMConstants_mxJPO.REL_JF_relProject2ChangeRecord,
                JF_PLMConstants_mxJPO.TYPE_JF_ChangeRecord,
                objectSelects,
                JF_Util_mxJPO.basicRellistSel(),
                false,
                true,
                (short) 1,
                EMPTY_STRING,
                EMPTY_STRING,
                (short) 0);
        records.sort("attribute[JF_UpdateDateTime]", "ascending", "date");
        Workbook workbook = sheet.getWorkbook();
        CellStyle basicStyle = formalCreateBasicStyle(workbook, HorizontalAlignment.CENTER);
        CellStyle leftStyle = formalCreateBasicStyle(workbook, HorizontalAlignment.LEFT);
        int rowIndex = 4;
        for (int i = 0; i < records.size(); i++) {
            Map data = formalBuildChangeRecordData((Map) records.get(i), i + 1);
            Row excelRow = formalGetOrCreateRow(sheet, rowIndex++);
            for (Object mappingObject : mapping) {
                Map column = (Map) mappingObject;
                String key = UIUtil.getValue(column, "id");
                int columnIndex = formalInt(UIUtil.getValue(column, "value"), 0);
                Cell cell = excelRow.getCell(columnIndex);
                if (cell == null) {
                    cell = excelRow.createCell(columnIndex);
                }
                cell.setCellValue(UIUtil.getValue(data, key));
                cell.setCellStyle("description".equals(key) ? leftStyle : basicStyle);
            }
        }
    }

    /**
     * 将变更记录属性转换成模板列语义
     **
     * @param source 变更记录对象数据
     * @param item 序号
     * @return Map ChangePerson、description、ConnECR及日期时间别名数据
     * @author caipan by codex
     * @date 2026/8/20 18:16
     */
    private Map formalBuildChangeRecordData(Map source, int item) {
        Map result = new HashMap();
        result.put("ITEM", String.valueOf(item));
        String updateDateTime = UIUtil.getValue(source, "attribute[JF_UpdateDateTime]");
        String changeDate = EMPTY_STRING;
        String changeTime = EMPTY_STRING;
        if (UIUtil.isNotNullAndNotEmpty(updateDateTime)) {
            try {
                LocalDateTime dateTime = LocalDateTime.parse(updateDateTime, inputFormatter);
                changeDate = dateTime.format(formatterDate);
                changeTime = dateTime.format(formatterTime);
            } catch (Exception e) {
                int blankIndex = updateDateTime.indexOf(' ');
                changeDate = blankIndex > 0 ? updateDateTime.substring(0, blankIndex) : updateDateTime;
                changeTime = blankIndex > 0 ? updateDateTime.substring(blankIndex + 1) : EMPTY_STRING;
            }
        }
        result.put("ChangeDate", changeDate);
        result.put("ChangeTime", changeTime);
        result.put("ChangePerson", UIUtil.getValue(source, "attribute[JF_UpdatePerson]"));
        result.put("description", UIUtil.getValue(source, SELECT_DESCRIPTION));
        result.put("ConnECR", UIUtil.getValue(source, "attribute[Title]").replace(";", "\n"));
        return result;
    }

    /**
     * 获取已存在行或创建新行
     **
     * @param sheet 目标Sheet
     * @param rowIndex 行索引
     * @return Row POI行
     * @author caipan by codex
     * @date 2026/8/20 18:16
     */
    private Row formalGetOrCreateRow(Sheet sheet, int rowIndex) {
        Row row = sheet.getRow(rowIndex);
        return row == null ? sheet.createRow(rowIndex) : row;
    }

    /**
     * 向表头单元格写入文本并应用样式
     **
     * @param row 目标行
     * @param columnIndex 列索引
     * @param value 文本值
     * @param style 表头样式
     * @author caipan by codex
     * @date 2026/8/20 18:16
     */
    private void formalSetStringCell(Row row, int columnIndex, String value, CellStyle style) {
        Cell cell = row.getCell(columnIndex);
        if (cell == null) {
            cell = row.createCell(columnIndex);
        }
        cell.setCellValue(value);
        cell.setCellStyle(style);
    }

    /**
     * 创建动态配置列表头样式
     **
     * @param workbook 导出工作簿
     * @return CellStyle 居中、加粗、换行且带边框的表头样式
     * @author caipan by codex
     * @date 2026/8/20 18:16
     */
    private CellStyle formalCreateHeaderStyle(Workbook workbook) {
        CellStyle style = formalCreateBasicStyle(workbook, HorizontalAlignment.CENTER);
        Font font = workbook.createFont();
        font.setColor(IndexedColors.BLACK.getIndex());
        font.setBold(true);
        style.setFont(font);
        return style;
    }

    /**
     * 创建MBOM导出基础单元格样式
     **
     * @param workbook 导出工作簿
     * @param alignment 水平对齐方式
     * @return CellStyle 换行、垂直居中且带边框的样式
     * @author caipan by codex
     * @date 2026/8/20 18:16
     */
    private CellStyle formalCreateBasicStyle(Workbook workbook, HorizontalAlignment alignment) {
        CellStyle style = workbook.createCellStyle();
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setAlignment(alignment);
        style.setWrapText(true);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setTopBorderColor(IndexedColors.BLACK.getIndex());
        style.setBottomBorderColor(IndexedColors.BLACK.getIndex());
        style.setLeftBorderColor(IndexedColors.BLACK.getIndex());
        style.setRightBorderColor(IndexedColors.BLACK.getIndex());
        return style;
    }

    /**
     * 面套MBOM导入按钮权限
     **
     * @param context ENOVIA上下文
     * @param args Command请求参数
     * @return Boolean 当前用户为项目面套AME时返回true
     * @author caipan by codex
     * @date 2026/8/21 18:00
     */
    public Boolean formalImportTrimMBOMAccess(Context context, String[] args) {
        try {
            Map params = JPO.unpackArgs(args);
            String projectId = UIUtil.getValue(params, "objectId");
            if (UIUtil.isNullOrEmpty(projectId)) {
                return Boolean.FALSE;
            }
            DomainObject projectObject = DomainObject.newInstance(context, projectId);
            MapList members = projectObject.getRelatedObjects(
                    context,
                    DomainRelationship.RELATIONSHIP_MEMBER,
                    DomainConstants.TYPE_PERSON,
                    StringList.create(SELECT_NAME),
                    new StringList(),
                    false,
                    true,
                    (short) 1,
                    EMPTY_STRING,
                    "attribute[Project Role]=='" + ATTR_PROJECT_ROLE_Range_Trim_AME + "'",
                    (short) 0);
            for (Object memberObject : members) {
                Map member = (Map) memberObject;
                if (context.getUser().equals(UIUtil.getValue(member, SELECT_NAME))) {
                    return Boolean.TRUE;
                }
            }
        } catch (Exception e) {
            JF_LOGGER.error("formalImportTrimMBOMAccess error", e);
        }
        return Boolean.FALSE;
    }

    /**
     * 导入面套MBOM门幅和利用率并重新计算关系用量
     **
     * @param context ENOVIA上下文
     * @param args objectId及上传文件
     * @return Map 导入结果、全部校验错误和更新统计
     * @throws Exception 文件解析或数据更新失败时抛出
     * @author caipan by codex
     * @date 2026/8/21 18:00
     */
    public Map formalImportTrimMBOMExcel(Context context, String[] args) throws Exception {
        Map params = JPO.unpackArgs(args);
        String projectId = UIUtil.getValue(params, "objectId");
        List<String> errors = new ArrayList<>();
        if (UIUtil.isNullOrEmpty(projectId)) {
            formalImportAddError(errors, "未获取到当前项目ID");
            return formalImportFailure(errors);
        }

        InputStream inputStream = null;
        Workbook workbook = null;
        boolean pushed = false;
        try {
            inputStream = formalImportGetUploadedExcelInputStream((List) params.get("files"));
            if (inputStream == null) {
                formalImportAddError(errors, "请选择需要导入的面套MBOM Excel文件");
                return formalImportFailure(errors);
            }
            workbook = WorkbookFactory.create(inputStream);
            MapList trimMapping = formalGetColumnMapping(context, FORMAL_MAPPING_TRIM);
            Map importData = formalImportParseTrimWorkbook(workbook, trimMapping);
            errors.addAll((List<String>) importData.get("errors"));
            if (!Boolean.TRUE.equals(importData.get("structureValid"))
                    || !Boolean.TRUE.equals(importData.get("trimFileValid"))) {
                return formalImportFailure(errors);
            }

            ContextUtil.pushContext(context);
            pushed = true;
            Map<String, List<Map>> targetIndex = formalImportBuildTrimTargetIndex(context, projectId);
            formalImportValidateTrimRows((MapList) importData.get("rows"), targetIndex, errors);
            if (!errors.isEmpty()) {
                return formalImportFailure(errors);
            }

            ContextUtil.startTransaction(context, true);
            try {
                Map report = formalImportApplyTrimUpdates(context, (MapList) importData.get("rows"));
                ContextUtil.commitTransaction(context);
                Map result = new HashMap();
                result.put("flag", "Y");
                result.put("message", EnoviaResourceBundle.getProperty(
                        context,
                        "emxComponentsStringResource",
                        context.getLocale(),
                        "emxComponents.MBOMTrimImport.Success"));
                result.put("report", report);
                return result;
            } catch (Exception e) {
                ContextUtil.abortTransaction(context);
                throw e;
            }
        } catch (Exception e) {
            JF_LOGGER.error("formalImportTrimMBOMExcel error, projectId:{}", projectId, e);
            formalImportAddError(errors, "导入失败：" + formalDefaultValue(e.getMessage(), e.getClass().getSimpleName()));
            return formalImportFailure(errors);
        } finally {
            if (pushed) {
                ContextUtil.popContext(context);
            }
            if (workbook != null) {
                try {
                    workbook.close();
                } catch (Exception e) {
                    JF_LOGGER.warn("Close trim MBOM import workbook failed", e);
                }
            }
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (Exception e) {
                    JF_LOGGER.warn("Close trim MBOM import stream failed", e);
                }
            }
        }
    }

    /**
     * 获取上传的面套MBOM Excel输入流
     **
     * @param files multipart上传项
     * @return InputStream 首个Excel文件输入流
     * @throws Exception 文件格式不支持时抛出
     * @author caipan by codex
     * @date 2026/8/21 18:00
     */
    private InputStream formalImportGetUploadedExcelInputStream(List files) throws Exception {
        if (CollectionUtils.isEmpty(files)) {
            return null;
        }
        for (Object fileObject : files) {
            FileItem fileItem = (FileItem) fileObject;
            if (fileItem.isFormField() || fileItem.getSize() <= 0) {
                continue;
            }
            String fileName = formalDefaultValue(fileItem.getName(), EMPTY_STRING).toLowerCase(Locale.ENGLISH);
            if (!fileName.endsWith(".xlsx") && !fileName.endsWith(".xls")) {
                throw new Exception("仅支持.xlsx或.xls格式的MBOM文件");
            }
            return fileItem.getInputStream();
        }
        return null;
    }

    /**
     * 解析并完整校验面套MBOM工作簿基础数据
     **
     * @param workbook 上传工作簿
     * @param trimMapping Export_MBOM_Trim列配置
     * @return Map 解析行、全部错误及结构校验状态
     * @author caipan by codex
     * @date 2026/8/21 18:00
     */
    private Map formalImportParseTrimWorkbook(Workbook workbook, MapList trimMapping) {
        Map result = new HashMap();
        MapList importRows = new MapList();
        List<String> errors = new ArrayList<>();
        result.put("rows", importRows);
        result.put("errors", errors);
        result.put("structureValid", Boolean.FALSE);
        result.put("trimFileValid", Boolean.FALSE);

        Sheet sheet = workbook.getSheet(FORMAL_SHEET_TRIM);
        if (sheet == null) {
            formalImportAddError(errors, "导入文件缺少面套的名为Trim的Sheet");
            return result;
        }

        Map<String, Integer> columnIndexes;
        try {
            columnIndexes = formalImportGetColumnIndexes(trimMapping);
        } catch (Exception e) {
            formalImportAddError(errors, e.getMessage());
            return result;
        }
        if (!formalImportValidateTrimHeaders(sheet, columnIndexes, errors)) {
            return result;
        }
        result.put("structureValid", Boolean.TRUE);

        DataFormatter formatter = new DataFormatter(Locale.CHINA);
        String firstPartNumber = EMPTY_STRING;
        Map<String, Map> firstRowByKey = new LinkedHashMap<>();
        for (int rowIndex = FORMAL_IMPORT_DATA_START_ROW; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
            Row excelRow = sheet.getRow(rowIndex);
            if (excelRow == null) {
                continue;
            }
            String partNumber = formalImportReadCell(formatter, excelRow,
                    columnIndexes.get(FORMAL_IMPORT_PART_NUMBER));
            String colorCode = formalImportReadCell(formatter, excelRow,
                    columnIndexes.get(FORMAL_IMPORT_COLOR_CODE));
            String revision = formalImportReadCell(formatter, excelRow,
                    columnIndexes.get(FORMAL_IMPORT_REVISION));
            String widthText = formalImportReadCell(formatter, excelRow,
                    columnIndexes.get(FORMAL_IMPORT_WIDTH));
            String utilizationText = formalImportReadCell(formatter, excelRow,
                    columnIndexes.get(FORMAL_IMPORT_UTILIZATION));
            if (UIUtil.isNullOrEmpty(partNumber)
                    && UIUtil.isNullOrEmpty(colorCode)
                    && UIUtil.isNullOrEmpty(revision)
                    && UIUtil.isNullOrEmpty(widthText)
                    && UIUtil.isNullOrEmpty(utilizationText)) {
                continue;
            }
            int excelRowNumber = rowIndex + 1;
            if (UIUtil.isNullOrEmpty(firstPartNumber) && UIUtil.isNotNullAndNotEmpty(partNumber)) {
                firstPartNumber = partNumber;
            }

            boolean basicValid = true;
            if (UIUtil.isNullOrEmpty(partNumber)) {
                formalImportAddError(errors, "第" + excelRowNumber + "行：内部零件号不能为空");
                basicValid = false;
            }
            if (UIUtil.isNullOrEmpty(colorCode)) {
                formalImportAddError(errors, "第" + excelRowNumber + "行：内部色号不能为空");
                basicValid = false;
            }
            if (UIUtil.isNullOrEmpty(revision)) {
                formalImportAddError(errors, "第" + excelRowNumber + "行：内部版本号不能为空");
                basicValid = false;
            }
            BigDecimal width = formalImportParseNonNegativeDecimal(widthText, "门幅", excelRowNumber, errors);
            BigDecimal utilization = formalImportParseNonNegativeDecimal(
                    utilizationText, "利用率", excelRowNumber, errors);
            if (width == null || utilization == null) {
                basicValid = false;
            }

            Map importRow = new HashMap();
            importRow.put(FORMAL_IMPORT_ROW_NUMBER, excelRowNumber);
            importRow.put(FORMAL_IMPORT_PART_NUMBER, formalImportNormalizeKeyValue(partNumber));
            importRow.put(FORMAL_IMPORT_COLOR_CODE, formalImportNormalizeKeyValue(colorCode));
            importRow.put(FORMAL_IMPORT_REVISION, formalImportNormalizeKeyValue(revision));
            importRow.put(FORMAL_IMPORT_WIDTH, width);
            importRow.put(FORMAL_IMPORT_UTILIZATION, utilization);
            importRow.put("formalImportBasicValid", basicValid);
            if (basicValid) {
                String importKey = formalImportBuildTrimKey(partNumber, revision);
                importRow.put(FORMAL_IMPORT_KEY, importKey);
                Map firstRow = firstRowByKey.get(importKey);
                if (firstRow == null) {
                    firstRowByKey.put(importKey, importRow);
                } else if (((BigDecimal) firstRow.get(FORMAL_IMPORT_WIDTH)).compareTo(width) != 0
                        || ((BigDecimal) firstRow.get(FORMAL_IMPORT_UTILIZATION)).compareTo(utilization) != 0) {
                    formalImportAddError(errors,
                            "第" + firstRow.get(FORMAL_IMPORT_ROW_NUMBER) + "行与第" + excelRowNumber
                                    + "行：相同内部零件号和完整版本号的门幅或利用率不一致");
                }
            }
            importRows.add(importRow);
        }

        if (UIUtil.isNullOrEmpty(firstPartNumber)) {
            formalImportAddError(errors, "Trim Sheet第5行开始没有可导入数据");
            return result;
        }
        if (!formalImportNormalizeKeyValue(firstPartNumber).startsWith(FORMAL_TYPE_TRIM)) {
            formalImportAddError(errors, "第一个非空内部零件号不是GT开头，当前文件不是面套MBOM文件");
            return result;
        }
        result.put("trimFileValid", Boolean.TRUE);
        return result;
    }

    /**
     * 获取面套导入必需字段的XML配置列号
     **
     * @param trimMapping Export_MBOM_Trim列配置
     * @return Map 字段配置ID到零基列号
     * @throws Exception 必需列未配置时抛出
     * @author caipan by codex
     * @date 2026/8/21 18:00
     */
    private Map<String, Integer> formalImportGetColumnIndexes(MapList trimMapping) throws Exception {
        Map<String, Integer> indexes = new LinkedHashMap<>();
        indexes.put(FORMAL_IMPORT_PART_NUMBER,
                formalImportGetColumnIndex(trimMapping, FORMAL_IMPORT_PART_NUMBER));
        indexes.put(FORMAL_IMPORT_COLOR_CODE,
                formalImportGetColumnIndex(trimMapping, FORMAL_IMPORT_COLOR_CODE));
        indexes.put(FORMAL_IMPORT_REVISION,
                formalImportGetColumnIndex(trimMapping, FORMAL_IMPORT_REVISION));
        indexes.put(FORMAL_IMPORT_WIDTH,
                formalImportGetColumnIndex(trimMapping, FORMAL_IMPORT_WIDTH));
        indexes.put(FORMAL_IMPORT_UTILIZATION,
                formalImportGetColumnIndex(trimMapping, FORMAL_IMPORT_UTILIZATION));
        return indexes;
    }

    /**
     * 从列映射中取得指定字段列号
     **
     * @param mapping XML列映射
     * @param fieldId 字段配置ID
     * @return int 零基列号
     * @throws Exception 字段缺失或列号非法时抛出
     * @author caipan by codex
     * @date 2026/8/21 18:00
     */
    private int formalImportGetColumnIndex(MapList mapping, String fieldId) throws Exception {
        for (Object mappingObject : mapping) {
            Map column = (Map) mappingObject;
            if (fieldId.equals(UIUtil.getValue(column, "id"))) {
                try {
                    return Integer.parseInt(UIUtil.getValue(column, "value"));
                } catch (NumberFormatException e) {
                    throw new Exception("Export_MBOM_Trim配置列号非法：" + fieldId);
                }
            }
        }
        throw new Exception("Export_MBOM_Trim缺少导入字段配置：" + fieldId);
    }

    /**
     * 按XML列位置校验面套导入中英文表头
     **
     * @param sheet Trim Sheet
     * @param columnIndexes 必需字段列号
     * @param errors 全部错误列表
     * @return boolean 全部表头正确时返回true
     * @author caipan by codex
     * @date 2026/8/21 18:00
     */
    private boolean formalImportValidateTrimHeaders(
            Sheet sheet, Map<String, Integer> columnIndexes, List<String> errors) {
        Map<String, String[]> expectedHeaders = new LinkedHashMap<>();
        expectedHeaders.put(FORMAL_IMPORT_PART_NUMBER, new String[]{"内部零件号", "Internal part number"});
        expectedHeaders.put(FORMAL_IMPORT_COLOR_CODE, new String[]{"内部色号", "Interior color"});
        expectedHeaders.put(FORMAL_IMPORT_REVISION, new String[]{"内部版本号", "Build number"});
        expectedHeaders.put(FORMAL_IMPORT_WIDTH, new String[]{"门幅", "Width"});
        expectedHeaders.put(FORMAL_IMPORT_UTILIZATION, new String[]{"利用率", "Utilizationrate"});
        DataFormatter formatter = new DataFormatter(Locale.CHINA);
        boolean valid = true;
        for (Map.Entry<String, String[]> header : expectedHeaders.entrySet()) {
            int columnIndex = columnIndexes.get(header.getKey());
            String actualCn = formalImportReadCell(
                    formatter, sheet.getRow(FORMAL_IMPORT_HEADER_CN_ROW), columnIndex);
            String actualEn = formalImportReadCell(
                    formatter, sheet.getRow(FORMAL_IMPORT_HEADER_EN_ROW), columnIndex);
            if (!formalImportNormalizeHeader(header.getValue()[0]).equals(formalImportNormalizeHeader(actualCn))) {
                formalImportAddError(errors,
                        "第" + (columnIndex + 1) + "列中文表头应为“" + header.getValue()[0]
                                + "”，实际为“" + actualCn + "”");
                valid = false;
            }
            if (!formalImportNormalizeHeader(header.getValue()[1]).equals(formalImportNormalizeHeader(actualEn))) {
                formalImportAddError(errors,
                        "第" + (columnIndex + 1) + "列英文表头应为“" + header.getValue()[1]
                                + "”，实际为“" + actualEn + "”");
                valid = false;
            }
        }
        return valid;
    }

    /**
     * 读取Excel单元格显示文本
     **
     * @param formatter POI格式化器
     * @param row Excel行
     * @param columnIndex 零基列号
     * @return String 去除首尾空格后的显示值
     * @author caipan by codex
     * @date 2026/8/21 18:00
     */
    private String formalImportReadCell(DataFormatter formatter, Row row, int columnIndex) {
        if (row == null) {
            return EMPTY_STRING;
        }
        Cell cell = row.getCell(columnIndex);
        return cell == null ? EMPTY_STRING : formatter.formatCellValue(cell).trim();
    }

    /**
     * 解析门幅或利用率非负数
     **
     * @param value Excel文本
     * @param fieldName 字段名称
     * @param rowNumber Excel行号
     * @param errors 全部错误列表
     * @return BigDecimal 有效非负数，非法时返回null
     * @author caipan by codex
     * @date 2026/8/21 18:00
     */
    private BigDecimal formalImportParseNonNegativeDecimal(
            String value, String fieldName, int rowNumber, List<String> errors) {
        if (UIUtil.isNullOrEmpty(value)) {
            formalImportAddError(errors, "第" + rowNumber + "行：" + fieldName + "不能为空");
            return null;
        }
        try {
            String normalized = value.replace(",", EMPTY_STRING).replace("％", "%").trim();
            boolean percent = normalized.endsWith("%");
            if (percent) {
                normalized = normalized.substring(0, normalized.length() - 1).trim();
            }
            BigDecimal decimal = new BigDecimal(normalized);
            if (percent) {
                decimal = decimal.divide(new BigDecimal("100"));
            }
            if (decimal.compareTo(BigDecimal.ZERO) < 0) {
                formalImportAddError(errors, "第" + rowNumber + "行：" + fieldName + "必须大于等于0");
                return null;
            }
            return decimal.stripTrailingZeros();
        } catch (Exception e) {
            formalImportAddError(errors, "第" + rowNumber + "行：" + fieldName + "不是有效数字“" + value + "”");
            return null;
        }
    }

    /**
     * 建立当前项目面套MBOM的零件号和完整版本索引
     **
     * @param context ENOVIA上下文
     * @param projectId 当前项目ID
     * @return Map 导入Key到全部MBOM对象和关系发生项
     * @throws Exception MBOM结构或颜色数据查询失败时抛出
     * @author caipan by codex
     * @date 2026/8/21 18:00
     */
    private Map<String, List<Map>> formalImportBuildTrimTargetIndex(
            Context context, String projectId) throws Exception {
        Map exportContext = formalBuildExportContext(context, projectId, FORMAL_TYPE_TRIM);
        Map structure = formalCollectTrimOrFoamStructure(context, exportContext, FORMAL_TYPE_TRIM);
        MapList sourceRows = (MapList) structure.get("rows");
        Map<String, List<Map>> targetIndex = new LinkedHashMap<>();
        Map<String, Set<String>> identitiesByKey = new HashMap<>();
        Map<String, String> partSubTypeByEbomId = new HashMap<>();
        for (Object rowObject : sourceRows) {
            Map row = (Map) rowObject;
            Map ebomInfo = formalFindEBOMInfo(context, exportContext, row);
            //导入索引必须与导出显示身份一致，但更新目标仍保留原MBOM对象和关系。
            Map importIdentity = formalCopyRow(row);
            if (MapUtils.isNotEmpty(ebomInfo)) {
                formalApplyRollPartIdentity(context, exportContext, importIdentity, ebomInfo);
            }
            String partNumber = UIUtil.getValue(importIdentity, FORMAL_IMPORT_PART_NUMBER);
            String revision = UIUtil.getValue(importIdentity, SELECT_REVISION);
            String netArea = UIUtil.getValue(ebomInfo, "attribute[NetArea]");
            String ebomId = UIUtil.getValue(ebomInfo, SELECT_ID);
            String partSubType = EMPTY_STRING;
            if (UIUtil.isNotNullAndNotEmpty(ebomId)) {
                if (partSubTypeByEbomId.containsKey(ebomId)) {
                    partSubType = partSubTypeByEbomId.get(ebomId);
                } else {
                    partSubType = DomainObject.newInstance(context, ebomId)
                            .getInfo(context, FORMAL_IMPORT_PART_SUB_TYPE);
                    partSubType = formalDefaultValue(partSubType, EMPTY_STRING);
                    partSubTypeByEbomId.put(ebomId, partSubType);
                }
            }
            String key = formalImportBuildTrimKey(partNumber, revision);
            String objectId = UIUtil.getValue(row, SELECT_ID);
            String relationshipId = UIUtil.getValue(row, SELECT_RELATIONSHIP_ID);
            String identity = objectId + "|" + relationshipId;
            Set<String> identities = identitiesByKey.computeIfAbsent(key, value -> new LinkedHashSet<>());
            if (!identities.add(identity)) {
                continue;
            }
            Map target = new HashMap();
            target.put(SELECT_ID, objectId);
            target.put(SELECT_RELATIONSHIP_ID, relationshipId);
            target.put(FORMAL_IMPORT_PART_NUMBER, formalImportNormalizeKeyValue(partNumber));
            target.put(FORMAL_IMPORT_REVISION, formalImportNormalizeKeyValue(revision));
            target.put(FORMAL_IMPORT_NET_AREA, netArea);
            target.put(FORMAL_IMPORT_PART_SUB_TYPE, partSubType);
            targetIndex.computeIfAbsent(key, value -> new ArrayList<>()).add(target);
        }
        return targetIndex;
    }

    /**
     * 校验导入行在项目MBOM中的匹配、样板包关系及对象级属性冲突
     **
     * @param importRows 已解析导入行
     * @param targetIndex 当前项目面套MBOM索引
     * @param errors 全部错误列表
     * @author caipan by codex
     * @date 2026/8/21 18:00
     */
    private void formalImportValidateTrimRows(
            MapList importRows, Map<String, List<Map>> targetIndex, List<String> errors) {
        Map<String, Map> firstInputByObject = new HashMap<>();
        for (Object rowObject : importRows) {
            Map importRow = (Map) rowObject;
            if (!Boolean.TRUE.equals(importRow.get("formalImportBasicValid"))) {
                continue;
            }
            int rowNumber = (Integer) importRow.get(FORMAL_IMPORT_ROW_NUMBER);
            String importKey = UIUtil.getValue(importRow, FORMAL_IMPORT_KEY);
            List<Map> targets = targetIndex.get(importKey);
            if (CollectionUtils.isEmpty(targets)) {
                formalImportAddError(errors,
                        "第" + rowNumber + "行：内部零件号和完整版本号在当前项目面套MBOM中不存在：" + importKey);
                continue;
            }
            importRow.put(FORMAL_IMPORT_TARGETS, targets);
            for (Map target : targets) {
                String objectId = UIUtil.getValue(target, SELECT_ID);
                String relationshipId = UIUtil.getValue(target, SELECT_RELATIONSHIP_ID);
                if (UIUtil.isNullOrEmpty(objectId)) {
                    formalImportAddError(errors, "第" + rowNumber + "行：匹配的MBOM对象ID为空：" + importKey);
                }
                if (formalImportIsTemplatePackage(target)) {
                    if (UIUtil.isNullOrEmpty(relationshipId)) {
                        formalImportAddError(errors, "第" + rowNumber + "行：匹配的样板包MBOM关系ID为空：" + importKey);
                    }
                }

                Map firstInput = firstInputByObject.get(objectId);
                if (firstInput == null) {
                    firstInputByObject.put(objectId, importRow);
                    continue;
                }
                BigDecimal firstWidth = (BigDecimal) firstInput.get(FORMAL_IMPORT_WIDTH);
                BigDecimal firstUtilization = (BigDecimal) firstInput.get(FORMAL_IMPORT_UTILIZATION);
                BigDecimal currentWidth = (BigDecimal) importRow.get(FORMAL_IMPORT_WIDTH);
                BigDecimal currentUtilization = (BigDecimal) importRow.get(FORMAL_IMPORT_UTILIZATION);
                if ((!UIUtil.getValue(firstInput, FORMAL_IMPORT_KEY).equals(importKey))
                        && (firstWidth.compareTo(currentWidth) != 0
                        || firstUtilization.compareTo(currentUtilization) != 0)) {
                    formalImportAddError(errors,
                            "第" + firstInput.get(FORMAL_IMPORT_ROW_NUMBER) + "行与第" + rowNumber
                                    + "行：不同导入数据对应同一MBOM对象，但门幅或利用率不一致");
                }
            }
        }
    }

    /**
     * 在事务中更新面套MBOM对象属性及关系用量
     **
     * @param context ENOVIA上下文
     * @param importRows 已通过全部校验的导入行
     * @return Map 唯一对象、关系和有效行更新统计
     * @throws Exception 属性更新失败时抛出
     * @author caipan by codex
     * @date 2026/8/21 18:00
     */
    private Map formalImportApplyTrimUpdates(Context context, MapList importRows) throws Exception {
        Map<String, Map<String, String>> objectUpdates = new LinkedHashMap<>();
        Map<String, String> relationshipUpdates = new LinkedHashMap<>();
        int validRowCount = 0;
        for (Object rowObject : importRows) {
            Map importRow = (Map) rowObject;
            List<Map> targets = (List<Map>) importRow.get(FORMAL_IMPORT_TARGETS);
            if (CollectionUtils.isEmpty(targets)) {
                continue;
            }
            validRowCount++;
            BigDecimal width = (BigDecimal) importRow.get(FORMAL_IMPORT_WIDTH);
            BigDecimal utilization = (BigDecimal) importRow.get(FORMAL_IMPORT_UTILIZATION);
            String widthValue = width.stripTrailingZeros().toPlainString();
            String utilizationValue = utilization.stripTrailingZeros().toPlainString();
            for (Map target : targets) {
                String objectId = UIUtil.getValue(target, SELECT_ID);
                String relationshipId = UIUtil.getValue(target, SELECT_RELATIONSHIP_ID);
                BigDecimal netArea = formalImportParseSystemDecimal(
                        UIUtil.getValue(target, FORMAL_IMPORT_NET_AREA));

                Map<String, String> attributes = new HashMap<>();
                attributes.put(ATTR_JF_MBOM_WIDTH, widthValue);
                attributes.put(ATTR_JF_MBOM_UTILIZATION_RATE, utilizationValue);
                objectUpdates.put(objectId, attributes);

                if (!formalImportIsTemplatePackage(target)) {
                    continue;
                }
                // 净面积、门幅或利用率无法参与除法时保留原关系用量，只更新对象上的门幅和利用率。
                if (netArea == null || netArea.compareTo(BigDecimal.ZERO) <= 0
                        || width.compareTo(BigDecimal.ZERO) == 0
                        || utilization.compareTo(BigDecimal.ZERO) == 0) {
                    continue;
                }
                String dosage = formalImportCalculateDosage(netArea, width, utilization);
                String existingDosage = relationshipUpdates.get(relationshipId);
                if (existingDosage != null && !existingDosage.equals(dosage)) {
                    throw new Exception("同一MBOM关系计算出不同用量：" + relationshipId);
                }
                relationshipUpdates.put(relationshipId, dosage);
            }
        }

        for (Map.Entry<String, Map<String, String>> objectUpdate : objectUpdates.entrySet()) {
            DomainObject.newInstance(context, objectUpdate.getKey())
                    .setAttributeValues(context, objectUpdate.getValue());
        }
        for (Map.Entry<String, String> relationshipUpdate : relationshipUpdates.entrySet()) {
            DomainRelationship.newInstance(context, relationshipUpdate.getKey()).setAttributeValue(
                    context,
                    ATTR_JF_MBOM_DOSAGE,
                    relationshipUpdate.getValue());
        }

        Map report = new HashMap();
        report.put("rowCount", validRowCount);
        report.put("objectCount", objectUpdates.size());
        report.put("relationshipCount", relationshipUpdates.size());
        return report;
    }

    /**
     * 计算导入后的面套关系用量
     **
     * @param netArea 系统净面积
     * @param width 导入门幅
     * @param utilization 导入利用率
     * @return String 净面积除以门幅和利用率的结果
     * @author caipan by codex
     * @date 2026/8/21 18:00
     */
    private String formalImportCalculateDosage(
            BigDecimal netArea, BigDecimal width, BigDecimal utilization) {
        return netArea.divide(
                        width.multiply(utilization),
                        FORMAL_IMPORT_DOSAGE_SCALE,
                        RoundingMode.HALF_UP)
                .stripTrailingZeros()
                .toPlainString();
    }

    /**
     * 判断目标零件是否为样板包
     **
     * @param target 当前项目面套MBOM匹配目标
     * @return boolean JF_PartSubType为T06时返回true
     * @author caipan by codex
     * @date 2026/8/24 16:00
     */
    private boolean formalImportIsTemplatePackage(Map target) {
        return FORMAL_IMPORT_TEMPLATE_PACKAGE_SUB_TYPE.equalsIgnoreCase(
                UIUtil.getValue(target, FORMAL_IMPORT_PART_SUB_TYPE));
    }

    /**
     * 创建面套导入匹配Key
     **
     * @param partNumber 内部零件号
     * @param revision 内部版本号
     * @return String 标准化内部零件号和完整版本号组合
     * @author caipan by codex
     * @date 2026/8/21 18:00
     */
    private String formalImportBuildTrimKey(String partNumber, String revision) {
        return formalImportNormalizeKeyValue(partNumber)
                + "|"
                + formalImportNormalizeKeyValue(revision);
    }

    /**
     * 标准化导入匹配值
     **
     * @param value 原值
     * @return String 去除首尾空格并转为大写的值
     * @author caipan by codex
     * @date 2026/8/21 18:00
     */
    private String formalImportNormalizeKeyValue(String value) {
        return formalDefaultValue(value, EMPTY_STRING).trim().toUpperCase(Locale.ENGLISH);
    }

    /**
     * 标准化Excel表头
     **
     * @param value 原表头
     * @return String 去除全部空白并转为小写的表头
     * @author caipan by codex
     * @date 2026/8/21 18:00
     */
    private String formalImportNormalizeHeader(String value) {
        return formalDefaultValue(value, EMPTY_STRING)
                .replaceAll("\\s+", EMPTY_STRING)
                .toLowerCase(Locale.ENGLISH);
    }

    /**
     * 解析系统净面积数值
     **
     * @param value 系统属性文本
     * @return BigDecimal 数值，非法时返回null
     * @author caipan by codex
     * @date 2026/8/21 18:00
     */
    private BigDecimal formalImportParseSystemDecimal(String value) {
        try {
            return UIUtil.isNullOrEmpty(value) ? null : new BigDecimal(value.trim());
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 向导入错误列表增加不重复错误
     **
     * @param errors 错误列表
     * @param error 错误文本
     * @author caipan by codex
     * @date 2026/8/21 18:00
     */
    private void formalImportAddError(List<String> errors, String error) {
        if (UIUtil.isNotNullAndNotEmpty(error) && !errors.contains(error)) {
            errors.add(error);
        }
    }

    /**
     * 创建面套导入失败返回值
     **
     * @param errors 全部错误列表
     * @return Map 失败标识、错误列表及合并消息
     * @author caipan by codex
     * @date 2026/8/21 18:00
     */
    private Map formalImportFailure(List<String> errors) {
        Map result = new HashMap();
        result.put("flag", "N");
        result.put("errors", errors);
        result.put("message", String.join("\n", errors));
        return result;
    }

}
