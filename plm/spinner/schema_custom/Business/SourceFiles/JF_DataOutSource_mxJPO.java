import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.dscn.plm.util.NioJDUtils;
import com.google.gson.Gson;
import com.matrixone.apps.common.CommonDocument;
import com.matrixone.apps.common.Department;
import com.matrixone.apps.common.util.ComponentsUtil;
import com.matrixone.apps.common.util.DocumentUtil;
import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.DomainRelationship;
import com.matrixone.apps.domain.Job;
import com.matrixone.apps.domain.util.*;
import com.matrixone.apps.framework.ui.ProgramCallable;
import com.matrixone.apps.framework.ui.UICache;
import com.matrixone.apps.framework.ui.UIUtil;
import com.matrixone.apps.program.ProgramCentralConstants;
import jakarta.mail.BodyPart;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMultipart;
import matrix.db.*;
import matrix.util.StringList;
import org.apache.commons.text.StringEscapeUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.io.File;
import java.util.*;
import java.util.Set;
import java.util.stream.Collectors;

import static com.matrixone.apps.domain.DomainConstants.*;
import static com.matrixone.apps.domain.DomainConstants.SELECT_ID;

/**
 * @ClassName JF_DataOutSource_mxJPO
 * @Author: LIUJR
 * @CreateDate: 2024/7/8 15:59
 * @UpdateRemark:
 * @Version: 1.0
 * @Description:
 */
public class JF_DataOutSource_mxJPO implements JF_PLMConstants_mxJPO{
    private static final Logger JF_LOGGER = LoggerFactory.getLogger(JF_DataOutSource_mxJPO.class);
    private static final String TYPE_JF_DATASOURCE = "JFDataOutSource";
    private static final String TYPE_JF_DATASOURCE_LOGS = "JFDataOutSourceLogs";
    private static final String TYPE_JF_VPMREFERENCE = "VPMReference";
    private static final String TYPE_JF_PART = "Part";
    private static final String TYPE_JF_DRAWING = "Drawing";
    private static final String POLICY_JF_DATASOURCE = "policy_JFDataOutSource";
    private static final String STATE_POLICY_JF_DATASOURCE_IN_WORK = "state_In_Work";
    private static final String STATE_POLICY_JF_DATASOURCE_APPROVE = "state_Approve";
    private static final String STATE_POLICY_JF_DATASOURCE_COMPLETE = "state_Complete";
    private static final String STATE_POLICY_JF_DATASOURCE_OUTSOURCE_COMPLETE = "state_OutsourceComplete";
    private static final String ATTRIBUTE_JF_CONNECT_PROJECT = "JSConnectProject";
    private static final String ATTRIBUTE_JF_APPROVE_PERSON = "JSApprovePerson";
    private static final String ATTRIBUTE_JF_DATA_PROCESS_PROGRESS = "JSdataProcessProgress";
    private static final String RANGE_PROCESS_PROGRESS_ENOVIAPROCESS = "EnoviaProcess";  //Enovia内部处理
    private static final String RANGE_PROCESS_PROGRESS_PENDINGCAAPROCESS = "PendingCAAProcess";  //待CAA处理
    private static final String RANGE_PROCESS_PROGRESS_CURRENTCAAPROCESS = "CurrentCAAProcess";  //CAA正在处理
    private static final String RANGE_PROCESS_PROGRESS_CAAPROCESSINGCOMPLETE = "CAAProcessingComplete";  //CAA处理完成
    private static final String RANGE_PROCESS_PROGRESS_OUTSOURCEDATACOMPLETE = "OutsourceDataComplete";  //外发数据完成
    private static final String RANGE_PROCESS_PROGRESS_CAAPROCEFAILD = "CAAProcessingFailed";  //CAA处理失败
    private static final String RANGE_PROCESS_PROGRESS_SEND_EMAIL = "SentEmail";  //外发数据完成
    private static final String ATTRIBUTE_JF_TOPIC = "JSTopic";
    private static final String ATTRIBUTE_JF_WATERMARK = "JSWaterMark";
    private static final String RANGE_ATTRIBUTE_JF_WATERMARK_Y = "Y";
    private static final String RANGE_ATTRIBUTE_JF_WATERMARK_N = "N";
    private static final String ATTRIBUTE_JF_OUTSOURCE_REV = "JSOutSourceRev";
    private static final String ATTRIBUTE_JF_LINE_MANAGER = "JF_Line_Manager";
    private static final String ATTRIBUTE_JF_CODE = "JSCode";
    private static final String ATTRIBUTE_JF_SECRET = "JSSecret";
    private static final String ATTRIBUTE_JF_ADDRESS = "JSAddressee";
    private static final String ATTRIBUTE_EMAIL_ADDRESS = "Email Address";
    private static final String ATTRIBUTE_PART_CATIA_REVISION = "XCADExtension.V_CADOrigin";
    private static final String ATTRIBUTE_PART_DRAWING_REVISION = "XCADRepExtension.V_CADFileOrigin";
    private static final String ATTRIBUTE_V_PART_NUMBER = "EnterpriseExtension.V_PartNumber";
    public static final String ATTRIBUTE_JF_ISXPDM = "JF_VPMReference.JF_ISXPDM";
    String ATTRIBUTE_PART_TYPE = "JF_VPMReference.JF_PartType";
    private static final String ATTRIBUTE_PART_NAME_EN = "JF_VPMReference.JF_PartNameEN";
    private static final String ATTRIBUTE_PART_NAME_CN = "JF_VPMReference.JF_PartNameCN";
    private static final String ATTRIBUTE_PART_MATERIAL = "JF_VPMReference.JF_Material";
    private static final String ATTRIBUTE_PART_ENTITY_NAME = "PLMEntity.V_Name";
    private static final String ATTRIBUTE_FLEXIBLE_PART = "JF_VPMReference.JF_FlexiblePart";
    private static final String ATTRIBUTE_PART_ENTITY_USAGE = "PLMEntity.V_usage";
    private static final String REL_INSTANCE_ATTR_EXTERNAL_ID = "PLMInstance.PLM_ExternalID";
    private static final String REL_INSTANCE_ATTR_V_TREE_ORDER = "PLMInstance.V_TreeOrder";
    private static final String REL_INSTANCE_ATTR_V_MATRIX1 = "LPAbstractInstance.V_matrix_1";
    private static final String REL_INSTANCE_ATTR_V_MATRIX2 = "LPAbstractInstance.V_matrix_2";
    private static final String REL_INSTANCE_ATTR_V_MATRIX3 = "LPAbstractInstance.V_matrix_3";
    private static final String REL_INSTANCE_ATTR_V_MATRIX4 = "LPAbstractInstance.V_matrix_4";
    private static final String REL_INSTANCE_ATTR_V_MATRIX5 = "LPAbstractInstance.V_matrix_5";
    private static final String REL_INSTANCE_ATTR_V_MATRIX6 = "LPAbstractInstance.V_matrix_6";
    private static final String REL_INSTANCE_ATTR_V_MATRIX7 = "LPAbstractInstance.V_matrix_7";
    private static final String REL_INSTANCE_ATTR_V_MATRIX8 = "LPAbstractInstance.V_matrix_8";
    private static final String REL_INSTANCE_ATTR_V_MATRIX9 = "LPAbstractInstance.V_matrix_9";
    private static final String REL_INSTANCE_ATTR_V_MATRIX10 = "LPAbstractInstance.V_matrix_10";
    private static final String REL_INSTANCE_ATTR_V_MATRIX11 = "LPAbstractInstance.V_matrix_11";
    private static final String REL_INSTANCE_ATTR_V_MATRIX12 = "LPAbstractInstance.V_matrix_12";
    private static final String RELATIONSHIP_JS_DATASOURCE_PERSON = "JFDataOut2Person";
    private static final String RELATIONSHIP_JS_DATASOURCE_LOGS = "JFDataOut2DataLog";
    private static final String RELATIONSHIP_JS_DATASOURCE_CONTENT = "Reference Document";
    private static final String RELATIONSHIP_INSTANCE = "VPMInstance";
    private static final String RELATIONSHIP_VPM_REP_INSTANCE = "VPMRepInstance";
    private static final String RELATIONSHIP_CAD_ASSEMBLY_REP_INSTANCE = "XCADAssemblyRepInstance";
    private static final String SUITE_KEY = "emxComponentsStringResource";
    private static final String MAP_COLATTR = "colAttrMap";
    private static final String STRING_APPROVER = "Approver";
    private static final String STRING_APPROVER1 = "Approver1";
    private static final String STRING_MQL_ATTRIBUTE = "attribute[%s].value";
    private static final String STRING_MQL_RELATIONSHIP_FROM = "from[%s].to.%s";
    private static final String STRING_MQL_RELATIONSHIP_TO = "to[%s].from.%s";
    private static final String STRING_CATIA_V5 = "CATIAV5";
    private static final String STRING_OTHER_DOC = "OtherDoc";
    private static final String STRING_SEND_DATA = "SendData";
    private static final String STRING_DOWNLOAD_V5_FILE = "DownloadV5File";
    private static final String STRING_FILE_SHARE_PATH = "dataOutSource.share.path";
    private static final String STRING_DEPARTMENT_ID = "JF_RDCenterDepartment.id";
    private static final StringList DATA_SOURCE_TYPE_ALL = new StringList();
    private static final StringList strBusSelectsList = new StringList();
    private static final StringList strPartSelectList = new StringList();
    private static final StringList strDrawingSelectList = new StringList();
    private static final StringList strRelSelectList = new StringList();


    static {
        //基础对象查询集
        strBusSelectsList.add(DomainConstants.SELECT_ID);
        strBusSelectsList.add(DomainConstants.SELECT_NAME);
        strBusSelectsList.add(DomainConstants.SELECT_OWNER);
        strBusSelectsList.add(DomainConstants.SELECT_TYPE);
        //数据外发申请单的添加内容类型
        DATA_SOURCE_TYPE_ALL.add(TYPE_JF_VPMREFERENCE);
        DATA_SOURCE_TYPE_ALL.add(DomainConstants.TYPE_DOCUMENT);
        DATA_SOURCE_TYPE_ALL.add(TYPE_JF_DRAWING);
        //数模对象查询集
        strPartSelectList.add(DomainConstants.SELECT_NAME);
        strPartSelectList.add(DomainConstants.SELECT_ID);
        strPartSelectList.add(DomainConstants.SELECT_TYPE);
        strPartSelectList.add(DomainConstants.SELECT_REVISION);
        strPartSelectList.add(DomainConstants.SELECT_ATTRIBUTE_TITLE);
        strPartSelectList.add(String.format(STRING_MQL_ATTRIBUTE, ATTRIBUTE_V_PART_NUMBER));
        strPartSelectList.add(String.format(STRING_MQL_ATTRIBUTE, ATTRIBUTE_PART_CATIA_REVISION));
        strPartSelectList.add(String.format(STRING_MQL_ATTRIBUTE, ATTRIBUTE_PART_NAME_EN));
        strPartSelectList.add(String.format(STRING_MQL_ATTRIBUTE, ATTRIBUTE_PART_NAME_CN));
        strPartSelectList.add(String.format(STRING_MQL_ATTRIBUTE, ATTRIBUTE_PART_MATERIAL));
        strPartSelectList.add(String.format(STRING_MQL_ATTRIBUTE, ATTRIBUTE_PART_ENTITY_NAME));
        strPartSelectList.add(String.format(STRING_MQL_ATTRIBUTE, ATTRIBUTE_FLEXIBLE_PART));
        strPartSelectList.add("physicalid");
        //数模关联的关系属性集
        strRelSelectList.add(String.format(STRING_MQL_ATTRIBUTE, REL_INSTANCE_ATTR_EXTERNAL_ID));
        strRelSelectList.add(String.format(STRING_MQL_ATTRIBUTE, REL_INSTANCE_ATTR_V_TREE_ORDER));
        strRelSelectList.add(String.format(STRING_MQL_ATTRIBUTE, REL_INSTANCE_ATTR_V_MATRIX1));
        strRelSelectList.add(String.format(STRING_MQL_ATTRIBUTE, REL_INSTANCE_ATTR_V_MATRIX2));
        strRelSelectList.add(String.format(STRING_MQL_ATTRIBUTE, REL_INSTANCE_ATTR_V_MATRIX3));
        strRelSelectList.add(String.format(STRING_MQL_ATTRIBUTE, REL_INSTANCE_ATTR_V_MATRIX4));
        strRelSelectList.add(String.format(STRING_MQL_ATTRIBUTE, REL_INSTANCE_ATTR_V_MATRIX5));
        strRelSelectList.add(String.format(STRING_MQL_ATTRIBUTE, REL_INSTANCE_ATTR_V_MATRIX6));
        strRelSelectList.add(String.format(STRING_MQL_ATTRIBUTE, REL_INSTANCE_ATTR_V_MATRIX7));
        strRelSelectList.add(String.format(STRING_MQL_ATTRIBUTE, REL_INSTANCE_ATTR_V_MATRIX8));
        strRelSelectList.add(String.format(STRING_MQL_ATTRIBUTE, REL_INSTANCE_ATTR_V_MATRIX9));
        strRelSelectList.add(String.format(STRING_MQL_ATTRIBUTE, REL_INSTANCE_ATTR_V_MATRIX10));
        strRelSelectList.add(String.format(STRING_MQL_ATTRIBUTE, REL_INSTANCE_ATTR_V_MATRIX11));
        strRelSelectList.add(String.format(STRING_MQL_ATTRIBUTE, REL_INSTANCE_ATTR_V_MATRIX12));
        //图纸对象结果集
        strDrawingSelectList.add(DomainConstants.SELECT_NAME);
        strDrawingSelectList.add(DomainConstants.SELECT_ID);
        strDrawingSelectList.add(DomainConstants.SELECT_TYPE);
        strDrawingSelectList.add(DomainConstants.SELECT_REVISION);
        strDrawingSelectList.add(String.format(STRING_MQL_ATTRIBUTE, ATTRIBUTE_PART_DRAWING_REVISION));
        strDrawingSelectList.add(String.format(STRING_MQL_ATTRIBUTE, ATTRIBUTE_PART_ENTITY_NAME));
        //需要对属性
        strDrawingSelectList.add(String.format(STRING_MQL_ATTRIBUTE, ATTRIBUTE_PART_NAME_EN));
        strDrawingSelectList.add(String.format(STRING_MQL_ATTRIBUTE, ATTRIBUTE_PART_NAME_CN));
    }

    /**
    * 获取当前研发人员的数据外发申请单
    * @description
    * @param context
	* @param args
    * @author LIUJR
    * @throws
    * @return void
    * @date 2024/7/8 16:00
    */
    public MapList getAllJFDataOutSource(Context context, String[] args)  throws Exception {
        MapList mlResult = new MapList();
        try {
            String loginUserName = context.getUser();
            StringBuilder strWhereBuilder = new StringBuilder();
            strWhereBuilder.append(DomainConstants.SELECT_OWNER)
                           .append(STRING_SYMB_EQUAL)
                           .append(STRING_SYMB_QUOTE)
                           .append(loginUserName)
                           .append(STRING_SYMB_QUOTE);
            String strWhere = strWhereBuilder.toString();
            ContextUtil.pushContext(context);
            mlResult = DomainObject.findObjects(context, TYPE_JF_DATASOURCE, DomainConstants.QUERY_WILDCARD, strWhere, strBusSelectsList);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        } finally {
            ContextUtil.popContext(context);
        }
        return mlResult;
    }

    /**
    * 创建数据外发申请单 设置属性 关联关系
    * @description
    * @param context
	* @param args
    * @author LIUJR
    * @throws
    * @return void
    * @date 2024/7/9 13:03
    */
    public void createJFDataOutSourceApply(Context context, String[] args) throws Exception {
        try {
            JF_LOGGER.info("method:createJFDataOutSourceApply start...");
            Map paramsMap = (Map) JPO.unpackArgs(args);
            Map requestMap = (Map) paramsMap.get(STRING_REQUESTMAP);
            Map paramMap = (Map) paramsMap.get(STRING_PARAMMAP);
            String strJSConnectProjectOID = (String) requestMap.get("JSConnectProjectOID");
            String strJSApprovePersonOID = (String) requestMap.get("JSApprovePersonOID");
            String strApprovePersonOID = (String) requestMap.get("ApprovePersonOID");
            String strJSApprovePersonValue = (String) requestMap.get("JSApprovePersonDisplay");
            String objectId = (String) paramMap.get(STRING_OBJECTID);
            if (UIUtil.isNullOrEmpty(objectId)) {
                objectId = (String) paramMap.get("newObjectId");
            }
            DomainObject objectJFDataOutSource = DomainObject.newInstance(context, objectId);
            HashMap<String, String> attributeMap = new HashMap<>();
            //重新设置属性 将关联项目属性改为id
            attributeMap.put(ATTRIBUTE_JF_CONNECT_PROJECT, strJSConnectProjectOID);
            //管理审批人员
            if (UIUtil.isNotNullAndNotEmpty(strJSApprovePersonOID)) {
                DomainRelationship.connect(context, objectJFDataOutSource, new RelationshipType(RELATIONSHIP_JS_DATASOURCE_PERSON), true, strJSApprovePersonOID.split("\\|"));
                //清空Title属性 为人的全名
                attributeMap.put(ATTRIBUTE_JF_APPROVE_PERSON, strJSApprovePersonValue.replaceAll("\\|", ","));
            }
            if (UIUtil.isNotNullAndNotEmpty(strApprovePersonOID)) {
                DomainRelationship.connect(context, objectJFDataOutSource, new RelationshipType("JFApprove2Person"), true, strApprovePersonOID.split("\\|"));
            }
            objectJFDataOutSource.setAttributeValues(context, attributeMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
        JF_LOGGER.info("method:createJFDataOutSourceApply end...");
    }


    /**
    * 添加"审批人"必选项,审批人由申请单Owner手段选择,审批人自动进行过滤,人员搜索界面过滤逻辑如下:
     * 直线经理:通过LineManager属性过滤;
     * 部门经理:根据申请单Owner获取部门信息,并获取部门经理
     * 整椅经理:根据"关联项目"获取SDT中整椅经理
    * @param context
	* @param args
    * @author LIUJR
    * @throws
    * @return matrix.util.StringList
    * @date 2025/4/18 13:59
    * @description
    */
    @ProgramCallable
    public static StringList getDataSourceApprovePersons(Context context, String[] args) throws Exception{
        JF_LOGGER.info("==============================getSystemAllProjectSpaces==============================");
        StringList strReturnList = new StringList();
        try {
            Map map = (Map) JPO.unpackArgs(args);
            JF_LOGGER.info("map:{}", map.toString());
            String strUser = context.getUser().toString();
            String objectId = (String) map.get("objectId");

            //直线经理id
            String strLineManagerId = JF_PublicMethodClass_mxJPO.getPersonLineManager(context, null, strUser);
            if (UIUtil.isNotNullAndNotEmpty(strLineManagerId)) {
                strReturnList.add(strLineManagerId);
            }
            DomainObject domainObject = DomainObject.newInstance(context);
            String projectId = EMPTY_STRING;
            if (UIUtil.isNotNullAndNotEmpty(objectId)) {
                //已经打开页面，能拿到objectId ，需要判断类型，可能是EBOM中发起的
                domainObject.setId(objectId);
                if (TYPE_JF_DATASOURCE.equalsIgnoreCase(domainObject.getInfo(context, SELECT_TYPE))) {
                    projectId = domainObject.getAttributeValue(context, ATTRIBUTE_JF_CONNECT_PROJECT);
                } else {
                    //
                    if (map.containsKey("projectId")) {
                        projectId = (String) map.get("projectId");
                    }
                }
            } else {
                //未创建
                if (map.containsKey("projectId")) {
                    projectId = (String) map.get("projectId");
                }
            }
            if (UIUtil.isNotNullAndNotEmpty(projectId)) {
                StringList typeSelectList = new StringList(SELECT_ID);
                typeSelectList.add(SELECT_NAME);
                StringList reSelectList = new StringList(SELECT_RELATIONSHIP_ID);
                JF_LOGGER.info("personId:{}", projectId);
                //获取据"关联项目"获取SDT中整椅经理
                MapList chairManagerMapList = JF_PublicMethodClass_mxJPO.getProjectPersonByRoleName(context, projectId, "Chair manager", typeSelectList, reSelectList);
                JF_LOGGER.info("chairManagerMapList:{}", chairManagerMapList.toString());
                if (!chairManagerMapList.isEmpty()) {
                    Map chairManagerMap = (Map) chairManagerMapList.get(0);
                    String personId = UIUtil.getValue(chairManagerMap, SELECT_ID);
                    JF_LOGGER.info("personId:{}", personId);
                    strReturnList.add(personId);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
        JF_LOGGER.info("strReturnList.length：{}", strReturnList.size());

        return strReturnList;
    }


    /**
     * BOM编辑页面  创建数据外发申请单 设置属性 关联关系
     * @description
     * @param context
     * @param args
     * @author LIUJR
     * @throws
     * @return void
     * @date 2024/7/9 13:03
     */
    public void createJFDataOutSourceApplyAddPart(Context context, String[] args) throws Exception {
        try {
            JF_LOGGER.info("=========================method:createJFDataOutSourceApplyAddPart start...");
            Map paramsMap = (Map) JPO.unpackArgs(args);
            JF_LOGGER.info("args：{}", paramsMap.toString());
            Map requestMap = (Map) paramsMap.get(STRING_REQUESTMAP);
            Map paramMap = (Map) paramsMap.get(STRING_PARAMMAP);
            String strJSConnectProjectOID = (String) requestMap.get("JSConnectProjectOID");
            String strJSApprovePersonOID = (String) requestMap.get("JSApprovePersonOID");
            String strApprovePersonOID = (String) requestMap.get("ApprovePersonOID");
            String strJSApprovePersonValue = (String) requestMap.get("JSApprovePersonDisplay");
            String objectId = (String) paramMap.get(STRING_OBJECTID);
            if (UIUtil.isNullOrEmpty(objectId)) {
                objectId = (String) paramMap.get("newObjectId");
            }
            DomainObject objectJFDataOutSource = DomainObject.newInstance(context, objectId);
            HashMap<String, String> attributeMap = new HashMap<>();
            //重新设置属性 将关联项目属性改为id
            attributeMap.put(ATTRIBUTE_JF_CONNECT_PROJECT, strJSConnectProjectOID);
            //管理审批人员
            if (UIUtil.isNotNullAndNotEmpty(strJSApprovePersonOID)) {
                DomainRelationship.connect(context, objectJFDataOutSource, new RelationshipType(RELATIONSHIP_JS_DATASOURCE_PERSON), true, strJSApprovePersonOID.split("\\|"));
                //清空Title属性 为人的全名
                attributeMap.put(ATTRIBUTE_JF_APPROVE_PERSON, strJSApprovePersonValue.replaceAll("\\|", ","));
            }
            if (UIUtil.isNotNullAndNotEmpty(strApprovePersonOID)) {
                DomainRelationship.connect(context, objectJFDataOutSource, new RelationshipType("JFApprove2Person"), true, strApprovePersonOID.split("\\|"));
            }
            objectJFDataOutSource.setAttributeValues(context, attributeMap);
            //关联外发内容
            String emxTableRowIds = UIUtil.getValue(requestMap, "emxTableRowIds");
            emxTableRowIds = emxTableRowIds.replaceAll(":BSF", "BSF");

            HashSet<String> strings = new HashSet<>();
            Arrays.stream(emxTableRowIds.split(":")).forEach(strRowIds -> {
                String[] split = strRowIds.split("\\|");
                if (UIUtil.isNotNullAndNotEmpty(split[1])) {
                    strings.add(split[1]);
                }
            });
            StringList choosePartIds = StringList.create(strings);
            if (!choosePartIds.isEmpty()) {
                objectJFDataOutSource.addRelatedObjects(context, new RelationshipType(DomainRelationship.RELATIONSHIP_REFERENCE_DOCUMENT), true, choosePartIds.toStringArray());
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
        JF_LOGGER.info("method:createJFDataOutSourceApplyAddPart end...");
    }


    /**
     * 修改数据外发申请单 设置属性 关联关系
     * @description
     * @param context
     * @param args
     * @author LIUJR
     * @throws
     * @return void
     * @date 2024/7/9 13:03
     */
    public void editJFDataOutSourceApply(Context context, String[] args) throws Exception {
        JF_LOGGER.info("method:editJFDataOutSourceApply start...");
        try {
            Map paramsMap = (Map) JPO.unpackArgs(args);
            Map requestMap = (Map) paramsMap.get(STRING_REQUESTMAP);
            Map paramMap = (Map) paramsMap.get(STRING_PARAMMAP);
            String strJSApprovePersonOID = (String) requestMap.get("JSApprovePersonOID");
            String strApprovePersonOID = (String) requestMap.get("ApprovePersonOID");
            String strJSApprovePersonDisplay = (String) requestMap.get("JSApprovePersonDisplay");
            String strJSConnectProjectOID = (String) requestMap.get("JSConnectProjectOID");

            String objectId = (String) requestMap.get(STRING_OBJECTID);
            if (UIUtil.isNullOrEmpty(objectId)) {
                objectId = (String) paramMap.get(STRING_PARENTOID);
            }
            DomainObject objectJFDataOutSource = DomainObject.newInstance(context, objectId);
            HashMap<String, String> attributeMap = new HashMap<>();
            //重新设置属性 将关联项目属性改为id
            attributeMap.put(ATTRIBUTE_JF_CONNECT_PROJECT, strJSConnectProjectOID);
            //重新设置属性
            //管理审批人员
            if (UIUtil.isNotNullAndNotEmpty(strJSApprovePersonOID)) {
                String[] split = strJSApprovePersonOID.split("\\|");
                StringList allApprovePerson = objectJFDataOutSource.getInfoList(context, "from[" + RELATIONSHIP_JS_DATASOURCE_PERSON + "].id");
                String[] persons = allApprovePerson.toStringArray();
                //去掉原来的审批人
                DomainRelationship.disconnect(context, persons);
                DomainRelationship.connect(context, objectJFDataOutSource, new RelationshipType(RELATIONSHIP_JS_DATASOURCE_PERSON), true, strJSApprovePersonOID.split("\\|"));
                attributeMap.put(ATTRIBUTE_JF_APPROVE_PERSON, strJSApprovePersonDisplay.replaceAll("\\|", ","));
            }
            if (UIUtil.isNotNullAndNotEmpty(strApprovePersonOID)) {
                String[] split = strJSApprovePersonOID.split("\\|");
                StringList allApprovePerson = objectJFDataOutSource.getInfoList(context, "from[JFApprove2Person].id");
                String[] persons = allApprovePerson.toStringArray();
                //去掉原来的审批人
                DomainRelationship.disconnect(context, persons);
                DomainRelationship.connect(context, objectJFDataOutSource, new RelationshipType("JFApprove2Person"), true, strApprovePersonOID.split("\\|"));
            }
            objectJFDataOutSource.setAttributeValues(context, attributeMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
        JF_LOGGER.info("method:editJFDataOutSourceApply end...");
    }

    /**
    * 数据外发项目
    * @param context
	* @param args
    * @author LIUJR
    * @throws
    * @return java.lang.String
    * @date 2025/4/10 14:18
    * @description
    */
    public String getDataOutFormProjectFieldValue(Context context, String[] args) throws Exception {
        Map formSettingMap = JPO.unpackArgs(args);
        String strHtml = EMPTY_STRING;
        Map fieldMap = (Map) formSettingMap.get("fieldMap");
        String strFieldName = (String) fieldMap.get("name");
        Map requestMap = (Map) formSettingMap.get("requestMap");
        String strMode = (String) requestMap.get("mode");
        String objectId = (String) requestMap.get("objectId");
        DomainObject domainObject = DomainObject.newInstance(context);
        domainObject.setId(objectId);
        String connectProject = domainObject.getAttributeValue(context, ATTRIBUTE_JF_CONNECT_PROJECT);
        if (UIUtil.isNullOrEmpty(connectProject)) {
            return strHtml;
        }
        try {
            ContextUtil.pushContext(context);
            domainObject.setId(connectProject);
            String description = domainObject.getDescription(context);
            if ("view".equalsIgnoreCase(strMode)) {
                strHtml = description;
            } else if ("edit".equalsIgnoreCase(strMode)) {
                strHtml = JF_PublicMethodClass_mxJPO.buildFieldHtml(
                        context,
                        strMode,
                        "JSConnectProject",
                        connectProject,
                        description,
                        "../common/emxFullSearch.jsp?field=TYPES=type_ProjectSpace&cancelLabel=emxFramework.Common.Close&HelpMarker=emxhelpselectorganization&includeOIDprogram=JF_PublicMethodClass:getSystemAllProjectSpaces&table=AEFGeneralSearchResults&&selection=single&showInitialResults=true&submitURL=./JF_AEFSearchUtil.jsp",
                        true);
            }
        }finally {
            ContextUtil.popContext(context);
        }
        return strHtml;
    }

    /**
     * 数据外发项目
     * @param context
     * @param args
     * @author LIUJR
     * @throws
     * @return java.lang.String
     * @date 2025/4/10 14:18
     * @description
     */
    public String getECRSnapshotFieldValue(Context context, String[] args) throws Exception {
        Map formSettingMap = JPO.unpackArgs(args);
        String strHtml = EMPTY_STRING;
        try {
            JF_LOGGER.info("=========================================");
            JF_LOGGER.info("getECRSnapshotFieldValue........................");
            JF_LOGGER.info("formSettingMap:{}", formSettingMap.toString());
            Map requestMap = (Map) formSettingMap.get("requestMap");
            String parentOID = (String) requestMap.get("parentOID");   //快照/ECRid
            String typeName = EMPTY_STRING;
            if (UIUtil.isNotNullAndNotEmpty(parentOID)) {
                DomainObject domainObject = DomainObject.newInstance(context, parentOID);
                typeName = domainObject.getTypeName(context);
            }
            if (UIUtil.isNullOrEmpty(parentOID) || typeName.equalsIgnoreCase(JF_PLMConstants_mxJPO.TYPE_VPMReference)) {
                strHtml = JF_PublicMethodClass_mxJPO.buildFieldJSHtml(
                        context,
                        "create",
                        "JSConnectProject",
                        "",
                        "",
                        "",
                        true,
                        "viewProjectSpaceDesc"
                );
            } else {
                DomainObject domainObject = DomainObject.newInstance(context);
                domainObject.setId(parentOID);
                String type = domainObject.getInfo(context, SELECT_TYPE);
                String projectId = EMPTY_STRING;
                if (JF_PLMConstants_mxJPO.TYPE_JFNewECR.equalsIgnoreCase(type) || "JFFormalECR".equalsIgnoreCase(type) || JF_PLMConstants_mxJPO.TYPE_JFDR.equalsIgnoreCase(type)) {
                    projectId = domainObject.getInfo(context, "from[JFChange2Project].to.id");
                } else if (JF_PLMConstants_mxJPO.TYPE_JFSnapshot.equalsIgnoreCase(type)) {
                    projectId = domainObject.getInfo(context, "to[JFProject2Snapshot].from.id");
                }
                if (UIUtil.isNullOrEmpty(projectId)) {
                    strHtml = "";
                } else {
                    try {
                        ContextUtil.pushContext(context);
                        domainObject.setId(projectId);
                        String description = domainObject.getDescription(context);
                        strHtml = JF_PublicMethodClass_mxJPO.buildFieldAutoHtml(
                                context,
                                "edit",
                                "JSConnectProject",
                                projectId,
                                description,
                                "",
                                true,
                                false,
                                true);
                    } finally {
                        ContextUtil.popContext(context);
                    }
                }
            }
            return strHtml;
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
    * 数据外发标题  ECR快照的时候需要自动拿取流水码
    * @param context
	* @param args
    * @author LIUJR
    * @throws
    * @return java.lang.String
    * @date 2025/9/16 15:26
    * @description
    */
    public String getDataResourceTitleValue(Context context, String[] args) throws Exception {
        Map formSettingMap = JPO.unpackArgs(args);
        String strHtml = EMPTY_STRING;
        try {
            JF_LOGGER.info("=========================================");
            JF_LOGGER.info("getDataResourceTitleValue........................");
            Map requestMap = (Map) formSettingMap.get("requestMap");
            String parentOID = (String) requestMap.get("parentOID");   //快照/ECRid
            String typeName = EMPTY_STRING;
            if (UIUtil.isNotNullAndNotEmpty(parentOID)) {
                DomainObject domainObject = DomainObject.newInstance(context, parentOID);
                typeName = domainObject.getTypeName(context);
            }
            if (UIUtil.isNullOrEmpty(parentOID) || JF_PLMConstants_mxJPO.TYPE_VPMReference.equalsIgnoreCase(typeName) || JF_PLMConstants_mxJPO.TYPE_JFDR.equalsIgnoreCase(typeName)) {
                strHtml = "";
            } else {
                DomainObject domainObject = DomainObject.newInstance(context);
                domainObject.setId(parentOID);
                strHtml = domainObject.getInfo(context, SELECT_NAME);
            }
//            JF_LOGGER.info("-------value:{}", EnoviaResourceBundle.getRangeI18NString(context, "JSdataProcessProgress", "CAAProcessingFailed", "zh"));
            return strHtml;
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * 数据外发审批人  需要使用js
     * @param context
     * @param args
     * @author LIUJR
     * @throws
     * @return java.lang.String
     * @date 2025/4/10 14:18
     * @description
     */
    public String getDataOutFormApproveFieldValue(Context context, String[] args) throws Exception {
        Map formSettingMap = JPO.unpackArgs(args);
        Map requestMap = (Map) formSettingMap.get("requestMap");
        String viewId = EMPTY_STRING;
        String viewName = EMPTY_STRING;
        if (requestMap.containsKey("mode"))  {
            //显示和创建
            String strMode = (String) requestMap.get("mode");
            String objectId = (String) requestMap.get("objectId");
            DomainObject domainObject = DomainObject.newInstance(context);
            domainObject.setId(objectId);
            String approveId = domainObject.getInfo(context, "from[JFApprove2Person].to.id");
            if (UIUtil.isNullOrEmpty(approveId)) {
                approveId = EMPTY_STRING;
            } else {
                viewId = approveId;
                viewName = JF_PublicMethodClass_mxJPO.getPersonAllName(context, approveId);
            }
            if ("view".equalsIgnoreCase(strMode)) {
                return viewName;
            }
        }
        String strHtml = JF_PublicMethodClass_mxJPO.buildFieldJSHtml(
                context,
                "create",
                "ApprovePerson",
                viewId,
                viewName,
                "${COMMON_DIR}/emxFullSearch.jsp?field=TYPES=type_Person&table=AEFGeneralSearchResults&selection=single&includeOIDprogram=JF_DataOutSource:getDataSourceApprovePersons&submitURL=./JF_AEFSearchUtil.jsp",
                true,
                "addJSApprovePerson"
                );
        return strHtml;
    }

    /**
    * 当table中的lie是显示关系的时候， 根据Table中的列名称，去拿取对象关系，显示信息
    * @param context
	* @param args
    * @author LIUJR
    * @throws
    * @return java.util.Vector
    * @date 2024/7/10 13:32
    * @description
    */
    public Vector getTableObjectRelationshipInfo(Context context, String[] args) throws Exception{
        JF_LOGGER.info("method:getTableObjectRelationshipInfo start...");
        Vector vector = new Vector();
        try {
            Map paramsMap = (Map) JPO.unpackArgs(args);
            Map columnMap = (Map) paramsMap.get(STRING_COLUMNMAP);
            Map colAttrMap = (Map) columnMap.get(MAP_COLATTR);
            String strAttrName = (String) colAttrMap.get(DomainConstants.SELECT_NAME);
            String rel = DomainConstants.EMPTY_STRING;
            if (STRING_APPROVER.equalsIgnoreCase(strAttrName)) {
                rel = RELATIONSHIP_JS_DATASOURCE_PERSON;
            }
            StringList strObjectIdList = JF_PublicMethodClass_mxJPO.getObjectIdList(args);
            for (String objectId : strObjectIdList) {
                DomainObject objectJFDataOutSource = DomainObject.newInstance(context, objectId);
                if (UIUtil.isNotNullAndNotEmpty(rel)) {
                    String personId = objectJFDataOutSource.getInfo(context, "from[" + rel + "].to.id");
                    if (UIUtil.isNullOrEmpty(personId)) {
                        vector.add(DomainConstants.EMPTY_STRING);
                    } else {
                        vector.add(JF_PublicMethodClass_mxJPO.getPersonAllName(context, personId));
                    }
                } else {
                    vector.add(DomainConstants.EMPTY_STRING);
                }
            }
        }catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
        JF_LOGGER.info("method:getTableObjectRelationshipInfo end...");
        return vector;
    }

    /**
     * 当Form中的lie是显示关系的时候， 根据Form中的列名称，去拿取对象关系，显示信息
     * @param context
     * @param args
     * @author LIUJR
     * @throws
     * @return java.util.Vector
     * @date 2024/7/10 13:32
     * @description
     */
    public String getFormObjectRelationshipInfo(Context context, String[] args) throws Exception{
        JF_LOGGER.info("method:getFormObjectRelationshipInfo start...");
        String strResult = DomainConstants.EMPTY_STRING;
        try {
            Map paramsMap = (Map) JPO.unpackArgs(args);
            Map fieldMap = (Map) paramsMap.get(STRING_FIELDMAP);
            //属性名称
            String strAttrName = (String) fieldMap.get(DomainConstants.SELECT_NAME);
            //参数Map,存放数据的
            HashMap requestMap = (HashMap)paramsMap.get(STRING_REQUESTMAP);
            //objectId
            String objectId = (String)requestMap.get(STRING_OBJECTID);
            String rel = DomainConstants.EMPTY_STRING;
            if (STRING_APPROVER.equalsIgnoreCase(strAttrName)) {
                rel = RELATIONSHIP_JS_DATASOURCE_PERSON;
            }
            DomainObject objectJFDataOutSource = DomainObject.newInstance(context, objectId);
            if (UIUtil.isNotNullAndNotEmpty(rel)) {
                String personId = objectJFDataOutSource.getInfo(context, "from[" + rel + "].to.id");
                if (UIUtil.isNullOrEmpty(personId)) {
                    strResult = DomainConstants.EMPTY_STRING;
                } else {
                    strResult = JF_PublicMethodClass_mxJPO.getPersonAllName(context, personId);
                }
            }
            strResult = StringEscapeUtils.escapeHtml4(strResult);
        }catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
        JF_LOGGER.info("method:getFormObjectRelationshipInfo end...");
        return strResult;
    }

    /**
    * 获取属性是id的table列，需要打开并新建tab页
    * @param context
	* @param args
    * @author LIUJR
    * @throws
    * @return java.util.Vector
    * @date 2024/7/9 16:35
    * @description
    */
    public Vector getAttributeWithRelTarget(Context context, String[] args) throws Exception {
        JF_LOGGER.info("method:getAttributeWithRelTarget start...");
        Vector vector = new Vector();
        try {
            Map paramsMap = (Map) JPO.unpackArgs(args);
            Map columnMap = (Map) paramsMap.get(STRING_COLUMNMAP);
            Map colAttrMap = (Map) columnMap.get(MAP_COLATTR);
            JF_LOGGER.info("getAttributeWithRelTarget——>colAttrMap:{}", colAttrMap);
            String strAttrName = (String) colAttrMap.get(DomainConstants.SELECT_NAME);
            StringList strObjectIdList = JF_PublicMethodClass_mxJPO.getObjectIdList(args);
            for (String objectId : strObjectIdList) {
                HashMap<String, String> params = new HashMap<>();
                params.put("IdWithObject", objectId);
                params.put("AttributeWithObject", strAttrName);
                vector.add(JF_PublicMethodClass_mxJPO.getAttributeWithIdOneTarget(context, JPO.packArgs(params)));
            }
        }catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
        JF_LOGGER.info("method:getAttributeWithRelTarget end...");
        return vector;
    }

    /**
    * 根据属性列 设置属性值
    * @param context
	* @param args
    * @author LIUJR
    * @throws
    * @return void
    * @date 2024/7/9 17:59
    * @description
    */
    public static void updateObjectAttributeValue(Context context, String[] args) throws Exception {
        try {
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
        }
    }

    /**
    * 获取对象的属性值
    * @param context
	* @param args
    * @author LIUJR
    * @throws
    * @return java.util.Vector
    * @date 2024/7/12 17:10
    * @description
    */
    public static Vector getObjectAttributeValue(Context context, String[] args) throws Exception {
        JF_LOGGER.info("method:getObjectAttributeValue start...");
        Vector vector = new Vector();
        try {
            Map paramsMap = (Map) JPO.unpackArgs(args);
            Map columnMap = (Map) paramsMap.get(STRING_COLUMNMAP);
            Map colAttrMap = (Map) columnMap.get(MAP_COLATTR);
            String strLanguage = context.getSession().getLanguage();
            JF_LOGGER.info("getAttributeWithRelTarget——>colAttrMap:{}", colAttrMap);
            String strAttrName = (String) colAttrMap.get(DomainConstants.SELECT_NAME);
            StringList strObjectIdList = JF_PublicMethodClass_mxJPO.getObjectIdList(args);
            for (String objectId : strObjectIdList) {
                DomainObject domainObject = DomainObject.newInstance(context, objectId);
                String attributeValue = domainObject.getAttributeValue(context, strAttrName);
                if (UIUtil.isNullOrEmpty(attributeValue)) {
                    vector.add(DomainConstants.EMPTY_STRING);
                } else {
                    String rangeI18NString = EnoviaResourceBundle.getRangeI18NString(context, strAttrName, attributeValue, strLanguage);
                    vector.add(rangeI18NString);
                }

            }
            JF_LOGGER.info("getObjectAttributeValue——>paramsMap:{}", paramsMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
        JF_LOGGER.info("method:getObjectAttributeValue end...");
        return vector;
    }

    /**
    * 数据转发menu名称
    * @param context
	* @param args
    * @author LIUJR
    * @throws
    * @return java.lang.String
    * @date 2024/7/9 18:39
    * @description
    */
    public String getJFDataOutSourceMenuLabel(Context context, String[] args) throws Exception {
        Map map = (Map)JPO.unpackArgs(args);
        Map paramMap = (Map)map.get(STRING_PARAMMAP);
        String strObjectId = (String) paramMap.get(STRING_OBJECTID);
        DomainObject domainObject = DomainObject.newInstance(context, strObjectId);
        return domainObject.getAttributeValue(context, DomainConstants.ATTRIBUTE_TITLE);
    }

    /**
    * 获取当前数据外发申请单的所有的内容  数模 图纸 文档
    * @param context
	* @param args
    * @author LIUJR
    * @throws
    * @return com.matrixone.apps.domain.util.MapList
    * @date 2024/7/10 15:27
    * @description
    */
    public MapList getJFDataOutSourceAllContents(Context context, String[] args) throws Exception{
        MapList mlPartInfoList = new MapList();
        try {
            Map paramsMap = (Map)JPO.unpackArgs(args);
            String strObjectId = (String) paramsMap.get(STRING_OBJECTID);
            DomainObject objectDataOutSource = DomainObject.newInstance(context, strObjectId);
            mlPartInfoList = getDataSourceAllContent(context, strObjectId);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
        return mlPartInfoList;
    }

    /**
    * 数据外发添加现有项 数模 图纸  文档
    * @param context
	* @param args
    * @author LIUJR
    * @throws
    * @return matrix.util.StringList
    * @date 2024/7/10 15:18
    * @description
    */
    public StringList getAllDataOutSourceContent(Context context, String[] args) throws Exception {
        JF_LOGGER.info("method:getAllDataOutSourceContent start...");
        StringList strResultList = new StringList();
        try {
            StringBuilder strWhereBuilder = new StringBuilder();
            String strWhere = strWhereBuilder.toString();
            ContextUtil.pushContext(context);
            MapList mlResult = DomainObject.findObjects(context, DATA_SOURCE_TYPE_ALL.toString(), DomainConstants.QUERY_WILDCARD, strWhere, strBusSelectsList);
            ContextUtil.popContext(context);
            strResultList = (StringList) mlResult.stream().map(m -> {
                Map map = (Map) m;
                String id = (String) map.get(DomainConstants.SELECT_ID);
                return id;
            }).collect(Collectors.toCollection(StringList::new));
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
        JF_LOGGER.info("数据外发添加现有项: {}", strResultList.stream().toList());
        JF_LOGGER.info("method:getAllDataOutSourceContent end...");
        return strResultList;
    }

    /**
    * 由工作中提升到审批中的trigger check 检查
     * 如果有数模 必须填写数模版本
     * 20251217 add by ljr
     * 数据外发提交审批的时候，校验清单中是否存在文档，有文档docx, elsx,ppt,pptx,doc需要校验文档是否存在PDF文件，若无PDF文件则不允许发起，提示“缺少外发PDF文件，请补充”。
    * @param context
	* @param args
    * @author LIUJR
    * @throws
    * @return void
    * @date 2024/7/11 9:42
    * @description
    */
    @ProgramCallable
    public int createJFDataOutSourceRouteCheck(Context context, String[] args) throws Exception {
        JF_LOGGER.info("method:createJFDataOutSourceRouteCheck start...");
        int flag = 0;
        try {
            String objectId =args[0]; //审核对象Id
            String alertMess = DomainConstants.EMPTY_STRING;
            //外发申请单内容
            DomainObject dataOutSourceObject = DomainObject.newInstance(context, objectId);
            String strHasContext = dataOutSourceObject.getInfo(context, "from[Reference Document]");
            if ("FALSE".equalsIgnoreCase(strHasContext)) {
                alertMess = EnoviaResourceBundle.getProperty(context, SUITE_KEY, context.getLocale(), "emxComponents.DataOutSource.NotHasContext");
                emxContextUtil_mxJPO.mqlNotice(context, alertMess);
                flag = 1;
            }
            String strJFWaterMark = dataOutSourceObject.getAttributeValue(context, ATTRIBUTE_JF_WATERMARK);
            //直线经理id
            String strLineManagerId = JF_PublicMethodClass_mxJPO.getPersonLineManager(context, null, dataOutSourceObject.getOwner(context).getName());
            if (UIUtil.isNullOrEmpty(strLineManagerId)) {
                alertMess = EnoviaResourceBundle.getProperty(context, SUITE_KEY, context.getLocale(), "emxComponents.DataOutSource.FillInTheLineManage");
                emxContextUtil_mxJPO.mqlNotice(context, alertMess);
                flag = 1;
            }
            //如果有数模 必须填写数模版本
            MapList dataSourceAllContent = getDataSourceAllContent(context, objectId);
            String attributeValue = dataOutSourceObject.getAttributeValue(context, ATTRIBUTE_JF_OUTSOURCE_REV);
            //如果包含type=VPMReference || type=Drawing
            JF_LOGGER.info("dataSourceAllContent：{}", dataSourceAllContent.toString());
            if (dataSourceAllContent.toString().contains("type=" + TYPE_JF_VPMREFERENCE) || dataSourceAllContent.toString().contains("type=" + TYPE_JF_DRAWING)) {
                //包含数模
                JF_LOGGER.info("@@@@@@@@@@@@@@@@@@@@@");
                if (UIUtil.isNullOrEmpty(attributeValue) || "NoCatiaFile".equalsIgnoreCase(attributeValue)) {
                    alertMess = EnoviaResourceBundle.getProperty(context, SUITE_KEY, context.getLocale(), "emxComponents.DataOutSource.pleaseChooseTransferRev");
                    emxContextUtil_mxJPO.mqlNotice(context, alertMess);
                    flag = 1;
                }
            }
            StringList stringList = StringList.create("doc", "docx", "ppt", "pptx");
            //数据外发提交审批的时候，校验清单中是否存在文档，有文档docx, elsx,ppt,pptx,doc需要校验文档是否存在PDF文件，若无PDF文件则不允许发起，提示“缺少外发PDF文件，请补充”。
            StringList docTitleList = new StringList();
            for (int i = 0; i < dataSourceAllContent.size(); i++) {
                Map map = (Map) dataSourceAllContent.get(i);
                String id = UIUtil.getValue(map, SELECT_ID);
                String type = UIUtil.getValue(map, SELECT_TYPE);
                if (!TYPE_DOCUMENT.equalsIgnoreCase(type)) {
                    continue;
                }
                String title = UIUtil.getValue(map, SELECT_ATTRIBUTE_TITLE);
                JF_LOGGER.info("title:{}", title);
                MapList versionList = JF_PublicMethodClass_mxJPO.getDocumentFiles(context, id);
                //遍历文件
                JF_LOGGER.info("versionList:{}", versionList);
                Boolean hasPdf = Boolean.FALSE;
                Boolean hasTransfer = Boolean.FALSE;
                for (int i1 = 0; i1 < versionList.size(); i1++) {
                    Map versionMap = (Map) versionList.get(i1);
                    String fileName = (String) versionMap.get(CommonDocument.SELECT_TITLE);
                    String[] split = fileName.split("\\.");
                    int len = split.length - 1;
                    JF_LOGGER.info("fileName:{}", fileName);
                    //判断是否有需要转换的文件
                    if (stringList.contains(split[len])) {
                        hasTransfer = Boolean.TRUE;
                    }
                    //如果是pdf不管
                    if (fileName.endsWith(".pdf")) {
                        hasPdf = Boolean.TRUE;
                    }
                }
                if (hasTransfer) {
                    if (!hasPdf) {
                        docTitleList.add(title);
                    }
                }
            }
            JF_LOGGER.info("docTitleList:{}", docTitleList);
            if (!docTitleList.isEmpty()) {
                alertMess = String.format(EnoviaResourceBundle.getProperty(context, SUITE_KEY, context.getLocale(), "emxComponents.DataOutSource.hadConvertFile"), docTitleList);
                emxContextUtil_mxJPO.mqlNotice(context, alertMess);
                flag = 1;
            }
        }catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
        JF_LOGGER.info("method:createJFDataOutSourceRouteCheck end...");
        return flag;
    }


    /**
     * 由工作中提升到审批中的trigger 自动发起一个流程
     * @param context
     * @param args
     * @author LIUJR
     * @throws
     * @return void
     * @date 2024/7/11 9:42
     * @description
     */
    @ProgramCallable
    public int createJFDataOutSourceRouteAction(Context context, String[] args) throws Exception {
        JF_LOGGER.info("method:createJFDataOutSourceRouteAction -创建流程- start...");
        try {
            String objectId =args[0]; //审核对象Id
            //人员
            DomainObject dataOutSourceObject = DomainObject.newInstance(context, objectId);
            //update by ljr 20250421  拿取数据外发的审批人 和 水印为否的拿取部门经理审批人（选择）  如果是同一个人合并
            String strAppPerson = dataOutSourceObject.getInfo(context, "from[JFApprove2Person].to.id");
            String personObjectID = EMPTY_STRING;
            if (UIUtil.isNotNullAndNotEmpty(strAppPerson)) {
                //走新逻辑
                personObjectID = strAppPerson;
            } else {
                //旧逻辑
                //直线经理id
                personObjectID = JF_PublicMethodClass_mxJPO.getPersonLineManager(context, null, dataOutSourceObject.getOwner(context).getName());
            }
            //流程标题
            String tileMess = EnoviaResourceBundle.getProperty(context, SUITE_KEY, context.getLocale(), "emxComponents.DataOutSource.ROUTE.MESS");
            //创建流程
            JF_Route_mxJPO jf_route = new JF_Route_mxJPO(context, args);
            //组装审批人员
            MapList approveList = new MapList();
            String strJFWaterMark = dataOutSourceObject.getAttributeValue(context, ATTRIBUTE_JF_WATERMARK);
            //一级审批人
            Map nReceiverMapOne = (Map) JF_PublicMethodClass_mxJPO.getMapAnyOrAll(personObjectID, tileMess, "true", "1", "All");//设置审批信息 标题
            approveList.add(nReceiverMapOne);
            //二级审批人 部门经理 如果不加水印 需要拿取
            if (RANGE_ATTRIBUTE_JF_WATERMARK_N.equalsIgnoreCase(strJFWaterMark)) {
                //无水印 需要拿取部门经理 关联关系
                StringList strDepartmentManagerIds = dataOutSourceObject.getInfoList(context, "from[" + RELATIONSHIP_JS_DATASOURCE_PERSON + "].to.id");
                for (String pid : strDepartmentManagerIds) {
                    if (personObjectID.equalsIgnoreCase(pid)) {
                        continue;
                    } else {
                        Map nReceiverMap = JF_PublicMethodClass_mxJPO.getMapAnyOrAll(pid, tileMess, "true", "2", "Any");
                        approveList.add(nReceiverMap);
                    }
                }
            }
            String state = STATE_POLICY_JF_DATASOURCE_APPROVE;//在哪个状态增加流程
            String policy = POLICY_JF_DATASOURCE;//哪个Policy上面
            String routeDescription = tileMess;//流程描述
            String routeId = jf_route.createAndStartRoute(context, approveList, objectId, state, policy, routeDescription);
        }catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
        JF_LOGGER.info("method:createJFDataOutSourceRouteAction -创建流程- end...");
        return 0;
    }

    /**
    * 数据外发申请单由审批提升到已完成的trigger：生成密钥对 保存到对象属性上去  并异步处理数据清单
    * @param context
	* @param args
    * @author LIUJR
    * @throws
    * @return void
    * @date 2024/7/11 13:52
    * @description
    */
    @ProgramCallable
    public void promoteApproveGenerateKeyPairs(Context context, String[] args) throws Exception {
        JF_LOGGER.info("method:promoteApproveGenerateKeyPairs -生成密钥对、下载数模、发送邮件- start...");
        try {
            String objectId =args[0]; //审核对象Id
            //同步生成密钥对
            DomainObject dataOutSourceObject = DomainObject.newInstance(context, objectId);
            HashMap<String, Integer> paramsMap = new HashMap<>();
            paramsMap.put("codeLen", 24);
            paramsMap.put("secretLen", 6);
            Map generateKeyPairs = JF_PublicMethodClass_mxJPO.getGenerateKeyPairs(context, JPO.packArgs(paramsMap));
            String codeLen = (String) generateKeyPairs.get("code");
            String secretLen = (String) generateKeyPairs.get("secret");
            //设置属性 密钥对生成完成
            HashMap<String, String> attrMap = new HashMap<>();
            attrMap.put(ATTRIBUTE_JF_CODE, codeLen);
            attrMap.put(ATTRIBUTE_JF_SECRET, secretLen);
            //设置外发数据申请单的属性为Enovia内部处理
            attrMap.put(ATTRIBUTE_JF_DATA_PROCESS_PROGRESS, RANGE_PROCESS_PROGRESS_ENOVIAPROCESS);
            dataOutSourceObject.setAttributeValues(context, attrMap);
            Thread.sleep(2000);
            JF_Util_mxJPO.runAsync(context, StringList.create(objectId, codeLen, secretLen).toStringArray(), "JF_DataOutSource","downloadApplyFormChecklist");
        }catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
        JF_LOGGER.info("method:promoteApproveGenerateKeyPairs -生成密钥对、下载数模、发送邮件- end...");
    }

    /**
    * 下载申请单内容 图纸 文档 数模
    * @param context
	* @param args
    * @author LIUJR
    * @throws
    * @return void
    * @date 2024/7/22 9:25
    * @description         //开启异步
     *             /*
     *             *   1. 如果申请单关联的内容没有数模(二维图不算数模，不会单独只添加二维图纸)，Enovia内部处理
     *                 1. Approve（(异步)promote Trigger）-Complete-OutsourceComplete
     *                   1. 设置属性为Enovia内部处理
     *                   2. Enovia创建申请单文件、DownloadV5File、OtherDoc等文件夹
     *                   3. 在在PDF文档上面打印水印
     *                   4. 下载文档、文档类型中望图纸特殊(开发的时候需要处理)
     *                   5. 压缩、打包、发邮件
     *                   6. 设置属性为外发数据完成
     *                2.如果申请单关联的内容有数模，CAA完成之后，Enovia继续处理
     *                 4. Approve（(异步)promote）-Complete-OutsourceComplete
     *                   1. 设置属性为待CAA处理
     *                   2. Enovia创建申请单文件、DownloadV5File、OtherDoc等文件夹
     *                   3. 在PDF文档上面打印水印
     *                   4. 如果是二维图纸(V6的图纸不管、只管V5的图纸)，下载二维图纸，需要把二维图纸也写入到json文件
     *                   5. 下载文档、数模
     *                 5. 等待CAA请求查询申请单接口(查询属性为待CAA处理的申请单)
     *                   1. CAA调用该接口之后，设置属性为CAA正在处理
     *                 6. CAA数据转换完成之后，调用Enovia接口(设置转换完成的属性)
     *                   1. 设置属性为CAA处理完成
     *                   2. 压缩、打包、发邮件
     *                   3. 设置属性为外发数据完成
     *                 7. 压缩、打包、发邮件
     *             * */
    public void downloadApplyFormChecklist(Context context, String[] args) throws Exception {
        JF_LOGGER.info("method:downloadApplyFormChecklist -下载数模、发送邮件- start...");
        String objectId = args[0];
        Boolean hasJsonWithCatia = Boolean.FALSE;
        DomainObject dataOutSourceObject = DomainObject.newInstance(context, objectId);
        String strBasicUrl = JF_PublicMethodClass_mxJPO.getBasicUrl(context, new String[]{STRING_FILE_SHARE_PATH});
        String strFileName = DomainConstants.EMPTY_STRING;
        String fileOtherDocPath = DomainConstants.EMPTY_STRING;
        String fileSendData = DomainConstants.EMPTY_STRING;
        String fileDownloadV5FilePath = DomainConstants.EMPTY_STRING;
        try {
            //查询申请单信息
            StringList strSelectList = new StringList();
            strSelectList.add(DomainConstants.SELECT_NAME);
            strSelectList.add(DomainConstants.SELECT_OWNER);
            strSelectList.add(String.format(STRING_MQL_ATTRIBUTE, ATTRIBUTE_JF_WATERMARK));
            strSelectList.add(String.format(STRING_MQL_ATTRIBUTE, ATTRIBUTE_JF_OUTSOURCE_REV));
            strSelectList.add(DomainConstants.SELECT_ATTRIBUTE_TITLE);
            Map selectInfoMap = dataOutSourceObject.getInfo(context, strSelectList);
            String strName = (String) selectInfoMap.get(DomainConstants.SELECT_NAME);
            strFileName = UIUtil.getValue(selectInfoMap, DomainConstants.SELECT_ATTRIBUTE_TITLE);
            //防止title为空时候，文件名错误
            strFileName = UIUtil.isNullOrEmpty(strFileName) ? strName : strFileName;
            String strOwner = (String) selectInfoMap.get(DomainConstants.SELECT_OWNER);
            //拿取申请的单内容,分类型存储，后续进行处理需要
            strBusSelectsList.add(String.format(STRING_MQL_ATTRIBUTE, ATTRIBUTE_PART_CATIA_REVISION));
            strBusSelectsList.add(String.format(STRING_MQL_ATTRIBUTE, ATTRIBUTE_PART_DRAWING_REVISION));
            //拿取申请单内容
            MapList dataSourceAllContent = getDataSourceAllContent(context, objectId);
            JF_LOGGER.info("申请单内容:{}", dataSourceAllContent.toString());
            Iterator iterator = dataSourceAllContent.iterator();
            StringList vpmReferenceV6List = new StringList();
            StringList documentList = new StringList();
            StringList drawingV5List = new StringList();
            StringList drawingV6List = new StringList();
            //分类型
            while (iterator.hasNext()) {
                Map map = (Map) iterator.next();
                String strType = (String) map.get(DomainConstants.SELECT_TYPE);
                String strOId = (String) map.get(DomainConstants.SELECT_ID);
                String strCatiaRevision = UIUtil.getValue(map, String.format(STRING_MQL_ATTRIBUTE, ATTRIBUTE_PART_CATIA_REVISION));
                if (TYPE_JF_VPMREFERENCE.equalsIgnoreCase(strType)) {
                    //挑出v5 情况
                    vpmReferenceV6List.add(strOId);
                } else if (TYPE_JF_DRAWING.equalsIgnoreCase(strType)){
                    String strDrawRevision = (String) map.get(String.format(STRING_MQL_ATTRIBUTE, ATTRIBUTE_PART_DRAWING_REVISION));
                    if (STRING_CATIA_V5.equalsIgnoreCase(strDrawRevision)) {
                        drawingV5List.add(strOId);
                    } else {
                        drawingV6List.add(strOId);
                    }
                } else {
                    documentList.add(strOId);
                }
            }
            // Enovia在外部共享盘创建文件夹结构为
            /*  1.  DOS-00000005(申请单Name)---Enovia创建该文件夹
                    1.1. DownloadV5File(Enovia下载的V5数据)---文件夹  Enovia创建该文件夹
                    1.2. TransverterV5File(CAA转换好的V5数据)----文件夹 CAA创建该文件夹
                    1.3. SendData(打包好、准备下发的数据)----文件夹  Enovia创建该文件夹
                    1.4. OtherDoc(文档、图纸(CATIA图纸只发PDF文件))----文件夹 Enovia创建该文件夹
                    1.5. DOS-00000005.json（EBOM结构）---Enovia创建该文件
             */
            //创建申请单文件夹目录
            StringList createDirList = new StringList();
            fileOtherDocPath = strBasicUrl + strName + File.separator + STRING_OTHER_DOC;
            fileSendData = strBasicUrl + strName + File.separator + STRING_SEND_DATA;
            fileDownloadV5FilePath = strBasicUrl + strName + File.separator + STRING_DOWNLOAD_V5_FILE + File.separator;
            createDirList.add(fileOtherDocPath + File.separator);
            createDirList.add(fileDownloadV5FilePath);
            createDirList.add(fileSendData + File.separator);
            for (String filePath : createDirList) {
                JF_PublicMethodClass_mxJPO.createDirFilePath(context, filePath);
            }
            //开始操作数模 生成json文件
            MapList vpmReferenceV5DownList = new MapList();
            MapList drawingV5DownList = new MapList();
            StringList repeatIdList = new StringList();
            if (vpmReferenceV6List.size() > 0 || drawingV5List.size() > 0 || drawingV6List.size() > 0) {
                JF_LOGGER.info("vpmReferenceV6List:{}", vpmReferenceV6List);
                //转换版本
                String strOutSourceRev = (String) selectInfoMap.get(String.format(STRING_MQL_ATTRIBUTE, ATTRIBUTE_JF_OUTSOURCE_REV));
                Map<String, String> paramsMap = new HashMap<>();
                paramsMap.put("strOutSourceRev", strOutSourceRev);
                paramsMap.put("strDirName", strName);
                paramsMap.put("strBasicUrl", strBasicUrl);
                paramsMap.put("vpmReferenceV6Ids", vpmReferenceV6List.join(","));
                paramsMap.put("drawingV5Ids", drawingV5List.join(","));
                paramsMap.put("drawingV6Ids", drawingV6List.join(","));
                Map map = constructJSONFileForDigifax(context, JPO.packArgs(paramsMap));
                vpmReferenceV5DownList = (MapList) map.get("vpmReferenceV5IdMapList");
                drawingV5DownList = (MapList) map.get("drawingV5IdMapList");
                repeatIdList = (StringList) map.get("repeatIdList");
                //是否包含数模和图纸
                hasJsonWithCatia = Boolean.TRUE;
            }
            JF_LOGGER.info("JSON data has been written to the file successfully.");
            //创建文件夹 下载v5数模到共享盘
            if (vpmReferenceV5DownList.size() > 0) {
                //需要创建DownloadV5File文件夹
                //创建好了文件夹后, 开始下载V5数模
                //下载数模  返回数模id 和路径地址
                MapList mapList = JF_PublicMethodClass_mxJPO.downloadDigifaxModel(context, "Part", vpmReferenceV5DownList, fileDownloadV5FilePath, repeatIdList);
            }
            //创建文件夹 下载图纸到共享盘
            if (drawingV5DownList.size() > 0) {
                //下载图纸 返回图纸id 和路径地址
                MapList mapList = JF_PublicMethodClass_mxJPO.downloadDigifaxModel(context, "Draw", drawingV5DownList, fileDownloadV5FilePath, repeatIdList);
            }
            JF_LOGGER.info("documentList:{}", documentList.toString());
            //创建文件夹 下载文档
            if (documentList.size() > 0) {
                //开始下载文档和图纸, 要判断是否加水印 加的话 需要先保存服务器 加好水印 再下载到共享盘
                String strIsWaterMark = (String) selectInfoMap.get(String.format(STRING_MQL_ATTRIBUTE, ATTRIBUTE_JF_WATERMARK));
                //需要加水印的文件集合
                StringList strWaterMarkList = new StringList();
                //所有文件集合
                StringList strAllFilesList = new StringList();
                //当前申请单文件下载的服务器地址
                String strPath = DomainConstants.EMPTY_STRING;
                for (String docId : documentList) {
                    String[] params = new String[3];
                    params[0] = docId;
                    params[1] = strName;
                    params[2] = fileOtherDocPath;
                    Map<String, Object> map = JF_WaterMarkUtils_mxJPO.fileCheckout(context, params);
                    //需要加水印的
                    StringList waterMark = (StringList) map.get("waterMark");
                    StringList files = (StringList) map.get("files");
                    if (!waterMark.isEmpty()) {
//                        StringList collect1 = Arrays.stream(waterMark.split(",")).collect(Collectors.toCollection(StringList::new));
                        strWaterMarkList.addAll(waterMark);
                    }
                    //路径
                    strPath = (String) map.get("path");
                    //所有文档
                    if (!files.isEmpty()) {
                        strAllFilesList.addAll(files);
                    }
                }
                //分类所有文档后,进行打印水印
                if ("Y".equalsIgnoreCase(strIsWaterMark)) {
                    //加水印 全部文档拿出来判断 走水印逻辑  先下载服务器 再放到共享盘
                    JF_WaterMarkUtils_mxJPO.addFilesWaterMark(context, strWaterMarkList, strOwner);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
        //数模下载后 开始做后续操作。
        if (!hasJsonWithCatia) {
            //当没有数模和转换图纸的时候  下载完成 并打包后 发送邮件  发送邮件 0716
            //进行打包
            String dirTwo = fileOtherDocPath;
            String outputDirectory = fileSendData;
            Boolean aBoolean = Boolean.TRUE;
            String[] directories = {dirTwo};
            //无数模和文档 文档打包发邮件一起
            aBoolean = PackageCompressAndSendEmails(context, directories, outputDirectory, strFileName, args);
            if (aBoolean) {
                dataOutSourceObject.setAttributeValue(context, ATTRIBUTE_JF_DATA_PROCESS_PROGRESS, RANGE_PROCESS_PROGRESS_SEND_EMAIL);
            }
        } else {
            //需要caa处理  将状态改为待caa处理
            dataOutSourceObject.setAttributeValue(context, ATTRIBUTE_JF_DATA_PROCESS_PROGRESS, RANGE_PROCESS_PROGRESS_PENDINGCAAPROCESS);
        }
    }

    /**
    * 打包 压缩 发邮件
    * @param context
	* @param directories 压缩目录的dir
	* @param outputDirectory 保存压缩包地址
	* @param strFileName 申请单名称
	* @param args 申请单id
    * @author LIUJR
    * @throws
    * @return java.lang.Boolean
    * @date 2024/7/26 9:34
    * @description
    */
    public static Boolean PackageCompressAndSendEmails(Context context, String[] directories, String outputDirectory, String strFileName, String[] args) {
        Boolean flag = Boolean.TRUE;
        try {
            //打包
            JF_LOGGER.info("压缩包 start...");
            String objectId = args[0];
            strFileName = strFileName.replaceAll("/", "|");
            strFileName = strFileName.replaceAll("\\\\", "|");
            JF_ZipCompressor_mxJPO.createSplitZip(directories, outputDirectory + File.separator + strFileName);
//            JF_ZipCompressor_mxJPO.createSplitZip(directories, outputDirectory + File.separator + strFileName);
            //发送邮件 将申请单发邮件
            JF_LOGGER.info("发邮件 start...");
            JF_LOGGER.info("申请单id:{}", objectId);
            flag  = JF_SendEmailUtils_mxJPO.sendEmailToPortal(context, args);
        } catch (Exception e) {
            flag = Boolean.FALSE;
        }
        return flag;
    }



    /**
    * 在外发数据申请单  审批完成提升到发送完成 的promote action 添加 trigger
     * 生成外发数据单的记录对象。
    * @param context
	* @param args
    * @author LIUJR
    * @throws
    * @return java.lang.Boolean
    * @date 2024/7/16 17:37
    * @description
    */
    @ProgramCallable
    public Boolean DataOutSourceSuccessfulGeneratedRecords(Context context, String[] args) throws Exception {
        Boolean flag = Boolean.TRUE;
        try {
            ContextUtil.startTransaction(context, true);
            Map paramsMap = (Map)JPO.unpackArgs(args);
            String objectId = (String) paramsMap.get(STRING_OBJECTID);
            DomainObject dataOutSourceObject = DomainObject.newInstance(context, objectId);
            //获取外发申请单的属性
            StringList selectAttrList = new StringList();
            selectAttrList.add(DomainConstants.SELECT_ATTRIBUTE_TITLE);
            selectAttrList.add(DomainConstants.SELECT_DESCRIPTION);
            selectAttrList.add(DomainConstants.SELECT_ATTRIBUTE_TITLE);
            Map selectAttrMap = dataOutSourceObject.getInfo(context, selectAttrList);
            MapList dataSourceAllContent = getDataSourceAllContent(context, objectId);
            StringList allContentName = mapListToStringList(context, dataSourceAllContent);
            //创建一个记录对象
            String sObjGeneratorName = UICache.getObjectGenerator(context, "type_JFDataOutSourceLogs", "");
            String sName = DomainObject.getAutoGeneratedName(context, sObjGeneratorName, "");
            String sPolicy = TYPE_JF_DATASOURCE_LOGS;
            Policy policy = new Policy(sPolicy);
            String revision = policy.getFirstInSequence(context);
            DomainObject domainObject = DomainObject.newInstance(context);
            domainObject.createObject(context, TYPE_JF_DATASOURCE_LOGS, sName, revision, sPolicy, context.getVault().getName());
            //设置属性
            HashMap<String, String> attrMap = new HashMap<>();
            attrMap.put(DomainObject.ATTRIBUTE_TITLE, UIUtil.getValue(selectAttrMap, DomainConstants.SELECT_ATTRIBUTE_TITLE));
            attrMap.put("JSLogStutas", "Complete");
            attrMap.put("JSLogdata", allContentName.join(","));
            domainObject.setAttributeValues(context, attrMap);
            //关联关系
            domainObject.addFromObject(context, new RelationshipType(RELATIONSHIP_JS_DATASOURCE_LOGS), objectId);
            ContextUtil.commitTransaction(context);
        }catch (Exception e) {
            e.printStackTrace();
            ContextUtil.abortTransaction(context);
            throw e;
        }
        return flag;
    }

    /**
    * 构造发邮件的复合消息体
    * @param context
	* @param args
    * @author LIUJR
    * @throws
    * @return javax.mail.internet.MimeMultipart
    * @date 2024/7/16 10:34
    * @description
    */
    public static MimeMultipart getHtmlMessages(Context context, String[] args) throws Exception {
        JF_LOGGER.info("getHtmlMessages ----------  start ----------------");
        // 创建多部分消息体
        MimeMultipart multipart = new MimeMultipart();
        //邮件内容的html模板部分
        BodyPart msgBodyPart = new MimeBodyPart();
        //邮件内容的html模板部分
        String objectId = args[0];
        JF_LOGGER.info("申请单id:{}", objectId);
        String htmlContent = setMessageContent(context, args);
        msgBodyPart.setContent(htmlContent, "text/html;charset=utf-8");//html代码部分
        multipart.addBodyPart(msgBodyPart);
        JF_LOGGER.info("getHtmlMessages ----------  end ----------------");
        return multipart;
    }

    /**
    * 构造邮件对象消息体
    * @param context
	* @param args
    * @author LIUJR
    * @throws
    * @return java.lang.String
    * @date 2024/7/16 10:03
    * @description
    */
    public  static  String setMessageContent(Context context, String[] args) throws Exception{
        JF_LOGGER.info("setMessageContent ----------  start ----------------");
        String resultHtml = DomainConstants.EMPTY_STRING;
        try {
            String objectId = args[0];
            JF_LOGGER.info("申请单id:{}", objectId);
            DomainObject dataOutSource = DomainObject.newInstance(context, objectId);
            //获取属性
            StringList attrSelectList = new StringList();
            attrSelectList.add(DomainConstants.SELECT_ATTRIBUTE_TITLE);
            attrSelectList.add(DomainConstants.SELECT_NAME);
            attrSelectList.add(String.format(STRING_MQL_ATTRIBUTE, ATTRIBUTE_V_PART_NUMBER));
            attrSelectList.add(DomainConstants.SELECT_DESCRIPTION);
//            attrSelectList.add(String.format(STRING_MQL_ATTRIBUTE, ATTRIBUTE_JF_CODE));
//            attrSelectList.add(String.format(STRING_MQL_ATTRIBUTE, ATTRIBUTE_JF_SECRET));
            Map attrMap = dataOutSource.getInfo(context, attrSelectList);
            attrMap.put(String.format(STRING_MQL_ATTRIBUTE, ATTRIBUTE_JF_CODE), args[1]);
            attrMap.put(String.format(STRING_MQL_ATTRIBUTE, ATTRIBUTE_JF_SECRET), args[2]);
            //数据外发申请单的owner的联系邮箱
            DomainObject personObject = PersonUtil.getPersonObject(context, dataOutSource.getOwner(context).getName());
            String owner = JF_PublicMethodClass_mxJPO.getPersonAllName(context, personObject.getId(context));
            String emailAddress = personObject.getAttributeValue(context, String.format(STRING_MQL_ATTRIBUTE, ATTRIBUTE_EMAIL_ADDRESS));
            //数据外发申请的内容
            MapList dataSourceAllContent = getDataSourceAllContent(context, objectId);
            JF_LOGGER.info("dataSourceAllContent:{}", dataSourceAllContent.toString());
            //拿取外发的链接地址
            Properties prop = NioJDUtils.readPageObject(context, "JFJDConfig");
            String linkAddress = prop.getProperty("dataOutSource.portal.url").trim();
            linkAddress += UIUtil.getValue(attrMap, String.format(STRING_MQL_ATTRIBUTE, ATTRIBUTE_JF_CODE));
            //拿取html模板
            String language = context.getLocale().toString().contains("zh") ? "zh" : "en";
            String html = JF_PublicMethodClass_mxJPO.getPageHTMLResourceFile(context, "dataSourceEmail", language);
            // 写入模板内容
            Document doc = Jsoup.parse(html);
            doc.getElementById(DomainConstants.SELECT_NAME).append(UIUtil.getValue(attrMap, DomainConstants.SELECT_NAME));
            doc.getElementById(ATTRIBUTE_JF_TOPIC).append(UIUtil.getValue(attrMap, DomainConstants.SELECT_ATTRIBUTE_TITLE));
            doc.getElementById("Owner").append(owner);
            doc.getElementById("OwnerEmail").append(emailAddress);
            doc.getElementById(DomainConstants.SELECT_DESCRIPTION).append(UIUtil.getValue(attrMap, DomainConstants.SELECT_DESCRIPTION));
            //设置链接地址
            Element address = doc.getElementById("Address");
            //链接href
            address.attr("href", linkAddress);
            //设置显示的值
            address.text("zh".equalsIgnoreCase(language) ? "点击下载文件" : "Click to download the file");
            //设置提取密码
            doc.getElementById("password").append(UIUtil.getValue(attrMap, String.format(STRING_MQL_ATTRIBUTE, ATTRIBUTE_JF_SECRET)));
            //创建动态 table列 保存数据外发单的内容
            //如果是零件需要排序 先分组 再排序 update  ljr
            Element content = doc.getElementById("content");
            Map groupMap = (Map) dataSourceAllContent.stream().collect(Collectors.groupingBy(m -> {
                        Map info = (Map) m;
                        return info.get(DomainConstants.SELECT_TYPE);
                    })
            );
            //遍历分组 然后排序
            JF_LOGGER.info("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@2");
            JF_LOGGER.info("分组后：{}",groupMap.toString());
            for (Object k : groupMap.keySet()) {
                String key = String.valueOf(k);
                List infoList = (List) groupMap.get(key);
                MapList mapList = new MapList();
                mapList.addAll(infoList);
                JF_LOGGER.info("@@@@@@@@@@@@@@分组后个别：{}",mapList.toString());
                if (TYPE_JF_VPMREFERENCE.equalsIgnoreCase(key)) {
                    JF_LOGGER.info("@@@@@@@@@@@@@@排序前：{}",mapList.toString());
                    mapList.addSortKey(String.format(STRING_MQL_ATTRIBUTE, ATTRIBUTE_V_PART_NUMBER),
                            ProgramCentralConstants.ASCENDING_SORT,
                            ProgramCentralConstants.SORTTYPE_STRING);
                    mapList.sort();
                    JF_LOGGER.info("@@@@@@@@@@@@@@排序后：{}",mapList.toString());
                }
                Iterator iterator = mapList.iterator();
                while (iterator.hasNext()) {
                    Map map = (Map) iterator.next();
                    Element row = new Element("tr");
                    String strContentType = DomainConstants.EMPTY_STRING;
                    String strContentName = DomainConstants.EMPTY_STRING;
                    //根据现在环境  返回国际化的值
                    if ("zh".equalsIgnoreCase(language)) {
                        strContentType = DomainConstants.TYPE_DOCUMENT.equalsIgnoreCase(UIUtil.getValue(map, DomainConstants.SELECT_TYPE))
                                ? "文档"
                                : TYPE_JF_DRAWING.equalsIgnoreCase(UIUtil.getValue(map, DomainConstants.SELECT_TYPE))
                                ? "图纸"
                                :"3D 数模";
                    } else {
                        strContentType = DomainConstants.TYPE_DOCUMENT.equalsIgnoreCase(UIUtil.getValue(map, DomainConstants.SELECT_TYPE))
                                ? "Documents"
                                : TYPE_JF_DRAWING.equalsIgnoreCase(UIUtil.getValue(map, DomainConstants.SELECT_TYPE))
                                ? "Drawings"
                                :"3D Model";
                    }
                    //名称   数模是企业编号  图纸是name  文档是title
                    strContentName = DomainConstants.TYPE_DOCUMENT.equalsIgnoreCase(UIUtil.getValue(map, DomainConstants.SELECT_TYPE))
                            ? UIUtil.getValue(map, DomainConstants.SELECT_ATTRIBUTE_TITLE)
                            : TYPE_JF_DRAWING.equalsIgnoreCase(UIUtil.getValue(map, DomainConstants.SELECT_TYPE))
                            ? UIUtil.getValue(map, String.format(STRING_MQL_ATTRIBUTE, "PLMEntity.V_Name"))
                            : "Y".equalsIgnoreCase(UIUtil.getValue(map, String.format(STRING_MQL_ATTRIBUTE, ATTRIBUTE_FLEXIBLE_PART)))
                            ? UIUtil.getValue(map, String.format(STRING_MQL_ATTRIBUTE, "PLMEntity.V_Name"))
                            : UIUtil.getValue(map, String.format(STRING_MQL_ATTRIBUTE, ATTRIBUTE_V_PART_NUMBER));
                    Element elementName = new Element("td").text(strContentName);
                    Element elementType = new Element("td").text(strContentType);
                    Element elementRevision = new Element("td").text(UIUtil.getValue(map, DomainConstants.SELECT_REVISION));
                    row.appendChild(elementName);
                    row.appendChild(elementRevision);
                    row.appendChild(elementType);
                    content.appendChild(row);
                }
            }
            resultHtml = doc.toString();
        } catch (Exception e){
            e.printStackTrace();
            throw e;
        }
        return resultHtml;
    }

    /**
    * 获取外发申请单的所有内容
    * @param context
	* @param objectId
    * @author LIUJR
    * @throws
    * @return com.matrixone.apps.domain.util.MapList
    * @date 2024/7/17 13:57
    * @description
    */
    public static MapList getDataSourceAllContent(Context context, String objectId) throws Exception{
        DomainObject dataOutSourceObject = DomainObject.newInstance(context, objectId);
        strBusSelectsList.add(DomainConstants.SELECT_ATTRIBUTE_TITLE);
        strBusSelectsList.add(String.format(STRING_MQL_ATTRIBUTE, ATTRIBUTE_PART_ENTITY_NAME));
        strBusSelectsList.add(String.format(STRING_MQL_ATTRIBUTE, ATTRIBUTE_V_PART_NUMBER));
        strBusSelectsList.add(String.format(STRING_MQL_ATTRIBUTE, "PLMEntity.V_Name"));
        strBusSelectsList.add(String.format(STRING_MQL_ATTRIBUTE, ATTRIBUTE_FLEXIBLE_PART));
        strBusSelectsList.add(DomainConstants.SELECT_DESCRIPTION);
        strBusSelectsList.add(DomainConstants.SELECT_REVISION);
        MapList mlPartInfoList = dataOutSourceObject.getRelatedObjects(
                context,
                RELATIONSHIP_JS_DATASOURCE_CONTENT, // relationship pattern
                DomainConstants.QUERY_WILDCARD, // object pattern
                strBusSelectsList, // object selects
                new StringList(), // relationship selects
                false, // to direction
                true, // from direction
                (short) 1, // recursion level
                "", //object where clause
                "", //relationship where clause
                0
        );
        return mlPartInfoList;
    }

    /**
    * 数据外发单的内容，根据其类型拿取不同的属性名称
    * @param context
	* @param mapList
    * @author LIUJR
    * @throws
    * @return matrix.util.StringList
    * @date 2024/7/17 14:01
    * @description
    */
    public static StringList mapListToStringList(Context context, MapList mapList) {
        StringList allContentName = (StringList) mapList.stream().map(m -> {
            Map map = (Map)m;
            String strType = (String) UIUtil.getValue(map, DomainConstants.SELECT_TYPE);
            String name = DomainConstants.EMPTY_STRING;
            if (DomainConstants.TYPE_DOCUMENT.equalsIgnoreCase(strType)) {
                name = (String) UIUtil.getValue(map, DomainConstants.SELECT_ATTRIBUTE_TITLE);
            } else {
                name = (String) UIUtil.getValue(map, String.format(STRING_MQL_ATTRIBUTE, ATTRIBUTE_PART_ENTITY_NAME));
            }
            return name;
        }).collect(Collectors.toCollection(StringList::new));
        return allContentName;
    }

    /**
    * 状态不允许降级
    * @param context
	* @param args
    * @author LIUJR
    * @throws
    * @return int
    * @date 2024/7/18 17:02
    * @description
    */
    @ProgramCallable
    public int policyDemoteCheckAccessControls(Context context, String[] args) throws Exception{
        String objectId = args[0];
        DomainObject domainObject = DomainObject.newInstance(context, objectId);
        JF_LOGGER.info("###############################3");
        int flag = 0;
        StringList params = new StringList();
        params.add("from[Object Route]");
        params.add(DomainConstants.SELECT_CURRENT);
        Map info = domainObject.getInfo(context, params);
        String current = (String) info.get(DomainConstants.SELECT_CURRENT);
        String hasRouteFlag = (String) info.get("from[Object Route]");
        String hasJFCode = domainObject.getAttributeValue(context, ATTRIBUTE_JF_CODE);
        if ("Approve".equalsIgnoreCase(current)&&"TRUE".equalsIgnoreCase(hasRouteFlag)) {
            flag = 1;
        }
        if ("Complete".equalsIgnoreCase(current) && UIUtil.isNotNullAndNotEmpty(hasJFCode)) {
            flag = 1;
        }
        if ("OutsourceComplete".equalsIgnoreCase(current)) {
            flag = 1;
        }
        if (1 == flag){
            //不允许降级
            String alertMess = EnoviaResourceBundle.getProperty(context, SUITE_KEY, context.getLocale(), "emxComponents.DataOutSource.DontDemoteState");
            emxContextUtil_mxJPO.mqlNotice(context, alertMess);
        }
        return flag;
    }

    /**
     * 当外发申请单的数模是v6，图纸是v5的时候。构造结构 json, 放置到共享盘
     * @param context
     * @param args
     *      vpmReferenceV6List v6数模id列表
     *      strName 申请单的名称 也是存放json的文件夹
     *      strOutSourceRev catia转换版本
     * @author LIUJR的
     * @throws
     * @return java.util.Map
     * @date 2024/7/22 11:11
     * @description
     */
    public static Map constructJSONFileForDigifax(Context context, String[] args) {
        HashMap<String, Object> resultMap = new HashMap<>();
        MapList vpmReferenceV5List = new MapList();
        MapList drawingV5List = new MapList();
        Map originalFileNameMap = new HashMap<String, String>();
        StringList repeatIdList = new StringList();
        try {
            Map params = (Map) JPO.unpackArgs(args);
            String strOutSourceRev = (String) params.get("strOutSourceRev");
            String strDirName = (String) params.get("strDirName");
            String vpmReferenceV6Ids = (String) params.get("vpmReferenceV6Ids");
            String drawingV5Ids = (String) params.get("drawingV5Ids");
            String drawingV6Ids = (String) params.get("drawingV6Ids");
            String strBasicUrl = (String) params.get("strBasicUrl");
            JSONObject json = new JSONObject();
            json.put("CATIARev", strOutSourceRev);
            JSONArray itemsArray = new JSONArray();
            //数模n层
            if (vpmReferenceV6Ids.length() > 0) {
                StringList vpmReferenceV6List = Arrays.stream(vpmReferenceV6Ids.split(",")).collect(Collectors.toCollection(StringList::new));
                for (String v6Id : vpmReferenceV6List) {
                    JSONObject itemJson = new JSONObject();
                    getPartItemArray(context, v6Id, itemJson, vpmReferenceV5List, originalFileNameMap, repeatIdList);
                    itemsArray.add(itemJson);
                }
            }
            //图纸 只有一层
            if (drawingV5Ids.length() > 0) {
                StringList drawingV5IdsList = Arrays.stream(drawingV5Ids.split(",")).collect(Collectors.toCollection(StringList::new));
                for (String drawingV5Id : drawingV5IdsList) {
                    JSONObject itemJson = new JSONObject();
                    Map drawingCheckInFileMap = getDrawingItemArray(context, drawingV5Id, itemJson, "V5");
                    itemsArray.add(itemJson);
                    //存储v5图纸 以供下载
                    drawingCheckInFileMap.put("oid", drawingV5Id);
                    drawingV5List.add(drawingCheckInFileMap);
                }
            }
            if (drawingV6Ids.length() > 0) {
                StringList drawingV6IdsList = Arrays.stream(drawingV6Ids.split(",")).collect(Collectors.toCollection(StringList::new));
                for (String drawingV6Id : drawingV6IdsList) {
                    JSONObject itemJson = new JSONObject();
                    Map drawingCheckInFileMap = getDrawingItemArray(context, drawingV6Id, itemJson, "V6");
                    itemsArray.add(itemJson);
                    //v6图纸不下载
                }
            }
            json.put("items", itemsArray);
            //对象构造好后, 生成json文件 写入文件
            String filePath = strBasicUrl + strDirName + "/" + strDirName + ".json";
            /* 操作文件 */
            try (FileWriter fileWriter = new FileWriter(filePath)) {
                fileWriter.write(json.toJSONString());
                JF_LOGGER.info("JSON数据已成功写入文件。");
            } catch (IOException e) {
                e.printStackTrace();
                JF_LOGGER.info("写入文件时发生错误。");
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        resultMap.put("vpmReferenceV5IdMapList", vpmReferenceV5List);
        resultMap.put("drawingV5IdMapList", drawingV5List);
        resultMap.put("repeatIdList", repeatIdList);
        return resultMap;
    }


    /**
    * 遍历数模一层节点树结构
    * @param context
	* @param v6Id
    * @author LIUJR
    * @throws
    * @return void
    * @date 2024/7/22 13:45
    * @description
    */
    private static void getPartItemArray(Context context, String v6Id, JSONObject itemJson, MapList vpmReferenceV5List, Map originalFileNameMap, StringList repeatIdList) {
        try {
            DomainObject digifaxV6Object = DomainObject.newInstance(context, v6Id);
            Map digifaxV6ObjectInfoMap = digifaxV6Object.getInfo(context, strPartSelectList);
            String strCatiaRevision = (String) UIUtil.getValue(digifaxV6ObjectInfoMap, String.format(STRING_MQL_ATTRIBUTE, ATTRIBUTE_PART_CATIA_REVISION));
            //构造item
            Map fileNameMap = constructPackingJSON(context, itemJson, digifaxV6ObjectInfoMap, "", originalFileNameMap, repeatIdList);
            fileNameMap.put("oid", UIUtil.getValue(digifaxV6ObjectInfoMap, DomainConstants.SELECT_ID));
            //将需要下载的v5进行保存起来，包括需要修改后的
            if (STRING_CATIA_V5.equalsIgnoreCase(strCatiaRevision)) {
                vpmReferenceV5List.add(fileNameMap);
            }
            //拿取child node
            MapList mlPartInfoList = getLevelPartInfo(context, digifaxV6Object);
            if (mlPartInfoList.size() > 0) {
                JSONArray childArray = new JSONArray();
                Iterator iterator = mlPartInfoList.iterator();
                while (iterator.hasNext()) {
                    Map childV6Map = (Map) iterator.next();
                    getPartChildArray(context, childV6Map, childArray, vpmReferenceV5List, originalFileNameMap, repeatIdList);
                }
                itemJson.put("child", childArray);
            }
        } catch (FrameworkException e) {
            throw new RuntimeException(e);
        }
    }

    /**
    * 构造图纸的json
    * @param context
	* @param drawingV5Id v5图纸id
	* @param itemJson
    * @author LIUJR
    * @throws
    * @return void
    * @date 2024/7/22 16:58
    * @description
    */
    private static Map getDrawingItemArray(Context context, String drawingV5Id, JSONObject itemJson, String catiaRev) {
        Map<String, String> map = new HashMap<>();
        try {
            DomainObject drawingV5Object = DomainObject.newInstance(context, drawingV5Id);
            Map drawingV5IdObjectInfoMap = drawingV5Object.getInfo(context, strDrawingSelectList);
            //构造item
            itemJson.put("name", UIUtil.getValue(drawingV5IdObjectInfoMap, DomainConstants.SELECT_NAME));
            itemJson.put("revision", UIUtil.getValue(drawingV5IdObjectInfoMap, DomainConstants.SELECT_REVISION));
            if ("V5".equalsIgnoreCase(catiaRev)) {
                String drawingId = UIUtil.getValue(drawingV5IdObjectInfoMap, DomainConstants.SELECT_ID);
                String[] params = new String[3];
                params[0] = drawingId;
                params[1] = UIUtil.getValue(drawingV5IdObjectInfoMap, DomainConstants.SELECT_TYPE);
                params[2] = "";
                Map drawingCheckInFileMap = getDigifaxOrDrawingCheckInFile(context, params);
                String originalFileName = (String) drawingCheckInFileMap.get("strModFileName");
                map = drawingCheckInFileMap;
                itemJson.put("originalFileName", originalFileName);
            } else {
                itemJson.put("originalFileName", "");
            }
            String value = UIUtil.getValue(drawingV5IdObjectInfoMap, String.format(STRING_MQL_ATTRIBUTE, ATTRIBUTE_PART_DRAWING_REVISION));
            String drawingMaster = UIUtil.isNullOrEmpty(value) ? "V6" : value;
            itemJson.put("Master", drawingMaster);
            String partNum = UIUtil.getValue(drawingV5IdObjectInfoMap, String.format(STRING_MQL_ATTRIBUTE, ATTRIBUTE_PART_ENTITY_NAME));
            if (UIUtil.isNullOrEmpty(partNum)) {
                 partNum = UIUtil.getValue(drawingV5IdObjectInfoMap, DomainConstants.SELECT_NAME);
            }
            itemJson.put("PartNumber", partNum);
            itemJson.put("type", UIUtil.getValue(drawingV5IdObjectInfoMap, DomainConstants.SELECT_TYPE));
            itemJson.put("ChineseName", UIUtil.getValue(drawingV5IdObjectInfoMap, String.format(STRING_MQL_ATTRIBUTE, ATTRIBUTE_PART_NAME_CN)));
            itemJson.put("EnglishName", UIUtil.getValue(drawingV5IdObjectInfoMap, String.format(STRING_MQL_ATTRIBUTE, ATTRIBUTE_PART_NAME_EN)));
        } catch (FrameworkException e) {
            throw new RuntimeException(e);
        }
        return map;
    }

    /**
    * 构造item节点信息
    * @param itemJson
	* @param digifaxObjectInfoMap
    * @author LIUJR
    * @throws
    * @return void
    * @date 2024/7/22 14:31
    * @description
    */
    public static Map constructPackingJSON(Context context, JSONObject itemJson, Map digifaxObjectInfoMap, String flag, Map originalFileNameMap, StringList repeatIdList) {
        itemJson.put("name", UIUtil.getValue(digifaxObjectInfoMap, DomainConstants.SELECT_NAME));
        itemJson.put("revision", UIUtil.getValue(digifaxObjectInfoMap, DomainConstants.SELECT_REVISION));
        //拿取信息
        String digifaxId = UIUtil.getValue(digifaxObjectInfoMap, DomainConstants.SELECT_ID);

        String[] params = new String[3];
        params[0] = digifaxId;
        params[1] = UIUtil.getValue(digifaxObjectInfoMap, DomainConstants.SELECT_TYPE);
        params[2] = UIUtil.getValue(digifaxObjectInfoMap, String.format(STRING_MQL_ATTRIBUTE, ATTRIBUTE_PART_CATIA_REVISION));
        Map digifaxCheckInFileMap = getDigifaxOrDrawingCheckInFile(context, params);
        String physicalid = UIUtil.getValue(digifaxObjectInfoMap, "physicalid");
        String strOriginalFileName = (String) digifaxCheckInFileMap.get("strModFileName");
        String originalFileName = strOriginalFileName;
        //new liujr
        if (originalFileNameMap.containsKey(originalFileName)) {
            String orId = (String) originalFileNameMap.get(originalFileName);
            if (!orId.equalsIgnoreCase(physicalid)) {
                originalFileName = physicalid + "//" + originalFileName;
                repeatIdList.add(physicalid);
            }
        } else {
            originalFileNameMap.put(originalFileName, physicalid);
        }
        //end
        itemJson.put("originalFileName", originalFileName);
        String partNum = UIUtil.getValue(digifaxObjectInfoMap, String.format(STRING_MQL_ATTRIBUTE, ATTRIBUTE_V_PART_NUMBER));
        if (UIUtil.isNullOrEmpty(partNum)) {
            String title = UIUtil.getValue(digifaxObjectInfoMap, DomainConstants.SELECT_ATTRIBUTE_TITLE);
            if (UIUtil.isNullOrEmpty(title)) {
                partNum = UIUtil.getValue(digifaxObjectInfoMap, DomainConstants.SELECT_NAME);
            } else {
                partNum = title;
            }
        }
        if ("Y".equalsIgnoreCase(UIUtil.getValue(digifaxObjectInfoMap, String.format(STRING_MQL_ATTRIBUTE, ATTRIBUTE_FLEXIBLE_PART)))) {
            //是变形件
//            partNum =  UIUtil.getValue(digifaxObjectInfoMap, String.format(STRING_MQL_ATTRIBUTE, "PLMEntity.V_Name"));
            //20251105 修改
            partNum =  strOriginalFileName.split("\\.")[0];
        }
        itemJson.put("PartNumber", partNum);
        String strV5OrV6 = UIUtil.getValue(digifaxObjectInfoMap, String.format(STRING_MQL_ATTRIBUTE, ATTRIBUTE_PART_CATIA_REVISION));
        strV5OrV6 = UIUtil.isNullOrEmpty(strV5OrV6) ? "V6" : strV5OrV6;
        itemJson.put("Master", strV5OrV6);
        itemJson.put("type", "Part");
        itemJson.put("ChineseName", UIUtil.getValue(digifaxObjectInfoMap, String.format(STRING_MQL_ATTRIBUTE, ATTRIBUTE_PART_NAME_CN)));
        itemJson.put("EnglishName", UIUtil.getValue(digifaxObjectInfoMap, String.format(STRING_MQL_ATTRIBUTE, ATTRIBUTE_PART_NAME_EN)));
        itemJson.put("Material", UIUtil.getValue(digifaxObjectInfoMap, String.format(STRING_MQL_ATTRIBUTE, ATTRIBUTE_PART_MATERIAL)));
        itemJson.put("PartID", UIUtil.getValue(digifaxObjectInfoMap, String.format(STRING_MQL_ATTRIBUTE, ATTRIBUTE_V_PART_NUMBER)));
        if ("child".equalsIgnoreCase(flag)) {
            itemJson.put(REL_INSTANCE_ATTR_EXTERNAL_ID, UIUtil.getValue(digifaxObjectInfoMap, String.format(STRING_MQL_ATTRIBUTE, REL_INSTANCE_ATTR_EXTERNAL_ID)));
            itemJson.put(REL_INSTANCE_ATTR_V_TREE_ORDER, UIUtil.getValue(digifaxObjectInfoMap, String.format(STRING_MQL_ATTRIBUTE, REL_INSTANCE_ATTR_V_TREE_ORDER)));
            itemJson.put(REL_INSTANCE_ATTR_V_MATRIX1, UIUtil.getValue(digifaxObjectInfoMap, String.format(STRING_MQL_ATTRIBUTE, REL_INSTANCE_ATTR_V_MATRIX1)));
            itemJson.put(REL_INSTANCE_ATTR_V_MATRIX2, UIUtil.getValue(digifaxObjectInfoMap, String.format(STRING_MQL_ATTRIBUTE, REL_INSTANCE_ATTR_V_MATRIX2)));
            itemJson.put(REL_INSTANCE_ATTR_V_MATRIX3, UIUtil.getValue(digifaxObjectInfoMap, String.format(STRING_MQL_ATTRIBUTE, REL_INSTANCE_ATTR_V_MATRIX3)));
            itemJson.put(REL_INSTANCE_ATTR_V_MATRIX4, UIUtil.getValue(digifaxObjectInfoMap, String.format(STRING_MQL_ATTRIBUTE, REL_INSTANCE_ATTR_V_MATRIX4)));
            itemJson.put(REL_INSTANCE_ATTR_V_MATRIX5, UIUtil.getValue(digifaxObjectInfoMap, String.format(STRING_MQL_ATTRIBUTE, REL_INSTANCE_ATTR_V_MATRIX5)));
            itemJson.put(REL_INSTANCE_ATTR_V_MATRIX6, UIUtil.getValue(digifaxObjectInfoMap, String.format(STRING_MQL_ATTRIBUTE, REL_INSTANCE_ATTR_V_MATRIX6)));
            itemJson.put(REL_INSTANCE_ATTR_V_MATRIX7, UIUtil.getValue(digifaxObjectInfoMap, String.format(STRING_MQL_ATTRIBUTE, REL_INSTANCE_ATTR_V_MATRIX7)));
            itemJson.put(REL_INSTANCE_ATTR_V_MATRIX8, UIUtil.getValue(digifaxObjectInfoMap, String.format(STRING_MQL_ATTRIBUTE, REL_INSTANCE_ATTR_V_MATRIX8)));
            itemJson.put(REL_INSTANCE_ATTR_V_MATRIX9, UIUtil.getValue(digifaxObjectInfoMap, String.format(STRING_MQL_ATTRIBUTE, REL_INSTANCE_ATTR_V_MATRIX9)));
            itemJson.put(REL_INSTANCE_ATTR_V_MATRIX10, UIUtil.getValue(digifaxObjectInfoMap, String.format(STRING_MQL_ATTRIBUTE, REL_INSTANCE_ATTR_V_MATRIX10)));
            itemJson.put(REL_INSTANCE_ATTR_V_MATRIX11, UIUtil.getValue(digifaxObjectInfoMap, String.format(STRING_MQL_ATTRIBUTE, REL_INSTANCE_ATTR_V_MATRIX11)));
            itemJson.put(REL_INSTANCE_ATTR_V_MATRIX12, UIUtil.getValue(digifaxObjectInfoMap, String.format(STRING_MQL_ATTRIBUTE, REL_INSTANCE_ATTR_V_MATRIX12)));
        }
        return digifaxCheckInFileMap;
    }

    /**
    * 获取数模的子节点
    * @param context
	* @param digifaxV6Object
    * @author LIUJR
    * @throws
    * @return com.matrixone.apps.domain.util.MapList
    * @date 2024/7/22 16:59
    * @description
    */
    public static MapList getLevelPartInfo(Context context, DomainObject digifaxV6Object) throws FrameworkException {
        MapList mlPartInfoList = digifaxV6Object.getRelatedObjects(
                context,
                RELATIONSHIP_INSTANCE, // relationship pattern
                TYPE_JF_VPMREFERENCE + "," + TYPE_JF_PART, // object pattern
                strPartSelectList, // object selects
                strRelSelectList, // relationship selects
                false, // to direction
                true, // from direction
                (short) 1, // recursion level
                "", //object where clause
                "", //relationship where clause
                0
        );
        return mlPartInfoList;
    }

    /**
    * 获取child的json  ----递归
    * @param context
	* @param pChildV6Map
	* @param childArray
    * @author LIUJR
    * @throws
    * @return void
    * @date 2024/7/22 14:05
    * @description
    */
    private static void getPartChildArray(Context context, Map pChildV6Map, JSONArray childArray, MapList vpmReferenceV5List, Map originalFileNameMap, StringList repeatIdList) {
        try {
            JSONObject childJson = new JSONObject();
            String childV6Id = (String) pChildV6Map.get(DomainConstants.SELECT_ID);
            DomainObject digifaxV6Object = DomainObject.newInstance(context, childV6Id);

            Map fileNameMap = constructPackingJSON(context, childJson, pChildV6Map, "child", originalFileNameMap, repeatIdList);
            fileNameMap.put("oid", childV6Id);
            //将需要下载的v5进行保存起来，包括需要修改后的
            //存储CATIA V5
            String strCatiaRevision = (String) UIUtil.getValue(pChildV6Map, String.format(STRING_MQL_ATTRIBUTE, ATTRIBUTE_PART_CATIA_REVISION));
            //将需要下载的v5进行保存起来，包括需要修改后的
            if (STRING_CATIA_V5.equalsIgnoreCase(strCatiaRevision)) {
                vpmReferenceV5List.add(fileNameMap);
            }
            //拿取child node
            MapList mlPartInfoList = getLevelPartInfo(context, digifaxV6Object);
            if (mlPartInfoList.size() > 0) {
                JSONArray childArrays = new JSONArray();
                Iterator iterator = mlPartInfoList.iterator();
                while (iterator.hasNext()) {
                    Map childV6Map = (Map) iterator.next();
                    getPartChildArray(context, childV6Map, childArrays, vpmReferenceV5List, originalFileNameMap, repeatIdList);
                }
                childJson.put("child", childArrays);
            }
            childArray.add(childJson);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
    * 获取图纸，装配体，零部件的文件名称
    * @param context
	* @param args
    * @author LIUJR
    * @throws
    * @return java.lang.String
    * @date 2024/7/23 11:30
    * @description
    */
    public static Map getDigifaxOrDrawingCheckInFile(Context context, String[] args) {
        HashMap<String, String> resultMap = new HashMap<>();
        String strOId = args[0];
        String strType = args[1];
        String strCatiaRevision = args[2];
        String strSourceFileName = DomainObject.EMPTY_STRING;
        try {
            DomainObject domainObject = DomainObject.newInstance(context, strOId);
            String strSuffixName = DomainConstants.EMPTY_STRING;
            if (TYPE_JF_DRAWING.equalsIgnoreCase(strType)) {
                //图纸
                strSourceFileName = domainObject.getInfo(context, "format[1].file.name");
                strSuffixName = ".CATDrawing";
                JF_LOGGER.info("TYPE_JF_DRAWING -- strFileName:{}", strSourceFileName);
            } else {
                if (STRING_CATIA_V5.equalsIgnoreCase(strCatiaRevision)) {
                    //数模 PLMEntity.V_usage 辨别是装配体还是零部件
                    String strUsage = domainObject.getAttributeValue(context, ATTRIBUTE_PART_ENTITY_USAGE);
                    String strRel = DomainConstants.EMPTY_STRING;
                    if ("3DPart".equalsIgnoreCase(strUsage)) {
                        //零部件
                        strRel = RELATIONSHIP_VPM_REP_INSTANCE;
                        strSuffixName = ".CATPart";
                    } else {
                        //装配体
                        strRel = RELATIONSHIP_CAD_ASSEMBLY_REP_INSTANCE;
                        strSuffixName = ".CATProduct";
                    }
                    JF_LOGGER.info("strRel:{}", strRel);
                    String strCadId = domainObject.getInfo(context, String.format(STRING_MQL_RELATIONSHIP_FROM, strRel, DomainConstants.SELECT_ID));
                    resultMap.put("strCadId", strCadId);
                    JF_LOGGER.info("strCadId:{}", strCadId);
                    if (UIUtil.isNotNullAndNotEmpty(strCadId)) {
                        DomainObject objectXCAD = DomainObject.newInstance(context, strCadId);
                        strSourceFileName = objectXCAD.getInfo(context, "format[1].file.name");
                        JF_LOGGER.info("object3DShape - allFormatFiles:{}", strSourceFileName.toString());
                    }
                }
            }
            String strFileName = DomainConstants.EMPTY_STRING;
            if (strSourceFileName.contains("=")) {
                strFileName = strSourceFileName.split("=")[1];
            }
            String strModFileName = DomainObject.EMPTY_STRING;
            if (UIUtil.isNotNullAndNotEmpty(strFileName)) {
                strModFileName = strFileName;
                strModFileName += strSuffixName;
            }
            //根据类型构造下载后的文件名称
            resultMap.put("strModFileName", strModFileName);
            resultMap.put("strSourceFileName", strSourceFileName);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return resultMap;
    }

    /**
    * 数据外发申请单权限： 只有研发人员才可以操作 ： 在研发部下面的人员就算研发人员(需要考虑研发部挂的人员，和研发部的下一级部门的人员)
     * 20250908 ECR和快照增加数据外发，采购部门有权限
    * @param context
	* @param args
    * @author LIUJR
    * @throws
    * @return java.lang.Boolean
    * @date 2024/7/30 9:32
    * @description
    */
    public Boolean getJFDataOutSourceAccess(Context context, String[] args) throws Exception {
        Boolean flag = Boolean.TRUE;
        Boolean isPush = Boolean.FALSE;
        try {
            Map paramsMap = (Map) JPO.unpackArgs(args);
            JF_LOGGER.info("paramsMap:{}", paramsMap);
            String toolbar = (String) paramsMap.get("toolbar");
            JF_LOGGER.info("toolbar:{}", toolbar);
            DomainObject personObject = PersonUtil.getPersonObject(context);
            String userName = context.getUser();
            StringList departmentList = personObject.getInfoList(context, "to[" + DomainRelationship.RELATIONSHIP_MEMBER + "].from.id");
            JF_LOGGER.info("departmentList:{}", departmentList);
            if (departmentList.isEmpty()) {
                flag = Boolean.FALSE;
            } else {
                //如果是ECR / DR或者快照
                String parentOID = (String) paramsMap.get("parentOID");
                if (UIUtil.isNotNullAndNotEmpty(parentOID)) {
                    DomainObject domainObject = DomainObject.newInstance(context, parentOID);
                    String typeName = domainObject.getTypeName(context);
                    String current = domainObject.getInfo(context, SELECT_CURRENT);
                    if (JF_PLMConstants_mxJPO.TYPE_JFSnapshot.equalsIgnoreCase(typeName) && !"FROZEN".equalsIgnoreCase(current)) {
                        return Boolean.FALSE;
                    }
                    if ((JF_PLMConstants_mxJPO.TYPE_JFNewECR.equalsIgnoreCase(typeName)||JF_PLMConstants_mxJPO.TYPE_JFFormalECR.equalsIgnoreCase(typeName)) && ("Create".equalsIgnoreCase(current) || "Review".equalsIgnoreCase(current))) {
                        return Boolean.FALSE;
                    }
                    if (JF_PLMConstants_mxJPO.TYPE_JFDR.equalsIgnoreCase(typeName) && !"Complete".equalsIgnoreCase(current)) {
                        return Boolean.FALSE;
                    }
                }
                //获取研发部门的对象id
                ContextUtil.pushContext(context);
                isPush = Boolean.TRUE;
                String strRDCenterDepartmentId = JF_PublicMethodClass_mxJPO.getBasicUrl(context, new String[]{STRING_DEPARTMENT_ID});
                String[] split = strRDCenterDepartmentId.split(",");
                StringList departmentsList = new StringList();
//                departmentsList = StringList.create(split);
                if (UIUtil.isNotNullAndNotEmpty(toolbar) && "JFNewECRAffectedItemsTooBar,JFSnapshotContentToolBar".contains(toolbar)) {
                    //20250908 ECR和快照增加数据外发，采购部门有权限
                    departmentsList.add(split[1]);
                    strRDCenterDepartmentId = split[1];
                } else if (UIUtil.isNotNullAndNotEmpty(toolbar) && ("JFDRVPMTable".equalsIgnoreCase(toolbar)||"JFProductConfigTableToolbar".equalsIgnoreCase(toolbar)||"JFProductConfigServicePartsListToolbar".equalsIgnoreCase(toolbar)||"JFProductConfigRouteToolbar".equalsIgnoreCase(toolbar))){
                    departmentsList.add(split[0]);
                    strRDCenterDepartmentId = split[0];
                } else {
                    departmentsList = StringList.create(split);
                }
                JF_LOGGER.info("departmentsList:{}", departmentsList);
                HashSet hashSet = new HashSet<>();
                for (String departId : departmentsList){
                    Department department = new Department();
                    department.setId(departId);
                    isPush = Boolean.TRUE;
                    MapList mapList = department.getRelatedObjects(
                            context,
                            DomainRelationship.RELATIONSHIP_COMPANY_DEPARTMENT,
                            DomainConstants.TYPE_DEPARTMENT,
                            strBusSelectsList,
                            strRelSelectList,
                            false,
                            true,
                            (short) 0,
                            "",
                            "",
                            0
                    );
                    StringList strDRDepartmentList = (StringList) mapList.stream().map(m -> {
                        Map map = (Map) m;
                        String did = UIUtil.getValue(map, DomainConstants.SELECT_ID);
                        return did;
                    }).collect(Collectors.toCollection(StringList::new));
                    strDRDepartmentList.add(strRDCenterDepartmentId);
                    hashSet.add(strDRDepartmentList);
                }
                StringList strDRDepartmentList = StringList.create(hashSet);
                // 使用 Stream 和 Lambda 表达式筛选未包含的项
                String[] projectArrays = departmentList.toStringArray();
                JF_LOGGER.info("strDRDepartmentList:{}", strDRDepartmentList);
                String strDepartments = strDRDepartmentList.toString();
                // 使用 Stream 和 Lambda 表达式筛选未包含的项
                Set<String> checkResultSet = Arrays.stream(projectArrays)
                        .map(String::trim) // 去除项前后的空白字符
                        .filter(item -> strDepartments.contains(item)) // 只保留包含的项
                        .collect(Collectors.toSet()); // 收集结果到 Set
                JF_LOGGER.info("checkResultSet:{}", checkResultSet);
                if (checkResultSet.isEmpty()) {
                    flag = Boolean.FALSE;
                } else {
                    flag = Boolean.TRUE;
                }
            }
            JF_LOGGER.info("flag1:{}",flag);
            if(!flag) {
                JF_LOGGER.info("flag2:{}",flag);
                Map requestMap = new HashMap();
                requestMap.put("roleName", "JfITAdmin");
                requestMap.put("userName", userName);
                flag = JF_Util_mxJPO.isIncludeRole(context, JPO.packArgs(requestMap));
            }
        } catch (FrameworkException e) {
            e.printStackTrace();
            flag = Boolean.FALSE;
        } finally {
            if (isPush) {
                ContextUtil.popContext(context);
            }
        }
        return flag;
    }

    /**
     * 增加Trigger:零件分类有正式编码之后不让修改该属性
     * @param context
     * @param args
     * @author LIUJR
     * @throws
     * @return int
     * @date 2024/7/30 14:19
     * @description
     */
    @ProgramCallable
    public int modifyAttributeCheck(Context context, String[] args) throws Exception{
        JF_LOGGER.info("##############modifyAttributeCheck###########");
        String objectId = args[0];
        String modAttrName = args[1];
        String oLdAttrValue = args[2];
        String newAttrValue = args[3];
        int iReturn = 0;
        if (ATTRIBUTE_PART_TYPE.equalsIgnoreCase(modAttrName)) {
            JF_LOGGER.info("objectId:{}",  objectId);
            JF_LOGGER.info("modAttrName:{}", modAttrName);
            DomainObject domainObject = DomainObject.newInstance(context, objectId);
            String attributeValue = domainObject.getAttributeValue(context, ATTRIBUTE_V_PART_NUMBER);
            String ATTRIBUTE_JF_ISXPDM_VALUE = domainObject.getAttributeValue(context, ATTRIBUTE_JF_ISXPDM);
            JF_LOGGER.info("attributeValue:{}", attributeValue);
            //修改零件类型 需要判断企业编号是否有值
            if (UIUtil.isNotNullAndNotEmpty(attributeValue)&&"N".equalsIgnoreCase(ATTRIBUTE_JF_ISXPDM_VALUE)) {
                //不能改
                String alertMess = EnoviaResourceBundle.getProperty(context, SUITE_KEY, context.getLocale(), "emxComponents.VPMReference.notModifyAttribute");
                emxContextUtil_mxJPO.mqlNotice(context, alertMess);
                iReturn = 1;
            } else {

            }
        }  else  if (ATTR_JF_VPMReferenceJF_Unit.equalsIgnoreCase(modAttrName)) {
            JF_LOGGER.info("objectId:{}",  objectId);
            JF_LOGGER.info("modAttrName:{}", modAttrName);
            JF_LOGGER.info("oLdAttrValue:{}", oLdAttrValue);
            JF_LOGGER.info("newAttrValue:{}", newAttrValue);
            DomainObject domainObject = DomainObject.newInstance(context, objectId);
            String attributeValue = domainObject.getAttributeValue(context, ATTR_JF_VPMReferenceJF_Unit);
            StringList classIdList = domainObject.getInfoList(context,"to[Classified Item].from.id");
            StringList typeSelectList = JF_Util_mxJPO.basicBolistSel();
            typeSelectList.add(SELECT_DESCRIPTION);
            boolean flag = false;
            String classId = "";
            for(int i=0;i<classIdList.size();i++) {
                DomainObject from = DomainObject.newInstance(context, classIdList.get(i));
                //获取到根节点，判断是否是零件分类库
                Map rootLibMap = JF_Library_mxJPO.getRootLibByClassId(context, typeSelectList, JF_Util_mxJPO.basicRellistSel(), from);
                String strLibTitle = (String) rootLibMap.get(SELECT_ATTRIBUTE_TITLE);
                Properties properties = JF_Util_mxJPO.readPageObject(context, "JFJDConfig");
                String partitionLibraryTitle = (String) properties.get("partitionLibrary.ContainStandardTitle");
                if(partitionLibraryTitle.contains(strLibTitle)) {
                    flag = true;
                    classId =  classIdList.get(i);
                   break;
                }
            }
            //入库了，拿到分类的零件单位
            DomainObject classObj = DomainObject.newInstance(context, classId);
            String classUnit = classObj.getAttributeValue(context,Attr_JF_LibUnit);
            // 分类的单位不为空，并且和修改之后的值不匹配的情况下，需要弹出提示 不让修改
            if(!newAttrValue.equalsIgnoreCase(classUnit)&&UIUtil.isNotNullAndNotEmpty(classUnit)) {
                String alertMess = EnoviaResourceBundle.getProperty(context, SUITE_KEY, context.getLocale(), "emxComponents.VPMReference.notModifyUnit");
                iReturn = 1;
                throw new Exception(alertMess);
            }
            //零件关联的分类，如果分类的零件单位属性有值，就不让修改零件单位，需要兼容历史数据，那就判断当前修改之后的值是否和分类下面的匹配

           /* if (UIUtil.isNotNullAndNotEmpty(attributeValue) && UIUtil.isNotNullAndNotEmpty(modAttrValue)&&!modAttrValue.startsWith("prd")) {
                //不能改
                String alertMess = EnoviaResourceBundle.getProperty(context, SUITE_KEY, context.getLocale(), "emxComponents.VPMReference.notModifyAttributePLM_ExternalID");
                emxContextUtil_mxJPO.mqlNotice(context, alertMess);
                iReturn = 1;
            }*/
        }
        JF_LOGGER.info("@@@@@@@@@@@@@return:{}", iReturn);
        return iReturn;
    }

    /**
    * 重新生成json文件
    * @param context
	* @param args
    * @author LIUJR
    * @throws
    * @return void
    * @date 2024/8/15 9:25
    * @description
    */
    public void generateJSONFileAgain(Context context, String[] args) throws Exception {
        JF_LOGGER.info("method:downloadApplyFormChecklist -重新生成json文件...");
        String objectId = args[0];
        DomainObject dataOutSourceObject = DomainObject.newInstance(context, objectId);
        String strBasicUrl = JF_PublicMethodClass_mxJPO.getBasicUrl(context, new String[]{STRING_FILE_SHARE_PATH});
        try {
            //查询申请单信息
            StringList strSelectList = new StringList();
            strSelectList.add(DomainConstants.SELECT_NAME);
            strSelectList.add(DomainConstants.SELECT_OWNER);
            strSelectList.add(String.format(STRING_MQL_ATTRIBUTE, ATTRIBUTE_JF_OUTSOURCE_REV));
            Map selectInfoMap = dataOutSourceObject.getInfo(context, strSelectList);
            String strName = (String) selectInfoMap.get(DomainConstants.SELECT_NAME);
            //拿取申请单内容
            MapList dataSourceAllContent = getDataSourceAllContent(context, objectId);
            JF_LOGGER.info("申请单内容:{}", dataSourceAllContent.toString());
            Iterator iterator = dataSourceAllContent.iterator();
            StringList vpmReferenceV6List = new StringList();
            StringList documentList = new StringList();
            StringList drawingV5List = new StringList();
            StringList drawingV6List = new StringList();
            //分类型
            while (iterator.hasNext()) {
                Map map = (Map) iterator.next();
                String strType = (String) map.get(DomainConstants.SELECT_TYPE);
                String strOId = (String) map.get(DomainConstants.SELECT_ID);
                String strCatiaRevision = UIUtil.getValue(map, String.format(STRING_MQL_ATTRIBUTE, ATTRIBUTE_PART_CATIA_REVISION));
                if (TYPE_JF_VPMREFERENCE.equalsIgnoreCase(strType)) {
                    //挑出v5 情况
                    vpmReferenceV6List.add(strOId);
                } else if (TYPE_JF_DRAWING.equalsIgnoreCase(strType)){
                    String strDrawRevision = (String) map.get(String.format(STRING_MQL_ATTRIBUTE, ATTRIBUTE_PART_DRAWING_REVISION));
                    if (STRING_CATIA_V5.equalsIgnoreCase(strDrawRevision)) {
                        drawingV5List.add(strOId);
                    } else {
                        drawingV6List.add(strOId);
                    }
                } else {
                    documentList.add(strOId);
                }
            }
            //开始操作数模 生成json文件
//            drawingV5List.addAll(drawingV6List);
            if (vpmReferenceV6List.size() > 0 || drawingV5List.size() > 0) {
                JF_LOGGER.info("vpmReferenceV6List:{}", vpmReferenceV6List);
                //转换版本
                String strOutSourceRev = (String) selectInfoMap.get(String.format(STRING_MQL_ATTRIBUTE, ATTRIBUTE_JF_OUTSOURCE_REV));
                Map<String, String> paramsMap = new HashMap<>();
                paramsMap.put("strOutSourceRev", strOutSourceRev);
                paramsMap.put("strDirName", strName);
                paramsMap.put("strBasicUrl", strBasicUrl);
                paramsMap.put("vpmReferenceV6Ids", vpmReferenceV6List.join(","));
                paramsMap.put("drawingV5Ids", drawingV5List.join(","));
                paramsMap.put("drawingV6Ids", drawingV6List.join(","));
                Map map = constructJSONFileForDigifax(context, JPO.packArgs(paramsMap));
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
    * 数据转发邮箱的格式自定义
    * @param context
	* @param args
    * @author LIUJR
    * @throws
    * @return java.lang.String
    * @date 2024/11/21 14:25
    * @description
    */
    public String buildOutSourceAddressHtml(Context context,String[] args) throws Exception {
        String result = DomainConstants.EMPTY_STRING;
        try{
            Map map = (Map) JPO.unpackArgs(args);
            JF_LOGGER.info("map:{}", map.toString());
            Map requestMap = (Map) map.get(STRING_REQUESTMAP);
            String form = (String) requestMap.get("form");
            StringBuffer sb = new StringBuffer();
            String language = context.getLocale().toString();
            String title = EnoviaResourceBundle.getAttributeI18NString(context,ATTRIBUTE_JF_ADDRESS,language);
            if ("JFCreateJFDataOutSourceForm".equalsIgnoreCase(form)) {
                sb.append("<textarea cols=\"25\" rows=\"5\" name=\"JSAddressee\" title=\"" + title + "\" id=\"JSAddressee\" value=\"\"></textarea>");
                String info = "sample: yanf@xxx.com;leicy@xxx.com";
                info += language.contains("zh") ? "(使用英文分号分隔)" : "Separate with English semicolons";
                sb.append("<span style=\"color: #660000;font-size: 13px;\" id=\"character-count\">" + StringEscapeUtils.escapeHtml4(info) + "</span>");
                sb.append("<script>window.addEventListener('load', function JFOnloadHandler() {\n" +
                            "document.getElementById('JSAddressee').customValidate = IdmCheckEmailAddress }, false);</script>");
            } else if ("JFEditJFDataOutSourceForm".equalsIgnoreCase(form)) {
                String mode = (String) requestMap.get("mode");
                String objectId = (String) requestMap.get("objectId");
                DomainObject domainObject = DomainObject.newInstance(context, objectId);
                String attrValue = domainObject.getAttributeValue(context, ATTRIBUTE_JF_ADDRESS);
                if ("view".equalsIgnoreCase(mode)) {
                    sb.append(attrValue);
                } else if ("edit".equalsIgnoreCase(mode)) {
                    sb.append("<textarea cols=\"25\" rows=\"5\" name=\"JSAddressee\" title=\"" + title + "\" id=\"JSAddressee\" value=\"\">"+ StringEscapeUtils.escapeHtml4(attrValue) +"</textarea>");
                    String info = "sample: yanf@xxx.com;leicy@xxx.com";
                    info += language.contains("zh") ? "(使用英文分号分隔)" : "Separate with English semicolons";
                    sb.append("<span style=\"color: #660000;font-size: 13px;\" id=\"character-count\">" + StringEscapeUtils.escapeHtml4(info) + "</span>");
                    sb.append("<script>window.addEventListener('load', function JFOnloadHandler() {\n" +
                            "document.getElementById('JSAddressee').customValidate = IdmCheckEmailAddress }, false);</script>");
                }
            }
            result = sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return  result;
    }


    /**
     * 修改JSAddressee属性
     * @param context
     * @param args
     * @return
     * @throws Exception
     */
    public void updateSourceAddress(Context context, String[] args) throws Exception {
        try {
            Map map = (Map) JPO.unpackArgs(args);
            JF_LOGGER.info("map:{}", map.toString());
            Map paramMap = (Map) map.get("paramMap");
            Map fieldMap = (Map) map.get("fieldMap");
            String objectId = (String) paramMap.get(STRING_OBJECTID);
            String newValue = (String) paramMap.get("New Value");
            String attr = (String) fieldMap.get("name");
            DomainObject domainObject = DomainObject.newInstance(context, objectId);
            domainObject.setAttributeValue(context, attr, newValue);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * @description 上传数据外发系统外数据
     * @param context
     * @param args
     * @return java.util.Map
     * key oids  value 12133.131|211.121 以 | 分割
     * key titles  value 文件名1|文件名2 以 | 分割
     * @throws Exception
     * @author CHENYAN
     * @date 2024/3/2 11:44
     */
    public static void uploadExFile(Context context,String[] args) throws Exception{
        JF_LOGGER.info("--------------------- uploadFile begin--------------------------");
        String strLang = context.getSession().getLanguage();
        try{
            //返回所有文档对象信息集合
            MapList resDocMapList = new MapList();
            Map paramMap = (Map) JPO.unpackArgs(args);
            List files = (List) paramMap.get("files");
            String strObjectId = (String) paramMap.get("objectId");
            JF_LOGGER.info("strObjectId:{}",strObjectId);

            String sOSName 		= System.getProperty("os.name");
            String separator 	= sOSName.contains("Windows") ? "\\" : "/";
            String sFolder		= sOSName.contains("Windows") ? JF_ECRRESTService_mxJPO.FolderWIN : JF_ECRRESTService_mxJPO.FolderUNIX ;
            String sTmpDir		= Environment.getValue(context, "TMPDIR");

            // 安全处理临时目录
            if(null != sTmpDir && !sTmpDir.trim().isEmpty()) {
                sFolder = sTmpDir;
                if(!sFolder.endsWith(separator)) {
                    sFolder = sFolder + separator;
                }
            }

            Iterator iter = files.iterator();
            int index;
            String sFilename="";
            java.io.File file = null;
            //文档id集合
            StringList oids = new StringList();
            //标题集合
            StringList titles = new StringList();

            while (iter.hasNext()) {
                file = (java.io.File) iter.next();
                sFilename = file.getName();
                JF_LOGGER.info("处理文件：{}", sFilename);

                // 安全截取文件名
                if(sFilename.contains("/")) {
                    index = sFilename.lastIndexOf("/");
                    sFilename = sFilename.substring(index + 1);
                }
                if(sFilename.contains("\\")) {
                    index = sFilename.lastIndexOf("\\");
                    sFilename = sFilename.substring(index+1);
                }

                java.io.File outfile = new java.io.File(sFolder + sFilename);

                // ========== 核心修复：流式读写，解决大文件OOM ==========
                try (FileInputStream fis = new FileInputStream(file);
                     FileOutputStream fos = new FileOutputStream(outfile)) {

                    // 8KB缓冲区，分段读写，不占用大内存
                    byte[] buffer = new byte[8192];
                    int len;
                    while ((len = fis.read(buffer)) != -1) {
                        fos.write(buffer, 0, len);
                    }
                    fos.flush();
                }
                // ======================================================

                // 创建ENOVIA文档对象
                String sObjGeneratorName = UICache.getObjectGenerator(context, "type_Document", "");
                String sName = DomainObject.getAutoGeneratedName(context, sObjGeneratorName, "");
                String docPolicy = PropertyUtil.getSchemaProperty(EnoviaResourceBundle.getProperty(context, "emxFrameowrk.FileUpload.Default.Policy"));

                if (UIUtil.isNotNullAndNotEmpty(docPolicy)) {
                    CommonDocument cDoc = new CommonDocument();
                    Policy policy = new Policy(docPolicy);
                    String revision = policy.getFirstInSequence(context);
                    PropertyUtil.setRPEValue(context, "MX_ALLOW_POV_STAMPING", "true", false);

                    cDoc.createObject(context, DomainObject.TYPE_DOCUMENT, sName, revision, docPolicy, context.getVault().getName());
                    cDoc.setAttributeValue(context, "Title", sFilename);

                    String storeFromBL = DocumentUtil.getStoreFromBL(context, "Document");
                    cDoc.createVersion(context, sFilename, sFilename, null);
                    cDoc.checkinFile(context, true, true, "", "generic", storeFromBL, sFilename, sFolder);

                    oids.add(cDoc.getId(context));
                    titles.add(sFilename);
                }

                // 单个文件处理完立即删除临时文件（修复原代码删错问题）
                if (outfile != null && outfile.exists()) {
                    outfile.delete();
                    JF_LOGGER.info("临时文件已删除：{}", outfile.getAbsolutePath());
                }
            }

            // 关联文档到业务对象
            String[] allContent = oids.toStringArray();
            String relationshipName = PropertyUtil.getSchemaProperty("relationship_ReferenceDocument");
            DomainObject domainObject = DomainObject.newInstance(context, strObjectId);
            domainObject.addRelatedObjects(context, new RelationshipType(relationshipName), true, allContent);

        }catch (Exception e){
            JF_LOGGER.error("文件上传异常：", e);
            throw e;
        }
        JF_LOGGER.info("--------------------- uploadExFile end--------------------------");
    }

}
