import com.dassault_systemes.createcontent.ENONewAdaptor;
import com.dassault_systemes.createcontent.ErrorMngt.ENONewException;
import com.dassault_systemes.createcontent.models.ENONewOptions;
import com.dassault_systemes.smasds.powerby.services.util.ServiceUtils;
import com.dscn.plm.util.NioJDUtils;
import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.DomainRelationship;
import com.matrixone.apps.domain.util.*;

import com.matrixone.apps.framework.ui.UIUtil;
import ds.enovia.apps.msf.BusinessObjects.ExecuteJPO.args;
import jakarta.json.*;
import matrix.db.Access;
import matrix.db.Context;
import matrix.db.JPO;
import matrix.db.RelationshipType;
import matrix.util.StringList;
import org.apache.commons.io.FileUtils;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.*;

public class JF_Trim_mxJPO {
    private static final Logger LOGGER = LoggerFactory.getLogger(JF_Trim_mxJPO.class);
    public String TYPE_JFDATATRANSMISSION = "type_JFPartNumberG";
    /**
     * @description: 创建 样板件、卷料
     * @param: context
    args
     * @return: java.lang.String
     * @author JJS
     * @date:  15:27
     */
    public Map createTrim(Context context,String[] args)throws Exception{
        Map paramMap = JPO.unpackArgs(args);
//        LOGGER.info("paramMap{}",paramMap);
        Map<String,String> map = new HashMap<>();
        String templateId = NioJDUtils.getPageStr(context, "JF_Trim.Template.id");
        //查询当前对象对该模版的权限，如果有编辑权限就跳过
        DomainObject doObject = DomainObject.newInstance(context, templateId);
        Access accessMask = doObject.getAccessMask(context);
        boolean bModify = accessMask.hasModifyAccess();
        if(!bModify){
            //赋予权限
            String username = context.getUser()+"_PRJ";
            String mql = "mod bus "+templateId+" add ownership - "+username+" as read,show,modify,changesov;  ";
            LOGGER.info("mql:{}",mql);
            MqlUtil.mqlCommand(context,true,mql,true  );
        }
        map.put("id",templateId);//需要替换一个已经存在的对象ID
        return map;
    }

    /*
     * @description: 批量创建样板件
     * @author: caipan
     * @date:
     * @param: * @param[1] context
     * @param[2] args
     * @return:
     **/
    public boolean BatchCreateTrim(Context context,String[] args) throws Exception, ENONewException {
        Map temp = JPO.unpackArgs(args);
        boolean flag = true;
        Boolean isPush = false;
        try {
            context.start(true);
            HttpServletRequest request = (HttpServletRequest) temp.get("HttpServletRequest");
            String CreateQuanlity = UIUtil.getValue(temp, "CreateQuanlity");
            String JFProjectNameOID = UIUtil.getValue(temp, "JFProjectNameOID");
            String JFPartitionChooserCmdOID = UIUtil.getValue(temp, "JFPartitionChooserCmdOID");
            if(UIUtil.isNotNullAndNotEmpty(JFPartitionChooserCmdOID)) {
                Map map = getLibPartCode(context, JFPartitionChooserCmdOID);
                temp.put("JFPartitionChooserDisplay", UIUtil.getValue(temp, "JF_PartNameCN"));
                temp.put("JFPartitionChooserValue",UIUtil.getValue(temp, "JF_PartNameEN"));
            }
            //关联项目
            Context var11 = context;
            JsonArray var81 = null;
            ENONewOptions var14 = new ENONewOptions();
            var14.setParsedResult(true);//true
            HashMap var87 = new HashMap();
            var87.put("activeFolder", false);
            var87.put("typeName", "assembly");
            var14.setParam(var87);
//            var14.setSecurityContext("ctx::VPLMProjectLeader.JFCompany.JFSeat");//设置上下文
            ENONewAdaptor var88 = new ENONewAdaptor(var11);
            var88.set_httpSession(request.getSession());//request
            JsonObjectBuilder objBuilder = Json.createObjectBuilder();
            objBuilder.add("type", "VPMReference");
            int num = Integer.parseInt(CreateQuanlity);
//            LOGGER.info("build JSON start:{}",temp);
            JsonArray var84 = getJson(context, args, num,temp);
//            LOGGER.info("json:{}", var84);
            var81 = var88.createObjectForMultiple(var84, var14);//拿到结果之后设置值 或者直接把值写到JSON 里面去
            LOGGER.info("var81:{}", var81);
            LOGGER.info("JFProjectNameOID:{}", JFProjectNameOID);
            StringList connId = new StringList();
            if (var81.size() > 0 ) {
                JF_Config_mxJPO jfConfigMxJPO = new JF_Config_mxJPO();
                for (int j = 0; j < var81.size(); j++) {
                    String newID = jfConfigMxJPO.getPhyId(var81.getJsonObject(j));
                    connId.add(newID);
                }
            }
            ContextUtil.pushContext(context);
            isPush = true;
            if(UIUtil.isNotNullAndNotEmpty(JFProjectNameOID)){
                //update by  ljr  批量创建样板件功能时选择了项目归属，数据关联了对应项目，但是关联项目的属性字段仍为空
                DomainObject domainObjectPs = DomainObject.newInstance(context, JFProjectNameOID);
                DomainObject domainObject = DomainObject.newInstance(context);
                String desc = domainObjectPs.getDescription(context);
                for (int i = 0; i < connId.size(); i++) {
                    String id = connId.get(i) ;
                    DomainRelationship domainRelationship = domainObjectPs.addToObject(context, new RelationshipType("JFProject2RootPart"), id);
                    domainRelationship.setAttributeValue(context, JF_PLMConstants_mxJPO.ATTR_JF_BelongPart, "Y");
                    domainObject.setId(id);
                    domainObject.setAttributeValue(context, JF_PLMConstants_mxJPO.ATTR_JF_ProjectRel, desc);
                }
                //end
            }
            //入库
            //先查询到库，然后在关联库
            MapList list = DomainObject.findObjects(context, "General Class","*","attribute[JF_PartSubType]==T02",new StringList(DomainConstants.SELECT_ID));
            String classId ="";
            if(list.size()>0){
                classId = (String)((Map)(list.get(0))).get(DomainConstants.SELECT_ID);
            }
            LOGGER.info("calssId:{} list:{}",classId,list);
            if(UIUtil.isNotNullAndNotEmpty(classId)) {
                    DomainObject domainObjectPs = DomainObject.newInstance(context, classId);
                    domainObjectPs.addRelatedObjects(context, new RelationshipType("Classified Item"), true ,connId.toStringArray());
            }
        }catch (Exception e){
            e.printStackTrace();
            flag= false;
        }finally {
            if (isPush) {
                ContextUtil.popContext(context);
            }
            if(context.isTransactionActive()){
                context.commit();
            }
        }
        return flag;
    }



    /*
     * @description: 创建零件公共方法
     * @author: liujr
     * @date:
     * @param: * @param[1] context
     * @param[2] args
     * @return:
     **/
    public String createTrimPublic(Context context,String[] args) throws Exception, ENONewException {
        Map temp = JPO.unpackArgs(args);
        String newId = DomainConstants.EMPTY_STRING;
        try {
            HttpServletRequest request = (HttpServletRequest) temp.get("HttpServletRequest");
            Context var11 = context;
            JsonArray var81 = null;
            ENONewOptions var14 = new ENONewOptions();
            var14.setParsedResult(true);//true
            HashMap var87 = new HashMap();
            var87.put("activeFolder", false);
            var87.put("typeName", "assembly");
            var14.setParam(var87);
            ENONewAdaptor var88 = new ENONewAdaptor(var11);
            var88.set_httpSession(request.getSession());//request
            JsonObjectBuilder objBuilder = Json.createObjectBuilder();
            objBuilder.add("type", "VPMReference");
            //默认为一个
            JsonArray var84 = getPublicCreatePartJson(context, temp);
            var81 = var88.createObjectForMultiple(var84, var14);//拿到结果之后设置值 或者直接把值写到JSON 里面去
            if (var81.size() > 0) {
                JF_Config_mxJPO jfConfigMxJPO = new JF_Config_mxJPO();
                newId = jfConfigMxJPO.getPhyId(var81.getJsonObject(0));
            }
        }catch (Exception e){
            e.printStackTrace();
            throw e;
        }
        return newId;
    }

    /**
    * 获取样板包的原材料
    * @param context
    	* @param args
    * @author LIUJR
    * @throws
    * @return com.matrixone.apps.domain.util.MapList
    * @date 2025/3/25 10:16
    * @description
    */
    public MapList getBomT06MaterialList(Context context, String[] args) throws Exception{
        try {
            StringList busSelectsList = JF_Util_mxJPO.basicBolistSel();
            Map paramsMap = (Map) JPO.unpackArgs(args);
            ContextUtil.pushContext(context);
            String strObjectId = (String) paramsMap.get(JF_PLMConstants_mxJPO.STRING_OBJECTID);
            DomainObject objectPart = DomainObject.newInstance(context, strObjectId);
            StringList trimPartIdList = objectPart.getInfoList(context, "from[JFRelPart2Raw].to.id");
            MapList mlPartInfoList = DomainObject.getInfo(context, trimPartIdList.toStringArray(), busSelectsList);
            return mlPartInfoList;
        }catch (Exception e) {
            e.printStackTrace();
            ContextUtil.popContext(context);
        }
        return new MapList();
    }

    /**
    * 获取所有的面料件
    * @param context
	* @param args
    * @author LIUJR
    * @throws
    * @return matrix.util.StringList
    * @date 2025/3/25 10:47
    * @description
    */
    public StringList getAllTrimPartList(Context context, String[] args) throws Exception {
        StringList result = new StringList();
        try {
            ContextUtil.pushContext(context);
            String where = "attribute[JF_VPMReference.JF_PartType]==T&&attribute[JF_VPMReference.JF_PartSubType]==T03";
            MapList CPartMapList = DomainObject.findObjects(context, JF_PLMConstants_mxJPO.TYPE_VPMReference, "*", where, new StringList("id"));
            for (int j = 0; j < CPartMapList.size(); j++) {
                Map CMap = (Map) CPartMapList.get(j);
                String cId = (String) CMap.get(DomainConstants.SELECT_ID);
                result.add(cId);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            ContextUtil.popContext(context);
        }
        return result;
    }

    /**
    * 排除面套下样板包关联了的面料件
    * @param context
	* @param args
    * @author LIUJR
    * @throws
    * @return matrix.util.StringList
    * @date 2025/3/25 10:48
    * @description
    */
    public StringList getPartT06ExcludeVPMList(Context context,String[] args)throws Exception{
        StringList result = new StringList();
        try {
            result = new StringList();
            Map paramMap = (Map) JPO.unpackArgs(args);
            String objectId = (String)paramMap.get("objectId");
            StringList busSelectsList = JF_Util_mxJPO.basicBolistSel();
            StringList basicRellistSel = JF_Util_mxJPO.basicRellistSel();
            ContextUtil.pushContext(context);
            DomainObject partT06 = DomainObject.newInstance(context, objectId);
            String trimId = partT06.getInfo(context, "to[VPMInstance].from.id");
            if (UIUtil.isNullOrEmpty(trimId)) {
                trimId = partT06.getInfo(context, "from[JFRelPart2Raw].to.id");
                if (UIUtil.isNullOrEmpty(trimId)) {
                    return result;
                } else {
                    return StringList.create(trimId);
                }
            } else {
                DomainObject trimObject = DomainObject.newInstance(context, trimId);
                MapList mapList = trimObject.getRelatedObjects(
                        context,
                        JF_PLMConstants_mxJPO.REL_Instance,
                        JF_PLMConstants_mxJPO.TYPE_VPMReference,
                        busSelectsList,
                        basicRellistSel,
                        false,
                        true,
                        (short) 1,
                        "attribute[JF_VPMReference.JF_PartType]==T&&attribute[JF_VPMReference.JF_PartSubType]==T06",
                        "",
                        0
                );
                Iterator iterator = mapList.iterator();
                while (iterator.hasNext()) {
                    Map map = (Map) iterator.next();
                    String id = UIUtil.getValue(map, DomainConstants.SELECT_ID);
                    DomainObject domainObject = DomainObject.newInstance(context, id);
                    StringList infoList = domainObject.getInfoList(context, "from[JFRelPart2Raw].to.id");
                    result.addAll(infoList);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            ContextUtil.popContext(context);
        }
        return result;
    }

    /**
    * 创建新的零件的方法  属性来源复制其存在的对象属性
    * @param context
	* @param temp
    * @author LIUJR
    * @throws
    * @return jakarta.json.JsonArray
    * @date 2025/2/27 10:40
    * @description
    */
    public  JsonArray getPublicCreatePartJson(Context context, Map temp) throws Exception{
        StringList createList = new StringList();
        JF_PublicMethodClass_mxJPO publicMethod = new JF_PublicMethodClass_mxJPO();
        String partName = publicMethod.getName(context, new String[]{});
        createList.add(partName);
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
            //基础属性
            JsonArrayBuilder publicAttributes = Json.createArrayBuilder();
            Map V_Name = setMap("V_Name", "String", UIUtil.getValue(temp, "v_name"));
            V_Name.put("modified",true);
            Map PLM_ExternalID = setMap("PLM_ExternalID", "String", phyName);
            PLM_ExternalID.put("modified", true);
            if (temp.containsKey("description")) {
                Map V_description = setMap("V_description", "String", UIUtil.getValue(temp, "description"));
                V_description.put("modified", true);
                publicAttributes.add(Json.createObjectBuilder(V_description));
            }

            Map policy = setMap("policy", "String", "VPLM_SMB_Definition_MajorRev");
            publicAttributes.add(Json.createObjectBuilder(V_Name));
            publicAttributes.add(Json.createObjectBuilder(PLM_ExternalID));
            publicAttributes.add(Json.createObjectBuilder(policy));
            attributeMap.put("publicAttributes", publicAttributes.build());
            //设置扩展属性
            JsonArrayBuilder extensionAttributes = Json.createArrayBuilder();
            Map JF_PartType = setMapAttribute("JF_PartType", "String", "T");
            Map JF_PartSubType = setMapAttribute("JF_PartSubType", "String", "T06");
            extensionAttributes.add(Json.createObjectBuilder(JF_PartType));
            extensionAttributes.add(Json.createObjectBuilder(JF_PartSubType));
            if (temp.containsKey(JF_PLMConstants_mxJPO.ATTR_JF_Fabric)) {
                Map JF_Fabric = setMapAttribute("JF_Fabric", "String", UIUtil.getValue(temp, JF_PLMConstants_mxJPO.ATTR_JF_Fabric));
                extensionAttributes.add(Json.createObjectBuilder(JF_Fabric));
            }
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
        return rootArray.build();
    }


    public  JsonArray getJson(Context context,String[] args, int CreateQuanlity,Map temp) throws Exception{
        StringList createList = new StringList();
        JF_VPMT_mxJPO jfVpmtMxJPO = JF_VPMT_mxJPO.getInstance(context);
        for(int i=0;i<CreateQuanlity;i++){
            createList.add(jfVpmtMxJPO.autoName(context,null,"T"));
        }
//        createList.add("Physical Product10501010");//后续可以调用编码器、或者生成正式编码
//        createList.add("Physical Product10601011");
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
            Map V_description = setMap("V_description", "String", UIUtil.getValue(temp, "description"));
            V_description.put("modified", true);
            Map policy = setMap("policy", "String", "VPLM_SMB_Definition_MajorRev");
            publicAttributes.add(Json.createObjectBuilder(V_Name));
            publicAttributes.add(Json.createObjectBuilder(PLM_ExternalID));
            publicAttributes.add(Json.createObjectBuilder(V_description));
            publicAttributes.add(Json.createObjectBuilder(policy));
            attributeMap.put("publicAttributes", publicAttributes.build());
            JsonArrayBuilder extensionAttributes = Json.createArrayBuilder();
            Map extensionAttribute = setMapAttribute("JF_ProcurementType", "String", UIUtil.getValue(temp, "JF_ProcurementType"));
            Map JF_DirectBuy = setMapAttribute("JF_DirectBuy", "String", UIUtil.getValue(temp, "JF_DirectBuy"));
//            Map JF_PartNameEN = setMapAttribute("JF_PartNameEN", "String", UIUtil.getValue(temp, "JFPartitionChooserValue"));
//            Map JF_PartNameCN = setMapAttribute("JF_PartNameCN", "String", UIUtil.getValue(temp, "JFPartitionChooserDisplay"));
            Map JF_PartNameCN = setMapAttribute("JF_PartNameCN", "String", UIUtil.getValue(temp, "JF_PartNameCN"));
            Map JF_PartNameEN = setMapAttribute("JF_PartNameEN", "String", UIUtil.getValue(temp, "JF_PartNameEN"));
            Map JF_Detail_CN = setMapAttribute("JF_Detail_CN", "String", UIUtil.getValue(temp, "JF_Detail_CN"));
            Map JF_Detail_EN = setMapAttribute("JF_Detail_EN", "String", UIUtil.getValue(temp, "JF_Detail_EN"));
            Map JF_PartType = setMapAttribute("JF_PartType", "String", "T");
            Map JF_PartSubType = setMapAttribute("JF_PartSubType", "String", "T02");
//            Map V_PartNumber = setMapAttribute("V_PartNumber", "String", phyName);
//            V_PartNumber.put("extension", "EnterpriseExtension");
            extensionAttributes.add(Json.createObjectBuilder(extensionAttribute));
            extensionAttributes.add(Json.createObjectBuilder(JF_DirectBuy));
            extensionAttributes.add(Json.createObjectBuilder(JF_PartNameEN));
            extensionAttributes.add(Json.createObjectBuilder(JF_PartNameCN));
            extensionAttributes.add(Json.createObjectBuilder(JF_Detail_CN));
            extensionAttributes.add(Json.createObjectBuilder(JF_Detail_EN));
            extensionAttributes.add(Json.createObjectBuilder(JF_PartType));
            extensionAttributes.add(Json.createObjectBuilder(JF_PartSubType));
//            extensionAttributes.add(Json.createObjectBuilder(V_PartNumber));
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
    public static Map setMapAttribute (String name,String type,String value){
        Map map = new HashMap();
        map.put("name", name);
        map.put("type", type);
        map.put("value", value);
        map.put("modified", true);
        map.put("isReadOnly", false);
        map.put("extension", "JF_VPMReference");
        map.put("visible", true);
        return map;
    }


    public void getName(Context context,String[] args) throws Exception{
        String type = "VPMReference";//type
        String pak = "PRODUCTCFG";//package
        String var7 = ServiceUtils.generateUniqueID(context, pak, type);
        String var8 = ServiceUtils.generateExternalID(context, type, var7);
        System.out.println("var8 = " + var8);
    }
    /*
     * @description: 获取子类型的样板类型
     * @author: caipan
     * @date:
     * @param: * @param[1] context
     * @param[2] args
     * @return:
     **/
    public HashMap getSubType(Context context, String[] args) throws Exception {
        HashMap rangeMap = new HashMap();
        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        HashMap requestMap = (HashMap) programMap.get("requestMap");
        String sLang = (String) requestMap.get("languageStr");

        if (sLang.contains(","))
            sLang = sLang.substring(0, sLang.indexOf(","));

        StringList options = new StringList(1);
        // IR-211724V6R2014
        options.add("T02");
        rangeMap.put("field_choices", options);
        options = new StringList(1);
        // IR-211724V6R2014
        options.addElement(EnoviaResourceBundle.getProperty(context,"emxFrameworkStringResource", new Locale(sLang),"emxFramework.Range.JF_VPMReference.JF_PartSubType.T02"));
        rangeMap.put("field_display_choices", options);
//        System.out.println(rangeMap.entrySet()+" set");
        return rangeMap;
    }
    /*
     * @description: 获取我的样板件
     * @author: caipan
     * @date:
     * @param: * @param[1] context
     * @param[2] args
     * @return:
     **/
        public MapList getMyTemplatPart(Context context,String[] args) throws Exception{
            String owner = context.getUser();
            String where = "owner=='"+owner+"' && attribute[JF_VPMReference.JF_PartSubType]==T02";
            StringList selList = JF_Util_mxJPO.basicBolistSel();
            selList.add("originated");
            MapList mapList = DomainObject.findObjects(context,JF_PLMConstants_mxJPO.TYPE_VPMReference,"*",where,selList);
            mapList.sort("originated","descending","date");
//            LOGGER.info("mapList:{}",mapList);
            return mapList;
        }


    public JsonArray BatchCreateGC(Context context,String[] args) throws Exception, ENONewException {
            LOGGER.info("BatchCreateGC:{}",context.getUser());
        Map temp = JPO.unpackArgs(args);
        boolean flag = true;
        JsonArray var81 = null;
        try {
            context.start(true);
            LOGGER.info("temp:{}",temp);
            HttpServletRequest request = (HttpServletRequest) temp.get("HttpServletRequest");
            String CreateQuanlity = (String)temp.get("CreateQuanlity");
            String type = (String)temp.get("type");
            Context var11 = context;
            ENONewOptions var14 = new ENONewOptions();
            var14.setParsedResult(true);//true
            HashMap var87 = new HashMap();
            var87.put("activeFolder", false);
            var87.put("typeName", "assembly");
            var14.setParam(var87);
//        var14.setSecurityContext("ctx::VPLMProjectLeader.JFCompany.JFSeat");//设置上下文
            ENONewAdaptor var88 = new ENONewAdaptor(var11);
            var88.set_httpSession(request.getSession());//request
            JsonObjectBuilder objBuilder = Json.createObjectBuilder();
            objBuilder.add("type", "VPMReference");
            int num = Integer.parseInt(CreateQuanlity);
//            LOGGER.info("build JSON start:{}",temp);
            JsonArray var84 = getJsongc(context, args, num,temp,type);
//            LOGGER.info("json:{}", var84);
            var81 = var88.createObjectForMultiple(var84, var14);//拿到结果之后设置值 或者直接把值写到JSON 里面去
            LOGGER.info("var81:{}", var81);
        }catch (Exception e){
            e.printStackTrace();
            flag= false;
        }finally {
            if(context.isTransactionActive()){
                context.commit();
            }
        }
        return var81;
    }
    public  JsonArray getJsongc(Context context,String[] args, int CreateQuanlity,Map temp,String type) throws Exception{
        StringList createList = new StringList();
        JF_VPMT_mxJPO jfVpmtMxJPO = JF_VPMT_mxJPO.getInstance(context);
        for(int i=0;i<CreateQuanlity;i++){
            createList.add(jfVpmtMxJPO.autoName(context,null,type));
        }
//        createList.add("Physical Product10501010");//后续可以调用编码器、或者生成正式编码
//        createList.add("Physical Product10601011");
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
            Map V_description = setMap("V_description", "String", UIUtil.getValue(temp, "description"));
            V_description.put("modified", true);
            Map policy = setMap("policy", "String", "VPLM_SMB_Definition_MajorRev");
            publicAttributes.add(Json.createObjectBuilder(V_Name));
            publicAttributes.add(Json.createObjectBuilder(PLM_ExternalID));
            publicAttributes.add(Json.createObjectBuilder(V_description));
            publicAttributes.add(Json.createObjectBuilder(policy));
            attributeMap.put("publicAttributes", publicAttributes.build());
            JsonArrayBuilder extensionAttributes = Json.createArrayBuilder();
//            Map extensionAttribute = setMapAttribute("JF_ProcurementType", "String", UIUtil.getValue(temp, "JF_ProcurementType"));
//            Map JF_DirectBuy = setMapAttribute("JF_DirectBuy", "String", UIUtil.getValue(temp, "JF_DirectBuy"));
//            Map JF_PartNameEN = setMapAttribute("JF_PartNameEN", "String", UIUtil.getValue(temp, "JFPartitionChooserValue"));
//            Map JF_PartNameCN = setMapAttribute("JF_PartNameCN", "String", UIUtil.getValue(temp, "JFPartitionChooserDisplay"));
            Map JF_PartType = setMapAttribute("JF_PartType", "String", type);
//            Map JF_PartSubType = setMapAttribute("JF_PartSubType", "String", "T02");
//            Map V_PartNumber = setMapAttribute("V_PartNumber", "String", phyName);
//            V_PartNumber.put("extension", "EnterpriseExtension");
//            extensionAttributes.add(Json.createObjectBuilder(extensionAttribute));
//            extensionAttributes.add(Json.createObjectBuilder(JF_DirectBuy));
//            extensionAttributes.add(Json.createObjectBuilder(JF_PartNameEN));
//            extensionAttributes.add(Json.createObjectBuilder(JF_PartNameCN));
            extensionAttributes.add(Json.createObjectBuilder(JF_PartType));
//            extensionAttributes.add(Json.createObjectBuilder(JF_PartSubType));
//            extensionAttributes.add(Json.createObjectBuilder(V_PartNumber));
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

    /*
     * @description: 获取当前分类的全路径
     * @author: caipan
     * @date:
     * @param: * @param[1] context
     * @param[2] classId
     * @return:
     **/
    public Map getLibPartCode(Context context,String classId) throws Exception{
            Map map = new HashMap();

        DomainObject from = DomainObject.newInstance(context,classId);
        MapList libraryMapList = new JF_VPMReferenceEBOM_mxJPO().findpartitionLibrary(context, from);
        Map libraryMap = (Map) libraryMapList.get(libraryMapList.size() - 1);
        String libraryTitle = (String) libraryMap.get("attribute[Title]");
        Properties properties = JF_Util_mxJPO.readPageObject(context, "JFJDConfig");
        String partitionLibraryTitle = (String) properties.get("partitionLibrary.Title");
        StringBuffer partitionCN = new StringBuffer();
        StringBuffer partitionEN = new StringBuffer();
        for (int i = libraryMapList.size() - 2; i >= 0; i--) {
            libraryMap = (Map) libraryMapList.get(i);
            partitionCN.append(libraryMap.get("attribute[Title]")).append("-");
            partitionEN.append(libraryMap.get(DomainConstants.SELECT_DESCRIPTION)).append("-");
        }
        if (partitionLibraryTitle.equals(libraryTitle)) {
            partitionCN.append(from.getAttributeValue(context, "Title"));
            partitionEN.append(from.getDescription(context));
            map.put("partitionEN",partitionEN.toString());
            map.put("partitionCN",partitionCN.toString());
        }
        return map;
    }
    /*
     * @description:获取零件详细分类中英文
     * @author: caipan
     * @date: 2025/8/1 16:42:28
     * @param: * @param[1] context
     * @param[2] args
     * @return:
     **/
    public String getT02JF_Detail_CN(Context context,String[] args) throws Exception{
        Map map = (Map) JPO.unpackArgs(args);
        Map requestMap = (Map) map.get(JF_PLMConstants_mxJPO.STRING_FIELDMAP);
        String fieldName = UIUtil.getValue(requestMap, DomainConstants.SELECT_NAME);
        MapList list = DomainObject.findObjects(context, "General Class","*","attribute[JF_PartSubType]==T02",new StringList(DomainConstants.SELECT_ID));
        String classId ="";
        if(list.size()>0){
            classId = (String)((Map)(list.get(0))).get(DomainConstants.SELECT_ID);
        }
        if(UIUtil.isNullOrEmpty(classId)) {
            //先写死吧
            if ("JF_Detail_EN".equalsIgnoreCase(fieldName)) {
                return "Pattern ";
            }
            if ("JF_Detail_CN".equalsIgnoreCase(fieldName)) {
                return "\u88C1\u7247";
            }
        }else{
            DomainObject classObj =DomainObject.newInstance(context,classId);
            if ("JF_Detail_EN".equalsIgnoreCase(fieldName)) {
                return classObj.getDescription(context);
//                return "Pattern ";
            }
            if ("JF_Detail_CN".equalsIgnoreCase(fieldName)) {
//                return "\u88C1\u7247";
                return classObj.getAttributeValue(context, "Title");
            }
        }
        return "";
    }
    /*
     * @description:获取创建样板件的range值
     * @author: caipan
     * @date: 2025/8/1 17:30:55
     * @param: * @param[1] context
     * @param[2] args
     * @return:
     **/
    public HashMap getRangeValue(Context context, String[] args) throws Exception {
        HashMap rangeMap = new HashMap();
        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        Map requestMap = (Map) programMap.get(JF_PLMConstants_mxJPO.STRING_FIELDMAP);
        String fieldName = UIUtil.getValue(requestMap, DomainConstants.SELECT_NAME);
        LOGGER.info("fieldName:{}",fieldName);
        String language = context.getLocale().getLanguage();
        StringList ranges = new StringList();
        StringList valueList = new StringList();
        if("JF_ProcurementType".equalsIgnoreCase(fieldName)) {
             ranges = FrameworkUtil.getRanges(context, "JF_VPMReference.JF_ProcurementType");
            ranges.remove(0);
             valueList = EnoviaResourceBundle.getAttrRangeI18NStringList(context, "JF_VPMReference.JF_ProcurementType", ranges, context.getLocale().toString());
        }
        if("JF_DirectBuy".equalsIgnoreCase(fieldName)) {
            ranges = FrameworkUtil.getRanges(context, "JF_VPMReference.JF_DirectBuy");
            valueList = EnoviaResourceBundle.getAttrRangeI18NStringList(context, "JF_VPMReference.JF_DirectBuy", ranges, context.getLocale().toString());
        }
        LOGGER.info("ranges:{}",ranges);

      /*  for(int i=0;i<ranges.size();i++) {
//            valueList.add(i18nNow.getRangeI18NString("JF_VPMReference."+fieldName,ranges.get(i), language));
            valueList.addElement(EnoviaResourceBundle.getProperty(context,"emxFrameworkStringResource", language,"JF_VPMReference."+fieldName));
            valueList.add(nlsRanges);
        }*/
        rangeMap.put("field_display_choices", valueList);
        rangeMap.put("field_choices", ranges);
        return rangeMap;
    }

}
