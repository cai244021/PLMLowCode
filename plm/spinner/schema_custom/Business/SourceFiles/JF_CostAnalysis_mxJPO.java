import com.dscn.plm.util.NioJDUtils;
import com.google.gson.Gson;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.DomainRelationship;
import com.matrixone.apps.domain.util.*;
import com.matrixone.apps.framework.ui.UIUtil;
import com.matrixone.apps.program.ProgramCentralUtil;
import com.matrixone.jdom.Document;
import com.matrixone.jdom.Element;
import matrix.db.*;
import matrix.util.StringList;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.Set;
import java.util.stream.Collectors;

import static com.matrixone.apps.domain.DomainConstants.*;

/**
 * @ClassName JF_CostAnalysis_mxJPO
 * @Author: LIUJR
 * @CreateDate: 2025/3/3 15:34
 * @UpdateRemark:
 * @Version: 1.0
 * @Description: 成分核算Program
 */
public class JF_CostAnalysis_mxJPO implements  JF_PLMConstants_mxJPO{
    private  static final Logger JF_LOGGER = LoggerFactory.getLogger(JF_CostAnalysis_mxJPO.class);
    private  static final String relIsExist = "from[" + rel_JFVPMReference2CostAnalysis + "]";
    //卷积成本
    private  static final String strJFRollUpCost = "from[" + rel_JFVPMReference2CostAnalysis + "].to." + SELECT_ATTR_JFRollUpCost;
    //实际成本
    private  static final String strJFActualcCost = "from[" + rel_JFVPMReference2CostAnalysis + "].to." + SELECT_ATTR_JFActualcCost;

    private  static final StringList selList = JF_Util_mxJPO.basicBolistSel();
    private  static final StringList relList = JF_Util_mxJPO.basicRellistSel();
    private  static final StringList partAttrList = StringList.create("NetArea", "JFLength", "Circumference", "JF_VPMReference.JF_ProcurementType","PerforationQuantity", "JF_VPMInstance.JF_Dosage");

    private static byte[] templateFileBytes ;
    private int itemAttrValueColIndex = 7;
    private int itemAttrNameColIndex = 3;
    private int partAttrColValueIndex = 5;
    private int partNameAttrColIndex = 2;

    /**
    *  获取BOM的成本分析
    * @param context
    * @param args
    * @author LIUJR
    * @throws
    * @return com.matrixone.apps.domain.util.MapList
    * @date 2025/3/3 15:36
    * @description
    */
    public MapList getBOMAllCostAnalysisList(Context context, String[] args) throws Exception{
        try {
            StringList busSelectsList = JF_Util_mxJPO.basicBolistSel();
            Map paramsMap = (Map) JPO.unpackArgs(args);
            ContextUtil.pushContext(context);
            String strObjectId = (String) paramsMap.get(STRING_OBJECTID);
            DomainObject objectPart = DomainObject.newInstance(context, strObjectId);
            StringList colorPartIdList = objectPart.getInfoList(context, "from[JFVPMReference2CostAnalysis].to.id");
            JF_LOGGER.info("ColorMatrix:{}", colorPartIdList);
            MapList mlPartInfoList = DomainObject.getInfo(context, colorPartIdList.toStringArray(), busSelectsList);
            JF_LOGGER.info("mlPartInfoList:{}", mlPartInfoList);
            return mlPartInfoList;
        }catch (Exception e) {
            e.printStackTrace();
            ContextUtil.popContext(context);
        }
        return new MapList();
    }

    /**
    *
    *@description 新建成本分析时获取成本库
    *@param context
	*@param args
    *@return com.matrixone.apps.domain.util.MapList
    *@throws
    *@author CHENYAN
    *@date 2025/3/4 10:18
    */
    public MapList getCostAnalysisLibList(Context context, String[] args) throws Exception{
        try {
            Properties properties = JF_Util_mxJPO.readPageObject(context,"JFJDConfig");
            String strCostLibraryName = properties.getProperty("Library.CostAnalysis.Name");
            StringList busSelectsList = JF_Util_mxJPO.basicBolistSel();
            MapList cosLibMapList = DomainObject.findObjects(context,"General Library","*","name=='"+strCostLibraryName+"'",busSelectsList);
            if (Objects.nonNull(cosLibMapList) && cosLibMapList.size() > 0){
                Map cosLibInfo = (Map) cosLibMapList.get(0);
                String strObjectId = (String) cosLibInfo.get(SELECT_ID);
                DomainObject cosLibBO = DomainObject.newInstance(context, strObjectId);
                MapList maps = cosLibBO.getRelatedObjects(context, "Subclass" , // relationship pattern
                        "General Class",                                    // object pattern
                        JF_Util_mxJPO.basicBolistSel(),                            // object selects
                        JF_Util_mxJPO.basicRellistSel(), // relationship selects
                        false,                                        // to direction
                        true,                                        // from direction
                        (short) 1,                                    // recursion level
                        "",                // object where clause
                        "",
                        (short) 0);
                JF_LOGGER.info("maps:{}",maps);
                return maps;
            }
        }catch (Exception e) {
            e.printStackTrace();
            ContextUtil.popContext(context);
        }
        return new MapList();
    }

    /**
    *
    *@description 获取选中的零件显示表格数据
    *@param context
    *@param args·
    *@return com.matrixone.apps.domain.util.MapList
    *@throws
    *@author CHENYAN
    *@date 2025/3/10 15:58
    */
    public MapList getCostAnalysisEditList(Context context, String[] args) throws Exception{
        MapList res = new MapList();
        try {
            JF_LOGGER.info("----------------------------  getCostAnalysisEditList begin -----------------------------------------------");
            Map agrsMap = JPO.unpackArgs(args);
            String strSelectIds = (String) agrsMap.get("selectIds");
            strSelectIds = URLDecoder.decode(strSelectIds, StandardCharsets.UTF_8);
            Gson gson = new Gson();
            Map selectIdMap = gson.fromJson(strSelectIds, Map.class);
            JF_LOGGER.info("selectIdMap:{}",selectIdMap);
//            String strSelectRids = (String) agrsMap.get("selectRids");
            Map partAttrMap = (Map) agrsMap.get("partAttrMap");
            JF_LOGGER.info("partAttrMap:{}",partAttrMap);
            String[] selectIdArr = strSelectIds.split(",");
//            String[] selectRidArr = strSelectRids.split(",");
            StringList typeSelectList = new StringList();
            typeSelectList.add(SELECT_ATTR_V_PART_NUMBER);
            typeSelectList.add(SELECT_NAME);
            DomainObject part = DomainObject.newInstance(context);
//            if (selectIdArr.length > 0){
//                for (int i = 0; i < selectIdArr.length; i++) {
//                    String strPartId = selectIdArr[i];
//
//                    if (UIUtil.isNotNullAndNotEmpty(strPartId)){
//                        part.setId(strPartId);
//                        Map partInfo = part.getInfo(context, typeSelectList);
//                        if (partAttrMap.containsKey(strPartId)){
//                            //往每个零件添加专属的属性Map
//                            partInfo.put("attrMapInfo",partAttrMap.get(strPartId));
//                        }
//                        //防止下标越界
//                        if (selectRidArr.length > i){
//                            partInfo.put(SELECT_RELATIONSHIP_ID,selectRidArr[i]);
//                        }
//                        res.add(partInfo);
//                    }
//                }
//            }
            if (Objects.nonNull(selectIdMap) && selectIdMap.size() > 0){
                for (Object oEntry : selectIdMap.entrySet()) {
                    Map.Entry entry = (Map.Entry)oEntry;
                    String strPartId = (String) entry.getKey();
                    List relIdList = (List) entry.getValue();
                    if (UIUtil.isNotNullAndNotEmpty(strPartId)){
                        part.setId(strPartId);
                        Map partInfo = part.getInfo(context, typeSelectList);
                        if (partAttrMap.containsKey(strPartId)){
//                            //往每个零件添加专属的属性Map
                            partInfo.put("attrMapInfo",partAttrMap.get(strPartId));
                        }
                            partInfo.put(SELECT_RELATIONSHIP_ID,StringList.create(relIdList).join(","));
                        res.add(partInfo);
                    }
                }
            }
        }catch (Exception e) {
            e.printStackTrace();
            ContextUtil.popContext(context);
        }
        return res ;
    }

    /**
     * 编辑
     * @param context
     * @param args
     * @author LIUJR
     * @throws
     * @return matrix.util.StringList
     * @date 2025/3/5 9:44
     * @description
     */
    public StringList getCostAnalysisEditCol(Context context,String[] args) throws Exception{
        String strLoginUser = context.getUser();
        String strTitle = EnoviaResourceBundle.getProperty(context, "emxFrameworkStringResource", context.getLocale(), "emxFramework.TableEdit.Edit");
        String strColHtml = "<a href=\"javascript:openCostAnalysisEditObj('$1',$3,'$4')\">"+
                "<img border='0' src='../common/images/iconActionEdit.png' alt=\"$2\" title=\"$2\"></img></a>&#160;";
        Map argMaps = JPO.unpackArgs(args);
        Map paramListMap = (Map) argMaps.get(STRING_PARAMLIST);
        String strObjectId = (String) paramListMap.get(STRING_OBJECTID);
        MapList argMapList = (MapList) argMaps.get(JF_PLMConstants_mxJPO.STRING_OBJECTLIST);
        StringList res = new StringList();
        for (int i = 0; i < argMapList.size(); i++) {
            Map infoMap = (Map) argMapList.get(i);
            String strId = (String) infoMap.get(SELECT_ID);
            String strType = (String) infoMap.get(SELECT_TYPE);
            String strObjColHtml = EMPTY_STRING;
            if ("JFCostAnalysis".equalsIgnoreCase(strType)) {
                DomainObject object = DomainObject.newInstance(context, strId);
                Map selectMap = object.getInfo(context, StringList.create(SELECT_CURRENT, SELECT_OWNER));
                String strCurrent = (String) selectMap.get(SELECT_CURRENT);
                String strOwner = (String) selectMap.get(SELECT_OWNER);
                if ("Active".equals(strCurrent) && strLoginUser.equals(strOwner)) {
                    strObjColHtml = strColHtml.replace("$1", strId).replace("$2", strTitle);
                    String strEditFlag = "1";
                    strObjColHtml = strObjColHtml.replace("$3", strEditFlag);
                    strObjColHtml = strObjColHtml.replace("$4",strObjectId);
                }
            }
            res.add(strObjColHtml);
        }
        return res;
    }

    /**
    *
    *@description 获取总的卷积成本价格
    *@param context
	*@param args
    *@return matrix.util.StringList
    *@throws
    *@author CHENYAN
    *@date 2025/6/26 15:04
    */
    public StringList getSumActualcCostValue(Context context ,String[] args) throws Exception{
        Map argMaps = JPO.unpackArgs(args);
        Map paramListMap = (Map) argMaps.get(STRING_PARAMLIST);
        String strParentId = (String) paramListMap.get(STRING_PARENTOID);
        String strObjectId = (String) paramListMap.get(STRING_OBJECTID);
        JF_LOGGER.info("strParentId:{}",strParentId);
        JF_LOGGER.info("strObjectId:{}",strObjectId);
        StringList res = new StringList();
        MapList argMapList = (MapList) argMaps.get(JF_PLMConstants_mxJPO.STRING_OBJECTLIST);
        for (int i = 0; i < argMapList.size(); i++) {
            Map infoMap = (Map) argMapList.get(i);
            String strValue = "";
            String strType = (String) infoMap.get(SELECT_TYPE);
            if (TYPE_JFCostAnalysis.equals(strType)){
                strValue = getSumActualcCostValueByPartId(context,strObjectId,false);
            }
            res.add(strValue);
        }
        return res ;
    }

    public String getSumActualcCostValueByForm(Context context, String[] args) throws Exception {
        Map argMaps = JPO.unpackArgs(args);
        HashMap requestMap = (HashMap) argMaps.get(STRING_REQUESTMAP);
        //零件id
        String strParentId = (String) requestMap.get("partId");
        //成本核算id
        String strObjectId = (String) requestMap.get(STRING_OBJECTID);
        String strValue = "";
        JF_LOGGER.info("strParentId:{}",strParentId);
        JF_LOGGER.info("strObjectId:{}",strObjectId);
        strValue = getSumActualcCostValueByPartId(context,strParentId,false);
        return strValue ;
    }
    /**
    *
    *@description 获取指定零件的总的卷积成本
    *@param context
	*@param strParId
    *@return java.lang.String
    *@throws
    *@author CHENYAN
    *@date 2025/6/26 15:35
    */
    public String getSumActualcCostValueByPartId(Context context ,String strParId ,boolean isPush) throws Exception {
        String strValue = "0.0";
        DomainObject part = DomainObject.newInstance(context, strParId);
        try {
            if (!isPush){
                ContextUtil.pushContext(context);
                StringList typeSelectList = JF_Util_mxJPO.basicBolistSel();
                typeSelectList.add("from[JFVPMReference2CostAnalysis]");
                typeSelectList.add("from[JFVPMReference2CostAnalysis].to.attribute[JFRollUpCost]");
                typeSelectList.add("from[JFVPMReference2CostAnalysis].to.attribute[JFActualcCost]");
                StringList relSelectList = JF_Util_mxJPO.basicRellistSel();
                relSelectList.add(SELECT_FROM_ID);
                relSelectList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JF_Dosage);
                MapList maps = part.getRelatedObjects(context, REL_Instance , // relationship pattern
                        TYPE_VPMReference,                                    // object pattern
                        typeSelectList,                            // object selects
                        relSelectList, // relationship selects
                        false,                                        // to direction
                        true,                                        // from direction
                        (short) 0,                                    // recursion level
                        "",                // object where clause
                        "",
                        (short) 0);
                JF_LOGGER.info("maps:{}",maps);
                // 存在子集的时候
                Map<String,BigDecimal>  priceInfo = new HashMap<>();
                if (maps.size() > 0){
                    //先按层级分组
                    Map groupMap = (Map) maps.stream().collect(Collectors.groupingBy(m -> {
                        Map info = (Map) m;
                        return info.get(SELECT_LEVEL);
                    }));
                    JF_LOGGER.info("groupMap:{}",groupMap);
                    LinkedHashMap sortedMap = JF_Cost_mxJPO.sortMapByKeysDesc(groupMap);
                    for (Object oEntry :sortedMap.entrySet()){
                        Map.Entry entry = (Map.Entry)oEntry;
                        List parentMapList = (List) entry.getValue();
                        //根据from id 分组
                        Map sunGroupMap = (Map) parentMapList.stream().collect(Collectors.groupingBy(m -> {
                            Map info = (Map) m;
                            String strFormId = (String) info.get(SELECT_FROM_ID);
                            //防止添加子集时刷新报错
                            return UIUtil.isNotNullAndNotEmpty(strFormId) ? strFormId : info.get("id[parent]") ;
                        }));
                    JF_LOGGER.info("sunGroupMap:{}",sunGroupMap);
                        //遍历子级
                        for (Object sunOEntry :sunGroupMap.entrySet()){
                            Map.Entry sunEntry = (Map.Entry)sunOEntry;
                            String strFromId = (String) sunEntry.getKey();
                            List sunMapList = (List) sunEntry.getValue();
                            // 使用Stream API计算总价
                            BigDecimal totalPrice = (BigDecimal) sunMapList.stream()
                                    .map(map -> {
                                        Map info = (Map)map;
                                        String strObjectId = (String) info.get(SELECT_ID);
                                        BigDecimal bigDecimal = null ;
                                        String dosage = (String) info.get(JF_PLMConstants_mxJPO.SELECT_ATTR_JF_Dosage);
                                        if (UIUtil.isNullOrEmpty(dosage) || "0.0".equalsIgnoreCase(dosage) || "0".equalsIgnoreCase(dosage)) {
                                            dosage = "1";
                                        }
                                        double dosageValue = Double.parseDouble(dosage);
                                        if (priceInfo.containsKey(strObjectId)){
                                            String strHasCreateFlag = (String) info.get("from[JFVPMReference2CostAnalysis]");
                                            //如果没有创建成本分析的话直接赋值0
                                            String strPrice = "0.0";
                                            if ("TRUE".equalsIgnoreCase(strHasCreateFlag)){
                                                bigDecimal = priceInfo.get(strObjectId);
                                                // 需要加上自身
                                                String strRollPrice = (String) info.get("from[JFVPMReference2CostAnalysis].to.attribute[JFRollUpCost]");
                                                String strActualPrice = (String) info.get("from[JFVPMReference2CostAnalysis].to.attribute[JFActualcCost]");
                                                //实际成本不为空使用实际成本 、为空使用自身卷积成本
                                                if (UIUtil.isNotNullAndNotEmpty(strActualPrice)){
                                                    strPrice = strActualPrice;
                                                }else {
                                                    if (UIUtil.isNotNullAndNotEmpty(strRollPrice)){
//                                                        strPrice = strRollPrice;
                                                        //卷积成本  * 用量  update by ljr 20260402
                                                        double dosageValue1 = Double.parseDouble(strRollPrice);
                                                        strPrice = String.valueOf(dosageValue * dosageValue1);

                                                    }
                                                }
                                            }
                                            BigDecimal selfDecimal = new BigDecimal(strPrice);
                                            if (Objects.nonNull(bigDecimal)){
                                                bigDecimal = bigDecimal.add(selfDecimal);
                                            }else {
                                                bigDecimal = selfDecimal;
                                            }
                                        }else {
                                            String strPrice = "0";
                                            String strHasCreateFlag = (String) info.get("from[JFVPMReference2CostAnalysis]");
                                            if ("TRUE".equalsIgnoreCase(strHasCreateFlag)){
                                                String strRollPrice = (String) info.get("from[JFVPMReference2CostAnalysis].to.attribute[JFRollUpCost]");
                                                String strActualPrice = (String) info.get("from[JFVPMReference2CostAnalysis].to.attribute[JFActualcCost]");
                                                //实际成本不为空使用实际成本 、为空使用自身卷积成本
                                                if (UIUtil.isNotNullAndNotEmpty(strActualPrice)){
                                                    strPrice = strActualPrice;
                                                }else {
                                                    if (UIUtil.isNotNullAndNotEmpty(strRollPrice)){
//                                                        strPrice = strRollPrice;
                                                        //卷积成本  * 用量  update by ljr 20260402
                                                        double dosageValue1 = Double.parseDouble(strRollPrice);
                                                        strPrice = String.valueOf(dosageValue * dosageValue1);
                                                    }
                                                }
                                            }
                                            bigDecimal = new BigDecimal(strPrice);
                                        }
                                        return bigDecimal;
                                    })
                                    .reduce(BigDecimal.ZERO,(a, b) -> {
                                        BigDecimal aBigDecimal = (BigDecimal)a;
                                        BigDecimal bBigDecimal = (BigDecimal)b;
                                        BigDecimal sum = aBigDecimal.add(bBigDecimal);
                                        return  sum;
                                    });
                            JF_LOGGER.info("strFromId:{} totalPrice :{}",strFromId,totalPrice);
                            priceInfo.put(strFromId,totalPrice);
                        }
                    }
                }
                JF_LOGGER.info("priceInfo:{}",priceInfo);
                //本身结果  默认为1
                Map partInfoMap = part.getInfo(context, typeSelectList);
                String strHasCreateFlag = (String) partInfoMap.get("from[JFVPMReference2CostAnalysis]");
                if ("TRUE".equalsIgnoreCase(strHasCreateFlag)){
                    String strRollPrice = (String) partInfoMap.get("from[JFVPMReference2CostAnalysis].to.attribute[JFRollUpCost]");
                    String strActualPrice = (String) partInfoMap.get("from[JFVPMReference2CostAnalysis].to.attribute[JFActualcCost]");
                    //实际成本不为空使用实际成本 、为空使用自身卷积成本
                    if (UIUtil.isNotNullAndNotEmpty(strActualPrice)){
                        strValue = strActualPrice;
                    }else {
                        if (UIUtil.isNotNullAndNotEmpty(strRollPrice)){
                            strValue = strRollPrice;
                        }
                    }
                    //求和自身结果
                    BigDecimal sumPrice = priceInfo.get(strParId);
                    if (Objects.nonNull(sumPrice)){
                        strValue = sumPrice.add(new BigDecimal(strValue)).toString();
                    }
                }
            }
        } finally {
            if (!isPush){
                ContextUtil.popContext(context);
            }
        }
        JF_LOGGER.info("strValue:{}",strValue);
        return strValue ;
    }
    /**
    *
    *@description 获取编辑成本快速的动态列
    *@param context
	*@param args
    *@return java.util.List
    *@throws
    *@author CHENYAN
    *@date 2025/3/10 16:30
    */
    public List getCostAnalyDynamicCol(Context context, String[] args) throws Exception{
        MapList fieldMapList = new MapList();
        //重排序属性集合
        MapList sortMapList = new MapList();
        try {
            JF_LOGGER.info("---------------------------------  getCostAnalyDynamicCol begin ---------------------------------------------");
            HashMap inputMap = (HashMap) JPO.unpackArgs(args);
            HashMap requestMap = (HashMap) inputMap.get("requestMap");
            String strSelectIds = (String) requestMap.get("selectIds");
            strSelectIds = URLDecoder.decode(strSelectIds, StandardCharsets.UTF_8);
            Gson gson = new Gson();
            Map selectIdMap = gson.fromJson(strSelectIds, Map.class);
            //标识选中零件有哪些属性
            Map partAttrMap = new HashMap<>();
            requestMap.put("partAttrMap",partAttrMap);
            Map attrSettingMap = getCostAnalysisAttrSettingByPage(context);
            requestMap.put("attrSettingMap",attrSettingMap);
            //标识成本快速的零件
            Map itemAttrMap = new HashMap<>();

            String[] selectIdArr = strSelectIds.split(",");
            StringList typeSelectList = new StringList();
            typeSelectList.add(SELECT_ATTR_V_PART_NUMBER);
            typeSelectList.add(SELECT_NAME);
            DomainObject part = DomainObject.newInstance(context);
            Map dynamicFieldMap = new HashMap();
            Set allItemIdSet = new HashSet();

            //获取excel属性顺序重排序属性集合
            initTemplateFile();
            Workbook workbook =  WorkbookFactory.create(new ByteArrayInputStream(templateFileBytes));
            //成本项属性
            Map<String,Row> excelItemAttrMap = getPartAttrByWorkBook(workbook,1,itemAttrNameColIndex,1);

            if (Objects.nonNull(selectIdMap) && selectIdMap.size() > 0) {
                for (Object oEntry : selectIdMap.entrySet()) {
                    Map.Entry entry = (Map.Entry) oEntry;
                    String strId = (String) entry.getKey();
                    if (UIUtil.isNotNullAndNotEmpty(strId)){
                        part.setId(strId);
                        Map attrMap = new HashMap<>();
                        MapList maps = part.getRelatedObjects(context, rel_JFCostAnalysis2CostItem+","+rel_JFVPMReference2CostAnalysis , // relationship pattern
                                TYPE_JFCostAnalysis+","+TYPE_JFCostAnalysisItem,                                    // object pattern
                                JF_Util_mxJPO.basicBolistSel(),                            // object selects
                                JF_Util_mxJPO.basicRellistSel(), // relationship selects
                                false,                                        // to direction
                                true,                                        // from direction
                                (short) 2,                                    // recursion level
                                "",                // object where clause
                                "",
                                (short) 0);
                        Set itemIdSet = (Set)maps.stream().filter(m ->{
                            Map infoMap = (Map)m;
                            return TYPE_JFCostAnalysisItem.equals(infoMap.get(SELECT_TYPE));
                        }).map(m ->{
                            Map infoMap = (Map)m;
                            itemAttrMap.put(infoMap.get(SELECT_ID),strId);
                            return infoMap.get(SELECT_ID);
                        }).collect(Collectors.toSet());
                        if (itemIdSet.size() > 0) {
                            allItemIdSet.addAll(itemIdSet);
                        }
                        attrMap.put("itemIdSet",itemIdSet);
                        partAttrMap.put(strId,attrMap);
                    }
                }
                DomainObject itemBO = DomainObject.newInstance(context);
                if (allItemIdSet.size() > 0){
                    for (Object oId : allItemIdSet) {
                        String strItemId = (String)oId;
                        itemBO.setId(strItemId);
                        //获取成本项属性
                        AttributeList attrList = itemBO.getAttributes(context).getAttributes();
                        for (int i = 0; i < attrList.size(); i++) {
                            Attribute attr = attrList.get(i);
                            String strAttrName = attr.getName();
                            //标题列移除
                            if (ATTRIBUTE_TITLE.equals(strAttrName)){
                                continue;
                            }
                            if (itemAttrMap.containsKey(strItemId)) {
                                String strPartId = (String) itemAttrMap.get(strItemId);
                                Map partAttrMapInfo = (Map) partAttrMap.get(strPartId);
                                //往part信息里面添加专属的属性信息
                                partAttrMapInfo.put(strAttrName,attr);
                            }
                            if (!dynamicFieldMap.containsKey(strAttrName)){
                                dynamicFieldMap.put(strAttrName,attr);
                            }
                        }
                    }
                }
                JF_LOGGER.info("partAttrMap:{}",partAttrMap);
                for(Object oEntry : attrSettingMap.entrySet()){
                    Map.Entry entry = (Map.Entry)oEntry;
                    Map<Object, Object> colMap = new HashMap<>();
                    Map settingsMap = new HashMap<>();
                    String strAttrName = (String) entry.getKey();
                    Map settingMap = (Map) entry.getValue();
                    String strEditable = "true";
                    String strGroupHeader = "";
                    String strRange = "";
                    JF_LOGGER.info("strAttrName:{}",strAttrName);
                        strEditable = (String) settingMap.get("editable");
                        strGroupHeader = (String) settingMap.get("Group Header");
                        strRange = (String) settingMap.get("range");
                    String strRequired = "false";
                    String strLabel = "emxFramework.Attribute." + strAttrName.replaceAll(" ", "_");
                    String strRegisteredSuite = "Framework";
                    //如果有range值  是否真皮 设置为单选，其他设置为文本框并加上实数校验  update by ljr 20260325
                    if (UIUtil.isNullOrEmpty(strRange)) {
                        settingsMap.put("Input Type", "textbox");
                        settingsMap.put("Validate", "validateInputIsRealNumber");
                    } else {
                        settingsMap.put("Input Type", "combobox");
                        settingsMap.put("Field Type", "attribute");
                        settingsMap.put("Admin Type", "attribute_" + strAttrName.replaceAll(" ", "_"));
                    }
                    //设置分组
                    if (UIUtil.isNotNullAndNotEmpty(strGroupHeader)) {
                        settingsMap.put("Group Header", strGroupHeader);
                    }
                    settingsMap.put("Required", strRequired);
                    settingsMap.put("Editable", strEditable);
                    settingsMap.put("Registered Suite", strRegisteredSuite);
                    settingsMap.put("Update Function", "updateCostAnalyFiled");
                    settingsMap.put("Update Program", "JF_CostAnalysis");
                    settingsMap.put("Edit Access Function", "getUpdateCostAnalyFiledAccess");
                    settingsMap.put("Edit Access Program", "JF_CostAnalysis");
                    settingsMap.put("Column Type", "program");
                    settingsMap.put("function", "getCostAnalyFiledValue");
                    settingsMap.put("program", "JF_CostAnalysis");
                    settingsMap.put("On Change Handler", "markEditSelectedLine");
                    colMap.put("settings", settingsMap);
                    colMap.put("name", strAttrName);
                    colMap.put("label", strLabel);
                    colMap.put("expression_businessobject", JF_PublicMethodClass_mxJPO.buildStringInStrings("attribute[",strAttrName,"]"));
                    fieldMapList.add(colMap);
                }
                // 创建一个排序后的 LinkedHashMap，保持 key 的升序
                Map<Integer, String> sortedMap = new LinkedHashMap<>();

                // 将原始 Map 的 entrySet 转成 List 并按 key 排序
                excelItemAttrMap.entrySet()
                        .stream()
                        .sorted((m1,m2) ->{
                            int m1Num = m1.getValue().getRowNum();
                            int m2Num = m2.getValue().getRowNum();
                            return  m1Num - m2Num ;
                        })
                        .forEachOrdered(e -> {
                            int rowNum = e.getValue().getRowNum();
                            String strAttrName = e.getKey();
                            sortedMap.put(rowNum,strAttrName);
                        });
                //保存已经排序过的Map
                Set<Map> sortedAttrMap = new HashSet<>();
                //排序属性
                for (Map.Entry<Integer, String> entry : sortedMap.entrySet()) {
                    String strExcelAttrName = entry.getValue();
                    for (int i = 0; i < fieldMapList.size(); i++) {
                        Map attrMap = (Map) fieldMapList.get(i);
                        String strAttrName = (String) attrMap.get(SELECT_NAME);
                        if (strExcelAttrName.equals(strAttrName) && (!sortedAttrMap.contains(attrMap))){
                            sortMapList.add(attrMap);
                            sortedAttrMap.add(attrMap);
                        }
                    }
                }
                //添加卷积 成本的列
                addCostAnalysisAttr(sortMapList);
            }
        }catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
        return sortMapList ;
    }

    /**
    *
    *@description 获取编辑成本快速的属性权限
    *@param context
	*@param args
    *@return matrix.util.StringList
    *@throws
    *@author CHENYAN
    *@date 2025/3/11 15:44
    */

    public StringList getUpdateCostAnalyFiledAccess(Context context ,String[] args) throws Exception {
        String strLoginUser = context.getUser();
        Map parameter = (Map)JPO.unpackArgs(args);
        Map requestMap = (Map) parameter.get("requestMap");
        String strObjectId = (String) requestMap.get("objectId");
        MapList partList = (MapList)parameter.get("objectList");
        Map columnMap = (Map)parameter.get("columnMap");
        Map colAttrMap = (Map)columnMap.get("colAttrMap");
        StringList res = new StringList();
        //table列名称
        String strColName = (String)colAttrMap.get("name");
            for (int i = 0; i < partList.size(); i++) {
                Boolean isEdit = Boolean.FALSE ;
                Map partInfoMap = (Map) partList.get(i);
                if (partInfoMap.containsKey("attrMapInfo")) {
                    Map attrMap = (Map) partInfoMap.get("attrMapInfo");
                    for (Object oEntry : attrMap.entrySet()) {
                        Map.Entry entry = (Map.Entry)oEntry;
                        String strAttrName = (String)entry.getKey();
                        if (strAttrName.equals(strColName)){
                            isEdit = Boolean.TRUE;
                            break;
                        }
                    }
                }
                res.add(isEdit.toString());
            }
        return res;
    }

    public StringList getCostAnalyFiledValue(Context context ,String[] args) throws Exception {
        JF_LOGGER.info("----------------------------------------- getCostAnalyFiledValue begin -----------------------------");
        Map parameter = (Map)JPO.unpackArgs(args);
        MapList partList = (MapList)parameter.get("objectList");
        Map columnMap = (Map)parameter.get("columnMap");
        Map colAttrMap = (Map)columnMap.get("colAttrMap");
        StringList res = new StringList();
        //table列名称
        String strColName = (String)colAttrMap.get("name");
        JF_LOGGER.info("strColName:{}", strColName);
        String strSelectAttr = JF_PublicMethodClass_mxJPO.buildStringInStrings("attribute[" + strColName + "]");
        String strHasAttrValue = JF_PublicMethodClass_mxJPO.buildStringInStrings("interface.",strSelectAttr);
        DomainObject partObject = DomainObject.newInstance(context);
        DomainRelationship domainRelationship;
        for (int i = 0; i < partList.size(); i++) {
            String strFieldValue = EMPTY_STRING ;
            Map partInfoMap = (Map) partList.get(i);
            JF_LOGGER.info("partInfoMap:{}", partInfoMap);
            JF_LOGGER.info("partInfoMap:{}", partInfoMap.containsKey("attrMapInfo"));
            if (partInfoMap.containsKey("attrMapInfo")) {
                if (partAttrList.contains(strColName)) {
                    String connId = (String) partInfoMap.get("id[connection]");
                    String id = (String) partInfoMap.get("id");
                    if (strColName.contains("JF_VPMInstance")) {
                        strFieldValue = "1";
//                        if (UIUtil.isNullOrEmpty(connId)) {
//                            strFieldValue = "";
//                        } else {
//                            domainRelationship = DomainRelationship.newInstance(context, connId);
//                            strFieldValue = domainRelationship.getAttributeValue(context, strColName);
//                        }
                    } else {
                        partObject.setId(id);
                        strFieldValue = partObject.getAttributeValue(context, strColName);
                        //如果是零件采购类型 需要国际化
                        if("JF_VPMReference.JF_ProcurementType".equalsIgnoreCase(strColName)) {
                            strFieldValue = EnoviaResourceBundle.getRangeI18NString(context, strColName, strFieldValue, context.getLocale().getLanguage());
                        }
                    }
                } else {
                    Map attrMap = (Map) partInfoMap.get("attrMapInfo");
                    JF_LOGGER.info("attrMap:{}", attrMap);
                    JF_LOGGER.info("attrMap:{}", attrMap.containsKey(strColName));
                    Set itemIdSet = (Set) attrMap.get("itemIdSet");
                    if (attrMap.containsKey(strColName)) {
                        MapList fieldMapList = DomainObject.getInfo(context, (String[]) itemIdSet.toArray(new String[itemIdSet.size()]),
                                StringList.create(strSelectAttr, strHasAttrValue));
                        for (int i1 = 0; i1 < fieldMapList.size(); i1++) {
                            Map fieldMapInfo = (Map) fieldMapList.get(i1);
                            //是否包含该属性结果
                            String strResHasAttr = (String) fieldMapInfo.get(strHasAttrValue);
                            if ("TRUE".equalsIgnoreCase(strResHasAttr)) {
                                strFieldValue = (String) fieldMapInfo.get(strSelectAttr);
                            }
                        }
                    } else {
                        //add by ljr  将卷积成本显示到小数点后两位 20260325
                        if (ATTR_JFRollUpCost.equals(strColName)) {
                            String itemId = StringList.create(itemIdSet).get(0);
                            DomainObject domainObject = DomainObject.newInstance(context, itemId);
                            String costId = domainObject.getInfo(context, "to[JFCostAnalysis2CostItem].from.id");
                            domainObject.setId(costId);
                            String attributeValue = domainObject.getAttributeValue(context, ATTR_JFRollUpCost);
                            if (UIUtil.isNotNullAndNotEmpty(attributeValue)) {
                                BigDecimal bd = new BigDecimal(attributeValue);
                                strFieldValue = bd.setScale(2, RoundingMode.DOWN).toPlainString();
                            } else {
                                strFieldValue = attributeValue;
                            }
                        }
                    }
                }
            }
            JF_LOGGER.info("strFieldValue:{}", strFieldValue);
            res.add(strFieldValue);
        }
        JF_LOGGER.info("----------------------------------------- getCostAnalyFiledValue end -----------------------------");
        return res;
    }

    public void addCostAnalysisAttr(MapList dynamicList){
        StringList costAnalysisAttrList = StringList.create(ATTR_JFActualcCost, ATTR_JFRollUpCost);
        for (int i = 0; i < costAnalysisAttrList.size(); i++) {
            Map<Object, Object> colMap = new HashMap<>();
            Map settingsMap = new HashMap<>();
            String strAttrName = costAnalysisAttrList.get(i);
            String strEditable = "false";
            String strRequired = "false";
            String strLabel = "emxFramework.Attribute." + strAttrName;
            //add by ljr  将卷积成本显示到小数点后两位需要program 20260325
            if (ATTR_JFRollUpCost.equals(strAttrName)){
                strLabel = "emxFramework.Attribute.JFSelfRollUpCost";
                settingsMap.put("function", "getCostAnalyFiledValue");
                settingsMap.put("program", "JF_CostAnalysis");
                settingsMap.put("Column Type", "program");
                colMap.put("expression_businessobject", "dump");
            } else {
                colMap.put("expression_businessobject", JF_PublicMethodClass_mxJPO.buildStringInStrings("from[", rel_JFVPMReference2CostAnalysis, "].to.attribute[", strAttrName, "]"));
            }
            // 设置实际成本可以编辑  add by chenyan 2025/06/05
            if (ATTR_JFActualcCost.equals(strAttrName)){
                strEditable = "true";
                settingsMap.put("Validate", "validateInputCostAnalysis");
            }
            String strRegisteredSuite = "Framework";
//                String strInputType = "textbox";
//                        settingsMap.put("Column Type", "program");
            settingsMap.put("Required", strRequired);
            settingsMap.put("Editable", strEditable);

            settingsMap.put("Input Type", "textbox");
            settingsMap.put("Registered Suite", strRegisteredSuite);
            settingsMap.put("Update Function", "updateCostAnalyFiled");
            settingsMap.put("Update Program", "JF_CostAnalysis");
            settingsMap.put("On Change Handler", "markEditSelectedLine");
//            settingsMap.put("Edit Access Function", "getUpdateCostAnalyFiledAccess");
//            settingsMap.put("Edit Access Program", "JF_CostAnalysis");
            colMap.put("settings", settingsMap);
            colMap.put("name", strAttrName);
            colMap.put("label", strLabel);
            dynamicList.add(colMap);
        }

    }

    /**
    *
    *@description 修改动态列上属性
    *@param context
	*@param args
    *@return void
    *@throws
    *@author CHENYAN
    *@date 2025/3/13 16:36
    */
    public void  updateCostAnalyFiled(Context context ,String[] args) throws Exception {
        JF_LOGGER.info("---------------------------------------------  updateCostAnalyFiled begin -------------------------------------------------------");
        Map parameter = (Map)JPO.unpackArgs(args);
        HashMap paramMap = (HashMap)parameter.get("paramMap");
        HashMap requestMap = (HashMap)parameter.get("requestMap");
        HashMap columnMap = (HashMap)parameter.get("columnMap");
        //partId
        String strPartId = (String)paramMap.get("objectId");
        //修改value值
        String strNewValue = (String)paramMap.get("New Value");
        //修改列信息
        String strColName = (String)columnMap.get("name");
        JF_LOGGER.info("strPartId:{}",strPartId);
        JF_LOGGER.info("strNewValue:{}",strNewValue);
        JF_LOGGER.info("strColName:{}",strColName);
        DomainObject part = DomainObject.newInstance(context, strPartId);
        //修改卷积属性
        if (ATTR_JFActualcCost.equals(strColName)){
            String strCostAnalysisId = part.getInfo(context, JF_PublicMethodClass_mxJPO.buildStringInStrings("from[", rel_JFVPMReference2CostAnalysis, "].to.id"));
            JF_LOGGER.info("strCostAnalysisId:{}",strCostAnalysisId);
            if (UIUtil.isNotNullAndNotEmpty(strCostAnalysisId)){
                try {
                    ContextUtil.startTransaction(context,true);
                    DomainObject costAnalysisBO = DomainObject.newInstance(context, strCostAnalysisId);
                    costAnalysisBO.setAttributeValue(context,strColName,strNewValue);
                    ContextUtil.commitTransaction(context);
                } catch (FrameworkException e) {
                    ContextUtil.abortTransaction(context);
                    throw new RuntimeException(e);
                }
            }

        }else {
            //判断该对象是否存在改属性
            String strBusWhere = JF_PublicMethodClass_mxJPO.buildStringInStrings("interface.attribute[",strColName,"]==TRUE");
            String strCostId = part.getInfo(context,JF_PublicMethodClass_mxJPO.buildStringInStrings("from[",rel_JFVPMReference2CostAnalysis,"].to.id"));
            JF_LOGGER.info("strCostId:{}",strCostId);
            //重新输入值成本快速id
            part.setId(strCostId);
            MapList objectList = part.getRelatedObjects(context,
                    rel_JFCostAnalysis2CostItem, //pattern to match relationships
                    TYPE_JFCostAnalysisItem, //pattern to match types
                    JF_Util_mxJPO.basicBolistSel(), //the eMatrix StringList object that holds the list of select statement pertaining to Business Obejcts.
                    JF_Util_mxJPO.basicRellistSel(), //the eMatrix StringList object that holds the list of select statement pertaining to Relationships.
                    false, //get To relationships
                    true, //get From relationships
                    (short) 1, //the number of levels to expand, 0 equals expand all.
                    strBusWhere, //where clause to apply to objects, can be empty ""
                    EMPTY_STRING, //where clause to apply to relationship, can be empty ""
                    (short)0, //limit
                    false, //checkHidden
                    true, //preventDuplicates
                    (short)0, //pageSize
                    null,
                    null,
                    null,
                    "all") ;// end(返回叶子节点) relationship(返回关系pattern中最后一个关系) all(全返回);
            if (objectList.size() > 0) {
                try {
                    ContextUtil.startTransaction(context,true);
                    DomainObject itemBO = DomainObject.newInstance(context);
                    for (int i = 0; i < objectList.size(); i++) {
                        Map infoMap = (Map) objectList.get(i);
                        String strId = (String)infoMap.get(SELECT_ID);
                        itemBO.setId(strId);
                        itemBO.setAttributeValue(context,strColName,strNewValue);
                    }
                    ContextUtil.commitTransaction(context);
                } catch (FrameworkException e) {
                    ContextUtil.abortTransaction(context);
                    throw new RuntimeException(e);
                }

            }
        }
        JF_LOGGER.info("---------------------------------------------  updateCostAnalyFiled end -------------------------------------------------------");
    }

    /**
    * BOM成本核算
    * @param context
	* @param args
    * @author LIUJR
    * @throws
    * @return java.util.Map
    * @date 2026/3/30 14:55
    * @description
    */
    @com.matrixone.apps.framework.ui.PostProcessCallable
    public Map updateCostAnalysisAttrAndFlush(Context context, String[] args) throws Exception {
        JF_LOGGER.info("----------------------------------  updateCostAnalysisAttrAndFlush begin --------------------------------------------");
        initTemplateFile();
        Map inputMap = (HashMap)JPO.unpackArgs(args);
        Map requestInfo = (Map) inputMap.get("requestMap");
        List elementList = null;
        Document doc = (Document) inputMap.get("XMLDoc");
        if(doc != null) {
            Element rootElement = (Element)doc.getRootElement();
            elementList     = rootElement.getChildren("object");
        }
//            JF_LOGGER.info("inputMap:{}",inputMap);
        JF_LOGGER.info("elementList:{}",elementList.size());
        //零件属性
        StringList selectPartAttrList = getSelectPartAttrList();
        //bom属性
        StringList bomSelectList = getSelectPartBOMAttrList();
        //加上几个特殊的属性
        for (int i = 0; i <partAttrList.size(); i++) {
            if (partAttrList.get(i).contains("JF_VPMInstance")) {
                continue;
            }
            selectPartAttrList.add("attribute[" + partAttrList.get(i) + "]");
        }
        if (Objects.nonNull(elementList)){
            DomainObject part = DomainObject.newInstance(context);
            for (int i = 0; i < elementList.size(); i++) {
                Workbook workbook =  WorkbookFactory.create(new ByteArrayInputStream(templateFileBytes));
                // 创建公式评估器并评估单元格
                FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();
                //零件属性
                Map<String,Row> partAttrMap = getPartAttrByWorkBook(workbook,0,partNameAttrColIndex,1);
                //成本项属性
                Map<String,Row> itemAttrMap = getPartAttrByWorkBook(workbook,1,itemAttrNameColIndex,1);
                Collection<Row> rowCollection = itemAttrMap.values();
                //按照行号排序公式行
                List<Row> sortedRowList = rowCollection.stream().filter(row -> {
                    return checkAttrIsFormula(row, itemAttrValueColIndex);
                }).sorted((row1, row2) -> {
                    return row1.getRowNum() - row2.getRowNum();
                }).collect(Collectors.toList());
                //保存写入的行
                List<Row> writePartRowList = new ArrayList<>();
                List<Row> writeRowList = new ArrayList<>();
                Element docElement = (Element)elementList.get(i);
                String strObjectId = docElement.getAttributeValue("objectId");
                String strRelId = docElement.getAttributeValue("relId");
                JF_LOGGER.info("strObjectId:{}",strObjectId);
                JF_LOGGER.info("strRelId:{}",strRelId);

                part.setId(strObjectId);
                String strSelectCostId = JF_PublicMethodClass_mxJPO.buildStringInStrings("from[", rel_JFVPMReference2CostAnalysis, "].to.id");
                selectPartAttrList.add(strSelectCostId);
                Map partInfoMap = part.getInfo(context, selectPartAttrList);
                //关系为空时标识没有BOM属性 一个零件会存在多个BOM关系
                if (UIUtil.isNotNullAndNotEmpty(strRelId)){
                    String[] relSplitArr = strRelId.split(",");
                    for (int i1 = 0; i1 < relSplitArr.length; i1++) {
                        String strRealRelId = relSplitArr[i1];
                        DomainRelationship rel = DomainRelationship.newInstance(context,strRealRelId);
                        AttributeList relAttributeValues = rel.getAttributeValues(context, bomSelectList);
                        for (int i2 = 0; i2 < relAttributeValues.size(); i2++) {
                            Attribute attribute = relAttributeValues.get(i2);
                            String strRelAttrName = attribute.getName();
                            String strRelAttrValue = attribute.getValue();
                            //如果是数值型需要累加BOM属性
                            //用量默认为 1  update by 20260402 ljr
                            if ("JF_VPMInstance.JF_Dosage".equalsIgnoreCase(strRelAttrName)) {
                                partInfoMap.put(strRelAttrName, "1");
                            } else {
                                if (partInfoMap.containsKey(strRelAttrName)) {
                                    String strAttrValue = (String) partInfoMap.get(strRelAttrName);
                                    boolean oldIsNum = JF_PublicMethodClass_mxJPO.isNumericOrPercentage(strAttrValue);
                                    if (oldIsNum) {
                                        boolean newIsNum = JF_PublicMethodClass_mxJPO.isNumericOrPercentage(strRelAttrValue);
                                        if (newIsNum) {
                                            BigDecimal oldDecimal = new BigDecimal(strAttrValue);
                                            BigDecimal newDecimal = new BigDecimal(strRelAttrValue);
                                            String strSumNum = oldDecimal.add(newDecimal).toString();
                                            partInfoMap.put(strRelAttrName, strSumNum);
                                        }
                                    }
                                } else {
                                    partInfoMap.put(strRelAttrName, strRelAttrValue);
                                }
                            }
                        }
                    }

                }
                String strCostAnalysisId = (String) partInfoMap.get(strSelectCostId);
                DomainObject costingBO = DomainObject.newInstance(context, strCostAnalysisId);
                StringList itemIdList = costingBO.getInfoList(context, "from[" + rel_JFCostAnalysis2CostItem + "].to.id");
                //写入零件属性
                writePartAttr(partAttrMap,partInfoMap,writePartRowList);
                //成本
                DomainObject itemBO = DomainObject.newInstance(context);
                //存在的公式属性
                Map<Row,String> existFormulaRowMap = new HashMap<>();
                for (int i1 = 0; i1 < itemIdList.size(); i1++) {
                    String strCostItemId = itemIdList.get(i1);
                    itemBO.setId(strCostItemId);
                    AttributeList attrList = itemBO.getAttributes(context).getAttributes();
                    for (int i2 = 0; i2 < attrList.size(); i2++) {
                        Attribute attribute = attrList.get(i2);
                        String strAttrName = attribute.getName();
                        String strAttrValue = attribute.getValue();
                            //存在该属性
                            if (itemAttrMap.containsKey(strAttrName)) {
                               //判断该行的的填充值是不是函数 不是函数才填充
                                Row itemAttrRow = itemAttrMap.get(strAttrName);
                                boolean isFormula = checkAttrIsFormula(itemAttrRow, itemAttrValueColIndex);
                                if (isFormula) {
                                    existFormulaRowMap.put(itemAttrRow,strCostItemId);
//                                    itemAttrRow.createCell()
                                }else {
                                    if (UIUtil.isNotNullAndNotEmpty(strAttrValue)){
                                        Cell itemCell = itemAttrRow.getCell(itemAttrValueColIndex);
                                        //转换%符号
                                        boolean isNum = JF_PublicMethodClass_mxJPO.isNumericOrPercentage(strAttrValue);
                                        //写入excel

                                        if (isNum) {
                                            Double dDouble = JF_PublicMethodClass_mxJPO.parseToDouble(strAttrValue);
                                            if (Objects.nonNull(dDouble)){
                                                itemCell.setCellValue(dDouble);
                                            }
                                        }else {
                                            itemCell.setCellValue(strAttrValue);
                                        }
                                        //保存写入行，待最后清空
                                        writeRowList.add(itemAttrRow);
                                    }

                                }
                            }
                    }
                }
                // 添加成分分析的卷积成本属性
                Row rollUpAttrRow = itemAttrMap.get("JFRollUpCost");
                JF_LOGGER.info("existFormulaRowMap:{}",existFormulaRowMap.size());
                JF_LOGGER.info("strCostAnalysisId:{}",strCostAnalysisId);
                existFormulaRowMap.put(rollUpAttrRow,strCostAnalysisId);
                //属性写入完成，开始计算公式属性行
                // 获取计算结果
                try {
                    ContextUtil.startTransaction(context,true);
                    DomainObject itemBo = DomainObject.newInstance(context);
                    // 公式的执行必须按顺序 否则计算结果不对
                    for (int i1 = 0; i1 < sortedRowList.size(); i1++) {
                        Row formulaRow = sortedRowList.get(i1);
                        if (existFormulaRowMap.containsKey(formulaRow)){
                            Cell formulaCellName = formulaRow.getCell(itemAttrNameColIndex);
                            String strAttrName = formulaCellName.getStringCellValue();
                            strAttrName = strAttrName.trim();
                            JF_LOGGER.info("strAttrName:{}",strAttrName);
                            Cell formulaCell = formulaRow.getCell(itemAttrValueColIndex);
                            CellValue cellValue = evaluator.evaluate(formulaCell);
                            String strCalcValue = cellValue.formatAsString();
                            String strCostItemId = existFormulaRowMap.get(formulaRow);
                            JF_LOGGER.info("strCalcValue:{}",strCalcValue);
                            if (UIUtil.isNotNullAndNotEmpty(strCalcValue)){
                                itemBo.setId(strCostItemId);
                                itemBo.setAttributeValue(context,strAttrName,strCalcValue);
                            }
                        }
                    }

                    ContextUtil.commitTransaction(context);
                } catch (Exception e) {
                    //公式报错直接不设置该属性
                        ContextUtil.abortTransaction(context);
                        e.printStackTrace();
                    JF_LOGGER.error(e.getMessage());
                        throw new RuntimeException(e);
                }
                //注释掉生成excel
//                workbook.write(new FileOutputStream("/tmp/chenyan/" + System.currentTimeMillis() + ".xlsx"));
                workbook.close();
            }
        }
        Map returnMap = new HashMap();
        returnMap.put ("Action", "execScript");
        returnMap.put("Message", "{ main:function()  {window.emxEditableTable.refreshStructure()}}");
//        returnMap.put("Message", "{ main:function()  {getTopWindow().close();var listFrame = findFrame(getTopWindow().opener.getTopWindow(),'detailsDisplay');listFrame.location.href=listFrame.location.href;}}");
        JF_LOGGER.info("----------------------------------  updateCostAnalysisAttrAndFlush end --------------------------------------------");
        return returnMap;
    }
    /**
     * 当零件创建成本分析的时候,卷积子级成本
     * @param context
     * @param args
     * @author LIUJR
     * @throws
     * @return void
     * @date 2025/3/13 13:40
     * @description
     */
    public void convolutionCostAnalysis(Context context, String[] args) throws Exception{
        try {
            String fromId = args[0];
            String toId = args[1];
            String relId = args[2];
            /*
             *   通过from端id，判断该零件的子零件是否关联了成本分析
             *   有就拿取成本分析的卷积成本属性，并累加。
             *   拿取to端的成本分析，赋值进去。
             */
            DomainObject partObject = DomainObject.newInstance(context, fromId);
            StringList childrenPartList = partObject.getInfoList(context, "from[" + JF_PLMConstants_mxJPO.REL_Instance + "].to.id");
            JF_LOGGER.info("childrenPartList:{}", childrenPartList);
            //卷积成本
            //实际成本
            selList.add(relIsExist);
            selList.add(strJFRollUpCost);
            selList.add(strJFActualcCost);
            MapList partCostAnalysisList = DomainObject.getInfo(context, childrenPartList.toStringArray(), selList);
            JF_LOGGER.info("partCostAnalysisList:{}", partCostAnalysisList);
            Double count = 0.0;
            Iterator iterator = partCostAnalysisList.iterator();
            while (iterator.hasNext()) {
                Map map = (Map) iterator.next();
                String strRelIsExist = UIUtil.getValue(map, relIsExist);
                String strRollUpCost = UIUtil.getValue(map, strJFRollUpCost);
                String strActualcCost = UIUtil.getValue(map, strJFActualcCost);
                if ("TRUE".equalsIgnoreCase(strRelIsExist)) {
                    String cost = UIUtil.isNullOrEmpty(strRollUpCost) || "0.0".equalsIgnoreCase(strRollUpCost) ? UIUtil.isNullOrEmpty(strActualcCost) ? "0.0" : strActualcCost : strRollUpCost;
                    Double icount = Double.valueOf(cost);
                    count += icount;
                }
            }
            JF_LOGGER.info("count:{}", count);
            DomainObject domainObject = DomainObject.newInstance(context, toId);
            //设置值
            domainObject.setAttributeValue(context, JF_PLMConstants_mxJPO.ATTR_JFRollUpCost, String.valueOf(count));
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 成本分析 修改时候 拿取零件的父级零件  并重新计算旗下的子级成本卷积
     * @param context
     * @param args
     * @author LIUJR
     * @throws
     * @return void
     * @date 2025/3/13 13:53
     * @description
     */
    public void convolutionCostAnalysisModify(Context context, String[] args) throws Exception{
        try {
            JF_LOGGER.info("convolutionCostAnalysisModify start....");
            String objectId = args[0];
            String attrName = args[1];
            String attrValue = args[2];
            JF_LOGGER.info("objectId:{}", objectId);
            DomainObject costAnalysisObject = DomainObject.newInstance(context, objectId);
            String partId = costAnalysisObject.getInfo(context, "to[JFVPMReference2CostAnalysis].from.id");
            if (UIUtil.isNotNullAndNotEmpty(partId)) {
                DomainObject partObject = DomainObject.newInstance(context, partId);
                StringList parentPartList = partObject.getInfoList(context, "to[" + JF_PLMConstants_mxJPO.REL_Instance + "].from.id");
                JF_LOGGER.info("parentPartList:{}", parentPartList);
                //父级 需要拿到下面的第一级子级
                selList.add(relIsExist);
                selList.add(strJFRollUpCost);
                selList.add(strJFActualcCost);
                for (String parentId : parentPartList) {
                    DomainObject domainObject = DomainObject.newInstance(context, parentId);
                    String strCostId = domainObject.getInfo(context, relIsExist + ".to.id");
                    JF_LOGGER.info("strCostId:{}", strCostId);
                    if (UIUtil.isNullOrEmpty(strCostId)) {
                        continue;
                    }
                    StringList childrenPartList = domainObject.getInfoList(context, "from[" + JF_PLMConstants_mxJPO.REL_Instance + "].to.id");
                    MapList partCostAnalysisList = DomainObject.getInfo(context, childrenPartList.toStringArray(), selList);
                    JF_LOGGER.info("partCostAnalysisList:{}", partCostAnalysisList);
                    Double count = 0.0;
                    Iterator iterator = partCostAnalysisList.iterator();
                    while (iterator.hasNext()) {
                        Map map = (Map) iterator.next();
                        String strRelIsExist = UIUtil.getValue(map, relIsExist);
                        String strRollUpCost = UIUtil.getValue(map, strJFRollUpCost);
                        String strActualcCost = UIUtil.getValue(map, strJFActualcCost);
                        if ("TRUE".equalsIgnoreCase(strRelIsExist)) {
                            String cost = UIUtil.isNullOrEmpty(strRollUpCost) || "0.0".equalsIgnoreCase(strRollUpCost) ? UIUtil.isNullOrEmpty(strActualcCost) ? "0.0" : strActualcCost : strRollUpCost;
                            Double icount = Double.valueOf(cost);
                            count += icount;
                        }
                    }

                    JF_LOGGER.info("parentId: {} ; count:{}", parentId, count);
                    DomainObject costObject = DomainObject.newInstance(context, strCostId);
                    // 父级节点自身制造成本+所有子级成本 add by chenyan 2025/06/24
//                    costObject.getInfo(context,"a")
                    costObject.setAttributeValue(context, JF_PLMConstants_mxJPO.ATTR_JFRollUpCost, String.valueOf(count));
                }
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
        JF_LOGGER.info("convolutionCostAnalysisModify end....");
    }

    /**
    *
    *@description 获取Excel模版中系统属性列表
    *@param workbook
    *@return java.util.Map<java.lang.String,org.apache.poi.ss.usermodel.Row>
    *@throws
    *@author CHENYAN
    *@date 2025/3/18 15:36
    */
    public Map<String,Row> getPartAttrByWorkBook(Workbook workbook,int sheetIndex,int colIndex,int beginIndex){
        Map res = new HashMap<String,Row>();
        Sheet sheet = workbook.getSheetAt(sheetIndex);
        int iLastRowIndex = sheet.getLastRowNum();
        //从第二行开始
        for (; beginIndex < iLastRowIndex; beginIndex++) {
            Row row = sheet.getRow(beginIndex);
            //属性名称
            Cell attrNameCell = row.getCell(colIndex);
            //值 通过单元格类型判断是否是函数函数输入
            String strAttrName = attrNameCell.getStringCellValue();
            if (UIUtil.isNotNullAndNotEmpty(strAttrName)) {
                strAttrName = strAttrName.trim();
                res.put(strAttrName,row);
            }
        }
        return res ;
    }

    /**
    *
    *@description 清空excel里面填充的数据
    *@param rowList 填充的row
	*@param colIndex 清空的指定列
    *@return void
    *@throws
    *@author CHENYAN
    *@date 2025/3/18 15:46
    */
    public void clearWorkBookDataByRow(List<Row> rowList,int colIndex){
        for (int i = 0; i < rowList.size(); i++) {
            Row row = rowList.get(i);
            Cell cell = row.getCell(colIndex);
            CellType cellType = cell.getCellType();
            //非函数单元格都需要清空
            if (!CellType.FORMULA.equals(cellType)){
                cell.setBlank();
            }
        }
    }

    /**
    *
    *@description 检查该属性行是输入值还是公式
    *@param row
	*@param colIndex
    *@return void
    *@throws
    *@author CHENYAN
    *@date 2025/3/18 16:19
    */
    public static boolean checkAttrIsFormula(Row row ,int colIndex){
        Cell cell = row.getCell(colIndex);
        boolean isFormual = false;
        if (Objects.nonNull(cell)){
            CellType cellType = cell.getCellType();
            isFormual = CellType.FORMULA.equals(cellType);
        }
        return isFormual;
    }

    /**
    *
    *@description 写入part属性
    *@param partAttrMap  零件excel的属性
	*@param partInfoMap 查询到零件属性数据
	*@param saveWriteRowList 保存写入行集合
    *@return void
    *@throws
    *@author CHENYAN
    *@date 2025/3/18 16:42
    */
    public void writePartAttr(Map<String,Row> partAttrMap ,Map partInfoMap,List<Row> saveWriteRowList){
        for(Object oEntry : partInfoMap.entrySet()){
            Map.Entry entry = (Map.Entry)oEntry;
            String strKey = (String) entry.getKey();
            String strValue = (String) entry.getValue();
            if (UIUtil.isNotNullAndNotEmpty(strValue)){
                //移除select属性
                strKey = strKey.replace("attribute[", "").replace("]", "");
                if (partAttrMap.containsKey(strKey)){
                    Row partAttrRow = partAttrMap.get(strKey);
                    if (!checkAttrIsFormula(partAttrRow,partAttrColValueIndex)){
                        Cell cell = partAttrRow.getCell(partAttrColValueIndex);
                        //转换%符号
                        boolean isNum = JF_PublicMethodClass_mxJPO.isNumericOrPercentage(strValue);
                        //写入excel

                        if (isNum) {
                            Double dDouble = JF_PublicMethodClass_mxJPO.parseToDouble(strValue);
                            if (Objects.nonNull(dDouble)){
                                cell.setCellValue(dDouble);
                            }
                        }else {
                            cell.setCellValue(strValue);
                        }
                        saveWriteRowList.add(partAttrRow);
                    }
                }
            }
        }
    }

    public StringList getSelectPartAttrList(){
        StringList typeSelectList = JF_Util_mxJPO.basicBolistSel();
        typeSelectList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_V_PART_NUMBER);
        typeSelectList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JF_PartNameCN);
        typeSelectList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JF_PartNameEN);
        typeSelectList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JF_SurfaceTreatment);
        typeSelectList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JF_Material);
        typeSelectList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JF_Lon);
        typeSelectList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JF_Wid);
        typeSelectList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JF_Hig);
        typeSelectList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JF_Weight);
        typeSelectList.add("attribute[CustomerPartNumber]");
        typeSelectList.add("attribute[CustomerPartRevision]");
        typeSelectList.add("attribute[PLMEntity.V_description]");
        typeSelectList.add("attribute[PLMEntity.V_Name]");
        typeSelectList.add("attribute[JF_VPMReference.JF_WeightTarget]");
        typeSelectList.add("attribute[JF_VPMReference.JF_Unit]");
        typeSelectList.add("attribute[JF_VPMReference.JF_FlexiblePart]");
//        typeSelectList.add("attribute[JF_VPMReference.JF_Fabric]");
        typeSelectList.add("attribute[JF_VPMReference.JF_GramWeight]");
        typeSelectList.add("attribute[JF_VPMReference.JF_MaterialStar]");
        typeSelectList.add("attribute[JF_VPMReference.JF_OriginalPart]");
        typeSelectList.add("attribute[JF_VPMReference.JF_PartDes]");
        typeSelectList.add("attribute[JF_VPMReference.JF_PartNumberExternal]");
        typeSelectList.add("attribute[JF_VPMReference.JF_ProcurementType]");
        typeSelectList.add("attribute[JF_VPMReference.JF_PartSubType]");
        typeSelectList.add("attribute[JF_VPMReference.JF_ProjectRel]");
        typeSelectList.add("attribute[JF_VPMReference.JF_SupplierOrSupplierPartNumber]");
        typeSelectList.add("attribute[JF_VPMReference.JF_SurfaceTreatment]");
        typeSelectList.add("attribute[JF_VPMReference.JF_TransformationPlan]");
        typeSelectList.add("attribute[JF_VPMReference.JF_TransferDownstreamSystem]");
        typeSelectList.add(SELECT_ATTR_JFPartType);
        return typeSelectList;
    }
    public StringList getSelectPartBOMAttrList(){
        StringList bomSelectList = new StringList();
        bomSelectList.add("JF_VPMInstance.JF_Color");
        bomSelectList.add("JF_VPMInstance.JF_Dosage");
        bomSelectList.add("JF_VPMInstance.JF_FNA");
        bomSelectList.add("JF_VPMInstance.JF_OnlyEBOMPart");
        bomSelectList.add("JF_VPMInstance.JF_ProcurementType");
        bomSelectList.add("JF_VPMInstance.JF_Symmetry");
        return bomSelectList;
    }
    public static Map getCostAnalysisAttrSettingByPage(Context context) throws Exception{
        String strJsonAttr = JF_PublicMethodClass_mxJPO.getBasicUrl(context,new String[]{"CostAnalysisEditAttrJsonList"});
        Gson gson = new Gson();
        Map AttrMap = gson.fromJson(strJsonAttr, Map.class);
        return AttrMap ;
    }

    /**
    *
    *@description 将模版写入到内存防止多次加载
    *@param
    *@return void
    *@throws
    *@author CHENYAN
    *@date 2025/6/4 14:55
    */
    public void initTemplateFile()throws Exception{
        if (Objects.isNull(templateFileBytes)){
            String classPath = Thread.currentThread().getContextClassLoader().getResource("").getPath();
            JF_LOGGER.info("classPath:{}",classPath);

            String strSPacePath = JF_PublicMethodClass_mxJPO.get3DspaceServicePath("WEB-INF");
            JF_LOGGER.info("strSPacePath:{}",strSPacePath);
            strSPacePath = JF_PublicMethodClass_mxJPO.buildStringInStrings(strSPacePath,"jf_template/CostingCalcTemplate.xlsx");
            JF_LOGGER.info("strSPacePath:{}",strSPacePath);
            templateFileBytes = Files.readAllBytes(Paths.get(strSPacePath));
        }
    }


}
