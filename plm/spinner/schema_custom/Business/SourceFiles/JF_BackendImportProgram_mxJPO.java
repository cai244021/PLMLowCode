import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.util.ContextUtil;
import com.matrixone.apps.domain.util.FrameworkUtil;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.util.PropertyUtil;
import com.matrixone.apps.framework.ui.UICache;
import com.matrixone.apps.framework.ui.UIUtil;
import matrix.db.Context;
import matrix.db.Policy;
import matrix.util.StringList;
import org.antlr.v4.runtime.misc.IntegerList;
import org.apache.poi.ss.usermodel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

/**
 * @ClassName JF_BackendImportProgram
 * @Author: LIUJR
 * @CreateDate: 2025/2/17 11:24
 * @UpdateRemark:
 * @Version: 1.0
 * @Description:  后台导入程序
 */
public class JF_BackendImportProgram_mxJPO {

    private static final Logger JF_LOGGER = LoggerFactory.getLogger(JF_BackendImportProgram_mxJPO.class);

    /**
    * 导入成本库制费库程序
    * @param context
	* @param args
    * @author LIUJR
    * @throws
    * @return void
    * @date 2025/2/17 11:30
    * @description
    */
    public void importCostAndManufactureFee(Context context, String[] args) throws Exception{
        try {
            ContextUtil.startTransaction(context, true);
            String importType = args[0];
            String strFilePath = args[1];
            java.io.File file = new File(strFilePath);
            if (!file.exists()) {
                JF_LOGGER.info("======================================文件不存在，请核对文件位置");
                return;
            }
            Map<String, IntegerList> checkColMap = new HashMap<>();
            int startRow = 0;
            int startCell = 0;
            int attrRow = 0;
            int lastCell = 0;
            String strType = DomainConstants.EMPTY_STRING;
            String strPolicy = DomainConstants.EMPTY_STRING;
            //需要校验列
            checkColMap = getCheckRowMap(importType);
            //开始打开文件
            FileInputStream fileInputStream = new FileInputStream(file);
            Workbook workbook = WorkbookFactory.create(fileInputStream);
            Sheet sheet = workbook.getSheetAt(0);
            //拿取行信息
            int iFirstRowNum = sheet.getFirstRowNum();
            int iLastRowNum = sheet.getLastRowNum();
            if ((iFirstRowNum == iLastRowNum) || (iLastRowNum < startRow)){
                JF_LOGGER.info("文件未填写!!!!!!!!!!!");
                return;
            }
            switch (importType) {
                case "Cost" : {
                    //成本库导入
                    startRow = 2;
                    attrRow = 1;
                    startCell = sheet.getRow(attrRow).getFirstCellNum();
                    lastCell = sheet.getRow(attrRow).getLastCellNum();
                    strType = JF_PLMConstants_mxJPO.TYPE_JFCost;
                    strPolicy = JF_PLMConstants_mxJPO.type_JFCost;
                    break;
                }
                case "ManufactureFee" : {
                    //制费库导入
                    startRow = 3;
                    attrRow = 2;
                    startCell = sheet.getRow(attrRow).getFirstCellNum();
                    lastCell = sheet.getRow(attrRow).getLastCellNum();
                    strType = JF_PLMConstants_mxJPO.TYPE_JFManufactureFee;
                    strPolicy = JF_PLMConstants_mxJPO.type_JFManufactureFee;
                    break;
                }
            }
            lastCell -= 1;
            //检查数据
            String checkMess = checkCostAndManufactureFeeCellValue(startRow, iLastRowNum, sheet, checkColMap);
            if (UIUtil.isNotNullAndNotEmpty(checkMess)) {
                JF_LOGGER.info(checkMess);
                return;
            }
            JF_LOGGER.info("startCell：{}", startCell);
            JF_LOGGER.info("lastCell：{}", lastCell);
            //获取属性列
            Map<Integer, String> attributMap = new HashMap<>();
            for (int cell1 = startCell; cell1 <= lastCell; cell1++) {
                String attribute = JF_ExcelUtils_mxJPO.getCellValue(sheet, attrRow, cell1, true);
                attributMap.put(cell1, attribute);
            }
            JF_LOGGER.info("attributMap：{}", attributMap.toString());
            //获取数据集合
            MapList fileAllDataMapList = getFileAllDataMapList(startRow, iLastRowNum, startCell, lastCell, sheet, attributMap);
            JF_LOGGER.info("fileAllDataMapList：{}", fileAllDataMapList.toString());
            //创建数据
            JF_Util_mxJPO jfUtilMxJPO = new JF_Util_mxJPO();
            // 获取组织
            String organization = jfUtilMxJPO.getPersonOrganization(context, JF_PLMConstants_mxJPO.USER_Admin_Platform);
            // 获取项目
            String project = JF_PLMConstants_mxJPO.PROJECT_JFSeat;
            String sProductionVault = PropertyUtil.getSchemaProperty(context, "vault_eServiceProduction");
            StringList newIdList = new StringList();
            for (int i = 0; i < fileAllDataMapList.size(); i++) {
                Map map = (Map) fileAllDataMapList.get(i);
                String sObjGeneratorName = UICache.getObjectGenerator(context, strType, "");
                String sName = DomainObject.getAutoGeneratedName(context, sObjGeneratorName, "");
                Policy policy = new Policy(strPolicy);
                String revision = policy.getFirstInSequence(context);
                String type  = PropertyUtil.getSchemaProperty(context, strType);
                DomainObject object = DomainObject.newInstance(context);
                object.createObject(context, type, sName, revision, strPolicy, sProductionVault);
                String description = (String) map.get(DomainConstants.SELECT_DESCRIPTION);
                // 删除 description 键值对
                map.remove(DomainConstants.SELECT_DESCRIPTION);
                object.setDescription(context,description);
                object.setAttributeValues(context, map);
                // 设置owner
                object.setOwner(context,JF_PLMConstants_mxJPO.USER_Admin_Platform);
                // 设置ECO的协作区和组织
                object.setPrimaryOwnership(context, project, organization);
                newIdList.add(object.getId(context));
            }
            ContextUtil.commitTransaction(context);
            JF_LOGGER.info("newIdList:{}", newIdList.toString());
        }catch (Exception e) {
            ContextUtil.abortTransaction(context);
            e.printStackTrace();
            throw e;
        }
    }


    /**
    * 获取整个文件的数据集合
    * @param startRow
	* @param iLastRowNum
	* @param startCell
	* @param lastCell
	* @param sheet
	* @param attributMap
    * @author LIUJR
    * @throws
    * @return com.matrixone.apps.domain.util.MapList
    * @date 2025/2/18 11:35
    * @description
    */
    public MapList getFileAllDataMapList(int startRow, int iLastRowNum, int startCell, int lastCell, Sheet sheet, Map attributMap) throws Exception{
        MapList mapList = new MapList();
        //检查完后，拿取表格数据
        for (int iRow = startRow; iRow <= iLastRowNum; iRow++) {
            Map map = new HashMap<>();
            for (int iCell = startCell; iCell <= lastCell; iCell++) {
                String cellValue = JF_ExcelUtils_mxJPO.getCellValue(sheet, iRow, iCell, true);
                //判断是否要日期格式转换
                String attribute = (String) attributMap.get(iCell);
                map.put(attribute, cellValue);
            }
            mapList.add(map);
        }
        return mapList;
    }


    /**
    * 上传文件中，格式检查，成本库和制费库目前只有日期格式检查
    * @param startRow
	* @param iLastRowNum
	* @param sheet
	* @param checkColMap
    * @author LIUJR
    * @throws
    * @return java.lang.String
    * @date 2025/2/18 11:12
    * @description
    */
    public String checkCostAndManufactureFeeCellValue(int startRow, int iLastRowNum, Sheet sheet, Map checkColMap) throws Exception{
        //检查数据
        StringBuilder sb = new StringBuilder();
        for (int iRow = startRow; iRow <= iLastRowNum; iRow++) {
            //成本和制费只有日期检查
            if (checkColMap.containsKey("date")) {
                IntegerList integerList = (IntegerList) checkColMap.get("date");
                for (int i = 0; i < integerList.size(); i++) {
                    Integer integer = integerList.get(i);
                    String cellValue = JF_ExcelUtils_mxJPO.getCellValue(sheet, iRow, integer, true);
                    Boolean flag = checkStringIsDateFormat(cellValue);
                    if (!flag)  {
                        //不符合格式，给出提示
                        int row = iRow + 1;
                        sb.append("第" + row + "行，日期格式有误，请符合日期格式MM/dd/yyyy" + "\n");
                    }
                }
            }
        }
        return sb.toString();
    }

    /**
    * 校验日期字符串是否符合MM/dd/yyyy格式
    * @param cellValue
    * @author LIUJR
    * @throws
    * @return java.lang.Boolean
    * @date 2025/2/18 11:08
    * @description
    */
    public static Boolean checkStringIsDateFormat(String cellValue) {
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
        sdf.setLenient(false); // 严格模式，防止无效日期被解
        try {
            sdf.parse(cellValue); // 尝试解析日期
            return true; // 日期格式正确且有效
        } catch (ParseException e) {
            return false; // 日期格式错误或无效
        }
    }

    /**
    * 返回表格中需要校验的lie
    * @param importType
    * @author LIUJR
    * @throws
    * @return java.util.Map<java.lang.String,java.lang.String>
    * @date 2025/2/17 15:43
    * @description
    */
    private Map<String, IntegerList> getCheckRowMap(String importType) {
        HashMap<String, IntegerList> checkRowMap = new HashMap<>();
        IntegerList integerList = new IntegerList();
        switch (importType) {
            case "Cost" : {
                //成本库导入
                integerList.add(5);
                checkRowMap.put("date", integerList);
                break;
            }
            case "ManufactureFee" : {
                //制费库导入
                integerList.add(3);
                integerList.add(7);
                checkRowMap.put("date", integerList);
                break;
            }
        }
        return checkRowMap;
    }


    /**
    * 获取制费的属性Map
    * @author LIUJR
    * @throws
    * @return java.util.Map<java.lang.Integer,java.lang.String>
    * @date 2025/2/17 15:35
    * @description
    */
    public Map<Integer, String> getManufactureFeeColAttributeMap() throws Exception{
        Map<Integer, String> colAttrMap = new HashMap<>();
        colAttrMap.put(0, DomainConstants.ATTRIBUTE_TITLE);
        colAttrMap.put(1, JF_PLMConstants_mxJPO.ATTR_JF_OEMName);
        colAttrMap.put(2, "JF_ProjectState");
        colAttrMap.put(3, "JF_FreeOfferDate");
        colAttrMap.put(4, "JF_ManufactureLocate");
        colAttrMap.put(5, "JF_FreeProject");
        colAttrMap.put(6, "JF_ProductionLine");
        colAttrMap.put(7, "JF_SOPDate");
        colAttrMap.put(8, "JF_Annual");
        colAttrMap.put(9, "JF_FreeLife");
        colAttrMap.put(10, "JF_ProjectYear");
        colAttrMap.put(11, "JF_UPH");
        colAttrMap.put(12, "JF_assemblyTotalPrice");
        colAttrMap.put(13, "JF_assemblyQualityPrice");
        colAttrMap.put(14, "JF_assemblyStartPrice");
        colAttrMap.put(15, "JF_assemblyRDPrice");
        colAttrMap.put(16, "JF_assemblyLogisticsPrice");
        colAttrMap.put(17, "JF_assemblyTimePrice");
        colAttrMap.put(18, "JF_assemblyManufacturingPrice");
        colAttrMap.put(19, "JF_FoamTotalPrice");
        colAttrMap.put(20, "JF_FoamManufacturingPrice");
        colAttrMap.put(21, "JF_TrimTotalPrice");
        colAttrMap.put(22, "JF_TrimTotalTimePrice");
        colAttrMap.put(23, "JF_TrimTotalManufacturingPrice");
        colAttrMap.put(24, "description");
        return colAttrMap;
    }

    /**
    * 获取成本的属性Map
    * @param
    * @author LIUJR
    * @throws
    * @return java.util.Map<java.lang.String,java.lang.String>
    * @date 2025/2/17 13:55
    * @description
    */
    public Map<Integer, String> getCostColAttributeMap() {
        Map<Integer, String> colAttrMap = new HashMap<>();
        colAttrMap.put(0, DomainConstants.ATTRIBUTE_TITLE);
        colAttrMap.put(1, "JF_ProjectNumber");
        colAttrMap.put(2, JF_PLMConstants_mxJPO.ATTR_JF_OEMName);
        colAttrMap.put(3, "JF_ProjectState");
        colAttrMap.put(4, "JF_SeatOrigin");
        colAttrMap.put(5, "JF_SOPDate");
        colAttrMap.put(6, "JF_PredictedYield");
        colAttrMap.put(7, "JF_ProductNumber");
        colAttrMap.put(8, "JF_SeatWeight");
        colAttrMap.put(9, "JF_SalePrice");
        colAttrMap.put(10, "JF_MaterialCost");
        colAttrMap.put(11, "JF_NumberofRows");
        colAttrMap.put(12, "JF_BackAdjust");
        colAttrMap.put(13, "JF_CushionAdjust");
        colAttrMap.put(14, "JF_SeatAdjust");
        colAttrMap.put(15, "JF_FucVentila");
        colAttrMap.put(16, "JF_FucVentilaPrices");
        colAttrMap.put(17, "JF_FucHeating");
        colAttrMap.put(18, "JF_FucHeatingPrices");
        colAttrMap.put(19, "JF_Fucheadrest");
        colAttrMap.put(20, "JF_FucheadrestPrices");
        colAttrMap.put(21, "JF_FucLumbar");
        colAttrMap.put(22, "JF_FucLumbarPrices");
        colAttrMap.put(23, "JF_FucMassage");
        colAttrMap.put(24, "JF_FucMassagePrice");
        colAttrMap.put(25, "JF_FucLegrest");
        colAttrMap.put(26, "JF_FucLegrestPrices");
        colAttrMap.put(27, "JF_FucCoverMaterial");
        colAttrMap.put(28, "JF_FucCoverPunching");
        colAttrMap.put(29, "JF_FucCoverNetarea");
        colAttrMap.put(30, "JF_FucCoverUtilization");
        colAttrMap.put(31, "JF_FucSewingHours");
        colAttrMap.put(32, "JF_FucCoverPatternNB");
        colAttrMap.put(33, "JF_FucCoverSupplyType");
        colAttrMap.put(34, "JF_FucCoverMainPrices");
        colAttrMap.put(35, "JF_FucCoverAxuPrices");
        colAttrMap.put(36, "JF_FucCoverMaterialPrices");
        colAttrMap.put(37, "JF_FucCoverMFGPrices");
        colAttrMap.put(38, "JF_FucCoverTotalPrices");
        colAttrMap.put(39, "JF_FucCoverComments");
        colAttrMap.put(40, "JF_FucFoamMaterial");
        colAttrMap.put(41, "JF_FucFoamSingleWeight");
        colAttrMap.put(42, "JF_FucFoamChemiUPrices");
        colAttrMap.put(43, "JF_FucFoamChemiTotalPrices");
        colAttrMap.put(44, "JF_FucFoamTotalWeight");
        colAttrMap.put(45, "JF_FucFoamMainPrices");
        colAttrMap.put(46, "JF_FucFoamAxuPrices");
        colAttrMap.put(47, "JF_FucFoamMFGPrices");
        colAttrMap.put(48, "JF_FucFoamSupplyType");
        colAttrMap.put(49, "JF_FucFoamTotalPrices");
        colAttrMap.put(50, "JF_FucFoamComments");
        colAttrMap.put(51, "JF_FucFrameWeight");
        colAttrMap.put(52, "JF_FucFrameIsDB");
        colAttrMap.put(53, "JF_FucFrameCorePrices");
        colAttrMap.put(54, "JF_FucFrameTotalPrices");
        colAttrMap.put(55, "JF_FucFrameComments");
        colAttrMap.put(56, "JF_FucOthMetalWeight");
        colAttrMap.put(57, "JF_FucOthMetalPrices");
        colAttrMap.put(58, "JF_FucComPlasticNumber");
        colAttrMap.put(59, "JF_FucComPlasticWeight");
        colAttrMap.put(60, "JF_FucComPlasticPrices");
        colAttrMap.put(61, "JF_FucComPlasticComments");
        colAttrMap.put(62, "JF_FucHarnessLength");
        colAttrMap.put(63, "JF_FucHarnessLoopsNB");
        colAttrMap.put(64, "JF_FucHarnessSpecial");
        colAttrMap.put(65, "JF_FucHarnessPrices");
        colAttrMap.put(66, "JF_FucHarnessRemark");
        colAttrMap.put(67, "JF_FuncEPP");
        colAttrMap.put(68, "JF_FuncEPPPrice");
        colAttrMap.put(69, "JF_FuncTable");
        colAttrMap.put(70, "JF_FuncTablePrice");
        colAttrMap.put(71, "JF_FuncBackrest");
        colAttrMap.put(72, "JF_FuncBackrestPrice");
        colAttrMap.put(73, "JF_FuncPedal");
        colAttrMap.put(74, "JF_FuncPedalPrice");
        colAttrMap.put(75, "JF_FuncContainer");
        colAttrMap.put(76, "JF_FuncContainerPrice");
        colAttrMap.put(77, "JF_FuncSBR");
        colAttrMap.put(78, "JF_FuncSBRPrice");
        colAttrMap.put(79, "JF_FuncSwitch");
        colAttrMap.put(80, "JF_FuncSwitchPrice");
        colAttrMap.put(81, "JF_FuncECU");
        colAttrMap.put(82, "JF_FuncECUPrice");
        colAttrMap.put(83, "JF_FuncGuy");
        colAttrMap.put(84, "JF_FuncGuyPrice");
        colAttrMap.put(85, "JF_FuncCarpet");
        colAttrMap.put(86, "JF_FuncCarpetPrice");
        colAttrMap.put(87, "JF_FuncFaster");
        colAttrMap.put(88, "JF_FuncFasterPrice");
        colAttrMap.put(89, "JF_FuncSmallBag");
        colAttrMap.put(90, "JF_FuncSmallBagPrice");
        colAttrMap.put(91, "JF_FuncDamper");
        colAttrMap.put(92, "JF_FuncDamperPrice");
        colAttrMap.put(93, "JF_FuncSTPS");
        colAttrMap.put(94, "JF_FuncSTPSPrice");
        colAttrMap.put(95, "JF_FuncArmchair");
        colAttrMap.put(96, "JF_FuncArmchairPrice");
        colAttrMap.put(97, "JF_FuncIpad");
        colAttrMap.put(98, "JF_FuncIpadPrice");
        colAttrMap.put(99, "JF_ToolingCharge");
        colAttrMap.put(100, "description");
        return colAttrMap;
    }











}
