import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.aspose.pdf.operators.Do;
import com.dassault_systemes.dostreaminformation.ENODOStream;
import com.dassault_systemes.dostreaminformation.ENODerivedOutputStreamInfoService;
import com.dassault_systemes.enovia.enterprisechangemgt.common.ChangeAction;
import com.dassault_systemes.enovia.enterprisechangemgt.common.ChangeConstants;
import com.dassault_systemes.enovia.enterprisechangemgt.common.ChangeOrder;
import com.dassault_systemes.enovia.enterprisechangemgt.util.ChangeUtil;
import com.dassault_systemes.enovia.versioning.services.VersioningServices;
import com.dassault_systemes.enovia.versioning.util.ENOVersioningOptions;
import com.dassault_systemes.lifecycle.implementations.LifecycleServices_NewMajorRevision_NLR;
import com.dassault_systemes.rest.service.lifecycle.DeleteWebServices;
import com.dassault_systemes.rest.service.lifecycle.MaturityResource;
import com.matrixone.apps.common.CommonDocument;
import com.matrixone.apps.common.Task;
import com.matrixone.apps.common.util.ComponentsUtil;
import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.DomainRelationship;
import com.matrixone.apps.domain.util.*;
import com.matrixone.apps.framework.ui.UIUtil;
import com.matrixone.apps.program.ProgramCentralConstants;
import ds.enovia.apps.msf.BusinessObjects.ExecuteJPO.args;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import matrix.db.*;
import matrix.util.MatrixException;
import matrix.util.StringList;
import org.apache.commons.text.StringEscapeUtils;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.io.*;
import java.io.File;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.matrixone.apps.domain.DomainConstants.SELECT_CURRENT;
import static com.matrixone.fcs.tools.FcsIndexGen.executor;

public class JF_Util_mxJPO {

    private static final DateTimeFormatter MATRIX_FORMAT = DateTimeFormatter.ofPattern(eMatrixDateFormat.getEMatrixDateFormat(), Locale.US);
private static final Logger log = LoggerFactory.getLogger(JF_Util_mxJPO.class);
private static final Logger ThreadLog = LoggerFactory.getLogger("MY_CUSTOM_LOGGER");
    public static void main(String[] args)  throws Exception{
       /* Map map = new HashMap();
        map.put("objectId","1231");
        map.put("name","4567");
        String mql = "print bus '${objectId}' select ${name}";
        mql = formatString(mql, map);
        System.out.println(mql);*/

       /* String message = getMessage(null,"","caipan","man");
        System.out.println(message);*/

    }

    /**
     * 解析字符串中的${}，并赋予相应的值，拼装MQL语句的时候比较方便
     * @param str 带占位符的MQL语句
     *            String mql = "print bus '${objectId}' select ${name}";
     * @param obj Map 里面包含参数
     *             Map map = new HashMap();
     *         map.put("objectId","1231");
     *         map.put("name","4567");
     * @return
     */
    private static String formatString(String str, Map<String, String> obj) {
        final  String regex = "\\" + "$" + "\\" + "{(.*?)}";
        try {
            // 正则表达式解析
            Pattern p = Pattern.compile(regex);
            Matcher m = p.matcher(str);
            StringBuffer sb = new StringBuffer();
            while (m.find()) {
                m.appendReplacement(sb, String.valueOf(obj.get(m.group(1))));
            }
            m.appendTail(sb);
            str = sb.toString();
        } catch (Exception e) {
            System.out.println("[formatString] fromat string error: " + str + " " + JSONObject.toJSONString(obj));
        }
        return str;
    }

    /**
     * @description: 获取配置文件的翻译文件，并且使用占位符
     * String message = getMessage(context,"Message.promoteToReleased","param");
     * 配置文件实例 emxFramework.Message.promoteToReleased=''{0}'' is not allowed to directly promote from the working state to the released state
     * @param: key 去除 emxFramework.的 key param 传递的参数
     * @return:
     * @author caipan
     * @date: 2023/8/16 16:45
     */
    public static String getMessage(Context context, String key, String... param) throws Exception {
        String message = getI18Framework(context, key,null);// " hello name {0} you sex ''{1}'' ";//
        return MessageFormat.format(message,param);
    }
    /**
     * @description: 获取Framework文件翻译 增加了前缀 emxFramework.
     * @param:
     * @return:
     * @author caipan
     * @date: 2023/8/16 16:45
     */
    public static String getI18Framework(Context context, String i18key,String language) throws Exception {
        Locale locale;
        //判断语言,如果传了语言则使用传的语言,若未传，则使用用户当前默认语言
        if(UIUtil.isNullOrEmpty(language)){
            locale = context.getLocale();
        }else{
            locale = new Locale(language);
        }
        //获取国际化翻译
        return EnoviaResourceBundle.getProperty(context, "emxFrameworkStringResource",locale, "emxFramework."+i18key);
    }
/**
 * @description  可以支持page 和资源文件 两种都可以获取出来
 * 完整的key 比如 emxFramework.Attribute.XP_VPMReference_Ext.BMT_Material_Number
 * @author caipan
 * @param[1] context
 * @param[2] i18key
 * @param[3] language
 * @throws

 * @time 2023/8/17 14:26
 */
    public static String getPageI18Framework(Context context, String i18key,String language) throws Exception {
        Locale locale;
        //判断语言,如果传了语言则使用传的语言,若未传，则使用用户当前默认语言
        if(UIUtil.isNullOrEmpty(language)){
            locale = context.getLocale();
        }else{
            locale = new Locale(language);
        }
        //获取国际化翻译
        return  EnoviaResourceBundle.getFrameworkStringResourceProperty(context, i18key, context.getLocale());
    }
    /**
     * @description  读取Page文件
     *          调用实例
     *          Properties prop = readPageObject(context, "NioJDConfig");
     * 		String specialStr = prop.getProperty("nio.AttributeSpecial.StringList");
     * @author caipan
     * @param[1] _context
     * @param[2] pageObjectName
     * @throws

     * @time 2023/8/17 14:28
     */
    public static Properties readPageObject(Context _context, String pageObjectName) throws Exception {
        Properties propertyEntry = new Properties();
        Page pageAttributePopulation = new Page(pageObjectName);
        pageAttributePopulation.open(_context);
        String strProperties = pageAttributePopulation.getContents(_context);
        pageAttributePopulation.close(_context);
        InputStream input = new ByteArrayInputStream(strProperties.getBytes("UTF8"));
        propertyEntry.load(input);
        return propertyEntry;
    }

    /**
     * @description 根据语言自动获取page的国际化文件，只支持自定义的 OOTB的改动容易出现问题
     *  目前默认有两个 一个是原生的 一个是带 _zh的properties ，后续有需要可以按需扩展
     *  调用实例
     *  getPageResourceFile(context, "cus", "zh", "nio.message.hello");
     * @author caipan
     * @param[1] context 上下文
     * @param[2] pageName Page文件名 ，不带.properties 和 _zh
     * @param[3] language 语言环境 zh(中文) or 其他
     * @param[4] key 配置的key
     * @throws

     * @time 2023/8/17 14:42
     */
    public static String getPageResourceFile(Context context,String pageName,String language,String key) throws Exception{
        String message =key;
        String pageNamebak =pageName;
        if(language.contains("zh")){
            pageName=new StringBuffer(pageName).append("_zh.properties").toString();
        }else{
            pageName=new StringBuffer(pageName).append(".properties").toString();
        }
        log.info("pageName:{}",pageName);
        Properties prop = readPageObject(context, pageName);
        message = prop.getProperty(key);
       if(UIUtil.isNotNullAndNotEmpty(message)){
           return  message;
        }else{
            log.info("key {} is not exist {} page:",key,pageName);
            //如果当前资源获取不到的话，去原生的properties去读取
             prop = readPageObject(context, new StringBuffer(pageNamebak).append(".properties").toString());
           log.info("go to {}.properties get key {}",pageNamebak,key);
           message = prop.getProperty(key);
           if(UIUtil.isNotNullAndNotEmpty(message)){
               return  message;
           }else {
               return key;
           }
        }

    }
    /**
     * @description 读取Excel文件内容
     * templatepath 文件路径
     * @author caipan
     * @param[1] context
     * @param[2] args
     * @throws

     * @time 2023/8/18 10:16
     */
    public MapList readExcel(Context context,String[] args) throws Exception{
        String templatepath = "D:\\D\\vpmList.xlsx";
        File fs = new File(templatepath);
        FileInputStream fis = new FileInputStream(fs);
        XSSFWorkbook XSSFwb = new XSSFWorkbook(fis);
        SXSSFWorkbook SXSSFwb = new SXSSFWorkbook(XSSFwb);
        XSSFWorkbook wb = SXSSFwb.getXSSFWorkbook();
        XSSFSheet sheet = wb.getSheetAt(0);
        log.info("start");
        MapList alldataList = null;//JF_SSExcelUtils_mxJPO.getMapList((XSSFSheet) sheet, 1);//调用工具类读取里面的内容
        return alldataList;
    }
    /**
     * @description 把结果输出到Excel中
     * @author caipan
     * @param[1] eBOMExpandItem 结果
     * @param[2] templatepath 模版目录 (根据模版的第一行的值，获取 MapList里面的key 进行写入)
     * @param[3] savepath 保存的路径
     * @throws

     * @time 2023/8/18 10:19
     */
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

    /**
     * 返回物理产品关联的Model
     * @param context
     * @param pidList VPMReference的物理ID集合
     * @return VPMReference关联的Model集合
     */
   /* public Map<String, StringList> getMultipleConfigurationContextInfo(Context context, StringList pidList) {
        Map<String, StringList> resultMap = new HashMap<>();
        boolean isStartTransaction = false;
        try {
            log.info("getMultipleConfigurationContextInfo>>all>>>>pidList = " + pidList);
            if (!ContextUtil.isTransactionActive(context)) {
                ContextUtil.startTransaction(context, true);
                isStartTransaction = true;
            }
            JsonObjectBuilder requestParametersJsonBuilder = Json.createObjectBuilder();
            requestParametersJsonBuilder.add("version", "1.0");
            requestParametersJsonBuilder.add("modelMask", "Read");
            requestParametersJsonBuilder.add("enabledCriteria", "YES");
            requestParametersJsonBuilder.add("cfgCtxt", "YES");
            JsonArrayBuilder pidArray = FLJsonUtil.BUILDER_FACTORY.createArrayBuilder();
            for (String pid : pidList) {
                pidArray.add(pid);
            }
            requestParametersJsonBuilder.add("pidList", pidArray);
            String requestParameterStr = requestParametersJsonBuilder.build().toString();
            log.info("getMultipleConfigurationContextInfo>>>all>>>requestParameterStr = " + requestParameterStr);
            JsonObjectBuilder retJsonBuilder = JSonUtilities.getMultipleConfigurationContextInfoAsJsonBuilder(context, new GetMultipleConfigurationContextInfoArgs(requestParameterStr));
            JsonObject retJson = retJsonBuilder.build();
            log.info("getMultipleConfigurationContextInfo>>>all>>>retJson = " + retJson);
            if (retJson.containsKey("contextInfo")) {
                JsonArray contextInfoArray = retJson.getJsonArray("contextInfo");
                for (int contextInfoIndex = 0; contextInfoIndex < contextInfoArray.size(); contextInfoIndex++) {
                    JsonObject contextInfoJson = contextInfoArray.getJsonObject(contextInfoIndex);
                    if (contextInfoJson.containsKey("modelPID") && contextInfoJson.containsKey("contextOf")) {
                        JsonArray contextOfArray = contextInfoJson.getJsonArray("contextOf");
                        for (int contextOfArrayIndex = 0; contextOfArrayIndex < contextOfArray.size(); contextOfArrayIndex++) {
                            String pid = contextOfArray.getString(contextOfArrayIndex);
                            String modelPID = contextInfoJson.getString("modelPID");
                            StringList modelIdList;
                            if (resultMap.containsKey(pid)) {
                                modelIdList = resultMap.get(pid);
                            } else {
                                modelIdList = new StringList();
                            }

                            if (!modelIdList.contains(modelPID)) {
                                modelIdList.add(modelPID);
                                resultMap.put(pid, modelIdList);
                            }
                        }
                    }
                }
            }
            if (isStartTransaction) {
                ContextUtil.commitTransaction(context);
                isStartTransaction = false;
            }
        } catch (Exception e) {
            log.error("getMultipleConfigurationContextInfo>>>all>>>>Exception", e);
            if (isStartTransaction) {
                ContextUtil.abortTransaction(context);
            }
        }
        log.info("getMultipleConfigurationContextInfo>>>all>>>>resultMap" + resultMap);
        return resultMap;
    }
*/
    /**
     * 获取配置
     * @param context
     * @param args VPMReference的物理ID
     * @return model物理ID集合
     * @throws Exception
     */
   /* public StringList getMultipleConfigurationContextInfo(Context context, String[]args) throws Exception {
        StringList modelIdList = new StringList();
        try {
            String pid = args[0];
            StringList pidList = new StringList();
            pidList.add(pid);
            Map<String, StringList> multipleConfigurationContextInfoMap = getMultipleConfigurationContextInfo(context, pidList);
            if (multipleConfigurationContextInfoMap.containsKey(pid)) {
                modelIdList = multipleConfigurationContextInfoMap.get(pid);
            }
            log.info("getMultipleConfigurationContextInfo>>>>>>>Single>>>>>>>modelIdList=" + modelIdList);
        } catch (Exception e) {
            log.error("getMultipleConfigurationContextInfo>>>>>>>Single>>>>>>>Exception=", e);
            throw e;
        }
        return modelIdList;
    }*/
    /**
     * 设置配置上下文
     *
     * @param context
     * @param pidList 需要配置上下文的对象PhysicalId集合
     * @param attachModelIdList 添加的上下文对象PhysicalId集合
     * @param detachModelIdList 移除的上下文对象PhysicalId集合
     * @return
     * @throws Exception
     */
   /* public String setConfiguredObjectInfo(Context context, StringList pidList, StringList attachModelIdList, StringList detachModelIdList) throws Exception {
        String resultMsg;
        boolean isStartTransaction = false;
        try {
            if (!ContextUtil.isTransactionActive(context)) {
                ContextUtil.startTransaction(context,true);
                isStartTransaction = true;
            }
            JsonObjectBuilder requestParametersJsonBuilder = Json.createObjectBuilder();
            requestParametersJsonBuilder.add("version", "1.2");
            JsonArrayBuilder pidListArray = FLJsonUtil.BUILDER_FACTORY.createArrayBuilder();
            for (String pId : pidList) {
                pidListArray.add(pId);
            }
            requestParametersJsonBuilder.add("pidList", pidListArray.build());
            JsonObjectBuilder contentJson = FLJsonUtil.BUILDER_FACTORY.createObjectBuilder();
            JsonArrayBuilder attachCfgCtxtArray = FLJsonUtil.BUILDER_FACTORY.createArrayBuilder();
            for (String attachModelId : attachModelIdList) {
                attachCfgCtxtArray.add(attachModelId);
            }
            contentJson.add("attachCfgCtxt", attachCfgCtxtArray.build());

            JsonArrayBuilder detachCfgCtxtArray = FLJsonUtil.BUILDER_FACTORY.createArrayBuilder();
            for (String detachModelId : detachModelIdList) {
                detachCfgCtxtArray.add(detachModelId);
            }
            contentJson.add("detachCfgCtxt", detachCfgCtxtArray.build());

            JsonObjectBuilder enabledCriteriajsonMap = Json.createObjectBuilder();
            enabledCriteriajsonMap.add("feature", "true");
            enabledCriteriajsonMap.add("productState", "true");
            contentJson.add("enabledCriteria", enabledCriteriajsonMap);

            requestParametersJsonBuilder.add("content", contentJson.build());
            String requestParameters = requestParametersJsonBuilder.build().toString();
            log.info("setConfiguredObjectInfo>>>>>>>>Request parameters=" + requestParameters);

            ConfiguredObjectInfoRequiredFacets localObject1 = new ConfiguredObjectInfoRequiredFacets(requestParameters, "1", "0", "0");
            JsonObjectBuilder retJsonBuilder = JSonUtilities.setConfiguredObjectDetailsAsJsonBuilder(context, localObject1);
            JsonObject retJson = retJsonBuilder.build();
            String setConfiguredResult = retJson.getString("result");
            if ("SUCCEED".equalsIgnoreCase(setConfiguredResult)) {
                resultMsg = "success";
            } else {
                resultMsg = retJson.toString();
            }
            if (isStartTransaction) {
                ContextUtil.commitTransaction(context);
                isStartTransaction = false;
            }
        } catch (Exception e) {
            log.error("setConfiguredObjectInfo>>>>>>>>Exception=", e);
            resultMsg = e.getMessage();
            if (isStartTransaction) {
                ContextUtil.abortTransaction(context);
            }
        }
        log.info("setConfiguredObjectInfo>>>>>>>>resultMsg=" + resultMsg);
        return resultMsg;
    }*/
/**
 * @description 根据数据对象获取Person的上下文角色
 * @author caipan
 * @param[1] context
 * @param[2] objectId
 * @throws

 * @time 2023/8/18 10:51
 */
    public static String getDataOwnerAdminSecurityContext(Context context, String objectId) throws Exception {
        StringList generalBusSelects = new StringList();
        generalBusSelects.add(DomainObject.SELECT_ID);
        generalBusSelects.add(DomainObject.SELECT_TYPE);
        generalBusSelects.add(DomainObject.SELECT_NAME);
        generalBusSelects.add(DomainObject.SELECT_REVISION);
        generalBusSelects.add(DomainObject.SELECT_POLICY);
        generalBusSelects.add(DomainObject.SELECT_CURRENT);
        generalBusSelects.add(DomainObject.SELECT_OWNER);
        generalBusSelects.add(DomainObject.SELECT_PROJECT);
        generalBusSelects.add(DomainObject.SELECT_ORGANIZATION);
        generalBusSelects.add(DomainObject.SELECT_ORIGINATED);

        String securityContext = "";
        DomainObject doObj = DomainObject.newInstance(context, objectId);
        Map infoMap = doObj.getInfo(context, generalBusSelects);
        String sOwner = UIUtil.getValue(infoMap, DomainObject.SELECT_OWNER);
        String sProj = UIUtil.getValue(infoMap, DomainObject.SELECT_PROJECT);
        String sOrg = UIUtil.getValue(infoMap, DomainObject.SELECT_ORGANIZATION);
        log.info(sOrg + "." + sProj);
        Vector assignments = PersonUtil.getAssignments(context, sOwner);
        for (Object o : assignments) {
            String assignment = (String) o;
            log.info("assignment = " + assignment);
            if (assignment.contains("." + sOrg + "." + sProj)) {
                if (assignment.startsWith("ctx::VPLMProjectLeader")) {
                    securityContext = assignment;
                    break;
                }
                if (assignment.startsWith("ctx::VPLMCreator")) {
                    securityContext = assignment;
                }
            }
        }
        log.info("securityContext = " + securityContext);
        return securityContext;
    }

    /**
     * @description 得到上一个发布大版本,如果没有就返回空
     * @author caipan
     * @param[1] context
     * @param[2] vpmId
     * @throws

     * @time 2023/8/18 10:55
     */
    public String getPreviousReleasedMajorId (Context context, String vpmId) throws Exception {
        String previousReleasedId = "";
        try {
            String strSwitch = JF_PublicMethodClass_mxJPO.getBasicUrl(context, new String[]{"part.NewRevision"});
            if("on".equalsIgnoreCase(strSwitch)){
                previousReleasedId = getLastReleasedMajoridExcludeOwner(context, vpmId);//当前零件的上一个发布版本
            }else {
                BusinessObjectList majorRevisionsBusObjList = DomainObject.newInstance(context, vpmId).getMajorRevisions(context);
                DomainObject doObj;
                String sCurrent;
                //存在多个版本时，反之只有一个版本则返回空ID
                if (majorRevisionsBusObjList.size() > 1) {
                    //当前版本前一个版本的索引位置
                    int index = -1;
                    //找到当前版本之前的所有版本
                    for (int i = majorRevisionsBusObjList.size() - 1; i >= 0; i--) {
                        doObj = (DomainObject) majorRevisionsBusObjList.get(i);
                        String currentObjId = doObj.getInfo(context, DomainObject.SELECT_ID);
                        if (currentObjId.equals(vpmId) && i > 0) {
                            index = i - 1;
                            break;
                        }
                    }

                    if (index >= 0) {
                        for (int i = index; i >= 0; i--) {
                            doObj = (DomainObject) majorRevisionsBusObjList.get(i);
                            sCurrent = doObj.getInfo(context, DomainObject.SELECT_CURRENT);
                            if ("RELEASED".equals(sCurrent)) {
                                previousReleasedId = doObj.getInfo(context, DomainObject.SELECT_ID);
                                break;
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }

        return previousReleasedId;
    }

    /**
    * 获取零件的最新发布版本（排除冒泡）
    * @param context
	* @param vpmId
    * @author LIUJR
    * @throws
    * @return java.lang.String
    * @date 2026/6/4 15:27
    * @description
    */
    public static String getLastReleasedMajoridExcludeBubblingRev(Context context, String vpmId) throws Exception {
        String lastReleasedId = "";
        try
        {
            String strSwitch = JF_PublicMethodClass_mxJPO.getBasicUrl(context, new String[]{"part.NewRevision"});
            if("on".equalsIgnoreCase(strSwitch)){
                lastReleasedId = getLastReleasedMajoridExcludeBubblingRev_new(context, vpmId);//新版本规则获取最新版  排除冒泡
            } else  {
                BusinessObjectList majorRevisionsBusObjList = DomainObject.newInstance(context, vpmId).getMajorRevisions(context);
                DomainObject doObj;
                String sCurrent;
                String sRevision;
                if (majorRevisionsBusObjList.size() == 1) {
                    doObj = DomainObject.newInstance(context, vpmId);
                    sCurrent = doObj.getInfo(context, DomainObject.SELECT_CURRENT);
                    sRevision = doObj.getInfo(context, DomainObject.SELECT_REVISION);
                    if ("RELEASED".equals(sCurrent) && sRevision.endsWith("-000")) {
                        lastReleasedId = vpmId;
                    }
                } else {
                    for (int i = majorRevisionsBusObjList.size() - 1; i >= 0; i--) {
                        doObj = (DomainObject) majorRevisionsBusObjList.get(i);
                        sCurrent = doObj.getInfo(context, DomainObject.SELECT_CURRENT);
                        sRevision = doObj.getInfo(context, DomainObject.SELECT_REVISION);
                        if ("RELEASED".equals(sCurrent) && sRevision.endsWith("-000")) {
                            lastReleasedId = doObj.getInfo(context, DomainObject.SELECT_ID);
                            break;
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }

        return lastReleasedId;
    }

    /**
    * 得到最新发布版本 基于最新版本规则 AA.1-000  排除冒泡
    * @param context
	* @param vpmId
    * @author LIUJR
    * @throws
    * @return java.lang.String
    * @date 2026/6/4 15:28
    * @description
    */
    public static String getLastReleasedMajoridExcludeBubblingRev_new(Context context, String vpmId) throws Exception {
        String lastReleasedId = "";
        try {
            BusinessObjectList majorRevisionsBusObjList = DomainObject.newInstance(context, vpmId).getMajorRevisions(context);
            DomainObject doObj;
            String sCurrent;
            String sRevision;
            log.info("size:{}",majorRevisionsBusObjList.size());
            for (int i = majorRevisionsBusObjList.size() - 1; i >= 0; i--) {
                doObj = (DomainObject) majorRevisionsBusObjList.get(i);
                sCurrent = doObj.getInfo(context, DomainObject.SELECT_CURRENT);
                sRevision = doObj.getInfo(context, DomainObject.SELECT_REVISION);
                if (!sRevision.endsWith("-000")) {
                    majorRevisionsBusObjList.remove(i);
                } else if (!"RELEASED".equals(sCurrent)) {
                    majorRevisionsBusObjList.remove(i);
                }
            }
            log.info("size after:{}",majorRevisionsBusObjList.size());
            lastReleasedId = getMaxRevisionIdStream2(context,majorRevisionsBusObjList);
            log.info("lastReleasedId:{}",lastReleasedId);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
        return lastReleasedId;
    }

    /**
    * 得到上一个发布大版本,如果没有就返回空  排除冒泡版本
    * @param context
	* @param vpmId
    * @author LIUJR
    * @throws
    * @return java.lang.String
    * @date 2026/6/4 13:44
    * @description
    */
    public String getPreviousReleasedMajorIdExcludeBubblingRev (Context context, String vpmId) throws Exception {
        String previousReleasedId = "";
        try {
            String strSwitch = JF_PublicMethodClass_mxJPO.getBasicUrl(context, new String[]{"part.NewRevision"});
            if("on".equalsIgnoreCase(strSwitch)){
                previousReleasedId = getLastReleasedMajoridExcludeOwnerAndBubblingRev(context, vpmId);//当前零件的上一个发布版本
            }else {
                BusinessObjectList majorRevisionsBusObjList = DomainObject.newInstance(context, vpmId).getMajorRevisions(context);
                DomainObject doObj;
                String sCurrent;
                String sRevision;
                //存在多个版本时，反之只有一个版本则返回空ID
                if (majorRevisionsBusObjList.size() > 1) {
                    //当前版本前一个版本的索引位置
                    int index = -1;
                    //找到当前版本之前的所有版本
                    for (int i = majorRevisionsBusObjList.size() - 1; i >= 0; i--) {
                        doObj = (DomainObject) majorRevisionsBusObjList.get(i);
                        String currentObjId = doObj.getInfo(context, DomainObject.SELECT_ID);
                        if (currentObjId.equals(vpmId) && i > 0) {
                            index = i - 1;
                            break;
                        }
                    }

                    if (index >= 0) {
                        for (int i = index; i >= 0; i--) {
                            doObj = (DomainObject) majorRevisionsBusObjList.get(i);
                            sCurrent = doObj.getInfo(context, DomainObject.SELECT_CURRENT);
                            sRevision = doObj.getInfo(context, DomainObject.SELECT_REVISION);
                            if (sRevision.endsWith("-000") && "RELEASED".equals(sCurrent)) {
                                previousReleasedId = doObj.getInfo(context, DomainObject.SELECT_ID);
                                break;
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }

        return previousReleasedId;
    }
    /**
     * 根据vpmId获取最新发布版的Majorid
     *
     * @param context  the eMatrix Context object
     * @param vpmId
     * @return 返回最新发布版vpmId
     */
    public static String getLastReleasedMajorid(Context context, String vpmId) throws Exception {
        String lastReleasedId = "";
            try
            {
                String strSwitch = JF_PublicMethodClass_mxJPO.getBasicUrl(context, new String[]{"part.NewRevision"});
                if("on".equalsIgnoreCase(strSwitch)){
                lastReleasedId = getLastReleasedMajorid_new(context, vpmId);//新版本规则获取最新版
                 } else  {
                BusinessObjectList majorRevisionsBusObjList = DomainObject.newInstance(context, vpmId).getMajorRevisions(context);
                DomainObject doObj;
                String sCurrent;
                if (majorRevisionsBusObjList.size() == 1) {
                    doObj = DomainObject.newInstance(context, vpmId);
                    sCurrent = doObj.getInfo(context, DomainObject.SELECT_CURRENT);
                    if ("RELEASED".equals(sCurrent)) {
                        lastReleasedId = vpmId;
                    }
                } else {
                    for (int i = majorRevisionsBusObjList.size() - 1; i >= 0; i--) {
                        doObj = (DomainObject) majorRevisionsBusObjList.get(i);
                        sCurrent = doObj.getInfo(context, DomainObject.SELECT_CURRENT);
                        if ("RELEASED".equals(sCurrent)) {
                            lastReleasedId = doObj.getInfo(context, DomainObject.SELECT_ID);
                            break;
                        }
                    }
                }
                }
            } catch (Exception e) {
                e.printStackTrace();
                throw e;
            }

        return lastReleasedId;
    }

    public static String getLastReleasedMajorid(Context context, String[]args) throws Exception {
        return JF_Util_mxJPO.getLastReleasedMajorid(context,args[0]);
    }

   /* *//**
     * 通过接口创建CA
     *
     * @param context
     * @param args    args[0]创建类型
     *                args[1] policy
     *                args[2]标题
     * @return
     * @throws Exception
     *//*
    public static String manuallyCreateAChangeAction(Context context, String[] args) throws Exception {
        String changeActionId;
        try {
            String sType = args[0];
            String sPolicy = args[1];
            String sTitle = args[2];
            String caCreateUrl = "/resources/modeler/change/changeaction";
            String caCreateTemplateContent = "{\"version\":\"v0\",\"changeaction\":{\"name\":\"\",\"revision\":\"-\",\"type\":\"" + sType + "\",\"policy\":\"" + sPolicy + "\",\"description\":\"\",\"attributes\":[{\"name\":\"Synopsis\",\"value\":\"" + sTitle + "\"},{\"name\":\"Estimated Completion Date\",\"value\":\"\"},{\"name\":\"Severity\",\"value\":\"Low\"}]}}";
            JsonReader caCreateTemplateContentJsonReader = Json.createReader(new StringReader(caCreateTemplateContent));
            JsonObject caCreateTemplateContentJsonObj = caCreateTemplateContentJsonReader.readObject();
            //webservice的jar包 到时候需要放开
            JsonObject autoCreateCaResultJsonObj = null;*//*WebServiceHandler.executeWebService(context, HttpMethod.POST, caCreateUrl,
                    PersonUtil.getDefaultSecurityContext(context), "zh", caCreateTemplateContentJsonObj);*//*
            if (autoCreateCaResultJsonObj.containsKey("changeaction")) {
                JsonObject changeActionJsonObj = autoCreateCaResultJsonObj.getJsonObject("changeaction");
                changeActionId = changeActionJsonObj.getString("id");
                if (changeActionId.startsWith("pid:")) {
                    changeActionId = changeActionId.substring(4);
                } else {
                    throw new Exception("CA Creation Failed!");
                }
            } else {
                throw new Exception(autoCreateCaResultJsonObj.toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
        return changeActionId;
    }*/
/**
 * @description 判断是否包含Interface
 * @author caipan
 * @param[1] context
 * @param[2] objectId
 * @param[3] interfaceName
 * @throws

 * @time 2023/8/18 10:59
 */
    public static boolean HasInterface(Context context, String objectId, String interfaceName) throws Exception {
        try {
            DomainObject obj = DomainObject.newInstance(context, objectId);
            StringList interfaceList = obj.getInfoList(context, "interface");
            return interfaceList.contains(interfaceName);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 获取有效性
     *
     * @param context the eMatrix Context object
     * @param args    args[0] VPMInstance physicalid                args[1] 返回有效性格式,值为XML/TXT                args[2] 返回内容是名称,还是描述信息,值为YES/NO
     * @return 错误信息msg和有效性Variant multiple filterable object info
     */
  /*  public Map<String, String> getMultipleFilterableObjectInfo(Context context, String[] args) {
        Map<String, String> resultMap = new HashMap<>();
        try {
            if (null == args || args.length < 3) {
                throw new IllegalArgumentException();
            }

            String relPhysicalId = args[0];
            String targetFormat = args[1];//XML
            String withDescription = args[2];//NO

            JsonObjectBuilder queryParametersBuilder = Json.createObjectBuilder();
            queryParametersBuilder.add("version", "1.3");

            JsonObjectBuilder queryParametersForOutputBuilder = Json.createObjectBuilder();
            queryParametersForOutputBuilder.add("targetFormat", targetFormat);
            queryParametersForOutputBuilder.add("withDescription", withDescription);
            queryParametersForOutputBuilder.add("view", "Current");
            queryParametersForOutputBuilder.add("domains", "All");
            queryParametersBuilder.add("output", queryParametersForOutputBuilder.build());

            JsonArrayBuilder pidListArrayBuilder = FLJsonUtil.BUILDER_FACTORY.createArrayBuilder();
            pidListArrayBuilder.add(relPhysicalId);

            queryParametersBuilder.add("pidList", pidListArrayBuilder.build());

            String indexBased = "0";//是否基于索引,0表示不是，1表示是  适配22  21这块不一样
            GetMultipleFilterableObjectInfoRequiredFacets facets = null; //暂时注释 对应的jar包之后就可以放开
                   *//* new GetMultipleFilterableObjectInfoRequiredFacets(queryParametersBuilder.build().toString(), indexBased);*//*

            String resultMsg;
            JsonObject tMultipleFilterableObjectInfoResultJson = JSonUtilities.getMultipleFilterableObjectInfoAsJSON(context, facets);
            if (tMultipleFilterableObjectInfoResultJson.containsKey("error")) {
                resultMsg = tMultipleFilterableObjectInfoResultJson.getString("error");
                JsonReader errorJsonReader = Json.createReader(new StringReader(resultMsg));
                JsonObject errorJsonObj = errorJsonReader.readObject();
                errorJsonReader.close();
                resultMsg = errorJsonObj.getString("errorMessage");
                resultMap.put("msg", resultMsg);
                resultMap.put("Variant", "");
            } else {
                JsonObject expressions = tMultipleFilterableObjectInfoResultJson.getJsonObject("expressions");
                JsonObject pidRet = expressions.getJsonObject(relPhysicalId);
                String hasEffectivity = pidRet.getString("hasEffectivity");
                if ("NO".equalsIgnoreCase(hasEffectivity)) {
                    resultMap.put("msg", "复制的对象上没有配置有效性");
                    resultMap.put("Variant", "");
                } else {
                    JsonObject content = pidRet.getJsonObject("content");
                    String Variant = content.getString("Variant");
                    resultMap.put("msg", "");
                    resultMap.put("Variant", Variant);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            resultMap.put("msg", e.getMessage());
            resultMap.put("Variant", "");
        }
        return resultMap;
    }*/
    /**
     * 粘贴有效性
     *
     * @param context        the eMatrix Context object
     * @param securityCtx    上下文
     * @param effectivity    有效性
     * @param pasteRelIdList 粘贴关系physicalid
     * @return 错误信息 ，空表示成功
     */
   /* public String pasteEffectivity(Context context, String securityCtx, String effectivity, StringList pasteRelIdList) {
        String resultMsg = "";
        try {
//            String urlPaste = "/resources/modeler/configuration/authoringServices/pasteVariantEffectivities";
            JsonObjectBuilder parameterJsonBuilder = FLJsonUtil.BUILDER_FACTORY.createObjectBuilder();
            parameterJsonBuilder.add("version", "1.0");

            JsonObjectBuilder outputJsonBuilder = FLJsonUtil.BUILDER_FACTORY.createObjectBuilder();
            outputJsonBuilder.add("targetFormat", "XML");
            outputJsonBuilder.add("view", "Current");
            outputJsonBuilder.add("domains", "Variant");
            parameterJsonBuilder.add("output", outputJsonBuilder.build());

            JsonArrayBuilder pidListJsonArrayBuilder = FLJsonUtil.BUILDER_FACTORY.createArrayBuilder();
            for (String pasteRelId : pasteRelIdList) {
                pidListJsonArrayBuilder.add(pasteRelId);
            }
            parameterJsonBuilder.add("pidList", pidListJsonArrayBuilder.build());

            parameterJsonBuilder.add("VariantContent", effectivity);

//            JsonObject pasteEffectivityResultJson = WebServiceHandler.executeWebService(context, HttpMethod.POST, urlPaste,
//                    securityCtx, "zh", parameterJsonBuilder.build());
            *//*SetVariantEffectivitiesArgs setVariantEffectivitiesArgs = new SetVariantEffectivitiesArgs(parameterJsonBuilder.build().toString());
            JsonObject pasteEffectivityResultJson = JSonUtilities.setVariantEffectivities(context, setVariantEffectivitiesArgs);*//*
            JsonObjectBuilder jsonObjectBuilder = JSonUtilities.pasteVariantEffectivitiesAsJsonBuilder(context, new PasteVariantEffectivitiesArgs(parameterJsonBuilder.build().toString()));
            JsonObject pasteEffectivityResultJson = jsonObjectBuilder.build();
            log.info("pasteEffectivityResultJson = " + pasteEffectivityResultJson);
            if (pasteEffectivityResultJson.containsKey("error")) {
                resultMsg = pasteEffectivityResultJson.getString("error");
                try {
                    if (UIUtil.isNotNullAndNotEmpty(resultMsg) && resultMsg.startsWith("{") && resultMsg.endsWith("}")) {
                        JsonReader errorJsonReader = Json.createReader(new StringReader(resultMsg));
                        JsonObject errorJsonObj = errorJsonReader.readObject();
                        errorJsonReader.close();
                        resultMsg = errorJsonObj.getString("errorMessage");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                log.info("1721>>>>>>>>>>resultMsg = " + resultMsg);
            }
            new NioSSVariantEff_mxJPO().setEvolutionEff(context, pasteRelIdList);
        } catch (Exception e) {
            log.error("NioSSUtil_mxJPO.pasteEffectivity>>>>Exception", e);
            resultMsg = e.getMessage();
        }
        return resultMsg;
    }*/

    /**
     * 通过物料产品获取上下文
     *
     * @param context the eMatrix Context object
     * @param args    args[0] 物料产品 id
     * @return 模型的id集合 model phy ids by vpm id
     * @throws Exception 异常
     */
    public StringList getModelPhyIdsByVpmId(Context context, String[] args) throws Exception {
        String vpmId = args[0];
        String getModelPhyIdsMql = "pri bus '" + vpmId
                + "' select from[VPLMrel/PLMConnection/V_Owner|to.type=='VPMCfgContext']" +
                ".to.paths[SemanticRelation].path.element[0].physicalid dump";
        String modelPhyIds = MqlUtil.mqlCommand(context, false, getModelPhyIdsMql, true);
        return FrameworkUtil.split(modelPhyIds, ",");
    }
    /**
     * 根据用户名获取系统人员ID
     *
     * @param context  the eMatrix Context object
     * @param userName 用户名
     * @return 人员ID, 未查询到人员时返回空
     */
    public String getPersonIdByName(Context context, String userName) {
        String userId = "";
        try {
            if (UIUtil.isNotNullAndNotEmpty(userName)) {
                String selectPersonMql = "list user '" + userName + "' select isaperson dump";
                String isPerson = MqlUtil.mqlCommand(context, false, selectPersonMql, true);
                if ("TRUE".equals(isPerson)) {
                    userId = PersonUtil.getPersonObjectID(context, userName);
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return userId;
    }

    /**
     * 获取物理产品所关联的材料信息
     *
     * @param context     the eMatrix Context object
     * @param vpmObjectId 物理产品id
     * @param busSelects  需要查询的bo信息
     * @param relSelects  需要查询的rel信息
     * @return material info by vpm
     * @throws Exception the exception
     */
    public MapList getMaterialInfoByVpm(Context context, String vpmObjectId, StringList busSelects, StringList relSelects) throws Exception {
        MapList returnList = new MapList();
        try {
            if (UIUtil.isNullOrEmpty(vpmObjectId)) {
                throw new Exception("物理产品ID不能为空");
            }
            if (null == busSelects || busSelects.isEmpty()) {
                busSelects = ChangeUtil.getBasicObjectSelects();
            }
            if (!busSelects.contains("attribute[PLMEntity.V_Name]")) {
                busSelects.add("attribute[PLMEntity.V_Name]");
            }
            if (null == relSelects || relSelects.isEmpty()) {
                relSelects = ChangeUtil.getBasicRelSelects();
            }
            ContextUtil.pushContext(context);
            DomainObject vpmObj = DomainObject.newInstance(context, vpmObjectId);
            MapList matMapList = vpmObj.getRelatedObjects(context, "VPLMrel/PLMConnection/V_Owner", "dsc_mat_cnx_Core", busSelects, relSelects, false, true, (short) 1, "", "", 0);
            if (matMapList.size() > 0) {
                for (int i = 0; i < matMapList.size(); i++) {
                    Map map = (Map) matMapList.get(i);
                    String id = UIUtil.getValue(map, DomainObject.SELECT_ID);
                    String pathsInfo = MqlUtil.mqlCommand(context, false, "print bus " + id + " select paths[SemanticRelation].path.element[0].physicalid dump", true);
                    StringList ids = FrameworkUtil.splitString(pathsInfo, ",");
                    for (int k = 0; k < ids.size(); k++) {
                        id = ids.get(k);
                        DomainObject domObj = new DomainObject(id);
                        if (domObj.exists(context)) {
                            String strType = domObj.getInfo(context, "type");
                            if ("dsc_matref_ref_Core".equals(strType)) {
                                Map matInfo = domObj.getInfo(context, busSelects, false);
                                returnList.add(matInfo);
                            }
                        }
                    }
                }
            }
            ContextUtil.popContext(context);
        } catch (Exception e) {
            log.error(e.getMessage());
            throw e;
        }
        return returnList;
    }
    /**
     * 获取更改项的CA和CR信息
     *
     * @param context              the eMatrix Context object
     * @param affectedItemId       更改项Id
     * @param changeActionSelects  需要查询的CA内容，默认可传空
     * @param requestedChange      要求的更改类型 For Update/For Release/For Revise/For Obsolescence/None/For Major Revise
     * @param changeRequestSelects 需要查询的CR内容，默认可传空
     * @return cr and ca by affected item id
     * @throws Exception the exception
     */
    public MapList getCRAndCAByAffectedItemId(Context context, String affectedItemId, StringList changeActionSelects, String requestedChange, StringList changeRequestSelects) throws Exception {
        MapList changeActionMapList;
        try {
            changeActionMapList = getChangeActionByAffectedItemId(context, affectedItemId, changeActionSelects, requestedChange);
            if (null == changeRequestSelects || changeRequestSelects.isEmpty()) {
                changeRequestSelects = ChangeUtil.getBasicObjectSelects();
            }
            for (int i = 0; i < changeActionMapList.size(); i++) {
                Map changeActionMap = (Map) changeActionMapList.get(i);
                String caId = UIUtil.getValue(changeActionMap, DomainConstants.SELECT_ID);
                DomainObject caObj = DomainObject.newInstance(context, caId);
                MapList crMapList = caObj.getRelatedObjects(context, "Change Implementation", "Change Request", changeRequestSelects, ChangeUtil.getBasicRelSelects(), true, false, (short) 1, "", "", 0);
                if (crMapList.size() > 0) {
                    changeActionMap.put("changeRequestInfo", crMapList.get(0));
                } else {
                    changeActionMap.put("changeRequestInfo", new HashMap<>());
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage());
            throw e;
        }
        return changeActionMapList;
    }
    /**
     * 查找更改项关联的CA
     *
     * @param context             the context
     * @param affectedItemId      更改项Id
     * @param changeActionSelects 需要查询的CA内容，默认可传空
     * @param requestedChange     要求的更改类型 For Update/For Release/For Revise/For Obsolescence/None/For Major Revise
     * @return change action by affected item id
     * @throws Exception the exception
     */
    public MapList getChangeActionByAffectedItemId(Context context, String affectedItemId, StringList changeActionSelects, String requestedChange) throws Exception {
        MapList resultMapList = new MapList();
        try {
            if (UIUtil.isNullOrEmpty(affectedItemId)) {
                throw new Exception("更改项ID不能为空");
            }

            if (null == changeActionSelects || changeActionSelects.isEmpty()) {
                changeActionSelects = ChangeUtil.getBasicObjectSelects();
                changeActionSelects.add(DomainObject.SELECT_CURRENT);
            }

            Map paraMap = new HashMap();
            paraMap.put(ChangeConstants.OBJECT_ID, affectedItemId);
            paraMap.put("functionality", "isChangeActionTab");
            MapList retList = JPO.invoke(context, "enoECMChangeUtil", null, "getConnectedChanges", JPO.packArgs(paraMap), MapList.class);
            DomainObject caObj;
            for (int i = 0; i < retList.size(); i++) {
                Map caMap = (Map) retList.get(i);
                String caId = UIUtil.getValue(caMap, DomainObject.SELECT_ID);
                caObj = DomainObject.newInstance(context, caId);
                Map caObjInfoMap = caObj.getInfo(context, changeActionSelects);
                caMap.putAll(caObjInfoMap);
                String sRequestedChange = UIUtil.getValue(caMap, "Requested Change");
                if ((UIUtil.isNotNullAndNotEmpty(requestedChange) && sRequestedChange.equals(requestedChange))|| UIUtil.isNullOrEmpty(requestedChange)) {
                    resultMapList.add(caMap);
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage());
            throw e;
        }
        return resultMapList;
    }

    /**
     * 获取下一个版本ID，如果自身就是最新版则返回空
     *
     * @param context the eMatrix Context object
     * @param vpmId   the vpm id
     * @return next version id
     * @throws Exception the exception
     */
    public String getNextVersionId(Context context, String vpmId) throws Exception {
        String nextId = "";
        DomainObject doObj = DomainObject.newInstance(context, vpmId);
        String name = doObj.getInfo(context, DomainObject.SELECT_NAME, false);
        String isLast = doObj.getInfo(context, "attribute[PLMReference.V_isLastVersion]", false);
        if (!"TRUE".equalsIgnoreCase(isLast)) {
            String busWhere = "majorid.previousmajorid == '" + vpmId + "'";
            MapList nextIdMapList = DomainObject.findObjects(context, "VPMReference", name, "*", "*", doObj.getVault(context), busWhere, false, ChangeUtil.getBasicObjectSelects());
            if (nextIdMapList.size() > 0) {
                nextId = UIUtil.getValue((Map) nextIdMapList.get(0), DomainObject.SELECT_ID);
            }
        }
        return nextId;
    }


    /**
     * 将CA的建议更改项移动到另外一个CA的建议的更改项中（前提两个CA在同一个CR下）
     *
     * @param context the eMatrix Context object
     * @param args    args[0] 旧CA Id                args[1] 新CA Id
     * @throws Exception the exception
     */
    public void moveCAAffectedItemsToExistingCA(Context context, String[] args) throws Exception {
        boolean isTransactionActive = false;
        try {
            if (!ContextUtil.isTransactionActive(context)) {
                ContextUtil.startTransaction(context, true);
                isTransactionActive = true;
            }
            String oldCAId = args[0];
            String newCAId = args[1];
            if (UIUtil.isNotNullAndNotEmpty(oldCAId) || UIUtil.isNotNullAndNotEmpty(newCAId)) {
                String crId = MqlUtil.mqlCommand(context, "pri bus '" + oldCAId + "' select to[Change Implementation|from.type=='Change Request'].from.id dump", true,true);
                MapList affectedItemMapList = new ChangeAction(oldCAId).getAffectedItems(context);
                StringList affectedItemIdList = new ChangeUtil().getStringListFromMapList(affectedItemMapList, DomainConstants.SELECT_ID);
                StringList relIdList = new ChangeUtil().getStringListFromMapList(affectedItemMapList, DomainConstants.SELECT_RELATIONSHIP_ID);
                ChangeOrder changeRequest = new ChangeOrder(crId);
                changeRequest.moveToChangeAction(context, relIdList, affectedItemIdList, newCAId);
            } else {
                throw new Exception("新旧CA ID均不能为空");
            }
        } catch (Exception e) {
            log.error(e.getMessage());
            if (isTransactionActive) {
                ContextUtil.abortTransaction(context);
            }
            throw e;
        } finally {
            if (isTransactionActive) {
                ContextUtil.commitTransaction(context);
            }
        }
    }
    /**
     * 更新CA和建议更改项的Request Change值
     *
     * @param context the eMatrix Context object
     * @param args    args[0] CA和Proposed Activity的关系id   args[1] 建议的更改项id    args[2] Request Change新值
     * @return int int
     * @throws Exception the exception
     */
    public int updateRequestedChange(Context context, String[] args) throws Exception {
        int resultInt;
        boolean startTransaction = false;
        try {
            if (!ContextUtil.isTransactionActive(context)) {
                ContextUtil.startTransaction(context, true);
                startTransaction = true;
            }

            if (args.length < 3) {
                emxContextUtil_mxJPO.mqlNotice(context, "所传参数个数不对");
                return 1;
            }

            String relId = args[0];
            String affectedItemId = args[1];
            String newRequestedChangeVal = args[2];

            String caId;
            if (UIUtil.isNullOrEmpty(relId)) {
                emxContextUtil_mxJPO.mqlNotice(context, "CA和Proposed Activity的关系id不能为空");
                return 1;
            } else {
                String fromType = MqlUtil.mqlCommand(context, false, "pri connection '" + relId + "' select from.type" +
                        " dump", true);
                if (mxType.isOfParentType(context, fromType, "Change Action")) {
                    caId = MqlUtil.mqlCommand(context, false, "pri connection '" + relId + "' select from.id dump",
                            true);
                } else {
                    emxContextUtil_mxJPO.mqlNotice(context, "所传参数relId不是CA和Proposed Activity的关系id");
                    return 1;
                }
            }

            if (UIUtil.isNullOrEmpty(affectedItemId)) {
                emxContextUtil_mxJPO.mqlNotice(context, "建议的更改项id不能为空");
                return 1;
            }

            Map paramMap = new HashMap();
            paramMap.put(ChangeConstants.NEW_VALUE, newRequestedChangeVal);
            paramMap.put("objectId", affectedItemId);
            paramMap.put("relId", relId);

            Map requestMap = new HashMap();
            requestMap.put("objectId", "");

            Map programMap = new HashMap();
            programMap.put(ChangeConstants.PARAM_MAP, paramMap);
            programMap.put(ChangeConstants.REQUEST_MAP, requestMap);

            enoECMChangeActionBase_mxJPO changeActionJPO = new enoECMChangeActionBase_mxJPO(context, new String[]{caId});
            resultInt = changeActionJPO.updateRequestedChange(context, JPO.packArgs(programMap));
        } catch (Exception e) {
            if (startTransaction) {
                ContextUtil.abortTransaction(context);
                startTransaction = false;
            }
            log.error(e.getMessage());
            emxContextUtil_mxJPO.mqlNotice(context, e.getMessage());
            resultInt = 1;
        } finally {
            if (startTransaction) {
                ContextUtil.commitTransaction(context);
            }
        }
        return resultInt;
    }
    /**
     * 将系统格式转换为任意格式
     *
     * @param patten the patten
     * @param value  the value
     * @return string string
     * @throws Exception the exception
     */
    public static String FormatDateToAny(String patten, String value) {
        String dateStr = "";
        try {
            if (UIUtil.isNotNullAndNotEmpty(value)) {
                //DateTimeFormatter formatter = DateTimeFormatter.ofPattern(patten);
                SimpleDateFormat targetDateFormat = new SimpleDateFormat(patten);
                //dateStr = LocalDateTime.parse(value, MATRIX_FORMAT).format(formatter);
                SimpleDateFormat systemDateFormat = new SimpleDateFormat(eMatrixDateFormat.getEMatrixDateFormat(), Locale.US);
                dateStr = targetDateFormat.format(systemDateFormat.parse(value));
            }
        } catch (Exception e) {
            log.error("FormatDateToAny => ", e);
        }
        return dateStr;
    }

    /**
     * 将任意日期格式转换为系统格式
     *
     * @param patten the patten
     * @param value  the value
     * @return string string
     * @throws Exception the exception
     */
    public static String FormatDateToMatrix(String patten, String value) {
        String dateStr = "";
        try {
            if (UIUtil.isNotNullAndNotEmpty(value)) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern(patten);
                dateStr = MATRIX_FORMAT.format(formatter.parse(value));
            }
        } catch (Exception e) {
            log.error("FormatDateToMatrix => ", e);
        }
        return dateStr;
    }

    /**
     * 关联建议的更改
     *
     * @param context         the context
     * @param caObjectId      the ca object id
     * @param affectedItemIds the affected item ids
     * @return map map
     * @throws Exception the exception
     */
    public Map connectAffectedItems(Context context, String caObjectId, StringList affectedItemIds) throws Exception {
        Map mpInvalidObjects;
        try {
            mpInvalidObjects = new ChangeAction(caObjectId).connectAffectedItems(context, affectedItemIds);
        } catch (Exception e) {
            log.error(e.getMessage());
            throw e;
        }
        return mpInvalidObjects;
    }

    /**
     * 根据已实施的更改获取对应的CA
     *
     * @param context
     * @param objectId
     * @return
     * @throws Exception
     */
    public static MapList getChangeListByInRealized(Context context, String objectId) throws Exception {
        String[] idArr = {objectId};
        Map objectMap = ChangeUtil.getChangeObjectsInRealized(context,
                new StringList("id"), idArr, 1);
        return (MapList) objectMap.get(objectId);
    }

    /**
     * 根据建议的更改获取对应的CA
     *
     * @param context
     * @param objectId
     * @return
     * @throws Exception
     */
    public static MapList getChangeListByInProposed(Context context, String objectId) throws Exception {
        Map paraMap = new HashMap();
        paraMap.put(ChangeConstants.OBJECT_ID, objectId);
        paraMap.put("functionality", "isChangeActionTab");
        return JPO.invoke(context, "enoECMChangeUtil", null, "getConnectedChanges", JPO.packArgs(paraMap), MapList.class);
    }

    public static void test(Context context,String[] args) throws Exception{
        log.info(getPageResourceFile(context, "cus", "zh", "nio.message.hello"));
        log.info(getPageResourceFile(context, "cus", "zh", "nio.message.hello"));
        log.info(getPageResourceFile(context, "cus", "zh", "nio.message.hello22"));
        log.info("Message:{}",getMessage(context,"Message.promoteToReleased","param"));
    }
    /**
     * 创建项目任务
     **
     * @param context
     * @param args 任务名称、类型、Owner和父对象参数
     * @return String 任务ID
     * @throws Exception
     * @author caipan
     * @date 2026/7/16 16:13
     */
    public String createTask(Context context,String[] args) throws  Exception{
        ContextUtil.pushContext(context);
        try {
            Map requestMap = JPO.unpackArgs(args);
            log.info("requestMap:{}",requestMap);
            String  taskName = UIUtil.getValue(requestMap, "taskName");
            String  taskType = UIUtil.getValue(requestMap, "taskType");//注册名 type_JF_SignTask
            String  taskOwner = UIUtil.getValue(requestMap, "taskOwner");
            String  parentId = UIUtil.getValue(requestMap, "parentId");//父类的ID ，比如ECO、DA、ECR
            String  assign = UIUtil.getValue(requestMap, "assign");//父类的ID ，比如ECO、DA、ECR
            String  taskProject = getObjectProject(context, parentId);//UIUtil.getValue(requestMap, "taskProject");
            String  taskOrganization =getPersonOrganization(context, taskOwner);// UIUtil.getValue(requestMap, "taskOrganization");//根据人员获取组织
            String taskId = FrameworkUtil.autoName(context, taskType, "policy_ProjectTask");
            DomainObject taskObj = DomainObject.newInstance(context);
            taskObj.setId(taskId);
            Map attributeMap = new HashMap();
            attributeMap.put("Title", taskName);
            taskObj.setAttributeValues(context, attributeMap);
            StringBuffer mqlStr = new StringBuffer();
            mqlStr.append("mod bus '").append(taskId).append("' project '").append(taskProject).append("' organization '").append(taskOrganization).append("' owner '").append(taskOwner).append("'");
            log.info("mqlStr:{}",mqlStr);
            MqlUtil.mqlCommand(context, true, mqlStr.toString(), false);
            //添加受托人
            String personId = PersonUtil.getPersonObjectID(context, taskOwner);
            Task task = new Task(taskId);
            if(!"false".equalsIgnoreCase(assign)) {
                log.info("personId:{} task {}",personId,taskId);
                assignPerson(context, personId, task);
            }

           // taskObj.promote(context);//提升到工作中
            MqlUtil.mqlCommand(context, true, "mod bus "+taskId+" current Assign", false);//去掉触发发邮件
            return taskId;
        } finally {
            ContextUtil.popContext(context);
        }
    }

    public void testTask(Context context,String[] args) throws Exception{
        Map map = new HashMap();
        map.put("taskName","财务BP");//任务标题
        map.put("taskType","type_JF_APRTask");//任务类型 注册名
        map.put("taskOwner","ZHANGYK");//任务owner
        map.put("parentId","JFSeat");//父ID
        createTask(context,JPO.packArgs(map));
    }

    /*
     * @description: Task分配人员
     * @author: caipan
     * @date:
     * @param: * @param[1] context
     * @param[2] personId
     * @param[3] task Task对象
     * @return:
     **/
    public void assignPerson(Context context, String personId, Task task) throws Exception{
        task.addAssignee(context, personId, "Task Assignee");
    }
    /*
     * @description: 删除任务
     * @author: caipan
     * @date:
     * @param: * @param[1] context
     * @param[2] taskId
     * @return:
     **/
    public static void deltask(Context context,String taskId)throws Exception{
            //任务关系Id
        try {
            ContextUtil.pushContext(context);
            Map map = com.matrixone.apps.program.Task.deleteTasks(context, new StringList(taskId), false);
            log.info("删除任务返回信息map:{}", map);
        }catch (Exception e){
            log.info("deltask error:{}",e.getMessage());
        }finally {
            ContextUtil.popContext(context);
        }
    }

    public void copyTask(Context context,String[] args) throws Exception{
        Map paramMap = new HashMap(1);
        paramMap.put("objectId",args[0]);//挂在那个下面 选择的节点
        paramMap.put("SeachProjectOID",args[0]);//复制来源对象ID 选择的任务ID
        String[] methodArgs = JPO.packArgs(paramMap);
        String[] result =  JPO.invoke(context,"emxProjectSpace", null, "copyPartialScheduleProcess", methodArgs, String[].class);

    }

    public static StringList basicBolistSel(){
        StringList boSel = new StringList();
        boSel.add(DomainConstants.SELECT_ID);
        boSel.add(DomainConstants.SELECT_NAME);
        boSel.add(DomainConstants.SELECT_TYPE);
        boSel.add(DomainConstants.SELECT_REVISION);
        boSel.add(DomainConstants.SELECT_CURRENT);
        boSel.add(DomainConstants.SELECT_ATTRIBUTE_TITLE);
        boSel.add(DomainConstants.SELECT_OWNER);
        return boSel;
    }
    public static StringList basicRellistSel(){
        StringList relSel = new StringList();
        relSel.add(DomainConstants.SELECT_RELATIONSHIP_ID);
        return relSel;
    }
    /**
     * @description 调用OOTB的webservice promote数据
     * @author caipan
     * @param[1] context
     * @param[2] phyId 物品产品的ID
     *  @param[3] nextStateStr ToFreeze\ToRelease
     * @throws

     * @time 2023/8/11 17:28
     */
    public boolean promoteVPM(Context context,StringList phyIdList,String nextStateStr) throws Exception {
        log.info(" start promoteVPM phyIdList:{}",phyIdList);
        boolean flag = false;
        try {
            ContextUtil.pushContext(context);
            MaturityResource service = new MaturityResource();
            JSONObject rootObj = new JSONObject();
            JSONArray array = new JSONArray();
            for (int k = 0; k < phyIdList.size(); k++) {
                String phyId = phyIdList.get(k);
                DomainObject vpmObj = new DomainObject();
                vpmObj.setId(phyId);
                StringList selList = new StringList();
                selList.add("physicalid");
                selList.add("name");
                selList.add("revision");
                selList.add("type");
                selList.add("policy");
                selList.add("attribute[PLMEntity.V_Name]");
                selList.add("current");
                selList.add("state");
                Map attributeMap = vpmObj.getInfo(context, selList);
                JSONObject objsub = new JSONObject();
                String nextState = getNextState(context, phyId);
                if (UIUtil.isNotNullAndNotEmpty(nextState)) {
                    objsub.put("physicalid", attributeMap.get("physicalid"));
                    objsub.put("tostate", nextState);
                    objsub.put("fromstate", attributeMap.get("current"));
                    objsub.put("type", attributeMap.get("type"));
                    objsub.put("revision", attributeMap.get("revision"));
                    objsub.put("policy", attributeMap.get("policy"));
                    objsub.put("name", attributeMap.get("name"));
                    objsub.put("coretype", "Reference");
                    objsub.put("signature", nextStateStr);//current.signature  可能存在多个签名信息  在数模这种有签名的对象上面需要改动
                    objsub.put("cadMaster", "3DEXPERIENCE");
                    array.add(objsub);
                }
            }
            rootObj.put("data", array);//如果需要批量提升，添加多个对象
            rootObj.put("notificationTimeout", 3600);
            com.matrixone.json.JSONObject json = service.maturityPromote_internal(context, rootObj.toString());

            if (json.contains("status")) {
                if ("success".equalsIgnoreCase(json.get("status").toString())) {
                    flag = true;
                } else {
                    throw new Exception("promote error" + json.toString());
                }
            }
            return flag;
        }catch (Exception e) {
            throw new Exception("promote release error:"+e.getMessage());
//            return flag;
        }finally {
            ContextUtil.popContext(context);
        }
    }
    public  void testPromoteToRelease(Context context,String[]args) throws Exception{
//        ContextUtil.pushContext(context);
        StringList phyList = new StringList();
        for(int i=0;i<args.length;i++) {
            phyList.add(args[i]);
        }
        promoteVPM(context, phyList,"ToRelease");
//        ContextUtil.popContext(context);
    }

    public  void testPromoteToFreeze(Context context,String[] args) throws Exception{
//        ContextUtil.pushContext(context);
        StringList phyList = new StringList();
        for(int i=0;i<args.length;i++) {
            phyList.add(args[i]);
        }
        promoteVPM(context, phyList,"ToFreeze");
//        ContextUtil.popContext(context);
    }

    /**
     * @description 得到对象的下一个状态
     * @author caipan
     * @param[1] context
     * @param[2] objId 对象的ID
     * @throws

     * @time 2023/8/14 14:24
     */
    public static String getNextState(Context context ,String objId) throws Exception{
        DomainObject obj = new DomainObject(objId);
        StringList selList = new StringList();
        selList.add("state");
        selList.add("current");
        Map map = obj.getInfo(context, selList);
        System.out.println(map.entrySet());
        StringList stateList = (StringList)map.get("state");
        String current = (String)map.get("current");
        String nextCurrent = "";
        if(stateList.contains(current)) {
            for (int i = 0; i < stateList.size(); i++) {
                if (current.equalsIgnoreCase(stateList.get(i)) && i < stateList.size() - 1) {
                    nextCurrent = stateList.get(i + 1);
                    break;
                }
            }
        }
        return nextCurrent;
    }

    /**
    * 拿取人员的组织 和协作区
    * @param context
	* @param personName 人员名称
    * @author LIUJR
    * @throws
    * @return java.lang.String
    * @date 2024/7/30 15:10
    * @description
    */
    public  static String getPersonOrganization(Context context, String personName) throws Exception{
        String organization = DomainConstants.EMPTY_STRING;
        //当人员都没有部门和业务单位的时候 获取他的property
        String mql = "print Person $1 select $2 dump $3;";
        String result = MqlUtil.mqlCommand(context, true, false, mql, true, personName, "assignment", "@");
        log.info("result:{}", result);
        String[] split = result.split("@");
        for (int j = 0; j < split.length; j++) {
            String assignment = split[j];
            if (assignment.contains("ctx::VPLMCreator") || assignment.contains("ctx::VPLMProjectLeader")) {
                log.info("assignment:{}", assignment);
                String[] split1 = assignment.split("\\.");
                organization = split1[1];
                break;
            }
        }
        log.info("organization:{}", organization);
        return organization;
    }

    /**
     * 拿取人员的组织 和协作区
     * @param context
     * @param personName 人员名称
     * @author LIUJR
     * @throws
     * @return java.lang.String
     * @date 2024/7/30 15:10
     * @description
     */
    public  static String getObjectProject(Context context, String objectId) throws Exception{
        String project = DomainConstants.EMPTY_STRING;
        //当人员都没有部门和业务单位的时候 获取他的property
        String mql = "print bus $1 select $2 dump $3;";
        String result = MqlUtil.mqlCommand(context, true, false, mql, true, objectId, "project", "@");
        log.info("result:{}", result);
        if(UIUtil.isNotNullAndNotEmpty(result)){
            project = result;
        }
        log.info("project:{}", project);
        return project;
    }

    public void testProject(Context context,String[] args) throws Exception{
        String taskId = args[0];
        Map map = new HashMap();
        map.put("taskId", taskId);
        String[] list = JPO.packArgs(map);
        getProjectPhase(context, list);
    }
    /*
     * @description: 根据任务ID，获取项目Name和阶段
     * @author: caipan
     * @date:
     * @param: * @param[1] context
     * @param[2] args
     * @return:
     **/
    public static  Map getProjectPhase(Context context,String[] args) throws Exception{
        Map requestMap = JPO.unpackArgs(args);
        String taskId = UIUtil.getValue(requestMap, "taskId");
        //获取任务关联的项目
        DomainObject taskObj = DomainObject.newInstance(context);
        taskObj.setId(taskId);
        String projectName = taskObj.getInfo(context, "to[Project Access Key].from.from[Project Access List].to.name");
        //获取最顶点的阶段
        MapList objectList = taskObj.getRelatedObjects(context,
                "Subtask", //pattern to match relationships
                DomainConstants.TYPE_TASK_MANAGEMENT, //pattern to match types
                basicBolistSel(), //the eMatrix StringList object that holds the list of select statement pertaining to Business Obejcts.
                basicRellistSel(), //the eMatrix StringList object that holds the list of select statement pertaining to Relationships.
                true, //get To relationships
                false, //get From relationships
                (short) 0, //the number of levels to expand, 0 equals expand all.
                DomainConstants.EMPTY_STRING, //where clause to apply to objects, can be empty ""
                DomainConstants.EMPTY_STRING, //where clause to apply to relationship, can be empty ""
                (short)0, //limit
                false, //checkHidden
                true, //preventDuplicates
                (short)0, //pageSize
                null,
                null,
                null,
                "end") ;// end(返回叶子节点) relationship(返回关系pattern中最后一个关系) all(全返回);

        Map resultMap = new HashMap();
        resultMap.put("projectName",projectName);
        if(objectList.size()>0) {
            resultMap.put("phase", ((Map)objectList.get(0)).get(DomainConstants.SELECT_NAME));
        }else{
            resultMap.put("phase","");
        }
        log.info("getProjectPhase:{}",resultMap);
        return resultMap;
    }
    /*
     * @description: 获取Image
     * @author: caipan
     * @date:
     * @param: * @param[1] context
     * @param[2] args
     * @return:
     **/
    public Vector uploadImageBox(Context context, String[] args) throws Exception {
        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        MapList objectList = (MapList) programMap.get("objectList");
        Map paramList = (Map) programMap.get("paramList");
        Map sMCSURLMap = (Map) paramList.get("ImageData");
        String sMCSURL = (String) sMCSURLMap.get("MCSURL");
        log.info("sMCSURL===>{}", sMCSURL);
        int size = objectList.size();
        Vector vResult = new Vector(size);
        StringList busSel = new StringList();
        busSel.add(DomainConstants.SELECT_ID);
        busSel.add(DomainConstants.SELECT_NAME);
        busSel.add(DomainConstants.SELECT_TYPE);
        busSel.add(DomainConstants.SELECT_REVISION);
        busSel.add("attribute[Title]");
        String imageUrl = "";
        for (int i = 0; i < size; i++) {
            Map objectMap = (Map) objectList.get(i);
            log.info("map:{}",objectMap);
            String dmuTemplateId = (String) objectMap.get(ProgramCentralConstants.SELECT_ID);
            //dmuTemplateId = "21798.25570.35638.34209";
            DomainObject dmuTemplateObj = DomainObject.newInstance(context, dmuTemplateId);
            String imageHolder = dmuTemplateObj.getInfo(context, "to[Image Holder].from.id");
            if (UIUtil.isNullOrEmpty(imageHolder)) {
                imageUrl = "../components/images/emxDefaultThumbnail.jpg";
            } else {
                imageUrl = emxUtil_mxJPO.getPrimaryImageURL(context, null, dmuTemplateId, "mxThumbnail Image", sMCSURL, "");
            }
            StringBuilder sbResult = new StringBuilder();
            sbResult.append("<a href=\"javascript:link('','" + dmuTemplateId + "','','','')\">");
            sbResult.append("<table height=\"100%\" border=\"0\" class=\"mx_thumbnail-image\" cellpadding=\"0\" cellspacing=\"1\"><tbody><tr><td valign=\"top\" class=\"mx_thumbnail-image\"><div style=\"position: relative\" class=\"mx_image-container\">\n" +
                    "<a href=\"javascript:emxTableColumnLinkClick('" + StringEscapeUtils.escapeHtml4("../components/emxImageManager.jsp?HelpMarker=emxhelpimagesview&toolbar=APPImageManagerToolBar&objectId=" + dmuTemplateId) + "',850,650,'false','popup', '', '', '','Large','')\">");
            sbResult.append("<img style='position: relative;border:1px solid #c8c8c8' border='0' src='" + imageUrl + "' alt=\""
                    + "" + "\" height=\"" + "42" + "\"></img></a>");
            sbResult.append("</div></td></tr></tbody></table></a>");
            log.info("sb:{}",sbResult);
            vResult.add(sbResult.toString());
        }
        return vResult;
    }
    public void testcopyPart(Context context,String[] args) throws Exception{
//        log.info("partId:{}",copypart(context,args[0],args[1],args[2],args[3]));
    }
/*
 * @description: 复制竞品BOM模版
 * @author: caipan
 * @date:
 * @param: * @param[1] context
 * @param[2] partName 竞品的Name
 * @param[3] title 竞品输入的中文
 * @param[4] parentId 挂载到哪个上面，不会挂接过去，只是拿父对象的协作区
 * @param[5] owner 创建账号
 * @return:
 **/
    public static String copyCompetitiveBOMPart(Context context,String partName,String title,String parentId,String owner)throws Exception {
        String vpmId = "";
        try {
            ContextUtil.pushContext(context);
            String partRev = "-";
            String type = JF_PLMConstants_mxJPO.TYPE_JF_CompetitiveBOM;
            String[] arrgs = new String[1];
            arrgs[0] = "JF_CompetitiveBOM.Template.id";
            String copyTemplateId = JF_PublicMethodClass_mxJPO.getBasicUrl(context, arrgs);
            String mql = "copy bus $1 to $2 $3 $4 $5;";
            String mqlStr = "print bus $1 $2 $3 select $4 dump;";
            String mql2 = "mod bus $1 current $2;";
            MqlUtil.mqlCommand(context, true, false, mql, true, copyTemplateId, partName, partRev, "history", "!path");
             vpmId = MqlUtil.mqlCommand(context, true, false, mqlStr, true, type, partName, partRev, "id");
            Map<String, String> attMap = new HashMap<>();
            attMap.put("PLMEntity.PLM_ExternalID", partName);
            //attMap.put("EnterpriseExtension.V_PartNumber", partName);
            attMap.put("PLMEntity.V_Name", title);
            if (UIUtil.isNotNullAndNotEmpty(vpmId)) {
                DomainObject vpm = DomainObject.newInstance(context, vpmId);
                vpm.setAttributeValues(context, attMap);
                MqlUtil.mqlCommand(context, true, false, mql2, true, vpmId, "IN_WORK");
                changeowner(context, vpmId, parentId, owner);
            }
        } catch (Exception e) {
            log.info("error:{}",e.getMessage());
        } finally {
            ContextUtil.popContext(context);
        }
        return vpmId;
    }
    public static void changeowner(Context context,String objectId,String parentId,String owner)throws Exception{
        String org = getPersonOrganization(context, owner);
        String project = getObjectProject(context, parentId);
        if(UIUtil.isNotNullAndNotEmpty(org)){
            String mql2 = "mod bus $1 owner $2 organization $3 project $4;";
            MqlUtil.mqlCommand(context,false,false,mql2,true,objectId,owner,org,project);
        }
    }

    /*
     * @description: 判断是否是项目任务
     * @author: caipan
     * @date:
     * @param: * @param[1] context
 * @param[2] taskObj
     * @return:
     **/
    public static boolean isProjectTask(Context context,DomainObject taskObj) throws Exception{

        MapList objectList = taskObj.getRelatedObjects(context,
                DomainConstants.RELATIONSHIP_PROJECT_ACCESS_KEY, //pattern to match relationships
                DomainConstants.TYPE_PROJECT_ACCESS_LIST, //pattern to match types
                basicBolistSel(), //the eMatrix StringList object that holds the list of select statement pertaining to Business Obejcts.
                basicRellistSel(), //the eMatrix StringList object that holds the list of select statement pertaining to Relationships.
                true, //get To relationships
                false, //get From relationships
                (short) 1, //the number of levels to expand, 0 equals expand all.
                DomainConstants.EMPTY_STRING, //where clause to apply to objects, can be empty ""
                DomainConstants.EMPTY_STRING, //where clause to apply to relationship, can be empty ""
                (short)1 //limit
        );
        return objectList.size()>0;
    }

    /*
     * @description: 获取人员角色工具类
     * @author: caipan
     * @date:
     * @param: * @param[1] context
     * @return:
     **/
    public static List getPersonAssignments(Context context, String personName, boolean isRole) throws FrameworkException
    {
        try {

            String mql = "list person '" + personName + "' select assignment dump `";
            List list = null;
            String groupMqlResut = MqlUtil.mqlCommand(context, false,mql,true);
            String[] roles = groupMqlResut.split("`");
            list = Arrays.asList(roles);
            return list;
        } catch (Exception e) {
            throw new FrameworkException(e);
        }
    }
    /*
     * @description:是否包含哪种角色 JfITAdmin IT管理员
     * @author: caipan
     * @date:
     * @param: * @param[1] context
     * @param[2] args
     * @return:
     **/
    public static boolean isIncludeRole(Context context,String[] args) throws Exception{
        boolean flag = false;
        Map requestMap = JPO.unpackArgs(args);
        String roleName =UIUtil.getValue(requestMap, "roleName");
        String userName =UIUtil.getValue(requestMap, "userName");
        List list = getPersonAssignments(context,userName,true);
        if(list.contains(roleName)){
            flag = true;
        }
        return flag;
    }
    /*
     * @description:获取项目的项目经理人员账号
     *  如果项目经理角色不存在，就返回项目的owner
     *  如果项目经理角色有多个，返回第一个
     * @author: caipan
     * @date: 2025/2/7 15:51:44
     * @param: * @param[1] context
     * @param[2] args
     * @return:
     **/
    public static String getProjectManager(Context context,String[] args) throws Exception{
        String projectId = args[0];
        String accout = "";
        try {
            ContextUtil.pushContext(context);
            StringList relList = basicRellistSel();
            relList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_ProjectRole);
            DomainObject projectObj = DomainObject.newInstance(context, projectId);
            String relWhere = JF_PLMConstants_mxJPO.SELECT_ATTR_ProjectRole + "=='Project manager'";
            MapList objectList = projectObj.getRelatedObjects(context,
                    DomainConstants.RELATIONSHIP_MEMBER, //pattern to match relationships
                    DomainConstants.TYPE_PERSON, //pattern to match types
                    basicBolistSel(), //the eMatrix StringList object that holds the list of select statement pertaining to Business Obejcts.
                    relList, //the eMatrix StringList object that holds the list of select statement pertaining to Relationships.
                    false, //get To relationships
                    true, //get From relationships
                    (short) 1, //the number of levels to expand, 0 equals expand all.
                    DomainConstants.EMPTY_STRING, //where clause to apply to objects, can be empty ""
                    relWhere, //where clause to apply to relationship, can be empty ""
                    (short) 1 //limit
            );
//            log.info("objectList:{}",objectList);
            if (objectList.size() == 0) {
                //拿项目的owner
                accout = projectObj.getOwner(context).getName();
            } else {
                Map map = (Map) objectList.get(0);
                accout = UIUtil.getValue(map, DomainConstants.SELECT_NAME);
            }
        }finally {
            ContextUtil.popContext(context);
        }
        log.info("项目经理账号:{}",accout);
        return accout;
    }

    /*
     * @description: userName是否属于departmentName  或者当前用户有JfITAdmin角色
     * @author: caipan
     * @date: 2025/2/20 14:25:05
     * @param: * @param[1] context
     * @param[2] args
     * @return:
     **/
    public static boolean getPersonDepartment(Context context,String departmentName,String username) throws Exception{
        String strWhereExp = JF_PublicMethodClass_mxJPO.buildStringInStrings("name=='",departmentName,"'");
        String strLoginUserId = PersonUtil.getPersonObjectID(context,username);
        DomainObject loginUser = DomainObject.newInstance(context, strLoginUserId);
        MapList maps = loginUser.getRelatedObjects(context, "Member" , // relationship pattern
                "Department",                                    // object pattern
                JF_Util_mxJPO.basicBolistSel(),                            // object selects
                JF_Util_mxJPO.basicRellistSel(), // relationship selects
                true,                                        // to direction
                false,                                        // from direction
                (short) 1,                                    // recursion level
                strWhereExp,                // object where clause
                "",
                (short) 0);
        Map requestMap = new HashMap();
        requestMap.put("roleName", "JfITAdmin");
        requestMap.put("userName", username);
        boolean  flag = JF_Util_mxJPO.isIncludeRole(context, JPO.packArgs(requestMap));
        return (maps != null && maps.size() > 0)||flag;
    }
    public void testCheckIn(Context context,String[] args) throws Exception{
        String objectId = args[0];
        String sFileName = "Drawing_Template_20231007_JFSeat.CATDrawing.pdf";
        String sFileFormat= "PDF";
        String outputWinDir = "/data/";
        int i = checkinParameter(context, objectId,sFileName,sFileFormat,outputWinDir);
        log.info("i={}",i);
    }

    public void testCheckOut(Context context,String[] args) throws Exception{
        DomainObject pdfObj = DomainObject.newInstance(context,args[0]);
        checkOutFile(context, pdfObj, "Drawing_Template_20231007_JFSeat.CATDrawing.pdf", "/data/cp/");
    }
    /*
     * @description: 下载文件
     * @author: caipan
     * @date: 2025/4/1 14:06:30
     * @param: * @param[1] context
     * @param[2] pdfObj 上传对象
     * @param[3] fileName 文件名
     * @param[4] outputWinDir 下载目录
     * @return:
     **/
    public void checkOutFile(Context context,DomainObject pdfObj,String fileName,String outputWinDir) throws Exception{
        pdfObj.checkoutFile(context, false, "PDF", fileName, outputWinDir);
    }
    /*
     * @description:上传文件
     * @author: caipan
     * @date: 2025/4/1 13:57:57
     * @param: * @param[1] context
     * @param[2] objectId
     * @param[3] sFileName
     * @param[4] sFileFormat
     * @param[5] outputWinDir
     * @return: 是否成功 0 成功
     **/
    public int checkinParameter(Context context, String objectId, String sFileName, String sFileFormat,String outputWinDir)
            throws Exception {
        String[] argFiles = new String[8];
        argFiles[0] = objectId; // bus id
        argFiles[1] = outputWinDir + File.separator;
        argFiles[2] = sFileName;
        argFiles[3] = sFileFormat;
        argFiles[4] = "STORE";
        argFiles[5] = "TRUE";
        argFiles[6] = "";
        argFiles[7] = "checkIn by Sign";
        int result = checkinBus_Zh(context, argFiles);
        try {
            //new File(outputWinDir + File.separator + sFileName).delete();
        } catch (Exception e) {
            // do nothing
        }
        return result;
    }
    public static int checkinBus_Zh(Context context, String[] args) throws Exception {
        String oid = null;
        String filePath = null;
        String fileName = null;
        String format = null;
        String store = null;
        String unlock = null;
        String server = null;
        String comments = null;
        String oldFileName = null;
        try {

            try {
                oid = args[0];
                filePath = args[1];
                fileName = args[2];
                format = args[3];
                store = args[4];
                unlock = args[5];
                server = args[6];
                comments = args[7];
            } catch (Exception ex) {
                // Ignore exception
            }
            if (oid == null || "".equals(oid)) {
                throw new Exception(ComponentsUtil.i18nStringNow("emxComponents.CommonDocumentBase.ObjectIdNotEmpty",
                        context.getLocale().getLanguage()));
            }
            if (fileName == null || "".equals(fileName)) {
                throw new Exception(ComponentsUtil.i18nStringNow("emxComponents.CommonDocumentBase.FileNameNotEmpty",
                        context.getLocale().getLanguage()));
            }
            if (format == null || "".equals(format)) {
                throw new Exception(ComponentsUtil.i18nStringNow("emxComponents.CommonDocumentBase.FileFormateNotEmpty",
                        context.getLocale().getLanguage()));
            }
            if (store == null || "".equals(store)) {
                throw new Exception(ComponentsUtil.i18nStringNow(
                        "emxComponents.CommonDocumentBase.FileStoreNameNotEmpty", context.getLocale().getLanguage()));
            }

            if (unlock != null && "true".equalsIgnoreCase(unlock)) {
                unlock = "unlock";
            } else {
                unlock = "";
            }
            if (server == null || "".equals(server) || "null".equals(server)) {
                server = "server";
            }
            server = server.toLowerCase();
            if (!"server".equals(server) && !"client".equals(server)) {
                server = "server";
            }
            if (oldFileName == null || "".equals(oldFileName) || "null".equals(oldFileName)) {
                oldFileName = fileName;
            }
            Map attrMap = new HashMap();
            // ContextUtil.startTransaction(context, true);
            CommonDocument object = new CommonDocument(oid);
            StringList selectList = new StringList();
            selectList.add(CommonDocument.SELECT_FILE_NAMES_OF_ACTIVE_VERSION);
            selectList.add(CommonDocument.SELECT_ACTIVE_FILE_LOCKER);
            selectList.add(CommonDocument.SELECT_MOVE_FILES_TO_VERSION);
            Map selectMap = object.getInfo(context, selectList);
            StringList fileList = (StringList) selectMap.get(CommonDocument.SELECT_FILE_NAMES_OF_ACTIVE_VERSION);
            StringList fileLockerList = (StringList) selectMap.get(CommonDocument.SELECT_ACTIVE_FILE_LOCKER);
            boolean moveFilesToVersion = Boolean
                    .valueOf((String) selectMap.get(CommonDocument.SELECT_MOVE_FILES_TO_VERSION)).booleanValue();
            if (comments != null && !"".equals(comments) && !"null".equals(comments)) {
                attrMap.put(CommonDocument.ATTRIBUTE_CHECKIN_REASON, comments);
            }

            String objectId = oid;
            if (objectId == null) {
                String errorMessage = i18nNow.getI18nString("emxComponents.CommonDocument.DocumentsAreNotLockedByUser",
                        "emxComponentsStringResource", context.getSession().getLanguage());
                throw new Exception(errorMessage + " \n" + oldFileName);
            }

            if (!moveFilesToVersion) {
                objectId = oid;
            }
            BusinessObject bo = new BusinessObject(objectId);
            bo.open(context);

            bo.checkinFile(context, Boolean.parseBoolean(unlock), true, "", format, fileName, filePath);
            bo.close(context);
            return 0;
        } catch (Exception ex) {
            ex.printStackTrace();
            ContextUtil.abortTransaction(context);
            throw new Exception(ex.getMessage() + " oid=" + oid);

        }
    }
    /*
     * @description:获取该项目下的项目角色人员账号
     * @author: caipan
     * @date: 2025/4/3 10:40:24
     * @param: * @param[1] context
     * @param[2] args
     * @return:
     **/
    public static String getProjectRoleName(Context context,String[] args) throws Exception{
        String projectId = args[0];
        String projectRole = args[1];
        String accout = "";
        try {
            ContextUtil.pushContext(context);
            StringList relList = basicRellistSel();
            relList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_ProjectRole);
            DomainObject projectObj = DomainObject.newInstance(context, projectId);
            String relWhere = JF_PLMConstants_mxJPO.SELECT_ATTR_ProjectRole + "=='"+projectRole+"'";
            MapList objectList = projectObj.getRelatedObjects(context,
                    DomainConstants.RELATIONSHIP_MEMBER, //pattern to match relationships
                    DomainConstants.TYPE_PERSON, //pattern to match types
                    basicBolistSel(), //the eMatrix StringList object that holds the list of select statement pertaining to Business Obejcts.
                    relList, //the eMatrix StringList object that holds the list of select statement pertaining to Relationships.
                    false, //get To relationships
                    true, //get From relationships
                    (short) 1, //the number of levels to expand, 0 equals expand all.
                    DomainConstants.EMPTY_STRING, //where clause to apply to objects, can be empty ""
                    relWhere, //where clause to apply to relationship, can be empty ""
                    (short) 1 //limit
            );
//            log.info("objectList:{}",objectList);
            if (objectList.size() == 0) {
                //拿项目的owner
                accout = projectObj.getOwner(context).getName();
            } else {
                Map map = (Map) objectList.get(0);
                accout = UIUtil.getValue(map, DomainConstants.SELECT_NAME);
            }
        }finally {
            ContextUtil.popContext(context);
        }
        log.info("账号:{}",accout);
        return accout;
    }
    /*
     * @description:获取子零件的整椅节点
     * @author: caipan
     * @date: 2025/4/10 14:59:43
     * @param: * @param[1] context
     * @param[2] args  零件ID
     * @return: 整椅的集合
     **/
    public StringList getWholeChair(Context context,String[] args) throws Exception{

        String id = args[0];
        StringList selList = JF_Util_mxJPO.basicBolistSel();
        selList.add("to[JFProject2RootPart].attribute[JFZeroPart]");
        StringList relList = JF_Util_mxJPO.basicRellistSel();
        StringList JFZeroPartList = new StringList();
        ContextUtil.pushContext(context);
        getParentNode(context, id, selList, relList, JFZeroPartList);
        log.info("JFZeroPartList:{}",JFZeroPartList);
        ContextUtil.popContext(context);
        return JFZeroPartList;
    }
    public void getParentNode(Context context,String id,StringList selList,StringList relList,StringList JFZeroPartList) throws Exception{
        DomainObject obj = DomainObject.newInstance(context, id);
        MapList objectList = obj.getRelatedObjects(context,
                JF_PLMConstants_mxJPO.REL_Instance, //pattern to match relationships
                JF_PLMConstants_mxJPO.TYPE_VPMReference, //pattern to match types
                selList, //the eMatrix StringList object that holds the list of select statement pertaining to Business Obejcts.
                relList, //the eMatrix StringList object that holds the list of select statement pertaining to Relationships.
                true, //get To relationships
                false, //get From relationships
                (short) 1, //the number of levels to expand, 0 equals expand all.
                DomainConstants.EMPTY_STRING, //where clause to apply to objects, can be empty ""
                null, //where clause to apply to relationship, can be empty ""
                (short) 0 //limit
        );
        Map temp = null;
        for(int i=0;i<objectList.size();i++){
            temp  = (Map)objectList.get(i);
//            String JFZeroPart = UIUtil.getValue(temp, "to[JFProject2RootPart].attribute[JFZeroPart]");
            String JFZeroPart = ZeroString(temp);
            log.info("JFZeroPart:{}",JFZeroPart);
            String parentId = UIUtil.getValue(temp, DomainConstants.SELECT_ID);
//            if("Y".equalsIgnoreCase(JFZeroPart)){
            if(JFZeroPart!=null&&JFZeroPart.contains("Y")){
            if (!JFZeroPartList.contains(parentId)){
                JFZeroPartList.add(parentId);
            }
            }else{
                getParentNode(context, parentId,selList,relList,JFZeroPartList);
            }
        }

    }
    /*
     * @description:零件的影响项目
     * @author: caipan
     * @date: 2025/4/21 16:21:37
     * @param: * @param[1] context
     * @param[2] args 零件ID   项目ID(去除掉的项目ID)
     * @return:
     **/
    public Map getAffectProjectAndChairManager(Context context,String[] args) throws Exception{
       StringList wholeList =  getWholeChair(context, args);
       String excludeId = "";
       if(args.length==2){
           excludeId = args[1];
       }
       StringList selList = JF_Util_mxJPO.basicBolistSel();
       selList.add("from[Member|attribute[Project Role]=='Chair manager'].to.id");
       StringList relList = JF_Util_mxJPO.basicRellistSel();
       DomainObject partObj = DomainObject.newInstance(context);
       Map projectMap = new HashMap();//存储ProjectId  PersonID
       for(int i=0;i<wholeList.size();i++){
        //查找关联的项目
           partObj.setId(wholeList.get(i));
         MapList list =   partObj.getRelatedObjects(context,
                   JF_PLMConstants_mxJPO.rel_JFProject2RootPart, //pattern to match relationships
                   DomainConstants.TYPE_PROJECT_SPACE, //pattern to match types
                   selList, //the eMatrix StringList object that holds the list of select statement pertaining to Business Obejcts.
                   relList, //the eMatrix StringList object that holds the list of select statement pertaining to Relationships.
                   true, //get To relationships
                   false, //get From relationships
                   (short) 1, //the number of levels to expand, 0 equals expand all.
                   DomainConstants.EMPTY_STRING, //where clause to apply to objects, can be empty ""
                   null, //where clause to apply to relationship, can be empty ""
                   (short) 1); //limit
           if(list.size()>0){
            Map temp = (Map)list.get(0);
            String projectId = UIUtil.getValue(temp, DomainConstants.SELECT_ID);
            String wholeManagerId = UIUtil.getValue(temp, "from[Member].to.id");
            if(!projectMap.containsKey(projectId)&&!projectId.equalsIgnoreCase(excludeId)) {
                projectMap.put(projectId, wholeManagerId);
            }
           }
       }
    return projectMap;
    }
    public Map getAffectProject(Context context,String[] args) throws Exception{
        String objectId = args[0];
        DomainObject Obj = DomainObject.newInstance(context,objectId);
        String revision = Obj.getInfo(context, DomainConstants.SELECT_REVISION);
        StringList wholeList =  new StringList();
        wholeList.addAll(getWholeChair(context, args));
        if(!revision.startsWith("AA")) {//当前版本不为首版发布，就肯定没有上一个发布版本
            String preReleased = getPreviousReleasedMajorId(context, objectId);//上一个发布版本
            if (UIUtil.isNotNullAndNotEmpty(preReleased)) {
                args[0] = preReleased;
                wholeList.addAll(getWholeChair(context, args));//上一个发布版本关联的整椅
            }
        }
        //对于将在此次ECR发布的数据（原处在冻结状态），会查询当前的借用情况。若在借用项目中，借用数据所属的整椅版本是最新已发布版本或是最新冻结版本，则会通知借用项目的整椅经理，需要他们在流程中审批。反之，不需要借用项目整椅经理审批。
        //对于将在此次ECR发布的数据（原处在冻结状态），会查询上一“已发布”版本数据的借用情况。若在借用项目中，借用数据所属的整椅的版本是最新已发布版本或最新冻结版本，则会通知借用项目的整椅经理，需要他们在流程中审批。反之，不需要借用项目整椅经理审批。
        DomainObject wholeObj = DomainObject.newInstance(context);
        StringList filterwholeList = new StringList();
        for(int i=0;i<wholeList.size();i++){
            String wholeId = wholeList.get(i);
            wholeObj.setId(wholeList.get(i));
            String islastVersion = wholeObj.getAttributeValue(context, JF_PLMConstants_mxJPO.ATTR_V_IsLastVersion);
            String current = wholeObj.getInfo(context, "current");
            String isReleasedId =  getLastReleasedMajorid(context,wholeId);
            log.info("islastVersion:{} isReleasedId：{}",islastVersion,isReleasedId);
            //在判断是不是最新发布版本 或者最新发布版本
            if(("TRUE".equalsIgnoreCase(islastVersion)&&("FROZEN".equalsIgnoreCase(current)||"RELEASED".equalsIgnoreCase(current))||wholeId.equalsIgnoreCase(isReleasedId))){
                if(!filterwholeList.contains(wholeId)){
                    filterwholeList.add(wholeId);
                }
            }
        }
        StringList selList = JF_Util_mxJPO.basicBolistSel();
//        selList.add("to[" + JF_PLMConstants_mxJPO.rel_JFProject2RootPart + "].from.id");
        //需要再次过滤整椅清单是否是属于该项目的整椅清单
       MapList projectList =  DomainObject.getInfo(context, filterwholeList.toStringArray(), selList);
       log.info("wholeList:{} filterwholeList:{} projectList:{} objectId {}",wholeList,filterwholeList,projectList,objectId);
       Map<String,MapList> map  = new HashMap<>();
        if(projectList.size()>0) {
            map.put(objectId, projectList);
        }
        return map;
    }
    /*
     * @description:构造链接形式的html
     * @author: caipan
     * @date: 2025/4/28 10:23:43
     * @param: * @param[1] context
     * @param[2] id 对象ID
     * @param[3] title 显示的Title
     * @return:
     **/
    public static String buildHtml(Context context,String id,String title){
        title = StringEscapeUtils.escapeHtml4(title);
        StringBuffer str = new StringBuffer();
        str.append("<a href=\"JavaScript:emxTableColumnLinkClick('../common/emxTree.jsp?mode=popup&amp;objectId=");
        str.append(id);
        str.append("', '700', '600', 'false', 'popup', '')\" >");
        str.append(title);
        str.append("</a>");
        log.info("html:{}",str);
      return str.toString();
    }

/*
 * @description:获取衍生物的最新文件 文件可能是文件列表
 * @author: caipan
 * @date: 2025/6/6 14:26:14
 * @param: * @param[1] context
 * @param[2] args args[0] 图纸的物理ID
 * @return:
 **/
public StringList getDerivedOutputFileListName(Context context,String[] args) throws Exception{
    ENODerivedOutputStreamInfoService var13 = new ENODerivedOutputStreamInfoService(context);
    StringList idlist = new StringList();
    idlist.add(args[0]);//图纸的物理ID
    StringList attrList = new StringList();
    attrList.add("filename");
    attrList.add("format");
    attrList.add("isSync");
    attrList.add("downloadable");
    attrList.add("visible");
    attrList.add("parameters");
    attrList.add("isExternal");
    attrList.add("deletable");
    attrList.add("title");
    Map var9 = var13.getDerivedOutputInformation(idlist, attrList, true);
    log.info("var9:{}",var9);
    return getFileName(context,args[0],var9);
}
    public StringList getFileName(Context context,String drwId,Map map) throws Exception{
        DomainObject drw = DomainObject.newInstance(context,drwId);
        StringList listFileName = drw.getInfoList( context, "from[DerivedOutputRelationship].to.format.file.name");//文件列表
        StringList listFilechecksum = drw.getInfoList( context, "from[DerivedOutputRelationship].to.format.file.checksum");//文件checksum
        //解析Map
        ArrayList infoList = (ArrayList)map.get(drwId);
        MapList list = new MapList();
        list.addAll(infoList);
        log.info("list:{}",list);
        StringList resultListName = new StringList();
        for(int i=0;i<list.size();i++){
            com.dassault_systemes.dostreaminformation.ENODOStream temp = (ENODOStream)list.get(i);
            boolean synchro = temp.getSynchroStatus();
            if (synchro){
                String name = temp.getName();
                String cheksum = "{MD5}"+ temp.getChecksum();
//                log.info("name:{} cheksum:{}",name,cheksum);
                if(listFileName.contains(name)){
                    if(!resultListName.contains(name)) {
                        resultListName.add(name);
                    }
                }else{
                    int index = listFilechecksum.indexOf(cheksum);
                    if(index>-1){
                        resultListName.add(listFileName.get(index));
                    }
                }
            }
        }
        log.info("resultListName:{}",resultListName);
        return resultListName;

    }
    /*
     * @description:增加历史记录
     * @author: caipan
     * @date: 2025/6/27 16:28:40
     * @param: * @param[1] context
     * @param[2] args
     *  args[0] 类型是对象和关系 bus connection
     * args[1] id
     * args[2] 修改的账号
     * @return:
     **/
    public String addHistory(Context context,String[] args){
        try {
            if (args.length == 3) {
                String mql = "mod " + args[0] + " " + args[1] + " add history modify comment '" + args[2] + " is modified'";
                String result = MqlUtil.mqlCommand(context, false, mql, true);
                return result;
            }
        }catch (Exception e){
            log.error(e.getMessage());
        }
        return "";
    }
    /*
     * @description:把mapList 转换成 StringList
     * @author: caipan
     * @date: 2025/7/18 14:08:01
     * @param: * @param[1] context
     * @param[2] list
     * @param[3] key map里面的key
     * @return:
     **/
    public static StringList mapList2StringList(MapList list,String key){
        StringList selList = new StringList();
        for(int i=0;i<list.size();i++){
            Map map = (Map)list.get(i);
            String value= UIUtil.getValue(map, key);
            if(!selList.contains(value)) {
                selList.add(value);
            }
        }
        return selList;
    }

    /*
     * @description: 供货件标识，多个的拼接起来
     * @author: caipan
     * @date: 2025/7/21 14:49:38
     * @param: * @param[1] map
     * @return:
     **/
    public static String ZeroString(Map map){
        Object zero = (Object)map.get("to[JFProject2RootPart].attribute[JFZeroPart]") ;
        String zeroStr = "";
        if(zero instanceof StringList){
            StringList zeroList = (StringList)map.get("to[JFProject2RootPart].attribute[JFZeroPart]") ;
            zeroStr =  String.join(",",zeroList);
        }else{
             zeroStr = (String)map.get("to[JFProject2RootPart].attribute[JFZeroPart]") ;
        }
        return zeroStr;
    }

    /**
    *
    *@description 校验指定项目的项目角色是否维护
    *@param context
	*@param args
    *@return java.lang.Boolean
    *@throws
    *@author CHENYAN
    *@date 2025/8/14 15:03
    */
    public static  Map checkProjectRoleIsMaintenanceByProjectIDAndRoleName(Context context ,String[] args) throws Exception{
        Map res = new HashMap() ;
        Boolean isRes = Boolean.TRUE ;
        Map argsMap = JPO.unpackArgs(args);
        String strProjectId = (String)argsMap.get("projectId");
        StringList roleNameList = (StringList) argsMap.get("projectRole");
        StringList notRoleNameList = new StringList();
        Boolean isPush = (Boolean) argsMap.get("isPush");

        try {
            if (!isPush){
                ContextUtil.pushContext(context);
            }
            DomainObject project = DomainObject.newInstance(context, strProjectId);
            StringList relSelectList = JF_Util_mxJPO.basicRellistSel();
            relSelectList.add("attribute[Project Role]");
            //拿取项目的成员以及关系属性project role
            MapList mapList = project.getRelatedObjects(
                    context,
                    DomainRelationship.RELATIONSHIP_MEMBER,
                    DomainConstants.TYPE_PERSON,
                    JF_Util_mxJPO.basicBolistSel(),
                    relSelectList,
                    false,
                    true,
                    (short) 1, // recursion level
                    "", //object where clause
                    "", //relationship where clause
                    0
            );
            Set roleSet = (Set) mapList.stream().map(m ->{
                Map personInfoMap = (Map)m;
                return personInfoMap.get("attribute[Project Role]");
            }).collect(Collectors.toSet());
            for (int i = 0; i < roleNameList.size(); i++) {
                String strRoleName = roleNameList.get(i);
                if (!roleSet.contains(strRoleName)) {
                    isRes = Boolean.FALSE;
                    notRoleNameList.add(strRoleName);
                    break;
                }
            }
        }finally {
            if (!isPush){
                ContextUtil.popContext(context);
            }
        }
        res.put("isPass",isRes);
        res.put("notInitProjectRole",notRoleNameList);
        return res;
    }
   /*
    * @description:升版数模的大版本
    * @author: caipan
    * @date: 2025/12/5 15:36:53
    * @param: * @param[1] context
    * @param[2] phyId
    * @return:
    **/    public static String majorRevision(Context context, String phyId) throws MatrixException {
       String majorRevisionParams = "{\"data\":[{\"physicalid\":\"@physicalid\",\"modifiedAttributes\":{}}]}";
       String mrParams = new String(majorRevisionParams);
       mrParams = mrParams.replaceAll("@physicalid", phyId);
       log.info("mrParams : " + mrParams);
       com.matrixone.json.JSONObject majorRevisionJson = new com.matrixone.json.JSONObject(mrParams);
       LifecycleServices_NewMajorRevision_NLR lifecycleNewRevisionFromNlr = new LifecycleServices_NewMajorRevision_NLR();
       com.matrixone.json.JSONObject majorRevisionResult = lifecycleNewRevisionFromNlr.NewMajorRevision(context, majorRevisionJson);
       log.info("majorRevisionResult : " + majorRevisionResult);
       /*
        * json:  拿取results
        *{"report":[],
        * "relResults":[{"sourcePid":"73A18B56D8CC0800693FB4A100001F06","physicalid":"73A18B56D8CC0800693FCB2D00003148"},{"sourcePid":"73A18B56D8CC0800693FB4A100001EFC","physicalid":"73A18B56D8CC0800693FCB2D00003142"},{"sourcePid":"73A18B56D8CC0800693FB4A100001F10","physicalid":"73A18B56D8CC0800693FCB2D0000314E"},{"sourcePid":"73A18B56D8CC0800693FB4A100001EF2","physicalid":"73A18B56D8CC0800693FCB2D0000313C"}],
        * "results":[{"versionid":"35827DD8BA9F4AFCAD42897E965F0A29","majorid":"73A18B56D8CC0800693FCB2D00003136","logicalid":"35827DD8BA9F4AFCAD42897E965F0A29","physicalid":"73A18B56D8CC0800693FCB2D00003136","attribute[PLMReference.V_DerivedFrom]":"35827DD8BA9F4AFCAD42897E965F0A29","derivedfromphysicalid":"35827DD8BA9F4AFCAD42897E965F0A29","revision":"--A.000"},{"versionid":"916634B4F5434A459FCF04FD0BC29EEC","majorid":"73A18B56D8CC0800693FCB2C00003128","logicalid":"916634B4F5434A459FCF04FD0BC29EEC","physicalid":"73A18B56D8CC0800693FCB2C00003128","attribute[PLMReference.V_DerivedFrom]":"916634B4F5434A459FCF04FD0BC29EEC","derivedfromphysicalid":"916634B4F5434A459FCF04FD0BC29EEC","revision":"AB.1"}],"status":"success"}
        * */
       return majorRevisionResult.toString();
   }

    /*
     * @description:测试升版
     * @author: caipan
     * @date: 2025/12/5 15:43:10
     * @param: * @param[1] context
     * @param[2] args
     * @return:
     **/
    public void testMajor(Context context,String[] args) throws Exception{
        ContextUtil.startTransaction(context,true);
        DomainObject domainObject = DomainObject.newInstance(context,args[0]);
        String majoredRevision = JF_Util_mxJPO.majorRevision(context, args[0]);
        log.info("context:{}", context.getUser().toString());
        log.info("majoredRevision:{}", majoredRevision);
        JSONObject jsonObject = JSONObject.parseObject(majoredRevision);
        JSONArray results = jsonObject.getJSONArray("results");
        log.info("results:{}", results);
        String logicalid = domainObject.getInfo(context, "logicalid");
        String bubbleId = "";
        for (int i = 0; i < results.size(); i++) {
            JSONObject jsonObject1 = results.getJSONObject(i);
            if (logicalid.equalsIgnoreCase(jsonObject1.getString("logicalid"))) {
                bubbleId = jsonObject1.getString("physicalid");
                break;
            }
        }
        domainObject.setId(bubbleId);
        log.info("bubbleId:{}",bubbleId);
        domainObject.setAttributeValue(context, "JF_VPMReferenceCost.JF_IsBubbling", "Y");
//        DomainRelationship.setToObject(context,args[0],new DomainObject(args[1]));
//        ContextUtil.abortTransaction(context);
        ContextUtil.commitTransaction(context);
    }


    /*
     * @description:获取最大版本返回Map 里面的id
     * @author: caipan
     * @date: 2025/12/31 13:50:23
     * @param: * @param[1] mapList
     * @return:
     **/
    public static String getMaxRevisionIdStream(List<Map<String, Object>> mapList) {
        if (mapList == null || mapList.isEmpty()) {
            return null;
        }
        // 使用Stream API和Comparator链式调用[1](@ref)[6](@ref)
        Optional<Map<String, Object>> maxMap = mapList.stream()
                .max(Comparator.comparing((Map<String, Object> m) -> {
                    String revision = (String) m.get("revision");
                    String[] parts = revision.split("\\.");
                    return parts[0]; // 第一级：字母前缀
                }).thenComparing(m -> {
                    String revision = (String) m.get("revision");
                    String[] parts = revision.split("\\.");
                    return Integer.parseInt(parts[1]); // 第二级：中间数字
                }).thenComparing(m -> {
                    String revision = (String) m.get("revision");
                    String[] parts = revision.split("\\.");
                    return Integer.parseInt(parts[2]); // 第三级：流水码
                }));

        return maxMap.map(m -> (String) m.get("id")).orElse(null);
    }

    /*
     * @description:获取最新版本的算法
     * @author: caipan
     * @date: 2025/12/31 14:55:07
     * @param: * @param[1] context
     * @param[2] mapList
     * @return:
     **/
    public static String getMaxRevisionIdStream2(Context context,BusinessObjectList mapList){
        if (mapList == null || mapList.isEmpty()) {
            return null;
        }
        // 使用Stream API和Comparator链式调用[1](@ref)[6](@ref)
        Optional<BusinessObject> maxMap = mapList.stream()
                .max(Comparator.comparing(m -> {
                    DomainObject domain = (DomainObject)m;
                    String revision = null;
                    try {
                        revision = domain.getInfo(context, DomainConstants.SELECT_REVISION);
                    } catch (FrameworkException e) {
                        throw new RuntimeException(e);
                    }
                    if(!isValidRevision(revision)){
                        return "AA";
                    }
                    String[] parts = revision.split("\\.");
                    return parts[0]; // 第一级：字母前缀
                }).thenComparing(m -> {
                    DomainObject domain = (DomainObject)m;
                    String revision = null;
                    try {
                        revision = domain.getInfo(context, DomainConstants.SELECT_REVISION);
                    } catch (FrameworkException e) {
                        throw new RuntimeException(e);
                    }
                   if(!isValidRevision(revision)){
                       return 1;
                   }
                    String[] parts = revision.split("\\.");
                    return Integer.parseInt(parts[1].split("-")[0]); // 第二级：中间数字
                }).thenComparing(m -> {
                    DomainObject domain = (DomainObject)m;
                    String revision = null;
                    try {
                        revision = domain.getInfo(context, DomainConstants.SELECT_REVISION);
                    } catch (FrameworkException e) {
                        throw new RuntimeException(e);
                    }
                    if(!isValidRevision(revision)){
                        return 000;
                    }
                    String[] parts = revision.split("\\.");
                    return Integer.parseInt(parts[1].split("-")[1]); // 第二级：中间数字
                }));

        return maxMap.map(m -> {
            try {
                return ((DomainObject)m).getInfo(context,DomainConstants.SELECT_ID);
            } catch (FrameworkException e) {
                throw new RuntimeException(e);
            }
        }).orElse(null);
    }


    /**
    *      * 从 StringList 中获取最大的版本号
     *      * 规则：AA.1-000 (前缀.主版本-子版本)
     *      * 比较优先级：1.前缀(字典序) > 2.主版本(数值) > 3.子版本(数值)
    * @param revisions  版本号字符串数组
    * @author LIUJR
    * @throws
    * @return java.lang.String   最大的版本号字符串，若数组为空或无有效数据则返回 null
    * @date 2026/3/5 15:21
    * @description
    */
    public static String getMaxRevision(Context context, String[] revisions) {
        // 1. 基础校验：数组本身是否为 null 或长度为 0
        if (revisions == null || revisions.length == 0) {
            return null;
        }
        // 2. 创建 Stream 并进行处理
        Optional<String> maxRev = Arrays.stream(revisions)
                .filter(rev -> rev != null && !rev.trim().isEmpty()) // 过滤掉数组中的 null 或空字符串元素
                .max(Comparator.comparing(rev -> {
                    // --- 第一级：比较前缀 (例如 "AA") ---
                    int dotIdx = ((String)rev).indexOf('.');
                    // 如果没有点，视为空前缀（排在最后）
                    return (dotIdx > 0) ? ((String)rev).substring(0, dotIdx) : "";
                }).thenComparingInt(rev -> {
                    // --- 第二级：比较主版本数字 (例如 "1" vs "10") ---
                    try {
                        int dotIdx = ((String)rev).indexOf('.');
                        int dashIdx = ((String)rev).indexOf('-');
                        // 确保格式包含 "." 和 "-" 且顺序正确
                        if (dotIdx > 0 && dashIdx > dotIdx) {
                            String numStr = ((String)rev).substring(dotIdx + 1, dashIdx);
                            return Integer.parseInt(numStr);
                        }
                    } catch (NumberFormatException e) {
                        // 解析失败（非数字），返回 -1 使其排在合法版本之后
                    }
                    return -1;
                }).thenComparingInt(rev -> {
                    // --- 第三级：比较子版本数字 (例如 "000" vs "001") ---
                    try {
                        int dashIdx = ((String)rev).indexOf('-');
                        // 确保 "-" 后面有内容
                        if (dashIdx > 0 && dashIdx < ((String)rev).length() - 1) {
                            String numStr = ((String)rev).substring(dashIdx + 1);
                            return Integer.parseInt(numStr);
                        }
                    } catch (NumberFormatException e) {
                        // 解析失败，返回 -1
                    }
                    return -1;
                }));
        // 3. 返回结果
        log.info("maxRev:{}", maxRev.orElse(null));
        return maxRev.orElse(null);
    }


    /*
     * @description:获取最新版本 基于最新的版本规则 AA.1-000 来获取的
     * @author: caipan
     * @date: 2025/12/31 13:57:03
     * @param: * @param[1] context
     * @param[2] vpmId
     * @return:
     **/
    public static String getLatestMajorid_new(Context context, String[] vpmId) throws Exception {
        String lastReleasedId = "";
        try {
            BusinessObjectList majorRevisionsBusObjList = DomainObject.newInstance(context, vpmId[0]).getMajorRevisions(context);
                lastReleasedId = getMaxRevisionIdStream2(context,majorRevisionsBusObjList);
              /*  for (int i = majorRevisionsBusObjList.size() - 1; i >= 0; i--) {
                    doObj = (DomainObject) majorRevisionsBusObjList.get(i);
                    sCurrent = doObj.getInfo(context, DomainObject.SELECT_CURRENT);

                }*/
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
        log.info("lastReleasedId:{}",lastReleasedId);
        return lastReleasedId;
    }
    public static boolean isValidRevision(String revision) {
        // 正则表达式：^[A-Z]{2}\.\d+-\d+$
        // ^ : 字符串开始
        // [A-Z]{2} : 两个大写字母
        // \. : 点号（需要转义）
        // \d+ : 一个或多个数字（中间数字）
        // - : 连字符
        // \d+ : 一个或多个数字（流水码）
        // $ : 字符串结束
        // 中间数字1-3位，流水码1-4位
        String regex = "^[A-Z]{2}\\.\\d{1,3}-\\d{1,4}$";
        return revision != null && revision.matches(regex);
    }
    /*
     * @description:得到最新发布版本 基于最新版本规则 AA.1.000
     * @author: caipan
     * @date: 2025/12/31 15:07:44
     * @param: * @param[1] context
     * @param[2] vpmId
     * @return:
     **/
    public static String getLastReleasedMajorid_new(Context context, String vpmId) throws Exception {
        String lastReleasedId = "";
        try {
            BusinessObjectList majorRevisionsBusObjList = DomainObject.newInstance(context, vpmId).getMajorRevisions(context);
            DomainObject doObj;
            String sCurrent;
            log.info("size:{}",majorRevisionsBusObjList.size());
                for (int i = majorRevisionsBusObjList.size() - 1; i >= 0; i--) {
                    doObj = (DomainObject) majorRevisionsBusObjList.get(i);
                    sCurrent = doObj.getInfo(context, DomainObject.SELECT_CURRENT);
                    if (!"RELEASED".equals(sCurrent)) {
                        majorRevisionsBusObjList.remove(i);
                    }
                }
            log.info("size after:{}",majorRevisionsBusObjList.size());
            lastReleasedId = getMaxRevisionIdStream2(context,majorRevisionsBusObjList);
            log.info("lastReleasedId:{}",lastReleasedId);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
        return lastReleasedId;
    }
    /*
     * @description:得到最新发布版本 基于最新版本规则 AA.1.000
     * @author: caipan
     * @date: 2025/12/31 15:19:41
     * @param: * @param[1] context
     * @param[2] vpmId
     * @return:
     **/
    public static String getLastReleasedMajorid_new(Context context, String[] vpmId) throws Exception {
        return getLastReleasedMajorid_new(context,vpmId[0]);
    }

    /*
     * @description:获取次最新发布版本---排除传递的excludeId objectId---按理这个是最新发布版 冒泡肯定是自己已经发布了
     * @author: caipan
     * @date: 2025/12/31 15:20:51
     * @param: * @param[1] context
     * @param[2] vpmId
     * @return:
     **/
    public static String getLastReleasedMajoridExcludeOwner(Context context,String excludeId) throws Exception {
        String lastReleasedId = "";
        try {
            BusinessObjectList majorRevisionsBusObjList = DomainObject.newInstance(context, excludeId).getMajorRevisions(context);
            DomainObject doObj;
            String sCurrent;
            String id;
            log.info("size:{}",majorRevisionsBusObjList.size());
            BusinessObjectList releaseList = new BusinessObjectList();
            for (int i = majorRevisionsBusObjList.size() - 1; i >= 0; i--) {
                doObj = (DomainObject) majorRevisionsBusObjList.get(i);
                sCurrent = doObj.getInfo(context, DomainObject.SELECT_CURRENT);
                id = doObj.getInfo(context, DomainObject.SELECT_ID);
               /* if (!"RELEASED".equals(sCurrent)) {
                    majorRevisionsBusObjList.remove(i);
                }
                if(excludeId.equalsIgnoreCase(id)){
                    majorRevisionsBusObjList.remove(i);
                }*/
                if("RELEASED".equals(sCurrent)&&!excludeId.equalsIgnoreCase(id)) {
                    releaseList.add(doObj);
                }
            }
            log.info("size after:{}",releaseList.size());
            if(releaseList.size()>0) {
                lastReleasedId = getMaxRevisionIdStream2(context, releaseList);
            }
            log.info("lastReleasedId:{}",lastReleasedId);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
        return lastReleasedId;
    }

    /**
    * 获取次最新发布版本---排除传递的excludeId objectId---按理这个是最新发布版 冒泡肯定是自己已经发布了 排除冒泡 版本
    * @param context
	* @param excludeId
    * @author LIUJR
    * @throws
    * @return java.lang.String
    * @date 2026/6/4 13:50
    * @description
    */
    public static String getLastReleasedMajoridExcludeOwnerAndBubblingRev(Context context,String excludeId) throws Exception {
        String lastReleasedId = "";
        try {
            BusinessObjectList majorRevisionsBusObjList = DomainObject.newInstance(context, excludeId).getMajorRevisions(context);
            DomainObject doObj;
            String sCurrent;
            String sRevision;
            String id;
            log.info("size:{}",majorRevisionsBusObjList.size());
            BusinessObjectList releaseList = new BusinessObjectList();
            for (int i = majorRevisionsBusObjList.size() - 1; i >= 0; i--) {
                doObj = (DomainObject) majorRevisionsBusObjList.get(i);
                sCurrent = doObj.getInfo(context, DomainObject.SELECT_CURRENT);
                sRevision = doObj.getInfo(context, DomainObject.SELECT_REVISION);
                id = doObj.getInfo(context, DomainObject.SELECT_ID);
               /* if (!"RELEASED".equals(sCurrent)) {
                    majorRevisionsBusObjList.remove(i);
                }
                if(excludeId.equalsIgnoreCase(id)){
                    majorRevisionsBusObjList.remove(i);
                }*/
                //排除冒泡
                if(sRevision.endsWith("-000") && "RELEASED".equals(sCurrent)&&!excludeId.equalsIgnoreCase(id)) {
                    releaseList.add(doObj);
                }
            }
            log.info("size after:{}",releaseList.size());
            if(releaseList.size()>0) {
                lastReleasedId = getMaxRevisionIdStream2(context, releaseList);
            }
            log.info("lastReleasedId:{}",lastReleasedId);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
        return lastReleasedId;
    }
    /*
     * @description:获取次最新发布版本---排除传递的excludeId objectId---按理这个是最新发布版 冒泡肯定是自己已经发布了
     * @author: caipan
     * @date: 2025/12/31 15:25:59
     * @param: * @param[1] context
     * @param[2] excludeId
     * @return:
     **/
    public static String getLastReleasedMajoridExcludeOwner(Context context,String[] excludeId)throws Exception{
        return getLastReleasedMajoridExcludeOwner(context,excludeId[0]);
    }
    /*
     * @description:排除掉传过来的PartId ,获取当前零件下的所有版本，并且按照从小到大的顺序排序
     * @author: caipan
     * @date: 2026/1/4 14:35:52
     * @param: * @param[1] objectId
     * @return:
     **/
    public static  BusinessObjectList sortMapListInRevisionExcludeTransmitPartId(Context context,String[] args) throws Exception{
        String objectId = args[0];
        BusinessObjectList majorRevisionsBusObjList = DomainObject.newInstance(context, objectId).getMajorRevisions(context);
        for(int i=0;i<majorRevisionsBusObjList.size();i++){
            DomainObject domain = (DomainObject)majorRevisionsBusObjList.get(i);
            String id = domain.getInfo(context,DomainConstants.SELECT_ID);
            String physicalId = domain.getInfo(context,DomainConstants.SELECT_PHYSICAL_ID);
            if(id.equals(objectId)||physicalId.equals(objectId)){
                majorRevisionsBusObjList.remove(i);
            }
        }
        log.info("majorRevisionsBusObjList:{} majorRevisionsBusObjList size{}",majorRevisionsBusObjList,majorRevisionsBusObjList.size());
        majorRevisionsBusObjList.sort(Comparator.comparing(map -> {
                    DomainObject domain = (DomainObject)map;
                    String revision=null;
                    try {
                        revision = domain.getInfo(context, DomainConstants.SELECT_REVISION);
                    } catch (FrameworkException e) {
                        throw new RuntimeException(e);
                    }
                    if (isValidRevision(revision)){
                        return extractPrefix(revision);
                    }else{
                        return "AA";
                    }
                })
                .thenComparing(map -> {
                    DomainObject domain = (DomainObject)map;
                    String revision=null;
                    try {
                        revision = domain.getInfo(context, DomainConstants.SELECT_REVISION);
                    } catch (FrameworkException e) {
                        throw new RuntimeException(e);
                    }
                    if (isValidRevision(revision)) {
                        return extractMiddleNumber(revision);
                    }else{
                        return 1;
                    }
                })
                .thenComparing(map -> {
                    DomainObject domain = (DomainObject)map;
                    String revision=null;
                    try {
                        revision = domain.getInfo(context, DomainConstants.SELECT_REVISION);
                    } catch (FrameworkException e) {
                        throw new RuntimeException(e);
                    }
                    if (isValidRevision(revision)) {
                        return extractSuffixNumber(revision);
                    }else{
                        return 000;
                    }

                }));
        log.info("版本排序:{}",majorRevisionsBusObjList);
        return majorRevisionsBusObjList;
    }

    /*
     * @description:获取当前零件下的所有版本，并且按照从小到大的顺序排序
     * @author: caipan
     * @date: 2026/1/4 14:35:52
     * @param: * @param[1] objectId
     * @return:
     **/
    public static  BusinessObjectList sortMapListInRevision(Context context,String[] args) throws Exception{
        String objectId = args[0];
        BusinessObjectList majorRevisionsBusObjList = DomainObject.newInstance(context, objectId).getMajorRevisions(context);
        majorRevisionsBusObjList.sort(Comparator.comparing(map -> {
                    DomainObject domain = (DomainObject)map;
                    String revision=null;
                    try {
                        revision = domain.getInfo(context, DomainConstants.SELECT_REVISION);
                    } catch (FrameworkException e) {
                        throw new RuntimeException(e);
                    }
                    if (isValidRevision(revision)){
                        return extractPrefix(revision);
                    }else{
                        return "AA";
                    }
                })
                .thenComparing(map -> {
                    DomainObject domain = (DomainObject)map;
                    String revision=null;
                    try {
                        revision = domain.getInfo(context, DomainConstants.SELECT_REVISION);
                    } catch (FrameworkException e) {
                        throw new RuntimeException(e);
                    }
                    if (isValidRevision(revision)) {
                        return extractMiddleNumber(revision);
                    }else{
                        return 1;
                    }
                })
                .thenComparing(map -> {
                    DomainObject domain = (DomainObject)map;
                    String revision=null;
                    try {
                        revision = domain.getInfo(context, DomainConstants.SELECT_REVISION);
                    } catch (FrameworkException e) {
                        throw new RuntimeException(e);
                    }
                    if (isValidRevision(revision)) {
                        return extractSuffixNumber(revision);
                    }else{
                        return 000;
                    }

                }));
        log.info("版本排序:{}",majorRevisionsBusObjList);
    return majorRevisionsBusObjList;
    }
    /*
     * @description:对传进来的mapList排序，根据Map的revision 这个key进行排序
     * @author: caipan
     * @date: 2026/1/4 14:30:41
     * @param: * @param[1] mapList
     * @return:
     **/
    public static void sortMapListInRevision(List<Map<String, Object>> mapList) {
        if (mapList == null || mapList.isEmpty()) {
            return;
        }

        mapList.sort(Comparator.comparing((Map<String, Object> map) -> {
                    String revision = (String) map.get("revision");
                    if (isValidRevision(revision)){
                        return extractPrefix(revision);
                    }else{
                        return "AA";
                    }
                })
                .thenComparing(map -> {
                    String revision = (String) map.get("revision");
                    if (isValidRevision(revision)) {
                        return extractMiddleNumber(revision);
                    }else{
                        return 1;
                    }
                })
                .thenComparing(map -> {
                    String revision = (String) map.get("revision");
                    if (isValidRevision(revision)) {
                        return extractSuffixNumber(revision);
                    }else{
                        return 000;
                    }

                }));
    }
    // 辅助方法：提取revision的各个部分
    private static String extractPrefix(String revision) {
        String[] prefix = revision.split("\\.");
        return prefix[0];
    }

    private static int extractMiddleNumber(String revision) {
        String[] parts = revision.split("\\.");
        String[] middle  = parts[1].split("-");
        return Integer.parseInt(middle[0]);
    }

    private static int extractSuffixNumber(String revision) {
        String[] parts = revision.split("\\.");
        String[] last  = parts[1].split("-");
        return Integer.parseInt(last[1]);
    }
    /*
     * @description:删除对象包含 数模
     *      args  传递 physicalId 即可
     * @author: caipan
     * @date: 2026/1/23 15:41:22
     * @param: * @param[1] context
     * @param[2] args
     * @return:
     **/
    public static void delObject(Context context,String[] args) throws Exception{
        try {
            ContextUtil.pushContext(context);
            DeleteWebServices service = new DeleteWebServices();
            JSONObject jsonObject = new JSONObject();
            JSONArray array = new JSONArray();
            for (int i = 0; i < args.length; i++) {
                JSONObject phyObject = new JSONObject();
                phyObject.put("physicalid", args[i]);
                array.add(phyObject);
            }
            JSONArray array2 = new JSONArray();
            JSONObject checkbox = new JSONObject();
            checkbox.put("type", "checkbox");
            checkbox.put("value", false);
            array2.add(checkbox);


            jsonObject.put("data", array);
            jsonObject.put("options", array2);
            log.info("json:{}", jsonObject.toJSONString());
            String json = jsonObject.toJSONString();
            JsonReader var8 = Json.createReader(new StringReader(json));
            JsonObject var24 = var8.readObject();
            var8.close();
            JsonObject resultMessage = service.deleteObjects(context,var24);
            log.info("resultMessage:{}",resultMessage);
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            ContextUtil.popContext(context);
        }
    }

    /**
    * 获取零件的最新版本（工作中，冻结，发布）  变形件原件关系管理使用
    * @param context
	* @param vpmId
    * @author LIUJR
    * @throws
    * @return java.lang.String
    * @date 2026/1/27 9:39
    * @description
    */
    public static String getLastMajorId(Context context, String vpmId) throws Exception {
        String lastId = "";
        try {
            BusinessObjectList majorRevisionsBusObjList = DomainObject.newInstance(context, vpmId).getMajorRevisions(context);
            DomainObject doObj;
            String sCurrent;
            log.info("size:{}",majorRevisionsBusObjList.size());
            for (int i = majorRevisionsBusObjList.size() - 1; i >= 0; i--) {
                doObj = (DomainObject) majorRevisionsBusObjList.get(i);
                sCurrent = doObj.getInfo(context, DomainObject.SELECT_CURRENT);
                if (!("RELEASED".equals(sCurrent)||"IN_WORK".equals(sCurrent)||"FROZEN".equals(sCurrent))) {
                    majorRevisionsBusObjList.remove(i);
                }
            }
            log.info("size after:{}",majorRevisionsBusObjList.size());
            lastId = JF_Util_mxJPO.getMaxRevisionIdStream2(context,majorRevisionsBusObjList);
            log.info("lastReleasedId:{}",lastId);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
        return lastId;
    }
    /*
     * @description:获取零件的所有发布版本，并且按照从小到大排序
     * @author: caipan
     * @date: 2026/3/19 15:24:18
     * @param: * @param[1] context
     * @param[2] args
     * @return:
     **/
    public static  BusinessObjectList sortMapListInRevisionCurrent(Context context,String[] args) throws Exception{
        String objectId = args[0];
        BusinessObjectList majorRevisionsBusObjListFirst = DomainObject.newInstance(context, objectId).getMajorRevisions(context);
        BusinessObjectList majorRevisionsBusObjList = new BusinessObjectList();
        for (int i = 0; i < majorRevisionsBusObjListFirst.size(); i++) {
            BusinessObject obj = majorRevisionsBusObjListFirst.get(i);
            DomainObject domainObj = DomainObject.newInstance(context, obj);
            String current = domainObj.getInfo(context, SELECT_CURRENT);
            if(current.equalsIgnoreCase("RELEASED")){
                majorRevisionsBusObjList.add(obj);
            }
        }
        majorRevisionsBusObjList.sort(Comparator.comparing(map -> {
                    DomainObject domain = (DomainObject)map;
                    String revision=null;
                    try {
                        revision = domain.getInfo(context, DomainConstants.SELECT_REVISION);
                    } catch (FrameworkException e) {
                        throw new RuntimeException(e);
                    }
                    if (isValidRevision(revision)){
                        return extractPrefix(revision);
                    }else{
                        return "AA";
                    }
                })
                .thenComparing(map -> {
                    DomainObject domain = (DomainObject)map;
                    String revision=null;
                    try {
                        revision = domain.getInfo(context, DomainConstants.SELECT_REVISION);
                    } catch (FrameworkException e) {
                        throw new RuntimeException(e);
                    }
                    if (isValidRevision(revision)) {
                        return extractMiddleNumber(revision);
                    }else{
                        return 1;
                    }
                })
                .thenComparing(map -> {
                    DomainObject domain = (DomainObject)map;
                    String revision=null;
                    try {
                        revision = domain.getInfo(context, DomainConstants.SELECT_REVISION);
                    } catch (FrameworkException e) {
                        throw new RuntimeException(e);
                    }
                    if (isValidRevision(revision)) {
                        return extractSuffixNumber(revision);
                    }else{
                        return 000;
                    }

                }));
        log.info("版本排序:{}",majorRevisionsBusObjList);
        return majorRevisionsBusObjList;
    }
    /*
     * @description:按照从大到小的顺序排序，第一个发布版本需要提到第一个
     * @author: caipan
     * @date: 2026/3/20 09:44:40
     * @param: * @param[1] context
     * @param[2] args
     * @return:
     **/
    public static  BusinessObjectList sortMapListFilterRevision(Context context,String[] args) throws Exception{
        String objectId = args[0];
        String passRevision = args[1];
        BusinessObjectList majorRevisionsBusObjList = DomainObject.newInstance(context, objectId).getMajorRevisions(context);
        majorRevisionsBusObjList.sort(
                // 新增：第一步优先匹配passRevision，匹配的排最前面
                Comparator.comparing(map -> {
                            DomainObject domain = (DomainObject)map;
                            String revision=null;
                            try {
                                revision = domain.getInfo(context, DomainConstants.SELECT_REVISION);
                            } catch (FrameworkException e) {
                                throw new RuntimeException(e);
                            }
                            // 匹配passrevision返回false（排序值更小），不匹配返回true
                            return !passRevision.equals(revision);
                        })
                        // 原有排序逻辑：前缀
                        .thenComparing(map -> {
                    DomainObject domain = (DomainObject)map;
                    String revision=null;
                    try {
                        revision = domain.getInfo(context, DomainConstants.SELECT_REVISION);
                    } catch (FrameworkException e) {
                        throw new RuntimeException(e);
                    }
                    if (isValidRevision(revision)){
                        return extractPrefix(revision);
                    }else{
                        return "AA";
                    }
                })
                .thenComparing(map -> {
                    DomainObject domain = (DomainObject)map;
                    String revision=null;
                    try {
                        revision = domain.getInfo(context, DomainConstants.SELECT_REVISION);
                    } catch (FrameworkException e) {
                        throw new RuntimeException(e);
                    }
                    if (isValidRevision(revision)) {
                        return extractMiddleNumber(revision);
                    }else{
                        return 1;
                    }
                })
                .thenComparing(map -> {
                    DomainObject domain = (DomainObject)map;
                    String revision=null;
                    try {
                        revision = domain.getInfo(context, DomainConstants.SELECT_REVISION);
                    } catch (FrameworkException e) {
                        throw new RuntimeException(e);
                    }
                    if (isValidRevision(revision)) {
                        return extractSuffixNumber(revision);
                    }else{
                        return 000;
                    }

                }));
        log.info("版本排序:{}",majorRevisionsBusObjList);
        return majorRevisionsBusObjList;
    }

    /*
     * @description:返回符合要求的数模版本  currentRevision 版本之前的一个版本到后面的所有版本
     * @author: caipan
     * @date: 2026/3/23 21:00:58
     * @param: * @param[1] context
     * @param[2] args
     * @return:
     **/
    public static BusinessObjectList sortMapListNextRevision(Context context,String[] args) throws Exception{
        String objectId = args[0];
        String currentRevision = args[1];//当前版本：格式如 AA.1-000
        BusinessObjectList majorRevisionsBusObjList = DomainObject.newInstance(context, objectId).getMajorRevisions(context);
        // 原有排序逻辑（同步适配版本格式）
        majorRevisionsBusObjList.sort(Comparator.comparing(map -> {
                    DomainObject domain = (DomainObject)map;
                    String revision=null;
                    try {
                        revision = domain.getInfo(context, DomainConstants.SELECT_REVISION);
                    } catch (FrameworkException e) {
                        throw new RuntimeException(e);
                    }
                    if (isValidRevision(revision)){
                        return revision.substring(0, 2); // 取前两位（如 AA/AB）
                    }else{
                        return "AA";
                    }
                })
                .thenComparing(map -> {
                    DomainObject domain = (DomainObject)map;
                    String revision=null;
                    try {
                        revision = domain.getInfo(context, DomainConstants.SELECT_REVISION);
                    } catch (FrameworkException e) {
                        throw new RuntimeException(e);
                    }
                    if (isValidRevision(revision)) {
                        return Integer.parseInt(revision.split("\\.")[1].split("-")[0]); // 取.和-之间的数字
                    }else{
                        return 1;
                    }
                })
                .thenComparing(map -> {
                    DomainObject domain = (DomainObject)map;
                    String revision=null;
                    try {
                        revision = domain.getInfo(context, DomainConstants.SELECT_REVISION);
                    } catch (FrameworkException e) {
                        throw new RuntimeException(e);
                    }
                    if (isValidRevision(revision)) {
                        return Integer.parseInt(revision.split("-")[1]); // 取-后的数字
                    }else{
                        return 0;
                    }
                }));
        //
        // ========== 步骤2：解析当前版本的三要素（用于定位） ==========
        String targetPrefix = isValidRevision(currentRevision) ? currentRevision.substring(0, 2) : "AA";
        int targetMiddle = isValidRevision(currentRevision) ? Integer.parseInt(currentRevision.split("\\.")[1].split("-")[0]) : 1;
        int targetSuffix = isValidRevision(currentRevision) ? Integer.parseInt(currentRevision.split("-")[1]):0; // 取-后的数字 : 0;
    log.info("targetPrefix:{} targetMiddle:{} targetSuffix:{}",targetPrefix,targetMiddle,targetSuffix);
        // ========== 步骤3：找到currentRevision在排序后列表中的位置 ==========
        int targetIndex = -1;
        for (int i = 0; i < majorRevisionsBusObjList.size(); i++) {
            DomainObject domain = (DomainObject) majorRevisionsBusObjList.get(i);
            String revision = domain.getInfo(context, DomainConstants.SELECT_REVISION);

            // 匹配当前版本
            if (isValidRevision(revision)) {
                String revPrefix = revision.substring(0, 2);
                int revMiddle = Integer.parseInt(revision.split("\\.")[1].split("-")[0]);
                int revSuffix = Integer.parseInt(revision.split("-")[1]);

                if (revPrefix.equals(targetPrefix) && revMiddle == targetMiddle && revSuffix == targetSuffix) {
                    targetIndex = i;
                    break;
                }
            }
        }
        // ========== 步骤4：截取「前一个+当前+后续」版本（核心需求） ==========
        BusinessObjectList resultList = new BusinessObjectList();
        if (targetIndex != -1) {
            // 起始位置：如果有前一个版本则取 targetIndex-1，否则从 targetIndex 开始
            int startIndex = targetIndex > 0 ? targetIndex - 1 : targetIndex;
            // 截取从起始位置到末尾的所有版本
            for (int i = startIndex; i < majorRevisionsBusObjList.size(); i++) {
                resultList.add(majorRevisionsBusObjList.get(i));
            }
        } else {
            // 未找到当前版本时，返回空列表（或根据业务需求调整）
            log.warn("未找到当前版本：{}，返回空列表", currentRevision);
        }
        log.info("保留前一个+当前+后续版本的结果：{}", resultList);
        return resultList;
    }
    /*
     * @description:启动异步线程的通用方法，支持传数组和Map
     * @author: caipan
     * @date: 2026/4/2 22:14:28
     * @param: * @param[1] context
     * @param[2] args 方法需要接收的参数，该参数会透传到具体的方法
     * @param[3] programJPO 执行的JPO
     * @param[4] method 执行的方法
     * @return:
     **/
    public static void runAsync(Context context, String[] args, String programJPO, String method) throws Exception {
        ThreadLog.info("启动异步线程{}:{}", programJPO, method);
        //20260806 update by caipan 记录异步任务提交、开始和完成时间，便于分析线程池排队情况
        final Instant submittedAt = Instant.now();
        ThreadLog.info("异步任务【{}:{}】提交时间:{}", programJPO, method, submittedAt);
        // ===================== 【核心：统一处理参数 + 完美打印】 =====================
        String[] safeArgs = args != null ? args : new String[0];
        Map<String, Object> paramMap = null;
        String printParam; // 用来最终打印的参数字符串
        try {
            // 尝试解析成 Map
            paramMap = JPO.unpackArgs(safeArgs);
            printParam = "Map参数：" + paramMap;
        } catch (Exception e) {
            // 解析失败 → 普通数组，转成可读字符串
            printParam = "数组参数：" + Arrays.toString(safeArgs);
        }
        // 【关键】这里无论什么格式，都能打印出人类可读的参数！
        ThreadLog.info("最终解析参数：{}", printParam);
        // ======================================================================
        try {
            Context threadSafeContext = context.getFrameContext();
            final AtomicBoolean isSuccess = new AtomicBoolean(false);
            CompletableFuture.runAsync(() -> {
                        Instant startedAt = Instant.now();
                        ThreadLog.info("异步任务【{}:{}】实际开始时间:{}，排队耗时:{}ms",
                                programJPO, method, startedAt,
                                Duration.between(submittedAt, startedAt).toMillis());
                        try {
                            JPO.invoke(threadSafeContext, programJPO, null, method, args, void.class);
                            isSuccess.set(true);
                        } catch (Exception e) {
                            ThreadLog.error("处理数据时出错:{}:{}", programJPO, method, e);
                        } finally {
                            if (threadSafeContext != null) {
                                try {
                                    threadSafeContext.close();
                                } catch (Exception ex) {
                                    ThreadLog.error("关闭上下文失败", ex);
                                }
                            }
                            Instant completedAt = Instant.now();
                            ThreadLog.info("异步任务【{}:{}】完成时间:{}，执行耗时:{}ms",
                                    programJPO, method, completedAt,
                                    Duration.between(startedAt, completedAt).toMillis());
                        }
                    }, executor)
                    .whenComplete((unused, throwable) -> {
                        if (throwable == null && isSuccess.get()) {
                            ThreadLog.info("异步线程【{}:{}】执行完成 - 状态：成功", programJPO, method);
                        } else {
                            ThreadLog.info("异步线程【{}:{}】执行完成 - 状态：失败", programJPO, method);
                        }
                    });
        } catch (Exception e) {
            ThreadLog.error("异步任务提交失败:{}", e);
        }
        ThreadLog.info("异步线程:{}:{}已提交，等待执行...", programJPO, method);
    }

    /**
     * 启动返回JSON结果的异步接口，并记录接口返回摘要。
     **
     * @param context 上下文
     * @param args 透传给目标方法的参数
     * @param programJPO 目标JPO名称
     * @param method 目标方法名称
     * @author caipan by codex
     * @date 2026/8/27
     */
    public static void runAsyncWithJsonResult(Context context, String[] args, String programJPO, String method) {
        ThreadLog.info("启动返回JSON结果的异步线程{}:{}", programJPO, method);
        final Instant submittedAt = Instant.now();
        final String[] safeArgs = args == null ? new String[0] : args;
        try {
            Context threadSafeContext = context.getFrameContext();
            final AtomicBoolean isSuccess = new AtomicBoolean(false);
            CompletableFuture.runAsync(() -> {
                Instant startedAt = Instant.now();
                ThreadLog.info("异步接口【{}:{}】实际开始时间:{}，排队耗时:{}ms", programJPO, method, startedAt,
                        Duration.between(submittedAt, startedAt).toMillis());
                try {
                    JSONObject resultJson = (JSONObject) JPO.invoke(threadSafeContext, programJPO, null, method,
                            safeArgs, JSONObject.class);
                    if (resultJson == null) {
                        ThreadLog.error("异步接口【{}:{}】未返回结果", programJPO, method);
                    } else {
                        JSONArray dataArray = resultJson.getJSONArray("dataArray");
                        int dataArrayCount = dataArray == null ? 0 : dataArray.size();
                        String resultCode = resultJson.getString("code");
                        String result = resultJson.getString("result");
                        String message = resultJson.getString("msg");
                        Object filePath = resultJson.get("filePath");
                        ThreadLog.info("异步接口【{}:{}】返回摘要: code={}, result={}, msg={}, dataArrayCount={}{}",
                                programJPO, method, resultCode, result, message, dataArrayCount,
                                filePath == null ? "" : ", filePath=" + filePath);
                        if (!"200".equals(resultCode)) {
                            ThreadLog.error("异步接口【{}:{}】执行失败: code={}, msg={}", programJPO, method,
                                    resultCode, message);
                        } else {
                            isSuccess.set(true);
                        }
                    }
                } catch (Exception e) {
                    ThreadLog.error("异步接口处理失败:{}:{}", programJPO, method, e);
                } finally {
                    if (threadSafeContext != null) {
                        try {
                            threadSafeContext.close();
                        } catch (Exception ex) {
                            ThreadLog.error("关闭上下文失败", ex);
                        }
                    }
                    Instant completedAt = Instant.now();
                    ThreadLog.info("异步接口【{}:{}】完成时间:{}，总耗时:{}ms", programJPO, method, completedAt,
                            Duration.between(submittedAt, completedAt).toMillis());
                    ThreadLog.info("异步接口【{}:{}】执行完成 - 状态：{}", programJPO, method,
                            isSuccess.get() ? "成功" : "失败");
                }
            });
        } catch (Exception e) {
            ThreadLog.error("异步接口任务提交失败:{}:{}", programJPO, method, e);
        }
        ThreadLog.info("返回JSON结果的异步线程:{}:{}已提交，等待执行...", programJPO, method);
    }
    /*
     * @description:启动异步线程
     * @author: caipan
     * @date: 2026/4/8 17:15:46
     * @param: * @param[1] context
     * @param[2] args args[0] JPO args[1]方法
     * @return:
     **/
    public void testRunAsync(Context context,String[] args) throws Exception{
        runAsync(context,args,args[0],args[1]);
    }
    /*
     * @description:获取最新版，OOTB接口获取方式，该方式不适合继峰，他是根据majororder 来判断的
     * @author: caipan
     * @date: 2026/4/8 17:16:42
     * @param: * @param[1] context
     * @param[2] args 接收零件的物理ID的数组
     * @return: 返回如下 islast true代表是最新版 {"lastVersionRequests":[{"id":"05A58B56D7D7340069D5F3BA00018C59","type":"VPMReference","islast":false,"candidates":["05A58B5652283E0069D5F4D700000004"],"status":"SUCCEEDED"}]}
     **/
    public  String ootbgetLastVersions(Context context,String[] args) throws Exception{
        Object localObject1 = new ENOVersioningOptions();
        String paramString1 = "E";
        JSONArray array = new JSONArray();
        for (int i=0;i<args.length;i++){
            JSONObject json = new JSONObject();
            json.put("id",args[i]);
            array.add(json);
        }
        JSONObject json = new JSONObject();
        json.put("lastVersionRequests",array);
//        String paramString2="{\"lastVersionRequests\":[{\"id\":\"05A58B56D7D7340069D5F3BA00018C59\"},{\"id\":\"05A58B5652283E0069D5F4D700000004\"},{\"id\":\"05A58B56D7D7340069D5F4300001F6FC\"}]}";
        ((ENOVersioningOptions)localObject1).setIntentMask(paramString1);
        VersioningServices localVersioningServices = new VersioningServices();
        String  str = localVersioningServices.getLastVersions(context, (ENOVersioningOptions)localObject1, json.toJSONString());

        log.info("str:{}",str);
        return str;
    }


    /**
     * 通用主线程入口
     *
     * @param context 当前请求上下文
     * @param paramsList 任务参数列表 (每个 Map 代表一次 JPO 调用的入参)
     * @param programJPO JPO 类名 (例如 "emxProjectSpace")
     * @param method 方法名 (例如 "createProjectTaskWithSelectTemplate")
     * @param <T> 返回值类型泛型 (例如 StringList.class, Integer.class)
     * @return 所有子任务执行结果的集合
     */
    public static <T> List<T> executeBatch(Context context, MapList paramsList, String programJPO, String method) {
        Instant start = Instant.now();
        List<T> allResults = new ArrayList<>();
        try {
            List<CompletableFuture<T>> futureList = new ArrayList<>();
            // 1. 提交所有异步任务
            for (Object object : paramsList) {
                Map params = (Map)object;
                CompletableFuture<T> future = invokeAsync(context, params, programJPO, method);
                futureList.add(future);
            }
            // 2. 等待所有任务完成
            CompletableFuture<Void> allFutures = CompletableFuture.allOf(
                    futureList.toArray(new CompletableFuture[0])
            );
            // 阻塞主线程直到所有子线程结束
            allFutures.join();
            // 3. 所有子线程都已经执行完了 收集结果
            for (CompletableFuture<T> future : futureList) {
                try {
                    T result = future.get(); // 获取单个结果
                    if (result != null) {
                        allResults.add(result);
                    }
                } catch (Exception e) {
                    // 记录单个任务获取结果的异常
                    System.err.println("获取子线程结果异常: " + e.getMessage());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        Instant end = Instant.now();
        Duration duration = Duration.between(start, end);
        log.error("主线程中子线程耗时:{}:{}", duration.getSeconds());
        return allResults;
    }

    /**
     * 通用子线程执行器
     * 负责处理 ENOVIA Context 的线程安全和 JPO 反射调用
     */
    private static <T> CompletableFuture<T> invokeAsync(Context context, Map params, String programJPO, String method) {
        log.info("启动异步线程{}:{}", programJPO, method);
        //20260806 update by caipan 记录批量任务排队时间，并在任务结束后关闭FrameContext
        final Instant submittedAt = Instant.now();
        log.info("异步任务【{}:{}】提交时间:{}", programJPO, method, submittedAt);
        final AtomicBoolean isSuccess = new AtomicBoolean(false);
        return CompletableFuture.supplyAsync((Supplier<T>) () -> {
            Instant startedAt = Instant.now();
            log.info("异步任务【{}:{}】实际开始时间:{}，排队耗时:{}ms",
                    programJPO, method, startedAt,
                    Duration.between(submittedAt, startedAt).toMillis());
            Map<String, Object> paramMap = new HashMap<>(params);
            Context threadSafeContext = null;
            try {
                // 注意：在 ENOVIA 多线程中必须获取新的 FrameContext
                threadSafeContext = context.getFrameContext();
                // 执行 JPO 调用
                // 注意：这里强制转换为 Object，由上层决定具体类型
                Object result = JPO.invoke(threadSafeContext, programJPO, new String[]{}, method, JPO.packArgs(paramMap), Object.class);
                isSuccess.set(true);
                return (T) result; // 泛型转换
            } catch (Exception e) {
                log.error("子线程 捕获到异常 处理数据时出错:{}:{}", programJPO, method, e);
                e.printStackTrace();
                return null; // 异常时返回 0
            } finally {
                if (threadSafeContext != null) {
                    try {
                        threadSafeContext.close();
                    } catch (Exception closeException) {
                        log.error("关闭子线程FrameContext失败", closeException);
                    }
                }
                Instant completedAt = Instant.now();
                log.info("异步任务【{}:{}】完成时间:{}，执行耗时:{}ms",
                        programJPO, method, completedAt,
                        Duration.between(startedAt, completedAt).toMillis());
            }
        }, executor).whenComplete((unused, throwable) -> {
            if (throwable == null && isSuccess.get()) {
                log.info("异步线程【{}:{}】执行完成 - 状态：成功", programJPO, method);
            } else {
                log.info("异步线程【{}:{}】执行完成 - 状态：失败", programJPO, method);
            }
        });
    }

    /**
     * 获取最新版本，通过 PLMReference.V_isLastVersion 属性来判断
     **
     * @param context
     * @param vpmId 零件ID
     * @return
     * @throws Exception
     * @author caipan
     * @date 2026/7/2 10:15
     */
    public static String getLastMajoridByIsLastVersion(Context context, String vpmId) throws Exception {
        String lastId = vpmId;
        try {
            BusinessObjectList majorRevisionsBusObjList = DomainObject.newInstance(context, vpmId).getMajorRevisions(context);
            DomainObject doObj;
            String flag;
            String objectId ;
            log.info("size:{}",majorRevisionsBusObjList.size());
            for (int i = majorRevisionsBusObjList.size() - 1; i >= 0; i--) {
                doObj = (DomainObject) majorRevisionsBusObjList.get(i);
                flag = doObj.getAttributeValue(context, JF_PLMConstants_mxJPO.ATTR_V_IsLastVersion);
                objectId = doObj.getObjectId(context);
                if ("TRUE".equalsIgnoreCase(flag)) {
                    return objectId;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return lastId;
        }
        return lastId;
    }

    /**
     * 获取零件归属项目
     **
     * @param context
     * @param args
     * @throws Exception
     * @author caipan
     * @date 2026/7/8 14:17
     */
    public String  getPartBelongProject(Context context,String[] args) throws Exception{
        String objectId = args[0];
        DomainObject partObj = DomainObject.newInstance(context, objectId);
        String projectId = "";
        MapList mapList = partObj.getRelatedObjects(
                context,
                JF_PLMConstants_mxJPO.rel_JFProject2RootPart,
                DomainConstants.TYPE_PROJECT_SPACE,
                basicBolistSel(),
                basicRellistSel(),
                true,
                false,
                (short) 1, // recursion level
                "", //object where clause
                "attribute[JF_BelongPart]==Y", //relationship where clause
                1
        );
        if(mapList.size()>0){
            projectId = UIUtil.getValue( ((Map)(mapList.get(0))),DomainConstants.SELECT_ID);
        }
        return projectId;
    }

    /**
     * 根据零件和项目获取客户零件号关系上的DirectBuy
     **
     * @param context 上下文
     * @param partId 零件ID
     * @param projectId 项目ID
     * @return String DirectBuy值；项目为空、未找到对应关系或关系属性为空时返回空字符串
     * @throws Exception
     * @author LIUJR
     * @date 2026/7/27
     */
    public static String getPartDirectBuyByProject(Context context, String partId, String projectId) throws Exception {
        //20260727 update by ljr 未维护项目对应的客户零件关系时，DirectBuy按空值返回，不默认推断为non-DB；
        String directBuy = DomainConstants.EMPTY_STRING;
        if (UIUtil.isNullOrEmpty(partId) || UIUtil.isNullOrEmpty(projectId)) {
            return directBuy;
        }

        String projectIdSelect = "frommid["
                + JF_PLMConstants_mxJPO.RELATIONSHIP_JFVPMREFERENCE2CUSTOMERPARTS2PROJECT
                + "].to.id";
        StringList objectSelectList = new StringList(DomainConstants.SELECT_ID);
        StringList relationshipSelectList = new StringList(projectIdSelect);
        relationshipSelectList.add(JF_PLMConstants_mxJPO.Select_Attr_JF_DirectBuy);
        MapList customerPartsList = DomainObject.newInstance(context, partId).getRelatedObjects(
                context,
                JF_PLMConstants_mxJPO.RELATIONSHIP_JFVPMREFERENCE2CUSTOMERPARTS,
                JF_PLMConstants_mxJPO.TYPE_JFCUSTOMERPARTS,
                objectSelectList,
                relationshipSelectList,
                true,
                false,
                (short) 1,
                DomainConstants.EMPTY_STRING,
                DomainConstants.EMPTY_STRING,
                0);
        for (int i = 0; i < customerPartsList.size(); i++) {
            Map customerPartsMap = (Map) customerPartsList.get(i);
            if (!projectId.equals(UIUtil.getValue(customerPartsMap, projectIdSelect))) {
                continue;
            }
            String relationshipDirectBuy = UIUtil.getValue(
                    customerPartsMap,
                    JF_PLMConstants_mxJPO.Select_Attr_JF_DirectBuy);
            if (UIUtil.isNotNullAndNotEmpty(relationshipDirectBuy)) {
                directBuy = relationshipDirectBuy;
            }
            break;
        }
        return directBuy;
    }

    /**
     * 根据零件和项目获取业务传递场景使用的DirectBuy
     * @param context 上下文
     * @param partId 零件ID
     * @param projectId 项目ID
     * @return String DirectBuy值；项目为空、未找到对应关系或关系属性为空时返回non-DB
     * @throws Exception
     * @author LIUJR
     * @date 2026/7/27
     * @description 接口同步/导出及MBOM初始化、更新、导出、同步使用；Table显示继续调用getPartDirectBuyByProject保持空值。
     */
    public static String getPartDirectBuyByProjectWithNonDBDefault(
            Context context,
            String partId,
            String projectId) throws Exception {
        //20260727 update by ljr 业务传递场景单独提供non-DB默认值，避免调用端嵌套处理DirectBuy；
        String directBuy = getPartDirectBuyByProject(context, partId, projectId);
        return UIUtil.isNullOrEmpty(directBuy)
                ? JF_PLMConstants_mxJPO.ATTR_ATTR_JFDIRECT_BUY_RANGE_N
                : directBuy;
    }

    /**
     * 按指定项目批量补充零件行的DirectBuy
     **
     * @param context 上下文
     * @param partList 零件行数据
     * @param projectId 项目ID
     * @return void
     * @throws Exception
     * @author LIUJR
     * @date 2026/7/24
     */
    public static void populatePartDirectBuyByProject(Context context, MapList partList, String projectId) throws Exception {
        if (partList == null || partList.isEmpty()) {
            return;
        }

        Map<String, String> directBuyCache = new HashMap<>();
        for (int i = 0; i < partList.size(); i++) {
            Map partMap = (Map) partList.get(i);
            String partId = UIUtil.getValue(partMap, DomainConstants.SELECT_ID);
            String directBuy = directBuyCache.get(partId);
            if (directBuy == null) {
                directBuy = getPartDirectBuyByProject(context, partId, projectId);
                directBuyCache.put(partId, directBuy);
            }
            // 保留原DirectBuy对象属性select作为Map键，避免改变现有ECR字段权限与取值代码。
            partMap.put(JF_PLMConstants_mxJPO.SELECT_ATTR_JFDIRECT_BUY, directBuy);
        }
    }

    /**
     * 将中英文日期字符串转换为达索系统日期格式
     **
     * @param value 原始日期字符串
     * @return String 达索系统日期格式；空值返回空字符串
     * @throws Exception 日期格式无法识别时抛出异常
     * @author caipan by codex
     * @date 2026/8/18
     */
    public static String convertDateToMatrixFormat(String value) throws Exception {
        if (UIUtil.isNullOrEmpty(value)) {
            return DomainConstants.EMPTY_STRING;
        }

        String dateValue = value.trim();
        List<DateTimeFormatter> dateTimeFormatters = Arrays.asList(
                DateTimeFormatter.ofPattern("M/d/uuuu h:mm:ss a", Locale.US),
                DateTimeFormatter.ofPattern("M/d/uuuu h:mm a", Locale.US),
                DateTimeFormatter.ofPattern("MMM d, uuuu h:mm:ss a", Locale.US),
                DateTimeFormatter.ofPattern("MMM d, uuuu h:mm a", Locale.US));
        for (DateTimeFormatter formatter : dateTimeFormatters) {
            try {
                return MATRIX_FORMAT.format(LocalDateTime.parse(dateValue, formatter));
            } catch (DateTimeParseException ignored) {
                // Continue with the next supported date-time format.
            }
        }

        List<DateTimeFormatter> dateFormatters = Arrays.asList(
                DateTimeFormatter.ofPattern("MMM d, uuuu", Locale.US),
                DateTimeFormatter.ofPattern("MMMM d, uuuu", Locale.US),
                DateTimeFormatter.ofPattern("MMM d uuuu", Locale.US),
                DateTimeFormatter.ofPattern("MMMM d uuuu", Locale.US),
                DateTimeFormatter.ofPattern("uuuu年M月d日", Locale.CHINA),
                DateTimeFormatter.ofPattern("uuuu/M/d"),
                DateTimeFormatter.ofPattern("uuuu-M-d"),
                DateTimeFormatter.ofPattern("M/d/uuuu"));
        for (DateTimeFormatter formatter : dateFormatters) {
            try {
                return MATRIX_FORMAT.format(LocalDate.parse(dateValue, formatter).atTime(12, 0));
            } catch (DateTimeParseException ignored) {
                // Continue with the next supported date format.
            }
        }
        throw new Exception("无法识别日期格式: " + value);
    }
}
