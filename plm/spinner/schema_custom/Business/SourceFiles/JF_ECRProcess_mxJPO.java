import com.dassault_systemes.enovia.history.jaxb.Current;
import com.matrixone.apps.common.RouteWorkflow;
import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.DomainRelationship;
import com.matrixone.apps.domain.util.*;
import com.matrixone.apps.framework.ui.UIUtil;
import matrix.db.Context;
import matrix.db.JPO;
import matrix.db.RelationshipType;
import matrix.util.StringList;
import org.apache.jena.sparql.function.library.date;
import org.apache.jena.sparql.pfunction.library.str;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class JF_ECRProcess_mxJPO {
    private static final Logger logger = LoggerFactory.getLogger(JF_ECRProcess_mxJPO.class);
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
        StringList partList = (StringList)requestmap.get("partList");//添加的受影响ID
        DomainObject ecrObj = DomainObject.newInstance(context);
        ecrObj.setId(ecrId);
        StringList lists = ecrObj.getInfoList(context, "from[JFRelateItem].to.id");//ECR受影响对象
        DomainObject partObj = DomainObject.newInstance(context);
        StringList busSelects = new StringList();
        busSelects.add(DomainConstants.SELECT_ID);
        busSelects.add(DomainConstants.SELECT_CURRENT);

        StringList relSelects = new StringList();
        relSelects.add(DomainConstants.SELECT_RELATIONSHIP_ID);

        Map map = null;
        Map parentMap = null;
        String partId ="";
        String parentId ="";
        String connectId ="";
        StringBuffer mqlWhere = new StringBuffer();
        mqlWhere.append("current==RELEASED");
        for(int i=0;i<partList.size();i++) {
            //获取当前零件的最新发布版本---当前按理是冻结版本
            partId = partList.get(i);
            logger.info("受影响对象冻结版本partId:{}", partId);
            String lastReleasedId = JF_Util_mxJPO.getLastReleasedMajorid(context, partId);
            logger.info("受影响对象最新发布版本:{}", lastReleasedId);
            if (UIUtil.isNullOrEmpty(lastReleasedId)) {
                continue;
            }
            partObj.setId(lastReleasedId);
            //获取当前对象的最新发布版本的父，如果不是的话就跳过
            MapList parentList = partObj.getRelatedObjects(context, JF_PLMConstants_mxJPO.REL_Instance, JF_PLMConstants_mxJPO.TYPE_VPMReference, busSelects, relSelects, true, false, (short) 1, mqlWhere.toString(), "", 0);
            for (int j = 0; j < parentList.size(); j++) {
                parentMap = (Map) parentList.get(j);
                parentId = UIUtil.getValue(parentMap, DomainConstants.SELECT_ID);
                //如果找出来的父在当前ECR的受影响对象中就跳过
                if (lists.contains(parentId)) {//获取这个最新发布版本的，所有ID，如果存在这个里面按理都应该跳过--包括已经关联的或者还未关联的
                    continue;
                }
                if (JF_Util_mxJPO.getLastReleasedMajorid(context, parentId).equalsIgnoreCase(parentId)) {//是最新发布版本
                    connectId = UIUtil.getValue(parentMap, DomainConstants.SELECT_RELATIONSHIP_ID);//拿到关系ID
                    createAffectParent(context, ecrId, connectId, parentId, partId);//建立ECR和受影响父件的关系
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
        Map attributeMap = new HashMap();
        attributeMap.put(JF_PLMConstants_mxJPO.ATTR_JFSubPartId,partId);
        attributeMap.put(JF_PLMConstants_mxJPO.ATTR_JFSubConnectId,connectId);
        affParent.setAttributeValues(context,attributeMap);
        logger.info("createAffectParent end");
    }

    public void testAffectAdd(Context context,String[] args) throws Exception{
        HashMap map =new HashMap();
        map.put("ecrId",args[0]);
        StringList list = new StringList();
        list.add("21798.25570.44534.63205");
        map.put("partList",list);
        processAffectedItemAdd(context, JPO.packArgs(map));
    }
    public void testAffectRemove(Context context,String[] args) throws Exception{
        HashMap map =new HashMap();
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
            DomainObject partObj = new DomainObject();
//            String queryDrw= "to[XCADBaseDependency|from.type==Drawing&&from.current=='FROZEN'].from.id";//查询冻结状态的V5图纸
            for (int i = 0; i < itemList.size(); i++) {
                map = (Map) itemList.get(i);
                partId = UIUtil.getValue(map, DomainConstants.SELECT_ID);
                String JJFIsFollow = UIUtil.getValue(map, JF_PLMConstants_mxJPO.SELECT_ATTR_JJFIsFollow);
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
                    }
                }
            }
            JF_Util_mxJPO util = new JF_Util_mxJPO();
            if(vpmIds.size()>0) {
                util.promoteVPM(context, vpmIds, "ToRelease");
            }
            logger.info("promoteAffectItemToRelease end");
        }catch (Exception ex){
            throw new Exception("promoteAffectItemToRelease error:"+ex.getMessage());
        }finally {
            ContextUtil.popContext(context);
        }
    }

    /**
     * 发布关联的文档
     **
     * @param context 上下文
     * @param args 审核对象ID
     * @return void
     * @throws Exception 发布文档失败时抛出异常
     * @author caipan by codex
     * @date 2026/8/19
     */
    public void promoteConnDoc(Context context,String[] args) throws Exception {
        String objectId = args[0];//对应的对象ID ，比如 ECR、DA、DRW、DR
        DomainObject obj = DomainObject.newInstance(context);
        obj.setId(objectId);
        //20260819 update by codex JFDR未完成时禁止发布关联文档
        String type = obj.getInfo(context, DomainConstants.SELECT_TYPE);
        String objectCurrent = obj.getInfo(context, DomainConstants.SELECT_CURRENT);
        if (JF_PLMConstants_mxJPO.TYPE_JFDR.equals(type) && !"Complete".equals(objectCurrent)) {
            logger.warn("skip promoteConnDoc because JFDR is not Complete, objectId:{}, current:{}", objectId, objectCurrent);
            return;
        }
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
                jfUtilMxJPO.promoteVPM(context, FROZENList, "ToRelease");
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
    public Map showTasksGraphical(Context context,String [] args) throws Exception{
//var10000 = XSSUtil.encodeForHTML(var1, var6); 转码
        Map resultMap = new HashMap();
        String ecrId = args[0];
        DomainObject ecrObj = DomainObject.newInstance(context,ecrId);
        String type = ecrObj.getInfo(context, DomainConstants.SELECT_TYPE);
        if(type.equals("JFNewECR")||type.equals("JFFormalECR")){
            JF_NewECRProcess_mxJPO newEcr = new JF_NewECRProcess_mxJPO();
            return newEcr.showTasksGraphical(context, args);
        }
        String current = ecrObj.getInfo(context, DomainConstants.SELECT_CURRENT);
        String projectId = ecrObj.getInfo(context, "from[JFChange2Project].to.id");
        //获取 Submit Review 这两个状态的流程
        StringList ids = new StringList();
        if(current.equals("Submit")){
            String tempId = getCurrentRoute(context, ecrId,null);
            if(UIUtil.isNotNullAndNotEmpty(tempId)){
                ids.add(tempId);
            }
        }
        if(current.equals("Review")||current.equals("Countersign")||current.equals("APR")||current.equals("Quotation")||current.equals("Complete")){
            String tempId = getCurrentRoute(context, ecrId,"Submit");
            if(UIUtil.isNotNullAndNotEmpty(tempId)){
                ids.add(tempId);
            }
             tempId = getCurrentRoute(context, ecrId,"Review");
            if(UIUtil.isNotNullAndNotEmpty(tempId)){
                ids.add(tempId);
            }
        }
//        StringList ids =ecrObj.getInfoList(context, "from[Object Route].to.id");
        StringList taskListId =ecrObj.getInfoList(context, "from[JFECR2Task].to.id");
        StringBuffer sbTasks = new StringBuffer();
        StringBuffer sbConnections = new StringBuffer();
logger.info("current:{}",current);
        int nodeSize = 0;
        int resultnodeSize = 0;
        if(ids.size()>0) {//有流程
            String language = "-8";
            String routeId = "";
            if (current.equals("Submit")) {//提交状态
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
                for (int r = 0; r < ids.size(); r++) {
                    StringBuffer sbtask = new StringBuffer();
                    routeId = ids.get(r);
                    RouteWorkflow rwObject = new RouteWorkflow(routeId);
                    if (r == 0) {
                        nodeSize = getRouteNode(context, routeId);
                        resultnodeSize = nodeSize;
                    }
                    if (r == 1) {
                        resultnodeSize = resultnodeSize + getRouteNode(context, routeId);
                    }

                    Map<String, StringBuffer> map = rwObject.getRouteTaskNodes(context, false, language);
                    analyString(map.get("sbTasks").toString(), r, current, sbtask, nodeSize, resultnodeSize,taskListId.size()).toString();
                    sbTasks.append(sbtask);
//                sbTasks.append(map.get("sbTasks"));
//                    sbConnections.append(map.get("sbConnections"));
                }
                for(int node=0;node<resultnodeSize;node++){
                    if(node==0){
                        connectTask(sbConnections,"start","task"+node);
                    }else{
                        int taskNode = node-1;
                        connectTask(sbConnections,"task"+taskNode,"task"+node);
                    }
                }
                if(current.equals("Review")||taskListId.size()==0){
                    int taskNode = resultnodeSize-1;
                    connectTask(sbConnections,"task"+taskNode,"end");
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
            }  if(count==1) {
                String taskNum ="task"+size;
                String task1Num ="task"+String.valueOf(size+1);
                //390
                int left =  390+220*(i-3);
                if(size>1){
                     left = 590+220*(i-3);
                }
                countleft = left;
                String task0Replace ="id=\""+taskNum+"\" style=\"top:0px; left:"+String.valueOf(left)+"px;";
                String task1Replace ="id=\""+task1Num+"\" style=\"top:0px; left:"+String.valueOf(left)+"px;";
                if(array[i].contains(task0)) {
                    resultStr.append("<div");
                    resultStr.append(array[i].replace(task0, task0Replace));
                }else  if(array[i].contains(task1)) {
                    resultStr.append("<div");
                    resultStr.append(array[i].replace(task1, task1Replace));
                }
               else if(array[i].contains("id=\"start\"")){
                    continue;
                }
               else if(array[i].contains("id=\"end\"")&&!"Review".equals(state)&& !(taskSize ==0)){
                    continue;
                }else  if(array[i].contains(end)) {
                    countleft = nodeSize*200+200;
                    task0Replace ="id='end' style=\"top:15px; left:"+String.valueOf(countleft)+"px;";
                    resultStr.append("<div");
                    resultStr.append(array[i].replace(end, task0Replace));
                }
                else  if(array[i].contains(end1)) {
                    countleft = nodeSize*200+200;
                    task0Replace ="id='end' style=\"top:15px; left:"+String.valueOf(countleft)+"px;";
                    resultStr.append("<div");
                    resultStr.append(array[i].replace(end1, task0Replace));
                }
                else {
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
            Map paramsMap = (Map) JPO.unpackArgs(args);
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
                type = "JFECR,JFNewECR,JFFormalECR";
                selList.add("from[JFECR2CO].to.id");
                MapList ecrList = objectProject.getRelatedObjects(context, relName, type, selList, relList, true, false, (short) 1, "", "", 0);
                Map temp = null;
                String ecoId = "";
                for(int i=0;i<ecrList.size();i++){
                    Map map = new HashMap();
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

    /**
     * 获取当前对象关联的最新流程
     **
     * @param context 3DE上下文
     * @param args 请求参数，args[0]为当前对象ID
     * @return String 按创建时间排序后的最新Route对象ID，无关联流程时返回空字符串
     * @throws Exception 查询流程失败时抛出异常
     * @author caipan
     * @date 2026/7/20 16:00
     */
    public String getLatestRoute(Context context, String[] args) throws Exception {
        String rootId = args[0];
        DomainObject rootObject = DomainObject.newInstance(context, rootId);
        String type = rootObject.getInfo(context, DomainConstants.SELECT_TYPE);
        //20260812 update by ljr 文档由批量审批单发布时，生命周期图形任务展示批量审批单关联的Route。
        if (type.equalsIgnoreCase(DomainConstants.TYPE_DOCUMENT)) {
            StringList batchReviewIds = rootObject.getInfoList(
                    context,
                    "to[" + DomainConstants.RELATIONSHIP_REFERENCE_DOCUMENT + "].from["
                            + JF_PLMConstants_mxJPO.TYPE_JFBatchDocumentReview + "].id");
            MapList allBatchReviewRouteList = new MapList();
            for (String batchReviewId : batchReviewIds) {
                DomainObject batchReviewObject = DomainObject.newInstance(context, batchReviewId);
                StringList routeSelectList = StringList.create(
                        DomainConstants.SELECT_ID,
                        DomainConstants.SELECT_ORIGINATED);
                MapList batchReviewRouteList = batchReviewObject.getRelatedObjects(
                        context,
                        DomainConstants.RELATIONSHIP_OBJECT_ROUTE,
                        DomainConstants.TYPE_ROUTE,
                        routeSelectList,
                        null,
                        false,
                        true,
                        (short) 1,
                        "",
                        "",
                        (short) 0);
                allBatchReviewRouteList.addAll(batchReviewRouteList);
            }
            allBatchReviewRouteList.sort(DomainConstants.SELECT_ORIGINATED, "descending", "date");
            if (!allBatchReviewRouteList.isEmpty()) {
                return UIUtil.getValue((Map) allBatchReviewRouteList.get(0), DomainConstants.SELECT_ID);
            }
        }
        StringList selectList = JF_Util_mxJPO.basicBolistSel();
        selectList.add(DomainConstants.SELECT_ORIGINATED);
        MapList routeList = rootObject.getRelatedObjects(context,
                DomainConstants.RELATIONSHIP_OBJECT_ROUTE,
                DomainConstants.TYPE_ROUTE,
                selectList,
                JF_Util_mxJPO.basicRellistSel(),
                false,
                true,
                (short) 1,
                "",
                "",
                (short) 0);
        routeList.sort(DomainConstants.SELECT_ORIGINATED, "descending", "date");
        if (routeList.size() > 0) {
            Map routeMap = (Map) routeList.get(0);
            return UIUtil.getValue(routeMap, DomainConstants.SELECT_ID);
        }
        return "";
    }
}
