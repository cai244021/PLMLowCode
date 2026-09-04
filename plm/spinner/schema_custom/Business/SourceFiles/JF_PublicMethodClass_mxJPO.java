import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.dscn.plm.util.NioJDUtils;
import com.google.gson.Gson;
import com.matrixone.apps.common.CommonDocument;
import com.matrixone.apps.common.Route;
import com.matrixone.apps.common.util.ComponentsUtil;
import com.matrixone.apps.common.util.DocumentUtil;
import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.DomainRelationship;
import com.matrixone.apps.domain.util.*;
import com.matrixone.apps.framework.ui.MqlNoticeUtil;
import com.matrixone.apps.framework.ui.ProgramCallable;
import com.matrixone.apps.framework.ui.UICache;
import com.matrixone.apps.framework.ui.UIUtil;
import com.matrixone.apps.program.ProgramCentralConstants;
import com.matrixone.enovia.bps.notifications.NotificationService;
import com.dassault_systemes.smasds.powerby.services.util.ServiceUtils;
import com.nomagic.esi.common.a.M;
import javassist.compiler.ast.StringL;
import matrix.db.File;
import matrix.db.*;
import matrix.util.SelectList;
import matrix.util.StringList;
import org.apache.commons.text.StringEscapeUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.SecureRandom;
import java.text.DecimalFormat;
import java.util.*;
import java.util.Set;
import java.util.stream.Collectors;

import static com.matrixone.apps.domain.DomainConstants.*;
import static com.matrixone.apps.domain.DomainConstants.SELECT_NAME;
import static com.matrixone.apps.domain.MultiValueSelects.RELATIONSHIP_REFERENCE_DOCUMENT;

/**
 * @ClassName JF_PublicMethodClass_mxJPO
 * @Author: LIUJR
 * @CreateDate: 2024/7/10 10:31
 * @UpdateRemark:
 * @Version: 1.0
 * @Description:
 */
public class JF_PublicMethodClass_mxJPO implements JF_PLMConstants_mxJPO{

    private static final Logger JF_LOGGER = LoggerFactory.getLogger(JF_PublicMethodClass_mxJPO.class);
    private static final StringList strBusSelectsList = new StringList();
    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final String ATTRIBUTE_JF_TOPIC = "JSTopic";
    private static final String STRING_MQL_ATTRIBUTE = "attribute[%s].value";
    private static final String STRING_MQL_RELATIONSHIP_FROM = "from[%s].to.%s";
    private static final String STRING_MQL_RELATIONSHIP_TO = "from[%s].to.%s";
    private static final String TYPE_JF_DATASOURCE = "JFDataOutSource";
    private static final String ATTRIBUTE_JF_LINE_MANAGER = "JF_Line_Manager";
    private static final String ATTRIBUTE_DOCUMENT_JF_PDFSOURCEFILEDID = "JF_PDFSourceFileId";

    private static final StringList versionSelectList = new StringList();

    static {
        strBusSelectsList.add(DomainConstants.SELECT_ID);
        strBusSelectsList.add(DomainConstants.SELECT_NAME);
        strBusSelectsList.add(DomainConstants.SELECT_OWNER);
        strBusSelectsList.add(DomainConstants.SELECT_TYPE);

        versionSelectList.add(CommonDocument.SELECT_ID);
        versionSelectList.add(CommonDocument.SELECT_REVISION);
        versionSelectList.add(CommonDocument.SELECT_DESCRIPTION);
        versionSelectList.add(CommonDocument.SELECT_LOCKED);
        versionSelectList.add(CommonDocument.SELECT_LOCKER);
        versionSelectList.add(CommonDocument.SELECT_TITLE);
        versionSelectList.add(CommonDocument.SELECT_FILE_NAME);
        versionSelectList.add(CommonDocument.SELECT_FILE_FORMAT);
        versionSelectList.add(CommonDocument.SELECT_FILE_MODIFIED);
        versionSelectList.add(CommonDocument.SELECT_FILE_SIZE);
        versionSelectList.add(CommonDocument.SELECT_OWNER);
        versionSelectList.add("physicalid");
        versionSelectList.add(DomainConstants.SELECT_ORIGINATED);
        versionSelectList.add(DomainConstants.SELECT_TYPE);
        versionSelectList.add(CommonDocument.SELECT_SUSPEND_VERSIONING);
        versionSelectList.add(CommonDocument.SELECT_IS_VERSION_OBJECT);
        versionSelectList.add(String.format(STRING_MQL_ATTRIBUTE, ATTRIBUTE_DOCUMENT_JF_PDFSOURCEFILEDID));
    }

    /**
    * 修改table中 对象的属性值
    * @param context
	* @param args
    * @author LIUJR
    * @throws
    * @return void
    * @date 2024/7/10 10:32
    * @description
    */
    public static void updateTableAttributeValue(Context context, String[] args) throws Exception {
        try {
            ContextUtil.pushContext(context);
            Map paramsMap = (Map) JPO.unpackArgs(args);
            Map columnMap = (Map) paramsMap.get(STRING_COLUMNMAP);
            String attrName = (String) columnMap.get(DomainConstants.SELECT_NAME);
            HashMap paramMap = (HashMap)paramsMap.get(STRING_PARAMMAP);
            String objectId = (String)paramMap.get(STRING_OBJECTID);
            String newValue = (String)paramMap.get(STRING_NEW_VALUE);
            DomainObject domainObject = DomainObject.newInstance(context, objectId);
            domainObject.setAttributeValue(context, attrName, newValue);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        } finally {
            ContextUtil.popContext(context);
        }
    }

    /**
     * 修改对象的基本信息
     * @param context
     * @param args
     * @author LIUJR
     * @throws
     * @return void
     * @date 2024/7/9 18:12
     * @description
     */
    public static void updateTableObjectBasic(Context context, String[] args) throws Exception{
        try {
            Map paramsMap = (Map) JPO.unpackArgs(args);
            Map columnMap = (Map) paramsMap.get(STRING_COLUMNMAP);
            String basicName = (String) columnMap.get(DomainConstants.SELECT_NAME);
            HashMap paramMap = (HashMap)paramsMap.get(STRING_PARAMMAP);
            String objectId = (String)paramMap.get(STRING_OBJECTID);
            String newValue = (String)paramMap.get(STRING_NEW_VALUE);
            DomainObject domainObject = DomainObject.newInstance(context, objectId);
            if ("Description".equalsIgnoreCase(basicName)) {
                domainObject.setDescription(context, newValue);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * 获取当前修改的属性名称，并获取他的range值
     * @param context
     * @param args
     * @author LIUJR
     * @throws
     * @return java.util.Map
     * @date 2024/7/9 17:55
     * @description
     */
    public static Map getAttributeRangeWithTable(Context context, String[] args) throws Exception {
        Map resuleMap = new HashMap();
        try {
            Map paramsMap = (Map) JPO.unpackArgs(args);
            Map columnMap = (Map) paramsMap.get(STRING_COLUMNMAP);
            String attrName = (String) columnMap.get(DomainConstants.SELECT_NAME);
            resuleMap = JF_PublicMethodClass_mxJPO.getAttrRanges(context, new String[]{attrName});
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
        return resuleMap;
    }

    /**
    * 修改form中属性的值
    * @param context
	* @param args
    * @author LIUJR
    * @throws
    * @return void
    * @date 2024/7/10 11:01
    * @description
    */
    public static void updateFormAttributeValue(Context context, String[] args) throws Exception {
        try {
            Map paramsMap = (Map) JPO.unpackArgs(args);
            //存储属性的名称fieldMap
            Map fieldMap = (Map) paramsMap.get(STRING_FIELDMAP);
            //属性名称
            String attrName = (String) fieldMap.get(DomainConstants.SELECT_NAME);
            //参数Map,存放数据的
            HashMap paramMap = (HashMap)paramsMap.get(STRING_PARAMMAP);
            //objectId
            String objectId = (String)paramMap.get(STRING_OBJECTID);
            //新值
            String newValue = (String)paramMap.get(STRING_NEW_VALUE);
            //旧值
            String oldValue = (String)paramMap.get("Old Value");
            //修改值
            if (!newValue.equalsIgnoreCase(oldValue)) {
                DomainObject domainObject = DomainObject.newInstance(context, objectId);
                domainObject.setAttributeValue(context, attrName, newValue);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
    * 获取对象的属性 构造一个链接地址
    * @param context
	* @param args
    * @author LIUJR
    * @throws
    * @return java.lang.String
    * @date 2024/7/10 13:24
    * @description
    */
    public static String getAttributeWithIdOneTarget(Context context, String[] args) throws Exception{
        String strResult = DomainConstants.EMPTY_STRING;
        try {
            ContextUtil.pushContext(context);
            Map paramsMap = (Map) JPO.unpackArgs(args);
            String objectId = (String) paramsMap.get("IdWithObject");
            String strAttrName = (String) paramsMap.get("AttributeWithObject");
            if (UIUtil.isNullOrEmpty(objectId) && UIUtil.isNullOrEmpty(strAttrName)) {
                //存储属性的名称fieldMap
                Map fieldMap = (Map) paramsMap.get(STRING_FIELDMAP);
                //属性名称
                strAttrName = (String) fieldMap.get(DomainConstants.SELECT_NAME);
                //参数Map,存放数据的
                HashMap paramMap = (HashMap)paramsMap.get(STRING_PARAMMAP);
                //objectId
                objectId = (String)paramMap.get(STRING_OBJECTID);
            }
            DomainObject objectJFDataOutSource = DomainObject.newInstance(context, objectId);
            //id
            String attributeValueId = EMPTY_STRING;
            if ("JSConnectProjectNumber".equalsIgnoreCase(strAttrName)) {
                attributeValueId = objectJFDataOutSource.getAttributeValue(context, "JSConnectProject");
            } else {
                attributeValueId = objectJFDataOutSource.getAttributeValue(context, strAttrName);
            }
            StringBuilder strBuilder = new StringBuilder();
            if (UIUtil.isNotNullAndNotEmpty(attributeValueId)) {
                if (attributeValueId.split("\\.").length != 4)  {
                    strBuilder.append(getConstructDataString(context, objectId, attributeValueId));
                } else {
                    BusinessObject businessObject = new BusinessObject(attributeValueId);
                    if (businessObject.exists(context)) {
                        DomainObject domainObject = DomainObject.newInstance(context, attributeValueId);
                        String strName = domainObject.getInfo(context, DomainConstants.SELECT_NAME);
                        if (TYPE_PROJECT_SPACE.equals(domainObject.getInfo(context, SELECT_TYPE))) {
                            if ("JSConnectProjectNumber".equalsIgnoreCase(strAttrName)) {
                                strBuilder.append(getConstructDataString(context, attributeValueId, strName));
                            } else {
                                strBuilder.append(domainObject.getInfo(context, SELECT_DESCRIPTION));
                            }
                        } else {
                            strBuilder.append(getConstructDataString(context, attributeValueId, strName));
                        }
                    }else{
                        strBuilder.append("");
                    }
                }
            }
            strResult = strBuilder.toString();
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        } finally {
            ContextUtil.popContext(context);
        }
        return strResult;
    }

    /**
     * @description  构造新tab页面链接
     * @param context
     * @param strObjectId
     * @param strName
     * @author LIUJR
     * @throws
     * @return java.lang.String
     * @date 2024/7/9 16:34
     */
    public static String getConstructDataString(Context context, String strObjectId, String strName) {
        StringBuilder strBuilder = new StringBuilder();
        String strBasicUrl = getBasicUrl(context, new String[]{"JF.3dspace.JFUrl"});
        strBuilder.append("<a href=\"" + StringEscapeUtils.escapeHtml4(strBasicUrl))
                .append(strObjectId).append("\" title=\"" + StringEscapeUtils.escapeHtml4(strName) + "\"  target=\"_blank\" >").append(StringEscapeUtils.escapeHtml4(strName));
        strBuilder.append("</a>");
        return strBuilder.toString();
    }

    /**
     * @description 获取当前环境的配置文件的项
     * @param context
     * @param args
     * @author LIUJR
     * @throws
     * @return java.lang.String
     * @date 2024/7/9 16:33
     */
    public static String getBasicUrl(Context context, String[] args) {
        String strUrl = "";
        try {
            String str = args[0];
            Properties prop = NioJDUtils.readPageObject(context, "JFJDConfig");
            strUrl = prop.getProperty(str);
            JF_LOGGER.info("strUrl:{}", strUrl);
        }catch (Exception e) {
            e.printStackTrace();
        }
        return strUrl;
    }

    /**
     * 获取table中的所有的对象id
     * @param args
     * @author LIUJR
     * @throws
     * @return matrix.util.StringList
     * @date 2024/7/9 16:35
     * @description
     */
    public static StringList getObjectIdList(String[] args) throws Exception {
        StringList objectIdList = new StringList();
        Map paramsMap = (Map)JPO.unpackArgs(args);
        MapList objectList = (MapList)paramsMap.get(STRING_OBJECTLIST);
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
    * 获取属性的range值
    * @param context
	* @param args
    * @author LIUJR
    * @throws
    * @return java.util.Map
    * @date 2024/7/10 13:21
    * @description
    */
    public static Map getAttrRanges(Context context, String[] args) throws Exception{
        HashMap resultMap = new HashMap();
        try{
            String attrName = args[0];
            String strLanguage = context.getSession().getLanguage();
            ContextUtil.pushContext(context);
            AttributeType attributeType = new AttributeType(attrName);
            attributeType.open(context);
            StringList choices = attributeType.getChoices();
            ContextUtil.popContext(context);
            StringList choicesValue = new StringList();
            for (int i = 0; i < choices.size(); i++) {
                if (UIUtil.isNullOrEmpty(choices.get(i))) {
                    choicesValue.add("");
                } else {
                    choicesValue.add(EnoviaResourceBundle.getRangeI18NString(context, attrName, choices.get(i), strLanguage));
                }
            }
            resultMap.put("field_choices", choices);
            resultMap.put("field_display_choices", choicesValue);
        } catch (Exception e) {
            JF_LOGGER.error(e.getStackTrace().toString());
            throw e;
        }
        return resultMap;
    }

    /**
     * 创建ESO任务权限
     * @param context
     * @param args
     * @author LIUJR
     * @throws
     * @return java.lang.Boolean
     * @date 2025/2/14 13:42
     * @description
     */
    public Boolean createESOAccess(Context context, String[] args) {
        Boolean flag = Boolean.FALSE;
        StringList selList = JF_Util_mxJPO.basicBolistSel();
        selList.add(SELECT_ID);
        try{
            Map params = JPO.unpackArgs(args);
            String strObjectId = (String) params.get("objectId");
            DomainObject projectObject = DomainObject.newInstance(context, strObjectId);
            if (!DomainConstants.TYPE_PROJECT_SPACE.equalsIgnoreCase(projectObject.getTypeName(context))) {
                //如果是任务 或者阶段
                String projectId = projectObject.getInfo(context, "to[Project Access Key].from.from[Project Access List].to.id");
                projectObject.setId(projectId);
            }
            //拿取项目的成员以及关系属性project role
            MapList mapList = projectObject.getRelatedObjects(
                    context,
                    DomainRelationship.RELATIONSHIP_MEMBER,
                    DomainConstants.TYPE_PERSON,
                    new StringList(SELECT_NAME),
                    new StringList(),
                    false,
                    true,
                    (short) 1, // recursion level
                    "", //object where clause
                    "attribute[Project Role]=='Chair manager'", //relationship where clause
                    0
            );
            if(mapList.size()>0) {
                Map peresonMap = (Map) mapList.get(0);
                String charMangerName = (String) peresonMap.get(SELECT_NAME);
                if (charMangerName.equalsIgnoreCase(context.getUser())) {
                    flag = Boolean.TRUE;
                } else {
                    flag = Boolean.FALSE;

                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            flag = Boolean.FALSE;
        }
        return flag;
    }

    /**
    *“导出项目任务”、“修改项目任务时间”功能命令对【ESO审核员】进行开放，允许【ESO审核员】创建及编制ESO任务；
    * @param context
	* @param args
    * @author LIUJR
    * @throws
    * @return java.lang.Boolean
    * @date 2025/9/23 10:06
    * @description
    */
    public Boolean importDownloadProjectTaskAccess(Context context, String[] args) {
        Boolean flag = Boolean.FALSE;
        StringList selList = JF_Util_mxJPO.basicBolistSel();
        selList.add(SELECT_ID);
        try{
            Map params = JPO.unpackArgs(args);
            String strObjectId = (String) params.get("objectId");
            DomainObject projectObject = DomainObject.newInstance(context, strObjectId);
            String type = projectObject.getInfo(context, SELECT_TYPE);
            String name = projectObject.getInfo(context, SELECT_NAME);
            if (TYPE_PROJECT_SPACE.equalsIgnoreCase(type) || JF_PLMConstants_mxJPO.TYPE_JF_ESOTask.equalsIgnoreCase(type) ||
                    (TYPE_TASK.equalsIgnoreCase(type) && name.contains("ESO"))) {
                String user = context.getUser();
                String owner = projectObject.getInfo(context, SELECT_OWNER);
                if (owner.equalsIgnoreCase(user)) {
                    flag = Boolean.TRUE;
                } else {
                    //判断是否有JFESOAdmin权限
                    Vector assignments = PersonUtil.getAssignments(context, user);
                    if (assignments.contains("JfESOAdmin")) {
                        flag = Boolean.TRUE;
                    } else {
                        flag = Boolean.FALSE;
                    }
                }
            } else {
                flag = Boolean.FALSE;
            }
        } catch (Exception e) {
            e.printStackTrace();
            flag = Boolean.FALSE;
        }
        return flag;
    }


    /**
     * 获取用户对象的全名
     * @param context
     * @param personId
     * @author LIUJR
     * @throws
     * @return java.lang.String
     * @date 2024/7/9 17:10
     * @description
     */
    public static String getPersonAllName(Context context, String personId) throws Exception{
        DomainObject objectPerson = DomainObject.newInstance(context, personId);
        StringList strAttrSelectList = new StringList();
        strAttrSelectList.add(String.format(STRING_MQL_ATTRIBUTE, DomainConstants.ATTRIBUTE_FIRST_NAME));
        strAttrSelectList.add(String.format(STRING_MQL_ATTRIBUTE, DomainConstants.ATTRIBUTE_LAST_NAME));
        Map AttrSelectMap = objectPerson.getInfo(context, strAttrSelectList);
        StringBuilder allName = new StringBuilder();
        allName.append(AttrSelectMap.get(String.format(STRING_MQL_ATTRIBUTE, DomainConstants.ATTRIBUTE_FIRST_NAME)));
        allName.append(STRING_SYMB_SPACE);
        allName.append(AttrSelectMap.get(String.format(STRING_MQL_ATTRIBUTE, DomainConstants.ATTRIBUTE_LAST_NAME)));
        return allName.toString();
    }

    /*
     * @description:获取物理产品的下一个Name
     * @author: caipan
     * @date: 2025/2/26 09:52:50
     * @param: * @param[1] context
     * @param[2] args
     * @return:
     **/
    public String getName(Context context,String[] args) throws Exception{
        String type = "VPMReference";//type
        String pak = "PRODUCTCFG";//package
        String var7 = ServiceUtils.generateUniqueID(context, pak, type);
        String var8 = ServiceUtils.generateExternalID(context, type, var7);
        System.out.println("var8 = " + var8);
        return var8;
    }

    /**
     * 详情页面修改权限
     * @param context
     * @param args
     * @author LIUJR
     * @throws
     * @return java.lang.Boolean
     * @date 2024/7/10 10:06
     * @description
     */
    public Boolean editAccessDetailsObject(Context context, String[] args) throws Exception {
        Boolean flag = true;
        try {
            Map map = (Map) JPO.unpackArgs(args);
            Map requestMap = (Map) map.get(STRING_REQUESTMAP);
            String objectId = (String) requestMap.get(STRING_OBJECTID);
            if (UIUtil.isNullOrEmpty(objectId)) {
                flag = true;
            } else {
                flag = false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
        return flag;
    }

    /**
     * 获取两个bo是否已经关联关系
     * @description
     * @author LiuJR
     * @param context
     * @param args
     * @return java.lang.Boolean
     * @throws
     * @date 2024/5/24 16:52
     */
    public static Boolean getTwoBusinessObject(Context context, String[] args) throws Exception{
        try {
            Map paramsMap = (Map) JPO.unpackArgs(args);
            String relName = (String) paramsMap.get("relName");
            String fromId = (String) paramsMap.get("fromId");
            String toId = (String) paramsMap.get("toId");
            DomainObject domainObject = DomainObject.newInstance(context, fromId);
            MapList relatedObjects = domainObject.getRelatedObjects(
                    context,
                    relName,
                    "*",
                    strBusSelectsList,
                    DomainConstants.EMPTY_STRINGLIST,
                    true,
                    true,
                    (short) 1,
                    "",
                    "",
                    0
            );
            if (relatedObjects.toString().contains(toId)) {
                return Boolean.TRUE;
            } else {
                return Boolean.FALSE;
            }
        }catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * 获取两个bo是否已经关联关系 返回关联关系
     * @description
     * @author LiuJR
     * @param context
     * @param args
     * @return java.lang.Boolean
     * @throws
     * @date 2024/5/24 16:52
     */
    public static String getTwoBusinessObjectConnId(Context context, String[] args) throws Exception{
        try {
            String connId = DomainObject.EMPTY_STRING;
            Map paramsMap = (Map) JPO.unpackArgs(args);
            String relName = (String) paramsMap.get("relName");
            String fromId = (String) paramsMap.get("fromId");
            String toId = (String) paramsMap.get("toId");
            DomainObject domainObject = DomainObject.newInstance(context, fromId);
            MapList relatedObjects = domainObject.getRelatedObjects(
                    context,
                    relName,
                    "*",
                    strBusSelectsList,
                    new StringList(DomainRelationship.SELECT_ID),
                    true,
                    true,
                    (short) 1,
                    "",
                    "",
                    0
            );
            Iterator iterator = relatedObjects.iterator();
            while (iterator.hasNext()) {
                Map map = (Map) iterator.next();
                String oid = (String) map.get(DomainConstants.SELECT_ID);
                if (oid.equalsIgnoreCase(toId)) {
                    connId = (String) map.get(DomainRelationship.SELECT_ID);
                    break;
                }
            }
            return connId;
        }catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
    * 审批任务界面中的内容  OOTB方法改写
    * @param context
	* @param args
    * @author LIUJR
    * @throws
    * @return java.lang.Object
    * @date 2024/7/11 18:31
    * @description
    */
    @ProgramCallable
    public Object getTaskContent(Context context,String[] args) throws Exception
    {
        HashMap programMap         = (HashMap) JPO.unpackArgs(args);
        String objectId    = (String) programMap.get(STRING_OBJECTID);
        String selectStr = "from["+DomainConstants.RELATIONSHIP_ROUTE_TASK+"].to.id";
        DomainObject taskObject = DomainObject.newInstance(context,objectId);
        objectId = taskObject.getInfo(context,selectStr);
        if (objectId == null) {
            DomainObject dmoLastRevision = new DomainObject(taskObject.getLastRevision(context));
            objectId = dmoLastRevision.getInfo(context, selectStr);
        }
        Route routeObject = (Route)DomainObject.newInstance(context,DomainConstants.TYPE_ROUTE);
        routeObject.setId(objectId);
        strBusSelectsList.add(routeObject.SELECT_TYPE);
        strBusSelectsList.add(routeObject.SELECT_DESCRIPTION);
        strBusSelectsList.add(routeObject.SELECT_POLICY);
        strBusSelectsList.add(routeObject.SELECT_CURRENT);
        strBusSelectsList.add(routeObject.SELECT_FILE_NAME);
        strBusSelectsList.add(routeObject.SELECT_FILE_FORMAT);
        strBusSelectsList.add(String.format(STRING_MQL_ATTRIBUTE, ATTRIBUTE_JF_TOPIC));
        strBusSelectsList.add(DomainConstants.SELECT_ATTRIBUTE_TITLE);
        StringList selListRel = new SelectList(3);
        selListRel.add(routeObject.SELECT_RELATIONSHIP_ID);
        selListRel.add(routeObject.SELECT_ROUTE_BASEPOLICY);
        selListRel.add(routeObject.SELECT_ROUTE_BASESTATE);
        MapList routableObjsList = routeObject.getRelatedObjects(
                context,
                DomainRelationship.RELATIONSHIP_OBJECT_ROUTE,
                DomainConstants.QUERY_WILDCARD,
                strBusSelectsList,
                selListRel,
                true,
                false,
                (short) 1,
                "",
                "",
                0
        );
        routableObjsList = (MapList) routableObjsList.stream().map(m -> {
            Map map = (Map) m;
            String strType = (String)map.get(routeObject.SELECT_TYPE);
            if (TYPE_JF_DATASOURCE.equalsIgnoreCase(strType)) {
                map.put(DomainConstants.SELECT_NAME, (String)map.get(DomainConstants.SELECT_NAME));
            } else if (DomainConstants.TYPE_DOCUMENT.equalsIgnoreCase(strType)) {
                map.put(DomainConstants.SELECT_NAME, (String)map.get(DomainConstants.SELECT_ATTRIBUTE_TITLE));
            }
            return map;
        }).collect(Collectors.toCollection(MapList::new));
        return routableObjsList;
    }

    /**
     * 获取密钥对
     * @param context
     * @param args
     * @author LIUJR
     * @throws
     * @return java.util.Map
     * @date 2024/7/11 18:18
     * @description
     */
    public static Map getGenerateKeyPairs(Context context, String[] args) throws Exception {
        try {
            Map params = (Map) JPO.unpackArgs(args);
            Integer codeLen = (Integer) params.get("codeLen");
            Integer secretLen = (Integer) params.get("secretLen");
            // 生成提取码
            String extractionSecret = generateExtractionCode(secretLen);
            String extractionCode = generateExtractionCode(codeLen);
            HashMap<String, String> map = new HashMap<>();
            map.put("code", extractionCode);
            map.put("secret", extractionSecret);
            return map;
        }catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
    * 生成密钥
    * @param length
    * @author LIUJR
    * @throws
    * @return java.lang.String
    * @date 2024/7/11 18:33
    * @description
    */
    public static String generateExtractionCode(int length) {
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            int randomIndex = random.nextInt(CHARACTERS.length());
            sb.append(CHARACTERS.charAt(randomIndex));
        }
        return sb.toString();
    }

    /**
    * 查询系统中的对象id
    * @param context
	* @param type
	* @param where
    * @author LIUJR
    * @throws
    * @return java.lang.String
    * @date 2024/7/12 13:26
    * @description
    */
    public  static String  findObject(Context context, String type,String where) throws Exception {
        String objectId = DomainConstants.EMPTY_STRING;
        StringList busSel = new StringList();
        busSel.add(DomainConstants.SELECT_ID);
        busSel.add(DomainConstants.SELECT_NAME);
        busSel.add(DomainConstants.SELECT_REVISION);
        MapList partnerMapList = DomainObject.findObjects(context,type,DomainConstants.QUERY_WILDCARD,where,busSel);
        if(!partnerMapList.isEmpty()){
            Map partnerMap = (Map)partnerMapList.get(0);
            objectId = (String)partnerMap.get(DomainConstants.SELECT_ID);
        }
        return objectId;
    }


    /**
    * 拿取html模板文件  读取整个page文件
    * @param context
	* @param pageName
	* @param language
    * @author LIUJR
    * @throws
    * @return java.lang.String
    * @date 2024/7/16 9:55
    * @description
    */
    public static String getPageHTMLResourceFile(Context context,String pageName,String language) throws Exception{
        String pageNamebak = pageName;
        Reader reader = null;

        if(language.contains("zh")){
            pageName=new StringBuffer(pageName).append("_zh.html").toString();
        }else{
            pageName=new StringBuffer(pageName).append(".html").toString();
        }
        JF_LOGGER.info("pageName:"+ pageName);
        Page pageAttributePopulation = new Page(pageName);
        pageAttributePopulation.open(context);
        String strProperties = pageAttributePopulation.getContents(context);
        pageAttributePopulation.close(context);
        InputStream input = new ByteArrayInputStream(strProperties.getBytes("UTF-8"));
        reader = new InputStreamReader(input, "UTF-8");
        StringBuilder sb = new StringBuilder();
        int bufferSize = 1024;
        char[] buffer = new char[bufferSize];
        int length = 0;
        while ((length = reader.read(buffer, 0, bufferSize)) != -1) {
            sb.append(buffer, 0, length);
        }
        return sb.toString();
    }

    /**
     *
     *@description 拼接变长字符串
     *@param strChars 拼接字符串
     *@return java.lang.String 返回拼接后字符串
     *@throws
     *@author CHENYAN
     *@date 2024/7/17 16:49
     */
    public static String buildStringInStrings(String... strChars){
        StringBuffer sbBuild = new StringBuffer();
        for(String str :strChars){
            sbBuild.append(str);
        }
        return sbBuild.toString();
    }

    /**
    * check out文件
    * @param context
	* @param args
     *  DirPath checkout 路径
     *  objectId 文档id
     *  fileName 文件名称
     *  fileType 文件类型
    * @author LIUJR
    * @throws
    * @return java.lang.String
    * @date 2024/7/18 10:38
    * @description
    */
    public static void checkOutFiles(Context context, String[] args) throws Exception{
        try{
            Map params = (Map) JPO.unpackArgs(args);
            //checkout 路径
            String strDirPath = (String) params.get("DirPath");
            //文档id
            String strObjectId = (String) params.get("objectId");
            //文件名称  具体名称(逗号分隔) 或者 *
            String strFileName = (String) params.get("fileName");
            //文件类型  .pdf .doc .docx .xls .xlsx 以逗号分隔  此处的筛选是在文件名称为* 的情况下
            String strFileType = (String) params.get("fileType");
            CommonDocument commonDocument = new CommonDocument();
            commonDocument.setId(strObjectId);
            FileList fileList = new FileList();
            if (!DomainConstants.QUERY_WILDCARD.equals(strFileName)) {
                //有具体的文件名称  拿取文件
                String[] splitFileName = strFileName.split(",");
                fileList = (FileList) Arrays.stream(splitFileName).map(fileName -> {
                    return  new File(fileName, "generic");
                }).collect(Collectors.toCollection(FileList::new));
            } else {
                FileList files = commonDocument.getFiles(context);
                if (UIUtil.isNullOrEmpty(strFileType)) {
                    //类型为空 下载所有的文档
                    fileList = files;
                } else {
                    String[] splitFileType = strFileType.split(",");
                    fileList = files.stream()
                            // 过滤出以数组中的后缀结尾的文件名
                            .filter(fileType -> Arrays.stream(splitFileType).anyMatch(fileType.getName() ::endsWith))
                            .collect(Collectors.toCollection(FileList::new));
                }
            }
            //下载文件
            if (!fileList.isEmpty()) {
                commonDocument.checkoutFiles(context, false, "generic", fileList, strDirPath);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * 数据申请单中的选择审批人的渲染框
     * @description
     * @author LiuJR
     * @param context
     * @param args
     * @return java.lang.String
     * @throws
     * @date 2024/5/9 11:20
     */
    @ProgramCallable
    public String selectAddRemovePerson(Context context,String[] args) throws Exception {
        StringBuilder sb = new StringBuilder();
        //添加移除文字提示
        String addContributor= "\u6dfb\u52a0\u90e8\u95e8\u7ecf\u7406\u5ba1\u6279\u4eba";
        String remove = "\u79fb\u9664";
        String contributors = DomainObject.EMPTY_STRING;
        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        HashMap requestMap = (HashMap) programMap.get("requestMap");
        String strMode = (String) requestMap.get("mode");
        String dataOutsourceId = null;
        if (UIUtil.isNotNullAndNotEmpty(strMode)) {
            if ("edit".equalsIgnoreCase(strMode)) {
                dataOutsourceId = (String) requestMap.get("objectId");
                DomainObject objectDataOutsource = DomainObject.newInstance(context, dataOutsourceId);
                String cmtModulHead = objectDataOutsource.getAttributeValue(context, "JSApprovePerson");
                String[] split = cmtModulHead.split(",");
                for (int i = 0; i < split.length; i++) {
                    DomainObject personObject = PersonUtil.getPersonObject(context, split[i]);
                    if (contributors.length() == 0) {
                        contributors += personObject.getId(context);
                    } else {
                        contributors += "," + personObject.getId(context);
                    }
                }
            } else if ("view".equalsIgnoreCase(strMode)) {
                dataOutsourceId = (String) requestMap.get("objectId");
                DomainObject objectDataOutsource = DomainObject.newInstance(context, dataOutsourceId);
                String strJSApprovePerson = objectDataOutsource.getAttributeValue(context, "JSApprovePerson");
                return strJSApprovePerson;
            }
        }
        //XSSOK
        sb.append("<input type=\"hidden\" name=\"IsContributorFieldModified\" id=\"IsContributorFieldModified\" value=\"false\" readonly=\"readonly\" />");
        //XSSOK
        sb.append("<input type=\"hidden\" name=\"JSApprovePerson\" id=\"JSApprovePerson\" value=\""+contributors+"\" readonly=\"readonly\" />");

        sb.append("<table>");
        sb.append("<tr>");
        sb.append("<th rowspan=\"2\">");
        sb.append("<select name=\"Contributor\" style=\"width:200px\" multiple=\"multiple\">");
        if (UIUtil.isNotNullAndNotEmpty(dataOutsourceId)){
            DomainObject objectCMTHead = DomainObject.newInstance(context, dataOutsourceId);
            String cmtModulHead = objectCMTHead.getAttributeValue(context, "IdmCmtModulHead");
            String[] split = cmtModulHead.split("\\|");
            for (int i = 0; i < split.length; i++) {
                String id = PersonUtil.getPersonObject(context, split[i]).getId(context);
                sb.append("<option value=\"" + id + "\" >");
                //XSSOK
                sb.append(split[i]);
                sb.append("</option>");
            }
        }
        sb.append("</select>");
        sb.append("</th>");
        sb.append("<td>");
        sb.append("<a href=\"javascript:addJSApprovePersonContributor()\">");
        sb.append("<img src=\"../common/images/iconStatusAdded.gif\" width=\"12\" height=\"12\" border=\"0\" />");
        sb.append("</a>");
        sb.append("<a href=\"javascript:addJSApprovePersonContributor()\">");
        //XSSOK
        sb.append(addContributor);
        sb.append("</a>");
        //sb.append("</div>");
        sb.append("</td>");
        sb.append("</tr>");
        sb.append("<tr>");
        sb.append("<td>");
        sb.append("<a href=\"javascript:removeJSApprovePerson()\">");
        sb.append("<img src=\"../common/images/iconStatusRemoved.gif\" width=\"12\" height=\"12\" border=\"0\" />");
        sb.append("</a>");
        sb.append("<a href=\"javascript:removeJSApprovePerson()\">");
        //XSSOK
        sb.append(remove);
        sb.append("</a>");
        sb.append("</td>");
        sb.append("</tr>");
        sb.append("</table>");
        return sb.toString();
    }


    /**
    * 研发技术文档 添加搜索显示人员
    * @param context
	* @param mode
	* @param viewList
	* @param editIdList
	* @param FieldName
	* @param addJS
	* @param removeJS
    * @author LIUJR
    * @throws
    * @return java.lang.String
    * @date 2025/11/10 17:09
    * @description
    */
    @ProgramCallable
    public static  String selectAddRemovePersonPublic(Context context,String mode, StringList viewList, StringList editIdList, String FieldName, String addJS, String removeJS) throws Exception {
        StringBuilder sb = new StringBuilder();
        //添加移除文字提示
//        String addContributor= "\u6dfb\u52a0\u90e8\u95e8\u7ecf\u7406\u5ba1\u6279\u4eba";
//        String remove = "\u79fb\u9664";
        //添加移除文字提示
        String strAddButton = EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", context.getLocale(), "emxComponents.Button.AddWCharDevEngineering");
        String strRemoveButton = EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", context.getLocale(), "emxComponents.Button.RemoveWCharDevEngineering");
        String contributors = DomainObject.EMPTY_STRING;
        if (UIUtil.isNotNullAndNotEmpty(mode)) {
            if ("view".equalsIgnoreCase(mode)) {
                return viewList.join("\n");
            }
        }
        //XSSOK
        sb.append("<input type=\"hidden\" name=\"IsContributorFieldModified\" id=\"IsContributorFieldModified\" value=\"false\" readonly=\"readonly\" />");
        //XSSOK
        sb.append("<input type=\"hidden\" name=\""+ FieldName +"\" id=\""+ FieldName +"\" value=\""+editIdList.join(",")+"\" readonly=\"readonly\" />");

        sb.append("<table>");
        sb.append("<tr>");
        sb.append("<th rowspan=\"2\">");
        sb.append("<select name=\""+ FieldName +"Contributor\" style=\"width:200px\" multiple=\"multiple\">");
        if (!editIdList.isEmpty()){
            DomainObject domainObject = DomainObject.newInstance(context);
            String strFullName = EMPTY_STRING;
            for (int i = 0; i < editIdList.size(); i++) {
                String id = editIdList.get(i);
                sb.append("<option value=\"" + id + "\" >");
                //XSSOK
                domainObject.setId(id);
                if (TYPE_PERSON.equalsIgnoreCase(domainObject.getTypeName(context))) {
                    strFullName = JF_PublicMethodClass_mxJPO.getPersonAllName(context,id) ;
                } else {
                    strFullName = domainObject.getInfo(context, SELECT_NAME);
                }
                strFullName = StringEscapeUtils.escapeHtml4(strFullName);
                sb.append(strFullName);
                sb.append("</option>");
            }
        }
        sb.append("</select>");
        sb.append("</th>");
        sb.append("<td>");
        sb.append("<a href=\"javascript:" + addJS + "\">");  //addJSApprovePersonContributor()
        sb.append("<img src=\"../common/images/iconStatusAdded.gif\" width=\"12\" height=\"12\" border=\"0\" />");
        sb.append("</a>");
        sb.append("<a href=\"javascript:" + addJS + "\">");
        //XSSOK
        sb.append(strAddButton);
        sb.append("</a>");
        //sb.append("</div>");
        sb.append("</td>");
        sb.append("</tr>");
        sb.append("<tr>");
        sb.append("<td>");
        sb.append("<a href=\"javascript:" + removeJS + "\">"); //removeJSApprovePerson
        sb.append("<img src=\"../common/images/iconStatusRemoved.gif\" width=\"12\" height=\"12\" border=\"0\" />");
        sb.append("</a>");
        sb.append("<a href=\"javascript:" + removeJS + "\">");
        //XSSOK
        sb.append(strRemoveButton);
        sb.append("</a>");
        sb.append("</td>");
        sb.append("</tr>");
        sb.append("</table>");
        return sb.toString();
    }


    /**
    * 获取人员的直线经理Id   strPersonId 与 strPersonName 任意传递一个即可
    * @param context
	* @param strPersonId  人员id
	* @param strPersonName 人员账号名称  如： object.getOwner().getName();
    * @author LIUJR
    * @throws
    * @return java.lang.String
    * @date 2024/7/19 9:12
    * @description 注意： 该方法获取人员的直线经理id strPersonId 与 strPersonName参数,根据需求,任意传一个参数就好
     *                  返回的直线经理Id, 需要判断为否为空,再进行下一步操作。如：if(UIUtil.isNotNullAndNotEmpty(strLineManagerId)){...}
    */
    public static String getPersonLineManager(Context context, String strPersonId, String strPersonName){
        try {

            String strLineManagerId = DomainConstants.EMPTY_STRING;
            DomainObject personObject = DomainObject.newInstance(context);
            if (UIUtil.isNotNullAndNotEmpty(strPersonName)) {
                String strPersonObjectId = PersonUtil.getPersonObjectID(context, strPersonName);
                personObject.setId(strPersonObjectId);
            } else if (UIUtil.isNotNullAndNotEmpty(strPersonId)) {
                personObject.setId(strPersonId);
            }
            String strLineManager = personObject.getAttributeValue(context, ATTRIBUTE_JF_LINE_MANAGER);
            if (UIUtil.isNotNullAndNotEmpty(strLineManager)) {
                // add by chenyan 2025/04/11 新增判断账号是否存在不存在的话直接返回空
                strLineManagerId = PersonUtil.getPersonObjectID(context, strLineManager);
            }
            return strLineManagerId;
        } catch (FrameworkException e) {
            e.printStackTrace();
            return DomainConstants.EMPTY_STRING ;
        }
    }

    /**
    * 构造流程审批节点信息
    * @param objectId 人员id
	* @param title 审批任务标题
	* @param allowDelegation 是否让委托 true / false
	* @param seq 审批顺序
	* @param anyOrAll 任一 还是全部  Any /  All
    * @author LIUJR
    * @throws
    * @return java.util.Map
    * @date 2024/7/19 14:00
    * @description
    */
    public static Map getMapAnyOrAll(String objectId, String title, String allowDelegation,String seq, String anyOrAll) {
        if (UIUtil.isNotNullAndNotEmpty(objectId)) {
            Map map = new HashMap();
            map.put("id", objectId);
            map.put("Route Instructions", "Instructions");
            map.put("Route Action", "Approve");
            map.put("Title", title);//任务标题
            map.put("Allow Delegation", allowDelegation);//是否让委托
            map.put("Route Sequence", seq);//任务顺序
            map.put("Parallel Node Procession Rule", anyOrAll);
            return map;
        }
        return null;
    }

    /**
     * 构造流程审批节点信息  仅通知
     * @param objectId 人员id
     * @param title 审批任务标题
     * @param allowDelegation 是否让委托 true / false
     * @param seq 审批顺序
     * @param anyOrAll 任一 还是全部  Any /  All
     * @author LIUJR
     * @throws
     * @return java.util.Map
     * @date 2024/7/19 14:00
     * @description
     */
    public static Map getMapAnyOrAllNotifyOnly(String objectId, String title, String allowDelegation,String seq, String anyOrAll) {
        if (UIUtil.isNotNullAndNotEmpty(objectId)) {
            Map map = new HashMap();
            map.put("id", objectId);
            map.put("Route Instructions", "Instructions");
            map.put("Route Action", "Notify Only");
            map.put("Title", title);//任务标题
            map.put("Allow Delegation", allowDelegation);//是否让委托
            map.put("Route Sequence", seq);//任务顺序
            map.put("Parallel Node Procession Rule", anyOrAll);
            return map;
        }
        return null;
    }

    /**
    *
    *@description 创建审核任务
    *@param strPersonId 人员id
	*@param title 审批任务标题
	*@param allowDelegation 是否让委托 true / false
	*@param seq 审批顺序
	*@param anyOrAll 任一 还是全部  Any /  All
	*@param strRouteAction 审批任务还是评论任务
    *@return java.util.Map
    *@throws
    *@author CHENYAN
    *@date 2025/4/9 15:14
    */
    public static Map getInboxTaskMap(String strPersonId, String title, String allowDelegation,String seq, String anyOrAll,String strRouteAction,String strRouteInstructions) {
        if (UIUtil.isNotNullAndNotEmpty(strPersonId)) {
            Map map = new HashMap();
            map.put("id", strPersonId);
            if (UIUtil.isNullOrEmpty(strRouteInstructions)){
                strRouteInstructions = "Instructions";
            }
            map.put("Route Instructions", strRouteInstructions);
            map.put("Route Action", strRouteAction);
            map.put("Title", title);//任务标题
            map.put("Allow Delegation", allowDelegation);//是否让委托
            map.put("Route Sequence", seq);//任务顺序
            map.put("Parallel Node Procession Rule", anyOrAll);
            return map;
        }
        return null;
    }

    /**
    * 判断一个文件路径是否存在 不存在就创建
    * @param context
	* @param filePath
    * @author LIUJR
    * @throws
    * @return java.lang.Boolean
    * @date 2024/7/22 17:27
    * @description
    */
    public static Boolean createDirFilePath(Context context, String filePath) {
        Boolean returnFlag = Boolean.TRUE;
        // 获取文件的父目录路径
        Path parentPath = Paths.get(filePath);
        // 检查目录是否存在，如果不存在则尝试创建
        if (parentPath != null && !Files.exists(parentPath)) {
            try {
                Files.createDirectories(parentPath);
                JF_LOGGER.info("目录已成功创建: " + parentPath);
            } catch (IOException e) {
                returnFlag = Boolean.FALSE;
            }
        } else {
            JF_LOGGER.info("目录已存在，无需创建。");
        }
        return returnFlag;
    }

    /**
    * 下载数模图纸文件
    * @param context
	* @param vpmReferenceV5DownList
    * @author LIUJR
    * @throws
    * @return java.lang.String
    * @date 2024/7/23 13:32
    * @description
    */
    public static MapList downloadDigifaxModel(Context context, String type, MapList vpmReferenceV5DownList, String fileDownloadV5FilePath, StringList repeatIdList) throws Exception{
        Iterator iterator = vpmReferenceV5DownList.iterator();
        MapList resultMapList = new MapList();
        DomainObject domainObject = DomainObject.newInstance(context);
        while (iterator.hasNext()) {
            Map map = (Map)iterator.next();
            String strModFileName = (String) map.get("strModFileName");
            String strSourceFileName = (String) map.get("strSourceFileName");
            String strCadId = (String) map.get("strCadId");
            String strOId = UIUtil.getValue(map, "oid");
            if (UIUtil.isNullOrEmpty(strCadId)) {
                strCadId = strOId;
            }
            //new  ljr
            String path = fileDownloadV5FilePath;
            if ("Part".equalsIgnoreCase(type)) {
                domainObject.setId(strOId);
                String physicalid = domainObject.getInfo(context, "physicalid");
                if (repeatIdList.contains(physicalid)) {
                    path += physicalid  + java.io.File.separator;
                    //创建目录
                    JF_PublicMethodClass_mxJPO.createDirFilePath(context, path);
                }
            }
            String mqlCmd = "checkout bus " + strCadId + " server format 1 directory " + path;
            MqlUtil.mqlCommand(context, false, mqlCmd, true);
            String modFileDir = path + strModFileName;
            java.io.File oldFile = new java.io.File(path  + strSourceFileName);
            //end
//            String mqlCmd = "checkout bus " + strCadId + " server format 1 directory " + fileDownloadV5FilePath;
//            MqlUtil.mqlCommand(context, false, mqlCmd, true);
//            String modFileDir = fileDownloadV5FilePath + java.io.File.separator + strModFileName;
//            java.io.File oldFile = new java.io.File(fileDownloadV5FilePath + java.io.File.separator + strSourceFileName);

            java.io.File newFile = new java.io.File(modFileDir);
            if (oldFile.exists() && oldFile.isFile()) {
                oldFile.renameTo(newFile);
            }
            Map<String, String> resultMap = new HashMap<>();
            resultMap.put("oid", strCadId);
            resultMap.put("filePath", modFileDir);
            resultMapList.add(resultMap);
        }
        return resultMapList;
    }

    /**
    * 下载物理产品略缩图
    * @param context
	* @param args
    * @author LIUJR
    * @throws
    * @return com.matrixone.apps.domain.util.MapList
    * @date 2024/12/5 13:56
    * @description
    */
    public static Map<String, String> downloadVPMReferenceImage(Context context, String[] args) throws Exception {
        String path = DomainConstants.EMPTY_STRING;
        Map<String, String> map = new HashMap<>();
        try{
            String basicUrl = getBasicUrl(context, new String[]{"DownloadVPM.Img.Path"});


            for(String vpmId : args) {
                String objectId = vpmId;
                DomainObject domainObject = DomainObject.newInstance(context, objectId);
                String physicalid = domainObject.getInfo(context, "physicalid");
                String strImgPath = JF_PublicMethodClass_mxJPO.buildStringInStrings(basicUrl, java.io.File.separator,physicalid, "_''.jpeg");
                //获取文件夹对象
                java.io.File imgFile = new java.io.File(strImgPath);
                //如果缩略图存在
                if (imgFile.exists()){
                    map.put(objectId, strImgPath);
                    continue;
                }
                String imageId = JF_PublicMethodClass_mxJPO.findObject(context, "PLMDerivedObjRepresentation", "name=='" + physicalid + "'");
                if (UIUtil.isNotNullAndNotEmpty(imageId)) {
                    DomainObject imageObject = DomainObject.newInstance(context, imageId);
                    String formatName = imageObject.getInfo(context, "format[6].file.name");
                    JF_LOGGER.info("formatName:{}", formatName);
                    String mql = "checkout bus PLMDerivedObjRepresentation " + physicalid + " '' format 6 " + basicUrl + ";";
//                    String mql = "checkout bus  " + physicalid + "  format 6 " + basicUrl + ";";
                    JF_LOGGER.info("mql:{}",mql);
                    MqlUtil.mqlCommand(context, false, mql, true);
                    String newFileDir = basicUrl + java.io.File.separator + formatName.replaceAll("\\.6", ".jpeg");
                    JF_LOGGER.info("newFileDir:{}", newFileDir);
                    String oldFileDir = basicUrl + java.io.File.separator + formatName;
                    JF_LOGGER.info("oldFileDir:{}", oldFileDir);
                    java.io.File oldFile = new java.io.File(oldFileDir);
                    java.io.File newFile = new java.io.File(newFileDir);
                    if (oldFile.exists() && oldFile.isFile()) {
                        oldFile.renameTo(newFile);
                    }
                    path = newFileDir;
                } else {
                    path = DomainConstants.EMPTY_STRING;
                }
                map.put(objectId, path);
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
        JF_LOGGER.info("map:{}", map);
        return  map;
    }

    /**
    * 拿取文档对象的所有的最后版本的文件集合
    * @param context
	* @param docId
    * @author LIUJR
    * @throws
    * @return com.matrixone.apps.domain.util.MapList
    * @date 2024/7/24 14:02
    * @description
    */
    public static MapList getDocumentFiles(Context context, String docId) throws Exception {
        CommonDocument commonDocument = new CommonDocument();
        commonDocument.setId(docId);
        //拿取文档对象下的文件对象
        MapList versionList = commonDocument.getRelatedObjects(context,
                CommonDocument.RELATIONSHIP_LATEST_VERSION,
                CommonDocument.TYPE_DOCUMENTS,
                versionSelectList,
                null,
                false,
                true,
                (short) 1,
                null,
                null,
                null,
                null,
                null
        );
        return versionList;
    }

  /**
  *
  *@description 构造链接Href 按钮
  *@param context
  *@param strObjectId  跳转对象Id
  *@param strObjectName 跳转对象显示名称
  *@param strMode 跳转模式  为空为 content
  *@return java.lang.String
  *@throws
  *@author CHENYAN
  *@date 2024/7/30 16:18
  */
    public static String buildLinkHrefHtml(Context context ,String strObjectId ,String strObjectName,String strMode){
        StringBuffer sbBuild = new StringBuffer();
        sbBuild.append( "<a class=\"object\" href=\"JavaScript:emxTableColumnLinkClick('../common/emxTree.jsp?objectId=");
        sbBuild.append(XSSUtil.encodeForHTMLAttribute(context, strObjectId));
        sbBuild.append( "', '800', '575','true','");
        sbBuild.append(UIUtil.isNullOrEmpty(strMode) ? "content":strMode);
        sbBuild.append("')\">");
        sbBuild.append(XSSUtil.encodeForHTML(context, strObjectName));
        sbBuild.append("</a>");
        return sbBuild.toString();
    }
    /**
    *
    *@description 构造form中 field的清除按钮
    *@param strFiledName  列名称
    *@param strBtnName  清除按钮名称
    *@return java.lang.String
    *@throws
    *@author CHENYAN
    *@date 2024/7/30 16:18
    */
    public static String buildClearButtonHtml(String strFiledName,String strBtnName){
        StringBuffer sbBuild = new StringBuffer();
        sbBuild.append("<a href=\"javascript:basicClear('");
        sbBuild.append(strFiledName);
        sbBuild.append("')\">");
        sbBuild.append(strBtnName);
        sbBuild.append("</a>");
        return sbBuild.toString();
    }

    /**
    *
    *@description 构造含有清除按钮和 选择弹窗按钮的Field 列
    *@param context
	*@param strMode form 的模式 view edit create
	*@param strFieldName form列名称
	*@param strObjectId form对象Id
	*@param strObjectName form对象名称
	*@param strSearchUrl 搜索URL
	*@param hasEditAccess 是否有编辑权限
    *@return java.lang.String
    *@throws
    *@author CHENYAN
    *@date 2024/7/30 16:23
    */
    public static String buildFieldHtml(Context context ,String strMode,String strFieldName ,String strObjectId ,String strObjectName ,String strSearchUrl,boolean hasEditAccess) throws Exception{
        String strLanguage = context.getSession().getLanguage();
        StringBuffer sb = new StringBuffer();
        if ("view".equals(strMode) || hasEditAccess == false){
            return buildLinkHrefHtml(context,strObjectId,strObjectName,"");
        }else if (("edit".equals(strMode) || "create".equals(strMode)) && hasEditAccess){
            String strNlsNative = "...";
            String strNlsClear = EnoviaResourceBundle.getProperty(context, "EnterpriseChangeMgt","EnterpriseChangeMgt.Command.Clear",strLanguage);
            sb.append("<table>");
            sb.append("<tbody>");
            sb.append("<tr>");
            sb.append("<td>");
            sb.append("<input type=\"hidden\" name=\"");
            sb.append(strFieldName);
            sb.append("fieldValue\" value=\"");
            sb.append(strObjectId);
            sb.append("\"/>");
            sb.append("<input type=\"hidden\" name=\"");
            sb.append(strFieldName);
            sb.append("\" value=\"");
            sb.append(StringEscapeUtils.escapeHtml4(strObjectName));
            sb.append("\"/>");
            sb.append("<input type=\"hidden\" name=\"");
            sb.append(strFieldName);
            sb.append("OID\" value=\"");
            sb.append(strObjectId);
            sb.append("\"/>");
            sb.append("<input type=\"text\" name=\"");
            sb.append(strFieldName);
            sb.append("Display\" value=\"");
            sb.append(StringEscapeUtils.escapeHtml4(strObjectName));
            sb.append("\" maxlength=\"\" size=\"20\" />");
            sb.append("</td>");
            sb.append("<td>");
            sb.append("<input type=\"button\" name=\"btn");
            sb.append(strFieldName);
            sb.append("\" value=\"");
            sb.append(XSSUtil.encodeForHTMLAttribute(context, strNlsNative));
            //判断是否包含回写参数
            //onclick="javascript:showChooser(
            // '../common/emxFullSearch.jsp?submitURL=./AEFSearchUtil.jsp&
            // field=TYPES=type_ProjectSpace:CURRENT=policy_ProjectSpace.state_Active
            // &cancelLabel=emxFramework.Common.Close
            // &selection=single&fieldNameActual=JFProjectName
            // &suiteKey=Framework&HelpMarker=emxhelpselectorganization
            // &fieldNameOID=JFProjectNameOID
            // &fieldNameActual=JFProjectName
            // &fieldNameDisplay=JFProjectNameDisplay'
            // &table=ENCAddExistingGeneralSearchResults&showInitialResults=true
            // ,'600','600','true','','JFProjectName')"
            if (!strSearchUrl.contains("fieldNameDisplay")){
                StringBuffer sbUrl = new StringBuffer(strSearchUrl);
                sbUrl.append("&fieldNameDisplay=");
                sbUrl.append(buildStringInStrings(strFieldName,"Display"));
                strSearchUrl = sbUrl.toString();
            }
            if (!strSearchUrl.contains("fieldNameOID")){
                StringBuffer sbUrl = new StringBuffer(strSearchUrl);
                sbUrl.append("&fieldNameOID=");
                sbUrl.append(buildStringInStrings(strFieldName,"OID"));
                strSearchUrl = sbUrl.toString();
            }
            if (!strSearchUrl.contains("fieldNameActual")){
                StringBuffer sbUrl = new StringBuffer(strSearchUrl);
                sbUrl.append("&fieldNameActual=");
                sbUrl.append(strFieldName);
                strSearchUrl = sbUrl.toString();
            }
            if (UIUtil.isNotNullAndNotEmpty(strSearchUrl)) {
                sb.append("\" onclick=\"javascript:showChooser('");
                sb.append(strSearchUrl);
                sb.append("','600','600','true','','");
            }else {
                sb.append("\"");
            }
            sb.append(strFieldName);
            sb.append("')\" />");
            sb.append("</td>");
            sb.append("<td>");
            sb.append(buildClearButtonHtml(strFieldName,XSSUtil.encodeForHTMLAttribute(context, strNlsClear)));
//            sb.append("<a href=\"javascript:basicClear('");
//            sb.append(strFieldName);
//            sb.append("')\">");
//            sb.append(XSSUtil.encodeForHTMLAttribute(context, strNlsClear));
//            sb.append("</a>");
            sb.append("</td>");
            sb.append("</tr>");
            sb.append("</tbody>");
            sb.append("</table>");
            return sb.toString();
        }
        return "";
    }


    /**
     *
     *@description 构造含有清除按钮和 选择弹窗按钮的Field 列
     *@param context
     *@param strMode form 的模式 view edit create
     *@param strFieldName form列名称
     *@param strObjectId form对象Id
     *@param strObjectName form对象名称
     *@param strSearchUrl 搜索URL
     *@param hasEditAccess 是否有编辑权限
     *@param clearButton 是否有清除按钮
     *@param readOnly 是否只读
     *@return java.lang.String
     *@throws
     *@author CHENYAN
     *@date 2024/7/30 16:23
     */
    public static String buildFieldAutoHtml(Context context ,String strMode,String strFieldName ,String strObjectId ,String strObjectName ,String strSearchUrl,boolean hasEditAccess, Boolean clearButton, Boolean readOnly) throws Exception{
        String strLanguage = context.getSession().getLanguage();
        StringBuffer sb = new StringBuffer();
        if ("view".equals(strMode) || hasEditAccess == false){
            return buildLinkHrefHtml(context,strObjectId,strObjectName,"");
        }else if (("edit".equals(strMode) || "create".equals(strMode)) && hasEditAccess){
            String strNlsNative = "...";
            String strNlsClear = EnoviaResourceBundle.getProperty(context, "EnterpriseChangeMgt","EnterpriseChangeMgt.Command.Clear",strLanguage);
            sb.append("<table>");
            sb.append("<tbody>");
            sb.append("<tr>");
            sb.append("<td>");
            sb.append("<input type=\"hidden\" name=\"");
            sb.append(StringEscapeUtils.escapeHtml4(strFieldName));
            sb.append("fieldValue\" value=\"");
            sb.append(StringEscapeUtils.escapeHtml4(strObjectId));
            sb.append("\"/>");
            sb.append("<input type=\"hidden\" name=\"");
            sb.append(StringEscapeUtils.escapeHtml4(strFieldName));
            sb.append("\" value=\"");
            sb.append(StringEscapeUtils.escapeHtml4(strObjectName));
            sb.append("\"/>");
            sb.append("<input type=\"hidden\" name=\"");
            sb.append(StringEscapeUtils.escapeHtml4(strFieldName));
            sb.append("OID\" value=\"");
            sb.append(StringEscapeUtils.escapeHtml4(strObjectId));
            sb.append("\"/>");
            if (readOnly) {
                sb.append("<label>");
                sb.append(StringEscapeUtils.escapeHtml4(strObjectName));
                sb.append("</label>");
            } else {
                sb.append("<input type=\"text\" name=\"");
                sb.append(StringEscapeUtils.escapeHtml4(strFieldName));
                sb.append("Display\" value=\"");
                sb.append(StringEscapeUtils.escapeHtml4(strObjectName));
                sb.append("\" maxlength=\"\" size=\"20\" />");
            }
            sb.append("</td>");
            if (UIUtil.isNotNullAndNotEmpty(strSearchUrl)) {
                sb.append("<td>");
                sb.append("<input type=\"button\" name=\"btn");
                sb.append(strFieldName);
                sb.append("\" value=\"");
                sb.append(XSSUtil.encodeForHTMLAttribute(context, strNlsNative));
                if (!strSearchUrl.contains("fieldNameDisplay")) {
                    StringBuffer sbUrl = new StringBuffer(strSearchUrl);
                    sbUrl.append("&fieldNameDisplay=");
                    sbUrl.append(buildStringInStrings(strFieldName, "Display"));
                    strSearchUrl = sbUrl.toString();
                }
                if (!strSearchUrl.contains("fieldNameOID")) {
                    StringBuffer sbUrl = new StringBuffer(strSearchUrl);
                    sbUrl.append("&fieldNameOID=");
                    sbUrl.append(buildStringInStrings(strFieldName, "OID"));
                    strSearchUrl = sbUrl.toString();
                }
                if (!strSearchUrl.contains("fieldNameActual")) {
                    StringBuffer sbUrl = new StringBuffer(strSearchUrl);
                    sbUrl.append("&fieldNameActual=");
                    sbUrl.append(strFieldName);
                    strSearchUrl = sbUrl.toString();
                }
                sb.append("\" onclick=\"javascript:showChooser('");
                sb.append(strSearchUrl);
                sb.append("','600','600','true','','");
                sb.append(strFieldName);
                sb.append("')\" />");
                sb.append("</td>");
            }
            if (clearButton) {
                sb.append("<td>");
                sb.append(buildClearButtonHtml(strFieldName, XSSUtil.encodeForHTMLAttribute(context, StringEscapeUtils.escapeHtml4(strNlsClear))));
//            sb.append("<a href=\"javascript:basicClear('");
//            sb.append(strFieldName);
//            sb.append("')\">");
//            sb.append(XSSUtil.encodeForHTMLAttribute(context, strNlsClear));
//            sb.append("</a>");
                sb.append("</td>");
            }
            sb.append("</tr>");
            sb.append("</tbody>");
            sb.append("</table>");
            return sb.toString();
        }
        return "";
    }



    /**
     *
     *@description 构造含有清除按钮和 选择弹窗按钮的Field 列   创建页面获取其他对象的值 给另外一个对象使用
     *@param context
     *@param strMode form 的模式 view edit create
     *@param strFieldName form列名称
     *@param strObjectId form对象Id
     *@param strObjectName form对象名称
     *@param strSearchUrl 搜索URL
     *@param hasEditAccess 是否有编辑权限
     *@return java.lang.String
     *@throws
     *@author CHENYAN
     *@date 2024/7/30 16:23
     */
    public static String buildFieldJSHtml(Context context ,String strMode,String strFieldName ,String strObjectId ,String strObjectName ,String strSearchUrl,boolean hasEditAccess, String js) throws Exception{
        String strLanguage = context.getSession().getLanguage();
        StringBuffer sb = new StringBuffer();
        if ("view".equals(strMode) || hasEditAccess == false){
            return buildLinkHrefHtml(context,strObjectId,strObjectName,"");
        }else if (("edit".equals(strMode) || "create".equals(strMode)) && hasEditAccess){
            String strNlsNative = "...";
            String strNlsClear = EnoviaResourceBundle.getProperty(context, "EnterpriseChangeMgt","EnterpriseChangeMgt.Command.Clear",strLanguage);
            sb.append("<table>");
            sb.append("<tbody>");
            sb.append("<tr>");
            sb.append("<td>");
            sb.append("<input type=\"hidden\" name=\"");
            sb.append(strFieldName);
            sb.append("fieldValue\" value=\"");
            sb.append(strObjectId);
            sb.append("\"/>");
            sb.append("<input type=\"hidden\" name=\"");
            sb.append(strFieldName);
            sb.append("\" value=\"");
            sb.append(StringEscapeUtils.escapeHtml4(strObjectName));
            sb.append("\"/>");
            sb.append("<input type=\"hidden\" name=\"");
            sb.append(strFieldName);
            sb.append("OID\" value=\"");
            sb.append(strObjectId);
            sb.append("\"/>");
            sb.append("<input type=\"text\" name=\"");
            sb.append(strFieldName);
            sb.append("Display\" value=\"");
            sb.append(StringEscapeUtils.escapeHtml4(strObjectName));
            sb.append("\" maxlength=\"\" size=\"20\" />");
            sb.append("</td>");
            sb.append("<td>");
            sb.append("<input type=\"button\" name=\"btn");
            sb.append(strFieldName);
            sb.append("\" value=\"");
            sb.append(XSSUtil.encodeForHTMLAttribute(context, strNlsNative));
            sb.append("\" onclick=\"javascript:"+ js + "(");

            if (!strSearchUrl.contains("fieldNameDisplay")){
                StringBuffer sbUrl = new StringBuffer(strSearchUrl);
                sbUrl.append("&fieldNameDisplay=");
                sbUrl.append(buildStringInStrings(strFieldName,"Display"));
                strSearchUrl = sbUrl.toString();
            }
            if (!strSearchUrl.contains("fieldNameOID")){
                StringBuffer sbUrl = new StringBuffer(strSearchUrl);
                sbUrl.append("&fieldNameOID=");
                sbUrl.append(buildStringInStrings(strFieldName,"OID"));
                strSearchUrl = sbUrl.toString();
            }
            if (!strSearchUrl.contains("fieldNameActual")){
                StringBuffer sbUrl = new StringBuffer(strSearchUrl);
                sbUrl.append("&fieldNameActual=");
                sbUrl.append(strFieldName);
                strSearchUrl = sbUrl.toString();
            }
//            sb.append(strSearchUrl);
//            sb.append("','600','600','true','','");
//            sb.append(strFieldName);
//            sb.append("')\" />");
            sb.append(")\" />");
            sb.append("</td>");
            sb.append("<td>");
            sb.append(buildClearButtonHtml(strFieldName,XSSUtil.encodeForHTMLAttribute(context, strNlsClear)));
            sb.append("</td>");
            sb.append("</tr>");
            sb.append("</tbody>");
            sb.append("</table>");
            return sb.toString();
        }
        return "";
    }


    /**
    *
    *@description 获取ECR 中登录人的对应的会签任务角色
    *@param context
	*@param args
    *@return matrix.util.MapList   key role(项目角色) current（任务状态） signTaskId（会签任务id）
    *@throws
    *@author CHENYAN
    *@date 2024/8/1 14:16
    */
    public static MapList getLoginUserSignTaskRoleByECR(Context context ,String[] args) throws Exception{
        String strObjectId = args[0];
        return getLoginUserSignTaskRoleByECRAndUser(context,new String[]{strObjectId,context.getUser()});
    }
    /**
    *
    *@description 获取指定用户名的ECR对应的会签任务角色
    *@param context
	*@param args
    *@return matrix.util.MapList   key role(项目角色) current（任务状态） signTaskId（会签任务id）
    *@throws
    *@author CHENYAN
    *@date 2024/8/1 14:20
    */
    public static MapList getLoginUserSignTaskRoleByECRAndUser(Context context ,String[] args) throws Exception{
        String strObjectId = args[0];
        String strLoginUser = args[1];
        if (UIUtil.isNullOrEmpty(strObjectId) || UIUtil.isNullOrEmpty(strLoginUser)){
            return  new MapList();
        }
        DomainObject ecr = DomainObject.newInstance(context, strObjectId);
        StringList typeSelectList = new StringList();
        typeSelectList.add(DomainConstants.SELECT_ID);
        typeSelectList.add(SELECT_OWNER);
        typeSelectList.add(DomainConstants.SELECT_CURRENT);
        typeSelectList.add(DomainConstants.SELECT_TYPE);
        typeSelectList.add("state[Review].actual");
        typeSelectList.add("state[Complete].actual");
        //任务描述保存着APR的审核信息 add by chenyan 2025/04/15
        typeSelectList.add(SELECT_DESCRIPTION);
        StringList relSelectList = new StringList();
        relSelectList.add(SELECT_ATTR_PROJECT_ROLE);
        String strWhere = buildStringInStrings("owner=='", strLoginUser, "'");
        MapList maps = ecr.getRelatedObjects(context, RELATIONSHIP_JF_ECR_TASK , // relationship pattern
                TYPE_JS_SIGN_TASK + "," + TYPE_JF_APRTask +"," + TYPE_JF_CustomerTask,                                    // object pattern
                typeSelectList,                            // object selects
                relSelectList, // relationship selects
                false,                                        // to direction
                true,                                        // from direction
                (short) 1,                                    // recursion level
                strWhere,                // object where clause
                "",
                (short) 0);
        //过滤出属性项目角色不为空的会签任务
        MapList projectRoleList = (MapList)maps.stream().filter(m ->{
            Map takInfo = (Map)m;
            String strProjectRole = (String) takInfo.get(SELECT_ATTR_PROJECT_ROLE);
            return UIUtil.isNotNullAndNotEmpty(strProjectRole);
        }).map(m ->{
            Map takInfo = (Map)m;
            String strProjectRole = (String) takInfo.get(SELECT_ATTR_PROJECT_ROLE);
            String strCurrent = (String) takInfo.get(DomainConstants.SELECT_CURRENT);
            String strTaskId = (String) takInfo.get(DomainConstants.SELECT_ID);
            String strTaskType= (String) takInfo.get(DomainConstants.SELECT_TYPE);
            Map signTaskMap = new HashMap<String,String>();
            signTaskMap.put("role",strProjectRole);
            signTaskMap.put("current",strCurrent);
            signTaskMap.put("signTaskId",strTaskId);
            if ("Complete".equals(strCurrent) || "Review".equals(strCurrent)){
                signTaskMap.put("submitTime",UIUtil.getValue(takInfo,"state[Review].actual"));
                signTaskMap.put("completeTime",UIUtil.getValue(takInfo,"state[Complete].actual"));
            }else {
                signTaskMap.put("submitTime","");
                signTaskMap.put("completeTime","");
            }
            signTaskMap.put(DomainConstants.SELECT_TYPE,strTaskType);
            signTaskMap.put("APRReviewNotes",UIUtil.getValue(takInfo,SELECT_DESCRIPTION));
            return signTaskMap;
        }).collect(Collectors.toCollection(MapList::new));
        return projectRoleList;
    }

    /**
     * 获取项目空间 排除基线
     * @param context
     * @param args
     * @author LIUJR
     * @throws
     * @return matrix.util.StringList
     * @date 2024/8/13 9:21
     * @description
     */
    public static StringList getDBSystemAllProjectSpaces(Context context, String[] args) throws Exception{
        JF_LOGGER.info("==============================getSystemAllProjectSpaces==============================");
        StringList strReturnList = new StringList();
        try {
            String strUser = context.getUser().toString();
            Vector assignments = PersonUtil.getAssignments(context, strUser);
            String strWhere = "type=='" + DomainConstants.TYPE_PROJECT_SPACE + "'";
            strBusSelectsList.add("from[" + DomainRelationship.RELATIONSHIP_MEMBER + "].to.name");
            MapList mlResult = DomainObject.findObjects(context, DomainConstants.TYPE_PROJECT_SPACE, DomainConstants.QUERY_WILDCARD, strWhere, strBusSelectsList);
            JF_LOGGER.info("mlResult.length：{}", mlResult.size());
            if (assignments.contains("jfLibAdmin")) {
                mlResult.stream().forEach(m -> {
                    Map map = (Map) m;
                    String strOid = UIUtil.getValue(map, DomainConstants.SELECT_ID);
                    strReturnList.add(strOid);
                });
            } else {
                mlResult.stream().forEach(m -> {
                    Map map = (Map) m;
                    String strOid = UIUtil.getValue(map, DomainConstants.SELECT_ID);
                    String strMember = UIUtil.getValue(map, "from[" + DomainRelationship.RELATIONSHIP_MEMBER + "].to.name");
                    if (strMember.contains(strUser)) {
                        strReturnList.add(strOid);
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
        JF_LOGGER.info("strReturnList.length：{}", strReturnList.size());

        return strReturnList;
    }

    /**
    * 获取项目空间 排除基线
    * @param context
	* @param args
    * @author LIUJR
    * @throws
    * @return matrix.util.StringList
    * @date 2024/8/13 9:21
    * @description
    */
    public static StringList getSystemAllProjectSpaces(Context context, String[] args) throws Exception{
        JF_LOGGER.info("==============================getSystemAllProjectSpaces==============================");
        StringList strReturnList = new StringList();
        try {
            String strUser = context.getUser().toString();
            String strWhere = "type=='" + DomainConstants.TYPE_PROJECT_SPACE + "'";
            strBusSelectsList.add("from[" + DomainRelationship.RELATIONSHIP_MEMBER + "].to.name");
            MapList mlResult = DomainObject.findObjects(context, DomainConstants.TYPE_PROJECT_SPACE, DomainConstants.QUERY_WILDCARD, strWhere, strBusSelectsList);
            JF_LOGGER.info("mlResult.length：{}", mlResult.size());
            mlResult.stream().forEach(m -> {
                Map map = (Map) m;
                String strOid = UIUtil.getValue(map, DomainConstants.SELECT_ID);
                String strMember = UIUtil.getValue(map, "from[" + DomainRelationship.RELATIONSHIP_MEMBER + "].to.name");
                if (strMember.contains(strUser)) {
                    strReturnList.add(strOid);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
        JF_LOGGER.info("strReturnList.length：{}", strReturnList.size());

        return strReturnList;
    }

    /**
    * 3dspace打开任意对象
    * @param context
	* @param dataId objectId
	* @param spaceURL space的基础路径
    * @author LIUJR
    * @throws
    * @return java.lang.String
    * @date 2024/8/26 9:31
    * @description
    */
    private String getURL(Context context, String dataId,String spaceURL) throws  Exception {
        StringBuffer buffer =  new StringBuffer();
        buffer.append(spaceURL +"/common/emxNavigator.jsp?objectId=" + dataId );
        return buffer.toString();
    }

    /**
    *  使用dashboard的app打开指定对象 并预览
    * @param context
	* @param vName 打开对象的显示名称
	* @param strObjectId objectId
	* @param appId appId
    * @author LIUJR
    * @throws
    * @return java.lang.String
    * @date 2024/8/26 9:30
    * @description
    */
    private  String getDashboardAppLink (Context context,String vName ,String strObjectId,String appId) throws  Exception{
        NotificationService notificationService = new NotificationService(context);
        String dashboardURL =notificationService.get3DDashboardURL();
        DomainObject domainObject = DomainObject.newInstance(context, strObjectId);
        String physicalId = domainObject.getInfo(context, "physicalid");
        //组装json
        JSONObject json = new JSONObject();
        JSONObject itemJson = new JSONObject();
        JSONArray childArray = new JSONArray();
        JSONObject childJson = new JSONObject();
        childJson.put("objectId", physicalId);
        childJson.put("objectType", "VPMReference");
        childJson.put("envId", "OnPremise");
        childJson.put("serviceId", "3DSpace");
        childJson.put("displayName", vName);
        childJson.put("displayType", "VPMReference");
        childJson.put("contextId", "ctx::VPLMProjectLeader.Company Name.Common Space");
        childJson.put("objectTaxonomies", new String[]{"PLMEntity","PLMReference","PLMCoreReference","LPAbstractReference","PHYSICALAbstractReference","VPMReference"});
        childArray.add(childJson);
        itemJson.put("items", childArray);
        json.put("data", itemJson);

        String strURL ="{\"" +
                "data" +
                "\":{" +
                "\"items\":[{" +
                "\"objectId\":\""+physicalId+"\"," +
                "\"objectType\":\"VPMReference\"," +
                "\"envId\":\"OnPremise\"," +
                "\"serviceId\":\"3DSpace\"," +
                "\"displayName\":\""+vName+"\"," +
                "\"displayType\":\"VPMReference\"," +
                "\"contextId\":\"ctx::VPLMProjectLeader.Company Name.Common Space\"," +
                "\"objectTaxonomies\":[\"PLMEntity\"," +
                "\"PLMReference\",\"PLMCoreReference\"," +
                "\"LPAbstractReference\"," +
                "\"PHYSICALAbstractReference\"," +
                "\"VPMReference\"" +
                "]" +
                "}]" +
                "}" +
                "}";
        strURL = json.toJSONString();
        /* appId
            ENXDISC_AP   - 3D Navigate
            ENOR3D_AP - 3D MarkUp
            ENOSCEN_AP - Product Explorer
        */
        strURL = dashboardURL+"/#/tab:New%20Tab/app:" + appId +"/content:X3DContentId=" + URLEncoder.encode(strURL, "utf-8");
        StringBuffer buffer = new StringBuffer();
        buffer.append(strURL);
        return buffer.toString();
    }

    /**
    *
    * @param context
	* @param args
    * @author LIUJR
    * @throws
    * @return matrix.util.StringList
    * @date 2024/8/26 17:33
    * @description
    */
    public static StringList getUserGroupPersons(Context context, String[] args) {
        StringList infoList = new StringList();
        try{
            Map paramMap = (Map) JPO.unpackArgs(args);
            JF_LOGGER.info("============getUserGroupPersons==================");
            JF_LOGGER.info("paramMap: {}", paramMap);
            String groupName = UIUtil.getValue(paramMap, "groupName");//director 总监\department_manager部门经理\ major_manager 专业经理
            String groupNameKey = "3dspace.GroupName."+groupName;
            groupName =getBasicUrl(context, new String[]{groupNameKey});
            if(UIUtil.isNullOrEmpty(groupName)){
                groupNameKey = "3dspace.GroupName.director";
                groupName =getBasicUrl(context, new String[]{groupNameKey});
            }
            JF_LOGGER.info("groupName:{}", groupName);
            String objectId = findObject(context, "Group", "name=='" + groupName + "'");
            if (UIUtil.isNullOrEmpty(objectId)) {
                MapList mlResult = DomainObject.findObjects(context, DomainConstants.TYPE_PERSON, DomainConstants.QUERY_WILDCARD, "", strBusSelectsList);
                infoList = (StringList) mlResult.stream().map(m -> {
                    Map map  = (Map) m;
                    return UIUtil.getValue(map, DomainConstants.SELECT_ID);
                }).collect(Collectors.toCollection(SelectList::new));
            } else {
                DomainObject domainObject = DomainObject.newInstance(context, objectId);
                infoList = domainObject.getInfoList(context, "from[Group Member].to.id");
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
        JF_LOGGER.info("persons:{}", infoList.toString());
        return infoList;
    }

    /**
    * 获取项目中特定角色的人员
    * @param context
	* @param strProjectId
	* @param strRoleName
	* @param typeSelectList
	* @param relSelectList
    * @author LIUJR
    * @throws
    * @return com.matrixone.apps.domain.util.MapList
    * @date 2025/4/18 14:41
    * @description
    */
    public static MapList getProjectPersonByRoleName(Context context ,String strProjectId,String strRoleName,StringList typeSelectList,StringList relSelectList) throws Exception{
        try {
            ContextUtil.pushContext(context);
            DomainObject project = DomainObject.newInstance(context,strProjectId);
            MapList maps = project.getRelatedObjects(context, DomainConstants.RELATIONSHIP_MEMBER , // relationship pattern
                    DomainConstants.TYPE_PERSON ,                                    // object pattern
                    typeSelectList,                            // object selects
                    relSelectList, // relationship selects
                    false,                                        // to direction
                    true,                                        // from direction
                    (short) 1,                                    // recursion level
                    "",                // object where clause
                    buildStringInStrings("attribute[Project Role].value=='",strRoleName,"'"),
                    (short) 0);
            return maps;
        }finally {
            ContextUtil.popContext(context);
        }
    }

    /**
    *
    *@description 获取ECR excel表格映射属性
    *@param context
	*@param strTableName    表名
    *@return com.matrixone.apps.domain.util.MapList
    *@throws
    *@author CHENYAN
    *@date 2024/9/27 17:44
    */

    public static MapList getECRTableMapping(Context context,String strTableName) throws Exception{
        MapList mapList = new MapList();
        Page pageAttributePopulation = new Page("SignTaskProperties_zh.xml");
        pageAttributePopulation.open(context);
        String strProperties = pageAttributePopulation.getContents(context);
        pageAttributePopulation.close(context);
        // 使用 ByteArrayInputStream 将字符串转为输入流
        try (InputStream inputStream = new ByteArrayInputStream(strProperties.getBytes("UTF-8"))) {
            // 创建 DocumentBuilder
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(inputStream);
            // 获取所有 config 元素
            NodeList configurationNodes = document.getElementsByTagName("configuration");
            for (int i = 0; i < configurationNodes.getLength(); i++) {
                Element configurationElement = (Element) configurationNodes.item(i);
                if (strTableName.equals(configurationElement.getAttribute("id"))) {
                    // 找到匹配的 configuration, 获取它下的 config 元素
                    NodeList configNodes = configurationElement.getElementsByTagName("config");
                    for (int j = 0; j < configNodes.getLength(); j++) {
                        Element configElement = (Element) configNodes.item(j);
                        String id = configElement.getAttribute("id");
                        String value = configElement.getAttribute("value");
                        String selectValue = configElement.getAttribute("selectValue");
                        String isEdit = configElement.getAttribute("isEdit");
                        HashMap<String, String> map = new HashMap<>();
                        map.put("id", id);
                        map.put("value", value);
                        map.put("selectValue", selectValue);
                        map.put("isEdit", isEdit);
                        mapList.add(map);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return mapList;
    }


    /**
    *
    *@description 获取xml中指定集合的所有数据
    *@param context
	*@param strTableName
    *@return com.matrixone.apps.domain.util.MapList
    *@throws
    *@author CHENYAN
    *@date 2025/3/21 11:30
    */
    public static MapList getJFCostChangeHistoryTemplateMapping(Context context,String strTableName) throws Exception{
        MapList mapList = new MapList();
        Page pageAttributePopulation = new Page("SignTaskProperties_zh.xml");
        pageAttributePopulation.open(context);
        String strProperties = pageAttributePopulation.getContents(context);
        pageAttributePopulation.close(context);
        // 使用 ByteArrayInputStream 将字符串转为输入流
        try (InputStream inputStream = new ByteArrayInputStream(strProperties.getBytes("UTF-8"))) {
            // 创建 DocumentBuilder
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(inputStream);
            // 获取所有 config 元素
            NodeList configurationNodes = document.getElementsByTagName("configuration");
            for (int i = 0; i < configurationNodes.getLength(); i++) {
                Element configurationElement = (Element) configurationNodes.item(i);
                if (strTableName.equals(configurationElement.getAttribute("id"))) {
                    // 找到匹配的 configuration, 获取它下的 config 元素
                    NodeList configNodes = configurationElement.getElementsByTagName("config");
                    for (int j = 0; j < configNodes.getLength(); j++) {
                        Element configElement = (Element) configNodes.item(j);
                        HashMap<String, String> map = new HashMap<>();
                        NamedNodeMap attributes = configElement.getAttributes();
                        // 遍历属性集合
                        for (int k = 0; k < attributes.getLength(); k++) {
                            Node attribute = attributes.item(k);
                            String attributeName = attribute.getNodeName();
                            String attributeValue = attribute.getNodeValue();
                            map.put(attributeName,attributeValue);
                        }
                        mapList.add(map);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return mapList;
    }

    /**
    * 校验两个对象是否有关系
    * @param context
	* @param strCheckId
	* @param strCheckedId
	* @param strRelName
	* @param toDirection
	* @param fromDirection
    * @author LIUJR
    * @throws
    * @return boolean
    * @date 2025/2/10 11:10
    * @description
    */
    public static boolean checkTwoBusHasConnection(Context context ,String strCheckId ,String strCheckedId ,String strRelName,boolean toDirection ,boolean fromDirection) throws Exception{
        return  checkTwoBusHasConnection(context,strCheckId,strCheckedId,strRelName,toDirection,fromDirection,"","");
    }

    /**
    * 校验两个对象是否有关系
    * @param context
	* @param strCheckId
	* @param strCheckedId
	* @param strRelName
	* @param toDirection
	* @param fromDirection
	* @param strBusWhere
	* @param strRelWhere
    * @author LIUJR
    * @throws
    * @return boolean
    * @date 2025/2/10 11:10
    * @description
    */
    public static boolean checkTwoBusHasConnection(Context context ,String strCheckId ,String strCheckedId ,String strRelName,boolean toDirection ,boolean fromDirection,String strBusWhere,String strRelWhere) throws Exception{
        if (UIUtil.isNullOrEmpty(strCheckId) || UIUtil.isNullOrEmpty(strCheckedId) ||
                UIUtil.isNullOrEmpty(strRelName) || UIUtil.isNullOrEmpty(strCheckedId) ){
            return  false ;
        }
        String strBWhere = "";
        if (UIUtil.isNotNullAndNotEmpty(strBusWhere)){
            strBWhere = JF_PublicMethodClass_mxJPO.buildStringInStrings("(",strBusWhere,")","&&","(",SELECT_ID,"=='",strCheckedId,"'",")");
        }else {
            strBWhere = JF_PublicMethodClass_mxJPO.buildStringInStrings("(",SELECT_ID,"=='",strCheckedId,"'",")");
        }
        DomainObject checkBus = DomainObject.newInstance(context, strCheckId);
        MapList res = checkBus.getRelatedObjects(context, strRelName, // relationship pattern
                "*",                                    // object pattern
                JF_Util_mxJPO.basicBolistSel(),                            // object selects
                JF_Util_mxJPO.basicRellistSel(), // relationship selects
                toDirection,                                        // to direction
                fromDirection,                                        // from direction
                (short) 1,                                    // recursion level
                strBWhere,                // object where clause
                strRelWhere,
                (short) 0);
        return res.size() > 0 ? true : false ;
    }


    /**
    * 克隆对象
    * @param context
	* @param strType
	* @param strName
	* @param revision
	* @param strPolicy
    * @author LIUJR
    * @throws
    * @return java.lang.String
    * @date 2025/2/10 11:11
    * @description
    */
    public static String cloneDomainObject(Context context, String strType, String strName, String revision, String strPolicy) throws Exception{
        DomainObject domainObject = DomainObject.newInstance(context);
        domainObject.createObject(context, strType, strName, revision, strPolicy, context.getVault().getName());
        return  domainObject.getId(context);
    }

    /***
    * 获取对象排序，返回对应的类型
    * @param context
	* @param type
	* @param strWhere
	* @param list
	* @param keSort
	* @param sort
	* @param sortType
    * @author LIUJR
    * @throws
    * @return java.lang.String
    * @date 2025/2/10 11:14
    * @description
    */
    public static String getObjectsKeySort(Context context, String type, String strWhere, StringList selList, String keSort, String sort, String sortType) throws Exception {
        String formattedNum = "";
        MapList  mapList = DomainObject.findObjects(context, type, "*", strWhere, selList);
        if (mapList.size() > 0){
            mapList.addSortKey(keSort, sort, sortType);
            mapList.sort();
            JF_LOGGER.info("maps:{}",mapList);
            Map infoMap = (Map) mapList.get(0);
            String strName = (String) infoMap.get(SELECT_NAME);
            String substring = strName.substring(12);
            int number = Integer.parseInt(substring);
            // 定义格式为至少4位数字
            DecimalFormat df = new DecimalFormat("0000");
            formattedNum = df.format(number + 1);
        } else {
            formattedNum = "0001";
        }
        return formattedNum;
    }

    /**
     * 获取ESO模块节点下当前最新的有效签发记录
     * 1. 仅查询审核次数和颜色标识均已维护的签发记录；
     * 2. 优先取审核次数最大的记录；
     * 3. 审核次数相同时按Name倒序，Name的业务定义能够代表签发记录的实际先后顺序。
     *
     * @param context 上下文
     * @param esoTaskId ESO模块节点ID
     * @return java.util.Map 最新签发记录；没有有效记录时返回空Map
     * @throws Exception 查询签发记录失败时抛出异常
     * @author LIUJR
     * @date 2026/7/29
     */
    public static Map getLatestESOReview(Context context, String esoTaskId) throws Exception {
        if (UIUtil.isNullOrEmpty(esoTaskId)) {
            return new HashMap();
        }

        MapList esoReviewList = DomainObject.newInstance(context, esoTaskId).getRelatedObjects(
                context,
                "JFESOTask2ESOReview",
                "JFESOReview",
                StringList.create(
                        DomainConstants.SELECT_ID,
                        DomainConstants.SELECT_NAME,
                        "attribute[JF_ReviewCounte]",
                        "attribute[JF_PhaseState]"),
                new StringList(),
                false,
                true,
                (short) 1,
                "attribute[JF_ReviewCounte]!=''&&attribute[JF_PhaseState]!=''",
                "",
                0);
        if (esoReviewList.isEmpty()) {
            return new HashMap();
        }

        // 将审核次数转换为整数排序字段，避免字符串排序时出现“10”小于“2”的问题。
        for (int i = 0; i < esoReviewList.size(); i++) {
            Map reviewMap = (Map) esoReviewList.get(i);
            String reviewCount = UIUtil.getValue(reviewMap, "attribute[JF_ReviewCounte]");
            int reviewCountValue = 0;
            try {
                reviewCountValue = Integer.parseInt(reviewCount.trim());
            } catch (Exception ignored) {
            }
            reviewMap.put("ReviewCount", String.valueOf(reviewCountValue));
        }

        // 先按审核次数整数倒序，再按Name倒序；Name按业务定义代表签发记录的实际先后顺序。
        esoReviewList.addSortKey("ReviewCount", ProgramCentralConstants.DESCENDING_SORT, ProgramCentralConstants.SORTTYPE_INTEGER);
        esoReviewList.addSortKey(DomainConstants.SELECT_NAME, ProgramCentralConstants.DESCENDING_SORT, ProgramCentralConstants.SORTTYPE_STRING);
        esoReviewList.sort();
        return (Map) esoReviewList.get(0);
    }

    /**
    * 获取项目的特定的一个人的id
     *  例如： 获取整椅经理，传入的relWhere:"attribute[Project Role]=='Chair manager'"
     * @param context
     * @param projectId
    * @author LIUJR
    * @throws
    * @return java.lang.String
    * @date 2025/2/18 16:14
    * @description
    */
    public static String getProjectWherePerson(Context context, String projectId, String select, String busWhere, String relWhere) throws Exception{
        String chairManagerId = EMPTY_STRING;
        try {
            ContextUtil.pushContext(context);
            DomainObject objectProject = DomainObject.newInstance(context, projectId);
            MapList mapList = objectProject.getRelatedObjects(
                    context,
                    RELATIONSHIP_MEMBER,
                    TYPE_PERSON,
                    new StringList(select),
                    new StringList(),
                    false,
                    true,
                    (short) 1,
                    busWhere,
                    relWhere,
                    0
            );
            JF_LOGGER.info("!!!!!!!!!!!!!!malist:{}", mapList.toString());
            if (!mapList.isEmpty()) {
                Map map = (Map) mapList.get(0);
                chairManagerId = UIUtil.getValue(map, select);
            }
        } catch (FrameworkException e) {
            throw e;
        } finally {
            ContextUtil.popContext(context);
        }
        return chairManagerId;
    }

    /**
    * 获取项目成员
    * @param context
	* @param projectId
	* @param select
    * @author LIUJR
    * @throws
    * @return matrix.util.StringList
    * @date 2025/2/19 14:30
    * @description
    */
    public static StringList getProjectAllPersons(Context context, String projectId, String select) throws Exception{
        StringList allPersonList = new StringList();
        try {
            ContextUtil.pushContext(context);
            DomainObject objectProject = DomainObject.newInstance(context, projectId);
            MapList mapList = objectProject.getRelatedObjects(
                    context,
                    RELATIONSHIP_MEMBER,
                    TYPE_PERSON,
                    new StringList(select),
                    new StringList(),
                    false,
                    true,
                    (short) 1,
                    "",
                    "",
                    0
            );
            JF_LOGGER.info("!!!!!!!!!!!!!!malist:{}", mapList.toString());
            if (!mapList.isEmpty()) {
                allPersonList = (StringList) mapList.stream().map(m -> {
                    Map map = (Map) m;
                    return UIUtil.getValue(map, select);
                }).collect(Collectors.toCollection(SelectList::new));
            }
        } catch (FrameworkException e) {
            throw e;
        } finally {
            ContextUtil.popContext(context);
        }
        return allPersonList;
    }

    /**
    * table表格中编辑权限，为owner可编辑
    * @param
    * @author LIUJR
    * @throws
    * @return java.lang.Boolean
    * @date 2025/2/20 10:04
    * @description
    */
    public StringList tableColumnEditAccessOwner(Context context, String[] args) throws Exception{
        StringList result = new StringList();
        Map argsMap = JPO.unpackArgs(args);
        Map requestMap = (Map)argsMap.get("requestMap");
        String strParentOID = (String)requestMap.get("parentOID");
        String strTable = (String)requestMap.get("table");
        try {
            StringList strObjectIdList = JF_PublicMethodClass_mxJPO.getObjectIdList(args);
            // add by chenyan 制造费表格只有报价才可编辑 2025/03/27
            if ("JFCostBreakDownTable".equals(strTable)){
                DomainObject BO = DomainObject.newInstance(context, strParentOID);
                String strCurrent = BO.getInfo(context, SELECT_CURRENT);
                Boolean isEditState = "Review".equals(strCurrent);
                for (String objectId : strObjectIdList) {
                    DomainObject object = DomainObject.newInstance(context, objectId);
                    String owner = object.getInfo(context, SELECT_OWNER);
                    if (isEditState && context.getUser().equalsIgnoreCase(owner)) {
                        result.add(Boolean.TRUE.toString());
                    } else {
                        result.add(Boolean.FALSE.toString());
                    }
                }
            }else {
                for (String objectId : strObjectIdList) {
                    DomainObject object = DomainObject.newInstance(context, objectId);
                    String owner = object.getInfo(context, SELECT_OWNER);
                    if (context.getUser().equalsIgnoreCase(owner)) {
                        result.add(Boolean.TRUE.toString());
                    } else {
                        result.add(Boolean.FALSE.toString());
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
    * 复制Excel列
    * @param sheet 工作表
	* @param sourceColIndex  源列索引(0-based)
	* @param iConfigEndCell  最后一列
	* @param targetColIndex  目标列索引(0-based)
    * @author LIUJR
    * @throws
    * @return void
    * @date 2025/3/26 10:41
    * @description
    */
    public static void copyColumn(Sheet sheet, int sourceColIndex, int iConfigEndCell, int targetColIndex) {
        // 首先插入一列空列(这将把原F列及以后的列向右移动)   移动的列数，n>0表示向左移动，n<0表示向右移动。
        JF_LOGGER.info("sourceColIndex:{}", sourceColIndex);
        JF_LOGGER.info("iConfigEndCell:{}", iConfigEndCell);
        JF_LOGGER.info("targetColIndex:{}", targetColIndex);
        sheet.shiftColumns(targetColIndex + 1, iConfigEndCell, -1);

        // 遍历每一行
        for (int r = 0; r <= sheet.getLastRowNum(); r++) {
            Row row = sheet.getRow(r);
            if (row == null) {
                continue;
            }

            // 获取源单元格
            Cell sourceCell = row.getCell(sourceColIndex);
            if (sourceCell == null) {
                continue;
            }

            // 创建目标单元格并复制内容和样式
            Cell targetCell = row.createCell(targetColIndex);
            copyCell(sourceCell, targetCell);
        }
    }

    /**
    * 复制单元格内容和样式
    * @param sourceCell 源单元格
	* @param targetCell 目标单元格
    * @author LIUJR
    * @throws
    * @return void
    * @date 2025/3/26 10:40
    * @description
    */
    public static void copyCell(Cell sourceCell, Cell targetCell) {
        // 复制单元格样式
        CellStyle newStyle = targetCell.getSheet().getWorkbook().createCellStyle();
        newStyle.cloneStyleFrom(sourceCell.getCellStyle());
        targetCell.setCellStyle(newStyle);

        // 根据单元格类型复制内容
        switch (sourceCell.getCellType()) {
            case STRING:
                targetCell.setCellValue(sourceCell.getStringCellValue());
                break;
            case NUMERIC:
                targetCell.setCellValue(sourceCell.getNumericCellValue());
                break;
            case BOOLEAN:
                targetCell.setCellValue(sourceCell.getBooleanCellValue());
                break;
            case FORMULA:
                targetCell.setCellFormula(sourceCell.getCellFormula());
                break;
            case BLANK:
                targetCell.setBlank();
                break;
            case ERROR:
                targetCell.setCellErrorValue(sourceCell.getErrorCellValue());
                break;
            default:
                targetCell.setCellValue(sourceCell.getStringCellValue());
        }
    }

    public static String get3DspaceServicePath(String strIndex){
        //文件地址
        String classPath = Thread.currentThread().getContextClassLoader().getResource("").getPath();
        String strFileTemPath = classPath.substring(0, classPath.indexOf(strIndex));
        return strFileTemPath ;
    }

    /**
    *
    *@description 判断字符串是否是数值型
    *@param str
    *@return boolean
    *@throws
    *@author CHENYAN
    *@date 2025/4/25 21:29
    */
    public static boolean isNumeric(String str) {
        if (str == null || str.isEmpty()) {
            return false;
        }
        // 正则表达式匹配整数和浮点数（包括负数）
        String regex = "-?\\d+(\\.\\d+)?([eE][+-]?\\d+)?";
        return str.matches(regex);
    }

    /**
    *
    *@description 检查对象是否有指定属性
    *@param context
	*@param bo
	*@param strAttrName  属性名
    *@return boolean
    *@throws
    *@author CHENYAN
    *@date 2025/6/3 14:58
    */
    public static boolean checkBOHasAttribute(Context context ,DomainObject bo,String strAttrName) {
        StringList selectList = new StringList();
        String strTypeAttr = String.format("type.attribute[%s]",strAttrName);
        String strInterfaceAttr = String.format("interface[*].attribute[%s]",strAttrName);
        selectList.add(strTypeAttr);
        selectList.add(strInterfaceAttr);
        boolean res = false ;
        Map infoMap = null;
        try {
            infoMap = bo.getInfo(context, selectList);
            for (Object oValue : infoMap.values()) {
                String strValue = (String)oValue;
                if (UIUtil.isNotNullAndNotEmpty(strValue)){
                    strValue = strValue.trim();
                    if ("TRUE".equalsIgnoreCase(strValue)) {
                        res = true;
                        break;
                    }
                }
            }
        } catch (FrameworkException e) {
            JF_LOGGER.error(e.getMessage());
            res = false ;
        }
        return res ;
    }


    public static boolean isNumericOrPercentage(String str) {
        if (str == null || str.trim().isEmpty()) {
            return false;
        }

        str = str.trim();

        // 判断是否是百分比格式
        if (str.endsWith("%")) {
            String numberPart = str.substring(0, str.length() - 1).trim();
            return isNumeric(numberPart);
        }

        // 判断是否是普通数字（整数或小数）
        return isNumeric(str);
    }
    /**
    *
    *@description 字符串转换成double
    *@param str
    *@return java.lang.Double
    *@throws
    *@author CHENYAN
    *@date 2025/6/3 16:00
    */
    public static Double parseToDouble(String str) {
        Double dValue = null ;
        if (UIUtil.isNotNullAndNotEmpty(str)) {
            str = str.trim();
            // 情况一：判断是否是百分比格式
            if (str.endsWith("%")) {
                String numberPart = str.substring(0, str.length() - 1).trim();
                if (isNumeric(numberPart)) {
                    try {
                        double value = Double.parseDouble(numberPart) / 100;
                        dValue = value ;
                    } catch (NumberFormatException e) {

                    }
                }
            // 情况二：判断是否是普通数字格式
            } else if (isNumeric(str)) {
                try {
                    dValue =  Double.parseDouble(str);
                } catch (NumberFormatException e) {
                    JF_LOGGER.error(e.getMessage());
                }
            }
        }
        return dValue;
    }

    /**
     * 项目成员SDT-项目经理能操作权限
     * @param context
     * @param args
     * @author LIUJR
     * @throws
     * @return java.lang.Boolean
     * @date 2025/3/26 17:19
     * @description
     */
    public Boolean projectManagerAccessIsPM(Context context, String[] args) {
        Boolean flag = Boolean.TRUE;
        try{
            Map params = JPO.unpackArgs(args);
            String strObjectId = (String) params.get("objectId");
            JF_MBOM_mxJPO jfMbomMxJPO = new JF_MBOM_mxJPO();
            Boolean mbomRootResponsibleIsPM = jfMbomMxJPO.getMBOMRootResponsibleIsPM(context, strObjectId);
            JF_LOGGER.info("!!!!!!!!!!!!!!!!");
            JF_LOGGER.info("mbomRootResponsibleIsPM:{}", mbomRootResponsibleIsPM);
            if (!mbomRootResponsibleIsPM) {
                return Boolean.FALSE;
            }
            DomainObject projectObject = DomainObject.newInstance(context, strObjectId);
            //拿取项目的成员以及关系属性project role
            MapList mapList = projectObject.getRelatedObjects(
                    context,
                    DomainRelationship.RELATIONSHIP_MEMBER,
                    DomainConstants.TYPE_PERSON,
                    new StringList(SELECT_NAME),
                    new StringList(),
                    false,
                    true,
                    (short) 1, // recursion level
                    "", //object where clause
                    "attribute[Project Role]=='Project manager'", //relationship where clause
                    0
            );
            StringList projectRoleList = (StringList) mapList.stream().map(m -> {
                Map map = (Map) m;
                return UIUtil.getValue(map, DomainConstants.SELECT_NAME);
            }).collect(Collectors.toCollection(StringList::new));
            if (projectRoleList.contains(context.getUser())) {
                flag = Boolean.TRUE;
            } else {
                flag = Boolean.FALSE;
            }
        } catch (Exception e) {
            e.printStackTrace();
            flag = Boolean.FALSE;
        }
        return flag;
    }


    /**
     * 项目成员SDT-项目经理能 admin 操作权限
     * @param context
     * @param args
     * @author LIUJR
     * @throws
     * @return java.lang.Boolean
     * @date 2025/3/26 17:19
     * @description
     */
    public Boolean projectManagerAccessOrAdmin(Context context, String[] args) {
        Boolean flag = Boolean.TRUE;
        try{
            Map params = JPO.unpackArgs(args);
            String strObjectId = (String) params.get("objectId");
            DomainObject projectObject = DomainObject.newInstance(context, strObjectId);
            //拿取项目的成员以及关系属性project role
            MapList mapList = projectObject.getRelatedObjects(
                    context,
                    DomainRelationship.RELATIONSHIP_MEMBER,
                    DomainConstants.TYPE_PERSON,
                    new StringList(SELECT_NAME),
                    new StringList(),
                    false,
                    true,
                    (short) 1, // recursion level
                    "", //object where clause
                    "attribute[Project Role]=='Project manager'", //relationship where clause
                    0
            );
            StringList projectRoleList = (StringList) mapList.stream().map(m -> {
                Map map = (Map) m;
                return UIUtil.getValue(map, DomainConstants.SELECT_NAME);
            }).collect(Collectors.toCollection(StringList::new));
            if (projectRoleList.contains(context.getUser())) {
                flag = Boolean.TRUE;
            } else if ("admin_platform".equalsIgnoreCase(context.getUser())){
                flag = Boolean.TRUE;
            } else {
                flag = Boolean.FALSE;
            }
        } catch (Exception e) {
            e.printStackTrace();
            flag = Boolean.FALSE;
        }
        return flag;
    }

    /**
     * 项目成员SDT-项目经理能操作权限
     * @param context
     * @param args
     * @author LIUJR
     * @throws
     * @return java.lang.Boolean
     * @date 2025/3/26 17:19
     * @description
     */
    public Boolean projectManagerAccess(Context context, String[] args) {
        Boolean flag = Boolean.TRUE;
        try{
            Map params = JPO.unpackArgs(args);
            String strObjectId = (String) params.get("objectId");
            DomainObject projectObject = DomainObject.newInstance(context, strObjectId);
            //拿取项目的成员以及关系属性project role
            MapList mapList = projectObject.getRelatedObjects(
                    context,
                    DomainRelationship.RELATIONSHIP_MEMBER,
                    DomainConstants.TYPE_PERSON,
                    new StringList(SELECT_NAME),
                    new StringList(),
                    false,
                    true,
                    (short) 1, // recursion level
                    "", //object where clause
                    "attribute[Project Role]=='Project manager'", //relationship where clause
                    0
            );
            StringList projectRoleList = (StringList) mapList.stream().map(m -> {
                Map map = (Map) m;
                return UIUtil.getValue(map, DomainConstants.SELECT_NAME);
            }).collect(Collectors.toCollection(StringList::new));
            if (projectRoleList.contains(context.getUser())) {
                flag = Boolean.TRUE;
            } else {
                flag = Boolean.FALSE;
            }
        } catch (Exception e) {
            e.printStackTrace();
            flag = Boolean.FALSE;
        }
        return flag;
    }

    /**
    * 获取项目中的供货件
    * @param context
	* @param projectObject
    * @author LIUJR
    * @throws
    * @return matrix.util.StringList
    * @date 2025/8/14 13:49
    * @description
    */
    public static StringList getProjectObjectZeroPart(Context context, DomainObject projectObject) throws Exception {
        StringList relList = JF_Util_mxJPO.basicRellistSel();
        StringList selList = JF_Util_mxJPO.basicBolistSel();
        MapList partMapList = projectObject.getRelatedObjects(context,
                JF_PLMConstants_mxJPO.rel_JFProject2RootPart, //pattern to match relationships
                JF_PLMConstants_mxJPO.TYPE_VPMReference, //pattern to match types
                selList, //the eMatrix StringList object that holds the list of select statement pertaining to Business Obejcts.
                relList, //the eMatrix StringList object that holds the list of select statement pertaining to Relationships.
                false, //get To relationships
                true, //get From relationships
                (short) 1, //the number of levels to expand, 0 equals expand all.
                "", //where clause to apply to objects, can be empty ""
                "attribute[JFZeroPart]==Y", //where clause to apply to relationship, can be empty ""
                (short) 0);//limit
        StringList partList = (StringList) partMapList.stream().map(m -> {
            Map map = (Map) m;
            return UIUtil.getValue(map, SELECT_ID);
        }).collect(Collectors.toCollection(StringList::new));
        return partList;
    }

    /**
     * table列的展示方法
     * @param context
     * @param args
     * @return
     * @throws Exception
     */
    public Vector tableViewColumnFromObjectMapList(Context context, String[] args)
            throws Exception {
        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        Map columnMap = (Map) programMap.get("columnMap");
        String columnName = (String)columnMap.get("name");
        MapList objectList = (MapList) programMap.get("objectList");
        Vector retVector = new Vector();
        if (objectList != null && objectList.size() > 0) {
            Map objectMap = null;
            String displayName = "";
            for (int i = 0; i < objectList.size(); i++) {
                objectMap  = (Map)objectList.get(i);
                if ("JF_belongProject".equalsIgnoreCase(columnName)) {
                    String projectId = (String)  objectMap.get("projectId");
                    String value = (String) objectMap.get(columnName);
                    displayName = getConstructDataString(context, projectId, value);
                } else {
                    displayName = (String) objectMap.get(columnName);
                }
                retVector.add(displayName);
            }
        }
        return retVector;
    }

    /**
     * 项目成员SDT-AME能操作权限  当责任人为AME才有权限
     * @param context
     * @param args
     * @author LIUJR
     * @throws
     * @return java.lang.Boolean
     * @date 2025/3/26 17:19
     * @description
     */
    public Boolean projectAMEAndFormOrTrimAMEAccess(Context context, String[] args) {
        JF_LOGGER.info("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        Boolean flag = Boolean.TRUE;
        try{
            Map params = JPO.unpackArgs(args);
            JF_LOGGER.info("params:{}", params);
            String strObjectId = (String) params.get("objectId");
            //判断当前的责任人是否是AME
            JF_MBOM_mxJPO jfMbomMxJPO = new JF_MBOM_mxJPO();
            Boolean mbomRootResponsibleIsPM = jfMbomMxJPO.getMBOMRootResponsibleIsPM(context, strObjectId);
            JF_LOGGER.info("mbomRootResponsibleIsPM:{}", mbomRootResponsibleIsPM);
            if (mbomRootResponsibleIsPM) {
                return Boolean.FALSE;
            }
            DomainObject projectObject = DomainObject.newInstance(context, strObjectId);
            //拿取项目的成员以及关系属性project role
            MapList mapList = projectObject.getRelatedObjects(
                    context,
                    DomainRelationship.RELATIONSHIP_MEMBER,
                    DomainConstants.TYPE_PERSON,
                    new StringList(SELECT_NAME),
                    new StringList(),
                    false,
                    true,
                    (short) 1, // recursion level
                    "", //object where clause
                    "attribute[Project Role]=='AME representative' || " +
                            "attribute[Project Role]=='Foam AME representative' || " +
                            "attribute[Project Role]=='Trim AME representative'", //relationship where clause
                    0
            );
            StringList projectRoleList = (StringList) mapList.stream().map(m -> {
                Map map = (Map) m;
                return UIUtil.getValue(map, DomainConstants.SELECT_NAME);
            }).collect(Collectors.toCollection(StringList::new));
            if (projectRoleList.contains(context.getUser())) {
                flag = Boolean.TRUE;
            } else {
                flag = Boolean.FALSE;
            }
        } catch (Exception e) {
            e.printStackTrace();
            flag = Boolean.FALSE;
        }
        return flag;
    }

    /**
    *
     * 场景:[{零件号=GT0001245,版本=AB.1, level=0, objReadAccess=TRUE, name=PCR-0000021},
     *      {零件号=GT0001285,版本=AC.1, level=0, objReadAccess=TRUE, name=PCR-0000028}...]
     *      按 零件号 分组,每组内按 版本 字段排序；版本 的格式如：AB.1、AB.2、AC.1、AC.3 等，
     *      其排序规则为：先比较前两个字母（从 AA 到 AZ，按字典序）；若字母相同，则比较小数点后的数字（按数值大小）；
     *       整体顺序：AA.1 < AA.2 < ... < AA.9 < AA.10 < AB.1 < ... < AZ.99 等。
    * @param list
    * @author LIUJR
    * @throws
    * @return java.util.Map<java.lang.String,java.util.List<java.util.Map<java.lang.String,java.lang.String>>>
    * @date 2025/11/28 11:23
    * @description
    */
    public static Map<String, List<Map<String, String>>> groupAndSortByNumberAndRev(MapList list) {

        // 自定义 rev 比较器
        Comparator<String> revComparator = (rev1, rev2) -> {
            if (rev1 == null && rev2 == null) return 0;
            if (rev1 == null) return -1;
            if (rev2 == null) return 1;

            String[] p1 = rev1.split("\\.", 2);
            String[] p2 = rev2.split("\\.", 2);

            if (p1.length != 2 || p2.length != 2) {
                return rev1.compareTo(rev2); // fallback
            }

            int prefixCmp = p1[0].compareTo(p2[0]);
            if (prefixCmp != 0) {
                return prefixCmp;
            }

            try {
                int n1 = Integer.parseInt(p1[1]);
                int n2 = Integer.parseInt(p2[1]);
                return Integer.compare(n1, n2);
            } catch (NumberFormatException e) {
                return p1[1].compareTo(p2[1]);
            }
        };

        return (Map<String, List<Map<String, String>>>) list.stream()
                .collect(Collectors.groupingBy(
                        map -> (String)((Map)map).get(SELECT_ATTR_V_PART_NUMBER),
                        Collectors.collectingAndThen(
                                Collectors.toList(),
                                sublist -> {
                                    sublist.sort(Comparator.comparing(map -> (String) ((Map)map).get(SELECT_REVISION), revComparator));
                                    return sublist;
                                }
                        )
                ));
    }

    /**
    * 获取零件中的最新冻结版本
     * DR校验规则中【数据结构中处在“冻结”的数据必须是最新修订版本】改为【数据结构中处在“冻结”的数据必须是最新冻结版本，且没有更高的发布版本】
    * @param context
	* @param partId
	* @param partName
	* @param revision
    * @author LIUJR
    * @throws
    * @return java.lang.String
    * @date 2025/12/12 9:21
    * @description
    */
    public static int getLatestFrozenAndHasHigherReleaseVersion(Context context, String partName, String partId, String revision) throws Exception{
        int flag = 0;
        try {
            DomainObject domainObject = DomainObject.newInstance(context);
            domainObject.setId(partId);
            MapList allMapList = domainObject.getRevisionsInfo(context, StringList.create(SELECT_ID, SELECT_CURRENT,SELECT_REVISION), StringList.create());
//            MapList frozenMapList = DomainObject.findObjects(context, JF_PLMConstants_mxJPO.TYPE_VPMReference, "*",
//                    "current=='FROZEN'" + "&&name=='" + partName + "'", StringList.create(SELECT_ID, SELECT_REVISION));
            String latestRev = null;
            JF_LOGGER.info("!!!!!!!!!!!!!");
            JF_LOGGER.info("allMapList:{}", allMapList);
            for (int i = 0; i < allMapList.size(); i++) {
                Map map = (Map) allMapList.get(i);
                String rev = UIUtil.getValue(map, SELECT_REVISION);
                String current = UIUtil.getValue(map, SELECT_CURRENT);
                if (!"FROZEN".equalsIgnoreCase(current)) {
                    continue;
                }
                // 比较 rev，保留更大的
                if (latestRev == null || compareRev(rev, latestRev) > 0) {
                    latestRev = rev;
                }
            }
            if (latestRev.equalsIgnoreCase(revision)) {
                //是最新冻结版本， 判断后续是否有更高发布版本
//                MapList releaseMapList = DomainObject.findObjects(context, JF_PLMConstants_mxJPO.TYPE_VPMReference, "*",
//                        "current=='RELEASED'" + "&&name=='"  + partName + "'", StringList.create(SELECT_ID, SELECT_REVISION));
                for (int i = 0; i < allMapList.size(); i++) {
                    Map map = (Map) allMapList.get(i);
                    String rev = UIUtil.getValue(map, SELECT_REVISION);
                    String current = UIUtil.getValue(map, SELECT_CURRENT);
                    if (!"RELEASED".equalsIgnoreCase(current)) {
                        continue;
                    }
                    // 比较 rev，保留更大的
                    if (compareRev(rev, latestRev) > 0) {
                        flag = 2;
                        break;
                    }
                }
            } else {
                flag = 1;
            }
        }catch (Exception e) {

        }
        return flag;
    }

    /**
     *  辅助方法：比较 "AA.1" 这类主版本
     * @param rev1
     * @param rev2
     * @author LIUJR
     * @throws
     * @return int
     * @date 2026/1/27 10:01
     * @description
     */
    private static int compareMainPart(String rev1, String rev2) {
        String[] p1 = rev1.split("\\.", 2);
        String[] p2 = rev2.split("\\.", 2);

        String pre1 = p1.length > 0 ? p1[0] : "";
        String pre2 = p2.length > 0 ? p2[0] : "";
        int cmp = pre1.compareTo(pre2);
        if (cmp != 0) {
            return cmp;
        }

        // 数字部分按数值比较
        if (p1.length == 2 && p2.length == 2) {
            try {
                int n1 = Integer.parseInt(p1[1]);
                int n2 = Integer.parseInt(p2[1]);
                return Integer.compare(n1, n2);
            } catch (NumberFormatException e) {
                // 回退到字符串比较
                return p1[1].compareTo(p2[1]);
            }
        }
        return rev1.compareTo(rev2);
    }

    /**
     * 排除Range包含空的情况
     * @param context
     * @param args
     * @author LIUJR
     * @throws
     * @return java.util.Map
     * @date 2025/12/23 15:01
     * @description
     */
    public Map getRangeRemoveBlank(Context context, String[] args) throws Exception {
        HashMap<Object, Object> resultMap = new HashMap<>();
        Map paramsMap = (Map) JPO.unpackArgs(args);
        Map columnMap = (Map) paramsMap.get(STRING_COLUMNMAP);
        String attrName = (String) columnMap.get(DomainConstants.SELECT_NAME);
        ContextUtil.pushContext(context);
        AttributeType attributeType = new AttributeType(attrName);
        attributeType.open(context);
        StringList choices = attributeType.getChoices();
        ContextUtil.popContext(context);
        for (int i = 0; i < choices.size(); i++) {
            if (UIUtil.isNullOrEmpty(choices.get(i))) {
                choices.remove(i);
            }
        }
        resultMap.put("field_choices", choices);
        resultMap.put("field_display_choices", choices);
        return resultMap;
    }

    /**
    * 辅助方法：比较两个 rev 字符串（AA.1-000 格式） 三段式
    * @param rev1
	* @param rev2
    * @author LIUJR
    * @throws
    * @return int
    * @date 2026/1/27 10:00
    * @description
    */
    private static int compareRev(String rev1, String rev2) {
        if (rev1 == null || rev2 == null) {
            return Boolean.compare(rev1 == null, rev2 == null);
        }
        // 按 "-" 分割：[ "AA.1", "000" ]
        String[] parts1 = rev1.split("-", 2);
        String[] parts2 = rev2.split("-", 2);

        String mainPart1 = parts1.length > 0 ? parts1[0] : "";
        String mainPart2 = parts2.length > 0 ? parts2[0] : "";
        String suffix1 = parts1.length > 1 ? parts1[1] : "000";
        String suffix2 = parts2.length > 1 ? parts2[1] : "000";

        // 1. 比较主部分（如 "AA.1" vs "AB.2"）
        int mainCmp = compareMainPart(mainPart1, mainPart2);
        if (mainCmp != 0) {
            return mainCmp;
        }

        // 2. 比较尾缀（数值比较）
        try {
            int s1 = Integer.parseInt(suffix1);
            int s2 = Integer.parseInt(suffix2);
            return Integer.compare(s1, s2);
        } catch (NumberFormatException e) {
            // 如果尾缀不是数字，回退到字符串比较（防御性）
            return suffix1.compareTo(suffix2);
        }
    }

    /**
    *
    *@description 优化获取xml数据
    *@param context
	*@param strTableName
    *@return com.matrixone.apps.domain.util.MapList
    *@throws
    *@author CHENYAN
    *@date 2026/2/4 11:01
    */
    public static MapList getFormalECRTableMapping(Context context,String strTableName) throws Exception{
        MapList mapList = new MapList();
        Page pageAttributePopulation = new Page("SignTaskProperties_zh.xml");
        pageAttributePopulation.open(context);
        String strProperties = pageAttributePopulation.getContents(context);
        pageAttributePopulation.close(context);
        // 使用 ByteArrayInputStream 将字符串转为输入流
        try (InputStream inputStream = new ByteArrayInputStream(strProperties.getBytes("UTF-8"))) {
            // 创建 DocumentBuilder
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(inputStream);
            // 获取所有 config 元素
            NodeList configurationNodes = document.getElementsByTagName("configuration");
            for (int i = 0; i < configurationNodes.getLength(); i++) {
                Element configurationElement = (Element) configurationNodes.item(i);
                if (strTableName.equals(configurationElement.getAttribute("id"))) {
                    // 找到匹配的 configuration, 获取它下的 config 元素
                    NodeList configNodes = configurationElement.getElementsByTagName("config");
                    for (int j = 0; j < configNodes.getLength(); j++) {
                        Element configElement = (Element) configNodes.item(j);
                        // 提取该 config 元素的所有属性
                        HashMap<String, String> attrMap = new HashMap<>();
                        NamedNodeMap attributes = configElement.getAttributes();
                        for (int k = 0; k < attributes.getLength(); k++) {
                            Node attr = attributes.item(k);
                            attrMap.put(attr.getNodeName(), attr.getNodeValue());
                        }
                        mapList.add(attrMap);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return mapList;
    }

    /**
     * 将MapList 进行特殊字段的分组 并组成Map返回
     *
     * @param context
     * @param mapList
     * @param groupAttr
     * @return java.util.Map
     * @throws
     * @author LIUJR
     * @date 2025/4/23 9:30
     * @description
     */
    public static Map getMapListGroupingMap(Context context, MapList mapList, String groupAttr) throws Exception {
        Map groupMap = (Map) mapList.stream().collect(Collectors.groupingBy(m -> {
            Map map = (Map) m;
            if (map.containsKey(groupAttr)) {
                return map.get(groupAttr);
            } else {
                return "null";
            }
        }));
        return groupMap;
    }

    /**
     * 获取项目关系上已发布的供货件
     **
     * @param context 上下文
     * @param projectObject 项目对象
     * @return MapList 已发布的项目供货件信息
     * @throws Exception
     * @author LIUJR
     * @date 2026/7/27
     */
    public static MapList getProjectReleasedZeroBelongPart(Context context, DomainObject projectObject) throws Exception {
        StringList partSelectList = JF_Util_mxJPO.basicBolistSel();
        partSelectList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_V_PART_NUMBER);
        StringList relSelectList = JF_Util_mxJPO.basicRellistSel();
        //20260727 update by ljr 公共查询统一限制供货件关系和发布状态，避免调用方重复拼接查询条件；
        return projectObject.getRelatedObjects(
                context,
                JF_PLMConstants_mxJPO.rel_JFProject2RootPart,
                JF_PLMConstants_mxJPO.TYPE_VPMReference,
                partSelectList,
                relSelectList,
                false,
                true,
                (short) 1,
                "current==RELEASED",
                JF_PLMConstants_mxJPO.SELECT_ATTR_JFZeroPart + "==Y && attribute[JF_BelongPart]==Y",
                (short) 0);
    }

    /**
     * 获取项目SDT成员的信息
     * @param context
     * @param projectId
     * @param select
     * @author LIUJR
     * @throws
     * @return matrix.util.StringList
     * @date 2025/2/19 14:30
     * @description
     */
    public static StringList getProjectSDTPersons(Context context, String projectId, String select) throws Exception{
        StringList allPersonList = new StringList();
        try {
            ContextUtil.pushContext(context);
            DomainObject objectProject = DomainObject.newInstance(context, projectId);
            MapList mapList = objectProject.getRelatedObjects(
                    context,
                    RELATIONSHIP_MEMBER,
                    TYPE_PERSON,
                    new StringList(select),
                    new StringList(),
                    false,
                    true,
                    (short) 1,
                    "",
                    "attribute[" + DomainConstants.ATTRIBUTE_PROJECT_ROLE  + "]!=''",
                    0
            );
            JF_LOGGER.info("!!!!!!!!!!!!!!malist:{}", mapList.toString());
            if (!mapList.isEmpty()) {
                allPersonList = (StringList) mapList.stream().map(m -> {
                    Map map = (Map) m;
                    return UIUtil.getValue(map, select);
                }).collect(Collectors.toCollection(SelectList::new));
            }
        } catch (FrameworkException e) {
            throw e;
        } finally {
            ContextUtil.popContext(context);
        }
        return allPersonList;
    }

    /**
     * 根据项目、文档类型和专业，获取项目专家组中匹配的专家人员ID。
     * @param context 系统上下文
     * @param projectId 项目ID
     * @param documentType 文档类型
     * @param documentSpecialty 文档专业
     * @return matrix.util.StringList 去重后的专家人员ID
     * @author LIUJR
     * @throws Exception 查询专家结构失败时抛出异常
     * @date 2026/8/5
     * @description
     */
    public static StringList getProjectDocumentExpertPersonIds(Context context, String projectId,
                                                                String documentType, String documentSpecialty) throws Exception {
        StringList expertPersonIdList = new StringList();
        if (UIUtil.isNullOrEmpty(projectId)
                || UIUtil.isNullOrEmpty(documentType)
                || UIUtil.isNullOrEmpty(documentSpecialty)) {
            return expertPersonIdList;
        }

        StringList objectSelectList = StringList.create(SELECT_ID, SELECT_TYPE, SELECT_LEVEL);
        StringList relationshipSelectList = StringList.create(
                "attribute[JF_ExpertsDocType]",
                "attribute[JF_ExpertsDocSpecialty]");

        // 一次展开“项目-专家组-人员”两层结构，匹配属性取专家组与人员之间的第二层关系。
        MapList expertStructureList = DomainObject.newInstance(context, projectId).getRelatedObjects(
                context,
                "JFProject2ExpertsGroup",
                JF_PLMConstants_mxJPO.TYPE_JFExpertsGroup + "," + TYPE_PERSON,
                objectSelectList,
                relationshipSelectList,
                false,
                true,
                (short) 2,
                EMPTY_STRING,
                EMPTY_STRING,
                0);

        // 同一人员可能存在于多个专家组中，使用有序Set避免生成重复的专家审批任务。
        Set<String> expertPersonIdSet = new LinkedHashSet<>();
        for (Object item : expertStructureList) {
            Map expertMap = (Map) item;
            if (!TYPE_PERSON.equals(UIUtil.getValue(expertMap, SELECT_TYPE))
                    || !"2".equals(UIUtil.getValue(expertMap, SELECT_LEVEL))) {
                continue;
            }

            boolean typeMatched = expertConfigurationContains(
                    expertMap.get("attribute[JF_ExpertsDocType]"), documentType);
            boolean specialtyMatched = expertConfigurationContains(
                    expertMap.get("attribute[JF_ExpertsDocSpecialty]"), documentSpecialty);
            if (typeMatched && specialtyMatched) {
                expertPersonIdSet.add(UIUtil.getValue(expertMap, SELECT_ID));
            }
        }
        expertPersonIdList.addAll(expertPersonIdSet);
        return expertPersonIdList;
    }

    /**
     * 精确判断单值、逗号分隔值或多值关系属性中是否包含指定配置值。
     * @param configurationValue 关系属性查询结果
     * @param expectedValue 待匹配值
     * @return boolean 是否精确包含待匹配值
     * @author LIUJR
     * @date 2026/8/5
     */
    private static boolean expertConfigurationContains(Object configurationValue, String expectedValue) {
        if (configurationValue == null || UIUtil.isNullOrEmpty(expectedValue)) {
            return false;
        }
        if (configurationValue instanceof Collection) {
            for (Object item : (Collection) configurationValue) {
                if (expertConfigurationContains(item, expectedValue)) {
                    return true;
                }
            }
            return false;
        }

        String value = String.valueOf(configurationValue).trim();
        if (value.startsWith("[") && value.endsWith("]")) {
            value = value.substring(1, value.length() - 1);
        }
        StringList configuredValueList = FrameworkUtil.split(value, ",");
        for (String configuredValue : configuredValueList) {
            if (expectedValue.equals(configuredValue.trim())) {
                return true;
            }
        }
        return false;
    }
}
