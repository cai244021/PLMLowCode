import com.alibaba.fastjson.JSONObject;
import com.dassault_systemes.createcontent.ENONewAdaptor;
import com.dassault_systemes.createcontent.ErrorMngt.ENONewException;
import com.dassault_systemes.createcontent.models.ENONewOptions;
import com.dassault_systemes.enovia.e6wv2.foundation.jaxb.Dataobject;
import com.dassault_systemes.enovia.tskv2.ProjectSequence;
import com.dassault_systemes.enovia.xapps.dseng.interfaces.dsengFactory;
import com.dassault_systemes.enovia.xapps.services.ER.implementations.ENOXERContext;
import com.dassault_systemes.enovia.xapps.services.context.ENOXContextManager;
import com.dassault_systemes.platform.model.PPRContext.itf.IPPRContextManagementItf;
import com.dassault_systemes.platform.model.PPRContext.services.IPPRContextManagementProvider;
import com.dassault_systemes.platform.model.itf.nav.INavBusObject;
import com.dassault_systemes.platform.ven.jackson.databind.ObjectMapper;
import com.dassault_systemes.plm.config.as_stream.itf.beans.args.filterable.GetExpressionsArgs;
import com.dassault_systemes.plm.config.as_stream.itf.beans.inputs.filterable.InGetExpressions;
import com.dassault_systemes.plm.config.as_stream.itf.beans.outputs.filterable.OutExpressionWithIndex;
import com.dassault_systemes.plm.config.as_stream.itf.beans.outputs.filterable.OutGetExpressions;
import com.dassault_systemes.plm.config.as_stream.itf.beans.results.filterable.GetExpressionsResults;
import com.dassault_systemes.plm.config.as_stream.itf.factories.FilterableServicesFactory;
import com.dassault_systemes.plm.config.as_stream.itf.services.filterable.IExpressionGetter;
import com.dassault_systemes.plm.config.as_stream.webservices.impl.builders.FilterableAsStreamArgsBuilder;
import com.dassault_systemes.ppr.utils.Utilities;
import com.dassault_systemes.smasds.powerby.services.util.ServiceUtils;
import com.matrixone.apps.common.Task;
import com.matrixone.apps.common.TaskDateRollup;
import com.matrixone.apps.common.WorkCalendar;
import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.DomainRelationship;
import com.matrixone.apps.domain.util.*;
import com.matrixone.apps.domain.util.DateUtil;
import com.matrixone.apps.framework.ui.UIUtil;
import com.matrixone.apps.program.ProgramCentralConstants;
import jakarta.json.*;
import jakarta.servlet.http.HttpServletRequest;
import matrix.db.Context;
import matrix.db.JPO;
import matrix.util.MatrixException;
import matrix.util.StringList;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class test_mxJPO {
    private static final Logger logger = LoggerFactory.getLogger(test_mxJPO.class);
    /*
     * @description:Excel函数获取
     * @author: caipan
     * @date: 2025/2/26 09:53:53
     * @param: * @param[1] context
     * @param[2] args
     * @return:
     **/
    public void testExpre(Context context,String[] args){
        {
            String excelFilePath = args[0]; // 替换为你的Excel文件路径
            try (FileInputStream fis = new FileInputStream(new File(excelFilePath));
                 Workbook workbook = new XSSFWorkbook(fis)) {

                Sheet sheet = workbook.getSheetAt(0); // 获取第一个工作表
                System.out.println(new Date());
                for(int i=1;i<50;i++) {
                    for (Row row : sheet) {
                        for (Cell cell : row) {
                            if (cell.getCellType() == CellType.FORMULA) {
                                // 强制计算公式，获取公式计算结果
                                workbook.getCreationHelper().createFormulaEvaluator().evaluateFormulaCell(cell);
                                double cellValue = cell.getNumericCellValue(); // 对于数字公式，使用getNumericCellValue()
                                System.out.println("Cell Value 表达式: " + cellValue);
                            } else {
                                // 处理非公式单元格，例如文本或数字值等
                                switch (cell.getCellType()) {
                                    case STRING:
                                        cell.setCellValue("hello"+i);
                                        System.out.println("Cell Value: " + cell.getStringCellValue());
                                        break;
                                    case NUMERIC:
                                        cell.setCellValue(10*i);
                                        System.out.println("Cell Value: " + cell.getNumericCellValue());
                                        break;
                                    // 可以根据需要添加更多类型处理
                                }
                            }
                        }
                        System.out.println("********************");
                    }
                }
                System.out.println(new Date());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
  public void getFUllName(Context context, String[] args) throws Exception {

     DomainObject obj = DomainObject.newInstance(context);
     obj.setId(args[0]);
     StringList selList = new StringList();
     selList.add("id");
     StringList relList = new StringList();
     relList.add("id[connection]");
     MapList list =  obj.getRelatedObjects(context, "VPMInstance", "VPMReference", selList, relList, true, false, (short) 2, null, null, 0);
     for(int i=list.size()-1;i>-1;i--){
         logger.info("map:{}",list.get(i));
     }
  }



    /*
     * @description:
     * @author: caipan
     * @date:
     * @param: * @param[1] jsonObject
     * @return:  key是关系的物理ID，value是配置表达式
     **/
    public static JSONObject getVariantFromJsonObject(JsonObject jsonObject){
        JsonObject expressionsJsonObj = jsonObject.getJsonObject("expressions");
        Set<String> keySet = expressionsJsonObj.keySet();
        JSONObject returnJson = new JSONObject();
        String variantStr = "";

        for (String element : keySet) {
            JsonObject configJsonObj = expressionsJsonObj.getJsonObject(element);
            String hasEffectivity = configJsonObj.getString("hasEffectivity");
            if("YES".equals(hasEffectivity)){
                JsonObject contentJsonObj = configJsonObj.getJsonObject("content");
                variantStr = contentJsonObj.getString("Variant");
                returnJson.put(element, variantStr);
            }
        }
        return returnJson;
    }

  public void getConfigbak(Context context,String[]args) throws Exception {
      ObjectMapper var4 = new ObjectMapper();
      JSONObject json = new JSONObject();
      json.put("view", "ALL");
      json.put("domain", "ALL");
      json.put("format", "TXT");
      json.put("ids", "64C5EA29C1E0010067402EAD000003A3");
      logger.info("json:{}",json.toJSONString());
      InGetExpressions var18 = (InGetExpressions)var4.readValue(json.toJSONString(), InGetExpressions.class);
      GetExpressionsArgs var6 = FilterableAsStreamArgsBuilder.buildGetExpressionsArgs(var18);
      IExpressionGetter var7 = FilterableServicesFactory.createExpressionGetter(context);
      GetExpressionsResults var8 = var7.getFilterableExpressions(var6);
      Map var9 = var8.getInstanceIndexesToExpressions();
      OutGetExpressions var10 = new OutGetExpressions();
      var9.forEach((var1x, var2x) -> {
          OutExpressionWithIndex var3 = new OutExpressionWithIndex();
          var3.index = (String) var1x;
          var3.expression = (String) var2x;
          var10.expressions.add(var3);
      });
      String var11 = var4.writeValueAsString(var10);
      logger.info("var11:{}",var11);
  }


  public void testReplace(Context context,String[] args) throws MatrixException {
      logger.info("testReplace:{}",args[0]);
      String connectId ="14585.59252.44408.7056:BSF";//旧版本
      DomainObject partObj = DomainObject.newInstance(context);
      partObj.setId("14585.59252.44408.30394");//新版本
      String revision =  partObj.getInfo(context, "to[VPMInstance].from.revision");
      logger.info("revision:{}",revision);

      DomainRelationship.setToObject(context, connectId, partObj);

  }


    /*
     * @description: 找一级件
     * @author: caipan
     * @date:
     * @param: * @param[1] context
     * @param[2] args
     * @return:
     **/
    public void getOnePart(Context context,String[] args) throws MatrixException {
      String id = args[0];
      DomainObject obj = DomainObject.newInstance(context);
      obj.setId(id);//受影响对象
        StringList boselect = new StringList();
        boselect.add("id");
        boselect.add("from[VPMInstance].to.name");

        StringList relselect = new StringList();
        relselect.add("id[connection]");

        StringList zhengyiList = new StringList();
        zhengyiList.add("14585.59252.44408.33870");
        //所有的父级
        StringList allPartList = new StringList();
        MapList parentList = obj.getRelatedObjects(context, "VPMInstance", "VPMReference",boselect , relselect, true, false, (short) 0, "", "", 0);
        logger.info("parentList:{}",parentList);
        StringList parentAllId = new StringList();
        boolean flag = false;
        StringList connectList = new StringList();//受影响对象关联的整椅号
        for(int i=parentList.size()-1;i>=0;i--){
            Map temp = (Map)parentList.get(i);
            String parentId = UIUtil.getValue(temp, "id");
            logger.info(parentId);
           if(zhengyiList.contains(parentId)){//判断当前受影响对象的父是有当前项目关联的整椅上
            flag = true;//包含
               connectList.add(parentId);
           }
        }

        if(flag){//找一级件

        }


    }

  public void queryParent(Context context,String[] args) throws Exception{
      logger.info("queryParent:{}",args[0]);
      DomainObject obj = DomainObject.newInstance(context);
      obj.setId("14585.59252.44408.30394");
     String revision =  obj.getInfo(context, "to[VPMInstance].from.revision");
     logger.info("revision:{}",revision);
  }

  public String getTaskWBSLevel(Context context,ProjectSequence ps,String taskPhyId ) throws Exception {
      String wbsLevel="0";
      Map<String, Dataobject> palSeqData = ps.getSequenceData(context);
      Dataobject taskObj = palSeqData.get(taskPhyId);
      System.out.println("taskLevel:{}"+taskObj.getDataelements().get(ProgramCentralConstants.KEY_WBS_ID));
      wbsLevel = (String)taskObj.getDataelements().get(ProgramCentralConstants.KEY_WBS_ID);
      return wbsLevel;
  }

    /** 获取持续天数
     * @description
     * @author caipan
     * @param[1] context
     * @param[2] args
     * @throws

     * @time 2024/1/6 22:26
     */
    public String getDurationDay(Context context,String[] args) throws Exception{
        Long duration= 1L;
        boolean flag = false;
        try {
            ContextUtil.pushContext(context);
            flag = true;
            Map requestMap = (Map) JPO.unpackArgs(args);
            String taskId = UIUtil.getValue(requestMap, "objectId");
            String strStartDate = UIUtil.getValue(requestMap, "startDateValue");
            String strEndDate = UIUtil.getValue(requestMap, "endDateValue");
            DomainObject taskObj = DomainObject.newInstance(context);
            taskObj.setId(taskId);//任务对象
            logger.info("taskId:{}",taskId);
//        StringList projectList = taskObj.getInfoList(context, "to[Project Access Key].from.from[Project Access List|to.type=='Project Space'].to.id");
//        if(projectList.size()>0){
            //获取项目日历
            Task task = (Task) DomainObject.newInstance(context, DomainConstants.TYPE_TASK, DomainConstants.PROGRAM);
            task.setId(taskId);
//            log.info("requestMap:{}", requestMap);
            SimpleDateFormat formatTime = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss aa", Locale.ENGLISH);
            Date endDate = formatTime.parse(strEndDate);
            Date startDate = formatTime.parse(strStartDate);
            WorkCalendar workCalendar = task.getSchedulingCalendar(context);
            String calendarId = workCalendar.getId(context);
            logger.info("日历:{}",workCalendar.getId(context));
            //caipan  计算持续时间
            if(UIUtil.isNotNullAndNotEmpty(calendarId)) {
                duration = ((WorkCalendar) workCalendar).computeDuration(context, startDate, endDate);
            }else{ //如果没有日历，直接比较日期 工作日
                DateUtil dateUtil = new DateUtil();
                duration = dateUtil.computeDuration(startDate, endDate);
            }
            logger.info("duration:{}", duration);
//        }
        }catch (Exception e){
            e.printStackTrace();
            logger.info("error:{}",e.getMessage());
            duration=1L;
        }finally {
            if(flag) {
                ContextUtil.popContext(context);
            }
        }
        return duration.toString();
    }
  public void testUpdateTask(Context context,String[] args) throws Exception{
      TaskDateRollup task = new TaskDateRollup();

      String TaskEstimatedStartDate = "7/01/2024";
      if(UIUtil.isNotNullAndNotEmpty(TaskEstimatedStartDate)) {
          TaskEstimatedStartDate = TaskEstimatedStartDate + " 9:00:00 AM";
      }
   /*   String TaskEstimatedFinishDate ="";
      if(UIUtil.isNotNullAndNotEmpty(TaskEstimatedFinishDate)) {
          TaskEstimatedFinishDate = TaskEstimatedFinishDate + " 6:00:00 PM";
      }*/
        logger.info("TaskEstimatedStartDate:{}",TaskEstimatedStartDate);
      task.setId(args[0]);
      task.updateStartDate(context, eMatrixDateFormat.getJavaDate(TaskEstimatedStartDate), false);
//      task.updateFinishDate(context,eMatrixDateFormat.getJavaDate(TaskEstimatedFinishDate), false);
      task.updateDuration(context,"5",false,false);
  }

  public void testCreatePart(Context context,String[] args) throws Exception, ENONewException {
      Map temp = JPO.unpackArgs(args);
      context.start(true);
      HttpServletRequest request = (HttpServletRequest)temp.get("HttpServletRequest");
      Context var11 = context;
      JsonArray var81 = null;
      ENONewOptions var14 = new ENONewOptions();
      var14.setParsedResult(true);//true 必须是true 不然报错
      HashMap var87 = new HashMap();
      var87.put("activeFolder", false);
      var87.put("typeName", "assembly");
      var14.setParam(var87);
      var14.setSecurityContext("ctx::VPLMProjectLeader.JFCompany.JFSeat");//设置上下文
      ENONewAdaptor var88 = new ENONewAdaptor(var11);
      var88.set_httpSession(request.getSession());//request
      JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
      Map map = new HashMap<>();
      Json.createObjectBuilder(map);
      JsonObjectBuilder objBuilder = Json.createObjectBuilder();
      objBuilder.add("type", "VPMReference");
      JsonArray var84 = getJson(args);
      logger.info("json:{}",var84);
      var81 = var88.createObject(var84, var14);
      logger.info("var81:{}",var81);
      if(context.isTransactionActive()){
          context.commit();
      }
  }

    /*
     * @description: 创建数模
     * @author: caipan
     * @date:
     * @param: * @param[1] context
     * @param[2] args
     * @return:
     **/
    public void testEngItem(Context context, String[] args) throws Exception {
//        context.resetRole("ctx::VPLMProjectLeader.JFCompany.JFSeat");
        String param = "{\n" + " \"items\": [\n" + " {\n" + " \"type\": \"PPRContext\",\n" + " \"attributes\": {\n" + " \"title\": \"\",\n" + " \"isManufacturable\": true,\n" + " \"description\": \"My description\"\n" + " }\n" + " }\n" + " ]\n" + "}";
        JsonReader paramReader = Json.createReader(new StringReader(param));
        JsonObject paramJson = paramReader.readObject();
        ENOXERContext enoxerContext = new ENOXERContext(context, DomainConstants.EMPTY_STRING, 1);
        ENOXContextManager.getContextManager().addContext(context, enoxerContext);
        try {
            ContextUtil.startTransaction(context, true);
            List<String> engItemsOrdered = dsengFactory.getAuthoringInstance(context).createEngItemsOrdered(context, paramJson, "dseng:EngItem");
            System.out.println("engItemsOrdered : " + engItemsOrdered);
            ContextUtil.commitTransaction(context);
        } catch (Exception e) {
            e.printStackTrace();
            ContextUtil.abortTransaction(context);
        }
    }
    public  JsonArray getJson(String[] args) {

        StringList createList = new StringList();
        createList.add("Physical Product10201210");//后续可以调用编码器、或者生成正式编码
        createList.add("Physical Product10201211");
//         createList.add("Physical Product00003020");
//         createList.add("Physical Product000030011");
        Map rootMap = new HashMap();
        JsonArrayBuilder rootArray = Json.createArrayBuilder();

        for(int i=0;i<createList.size();i++) {
            String phyName = createList.get(i);//创建的文件名
            JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
            Map map = new HashMap<>();
            map.put("type", "VPMReference");
            map.put("subTypes", arrayBuilder.build());

            Map map1 = new HashMap();
            map1.put("name", "VPMReference");
            JsonObjectBuilder objBuilder1 = Json.createObjectBuilder(map1);
            arrayBuilder.add(objBuilder1.build());

            JsonArrayBuilder interfaceArray = Json.createArrayBuilder();
            interfaceArray.add("EnterpriseExtension");
            interfaceArray.add("XP_VPMReference_Ext");
            interfaceArray.add("JF_VPMReference");
            //interfaces
            map.put("interfaces", interfaceArray.build());

            Map attributeMap = new HashMap();
            JsonArrayBuilder internalAttributes = Json.createArrayBuilder();
            attributeMap.put("internalAttributes", internalAttributes.build());
            JsonArrayBuilder publicAttributes = Json.createArrayBuilder();
            Map V_Name = setMap("V_Name", "String", phyName);
            V_Name.put("modified",true);
            Map PLM_ExternalID = setMap("PLM_ExternalID", "String", phyName);
            PLM_ExternalID.put("modified", true);
            Map V_description = setMap("V_description", "String", "");
            Map policy = setMap("policy", "String", "VPLM_SMB_Definition_MajorRev");
            publicAttributes.add(Json.createObjectBuilder(V_Name));
            publicAttributes.add(Json.createObjectBuilder(PLM_ExternalID));
            publicAttributes.add(Json.createObjectBuilder(V_description));
            publicAttributes.add(Json.createObjectBuilder(policy));
            attributeMap.put("publicAttributes", publicAttributes.build());
            JsonArrayBuilder extensionAttributes = Json.createArrayBuilder();
            Map extensionAttribute = setMap("JF_PartDes", "String", "");
            extensionAttributes.add(Json.createObjectBuilder(extensionAttribute));
            attributeMap.put("extensionAttributes", extensionAttributes.build());
            JsonArrayBuilder volatileAttributes = Json.createArrayBuilder();
            Map volatileAttribute = setMap("Template", "String", "UPSV5ProductTemplate");
            volatileAttribute.put("authorizedValuesRequired", true);
            volatileAttribute.put("storeLocalPreference", true);
            volatileAttribute.put("basicAttribute", false);
            volatileAttribute.put("modified", true);
            Map persistData = new HashMap();
            Map ProductTemplate = new HashMap();
            ProductTemplate.put("name", "ProductTemplate");
            Map UPSV5ProductTemplate = new HashMap();
            UPSV5ProductTemplate.put("name", "UPSV5ProductTemplate");
            UPSV5ProductTemplate.put("filename", "UPSV5ProductTemplate");
            UPSV5ProductTemplate.put("fileext", ".CATProduct");
            UPSV5ProductTemplate.put("physicalid", "");
            UPSV5ProductTemplate.put("isfilebased", true);
            JsonArrayBuilder templateObjects = Json.createArrayBuilder();
            templateObjects.add(Json.createObjectBuilder(ProductTemplate).build());
            templateObjects.add(Json.createObjectBuilder(UPSV5ProductTemplate).build());
            persistData.put("templateObjects", templateObjects.build());

            Map groups = new HashMap();
            JsonArrayBuilder group1 = Json.createArrayBuilder().add("ProductTemplate");
            JsonArrayBuilder CATIAV5group1 = Json.createArrayBuilder().add("UPSV5ProductTemplate");
            groups.put("3DExperience", group1.build());
            groups.put("CATIAV5", CATIAV5group1.build());
            persistData.put("groups", Json.createObjectBuilder(groups).build());


            volatileAttribute.put("persistData", Json.createObjectBuilder(persistData).build());
            volatileAttributes.add(Json.createObjectBuilder(volatileAttribute).build());


            Map name = setMap("Filename", "String", phyName);
            name.put("validationService", "DS/ENONewWidget/validation/FileName");
            name.put("modified", true);


            Map visibilityMap = new HashMap();
            JsonArrayBuilder visibilityArray = Json.createArrayBuilder();
            visibilityArray.add("UPSV5ProductTemplate");
            visibilityMap.put("Template", visibilityArray.build());
            name.put("visibility", Json.createObjectBuilder(visibilityMap).build());

            volatileAttributes.add(Json.createObjectBuilder(name).build());
            JsonObjectBuilder visibility = Json.createObjectBuilder(visibilityMap);
            visibilityMap.put("visibility", visibility.build());
            attributeMap.put("volatileAttributes", volatileAttributes.build());

            map.put("attributes", attributeMap);
            JsonObject result = Json.createObjectBuilder(map).build();
            rootArray.add(result);
        }
      /*  rootMap.put("create",rootArray.build());
        JsonObject rootJson = Json.createObjectBuilder(rootMap).build();
        System.out.println(rootJson);*/
        return rootArray.build();
    }

    public static Map setMap (String name,String type,String value){
        Map map = new HashMap();
        map.put("name", name);
        map.put("type", type);
        map.put("value", value);
        return map;
    }

    /*
     * @description:获取物理产品的下一个Name
     * @author: caipan
     * @date: 2025/2/26 09:52:50
     * @param: * @param[1] context
     * @param[2] args
     * @return:
     **/
    public void getName(Context context,String[] args) throws Exception{
        String type = "VPMReference";//type
        String pak = "PRODUCTCFG";//package
        String var7 = ServiceUtils.generateUniqueID(context, pak, type);
        String var8 = ServiceUtils.generateExternalID(context, type, var7);
        System.out.println("var8 = " + var8);
    }
/*
 * @description:导出项目人员清单
 * @author: caipan
 * @date: 2025/1/23 10:22:41
 * @param: * @param[1] context
 * @param[2] args
 * @return:
 **/
public void test1(Context context,String[] args) throws Exception{
    String templatepathFile ="/data/cp/test.xlsx";
    String saveFileName = "makeTemplate-"+new Date().getTime()+".xlsx";
    String savepath ="/data/cp/";
    MapList resultList = new MapList();

    StringList selList = new StringList();
    selList.add("name");
    selList.add("id");
    selList.add("attribute[First Name]");
    selList.add("attribute[Last Name]");
    selList.add("description");

    StringList relList = new StringList();
    relList.add(DomainConstants.SELECT_RELATIONSHIP_ID);
    relList.add("attribute[Project Role]");
    MapList list = DomainObject.findObjects(context, DomainConstants.TYPE_PROJECT_SPACE, "*", null,selList);
    for(int i = 0;i<list.size();i++){
        Map tmp = (Map)list.get(i);
        DomainObject project = DomainObject.newInstance(context,UIUtil.getValue(tmp, "id"));
        MapList personList =  project.getRelatedObjects(context, "Member", "Person", selList, relList, false, true, (short) 1, null, null, 0);
        for(int j=0;j<personList.size();j++){
            Map temp = (Map)personList.get(j);
            Map resultMap= new HashMap();
            resultMap.put("ProjectName",UIUtil.getValue(tmp, "name"));
            resultMap.put("ProjectTitle",UIUtil.getValue(tmp, "description"));
            resultMap.put("owner",UIUtil.getValue(temp, "name"));
            resultMap.put("role",UIUtil.getValue(temp, "attribute[Project Role]"));
            resultMap.put("username",UIUtil.getValue(temp, "attribute[First Name]"));
            resultList.add(resultMap);
        }
    }
    String path = Export(resultList, templatepathFile, savepath+saveFileName);
    System.out.println(path+"=path");
}
    public static String Export(MapList eBOMExpandItem, String templatepath, String savepath) throws IOException {
        // TODO Auto-generated method stub

        System.out.println("export start time==" + (new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date())));
        System.out.println("==templatepath==" + templatepath);
        File fs = new File(templatepath);
        FileInputStream fis = new FileInputStream(fs);
        XSSFWorkbook XSSFwb = new XSSFWorkbook(fis);
        SXSSFWorkbook SXSSFwb = new SXSSFWorkbook(XSSFwb);
        XSSFWorkbook wb = SXSSFwb.getXSSFWorkbook();
//        HSSFWorkbook wb= new HSSFWorkbook(fis);
        XSSFSheet sheet = wb.getSheetAt(0);
        Iterator<Map<String, String>> listmap = eBOMExpandItem.iterator();
        int index = 1;
        XSSFRow row1 = sheet.getRow(0);
        int cellnum = row1.getLastCellNum();
        while (listmap.hasNext()) {
            Map<String, String> map = listmap.next();
            XSSFRow row = sheet.createRow(index);
            for (String tempkey : map.keySet()) {
                for (int i = 0; i < cellnum; i++) {
                    String cellstr = row1.getCell(i).getStringCellValue();
                    if (tempkey.equals(cellstr)) {
                        XSSFCell cell = row.createCell(i);
                        cell.setCellValue(map.get(tempkey));
                    }
                }
            }
            index++;
        }
        createFile(savepath);
        FileOutputStream fileOut = new FileOutputStream(savepath);
        try {
            wb.write(fileOut);
            fileOut.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("export end time==" + (new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date())));
        return savepath;
    }

    public static void createFile(String path) {
        File file = new File(path);
        if (file.exists()) {
            System.out.println("File exists");
        } else {
            System.out.println("File not exists, create it ...");
            // getParentFile() 获取上级目录(包含文件名时无法直接创建目录的)
            if (!file.getParentFile().exists()) {
                System.out.println("not exists");
                // 创建上级目录
                file.getParentFile().mkdirs();
            }
            try {
                // 在上级目录里创建文件
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }








    public void test(Context context, String[] args) throws FrameworkException {
        try{
            // String pprByGeneralOperationReference = getPPRByGeneralOperationReference(context, "21798.25570.9499.57258");
            // System.out.println("pprByGeneralOperationReference = " + pprByGeneralOperationReference);
            // String pprByGeneralOperationReference1 = getPPRByGeneralOperationReference(context, "21798.25570.9499.63461");
            // System.out.println("pprByGeneralOperationReference1 = " + pprByGeneralOperationReference1);

            // ContextUtil.pushContext(context);
            // ContextUtil.pushContext(context, "YANGLE", null, "eService Production");
            // context.resetRole("ctx::VPLMCreator.Company Name.Common Space");
            // ContextUtil.startTransaction(context, true);
            // String var2 = "{\"version\":\"v0\",\"type\":\"prerequisite\",\"to\":\"pid:AA2A612918123F006735A3580000012B\",\"from\":\"pid:AA2A612918123F006735A3760000012D\"}";
            //String var4 = ChangeDependenciesJsonUtilities.createLinkFromJson(context, var2);
            // System.out.println("var4 = " + var4);
            // String name = UUID.getNewUUIDHEXString();
            // System.out.println("name = " + name);
            // String mql = "add flattablerow ChangeDependencies "+name+" uuid "+name+" fromPhysicalId AA2A612918123F006735A3760000012D toPhysicalId AA2A612918123F006735AD8D00000131 type prerequisite userDefine 1 consumed 0 twinRowId ''";
            // String s = MqlUtil.mqlCommand(context,mql);
            // System.out.println("s = " + s);
            //
            // //添加MBOM子级
            // addNewManufacturedChild(context, "CEFC11FE00005C98667923100000EF2E", "CEFC11FE00005C98667923220000F762");
            // //添加工序子级
            // addNewSystemsChild(context, "CEFC11FE00005C98667923100000EF2E", "CEFC11FE00005C98667923430001041B");
            //
            // PPR ppr = new PPR("CEFC11FE00005C98667923100000EF2E");
            // //获取工序：systems：[CEFC11FE00005C98667923430001041B, CEFC11FE00005C986679232B0000FEAB]
            // List<String> systems = ppr.getSystems(context);
            // System.out.println("systems = " + systems);
            // //获取MBOM:manufacturedItems：[CEFC11FE00005C98667923220000F762]
            // List<String> manufacturedItems = ppr.getManufacturedItems(context);
            // System.out.println("manufacturedItems = " + manufacturedItems);
            // //获取EBOM：products：[CEFC11FE00005C986679234F00010554]
            // List<String> products = ppr.getProducts(context);
            // System.out.println("products = " + products);
            // ContextUtil.commitTransaction(context);
        }catch(Exception e){
            e.printStackTrace();
            //ContextUtil.abortTransaction(context);
        }finally {
            //ContextUtil.popContext(context);
        }
    }

    public static String getPPRByGeneralOperationReference(Context context, String id) {
        String pprId = "";
        try {
            if (UIUtil.isNullOrEmpty(id)) {
                return pprId;
            }
            DomainObject object = new DomainObject(id);
            String physicalId = object.getPhysicalId(context);
            IPPRContextManagementItf var2 = IPPRContextManagementProvider.getIPPRContextManagementItf();
            INavBusObject var3 = Utilities.getNBOFromPID(context, physicalId);
            List<INavBusObject> lists = new ArrayList<>();
            lists.add(var3);
            List<List<INavBusObject>> listPPRContextAttachedToProcessRef = var2.getListPPRContextAttachedToProcessRef(context, lists);
            for (int i = 0; i < listPPRContextAttachedToProcessRef.size(); i++) {
                List<INavBusObject> iNavBusObjects = listPPRContextAttachedToProcessRef.get(i);
                System.out.println("iNavBusObjects = " + iNavBusObjects);
                for (int i1 = 0; i1 < iNavBusObjects.size(); i1++) {
                    INavBusObject iNavBusObject = iNavBusObjects.get(i1);
                    String toString = iNavBusObject.toString();
                    if (UIUtil.isNotNullAndNotEmpty(toString)) {
                        toString = toString.substring(1, toString.length() - 1);
                        System.out.println("toString = " + toString);
                        pprId = toString.split(",")[0];
                        break;
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return pprId;
    }

    public void addNewManufacturedChild(Context context,String[] args){
        addNewManufacturedChild(context,args[0],args[1]);
    }

    /**
     * 在PPR下增加MBOM子级，如CreateAssembly类型
     * @param context
     * @param pprId
     * @param childId
     */
    public void addNewManufacturedChild(Context context, String pprId, String childId) {
        try {
            if (UIUtil.isNullOrEmpty(pprId) || UIUtil.isNullOrEmpty(childId)) {
                return;
            }
            DomainObject pprObject = new DomainObject(pprId);
            String physicalId = pprObject.getPhysicalId(context);
            DomainObject childObj = new DomainObject(childId);
            String childPhysicalId = childObj.getPhysicalId(context);
            IPPRContextManagementItf var2 = IPPRContextManagementProvider.getIPPRContextManagementItf();
            INavBusObject ppr = Utilities.getNBOFromPID(context, physicalId);
            INavBusObject child = Utilities.getNBOFromPID(context, childPhysicalId);
            List<INavBusObject> lists = new ArrayList<>();
            lists.add(child);
            //var2.attachProcessReferenceToPPRContext(context, lists, ppr);
            var2.attachMBOMReferenceToPPRContext(context, lists, ppr);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 在PPR下增加工序子级，如DELLmiGeneralSystemReference类型
     * @param context
     * @param pprId
     * @param childId
     */
    public void addNewSystemsChild(Context context, String pprId, String childId) {
        try {
            if (UIUtil.isNullOrEmpty(pprId) || UIUtil.isNullOrEmpty(childId)) {
                return;
            }
            DomainObject pprObject = new DomainObject(pprId);
            String physicalId = pprObject.getPhysicalId(context);
            DomainObject childObj = new DomainObject(childId);
            String childPhysicalId = childObj.getPhysicalId(context);
            IPPRContextManagementItf var2 = IPPRContextManagementProvider.getIPPRContextManagementItf();
            INavBusObject ppr = Utilities.getNBOFromPID(context, physicalId);
            INavBusObject child = Utilities.getNBOFromPID(context, childPhysicalId);
            List<INavBusObject> lists = new ArrayList<>();
            lists.add(child);
            var2.attachProcessReferenceToPPRContext(context, lists, ppr);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public  void getECR(Context context,String[] args) throws Exception{
        String strObjectId = args[0];
        DomainObject objectProject = DomainObject.newInstance(context, strObjectId);
        MapList mapList = objectProject.getRelatedObjects(context,
                JF_PLMConstants_mxJPO.REL_JFChange2Project, //pattern to match relationships
                JF_PLMConstants_mxJPO.TYPE_JFNewECR, //pattern to match types
                JF_Util_mxJPO.basicBolistSel(), //the eMatrix StringList object that holds the list of select statement pertaining to Business Obejcts.
                JF_Util_mxJPO.basicRellistSel(), //the eMatrix StringList object that holds the list of select statement pertaining to Relationships.
                true, //get To relationships
                false, //get From relationships
                (short) 1, //the number of levels to expand, 0 equals expand all.
                null, //where clause to apply to objects, can be empty ""
                null, //where clause to apply to relationship, can be empty ""
                (short) 0);//limit
        System.out.println(mapList);
    }

}
