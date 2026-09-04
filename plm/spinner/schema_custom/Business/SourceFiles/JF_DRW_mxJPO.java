import com.dassault_systemes.dostreaminformation.ENODerivedOutputStreamInfoService;
import com.dassault_systemes.enovia.enterprisechangemgt.common.ChangeAction;
import com.matrixone.apps.common.CommonDocument;
import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.DomainRelationship;
import com.matrixone.apps.domain.util.*;
import com.matrixone.apps.framework.ui.UIUtil;
import com.matrixone.apps.program.ProgramCentralConstants;
import matrix.db.Context;
import matrix.db.FileList;
import matrix.db.JPO;
import matrix.db.RelationshipType;
import matrix.util.MatrixException;
import matrix.util.StringList;

import java.util.*;
import java.util.stream.Collectors;

import org.apache.commons.text.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.matrixone.apps.domain.DomainConstants.*;

public class JF_DRW_mxJPO {

    private static final Logger _logger =  LoggerFactory.getLogger(JF_DRW_mxJPO.class);
    private final String SUITE_KEY = "emxComponentsStringResource";
    private final String STATE_JFDRW_InWork = "state_InApproval";
    private final String POLICY_JFDRW = "policy_ChangeAction";
    private final String type_ProposedActivity = "Proposed Activity";
    private final String from_ProjectTask_Name = "from[Project Task].to.name";
    private final String state_Complete_actual = "state[Complete].actual";


    /**
     * 获取所有的JFDRW对象
     * @param context
     * @param args
     * @return
     * @throws Exception
     */
    public MapList getJFDRW(Context context, String[] args) throws Exception{
        StringList objectSelects = basicBolistSel();
        String user = context.getUser();
        objectSelects.add("attribute[Synopsis]");
        objectSelects.add("attribute[JF_PublicPurpose]");
        objectSelects.add("attribute[JF_ProjectName]");
        //新增DA管理员可以查看所有的DRW add by chenyan 2025/04/03
        String strLoginUser = context.getUser();
        Vector assignments = PersonUtil.getAssignments(context,strLoginUser );
        _logger.info("assignments:{}", assignments);
        boolean isShowAll = false;
        if (assignments.contains(JF_PLMConstants_mxJPO.ROLE_DRWADMIN)) {
            isShowAll = true;
        }
        String strWhere = "";
        if (!isShowAll) {
            strWhere = JF_PublicMethodClass_mxJPO.buildStringInStrings("owner=='", strLoginUser, "'");
        }
        MapList mapList = DomainObject.findObjects(context, "JF_DRW", "*", strWhere, objectSelects);
        return mapList;
    }


    /**
     * 获取table中的所有的对象id
     * @param args
     * @throws
     * @return matrix.util.StringList
     * @date 2024/7/9 16:35
     * @description
     */
    public static StringList getObjectIdList(String[] args) throws Exception {
        StringList objectIdList = new StringList();
        Map paramsMap = (Map) JPO.unpackArgs(args);
        MapList objectList = (MapList)paramsMap.get("objectList");
        if (objectList.isEmpty()) {
            return objectIdList;
        }else {
            int iTemp = 0;
            for(int iSize = objectList.size(); iTemp < iSize; ++iTemp) {
                Map objectMap = (Map)objectList.get(iTemp);
                if (null != objectMap && objectMap.containsKey(DomainConstants.SELECT_ID)) {
                    String objectId = (String)objectMap.get(DomainConstants.SELECT_ID);
                    objectIdList.add(objectId);
                }
            }
        }
        return objectIdList;
    }


    /**
     * @description:  获取项目名称
     * @param: context
    args
     * @return: java.util.Vector<java.lang.String>
     * @date:  17:52
     */
    public Vector<String> getProject(Context context, String[] args)throws Exception{
        Vector<String> result = new Vector();
        StringList strObjectIdList = getObjectIdList(args);
        StringList bosel = basicBolistSel();
        bosel.add("attribute[First Name]");
        bosel.add("attribute[Last Name]");
        for (String strObjectId : strObjectIdList) {
            StringBuffer stringBuffer = new StringBuffer();
            DomainObject dr = DomainObject.newInstance(context,strObjectId);
            MapList projectList = dr.getRelatedObjects(context,"JFChange2Project",DomainConstants.TYPE_PROJECT_SPACE,bosel,null,
                    false,true,(short) 1,
                    "","",0);
            if(projectList.size()>0) {
                stringBuffer.append("<table><td><a href=\"/3dspace/common/emxNavigator.jsp?objectId=");
                Map projectMap = (Map) projectList.get(0);
                String projectId = (String) projectMap.get("id");
                stringBuffer.append(projectId).append("\" target=\"_blank>\">");
                String projectName = (String) projectMap.get("name");
                projectName = StringEscapeUtils.escapeHtml4(projectName);
                stringBuffer.append(projectName);
                stringBuffer.append("</a></td></table>");
            }
            result.add(stringBuffer.toString());
        }
        return result;

    }

    public StringList getDrwProjectDescription(Context context, String[] args) throws Exception {
        _logger.info("------------------------------------------  getProjectTitle  begin ------------------------------------------");
        StringList strObjectIdList = getObjectIdList(args);
        StringList res = new StringList();
        try {
            ContextUtil.pushContext(context);
            for (int i = 0; i < strObjectIdList.size(); i++) {
                String strID = strObjectIdList.get(i);
                DomainObject obj = DomainObject.newInstance(context, strID);
                String strAttrValue = obj.getInfo(context, "from[JFChange2Project].to[Project Space].description");
                res.add(strAttrValue);
            }
        } finally {
            ContextUtil.popContext(context);
        }
        return res;
    }

    public StringList basicBolistSel(){
        StringList boSel = new StringList();
        boSel.add(DomainConstants.SELECT_ID);
        boSel.add(DomainConstants.SELECT_NAME);
        boSel.add(DomainConstants.SELECT_TYPE);
        boSel.add(DomainConstants.SELECT_REVISION);
        boSel.add(DomainConstants.SELECT_CURRENT);
        return boSel;
    }

    /**
     * 创建DRW后关联经理和整椅经理以及项目
     * @param context
     * @param args
     * @throws Exception
     */
    public void connectRel2Person(Context context,String[] args)throws Exception{
        Map argsMap = (Map)JPO.unpackArgs(args);
        Map requestMap = (Map)argsMap.get("requestMap");
        Map paramMap = (Map)argsMap.get("paramMap");
        String MangerOID = (String)requestMap.get("MangerOID");
        String projectOid = (String)requestMap.get("ProjectNameOID");
        String drId = (String)paramMap.get("objectId");
        DomainObject dr = DomainObject.newInstance(context,drId);
        StringList MangerOIDs = FrameworkUtil.split(MangerOID,"|");
        DomainObject project = DomainObject.newInstance(context, projectOid);
        String ChairMangerOID = project.getInfo(context, "from[Member].to.id");
        //关联整椅经理
        if(ChairMangerOID != null && !"".equals(ChairMangerOID)){
            dr.addToObject(context,new RelationshipType("JFDRWChairManger2Person"),ChairMangerOID);
        }
        //关联经理
//        for(String Manger : MangerOIDs){
//            dr.addToObject(context,new RelationshipType("JFDRWManger2Person"),Manger);
//        }
        String contextUser = context.getUser();
        JF_PublicMethodClass_mxJPO jfPublicMethodClassMxJPO = new JF_PublicMethodClass_mxJPO();
        // 获取直线经理id
        String lineManagerID = jfPublicMethodClassMxJPO.getPersonLineManager(context, null, contextUser);
        // 关联直线经理
        if(!"".equals(lineManagerID)){
            dr.addToObject(context,new RelationshipType("JFDRWManger2Person"),lineManagerID);
        }
        //关联项目
        dr.addToObject(context,new RelationshipType("JFChange2Project"),projectOid);
        //add by ljr
        ContextUtil.pushContext(context);
        dr.promote(context);
        ContextUtil.popContext(context);
        //end
    }


    /**
     * drw列表展示经理
     * @param context
     * @param args
     * @return
     * @throws Exception
     */
    public Vector<String> getManger(Context context,String[] args)throws Exception{
        Vector<String> result = new Vector();
        StringList strObjectIdList = getObjectIdList(args);
        StringList bosel = basicBolistSel();
        bosel.add("attribute[First Name]");
        bosel.add("attribute[Last Name]");
        for (String strObjectId : strObjectIdList) {
            StringBuffer stringBuffer = new StringBuffer();
            DomainObject dr = DomainObject.newInstance(context,strObjectId);
            MapList mangerList = dr.getRelatedObjects(context,"JFDRWManger2Person",DomainConstants.TYPE_PERSON,bosel,null,
                    false,true,(short) 1,
                    "","",0);
            for(int i=0;i<mangerList.size();i++){
                Map mangerMap = (Map)mangerList.get(i);
                String allName = ((String)mangerMap.get("attribute[First Name]")+" "+(String)mangerMap.get("attribute[Last Name]")).trim();
                stringBuffer.append(allName);
                if(i!=mangerList.size()-1){
                    stringBuffer.append("|");
                }
            }
            result.add(stringBuffer.toString());
        }
        return result;
    }

    /**
     * drw列表展示整椅经理
     * @param context
     * @param args
     * @return
     * @throws Exception
     */
    public Vector<String> getChairManger(Context context,String[] args)throws Exception{
        Vector<String> result = new Vector();
        StringList strObjectIdList = getObjectIdList(args);
        StringList bosel = basicBolistSel();
        bosel.add("attribute[First Name]");
        bosel.add("attribute[Last Name]");
        for (String strObjectId : strObjectIdList) {
            StringBuffer stringBuffer = new StringBuffer();
            DomainObject dr = DomainObject.newInstance(context,strObjectId);
            MapList mangerList = dr.getRelatedObjects(context,"JFDRWChairManger2Person",DomainConstants.TYPE_PERSON,bosel,null,
                    false,true,(short) 1,
                    "","",0);
            for(int i=0;i<mangerList.size();i++){
                Map mangerMap = (Map)mangerList.get(i);
                String allName = ((String)mangerMap.get("attribute[First Name]")+" "+(String)mangerMap.get("attribute[Last Name]")).trim();
                stringBuffer.append(allName);
                if(i!=mangerList.size()-1){
                    stringBuffer.append("|");
                }
            }
            result.add(stringBuffer.toString());
        }
        return result;
    }

    /**
     * @description:  过滤存在其他CA或DRW中的图纸 以及owner为当前用户的
     * @param: context
    args
     * @return: matrix.util.StringList
     * @date:  17:53
     */
    public int getExcludeDrawing_OLd(Context context,String[] args)throws Exception{
        int isOK = 0;
        try {
            ContextUtil.pushContext(context);
            String fromId = args[0];
            DomainObject fromObj = DomainObject.newInstance(context,fromId);
            if(fromObj.isKindOf(context ,"Proposed Activity")){
                String proStr = fromObj.getInfo(context,"paths[Proposed Activity.Where].path");
                if(UIUtil.isNotNullAndNotEmpty(proStr) && proStr.contains("Drawing")){
                    String DRWId = fromObj.getInfo(context, "to[Proposed Activities].from.id");
                    DomainObject drwObj = DomainObject.newInstance(context, DRWId);
                    String user = drwObj.getInfo(context, "owner");
                    String[] prdArr = proStr.split(",");
                    String prdId=prdArr[2];
                    DomainObject domainObject = DomainObject.newInstance(context, prdId);
                    String owner = domainObject.getInfo(context, "owner");
                    String name = domainObject.getName();
                    if (!user.equals(owner)){
                        String msg = EnoviaResourceBundle.getProperty(context, SUITE_KEY, context.getLocale(), "emxComponents.DRWCheckDrawingOwner.RELEASED.MESS");
                        emxContextUtil_mxJPO.mqlNotice(context, name + msg);
                        return 1;
                    }
                    if (hasCa(context, prdId)){
                        String msg = EnoviaResourceBundle.getProperty(context, SUITE_KEY, context.getLocale(), "emxComponents.DRWCheckDrawingCADRW.RELEASED.MESS");
                        emxContextUtil_mxJPO.mqlNotice(context, name + msg);
                        return 1;
                    }
                }
            }

        }catch (Exception e){
            e.printStackTrace();
        }finally {
            ContextUtil.popContext(context);
        }
        return isOK;
    }

    public int getExcludeDrawing(Context  context,String args[]) throws Exception{
        int isOK=0;
        try{
            String fromId=args[0];//ca or (中间对象)Proposed Activity
            String toId=args[1];
            _logger.info(" triggerIsCheckVPMCanAddedToChangAction from id "+fromId);
            _logger.info(" testCheck to id "+toId );
            DomainObject fromObj=DomainObject.newInstance(context);
            fromObj.setId(fromId);
            //触发场景1，CA from与中间 to 对象（Proposed Activity）关联 此时trigger里通过中间toId对象通过paths获取不了添加的对象；
            //触发场景2，关联了CA的中间对象fromId与 to端中间对象（Proposed Activity）关联  此时trigger程序里面通过中间fromId对象，通过paths可获取到添加的对象；

            //限制类型 from端类型为 type_ProposedActivity
            //只有场景2 触发才会取到添加的对象 程序才能进行数据验证；
            if(fromObj.isKindOf(context ,type_ProposedActivity)) {
                DomainObject toObj = new DomainObject(toId);
                toObj.setId(toId);
                if (toObj.isKindOf(context, type_ProposedActivity)) {
                    String proStr = fromObj.getInfo(context, "paths[Proposed Activity.Where].path");
                    _logger.info("proStr >>2>" + proStr);
                    //限制为to端为数模类型 才进行校验 避免其他类型触发导致程序bug
                    if (UIUtil.isNotNullAndNotEmpty(proStr) && proStr.contains(JF_PLMConstants_mxJPO.TYPE_Drawing)) {
                        String[] prdArr = proStr.split(",");
                        String prdId = prdArr[2];
                        _logger.info("prdId >>>" + prdId);
                        DomainObject drwObj = new DomainObject(prdId);
                        String caId = drwObj.getInfo(context, "attribute[Change Id]");
                        _logger.info("caId:{}", caId);
//                    String errorMsg=checkPrdISCanAddForCA(context,new String[]{prdId});
                        String errorMsg = "";
                        if (!getCAByAffectId(context, prdId,fromId)) {
                            errorMsg = EnoviaResourceBundle.getProperty(context, SUITE_KEY, context.getLocale(), "emxComponents.DRWCheckDrawingCADRW.RELEASED.MESS");
                        }
                        _logger.info("errorMsg >>" + errorMsg.trim());
                        errorMsg = errorMsg.trim();
                        if (UIUtil.isNotNullAndNotEmpty(errorMsg)) {
                            isOK = 1;
                            DomainObject.deleteObjects(context, new String[]{toId});
                            //space 提示信息
                            MqlUtil.mqlCommand(context, "notice $1", errorMsg);
                            //dashboard 可见错误提示
//                            new Exception(errorMsg);
                        }
                    }
                }
            }
        }catch (Exception ex){
            isOK=1;
            ex.printStackTrace();
//            throw ex;
        }
        _logger.info("isOK >>"+isOK);
        return isOK;
    }

    public static boolean getCAByAffectId(Context context, String AffectedId,String parentId) {
        MapList mlCAs = new MapList();
        try {
         /*   Map m = new HashMap();
            m.put(ChangeConstants.OBJECT_ID, AffectedId);
            m.put("functionality", "isChangeActionTab");
            enoECMChangeUtil_mxJPO enoECMChangeUtil = new enoECMChangeUtil_mxJPO(context, null);
            mlCAs = enoECMChangeUtil.getConnectedChanges(context, JPO.packArgs(m));*/
            String mql ="query path type 'Proposed Activity.Where' starting with '"+AffectedId+"' where owner.id!=='"+parentId+"' select  owner.to[Proposed Activities].id dump @";
            _logger.info("mql:{}",mql);
            String result =  MqlUtil.mqlCommand(context, true, mql, false);
            _logger.info("result:{}",result);
            if(UIUtil.isNotNullAndNotEmpty(result)){
                return false;
            }
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


    /**
     * @description:  DRW提升到审批中时创建流程
     * @param: context
    args
     * @return: void
     */
    public void startDRWRoute(Context context, String[] args) throws Exception {
        String objId = args[0];
        String type = args[1];
        String ChairMangerOID = "";
        StringList objectSelects = new StringList();
        objectSelects.add(DomainConstants.SELECT_ID);
        objectSelects.add(DomainConstants.SELECT_NAME);

        StringList relSel = new StringList();
        relSel.add("attribute[Project Role]");

        if ("JF_DRW".equals(type)){
            DomainObject drw = DomainObject.newInstance(context, objId);
            String MangerOID = drw.getInfo(context, "from[JFDRWManger2Person].to.id");
            String projectManagerID = drw.getInfo(context, "from[JFChange2Project].to.id");
            DomainObject projectObj = new DomainObject(projectManagerID);
            MapList mapList = projectObj.getRelatedObjects(context, "Member", "Person", objectSelects, relSel,
                    false, true, (short) 1,
                    "", "", 0);
            for (int i = 0; i < mapList.size(); i++) {
                Map map = (Map) mapList.get(i);
                String projectRole = (String) map.get("attribute[Project Role]");
                if("Chair manager".equals(projectRole)){
                    ChairMangerOID = (String) map.get("id");
                }
            }
            JF_Route_mxJPO jf_route = new JF_Route_mxJPO(context, args);
            //审批人员信息
            MapList approveList = new MapList();

            //经理审批节点
            if (MangerOID != null && !"".equals(MangerOID)){
                if (!MangerOID.equals(ChairMangerOID)){
                    String tileMess = EnoviaResourceBundle.getProperty(context, SUITE_KEY, context.getLocale(), "emxComponents.DRManger.ROUTE.MESS");
                    Map nReceiverMapOne = (Map) jf_route.getMap(MangerOID, tileMess);//设置审批信息 标题
                    nReceiverMapOne.put(DomainObject.ATTRIBUTE_ROUTE_SEQUENCE, "1");
                    approveList.add(nReceiverMapOne);
                }
            }
            //整椅经理审批节点
            String tileMess2 = EnoviaResourceBundle.getProperty(context, SUITE_KEY, context.getLocale(), "emxComponents.DRChairManger.ROUTE.MESS");
            Map nReceiverMapOne2 = (Map) jf_route.getMap(ChairMangerOID, tileMess2);//设置审批信息 标题
            nReceiverMapOne2.put(DomainObject.ATTRIBUTE_ROUTE_SEQUENCE, "2");
            approveList.add(nReceiverMapOne2);
            String routeId = jf_route.createAndStartRoute(context, approveList, objId, STATE_JFDRW_InWork, POLICY_JFDRW, tileMess2);
        }
    }

    /**
     * DRW发起流程的时候，如果关联了数模，需要检查图纸关联的数模是否发布
     * @param context
     * @param args
     * @return
     * @throws Exception
     */
    public int checkVPMReference(Context context, String[] args) throws Exception {
        String objId = args[0];
        String type = args[1];
        if ("JF_DRW".equals(type)){
            MapList mapList = getRecommendation(context, objId);
            Iterator iterator = mapList.iterator();
            StringList objectSelects = basicBolistSel();
            StringBuffer stringBuffer = new StringBuffer();

            while (iterator.hasNext()){
                Map map = (Map) iterator.next();
                String id = (String) map.get("id");
                DomainObject domainObject = DomainObject.newInstance(context, id);
                MapList VPMList = domainObject.getRelatedObjects(context,"XCADBaseDependency","VPMReference", objectSelects,null,
                        false,true,(short) 1,
                        "","",0);
                Iterator iteratorVPM = VPMList.iterator();
                while (iteratorVPM.hasNext()){
                    Map VPMMap = (Map) iteratorVPM.next();
                    String current = (String) VPMMap.get("current");
                    if (!"RELEASED".equals(current)){
                        String name = (String) VPMMap.get("name");
                        stringBuffer.append(name + ";");
                    }
                }
            }

            //add by ljr 校验DRW提升审核的时候 附件的必填
            StringBuilder strMess = new StringBuilder();
            StringList strBusSelectList = new StringList();
            String str = "from[" + DomainRelationship.RELATIONSHIP_REFERENCE_DOCUMENT + "]";
            strBusSelectList.add(str);
            MapList resultList = DomainObject.getInfo(context, new String[]{objId}, strBusSelectList);
            String value = UIUtil.getValue((Map) resultList.get(0), str);
            if ("FALSE".equalsIgnoreCase(value)) {
                strMess.append(EnoviaResourceBundle.getProperty(context, SUITE_KEY, context.getLocale(), "emxComponents.DRWCheckDOC.Mess"));
            }
            //end

            if (stringBuffer.length() > 0){
                if (strMess.length() > 0) {
                    strMess.append(";");
                }
                String msg = EnoviaResourceBundle.getProperty(context, SUITE_KEY, context.getLocale(), "emxComponents.DRWCheckVPM.RELEASED.MESS");
                strMess.append(stringBuffer).append(msg);
            }
            if (strMess.isEmpty()) {
                return 0;
            }  else {
                emxContextUtil_mxJPO.mqlNotice(context, strMess.toString());
                return 1;
            }

        }
        return 0;
    }


    //获取建议的更改对象
    public MapList getRecommendation(Context context, String id) throws Exception {
        MapList mapList = new ChangeAction(id).getAffectedItems(context);
        return mapList;
    }


    /**
     * 查看是否关联了CA   或者 DRW
     * @param context
     * @param id
     * @return
     */
    public boolean hasCa(Context context, String id) throws MatrixException {
        HashMap<String, String> argsMap = new HashMap<>();
        argsMap.put("functionality", "isChangeActionTab");
        argsMap.put("objectId", id);
        MapList caList = JPO.invoke(context, "enoECMChangeUtil", null, "getConnectedChanges", JPO.packArgs(argsMap), MapList.class);
        if (caList.size() > 0){
            return true;
        }
        return false;
    }


    /**
     *
     *@description 获取项目
     *@param: *@param context
     *@param args
     *@return: java.lang.String
     *@throws:
     *@date: 2024/7/18 16:52
     */
    public  String getProjectForm(Context context, String[] args)throws Exception{
        Map paramMap = (Map) JPO.unpackArgs(args);
        Map requestMap = (Map) paramMap.get("requestMap");
        String objectId = (String)requestMap.get("objectId");
        StringList bosel = basicBolistSel();
        StringBuffer stringBuffer = new StringBuffer();
        DomainObject dr = DomainObject.newInstance(context,objectId);
        MapList projectList = dr.getRelatedObjects(context,"JFChange2Project",DomainConstants.TYPE_PROJECT_SPACE,bosel,null,
                false,true,(short) 1,
                "","",0);
        if(projectList.size()>0){
            stringBuffer.append("<table><td><a href=\"/3dspace/common/emxNavigator.jsp?objectId=");
            Map projectMap = (Map) projectList.get(0);
            String projectId = (String) projectMap.get("id");
            stringBuffer.append(projectId).append("\" target=\"_blank>\">");
            String projectName = (String) projectMap.get("name");
            stringBuffer.append(projectName);
            stringBuffer.append("</a></td></table>");
        }
        return stringBuffer.toString();
    }
//
    /**
     * 修改DRW的项目关系
     * @param context
     * @param args
     * @throws Exception
     */
    public void updateProjectForm(Context context, String[] args) throws Exception {
        Map argsMap = (Map) JPO.unpackArgs(args);
        Map paramMap = (Map) argsMap.get("paramMap");
        String newId = (String) paramMap.get("New OID");
        String objectId = (String) paramMap.get("objectId");
        String oldId = (String) paramMap.get("Old OID");
        if (!newId.equals(oldId)){
            DomainObject dr = DomainObject.newInstance(context,objectId);
            String oldReId = dr.getInfo(context, "relationship[JFChange2Project].id");
            DomainRelationship.disconnect(context, oldReId);
            dr.addToObject(context,new RelationshipType("JFChange2Project"),newId);

            String oldReChairId = dr.getInfo(context, "relationship[JFDRWChairManger2Person].id");

            DomainObject projectDomain = DomainObject.newInstance(context, newId);
            String newChairId = projectDomain.getInfo(context, "from[Member].to.id");
            String oldChairId = dr.getInfo(context, "from[JFDRWChairManger2Person].to.id");
            if (!newChairId.equals(oldChairId)){
                if (UIUtil.isNotNullAndNotEmpty(oldReChairId)){
                    DomainRelationship.disconnect(context, oldReChairId);
                }
                dr.addToObject(context,new RelationshipType("JFDRWChairManger2Person"),newChairId);
            }
        }

    }

    /**
     * 修改DRW的经理关系
     * @param context
     * @param args
     * @throws Exception
     */
    public void updatePersonForm(Context context, String[] args) throws Exception {
        try {
            ContextUtil.pushContext(context);
            Map argsMap = (Map) JPO.unpackArgs(args);
            Map paramMap = (Map) argsMap.get("paramMap");
            String newOID = (String)paramMap.get("New OID");
            String newName = (String)paramMap.get("New Value");
            String oldName = (String)paramMap.get("Old value");
            String objectId = (String)paramMap.get("objectId");
            if (!newName.equals(oldName)){
                DomainObject dr = DomainObject.newInstance(context,objectId);
                String oldReId = dr.getInfo(context, "relationship[JFDRWManger2Person].id");
                if (UIUtil.isNotNullAndNotEmpty(oldReId)){
                    DomainRelationship.disconnect(context, oldReId);
                }
                if (!"".equals(newOID))
                    dr.addToObject(context,new RelationshipType("JFDRWManger2Person"),newOID);
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            ContextUtil.popContext(context);
        }
    }
//
//    /**
//     * 修改DRW的椅子经理关系
//     * @param context
//     * @param args
//     * @throws Exception
//     */
//    public void updateChairPersonForm(Context context, String[] args) throws Exception {
//        System.out.println("=======================2222");
//        System.out.println("222222222222222222222222222");
//        Map paramMap = (Map) JPO.unpackArgs(args);
//        Map requestMap = (Map) paramMap.get("requestMap");
//        System.out.println("=============================");
//        System.out.println("requestMap::" + requestMap);
//        System.out.println("============================");
//        String ChairMangerOID = (String)requestMap.get("ChairMangerOID");
//        String objectId = (String)requestMap.get("objectId");
//        DomainObject dr = DomainObject.newInstance(context,objectId);
//        String oldReId = dr.getInfo(context, "relationship[JFDRWChairManger2Person].id");
//        DomainRelationship.disconnect(context, oldReId);
//        dr.addToObject(context,new RelationshipType("JFDRWChairManger2Person"),ChairMangerOID); //JFDRWChairManger2Person
//    }

    /**
     * form表单查看经理
     * @param context
     * @param args
     * @return
     * @throws Exception
     */
    public String getMangerForm(Context context,String[] args)throws Exception{
        Map paramMap = (Map) JPO.unpackArgs(args);
        Map requestMap = (Map) paramMap.get("requestMap");
        String objectId = (String)requestMap.get("objectId");
        StringBuffer stringBuffer = new StringBuffer();
        if(UIUtil.isNotNullAndNotEmpty(objectId)) {
            StringList bosel = basicBolistSel();
            bosel.add("attribute[First Name]");
            bosel.add("attribute[Last Name]");
            DomainObject dr = DomainObject.newInstance(context, objectId);
            MapList mangerList = dr.getRelatedObjects(context, "JFDRWManger2Person", DomainConstants.TYPE_PERSON, bosel, null,
                    false, true, (short) 1,
                    "", "", 0);
            for (int i = 0; i < mangerList.size(); i++) {
                Map mangerMap = (Map) mangerList.get(i);
                String allName = ((String) mangerMap.get("attribute[First Name]") + " " + (String) mangerMap.get("attribute[Last Name]")).trim();
                stringBuffer.append(allName);
                if (i != mangerList.size() - 1) {
                    stringBuffer.append("|");
                }
            }
        }
        return stringBuffer.toString();
    }


    /**
     * form表单查看整椅经理
     * @param context
     * @param args
     * @return
     * @throws Exception
     */
    public String getChairMangerForm(Context context,String[] args)throws Exception{
        Map paramMap = (Map) JPO.unpackArgs(args);
        Map requestMap = (Map) paramMap.get("requestMap");
        StringList relsel = new StringList();
        String objectId = (String)requestMap.get("objectId");
        StringBuffer stringBuffer = new StringBuffer();
        if(UIUtil.isNotNullAndNotEmpty(objectId)) {
            StringList bosel = basicBolistSel();
            bosel.add("attribute[First Name]");
            bosel.add("attribute[Last Name]");
            relsel.add("attribute[Project Role]");
            DomainObject dr = DomainObject.newInstance(context, objectId);
            String projectId = dr.getInfo(context, "from[JFChange2Project].to.id");
//            MapList mangerList = dr.getRelatedObjects(context, "JFChange2Project", DomainConstants.TYPE_PROJECT_SPACE, bosel, relsel,
//                    false, true, (short) 1,
//                    "", "", 0);
            DomainObject projectSpace = new DomainObject(projectId);
            MapList mapList = projectSpace.getRelatedObjects(context, "Member", DomainConstants.TYPE_PERSON, bosel, relsel,
                    false, true, (short) 1,
                    "", "", 0);
            for (int i = 0; i < mapList.size(); i++) {
                Map map = (Map) mapList.get(i);
                String projectRole = (String) map.get("attribute[Project Role]");
                if("Chair manager".equals(projectRole)){
                    String lastName = (String) map.get("attribute[Last Name]");
                    String firstName = (String) map.get("attribute[First Name]");
                    stringBuffer.append(lastName).append(" ").append(firstName);
                }
            }
        }
        return stringBuffer.toString();
    }

    /**
    *    DRW关联的文档图纸进行签名:
    *          1. DRW发布的时候增加Trigger，查询关联的数据
    *          2. 如果是文档类型，在文件是否是PDF，如果是PDF就进行签名
    *          3. 签完名之后再把文件升版(不是文档升版)checkIn回去
    * @param context
	* @param args
    * @author LIUJR
    * @throws
    * @return void
    * @date 2025/4/1 15:35
    * @description
    */
    public void DRWReleaseDrawingDocActive(Context context, String[] args) throws Exception{
        try {
            ContextUtil.pushContext(context);
            String drwId = args[0];
            _logger.info("drwId:{}", drwId);
            DomainObject domainObject = DomainObject.newInstance(context);
            domainObject.setId(drwId);
            String type = domainObject.getInfo(context, DomainConstants.SELECT_TYPE);
            if (!"JF_DRW".equalsIgnoreCase(type)) {
                return;
            }
            //获取关联的文档图纸
            StringList caContentList = domainObject.getInfoList(context, "from[Proposed Activities].to.paths[Proposed Activity.Where].path");
            if (caContentList.isEmpty()) {
                return;
            }
            //更改内容
            MapList mapList = new MapList();
            for (String caContent : caContentList) {
                String[] prdArr = caContent.split(",");
                String caContentId = prdArr[2];
                domainObject.setId(caContentId);
                String strType = domainObject.getInfo(context, DomainConstants.SELECT_TYPE);
                if (DomainConstants.TYPE_DOCUMENT.equalsIgnoreCase(strType)) {
                    //文档类型，在文件是否是PDF，如果是PDF就进行签名
                    MapList versionList = JF_PublicMethodClass_mxJPO.getDocumentFiles(context, caContentId);
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
                            paramMap.put("docId", caContentId);
                            paramMap.put("fileId", fileId);
                            paramMap.put("fileName", fileName);
                            paramMap.put("fileFormat", fileFormat);
                            paramMap.put("ownerName", owner);
                            paramMap.put("ownerDate", createDate);
                            mapList.add(paramMap);
                        }
                    }
                }
            }
            if (mapList.isEmpty()) {
                return;
            }
            //拿取签名参数
            HashMap<String, String> signMap = new HashMap<>();
            getECROrDRWParamsMap(context, drwId, signMap);
            //开始签名
            HashMap<String, Object> paramsMap = new HashMap<>();
            paramsMap.put("list", mapList);
            paramsMap.put("signMap", signMap);
            paramsMap.put("flag", "DOC");
            ContextUtil.startTransaction(context, true);
            JF_ElectronicSignature_mxJPO.electronicSignAssembly(context, JPO.packArgs(paramsMap));
            ContextUtil.commitTransaction(context);
        }catch (Exception e) {
            e.printStackTrace();
            ContextUtil.abortTransaction(context);
        } finally {
            ContextUtil.popContext(context);
        }
    }

    /**
    * 图纸发布 的trigger
     * 1. <Drawing>类型状态提升至[RELEASED]状态时，自动触发trigger，trigger逻辑如下：
     * 2. 判断是否关联ECR/DRW，如果不关联，结束；如果关联执行以下逻辑：
     *      1.获取Drawing的Owner信息；获取Drawing关联的ECR/DRW
     *      2. 首先判断Drawing是否直接关联DRW，如果true，获取对应DRW；如果否执行以下逻辑：
     *      3. Drawing不会直接关联ECR，需要通过Drawing关联的模型，判断模型是否关联ECR
     *      4. 获取ECR/DR/DRW审核Route,获取Inbox Task节点的审批人（直线经理，整椅经理）和日期
     *      5. 通过<DerivedOutputRelationship>关系查询DerivedOutputEntity,获取DerivedOutputEntity关联的PDF文件
     *      6.遍历PDF文件的所有sheet页;判断页码，是否为最后一页，如果是，结束；否则执行以下逻辑：
     *          - 按照坐标签名（中文名）
     *          - 按照坐标注明日期（2025-01-05）
     *      7.重新Checkin文件到对应的DerivedOutputEntity中
     *      8. 20250258  update：
     *          新ECR 签发图纸， 同方法需要兼容
     *          区别点： ECR 与 NewECR的审批流程不同
    * @param context
	* @param args
    * @author LIUJR
    * @throws
    * @return void
    * @date 2025/4/2 16:15
    * @description
    */
    public void DrawingReleaseSignature(Context context, String[] args) throws Exception {
        try{
            ContextUtil.pushContext(context);
            String drawId = args[0];
            DomainObject domainObject = DomainObject.newInstance(context);
            domainObject.setId(drawId);
            String type = domainObject.getInfo(context, DomainConstants.SELECT_TYPE);
            if (!"Drawing".equalsIgnoreCase(type)) {
                return;
            }
            //获取图纸的最新版本的衍生物
            String physicalid = domainObject.getInfo(context, SELECT_PHYSICAL_ID);
            JF_Util_mxJPO jfUtilMxJPO = new JF_Util_mxJPO();
            StringList derivedOutputFileListMapList = jfUtilMxJPO.getDerivedOutputFileListName(context, new String[]{physicalid});
       /*     List infoList = (List) derivedOutputFileListMap.get(physicalid);
            MapList derivedOutputFileListMapList = new MapList();
            derivedOutputFileListMapList.addAll(infoList);*/
            //拿取签名
            String ownerPerson = domainObject.getInfo(context, DomainConstants.SELECT_OWNER);
            String ownerDate = domainObject.getInfo(context, "state[FROZEN].actual");
            String physicalId = domainObject.getInfo(context, "physicalid");
            _logger.info("ownerDate:{}", ownerDate);
            _logger.info("ownerPerson:{}", ownerPerson);
            _logger.info("physicalId:{}", physicalId);
            String pdfId = domainObject.getInfo(context, "from[DerivedOutputRelationship].to.id");
            String partId = domainObject.getInfo(context, "from[XCADBaseDependency].to.id");
            String ercDrwId = DomainConstants.EMPTY_STRING;
            String whereId = MqlUtil.mqlCommand(context, "query path type 'Proposed Activity.Where' containsany  " + physicalId + " select owner.id dump;");
            _logger.info("whereId:{}", whereId);
            if (UIUtil.isNotNullAndNotEmpty(whereId)) {
                String[] split = whereId.split(",");
                whereId = split[1];
                _logger.info("whereId:{}", whereId);
                //反查DRW
                //query  path   type 'Proposed Actibity.where'   containsany    '变更对象的物理id'    select owner.id;
                //找到owner.id后，用owner id去查到对应的ca，作为from端查询：
                DomainObject domainObject1 = DomainObject.newInstance(context, whereId);
                ercDrwId = domainObject1.getInfo(context, "to[Proposed Activities].from.id");
            } else {
                //ECR
                domainObject.setId(partId);
                ercDrwId = domainObject.getInfo(context, "to[JFRelateItem].from.id");
                if (UIUtil.isNullOrEmpty(ercDrwId)) {
                    //标准件发布
                    ercDrwId = domainObject.getInfo(context, "to[JFSPartsApplication2VPMReference].from.id");
                }
            }
            _logger.info("ercDrwId：{}", ercDrwId);
            if (UIUtil.isNullOrEmpty(ercDrwId)) {
                return;
            }
            //拿取对象的审批名字
            //拿取签名参数
            HashMap<String, String> signMap = new HashMap<>();
            getECROrDRWParamsMap(context, ercDrwId, signMap);
            if (signMap.isEmpty()) {
                return;
            }
            //开始签名
            signMap.put("ownerName", ownerPerson);
            signMap.put("ownerDate", ownerDate);
            _logger.info("signMap:{}", signMap.toString());
            MapList mapList = new MapList();
            for (int i = 0; i < derivedOutputFileListMapList.size(); i++) {
                    String name =derivedOutputFileListMapList.get(i);
                    HashMap<String, String> paramMap = new HashMap<>();
                    paramMap.put("docId", pdfId);
                    paramMap.put("fileName", name);
                    mapList.add(paramMap);
            }
            HashMap<String, Object> paramsMap = new HashMap<>();
            paramsMap.put("list", mapList);
            paramsMap.put("signMap", signMap);
            paramsMap.put("flag", "DRAW");
            ContextUtil.startTransaction(context, true);
            JF_ElectronicSignature_mxJPO.electronicSignAssembly(context, JPO.packArgs(paramsMap));
            ContextUtil.commitTransaction(context);
        }catch (Exception e) {
            e.printStackTrace();
            ContextUtil.abortTransaction(context);
        } finally {
            ContextUtil.popContext(context);
        }
    }

    /**
    * 获取参数
    * @param context
	* @param drwOrECRId
    * @author LIUJR
    * @throws
    * @return void
    * @date 2025/4/3 14:54
    * @description
    */
    public void getECROrDRWParamsMap(Context context, String drwOrECRId, HashMap<String, String> signMap) throws Exception {
        try {
            DomainObject domainObject = DomainObject.newInstance(context);
            //拿取签名参数
            domainObject.setId(drwOrECRId);
            String strType = domainObject.getInfo(context, SELECT_TYPE);
            String reviewPerson = DomainConstants.EMPTY_STRING;
            String reviewDate = DomainConstants.EMPTY_STRING;
            String completePerson = DomainConstants.EMPTY_STRING;
            String completeDate = DomainConstants.EMPTY_STRING;
            StringList infoList = new StringList();
            StringList busSelectList = JF_Util_mxJPO.basicBolistSel();
            StringList relSelectList = JF_Util_mxJPO.basicRellistSel();
            busSelectList.add(DomainConstants.SELECT_ORIGINATED);
            busSelectList.add("attribute[Route Activity State]");
            MapList mapList = domainObject.getRelatedObjects(context,
                    DomainConstants.RELATIONSHIP_OBJECT_ROUTE,
                    DomainConstants.TYPE_ROUTE,
                    busSelectList,
                    relSelectList,
                    false,
                    true,
                    (short) 1,
                    "",
                    "",
                    0
            );
            //过滤掉驳回的流程
            mapList = (MapList) mapList.stream().filter(m -> {
                Map map = (Map) m;
                String status = UIUtil.getValue(map, "attribute[Route Activity State]");
                if ("Rejected".equalsIgnoreCase(status)) {
                    return false;
                } else {
                    return true;
                }
            }).collect(Collectors.toCollection(MapList::new));
            mapList.addSortKey(SELECT_ORIGINATED, ProgramCentralConstants.ASCENDING_SORT, ProgramCentralConstants.SORTTYPE_DATE);
            mapList.sort();
            int i = 0;
            if (mapList.size() >= 2) {
                i = mapList.size() - 1 - 1;
            }
            Map map2 = (Map) mapList.get(i);
            String routeId = (String) map2.get(DomainConstants.SELECT_ID);
            DomainObject route = DomainObject.newInstance(context, routeId);
            infoList = route.getInfoList(context, "to[Route Task].from.id");
            if (infoList.isEmpty()) {
                return;
            }
            busSelectList.add(from_ProjectTask_Name);
            busSelectList.add(state_Complete_actual);
            MapList list = DomainObject.getInfo(context, infoList.toStringArray(), busSelectList);
            //排序
            list.addSortKey(SELECT_ORIGINATED, ProgramCentralConstants.ASCENDING_SORT, ProgramCentralConstants.SORTTYPE_DATE);
            list.sort();
            //拿取了所有需要电子签名的图纸文档  现在开始电子签名
            if ("JFNewECR".equalsIgnoreCase(strType) || "JFFormalECR".equalsIgnoreCase(strType)) {
                //获取JFNewECR的项目中的整椅经理
                String projectId = domainObject.getInfo(context, "from[JFChange2Project].to.id");
                String chairManagerId = JF_PublicMethodClass_mxJPO.getProjectWherePerson(context, projectId, SELECT_ID, "", "attribute[Project Role]=='Chair manager'");
                Map map = (Map) list.get(0);
                reviewPerson = UIUtil.getValue(map, from_ProjectTask_Name);
                reviewDate = UIUtil.getValue(map, state_Complete_actual);
                Map map1 = (Map) list.get(1);
                if (UIUtil.isNullOrEmpty(chairManagerId)) {
                    //没有 整椅经理
                    completePerson = UIUtil.getValue(map1, from_ProjectTask_Name);
                    completeDate = UIUtil.getValue(map1, state_Complete_actual);
                } else {
                    //有整椅经理 需要与流程做匹配
                    DomainObject domainObject1 = DomainObject.newInstance(context, chairManagerId);
                    String chairName = domainObject1.getInfo(context, SELECT_NAME);
                    for (int i1 = 1; i1 < list.size() - 1 ; i1++) {
                        Map map3 = (Map) list.get(i1);
                        String personName = UIUtil.getValue(map3, from_ProjectTask_Name);
                        if (personName.equalsIgnoreCase(chairName)) {
                            completePerson = personName;
                            completeDate = UIUtil.getValue(map3, state_Complete_actual);
                            break;
                        }
                    }
                    if (UIUtil.isNullOrEmpty(completeDate) && UIUtil.isNullOrEmpty(completePerson)) {
                        //没有匹配的人  拿取第一个的时间
                        completePerson = chairName;
                        completeDate = UIUtil.getValue(map1, state_Complete_actual);
                    }
                }
            } else if ("JF_DRW".equalsIgnoreCase(strType)){
                if (list.size() == 1) {
                    Map map = (Map) list.get(0);
                    reviewPerson = UIUtil.getValue(map, from_ProjectTask_Name);
                    completePerson = UIUtil.getValue(map, from_ProjectTask_Name);
                    reviewDate = UIUtil.getValue(map, state_Complete_actual);
                    completeDate = UIUtil.getValue(map, state_Complete_actual);
                } else if (list.size() == 2) {
                    Map map = (Map) list.get(0);
                    reviewPerson = UIUtil.getValue(map, from_ProjectTask_Name);
                    reviewDate = UIUtil.getValue(map, state_Complete_actual);
                    Map map1 = (Map) list.get(1);
                    completePerson = UIUtil.getValue(map1, from_ProjectTask_Name);
                    completeDate = UIUtil.getValue(map1, state_Complete_actual);
                }
            } else if ("JFSPartsApplication".equalsIgnoreCase(strType)) {
                Map map = (Map) list.get(0);
                reviewPerson = UIUtil.getValue(map, from_ProjectTask_Name);
                reviewDate = UIUtil.getValue(map, state_Complete_actual);
            }
            //开始签名
            signMap.put("reviewName", reviewPerson);
            signMap.put("reviewDate", reviewDate);
            signMap.put("completeName", completePerson);
            signMap.put("completeDate", completeDate);
            _logger.info("signMap:{}", signMap.toString());
        }catch (Exception e) {
            e.printStackTrace();
        }
    }
}
