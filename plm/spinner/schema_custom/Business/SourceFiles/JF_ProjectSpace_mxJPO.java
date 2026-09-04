import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.dassault_systemes.i3dx.appsmodel.model.Company;
import com.dassault_systemes.i3dx.appsmodel.model.util.CompanyUtil;
import com.matrixone.apps.common.Issue;
import com.matrixone.apps.common.Task;
import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.DomainRelationship;
import com.matrixone.apps.domain.util.*;
import com.matrixone.apps.framework.ui.ProgramCallable;
import com.matrixone.apps.framework.ui.UIUtil;
import matrix.db.BusinessObjectList;
import matrix.db.Context;
import matrix.db.FileList;
import matrix.db.JPO;
import matrix.util.StringList;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.poi.ss.usermodel.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import jakarta.mail.BodyPart;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMultipart;

import static com.matrixone.apps.domain.DomainConstants.SELECT_ID;
import static com.matrixone.apps.domain.DomainConstants.SELECT_NAME;

/**
 * @description: 和项目有关的代码
 * @param: null
 * @return:
 * @author JJS
 * @date:  16:11
 */
public class JF_ProjectSpace_mxJPO {
    private static final Logger log = LoggerFactory.getLogger(JF_ProjectSpace_mxJPO.class);

    private static final String STRING_FILE_SHARE_PATH = "dataOutSource.share.path";

    private static final String STRING_SEND_DATA = "SendData";
    /** 
     * @description: 任务根据角色关联人员
     * @param: context
	args 
     * @return: void 
     * @author JJS
     * @date:  16:16
     */
    public void connectRolePerson(Context context, String[] args)throws Exception{

        ContextUtil.startTransaction(context,true);
        try {
            Map<String, StringList> taskId2personIds = JPO.unpackArgs(args);
            JF_Util_mxJPO jfUtilMxJPO = new JF_Util_mxJPO();
            Task task = new Task();
            for (String taskId : taskId2personIds.keySet()) {
                StringList personIds = taskId2personIds.get(taskId);
                task.setId(taskId);
               StringList assigneesList =  task.getInfoList(context, "to[Assigned Tasks].from.id");
                for (String personId : personIds) {
                    if(!assigneesList.contains(personId)) {
                        jfUtilMxJPO.assignPerson(context, personId, task);
                    }
                }
            }
            ContextUtil.commitTransaction(context);
        }catch (Exception e){
            log.error(e.getMessage());
            e.printStackTrace();
            ContextUtil.abortTransaction(context);
        }
    }

    /**
    * 项目的人员的状态
    * @param context
	* @param args
    * @author LIUJR
    * @throws
    * @return matrix.util.StringList
    * @date 11/06/2025 13:44
    * @description
    */
    @ProgramCallable
    public static StringList getPersonStatus(Context context, String[] args) throws Exception {
        log.info("getPersonStatus ............................");
        StringList returnList = new StringList();
        try {
            Map paramsMap = (Map) JPO.unpackArgs(args);
            MapList mapList = (MapList) paramsMap.get("objectList");
            Iterator iterator = mapList.iterator();
            DomainObject domainObject = DomainObject.newInstance(context);
            String nls = "emxFramework.State.Person.";
            while (iterator.hasNext()){
                Map map = (Map) iterator.next();
                if (map.containsKey("username")) {
                    String username = (String) map.get("username");
                    domainObject = PersonUtil.getPersonObject(context, username);
                    String current = nls + domainObject.getInfo(context, DomainConstants.SELECT_CURRENT);
                    String currentNls = EnoviaResourceBundle.getProperty(context, "emxFrameworkStringResource", context.getLocale(), current);
                    returnList.add(currentNls);
                } else if (map.containsKey("org")){
                    String orgName = (String) map.get("org");
                    String objectId = JF_PublicMethodClass_mxJPO.findObject(context, DomainConstants.TYPE_COMPANY, "name=='" + orgName + "'");
                    if (UIUtil.isNotNullAndNotEmpty(objectId)) {
                        domainObject.setId(objectId);
                        String current = nls + domainObject.getInfo(context, DomainConstants.SELECT_CURRENT);
                        String currentNls = EnoviaResourceBundle.getProperty(context, "emxFrameworkStringResource", context.getLocale(), current);
                        returnList.add(currentNls);
                    } else {
                        returnList.add(DomainConstants.EMPTY_STRING);
                    }
                } else {
                    returnList.add(DomainConstants.EMPTY_STRING);
                }
            }
        }catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
        return returnList;
    }


    /*
     * @description:获取项目关联的环境数据
     * @author: caipan
     * @date: 2025/7/16 13:50:07
     * @param: * @param[1] context
     * @param[2] args
     * @return:
     **/
    public  MapList getJFProject2EnvironmentList(Context context,String[] args) throws Exception{
        try {
            StringList selList = JF_Util_mxJPO.basicBolistSel();
            StringList relList = JF_Util_mxJPO.basicRellistSel();
            Map paramsMap = (Map) JPO.unpackArgs(args);
            ContextUtil.pushContext(context);
            String strObjectId = (String) paramsMap.get(JF_PLMConstants_mxJPO.STRING_OBJECTID);
            DomainObject objectProject = DomainObject.newInstance(context, strObjectId);
            return  objectProject.getRelatedObjects(context,
                    JF_PLMConstants_mxJPO.ATTR_JFProject2Environment, //pattern to match relationships
                    JF_PLMConstants_mxJPO.TYPE_VPMReference, //pattern to match types
                    selList, //the eMatrix StringList object that holds the list of select statement pertaining to Business Obejcts.
                    relList, //the eMatrix StringList object that holds the list of select statement pertaining to Relationships.
                    false, //get To relationships
                    true, //get From relationships
                    (short) 1, //the number of levels to expand, 0 equals expand all.
                    DomainConstants.EMPTY_STRING, //where clause to apply to objects, can be empty ""
                    null, //where clause to apply to relationship, can be empty ""
                    (short) 0); //limit
        }catch (Exception e) {
            e.printStackTrace();
        }finally {
            ContextUtil.popContext(context);
        }
        return new MapList();
    }
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
            childPartList =  obj.getRelatedObjects(context,JF_PLMConstants_mxJPO.REL_Instance,JF_PLMConstants_mxJPO.TYPE_VPMReference,
                    boSel,relSel,false,true,Short.parseShort(expandLevel),"","",0);
        }catch (Exception e){
            e.printStackTrace();
        }
        return childPartList;
    }
    /*
     * @description:当前角色是整椅就显示
     * @author: caipan
     * @date: 2025/7/16 15:53:29
     * @param: * @param[1] context
     * @param[2] arg
     * @return:
     **/
    public boolean isSDTWholeSeat(Context context,String[] arg) throws Exception{
        Map request = JPO.unpackArgs(arg);
        log.info("request:{}",request);
        String objectId = (String)request.get("objectId");
        JF_ECRProcess_mxJPO process = new JF_ECRProcess_mxJPO();
        Map AME = process.getProjectRole(context, objectId, "Chair manager");
        String chairName = UIUtil.getValue(AME,DomainConstants.SELECT_NAME);
        log.info("chairName:{}",chairName);
        if(context.getUser().equals(chairName)){
            return true;
        }
        return false;
    }
    public boolean isSDTWholeSeatAndSupply(Context context,String[] arg) throws Exception{
        Map request = JPO.unpackArgs(arg);
        log.info("request:{}",request);
        String objectId = (String)request.get("objectId");
        JF_ECRProcess_mxJPO process = new JF_ECRProcess_mxJPO();
        Map AME = process.getProjectRole(context, objectId, "Chair manager");
        String chairName = UIUtil.getValue(AME,DomainConstants.SELECT_NAME);
        log.info("chairName:{}",chairName);
        DomainObject projectObj = DomainObject.newInstance(context, objectId);
        String supply = projectObj.getInfo(context, JF_PLMConstants_mxJPO.SELECT_ATTR_JF_SupplyAffirm);
        if(context.getUser().equals(chairName)&&"N".equalsIgnoreCase(supply)){
            return true;
        }
        return false;
    }
    public Map getSupplyRange(Context context , String[] args)throws Exception{
        Map policyMap = new HashMap();
        Map programMap = (Map)JPO.unpackArgs(args);
        Map requestMap = (Map)programMap.get("requestMap");
        Map paramMap   = (Map)programMap.get("paramMap");
        String sLanguage = (String) paramMap.get("languageStr");
        String objectId = (String) requestMap.get("objectId");
        String Y= (String) EnoviaResourceBundle.getFrameworkStringResourceProperty(context, "emxFramework.Range.JF_SupplyAffirm.Y", context.getLocale());
        String N= (String) EnoviaResourceBundle.getFrameworkStringResourceProperty(context, "emxFramework.Range.JF_SupplyAffirm.N", context.getLocale());
        log.info("objectId:{}",objectId);
        DomainObject projectObj = DomainObject.newInstance(context, objectId);
        String supply = projectObj.getInfo(context, JF_PLMConstants_mxJPO.SELECT_ATTR_JF_SupplyAffirm);
        StringList keyList = new StringList();
        StringList valueList = new StringList();
        if("N".equalsIgnoreCase(supply)){
            keyList.add("N");
            valueList.add(N);
//            keyList.add("Y");
//            valueList.add(Y);
        }else{
            keyList.add("Y");
            valueList.add(Y);
//            keyList.add("N");
//            valueList.add(N);
        }
        policyMap.put("field_choices",  keyList);
        policyMap.put("field_display_choices", valueList);
        return policyMap;
    }


    /**
     * @Author Liuxg
     * @Description 传入项目编号，获取项目信息，（用于接口传递）,需要支持批量
     * @Date 2025/7/23 16:25
     * @Param [context, args]
     * @return com.alibaba.fastjson.JSON
    **/
    public JSONObject getProjectInfoInterface(Context context,String[]args)throws Exception{
        JSONObject returnJosn=new JSONObject();
        try {
            HashMap programMap = (HashMap) JPO.unpackArgs(args);
//            String  projectName = (String) programMap.get("ProjectID");
            ArrayList  projectNames = (ArrayList) programMap.get("ProjectID");
            log.info("ProjectID--->"+projectNames);

            StringList pnamelist=new StringList(projectNames);
            log.info("pnamelist--->"+pnamelist);
            StringList busSelects=new StringList();
            busSelects.add(DomainConstants.SELECT_ID);

            JSONArray dataarray=new JSONArray();

            if(pnamelist.size()>0){

                for(int i=0;i<pnamelist.size();i++){
                    JSONObject projectjson=new JSONObject();
                    String projectName=pnamelist.get(i);
                    MapList mlProject = DomainObject.findObjects(context, "Project Space", // type filter
                            null, // vault filter
                            "name == '" + projectName + "'", // where clause
                            busSelects);
                    //如果能查询到，应该只有一个项目
                    log.info("mlProject.size()---->"+mlProject.size());
                    if(mlProject.size()>0){
                        Map projectMap= (Map) mlProject.get(0);
                        String projectid= (String) projectMap.get(DomainConstants.SELECT_ID);
                        log.info("projectid--->"+projectid);
                        JSONObject projectdata=getProjectInfo(context,projectid);
                        projectjson.put("data",projectdata);
                        projectjson.put("ProjectID",projectName);
                        projectjson.put("result","Y");

                    }else {
                        //没有查询到对象
                        projectjson.put("data",new JSONObject());
                        projectjson.put("ProjectID",projectName);
                        projectjson.put("result","N");

                    }
                    dataarray.add(projectjson);
                }
            }else {
                //为空查询全量
                MapList mlProject = DomainObject.findObjects(context, "Project Space", // type filter
                        null, // vault filter
                        "", // where clause
                        busSelects);
                for(int i=0;i<mlProject.size();i++){
                    Map projectMap= (Map) mlProject.get(i);
                    JSONObject projectjson=new JSONObject();
                    String projectid= (String) projectMap.get(DomainConstants.SELECT_ID);
                    JSONObject projectdata=getProjectInfo(context,projectid);
                    projectjson.put("data",projectdata);
                    projectjson.put("ProjectID",projectdata.get("ProjectID"));
                    projectjson.put("result","Y");
                    dataarray.add(projectjson);
                }

            }
                    returnJosn.put("dataArray",dataarray);
                    returnJosn.put("code","200");
                    returnJosn.put("msg","");
                    returnJosn.put("result","success");

        }catch (Exception e){
            e.printStackTrace();
            returnJosn.put("code","500");
            returnJosn.put("msg",e.getMessage());
            returnJosn.put("result","failed");
        }
        return returnJosn;
    }

    /**
     * @Author Liuxg
     * @Description 获取项目信息json数据
     * @Date 2025/7/27 23:55
     * @Param [context, projectid]
     * @return com.alibaba.fastjson.JSONObject
    **/
    public JSONObject getProjectInfo(Context context,String projectid) throws Exception{

        JSONObject data = new JSONObject();
        try {

            DomainObject projectObj = DomainObject.newInstance(context, projectid);

            Map projectMap = projectObj.getInfo(context, getProjectAttrList());
            Boolean noDVFlag  = JF_ESO_mxJPO.projectSpaceHasDVFlag(context, projectObj);


            JSONObject projectjson = new JSONObject();
            projectjson.put("ProjectID", ProjectAttrProcess(projectMap, DomainConstants.SELECT_NAME));
            projectjson.put("ProjectName", ProjectAttrProcess(projectMap, DomainConstants.SELECT_DESCRIPTION));
            projectjson.put("ProjectType", ProjectAttrProcess(projectMap, "attribute[JF_ProjType]"));
            projectjson.put("DirectCustomer", ProjectAttrProcess(projectMap, "attribute[JF_DirectCustomer]"));
            projectjson.put("CustomerLocation", ProjectAttrProcess(projectMap, "attribute[JF_CustomerLoc]"));
            projectjson.put("Model", ProjectAttrProcess(projectMap, "attribute[JF_Model]"));
            projectjson.put("OEM", ProjectAttrProcess(projectMap, "attribute[JF_ProjectCustomers]"));
            //add by caipan
            String rangeI18NString = EnoviaResourceBundle.getRangeI18NString(context, "JFAffectedFactory", ProjectAttrProcess(projectMap, "attribute[JFAffectedFactory]"), context.getSession().getLanguage());
            projectjson.put("Plant", rangeI18NString);
            log.info("JFAffectedFactory:{} rangeI18NString:{}",ProjectAttrProcess(projectMap, "attribute[JFAffectedFactory]"),rangeI18NString);
            projectjson.put("PlantLocation", ProjectAttrProcess(projectMap, "attribute[JF_PlantLoc]"));

            projectjson.put("SalesTarget", ProjectAttrProcess(projectMap, "attribute[JF_SalesTarget]"));
            projectjson.put("AnnualSales", ProjectAttrProcess(projectMap, "attribute[JF_AnnualSales]"));
            projectjson.put("ProductName", ProjectAttrProcess(projectMap, "attribute[JF_ProductName]"));
            projectjson.put("ProductType", ProjectAttrProcess(projectMap, "attribute[JF_ProductType]"));
            projectjson.put("MProductionYear", ProjectAttrProcess(projectMap, "attribute[JF_DateOfMassProduction]"));
            projectjson.put("ProjectSOP", ProjectAttrProcess(projectMap, "attribute[JF_PSStartDate]"));
            projectjson.put("ProjectEOP", ProjectAttrProcess(projectMap, "attribute[JF_ProjectEOP]"));
            projectjson.put("T0Date", ProjectAttrProcess(projectMap, "attribute[JF_T0Date]"));
            projectjson.put("DVDate", ProjectAttrProcess(projectMap, "attribute[JF_DVDate]"));
            projectjson.put("PVDate", ProjectAttrProcess(projectMap, "attribute[JF_PVDate]"));
            projectjson.put("SOPDate", ProjectAttrProcess(projectMap, "attribute[JF_PSSOPDate]"));
            projectjson.put("CatiaRevision", ProjectAttrProcess(projectMap, "attribute[JSOutSourceRev]"));
            JSONObject ESOInfojson = new JSONObject();

            ESOInfojson.put("ProjectTag", ProjectAttrProcess(projectMap, "attribute[JF_ProjectTag]"));
            ESOInfojson.put("ProjectState", ProjectAttrProcess(projectMap, "attribute[JF_ProjectStatus]"));
            ESOInfojson.put("ECInfo", ProjectAttrProcess(projectMap, "attribute[JF_ProjectECI]"));
            ESOInfojson.put("Grade", ProjectAttrProcess(projectMap, "attribute[JF_ProjectGrade]"));

            ESOInfojson.put("Phase1Date", ProjectAttrProcess(projectMap, "attribute[JF_Phase1PlannedCompletionTime]"));
            if (noDVFlag) {
                ESOInfojson.put("Phase2+3Date", ProjectAttrProcess(projectMap, "attribute[JF_Phase2_3PlannedCompletionTime]"));
            } else {
                ESOInfojson.put("Phase2Date", ProjectAttrProcess(projectMap, "attribute[JF_Phase2PlannedCompletionTime]"));
                ESOInfojson.put("Phase3Date", ProjectAttrProcess(projectMap, "attribute[JF_Phase3PlannedCompletionTime]"));
            }
            ESOInfojson.put("Phase4Date", ProjectAttrProcess(projectMap, "attribute[JF_Phase4PlannedCompletionTime]"));
            ESOInfojson.put("Phase5Date", ProjectAttrProcess(projectMap, "attribute[JF_Phase5PlannedCompletionTime]"));

            JSONArray memberlist = getProjectMemberInfo(context, projectid);
            log.info("memberlist----->"+memberlist);
            data.put("Project", projectjson);
            data.put("MemberList", memberlist);
            data.put("ESOInfo", ESOInfojson);



        } catch (Exception e) {
            e.printStackTrace();
            throw e;

        }
        return data;
    }

    /**
     * @Author Liuxg
     * @Description 处理属性
     * @Date 2025/7/24 23:53
     * @Param [attrmap, attr]
     * @return java.lang.String
    **/
    public String ProjectAttrProcess(Map attrmap,String attr){
        String result="";
        if(attrmap.containsKey(attr)){
            String value= (String) attrmap.get(attr);
            if(UIUtil.isNotNullAndNotEmpty(value)){
                result=value;
            }
        }
       return result;
    }

    /**
     * @Author Liuxg
     * @Description 获取项目人员信息
     * @Date 2025/7/24 23:53
     * @Param [context, Projectid]
     * @return com.alibaba.fastjson.JSONArray
    **/
    public JSONArray getProjectMemberInfo(Context context,String Projectid)throws Exception{
        JSONArray memberlist=new JSONArray();
        try {
            ContextUtil.pushContext(context);
            //Person	JF_UserID
            //Person	JF_UserName
            //Member	JF_Role
            StringList busSl=new StringList();
            StringList relSl=new StringList();

            //人员的id取什么需要明确
            busSl.add("attribute[First Name]");
            busSl.add(DomainConstants.SELECT_NAME);
            busSl.add("attribute[JF_UserID]");
            busSl.add("attribute[JF_UserName]");
            relSl.add("attribute[Project Role]");

            DomainObject projectSpace = DomainObject.newInstance(context, Projectid);

            MapList mapList = projectSpace.getRelatedObjects(context, "Member", DomainConstants.TYPE_PERSON, busSl, relSl,
                    false, true, (short) 1,
                    "", "", 0);


            //问题账户集合，临时处理
            Map errorMap=new HashMap();
            errorMap.put("uma001","umaxh001");
            errorMap.put("uliuhu005","uliuh005");
            errorMap.put("uwana03","uwana003");
            errorMap.put("ushenb002","usheb002");
            errorMap.put("uzhang001","uzhal012");
//            errorMap.put("uzhad09","uzhad09");
//            errorMap.put("uzhaj0008","uzhaj0008");
            errorMap.put("ufan004","ufanc004");
            errorMap.put("uzhy007","uzhey007");

            int num=1;
            log.info("mapList------>"+mapList.size());
            for(int i=0;i<mapList.size();i++){
                Map personMap= (Map) mapList.get(i);
                String Role= (String) personMap.get("attribute[Project Role]");
                log.info("Role------>"+Role);
                if(UIUtil.isNotNullAndNotEmpty(Role)){
                    JSONObject jsonObject=new JSONObject();
                    String UserName= (String) personMap.get("attribute[First Name]");
                    String UserID= (String)personMap.get(DomainConstants.SELECT_NAME);
                    if(errorMap.containsKey(UserID)){
                        UserID= (String) errorMap.get(UserID);
                    }
//                    String UserName= (String) personMap.get("attribute[JF_UserName]");
//                    String UserID= (String)personMap.get("attribute[JF_UserID]");
                    jsonObject.put("UserName",UserName);
                    jsonObject.put("UserID",UserID);
                    //role 需要单独处理，不能直接用原本的range值
                    jsonObject.put("Role",getRoleValue(Role));
                    jsonObject.put("index",num);
                    num++;
                    memberlist.add(jsonObject);
                }
            }

        }catch (Exception e){
            e.printStackTrace();
        }finally {
            ContextUtil.popContext(context);
        }
        return memberlist;
    }

    /**
     * @Author Liuxg
     * @Description 处理人员在项目中角色的值的方法
     * @Date 2025/7/27 23:54
     * @Param [rolekey]
     * @return java.lang.String
    **/
    public String getRoleValue(String rolekey){
        String result="";
        //  range = Financial BP
        //  range = Costing
        //  range = Chair manager
        //  range = AQE representative/PQL
        //  range = Logistics representative
        //  range = AME representative
        //  range = Launch manager
        //  range = Purchasing representative
        //  range = SQD Representative
        //  range = Business manager
        //  range = Project manager
        //  range = Internal Supplier
        //  range = Foam AME representative
        //  range = Trim AME representative



        //项目经理 PM
        //商务经理 BU
        //SQD代表 SQD
        //采购代表 PUR
        //launch经理 LAUNCH
        //发泡AME代表 FAME
        //面套AME代表 TAME
        //总装AME代表 CAME
        //物流代表 LOGISTICS
        //AQE代表/PQL AQE
        //整椅经理 RD
        //Costing COSTING
        //SDT-财务BP FA
        //内部供应商 ICO

        switch (rolekey){
            case "Financial BP":
                result="FA";
                break;
            case "Costing":
                result="COSTING";
                break;
            case "Chair manager":
                result="RD";
                break;
            case "AQE representative/PQL":
                result="AQE";
                break;
            case "Logistics representative":
                result="LOGISTICS";
                break;
            case "AME representative":
                result="CAME";
                break;
            case "Launch manager":
                result="LAUNCH";
                break;
            case "Purchasing representative":
                result="PUR";
                break;
            case "SQD Representative":
                result="SQD";
                break;
            case "Business manager":
                result="BU";
                break;
            case "Project manager":
                result="PM";
                break;
            case "Internal Supplier":
                result="ICO";
                break;
            case "Foam AME representative":
                result="FAME";
                break;
            case "Trim AME representative":
                result="TAME";
                break;
            default:
                result="";
        }

        return result;
    }

    /**
     * @Author Liuxg
     * @Description 创建项目后trigger触发同步BI（暂时未添加）
     * @Date 2025/7/24 23:53
     * @Param [context, args]
     * @return void
    **/
    public void JFCreateProjectSendBI(Context context,String[]args)throws Exception{
        boolean ispush = false;
        try {
            String projectid=args[0];



                JSONObject data=getProjectInfo(context,projectid);

                //调用BI接口
//            JSONObject projectjson= (JSONObject) data.get("Project");
//            System.out.println("projectjson--->"+projectjson);
                System.out.println("data--->"+data);
                //测试用：
//            调用地址：http://172.16.33.183:7080/JFSEATesb/Services/ServiceProjectInfo

                Map headerMap=new HashMap();
                headerMap.put("jf_svc","IOA0170");
                headerMap.put("jf_applicationid",UUID.randomUUID()+"");
                headerMap.put("jf_sender","PLM");
                headerMap.put("jf_receiver","BI");
                headerMap.put("jf_document",UUID.randomUUID()+"");

//              String url="http://172.16.33.183:7080/JFSEATesb/Services/ServiceProjectInfo";
//              String url="http://172.16.33.183:7080/JFSEATesb/Services/PLM/ServiceProjectInfo";
                String url = JF_PublicMethodClass_mxJPO.getBasicUrl(context, new String[]{"JFSyncProject.ESB.URL"});

                String result= dopost(data.toString(),url,headerMap);

                JSONObject rejson=JSONObject.parseObject(result);
                String strJsonPath = JF_PublicMethodClass_mxJPO.getBasicUrl(context, new String[]{"MBOMToMDM.JsonPath"});
                String strTime = getTimeCuo();
                writeJsonToFile(rejson, strJsonPath+"JFCreateProjectSendBI_"+strTime+".json");
                //mod by liuxg 新增项目创建后发送邮件代码
                ContextUtil.pushContext(context);
                ispush = true;
               // CreateSengEmail(context,projectobj.getDescription(context));



        }catch (Exception e){
            e.printStackTrace();
            throw e;
        }finally {
            if(ispush) {
                ContextUtil.popContext(context);
            }
        }
    }
    
    /**
     * @Author Liuxg
     * @Description 传入项目编号，获取项目下的所有任务信息
     * @Date 2025/7/29 0:09
     * @Param [context, args]
     * @return com.alibaba.fastjson.JSONObject
    **/
    public JSONObject getProjectTaskInfoInterface(Context context,String[]args)throws Exception{
        JSONObject returnJosn=new JSONObject();
        try {
            HashMap programMap = (HashMap) JPO.unpackArgs(args);
//            String  projectName = (String) programMap.get("ProjectID");
            ArrayList  projectNames = (ArrayList) programMap.get("ProjectID");
            log.info("ProjectID--->"+projectNames);

            StringList pnamelist=new StringList(projectNames);
            log.info("pnamelist--->"+pnamelist);

            JSONArray dataarray=new JSONArray();

            StringList busSelects=new StringList();
            busSelects.add(DomainConstants.SELECT_ID);


            if(pnamelist.size()>0){
                for(int i=0;i<pnamelist.size();i++){
                    JSONObject projectjson=new JSONObject();
                    String projectName=pnamelist.get(i);

                    MapList mlProject = DomainObject.findObjects(context, "Project Space", // type filter
                            null, // vault filter
                            "name == '" + projectName + "'", // where clause
                            busSelects);
                    //如果能查询到，应该只有一个项目
                    if(mlProject.size()>0){
                        Map projectMap= (Map) mlProject.get(0);
                        String projectid= (String) projectMap.get(DomainConstants.SELECT_ID);

                        JSONArray projectdata=getProjectTaskInfo(context,projectid);
                        projectjson.put("data",projectdata);
                        projectjson.put("ProjectID",projectName);
                        projectjson.put("result","Y");

                    }else {
                        //没有查询到对象
                        projectjson.put("data",new JSONArray());
                        projectjson.put("ProjectID",projectName);
                        projectjson.put("result","N");

                    }

                    dataarray.add(projectjson);
                }
            }else {
                //查全量

                MapList mlProject = DomainObject.findObjects(context, "Project Space", // type filter
                        null, // vault filter
                        "", // where clause
                        busSelects);
                for(int i=0;i<mlProject.size();i++){
                    Map projectMap= (Map) mlProject.get(i);
                    String projectid= (String) projectMap.get(DomainConstants.SELECT_ID);
                    JSONObject projectjson=new JSONObject();
                    JSONArray projectdata=getProjectTaskInfo(context,projectid);

                    DomainObject proObj=DomainObject.newInstance(context,projectid);

                    projectjson.put("data",projectdata);
                    projectjson.put("ProjectID",proObj.getName(context));
                    projectjson.put("result","Y");
                    dataarray.add(projectjson);
                }
            }


            returnJosn.put("dataArray",dataarray);
            returnJosn.put("code","200");
            returnJosn.put("msg","");
            returnJosn.put("result","success");



        }catch (Exception e){
            e.printStackTrace();
            returnJosn.put("code","500");
            returnJosn.put("msg",e.getMessage());
            returnJosn.put("result","failed");
        }
        return returnJosn;
    }

    public JSONArray getProjectTaskInfo(Context context,String projectid)throws Exception{
        JSONArray TaskArray=new JSONArray();

        //通过project的 Project Access List  from  Project Access List  PAL-21722479906420   获取到pal对象
        //Project Access Key  to  Gate  T-sit-0000194  11746685916196  pak关系获取所有关联的任务类型对象，
        DomainObject projectSpace=DomainObject.newInstance(context,projectid);

        String PALid=projectSpace.getInfo(context,"to[Project Access List].from.id");
        if(UIUtil.isNotNullAndNotEmpty(PALid)){
            DomainObject PALobj=DomainObject.newInstance(context,PALid);
            StringList busSl=new StringList();
            StringList relSl=new StringList();

            //人员的id取什么需要明确
            busSl.add(DomainConstants.SELECT_NAME);
            busSl.add(DomainConstants.SELECT_DESCRIPTION);
            busSl.add(DomainConstants.SELECT_ID);
            busSl.add(DomainConstants.SELECT_PHYSICAL_ID);
            busSl.add("to[Subtask].from.id");
            busSl.add(DomainConstants.SELECT_TYPE);
            busSl.add(DomainConstants.SELECT_ORIGINATED);
            busSl.add(DomainConstants.SELECT_MODIFIED);
            busSl.add(DomainConstants.SELECT_OWNER);
            busSl.add("Originator");
            busSl.add(DomainConstants.SELECT_CURRENT);
            busSl.add("attribute[Percent Complete]");
            busSl.add("attribute[Task Actual Duration]");
            busSl.add("attribute[Task Actual Finish Date]");
            busSl.add("attribute[Task Actual Start Date]");
            busSl.add("attribute[Task Estimated Duration]");
            busSl.add("attribute[Task Estimated Finish Date]");
            busSl.add("attribute[Task Estimated Start Date]");
            busSl.add("attribute[Task Constraint Date]");

            busSl.add("attribute[Project Role]");
            busSl.add("attribute[Critical Task]");
            busSl.add("attribute[Task Constraint Type]");
            busSl.add("attribute[Needs Review]");
            busSl.add("attribute[JF_isKeyTask]");
            busSl.add("attribute[JF_ReviewCount]");
            busSl.add("attribute[JF_ColorIdentification]");
            busSl.add("attribute[JF_IsAPQP]");

            //新增ESO任务的属性，有就有，没有就为空
            busSl.add("attribute[JF_Function]");
            busSl.add("attribute[JF_Locations]");
            busSl.add("attribute[JF_Department]");
            busSl.add("attribute[JF_Modules]");
            busSl.add("attribute[JF_ESOType]");
            busSl.add("attribute[JF_Rows]");
            busSl.add("attribute[JF_R2_TKO]");



            relSl.add(DomainRelationship.SELECT_ID);


            MapList mapList = PALobj.getRelatedObjects(context, "Project Access Key", "*", busSl, relSl,
                    false, true, (short) 1,
                    "", "", 0);
            for(int i=0;i<mapList.size();i++){
                Map TaskMap= (Map) mapList.get(i);

                JSONObject jsonObject=new JSONObject();


                jsonObject.put("name", ProjectAttrProcess(TaskMap, DomainConstants.SELECT_NAME));
                jsonObject.put("description", ProjectAttrProcess(TaskMap,DomainConstants.SELECT_DESCRIPTION));
                jsonObject.put("id", ProjectAttrProcess(TaskMap,DomainConstants.SELECT_ID));
                jsonObject.put("ParentID", ProjectAttrProcess(TaskMap,"to[Subtask].from.id"));
                jsonObject.put("Type", ProjectAttrProcess(TaskMap,DomainConstants.SELECT_TYPE));
                jsonObject.put("created", ProjectAttrProcess(TaskMap,DomainConstants.SELECT_ORIGINATED));
                jsonObject.put("modified", ProjectAttrProcess(TaskMap,DomainConstants.SELECT_MODIFIED));
                jsonObject.put("owner", ProjectAttrProcess(TaskMap,DomainConstants.SELECT_OWNER));
                jsonObject.put("Originator", ProjectAttrProcess(TaskMap,"Originator"));
                jsonObject.put("current", ProjectAttrProcess(TaskMap,DomainConstants.SELECT_CURRENT));
                jsonObject.put("Percent Complete", ProjectAttrProcess(TaskMap,"attribute[Percent Complete]"));
                jsonObject.put("Task Actual Duration", ProjectAttrProcess(TaskMap,"attribute[Task Actual Duration]"));
                jsonObject.put("Task Actual Finish Date", ProjectAttrProcess(TaskMap,"attribute[Task Actual Finish Date]"));
                jsonObject.put("Task Actual Start Date", ProjectAttrProcess(TaskMap,"attribute[Task Actual Start Date]"));
                jsonObject.put("Task Estimated Duration", ProjectAttrProcess(TaskMap,"attribute[Task Estimated Duration]"));
                jsonObject.put("Task Estimated Finish Date", ProjectAttrProcess(TaskMap,"attribute[Task Estimated Finish Date]"));
                jsonObject.put("Task Estimated Start Date", ProjectAttrProcess(TaskMap,"attribute[Task Estimated Start Date]"));
                jsonObject.put("Task Constraint Date", ProjectAttrProcess(TaskMap,"attribute[Task Constraint Date]"));

                jsonObject.put("Project Role", ProjectAttrProcess(TaskMap,"attribute[Project Role]"));
                jsonObject.put("Critical Task", ProjectAttrProcess(TaskMap,"attribute[Critical Task]"));
                jsonObject.put("Task Constraint Type", ProjectAttrProcess(TaskMap,"attribute[Task Constraint Type]"));
                jsonObject.put("Needs Review", ProjectAttrProcess(TaskMap,"attribute[Needs Review]"));
                jsonObject.put("isKeyTask", ProjectAttrProcess(TaskMap,"attribute[JF_isKeyTask]"));
                jsonObject.put("ReviewCount", ProjectAttrProcess(TaskMap,"attribute[JF_ReviewCount]"));
                jsonObject.put("ColorIdentification", ProjectAttrProcess(TaskMap,"attribute[JF_ColorIdentification]"));
                jsonObject.put("IsAPQP", ProjectAttrProcess(TaskMap,"attribute[JF_IsAPQP]"));

//                busSl.add("JF_Function");
//                busSl.add("JF_Locations");
//                busSl.add("JF_Department");
//                busSl.add("JF_Modules");
//                busSl.add("JF_ESOType");


                jsonObject.put("Function", ProjectAttrProcess(TaskMap,"attribute[JF_Function]"));
                jsonObject.put("Locations", ProjectAttrProcess(TaskMap,"attribute[JF_Locations]"));
                jsonObject.put("Department", ProjectAttrProcess(TaskMap,"attribute[JF_Department]"));
                jsonObject.put("Modules", ProjectAttrProcess(TaskMap,"attribute[JF_Modules]"));
                jsonObject.put("ESOType", ProjectAttrProcess(TaskMap,"attribute[JF_ESOType]"));
                jsonObject.put("Rows", ProjectAttrProcess(TaskMap,"attribute[JF_Rows]"));
                jsonObject.put("TKO", ProjectAttrProcess(TaskMap,"attribute[JF_R2_TKO]"));
                jsonObject.put("issues", getTaskIssueInfo(context, ProjectAttrProcess(TaskMap, DomainConstants.SELECT_PHYSICAL_ID)));
                TaskArray.add(jsonObject);
            }
        }


        return TaskArray;
    }

    /**
     * 获取项目任务关联的Issue信息
     **
     * @param context
     * @param taskPhysicalId 任务physical id
     * @return JSONArray Issue信息数组
     * @throws Exception
     * @author Codex
     * @date 2026/7/6 00:00
     */
    public JSONArray getTaskIssueInfo(Context context, String taskPhysicalId) throws Exception {
        JSONArray issueArray = new JSONArray();
        if (UIUtil.isNullOrEmpty(taskPhysicalId)) {
            return issueArray;
        }

        MapList taskIssues = new Issue().getAllIssues(context, taskPhysicalId, true, true);
        if (taskIssues == null || taskIssues.isEmpty()) {
            return issueArray;
        }

        StringList issueIds = new StringList();
        for (Object taskIssue : taskIssues) {
            Map issueMap = (Map) taskIssue;
            String issueId = ProjectAttrProcess(issueMap, DomainConstants.SELECT_ID);
            if (UIUtil.isNotNullAndNotEmpty(issueId)) {
                issueIds.add(issueId);
            }
        }
        if (issueIds.isEmpty()) {
            return issueArray;
        }

        String assignPersonSelect = "from[Technical Assignee].to.name";
        StringList issueSelects = StringList.create(
                DomainConstants.SELECT_NAME,
                DomainConstants.SELECT_DESCRIPTION,
                DomainConstants.SELECT_CURRENT,
                DomainConstants.SELECT_OWNER,
                assignPersonSelect,
                "attribute[Estimated Start Date]",
                "attribute[Estimated End Date]",
                "attribute[Actual Start Date]",
                "attribute[Actual End Date]",
                "attribute[Action Taken]",
                "attribute[Escalation Required]",
                "attribute[Issue Category]",
                "attribute[Resolution Recommendation]",
                "attribute[ResolutionStatement]",
                "attribute[Steps To Reproduce]",
                "attribute[Waiting On]"
        );
        MapList issueInfoList = DomainObject.getInfo(context, issueIds.toStringArray(), issueSelects);
        for (Object issueInfo : issueInfoList) {
            Map issueInfoMap = (Map) issueInfo;
            JSONObject issueJson = new JSONObject();
            issueJson.put("name", ProjectAttrProcess(issueInfoMap, DomainConstants.SELECT_NAME));
            issueJson.put("description", ProjectAttrProcess(issueInfoMap, DomainConstants.SELECT_DESCRIPTION));
            issueJson.put("current", ProjectAttrProcess(issueInfoMap, DomainConstants.SELECT_CURRENT));
            issueJson.put("owner", ProjectAttrProcess(issueInfoMap, DomainConstants.SELECT_OWNER));
            issueJson.put("assignPerson", getStringListValue(issueInfoMap, assignPersonSelect));
            issueJson.put("Estimated Start Date", ProjectAttrProcess(issueInfoMap, "attribute[Estimated Start Date]"));
            issueJson.put("Estimated End Date", ProjectAttrProcess(issueInfoMap, "attribute[Estimated End Date]"));
            issueJson.put("Actual Start Date", ProjectAttrProcess(issueInfoMap, "attribute[Actual Start Date]"));
            issueJson.put("Actual End Date", ProjectAttrProcess(issueInfoMap, "attribute[Actual End Date]"));
            issueJson.put("Action Taken", ProjectAttrProcess(issueInfoMap, "attribute[Action Taken]"));
            issueJson.put("Escalation Required", ProjectAttrProcess(issueInfoMap, "attribute[Escalation Required]"));
            issueJson.put("Issue Category", ProjectAttrProcess(issueInfoMap, "attribute[Issue Category]"));
            issueJson.put("Resolution Recommendation", ProjectAttrProcess(issueInfoMap, "attribute[Resolution Recommendation]"));
            issueJson.put("ResolutionStatement", ProjectAttrProcess(issueInfoMap, "attribute[ResolutionStatement]"));
            issueJson.put("Steps To Reproduce", ProjectAttrProcess(issueInfoMap, "attribute[Steps To Reproduce]"));
            issueJson.put("Waiting On", ProjectAttrProcess(issueInfoMap, "attribute[Waiting On]"));
            issueArray.add(issueJson);
        }
        return issueArray;
    }

    /**
     * 将select返回的单值或多值转换为逗号分隔字符串
     **
     * @param attrmap select结果
     * @param attr select表达式
     * @return String 多值使用逗号拼接
     * @throws Exception
     * @author Codex
     * @date 2026/7/6 00:00
     */
    public String getStringListValue(Map attrmap, String attr) throws Exception {
        String result = "";
        if (!attrmap.containsKey(attr)) {
            return result;
        }
        Object value = attrmap.get(attr);
        if (value instanceof List) {
            List list = (List) value;
            StringList valueList = new StringList();
            for (Object item : list) {
                if (item != null && UIUtil.isNotNullAndNotEmpty(String.valueOf(item))) {
                    valueList.add(String.valueOf(item));
                }
            }
            result = String.join(",", valueList);
        } else if (value != null && UIUtil.isNotNullAndNotEmpty(String.valueOf(value))) {
            result = String.valueOf(value).replace('\u0007', ',');
        }
        return result;
    }

    /**
     * @Author Liuxg
     * @Description 根据JFESOReview获取相关信息
     * @Date 2025/8/5 1:45
     * @Param [context, args]
     * @return com.alibaba.fastjson.JSONObject
    **/
    public JSONObject getESOStateTaskInfo(Context context, String JFESOReviewid)throws Exception{
        JSONObject jsonObject=new JSONObject();
        try {
            //通过ESOR获取关联的ESO任务，以及关联的phase和project
            //JFESOReview

            //获取ESO相关的属性，和当前的
            DomainObject ESORobj=DomainObject.newInstance(context,JFESOReviewid);

            StringList attrlist=new StringList();
            attrlist.add("attribute[JF_ReviewCounte]");
            attrlist.add("attribute[JF_EstimatedSignDate]");
            attrlist.add("attribute[JF_ActualSignDate]");
            attrlist.add("attribute[JF_PhaseState]");
            //定点时间取什么？
            attrlist.add("attribute[JF_SouringDate]");
            Map EsoRAtttMap = ESORobj.getInfo(context, attrlist);

            //ESO任务
            String ESOTaskid=ESORobj.getInfo(context,"to[JFESOTask2ESOReview].from.id");
            DomainObject ESOTaskObj=DomainObject.newInstance(context,ESOTaskid);
            StringList esoTaskattrlist=new StringList();
            esoTaskattrlist.add("attribute[JF_Function]");
            esoTaskattrlist.add("attribute[JF_Locations]");
            esoTaskattrlist.add("attribute[JF_Department]");
            esoTaskattrlist.add("attribute[JF_Modules]");
            esoTaskattrlist.add("attribute[JF_ESOType]");
            esoTaskattrlist.add("attribute[JF_Rows]");
            esoTaskattrlist.add("attribute[JF_R2_TKO]");
            esoTaskattrlist.add(DomainConstants.SELECT_ID);
            esoTaskattrlist.add(DomainConstants.SELECT_TYPE);



            Map EsoTaskAtttMap = ESOTaskObj.getInfo(context, esoTaskattrlist);
            String phaseName=getPhaseName(context,ESOTaskid);

            //项目
            String projectid=ESOTaskObj.getInfo(context,"to[Project Access Key].from.from[Project Access List].to.id");
            DomainObject projectobj=DomainObject.newInstance(context,projectid);
            StringList projectAttrlist=new StringList();
            projectAttrlist.add(DomainConstants.SELECT_NAME);
            projectAttrlist.add(DomainConstants.SELECT_DESCRIPTION);
            projectAttrlist.add("attribute[JF_ProjectCode]");
            projectAttrlist.add("attribute[JF_ProjectTag]");
            projectAttrlist.add("attribute[JF_ProjectStatus]");
            projectAttrlist.add("attribute[JF_ProjectECI]");
            projectAttrlist.add("attribute[JF_ProjectGrade]");
            projectAttrlist.add("attribute[JF_Phase1PlannedCompletionTime]");
            projectAttrlist.add("attribute[JF_Phase2PlannedCompletionTime]");
            projectAttrlist.add("attribute[JF_Phase3PlannedCompletionTime]");
            projectAttrlist.add("attribute[JF_Phase4PlannedCompletionTime]");
            projectAttrlist.add("attribute[JF_Phase5PlannedCompletionTime]");
            projectAttrlist.add("attribute[JF_Phase2_3PlannedCompletionTime]");

            Map ProjectMap=projectobj.getInfo(context,projectAttrlist);
            Boolean noDVFlag = JF_ESO_mxJPO.projectSpaceHasDVFlag(context, projectobj);

            jsonObject.put("ProjectID",ProjectMap.get(DomainConstants.SELECT_NAME));
            jsonObject.put("ProjectName",ProjectMap.get("attribute[JF_ProjectCode]"));
            //mod by liuxg 项目代号取描述
            jsonObject.put("ProjectTag",ProjectMap.get(DomainConstants.SELECT_DESCRIPTION));
            jsonObject.put("ProjectState",ProjectMap.get("attribute[JF_ProjectStatus]"));
            jsonObject.put("ECInfo",ProjectMap.get("attribute[JF_ProjectECI]"));
            jsonObject.put("Grade",ProjectMap.get("attribute[JF_ProjectGrade]"));
            jsonObject.put("Phase1Date",ProjectMap.get("attribute[JF_Phase1PlannedCompletionTime]"));
            if (noDVFlag) {
                jsonObject.put("Phase2+3Date", ProjectMap.get("attribute[JF_Phase2_3PlannedCompletionTime]"));
            } else {
                jsonObject.put("Phase2Date", ProjectMap.get("attribute[JF_Phase2PlannedCompletionTime]"));
                jsonObject.put("Phase3Date", ProjectMap.get("attribute[JF_Phase3PlannedCompletionTime]"));
            }
            jsonObject.put("Phase4Date",ProjectMap.get("attribute[JF_Phase4PlannedCompletionTime]"));
            jsonObject.put("Phase5Date",ProjectMap.get("attribute[JF_Phase5PlannedCompletionTime]"));


            jsonObject.put("Function",EsoTaskAtttMap.get("attribute[JF_Function]"));
            jsonObject.put("Location",EsoTaskAtttMap.get("attribute[JF_Locations]"));
            jsonObject.put("Department",EsoTaskAtttMap.get("attribute[JF_Department]"));
            jsonObject.put("Module",EsoTaskAtttMap.get("attribute[JF_Modules]"));
            jsonObject.put("Type",EsoTaskAtttMap.get("attribute[JF_ESOType]"));
            jsonObject.put("Rows",EsoTaskAtttMap.get("attribute[JF_Rows]"));
            jsonObject.put("TKO",EsoTaskAtttMap.get("attribute[JF_R2_TKO]"));

            jsonObject.put("PhaseName",phaseName);

            jsonObject.put("ReviewCount",EsoRAtttMap.get("attribute[JF_ReviewCounte]"));
            jsonObject.put("EstimatedSignDate",EsoRAtttMap.get("attribute[JF_EstimatedSignDate]"));
            jsonObject.put("ActualSignDate",EsoRAtttMap.get("attribute[JF_ActualSignDate]"));
            jsonObject.put("PhaseStatus",EsoRAtttMap.get("attribute[JF_PhaseState]"));
            jsonObject.put("SouringDate",EsoRAtttMap.get("attribute[JF_SouringDate]"));
            String modif=ESORobj.getModified(context);
            jsonObject.put("LatestModifyTime",modif);

        }catch (Exception e){
            e.printStackTrace();
        }

        return jsonObject;
    }

    /**
     * @Author Liuxg
     * @Description ESOR流程完成后发送BI，同步按钮也可以批量调用此方法
     * @Date 2025/9/25 23:17
     * @Param [context, args]
     * @return void
    **/
    public String  triggerSendESOR2BI(Context context,String []args)throws Exception{
        String ESORid=args[0];
        JSONObject data=getESOStateTaskInfo(context,ESORid);
        //

        log.info("triggerSendESOR2BI----->"+data.toString());
        Map headerMap=new HashMap();
        headerMap.put("jf_svc","IOA0173");
        headerMap.put("jf_applicationid",UUID.randomUUID()+"");
        headerMap.put("jf_sender","PLM");
        headerMap.put("jf_receiver","BI");
        headerMap.put("jf_document",UUID.randomUUID()+"");


        String type="";
        //检查是否测试环境。
        String JFUrl = JF_PublicMethodClass_mxJPO.getBasicUrl(context, new String[]{"JF.3dspace.JFUrl"});
        log.info("JFUrl---->"+JFUrl);
        if(JFUrl.contains("plmsit")){
            //测试环境接口已经关闭，不处理。
        }else {
            //
//            String url="http://172.16.33.183:7080/JFSEATesb/Services/PLM/ServiceESOSignoff";
            String url = JF_PublicMethodClass_mxJPO.getBasicUrl(context, new String[]{"JFSyncESO.ESB.URL"});
            String resStr=dopost(data.toString(),url,headerMap);
            JSONObject resultjson=JSONObject.parseObject(resStr);
            JSONObject datajson=resultjson.getJSONObject("DATA");
            type =datajson.getString("TYPE");
        }

        return type;

    }


    /**
     * @Author Liuxg
     * @Description 同步ESOR数据到
     * @Date 2025/9/26 0:09
     * @Param [context, args]
     * @return void
    **/
    public String CommandBatchSendESOR2BI(Context context,String []args)throws Exception{
        String result="T";
        try {
            Map prammap=JPO.unpackArgs(args);
            StringList idlist= (StringList) prammap.get("idlist");
            log.info("idlist--->"+idlist);
            log.info("idlist--->"+idlist.size());
            for(String id:idlist){
               String resStr= triggerSendESOR2BI(context,new String[]{id});
               if(!"S".equals(resStr)){
                   result="F";
               }
            }
        }catch (Exception e){
            e.printStackTrace();
            result="F";
        }
        return result;
    }




    public static String dopost(String json,String url,Map headerMap)throws Exception{
        String result="";
        // 创建HTTP客户端
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            // 创建POST请求
            HttpPost post = new HttpPost(url);

            if (headerMap != null && !headerMap.isEmpty()) {
                Set keySet = headerMap.keySet();
                Iterator iterator = keySet.iterator();
                while (iterator.hasNext()) {
                    String key = (String) iterator.next();
                    String value = (String) headerMap.get(key);
//                    log.info("headerMap---key>"+key);
//                    log.info("headerMap---value>"+value);
                    post.setHeader(key, value);
                }
            }

            //JSONObject JSON=JSONObject.parseObject(json);
            //log.info("JSON: " + JSON.get("Head"));


            // 设置JSON数据
            post.setEntity(new StringEntity(json, "UTF-8"));
//            post.setHeader("Content-Type", "application/json");
            post.setHeader("Content-Type", "application/json;charset=UTF-8");
            // 发送请求并获取响应
            log.info("execute: start" );
            CloseableHttpResponse httpResponse = client.execute(post);
            log.info("execute: end" );
            try {
                // 读取响应内容
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(httpResponse.getEntity().getContent()));

                // 传统方式读取响应
                StringBuilder responseBuilder = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    responseBuilder.append(line).append("\n");
                }

                String response = responseBuilder.toString().trim();
                log.info("Response: " + response);
                result=response;
            } finally {
                // 确保关闭响应
                httpResponse.close();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return result;
    }



    /**
     * @Author Liuxg
     * @Description 根据项目获取项目下的所有ESO信息
     * @Date 2025/8/13 22:43
     * @Param [context, args]
     * @return com.alibaba.fastjson.JSONObject
    **/
    public JSONArray getAllESOInfoByProject(Context context,MapList mlProject)throws Exception{

        log.info("getAllESOInfoByProject--->"+mlProject);

        JSONArray returnJosn=new JSONArray();
        try {

            StringList busSelects=new StringList();
            busSelects.add(DomainConstants.SELECT_ID);

            if(mlProject.size()>0){
                for(int p=0;p<mlProject.size();p++){
                    //结构改造
//                    JSONObject projectjson=new JSONObject();
//                    JSONArray projectData=new JSONArray();

                    Map projectMap= (Map) mlProject.get(p);
                    String projectid= (String) projectMap.get(DomainConstants.SELECT_ID);
                    DomainObject projectObj=DomainObject.newInstance(context,projectid);
                    Boolean noDVFlag = JF_ESO_mxJPO.projectSpaceHasDVFlag(context, projectObj);

//                    projectjson.put("ProjectID",projectObj.getName(context));

                    StringList projectAttrlist=new StringList();
                    projectAttrlist.add(DomainConstants.SELECT_NAME);
                    projectAttrlist.add(DomainConstants.SELECT_DESCRIPTION);
                    projectAttrlist.add("attribute[JF_ProjectCode]");
                    projectAttrlist.add("attribute[JF_ProjectTag]");
                    projectAttrlist.add("attribute[JF_ProjectStatus]");
                    projectAttrlist.add("attribute[JF_ProjectECI]");
                    projectAttrlist.add("attribute[JF_ProjectGrade]");
                    projectAttrlist.add("attribute[JF_Phase1PlannedCompletionTime]");
                    projectAttrlist.add("attribute[JF_Phase2PlannedCompletionTime]");
                    projectAttrlist.add("attribute[JF_Phase3PlannedCompletionTime]");
                    projectAttrlist.add("attribute[JF_Phase4PlannedCompletionTime]");
                    projectAttrlist.add("attribute[JF_Phase5PlannedCompletionTime]");
                    projectAttrlist.add("attribute[JF_Phase2_3PlannedCompletionTime]");

                    Map ProjectMap=projectObj.getInfo(context,projectAttrlist);
                    //获取项目下的所有任务，并筛选出ESO，和其下的ESO审核状态

                    StringList esoTaskattrlist=new StringList();
                    esoTaskattrlist.add("attribute[JF_Function]");
                    esoTaskattrlist.add("attribute[JF_Locations]");
                    esoTaskattrlist.add("attribute[JF_Department]");
                    esoTaskattrlist.add("attribute[JF_Modules]");
                    esoTaskattrlist.add("attribute[JF_ESOType]");
                    esoTaskattrlist.add("attribute[JF_Rows]");
                    esoTaskattrlist.add("attribute[JF_R2_TKO]");
                    esoTaskattrlist.add(DomainConstants.SELECT_ID);
                    esoTaskattrlist.add(DomainConstants.SELECT_TYPE);

                    String PALid=projectObj.getInfo(context,"to[Project Access List].from.id");
                    DomainObject PALobj=DomainObject.newInstance(context,PALid);
                    MapList mapList = PALobj.getRelatedObjects(context, "Project Access Key", "JF_ESOTask", esoTaskattrlist, null,
                            false, true, (short) 1,
                            "", "", 0);
                    for(int i=0;i<mapList.size();i++){
                        Map ESOTaskmap= (Map) mapList.get(i);
                        String ESOTaskid= (String) ESOTaskmap.get(DomainConstants.SELECT_ID);
                        log.info("ESOTaskid--->"+ESOTaskid);
                        DomainObject ESOTask=DomainObject.newInstance(context,ESOTaskid);
                        StringList ESORidlist=ESOTask.getInfoList(context,"from[JFESOTask2ESOReview].to.id");
                        //往上一直查到phase为止，取name
                        String phaseName=getPhaseName(context,ESOTaskid);

                        Map EsoTaskAtttMap = ESOTask.getInfo(context, esoTaskattrlist);

                        for(String ESORid:ESORidlist){
                            log.info("ESORid---->"+ESORid);
                            DomainObject ESORobj=DomainObject.newInstance(context,ESORid);
                            String modif=ESORobj.getModified(context);
                            StringList attrlist=new StringList();
                            attrlist.add("attribute[JF_ReviewCounte]");
                            attrlist.add("attribute[JF_EstimatedSignDate]");
                            attrlist.add("attribute[JF_ActualSignDate]");
                            attrlist.add("attribute[JF_PhaseState]");
                            attrlist.add("attribute[JF_SouringDate]");

                            //定点时间取什么？
                            attrlist.add("attribute[JF_SouringDate]");
                            Map EsoRAtttMap = ESORobj.getInfo(context, attrlist);

                            JSONObject jsonObject=new JSONObject();

                            jsonObject.put("ProjectID",ProjectMap.get(DomainConstants.SELECT_NAME));
                            jsonObject.put("ProjectName",ProjectMap.get("attribute[JF_ProjectCode]"));
                            //mod by liuxg 项目代号取描述
                            jsonObject.put("ProjectTag",ProjectMap.get(DomainConstants.SELECT_DESCRIPTION));
                            jsonObject.put("ProjectState",ProjectMap.get("attribute[JF_ProjectStatus]"));
                            jsonObject.put("ECInfo",ProjectMap.get("attribute[JF_ProjectECI]"));
                            jsonObject.put("Grade",ProjectMap.get("attribute[JF_ProjectGrade]"));
                            jsonObject.put("Phase1Date",ProjectMap.get("attribute[JF_Phase1PlannedCompletionTime]"));
                            if (noDVFlag) {
                                jsonObject.put("Phase2+3Date", ProjectMap.get("attribute[JF_Phase2_3PlannedCompletionTime]"));
                            } else {
                                jsonObject.put("Phase2Date",ProjectMap.get("attribute[JF_Phase2PlannedCompletionTime]"));
                                jsonObject.put("Phase3Date",ProjectMap.get("attribute[JF_Phase3PlannedCompletionTime]"));
                            }
                            jsonObject.put("Phase4Date",ProjectMap.get("attribute[JF_Phase4PlannedCompletionTime]"));
                            jsonObject.put("Phase5Date",ProjectMap.get("attribute[JF_Phase5PlannedCompletionTime]"));

                            jsonObject.put("Function",EsoTaskAtttMap.get("attribute[JF_Function]"));
                            jsonObject.put("Location",EsoTaskAtttMap.get("attribute[JF_Locations]"));
                            jsonObject.put("Department",EsoTaskAtttMap.get("attribute[JF_Department]"));
                            jsonObject.put("Module",EsoTaskAtttMap.get("attribute[JF_Modules]"));
                            jsonObject.put("Type",EsoTaskAtttMap.get("attribute[JF_ESOType]"));
                            jsonObject.put("Rows",EsoTaskAtttMap.get("attribute[JF_Rows]"));
                            jsonObject.put("TKO",EsoTaskAtttMap.get("attribute[JF_R2_TKO]"));

                            jsonObject.put("PhaseName",phaseName);

                            jsonObject.put("ReviewCount",EsoRAtttMap.get("attribute[JF_ReviewCounte]"));
                            jsonObject.put("EstimatedSignDate",EsoRAtttMap.get("attribute[JF_EstimatedSignDate]"));
                            jsonObject.put("ActualSignDate",EsoRAtttMap.get("attribute[JF_ActualSignDate]"));
                            jsonObject.put("PhaseStatus",EsoRAtttMap.get("attribute[JF_PhaseState]"));
                            jsonObject.put("SouringDate",EsoRAtttMap.get("attribute[JF_SouringDate]"));
                            jsonObject.put("LatestModifyTime",modif);
//                            projectData.add(jsonObject);
                            returnJosn.add(jsonObject);
                        }

                    }
//                    projectjson.put("ESOReview",projectData);
//                    returnJosn.add(projectjson);
                }



            }else {
                //没有查询到对象
//                returnJosn.put("code","500");
//                returnJosn.put("msg","ProjectID error,project does not exits!");
//                returnJosn.put("result","failed");
            }

        }catch (Exception e){
            e.printStackTrace();

        }
        return returnJosn;
    }

    public String getPhaseName(Context context,String id)throws Exception{
        DomainObject subtask=DomainObject.newInstance(context,id);
        String parentType=subtask.getInfo(context,"to[Subtask].from.type");
        if(UIUtil.isNotNullAndNotEmpty(parentType)){
            if("Phase".equalsIgnoreCase(parentType)){
                String parentName=subtask.getInfo(context,"to[Subtask].from.name");
                return parentName;
            }else {
                String parentid=subtask.getInfo(context,"to[Subtask].from.id");
                return getPhaseName(context,parentid);
            }
        }else {
            //为空，到根节点了
            return "";
        }

    }



    public JSONObject getAllESOInfoByProject(Context context,String projectid)throws Exception{

        JSONObject projectjson=new JSONObject();

        try {

            StringList busSelects=new StringList();
            busSelects.add(DomainConstants.SELECT_ID);


                    //结构改造

                    JSONArray projectData=new JSONArray();


                    DomainObject projectObj=DomainObject.newInstance(context,projectid);
                    Boolean noDVFlag = JF_ESO_mxJPO.projectSpaceHasDVFlag(context, projectObj);

                    projectjson.put("ProjectID",projectObj.getName(context));

                    StringList projectAttrlist=new StringList();
                    projectAttrlist.add(DomainConstants.SELECT_NAME);
                    projectAttrlist.add("attribute[JF_ProjectCode]");
                    projectAttrlist.add("attribute[JF_ProjectTag]");
                    projectAttrlist.add("attribute[JF_ProjectStatus]");
                    projectAttrlist.add("attribute[JF_ProjectECI]");
                    projectAttrlist.add("attribute[JF_ProjectGrade]");
                    projectAttrlist.add("attribute[JF_Phase1PlannedCompletionTime]");
                    projectAttrlist.add("attribute[JF_Phase2PlannedCompletionTime]");
                    projectAttrlist.add("attribute[JF_Phase3PlannedCompletionTime]");
                    projectAttrlist.add("attribute[JF_Phase4PlannedCompletionTime]");
                    projectAttrlist.add("attribute[JF_Phase5PlannedCompletionTime]");
                    projectAttrlist.add("attribute[JF_Phase2_3PlannedCompletionTime]");

                    Map ProjectMap=projectObj.getInfo(context,projectAttrlist);
                    //获取项目下的所有任务，并筛选出ESO，和其下的ESO审核状态

                    StringList esoTaskattrlist=new StringList();
                    esoTaskattrlist.add("attribute[JF_Function]");
                    esoTaskattrlist.add("attribute[JF_Locations]");
                    esoTaskattrlist.add("attribute[JF_Department]");
                    esoTaskattrlist.add("attribute[JF_Modules]");
                    esoTaskattrlist.add("attribute[JF_ESOType]");
                    esoTaskattrlist.add(DomainConstants.SELECT_ID);
                    esoTaskattrlist.add(DomainConstants.SELECT_TYPE);

                    String PALid=projectObj.getInfo(context,"to[Project Access List].from.id");
                    DomainObject PALobj=DomainObject.newInstance(context,PALid);
                    MapList mapList = PALobj.getRelatedObjects(context, "Project Access Key", "JF_ESOTask", esoTaskattrlist, null,
                            false, true, (short) 1,
                            "", "", 0);
                    for(int i=0;i<mapList.size();i++){
                        Map ESOTaskmap= (Map) mapList.get(i);
                        String ESOTaskid= (String) ESOTaskmap.get(DomainConstants.SELECT_ID);
                        DomainObject ESOTask=DomainObject.newInstance(context,ESOTaskid);
                        StringList ESORidlist=ESOTask.getInfoList(context,"from[JFESOTask2ESOReview].to.id");
                        String phaseName=ESOTask.getInfo(context,"to[Subtask].from.name");
                        Map EsoTaskAtttMap = ESOTask.getInfo(context, esoTaskattrlist);

                        for(String ESORid:ESORidlist){
                            DomainObject ESORobj=DomainObject.newInstance(context,ESORid);

                            StringList attrlist=new StringList();
                            attrlist.add("attribute[JF_ReviewCounte]");
                            attrlist.add("attribute[JF_EstimatedSignDate]");
                            attrlist.add("attribute[JF_ActualSignDate]");
                            attrlist.add("attribute[JF_PhaseState]");
                            //定点时间取什么？
                            attrlist.add("attribute[JF_SouringDate]");
                            Map EsoRAtttMap = ESORobj.getInfo(context, attrlist);
                            JSONObject jsonObject=new JSONObject();

                            jsonObject.put("ProjectID",ProjectMap.get(DomainConstants.SELECT_NAME));
                            jsonObject.put("ProjectName",ProjectMap.get("attribute[JF_ProjectCode]"));
                            jsonObject.put("ProjectTag",ProjectMap.get("attribute[JF_ProjectTag]"));
                            jsonObject.put("ProjectState",ProjectMap.get("attribute[JF_ProjectStatus]"));
                            jsonObject.put("ECInfo",ProjectMap.get("attribute[JF_ProjectECI]"));
                            jsonObject.put("Grade",ProjectMap.get("attribute[JF_ProjectGrade]"));
                            jsonObject.put("Phase1Date",ProjectMap.get("attribute[JF_Phase1PlannedCompletionTime]"));
                            if (noDVFlag) {
                                jsonObject.put("Phase2+3Date", ProjectMap.get("attribute[JF_Phase2_3PlannedCompletionTime]"));
                            } else {
                                jsonObject.put("Phase2Date", ProjectMap.get("attribute[JF_Phase2PlannedCompletionTime]"));
                                jsonObject.put("Phase3Date", ProjectMap.get("attribute[JF_Phase3PlannedCompletionTime]"));
                            }
                            jsonObject.put("Phase4Date",ProjectMap.get("attribute[JF_Phase4PlannedCompletionTime]"));
                            jsonObject.put("Phase5Date",ProjectMap.get("attribute[JF_Phase5PlannedCompletionTime]"));

                            jsonObject.put("Function",EsoTaskAtttMap.get("attribute[JF_Function]"));
                            jsonObject.put("Location",EsoTaskAtttMap.get("attribute[JF_Locations]"));
                            jsonObject.put("Department",EsoTaskAtttMap.get("attribute[JF_Department]"));
                            jsonObject.put("Module",EsoTaskAtttMap.get("attribute[JF_Modules]"));
                            jsonObject.put("Type",EsoTaskAtttMap.get("attribute[JF_ESOType]"));

                            jsonObject.put("PhaseName",phaseName);

                            jsonObject.put("ReviewCount",EsoRAtttMap.get("attribute[JF_ReviewCounte]"));
                            jsonObject.put("EstimatedSignDate",EsoRAtttMap.get("attribute[JF_EstimatedSignDate]"));
                            jsonObject.put("ActualSignDate",EsoRAtttMap.get("attribute[JF_ActualSignDate]"));
                            jsonObject.put("PhaseStatus",EsoRAtttMap.get("attribute[JF_PhaseState]"));
                            jsonObject.put("SouringDate",EsoRAtttMap.get("attribute[JF_SouringDate]"));
                            projectData.add(jsonObject);
                            //returnJosn.add(jsonObject);
                        }

                    }
                    projectjson.put("data",projectData);



        }catch (Exception e){
            e.printStackTrace();

        }
        return projectjson;
    }



    /**
     * @Author Liuxg
     * @Description BI获取esor全量数据接口
     * @Date 2025/8/13 23:31
     * @Param [context, args]
     * @return com.alibaba.fastjson.JSONObject
    **/
    public JSONObject getAllESOInfoByALLProject(Context context,String[]args)throws Exception{
        JSONArray jsonArray=new JSONArray();
        JSONObject resultjson=new JSONObject();
        try {
            HashMap programMap = (HashMap) JPO.unpackArgs(args);
//            String  projectName = (String) programMap.get("ProjectID");

            ArrayList  projectNames = (ArrayList) programMap.get("ProjectID");
            log.info("ProjectID--->"+projectNames);

            StringList pnamelist=new StringList(projectNames);
            log.info("pnamelist--->"+pnamelist);


            StringList busSelects=new StringList();
            busSelects.add(DomainConstants.SELECT_ID);
            MapList mlProject=new MapList();

            if(pnamelist.size()>0){

                for (int i=0;i<pnamelist.size();i++){
                    String projectName=pnamelist.get(i);
                    System.out.println("projectName----->"+projectName);
                    MapList singlProject = DomainObject.findObjects(context, "Project Space", // type filter
                            null, // vault filter
                            "name == '" + projectName + "'", // where clause
                            busSelects);
                    System.out.println("singlProject----->"+singlProject.size());
                    if(singlProject.size()>0){
                        mlProject.addAll(singlProject);
//                    //正确数据,应该有且只有一个
//                        for(int x=0;x<singlProject.size();x++){
//                            Map projectMap= (Map) singlProject.get(x);
//                            String proid= (String) projectMap.get(DomainConstants.SELECT_ID);
//                            JSONObject projson=getAllESOInfoByProject(context,proid);
//                            jsonArray.add(projson);
//                        }
//
//                    }else {
//                        JSONObject projson=new JSONObject();
//                        projson.put("ProjectID",projectName);
//                        projson.put("data",new JSONArray());
//                        jsonArray.add(projson);
                    }
                }

            }else{
                 mlProject = DomainObject.findObjects(context, "Project Space", // type filter
                        null, // vault filter
                        null, // where clause
                        busSelects);

//                for(int i=0;i<mlProject.size();i++){
//                    Map projectMap= (Map) mlProject.get(i);
//                    String proid= (String) projectMap.get(DomainConstants.SELECT_ID);
//                    JSONObject projson=getAllESOInfoByProject(context,proid);
//                    jsonArray.add(projson);
//                }
            }




            resultjson.put("data", getAllESOInfoByProject(context,mlProject));
            resultjson.put("code","200");
            resultjson.put("msg","");
            resultjson.put("result","success");
        }catch (Exception e){
            e.printStackTrace();
            resultjson.put("code","500");
            resultjson.put("msg",e.getMessage());
            resultjson.put("result","failed");
        }
        return resultjson;
    }


    /**
     * @Author Liuxg
     * @Description trigger触发partlist提升到审核后同步物料信息到BI(需要确认是否是partlist)
     * @Date 2025/8/11 23:10
     * @Param [context, args]
     * @return void
    **/
    public void triggerSendPartlistToBI(Context context,String []args)throws Exception{
        JSONObject postjson=new JSONObject();
        JSONArray array=new JSONArray();
        try {
            String partlistid=args[0];

            array=getPartlistVPMinfo(context,partlistid);

            postjson.put("data",array);
            log.info("data:"+postjson.toString());
            //需要提供对应的url地址喝header信息

            Map headerMap=new HashMap();
            headerMap.put("jf_svc","IOA0177");
            headerMap.put("jf_applicationid",UUID.randomUUID()+"");
            headerMap.put("jf_sender","PLM");
            headerMap.put("jf_receiver","SRM");
            headerMap.put("jf_document",UUID.randomUUID()+"");

//            String url="http://172.16.33.183:7080/JFSEATesb/Services/ServiceProjectInfo";
//            String url="http://172.16.33.183:7080/JFSEATesb/Services/SRM/ItemMaster";
            String url = JF_PublicMethodClass_mxJPO.getBasicUrl(context, new String[]{"JFSyncSRMPart.ESB.URL"});
            dopost(postjson.toString(),url,headerMap);


        }catch (Exception e){
            e.printStackTrace();
        }

    }

    /**
     * @Author Liuxg
     * @Description 获取partlist下各物料信息
     * @Date 2025/8/24 22:15
     * @Param [context, partlistid]
     * @return com.alibaba.fastjson.JSONArray
    **/
    public JSONArray getPartlistVPMinfo(Context context,String partlistid)throws Exception{
        JSONArray array=new JSONArray();
        try {

            DomainObject partlistobj=DomainObject.newInstance(context,partlistid);
            //获取所有零件
            StringList VPMidlist=partlistobj.getInfoList(context,"from[JFPartList2VPMReference].to.id");
            StringList bul=getSelectPartAttrList();
            for(String vpmid:VPMidlist){
                JSONObject vpmjson=new JSONObject();
                //是否需要零件下的子零件？
                DomainObject vpmobj=DomainObject.newInstance(context,vpmid);
                Map vpmmap=vpmobj.getInfo(context,bul);

                vpmjson.put("PartNumber",vpmmap.get("attribute[EnterpriseExtension.V_PartNumber]"));
                vpmjson.put("PartNameCN",vpmmap.get("attribute[JF_VPMReference.JF_PartNameCN]"));
                vpmjson.put("Unit",vpmmap.get("attribute[JF_VPMReference.JF_Unit]"));
                //默认1
                vpmjson.put("enabledFlag",1);
                //默认PLM
                vpmjson.put("sourceCode","PLM");
                String rangeI18NString = EnoviaResourceBundle.getRangeI18NString(context, "JF_PartType", vpmmap.get("attribute[JF_VPMReference.JF_PartType]").toString(), context.getSession().getLanguage());

                vpmjson.put("PartType",rangeI18NString);



                //默认0
                vpmjson.put("defaultFlag",0);
                vpmjson.put("PartNameEN",vpmmap.get("attribute[JF_VPMReference.JF_PartNameEN]"));
                vpmjson.put("Description",vpmmap.get(DomainConstants.SELECT_DESCRIPTION));

                array.add(vpmjson);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return array;
    }


    /**
     * @Author Liuxg
     * @Description 采购信息同步（接收SRM同步来的信息）（需要再明确是谁提供接口，目前功能描述，是PLM提供）
     * @Date 2025/8/18 2:59
     * @Param [context, args]
     * @return com.alibaba.fastjson.JSONObject
    **/
    public JSONObject getPurchaseInfoByBI(Context context,String[]args)throws Exception{
        JSONObject resultjson=new JSONObject();
        try {
            HashMap dataMap = (HashMap) JPO.unpackArgs(args);
//            Map  dataMap = (HashMap) programMap.get("data");
            //PartID	零件ID
            //SupplierSouringDate	定点实际完成日期
            //SouringRespName	定点负责人姓名
            //SouringSupCode	定点供应商编号
            //SouringSupName	定点供应商名称
            //BudgetUnitPrice	预算单价
            //Currency	币种
            String PartID= (String) dataMap.get("PartID");
            String SupplierSouringDate= (String) dataMap.get("SupplierSouringDate");
            String SouringRespName= (String) dataMap.get("SouringRespName");
            String SouringSupCode= (String) dataMap.get("SouringSupCode");
            String SouringSupName= (String) dataMap.get("SouringSupName");
            //只有这两个属性有，其余属性没有扩展
            String BudgetUnitPrice= (String) dataMap.get("BudgetUnitPrice");
            String Currency= (String) dataMap.get("Currency");
            //项目号+阶段号+part number+版本号,传过来的数据
            String []prtisstrs=PartID.split("#");
            if(prtisstrs.length<4){
                resultjson.put("code","500");
                resultjson.put("msg","PartID格式不正确，请检查");
                resultjson.put("result","error");
            }else{
                String projectName=prtisstrs[0];
                String phaseName=prtisstrs[1];
                String partName=prtisstrs[2];
                String partRev=prtisstrs[3];
                //根据信息查询，对应的partlist对象，以及该对象和vpm的关系（属性都在关系上）
                //此关系无限制，怎么确保找到的partlist是唯一的？
                //1，找到项目，2，找到vpm，3通过项目找到partlis，通过vpm找到partlist，两者比对，是同一个partlist（不唯一，怎么确保唯一）就是要找的对象，再找出关系
                StringList busSelects=new StringList();
                StringList relList=new StringList();
                busSelects.add(DomainConstants.SELECT_ID);
                relList.add(DomainRelationship.SELECT_ID);

                MapList mlProject = DomainObject.findObjects(context, "Project Space", // type filter
                        null, // vault filter
                        "name == '" + projectName + "'", // where clause
                        busSelects);
                //TNR确定，对象应该有且唯一
                MapList mlVPM = DomainObject.findObjects(context, "VPMReference", null,
                        "attribute[EnterpriseExtension.V_PartNumber]=='" + partName + "' && revision=='" + partRev + "'", busSelects);
                log.info("mlProject.size()--->"+mlProject.size());
                log.info("mlVPM.size()--->"+mlVPM.size());

                if(mlProject.size()>0){
                    Map projectmap= (Map) mlProject.get(0);
                    String projectid= (String) projectmap.get(DomainConstants.SELECT_ID);
                    //解决测试数据重复问题，正式环境不需要
                    if("35845.4994.18442.45465".equalsIgnoreCase(projectid)){
                         projectmap= (Map) mlProject.get(1);
                         projectid= (String) projectmap.get(DomainConstants.SELECT_ID);
                    }
                    DomainObject projectobj=DomainObject.newInstance(context,projectid);
                    StringList partlistidS=projectobj.getInfoList(context,"from[JFProject2PartList].to.id");
                    if(mlVPM.size()>0){
                        Map vpmmap= (Map) mlVPM.get(0);
                        String vpmid= (String) vpmmap.get(DomainConstants.SELECT_ID);
                        DomainObject vpmobj=DomainObject.newInstance(context,vpmid);
                        StringList partlistidS2=vpmobj.getInfoList(context,"to[JFPartList2VPMReference].from.id");
                        //取交集
                        partlistidS.retainAll(partlistidS2);

                        for(String partlistid:partlistidS){
                            DomainObject partlistObj=DomainObject.newInstance(context,partlistid);
                            String JFProjectPhase=partlistObj.getAttributeValue(context,"JFPartListType");
                            log.info("phaseName--->"+phaseName);
                            log.info("JFProjectPhase--->"+JFProjectPhase);
                            if(phaseName.equals(JFProjectPhase)){
                                //找到同项目阶段的partlist，更新属性值


                                StringList reIds = partlistObj.getInfoList(context, "from[JFPartList2VPMReference|to.id=='" + vpmid + "'].id", false);
                                log.info("reIds--->"+reIds);
                                if(reIds.size()>0){
                                    String reId=reIds.get(0);
                                    DomainRelationship ship=DomainRelationship.newInstance(context,reId);
                                    HashMap attrmap=new HashMap();

                                    attrmap.put("JFCurrency", Currency);
                                    attrmap.put("JFBudgetUnitPrice", BudgetUnitPrice);

                                    attrmap.put("JFSupplierSouringDate",transDate(SupplierSouringDate));
                                    attrmap.put("JFSouringRespName", SouringRespName);
                                    attrmap.put("JFSouringSupCode", SouringSupCode);
                                    attrmap.put("JFSouringSupName", SouringSupName);
                                    ship.setAttributeValues(context, attrmap);
                                }

                            }
                        }
                        resultjson.put("code","200");
                        resultjson.put("msg","success");
                        resultjson.put("result","success");
                    }else {
                        //未找到VPM
                        resultjson.put("code","500");
                        resultjson.put("msg","零件不存在");
                        resultjson.put("result","error");
                    }

                }else {
                    //未找到项目
                    resultjson.put("code","500");
                    resultjson.put("msg","项目不存在");
                    resultjson.put("result","error");
                }
            }




        }catch (Exception e){
            e.printStackTrace();
            resultjson.put("code","500");
            resultjson.put("msg",e.getMessage());
            resultjson.put("result","failed");
        }
        return resultjson;
    }

    public String transDate(String dateStr)throws Exception{
        // 解析输入格式
        SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = inputFormat.parse(dateStr);

        // 转换为目标格式
        SimpleDateFormat outputFormat = new SimpleDateFormat("M/d/yyyy hh:mm:ss a");
        String outputDate = outputFormat.format(date);
        return outputDate;
    }

    /**
     * @Author Liuxg
     * @Description 查询partlist在SRM同步状态
     * @Date 2025/8/18 23:36
     * @Param [context, ProjectID, PhaseID]
     * @return java.lang.String
    **/
    public String getSRMisSync(Context context,String ProjectID,String PhaseID)throws Exception{
        String flag="E";
        //SRM同步状态查询接口地址，待提供
        JSONObject jsonObject=new JSONObject();
        jsonObject.put("ProjectID",ProjectID);
        jsonObject.put("PhaseID",PhaseID);

        //具体header信息待提供
        Map headerMap=new HashMap();

        headerMap.put("jf_svc","IOA0178");
        headerMap.put("jf_applicationid",UUID.randomUUID()+"");
        headerMap.put("jf_sender","PLM");
        headerMap.put("jf_receiver","SRM");
        headerMap.put("jf_document",UUID.randomUUID()+"");

//        String url="http://172.16.33.183:7080/JFSEATesb/Services/SRM/PartListStatus";

        String url = JF_PublicMethodClass_mxJPO.getBasicUrl(context, new String[]{"JFSyncSRMCheckStatus.ESB.URL"});
        String result=dopost(jsonObject.toString(),url,headerMap);
        JSONObject resultjson=JSONObject.parseObject(result);

        log.info("resultjson--->"+resultjson);

        //restResponseDtlDTOList
        if(resultjson.containsKey("restResponseDtlDTOList")){
            JSONArray restResponseDtlDTOList= resultjson.getJSONArray("restResponseDtlDTOList");
            if(restResponseDtlDTOList.size()>0){
                JSONObject firstobj=restResponseDtlDTOList.getJSONObject(0);
                if(firstobj.containsKey("responseStatus")){
                    String responseStatus=firstobj.getString("responseStatus");
                    if("SUCCESS".equals(responseStatus)){
                        JSONObject resultobj=firstobj.getJSONObject("result");
                        String finalStr=resultobj.getString("result");
                        if("Y".equals(finalStr)){
                            flag= "S";
                        }else {
                            //不为Y就说明是没有询价单
                            flag="当前数据有进行中的询价";
                        }
                    }else {
                        //状态不为SUCCESS的时候取responseMessage的信息
                        String responseMessage=firstobj.getString("responseMessage");
                        flag=responseMessage;
                    }
                }else {
                    //没有responseStatus，应该算接口错误
                    flag="E";
                }
            }else {
                flag="E";
            }

        }else {
            flag="E";
        }

        return flag;
    }

    public void Test(Context context,String[]args)throws Exception{
        try {
//            String flag=getSRMisSync(context,"ACCS24-0022","ACCS24-0022#Pre-DV");
//            System.out.println("flag---->"+flag);

            String id="35845.4994.12666.59730";
            DomainObject domainObject=DomainObject.newInstance(context,id);
            String des=domainObject.getDescription(context);
            System.out.println(des);


        }catch (Exception e){
            e.printStackTrace();
        }
    }
    public void TestgetPartListinfoSendSRM(Context context,String[]args)throws Exception{
        try {
//            getPartListinfoSendSRM(context,new String[]{"35845.4994.36342.5706"});
//            getPartListinfoSendSRM(context,new String[]{"35845.4994.13839.9229"});
//            getPartListinfoSendSRM(context,new String[]{"35845.4994.13831.18954"});
//            JPO.invoke(context,"JF_ProjectSpace",null,"triggerSendPartlistToBI",new String[]{"35845.4994.16792.10497"});
            getPartListinfoSendSRM(context,new String[]{"35845.4994.16792.10497"});
        }catch (Exception e){
            e.printStackTrace();
        }
    }


    /**
     * @Author Liuxg
     * @Description 仅用于SRM通过代码同步没有partlist的项目信息
     * @Date 2025/12/17 18:01
     * @Param [context, args]
     * @return void
    **/
    public void getPartListinfoSendSRMForOnlyProject(Context context,String[]args){
        try {

            String projectid = args[0];
            DomainObject projectObj = DomainObject.newInstance(context, projectid);
            Map projectMap = projectObj.getInfo(context, getProjectAttrList());

            JSONObject projectjson = new JSONObject();
            projectjson.put("UUID", projectid);
            String ProjectID = ProjectAttrProcess(projectMap, DomainConstants.SELECT_NAME);
            projectjson.put("ProjectID", ProjectID);
            projectjson.put("ProjectName", ProjectAttrProcess(projectMap, DomainConstants.SELECT_DESCRIPTION));
            projectjson.put("ProductName", ProjectAttrProcess(projectMap, "attribute[JF_ProductName]"));
            projectjson.put("TargetSalesVolume", ProjectAttrProcess(projectMap, "attribute[JF_SalesTarget]"));
            projectjson.put("DirectCustomer", ProjectAttrProcess(projectMap, "attribute[JF_DirectCustomer]"));
            projectjson.put("OEM", ProjectAttrProcess(projectMap, "attribute[JF_ProjectCustomers]"));
            projectjson.put("VehicleModel", ProjectAttrProcess(projectMap, "attribute[JF_Model]"));
            projectjson.put("ProjectStartDate", transOOTBtoSRMDate(ProjectAttrProcess(projectMap, "attribute[JF_PSStartDate]")));
            projectjson.put("EndOfProduction", transOOTBtoSRMDate(ProjectAttrProcess(projectMap, "attribute[JF_ProjectEOP]")));
            projectjson.put("PartT0Time", transOOTBtoSRMDate(ProjectAttrProcess(projectMap, "attribute[JF_T0Date]")));
            projectjson.put("DesignVerificationTime", transOOTBtoSRMDate(ProjectAttrProcess(projectMap, "attribute[JF_DVDate]")));
            projectjson.put("ProcessVerificationTime", transOOTBtoSRMDate(ProjectAttrProcess(projectMap, "attribute[JF_PVDate]")));
            projectjson.put("StartOfProductionTime", transOOTBtoSRMDate(ProjectAttrProcess(projectMap, "attribute[JF_PSSOPDate]")));
            projectjson.put("ProductCatagory", ProjectAttrProcess(projectMap, "attribute[JF_ProductType]"));

            projectjson.put("FactoryCode", ProjectAttrProcess(projectMap, "attribute[JFAffectedFactory]"));

            String JF_ProducingArea = ProjectAttrProcess(projectMap, "attribute[JF_ProducingArea]");
            String JF_ProducingAreaCN = "";
            if (UIUtil.isNotNullAndNotEmpty(JF_ProducingArea)) {
                JF_ProducingAreaCN = EnoviaResourceBundle.getRangeI18NString(context, "JF_ProducingArea", JF_ProducingArea, context.getSession().getLanguage());
            }

            projectjson.put("ProducingArea", JF_ProducingAreaCN);
            //采购组织编码默认空
            projectjson.put("purchaseOrgCode", "");
            projectjson.put("optType", "UPDATE");
            projectjson.put("ProjectDescription", projectObj.getDescription(context));
            //mod by 1017
            projectjson.put("ProjectCycle", ProjectAttrProcess(projectMap, "attribute[JF_ProjectLifeCycle]"));
            projectjson.put("PaymentTerms4Molds", ProjectAttrProcess(projectMap, "attribute[JF_PaymentTerms4Molds]"));

            String ProjectManager=ProjectAttrProcess(projectMap, DomainConstants.SELECT_OWNER);

            projectjson.put("ProjectManager",ProjectManager);

            projectjson.put("PhaseID", "");


            StringList PreDVlist=new StringList();
            PreDVlist.add("SOP");
            PreDVlist.add("PV-TKO");
            PreDVlist.add("Pre-PV");
            PreDVlist.add("DV-TKO");
            PreDVlist.add("Pre-DV");

            JSONArray pvArray = new JSONArray();
            for(int pv=0;pv<PreDVlist.size();pv++){
                JSONObject Prephasejson = new JSONObject();
                String pvName=PreDVlist.get(pv);
                Prephasejson.put("PhaseID",ProjectID+"#"+pvName);
                Prephasejson.put("PhaseCode",pvName);
                Prephasejson.put("budgetAmount","");
                Prephasejson.put("ProjectManager",ProjectManager);
                Prephasejson.put("taskExplanation","");
                Prephasejson.put("ProjectID",ProjectID);
                pvArray.add(Prephasejson);
            }
            JSONArray memberlist = getProjectMemberInfo(context, projectid);

            JSONObject data=new JSONObject();
            data.put("Project", projectjson);
            data.put("Phase", pvArray);

            data.put("Roles",memberlist);

            data.put("PartList", new JSONObject());
            data.put("PartInfo", new JSONArray());
            log.info("getPartListinfoSendSRM--->" + data.toString());


            //需要提供url和header地址
            Map headerMap = new HashMap();
            headerMap.put("jf_svc", "IOA0186");
            headerMap.put("jf_applicationid", UUID.randomUUID() + "");
            headerMap.put("jf_sender", "PLM");
            headerMap.put("jf_receiver", "SRM");
            headerMap.put("jf_document", UUID.randomUUID() + "");

            String url = JF_PublicMethodClass_mxJPO.getBasicUrl(context, new String[]{"JFSyncSRMPartList.ESB.URL"});

            String result = dopost(data.toString(), url, headerMap);
            JSONObject response = JSONObject.parseObject(result);

            log.info("SRMresponse---->"+response);



        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * @Author Liuxg
     * @Description partlist同步SRM
     * @Date 2025/8/22 16:15
     * @Param [context, args]
     * @return void
    **/
    public JSONObject getPartListinfoSendSRM(Context context,String []args)throws Exception{
        JSONObject resultjson=new JSONObject();
        resultjson.put("code",200);
        try {
            String partlistid=args[0];
            log.info("partlistid---->"+partlistid);
            JSONObject data=new JSONObject();
            DomainObject partlistObj=DomainObject.newInstance(context,partlistid);
            StringList projectlist=partlistObj.getInfoList(context,"to[JFProject2PartList].from.id");

            log.info("partlistid---->"+partlistid);
            log.info("partlistid---->"+partlistid);
            log.info("partlistid---->"+partlistid);
            log.info("partlistid---->"+partlistid);
            log.info("partlistid---->"+partlistid);
            String strBasicUrl = JF_PublicMethodClass_mxJPO.getBasicUrl(context, new String[]{STRING_FILE_SHARE_PATH});

            if(projectlist.size()>0) {
                //应该有且只有一个project
                String projectid = projectlist.get(0);
                DomainObject projectObj = DomainObject.newInstance(context, projectid);
                Map projectMap = projectObj.getInfo(context, getProjectAttrList());

                JSONObject projectjson = new JSONObject();
                projectjson.put("UUID", projectid);
                String ProjectID = ProjectAttrProcess(projectMap, DomainConstants.SELECT_NAME);
                projectjson.put("ProjectID", ProjectID);
                projectjson.put("ProjectName", ProjectAttrProcess(projectMap, DomainConstants.SELECT_DESCRIPTION));
                projectjson.put("ProductName", ProjectAttrProcess(projectMap, "attribute[JF_ProductName]"));
                projectjson.put("TargetSalesVolume", ProjectAttrProcess(projectMap, "attribute[JF_SalesTarget]"));
                projectjson.put("DirectCustomer", ProjectAttrProcess(projectMap, "attribute[JF_DirectCustomer]"));
                projectjson.put("OEM", ProjectAttrProcess(projectMap, "attribute[JF_ProjectCustomers]"));
                projectjson.put("VehicleModel", ProjectAttrProcess(projectMap, "attribute[JF_Model]"));
                projectjson.put("ProjectStartDate", transOOTBtoSRMDate(ProjectAttrProcess(projectMap, "attribute[JF_PSStartDate]")));
                projectjson.put("EndOfProduction", transOOTBtoSRMDate(ProjectAttrProcess(projectMap, "attribute[JF_ProjectEOP]")));
                projectjson.put("PartT0Time", transOOTBtoSRMDate(ProjectAttrProcess(projectMap, "attribute[JF_T0Date]")));
                projectjson.put("DesignVerificationTime", transOOTBtoSRMDate(ProjectAttrProcess(projectMap, "attribute[JF_DVDate]")));
                projectjson.put("ProcessVerificationTime", transOOTBtoSRMDate(ProjectAttrProcess(projectMap, "attribute[JF_PVDate]")));
                projectjson.put("StartOfProductionTime", transOOTBtoSRMDate(ProjectAttrProcess(projectMap, "attribute[JF_PSSOPDate]")));
                projectjson.put("ProductCatagory", ProjectAttrProcess(projectMap, "attribute[JF_ProductType]"));

                projectjson.put("FactoryCode", ProjectAttrProcess(projectMap, "attribute[JFAffectedFactory]"));

                String JF_ProducingArea = ProjectAttrProcess(projectMap, "attribute[JF_ProducingArea]");
                String JF_ProducingAreaCN = "";
                if (UIUtil.isNotNullAndNotEmpty(JF_ProducingArea)) {
                    JF_ProducingAreaCN = EnoviaResourceBundle.getRangeI18NString(context, "JF_ProducingArea", JF_ProducingArea, context.getSession().getLanguage());
                }

                projectjson.put("ProducingArea", JF_ProducingAreaCN);
                //采购组织编码默认空
                projectjson.put("purchaseOrgCode", "");
                projectjson.put("optType", "UPDATE");
                projectjson.put("ProjectDescription", projectObj.getDescription(context));
                //mod by 1017
                projectjson.put("ProjectCycle", ProjectAttrProcess(projectMap, "attribute[JF_ProjectLifeCycle]"));
                projectjson.put("PaymentTerms4Molds", ProjectAttrProcess(projectMap, "attribute[JF_PaymentTerms4Molds]"));

                String ProjectManager=ProjectAttrProcess(projectMap, DomainConstants.SELECT_OWNER);
//                String ProjectManager="uhexy001";
                projectjson.put("ProjectManager",ProjectManager);
                //获取phase信息
                String JFPartListType = partlistObj.getAttributeValue(context, "JFPartListType");
//                JSONObject phasejson = new JSONObject();
//
                //放在project中的PhaseID（当前的）
                projectjson.put("PhaseID", ProjectID + "#" + JFPartListType);

//                projectjson.put("ProjectType", ProjectAttrProcess(projectMap, "attribute[JF_ProjType]"));
//                projectjson.put("CustomerLocation", ProjectAttrProcess(projectMap, "attribute[JF_CustomerLoc]"));

//                projectjson.put("PlantLocation", ProjectAttrProcess(projectMap, "attribute[JF_PlantLoc]"));
//                projectjson.put("AnnualSales", ProjectAttrProcess(projectMap, "attribute[JF_AnnualSales]"));
//                projectjson.put("MProductionYear", ProjectAttrProcess(projectMap, "attribute[JF_DateOfMassProduction]"));

                //mod 1017 新增5个阶段项(pre-dv)

                StringList PreDVlist=new StringList();
                PreDVlist.add("SOP");
                PreDVlist.add("PV-TKO");
                PreDVlist.add("Pre-PV");
                PreDVlist.add("DV-TKO");
                PreDVlist.add("Pre-DV");

                //                {
                //                    "taskNum": "ACCS0927-002#Pre-PV",
                //                    "taskName": "Pre-PV",
                //                    "principalUserCode": "uhexy001",
                //                    "budgetAmount": 999999999,
                //                    "taskExplanation": "",
                //                    "parentTaskNum": "ACCS0927-002"
                //                },
                JSONArray pvArray = new JSONArray();
                for(int pv=0;pv<PreDVlist.size();pv++){
                    JSONObject Prephasejson = new JSONObject();
                    String pvName=PreDVlist.get(pv);
                    Prephasejson.put("PhaseID",ProjectID+"#"+pvName);
                    Prephasejson.put("PhaseCode",pvName);
                    Prephasejson.put("budgetAmount","");
                    Prephasejson.put("ProjectManager",ProjectManager);
                    Prephasejson.put("taskExplanation","");
                    Prephasejson.put("ProjectID",ProjectID);
                    pvArray.add(Prephasejson);
                }

//                projectjson.put("sitfProjectTasks",pvArray);



//                phasejson.put("PhaseCode", JFPartListType);
//                phasejson.put("budgetAmount", "");
//                phasejson.put("taskExplanation", "");
//                phasejson.put("ProjectID", ProjectID);
//                //测试用
//                phasejson.put("ProjectManager", "uhexy001");


                //新增第一次同步检查,如果是第一次同步就跳过
                String datatypesync="E";
                if("admin_platform".equalsIgnoreCase(context.getUser())||checkFirstSyncSRM(context,projectid)){
                    datatypesync="S";
                }else {
                    //查询同步接口
                    try {
                        datatypesync = getSRMisSync(context, ProjectID, ProjectID + "#" + JFPartListType);
                    }catch (Exception e){
                        e.printStackTrace();
                        datatypesync="E";
                    }

                }

                log.info("datatypesync----->"+datatypesync);
                log.info("datatypesync----->"+datatypesync);
                log.info("datatypesync----->"+datatypesync);
                log.info("datatypesync----->"+datatypesync);
                log.info("datatypesync----->"+datatypesync);
                log.info("datatypesync----->"+datatypesync);
                log.info("datatypesync----->"+datatypesync);
                //如果
                if ("S".equals(datatypesync)) {

                    //获取成员信息
                    JSONArray memberlist = getProjectMemberInfo(context, projectid);
                    //测试数据
                    JSONArray memberlist2 = new JSONArray();

                    StringList namelist = new StringList();
//                    namelist.add("uzeny001");
//                    namelist.add("uzhuj003");
//                    namelist.add("uqiny002");
//                    namelist.add("uluoz002");
//                    namelist.add("uyuxc001");
//                    namelist.add("uzuow001");
//                    namelist.add("uchem002");
//                    namelist.add("uzhey002");
//
//                    for (int m = 0; m < memberlist.size(); m++) {
//                        JSONObject memMap = memberlist.getJSONObject(m);
//                        memMap.put("UserID", namelist.get(m));
//                        memberlist2.add(memMap);
//                        if (m == namelist.size() - 1) {
//                            break;
//                        }
//                    }

                    //获取partlist信息
                    JSONObject partlistjson = new JSONObject();
                    String PartListID = partlistObj.getName(context);
                    partlistjson.put("PartListID", PartListID);
                    partlistjson.put("PartListState", partlistObj.getCurrentState(context).getName());
                    partlistjson.put("PhaseName", JFPartListType);
                    partlistjson.put("PartListOwner", partlistObj.getOwner(context).getName());
//                partlistjson.put("PartListOwner","uhexy001");


                    //获取零件信息
//                StringList VPMidlist=partlistObj.getInfoList(context,"from[JFPartList2VPMReference].to.id");
                    StringList bul = getSelectPartAttrList();
                    StringList rell = getPartListRelAttrList();
                    JSONArray allpartArray = new JSONArray();

                    String strRelWhere = "attribute[JSdataProcessProgress]=='CAAProcessingComplete'&&attribute[JF_SyncStatus]=='unsynchronized'";
                    //测试用
                    //String strRelWhere = "";

                    MapList vpmList = partlistObj.getRelatedObjects(context, "JFPartList2VPMReference", "VPMReference", bul, rell,
                            false, true, (short) 1,
                            "", strRelWhere, 0);
                    //mod 新增获取partlist的ebom文件，如果

                    //获取生成EBOM文档
                    StringBuilder sbSelectMQL = new StringBuilder();
                    sbSelectMQL.append("print bus ");
                    sbSelectMQL.append(partlistid);
                    sbSelectMQL.append(" select from[Reference Document|attribute[Project Role]=='EBOMFile'].to.id dump");
                    String strEBOMDocId = MqlUtil.mqlCommand(context, Boolean.FALSE, sbSelectMQL.toString(), Boolean.TRUE);

                    boolean needDownload=true;
                    FileList allFileList=new FileList();


                    for (int i = 0; i < vpmList.size(); i++) {
                        Map vpmmap = (Map) vpmList.get(i);
                        JSONObject vpmjson = new JSONObject();
                        String vpmid = (String) vpmmap.get(DomainConstants.SELECT_ID);
                        DomainObject vpmobj = DomainObject.newInstance(context, vpmid);

//                    String PartNumber= (String) vpmmap.get(DomainConstants.SELECT_NAME);
                        String PartNumber = (String) vpmmap.get("attribute[EnterpriseExtension.V_PartNumber]");
                        String Revision = (String) vpmmap.get(DomainConstants.SELECT_REVISION);

                        vpmjson.put("PartID", ProjectID + "#" + JFPartListType + "#" + PartNumber + "#" + Revision);
                        vpmjson.put("PartNumber", PartNumber);
                        vpmjson.put("Revision", Revision);
                        vpmjson.put("PartNameCN", ProjectAttrProcess(vpmmap, "attribute[JF_VPMReference.JF_PartNameCN]"));
                        vpmjson.put("Description", ProjectAttrProcess(vpmmap, DomainConstants.SELECT_DESCRIPTION));
                        String JFCarryOver = ProjectAttrProcess(vpmmap, "attribute[JFCarryOver]");
                        if ("CarryOver".equalsIgnoreCase(JFCarryOver)) {
                            JFCarryOver = "co";
                        }
                        vpmjson.put("CarryOver", JFCarryOver);
                        String JF_PartType = ProjectAttrProcess(vpmmap, "attribute[JF_VPMReference.JF_PartType]");
                        String JF_PartTypeCN = "";
                        if (UIUtil.isNotNullAndNotEmpty(JF_PartType)) {
                            JF_PartTypeCN = EnoviaResourceBundle.getRangeI18NString(context, "JF_VPMReference.JF_PartType", JF_PartType, context.getSession().getLanguage());
                        }

                        vpmjson.put("PartType", JF_PartTypeCN);
                        //采购类型
//                        String JF_ProcurementType = ProjectAttrProcess(vpmmap, "attribute[JF_VPMReference.JF_ProcurementType]");
                        String JF_ProcurementType = ProjectAttrProcess(vpmmap, "attribute[JFProcurementType]");
                        if ("make".equalsIgnoreCase(JF_ProcurementType)) {
                            JF_ProcurementType = "Make";
                        } else if ("buy".equalsIgnoreCase(JF_ProcurementType)) {
                            JF_ProcurementType = "Buy";
                        } else if ("ICO".equalsIgnoreCase(JF_ProcurementType)) {
                            JF_ProcurementType = "ICO";
                        }

                        if ("Make".equalsIgnoreCase(JF_ProcurementType)){
                            continue;
                        }

                        vpmjson.put("PartCurementType", JF_ProcurementType);
                        vpmjson.put("IsDirectBuy", ProjectAttrProcess(vpmmap, "attribute[JF_VPMReference.JF_DirectBuy]"));
                        String JF_ProcurementGroupEN = ProjectAttrProcess(vpmmap, "attribute[JF_VPMReference.JF_ProcurementGroup]");
                        String rangeI18NString = JF_ProcurementGroupEN;
//                    if(UIUtil.isNotNullAndNotEmpty(JF_ProcurementGroupEN)){
//                        rangeI18NString = EnoviaResourceBundle.getRangeI18NString(context, "JF_VPMReference.JF_ProcurementGroup", JF_ProcurementGroupEN, context.getSession().getLanguage());
//                    }

                        vpmjson.put("ProcurementGroup", rangeI18NString);
                        vpmjson.put("Unit", ProjectAttrProcess(vpmmap, "attribute[JF_VPMReference.JF_Unit]"));
                        vpmjson.put("DrawingNumber", ProjectAttrProcess(vpmmap, "attribute[JF_VPMReference.JF_CustomerDrawingNumber]"));


                        vpmjson.put("Specifications", ProjectAttrProcess(vpmmap, "attribute[JFPartSpecifications]"));
                        //JFProportionConfiguration(实际未拓展，方案中存在于关系上)
                        vpmjson.put("ConfigurationRatio", "");
                        vpmjson.put("Usage", ProjectAttrProcess(vpmmap, "attribute[JFBicycleUsage]"));
                        vpmjson.put("TotalDemand", ProjectAttrProcess(vpmmap, "attribute[JFTotalDemand]"));
                        vpmjson.put("DeliveryDate", transOOTBtoSRMDate(ProjectAttrProcess(vpmmap, "attribute[JFDeliveryDate]")));
                        vpmjson.put("Currency", ProjectAttrProcess(vpmmap, "attribute[JFCurrency]"));
                        //vpmjson.put("ProductionLocation", ProjectAttrProcess(vpmmap, "attribute[JFOutputLocation]"));

                        String JFOutputLocation = ProjectAttrProcess(vpmmap, "attribute[JFOutputLocation]");
                        String JFOutputLocationCN = "";
                        if (UIUtil.isNotNullAndNotEmpty(JFOutputLocation)) {
                            JFOutputLocationCN = EnoviaResourceBundle.getRangeI18NString(context, "JFOutputLocation", JFOutputLocation, context.getSession().getLanguage());
                        }
                        vpmjson.put("ProductionLocation", JFOutputLocationCN);


                        //0912 不需要传
//                    vpmjson.put("BudgetUnitPrice",ProjectAttrProcess(vpmmap, "attribute[JFBudgetUnitPrice]"));
                        vpmjson.put("BudgetMoldCost", ProjectAttrProcess(vpmmap, "attribute[JFBudgetMoldFee]"));
                        vpmjson.put("BudgetExperimentCost", ProjectAttrProcess(vpmmap, "attribute[JFBudgetExperimentalFee]"));
                        //未拓展
                        vpmjson.put("BudgetPalletCosts", ProjectAttrProcess(vpmmap, ""));
                        vpmjson.put("BudgetInspectionToolCost", ProjectAttrProcess(vpmmap, "attribute[JFBudgetInspectionToolFee]"));
                        vpmjson.put("BudgetLeatherGrainCost", ProjectAttrProcess(vpmmap, "attribute[JFBudgetLeatherTextureFee]"));
                        //未明确哪个属性 属性说明
                        vpmjson.put("AttributeDescription", "");
                        vpmjson.put("ParentPartNumber", vpmobj.getInfo(context, "to[VPMInstance].from.attribute[EnterpriseExtension.V_PartNumber].value"));
                        vpmjson.put("DeliveryStage", ProjectID + "#" + ProjectAttrProcess(vpmmap, "attribute[JFDeliveryPhase]"));
                        //未明确哪个属性，疑似JF_VPMReference.JF_ProcurementType？
                        vpmjson.put("MakeBuy", "");
                        vpmjson.put("SSOWReleaseDate", transOOTBtoSRMDate(ProjectAttrProcess(vpmmap, "attribute[JFSSOWIssueDate]")));
                        vpmjson.put("DrawingPlanReleaseDate", transOOTBtoSRMDate(ProjectAttrProcess(vpmmap, "attribute[JFDrawDataCompletionPlanDate]")));
                        //未明确哪个属性 图纸数据发放实际完成日期,疑似未扩展
                        vpmjson.put("IsSuspend", ProjectAttrProcess(vpmmap, "attribute[JFStopUsingFlag]"));
                        vpmjson.put("T0DemandPlanTime", transOOTBtoSRMDate(ProjectAttrProcess(vpmmap, "attribute[JFPartRequirementPlanDate]")));

                        //规则是,
                        String phyid = ProjectAttrProcess(vpmmap, DomainConstants.SELECT_PHYSICAL_ID);
                        JSONArray filepatharray=new JSONArray();
                        if (UIUtil.isNotNullAndNotEmpty(phyid)) {
                            String fileSendData = strBasicUrl + PartListID + "_" + phyid + File.separator + STRING_SEND_DATA;

                            File srmfile=new File(fileSendData);
                            if (srmfile.exists() && srmfile.isDirectory()) {
                                //给出放入ebom文件的标记，如果放了就不再放了
                                //需要确保是自制件，
                                //文件件存在的时候将ebom文件放入
                               log.info("文件夹存在");

                                if(UIUtil.isNotNullAndNotEmpty(strEBOMDocId)&&needDownload){
                                    DomainObject ebomDocObj=DomainObject.newInstance(context,strEBOMDocId);
                                    allFileList=ebomDocObj.getFiles(context);
                                    if(allFileList.size()>0){
                                        ebomDocObj.checkoutFiles(context, false, null,allFileList, fileSendData);
                                    }

                                    needDownload=false;
                                }

                                vpmjson.put("FilePath", fileSendData);
                                File[] files = srmfile.listFiles();

                                for (File file : files) {
                                    log.info("文件路径: " + file.getAbsolutePath());
                                    filepatharray.add(file.getAbsolutePath());
                                }
                            }
                        }
                        vpmjson.put("FilePath", filepatharray);
                        allpartArray.add(vpmjson);
//                        if(!"Make".equalsIgnoreCase(JF_ProcurementType)){
//                            //make件不需要
//                            allpartArray.add(vpmjson);
//                        }


                    }

                    data.put("Project", projectjson);
                    data.put("Phase", pvArray);
                    //检查是否测试环境。
                    String JFUrl = JF_PublicMethodClass_mxJPO.getBasicUrl(context, new String[]{"JF.3dspace.JFUrl"});
                    if(JFUrl.contains("plmsit")){
                        data.put("Roles", memberlist2);
                    }else {
                        data.put("Roles",memberlist);
                    }
                    data.put("PartList", partlistjson);
                    data.put("PartInfo", allpartArray);
                    log.info("getPartListinfoSendSRM--->" + data.toString());
                    //需要提供url和header地址
                    Map headerMap = new HashMap();
                    headerMap.put("jf_svc", "IOA0186");
                    headerMap.put("jf_applicationid", UUID.randomUUID() + "");
                    headerMap.put("jf_sender", "PLM");
                    headerMap.put("jf_receiver", "SRM");
                    headerMap.put("jf_document", UUID.randomUUID() + "");

                    String url = JF_PublicMethodClass_mxJPO.getBasicUrl(context, new String[]{"JFSyncSRMPartList.ESB.URL"});
//                    String url = "http://172.16.33.183:7080/JFSEATesb/Services/SRM/SyncPartList";

                    String result = dopost(data.toString(), url, headerMap);
                    JSONObject response = JSONObject.parseObject(result);

                    JSONObject record=new JSONObject();
                    record.put("jsonStr",data);
                    record.put("response",response);

                    String strJsonPath = JF_PublicMethodClass_mxJPO.getBasicUrl(context, new String[]{"MBOMToMDM.JsonPath"});
                    String strTime = getTimeCuo();
                    writeJsonToFile(record, strJsonPath+"JFSendSRM_"+ProjectID+strTime+".json");

                    String responseFlag="E";
                    log.info("SRMresponse---->"+response);
                    if(response.containsKey("restResponseDtlDTOList")){
                        JSONArray restResponseDtlDTOList= response.getJSONArray("restResponseDtlDTOList");
                        if(restResponseDtlDTOList.size()>0){
                            JSONObject firstobj=restResponseDtlDTOList.getJSONObject(0);
                            if(firstobj.containsKey("responseStatus")){
                                String responseStatus=firstobj.getString("responseStatus");
                                if("SUCCESS".equals(responseStatus)){
                                    responseFlag="S";
                                }else {
                                    //取报错信息"responseMessage

                                    String responseMessage=firstobj.containsKey("responseMessage")?firstobj.getString("responseMessage"):"E";
                                    responseFlag=responseMessage;
                                }
                            }else {
                                responseFlag="E";
                            }
                        }else {
                            responseFlag="E";
                        }

                    }else {
                        responseFlag="E";
                    }



                    //成功后修改已同步零件属性
                    if ("S".equals(responseFlag)) {
                        //传输成功
                        StringList phyidlist = new StringList();
                        for (int i = 0; i < vpmList.size(); i++) {
                            //SELECT_PHYSICAL_ID
                            Map vpmmap = (Map) vpmList.get(i);
                            phyidlist.add((String) vpmmap.get(DomainConstants.SELECT_PHYSICAL_ID));
                        }
                        Map parmmap = new HashMap();
                        parmmap.put("id", partlistid);
                        parmmap.put("idlist", phyidlist);

                        //修改零件属性
                        JPO.invoke(context, "JF_PartList", null, "modifyPartListPartSynchronizedInvoke", JPO.packArgs(parmmap), null);
                        //设置partlist属性
                        setPartlistSate(context, partlistid);
                    } else {
                        //传输失败
                        String errorStr="";
                        if("E".equals(responseFlag)){
                            errorStr="接口调用失败，请联系管理员";
                        }else {
                            errorStr=responseFlag;
                        }
                        resultjson.put("code",500);
                        resultjson.put("msg",errorStr);
                        try {
                            ContextUtil.pushContext(context);
                            partlistObj.setAttributeValue(context, "JFSyncSRM", "fail");
                        }catch (Exception e2){
                            e2.printStackTrace();
                        }finally {
                            ContextUtil.popContext(context);
                        }

                    }
                    JPO.invoke(context, "JF_PartList", null, "problematicPartListData", new String[]{partlistid}, null);
                }else{

                    // “1）PLM中PartList中同步的项目阶段早于SRM中PartList中的阶段”，“ 2）PLM中PartList中同步的项目阶段在SRM中正在询价”，同步失败
                    //询价不通过给出提示
                    //需要是点击按钮的才会有提示,按钮多传一个值
//                    if(args.length>1){
                        String errorNotice="";
                        if("E".equals(datatypesync)){
                            errorNotice="接口调用失败，请联系管理员";
                        }else {
                            errorNotice=datatypesync;
                        }
                        resultjson.put("code",500);
                        resultjson.put("msg",errorNotice);
//                    }

                }
            }

        }catch (Exception e){
            e.printStackTrace();
            resultjson.put("code",500);
            resultjson.put("msg",e.getMessage());
        }
        return resultjson;
    }

    /**
     * @Author Liuxg
     * @Description 检查是否符合第一次同步SRM
     * @Date 2025/12/18 11:29
     * @Param [context, projectid]
     * @return boolean
    **/
    public boolean checkFirstSyncSRM(Context context,String projectid)throws Exception{
        boolean b=false;

        DomainObject projectObj=DomainObject.newInstance(context,projectid);
        //JFProject2PartList
//        StringList partlistidArray=projectObj.getInfoList(context,"from[JFProject2PartList].to.id");
        StringList bul=JF_Util_mxJPO.basicBolistSel();
        StringList rell=JF_Util_mxJPO.basicRellistSel();
        bul.add("attribute[JFSyncSRM]");
        String where="attribute[JFSyncSRM].value == 'synchronized' || attribute[JFSyncSRM].value== 'NotFullsynchronized' ";
        MapList partlistidArray = projectObj.getRelatedObjects(context, "JFProject2PartList", "JFPartList", bul, rell,
                false, true, (short) 1,
                where, "", 0);

        if(partlistidArray.size()>0){
            b=false;
        }

        log.info("----->partlistidArray---"+partlistidArray.size());
        return b;
    }
    public String transOOTBtoSRMDate(String dateStr)throws Exception{
        // 解析输入格式
        if(UIUtil.isNotNullAndNotEmpty(dateStr)){
            SimpleDateFormat inputFormat = new SimpleDateFormat("M/d/yyyy hh:mm:ss a");
            Date date = inputFormat.parse(dateStr);

            // 转换为目标格式
            SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd");
            String outputDate = outputFormat.format(date);
            return outputDate;
        }else {
            return "";
        }

    }

    /**
     * @Author Liuxg
     * @Description 零件属性
     * @Date 2025/8/24 16:24
     * @Param []
     * @return matrix.util.StringList
    **/
    public StringList getSelectPartAttrList(){
        StringList typeSelectList = JF_Util_mxJPO.basicBolistSel();
        typeSelectList.add(DomainConstants.SELECT_DESCRIPTION);
        typeSelectList.add("attribute[JF_VPMReference.JF_PartNameCN]");
        typeSelectList.add("attribute[JF_VPMReference.JF_PartNameEN]");
        typeSelectList.add("attribute[JF_VPMReference.JF_PartType]");
        typeSelectList.add("attribute[JF_VPMReference.JF_ProcurementType]");
        typeSelectList.add("attribute[JF_VPMReference.JF_DirectBuy]");
        typeSelectList.add("attribute[JF_VPMReference.JF_ProcurementGroup]");
        typeSelectList.add("attribute[JF_VPMReference.JF_ProcurementGroup]");
        typeSelectList.add("attribute[JF_VPMReference.JF_Unit]");
        typeSelectList.add("attribute[JF_VPMReference.JF_CustomerDrawingNumber]");
        typeSelectList.add("attribute[EnterpriseExtension.V_PartNumber]");
        typeSelectList.add(DomainConstants.SELECT_PHYSICAL_ID);

//        typeSelectList.add("attribute[JF_VPMReference.JF_CustomerPartNumber]");
//        typeSelectList.add("attribute[JF_VPMReference.JF_CustomerPartRevision]");
//        typeSelectList.add("attribute[PLMEntity.V_description]");
//        typeSelectList.add("attribute[PLMEntity.V_Name]");
//        typeSelectList.add("attribute[JF_VPMReference.JF_WeightTarget]");
//        typeSelectList.add("attribute[JF_VPMReference.JF_FlexiblePart]");
//        typeSelectList.add("attribute[JF_VPMReference.JF_Fabric]");
//        typeSelectList.add("attribute[JF_VPMReference.JF_GramWeight]");
//        typeSelectList.add("attribute[JF_VPMReference.JF_MaterialStar]");
//        typeSelectList.add("attribute[JF_VPMReference.JF_OriginalPart]");
//        typeSelectList.add("attribute[JF_VPMReference.JF_PartDes]");
//        typeSelectList.add("attribute[JF_VPMReference.JF_PartNumberExternal]");
//
//        typeSelectList.add("attribute[JF_VPMReference.JF_PartSubType]");
//        typeSelectList.add("attribute[JF_VPMReference.JF_ProjectRel]");
//        typeSelectList.add("attribute[JF_VPMReference.JF_SupplierOrSupplierPartNumber]");
//        typeSelectList.add("attribute[JF_VPMReference.JF_SurfaceTreatment]");
//        typeSelectList.add("attribute[JF_VPMReference.JF_TransformationPlan]");
//        typeSelectList.add("attribute[JF_VPMReference.JF_TransferDownstreamSystem]");

        return typeSelectList;
    }

    /**
     * @Author Liuxg
     * @Description JFPartList2VPMReference关系上的属性
     * @Date 2025/8/24 19:56
     * @Param []
     * @return matrix.util.StringList
    **/
    public StringList getPartListRelAttrList(){
        StringList rellist=new StringList();
        rellist.add(DomainRelationship.SELECT_ID);
        rellist.add("attribute[JFPartSpecifications]");
        rellist.add("attribute[JFBicycleUsage]");
        rellist.add("attribute[JFTotalDemand]");
        rellist.add("attribute[JFDeliveryDate]");
        rellist.add("attribute[JFCurrency]");
        rellist.add("attribute[JFOutputLocation]");
        rellist.add("attribute[JFBudgetUnitPrice]");
        rellist.add("attribute[JFBudgetMoldFee]");
        rellist.add("attribute[JFBudgetExperimentalFee]");
        rellist.add("attribute[JFBudgetInspectionToolFee]");
        rellist.add("attribute[JFBudgetLeatherTextureFee]");
        rellist.add("attribute[JFDeliveryPhase]");
        rellist.add("attribute[JFSSOWIssueDate]");
        rellist.add("attribute[JFDrawDataCompletionPlanDate]");
        rellist.add("attribute[JFStopUsingFlag]");
        rellist.add("attribute[JFPartRequirementPlanDate]");
        rellist.add("attribute[JFCarryOver]");


        rellist.add("attribute[JSdataProcessProgress]");
        rellist.add("attribute[JF_SyncStatus]");
        rellist.add("attribute[JFProcurementType]");

        return rellist;
    }

    /**
     * @Author Liuxg
     * @Description 
     * @Date 2025/8/24 22:06 
     * @Param [] 
     * @return matrix.util.StringList
    **/
    public StringList getProjectAttrList(){
        StringList busSelects = new StringList();

        busSelects.add(DomainConstants.SELECT_ID);
        busSelects.add(DomainConstants.SELECT_NAME);
        busSelects.add(DomainConstants.SELECT_DESCRIPTION);
        busSelects.add(DomainConstants.SELECT_OWNER);
        busSelects.add("attribute[JF_ProjType]");
        busSelects.add("attribute[JF_DirectCustomer]");
        busSelects.add("attribute[JF_CustomerLoc]");
        busSelects.add("attribute[JF_Model]");
        busSelects.add("attribute[JF_ProjectCustomers]");
        busSelects.add("attribute[JF_Plant]");
        busSelects.add("attribute[JF_PlantLoc]");
        busSelects.add("attribute[JF_SalesTarget]");
        busSelects.add("attribute[JF_AnnualSales]");
        busSelects.add("attribute[JF_ProductName]");
        busSelects.add("attribute[JF_ProductType]");
        busSelects.add("attribute[JF_DateOfMassProduction]");
        busSelects.add("attribute[JF_PSStartDate]");
        busSelects.add("attribute[JF_ProjectEOP]");
        busSelects.add("attribute[JF_T0Date]");
        busSelects.add("attribute[JF_DVDate]");
        busSelects.add("attribute[JF_PVDate]");
        busSelects.add("attribute[JF_PSSOPDate]");

        busSelects.add("attribute[JFAffectedFactory]");

        busSelects.add("attribute[JF_ProjectTag]");
        busSelects.add("attribute[JF_ProjectStatus]");
        busSelects.add("attribute[JF_ProjectECI]");
        busSelects.add("attribute[JF_ProjectGrade]");
        busSelects.add("attribute[JF_Phase1PlannedCompletionTime]");
        busSelects.add("attribute[JF_Phase2PlannedCompletionTime]");
        busSelects.add("attribute[JF_Phase3PlannedCompletionTime]");
        busSelects.add("attribute[JF_Phase4PlannedCompletionTime]");
        busSelects.add("attribute[JF_Phase5PlannedCompletionTime]");
        busSelects.add("attribute[JF_Phase2_3PlannedCompletionTime]");
        busSelects.add("attribute[JSOutSourceRev]");
        busSelects.add("attribute[JF_ProducingArea]");
        busSelects.add("attribute[JF_PSSOPDate]");
        busSelects.add("attribute[JF_ProjectLifeCycle]");
        busSelects.add("attribute[JF_PaymentTerms4Molds]");
        return busSelects;
    }
    public boolean importSDTChairManager(Context context,String[] args) throws Exception{
        Map request = JPO.unpackArgs(args);
        log.info("request:{}",request);
        String objectId = (String)request.get("objectId");
        DomainObject obj = DomainObject.newInstance(context,objectId);
       boolean isProject = obj.isKindOf(context,"Project Space");
       Map map = new HashMap();
       if(isProject){
            map.put("objectId",objectId);
       }else{
        Task task = new Task(objectId);
        Map temp = task.getProject(context,JF_Util_mxJPO.basicBolistSel());
        map.put("objectId",UIUtil.getValue(temp,DomainConstants.SELECT_ID));
       }
       boolean flag = isSDTWholeSeat(context,JPO.packArgs(map));//是否是整椅经理
       Map roleMap = new HashMap();
       roleMap.put("roleName","JfESOAdmin");
       roleMap.put("userName",context.getUser());
//        boolean esoAdmin = JF_Util_mxJPO.isIncludeRole(context,JPO.packArgs(roleMap));//是否有ESO审核员角色
        //并且是Name为ESO的任务
        boolean isTask = obj.isKindOf(context,DomainConstants.TYPE_TASK);
        Map taskMap = obj.getInfo(context,JF_Util_mxJPO.basicBolistSel());
        if((flag)&&isTask) {
            if (UIUtil.getValue(taskMap, DomainConstants.SELECT_NAME).contains("ESO") ){
                return true;
            }
        }
        return false;
    }

    public void setPartlistSate(Context context,String partlistid)throws Exception{
        DomainObject partlistObj=DomainObject.newInstance(context,partlistid);

        StringList bul=getSelectPartAttrList();
        StringList rell=getPartListRelAttrList();

        //未同步的零件
        String strRelWhere = "attribute[JF_SyncStatus]=='unsynchronized'";

        //查询已CAA转换但是没有同步的
        MapList vpmList = partlistObj.getRelatedObjects(context, "JFPartList2VPMReference", "VPMReference", bul, rell,
                false, true, (short) 1,
                "", strRelWhere, 0);
        try {
            ContextUtil.pushContext(context);
            if(vpmList.size()==0){
                //已经全部同步
                partlistObj.setAttributeValue(context,"JFSyncSRM","synchronized");
            }else {
                //未全部同步
                partlistObj.setAttributeValue(context,"JFSyncSRM","NotFullsynchronized");
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            ContextUtil.popContext(context);
        }

    }


    public void moddate(Context context,String[]args){
        try {
            String id="35845.4994.54509.28244";
            DomainObject partlistObj=DomainObject.newInstance(context,id);

            StringList bul = getSelectPartAttrList();
            StringList rell = getPartListRelAttrList();
            JSONArray allpartArray = new JSONArray();

//            String strRelWhere = "attribute[JSdataProcessProgress]=='CAAProcessingComplete'&&attribute[JF_SyncStatus]=='unsynchronized'";
            //测试用
            String strRelWhere = "";

            MapList vpmList = partlistObj.getRelatedObjects(context, "JFPartList2VPMReference", "VPMReference", bul, rell,
                    false, true, (short) 1,
                    "", strRelWhere, 0);
            String dateStr="11/27/2025 12:00:00 PM";
            for (int i = 0; i < vpmList.size(); i++) {
                Map vpmmap = (Map) vpmList.get(i);
                String vpmid = (String) vpmmap.get(DomainConstants.SELECT_ID);
                String relid= (String) vpmmap.get(DomainRelationship.SELECT_ID);
                DomainRelationship rel=DomainRelationship.newInstance(context,relid);
//                rel.setAttributeValue(context,"JFDeliveryDate",dateStr);
//                rel.setAttributeValue(context,"JFPartRequirementCommitmentDate",dateStr);
//                rel.setAttributeValue(context,"JFPartRequirementPlanDate",dateStr);
                rel.setAttributeValue(context,"JF_SyncStatus","unsynchronized");
                rel.setAttributeValue(context,"JSdataProcessProgress","CAAProcessingComplete");
            }

        }catch (Exception e){
            e.printStackTrace();
        }
    }


    public boolean JF_ProjectLevelAccess(Context context,String[]args)throws Exception{
        boolean b=false;
        try {
//            Map programMap = (HashMap) JPO.unpackArgs(args);

            DomainObject userObj=PersonUtil.getPersonObject(context);
            //商务部门name
            String BusinessName=JF_PublicMethodClass_mxJPO.getBasicUrl(context, new String[]{"Department.Business.Name"});

            StringList Department_namelist=userObj.getInfoList(context,"to[Member].from.name");

            log.info("JF_ProjectLevelAccess--->"+BusinessName);
            log.info("JF_ProjectLevelAccess--->"+Department_namelist);

            if(UIUtil.isNotNullAndNotEmpty(BusinessName)&&Department_namelist.contains(BusinessName)){
                b=true;
            }

        }catch (Exception e){
            e.printStackTrace();
        }
        return b;
    }



    public Map getJFProjectLevelRanges(Context context, String[] args) throws Exception {
        log.info("-------------------------- JFProjectLevel begin ------------------------------------------");
        HashMap<Object, Object> res = new HashMap<>();
        StringList ranges = FrameworkUtil.getRanges(context, "JFProjectLevel");
        StringList ranges2=new StringList();
        for (int i=0;i<ranges.size();i++){
            String range=ranges.get(i);
            if(UIUtil.isNotNullAndNotEmpty(range)){
                ranges2.add(range);
            }
        }
        StringList nlsRanges = EnoviaResourceBundle.getAttrRangeI18NStringList(context, "JF_ExpertsDocSpecialty", ranges2, context.getLocale().toString());
        res.put("field_choices", ranges2);
        res.put("field_display_choices", nlsRanges);
        log.info("res:{}", res);
        log.info("-------------------------- JFProjectLevel end ------------------------------------------");
        return res;
    }

    public StringList getFreePart(Context context, String projectId)throws Exception{
        //导出游离件
        StringList freePartid=new StringList();
        try {
            //JFProject2RootPart   JFZeroPart   JF_BelongPart
            //JF_BelongPart 所有为Y的是属于当前项目的。属于项目零件组
            //JFZeroPart  所有为Y的属于供货价清单。

            //获取供货价清单下的所有零件的最新版（发布工作中冻结），形成有效零件组
            //遍历项目零件组的零件获取最新发布版，形成新的项目零件组。
            //遍历项目零级组，查询是否在有效零件组中，

            //attribute[PLMReference.V_isLastVersion]=='TRUE'
            StringList bulist= JF_Util_mxJPO.basicBolistSel();
            bulist.add(JF_PLMConstants_mxJPO.SELECT_ATTR_V_isLastVersion);
            bulist.add("attribute[JF_VPMReference.JF_PartType]");


            StringList relist= JF_Util_mxJPO.basicRellistSel();
            relist.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JFZeroPart);
            relist.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JF_BelongPart);


            DomainObject projectObj=DomainObject.newInstance(context,projectId);
            //获取项目下的所有关联零件
            MapList relPartList = projectObj.getRelatedObjects(context,
                    JF_PLMConstants_mxJPO.rel_JFProject2RootPart, // relationship pattern
                    JF_PLMConstants_mxJPO.TYPE_VPMReference,                                    // object pattern
                    bulist,                            // object selects
                    relist, // relationship selects
                    false,                                        // to direction
                    true,                                        // from direction
                    (short) 1,                                    // recursion level
                    "",                // object where clause
                    "",
                    (short) 0);

            //供货件清单
            StringList rootlist=new StringList();
            //零件组
            StringList BelongPastList=new StringList();

            for(int i=0;i<relPartList.size();i++){
                Map relPatmap= (Map) relPartList.get(i);
                String JFZeroPart= (String) relPatmap.get(JF_PLMConstants_mxJPO.SELECT_ATTR_JFZeroPart);
                String JF_BelongPart= (String) relPatmap.get(JF_PLMConstants_mxJPO.SELECT_ATTR_JF_BelongPart);
                String isLastVersion= (String) relPatmap.get(JF_PLMConstants_mxJPO.SELECT_ATTR_V_isLastVersion);
                String JF_PartType= (String) relPatmap.get("attribute[JF_VPMReference.JF_PartType]");
                String partid= (String) relPatmap.get(DomainConstants.SELECT_ID);
                if("Y".equalsIgnoreCase(JFZeroPart)&&"TRUE".equalsIgnoreCase(isLastVersion)){
                    //最新版本供货件
                    //获取供货件的最新发布版
                    String LastReleasedid=JF_Util_mxJPO.getLastReleasedMajorid(context,partid);

                    Set<String> rootSet=new HashSet();
                    rootSet.add(partid);
                    rootSet.add(LastReleasedid);
                    log.info("rootSet----->"+rootSet);
                    for(String zeroPartid:rootSet){
                        if(UIUtil.isNullOrEmpty(zeroPartid)){
                            continue;
                        }

                        DomainObject rootObj=DomainObject.newInstance(context,zeroPartid);
                        MapList childPartList = rootObj.getRelatedObjects(context,
                                "VPMInstance", // relationship pattern
                                JF_PLMConstants_mxJPO.TYPE_VPMReference,                                    // object pattern
                                bulist,                            // object selects
                                relist, // relationship selects
                                false,                                        // to direction
                                true,                                        // from direction
                                (short) 0,                                    // recursion level
                                "",                // object where clause
                                "",
                                (short) 0);
                        //获取供货件清单中的所有零件的最新版本
                        for(int j=0;j<childPartList.size();j++){
                            Map childmap= (Map) childPartList.get(j);
                            String childid= (String) childmap.get(DomainConstants.SELECT_ID);
//                            String lastid=getLastMajorid(context,childid);
                            if(UIUtil.isNotNullAndNotEmpty(childid)){
                                rootlist.add(childid);
                            }
                        }
                    }

                }

                if(!"Y".equalsIgnoreCase(JFZeroPart)){
                    //非0级件
                    //最新发布版
                    String LastReleasedid=JF_Util_mxJPO.getLastReleasedMajorid(context,partid);
                    if(UIUtil.isNotNullAndNotEmpty(LastReleasedid)){
                        BelongPastList.add(LastReleasedid);
                    }
                }

            }
            log.info("rootlist----->"+rootlist);
            log.info("BelongPastList----->"+BelongPastList);

            Set freeidset=new HashSet();
            for(String id:BelongPastList){
                if(!rootlist.contains(id)){
                    freeidset.add(id);
                }
            }

            freePartid.addAll(freeidset);
            //输出excel

        }catch (Exception e){
            e.printStackTrace();
        }
        return freePartid;

    }


    /**
     * @Author Liuxg
     * @Description 获取零件的最新版本（工作中，冻结，发布）
     * @Date 2025/12/12 10:14
     * @Param [context, vpmId]
     * @return java.lang.String
    **/
    public static String getLastMajorid(Context context, String vpmId) throws Exception {
        String lastId = "";
        try {
            BusinessObjectList majorRevisionsBusObjList = DomainObject.newInstance(context, vpmId).getMajorRevisions(context);
            DomainObject doObj;
            String sCurrent;
            if (majorRevisionsBusObjList.size() == 1) {
                doObj = DomainObject.newInstance(context, vpmId);
                sCurrent = doObj.getInfo(context, DomainObject.SELECT_CURRENT);
                if ("RELEASED".equals(sCurrent)||"IN_WORK".equals(sCurrent)||"FROZEN".equals(sCurrent)) {
                    lastId = vpmId;
                }
            } else {
                for (int i = majorRevisionsBusObjList.size() - 1; i >= 0; i--) {
                    doObj = (DomainObject) majorRevisionsBusObjList.get(i);
                    sCurrent = doObj.getInfo(context, DomainObject.SELECT_CURRENT);
                    if ("RELEASED".equals(sCurrent)||"IN_WORK".equals(sCurrent)||"FROZEN".equals(sCurrent)) {
                        lastId =  doObj.getInfo(context, DomainObject.SELECT_ID);
                        break;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
        return lastId;
    }

    public void Testshare(Context context,String args[])throws Exception{
        try {
            Map parmmap=new HashMap();
            parmmap.put("objectId",args[0]);

            MapList fileList = JPO.invoke(context, "JF_PublicProjectQuery", null, "getVpmRefAndProjectInformation", JPO.packArgs(parmmap), MapList.class);
            log.info("fileList--->"+fileList);

        }catch (Exception e){
            e.printStackTrace();
        }
    }


    /**
     * @Author Liuxg
     * @Description 输出Excel
     * @Date 2025/12/12 10:19
     * @Param [context, freePartid]
     * @return void
    **/
    public Map freePartExportExcel(Context context, String[] args)throws Exception{
        log.info("freePartExportExcel---->start");
        Map resMap = new HashMap();

        try {
            ContextUtil.pushContext(context);
            String projectid=args[0];
            log.info("freePartExportExcel--projectid-->"+projectid);
            if(UIUtil.isNotNullAndNotEmpty(projectid)){
                StringList freePartid=getFreePart(context,projectid);
                log.info("freePartExportExcel--freePartid-->"+freePartid);
                DomainObject projectObj=DomainObject.newInstance(context,projectid);
                String projectName = projectObj.getInfo(context, "name");

                String  classPath = JF_PublicMethodClass_mxJPO.getBasicUrl(context, new String[]{"Space.WEBINFO.Path"});
                log.info("classPath:{}", classPath);
                int webInfIndex = classPath.indexOf("WEB-INF");
                if (webInfIndex == -1) {
                    throw new IllegalStateException("WEB-INF not found in classPath.");
                }
                String fileTemPath = classPath.substring(0, webInfIndex);
                String filePath = JF_ECRService_mxJPO.getTemplatePath("FreePartTemplate.xlsx", fileTemPath);
                //打开文件
                InputStream inputStream = new FileInputStream(filePath);
                Workbook workbook = WorkbookFactory.create(inputStream);
                String fileName = "";


                fileName = projectName+"_游离零件清单";
                Sheet sheet = workbook.getSheetAt(0);

                CellStyle basicCellStyle = JF_ExportMBOM_mxJPO.getBasicCellStyle(workbook);
//                CellStyle basicCellStyle2 = JF_ExportMBOM_mxJPO.getBasicCellStyle(workbook);
//                basicCellStyle2.setWrapText(true);
                CellStyle basicCellStyle2 = workbook.createCellStyle();
                basicCellStyle2.setBorderTop(BorderStyle.THIN);
                basicCellStyle2.setBorderBottom(BorderStyle.THIN);
                basicCellStyle2.setBorderLeft(BorderStyle.THIN);
                basicCellStyle2.setBorderRight(BorderStyle.THIN);
                basicCellStyle2.setWrapText(true); //
                basicCellStyle2.setAlignment(HorizontalAlignment.LEFT); // 左对齐
                basicCellStyle2.setVerticalAlignment(VerticalAlignment.TOP); // 顶部对齐



                MapList FreePartMappingList = JF_PublicMethodClass_mxJPO.getJFCostChangeHistoryTemplateMapping(context, "FreePart");
                StringList bul= JF_Util_mxJPO.basicBolistSel();
                bul.add("attribute[EnterpriseExtension.V_PartNumber]");
                bul.add("attribute[JF_VPMReference.JF_PartNameEN]");
                bul.add("attribute[JF_VPMReference.JF_PartNameCN]");

                bul.add("attribute[JF_VPMReference.JF_PartType]");
                bul.add("attribute[JF_VPMReference.JF_Detail_CN]");
                bul.add("attribute[JF_VPMReference.JF_Detail_EN]");

                Map parmmap=new HashMap();
                for(int i=0;i<freePartid.size();i++){
                    String freeid=freePartid.get(i);

                    DomainObject freeObj=DomainObject.newInstance(context,freeid);
                    parmmap.put("objectId",freeid);
                    StringList ProjectNameList=new StringList();

                    MapList fileList = JPO.invoke(context, "JF_PublicProjectQuery", null, "getVpmRefAndProjectInformation", JPO.packArgs(parmmap), MapList.class);
                    for(int f=0;f<fileList.size();f++){
                        Map promap= (Map) fileList.get(f);
                        String ProjectName= (String) promap.get("ProjectName");
                        //"<br/>"
                        String ParentID= (String) promap.get("ParentID");
                        ParentID = ParentID.replaceAll("<br/>",",");
                        String rowproject=ProjectName+":"+ParentID;
                        ProjectNameList.add(rowproject);
                    }
                    String ProjectNameStr=ProjectNameList.join("\n");
                    log.info("ProjectNameStr--->"+ProjectNameStr);


                    Map attrMap=freeObj.getInfo(context,bul);
                    Row row = sheet.createRow(i+1);
                    attrMap.put("ITEM",i+1);
                    for (int j = 0; j < FreePartMappingList.size(); j++) {
                        Map mappingMap = (Map) FreePartMappingList.get(j);
                        String strKey = (String)mappingMap.get("id");
                        String strColIndex = (String)mappingMap.get("value");
                        if("current".equals(strKey)){
                            String strNlsCurrent = attrMap.get(strKey)+"";

                            String strValue = EnoviaResourceBundle.getStateI18NString(context, "VPLM_SMB_Definition_MajorRev",strNlsCurrent, context.getLocale().toString());

                            Cell cell = null ;
                            cell = row.createCell(Integer.parseInt(strColIndex));
                            if (JF_PublicMethodClass_mxJPO.isNumeric(strValue)) {
                                double dValue = Double.parseDouble(strValue);
                                cell.setCellValue(dValue);
                                cell.setCellStyle(basicCellStyle);
                            }else {
                                cell.setCellValue(strValue);
                                cell.setCellStyle(basicCellStyle);
                            }
                        }else if("owner".equals(strKey)){
                            String owner = attrMap.get(strKey)+"";
                            String strValue=PersonUtil.getFullName(context,owner);

                            Cell cell = null ;
                            cell = row.createCell(Integer.parseInt(strColIndex));
                            if (JF_PublicMethodClass_mxJPO.isNumeric(strValue)) {
                                double dValue = Double.parseDouble(strValue);
                                cell.setCellValue(dValue);
                                cell.setCellStyle(basicCellStyle);
                            }else {
                                cell.setCellValue(strValue);
                                cell.setCellStyle(basicCellStyle);
                            }
                        }else if("attribute[JF_VPMReference.JF_PartType]".equals(strKey)) {
                            //零件类型
                            String JF_PartType = attrMap.get(strKey) + "";

                            String strValue = "";
                            if (UIUtil.isNotNullAndNotEmpty(JF_PartType)) {
                                strValue = EnoviaResourceBundle.getRangeI18NString(context, "JF_VPMReference.JF_PartType", JF_PartType, context.getSession().getLanguage());
                            }

                            Cell cell = null;
                            cell = row.createCell(Integer.parseInt(strColIndex));
                            if (JF_PublicMethodClass_mxJPO.isNumeric(strValue)) {
                                double dValue = Double.parseDouble(strValue);
                                cell.setCellValue(dValue);
                                cell.setCellStyle(basicCellStyle);
                            } else {
                                cell.setCellValue(strValue);
                                cell.setCellStyle(basicCellStyle);
                            }
                        }else if("projectName".equals(strKey)){
                            //新增共用项目
                            Cell cell = null ;
                            cell = row.createCell(Integer.parseInt(strColIndex));
                            cell.setCellStyle(basicCellStyle2);
                            cell.setCellValue(ProjectNameStr);

                        } else{
                            String strValue = attrMap.get(strKey)+"";
                            Cell cell = null ;
                            cell = row.createCell(Integer.parseInt(strColIndex));
                            if (JF_PublicMethodClass_mxJPO.isNumeric(strValue)) {
                                double dValue = Double.parseDouble(strValue);
                                cell.setCellValue(dValue);
                                cell.setCellStyle(basicCellStyle);
                            }else {
                                cell.setCellValue(strValue);
                                cell.setCellStyle(basicCellStyle);
                            }
                        }

                    }
                }
                sheet.autoSizeColumn(10, true);


                //获取当前时间
                LocalDateTime now = LocalDateTime.now();
                // 将 LocalDateTime 对象转换成指定格式的字符串
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
                String strFormattedDate = now.format(formatter);
                resMap.put("file", workbook);
                resMap.put("flag", "Y");
                resMap.put("fileName", JF_PublicMethodClass_mxJPO.buildStringInStrings(fileName, ".xlsx"));
            }else {
                resMap.put("flag", "N");
            }
        }catch (Exception e){
            e.printStackTrace();
            resMap.put("flag", "N");
        }finally {
            ContextUtil.popContext(context);
        }

        log.info("freePartExportExcel--resMap-->"+resMap);
        log.info("freePartExportExcel---->end");
        return resMap;
    }


    public void writeJsonToFile(JSONObject jsonObject, String filePath) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            // 使用 fastjson 将 JSONObject 转换为格式化的字符串
            String jsonString = JSON.toJSONString(jsonObject, SerializerFeature.PrettyFormat);
            writer.write(jsonString);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getTimeCuo(){
        // 获取当前日期和时间
        LocalDateTime now = LocalDateTime.now();

        // 定义格式化器
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyMMddHHmm");

        // 格式化当前日期
        String formattedDate = now.format(formatter);
        return formattedDate;
    }

    public void TestSRMInterface(Context context,String []args){
        try {
            JSONObject data=new JSONObject();
            data.put("Test","Test");

            Map headerMap = new HashMap();
            headerMap.put("jf_svc", "IOA0186");
            headerMap.put("jf_applicationid", UUID.randomUUID() + "");
            headerMap.put("jf_sender", "PLM");
            headerMap.put("jf_receiver", "SRM");
            headerMap.put("jf_document", UUID.randomUUID() + "");

            String url = JF_PublicMethodClass_mxJPO.getBasicUrl(context, new String[]{"JFSyncSRMPartList.ESB.URL"});
//                    String url = "http://172.16.33.183:7080/JFSEATesb/Services/SRM/SyncPartList";

            String result = dopost(data.toString(), url, headerMap);
            JSONObject response = JSONObject.parseObject(result);

            String responseFlag="E";
            log.info("SRMresponse---->"+response);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * @Author Liuxg
     * @Description 根据条件筛选指定的项目
     * @Date 2025/12/17 15:33
     * @Param [context, args]
     * @return void
    **/
    public void findAllProject(Context context,String []args){
        try {
            StringBuffer where=new StringBuffer();
            //W04B，X04C，L946-B1，DX1H-P，T1V，E15MCA1
            StringList namelist=new StringList();
            namelist.add("W04B");
            namelist.add("E15MCA1");
            namelist.add("L946-B1");
            namelist.add("X04C");
            namelist.add("DX1H-P");
            namelist.add("T1V");


            //strBusWhere = "attribute[Title].value ~~ '*CCB*'";
            for(int i=0;i<namelist.size();i++){
                String name=namelist.get(i);
                if(i<namelist.size()-1){
                    where.append("attribute[JF_ProjectCode].value ~~ '*").append(name).append("*'||");
                }else {
                    where.append("attribute[JF_ProjectCode].value ~~ '*").append(name).append("*'");
                }

            }
            log.info("where--->"+where.toString());
            StringList sbul=JF_Util_mxJPO.basicBolistSel();
            sbul.add("attribute[JF_ProjectCode]");
            MapList list =  DomainObject.findObjects(context, "Project Space", "*", where.toString(),sbul);

            log.info("list--->"+list.size());
            StringList stl=new StringList();

            for(int i=0;i<list.size();i++){
                Map promap= (Map) list.get(i);
                stl.add(promap.get("attribute[JF_ProjectCode]").toString());
            }

            log.info("stl---->"+stl);

            StringList sbl2=new StringList();
            for(int i=0;i<namelist.size();i++){
                String name=namelist.get(i);
                String wher="attribute[JF_ProjectCode].value ~~ '*"+name+"'";
                MapList list2=DomainObject.findObjects(context, "Project Space", "*", wher,sbul);
                log.info("list2--->"+list2.size());


                for(int j=0;j<list2.size();j++){
                    Map promap= (Map) list2.get(j);
                    sbl2.add(promap.get("attribute[JF_ProjectCode]").toString());
                }
            }
            log.info("sbl2---->"+sbl2);



            StringList sbl3=new StringList();
            for(int i=0;i<namelist.size();i++){
                String name=namelist.get(i);
                String wher="attribute[JF_ProjectCode].value ~~ '"+name+"*'";
                MapList list3=DomainObject.findObjects(context, "Project Space", "*", wher,sbul);
                log.info("list3--->"+list3.size());


                for(int j=0;j<list3.size();j++){
                    Map promap= (Map) list3.get(j);
                    sbl3.add(promap.get("attribute[JF_ProjectCode]").toString());
                }
            }
            log.info("sbl3---->"+sbl3);

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * 项目空间详细信息界面 同步项目信息到BIcommand执行方法
     * @Author Liuxg
     * @Description
     * @Date 2026/1/16 8:54
     * @Param [context, args]
     * @return java.lang.String
    **/
    public String JFCommandSendProjectSendBI(Context context,String []args)throws Exception{
        String result="T";
        try {
            JFCreateProjectSendBI(context,args);
        }catch (Exception e){
            e.printStackTrace();
            result="F";
        }
        return result;
    }

    public boolean checkProjectManager(Context context,String []args)throws Exception{
        Map programMap = (Map) JPO.unpackArgs(args);
        String objectId= (String)programMap.get("objectId");

        String contextName=context.getUser();

        String projectManagerName = JF_PublicMethodClass_mxJPO.getProjectWherePerson(context, objectId, SELECT_NAME, "", "attribute[Project Role]=='Project manager'");
        if(contextName.equals(projectManagerName)){
            return true;
        }else {
            return false;
        }

    }


    public void TestModTime(Context context,String[]args)throws Exception{
        try {
            String id="35845.4994.12666.59730";
//
//            String mql = "mod bus '" + id + "' name '" + name + "' revision '"+sb.toString()+"'";
//            MqlUtil.mqlCommand(context, false, mql, true);

            DomainObject projectObj=DomainObject.newInstance(context,id);
            Map valueMap = new HashMap();
            valueMap.put(JF_PLMConstants_mxJPO.ATTR_Task_Estimated_Start_Date, "01/01/2026");
            valueMap.put(JF_PLMConstants_mxJPO.ATTR_Task_Estimated_Finish_Date, "01/11/2026");

            projectObj.setAttributeValues(context, valueMap);


        }catch (Exception e){
            e.printStackTrace();
        }
    }


}
