import com.dassault_systemes.createcontent.ErrorMngt.ENONewException;
import com.dassault_systemes.enovia.e6wv2.foundation.jaxb.Dataobject;
import com.dassault_systemes.enovia.tskv2.ProjectSequence;
import com.matrixone.apps.common.util.ComponentsUIUtil;
import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.DomainRelationship;
import com.matrixone.apps.domain.Job;
import com.matrixone.apps.domain.util.*;
import com.matrixone.apps.domain.util.DateUtil;
import com.matrixone.apps.framework.ui.UIUtil;
import com.matrixone.apps.program.ProgramCentralConstants;
import com.matrixone.apps.program.ProjectSpace;
import jakarta.servlet.http.HttpServletRequest;
import matrix.db.*;
import matrix.util.StringList;
import org.antlr.v4.runtime.misc.IntegerList;
import org.apache.commons.io.FileUtils;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class JF_ProcessExcel_mxJPO {
    private static final Logger LOGGER = LoggerFactory.getLogger(JF_ProcessExcel_mxJPO.class);
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy/MM/dd");

    public XSSFWorkbook generateProjectTask(Context context, String[] args) throws Exception {
        Map map = JPO.unpackArgs(args);
        String objectId = UIUtil.getValue(map, DomainConstants.SELECT_ID);
        LOGGER.info("objectId:{}", objectId);
        XSSFWorkbook xssfWorkbook = new XSSFWorkbook();
        try {
            DomainObject projectObj = DomainObject.newInstance(context);
            projectObj.setId(objectId);
            String mqlCmd = "print bus $1 select $2 $3 dump $4";
            String rootNodePALPhysicalId = MqlUtil.mqlCommand(context,
                    true,
                    true,
                    mqlCmd,
                    true,
                    objectId,
                    ProgramCentralConstants.SELECT_PAL_PHYSICALID_FROM_PROJECT,
                    ProgramCentralConstants.SELECT_PAL_PHYSICALID_FROM_TASK,
                    "|");
            ProjectSequence ps = new ProjectSequence(context, rootNodePALPhysicalId);
//            LOGGER.info("ps:{}",rootNodePALPhysicalId);
            StringList bolist = JF_Util_mxJPO.basicBolistSel();
            bolist.add(JF_PLMConstants_mxJPO.SELECT_ATTR_Task_Estimated_Start_Date);
            bolist.add(JF_PLMConstants_mxJPO.SELECT_ATTR_Task_Estimated_Finish_Date);
            MapList objectList = getWBSTasks(context, objectId, DomainConstants.RELATIONSHIP_SUBTASK, (short) 0);
            LOGGER.info("objectList:{}",objectList.size());
            String[] rowName = {"Level", "Task Level", "Name","Project Role" ,"Type", "maturity", "Task Estimated Start Date(Year/Month/Day)", "Task Estimated Finish Date((Year/Month/Day))", "Task Estimated Duration(Automatic Calculation)", "id(forbid Edit)"};
            String[] attributeName = {"level", "taskLevel", "name","attribute[Project Role]", "type", DomainConstants.SELECT_CURRENT, JF_PLMConstants_mxJPO.SELECT_ATTR_Task_Estimated_Start_Date, JF_PLMConstants_mxJPO.SELECT_ATTR_Task_Estimated_Finish_Date, JF_PLMConstants_mxJPO.SELECT_ATTR_Task_Estimated_Duration, "id"};
            XSSFSheet sheet = xssfWorkbook.createSheet("sheet1");
            XSSFRow rowm = sheet.createRow(0);

            CellStyle headerStyle = xssfWorkbook.createCellStyle();
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            CellStyle headerDateStyle = xssfWorkbook.createCellStyle();
            headerDateStyle.setFillForegroundColor(IndexedColors.GREEN.getIndex());
            headerDateStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            // 创建日期格式
            CreationHelper createHelper = xssfWorkbook.getCreationHelper();
            CellStyle datecellStyle = xssfWorkbook.createCellStyle();
            datecellStyle.setDataFormat(createHelper.createDataFormat().getFormat("yyyy/MM/dd"));

            XSSFCell cellRowName = null;
            for (int i = 0; i < rowName.length; i++) {
                cellRowName = rowm.createCell(i);
                cellRowName.setCellType(CellType.STRING);
                cellRowName.setCellValue(rowName[i]);
                if (rowName[i].contains("Date")) {
                    cellRowName.setCellStyle(headerDateStyle);
                } else {
                    cellRowName.setCellStyle(headerStyle);
                }
                if (i != 0) {
                    sheet.setColumnWidth(i, 256 * 26);
                }
            }
            sheet.createFreezePane(0, 1, 0, 1);
//            sheet.setDefaultColumnStyle(5, datecellStyle);
//            sheet.setDefaultColumnStyle(6, datecellStyle);
            Set filterSet = new HashSet();
            int line = 1;

            //项目角色
            String[] options = {"Financial BP", "Costing", "Chair manager", "AQE representative/PQL",
                    "Logistics representative","AME representative","Launch manager","Purchasing representative",
                    "SQD Representative","Business manager","Project manager","Internal Supplier","Foam AME","Trim AME representative"};

            String[] optionsCN = {"SDT-项目财务经理/PC", "SDT-项目成本控制经理/Costing", "SDT-项目总工程师/PDL", "SDT-项目质量经理/PQL",
                    "SDT-项目物流经理/Log","SDT-项目工艺经理/总装AME","SDT-launch经理/LM","SDT-项目采购经理/PUR",
                    "SDT-项目供应商管理经理/ASQ","SDT-项目客户经理/Sales","SDT-项目经理/PM","SDT-内部供应商","SDT-项目工艺经理/发泡AME","SDT-项目工艺经理/面套AME"};


            Map rangeMap=new HashMap();
            rangeMap.put("Financial BP","SDT-项目财务经理/PC");
            rangeMap.put("Costing","SDT-项目成本控制经理/Costing");
            rangeMap.put("Chair manager","SDT-项目总工程师/PDL");
            rangeMap.put("AQE representative/PQL","SDT-项目质量经理/PQL");
            rangeMap.put("Logistics representative","SDT-项目物流经理/Log");
            rangeMap.put("AME representative","SDT-项目工艺经理/总装AME");
            rangeMap.put("Launch manager","SDT-launch经理/LM");
            rangeMap.put("Purchasing representative","SDT-项目采购经理/PUR");
            rangeMap.put("SQD Representative","SDT-项目供应商管理经理/ASQ");
            rangeMap.put("Business manager","SDT-项目客户经理/Sales");
            rangeMap.put("Project manager","SDT-项目经理/PM");
            rangeMap.put("Internal Supplier","SDT-内部供应商");
            rangeMap.put("Foam AME","SDT-项目工艺经理/发泡AME");
            rangeMap.put("Trim AME representative","SDT-项目工艺经理/面套AME");





            //project信息导出


            StringList objectSelects = new StringList();
            objectSelects.addElement(DomainConstants.SELECT_ID);
            objectSelects.addElement(DomainConstants.SELECT_NAME);
            objectSelects.addElement(ProgramCentralConstants.SELECT_PHYSICALID);
            objectSelects.add(JF_PLMConstants_mxJPO.SELECT_ATTR_Task_Estimated_Start_Date);
            objectSelects.add(JF_PLMConstants_mxJPO.SELECT_ATTR_Task_Estimated_Finish_Date);
            objectSelects.add(JF_PLMConstants_mxJPO.SELECT_ATTR_Task_Estimated_Duration);
            objectSelects.add(JF_PLMConstants_mxJPO.SELECT_ATTR_ProjectRole);
            objectSelects.addElement(JF_PLMConstants_mxJPO.SELECT_ATTR_JF_Modules);
            objectSelects.addElement(JF_PLMConstants_mxJPO.SELECT_ATTR_JF_ESOType);
            objectSelects.add(DomainConstants.SELECT_CURRENT);
            objectSelects.add(DomainConstants.SELECT_TYPE);

            Map projectMap = projectObj.getInfo(context, objectSelects);
            LOGGER.info("projectMap:{}", projectMap);
            projectMap.put("level", "0");
            createRowTask(context, projectMap, filterSet, sheet, 0, cellRowName, attributeName, createHelper, ps,rangeMap);

            for (int i = 0; i < objectList.size(); i++) {
                Map temp = (Map) objectList.get(i);

                //去掉ESO开头的任务以及子级
                line = createRowTask(context, temp, filterSet, sheet, line, cellRowName, attributeName, createHelper, ps,rangeMap);

            }


            addDropdownToColumn(sheet,optionsCN,1,line,3);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return xssfWorkbook;
    }



    public  void addDropdownToColumn(Sheet sheet, String[] options,
                                           int firstRow, int lastRow, int columnIndex) {
        DataValidationHelper validationHelper = sheet.getDataValidationHelper();

        // 创建下拉列表约束
        DataValidationConstraint constraint =
                validationHelper.createExplicitListConstraint(options);

        // 定义作用范围（整列）
        CellRangeAddressList addressList = new CellRangeAddressList(
                firstRow, lastRow, columnIndex, columnIndex);

        // 创建数据验证对象
        DataValidation validation = validationHelper.createValidation(constraint, addressList);

        // 可选：配置错误提示
        validation.setShowErrorBox(true);
        validation.createErrorBox("输入错误", "请从下拉列表中选择有效值");

        // 应用到工作表
        sheet.addValidationData(validation);
    }


    public int createRowTask(Context context, Map temp, Set filterSet, XSSFSheet sheet, int line, XSSFCell cellRowName, String[] attributeName, CreationHelper createHelper, ProjectSequence ps,Map rangeMap) throws Exception {
        //去掉ESO开头的任务以及子级
        String level = UIUtil.getValue(temp, "level");
        String physicalid = UIUtil.getValue(temp, "physicalid");
        String toPhysicalid = UIUtil.getValue(temp, "to[Subtask].from.physicalid");
        int intLevel = Integer.parseInt(level);
        String name = UIUtil.getValue(temp, "name");
        String type = UIUtil.getValue(temp, "type");
             /*   if(name.contains("ESO")||type.equals("JF_ESOTask")){
                    parentLevel = intLevel;
                }
                if(intLevel>parentLevel){
                    continue;
                }
*/
        if (name.contains("ESO")) {
            filterSet.add(physicalid);
        } else if (type.equals("JF_ESOTask")) {
            filterSet.add(physicalid);
//                    continue;
        }

        if (!filterSet.contains(toPhysicalid)) {
            XSSFRow rowm = sheet.createRow(line + 1);
            int rowNum = line + 2;
            line++;
            for (int j = 0; j < attributeName.length; j++) {
                cellRowName = rowm.createCell(j);
                if (attributeName[j].contains("taskLevel")) {
                    //获取任务层级
                    cellRowName.setCellType(CellType.STRING);
                    cellRowName.setCellValue(getTaskWBSLevel(context, ps, UIUtil.getValue(temp, ProgramCentralConstants.SELECT_PHYSICALID)));
                }else if(attributeName[j].contains("Project Role")){
                    cellRowName.setCellType(CellType.STRING);

                    String role=UIUtil.getValue(temp, attributeName[j]);
                    if(UIUtil.isNotNullAndNotEmpty(role)){
                        cellRowName.setCellValue(UIUtil.getValue(rangeMap,role));
                    }else {
                        cellRowName.setCellValue("");
                    }

                } else if (attributeName[j].contains(JF_PLMConstants_mxJPO.SELECT_ATTR_Task_Estimated_Duration)) {
//                        cellRowName.setCellType(CellType.STRING);
                    String formula = "NETWORKDAYS(G" + rowNum + ",H" + rowNum + ")";
                    cellRowName.setCellFormula(formula);
                } else {
                    if (!attributeName[j].contains("Date")) {
                        cellRowName.setCellType(CellType.STRING);
                        cellRowName.setCellValue(UIUtil.getValue(temp, attributeName[j]));

                    } else {
                        cellRowName.setCellType(CellType.STRING);
                        //需要格式化日期---去掉时分秒
//                        cellRowName.setCellStyle(datecellStyle);
                        String date = UIUtil.getValue(temp, attributeName[j]).split(" ")[0];
//                        cellRowName.setCellValue(formatDate(context, date));
                        cellRowName.setCellValue(createHelper.createRichTextString(formatDate(context, date)));
                    }
                }
            }
        }
        return line;
    }

    public String formatDate(Context context, String date) throws Exception {
        SimpleDateFormat format = new SimpleDateFormat("MM/dd/yyyy");
        SimpleDateFormat format2 = new SimpleDateFormat("yyyy/MM/dd");
        Date d = format.parse(date);
        return format2.format(d);
    }

    public String formatDateToSystem(Context context, String date) throws Exception {
        SimpleDateFormat format = new SimpleDateFormat("MM/dd/yyyy");
        SimpleDateFormat format2 = new SimpleDateFormat("yyyy/MM/dd");
        Date d = format2.parse(date);
        return format.format(d);
    }

    public Date parseDate(Context context, String date) throws Exception {
        SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd");
        Date d = format.parse(date);
        return d;
    }

    public Date parseDateSystem(Context context, String date) throws Exception {
        SimpleDateFormat format = new SimpleDateFormat("MM/dd/yyyy");
        Date d = format.parse(date);
        return d;
    }


    /**
     * 上传项目任务
     *
     * @param context
     * @param args
     * @return java.lang.String
     * @throws
     * @description
     * @author LiuJR
     * @date 2023/12/14 13:39
     */
    public StringList importProjectTask(Context context, String[] args) throws Exception {
        String strReturnMess = "";
        StringList errorList = new StringList();
        Locale sLanguage = context.getLocale();
        try {
            ContextUtil.pushContext(context);

            Map paramMap = (Map) JPO.unpackArgs(args);

            LOGGER.info("paramMap:{}", paramMap);
            //国际化语言
            String strLanguage = (String) paramMap.get("language");
            //国际化文件的区
            String sSuiteKey = (String) paramMap.get("suiteKey");
            //当前项目ID
            String ProjectobjectId = (String) paramMap.get("objectId");
            //上传的文件
            List files = (List) paramMap.get("files");
            //workspce文件地址
            String workspace = context.createWorkspace();
            Iterator iteratorFile = files.iterator();
            Boolean fileIsExcel = true;
            LOGGER.info("====开始校验上传文档的内容=======");
            //拿取文件数据和检验
            Iterator iterator = files.iterator();
            int index;
            String sFileName = "";
            File file = null;
            File outfile = null;
            String outFilePath = "";
            String strPartId = null;
            //文件类型
            String strContentType = "";
            //存放所有校验都通过后，需要创建的对象的信息集合
            MapList mapAllTierXList = new MapList();
            //存放文件中每行数据对象的唯一标识，以便检验确定唯一性
            StringList rowTierXList = new StringList();
            //存放系统中所有TierX 的唯一标识，以便校验确定唯一性

            //当个文件校验
            Boolean fileSuccess = true;
            //所有文件校验
            Boolean allFileSuccess = true;
            //存放异常时需要下载的文件

            String id, EstimatedStartDate, EstimatedEndDate;
            while (iterator.hasNext()) {
                file = (File) iterator.next();
                //判断当前文件名：由于各个浏览器的不同，可能会包含路径
                sFileName = file.getName();
                if (sFileName.contains("/")) {
                    index = sFileName.lastIndexOf("/");
                    sFileName = sFileName.substring(index);
                }
                if (sFileName.contains("\\")) {
                    index = sFileName.lastIndexOf("\\");
                    sFileName = sFileName.substring(index + 1);
                }
                outFilePath = workspace + sFileName;
                if (new File(outFilePath).exists()) {
                    File file1 = new File(outFilePath);
                    file1.delete();
                }
                //暂存文件
                outfile = new File(outFilePath);
                //写入暂存文件
                FileUtils.copyFile(file, outfile);
                //读取excel文件中的数据
                InputStream inputStream = new FileInputStream(outFilePath);
                Workbook workbook = WorkbookFactory.create(inputStream);
                Sheet sheet = workbook.getSheetAt(0);
                MapList alldataList = JF_ExcelUtils_mxJPO.getMapList((XSSFSheet) sheet, 1, true, false);//调用工具类读取里面的内容
                LOGGER.info("alldataList:{}", alldataList);


                //设置列和属性对应
                Map<String, String> attributeMap = setColAttributeMapping();
                Map temp = null;
                Map valueMap = new HashMap();
                String startDate;
                String FinishDate;
                String RowNum;
                String objectId;
                String Level;
                String role;
//                LOGGER.info("addDataList:{}", alldataList);
                //全部检查一遍是否有问题
                String errorNotice = EnoviaResourceBundle.getProperty(context, "emxProgramCentralStringResource", sLanguage, "emxProgramCentral.import.Notice1");
                String errorNotice2 = EnoviaResourceBundle.getProperty(context, "emxProgramCentralStringResource", sLanguage, "emxProgramCentral.import.Notice2");
                String errorNotice3 = EnoviaResourceBundle.getProperty(context, "emxProgramCentralStringResource", sLanguage, "emxProgramCentral.import.Notice3");
                String errorNotice4 = EnoviaResourceBundle.getProperty(context, "emxProgramCentralStringResource", sLanguage, "emxProgramCentral.import.Notice4");
                String errorNotice5 = EnoviaResourceBundle.getProperty(context, "emxProgramCentralStringResource", sLanguage, "emxProgramCentral.import.Notice5");
                String errorNotice6 = EnoviaResourceBundle.getProperty(context, "emxProgramCentralStringResource", sLanguage, "emxProgramCentral.import.Notice7");
//                String errorNotice6 = "id error";
                LOGGER.info("alldataList:{}", alldataList);


                Map rangeMapCN=new HashMap();
                rangeMapCN.put("","");
                rangeMapCN.put("SDT-项目财务经理/PC","Financial BP");
                rangeMapCN.put("SDT-项目成本控制经理/Costing","Costing");
                rangeMapCN.put("SDT-项目总工程师/PDL","Chair manager");
                rangeMapCN.put("SDT-项目质量经理/PQL","AQE representative/PQL");
                rangeMapCN.put("SDT-项目物流经理/Log","Logistics representative");
                rangeMapCN.put("SDT-项目工艺经理/总装AME","AME representative");
                rangeMapCN.put("SDT-launch经理/LM","Launch manager");
                rangeMapCN.put("SDT-项目采购经理/PUR","Purchasing representative");
                rangeMapCN.put("SDT-项目供应商管理经理/ASQ","SQD Representative");
                rangeMapCN.put("SDT-项目客户经理/Sales","Business manager");
                rangeMapCN.put("SDT-项目经理/PM","Project manager");
                rangeMapCN.put("SDT-内部供应商","Internal Supplier");
                rangeMapCN.put("SDT-项目工艺经理/发泡AME","Foam AME");
                rangeMapCN.put("SDT-项目工艺经理/面套AME","Trim AME representative");


                //检查项目id是否一致
                if(checkProjectInfo(context,alldataList,ProjectobjectId,attributeMap)){
                    for (int i = 0; i < alldataList.size(); i++) {
                        StringBuffer errorStr = new StringBuffer();
                        temp = (Map) alldataList.get(i);
                        Level = UIUtil.getValue(temp, attributeMap.get("Level"));
                        startDate = UIUtil.getValue(temp, attributeMap.get("startDate"));
                        FinishDate = UIUtil.getValue(temp, attributeMap.get("FinishDate"));
                        objectId = UIUtil.getValue(temp, attributeMap.get("objectId"));
                        role = UIUtil.getValue(temp, attributeMap.get("role"));
                        RowNum = UIUtil.getValue(temp, "RowNum");
                        if (UIUtil.isNullOrEmpty(objectId) || UIUtil.isNullOrEmpty(startDate) || !checkDateFormat(context, startDate) || UIUtil.isNullOrEmpty(FinishDate) || !checkDateFormat(context, FinishDate)) {
                            errorStr.append(errorNotice5 + " ");
                            errorStr.append(RowNum);
                            errorStr.append(" " + errorNotice4);
                            errorStr.append(errorNotice);
                            errorList.add(errorStr.toString());
                        }
                        errorStr.setLength(0);
                        if (!compareDate(context, startDate, FinishDate)) {//开始时间大于结束时间报错
                            if (errorStr.length() == 0) {
                                errorStr.append(errorNotice5 + " ");
                                errorStr.append(RowNum);
                                errorStr.append(" " + errorNotice4);
                            }
                            errorStr.append(errorNotice2);
                            errorList.add(errorStr.toString());
                        }
                        errorStr.setLength(0);
                        if ("0".equalsIgnoreCase(Level)) {
                            //第一层的任务，不需要校验父和子的时间
                        } else {
                            if (!checkParentDate(context, alldataList, i, startDate, FinishDate, Level)) {//校验父任务结束时间不能早于子任务结束时间，开始时间不能晚于子任务开始时间
                                if (errorStr.length() == 0) {
                                    errorStr.append(errorNotice5 + " ");
                                    errorStr.append(RowNum);
                                    errorStr.append(" " + errorNotice4);
                                }
                                errorStr.append(errorNotice3);
                                errorList.add(errorStr.toString());
                            }
                        }
                    }
                }else {
                    //id不一致
                    errorList.add(errorNotice6);

                }



                LOGGER.info("errorList:{}", errorList);
                valueMap.clear();
                //如果没有问题
                ContextUtil.startTransaction(context, true);
                if (errorList.size() == 0) {
                    for (int i = 0; i < alldataList.size(); i++) {
                        temp = (Map) alldataList.get(i);
                        startDate = UIUtil.getValue(temp, attributeMap.get("startDate"));
                        FinishDate = UIUtil.getValue(temp, attributeMap.get("FinishDate"));
                        String Level1 = UIUtil.getValue(temp, attributeMap.get("Level"));
                        String role1 = UIUtil.getValue(temp, attributeMap.get("role"));
                        startDate = formatDateToSystem(context, startDate);
                        FinishDate = formatDateToSystem(context, FinishDate);
                        valueMap.put(JF_PLMConstants_mxJPO.ATTR_Task_Estimated_Start_Date, startDate);
                        valueMap.put(JF_PLMConstants_mxJPO.ATTR_Task_Estimated_Finish_Date, FinishDate);
                        String rangeRole="";
                        if(!Level1.equals("0")){
                            rangeRole= (String) rangeMapCN.get(role1);
                            valueMap.put(JF_PLMConstants_mxJPO.ATTR_ProjectRole, rangeRole);
                        }
                        valueMap.put(JF_PLMConstants_mxJPO.ATTR_Task_Estimated_Duration, getDurationDay(context, startDate, FinishDate));//持续时间 需要计算
                        if (valueMap.size() > 0) {
                            setEstimatedDate(context, valueMap, UIUtil.getValue(temp, attributeMap.get("objectId")));
                        }
                    }
                    ContextUtil.commitTransaction(context);
                    return errorList;
                } else {
                    ContextUtil.abortTransaction(context);
                    return errorList;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            ContextUtil.abortTransaction(context);
            errorList.add(e.getMessage());
            return errorList;
        } finally {
            ContextUtil.popContext(context);
        }
        return errorList;
    }

    public boolean checkProjectInfo(Context context,MapList alldataList,String projectid,Map<String, String> attributeMap)throws Exception{
        boolean b=false;
        for (int i = 0; i < alldataList.size(); i++) {
            Map temp = (Map) alldataList.get(i);
            String Level = UIUtil.getValue(temp, attributeMap.get("Level"));
            String objectId = UIUtil.getValue(temp, attributeMap.get("objectId"));
            if("0".equals(Level)){
                if(projectid.equals(objectId)){
                    b=true;
                }else {
                    b=false;
                }
                break;
            }
        }

        return b;
    }

    /*
     * @description: 设置计划开始时间和结束时间
     * @author: caipan
     * @date:
     * @param: * @param[1] context
     * @param[2] map
     * @param[3] objectId
     * @return:
     **/
    public void setEstimatedDate(Context context, Map map, String objectId) throws Exception {
        if (UIUtil.isNotNullAndNotEmpty(objectId)) {
            DomainObject taskObj = DomainObject.newInstance(context);
            taskObj.setId(objectId);
            String current = taskObj.getInfo(context, DomainConstants.SELECT_CURRENT);
            if ("Create".equalsIgnoreCase(current) || "Assign".equalsIgnoreCase(current) || "Active".equalsIgnoreCase(current)) {
                taskObj.setAttributeValues(context, map);
            }
        }
    }

    /*
     * @description: 检查日期格式是否符合MM/dd/yyyy，如果符合就通过
     * @author: caipan
     * @date:
     * @param: * @param[1] context
     * @param[2] date
     * @return:
     **/
    public boolean checkDateFormat(Context context, String date) {
        try {
            SimpleDateFormat OOTBformat = new SimpleDateFormat("yyyy/MM/dd", Locale.US);
            OOTBformat.parse(date);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
    //两个时间比较大小
    //子任务不能大于父任务的两个时间

    /**
     * 初始化面套BOM结构
     * 逻辑：
     * 1.根据excel模板/ds/tomee/space-cas/webapps/3dspace/jf_template/TrimImportBOMTemplate.xlsx,填写对应的零件属性和值
     * 2.校验：
     * 01. 样板件零件号_版本、面料零件号_版本不能为空
     * 02. 样板件零件号_版本、面料零件号_版本格式必须为：企业编码_版本号
     * 03. 面积和周长的格式不对，必须为数值
     * 04. 样板件、面料件（企业编码+版本）必须存在系统
     * 05. 面料件（企业编码+版本+材料）
     *
     * @param context
     * @param args
     * @return java.lang.String
     * @throws
     * @description
     * @author LiuJR
     * @date 2024/10/11 13:39
     */
    public StringList importTrimPart(Context context, String[] args) throws Exception {
        String strReturnMess = "";
        StringList errorList = new StringList();
        JF_Util_mxJPO jfUtilMxJPO = new JF_Util_mxJPO();
        String personName = context.getUser();
        String organization = jfUtilMxJPO.getPersonOrganization(context, personName);
        try {
            ContextUtil.pushContext(context);
            Map paramMap = (Map) JPO.unpackArgs(args);
            LOGGER.info(paramMap.toString());
            String objectId = (String) paramMap.get("objectId");
            HttpServletRequest request = (HttpServletRequest) paramMap.get("HttpServletRequest");
            LOGGER.info("objectId:{}", objectId);
            Locale sLanguage = context.getLocale();
            //国际化文件的区
            String sSuiteKey = (String) paramMap.get("suiteKey");
            //上传的文件
            List files = (List) paramMap.get("files");
            //workspce文件地址
            String workspace = context.createWorkspace();
            LOGGER.info("====开始校验上传文档的内容=======");
            //拿取文件数据和检验
            Iterator iterator = files.iterator();
            int index;
            String sFileName = "";
            File file = null;
            File outfile = null;
            String outFilePath = "";
            //存放异常时需要下载的文件
            MapList modifyAttrList = new MapList();
            while (iterator.hasNext()) {
                file = (File) iterator.next();
                //判断当前文件名：由于各个浏览器的不同，可能会包含路径
                sFileName = file.getName();
                if (sFileName.contains("/")) {
                    index = sFileName.lastIndexOf("/");
                    sFileName = sFileName.substring(index);
                }
                if (sFileName.contains("\\")) {
                    index = sFileName.lastIndexOf("\\");
                    sFileName = sFileName.substring(index + 1);
                }
                outFilePath = workspace + sFileName;
                if (new File(outFilePath).exists()) {
                    File file1 = new File(outFilePath);
                    file1.delete();
                }
                //暂存文件
                outfile = new File(outFilePath);
                //写入暂存文件
                FileUtils.copyFile(file, outfile);
                //读取excel文件中的数据
                InputStream inputStream = new FileInputStream(outFilePath);
                Workbook workbook = WorkbookFactory.create(inputStream);
                Sheet sheet = workbook.getSheetAt(0);
                MapList alldataList = JF_ExcelUtils_mxJPO.getMapList((XSSFSheet) sheet, 1, false, true);//调用工具类读取里面的内容
//                LOGGER.info("alldata:{}", alldataList.toString());
                Map temp = null;
                String templatePartName, templatePartRev;
                String usage, JF_Area, JF_Circumference, trinName, JF_TrimRev, group;
                String RowNum;
                //全部检查一遍是否有问题
                Map<String, String> attributeMapp = setTrimAttributeMapping();
                String errorNotice = EnoviaResourceBundle.getProperty(context, "emxProgramCentralStringResource", sLanguage, "emxProgramCentral.importTrim.Notice1");
                String errorTPartType = EnoviaResourceBundle.getProperty(context, "emxProgramCentralStringResource", sLanguage, "emxProgramCentral.importTrim.TPartType");
                String errorMPartType = EnoviaResourceBundle.getProperty(context, "emxProgramCentralStringResource", sLanguage, "emxProgramCentral.importTrim.MPartType");
                String errorNumberS = EnoviaResourceBundle.getProperty(context, "emxProgramCentralStringResource", sLanguage, "emxProgramCentral.importTrim.NumberS");
                String errorNotice3 = EnoviaResourceBundle.getProperty(context, "emxProgramCentralStringResource", sLanguage, "emxProgramCentral.importTrim.Notice3");
                String errorNoticeUsage = EnoviaResourceBundle.getProperty(context, "emxProgramCentralStringResource", sLanguage, "emxProgramCentral.importTrim.Usage");
                String errorNotice4 = EnoviaResourceBundle.getProperty(context, "emxProgramCentralStringResource", sLanguage, "emxProgramCentral.import.Notice4");
                String errorNotice5 = EnoviaResourceBundle.getProperty(context, "emxProgramCentralStringResource", sLanguage, "emxProgramCentral.import.Notice5");
                String errorNotice6 = EnoviaResourceBundle.getProperty(context, "emxProgramCentralStringResource", sLanguage, "emxProgramCentral.importTrim.Notice4");
                //当前面料件已经冻结或者发布，请换号  废弃
                String errorNoticeP = EnoviaResourceBundle.getProperty(context, "emxProgramCentralStringResource", sLanguage, "emxProgramCentral.import.Notice6");
                String errorMMPartType = EnoviaResourceBundle.getProperty(context, "emxProgramCentralStringResource", sLanguage, "emxProgramCentral.importTrim.MMPartType");
                //读取出来的零件号是如下格式 GT000012_AA.1
                StringList temNameList = new StringList();
                boolean partNameFormat = true;//零件Name 是否合规 GT000012_AA.1
                for (int i = 0; i < alldataList.size(); i++) {
                    StringBuffer errorStr = new StringBuffer();
                    temp = (Map) alldataList.get(i);
                    //样板件零件号版本
                    templatePartName = UIUtil.getValue(temp, UIUtil.getValue(attributeMapp, "templateName"));
                    temNameList = FrameworkUtil.split(templatePartName, "_");
                    if (temNameList.size() == 2) {
                        templatePartName = temNameList.get(0);
                        templatePartRev = temNameList.get(1);
                    } else {
                        partNameFormat = false;
                        templatePartName = "";
                        templatePartRev = "";
                    }
                    //面料零件号版本
                    trinName = UIUtil.getValue(temp, UIUtil.getValue(attributeMapp, "JF_Trim"));//面料号
                    temNameList = FrameworkUtil.split(trinName, "_");
                    if (temNameList.size() == 2) {
                        trinName = temNameList.get(0);
                        JF_TrimRev = temNameList.get(1);
                    } else {
                        partNameFormat = false;
                        trinName = "";
                        JF_TrimRev = "";
                    }
                    usage = UIUtil.getValue(temp, UIUtil.getValue(attributeMapp, "usage"));//样版用量
                    JF_Area = UIUtil.getValue(temp, UIUtil.getValue(attributeMapp, "JF_Area"));//面积
                    JF_Circumference = UIUtil.getValue(temp, UIUtil.getValue(attributeMapp, "JF_Circumference"));//周长
                    group = UIUtil.getValue(temp, UIUtil.getValue(attributeMapp, "group"));//分组
                    RowNum = UIUtil.getValue(temp, "RowNum");//行数
                    //零件号格式不对
                    errorStr.setLength(0);
                    if (!partNameFormat) {//零件号格式不对
                        errorStr.append(errorNotice5 + " ");
                        errorStr.append(RowNum);
                        errorStr.append(" " + errorNotice4);
                        errorStr.append(errorNotice6);
                        errorList.add(errorStr.toString());
                    }
                    //样板件号、面料件号 不能为空
                    if (UIUtil.isNullOrEmpty(templatePartName) || UIUtil.isNullOrEmpty(templatePartRev) || UIUtil.isNullOrEmpty(trinName) || UIUtil.isNullOrEmpty(JF_TrimRev)) {
                        errorStr.append(errorNotice5 + " ");
                        errorStr.append(RowNum);
                        errorStr.append(" " + errorNotice4);
                        errorStr.append(errorNotice);
                        errorList.add(errorStr.toString());
                    }
                    //样板件、面料件 企业编码不能相同
                    errorStr.setLength(0);
                    if (trinName.equalsIgnoreCase(templatePartName)) {
                        errorStr.append(errorNotice5 + " ");
                        errorStr.append(RowNum);
                        errorStr.append(" " + errorNotice4);
                        errorStr.append(" " + templatePartName + " :" + templatePartRev);
                        errorStr.append(errorNumberS);
                        errorList.add(errorStr.toString());
                    }
                    //样版件是否存在
                    errorStr.setLength(0);
                    MapList templateMapList = checkPartTypeExist(context, templatePartName, templatePartRev, "T02");
                    if (templateMapList.isEmpty()) {
                        errorStr.append(errorNotice5 + " ");
                        errorStr.append(RowNum);
                        errorStr.append(" " + errorNotice4);
                        errorStr.append(" " + templatePartName + " :" + templatePartRev);
                        errorStr.append(errorTPartType);
                        errorList.add(errorStr.toString());
                    }
                    //面料件是否存在
                    errorStr.setLength(0);
                    MapList trimMapList = checkPartTypeExist(context, trinName, JF_TrimRev, "T03");
                    if (trimMapList.isEmpty()) {
                        errorStr.append(errorNotice5 + " ");
                        errorStr.append(RowNum);
                        errorStr.append(" " + errorNotice4);
                        errorStr.append(" " + trinName + " :" + JF_TrimRev);
                        errorStr.append(errorMPartType);
                        errorList.add(errorStr.toString());
                    }
                    //面积和周长的格式不对
                    errorStr.setLength(0);
                    if (!(isNumber(usage) && isNumber(JF_Area) && isNumber(JF_Circumference))) {//必须为数字
                        errorStr.append(errorNotice5 + " ");
                        errorStr.append(RowNum);
                        errorStr.append(" " + errorNotice4);
                        errorStr.append(errorNotice3);
                        errorList.add(errorStr.toString());
                    }
                    //用量数值不为0 或者0.0
                    errorStr.setLength(0);
                    if ("0".equalsIgnoreCase(usage) || "0.0".equalsIgnoreCase(usage) || UIUtil.isNullOrEmpty(usage)) {
                        errorStr.append(errorNotice5 + " ");
                        errorStr.append(RowNum);
                        errorStr.append(" " + errorNotice4);
                        errorStr.append(errorNoticeUsage);
                        errorList.add(errorStr.toString());
                    }
                }
                LOGGER.info("errorList:{}", errorList);
                if (errorList.size() > 0) {
                    return errorList;
                }
                //如果没有问题 开始拿取样板包
                //面套总成
                DomainObject partObject = DomainObject.newInstance(context);
                partObject.setId(objectId);
                StringList basicBoListSel = JF_Util_mxJPO.basicBolistSel();
                StringList basicRelLstSel = JF_Util_mxJPO.basicRellistSel();
                basicBoListSel.add(JF_PLMConstants_mxJPO.SELECT_ATTR_PLMEntity_V_Name);
                basicBoListSel.add("attribute[PLMEntity.V_description]");
                //拿取面套下的样板包及其面料件
                MapList mapList1 = partObject.getRelatedObjects(
                        context,
                        JF_PLMConstants_mxJPO.REL_Instance,
                        JF_PLMConstants_mxJPO.TYPE_VPMReference,
                        basicBoListSel,
                        basicRelLstSel,
                        false,
                        true,
                        (short) 1,
                        "attribute[JF_VPMReference.JF_PartSubType]=='T06'&&current==IN_WORK",
                        "",
                        0
                );
                StringList partT06List = (StringList) mapList1.stream().map(m -> {
                    Map map = (Map) m;
                    return UIUtil.getValue(map, DomainConstants.SELECT_ID);
                }).collect(Collectors.toCollection(StringList::new));
                //存储现有的面料_分组 : 样板包号
                HashMap<String, String> packageT06Map = new HashMap<>();
                //存储现有的面料_分组 : 面料号
                HashMap<String, String> packageT03Map = new HashMap<>();
                Iterator iterator1 = mapList1.iterator();
                while (iterator1.hasNext()) {
                    Map map1 = (Map) iterator1.next();
                    String partT06Id = UIUtil.getValue(map1, DomainConstants.SELECT_ID);
                    partObject.setId(partT06Id);
                    String strMId = partObject.getInfo(context, "from[JFRelPart2Raw].to.id");
                    String key = UIUtil.getValue(map1, "attribute[PLMEntity.V_description]");
                    if (UIUtil.isNullOrEmpty(key)) {
                        continue;
                    }
                    packageT06Map.put(key, partT06Id);
                    packageT03Map.put(key, strMId);
                }
                //遍历表格数据，拿取到面料件的企业编码_材料，到集合中拿取样板包，
                ContextUtil.startTransaction(context, true);
                //搭建bom结构关系
                //存储样板包 样板件  面料件
                HashSet<String> partT06IdSet = new HashSet<>();
                MapList mapList = new MapList();
                for (int i = 0; i < alldataList.size(); i++) {
                    temp = (Map) alldataList.get(i);
                    Map templateIdAndTrimIdMap = getRowDataTemplateIdAndTrimIdMap(context, temp, attributeMapp);
                    String templateId = UIUtil.getValue(templateIdAndTrimIdMap, "templateId");
                    String trimId = UIUtil.getValue(templateIdAndTrimIdMap, "trimId");
                    String key = UIUtil.getValue(templateIdAndTrimIdMap, "key");
                    if (UIUtil.isNullOrEmpty(templateId) || UIUtil.isNullOrEmpty(trimId)) {
                        continue;
                    }
                    //开始搭建结构
                    partT06IdSet.add(buildBOMT06Structure(context, request, personName, organization, trimId, templateId, key, temp, attributeMapp, packageT06Map, packageT03Map, mapList));
                }
                StringList partET06List = new StringList();
                partET06List.addAll(StringList.create(partT06IdSet));
                LOGGER.info("partET06List:{}", partET06List);
                //做关系
                LOGGER.info("mapList:{}", mapList);
                if (mapList.size() > 0) {
                    Map groupMap = (Map) mapList.stream().collect(Collectors.groupingBy(m -> {
                                Map info = (Map) m;
                                //改为样板包分组
                                return info.get("T06Id");
                            })
                    );
                    LOGGER.info("groupMap:{}", groupMap);
                    for (int i = 0; i < partET06List.size(); i++) {
                        String partET06Id = partET06List.get(i);
                        if (groupMap.containsKey(partET06Id)) {
                            List infoList = (List) groupMap.get(partET06Id);
                            Set changeSourceSet = (Set) infoList.stream().map(m -> {
                                Map sun = (Map) m;
                                return sun.get("templateId");
                            }).collect(Collectors.toSet());
                            //表格中的样板包下的样板件
                            StringList templateIdList = StringList.create(changeSourceSet);
                            DomainObject domainObject = DomainObject.newInstance(context, partET06Id);
                            //目前样板包关联的样板件
                            MapList templateMapList = domainObject.getRelatedObjects(
                                    context,
                                    JF_PLMConstants_mxJPO.REL_Instance,
                                    JF_PLMConstants_mxJPO.TYPE_VPMReference,
                                    basicBoListSel,
                                    basicRelLstSel,
                                    false,
                                    true,
                                    (short) 1,
                                    "attribute[JF_VPMReference.JF_PartSubType]=='T02'",
                                    "",
                                    0
                            );
                            StringList templateList = (StringList) templateMapList.stream().map(m -> {
                                Map map = (Map) m;
                                return UIUtil.getValue(map, DomainConstants.SELECT_ID);
                            }).collect(Collectors.toCollection(StringList::new));
                            LOGGER.info("@@@@@@@templateList:{}", templateList);
                            for (int t = 0; t < templateList.size(); t++) {
                                String templateId = templateList.get(t);
                                if (!templateIdList.contains(templateId)) {
                                    //如果不包含需要断开关系
                                    Map mapDel = new HashMap();
                                    mapDel.put("relName", JF_PLMConstants_mxJPO.REL_Instance);
                                    mapDel.put("fromId", partET06Id);
                                    mapDel.put("toId", templateId);
                                    String connId = JF_PublicMethodClass_mxJPO.getTwoBusinessObjectConnId(context, JPO.packArgs(mapDel));
                                    DomainRelationship.disconnect(context, connId);
                                }
                            }
                        }
                    }
                }
                //面套总成关联样版包
                partObject.setId(objectId);
                //批量建立关系 面套 - 样板包
                for (int i = 0; i < partET06List.size(); i++) {
                    String partET06Id = partET06List.get(i);
                    if (!partT06List.contains(partET06Id)) {  //如果面套件的样板不包含这个样板包，需要新建关系
                        DomainRelationship ship = partObject.addToObject(context, new RelationshipType(JF_PLMConstants_mxJPO.REL_Instance), partET06Id);
                        addInterface(context, ship.getPhysicalId(context));
                        ship.setAttributeValue(context, JF_PLMConstants_mxJPO.ATTR_JF_Dosage, "1.0");
                    }
                }
                //如果excel表没有的样板包 不需要删除
                ContextUtil.commitTransaction(context);
                return errorList;
            }
        } catch (Exception e) {
            e.printStackTrace();
            ContextUtil.abortTransaction(context);
            errorList.add(e.getMessage());
            return errorList;
        } finally {
            ContextUtil.popContext(context);
        }
        return errorList;
    }

    /**
     * 新建样板包
     *
     * @param context
     * @param trimId
     * @param request
     * @param personName
     * @param organization
     * @param trimV_Name
     * @param strJF_PartDes
     * @return java.lang.String
     * @throws
     * @author LIUJR
     * @date 2025/3/20 13:32
     * @description
     */
    public String createPartT06Object(Context context, String trimId, HttpServletRequest request, String personName, String organization, String trimV_Name, String strJF_PartDes) throws Exception {
        JF_Trim_mxJPO trimMxJPO = new JF_Trim_mxJPO();
        String project = JF_PLMConstants_mxJPO.PROJECT_JFSeat;
        String newId = DomainConstants.EMPTY_STRING;
        String mql = "mod bus $1 current $2;";
        try {
            HashMap<Object, Object> objectObjectHashMap = new HashMap<>();
            objectObjectHashMap.put("HttpServletRequest", request);
            objectObjectHashMap.put("v_name", strJF_PartDes);
            objectObjectHashMap.put("description", trimV_Name);
            //零件的名称
            newId = trimMxJPO.createTrimPublic(context, JPO.packArgs(objectObjectHashMap));
            DomainObject newPartT06Object = DomainObject.newInstance(context, newId);
//            MapList NumberGenerator = DomainObject.findObjects(context, "eService Number Generator", "*", "name=='type_JFPartNumberGT'", new StringList("id"));
//            if (NumberGenerator.size() > 0) {
//                JF_VPMT_mxJPO jfVpmtMxJPO = JF_VPMT_mxJPO.getInstance(context);
//                String PartType = newPartT06Object.getAttributeValue(context, JF_PLMConstants_mxJPO.ATTR_JF_PartType);
//                String partName = jfVpmtMxJPO.autoName(context, newPartT06Object, PartType);
//                newPartT06Object.setAttributeValue(context, JF_PLMConstants_mxJPO.ATTR_V_PART_NUMBER, partName);
//            }
            // 设置owner
            newPartT06Object.setOwner(context, personName);
            // 设置协作区和组织
            newPartT06Object.setPrimaryOwnership(context, project, organization);
            //提升状态
            MqlUtil.mqlCommand(context, true, false, mql, true, newId, "IN_WORK");
            //查询同步属性
            //关联面料
            newPartT06Object.addToObject(context, new RelationshipType(JF_PLMConstants_mxJPO.TYPE_JF_relPart2Raw), trimId);
            //样板包入库 查找库
            String classId = JF_PublicMethodClass_mxJPO.findObject(context, "General Class", "attribute[JF_PartSubType]==T06");
//            String classId = JF_PublicMethodClass_mxJPO.getBasicUrl(context, new String[]{"TrimT06.Class.Id"});
            if (UIUtil.isNotNullAndNotEmpty(classId)) {
                LOGGER.info("样板包入库  TrimT06.Class.Id:{}", classId);
                newPartT06Object.addFromObject(context, new RelationshipType("Classified Item"), classId);
            }
                        /*属性映射   20260817 update by ljr
                1.样板包的零件中文名称="样板包"_卷料的材料--IT
                2.样板包的零件英文名称="PatternAssembly"--IT
                3.样板包的材料=卷料的材料--IT
                4.样板包的卷料号=卷料零件号--IT
                5.样板包的功能用途=卷料的功能用途--IT
             */
            DomainObject trimObject = DomainObject.newInstance(context, trimId);
            HashMap<String, String> attributeMap = new HashMap<>();
            String function = trimObject.getAttributeValue(context, JF_PLMConstants_mxJPO.ATTR_Function);
            String material = trimObject.getAttributeValue(context, JF_PLMConstants_mxJPO.ATTR_JF_Material);
            String rollPartNumber = trimObject.getAttributeValue(context, JF_PLMConstants_mxJPO.ATTR_V_PART_NUMBER);
            attributeMap.put(JF_PLMConstants_mxJPO.ATTR_JF_PartNameCN,"样板包_" + material);
            attributeMap.put(JF_PLMConstants_mxJPO.ATTR_JF_PartNameEN,"Pattern Assembly");
            attributeMap.put(JF_PLMConstants_mxJPO.ATTR_JF_Material,material);
            //20260817 update by ljr 样板包的卷料号和功能用途直接抓取卷料对应属性
            attributeMap.put(JF_PLMConstants_mxJPO.ATTR_RollPartNumber, rollPartNumber);
            attributeMap.put(JF_PLMConstants_mxJPO.ATTR_Function, function);
            newPartT06Object.setAttributeValues(context, attributeMap);
        } catch (Exception e) {
            throw e;
        } catch (ENONewException e) {
            throw new RuntimeException(e);
        }
        return newId;
    }

    /**
     * 查询物理产品的属性
     *
     * @param context
     * @param args
     * @return matrix.util.StringList
     * @throws
     * @author LIUJR
     * @date 2025/2/27 15:47
     * @description
     */
    public static StringList getVPMReferenceAttribute(Context context, String[] args) {
        StringList stringList = new StringList();
        stringList.add("JF_VPMReference.JF_PartDes");
        stringList.add("JF_VPMReference.JF_PartEnDesc");
        stringList.add("JF_VPMReference.JF_Wid");
        stringList.add("JF_VPMReference.JF_GramWeight");
        stringList.add("JF_VPMReference.JF_TransferDownstreamSystem");
        stringList.add("CustomerPartRevision");
        stringList.add("JF_VPMReference.JF_PartType");
        stringList.add("JF_VPMReference.JF_ISWholeChair");
        stringList.add("JF_VPMReference.JF_Weight");
        stringList.add("JF_VPMReference.JF_Craft");
        stringList.add("JF_VPMReference.JF_PartNumberExternal");
        stringList.add("JF_VPMReference.JF_DirectBuy");
        stringList.add("JF_VPMReference.JF_MaterialStar");
        stringList.add("JF_VPMReference.JF_ProcurementType");
        stringList.add("JF_VPMReference.JF_PartSubType");
//        stringList.add("JF_VPMReference.JF_ColorPart");
        stringList.add("JF_VPMReference.JF_Lon");
        stringList.add("JF_VPMReference.JF_Material");
        stringList.add("JF_VPMReference.JF_PartNameCN");
        stringList.add("JF_VPMReference.JF_ProjectRel");
        stringList.add("JF_VPMReference.JF_FlexiblePart");
        stringList.add("JF_VPMReference.JF_Area");
        stringList.add("JF_VPMReference.JF_Unit");
        stringList.add("JF_VPMReference.JF_Circumference");
        stringList.add("JF_VPMReference.JF_TransformationPlan");
        stringList.add("JF_VPMReference.JF_SupplierOrSupplierPartNumber");
        stringList.add("CustomerPartNumber");
        stringList.add("JF_VPMReference.JF_SurfaceTreatment");
        stringList.add("JF_VPMReference.JF_PartNameEN");
        stringList.add("JF_VPMReference.JF_ISXPDM");
        stringList.add("JF_VPMReference.JF_WeightTarget");
        stringList.add("JF_VPMReference.JF_Hig");
        stringList.add("JF_VPMReference.JF_OriginalPart");
        stringList.add("EnterpriseExtension.V_PartNumber");
        stringList.add("PLMEntity.V_Name");
        return stringList;
    }

    /*
     * @description: 检查导入的零件号是否存在
     * @author: caipan
     * @date:
     * @param: * @param[1] context
     * @param[2] templatName
     * @param[3] trimName
     * @return:
     **/
    public MapList checkPartExist(Context context, String templatName, String templatRev) throws Exception {
        StringList selList = JF_Util_mxJPO.basicBolistSel();
        String where = "attribute[EnterpriseExtension.V_PartNumber]=='" + templatName + "' && revision=='" + templatRev + "'";
        MapList list = DomainObject.findObjects(context, JF_PLMConstants_mxJPO.TYPE_VPMReference, "*", where, selList);
        return list;
    }


    /*
     * @description: 检查导入的零件号，及其类型是否存在
     * @author: caipan
     * @date:
     * @param: * @param[1] context
     * @param[2] templatName
     * @param[3] trimName
     * @return:
     **/
    public MapList checkPartTypeExist(Context context, String templatName, String templatRev, String subType) throws Exception {
        MapList mapList = new MapList();
        StringList selList = JF_Util_mxJPO.basicBolistSel();
        selList.add("attribute[JF_VPMReference.JF_PartSubType]");
        String where = "attribute[EnterpriseExtension.V_PartNumber]=='" + templatName + "' && revision=='" + templatRev + "'";
        MapList list = DomainObject.findObjects(context, JF_PLMConstants_mxJPO.TYPE_VPMReference, "*", where, selList);
        Iterator iterator = list.iterator();
        while (iterator.hasNext()) {
            Map map = (Map) iterator.next();
            String attr = UIUtil.getValue(map, "attribute[JF_VPMReference.JF_PartSubType]");
            if (subType.contains(attr)) {
                mapList.add(map);
            }
        }
        return mapList;
    }

    /**
     * 检查面料件    面料件+版本+材料+面料子类型是否存在
     *
     * @param context
     * @param templatName
     * @param templatRev
     * @return com.matrixone.apps.domain.util.MapList
     * @throws
     * @author LIUJR
     * @date 2025/2/26 16:20
     * @description
     */
    public MapList checkTrimPartExist(Context context, String templatName, String templatRev, String JF_Fabric) throws Exception {
        StringList selList = JF_Util_mxJPO.basicBolistSel();
        String where = "attribute[EnterpriseExtension.V_PartNumber]=='" + templatName + "' && revision=='" + templatRev + "' && attribute[JF_VPMReference.JF_Fabric]=='" + JF_Fabric + "' && attribute[JF_VPMReference.JF_PartSubType]==T03";
        MapList list = DomainObject.findObjects(context, JF_PLMConstants_mxJPO.TYPE_VPMReference, "*", where, selList);
        return list;
    }

    /*
     * @description:获取零件ID
     * @author: caipan
     * @date:
     * @param: * @param[1] context
     * @param[2] templatName
     * @param[3] templatRev
     * @return:
     **/
    public String getPartId(Context context, String templatName, String templatRev) throws Exception {
        StringList selList = JF_Util_mxJPO.basicBolistSel();
        String where = "attribute[EnterpriseExtension.V_PartNumber]=='" + templatName + "' && revision=='" + templatRev + "'";
        MapList list = DomainObject.findObjects(context, JF_PLMConstants_mxJPO.TYPE_VPMReference, "*", where, selList);
        if (list.size() > 0) {
            return (String) ((Map) list.get(0)).get(DomainObject.SELECT_ID);
        }
        return "";
    }

    /*
     * @description:获取零件ID
     * @author: caipan
     * @date:
     * @param: * @param[1] context
     * @param[2] templatName
     * @param[3] templatRev
     * @param[4] JF_Fabric
     * @return:
     **/
    public String getPartAndAttrId(Context context, String templatName, String templatRev, String fabric, String subType) throws Exception {
        StringList selList = JF_Util_mxJPO.basicBolistSel();
        String where = "attribute[EnterpriseExtension.V_PartNumber]=='" + templatName + "' && revision=='" + templatRev + "' && attribute[JF_VPMReference.JF_PartSubType]=='" + subType + "'";
        /*if (UIUtil.isNotNullAndNotEmpty(fabric)) {
            where += " && attribute[JF_VPMReference.JF_Fabric]=='" + fabric + "'";
        }*/
        MapList list = DomainObject.findObjects(context, JF_PLMConstants_mxJPO.TYPE_VPMReference, "*", where, selList);
        if (list.size() > 0) {
            return (String) ((Map) list.get(0)).get(DomainObject.SELECT_ID);
        }
        return "";
    }

    /**
     * @param context
     * @param temp         行的值
     * @param attributeMap 属性值
     * @return java.util.Map
     * @throws
     * @author LIUJR
     * @date 2025/3/20 10:13
     * @description
     */
    public Map getRowDataTemplateIdAndTrimIdMap(Context context, Map temp, Map attributeMap) throws Exception {
        HashMap<String, String> returnMap = new HashMap<>();
        String templatePartRev = "";
        //样板件零件号
        String templatePartName = UIUtil.getValue(temp, UIUtil.getValue(attributeMap, "templateName"));//样版零件号
        StringList temNameList = FrameworkUtil.split(templatePartName, "_");
        if (temNameList.size() == 2) {
            templatePartName = temNameList.get(0);
            templatePartRev = temNameList.get(1);
        } else {
            templatePartName = "";
            templatePartRev = "";
        }
        //面料件零件号
        String trinName = UIUtil.getValue(temp, UIUtil.getValue(attributeMap, "JF_Trim"));//面料号
        //材料
        String group = UIUtil.getValue(temp, UIUtil.getValue(attributeMap, "group"));
        String key = trinName + "_" + group;
        String JF_TrimRev = "";
        temNameList = FrameworkUtil.split(trinName, "_");
        trinName = temNameList.get(0);
        JF_TrimRev = temNameList.get(1);

        //样板件号
        String templateId = getPartAndAttrId(context, templatePartName, templatePartRev, "", "T02");
        //面料号
        String trimId = getPartAndAttrId(context, trinName, JF_TrimRev, "", "T03");
        returnMap.put("templateId", templateId);
        returnMap.put("trimId", trimId);
        returnMap.put("key", key);
        return returnMap;
    }

    /**
     * 搭建样板包结构
     *
     * @param context
     * @param request
     * @param personName    owner
     * @param organization  organization
     * @param trimId        面料id
     * @param templateId    样板件id
     * @param key           面料的key
     * @param temp          行数
     * @param attributeMap  属性值
     * @param packageT06Map 存储样板包的集合
     * @param packageT03Map 存储面料件的id集合
     * @return void
     * @throws
     * @author LIUJR
     * @date 2025/3/20 10:26
     * @description
     */
    public String buildBOMT06Structure(Context context, HttpServletRequest request, String personName, String organization, String trimId, String templateId, String key, Map temp, Map attributeMap, Map packageT06Map, Map packageT03Map, MapList mapList) throws Exception {
        LOGGER.info("buildStructrure:{}", temp);
        String rel = JF_PLMConstants_mxJPO.REL_Instance;
        String strJF_Area = UIUtil.getValue(temp, UIUtil.getValue(attributeMap, "JF_Area"));//面积
        String strJF_Circumference = UIUtil.getValue(temp, UIUtil.getValue(attributeMap, "JF_Circumference"));//周长
        String strJF_PartDes = UIUtil.getValue(temp, UIUtil.getValue(attributeMap, "description"));//描述
        String strUsage = UIUtil.getValue(temp, UIUtil.getValue(attributeMap, "usage"));//用量
        DomainObject parentObj = DomainObject.newInstance(context);
        parentObj.setId(trimId);
        DomainObject subObj = DomainObject.newInstance(context);
        subObj.setId(templateId);
        //样板件添加面积和周长属性
        String mql = "print bus " + templateId + " select  attribute dump @ ;";
        String attr = MqlUtil.mqlCommand(context, false, mql, true);
        StringList attrList = FrameworkUtil.split(attr, "@");
        LOGGER.info("attrList:{}", attrList);
        Map subMap = new HashMap();
        if (attrList.contains("Circumference")) {
            if (UIUtil.isNotNullAndNotEmpty(strJF_Circumference)) {
                subMap.put("Circumference", strJF_Circumference);
            }
        }
        if (attrList.contains(JF_PLMConstants_mxJPO.ATTR_JF_Area)) {
            if (UIUtil.isNotNullAndNotEmpty(strJF_Area)) {
                subMap.put(JF_PLMConstants_mxJPO.ATTR_JF_Area, strJF_Area);
            }
        }
        if (attrList.contains("NetArea")) {
            if (UIUtil.isNotNullAndNotEmpty(strJF_Area)) {
                subMap.put("NetArea", strJF_Area);
            }
        }
        LOGGER.info("subMap:{}", subMap);
        //设置属性
        //设置子对象属性
        Map parentMap = new HashMap();
        //修改样板包属性  v_name = 样板件描述  v_desc = 面料号_分组
        parentMap.put("PLMEntity.V_description", key);
        parentMap.put("PLMEntity.V_Name", strJF_PartDes);
        subObj.setAttributeValues(context, subMap);
        //开始搭建结构，根据是否含有面料的样板包判断
        DomainObject domainObject = DomainObject.newInstance(context);
        String partT06Id = DomainConstants.EMPTY_STRING;
        if (packageT06Map.containsKey(key)) {
            // 如果有，就走关联流程1；找到样板件关联到样板包，去除没有的样板件
            String partT6Id = UIUtil.getValue(packageT06Map, key);
            domainObject.setId(partT6Id);
            partT06Id = partT6Id;
            Map map1 = new HashMap();
            map1.put("relName", JF_PLMConstants_mxJPO.REL_Instance);
            map1.put("fromId", partT6Id);
            map1.put("toId", templateId);
            String connId = JF_PublicMethodClass_mxJPO.getTwoBusinessObjectConnId(context, JPO.packArgs(map1));
            if (UIUtil.isNullOrEmpty(connId)) {
                //没有关系 需要连接
                DomainRelationship ship = domainObject.addToObject(context, new RelationshipType(rel), templateId);
                addInterface(context, ship.getPhysicalId(context));
                ship.setAttributeValue(context, JF_PLMConstants_mxJPO.ATTR_JF_Dosage, strUsage);
            } else {
                //有关系 需要改变用量的值
                //修改关系属性
                DomainRelationship ship = DomainRelationship.newInstance(context, connId);
                String attributeValue = ship.getAttributeValue(context, JF_PLMConstants_mxJPO.ATTR_JF_Dosage);
                if (!attributeValue.equalsIgnoreCase(strUsage)) {
                    ship.setAttributeValue(context, JF_PLMConstants_mxJPO.ATTR_JF_Dosage, strUsage);
                }
            }
            domainObject.setAttributeValues(context, parentMap);
        } else {
            // 如果没有走创建流程2；创建同材料的样板包，将样板件挂到样板包，并将面料关联到样板包
            //开始创建样板包
            partT06Id = createPartT06Object(context, trimId, request, personName, organization, key, strJF_PartDes);
            domainObject.setId(partT06Id);
            //关联样板件
            DomainRelationship ship = domainObject.addToObject(context, new RelationshipType(rel), templateId);
            addInterface(context, ship.getPhysicalId(context));
            ship.setAttributeValue(context, JF_PLMConstants_mxJPO.ATTR_JF_Dosage, strUsage);
            packageT06Map.put(key, partT06Id);
            packageT03Map.put(key, trimId);
        }

        HashMap hashMap = new HashMap();
        hashMap.put("templateId", templateId);
        hashMap.put("T06Id", partT06Id);
        mapList.add(hashMap);
        return partT06Id;
    }


    /*
     * @description: 搭建BOM关系
     * @author: caipan
     * @date:
     * @param: * @param[1] context
     * @param[2] temp
     * @param[3] attributeMapp
     * @return: StringList  返回样板件ID---后续拿到ID之后和面套总成关联
     **/
    public StringList buildEBOMStructure(Context context, Map temp, Map attributeMapp, MapList mapList, Map map, Map newTrimMap) throws Exception {
        StringList list = new StringList();
        String templatePartRev = "";
        LOGGER.info("!!!!!!!!!!!!!!map:{}", temp.toString());
        //样板件零件号
        String templatePartName = UIUtil.getValue(temp, UIUtil.getValue(attributeMapp, "templateName"));//样版零件号
        StringList temNameList = FrameworkUtil.split(templatePartName, "_");
        if (temNameList.size() == 2) {
            templatePartName = temNameList.get(0);
            templatePartRev = temNameList.get(1);
        } else {
            templatePartName = "";
            templatePartRev = "";
        }
        //面料件零件号
        String trinName = UIUtil.getValue(temp, UIUtil.getValue(attributeMapp, "JF_Trim"));//面料号
        String usage = UIUtil.getValue(temp, UIUtil.getValue(attributeMapp, "usage"));//用量
        String JF_Fabric = UIUtil.getValue(temp, UIUtil.getValue(attributeMapp, "JF_Fabric"));//材料
        String JF_TrimRev = "";
        temNameList = FrameworkUtil.split(trinName, "_");
        trinName = temNameList.get(0);
        JF_TrimRev = temNameList.get(1);
        String templateId = getPartAndAttrId(context, templatePartName, templatePartRev, "", "T02");
        String trinId = getPartAndAttrId(context, trinName, JF_TrimRev, JF_Fabric, "T03");
        if (UIUtil.isNullOrEmpty(trinId)) {
            String key = trinName + "_" + JF_TrimRev + "_" + JF_Fabric;
            if (newTrimMap.containsKey(key)) {
                trinId = (String) newTrimMap.get(key);
            }
        }
        LOGGER.info("templateId:{},trinId:{}", templateId, trinId);
        if (UIUtil.isNotNullAndNotEmpty(templateId) && UIUtil.isNotNullAndNotEmpty(trinId)) {
            //搭建关系 设置相关的属性
            buildStructrure(context, trinId, templateId, temp, attributeMapp);
            if (!list.contains(trinId)) {
                list.add(trinId);
            }
            //end by ljr
            HashMap hashMap = new HashMap();
            hashMap.put("templateId", templateId);
            hashMap.put("trinId", trinId);
            mapList.add(hashMap);
            //end
            map.put(templateId, usage);
        }
        return list;
    }

    /*
     * @description:建立关系，并且设置关联的属性
     * @author: caipan
     * @date:
     * @param: * @param[1] context
     * @param[2] parentId  父ID
     * @param[3] subId  子ID
     * @return:
     **/
    public void buildStructrure(Context context, String parentId, String subId, Map valueMap, Map attributeMapp) throws Exception {
        LOGGER.info("buildStructrure:{}", valueMap);
        String rel = JF_PLMConstants_mxJPO.REL_Instance;
        DomainObject parentObj = DomainObject.newInstance(context);
        parentObj.setId(parentId);

        DomainObject subObj = DomainObject.newInstance(context);
        subObj.setId(subId);
        //设置子对象属性
        Map parentMap = new HashMap();
        Map subMap = new HashMap();
        String JF_Area = UIUtil.getValue(valueMap, UIUtil.getValue(attributeMapp, "JF_Area"));//面积
        String JF_Circumference = UIUtil.getValue(valueMap, UIUtil.getValue(attributeMapp, "JF_Circumference"));//周长
        String JF_Fabric = UIUtil.getValue(valueMap, UIUtil.getValue(attributeMapp, "JF_Fabric"));//面料
        String JF_PartDes = UIUtil.getValue(valueMap, UIUtil.getValue(attributeMapp, "description"));//描述
        String strUsage = UIUtil.getValue(valueMap, UIUtil.getValue(attributeMapp, "usage"));//用量
        if (UIUtil.isNotNullAndNotEmpty(JF_Area)) {
            subMap.put(JF_PLMConstants_mxJPO.ATTR_JF_Area, JF_Area);
        }
        if (UIUtil.isNotNullAndNotEmpty(JF_Circumference)) {
            subMap.put(JF_PLMConstants_mxJPO.ATTR_JF_Circumference, JF_Circumference);
        }
        if (UIUtil.isNotNullAndNotEmpty(JF_PartDes)) {
            subMap.put(JF_PLMConstants_mxJPO.ATTR_JF_PartDes, JF_PartDes);
        }
        if (UIUtil.isNotNullAndNotEmpty(JF_Fabric)) {
            parentMap.put(JF_PLMConstants_mxJPO.ATTR_JF_Fabric, JF_Fabric);
        }
        parentObj.setAttributeValues(context, parentMap);
        subObj.setAttributeValues(context, subMap);
        //建立关系
        StringList subList = parentObj.getInfoList(context, "from[" + JF_PLMConstants_mxJPO.REL_Instance + "].to.id");
        if (!subList.contains(subId)) {
            DomainRelationship ship = parentObj.addToObject(context, new RelationshipType(rel), subId);
            addInterface(context, ship.getPhysicalId(context));
            ship.setAttributeValue(context, JF_PLMConstants_mxJPO.ATTR_JF_Dosage, strUsage);
        }
    }

    public void addInterface(Context context, String relId) throws Exception {
//				String interfaceStr = MqlUtil.mqlCommand(context, "print connection $1 select $2 dump $3",relId,"interface","@");
        String interfaceStr = MqlUtil.mqlCommand(context, false, "print connection '" + relId + "' select interface dump @ ", true);
        StringList interfaceResult = FrameworkUtil.split(interfaceStr, "@");
        String internaceName = "JF_VPMInstance";
        if (!interfaceResult.contains(internaceName)) {
            MqlUtil.mqlCommand(context, "mod connection $1 add $2 $3", relId, "interface", internaceName);//暂时注释 貌似可以自动增加
        }
    }

    /*
     * @description: 获取项目任务的级别
     * @author: caipan
     * @date:
     * @param: * @param[1] context
     * @param[2] ps  任务/项目关联的  Project Access List 权限对象
     * @param[3] taskPhyId
     * @return:
     **/
    public String getTaskWBSLevel(Context context, ProjectSequence ps, String taskPhyId) throws Exception {
        String wbsLevel = "0";
        Map<String, Dataobject> palSeqData = ps.getSequenceData(context);
        Dataobject taskObj = palSeqData.get(taskPhyId);
        wbsLevel = (String) taskObj.getDataelements().get(ProgramCentralConstants.KEY_WBS_ID);
        return wbsLevel;
    }

    /**
     * copy emxTaskBase
     * This Method will return MapList of expanded WBS of Project with seleced Expansion Level.
     * Added by OEF for IR-017626V6R2011 02/12/2009.
     *
     * @param context      Matrix Context object
     * @param objectId     ProjectId
     * @param relPattern   Relationship for Expansion
     * @param nExpandLevel Expansion Level of WBS
     * @return MapList of WBS with Level of Expansion
     * @throws Exception
     */
    protected MapList getWBSTasks(Context context, String objectId, String relPattern, short nExpandLevel) throws Exception {
        {
            long start = System.currentTimeMillis();
            MapList objectList = new MapList();

            try {
                ProjectSpace rootNodeObj = new ProjectSpace(objectId);

                StringList objectSelects = new StringList(5);
                objectSelects.addElement(DomainConstants.SELECT_ID);
                objectSelects.addElement(DomainConstants.SELECT_NAME);
                //objectSelects.addElement(SELECT_IS_PARENT_TASK_DELETED);
                //  objectSelects.addElement(DomainConstants.SELECT_POLICY);
                objectSelects.addElement(ProgramCentralConstants.SELECT_PHYSICALID);
                objectSelects.add(JF_PLMConstants_mxJPO.SELECT_ATTR_Task_Estimated_Start_Date);
                objectSelects.add(JF_PLMConstants_mxJPO.SELECT_ATTR_Task_Estimated_Finish_Date);
                objectSelects.add(JF_PLMConstants_mxJPO.SELECT_ATTR_Task_Estimated_Duration);
                objectSelects.add(JF_PLMConstants_mxJPO.SELECT_ATTR_ProjectRole);
                objectSelects.addElement(JF_PLMConstants_mxJPO.SELECT_ATTR_JF_Modules);
                objectSelects.addElement(JF_PLMConstants_mxJPO.SELECT_ATTR_JF_ESOType);
                objectSelects.add(DomainConstants.SELECT_CURRENT);
                objectSelects.add(DomainConstants.SELECT_TYPE);
                //Added for PCS-expand query
                objectList = rootNodeObj.getObjectListFromPAL(context, objectSelects, nExpandLevel);
                if (relPattern != null && relPattern.contains(DomainConstants.RELATIONSHIP_DELETED_SUBTASK)) {
                    //objectSelects.add(ProgramCentralConstants.SELECT_IS_SUMMARY_TASK);
                    StringList relationshipSelects = new StringList(2);
                    relationshipSelects.addElement(DomainConstants.SELECT_RELATIONSHIP_ID);
                    relationshipSelects.addElement(DomainConstants.SELECT_LEVEL);
                    MapList streamDataList = rootNodeObj.getRelatedObjects(context,
                            DomainConstants.RELATIONSHIP_DELETED_SUBTASK,
                            ProgramCentralConstants.TYPE_PROJECT_MANAGEMENT,
                            objectSelects,
                            relationshipSelects,
                            false,
                            true,
                            nExpandLevel,
                            null,
                            null,
                            0);
                    for (int i = 0, size = streamDataList.size(); i < size; i++) {
                        Map taskMap = (Map) streamDataList.get(i);

                        String isSummary = (String) taskMap.get(ProgramCentralConstants.SELECT_IS_SUMMARY_TASK);
                        //taskMap.put("hasChildren",isSummary);
                        taskMap.put("direction", "from");

                        objectList.add(taskMap);
                    }
                }

            } catch (Exception e) {
                //e.printStackTrace();
                DebugUtil.debug("getWBSTasks " + e.getMessage());
                throw e;
            } finally {
                DebugUtil.debug("Total time taken by expand program(getWBSTasks)::" + (System.currentTimeMillis() - start) + "ms");
                return objectList;
            }
        }
    }

    /*
     * @description: 获取两个日期相差的工作日天数
     * @author: caipan
     * @date:
     * @param: * @param[1] context
     * @param[2] startDate
     * @param[3] endDate
     * @return:
     **/
    public String getDurationDay(Context context, String startDate, String endDate) throws Exception {
        Long duration = 1L;
        Date startD = parseDateSystem(context, startDate);
        Date endD = parseDateSystem(context, endDate);
        DateUtil dateUtil = new DateUtil();
        duration = dateUtil.computeDuration(startD, endD);
        return duration.toString();
    }

    /*
     * @description: 比较时间大小
     * @author: caipan
     * @date:
     * @param: * @param[1] start
     * @param[2] end
     * @return:
     **/
    public boolean compareDate(Context context, String start, String end) throws Exception {
        Date startD = parseDate(context, start);
        Date endD = parseDate(context, end);
        if (startD.compareTo(endD) > 0) {
            return false;
        } else {
            return true;
        }

    }

    //校验父任务结束时间不能早于子任务结束时间，开始时间不能晚于子任务开始时间
    //子任务结束时间小于等于父任务的结束时间，子任务的开始时间大于等于父任务的开始时间
    public boolean checkParentDate(Context context, MapList mapList, int i, String startDate, String endDate, String level) {
        boolean flag = true;
        try {
            Map temp;
            int strLevel;
            String parentStartDate, parentFinishDate;
            int L = Integer.parseInt(level) - 1;//父的层级
            Map<String, String> attributeMapping = setColAttributeMapping();
            for (int j = i - 1; j < mapList.size(); j--) {
                temp = (Map) mapList.get(j);
                strLevel = Integer.parseInt(UIUtil.getValue(temp, attributeMapping.get("Level")));
                parentStartDate = UIUtil.getValue(temp, attributeMapping.get("startDate"));
                parentFinishDate = UIUtil.getValue(temp, attributeMapping.get("FinishDate"));

                LOGGER.info("checkParentDate---strLevel->"+strLevel);
                LOGGER.info("checkParentDate---parentStartDate->"+parentStartDate);
                LOGGER.info("checkParentDate---parentFinishDate->"+parentFinishDate);
                LOGGER.info("checkParentDate---compareDate-end>"+compareDate(context, endDate, parentFinishDate));
                LOGGER.info("checkParentDate---compareDate-start>"+compareDate(context, parentStartDate, startDate));


                if (L == strLevel) {//等于父的层级 就比较时间
                    if (compareDate(context, endDate, parentFinishDate) && compareDate(context, parentStartDate, startDate)) {//父任务的结束时间大于等于子任务的结束时间  //父任务的开始时间小于等于子任务的开始时间
                        return true;
                    } else {
                        return false;
                    }
                }
            }
        } catch (Exception e) {
            flag = false;
        }
        return flag;
    }

    public boolean checkParentESODate(Context context, MapList mapList, int i, String startDate, String endDate, String level) {
        boolean flag = true;
        try {
            Map temp;
            int strLevel;
            String parentStartDate, parentFinishDate;
            int L = Integer.parseInt(level) - 1;//父的层级
            Map<String, String> attributeMapping = setESOColAttributeMapping();
            for (int j = i - 1; j < mapList.size(); j--) {
                temp = (Map) mapList.get(j);
                strLevel = Integer.parseInt(UIUtil.getValue(temp, attributeMapping.get("Level")));
                parentStartDate = UIUtil.getValue(temp, attributeMapping.get("startDate"));
                parentFinishDate = UIUtil.getValue(temp, attributeMapping.get("FinishDate"));

                if (L == strLevel) {//等于父的层级 就比较时间
                    if (compareDate(context, endDate, parentFinishDate) && compareDate(context, parentStartDate, startDate)) {//父任务的结束时间大于等于子任务的结束时间  //父任务的开始时间小于等于子任务的开始时间
                        return true;
                    } else {
                        return false;
                    }
                }
            }
        } catch (Exception e) {
            flag = false;
        }
        return flag;
    }

    public Map<String, String> setESOColAttributeMapping() {
        Map<String, String> attributeMap = new HashMap();
        attributeMap.put("maturity", "4");
        attributeMap.put("startDate", "7");
        attributeMap.put("FinishDate", "8");
        attributeMap.put("objectId", "10");
        attributeMap.put("Level", "0");
        return attributeMap;
    }

    public Map<String, String> setColAttributeMapping() {
        Map<String, String> attributeMap = new HashMap();
        attributeMap.put("role", "3");
        attributeMap.put("maturity", "5");
        attributeMap.put("startDate", "6");
        attributeMap.put("FinishDate", "7");
        attributeMap.put("objectId", "9");
        attributeMap.put("Level", "0");
        return attributeMap;
    }


    public Map<String, String> setTrimAttributeMapping() {
        Map<String, String> attributeMap = new HashMap();
        attributeMap.put("templateName", "0");
//        attributeMap.put("templateRev","1");
        attributeMap.put("usage", "1");
        attributeMap.put("JF_Area", "2");
        attributeMap.put("JF_Circumference", "3");
        attributeMap.put("group", "4");
        attributeMap.put("JF_Trim", "5");
        //add by ljr
        attributeMap.put("description", "6");
//        attributeMap.put( "JF_TrimRev","7");
        return attributeMap;
    }

    /*
     * @description:竞品BOM导出模版映射表
     * @author: caipan
     * @date:
     * @param:
     * @return:
     **/
    public StringList setCompetitiveAttributeMapping() {
        StringList attributeList = new StringList();
        attributeList.add(DomainConstants.SELECT_LEVEL);
        attributeList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JF_PartNameCN);
        attributeList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JF_PartNameEN);
        attributeList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JFPartType);
        attributeList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JF_Material);
        attributeList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JF_Lon);
        attributeList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JF_Wid);
        attributeList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JF_Hig);
        attributeList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JF_GramWeight);
        attributeList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JF_SurfaceTreatment);
        attributeList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JF_Weight);
        attributeList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JF_quantity);
        attributeList.add(DomainConstants.SELECT_DESCRIPTION);
        attributeList.add(DomainConstants.SELECT_OWNER);
        return attributeList;
    }

    /**
     * 下载模板
     *
     * @param context
     * @param args
     * @return org.apache.poi.ss.usermodel.Workbook
     * @throws
     * @author LIUJR
     * @date 2025/8/26 9:38
     * @description
     */
    public Map generateTemplate(Context context, String[] args) throws Exception {
        Map resMap = new HashMap();
        try {
            String fileName = args[0];
            String classPath = Thread.currentThread().getContextClassLoader().getResource("").getPath();
            String path = classPath.substring(0, classPath.indexOf("WEB-INF")) + "jf_template" + File.separator + fileName;
            InputStream inputStream = new FileInputStream(path);
            Workbook workbook = WorkbookFactory.create(inputStream);
            resMap.put("file", workbook);
            resMap.put("flag", "Y");
            resMap.put("fileName", fileName);
        } catch (Exception ex) {
            ex.printStackTrace();
            resMap.put("flag", "N");
            return resMap;
        }
        return resMap;
    }

    public Workbook generateTrimTemplate(Context context, String[] args) throws Exception {
        Workbook xssfWorkbook = null;
        try {
            String classPath = Thread.currentThread().getContextClassLoader().getResource("").getPath();
            String importFileName = "TrimImportBOMTemplate.xlsx";
            String path = classPath.substring(0, classPath.indexOf("WEB-INF")) + "jf_template" + File.separator + importFileName;
            InputStream inputStream = new FileInputStream(path);
            xssfWorkbook = WorkbookFactory.create(inputStream);
            return xssfWorkbook;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return xssfWorkbook;
    }

    /*
     * @description:判断是否是数字(可以输入小数)
     * @author: caipan
     * @date:
     * @param: * @param[1] str
     * @return:
     **/
    public boolean isNumber(String str) {
        Pattern p = Pattern.compile("[0-9]+(\\.[0-9]+)?");
        return p.matcher(str).matches();
    }

    /*
     * @description:导出竞品BOM
     * @author: caipan
     * @date:
     * @param: * @param[1] context
     * @param[2] args
     * @return:
     **/
    public XSSFWorkbook generateCompetitiveBOM(Context context, String[] args) throws Exception {
        Map map = JPO.unpackArgs(args);
        String objectId = UIUtil.getValue(map, DomainConstants.SELECT_ID);
        LOGGER.info("objectId:{}", objectId);//竞品BOM
        String classPath = Thread.currentThread().getContextClassLoader().getResource("").getPath();
        String importFileName = "CompetitiveBOMTemplate.xlsx";
        String path = classPath.substring(0, classPath.indexOf("WEB-INF")) + "jf_template" + File.separator + importFileName;
        File fs = new File(path);
        FileInputStream fis = new FileInputStream(fs);
        XSSFWorkbook XSSFwb = new XSSFWorkbook(fis);
        SXSSFWorkbook SXSSFwb = new SXSSFWorkbook(XSSFwb);
        XSSFWorkbook wb = SXSSFwb.getXSSFWorkbook();
        XSSFSheet sheet = wb.getSheetAt(0);

        CellStyle style = sheet.getRow(1).getCell(1).getCellStyle();
        //获取竞品所有层级的数据
        DomainObject obj = DomainObject.newInstance(context);
        obj.setId(objectId);


        if (!JF_PLMConstants_mxJPO.TYPE_JF_CompetitiveBOM.equalsIgnoreCase(obj.getInfo(context, DomainConstants.SELECT_TYPE))) {
            String errorNotice = EnoviaResourceBundle.getProperty(context, "emxProgramCentralStringResource", context.getLocale(), "emxProgramCentral.Command.JFDownLoadCompetitiveCmdMessage");
            XSSFRow rowm = sheet.createRow(1);
            XSSFCell cellRow = rowm.createCell(1);
            cellRow.setCellType(CellType.STRING);
            Font font = wb.createFont();
            font.setColor(IndexedColors.RED.getIndex());
            style.setFont(font);
            cellRow.setCellStyle(style);
            cellRow.setCellValue(errorNotice);
        } else {

            StringList objectSelects = JF_Util_mxJPO.basicBolistSel();

            objectSelects.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JF_PartNameCN);
            objectSelects.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JF_PartNameEN);
            objectSelects.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JFPartType);
            objectSelects.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JF_Material);
            objectSelects.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JF_Lon);
            objectSelects.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JF_Wid);
            objectSelects.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JF_Hig);
            objectSelects.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JF_GramWeight);
            objectSelects.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JF_SurfaceTreatment);
            objectSelects.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JF_Weight);
            objectSelects.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JF_quantity);
            objectSelects.add(DomainConstants.SELECT_DESCRIPTION);
            objectSelects.add(DomainConstants.SELECT_OWNER);

            StringList relationshipSelects = JF_Util_mxJPO.basicRellistSel();

            MapList objectList = obj.getRelatedObjects(context,
                    JF_PLMConstants_mxJPO.REL_Instance,
                    JF_PLMConstants_mxJPO.TYPE_JF_CompetitiveBOM,
                    objectSelects,
                    relationshipSelects,
                    false,
                    true,
                    (short) 0,
                    null,
                    null,
                    0);
            StringList attributeName = setCompetitiveAttributeMapping();

            Map rootMap = obj.getInfo(context, objectSelects);
            rootMap.put(DomainConstants.SELECT_LEVEL, "0");
            objectList.add(0, rootMap);
            XSSFRow rowm = null;
            XSSFCell cellRow = null;
            String partTypeAscc = "";
            String cellValue = "";
            for (int i = 0; i < objectList.size(); i++) {
                Map temp = (Map) objectList.get(i);
                rowm = sheet.createRow(i + 1);
                int k = 0;
                for (int j = 0; j < attributeName.size(); j++) {
                    cellRow = rowm.createCell(k);
                    k = k + 1;
                    cellRow.setCellType(CellType.STRING);
                    cellRow.setCellStyle(style);
                    cellValue = UIUtil.getValue(temp, attributeName.get(j));
                    if (JF_PLMConstants_mxJPO.SELECT_ATTR_JFPartType.equalsIgnoreCase(attributeName.get(j))) {//零件类型 需要翻译
                        cellRow.setCellValue(EnoviaResourceBundle.getRangeI18NString(context, JF_PLMConstants_mxJPO.ATTR_JFPartType, cellValue, context.getLocale().getLanguage()));
                    } else if (DomainConstants.SELECT_OWNER.equalsIgnoreCase(attributeName.get(j))) {//owner 显示fullName
                        cellRow.setCellValue(PersonUtil.getFullName(context, cellValue));
                    } else {
                        cellRow.setCellValue(cellValue);
                    }
                }
            }
        }
        return wb;
    }

    /**
     * 导入NewECR的受影响零件
     *
     * @param context
     * @param args
     * @return matrix.util.StringList
     * @throws
     * @author LIUJR
     * @date 2025/7/30 14:13
     * @description
     */
    public StringList importECRItemPart(Context context, String[] args) throws Exception {
        StringList errorList = new StringList();
        try {
            ContextUtil.pushContext(context);
            Map paramMap = (Map) JPO.unpackArgs(args);
            //上传的文件
            List files = (List) paramMap.get("files");
            String objectId = (String) paramMap.get("objectId");
            File file = (File) files.get(0);
            LOGGER.info("file:{}", file.getPath());
            LOGGER.info("file:{}", file.length());
            //todo  放入job中
            Job job = new Job("JF_NewECRService", "createECRAffectedItemsPart", new String[]{objectId, file.getPath()});
            job.setTitle("ECR零件导入");
            job.createAndSubmit(context);
            //返回信息
            errorList.add(ComponentsUIUtil.getI18NString(context, context.getLocale().toString(), "emxComponents.Mess.ImportECRPartList", new String[]{}));
        } catch (Exception e) {
            e.printStackTrace();
            return new StringList(e.getMessage());
        } finally {
            ContextUtil.popContext(context);
        }
        return errorList;
    }

    /*
     * @description:导出ESO任务
     * @author: caipan
     * @date: 2025/9/23 10:21:06
     * @param: * @param[1] context
     * @param[2] args
     * @return:
     **/
    public XSSFWorkbook generateProjectESOTask(Context context, String[] args) throws Exception {
        Map map = JPO.unpackArgs(args);
        String objectId = UIUtil.getValue(map, DomainConstants.SELECT_ID);
        LOGGER.info("objectId:{}", objectId);
        XSSFWorkbook xssfWorkbook = new XSSFWorkbook();
        try {
            DomainObject projectObj = DomainObject.newInstance(context);
            projectObj.setId(objectId);
            String mqlCmd = "print bus $1 select $2 $3 dump $4";
            String rootNodePALPhysicalId = MqlUtil.mqlCommand(context,
                    true,
                    true,
                    mqlCmd,
                    true,
                    objectId,
                    ProgramCentralConstants.SELECT_PAL_PHYSICALID_FROM_PROJECT,
                    ProgramCentralConstants.SELECT_PAL_PHYSICALID_FROM_TASK,
                    "|");
            ProjectSequence ps = new ProjectSequence(context, rootNodePALPhysicalId);
//            LOGGER.info("ps:{}",rootNodePALPhysicalId);
            StringList bolist = JF_Util_mxJPO.basicBolistSel();
            bolist.add(JF_PLMConstants_mxJPO.SELECT_ATTR_Task_Estimated_Start_Date);
            bolist.add(JF_PLMConstants_mxJPO.SELECT_ATTR_Task_Estimated_Finish_Date);
            MapList objectList = getWBSTasks(context, objectId, DomainConstants.RELATIONSHIP_SUBTASK, (short) 0);

            StringList objectSelects = new StringList(5);
            objectSelects.addElement(DomainConstants.SELECT_ID);
            objectSelects.addElement(DomainConstants.SELECT_NAME);
            //objectSelects.addElement(SELECT_IS_PARENT_TASK_DELETED);
            //  objectSelects.addElement(DomainConstants.SELECT_POLICY);
            objectSelects.addElement(ProgramCentralConstants.SELECT_PHYSICALID);
            objectSelects.addElement(JF_PLMConstants_mxJPO.SELECT_ATTR_Task_Estimated_Start_Date);
            objectSelects.addElement(JF_PLMConstants_mxJPO.SELECT_ATTR_Task_Estimated_Finish_Date);
            objectSelects.addElement(JF_PLMConstants_mxJPO.SELECT_ATTR_Task_Estimated_Duration);
            objectSelects.addElement(JF_PLMConstants_mxJPO.SELECT_ATTR_JF_Modules);
            objectSelects.addElement(JF_PLMConstants_mxJPO.SELECT_ATTR_JF_ESOType);
            objectSelects.addElement(DomainConstants.SELECT_CURRENT);
            objectSelects.addElement(DomainConstants.SELECT_TYPE);
            objectList.add(0, projectObj.getInfo(context, objectSelects));
            String[] rowName = {"Level", "Task Level", "Name", "Type", "maturity", "Modules", "ESO Type", "Task Estimated Start Date(Year/Month/Day)", "Task Estimated Finish Date((Year/Month/Day))", "Task Estimated Duration(Automatic Calculation)", "id(forbid Edit)"};
            String[] attributeName = {"level", "taskLevel", "name", "type", DomainConstants.SELECT_CURRENT, JF_PLMConstants_mxJPO.SELECT_ATTR_JF_Modules, JF_PLMConstants_mxJPO.SELECT_ATTR_JF_ESOType, JF_PLMConstants_mxJPO.SELECT_ATTR_Task_Estimated_Start_Date, JF_PLMConstants_mxJPO.SELECT_ATTR_Task_Estimated_Finish_Date, JF_PLMConstants_mxJPO.SELECT_ATTR_Task_Estimated_Duration, "id"};
            XSSFSheet sheet = xssfWorkbook.createSheet("sheet1");
            XSSFRow rowm = sheet.createRow(0);

            CellStyle headerStyle = xssfWorkbook.createCellStyle();
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            //拿取JF_Function
            Page pageAttributePopulation = new Page("SignTaskProperties_zh.xml");
            pageAttributePopulation.open(context);
            String strProperties = pageAttributePopulation.getContents(context);
            String strJF_Function = String.format("/configurations/configuration[@id='ESOTask']/%s/@Value", "JF_Function");
            InputStream inputStream = new ByteArrayInputStream(strProperties.getBytes("UTF-8"));
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(inputStream);
            strJF_Function = JF_ESO_mxJPO.findAttributeWithXPath(document, strJF_Function);
            LOGGER.info("");
            CellStyle esoStyle = xssfWorkbook.createCellStyle();
            // 2. 设置字体颜色为红色
            Font font = xssfWorkbook.createFont();
            font.setColor(IndexedColors.DARK_RED.getIndex()); // 使用 POI 内置的深红色颜色索引
            esoStyle.setFont(font);
            esoStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            XSSFColor lightRed = new XSSFColor(new java.awt.Color(255, 182, 193), null);
            esoStyle.setFillForegroundColor(lightRed);

            CellStyle headerDateStyle = xssfWorkbook.createCellStyle();
            headerDateStyle.setFillForegroundColor(IndexedColors.GREEN.getIndex());
            headerDateStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            // 创建日期格式
            CreationHelper createHelper = xssfWorkbook.getCreationHelper();
            CellStyle datecellStyle = xssfWorkbook.createCellStyle();
            datecellStyle.setDataFormat(createHelper.createDataFormat().getFormat("yyyy/MM/dd"));

            XSSFCell cellRowName = null;
            for (int i = 0; i < rowName.length; i++) {
                cellRowName = rowm.createCell(i);
                cellRowName.setCellType(CellType.STRING);
                cellRowName.setCellValue(rowName[i]);
                if (rowName[i].contains("Date")) {
                    cellRowName.setCellStyle(headerDateStyle);
                } else {
                    cellRowName.setCellStyle(headerStyle);
                }
                if (i != 0) {
                    sheet.setColumnWidth(i, 256 * 26);
                }
            }
            sheet.createFreezePane(0, 1, 0, 1);
//            sheet.setDefaultColumnStyle(5, datecellStyle);
//            sheet.setDefaultColumnStyle(6, datecellStyle);
            for (int i = 0; i < objectList.size(); i++) {
                Map temp = (Map) objectList.get(i);
                rowm = sheet.createRow(i + 1);
                int rowNum = i + 2;
                Boolean esoFlag = Boolean.FALSE;
                for (int j = 0; j < attributeName.length; j++) {
                    cellRowName = rowm.createCell(j);
                    if (attributeName[j].contains("taskLevel")) {
                        //获取任务层级
                        cellRowName.setCellType(CellType.STRING);
                        cellRowName.setCellValue(getTaskWBSLevel(context, ps, UIUtil.getValue(temp, ProgramCentralConstants.SELECT_PHYSICALID)));
                    } else if (attributeName[j].contains(JF_PLMConstants_mxJPO.SELECT_ATTR_Task_Estimated_Duration)) {
//                        cellRowName.setCellType(CellType.STRING);
                        String formula = "NETWORKDAYS(H" + rowNum + ",I" + rowNum + ")";
                        cellRowName.setCellFormula(formula);
                    } else {
                        if (!attributeName[j].contains("Date")) {
                            cellRowName.setCellType(CellType.STRING);
                            cellRowName.setCellValue(UIUtil.getValue(temp, attributeName[j]));

                        } else {
                            cellRowName.setCellType(CellType.STRING);
                            //需要格式化日期---去掉时分秒
//                        cellRowName.setCellStyle(datecellStyle);
                            String date = UIUtil.getValue(temp, attributeName[j]).split(" ")[0];
//                        cellRowName.setCellValue(formatDate(context, date));
                            cellRowName.setCellValue(createHelper.createRichTextString(formatDate(context, date)));
                        }
                    }
                    if (i == 0) {
                        cellRowName.setCellStyle(headerStyle);
                    }
                    if ("name".equalsIgnoreCase(attributeName[j]) || "type".equalsIgnoreCase(attributeName[j])) {
                        CellType cellType = cellRowName.getCellType();
                        if (CellType.STRING.equals(cellType)) {
                            String stringCellValue = cellRowName.getStringCellValue();
                            if (strJF_Function.contains(stringCellValue) || JF_PLMConstants_mxJPO.TYPE_JF_ESOTask.equalsIgnoreCase(stringCellValue)) {
                                cellRowName.setCellStyle(esoStyle);
                                esoFlag = Boolean.TRUE;
                            }
                        }
                    } else if (JF_PLMConstants_mxJPO.SELECT_ATTR_JF_Modules.equalsIgnoreCase(attributeName[j]) || JF_PLMConstants_mxJPO.SELECT_ATTR_JF_ESOType.equalsIgnoreCase(attributeName[j])) {
                        if (esoFlag) {
                            cellRowName.setCellStyle(esoStyle);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return xssfWorkbook;
    }

    /*
     * @description:导入ESO任务时间，根节点不修改
     * @author: caipan
     * @date: 2025/9/23 11:14:31
     * @param: * @param[1] context
     * @param[2] args
     * @return:
     **/
    public StringList importProjectESOTask(Context context, String[] args) throws Exception {
        String strReturnMess = "";
        StringList errorList = new StringList();
        Locale sLanguage = context.getLocale();
        try {
            ContextUtil.pushContext(context);

            Map paramMap = (Map) JPO.unpackArgs(args);
            //国际化语言
            String strLanguage = (String) paramMap.get("language");
            //国际化文件的区
            String sSuiteKey = (String) paramMap.get("suiteKey");
            String parentId = (String) paramMap.get("objectId");
            //上传的文件
            List files = (List) paramMap.get("files");
            //workspce文件地址
            String workspace = context.createWorkspace();
            Iterator iteratorFile = files.iterator();
            Boolean fileIsExcel = true;
            LOGGER.info("====开始校验上传文档的内容=======");
            //拿取文件数据和检验
            Iterator iterator = files.iterator();
            int index;
            String sFileName = "";
            File file = null;
            File outfile = null;
            String outFilePath = "";
            String strPartId = null;
            //文件类型
            String strContentType = "";
            //存放所有校验都通过后，需要创建的对象的信息集合
            MapList mapAllTierXList = new MapList();
            //存放文件中每行数据对象的唯一标识，以便检验确定唯一性
            StringList rowTierXList = new StringList();
            //存放系统中所有TierX 的唯一标识，以便校验确定唯一性

            //当个文件校验
            Boolean fileSuccess = true;
            //所有文件校验
            Boolean allFileSuccess = true;
            //存放异常时需要下载的文件

            String id, EstimatedStartDate, EstimatedEndDate;
            while (iterator.hasNext()) {
                file = (File) iterator.next();
                //判断当前文件名：由于各个浏览器的不同，可能会包含路径
                sFileName = file.getName();
                if (sFileName.contains("/")) {
                    index = sFileName.lastIndexOf("/");
                    sFileName = sFileName.substring(index);
                }
                if (sFileName.contains("\\")) {
                    index = sFileName.lastIndexOf("\\");
                    sFileName = sFileName.substring(index + 1);
                }
                outFilePath = workspace + sFileName;
                if (new File(outFilePath).exists()) {
                    File file1 = new File(outFilePath);
                    file1.delete();
                }
                //暂存文件
                outfile = new File(outFilePath);
                //写入暂存文件
                FileUtils.copyFile(file, outfile);
                //读取excel文件中的数据
                InputStream inputStream = new FileInputStream(outFilePath);
                Workbook workbook = WorkbookFactory.create(inputStream);
                Sheet sheet = workbook.getSheetAt(0);
                MapList alldataList = JF_ExcelUtils_mxJPO.getMapList((XSSFSheet) sheet, 2, true, false);//调用工具类读取里面的内容
                LOGGER.info("alldataList:{}", alldataList);
                //设置列和属性对应
                Map<String, String> attributeMap = setESOColAttributeMapping();
                Map temp = null;
                Map valueMap = new HashMap();
                String startDate;
                String FinishDate;
                String RowNum;
                String objectId;
                String Level;
//                LOGGER.info("addDataList:{}", alldataList);
                //全部检查一遍是否有问题
                String errorNotice = EnoviaResourceBundle.getProperty(context, "emxProgramCentralStringResource", sLanguage, "emxProgramCentral.import.Notice1");
                String errorNotice2 = EnoviaResourceBundle.getProperty(context, "emxProgramCentralStringResource", sLanguage, "emxProgramCentral.import.Notice2");
                String errorNotice3 = EnoviaResourceBundle.getProperty(context, "emxProgramCentralStringResource", sLanguage, "emxProgramCentral.import.Notice3");
                String errorNotice4 = EnoviaResourceBundle.getProperty(context, "emxProgramCentralStringResource", sLanguage, "emxProgramCentral.import.Notice4");
                String errorNotice5 = EnoviaResourceBundle.getProperty(context, "emxProgramCentralStringResource", sLanguage, "emxProgramCentral.import.Notice5");
                DomainObject parentObj = DomainObject.newInstance(context, parentId);
                String parentStart = parentObj.getAttributeValue(context, "Task Estimated Start Date");
                String parentFinish = parentObj.getAttributeValue(context, "Task Estimated Finish Date");
                //格式化日期
                parentStart = formatDate(context, parentStart);
                parentFinish = formatDate(context, parentFinish);
//                LOGGER.info("alldataList:{}",alldataList);
                for (int i = 0; i < alldataList.size(); i++) {
                    StringBuffer errorStr = new StringBuffer();
                    temp = (Map) alldataList.get(i);
                    Level = UIUtil.getValue(temp, attributeMap.get("Level"));
                    startDate = UIUtil.getValue(temp, attributeMap.get("startDate"));
                    FinishDate = UIUtil.getValue(temp, attributeMap.get("FinishDate"));
                    objectId = UIUtil.getValue(temp, attributeMap.get("objectId"));
                    RowNum = UIUtil.getValue(temp, "RowNum");
                    if (UIUtil.isNullOrEmpty(objectId) || UIUtil.isNullOrEmpty(startDate) || !checkDateFormat(context, startDate) || UIUtil.isNullOrEmpty(FinishDate) || !checkDateFormat(context, FinishDate)) {
                        errorStr.append(errorNotice5 + " ");
                        errorStr.append(RowNum);
                        errorStr.append(" " + errorNotice4);
                        errorStr.append(errorNotice);
                        errorList.add(errorStr.toString());
                    }
                    errorStr.setLength(0);
                    if (!compareDate(context, startDate, FinishDate)) {//开始时间大于结束时间报错
                        if (errorStr.length() == 0) {
                            errorStr.append(errorNotice5 + " ");
                            errorStr.append(RowNum);
                            errorStr.append(" " + errorNotice4);
                        }
                        errorStr.append(errorNotice2);
                        errorList.add(errorStr.toString());
                    }
                    errorStr.setLength(0);
                    if ("1".equalsIgnoreCase(Level)) {
                        //ESO下级任务直接和ESO任务比较时间
                        if (!(compareDate(context, FinishDate, parentFinish) && compareDate(context, parentStart, startDate))) {//父任务的结束时间大于等于子任务的结束时间  //父任务的开始时间小于等于子任务的开始时间
                            if (errorStr.length() == 0) {
                                errorStr.append(errorNotice5 + " ");
                                errorStr.append(RowNum);
                                errorStr.append(" " + errorNotice4);
                            }
                            errorStr.append(errorNotice3);
                            errorList.add(errorStr.toString());
                        }
                    } else {
                        if (!checkParentESODate(context, alldataList, i, startDate, FinishDate, Level)) {//校验父任务结束时间不能早于子任务结束时间，开始时间不能晚于子任务开始时间
                            if (errorStr.length() == 0) {
                                errorStr.append(errorNotice5 + " ");
                                errorStr.append(RowNum);
                                errorStr.append(" " + errorNotice4);
                            }
                            errorStr.append(errorNotice3);
                            errorList.add(errorStr.toString());
                        }
                    }
                }
                LOGGER.info("errorList:{}", errorList);
                valueMap.clear();
                //如果没有问题
                ContextUtil.startTransaction(context, true);
                LOGGER.info("alldataList.size:{}", alldataList.size());
                if (errorList.size() == 0) {
                    for (int i = 0; i < alldataList.size(); i++) {
                        temp = (Map) alldataList.get(i);
                        startDate = UIUtil.getValue(temp, attributeMap.get("startDate"));
                        FinishDate = UIUtil.getValue(temp, attributeMap.get("FinishDate"));
                        startDate = formatDateToSystem(context, startDate);
                        FinishDate = formatDateToSystem(context, FinishDate);
                        valueMap.put(JF_PLMConstants_mxJPO.ATTR_Task_Estimated_Start_Date, startDate);
                        valueMap.put(JF_PLMConstants_mxJPO.ATTR_Task_Estimated_Finish_Date, FinishDate);
                        valueMap.put(JF_PLMConstants_mxJPO.ATTR_Task_Estimated_Duration, getDurationDay(context, startDate, FinishDate));//持续时间 需要计算
                        if (valueMap.size() > 0) {
                            setEstimatedDate(context, valueMap, UIUtil.getValue(temp, attributeMap.get("objectId")));
                        }
                    }
                    ContextUtil.commitTransaction(context);
                    return errorList;
                } else {
                    ContextUtil.abortTransaction(context);
                    return errorList;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            ContextUtil.abortTransaction(context);
            errorList.add(e.getMessage());
            return errorList;
        } finally {
            ContextUtil.popContext(context);
        }
        return errorList;
    }

    /**
     * 导出partList 清单
     *
     * @param context
     * @param args
     * @return org.apache.poi.xssf.usermodel.XSSFWorkbook
     * @throws
     * @author LIUJR
     * @date 2025/10/27 15:11
     * @description
     */
    public XSSFWorkbook generatePartLists(Context context, String[] args) throws Exception {
        Map paramsMap = JPO.unpackArgs(args);
        String objectId = UIUtil.getValue(paramsMap, DomainConstants.SELECT_ID);
        LOGGER.info("objectId:{}", objectId);
        String user = context.getUser();
        XSSFWorkbook xssfWorkbook = new XSSFWorkbook();
        try {
            DomainObject partListObj = DomainObject.newInstance(context);
            partListObj.setId(objectId);
            String current = partListObj.getInfo(context, DomainConstants.SELECT_CURRENT);
            String owner = partListObj.getInfo(context, DomainConstants.SELECT_OWNER);
            //projectId
            String projectId = partListObj.getInfo(context, "to[JFProject2PartList].from.id");
            DomainObject projectObject = DomainObject.newInstance(context, projectId);
            MapList maps = projectObject.getRelatedObjects(context,
                    DomainConstants.RELATIONSHIP_MEMBER, // relationship pattern
                    DomainConstants.TYPE_PERSON,                                    // object pattern
                    StringList.create(DomainConstants.SELECT_NAME, DomainConstants.SELECT_ID),                            // object selects
                    StringList.create(DomainRelationship.SELECT_ID, JF_PLMConstants_mxJPO.SELECT_ATTR_PROJECT_ROLE), // relationship selects
                    false,                                        // to direction
                    true,                                        // from direction
                    (short) 1,                                    // recursion level
                    DomainConstants.SELECT_NAME + "=='" + user + "'",                // object where clause
                    "",
                    (short) 0);
            if (maps.isEmpty()) {
                return xssfWorkbook;
            }
            Map map = (Map) maps.get(0);
            String role = UIUtil.getValue(map, JF_PLMConstants_mxJPO.SELECT_ATTR_PROJECT_ROLE);
            //判断当前角色是项目经理，商务代表，还是SQD，决定导出的内容
            //列名
            String strTypeWhere = "";
            StringList rowNameList = StringList.create("Level", "Part Number", "Part Type", "Revision", "Current", "Chinese Name", "English Name", "DirectBuy");
            StringList attributeNameList = StringList.create("level", JF_PLMConstants_mxJPO.SELECT_ATTR_V_PART_NUMBER, JF_PLMConstants_mxJPO.SELECT_ATTR_JF_PartType,
                    DomainConstants.SELECT_REVISION, DomainConstants.SELECT_CURRENT, JF_PLMConstants_mxJPO.SELECT_ATTR_JF_PartNameCN,
                    JF_PLMConstants_mxJPO.SELECT_ATTR_JF_PartNameEN, JF_PLMConstants_mxJPO.SELECT_ATTR_JFDIRECT_BUY);
            StringList rowTitleList = new StringList("Level");
            rowTitleList.add(EnoviaResourceBundle.getProperty(context, "emxFrameworkStringResource", context.getLocale(), "emxFramework.Attribute.EnterpriseExtension.V_PartNumber"));
            rowTitleList.add(EnoviaResourceBundle.getProperty(context, "emxFrameworkStringResource", context.getLocale(), "emxFramework.Attribute.JF_VPMReference.JF_PartType"));
            rowTitleList.add(EnoviaResourceBundle.getProperty(context, "emxFrameworkStringResource", context.getLocale(), "emxFramework.ObjectCompare.revision"));
            rowTitleList.add(EnoviaResourceBundle.getProperty(context, "emxFrameworkStringResource", context.getLocale(), "emxFramework.Basic.Current"));
            rowTitleList.add(EnoviaResourceBundle.getProperty(context, "emxFrameworkStringResource", context.getLocale(), "emxFramework.Attribute.JF_VPMReference.JF_PartNameCN"));
            rowTitleList.add(EnoviaResourceBundle.getProperty(context, "emxFrameworkStringResource", context.getLocale(), "emxFramework.Attribute.JF_VPMReference.JF_PartNameEN"));
            rowTitleList.add("DirectBuy");
            Boolean isEdit = Boolean.FALSE;
            StringList typeSelectList = JF_Util_mxJPO.basicBolistSel();
            typeSelectList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_V_PART_NUMBER);
            typeSelectList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JF_PartType);
            typeSelectList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JF_PartNameCN);
            typeSelectList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JF_PartNameEN);
            typeSelectList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JFDIRECT_BUY);
            StringList basicRellistSel = JF_Util_mxJPO.basicRellistSel();
            if (JF_PLMConstants_mxJPO.ATTR_PROJECT_ROLE_Range_PM.equalsIgnoreCase(role)) {
                if ("InWork".equalsIgnoreCase(current) && user.equalsIgnoreCase(owner)) {
                    isEdit = Boolean.TRUE;
                }
                rowNameList.addAll(StringList.create("Procurement Group(editable)",
                        "BicycleUsage(editable)", "TotalDemand(editable)", "DeliveryDate(YYYY/MM/DD editable)", "Plan Date(YYYY/MM/DD editable)",
                        "Draw Data Completion Plan Date(YYYY/MM/DD editable)", "3D Released Designated Plan Date(YYYY/MM/DD editable)"));
                attributeNameList.addAll(StringList.create(JF_PLMConstants_mxJPO.SELECT_ATTR_JF_VPMReference_JF_ProcurementGroup,
                        JF_PLMConstants_mxJPO.SELECT_ATTR_JFBicycleUsage, JF_PLMConstants_mxJPO.SELECT_ATTR_JFTotalDemand,
                        JF_PLMConstants_mxJPO.SELECT_ATTR_JFDeliveryDate,
                        JF_PLMConstants_mxJPO.SELECT_ATTR_JFPartRequirementPlanDate,
                        JF_PLMConstants_mxJPO.SELECT_ATTR_JFDrawDataCompletionPlanDate,
                        JF_PLMConstants_mxJPO.SELECT_ATTR_JF3DReleasedDesignatedPlanDate));
                rowTitleList.add(EnoviaResourceBundle.getProperty(context, "emxFrameworkStringResource", context.getLocale(), "emxFramework.Attribute.JF_VPMReference.JF_ProcurementGroup"));
                rowTitleList.add(EnoviaResourceBundle.getProperty(context, "emxFrameworkStringResource", context.getLocale(), "emxFramework.Attribute.JFBicycleUsage"));
                rowTitleList.add(EnoviaResourceBundle.getProperty(context, "emxFrameworkStringResource", context.getLocale(), "emxFramework.Attribute.JFTotalDemand"));
                rowTitleList.add(EnoviaResourceBundle.getProperty(context, "emxFrameworkStringResource", context.getLocale(), "emxFramework.Attribute.JFDeliveryDate"));
                rowTitleList.add(EnoviaResourceBundle.getProperty(context, "emxFrameworkStringResource", context.getLocale(), "emxFramework.Attribute.JFPartRequirementPlanDate"));
                rowTitleList.add(EnoviaResourceBundle.getProperty(context, "emxFrameworkStringResource", context.getLocale(), "emxFramework.Attribute.JFDrawDataCompletionPlanDate"));
                rowTitleList.add(EnoviaResourceBundle.getProperty(context, "emxFrameworkStringResource", context.getLocale(), "emxFramework.Attribute.JF3DReleasedDesignatedPlanDate"));
                typeSelectList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JF_VPMReference_JF_ProcurementGroup);
                basicRellistSel.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JFBicycleUsage);
                basicRellistSel.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JFTotalDemand);
                basicRellistSel.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JFDeliveryDate);
                basicRellistSel.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JFPartRequirementPlanDate);
                basicRellistSel.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JFDrawDataCompletionPlanDate);
                basicRellistSel.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JF3DReleasedDesignatedPlanDate);
            } else if (JF_PLMConstants_mxJPO.ATTR_PROJECT_ROLE_Range_BU.equalsIgnoreCase(role)) {
                //商务需要过滤DirectBuy为Y的零件
                strTypeWhere = "attribute[JF_VPMReference.JF_DirectBuy]=='consignment' || attribute[JF_VPMReference.JF_DirectBuy]=='direct-buy'";
                if ("Review".equalsIgnoreCase(current)) {
                    isEdit = Boolean.TRUE;
                }
                rowNameList.addAll(StringList.create("JFPartSupplierInfo(editable)"));
                attributeNameList.addAll(StringList.create("attribute[JFPartSupplierInfo]"));
                basicRellistSel.add("attribute[JFPartSupplierInfo]");
                rowTitleList.add(EnoviaResourceBundle.getProperty(context, "emxFrameworkStringResource", context.getLocale(), "emxFramework.Attribute.JFPartSupplierInfo"));
            } else if (JF_PLMConstants_mxJPO.ATTR_PROJECTROLE_RANGE_SQDRepresentative.equalsIgnoreCase(role)) {
                if ("Review".equalsIgnoreCase(current)) {
                    isEdit = Boolean.TRUE;
                }
                rowNameList.addAll(StringList.create("Plan Date", "Commitment Date(YYYY/MM/DD editable)"));
                attributeNameList.addAll(StringList.create(JF_PLMConstants_mxJPO.SELECT_ATTR_JFPartRequirementPlanDate, "attribute[JFPartRequirementCommitmentDate]"));
                basicRellistSel.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JFPartRequirementPlanDate);
                basicRellistSel.add("attribute[JFPartRequirementCommitmentDate]");
                rowTitleList.add(EnoviaResourceBundle.getProperty(context, "emxFrameworkStringResource", context.getLocale(), "emxFramework.Attribute.JFPartRequirementPlanDate"));
                rowTitleList.add(EnoviaResourceBundle.getProperty(context, "emxFrameworkStringResource", context.getLocale(), "emxFramework.Attribute.JFPartRequirementCommitmentDate"));
            } else {
                return xssfWorkbook;
            }
            rowNameList.add("Oid(forbid Edit)");
            rowNameList.add("Rid(forbid Edit)");
            attributeNameList.add(DomainConstants.SELECT_ID);
            attributeNameList.add(DomainRelationship.SELECT_ID);
            rowTitleList.add("Oid(forbid Edit)");
            rowTitleList.add("Rid(forbid Edit)");
            LOGGER.info("rowNameList:{}", rowNameList);
            LOGGER.info("attributeNameList:{}", attributeNameList);
            //查询数据
            MapList objectList = partListObj.getRelatedObjects(context,
                    JF_PLMConstants_mxJPO.rel_JFPartList2VPMReference, // relationship pattern
                    JF_PLMConstants_mxJPO.TYPE_VPMReference,                                    // object pattern
                    typeSelectList,                            // object selects
                    basicRellistSel, // relationship selects
                    false,                                        // to direction
                    true,                                        // from direction
                    (short) 1,                                    // recursion level
                    strTypeWhere,                // object where clause
                    "",
                    (short) 0);
            LOGGER.info("objectList:{}", objectList);
            //设置单元格边框
            CellStyle cellBorderStyle = xssfWorkbook.createCellStyle();
            cellBorderStyle.setBorderTop(BorderStyle.THIN);
            cellBorderStyle.setBorderBottom(BorderStyle.THIN);
            cellBorderStyle.setBorderLeft(BorderStyle.THIN);
            cellBorderStyle.setBorderRight(BorderStyle.THIN);
            cellBorderStyle.setAlignment(HorizontalAlignment.CENTER); // 水平居中
            cellBorderStyle.setVerticalAlignment(VerticalAlignment.CENTER); // 垂直居中
            //导出样式设置
            CellStyle headerStyle = xssfWorkbook.createCellStyle();
            headerStyle.cloneStyleFrom(cellBorderStyle);
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            CreationHelper createHelper = xssfWorkbook.getCreationHelper();
            //日期样式
            CellStyle headerDateStyle = xssfWorkbook.createCellStyle();
            headerDateStyle.cloneStyleFrom(cellBorderStyle);
            headerDateStyle.setFillForegroundColor(IndexedColors.GREEN.getIndex());
            headerDateStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            XSSFCell cellRowName = null;
            XSSFCell cellRowTitle = null;
            XSSFSheet sheet = xssfWorkbook.createSheet("sheet1");
            XSSFRow xssRowTitle = sheet.createRow(0);
            XSSFRow xssRowName = sheet.createRow(1);
            int num = 8;
            if (JF_PLMConstants_mxJPO.ATTR_PROJECTROLE_RANGE_SQDRepresentative.equalsIgnoreCase(role)) {
                num = 9;
            }
            //标题头信息
            for (int i = 0; i < rowNameList.size(); i++) {
                cellRowTitle = xssRowTitle.createCell(i);
                cellRowTitle.setCellType(CellType.STRING);
                cellRowTitle.setCellValue(rowTitleList.get(i));
                cellRowName = xssRowName.createCell(i);
                cellRowName.setCellType(CellType.STRING);
                cellRowName.setCellValue(rowNameList.get(i));
                if (i >= num && i < rowNameList.size() - 2) {
                    cellRowTitle.setCellStyle(headerDateStyle);
                    cellRowName.setCellStyle(headerDateStyle);
                } else {
                    cellRowTitle.setCellStyle(headerStyle);
                    cellRowName.setCellStyle(headerStyle);
                }
                if (i >= 8) {
                    if (rowNameList.get(i).contains("Date")) {
                        sheet.setColumnWidth(i, 330 * 26);
                    } else {
                        sheet.setColumnWidth(i, 280 * 26);
                    }
                } else {
                    sheet.setColumnWidth(i, 200 * 26);
                }
            }
            sheet.createFreezePane(0, 2, 0, 2);
            int line = 2;
            for (int i = 0; i < objectList.size(); i++) {
                Map temp = (Map) objectList.get(i);
                xssRowName = sheet.createRow(line);
                line++;
                for (int j = 0; j < attributeNameList.size(); j++) {
                    cellRowName = xssRowName.createCell(j);
                    String strAttrName = attributeNameList.get(j);
                    if (!strAttrName.contains("Date")) {
                        cellRowName.setCellType(CellType.STRING);
                        String value = UIUtil.getValue(temp, strAttrName);
                        if (j == 2) {
                            //国际化
                            value = EnoviaResourceBundle.getRangeI18NString(context, "JF_VPMReference.JF_PartType", value, context.getLocale().toString());
                        } else if (j == 4) {
                            value = EnoviaResourceBundle.getStateI18NString(context, "VPLM_SMB_Definition_MajorRev", value, context.getLocale().toString());
                        }
                        cellRowName.setCellValue(value);
                    } else {
                        cellRowName.setCellType(CellType.STRING);
                        //需要格式化日期---去掉时分秒
                        String date = UIUtil.getValue(temp, strAttrName).split(" ")[0];
                        if (UIUtil.isNotNullAndNotEmpty(date)) {
                            cellRowName.setCellValue(createHelper.createRichTextString(formatDate(context, date)));
                        } else {
                            cellRowName.setCellValue(createHelper.createRichTextString(date));
                        }
                    }
                    cellRowName.setCellStyle(cellBorderStyle);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return xssfWorkbook;
    }

    /**
     * 上传partList 的清单修改属性
     * SQD上传 承诺日期不能晚于需求日期
     * PM上传  采购分组无值才可以修改值
     *
     * @param context
     * @param args
     * @return java.lang.String
     * @throws
     * @description
     * @author LiuJR
     * @date 2023/12/14 13:39
     */
    public StringList importJFPartLists(Context context, String[] args) throws Exception {
        String strReturnMess = "";
        StringList errorList = new StringList();
        Locale sLanguage = context.getLocale();
        try {
            ContextUtil.pushContext(context);

            Map paramMap = (Map) JPO.unpackArgs(args);
            //上传的文件
            List files = (List) paramMap.get("files");
            //workspce文件地址
            String workspace = context.createWorkspace();
            LOGGER.info("====开始校验上传文档的内容=======");
            //拿取文件数据和检验
            Iterator iterator = files.iterator();
            int index;
            String sFileName = "";
            File file = null;
            File outfile = null;
            String outFilePath = "";
            while (iterator.hasNext()) {
                file = (File) iterator.next();
                //判断当前文件名：由于各个浏览器的不同，可能会包含路径
                sFileName = file.getName();
                if (sFileName.contains("/")) {
                    index = sFileName.lastIndexOf("/");
                    sFileName = sFileName.substring(index);
                }
                if (sFileName.contains("\\")) {
                    index = sFileName.lastIndexOf("\\");
                    sFileName = sFileName.substring(index + 1);
                }
                outFilePath = workspace + sFileName;
                if (new File(outFilePath).exists()) {
                    File file1 = new File(outFilePath);
                    file1.delete();
                }
                //暂存文件
                outfile = new File(outFilePath);
                //写入暂存文件
                FileUtils.copyFile(file, outfile);
                //读取excel文件中的数据
                InputStream inputStream = new FileInputStream(outFilePath);
                Workbook workbook = WorkbookFactory.create(inputStream);
                Sheet sheet = workbook.getSheetAt(0);
                MapList alldataList = JF_ExcelUtils_mxJPO.getMapList((XSSFSheet) sheet, 1, true, false);//调用工具类读取里面的内容
                Map temp = null;
                String RowNum;
                Row row = sheet.getRow(1);
                int rIdCellNum = row.getLastCellNum() - 1;
                LOGGER.info("addDataList:{}", alldataList);
                int oIdCellNum = rIdCellNum - 1;
                int num = oIdCellNum - 8;
                LOGGER.info("rIdCellNum:{}", rIdCellNum);
                LOGGER.info("oIdCellNum:{}", oIdCellNum);
                LOGGER.info("num:{}", num);
                String cell8Name = row.getCell(8).getStringCellValue();
                String cell9Name = row.getCell(9).getStringCellValue();
                //全部检查一遍是否有问题
                StringList ranges = FrameworkUtil.getRanges(context, JF_PLMConstants_mxJPO.ATTR_JF_VPMReference_JF_ProcurementGroup);
                LOGGER.info("ranges:{}", ranges);
                String errorNotice0 = EnoviaResourceBundle.getProperty(context, "emxProgramCentralStringResource", sLanguage, "emxProgramCentral.PartList.ImportNotive1");
                String errorNotice1 = EnoviaResourceBundle.getProperty(context, "emxProgramCentralStringResource", sLanguage, "emxProgramCentral.PartList.ImportNotive2");
                String errorNotice2 = EnoviaResourceBundle.getProperty(context, "emxProgramCentralStringResource", sLanguage, "emxProgramCentral.PartList.ImportNotive3");
                String errorNotice3 = EnoviaResourceBundle.getProperty(context, "emxProgramCentralStringResource", sLanguage, "emxProgramCentral.PartList.ImportNotive4");
                String errorNotice4 = EnoviaResourceBundle.getProperty(context, "emxProgramCentralStringResource", sLanguage, "emxProgramCentral.import.Notice4");
                String errorNotice5 = EnoviaResourceBundle.getProperty(context, "emxProgramCentralStringResource", sLanguage, "emxProgramCentral.import.Notice5");
                String errorNotice6 = EnoviaResourceBundle.getProperty(context, "emxProgramCentralStringResource", sLanguage, "emxProgramCentral.PartList.ImportNotive5");
                LOGGER.info("alldataList:{}", alldataList);
                String regex = "^[+-]?([0-9]+(\\.[0-9]*)?|\\.[0-9]+)$";
                for (int i = 1; i < alldataList.size(); i++) {
                    StringBuffer errorStr = new StringBuffer();
                    temp = (Map) alldataList.get(i);
                    LOGGER.info("temp:{}", temp);
                    RowNum = UIUtil.getValue(temp, "RowNum");
                    String strCell = errorNotice5 + RowNum + errorNotice4;
                    if (num == 5 || num == 7) {
                        String strGroup = UIUtil.getValue(temp, "8");
                        String strBicycleUsage = UIUtil.getValue(temp, "9");
                        String strTotalDemand = UIUtil.getValue(temp, "10");
                        String strDeliveryDate = UIUtil.getValue(temp, "11");
                        String strPartRequirementPlanDate = UIUtil.getValue(temp, "12");
                        String strDrawDataCompletionPlanDate = UIUtil.getValue(temp, "13");
                        String str3DReleasedDesignatedPlanDate = UIUtil.getValue(temp, "14");
                        if (!ranges.contains(strGroup)) {
                            //分组不包含
                            errorStr.append(errorNotice0.replace("1", strGroup));
                        }
                        if ((UIUtil.isNotNullAndNotEmpty(strBicycleUsage) && !strBicycleUsage.matches(regex)) || (UIUtil.isNotNullAndNotEmpty(strTotalDemand) && !strTotalDemand.matches(regex))) {
                            errorStr.append(errorNotice1);
                        }
                        if (UIUtil.isNotNullAndNotEmpty(strDeliveryDate)) {
                            try {
                                LocalDate.parse(strDeliveryDate.trim(), FORMATTER);
                            } catch (Exception e) {
                                errorStr.append(errorNotice2);
                            }
                        }
                        if (UIUtil.isNotNullAndNotEmpty(strPartRequirementPlanDate)) {
                            try {
                                LocalDate.parse(strPartRequirementPlanDate.trim(), FORMATTER);
                            } catch (Exception e) {
                                errorStr.append(errorNotice2);
                            }
                        }
                        if (num == 7 && UIUtil.isNotNullAndNotEmpty(strDrawDataCompletionPlanDate)) {
                            try {
                                LocalDate.parse(strDrawDataCompletionPlanDate.trim(), FORMATTER);
                            } catch (Exception e) {
                                errorStr.append(errorNotice2);
                            }
                        }
                        if (num == 7 && UIUtil.isNotNullAndNotEmpty(str3DReleasedDesignatedPlanDate)) {
                            try {
                                LocalDate.parse(str3DReleasedDesignatedPlanDate.trim(), FORMATTER);
                            } catch (Exception e) {
                                errorStr.append(errorNotice2);
                            }
                        }
                    } else if (num == 2) {
                        String date1 = UIUtil.getValue(temp, "8");
                        String date2 = UIUtil.getValue(temp, "9");
                        if (UIUtil.isNotNullAndNotEmpty(date2)) {
                            try {
                                LocalDate.parse(date2.trim(), FORMATTER);
                            } catch (Exception e) {
                                //分组不包含
                                errorStr.append(errorNotice3);
                            }
                            if (!compareDate(context, date2, date1)) {//开始时间大于结束时间报错
                                //date2不能晚于date1 SQD上传 承诺日期不能晚于需求日期
                                errorStr.append(errorNotice6);
                            }
                        }
                    }
                    if (errorStr.length() > 0) {
                        errorList.add(strCell + errorStr.toString());
                    }
                }
                LOGGER.info("errorList:{}", errorList);
                //如果没有问题
                ContextUtil.startTransaction(context, true);
                DomainObject domainObject = DomainObject.newInstance(context);
                DomainRelationship domainRelationship = DomainRelationship.newInstance(context);
                if (errorList.size() == 0) {
                    for (int i = 1; i < alldataList.size(); i++) {
                        temp = (Map) alldataList.get(i);
                        Map boValueMap = new HashMap();
                        Map reValueMap = new HashMap();
                        String oId = UIUtil.getValue(temp, String.valueOf(oIdCellNum));
                        String rId = UIUtil.getValue(temp, String.valueOf(rIdCellNum));
                        if (num == 5 || num == 7) {
                            String strGroup = UIUtil.getValue(temp, "8");
                            String strBicycleUsage = UIUtil.getValue(temp, "9");
                            String strTotalDemand = UIUtil.getValue(temp, "10");
                            String strDeliveryDate = UIUtil.getValue(temp, "11");
                            String strPartRequirementPlanDate = UIUtil.getValue(temp, "12");
                            String strDrawDataCompletionPlanDate = UIUtil.getValue(temp, "13");
                            String str3DReleasedDesignatedPlanDate = UIUtil.getValue(temp, "14");
                            domainObject.setId(oId);
                            String group = domainObject.getInfo(context, JF_PLMConstants_mxJPO.SELECT_ATTR_JF_VPMReference_JF_ProcurementGroup);
                            if (UIUtil.isNullOrEmpty(group)) {
                                boValueMap.put(JF_PLMConstants_mxJPO.ATTR_JF_VPMReference_JF_ProcurementGroup, strGroup);
                            }
                            reValueMap.put(JF_PLMConstants_mxJPO.ATTR_JFBicycleUsage, strBicycleUsage);
                            reValueMap.put(JF_PLMConstants_mxJPO.ATTR_JFTotalDemand, strTotalDemand);
                            if (UIUtil.isNotNullAndNotEmpty(strDeliveryDate)) {
                                strDeliveryDate = formatDateToSystem(context, strDeliveryDate);
                                reValueMap.put(JF_PLMConstants_mxJPO.ATTR_JFDeliveryDate, strDeliveryDate);
                            }
                            if (UIUtil.isNotNullAndNotEmpty(strPartRequirementPlanDate)) {
                                strPartRequirementPlanDate = formatDateToSystem(context, strPartRequirementPlanDate);
                                reValueMap.put(JF_PLMConstants_mxJPO.ATTR_JFPartRequirementPlanDate, strPartRequirementPlanDate);
                            }
                            if (num == 7 && UIUtil.isNotNullAndNotEmpty(strDrawDataCompletionPlanDate)) {
                                strDrawDataCompletionPlanDate = formatDateToSystem(context, strDrawDataCompletionPlanDate);
                                reValueMap.put(JF_PLMConstants_mxJPO.ATTR_JFDrawDataCompletionPlanDate, strDrawDataCompletionPlanDate);
                            }
                            if (num == 7 && UIUtil.isNotNullAndNotEmpty(str3DReleasedDesignatedPlanDate)) {
                                str3DReleasedDesignatedPlanDate = formatDateToSystem(context, str3DReleasedDesignatedPlanDate);
                                reValueMap.put(JF_PLMConstants_mxJPO.ATTR_JF3DReleasedDesignatedPlanDate, str3DReleasedDesignatedPlanDate);
                            }
                        } else if (num == 1) {
                            String value = UIUtil.getValue(temp, "8");
                            reValueMap.put("JFPartSupplierInfo", value);
                        } else if (num == 2) {
                            String value = UIUtil.getValue(temp, "9");
                            if (UIUtil.isNotNullAndNotEmpty(value)) {
                                value = formatDateToSystem(context, value);
                                reValueMap.put("JFPartRequirementCommitmentDate", value);
                            }
                        }
                        LOGGER.info("boValueMap:{}", boValueMap);
                        LOGGER.info("reValueMap:{}", reValueMap);
                        if (boValueMap.size() > 0) {
                            domainObject.setId(oId);
                            domainObject.setAttributeValues(context, boValueMap);
                        }
                        if (reValueMap.size() > 0) {
                            domainRelationship = DomainRelationship.newInstance(context, rId);
                            domainRelationship.setAttributeValues(context, reValueMap);
                        }
                    }
                    ContextUtil.commitTransaction(context);
                    return errorList;
                } else {
                    ContextUtil.abortTransaction(context);
                    return errorList;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            ContextUtil.abortTransaction(context);
            errorList.add(e.getMessage());
            return errorList;
        } finally {
            ContextUtil.popContext(context);
        }
        return errorList;
    }

}
