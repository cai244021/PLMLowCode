import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.DomainRelationship;
import com.matrixone.apps.domain.util.*;
import com.matrixone.apps.framework.ui.UICache;
import com.matrixone.apps.framework.ui.UIUtil;
import matrix.db.Context;
import matrix.db.JPO;
import matrix.db.Page;
import matrix.db.Policy;
import matrix.util.StringList;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Pattern;

import static com.matrixone.apps.domain.DomainConstants.*;

public class JF_ExportMBOM_mxJPO implements JF_PLMConstants_mxJPO{
    private static final DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("M/d/yyyy h:mm:ss a", Locale.US);
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
    private static final DateTimeFormatter formatterDate = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.ENGLISH);
    private static final DateTimeFormatter formatterTime = DateTimeFormatter.ofPattern("HH:mm:ss", Locale.ENGLISH);
    private static final SimpleDateFormat sdf = new SimpleDateFormat("M/d/yyyy h:mm:ss a");
    private static final String MDM_TRIM_ROLL_REVISION = "AA.1-000";
    private static final Set<String> MDM_TRIM_ROLL_DETAIL_CATEGORIES = Collections.unmodifiableSet(
            new HashSet<>(Arrays.asList("样板包", "松紧带", "嵌条")));

    private static final Logger JF_LOGGER = LoggerFactory.getLogger(JF_ExportMBOM_mxJPO.class);


    /**
     * 导出MBOM清单  备份
     * @param context
     * @param args
     * @return
     * @throws Exception
     */
    public Map exportMBOMExcel1(Context context, String[] args) throws Exception {
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

        Map<String, String> mbomConfigMap = getMBOMConfig(context);
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

    /**
     * @Author Liuxg
     * @Description  获取MDM接口数据
     * @Date 2025/11/13 3:07
     * @Param [context, workbook, projectId, target]
     * @return void
     **/
    public void getExportMBOMInfoForSync(Context context, String projectId, String target,JSONArray partsArray,JSONArray bomArray) throws Exception{
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
        typeSelectList.add("attribute[" + JF_PLMConstants_mxJPO.ATTR_JF_ROLL_PART_NUMBER + "]");
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
        //20260724 update by ljr MDM同步新增详细分类，取对应EBOM零件的详细分类中文；
        EBomTypeSelectList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JF_Detail_CN);

        JF_ExportEBOM_mxJPO jfExportEBOMMxJPO = new JF_ExportEBOM_mxJPO();
        JF_VPMReferenceEBOM_mxJPO ebom = new JF_VPMReferenceEBOM_mxJPO();
        JF_DR_mxJPO dr = new JF_DR_mxJPO();
        Map<String, Map> customerPartsDBInfoCache = new HashMap<>();
        Map<String, Boolean> rollPartExistenceCache = new HashMap<>();
        DomainObject projectObj = DomainObject.newInstance(context,projectId);
        String topMbomId = projectObj.getInfo(context, "from[JF_relProject2MBOM].to.id");

        DomainObject topMbomObj = DomainObject.newInstance(context, topMbomId);
        StringList zeroLevelPart = topMbomObj.getInfoList(context, "from[JF_relManufacturedItem].to.id");


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

        Map<String, String> mbomConfigMap = getMBOMConfig(context);
        //获取表格映射
        MapList MBOMMappingList = JF_PublicMethodClass_mxJPO.getJFCostChangeHistoryTemplateMapping(context, "MBOM");

        JF_LOGGER.info("MBOMMappingList----:{}",MBOMMappingList);
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

                        getExtractOneLevelInfoforSync(oneLevelMapInfo, oneLevelMapInfoMiddle, gcId, gxPartMap, gxPartId, gxJF_Dosage, gxStrRevision, gxJF_PartNumber,gxfromid);
                    }
                    continue;
                }

                getExtractOneLevelInfoforSync(oneLevelMapInfo, oneLevelMapInfoMiddle, gcId, oneLevelMap, oneLevelId, JF_Dosage, strRevision, JF_PartNumber,JF_fromid);
            }
        }
        //整椅MBOM：仅保留整椅下的1级别件，如果1级件存在GX件，则把展示GX下级件；
        //面套MBOM：过滤整椅下的发泡件（GT），展示面套件下级所有零件；
        //发泡MBOM ：过滤整椅下的发泡件（GU），展示发泡件下级所有零件。


        //，gcid  fromid 用量
        MapList DosageMaplist=new MapList();

        //遍历一级件及其子集
        for (Object o : oneLevelMapInfo.entrySet()) {
            Map.Entry entry = (Map.Entry)o;
            String strOneLevelId = (String) entry.getKey();
            Map oneLevelInfo = (Map) entry.getValue();
            //添加一级件
//            actualDataList.add(oneLevelInfo);
            String onePartNumber = UIUtil.getValue(oneLevelInfo,"attribute[JF_PartNumber]");
            String firstTwoCharacters = onePartNumber.substring(0, 2);
            DomainObject oneLevel = DomainObject.newInstance(context,strOneLevelId);

            MapList partMapList=new MapList();


//            //配置中fasle或者是选择的导出整椅件(如果配置是true但是选择的是整椅，应该怎么处理，以哪个为准，目前是以选择为准)
//            if ("false".equals(mbomConfigMap.get(firstTwoCharacters))&&"GC".equals(target)){
//
//                //此处需要单独处理
//                //新需求 如果是false还是要展开，但是需要遍历完所有节点的新属性JF_SpecialProcurementType/不为NA的时候需要向上单向展开(双经销/外协/NA)
//                //为NA的时候不需要保留
//                partMapList=checkOnePart(context,oneLevelInfo,typeSelectList,relSelectList);
////                continue;
//            }else{
//                //一级件子件
//                partMapList = oneLevel.getRelatedObjects(context,
//                        JF_PLMConstants_mxJPO.REL_JF_relManufacturedItem, // relationship pattern
//                        JF_PLMConstants_mxJPO.TYPE_JF_ManufacturedItem,                                    // object pattern
//                        typeSelectList,                            // object selects
//                        relSelectList, // relationship selects
//                        false,                                        // to direction
//                        true,                                        // from direction
//                        (short) 0,                                    // recursion level
//                        "",                // object where clause
//                        "",
//                        (short) 0);
//            }


            //特殊采购类型处理方式
            partMapList=checkOnePart(context,oneLevelInfo,typeSelectList,relSelectList);

            StringList p_idlist=new StringList();
            for(int p=0;p<partMapList.size();p++){
                Map partInfoMap = (Map) partMapList.get(p);
                String p_id = UIUtil.getValue(partInfoMap, "id");
                p_idlist.add(p_id);
            }


            //0107新增需求自制面套和发泡需要多取一层到2级件并需要对已经确定的特殊类型筛选结果partMapList合并去重
            String one_JF_ProcurementType = UIUtil.getValue(oneLevelInfo,"attribute[JF_ProcurementType]");

            MapList make_MapList=new MapList();
            //update by ljr 20260303
//            if("make".equalsIgnoreCase(one_JF_ProcurementType)&&("GT".equals(firstTwoCharacters)||"GU".equals(firstTwoCharacters))){
            if("make".equalsIgnoreCase(one_JF_ProcurementType)&&(!"GT".equals(firstTwoCharacters)&&!"GU".equals(firstTwoCharacters))){
                MapList make_partMapList = oneLevel.getRelatedObjects(context,
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
                for(int m=0;m<make_partMapList.size();m++){
                    Map m_map= (Map) make_partMapList.get(m);
                    String m_id= UIUtil.getValue(m_map, "id");
                    //原来的集合已经有了，就不处理,没有就单独处理
                    if(!p_idlist.contains(m_id)){
                        make_MapList.add(m_map);
                    }
                }
            }





//            if(!"GC".equals(target)){
//                //未选择整椅,选择面套(发泡)
//                if(!target.equals(firstTwoCharacters)){
//                    //当前遍历的件不是面套(发泡)
//                    continue;
//                }else{
//                    //面套（发泡），添加一级件
//                    actualDataList.add(oneLevelInfo);
//                }
//            }else{
//                //整椅，添加一级件
//                actualDataList.add(oneLevelInfo);
//            }

            actualDataList.add(oneLevelInfo);

            //子件对应的gc id集合 因为用量需要和GCid对应起来
            StringList mappingGCId = getMappingGCId(oneLevelInfo, zeroLevelPart);

            JF_LOGGER.info("---lxg-mappingGCId->"+mappingGCId);
            int level = Integer.MAX_VALUE;
            Map<String, Object> sameBomeMap = new HashMap<>();


            //合并一级件下面的用量
            for (Object o1 : partMapList) {
                Map partInfoMap = (Map) o1;
//                JF_LOGGER.info("--lxg--partInfoMap-->:{}",partInfoMap);
                String childId = UIUtil.getValue(partInfoMap, "id");
                String fromId = UIUtil.getValue(partInfoMap, SELECT_FROM_ID);
                String JF_Dosage = UIUtil.getValue(partInfoMap, "attribute[JF_Dosage]");
                String strRevision = (String) partInfoMap.get(SELECT_REVISION);
                String JF_PartNumber = (String) partInfoMap.get("attribute[JF_PartNumber]");
                int childLevel = Integer.parseInt(UIUtil.getValue(partInfoMap, SELECT_LEVEL));
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
//                    JF_LOGGER.info("@@@@@@@@@@@@@@@@@@添加到集合的子集:{}",partInfoMap);
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


            for (Object o1 : make_MapList) {
                Map partInfoMap = (Map) o1;
//                JF_LOGGER.info("--lxg--partInfoMap-->:{}",partInfoMap);
                String childId = UIUtil.getValue(partInfoMap, "id");
                String fromId = UIUtil.getValue(partInfoMap, SELECT_FROM_ID);
                String JF_Dosage = UIUtil.getValue(partInfoMap, "attribute[JF_Dosage]");
                String strRevision = (String) partInfoMap.get(SELECT_REVISION);
                String JF_PartNumber = (String) partInfoMap.get("attribute[JF_PartNumber]");
                int childLevel = Integer.parseInt(UIUtil.getValue(partInfoMap, SELECT_LEVEL));
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
//                    JF_LOGGER.info("@@@@@@@@@@@@@@@@@@添加到集合的子集:{}",partInfoMap);
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

        String strTime = getTimeCuo();
        strTime = "JF" + strTime;

        //part集合 去重。
        Set partSet=new HashSet();

//        JSONArray partsArray=new JSONArray();
//        JSONArray bomArray=new JSONArray();


        //获取所有的mbom对应的完整颜色码，返回的fullNumberMap key是mbom节点id，value是对应的包含颜色码的编码集合 U0005767(partnumber去掉第一位)AA(大版本)D16(颜色码) U0005767AAD16
        Map<String, Map> fullNumberMap = getMbomInformationPartsJson2(context, EBomTypeSelectList, ebom, latestMatrixId, colorStyle, projectId,actualDataList  );
        JF_LOGGER.info("fullNumberMap--->"+fullNumberMap);

        Map relMap=new HashMap();
        //开始写入数据
        for (int i = 0; i < actualDataList.size(); i++) {
            Map partInfoMap = (Map) actualDataList.get(i);
            partInfoMap.put("ITEM",String.valueOf(i+1));
            String strPartId = (String) partInfoMap.get(SELECT_ID);
            String strLevel = (String) partInfoMap.get(SELECT_LEVEL);
            String strRevision = (String) partInfoMap.get(SELECT_REVISION);
            String JF_PartNumber = (String) partInfoMap.get("attribute[JF_PartNumber]");
            if(JF_PartNumber.equals("GF0001137")||JF_PartNumber.equals("GU0006005")){
                JF_LOGGER.info("-----lxg-GF0001137-->"+partInfoMap);
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
                String ebomPhysicalId = UIUtil.getValue(ebomInfo, SELECT_PHYSICAL_ID);
                String ebomId = UIUtil.getValue(ebomInfo, SELECT_ID);
                // MDM同步按当前项目读取客户零件号关系属性，避免继续使用零件对象上的旧属性。
                if (!customerPartsDBInfoCache.containsKey(ebomId)) {
                    customerPartsDBInfoCache.put(ebomId,
                            dr.getDRCustomerPartsDBRelationInfo(context, ebomId, projectId));
                }
                Map customerPartsDBInfo = customerPartsDBInfoCache.get(ebomId);
                //20260727 update by ljr MDM同步时，未维护项目对应客户零件/DB信息的零件按non-DB传递；
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
                String detailCategory = UIUtil.getValue(ebomInfo, JF_PLMConstants_mxJPO.SELECT_ATTR_JF_Detail_CN);
                //获取缩略图
                String strImgId = jfExportEBOMMxJPO.getDownLoadPictureByPartId(context, ebomPhysicalId, strPrePath);
                partInfoMap.put("picture",strImgId);
                partInfoMap.put("attribute[JF_DirectBuy]", directBuy);
                partInfoMap.put("attribute[CustomerPartNumber]",ebomCustomerPartNumber);
                partInfoMap.put("attribute[JF_VPMReference.JF_CustomerPartRevision]",ebomCustomerPartRevision);
                partInfoMap.put(JF_PLMConstants_mxJPO.SELECT_ATTR_JF_Detail_CN, detailCategory);


                if (UIUtil.isNotNullAndNotEmpty(latestMatrixId)){
                    //有颜色矩阵才去写关于颜色的信息
                    DomainObject ebomObj = DomainObject.newInstance(context, ebomId);
                    //整椅关联的项目和物理产品的颜色分组关系
                    MapList projectMapList = ebomObj.getRelatedObjects(context, "JFProject2ColorGroup", "Project Space", StringList.create("id"),
                            StringList.create("id[connection]","attribute[JF_ColorGroupName]","attribute[JF_ColorMatrixName]"), true, false, (short) 1, "", "", (short) 0);
                    JF_LOGGER.info("projectMapList---lxg-->"+projectMapList.size());


                    if (CollectionUtils.isEmpty(projectMapList)){
                        //EBOM没有对对应的颜色分组的时候
                        //判断是否是供货价，供货件取UA，非供货件取NA
                        boolean flag = ebom.getPartIsSupplyPart(context, new String[]{ebomId,projectId});
                        String attributeValue = flag ? "UA" : "NA";
//                        String attributeValue ="UA";
//                        writeRowIndex = getWriteRowIndex(context, workbook, sheet, lastColumnIndex, MBOMMappingList, colorStyle, writeRowIndex, basicCellStyle, partInfoMap, strRevision, JF_PartNumber, attributeValue);
                        getMbomjson(context, MBOMMappingList, colorStyle,partInfoMap, strRevision, JF_PartNumber, attributeValue,strTime,partsArray,bomArray,partSet, fullNumberMap,zeroLevelPart,relMap,rollPartExistenceCache);


                    }else {
                        boolean isColor = false;//add by caipan
                        for (Object o : projectMapList) {
                            Map projectMap = (Map) o;
                            String id = UIUtil.getValue(projectMap, "id");
                            if (Objects.equals(id,projectId)){
                                isColor = true;
                                String JF_ColorGroupName = UIUtil.getValue(projectMap, "attribute[JF_ColorGroupName]");
//                                writeRowIndex = getWriteRowIndex(context, workbook, sheet, lastColumnIndex, MBOMMappingList, colorStyle, writeRowIndex, basicCellStyle, partInfoMap, strRevision, JF_PartNumber, JF_ColorGroupName);
                                getMbomjson(context, MBOMMappingList, colorStyle, partInfoMap, strRevision, JF_PartNumber, JF_ColorGroupName,strTime,partsArray,bomArray,partSet, fullNumberMap,zeroLevelPart,relMap,rollPartExistenceCache);
                            }
                        }
                        if(!isColor) {
                            boolean flag = ebom.getPartIsSupplyPart(context, new String[]{ebomId, projectId});
                            String attributeValue = flag ? "UA" : "NA";
//                        String attributeValue ="UA";
//                        writeRowIndex = getWriteRowIndex(context, workbook, sheet, lastColumnIndex, MBOMMappingList, colorStyle, writeRowIndex, basicCellStyle, partInfoMap, strRevision, JF_PartNumber, attributeValue);
                            getMbomjson(context, MBOMMappingList, colorStyle, partInfoMap, strRevision, JF_PartNumber, attributeValue, strTime, partsArray, bomArray, partSet, fullNumberMap, zeroLevelPart, relMap, rollPartExistenceCache);
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
//                    writeDataInExcel(context,workbook,sheet,writeRowIndex,MBOMMappingList,partInfoMap,basicCellStyle,lastColumnIndex);
//                    writeRowIndex ++;
                    getPartInfoMapJson(context,partsArray,bomArray,partInfoMap,strRevision,strTime,partSet, fullNumberMap,zeroLevelPart,relMap,rollPartExistenceCache);

                }
            }

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

    /**
     * 保留原方法签名，兼容已有Java/JPO调用。
     */
    public void getMbomjson(Context context, MapList MBOMMappingList, Map colorStyle, Map partInfoMap,
                            String strRevision, String JF_PartNumber, String JF_ColorGroupName,
                            String strTime, JSONArray partsArray, JSONArray bomArray, Set partSet,
                            Map fullNumberMap, StringList zeroLevelPart, Map relMap) throws Exception {
        getMbomjson(context, MBOMMappingList, colorStyle, partInfoMap, strRevision, JF_PartNumber,
                JF_ColorGroupName, strTime, partsArray, bomArray, partSet, fullNumberMap,
                zeroLevelPart, relMap, new HashMap<>());
    }

    public void getMbomjson(Context context, MapList MBOMMappingList, Map colorStyle, Map partInfoMap, String strRevision, String JF_PartNumber, String JF_ColorGroupName,String strTime,JSONArray partsArray,JSONArray bomArray,Set partSet,Map fullNumberMap,StringList zeroLevelPart,Map relMap,Map<String, Boolean> rollPartExistenceCache) throws Exception {
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
                getPartInfoMapJson(context,partsArray,bomArray,partInfoMap,strRevision,strTime,partSet,fullNumberMap,zeroLevelPart,relMap,rollPartExistenceCache);

            }
        }else {
//            JF_LOGGER.info("partInfoMap:{}",partInfoMap);
            partInfoMap.put("usage","");
            partInfoMap.put("styleTitle","");
//            writeDataInExcel(context,workbook,sheet,writeRowIndex,MBOMMappingList,partInfoMap,basicCellStyle,lastColumnIndex);
            getPartInfoMapJson(context,partsArray,bomArray,partInfoMap,strRevision,strTime,partSet,fullNumberMap,zeroLevelPart,relMap,rollPartExistenceCache);

        }

        String partId = UIUtil.getValue(partInfoMap, "id");
        String FullNumber=UIUtil.getValue(partInfoMap, "InternalFullPartNumber");
        if(FullNumber.equals("F0001137AANUL")){
            JF_LOGGER.info("partId--lxg-->"+partId);
            JF_LOGGER.info("map--lxg-->"+partInfoMap);
        }


    }

    /**
     * 保留原方法签名，兼容已有Java/JPO调用。
     */
    public void getPartInfoMapJson(Context context, JSONArray partsArray, JSONArray bomArray, Map map,
                                   String strRevision, String strTime, Set partSet, Map fullNumberMap,
                                   StringList zeroLevelPart, Map relMap) throws Exception {
        getPartInfoMapJson(context, partsArray, bomArray, map, strRevision, strTime, partSet,
                fullNumberMap, zeroLevelPart, relMap, new HashMap<>());
    }

    public void getPartInfoMapJson(Context context,JSONArray partsArray,JSONArray bomArray,Map map,String strRevision,String strTime,Set partSet,Map fullNumberMap,StringList zeroLevelPart,Map relMap,Map<String, Boolean> rollPartExistenceCache)throws Exception{
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
        setMdmPartNumberAndVersion(context, partJson, map, JF_PartNumber, strRevision,
                rollPartExistenceCache);
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

    /**
     * 设置MDM零件号和版本。样板包、松紧带、嵌条优先使用系统中已存在的固定版本卷料零件；
     * 卷料号为空或固定版本卷料不存在时，回退MBOM自身零件号和完整版本。
     * 其他详细分类继续沿用原有的零件号和大版本同步规则。
     */
    private void setMdmPartNumberAndVersion(Context context, JSONObject partJson, Map mbomInfo,
                                             String mbomPartNumber, String mbomRevision,
                                             Map<String, Boolean> rollPartExistenceCache) throws Exception {
        String originalMdmRevision = UIUtil.isNullOrEmpty(mbomRevision)
                ? "" : mbomRevision.split("\\.")[0];
        partJson.put("PartNumber", mbomPartNumber);
        partJson.put("Version", originalMdmRevision);

        String detailCategory = UIUtil.getValue(
                mbomInfo, JF_PLMConstants_mxJPO.SELECT_ATTR_JF_Detail_CN).trim();
        if (!MDM_TRIM_ROLL_DETAIL_CATEGORIES.contains(detailCategory)) {
            return;
        }

        //三类面套零件即使卷料不可用，也需要传MBOM本身的完整版本。
        partJson.put("Version", mbomRevision);
        String rollPartNumber = UIUtil.getValue(
                mbomInfo, "attribute[" + JF_PLMConstants_mxJPO.ATTR_JF_ROLL_PART_NUMBER + "]").trim();
        if (UIUtil.isNullOrEmpty(rollPartNumber)) {
            return;
        }

        String cacheKey = rollPartNumber + "|" + MDM_TRIM_ROLL_REVISION;
        Boolean rollPartExists = rollPartExistenceCache.get(cacheKey);
        if (rollPartExists == null) {
            Map rollPartInfo = getEbomInfo(context, rollPartNumber, MDM_TRIM_ROLL_REVISION,
                    StringList.create(SELECT_ID));
            rollPartExists = MapUtils.isNotEmpty(rollPartInfo);
            rollPartExistenceCache.put(cacheKey, rollPartExists);
        }
        if (rollPartExists) {
            partJson.put("PartNumber", rollPartNumber);
            partJson.put("Version", MDM_TRIM_ROLL_REVISION);
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

    public void test(Context context,String[] args) throws Exception{
        String objectId = args[0];
        DomainObject domainObject = DomainObject.newInstance(context, objectId);
        StringList typeSelectList = JF_Util_mxJPO.basicBolistSel();
        typeSelectList.add(SELECT_PHYSICAL_ID);
        typeSelectList.add(SELECT_LEVEL);
        typeSelectList.add("attribute[JF_PartNumber]");
        StringList relSelectList = JF_Util_mxJPO.basicRellistSel();
        relSelectList.add(SELECT_FROM_ID);
        relSelectList.add("attribute[JF_Dosage]");
        MapList partMapList = domainObject.getRelatedObjects(context,
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
        JF_LOGGER.info("partMapList===:{}", JSON.toJSONString(partMapList));
    }


    public void TestMDM(Context context,String []args)throws Exception{
        try {
            String ProjectID=args[0];
            String Factory=args[1];
            Map pmap=new HashMap();
            pmap.put("ProjectID",ProjectID);
            pmap.put("Factory",Factory);

            getMBOMInfoInterface(context,JPO.packArgs(pmap));


//            String tojson="{\n" +
//                    "    \"Head\": {\n" +
//                    "        \"Plant\": \"9061\",\n" +
//                    "        \"ProjectName\": \"SIT测试项目-OK\",\n" +
//                    "        \"ProjectId\": \"BGCS24-2025\"\n" +
//                    "    },\n" +
//                    "    \"BOM\": [\n" +
//                    "        {\n" +
//                    "            \"ParentID\": \"C0003049DDSAC0\",\n" +
//                    "            \"ChildID\": \"T0021430NULAC0\",\n" +
//                    "            \"Dosage\": \"1\",\n" +
//                    "            \"IsVirtual\": \"N\"\n" +
//                    "        }\n" +
//                    "    ],\n" +
//                    "    \"Parts\": [\n" +
//                    "        {\n" +
//                    "            \"SellableItem\": \"N\",\n" +
//                    "            \"FullNumber\": \"C0003049DDSAC0\",\n" +
//                    "            \"Description\": \"MBOM测试整椅1_深咖色\",\n" +
//                    "            \"PartNumber\": \"GC0003049\",\n" +
//                    "            \"Customer\": \"\",\n" +
//                    "            \"DescriptionEN\": \"MBOM测试整椅1\",\n" +
//                    "            \"CustomerPartNumber\": \"\",\n" +
//                    "            \"Unit\": \"PC\",\n" +
//                    "            \"ProcurementType\": \"make\",\n" +
//                    "            \"DirectBuy\": \"non-DB\",\n" +
//                    "            \"ColorCode\": \"DDS\",\n" +
//                    "            \"ColorDescription\": \"颜色风格4\",\n" +
//                    "            \"Version\": \"AC\",\n" +
//                    "            \"CustomerPartRevision\": \"\",\n" +
//                    "            \"ECNNumber\": \"JF2512120603\",\n" +
//                    "            \"SpecialType \": \"NA\",\n" +
//                    "            \"Type \": \"C\"\n" +
//                    "        },\n" +
//                    "        {\n" +
//                    "            \"SellableItem\": \"N\",\n" +
//                    "            \"FullNumber\": \"T0021430NULAC0\",\n" +
//                    "            \"Description\": \"面套件总成_NUL\",\n" +
//                    "            \"PartNumber\": \"GT0021430\",\n" +
//                    "            \"Customer\": \"\",\n" +
//                    "            \"DescriptionEN\": \"Driver's seat inner side panel assembly\",\n" +
//                    "            \"CustomerPartNumber\": \"\",\n" +
//                    "            \"Unit\": \"PC\",\n" +
//                    "            \"ProcurementType\": \"make\",\n" +
//                    "            \"DirectBuy\": \"non-DB\",\n" +
//                    "            \"ColorCode\": \"NUL\",\n" +
//                    "            \"ColorDescription\": \"颜色风格1,颜色风格2,颜色风格3,颜色风格4,颜色风格5\",\n" +
//                    "            \"Version\": \"AC\",\n" +
//                    "            \"CustomerPartRevision\": \"\",\n" +
//                    "            \"ECNNumber\": \"JF2512120603\",\n" +
//                    "            \"SpecialType \": \"NA\",\n" +
//                    "            \"Type \": \"T\"\n" +
//                    "        }\n" +
//                    "    ]\n" +
//                    "}";
//
//
//
//            JSONObject testjson=new JSONObject();
//            JSONObject Head=new JSONObject();
//            JSONArray BOM=new JSONArray();
//            JSONArray Parts=new JSONArray();
//
//            JSONObject Part1=new JSONObject();
//            JSONObject Part2=new JSONObject();
//            JSONObject bomrel=new JSONObject();
//
//            Head.put("Plant","9061");
//            Head.put("ProjectName","SIT测试项目-OK");
//            Head.put("ProjectId","BGCS24-2025");
//
//
//            Part1.put("FullNumber", "C0003049DDSAC0" );
//            Part1.put("Description", "MBOM测试整椅1_深咖色");
//            Part1.put("DescriptionEN", "MBOM测试整椅1");
//            Part1.put("Unit", "PC");
//            Part1.put("ProcurementType", "make");
//            Part1.put("DirectBuy", "non-DB");
//            Part1.put("SellableItem", "N");
//            Part1.put("PartNumber", "GC0003049");
//            Part1.put("Version", "AC");
//            Part1.put("ColorCode","DDS" );
//            Part1.put("ColorDescription","颜色风格4");
//            Part1.put("CustomerPartNumber", "");
//            Part1.put("CustomerPartRevision", "");
//            Part1.put("Customer", "");
//            Part1.put("SpecialType ", "NA");
//            Part1.put("Type ", "C");
//            Part1.put("ECNNumber", "JF2512120604");
//
//            Part2.put("FullNumber", "T0021430NULAC0" );
//            Part2.put("Description", "MBOM深咖色");
//            Part2.put("DescriptionEN", "Driver's seat inner side panel assembly");
//            Part2.put("Unit", "PC");
//            Part2.put("ProcurementType", "make");
//            Part2.put("DirectBuy", "non-DB");
//            Part2.put("SellableItem", "N");
//            Part2.put("PartNumber", "GT0021430");
//            Part2.put("Version", "AC");
//            Part2.put("ColorCode","DDS" );
//            Part2.put("ColorDescription","颜色风格4");
//            Part2.put("CustomerPartNumber", "");
//            Part2.put("CustomerPartRevision", "");
//            Part2.put("Customer", "");
//            Part2.put("SpecialType ", "NA");
//            Part2.put("Type ", "C");
//            Part2.put("ECNNumber", "JF2512120604");
//
//
//            bomrel.put("ParentID","C0003049DDSAC0");
//            bomrel.put("ChildID","T0021430NULAC0");
//            bomrel.put("Dosage","1");
//            bomrel.put("IsVirtual","N");
//
//            Parts.add(Part1);
//            Parts.add(Part2);
//            BOM.add(bomrel);
//            testjson.put("Head",Head);
//            testjson.put("BOM",BOM);
//            testjson.put("Parts",Parts);
//
//
//
//
//
//
//            String url = JF_PublicMethodClass_mxJPO.getBasicUrl(context, new String[]{"JFSyncMBOM.ESB.URL"});
//            Map headerMap = new HashMap();
//            headerMap.put("jf_svc", "IOA0185");
//            headerMap.put("jf_applicationid", UUID.randomUUID() + "");
//            headerMap.put("jf_sender", "PLM");
//            headerMap.put("jf_receiver", "MDM");
//            headerMap.put("jf_document", UUID.randomUUID() + "");
//
//            JF_LOGGER.info("MDMSYNC---->start");
////            JSONObject datajson=JSON.parseObject(tojson);
////            tojson=tojson.replaceAll("'","\'");
//            JF_LOGGER.info("MDMSYNC--tojson-->"+testjson.toString());
//            String sult=JF_ProjectSpace_mxJPO.dopost(testjson.toString(), url, headerMap);
//            JF_LOGGER.info("MDMSYNC---->end");

        }catch (Exception e){
            e.printStackTrace();
        }
    }


    public void modMBOMRev(Context context,String[]args)throws Exception{
        String id=args[0];
        DomainObject gcObj=DomainObject.newInstance(context,id);
        StringList typeSelectList=JF_Util_mxJPO.basicBolistSel();
        StringList relSelectList=JF_Util_mxJPO.basicRellistSel();

        MapList partMapList = gcObj.getRelatedObjects(context,
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

        for(int i=0;i<partMapList.size();i++){
            Map partmap= (Map) partMapList.get(i);
            String name= (String) partmap.get(SELECT_NAME);
            String rev= (String) partmap.get(SELECT_REVISION);
            String partid= (String) partmap.get(SELECT_ID);
            String mql2 = "mod bus '" + partid + "' name '" + name + "' revision '"+rev+"-000'";
            MqlUtil.mqlCommand(context, false, mql2, true);

        }
    }

    /**
     * MBOM同步MDM接口
     */
    public JSONObject getMBOMInfoInterface(Context context, String[]args)throws Exception {
        JF_LOGGER.info("getMBOMInfoInterface---->start");
        JF_LOGGER.info("getMBOMInfoInterface---->start");
        JF_LOGGER.info("getMBOMInfoInterface---->start");
        JF_LOGGER.info("getMBOMInfoInterface---->start");
        JF_LOGGER.info("getMBOMInfoInterface---->start");
        JF_LOGGER.info("getMBOMInfoInterface---->start");
        JF_LOGGER.info("getMBOMInfoInterface---->start");
        JF_LOGGER.info("getMBOMInfoInterface---->start");
        JF_LOGGER.info("getMBOMInfoInterface---->start");
        JF_LOGGER.info("getMBOMInfoInterface---->start");
        JF_LOGGER.info("getMBOMInfoInterface---->start");
        JF_LOGGER.info("getMBOMInfoInterface---->start");
        JF_LOGGER.info("getMBOMInfoInterface---->start");
        JF_LOGGER.info("getMBOMInfoInterface---->start");
        JF_LOGGER.info("getMBOMInfoInterface---->start");
        JF_LOGGER.info("getMBOMInfoInterface---->start");
        JF_LOGGER.info("getMBOMInfoInterface---->start");
        JF_LOGGER.info("getMBOMInfoInterface---->start");
        JF_LOGGER.info("getMBOMInfoInterface---->start");
        JSONObject returnJson = new JSONObject();

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
        JSONArray partsArray = new JSONArray();
        JSONArray bomArray = new JSONArray();

        try {
            HashMap programMap = (HashMap) JPO.unpackArgs(args);
            String projectId = (String) programMap.get("ProjectID");
            String Factory = (String) programMap.get("Factory");
//            String  projectId = args[0];
            DomainObject projectObj = DomainObject.newInstance(context, projectId);
            String topMbomId = projectObj.getInfo(context, "from[JF_relProject2MBOM].to.id");
            DomainObject topMbomObj = DomainObject.newInstance(context, topMbomId);
            JF_VPMReferenceEBOM_mxJPO ebom = new JF_VPMReferenceEBOM_mxJPO();


//            String url = "http://172.16.33.183:7080/JFSEATesb/Services/MDM/MBOM";

            String url = JF_PublicMethodClass_mxJPO.getBasicUrl(context, new String[]{"JFSyncMBOM.ESB.URL"});
            //根据工厂分割
            String[] factorys = Factory.split("@@");
            StringBuffer errorcode=new StringBuffer();
            StringBuffer detail=new StringBuffer();


            Map errormap=checkOnePartChild(context,projectId);
            if(errormap.size()==0){
                getExportMBOMInfoForSync(context,projectId,"GC",partsArray,bomArray);
                for (int i = 0; i < factorys.length; i++) {

                    String strTime = getTimeCuo();
                    strTime = "JF" + strTime;

                    for(Object po:partsArray){
                        JSONObject partobj= (JSONObject) po;
                        partobj.put("ECNNumber",strTime);
                    }



                    String ProjectId = projectObj.getName(context);
                    String ProjectName = projectObj.getDescription(context);


                    String Plant = factorys[i];
                    JSONObject headjson = new JSONObject();
                    headjson.put("Plant", Plant);
                    headjson.put("ProjectId", ProjectId);
                    headjson.put("ProjectName", ProjectName);

//                JF_LOGGER.info("headjson---->"+headjson.toString());


                    JSONObject dataJson = new JSONObject();
                    dataJson.put("BOM", bomArray);
                    dataJson.put("Parts", partsArray);
                    dataJson.put("Head", headjson);
                    // 将 dataJson 写入文件

                    //需要发送bi

                    Map headerMap = new HashMap();
                    headerMap.put("jf_svc", "IOA0185");
                    headerMap.put("jf_applicationid", UUID.randomUUID() + "");
                    headerMap.put("jf_sender", "PLM");
                    headerMap.put("jf_receiver", "MDM");
                    headerMap.put("jf_document", UUID.randomUUID() + "");

                    JF_LOGGER.info("MDMSYNC---->start");
//                    String tojson=JSON.toJSONString(dataJson,SerializerFeature.PrettyFormat);
                    String tojson=JSON.toJSONString(dataJson);
//                    Pattern QUOTE_PATTERN = Pattern.compile("\"");
//                    tojson=QUOTE_PATTERN.matcher(tojson).replaceAll("\\\\\"");
//                    Pattern SINGLE_QUOTE_PATTERN = Pattern.compile("'");
//                    tojson=SINGLE_QUOTE_PATTERN.matcher(tojson).replaceAll("\\\\'");

//                    tojson=tojson.replaceAll("'","\\'");
                    JF_LOGGER.info("MDMSYNC--tojson-->"+tojson);
                    String sult=JF_ProjectSpace_mxJPO.dopost(tojson, url, headerMap);
//                    String sult="{\"DATA\":{\"MESSAGE\":{\"status\":\"success\",\"msg\":\"同步成功\"},\"TYPE\":\"S\"}}";
                    JF_LOGGER.info("MDMSYNC---->end");
                    JSONObject dataJson2=JSONObject.parseObject(sult);
                    String strJsonPath = JF_PublicMethodClass_mxJPO.getBasicUrl(context, new String[]{"MBOMToMDM.JsonPath"});
                    JF_LOGGER.info("MDMSYNC--sult-->"+sult);
                    dataJson.put("result",dataJson2);
                    String timedateStr="JF_"+getTimeCuo2();

                    String tojson2=JSON.toJSONString(dataJson,SerializerFeature.PrettyFormat);
//                    tojson2=tojson2.replaceAll("'","\\\\'");
//                    JF_LOGGER.info("MDMSYNC--tojson2-->"+tojson2);
                    writeJsonToFile(tojson2, strJsonPath+ProjectId+Plant+timedateStr+".json");
                    String errorcodeStr="";
                    String detailStr="";
                    if(dataJson2.containsKey("error")){
                        //接口ESB不通
                        JSONObject errorjson=dataJson2.getJSONObject("error");
                        if(errorjson.containsKey("code")&&errorjson.containsKey("detail")){
                            errorcodeStr=errorjson.get("code")+"";
                            detailStr=errorjson.get("detail")+"";
                        }
                    }else {
                        //MDM接口返回错误信息
                        JSONObject DATA=dataJson2.getJSONObject("DATA");
                        String typeStr=DATA.getString("TYPE");
                        if("E".equals(typeStr)){
                            JSONObject MESSAGEObj=DATA.getJSONObject("MESSAGE");
                            detailStr=MESSAGEObj.getString("msg");
                            detailStr=detailStr.replaceAll("[\\r\\n]+", "");
                            JF_LOGGER.info("MDMSYNC--detailStr-->"+detailStr);

                            errorcodeStr=MESSAGEObj.getString("status");
                        }
                    }


                    if(UIUtil.isNotNullAndNotEmpty(errorcodeStr)&&UIUtil.isNotNullAndNotEmpty(detailStr)){
                        errorcode.append(errorcodeStr).append(".");
                        detail.append(detailStr).append(".");
                    }else {
                        //同步后新增一条同步记录
                        //获取最新的更新记录，有就有获取对应的更新时间，人员，以及获取当时的根节点上的更新原因
                        MapList changeRecordList=getPsChangeRecord(context,projectId);
                        String JF_UpdateDateTime="";
                        if(changeRecordList.size()>0){
                            Map lastchangeRecord= (Map) changeRecordList.get(0);
                            String changeRecordId= (String) lastchangeRecord.get(SELECT_ID);
                            DomainObject changeRecordObj=DomainObject.newInstance(context,changeRecordId);
                            //获取最新的更新时间用于同步记录对象上记录更新时间,PM说明，AME说明
                            JF_UpdateDateTime=changeRecordObj.getAttributeValue(context,"JF_UpdateDateTime");
                        }
                        String JF_PMChangeDesc=topMbomObj.getAttributeValue(context,"JF_PMChangeDesc");
                        String JF_AMEChangeDesc=topMbomObj.getAttributeValue(context,"JF_AMEChangeDesc");
                        //创建同步对象记录
                        DomainObject domainObject = DomainObject.newInstance(context);
                        String sObjGeneratorName = UICache.getObjectGenerator(context, TYPE_type_JF_ChangeRecord, "");
                        String sName = DomainObject.getAutoGeneratedName(context, sObjGeneratorName, "");
                        domainObject.createObject(context, "JF_SyncMBOMRecord", "JFSyncMBOMRecord"+sName, "A", "JF_SyncMBOMRecord", context.getVault().getName());

                        //设置属性
                        HashMap<String, String> attrMap = new HashMap<>();
                        attrMap.put(ATTR_JF_UpdatePerson, context.getUser());
                        //前台时间转换为数据库时间
                        LocalDateTime now = LocalDateTime.now();
                        // 格式化整个日期时间字符串
                        String formattedDateTime = now.format(inputFormatter);
                        attrMap.put("JF_SyncMBOMUpdateDateTime", formattedDateTime);
                        attrMap.put("JF_PMChangeDesc", JF_PMChangeDesc);
                        attrMap.put("JF_AMEChangeDesc", JF_AMEChangeDesc);
                        //新增同步工厂
                        attrMap.put("JFAffectedFactory", Plant);
                        if(UIUtil.isNotNullAndNotEmpty(JF_UpdateDateTime)){
                            attrMap.put(ATTR_JF_UpdateDateTime, JF_UpdateDateTime);
                        }
                        domainObject.setAttributeValues(context, attrMap);
                        JF_Util_mxJPO.changeowner(context, domainObject.getId(context), projectId,context.getUser());
                        //导出并checkin文件
                        exporSyncMBOMExcel(context,projectId,domainObject);
                        DomainRelationship.connect(context,topMbomObj,"JF_relMBOM2SyncRecord",domainObject);
                    }

                }

                String finalErrorCode=errorcode.toString();
                String finalDetail=detail.toString();

                if(UIUtil.isNotNullAndNotEmpty(finalErrorCode)&&UIUtil.isNotNullAndNotEmpty(finalDetail)){
                    returnJson.put("code", "500");
                    returnJson.put("msg", finalDetail);
                    returnJson.put("result", "failed");
                }else {
                    returnJson.put("code", "200");
                    returnJson.put("msg", "");
                    returnJson.put("result", "success");
                }

            }else {
                Set keyset=errormap.keySet();
                StringBuffer sb=new StringBuffer();
                for(Object o:keyset){
                    String partNumber= (String) o;
                    sb.append(partNumber).append(",");
                }

                String errormsg = (String) EnoviaResourceBundle.getFrameworkStringResourceProperty(context, "emxFramework.SyncMBOMToMDM.ErrorMsg", context.getLocale());

                sb.append(errormsg);

                returnJson.put("code", "500");
                returnJson.put("msg", sb.toString());
                returnJson.put("result", "failed");
            }


        } catch (Exception e) {
            e.printStackTrace();
            returnJson.put("code", "500");
//            returnJson.put("msg", e.getMessage());
            returnJson.put("msg", "\u540c\u6b65\u5931\u8d25\uff0c\u53ef\u80fd\u63a5\u53e3\u670d\u52a1\u672a\u542f\u52a8\uff0c\u8bf7\u8054\u7cfb\u7ba1\u7406\u5458");
            returnJson.put("result", "failed");
        }

        JF_LOGGER.info("MDMSYNC--returnJson-->"+returnJson);
        return returnJson;
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




    public void getBOMInfoJson(Context context, Map<String, Set<String>> fullNumberMap, StringList typeSelectList, StringList relSelectList, JSONArray bomArray) throws Exception {
        fullNumberMap.forEach((key, value) -> {
            //mbom节点id
            String id = key;
//            JF_LOGGER.info("------partId:{}",id);
            Set<String> fullNumberList = value;
            try {
                //获取下一层mbom
                DomainObject partObj = DomainObject.newInstance(context, id);
                MapList partMapList = partObj.getRelatedObjects(context,
                        JF_PLMConstants_mxJPO.REL_JF_relManufacturedItem,
                        JF_PLMConstants_mxJPO.TYPE_JF_ManufacturedItem,
                        typeSelectList,
                        relSelectList,
                        false,
                        true,
                        (short) 1,
                        "",
                        "",
                        (short) 0);
                for (Object o : partMapList) {
                    //遍历子Mbom
                    Map<String, Object> childMap = (Map<String, Object>) o;
                    String JF_Dosage = UIUtil.getValue(childMap, "attribute[JF_Dosage]");
                    String childId = UIUtil.getValue(childMap, "id");
                    Set<String> childFullNumberList = fullNumberMap.get(childId);

                    // 检查 childFullNumberList 是否为 null
                    if (childFullNumberList == null) {
                        continue;
                    }
                    for (String fullNumber : fullNumberList) {
                        for (String childFullNumber : childFullNumberList) {
                            JSONObject jsonObject = new JSONObject();
                            jsonObject.put("ParentID", fullNumber);
                            jsonObject.put("ChildID", childFullNumber);
                            jsonObject.put("IsVirtual", "N");
                            jsonObject.put("Dosage", JF_Dosage);
                            bomArray.add(jsonObject);
                        }
                    }
                }
            } catch (FrameworkException e) {
                JF_LOGGER.error("--getBOMInfoJson--error for id {}: {}", id, e.getMessage());
            }
        });
    }

    // 将 JSON 对象写入文件的方法
    public void writeJsonToFile(String jsonString, String filePath) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            // 使用 fastjson 将 JSONObject 转换为格式化的字符串
//            String jsonString = JSON.toJSONString(jsonObject, SerializerFeature.PrettyFormat);
            writer.write(jsonString);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void processManufacturedItems(Context context, String gcId, StringList typeSelectList, StringList relSelectList, Map fullNumberMap, JSONArray bomArray,Set<String> processedIds) throws Exception {
        // 检查当前 零件 是否已经处理过
        if (processedIds.contains(gcId)) {
            return;
        }
        // 将当前 零件 添加到已处理集合中
        processedIds.add(gcId);

        DomainObject gcObj = DomainObject.newInstance(context, gcId);
        List fullNumberList = (List) fullNumberMap.get(gcId);
        MapList partMapList = gcObj.getRelatedObjects(context,
                JF_PLMConstants_mxJPO.REL_JF_relManufacturedItem, // relationship pattern
                JF_PLMConstants_mxJPO.TYPE_JF_ManufacturedItem,    // object pattern
                typeSelectList,                                    // object selects
                relSelectList,                                     // relationship selects
                false,                                             // to direction
                true,                                              // from direction
                (short) 1,                                        // recursion level
                "",                                                // object where clause
                "",
                (short) 0);

        // 处理当前项的所有子项
        for (Object o : partMapList) {
            Map partMap = (Map) o;
            String partId = (String) partMap.get(SELECT_ID);
            String JF_Dosage = (String) partMap.get("attribute[JF_Dosage]");
            List childFullNumberList = (List) fullNumberMap.get(partId);
            for (Object o1 : fullNumberList) {
                String fullNumber = (String) o1;
                for (Object o2 : childFullNumberList) {
                    String childFullNumber = (String) o2;
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("ParentID",fullNumber);
                    jsonObject.put("ChildID",childFullNumber);
                    jsonObject.put("IsVirtual","N");
                    jsonObject.put("Dosage",JF_Dosage);
                    bomArray.add(jsonObject);
                }
            }
            // 递归调用处理子项
            processManufacturedItems(context, partId,typeSelectList,relSelectList, fullNumberMap, bomArray,processedIds);
        }
    }

//    public void processManufacturedItems(Context context, String gcId, StringList typeSelectList, StringList relSelectList, Map fullNumberMap, JSONArray bomArray) throws Exception {
//        Set<String> processedIds = new HashSet<>();
//        Stack<String> stack = new Stack<>();
//        stack.push(gcId);
//        while (!stack.isEmpty()) {
//            String currentGcId = stack.pop();
//
//            // 检查当前零件是否已经处理过
//            if (processedIds.contains(currentGcId)) {
//                continue;
//            }
//
//            // 将当前零件标记为已处理
//            processedIds.add(currentGcId);
//            DomainObject gcObj = DomainObject.newInstance(context, currentGcId);
//            List fullNumberList = (List) fullNumberMap.get(currentGcId);
//            MapList partMapList = gcObj.getRelatedObjects(context,
//                    JF_PLMConstants_mxJPO.REL_JF_relManufacturedItem,
//                    JF_PLMConstants_mxJPO.TYPE_JF_ManufacturedItem,
//                    typeSelectList,
//                    relSelectList,
//                    false,
//                    true,
//                    (short) 1,
//                    "",
//                    "",
//                    (short) 0);
//            // 处理子项
//            for (Object o : partMapList) {
//                Map partMap = (Map) o;
//                String partId = (String) partMap.get(SELECT_ID);
//                String JF_Dosage = (String) partMap.get("attribute[JF_Dosage]");
//                List childFullNumberList = (List) fullNumberMap.get(partId);
//                for (Object o1 : fullNumberList) {
//                    String fullNumber = (String) o1;
//                    for (Object o2 : childFullNumberList) {
//                        String childFullNumber = (String) o2;
//                        JSONObject jsonObject = new JSONObject();
//                        jsonObject.put("ParentID", fullNumber);
//                        jsonObject.put("ChildID", childFullNumber);
//                        jsonObject.put("IsVirtual", "N");
//                        jsonObject.put("Dosage", JF_Dosage);
//                        bomArray.add(jsonObject);
//                    }
//                }
//                // 将子项的 partId 推入堆栈以进行处理
//                stack.push(partId);
//            }
//            // 释放引用
//            gcObj = null; // 释放对象引用，帮助垃圾回收
//        }
//    }




    /**
     * mbom对应的ebom
     * @param context
     * @param topMbomObj
     * @param EBomTypeSelectList
     * @return
     * @throws Exception
     */
    public Map getMbomAndEbomMapping(Context context, DomainObject topMbomObj, StringList EBomTypeSelectList) throws Exception{
        Map retMap = new HashMap();
        MapList partMapList = topMbomObj.getRelatedObjects(context,
                JF_PLMConstants_mxJPO.REL_JF_relManufacturedItem, // relationship pattern
                JF_PLMConstants_mxJPO.TYPE_JF_ManufacturedItem,    // object pattern
                StringList.create("id"),                                    // object selects
                StringList.create("id[connection]"),                                     // relationship selects
                false,                                             // to direction
                true,                                              // from direction
                (short) 0,                                        // recursion level
                "",                                                // object where clause
                "",
                (short) 0);
        for (Object o : partMapList) {
            Map map = (Map) o;
            String partId = UIUtil.getValue(map,"id");
            String JF_PartNumber = UIUtil.getValue(map,"attribute[JF_PartNumber]");
            String revision = UIUtil.getValue(map,"revision");
            Map eBomMap = getEbomInfo(context,JF_PartNumber,revision,EBomTypeSelectList);
            if (MapUtils.isNotEmpty(eBomMap)){
                retMap.put(partId,eBomMap);
            }
        }
        return  retMap;
    }


    public MapList getPartMapListForSyncMDM(Context context,DomainObject topMbomObj)throws Exception{

        MapList allPartMapList=new MapList();

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



        StringList zeroLevelPart = topMbomObj.getInfoList(context, "from[JF_relManufacturedItem].to.id");

        Map<String, String> mbomConfigMap = getMBOMConfig(context);
        //获取表格映射
        MapList MBOMMappingList = JF_PublicMethodClass_mxJPO.getJFCostChangeHistoryTemplateMapping(context, "MBOM");

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



        //遍历一级件及其子集
        for (Object o : oneLevelMapInfo.entrySet()) {
            Map.Entry entry = (Map.Entry)o;
            String strOneLevelId = (String) entry.getKey();
            Map oneLevelInfo = (Map) entry.getValue();
            //添加一级件
            actualDataList.add(oneLevelInfo);
            String onePartNumber = UIUtil.getValue(oneLevelInfo,"attribute[JF_PartNumber]");
            String firstTwoCharacters = onePartNumber.substring(0, 2);
            DomainObject oneLevel = DomainObject.newInstance(context,strOneLevelId);

            MapList partMapList=new MapList();
            if ("false".equals(mbomConfigMap.get(firstTwoCharacters))){
                //此处需要单独处理
                //新需求 如果是false还是要展开，但是需要遍历完所有节点的新属性JF_SpecialProcurementType/不为NA的时候需要向上单向展开(双经销/外协/NA)
                //为NA的时候不需要保留
//                partMapList=checkOnePart(context,oneLevel,typeSelectList,relSelectList);
//                continue;
            }else {
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
            allPartMapList.addAll(partMapList);

//
//            //子件对应的gc id集合 因为用量需要和GCid对应起来
//            StringList mappingGCId = getMappingGCId(oneLevelInfo, zeroLevelPart);
//
//            JF_LOGGER.info("---lxg-mappingGCId->"+mappingGCId);
//            int level = Integer.MAX_VALUE;
//            Map<String, Object> sameBomeMap = new HashMap<>();
//            //合并一级件下面的用量
//            for (Object o1 : partMapList) {
//                Map partInfoMap = (Map) o1;
//                String childId = UIUtil.getValue(partInfoMap, "id");
//                String fromId = UIUtil.getValue(partInfoMap, SELECT_FROM_ID);
//                String JF_Dosage = UIUtil.getValue(partInfoMap, "attribute[JF_Dosage]");
//                String strRevision = (String) partInfoMap.get(SELECT_REVISION);
//                String JF_PartNumber = (String) partInfoMap.get("attribute[JF_PartNumber]");
//                int childLevel = Integer.parseInt(UIUtil.getValue(partInfoMap, SELECT_LEVEL));
//                if (childLevel > level) {
//                    //childLevel大于level的,说明该层的父级已经重复，不用合并用量
//                    continue;
//                }
//                level = Integer.MAX_VALUE;
//                StringBuilder sb = new StringBuilder();
//                sb.append(fromId);
//                sb.append(JF_PartNumber);
//                sb.append(strRevision);
//                //因为MBOM结构相同的JF_PartNumber和revision对应的id不一样 所以不能用id判断去重合并
//                String strUniqueKey = sb.toString();
//                if (!sameBomeMap.containsKey(strUniqueKey)){
//                    sameBomeMap.put(strUniqueKey, partInfoMap);
//                    for (String strGCId : mappingGCId) {
//                        partInfoMap.put(strGCId,JF_Dosage);
//                    }
//                    //标识一级件下面子件
//                    partInfoMap.put("isSunPart",true);
//                    JF_LOGGER.info("@@@@@@@@@@@@@@@@@@添加到集合的子集:{}",partInfoMap);
//                    actualDataList.add(partInfoMap);
//                }else {
//                    level = childLevel;
//                    Map bommap = (Map) sameBomeMap.get(strUniqueKey);
//                    String dosageStr = (String) bommap.get("attribute[JF_Dosage]");
//                    if (UIUtil.isNullOrEmpty(dosageStr)){
//                        dosageStr = "0" ;
//                    }
//                    if (UIUtil.isNullOrEmpty(JF_Dosage)){
//                        JF_Dosage = "0" ;
//                    }
//                    JF_LOGGER.info("dosageStr:{}strChildDosage：{}",dosageStr,JF_Dosage);
//                    double dosage = Double.parseDouble(dosageStr) + Double.parseDouble(JF_Dosage);
//                    String strSumDosage = String.valueOf(dosage);
//                    for (String strGCId : mappingGCId) {
//                        bommap.put(strGCId,strSumDosage);
//                    }
//                    bommap.put("attribute[JF_Dosage]", strSumDosage);
//                }
//            }
        }



        return allPartMapList;
    }


    public Map<String, Set<String>> getMbomInformationPartsJson(Context context,  StringList eBomTypeSelectList,
                                                                JF_VPMReferenceEBOM_mxJPO ebom, String latestMatrixId, Map<String, MapList> colorStyle, String projectId,MapList partMapList) throws Exception {
        Map<String, Set<String>> retMap = new HashMap<>();
        Map<String, String> setMap = new HashMap<>();
        // 用于跟踪已处理的 internalFullPartNumber
        Set<String> processedFullNumbers = new HashSet<>();


        for (Object o : partMapList) {
            Map<String, Object> map = (Map<String, Object>) o;
            String partId = UIUtil.getValue(map, "id");

            String JF_PartNumber = UIUtil.getValue(map, "attribute[JF_PartNumber]");

            String revision = UIUtil.getValue(map, "revision");

            if (JF_PartNumber.startsWith("GX")) {
                continue;
            }
            //查询MBOM节点对应的ebom零件
            Map<String, Object> eBomMap = getEbomInfo(context, JF_PartNumber, revision, eBomTypeSelectList);
            //mod by 新增需求改造 ：当MBOM结构中存在“双经销”或“外协”件时，同步MDM及导出整椅MBOM结构时需要将对应零件的父级件进行输出，直至供货层级零件。
            //往上找找到供货层级结束，递归
//            JF_LOGGER.info("---lxg---eBomMap->"+eBomMap);

            //setMap去重
            if (MapUtils.isNotEmpty(eBomMap) && !setMap.containsKey(partId)) {
                setMap.put(partId, "");
                String ebomId = UIUtil.getValue(eBomMap, "id");
                Set<String> list = new HashSet<>();
                String internalRevision = revision.split("\\.")[0];
                String internalPartNumber = JF_PartNumber.substring(1);
                String internalFullPartNumber;
                if (UIUtil.isNotNullAndNotEmpty(latestMatrixId)) {
                    //颜色矩阵不为空
                    DomainObject ebomObj = DomainObject.newInstance(context, ebomId);
                    MapList projectMapList = ebomObj.getRelatedObjects(context, "JFProject2ColorGroup", "Project Space",
                            StringList.create("id"),
                            StringList.create("id[connection]", "attribute[JF_ColorGroupName]", "attribute[JF_ColorMatrixName]"),
                            true, false, (short) 1, "", "", (short) 0);

                    String JF_ColorGroupName = "";
                    if (CollectionUtils.isEmpty(projectMapList)) {
                        //判断当前零件是否是供货件,必须是所属项目的供货件
                        boolean flag = ebom.getPartIsSupplyPart(context, new String[]{ebomId, projectId});
                        JF_ColorGroupName = flag ? "UA" : "NA";
                    } else {
                        for (Object o1 : projectMapList) {
                            Map<String, Object> projectMap = (Map<String, Object>) o1;
                            String id = UIUtil.getValue(projectMap, "id");
                            if (Objects.equals(id, projectId)) {
                                //ebom零件通过JFProject2ColorGroup关联的项目，和MBOM关联的项目是同一个的话，获取颜色分组
                                JF_ColorGroupName = UIUtil.getValue(projectMap, "attribute[JF_ColorGroupName]");
                            }
                        }
                    }
                    //colorStyle是通过颜色矩阵获取的颜色风格的集合，key颜色分组，value颜色风格
                    //根据ebom零件的颜色分组name获取颜色矩阵中颜色分组对应的颜色风格
                    MapList styleMapList = colorStyle.get(JF_ColorGroupName);

//                    JF_LOGGER.info("---lxg---JF_ColorGroupName->"+JF_ColorGroupName);
//                    JF_LOGGER.info("---lxg---colorStyle->"+colorStyle);
//                    JF_LOGGER.info("---lxg---styleMapList->"+styleMapList);

                    if (CollectionUtils.isNotEmpty(styleMapList)) {
                        for (Object o1 : styleMapList) {
                            Map<String, Object> styleMap = (Map<String, Object>) o1;
                            //获取颜色风格上的内部颜色编码
                            String JF_InternalColorCode = UIUtil.getValue(styleMap, "attribute[JF_InternalColorCode]");
                            String Title = UIUtil.getValue(styleMap, "attribute[Title]");
                            internalFullPartNumber = internalPartNumber + internalRevision + JF_InternalColorCode;
                            if (!processedFullNumbers.contains(internalFullPartNumber)){

                                list.add(internalFullPartNumber);
                                processedFullNumbers.add(internalFullPartNumber);
                            }
                        }
                        retMap.put(partId, list);
                    }
                } else {
                    //颜色矩阵为空
                    internalFullPartNumber = internalPartNumber + internalRevision;
                    if (!processedFullNumbers.contains(internalFullPartNumber)){

                        list.add(internalFullPartNumber);
                        processedFullNumbers.add(internalFullPartNumber);
                        retMap.put(partId, list);
                    }
                }
            }
        }
        JF_LOGGER.info("---getMbomInformationPartsJson--end");
        return retMap;
    }

    /**
     * @Author Liuxg
     * @Description 改造原本获取方法
     * @Date 2025/12/2 5:22
     * @Param [context, eBomTypeSelectList, ebom, latestMatrixId, colorStyle, projectId, partMapList]
     * @return java.util.Map<java.lang.String,java.util.Set<java.lang.String>>
     **/
    public Map<String, Map> getMbomInformationPartsJson2(Context context,  StringList eBomTypeSelectList,
                                                         JF_VPMReferenceEBOM_mxJPO ebom, String latestMatrixId, Map<String, MapList> colorStyle, String projectId,MapList partMapList) throws Exception {
        Map<String, Map> retMap = new HashMap<>();
        Map<String, String> setMap = new HashMap<>();
        // 用于跟踪已处理的 internalFullPartNumber
        Set<String> processedFullNumbers = new HashSet<>();


        for (Object o : partMapList) {
            Map<String, Object> map = (Map<String, Object>) o;
            String partId = UIUtil.getValue(map, "id");

            String JF_PartNumber = UIUtil.getValue(map, "attribute[JF_PartNumber]");

            String revision = UIUtil.getValue(map, "revision");

            if (JF_PartNumber.startsWith("GX")) {
                continue;
            }
            //查询MBOM节点对应的ebom零件
            Map<String, Object> eBomMap = getEbomInfo(context, JF_PartNumber, revision, eBomTypeSelectList);
            //mod by 新增需求改造 ：当MBOM结构中存在“双经销”或“外协”件时，同步MDM及导出整椅MBOM结构时需要将对应零件的父级件进行输出，直至供货层级零件。
            //往上找找到供货层级结束，递归
//            JF_LOGGER.info("---lxg---eBomMap->"+eBomMap);

            //setMap去重
            if (MapUtils.isNotEmpty(eBomMap) && !setMap.containsKey(partId)) {
                setMap.put(partId, "");
                String ebomId = UIUtil.getValue(eBomMap, "id");
                Set<String> list = new HashSet<>();
                Map fullmap=new HashMap();
                String internalRevision = revision.split("\\.")[0];
                String internalPartNumber = JF_PartNumber.substring(1);
                String internalFullPartNumber;
                if (UIUtil.isNotNullAndNotEmpty(latestMatrixId)) {
                    //颜色矩阵不为空
                    DomainObject ebomObj = DomainObject.newInstance(context, ebomId);
                    MapList projectMapList = ebomObj.getRelatedObjects(context, "JFProject2ColorGroup", "Project Space",
                            StringList.create("id"),
                            StringList.create("id[connection]", "attribute[JF_ColorGroupName]", "attribute[JF_ColorMatrixName]"),
                            true, false, (short) 1, "", "", (short) 0);

                    String JF_ColorGroupName = "";
                    if (CollectionUtils.isEmpty(projectMapList)) {
                        //判断当前零件是否是供货件,必须是所属项目的供货件
                        boolean flag = ebom.getPartIsSupplyPart(context, new String[]{ebomId, projectId});
                        JF_ColorGroupName = flag ? "UA" : "NA";
                    } else {
                        for (Object o1 : projectMapList) {
                            Map<String, Object> projectMap = (Map<String, Object>) o1;
                            String id = UIUtil.getValue(projectMap, "id");
                            if (Objects.equals(id, projectId)) {
                                //ebom零件通过JFProject2ColorGroup关联的项目，和MBOM关联的项目是同一个的话，获取颜色分组
                                JF_ColorGroupName = UIUtil.getValue(projectMap, "attribute[JF_ColorGroupName]");
                            }
                        }
                    }
                    //colorStyle是通过颜色矩阵获取的颜色风格的集合，key颜色分组，value颜色风格
                    //根据ebom零件的颜色分组name获取颜色矩阵中颜色分组对应的颜色风格
                    MapList styleMapList = colorStyle.get(JF_ColorGroupName);
//
//                    JF_LOGGER.info("---lxg---JF_ColorGroupName->"+JF_ColorGroupName);
//                    JF_LOGGER.info("---lxg---colorStyle->"+colorStyle);
//                    JF_LOGGER.info("---lxg---styleMapList->"+styleMapList);

                    if (CollectionUtils.isNotEmpty(styleMapList)) {
                        for (Object o1 : styleMapList) {
                            Map<String, Object> styleMap = (Map<String, Object>) o1;
                            //获取颜色风格上的内部颜色编码
                            String JF_InternalColorCode = UIUtil.getValue(styleMap, "attribute[JF_InternalColorCode]");
                            String Title = UIUtil.getValue(styleMap, "attribute[Title]");
                            String styleName = UIUtil.getValue(styleMap, "name");
                            internalFullPartNumber = internalPartNumber +  JF_InternalColorCode+internalRevision+"0";
//                            if (!processedFullNumbers.contains(internalFullPartNumber)){
//
//                                list.add(internalFullPartNumber);
//                                processedFullNumbers.add(internalFullPartNumber);
//                            }
                            fullmap.put(styleName,internalFullPartNumber);


                        }
                        retMap.put(partId, fullmap);
                    }
                } else {
                    //颜色矩阵为空
                    internalFullPartNumber = internalPartNumber + internalRevision;
//                    if (!processedFullNumbers.contains(internalFullPartNumber)){
//
//                        list.add(internalFullPartNumber);
//                        processedFullNumbers.add(internalFullPartNumber);
//                        retMap.put(partId, list);
//                    }
                    fullmap.put("",internalFullPartNumber);
                    retMap.put(partId, fullmap);
                }
            }
        }
        JF_LOGGER.info("---getMbomInformationPartsJson2--end");
        return retMap;
    }


    //获取当前时间
    public String getTime(){
        String time = "";
        // 获取当前的时间戳
        Instant instant = Instant.now();

        // 将时间戳格式化为可读的日期时间
        time = DateTimeFormatter
                .ofPattern("yyyy-MM-dd HH:mm:ss")
                .withZone(ZoneId.systemDefault())
                .format(instant);
        return time;
    }

    public String getTimeCuo(){
        // 获取当前日期和时间
        LocalDateTime now = LocalDateTime.now();

        // 定义格式化器
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyMMddHHmm");

        // 格式化当前日期
        String formattedDate = now.format(formatter);
        return formattedDate;
    }
    public String getTimeCuo2(){
        // 获取当前日期和时间
        LocalDateTime now = LocalDateTime.now();

        // 定义格式化器
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

        // 格式化当前日期
        String formattedDate = now.format(formatter);
        return formattedDate;
    }


    /**
     * 获取page里面的信息
     * @param context
     * @param strTableName
     * @return
     * @throws Exception
     */
    public static Map<String,String> getMBOMConfig(Context context) throws Exception{
        // 创建一个 Map 来存储 id 和 expanded
        Map<String, String> attributesMap = new HashMap<>();
        Page pageAttributePopulation = new Page("MBOMConfigProperties.xml");
        pageAttributePopulation.open(context);
        String strProperties = pageAttributePopulation.getContents(context);
        pageAttributePopulation.close(context);
        // 使用 ByteArrayInputStream 将字符串转为输入流
        try (InputStream inputStream = new ByteArrayInputStream(strProperties.getBytes("UTF-8"))) {
            // 创建 DocumentBuilder
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(inputStream);
            // 获取所有 config 元素
            NodeList CategoryNodes = document.getElementsByTagName("Category");
            for (int i = 0; i < CategoryNodes.getLength(); i++) {
                Element categoryElement = (Element) CategoryNodes.item(i);
                String id = categoryElement.getAttribute("id");
                String expanded = categoryElement.getAttribute("expanded");
                // 将 id 和 expanded 存入 Map
                attributesMap.put(id, expanded);
            }
            JF_LOGGER.info("--attributesMap:{}",attributesMap);
        } catch (Exception e) {
            JF_LOGGER.error("----getMBOMConfig----error",e);
        }
        return attributesMap;
    }


    /**
     * @Author Liuxg
     * @Description 生成同步MBOM记录excel
     * @Date 2025/9/24 1:09
     * @Param [context, args]
     * @return java.util.Map
     **/
    public void exporSyncMBOMExcel(Context context, String projectId,DomainObject Mbomrootobj) throws Exception {

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
            fileName = "MBOM制造物料同步记录清单_"+projectName+"_";
            Sheet zeroSheet = workbook.getSheet("Zero");
            if (zeroSheet == null) {
                throw new IllegalStateException("Zero sheet not found in MBOM export template.");
            }
            // MDM同步记录只保留Zero报表，按Sheet对象删除其他页，避免模板顺序调整导致误删。
            for (int sheetIndex = workbook.getNumberOfSheets() - 1; sheetIndex >= 0; sheetIndex--) {
                if (workbook.getSheetAt(sheetIndex) != zeroSheet) {
                    workbook.removeSheetAt(sheetIndex);
                }
            }

            getExportMBOMInfo(context,workbook,zeroSheet,projectId,"GC");

            //获取当前时间
            LocalDateTime now = LocalDateTime.now();
            // 将 LocalDateTime 对象转换成指定格式的字符串
            String strFormattedDate = now.format(formatter);

            String workspace = context.createWorkspace();
            String excelfile= JF_PublicMethodClass_mxJPO.buildStringInStrings(fileName, strFormattedDate, ".xlsx");
            String fullPath = workspace + File.separator +excelfile;
            try (FileOutputStream fos = new FileOutputStream(fullPath)) {
                workbook.write(fos);
                JF_LOGGER.info("文件已保存至: " + fullPath);
                Mbomrootobj.checkinFile(context, false, true, "", "generic", excelfile, workspace);
                JF_LOGGER.info("文件已保存完毕 ");
            }catch (Exception e2){
                JF_LOGGER.error("JF_ExportMBOM------exportMBOMExcel error2", e2);
            }finally {
                workbook.close();
            }


        }catch (Exception e){

            JF_LOGGER.error("JF_ExportMBOM------exportMBOMExcel error", e);

        }finally {


            ContextUtil.popContext(context);
        }

    }


//    public void getExportSyncMBOMInfo(Context context, Workbook workbook, String projectId, String mbomType,Sheet sheet) throws Exception{
//        StringList typeSelectList = JF_Util_mxJPO.basicBolistSel();
//        typeSelectList.add(SELECT_PHYSICAL_ID);
//        typeSelectList.add(SELECT_LEVEL);
//        typeSelectList.add("attribute[JF_PartNameEN]");
//        typeSelectList.add("attribute[JF_PartNameCN]");
//        typeSelectList.add("attribute[JF_PartType]");
//        typeSelectList.add("attribute[JF_ProcurementType]");
//        typeSelectList.add("attribute[JF_DirectBuy]");
//        typeSelectList.add("attribute[JF_Unit]");
//        typeSelectList.add("attribute[JF_PartDes]");
//        typeSelectList.add("attribute[JF_PartENDes]");
//        typeSelectList.add("attribute[JF_Width]");
//        typeSelectList.add("attribute[JF_Utilizationrate]");
//        typeSelectList.add("attribute[JF_PartNumber]");
//        StringList relSelectList = JF_Util_mxJPO.basicRellistSel();
//        relSelectList.add(SELECT_FROM_ID);
//        relSelectList.add("attribute[JF_Dosage]");
//        StringList EBomTypeSelectList = JF_Util_mxJPO.basicBolistSel();
//        EBomTypeSelectList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_V_PART_NUMBER);
//        EBomTypeSelectList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JF_PartNameCN);
//        EBomTypeSelectList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JF_PartNameEN);
//        EBomTypeSelectList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JFDIRECT_BUY);
//        EBomTypeSelectList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JF_ProcurementType);
//        EBomTypeSelectList.add("attribute[CustomerPartNumber]");
//        EBomTypeSelectList.add("attribute[CustomerPartRevision]");
//        EBomTypeSelectList.add("attribute[PLMEntity.V_description]");
//        EBomTypeSelectList.add(SELECT_ATTR_JFPartType);
//        EBomTypeSelectList.add(SELECT_PHYSICAL_ID);
//
//        JF_ExportEBOM_mxJPO jfExportEBOMMxJPO = new JF_ExportEBOM_mxJPO();
//        JF_VPMReferenceEBOM_mxJPO ebom = new JF_VPMReferenceEBOM_mxJPO();
//        DomainObject projectObj = DomainObject.newInstance(context,projectId);
//        String topMbomId = projectObj.getInfo(context, "from[JF_relProject2MBOM].to.id");
//
//        DomainObject topMbomObj = DomainObject.newInstance(context, topMbomId);
//        StringList zeroLevelPart = topMbomObj.getInfoList(context, "from[JF_relManufacturedItem].to.id");
//        int lastColumnIndex = sheet.getRow(3).getLastCellNum()-1;
//        lastColumnIndex = lastColumnIndex + zeroLevelPart.size();
//
//        //获取项目关联的最新发布版颜色矩阵
//        String latestMatrixId = getLatestMatrixId(context,projectObj,relSelectList);
//        JF_LOGGER.info("latestMatrixId------:{}",latestMatrixId);
//        Map colorStyle = null;
//        //获取颜色分组对应的颜色风格
//        if (UIUtil.isNotNullAndNotEmpty(latestMatrixId)){
//            colorStyle = getColorStyleMap(context,latestMatrixId,relSelectList);
//        }
//        JF_LOGGER.info("colorStyle------:{}",colorStyle);
//
//        //获取配置文件 根据MBOM制造件“零件号”前两位，配置MBOM导出时半成品展开层级 如果配置文件里面对应零件类型的expand为false,只导出一级件，不展开子级。如果是true就和现在的处理一样
//        Map<String, String> mbomConfigMap = getMBOMConfig(context);
//        //获取表格映射
//        MapList MBOMMappingList = JF_PublicMethodClass_mxJPO.getJFCostChangeHistoryTemplateMapping(context, "MBOM");
//        //表头添加列
//        createHeaderColumn(context,workbook,sheet,zeroLevelPart,MBOMMappingList,latestMatrixId,EBomTypeSelectList,ebom,projectId);
//        JF_LOGGER.info("MBOMMappingList----:{}",MBOMMappingList);
//        //实际写入数据
//        MapList actualDataList = new MapList();
//        //去重一级件
//        Map oneLevelMapInfo = new HashMap<String,Map>();
//        Map<String, String> oneLevelMapInfoMiddle = new HashMap<>();
//        for (String gcId : zeroLevelPart) {
//            DomainObject gcObj = DomainObject.newInstance(context, gcId);
//            Map GCInfo = gcObj.getInfo(context, typeSelectList);
//            //手动设置0级
//            GCInfo.put(SELECT_LEVEL,"0");
//            //设置本身用量为1
//            GCInfo.put(gcId,"1");
//            actualDataList.add(GCInfo);
//            //一级件
//            MapList partMapList = gcObj.getRelatedObjects(context,
//                    JF_PLMConstants_mxJPO.REL_JF_relManufacturedItem, // relationship pattern
//                    JF_PLMConstants_mxJPO.TYPE_JF_ManufacturedItem,                                    // object pattern
//                    typeSelectList,                            // object selects
//                    relSelectList, // relationship selects
//                    false,                                        // to direction
//                    true,                                        // from direction
//                    (short) 1,                                    // recursion level
//                    "",                // object where clause
//                    "",
//                    (short) 0);
//            for (Object o : partMapList) {
//                Map oneLevelMap = (Map) o;
//                String oneLevelId = UIUtil.getValue(oneLevelMap, "id");
//                String JF_Dosage = UIUtil.getValue(oneLevelMap, "attribute[JF_Dosage]");
//                String strRevision = (String) oneLevelMap.get(SELECT_REVISION);
//                String JF_PartNumber = (String) oneLevelMap.get("attribute[JF_PartNumber]");
//                if (JF_PartNumber.startsWith("GX")) {
//                    DomainObject oneLevel = DomainObject.newInstance(context, oneLevelId);
//                    MapList gxpartMapList = oneLevel.getRelatedObjects(context,
//                            JF_PLMConstants_mxJPO.REL_JF_relManufacturedItem, // relationship pattern
//                            JF_PLMConstants_mxJPO.TYPE_JF_ManufacturedItem,    // object pattern
//                            typeSelectList,                                     // object selects
//                            relSelectList,                                     // relationship selects
//                            false,                                             // to direction
//                            true,                                              // from direction
//                            (short) 1,                                         // recursion level
//                            "",                                                // object where clause
//                            "",
//                            (short) 0);
//
//                    // 遍历 gxpartMapList
//                    for (Object gxPartObj : gxpartMapList) {
//                        Map gxPartMap = (Map) gxPartObj;
//                        String gxPartId = UIUtil.getValue(gxPartMap, "id");
//                        String gxJF_Dosage = UIUtil.getValue(gxPartMap, "attribute[JF_Dosage]");
//                        String gxStrRevision = (String) gxPartMap.get(SELECT_REVISION);
//                        String gxJF_PartNumber = (String) gxPartMap.get("attribute[JF_PartNumber]");
//
//                        getExtractOneLevelInfo(oneLevelMapInfo, oneLevelMapInfoMiddle, gcId, gxPartMap, gxPartId, gxJF_Dosage, gxStrRevision, gxJF_PartNumber);
//                    }
//                    continue;
//                }
//
//                getExtractOneLevelInfo(oneLevelMapInfo, oneLevelMapInfoMiddle, gcId, oneLevelMap, oneLevelId, JF_Dosage, strRevision, JF_PartNumber);
//            }
//        }
//
//        //遍历一级件及其子集
//        for (Object o : oneLevelMapInfo.entrySet()) {
//            Map.Entry entry = (Map.Entry)o;
//            String strOneLevelId = (String) entry.getKey();
//            Map oneLevelInfo = (Map) entry.getValue();
//            //添加一级件
//            actualDataList.add(oneLevelInfo);
//            String onePartNumber = UIUtil.getValue(oneLevelInfo,"attribute[JF_PartNumber]");
//            String firstTwoCharacters = onePartNumber.substring(0, 2);
//            if ("false".equals(mbomConfigMap.get(firstTwoCharacters))){
//                continue;
//            }
//            DomainObject oneLevel = DomainObject.newInstance(context,strOneLevelId);
//            MapList partMapList = oneLevel.getRelatedObjects(context,
//                    JF_PLMConstants_mxJPO.REL_JF_relManufacturedItem, // relationship pattern
//                    JF_PLMConstants_mxJPO.TYPE_JF_ManufacturedItem,                                    // object pattern
//                    typeSelectList,                            // object selects
//                    relSelectList, // relationship selects
//                    false,                                        // to direction
//                    true,                                        // from direction
//                    (short) 0,                                    // recursion level
//                    "",                // object where clause
//                    "",
//                    (short) 0);
//
//            //子件对应的gc id集合 因为用量需要和GCid对应起来
//            StringList mappingGCId = getMappingGCId(oneLevelInfo, zeroLevelPart);
//            int level = Integer.MAX_VALUE;
//            Map<String, Object> sameBomeMap = new HashMap<>();
//            //合并一级件下面的用量
//            for (Object o1 : partMapList) {
//                Map partInfoMap = (Map) o1;
//                String childId = UIUtil.getValue(partInfoMap, "id");
//                String fromId = UIUtil.getValue(partInfoMap, SELECT_FROM_ID);
//                String JF_Dosage = UIUtil.getValue(partInfoMap, "attribute[JF_Dosage]");
//                String strRevision = (String) partInfoMap.get(SELECT_REVISION);
//                String JF_PartNumber = (String) partInfoMap.get("attribute[JF_PartNumber]");
//                int childLevel = Integer.parseInt(UIUtil.getValue(partInfoMap, SELECT_LEVEL));
//                if (childLevel > level) {
//                    //childLevel大于level的,说明该层的父级已经重复，不用合并用量
//                    continue;
//                }
//                level = Integer.MAX_VALUE;
//                StringBuilder sb = new StringBuilder();
//                sb.append(fromId);
//                sb.append(JF_PartNumber);
//                sb.append(strRevision);
//                //因为MBOM结构相同的JF_PartNumber和revision对应的id不一样 所以不能用id判断去重合并
//                String strUniqueKey = sb.toString();
//                if (!sameBomeMap.containsKey(strUniqueKey)){
//                    sameBomeMap.put(strUniqueKey, partInfoMap);
//                    for (String strGCId : mappingGCId) {
//                        partInfoMap.put(strGCId,JF_Dosage);
//                    }
//                    //标识一级件下面子件
//                    partInfoMap.put("isSunPart",true);
//                    JF_LOGGER.info("@@@@@@@@@@@@@@@@@@添加到集合的子集:{}",partInfoMap);
//                    actualDataList.add(partInfoMap);
//                }else {
//                    level = childLevel;
//                    Map bommap = (Map) sameBomeMap.get(strUniqueKey);
//                    String dosageStr = (String) bommap.get("attribute[JF_Dosage]");
//                    if (UIUtil.isNullOrEmpty(dosageStr)){
//                        dosageStr = "0" ;
//                    }
//                    if (UIUtil.isNullOrEmpty(JF_Dosage)){
//                        JF_Dosage = "0" ;
//                    }
//                    JF_LOGGER.info("dosageStr:{}strChildDosage：{}",dosageStr,JF_Dosage);
//                    double dosage = Double.parseDouble(dosageStr) + Double.parseDouble(JF_Dosage);
//                    String strSumDosage = String.valueOf(dosage);
//                    for (String strGCId : mappingGCId) {
//                        bommap.put(strGCId,strSumDosage);
//                    }
//                    bommap.put("attribute[JF_Dosage]", strSumDosage);
//                }
//            }
//        }
//        //缩略图保存地址
//        String strPrePath = JF_PublicMethodClass_mxJPO.getBasicUrl(context, new String[]{"DownloadVPM.Img.Path"});
//
//
//        //初始下标
//        int writeRowIndex = 4 ;
//        CellStyle basicCellStyle = getBasicCellStyle(workbook);
//        //开始写入数据
//        for (int i = 0; i < actualDataList.size(); i++) {
//            Map partInfoMap = (Map) actualDataList.get(i);
//            partInfoMap.put("ITEM",String.valueOf(i+1));
//            String strPartId = (String) partInfoMap.get(SELECT_ID);
//            String strLevel = (String) partInfoMap.get(SELECT_LEVEL);
//            String strRevision = (String) partInfoMap.get(SELECT_REVISION);
//            String JF_PartNumber = (String) partInfoMap.get("attribute[JF_PartNumber]");
//            DomainObject partObj = DomainObject.newInstance(context, strPartId);
//            //穿透“零件号”以“GX”开头的层级
//            if (JF_PartNumber.startsWith("GX")){
//                MapList partMapList = partObj.getRelatedObjects(context,
//                        JF_PLMConstants_mxJPO.REL_JF_relManufacturedItem, // relationship pattern
//                        JF_PLMConstants_mxJPO.TYPE_JF_ManufacturedItem,                                    // object pattern
//                        typeSelectList,                            // object selects
//                        relSelectList, // relationship selects
//                        false,                                        // to direction
//                        true,                                        // from direction
//                        (short) 0,                                    // recursion level
//                        "",                // object where clause
//                        "",
//                        (short) 0);
//                for (Object o : partMapList) {
//                    Map map = (Map) o;
//                    String id = UIUtil.getValue(map, "id");
//                    String fromId = UIUtil.getValue(map, SELECT_FROM_ID);
//                    for (int i1 = i+1; i1 < actualDataList.size(); i1++) {
//                        Map map1 = (Map) actualDataList.get(i1);
//                        String id1 = UIUtil.getValue(map1, "id");
//                        String fromId1 = UIUtil.getValue(map1, SELECT_FROM_ID);
//                        if (Objects.equals(id,id1) && Objects.equals(fromId,fromId1)){
//                            String level = UIUtil.getValue(map1, SELECT_LEVEL);
//                            int iLevel = Integer.parseInt(level) - 1;
//                            String sLevel = String.valueOf(iLevel);
//                            map1.put("level",sLevel);
//                            break;
//                        }
//                    }
//                }
//                continue;
//            }
//            //子件层级+1
//            if (partInfoMap.containsKey("isSunPart")){
//                int iActualLevel = Integer.parseInt(strLevel) + 1;
//                strLevel = String.valueOf(iActualLevel);
//            }
//            partInfoMap.put(JF_PublicMethodClass_mxJPO.buildStringInStrings(SELECT_LEVEL,"_",strLevel),strLevel);
//
//            //找到对应的EBOM的属性
//            Map ebomInfo = getEbomInfo(context, JF_PartNumber, strRevision, EBomTypeSelectList);
//            if (MapUtils.isNotEmpty(ebomInfo)){
//                String ebomPhysicalId = UIUtil.getValue(ebomInfo, SELECT_PHYSICAL_ID);
//                String ebomId = UIUtil.getValue(ebomInfo, SELECT_ID);
//                String ebomCustomerPartNumber = UIUtil.getValue(ebomInfo, "attribute[CustomerPartNumber]");
//                String ebomCustomerPartRevision = UIUtil.getValue(ebomInfo, "attribute[JF_VPMReference.JF_CustomerPartRevision]");
//                //获取缩略图
//                String strImgId = jfExportEBOMMxJPO.getDownLoadPictureByPartId(context, ebomPhysicalId, strPrePath);
//                partInfoMap.put("picture",strImgId);
//                partInfoMap.put("attribute[CustomerPartNumber]",ebomCustomerPartNumber);
//                partInfoMap.put("attribute[JF_VPMReference.JF_CustomerPartRevision]",ebomCustomerPartRevision);
//                if (UIUtil.isNotNullAndNotEmpty(latestMatrixId)){
//                    //有颜色矩阵才去写关于颜色的信息
//                    DomainObject ebomObj = DomainObject.newInstance(context, ebomId);
//                    MapList projectMapList = ebomObj.getRelatedObjects(context, "JFProject2ColorGroup", "Project Space", StringList.create("id"),
//                            StringList.create("id[connection]","attribute[JF_ColorGroupName]","attribute[JF_ColorMatrixName]"), true, false, (short) 1, "", "", (short) 0);
//                    if (CollectionUtils.isEmpty(projectMapList)){
//                        //判断是否是供货价
//                        boolean flag = ebom.getPartIsSupplyPart(context, new String[]{ebomId,projectId});
//                        String attributeValue = flag ? "UA" : "NA";
//                        writeRowIndex = getSyncWriteRowIndex(context, workbook, sheet, lastColumnIndex, MBOMMappingList, colorStyle, writeRowIndex, basicCellStyle, partInfoMap, strRevision, JF_PartNumber, attributeValue,mbomType);
//                    }else {
//                        for (Object o : projectMapList) {
//                            Map projectMap = (Map) o;
//                            String id = UIUtil.getValue(projectMap, "id");
//                            if (Objects.equals(id,projectId)){
//                                String JF_ColorGroupName = UIUtil.getValue(projectMap, "attribute[JF_ColorGroupName]");
//                                writeRowIndex = getSyncWriteRowIndex(context, workbook, sheet, lastColumnIndex, MBOMMappingList, colorStyle, writeRowIndex, basicCellStyle, partInfoMap, strRevision, JF_PartNumber, JF_ColorGroupName,mbomType);
//                            }
//                        }
//                    }
//                }else {
//                    partInfoMap.put("JF_InternalColorCode","NUL");
//                    partInfoMap.put("JF_CustormColorCode","NUL");
//                    partInfoMap.put("InternalFullPartNumber","NUL");
////                    partInfoMap.put("CustomerFullPartNumber","NUL");
////                    JF_LOGGER.info("partInfoMap:{}",partInfoMap);
//                    partInfoMap.put("usage","");
//                    partInfoMap.put("styleTitle","");
//                    boolean b=writeSyncDataInExcel(context,workbook,sheet,writeRowIndex,MBOMMappingList,partInfoMap,basicCellStyle,lastColumnIndex,mbomType);
//                    if(b){
//                        writeRowIndex ++;
//                    }
//
//                }
//            }
//
//        }
//
//
//    }
//
//    public int getSyncWriteRowIndex(Context context, Workbook workbook, Sheet sheet, int lastColumnIndex, MapList MBOMMappingList, Map colorStyle, int writeRowIndex, CellStyle basicCellStyle, Map partInfoMap, String strRevision, String JF_PartNumber, String JF_ColorGroupName,String Mbomtype) throws Exception {
//        MapList styleMapList = (MapList) colorStyle.get(JF_ColorGroupName);
//        // 使用 Map 来存储已经处理过的 JF_InternalColorCode 和对应的 styleTitle
//        Map<String,String> processedColorCodes = new HashMap<>();
//        if (CollectionUtils.isNotEmpty(styleMapList)){
//            for (Object o1 : styleMapList) {
//                Map styleMap = (Map) o1;
//                String JF_InternalColorCode = UIUtil.getValue(styleMap, "attribute[JF_InternalColorCode]");
//                String JF_CustormColorCode = UIUtil.getValue(styleMap, "attribute[JF_CustormColorCode]");
//                String Title = UIUtil.getValue(styleMap, "attribute[Title]");
////                String CustomerFullPartNumber = JF_PartNumber + strRevision.split("\\.")[0]+JF_CustormColorCode;
//                if (processedColorCodes.containsKey(JF_InternalColorCode)){
//                    String existCode = processedColorCodes.get(JF_InternalColorCode);
//                    existCode = existCode+","+Title;
//                    processedColorCodes.put(JF_InternalColorCode,existCode);
//                }else {
//                    // 如果没有处理过，添加到 processedColorCodes
//                    processedColorCodes.put(JF_InternalColorCode, Title);
//                    partInfoMap.put("JF_InternalColorCode",JF_InternalColorCode);
//                    partInfoMap.put("JF_CustormColorCode",JF_CustormColorCode);
////                partInfoMap.put("CustomerFullPartNumber",CustomerFullPartNumber);
////                    JF_LOGGER.info("partInfoMap:{}",partInfoMap);
//                    partInfoMap.put("usage","");
//                }
//            }
//            // 在循环结束后，写入数据到 Excel
//            for (Map.Entry<String, String> entry : processedColorCodes.entrySet()) {
//                String colorCode = entry.getKey();
//                String accumulatedTitle = entry.getValue();
//                String InternalFullPartNumber = JF_PartNumber + strRevision.split("\\.")[0]+colorCode;
//                partInfoMap.put("InternalFullPartNumber",InternalFullPartNumber.substring(1));//内部完整零件号规则更新，取消第一位的G，如GC0002945AAS09变为C0002945AAS09
//                partInfoMap.put("styleTitle", accumulatedTitle);
//                boolean b=writeSyncDataInExcel(context, workbook, sheet, writeRowIndex, MBOMMappingList, partInfoMap, basicCellStyle, lastColumnIndex,Mbomtype);
//                if(b){
//                    writeRowIndex++;
//                }
//
//            }
//        }else {
////            JF_LOGGER.info("partInfoMap:{}",partInfoMap);
//            partInfoMap.put("usage","");
//            partInfoMap.put("styleTitle","");
//            boolean b=writeSyncDataInExcel(context,workbook,sheet,writeRowIndex,MBOMMappingList,partInfoMap,basicCellStyle,lastColumnIndex,Mbomtype);
//            if(b){
//                writeRowIndex ++;
//            }
//
//        }
//
//        return writeRowIndex;
//    }
//
//
//    private boolean writeSyncDataInExcel(Context context, Workbook workbook, Sheet sheet, int writeRowIndex, MapList mbomMappingList, Map dataMap, CellStyle basicCellStyle, int lastColumnIndex,String Mbomtype) throws Exception {
//        boolean b=true;
//        String JF_PartType = (String) dataMap.get("attribute[JF_PartType]");
//        if (Mbomtype.equals(JF_PartType)) {
//            Row row = sheet.createRow(writeRowIndex);
//            for (int i = 0; i < mbomMappingList.size(); i++) {
//                Map mappingMap = (Map) mbomMappingList.get(i);
//                String strKey = (String) mappingMap.get("id");
//                String strColIndex = (String) mappingMap.get("value");
//                if (dataMap.containsKey(strKey)) {
//                    Cell cell = null;
//                    if ("picture".equals(strKey)) {
//                        String strImgPath = (String) dataMap.get(strKey);
//                        if (UIUtil.isNotNullAndNotEmpty(strImgPath)) {
//                            JF_ExportEBOM_mxJPO.insertPictureInCell(workbook, sheet, strImgPath, writeRowIndex, Integer.parseInt(strColIndex), 100, 50);
//                        }
//                        //设置边框
//                        cell = row.createCell(Integer.parseInt(strColIndex));
//                    } else if ("usage".equals(strKey)) {
//                        String strCellValue = "";
//                        cell = row.createCell(Integer.parseInt(strColIndex));
//                        String strPCId = (String) mappingMap.get("objectId");
//                        String styleTitle = (String) mappingMap.get("styleTitle");
//                        if (dataMap.containsKey(strPCId) && dataMap.containsKey("styleTitle")) {
//                            String dataTitle = (String) dataMap.get("styleTitle");
//                            if (dataTitle.contains(styleTitle)) {
//                                String usage = (String) dataMap.get(strPCId);
//                                strCellValue = usage;
//                            }
//                        }
//                        strCellValue = "0".equals(strCellValue) ? "" : strCellValue;
//                        if (JF_PublicMethodClass_mxJPO.isNumeric(strCellValue)) {
//                            double dValue = Double.parseDouble(strCellValue);
//                            cell.setCellValue(dValue);
//                            cell.setCellStyle(basicCellStyle);
//                        } else {
//                            cell.setCellValue(strCellValue);
//                            cell.setCellStyle(basicCellStyle);
//                        }
//                    } else {
//                        String strValue = (String) dataMap.get(strKey);
//                        cell = row.createCell(Integer.parseInt(strColIndex));
//                        if (JF_PublicMethodClass_mxJPO.isNumeric(strValue)) {
//                            double dValue = Double.parseDouble(strValue);
//                            cell.setCellValue(dValue);
//                            cell.setCellStyle(basicCellStyle);
//                        } else {
//                            cell.setCellValue(strValue);
//                            cell.setCellStyle(basicCellStyle);
//                        }
//                    }
//                }
//            }
//
//            //单元格样式
//            for (int i = 0; i < lastColumnIndex; i++) {
//                Cell cell = row.getCell(i);
//                if (cell == null) {
//                    cell = row.createCell(i);
//                }
//                cell.setCellStyle(basicCellStyle);
//            }
//
//        }else {
//            b=false;
//        }
//        return b;
//    }
//
//


}
