import com.dassault_systemes.enovia.enterprisechangemgt.common.ChangeAction;
import com.matrixone.apps.common.Route;
import com.matrixone.apps.common.util.ComponentsUIUtil;
import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.DomainRelationship;
import com.matrixone.apps.domain.util.*;
import com.matrixone.apps.framework.ui.UIUtil;
import matrix.db.*;
import matrix.util.StringList;
import org.apache.commons.text.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.*;

import static com.matrixone.apps.domain.DomainConstants.SELECT_ID;
import static com.matrixone.apps.domain.DomainConstants.TYPE_DOCUMENT;
import static com.matrixone.apps.domain.MultiValueSelects.RELATIONSHIP_REFERENCE_DOCUMENT;

/**
 *
 * @author jjs
 */
public class JF_DR_mxJPO implements JF_PLMConstants_mxJPO{
    private  static final Logger logger = LoggerFactory.getLogger(JF_DR_mxJPO.class);
    private final String SUITE_KEY = "emxComponentsStringResource";
    private final String POLICY_JFDR = "policy_JFDR";
    private final String STATE_JFDR_APPROVE = "state_Approve";
    private final String RELATIONSHIP_JFDR2VPMREFERENCE = "JFDR2VPMReference";
    private final String ATTRIBUTE_JFISFOLLOW = "JFIsFollow";
    private final String TYPE_VPMREFERENCE = "VPMReference";
    private final String RELATIONSHIP_VPMINSTANCE = "VPMInstance";
    private final String RELATIONSHIP_JFCHANGE2PROJECT = "JFChange2Project";
    private final String RELATIONSHIP_XCADBASEDEPENDENCY = "XCADBaseDependency";
    private final String ATTRIBUTE_ConnectDR = "JF_VPMReference.JF_ConnectDR";
    protected static final String ATTRIBUTE_DEMOTE_ON_REJECTION = PropertyUtil.getSchemaProperty("attribute_DemoteOnRejection");
    //和DR相关的方式

    /**
     * 查询系统中所有的DR
     * @param context
     * @param args
     * @return
     * @throws Exception
     */
    /**
    *
    *@description 查询系统中所有的DR
    *@param: *@param context
     *@param args
    *@return: com.matrixone.apps.domain.util.MapList
    *@throws:
    *@author: JJS
    *@date: 2024/7/18 16:57
    */
    public MapList getAllDRList(Context context, String[] args)throws Exception{
        JF_Util_mxJPO utilMxJPO = new JF_Util_mxJPO();
        String loginer = context.getUser();
        StringList busSel = utilMxJPO.basicBolistSel();
        busSel.add(DomainConstants.SELECT_OWNER);
        MapList DRList  = DomainObject.findObjects(context,TYPE_JFDR, DomainConstants.QUERY_WILDCARD,"",busSel);
        MapList result = new MapList();
        String strLoginUser = context.getUser();
        //新增DR管理员可以查看所有的DR add by chenyan 2025/04/03
        Vector assignments = PersonUtil.getAssignments(context, strLoginUser);
        boolean isShowAll = false;
        if (assignments.contains(JF_PLMConstants_mxJPO.ROLE_DRADMIN)) {
            isShowAll = true;
        }
        if (isShowAll){
            result = DRList;
        }else {
            for(int i=0;i<DRList.size();i++){
                Map m = (Map) DRList.get(i);
                String owner = (String) m.get(DomainConstants.SELECT_OWNER);
                String id = (String)m.get(DomainConstants.SELECT_ID);
                if(owner.equals(loginer)){
                    result.add(m);
                }else{
                    DomainObject dr = DomainObject.newInstance(context,id);
                    StringList approver = dr.getInfoList(context,"from[Object Route].to.from[Route Node].to.name");
                    for(String approverName : approver){
                        if(approverName.contains(loginer)){
                            result.add(m);
                            break;
                        }
                    }
                }
            }
        }

        return result;
    }

    /**
     * 创建DR后关联经理和整椅经理
     * @param context
     * @param args
     * @throws Exception
     */
    public void connectRel2Person(Context context,String[] args)throws Exception{
        try {
            logger.info("connectRel2Person>>>>>>>>>");
            Map argsMap = (Map) JPO.unpackArgs(args);
            Map requestMap = (Map) argsMap.get("requestMap");
            Map paramMap = (Map) argsMap.get("paramMap");
            String MangerOID = (String) requestMap.get("MangerOID");
            String projectOid = (String) requestMap.get("ProjectNameOID");
            String drId = (String) paramMap.get("objectId");
            DomainObject dr = DomainObject.newInstance(context, drId);
            //关联项目
            dr.addToObject(context, new RelationshipType(RELATIONSHIP_JFCHANGE2PROJECT), projectOid);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     *添加流程
     * @param context
     */
    public void addDRRoute(Context context,String drId ,StringList mangerIds,StringList charMangerIds,String[] args) throws Exception {

        MapList approveList = new MapList();
        JF_Route_mxJPO jf_route = new JF_Route_mxJPO(context, args);
        String routeIntructions = EnoviaResourceBundle.getProperty(context, SUITE_KEY, context.getLocale(), "emxComponents.Widget.Tasks");
        //经理审批节点
        if(mangerIds.size()>0){
            String tileMess = EnoviaResourceBundle.getProperty(context, SUITE_KEY, context.getLocale(), "emxComponents.DRManger.ROUTE.MESS");
            for(String mangerId : mangerIds){
                Map nReceiverMapOne = (Map) jf_route.getMap(mangerId, tileMess);//设置审批信息 标题
                nReceiverMapOne.put("Route Instructions", routeIntructions);
                nReceiverMapOne.put(DomainObject.ATTRIBUTE_ROUTE_SEQUENCE, "1");
                approveList.add(nReceiverMapOne);
            }
        }
        //整椅经理审批节点
        String tileMess = EnoviaResourceBundle.getProperty(context, SUITE_KEY, context.getLocale(), "emxComponents.DRChairManger.ROUTE.MESS");
        for(String charMangerId : charMangerIds){
            Map nReceiverMapOne = (Map) jf_route.getMap(charMangerId, tileMess);//设置审批信息 标题
            nReceiverMapOne.put("Route Instructions", routeIntructions);
            if(mangerIds.size()>0) {
                nReceiverMapOne.put(DomainObject.ATTRIBUTE_ROUTE_SEQUENCE, "2");
            }
            approveList.add(nReceiverMapOne);
        }
        String state = STATE_JFDR_APPROVE;//在哪个状态增加流程
        String policy = POLICY_JFDR;//哪个Policy上面
        String routeDescription = EnoviaResourceBundle.getProperty(context, SUITE_KEY, context.getLocale(), "emxComponents.DR.ROUTE.MESS");; //流程描述
        String routeId = jf_route.createAndStartRoute(context, approveList, drId, state, policy, routeDescription);
//        String sRouteId = jf_route.createRoute(context,
//                approveList,
//                drId,
//                state,
//                policy,
//                routeDescription,
//                context.getUser(),
//                JF_PLMConstants_mxJPO.ATTRIBUTE_ROUTE_COMPLETION_ACTION_RANGE_PROMOTE_CONNECTED_OBJECT,
//                JF_PLMConstants_mxJPO.STRING_All);
        Route route = new Route();
        route.setId(routeId);
        route.setAttributeValue(context,ATTRIBUTE_DEMOTE_ON_REJECTION,"Yes");
    }
    /**
     * dr列表展示经理
     * @param context
     * @param args
     * @return
     * @throws Exception
     */
    public Vector<String> getManger(Context context,String[] args)throws Exception{
        Vector<String> result = new Vector();
        StringList strObjectIdList = JF_PublicMethodClass_mxJPO.getObjectIdList(args);
        for (String strObjectId : strObjectIdList) {
            DomainObject dr = DomainObject.newInstance(context,strObjectId);
            String lineMangerId = JF_PublicMethodClass_mxJPO.getPersonLineManager(context,null,dr.getInfo(context,"owner"));
            if(UIUtil.isNotNullAndNotEmpty(lineMangerId)){
                result.add(DomainObject.newInstance(context,lineMangerId).getInfo(context,"name"));
                continue;
            }
            result.add("");
        }
        return result;
    }

    /**
     * dr列表展示整椅子经理
     * @param context
     * @param args
     * @return
     * @throws Exception
     */
    public Vector<String> getChairManger(Context context,String[] args)throws Exception{
        Vector<String> result = new Vector();
        StringList strObjectIdList = JF_PublicMethodClass_mxJPO.getObjectIdList(args);
        JF_Util_mxJPO jfUtilMxJPO = new JF_Util_mxJPO();
        StringList bosel = jfUtilMxJPO.basicBolistSel();
        bosel.add("attribute[First Name]");
        bosel.add("attribute[Last Name]");
        for (String strObjectId : strObjectIdList) {
            StringBuffer stringBuffer = new StringBuffer();
            DomainObject dr = DomainObject.newInstance(context,strObjectId);
            MapList mangerList = dr.getRelatedObjects(context,RELATIONSHIP_JFDRCHAIRMANGER2PERSON,DomainConstants.TYPE_PERSON,bosel,null,
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
        DomainObject object = DomainObject.newInstance(context,objectId);
        String lineManagerId = JF_PublicMethodClass_mxJPO.getPersonLineManager(context,null,object.getInfo(context,"owner"));
        if(UIUtil.isNotNullAndNotEmpty(lineManagerId)){
            return DomainObject.newInstance(context,lineManagerId).getInfo(context,"name");
        }
        return "";
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
        String objectId = (String)requestMap.get("objectId");
        StringBuffer stringBuffer = new StringBuffer();
        if(UIUtil.isNotNullAndNotEmpty(objectId)) {
            JF_Util_mxJPO jfUtilMxJPO = new JF_Util_mxJPO();
            StringList bosel = jfUtilMxJPO.basicBolistSel();
            bosel.add("attribute[First Name]");
            bosel.add("attribute[Last Name]");
            DomainObject dr = DomainObject.newInstance(context, objectId);
            MapList mangerList = dr.getRelatedObjects(context, RELATIONSHIP_JFDRCHAIRMANGER2PERSON, DomainConstants.TYPE_PERSON, bosel, null,
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
     * @description: 获取所有的DR关联的数模对象
     * @param: context
	args
     * @return: com.matrixone.apps.domain.util.MapList
     * @author JJS
     * @date:  17:53
     */
    public MapList getAllVPM(Context context,String[] args)throws Exception{
        Map paramMap = (Map) JPO.unpackArgs(args);
        String objectId = (String)paramMap.get("objectId");
        DomainObject dr = DomainObject.newInstance(context, objectId);
        JF_Util_mxJPO jfUtilMxJPO = new JF_Util_mxJPO();
        StringList bosel = jfUtilMxJPO.basicBolistSel();
        StringList relsel = new StringList();
        relsel.add(DomainRelationship.SELECT_ID);
        MapList VPMList = dr.getRelatedObjects(context,RELATIONSHIP_JFDR2VPMREFERENCE,TYPE_VPMREFERENCE,bosel,relsel,
                false,true,(short) 1,
                "","",0);
        return VPMList;
    }

    /**
     * @description:  过滤已经关联了dr的物理产品
     * @param: context
    	args
     * @return: matrix.util.StringList
     * @author JJS
     * @date:  17:53
     */
    public StringList getExcludeVPMid(Context context,String[] args)throws Exception{
        StringList result = new StringList();
        Map paramMap = (Map) JPO.unpackArgs(args);
        String objectId = (String)paramMap.get("objectId");
        DomainObject dr = DomainObject.newInstance(context, objectId);
        StringList bosel = JF_Util_mxJPO.basicBolistSel();
        MapList VPMList = DomainObject.findObjects(context,TYPE_VPMREFERENCE,"*","to["+RELATIONSHIP_JFDR2VPMREFERENCE+"]==True",bosel);
//        MapList VPMList = dr.getRelatedObjects(context,RELATIONSHIP_JFDR2VPMREFERENCE,TYPE_VPMREFERENCE,bosel,null,
//                false,true,(short) 1,
//                "","",0);
        for(int i=0; i<VPMList.size(); i++){
            Map mangerMap = (Map) VPMList.get(i);
            String id = (String)mangerMap.get(DomainConstants.SELECT_ID);
            result.add(id);
        }
        return result;
    }
    /**
     * @description:  获取项目名称
     * @param: context
    	args
     * @return: java.util.Vector<java.lang.String>
     * @author JJS
     * @date:  17:52
     */
    public Vector<String> getProject(Context context,String[] args)throws Exception{
        Vector<String> result = new Vector();
        StringList strObjectIdList = JF_PublicMethodClass_mxJPO.getObjectIdList(args);
        StringList bosel = JF_Util_mxJPO.basicBolistSel();
        bosel.add("attribute[First Name]");
        bosel.add("attribute[Last Name]");
        for (String strObjectId : strObjectIdList) {
            StringBuffer stringBuffer = new StringBuffer();
            DomainObject dr = DomainObject.newInstance(context,strObjectId);
            MapList projectList = dr.getRelatedObjects(context,RELATIONSHIP_JFCHANGE2PROJECT,DomainConstants.TYPE_PROJECT_SPACE,bosel,null,
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

    /**
     * 获取项目描述
     * @param context
     * @param args
     * @return
     * @throws Exception
     */
    public Vector<String> getProjectDescription(Context context,String[] args)throws Exception{
        Vector<String> result = new Vector();
        StringList strObjectIdList = JF_PublicMethodClass_mxJPO.getObjectIdList(args);
        StringList bosel = JF_Util_mxJPO.basicBolistSel();
        bosel.add("description");
        ContextUtil.pushContext(context);
        try {
            for (String strObjectId : strObjectIdList) {
                String reStr = "";
                DomainObject dr = DomainObject.newInstance(context,strObjectId);
                MapList projectList = dr.getRelatedObjects(context,RELATIONSHIP_JFCHANGE2PROJECT,DomainConstants.TYPE_PROJECT_SPACE,bosel,null,
                        false,true,(short) 1,
                        "","",0);
                if(projectList.size()>0) {
                    Map projectMap = (Map) projectList.get(0);
                    reStr = (String) projectMap.get("description");
                }
                result.add(reStr);
            }
        }catch (Exception e){
            logger.info("getProjectDescription======error",e);
        }finally {
            ContextUtil.popContext(context);
        }
        return result;
    }
/**
*
*@description 获取项目
*@param: *@param context
 *@param args
*@return: java.lang.String
*@throws:
*@author: JJS
*@date: 2024/7/18 16:52
*/
    public  String getProjectForm(Context context, String[] args)throws Exception{
        Map paramMap = (Map) JPO.unpackArgs(args);
        Map requestMap = (Map) paramMap.get("requestMap");
        String objectId = (String)requestMap.get("objectId");
        StringList bosel = JF_Util_mxJPO.basicBolistSel();
        StringBuffer stringBuffer = new StringBuffer();
        DomainObject dr = DomainObject.newInstance(context,objectId);
        MapList projectList = dr.getRelatedObjects(context,RELATIONSHIP_JFCHANGE2PROJECT,DomainConstants.TYPE_PROJECT_SPACE,bosel,null,
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
    /** 
     * @description:  DR提升到审批中是开启流程，如果是回退的就重启流程
     * @param: context
    	args
     * @return: void 
     * @author JJS
     * @date:  17:51
     */
    public int startDRRoute(Context context,String[] args)throws Exception{
        String drId = args[0];
        DomainObject dr = DomainObject.newInstance(context,drId);
        //经理
        //StringList mangerIds = dr.getInfoList(context,"from["+RELATIONSHIP_JFDRMANGER2PERSON+"].to.id");
        String mangerId = JF_PublicMethodClass_mxJPO.getPersonLineManager(context,null,dr.getInfo(context,"owner"));
        //整椅经理
        String projectId = dr.getInfo(context,"from[JFChange2Project].to.id");
        DomainObject project = DomainObject.newInstance(context,projectId);
        StringList bosel = JF_Util_mxJPO.basicBolistSel();

        MapList peresonMapList = project.getRelatedObjects(context,"Member","Person",bosel,null,false,true,
                (short)1,"","attribute[Project Role]=='Chair manager'",0);
        if(peresonMapList.size()>0) {
            Map peresonMap = (Map) peresonMapList.get(0);
            String charMangerIds = (String) peresonMap.get("id");
            addDRRoute(context, drId, new StringList(mangerId), new StringList(charMangerIds), args);
            return 0;
        }else{
            logger.info("项目角色没有关联整椅经理");
            emxContextUtil_mxJPO.mqlNotice(context, "关联项目中项目角色没有分配整椅经理");
            return 1;
        }
    }
    /** 
     * @description: 检查物理产品的必填属性
     * @param: context
	args 
     * @return: java.lang.Integer 
     * @author JJS
     * @date:  13:21
     */
    public int checkAttribute(Context context,String[] args)throws Exception{
        int ischeck = 0;
        String drId = args[0];
        StringBuffer stringBuffer = new StringBuffer();
        String msg = EnoviaResourceBundle.getProperty(context, SUITE_KEY, context.getLocale(), "emxComponents.DRCheckVPM.ROUTE.MESS");
        DomainObject dr = DomainObject.newInstance(context,drId);
        String projectId = dr.getInfo(context, "from[" + RELATIONSHIP_JFCHANGE2PROJECT + "].to.id");
        StringList bosel = JF_Util_mxJPO.basicBolistSel();
        bosel.add("attribute[JF_VPMReference.JF_PartType]");  //零件类型
        bosel.add("attribute[JF_VPMReference.JF_PartNameCN]"); //中文名称
        bosel.add(SELECT_ATTR_JF_Detail_CN);  //directbuy
        // addby chenyan 新增校验采购类型不能为空
        bosel.add("attribute[JF_VPMReference.JF_ProcurementType]"); //采购类型
//        bosel.add("attribute[JF_VPMReference.JF_ColorPart]"); //颜色件
        bosel.add("attribute[EnterpriseExtension.V_PartNumber]"); //企业编码
        bosel.add(DomainConstants.KEY_LEVEL);
        bosel.add("to[JFProject2RootPart|attribute[JF_BelongPart]==Y]");   //项目
        bosel.add("to[Classified Item]");   //必须入库
        StringList relsel = new StringList();
        relsel.add("attribute[JF_VPMInstance.JF_Dosage]");   //数量

        StringList checkerrorList = new StringList(); //有问题的零件id
        HashSet checkPSList = new HashSet(); //无项目的零件id
        HashSet checkLibList = new HashSet(); //无入库的零件id
        HashSet checkStandardList = new HashSet(); //未发布的标准件
        HashSet checkDrwList = new HashSet(); //未关联的图纸
        MapList VPMList = dr.getRelatedObjects(context,RELATIONSHIP_JFDR2VPMREFERENCE+","+RELATIONSHIP_VPMINSTANCE,TYPE_VPMREFERENCE,bosel,relsel,
                false,true,(short) 0,
                "","",0);
        String strLanguage = context.getLocale().toString();
        logger.info("strLanguage:{}",strLanguage);
        JF_DR_mxJPO jfDrMxJPO = new JF_DR_mxJPO();
        for(int i=0; i<VPMList.size(); i++){
            Map mangerMap = (Map) VPMList.get(i);
            logger.info("mangerMap:{}",mangerMap);
            String name = (String)mangerMap.get("name");
            String id = (String)mangerMap.get("id");
            String revision = (String)mangerMap.get("revision");
            String chineseName = (String) mangerMap.get("attribute[JF_VPMReference.JF_PartNameCN]");
            String parttype = (String) mangerMap.get("attribute[JF_VPMReference.JF_PartType]");
            String detail_CN = (String) mangerMap.get(SELECT_ATTR_JF_Detail_CN);
            String procurementType = (String) mangerMap.get("attribute[JF_VPMReference.JF_ProcurementType]");
//            String colorPart = (String) mangerMap.get("attribute[JF_VPMReference.JF_ColorPart]");
            String dosage = (String)  mangerMap.get("attribute[JF_VPMInstance.JF_Dosage]");
            String partNumber = (String) mangerMap.get("attribute[EnterpriseExtension.V_PartNumber]");
//            String partNumber = (String) mangerMap.get("attribute[EnterpriseExtension.V_PartNumber]")+"_"+revision;
            if (UIUtil.isNotNullAndNotEmpty(partNumber)) {
                partNumber = partNumber+"_"+revision;
            } else {
                partNumber = name+"_"+revision;
            }
            String hasProjectSpace = (String) mangerMap.get("to[JFProject2RootPart]"); //关联项目
            String hasProjectSpace2 = (String) mangerMap.get("to[JFProject2RootPart|attribute[JF_BelongPart]==Y]"); //关联项目
            if(UIUtil.isNullOrEmpty(hasProjectSpace)){
                hasProjectSpace = hasProjectSpace2;
            }
            String hasLib = (String) mangerMap.get("to[Classified Item]"); //入库
            String current = (String) mangerMap.get(DomainConstants.SELECT_CURRENT);
            logger.info("procurementType:{}",procurementType);
            String level = (String) mangerMap.get(DomainConstants.KEY_LEVEL);
            if("1".equals(level)){
                dosage = "1";
            }
            if(checkerrorList.contains(id)){
                continue;
            }
            //add by caipan 零件必填校验 20250903
            StringList nullList = new StringList();
            if(UIUtil.isNotNullAndNotEmpty(detail_CN)) {
                 nullList = getRequireAttribute(context, id, detail_CN, checkerrorList);
                //校验客户零件号/DB信息属性  工作中的数据
                if ("IN_WORK".equalsIgnoreCase(current)) {
                    jfDrMxJPO.checkDRCustomerPartsDBRequireAttribute(context, id, detail_CN, nullList, projectId);
                }
            }else{
                nullList.add("JF_VPMReference.JF_Detail_CN");
            }
            StringList requireAttrList  = new StringList();
            //图纸必填，但是该数据没有关联图纸，增加报错信息 特殊处理
          /*  if(nullList.contains("connectdrw")){
//                    requireAttrList.add(EnoviaResourceBundle.getProperty(context, "emxFrameworkStringResource", context.getLocale(), "emxFramework.connect.DRW"));
                requireAttrList.add(EnoviaResourceBundle.getProperty(context, "emxFrameworkStringResource", context.getLocale(), "emxFramework.Type.ENOSTDIFSheetDS"));
                nullList.remove("connectdrw");
            }*/
            if(nullList.contains("connectdrw")){
                checkDrwList.add(partNumber);
                nullList.remove("connectdrw");
            }
            for(int j=0;j<nullList.size();j++){
                if(j==0) {
                    if (UIUtil.isNotNullAndNotEmpty(partNumber)) {
                        stringBuffer.append(partNumber);
                    } else {
                        stringBuffer.append(name);
                    }
                }
                requireAttrList.add(EnoviaResourceBundle.getAttributeI18NString(context,nullList.get(j),strLanguage));

            }
            if(requireAttrList.size()>0) {
                //未填写属性
                String strNotWriteAttr = msg.replace("{}", requireAttrList.join(","));
                stringBuffer.append(strNotWriteAttr);
                if (i != VPMList.size() - 1) {
                    stringBuffer.append("\n");
                }
            }
           /* if(UIUtil.isNullOrEmpty(chineseName)||UIUtil.isNullOrEmpty(parttype)||
            UIUtil.isNullOrEmpty(directBuy)||UIUtil.isNullOrEmpty(dosage)
            ||UIUtil.isNullOrEmpty(partNumber) || UIUtil.isNullOrEmpty(procurementType)){
                //||UIUtil.isNullOrEmpty(colorPart)
                if(!checkerrorList.contains(id)){
                    // add by chenyan 提示信息添加
                    if (UIUtil.isNotNullAndNotEmpty(partNumber)){
                        stringBuffer.append(partNumber);
                    }else{
                        stringBuffer.append(name);
                    }
//                    stringBuffer.append(partNumber).append("-").append(revision);
                    StringList requireAttrList = new StringList();
                    if (UIUtil.isNullOrEmpty(chineseName)){
                        requireAttrList.add(EnoviaResourceBundle.getAttributeI18NString(context,"JF_VPMReference.JF_PartNameCN",strLanguage));
                    }
                    if (UIUtil.isNullOrEmpty(parttype)){
                        requireAttrList.add(EnoviaResourceBundle.getAttributeI18NString(context,"JF_VPMReference.JF_PartType",strLanguage));
                    }
                    if (UIUtil.isNullOrEmpty(directBuy)){
                        requireAttrList.add(EnoviaResourceBundle.getAttributeI18NString(context,"JF_VPMReference.JF_DirectBuy",strLanguage));
                    }
//                    if (UIUtil.isNullOrEmpty(colorPart)){
//                        requireAttrList.add(EnoviaResourceBundle.getAttributeI18NString(context,"JF_VPMReference.JF_ColorPart",strLanguage));
//                    }
                    if (UIUtil.isNullOrEmpty(partNumber)){
                        requireAttrList.add(EnoviaResourceBundle.getAttributeI18NString(context,"EnterpriseExtension.V_PartNumber",strLanguage));
                    }
                    if (UIUtil.isNullOrEmpty(procurementType)){
                        requireAttrList.add(EnoviaResourceBundle.getAttributeI18NString(context,"JF_VPMReference.JF_ProcurementType",strLanguage));
                    }
                    if (UIUtil.isNullOrEmpty(dosage)){
                        requireAttrList.add(EnoviaResourceBundle.getAttributeI18NString(context,"JF_VPMReference.JF_Dosage",strLanguage));
                    }
                    logger.info("requireAttrList:{}",requireAttrList);
                    //未填写属性
                    String strNotWriteAttr = msg.replace("{}",requireAttrList.join(","));
                    stringBuffer.append(strNotWriteAttr);
                    if(i!=VPMList.size()-1){
                        stringBuffer.append("\n");
                    }
                    checkerrorList.add(id);
                }
            }*/
            checkerrorList.add(id);
            //add ljr
            if ("FALSE".equalsIgnoreCase(hasProjectSpace)) {
                //无项目
                checkPSList.add(partNumber);
            }
            if ("FALSE".equalsIgnoreCase(hasLib)) {
                //无库
                checkLibList.add(partNumber);
            }

            if (!JF_FasteningPiece_mxJPO.checkStandardPartReleased(context, id)) {
                checkStandardList.add(partNumber);
            }

        }
        if (!checkPSList.isEmpty()) {
            //无项目的情况
            String mess = EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", context.getLocale(), "emxComponent.Common.VPMNoPS");
            stringBuffer.append("\n");
            stringBuffer.append(mess);
            stringBuffer.append("\n");
            stringBuffer.append(StringList.create(checkPSList).join(","));
        }
        if (!checkLibList.isEmpty()) {
            //没有入库
            String mess = EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", context.getLocale(), "emxComponent.Common.VPMNoLib");
            stringBuffer.append("\n");
            stringBuffer.append(mess);
            stringBuffer.append("\n");
            stringBuffer.append(StringList.create(checkLibList).join(","));
        }
        if (!checkStandardList.isEmpty()) {
            //标准件没有发布  add by caipan
            String mess = EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", context.getLocale(), "emxComponent.Message.NoReleaseStandard");
            stringBuffer.append("\n");
            stringBuffer.append(mess);
            stringBuffer.append("\n");
            stringBuffer.append(StringList.create(checkStandardList).join(","));
        }

        if (!checkDrwList.isEmpty()) {
            //无图纸
            String mess = EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", context.getLocale(), "emxComponents.DRCheckVPM.DRW.MESS");
            stringBuffer.append("\n");
            stringBuffer.append(mess);
            stringBuffer.append("\n");
            stringBuffer.append(StringList.create(checkDrwList).join(","));
        }
        //end
        if(stringBuffer.length()>0){
//            stringBuffer.append(msg);
            ischeck = 1;
            emxContextUtil_mxJPO.mqlNotice(context, stringBuffer.toString());
        }
        return ischeck;
    }
    /**
     * JFDR审核完成后将关联物理产品冻结
     **
     * @param context 上下文
     * @param args JFDR对象ID
     * @return int 执行成功返回0
     * @throws Exception 冻结关联数据失败时抛出异常
     * @author caipan by codex
     * @date 2026/8/19
     */
    public int updateVPM2Freeze(Context context,String[] args)throws Exception{
        ContextUtil.pushContext(context);
        try {
            String objectId = args[0];
            DomainObject dr = DomainObject.newInstance(context, objectId);
            //20260819 update by codex JFDR未完成时禁止冻结关联零件
            String drCurrent = dr.getInfo(context, DomainConstants.SELECT_CURRENT);
            if (!"Complete".equals(drCurrent)) {
                logger.warn("skip updateVPM2Freeze because JFDR is not Complete, objectId:{}, current:{}", objectId, drCurrent);
                return 0;
            }
            JF_Util_mxJPO jfUtilMxJPO = new JF_Util_mxJPO();
            StringList bosel = JF_Util_mxJPO.basicBolistSel();
            bosel.add("to["+RELATIONSHIP_XCADBASEDEPENDENCY+"|from.type==Drawing].from.id");
            StringList vpmIds = new StringList();
            StringList privateIds = new StringList();
            StringList relsel = new StringList();
            relsel.add("attribute["+ATTRIBUTE_JFISFOLLOW+"]");
            String relationship = RELATIONSHIP_VPMINSTANCE;
            String where = "current==IN_WORK";
            MapList VPMList = dr.getRelatedObjects(context, RELATIONSHIP_JFDR2VPMREFERENCE, "*", bosel, relsel,
                    false, true, (short) 1,
                    where, "", 0);
            for(int i=0; i<VPMList.size(); i++){
                Map mangerMap = (Map) VPMList.get(i);
                String vpmId = (String) mangerMap.get(DomainConstants.SELECT_ID);
                Object obj=mangerMap.get("to["+RELATIONSHIP_XCADBASEDEPENDENCY+"].from.id");
                StringList drwList = new StringList();
                if(obj instanceof String){
                    if(UIUtil.isNotNullAndNotEmpty(obj.toString())) {
                        drwList.add(obj.toString());
                    }
                }else   if(obj instanceof StringList){
                    drwList.addAll((StringList)obj);
                }
//                String drawingId = (String)mangerMap.get("to["+RELATIONSHIP_XCADBASEDEPENDENCY+"|from.type==Drawing].from.id");
                String JFIsFollow = (String) mangerMap.get("attribute["+ATTRIBUTE_JFISFOLLOW+"]");
                DomainObject vpm = DomainObject.newInstance(context, vpmId);
//                relationship = RELATIONSHIP_VPMINSTANCE;
//                if("Y".equals(JFIsFollow)){
//                    relationship = RELATIONSHIP_VPMINSTANCE + "," + RELATIONSHIP_VPMREPINSTANCE;
//                }
                MapList childVPMList = vpm.getRelatedObjects(context, relationship, "*", bosel, null,
                        false, true, (short) 0,
                        "", "", 0);
                vpmIds.add(vpmId);
                logger.info("drwList:{}",drwList);
                logger.info("JFIsFollow:{}",JFIsFollow);
                if("Y".equalsIgnoreCase(JFIsFollow)&&drwList.size()>0) {
                    vpmIds.addAll(filterInWorkData(context,drwList));
                }
                //文档类型的图纸
                StringList documentDrwList = new StringList();
                String documentDrw = MqlUtil.mqlCommand(context, false, "pri bus '" + vpmId + "' select from[Reference Document|to.attribute[JF_DocumentType].value==Drawing].to.id dump", true);
                if (UIUtil.isNotNullAndNotEmpty(documentDrw)){
                    documentDrwList.addAll(Arrays.asList(documentDrw.split(",")));
                }
                logger.info("documentDrwList:{}",documentDrwList);
                if("Y".equalsIgnoreCase(JFIsFollow)&&documentDrwList.size()>0) {
                    vpmIds.addAll(filterInWorkData(context,documentDrwList));
                }
                for (int j = 0; j < childVPMList.size(); j++) {
                    Map childmangerMap = (Map) childVPMList.get(j);
                    String id = (String) childmangerMap.get(DomainConstants.SELECT_ID);
                    String current = (String)childmangerMap.get(DomainConstants.SELECT_CURRENT);
//                    String childdrawingId = (String)childmangerMap.get("to["+RELATIONSHIP_XCADBASEDEPENDENCY+"|from.type==Drawing].from.id");
                    Object childObj = childmangerMap.get("to["+RELATIONSHIP_XCADBASEDEPENDENCY+"].from.id");

                    //add by caipan 数模和图纸一对多
                    StringList drwListchild = new StringList();
                    if(childObj instanceof String){
                        if(UIUtil.isNotNullAndNotEmpty(childObj.toString())) {
                            drwListchild.add(childObj.toString());
                        }
                    }else   if(childObj instanceof StringList){
                        drwListchild.addAll((StringList)childObj);
                    }
                    logger.info("drwListchild:{}",drwListchild);
                    //文档类型的图纸
                    StringList documentDrwChildList = new StringList();
                    String documentDrwChild = MqlUtil.mqlCommand(context, false, "pri bus '" + id + "' select from[Reference Document|to.attribute[JF_DocumentType].value==Drawing].to.id dump", true);
                    if (UIUtil.isNotNullAndNotEmpty(documentDrwChild)){
                        documentDrwChildList.addAll(Arrays.asList(documentDrwChild.split(",")));
                    }
                    logger.info("documentDrwChildList:{}",documentDrwChildList);
                    if (!vpmIds.contains(id)&&"IN_WORK".equals(current)) {
                        vpmIds.add(id);
                        if("Y".equalsIgnoreCase(JFIsFollow)&&drwListchild.size()>0){
                            //图纸状态 工作中的才加入
                            vpmIds.addAll(filterInWorkData(context,drwListchild));
                        }
                        if("Y".equalsIgnoreCase(JFIsFollow)&&documentDrwChildList.size()>0) {
                            vpmIds.addAll(filterInWorkData(context,documentDrwChildList));
                        }
                    }
                    //还需要处理私有状态的数模
                    if(!privateIds.contains(id)&&"PRIVATE".equals(current)) {
                        privateIds.add(id);//先提升到工作中
                        vpmIds.add(id);//在提升到冻结
                    }
                }
            }
            logger.info(String.valueOf(vpmIds));
            //先提升到工作中
            logger.info("privateIds:{}",privateIds);
            if(privateIds.size()>0){
                new JF_Util_mxJPO().promoteVPM(context, privateIds, "ShareWithinProject");
            }
            //20260819 update by codex 冻结逻辑改由JFDR Approve提升成功后的action触发
            jfUtilMxJPO.promoteVPM(context, vpmIds, "ToFreeze");
            //给冻结的数据增加JF_VPMReference.JF_ConnectDR属性，如果是当前DR冻结的数据，必须是这个 add by caipan
            setJF_ConnectDR(context,vpmIds,objectId);
        }catch (Exception e){
            logger.error("updateVPM2Freeze error, objectId:{}", args[0], e);
            throw e;
        }finally {
            ContextUtil.popContext(context);
        }
        return 0;
    }
    public void test(Context context,String[] args)throws Exception{
        ContextUtil.pushContext(context);
        DomainObject dr = DomainObject.newInstance(context, args[0]);
        promoteDRDocuemnt(context,dr);
        ContextUtil.popContext(context);
    }
    /** 
     * @description: 提升dr附件状态到发布状态
     * @param: context
	dr 
     * @return: void 
     * @author JJS
     * @date:  11:24
     */
    public void promoteDRDocuemnt(Context context,DomainObject dr)throws Exception{
        StringList boSel = JF_Util_mxJPO.basicBolistSel();
        MapList maps = dr.getRelatedObjects(context, RELATIONSHIP_REFERENCE_DOCUMENT, // relationship pattern
                TYPE_DOCUMENT,                                    // object pattern
                boSel,                            // object selects
                null, // relationship selects
                false,                                        // to direction
                true,                                        // from direction
                (short) 1,                                    // recursion level
                "current!=RELEASED",                // object where clause
                "",
                (short) 0);
        for(int i=0; i<maps.size(); i++){
            Map docMap = (Map) maps.get(i);
            String id = (String) docMap.get(DomainConstants.SELECT_ID);
            DomainObject doc = DomainObject.newInstance(context, id);
            doc.setState(context,"RELEASED");
        }
    }
    /** 
     * @description: 张开dr关联零件的子件 
     * @param: context
        args
     * @return: com.matrixone.apps.domain.util.MapList 
     * @author JJS
     * @date:  17:46
     */
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
            StringList relSel = new StringList();
            relSel.add(DomainRelationship.SELECT_ID);
            childPartList =  obj.getRelatedObjects(context,RELATIONSHIP_VPMINSTANCE,TYPE_VPMREFERENCE,
                    boSel,relSel,false,true,Short.parseShort(expandLevel),"","",0);
//            for(int i=0;i<childPartList.size();i++){
//                Map mangerMap = (Map) childPartList.get(i);
//                mangerMap.put("disableSelection","true");
//            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return childPartList;
    }
    /** 
     * @description: dr表单工作中可修改属性 
     * @param: context
	args 
     * @return: boolean
     * @author JJS
     * @date:  11:09
     */
    public boolean isEditable(Context context,String[] args)throws Exception{
        Map paramMap = (Map) JPO.unpackArgs(args);
        Map requestMap = (Map) paramMap.get("requestMap");
        String objectId = (String)requestMap.get("objectId");
        DomainObject dr = DomainObject.newInstance(context, objectId);
        String current = dr.getInfo(context,"current");
        return "In_Work".equals(current)?true:false;
    }

    /**
     * @description:  dr表格工作中可修改属性
     * @param: context
    	args
     * @return: boolean
     * @author JJS
     * @date:  11:15
     */
    public StringList isEditableTable(Context context,String[] args)throws Exception{
        StringList result = new StringList();
        StringList strObjectIdList = JF_PublicMethodClass_mxJPO.getObjectIdList(args);
        for (String objectId : strObjectIdList) {
            DomainObject dr = DomainObject.newInstance(context, objectId);
            String current = dr.getInfo(context,"current");
            result.add("In_Work".equals(current)?"true":"false");
        }

        return result;
    }
    /** 
     * @description: 更新整椅经理 
     * @param: context
	args 
     * @return: void 
     * @author JJS
     * @date:  14:04
     */
    public void updateManger(Context context ,String[] args)throws Exception{
        Map map = (Map)JPO.unpackArgs(args);
        Map paramMap = (Map)map.get("paramMap");
        String newId = (String)paramMap.get("New OID");
        String newName = (String)paramMap.get("New Value");
        String oldName = (String)paramMap.get("Old Value");
        String objectId = (String)paramMap.get("objectId");
        DomainObject dr = DomainObject.newInstance(context, objectId);
        String rel = dr.getInfo(context,"from["+RELATIONSHIP_JFDRMANGER2PERSON+"].id");
        if(!newName.equals(oldName)){
            if(UIUtil.isNotNullAndNotEmpty(rel)){
                DomainRelationship.disconnect(context,rel);
            }
            if(!"".equals(newId)){
                dr.addToObject(context,new RelationshipType(RELATIONSHIP_JFDRMANGER2PERSON),newId);
            }
        }
    }
    /**
     * @description: 创建dr并关联零件
     * @param: context
	args
     * @return: java.util.Map<java.lang.String,java.lang.String>
     * @author JJS
     * @date:  9:58
     */
    public Map<String,String> createDR(Context context, String[] args)throws Exception{
        logger.info("createDR>>>>>");
        Map<String,String> result = new HashMap<>();
        try {
            Map paramMap = (Map) JPO.unpackArgs(args);
            String emxTableRowIds = (String) paramMap.get("emxTableRowIds");
            String connectIds = (String) paramMap.get("connectIds");
            logger.info("connectIds>>>>>" + connectIds);
            String drId = FrameworkUtil.autoName(context, "type_JFDR", "");
            logger.info("drId>>>>>" + drId);
            DomainObject obj = DomainObject.newInstance(context, drId);
            StringList connectIdList = FrameworkUtil.split(connectIds, ",");
            for (String connectId : connectIdList) {
                if(UIUtil.isNotNullAndNotEmpty(connectId)) {
                    logger.info("connectId>>>>>"+connectId);
                    DomainRelationship connection = obj.addToObject(context, new RelationshipType(RELATIONSHIP_JFDR2VPMREFERENCE), connectId);
                    connection.setAttributeValue(context,ATTRIBUTE_JFISFOLLOW,"Y");
                }
            }
            result.put("id", drId);
        }catch (Exception e){
            e.printStackTrace();
        }
        return result;
    }
    /**
     * @description: 修改项目
     * @param: context
	args
     * @return: void
     * @author JJS
     * @date:  15:48
     */
    public void updateProject(Context context,String[] args)throws Exception{
        Map map = JPO.unpackArgs(args);
        Map paramMap = (Map) map.get("paramMap");
        logger.info("paramMap>>>>>"+paramMap);
        String newId = (String)paramMap.get("New OID");
        String oldId = (String)paramMap.get("Old OID");
        String objectId = (String)paramMap.get("objectId");
        logger.info("objectId>>>>>"+objectId);
        DomainObject dr = DomainObject.newInstance(context, objectId);
        Map relMap = new HashMap<>();
        relMap.put("relName", RELATIONSHIP_JFCHANGE2PROJECT);
        relMap.put("fromId", objectId);
        relMap.put("toId", oldId);
        if(newId.equals(oldId))
            return;
        String relId = JF_PublicMethodClass_mxJPO.getTwoBusinessObjectConnId(context, JPO.packArgs(relMap));
        if(UIUtil.isNotNullAndNotEmpty(relId)){
            DomainRelationship.disconnect(context,relId);
        }
        if(UIUtil.isNotNullAndNotEmpty(newId)) {
            dr.addToObject(context, new RelationshipType(RELATIONSHIP_JFCHANGE2PROJECT), newId);
        }


        //修改整椅经理
//        String chairMangerRelId = dr.getInfo(context,"from["+RELATIONSHIP_JFDRCHAIRMANGER2PERSON+"].id");
//        if (UIUtil.isNotNullAndNotEmpty(chairMangerRelId)){
//            DomainRelationship.disconnect(context,chairMangerRelId);
//        }
//        if (UIUtil.isNotNullAndNotEmpty(newId)){
//            DomainObject project = DomainObject.newInstance(context, newId);
//            StringList boSel = JF_Util_mxJPO.basicBolistSel();
//            StringList relSel = JF_Util_mxJPO.basicRellistSel();
//            String where = "attribute[Project Role]=='Chair manager'";
//            MapList personMapList = project.getRelatedObjects(context, DomainConstants.RELATIONSHIP_MEMBER, DomainConstants.TYPE_PERSON, boSel, relSel,
//                    false, true, (short) 1, DomainConstants.EMPTY_STRING, where, 0);
//            if (personMapList.size() > 0) {
//                Map personMap = (Map) personMapList.get(0);
//                String chairMangerOID = (String) personMap.get(DomainConstants.SELECT_ID);
//                if (UIUtil.isNotNullAndNotEmpty(chairMangerOID)) {
//                    dr.addToObject(context, new RelationshipType(RELATIONSHIP_JFDRCHAIRMANGER2PERSON), chairMangerOID);
//                }
//            }
//        }
    }
/** 
 * @description: 校验dr是否关联附件
 * @param: context
	args 
 * @return: int 
 * @author JJS
 * @date:  11:18
 */
    public int checkDocument(Context context,String[] args)throws Exception{
        logger.info("checkDocument>>>>>"+args[0]);
        String objectId = (String)args[0];
        DomainObject obj = DomainObject.newInstance(context, objectId);
        StringList boSel = JF_Util_mxJPO.basicBolistSel();
        MapList maps = obj.getRelatedObjects(context, RELATIONSHIP_REFERENCE_DOCUMENT, // relationship pattern
                TYPE_DOCUMENT,                                    // object pattern
                boSel,                            // object selects
                null, // relationship selects
                false,                                        // to direction
                true,                                        // from direction
                (short) 1,                                    // recursion level
                "",                // object where clause
                "",
                (short) 0);
        if(maps.isEmpty()){
            String strMess = EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", context.getLocale(), "emxComponents.Document.unupload");
            emxContextUtil_mxJPO.mqlNotice(context,strMess.replace("{}",obj.getInfo(context,"name")));
            return 1;
        }
        return 0;
    }
    /*
     * @description: DR提交到审核的时候，增加Change Control 来控制编辑权限
     * @author: caipan
     * @date:
     * @param: * @param[1] context
     * @param[2] args
     * @return:
     **/
    public void addChangeInterface(Context context,String[] args) throws  Exception{
        logger.info("addChangeInterface start");
        String drId = args[0];
        DomainObject dr = DomainObject.newInstance(context,drId);
        StringList bosel = JF_Util_mxJPO.basicBolistSel();
        StringList relsel =JF_Util_mxJPO.basicRellistSel();
        String where = "current==IN_WORK";
        MapList VPMList = dr.getRelatedObjects(context,RELATIONSHIP_JFDR2VPMREFERENCE+","+RELATIONSHIP_VPMINSTANCE,TYPE_VPMREFERENCE,bosel,relsel,
                false,true,(short) 0,
                where,"",0);
        DomainObject vpmObj= DomainObject.newInstance(context);
        for(int i=0; i<VPMList.size(); i++){
            Map mangerMap = (Map) VPMList.get(i);
            String id = UIUtil.getValue(mangerMap, DomainConstants.SELECT_ID);
            vpmObj.setId(id);
            StringList list = vpmObj.getInfoList(context, "interface");
            if(!list.contains("Change Control")) {
                MqlUtil.mqlCommand(context, false, "mod bus '" + id + "' add interface 'Change Control' ", true);
            }
            MqlUtil.mqlCommand(context, false, "mod bus '" + id + "' "+ATTRIBUTE_ConnectDR+" '"+drId+"'", true);
        }
        logger.info("addChangeInterface end");

    }

    /*
     * @description:DR审核完成的时候，移除掉Change Control 来释放升版权限
     * @author: caipan
     * @date:
     * @param: * @param[1] context
     * @param[2] args
     * @return:
     **/
    public void removeChangeInterface(Context context,String[] args) throws  Exception{
        logger.info("removeChangeInterface start");
        String drId = args[0];
        DomainObject dr = DomainObject.newInstance(context,drId);
        StringList bosel = JF_Util_mxJPO.basicBolistSel();
        StringList relsel =JF_Util_mxJPO.basicRellistSel();
        String where = "current==FROZEN";
        MapList VPMList = dr.getRelatedObjects(context,RELATIONSHIP_JFDR2VPMREFERENCE+","+RELATIONSHIP_VPMINSTANCE,TYPE_VPMREFERENCE,bosel,relsel,
                false,true,(short) 0,
                where,"",0);
        DomainObject vpmObj= DomainObject.newInstance(context);
        for(int i=0; i<VPMList.size(); i++){
            Map mangerMap = (Map) VPMList.get(i);
            String id = UIUtil.getValue(mangerMap, DomainConstants.SELECT_ID);
            vpmObj.setId(id);
            StringList list = vpmObj.getInfoList(context, "interface");
            if(list.contains("Change Control")) {
                MqlUtil.mqlCommand(context, false, "mod bus '" + id + "' remove interface 'Change Control' ", true);
            }
        }
        logger.info("removeChangeInterface end");
    }

    /*
     * @description: DR移除数据的时候，去掉Change Control---只要在审核拒绝才会有这种情况
     * @author: caipan
     * @date:
     * @param: * @param[1] context
     * @param[2] args
     * @return:
     **/
    public void drDisVpmRemoveChangeControl(Context context,String[] args) throws Exception {
        StringList vpmList = new StringList();
        for (String tableRowId : args) {
            StringList tableIds = FrameworkUtil.split(tableRowId, "|");
            if (tableIds.size() > 1) {
                vpmList.add(tableIds.get(1));
            }
        }
        for (int j = 0; j < vpmList.size(); j++) {
            String vpmId = vpmList.get(j);
            DomainObject vpmObj = DomainObject.newInstance(context);
            vpmObj.setId(vpmId);
            String current = vpmObj.getInfo(context,DomainConstants.SELECT_CURRENT);
            StringList bosel = JF_Util_mxJPO.basicBolistSel();
            StringList relsel = JF_Util_mxJPO.basicRellistSel();
            String where = "current==IN_WORK";
            MapList VPMList = vpmObj.getRelatedObjects(context, RELATIONSHIP_VPMINSTANCE, TYPE_VPMREFERENCE, bosel, relsel,
                    false, true, (short) 0,
                    where, "", 0);
            for (int i = 0; i < VPMList.size(); i++) {
                Map mangerMap = (Map) VPMList.get(i);
                String id = UIUtil.getValue(mangerMap, DomainConstants.SELECT_ID);
                vpmObj.setId(id);
                StringList list = vpmObj.getInfoList(context, "interface");
                if (list.contains("Change Control")) {
                    MqlUtil.mqlCommand(context, false, "mod bus '" + id + "' remove interface 'Change Control' ", true);
                }
                MqlUtil.mqlCommand(context, false, "mod bus '" + id + "' "+ATTRIBUTE_ConnectDR+" ''", true);
            }
            if("IN_WORK".equalsIgnoreCase(current)) {
                MqlUtil.mqlCommand(context, false, "mod bus '" + vpmId + "' " + ATTRIBUTE_ConnectDR + " ''", true);
            }
        }
    }

    /*
     * @description: DR流程拒绝的时候，去掉Change Control---只要在审核拒绝才会有这种情况
     * @author: caipan
     * @date:
     * @param: * @param[1] context
     * @param[2] args
     * @return:
     **/
    public void drRefuseRemoveChangeControl(Context context,String[] args) throws Exception {
        StringList vpmList = new StringList();
        String drId = args[0];
        DomainObject drObj = DomainObject.newInstance(context);
        drObj.setId(drId);
        vpmList = drObj.getInfoList(context, "from[JFDR2VPMReference].to.id");
        logger.info("vpmList:{}",vpmList);
        for (int j = 0; j < vpmList.size(); j++) {
            String vpmId = vpmList.get(j);
            DomainObject vpmObj = DomainObject.newInstance(context);
            vpmObj.setId(vpmId);
            StringList bosel = JF_Util_mxJPO.basicBolistSel();
            StringList relsel = JF_Util_mxJPO.basicRellistSel();
            String where = "current==IN_WORK";
            MapList VPMList = vpmObj.getRelatedObjects(context, RELATIONSHIP_VPMINSTANCE, TYPE_VPMREFERENCE, bosel, relsel,
                    false, true, (short) 0,
                    where, "", 0);
            logger.info("VPMList:{}",VPMList);
            for (int i = 0; i < VPMList.size(); i++) {
                Map mangerMap = (Map) VPMList.get(i);
                String id = UIUtil.getValue(mangerMap, DomainConstants.SELECT_ID);
                vpmObj.setId(id);
                StringList list = vpmObj.getInfoList(context, "interface");
                if (list.contains("Change Control")) {
                    MqlUtil.mqlCommand(context, false, "mod bus '" + id + "' remove interface 'Change Control' ", true);
                }
            }
            //父也需要去掉Change Control
            vpmObj.setId(vpmId);
            StringList list = vpmObj.getInfoList(context, "interface");
            if (list.contains("Change Control")) {
                MqlUtil.mqlCommand(context, false, "mod bus '" + vpmId + "' remove interface 'Change Control' ", true);
            }

        }
    }
  /*
   * @description:保留工作中的数据
   * @author: caipan
   * @date: 2025/1/3 14:32:39
   * @param: * @param[1] context
   * @param[2] list
   * @return: 
   **/
    public StringList filterInWorkData(Context context,StringList list ) throws Exception {
        MapList results = DomainObject.getInfo(context, list.toStringArray(), JF_Util_mxJPO.basicBolistSel());
        StringList resultList = new StringList();
        String current = "";
        String id = "";
        Map map=null;
        for(int i=0;i<results.size();i++){
            map = (Map)results.get(i);
            current= UIUtil.getValue(map, DomainObject.SELECT_CURRENT);
            id= UIUtil.getValue(map, DomainObject.SELECT_ID);
            if("IN_WORK".equalsIgnoreCase(current)){
                resultList.add(id);
            }else if("PRIVATE".equalsIgnoreCase(current)){
                //提升生命周期到工作中
                new JF_Util_mxJPO().promoteVPM(context, new StringList(id), "ShareWithinProject");
                resultList.add(id);
            }
        }
        return resultList;
    }


    /**
     * 发DR的时候校验零件是否是最新版本
     * @param context
     * @param args
     * @return
     * @throws Exception
     */
    public int checkVPMReferenceStateIsLastRevision(Context context,String[] args)throws Exception {
        int ischeck = 0;
        String drId = args[0];
        StringBuilder stringBuffer = new StringBuilder();
        DomainObject dr = DomainObject.newInstance(context,drId);
        StringList bosel = JF_Util_mxJPO.basicBolistSel();
        bosel.add("attribute[PLMReference.V_isLastVersion]");
        bosel.add("attribute[EnterpriseExtension.V_PartNumber]"); //企业编码
        StringList relsel = new StringList();
        MapList VPMList = dr.getRelatedObjects(context,RELATIONSHIP_JFDR2VPMREFERENCE+","+RELATIONSHIP_VPMINSTANCE,TYPE_VPMREFERENCE,bosel,relsel,
                false,true,(short) 0,
                "","",0);
        String strFROZENError = ComponentsUIUtil.getI18NString(context, context.getLocale().toString(), "emxComponents.DRMess.FROZENError", new String[]{});
        String strFROZENHasReleaseError = ComponentsUIUtil.getI18NString(context, context.getLocale().toString(), "emxComponents.DRMess.FROZENHasReleaseError", new String[]{});
        for (Object o : VPMList) {
            Map map = (Map) o;
            String isLast = UIUtil.getValue(map, "attribute[PLMReference.V_isLastVersion]");
            String V_PartNumber = UIUtil.getValue(map, "attribute[EnterpriseExtension.V_PartNumber]");
            String current = UIUtil.getValue(map, DomainConstants.SELECT_CURRENT);
            String id = UIUtil.getValue(map, DomainConstants.SELECT_ID);
            String name = UIUtil.getValue(map, "name");
            if(UIUtil.isNullOrEmpty(V_PartNumber)){
                V_PartNumber = name;
            }
            String revision = UIUtil.getValue(map, "revision");
            String msg = V_PartNumber + "_" + revision;
            if (current.equalsIgnoreCase("IN_WORK")) {
                if (!"TRUE".equals(isLast)) {
                    stringBuffer.append(msg).append("\u4e0d\u662f\u6700\u65b0\u7248\u672c,\u4e0d\u5141\u8bb8\u63d0\u5347").append("\n");
                }
            }else if (current.equalsIgnoreCase("FROZEN")) {
                //update by ljr DR校验规则中【数据结构中处在“冻结”的数据必须是最新修订版本】改为【数据结构中处在“冻结”的数据必须是最新冻结版本】，且没有更高的发布版本，需要同步更新手册中对应的规则说明
                int flag = JF_PublicMethodClass_mxJPO.getLatestFrozenAndHasHigherReleaseVersion(context, name, id, revision);
                if (flag == 1) {
                    stringBuffer.append(msg).append(strFROZENError).append("\n");
                } else if (flag == 2){
                    //相同则判断他是否有更高的发布版本
                    stringBuffer.append(msg).append(strFROZENHasReleaseError).append("\n");
                }
            }else if(current.equalsIgnoreCase("RELEASED")){
                //最新发布版本
                if(!id.equalsIgnoreCase(JF_Util_mxJPO.getLastReleasedMajorid(context, id))){
                    stringBuffer.append(msg).append("\u4E0D\u662F\u6700\u65B0\u53D1\u5E03\u7248\u672C,\u4E0D\u5141\u8BB8\u63D0\u5347").append("\n");
                }
            }
        }
        logger.info("stringBuffer:{}", stringBuffer);
        if (stringBuffer.length()>0){
            ischeck = 1;
            emxContextUtil_mxJPO.mqlNotice(context, stringBuffer.toString());
        }
        return ischeck;
    }

    /*
     * @description:PolicyJFDRInWorkPromoteCheck checkVPMReferenceDrawing Trigger
     *  校验如果填写了图纸跟随发布，当前零件包括子级都需要关联了图纸 包括文档图纸
     * @author: caipan
     * @date: 2025/5/28 09:45:03
     * @param: * @param[1] context
     * @param[2] args
     * @return: 
     **/
    public int checkVPMReferenceDrawing(Context context,String[] args)throws Exception {
        int ischeck = 0;
        String drId = args[0];
        StringBuffer stringBuffer = new StringBuffer();
        DomainObject dr = DomainObject.newInstance(context, drId);
        StringList bosel = JF_Util_mxJPO.basicBolistSel();
        bosel.add("attribute[EnterpriseExtension.V_PartNumber]"); //企业编码
        bosel.add("to[XCADBaseDependency|from.type==Drawing]");
        StringList relsel = JF_Util_mxJPO.basicRellistSel();
        relsel.add("attribute[JFIsFollow]");
        String where = "attribute[JFIsFollow]==Y";
        MapList VPMList = dr.getRelatedObjects(context, RELATIONSHIP_JFDR2VPMREFERENCE, TYPE_VPMREFERENCE, bosel, relsel,
                false, true, (short) 1,
                "", where, 0);
        MapList drawingList = new MapList();
        bosel.add("from[Reference Document|to.attribute[JF_DocumentType]=='Drawing']");
        for (int i = 0; i < VPMList.size(); i++) {
            Map mangerMap = (Map) VPMList.get(i);
            String id = (String) mangerMap.get("id");
            dr.setId(id);
            where = "current==IN_WORK";
           MapList subList =  dr.getRelatedObjects(context, RELATIONSHIP_VPMINSTANCE, TYPE_VPMREFERENCE, bosel, relsel,
                    false, true, (short) 0,
                   where, "", 0);
            drawingList.add(mangerMap);
            if(subList.size()>0){
                drawingList.addAll(subList);
            }
        }
        StringList errorList = new StringList();
        for(int i=0;i<drawingList.size();i++){
            Map map = (Map)drawingList.get(i);
            String docDrwaing = UIUtil.getValue(map,"from[Reference Document]");
            String Drwaing = UIUtil.getValue(map,"to[XCADBaseDependency]");
            String V_PartNumber = UIUtil.getValue(map,"attribute[EnterpriseExtension.V_PartNumber]");
            logger.info("docDrwaing:{} Drwaing {}",docDrwaing,Drwaing);
            if(UIUtil.isNullOrEmpty(docDrwaing)&&UIUtil.isNullOrEmpty(Drwaing)){
                errorList.add(V_PartNumber);
            }
        }
        if (errorList.size() > 0) {
            ischeck = 1;
            emxContextUtil_mxJPO.mqlNotice(context, JF_Util_mxJPO.getMessage(context, "DR.JFIsFollowError", errorList.toString()));
        }
        return ischeck;
    }

    /*
     * @description:上传附件，DR只有在工作才可以上传，其他的忽略
     * @author: caipan
     * @date: 2025/7/16 17:36:16
     * @param: * @param[1] context
     * @param[2] args
     * @return:
     **/
    public  boolean isShowUpload(Context context,String[] args) throws Exception{
        Map params = JPO.unpackArgs(args);
        String strObjectId = (String) params.get("objectId");
        DomainObject obj = DomainObject.newInstance(context,strObjectId);
        String type = obj.getInfo(context, "type");
        String owner =obj.getInfo(context, DomainConstants.SELECT_OWNER);
        String current =obj.getInfo(context, DomainConstants.SELECT_CURRENT);
        if("JFDR".equals(type)||"JFDA".equalsIgnoreCase(type)||"JFECO".equalsIgnoreCase(type)){
            if(owner.equalsIgnoreCase(context.getUser())&&current.equalsIgnoreCase("In_Work")){
                return true;
            }else{
                return false;
            }
        }
       else if("JFProductConfigTable".equals(type)||"JFDBList".equals(type)||"JFServicePartsList".equals(type)||"JFConfigTableRoute".equals(type)){
            if(owner.equalsIgnoreCase(context.getUser())&&current.equalsIgnoreCase("InWork")){
                return true;
            }else{
                return false;
            }
        }
        // add by LIUJR 20260708 标准件发布申请单附件上传/删除仅允许申请单Owner操作。
        if("JFSPartsApplication".equals(type)){
            return owner.equalsIgnoreCase(context.getUser());
        }
        return true;
    }
    /*
     * @description:校验DR、DRW是否包含零件
     * @author: caipan
     * @date: 2025/8/1 09:02:22
     * @param: * @param[1] context
     * @param[2] args
     * @return:
     **/
    public int checkHasPart(Context context,String[] args)throws Exception{
        logger.info("checkHasPart>>>>>"+args[0]);
        String objectId = (String)args[0];
        DomainObject obj = DomainObject.newInstance(context, objectId);
        StringList boSel = JF_Util_mxJPO.basicBolistSel();
        MapList maps = obj.getRelatedObjects(context, REL_JFDR2VPMREFERENCE, // relationship pattern
                TYPE_VPMREFERENCE,                                    // object pattern
                boSel,                            // object selects
                null, // relationship selects
                false,                                        // to direction
                true,                                        // from direction
                (short) 1,                                    // recursion level
                "",                // object where clause
                "",
                (short) 0);
        if(maps.size()==0){
            String strMess = EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", context.getLocale(), "emxComponents.DR.HasPart");
            emxContextUtil_mxJPO.mqlNotice(context,strMess.replace("{}",obj.getInfo(context,"name")));
            return 1;
        }
        return 0;
    }

    public int checkDRWHasDrawing(Context context,String[] args)throws Exception{
        logger.info("checkDRWHasDrawing>>>>>"+args[0]);
        String objectId = (String)args[0];
        DomainObject obj = DomainObject.newInstance(context, objectId);
        ChangeAction changeActionObj=new ChangeAction();
        changeActionObj.setId(objectId);
        MapList mapList = changeActionObj.getAffectedItems(context);
        if(mapList.size()==0){
            String strMess = EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", context.getLocale(), "emxComponents.DRW.HasPart");
            emxContextUtil_mxJPO.mqlNotice(context,strMess.replace("{}",obj.getInfo(context,"name")));
            return 1;
        }
        return 0;
    }
    /*
     * @description:DR搜索零件Include方法
     * @author: caipan
     * @date: 2025/8/5 17:37:17
     * @param: * @param[1] context
     * @param[2] args
     * @return:
     **/
    public StringList getIncludeVPMid(Context context,String[] args)throws Exception{
        StringList result = null;
        Map paramMap = (Map) JPO.unpackArgs(args);
        String objectId = (String)paramMap.get("objectId");
        DomainObject dr = DomainObject.newInstance(context, objectId);
        String projectId = dr.getInfo(context, "from["+JF_PLMConstants_mxJPO.REL_JFChange2Project+"].to.id");
        dr.setId(projectId);
        StringList selList = new StringList();
        selList.add(DomainConstants.SELECT_ID);
//        selList.add("to["+RELATIONSHIP_JFDR2VPMREFERENCE+"]");

        StringList relList = new StringList();
        relList.add(DomainConstants.SELECT_RELATIONSHIP_ID);
        //本项目的工作中的，并且没有关联到DR的数据
        String where = "current==IN_WORK && to[JFDR2VPMReference]==FALSE";
        MapList list = dr.getRelatedObjects(context,
                JF_PLMConstants_mxJPO.rel_JFProject2RootPart, //pattern to match relationships
                JF_PLMConstants_mxJPO.TYPE_VPMReference, //pattern to match types
                selList, //the eMatrix StringList object that holds the list of select statement pertaining to Business Obejcts.
                relList, //the eMatrix StringList object that holds the list of select statement pertaining to Relationships.
                false, //get To relationships
                true, //get From relationships
                (short) 1, //the number of levels to expand, 0 equals expand all.
                where, //where clause to apply to objects, can be empty ""
                null, //where clause to apply to relationship, can be empty ""
                (short) 0); //limit
        result = JF_Util_mxJPO.mapList2StringList(list, DomainConstants.SELECT_ID);
        return result;
    }
    /*
     * @description:根据详细分类获取必填属性，并且校验缺了哪些必填属性
     * @author: caipan
     * @date: 2025/9/3 13:36:44
     * @param: * @param[1] context
     * @param[2] detailType
     * @return:
     **/

    public StringList getRequireAttribute(Context context, String id,String detailType,StringList checkerrorList) throws Exception {
        logger.info("getRequireAttribute start :{},{}",detailType,id);
        DomainObject partObj = DomainObject.newInstance(context);
        partObj.setId(id);
        StringList nullList = new StringList();
        Page pageAttributePopulation = new Page("PartAttributeProperties.xml");
        pageAttributePopulation.open(context);
        String strProperties = pageAttributePopulation.getContents(context);
        logger.info("strProperties:",strProperties);
        pageAttributePopulation.close(context);
        // 使用 ByteArrayInputStream 将字符串转为输入流
        InputStream inputStream = new ByteArrayInputStream(strProperties.getBytes("UTF-8"));
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(inputStream);
        StringList attrList = new StringList();
        attrList.addAll(findPartAttributeWithXPath(document, "MandatoryAttributePart", detailType));
        logger.info("attrList:{}",attrList);
        if(attrList.contains("connectdrw")){
            //单独校验是否图纸
            attrList.remove("connectdrw");
            if(!getVPMReferenceHasDRW(context, id)){
                nullList.add("connectdrw");
            }
        }
        //参考重量JF_Weight有非0.0值时通过，值为0.0时校验重量JF_WeightTarget是否填写
        if(attrList.contains("JF_VPMReference.JF_WeightTarget")){
            attrList.add("JF_VPMReference.JF_Weight");
            attrList.remove("JF_VPMReference.JF_WeightTarget");
        }
        AttributeList attrValueList = partObj.getAttributes(context, attrList);
        for(int i=0;i<attrValueList.size();i++) {
            String value = attrValueList.get(i).getValue();
            String valueName = attrValueList.get(i).getName();
            if(valueName.equalsIgnoreCase("JF_VPMReference.JF_Weight")&&value.equalsIgnoreCase("0.0")){
                 String JF_WeightTarget = partObj.getAttributeValue(context,"JF_VPMReference.JF_WeightTarget");
                 if(UIUtil.isNullOrEmpty(JF_WeightTarget)){
                     nullList.add("JF_VPMReference.JF_WeightTarget");
                 }
            }
            if(UIUtil.isNullOrEmpty(value)&&!checkerrorList.contains(id)){
                nullList.add(valueName);
            }
        }
        return nullList;
    }
    /*
     * @description:获取物理产品是否有图纸 包含图纸和文档图纸
     * @author: caipan
     * @date: 2025/9/3 15:21:15
     * @param: * @param[1] context
     * @param[2] id
     * @return:
     **/
    public boolean getVPMReferenceHasDRW(Context context,String id) throws Exception{
        DomainObject obj = DomainObject.newInstance(context, id);
        MapList documentMapList = obj.getRelatedObjects(context, "Reference Document", "Document", StringList.create("id","name","attribute[Title]"), new StringList(),
                false, true, (short) 1, "attribute[JF_DocumentType].value==Drawing", "", 0);
        MapList drawingMapList = obj.getRelatedObjects(context, "XCADBaseDependency", "Drawing", StringList.create("id","attribute[PLMEntity.V_Name]"), new StringList(),
                true, false, (short) 1, "", "", 0);
        if(documentMapList.size()==0&&drawingMapList.size()==0){
            return false;
        }
        return true;
    }
    public static StringList findPartAttributeWithXPath(Document doc, String configId, String partListTypeId) throws XPathExpressionException {

        // 创建 XPath 工厂和解析器
        XPathFactory xPathFactory = XPathFactory.newInstance();
        XPath xpath = xPathFactory.newXPath();
        // 构建 XPath 表达式
        String expression = String.format("/configurations/configuration[@id='%s']/partType[@id='%s']/attribute/@name", configId, partListTypeId);
//        String expression = "/configurations/configuration[@id='MandatoryAttributePart']/partType[@id='4向手动总成']/attribute/@name";
        logger.info("expression:{}", expression);
        // 执行查询
        NodeList nodes = (NodeList) xpath.compile(expression).evaluate(doc, XPathConstants.NODESET);
        // 检查是否找到节点
        if (nodes.getLength() == 0) {
            return null;
        }
        StringList attributeList = new StringList();
        // 5. 遍历结果并存入Map
        for (int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.item(i);
            String name = node.getNodeValue();
            attributeList.add(name);
        }
        return attributeList;
    }

    /**
     * 设置JF_VPMReference.JF_ConnectDR 属性，解决Bug，重复添加数据到DR，造成最后DR为空
     * @param context
     * @param vpmId
     * @throws Exception
     */
    public  void setJF_ConnectDR(Context context,StringList vpmId,String drId) throws Exception{
        DomainObject obj = DomainObject.newInstance(context);
        String type="";
        for (int i = 0; i < vpmId.size(); i++) {
            obj.setId(vpmId.get(i));
            type = obj.getInfo(context,DomainConstants.SELECT_TYPE);
            if(JF_PLMConstants_mxJPO.TYPE_VPMReference.equalsIgnoreCase(type)){
                MqlUtil.mqlCommand(context, false, "mod bus '" + vpmId.get(i) + "' "+ATTRIBUTE_ConnectDR+" '"+drId+"'", true);
            }
        }
    }

    /**
     * 校验DR中零件所属项目下的客户零件号/DB关系属性
     * @param context 上下文
     * @param partId 零件ID
     * @param detailType 零件的库分类
     * @param nullList 未填写属性集合，调用方统一转为国际化属性名称并提示
     * @author LIUJR
     * @throws Exception 异常
     * @date 2026/7/21
     * @description 客户零件号/DB字段已由零件对象属性调整为JFVPMReference2CustomerParts关系属性。
     *              DR校验时先读取零件所属项目，再读取该项目对应的客户零件号/DB主关系；
     *              零件未维护所属项目时不校验客户零件号/DB；DirectBuy为non-DB时不校验客户零件号、客户零件名称和客户零件版本。
     */
    public void checkDRCustomerPartsDBRequireAttribute(Context context, String partId, String detailType, StringList nullList, String projectId) throws Exception {
        Page pageAttributePopulation = new Page("PartAttributeProperties.xml");
        pageAttributePopulation.open(context);
        String strProperties = pageAttributePopulation.getContents(context);
        logger.info("strProperties:",strProperties);
        pageAttributePopulation.close(context);
        // 使用 ByteArrayInputStream 将字符串转为输入流
        InputStream inputStream = new ByteArrayInputStream(strProperties.getBytes("UTF-8"));
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(inputStream);
        StringList customerPartsDBAttrList = findPartRelationshipWithXPath(document, "MandatoryAttributePart", detailType);
        if (customerPartsDBAttrList == null || customerPartsDBAttrList.size() == 0) {
            return;
        }
        // 没有项目时，客户零件号/DB无法定位到项目维度的数据，本次规则明确不做校验。
        if (UIUtil.isNullOrEmpty(projectId)) {
            return;
        }
        // 同一个零件可能维护多条客户零件号/DB，只校验当前所属项目对应的那条关系属性。
        Map customerPartsDBMap = getDRCustomerPartsDBRelationInfo(context, partId, projectId);
        String directBuy = UIUtil.getValue(customerPartsDBMap, Select_Attr_JF_DirectBuy);
        if (customerPartsDBAttrList.contains(Attr_JF_DirectBuy) && UIUtil.isNullOrEmpty(directBuy)) {
            nullList.add(Attr_JF_DirectBuy);
        }
        // non-DB或未维护DirectBuy时，不继续校验客户零件号、客户零件名称、客户零件版本。
        if (!"consignment".equalsIgnoreCase(directBuy) && !"direct-buy".equalsIgnoreCase(directBuy)) {
            return;
        }
        if (customerPartsDBAttrList.contains(Attr_JFCustomerPartNumber)
                && UIUtil.isNullOrEmpty(UIUtil.getValue(customerPartsDBMap, Select_Attr_JFCustomerPartNumber))) {
            nullList.add(Attr_JFCustomerPartNumber);
        }
        if (customerPartsDBAttrList.contains(Attr_JFCustomerPartName)
                && UIUtil.isNullOrEmpty(UIUtil.getValue(customerPartsDBMap, Select_Attr_JFCustomerPartName))) {
            nullList.add(Attr_JFCustomerPartName);
        }
        if (customerPartsDBAttrList.contains(Attr_JFCustomerPartRevision)
                && UIUtil.isNullOrEmpty(UIUtil.getValue(customerPartsDBMap, Select_Attr_JFCustomerPartRevision))) {
            nullList.add(Attr_JFCustomerPartRevision);
        }
    }

    /**
     * 查询零件项目对应的客户零件号/DB主关系
     * @param context 上下文
     * @param partId 零件ID
     * @param projectId 所属项目ID
     * @author LIUJR
     * @throws Exception 异常
     * @return java.util.Map 客户零件号/DB关系信息
     * @date 2026/7/21
     * @description 一个零件可以维护多个客户零件号/DB，DR校验只读取项目对应的JFVPMReference2CustomerParts关系属性。
     */
    public Map getDRCustomerPartsDBRelationInfo(Context context, String partId, String projectId) throws Exception {
        if (UIUtil.isNullOrEmpty(partId) || UIUtil.isNullOrEmpty(projectId)) {
            return new HashMap();
        }
        String projectIdSelect = "frommid[" + RELATIONSHIP_JFVPMREFERENCE2CUSTOMERPARTS2PROJECT + "].to.id";
        StringList objectSelectList = JF_Util_mxJPO.basicBolistSel();
        StringList relSelectList = JF_Util_mxJPO.basicRellistSel();
        relSelectList.add(projectIdSelect);
        relSelectList.add(Select_Attr_JFCustomerPartNumber);
        relSelectList.add(Select_Attr_JFCustomerPartName);
        relSelectList.add(Select_Attr_JFCustomerPartRevision);
        relSelectList.add(Select_Attr_JF_DirectBuy);
        DomainObject partObject = DomainObject.newInstance(context, partId);
        MapList relatedList = partObject.getRelatedObjects(context,
                RELATIONSHIP_JFVPMREFERENCE2CUSTOMERPARTS,
                TYPE_JFCUSTOMERPARTS,
                objectSelectList,
                relSelectList,
                true,
                false,
                (short) 1,
                "",
                "",
                0);
        for (int i = 0; i < relatedList.size(); i++) {
            Map relatedMap = (Map) relatedList.get(i);
            if (projectId.equals(UIUtil.getValue(relatedMap, projectIdSelect))) {
                return relatedMap;
            }
        }
        return new HashMap();
    }

    /**
     * 根据详细分类获取必填关系属性
     * @param doc XML文档
     * @param configId 配置ID
     * @param partListTypeId 零件详细分类
     * @author LIUJR
     * @throws XPathExpressionException XPath异常
     * @return matrix.util.StringList 关系属性名称集合
     * @date 2026/7/21
     * @description XML中attribute节点表示零件对象属性，relationship节点表示客户零件号/DB关系属性，DR校验按节点类型分别取值。
     */
    private StringList findPartRelationshipWithXPath(Document doc, String configId, String partListTypeId) throws XPathExpressionException {
        XPathFactory xPathFactory = XPathFactory.newInstance();
        XPath xpath = xPathFactory.newXPath();
        String expression = String.format("/configurations/configuration[@id='%s']/partType[@id='%s']/relationship/@name", configId, partListTypeId);
        logger.info("relationship expression:{}", expression);
        NodeList nodes = (NodeList) xpath.compile(expression).evaluate(doc, XPathConstants.NODESET);
        StringList relationshipList = new StringList();
        for (int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.item(i);
            relationshipList.add(node.getNodeValue());
        }
        return relationshipList;
    }
}
