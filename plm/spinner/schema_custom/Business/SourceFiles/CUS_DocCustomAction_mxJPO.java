import com.matrixone.apps.common.CommonDocument;
import com.matrixone.apps.common.util.ComponentsUtil;
import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.DomainRelationship;
import com.matrixone.apps.domain.util.*;
import com.matrixone.apps.framework.ui.UIUtil;
import matrix.db.Attribute;
import matrix.db.BusinessObject;
import matrix.db.Context;
import matrix.db.JPO;
import matrix.util.StringList;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.logging.Logger;

public class CUS_DocCustomAction_mxJPO {
    private final Logger _logger = Logger.getLogger("CUS_DocCustomAction_mxJPO");
    public static Properties config_properties = new Properties();

    public static final String TYPE_VPMREFERENCE = "VPMReference";
    public static final String TYPE_DOCUMENT = "Document";
    public static final String TYPE_DRAWING = "Drawing";
    public static final String TYPE_Aer_ECRDocument = "Aer_ECRDocument";
    public static final String TYPE_Change_Analysis = "Change Analysis";
    public static final String TYPE_Change_Request = "Change Request";

    public CUS_DocCustomAction_mxJPO(Context context, String[] strings) throws Exception {
//        Properties config_properties1 = this.config_properties;
//        System.out.println("config_properties>>>"+config_properties);
        this.config_properties = (Properties) JPO.invoke(context, "CUS_ConfigConstants", new String[]{}, "getPagePropertiesValue", new String[]{}, Properties.class);

    }


    /*
     *
     * 权限设置为不公开把 合作区设置为owner+ _PRJ;
     * */
    public int modifyDocAttributeAccessControl(Context context, String args[]) {
        int flag = 0;
        try {
            String objectId = args[0];
            // String newValue=args[1];

            //String newValue=args[1];
            DomainObject doObject = DomainObject.newInstance(context, objectId);
            String organization = doObject.getInfo(context, "organization");
            String project = doObject.getInfo(context, "project");

            System.out.println("modifyDocAttributeAccessControl start>>>>>>" + organization);
            System.out.println("modifyDocAttributeAccessControl start>>>>>>" + project);
            ContextUtil.pushContext(context);
          /*  MqlUtil.mqlCommand(context,"set context user creator");
            MqlUtil.mqlCommand(context,"trigger off");*/
            String newValue = doObject.getAttributeValue(context, "Aer_AccessControl");
            System.out.println("newValue>>>>>>" + newValue);
            String Aer_ProjOrg = doObject.getAttributeValue(context, "Aer_ProjOrg");
            System.out.println("Aer_ProjOrg>>>>>>" + Aer_ProjOrg);
            if ("Public".equals(newValue)) {

                String strProOrg = doObject.getAttributeValue(context, "Aer_ProjOrg");
                System.out.println("strProOrg>>>>>>>>>>>>>>" + strProOrg);
                if (UIUtil.isNotNullAndNotEmpty(strProOrg)) {
                    String[] poSpilt = strProOrg.split("\\|");
                    String projects = poSpilt[0];
                    project = projects.replace("project:", "");
                    //String organizations=poSpilt[1];
                    // organization=organizations.replace("organization:","");
                    System.out.println("set project>>>>Public>>>>>>>>>:" + project);
                    //System.out.println("organization>>>>Public>>>>>>>>>>:"+organization);
                    MqlUtil.mqlCommand(context, "mod  bus $1 project $2", objectId, project);
                    // MqlUtil.mqlCommand(context,"mod  bus $1 organization $2",objectId,organization);

                }


            } else if ("Private".equals(newValue)) {
                System.out.println("delete project or organization");

                String owner = doObject.getInfo(context, "owner");
                if ("User Agent".equals(owner) || "create".equals(owner)) {
                    owner = "admin_platforom_PRJ";
                } else {
                    owner = owner + "_PRJ";
                }
                String valProOrg = "";
                if (UIUtil.isNotNullAndNotEmpty(project) && (!project.equals(owner)) && UIUtil.isNotNullAndNotEmpty(organization)) {
                    valProOrg = "project:" + project + "|" + "organization:" + organization;
                    doObject.setAttributeValue(context, "Aer_ProjOrg", valProOrg);
                }
                System.out.println("set project>>>>private>>>>>>>>>:" + owner);

             /*   doObject.setProjectOwner(context,"");
                doObject.setOrganizationOwner(context,"");*/

                MqlUtil.mqlCommand(context, "mod  bus $1 project $2;", objectId, owner);
                //  MqlUtil.mqlCommand(context,"mod  bus $1 organization $2;",objectId,"");
            }
            //MqlUtil.mqlCommand(context,"trigger on");
            ContextUtil.popContext(context);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return flag;
    }

    public int createClassifiedItemRelationshipCheck(Context context, String args[]) {
        int flag = 0;
        try {
            String fromId = args[0];
//            String toId=args[1];
            DomainObject doFromVPM = DomainObject.newInstance(context, fromId);
            System.out.println("fromId--->:" + fromId);
//            System.out.println("toId--->:"+toId);

            // Updated by FangChen for check Subclass [2023/07/03] start
            StringList selObjRoute = new StringList();
            selObjRoute.add(DomainConstants.SELECT_ID);
            selObjRoute.add(DomainConstants.SELECT_TYPE);
            selObjRoute.add(DomainConstants.SELECT_CURRENT);
            selObjRoute.add("attribute[Aer_LibraryType]");

            MapList topList = doFromVPM.getRelatedObjects(context, "Subclass", "General Class,General Library",
                    selObjRoute, null, true, false, (short) 0, "", null, (short) 0);

            String strLibType = "";
            int iTopList = topList.size();
            String strType = "";
            for (int i = 0; i < iTopList; i++) {
                Map tmpObj = (Map) topList.get(i);
                System.out.println("tmpObj--->:" + tmpObj);
                strType = (String) tmpObj.get(DomainConstants.SELECT_TYPE);
                if ("General Library".equals(strType)) {
                    strLibType = (String) tmpObj.get("attribute[Aer_LibraryType]");
                    System.out.println("strLibType--->:" + strLibType);
                    break;
                }
            }

            if (UIUtil.isNotNullAndNotEmpty(strLibType)) {
                System.out.println("topListSize:" + topList.size());
                System.out.println("topList:" + topList);

                //if("VPMReference".equals(toType)){
                StringList hasSubclass = doFromVPM.getInfoList(context, "from[Subclass].to.name");
                System.out.println("Subclass   : " + hasSubclass);
                if (hasSubclass.size() > 0) {
                    MqlUtil.mqlCommand(context, "notice $1", "当前分类下还有子分类，不允许关联物理产品！");
                    flag = 1;
                }
                //}
            }
            // Updated by FangChen for check Subclass [2023/07/03] end

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return flag;
    }

    /*
     *检查文档是否可以升版
     * */
    public int triggerIsCheckDocCanRevise(Context context, String args[]) throws Exception {
        int isOK = 0;
        try {

//            String objectId=args[0];
//            System.out.println("enter triggerIsCheckDocCanRevise  objectId "+objectId );
//            DomainObject doObj=DomainObject.newInstance(context,objectId);
//            String policy=doObj.getInfo(context, DomainConstants.SELECT_POLICY);
//            if("Document Release".equals(policy)){
//                String current=doObj.getInfo(context,DomainConstants.SELECT_CURRENT);
//                if(!("RELEASED".equalsIgnoreCase(current)||"FROZEN".equalsIgnoreCase(current))){
//                    String errorMsg="该文档未冻结或未发布,不能升版！！！";
//                    isOK=1;
//                    MqlUtil.mqlCommand(context, "notice $1", errorMsg);
//                    //  isOK = 1; dashboard 可见错误
//                    throw new Exception(errorMsg);
//                }
//
//            }
//
//


        } catch (Exception ex) {
            isOK = 1;
            ex.printStackTrace();

        }
        return isOK;
    }


    public static String type_Aer_TechnicalDocument = "Aer_TechnicalDocument";
    public static String relationship_Aer_DocWorkSpace = "Aer_DocWorkSpace";
    public static String relationship_Vaulted_Objects = "Vaulted Objects";

    /*
     *技术文档发布 将文件挂到选择的书签文件夹中；
     * */
    public int triggerIsDocReleaseConnectWorkFolder(Context context, String args[]) throws Exception {
        int isOK = 0;
        try {

            String objectId = args[0];
            _logger.info("enter triggerIsDocReleaseConnectWorkFolder  objectId " + objectId);
            DomainObject doObj = DomainObject.newInstance(context, objectId);
            String policy = doObj.getInfo(context, DomainConstants.SELECT_POLICY);
            String type = doObj.getInfo(context, DomainConstants.SELECT_TYPE);
            if ("Document Release".equals(policy) && type_Aer_TechnicalDocument.equals(type)) {
                ContextUtil.pushContext(context);
                ContextUtil.startTransaction(context, true);
                try {
                    //mod by Nisha 技术文件创建入口更改到书签文件夹，当文件发布时需要从临时文件夹移动到选择的正式文件夹，即需要先移除创建时勾选的文件夹
//                    StringList slVaultedObjectsRelId = doObj.getInfoList(context, "to[Vaulted Objects].id");
//                    if (slVaultedObjectsRelId.size() > 0) {
//                        String[] strDel = slVaultedObjectsRelId.toStringArray();
//                        DomainRelationship.disconnect(context, strDel);
//                    }
//                    //end
//                    StringList reWorkSpaceSL = doObj.getInfoList(context, "to[" + relationship_Aer_DocWorkSpace + "].from.id");
//                    StringList reVaultedObjectsSL = doObj.getInfoList(context, "to[" + relationship_Vaulted_Objects + "].from.id");
//                    for (int i = 0; i < reWorkSpaceSL.size(); i++) {
//                        String wkId = reWorkSpaceSL.get(i);
//                        if (!reVaultedObjectsSL.contains(wkId)) {
//                            DomainRelationship.connect(context, wkId, relationship_Vaulted_Objects, objectId, false);
//                        }
//                    }
                    ContextUtil.commitTransaction(context);
                } catch (FrameworkException e) {
                    ContextUtil.abortTransaction(context);
                    throw e;
                } finally {
                    ContextUtil.popContext(context);
                }


            }


        } catch (Exception ex) {
            // isOK=1;
            ex.printStackTrace();

        }
        return isOK;
    }

    /*
     * 更新文件夹
     * */
    public void updateFolderInfo(Context context, String[] args) {
        try {
            Map programMap = (Map) JPO.unpackArgs(args);
//            HashMap requestMap = (HashMap) programMap.get("requestMap");
//            _logger.info("re....."+requestMap);

            HashMap paramMap = (HashMap) programMap.get("paramMap");
            // _logger.info("paramMap....."+paramMap);

            String oid = (String) paramMap.get("objectId");
            DomainObject doObj = DomainObject.newInstance(context, oid);
            StringList reWorkSpaceSL = doObj.getInfoList(context, "to[" + relationship_Aer_DocWorkSpace + "].from.id");
            _logger.info("reWorkSpaceSL " + reWorkSpaceSL);
            String newSelectId = (String) paramMap.get("New OID");
            if (UIUtil.isNotNullAndNotEmpty(newSelectId)) {
                String[] selectId = newSelectId.split("\\|");

                for (int i = 0; i < selectId.length; i++) {
                    String newId = selectId[i];
                    if (!reWorkSpaceSL.contains(newId)) {
                        DomainRelationship.connect(context, newId, relationship_Aer_DocWorkSpace, oid, false);
                    } else {
                        reWorkSpaceSL.remove(newId);
                    }

                }

            }
            StringList deleteRid = new StringList();
            //移除
            for (int i = 0; i < reWorkSpaceSL.size(); i++) {
                String wkId = reWorkSpaceSL.get(i);
                StringList reId = doObj.getInfoList(context, "to[" + relationship_Aer_DocWorkSpace + "|from.id=='" + wkId + "'].id", false);
                //DomainRelationship.disconnect();
                deleteRid.addAll(reId);


            }
            _logger.info("deleteRid >>" + deleteRid);
            if (deleteRid.size() > 0) {
                DomainRelationship.disconnect(context, deleteRid.toStringArray());
            }


            _logger.info("exit updateFolderInfo >>");

        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    public String showDocRelFolders(Context context, String[] args) {
        String linkStr = "";
        try {
            Map programMap = (Map) JPO.unpackArgs(args);
//            HashMap requestMap = (HashMap) programMap.get("requestMap");
//            _logger.info("re....."+requestMap);
            String oid = (String) programMap.get("objectId");
            if (UIUtil.isNullOrEmpty(oid)) {
                HashMap requestMap = (HashMap) programMap.get("requestMap");
                oid = (String) requestMap.get("objectId");
            }
            StringBuffer sbStr = new StringBuffer();
            _logger.info("oid>>" + oid);
            ContextUtil.pushContext(context);
            if (UIUtil.isNotNullAndNotEmpty(oid)) {
                System.out.println("oid====" + oid);
                DomainObject doDoc = DomainObject.newInstance(context, oid);
                String current = doDoc.getInfo(context, DomainConstants.SELECT_CURRENT);
                StringList reWorkSpaceSL = new StringList();

                //mod by wwy 2024-10-25 文档 文件夹属性显示 当前文件 所在的文件夹路径 start
//                if ("RELEASED".equals(current)) {
                //如果技术文档发布了 用ootb 文件夹关系 relationship_Vaulted_Objects
                reWorkSpaceSL = doDoc.getInfoList(context, "to[" + relationship_Vaulted_Objects + "].from.id");

//                } else {
//                    reWorkSpaceSL = doDoc.getInfoList(context, "to[Aer_DocWorkSpace].from.id");
//
//                }
                //mod by wwy 2024-10-25 文档 文件夹属性显示 当前文件 所在的文件夹路径 end
                System.out.println("reWorkSpaceSL====" + reWorkSpaceSL);

                for (int i = 0; i < reWorkSpaceSL.size(); i++) {

                    String workId = reWorkSpaceSL.get(i);
//                    DomainObject doWork=DomainObject.newInstance(context,workId);
//                    String title=doWork.getAttributeValue(context,DomainConstants.ATTRIBUTE_TITLE);
//                    String link=getObjLink(workId,title);

                    StringList getOnePath = getParentWorkSpaceId(context, workId);
                    _logger.info("getOnePath>>" + getOnePath);
                    int workSize = getOnePath.size();
                    workSize = workSize - 1;
                    for (int j = workSize; j >= 0; --j) {
                        String wkId = getOnePath.get(j);
                        DomainObject doWK = DomainObject.newInstance(context, wkId);

                        String link = getObjLink(wkId, doWK.getAttributeValue(context, DomainConstants.ATTRIBUTE_TITLE));
                        sbStr.append(link);
                        if (j != 0) {
                            sbStr.append("——>");
                        }
                    }

                    if (i != reWorkSpaceSL.size() - 1) {
                        sbStr.append("</br>");
                    }
                }
            }
            ContextUtil.popContext(context);

            linkStr = sbStr.toString();
            _logger.info("linkStr>>" + linkStr);

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return linkStr;

    }

    /*
     * 技术文件获取关联的文件夹
     * */
    public StringList getParentWorkSpaceId(Context context, String workId) {
        StringList slNew = new StringList();
        //StringList slHasNew=new StringList();
        try {
            //DomainObject doWorkId=DomainObject.newInstance(context,workId);
            slNew.add(workId);
            StringList parentIds = getParentWorkIds(context, workId);

            if (parentIds.size() > 0) {
                slNew.addAll(getParentWorkSpaceId(context, parentIds.get(0)));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return slNew;
    }

    public StringList getParentWorkIds(Context context, String workId) {
        StringList parentIds = new StringList();
        try {
            DomainObject doWorkId = DomainObject.newInstance(context, workId);
            parentIds = doWorkId.getInfoList(context, "to[Sub Vaults].from.id");
            if (parentIds.size() == 0) {
                parentIds = doWorkId.getInfoList(context, "to[Data Vaults].from.id");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return parentIds;
    }

    /*
     * 备份用
     * */

    public List getParentMultiWorkSpaceId(Context context, String workId, StringList existSL) {

        List listMulti = new ArrayList();
        try {
            StringList parentIds = getParentWorkIds(context, workId);
            //多个父书签怎么处理
            if (parentIds.size() > 1) {


                StringList recordLastSL = existSL;
                for (int i = 0; i < parentIds.size(); i++) {
                    String workIdVal = parentIds.get(i);
                    StringList slNew = new StringList();
                    slNew.add(workId);
                    StringList slHasNew = new StringList();
                    //第二层级就有多个父
                    if (!recordLastSL.toString().contains(workIdVal)) {
                        slNew.addAll(getParentWorkSpaceId(context, workIdVal));
                        recordLastSL.add(workIdVal);
                        listMulti.add(slNew);
                    } else {
                        //

                        //第二层 相同 判断 第二层的上一层是否有多个父
                        StringList LastParentIds = getParentWorkIds(context, workIdVal);
                        //无父级
                        if (LastParentIds.size() == 0) {
                            slNew.add(workIdVal);
                            listMulti.add(slNew);
                        } else if (LastParentIds.size() == 1) {
                            slNew.addAll(getParentWorkSpaceId(context, workIdVal));
                            listMulti.add(slNew);
                        } else if (LastParentIds.size() > 1) {
                            for (int j = 0; j < LastParentIds.size(); j++) {
                                //递归
                                //getParentMultiWorkSpaceId(context,  LastParentIds.get(j), recordLastSL);
                                slNew = new StringList();
                                slNew.add(workId);
                                slNew.add(workIdVal);
                                slNew.addAll(getParentWorkSpaceId(context, LastParentIds.get(j)));
                                listMulti.add(slNew);
                            }

                        }


                    }

                }

            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return listMulti;
    }


    public String getObjLink(String id, String ShowName) {
        String link = "<a href=\"javascript:void(0)\" " +
                "onclick=\"javascript:showModalDialog(" +
                "'../common/emxTree.jsp?objectId=" + id + "','860','520'" +
                ");\">" + ShowName + "</a>";

        return link;
    }

    /*
     * 技术文件编辑页面只显示 view不显示
     * */
    public boolean isShowEditFolderAccess(Context context, String[] args) {
        boolean isOK = false;
        try {
            Map programMap = (Map) JPO.unpackArgs(args);
            //HashMap requestMap = (HashMap) programMap.get("requestMap");
            _logger.info("programMap...." + programMap);
            String mode = (String) programMap.get("mode");
            _logger.info("mode...." + mode);
            if ("edit".equalsIgnoreCase(mode)) {
                isOK = true;
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return isOK;

    }

    /*
     * 技术文件view页面只显示 编辑不显示
     * */
    public boolean notShowEditFolderAccess(Context context, String[] args) {
        return !isShowEditFolderAccess(context, args);

    }

    /*
     * 技术文件提升到冻结 检查是否关联文件夹
     * */
    public int triggerIsCheckTechDocCanPromote(Context context, String args[]) {
        int isOK = 0;
        try {
            DomainObject doDoc = DomainObject.newInstance(context, args[0]);
            String type = doDoc.getInfo(context, DomainObject.SELECT_TYPE);
            if (type_Aer_TechnicalDocument.equals(type)) {
                StringList reWorkSpaceSL = doDoc.getInfoList(context, "to[" + relationship_Aer_DocWorkSpace + "].from.id");
                if (reWorkSpaceSL.size() == 0) {
                    String errorMsg = "该技术文档未关联文件夹,不能提升！！！";
                    isOK = 1;
                    MqlUtil.mqlCommand(context, "notice $1", errorMsg);
                    //  isOK = 1; dashboard 可见错误
                    throw new Exception(errorMsg);
                }
            }

        } catch (Exception ex) {
            isOK = 1;
            ex.printStackTrace();
        }
        return isOK;
    }

    /*
     *检查文档是否可以修改
     * */
    public int triggerIsCheckDocCanModify(Context context, String args[]) throws Exception {
        int isOK = 0;
        try {
            String currentOwner = context.getUser();
            if ("creator".equalsIgnoreCase(currentOwner) || "User Agent".equalsIgnoreCase(currentOwner) || currentOwner.contains("admin")) {
                return isOK;
            }
            String objectId = args[0];
            System.out.println("enter triggerIsCheckDocCanModify  objectId " + objectId);
            DomainObject doObj = DomainObject.newInstance(context, objectId);
            String policy = doObj.getInfo(context, DomainConstants.SELECT_POLICY);
            if ("Document Release".equals(policy)) {
                String current = doObj.getInfo(context, DomainConstants.SELECT_CURRENT);

                if ("RELEASED".equalsIgnoreCase(current) || "FROZEN".equalsIgnoreCase(current)) {
                    String errorMsg = "该文档已冻结或已发布,不能修改！！！";
                    isOK = 1;
                    MqlUtil.mqlCommand(context, "notice $1", errorMsg);
                    //  isOK = 1; dashboard 可见错误
                    throw new Exception(errorMsg);
                }

            }


        } catch (Exception ex) {
            isOK = 1;
            ex.printStackTrace();

        }
        return isOK;
    }


    public static String relationship_Object_Route = "Object Route";

    public void triggerIsCADemoteToInWorkCompleteRoute(Context context, String[] args) {
        String caId = args[0];
        try {
            ContextUtil.pushContext(context);
            DomainObject doCa = DomainObject.newInstance(context, caId);
            //from[Object Route].to.current = Complete
            StringList notCompleteRouteId = doCa.getInfoList(context, "from[" + relationship_Object_Route + "|to.current!='Complete'&&to.attribute[Route Status]=='Stopped'].to.id", false);
            _logger.info("notCompleteRouteId>>>>>>>>+" + notCompleteRouteId);
            for (int i = 0; i < notCompleteRouteId.size(); i++) {
                String routeId = notCompleteRouteId.get(i);
                DomainObject routeObj = DomainObject.newInstance(context, routeId);
                //Route Activity State: Rejected was: Awaiting Approval
                String RouteActivityState = routeObj.getAttributeValue(context, "Route Activity State");
                if ("Stopped".equals(RouteActivityState)) {
                    String descStr = routeObj.getInfo(context, DomainConstants.SELECT_DESCRIPTION);
                    descStr = descStr + (" (CA 被手动降级驳回 程序将流程设置为完成状态)");
                    _logger.info("descStr>>>>>>>>+" + descStr);
                    routeObj.setDescription(context, descStr);
                }
                MqlUtil.mqlCommand(context, "mod bus " + routeId + " current Complete;");

            }
            //description

            ContextUtil.popContext(context);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static String eService_Production = "eService Production";
    public static String attribute_Aer_FileLevel = "Aer_FileLevel";
    public static String attribute_Aer_SecrecyLevel = "Aer_SecrecyLevel";
    public static String attribute_Aer_TemplateDocName = "Aer_TemplateDocName";


    /*
     *
     *创建文档obj
     * */
    public Map createDocObj(Context context, String[] args) throws Exception {
        Map reMap = new HashMap<>();

        try {
            Map program = JPO.unpackArgs(args);
            _logger.info("program>>>wwy>>>>" + program);
            String name = (String) program.get("Name");
            String TypeActual = (String) program.get("TypeActual");
            String Policy = (String) program.get("Policy");
            DomainObject newObj = new DomainObject();
            newObj.createObject(context, TypeActual, name, "A", Policy, eService_Production);

            String Description = (String) program.get("Description");
            newObj.setDescription(context, Description);
            String Aer_FileLevel = (String) program.get("Aer_FileLevel");
            String Title = (String) program.get("Title");
            String Aer_SecrecyLevel = (String) program.get("Aer_SecrecyLevel");
//
//            if(UIUtil.isNotNullAndNotEmpty(Title))newObj.setAttributeValue(context,DomainConstants.ATTRIBUTE_TITLE,Title);
//            if(UIUtil.isNotNullAndNotEmpty(Aer_FileLevel))newObj.setAttributeValue(context,attribute_Aer_FileLevel,Aer_FileLevel);
//            if(UIUtil.isNotNullAndNotEmpty(Aer_SecrecyLevel))newObj.setAttributeValue(context,attribute_Aer_FileLevel,Aer_SecrecyLevel);
//
            String objectId = newObj.getObjectId(context);

            reMap.put("objectId", objectId);
            reMap.put("id", objectId);
        } catch (Exception ex) {
            ex.printStackTrace();
            throw ex;
        }
        return reMap;

    }

    /*
     *
     *创建文档obj
     * */
    public Map createDocObjPost(Context context, String[] args) throws Exception {
        Map reMap = new HashMap<>();

        try {
            Map program = JPO.unpackArgs(args);
            _logger.info("program>>>>>>>" + program);


        } catch (Exception ex) {
            ex.printStackTrace();
            throw ex;
        }
        return reMap;

    }

    public boolean checkInDoc(Context context, DomainObject doObj, String savePath, String fileName) throws Exception {
        boolean isOk = true;
        try {
            doObj.checkinFile(context, false, true, DomainConstants.EMPTY_STRING, "generic", fileName, savePath);
            _logger.info("docObj.checkinFile(context, false, true, DomainConstants.EMPTY_STRING, \"generic\", filename, dirPath) : " + fileName);


        } catch (Exception ex) {
            isOk = false;
            ex.printStackTrace();
            throw ex;
        }

        return isOk;
    }


    /**
     * @return int
     * @description 通过服务器上的文件上传到文档对象
     * @param[1] context
     * @param[2] args
     * @author Yang Le
     * @time 2024/3/11 10:27
     */
    public static int checkinBus_Zh(Context context, String[] args) throws Exception {
        if (!context.isConnected()) {
            throw new Exception(ComponentsUtil.i18nStringNow("emxComponents.Generic.NotSupportedOnDesktopClient", context.getLocale().getLanguage()));
        }
        try {
            String oid = null;
            String filePath = null;
            String fileName = null;
            String format = null;
            String store = null;
            String unlock = null;
            String server = null;
            String comments = null;
            String oldFileName = null;
            try {
                oid = args[0];
                filePath = args[1];
                fileName = args[2];
                format = args[3];
                store = args[4];
                unlock = args[5];
                server = args[6];
                comments = args[7];
            } catch (Exception ex) {
                //Ignore exception
            }
            if (oid == null || "".equals(oid)) {
                throw new Exception(ComponentsUtil.i18nStringNow("emxComponents.CommonDocumentBase.ObjectIdNotEmpty", context.getLocale().getLanguage()));
            }
            if (fileName == null || "".equals(fileName)) {
                throw new Exception(ComponentsUtil.i18nStringNow("emxComponents.CommonDocumentBase.FileNameNotEmpty", context.getLocale().getLanguage()));
            }
            if (format == null || "".equals(format)) {
                throw new Exception(ComponentsUtil.i18nStringNow("emxComponents.CommonDocumentBase.FileFormateNotEmpty", context.getLocale().getLanguage()));
            }
            if (store == null || "".equals(store)) {
                throw new Exception(ComponentsUtil.i18nStringNow("emxComponents.CommonDocumentBase.FileStoreNameNotEmpty", context.getLocale().getLanguage()));
            }

            if (unlock != null && "true".equalsIgnoreCase(unlock)) {
                unlock = "unlock";
            } else {
                unlock = "";
            }
            if (server == null || "".equals(server) || "null".equals(server)) {
                server = "server";
            }
            server = server.toLowerCase();
            if (!"server".equals(server) && !"client".equals(server)) {
                server = "server";
            }
            if (oldFileName == null || "".equals(oldFileName) || "null".equals(oldFileName)) {
                oldFileName = fileName;
            }
            Map attrMap = new HashMap();
            ContextUtil.startTransaction(context, true);
            CommonDocument object = new CommonDocument(oid);
            StringList selectList = new StringList();
            selectList.add(CommonDocument.SELECT_FILE_NAMES_OF_ACTIVE_VERSION);
            selectList.add(CommonDocument.SELECT_ACTIVE_FILE_LOCKER);
            selectList.add(CommonDocument.SELECT_MOVE_FILES_TO_VERSION);
            Map selectMap = object.getInfo(context, selectList);
            boolean moveFilesToVersion = Boolean.valueOf((String) selectMap.get(CommonDocument.SELECT_MOVE_FILES_TO_VERSION)).booleanValue();
            if (comments != null && !"".equals(comments) && !"null".equals(comments)) {
                attrMap.put(CommonDocument.ATTRIBUTE_CHECKIN_REASON, comments);
            }

            String objectId = object.reviseVersion(context, oldFileName, fileName, attrMap);
            if (objectId == null) {
                String errorMessage = i18nNow.getI18nString("emxComponents.CommonDocument.DocumentsAreNotLockedByUser", "emxComponentsStringResource", context.getSession().getLanguage());
                throw new Exception(errorMessage + " \r\n" + oldFileName);
            }
            System.out.println("");
            if (!moveFilesToVersion) {
                objectId = oid;
            }
            BusinessObject bo = new BusinessObject(objectId);
            bo.open(context);
            bo.checkinFile(context, Boolean.parseBoolean(unlock), true, "", format, fileName, filePath);
            bo.close(context);
            //由useraget 签入，更改所有者和协作区
            ContextUtil.commitTransaction(context);
            return 0;
        } catch (Exception ex) {
            ContextUtil.abortTransaction(context);
            ex.printStackTrace();
            throw ex;
        }
    }

    private static final String PARAM_TRUE = "true";
    private static final String PARAM_NULL = "null";
    private static final String EMXCOMPONENTS_STR_RESOURCE = "emxComponentsStringResource";

    /*
     * ootb code
     * */
    public int checkinBus(Context context, String[] args) throws Exception {
        if (!context.isConnected()) {
            throw new Exception(ComponentsUtil.i18nStringNow("emxComponents.Generic.NotSupportedOnDesktopClient", context.getLocale().getLanguage()));
        }
        try {
            String oid = null;
            String filePath = null;
            String fileName = null;
            String format = null;
            String store = null;
            String unlock = null;
            String server = null;
            String comments = null;
            String oldFileName = null;
            try {
                oid = args[0];
                filePath = args[1];
                fileName = args[2];
                format = args[3];
                store = args[4];
                unlock = args[5];
                server = args[6];
                comments = args[7];
            } catch (Exception ex) {
                //Ignore exception
            }
            if (oid == null || "".equals(oid)) {
                throw new Exception(ComponentsUtil.i18nStringNow("emxComponents.CommonDocumentBase.ObjectIdNotEmpty", context.getLocale().getLanguage()));
            }
            if (fileName == null || "".equals(fileName)) {
                throw new Exception(ComponentsUtil.i18nStringNow("emxComponents.CommonDocumentBase.FileNameNotEmpty", context.getLocale().getLanguage()));
            }
            if (format == null || "".equals(format)) {
                throw new Exception(ComponentsUtil.i18nStringNow("emxComponents.CommonDocumentBase.FileFormateNotEmpty", context.getLocale().getLanguage()));
            }
            if (store == null || "".equals(store)) {
                throw new Exception(ComponentsUtil.i18nStringNow("emxComponents.CommonDocumentBase.FileStoreNameNotEmpty", context.getLocale().getLanguage()));
            }

            if (unlock != null && PARAM_TRUE.equalsIgnoreCase(unlock)) {
                unlock = "unlock";
            } else {
                unlock = "";
            }
            if (server == null || "".equals(server) || PARAM_NULL.equals(server)) {
                server = "server";
            }
            server = server.toLowerCase();
            if (!"server".equals(server) && !"client".equals(server)) {
                server = "server";
            }
            if (oldFileName == null || "".equals(oldFileName) || PARAM_NULL.equals(oldFileName)) {
                oldFileName = fileName;
            }
            Map attrMap = new HashMap();
            ContextUtil.startTransaction(context, true);
            CommonDocument object = (CommonDocument) DomainObject.newInstance(context, oid);
            StringList selectList = new StringList();
            selectList.add(CommonDocument.SELECT_FILE_NAMES_OF_ACTIVE_VERSION);
            selectList.add(CommonDocument.SELECT_ACTIVE_FILE_LOCKER);
            selectList.add(CommonDocument.SELECT_MOVE_FILES_TO_VERSION);
            Map selectMap = object.getInfo(context, selectList);
            StringList fileList = (StringList) selectMap.get(CommonDocument.SELECT_FILE_NAMES_OF_ACTIVE_VERSION);
            StringList fileLockerList = (StringList) selectMap.get(CommonDocument.SELECT_ACTIVE_FILE_LOCKER);
            boolean moveFilesToVersion = Boolean.valueOf((String) selectMap.get(CommonDocument.SELECT_MOVE_FILES_TO_VERSION)).booleanValue();
            if (comments != null && !"".equals(comments) && !PARAM_NULL.equals(comments)) {
                attrMap.put(CommonDocument.ATTRIBUTE_CHECKIN_REASON, comments);
            }

            String objectId = object.reviseVersion(context, oldFileName, fileName, attrMap);
            if (objectId == null) {
                String errorMessage = EnoviaResourceBundle.getProperty(context, EMXCOMPONENTS_STR_RESOURCE, context.getLocale(), "emxComponents.CommonDocument.DocumentsAreNotLockedByUser");
                throw new Exception(errorMessage + " \n" + oldFileName);
            }

            if (!moveFilesToVersion) {
                objectId = oid;
            }
            String cmd = "checkin bus $1 $2 $3 format $4 store $5 append $6";
            MqlUtil.mqlCommand(context, cmd, objectId, unlock, server, format, store, filePath + File.separatorChar + fileName);

            ContextUtil.commitTransaction(context);
            return 0;
        } catch (Exception ex) {
            ex.printStackTrace();
            ContextUtil.abortTransaction(context);
            throw ex;

        }
    }


    /*
     * 文档创建的时候 根据选择的模板属性 自动上传模板
     * 不会升版 覆盖
     * */
    public void triggerIsDocCreateWithTemplateDoc(Context context, String[] args) {
        String docId = args[0];
        try {
            _logger.info(" triggerIsDocCreateWithTemplateDoc>>>start>>>>");
            DomainObject doDoc = DomainObject.newInstance(context, docId);
            //String templateDocName=doDoc.
            Attribute temp_attribute = doDoc.getAttributeValues(context, attribute_Aer_TemplateDocName);//模板名称
            if (temp_attribute != null) {


                StringList slAttrValue = temp_attribute.getValueList();
                _logger.info("slAttrValue>>>>>>>" + slAttrValue);
                StringListRemoveDuplicates(slAttrValue);
                if (UIUtil.isNullOrEmpty(String.valueOf(slAttrValue)) || "[]".equals(slAttrValue.toString())) {
                    _logger.info("模板属性为空 >>>>>>>" + slAttrValue);
                    return;
                }
                String templateDocKey = (String) config_properties.get("templateDocName");
                String templateDocPath = (String) config_properties.get("templateDocPath");
                if (UIUtil.isNotNullAndNotEmpty(templateDocPath)) {
                    File isFolder = new File(templateDocPath);
                    if (!isFolder.exists()) {
                        isFolder.mkdirs();
                    }

                }
                for (int i = 0; i < slAttrValue.size(); i++) {
                    String docRangeName = slAttrValue.get(i);
                    String tempDocName = (String) config_properties.get(templateDocKey + "." + docRangeName);
                    if (UIUtil.isNotNullAndNotEmpty(tempDocName)) {
                        String fileFullPath = templateDocPath + tempDocName;
                        File file = new File(fileFullPath);
                        if (file.exists()) {
                            String suffix = tempDocName.replaceAll(".*(\\..*)", "$1");
                            //命名规则 讨论
                            //String newDocName=System.currentTimeMillis()+"_"+tempDocName;
                            String newDocName = "New_" + tempDocName;
                            String tmpFileFullPath = templateDocPath + "tmpPath";//临时路径用来复制一份原文件,进行重名名上传
                            copyFileAndReName(tempDocName, templateDocPath, tmpFileFullPath, newDocName);

                            String[] argFiles = new String[8];
                            argFiles[0] = docId; //bus id
                            argFiles[1] = tmpFileFullPath;
                            argFiles[2] = newDocName;
                            argFiles[3] = "generic";
                            argFiles[4] = "STORE";
                            argFiles[5] = "TRUE";
                            argFiles[6] = "";
                            argFiles[7] = "";
                            checkinBus_Zh(context, argFiles);
                            deleteFile(tmpFileFullPath, newDocName);
                            // checkInDoc(context,doDoc,tmpFileFullPath+"/",newDocName);
                        } else {
                            System.out.println("根据模板上传文件失败 ，服务器该路径下不存在此文件 : " + fileFullPath);
                        }


                    }


                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    private static final String appendingSuffix = "-hist";

    /*
     *
     * 在线编辑保存文件
     * */
    public Map saveOnlineDocAction(Context context, String[] args) {
        Map reValMap = new HashMap<>();
        try {
            // strDocId,file3deName,onlineDocFileName
            String objectId = args[0];
            String file3deName = args[1];
            String onlineDocFileName = args[2];
            _logger.info("saveOnlineDocAction objectId >> " + objectId);
            _logger.info(" onlineDocFileName >> " + onlineDocFileName);

            //服务器预览编辑地址
            String localEditSavePath = config_properties.getProperty("DocOnlineLocalEditSaveDirectory");
            //重命名上传
            String newLocalTmpSavePath = "";
            String newFileName = "";
            if (UIUtil.isNotNullAndNotEmpty(objectId)) {
                newLocalTmpSavePath = localEditSavePath + "Temp/";
                _logger.info("上传回3de名称 newLocalSavePath " + newLocalTmpSavePath);

 /*               String   DocOnlineLocalSavePath=config_properties.getProperty("DocOnlineLocalSavePath");
                File file=new File(DocOnlineLocalSavePath + docName);
                if(file.exists()){
                    String suffixPath=DocOnlineLocalSavePath + docName + appendingSuffix;
                    File folderSuffix=new File(suffixPath);*/

                String lastNewSaveFilePath = localEditSavePath + "" + onlineDocFileName + "" + appendingSuffix;
                String lastPath = lastNewSaveFilePath + "/" + onlineDocFileName;
                lastPath = lastPath.replaceAll("//", "/");
                _logger.info("oldPath>>>" + lastPath);
                File oldPaths = new File(lastPath);
                if (!oldPaths.exists()) {
                    lastNewSaveFilePath = localEditSavePath;
                }

                boolean isOK = copyFileAndReName(onlineDocFileName, lastNewSaveFilePath, newLocalTmpSavePath, file3deName);
                if (isOK) {
//                  DomainObject doDOC=DomainObject.newInstance(context,objectId);
//                  //升版
//                  checkInDoc(context,doDOC,newLocalTmpSavePath,file3deName);
//                  deleteFile(newLocalTmpSavePath,file3deName);
//                  //覆盖
//                 //checkinBus_Zh()

                    checkinDocObjFileAutoRevise(context, new String[]{objectId, file3deName, newLocalTmpSavePath});
                    _logger.info("上传文件成功 删除临时文件开始 ");
                    deleteFile(newLocalTmpSavePath, file3deName);
                    _logger.info("上传文件成功 删除临时文件success ");

                    reValMap.put("flags", "success");
                    _logger.info("上传文件保存成功 ");

                } else {

                    _logger.info("上传文件失败 ");
                    reValMap.put("flags", "error");
                }

            } else {
                _logger.info("上传文件失败  not Doc Id objectId " + objectId);
                reValMap.put("flags", "error");
            }

        } catch (Exception ex) {
            reValMap.put("flags", "error");
            ex.printStackTrace();
        }
        _logger.info("reValMap " + reValMap);

        return reValMap;

    }


    /*
     *
     * 在线编辑保存文件覆盖模式
     * */
    public Map saveOnlineDocAction_Cover(Context context, String[] args) {
        Map reValMap = new HashMap<>();
        try {
            // strDocId,file3deName,onlineDocFileName
            String objectId = args[0];
            String file3deName = args[1];
            String onlineDocFileName = args[2];
            _logger.info("saveOnlineDocAction objectId >> " + objectId);
            _logger.info(" onlineDocFileName >> " + onlineDocFileName);

            //服务器预览编辑地址
            String localEditSavePath = config_properties.getProperty("DocOnlineLocalEditSaveDirectory");
            //重命名上传
            String newLocalTmpSavePath = "";
            String newFileName = "";
            if (UIUtil.isNotNullAndNotEmpty(objectId)) {
                newLocalTmpSavePath = localEditSavePath + "Temp/";
                _logger.info("上传回3de名称 newLocalSavePath " + newLocalTmpSavePath);

 /*               String   DocOnlineLocalSavePath=config_properties.getProperty("DocOnlineLocalSavePath");
                File file=new File(DocOnlineLocalSavePath + docName);
                if(file.exists()){
                    String suffixPath=DocOnlineLocalSavePath + docName + appendingSuffix;
                    File folderSuffix=new File(suffixPath);*/

                String lastNewSaveFilePath = localEditSavePath + "" + onlineDocFileName + "" + appendingSuffix;
                String lastPath = lastNewSaveFilePath + "/" + onlineDocFileName;
                lastPath = lastPath.replaceAll("//", "/");
                _logger.info("oldPath 1>>>" + lastPath);
                File oldPaths = new File(lastPath);
                if (!oldPaths.exists()) {
                    lastNewSaveFilePath = localEditSavePath;
                }

                boolean isOK = copyFileAndReName(onlineDocFileName, lastNewSaveFilePath, newLocalTmpSavePath, file3deName);
                if (isOK) {

                    String[] argFiles = new String[8];
                    argFiles[0] = objectId; //bus id
                    argFiles[1] = newLocalTmpSavePath;
                    argFiles[2] = file3deName;
                    argFiles[3] = "generic";
                    argFiles[4] = "STORE";
                    argFiles[5] = "TRUE";
                    argFiles[6] = "";
                    argFiles[7] = "";
                    checkinBus_Zh(context, argFiles);

                    _logger.info("上传文件成功 删除临时文件开始 ");
                    deleteFile(newLocalTmpSavePath, file3deName);
                    _logger.info("上传文件成功 删除临时文件success ");

                    reValMap.put("flags", "success");
                    _logger.info("上传文件保存成功 ");

                } else {

                    _logger.info("上传文件失败 ");
                    reValMap.put("flags", "error");
                }

            } else {
                _logger.info("上传文件失败  not Doc Id objectId " + objectId);
                reValMap.put("flags", "error");
            }

        } catch (Exception ex) {
            reValMap.put("flags", "error");
            ex.printStackTrace();
        }
        _logger.info("reValMap " + reValMap);

        return reValMap;

    }


    public static String relationship_Active_Version = "Active Version";

    /*
     * 根据文档对象去上传文件，同名文件升版，不同文件名称新建关联
     * */
    public void checkinDocObjFileAutoRevise(Context context, String[] args) {
        try {
            String docId = args[0];
            String fileName = args[1];
            String filePath = args[2];
            try {
                DomainObject domainObject = new DomainObject(docId);
                StringList versionDocIdSL = domainObject.getInfoList(context, "from[" + relationship_Active_Version + "|to.attribute[Title]=='" + fileName + "'].to.id", false);
                if (versionDocIdSL.size() > 0) {
                    String versionId = versionDocIdSL.get(0);
                    String[] argFiles = new String[8];
                    argFiles[0] = docId;
                    argFiles[1] = versionId; //bus id
                    argFiles[2] = fileName;
                    argFiles[3] = fileName;
                    argFiles[4] = "generic";
                    argFiles[5] = filePath;

                    checkinVersionFileAutoRevise(context, argFiles);
                } else {
                    String[] argFiles = new String[8];
                    argFiles[0] = docId; //bus id
                    argFiles[1] = filePath;
                    argFiles[2] = fileName;
                    argFiles[3] = "generic";
                    argFiles[4] = "STORE";
                    argFiles[5] = "TRUE";
                    argFiles[6] = "";
                    argFiles[7] = "";
                    checkinBus_Zh(context, argFiles);

                }

            } catch (Exception ex) {
                ex.printStackTrace();
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }


    /*
     * 根据文件对象去升版上传文件更新
     * */
    public void checkinVersionFileAutoRevise(Context context, String[] args) {
        try {
            String docId = args[0];
            String versionId = args[1];
            String oldfilename = args[2];
            String filename = args[3];
            String format = args[4];
            String dirPath = args[5];
            DomainObject versionObj = DomainObject.newInstance(context, versionId);
            //1.文件对象加锁
            boolean locked = versionObj.isLocked(context);
            _logger.info("locked 1: " + locked);
            if (!locked) {
                versionObj.lock(context);
            }
            locked = versionObj.isLocked(context);
            _logger.info("locked 2: " + locked);
            CommonDocument commonDocument = new CommonDocument(docId);

            //升版文件 如 oldfilename, filename 不一致 则用filename
            String newVersionId = commonDocument.reviseVersion(context, oldfilename, filename, new HashMap());

            commonDocument.checkinFile(context, true, true, DomainConstants.EMPTY_STRING, format, filename, dirPath);
            _logger.info("commonDocument.checkinFile : " + dirPath);

            //DomainObject versionObj = DomainObject.newInstance(context, versionId);


//            //升版文件名相同
//            versionId = commonDocument.reviseVersion(context, filename, filename, new HashMap());
//            //创建 Document 文件( policy Version)对象
//            versionId = commonDocument.createVersion(context, "", filename, new HashMap());

            _logger.info("checkinVersionFileAutoRevise>>> 根据文件对象上传文件成功  newVersionId " + newVersionId);

        } catch (Exception ex) {
            _logger.info("checkinVersionFileAutoRevise>>> 根据文件对象上传文件失败");

            ex.printStackTrace();
        }

    }


    //删除支持文件
    private void deleteFile(String path, String fileName) {
        try {
            String dePath = path + "/" + fileName;
            dePath = dePath.replaceAll("//", "/");
            File oldPaths = new File(dePath);
            if (oldPaths.exists()) {
                oldPaths.delete();
                _logger.info("delete file success path " + dePath);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    //删除文件夹下所有内容
    private void deleteAllFile(String path) {
        try {
            File directory = new File(path);

            File[] files = directory.listFiles();
            if (files != null && files.length > 0) {

                for (File file : files) {

                    if (file.isDirectory()) {

                        deleteAllFile(file.getPath());

                    }
                    file.delete();

                }
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    //文件移动到指定文件
    private Boolean copyFile(String filename, String oldpath, String newpath) {
        try {
            File oldPaths = new File(oldpath + "/" + filename);
            File newPaths = new File(newpath + "/" + filename);

            if (!newPaths.exists()) {
                Files.copy(oldPaths.toPath(), newPaths.toPath());
            } else {
                newPaths.delete();
                Files.copy(oldPaths.toPath(), newPaths.toPath());
                return true;
            }

        } catch (IOException e) {
            e.printStackTrace();

        }
        return false;
    }

    //文件移动到指定文件夹并重命名;
    private Boolean copyFileAndReName(String filename, String oldpath, String newpath, String newFileName) {
        try {
            String oldPath = oldpath + "/" + filename;
            oldPath = oldPath.replaceAll("//", "/");
            _logger.info("oldPath>>>" + oldPath);
            File oldPaths = new File(oldPath);
            newpath = newpath.replaceAll("//", "/");
            File newDir = new File(newpath);
            _logger.info("newpath>>>" + newpath);
            if (!newDir.exists()) {
                newDir.mkdirs();
            }
            File newPaths = new File(newpath + "/" + filename);

            if (!newPaths.exists()) {
                Files.copy(oldPaths.toPath(), newPaths.toPath());
            } else {
                newPaths.delete();
                Files.copy(oldPaths.toPath(), newPaths.toPath());

            }
            if (newPaths.exists()) {
                boolean isRenamed = newPaths.renameTo(new File(newpath + "/" + newFileName));
                if (isRenamed) {
                    _logger.info("文件移动到指定文件夹并重命名 Success 路径： " + newpath + "/" + newFileName);
                } else {
                    _logger.info("文件移动到指定文件夹并重命名 fail ");
                }

                return true;
            }

        } catch (IOException e) {
            e.printStackTrace();

        }
        return false;
    }


    /*
     * 重命名文件
     * */
    public boolean renameFile(String oldPath, String newPath) throws Exception {
        boolean isOK = true;
        try {

            File file = new File(oldPath);
            if (file.exists()) {
                boolean isRenamed = file.renameTo(new File(newPath));

                if (isRenamed) {
                    _logger.info("文件已成功重命名！");

                } else {
                    isOK = false;
                    _logger.info("文件重命名失败！");
                }
            } else {
                isOK = false;
                _logger.info("指定的文件不存在！" + oldPath);
            }
        } catch (Exception ex) {
            isOK = false;
            ex.printStackTrace();
        }
        return isOK;
    }


    /*
     * StringList 去重
     * */
    public static void StringListRemoveDuplicates(StringList sl) {
        HashSet h = new HashSet(sl);
        sl.clear();
        sl.addAll(h);
    }

    /*
     *
     * 不能随意从冻结降至工作中
     * */
    public int triggerIsDocCannotDemoteToInWork(Context context, String[] args) {
        int isOk = 0;
        try {
            String docId = args[0];
            String policy = args[1];
            String event = args[2];
            _logger.info(" triggerIsDocCannotDemoteToInWork event >>" + event);

            if ("Demote".equals(event) && "Document Release".equals(policy)) {

                DomainObject doDoc = DomainObject.newInstance(context, docId);
                String current = doDoc.getInfo(context, DomainConstants.SELECT_CURRENT);
                if ("FROZEN".equals(current)) {
                    _logger.info(" triggerIsDocCannotDemoteToInWork current >>" + current);
                    String errorMsg = "文档升级至冻结，不允许退回至工作中！";
                    MqlUtil.mqlCommand(context, "notice $1", errorMsg);
                    //  isOK = 1; dashboard 可见错误
                    throw new Exception(errorMsg);
                }


            }
        } catch (Exception ex) {
            isOk = 1;
            ex.printStackTrace();
        }
        return isOk;
    }


    /**
     * @return void
     * @Description预发布单发布时，关联文档修改预发布状态
     * @Author: Nisha
     * @Date: 2024/9/12 16:10
     * @param:[context, args]
     **/
    public void triggerAddDocAttributeModelDocPreRelease(Context context, String args[]) {
        System.out.println("triggerAddDocAttributeModelDocPreRelease ================");
        try {
            String preReleaseId = args[0];
            DomainObject doPreObj = DomainObject.newInstance(context, preReleaseId);
            String preType = doPreObj.getAttributeValue(context, "Aer_PreReleaseType");

            // mod by wwy 2024-11-01 数模对象 预发布标识 属性修改 start start
            if (UIUtil.isNotNullAndNotEmpty(preType)) {

                StringList selects = new StringList();
                selects.add(DomainConstants.SELECT_ID);
                selects.add(DomainConstants.SELECT_TYPE);
                selects.add(DomainConstants.SELECT_CURRENT);
                selects.add("attribute[Aer_PreReleaseType].value");
                selects.add("state[Complete].actual");

                MapList relVPMList = doPreObj.getRelatedObjects(context, "Aer_PreReleaseVPMRefRel", "VPMReference",
                        selects, null, false, true, (short) 1, "", null, 0);
                System.out.println("relDateList===" + relVPMList);

                if (relVPMList.size() > 0) {
                    for (int i = 0; i < relVPMList.size(); i++) {
                        String vpmId = (String) ((Map) relVPMList.get(i)).get(DomainObject.SELECT_ID);
                        DomainObject vpmObj = DomainObject.newInstance(context, vpmId);
                        String sWhere = "current == Complete";
                        MapList relPREList = vpmObj.getRelatedObjects(context, "Aer_PreReleaseVPMRefRel", "Aer_PreRelease",
                                selects, null, true, false, (short) 1, sWhere, "from.id != '" + preReleaseId + "'", 0);
                        String value = "";
                        if (relPREList.size() > 0) {
                            // 预发布标识 构型数据预发布成功，物料预发布成功
                            if ("VPMReference".equals(preType)) {
                                value = "ConfigurationDataPreReleaseYMaterialPreReleaseY";
                            }
                            // 预发布标识 物料预发布成功，构型数据预发布成功
                            else if ("Document".equals(preType)) {
                                value = "MaterialPreReleaseYConfigurationDataPreReleaseY";
                            }
                        } else {
                            // 预发布标识 物料预发布成功
                            if ("VPMReference".equals(preType)) {
                                value = "MaterialPreReleaseY";
                            }
                            // 预发布标识 	构型数据预发布成功
                            else if ("Document".equals(preType)) {
                                value = "ConfigurationDataPreReleaseY";
                            }
                        }
                        System.out.println("relPREList===" + relPREList);
                        MqlUtil.mqlCommand(context, "trigger off;");
                        vpmObj.setAttributeValue(context, "Aer_VPMReferenceExt.Aer_PreReleaseIdentification", value);
                        MqlUtil.mqlCommand(context, "trigger on;");

                    }
                }
            }
            // add by wwy 2024-11-01 数模对象 预发布标识 属性修改 属性移除 end


//            if (UIUtil.isNotNullAndNotEmpty(preType) && "VPMReference".equals(preType)) {
//                return;
//            }
//            StringList slDocId = doPreObj.getInfoList(context, "from[Aer_PreReleaseVPMRefRel].to.id");
//            DomainObject objDoc = DomainObject.newInstance(context);
//            for (int i = 0; i < slDocId.size(); i++) {
//                String strTempId = slDocId.get(i);
//                objDoc.setId(strTempId);
//                String strType = objDoc.getInfo(context, "type");
//                if (!strType.equals("Document")) {
//                    continue;
//                } else {
//                    BusinessInterfaceList businessInterfaces = objDoc.getBusinessInterfaces(context);
//                    if (UIUtil.isNullOrEmpty(String.valueOf(businessInterfaces.find("Aer_DocumentExt3")))) {
//                        BusinessInterface busInterface = new BusinessInterface("Aer_DocumentExt3", context.getVault());
//                        objDoc.addBusinessInterface(context, busInterface);
//                    }
//                    objDoc.setAttributeValue(context, "Aer_ModelDocPreRelease", "DOCPreReleaseSuccess");
//                }
//
//            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


}