import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.DomainRelationship;
import com.matrixone.apps.domain.util.ContextUtil;
import com.matrixone.apps.domain.util.EnoviaResourceBundle;
import com.matrixone.apps.domain.util.FrameworkException;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.framework.ui.UIUtil;
import com.matrixone.apps.program.ProgramCentralConstants;
import matrix.db.Context;
import matrix.db.JPO;
import matrix.util.StringList;
import org.antlr.v4.runtime.misc.IntegerList;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.commons.io.FileUtils;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.util.Units;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.dassault_systemes.product.common.services.utility.Value.VPMInstance;
import static com.matrixone.apps.domain.DomainConstants.*;

/**
 * @ClassName JF_ExportEBOM_mxJPO
 * @Author: LIUJR
 * @CreateDate: 2024/12/6 10:12
 * @UpdateRemark:
 * @Version: 1.0
 * @Description:
 */
public class JF_ExportEBOM_mxJPO implements JF_PLMConstants_mxJPO{

    private static final Logger JF_LOGGER = LoggerFactory.getLogger(JF_ExportEBOM_mxJPO.class);

    private static final String STRING_ATTRIBUTE =  "attribute[%s]";
    private static final Map<String, String> STRING_EXPORT_TYPE =  new HashMap();
    static {
        STRING_EXPORT_TYPE.put("C", "exportCompleteSeat.xlsx");   //整椅
//        STRING_EXPORT_TYPE.put("U", "exportFoam.xlsx");   //发泡
//        STRING_EXPORT_TYPE.put("P", "exportPlasticParts.xlsx");   //塑料件
//        STRING_EXPORT_TYPE.put("T", "exportTrim.xlsx");   //面套
//        STRING_EXPORT_TYPE.put("E", "exportEEParts.xlsx");   //电器
//        STRING_EXPORT_TYPE.put("E", "exportEEParts.xlsx");   //电器
//        STRING_EXPORT_TYPE.put("E", "exportEEParts.xlsx");   //电器
        //骨架
        //线束
        /*
        整椅	Complete Seat
        发泡	Foam
        骨架	Frame
        塑料	Plastic Parts
        线束	Harness
        面套	Trim
        电气	EE Parts */
    }

    /**
    * 导出7个专业的配置EBOM表和配置矩阵表
    * @param context
	* @param args
    * @author LIUJR
    * @throws
    * @return java.util.Map
    * @date 2024/12/11 17:26
    * @description
    */
    public Map exportFormList(Context context, String[] args) throws IOException {
        Map resMap = new HashMap();
        //整椅
        //拿到文件模板，找到sheet1的Configuration，打开并另存为文件
        InputStream inputStream = null;
        try {
            String classPath = Thread.currentThread().getContextClassLoader().getResource("").getPath();
            String exportTempFileName = JF_PublicMethodClass_mxJPO.getBasicUrl(context, new String[]{"DownloadVPM.Template.FileName"});
//            String exportTempFileName = "exportConfigTemplate.xlsx";
            String fileTemPath = classPath.substring(0, classPath.indexOf("WEB-INF")) + "jf_template" + File.separator + exportTempFileName;
            File temFile = new File(fileTemPath);
            String objectId = args[0];
            DomainObject domainObject = DomainObject.newInstance(context, objectId);
            String partNo = domainObject.getAttributeValue(context, ATTR_V_CADOrigin);
            String objectPhyId = domainObject.getInfo(context, DomainRelationship.SELECT_PHYSICAL_ID);
            if (UIUtil.isNullOrEmpty(partNo)) {
                partNo = domainObject.getInfo(context, SELECT_NAME);
            }
            partNo = partNo.replaceAll(" ", "").trim();
            //获取物理产品PC
            Map vpmPCList = getVpmPCList(context, new String[]{objectId});
            JF_LOGGER.info("pcList:{}", vpmPCList.toString());
            MapList allPC = (MapList)  vpmPCList.get("allPC");
            StringList selectPCList = (StringList)  vpmPCList.get("selectPCList");
//            MapList allPCList = (MapList)  vpmPCList.get("allPCList");   //cy
            JF_Config_mxJPO jfConfigMxJPO = new JF_Config_mxJPO();
            //获取Pc下过滤出来的零件列表
            Map<String, StringList> ebomFilterPCPartIdMap = getEBOMFilterPCPartId(context, allPC, objectPhyId, jfConfigMxJPO);
            //开始导出
            for (Map.Entry<String, String> entry : STRING_EXPORT_TYPE.entrySet()) {
                String type = entry.getKey();
                String exportFileName = entry.getValue();
                // 处理每个键值对
                String fileName = partNo + "_" + exportFileName;
                String outPath = classPath.substring(0, classPath.indexOf("WEB-INF")) + "jf_export" + File.separator + fileName;
                resMap.put("filePath", outPath);
                resMap.put("fileName", fileName);
                File outFile = new File(outPath);
                FileUtils.copyFile(temFile, outFile);
                inputStream = new FileInputStream(outFile);
                Workbook workbook = WorkbookFactory.create(inputStream);
                //导出配置矩阵表
                workbook = exportConfiguration(context, objectId, workbook, type, allPC, selectPCList, ebomFilterPCPartIdMap);
                //导出指定的超级BOM
                workbook = exportEBOMByPartId(context, objectId, workbook, type, allPC,ebomFilterPCPartIdMap);
                workbook.write(new FileOutputStream(outFile));
                //EBOM表
                JF_LOGGER.info("完成```````````````````````");
                workbook.close();
            }
        }catch (Exception e) {
            resMap.clear();
            resMap.put("filePath", "");
            resMap.put("fileName", "");
            e.printStackTrace();
        } finally {
          if (inputStream != null) {
              inputStream.close();
          }
        }
        return resMap;
    }

    /**
     * 导出配置矩阵表
     * @param context
     * @param objectId
     * @param workbook
     * @param type
     * @param allPC 物理产品PC MapList
     * @param selectPCList 物理产品PC StringList
     * @param pcVpmBomResMap 获取Pc下过滤出来的零件列表
     * @return
     * @throws Exception
     */
    public Workbook exportConfiguration(Context context, String objectId, Workbook workbook, String type, MapList allPC, StringList selectPCList, Map<String, StringList> pcVpmBomResMap)  throws Exception{
        try{
            JF_Config_mxJPO jfConfigMxJPO = new JF_Config_mxJPO();
            Sheet sheet = workbook.getSheetAt(1);
            Boolean hasPC = selectPCList.size() > 0 ? true : false;
            //拿到列的对应的属性
            Map<Object, Object> colMap = setColAttributeMapping(hasPC);
            if (hasPC) {
                CellStyle titleCellStyle = setCellStyleTitle(workbook, 0);
                CellStyle title1CellStyle = setCellStyleTitle(workbook, 1);
                //写入配置车型Pc 及 变形组和变形值写入
                //这个地方 有记录每一个配置车型列的值 id  例如： 6:Pcid
                sheet = constructPCDynamicColumns(context, sheet, titleCellStyle, title1CellStyle, allPC, selectPCList, colMap);
            }
            //找整椅v5 找到整椅的V5节点，上一级是V6的整椅，拿到其属性与上级节点的关系id.
            Map catiaV5Bom = getCatiaV5Bom(context, new String[]{objectId});
            MapList v5MapList = (MapList) catiaV5Bom.get("v5MapList");
            //单元格风格
            CellStyle basicCellStyle = setCellStyleTitle(workbook, 2);
            //开始创建动态头和列数据  获取动态数据开列
            Integer startRow = (Integer) colMap.get("startRow");
            Integer endVaCell = (Integer) colMap.get("endVaCell");
            Integer startPCCell = (Integer) colMap.get("startPCCell");
            Integer endPCCell = (Integer) colMap.get("endPCCell");
            JF_LOGGER.info("动态特征动态列:{}", colMap.toString());
            //开始构造动态表
            for (int i = 0; i < v5MapList.size(); i++) {
                Map map = (Map) v5MapList.get(i);
                String vpmId = UIUtil.getValue(map, SELECT_ID);
                String vpmPhyId = DomainObject.newInstance(context, vpmId).getInfo(context, DomainConstants.SELECT_PHYSICAL_ID);
                String fromVpmId = UIUtil.getValue(map, "from." + SELECT_ID);
                String fromVpmPhyId = UIUtil.getValue(map, "from." + DomainRelationship.SELECT_PHYSICAL_ID);
                String connId = UIUtil.getValue(map, DomainRelationship.SELECT_PHYSICAL_ID);
                JF_LOGGER.info("vpmPhyId:{}, connId:{}, fromVpmPhyId:{}", vpmPhyId,connId,fromVpmPhyId);
                Map configMain = jfConfigMxJPO.getConfigMain(context, new String[]{connId});
                JF_LOGGER.info("configMain:{}", configMain.toString());
                //备注字段
                if (UIUtil.isNullOrEmpty(UIUtil.getValue(map, SELECT_ATTR_JF_PartDes))) {
                    String desc = UIUtil.getValue(map, DomainConstants.SELECT_DESCRIPTION);
                    if (UIUtil.isNotNullAndNotEmpty(desc)) {
                        map.put(SELECT_ATTR_JF_PartDes, desc);
                    }
                }
                if (UIUtil.isNullOrEmpty(UIUtil.getValue(map, SELECT_ATTR_V_PART_NUMBER))) {
                    String name = UIUtil.getValue(map, DomainConstants.SELECT_NAME);
                    map.put(SELECT_ATTR_V_PART_NUMBER, name);
                }
                for (int j = 0; j <= endVaCell; j++) {
                    String strCell = (String) colMap.get(j);
                    if (map.toString().contains(strCell)) {
                        continue;
                    }
                    //顺序字段
                    if ("item".equalsIgnoreCase(strCell)) {
                        map.put(strCell, String.valueOf(i + 1));
                    }
                    //pc结果值
                    if (j >= startPCCell && j <= endPCCell) {
                        String pcId = strCell;
                        StringList strings = pcVpmBomResMap.get(pcId);
                        if (strings.toString().contains(vpmPhyId)) {
                            long size = strings.stream()
                                    .filter(s -> s.equals(vpmPhyId))
                                    .count();
                            map.put(pcId, size);
                        }
                    }
                    //变形值的设定
                    if (j > endPCCell && j <= endVaCell) {
                        String displayName = strCell;
                        String config = (String) configMain.get(connId);
                        if (UIUtil.isNullOrEmpty(config)) {
                            map.put(displayName, "");
                        } else if (config.contains(displayName)) {
                            map.put(displayName, "√");
                        } else {
                            map.put(displayName, "");
                        }
                    }
                }
            }
            JF_LOGGER.info("动态数据:{}", v5MapList.toString());
            JF_LOGGER.info("v5MapList:{}", v5MapList.size());
            //开始写入文件
            Integer tempRow = startRow;
            for (int i = 0; i < v5MapList.size(); i++){
                Map map = (Map) v5MapList.get(i);
                Row row = sheet.createRow(tempRow);
                for (int j = 0; j <= endVaCell; j++) {
                    Cell cell = row.createCell(j, CellType.STRING);
                    String item = (String) colMap.get(j);
                    String value = String.valueOf(map.get(item));
                    cell.setCellValue(UIUtil.isNullOrEmpty(value) ? DomainConstants.EMPTY_STRING : value);
                    cell.setCellStyle(basicCellStyle);
                }
                tempRow ++;
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
        return workbook;
    }

    /**
     * 构造动态列
     * @param context
     * @param sheet  sheet 1
     * @param titleCellStyle 大标题风格
     * @param title1CellStyle 小标题风格
     * @param allPC pc
     * @param selectPCList pc
     * @param colMap 单元格列
     * @return
     * @throws Exception
     */
    public Sheet constructPCDynamicColumns(Context context, Sheet sheet, CellStyle titleCellStyle, CellStyle title1CellStyle, MapList allPC, StringList selectPCList, Map<Object, Object> colMap) throws Exception{
        //通过产品配置，找到模型，再找到变形和选择组，将其拿出来保存
        Map<String, StringList> variantMap = getPCVariantValues(context, selectPCList);
        //获取写入行和列
        Integer startTitle = (Integer) colMap.get("startTitle");
        Integer startName = (Integer)  colMap.get("startName");
        Integer startPCCell = (Integer)  colMap.get("startPCCell");
        Row rowTitle = sheet.getRow(startTitle);
        Row rowName = sheet.getRow(startName);
        /*产品配置动态列开始构造*/
        for (int i = 0; i < allPC.size(); i++) {
            Integer cellIndex = startPCCell + i;
            Cell cellTitle = rowTitle.createCell(cellIndex, CellType.STRING);
            if (cellIndex.equals(startPCCell)) {
                //第一个单元格的时候
                cellTitle.setCellValue("配置车型");
            }
            cellTitle.setCellStyle(titleCellStyle);
            Cell cellName = rowName.createCell(cellIndex, CellType.STRING);
            Map map = (Map) allPC.get(i);
            String markName = UIUtil.getValue(map, "attribute[Marketing Name]");
            cellName.setCellValue(markName);
            cellName.setCellStyle(title1CellStyle);
            colMap.put(cellIndex, UIUtil.getValue(map, SELECT_ID));
            if (i == allPC.size() -1) {
                //最后一个单元格
                colMap.put("endPCCell", cellIndex);
            }
        }
        JF_LOGGER.info("产品配置动态列:{}", colMap.toString());
        Integer endPCCell = (Integer)  colMap.get("endPCCell");
        //合并pc表头
        CellRangeAddress cellRangeAddress;
        if (startPCCell != endPCCell) {
            cellRangeAddress = new CellRangeAddress(startTitle, startTitle, startPCCell, endPCCell); // 参数依次是：开始行，结束行，开始列，结束列
            sheet.addMergedRegion(cellRangeAddress);
        }
        /*动态特征动态列开始构造*/
        Integer startVaCell = endPCCell + 1;
        colMap.put("startVaCell", startVaCell);
        int count = 0;
        for (Map.Entry<String, StringList> entry: variantMap.entrySet()){
            String variantDisplayName = entry.getKey();
            StringList displayNameList = entry.getValue();
            startVaCell += count;
            Integer cellIndex = 0;
            for (int i = 0; i < displayNameList.size(); i++) {
                cellIndex = startVaCell + i;
                Cell cellTitle = rowTitle.createCell(cellIndex, CellType.STRING);
                if (cellIndex.equals(startVaCell)) {
                    //第一个单元格的时候
                    cellTitle.setCellValue(variantDisplayName);
                }
                cellTitle.setCellStyle(titleCellStyle);
                Cell cellName = rowName.createCell(cellIndex, CellType.STRING);
                String displayName = displayNameList.get(i);
                cellName.setCellValue(displayName);
                cellName.setCellStyle(title1CellStyle);
                //update by ljr  这个地方可能存在 不同的变形组有同样的变形值，需要将其组成唯一键值 20251124
                colMap.put(cellIndex, variantDisplayName + "{" + displayName + "}");
            }
            count = displayNameList.size();
            //合并pc表头
            if (startVaCell != cellIndex) {
                cellRangeAddress = new CellRangeAddress(startTitle, startTitle, startVaCell, cellIndex); // 参数依次是：开始行，结束行，开始列，结束列
                sheet.addMergedRegion(cellRangeAddress);
            }
        }
        Integer endVaCell = startVaCell + count - 1;
        colMap.put("endVaCell", endVaCell);
        return sheet;
    }

    public static CellStyle setCellStyleTitle(Workbook workbook, int isTitle) {
        //构建单元格样式
        CellStyle style = workbook.createCellStyle();
        //创建字体样式
        Font font = workbook.createFont();
        //设置标题字体颜色
        font.setColor(IndexedColors.BLACK.getIndex());  //字体颜色
        font.setFontHeightInPoints((short) 12);  //字号
        style.setAlignment(HorizontalAlignment.CENTER); // 设置水平居中
        style.setVerticalAlignment(VerticalAlignment.CENTER); // 设置垂直居中
        if (0 == isTitle) {
            font.setBold(true);  //加粗
            //设置背景色
            XSSFColor color = new XSSFColor();
            //根据你需要的rgb值获取byte数组
            color.setRGB(intToByteArray(getIntFromColor(0,176,240)));
            //自定义颜色
            style.setFillForegroundColor(color);
            //必须设置 否则背景色不生效
            style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        } else if (1 == isTitle){
            font.setBold(true);  //加粗
        } else if (3 == isTitle) {
            style.setWrapText(true); // 设置自动换行
        }
        //设置字体
        style.setFont(font);
        //边框
        style.setBorderTop(BorderStyle.THIN); // 上边框
        style.setTopBorderColor(IndexedColors.BLACK.getIndex()); // 上边框颜色
        style.setBorderBottom(BorderStyle.THIN); // 下边框
        style.setBottomBorderColor(IndexedColors.BLACK.getIndex()); // 下边框颜色
        style.setBorderLeft(BorderStyle.THIN); // 左边框
        style.setLeftBorderColor(IndexedColors.BLACK.getIndex()); // 左边框颜色
        style.setBorderRight(BorderStyle.THIN); // 右边框
        style.setRightBorderColor(IndexedColors.BLACK.getIndex()); // 右边框颜色
        return style;
    }


    //rgb转int
    private static int getIntFromColor(int Red, int Green, int Blue){
        Red = (Red << 16) & 0x00FF000;
        Green = (Green << 8) & 0x0000FF00;
        Blue = Blue & 0x00000FF;
        return 0xFF000000 | Red | Green | Blue;
    }

    //int转byte[]
    public static byte[] intToByteArray(int i) {
        byte[] result = new byte[4];
        result[0] = (byte)((i >> 24) & 0xFF);
        result[1] = (byte)((i >> 16) & 0xFF);
        result[2] = (byte)((i >> 8) & 0xFF);
        result[3] = (byte)(i & 0xFF);
        return result;
    }


    /**
    * 获取动态数据列
    * @param
    * @author LIUJR
    * @throws
    * @return java.util.Map<java.lang.String,java.lang.String>
    * @date 2024/12/6 15:40
    * @description
    */
    public Map<Object, Object> setColAttributeMapping(Boolean hasPC){
        Map<Object, Object> attributeMap = new HashMap();
        attributeMap.put(0, "item");
        attributeMap.put(1, SELECT_ATTR_JF_PartNameEN);
        attributeMap.put(2, SELECT_ATTR_JF_PartNameCN);
//        attributeMap.put(3, String.format(STRING_ATTRIBUTE, PLMEntity_V_Name));
        attributeMap.put(3, "attribute[CustomerPartNumber]");
//        attributeMap.put(4, String.format(STRING_ATTRIBUTE, "JF_VPMReference.JF_RevisionIndex"));
        attributeMap.put(4, "attribute[CustomerPartRevision]");
        attributeMap.put(5, SELECT_ATTR_V_PART_NUMBER);
        attributeMap.put(6, DomainConstants.SELECT_REVISION);
        attributeMap.put(7, "Customer Colour");
        attributeMap.put(8, "Jfseat Colour");
        attributeMap.put(9, "Seat ASSY");
        attributeMap.put(10, SELECT_ATTR_JF_PartDes);
        attributeMap.put("startRow", 4);
        attributeMap.put("startPCCell", 11);
        if (hasPC) {
            attributeMap.put("startTitle", 2);
            attributeMap.put("startName", 3);
        } else {
            attributeMap.put("startPCCell", 11);
            attributeMap.put("endPCCell", 10);
            attributeMap.put("startVaCell", 11);
            attributeMap.put("endVaCell", 10);
        }
        return attributeMap;
    }

    /**
    * 获取pc的变形值
    * @param context
	* @param selectPCList
    * @author LIUJR
    * @throws
    * @return java.util.Map
    * @date 2024/12/6 15:15
    * @description
    */
    private Map<String, StringList> getPCVariantValues(Context context, StringList selectPCList) throws Exception{
        Map<String, StringList> map = new HashMap();
        try {
            HashSet hashSet = new HashSet();
            Set set = new HashSet();
            //模型
            for (String pcId : selectPCList) {
                DomainObject pcObject = DomainObject.newInstance(context, pcId);
                StringList infoList = pcObject.getInfoList(context, "to[Product Configuration].from.id");
                infoList.stream().forEach(modeId -> {
                    set.add(modeId);
                });
            }
            StringList selBusList = new StringList();
            selBusList.add(DomainConstants.SELECT_ATTRIBUTE_DISPLAY_NAME);
            selBusList.add(SELECT_ID);
            selBusList.add(SELECT_TYPE);
            selBusList.add("from.to.id");
            selBusList.add("from.to." + DomainConstants.SELECT_ATTRIBUTE_DISPLAY_NAME);
            StringList modeList = StringList.create(set);
            for(String modeId : modeList)  {
                DomainObject modelObj = DomainObject.newInstance(context, modeId);
                MapList groupList = modelObj.getRelatedObjects(context,
                        "Configuration Features",
                        "*",
                        selBusList,
                        new StringList(),
                        false,
                        true,
                        (short) 1,
                        null,
                        null,
                        0);
                Iterator iterator = groupList.iterator();
                while (iterator.hasNext()) {
                    Map map1 = (Map) iterator.next();
                    String name = UIUtil.getValue(map1, DomainConstants.SELECT_ATTRIBUTE_DISPLAY_NAME);
                    if (map.containsKey(name)) {
                        continue;
                    }
                    String type = UIUtil.getValue(map1, SELECT_TYPE);
                    String rel = "Variant Values";
                    if ("Variability Group".equalsIgnoreCase(type)) {
                        rel = "Grouped Variability Criteria";
                    }
                    StringList valueName = new StringList();
                    if (map1.get("from[" + rel  + "].to." + DomainConstants.SELECT_ATTRIBUTE_DISPLAY_NAME).toString().contains("[")) {
                        valueName = (StringList) map1.get("from[" + rel  + "].to." + DomainConstants.SELECT_ATTRIBUTE_DISPLAY_NAME);
                    } else {
                        String value = (String) map1.get("from[" + rel  + "].to." + DomainConstants.SELECT_ATTRIBUTE_DISPLAY_NAME);
                        valueName = StringList.create(value);
                    }
                    map.put(name, valueName);
                }
            };
        } catch (FrameworkException e) {
            e.printStackTrace();
            throw e;
        }
        JF_LOGGER.info("map:{}", map.toString());
        return map;
    }

    /**
    * 获取物理产品pc
    * @param context
	* @param strings
    * @author LIUJR
    * @throws
    * @return java.util.Map
    * @date 2024/12/6 15:11
    * @description
    */
    private Map getVpmPCList(Context context, String[] strings) throws Exception{
        HashMap map = new HashMap();
        try {
            String objectId = strings[0];
            StringList selectPCList = new StringList();
            JF_Config_mxJPO jfConfigMxJPO = new JF_Config_mxJPO();
            HashMap hashMap = new HashMap();
            hashMap.put("objectId", objectId);
            //通过VPMReference获取关联的所有Model以及Model Year下面所有的PC
            MapList allPCList = jfConfigMxJPO.getAllPC(context, JPO.packArgs(hashMap));
            List<Map> newPCList = (List<Map>) allPCList
                    .stream()
                    .filter(m -> (
                            !((Map) m).toString()
                            .contains("disableSelection"))).collect(Collectors.toList());

            //配置车型，将选择的产品配置表达式拿出来，做成一个集合保存
            MapList allPC = new MapList(newPCList);
            selectPCList = (StringList) allPC.stream().map(m -> {
                Map map1 = (Map) m;
                return UIUtil.getValue(map1, SELECT_ID);
            }).collect(Collectors.toCollection(StringList::new));
            map.put("allPC", allPC);
            map.put("selectPCList", selectPCList);
            map.put("allPCList", allPCList);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
        return map;
    }

    /**
    * 获取v5节点
    * @param context
	* @param args
    * @author LIUJR
    * @throws
    * @return java.util.Map
    * @date 2024/12/6 14:17
    * @description
    */
    private Map getCatiaV5Bom(Context context, String[] args) throws Exception{
        HashMap map = new HashMap();
        try{
            ContextUtil.pushContext(context);
            DomainObject domainObject = DomainObject.newInstance(context);
            String wholeId = args[0];
            domainObject.setId(wholeId);
            StringList busList = JF_Util_mxJPO.basicBolistSel();
            busList.add(SELECT_ATTR_V_CADOrigin);
            busList.add(SELECT_ATTR_JFPartType);
            busList.add(SELECT_ATTR_JF_PartNameEN);
            busList.add(SELECT_ATTR_JF_PartNameCN);
            busList.add(String.format(STRING_ATTRIBUTE, PLMEntity_V_Name));
            busList.add(String.format(STRING_ATTRIBUTE, "JF_VPMReference.JF_RevisionIndex"));
            busList.add(SELECT_ATTR_V_PART_NUMBER);
            busList.add(SELECT_ATTR_JF_PartDes);
            // add by chenyan 添加客户零件号和客户零件版本
            busList.add("attribute[JF_VPMReference.JF_CustomerPartNumber]");
            busList.add("attribute[JF_VPMReference.JF_CustomerPartRevision]");
            busList.add(DomainConstants.SELECT_DESCRIPTION);
            StringList relList = JF_Util_mxJPO.basicRellistSel();
            relList.add("from." + SELECT_ATTR_V_CADOrigin);
            relList.add("from." + SELECT_ID);
            relList.add("from." + DomainRelationship.SELECT_PHYSICAL_ID);
            relList.add("from." + SELECT_ATTR_JFPartType);
            relList.add(DomainRelationship.SELECT_ID);
            relList.add(DomainRelationship.SELECT_PHYSICAL_ID);
            MapList allVpmList = domainObject.getRelatedObjects(
                    context,
                    REL_Instance,
                    TYPE_VPMReference,
                    busList,
                    relList,
                    false,
                    true,
                    (short) 0,
                    null,
                    null,
                    0
            );
            //分组
            Map groupMap = (Map) allVpmList.stream().collect(Collectors.groupingBy(m -> {
                        Map info = (Map) m;
                        return info.get(SELECT_ATTR_V_CADOrigin);
                    })
            );
            MapList v5MapList = new MapList();
            MapList v5ConnMapList = new MapList();
            if (groupMap.containsKey("CATIAV5")) {
                String fromCADOrigin, fromId, toId, connId;
                List infoList = (List) groupMap.get("CATIAV5");
                for (int i = 0; i < infoList.size(); i++) {
                    Map temp = (Map) infoList.get(i);
                    fromCADOrigin = UIUtil.getValue(temp, "from." + SELECT_ATTR_V_CADOrigin);
                    fromId = UIUtil.getValue(temp, "from." + SELECT_ID);
                    toId = UIUtil.getValue(temp, SELECT_ID);
                    connId = UIUtil.getValue(temp, DomainRelationship.SELECT_ID);
                    if ("".equalsIgnoreCase(fromCADOrigin)) {
                        //表示是v5节点  保存
                        v5MapList.add(temp);
                        HashMap hashMap = new HashMap();
                        hashMap.put("fromId", fromId);
                        hashMap.put("id", toId);
                        hashMap.put("connId", connId);
                        v5ConnMapList.add(hashMap);
                    }
                }
            }
            map.put("v5MapList", v5MapList);
            map.put("v5ConnMapList", v5ConnMapList);
        }catch (Exception e) {
            e.printStackTrace();
        } finally {
            ContextUtil.popContext(context);
        }
        return map;
    }

    /**
     *
     *@description 导出指定的超级BOM
     *@param context
     *@param strObjectId 根节点ID
     *@param workbook 工作簿
     *@param type 专业
     *@return org.apache.poi.ss.usermodel.Workbook
     *@throws
     *@author CHENYAN
     *@date 2024/12/16 10:51
     */
    public  Workbook exportEBOMByPartId(Context context ,String strObjectId, Workbook workbook, String type, MapList makeTableMapList ,Map<String,StringList>ebomFilterPCPartIdMap) throws Exception {
        DomainObject bo = DomainObject.newInstance(context, strObjectId);
        StringList typeSelectList = JF_Util_mxJPO.basicBolistSel();
        typeSelectList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_V_PART_NUMBER);
        typeSelectList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JF_PartNameCN);
        typeSelectList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JF_PartNameEN);
        typeSelectList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_SurfaceTreatment);
        typeSelectList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JF_Material);
        typeSelectList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JFLength);
        typeSelectList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JFWidth);
        typeSelectList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JF_WeightTarget);
        typeSelectList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JFHeight);
        typeSelectList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JF_Weight);
        typeSelectList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JF_ProcurementType);
        typeSelectList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JFDIRECT_BUY);
        typeSelectList.add("to[JFProject2RootPart|attribute[JF_BelongPart]==Y].from.description");
//        typeSelectList.add("attribute[JF_VPMReference.JF_CustomerPartNumber]");
//        typeSelectList.add("attribute[JF_VPMReference.JF_CustomerPartRevision]");
        typeSelectList.add(SELECT_ATTR_CustomerPartNumber);
        typeSelectList.add(SELECT_ATTR_CustomerPartRevision);
        typeSelectList.add("attribute[PLMEntity.V_description]");
        typeSelectList.add(SELECT_ATTR_JFPartType);
        typeSelectList.add("from[Reference Document|to.attribute[JF_DocumentType]==Drawing].attribute[Title]");
        typeSelectList.add("from[Reference Document|to.attribute[JF_DocumentType]==Drawing].revision");
        StringList relSelectList = JF_Util_mxJPO.basicRellistSel();
        relSelectList.add(SELECT_FROM_ID);
        relSelectList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JF_Dosage);
        relSelectList.add("to.attribute[XCADExtension.V_CADOrigin]");
        relSelectList.add("from.attribute[XCADExtension.V_CADOrigin]");
        //是否是单配还是超配
        String strIsOneConfig = bo.getInfo(context, "attribute[XCADExtension.V_CADOrigin]");
        //单配超配标识
        boolean isOneConfig = "CATIAV5".equals(strIsOneConfig);
        MapList maps = bo.getRelatedObjects(context, VPMInstance , // relationship pattern
                "VPMReference",                                    // object pattern
                typeSelectList,                            // object selects
                relSelectList, // relationship selects
                false,                                        // to direction
                true,                                        // from direction
                (short) 0,                                    // recursion level
                "",                // object where clause
                "",
                (short) 0);
        JF_LOGGER.info("maps:{}",maps.size());
        JF_LOGGER.info("isOneConfig:{}",isOneConfig);
        MapList filterDataList = maps;
        //排除掉配置层下面的结构 from端  是V5 排除
//        MapList filterDataList = (MapList) maps.stream().filter(m ->{
//            Map partInfoMap = (Map)m;
//            String strFromV5V6Flag = (String) partInfoMap.get("from.attribute[XCADExtension.V_CADOrigin]");
//            return  !("CATIAV5".equalsIgnoreCase(strFromV5V6Flag)) ;
//        }).collect(Collectors.toCollection(MapList::new));
//        filterDataList.addSortKey(SELECT_LEVEL, ProgramCentralConstants.ASCENDING_SORT, ProgramCentralConstants.SORTTYPE_INTEGER);
//        filterDataList.sort();
        DomainObject part = DomainObject.newInstance(context);
        Map packMap = new HashMap<>();
        packMap.put("objectId",strObjectId);
//        StringList partIdList = (StringList) filterDataList.stream().map(m->{
//            Map info = (Map) m;
//            return  info.get(SELECT_ID);
//        }).collect(Collectors.toCollection(StringList::new));
//        //下载图片最开始下载 防止图片下载异步导致有路径没有文件
//        Map picturePathMap = JF_PublicMethodClass_mxJPO.downloadVPMReferenceImage(context, partIdList.toStringArray());
        //获取xml表格首页映射
        MapList EBOMMappingList = JF_PublicMethodClass_mxJPO.getJFCostChangeHistoryTemplateMapping(context, "EBOM_2");
        Sheet sheet = workbook.getSheetAt(2); // 获取工作表
        //初始化表头
        initConfigurationHeadInfo(workbook,sheet,makeTableMapList,EBOMMappingList);
        //获取表头最大列便于设置边框
        Row threeRow = sheet.getRow(3);
        short lastCellNum = threeRow.getLastCellNum();
        //创建单元格 style
        CellStyle basicCellStyle = setCellStyleTitle(workbook, 2);
        int writeRowIndex = 5 ;
        Map<String, Object> sameBomeMap = new HashMap<>();
        int level = Integer.MAX_VALUE;
        MapList result = new MapList();
        //获取PC关联的GC并放到表格集合
        //合并
        for (int i = 0; i < filterDataList.size(); i++) {
            Map partInfoMap = (Map) filterDataList.get(i);
            String strChildId = (String) partInfoMap.get(SELECT_ID);
            String strLevel = (String) partInfoMap.get(SELECT_LEVEL);
            String strFromId = (String) partInfoMap.get(SELECT_FROM_ID);
            String strPartType = (String) partInfoMap.get(SELECT_ATTR_JFPartType);
            String strChildDosage = (String) partInfoMap.get(SELECT_ATTR_JF_Dosage);
            int childLevel = Integer.parseInt((String) partInfoMap.get(SELECT_LEVEL));
            if (childLevel > level) {
                //childLevel大于level的,说明该层的父级已经重复，不用合并用量
                continue;
            }
            if (!isOneConfig){
                //排除掉超级BOM的整椅
                if ("1".equals(strLevel)){
                    if ("C".equals(strPartType)){
                        continue;
                    }
                }
            }
            level = Integer.MAX_VALUE;
            StringBuilder sb = new StringBuilder();
            sb.append(strFromId);
            sb.append(strChildId);
            String strUniqueKey = sb.toString();
            if (!sameBomeMap.containsKey(strUniqueKey)) {
                sameBomeMap.put(strUniqueKey, partInfoMap);
                /*********************************** 原来逻辑 begin 2025/06/05 chenyan ************************************************/
                // 判断是配置层之下还是配置层之上
//                String strFromV5V6Flag = (String) partInfoMap.get("from.attribute[XCADExtension.V_CADOrigin]");
//                boolean isConfigFlag = true;
//                if ("CATIAV5".equalsIgnoreCase(strFromV5V6Flag)){
//                    isConfigFlag = false;
//                    for (int i1 = 0; i1 < filterDataList.size(); i1++) {
//                        Map fMap = (Map) filterDataList.get(i1);
//                        if (strFromId.equals(fMap.get(SELECT_ID))){
//                            isConfigFlag = (Boolean) fMap.get("isConfigFlag");
//                            //在配置层之下的层级保存配置id
//                            if (fMap.containsKey("configId")) {
//                                partInfoMap.put("configId",fMap.get("configId"));
//                            }else {
//                                partInfoMap.put("configId",strFromId);
//                            }
//                            break;
//                        }
//                    }
//                }
//                partInfoMap.put("isConfigFlag",isConfigFlag);
                /*********************************** 原来逻辑 begin 2025/06/05 chenyan ************************************************/

                result.add(partInfoMap);
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
                double dosage = Double.parseDouble(dosageStr) + Double.parseDouble(strChildDosage);
                bommap.put(SELECT_ATTR_JF_Dosage, String.valueOf(dosage));
            }
        }
        Set uniqueIdSet = new HashSet<String>();
        Map<String,Map> configMap = new HashMap<>();
        for (int i = 0; i < makeTableMapList.size(); i++) {
            Map PCMap = (Map) makeTableMapList.get(i);
            String strGCIds = (String) PCMap.get(JF_PLMConstants_mxJPO.SELECT_PC_CONNECTION_PC_ID);
            if (UIUtil.isNotNullAndNotEmpty(strGCIds)){
                String[] split = strGCIds.split(",");
                DomainObject gc = DomainObject.newInstance(context);
                for (int i1 = 0; i1 < split.length; i1++) {
                    String strGCId = split[i1];
                    if (!uniqueIdSet.contains(strGCId)){
                        gc.setId(strGCId);
                        Map gcMap = gc.getInfo(context, typeSelectList);
                        gcMap.put(SELECT_LEVEL,"1");
                        // 注释于 2025/06/05 所有层级都需要合并 chenyan
//                        gcMap.put("isConfigFlag",false);
                        gcMap.put(SELECT_ATTR_JF_Dosage, "1");
                        //标识单配整椅GC
                        gcMap.put("oneConfigGC", PCMap.get(SELECT_ID));
                        result.add(0,gcMap);
                        uniqueIdSet.add(strGCId);
                    }
                }
            }
        }
        //清理唯一标识 确保EBOM里面数据唯一
        uniqueIdSet.clear();
        level = Integer.MAX_VALUE ;
        String strPrePath = JF_PublicMethodClass_mxJPO.getBasicUrl(context, new String[]{"DownloadVPM.Img.Path"});
        for (int i = 0; i < result.size(); i++) {
            Map partInfoMap = (Map) result.get(i);
            partInfoMap.put("ITEM",String.valueOf(i+1));
            String strPartId = (String) partInfoMap.get(SELECT_ID);
            if (uniqueIdSet.contains(strPartId)){
                continue;
            }
            uniqueIdSet.add(strPartId);
            String strLevel = (String) partInfoMap.get(SELECT_LEVEL);
            // add by chenyan 实际重量为空时去参考重量 2025/07/30
            String strWeightTarget = (String) partInfoMap.get(JF_PLMConstants_mxJPO.SELECT_ATTR_JF_WeightTarget);
            if (UIUtil.isNullOrEmpty(strWeightTarget)){
                partInfoMap.put(SELECT_ATTR_JF_WeightTarget,partInfoMap.get(SELECT_ATTR_JF_Weight));
            }
            //实际层级减1
            Integer realLevel = Integer.valueOf(strLevel) -1 ;
            String strRealLevel = realLevel.toString();
            partInfoMap.put(JF_PublicMethodClass_mxJPO.buildStringInStrings(SELECT_LEVEL,"_",strRealLevel),strRealLevel);
            part.setId(strPartId);
            String strPartPhyId = part.getInfo(context,DomainRelationship.SELECT_PHYSICAL_ID);
            String strImgId = getDownLoadPictureByPartId(context, strPartPhyId, strPrePath);
            partInfoMap.put("picture",strImgId);
            //没有取号时设置为name
            if (UIUtil.isNullOrEmpty((String) partInfoMap.get(JF_PLMConstants_mxJPO.SELECT_ATTR_V_PART_NUMBER))){
                partInfoMap.put(JF_PLMConstants_mxJPO.SELECT_ATTR_V_PART_NUMBER,UIUtil.getValue(partInfoMap,SELECT_NAME));
            }
            //获取图纸信息
            Map drawInfoMap = getDrawInfoByPart(context, part,partInfoMap);
            //将图纸信息添加到数据集合中
            partInfoMap.putAll(drawInfoMap);
            //获取PC 数量
            Map<String, String> pcUsageNum = getPCUsageNum(makeTableMapList, ebomFilterPCPartIdMap, strPartPhyId);
            configMap.put(strPartId,pcUsageNum);
            //将PC数量添加到数据集合中
            partInfoMap.putAll(pcUsageNum);
            partInfoMap.put("PCDescription","");
            writeDataInExcelRow(context,workbook,sheet,writeRowIndex,EBOMMappingList,partInfoMap,basicCellStyle,lastCellNum,isOneConfig,configMap);
            //每行设置边框
            writeRowIndex ++;
        }
        return workbook;
    }
    /**
     *
     *@description 将图片插入到excel的单元格中
     *@param workbook  excel 对象
     *@param sheet sheet 对象
     *@param strFilePath 图片全路径
     *@param row 行
     *@param col 列
     *@param scaleX 偏移x
     *@param scaleY 偏离y
     *@return void
     *@throws
     *@author CHENYAN
     *@date 2024/12/10 13:40
     */
    public static void insertPictureInCell(org.apache.poi.ss.usermodel.Workbook workbook, Sheet sheet, String strFilePath, int row, int col, double scaleX, double scaleY) throws Exception {
        // 输入流
        InputStream fis = null;
        try {
            java.io.File picFile = new java.io.File(strFilePath);
            //文件不存在跳过直接返回
            if (!picFile.exists()){
                JF_LOGGER.warn("插入图片失败，文件为空");
                return;
            }
            fis = new FileInputStream(picFile);
            byte[] bytes = IOUtils.toByteArray(fis);
            int iPicType = -1 ;
            String strPicType = strFilePath.substring(strFilePath.lastIndexOf("."));
            switch (strPicType){
                case "png" :{
                    iPicType = Workbook.PICTURE_TYPE_PNG;
                }
                case "jpeg" :{
                    iPicType = Workbook.PICTURE_TYPE_JPEG;
                }
                default:{
                    iPicType = Workbook.PICTURE_TYPE_PNG;
                }
            }
            int pictureIdx = workbook.addPicture(bytes, iPicType);
            CreationHelper helper = workbook.getCreationHelper();
            Drawing drawing = sheet.createDrawingPatriarch();
            ClientAnchor anchor = helper.createClientAnchor();
            //设置为 MOVE_AND_RESIZE 意味着图片将会随着单元格的移动和调整大小而相应地移动和调整其大小
            anchor.setAnchorType(ClientAnchor.AnchorType.MOVE_AND_RESIZE);
            // 左上角列索引
            anchor.setCol1(col);
            // 左上角行索引
            anchor.setRow1(row);
            // 右下角列索引
            anchor.setCol2(col);
            // 右下角行索引
            anchor.setRow2(row);
            // 右下角相对于左上角单元格右边界的偏移量（EMU）
            anchor.setDx2(Units.toEMU(scaleX)); //dx = left + wanted width
            // 右下角相对于左上角单元格右边界的偏移量（EMU）
            anchor.setDy2(Units.toEMU(scaleY)); //dy= top + wanted height
            // 插入图片
            Picture pict = drawing.createPicture(anchor, pictureIdx);
//            pict.resize(scaleX, scaleY);
        } catch (Exception e) {
            throw e;
        }finally {
            if (fis != null){
                try {
                    fis.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    /**
     *
     *@description 初始化配置描述头信息
     *@param sheet sheet
     *@param dataList 配置数据
     *@return void
     *@throws
     *@author CHENYAN
     *@date 2024/12/10 14:09
     */
    public static void initConfigurationHeadInfoSecond(Workbook workbook,Sheet sheet,MapList dataList,MapList EBOMMappingList){
        //获取最大列
        int lastColIndex = 0;
        for (Row row : sheet) {
            if (row != null && row.getLastCellNum() > lastColIndex) {
                lastColIndex = row.getLastCellNum();
            }
        }
        Map PCDesIndexMap = getPDDescriptionIndexByXmlMapping(EBOMMappingList);
        JF_LOGGER.info("PCDesIndexMap:{}",PCDesIndexMap);
        Object valueObj = PCDesIndexMap.get("value");
        int iPCDesIndex = 0;
        if (valueObj instanceof Integer) {
            iPCDesIndex = (Integer) valueObj;
        } else if (valueObj instanceof String) {
            iPCDesIndex = Integer.parseInt((String) valueObj);
        }
        JF_LOGGER.info("iPCDesIndex:{}",iPCDesIndex);
        //需要扩展的列
        int iAddCol = -1;
        boolean isAddCol = false ;
        if (dataList != null){
            if (dataList.size() > 1){
                isAddCol = true;
            }
            //除去本身
            iAddCol = dataList.size() - 1 ;
        }


        if (isAddCol){
            sheet.shiftColumns(iPCDesIndex+1, iPCDesIndex + 1, iAddCol);
            CellRangeAddress cellRangeAddress = new CellRangeAddress(0, 0, iPCDesIndex, iPCDesIndex+iAddCol); // 参数依次是：开始行，结束行，开始列，结束列
            sheet.addMergedRegion(cellRangeAddress);
//            //add by 陈彦 新增折叠PC列 2025/01/08
//            sheet.groupColumn(iPCDesIndex, iPCDesIndex+iAddCol);
//            //默认展开2个
//            sheet.setRowGroupCollapsed(2, true);
        }

        Row rowTwo = sheet.getRow(1);
        Row rowThree = sheet.getRow(2);
        Row rowFour = sheet.getRow(3);
        Row rowFive = sheet.getRow(4);
        JF_LOGGER.info("需要新增的列总数:{}",iAddCol);
        for (int i = 0; i <= iAddCol; i++) {
            Map dataMap = (Map) dataList.get(i);
            String strPCName = (String) dataMap.get(JF_PLMConstants_mxJPO.select_attr_Marketing_Name);
            //PC 关联 EBOM
            String strPCEBOM = (String) dataMap.get(JF_PLMConstants_mxJPO.SELECT_PC_CONNECTION_PC);

            String strObjectId = (String) dataMap.get(SELECT_ID);
            int iRealColIndex = i + iPCDesIndex ;
            Cell twoDestCell = null;
            Cell threeDestCell = null;
            Cell fourDestCell = null;
            Cell fiveDestCell = null;
            if (i == 0 ){
                twoDestCell = rowTwo.getCell(iRealColIndex);
                threeDestCell = rowThree.getCell(iRealColIndex);
                PCDesIndexMap.put("objectId",strObjectId);
            }else {
                twoDestCell = rowTwo.createCell(iRealColIndex);
                threeDestCell = rowThree.createCell(iRealColIndex);
                fourDestCell = rowFour.createCell(iRealColIndex);
                fiveDestCell = rowFive.createCell(iRealColIndex);
                Map addPCDesIndexMap = new HashMap<>();
                addPCDesIndexMap.putAll(PCDesIndexMap);
                PCDesIndexMap.put("objectId",strObjectId);
                PCDesIndexMap.put("value",String.valueOf(iRealColIndex));
                EBOMMappingList.add(addPCDesIndexMap);
            }
            //第一个时在原cell上操作
            if (i == 0){
                if (twoDestCell == null) {
                    twoDestCell = rowTwo.createCell(iRealColIndex);
                }
                twoDestCell.setCellValue(strPCName);
                if (threeDestCell == null) {
                    threeDestCell = rowThree.createCell(iRealColIndex);
                }
                threeDestCell.setCellValue(strPCEBOM);
            }else {
                copyCell(rowTwo.getCell(iPCDesIndex),twoDestCell,workbook,false,strPCName);
                copyCell(rowThree.getCell(iPCDesIndex),threeDestCell,workbook,false,strPCEBOM);
                copyCell(rowFour.getCell(iPCDesIndex),fourDestCell,workbook,true,"");
                copyCell(rowFive.getCell(iPCDesIndex),fiveDestCell,workbook,true,"");
            }

        }
    }

    /**
     *
     *@description 初始化配置描述头信息
     *@param sheet sheet
     *@param dataList 配置数据
     *@return void
     *@throws
     *@author CHENYAN
     *@date 2024/12/10 14:09
     */
    public static void initConfigurationHeadInfo(Workbook workbook,Sheet sheet,MapList dataList,MapList EBOMMappingList){
        //获取最大列
        int lastColIndex = 0;
        for (Row row : sheet) {
            if (row != null && row.getLastCellNum() > lastColIndex) {
                lastColIndex = row.getLastCellNum();
            }
        }
        Map PCDesIndexMap = getPDDescriptionIndexByXmlMapping(EBOMMappingList);
        int iPCDesIndex = Integer.parseInt((String) PCDesIndexMap.get("value"));
        JF_LOGGER.info("iPCDesIndex:{}",iPCDesIndex);
        //需要扩展的列
        int iAddCol = -1;
        boolean isAddCol = false ;
        if (dataList != null){
            if (dataList.size() > 1){
                isAddCol = true;
            }
            //除去本身
            iAddCol = dataList.size() - 1 ;
        }


        if (isAddCol){
            sheet.shiftColumns(iPCDesIndex+1, lastColIndex, iAddCol);
            CellRangeAddress cellRangeAddress = new CellRangeAddress(0, 0, iPCDesIndex, iPCDesIndex+iAddCol); // 参数依次是：开始行，结束行，开始列，结束列
            sheet.addMergedRegion(cellRangeAddress);
//            //add by 陈彦 新增折叠PC列 2025/01/08
//            sheet.groupColumn(iPCDesIndex, iPCDesIndex+iAddCol);
//            //默认展开2个
//            sheet.setRowGroupCollapsed(2, true);
        }

        Row rowTwo = sheet.getRow(1);
        Row rowThree = sheet.getRow(2);
        Row rowFour = sheet.getRow(3);
        Row rowFive = sheet.getRow(4);
        JF_LOGGER.info("需要新增的列总数:{}",iAddCol);
        for (int i = 0; i <= iAddCol; i++) {
            Map dataMap = (Map) dataList.get(i);
            String strPCName = (String) dataMap.get(JF_PLMConstants_mxJPO.select_attr_Marketing_Name);
            //PC 关联 EBOM
            String strPCEBOM = (String) dataMap.get(JF_PLMConstants_mxJPO.SELECT_PC_CONNECTION_PC);

            String strObjectId = (String) dataMap.get(SELECT_ID);
            int iRealColIndex = i + iPCDesIndex ;
            Cell twoDestCell = null;
            Cell threeDestCell = null;
            Cell fourDestCell = null;
            Cell fiveDestCell = null;
            if (i == 0 ){
                twoDestCell = rowTwo.getCell(iRealColIndex);
                threeDestCell = rowThree.getCell(iRealColIndex);
                PCDesIndexMap.put("objectId",strObjectId);
            }else {
                twoDestCell = rowTwo.createCell(iRealColIndex);
                threeDestCell = rowThree.createCell(iRealColIndex);
                fourDestCell = rowFour.createCell(iRealColIndex);
                fiveDestCell = rowFive.createCell(iRealColIndex);
                Map addPCDesIndexMap = new HashMap<>();
                addPCDesIndexMap.putAll(PCDesIndexMap);
                PCDesIndexMap.put("objectId",strObjectId);
                PCDesIndexMap.put("value",String.valueOf(iRealColIndex));
                EBOMMappingList.add(addPCDesIndexMap);
            }
            //第一个时在原cell上操作
            if (i == 0){
                twoDestCell.setCellValue(strPCName);
                threeDestCell.setCellValue(strPCEBOM);
            }else {
                copyCell(rowTwo.getCell(iPCDesIndex),twoDestCell,workbook,false,strPCName);
                copyCell(rowThree.getCell(iPCDesIndex),threeDestCell,workbook,false,strPCEBOM);
                copyCell(rowFour.getCell(iPCDesIndex),fourDestCell,workbook,true,"");
                copyCell(rowFive.getCell(iPCDesIndex),fiveDestCell,workbook,true,"");
            }

        }
    }


    /**
    * 移除与指定区域重叠的所有合并区域
    * @param sheet
	* @param newRegion
    * @author LIUJR
    * @throws
    * @return void
    * @date 2025/11/13 14:50
    * @description
    */
    public static void removeOverlappingMergedRegions(Sheet sheet, CellRangeAddress newRegion) {
        List<CellRangeAddress> mergedRegions = sheet.getMergedRegions();
        // 从后往前遍历，避免删除时索引错乱
        for (int i = mergedRegions.size() - 1; i >= 0; i--) {
            CellRangeAddress existing = mergedRegions.get(i);
            if (existing.intersects(newRegion)) {
                sheet.removeMergedRegion(i);
            }
        }
    }

    /**
    * 初始化配置描述头信息
    * @param context
	* @param sheet
	* @param styleList
	* @param EBOMMappingList
    * @author LIUJR
    * @throws
    * @return void
    * @date 2025/11/13 10:17
    * @description
    */
    public static void initColorStyleHeadInfo(Workbook workbook, Context context,Sheet sheet,StringList styleList,MapList EBOMMappingList, Map<String, Object> EBOMConfMap) throws Exception {
        try {
            //获取最大列
            int lastColIndex =(Integer)EBOMConfMap.get("lastColIndex");
            //获取客户颜色码和内部颜色码的index
            Map EBOMMappingMap = JF_NewECRService_mxJPO.getMapListGroupingMap(context, EBOMMappingList, SELECT_ID);
            List JF_CustormColorCode = (List) EBOMMappingMap.get("JF_CustormColorCode");
            List JF_InternalColorCode = (List) EBOMMappingMap.get("JF_InternalColorCode");
            Map custormColorCodeMap = (Map) JF_CustormColorCode.get(0);
            Map internalColorCodeMap = (Map) JF_InternalColorCode.get(0);
            int custormColorStartIndex = Integer.parseInt((String) custormColorCodeMap.get("start"));
            int custormColorEndIndex = Integer.parseInt((String) custormColorCodeMap.get("end"));
            int internalColorStartIndex = Integer.parseInt((String) internalColorCodeMap.get("start"));
            int internalColorEndIndex = Integer.parseInt((String) internalColorCodeMap.get("end"));
            //备份
            int custormStyleIndex = custormColorStartIndex;
            int internalStyleIndex = internalColorStartIndex;
            //需要扩展的列  原始有3列
            int iAddCol = 0;
            Integer colorNum = (Integer)EBOMConfMap.get("colorNum");
            if (styleList != null){
                if (styleList.size() > colorNum){
                    //除去本身
                    iAddCol = styleList.size() - colorNum ;
                }
            }
            IntegerList str16List = new IntegerList();
            IntegerList str20List = new IntegerList();
            IntegerList str40List = new IntegerList();
            String strLen20 = (String) EBOMConfMap.get("Len20");
            String strLen40 = (String) EBOMConfMap.get("Len40");
            if (styleList == null) {
                for (int i = 0; i < EBOMMappingList.size(); i++) {
                    if (i < 24) {
                        continue;
                    }
                    Map map = (Map) EBOMMappingList.get(i);
                    String value = (String) map.get("value");
                    String id = (String) map.get("id");
                    int index = Integer.parseInt(value.trim());
                    if (strLen20.contains(id)) {
                        str20List.add(index);
                    } else if (strLen40.contains(id)) {
                        str40List.add(index);
                    } else {
                        str16List.add(index);
                    }
                }
                EBOMConfMap.put("lastColIndex", lastColIndex - 1);
                EBOMConfMap.put("str16List", str16List);
                EBOMConfMap.put("str20List", str20List);
                EBOMConfMap.put("str40List", str40List);
                return;
            }
            //获取标题行
            int titleRow = (Integer)EBOMConfMap.get("titleIndex");
            Row rowFour = sheet.getRow(titleRow);
            //获取风格 后续给增加的列单元格设置
            Cell colorCell = rowFour.getCell(custormColorStartIndex);
            CellStyle newStyle = workbook.createCellStyle(); // 创建新样式，避免修改原样式
            // 复制所有属性
            newStyle.cloneStyleFrom(colorCell.getCellStyle()); // 这是核心！
            newStyle.setBorderTop(BorderStyle.THIN);
            newStyle.setBorderBottom(BorderStyle.THIN);
            newStyle.setBorderLeft(BorderStyle.THIN);
            newStyle.setBorderRight(BorderStyle.THIN);
            newStyle.setAlignment(HorizontalAlignment.CENTER); // 水平居中
            newStyle.setVerticalAlignment(VerticalAlignment.CENTER); // 垂直居中
            //关键点1： 客户颜色码 新增
            if (iAddCol > 0) {
                // 1. 先执行列移动
                sheet.shiftColumns(custormColorEndIndex + 1, lastColIndex, iAddCol);   //将第 开始列（p1） 列及其右侧的所有列(最后一列 p2)向右移动 n(新增列 p3) 列；
                //列数发生变化
                custormColorEndIndex += iAddCol;
                internalColorStartIndex += iAddCol;
                internalColorEndIndex += iAddCol;
                lastColIndex += iAddCol; //最后一列需要增加
                //关键点2 ： 内部颜色码 新增
                // 1. 先执行列移动
                sheet.shiftColumns(internalColorEndIndex + 1, lastColIndex, iAddCol);
                //列数发生变化
                internalColorEndIndex += iAddCol;
                lastColIndex += iAddCol; //最后一列需要增加
                //合并

                CellRangeAddress newRegion = new CellRangeAddress(titleRow, titleRow, custormColorStartIndex, custormColorEndIndex - 1);
                // 2. 【关键】移除所有与 newRegion 重叠的已有合并区域
                 removeOverlappingMergedRegions(sheet, newRegion);
               // 3. 添加新的合并区域
                newRegion = new CellRangeAddress(titleRow, titleRow, custormColorStartIndex, custormColorEndIndex);
                sheet.addMergedRegion(newRegion);
                //合并
                CellRangeAddress newRegion1 = new CellRangeAddress(titleRow, titleRow, internalColorStartIndex, internalColorEndIndex -1);
                // 2. 【关键】移除所有与 newRegion 重叠的已有合并区域
                removeOverlappingMergedRegions(sheet, newRegion1);
                // 3. 添加新的合并区域
                newRegion1 = new CellRangeAddress(titleRow, titleRow, internalColorStartIndex, internalColorEndIndex);
                sheet.addMergedRegion(newRegion1);
                colorCell = rowFour.getCell(custormColorStartIndex);
                colorCell.setCellStyle(newStyle);
                colorCell = rowFour.getCell(internalColorStartIndex);
                colorCell.setCellStyle(newStyle);
            }
            int titleNameIndexRow = (Integer)EBOMConfMap.get("titleNameIndex");
            Row rowFive = sheet.getRow(titleNameIndexRow);
            Cell custormColorCell = null;
            Cell internalColorCell = null;
            custormColorCell = rowFive.getCell(custormColorStartIndex);
            //保存样式
            CellStyle cellStyle = custormColorCell.getCellStyle();
            //开始写入颜色风格
            for (int i = 0; i < styleList.size(); i++) {
                String style = (String) styleList.get(i);
                custormColorCell = rowFive.getCell(custormColorStartIndex + i);
                if (custormColorCell == null) {
                    custormColorCell = rowFive.createCell(custormColorStartIndex + i);
                }
                custormColorCell.setCellValue(style);
                custormColorCell.setCellStyle(cellStyle);
                internalColorCell = rowFive.getCell(internalColorStartIndex + i);
                if (internalColorCell == null) {
                    internalColorCell = rowFive.createCell(internalColorStartIndex + i);
                }
                internalColorCell.setCellValue(style);
                internalColorCell.setCellStyle(cellStyle);
            }
            //修改写入行MapList中的行数
            List<Integer> list;
            for (int i = 0; i < EBOMMappingList.size(); i++) {
                Map map = (Map) EBOMMappingList.get(i);
                String value = (String) map.get("value");
                String id = (String) map.get("id");
                int num = Integer.parseInt(value.trim());
                if (num < (Integer)EBOMConfMap.get("NoChangeIndex")) {
                    continue;
                }
                if ("JF_CustormColorCode".equalsIgnoreCase(id)) {
                    String start = (String) map.get("start");
                    String end = (String) map.get("end");
                    map.put("end", custormColorEndIndex);
                    list = IntStream.rangeClosed(custormColorStartIndex, custormColorEndIndex)
                            .boxed()
                            .collect(Collectors.toList());
                    str16List.addAll(list);
                } else if ("JF_InternalColorCode".equalsIgnoreCase(id)) {
                    String start = (String) map.get("start");
                    String end = (String) map.get("end");
                    map.put("start", internalColorStartIndex);
                    map.put("value", internalColorStartIndex);
                    map.put("end", internalColorEndIndex);
                    list = IntStream.rangeClosed(internalColorStartIndex, internalColorEndIndex)
                            .boxed()
                            .collect(Collectors.toList());
                    str16List.addAll(list);
                } else {
                    int index = Integer.parseInt(value.trim()) + (iAddCol * 2);
                    map.put("value", index);
                    if (strLen20.contains(id)) {
                        str20List.add(index);
                    } else if (strLen40.contains(id)) {
                        str40List.add(index);
                    } else {
                        str16List.add(index);
                    }
                }
            }
            EBOMConfMap.put("lastColIndex", lastColIndex);
            EBOMConfMap.put("str16List", str16List);
            EBOMConfMap.put("str20List", str20List);
            EBOMConfMap.put("str40List", str40List);
        } catch (Exception e) {
            JF_LOGGER.info(e.getMessage());
            throw e;
        }
    }


    /**
     *
     *@description 复制单元格
     *@param srcCell 源单元格
     *@param destCell 目标单元格
     *@param workbook 工作薄
     *@param isCopyValue 是否复制源单元格值
     *@param strNewValue 覆盖单元格值
     *@return void
     *@throws
     *@author CHENYAN
     *@date 2024/12/10 16:26
     */
    private static void copyCell(Cell srcCell, Cell destCell, Workbook workbook ,boolean isCopyValue,String strNewValue) {
        // 复制单元格的值
        if (isCopyValue){
            // 设置单元格类型
            destCell.setCellType(srcCell.getCellType());
            // 复制单元格的值
            switch (srcCell.getCellType()) {
                case STRING:
                    destCell.setCellValue(srcCell.getStringCellValue());
                    break;
                case NUMERIC:
                    destCell.setCellValue(srcCell.getNumericCellValue());
                    break;
                case BOOLEAN:
                    destCell.setCellValue(srcCell.getBooleanCellValue());
                    break;
                case FORMULA:
                    destCell.setCellFormula(srcCell.getCellFormula());
                    break;
                case BLANK:
                    // 不需要设置任何值
                    break;
                case ERROR:
                    destCell.setCellErrorValue(srcCell.getErrorCellValue());
                    break;
                default:
                    break;
            }
        }else {
            if (UIUtil.isNotNullAndNotEmpty(strNewValue)){
                destCell.setCellType(CellType.STRING);
                destCell.setCellValue(strNewValue);
            }
        }
        // 复制单元格样式
        if (srcCell.getCellStyle() != null) {
            CellStyle newCellStyle = workbook.createCellStyle();
            newCellStyle.cloneStyleFrom(srcCell.getCellStyle());
            destCell.setCellStyle(newCellStyle);
        }

    }
    /**
     *
     *@description 获取配置描述的excel 下标
     *@param xmlMapList
     *@return int
     *@throws
     *@author CHENYAN
     *@date 2024/12/11 16:42
     */
    public static Map getPDDescriptionIndexByXmlMapping(MapList xmlMapList){
        MapList indexMapList = (MapList)xmlMapList.stream().filter(m ->{
            Map xmlMap = (Map)m;
            String strXmlId = (String) xmlMap.get("id");
            return "PCDescription".equalsIgnoreCase(strXmlId);
        }).collect(Collectors.toCollection(MapList::new));
        return (Map) indexMapList.get(0);
    }

    /**
     *
     *@description 获取零件关联的图纸信息
     *@param context
     *@param part
     *@return java.util.Map
     *@throws
     *@author CHENYAN
     *@date 2024/12/12 14:28
     */

    /**
     *
     *@description 获取零件关联的图纸信息
     *@param context
     *@param part
     *@return java.util.Map
     *@throws
     *@author CHENYAN
     *@date 2024/12/12 14:28
     */

    public Map getDrawInfoByPart(Context context ,DomainObject part,Map partMap) throws Exception{
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
        Map res = new HashMap<String,Set<String>>();
        StringList drawNameSet = new StringList();
        StringList drawRevisionSet = new StringList();
        for (int i = 0; i < maps.size(); i++) {
            Map drawMap = (Map) maps.get(i);
            String strDrawName = (String) drawMap.get("attribute[PLMEntity.V_Name]");
            strDrawName = UIUtil.isNullOrEmpty(strDrawName) ? (String) drawMap.get(SELECT_NAME) : strDrawName;
            String strDrawRevision = (String) drawMap.get(SELECT_REVISION);
            drawNameSet.add(strDrawName);
            drawRevisionSet.add(strDrawRevision);
        }
        if (partMap.containsKey("from[Reference Document].to.attribute[Title]")){
            Object oDocName = partMap.get("from[Reference Document].to.attribute[Title]");
            JF_LOGGER.info("oDocName:{}",oDocName);
            if (oDocName instanceof String){
                drawNameSet.add((String)oDocName);
            }else if (oDocName instanceof StringList){
                StringList docNameList = (StringList)oDocName ;
                drawNameSet.addAll(docNameList);
            }
        }
        if (partMap.containsKey("from[Reference Document].to.revision")){
            Object oDocRevision = partMap.get("from[Reference Document].to.revision");
            JF_LOGGER.info("oDocRevision:{}",oDocRevision);

            if (oDocRevision instanceof String){
                drawRevisionSet.add((String)oDocRevision);
            }else if (oDocRevision instanceof StringList){
                StringList docRevisionList = (StringList)oDocRevision ;
                drawRevisionSet.addAll(docRevisionList);
            }
        }
        res.put("drawName",drawNameSet.join("\n"));
        res.put("drawRevision",drawRevisionSet.join("\n"));
        return res ;
    }
    /**
     *
     *@description 获取零件关联的图纸信息
     *@param context
     *@param part
     *@return java.util.Map
     *@throws
     *@author CHENYAN
     *@date 2024/12/12 14:28
     */

    public Map getDrawInfoByPartSecond(Context context ,DomainObject part,Map partMap) throws Exception{
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
        Map res = new HashMap<String,Set<String>>();
        StringList drawNameList = new StringList();
        StringList drawRevisionList = new StringList();
        StringList drawCurrentList = new StringList();
        for (int i = 0; i < maps.size(); i++) {
            Map drawMap = (Map) maps.get(i);
            String strDrawName = (String) drawMap.get("attribute[PLMEntity.V_Name]");
            strDrawName = UIUtil.isNullOrEmpty(strDrawName) ? (String) drawMap.get(SELECT_NAME) : strDrawName;
            String strDrawRevision = (String) drawMap.get(SELECT_REVISION);
            String strDrawCurrent = (String) drawMap.get(SELECT_CURRENT);
            drawNameList.add(strDrawName);
            drawRevisionList.add(strDrawRevision);
            drawCurrentList.add(EnoviaResourceBundle.getStateI18NString(context, "VPLM_SMB_Definition_MajorRev", strDrawCurrent, context.getLocale().toString()));
        }
        //文档图纸
        MapList mapList = part.getRelatedObjects(context, "Reference Document" , // relationship pattern
                "Document",                                    // object pattern
                StringList.create(SELECT_ATTRIBUTE_TITLE, SELECT_CURRENT, SELECT_REVISION),                            // object selects
                JF_Util_mxJPO.basicRellistSel(), // relationship selects
                false,                                        // to direction
                true,                                        // from direction
                (short) 1,                                    // recursion level
                "attribute[JF_DocumentType]==Drawing",                // object where clause
                "",
                (short) 0);
        JF_LOGGER.info("docmapList:{}", mapList);
        for (int i = 0; i < mapList.size(); i++) {
            Map drawMap = (Map) mapList.get(i);
            String title = UIUtil.getValue(drawMap, SELECT_ATTRIBUTE_TITLE);
            String current = UIUtil.getValue(drawMap, SELECT_CURRENT);
            String revision = UIUtil.getValue(drawMap, SELECT_REVISION);
            drawNameList.add(title);
            drawRevisionList.add(revision);
            drawCurrentList.add(EnoviaResourceBundle.getStateI18NString(context, "Document Release", current, context.getLocale().toString()));
        }
        res.put("drawName",drawNameList.join("\n"));
        res.put("drawRevision",drawRevisionList.join("\n"));
        res.put("drawCurrent",drawCurrentList.join("\n"));
        return res ;
    }
    /**
     *
     *@description 获取指定零件在EBOM中的配置数量
     *@param pcList PC集合
     *@param strUsagePartId 判断数量零件 id
     *@return java.util.Map<java.lang.String,java.lang.Long>
     *@throws
     *@author CHENYAN
     *@date 2024/12/12 14:48
     */
    public Map<String,String> getPCUsageNum(MapList pcList,Map<String, StringList> ebomFilterPCPartIdMap,String strUsagePartId) throws Exception{
        Map res = new HashMap<String,String>();
        for (int i = 0; i < pcList.size(); i++) {
            Map PCMap = (Map) pcList.get(i);
            String strPCId = (String) PCMap.get(SELECT_ID);
            if (ebomFilterPCPartIdMap.containsKey(strPCId)) {
                StringList usageParIdList = ebomFilterPCPartIdMap.get(strPCId);
                    //配置数量
                    long countNum = usageParIdList.stream().filter(strPartId -> {
                        return strPartId.equals(strUsagePartId);
                    }).count();
                    res.put(strPCId,String.valueOf(countNum));
            }else{
                res.put(strPCId,"");
            }
            // 不包含flag 标识请求成功
        }
        return res;
    }
    /**
    *
    *@description 获取EBOM下PC过滤零件
    *@param context
	*@param pcList
	*@param strRootPartId
	*@param jf_config
    *@return com.matrixone.apps.domain.util.MapList
    *@throws
    *@author CHENYAN
    *@date 2024/12/13 14:14
    */
    public  Map<String,StringList> getEBOMFilterPCPartId(Context context ,MapList pcList,String strRootPartId,JF_Config_mxJPO jf_config) throws Exception{
        Map<String,StringList> res = new HashMap();
        DomainObject PC = DomainObject.newInstance(context);
        for (int i = 0; i < pcList.size(); i++) {
            Map PCMap = (Map) pcList.get(i);
            String strPCId = (String) PCMap.get(SELECT_ID);
            PC.setId(strPCId);
            String strPCPhyId = PC.getInfo(context, DomainRelationship.SELECT_PHYSICAL_ID);
            //结算单车BOM
            Map<String, StringList> bom = jf_config.getBOM(context, strRootPartId, strPCPhyId);
            // 不包含flag 标识请求成功
            if (!bom.containsKey("flag")){
                StringList strings = bom.get("vpmPhycialIdList");
                res.put(strPCId,strings);
            }
        }
        return res;
    }
    /**
     *
     *@description 将零件Map数据写入excel
     *@param workbook
     *@param sheet
     *@param iRowIndex
     *@param EBOMMappingList
     *@param dataMap
     *@return void
     *@throws
     *@author CHENYAN
     *@date 2024/12/13 9:59
     */
    public void writeDataInExcelRow(Context context,Workbook workbook,Sheet sheet,int iRowIndex ,MapList EBOMMappingList ,Map dataMap,CellStyle basicCellStyle,short lastCellNum,boolean isOneConfig,Map<String,Map> configMap) throws Exception{
        Row row = sheet.createRow(iRowIndex);
        for (int i = 0; i < EBOMMappingList.size(); i++) {
            Map mappingMap = (Map) EBOMMappingList.get(i);
            String strKey = (String)mappingMap.get("id");
            String strColIndex = (String)mappingMap.get("value");
            if (dataMap.containsKey(strKey)){
                Cell cell = null ;
                //添加图片
                if ("picture".equals(strKey)){
                    String strImgPath = (String) dataMap.get(strKey);
                    if (UIUtil.isNotNullAndNotEmpty(strImgPath)){
                        insertPictureInCell(workbook, sheet, strImgPath, iRowIndex, Integer.parseInt(strColIndex), 100, 50);
                    }
                    //设置边框
                    cell = row.createCell(Integer.parseInt(strColIndex));
                    //根据PC ID 匹配到指定列
                }else if ("PCDescription".equals(strKey)){
                    String strCellValue  = "";
                    cell = row.createCell(Integer.parseInt(strColIndex));
                    String strPCId = (String) mappingMap.get("objectId");
                    //多单椅EBOM导出指定key
                    if (configMap.containsKey("EBOMExport")){
                        String strDosage = (String) dataMap.get(strPCId);
                        strCellValue = strDosage;
                    } else if (dataMap.containsKey("oneConfigGC")) {
                        String strDataId = (String) dataMap.get("oneConfigGC");
                        strCellValue = "0";
                        if (strPCId.equals(strDataId)){
                            strCellValue = "1";
                        }
                    }else if (UIUtil.isNotNullAndNotEmpty(strPCId) && dataMap.containsKey(strPCId)){
                        /*********************************** 原来逻辑 begin  2025/06/05 chenyan ************************************************/
//                        Boolean isConfigFlag = (Boolean)dataMap.get("isConfigFlag");
//                        strCellValue =  (String)dataMap.get(strPCId);
//                        //除配置层都使用用量
//                        if ((!isConfigFlag)){
//                            String strDosage = (String) dataMap.get(SELECT_ATTR_JF_Dosage);
//                            strCellValue = strDosage;
//                        }else {
//                            String strConfigId = (String) dataMap.get("configId");
//                            if (UIUtil.isNotNullAndNotEmpty(strConfigId)){
//                                if (configMap.containsKey(strConfigId)) {
//                                    Map configCountGCMap = configMap.get(strConfigId);
//                                    String strCount = (String) configCountGCMap.get(strPCId);
//                                    if (UIUtil.isNullOrEmpty(strCount) || "0".equals(strCount)){
//                                        strCellValue = "0";
//                                    }else {
//                                        String strDosage = (String) dataMap.get(SELECT_ATTR_JF_Dosage);
//                                        strCellValue = strDosage;
//                                    }
//                                }
//                            }
//                        }
                        /*********************************** 原来逻辑 end 2025/06/05 chenyan ************************************************/
                        /*********************************** 新逻辑 begin 2025/06/05 chenyan ************************************************/
                        strCellValue =  (String)dataMap.get(strPCId);
                        if (UIUtil.isNotNullAndNotEmpty(strCellValue) && (!"0".equals(strCellValue))){
                            String strDosage = (String) dataMap.get(SELECT_ATTR_JF_Dosage);
                            strCellValue = strDosage;
                        }
                        /*********************************** 新逻辑 end 2025/06/05 chenyan ************************************************/


                        //当不存在配置的时候
                    }else if (UIUtil.isNullOrEmpty(strPCId)){
                        //单配直接用量
                        if (isOneConfig){
                            strCellValue = (String) dataMap.get(SELECT_ATTR_JF_Dosage);
                        }else {
                            /*********************************** 原来逻辑 begin 2025/06/05 chenyan ************************************************/
//                            Boolean isConfigFlag = (Boolean)dataMap.get("isConfigFlag");
//                            strCellValue =  (String)dataMap.get(strPCId);
//                            //除配置层都使用用量
//                            if ((!isConfigFlag)){
//                                String strDosage = (String) dataMap.get(SELECT_ATTR_JF_Dosage);
//                                strCellValue = strDosage;
//                            }
                            /*********************************** 原来逻辑 begin 2025/06/05 chenyan ************************************************/


                            /*********************************** 新逻辑 begin 2025/06/05 chenyan ************************************************/
                            strCellValue =  (String)dataMap.get(strPCId);
                            if (UIUtil.isNotNullAndNotEmpty(strCellValue) && (!"0".equals(strCellValue))){
                                String strDosage = (String) dataMap.get(SELECT_ATTR_JF_Dosage);
                                strCellValue = strDosage;
                            }
                            /*********************************** 新逻辑 end 2025/06/05 chenyan ************************************************/
                        }
                    }
                    strCellValue = UIUtil.isNullOrEmpty(strCellValue) ? "0" : strCellValue;
                    if (JF_PublicMethodClass_mxJPO.isNumeric(strCellValue)) {
                        double dValue = Double.parseDouble(strCellValue);
                        cell.setCellValue(dValue);
                    }else {
                        cell.setCellValue(strCellValue);
                    }
//                    cell.setCellValue(strCellValue);

                }else if (SELECT_ATTR_JFPartType.equals(strKey)) {
                    String strValue = (String) dataMap.get(strKey);
                    //格式化处理
                    String strFormat = (String) mappingMap.get("format");
                    if (UIUtil.isNotNullAndNotEmpty(strFormat)) {
                        String strNlsKey = (String) mappingMap.get("nlsKey");
                        if ("range".equals(strFormat)) {
                            if (UIUtil.isNotNullAndNotEmpty(strValue)) {
                                strValue = EnoviaResourceBundle.getRangeI18NString(context, strNlsKey, strValue, context.getLocale().getLanguage());
                            }
                        }
                    }
                    cell = row.createCell(Integer.parseInt(strColIndex));
                    cell.setCellValue(strValue);

                }else {
//                    JF_LOGGER.info("strKey:{}",strKey);
//                    JF_LOGGER.info("dataMap:{}",dataMap);
//                    String strValue = (String) dataMap.get(strKey);
                    Object strValueObj =  dataMap.get(strKey);
                    String strValue = "";
                    if(strValueObj instanceof StringList){
                        strValue =((StringList) strValueObj).get(0);
                    }else{
                        strValue=(String) dataMap.get(strKey);
                    }
                    cell = row.createCell(Integer.parseInt(strColIndex));
                    if (JF_PublicMethodClass_mxJPO.isNumeric(strValue)) {
                        double dValue = Double.parseDouble(strValue);
                        cell.setCellValue(dValue);
                    }else {
                        //格式化处理
                        String strFormat = (String) mappingMap.get("format");
                        if (UIUtil.isNotNullAndNotEmpty(strFormat)) {
                            String strNlsKey = (String) mappingMap.get("nlsKey");
                            if ("range".equals(strFormat)) {
                                if (UIUtil.isNotNullAndNotEmpty(strValue)) {
                                    strValue = EnoviaResourceBundle.getRangeI18NString(context, strNlsKey, strValue, context.getLocale().getLanguage());
                                }
                            }
                        }
                        cell.setCellValue(strValue);
                    }
                }

            }
        }
        if (lastCellNum != -1){
            for (int i = 0; i < lastCellNum ;i++){
                Cell cell = row.getCell(i);
                if (cell == null){
                    cell = row.createCell(i);
                }
                cell.setCellStyle(basicCellStyle);
            }
        }
    }

    /**
     *
     *@description 将零件Map数据写入excel
     *@param workbook
     *@param sheet
     *@param iRowIndex
     *@param EBOMMappingList
     *@param dataMap
     *@return void
     *@throws
     *@author CHENYAN
     *@date 2024/12/13 9:59
     */
    public void writeDataInExcelRowSecond(Context context,Workbook workbook,Sheet sheet,int iRowIndex ,MapList EBOMMappingList ,Map dataMap,CellStyle basicCellStyle,short lastCellNum,boolean isOneConfig,Map<String,Map> configMap, StringList styleList, CellStyle wrapTextCellStyle, CellStyle redStyle, CellStyle leftStyle) throws Exception{
        try {
            Row row = sheet.createRow(iRowIndex);
            Integer start = 0;
            Integer iRed = 0;
            Boolean redFlag = Boolean.FALSE;
            Integer classAttribute = 0;
            for (int i = 0; i < EBOMMappingList.size(); i++) {
                Map mappingMap = (Map) EBOMMappingList.get(i);
                String strKey = (String)mappingMap.get("id");
                Integer strColIndex = 0;
                Object valueObj = mappingMap.get("value");
                if (valueObj instanceof Integer) {
                    strColIndex = (Integer) valueObj;
                } else if (valueObj instanceof String) {
                    strColIndex = Integer.parseInt((String) valueObj);
                }
                Cell cell = row.createCell(strColIndex);
                String level = UIUtil.getValue(dataMap, SELECT_LEVEL);
                level = UIUtil.getValue(dataMap, SELECT_LEVEL + "_" + level);
                if ("attribute[CustomerPartNumber]".equalsIgnoreCase(strKey) && ("-1".equalsIgnoreCase(level) || "0".equalsIgnoreCase(level))) {
                    if (UIUtil.isNullOrEmpty(UIUtil.getValue(dataMap, "attribute[CustomerPartNumber]"))) {
                        redFlag = Boolean.TRUE;
                        iRed = strColIndex;
                    }
                }
                if (dataMap.containsKey(strKey)){
                    //添加图片
                    if ("picture".equals(strKey)){
                        String strImgPath = (String) dataMap.get(strKey);
                        if (UIUtil.isNotNullAndNotEmpty(strImgPath)){
                            insertPictureInCell(workbook, sheet, strImgPath, iRowIndex, strColIndex, 100, 50);
                        }
                        //根据PC ID 匹配到指定列
                    }else
                        if ("PCDescription".equals(strKey)){
                        String strCellValue  = "";
                        cell = row.createCell(strColIndex);
                        String strPCId = (String) mappingMap.get("objectId");
                        //多单椅EBOM导出指定key
                        if (configMap.containsKey("EBOMExport")){
                            String strDosage = (String) dataMap.get(strPCId);
                            strCellValue = strDosage;
                        } else if (dataMap.containsKey("oneConfigGC")) {
                            String strDataId = (String) dataMap.get("oneConfigGC");
                            strCellValue = "0";
                            if (strPCId.equals(strDataId)){
                                strCellValue = "1";
                            }
                        }else if (UIUtil.isNotNullAndNotEmpty(strPCId) && dataMap.containsKey(strPCId)){
                            strCellValue =  (String)dataMap.get(strPCId);
                            if (UIUtil.isNotNullAndNotEmpty(strCellValue) && (!"0".equals(strCellValue))){
                                String strDosage = (String) dataMap.get(SELECT_ATTR_JF_Dosage);
                                strCellValue = strDosage;
                            }
                            //当不存在配置的时候
                        }else if (UIUtil.isNullOrEmpty(strPCId)){
                            //单配直接用量
                            if (isOneConfig){
                                strCellValue = (String) dataMap.get(SELECT_ATTR_JF_Dosage);
                            }else {
                                strCellValue =  (String)dataMap.get(strPCId);
                                if (UIUtil.isNotNullAndNotEmpty(strCellValue) && (!"0".equals(strCellValue))){
                                    String strDosage = (String) dataMap.get(SELECT_ATTR_JF_Dosage);
                                    strCellValue = strDosage;
                                }
                            }
                        }
                        strCellValue = UIUtil.isNullOrEmpty(strCellValue) ? "0" : strCellValue;
                        if (JF_PublicMethodClass_mxJPO.isNumeric(strCellValue)) {
                            double dValue = Double.parseDouble(strCellValue);
                            cell.setCellValue(dValue);
                        }else {
                            cell.setCellValue(strCellValue);
                        }
                    }else if (SELECT_ATTR_JFPartType.equals(strKey)) {
                        String strValue = (String) dataMap.get(strKey);
                        //格式化处理
                        String strFormat = (String) mappingMap.get("format");
                        if (UIUtil.isNotNullAndNotEmpty(strFormat)) {
                            String strNlsKey = (String) mappingMap.get("nlsKey");
                            if ("range".equals(strFormat)) {
                                if (UIUtil.isNotNullAndNotEmpty(strValue)) {
                                    strValue = EnoviaResourceBundle.getRangeI18NString(context, strNlsKey, strValue, context.getLocale().getLanguage());
                                }
                            }
                        }
                        cell = row.createCell(strColIndex);
                        cell.setCellValue(strValue);
                    }else if ("JF_CustormColorCode".equalsIgnoreCase(strKey) || "JF_InternalColorCode".equalsIgnoreCase(strKey)){
                        Object startObj = mappingMap.get("start");
                        if (startObj instanceof Integer) {
                            start = (Integer) startObj;
                        } else if (startObj instanceof String) {
                            start = Integer.parseInt((String) startObj);
                        }
                        Map styleMap = (Map) dataMap.get(strKey);
                        for (int i1 = 0; i1 < styleList.size(); i1++) {
                            String style = styleList.get(i1);
                            String styleCode = (String) styleMap.get(style);
                            cell = row.createCell(start + i1);
                            cell.setCellValue(styleCode);
                        }
                    } else if ("classAttribute".equalsIgnoreCase(strKey)) {
                            classAttribute = strColIndex;
                            cell = row.createCell(strColIndex);
                            String strValue=(String) dataMap.get(strKey);
                            cell.setCellValue(strValue);
                    } else {
                        Object strValueObj =  dataMap.get(strKey);
                        String strValue = "";
                        if(strValueObj instanceof StringList){
                            strValue =((StringList) strValueObj).get(0);
                        }else{
                            strValue=(String) dataMap.get(strKey);
                        }
                        cell = row.createCell(strColIndex);
                        if (JF_PublicMethodClass_mxJPO.isNumeric(strValue)) {
                            double dValue = Double.parseDouble(strValue);
                            cell.setCellValue(dValue);
                        }else {
                            //格式化处理
                            String strFormat = (String) mappingMap.get("format");
                            if (UIUtil.isNotNullAndNotEmpty(strFormat)) {
                                String strNlsKey = (String) mappingMap.get("nlsKey");
                                if ("range".equals(strFormat)) {
                                    if (UIUtil.isNotNullAndNotEmpty(strValue)) {
                                        strValue = EnoviaResourceBundle.getRangeI18NString(context, strNlsKey, strValue, context.getLocale().getLanguage());
                                    }
                                }
                            }
                            cell.setCellValue(strValue);
                        }
                    }
                }
            }
            if (lastCellNum != -1){
                for (int i = 0; i < lastCellNum ;i++){
                    Cell cell = row.getCell(i);
                    if (cell == null){
                        cell = row.createCell(i);
                    }
                    if (i == iRed && redFlag) {
                        cell.setCellStyle(redStyle);
                    } else if(i == classAttribute) {
                        cell.setCellStyle(leftStyle);
                    } else if (i >= 16 && i <= 18) {
                        cell.setCellStyle(wrapTextCellStyle);
                    } else {
                        cell.setCellStyle(basicCellStyle);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }



    /**
    *
    *@description 获取指定根节点零件下所有的缩略图
    *@param context
	*@param rootPart
    *@return java.util.Map
    *@throws
    *@author CHENYAN
    *@date 2025/1/9 13:30
    */
    public Map downLoadPictureByRootPartId(Context context ,DomainObject rootPart) throws Exception{
        MapList maps = rootPart.getRelatedObjects(context, VPMInstance , // relationship pattern
                "VPMReference",                                    // object pattern
                JF_Util_mxJPO.basicBolistSel(),                            // object selects
                JF_Util_mxJPO.basicRellistSel(), // relationship selects
                false,                                        // to direction
                true,                                        // from direction
                (short) 0,                                    // recursion level
                "",                // object where clause
                "",
                (short) 0);
        StringList partIdList = (StringList) maps.stream().map(m->{
            Map info = (Map) m;
            return  info.get(SELECT_ID);
        }).collect(Collectors.toCollection(StringList::new));
        //下载图片最开始下载 防止图片下载异步导致有路径没有文件
        Map picturePathMap = JF_PublicMethodClass_mxJPO.downloadVPMReferenceImage(context, partIdList.toStringArray());
        return picturePathMap;
    }
    public Map getParentMapAndGetHavePC(String strBeginId ,String strEndId ,MapList dataMapList){
        for (int i = 0; i < dataMapList.size(); i++) {
            Map dataMap = (Map) dataMapList.get(i);
            String strId = (String)dataMap.get(SELECT_ID);
            String strHasExistPC = (String)dataMap.get("attribute[PLMReference.V_hasConfigContext]");
            if (strBeginId.equals(strId)){

            }
        }
        return null;
    }
    public  String getDownLoadPictureByPartId(Context context ,String strPartPhysicalid,String strPrePath) throws Exception{
        String path = DomainConstants.EMPTY_STRING;
        Map<String, String> map = new HashMap<>();
        String strImageId = JF_PublicMethodClass_mxJPO.findObject(context, "PLMDerivedObjRepresentation", "name=='" + strPartPhysicalid + "'");
        if (UIUtil.isNotNullAndNotEmpty(strImageId)) {
            DomainObject imageObject = DomainObject.newInstance(context, strImageId);
            String strImgPhysicalid = imageObject.getInfo(context, "physicalid");
            path = JF_PublicMethodClass_mxJPO.buildStringInStrings(strPrePath, java.io.File.separator, strImgPhysicalid, "_''.jpeg");
        }
        return  path;
    }


    /*
     * @description:导入缩略图  查询工作中的GC、GX类型数据 全展开下载
     * @author: caipan
     * @date: 2025/1/17 09:37:27
     * @param: * @param[1] context
     * @param[2] args
     * @return:
     **/
    public void downLoadThumbnail(Context context,String[] args) throws Exception{
        JF_LOGGER.info("downLoadThumbnail start");
     /*   String where = "current=='IN_WORK' && (attribute[JF_VPMReference.JF_PartType]==C || attribute[JF_VPMReference.JF_PartType]==X) ";
        MapList list =  DomainObject.findObjects(context, JF_PLMConstants_mxJPO.TYPE_VPMReference, "*", where,JF_Util_mxJPO.basicBolistSel());
      */
        StringList sel = new StringList();
        sel.add(SELECT_ID);

        StringList rel = new StringList();
        rel.add(SELECT_RELATIONSHIP_ID);
        MapList list =getSupplyPartList(context,new String[]{"current=='IN_WORK'"});
        DomainObject domainObject = DomainObject.newInstance(context );
        Map temp,subtemp;
        StringList idlist  = new StringList();
        for(int i=0;i<list.size();i++) {
            temp = (Map) list.get(i);
            domainObject.setId(UIUtil.getValue(temp, SELECT_ID));
            idlist.add(UIUtil.getValue(temp, SELECT_ID));
            MapList allVpmList = domainObject.getRelatedObjects(
                    context,
                    REL_Instance,
                    TYPE_VPMReference,
                    sel,
                    rel,
                    false,
                    true,
                    (short) 0,
                    null,
                    null,
                    0
            );
            //下载缩略图
            for(int j=0;j<allVpmList.size();j++){
                subtemp= (Map)allVpmList.get(j);
                String id = UIUtil.getValue(subtemp, SELECT_ID);
                if(!idlist.contains(id)){
                    idlist.add(id);
                }
            }
        }
        //下载图片最开始下载 防止图片下载异步导致有路径没有文件
        JF_LOGGER.info("idlist:{}",idlist.size());
        Map picturePathMap = JF_PublicMethodClass_mxJPO.downloadVPMReferenceImage(context, idlist.toStringArray());
        JF_LOGGER.info("downLoadThumbnail end");
    }

    /*
     * @description:第一次执行一遍所有冻结和发布状态的
     * @author: caipan
     * @date: 2025/1/17 11:26:13
     * @param: * @param[1] context
     * @param[2] args
     * @return:
     **/
    public void downLoadThumbnailRelease(Context context,String[] args) throws Exception{
        JF_LOGGER.info("downLoadThumbnail release start");
        String where = "(current=='FROZEN' || current=='RELEASED') && (attribute[JF_VPMReference.JF_PartType]==C || attribute[JF_VPMReference.JF_PartType]==X) ";
//        MapList list =  DomainObject.findObjects(context, JF_PLMConstants_mxJPO.TYPE_VPMReference, "*", where,JF_Util_mxJPO.basicBolistSel());
        MapList list =getSupplyPartList(context,new String[]{"current=='FROZEN' || current=='RELEASED'"});
        StringList sel = new StringList();
        sel.add(SELECT_ID);

        StringList rel = new StringList();
        rel.add(SELECT_RELATIONSHIP_ID);
        DomainObject domainObject = DomainObject.newInstance(context );
        Map temp,subtemp;
        StringList idlist  = new StringList();
        for(int i=0;i<list.size();i++) {
            temp = (Map) list.get(i);
            domainObject.setId(UIUtil.getValue(temp, SELECT_ID));
            idlist.add(UIUtil.getValue(temp, SELECT_ID));
            MapList allVpmList = domainObject.getRelatedObjects(
                    context,
                    REL_Instance,
                    TYPE_VPMReference,
                    sel,
                    rel,
                    false,
                    true,
                    (short) 0,
                    null,
                    null,
                    0
            );
            //下载缩略图
            for(int j=0;j<allVpmList.size();j++){
                subtemp= (Map)allVpmList.get(j);
                String id = UIUtil.getValue(subtemp, SELECT_ID);
                if(!idlist.contains(id)){
                    idlist.add(id);
                }
            }
        }
        //下载图片最开始下载 防止图片下载异步导致有路径没有文件
        JF_LOGGER.info("idlist:{}",idlist.size());
        Map picturePathMap = JF_PublicMethodClass_mxJPO.downloadVPMReferenceImage(context, idlist.toStringArray());
        JF_LOGGER.info("downLoadThumbnail end");
    }
    /*
     * @description:获取供货件清单
     * @author: caipan
     * @date: 2026/2/4 17:14:12
     * @param: * @param[1] context
     * @param[2] args args[0] 关系查询语句
     * @return:
     **/
    public MapList getSupplyPartList(Context context,String[] args) throws Exception{
        String str = args[0];
        StringList selList = new StringList();
        selList.add(SELECT_ID);
        StringList relList = new StringList();
        relList.add(SELECT_RELATIONSHIP_ID);
        relList.add("attribute[JFZeroPart]");
        MapList mlProject = DomainObject.findObjects(context, "Project Space", // type filter
                null, // vault filter
                "", // where clause
                selList);
        DomainObject obj = DomainObject.newInstance(context );
        String where = str;
        MapList resultMapList = new MapList();
        for(int i=0;i<mlProject.size();i++){
            Map pMap = (Map)mlProject.get(i);
            String id = UIUtil.getValue(pMap, SELECT_ID);
            obj.setId(id);
            MapList partMapList = obj.getRelatedObjects(context,
                    JF_PLMConstants_mxJPO.rel_JFProject2RootPart, //pattern to match relationships
                    JF_PLMConstants_mxJPO.TYPE_VPMReference, //pattern to match types
                    selList, //the eMatrix StringList object that holds the list of select statement pertaining to Business Obejcts.
                    relList, //the eMatrix StringList object that holds the list of select statement pertaining to Relationships.
                    false, //get To relationships
                    true, //get From relationships
                    (short) 1, //the number of levels to expand, 0 equals expand all.
                    where, //where clause to apply to objects, can be empty ""
                    "attribute[JFZeroPart]==Y", //where clause to apply to relationship, can be empty ""
                    (short) 0);//limit
            resultMapList.addAll(partMapList);
        }
        return resultMapList;
    }
}
