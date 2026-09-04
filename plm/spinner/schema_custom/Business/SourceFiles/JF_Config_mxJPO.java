import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.dassault_systemes.createcontent.ErrorMngt.ENONewException;
import com.dassault_systemes.fl.modeler.util.FLJsonUtil;
import com.dassault_systemes.plm.config.entity.itf.ConfiguredEntitiesFactory;
import com.dassault_systemes.plm.config.entity.itf.IConfiguredEntitiesFactory;
import com.dassault_systemes.plm.config.entity.itf.IConfiguredEntity;
import com.dassault_systemes.plm.config.webservices.JSonUtilities;
import com.dassault_systemes.plm.config.webservices.navigation_services.args.GetMultipleFilterableObjectInfoRequiredFacets;
import com.matrixone.apps.common.Route;
import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.DomainRelationship;
import com.matrixone.apps.domain.util.*;
import com.matrixone.apps.framework.ui.UIUtil;
import ds.enovia.apps.msf.BusinessObjects.ExecuteJPO.args;
import jakarta.json.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.HttpMethod;
import matrix.db.AttributeList;
import matrix.db.Context;
import matrix.db.JPO;
import matrix.db.RelationshipType;
import matrix.util.StringList;
import org.apache.commons.text.StringEscapeUtils;
import org.apache.poi.ss.formula.functions.T;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.xmlbeans.impl.store.DomImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

import static com.dassault_systemes.product.common.services.utility.Value.VPMInstance;
import static com.matrixone.apps.domain.DomainConstants.*;
import static com.matrixone.apps.domain.MultiValueSelects.RELATIONSHIP_REFERENCE_DOCUMENT;

/**
 *
 * @author jjs
 */
public class JF_Config_mxJPO implements JF_PLMConstants_mxJPO{
    private  static final Logger logger = LoggerFactory.getLogger(JF_Config_mxJPO.class);
    private  JF_WebServiceHandler_mxJPO WebServiceHandler = new JF_WebServiceHandler_mxJPO();

    /**
     * 结算单车BOM
     * @param context
     * @param rootPhysicalId 跟节点的物理ID
     * @param pcId 产品配置的物理ID
     */
    public Map<String,StringList> getBOM(Context context,String rootPhysicalId,String pcId){
        logger.info("getBOM rootPhysicalId :{} pcId:{}",rootPhysicalId,pcId);
        StringList selList = new StringList();
        selList.add("id");
        selList.add("name");
        selList.add("type");
        selList.add("physicalid");
        selList.add("revision");

        StringList relList = new StringList();
        relList.add("physicalid[connection]");
        relList.add("attribute[PLMInstance.V_hasConfigEffectivity]");

        Map<String,String> returMap = new HashMap<>();

        Map<String, StringList> resultMap = new HashMap<>();
        try {
            String[] paramArgs = {rootPhysicalId, pcId};
            String resultStr = getProductConfigurationFilterResult24(context, paramArgs);//过滤单车  调用的24的webservice
            resultMap = getVPMReferenceByProductConfigurationFilter24(resultStr);//解析单车的结果 调用的是24的
        }catch (Exception ex){
            ex.printStackTrace();
            returMap.put("flag","false");
            returMap.put("message",ex.getMessage());
        }
        returMap.put("flag","true");
        return resultMap;
    }

    /**
     * get sv config txt
     * @param pid SV Connection Physical ID
     * @param pid
     * @return
     * @throws Exception
     */
    public  Map getConfigurationTEXT(Context context,StringList pid,String withDescription)throws Exception{
        return getConfiguration(context,pid,"TXT",withDescription,"ALL");
    }

    public  Map getConfigMain(Context context,String[]args)throws Exception{
        StringList pidList = new StringList();
        for (int i=0;i<args.length;i++){
            pidList.add(args[i]);
        }
        Map expression =  getConfiguration(context,pidList,"TXT","YES","ALL");
        logger.info("expression:{}",expression);
        return expression;
    }
    /**
     * get sv config xml
     * @param context
     * @param pid SV Connection Physical ID
     * @return Map  key是关系的物理ID value 是配置表达式
     * @throws Exception
     */
    public static Map getConfigurationXML(Context context,StringList pid)throws Exception{
        return getConfiguration(context,pid,"XML","NO","Current");
    }

    /*
     * @description:
     * @author: caipan
     * @date:
     * @param: * @param[1] context
     * @param[2] pid
     * @param[3] targetFormat
     * @param[4] withDescription
     * @param[5] view
     * @return: Map  key是关系的物理ID value 是配置表达式
     **/
    public static Map getConfiguration(Context context,StringList pid,String targetFormat,String withDescription,String view) throws Exception {
        JsonObjectBuilder var3 = FLJsonUtil.BUILDER_FACTORY.createObjectBuilder();
        var3.add("version", "2.0");
        JsonObjectBuilder var4 = FLJsonUtil.BUILDER_FACTORY.createObjectBuilder();
        var4.add("targetFormat", targetFormat);
        var4.add("withDescription", withDescription);
        var4.add("view", view);
        var4.add("domains", "All");
        var3.add("output", var4.build());
        JsonArrayBuilder var5 = FLJsonUtil.BUILDER_FACTORY.createArrayBuilder();
        ArrayList var1 = new ArrayList();
        var1.addAll(pid);
        Iterator var6 = var1.iterator();

        String var7;
        while(var6.hasNext()) {
            var7 = (String)var6.next();
            var5.add(var7);
        }
        var3.add("pidList", var5.build());
        String var8 = var3.build().toString();
        GetMultipleFilterableObjectInfoRequiredFacets facets = new GetMultipleFilterableObjectInfoRequiredFacets(var8,"0");
        JsonObjectBuilder jsonObjectBuilder = JSonUtilities.getMultipleFilterableObjectInfoAsJsonBuilder(context, facets);
        JsonObject jsonObject = jsonObjectBuilder.build();
        Map jsonMap =  getVariantFromJsonObject(jsonObject);
        logger.info("json:{}",jsonMap);
        return jsonMap;
    }

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


    public static String getVariantFromJsonObjectBak(JsonObject jsonObject){
        JsonObject expressionsJsonObj = jsonObject.getJsonObject("expressions");
        Set keySet = expressionsJsonObj.keySet();
        String variantStr = "";
        if(keySet.size()==1){
            String relPhysicalId = (String) keySet.toArray()[0];
            JsonObject configJsonObj = expressionsJsonObj.getJsonObject(relPhysicalId);
            String hasEffectivity = configJsonObj.getString("hasEffectivity");
            if("YES".equals(hasEffectivity)){
                JsonObject contentJsonObj = configJsonObj.getJsonObject("content");
                variantStr = contentJsonObj.getString("Variant");
            }
        }
        return variantStr;
    }



    public String getProductConfigurationFilterResult24(Context context, String[] args) {
        String filterJsonStr = "{\"batch\":{\"expands\":[{\"label\":\"expandLabelName\",\"root\":{\"physical_id\":\"rootPhysicalId\"}," +
                "\"filter\":{\"or\":{\"filters\":[{\"and_not\":{\"left\":{\"and\":{\"filters\":[{\"config_filter_id\":{\"physical_id\":" +
                "\"productConfigurationPhysicalId\"}},{\"prefix_filter\":{\"prefix_path\":[{\"physical_id_path\":[\"rootPhysicalId\"]}]}}]}}," +
                "\"right\":{\"or\":{\"filters\":[{\"sequence_filter\":{\"sequence\":[{\"uql\":\"NOT (((typecode:0 OR typecode:1) AND " +
                "(ds6w_58_globaltype:\\\"ds6w:Document\\\" OR ds6w_58_globaltype:\\\"ds6w:Part\\\"))  OR " +
                "(typecode:2 AND NOT (flattenedtaxonomies:\\\"reltypes/XCADBaseDependency\\\")))\"}]}}]}}}}]}}}]}," +
                "\"outputs\":{\"select_object\":[\"physicalid\",\"ds6w:globalType\",\"ds6w:label\",\"ds6wg:revision\"," +
                "\"ds6w:type\",\"ds6w:description\",\"ds6w:responsible\",\"ds6w:cadMaster\",\"ds6w:identifier\"," +
                "\"ds6wg:PLMReference.V_isLastVersion\",\"ds6w:reserved\",\"ds6w:status\",\"ds6wg:EnterpriseExtension.V_PartNumber\"," +
                "\"ds6w:category\",\"owner\",\"ds6w:reservedBy\",\"bo.pgpshowextension.V_PGP_Show\",\"ds6w:modified\",\"ds6w:project\"]," +
                "\"select_relation\":[\"physicalid\",\"ds6w:globalType\",\"ds6w:label\",\"matrixtxt\",\"ro.pgpshowextension.V_PGP_Show\"," +
                "\"ro.plminstance.v_treeorder\"],\"hits\":{\"predefined_computation\":[\"urlstream|thumbnail|2dthb|allrefs\",\"icons\"]}," +
                "\"select_pgp\":[\"show\",\"hide\"]}}";

        String productConfigurationFilterResult = "";
        try {
            String expandLabelName = System.currentTimeMillis() + "";
            String rootPhysicalId = args[0];
            String productConfigurationPhysicalId = args[1];
//            logger.info("getProductConfigurationFilterResult>>>>>>>>>>>>rootPhysicalId = " + rootPhysicalId);
//            logger.info("getProductConfigurationFilterResult>>>>>>>>>>>>productConfigurationPhysicalId = " + productConfigurationPhysicalId);

            String securityCtx =  PersonUtil.getDefaultSecurityContext(context,context.getUser());//getUserSecurityContextString(context, context.getUser());
            String interfaceAddress = "/cvservlet/progressiveexpand/v2?output_format=cvjson&tenant=OnPremise" +
                    "&SecurityContext=ctx::" + securityCtx;
//            logger.info("getProductConfigurationFilterResult>>>>>>>>>>>>interfaceAddress = " + interfaceAddress);
            filterJsonStr = filterJsonStr.replace("expandLabelName", expandLabelName);
            filterJsonStr = filterJsonStr.replaceAll("rootPhysicalId", rootPhysicalId);
            filterJsonStr = filterJsonStr.replace("productConfigurationPhysicalId", productConfigurationPhysicalId);
//            logger.info("getProductConfigurationFilterResult>>>>>>>>>>>filterJsonStr>>>>>>>>>>>>>>>>>" + filterJsonStr);
            JSONObject filterJson = JSONObject.parseObject(filterJsonStr);
//            JsonObject filterJson = Json.createReader(new StringReader(filterJsonStr)).readObject();
            JSONObject resultJson = WebServiceHandler.executeWebService(context, HttpMethod.POST, interfaceAddress, securityCtx, "zh",
                    filterJson);
//            logger.info("getProductConfigurationFilterResult>>>>>>>>>resultJson = " + resultJson);
            assert resultJson != null;
            productConfigurationFilterResult = resultJson.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
//        logger.info("getProductConfigurationFilterResult>>>>>>>>>>>>>Filter Results>>>>>>>>>>>>>>>>>>>" + productConfigurationFilterResult);
        return productConfigurationFilterResult;
    }
    public String getProductConfigurationFilterResult21(Context context, String[] args) {
        String filterJsonStr="{\"select_rel\":[\"physicalid\",\"ds6w:type\",\"ds6w:label\",\"ro.plminstance.V_treeorder\",\"matrixtxt\",\"ds6w:globalType\",\"ds6wg:NioVPMInstExtension.NioInstECUMark\",\"ro.PGPShowExtension.V_PGP_Show\",\"ro.PGPColorExtension.V_PGP_ColorRGB.Asstring\",\"ro.PGPOpacityExtension.V_PGP_Opacity\",\"ro.PGPInheritanceExtension.V_PGP_Inheritance\"],\"compute_select_bo\":[\"thumbnail_2d\",\"icon\"],\"fcs_url_mode\":\"REDIRECT\",\"specVersion\":\"420.6.1\",\"union\":{\"expand\":[{\"root_path_physicalid\":[[\"rootphysicalid\"]],\"config_filter\":\"configfilter\",\"expand_iter\":\"-1\",\"q.iterative_filter_query_bo\":\"[ds6w:globalType]:\\\"ds6w:Document\\\" OR [ds6w:globalType]:\\\"ds6w:Part\\\"\",\"no_type_filter_rel\":[\"XCADBaseDependency\",\"Reference Document\"]}]},\"select_bo\":[\"ds6w:type\",\"ds6wg:revision\",\"ds6w:identifier\",\"physicalid\"],\"label\":\"rong.ni-ENOPSTR_AP-1689929260141\",\"select_pgp\":[\"Show\",\"Color\",\"Opacity\"]}";
        String productConfigurationFilterResult = "";
        String configFilter = "";
        try {
            String expandLabelName = System.currentTimeMillis() + "";
            String rootPhysicalId = args[0];
            String productConfigurationPhysicalId = args[1];
            DomainObject pcObj = DomainObject.newInstance(context);
            pcObj.setId(productConfigurationPhysicalId);
            configFilter = pcObj.getAttributeValue(context, "Filter Compiled Form");
//            logger.info("getProductConfigurationFilterResult>>>>>>>>>>>>rootPhysicalId = " + rootPhysicalId);
//            logger.info("getProductConfigurationFilterResult>>>>>>>>>>>>productConfigurationPhysicalId = " + productConfigurationPhysicalId);
//            logger.info("getProductConfigurationFilterResult>>>>>>>>>>>>configFilter = " + configFilter);

            String securityCtx = PersonUtil.getDefaultSecurityContext(context,context.getUser());//getUserSecurityContextString(context, context.getUser());
            String interfaceAddress = "/cvservlet/multiexpand?&tenant=OnPremise" +
                    "&SecurityContext=ctx::" + securityCtx;
//            logger.info("getProductConfigurationFilterResult>>>>>>>>>>>>interfaceAddress = " + interfaceAddress);
//            filterJsonStr = filterJsonStr.replace("label", expandLabelName);
            filterJsonStr = filterJsonStr.replaceAll("rootphysicalid", rootPhysicalId);
            filterJsonStr = filterJsonStr.replace("configfilter", configFilter);

            JSONObject filterJson = new JSONObject();
//            logger.info("getProductConfigurationFilterResult>>>>>>>>>>>filterJsonStr>>>>>>>>>>>>>>>>>" + filterJsonStr);
//            JsonObject filterJson = Json.createReader(new StringReader(filterJsonStr)).readObject();
            JSONObject resultJson = WebServiceHandler.executeWebService(context, HttpMethod.POST, interfaceAddress, securityCtx, "zh",
                    filterJson);
//            logger.info("getProductConfigurationFilterResult>>>>>>>>>resultJson = " + resultJson);
            assert resultJson != null;
            productConfigurationFilterResult = resultJson.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
//        logger.info("getProductConfigurationFilterResult>>>>>>>>>>>>>Filter Results>>>>>>>>>>>>>>>>>>>" + productConfigurationFilterResult);
        return productConfigurationFilterResult;
    }



    public String getJsonValue(JsonObject paramJson, String key) {
        String value = "";
        if (paramJson.containsKey(key)) {
            value = paramJson.getString(key);
        }
        return value;
    }

    public Map<String, Set> getVPMReferenceByProductConfigurationFilter21(String productConfigurationFilterResult) {
//        logger.info("getVPMReferenceByProductConfigurationFilter>>>>>>>>productConfigurationFilterResult>>>>>>>>>>>" + productConfigurationFilterResult);
        Map<String, Set> reusltMap = new HashMap<>();
        Set vpmPhycialIdList = new HashSet();
        Set relPhycialIdList = new HashSet();
        ParseFilterString(productConfigurationFilterResult,vpmPhycialIdList,relPhycialIdList);
        reusltMap.put("vpmPhycialIdList", vpmPhycialIdList);
        reusltMap.put("relPhycialIdList", relPhycialIdList);
        return reusltMap;
    }
    public void ParseFilterString(String result, Set busset, Set relSet){
        logger.info("ParseFilterString start");
        JSONObject resultObj = JSONObject.parseObject(result);
        JSONArray array = resultObj.getJSONArray("results");
        logger.info(array.size()+" array Size");
        for (int i=0;i<array.size();i++){
            JSONObject attributes = JSONObject.parseObject(array.get(i).toString());
//            System.out.println(attributes+" attributes");
            if(attributes.containsKey("attributes")) {
                JSONArray attribute = attributes.getJSONArray("attributes");
                String type = "";
                String physicalid = "";
                for (int j = 0; j < attribute.size(); j++) {
                    JSONObject value = JSONObject.parseObject(attribute.get(j).toString());
                    if ("ds6w:type".equalsIgnoreCase(value.get("name").toString())) {
                        type = value.get("value").toString();
                    }
                    if ("physicalid".equalsIgnoreCase(value.get("name").toString())) {
                        physicalid = value.get("value").toString();
                    }
                    if (j == attribute.size() - 1)
                        if (UIUtil.isNotNullAndNotEmpty(physicalid) && UIUtil.isNotNullAndNotEmpty(type)) {
                            if (type.equalsIgnoreCase("VPMInstance")) {
                                relSet.add(physicalid);
                            }
                            if (type.equalsIgnoreCase("VPMReference")) {
                                busset.add(physicalid);
                            }
                        }
                }
            }
        }
        logger.info("ParseFilterString end");
    }

    /**
     * eBOMExpandItem 导出的内容
     * templatepath 模版的绝对路径
     * savepath 保存的绝对地址
     * @param eBOMExpandItem
     * @param templatepath
     * @param savepath
     * @return
     * @throws IOException
     */
    public  String Export(MapList eBOMExpandItem, String templatepath, String savepath) throws IOException {
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


    public Map<String, StringList> getVPMReferenceByProductConfigurationFilter24(String productConfigurationFilterResult) {
//        logger.info("getVPMReferenceByProductConfigurationFilter>>>>>>>>productConfigurationFilterResult>>>>>>>>>>>" + productConfigurationFilterResult);
        Map<String, StringList> reusltMap = new HashMap<>();
        StringList vpmPhycialIdList = new StringList();
        StringList relPhycialIdList = new StringList();
        JSONObject productConfigurationFilterResultJson = JSONObject.parseObject(productConfigurationFilterResult);
//        logger.info("productConfigurationFilterResultJson:{}",productConfigurationFilterResultJson.get("data"));
        JSONObject data = (JSONObject)productConfigurationFilterResultJson.get("data");
        JSONArray productConfigurationFilterResultArray = data.getJSONArray("results");
//        logger.info("productConfigurationFilterResultArray:{}",productConfigurationFilterResultArray);
        for (int i = 0; i < productConfigurationFilterResultArray.size(); i++) {
            JSONObject vpmInfoJson = productConfigurationFilterResultArray.getJSONObject(i);
            String globalType = vpmInfoJson.getString("ds6w:globalType");
            if ("ds6w:Part".equals(globalType)) {
                String vpmPhycialId = vpmInfoJson.getString("resourceid");
                vpmPhycialIdList.add(vpmPhycialId);
            }

            if ("ds6w:Instance".equals(globalType)) {
                String relPhycialId = vpmInfoJson.getString("resourceid");
                relPhycialIdList.add(relPhycialId);
            }
        }
        reusltMap.put("vpmPhycialIdList", vpmPhycialIdList);
        reusltMap.put("relPhycialIdList", relPhycialIdList);
//        logger.info("getVPMReferenceByProductConfigurationFilter>>>>>>>>>>>>reusltMap>>>>>>>" + reusltMap);
        return reusltMap;
    }
    /*
     * @description: 获取虚拟层+V5的一级件
     * @author: caipan
     * @date:
     * @param: * @param[1] context
     * @param[2] args 整椅ID
     * @return:
     **/
    public Map<String, StringList> getSuperBom(Context context,String[] args) throws Exception {
        int level = 4;
        Map<String, StringList> v5MapList = new HashMap();
        try {
            ContextUtil.pushContext(context);
            DomainObject Wholechair = DomainObject.newInstance(context);
            String wholeId = args[0];
            Wholechair.setId(wholeId);
            StringList selList = JF_Util_mxJPO.basicBolistSel();
            selList.add(SELECT_ATTR_V_CADOrigin);
            selList.add(SELECT_ATTR_JFPartType);
            StringList relList = JF_Util_mxJPO.basicRellistSel();
            relList.add("from." + SELECT_ATTR_V_CADOrigin);
            relList.add("from." + SELECT_ID);
            relList.add("from." + SELECT_ATTR_JFPartType);
            String type = Wholechair.getInfo(context, SELECT_ATTR_JFPartType);
            if (type.equalsIgnoreCase("C")) {
                level = 3;
            }
            MapList allVpmList = Wholechair.getRelatedObjects(context, REL_Instance, TYPE_VPMReference, selList, relList, false, true, (short) level, null, null, 0);
//            logger.info("allVpmList:{}", allVpmList);
            //SELECT_ATTR_V_CADOrigin
            String fromCADOrigin, toCADOrigin, fromId, toId, toType, fromType;

            StringList gcList = new StringList();
            gcList.add(wholeId);
            for (int i = 0; i < allVpmList.size(); i++) {
                //如果父是V6 子是V5 这种V5的一级件需要保留
                // 如果第一层是V5也需要保留
                Map temp = (Map) allVpmList.get(i);
                fromCADOrigin = UIUtil.getValue(temp, "from." + SELECT_ATTR_V_CADOrigin);
                toCADOrigin = UIUtil.getValue(temp, SELECT_ATTR_V_CADOrigin);
                toType = UIUtil.getValue(temp, SELECT_ATTR_JFPartType);
                fromType = UIUtil.getValue(temp, "from." + SELECT_ATTR_JFPartType);
                StringList v5List = new StringList();
                StringList v5parentList = new StringList();
                StringList v6List = new StringList();
                fromId = UIUtil.getValue(temp, "from." + SELECT_ID);
                toId = UIUtil.getValue(temp, SELECT_ID);
                if ("".equalsIgnoreCase(fromCADOrigin) && toCADOrigin.equalsIgnoreCase("CATIAV5")) {
                    //保存需要引用的V5
                    if (v5MapList.containsKey(fromId)) {
                        v5List = v5MapList.get(fromId);
                        v5List.add(toId);
                        v5MapList.put(fromId, v5List);
                    } else {
                        v5List.add(toId);
                        v5MapList.put(fromId, v5List);
                    }
                }

                //需要创建V5零件的V6数据 整椅需要新建
                if ("".equalsIgnoreCase(fromCADOrigin) && fromType.equalsIgnoreCase("X")&& toType.equalsIgnoreCase("C")) {
                    if (!gcList.contains(toId)) {
                        gcList.add(toId);
                    }
                }
            }
            logger.info("gcList:{}", gcList);
            logger.info("v5MapList:{}", v5MapList);
            v5MapList.put("gcList", gcList);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            ContextUtil.popContext(context);
        }
        return v5MapList;
    }

    /*
     * @description:创建单车BOM入口
     * @author: caipan
     * @date:
     * @param: * @param[1] context
     * @param[2] args
     * @return:
     **/
    public boolean createSingleBOM(Context context,String[] args) throws Exception, ENONewException {
        boolean flag = true;
        StringList createList = new StringList();
        boolean ispush = false;
        try {
//            ContextUtil.startTransaction(context, true);
            logger.info("createSingleBOM start");
            Map request = JPO.unpackArgs(args);
            StringList pcList = (StringList) request.get("pcList");
            String rootId = UIUtil.getValue(request, "objectId");
            HttpServletRequest req = (HttpServletRequest) request.get("HttpServletRequest");
            DomainObject rootObj = DomainObject.newInstance(context);
            rootObj.setId(rootId);//超级BOM根节点
            String type = rootObj.getInfo(context, SELECT_ATTR_JFPartType);//GX还是GC
//            String projectId = rootObj.getInfo(context, "to[JFProject2RootPart].from.id");//根节点关联的项目
            String projectId =  new JF_VPMReferenceEBOM_mxJPO().getPartBelongProject(context, new String[]{rootId});
            String phyrootId = rootObj.getPhysicalId(context);
            //获取超级BOM的结构，并且获取到V5一级件列表，组装好V5件需要关联的GC件
            String[] create = new String[]{rootId};
            Map<String, StringList> v5MapSource = getSuperBom(context, create);

            //end
            //创建数模
            StringList oldAttribute = new StringList();
            oldAttribute.add(SELECT_ATTR_JF_PartNameCN);
            oldAttribute.add(SELECT_ATTR_JF_PartNameEN);
            oldAttribute.add(SELECT_ATTR_JF_ProcurementType);
            oldAttribute.add(SELECT_ATTR_JFDIRECT_BUY);
            oldAttribute.add(select_attr_JF_ISWholeChair);

            for (int i = 0; i < pcList.size(); i++) {
            //解析超级BOM的整椅 20250108
                Map<String, StringList> v5Map = deepCopy(v5MapSource);
                StringList gcList = v5Map.get("gcList");
                //获取所有的GC和一级件需要建立的关系(V5一级件)
                Map<String, StringList> gcMap = new HashMap();
                logger.info("v5Map639:{}", v5Map);
                for (String key : v5Map.keySet()) {
                    if (!"gcList".equalsIgnoreCase(key)) {
                        //查找父
                        String parentGC = getParentId(context, key);
                        logger.info("key:{} parentGC:{}", key, parentGC);
                        if (UIUtil.isNotNullAndNotEmpty(parentGC)) {//如果父不为空
                            StringList sourceList = v5Map.get(key);
                            if (!gcMap.containsKey(parentGC)) {
                                gcMap.put(parentGC, sourceList);
                            } else {
                                StringList temp = gcMap.get(parentGC);
                                sourceList.addAll(temp);
                                gcMap.put(parentGC, sourceList);
                            }
                        } else {
                            StringList sourceList = v5Map.get(key);
                            if (gcMap.containsKey(key)) {
                                StringList temp = gcMap.get(key);
                                sourceList.addAll(temp);
                                gcMap.put(key, sourceList);
                            } else {
                                gcMap.put(key, sourceList);
                            }
                        }
                        logger.info("gcMap:{}", gcMap);
                    }
                }
                logger.info("gcMap:{}", gcMap);
                //end


                DomainObject pcObj = DomainObject.newInstance(context);
                pcObj.setId(pcList.get(i));//PC
                String phyPcId = pcObj.getPhysicalId(context);
                String createRootId = "";
                //先判断当前PC
                String singleId = pcObj.getInfo(context, "from[" + REL_JFPC2EBOM + "].to.id");
                Map<String, StringList> sigleBOM = getBOM(context, phyrootId, phyPcId);//结算出来的单车BOM
                StringList vpmPhycialIdList = sigleBOM.get("vpmPhycialIdList");//所有的对象
                StringList sel = new StringList();
                sel.add(SELECT_ID);
                //原来是物理ID，换成ID
                MapList results = DomainObject.getInfo(context, vpmPhycialIdList.toStringArray(), sel);
                StringList filterList = new StringList();
                for (int r = 0; r < results.size(); r++) {
                    Map temp = (Map) results.get(r);
                    filterList.add(UIUtil.getValue(temp, SELECT_ID));
                }

                StringList relPhycialIdList = sigleBOM.get("relPhycialIdList");//所有的关系


                if (UIUtil.isNotNullAndNotEmpty(singleId)) {//走更新逻辑
                    logger.info("update");
                    StringList allV5Part = new StringList();
                    if (type.equalsIgnoreCase("X")) {
                        if (v5Map.containsKey(rootId)) {
                            StringList rootV5List = v5Map.get(rootId);
                            allV5Part.addAll(rootV5List);
                            gcList.remove(0);
                        }
                    }
                    for (int a = 0; a < gcList.size(); a++) {//整椅下关联的V5一级件
                        if (gcMap.containsKey(gcList.get(a))) {
                            allV5Part.addAll(gcMap.get(gcList.get(a)));
                        }
                    }
                    //过滤掉结算外的V5一级件
                    logger.info("allV5Part:{}", allV5Part);
                    StringList allFilterV5Part = new StringList();//所有的应该存在的V5数据
                    for (int a = 0; a < allV5Part.size(); a++) {
                        if (filterList.contains(allV5Part.get(a))) {
                            allFilterV5Part.add(allV5Part.get(a));
                        }
                    }
                    logger.info("allFilterV5Part:{}", allFilterV5Part);
                    DomainObject singleObj = DomainObject.newInstance(context, singleId);
                    String[] single = new String[]{singleId};
                    Map<String, StringList> mapSingleList = getSinglewholeV5Data(context, single);//单车引用的V5数据
                    logger.info("mapSingleList:{}",mapSingleList);
                    StringList singleList = mapSingleList.get("V5List");
                    StringList gcSuperlist = mapSingleList.get("gcSuperlist");
                    StringList gcSigleIdlist = mapSingleList.get("gcSigleIdlist");
                    StringList disConnectList = singleList.copyOf();
                    disConnectList.removeAll(allFilterV5Part);//需要断开关系的数据---多余的数据
                    logger.info("disConnectList:{}", disConnectList);
                    for (int a = 0; a < disConnectList.size(); a++) {
                        if (mapSingleList.containsKey(disConnectList.get(a))) {
                            //断开关系
                            StringList relIdList = mapSingleList.get(disConnectList.get(a));
                            for (int b = 0; b < relIdList.size(); b++) {
                                MqlUtil.mqlCommand(context, false, "del connection '" + relIdList.get(b) + "' ", true);
                            }
                        }
                    }
                    //需要关联到单椅上面的V5数据
                    allFilterV5Part.removeAll(singleList);//结算多出来的V5数据---需要关联到对应的整椅下面
                    logger.info("allFilterV5Part:{}", allFilterV5Part);
                    if (type.equalsIgnoreCase("C")) {
                        for (int a = 0; a < allFilterV5Part.size(); a++) {
                            conectInstance(context, singleObj, allFilterV5Part.get(a));
                        }
                    } else {
                        DomainObject v5gcObj = DomainObject.newInstance(context);
                        for (int a = 0; a < allFilterV5Part.size(); a++) {
                            String addId = allFilterV5Part.get(a);
                            for (String key : gcMap.keySet()) {
                                if (gcMap.get(key).contains(addId)) {
                                    //找到对应的GC，增加一级件
                                    //在通过JF_VPMReference.JF_copySorce，找到单车上面的整椅件
                                    int indexof = gcSuperlist.indexOf(key);
                                    v5gcObj.setId(gcSigleIdlist.get(indexof));
                                    conectInstance(context, v5gcObj, addId);
                                    break;
//                            logger.info("单车GCID:{}",gcSigleIdlist.get(indexof));
                                }
                            }
                        }
                    }
//GC挂零件，需要验证下

                } else {//走新建逻辑


                    if (gcList.size() > 0) {
                        String gxId = "";
                        DomainObject copygx = new DomainObject();
                        if ("X".equalsIgnoreCase(type)) {
                            gxId = gcList.get(0);//单独创建GX类型的数据
                            gcList.remove(0);

                            Map createGCMap = new HashMap();
                            createGCMap.put("HttpServletRequest", req);
                            createGCMap.put("CreateQuanlity", String.valueOf("1"));
                            createGCMap.put("type", "X");
                            String[] createGC = JPO.packArgs(createGCMap);
                            JF_Trim_mxJPO trim = new JF_Trim_mxJPO();
                            jakarta.json.JsonArray gxArrayList = trim.BatchCreateGC(context, createGC);
                            Map temp = rootObj.getInfo(context, oldAttribute);
                            temp.remove("type");
                            temp.remove("id");
                            temp.put(attr_V_DerivedFrom, gxId);//记录复制来源
                            temp.put(attr_JF_copySorce, gxId);//记录复制来源
                            logger.info("temp:{}", temp);
                            temp = changeMap(context, temp);
                            logger.info("temp:{}", temp);
                            if (gxArrayList.size() > 0) {
                                JsonObject jsonValue = gxArrayList.getJsonObject(0);
                                String gxidtem = getPhyId(jsonValue);
                                createList.add(gxidtem);
                                createRootId = gxidtem;
                                copygx.setId(gxidtem);
                                copygx.setAttributeValues(context, temp);//复制原有的基础信息
                                //如果和项目有关系的话，需要建立关系并且设置为Y
                                conectProject(context, copygx, projectId);
                                if (v5Map.containsKey(rootId)) {
                                    StringList rootV5List = v5Map.get(rootId);
                                    for (int e = 0; e < rootV5List.size(); e++) {
                                        conectInstance(context, copygx, rootV5List.get(e), filterList);
                                    }
                                }

                            }
                        }
                        Map createGCMap = new HashMap();
                        createGCMap.put("HttpServletRequest", req);
                        createGCMap.put("CreateQuanlity", String.valueOf(gcList.size()));
                        createGCMap.put("type", "C");
                        String[] createGC = JPO.packArgs(createGCMap);
                        JF_Trim_mxJPO trim = new JF_Trim_mxJPO();
                        jakarta.json.JsonArray gcArrayList = trim.BatchCreateGC(context, createGC);
                        logger.info("gcArrayList:{}", gcArrayList);
                        //拿到创建之后的数据，更新属性信息
                        DomainObject newGC = DomainObject.newInstance(context);
                        DomainObject oldGC = DomainObject.newInstance(context);
                        if (gcArrayList.size() == gcList.size()) {//创建的数量和需要创建的数量匹配
                            for (int j = 0; j < gcArrayList.size(); j++) {
                                oldGC.setId(gcList.get(j));
                                String newGCID = getPhyId(gcArrayList.getJsonObject(j));
                                createList.add(newGCID);
                                newGC.setId(newGCID);//不确定是否需要自动关联项目
                                if (!type.equalsIgnoreCase("X")) {
                                    createRootId = newGCID;
                                }
                                Map temp = oldGC.getInfo(context, oldAttribute);
                                temp.put(attr_V_DerivedFrom, gcList.get(j));//记录复制来源
                                temp.put(attr_JF_copySorce, gcList.get(j));//记录复制来源
                                temp.remove("type");
                                temp.remove("id");
                                temp = changeMap(context, temp);
                                newGC.setAttributeValues(context, temp);//复制原有的基础信息
                                conectProject(context, newGC, projectId);
                                //关联引用的一级件
                                logger.info("gcList.get(j):{}", gcList.get(j));
                                logger.info("gcMap:{}", gcMap);
                                //v5一级件  value GC
                                if (gcMap.containsKey(gcList.get(j))) {
                                    StringList v5List = gcMap.get(gcList.get(j));
                                    logger.info("v5List:{}", v5List);
                                    for (int k = 0; k < v5List.size(); k++) {
                                        //这个地方需要结合结算出来的BOM，来判断是否需要建立关系
                                        conectInstance(context, newGC, v5List.get(k), filterList);
                                    }
                                }
                                //gx关联GC
                                if ("X".equalsIgnoreCase(type)) {
                                    filterList.add(newGCID);//所有的JC按理需要全部挂上
                                    conectInstance(context, copygx, newGCID, filterList);
                                }
                            }
                        } else {
                            logger.error("创建GC的数量不一致");
                        }
                    }
                    //PC和根节点建立关系
                    logger.info("根节点:{}", createRootId);
                    ContextUtil.pushContext(context);
                    ispush = true;
                    DomainRelationship rootship = pcObj.addToObject(context, new RelationshipType(REL_JFPC2EBOM), createRootId);
                    ContextUtil.popContext(context);
                    ispush = false;
                }

            }
            logger.info("createSingleBOM end");
        } catch (Exception e) {
            e.printStackTrace();
            flag = false;
//            ContextUtil.abortTransaction(context);
            //创建出错,删除数据
            for (int i = 0; i < createList.size(); i++) {
                String mql = "del bus '" + createList.get(i) + "'";
                MqlUtil.mqlCommand(context, false, mql, true);
            }

        } finally {
            if (ispush) {
                ContextUtil.popContext(context);
            }
//            ContextUtil.commitTransaction(context);
        }
        return flag;
    }


    /*
     * @description: 通过VPMReference获取关联的所有Model以及Model Year下面所有的PC
     * @author: caipan
     * @date:
     * @param: * @param[1] context
     * @param[2] args
     * @return:
     **/
    public MapList getAllPC(Context context, String[] args) throws Exception{
        Map map = JPO.unpackArgs(args);
        String objectId = (String) map.get("objectId");
        DomainObject obj = new DomainObject(objectId);
        MapList pcList = new MapList();
        if(UIUtil.isNullOrEmpty(obj.getInfo(context, SELECT_ATTR_V_CADOrigin))){//V6 才可以用下面的API,V5 会报错
        IConfiguredEntitiesFactory entitiesFactory = ConfiguredEntitiesFactory.getConfiguredEntitiesFactory(context);
        IConfiguredEntity configuredEntity = entitiesFactory.CreateConfiguredEntity(context, objectId);
        //id集合
        List<String> modelList = configuredEntity.getAttachedModels(context);//获取Model
       logger.info("根据数模查询模型:{} ", modelList);
       DomainObject modelObj = DomainObject.newInstance(context);
       DomainObject modelYearObj = DomainObject.newInstance(context);
       StringList selList = JF_Util_mxJPO.basicBolistSel();
        selList.add("attribute[Marketing Name]" );
        //add by chenyan 添加PC关联的EBOM信息避免二次查询
        selList.add(SELECT_PC_CONNECTION_PC);
        selList.add(SELECT_PC_CONNECTION_PC_ID);
        selList.add(SELECT_PC_CONNECTION_PC_NAME);
        // add by chenyan 配置的EBOM需要展示GX下面的GC而不是GX 2025/01/08
        selList.add(SELECT_PC_CONNECTION_PC_PART_TYPE);
//        selList.add(SELECT_PC_CONNECTION_PC_2);
//        selList.add(SELECT_PC_CONNECTION_PC_NAME_2);
//        selList.add(SELECT_PC_CONNECTION_PC_ID_2);
       StringList relList = JF_Util_mxJPO.basicRellistSel();
       String modelYearId ="";
       //GX展开一级件
       StringList typeSelectList = JF_Util_mxJPO.basicBolistSel();
       typeSelectList.add(SELECT_ATTR_V_PART_NUMBER);
       for(int i=0;i<modelList.size();i++) {
           String modelId = modelList.get(i);
           modelObj.setId(modelId);
           MapList modelYear = modelObj.getRelatedObjects(context, REL_MainProduct + "," + REL_Products, type_Products, selList, relList, false, true, (short) 1, null, null, 0);
           for (int j = 0; j < modelYear.size(); j++) {
               Map temp = (Map) modelYear.get(j);
               modelYearId = UIUtil.getValue(temp, SELECT_ID);//ModelYear ID
               modelYearObj.setId(modelYearId);
              MapList list = modelYearObj.getRelatedObjects(context, REL_ProductConfiguration, type_ProductConfiguration, selList, relList, false, true, (short) 1, null, null, 0);
                for(int k=0;k<list.size();k++){
                    Map pcMap = (Map)list.get(k);
                    String strGXId = (String) pcMap.get(SELECT_PC_CONNECTION_PC_ID);
                    //add by chenyan 新增获取GC逻辑
                    String strPartType = (String) pcMap.get(SELECT_PC_CONNECTION_PC_PART_TYPE);
                    if ("X".equalsIgnoreCase(strPartType)){
                        DomainObject GX = DomainObject.newInstance(context, strGXId);
                        MapList maps = GX.getRelatedObjects(context, VPMInstance , // relationship pattern
                                "VPMReference",                                    // object pattern
                                typeSelectList,                            // object selects
                                JF_Util_mxJPO.basicRellistSel(), // relationship selects
                                false,                                        // to direction
                                true,                                        // from direction
                                (short) 1,                                    // recursion level
                                "attribute[JF_VPMReference.JF_PartType]=='C'",                // object where clause
                                "",
                                (short) 0);
                        logger.info("maps:{}",maps);
                        // 只取一个GC
                        String strPCName = "";
                        if (maps.size() > 0){
                            Map GCMap = (Map) maps.get(0);
                            strPCName = (String) GCMap.get(SELECT_ATTR_V_PART_NUMBER);
                            String strGCId = (String) GCMap.get(SELECT_ID);
                            if (UIUtil.isNullOrEmpty(strPCName)){
                                strPCName = (String) GCMap.get(SELECT_NAME);
                            }
                            pcMap.put(SELECT_PC_CONNECTION_PC_ID,strGCId);
                        }
                        pcMap.put(SELECT_PC_CONNECTION_PC,strPCName);
                    }
                    String current = UIUtil.getValue(pcMap, DomainConstants.SELECT_CURRENT);
                    if(!vpmReleased.equalsIgnoreCase(current)&&!vpmFROZEN.equalsIgnoreCase(current)){//只有冻结或者发布状态可以创建单车BOM
                        pcMap.put("disableSelection", "true");
                    }
                    pcList.add(pcMap);
                }
           }
       }
        }
        return pcList;

    }
    /*
     * @description:创建EBOM按钮是否显示
     * @author: caipan
     * @date:
     * @param: * @param[1] context
     * @param[2] args
     * @return:
     **/
    public boolean isShowCreateEBOM(Context context,String[] args) throws Exception{
        Map request = JPO.unpackArgs(args);
        String objectId = (String)request.get("objectId");
        StringList selList = JF_Util_mxJPO.basicBolistSel();
        selList.add(SELECT_ATTR_V_CADOrigin);
        selList.add(SELECT_ATTR_JFPartType);
        DomainObject rootObj = DomainObject.newInstance(context);
        rootObj.setId(objectId);
        Map map = rootObj.getInfo(context, selList);
        String cadtype = UIUtil.getValue(map,SELECT_ATTR_V_CADOrigin);
        logger.info("cadtype:{}",cadtype);
        logger.info("map:{}",map);
        String partType = UIUtil.getValue(map,SELECT_ATTR_JFPartType);
        if(UIUtil.isNullOrEmpty(cadtype)) {
            IConfiguredEntitiesFactory entitiesFactoryroot = ConfiguredEntitiesFactory.getConfiguredEntitiesFactory(context);
            IConfiguredEntity configuredEntityroot = entitiesFactoryroot.CreateConfiguredEntity(context, objectId);
            List<String> modelListroot = configuredEntityroot.getAttachedModels(context);//获取Model
            //V6总成，类型是GX、GC，没有父或者父没有关联Model该按钮显示
            if (modelListroot.size() > 0  && (partType.equalsIgnoreCase("X") || partType.equalsIgnoreCase("C"))) {
                StringList parentList = rootObj.getInfoList(context, "to[VPMInstance].from.id");
                if (parentList.size() == 0) {
                    return true;
                } else {
                    for (int i = 0; i < parentList.size(); i++) {
                        IConfiguredEntitiesFactory entitiesFactory = ConfiguredEntitiesFactory.getConfiguredEntitiesFactory(context);
                        IConfiguredEntity configuredEntity = entitiesFactory.CreateConfiguredEntity(context, parentList.get(i));
                        List<String> modelList = configuredEntity.getAttachedModels(context);//获取Model
                        if (modelList.size() == 0) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }
    public String getParentId(Context context,String id) throws Exception {
        StringList selList = JF_Util_mxJPO.basicBolistSel();
        selList.add(SELECT_ATTR_JF_PartType);
        StringList relList = JF_Util_mxJPO.basicRellistSel();
        DomainObject obj = DomainObject.newInstance(context);
        obj.setId(id);
        String where = SELECT_ATTR_JF_PartType + "==C";
        MapList list = obj.getRelatedObjects(context, REL_Instance, TYPE_VPMReference, selList, relList, true, false, (short) 1, where, null, 1);
        if (list.size() > 0) {
            Map temp = (Map) list.get(0);
            return UIUtil.getValue(temp, DomainConstants.SELECT_ID);
        } else {
            String type = obj.getInfo(context, DomainConstants.SELECT_TYPE);
            if ("X".equalsIgnoreCase(type) || "C".equalsIgnoreCase(type)) {
                return "";
            } else {//功能件特殊情况 有两层V6虚拟层
                list = obj.getRelatedObjects(context, REL_Instance, TYPE_VPMReference, selList, relList, true, false, (short) 2, null, null, 0);
                for (int i = list.size() - 1; i > -1; i--) {
                    Map temp = (Map) list.get(i);
                    String parentType = UIUtil.getValue(temp, SELECT_ATTR_JF_PartType);
                    if ("X".equalsIgnoreCase(parentType) || "C".equalsIgnoreCase(parentType)) {
                        return UIUtil.getValue(temp, DomainConstants.SELECT_ID);
                    }
                }
            }
        }
        return "";
    }
    public String getParentIdbak(Context context,String id) throws Exception{
        StringList selList = JF_Util_mxJPO.basicBolistSel();
        StringList relList = JF_Util_mxJPO.basicRellistSel();
        DomainObject obj = DomainObject.newInstance(context);
        obj.setId(id);
        String where =SELECT_ATTR_JF_PartType+"==C";
        logger.info("where:{}",where);
        MapList list =  obj.getRelatedObjects(context, REL_Instance, TYPE_VPMReference, selList, relList, true, false, (short) 1, where, null, 1);
        if(list.size()>0){
            Map temp = (Map)list.get(0);
            return UIUtil.getValue(temp, DomainConstants.SELECT_ID);
        }else{
            return "";
        }
    }

    public Map changeMap(Context context,Map<String,String> map) {
        Map<String, String> resultMap = new HashMap<>();
        for (String str : map.keySet()) {
            if (str.contains(ATTR_JF_PartNameCN)) {
                resultMap.put(ATTR_JF_PartNameCN, map.get(str));
            } else if (str.contains(ATTR_JF_PartNameEN)) {
                resultMap.put(ATTR_JF_PartNameEN, map.get(str));
            } else if (str.contains(ATTR_JF_ProcurementType)) {
                resultMap.put(ATTR_JF_ProcurementType, map.get(str));
            } else if (str.contains(ATTR_JFDIRECT_BUY)) {
                resultMap.put(ATTR_JFDIRECT_BUY, map.get(str));
            }else if (str.contains(attr_V_DerivedFrom)) {
                resultMap.put(attr_V_DerivedFrom, map.get(str));
            }else if (str.contains(attr_JF_copySorce)) {
                resultMap.put(attr_JF_copySorce, map.get(str));
            }else if (str.contains(attr_JF_ISWholeChair)) {
                resultMap.put(attr_JF_ISWholeChair, "WholeChair");
            }
        }
        return resultMap;
    }

    /*
     * @description:解析创建出来的物理ID
     * @author: caipan
     * @date:
     * @param: * @param[1] jsonObject
     * @return:
     **/
    public String getPhyId(JsonObject jsonObject){
        return  jsonObject.get("physicalid").toString().replace("\"","");
    }
    /*
     * @description: 建立Instance关系，并且设置Interface
     * @author: caipan
     * @date:
     * @param: * @param[1] context
     * @param[2] copygx
     * @param[3] toId
     * @return:
     **/
    public void  conectInstance(Context context ,DomainObject copygx,String toId,StringList filterListVpm){
        try {
            JF_ProcessExcel_mxJPO process = new JF_ProcessExcel_mxJPO();
            if(filterListVpm.contains(toId)) {
                DomainRelationship rootship = copygx.addToObject(context, new RelationshipType(JF_PLMConstants_mxJPO.REL_Instance), toId);
                process.addInterface(context, rootship.getPhysicalId(context));
                rootship.setAttributeValue(context, attr_JF_QuoteData, "Y");
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    /*
     * @description:整椅关联项目
     * @author: caipan
     * @date:
     * @param: * @param[1] context
     * @param[2] copygx
     * @param[3] toId
     * @param[4] filterListVpm
     * @return:
     **/
    public void  conectProject(Context context ,DomainObject copygx,String fromID){
        try {
            ContextUtil.pushContext(context);
            if(UIUtil.isNotNullAndNotEmpty(fromID)) {
                JF_ProcessExcel_mxJPO process = new JF_ProcessExcel_mxJPO();
                DomainRelationship rootship = copygx.addFromObject(context, new RelationshipType("JFProject2RootPart"), fromID);
                rootship.setAttributeValue(context, "JFZeroPart", "Y");

            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            try {
                ContextUtil.popContext(context);
            } catch (FrameworkException e) {
                throw new RuntimeException(e);
            }
        }
    }
    public void  conectInstance(Context context ,DomainObject copygx,String toId){
        try {
            ContextUtil.pushContext(context);
            if(UIUtil.isNotNullAndNotEmpty(toId)) {
                JF_ProcessExcel_mxJPO process = new JF_ProcessExcel_mxJPO();
                DomainRelationship rootship = copygx.addToObject(context, new RelationshipType(JF_PLMConstants_mxJPO.REL_Instance), toId);
                process.addInterface(context, rootship.getPhysicalId(context));
                rootship.setAttributeValue(context, attr_JF_QuoteData, "Y");
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            try {
                ContextUtil.popContext(context);
            } catch (FrameworkException e) {
                throw new RuntimeException(e);
            }
        }
    }
    /*
     * @description:获取结算出来的单车BOM，引用的V5数据
     * @author: caipan
     * @date:
     * @param: * @param[1] context
     * @param[2] args
     * @return:
     **/
    public Map<String,StringList> getSinglewholeV5Data(Context context, String[] args) throws FrameworkException {
        int level = 2;
        Map<String, StringList> v5MapList = new HashMap();
        StringList list = new StringList();
        StringList gcIdlist = new StringList();
        try {
            ContextUtil.pushContext(context);
            DomainObject Wholechair = DomainObject.newInstance(context);
            String wholeId = args[0];

            Wholechair.setId(wholeId);
            StringList selList = JF_Util_mxJPO.basicBolistSel();
            selList.add(SELECT_ATTR_V_CADOrigin);
            selList.add(SELECT_ATTR_JFPartType);
            selList.add(select_attr_JF_copySorce);
            selList.add(select_attr_JF_ISWholeChair);
            StringList relList = JF_Util_mxJPO.basicRellistSel();
            relList.add("from." + SELECT_ATTR_V_CADOrigin);
            relList.add("from." + SELECT_ID);
            relList.add("from." + SELECT_ATTR_JFPartType);
            relList.add("from." + select_attr_JF_copySorce);
            String type = Wholechair.getInfo(context, SELECT_ATTR_JFPartType);
            String rootCopyId = Wholechair.getInfo(context, select_attr_JF_copySorce);
            String relWhere = select_attr_JF_QuoteData+"==Y";
            if (type.equalsIgnoreCase("C")) {
                level = 1;
            }
            MapList allVpmList = Wholechair.getRelatedObjects(context, REL_Instance, TYPE_VPMReference, selList, relList, false, true, (short) level, null, relWhere, 0);
            StringList gcList = new StringList();
            StringList gcIdList = new StringList();
            gcList.add(rootCopyId);
            gcIdList.add(wholeId);
            for(int i=0;i<allVpmList.size();i++){
                StringList v5parentlist = new StringList();
                Map temp = (Map)allVpmList.get(i);
                String id  = UIUtil.getValue(temp, SELECT_ID);
                String fromObjectId  = UIUtil.getValue(temp, ("from." + SELECT_ID));
                String fromId  = UIUtil.getValue(temp, ("from." + select_attr_JF_copySorce));
                String copySourceId  = UIUtil.getValue(temp, (select_attr_JF_copySorce));
                String totype  = UIUtil.getValue(temp, ( SELECT_ATTR_JFPartType));
                String relId  = UIUtil.getValue(temp, (DomainConstants.SELECT_RELATIONSHIP_ID));
                String JF_ISWholeChair  = UIUtil.getValue(temp, (select_attr_JF_ISWholeChair));
                if(!list.contains(id)&&(!JF_ISWholeChair.equalsIgnoreCase("WholeChair"))){
                    list.add(id);
                    v5parentlist.add(relId);
                    v5MapList.put(id,v5parentlist);//v5一级件的关系ID ，断开关系的时候使用
                }
                if(!UIUtil.isNullOrEmpty(copySourceId)){
                    if(!gcList.contains(copySourceId)){
                        gcList.add(copySourceId);
                        gcIdList.add(id);
                    }
                }
            }
            v5MapList.put("V5List",list);
            v5MapList.put("gcSuperlist",gcList);
            v5MapList.put("gcSigleIdlist",gcIdList);
        } catch (Exception e) {

        } finally {
            ContextUtil.popContext(context);
        }
        return v5MapList;
    }


    public boolean  getFilterV5Data(Context context ,String toId,StringList filterListVpm){
        try {
            if(filterListVpm.contains(toId)) {
                return true;
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return false;
    }
    /*
     * @description:复制Map
     * @author: caipan
     * @date:
     * @param: * @param[1] src
     * @return:
     **/
    public static <E,T> Map<E,T> deepCopy(Map<E,T> src) {
        try {
            ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
            ObjectOutputStream out = new ObjectOutputStream(byteOut);
            out.writeObject(src);

            ByteArrayInputStream byteIn = new ByteArrayInputStream(byteOut.toByteArray());
            ObjectInputStream in = new ObjectInputStream(byteIn);
            @SuppressWarnings("unchecked")
            Map<E, T>  dest = (Map<E,T> ) in.readObject();
            return dest;
        } catch (Exception e) {
            e.printStackTrace();
            return new HashMap<E,T>();
        }
    }
    /*
     * @description:获取最新发布版本
     * @author: caipan
     * @date:
     * @param: * @param[1] context
     * @param[2] args
     * @return:
     **/
    public StringList getLasterRelease(Context context, String[] args){
        StringList resultList = new StringList();
        try {
            Map argMaps = JPO.unpackArgs(args);
            MapList argMap = (MapList) argMaps.get("objectList");
            Iterator iterator = argMap.iterator();
            while (iterator.hasNext()){
                Map map = (Map) iterator.next();
                String objectId = (String) map.get("id");
                StringBuffer buffer = new StringBuffer();
                DomainObject object = DomainObject.newInstance(context, objectId);
                String singleId = object.getInfo(context, "from[" + REL_JFPC2EBOM + "].to.id");
                if(UIUtil.isNotNullAndNotEmpty(singleId)) {
                    String lasterId = JF_Util_mxJPO.getLastReleasedMajorid(context, singleId);//最新发布版本
                    logger.info("lasterId:{}",lasterId);
                    if (UIUtil.isNotNullAndNotEmpty(lasterId)) {
                        singleId = lasterId;
                        buffer.append("<table><td><a href=\"/3dspace/common/emxNavigator.jsp?objectId=");
                        String name = "";
                        object.setId(singleId);
                        String folderId = singleId;
                        buffer.append(folderId).append("\" target=\"_blank>\">");
                        String folderName = object.getInfo(context, DomainConstants.SELECT_REVISION);
                        folderName = StringEscapeUtils.escapeHtml4(folderName);
                        buffer.append(folderName);
                        buffer.append("</a></td></table>");
                        resultList.add(buffer.toString());
                    }else{
                        resultList.add("");
                    }
                }else{
                    resultList.add("");
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return resultList;
    }
  }
