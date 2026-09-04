import com.dassault_systemes.delmia.ppr.nav.crs_utils.CommonCRUDProvider;
import com.dassault_systemes.delmia.ppr.nav.utils.NavBusUtil;
import com.dassault_systemes.enovia.common.json.ENOXJsonObjectBuilder;
import com.dassault_systemes.enovia.xapps.dsmfg.V1.implementations.dsmfgConstants;
import com.dassault_systemes.enovia.xapps.dsmfg.V1.utils.dsmfgPayloadUtil;
import com.dassault_systemes.enovia.xapps.dsmfg.implementations.dsmfgResourceUtil;
import com.dassault_systemes.enovia.xapps.exception.ENOXException;
import com.dassault_systemes.enovia.xapps.services.ER.implementations.ENOXERContext;
import com.dassault_systemes.enovia.xapps.services.context.ENOXContextManager;
import com.dassault_systemes.enovia.xapps.xtools.ENOXMiscTools;
import com.dassault_systemes.platform.model.BaseKind;
import com.dassault_systemes.platform.model.CommonWebException;
import com.dassault_systemes.platform.model.Oxid;
import com.dassault_systemes.platform.model.itf.IOxidService;
import com.dassault_systemes.platform.model.itf.nav.INavBus;
import com.dassault_systemes.platform.model.itf.nav.INavBusConnection;
import com.dassault_systemes.platform.model.itf.nav.INavBusObject;
import com.dassault_systemes.platform.model.itf.nav.INavBusProvider;
import com.dassault_systemes.platform.model.mbom.implementation.MBOMNavigation;
import com.dassault_systemes.platform.model.mbom.itf.IMBOMAuthoringServices;
import com.dassault_systemes.platform.model.mbom.itf.IMBOMNavigationServices;
import com.dassault_systemes.platform.model.mbom.services.MBOMServicesProvider;
import com.dassault_systemes.platform.model.services.IdentificationServicesProvider;
import com.dassault_systemes.platform.model.services.NavigationServicesProvider;
import com.dassault_systemes.platform.model.services.links.LinkOutputAndErrorInfo;
import com.dassault_systemes.platform.model.services.links.LinkRelationInformation;
import com.dassault_systemes.rest.service.lifecycle.ReviseResource;
import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.util.ContextUtil;
import com.matrixone.apps.domain.util.EnoviaResourceBundle;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.framework.ui.UIUtil;
import matrix.db.Context;
import matrix.util.MatrixException;
import matrix.util.StringList;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
/*
 * @description: MBOM工具类
 * @author: caipan
 * @date: 2025/3/25 15:50:30
 * @param: * @param[1] null
 * @return:
 **/
public class JF_MBOMUtil_mxJPO {
    private static final Logger _logger = LoggerFactory.getLogger(JF_MBOMUtil_mxJPO.class);

    /*
     * @description:创建MBOM的零件对象
     * @author: caipan
     * @date: 2025/3/25 15:55:17
     * @param: * @param[1] context
     * @param[2] args args[0] CreateAssembly  总成 Provide  零件
     * @return:创建M零件的ID
     **/
    public String createMfgItems(Context context,String[] args) throws Exception{
        String partType = args[0];
        String phyId = createMfgItems(context,partType);//创建M零件对象 ，返回物理ID
        return phyId;
    }
    /*
     * @description:创建 E零件 和M零件的scope关系
     * @author: caipan
     * @date: 2025/3/25 16:04:35
     * @param: * @param[1] context
     * @param[2] args
     * @return:
     **/
    public void createScopeLinkForMBOM(Context context,String[] args) throws  Exception{
        String ephyId =args[0];
        String MphyId =args[1];
        createScopeLinkForPBOM(context,ephyId,MphyId);
    }
    /*
     * @description:通过EPart查询到MPart
     * @author: caipan
     * @date: 2025/3/26 09:51:30
     * @param: * @param[1] context
     * @param[2] args args[0]传物理ID
     * @return:M零件列表
     **/
    public StringList queryE2M(Context context,String[] args) throws Exception{
        String ePartId = args[0];
        StringList list = getManufacturingItemFromEngineeringItem(context, ePartId);
        _logger.info("mPart List:{}",list);
        return list;
    }
    /*
     * @description:通过MPart查询到EPart
     * @author: caipan
     * @date: 2025/3/26 09:51:30
     * @param: * @param[1] context
     * @param[2] args args[0]传物理ID
     * @return:E零件列表
     **/
    public String queryM2E(Context context,String[] args) throws Exception{
        String mPartId = args[0];
        String ePartId = getEngineeringItemFromManufacturingItem(context, mPartId);
        _logger.info("ePartId:{}",ePartId);
        return ePartId;
    }
    /*
     * @description:M零件和M零件创建MBOM关系
     * @author: caipan
     * @date: 2025/3/26 13:40:35
     * @param: * @param[1] context
     * @param[2] args args[0] fomd端物理ID args[1] to断物理ID
     * @return: 关系ID
     **/
    public String  createMfgInstances(Context context,String[] args) throws Exception{
        String fromId = args[0];
        String toId = args[1];
       String relId = createMfgInstances(context, fromId, toId);
       return relId;
    }

    /**
     * @param context 上下文
     * @param type    数据类型 CreateAssembly Provide
     * @return List<String>
     * @Author HLY
     * @CreateTime 2024/9/4
     * @Description: 创建一个MA 对象
     */
    public static String createMfgItems(Context context, String type) throws Exception {
        String resultPhyId = DomainConstants.EMPTY_STRING;
        try {
            ContextUtil.startTransaction(context, true);
            //添加后台 ENOXERContext
            ENOXERContext enoxerContext = new ENOXERContext(context, DomainConstants.EMPTY_STRING, 1);
            ENOXContextManager.getContextManager().addContext(context, enoxerContext);

            ArrayList resultList = new ArrayList();
            CommonCRUDProvider commonCRUDProvider = new CommonCRUDProvider(CommonCRUDProvider.DB_3DSpace, context);
            ArrayList providerList = new ArrayList();
            List<String> AddNewMfgItems = AddNewMfgItems(context, commonCRUDProvider, providerList, type);
            if (AddNewMfgItems == null || AddNewMfgItems.isEmpty()) {
                return resultPhyId;
            }
            commonCRUDProvider.invokeAdd(providerList);
            if (commonCRUDProvider.getStatusCode() != CommonCRUDProvider.CommonCRUDStatusCode.CRUD_SUCCESS) {
                throw new ENOXException(400, commonCRUDProvider.getStatusMessage());
            }
            commonCRUDProvider.invokeSave();
            if (commonCRUDProvider.getStatusCode() != CommonCRUDProvider.CommonCRUDStatusCode.CRUD_SUCCESS) {
                throw new ENOXException(400, commonCRUDProvider.getStatusMessage());
            }
            Iterator<String> it = AddNewMfgItems.iterator();
            while (it.hasNext()) {
                resultList.add(commonCRUDProvider.getIdFromTempId(it.next()));
            }
            resultPhyId = (String) resultList.get(0);
            ContextUtil.commitTransaction(context);
        } catch (Exception e) {
            e.printStackTrace();
            ContextUtil.abortTransaction(context);
            throw e;
        }
        return resultPhyId;
    }


    /**
     * @param context            安全上下文
     * @param commonCRUDProvider
     * @param providerList
     * @param type               数据类型
     * @return List<String>
     * @Author HLY
     * @CreateTime 2024/9/4
     * @Description: OOTB API 创建MA
     */
    private static List<String> AddNewMfgItems(Context context, CommonCRUDProvider commonCRUDProvider, ArrayList providerList, String type) throws Exception {
        ArrayList arrayList = new ArrayList();

        String newId = commonCRUDProvider.getNewId();
        arrayList.add(newId);
        HashMap hashMap = new HashMap();
        HashMap convertJsonObjectToMap = new HashMap();

        dsmfgPayloadUtil.addEnterpriseAttributesToCRUDMapFromAttributesMap(context, convertJsonObjectToMap, hashMap);
        dsmfgPayloadUtil.addCustomerAttributesToCRUDMapFromAttributesMap(context, convertJsonObjectToMap, hashMap, type);
        commonCRUDProvider.addEntryToCRUDMap(hashMap, CommonCRUDProvider.Action, CommonCRUDProvider.Action_Create);
        commonCRUDProvider.addEntryToCRUDMap(hashMap, CommonCRUDProvider.Type, type);
        commonCRUDProvider.addEntryToCRUDMap(hashMap, CommonCRUDProvider.Id, newId);
        providerList.add(hashMap);

        return arrayList;
    }


    /**
     * @param context  安全上下文
     * @param mfgPhyId M物料的 physicalid
     * @return ENOXJsonObjectBuilder
     * @Author HLY
     * @CreateTime 2024/9/4
     * @Description: 删除一个MA
     */
    public static String deleteMfgItems(Context context, String mfgPhyId) throws Exception {
        org.json.JSONObject deleteResult = new org.json.JSONObject();
        deleteResult.put("status", 100);
        deleteResult.put("message", "del fail");
        try {
            //开启事务
            ContextUtil.startTransaction(context, true);
            DomainObject mfgObj = DomainObject.newInstance(context, mfgPhyId);
            String type = mfgObj.getInfo(context, DomainConstants.SELECT_TYPE);

            //添加后台 ENOXERContext
            ENOXERContext enoxerContext = new ENOXERContext(context, DomainConstants.EMPTY_STRING, 1);
            ENOXContextManager.getContextManager().addContext(context, enoxerContext);

            CommonCRUDProvider commonCRUDProvider = new CommonCRUDProvider(CommonCRUDProvider.DB_3DSpace, context);
            ArrayList dataList = new ArrayList();
            HashMap map = new HashMap();
            //String var6 = CommonMiscUtil.getTypeFromID(context, mfgPhyId);
            commonCRUDProvider.addEntryToCRUDMap(map, CommonCRUDProvider.Action, CommonCRUDProvider.Action_Delete);
            commonCRUDProvider.addEntryToCRUDMap(map, CommonCRUDProvider.Type, type);
            commonCRUDProvider.addEntryToCRUDMap(map, CommonCRUDProvider.Id, mfgPhyId);
            dataList.add(map);
            commonCRUDProvider.invokeAdd(dataList);
            if (commonCRUDProvider.getStatusCode() != CommonCRUDProvider.CommonCRUDStatusCode.CRUD_SUCCESS) {
                throw new ENOXException(400, commonCRUDProvider.getStatusMessage());
            } else {
                commonCRUDProvider.invokeSave();
                if (commonCRUDProvider.getStatusCode() != CommonCRUDProvider.CommonCRUDStatusCode.CRUD_SUCCESS) {
                    throw new ENOXException(400, commonCRUDProvider.getStatusMessage());
                } else {
                    ENOXJsonObjectBuilder enoJsonBuilder = ENOXMiscTools.getStandardResponseSuccess(dsmfgResourceUtil.getTranslation(context, dsmfgResourceUtil.DSMFG_NLS_KEY.DELETE_SUCCESS.toString(), new String[0]));
                    String delResultStr = enoJsonBuilder.getJSONObjectString();
                    ContextUtil.commitTransaction(context);
                    return delResultStr;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            deleteResult.put("message", e.getMessage());
            //throw e;
            ContextUtil.abortTransaction(context);
        }
        return deleteResult.toString();
    }


    /**
     * @param context
     * @param fromPhyId
     * @param toPhyId
     * @return String
     * @Author HLY
     * @CreateTime 2024/9/4
     * @Description: 创建MA实例 MBOM关系
     */
    public static String createMfgInstances(Context context, String fromPhyId, String toPhyId) throws Exception {

        String relId = DomainConstants.EMPTY_STRING;
        try {
            ArrayList resultList = new ArrayList();

            ContextUtil.startTransaction(context, true);
            ArrayList invokeAddList = new ArrayList();
            ArrayList newIdList = new ArrayList();
            CommonCRUDProvider commonCRUDProvider = new CommonCRUDProvider(CommonCRUDProvider.DB_3DSpace, context);

            String newId = commonCRUDProvider.getNewId();
            newIdList.add(newId);
            invokeAddList.add(getCrudInputMapForInstanceCreate(commonCRUDProvider, fromPhyId, toPhyId, newId));

            commonCRUDProvider.invokeAdd(invokeAddList);
            if (commonCRUDProvider.getStatusCode() != CommonCRUDProvider.CommonCRUDStatusCode.CRUD_SUCCESS) {
                throw new ENOXException(400, dsmfgResourceUtil.getTranslationForErrorCode(context, commonCRUDProvider.getStatusMessage()));
            } else {
                commonCRUDProvider.invokeSave();
                if (commonCRUDProvider.getStatusCode() != CommonCRUDProvider.CommonCRUDStatusCode.CRUD_SUCCESS) {
                    throw new ENOXException(400, dsmfgResourceUtil.getTranslationForErrorCode(context, commonCRUDProvider.getStatusMessage()));
                } else {
                    Iterator var17 = newIdList.iterator();
                    while (var17.hasNext()) {
                        String var18 = (String) var17.next();
                        resultList.add(commonCRUDProvider.getIdFromTempId(var18));
                    }
                    ContextUtil.commitTransaction(context);
                    //System.out.println("resultList : " + resultList);
                    relId = (String) resultList.get(0);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            ContextUtil.abortTransaction(context);
            throw e;
        }
        return relId;
    }


    /**
     * @param paramCommonCRUDProvider
     * @param fromPhyId
     * @param toPhyId
     * @param newId
     * @return Map<String, List < Object>>
     * @Author HLY
     * @CreateTime 2024/9/4
     * @Description:
     */
    private static Map<String, List<Object>> getCrudInputMapForInstanceCreate(CommonCRUDProvider paramCommonCRUDProvider, String fromPhyId, String toPhyId, String newId) throws Exception {
        HashMap<String, List<Object>> hashMap = new HashMap<String, List<Object>>();
        String instTypeName = "DELFmiFunctionIdentifiedInstance";
        paramCommonCRUDProvider.addEntryToCRUDMap(hashMap, CommonCRUDProvider.Action, CommonCRUDProvider.Action_Create);
        paramCommonCRUDProvider.addEntryToCRUDMap(hashMap, CommonCRUDProvider.Type, instTypeName);
        paramCommonCRUDProvider.addEntryToCRUDMap(hashMap, CommonCRUDProvider.Id, newId);
        paramCommonCRUDProvider.addEntryToCRUDMap(hashMap, CommonCRUDProvider.From_IDs, fromPhyId);
        paramCommonCRUDProvider.addEntryToCRUDMap(hashMap, CommonCRUDProvider.To_IDs, toPhyId);
        return (Map) hashMap;
    }


    /** 断开MBOM关系
     * @param context
     * @param instArray
     * @return ENOXJsonObjectBuilder
     * @Author HLY
     * @CreateTime 2024/9/5
     * @Description:
     */
    public static String deleteMfgInstances(Context context, JSONArray instArray) throws Exception {
        org.json.JSONObject resultJson = new JSONObject();
        resultJson.put("status", 100);
        resultJson.put("message", "Detach of mfg Instances fail");
        try {
            ContextUtil.startTransaction(context, true);
            ArrayList arrayList = new ArrayList();
            CommonCRUDProvider commonCRUDProvider = new CommonCRUDProvider(CommonCRUDProvider.DB_3DSpace, context);
            if (instArray == null || instArray.length() <= 0 || instArray.length() > 10) {
                throw new ENOXException(400, dsmfgResourceUtil.getTranslation(context, "dsmfg.getMfgItemsFromEngItem.sizeOutOfRange", new String[0]));
            }
            for (int i = 0; i < instArray.length(); i++) {
                String relId_i = instArray.getString(i);
                HashMap hashMap = new HashMap();
                commonCRUDProvider.addEntryToCRUDMap(hashMap, CommonCRUDProvider.Action, CommonCRUDProvider.Action_Delete);
                commonCRUDProvider.addEntryToCRUDMap(hashMap, CommonCRUDProvider.Type, dsmfgConstants.TYPE_MFG_ITEM_INSTANCE);
                commonCRUDProvider.addEntryToCRUDMap(hashMap, CommonCRUDProvider.Id, relId_i);
                arrayList.add(hashMap);
            }
            commonCRUDProvider.invokeAdd(arrayList);
            if (commonCRUDProvider.getStatusCode() != CommonCRUDProvider.CommonCRUDStatusCode.CRUD_SUCCESS) {
                throw new ENOXException(400, dsmfgResourceUtil.getTranslationForErrorCode(context, commonCRUDProvider.getStatusMessage()));
            }
            commonCRUDProvider.invokeSave();
            if (commonCRUDProvider.getStatusCode() != CommonCRUDProvider.CommonCRUDStatusCode.CRUD_SUCCESS) {
                throw new ENOXException(400, dsmfgResourceUtil.getTranslationForErrorCode(context, commonCRUDProvider.getStatusMessage()));
            }
            ContextUtil.commitTransaction(context);
            return ENOXMiscTools.getStandardResponseSuccess("Detach of mfg Instances successful").getJSONObjectString();
        } catch (Exception e) {
            e.printStackTrace();
            ContextUtil.abortTransaction(context);
        }
        return resultJson.toString();
    }


    /**
     * @param context
     * @param mfgInstPhyId
     * @return void
     * @Author HLY
     * @CreateTime 2024/10/14
     * @Description: 通过MBOM的实例 获取 实施链接关联的EBOM
     */
    public static List<List<String>> getEngInstByMfgInst(Context context, String mfgInstPhyId) throws Exception {
        List<List<String>> resultList = new ArrayList<List<String>>();
        StringList mInstList = new StringList();//MBOM关系id
        mInstList.add(mfgInstPhyId);
        List<List<List<String>>> engInstList = getEngInstByMfgInst(context, mInstList);
        if (engInstList != null && !engInstList.isEmpty()) {
            resultList = engInstList.get(0);
        }
        return resultList;
    }


    /**
     * @param context
     * @param mInstList MBOM实例集合
     * @return void
     * @Author HLY
     * @CreateTime 2024/10/14
     * @Description: 通过MBOM的实例 获取 实施链接关联的EBOM
     */
    public static List<List<List<String>>> getEngInstByMfgInst(Context context, StringList mInstList) throws Exception {
        //_logger.info(" -------------------  getEngInstByMfgInst-------------------  ");
        MBOMNavigation mbomNavigation = new MBOMNavigation();
        //_logger.info("mbomNavigation : " + mbomNavigation);
        List<INavBusConnection> m2EInstQueryList = getM2EInstQueryList(context, mInstList);
        List<LinkRelationInformation> implementedInstancesPathsFromList = mbomNavigation.getImplementedInstancesPathsFromList(context, m2EInstQueryList, new boolean[]{true, true, true, true});
        //_logger.info("implementedInstancesPathsFromList : " + implementedInstancesPathsFromList);
        List<List<List<String>>> resultList = new ArrayList<List<List<String>>>();
        if(implementedInstancesPathsFromList!=null && !implementedInstancesPathsFromList.isEmpty()){
            for (int i = 0; i < implementedInstancesPathsFromList.size(); i++) {
                LinkRelationInformation linkRelationInformation = implementedInstancesPathsFromList.get(i);
                if(linkRelationInformation==null){
                    continue;
                }
                List<List<INavBus>> pointedObjects = linkRelationInformation.getPointedObjects();
                if(pointedObjects==null || pointedObjects.isEmpty()){
                    continue;
                }
                //System.out.println("pointedObjects : " + pointedObjects);
                List<List<String>> engConnList = new ArrayList<List<String>>();
                for (int j = 0; j < pointedObjects.size(); j++) {
                    List<INavBus> iNavBuses = pointedObjects.get(j);
                    List<String> pathEngConnList = new ArrayList<String>();
                    for (int k = 0; k < iNavBuses.size(); k++) {
                        INavBus iNavBus = iNavBuses.get(k);
                        if(iNavBus==null){
                            pathEngConnList.add(DomainConstants.EMPTY_STRING);
                        }else {
                            String connStr = iNavBus.toString();
                            //System.out.println("connStr : " + connStr);
                            JSONArray connArray = new JSONArray(connStr);
                            String relPhyid_i = connArray.getString(0);
                            //System.out.println("relPhyid_i : " + relPhyid_i);
                            pathEngConnList.add(relPhyid_i);
                        }
                    }
                    engConnList.add(pathEngConnList);
                }
                resultList.add(engConnList);
            }
        }
        //_logger.info("resultList : " + resultList);
        return resultList;
    }


    /**
     * @param context
     * @param mInstList
     * @return List<List < INavBusConnection>>
     * @Author HLY
     * @CreateTime 2024/10/11
     * @Description: 获取MBOM实例查询EBOM实例路径的参数集合
     */
    private static List<INavBusConnection> getM2EInstQueryList(Context context, StringList mInstList) throws CommonWebException, MatrixException {
        IOxidService oxidService = IdentificationServicesProvider.getOxidService();
        List idList = Collections.nCopies(mInstList.size(), BaseKind.connection);
        List oxidList = oxidService.getOxidsFromPidsAndKinds(context, mInstList, idList);
        INavBusProvider provider = NavigationServicesProvider.getNavBusProvider();
        ArrayList arrayList = new ArrayList();
        Map oxidMap = provider.createNavBusConnections(context, oxidList);
        Iterator oxidIt = oxidList.iterator();
        while (oxidIt.hasNext()) {
            Oxid oxid = (Oxid) oxidIt.next();
            arrayList.add((INavBusConnection) oxidMap.get(oxid));
        }
        return arrayList;
    }

    /**
     * @param context   上下文
     * @param mInstList MBOM实例集合
     * @param eInstList EBOM实例集合
     * @return void
     * @Author HLY
     * @CreateTime 2024/8/30
     * @Description: 创建实施链接关系
     */
    public static void createM2EInstLink(Context context, StringList mInstList, StringList eInstList) throws Exception {
        //JSONArray array = new JSONArray();
        _logger.info(" ------------------ createM2EInstLink ------------------ ");
        _logger.info("mInstList : " + mInstList);
        _logger.info("eInstList : " + eInstList);

        List<List<INavBusConnection>> mConnList = getCreateLinkInstList(context, mInstList);
        //_logger.info("mConnList : " + mConnList);
        List<List<INavBusConnection>> eConnList = getCreateLinkInstList(context, eInstList);
        //_logger.info("eConnList : " + eConnList);
        //创建实施链接关系
        IMBOMAuthoringServices services = MBOMServicesProvider.getAuthoringServices();
        //_logger.info("services : " + services);

        List<LinkOutputAndErrorInfo> mbomImplementLinks = services.createMBOMImplementLinks(context, mConnList, eConnList);
        _logger.info("mbomImplementLinks : " + mbomImplementLinks);

//        for (int i = 0; i < mbomImplementLinks.size(); i++) {
//            LinkOutputAndErrorInfo linkOutputAndErrorInfo = mbomImplementLinks.get(i);
//            _logger.info("linkOutputAndErrorInfo : " + linkOutputAndErrorInfo);
//
//            linkOutputAndErrorInfo.get
//
//            LinkOutputAndErrorInfo.status statusValue = linkOutputAndErrorInfo.getStatusValue();
//            _logger.info("statusValue : " + statusValue);
//
//            String messageFromBL = linkOutputAndErrorInfo.getMessageFromBL();
//            _logger.info("messageFromBL : " + messageFromBL);
//
//            String createResult = linkOutputAndErrorInfo.toString();
//            _logger.info("createResult : " + createResult);
//            array.put(createResult);
//        }
//        _logger.info(" ------------------ createM2EInstLink ------------------ ");
//        return array;
    }


    /**
     * @param context   上下文
     * @param mInst     MBOM实例
     * @param eInstList EBOM实例集合
     * @return void
     * @Author HLY
     * @CreateTime 2024/8/30
     * @Description: 创建实施链接关系
     */
    public static void createM2EInstLink(Context context, String mInst, StringList eInstList) throws Exception {
        StringList mInstList = new StringList();
        mInstList.add(mInst);
        createM2EInstLink(context, mInstList, eInstList);
    }

    /**
     * @param context 上下文
     * @param mInst   MBOM实例
     * @param eInst   EBOM实例
     * @return void
     * @Author HLY
     * @CreateTime 2024/8/30
     * @Description: 创建实施链接关系
     */
    public static void createM2EInstLink(Context context, String mInst, String eInst) throws Exception {
        StringList mInstList = new StringList();
        mInstList.add(mInst);
        StringList eInstList = new StringList();
        eInstList.add(eInst);
        createM2EInstLink(context, mInstList, eInstList);
    }

    /**
     * @param context    上下文
     * @param instIdList 实例id集合
     * @return List<List < INavBusConnection>>
     * @Author HLY
     * @CreateTime 2024/10/14
     * @Description: 获取创建EBOM实例和MBOM实例链接的connection集合
     */
    private static List<List<INavBusConnection>> getCreateLinkInstList(Context context, StringList instIdList) throws CommonWebException, MatrixException {
        //_logger.info(" ------------------ getCreateLinkInstList ------------------ ");
        ArrayList bomList = new ArrayList();
        //StringList ids = new StringList();
        IOxidService oxidService = IdentificationServicesProvider.getOxidService();
        //_logger.info("oxidService : " + oxidService);
        List idList = Collections.nCopies(instIdList.size(), BaseKind.connection);
        //_logger.info("idList : " + idList);
        List oxidList = oxidService.getOxidsFromPidsAndKinds(context, instIdList, idList);
        //_logger.info("oxidList : " + oxidList);
        INavBusProvider provider = NavigationServicesProvider.getNavBusProvider();
        //_logger.info("provider : " + provider);
        ArrayList arrayList = new ArrayList();
        //_logger.info("arrayList : " + arrayList);
        Map oxidMap = provider.createNavBusConnections(context, oxidList);
        //_logger.info("oxidMap : " + oxidMap);
        Iterator oxidIt = oxidList.iterator();
        //_logger.info("oxidIt : " + oxidIt);
        while (oxidIt.hasNext()) {
            Oxid oxid = (Oxid) oxidIt.next();
            //_logger.info("oxid : " + oxid);
            arrayList.add((INavBusConnection) oxidMap.get(oxid));
        }
        bomList.add(arrayList);
        //_logger.info("bomList : " + bomList);
        //_logger.info(" ------------------ getCreateLinkInstList ------------------ ");
        return bomList;
    }


    /**
     * @param context
     * @param args
     * @return void
     * @Author HLY
     * @CreateTime 2024/9/20
     * @Description: 更新MBOM链接
     */

    /**
     * @param context
     * @param engId
     * @param mfgId
     * @param sourceEngId
     * @param sourceMfgId
     * @return void
     * @Author HLY
     * @CreateTime 2024/10/18
     * @Description: 更新一层 MBOM实施链接 这个EBOM和MBOM实例是一对一的 不能存在一个MBOM实例对应多个EBOM实例
     */
    public static void updateMbomImplementLinks(Context context, String engId, String mfgId, String sourceEngId, String sourceMfgId) throws Exception {
      /*  _logger.info(" ------------------ updateMbomImplementLinks ------------------ ");
        StringList busList = new StringList();
        busList.add(DomainConstants.SELECT_ID);
        busList.add(DomainConstants.SELECT_TYPE);
        busList.add(DomainConstants.SELECT_NAME);
        busList.add(DomainConstants.SELECT_REVISION);
        busList.add(DomainConstants.SELECT_OWNER);

        StringList relList = new StringList();
        relList.add(DomainConstants.SELECT_RELATIONSHIP_ID);
        relList.add(DomainConstants.SELECT_RELATIONSHIP_TYPE);
        relList.add(DomainConstants.SELECT_LEVEL);

        DomainObject engObj = DomainObject.newInstance(context, engId);
        _logger.info("engObj : " + engObj);

        DomainObject mfgObj = DomainObject.newInstance(context, mfgId);
        _logger.info("mfgObj : " + mfgObj);

        DomainObject sourceEngObj = DomainObject.newInstance(context, sourceEngId);
        _logger.info("sourceEngObj : " + sourceEngObj);

        DomainObject sourceMfgObj = DomainObject.newInstance(context, sourceMfgId);
        _logger.info("sourceMfgObj : " + sourceMfgObj);

        //一层E物料的BOM结构
        MapList eBomList = engObj.getRelatedObjects(context, REL_VPMINSTANCE, Vpmreference, busList, relList, false, true, (short) 1, null, null, 0);
        _logger.info("eBomList : " + eBomList);

        //一层M物料的BOM结构
        MapList mBomList = mfgObj.getRelatedObjects(context, Lead_Constants_mxJPO.REL_DELFMIFUNCTIONIDENTIFIEDINSTANCE, Lead_Constants_mxJPO.TYPE_DELFMIFUNCTIONREFERENCE, busList, relList, false, true, (short) 1, null, null, 0);
        _logger.info("mBomList : " + mBomList);

        //一层源E物料的BOM结构
        MapList sourceEBomList = sourceEngObj.getRelatedObjects(context, Lead_Constants_mxJPO.REL_VPMINSTANCE, Lead_Constants_mxJPO.TYPE_VPMREFERENCE, busList, relList, false, true, (short) 1, null, null, 0);
        _logger.info("sourceEBomList : " + sourceEBomList);

        //一层源M物料的BOM结构
        MapList sourceMBomList = sourceMfgObj.getRelatedObjects(context, Lead_Constants_mxJPO.REL_DELFMIFUNCTIONIDENTIFIEDINSTANCE, Lead_Constants_mxJPO.TYPE_DELFMIFUNCTIONREFERENCE, busList, relList, false, true, (short) 1, null, null, 0);
        _logger.info("sourceMBomList : " + sourceMBomList);

        //获取源EBOM实例与MBOM实例的映射关系
        Map<String, String> sourceM2EInstMapping = new HashMap<String, String>();

        for (int i = 0; i < sourceMBomList.size(); i++) {
            Map map_i = (Map) sourceMBomList.get(i);
            //_logger.info("sourceMBomList map_i : "+map_i);
            String mInstPhyId_i = (String) map_i.get(Lead_Constants_mxJPO.RELPHYIDKEY);
            //_logger.info("mInstPhyId_i : "+mInstPhyId_i);
            List<List<String>> engInstByMfgInst_i = getEngInstByMfgInst(context, mInstPhyId_i);
            //_logger.info("engInstByMfgInst_i : "+engInstByMfgInst_i);
            if (engInstByMfgInst_i!=null && !engInstByMfgInst_i.isEmpty()) {
                List<String> eInstList0 = engInstByMfgInst_i.get(0);
                if(eInstList0!=null && !eInstList0.isEmpty()){
                    String eInstPhyId_i = eInstList0.get(0);
                    sourceM2EInstMapping.put(mInstPhyId_i, eInstPhyId_i);
                }
            }
        }
        _logger.info("sourceM2EInstMapping : " + sourceM2EInstMapping);

        //源EBOM 实例physicalid 与 EBOM 实例 physicalid 映射关系
        Map<String, String> sEInstPhyMapping = getEInstPhyIdMapping(eBomList, sourceEBomList);
        _logger.info("sEInstPhyMapping : " + sEInstPhyMapping);

        //获取源EBOM MBOM 实例 logicalid 和 physicalid的映射关系
        Map sourceLPMapping = getSourceInstMapping(sourceEBomList, sourceMBomList);
        _logger.info("sourceLPMapping : " + sourceLPMapping);

        for (int i = 0; i < mBomList.size(); i++) {
            Map map_i = (Map) mBomList.get(i);
            _logger.info("map_i : " + map_i);
            String mInstLogId_i = (String) map_i.get(Lead_Constants_mxJPO.RELLOGIDKEY);
            _logger.info("mInstLogId_i : " + mInstLogId_i);
            String mInstPhyId_i = (String) map_i.get(Lead_Constants_mxJPO.RELPHYIDKEY);
            _logger.info("mInstPhyId_i : " + mInstPhyId_i);
            String eInstPhyId_i = getEbomPhyId(sourceM2EInstMapping, sourceLPMapping, sEInstPhyMapping, mInstLogId_i);
            _logger.info("eInstPhyId_i : " + eInstPhyId_i);

            if(UIUtil.isNotNullAndNotEmpty(eInstPhyId_i)){
                createM2EInstLink(context,mInstPhyId_i,eInstPhyId_i);
            }
        }*/
        _logger.info(" ------------------ updateMbomImplementLinks ------------------ ");
    }

    /**
     *
     * @param eBomList
     * @param sourceEBomList
     * @return Map<String,String>
     * @Author HLY
     * @CreateTime 2024/10/18
     * @Description: 获取 源EBOM实例 与 EBOM实例的映射关系
     */
    private static Map<String, String> getEInstPhyIdMapping(MapList eBomList, MapList sourceEBomList) {
        //EBOM logicalid 或 physicalid的映射关系
        Map<String, String> eInstLPMapping = new HashMap<String, String>();
        for (int i = 0; i < eBomList.size(); i++) {
            Map map_i = (Map) eBomList.get(i);
//            String eInstLogId_i = (String) map_i.get(Lead_Constants_mxJPO.RELLOGIDKEY);
//            String eInstPhyId_i = (String) map_i.get(Lead_Constants_mxJPO.RELPHYIDKEY);
//            eInstLPMapping.put(eInstLogId_i, eInstPhyId_i);
        }

        Map<String, String> sourceEInstLPMapping = new HashMap<String, String>();
        for (int i = 0; i < sourceEBomList.size(); i++) {
            Map map_i = (Map) sourceEBomList.get(i);
//            String eInstLogId_i = (String) map_i.get(Lead_Constants_mxJPO.RELLOGIDKEY);
//            String eInstPhyId_i = (String) map_i.get(Lead_Constants_mxJPO.RELPHYIDKEY);
//            sourceEInstLPMapping.put(eInstLogId_i, eInstPhyId_i);
        }

        Map sEInstPhyMapping = new HashMap();
        Iterator<String> it = eInstLPMapping.keySet().iterator();
        while (it.hasNext()) {
            String logicalid = it.next();
            if (sourceEInstLPMapping.containsKey(logicalid)) {
                String sourceEInstPhyId = sourceEInstLPMapping.get(logicalid);
                String eInstPhyId = eInstLPMapping.get(logicalid);
                sEInstPhyMapping.put(sourceEInstPhyId, eInstPhyId);
            }
        }
        return sEInstPhyMapping;
    }


    /**
     *
     * @param sourceM2EInstMapping
     * @param sourceLPMapping
     * @param sEInstPhyMapping
     * @param mInstLogId
     * @return String
     * @Author HLY
     * @CreateTime 2024/10/18
     * @Description: 通过MBOM实例 logicalid 找到源 MBOM实例  通过实施链接 找到源EBOM实例 找到新的EBOM实例
     */
    private static String getEbomPhyId(Map sourceM2EInstMapping, Map sourceLPMapping, Map<String, String> sEInstPhyMapping, String mInstLogId) {
        String eInstPhyId = DomainConstants.EMPTY_STRING;
        String sourceMInstPhyId_i = (String) sourceLPMapping.get(mInstLogId);
        //_logger.info("sourceMInstPhyId_i : " + sourceMInstPhyId_i);
        if (UIUtil.isNotNullAndNotEmpty(sourceMInstPhyId_i)) {

            //源版本 MBOM关联的EBOM实例 physicalid
            String sourceEInstPhyId = (String) sourceM2EInstMapping.get(sourceMInstPhyId_i);
            //_logger.info("sourceEInstPhyId : " + sourceEInstPhyId);

            if(sEInstPhyMapping.containsKey(sourceEInstPhyId)){
                eInstPhyId =  sEInstPhyMapping.get(sourceEInstPhyId);
            }
        }
        return eInstPhyId;
    }

    /**
     * @param sourceEBomList
     * @param sourceMBomList
     * @return Map
     * @Author HLY
     * @CreateTime 2024/10/18
     * @Description: 获取源EBOM MBOM 实例 logicalid 和 physicalid的映射关系
     */
    private static Map getSourceInstMapping(MapList sourceEBomList, MapList sourceMBomList) {

        Map sourceLPMapping = new HashMap();
        for (int i = 0; i < sourceEBomList.size(); i++) {
            Map map_i = (Map) sourceEBomList.get(i);
//            String eInstLogId_i = (String) map_i.get(Lead_Constants_mxJPO.RELLOGIDKEY);
//            String eInstPhyId_i = (String) map_i.get(Lead_Constants_mxJPO.RELPHYIDKEY);
//            sourceLPMapping.put(eInstLogId_i, eInstPhyId_i);
        }
        for (int i = 0; i < sourceMBomList.size(); i++) {
            Map map_i = (Map) sourceMBomList.get(i);
//            String mInstLogId_i = (String) map_i.get(Lead_Constants_mxJPO.RELLOGIDKEY);
//            String mInstPhyId_i = (String) map_i.get(Lead_Constants_mxJPO.RELPHYIDKEY);
//            sourceLPMapping.put(mInstLogId_i, mInstPhyId_i);
        }
        //_logger.info("sourceLPMapping : "+sourceLPMapping);

        return sourceLPMapping;
    }

    /**
     * @description 为E物料和P物料之间创建Scope关系
     * @param[1] context
     * @param[2] eId E物料ID
     * @param[3] pId P物料ID
     * @author Yang Le
     * @time 2023/9/27 11:14
     */
    public static void createScopeLinkForPBOM(Context context, String eId, String pId) throws Exception {
        _logger.info("enter function createScopeLinkForPBOM().....");
        boolean isPushed = false;
        try {
            if (UIUtil.isNullOrEmpty(eId) || UIUtil.isNullOrEmpty(pId)) {
                return;
            }
            ContextUtil.pushContext(context);
            isPushed = true;
            ContextUtil.startTransaction(context, true);
            String ebomPId = new DomainObject(eId).getPhysicalId(context);
            String pbomPId = new DomainObject(pId).getPhysicalId(context);
            ArrayList array = new ArrayList();
            HashMap hashMap = new HashMap();
            //调用OOTBapi创建Scope关系
            CommonCRUDProvider provider = new CommonCRUDProvider(CommonCRUDProvider.DB_3DSpace, context);
            String providerNewId = provider.getNewId();
            provider.addEntryToCRUDMap(hashMap, CommonCRUDProvider.Action, CommonCRUDProvider.Action_Create);
            provider.addEntryToCRUDMap(hashMap, CommonCRUDProvider.Type, "dsmfg:ScopeEngItem");
            provider.addEntryToCRUDMap(hashMap, CommonCRUDProvider.Id, providerNewId);
            //P物料id
            provider.addEntryToCRUDMap(hashMap, CommonCRUDProvider.From_IDs, pbomPId);
            //E物料id
            provider.addEntryToCRUDMap(hashMap, CommonCRUDProvider.To_IDs, ebomPId);
            array.add(hashMap);
            provider.invokeAdd(array);
            provider.invokeSave();
            //获取执行返回状态和信息
            CommonCRUDProvider.CommonCRUDStatusCode statusCode = provider.getStatusCode();
            _logger.info("statusCode = " + statusCode);
            String statusMessage = provider.getStatusMessage();
            _logger.info("statusMessage = " + statusMessage);
            if (CommonCRUDProvider.CommonCRUDStatusCode.CRUD_SUCCESS != statusCode) {
                //如果创建失败，抛出异常
                // String msg = EnoviaResourceBundle.getProperty(context, "emxFrameworkStringResource", context.getLocale(), "emxFramework.common.EBOMCreateScopeFailed");
                // msg = String.format(msg, eId) + statusMessage;
                throw new Exception(statusMessage);
            }
            // else {
            //     logger.info("创建E物料" + eId + "的Scope关系成功");
            // }

            ContextUtil.commitTransaction(context);
            ContextUtil.popContext(context);
            isPushed = false;
        } catch (Exception e) {
            ContextUtil.abortTransaction(context);
            e.printStackTrace();
            throw e;
        } finally {
            if (isPushed) {
                ContextUtil.popContext(context);
            }
        }
        _logger.info("enter function createScopeLinkForPBOM().....");
    }


    /**
     * @return StringList
     * @description 通过E物料 id查询对应的P物料
     * @param[1] context
     * @param[2] engineeringId E物料ID
     * @author Yang Le
     * @time 2023/9/21 15:33
     */
    public static StringList getManufacturingItemFromEngineeringItem(Context context, String engineeringId) throws Exception {
//        logger.info("enter function getManufacturingItemFromEngineeringItem().....");
        StringList sl = new StringList();
        StringList idList = new StringList();
        try {
            //校验E物料id是否为空
            if (UIUtil.isNullOrEmpty(engineeringId)) {
                return sl;
            }
            //获取E物料的physicalid
            DomainObject engineeringObj = new DomainObject(engineeringId);
            String physicalId = engineeringObj.getPhysicalId(context);
            //查询对应的P物料
            //调用ootb已有api进行查询
            INavBusObject processBus = NavBusUtil.getNavBusObject(context, physicalId);
            ArrayList processBusList = new ArrayList();
            processBusList.add(processBus);
            IMBOMNavigationServices services = MBOMServicesProvider.getNavigationServices();

            List<List<INavBusObject>> scopingReferencesFromList = services.getScopingReferencesFromList(context, processBusList);
            for (int i = 0; i < scopingReferencesFromList.size(); i++) {
                List<INavBusObject> iNavBusObjects = scopingReferencesFromList.get(i);
                for (INavBusObject iNavBusObject : iNavBusObjects) {
                    String pid = com.dassault_systemes.pprRestServices.utils.NavBusUtil.getPIDFromNavBus(iNavBusObject);
                    if (UIUtil.isNotNullAndNotEmpty(pid)) {
                        sl.add(pid);
                    }
                }
            }
            //去重
            Set<String> set = new HashSet<>(sl);
            String[] strings = set.toArray(new String[]{});
            idList = new StringList(strings);
            _logger.info("查询P物料结果:" + idList.toString());
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
//        logger.info("exiting function getManufacturingItemFromEngineeringItem().....");
        return idList;
    }

    /**
     * @return String
     * @description 通过P物料Id 查询对应的E物料
     * @param[1] context
     * @param[2] ManufacturingId P物料ID
     * @author Yang Le
     * @time 2023/9/21 15:46
     */
    public static String getEngineeringItemFromManufacturingItem(Context context, String manufacturingId) throws Exception {
//        logger.info("enter function getEngineeringItemFromManufacturingItem()..");
        String engineeringId = "";
        try {
//            logger.info("manufacturingId:" + manufacturingId);
            //校验P物料id是否为空
            if (UIUtil.isNullOrEmpty(manufacturingId)) {
                return engineeringId;
            }
            //获取P物料的physicalid
            DomainObject manufacturingObject = new DomainObject(manufacturingId);
            String physicalId = manufacturingObject.getPhysicalId(context);
//            logger.info("physicalId:" + physicalId);
            //api限制查询时需要开启事务
            ContextUtil.startTransaction(context, true);
            //查询对应的E物料
            //调用ootb已有api进行查询
            CommonCRUDProvider provider = new CommonCRUDProvider(CommonCRUDProvider.DB_3DSpace, context);
            ArrayList arrayList = new ArrayList();
            arrayList.add(CommonCRUDProvider.To_IDs);
            String providerNewId = provider.getNewId();
            HashMap hashMap = new HashMap();
            ArrayList array = new ArrayList();
            provider.addEntryToCRUDMap(hashMap, CommonCRUDProvider.Action, Arrays.asList(CommonCRUDProvider.Action_Read));
            provider.addEntryToCRUDMap(hashMap, CommonCRUDProvider.Select_Attrs, arrayList);
            provider.addEntryToCRUDMap(hashMap, CommonCRUDProvider.Type, Arrays.asList("DELFmiProcessImplementCnx"));
            provider.addEntryToCRUDMap(hashMap, CommonCRUDProvider.Id, Arrays.asList(providerNewId));
            provider.addEntryToCRUDMap(hashMap, CommonCRUDProvider.From_IDs, Arrays.asList(physicalId));
            array.add(hashMap);
            provider.invokeAdd(array);
            provider.invokeSave();
            Map map = provider.getMapOfAttributesByPId();
//            logger.info("查询E物料结果:" + map);
            Map map1 = (Map) map.get("1");
            if (!map1.isEmpty()) {
                List<String> toIds = (List) map1.get(CommonCRUDProvider.To_IDs);
//                logger.info("todis:" + toIds);
                //list也可能为null
                if (toIds!=null&&toIds.size() > 0) {
                    //获取的元素可能是null
                    engineeringId = toIds.get(0);
                }
            }

            ContextUtil.commitTransaction(context);
        } catch (Exception e) {
            ContextUtil.abortTransaction(context);
            e.printStackTrace();
            //throw e; //mod by hly 20231115
        }
        _logger.info("exiting function getEngineeringItemFromManufacturingItem()..{}",engineeringId);
        return engineeringId;
    }
    /*
     * @description:删除MBOM上的所有Scope关系
     * @author: caipan
     * @date: 2025/3/26 13:49:52
     * @param: * @param[1] context
     * @param[2] pId
     * @return:
     **/
    public static void deleteScopeLinkForMBOM(Context context, String pId) throws Exception {
        _logger.info("enter function deleteScopeLinkForMBOM().....");
        boolean isPushed = false;
        try {
            ContextUtil.pushContext(context);
            isPushed = true;
            ContextUtil.startTransaction(context, true);
            if (UIUtil.isNullOrEmpty(pId)) {
                String msg = EnoviaResourceBundle.getProperty(context, "emxFrameworkStringResource", context.getLocale(), "emxFramework.common.PBOMIsNull");
                throw new Exception(msg);
            }
            //pId不为空
            //获取physicalId
            String physicalId = new DomainObject(pId).getPhysicalId(context);
            //调用ootb删除的api
            ArrayList array = new ArrayList();
            HashMap hashMap = new HashMap();
            CommonCRUDProvider provider = new CommonCRUDProvider(CommonCRUDProvider.DB_3DSpace, context);
            String providerId = provider.getNewId();
            provider.addEntryToCRUDMap(hashMap, CommonCRUDProvider.Action, CommonCRUDProvider.Action_Delete);
            provider.addEntryToCRUDMap(hashMap, CommonCRUDProvider.Type, "DELFmiProcessImplementCnx");
            provider.addEntryToCRUDMap(hashMap, CommonCRUDProvider.Id, providerId);
            provider.addEntryToCRUDMap(hashMap, CommonCRUDProvider.From_IDs, physicalId);
            array.add(hashMap);
            provider.invokeAdd(array);
            provider.invokeSave();
            //获取执行返回状态和信息
            CommonCRUDProvider.CommonCRUDStatusCode statusCode = provider.getStatusCode();
            _logger.info("statusCode = " + statusCode);
            String statusMessage = provider.getStatusMessage();
            _logger.info("statusMessage = " + statusMessage);
            if (CommonCRUDProvider.CommonCRUDStatusCode.CRUD_SUCCESS != statusCode) {
                //如果删除失败，抛出异常
                String msg = EnoviaResourceBundle.getProperty(context, "emxFrameworkStringResource", context.getLocale(), "emxFramework.common.DeletePBOMScopeFailed");
                msg = String.format(msg, physicalId) + statusMessage;
                throw new Exception(msg);
            }
            // else {
            //     logger.info("删除P物料" + physicalId + "的Scope关系成功");
            // }

            ContextUtil.commitTransaction(context);
            ContextUtil.popContext(context);
            isPushed = false;
        } catch (Exception e) {
            ContextUtil.abortTransaction(context);
            e.printStackTrace();
            throw e;
        } finally {
            if (isPushed) {
                ContextUtil.popContext(context);
            }
        }
        _logger.info("exiting function deleteScopeLinkForMBOM().....");
    }
    /*
     * @description:升版数据，调用OOTB API
     * @author: caipan
     * @date: 2025/3/26 15:55:44
     * @param: * @param[1] context
     * @param[2] args
     * @return:
     **/
    public void majorRevise(Context context,String[] args) throws Exception{
        ContextUtil.startTransaction(context, true);
        ReviseResource reviseResource = new ReviseResource();
        Map revise = reviseResource.revise(context, args);
        _logger.info("revise:{}",revise);
        ContextUtil.commitTransaction(context);
    }

}
