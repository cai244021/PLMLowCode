import com.matrixone.apps.common.CommonDocument;
import com.matrixone.apps.common.RouteWorkflow;
import com.matrixone.apps.common.util.ComponentsUIUtil;
import com.matrixone.apps.common.util.ComponentsUtil;
import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.DomainRelationship;
import com.matrixone.apps.domain.util.*;
import com.matrixone.apps.framework.ui.UIUtil;
import matrix.db.Context;
import matrix.db.JPO;
import matrix.db.RelationshipType;
import matrix.util.StringList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

import static com.matrixone.apps.domain.DomainConstants.*;

public class JF_NewECRProcess_mxJPO {
    private static final Logger logger = LoggerFactory.getLogger(JF_NewECRProcess_mxJPO.class);
    private static HashSet<String> allOkPartIdList = new HashSet();
    /*
     * @description: 获取受影响父件
     * @author: caipan
     * @date:
     * @param: * @param[1] context
     * @param[2] ecrId
     * @return:
     **/
    /*public void processAffectedItem(Context context, String ecrId) throws Exception{
        logger.info("processAffectedItem start");
        DomainObject ecrObj = DomainObject.newInstance(context);
        ecrObj.setId(ecrId);
        StringList busSelects = new StringList();
        busSelects.add(DomainConstants.SELECT_ID);
        busSelects.add(DomainConstants.SELECT_CURRENT);

        StringList relSelects = new StringList();
        relSelects.add(DomainConstants.SELECT_RELATIONSHIP_ID);
        //获取受影响对象
        MapList affectList = ecrObj.getRelatedObjects(context,  JF_PLMConstants_mxJPO.REL_JFRelateItem, JF_PLMConstants_mxJPO.TYPE_VPMReference,busSelects, relSelects, false, true, (short) 1, "", "", 0);
        StringList lists = ecrObj.getInfoList(context, "from[JFRelateItem].to.id");
        StringList listsSubPartId = ecrObj.getInfoList(context, "from[JFRelateItemParent].attribute[JFSubPartId]");
        StringList listPartId = new StringList();
        for(int i=0;i<listsSubPartId.size();i++){
            if(!listPartId.contains(listsSubPartId.get(i))){
                listPartId.add(listsSubPartId.get(i));
            }
        }
        DomainObject partObj = DomainObject.newInstance(context);
        Map map = null;
        Map parentMap = null;
        String partId ="";
        String parentId ="";
        String connectId ="";
        StringBuffer mqlWhere = new StringBuffer();
        mqlWhere.append("current==RELEASED");
        for(int i=0;i<affectList.size();i++){
            map = (Map)affectList.get(i);
            partId = UIUtil.getValue(map, DomainConstants.SELECT_ID);

            //判断是否已经生成  查关系属性的受影响对象ID，是否已经生成
            //如果生成了，就不往下执行
            if(listPartId.contains(partId)){
                continue;
            }
            //获取当前零件的最新发布版本---当前按理是冻结版本
            logger.info("受影响对象冻结版本partId:{}",partId);
            String lastReleasedId = JF_Util_mxJPO.getLastReleasedMajorid(context, partId);
            logger.info("受影响对象最新发布版本:{}",lastReleasedId);
            if(UIUtil.isNullOrEmpty(lastReleasedId)){
                continue;
            }
            partObj.setId(lastReleasedId);
            //获取当前对象的最新发布版本的父，如果不是的话就跳过
            MapList parentList = partObj.getRelatedObjects(context, JF_PLMConstants_mxJPO.REL_Instance,JF_PLMConstants_mxJPO.TYPE_VPMReference, busSelects, relSelects, true, false, (short) 1, mqlWhere.toString(), "", 0);
            for(int j =0;j<parentList.size();j++){
                parentMap = (Map)parentList.get(j);
                parentId = UIUtil.getValue(parentMap, DomainConstants.SELECT_ID);
                //如果找出来的父在当前ECR的受影响对象中就跳过
                if(lists.contains(parentId)){
                    continue;
                }
                if(JF_Util_mxJPO.getLastReleasedMajorid(context, parentId).equalsIgnoreCase(parentId)){//是最新发布版本
                    connectId = UIUtil.getValue(parentMap, DomainConstants.SELECT_RELATIONSHIP_ID);//拿到关系ID
                    createAffectParent(context, ecrId, connectId, parentId, partId);//建立ECR和受影响父件的关系
                }
            }
        }
        //如果目前有移除的话

    *//*    for(int i=0;i<listPartId.size();i++){
            if(!lists.contains(listPartId.get(i))){
            //说明受影响对象移除了 需要断开受影响父件
                StringBuffer where = new StringBuffer();
                where.append("attribute[JFSubPartId]=='");
                where.append(listPartId.get(i)).append("'");
                MapList disparentId = ecrObj.getRelatedObjects(context,  JF_PLMConstants_mxJPO.REL_JFRelateItemParent, JF_PLMConstants_mxJPO.TYPE_VPMReference,busSelects, relSelects, false, true, (short) 1, "", "", 0);
                for (int j=0;j<disparentId.)
            }
        }
*//*
        logger.info("processAffectedItem end");
    }*/
    /*
     * @description: 增加计算受影响父
     * @author: caipan
     * @date:
     * @param: * @param[1] context
     * @param[2] args
     * @return:
     **/
    public void processAffectedItemAdd(Context context,String[] args)throws Exception{
        logger.info("processAffectedItemAdd");
        HashMap requestmap = JPO.unpackArgs(args);
        String ecrId = UIUtil.getValue(requestmap, "ecrId");
        StringList partList = (StringList) requestmap.get("partList");//添加的受影响ID
        DomainObject domainObject = DomainObject.newInstance(context);
        domainObject.setId(ecrId);
        StringList strECRRelateRootList = domainObject.getInfoList(context, "from[JFECRRelateRoot].to.id");//ECR受影响对象
        String strProjectId = domainObject.getInfo(context, "from[JFChange2Project].to.id");//ECR所属项目
        domainObject.setId(strProjectId);
        //获取项目中的零件清单
        StringList projectPartList = domainObject.getInfoList(context, "from[JFProject2RootPart].to.id");
        DomainObject partObj = DomainObject.newInstance(context);
        StringList busSelects = new StringList();
        busSelects.add(DomainConstants.SELECT_ID);
        busSelects.add(DomainConstants.SELECT_CURRENT);
        busSelects.add(JF_PLMConstants_mxJPO.select_attr_JF_ISWholeChair);
        busSelects.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JF_PartType);
        StringList relSelects = new StringList();
        relSelects.add(DomainConstants.SELECT_RELATIONSHIP_ID);

        Map parentMap = null;
        String partId ="";
        String parentId ="";
        String connectId ="";
        StringBuffer mqlWhere = new StringBuffer();
        mqlWhere.append("current==RELEASED");
        for(int i=0;i<partList.size();i++) {
            //获取当前零件的最新发布版本
            partId = partList.get(i);
            logger.info("受影响对象冻结版本partId:{}", partId);
            String lastReleasedId = JF_Util_mxJPO.getLastReleasedMajorid(context, partId);
            logger.info("受影响对象最新发布版本:{}", lastReleasedId);
            if (UIUtil.isNullOrEmpty(lastReleasedId)) {
                continue;
            }
            partObj.setId(lastReleasedId);
            //获取当前对象的父,--发布--冻结，如果不是的话就跳过
            MapList parentList = partObj.getRelatedObjects(
                    context,
                    JF_PLMConstants_mxJPO.REL_Instance,
                    JF_PLMConstants_mxJPO.TYPE_VPMReference,
                    busSelects,
                    relSelects,
                    true,
                    false,
                    (short) 1,
                    mqlWhere.toString(),
                    "",
                    0);
            logger.info("受影响对象最新发布版本的父级:{}", parentList);
            for (int j = 0; j < parentList.size(); j++) {
                parentMap = (Map) parentList.get(j);
                parentId = UIUtil.getValue(parentMap, DomainConstants.SELECT_ID);
                logger.info("父级id：{}.....", parentId);
                //如果找出来的父在当前ECR的受影响对象中就跳过
                if (strECRRelateRootList.contains(parentId)) {//获取这个最新发布版本的，所有ID，如果存在这个里面按理都应该跳过--包括已经关联的或者还未关联的
                    continue;
                }
                logger.info("找出来的父不在当前ECR的受影响对象中.....");
                //父级不是最新发布版本，跳过
                if (!JF_Util_mxJPO.getLastReleasedMajorid(context, parentId).equalsIgnoreCase(parentId)) {
                    continue;
                }
                logger.info("父级是最新发布版本.....");
                //判断父级是否是ECR的项目中  如果是 建立关系
                if (projectPartList.contains(parentId)) {
                    logger.info("conn： {}", parentId);
                    String wholeChair = UIUtil.getValue(parentMap, JF_PLMConstants_mxJPO.select_attr_JF_ISWholeChair);
                    String partType = UIUtil.getValue(parentMap, JF_PLMConstants_mxJPO.SELECT_ATTR_JF_PartType);
                    connectId = UIUtil.getValue(parentMap, DomainConstants.SELECT_RELATIONSHIP_ID);//拿到关系ID
                    if ("WholeChair".equalsIgnoreCase(wholeChair) && "C".equalsIgnoreCase(partType)) {
                        createAffectParent(context, ecrId, connectId, parentId, partId);
                    } else {
                        createAffectParentSetAttr(context, ecrId, connectId, parentId, partId, "N");//建立ECR和受影响父件的关系
                    }
                }
            }
        }
    }

    /*
     * @description: 移除计算受影响父
     * @author: caipan
     * @date:
     * @param: * @param[1] context
     * @param[2] args
     * @return:
     **/
    public void processAffectedItemRemove(Context context,String[] args)throws Exception{
        try {
            logger.info("processAffectedItemRemove start");
            HashMap requestmap = JPO.unpackArgs(args);
            logger.info("requestmap:{}", requestmap);
            String ecrId = UIUtil.getValue(requestmap, "ecrId");
            StringList partList = (StringList) requestmap.get("partList");//移除的受影响ID
            DomainObject ecrObj = DomainObject.newInstance(context);
            ecrObj.setId(ecrId);


            for (int i = 0; i < partList.size(); i++) {
                StringBuffer where = new StringBuffer();
                where.append("attribute[JFSubPartId]=='");
                where.append(partList.get(i)).append("'");
                logger.info("where:{}", where);
                MapList disparentId = ecrObj.getRelatedObjects(context, JF_PLMConstants_mxJPO.REL_JFRelateItemParent, JF_PLMConstants_mxJPO.TYPE_VPMReference, JF_Util_mxJPO.basicBolistSel(),JF_Util_mxJPO.basicRellistSel(), false, true, (short) 1, "", where.toString(), 0);

                for (int j = 0; j < disparentId.size(); j++) {
                    //删除该关系
                    Map map = (Map) disparentId.get(j);
                    String relId = UIUtil.getValue(map, DomainConstants.SELECT_RELATIONSHIP_ID);
                    logger.info("relId:{}", relId);
                    MqlUtil.mqlCommand(context, true, "del connection '" + relId + "'", true);//需要看下是否有历史记录
                }
            }
            logger.info("processAffectedItemRemove end");
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    /*
     * @description: 创建受影响父件和ECR关系
     * @author: caipan
     * @date:
     * @param: * @param[1] context
     * @param[2] ecrId
     * @param[3] connectId
     * @param[4] parentId
     * @param[5] partId
     * @return:
     **/
    public  void createAffectParent(Context context, String ecrId, String connectId, String parentId, String partId) throws Exception{
        logger.info("createAffectParent start");
        logger.info("ecrId:{},旧关系ID:{},父ID:{},partId:{}",ecrId,connectId,parentId,partId);
        DomainObject ecrObj = DomainObject.newInstance(context);
        ecrObj.setId(ecrId);
        DomainRelationship affParent= ecrObj.addToObject(context, new RelationshipType(JF_PLMConstants_mxJPO.REL_JFRelateItemParent),parentId);
        Map<String, String> attributeMap = new HashMap<String, String>();
        attributeMap.put(JF_PLMConstants_mxJPO.ATTR_JFSubPartId,partId);
        attributeMap.put(JF_PLMConstants_mxJPO.ATTR_JFSubConnectId,connectId);
        attributeMap.put(JF_PLMConstants_mxJPO.ATTR_JFSubIFReplace,"N");
        attributeMap.put(JF_PLMConstants_mxJPO.ATTR_JFIslocked, "Y");
        affParent.setAttributeValues(context,attributeMap);
        logger.info("createAffectParent end");
    }


    /*
     * @description: 创建受影响父件和ECR关系
     * @author: caipan
     * @date:
     * @param: * @param[1] context
     * @param[2] ecrId
     * @param[3] connectId
     * @param[4] parentId
     * @param[5] partId
     * @return:
     **/
    public  void createAffectParentSetAttr(Context context, String ecrId, String connectId, String parentId, String partId, String strSubIFReplace) throws Exception{
        logger.info("createAffectParent start");
        logger.info("ecrId:{},旧关系ID:{},父ID:{},partId:{}",ecrId,connectId,parentId,partId);
        DomainObject ecrObj = DomainObject.newInstance(context);
        ecrObj.setId(ecrId);
        DomainRelationship affParent= ecrObj.addToObject(context, new RelationshipType(JF_PLMConstants_mxJPO.REL_JFRelateItemParent),parentId);
        Map<String, String> attributeMap = new HashMap<String, String>();
        attributeMap.put(JF_PLMConstants_mxJPO.ATTR_JFSubPartId,partId);
        attributeMap.put(JF_PLMConstants_mxJPO.ATTR_JFSubConnectId,connectId);
        attributeMap.put(JF_PLMConstants_mxJPO.ATTR_JFSubIFReplace,strSubIFReplace);
        attributeMap.put(JF_PLMConstants_mxJPO.ATTR_JFIslocked, "Y");
        affParent.setAttributeValues(context,attributeMap);
        logger.info("createAffectParent end");
    }


    public void testAffectAdd(Context context,String[] args) throws Exception{
        HashMap<String, java.io.Serializable> map =new HashMap<String, java.io.Serializable>();
        map.put("ecrId",args[0]);
        StringList list = new StringList();
        list.add("21798.25570.44534.63205");
        map.put("partList",list);
        processAffectedItemAdd(context, JPO.packArgs(map));
    }
    public void testAffectRemove(Context context,String[] args) throws Exception{
        HashMap<String, java.io.Serializable> map =new HashMap<String, java.io.Serializable>();
        map.put("ecrId",args[0]);
        StringList list = new StringList();
        list.add("21798.25570.44534.63205");
        map.put("partList",list);
        processAffectedItemRemove(context, JPO.packArgs(map));
    }
    /*
     * @description:替换Instance关系,如果选择是就替换关系
     * @author: caipan
     * @date:
     * @param: * @param[1] context
     * @param[2] args  ECRID
     * @return:
     **/
    public void replaceInstance(Context context,String[] args) throws Exception {
        logger.info("replaceInstance start");
        try {
            ContextUtil.pushContext(context);
            String ecrId = args[0];
            DomainObject ecrObj = DomainObject.newInstance(context);
            ecrObj.setId(ecrId);
            StringList relList = JF_Util_mxJPO.basicRellistSel();
            relList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JFSubIFReplace);
            relList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JFSubPartId);
            relList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JFSubConnectId);
            MapList parentList = ecrObj.getRelatedObjects(context, JF_PLMConstants_mxJPO.REL_JFRelateItemParent, JF_PLMConstants_mxJPO.TYPE_VPMReference, JF_Util_mxJPO.basicBolistSel(), relList, false, true, (short) 1, "", "", 0);
            Map map = null;
            String connectId = null;
            String partId = null;
            String ifReplace = null;
            DomainObject partObj = new DomainObject();
            for (int i = 0; i < parentList.size(); i++) {
                map = (Map) parentList.get(i);
                connectId = UIUtil.getValue(map, JF_PLMConstants_mxJPO.SELECT_ATTR_JFSubConnectId);
                partId = UIUtil.getValue(map, JF_PLMConstants_mxJPO.SELECT_ATTR_JFSubPartId);
                partObj.setId(partId);
                ifReplace = UIUtil.getValue(map, JF_PLMConstants_mxJPO.SELECT_ATTR_JFSubIFReplace);
                logger.info("是否替换:{},connectId:{}", ifReplace);
                if ("Y".equalsIgnoreCase(ifReplace)) {
                    DomainRelationship.setToObject(context, connectId, partObj);//替换Instance关系
                }
            }
            logger.info("replaceInstance end");
        }catch (Exception ex){
            throw new Exception("replaceInstance error:"+ex.getMessage());
        }finally {
            ContextUtil.popContext(context);
        }
    }

    /*
     * @description:推动ECR受影响对象到发布
     * @author: caipan
     * @date:
     * @param: * @param[1] context
     * @param[2] args  ECRID
     * @return:
     **/
    public void promoteAffectItemToRelease(Context context,String[] args) throws Exception {
        logger.info("promoteAffectItemToRelease start");
        try {
            ContextUtil.pushContext(context);
            String ecrId = args[0];
            DomainObject ecrObj = DomainObject.newInstance(context);
            ecrObj.setId(ecrId);
            StringList relList = JF_Util_mxJPO.basicRellistSel();
            relList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JJFIsFollow);
            String where ="current==FROZEN" ;
            MapList itemList = ecrObj.getRelatedObjects(context, JF_PLMConstants_mxJPO.REL_JFRelateItem, JF_PLMConstants_mxJPO.TYPE_VPMReference, JF_Util_mxJPO.basicBolistSel(), relList, false, true, (short) 1, where, "", 0);
            Map map = null;
            String partId = null;
            StringList vpmIds = new StringList();
            HashSet documentsSet = new HashSet();
            DomainObject partObj = new DomainObject();
//            String queryDrw= "to[XCADBaseDependency|from.type==Drawing&&from.current=='FROZEN'].from.id";//查询冻结状态的V5图纸
            for (int i = 0; i < itemList.size(); i++) {
                map = (Map) itemList.get(i);
                partId = UIUtil.getValue(map, DomainConstants.SELECT_ID);
                String JJFIsFollow = UIUtil.getValue(map, JF_PLMConstants_mxJPO.SELECT_ATTR_JJFIsFollow);
                MqlUtil.mqlCommand(context, false, "mod bus '" + partId + "' JF_VPMReference.JF_ConnectECR '"+ecrId+"'", true);
                //增加图纸发布
                partObj.setId(partId);
                vpmIds.add(partId);
                if("Y".equalsIgnoreCase(JJFIsFollow)) {
//                    StringList drwList = partObj.getInfoList(context, queryDrw);
                    MapList drwList = partObj.getRelatedObjects(context, JF_PLMConstants_mxJPO.REL_XCADBaseDependency, JF_PLMConstants_mxJPO.TYPE_Drawing, JF_Util_mxJPO.basicBolistSel(), relList, true, false, (short) 1, where, "", 0);
                    logger.info("drwList:{}",drwList);
                    if (drwList.size() > 0) {
                        for(int j=0;j<drwList.size();j++) {
                            Map temp = (Map)drwList.get(j);
                            vpmIds.addAll(UIUtil.getValue(temp, DomainConstants.SELECT_ID));
                        }
                    }
                    MapList documentDrwMapList = partObj.getRelatedObjects(context, "Reference Document", "Document", StringList.create("id"), new StringList(),
                            false, true, (short) 1, "current==FROZEN&&attribute[JF_DocumentType].value==Drawing", "", 0);
                    logger.info("documentDrwMapList:{}",documentDrwMapList);
                    for (Object o : documentDrwMapList) {
                        Map map1 = (Map) o;
                        vpmIds.add(UIUtil.getValue(map1,DomainConstants.SELECT_ID));
                        documentsSet.add(UIUtil.getValue(map1,DomainConstants.SELECT_ID));
                    }
                }
            }
            JF_Util_mxJPO util = new JF_Util_mxJPO();
            if(vpmIds.size()>0) {
                util.promoteVPM(context, vpmIds, "ToRelease");
            }
            logger.info("promoteAffectItemToRelease end");
            //零件下发布的pdf文档签字
            //签字
            partReferenceDocumentSignature(context, ecrId, StringList.create(documentsSet));
        }catch (Exception ex){
            throw new Exception("promoteAffectItemToRelease error:"+ex.getMessage());
        }finally {
            ContextUtil.popContext(context);
        }
    }

    /**
    * 在ecr中变更发布的零件，相关的二维图纸或者文档需要签字
    * @param context
    * @param ecrId
	* @param documentsList
    * @author LIUJR
    * @throws
    * @return void
    * @date 03/06/2025 13:15
    * @description
    */
    public static void partReferenceDocumentSignature(Context context, String ecrId, StringList documentsList) throws Exception{
        try {
            HashMap<String, String> signMap = new HashMap<>();
            JF_DRW_mxJPO jfDrwMxJPO = new JF_DRW_mxJPO();
            jfDrwMxJPO.getECROrDRWParamsMap(context, ecrId, signMap);
            if (signMap.isEmpty()) {
                return;
            }
            MapList mapList = new MapList();
            for (int i = 0; i < documentsList.size(); i++) {
                String documentId = documentsList.get(i);
                //文档类型，在文件是否是PDF，如果是PDF就进行签名
                MapList versionList = JF_PublicMethodClass_mxJPO.getDocumentFiles(context, documentId);
                Iterator iterator = versionList.iterator();
                while (iterator.hasNext()) {
                    Map map = (Map) iterator.next();
                    String fileId = (String) map.get(CommonDocument.SELECT_ID);
                    String fileName = (String) map.get(CommonDocument.SELECT_TITLE);
                    String fileFormat = (String) map.get(CommonDocument.SELECT_FILE_FORMAT);
                    if (fileName.endsWith(".pdf")) {
                        DomainObject fileObject = DomainObject.newInstance(context, fileId);
                        String createDate = fileObject.getInfo(context, SELECT_ORIGINATED);
                        String owner = fileObject.getInfo(context, SELECT_OWNER);
                        //组装参数
                        HashMap<String, String> paramMap = new HashMap<>();
                        paramMap.put("docId", documentId);
                        paramMap.put("fileId", fileId);
                        paramMap.put("fileName", fileName);
                        paramMap.put("fileFormat", fileFormat);
                        paramMap.put("ownerName", owner);
                        paramMap.put("ownerDate", createDate);
                        mapList.add(paramMap);
                    }
                }
            }
            if (mapList.isEmpty()) {
                return;
            }
            //开始签名
            HashMap<String, Object> paramsMap = new HashMap<>();
            paramsMap.put("list", mapList);
            paramsMap.put("signMap", signMap);
            paramsMap.put("flag", "DOC");
            ContextUtil.startTransaction(context, true);
            JF_ElectronicSignature_mxJPO.electronicSignAssembly(context, JPO.packArgs(paramsMap));
            ContextUtil.commitTransaction(context);
        } catch (Exception e) {
            e.printStackTrace();
            ContextUtil.abortTransaction(context);
            throw e;
        }
    }


    /*
     * @description: 发布关联的文档
     * @author: caipan
     * @date:
     * @param: * @param[1] context
     * @param[2] args
     * @return:
     **/
    public void promoteConnDoc(Context context,String[] args) throws Exception {
        String objectId = args[0];//对应的对象ID ，比如 ECR、DA、DRW、DR
        DomainObject obj = DomainObject.newInstance(context);
        obj.setId(objectId);
        MapList docList = obj.getRelatedObjects(context, JF_PLMConstants_mxJPO.REL_ReferenceDocument, JF_PLMConstants_mxJPO.TYPE_Document, JF_Util_mxJPO.basicBolistSel(), JF_Util_mxJPO.basicRellistSel(), false, true, (short) 1, "current!==RELEASED&&current!==OBSOLETE", "", 0);
        Map map = null;
        String current = "";
        JF_Util_mxJPO jfUtilMxJPO = new JF_Util_mxJPO();
        StringList InworkList = new StringList();
        StringList FROZENList = new StringList();
        for (int i = 0; i < docList.size(); i++) {
            map = (Map) docList.get(i);
            current = UIUtil.getValue(map, DomainConstants.SELECT_CURRENT);
            if ("IN_WORK".equalsIgnoreCase(current)) {
                InworkList.add(UIUtil.getValue(map, DomainConstants.SELECT_ID));
            } else if ("FROZEN".equalsIgnoreCase(current)) {
                FROZENList.add(UIUtil.getValue(map, DomainConstants.SELECT_ID));
            }
        }

        if (obj.isKindOf(context, "Change Action")) {
            if (InworkList.size() > 0) {
                for (int i = 0; i < InworkList.size(); i++) {
                    String mql = "mod bus " + InworkList.get(i) + " current FROZEN";
                    MqlUtil.mqlCommand(context, false, mql, true);
                    FROZENList.addAll(InworkList);
                }
            }
            if (FROZENList.size() > 0) {
                if (FROZENList.size() > 0) {
                    for (int i = 0; i < FROZENList.size(); i++) {
                        String mql = "mod bus " + FROZENList.get(i) + " current RELEASED";
                        MqlUtil.mqlCommand(context, false, mql, true);
                    }
                }
            }
        } else {
            if (InworkList.size() > 0) {
                jfUtilMxJPO.promoteVPM(context, InworkList, "ToFreeze");
                FROZENList.addAll(InworkList);
            }
            if (FROZENList.size() > 0) {
                jfUtilMxJPO.promoteVPM(context, InworkList, "ToRelease");
            }
        }
    }
    /*
     * @description:显示会签人员图形化界面
     * @author: caipan
     * @date: 2025/3/4 16:27:47
     * @param: * @param[1] context
     * @param[2] args
     * @return:
     **/
    public Map<String, StringBuffer> showTasksGraphical(Context context, String [] args) throws Exception{
//var10000 = XSSUtil.encodeForHTML(var1, var6); 转码
        Map<String, StringBuffer> resultMap = new HashMap<>();
        String ecrId = args[0];
        DomainObject ecrObj = DomainObject.newInstance(context,ecrId);
        String current = ecrObj.getInfo(context, DomainConstants.SELECT_CURRENT);
        String projectId = ecrObj.getInfo(context, "from[JFChange2Project].to.id");
        //获取 Submit Review 这两个状态的流程
        StringList ids = new StringList();
        if(current.equals("Review")||current.equals("Countersign")||current.equals("APR")||current.equals("Quotation")||current.equals("Complete")){
            String  tempId = getCurrentRoute(context, ecrId,"Review");
            if(UIUtil.isNotNullAndNotEmpty(tempId)){
                ids.add(tempId);
            }
        }
//        StringList ids =ecrObj.getInfoList(context, "from[Object Route].to.id");
        StringList taskListId =ecrObj.getInfoList(context, "from[JFECR2Task].to.id");
        StringBuffer sbTasks = new StringBuffer();
        StringBuffer sbConnections = new StringBuffer();
        int nodeSize = 0;
        int resultnodeSize = 0;
        if(ids.size()>0&&(current.equals("Review")||"Countersign".equals(current) || "APR".equals(current) || "Quotation".equals(current) || "Complete".equals(current))) {//有流程
            String language = "-8";
            String routeId = "";
            if (current.equals("Review")) {//提交状态
                String submitId = getCurrentRoute(context, ecrId,null);
                logger.info("submitId:{}", submitId);
                if (UIUtil.isNullOrEmpty(submitId)) {
                    sbTasks.append("<td align=\"center\"><b><i>" + EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", context.getLocale(), "emxComponents.TaskSummary.NoTasksFound") + "</i></b></td>");
                    sbTasks.append("</tr></table>");
                } else {
                    RouteWorkflow rwObject = new RouteWorkflow(submitId);
                    Map<String, StringBuffer> map = rwObject.getRouteTaskNodes(context, false, language);
                    sbTasks.append(map.get("sbTasks"));
                    sbConnections.append(map.get("sbConnections"));
                }
            } else {
                logger.info("ids:{}",ids);
                for (int r = 0; r < ids.size(); r++) {
                    StringBuffer sbtask = new StringBuffer();
                    routeId = ids.get(r);
                    RouteWorkflow rwObject = new RouteWorkflow(routeId);
                    if (r == 0) {
                        nodeSize = getRouteNode(context, routeId);
                        resultnodeSize = nodeSize;
                    }
                    Map<String, StringBuffer> map = rwObject.getRouteTaskNodes(context, false, language);
                    analyString(map.get("sbTasks").toString(), r, current, sbtask, nodeSize, resultnodeSize,taskListId.size()).toString();
                    sbTasks.append(sbtask);
                    StringBuffer sbConnectionstemp = map.get("sbConnections");
                    //需要截取下
                    StringTokenizer tokenizer = new StringTokenizer(sbConnectionstemp.toString(), ";");
                    int tokenCount = tokenizer.countTokens();
                    for (int i = 0; i < tokenCount - 1; i++) { // 少循环一次，跳过最后一个
                        sbConnections.append(tokenizer.nextToken());
                        sbConnections.append(";");
                    }
                }
                    //获取第二个节点的流程，加上会签、APR流程
            MapList SignTask = new MapList();
            MapList APRTask = new MapList();
            MapList CustomerTask = new MapList();
            StringList selList = JF_Util_mxJPO.basicBolistSel();
            selList.add("originated");
            StringList relList = JF_Util_mxJPO.basicRellistSel();
            if(taskListId.size()>0) {
                if ("Countersign".equals(current) || "APR".equals(current) || "Quotation".equals(current) || "Complete".equals(current)) {//会签
                    sbTasks.append(allHtml(resultnodeSize));//增加全部标签
                    int taskNode = resultnodeSize - 1;
                    connectTask(sbConnections, "task" + taskNode, "all");//增加先后循序

//                SignTask.addAll(ecrObj.getInfoList(context, "from[JFECR2Task].to.id"));
                    SignTask = ecrObj.getRelatedObjects(context, JF_PLMConstants_mxJPO.RELATIONSHIP_JF_ECR_TASK, // relationship pattern
                            JF_PLMConstants_mxJPO.TYPE_JS_SIGN_TASK,                                    // object pattern
                            selList,                            // object selects
                            relList, // relationship selects
                            false,                                        // to direction
                            true,                                        // from direction
                            (short) 1,                                    // recursion level
                            "",                // object where clause
                            "",
                            (short) 0);
                }
                if ("APR".equals(current) || "Quotation".equals(current) || "Complete".equals(current)) {//APR
                    APRTask = ecrObj.getRelatedObjects(context, JF_PLMConstants_mxJPO.RELATIONSHIP_JF_ECR_TASK, // relationship pattern
                            JF_PLMConstants_mxJPO.TYPE_JF_APRTask,                                    // object pattern
                            selList,                            // object selects
                            relList, // relationship selects
                            false,                                        // to direction
                            true,                                        // from direction
                            (short) 1,                                    // recursion level
                            "",                // object where clause
                            "",
                            (short) 0);
                }
                if ("Quotation".equals(current) || "Complete".equals(current)) {//报价
                    CustomerTask = ecrObj.getRelatedObjects(context, JF_PLMConstants_mxJPO.RELATIONSHIP_JF_ECR_TASK, // relationship pattern
                            JF_PLMConstants_mxJPO.TYPE_JF_CustomerTask,                                    // object pattern
                            selList,                            // object selects
                            relList, // relationship selects
                            false,                                        // to direction
                            true,                                        // from direction
                            (short) 1,                                    // recursion level
                            "",                // object where clause
                            "",
                            (short) 0);
                    APRTask.addAll(CustomerTask);
                }
                DomainObject taskObj = DomainObject.newInstance(context);

                String review = "\u6279\u51C6";
                String submit = "\u5F85\u63D0\u4EA4";
                String complete = "\u5B8C\u6210";
                //会签是并行任务
                StringList signtaskName = new StringList();
                for (int i = 0; i < SignTask.size(); i++) {
                    //查询Title、owner
                    Map attributeMap = (Map) SignTask.get(i);
                    String userName = UIUtil.getValue(attributeMap, DomainConstants.SELECT_OWNER);
                    String objectId = UIUtil.getValue(attributeMap, DomainConstants.SELECT_ID);
                    String title = UIUtil.getValue(attributeMap, DomainConstants.SELECT_ATTRIBUTE_TITLE);
                    String taskcurrent = UIUtil.getValue(attributeMap, DomainConstants.SELECT_CURRENT);
                    String originated = UIUtil.getValue(attributeMap, "originated");
                    int count = resultnodeSize + i;
                    int topi = i * 120;
                    String top = String.valueOf(topi);
//                String left = "970";
                    String left = String.valueOf(resultnodeSize * 200 + 400);
                    if ("Active".equals(taskcurrent)) {
                        sbTasks.append(getTaskOwnerURLActive(context, userName, "task" + count, top, left, title, objectId, submit, originated));
                    } else if ("Review".equals(taskcurrent)) {
                        sbTasks.append(getTaskOwnerURLComplete(context, userName, "task" + count, top, left, title, objectId, review, originated, "Submit"));
                    } else if ("Complete".equals(taskcurrent)) {
                        sbTasks.append(getTaskOwnerURLComplete(context, userName, "task" + count, top, left, title, objectId, complete, originated, "Agree"));
                    } else {
                        sbTasks.append(getTaskOwnerURLActive(context, userName, "task" + count, top, left, title, objectId, submit, originated));
                    }
                    connectSignTask(sbConnections, "all", "task" + count);
                    if (current.equals("Countersign")) {
                        connectTask(sbConnections, "task" + count, "APR");
                    }
                    signtaskName.add("task" + count);
                }
                if (current.equals("Countersign")) {
                    int count = resultnodeSize + signtaskName.size();
                    int topi = 285;
                    String top = String.valueOf(topi);
                    String left = String.valueOf(resultnodeSize * 200 + 600);
                    Map map = getProjectRole(context, projectId, "Financial BP");
                    sbTasks.append(getTaskOwnerURLAPRInactive(context, UIUtil.getValue(map,DomainConstants.SELECT_NAME), "APR", top, left, "\u9879\u76ee\u8d22\u52a1\u7ecf\u7406/PC", UIUtil.getValue(map,DomainConstants.SELECT_ID), submit, ""));
                    sbTasks.append(endHtml(resultnodeSize, current));
                    connectTask(sbConnections, "APR", "end");
                }

                int lastcount = SignTask.size() + 1;
                //APR、客户报价是串行
                for (int i = 0; i < APRTask.size(); i++) {
                    int currentCounte = signtaskName.size() + i;
                    //查询Title、owner
                    Map attributeMap = (Map) APRTask.get(i);
                    String userName = UIUtil.getValue(attributeMap, DomainConstants.SELECT_OWNER);
                    String objectId = UIUtil.getValue(attributeMap, DomainConstants.SELECT_ID);
                    String title = UIUtil.getValue(attributeMap, DomainConstants.SELECT_ATTRIBUTE_TITLE);
                    String taskcurrent = UIUtil.getValue(attributeMap, DomainConstants.SELECT_CURRENT);
                    String originated = UIUtil.getValue(attributeMap, "originated");
                    int count = resultnodeSize + currentCounte;
                    String top = "300";
                    String left = String.valueOf((resultnodeSize + 1) * 200 + 500 + i * 200);
                    if ("Active".equals(taskcurrent)) {
                        sbTasks.append(getTaskOwnerURLActive(context, userName, "task" + count, top, left, title, objectId, submit, originated));
                    } else if ("Review".equals(taskcurrent)) {
                        sbTasks.append(getTaskOwnerURLComplete(context, userName, "task" + count, top, left, title, objectId, review, originated, "Submit"));
                    } else if ("Complete".equals(taskcurrent)) {
                        sbTasks.append(getTaskOwnerURLComplete(context, userName, "task" + count, top, left, title, objectId, complete, originated, "Agree"));
                    } else {
                        sbTasks.append(getTaskOwnerURLActive(context, userName, "task" + count, top, left, title, objectId, submit, originated));
                    }
                    if (i == 0) {//APR和会签签一遍
                        for (int k = 0; k < signtaskName.size(); k++) {
                            connectTask(sbConnections, signtaskName.get(k), "task" + String.valueOf(count));
                        }
                    }
                    if (i == APRTask.size() - 1) {//增加end节点
                        sbTasks.append(endHtml(resultnodeSize, current, (resultnodeSize + 1) * 200 + 500 + i * 200));
                        connectTask(sbConnections, "task" + count, "end");

                    } else {
                        int countlast = count + 1;
                        connectTask(sbConnections, "task" + count, "task" + countlast);
                    }
                }
            }
        }

        }else{
            sbTasks.append("<td align=\"center\"><b><i>" + EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", context.getLocale(), "emxComponents.TaskSummary.NoTasksFound") + "</i></b></td>");
            sbTasks.append("</tr></table>");

        }
        resultMap.put("sbConnections", sbConnections);
        resultMap.put("sbTasks", sbTasks);
        return resultMap;
    }
    public StringBuffer connectTask(StringBuffer sb,String source,String target){
        sb.append("jsPlumb.connect({ source:\""+source+"\", target:\""+target+"\" });");
        return sb;
    }
    public StringBuffer connectSignTask(StringBuffer sb,String source,String target){
        sb.append("jsPlumb.connect({ source:\""+source+"\", target:\""+target+"\",hasSplitTask:false });");
        return sb;
    }
    /*
     * @description:活动中的
     * @author: caipan
     * @date: 2025/3/5 13:52:03
     * @param: * @param[1] var1
     * @param[2] var2
     * @param[3] var3
     * @return:
     **/
    private String getTaskOwnerURLActive(Context context, String userName, String id,String top,String left,String title,String objectId,String state,String
        date) throws FrameworkException {
        StringBuffer sbTasks =new StringBuffer();
        String var4 = null;
        userName = PersonUtil.getFullName(context, userName);
        if (userName.length() > 17) {
            userName = userName.substring(0, 17) + "...";
        }
        if (UIUtil.isNotNullAndNotEmpty(date)) {
            int var27 = eMatrixDateFormat.getEMatrixDisplayDateFormat();
            double var28 = Double.parseDouble("-8");
            date = eMatrixDateFormat.getFormattedDisplayDateTime(context, date, false, var27, var28, context.getLocale());
        }

        sbTasks.append("<div class='task active my-task' id='"+id+"' style=\"top:"+top+"px; left:"+left+"px;\"><span class=\"action\">"+state+"</span><label class=\"object\" title=\""+title+"\" onClick=\"showModalDialog('../common/emxTree.jsp?objectId="+objectId+"')\">"+title+"</label>");
        sbTasks.append("<span class=\"assignee\" title=\"" + XSSUtil.encodeForHTML(context, userName) + "\"onClick=\"showModalDialog('../common/emxTree.jsp?objectId=" + objectId + "')\">" + XSSUtil.encodeForHTML(context, userName) + "</span>");
        sbTasks.append("<span class=\"due-date\">" + date + "</span></div>");
        return sbTasks.toString();
    }
    /*
     * @description:完成的
     * @author: caipan
     * @date: 2025/3/5 13:52:03
     * @param: * @param[1] var1
     * @param[2] var2
     * @param[3] var3
     * @return:
     **/
    private String getTaskOwnerURLComplete(Context context, String userName, String id,String top,String left,String title,String objectId,String state,String date,String comment) throws FrameworkException {
        StringBuffer sbTasks =new StringBuffer();
        String var4 = null;
        userName = PersonUtil.getFullName(context, userName);
        if (userName.length() > 17) {
            userName = userName.substring(0, 17) + "...";
        }

        if (UIUtil.isNotNullAndNotEmpty(date)) {
            int var27 = eMatrixDateFormat.getEMatrixDisplayDateFormat();
            double var28 = Double.parseDouble("-8");
            date = eMatrixDateFormat.getFormattedDisplayDateTime(context, date, false, var27, var28, context.getLocale());
        }
//        sbTasks.append("<div class=\"task completed approved\" id=\"task1\" style=\"top:0px; left:500px;\"><span class=\"action\">批准</span><label class=\"object\" title=\"2\" onClick=\"showModalDialog('../common/emxForm.jsp?form=APPRouteNodeTask&toolbar=APPRoleNodeTaskActionsToolBar&relId=73A18B56D888210067C6AC18000030A6&objectId=35845.4994.8309.61828')\">2</label><span class=\"assignee\" title=\"&#x6574;&#x6905; &#x7ecf;&#x7406;\"onClick=\"showModalDialog('../common/emxTree.jsp?objectId=35845.4994.31611.26536')\">&#x6574;&#x6905; &#x7ecf;&#x7406;</span><span class=\"due-date\">2025年3月6日</span></div>");
        sbTasks.append("<div class='task completed approved' id='"+id+"' style=\"top:"+top+"px; left:"+left+"px;\"><span class=\"action\">"+state+"</span><label class=\"object\" title=\""+title+"\" onClick=\"showModalDialog('../common/emxTree.jsp?objectId="+objectId+"')\">"+title+"</label>");
//        sbTasks.append("<span class=\"assignee\" title=\"" + XSSUtil.encodeForHTML(context, userName) + "\"onClick=\"showModalDialog('../common/emxTree.jsp?objectId=" + var3 + "')\">" + XSSUtil.encodeForHTML(context, userName) + "</span>");
        sbTasks.append("<span class=\"assignee\" title=\"" + XSSUtil.encodeForHTML(context, userName) + "\"onClick=\"showModalDialog('../common/emxTree.jsp?objectId=" + objectId + "')\">" + XSSUtil.encodeForHTML(context, userName) + "</span>");
        sbTasks.append("<span class=\"due-date\">" + date + "</span>");
        sbTasks.append("<span class=\"badge\" title=\""+comment+"\"></span></div>");
        return sbTasks.toString();
    }
    /*
     * @description:拒绝
     * @author: caipan
     * @date: 2025/3/6 10:01:48
     * @param: * @param[1] context
     * @param[2] userName
     * @param[3] id
     * @param[4] top
     * @param[5] left
     * @param[6] title
     * @param[7] objectId
     * @param[8] state
     * @param[9] date
     * @return:
     **/
    private String getTaskOwnerURLrejected(Context context, String userName,String id,String top,String left,String title,String objectId,String state,String date,String comment) throws FrameworkException {
        StringBuffer sbTasks =new StringBuffer();
        String var4 = null;
        userName = PersonUtil.getFullName(context, userName);
        if (userName.length() > 17) {
            userName = userName.substring(0, 17) + "...";
        }

        if (UIUtil.isNotNullAndNotEmpty(date)) {
            int var27 = eMatrixDateFormat.getEMatrixDisplayDateFormat();
            double var28 = Double.parseDouble("-8");
            date = eMatrixDateFormat.getFormattedDisplayDateTime(context, date, false, var27, var28, context.getLocale());
        }
//        sbTasks.append("<div class=\"task completed approved\" id=\"task1\" style=\"top:0px; left:500px;\"><span class=\"action\">批准</span><label class=\"object\" title=\"2\" onClick=\"showModalDialog('../common/emxForm.jsp?form=APPRouteNodeTask&toolbar=APPRoleNodeTaskActionsToolBar&relId=73A18B56D888210067C6AC18000030A6&objectId=35845.4994.8309.61828')\">2</label><span class=\"assignee\" title=\"&#x6574;&#x6905; &#x7ecf;&#x7406;\"onClick=\"showModalDialog('../common/emxTree.jsp?objectId=35845.4994.31611.26536')\">&#x6574;&#x6905; &#x7ecf;&#x7406;</span><span class=\"due-date\">2025年3月6日</span></div>");
        sbTasks.append("<div class='task completed rejected' id='"+id+"' style=\"top:"+top+"px; left:"+left+"px;\"><span class=\"action\">"+state+"</span><label class=\"object\" title=\""+title+"\" onClick=\"showModalDialog('../common/emxTree.jsp?objectId="+objectId+"')\">"+title+"</label>");
//        sbTasks.append("<span class=\"assignee\" title=\"" + XSSUtil.encodeForHTML(context, userName) + "\"onClick=\"showModalDialog('../common/emxTree.jsp?objectId=" + var3 + "')\">" + XSSUtil.encodeForHTML(context, userName) + "</span>");
        sbTasks.append("<span class=\"assignee\" title=\"" + XSSUtil.encodeForHTML(context, userName) + "\"onClick=\"showModalDialog('../common/emxTree.jsp?objectId=" + objectId + "')\">" + XSSUtil.encodeForHTML(context, userName) + "</span>");
        sbTasks.append("<span class=\"due-date\">" + date + "</span>");
        sbTasks.append("<span class=\"badge\" title=\""+comment+"\"></span></div>");
        return sbTasks.toString();
    }
    /*
     * @description:未活动的
     * @author: caipan
     * @date: 2025/3/5 13:57:22
     * @param: * @param[1] var1
     * @param[2] var2
     * @param[3] var3
     * @return:
     **/
    private String getTaskOwnerURLInactive(Context context, String userName,String id,String top,String left,String title,String objectId,String state,String date) throws FrameworkException {
        StringBuffer sbTasks =new StringBuffer();
        String var4 = null;
        userName = PersonUtil.getFullName(context, userName);
        if (userName.length() > 17) {
            userName = userName.substring(0, 17) + "...";
        }

        if (UIUtil.isNotNullAndNotEmpty(date)) {
            int var27 = eMatrixDateFormat.getEMatrixDisplayDateFormat();
            double var28 = Double.parseDouble("-8");
            date = eMatrixDateFormat.getFormattedDisplayDateTime(context, date, false, var27, var28, context.getLocale());
        }
//        sbTasks.append("<div class=\"task completed approved\" id=\"task1\" style=\"top:0px; left:500px;\"><span class=\"action\">批准</span><label class=\"object\" title=\"2\" onClick=\"showModalDialog('../common/emxForm.jsp?form=APPRouteNodeTask&toolbar=APPRoleNodeTaskActionsToolBar&relId=73A18B56D888210067C6AC18000030A6&objectId=35845.4994.8309.61828')\">2</label><span class=\"assignee\" title=\"&#x6574;&#x6905; &#x7ecf;&#x7406;\"onClick=\"showModalDialog('../common/emxTree.jsp?objectId=35845.4994.31611.26536')\">&#x6574;&#x6905; &#x7ecf;&#x7406;</span><span class=\"due-date\">2025年3月6日</span></div>");
        sbTasks.append("<div class='task' id='"+id+"' style=\"top:"+top+"px; left:"+left+"px;\"><span class=\"action\">"+state+"</span><label class=\"object\" title=\""+title+"\" onClick=\"showModalDialog('../common/emxTree.jsp?objectId="+objectId+"')\">"+title+"</label>");
//        sbTasks.append("<span class=\"assignee\" title=\"" + XSSUtil.encodeForHTML(context, userName) + "\"onClick=\"showModalDialog('../common/emxTree.jsp?objectId=" + var3 + "')\">" + XSSUtil.encodeForHTML(context, userName) + "</span>");
        sbTasks.append("<span class=\"assignee\" title=\"" + XSSUtil.encodeForHTML(context, userName) + "\"onClick=\"showModalDialog('../common/emxTree.jsp?objectId=" + objectId + "')\">" + XSSUtil.encodeForHTML(context, userName) + "</span>");
        sbTasks.append("<span class=\"due-date\">" + date + "</span></div>");
        return sbTasks.toString();
    }
    private String getTaskOwnerURLAPRInactive(Context context, String userName,String id,String top,String left,String title,String objectId,String state,String date) throws FrameworkException {
        StringBuffer sbTasks =new StringBuffer();
        String var4 = null;
        userName = PersonUtil.getFullName(context, userName);
        if (userName.length() > 17) {
            userName = userName.substring(0, 17) + "...";
        }

   /*     if (UIUtil.isNotNullAndNotEmpty(date)) {
            int var27 = eMatrixDateFormat.getEMatrixDisplayDateFormat();
            double var28 = Double.parseDouble("-8");
            date = eMatrixDateFormat.getFormattedDisplayDateTime(context, date, false, var27, var28, context.getLocale());
        }*/
//        sbTasks.append("<div class=\"task completed approved\" id=\"task1\" style=\"top:0px; left:500px;\"><span class=\"action\">批准</span><label class=\"object\" title=\"2\" onClick=\"showModalDialog('../common/emxForm.jsp?form=APPRouteNodeTask&toolbar=APPRoleNodeTaskActionsToolBar&relId=73A18B56D888210067C6AC18000030A6&objectId=35845.4994.8309.61828')\">2</label><span class=\"assignee\" title=\"&#x6574;&#x6905; &#x7ecf;&#x7406;\"onClick=\"showModalDialog('../common/emxTree.jsp?objectId=35845.4994.31611.26536')\">&#x6574;&#x6905; &#x7ecf;&#x7406;</span><span class=\"due-date\">2025年3月6日</span></div>");
        sbTasks.append("<div class='task' id='APR' style=\"top:"+top+"px; left:"+left+"px;\"><span class=\"action\">"+state+"</span><label class=\"object\" title=\""+title+"\" onClick=\"showModalDialog('../common/emxTree.jsp?objectId="+objectId+"')\">"+title+"</label>");
//        sbTasks.append("<span class=\"assignee\" title=\"" + XSSUtil.encodeForHTML(context, userName) + "\"onClick=\"showModalDialog('../common/emxTree.jsp?objectId=" + var3 + "')\">" + XSSUtil.encodeForHTML(context, userName) + "</span>");
        sbTasks.append("<span class=\"assignee\" title=\"" + XSSUtil.encodeForHTML(context, userName) + "\"onClick=\"showModalDialog('../common/emxTree.jsp?objectId=" + objectId + "')\">" + XSSUtil.encodeForHTML(context, userName) + "</span></div>");
//        sbTasks.append("<span class=\"due-date\">" + date + "</span></div>");
        return sbTasks.toString();
    }

    public StringBuffer analyString(String str,int count,String state,StringBuffer resultStr,int size,int nodeSize,int taskSize){
        String task0 ="id=\"task0\" style=\"top:0px; left:150px;";
        String task1 ="id=\"task1\" style=\"top:0px; left:370px;";
        String end ="id=\"end\" style=\"top:15px;left:370px;";
        String end1 ="id=\"end\" style=\"top:15px;left:590px;";
        int countleft = 0;
        String[] array = str.split("<div");
        for(int i=1;i<array.length;i++){
            logger.info("array[i]:{}:{}",i,array[i]);
            String temp = "";
            if(count==0) {
                if(!array[i].contains("id=\"end\"")) {
                    resultStr.append("<div");
                    resultStr.append(array[i]);
                }
            }
        }
        return resultStr;
    }
    public String allHtml(int nodesize){
        int allleft = nodesize*200+180;
        String allhtml = "<div class=\"node\" id=\"all\" style=\"top:300px; left:"+allleft+"px;\"><label>\u5168\u90E8</label></div>";
        return allhtml;
    }
    public String endHtml(int nodesize,String current){
        int allleft  =0;
        if(current.equals("Countersign")) {
             allleft = nodesize * 200 + 900;
        }else if(current.equals("APR")) {
            allleft = nodesize * 200 + 1000;
        }else if(current.equals("Quotation")) {
            allleft = nodesize * 200 + 1200;
        }else {
            allleft = nodesize * 200 + 1250;
        }
        String allhtml = "<div class=\"end\" id=\"end\" style=\"top:300px; left:"+allleft+"px;\"></div>";
        return allhtml;
    }

    public String endHtml(int nodesize,String current,int left){
        int allleft  =left+200;
        String allhtml = "<div class=\"end\" id=\"end\" style=\"top:315px; left:"+allleft+"px;\"></div>";
        return allhtml;
    }

    /*
     * @description:获取流程有多少个节点
     * @author: caipan
     * @date: 2025/3/7 11:00:46
     * @param: * @param[1] context
     * @param[2] rootId
     * @return:
     **/
    public int getRouteNode(Context context,String rootId)throws Exception{
        DomainObject routeObj = DomainObject.newInstance(context,rootId);
        MapList nodeList = routeObj.getRelatedObjects(context, DomainConstants.RELATIONSHIP_ROUTE_NODE, // relationship pattern
                DomainConstants.TYPE_PERSON,                                    // object pattern
                JF_Util_mxJPO.basicBolistSel(),                            // object selects
                JF_Util_mxJPO.basicRellistSel(), // relationship selects
                false,                                        // to direction
                true,                                        // from direction
                (short) 1,                                    // recursion level
                "",                // object where clause
                "",
                (short) 0);
        return nodeList.size();
    }
    public String getCurrentRoute(Context context,String rootId,String state)throws Exception{
        DomainObject routeObj = DomainObject.newInstance(context,rootId);
        String policy = routeObj.getInfo(context, "policy");
        String current = routeObj.getInfo(context, "current");
        if(UIUtil.isNotNullAndNotEmpty(state)){
            current = state;
        }
        StringList selList = JF_Util_mxJPO.basicBolistSel();
        selList.add(DomainConstants.SELECT_ORIGINATED);
        String registcurrent = FrameworkUtil.reverseLookupStateName(context, policy,current);
        MapList nodeList = routeObj.getRelatedObjects(context, DomainConstants.RELATIONSHIP_OBJECT_ROUTE, // relationship pattern
                DomainConstants.TYPE_ROUTE,                                    // object pattern
                selList,                            // object selects
                JF_Util_mxJPO.basicRellistSel(), // relationship selects
                false,                                        // to direction
                true,                                        // from direction
                (short) 1,                                    // recursion level
                "",                // object where clause
                "attribute[Route Base State]=="+registcurrent,
                (short) 0);
        nodeList.sort(DomainConstants.SELECT_ORIGINATED,"descending","date");
        String routeId = "";
        if(nodeList.size()>0){
            Map map = (Map)nodeList.get(0);
            routeId = UIUtil.getValue(map, DomainConstants.SELECT_ID);
        }
        return routeId;
    }

    public String getCurrentRoute(Context context,String[] args) throws Exception{
        String rootId = args[0];
        return getCurrentRoute(context,rootId,null);
    }

    /*
     * @description: 获取项目角色人员账号
     * @author: caipan Financial BP
     * @date: 2025/3/13 10:11:53
     * @param: * @param[1] context
     * @param[2] projectId
     * @param[3] projectRole
     * @return:
     **/
    public Map getProjectRole(Context context,String projectId,String projectRole) throws Exception{
        Map map = new HashMap();
        DomainObject projectObject = DomainObject.newInstance(context, projectId);
        StringList selList = JF_Util_mxJPO.basicBolistSel();
        StringList relList = JF_Util_mxJPO.basicRellistSel();
        relList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_ProjectRole);
        String strRelWhere = JF_PLMConstants_mxJPO.SELECT_ATTR_ProjectRole+"=='"+projectRole+"'";
        MapList mapList = projectObject.getRelatedObjects(
                context,
                DomainRelationship.RELATIONSHIP_MEMBER,
                DomainConstants.TYPE_PERSON,
                selList,
                relList,
                false,
                true,
                (short) 1, // recursion level
                null, //object where clause
                strRelWhere, //relationship where clause
                1
        );
        if(mapList.size()>0){
            map = (Map)mapList.get(0);
        }
        return map;
    }

    /*
     * @description:获取项目关联的变更单
     * @author: caipan
     * @date: 2025/4/2 13:44:25
     * @param: * @param[1] context
     * @param[2] args
     * @return:
     **/
    public MapList getProject2Change(Context context,String[] args) throws Exception{
        try {
            Map paramsMap = JPO.unpackArgs(args);
            ContextUtil.pushContext(context);
            String strObjectId = (String) paramsMap.get("objectId");
            String type = (String) paramsMap.get("JFType");
            logger.info("strObjectId:{} {}",strObjectId,type);
            DomainObject objectProject = DomainObject.newInstance(context, strObjectId);
            String relName = JF_PLMConstants_mxJPO.REL_JFChange2Project;
//            String type ="";
            StringList selList = JF_Util_mxJPO.basicBolistSel();
            StringList relList = JF_Util_mxJPO.basicRellistSel();
            MapList mlPartInfoList = new MapList();
            if("JFDataOutSource".equals(type)) {//数据外发
//                mlPartInfoList = objectProject.getRelatedObjects(context, relName, type, selList, relList, true, false, (short) 1, "", "", 0);
                String where ="attribute[JSConnectProject]=='"+strObjectId+"'";
                 mlPartInfoList = DomainObject.findObjects(context, "JFDataOutSource", "*", where, selList);
            }
           else if(!"JFECO".equals(type)) {
                 mlPartInfoList = objectProject.getRelatedObjects(context, relName, type, selList, relList, true, false, (short) 1, "", "", 0);
            }
            else{
                //通过项目找到所有的ECR，在找关联的ECO
                type = "JFECR";
                selList.add("from[JFECR2CO].to.id");
                MapList ecrList = objectProject.getRelatedObjects(context, relName, type, selList, relList, true, false, (short) 1, "", "", 0);
                Map temp = null;
                String ecoId = "";
                for(int i=0;i<ecrList.size();i++){
                    Map<String, String> map = new HashMap<>();
                    temp = (Map)ecrList.get(i);
                    logger.info("temp:{}",temp);
                    Object eco = temp.get("from[JFECR2CO].to.id");
                    if(eco instanceof StringList){
                        ecoId = ((StringList)eco).get(0);
                    }else{
                        ecoId =(String)eco;
                    }
                    if(UIUtil.isNotNullAndNotEmpty(ecoId)) {
                        map.put("id", ecoId);
                        mlPartInfoList.add(map);
                    }
                }
            }
            return mlPartInfoList;
        }catch (Exception e) {
            e.printStackTrace();

        }finally {
            ContextUtil.popContext(context);
        }
        return new MapList();
    }
    /*
     * @description:保存的时候弹出提示信息
     * @author: caipan
     * @date: 2025/4/9 11:05:44
     * @param: * @param[1] context
     * @param[2] args
     * @return:
     **/
public void notifySave(Context context,String[] args) throws Exception{
    String msg = EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", context.getLocale(), "emxComponents.ECR.saveReplaceParentMessage");
    emxContextUtil_mxJPO.mqlNotice(context, msg);
}
    /*
     * @description:获取当前ECR可以添加的一级件或者整椅件
     * @author: caipan
     * @date: 2025/4/22 10:08:00
     * @param: * @param[1] context
     * @param[2] args args[0] ECR ID
     * @return:
     **/
    public  StringList includeECRAffect(Context context,String[] args) throws Exception{
        logger.info("includeECRAffect。。。。。。。。。。。。。。。。。。。。");
        HashMap requestMap = JPO.unpackArgs(args);
        String ecrId = (String) requestMap.get("objectId");
        DomainObject ecrObj = DomainObject.newInstance(context, ecrId);
        StringList resultList = new StringList();
        String projectId = ecrObj.getInfo(context, "from["+JF_PLMConstants_mxJPO.REL_JFChange2Project+"].to.id");
        DomainObject projectObj = DomainObject.newInstance(context, projectId);
        //获取项目关联的整椅、以及他的子级 并且是冻结状态的
        StringList selList = JF_Util_mxJPO.basicBolistSel();
        StringList relList = JF_Util_mxJPO.basicRellistSel();
        String busWhere = "attribute[PLMReference.V_isLastVersion]==TRUE&&current==FROZEN";
       String relWhere = "attribute[JFZeroPart]==Y";
       String rel = JF_PLMConstants_mxJPO.rel_JFProject2RootPart;//JF_PLMConstants_mxJPO.REL_Instance;
        MapList mapList = projectObj.getRelatedObjects(context,
                rel, //pattern to match relationships
                JF_PLMConstants_mxJPO.TYPE_VPMReference, //pattern to match types
                selList, //the eMatrix StringList object that holds the list of select statement pertaining to Business Obejcts.
                relList, //the eMatrix StringList object that holds the list of select statement pertaining to Relationships.
                false, //get To relationships
                true, //get From relationships
                (short) 1, //the number of levels to expand, 0 equals expand all.
                busWhere, //where clause to apply to objects, can be empty ""
                relWhere, //where clause to apply to relationship, can be empty ""
                (short) 0); //limit
        Map map = null;
        DomainObject vpmObj = DomainObject.newInstance(context);
         rel = JF_PLMConstants_mxJPO.REL_Instance;
         MapList addPartList = new MapList();
        addPartList.addAll(mapList);
        logger.info("addPartList:{}", addPartList.toString());
        for(int i=0;i<mapList.size();i++){
            map = (Map)mapList.get(i);
            String id = UIUtil.getValue(map, DomainConstants.SELECT_ID);
            String current = UIUtil.getValue(map, DomainConstants.SELECT_CURRENT);
            vpmObj.setId(id);
            addPartList.addAll(vpmObj.getRelatedObjects(context,
                    rel, //pattern to match relationships
                    JF_PLMConstants_mxJPO.TYPE_VPMReference, //pattern to match types
                    selList, //the eMatrix StringList object that holds the list of select statement pertaining to Business Obejcts.
                    relList, //the eMatrix StringList object that holds the list of select statement pertaining to Relationships.
                    false, //get To relationships
                    true, //get From relationships
                    (short) 1, //the number of levels to expand, 0 equals expand all.
                    busWhere, //where clause to apply to objects, can be empty ""
                    "", //where clause to apply to relationship, can be empty ""
                    (short) 0)); //limit

        }
        resultList = (StringList) addPartList.stream().map(m -> {
            Map map1 = (Map)m;
            return UIUtil.getValue(map1, DomainConstants.SELECT_ID);
        }).collect(Collectors.toCollection(StringList::new));
        logger.info("resultList:{}", resultList);
        return resultList;
    }

    public StringList filterAffectedItems(Context context, String[] args) throws Exception {
        logger.info("filterAffectedItems");
        try {
            StringList typeSelectList = JF_Util_mxJPO.basicBolistSel();
            typeSelectList.add(SELECT_CURRENT);
//            typeSelectList.add("to[JFRelateItem]");
//            typeSelectList.add("to[JFECRRelateRoot]");
//            typeSelectList.add("attribute[PLMReference.V_isLastVersion]");
            logger.info("start:{}",new Date());
            MapList res = DomainObject.findObjects(context, JF_PLMConstants_mxJPO.TYPE_VPMReference, "*", "current=='FROZEN'&&to[JFECRRelateRoot]==FALSE&&to[JFRelateItem]==FALSE&&attribute[PLMReference.V_isLastVersion]==TRUE", new StringList(SELECT_ID));
//            MapList res = DomainObject.findObjects(context, JF_PLMConstants_mxJPO.TYPE_VPMReference, "attribute[PLMReference.V_isLastVersion]==true && to[JFRelateItem]==false && to[JFECRRelateRoot]==false", "*", new StringList(SELECT_ID));
            logger.info("end:{}",new Date());
            logger.info("res:{}",res.size());
            Map map = null;
            Set list = new HashSet();
      for (int i=0;i<15001;i++){
          map = (Map)res.get(i);
          list.add(UIUtil.getValue(map, SELECT_ID));
      }
            return StringList.create(list);
        }catch (Exception e){
            e.printStackTrace();
        }
        return new StringList();
    }
    /*
     * @description:ECR添加受影响对象 只添加ECR关联项目的整椅件+一级件
     * @author: caipan
     * @date: 2025/4/23 14:19:37
     * @param: * @param[1] context
     * @param[2] args
     * @return:
     **/
    public String getSearchUrl(Context context, String[] args) {
        String strObjectId = args[0];
        String strParentId = args[1];
        //AEFGeneralSearchResults
//        StringBuffer sbUrl = new StringBuffer("../common/emxFullSearch.jsp?field=TYPES=type_VPMReference:CURRENT=policy_VPLM_SMB_Definition_MajorRev.state_Review,policy_VPLM_SMB_Definition.state_Review&showInitialResults=true&table=AEFGeneralSearchResults&selection=multiple");
       //走索引查询 最新冻结版，并且没有和ECR建立Root关系
        StringBuffer sbUrl = new StringBuffer("../common/emxFullSearch.jsp?field=TYPES=type_VPMReference:CURRENT=policy_VPLM_SMB_Definition_MajorRev.state_Review,policy_VPLM_SMB_Definition.state_Review:bo.PLMReference.V_isLastVersion=true:JFRelateItemSearch=false:JFECRRelateRootSearch=false&showInitialResults=true&table=AEFGeneralSearchResults&selection=single");
        sbUrl.append("&objectId=");
        sbUrl.append(strObjectId);
        sbUrl.append("&parentId=");
        sbUrl.append(strParentId);
//        sbUrl.append("&includeOIDprogram=JF_NewECRProcess:filterAffectedItems");
        sbUrl.append("&hideHeader=true&cancelLabel=emxFramework.Command.Cancel");
        sbUrl.append("&submitURL=../common/JF_NewECRPostProcessAddAffectedItems.jsp");
        logger.info("sbUrl:{}",sbUrl);
        return sbUrl.toString();
    }

    /*
     * @description:校验提交进来的数据是否已经添加了
     * 选中添加的根节点，查询ECR已经关联的Root节点以及Root节点关联的子级、孙子级状态为冻结的(JFECRRelateRoot,JFECRRoot2Item)这两条关系
     * 如果已经存在了就报错，提示已经在哪个根节点中添加了，不需要重复添加
     * 需要考虑如果同时添加了，这种情况应该也是需要提示---提示 哪个零件，已经在哪个你选择的哪个根节点里面
     * @author: caipan
     * @date: 2025/4/24 10:08:10
     * @param: * @param[1] context
     * @param[2] args
     * @return:  返回 success 就代表没问题，返回其他都代表有问题
     **/
    public String validateECRAddExist(Context context,String[] args)throws Exception{
        try {
            Map requestMap = JPO.unpackArgs(args);
            ContextUtil.pushContext(context);
            String ecrId = UIUtil.getValue(requestMap, "ecrId");
            StringList selectList = (StringList) requestMap.get("selectList");
            DomainObject ecrObj = DomainObject.newInstance(context, ecrId);
            StringList selList = JF_Util_mxJPO.basicBolistSel();
            selList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_V_PART_NUMBER);
            StringList relList = JF_Util_mxJPO.basicRellistSel();
            //ECR 关联的系统受影响的数据
            StringList rootPartList = ecrObj.getInfoList(context, "from[JFECRRelateRoot].to.id");
            StringList partList = ecrObj.getInfoList(context, "from[JFRelateItem].to.id");
            logger.info("selectList：{}", selectList);
            logger.info("partList：{}", partList.toString());
            logger.info("rootPartList：{}", rootPartList);
            StringBuffer errorStr = new StringBuffer();
            //判断添加的件 是否是ecr项目中 获取ecr的项目的整椅件
            StringList ecrProjectPartList = ecrObj.getInfoList(context, "from[JFChange2Project].to.from[JFProject2RootPart].to.id");
            DomainObject domainObject = DomainObject.newInstance(context);
            StringList allEcrPSPartList = new StringList();
            for (int i = 0; i < ecrProjectPartList.size(); i++) {
                domainObject.setId(ecrProjectPartList.get(i));
                allEcrPSPartList.addAll((StringList)domainObject.getRelatedObjects(context,
                        JF_PLMConstants_mxJPO.REL_Instance, //pattern to match relationships
                        JF_PLMConstants_mxJPO.TYPE_VPMReference, //pattern to match types
                        selList, //the eMatrix StringList object that holds the list of select statement pertaining to Business Obejcts.
                        relList, //the eMatrix StringList object that holds the list of select statement pertaining to Relationships.
                        false, //get To relationships
                        true, //get From relationships
                        (short) 0, //the number of levels to expand, 0 equals expand all.
                        DomainConstants.EMPTY_STRING, //where clause to apply to objects, can be empty ""
                        null, //where clause to apply to relationship, can be empty ""
                        (short) 0//limit
                ).stream().map(m -> {
                    Map map = (Map) m;
                    return UIUtil.getValue(map, SELECT_ID);
                }).collect(Collectors.toCollection(StringList::new)));
            }
            //判断选择的受影响件是否是ecr项目中的
            StringList errorList = new StringList();
            allEcrPSPartList.addAll(ecrProjectPartList);
            for (int i = 0; i < selectList.size(); i++) {
                if (allEcrPSPartList.contains(selectList.get(i))) {
                    continue;
                }
                domainObject.setId(ecrProjectPartList.get(i));
                String attributeValue = domainObject.getAttributeValue(context, JF_PLMConstants_mxJPO.SELECT_ATTR_V_PART_NUMBER);
                attributeValue = UIUtil.isNullOrEmpty(attributeValue) ? domainObject.getInfo(context, DomainConstants.SELECT_NAME) : attributeValue;
                errorList.add(attributeValue);
            }
            if(errorList.size()>0){
                return JF_Util_mxJPO.getMessage(context, "NewECR.addError", errorList.toString());
            }
            errorList.clear();
            //已添加到当前ECR已有的受影响项中
            //没有在其他ecr的情况，就查看 是否存在当前ecr的已经添加的节点中
            String objectId = "";
            String partName = "";
            DomainObject part = DomainObject.newInstance(context);
            //重复添加进来的数据  partList
            HashSet hashSet = new HashSet();
            if (!partList.isEmpty() || !rootPartList.isEmpty()) {
                for (int i = 0; i < selectList.size(); i++) {
                    objectId = selectList.get(i);
                    if (partList.contains(objectId)) {
                        //如果item里面包含了 这个一级节点  不允许重复添加
                        domainObject.setId(objectId);
                        partName = domainObject.getAttributeValue(context, JF_PLMConstants_mxJPO.ATTR_V_PART_NUMBER);
                        //返回错误  提示该数据对象已经添加到当前ECR
                        hashSet.add(partName);
                    }
                    //判断添加进来的数据 是否在JFECRRelateRoot 关系层级中，如果在不允许添加
                    part.setId(objectId);
                    getPartExistECR(context, part, rootPartList, selList, relList, hashSet);
                }
                logger.info("errorList:{}", errorList);
                if (hashSet.size() > 0) {
                    return JF_Util_mxJPO.getMessage(context, "NewECR.addExistError", StringList.create(hashSet).toString());
                }
            }

            //一起批量添加的呢 处理
            //判断 当选中的是 多个的情况 需要做这个操作
//            if (selectList.size() > 1) {
//                Map zeroPartMap = new HashMap<>();
//                for (int i = 0; i < selectList.size(); i++) {
//                    objectId = selectList.get(i);
//                    DomainObject vpmObj = DomainObject.newInstance(context,objectId);
//                    String strZeroPart = vpmObj.getInfo(context, "to[JFProject2RootPart].attribute[JFZeroPart]");
//                    partName = vpmObj.getAttributeValue(context, JF_PLMConstants_mxJPO.ATTR_V_PART_NUMBER);
//                    partName = UIUtil.isNotNullAndNotEmpty(partName) ? partName : vpmObj.getInfo(context, DomainConstants.SELECT_NAME);
//                    //加入的件 所有展开结构保存
//                    zeroPartMap.put(partName, vpmObj.getRelatedObjects(context,
//                            JF_PLMConstants_mxJPO.REL_Instance, //pattern to match relationships
//                            JF_PLMConstants_mxJPO.TYPE_VPMReference, //pattern to match types
//                            selList, //the eMatrix StringList object that holds the list of select statement pertaining to Business Obejcts.
//                            relList, //the eMatrix StringList object that holds the list of select statement pertaining to Relationships.
//                            false, //get To relationships
//                            true, //get From relationships
//                            (short) 0, //the number of levels to expand, 0 equals expand all.
//                            "", //where clause to apply to objects, can be empty ""
//                            "", //where clause to apply to relationship, can be empty ""
//                            (short) 0 //limit
//                    ).stream().map(m -> {
//                                Map map = (Map) m;
//                        String number = UIUtil.getValue(map, JF_PLMConstants_mxJPO.SELECT_ATTR_V_PART_NUMBER);
//                        number = UIUtil.isNotNullAndNotEmpty(number) ? number : UIUtil.getValue(map, DomainConstants.SELECT_NAME);
//                        return  number;
//                    }).collect(Collectors.toCollection(StringList::new)));
//                }
//                Iterator iter = zeroPartMap.entrySet().iterator();
//                for (int i = 0; i < selectList.size(); i++) {
//                    objectId = selectList.get(i);
//                    DomainObject vpmObj = DomainObject.newInstance(context,objectId);
//                    partName = vpmObj.getAttributeValue(context, JF_PLMConstants_mxJPO.ATTR_V_PART_NUMBER);
//                    partName = UIUtil.isNotNullAndNotEmpty(partName) ? partName : vpmObj.getInfo(context, DomainConstants.SELECT_NAME);
//                    while (iter.hasNext()) {
//                        Map.Entry entry = (Map.Entry) iter.next();
//                        String key = (String)entry.getKey();
//                        StringList val = (StringList) entry.getValue();
//                        if (val.contains(partName)) {
//                            //包含 说明是对方的件
//                            errorStr.append(JF_Util_mxJPO.getMessage(context, "NewECR.addPartExistZeroPartError",partName, key));
//                        }
//                    }
//                }
////                //开始判断件是否在别的添加件中
////                StringList intersectionList = new StringList();
////                HashMap<String, String> stringStringHashMap = new HashMap<>();
////                Iterator iter = zeroPartMap.entrySet().iterator();
////                while (iter.hasNext()) {
////                    Map.Entry entry = (Map.Entry) iter.next();
////                    String key = (String)entry.getKey();
////                    StringList val = (StringList) entry.getValue();
////                    //求交集
////                    StringList intersection = intersectionList.stream()
////                            .filter(val::contains)
////                            .collect(Collectors.toCollection(StringList::new));
////                    if (intersection.isEmpty()) {
////
////                    }
////                    if (!intersection.isEmpty()) {
////                        errorStr.append(JF_Util_mxJPO.getMessage(context, "NewECR.addPartExistZeroPartError",intersection.join(","), key));
////                    }
////                    intersectionList.addAll(val);
////                }
//                if (!errorStr.isEmpty()){
//                    return errorStr.toString();
//                }
//            }
            String strMess = checkVPMReferenceStateIsLastRevision(context, selectList, new HashMap<>());
            if (strMess.length() > 0) {
                return strMess;
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        } finally {
            ContextUtil.popContext(context);
        }
        return "success";
    }


    /**
    * 发ECR的时候校验零件是否是最新版本
    * @param context
	* @param selectList
	* @param partMap
    * @author LIUJR
    * @throws
    * @return int
    * @date 04/06/2025 17:00
    * @description
    */
    public String checkVPMReferenceStateIsLastRevision(Context context, StringList selectList, HashMap<String, StringList> partMap)throws Exception {
        StringBuilder partState = new StringBuilder();
        StringBuilder partRelease = new StringBuilder();
        String strPartState = ComponentsUIUtil.getI18NString(context, context.getLocale().toString(), "emxComponent.Mess.ECRCheckPartState", new String[]{});
        String strPartRelease = ComponentsUIUtil.getI18NString(context, context.getLocale().toString(), "emxComponent.Mess.ECRCheckPartRelease", new String[]{});
        for (int i = 0; i < selectList.size(); i++) {
            String objectId = selectList.get(i);
            DomainObject domainObject = DomainObject.newInstance(context, objectId);
            String number = domainObject.getAttributeValue(context, JF_PLMConstants_mxJPO.ATTR_V_PART_NUMBER);
            StringList bosel = JF_Util_mxJPO.basicBolistSel();
            bosel.add("attribute[PLMReference.V_isLastVersion]");
            bosel.add("attribute[EnterpriseExtension.V_PartNumber]"); //企业编码
            StringList relsel = new StringList();
            MapList VPMList = domainObject.getRelatedObjects(
                    context,
                    JF_PLMConstants_mxJPO.REL_Instance,
                    JF_PLMConstants_mxJPO.TYPE_VPMReference,
                    bosel,
                    relsel,
                    false,
                    true,
                    (short) 0,

                    "",
                    "",
                    0
            );
            StringList partList = new StringList();
            for (Object o : VPMList) {
                Map map = (Map) o;
                String isLast = UIUtil.getValue(map, "attribute[PLMReference.V_isLastVersion]");
                String V_PartNumber = UIUtil.getValue(map, "attribute[EnterpriseExtension.V_PartNumber]");
                String current = UIUtil.getValue(map, DomainConstants.SELECT_CURRENT);
                String id = UIUtil.getValue(map, DomainConstants.SELECT_ID);
                partList.add(id);
                String name = UIUtil.getValue(map, "name");
                if (UIUtil.isNullOrEmpty(V_PartNumber)) {
                    V_PartNumber = name;
                }
                String revision = UIUtil.getValue(map, "revision");
                String msg = V_PartNumber + "_" + revision;
                if (current.equalsIgnoreCase("IN_WORK") || current.equalsIgnoreCase("FROZEN")) {
                    if (!"TRUE".equals(isLast)) {
                        partState.append(msg);
                    }
                } else if (current.equalsIgnoreCase("RELEASED")) {
                    //最新发布版本
                    if (!id.equalsIgnoreCase(JF_Util_mxJPO.getLastReleasedMajorid(context, id))) {
                        partRelease.append(msg);
                    }
                }
            }
            partMap.put(number, partList);
        }
        String result = EMPTY_STRING;
        if (partState.length() > 0) {
            result += strPartState + partState;
        }
        if (partRelease.length() > 0) {
            result += strPartRelease + partRelease;
        }
        return result;
    }

    /**
     * 校验ecr中的零件EBOM 数据结构中处在“已发布”的数据必须是最新已发布版本
     * @param context
     * @param ecrId
     * @author LIUJR
     * @throws
     * @return int
     * @date 01/27/2026 10:00
     * @description
     */
    public static String checkVPMReferenceReleaseIsLastRevision(Context context, String ecrId)throws Exception {
        String mess = EMPTY_STRING;
        try {
            DomainObject ecrObject = DomainObject.newInstance(context, ecrId);
            String ecrName = ecrObject.getInfo(context, SELECT_NAME);
            StringList rootList = ecrObject.getInfoList(context, "from[" + JF_PLMConstants_mxJPO.REL_JFECRRelateRoot + "].to.id");
            String strPartRelease = ComponentsUIUtil.getI18NString(context, context.getLocale().toString(), "emxComponent.ECRMess.CheckPartRelease", new String[]{});
            HashSet<String> errorPartList = new HashSet<>();
            StringList bosel = JF_Util_mxJPO.basicBolistSel();
            bosel.add(JF_PLMConstants_mxJPO.SELECT_ATTR_V_IsLastVersion);
            bosel.add(JF_PLMConstants_mxJPO.SELECT_ATTR_V_PART_NUMBER); //企业编码
            String current = EMPTY_STRING;
            String objectId = EMPTY_STRING;
            String v_PartNumber = EMPTY_STRING;
            String id = EMPTY_STRING;
            MapList mapVPMList = new MapList();
            Map map = new HashMap();
            DomainObject domainObject = DomainObject.newInstance(context);
            for (int i = 0; i < rootList.size(); i++) {
                objectId = rootList.get(i);
                domainObject.setId(objectId);
                mapVPMList = domainObject.getRelatedObjects(
                        context,
                        JF_PLMConstants_mxJPO.REL_Instance,
                        JF_PLMConstants_mxJPO.TYPE_VPMReference,
                        bosel,
                        JF_Util_mxJPO.basicRellistSel(),
                        false,
                        true,
                        (short) 0,
                        "",
                        "",
                        0
                );
                mapVPMList.add(domainObject.getInfo(context, bosel));
                for (Object o : mapVPMList) {
                    map = (Map) o;
                    current = UIUtil.getValue(map, DomainConstants.SELECT_CURRENT);
                    if (!current.equalsIgnoreCase("RELEASED")) {
                        continue;
                    }
                    v_PartNumber = UIUtil.getValue(map, JF_PLMConstants_mxJPO.SELECT_ATTR_V_PART_NUMBER);
                    id = UIUtil.getValue(map, DomainConstants.SELECT_ID);
                    v_PartNumber = UIUtil.isNullOrEmpty(v_PartNumber) ? UIUtil.getValue(map, SELECT_NAME) : v_PartNumber;
                    //最新发布版本
                    if (!id.equalsIgnoreCase(JF_Util_mxJPO.getLastReleasedMajorid(context, id))) {
                        errorPartList.add( v_PartNumber + "_" + UIUtil.getValue(map, SELECT_REVISION));
                    }
                }
            }
            if (!errorPartList.isEmpty()) {
                StringList stringList = StringList.create(errorPartList);
                mess = String.format(strPartRelease, ecrName, stringList.join(","));
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
        return mess;
    }


    /**
    * 获取受影响项是否存在ecr
    * @param context
	* @param part
	* @param partList
	* @param selList
	* @param relList
	* @param hashSet
    * @author LIUJR
    * @throws
    * @return void
    * @date 15/05/2025 13:44
    * @description
    */
    private void getPartExistECR(Context context, DomainObject part, StringList partList, StringList selList, StringList relList, HashSet hashSet) throws Exception{
        StringList errorList = new StringList();
        StringList partIdList = (StringList)part.getRelatedObjects(context,
                JF_PLMConstants_mxJPO.REL_Instance, //pattern to match relationships
                JF_PLMConstants_mxJPO.TYPE_VPMReference, //pattern to match types
                selList, //the eMatrix StringList object that holds the list of select statement pertaining to Business Obejcts.
                relList, //the eMatrix StringList object that holds the list of select statement pertaining to Relationships.
                false, //get To relationships
                true, //get From relationships
                (short) 1, //the number of levels to expand, 0 equals expand all.
                DomainConstants.EMPTY_STRING, //where clause to apply to objects, can be empty ""
                null, //where clause to apply to relationship, can be empty ""
                (short) 0//limit
        ).stream().map(m -> {
            Map map = (Map) m;
            return UIUtil.getValue(map, SELECT_ID);
        }).collect(Collectors.toCollection(StringList::new));
        DomainObject domainObject = DomainObject.newInstance(context);
        for (int i1 = 0; i1 < partIdList.size(); i1++) {
            domainObject.setId(partIdList.get(i1));
            if (partList.contains(partIdList.get(i1))) {
                String partName = domainObject.getAttributeValue(context, JF_PLMConstants_mxJPO.ATTR_V_PART_NUMBER);
                //返回错误  提示该数据对象已经添加到当前ECR
                hashSet.add(partName);
                continue;
            }
            getPartExistECR(context, domainObject, partList, selList, relList, hashSet);
        }
    }

    /**
    * 移除受影响件   把结构解散    将受影响父件移除
    * @param context
	* @param args
    * @author LIUJR
    * @throws
    * @return java.lang.String
    * @date 08/05/2025 10:14
    * @description
    */
    public String removeSelectPartAffectedItems(Context context, String[] args) throws Exception {
        logger.info("-------------removeSelectPartAffectedItems-----------------------");
        Boolean isPush = Boolean.FALSE;
        try {
            Map paramsMap = JPO.unpackArgs(args);
            //开始断开关系
            ContextUtil.pushContext(context);
            isPush = Boolean.TRUE;
            MapList selectIdMapList = (MapList) paramsMap.get("selectId");
            String strObjectId = (String) paramsMap.get("strObjectId");
            logger.info("strObjectId:{}", strObjectId);
            DomainObject domainObject = DomainObject.newInstance(context);
            domainObject.setId(strObjectId);
            //获取ecr的受影响项
            MapList ecrAffectedItemMapList = domainObject.getRelatedObjects(
                    context,
                    "JFRelateItem",
                    "*",
                    new StringList(DomainConstants.SELECT_ID),
                    new StringList(DomainRelationship.SELECT_ID),
                    false,
                    true,
                    (short) 1,
                    "",
                    "",
                    0
            );
            logger.info("ecrAffectedItemMapList:{}", ecrAffectedItemMapList.toString());
            //开始遍历选择删除的件
            StringList selectRelList = new StringList();
            selectRelList.add(DomainRelationship.SELECT_ID);
            selectRelList.add(JF_PLMConstants_mxJPO.SELECT_ATTRIBUTE_JF_ECRID);
            //获取ECR中其他未选择整椅的结构
            StringList rootList = domainObject.getInfoList(context, "from[" + JF_PLMConstants_mxJPO.REL_JFECRRelateRoot + "].to.id");
            StringList selectIds = (StringList) selectIdMapList.stream().map(m -> {
                Map map = (Map) m;
                String oId = (String) map.get("objectId");
                return oId;
            }).collect(Collectors.toCollection(StringList::new));
            logger.info("rootList:{}", rootList.toString());
            logger.info("selectIds:{}", selectIds.toString());
            rootList.removeAll(selectIds);
            logger.info("rootList:{}", rootList.toString());
            MapList rootMapList = new MapList();
            for (int i = 0; i < rootList.size(); i++) {
                String id = rootList.get(i);
                domainObject.setId(id);
                MapList ecrRoot2ItemMapList = domainObject.getRelatedObjects(
                        context,
                        "JFECRRoot2Item",
                        "*",
                        new StringList(DomainConstants.SELECT_ID),
                        selectRelList,
                        false,
                        true,
                        (short) 0,
                        "",
                        "",
                        0
                );
                rootMapList.addAll(ecrRoot2ItemMapList);
            }
            //根据id进行分组
            Map mapListGroupingMap = JF_NewECRService_mxJPO.getMapListGroupingMap(context, rootMapList, SELECT_ID);
            logger.info("mapListGroupingMap:{}", mapListGroupingMap);
            //开始遍历选择删除的ect结构
            HashSet ecrRemoveItemRIdList = new HashSet();
            HashSet ecrRemoveOIdList = new HashSet();
            if (null != selectIdMapList &&  selectIdMapList.size() > 0) {
                for (int i = 0; i < selectIdMapList.size(); i++) {
                    Map rowMap = (Map) selectIdMapList.get(i);
                    String oId = (String) rowMap.get("objectId");
                    ecrRemoveOIdList.add(oId);
                    //结构关系
                    String relId = (String) rowMap.get("relId");
                    ecrRemoveItemRIdList.add(relId);
                    domainObject.setId(oId);
                    //需要判断下层子级结构是否在同一个ECR的其他的共用件结构中，或者是其他ecr的结构中
                    //获取一级节点下的所有的结构节点 整个结构都要解散  以便于下次添加
                    MapList ecrRoot2ItemMapList = domainObject.getRelatedObjects(
                            context,
                            "JFECRRoot2Item",
                            "*",
                            new StringList(DomainConstants.SELECT_ID),
                            selectRelList,
                            false,
                            true,
                            (short) 0,
                            "",
                            "",
                            0
                    );
                    logger.info("ecrRoot2ItemMapList:{}", ecrRoot2ItemMapList);
                    Iterator iterator = ecrRoot2ItemMapList.iterator();
                    while (iterator.hasNext()) {
                        Map map = (Map) iterator.next();
                        String ecrId = UIUtil.getValue(map, JF_PLMConstants_mxJPO.SELECT_ATTRIBUTE_JF_ECRID);
                        String rId = UIUtil.getValue(map, DomainRelationship.SELECT_ID);
                        String oid = UIUtil.getValue(map, DomainConstants.SELECT_ID);
                        //先判断其他结构是否包含这个件
                        Boolean flag = Boolean.FALSE;
                        if (mapListGroupingMap.containsKey(oid)) {
                            List relIdList = (List) mapListGroupingMap.get(oid);
                            for (int i1 = 0; i1 < relIdList.size(); i1++) {
                                Map relIdMap = (Map)relIdList.get(i1);
                                String ecrId1 = UIUtil.getValue(relIdMap, JF_PLMConstants_mxJPO.SELECT_ATTRIBUTE_JF_ECRID);
                                String rId1 = UIUtil.getValue(relIdMap, DomainRelationship.SELECT_ID);
                                if (rId1.equalsIgnoreCase(rId)) {
                                    flag = Boolean.TRUE;
                                } else if (ecrId1.contains(strObjectId)) {
                                    flag = Boolean.TRUE;
                                }
                            }
                        }
                        if (flag) {
                            continue;
                        }
                        if (ecrId.equalsIgnoreCase(strObjectId)) {
                            //只有一个ecr应该删除
                            ecrRemoveItemRIdList.add(rId);
                            ecrRemoveOIdList.add(oid);
                        } else {
                            //多个ecr应该将ecrid移除
                            DomainRelationship domainRelationship = DomainRelationship.newInstance(context, rId);
                            if (ecrId.contains(strObjectId)) {
                                String[] split = ecrId.split(",");
                                StringList ecrIdList = new StringList();
                                for (int i1 = 0; i1 < split.length; i1++) {
                                    if (split[i1].equalsIgnoreCase(strObjectId)) {
                                        continue;
                                    }
                                    ecrIdList.add(split[i1]);
                                }
                                ecrRemoveOIdList.add(oid);
                                domainRelationship.setAttributeValue(context, JF_PLMConstants_mxJPO.ATTRIBUTE_JF_ECRID, ecrIdList.join(","));
                            }
                        }
                    }
                }
            }
            //断开移除的几点和结构节点
            if (!ecrRemoveItemRIdList.isEmpty()) {
                DomainRelationship.disconnect(context, StringList.create(ecrRemoveItemRIdList).toStringArray());
            }
            logger.info("ecrRemoveOIdList:{}", ecrRemoveOIdList.toString());
            StringList removeItemList = new StringList();
            StringList ecrRemoveOIds = StringList.create(ecrRemoveOIdList);
            for (int i = 0; i < ecrAffectedItemMapList.size(); i++) {
                Map map  = (Map) ecrAffectedItemMapList.get(i);
                if (ecrRemoveOIds.contains(UIUtil.getValue(map, DomainConstants.SELECT_ID))) {
                    removeItemList.add(UIUtil.getValue(map, DomainRelationship.SELECT_ID));
                }
            }
            logger.info("removeItemList:{}", removeItemList.toString());
            if (!removeItemList.isEmpty()) {
                DomainRelationship.disconnect(context, removeItemList.toStringArray());
            }
        } catch (FrameworkException e) {
            e.printStackTrace();
            throw e;
        } finally {
            if (isPush) {
                ContextUtil.popContext(context);
            }
        }
        return "";
    }

    /*
     * @description: PolicyJFNewECRCreatePromoteAction trigger 设置JFECRRelateRoot 关系属性JF_freeState 是否游离状态
     * @author: caipan
     * @date: 2025/5/15 10:19:14
     * @param: * @param[1] context
     * @param[2] args
     * @return: 
     **/
    public void setFreeState(Context context,String[] args) throws Exception{
        String ecrId = args[0];
        DomainObject ecrObj = DomainObject.newInstance(context, ecrId);
        //获取ECR关联项目的ID
       String affectProjectId =  ecrObj.getInfo(context, "from["+JF_PLMConstants_mxJPO.REL_JFChange2Project+"].to.id");
        StringList selList = JF_Util_mxJPO.basicBolistSel();
        logger.info("affectProjectId:{}",affectProjectId);
        selList.add("to[JFProject2RootPart|from.id=="+affectProjectId+"].attribute[JFZeroPart]");
        StringList relList = JF_Util_mxJPO.basicRellistSel();
         MapList list =   ecrObj.getRelatedObjects(context,
                JF_PLMConstants_mxJPO.REL_JFECRRelateRoot, //pattern to match relationships
                JF_PLMConstants_mxJPO.TYPE_VPMReference, //pattern to match types
                selList, //the eMatrix StringList object that holds the list of select statement pertaining to Business Obejcts.
                relList, //the eMatrix StringList object that holds the list of select statement pertaining to Relationships.
                false, //get To relationships
                true, //get From relationships
                (short) 1, //the number of levels to expand, 0 equals expand all.
                DomainConstants.EMPTY_STRING, //where clause to apply to objects, can be empty ""
                null, //where clause to apply to relationship, can be empty ""
                (short) 0); //limit
        Map temp = null;
        for(int i=0;i<list.size();i++){
            temp = (Map) list.get(i);
            String relId = UIUtil.getValue(temp, DomainConstants.SELECT_RELATIONSHIP_ID);
            String JFZeroPart = UIUtil.getValue(temp, "to[JFProject2RootPart].attribute[JFZeroPart]");
            DomainRelationship ship = new DomainRelationship(relId);
            if(!"Y".equalsIgnoreCase(JFZeroPart)) {
                ship.setAttributeValue(context, "JF_freeState", "Y");
            }else{
                ship.setAttributeValue(context, "JF_freeState", "N");
            }
        }

    }
        /*
         * @description:PolicyJFNewECRCreatePromoteCheck promote check
         *  1 如果受影响件的变更来源没有填写，就提示
         *  2 受影响件全部填写了，自动把值填写到ECR上
         *  3 校验ECR填写的内外部开发费用和ECR的变更来源是否一致，不一致提示
         *
         * @author: caipan
         * @date: 2025/5/21 15:45:27
         * @param: * @param[1] context
         * @param[2] args
         * @return: 
         **/
        public int checkNewECRAffectedItemsChangeSource(Context context,String[] args) throws Exception{
            String strObjectId = args[0];
            StringList selList =JF_Util_mxJPO.basicBolistSel();
            selList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_V_PART_NUMBER);
            StringList relList =JF_Util_mxJPO.basicRellistSel();
            relList.add("attribute[JFChangeSource]");
            String relWhere = "attribute[JFChangeSource]==''";
            DomainObject ecrObj = DomainObject.newInstance(context, strObjectId);
          MapList affectList =   ecrObj.getRelatedObjects(context,
                    JF_PLMConstants_mxJPO.REL_JFRelateItem, //pattern to match relationships
                    JF_PLMConstants_mxJPO.TYPE_VPMReference, //pattern to match types
                    selList, //the eMatrix StringList object that holds the list of select statement pertaining to Business Obejcts.
                    relList, //the eMatrix StringList object that holds the list of select statement pertaining to Relationships.
                    false, //get To relationships
                    true, //get From relationships
                    (short) 1, //the number of levels to expand, 0 equals expand all.
                    DomainConstants.EMPTY_STRING, //where clause to apply to objects, can be empty ""
                  DomainConstants.EMPTY_STRING, //where clause to apply to relationship, can be empty ""
                    (short) 0); //limit
            Set<String> partNameList = new HashSet();//子类型的变更来源没有值的 有问题的企业编码
            Set<String> JFChangeSourceList = new HashSet();//子类型的变更来源  记录
            Map map = null;

            for(int i=0;i<affectList.size();i++){
                map = (Map)affectList.get(i);
                String JFChangeSource =UIUtil.getValue(map, "attribute[JFChangeSource]");
                String partName =UIUtil.getValue(map, JF_PLMConstants_mxJPO.SELECT_ATTR_V_PART_NUMBER);
                if(UIUtil.isNullOrEmpty(JFChangeSource)){
                    partNameList.add(partName);
                }else{
                    JFChangeSourceList.add(JFChangeSource);
                }
            }

            if(partNameList.size()>0){
                //提示 变更来源没有填写
                String msg = EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", context.getLocale(), "emxComponents.NewECR.error4");
                msg = partNameList.toString()+":"+msg;
                emxContextUtil_mxJPO.mqlNotice(context, msg);
                return 1;
            } else {
                StringList attr = new StringList();
                attr.add("attribute[JFChangesDeveExpensesManHours]");
                attr.add("attribute[JFChangesDeveExpensesManHoursExternal]");
                attr.add("attribute[JFQQ]");
                attr.add("from[Reference Document|attribute[Project Role]=='QQ'].to.id");
                Map attrMap = ecrObj.getInfo(context, attr);
                String JFChangesDeveExpensesManHours = UIUtil.getValue(attrMap, "attribute[JFChangesDeveExpensesManHours]");//内部
                String JFChangesDeveExpensesManHoursExternal = UIUtil.getValue(attrMap, "attribute[JFChangesDeveExpensesManHoursExternal]");//外部
                logger.info("attrMap:{}",attrMap);
                //计算得到ECR的变更来源应该是什么值
                String message = "";
                String JFQQ=UIUtil.getValue(attrMap, "attribute[JFQQ]");//QQ编号
                String JFQQFile=UIUtil.getValue(attrMap, "from[Reference Document].to.id");//QQ编号
                if (JFChangeSourceList.size() > 0) {
                    String ecrMsg = EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", context.getLocale(), "emxComponents.NewECR.error0");
                    if (JFChangeSourceList.size() > 1) {
                        if (UIUtil.isNullOrEmpty(JFChangesDeveExpensesManHours) || UIUtil.isNullOrEmpty(JFChangesDeveExpensesManHoursExternal)) {
                            String msg = ecrMsg+""+EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", context.getLocale(), "emxComponents.NewECR.error1");
                            msg = msg+","+EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", context.getLocale(), "emxComponents.NewECR.error2");
                            msg = msg+" "+EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", context.getLocale(), "emxComponents.NewECR.error3");
                            emxContextUtil_mxJPO.mqlNotice(context, msg);
                            return 1;
                        }
                        //如果是BOth QQ 和QQ附件必填
                        if(UIUtil.isNullOrEmpty(JFQQ)||UIUtil.isNullOrEmpty(JFQQFile)){
                            String msg = ecrMsg+""+EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", context.getLocale(), "emxComponents.NewECR.error5");
                            msg = msg+" "+EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", context.getLocale(), "emxComponents.NewECR.error3");
                            emxContextUtil_mxJPO.mqlNotice(context, msg);
                            return 1;
                        }
                        ecrObj.setAttributeValue(context,"JFChangeSource","Both");
                    } else {
                        String subChangeSource = JFChangeSourceList.iterator().next();
                        if(subChangeSource.equalsIgnoreCase("Internal Changes")&&UIUtil.isNullOrEmpty(JFChangesDeveExpensesManHours)){
                            String msg = ecrMsg+""+EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", context.getLocale(), "emxComponents.NewECR.error1");
                            msg = msg+" "+EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", context.getLocale(), "emxComponents.NewECR.error3");
                            emxContextUtil_mxJPO.mqlNotice(context, msg);
                            return 1;
                        }else  if(subChangeSource.equalsIgnoreCase("External Changes")&&UIUtil.isNullOrEmpty(JFChangesDeveExpensesManHoursExternal)){
                            String msg = ecrMsg+""+EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", context.getLocale(), "emxComponents.NewECR.error2");
                            msg = msg+" "+EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", context.getLocale(), "emxComponents.NewECR.error3");
                            emxContextUtil_mxJPO.mqlNotice(context, msg);
                            return 1;
                        }else if(subChangeSource.equalsIgnoreCase("Both")&&(UIUtil.isNullOrEmpty(JFChangesDeveExpensesManHoursExternal)||UIUtil.isNullOrEmpty(JFChangesDeveExpensesManHoursExternal))){
                            String msg = ecrMsg+""+EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", context.getLocale(), "emxComponents.NewECR.error1");
                            msg = msg+","+EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", context.getLocale(), "emxComponents.NewECR.error2");
                            msg = msg+" "+EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", context.getLocale(), "emxComponents.NewECR.error3");
                            emxContextUtil_mxJPO.mqlNotice(context, msg);
                            return 1;
                        }
                        //如果是BOth QQ 和QQ附件必填
                        if(!subChangeSource.equals("Internal Changes")&&(UIUtil.isNullOrEmpty(JFQQ)||UIUtil.isNullOrEmpty(JFQQFile))){
                            String msg = ecrMsg+""+EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", context.getLocale(), "emxComponents.NewECR.error5");
                            msg = msg+" "+EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", context.getLocale(), "emxComponents.NewECR.error3");
                            emxContextUtil_mxJPO.mqlNotice(context, msg);
                            return 1;
                        }
                        ecrObj.setAttributeValue(context,"JFChangeSource",subChangeSource);
                    }
                    return 0;
                }
            }
                return 0;
        }

        /*
         * @description:设置允许审批标识和清空上次拒绝的评论
         * @author: caipan
         * @date: 2026/1/21 14:06:51
         * @param: * @param[1] context
         * @param[2] args
         * @return:
         **/
        public void setAllowableReview(Context context,String[] args) throws Exception{
           //获取到正式ECR
            DomainObject formatECR = DomainObject.newInstance(context,args[0]);
            String type = formatECR.getInfo(context, SELECT_TYPE);
            if("JFFormalECR".equalsIgnoreCase(type)){
                Map map = new HashMap();
                map.put("JFAllowableReview","Y");
                map.put("JFReviewMessage","");
                formatECR.setAttributeValues(context,map);
            }
        }
        /*
         * @description:设置评估状态，APR审核完成之后
         * @author: caipan
         * @date: 2026/1/22 09:50:21
         * @param: * @param[1] context
         * @param[2] args
         * @return: 
         **/
    public void SetJF_Evaluate(Context context,String[] args) throws Exception{
        //获取到正式ECR
        DomainObject formatECR = DomainObject.newInstance(context,args[0]);
        String type = formatECR.getInfo(context, SELECT_TYPE);
        StringList selList = JF_Util_mxJPO.basicBolistSel();
        StringList relList = JF_Util_mxJPO.basicRellistSel();
        ContextUtil.pushContext(context);
        try {
            if ("JFFormalECR".equalsIgnoreCase(type)) {
                String rel = JF_PLMConstants_mxJPO.REL_JFRELATEITEM;
                //JF_VPMReferenceCost.JF_Evaluate
                //获取到所有的变更记录
                MapList list = formatECR.getRelatedObjects(context,
                        rel, //pattern to match relationships
                        JF_PLMConstants_mxJPO.TYPE_VPMReference, //pattern to match types
                        selList, //the eMatrix StringList object that holds the list of select statement pertaining to Business Obejcts.
                        relList, //the eMatrix StringList object that holds the list of select statement pertaining to Relationships.
                        false, //get To relationships
                        true, //get From relationships
                        (short) 1, //the number of levels to expand, 0 equals expand all.
                        EMPTY_STRING, //where clause to apply to objects, can be empty ""
                        null, //where clause to apply to relationship, can be empty ""
                        (short) 0); //limit
                DomainObject partObj = DomainObject.newInstance(context);
                for (int i = 0; i < list.size(); i++) {
                    Map map = (Map) list.get(i);
                    String id = UIUtil.getValue(map, SELECT_ID);
                    partObj.setId(id);
                    partObj.setAttributeValue(context, JF_PLMConstants_mxJPO.ATTR_JF_VPMReferenceCost_JF_Evaluate,"Y");
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            ContextUtil.popContext(context);
        }
    }

    /**
    * 5.149 ECR提交审批时校验AA版本或者其他版本必须挂到供货件
     *      供货件的判定：当前ECR关联项目的供货件
     *   1. AA版只能挂在供货件BOM结构中和供货件整椅一起发布，或者AA版本身就是一个供货件；
     *     1. 如果本身就挂载在供货件进行发布---直接跳过
     *     2. 如果本身挂载在非供货件进行发布---报错
     *     3. 如果本身挂载在非供货件和供货件在同一个ECR一起发布---不报错
     *     4. 如果本身挂载在非供货件，和挂载到不在当前ECR下面的供货件---报错和第二点一致
     *     5. AA版本身就是一个供货件--跳过
     *   2. 非AA版发布时，其上一发布版本若挂在最新发布的供货件下则允许单独发布，否则不允许单独发布，也必须挂整椅下一起发布
     *     1. 查找到所有的冻结数据的上一个发布版本，递归找到所有的供货件，如果存在一个供货件符合情况就跳过，都不符合要求才报错
     *     2. 如果当前数据本身就是供货件，就直接跳过
    * @param context
	* @param args
    * @author LIUJR
    * @throws
    * @return int
    * @date 2026/6/3 14:08
    * @description
    */
    public int checkRevisionFrozenAndZeroPartFlag(Context context, String[] args) {
        logger.info("-------------checkRevisionFrozenAndZeroPartFlag----------------start");
        int iReturn = 0;
        try {
            String strEcrId = args[0];
            DomainObject object = DomainObject.newInstance(context);
            object.setId(strEcrId);
            //获取ECR的root节点
            MapList rootList = object.getRelatedObjects(
                    context,
                    JF_PLMConstants_mxJPO.REL_JFECRRelateRoot,
                    JF_PLMConstants_mxJPO.TYPE_VPMReference,
                    JF_Util_mxJPO.basicBolistSel(),
                    JF_Util_mxJPO.basicRellistSel(),
                    false,
                    true,
                    (short) 1,
                    "",
                    "",
                    0
            );
            logger.info("获取ECR的root节点:{}",rootList);
            //获取ECR所属项目
            String strEcrProjectId = object.getInfo(context, "from[" + JF_PLMConstants_mxJPO.REL_JFChange2Project + "].to.id");
            logger.info("获取ECR所属项目:{}",strEcrProjectId);
            //获取ECR所属项目的供货件
            StringList projectObjectZeroPart = JF_PublicMethodClass_mxJPO.getProjectObjectZeroPart(context, DomainObject.newInstance(context, strEcrProjectId));
            logger.info("获取ECR所属项目的供货件:{}",projectObjectZeroPart);
            //将变更结构root节点中的AA版本过滤出来
            String strRevision = EMPTY_STRING;
            Map map = new HashMap();
            MapList mapIsAAList = new MapList();
            HashSet<String> rootNotAAIsZeroPartSet = new HashSet<>();
            DomainObject partObject = DomainObject.newInstance(context);
            for (int i = 0; i < rootList.size(); i++) {
                map = (Map) rootList.get(i);
                strRevision = UIUtil.getValue(map, SELECT_REVISION);
                String id = UIUtil.getValue(map, SELECT_ID);
                if (strRevision.startsWith("AA")) {
                    mapIsAAList.add(map);
                    //update by ljr 20260722  AA版本下也有可能存在非AA版本的数据
                    partObject.setId(id);
                    rootNotAAIsZeroPartSet.addAll((HashSet)partObject.getRelatedObjects(
                            context,
                            JF_PLMConstants_mxJPO.REL_JFECRRoot2Item,
                            JF_PLMConstants_mxJPO.TYPE_VPMReference,
                            JF_Util_mxJPO.basicBolistSel(),
                            JF_Util_mxJPO.basicRellistSel(),
                            false,
                            true,
                            (short) 0,
                            "",
                            "",
                            0
                    ).stream().filter(m -> {
                        Map map1 = (Map) m;
                        if (UIUtil.getValue(map1, SELECT_REVISION).startsWith("AA")) {
                            return Boolean.FALSE;
                        }else  {
                            return Boolean.TRUE;
                        }
                    }).map(m -> {
                        Map map1 = (Map) m;
                        return UIUtil.getValue(map1, SELECT_ID);
                    }).collect(Collectors.toCollection(HashSet::new)));
                } else if (projectObjectZeroPart.contains(id)) {
                    rootNotAAIsZeroPartSet.add(id);
                    partObject.setId(id);
                    rootNotAAIsZeroPartSet.addAll((HashSet)partObject.getRelatedObjects(
                            context,
                            JF_PLMConstants_mxJPO.REL_JFECRRoot2Item,
                            JF_PLMConstants_mxJPO.TYPE_VPMReference,
                            JF_Util_mxJPO.basicBolistSel(),
                            JF_Util_mxJPO.basicRellistSel(),
                            false,
                            true,
                            (short) 0,
                            "",
                            "",
                            0
                    ).stream().map(m -> {
                        Map map1 = (Map) m;
                        return UIUtil.getValue(map1, SELECT_ID);
                    }).collect(Collectors.toCollection(HashSet::new)));
                }
            }
            StringList  rootNotAAIsZeroPartList = StringList.create(rootNotAAIsZeroPartSet);
            logger.info("将变更结构root节点中的AA版本过滤出来:{}",mapIsAAList);
            logger.info("rootNotAAIsZeroPartList:{}",rootNotAAIsZeroPartList);
            //获取ECR的item节点  冻结状态的零件
            MapList itemList = object.getRelatedObjects(
                    context,
                    JF_PLMConstants_mxJPO.REL_JFRelateItem,
                    JF_PLMConstants_mxJPO.TYPE_VPMReference,
                    JF_Util_mxJPO.basicBolistSel(),
                    JF_Util_mxJPO.basicRellistSel(),
                    false,
                    true,
                    (short) 1,
                    "current==FROZEN",
                    "",
                    0
            );
            logger.info("获取ECR的item节点 冻结状态的零件:{}",itemList);
            //将item关系中的非AA版本过滤出来
            MapList mapNotAAList = new MapList();
            for (int i = 0; i < itemList.size(); i++) {
                map = (Map) itemList.get(i);
                strRevision = UIUtil.getValue(map, SELECT_REVISION);
                if (!strRevision.startsWith("AA")) {
                    mapNotAAList.add(map);
                }
            }
            logger.info("将item关系中的非AA版本过滤出来 :{}",mapNotAAList);
            //构造一个where条件，JF_ECRId属性必须包含当前ECR的id 才是本ECR中的变更结构
            StringBuilder relWhereSb = new StringBuilder();
            relWhereSb.append(strEcrId);
            relWhereSb.append("  matchlist  '");
            relWhereSb.append(JF_PLMConstants_mxJPO.SELECT_ATTRIBUTE_JF_ECRID);
            relWhereSb.append("'");
            relWhereSb.append(" ','");
            //以上数据构造完成
            //开始进行校验
            StringList errorIsAAList = new StringList();
            StringList errorNotAAList = new StringList();
            //AA版本的校验
            if (!mapIsAAList.isEmpty()) {
                allOkPartIdList = new HashSet<>();
                Map paramsMap = new HashMap();
                paramsMap.put("zeroPart", projectObjectZeroPart);   //项目中所有的供货件
                paramsMap.put("IsAAList", mapIsAAList);   //变更结构的root节点
                paramsMap.put("relWhere", relWhereSb.toString());   //变更结果获取的where条件
                //AA版本 开始校验
                errorIsAAList = checkPartFirstRevision(context, JPO.packArgs(paramsMap));
            }
            //非AA版本的校验
            if (!mapNotAAList.isEmpty()) {
                Map paramsMap = new HashMap();
                paramsMap.put("zeroPart", projectObjectZeroPart);    //项目中所有的供货件
                paramsMap.put("NotAAList", mapNotAAList);   //ECR中冻结的受影响数据
                paramsMap.put("rootNotAAIsZeroPartList", rootNotAAIsZeroPartList);   //ECR中搭建在供货件下的非AA版本的冻结的受影响数据
                //非AA版本  开始校验
                errorNotAAList = checkPartNotFirstRevision(context, JPO.packArgs(paramsMap));
            }
            StringList basicBolistSel = JF_Util_mxJPO.basicBolistSel();
            basicBolistSel.add(JF_PLMConstants_mxJPO.SELECT_ATTR_V_PART_NUMBER);
            //校验完成 整理校验异常信息
            StringBuilder stringBuilder = new StringBuilder();
            if (!errorIsAAList.isEmpty()) {
                StringList errorList = new StringList();
                MapList mapList = DomainObject.getInfo(context, errorIsAAList.toStringArray(), basicBolistSel);
                mapList.stream().forEach(m -> {
                    Map map1 = (Map) m;
                    errorList.add(UIUtil.getValue(map1, JF_PLMConstants_mxJPO.SELECT_ATTR_V_PART_NUMBER) + "_" + UIUtil.getValue(map1, SELECT_REVISION));
                });
                stringBuilder.append(ComponentsUtil.i18nStringNow("emxComponents.PromoteCheck.PartAACheckMess", context.getLocale().getLanguage()));
                stringBuilder.append(errorList.join(","));
            }
            if (!errorNotAAList.isEmpty()) {
                StringList errorList = new StringList();
                MapList mapList = DomainObject.getInfo(context, errorNotAAList.toStringArray(), basicBolistSel);
                mapList.stream().forEach(m -> {
                    Map map1 = (Map) m;
                    errorList.add(UIUtil.getValue(map1, JF_PLMConstants_mxJPO.SELECT_ATTR_V_PART_NUMBER) + "_" + UIUtil.getValue(map1, SELECT_REVISION));
                });
                stringBuilder.append(ComponentsUtil.i18nStringNow("emxComponents.PromoteCheck.PartNoAACheckMess", context.getLocale().getLanguage()));
                stringBuilder.append(errorList.join(","));
            }
            if (stringBuilder.length() > 0 ) {
                emxContextUtil_mxJPO.mqlNotice(context, stringBuilder.toString());
                return 1;
            }
        }catch (Exception e) {
            e.printStackTrace();
            iReturn = 1;
        }
        return iReturn;
    }

    /**
    * 1. AA版只能挂在供货件BOM结构中和供货件整椅一起发布，或者AA版本身就是一个供货件；
    *     1. 如果本身就挂载在供货件进行发布---直接跳过
    *     2. 如果本身挂载在非供货件进行发布---报错
    *     3. 如果本身挂载在非供货件和供货件在同一个ECR一起发布---不报错
    *     4. 如果本身挂载在非供货件，和挂载到不在当前ECR下面的供货件---报错和第二点一致
    *     5. AA版本身就是一个供货件--跳过
    * @param context
	* @param args
    * @author LIUJR
    * @throws
    * @return matrix.util.StringList
    * @date 2026/6/3 15:28
    * @description
    */
    public static StringList checkPartFirstRevision(Context context, String args[]) throws Exception{
        logger.info("-------------checkPartFirstRevision----------------start");
        StringList returnList = new StringList();
        try {
            Map paramsMap = (Map) JPO.unpackArgs(args);
            StringList projectObjectZeroPart = (StringList) paramsMap.get("zeroPart");
            MapList mapIsAAList = (MapList) paramsMap.get("IsAAList");
            logger.info("projectObjectZeroPart :{}",projectObjectZeroPart);
            logger.info("mapIsAAList :{}",mapIsAAList);
            HashSet<String> errorSet = new HashSet<>();
            String relWhere = (String) paramsMap.get("relWhere");
            Map map = new HashMap();
            String partId = EMPTY_STRING;
            DomainObject object = DomainObject.newInstance(context);
            for (int i = 0; i < mapIsAAList.size(); i++) {
                map = (Map) mapIsAAList.get(i);
                partId = UIUtil.getValue(map, SELECT_ID);
                logger.info("mapIsAAList  -----partId  :{}",partId);
                object.setId(partId);
                //如果本身是供货件
                logger.info("projectObjectZeroPart.contains(partId):{}",projectObjectZeroPart.contains(partId));
                if (projectObjectZeroPart.contains(partId)) {
                    //需要将整个变更结构拿出来  如果本身挂载在非供货件和供货件在同一个ECR一起发布---不报错
                    if (!errorSet.isEmpty()) {
                        MapList rootList = object.getRelatedObjects(
                                context,
                                JF_PLMConstants_mxJPO.REL_JFECRRoot2Item,
                                JF_PLMConstants_mxJPO.TYPE_VPMReference,
                                JF_Util_mxJPO.basicBolistSel(),
                                JF_Util_mxJPO.basicBolistSel(),
                                false,
                                true,
                                (short) 0,
                                "current==FROZEN",
                                relWhere,
                                0
                        );
                        logger.info("contains rootList :{}",rootList);
                        for (int i1 = 0; i1 < rootList.size(); i1++) {
                            map = (Map) rootList.get(i1);
                            String id = UIUtil.getValue(map, EMPTY_STRING);
                            allOkPartIdList.add(id);
                            if (errorSet.contains(id)) {
                                errorSet.remove(id);
                            }
                        }
                        logger.info("contains errorSet :{}",errorSet);
                    }
                    continue;
                }
                logger.info("allOkPartIdList:{}",allOkPartIdList);
                //如果这个件已经包含在了本ECR的供货件清单下发布  就不报错
                if (allOkPartIdList.contains(partId)) {
                    continue;
                }
                //如果本身挂载在非供货件进行发布
                errorSet.add(partId);
                //获取root节点的下一级变更结构
                MapList rootList = object.getRelatedObjects(
                        context,
                        JF_PLMConstants_mxJPO.REL_JFECRRoot2Item,
                        JF_PLMConstants_mxJPO.TYPE_VPMReference,
                        JF_Util_mxJPO.basicBolistSel(),
                        JF_Util_mxJPO.basicRellistSel(),
                        false,
                        true,
                        (short) 1,
                        "current==FROZEN",
                        relWhere,
                        0
                );
                logger.info("获取root节点的下一级变更结构:{}",rootList);
                //获取AA版本
                MapList mapIsAASubList = new MapList();
                for (int i1 = 0; i1 < rootList.size(); i1++) {
                    map = (Map) rootList.get(i1);
                    if (UIUtil.getValue(map, SELECT_REVISION).startsWith("AA")) {
                        mapIsAASubList.add(map);
                    }
                }
                logger.info("mapIsAASubList:{}",mapIsAASubList);
                logger.info("递归调用！！！！！！！！！！！！！！！！");
                //递归调用
                if (!mapIsAASubList.isEmpty()) {
                    Map paramsSubMap = new HashMap();
                    paramsSubMap.put("zeroPart", projectObjectZeroPart);
                    paramsSubMap.put("IsAAList", mapIsAASubList);
                    paramsSubMap.put("relWhere", relWhere);
                    //AA版本
                    StringList stringList = checkPartFirstRevision(context, JPO.packArgs(paramsSubMap));
                    errorSet.addAll(stringList);
                }
                logger.info("errorSet：{}", errorSet);
            }
            returnList = StringList.create(errorSet);
        }catch (Exception e) {
            e.printStackTrace();
            logger.info(e.getMessage().toString());
            throw e;
        }
        logger.info("-------------checkPartFirstRevision----------------end");
        return returnList;
    }

    /**
     *   2. 非AA版发布时，其上一发布版本若挂在最新发布的供货件下则允许单独发布，否则不允许单独发布，也必须挂整椅下一起发布
     *     1. 查找到所有的冻结数据的上一个发布版本，递归找到所有的供货件，如果存在一个供货件符合情况就跳过，都不符合要求才报错
     *     2. 如果当前数据本身就是供货件，就直接跳过
     * @param context
     * @param args
     * @author LIUJR
     * @throws
     * @return matrix.util.StringList
     * @date 2026/6/3 15:28
     * @description
     */
    public static StringList checkPartNotFirstRevision(Context context, String args[]) throws Exception{
        logger.info("-------------checkPartNotFirstRevision----------------start");
        HashSet<String> returnSet = new HashSet<>();
        try {
            Map paramsMap = (Map) JPO.unpackArgs(args);
            StringList projectObjectZeroPart = (StringList) paramsMap.get("zeroPart");
            StringList rootNotAAIsZeroPartList = (StringList) paramsMap.get("rootNotAAIsZeroPartList");
            MapList mapNotAAList = (MapList) paramsMap.get("NotAAList");
            logger.info("mapNotAAList:{}", mapNotAAList);
            Map map = new HashMap();
            String partId = EMPTY_STRING;
            DomainObject object = DomainObject.newInstance(context);
            DomainObject rootObject = DomainObject.newInstance(context);
            JF_Util_mxJPO utilMxJPO = new JF_Util_mxJPO();
            StringList wholeChairList = new StringList();
            String strRevision = EMPTY_STRING;
            for (int i = 0; i < mapNotAAList.size(); i++) {
                map = (Map) mapNotAAList.get(i);
                partId = UIUtil.getValue(map, SELECT_ID);
                object.setId(partId);
                logger.info("partId:{}", partId);
                logger.info("projectObjectZeroPart.contains(partId):{}", projectObjectZeroPart.contains(partId));
                if (projectObjectZeroPart.contains(partId)) {
                    continue;
                }
                if (rootNotAAIsZeroPartList.contains(partId)) {
                    continue;
                }
                //查找到所有的冻结数据的上一个发布版本含冒泡，递归找到所有的供货件，如果存在一个供货件符合情况就跳过，都不符合要求才报错
                //最新发布的供货件含冒泡版本下则允许单独发布
                String preReleased = utilMxJPO.getPreviousReleasedMajorId(context, partId);//上一个发布版本
                logger.info("" +
                        "" +
                        ":{}", preReleased);
                Boolean flag = Boolean.FALSE;
                if (UIUtil.isNotNullAndNotEmpty(preReleased)) {
                    wholeChairList = utilMxJPO.getWholeChair(context, new String[]{preReleased});//上一个发布版本关联的整椅
                    logger.info("wholeChairList:{}", wholeChairList);
                    for (int i1 = 0; i1 < wholeChairList.size(); i1++) {
                        String id = wholeChairList.get(i1);
                        logger.info("id:{}", id);
                        rootObject.setId(id);
                        strRevision = rootObject.getInfo(context, SELECT_REVISION);
                        logger.info("strRevision:{}", strRevision);
                        String lastReleasedMajorid = JF_Util_mxJPO.getLastReleasedMajorid(context, id);
                        logger.info("JF_Util_mxJPO.getLastReleasedMajoridExcludeBubblingRev(context, id):{}", lastReleasedMajorid);
                        if (UIUtil.isNotNullAndNotEmpty(lastReleasedMajorid) && lastReleasedMajorid.equalsIgnoreCase(id) && projectObjectZeroPart.contains(id)) {
                            flag = Boolean.TRUE;
                            break;
                        }
                    }
                }
                logger.info("flag:{}", flag);
                if (!flag) {
                    returnSet.add(partId);
                }
            }
        }catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
        logger.info("-------------checkPartNotFirstRevision----------------end");
        return StringList.create(returnSet);
    }
}
