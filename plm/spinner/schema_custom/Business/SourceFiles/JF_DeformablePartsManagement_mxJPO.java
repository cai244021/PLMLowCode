import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.matrixone.apps.common.CommonDocument;
import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.DomainRelationship;
import com.matrixone.apps.domain.util.ContextUtil;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.util.MqlUtil;
import com.matrixone.apps.domain.util.PersonUtil;
import com.matrixone.apps.framework.ui.UIUtil;
import io.netty.handler.codec.http.HttpMethod;
import matrix.db.*;
import matrix.util.StringList;
import org.apache.http.client.HttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import static com.matrixone.apps.domain.DomainConstants.SELECT_ID;
import static com.matrixone.apps.domain.DomainConstants.SELECT_OWNER;

/**
 * @ClassName JF_DeformablePartsManagement_mxJPO
 * @Author: LIUJR
 * @CreateDate: 2024/8/6 15:23
 * @UpdateRemark:
 * @Version: 1.0
 * @Description: 变形件管理
 */
public class JF_DeformablePartsManagement_mxJPO implements JF_PLMConstants_mxJPO{
    private static final Logger JF_LOGGER = LoggerFactory.getLogger(JF_DeformablePartsManagement_mxJPO.class);
    private static final StringList busSelectList = new StringList();
    private static final String RELATIONSHIP_VPMINSTANCE = "VPMInstance";
    private static final String RELATIONSHIP_ORIGINAL_PART_FLEXIBLE_PART = "JFOriginalPart2Flexible";
    private static final String TYPE_VPMREFERENCE = "VPMReference";
    private static final String ATTRIBUTE_VPMREFERENCE_ORIGINALPART = "JF_VPMReference.JF_OriginalPart";
    private static final String ATTRIBUTE_VPMREFERENCE_FLEXIBLEPART = "JF_VPMReference.JF_FlexiblePart";
    private static final String ATTRIBUTE_VPMREFERENCE_JF_PartSubType = "JF_VPMReference.JF_PartSubType";
    private static final String ATTRIBUTE_VPMREFERENCE_TRANSFORMATIONPLAN = "JF_VPMReference.JF_TransformationPlan";
    private static final String ATTRIBUTE_V_PART_NUMBER = "EnterpriseExtension.V_PartNumber";

    static {
        busSelectList.add(DomainConstants.SELECT_ID);
        busSelectList.add(DomainConstants.SELECT_NAME);
        busSelectList.add(DomainConstants.SELECT_TYPE);
        busSelectList.add(DomainConstants.SELECT_REVISION);
    }

    /**
    * 获取零件下的原件还是变形件
    * @param context
	* @param args
    * @author LIUJR
    * @throws
    * @return com.matrixone.apps.domain.util.MapList
    * @date 2024/8/7 11:01
    * @description
    */
    public MapList getPartAllPart(Context context, String[] args)throws Exception{
        Map paramMap = JPO.unpackArgs(args);
        String objId = (String)paramMap.get(STRING_OBJECTID);
        DomainObject object = DomainObject.newInstance(context,objId);
        MapList childmapList = object.getRelatedObjects(
                context,
                RELATIONSHIP_ORIGINAL_PART_FLEXIBLE_PART,
                TYPE_VPMREFERENCE,
                busSelectList,
                null,
                true,
                true,
                (short) 1,
                "",
                "",
                0
        );
        childmapList.sort(DomainConstants.SELECT_NAME, "descending", "string");
        return childmapList;
    }


    /**
     * 克隆变形件前置JPO
     * @param context
     * @param args
     * @author LIUJR
     * @throws
     * @return voidf
     * @date 2024/8/8 11:27
     * @description
     */
    public Boolean preCloneDeformablePartsBack(Context context, String[] args) {
        Boolean flag = Boolean.TRUE;
        try {
            Map paramsMap = (Map) JPO.unpackArgs(args);
            StringList stringList = (StringList) paramsMap.get("objectIdList");
            String objectId = (String) paramsMap.get("objectId");
            String attrValue = (String) paramsMap.get(ATTRIBUTE_VPMREFERENCE_TRANSFORMATIONPLAN);
            String attrCodeValue = (String) paramsMap.get("code");
            ContextUtil.startTransaction(context, true);
            cloneDeformableParts(context, new String[]{objectId, attrValue, attrCodeValue});
            ContextUtil.commitTransaction(context);
        }catch (Exception e) {
            e.printStackTrace();
            flag = Boolean.FALSE;
            ContextUtil.abortTransaction(context);
        }
        return flag;
    }


    /**
    * 克隆变形件前置JPO   版本2 最新版本
    * @param context
	* @param args
    * @author LIUJR
    * @throws
    * @return voidf
    * @date 2024/8/8 11:27
    * @description
    */
    public Boolean preCloneDeformableParts(Context context, String[] args) {
        Boolean flag = Boolean.TRUE;
        try {
            Map paramsMap = (Map) JPO.unpackArgs(args);
            StringList stringList = (StringList) paramsMap.get("objectIdList");
            String objectId = (String) paramsMap.get("objectId");
            String attrValue = (String) paramsMap.get(ATTRIBUTE_VPMREFERENCE_TRANSFORMATIONPLAN);
            String attrCodeValue = (String) paramsMap.get("code");
            ContextUtil.startTransaction(context, true);
            StringList relOidList = cloneDeformablePartsSecond(context, new String[]{objectId, attrValue, attrCodeValue});
            DomainObject domainObject = DomainObject.newInstance(context);
            //获取原件下的一级件的子级
            domainObject.setId(objectId);
            String revision = domainObject.getInfo(context, DomainConstants.SELECT_REVISION);
            MapList childPartList = domainObject.getRelatedObjects(context, RELATIONSHIP_VPMINSTANCE, TYPE_VPMREFERENCE,
                    JF_Util_mxJPO.basicBolistSel(), JF_Util_mxJPO.basicRellistSel(), false, true, (short)1, "", "", 0);
            JF_LOGGER.info("childPartList:{}", childPartList);
            String flexId = DomainConstants.EMPTY_STRING;
            String name = DomainConstants.EMPTY_STRING;
            String mql2 = "mod bus $1 name $2 revision $3";
            JF_LOGGER.info("relOidList:{}", relOidList);
            for (int i = 0; i < relOidList.size(); i++) {
                //遍历变形件
                flexId = relOidList.get(i);
                domainObject.setId(flexId);
                //支持任意版本创建变形件，创建出来版本和原始版本一致
                name = domainObject.getInfo(context, DomainConstants.SELECT_NAME);
                MqlUtil.mqlCommand(context,false,false,mql2,true,flexId,name,revision);
                //下面关联的3DShape 也需要改
                StringList shapeList = domainObject.getInfoList(context, "from[VPMRepInstance].to.id");
                StringList assemblyList = domainObject.getInfoList(context, "from[XCADAssemblyRepInstance].to.id");
                if (!"AA.1-000".equalsIgnoreCase(revision)) {
                    shapeList.addAll(assemblyList);
                }
                JF_LOGGER.info("admin other RELEASED--shapeList->" + shapeList);
                for (String shapeId : shapeList) {
                    domainObject.setId(shapeId);
                    name = domainObject.getName(context);
                    MqlUtil.mqlCommand(context,false,false,mql2,true,shapeId,name,revision);
                }
            }
            ContextUtil.commitTransaction(context);
        }catch (Exception e) {
            e.printStackTrace();
            flag = Boolean.FALSE;
            ContextUtil.abortTransaction(context);
        }
        return flag;
    }

    /**
    * 变形件克隆
    * @param context
	* @param args
    * @author LIUJR
    * @throws
            * @return void
    * @date 2024/8/6 15:24
            * @description
    */
    public void cloneDeformableParts(Context context, String[] args) throws Exception {
        Boolean isPush = Boolean.FALSE;
        try {
            String objectId = args[0];
            String attrValue = args[1];
            String attrCodeValue = args[2];
            DomainObject domainObject = DomainObject.newInstance(context, objectId);
            //构造参数json
            String strPrefix = DomainConstants.EMPTY_STRING;
            JSONObject json = new JSONObject();
            //普通参数
            json.put("NLVEnabled", DomainConstants.EMPTY_STRING);
            json.put("excludeComposeeTypes", new JSONArray());
            json.put("folderid", null);
            json.put("includeDrawings", Boolean.FALSE);
            json.put("keepConfig", Boolean.FALSE);
            json.put("notificationTimeout", 600);
            //data部分
            JSONArray dataArray = new JSONArray();
            JSONObject jsonData = new JSONObject();
            //如果是重用，需要设置其他的
            jsonData.put("physicalid", domainObject.getInfo(context, "physicalid"));
            dataArray.add(jsonData);
            json.put("data", dataArray);
            //option部分
            JSONArray optionsArray = new JSONArray();
            for (int i = 0; i < 4; i++) {
                JSONObject jsonOptions = new JSONObject();
                switch (i) {
                    case 0: {
                        jsonOptions.put("key", "prefix");
                        jsonOptions.put("nlsKey", "前缀：");
                        jsonOptions.put("type", "text");
                        jsonOptions.put("value", strPrefix);
                        break;
                    }
                    case 1: {
                        jsonOptions.put("key", "wholeStructure");
                        jsonOptions.put("nlsKey", "包括某些结构对象");
                        jsonOptions.put("type", "checkbox");
                        jsonOptions.put("value", true);
                        break;
                    }
                    case 2: {
                        jsonOptions.put("key", "clearConfigurations");
                        jsonOptions.put("nlsKey", "duplicate.options.clearConfigurations");
                        jsonOptions.put("type", "checkbox");
                        jsonOptions.put("value", true);
                        break;
                    }
                    case 3: {
                        jsonOptions.put("key", "advanced");
                        jsonOptions.put("nlsKey", "高级");
                        jsonOptions.put("type", "accordion");
                        jsonOptions.put("value", true);
                        break;
                    }
                }
                optionsArray.add(jsonOptions);
            }
            json.put("options", optionsArray);
            //构造请求地址
            String url = "/resources/lifecycle/duplicate/structure?tenant=OnPremise";
            JF_LOGGER.info("json:{}", json.toJSONString());
            JSONObject vpmReferenceJSON = JF_WebServiceHandler_mxJPO.executeWebService(context, "POST", url, PersonUtil.getDefaultSecurityContext(context), "zh", json);
            JSONObject data = vpmReferenceJSON.getJSONObject("data");
            JF_LOGGER.info("返回值vpmReferenceJSON:{}", vpmReferenceJSON.toString());
            JSONArray results = data.getJSONArray("results");
            JSONArray jsonArray = results.getJSONArray(0);
            //解析response json
            JF_LOGGER.info("返回值jsonArray：{}", jsonArray.toString());
            MapList mapList = new MapList();
            for (int i = 0; i < jsonArray.size(); i++) {
                JSONObject item = jsonArray.getJSONObject(i);
                Map<String, String> map = new HashMap<>();
                String physicalId = item.getString("physicalid");
                String sourceId = item.getString("sourceid");
                map.put("physicalid", physicalId);
                map.put("sourceId", sourceId);
                mapList.add(map);
            }
            //拿取到返回结果后
            JF_LOGGER.info("返回值mapList:{}", mapList.toString());
            //与原件建立关系
            StringList relOidList = new StringList();
            ContextUtil.pushContext(context);
            isPush = Boolean.TRUE;
            Iterator iterator = mapList.iterator();
            while (iterator.hasNext()) {
                Map map = (Map) iterator.next();
                String physicalid = UIUtil.getValue(map, "physicalid");
                String sourceId = UIUtil.getValue(map, "sourceId");
                DomainObject deformablePart = DomainObject.newInstance(context, physicalid);
                DomainObject sourcePart = DomainObject.newInstance(context, sourceId);
                if (TYPE_VPMREFERENCE.equalsIgnoreCase(deformablePart.getTypeName(context))) {
                    //拿取原件的企业编码
                    String strPartNumber = sourcePart.getAttributeValue(context, ATTRIBUTE_V_PART_NUMBER);
                    String strPartType = sourcePart.getAttributeValue(context, JF_PLMConstants_mxJPO.ATTR_JFPartType);
                    String strPartSubType = sourcePart.getAttributeValue(context, ATTRIBUTE_VPMREFERENCE_JF_PartSubType);
                    //当是物理产品的时候
                    //变形件设置
                    HashMap<String, String> deformableMap = new HashMap<>();
                    deformableMap.put(ATTRIBUTE_VPMREFERENCE_ORIGINALPART, "N");
                    deformableMap.put(ATTRIBUTE_VPMREFERENCE_FLEXIBLEPART, "Y");
                    deformableMap.put(ATTRIBUTE_VPMREFERENCE_TRANSFORMATIONPLAN, attrValue);
//                    deformableMap.put(ATTRIBUTE_V_PART_NUMBER, strPartNumber);
                    deformableMap.put(ATTRIBUTE_VPMREFERENCE_JF_PartSubType, strPartSubType);
                    deformableMap.put(JF_PLMConstants_mxJPO.ATTR_JFPartType, strPartType);
                    if ("undefined".equalsIgnoreCase(attrCodeValue) || UIUtil.isNullOrEmpty(attrCodeValue) || "null".equalsIgnoreCase(attrCodeValue)) {
                        deformableMap.put("PLMEntity.V_Name", strPartNumber);
                    } else {
                        deformableMap.put("PLMEntity.V_Name", strPartNumber + "_" + attrCodeValue);
                    }
                    deformablePart.setAttributeValues(context, deformableMap);
                    //原件设置
                    HashMap<String, String> sourceMap = new HashMap<>();
                    sourceMap.put(ATTRIBUTE_VPMREFERENCE_ORIGINALPART, "Y");
                    sourceMap.put(ATTRIBUTE_VPMREFERENCE_FLEXIBLEPART, "N");
                    sourcePart.setAttributeValues(context, sourceMap);

                    //保存关系
                    if ("FALSE".equalsIgnoreCase(deformablePart.getInfo(context, "to[" + RELATIONSHIP_VPMINSTANCE + "]"))) {
                        relOidList.add(deformablePart.getId(context));
                    }
                    //将文件改名字
                    String shipId = deformablePart.getInfo(context, "from[VPMRepInstance].to.id");
                    if (UIUtil.isNotNullAndNotEmpty(shipId)) {
                        String fileName = DomainConstants.EMPTY_STRING;
                        if ("undefined".equalsIgnoreCase(attrCodeValue) || UIUtil.isNullOrEmpty(attrCodeValue) || "null".equalsIgnoreCase(attrCodeValue)) {
                            fileName = strPartNumber;
                        } else {
                            fileName = strPartNumber + "_" + attrCodeValue;
                        }
                        DomainObject domainObjectShip = DomainObject.newInstance(context, shipId);
                        JF_VPMReference_mxJPO jfVpmReferenceMxJPO = new JF_VPMReference_mxJPO();
                        jfVpmReferenceMxJPO.updateDrawFileName(context, domainObjectShip, fileName, shipId);
                    }
                }
            }
            //关联关系
            String[] stringArray = relOidList.toStringArray();
            DomainRelationship.connect(context, domainObject, new RelationshipType(RELATIONSHIP_ORIGINAL_PART_FLEXIBLE_PART), true, stringArray);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        } finally {
            if (isPush) {
                ContextUtil.popContext(context);
            }
        }
    }



    /**
    * 变形件克隆 版本2 最新版
    * @param context
	* @param args
    * @author LIUJR
    * @throws
    * @return void
    * @date 2024/8/6 15:24
    * @description
    */
    public StringList cloneDeformablePartsSecond(Context context, String[] args) throws Exception {
        Boolean isPush = Boolean.FALSE;
        StringList relOidList = new StringList();
        try {
            String objectId = args[0];
            String attrValue = args[1];
            String attrCodeValue = args[2];
            DomainObject domainObject = DomainObject.newInstance(context, objectId);
            DomainObject psObject = DomainObject.newInstance(context, objectId);
            String title = domainObject.getAttributeValue(context, JF_PLMConstants_mxJPO.ATTR_V_PART_NUMBER);
            String revision = domainObject.getInfo(context, DomainConstants.SELECT_REVISION);
            JF_VPMReferenceEBOM_mxJPO jfVpmReferenceEBOMMxJPO = new JF_VPMReferenceEBOM_mxJPO();
            String psId = jfVpmReferenceEBOMMxJPO.getPartBelongProject(context, new String[]{objectId});
            JF_LOGGER.info("psId:{}", psId);
            //以下为重用构造参数json 2051215 liujr
            String strPrefix = DomainConstants.EMPTY_STRING;
            JSONObject json = new JSONObject();
            //普通参数
            json.put("NLVEnabled", DomainConstants.EMPTY_STRING);
            json.put("excludeComposeeTypes", new JSONArray());
//            json.put("folderid", null);
            json.put("includeDrawings", Boolean.FALSE);
            json.put("keepConfig", Boolean.FALSE);
            json.put("notificationTimeout", 600);
            //data部分
            JSONArray dataArray = new JSONArray();
            JSONObject jsonData = new JSONObject();
            //如果是重用，需要设置其他的
            jsonData.put("physicalid", domainObject.getInfo(context, "physicalid"));
            //结构中有子级，需要重用结构  update by ljr
            jsonData.put("isRootNode", true);
            jsonData.put("operation", "duplicate");
            jsonData.put("title", title);
            jsonData.put("revision", revision);
            //下级关系
            MapList childPartList = domainObject.getRelatedObjects(context,
                    RELATIONSHIP_VPMINSTANCE,
                    TYPE_VPMREFERENCE,
                    StringList.create(SELECT_ID, JF_PLMConstants_mxJPO.SELECT_ATTR_V_PART_NUMBER,DomainConstants.SELECT_REVISION, "physicalid"),
                    StringList.create(DomainRelationship.SELECT_ID, "physicalid[connection]"),
                    false,
                    true,
                    (short)1,
                    "",
                    "",
                    0);
            JF_LOGGER.info("childPartList:{}", childPartList);
            JSONArray relationsDataArray = new JSONArray();
            for (int i = 0; i < childPartList.size(); i++) {
                Map map = (Map) childPartList.get(i);
                JSONObject relationsJsonData = new JSONObject();
                relationsJsonData.put("physicalid", UIUtil.getValue(map, "physicalid[connection]"));
                relationsJsonData.put("type", "VPMInstance");
                relationsJsonData.put("operation", "reuse");
                relationsDataArray.add(relationsJsonData);
            }
            jsonData.put("relations", relationsDataArray);
            dataArray.add(jsonData);
            //子级部分
            for (int i = 0; i < childPartList.size(); i++) {
                Map map = (Map) childPartList.get(i);
                JSONObject childJsonData = new JSONObject();
                childJsonData.put("physicalid", UIUtil.getValue(map, "physicalid"));
                childJsonData.put("isRootNode", "false");
                childJsonData.put("operation", "reuse");
                childJsonData.put("title", UIUtil.getValue(map, JF_PLMConstants_mxJPO.SELECT_ATTR_V_PART_NUMBER));
                childJsonData.put("revision", UIUtil.getValue(map, DomainConstants.SELECT_REVISION));
                childJsonData.put("relations", new JSONArray());
                dataArray.add(childJsonData);
            }
            json.put("data", dataArray);
            //option部分
            JSONArray optionsArray = new JSONArray();
            for (int i = 0; i < 4; i++) {
                JSONObject jsonOptions = new JSONObject();
                switch (i) {
                    case 0: {
                        jsonOptions.put("key", "prefix");
                        jsonOptions.put("nlsKey", "前缀：");
                        jsonOptions.put("type", "text");
                        jsonOptions.put("value", strPrefix);
                        break;
                    }
                    case 1: {
                        jsonOptions.put("key", "wholeStructure");
                        jsonOptions.put("nlsKey", "包括某些结构对象");
                        jsonOptions.put("type", "checkbox");
                        jsonOptions.put("value", true);
                        break;
                    }
                    case 2: {
                        jsonOptions.put("key", "clearConfigurations");
                        jsonOptions.put("nlsKey", "duplicate.options.clearConfigurations");
                        jsonOptions.put("type", "checkbox");
                        jsonOptions.put("value", true);
                        break;
                    }
                    case 3: {
                        jsonOptions.put("key", "advanced");
                        jsonOptions.put("nlsKey", "高级");
                        jsonOptions.put("type", "accordion");
                        jsonOptions.put("value", true);
                        //重用
                        jsonOptions.put("usingAdvancedDuplicate", true);
                        break;
                    }
                }
                optionsArray.add(jsonOptions);
            }
            json.put("options", optionsArray);
            //构造请求地址
            String url = "/resources/lifecycle/duplicate/structure?tenant=OnPremise";
            JF_LOGGER.info("json:{}", json.toJSONString());
            JSONObject vpmReferenceJSON = JF_WebServiceHandler_mxJPO.executeWebService(context, "POST", url, PersonUtil.getDefaultSecurityContext(context), "zh", json);
            JSONObject data = vpmReferenceJSON.getJSONObject("data");
            JF_LOGGER.info("返回值vpmReferenceJSON:{}", vpmReferenceJSON.toString());
            JSONArray results = data.getJSONArray("results");
            JSONArray jsonArray = results.getJSONArray(0);
            //解析response json
            JF_LOGGER.info("返回值jsonArray：{}", jsonArray.toString());
            MapList mapList = new MapList();
            for (int i = 0; i < jsonArray.size(); i++) {
                JSONObject item = jsonArray.getJSONObject(i);
                Map<String, String> map = new HashMap<>();
                String physicalId = item.getString("physicalid");
                String sourceId = item.getString("sourceid");
                map.put("physicalid", physicalId);
                map.put("sourceId", sourceId);
                mapList.add(map);
            }
            //拿取到返回结果后
            JF_LOGGER.info("返回值mapList:{}", mapList.toString());
            //与原件建立关系
            ContextUtil.pushContext(context);
            isPush = Boolean.TRUE;
            Iterator iterator = mapList.iterator();
            while (iterator.hasNext()) {
                Map map = (Map) iterator.next();
                String physicalid = UIUtil.getValue(map, "physicalid");
                String sourceId = UIUtil.getValue(map, "sourceId");
                DomainObject deformablePart = DomainObject.newInstance(context, physicalid);
                DomainObject sourcePart = DomainObject.newInstance(context, sourceId);
                if (TYPE_VPMREFERENCE.equalsIgnoreCase(deformablePart.getTypeName(context))) {
                    String classId = sourcePart.getInfo(context, "to[Classified Item].from.id");
                    //关联库
                    deformablePart.addRelatedObjects(context, new RelationshipType("Classified Item"), false ,new String[]{classId});
                    //拿取原件的企业编码
                    String strPartNumber = sourcePart.getAttributeValue(context, ATTRIBUTE_V_PART_NUMBER);
                    String strPartType = sourcePart.getAttributeValue(context, JF_PLMConstants_mxJPO.ATTR_JFPartType);
                    String strPartSubType = sourcePart.getAttributeValue(context, ATTRIBUTE_VPMREFERENCE_JF_PartSubType);
                    //当是物理产品的时候
                    //变形件设置
                    HashMap<String, String> deformableMap = new HashMap<>();
                    deformableMap.put(ATTRIBUTE_VPMREFERENCE_ORIGINALPART, "N");
                    deformableMap.put(ATTRIBUTE_VPMREFERENCE_FLEXIBLEPART, "Y");
                    deformableMap.put(ATTRIBUTE_VPMREFERENCE_TRANSFORMATIONPLAN, attrValue);
//                    deformableMap.put(ATTRIBUTE_V_PART_NUMBER, strPartNumber);
                    deformableMap.put(ATTRIBUTE_VPMREFERENCE_JF_PartSubType, strPartSubType);
                    deformableMap.put(JF_PLMConstants_mxJPO.ATTR_JFPartType, strPartType);
                    JF_LOGGER.info("attrCodeValue:{}", attrCodeValue);
                    if ("undefined".equalsIgnoreCase(attrCodeValue) || UIUtil.isNullOrEmpty(attrCodeValue) || "null".equalsIgnoreCase(attrCodeValue)) {
                        deformableMap.put("PLMEntity.V_Name", strPartNumber);
                    } else {
                        deformableMap.put("PLMEntity.V_Name", strPartNumber + "_" + attrCodeValue);
                    }
                    deformablePart.setAttributeValues(context, deformableMap);
                    //原件设置
                    HashMap<String, String> sourceMap = new HashMap<>();
                    sourceMap.put(ATTRIBUTE_VPMREFERENCE_ORIGINALPART, "Y");
                    sourceMap.put(ATTRIBUTE_VPMREFERENCE_FLEXIBLEPART, "N");
                    sourcePart.setAttributeValues(context, sourceMap);

                    //保存关系
                    if ("FALSE".equalsIgnoreCase(deformablePart.getInfo(context, "to[" + RELATIONSHIP_VPMINSTANCE + "]"))) {
                        relOidList.add(deformablePart.getId(context));
                    }
                    //将文件改名字
                    String shipId = deformablePart.getInfo(context, "from[VPMRepInstance].to.id");
                    //XCADAssemblyRepInstance
                    if (UIUtil.isNotNullAndNotEmpty(shipId)) {
                        String fileName = DomainConstants.EMPTY_STRING;
                        if ("undefined".equalsIgnoreCase(attrCodeValue) || UIUtil.isNullOrEmpty(attrCodeValue)  || "null".equalsIgnoreCase(attrCodeValue)) {
                            fileName = strPartNumber;
                        } else {
                            fileName = strPartNumber + "_" + attrCodeValue;
                        }
                        DomainObject domainObjectShip = DomainObject.newInstance(context, shipId);
                        JF_VPMReference_mxJPO jfVpmReferenceMxJPO = new JF_VPMReference_mxJPO();
                        jfVpmReferenceMxJPO.updateDrawFileName(context, domainObjectShip, fileName, shipId);
                    }
                    String xcadShipId = deformablePart.getInfo(context, "from[XCADAssemblyRepInstance].to.id");
                    if (UIUtil.isNotNullAndNotEmpty(xcadShipId)) {
                        String fileName = DomainConstants.EMPTY_STRING;
                        if ("undefined".equalsIgnoreCase(attrCodeValue) || UIUtil.isNullOrEmpty(attrCodeValue)  || "null".equalsIgnoreCase(attrCodeValue)) {
                            fileName = strPartNumber;
                        } else {
                            fileName = strPartNumber + "_" + attrCodeValue;
                        }
                        DomainObject domainObjectShip = DomainObject.newInstance(context, xcadShipId);
                        JF_VPMReference_mxJPO jfVpmReferenceMxJPO = new JF_VPMReference_mxJPO();
                        jfVpmReferenceMxJPO.updateDrawFileName(context, domainObjectShip, fileName, xcadShipId);
                    }
                }
            }
            //关联关系
            String[] stringArray = relOidList.toStringArray();
            DomainRelationship.connect(context, domainObject, new RelationshipType(RELATIONSHIP_ORIGINAL_PART_FLEXIBLE_PART), true, stringArray);
            //关联项目
            if (UIUtil.isNotNullAndNotEmpty(psId)) {
                psObject.setId(psId);
                String description = psObject.getDescription(context);
                DomainRelationship domainRelationship;
                for (int i = 0; i < relOidList.size(); i++) {
                    String id = relOidList.get(i);
                    domainRelationship = psObject.addToObject(context, new RelationshipType(JF_PLMConstants_mxJPO.rel_JFProject2RootPart), id);
                    domainRelationship.setAttributeValue(context, ATTR_JF_BelongPart, "Y");
                    domainObject.setId(id);
                    domainObject.setAttributeValue(context, JF_PLMConstants_mxJPO.ATTR_JF_ProjectRel, description);
                }
            }
        }catch (Exception e) {
            e.printStackTrace();
            throw e;
        } finally {
            if (isPush) {
                ContextUtil.popContext(context);
            }
        }
        return relOidList;
    }


    /**
     * 获取零件下样板件
     * @param context
     * @param args
     * @author caipan
     * @throws
     * @return com.matrixone.apps.domain.util.MapList
     * @date 2024/10/16 11:01
     * @description
     */
    public MapList getPartConnTrim(Context context, String[] args)throws Exception{
        Map paramMap = JPO.unpackArgs(args);
        String objId = (String)paramMap.get(STRING_OBJECTID);
        DomainObject object = DomainObject.newInstance(context,objId);
        String where = "attribute[JF_VPMReference.JF_PartType]==T";//样板间是JF_PartSubType==T02 ，目前先放到面套件            StringList relSel = new StringList();
        StringList relSel = new StringList();
        relSel.add("attribute[JF_VPMInstance.JF_Dosage]");
        relSel.add(DomainRelationship.SELECT_ID);
        relSel.add("from.id");
        busSelectList.add(DomainConstants.SELECT_LEVEL);
        MapList childmapList = object.getRelatedObjects(
                context,
                JF_PLMConstants_mxJPO.REL_Instance,
                TYPE_VPMREFERENCE,
                busSelectList,
                relSel,
                false,
                true,
                (short) 1,
                where,
                "",
                0
        );
        childmapList.sort(DomainConstants.SELECT_NAME, "descending", "string");
        return childmapList;
    }

    /**
     *    原件升级的时候，连带着变形件升级；
     *    在零件升版（revision action）的时候创建trigger,去将关联变形件升版，
     *    如果变形件版本存在直接拿取，然后将升版后的原件与升版后的变形件关联起来
     * @param context
     * @param args
     * @author LIUJR
     * @throws
     * @return void
     * @date 09/06/2025 10:11
     * @description
     */
    public void originalPartRevisionAndConn(Context context, String[] args){
        try {
            ContextUtil.startTransaction(context, true);
            JF_LOGGER.info("originalPartRevisionAndConn start。。。。。。。。。。。。");
            String objectId = args[0];
            JF_LOGGER.info("docId:{}", objectId);
            //升版的原件
            DomainObject contextFeature = new DomainObject(objectId);
            //如果是原件
            String original = contextFeature.getAttributeValue(context, JF_PLMConstants_mxJPO.Attr_JF_OriginalPart);
            String flex = contextFeature.getAttributeValue(context, JF_PLMConstants_mxJPO.Attr_JF_FlexiblePart);
            JF_LOGGER.info("original：{}", original);
            JF_LOGGER.info("flex：{}", flex);
            //不是原件
            if (("N".equalsIgnoreCase(original)&&"N".equalsIgnoreCase(flex)) || "Y".equalsIgnoreCase(flex)) {
                return;
            }
            BusinessObject nextRevision = contextFeature.getNextRevision(context);
            //升版后的原件
            String nextObjectId = nextRevision.getObjectId(context);
            DomainObject nextObject = DomainObject.newInstance(context, nextObjectId);
            //升版原件的所有变形件
            StringList flexPartList = contextFeature.getInfoList(context, "from[" + RELATIONSHIP_ORIGINAL_PART_FLEXIBLE_PART + "].to.id");
            JF_LOGGER.info("flexPartList：{}", flexPartList);
            DomainObject flexObject = DomainObject.newInstance(context);
            StringList relOidList = new StringList();
            for (int i = 0; i < flexPartList.size(); i++) {
                String flexPartId = flexPartList.get(i);
                flexObject.setId(flexPartId);
                String number = flexObject.getAttributeValue(context, JF_PLMConstants_mxJPO.ATTR_V_PART_NUMBER);
                String nextMajorSequence = flexObject.getNextMajorSequence(context);
                String physicalid = flexObject.getInfo(context, "physicalid");
                String logicalid = flexObject.getInfo(context, "logicalid");
                JF_LOGGER.info("number：{}", number);
                JF_LOGGER.info("nextMajorSequence：{}", nextMajorSequence);
                JF_LOGGER.info("physicalid：{}", physicalid);
                //获取系统中变形件的下一个版本是否存在，判断： type==VPMReference, EnterpriseExtension.V_PartNumber==number,JF_FlexiblePart==Y
                String flexNextId = JF_PublicMethodClass_mxJPO.findObject(context, JF_PLMConstants_mxJPO.TYPE_VPMReference,
                        SELECT_ATTR_V_PART_NUMBER + "==" + number +
                                "&&revision==" + nextMajorSequence +
                                "&&" + SELECT_Attr_JF_FlexiblePart + "==Y");
                JF_LOGGER.info("flexNextId: {}", flexNextId);
                if (UIUtil.isNullOrEmpty(flexNextId)) {
                    //需要升版
                    String majoredRevision = JF_Util_mxJPO.majorRevision(context, physicalid);
                    JF_LOGGER.info("majoredRevision: {}", majoredRevision);
                    JSONObject jsonObject = JSONObject.parseObject(majoredRevision);
                    JSONArray results = jsonObject.getJSONArray("results");
                    for (int i1 = 0; i1 < results.size(); i1++) {
                        JSONObject jsonObject1 = results.getJSONObject(i1);
                        if (logicalid.equalsIgnoreCase(jsonObject1.getString("logicalid"))) {
                            flexNextId = jsonObject1.getString("physicalid");
                            break;
                        }

                    }
                    JF_LOGGER.info("flexNextId: {}", flexNextId);
                }
                //不需要升版 直接关联到升版后的原件
                relOidList.add(flexNextId);
            }
            JF_LOGGER.info("relOidList：{}", relOidList);
            //关联关系
//            if (!relOidList.isEmpty()) {
//                DomainRelationship.connect(context, nextObject, new RelationshipType(RELATIONSHIP_ORIGINAL_PART_FLEXIBLE_PART), true, relOidList.toStringArray());
//            }
            ContextUtil.commitTransaction(context);
        }catch (Exception e) {
            e.printStackTrace();
            JF_LOGGER.warn(e.getMessage());
            ContextUtil.abortTransaction(context);
        }
        JF_LOGGER.info("originalPartRevisionAndConn end。。。。。。。。。。。。");
    }

    /**
    * 原件关联变形件按钮的权限
    * @param context
	* @param args
    * @author LIUJR
    * @throws
    * @return java.lang.Boolean
    * @date 2026/1/15 15:28
    * @description
    */
    public Boolean getConnFlexblePartCommAccess(Context context ,String[] args) {
        Boolean flag = Boolean.TRUE;
        try {
            //(context.user==owner&&attribute[JF_VPMReference.JF_FlexiblePart].value==N)
            HashMap paramsMap = (HashMap) JPO.unpackArgs(args);
            HashMap requestMap = (HashMap) paramsMap.get("requestMap");
            String strObjectId = null;
            if (requestMap == null) {
                strObjectId = (String) paramsMap.get("objectId");
            } else {
                strObjectId = (String) requestMap.get("objectId");
            }
            DomainObject partObject = DomainObject.newInstance(context, strObjectId);
            String owner = partObject.getInfo(context, DomainConstants.SELECT_OWNER);
            String flex = partObject.getAttributeValue(context, JF_PLMConstants_mxJPO.Attr_JF_FlexiblePart);
            JF_LOGGER.info("owner:{}", owner);
            JF_LOGGER.info("flex:{}", flex);
            if (context.getUser().toString().equalsIgnoreCase(owner)) {
                if ("Y".equalsIgnoreCase(flex)) {
                    return Boolean.FALSE;
                }
            }else {
                return Boolean.FALSE;
            }
            JF_LOGGER.info("strObjectId:{}", strObjectId);
            String number = partObject.getAttributeValue(context, JF_PLMConstants_mxJPO.ATTR_V_PART_NUMBER);
            JF_LOGGER.info("number:{}", number);
            String rev = partObject.getInfo(context, DomainConstants.SELECT_REVISION);
            JF_LOGGER.info("rev:{}", rev);
            //原件关联的变形件
            StringList flexConnIdList = partObject.getInfoList(context, "from[" + REL_JFOriginalPart2Flexible + "].to.id");
            JF_LOGGER.info("flexConnIdList:{}", flexConnIdList);
            //查询原件是否有相同的变形件
            MapList partnerMapList = DomainObject.findObjects(
                    context,
                    JF_PLMConstants_mxJPO.TYPE_VPMReference,
                    DomainConstants.QUERY_WILDCARD,
                    SELECT_ATTR_V_PART_NUMBER + "=='" + number + "' && revision=='" + rev +"' && owner=='" + owner + "' && " + SELECT_Attr_JF_FlexiblePart + "==Y",
                    JF_Util_mxJPO.basicBolistSel());
            JF_LOGGER.info("partnerMapList:{}", partnerMapList);
            if (partnerMapList.isEmpty()) {
                flag = Boolean.FALSE;
            } else {
                StringList otherList = new StringList();
                for (int i = 0; i < partnerMapList.size(); i++) {
                    Map map = (Map) partnerMapList.get(i);
                    if (!flexConnIdList.contains(UIUtil.getValue(map, SELECT_ID))) {
                        otherList.add(UIUtil.getValue(map, SELECT_ID));
                    }
                }
                JF_LOGGER.info("otherList:{}", otherList);
                if (otherList.isEmpty()) {
                    flag = Boolean.FALSE;
                } else {
                    flag = Boolean.TRUE;
                }
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
        return flag;
    }

    /**
    * 原件关联变形件时  获取可关联的变形件
     *     添加时检查变形件零件号版本、所有者是不是和原件一致，不一致的情况下不允许添加。
    * @param context
	* @param args
    * @author LIUJR
    * @throws
    * @return matrix.util.StringList
    * @date 2026/1/15 15:32
    * @description
    */
    public StringList getConnFlexblePart(Context context, String[] args) {
        StringList partList = new StringList();
        try {
            Map paramMap = (Map) JPO.unpackArgs(args);
            String objectId = (String)paramMap.get("objectId");
            DomainObject partObject = DomainObject.newInstance(context, objectId);
            String number = partObject.getAttributeValue(context, JF_PLMConstants_mxJPO.ATTR_V_PART_NUMBER);
            String rev = partObject.getInfo(context, DomainConstants.SELECT_REVISION);
            String owner = partObject.getInfo(context, DomainConstants.SELECT_OWNER);
            //原件关联的变形件
            StringList flexConnIdList = partObject.getInfoList(context, "from[" + REL_JFOriginalPart2Flexible + "].to.id");
            //查询原件是否有相同的变形件
            MapList partnerMapList = DomainObject.findObjects(
                    context,
                    JF_PLMConstants_mxJPO.TYPE_VPMReference,
                    DomainConstants.QUERY_WILDCARD,
                    SELECT_ATTR_V_PART_NUMBER + "=='" + number + "' && revision=='" + rev +"' && owner=='" + owner + "' && " + SELECT_Attr_JF_FlexiblePart + "==Y",
                    JF_Util_mxJPO.basicBolistSel());
            if (!partnerMapList.isEmpty()) {
                StringList otherList = new StringList();
                for (int i = 0; i < partnerMapList.size(); i++) {
                    Map map = (Map) partnerMapList.get(i);
                    if (!flexConnIdList.contains(UIUtil.getValue(map, SELECT_ID))) {
                        partList.add(UIUtil.getValue(map, SELECT_ID));
                    }
                }
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
        return partList;
    }

    /**
    * 创建变形件的权限设置  只允许在最新版本（包括所有版本）的原件上创建变形件
    * @param context
	* @param args
    * @author LIUJR
    * @throws
    * @return java.lang.Boolean
    * @date 2026/1/21 16:24
    * @description
    */
    public static Boolean getCreateFlexblePartAccess(Context context, String[] args) throws Exception {
        Boolean flag = Boolean.FALSE;
        try {
            Map request = JPO.unpackArgs(args);
            JF_LOGGER.info("request:{}",request);
            String objectId = (String)request.get("objectId");
            DomainObject object = DomainObject.newInstance(context,objectId);
            //判断是否是最新版本  冒泡后续需要改
//            String latestMajoridNew = JF_Util_mxJPO.getLatestMajorid_new(context, new String[]{objectId});
            String isLastVersion = object.getAttributeValue(context, JF_PLMConstants_mxJPO.ATTR_V_isLastVersion);
            String flexPart = object.getAttributeValue(context, JF_PLMConstants_mxJPO.Attr_JF_FlexiblePart);
            String owner = object.getInfo(context, SELECT_OWNER);
            if ("TRUE".equalsIgnoreCase(isLastVersion) && context.getUser().toString().equalsIgnoreCase(owner) && "N".equalsIgnoreCase(flexPart)) {
                flag = Boolean.TRUE;
            }
        }catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
        return flag;
    }
}
