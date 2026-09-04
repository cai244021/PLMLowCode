import com.alibaba.fastjson.JSON;
import com.dassault_systemes.createcontent.ENONewAdaptor;
import com.dassault_systemes.createcontent.ErrorMngt.ENONewException;
import com.dassault_systemes.createcontent.models.ENONewOptions;
import com.dassault_systemes.smasds.powerby.services.util.ServiceUtils;
import com.dscn.plm.util.NioJDUtils;
import com.matrixone.apps.common.util.ComponentsUIUtil;
import com.matrixone.apps.common.util.ComponentsUtil;
import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.DomainRelationship;
import com.matrixone.apps.domain.util.*;
import com.matrixone.apps.framework.ui.UIUtil;
import com.matrixone.apps.program.ProgramCentralConstants;
import matrix.db.*;
import matrix.util.MatrixException;
import matrix.util.StringList;
import org.apache.commons.text.StringEscapeUtils;
import org.apache.poi.ss.usermodel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.FileInputStream;

import java.io.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static com.dassault_systemes.enovia.apps.materialcomposition.MATCConstants.STRING_NEW_VALUE;
import static com.matrixone.apps.domain.DomainConstants.*;

public class JF_Cost_mxJPO {
    private static final Logger LOGGER = LoggerFactory.getLogger(JF_Cost_mxJPO.class);
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");


    /*
     * @description: 获取最新版的快速报价
     * @author: caipan
     * @date: 2025/1/6 15:49:15
     * @param: * @param[1] context
     * @param[2] args
     * @return:
     **/
    public MapList getRapidOffer(Context context,String[] args) throws Exception{
        StringList selList = JF_Util_mxJPO.basicBolistSel();
        String where = JF_PLMConstants_mxJPO.islast;
        MapList list = DomainObject.findObjects(context, JF_PLMConstants_mxJPO.type_JFRapidOffer, "*", where,selList);
        return list;
    }
    /*
     * @description:获取功能模块库
     * @author: caipan
     * @date: 2025/1/7 14:12:16
     * @param: * @param[1] context
     * @param[2] args
     * @return:
     **/
    public MapList getFunctionModuleTemplate(Context context,String[] args) throws Exception{
        StringList selList = JF_Util_mxJPO.basicBolistSel();
        String where = null;
        MapList list = DomainObject.findObjects(context, JF_PLMConstants_mxJPO.type_JFFunctionModuleTemplate, "*", where,selList);
        return list;
    }
    /*
     * @description: 获取成本库
     * @author: caipan
     * @date: 2025/1/7 16:47:16
     * @param: * @param[1] context
     * @param[2] args
     * @return:
     **/
    public MapList getCostLib(Context context,String[] args) throws Exception{
        StringList selList = JF_Util_mxJPO.basicBolistSel();
        String where = null;
        MapList list = DomainObject.findObjects(context, JF_PLMConstants_mxJPO.type_JFCost, "*", where,selList);
        return list;
    }

    /*
     * @description: 获取制费库
     * @author: caipan
     * @date: 2025/1/7 16:47:16
     * @param: * @param[1] context
     * @param[2] args
     * @return:
     **/
    public MapList getManufactureFeeLib(Context context,String[] args) throws Exception{
        StringList selList = JF_Util_mxJPO.basicBolistSel();
        String where = null;
        MapList list = DomainObject.findObjects(context, JF_PLMConstants_mxJPO.type_JFManufactureFee, "*", where,selList);
        return list;
    }
    /*
     * @description:获取快速报价下的模块
     * @author: caipan
     * @date: 2025/1/13 13:27:54
     * @param: * @param[1] context
     * @param[2] args
     * @return:
     **/
    public MapList getCostModules(Context context,String[] args) throws Exception{
        Map map = JPO.unpackArgs(args);
        String objectId = UIUtil.getValue(map, "objectId");
        LOGGER.info("map:{},objectId：{}",map,objectId);
        DomainObject obj = DomainObject.newInstance(context,objectId);
        StringList selList = JF_Util_mxJPO.basicBolistSel();
        selList.add("attribute[JF_EvaluatePrice]");
        StringList relList = JF_Util_mxJPO.basicRellistSel();
        relList.add(SELECT_FROM_ID);
        String where = null;
        MapList mapList = obj.getRelatedObjects(
                context,
                JF_PLMConstants_mxJPO.rel_JFOffer2JFCostConfig,
                JF_PLMConstants_mxJPO.type_JFCostConfig,
                selList,
                relList,
                false,
                true,
                (short) 1,
                "",
                "",
                0);

        return mapList;
    }
    /*
     * @description:如果选择了配置就只读，带过来显示在这里
     *               如果没有选择读写
     * @author: caipan
     * @date: 2025/1/13 10:06:02
     * @param: * @param[1] context
     * @param[2] args
     * @return:
     **/
    public String getNameNumberBySelect(Context context ,String[] args) throws Exception{
        Map argsMap = JPO.unpackArgs(args);
        LOGGER.info("getNameNumberBySelect:{}",argsMap);
        Map requestMap = (Map) argsMap.get("requestMap");
        LOGGER.info("requestMap:{}",requestMap);
        String strPId = (String) requestMap.get("objectId");
        String emxTableRowId = (String) requestMap.get("emxTableRowIds");
        StringList selectId = FrameworkUtil.split(emxTableRowId, ",");
        LOGGER.info("selectId:{}",selectId.size());
        LOGGER.info("selectId:{}",selectId);
        StringBuffer sb = new StringBuffer();
        if(selectId.size()==2){
            String id = FrameworkUtil.split(selectId.get(0),"|").get(1);
            if(UIUtil.isNotNullAndNotEmpty(id)) {
                DomainObject obj = DomainObject.newInstance(context, id);
                String title = obj.getInfo(context, DomainConstants.SELECT_ATTRIBUTE_TITLE);
                String type = obj.getInfo(context, DomainConstants.SELECT_TYPE);
                if(JF_PLMConstants_mxJPO.type_JFCostConfig.equals(type)){
                    sb.append("<input id=\"JFConfigGroup\"  disabled='true' name=\"JFConfigGroup\" type=\"text\"  size=\"20\" value='"+title+"'/>");
                    sb.append("<input id=\"JFConfigGroupOID\"  disabled='true' name=\"JFConfigGroupOID\" type=\"hidden\"  size=\"20\" value='"+id+"'/>");
                }else{
                    sb.append("<input id=\"JFConfigGroup\"  disabled='true' name=\"JFConfigGroup\" type=\"text\"  size=\"20\" value=\"error\"/>");
                }
            }else{
                sb.append("<input id=\"JFConfigGroup\" name=\"JFConfigGroup\" type=\"text\"  size=\"20\" value=\"\"/>");
            }
        }else if(selectId.size()>2) {
            sb.append("<input id=\"JFConfigGroup\"  disabled='true' name=\"JFConfigGroup\" type=\"text\"  size=\"20\" value=\"two\"/>");
        }else{
            LOGGER.info("emxTableRowId:{}", emxTableRowId);
            sb.append("<input id=\"JFConfigGroup\" name=\"JFConfigGroup\" type=\"text\"  size=\"20\" value=\"\"/>");
        }

        sb.append("<script language=\"JavaScript\">");
        sb.append("myValidationRoutines.push(['validateRequiredField', 'JFConfigGroup']);");
        sb.append("</script>");
        return sb.toString() ;
    }

    /*
     * @description:创建模块或者报价项目
     * @author: caipan
     * @date: 2025/1/13 14:28:37
     * @param: * @param[1] context
     * @param[2] args
     * @return:
     **/
    public Map createModule(Context context, String args[]) throws Exception {
        Map map = new HashMap();
        try {
            HashMap requestMap = (HashMap) JPO.unpackArgs(args);
            LOGGER.info("requestMap:{}", requestMap);
            String parentOID = (String) requestMap.get("parentOID");
            String strJFColorGroupName = (String) requestMap.get("JF_ColorGroupName");
            String JFConfigGroup = (String) requestMap.get("JFConfigGroup");
            String JFConfigGroupOID = (String) requestMap.get("JFConfigGroupOID");
            LOGGER.info("JFConfigGroupOID:{}",JFConfigGroupOID);
            String strDescription = (String) requestMap.get("Description");
            String emxTableRowIds = (String) requestMap.get("emxTableRowIds");
            StringList selectList = FrameworkUtil.split(emxTableRowIds, ",");
            if(selectList.size()==2){
                JFConfigGroupOID = FrameworkUtil.split(selectList.get(0), "|").get(1);
            }
            LOGGER.info("JFConfigGroupOID:{}",JFConfigGroupOID);
            String costConfigId = "";
            DomainObject cost = DomainObject.newInstance(context);
            cost.setId(parentOID);
            if (UIUtil.isNullOrEmpty(JFConfigGroupOID)) {
                costConfigId = FrameworkUtil.autoName(context, "type_JFCostConfig", "policy_JFCostConfig");
                cost.addToObject(context, new RelationshipType(JF_PLMConstants_mxJPO.rel_JFOffer2JFCostConfig), costConfigId);
                cost.setId(costConfigId);
                cost.setAttributeValue(context, DomainConstants.ATTRIBUTE_TITLE, JFConfigGroup);//设置配置的Title
                cost.setDescription(context, strDescription);//设置配置的Title
            } else {
                LOGGER.info("no create:{},",JFConfigGroupOID);
                costConfigId = JFConfigGroupOID;
                cost.setId(costConfigId);
            }
            String moduleId = FrameworkUtil.autoName(context, "type_JFModules", "policy_JFModules");
            cost.addToObject(context, new RelationshipType(JF_PLMConstants_mxJPO.rel_JFModule2Module), moduleId);
            map.put("id", moduleId);
        }catch (Exception e){
            e.printStackTrace();
        }
        return  map;

    }
    /*
     * @description: 展开快速报价
     * @author: caipan
     * @date: 2025/1/14 11:32:04
     * @param: * @param[1] context
     * @param[2] args
     * @return:
     **/
    public MapList getExpand(Context context, String[] args)  {
        MapList childPartList = new MapList();
        try {
            Map paramMap = JPO.unpackArgs(args);
            String objId = (String)paramMap.get("objectId");
            String expandLevel = (String) paramMap.get("expandLevel");
            if ("All".equals(expandLevel)) {
                expandLevel = "0";
            }
            DomainObject obj = DomainObject.newInstance(context,objId);
            StringList boSel = JF_Util_mxJPO.basicBolistSel();
            boSel.add("attribute[JF_EvaluatePrice]");
            StringList relSel = new StringList();
            relSel.add(DomainRelationship.SELECT_ID);
            relSel.add(SELECT_FROM_ID);
            String relName = JF_PLMConstants_mxJPO.rel_JFModule2Module+","+JF_PLMConstants_mxJPO.rel_JFModule2ReferConst+","+JF_PLMConstants_mxJPO.rel_JFCostReferConst2Function;
            childPartList =  obj.getRelatedObjects(context,relName,"*",
                    boSel,relSel,false,true,Short.parseShort(expandLevel),"","",0);
        }catch (Exception e){
            e.printStackTrace();
        }
        return childPartList;
    }
    /*
     * @description:获取快速报价下的成本项目
     * @author: caipan
     * @date: 2025/1/16 15:16:05
     * @param: * @param[1] context
     * @param[2] args
     * @return:
     **/
    public MapList getCostProject(Context context,String[] args) throws Exception{
        Map map = JPO.unpackArgs(args);
        String objectId = UIUtil.getValue(map, "objectId");
        LOGGER.info("map:{},objectId：{}",map,objectId);
        DomainObject obj = DomainObject.newInstance(context,objectId);
        StringList selList = JF_Util_mxJPO.basicBolistSel();
        StringList relList = JF_Util_mxJPO.basicRellistSel();
        String where = null;
        MapList mapList = obj.getRelatedObjects(
                context,
                JF_PLMConstants_mxJPO.rel_JFRapidOffer2CostBreakDown,
                JF_PLMConstants_mxJPO.type_JFCostBreakDown,
                selList,
                relList,
                false,
                true,
                (short) 1,
                "",
                "",
                0);

        return mapList;
    }
    /*
     * @description:获取功能模块库的Interface
     * @author: caipan
     * @date: 2025/1/21 10:37:55
     * @param: * @param[1] context
     * @param[2] args
     * @return:
     **/
    public MapList getCostInterface(Context context,String[] args) throws Exception{
        MapList list = new MapList();
        String mql = "list interface JFCost* select name description dump";
       String result =  MqlUtil.mqlCommand(context, false, mql, true);
       String[] array = result.split("\n");
       for(int i=0;i<array.length;i++){
        Map map = new HashMap();
        StringList interfaceList = FrameworkUtil.split(array[i],",");
        if(interfaceList.size()==2){
            map.put("name",interfaceList.get(0));
            map.put("description",interfaceList.get(1));
            map.put("id",interfaceList.get(0));
            list.add(map);
        }

       }
       LOGGER.info("list:{}",list);
        return list;
    }
    public Vector getInterface(Context context, String[] args)
            throws Exception {
        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        Map columnMap = (Map) programMap.get("columnMap");
        LOGGER.info("columnMap:{}",columnMap);
        String columnName = (String)columnMap.get("name");
        MapList objectList = (MapList) programMap.get("objectList");
        Vector retVector = new Vector();
        if (objectList != null && objectList.size() > 0) {
            Map objectMap = null;
            String displayName = "";
            for (int i = 0; i < objectList.size(); i++) {
                objectMap  = (Map)objectList.get(i);
                displayName = (String)objectMap.get(columnName);
                retVector.add(displayName);
            }
        }
        return retVector;
    }

    /**
     * 获取报价的未参与的制费库
     * @param context
     * @param args
     * @author LIUJR
     * @throws
     * @return matrix.util.StringList
     * @date 2025/1/21 11:03
     * @description
     */
    public StringList getCostNotReferencedCostBreakDown(Context context, String[] args) {
        StringList strCostBreakDownList = new StringList();
        try{
//            HashMap hashMap = new HashMap();
//            //拿取当前报价的制费明细
//            MapList costProject = getCostProject(context, args);
//            costProject.stream().forEach(m -> {
//                Map map = (Map) m;
//                String title = UIUtil.getValue(map, DomainConstants.SELECT_ATTRIBUTE_TITLE);
//                String id = UIUtil.getValue(map, DomainConstants.SELECT_ID);
//                hashMap.put(title, id);
//            });
//            LOGGER.info("hashMap:{}", hashMap.toString());
            //所有的制费库
            MapList manufactureFeeLib = getManufactureFeeLib(context, args);
            LOGGER.info("manufactureFeeLib:{}", manufactureFeeLib.toString());
//            manufactureFeeLib.stream().forEach(m -> {
//                Map map = (Map)m;
//                String title = UIUtil.getValue(map, DomainConstants.SELECT_ATTRIBUTE_TITLE);
//                if (!hashMap.containsKey(title)) {
//                    strCostBreakDownList.add(UIUtil.getValue(map, DomainConstants.SELECT_ID));
//                }
            strCostBreakDownList = (StringList) manufactureFeeLib.stream().map(m -> {
                Map map = (Map) m;
                return UIUtil.getValue(map, DomainConstants.SELECT_ID);
            }).collect(Collectors.toCollection(StringList::new));
        }catch (Exception e) {
            e.printStackTrace();
        }
        LOGGER.info("strCostBreakDownList:{}", strCostBreakDownList);
        return strCostBreakDownList;
    }

    /**
     *  查询制费库和费用明细的集合
     * @param
     * @author LIUJR
     * @throws
     * @return matrix.util.StringList
     * @date 2025/1/21 13:40
     * @description
     */
    public StringList getSelectCostBreakDownAttributes() {
        StringList busSelectList = new StringList();
        busSelectList.add(DomainConstants.ATTRIBUTE_TITLE);
        busSelectList.add(JF_PLMConstants_mxJPO.ATTR_JF_OEMName);
        busSelectList.add(JF_PLMConstants_mxJPO.ATTR_JF_FreeProject);
        busSelectList.add(JF_PLMConstants_mxJPO.ATTR_JF_Annual);
        busSelectList.add(JF_PLMConstants_mxJPO.ATTR_JF_FreeLife);
//        busSelectList.add(JF_PLMConstants_mxJPO.ATTR_JF_assemblyTotalPrice);
//        busSelectList.add(JF_PLMConstants_mxJPO.ATTR_JF_assemblyQualityPrice);
//        busSelectList.add(JF_PLMConstants_mxJPO.ATTR_JF_assemblyStartPrice);
//        busSelectList.add(JF_PLMConstants_mxJPO.ATTR_JF_assemblyRDPrice);
//        busSelectList.add(JF_PLMConstants_mxJPO.ATTR_JF_assemblyLogisticsPrice);
        busSelectList.add(JF_PLMConstants_mxJPO.ATTR_JF_assemblyTimePrice);
        busSelectList.add(JF_PLMConstants_mxJPO.ATTR_JF_assemblyManufacturingPrice);
//        busSelectList.add(JF_PLMConstants_mxJPO.ATTR_JF_FoamTotalPrice);
        busSelectList.add(JF_PLMConstants_mxJPO.ATTR_JF_FoamManufacturingPrice);
//        busSelectList.add(JF_PLMConstants_mxJPO.ATTR_JF_TrimTotalPrice);
        busSelectList.add(JF_PLMConstants_mxJPO.ATTR_JF_TrimTotalTimePrice);
        busSelectList.add(JF_PLMConstants_mxJPO.ATTR_JF_TrimTotalManufacturingPrice);
        busSelectList.add("JF_FoamModesNumber");
        return busSelectList;
    }

    /**
     * 添加参考项
     * @param context
     * @param args
     * @author LIUJR
     * @throws
     * @return java.lang.Boolean
     * @date 2025/1/21 14:03
     * @description
     */
    public String AddCostBreakDownCreate(Context context, String[] args) throws Exception {
        LOGGER.info("!!!!!!!!!!!AddCostBreakDownCreate");
        String mess = DomainConstants.EMPTY_STRING;
        try{
            ContextUtil.startTransaction(context, true);
            Map map = (Map) JPO.unpackArgs(args);
            String objectId = (String) map.get(JF_PLMConstants_mxJPO.STRING_OBJECTID);
            StringList selectedBuildIds = (StringList) map.get("selectedBuildIds");
            StringList selectCostBreakDownAttributes = getSelectCostBreakDownAttributes();
            StringList newIds = new StringList();
            for (String selectId : selectedBuildIds) {
                DomainObject domainObject = DomainObject.newInstance(context, selectId);
                AttributeList attributeValues = domainObject.getAttributeValues(context, selectCostBreakDownAttributes);
                String description = domainObject.getDescription(context);
                String newCostBreakDownId = FrameworkUtil.autoName(context, "type_JFCostBreakDown", "policy_JFCostBreakDown");
                DomainObject costBreakObject = DomainObject.newInstance(context, newCostBreakDownId);
                costBreakObject.setAttributeValues(context, attributeValues);
                costBreakObject.setDescription(context, description);
                newIds.add(newCostBreakDownId);
            }
            DomainObject rapidOfferObject = DomainObject.newInstance(context, objectId);
            DomainRelationship.connect(
                    context,
                    rapidOfferObject,
                    JF_PLMConstants_mxJPO.rel_JFRapidOffer2CostBreakDown,
                    true,
                    newIds.toStringArray());
            mess = ComponentsUIUtil.getI18NString(context, context.getLocale().toString(), "emxComponents.CostBreakDown.CreateSuccess", new String[]{});
            ContextUtil.commitTransaction(context);
        }catch (Exception e) {
            e.printStackTrace();
            mess = ComponentsUIUtil.getI18NString(context, context.getLocale().toString(), "emxComponents.CostBreakDown.CreateFailed", new String[]{});
            ContextUtil.abortTransaction(context);
        }
        return mess;
    }

    /**
    *
    *@description 构建模版清单中得编辑列只有功能组件才可编辑
    *@param context
	*@param args
    *@return matrix.util.StringList
    *@throws
    *@author CHENYAN
    *@date 2025/1/22 14:10
    */
    public StringList getCostEditCol(Context context,String[] args) throws Exception{
        String strLoginUser = context.getUser();
        String strTitle = EnoviaResourceBundle.getProperty(context, "emxFrameworkStringResource", context.getLocale(), "emxFramework.TableEdit.Edit");
        String strColHtml = "<a href=\"javascript:openEditObj('$1',$3)\">"+
        "<img border='0' src='../common/images/iconActionEdit.png' alt=\"$2\" title=\"$2\"></img></a>&#160;";
        Map argMaps = JPO.unpackArgs(args);
        String strRapidOfferId = (String) argMaps.get("objectId");
        if (UIUtil.isNullOrEmpty(strRapidOfferId)){
            Map paramList = (Map) argMaps.get("paramList");
            LOGGER.info("paramList;{}",paramList);
            strRapidOfferId = (String) paramList.get("parentOID");
        }
        MapList argMapList = (MapList) argMaps.get(JF_PLMConstants_mxJPO.STRING_OBJECTLIST);

        DomainObject rapidOffer = DomainObject.newInstance(context, strRapidOfferId);
        Map selectMap = rapidOffer.getInfo(context, StringList.create(SELECT_CURRENT,SELECT_OWNER));
        String strCurrent = (String) selectMap.get(SELECT_CURRENT);
        String strOwner = (String) selectMap.get(SELECT_OWNER);
        StringList res = new StringList();
        for (int i = 0; i < argMapList.size(); i++) {
            Map infoMap = (Map) argMapList.get(i);
            String strId = (String) infoMap.get(SELECT_ID);
            String strType = (String) infoMap.get(SELECT_TYPE);
            String strObjColHtml = "";
            if (JF_PLMConstants_mxJPO.type_JFFunctionModule.equals(strType)){
                strObjColHtml = strColHtml.replace("$1",strId).replace("$2",strTitle);
                String strEditFlag = "0" ;
                if ("Review".equals(strCurrent) && strLoginUser.equals(strOwner)){
                     strEditFlag = "1" ;
                }
                strObjColHtml = strObjColHtml.replace("$3",strEditFlag);

            }
            res.add(strObjColHtml);
        }
        return res;
    }

    /**
     * 获取快速报价的所有版本
     * @param context
     * @param args
     * @author LIUJR
     * @throws
     * @return com.matrixone.apps.domain.util.MapList
     * @date 2025/1/22 10:32
     * @description
     */
    public MapList getRapidOfferVersions(Context context, String[] args) {
        MapList mapList = new MapList();
        try {
            Map map = JPO.unpackArgs(args);
            String objectId = UIUtil.getValue(map, "objectId");
            LOGGER.info("map:{},objectId：{}", map, objectId);
            DomainObject obj = DomainObject.newInstance(context, objectId);
            BusinessObjectList revisions = obj.getRevisions(context);
            for (int i = 0; i < revisions.size(); i++) {
                BusinessObject businessObject = revisions.get(i);
                String strId = businessObject.getObjectId(context);
                Map<String, String> map1 = new HashMap<>();
                map1.put(DomainConstants.SELECT_ID, strId);
                mapList.add(map1);
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
        return mapList;
    }

    /**
    * 设置对象或者关系的owner
    * @param context
	* @param oldObjectId
	* @param newObjectId
	* @param type
    * @author LIUJR
    * @throws
    * @return void
    * @date 2025/3/6 15:27
    * @description
    */
    public static void setOwnerAndProjectOran(Context context, String oldObjectId, String newObjectId, String type) {
        try {
            if ("bus".equalsIgnoreCase(type)) {
                DomainObject oldObject = DomainObject.newInstance(context, oldObjectId);
                DomainObject newObject = DomainObject.newInstance(context, newObjectId);
                String owner = oldObject.getInfo(context, SELECT_OWNER);
                String organization = JF_Util_mxJPO.getPersonOrganization(context, owner);
                // 设置owner
                newObject.setOwner(context, owner);
                // 设置ECO的协作区和组织
                if (UIUtil.isNotNullAndNotEmpty(organization)) {
                    newObject.setPrimaryOwnership(context, "JFSeat", organization);
                }
            } else {
                DomainRelationship domainRelationship = DomainRelationship.newInstance(context, oldObjectId);
                String mql = "print connection '"+oldObjectId+"' select owner dump ";
                String owner = MqlUtil.mqlCommand(context,false , mql, true);
                String organization = JF_Util_mxJPO.getPersonOrganization(context, owner);
                if (UIUtil.isNotNullAndNotEmpty(organization)) {
                    MqlUtil.mqlCommand(context, "mod connection $1 owner $2 project  $3  organization $4;", newObjectId , owner, "JFSeat", organization);//暂时注释 貌似可以自动增加
                } else {
                    MqlUtil.mqlCommand(context, "mod connection $1 owner $2;", newObjectId , owner);//暂时注释 貌似可以自动增加
                }
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 快速报价升版
     * @param context
     * @param args
     * @author LIUJR
     * @throws
     * @return java.lang.Boolean
     * @date 2025/1/21 14:03
     * @description
     */
    public String setRapidOfferRevise(Context context, String[] args) throws Exception {
        LOGGER.info("!!!!!!!!!!!setRapidOfferRevise");
        String mess = DomainConstants.EMPTY_STRING;
        try{
            ContextUtil.pushContext(context);
            ContextUtil.startTransaction(context, true);
            Map paramsMap = (Map) JPO.unpackArgs(args);
            StringList objectIdList = (StringList) paramsMap.get("objectIdList");
            String rel = JF_PLMConstants_mxJPO.rel_JFOffer2JFCostConfig
                    + "," +
                    JF_PLMConstants_mxJPO.rel_JFRapidOffer2CostBreakDown;
            String type = JF_PLMConstants_mxJPO.type_JFCostConfig
                    + "," +
                    JF_PLMConstants_mxJPO.type_JFCostBreakDown;
            for (String objectId : objectIdList) {
                DomainObject domainObject = DomainObject.newInstance(context, objectId);
                //报价升版
                BusinessObject newRapidOffer = domainObject.reviseObject(context, domainObject.getNextSequence(context), false);
                String newRapidOfferId = newRapidOffer.getObjectId(context);
                //修改owner， 组织协作区
                setOwnerAndProjectOran(context, objectId, newRapidOfferId, "bus");
                //快速报价 关系查找
                StringList selList = JF_Util_mxJPO.basicBolistSel();
                StringList relList = JF_Util_mxJPO.basicRellistSel();
                MapList mapList = domainObject.getRelatedObjects(
                        context,
                        rel,
                        type,
                        selList,
                        relList,
                        false,
                        true,
                        (short) 1,
                        "",
                        "",
                        0
                );
                Iterator iterator = mapList.iterator();
                while (iterator.hasNext()) {
                    Map map = (Map) iterator.next();
                    String id = UIUtil.getValue(map, DomainConstants.SELECT_ID);
                    String connId = UIUtil.getValue(map, DomainConstants.SELECT_RELATIONSHIP_ID);
                    String strType = UIUtil.getValue(map, DomainConstants.SELECT_TYPE);
                    String strConnRel = EMPTY_STRING;
                    if (JF_PLMConstants_mxJPO.type_JFCostConfig.equalsIgnoreCase(strType)) {
                        strConnRel = JF_PLMConstants_mxJPO.rel_JFOffer2JFCostConfig;
                    } else {
                        strConnRel = JF_PLMConstants_mxJPO.rel_JFRapidOffer2CostBreakDown;
                    }
                    setNewObjectRevision(context, connId, id, strType, strConnRel, newRapidOfferId);
                }
            }
            mess = ComponentsUIUtil.getI18NString(context, context.getLocale().toString(), "emxComponents.RapidOffer.reviseSuccess", new String[]{});
            ContextUtil.commitTransaction(context);
        }catch (Exception e) {
            e.printStackTrace();
            ContextUtil.popContext(context);
            mess = ComponentsUIUtil.getI18NString(context, context.getLocale().toString(), "emxComponents.RapidOffer.reviseFailed", new String[]{});
            ContextUtil.abortTransaction(context);
        }
        return mess;
    }

    /**
     * 递归方法 升版时候
     * @param context
     * @param objectId  升版的对象
     * @param strType 该升版对象的类型
     * @param strConnRel 该升版对象与父对象关联的关系
     * @param parentId 父对象id
     * @author LIUJR
     * @throws
     * @return void
     * @date 2025/1/22 15:15
     * @description
     */
    public void setNewObjectRevision(Context context, String connId, String objectId, String strType, String strConnRel, String parentId) throws Exception{
        DomainObject domainObject = DomainObject.newInstance(context, objectId);
        BusinessObject newDomainObject = domainObject.reviseObject(context, domainObject.getNextSequence(context), false);
        StringList busAttributeList = new StringList();
        String rel = EMPTY_STRING;
        String type = EMPTY_STRING;
        switch (strType) {
            case JF_PLMConstants_mxJPO.type_JFCostConfig: {
                rel = JF_PLMConstants_mxJPO.rel_JFModule2Module;
                type = JF_PLMConstants_mxJPO.type_JFModules;
                break;
            }
            case JF_PLMConstants_mxJPO.type_JFModules: {
                rel = JF_PLMConstants_mxJPO.rel_JFModule2ReferConst;
                type = JF_PLMConstants_mxJPO.type_JFCostReferConst;
                break;
            }
            case JF_PLMConstants_mxJPO.type_JFCostReferConst: {
                rel = JF_PLMConstants_mxJPO.rel_JFCostReferConst2Function;
                type = JF_PLMConstants_mxJPO.type_JFFunctionModule;
                break;
            }
        }
        //复制属性和描述
        AttributeList attributeValues = domainObject.getAttributeValues(context, busAttributeList);
        newDomainObject.setAttributes(context, attributeValues);
        newDomainObject.setDescription(context, domainObject.getDescription(context));
        //与上级关联关系
        DomainObject parentObject = DomainObject.newInstance(context, parentId);
        String newObjectId = newDomainObject.getObjectId(context);
        setOwnerAndProjectOran(context, objectId, newObjectId, "bus");
        DomainRelationship domainRelationship = parentObject.addToObject(context, new RelationshipType(strConnRel), newObjectId);
        setOwnerAndProjectOran(context, connId, domainRelationship.getPhysicalId(context), "rel");
        if (JF_PLMConstants_mxJPO.type_JFCostBreakDown.equalsIgnoreCase(strType)) {
            return;
        }
        //查询下一级
        StringList selList = JF_Util_mxJPO.basicBolistSel();
        StringList relList = JF_Util_mxJPO.basicRellistSel();
        MapList mapList = domainObject.getRelatedObjects(
                context,
                rel,
                type,
                selList,
                relList,
                false,
                true,
                (short) 1,
                "",
                "",
                0
        );
        Iterator iterator = mapList.iterator();
        while (iterator.hasNext()) {
            Map map = (Map) iterator.next();
            String id = UIUtil.getValue(map, DomainConstants.SELECT_ID);
            String relConnId = UIUtil.getValue(map, DomainConstants.SELECT_RELATIONSHIP_ID);
            setNewObjectRevision(context, relConnId, id, type, rel, newObjectId);
        }
    }

    /**
    * 权限：当前报价是否已经升版
    * @param context
	* @param args
    * @author LIUJR
    * @throws
    * @return java.lang.Boolean
    * @date 2025/1/22 16:58
    * @description
    */
    public Boolean getRapidOfferHasRevise(Context context, String[] args) {
        Boolean flag = Boolean.TRUE;
        try {
            Map paramsMap = (Map) JPO.unpackArgs(args);
            String objectId = (String) paramsMap.get("objectId");
            DomainObject domainObject = DomainObject.newInstance(context, objectId);
            String nextSequence = domainObject.getNextSequence(context);
            String typeName = domainObject.getTypeName(context);
            String name = domainObject.getInfo(context, SELECT_NAME);
            String where = "name=='" + name +"'&&revision=='" + nextSequence + "'";
            String id = JF_PublicMethodClass_mxJPO.findObject(context, typeName, where);
            if (UIUtil.isNotNullAndNotEmpty(id)) {
                flag = Boolean.FALSE;
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
        return flag;
    }

    /**
    *
    *@description 构造快速报价中编辑表单动态列
    *@param context
	*@param args
    *@return com.matrixone.apps.domain.util.MapList
    *@throws
    *@author CHENYAN
    *@date 2025/2/5 11:46
    */
    public MapList getJFFunctionModuleDynamicField(Context context ,String[] args ) throws Exception{
        MapList colMapList = new MapList();
        HashMap programMap = (HashMap)JPO.unpackArgs(args);
        HashMap requestMap = (HashMap)programMap.get("requestMap");
        String strObjectId = (String)requestMap.get("objectId");
        DomainObject bo = DomainObject.newInstance(context, strObjectId);
        //获取该对象上面的interface
        BusinessInterfaceList interfaceList = bo.getBusinessInterfaces(context);
        //分组起始位置
        int GroupIndex = 3 ;
        //计数属性
        int attrCount = 0 ;
        String strGroupName = "";
        try {
            if (interfaceList.size() > 0){
                for (int i = 0; i < interfaceList.size(); i++) {
                    BusinessInterface anInterface = interfaceList.get(i);
                    String strInterfaceName = anInterface.getName();
                    //计算评估价格需要的列展示在form里面
                    getExtraColumnByInterface(context,colMapList,strInterfaceName);
                    AttributeTypeList attributeTypes = anInterface.getAttributeTypes(context);
                    for (int i1 = 0; i1 < attributeTypes.size(); i1++) {
                        Map<Object, Object> colMap = new HashMap<>();
                        Map settingsMap = new HashMap<>();
                        AttributeType attributeType = attributeTypes.get(i1);
                        //属性名称
                        String strAttrName = attributeType.getName();
                        LOGGER.info("strAttrName111111:{}",strAttrName);
                        //属性类型
//                        String strAttrType = attributeType.getDataType(); //此API报错无返回
                        String strAttrType = attributeType.getDataType(context);
                        LOGGER.info("strAttrName:{}",strAttrName);
                        LOGGER.info("strAttrType:{}",strAttrType);
                        //国际化
                        String strLabel = JF_PublicMethodClass_mxJPO.buildStringInStrings("emxFramework.Attribute.",strAttrName);
                        String strAdminType = JF_PublicMethodClass_mxJPO.buildStringInStrings("attribute_",strAttrName);
                        String strFieldType = "attribute";
                        String strInputType = "textbox";
                        String strRegisteredSuite = "Framework";
//                        if ((attrCount % 2) == 0){
//                            GroupIndex++;
//                            strGroupName = "g"+GroupIndex;
//                        }
                        String strEditable = "true";
                        String strExpressionBusinessObject = "attribute[$1].value";
                        strExpressionBusinessObject = strExpressionBusinessObject.replace("$1",strAttrName);
                        //属性为日期特殊处理
                        if ("date".equals(strAttrType)){
                            settingsMap.put("Show Clear Button","true");
                            settingsMap.put("format","date");
                        }
                        if ("real".equals(strAttrType)){
                            settingsMap.put("Column Style","left-align");
                        }
                        settingsMap.put("interfaceName",strInterfaceName);
                        colMap.put("name",strAttrName);
                        colMap.put("label",strLabel);
                        //更新方法
    //                    settingsMap.put("Update Function","");
    //                    settingsMap.put("Update Program","");
    //                    settingsMap.put("Edit Access Function","");
    //                    settingsMap.put("Edit Access Program","");
                        settingsMap.put("Admin Type",strAdminType);
                        settingsMap.put("Input Type",strInputType);
                        settingsMap.put("Field Type",strFieldType);
                        settingsMap.put("Editable",strEditable);
//                        settingsMap.put("Group Name",strGroupName);
                        settingsMap.put("Registered Suite",strRegisteredSuite);
                        colMap.put("expression_businessobject", strExpressionBusinessObject);
                        colMap.put("settings", settingsMap);
                        LOGGER.info("colMap:{}",colMap);
                        colMapList.add(colMap);
                        attrCount++;
                    }
                }
            }
        } catch (MatrixException e) {
            throw new RuntimeException(e);
        }
        return colMapList;
    }

    public void getExtraColumnByInterface(Context context, MapList colMapList, String strInterfaceName) throws Exception{
        if ("JFCostFoamMaterials".equals(strInterfaceName)){
            //发泡
            Map<Object, Object> colMap = new HashMap<>();
            Map settingsMap = new HashMap<>();
            getExtraColumnCommonSettings(colMap,settingsMap,strInterfaceName);
            colMap.put("name","JF_FucFoamTotalWeight1");
            colMap.put("label","emxFramework.Attribute.JF_FucFoamTotalWeight1");
            colMapList.add(colMap);
        }else if ("JFCostCommonPlasticParts".equals(strInterfaceName)){
            //普通塑料件
            Map<Object, Object> colMap = new HashMap<>();
            Map settingsMap = new HashMap<>();
            getExtraColumnCommonSettings(colMap,settingsMap,strInterfaceName);
            colMap.put("name","JF_FucComPlasticNumber1");
            colMap.put("label","emxFramework.Attribute.JF_FucComPlasticNumber1");
            colMapList.add(colMap);
            Map<Object, Object> colMap1 = new HashMap<>();
            Map settingsMap1 = new HashMap<>();
            settingsMap1.put("interfaceName",strInterfaceName);
            getExtraColumnCommonSettings(colMap1,settingsMap1,strInterfaceName);
            colMap1.put("name","JF_FucComPlasticWeight1");
            colMap1.put("label","emxFramework.Attribute.JF_FucComPlasticWeight1");
            colMapList.add(colMap1);
        }else if ("JFCostOtherMetalParts".equals(strInterfaceName)){
            //其他金属件
            Map<Object, Object> colMap = new HashMap<>();
            Map settingsMap = new HashMap<>();
            getExtraColumnCommonSettings(colMap,settingsMap,strInterfaceName);
            colMap.put("name","JF_FucOthMetalWeight1");
            colMap.put("label","emxFramework.Attribute.JF_FucOthMetalWeight1");
            colMapList.add(colMap);
        }else if ("JFCostWiringHarness".equals(strInterfaceName)){
            //线束
            Map<Object, Object> colMap = new HashMap<>();
            Map settingsMap = new HashMap<>();
            getExtraColumnCommonSettings(colMap,settingsMap,strInterfaceName);
            colMap.put("name","JF_FucHarnessLoopsNB1");
            colMap.put("label","emxFramework.Attribute.JF_FucHarnessLoopsNB1");
            colMapList.add(colMap);
        }
    }

    public void getExtraColumnCommonSettings(Map colMap, Map settingsMap, String strInterfaceName) {
        settingsMap.put("interfaceName",strInterfaceName);
        settingsMap.put("Editable","false");
        settingsMap.put("function","getExtraColumnAttributeValue");
        settingsMap.put("program","JF_Cost");
        settingsMap.put("Registered Suite","Framework");
        settingsMap.put("Field Type","program");
        colMap.put("settings", settingsMap);
    }

    public String getExtraColumnAttributeValue(Context context,String[] args)throws Exception{
        Map paramMap = (Map) JPO.unpackArgs(args);
        Map requestMap = (Map) paramMap.get("requestMap");
        Map fieldMap = (Map) paramMap.get("fieldMap");
        String name = (String)fieldMap.get("name");
        String objectId = (String)requestMap.get("objectId");
        DomainObject object = DomainObject.newInstance(context,objectId);
        String refId = object.getInfo(context,"to[JFCostReferConst2Function].from.id");
        DomainObject refObject = DomainObject.newInstance(context,refId);
        String costId = refObject.getInfo(context, "from[JFCostReferConst2Cost].to.id");
        DomainObject costObject = DomainObject.newInstance(context,costId);
        if ("JF_FucFoamTotalWeight1".equals(name)){
           return costObject.getAttributeValue(context,"JF_FucFoamTotalWeight");
        }else if ("JF_FucComPlasticNumber1".equals(name)) {
            return costObject.getAttributeValue(context,"JF_FucComPlasticNumber");
        }else if ("JF_FucComPlasticWeight1".equals(name)) {
            return costObject.getAttributeValue(context,"JF_FucComPlasticWeight");
        }else if ("JF_FucOthMetalWeight1".equals(name)) {
            return costObject.getAttributeValue(context,"JF_FucOthMetalWeight");
        }else if ("JF_FucHarnessLoopsNB1".equals(name)) {
            return costObject.getAttributeValue(context,"JF_FucHarnessLoopsNB");
        }
        return "";
    }

    /**
    * 导出报价
    * @param context
	* @param args
    * @author LIUJR
    * @throws
    * @return java.util.Map
    * @date 2025/2/6 11:26
    * @description
    */
    public Map exportFormList(Context context, String[] args) throws Exception {
        Map resMap = new HashMap();
        Boolean isPush = Boolean.FALSE;
        //整椅
        //拿到文件模板，找到sheet1的Configuration，打开并另存为文件
        try {
            ContextUtil.pushContext(context);
            isPush = Boolean.TRUE;
            String classPath = Thread.currentThread().getContextClassLoader().getResource("").getPath();
            //文件地址
            String fileTemPath = classPath.substring(0, classPath.indexOf("WEB-INF"));
            String filePath = JF_ECRService_mxJPO.getTemplatePath("ExportCostTemplate", fileTemPath);
            LOGGER.info("filePath:{}", filePath);
            InputStream inputStream = new FileInputStream(filePath);
            Workbook workbook = WorkbookFactory.create(inputStream);
            Sheet sheet = workbook.getSheetAt(0);
            LOGGER.info("sheet:{}", sheet.getSheetName());
            //报价id
            String objectId = args[0];
            DomainObject domainObject = DomainObject.newInstance(context);
            domainObject.setId(objectId);
            String costTitle = domainObject.getInfo(context, SELECT_ATTRIBUTE_TITLE);
            costTitle = UIUtil.isNullOrEmpty(costTitle) ? domainObject.getInfo(context, SELECT_NAME) : costTitle;
            //获取每个分类的填写的行
            Map exportRowCloumnMap = getExportRowCloumnMap();
            Map<Integer, String> costClassMap = (Map<Integer, String>)exportRowCloumnMap.get("costClassMap");
            Map<String, Integer> settingMap = (Map<String, Integer>)exportRowCloumnMap.get("settingMap");
            Integer  iMinorHeading = settingMap.get("MinorHeading");
            Integer  iEndRow = settingMap.get("endRow");
            Integer  iStartCell = settingMap.get("StartCell");
            Integer  iTitleRow  = settingMap.get("titleRow");
            Integer  iTtleCell = settingMap.get("titleCell");
            String  strAssyItem = (String) exportRowCloumnMap.get("AssyItem");
            String  strConfigItem = (String) exportRowCloumnMap.get("ConfigItem");
            //查询集合
            StringList basicBoListSel = JF_Util_mxJPO.basicBolistSel();
            basicBoListSel.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JF_CostClass);
            basicBoListSel.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JF_EvaluatePrice);
            basicBoListSel.add(DomainConstants.SELECT_ORIGINATED);
            StringList basicRelLstSel = JF_Util_mxJPO.basicRellistSel();
            //拿取报价下的配置   rel: JFOffer2JFCostConfig  type: JFCostConfig
            StringList costConfigList = domainObject.getInfoList(context, "from[JFOffer2JFCostConfig].to.id");
            //存储写入数据
            MapList assyMapList = new MapList();
            MapList configureMapList = new MapList();
            String title = EMPTY_STRING;
            for (String costConfigId : costConfigList) {
                domainObject.setId(costConfigId);
                HashMap<String, Object> configureMap = new HashMap<>();
                String info = domainObject.getInfo(context, SELECT_ATTRIBUTE_TITLE);
                //配置的title
                title = UIUtil.isNullOrEmpty(info)
                        ? domainObject.getInfo(context, SELECT_NAME)
                        : info;
                configureMap.put("Title", title);
                //拿取配置下的报价整椅  rel: JFModule2Module type: JFModules
                MapList modulesList = domainObject.getRelatedObjects(
                        context,
                        JF_PLMConstants_mxJPO.rel_JFModule2Module,
                        JF_PLMConstants_mxJPO.type_JFModules,
                        basicBoListSel,
                        basicRelLstSel,
                        false,
                        true,
                        (short) 1,
                        "",
                        "",
                        0
                );
                //按照name进行排序
                modulesList.addSortKey(SELECT_NAME, ProgramCentralConstants.ASCENDING_SORT, ProgramCentralConstants.SORTTYPE_STRING);
                modulesList.sort();
                //拿取整椅下的参考项目  rel: JFModule2ReferConst type: JFCostReferConst
                //拿取项目下的功能组件  rel: JFCostReferConst2Function type: JFFunctionModule   attr: JF_CostClass,JF_EvaluatePrice
                Iterator iterator = modulesList.iterator();
                while (iterator.hasNext()) {
                    Map moduleMap = (Map) iterator.next();
                    String id = UIUtil.getValue(moduleMap, SELECT_ID);
                    HashMap<String, Object> assyMap = new HashMap<>();
                    title = UIUtil.getValue(moduleMap, SELECT_ATTRIBUTE_TITLE);
                    title = UIUtil.isNullOrEmpty(title)
                            ? domainObject.getInfo(context, SELECT_NAME)
                            : title;
                    assyMap.put("Title", title);
                    domainObject.setId(id);
                    MapList functionModuleMapList = domainObject.getRelatedObjects(
                            context,
                            JF_PLMConstants_mxJPO.rel_JFModule2ReferConst + "," + JF_PLMConstants_mxJPO.rel_JFCostReferConst2Function,
                            "*",
                            basicBoListSel,
                            basicRelLstSel,
                            false,
                            true,
                            (short) 0,
                            "",
                            "",
                            0
                    );
                    functionModuleMapList = (MapList) functionModuleMapList.stream().filter(m -> {
                        Map map = (Map) m;
                        String type = UIUtil.getValue(map, SELECT_TYPE);
                        if (JF_PLMConstants_mxJPO.type_JFFunctionModule.equalsIgnoreCase(type)) {
                            return true;
                        } else{
                            return false;
                        }
                    }).collect(Collectors.toCollection(MapList::new));
                    //将功能组件分组  按照分类分组
                    Map groupMap = (Map) functionModuleMapList.stream().collect(Collectors.groupingBy(m -> {
                        Map map = (Map) m;
                        return map.get(JF_PLMConstants_mxJPO.SELECT_ATTR_JF_CostClass);
                    }));
                    for (Object oEntry : groupMap.entrySet()) {
                        Map.Entry entry = (Map.Entry) oEntry;
                        //分类
                        String strCostClass = (String) entry.getKey();
                        List sunMapList = (List) entry.getValue();
                        // 使用Stream API计算总价
                        BigDecimal totalPrice = (BigDecimal) sunMapList.stream()
                                .filter(map -> {
                                    boolean filterRes = false;
                                    Map map1 = (Map) map;
                                    if (map1.containsKey(JF_PLMConstants_mxJPO.SELECT_ATTR_JF_EvaluatePrice)) {
                                        String strPrice = (String) map1.get(JF_PLMConstants_mxJPO.SELECT_ATTR_JF_EvaluatePrice);
                                        if (UIUtil.isNotNullAndNotEmpty(strPrice)) {
                                            filterRes = strPrice.matches("^-?\\d+(\\.\\d+)?$");
                                        }
                                    }
                                    return filterRes;
                                }).map(map -> {
                                    Map map1 = (Map) map;
                                    BigDecimal bigDecimal;
                                    String strPrice = (String) map1.get(JF_PLMConstants_mxJPO.SELECT_ATTR_JF_EvaluatePrice);
                                    bigDecimal = new BigDecimal(strPrice);
                                    return bigDecimal;
                                })
                                .reduce(BigDecimal.ZERO, (a, b) -> {
                                    BigDecimal aBigDecimal = (BigDecimal) a;
                                    BigDecimal bBigDecimal = (BigDecimal) b;
                                    BigDecimal sum = aBigDecimal.add(bBigDecimal);
                                    return sum;
                                });
                        double db = totalPrice.setScale(4, BigDecimal.ROUND_HALF_UP).doubleValue();
                        if (configureMap.containsKey(strCostClass)) {
                            Double aDouble = (Double) configureMap.get(strCostClass);
                            aDouble += db;
                            configureMap.put(strCostClass, aDouble);
                        } else {
                            configureMap.put(strCostClass, db);
                        }
                        assyMap.put(strCostClass, db);
                    }
                    assyMapList.add(assyMap);
                }
                configureMapList.add(configureMap);
            }
            LOGGER.info("!!!!!!!!!!!!!!!!!!开始写入文件!!!!!!!!!!!!!!!!!!!");
            LOGGER.info("assyMapList:{}", assyMapList);
            LOGGER.info("configureMapList:{}", configureMapList);
            LOGGER.info("costClassMap:{}", costClassMap);
            //写入报价的title作为标题
            sheet.getRow(iTitleRow).getCell(iTtleCell).setCellValue(costTitle);
            //写入报价整椅
            for (int i = 0;i < assyMapList.size(); i++) {
                Map map = (Map) assyMapList.get(i);
                int iTitle = i + 1;
                Cell cell1 = sheet.getRow(iTitleRow).getCell(iStartCell);
                cell1.setCellType(CellType.STRING);
                cell1.setCellValue(strAssyItem + iTitle);
                //遍历行
                for (int j = iMinorHeading; j <=iEndRow; j++) {
                    if (j == 6) {
                        continue;
                    }
                    Row row = sheet.getRow(j);
                    Cell cell = row.getCell(iStartCell);
                    String classCost = costClassMap.get(j);
                    if ("Title".equalsIgnoreCase(classCost)) {
                        String strTitle = (String) map.get(classCost);
                        cell.setCellType(CellType.STRING);
                        cell.setCellValue(strTitle);
                    } else {
                        if (map.containsKey(classCost)) {
                            Double db = (Double) map.get(classCost);
                            cell.setCellType(CellType.NUMERIC);
                            cell.setCellValue(db);
                        } else {
                            cell.setCellType(CellType.NUMERIC);
                            cell.setCellValue(0);
                        }
                    }
                }
                iStartCell ++;
            }
            //分隔
            iStartCell ++;
            //写入配置层
            for (int i = 0;i < configureMapList.size(); i++) {
                Map map = (Map) configureMapList.get(i);
                int iTitle = i + 1;
                Cell cell1 = sheet.getRow(iTitleRow).getCell(iStartCell);
                cell1.setCellType(CellType.STRING);
                cell1.setCellValue(strConfigItem + iTitle);
                //遍历行
                for (int j = iMinorHeading; j <=iEndRow; j++) {
                    Row row = sheet.getRow(j);
                    Cell cell = row.getCell(iStartCell);
                    String classCost = costClassMap.get(j);
                    if ("Title".equalsIgnoreCase(classCost)) {
                        String strTitle = (String) map.get(classCost);
                        cell.setCellType(CellType.STRING);
                        cell.setCellValue(strTitle);
                    } else {
                        if (map.containsKey(classCost)) {
                            Double db = (Double) map.get(classCost);
                            cell.setCellType(CellType.NUMERIC);
                            cell.setCellValue(db);
                        } else {
                            cell.setCellType(CellType.NUMERIC);
                            cell.setCellValue(0);
                        }
                    }
                }
                iStartCell ++;
            }
            //开始合并
            sheet.setForceFormulaRecalculation(true);
            //导出
            String fileName = ComponentsUIUtil.getI18NString(context, context.getLocale().toString(), "emxComponents.Command.RapidOfferexportCmd", new String[]{});
            resMap.put("file", workbook);
            resMap.put("flag", "Y");
            //获取当前时间
            LocalDateTime now = LocalDateTime.now();
            // 将 LocalDateTime 对象转换成指定格式的字符串
            String strFormattedDate = now.format(formatter);
            //分中英文
            resMap.put("fileName", JF_PublicMethodClass_mxJPO.buildStringInStrings(fileName, strFormattedDate, ".xlsx"));
        }catch (Exception e) {
            resMap.clear();
            resMap.put("file", "");
            resMap.put("fileName", "");
            resMap.put("flag", "N");
            e.printStackTrace();
        } finally {
            if (isPush) {
                try {
                    ContextUtil.popContext(context);
                } catch (FrameworkException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return resMap;
    }


    /**
    * 获取每个分类的行
    * @param
    * @author LIUJR
    * @throws
    * @return java.util.Map
    * @date 2025/3/19 11:30
    * @description
    */
    public Map<String, Object> getExportRowCloumnMap() throws Exception{
        Map<String, Object> map = new HashMap<>();
        Map<Integer, String> costClassMap = new HashMap<>();
        costClassMap.put(24, "A/R");
        costClassMap.put(23, "Latch");
        costClassMap.put(22, "S-Felt");
        costClassMap.put(21, "S-Others");
        costClassMap.put(20, "S-Stamping");
        costClassMap.put(19, "SBR");
        costClassMap.put(18, "Electrics");
        costClassMap.put(17, "S-C Parts");
        costClassMap.put(16, "Heating");
        costClassMap.put(15, "Harness");
        costClassMap.put(14, "Trim Assy");
        costClassMap.put(13, "H/R");
        costClassMap.put(12, "Venting");
        costClassMap.put(11, "Foam Assy");
        costClassMap.put(10, "Lumber");
        costClassMap.put(9, "Metal");
        costClassMap.put(8, "Frame Assy");
        costClassMap.put(7, "Plastic");
        costClassMap.put(5, "Title");
        map.put("costClassMap", costClassMap);
        Map<String, Integer> settingMap = new HashMap<>();
        settingMap.put("MinorHeading", 5);
        settingMap.put("StartCell", 7);
        settingMap.put("titleRow", 3);
        settingMap.put("titleCell", 4);
        settingMap.put("endRow", 24);
        map.put("settingMap", settingMap);
        map.put("AssyItem", "Assy ");
        map.put("ConfigItem", "Configure ");
        return map;
    }

    public MapList getSelectFunctionModuleTemplate(Context context,String[] args) throws Exception{
        Map argsMap = JPO.unpackArgs(args);
        LOGGER.info("argsMap:{}",argsMap);
        String strIds = (String) argsMap.get("ids");
        String[] splitIds = strIds.split(",");
        String strReferProjectId  = "";
        if (splitIds.length > 0){
            strReferProjectId = splitIds[0];
        }
        LOGGER.info("strReferProjectId:{}",strReferProjectId);
        StringList selList = JF_Util_mxJPO.basicBolistSel();
        selList.add("attribute[JF_ConnInterface]");
        selList.add(SELECT_ATTRIBUTE_TITLE);
        DomainObject BO = DomainObject.newInstance(context, strReferProjectId);
        String strRootPartId = BO.getInfo(context, "to[JFModule2ReferConst].from.id");
        //切换成报价整椅
        BO.setId(strRootPartId);
        JF_Util_mxJPO.basicBolistSel();
        StringList typeSelectList = JF_Util_mxJPO.basicBolistSel();
        typeSelectList.add(SELECT_ATTRIBUTE_TITLE);
        MapList objectList = BO.getRelatedObjects(context,
                JF_PLMConstants_mxJPO.rel_JFModule2ReferConst+","+JF_PLMConstants_mxJPO.rel_JFCostReferConst2Function, //pattern to match relationships
                JF_PLMConstants_mxJPO.type_JFCostReferConst+","+JF_PLMConstants_mxJPO.type_JFFunctionModule, //pattern to match types
                typeSelectList, //the eMatrix StringList object that holds the list of select statement pertaining to Business Obejcts.
                JF_Util_mxJPO.basicRellistSel(), //the eMatrix StringList object that holds the list of select statement pertaining to Relationships.
                false, //get To relationships
                true, //get From relationships
                (short) 2, //the number of levels to expand, 0 equals expand all.
                EMPTY_STRING, //where clause to apply to objects, can be empty ""
                EMPTY_STRING, //where clause to apply to relationship, can be empty ""
                (short)0, //limit
                false, //checkHidden
                true, //preventDuplicates
                (short)0, //pageSize
                null,
                null,
                null,
                "end") ;// end(返回叶子节点) relationship(返回关系pattern中最后一个关系) all(全返回);
        String where = null;
        MapList list = DomainObject.findObjects(context, JF_PLMConstants_mxJPO.type_JFFunctionModuleTemplate, "*", where,selList);
        for (int i = 0; i < list.size(); i++) {
            Map funcInfoMap = (Map) list.get(i);
            String strFunTitle = (String) funcInfoMap.get(SELECT_ATTRIBUTE_TITLE);
            Boolean isDisable = Boolean.FALSE ;
            for (int i1 = 0; i1 < objectList.size(); i1++) {
                Map existFunInfoMap = (Map) objectList.get(i1);
                String strExistFunTitle = (String) existFunInfoMap.get(SELECT_ATTRIBUTE_TITLE);
                if (strExistFunTitle.equals(strFunTitle)){
                    isDisable = Boolean.TRUE;
                    break;
                }
            }
            funcInfoMap.put("disableSelection",isDisable.toString());
        }
        LOGGER.info("list:{}",list);
        return list;
    }

    public StringList getAffectedItemsCostTableField(Context context ,String[] args) throws Exception{
        LOGGER.info("-------------------------------  getAffectedItemsCostTableField begin  ---------------------------------------");
        try {
            Map paramsMap = (Map) JPO.unpackArgs(args);
            Map paramList = (Map) paramsMap.get("paramList");
            MapList objectList = (MapList)paramsMap.get("objectList");
            String strIds = (String) paramList.get("ids");
            LOGGER.info("strIds:{}",strIds);
            String[] splitIds = strIds.split(",");
            StringList res = new StringList(objectList.size());
            String strReferProjectId  = "";
            String strMQLSelect = "";
            if (splitIds.length > 0){
                strReferProjectId = splitIds[0];
            }
            if (UIUtil.isNullOrEmpty(strReferProjectId)){
                for (int i = 0; i < res.size(); i++) {
                    res.add(i,"-");
                }
                return res;
            }
            DomainObject referProject = DomainObject.newInstance(context, strReferProjectId);
            //获取模版参考项目
            String strTmpReferProjectId = referProject.getInfo(context, "from[JFCostReferConst2Cost].to.id");
            LOGGER.info("strTmpReferProjectId:{}",strTmpReferProjectId);
            if (UIUtil.isNotNullAndNotEmpty(strTmpReferProjectId)){
                referProject.setId(strTmpReferProjectId);
            }
            Map columnMap = (Map) paramsMap.get("columnMap");
            String strColName = (String) columnMap.get("name");
            LOGGER.info("strColName:{}",strColName);
            switch (strColName){
                case "JF_FuntionOption":{
                    strMQLSelect = "property[function].value" ;
                    break;
                }
                case "JF_ReferPrice":{
                    strMQLSelect = "property[price].value" ;
                    break;
                }
                default:{
                    //设置默认
                    for (int i = 0; i < res.size(); i++) {
                        res.add(i,"-");
                    }
                    return res ;
                }
            }
            for (int i = 0; i < objectList.size(); i++) {
                String strAttrValue = "JF_FuntionOption".equals(strColName) ? "-" : "";
                Map info = (Map) objectList.get(i);
                //获取保存interface
                String strInterfaceName = (String) info.get("attribute[JF_ConnInterface]");
                if (UIUtil.isNotNullAndNotEmpty(strInterfaceName)){
                    //获取interface保存的property信息
                    String strAttrInfo = MqlUtil.mqlCommand(context,false,  JF_PublicMethodClass_mxJPO.buildStringInStrings("print interface '",strInterfaceName,"' select ",strMQLSelect,"  dump  "),true);
                    LOGGER.info("strAttrInfo:{}",strAttrInfo);
                    if (UIUtil.isNotNullAndNotEmpty(strAttrInfo)){
                        strAttrValue = referProject.getInfo(context, JF_PublicMethodClass_mxJPO.buildStringInStrings("attribute[", strAttrInfo, "]"));
                    }
                }
                res.add(strAttrValue);
            }
            LOGGER.info("-------------------------------  getAffectedItemsCostTableField end  ---------------------------------------");
            return res;
        }catch (Exception e){
            LOGGER.info(e.getMessage());
            throw e;
        }
    }

    public StringList getJFEvaluatePriceEditAccess(Context context ,String[] args) throws Exception{
        StringList res = new StringList();
        try {
            String strLoginUser = context.getUser();
            Map tableSettingMap =  JPO.unpackArgs(args);
            Map requestMap = (Map) tableSettingMap.get("requestMap");
            String strRapidOfferId = (String) requestMap.get("objectId");
            if (UIUtil.isNullOrEmpty(strRapidOfferId)){
                Map paramList = (Map) tableSettingMap.get("paramList");
                strRapidOfferId = (String) paramList.get("parentOID");
            }
            MapList objectList = (MapList)tableSettingMap.get("objectList");
            Map columnMap = (Map) tableSettingMap.get("columnMap");
            String strFieldName = (String) columnMap.get("name");
            LOGGER.info("strFieldName:{}",strFieldName);
            DomainObject rapidOffer = DomainObject.newInstance(context, strRapidOfferId);
            Map selectMap = rapidOffer.getInfo(context, StringList.create(SELECT_CURRENT,SELECT_OWNER));
            String strCurrent = (String) selectMap.get(SELECT_CURRENT);
            String strOwner = (String) selectMap.get(SELECT_OWNER);
            for (int i = 0; i < objectList.size(); i++) {
                Boolean isEdit = Boolean.FALSE ;
                Map info = (Map) objectList.get(i);
                String strType = (String) info.get(SELECT_TYPE);
                if ("Title".equals(strFieldName)){
                    LOGGER.info("strType:{}",strType);
                    if ("JFFunctionModule".equals(strType) || "JFCostReferConst".equals(strType)){
                        isEdit = Boolean.FALSE;
                    }else {
                        if (strLoginUser.equals(strOwner) && "Review".equals(strCurrent)){
                            isEdit = Boolean.TRUE;
                        }
                    }
                }else if ("Description".equals(strFieldName)){
                    if ("Review".equals(strCurrent) && strLoginUser.equals(strOwner)){
                        isEdit = Boolean.TRUE;
                    }
                } else {
                    if ("JFFunctionModule".equals(strType)){
                        if ("Review".equals(strCurrent) && strLoginUser.equals(strOwner)){
                            isEdit = Boolean.TRUE;
                        }
                    }
                }
                res.add(isEdit.toString());
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
            throw new RuntimeException(e);
        }
        return res;
    }

    /**
     *
     *@description 获取评估价格列数据
     *@param context
     *@param args
     *@return matrix.util.StringList
     *@throws
     *@author CHENYAN
     *@date 2025/2/14 14:15
     */
    public StringList getJFEvaluatePriceValueField(Context context ,String[] args) throws Exception{
        LOGGER.info("--------------------------------  getJFEvaluatePriceValueField begin --------------------------------------------------");
        Map tableSettingMap =  JPO.unpackArgs(args);
        StringList res = new StringList();
        Map paramList = (Map) tableSettingMap.get("paramList");
        String strSelectedTable = (String) paramList.get("table");
        //前台调用刷新js时program参数不为空 防止刷新时objectList为旧数据
        String strProgram = (String) paramList.get("program");
        boolean isJsFlush = UIUtil.isNotNullAndNotEmpty(strProgram);
        LOGGER.info("strProgram:{}",strProgram);
        LOGGER.info("strSelectedTable:{}",strSelectedTable);
        MapList objectList = (MapList)tableSettingMap.get("objectList");
        LOGGER.info("paramList:{}",paramList);
        try {
            if ("JFRapidOfferEditTable2".equals(strSelectedTable)){
                for (int i = 0; i < objectList.size(); i++) {
                    Map info = (Map) objectList.get(i);
                        String strPrice = (String) info.get("attribute[JF_EvaluatePrice]");
                        res.add(strPrice);
                }
            }else {
                Map<String,BigDecimal>  priceInfo = new HashMap<>();
                //先按层级分组
                Map groupMap = (Map) objectList.stream().collect(Collectors.groupingBy(m -> {
                    Map info = (Map) m;
                    String  strType = (String) info.get(SELECT_TYPE);
                    if (isJsFlush && JF_PLMConstants_mxJPO.type_JFFunctionModule.equals(strType)){
                        String strFuncId = (String)info.get(SELECT_ID);
                        try {
                            DomainObject funBO = DomainObject.newInstance(context,strFuncId);
                            String strNewValue = funBO.getInfo(context,"attribute[JF_EvaluatePrice]");
                            LOGGER.info("strNewValue:{}",strNewValue);
                            info.put("attribute[JF_EvaluatePrice]",strNewValue);
                        } catch (FrameworkException e) {
                           LOGGER.info(e.getMessage());
                        }
                    }
                    return info.get(SELECT_LEVEL);
                }));
                LinkedHashMap sortedMap = sortMapByKeysDesc(groupMap);
//                LOGGER.info("sortedMap:{}",sortedMap);
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
//                    LOGGER.info("sunGroupMap:{}",sunGroupMap);
                    //遍历子级
                    for (Object sunOEntry :sunGroupMap.entrySet()){
                        Map.Entry sunEntry = (Map.Entry)sunOEntry;
                        String strFromId = (String) sunEntry.getKey();
                        List sunMapList = (List) sunEntry.getValue();
                        // 使用Stream API计算总价
                        BigDecimal totalPrice = (BigDecimal) sunMapList.stream()
                                .filter(map -> {
                                    boolean filterRes = false ;
                                    Map info = (Map)map;
                                    String strObjectId = (String) info.get(SELECT_ID);
                                    if (priceInfo.containsKey(strObjectId)){
                                        filterRes = true;
                                    } else if (info.containsKey("attribute[JF_EvaluatePrice]")) {
                                        String strPrice = (String) info.get("attribute[JF_EvaluatePrice]");
                                        if (UIUtil.isNotNullAndNotEmpty(strPrice)){
                                            filterRes = strPrice.matches("^-?\\d+(\\.\\d+)?$");
                                        }
                                    }
                                    return filterRes ;
                                })
                                .map(map -> {
                                    Map info = (Map)map;
                                    String strObjectId = (String) info.get(SELECT_ID);
                                    BigDecimal bigDecimal ;
                                    if (priceInfo.containsKey(strObjectId)){
                                        bigDecimal = priceInfo.get(strObjectId);
                                    }else {
                                        String strPrice = (String) info.get("attribute[JF_EvaluatePrice]");
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
                        priceInfo.put(strFromId,totalPrice);
                    }
                }
                for (int i = 0; i < objectList.size(); i++) {
                    Map info = (Map) objectList.get(i);
                    String strObjectId = (String) info.get(SELECT_ID);
                    if (priceInfo.containsKey(strObjectId)){
                        res.add(priceInfo.get(strObjectId).toString());
                    }else {
                        String strPrice = (String) info.get("attribute[JF_EvaluatePrice]");
                        res.add(strPrice);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error(e.getMessage());
            throw new RuntimeException(e);
        }
        LOGGER.info("--------------------------------  getJFEvaluatePriceValueField end --------------------------------------------------");
        return res;
    }

    /**
    *
    *@description 排序Map
    *@param map
    *@return java.util.LinkedHashMap<java.lang.String,java.lang.String>
    *@throws
    *@author CHENYAN
    *@date 2025/2/14 15:19
    */
    public static LinkedHashMap<String, Object> sortMapByKeysDesc(Map<String, Object> map) {
        List<Map.Entry<String, Object>> list = new ArrayList<>(map.entrySet());

        // 按键的整数值倒序排序
        list.sort((o1, o2) -> {
            return Integer.compare(Integer.parseInt(o2.getKey()), Integer.parseInt(o1.getKey()));
        });

        LinkedHashMap<String, Object> result = new LinkedHashMap<>();
        for (Map.Entry<String, Object> entry : list) {
            result.put(entry.getKey(), entry.getValue());
        }
        return result;
    }

    public  void updateTableAttributeValue(Context context, String[] args) throws Exception {
        try {
            LOGGER.info("updateTableAttributeValue:{}");
            Map paramsMap = (Map) JPO.unpackArgs(args);
            Map columnMap = (Map) paramsMap.get(JF_PLMConstants_mxJPO.STRING_COLUMNMAP);
            String attrName = (String) columnMap.get(DomainConstants.SELECT_NAME);
            LOGGER.info("attrName:{}",attrName);
            HashMap paramMap = (HashMap)paramsMap.get(JF_PLMConstants_mxJPO.STRING_PARAMMAP);
            String objectId = (String)paramMap.get(JF_PLMConstants_mxJPO.STRING_OBJECTID);
            String newValue = (String)paramMap.get(STRING_NEW_VALUE);
            DomainObject domainObject = DomainObject.newInstance(context, objectId);
            domainObject.setAttributeValue(context, attrName, newValue);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
    /* 是否显示快速报价按钮 商务部门或者有JfITAdmin角色 可见
     * @description:
     * @author: caipan
     * @date: 2025/2/20 14:35:21
     * @param: * @param[1] context
     * @param[2] args
     * @return:
     **/
    public boolean isShowCost(Context context,String[] args) throws Exception{
       String depName =  NioJDUtils.getPageStr(context, "Department.Business.Name");
        return JF_Util_mxJPO.getPersonDepartment(context, depName,context.getUser())||isShowFunctionLib(context,args);
    }
    /*
     * @description:功能组件库 ITAdmin 和  admin_platform可见
     * @author: caipan
     * @date: 2025/2/20 14:41:41
     * @param: * @param[1] context
     * @param[2] args
     * @return:
     **/
    public boolean isShowFunctionLib(Context context,String[] args) throws Exception{
        Map requestMap = new HashMap();
        requestMap.put("roleName", "JfCostAdmin");
        requestMap.put("userName", context.getUser());
        boolean  flag = JF_Util_mxJPO.isIncludeRole(context, JPO.packArgs(requestMap))||context.getUser().equals("admin_platform");
        return flag;
    }


    public MapList getCostModules2(Context context,String[] args) throws Exception{
        Map argsMap = JPO.unpackArgs(args);
        String objectId = UIUtil.getValue(argsMap, "objectId");
        LOGGER.info("map:{},objectId：{}",argsMap,objectId);
        DomainObject obj = DomainObject.newInstance(context,objectId);
        StringList selList = JF_Util_mxJPO.basicBolistSel();
        selList.add("attribute[JF_EvaluatePrice]");
        StringList relList = JF_Util_mxJPO.basicRellistSel();
        relList.add(SELECT_FROM_ID);
        String where = null;
        MapList mapList = obj.getRelatedObjects(
                context,
                JF_PLMConstants_mxJPO.rel_JFOffer2JFCostConfig + "," + JF_PLMConstants_mxJPO.rel_JFModule2Module+","+JF_PLMConstants_mxJPO.rel_JFModule2ReferConst+","+JF_PLMConstants_mxJPO.rel_JFCostReferConst2Function,
                JF_PLMConstants_mxJPO.type_JFCostConfig +","+ JF_PLMConstants_mxJPO.type_JFModules + "," + JF_PLMConstants_mxJPO.type_JFCostReferConst + "," +JF_PLMConstants_mxJPO.type_JFFunctionModule ,
                selList,
                relList,
                false,
                true,
                (short) 0,
                "",
                "",
                0);
        Map<String,BigDecimal>  priceInfo = new HashMap<>();
        //先按层级分组
        Map groupMap = (Map) mapList.stream().collect(Collectors.groupingBy(m -> {
            Map info = (Map) m;
            return info.get(SELECT_LEVEL);
        }));
        LinkedHashMap sortedMap = sortMapByKeysDesc(groupMap);
        LOGGER.info("sortedMap:{}",sortedMap);
        for (Object oEntry :sortedMap.entrySet()){
            Map.Entry entry = (Map.Entry)oEntry;
            List parentMapList = (List) entry.getValue();
            //根据from id 分组
            Map sunGroupMap = (Map) parentMapList.stream().collect(Collectors.groupingBy(m -> {
                Map info = (Map) m;
                return info.get(SELECT_FROM_ID);
            }));
            LOGGER.info("sunGroupMap:{}",sunGroupMap);
            //遍历子级
            for (Object sunOEntry :sunGroupMap.entrySet()){
                Map.Entry sunEntry = (Map.Entry)sunOEntry;
                String strFromId = (String) sunEntry.getKey();
                List sunMapList = (List) sunEntry.getValue();
                // 使用Stream API计算总价
                BigDecimal totalPrice = (BigDecimal) sunMapList.stream()
                        .filter(map -> {
                            boolean filterRes = false ;
                            Map info = (Map)map;
                            String strObjectId = (String) info.get(SELECT_ID);
                            if (priceInfo.containsKey(strObjectId)){
                                filterRes = true;
                            } else if (info.containsKey("attribute[JF_EvaluatePrice]")) {
                                String strPrice = (String) info.get("attribute[JF_EvaluatePrice]");
                                if (UIUtil.isNotNullAndNotEmpty(strPrice)){
                                    filterRes = strPrice.matches("^-?\\d+(\\.\\d+)?$");
                                }
                            }
                            return filterRes ;
                        })
                        .map(map -> {
                            Map info = (Map)map;
                            String strObjectId = (String) info.get(SELECT_ID);
                            BigDecimal bigDecimal ;
                            if (priceInfo.containsKey(strObjectId)){
                                bigDecimal = priceInfo.get(strObjectId);
                            }else {
                                String strPrice = (String) info.get("attribute[JF_EvaluatePrice]");
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
                priceInfo.put(strFromId,totalPrice);
            }
        }
        //实际返回配置层
        MapList resMapList = new MapList();
        for (int i = 0; i < mapList.size(); i++) {
            Map info = (Map) mapList.get(i);
            String strType = (String) info.get(SELECT_TYPE);
            if (JF_PLMConstants_mxJPO.type_JFCostConfig.equals(strType)){
                String strObjectId = (String) info.get(SELECT_ID);
                if (priceInfo.containsKey(strObjectId)){
                    info.put("attribute[JF_EvaluatePrice]",priceInfo.get(strObjectId).toString());
                    resMapList.add(info);
                }
            }

        }
        LOGGER.info("resMapList:{}",resMapList);
        return resMapList;
    }



    @com.matrixone.apps.framework.ui.PostProcessCallable
    public Map postUpdateReFlushTable(Context context, String[] args) throws Exception {
        Map returnMap = new HashMap();
        returnMap.put ("Action", "execScript");
        returnMap.put("Message", "{ main:function()  {window.emxEditableTable.refreshStructure()}}");
        return returnMap;
    }

    /**
    *
    *@description 快速报价转移责任人时需要把下面模块的数据全部转移
    *@param context
	*@param args
    *@return java.util.Map
    *@throws
    *@author CHENYAN
    *@date 2025/3/21 11:23
    */
    @com.matrixone.apps.framework.ui.PostProcessCallable
    public Map postUpdateJFRapidOfferModeOwner(Context context, String[] args) throws Exception {
        Map returnMap = new HashMap();
        try {
            ContextUtil.startTransaction(context,true);
            Map parameter = JPO.unpackArgs(args);
            Map requestMap = (Map)parameter.get("requestMap");
            LOGGER.info("requestMap:{}",requestMap);
            String strOid = (String) requestMap.get("objectId");
            String strOwner = (String) requestMap.get("Owner");
            String strManufacturingPerson = (String) requestMap.get("ManufacturingPerson");
            LOGGER.info("strOid:{}",strOid);
            LOGGER.info("strOwner:{}",strOwner);
            LOGGER.info("strManufacturingPerson:{}",strManufacturingPerson);
            DomainObject object = DomainObject.newInstance(context, strOid);
            Map costInfo = object.getInfo(context, StringList.create(SELECT_OWNER, "from[JFRapidOffer2ManufacturingPerson].to.name","from[JFRapidOffer2ManufacturingPerson].id"));
            String strMaterialPerson = (String) costInfo.get(SELECT_OWNER);
            String strOldManufacturingPerson = (String)costInfo.get("from[JFRapidOffer2ManufacturingPerson].to.name");
            String strManufacturingPersonRelId = (String)costInfo.get("from[JFRapidOffer2ManufacturingPerson].id");
            LOGGER.info("strOldManufacturingPerson:{}",strOldManufacturingPerson);
            LOGGER.info("strOld MaterialPerson:{}",strMaterialPerson);
            String organization = JF_Util_mxJPO.getPersonOrganization(context,strOwner);
            transferMaterialPerson(context,object,strOwner,organization);

            if (!strManufacturingPerson.equals(strOldManufacturingPerson)){
                transferManufacturingPerson(context,object,strManufacturingPerson,strManufacturingPersonRelId);
            }
            if (!strOwner.equals(strMaterialPerson)){
                object.setOwner(context,strOwner);
                String project = object.getProjectOwner(context).getName();
                // 设置组织和协作区
                object.setPrimaryOwnership(context, project, organization);
            }
            ContextUtil.commitTransaction(context);
            returnMap.put ("Action", "CONTINUE");
        } catch (Exception e) {
            ContextUtil.abortTransaction(context);
            LOGGER.error(e.getMessage());
            returnMap.put ("Action", "STOP");
            returnMap.put("Message", "update fail ");
        }
        return returnMap;
    }

    public StringList filterBDPersonId(Context context ,String[] args) throws Exception{
        StringList res = new StringList();
        String depName =  NioJDUtils.getPageStr(context, "Department.Business.Name");
        String strWhere = JF_PublicMethodClass_mxJPO.buildStringInStrings("name=='",depName,"'");
        MapList mlResult = DomainObject.findObjects(context, "Department", DomainConstants.QUERY_WILDCARD, strWhere, JF_Util_mxJPO.basicBolistSel());
        if (Objects.nonNull(mlResult) && mlResult.size() > 0){
            Map dMap = (Map) mlResult.get(0);
            String strId = (String)dMap.get(SELECT_ID);
            try {
                //提升权限查询
                ContextUtil.pushContext(context);
                DomainObject department = DomainObject.newInstance(context, strId);
                StringList personIdList  = department.getInfoList(context, "from[Member].to.id");
                res.addAll(personIdList);
            } finally {
                ContextUtil.popContext(context);
            }
        }
        return res;
    }

    /**
    *
    *@description 转移材料费责任人
    *@param context
	*@param object
	*@param strOwner
    *@return void
    *@throws
    *@author CHENYAN
    *@date 2025/3/26 12:00
    */
    public void transferMaterialPerson(Context context ,DomainObject object,String strOwner,String organization) throws Exception{
        StringList selList = JF_Util_mxJPO.basicBolistSel();
        selList.add(SELECT_OWNER);
        StringList relList = JF_Util_mxJPO.basicRellistSel();
        MapList mapList = object.getRelatedObjects(
                context,
                JF_PLMConstants_mxJPO.rel_JFOffer2JFCostConfig + "," + JF_PLMConstants_mxJPO.rel_JFModule2Module+","+JF_PLMConstants_mxJPO.rel_JFModule2ReferConst+","+JF_PLMConstants_mxJPO.rel_JFCostReferConst2Function,
                JF_PLMConstants_mxJPO.type_JFCostConfig +","+ JF_PLMConstants_mxJPO.type_JFModules + "," + JF_PLMConstants_mxJPO.type_JFCostReferConst + "," +JF_PLMConstants_mxJPO.type_JFFunctionModule ,
                selList,
                relList,
                false,
                true,
                (short) 0,
                "",
                "",
                0);
        LOGGER.info("mapList:{}",mapList);
        DomainObject bo = DomainObject.newInstance(context);
        for (int i = 0; i < mapList.size(); i++) {
            Map infoMap = (Map) mapList.get(i);
            String strId = (String) infoMap.get(SELECT_ID);
            bo.setId(strId);
            String strOldOwner = (String) infoMap.get(SELECT_OWNER);
            if (!strOldOwner.equals(strOwner)){
                bo.setOwner(context,strOwner);
                String project = bo.getProjectOwner(context).getName();
                // 设置组织和协作区
                bo.setPrimaryOwnership(context, project, organization);
            }
        }
    }

    /**
    *
    *@description 转移制造费责任人
    *@param context
	*@param object
	*@param
    *@return void
    *@throws
    *@author CHENYAN
    *@date 2025/3/26 11:48
    */
    public void transferManufacturingPerson(Context context ,DomainObject object,String strManufacturingPerson,String strManufacturingPersonRelId) throws Exception{
        StringList selList = JF_Util_mxJPO.basicBolistSel();
        StringList relList = JF_Util_mxJPO.basicRellistSel();
        DomainObject personBO = PersonUtil.getPersonObject(context, strManufacturingPerson);
        //存在关系断开
        if (UIUtil.isNotNullAndNotEmpty(strManufacturingPersonRelId)){
            DomainRelationship.disconnect(context,strManufacturingPersonRelId);
        }
        //连接制造费责任人
        DomainRelationship.connect(context, object, "JFRapidOffer2ManufacturingPerson", personBO);
        MapList mapList = object.getRelatedObjects(
                context,
                JF_PLMConstants_mxJPO.rel_JFRapidOffer2CostBreakDown,
                JF_PLMConstants_mxJPO.type_JFCostBreakDown ,
                selList,
                relList,
                false,
                true,
                (short) 1,
                "",
                "",
                0);
        DomainObject bo = DomainObject.newInstance(context);
        String organization = JF_Util_mxJPO.getPersonOrganization(context,strManufacturingPerson);
        for (int i = 0; i < mapList.size(); i++) {
            Map infoMap = (Map) mapList.get(i);
            String strId = (String) infoMap.get(SELECT_ID);
            bo.setId(strId);
            bo.setOwner(context,strManufacturingPerson);
            String project = bo.getProjectOwner(context).getName();
            // 设置组织和协作区
            bo.setPrimaryOwnership(context, project, organization);
        }
    }

    /**
    *
    *@description 获取系统中得模版整椅
    *@param context
	*@param args
    *@return com.matrixone.apps.domain.util.MapList
    *@throws
    *@author CHENYAN
    *@date 2025/3/26 15:35
    */
    public MapList getCostTemplate(Context context ,String[] args) throws Exception{
        Map argsMap = JPO.unpackArgs(args);
        LOGGER.info("argsMap:{}",argsMap);
        String strSelectId = (String) argsMap.get("selectId");
        LOGGER.info("strObjectId:{}",strSelectId);
        DomainObject BO = DomainObject.newInstance(context, strSelectId);
        StringList infoList = BO.getInfoList(context, "from[JFModule2ReferConst].to.from[JFCostReferConst2Cost].to.id");
        LOGGER.info("infoList:{}",infoList);
        MapList res = DomainObject.findObjects(context, "JFCost", "*", "", JF_Util_mxJPO.basicBolistSel());
        if (infoList.size() > 0){
            for (int i = 0; i < res.size(); i++) {
                Boolean isDisable = Boolean.FALSE;
                Map resInfo = (Map)res.get(i);
                String strId = (String)resInfo.get(SELECT_ID);
                for (int i1 = 0; i1 < infoList.size(); i1++) {
                    String strAlreadyId = (String)infoList.get(i1);
                    if (strAlreadyId.equals(strId)){
                        isDisable = Boolean.TRUE;
                        break;
                    }
                }
                resInfo.put("disableSelection",isDisable.toString());

            }
        }
        return res ;
    }

    public String buildEvaluatePriceHtml(Context context, String[] args) throws Exception {
        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        HashMap requestMap = (HashMap) programMap.get("requestMap");
        HashMap fieldMap = (HashMap) programMap.get("fieldMap");
        String strMode = (String) requestMap.get("mode");
        String strLoginUser = context.getUser();
        String strLang = context.getSession().getLanguage();
        String strCalcBtnNls = ComponentsUtil.i18nStringNow("emxComponents.Common.JFEvaluatePriceCalcBtn", strLang);
        String strChangeSource = "";
        String strIsPlatForm = "";
        String objectId = null;
        if (requestMap == null) {
            objectId = (String) programMap.get("objectId");
        } else {
            objectId = (String) requestMap.get("objectId");
        }
        StringBuffer sbDocDown = new StringBuffer();

        DomainObject domainObject = DomainObject.newInstance(context, objectId);
        String attributeValue = domainObject.getAttributeValue(context, "JF_EvaluatePrice");

//            if ("view".equals(strMode)) {
//                sbDocDown.append(JF_PublicMethodClass_mxJPO.buildStringInStrings("<span>", strQQID, "</span>&nbsp;"));
//            } else if ("edit".equals(strMode)) {
//                sbDocDown.append("<input  id=\"JFECRQQFileId\" name=\"JFECRQQFileId\" value=\"" + docIDList.join(",") + "\" type=\"hidden\">");
//                sbDocDown.append("<input onclick=\"javascript:showModalDialog('../components/JF_ECRQQFilePreCheckin.jsp?objectAction=checkin&msfBypass=true&");
//                sbDocDown.append("objectId=");
//                sbDocDown.append("");
//                sbDocDown.append("','730','450')\"" +
//                        "        value=\"");
//                sbDocDown.append(strFileUploadNls);
//                sbDocDown.append("\" type=\"button\">");
//            }

        sbDocDown.append("<table");
        sbDocDown.append("><tr>");
        sbDocDown.append("<td>");
        sbDocDown.append("<input value=\""+attributeValue+"\" id=\"JF_EvaluatePricefieldValue\" name=\"JF_EvaluatePricefieldValue\" type=\"hidden\"/>");
        sbDocDown.append("<input value=\""+attributeValue+"\" id=\"JF_EvaluatePrice\" name=\"JF_EvaluatePrice\" type=\"text\"  size=\"20\" maxlength=\"\" />");
        sbDocDown.append("</td>");
        sbDocDown.append("<td>");
        sbDocDown.append("<input onclick=\"javascript:calcEvaluatePrice()\"" +
                "  value=\"");
        sbDocDown.append(strCalcBtnNls);
        sbDocDown.append("\" type=\"button\"/>");
        sbDocDown.append("</td>");
        sbDocDown.append("<td id=\"JFReplace\">");
        sbDocDown.append("</td>");
        sbDocDown.append("</tr></table>");
        return sbDocDown.toString();
    }

    public void updateEvaluatePrice(Context context, String[] args) throws Exception {
        try {
            Map requestMap = JPO.unpackArgs(args);
            LOGGER.info("------------------------updateEvaluatePrice ---------------------------requestMap:"+requestMap);
            Map paramMap = (Map) requestMap.get("paramMap");
            String newValue = (String)paramMap.get("New Value");
            String newOID = (String)paramMap.get("New OID");
            LOGGER.info("------------------------updateEvaluatePrice ---------------------------newOID:"+newOID);
            LOGGER.info("------------------------updateEvaluatePrice ---------------------------newValue:"+newValue);
            String taskId = (String) paramMap.get("objectId");
            DomainObject taskDomain = DomainObject.newInstance(context, taskId);
            taskDomain.setAttributeValue(context,"JF_EvaluatePrice",newValue);
        }catch (Exception e){
            LOGGER.info("updateEvaluatePrice------error",e);
        }

    }

}
