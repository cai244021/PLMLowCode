import com.matrixone.apps.domain.DomainAccess;
import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.DomainRelationship;
import com.matrixone.apps.domain.util.*;
import com.matrixone.apps.framework.ui.ProgramCallable;
import com.matrixone.apps.framework.ui.UIUtil;
import com.matrixone.apps.program.ProgramCentralConstants;
import com.matrixone.apps.program.ProgramCentralUtil;
import matrix.db.*;
import matrix.util.MatrixException;
import matrix.util.StringList;
import org.apache.commons.text.StringEscapeUtils;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.matrixone.apps.common.Task;
import jakarta.mail.internet.MimeMultipart;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.Set;
import java.util.stream.Collectors;

import static com.dassault_systemes.enovia.apps.materialcomposition.MATCConstants.STRING_NEW_VALUE;
import static com.matrixone.apps.domain.DomainConstants.*;
import static com.matrixone.apps.domain.DomainConstants.SELECT_ID;

public class JF_DeviationApplicationSource_mxJPO  implements JF_PLMConstants_mxJPO{
    private static final Logger _logger = LoggerFactory.getLogger(JF_DeviationApplicationSource_mxJPO.class);

    private static String COLUMN_MAP = "columnMap";
    private static String OBJECT_LIST = "objectList";
    private static String PARAM_LIST = "paramList";
    private static String LANGUAGE_STR = "languageStr";
    private static String SETTINGS = "settings";
    private static String ICON = "Icon";
    private static String TRUE_STRING = "true";
    private static String FALSE_STRING = "false";
    private static String ID_LEVEL = "id[level]";
    private static String TABLE = "table";
    private static String FORM = "form";
    private static String TYPE_KIND_OF = "type.kindof";
    private static final String TYPE_JFDA = "JFDA";
    private static final String ATTRIBUTE_JFDASTARTTIME = "JFDAStartTime";
    private static final String ATTRIBUTE_JFDACLOSETIME = "JFDACloseTime";
    private static final String ATTRIBUTE_JFDAEXTENSIONTIME = "JFDAExtensionTime";
    private static final String ATTRIBUTE_JFDAEXTENSIONTIMEBAK = "JFDAExtensionTimeBak";
    private static final String ATTRIBUTE_JFDADELAYCOUNT = "JFDADelayCount";
    private static final String ATTRIBUTE_JFCHANGETYPE = "JFChangeType";
    private static final String ATTRIBUTE_JFAFFECTSFACTORY = "JFAffectsFactory";
    private static final String TYPE_JF_DATASK = "JF_DATask";
    private static final String SYMBOLIC_TYPE_JF_DATASK = "type_JF_DATask";
    private static final String RELATIONSHIP_DA2VPM = "JFDA2VPMReference";
    private static final String RELATIONSHIP_JFDA2JFDATASK = "JFDA2JFDATask";
    private static final String RELATIONSHIP_MEMBER = "Member";
    private static final String RELATIONSHIP_JFDRCHAIRMANGER2PERSON = "JFDRChairManger2Person";
    private static final String TYPE_VPMREFERENCE = "VPMReference";
    private static final String TYPE_JF_ECOTASK = "JF_ECOTask";
    private static final String STATE_POLICY_JFDA_Approve= "state_Approve";
    private static final String STATE_POLICY_JFDA_IMPLEMENT= "state_Implement";
    private static final String SUITE_KEY = "emxComponentsStringResource";
    private static final String POLICY_JFDA = "policy_JFDA";
    private static final String JFPROJECTNAMEOID = "JFProjectNameOID";
    private static final String STATE_IN_WORK = "In_Work";
    private static final String RELATIONSHIP_JFCHANGE2PROJECT = "JFChange2Project";
    private static final String RELATIONSHIP_JFECR2DA = "JFECR2DA";
    private static final String RELATIONSHIP_JFDR2DA = "JFDR2DA";
    private static final String FIELD_RELATED_DR_OID = "RelatedDROID";
    private static final String FIELD_RELATED_ECR_OID = "RelatedECROID";
    private static final String RESOURCE_DA_SELECT_PROJECT_FIRST = "emxFramework.JFDA.SelectProjectFirst";
    private static final String RESOURCE_DA_INVALID_RELATED_CHANGE = "emxFramework.JFDA.InvalidRelatedChange";
    private static final String RESOURCE_DA_RELATED_CHANGE_ACCESS_DENIED = "emxFramework.JFDA.RelatedChangeAccessDenied";
    private static final String[] stateIndex = new String[]{"Create", "Assign", "Active", "Review", "Complete"};
    private static final DateTimeFormatter DA_REPORT_FILE_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DA_REPORT_OUTPUT_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DA_REPORT_OUTPUT_DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final StringList bosel = JF_Util_mxJPO.basicBolistSel();
    private static final StringList relsel = JF_Util_mxJPO.basicRellistSel();
    private static final Logger logger = LoggerFactory.getLogger(JF_DeviationApplicationSource_mxJPO.class);
    private static StringList selList = new StringList();

    private static class DAApprovalNode {
        String receiveTime = "";
        String completeTime = "";
        String current = "";
    }

    //更新实际开始时间
    public JF_DeviationApplicationSource_mxJPO() {

    }

    static {
        selList.add(DomainConstants.SELECT_ID);
        selList.add(DomainConstants.SELECT_TYPE);
        selList.add(DomainConstants.SELECT_NAME);
        selList.add(DomainConstants.SELECT_STATES);
        relsel.add(DomainRelationship.SELECT_ID);
    }

    /**
     * @description 查询所有的DA实体类
     * @param context
     * @param args
     * @Author lsa
     */
    public MapList getAllJFDASource(Context context, String[] args) {
        MapList mapList;
        try {
            String strLoginUser = context.getUser();
            Vector assignments = PersonUtil.getAssignments(context,strLoginUser );
            _logger.info("assignments:{}", assignments);
            //新增DA管理员可以查看所有的DR add by chenyan 2025/04/03
            boolean isShowAll = false;
            if (assignments.contains(JF_PLMConstants_mxJPO.ROLE_DAADMIN)) {
                isShowAll = true;
            }
            String strWhere = "";
            if (!isShowAll) {
                strWhere = JF_PublicMethodClass_mxJPO.buildStringInStrings("owner=='", strLoginUser, "'"," || from[JFChange2Project].to.owner=='",strLoginUser,"'");
            }
            mapList = DomainObject.findObjects(context, TYPE_JFDA, STRING_SYMB_ASTERISK, strWhere, selList);
        } catch (FrameworkException e) {
            throw new RuntimeException(e);
        }
        return mapList;
    }

    /**
     * 创建DA申请单，关联项目及所选的一个或多个DR、正式ECR
     **
     * @param context
     * @param args 创建表单参数
     * @return void
     * @throws Exception
     * @author lsa
     * @date 2026/7/24 16:13
     */
    public void createJFDeviationApplication(Context context, String[] args) throws Exception {
        boolean transactionStarted = false;
        boolean contextPushed = false;
        try {
            Map paramsMap = JPO.unpackArgs(args);
            logger.info("!!!!!!!!!!!!!!!!!args:{}", paramsMap.toString());

            Map requestMap  = (Map) paramsMap.get(STRING_REQUESTMAP);
            Map paramMap  = (Map) paramsMap.get(STRING_PARAMMAP);
            String objectId = (String) paramMap.get(STRING_OBJECTID);
            String projectOID = (String) requestMap.get(JFPROJECTNAMEOID);
            String relatedDROID = UIUtil.getValue(requestMap, FIELD_RELATED_DR_OID);
            String relatedECROID = UIUtil.getValue(requestMap, FIELD_RELATED_ECR_OID);
            String[] relatedDRIds = UIUtil.isNullOrEmpty(relatedDROID)
                    ? new String[0] : relatedDROID.split("\\|");
            String[] relatedECRIds = UIUtil.isNullOrEmpty(relatedECROID)
                    ? new String[0] : relatedECROID.split("\\|");
//            String ChairMangerOID = (String) requestMap.get(CHAIRMANGEROID);
            if (UIUtil.isNullOrEmpty(projectOID)) {
                String selectProjectMessage = EnoviaResourceBundle.getProperty(context,
                        "emxFrameworkStringResource", context.getLocale(), RESOURCE_DA_SELECT_PROJECT_FIRST);
                throw new FrameworkException(selectProjectMessage);
            }
            DomainObject obj = DomainObject.newInstance(context, objectId);
            DomainObject projectObj = DomainObject.newInstance(context, projectOID);
            if (!TYPE_PROJECT_SPACE.equals(projectObj.getInfo(context, SELECT_TYPE))) {
                String selectProjectMessage = EnoviaResourceBundle.getProperty(context,
                        "emxFrameworkStringResource", context.getLocale(), RESOURCE_DA_SELECT_PROJECT_FIRST);
                throw new FrameworkException(selectProjectMessage);
            }
            String invalidRelatedChangeMessage = EnoviaResourceBundle.getProperty(context,
                    "emxFrameworkStringResource", context.getLocale(), RESOURCE_DA_INVALID_RELATED_CHANGE);
            for (String relatedDRId : relatedDRIds) {
                DomainObject drObj = DomainObject.newInstance(context, relatedDRId);
                Map drInfo = drObj.getInfo(context, StringList.create(SELECT_TYPE,
                        "from[" + RELATIONSHIP_JFCHANGE2PROJECT + "].to.id"));
                if (!TYPE_JFDR.equals(UIUtil.getValue(drInfo, SELECT_TYPE))
                        || !projectOID.equals(UIUtil.getValue(drInfo,
                        "from[" + RELATIONSHIP_JFCHANGE2PROJECT + "].to.id"))) {
                    throw new FrameworkException(invalidRelatedChangeMessage);
                }
            }
            for (String relatedECRId : relatedECRIds) {
                DomainObject ecrObj = DomainObject.newInstance(context, relatedECRId);
                Map ecrInfo = ecrObj.getInfo(context, StringList.create(SELECT_TYPE,
                        "from[" + RELATIONSHIP_JFCHANGE2PROJECT + "].to.id"));
                if (!TYPE_JFFormalECR.equals(UIUtil.getValue(ecrInfo, SELECT_TYPE))
                        || !projectOID.equals(UIUtil.getValue(ecrInfo,
                        "from[" + RELATIONSHIP_JFCHANGE2PROJECT + "].to.id"))) {
                    throw new FrameworkException(invalidRelatedChangeMessage);
                }
            }
            ContextUtil.startTransaction(context, true);
            transactionStarted = true;
            ContextUtil.pushContext(context);
            contextPushed = true;
            obj.setAttributeValue(context,"JFProjectName",projectObj.getDescription(context));
            DomainRelationship.connect(context,obj,RELATIONSHIP_JFCHANGE2PROJECT,projectObj);
            for (String relatedDRId : relatedDRIds) {
                DomainRelationship.connect(context, DomainObject.newInstance(context, relatedDRId),
                        RELATIONSHIP_JFDR2DA, obj);
            }
            for (String relatedECRId : relatedECRIds) {
                DomainRelationship.connect(context, DomainObject.newInstance(context, relatedECRId),
                        RELATIONSHIP_JFECR2DA, obj);
            }
            ContextUtil.commitTransaction(context);
            transactionStarted = false;
//            if(!"".equals(ChairMangerOID)){
//                DomainObject ChairMangerObj = DomainObject.newInstance(context, ChairMangerOID);
//                DomainRelationship.connect(context,obj,RELATIONSHIP_JFDRCHAIRMANGER2PERSON,ChairMangerObj);
//            }
        } catch (Exception e) {
            e.printStackTrace();
            if (transactionStarted) {
                ContextUtil.abortTransaction(context);
            }
            throw e;
        } finally {
            if (contextPushed) {
                ContextUtil.popContext(context);
            }
        }
    }

    /**
     * 获取DA所选或已关联项目下尚未关联的DR或正式ECR搜索范围
     **
     * @param context
     * @param args 搜索参数，包含projectId或daId以及changeType
     * @return StringList 当前项目关联的DR或正式ECR ID
     * @throws Exception
     * @author caipan
     * @date 2026/7/24 16:13
     */
    @com.matrixone.apps.framework.ui.IncludeOIDProgramCallable
    public StringList getDAProjectRelatedChangeIds(Context context, String[] args) throws Exception {
        Map params = JPO.unpackArgs(args);
        String projectId = (String) params.get("projectId");
        String daId = (String) params.get("daId");
        if (UIUtil.isNullOrEmpty(daId)) {
            daId = (String) params.get(STRING_OBJECTID);
        }
        String changeType = (String) params.get("changeType");
        StringList result = new StringList();
        if (!(TYPE_JFDR.equals(changeType) || TYPE_JFFormalECR.equals(changeType))) {
            return result;
        }
        if (UIUtil.isNullOrEmpty(projectId) && UIUtil.isNotNullAndNotEmpty(daId)) {
            DomainObject daObj = DomainObject.newInstance(context, daId);
            if (TYPE_JFDA.equals(daObj.getInfo(context, SELECT_TYPE))) {
                projectId = daObj.getInfo(context,
                        "from[" + RELATIONSHIP_JFCHANGE2PROJECT + "].to.id");
            }
        }
        if (UIUtil.isNullOrEmpty(projectId)) {
            return result;
        }
        DomainObject projectObj = DomainObject.newInstance(context, projectId);
        if (!TYPE_PROJECT_SPACE.equals(projectObj.getInfo(context, SELECT_TYPE))) {
            return result;
        }
        StringList relatedChangeIds = new StringList();
        if (UIUtil.isNotNullAndNotEmpty(daId)) {
            String relationship = TYPE_JFDR.equals(changeType)
                    ? RELATIONSHIP_JFDR2DA : RELATIONSHIP_JFECR2DA;
            relatedChangeIds = DomainObject.newInstance(context, daId).getInfoList(context,
                    "to[" + relationship + "].from.id");
        }
        StringList objectSelects = new StringList();
        objectSelects.add(SELECT_ID);
        MapList changeList = projectObj.getRelatedObjects(context,
                RELATIONSHIP_JFCHANGE2PROJECT,
                changeType,
                objectSelects,
                null,
                true,
                false,
                (short) 1,
                "",
                "",
                0);
        for (Object changeObj : changeList) {
            String changeId = UIUtil.getValue((Map) changeObj, SELECT_ID);
            if (UIUtil.isNotNullAndNotEmpty(changeId) && !relatedChangeIds.contains(changeId)) {
                result.add(changeId);
            }
        }
        return result;
    }

    /**
     * 获取DA关联的DR列表
     **
     * @param context
     * @param args 表格参数，包含objectId
     * @return MapList DA关联的DR对象及关系信息
     * @throws Exception
     * @author caipan
     * @date 2026/7/24 16:13
     */
    @ProgramCallable
    public MapList getDARelatedDRList(Context context, String[] args) throws Exception {
        return getDARelatedChangeList(context, args, RELATIONSHIP_JFDR2DA, TYPE_JFDR);
    }

    /**
     * 获取DA关联的正式ECR列表
     **
     * @param context
     * @param args 表格参数，包含objectId
     * @return MapList DA关联的正式ECR对象及关系信息
     * @throws Exception
     * @author caipan
     * @date 2026/7/24 16:13
     */
    @ProgramCallable
    public MapList getDARelatedECRList(Context context, String[] args) throws Exception {
        return getDARelatedChangeList(context, args, RELATIONSHIP_JFECR2DA, TYPE_JFFormalECR);
    }

    /**
     * 添加或移除DA关联的DR、正式ECR
     **
     * @param context
     * @param args 操作参数，包含daId、mode、changeType和changeIds
     * @return Map 操作结果
     * @throws Exception
     * @author caipan
     * @date 2026/7/24 16:13
     */
    public Map updateDARelatedChanges(Context context, String[] args) throws Exception {
        Map result = new HashMap();
        boolean transactionStarted = false;
        boolean contextPushed = false;
        try {
            Map params = JPO.unpackArgs(args);
            String daId = (String) params.get("daId");
            String mode = (String) params.get("mode");
            String changeType = (String) params.get("changeType");
            StringList changeIds = (StringList) params.get("changeIds");
            DomainObject daObj = DomainObject.newInstance(context, daId);
            Map daInfo = daObj.getInfo(context, StringList.create(SELECT_TYPE, SELECT_CURRENT,
                    SELECT_OWNER, "from[" + RELATIONSHIP_JFCHANGE2PROJECT + "].to.id"));
            if (!TYPE_JFDA.equals(UIUtil.getValue(daInfo, SELECT_TYPE))
                    || !STATE_IN_WORK.equals(UIUtil.getValue(daInfo, SELECT_CURRENT))
                    || !context.getUser().equalsIgnoreCase(UIUtil.getValue(daInfo, SELECT_OWNER))) {
                throw new FrameworkException(EnoviaResourceBundle.getProperty(context,
                        "emxFrameworkStringResource", context.getLocale(),
                        RESOURCE_DA_RELATED_CHANGE_ACCESS_DENIED));
            }
            String relationship;
            if (TYPE_JFDR.equals(changeType)) {
                relationship = RELATIONSHIP_JFDR2DA;
            } else if (TYPE_JFFormalECR.equals(changeType)) {
                relationship = RELATIONSHIP_JFECR2DA;
            } else {
                throw new FrameworkException(EnoviaResourceBundle.getProperty(context,
                        "emxFrameworkStringResource", context.getLocale(),
                        RESOURCE_DA_INVALID_RELATED_CHANGE));
            }
            String projectId = UIUtil.getValue(daInfo,
                    "from[" + RELATIONSHIP_JFCHANGE2PROJECT + "].to.id");
            String invalidMessage = EnoviaResourceBundle.getProperty(context,
                    "emxFrameworkStringResource", context.getLocale(),
                    RESOURCE_DA_INVALID_RELATED_CHANGE);
            Map<String, String> relatedConnectionIds = new HashMap<>();
            MapList relatedChanges = getDARelatedChangeList(context, args, relationship, changeType);
            for (Object relatedChange : relatedChanges) {
                Map relatedMap = (Map) relatedChange;
                relatedConnectionIds.put(UIUtil.getValue(relatedMap, SELECT_ID),
                        UIUtil.getValue(relatedMap, DomainRelationship.SELECT_ID));
            }
            if (!"add".equals(mode) && !"remove".equals(mode)) {
                throw new FrameworkException(invalidMessage);
            }
            if (changeIds != null) {
                for (String changeId : changeIds) {
                    if (UIUtil.isNullOrEmpty(changeId)) {
                        continue;
                    }
                    if ("add".equals(mode)) {
                        Map changeInfo = DomainObject.newInstance(context, changeId).getInfo(context,
                                StringList.create(SELECT_TYPE,
                                        "from[" + RELATIONSHIP_JFCHANGE2PROJECT + "].to.id"));
                        if (!changeType.equals(UIUtil.getValue(changeInfo, SELECT_TYPE))
                                || !projectId.equals(UIUtil.getValue(changeInfo,
                                "from[" + RELATIONSHIP_JFCHANGE2PROJECT + "].to.id"))) {
                            throw new FrameworkException(invalidMessage);
                        }
                    } else if (!relatedConnectionIds.containsKey(changeId)) {
                        throw new FrameworkException(invalidMessage);
                    }
                }
            }
            ContextUtil.startTransaction(context, true);
            transactionStarted = true;
            ContextUtil.pushContext(context);
            contextPushed = true;
            if (changeIds != null) {
                for (String changeId : changeIds) {
                    if (UIUtil.isNullOrEmpty(changeId)) {
                        continue;
                    }
                    if ("add".equals(mode)) {
                        DomainObject changeObj = DomainObject.newInstance(context, changeId);
                        if (!relatedConnectionIds.containsKey(changeId)) {
                            DomainRelationship.connect(context, changeObj, relationship, daObj);
                        }
                    } else {
                        String connectionId = relatedConnectionIds.get(changeId);
                        if (UIUtil.isNotNullAndNotEmpty(connectionId)) {
                            DomainRelationship.disconnect(context, connectionId);
                        }
                    }
                }
            }
            ContextUtil.commitTransaction(context);
            transactionStarted = false;
            result.put("code", "200");
            result.put("mess", "");
        } catch (Exception e) {
            if (transactionStarted) {
                ContextUtil.abortTransaction(context);
            }
            result.put("code", "404");
            result.put("mess", e.getMessage());
        } finally {
            if (contextPushed) {
                ContextUtil.popContext(context);
            }
        }
        return result;
    }

    /**
     * 按指定关系获取DA关联的变更对象
     **
     * @param context
     * @param args 参数中包含DA对象ID
     * @param relationship DA关联关系
     * @param changeType DR或正式ECR类型
     * @return MapList 关联对象及关系信息
     * @throws Exception
     * @author caipan
     * @date 2026/7/24 16:13
     */
    private MapList getDARelatedChangeList(Context context, String[] args,
                                           String relationship, String changeType) throws Exception {
        Map params = JPO.unpackArgs(args);
        String daId = UIUtil.getValue(params, "daId");
        if (UIUtil.isNullOrEmpty(daId)) {
            daId = UIUtil.getValue(params, STRING_OBJECTID);
        }
        if (UIUtil.isNullOrEmpty(daId)) {
            return new MapList();
        }
        DomainObject daObj = DomainObject.newInstance(context, daId);
        return daObj.getRelatedObjects(context,
                relationship,
                changeType,
                JF_Util_mxJPO.basicBolistSel(),
                JF_Util_mxJPO.basicRellistSel(),
                true,
                false,
                (short) 1,
                "",
                "",
                0);
    }

    /**
     * DA开始终止时间修改权限
     * @param context
     * @param args
     * @author lsa
     * @throws
     * @return void
     */
    public Boolean editAccessStartAndEndTime(Context context, String[] args) throws Exception {
        Boolean flag = false;
        try {
            Map argsMaps = JPO.unpackArgs(args);
            relsel.add(DomainRelationship.SELECT_ID);
            Map requestMap = (Map) argsMaps.get(STRING_REQUESTMAP);
            String objectId = (String) requestMap.get(STRING_OBJECTID);
            DomainObject daObj = new DomainObject(objectId);
            // 如果状态在审核才能编辑
            State currentState = daObj.getCurrentState(context);
            if(STATE_IN_WORK.equalsIgnoreCase(currentState.getName()) || "Close".equalsIgnoreCase(currentState.getName()) || "DAImplementationPlan".equalsIgnoreCase(currentState.getName()) || "Implement".equalsIgnoreCase(currentState.getName())){
                 return flag;
            }
            //Modification from ljr   -20250207
            Map map = daObj.getRelatedObject(context, DomainRelationship.RELATIONSHIP_OBJECT_ROUTE, true, bosel, relsel);
            String routeId = (String) map.get(DomainConstants.SELECT_ID);
            if(UIUtil.isNotNullAndNotEmpty(routeId)){
                DomainObject routeObj = DomainObject.newInstance(context, routeId);
//                String tileMess = EnoviaResourceBundle.getProperty(context, SUITE_KEY, context.getLocale(), "emxComponents.DARouteInfo.TitleMessage");
                String projectManageZh = EnoviaResourceBundle.getProperty(context, SUITE_KEY, Locale.CHINA, "emxComponents.DARouteInfo.TitleMessage.ProjectManage");
                String projectManageUs = EnoviaResourceBundle.getProperty(context, SUITE_KEY, Locale.US, "emxComponents.DARouteInfo.TitleMessage.ProjectManage");
                //需要增加兼容 liujr 20260624  兼容旧的和新的流程title设置
                String projectManageZh_old = EnoviaResourceBundle.getProperty(context, SUITE_KEY, Locale.CHINA, "emxComponents.DARouteInfo.TitleMessage.ProjectManage_Old");
                String projectManageUs_old = EnoviaResourceBundle.getProperty(context, SUITE_KEY, Locale.US, "emxComponents.DARouteInfo.TitleMessage.ProjectManage_Old");
                relsel.add(DomainConstants.SELECT_ATTRIBUTE_TITLE);
                MapList mapList = routeObj.getRelatedObjects(context,
                        DomainRelationship.RELATIONSHIP_ROUTE_NODE,
                        DomainConstants.TYPE_PERSON,
                        selList,
                        relsel,
                        false,
                        true,
                        (short) 1,
                        "",
                        "",
                        0
                );
                if (mapList.isEmpty()) {
                    return flag;
                }
                StringList resultList = (StringList)mapList.stream().filter(m ->{
                    Map map1 = (Map)m;
                    String strTitle = UIUtil.getValue(map1, SELECT_ATTRIBUTE_TITLE);
                    return strTitle.contains(projectManageUs) || strTitle.contains(projectManageZh) || strTitle.contains(projectManageUs_old) || strTitle.contains(projectManageZh_old);
                }).map(m1 -> {
                    Map map1 = (Map) m1;
                    return UIUtil.getValue(map1, SELECT_NAME);
                }).collect(Collectors.toCollection(StringList::new));
                if (resultList.isEmpty()) {
                    return flag;
                }
                // 获取审批节点项目经理-id
                String personName = resultList.get(0);
                if(context.getUser().equalsIgnoreCase(personName)){
                    flag = true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
        return flag;
    }


    /**
     * 修改JSAddressee属性
     * @param context
     * @param args
     * @return
     * @throws Exception
     */
    public void updateDaAttributeTime(Context context, String[] args) throws Exception {
        try {
            Map map = (Map) JPO.unpackArgs(args);
            _logger.info("map：{}",map.toString());
            Map paramMap = (Map) map.get("paramMap");
            Map fieldMap = (Map) map.get("fieldMap");
            Map requestMap = (Map) map.get("requestMap");
            String objectId = (String) paramMap.get(STRING_OBJECTID);
            String newValue = (String) paramMap.get("New Value");
            String attr = (String) fieldMap.get("name");
            ContextUtil.pushContext(context);
            DomainObject domainObject = DomainObject.newInstance(context, objectId);
            TimeZone var20 = TimeZone.getTimeZone(context.getSession().getTimezone());
//            if (ProgramCentralUtil.isNotNullString(newValue)) {
//                double var21 = -1.0 * (double)var20.getRawOffset();
//                double var23 = new Double(var21 / 3600000.0);
//                HashMap var5 = (HashMap)map.get("requestMap");
//                Locale var9 = (Locale)var5.get("locale");
//                if (null == var9) {
//                    var9 = (Locale)var5.get("localeObj");
//                }
//                var7 = eMatrixDateFormat.getFormattedInputDate(newValue, var23, var9);
//            }
            double iClientTimeOffset = (Double.parseDouble((String) requestMap.get("timeZone")));
            //前台时间转换为数据库时间
            newValue = eMatrixDateFormat.getFormattedInputDate(newValue, iClientTimeOffset, context.getLocale());
            domainObject.setAttributeValue(context, attr, newValue);
            ContextUtil.popContext(context);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 校验并保存DA延期关闭时间
     **
     * @param context
     * @param args 表单字段更新参数
     * @return void
     * @throws Exception 延期次数达到上限、基准日期无效或延期日期不符合规则时抛出异常
     * @author caipan
     * @date 2026/8/4
     */
    public void updateDAExtensionTime(Context context, String[] args) throws Exception {
        Map argsMap = JPO.unpackArgs(args);
        Map paramMap = (Map) argsMap.get(STRING_PARAMMAP);
        Map requestMap = (Map) argsMap.get(STRING_REQUESTMAP);
        String objectId = (String) paramMap.get(STRING_OBJECTID);
        String newValue = (String) paramMap.get("New Value");
        DomainObject daObject = DomainObject.newInstance(context, objectId);
        if (UIUtil.isNullOrEmpty(newValue)) {
            String message = EnoviaResourceBundle.getProperty(context, SUITE_KEY, context.getLocale(),
                    "emxComponents.JFDA.ExtensionDateRequired");
            throw new Exception(message);
        }

        String delayCountValue = daObject.getAttributeValue(context, ATTRIBUTE_JFDADELAYCOUNT);
        int delayCount = UIUtil.isNullOrEmpty(delayCountValue) ? 0 : Integer.parseInt(delayCountValue);
        if (delayCount >= 2) {
            String message = EnoviaResourceBundle.getProperty(context, SUITE_KEY, context.getLocale(),
                    "emxComponents.JFDA.DelayCountLimit");
            throw new Exception(message);
        }

        String baseAttribute = delayCount == 0 ? ATTRIBUTE_JFDACLOSETIME : ATTRIBUTE_JFDAEXTENSIONTIME;
        String baseDateValue = daObject.getAttributeValue(context, baseAttribute);
        if (UIUtil.isNullOrEmpty(baseDateValue)) {
            String message = EnoviaResourceBundle.getProperty(context, SUITE_KEY, context.getLocale(),
                    "emxComponents.JFDA.ExtensionBaseDateRequired");
            throw new Exception(message);
        }

        String formattedNewValue;
        Date baseDate;
        Date extensionDate;
        try {
            double clientTimeOffset = Double.parseDouble((String) requestMap.get("timeZone"));
            formattedNewValue = eMatrixDateFormat.getFormattedInputDate(newValue, clientTimeOffset,
                    context.getLocale());
            baseDate = eMatrixDateFormat.getJavaDate(baseDateValue);
            extensionDate = eMatrixDateFormat.getJavaDate(formattedNewValue);
        } catch (Exception exception) {
            String message = EnoviaResourceBundle.getProperty(context, SUITE_KEY, context.getLocale(),
                    "emxComponents.JFDA.ExtensionDateInvalid");
            throw new Exception(message, exception);
        }
        if (!extensionDate.after(baseDate)) {
            String message = EnoviaResourceBundle.getProperty(context, SUITE_KEY, context.getLocale(),
                    "emxComponents.JFDA.ExtensionDateAfterBase");
            throw new Exception(message);
        }

        Calendar maximumDate = Calendar.getInstance();
        maximumDate.setTime(baseDate);
        maximumDate.add(Calendar.DAY_OF_MONTH, 30);
        if (extensionDate.after(maximumDate.getTime())) {
            String message = EnoviaResourceBundle.getProperty(context, SUITE_KEY, context.getLocale(),
                    "emxComponents.JFDA.ExtensionDateWithinThirtyDays");
            throw new Exception(message);
        }

        boolean isPush = false;
        try {
            ContextUtil.pushContext(context);
            isPush = true;
            daObject.setAttributeValue(context, ATTRIBUTE_JFDAEXTENSIONTIME, formattedNewValue);
        } finally {
            if (isPush) {
                ContextUtil.popContext(context);
            }
        }
    }

    /**
     * DA标题关联项目 影响工厂 变更类型编辑权限 只有草稿阶段owner才能编辑
     * @param context
     * @param args
     * @author lsa
     * @throws
     * @return void
     */
    public Boolean editAccessDAAttribute(Context context, String[] args) {
        Boolean flag = false;
        try {
            Map argsMaps = JPO.unpackArgs(args);
            Map requestMap = (Map) argsMaps.get(STRING_REQUESTMAP);
            String objectId = (String) requestMap.get(STRING_OBJECTID);
            DomainObject daObj = new DomainObject(objectId);
            // 如果状态在审核才能编辑
            State currentState = daObj.getCurrentState(context);
            User user = daObj.getOwner(context);
            String userName = user.getName();
            String contextUser = context.getUser();
            if(STATE_IN_WORK.equalsIgnoreCase(currentState.getName()) && userName.equalsIgnoreCase(contextUser)){
                flag = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return flag;
    }



    /**
     * DA 零件清单新增零件和移除零件按钮只能owner 和项目经理在草稿阶段添加移除
     * @param context
     * @param args
     * @author lsa
     * @throws
     * @return void
     */
    public Boolean access2AddRemovePart(Context context, String[] args) {
        Boolean flag = false;
        String owner = "";
        try {
            Map argsMaps = JPO.unpackArgs(args);
            String objectId = (String) argsMaps.get(STRING_OBJECTID);
            DomainObject daObj = new DomainObject(objectId);
            State currentState = daObj.getCurrentState(context);
            User user = daObj.getOwner(context);
            String userName = user.getName();
            String contextUser = context.getUser();
            // 草稿状态并且只有owner可以查看
            if(STATE_IN_WORK.equalsIgnoreCase(currentState.getName()) && userName.equalsIgnoreCase(contextUser)){
                flag = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return flag;
    }

    /**
     * 新建执行计划以及删除执行计划权限显示
     * @param context
     * @param args
     * @author lsa
     * @throws
     * @return void
     */
    public Boolean access2ExecutePlan(Context context, String[] args) throws Exception {
        Boolean flag = false;
        try {
            Map argsMaps = JPO.unpackArgs(args);
            String objectId = (String) argsMaps.get(STRING_OBJECTID);
            DomainObject daObj = new DomainObject(objectId);
            State currentState = daObj.getCurrentState(context);
            Map map = daObj.getRelatedObject(context, RELATIONSHIP_JFCHANGE2PROJECT, true, bosel, relsel);
            if(null != map){
                String projectId = (String) map.get(DomainConstants.SELECT_ID);
                if(UIUtil.isNotNullAndNotEmpty(projectId)){
//                    DomainObject projectObj = DomainObject.newInstance(context, projectId);
                    String projectManager = JF_Util_mxJPO.getProjectManager(context, new String[]{projectId});
//                    User user = projectObj.getOwner(context);
                    // 获取项目经理id
                    String contextUser = context.getUser();
                    //只有状态在执行计划并且项目经理能创建删除任务
                    if(contextUser.equalsIgnoreCase(projectManager) && "DAImplementationPlan".equalsIgnoreCase(currentState.getName())){
                        flag = true;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
        return flag;
    }

    /**
     * 整椅经理是否可编辑权限
     * @param context
     * @param args
     * @author lsa
     * @throws
     * @return void
     */
    public Boolean accessChairEdit(Context context, String[] args) throws Exception {
        Boolean flag = false;
        Map argsMaps = JPO.unpackArgs(args);
        Map requestMap = (Map) argsMaps.get(STRING_REQUESTMAP);
        String objectId = (String) requestMap.get(STRING_OBJECTID);
        DomainObject object = DomainObject.newInstance(context, objectId);
        String current = object.getCurrentState(context).getName();
        // 整椅经理只有在DA草稿或者审核状态才能编辑
        if(STATE_IN_WORK.equalsIgnoreCase(current))flag = true;
        return false;
    }

    /**
     * @description 修改整椅经理
     * @param context
     * @param args
     * @author lsa
     * @throws
     * @return void
     */
    public void updateChairManager(Context context, String[] args) throws Exception {
        Map argMaps = JPO.unpackArgs(args);
        Map paramMap = (Map) argMaps.get(STRING_PARAMMAP);
        String objectId = (String) paramMap.get(DomainObject.SELECT_ID);
        String newId = (String) paramMap.get(STRING_NEW_OID);
        DomainObject object = new DomainObject(objectId);
        Map chairMaps = object.getRelatedObject(context, RELATIONSHIP_JFDRCHAIRMANGER2PERSON, true, bosel, relsel);
        if(null != chairMaps){
            String connectId = (String) chairMaps.get("id[connection]");
            // 断掉DA与整椅经理之前的关系
            DomainRelationship.disconnect(context,connectId);
        }
        // 如果没填整椅经理直接建立关系
        DomainRelationship.connect(context,object,RELATIONSHIP_JFDRCHAIRMANGER2PERSON,new DomainObject(newId));
    }

    /**
     * @description 创建DA延期流程申请，延期次数达到两次时禁止创建
     * @param context
     * @param args
     * @author lsa
     * @date 2026/8/4
     * @throws
     * @return void
     */
    public void createJFDAExtensionRoute(Context context, String[] args) throws Exception {
        Map argsMaps = JPO.unpackArgs(args);
        List<String> list = new ArrayList<>();
        Map requestMap = (Map) argsMaps.get(STRING_REQUESTMAP);
        String projectId = "";
        String routeId = DomainConstants.EMPTY_STRING;
        String objectId = (String) requestMap.get(STRING_OBJECTID);
        DomainObject daObj = new DomainObject(objectId);
        //20260804 update by ljr 达到两次延期上限后禁止绕过按钮继续创建延期流程
        String delayCountValue = daObj.getAttributeValue(context, ATTRIBUTE_JFDADELAYCOUNT);
        int delayCount = UIUtil.isNullOrEmpty(delayCountValue) ? 0 : Integer.parseInt(delayCountValue);
        if (delayCount >= 2) {
            String message = EnoviaResourceBundle.getProperty(context, SUITE_KEY, context.getLocale(),
                    "emxComponents.JFDA.DelayCountLimit");
            throw new Exception(message);
        }
        //20260819 update by caipan 防止存在未完成延期流程时重复创建
        Map accessMap = new HashMap();
        accessMap.put(STRING_OBJECTID, objectId);
        if (!delayProcessAccess(context, JPO.packArgs(accessMap))) {
            String message = EnoviaResourceBundle.getProperty(context, SUITE_KEY, context.getLocale(),
                    "emxComponents.JFDA.DelayRouteInProgress");
            throw new Exception(message);
        }
        daObj.setAttributeValue(context,"JFDAIsDelay","TRUE");
        // 获取直线经理名称
        String lineManager = context.getUser();
        JF_PublicMethodClass_mxJPO jfPublicMethodClassMxJPO = new JF_PublicMethodClass_mxJPO();
        String lineManagerID = jfPublicMethodClassMxJPO.getPersonLineManager(context, null, lineManager);
        list.add(lineManagerID);
        // 获取所关联的项目
        Map map = daObj.getRelatedObject(context, RELATIONSHIP_JFCHANGE2PROJECT, true, bosel, relsel);
        if(null != map){
            projectId = (String) map.get(DomainConstants.SELECT_ID);
        }
        // 判断整椅经理是否和直线经理是否为一个人
        String chairManagerName = getChairManagerName(context, projectId);
        String chairManagerId ="";
        if(!"".equalsIgnoreCase(chairManagerName)){
             chairManagerId = PersonUtil.getPersonObjectID(context, chairManagerName);
            if(!list.contains(chairManagerId)) list.add(chairManagerId);
        }

//        Map chairMaps = daObj.getRelatedObject(context, RELATIONSHIP_JFDRCHAIRMANGER2PERSON, true, bosel, relsel);
//        if(null != chairMaps){
//            chairManagerId = (String) chairMaps.get(DomainConstants.SELECT_ID);
//            if(!list.contains(chairManagerId)) list.add(chairManagerId);
//        }
        // 部门负责人Id
        String DepartmentHeadOID = (String) requestMap.get("DepartmentHeadOID");
        if(!"".equalsIgnoreCase(DepartmentHeadOID)) list.add(DepartmentHeadOID);
        // 项目总监Id
        String ProjectManagerOID = (String) requestMap.get("ProjectManagerOID");
        if(!"".equalsIgnoreCase(ProjectManagerOID)) list.add(ProjectManagerOID);

        //创建流程
        JF_Route_mxJPO jf_route = new JF_Route_mxJPO(context, args);
        //组装审批人员
        MapList approveList = new MapList();
        //流程标题
        String tileMess = EnoviaResourceBundle.getProperty(context, SUITE_KEY, context.getLocale(), "emxComponents.DAExtensionRouteInfo.TitleMessage");
        String ProjectDirct = EnoviaResourceBundle.getProperty(context, SUITE_KEY, context.getLocale(), "emxComponents.DARouteInfo.TitleMessage.ProjectDirct");
        String DepHeader = EnoviaResourceBundle.getProperty(context, SUITE_KEY, context.getLocale(), "emxComponents.DARouteInfo.TitleMessage.DepHeader");
        String WholeChair = EnoviaResourceBundle.getProperty(context, SUITE_KEY, context.getLocale(), "emxComponents.DARouteInfo.TitleMessage.WholeChair");
        String LineManage = EnoviaResourceBundle.getProperty(context, SUITE_KEY, context.getLocale(), "emxComponents.DARouteInfo.TitleMessage.LineManage");

        for (int i = 0; i < list.size(); i++) {
            String tileMessRoleStr = "";
            String id  = list.get(i);
            if(ProjectManagerOID.equalsIgnoreCase(id)){
                tileMessRoleStr = tileMess+"-"+ProjectDirct;
            }
            else if(DepartmentHeadOID.equalsIgnoreCase(id)){
                tileMessRoleStr = tileMess+"-"+DepHeader;
            }
            else if(lineManagerID.equalsIgnoreCase(id)){
                tileMessRoleStr = tileMess+"-"+LineManage;
            }
            else if(chairManagerId.equalsIgnoreCase(id)){
                tileMessRoleStr = tileMess+"-"+WholeChair;
            }

            Map nReceiverMap = (Map) JF_PublicMethodClass_mxJPO.getMapAnyOrAll(id, tileMessRoleStr, "true", String.valueOf(i+1), "All");
            approveList.add(nReceiverMap);
        }

        String state = STATE_POLICY_JFDA_IMPLEMENT;
        String policy = POLICY_JFDA;
        String routeDescription = tileMess;
        routeId = jf_route.createAndStartRouteForNotify(context, approveList, objectId, state, policy, routeDescription);
        logger.info("ExtensionRouteId:{}",routeId);
        //20260819 update by caipan 首次创建延期流程时保存首次延期时间
        if (delayCount == 0) {
            String extensionTime = daObj.getAttributeValue(context, ATTRIBUTE_JFDAEXTENSIONTIME);
            daObj.setAttributeValue(context, ATTRIBUTE_JFDAEXTENSIONTIMEBAK, extensionTime);
        }
    }


    /**
     * @description 获取DA所关联的所有的物理对象
     * @param context
     * @param args
     * @author lsa
     */
    public MapList getAllVPMReference(Context context, String[] args){
        MapList allVPMReferenceMapList = getAllVPMReferenceMapList(context, args);
        return allVPMReferenceMapList;
    }

    /**
     * @description 排除DA有关联的所有物理对象，防止一个DA对象
     *              连接两个同样物理对象
     * @param context
     * @param args
     * @author lsa
     */
    public StringList getExcludeAllVPMReference(Context context, String[] args){
        StringList list = new StringList();
        try {
            MapList allVPMMapList = getAllVPMReferenceMapList(context, args);
            if(!allVPMMapList.isEmpty()){
                //遍历拿到的所有DA关联对象数组
                for (int i = 0; i < allVPMMapList.size(); i++) {
                    Map resMap = (Map) allVPMMapList.get(i);
                    String id = (String) resMap.get("id");
                    list.add(id);
                }
            }

            MapList mapList = DomainObject.findObjects(context, "JF_CompetitiveBOM", STRING_SYMB_ASTERISK, "", selList);
            for (int i = 0; i < mapList.size(); i++) {
                Map map = (Map) mapList.get(i);
                String id = (String) map.get("id");
                list.add(id);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return list;
    }


    /**
     * @description 公共方法：获取DA有关联的所有物理对象
     * @param context
     * @param args
     * @author lsa
     */
    public MapList getAllVPMReferenceMapList(Context context, String[] args){
        MapList resMapList;
        try {
            Map paramMap = (Map) JPO.unpackArgs(args);
            String objectId = (String) paramMap.get(STRING_OBJECTID);
            DomainObject object = DomainObject.newInstance(context, objectId);
            //获取零件清单集合
            resMapList = object.getRelatedObjects(context, RELATIONSHIP_DA2VPM, TYPE_VPMREFERENCE, bosel, relsel, false, true, (short) 0, "", "", 0);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return resMapList;
    }

    /**
     * DA提升审核前校验直线经理、零件和文档
     **
     * @param context
     * @param args Trigger参数，第一个参数为DA对象ID
     * @return int 校验通过返回0，校验失败返回1
     * @throws Exception
     * @author lsa
     * @date 2026/7/24
     */
    public int createDARouteProcessCheck(Context context, String[] args) throws Exception {
        String objectId = args[0];
        DomainObject daObj = new DomainObject(objectId);
        StringList relatedChangeSelects = StringList.create(
                "to[" + RELATIONSHIP_JFDR2DA + "]",
                "to[" + RELATIONSHIP_JFECR2DA + "]");
        Map relatedChangeInfo = daObj.getInfo(context, relatedChangeSelects);
        boolean hasRelatedChange = "TRUE".equalsIgnoreCase(UIUtil.getValue(relatedChangeInfo,
                "to[" + RELATIONSHIP_JFDR2DA + "]"))
                || "TRUE".equalsIgnoreCase(UIUtil.getValue(relatedChangeInfo,
                "to[" + RELATIONSHIP_JFECR2DA + "]"));
        MapList partMapList = new MapList();
        MapList documentList = new MapList();
        logger.info("hasRelatedChange:{} relatedChangeInfo:{}",hasRelatedChange,relatedChangeInfo);
        if (!hasRelatedChange) {
            partMapList = daObj.getRelatedObjects(context, RELATIONSHIP_DA2VPM, TYPE_VPMREFERENCE,
                    bosel, relsel, false, true, (short) 0, "", "", 0);
            documentList = daObj.getRelatedObjects(context, "Reference Document", "Document",
                    bosel, relsel, false, true, (short) 0, "", "", 0);
        }
        // 获取直线经理名称
        String lineManager = context.getUser();
        JF_PublicMethodClass_mxJPO jfPublicMethodClassMxJPO = new JF_PublicMethodClass_mxJPO();
        String lineManagerID = jfPublicMethodClassMxJPO.getPersonLineManager(context, null, lineManager);
        // 判断当前登录用户直线经理不能为空
        String lineManagerIsEmpty = EnoviaResourceBundle.getProperty(context, SUITE_KEY, context.getLocale(), "emxComponents.Notice.LineManagerIsNotEmpty");
        String PartListIsEmpty = EnoviaResourceBundle.getProperty(context, SUITE_KEY, context.getLocale(), "emxComponents.Notice.PartListIsEmpty");
        String docListIsEmpty = EnoviaResourceBundle.getProperty(context, SUITE_KEY, context.getLocale(), "emxComponents.Notice.DocListIsEmpty");
        if("".equalsIgnoreCase(lineManagerID)){
            emxContextUtil_mxJPO.mqlNotice(context, lineManagerIsEmpty);
            return 1;
        }
        //提升状态如果没有加零件则不让提升
        if(!hasRelatedChange && partMapList.isEmpty()){
            emxContextUtil_mxJPO.mqlNotice(context, PartListIsEmpty);
            return 1;
        }

        //提升状态如果没有添加附件不让提升
        if(!hasRelatedChange && documentList.isEmpty()){
            emxContextUtil_mxJPO.mqlNotice(context, docListIsEmpty);
            return 1;
        }

        return 0;
    }

    /**
     * @description DA提升到执行、关闭状态需要项目经理去提升
     * @param context
     * @param args
     * @author lsa
     */
    public int promoteProjectManagerCheck(Context context, String[] args) throws Exception {
        String owner = "";
        String objectId = args[0];
        bosel.add("owner");
        bosel.add("attribute[Task Estimated Finish Date]");
        String whetherProjectManager = EnoviaResourceBundle.getProperty(context, SUITE_KEY, context.getLocale(), "emxComponents.Notice.whetherProjectManager");
        String whetherHasTask = EnoviaResourceBundle.getProperty(context, SUITE_KEY, context.getLocale(), "emxComponents.Notice.whetherHasTask");
        String whetherSubTask = EnoviaResourceBundle.getProperty(context, SUITE_KEY, context.getLocale(), "emxComponents.Notice.whetherSubTask");
        String EstimatedDateMessage = EnoviaResourceBundle.getProperty(context, SUITE_KEY, context.getLocale(), "emxComponents.Notice.EstimatedDateMessage");
        DomainObject daObj = new DomainObject(objectId);
        State currentState = daObj.getCurrentState(context);

        Map map = daObj.getRelatedObject(context, RELATIONSHIP_JFCHANGE2PROJECT, true, bosel, relsel);
        MapList mapList = daObj.getRelatedObjects(context, RELATIONSHIP_JFDA2JFDATASK, TYPE_JF_DATASK, bosel, relsel, false, true, (short) 0, "", "", 0);
        if(null != map){
            owner = JF_Util_mxJPO.getProjectManager(context, new String[]{ (String) map.get("id")});
//            owner = (String) map.get("owner");
        }

        //判断登录用户是否项目经理
        String contextUser = context.getUser();
        if(!contextUser.equalsIgnoreCase(owner)){
            emxContextUtil_mxJPO.mqlNotice(context, whetherProjectManager);
            return 1;
        }

        //如果没有创建任务则不能提升执行
        if("DAImplementationPlan".equalsIgnoreCase(currentState.getName())){
            if(mapList.isEmpty()){
                emxContextUtil_mxJPO.mqlNotice(context, whetherHasTask);
                return 1;
            }
            //如果计划完成时间没有填写不让提升
            Map map1 =null;
            for(int i=0;i<mapList.size();i++){
                map1 = (Map)mapList.get(i);
                String EstimatedStartDate = UIUtil.getValue(map1,"attribute[Task Estimated Finish Date]");
                if(UIUtil.isNullOrEmpty(EstimatedStartDate)){
                    emxContextUtil_mxJPO.mqlNotice(context, EstimatedDateMessage);
                    return 1;
                }
            }
        }

        // 如果任务没完成则不能提升到关闭
        int total = 0;
        if("Implement".equalsIgnoreCase(currentState.getName())){
            for (int i = 0; i < mapList.size(); i++) {
                Map taskMaps = (Map) mapList.get(i);
                String current = (String) taskMaps.get("current");
                if("Review".equalsIgnoreCase(current)){
                    total++;
                }
            }

            if(total != mapList.size()){
                emxContextUtil_mxJPO.mqlNotice(context, whetherSubTask);
                return 1;
            }
        }

        return 0;
    }

    /**
     * @description DA提升审核创建审核流程对象且关联相关零件
     * @param context
     * @param args
     * @author lsa
     */
    public void createDARouteProcess(Context context, String[] args) throws Exception {
        try {
            //审核对象Id
            String objectId = args[0];
            String routeId = DomainConstants.EMPTY_STRING;
            String chairManagerId = DomainConstants.EMPTY_STRING;
            DomainObject daObj = DomainObject.newInstance(context, objectId);
            // 获取直线经理名称
            String lineManager = context.getUser();
            JF_PublicMethodClass_mxJPO jfPublicMethodClassMxJPO = new JF_PublicMethodClass_mxJPO();
            String lineManagerID = jfPublicMethodClassMxJPO.getPersonLineManager(context, null, lineManager);

            // 获取DA对象所关联的项目id
            Map map = daObj.getRelatedObject(context, RELATIONSHIP_JFCHANGE2PROJECT, true, bosel, relsel);
            String projectId = (String) map.get(DomainConstants.SELECT_ID);
            //获取整椅经理对象
            String chairManagerName = getChairManagerName(context, projectId);
            if(!"".equalsIgnoreCase(chairManagerName)){
                chairManagerId = PersonUtil.getPersonObjectID(context, chairManagerName);
            }
            if(UIUtil.isNotNullAndNotEmpty(projectId)){
                DomainObject projectObj = DomainObject.newInstance(context, projectId);
                User user = projectObj.getOwner(context);
                // 获取项目经理id
                String projectManagerId = PersonUtil.getPersonObjectID(context, JF_Util_mxJPO.getProjectManager(context, new String[]{projectId}));
                //创建流程
                JF_Route_mxJPO jf_route = new JF_Route_mxJPO(context, args);
                //组装审批人员
                MapList approveList = new MapList();
                //流程标题
                String tileMess = EnoviaResourceBundle.getProperty(context, SUITE_KEY, context.getLocale(), "emxComponents.DARouteInfo.TitleMessage");
                String WholeChair = EnoviaResourceBundle.getProperty(context, SUITE_KEY, context.getLocale(), "emxComponents.DARouteInfo.TitleMessage.WholeChair");
                String ProjectManage = EnoviaResourceBundle.getProperty(context, SUITE_KEY, context.getLocale(), "emxComponents.DARouteInfo.TitleMessage.ProjectManage");
                String LineManage = EnoviaResourceBundle.getProperty(context, SUITE_KEY, context.getLocale(), "emxComponents.DARouteInfo.TitleMessage.LineManage");

                // 如果直线经理和整椅经理不是同一个人
                if(!lineManagerID.equalsIgnoreCase(chairManagerId) && !DomainConstants.EMPTY_STRING.equalsIgnoreCase(chairManagerId)){
                    //审批人 直线经理
                    Map nReceiverMapOne = (Map) JF_PublicMethodClass_mxJPO.getMapAnyOrAll(lineManagerID, tileMess+"-"+LineManage, "true", "1", "All");//设置审批信息 标题
                    approveList.add(nReceiverMapOne);

                    //审批人 整椅经理
                    Map nReceiverMapTwo = (Map) JF_PublicMethodClass_mxJPO.getMapAnyOrAll(chairManagerId, tileMess+"-"+WholeChair, "true", "2", "All");//设置审批信息 标题
                    approveList.add(nReceiverMapTwo);

                    //审批人 项目经理
                    Map nReceiverMapEnd = (Map) JF_PublicMethodClass_mxJPO.getMapAnyOrAll(projectManagerId, tileMess+"-"+ProjectManage, "true", "3", "All");//设置审批信息 标题
                    approveList.add(nReceiverMapEnd);
                }else {
                    //审批人 直线经理
                    Map nReceiverMapOne = (Map) JF_PublicMethodClass_mxJPO.getMapAnyOrAll(lineManagerID, tileMess+"-"+LineManage, "true", "1", "All");//设置审批信息 标题
                    approveList.add(nReceiverMapOne);

                    //审批人 项目经理
                    Map nReceiverMapEnd = (Map) JF_PublicMethodClass_mxJPO.getMapAnyOrAll(projectManagerId, tileMess+"-"+ProjectManage, "true", "2", "All");//设置审批信息 标题
                    approveList.add(nReceiverMapEnd);
                }
                String state = STATE_POLICY_JFDA_Approve;
                String policy = POLICY_JFDA;
                String routeDescription = tileMess;
                routeId = jf_route.createAndStartRoute(context, approveList, objectId, state, policy, routeDescription);
                logger.info("routeId:{}",routeId);
            }
        }catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @description DA状态提升到DA执行计划判断DA启动时间和DA关闭时间是否维护
     * @param context
     * @param args
     * @author lsa
     */
    public int checkDAAttIsEmpty(Context context, String[] args) throws Exception {
        String objectId = args[0];
        DomainObject object = new DomainObject(objectId);
        String attStart = object.getAttributeValue(context, ATTRIBUTE_JFDASTARTTIME);
        String attClose = object.getAttributeValue(context, ATTRIBUTE_JFDACLOSETIME);
        StringBuilder builder = new StringBuilder();
        String attStartIsEmpty = EnoviaResourceBundle.getProperty(context, SUITE_KEY, context.getLocale(), "emxComponents.DA.attStartIsEmpty");
        String attCloseIsEmpty = EnoviaResourceBundle.getProperty(context, SUITE_KEY, context.getLocale(), "emxComponents.DA.attCloseIsEmpty");
        if(UIUtil.isNullOrEmpty(attStart)){
            builder.append(object.getName()).append(":").append(attStartIsEmpty).append("\n");
        }
        
        if (UIUtil.isNullOrEmpty(attClose)){
            builder.append(object.getName()).append(":").append(attCloseIsEmpty);
        }

        if(builder.length() > 0 ){
            emxContextUtil_mxJPO.mqlNotice(context, builder.toString());
            return 1;
        }
        return 0;
    }

    /**
     * @description DA更新关联项目对象
     * @param context
     * @param args
     * @author lsa
     */
    public void updateDaAttProcess(Context context, String[] args){
        try {
            Map argsMap = (Map) JPO.unpackArgs(args);
            Map paramMap = (Map) argsMap.get(STRING_PARAMMAP);
            //获取重新选取的项目id
            String newOid = (String) paramMap.get("New OID");
            //获取当前DA id
            String objectId = (String) paramMap.get(STRING_OBJECTID);
            // 获取修改后的项目名称
            String newValue = (String) paramMap.get("New Value");
            DomainObject daObj = DomainObject.newInstance(context, objectId);
            //查询到有关联的项目
            Map map = daObj.getRelatedObject(context, RELATIONSHIP_JFCHANGE2PROJECT, true, bosel, relsel);
            String connectId = (String) map.get("id[connection]");
            daObj.setAttributeValue(context,"JFProjectName",newValue);
            //断掉修改前的关系
            DomainRelationship.disconnect(context,connectId);
            // 连接修改后的项目关系
            DomainRelationship.connect(context, daObj, RELATIONSHIP_JFCHANGE2PROJECT , new DomainObject(newOid));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @description 创建DATask实体对象
     * @param context
     * @param args
     * @author lsa
     */
    public Map createJFDATaskObject(Context context, String[] args) throws FrameworkException {
        logger.info("createJFDATaskObject start");
        Map resultMap = new HashMap();
        try {
            ContextUtil.pushContext(context);
            HashMap argMaps = JPO.unpackArgs(args);
            String responsible = (String) argMaps.get("responsible");
            String objectId = (String) argMaps.get(STRING_OBJECTID);
            String taskName = (String) argMaps.get("Title");
            String taskType = (String) argMaps.get("type");
            Map map = new HashMap();
            map.put("taskName",taskName);//任务标题
            map.put("taskType",taskType);//任务类型 注册名
            map.put("taskOwner",responsible);//任务owner
            map.put("parentId",objectId);
            //创建JFDATask任务实体
            JF_Util_mxJPO jfUtilMxJPO = new JF_Util_mxJPO();
            String taskId = jfUtilMxJPO.createTask(context, JPO.packArgs(map));
            DomainObject taskObj = DomainObject.newInstance(context, taskId);
            // DA与创建的DTask连接关系
            DomainRelationship.connect(context, new DomainObject(objectId),RELATIONSHIP_JFDA2JFDATASK,taskObj);
            resultMap.put("id",taskId);
            resultMap.put("objectId",taskId);
            logger.info("taskId:{}",taskId);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }finally {
            ContextUtil.popContext(context);
        }
        logger.info("createJFDATaskObject end:{}",resultMap);
        return resultMap;
    }



    /**
     * @description 获取工作任务的所有range值
     * @param context
     * @param args
     * @author lsa
     */
    public Map getAllRangeWorkTask(Context context, String[] args) throws MatrixException {
        HashMap<Object, Object> res = new HashMap<>();
        String ranges[] = {"EngineeringAction", "ProcurementAction", "ASQAction", "IEAction", "QualityAction", "LogisticsAction", "GenerateFactoryAction"};
        StringList sortRanges = new StringList(ranges);
        StringList nlsRanges = EnoviaResourceBundle.getAttrRangeI18NStringList(context, "Title", sortRanges, context.getLocale().toString());
        res.put("field_choices", sortRanges);
        res.put("field_display_choices", nlsRanges);
        return res;
    }

    /**
     * @description 获取所有的执行计划任务
     * @param context
     * @param args
     * @author lsa
     */
    public MapList getAllExecutionPlanTask(Context context, String[] args){
        MapList mapList = new MapList();
        try {
            Map argsMap = (Map) JPO.unpackArgs(args);
            String objectId = (String) argsMap.get(STRING_OBJECTID);
            logger.info("objectId:{}",objectId);
            DomainObject daObj = DomainObject.newInstance(context, objectId);
            bosel.add("to[Assigned Tasks].from.name");
            mapList = daObj.getRelatedObjects(context, RELATIONSHIP_JFDA2JFDATASK, TYPE_JF_DATASK, bosel, relsel, false, true, (short) 0, "", "", 0);
            logger.info("getAllExecutionPlanTask:{}",mapList);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return mapList;
    }


    /**
     * @description 获取执行计划任务的责任人
     * @param context
     * @param args
     * @author lsa
     */
    public StringList getResponsible(Context context, String[] args){
        StringList resultList = new StringList();
        try {
            Map argsMaps = JPO.unpackArgs(args);
            MapList resultMap = (MapList) argsMaps.get("objectList");
            for (int i = 0; i < resultMap.size(); i++) {
                Map map = (Map) resultMap.get(i);
                String responsible = (String) map.get("to[Assigned Tasks].from.name");
                resultList.add(responsible);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return resultList;
    }

    /**
     * @description 生成DA执行计划表单中最后一栏按钮
     * @param context
     * @param args
     * @author lsa
     */
    public StringList confirmTaskContent(Context context, String[] args){
        StringList resList = new StringList();
        String submit = EnoviaResourceBundle.getProperty(context, SUITE_KEY, context.getLocale(), "emxComponents.DA.submitButton");
        String Submitted = EnoviaResourceBundle.getProperty(context, SUITE_KEY, context.getLocale(), "emxComponents.DA.submittedButton");
        try {
            HashMap argMaps = JPO.unpackArgs(args);
            MapList objectList = (MapList) argMaps.get("objectList");
            StringBuffer buffer = new StringBuffer();
            for (int i = 0; i < objectList.size(); i++) {
                Map map = (Map) objectList.get(i);
                String id = (String) map.get(DomainConstants.SELECT_ID);
                String parentId = (String) map.get("id[parent]");
                String projectManager = MqlUtil.mqlCommand(context, "print bus " + parentId + " select from[JFChange2Project].to.owner dump |");
                DomainObject object = DomainObject.newInstance(context, id);
                State currentState = object.getCurrentState(context);
                Map responseMap = object.getRelatedObject(context, "Assigned Tasks", false, bosel, relsel);
                if(null==responseMap){
                    resList.add(buffer.toString());
                    buffer.setLength(0);
                }else {
                    String taskResponseName = (String) responseMap.get(DomainConstants.SELECT_NAME);
                    String contextUser = context.getUser();
                    int index = findIndex(stateIndex, currentState.getName());
                    if (contextUser.equalsIgnoreCase(taskResponseName) && index == 2) {
                        buffer.append("<table><td><a target='listHidden' href=\"/3dspace/common/JF_TasKJudgeValAndPromote.jsp?objectId=");
                        buffer.append(id).append("\" >");
                        buffer.append(submit);
                        buffer.append("</a></td></table>");
                    } else if ((contextUser.equalsIgnoreCase(taskResponseName) && index > 2) || (contextUser.equalsIgnoreCase(projectManager) && index > 2)) {
                        buffer.append("<table><td>");
                        buffer.append(Submitted);
                        buffer.append("</td></table>");
                    } else {
                        buffer.append("");
                    }
                }
                resList.add(buffer.toString());
                buffer.setLength(0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return resList;
    }


    /**
     * @description 修改执行计划任务的责任人
     * @param context
     * @param args
     * @author lsa
     */
    public void updateResponsibleFunc(Context context, String[] args) {
        try {
            Map argMaps = JPO.unpackArgs(args);
            Map paramMap = (Map) argMaps.get(STRING_PARAMMAP);
            String objectId = (String) paramMap.get(STRING_OBJECTID);
            String newPersonName = (String) paramMap.get("New Value");
            DomainObject taskObject = DomainObject.newInstance(context, objectId);
            String typeName = taskObject.getInfo(context,DomainConstants.SELECT_TYPE);
            Task task = new Task(objectId);
            MapList mapList = taskObject.getRelatedObjects(context, "Assigned Tasks", "Person", bosel, relsel, true, false, (short) 0, "", "", 0);
            if(!mapList.isEmpty()){
                for (int i = 0; i < mapList.size(); i++) {
                    Map map = (Map) mapList.get(i);
                    String connectionId = (String) map.get("id[connection]");
                    // 断掉原有的责任人
                    task.removeAssignee(context,connectionId);
                }
            }
            String personObjectID = PersonUtil.getPersonObjectID(context, newPersonName);
            JF_Util_mxJPO jfUtilMxJPO = new JF_Util_mxJPO();
            // 分配为修改后的责任人
            jfUtilMxJPO.assignPerson(context, personObjectID,task);

            // 如果是ECOTask DA task  则把任务owner改为责任人
            if(TYPE_JF_ECOTASK.equalsIgnoreCase(typeName) || TYPE_JF_DATASK.equals(typeName)){
                // 设置ECOTask的owner
                taskObject.setOwner(context,newPersonName);
                // 获取组织
                String organization = jfUtilMxJPO.getPersonOrganization(context, newPersonName);
                // 获取项目
                String project = jfUtilMxJPO.getObjectProject(context, objectId);
                // 设置ECOTask的项目和组织
                taskObject.setPrimaryOwnership(context, project, organization);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void updateExecTaskAttrFunc(Context context, String[] args)throws Exception {
        try {
            ContextUtil.pushContext(context);
            Map paramsMap = (Map) JPO.unpackArgs(args);
            logger.info("paramsMap:{}",paramsMap);
            Map columnMap = (Map) paramsMap.get(JF_PLMConstants_mxJPO.STRING_COLUMNMAP);
            Map requestMap = (Map) paramsMap.get("requestMap");
            String strTableName = (String) requestMap.get("selectedTable");
            String strParentOID = (String) requestMap.get("parentOID");
            String strAttrName = (String) columnMap.get(DomainConstants.SELECT_NAME);
            HashMap paramMap = (HashMap)paramsMap.get(JF_PLMConstants_mxJPO.STRING_PARAMMAP);
            String strObjectId = (String)paramMap.get(JF_PLMConstants_mxJPO.STRING_OBJECTID);
            String strNewValue = (String)paramMap.get(STRING_NEW_VALUE);
            logger.info("strTableName:{}",strTableName);
            logger.info("strParentOID:{}",strParentOID);
            logger.info("strAttrName:{}",strAttrName);
            logger.info("strObjectId:{}",strObjectId);
            logger.info("strNewValue:{}",strNewValue);
            String strAttrRealName = "";
            boolean isAttr = false ;
            if ("JFTaskEstimatedFinishDate".equals(strAttrName) || "TaskEstimatedFinishDate".equals(strAttrName)){
                strAttrRealName = "Task Estimated Finish Date";
                isAttr = true;
                //20260818 update by codex caipan 兼容中文、英文及达索系统日期格式
                strNewValue = JF_Util_mxJPO.convertDateToMatrixFormat(strNewValue);
            }else if ("JFDAActualFinishTime".equals(strAttrName) || "JF_DAActualFinishTime".equals(strAttrName)){
                strAttrRealName = "JF_DAActualFinishTime";
                isAttr = true;
                //20260818 update by codex caipan 兼容中文、英文及达索系统日期格式
                strNewValue = JF_Util_mxJPO.convertDateToMatrixFormat(strNewValue);
            }else if ("Title".equals(strAttrName)){
                strAttrRealName = "Title";
                isAttr = true;
            }else if ("description".equals(strAttrName)){
                strAttrRealName = "JF_DAActualFinishTime";
            }else if ("JF_CostChanges".equals(strAttrName)){
                strAttrRealName = "JF_CostChanges";
                isAttr = true;
            }
            DomainObject execTask = DomainObject.newInstance(context, strObjectId);
            if (isAttr){
                execTask.setAttributeValue(context,strAttrRealName,strNewValue);
            }else {
                execTask.setDescription(context,strNewValue);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }finally {
            ContextUtil.popContext(context);
        }
    }

    /**
     * @description 获取执行计划任务的附件信息
     * @param context
     * @param args
     * @author lsa
     */
    public StringList getAttachmentContent(Context context, String[] args){
        StringList resultList = new StringList();
        try {
            Map argMaps = JPO.unpackArgs(args);
            MapList argMap = (MapList) argMaps.get("objectList");
            Iterator iterator = argMap.iterator();
            String contextUser = context.getUser();


            Map requestMap = (Map) argMaps.get(STRING_REQUESTMAP);

            boolean flag  = JPO.invoke(context, "JF_PCR", null, "VerificationSubmitAccess", JPO.packArgs(requestMap), Boolean.class);

            String parentId =(String)requestMap.get("parentOID");
            DomainObject obj = DomainObject.newInstance(context,parentId);
            String pcrCurrent = obj.getInfo(context, SELECT_CURRENT);

            while (iterator.hasNext()){
                Map map = (Map) iterator.next();
                String objectId = (String) map.get("id");
                StringBuffer buffer = new StringBuffer();
                DomainObject object = DomainObject.newInstance(context, objectId);
                MapList mapList = object.getRelatedObjects(context, "Reference Document", "*", bosel, null, false, true, (short) 0, "", "", 0);
                Map responseMap = object.getRelatedObject(context, "Assigned Tasks", false, JF_Util_mxJPO.basicBolistSel(), JF_Util_mxJPO.basicRellistSel());
                State currentState = object.getCurrentState(context);
                if(mapList.size()>0){
                    for (int i = 0; i < mapList.size(); i++) {
                        Map projectMap = (Map) mapList.get(i);
                        String folderId = (String) projectMap.get("id");
                        buffer.append("<table id='").append(folderId).append("'><td><a href=\"/3dspace/common/emxNavigator.jsp?objectId=");
                        buffer.append(folderId).append("\" target=\"_blank>\">");
                        String folderName = (String) projectMap.get("name");
                        folderName = StringEscapeUtils.escapeHtml4(folderName);
                        buffer.append(folderName);
                        buffer.append("</a>");
                        buffer.append("</td>");
                        if(responseMap!=null){
                            String taskResponseName = (String) responseMap.get(DomainConstants.SELECT_NAME);
                            if(contextUser.equals(taskResponseName)&&flag && "Active".equals(currentState.getName()) &&(pcrCurrent.equalsIgnoreCase("Execution") ||pcrCurrent.equalsIgnoreCase("Verification"))  ) {

                                buffer.append("<td>");
//                        buffer.append("<a href=\"javascript:removeDocumentPerson('ReceiptConfirmationHidden','GroupFollower','IsGroupFieldModified')\">");
                                buffer.append("<a href=\"javascript:removePCRExecuteTaskDoc('").append(folderId).append("')\">");
                                buffer.append("<img src=\"../common/images/iconStatusRemoved.gif\" width=\"12\" height=\"12\" border=\"0\" />");
                                buffer.append("</a>");
                                buffer.append("<a href=\"javascript:removePCRExecuteTaskDoc(").append(folderId).append(")\">");
                                buffer.append("</a>");
                                buffer.append("</td>");
                            }
                        }

                        buffer.append ("</table>");
                    }
                }
                resultList.add(buffer.toString());
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return resultList;
    }


    /**
     * @description: 将DA状态从任务计划提升至执行时给DATask全部都提升状态至未决
     *               将DA状态从执行提升至关闭时给DATask全部都提升状态至完成
     * @author: lsa
     * @return:
     */
    public void promoteDATaskState(Context context,String[] args) throws FrameworkException {
        String objectId = args[0];
        try {
            DomainObject daObj = DomainObject.newInstance(context, objectId);
            relsel.add(DomainRelationship.SELECT_ID);
            String current = daObj.getInfo(context, SELECT_CURRENT);
            MapList mapList = daObj.getRelatedObjects(context, RELATIONSHIP_JFDA2JFDATASK, TYPE_JF_DATASK, bosel, relsel, false, true, (short) 0, "", "", 0);
            ContextUtil.pushContext(context);
            for (int i = 0; i < mapList.size(); i++) {
                Map argMaps = (Map) mapList.get(i);
                String daTaskId = (String) argMaps.get(DomainConstants.SELECT_ID);
                DomainObject daTaskObj = new DomainObject(daTaskId);
                if("DAImplementationPlan".equalsIgnoreCase(current)){
                    MqlUtil.mqlCommand(context, true, "mod bus "+daTaskId+" current Active", false);//去掉触发发邮件
                    JF_SignTask_mxJPO.projectTaskToActiveSendEmail(context, new String[]{daTaskId});
                }else {
                    daTaskObj.promote(context);
                    JPO.invoke(context, "JF_ChangeExecutionECOSource", new String[0], "setJF_TaskFileToPromote", new String[]{daTaskId,"RELEASED"}, void.class);
                }

            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            ContextUtil.popContext(context);
        }

    }



    /**
     * @description 查看表单的整椅经理
     * @param context
     * @param args
     * @Author lsa
     */
    public String getChairMangerForm(Context context,String[] args)throws Exception{
        Map paramMap = (Map) JPO.unpackArgs(args);
        Map requestMap = (Map) paramMap.get(STRING_REQUESTMAP);
        String objectId = (String)requestMap.get(STRING_OBJECTID);
        StringBuffer stringBuffer = new StringBuffer();
        String projectId = "";
        try {
            ContextUtil.pushContext(context);
            if (UIUtil.isNotNullAndNotEmpty(objectId)) {
                bosel.add(DomainConstants.SELECT_ID);
                bosel.add("attribute[First Name]");
                bosel.add("attribute[Last Name]");
                relsel.add("attribute[Project Role]");
                DomainObject daObj = DomainObject.newInstance(context, objectId);
                // 获取所关联的项目
                Map map = daObj.getRelatedObject(context, RELATIONSHIP_JFCHANGE2PROJECT, true, bosel, relsel);
                if (null != map) {
                    projectId = (String) map.get(DomainConstants.SELECT_ID);
                }
                DomainObject projectObej = new DomainObject(projectId);
                MapList memberMaps = projectObej.getRelatedObjects(context, RELATIONSHIP_MEMBER, DomainConstants.TYPE_PERSON, bosel, relsel, false, true, (short) 1, "", "", 0);
                if (!memberMaps.isEmpty()) {
                    for (int i = 0; i < memberMaps.size(); i++) {
                        Map mangerMap = (Map) memberMaps.get(i);
                        String projectRole = (String) mangerMap.get("attribute[Project Role]");
                        if ("Chair manager".equalsIgnoreCase(projectRole)) {
                            String allName = ((String) mangerMap.get("attribute[First Name]") + " " + (String) mangerMap.get("attribute[Last Name]")).trim();
                            stringBuffer.append(allName).append(" ");
                        }
                    }
                }

            }
        }finally {
            ContextUtil.popContext(context);
        }
        return stringBuffer.toString();
    }


    /**
     * @description 仅执行计划状态任务工作内容、责任人、计划完成时间只能由项目经理维护
     * @param context
     * @param args
     * @Author lsa
     */
    public StringList IsEditByProjectManager(Context context,String[] args) throws Exception{
        StringList result = new StringList();
        Map argsMaps = JPO.unpackArgs(args);
        MapList argMap = (MapList) argsMaps.get("objectList");
        for (int i = 0; i < argMap.size(); i++) {
            Map map = (Map) argMap.get(i);
            String id = (String) map.get("id[parent]");
            DomainObject daObj = new DomainObject(id);
            State currentState = daObj.getCurrentState(context);
            Map projectMap = daObj.getRelatedObject(context, RELATIONSHIP_JFCHANGE2PROJECT, true, bosel, relsel);
            if(null != projectMap){
                String projectId = (String) projectMap.get(DomainConstants.SELECT_ID);
                if(UIUtil.isNotNullAndNotEmpty(projectId)) {
                    String projectManager = JF_Util_mxJPO.getProjectManager(context, new String[]{projectId});
//                    User user = projectObj.getOwner(context);
                    // 获取项目经理id
                    String contextUser = context.getUser();
                    // DA状态在执行计划并且当前登录用户是项目经理给予编辑权限
                    result.add((contextUser.equalsIgnoreCase(projectManager) && "DAImplementationPlan".equalsIgnoreCase(currentState.getName())) ? "true" : "false");
                }
            }
        }
        return result;
    }

    /**
     * 项目经理在执行状态也可以编辑计划完成时间
     **
     * @param context
     * @param args
     * @return
     * @throws Exception
     * @author caipan
     * @date 2026/6/11 18:06
     */
    public StringList IsEditByProjectManagerforDate(Context context,String[] args) throws Exception{
        StringList result = new StringList();
        Map argsMaps = JPO.unpackArgs(args);
        MapList argMap = (MapList) argsMaps.get("objectList");
        for (int i = 0; i < argMap.size(); i++) {
            Map map = (Map) argMap.get(i);
            String id = (String) map.get("id[parent]");
            DomainObject daObj = new DomainObject(id);
            State currentState = daObj.getCurrentState(context);
            Map projectMap = daObj.getRelatedObject(context, RELATIONSHIP_JFCHANGE2PROJECT, true, bosel, relsel);
            if(null != projectMap){
                String projectId = (String) projectMap.get(DomainConstants.SELECT_ID);
                if(UIUtil.isNotNullAndNotEmpty(projectId)) {
                    String projectManager = JF_Util_mxJPO.getProjectManager(context, new String[]{projectId});
//                    User user = projectObj.getOwner(context);
                    // 获取项目经理id
                    String contextUser = context.getUser();
                    // DA状态在执行计划并且当前登录用户是项目经理给予编辑权限
                    result.add((contextUser.equalsIgnoreCase(projectManager) && ("DAImplementationPlan".equalsIgnoreCase(currentState.getName())||"Implement".equalsIgnoreCase(currentState.getName()))) ? "true" : "false");
                }
            }
        }
        return result;
    }


    /**
     * @description 任务负责人仅在DA执行状态时,能够修改自己负责的任务的实际完成时间、成本变化
     * @param context
     * @param args
     * @Author lsa
     */
    public StringList IsEditByTask(Context context,String[] args) throws Exception{
        StringList result = new StringList();
        StringList strObjectIdList = JF_PublicMethodClass_mxJPO.getObjectIdList(args);
        for (String objectId : strObjectIdList) {
            DomainObject daTask = DomainObject.newInstance(context, objectId);
            State currentState = daTask.getCurrentState(context);
            String current = currentState.getName();
            User user = daTask.getOwner(context);
            String contextUser = context.getUser();
            result.add(("Active".equalsIgnoreCase(current) && contextUser.equalsIgnoreCase(user.getName()))?"true":"false");
        }
        return result;
    }

    /**
     * @description 获取数组中的下标
     * @param arr
     * @param target
     * @author lsa
     */
    public static int findIndex(String[] arr, String target) {
        for (int i = 0; i < arr.length; i++) {
            if (arr[i].equals(target)) {
                return i;
            }
        }
        return -1; // 未找到元素时返回-1
    }

    /**
     * @description 通过项目daId获取整椅经理
     * @param context
     * @param projectId
     * @author lsa
     */
    public String getChairManagerName(Context context,String projectId) throws Exception {
        String chairManagerName = "";
        DomainObject projectObj = new DomainObject(projectId);
        relsel.add("attribute[Project Role]");
        MapList memberMaps = projectObj.getRelatedObjects(context, RELATIONSHIP_MEMBER, DomainConstants.TYPE_PERSON, JF_Util_mxJPO.basicBolistSel(), relsel, false, true, (short) 1, "", "", 0);
        if(!memberMaps.isEmpty()){
            for (int i = 0; i < memberMaps.size(); i++) {
                Map mangerMap = (Map) memberMaps.get(i);
                String projectRole = (String) mangerMap.get("attribute[Project Role]");
                if("Chair manager".equalsIgnoreCase(projectRole)){
                    chairManagerName = (String) mangerMap.get(DomainObject.SELECT_NAME);
                }
            }
        }
        return chairManagerName;
    }



    /**
     * @description 通过拖放上传文件附件
     * @param context
     * @param args
     * @author lsa
     */
    public StringList columnDropZone(Context context, String[] args) throws Exception
    {
        StringList dropIconList = new StringList();
        HashMap prgmMap = (HashMap) JPO.unpackArgs(args);
        HashMap paramList = (HashMap) prgmMap.get("paramList");
        paramList.put( "refreshStructure", "True" );
        prgmMap.put("paramList", paramList);
        args = JPO.packArgs(prgmMap);
        emxGenericColumns_mxJPO genericColumn = new emxGenericColumns_mxJPO(context,args);
        Vector columnIconList = genericColumn.columnDropZone(context, args);
        //for ODT
        String invokeFrom = (String) prgmMap.get("invokeFrom");
        Map programMap = (HashMap) JPO.unpackArgs(args);
        MapList objectList  = (MapList)programMap.get("objectList");
        int objectListSize = objectList.size();
        boolean needDBCall = false;

        if(objectList != null && objectListSize>0){

            String[] objectIdArray = new String[objectListSize];
            for (int i=0; i<objectListSize; i++) {
                Map objectMap = (Map) objectList.get(i);
                String objectId = (String)objectMap.get(DomainObject.SELECT_ID);
                objectIdArray[i] = objectId;

                String is_Project = (String)objectMap.get(ProgramCentralConstants.SELECT_IS_PROJECT_SPACE);
                String is_ProjectConcept = (String)objectMap.get(ProgramCentralConstants.SELECT_IS_PROJECT_CONCEPT);
                String is_WorkspaceVault = (String)objectMap.get(ProgramCentralConstants.SELECT_IS_WORKSPACE_VAULT);
                String hasModifyAccess = (String)objectMap.get("current.access[modify]");
                String hasfromconnectAccess = (String)objectMap.get("current.access[fromconnect]");
                String hasToconnectAccess = (String)objectMap.get("current.access[toconnect]");
                String is_ControlledFolder = (String)objectMap.get(ProgramCentralConstants.SELECT_IS_CONTROLLED_FOLDER);
                String current = (String)objectMap.get(ProgramCentralConstants.SELECT_CURRENT);

                if(is_Project==null || is_WorkspaceVault==null || hasModifyAccess==null || hasfromconnectAccess==null || hasToconnectAccess==null
                        || is_ControlledFolder==null || current==null || is_ProjectConcept==null){
                    needDBCall = true;
                }
            }
            MapList objectInfoList = new MapList();
            if(needDBCall){
                StringList busSelect = new StringList(5);
                busSelect.addElement(ProgramCentralConstants.SELECT_IS_PROJECT_SPACE);
                busSelect.addElement(ProgramCentralConstants.SELECT_IS_PROJECT_CONCEPT);
                busSelect.addElement(ProgramCentralConstants.SELECT_IS_WORKSPACE_VAULT);
                busSelect.addElement("current.access[modify]");
                busSelect.addElement("current.access[fromconnect]");
                busSelect.addElement("current.access[toconnect]");
                busSelect.addElement(ProgramCentralConstants.SELECT_IS_CONTROLLED_FOLDER);
                busSelect.addElement(ProgramCentralConstants.SELECT_CURRENT);

                //objectInfoList = DomainObject.getInfo(context, objectIdArray, busSelect);
                BusinessObjectWithSelectList objectWithSelectList = null;

                if("TestCase".equalsIgnoreCase(invokeFrom)) { //Added for ODT
                    objectWithSelectList = DomainObject.getSelectBusinessObjectData(context, objectIdArray, busSelect);
                }
                else {
                    objectWithSelectList = ProgramCentralUtil.getObjectWithSelectList(context, objectIdArray, busSelect);
                }

                for (int i=0, bsize = objectWithSelectList.size(); i <bsize ; i++) {
                    BusinessObjectWithSelect objectWithSelect = objectWithSelectList.getElement(i);
                    Map mapTask = new HashMap();
                    for (int j=0, busSelectSize = busSelect.size(); j <busSelectSize ; j++) {
                        String strSelectable = (String)busSelect.get(j);
                        mapTask.put(strSelectable, objectWithSelect.getSelectData(strSelectable));
                    }
                    objectInfoList.add(mapTask);
                }
            }

            for(int i=0;i<objectListSize;i++){
                Map objectMap = (Map)objectList.get(i);
                String assignedTask = (String) objectMap.get("to[Assigned Tasks].from.name");
                String current = (String) objectMap.get("current");
                if(needDBCall){
                    Map objectInfoMap = (Map)objectInfoList.get(i);
                    objectMap.putAll(objectInfoMap);
                }
                String dropIcon = (String)columnIconList.get(i);

                String modify = (String)objectMap.get("current.access[modify]");
                String fromconnect = (String)objectMap.get("current.access[fromconnect]");
                String toconnect = (String)objectMap.get("current.access[toconnect]");

                boolean showDropIcon = false;
                if("TRUE".equalsIgnoreCase(modify) && "TRUE".equalsIgnoreCase(fromconnect) && "TRUE".equalsIgnoreCase(toconnect)){
                    showDropIcon = true;
                }

                if(showDropIcon && "Active".equalsIgnoreCase(current) && context.getUser().equalsIgnoreCase(assignedTask)){
                    dropIconList.addElement(dropIcon);
                }else{
                    dropIconList.addElement(DomainObject.EMPTY_STRING);
                }

            }
        }

        return dropIconList;
    }


    /**
     * @description 通过点击上传文件图标上传附件
     * @param context
     * @param args
     * @author lsa
     */
    public StringList JFUploadFolderClick(Context context, String[] args) throws Exception{
        StringList resList = new StringList();
        long currentMillisTimestamp = System.currentTimeMillis();
        try {
            Map argMaps = JPO.unpackArgs(args);
            MapList objectList = (MapList) argMaps.get("objectList");
            StringBuffer buffer = new StringBuffer();
            for (int i = 0; i < objectList.size(); i++) {
                Map map = (Map) objectList.get(i);
                String id = (String) map.get(DomainConstants.SELECT_ID);
                DomainObject object = DomainObject.newInstance(context, id);
                State currentState = object.getCurrentState(context);
                Map responseMap = object.getRelatedObject(context, "Assigned Tasks", false, bosel, relsel);
                if(responseMap!=null){
                    String taskResponseName = (String) responseMap.get(DomainConstants.SELECT_NAME);
                    String contextUser = context.getUser();
                    if(contextUser.equals(taskResponseName) && "Active".equals(currentState.getName())){
                        buffer.append("<a onclick=\"ECOShowModalDialog(event,'../components/JF_ECOCommonDocumentPreCheckin.jsp?objectId=");
                        buffer.append(id).append("&amp;timeStamp=").append(currentMillisTimestamp)
                                .append("&amp;showName=null&amp;customSortColumns=null&amp;customSortDirections=null&amp;table=JFExecutionPlanTable&amp;showPolicy=null&amp;folderURL=null&amp;showFormat=null&amp;parentRelName=relationship_ReferenceDocument&amp;showDescription=null&amp;showType=null&amp;showOwner=null&amp;widgetId=null&amp;showRevision=null&amp;objectAction=createMasterPerFile&amp;showTitle=true&amp;appDir=programcentral&amp;appProcessPage=emxProgramCentraFolderUtil.jsp?actionMode=uploaddeliverable&amp;suiteKey=ProgramCentral&amp;StringResourceFileId=emxProgramCentralStringResource&amp;SuiteDirectory=programcentral&amp;refreshTableContent=true','730','450');\"><img style=\"border:0; padding: 2px;\" src=\"../common/images/iconActionAppend.gif\" alt=\"上传新文件或其他文件\" title=\"上传新文件或其他文件\"></img></a>");
                    }
                }
                resList.add(buffer.toString());
                buffer.setLength(0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return resList;
    }

    /**
     * @description 申请延期流程按钮权限，最多允许完成两次延期
     * @param context
     * @param args
     * @return Boolean 可以申请延期时返回true
     * @throws Exception
     * @author lsa
     * @date 2026/8/4
     */
    public Boolean delayProcessAccess(Context context, String[] args) throws Exception{
        Map argsMap = JPO.unpackArgs(args);
        String objectId = (String) argsMap.get(STRING_OBJECTID);
        DomainObject daObj = new DomainObject(objectId);
        relsel.add("attribute[Route Base State]");
        State currentState = daObj.getCurrentState(context);
        String JFDAIsDelay = daObj.getAttributeValue(context,"JFDAIsDelay");
        //20260804 update by ljr DA最多允许完成两次延期
        String delayCountValue = daObj.getAttributeValue(context, ATTRIBUTE_JFDADELAYCOUNT);
        int delayCount = UIUtil.isNullOrEmpty(delayCountValue) ? 0 : Integer.parseInt(delayCountValue);
        if (delayCount >= 2) return false;
        String current = currentState.getName();
        // 还没申请延期流程则可以申请流程
        if("FALSE".equals(JFDAIsDelay) && "Implement".equals(current)) return true;
        User user = daObj.getOwner(context);
        String owner = user.getName();
        String loginUser = context.getUser();
        MapList mapList = daObj.getRelatedObjects(context, "Object Route", "Route", bosel, relsel, false, true, (short) 0, "", "", 0);
        boolean hasCompletedExtensionRoute = false;
        if(!mapList.isEmpty()){
            for (int i = 0; i < mapList.size(); i++) {
                Map routeMap = (Map) mapList.get(i);
                String routeCurrent = (String) routeMap.get("current");
                String routeBaseState = (String) routeMap.get("attribute[Route Base State]");
                //20260819 update by caipan 存在未完成的延期流程时禁止重复申请
                if("state_Implement".equals(routeBaseState)){
                    if (!"Complete".equals(routeCurrent)) {
                        return false;
                    }
                    hasCompletedExtensionRoute = true;
                }
            }
        }
        // 延期流程完成后才能申请第二次延期
        return hasCompletedExtensionRoute && loginUser.equals(owner) && "Implement".equals(current);
    }



    /**
     * @description 获取DA，ECO所关联的附件
     * @param context
     * @param args
     * @author lsa
     */
    public MapList getAllReferenceDocument(Context context, String[] args) throws Exception{
        Map map = JPO.unpackArgs(args);
        String objectId = (String) map.get("objectId");
        DomainObject obj = new DomainObject(objectId);
        bosel.add(SELECT_ID);
        MapList mapList = obj.getRelatedObjects(context, "Reference Document", "Document", bosel, relsel, false, true, (short) 0, "", "", 0);
        logger.info("mapList:{}",mapList);
        return mapList;
    }


    /**
    * 创建时候设置影响工厂的值
    * @param context
	* @param args
    * @author LIUJR
    * @throws
    * @return void
    * @date 2024/9/13 11:25
    * @description
    */
    @ProgramCallable
    public void editJFAffectsFactorys(Context context, String[] args) throws Exception {
        try{
            Map paramMap = (Map) JPO.unpackArgs(args);
            Map paramsMap = (Map) paramMap.get("paramMap");
            Map fieldMap = (Map) paramMap.get("fieldMap");
            String[] strNewValues = (String[]) paramsMap.get("New Values");
            StringList strings = StringList.create(strNewValues);
            String objectId = (String) paramsMap.get(STRING_OBJECTID);
            Map settings = (Map) fieldMap.get("settings");
            String adminType = (String) settings.get("Admin Type");
            String attribute = PropertyUtil.getSchemaProperty(context, adminType);
            String mql = "mod bus " + objectId + " " + attribute + "  " + strings.join(",");
            MqlUtil.mqlCommand(context, mql);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
    * 修改影响工厂
    * @param context
	* @param args
    * @author LIUJR
    * @throws
    * @return void
    * @date 2024/9/13 11:25
    * @description
    */
    @ProgramCallable
    public void editModJFAffectsFactory(Context context, String[] args) throws Exception {
        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        Map paramMap = (Map) programMap.get("paramMap");
        Map fieldMap = (Map) programMap.get("fieldMap");
        String strObjectId = (String) paramMap.get("objectId");
        String[] strNewValues = (String[]) paramMap.get("New Values");
        logger.info("strNewValues:{}", Arrays.stream(strNewValues).toList());
        String strJFAffectsFactory = "";
        if (null != strNewValues && strNewValues.length > 0) {
            strJFAffectsFactory = StringList.create(strNewValues).join(",");
        }
        logger.info("strJFAffectsFactory:{}", strJFAffectsFactory);
        String strMql = JF_PublicMethodClass_mxJPO.buildStringInStrings("mod bus ", strObjectId, " ", "JFAffectsFactory", " ", strJFAffectsFactory, ";");
        MqlUtil.mqlCommand(context, strMql);
    }
    /**
    * 构造DA中影响工厂
    * @param context
	* @param args
    * @author LIUJR
    * @throws
    * @return java.lang.String
    * @date 2024/9/13 9:58
    * @description
    */
    public String buildDAFormLinkageAttributeHtml(Context context,String[] args) throws Exception{
        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        HashMap requestMap = (HashMap) programMap.get("requestMap");
        HashMap fieldMap = (HashMap) programMap.get("fieldMap");
        String strFieldName = (String) fieldMap.get("name");
        String strMode = (String) requestMap.get("mode");
        String strObjectId = (String) requestMap.get("objectId");
        String strLoginUser = context.getUser();
        if ("view".equals(strMode)){
            StringBuilder strBuilder = new StringBuilder();
            DomainObject daObject = DomainObject.newInstance(context, strObjectId);
            String strMQL = JF_PublicMethodClass_mxJPO.buildStringInStrings("print bus ",strObjectId," select " ,"attribute[JFAffectsFactory]"," dump ");
            String strRes = MqlUtil.mqlCommand(context, strMQL);
            String[] split = strRes.split(",");
            logger.info("split:{}", Arrays.stream(split).toList());
            StringList rangeNlsList = i18nNow.getAttrRangeI18NStringList("JFAffectsFactory", StringList.create(split), context.getSession().getLanguage());
            logger.info("rangeNlsList:{}", rangeNlsList);
            for (int i = 0; i < rangeNlsList.size(); i++) {
                String strRanNls = rangeNlsList.get(i);
                strRanNls = StringEscapeUtils.escapeHtml4(strRanNls);
                strBuilder.append(strRanNls);
                if (i < rangeNlsList.size() - 1) {
                    strBuilder.append("<br>");
                }
            }
            logger.info("strBuilder:{}", strBuilder.toString());
            return strBuilder.toString();
        }else if ("edit".equals(strMode)){
            DomainObject daObject = DomainObject.newInstance(context, strObjectId);
            Map daInfo = daObject.getInfo(context, StringList.create(SELECT_OWNER, SELECT_CURRENT));
            String strOwner = (String)daInfo.get(SELECT_OWNER);
            String strCurrent = (String)daInfo.get(SELECT_CURRENT);

            String strMQL = JF_PublicMethodClass_mxJPO.buildStringInStrings("print bus ",strObjectId," select " ,"attribute[JFAffectsFactory]"," dump ");
            String strFieldValue = MqlUtil.mqlCommand(context, strMQL);
            String[] split = strFieldValue.split(",");
            StringList AffectedFactory =  StringList.create(split);
            StringList list = com.matrixone.apps.domain.util.mxAttr.getChoices(context,"JFAffectsFactory");
            StringBuffer sb = new StringBuffer();
            //只有工作中才可以编辑
            if (strOwner.equals(strLoginUser) && STATE_IN_WORK.equals(strCurrent)){
                try {
                    sb.append("<table>");
                    sb.append("<tbody>");
                    for(int i = 0;i < list.size();i++) {
                        String strRange = list.get(i);
                        String strRangeNls = i18nNow.getRangeI18NString("JFAffectsFactory", strRange,context.getSession().getLanguage());
                        strRange = StringEscapeUtils.escapeHtml4(strRange);
                        strRangeNls = StringEscapeUtils.escapeHtml4(strRangeNls);
                        sb.append("<tr>");
                        sb.append("<td>");
                        if(AffectedFactory.contains(strRange)) {
                            sb.append("<input id=\"" + strRange + "\" type=\"checkbox\" name=\"JFAffectsFactory\" value=\"" + strRange + "\" required=\"true\" checked>");
                        } else {
                            sb.append("<input id=\"" + strRange + "\" type=\"checkbox\" name=\"JFAffectsFactory\" value=\"" + strRange + "\" required=\"true\">");
                        }
                        sb.append("</td>");
                        sb.append("<td>");
                        sb.append(strRangeNls);
                        sb.append("</td>");
                        sb.append("</tr>");
                    }
                    sb.append("</table>");
                    sb.append("</tbody>");
                }catch (Exception e){
                    logger.info("list:{}",list);
                }
                logger.info("sb:{}",sb.toString());
                return  sb.toString();
            }else {
                StringList rangeNlsList = i18nNow.getAttrRangeI18NStringList("JFAffectsFactory", StringList.create(split), context.getSession().getLanguage());
                for (int i = 0; i < rangeNlsList.size(); i++) {
                    String strRanNls = rangeNlsList.get(i);
                    strRanNls = StringEscapeUtils.escapeHtml4(strRanNls);
                    sb.append(strRanNls);
                    if (i < rangeNlsList.size() - 1) {
                        sb.append("<br>");
                    }
                }
            }
            return  sb.toString();
        }
        return "";
    }
    /*
     * @description:创建DA执行任务
     * @author: caipan
     * @date: 2025/9/12 15:26:52
     * @param: * @param[1] context
     * @param[2] args
     * @return:
     **/
    public void createDATaskObject(Context context, String[] args) throws FrameworkException {
        try {
            ArrayList argMaps = JPO.unpackArgs(args);
            // 排除重复的命名
            ArrayList<String> uniqueName = new ArrayList<>();
            // 获取ECO的id
            String ECOId = (String) argMaps.get(argMaps.size()-1);
            DomainObject ecoObj = new DomainObject(ECOId);
            //获取项目ID
             String projectId="";
                StringList ProjectList = ecoObj.getInfoList(context, "from[JFChange2Project].to.id");
                if(ProjectList.size()>0) {
                    projectId =ProjectList.get(0);
                }
            MapList mapList = ecoObj.getRelatedObjects(context, RELATIONSHIP_JFDA2JFDATASK, TYPE_JF_DATASK, bosel, relsel, false, true, (short) 1, "", "", 0);
            if(!mapList.isEmpty()){
                for (int i = 0; i < mapList.size(); i++) {
                    Map map = (Map) mapList.get(i);
                    String ecoTaskId = (String) map.get(SELECT_ID);
                    DomainObject ecoTaskObj = DomainObject.newInstance(context, ecoTaskId);
                    String value = ecoTaskObj.getAttributeValue(context, "Title");
                    uniqueName.add(value);
                }
            }

            for (int i = 0; i < argMaps.size()-1; i++) {
                String id = (String) argMaps.get(i);
                DomainObject object = new DomainObject(id);
                String ecoTaskName = object.getInfo(context, "attribute[Title]");
                String projectRole = object.getInfo(context, "attribute[Project Role]");
                String  taskowner= context.getUser();
                boolean flag = true;
                logger.info("projectId:{},projectRole:{}",projectId,projectRole);
                if(UIUtil.isNotNullAndNotEmpty(projectId)&&UIUtil.isNotNullAndNotEmpty(projectRole)){
                    String stdname = JF_Util_mxJPO.getProjectRoleName(context, new String[]{projectId,projectRole});
                    if(UIUtil.isNotNullAndNotEmpty(stdname)){
                        taskowner = stdname;
                        flag = false;
                    }
                }
                // 如果已经创建了同名的ECOTask,就过滤掉不再新建同名任务
                if(!uniqueName.contains(ecoTaskName)){
                    Map map = new HashMap();
                    map.put("taskName",ecoTaskName);//任务标题
                    map.put("taskType",SYMBOLIC_TYPE_JF_DATASK);//任务类型 注册名
                    map.put("taskOwner",taskowner);  //ECO的owner
                    map.put("parentId",ECOId);
                    if(flag) {
                        map.put("assign", "false");
                    }
                    //创建JFDATask任务实体
                    JF_Util_mxJPO jfUtilMxJPO = new JF_Util_mxJPO();
                    String taskId = jfUtilMxJPO.createTask(context, JPO.packArgs(map));
                    DomainObject taskObj = new DomainObject(taskId);
                    taskObj.setAttributeValue(context, "Project Role", projectRole);
                    // DA与创建的DATask连接关系
//                    DomainRelationship.connect(context, ecoObj,RELATIONSHIP_JFCO2ECOTASK,taskObj);
                    DomainRelationship.connect(context, new DomainObject(ecoObj),RELATIONSHIP_JFDA2JFDATASK,taskObj);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 1. DA任务状态报表定时器入口，由sendDAReportEmail.sh通过MQL调用；
     * 2. 调用exportAllDAAndDATaskList读取DA报表模板并写入三张Sheet统计数据；
     * 3. 将生成的Excel写入export.DA.Download配置目录，并作为附件发送到export.DA.SendEmail配置人员；
     * 4. DA专用配置为空时兼容使用ECR报表配置，避免老环境未新增配置时任务直接失败；
     * @param context
     * @param args
     * @author LIUJR
     * @throws
     * @return void
     * @date 2026/6/9 15:28
     * @description DA任务状态报表后台定时导出并发送邮件
     */
    public void ExportAndSendDAReportEmailsBackground(Context context, String[] args) {
        try {
            //开始写入execl文件
            Map res = exportAllDAAndDATaskList(context, args);
            String flag = (String) res.get("flag");
            if ("N".equalsIgnoreCase(flag)) {
                return;
            }
            Workbook workbook = (Workbook) res.get("file");
            String fileName = (String) res.get("fileName");
            String downloadDir = JF_PublicMethodClass_mxJPO.getBasicUrl(context, new String[]{"export.DA.Download"});

            File dir = new File(downloadDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            File reportFile = new File(dir, fileName);
            FileOutputStream fileOut = new FileOutputStream(reportFile);
            workbook.write(fileOut);
            workbook.close();
            fileOut.close();
            //发送邮件
            MimeMultipart multipart = JF_SendEmailUtils_mxJPO.sendEmailWithAttachments(context, reportFile.getAbsolutePath(), "DAEmailWithAttachments");
            //发送人邮箱
            String toAddressee = JF_PublicMethodClass_mxJPO.getBasicUrl(context, new String[]{"export.DA.SendEmail"});
            if (UIUtil.isNullOrEmpty(toAddressee)) {
                _logger.info("DA report mail recipient is empty, skip sending mail.");
                return;
            }
            JF_SendEmailUtils_mxJPO.SendEmail(context, toAddressee, "\u0044\u0041\u4efb\u52a1\u72b6\u6001\u6c47\u603b\u4fe1\u606f", multipart);
        } catch (Exception e) {
            e.printStackTrace();
            _logger.info("ExportAndSendDAReportEmailsBackground failed: {}", e.getMessage());
        }
    }

    /**
     * 1. DA和DATask报表导出主方法，负责组装Workbook但不直接发送邮件；
     * 2. 从3dspace/jf_template目录读取DADATaskTemplate.xlsx模板，创建边框样式和百分比样式；
     * 3. 查询DA、DATask和DA审批路由节点数据，并分别写入PLM系统DA状态统计、DA变更请求任务状态、DA任务执行计划状态三张Sheet；
     * 4. 返回Workbook、文件名和执行标识，由定时器入口统一保存到服务器目录并发送邮件；
     * @param context
     * @param args
     * @author LIUJR
     * @throws
     * @return java.util.Map
     * @date 2026/6/9 15:28
     * @description 导出DA任务状态报表Workbook
     */
    public Map exportAllDAAndDATaskList(Context context, String[] args) {
        Map resMap = new HashMap();
        boolean isPush = false;
        try {
            ContextUtil.pushContext(context);
            isPush = true;
            //拿取模板路径
            String classPath = Thread.currentThread().getContextClassLoader().getResource("").getPath();
            _logger.info("classPath:{}", classPath);
            if (classPath.length() < 20) {
                classPath = JF_PublicMethodClass_mxJPO.getBasicUrl(context, new String[]{"Space.WEBINFO.Path"});
            }
            String fileTemPath = classPath.substring(0, classPath.indexOf("WEB-INF"));
            String templatePath = JF_ECRService_mxJPO.getTemplatePath("JFDADaTaskTemplate", fileTemPath);
            InputStream inputStream = new FileInputStream(templatePath);
            Workbook workbook = WorkbookFactory.create(inputStream);
            inputStream.close();
            //设置单元格边框
            CellStyle cellBorderStyle = workbook.createCellStyle();
            cellBorderStyle.setBorderTop(BorderStyle.THIN);
            cellBorderStyle.setBorderBottom(BorderStyle.THIN);
            cellBorderStyle.setBorderLeft(BorderStyle.THIN);
            cellBorderStyle.setBorderRight(BorderStyle.THIN);
            cellBorderStyle.setAlignment(HorizontalAlignment.CENTER);
            cellBorderStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            //百分比风格
            CellStyle percentStyle = workbook.createCellStyle();
            percentStyle.cloneStyleFrom(cellBorderStyle);
            DataFormat dataFormat = workbook.createDataFormat();
            percentStyle.setDataFormat(dataFormat.getFormat("0.00%"));
            //获取需要的数据
            MapList allDAList = getDAReportAllDA(context);
            MapList allDATaskList = getDAReportAllDATask(context);
            /*
             * 1. 将DATask查询结果按父DA对象id分组，供项目统计Sheet判断各DA下任务是否延期；
             * 2. 分组键取DATask通过JFDA2JFDATask关系连接的DA对象id；
             * 3. 没有关联父DA的任务不参与DA维度统计，避免错误计入项目DA数量；
            * */
            logger.info("allDATaskList:{}", allDATaskList);
            Map daTasksByDA = JF_PublicMethodClass_mxJPO.getMapListGroupingMap(context, allDATaskList,"to[JFDA2JFDATask].from.id");
            daTasksByDA.remove("null");
            //拿取审批节点的信息
            Map approvalInfoByDA = buildDAApprovalInfo(context, allDAList);
            //写入excel
            writePLMProjectDAStatusToFile(context, workbook, cellBorderStyle, percentStyle, allDAList, daTasksByDA, approvalInfoByDA);
            writeDATaskStatusToFile(context, workbook, cellBorderStyle, allDAList, approvalInfoByDA);
            writeDADATaskPlanStatusToFile(context, workbook, cellBorderStyle, allDATaskList);
            //进行公式计算
            workbook.getSheetAt(0).setForceFormulaRecalculation(true);
            workbook.getSheetAt(1).setForceFormulaRecalculation(true);
            workbook.getSheetAt(2).setForceFormulaRecalculation(true);
            resMap.put("file", workbook);
            resMap.put("flag", "Y");
            resMap.put("fileName", "\u0044\u0041\u4efb\u52a1\u72b6\u6001\u62a5\u8868_" + LocalDateTime.now().format(DA_REPORT_FILE_DATE_FORMATTER) + ".xlsx");
        } catch (Exception e) {
            e.printStackTrace();
            resMap.put("flag", "N");
            return resMap;
        } finally {
            if (isPush) {
                try {
                    ContextUtil.popContext(context);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return resMap;
    }

    /**
     * 1. 查询系统中所有JFDA对象，作为DA报表两张DA维度Sheet的数据来源；
     * 2. 选择DA编号、标题、所有者、当前状态、DA开始/关闭/延期相关属性；
     * 3. 同时选择DA关联项目和关键生命周期状态实际到达时间，用于项目汇总、状态展示和延期判断；
     * 4. 查询结果按DA编号升序排序，保证每周导出的报表顺序稳定；
     * @param context
     * @author LIUJR
     * @throws Exception
     * @return matrix.util.MapList
     * @date 2026/6/9 15:28
     * @description 查询DA报表所需的全部DA数据
     */
    private static MapList getDAReportAllDA(Context context) throws Exception {
        StringList selects = new StringList();
        selects.add(SELECT_ID);
        selects.add(SELECT_NAME);
        selects.add(SELECT_OWNER);
        selects.add(SELECT_ORIGINATED);
        selects.add(SELECT_CURRENT);
        selects.add(SELECT_ATTRIBUTE_TITLE);
        selects.add("attribute[JFDAStartTime]");
        selects.add("attribute[JFDACloseTime]");
        selects.add("attribute[JFDAExtensionTime]");
        selects.add("attribute[JFDAIsDelay]");
        selects.add("attribute[JFProjectPhase]");
        selects.add("attribute[JFChangeType]");
        selects.add("attribute[JFAffectsFactory]");
        selects.add("attribute[JFReasonDeviation]");
        selects.add("attribute[JFBeforeChange]");
        selects.add("attribute[JFAfterChange]");
        selects.add("from[JFChange2Project].to.id");
        selects.add("from[JFChange2Project].to.name");
        selects.add("from[JFChange2Project].to.description");
        selects.add("state[DAImplementationPlan].actual");
        selects.add("state[Implement].actual");
        selects.add("state[Close].actual");
        MapList res = DomainObject.findObjects(context, TYPE_JFDA, "*", "", selects);
        res.addSortKey(SELECT_NAME, ProgramCentralConstants.ASCENDING_SORT, ProgramCentralConstants.SORTTYPE_STRING);
        res.sort();
        return res;
    }

    /**
     * 1. 查询系统中所有JF_DATask对象，作为DA任务执行计划状态Sheet的数据来源；
     * 2. 选择任务编号、标题、当前状态、描述、负责角色、计划完成时间和实际完成时间；
     * 3. 通过Assigned Tasks和JFDA2JFDATask关系取任务负责人、父DA和父DA关联项目；
     * 4. 查询结果按DATask编号升序排序，保证导出明细顺序稳定；
     * @param context
     * @author LIUJR
     * @throws Exception
     * @return matrix.util.MapList
     * @date 2026/6/9 15:28
     * @description 查询DA任务报表所需的全部DATask数据
     */
    private static MapList getDAReportAllDATask(Context context) throws Exception {
        StringList selects = new StringList();
        selects.add(SELECT_ID);
        selects.add(SELECT_NAME);
        selects.add(SELECT_CURRENT);
        selects.add(SELECT_DESCRIPTION);
        selects.add(SELECT_ATTRIBUTE_TITLE);
        selects.add("attribute[Project Role]");
        selects.add("attribute[Task Estimated Finish Date]");
        selects.add("attribute[JF_DAActualFinishTime]");
        selects.add("to[Assigned Tasks].from[Person].name");
        selects.add("to[JFDA2JFDATask].from.id");
        selects.add("to[JFDA2JFDATask].from.name");
        selects.add("to[JFDA2JFDATask].from.from[JFChange2Project].to.name");
        selects.add("to[JFDA2JFDATask].from.from[JFChange2Project].to.description");
        MapList res = DomainObject.findObjects(context, TYPE_JF_DATASK, "*", "", selects);
        res.addSortKey(SELECT_NAME, ProgramCentralConstants.ASCENDING_SORT, ProgramCentralConstants.SORTTYPE_STRING);
        res.sort();
        return res;
    }

    /**
     * 1. 写入模板第一张Sheet“PLM系统DA状态统计”，统计每个项目下DA总数、未完成数、延期数、延期率和关闭数；
     * 2. 根据DA审批路由节点统计直线经理、整椅经理、项目经理审批超过3天未完成的DA数量；
     * 3. 根据DA状态到达时间和DATask计划完成时间统计PM发起、各角色任务执行、PM关闭等延期数量；
     * 4. 写入项目明细行后追加DA合计行，并保留Excel公式用于未完成数、延期率和各列汇总计算；
     * @param context
     * @param workbook
     * @param style
     * @param percentStyle
     * @param allDAList
     * @param daTasksByDA
     * @param approvalInfoByDA
     * @author LIUJR
     * @throws Exception
     * @return void
     * @date 2026/6/9 15:28
     * @description 写入PLM项目维度DA状态统计Sheet
     */
    private static void writePLMProjectDAStatusToFile(Context context, Workbook workbook, CellStyle style, CellStyle percentStyle,
                                                      MapList allDAList, Map daTasksByDA, Map approvalInfoByDA) throws Exception {
        Sheet sheet = workbook.getSheetAt(0);
        int dataStartRow = 4;
        clearSheetRows(sheet, dataStartRow);
        Map daByProject = JF_PublicMethodClass_mxJPO.getMapListGroupingMap(context, allDAList, "from[JFChange2Project].to.id");
        StringList projectSelects = new StringList();
        projectSelects.add(SELECT_ID);
        projectSelects.add(SELECT_NAME);
        projectSelects.add(SELECT_TYPE);
        projectSelects.add(SELECT_DESCRIPTION);
        MapList projectList = DomainObject.findObjects(context, TYPE_PROJECT_SPACE, "*", "", projectSelects);
        projectList.addSortKey(SELECT_NAME, ProgramCentralConstants.ASCENDING_SORT, ProgramCentralConstants.SORTTYPE_STRING);
        projectList.sort();
        //角色写入列
        Map roleColumnMap = getDARoleColumnMap();
        int rowIndex = dataStartRow;
        for (int i = 0; i < projectList.size(); i++) {
            Map projectMap = (Map) projectList.get(i);
            String projectId = UIUtil.getValue(projectMap, SELECT_ID);
            List daList = (List) daByProject.get(projectId);
            if (daList == null) {
                daList = new ArrayList();
            }

            Row row = sheet.createRow(rowIndex);
            int totalCount = daList.size();
            int delayCount = 0;
            int closedCount = 0;
            int lineDelay = 0;
            int chairDelay = 0;
            int pmDelay = 0;
            int pmStartDelay = 0;
            int pmCloseDelay = 0;
            Map roleDelayDASetMap = new HashMap();
            for (Object roleObj : roleColumnMap.keySet()) {
                roleDelayDASetMap.put(roleObj, new HashSet());
            }

            for (Object obj : daList) {
                Map daMap = (Map) obj;
                String daId = UIUtil.getValue(daMap, SELECT_ID);
                String current = UIUtil.getValue(daMap, SELECT_CURRENT);
                boolean isClosed = "Close".equalsIgnoreCase(current);
                if (isClosed) {
                    closedCount++;
                    continue;
                }

                boolean daDelayed = false;
                Map approvalNodes = (Map) approvalInfoByDA.get(daId);
                if (approvalNodes == null) {
                    approvalNodes = createEmptyDAApprovalNodes();
                }
                if (isApprovalNodeOverdue((DAApprovalNode) approvalNodes.get("LINE"), 3)) {
                    lineDelay++;
                    daDelayed = true;
                }
                if (isApprovalNodeOverdue((DAApprovalNode) approvalNodes.get("CHAIR"), 3)) {
                    chairDelay++;
                    daDelayed = true;
                }
                if (isApprovalNodeOverdue((DAApprovalNode) approvalNodes.get("PM"), 3)) {
                    pmDelay++;
                    daDelayed = true;
                }
                if (isPMStartOverdue(daMap)) {
                    pmStartDelay++;
                    daDelayed = true;
                }
                if (isDACloseOverdue(daMap)) {
                    pmCloseDelay++;
                    daDelayed = true;
                }

                List taskList = (List) daTasksByDA.get(daId);
                if (taskList != null) {
                    for (Object taskObj : taskList) {
                        Map taskMap = (Map) taskObj;
                        String role = UIUtil.getValue(taskMap, "attribute[Project Role]");
                        if (roleDelayDASetMap.containsKey(role) && isDATaskOverdue(taskMap)) {
                            ((Set) roleDelayDASetMap.get(role)).add(daId);
                            daDelayed = true;
                        }
                    }
                }
                if (daDelayed) {
                    delayCount++;
                }
            }

            int excelRow = rowIndex + 1;
            writeCell(row, 0, i + 1, style);
            writeCell(row, 1, UIUtil.getValue(projectMap, SELECT_NAME), style);
            writeCell(row, 2, UIUtil.getValue(projectMap, SELECT_DESCRIPTION), style);
            writeCell(row, 3, totalCount, style);
            writeFormulaCell(row, 4, "D" + excelRow + "-F" + excelRow + "-X" + excelRow, style);
            writeCell(row, 5, delayCount, style);
            writeFormulaCell(row, 6, "IFERROR(F" + excelRow + "/D" + excelRow + ",0)", percentStyle);
            writeCell(row, 7, "\u5404\u9879\u76ee\u5ef6\u671f\u5ba1\u6279\u0044\u0041\u6570\u91cf\u7edf\u8ba1", style);
            writeCell(row, 8, lineDelay, style);
            writeCell(row, 9, chairDelay, style);
            writeCell(row, 10, pmDelay, style);
            writeCell(row, 11, pmStartDelay, style);
            writeCell(row, 12, ((Set) roleDelayDASetMap.get("Chair manager")).size(), style);
            writeCell(row, 13, ((Set) roleDelayDASetMap.get("Financial BP")).size(), style);
            writeCell(row, 14, ((Set) roleDelayDASetMap.get("Purchasing representative")).size(), style);
            writeCell(row, 15, ((Set) roleDelayDASetMap.get("SQD Representative")).size(), style);
            writeCell(row, 16, ((Set) roleDelayDASetMap.get("Logistics representative")).size(), style);
            writeCell(row, 17, ((Set) roleDelayDASetMap.get("AME representative")).size(), style);
            writeCell(row, 18, ((Set) roleDelayDASetMap.get("Foam AME representative")).size(), style);
            writeCell(row, 19, ((Set) roleDelayDASetMap.get("Trim AME representative")).size(), style);
            writeCell(row, 20, ((Set) roleDelayDASetMap.get("Launch manager")).size(), style);
            writeCell(row, 21, ((Set) roleDelayDASetMap.get("AQE representative/PQL")).size(), style);
            writeCell(row, 22, pmCloseDelay, style);
            writeCell(row, 23, closedCount, style);
            writeCell(row, 24, "", style);
            rowIndex++;
        }

        if (rowIndex - 1 > dataStartRow) {
            sheet.addMergedRegion(new CellRangeAddress(dataStartRow, rowIndex - 1, 7, 7));
        }

        Row totalRow = sheet.createRow(rowIndex);
        int totalExcelRow = rowIndex + 1;
        int firstExcelRow = dataStartRow + 1;
        int lastExcelRow = rowIndex;
        writeCell(totalRow, 0, "\u0044\u0041\u5408\u8ba1", style);
        writeCell(totalRow, 1, "", style);
        writeCell(totalRow, 2, "", style);
        sheet.addMergedRegion(new CellRangeAddress(rowIndex, rowIndex, 0, 2));
        if (lastExcelRow >= firstExcelRow) {
            writeFormulaCell(totalRow, 3, "SUM(D" + firstExcelRow + ":D" + lastExcelRow + ")", style);
            writeFormulaCell(totalRow, 4, "SUM(E" + firstExcelRow + ":E" + lastExcelRow + ")", style);
            writeFormulaCell(totalRow, 5, "SUM(F" + firstExcelRow + ":F" + lastExcelRow + ")", style);
            writeFormulaCell(totalRow, 6, "IFERROR(F" + totalExcelRow + "/D" + totalExcelRow + ",0)", percentStyle);
            writeCell(totalRow, 7, "\u0044\u0041\u6570\u91cf\u603b\u8ba1", style);
            for (int col = 8; col <= 23; col++) {
                String colName = String.valueOf((char) ('A' + col));
                writeFormulaCell(totalRow, col, "SUM(" + colName + firstExcelRow + ":" + colName + lastExcelRow + ")", style);
            }
        } else {
            for (int col = 3; col <= 23; col++) {
                writeCell(totalRow, col, 0, style);
            }
        }
        writeCell(totalRow, 24, "", style);
    }

    /**
     * 1. 写入模板第二张Sheet“DA变更请求任务状态”，按DA对象输出DA明细信息；
     * 2. 输出DA编号、标题、创建人、创建时间、当前状态、开始/关闭/延期相关字段；
     * 3. 输出直线经理、整椅经理、项目经理审批任务的接收时间和完成时间；
     * 4. 输出关联项目、项目阶段、变更类型、影响工厂、偏差原因、变更前后信息，变更类型和影响工厂按range国际化显示；
     * @param context
     * @param workbook
     * @param style
     * @param allDAList
     * @param approvalInfoByDA
     * @author LIUJR
     * @throws Exception
     * @return void
     * @date 2026/6/9 15:28
     * @description 写入DA变更请求任务状态Sheet
     */
    private static void writeDATaskStatusToFile(Context context, Workbook workbook, CellStyle style,
                                                MapList allDAList, Map approvalInfoByDA) throws Exception {
        Sheet sheet = workbook.getSheetAt(1);
        clearSheetRows(sheet, 1);
        Map exportDANlsMap = exportDANls();
        int rowIndex = 1;
        for (int i = 0; i < allDAList.size(); i++) {
            Map daMap = (Map) allDAList.get(i);
            String daId = UIUtil.getValue(daMap, SELECT_ID);
            Map approvalNodes = (Map) approvalInfoByDA.get(daId);
            if (approvalNodes == null) {
                approvalNodes = createEmptyDAApprovalNodes();
            }
            DAApprovalNode lineNode = (DAApprovalNode) approvalNodes.get("LINE");
            DAApprovalNode chairNode = (DAApprovalNode) approvalNodes.get("CHAIR");
            DAApprovalNode pmNode = (DAApprovalNode) approvalNodes.get("PM");
            Row row = sheet.createRow(rowIndex);
            writeCell(row, 0, i + 1, style);
            writeCell(row, 1, UIUtil.getValue(daMap, SELECT_NAME), style);
            writeCell(row, 2, UIUtil.getValue(daMap, SELECT_ATTRIBUTE_TITLE), style);
            writeCell(row, 3, PersonUtil.getFullName(context, UIUtil.getValue(daMap, SELECT_OWNER)), style);
            writeCell(row, 4, formatDateTime(UIUtil.getValue(daMap, SELECT_ORIGINATED), false), style);
            writeCell(row, 5, translateDACurrent(UIUtil.getValue(daMap, SELECT_CURRENT)), style);
            writeCell(row, 6, formatDateTime(UIUtil.getValue(daMap, "attribute[JFDAStartTime]"), true), style);
            writeCell(row, 7, formatDateTime(UIUtil.getValue(daMap, "attribute[JFDACloseTime]"), true), style);
            writeCell(row, 8, UIUtil.getValue(daMap, "attribute[JFDAIsDelay]"), style);
            writeCell(row, 9, formatDateTime(UIUtil.getValue(daMap, "attribute[JFDAExtensionTime]"), true), style);
            writeCell(row, 10, formatDateTime(lineNode.receiveTime, false), style);
            writeCell(row, 11, formatDateTime(lineNode.completeTime, false), style);
            writeCell(row, 12, formatDateTime(chairNode.receiveTime, false), style);
            writeCell(row, 13, formatDateTime(chairNode.completeTime, false), style);
            writeCell(row, 14, formatDateTime(pmNode.receiveTime, false), style);
            writeCell(row, 15, formatDateTime(pmNode.completeTime, false), style);
            writeCell(row, 16, formatDateTime(UIUtil.getValue(daMap, "state[DAImplementationPlan].actual"), false), style);
            writeCell(row, 17, formatDateTime(UIUtil.getValue(daMap, "state[Implement].actual"), false), style);
            writeCell(row, 18, UIUtil.getValue(daMap, "from[JFChange2Project].to.name"), style);
            writeCell(row, 19, UIUtil.getValue(daMap, "from[JFChange2Project].to.description"), style);
            writeCell(row, 20, UIUtil.getValue(daMap, "attribute[JFProjectPhase]"), style);
            String changeType = UIUtil.getValue(daMap, "attribute[JFChangeType]");
            String changeTypeKeyPrefix = "emxFramework.Range." + ATTRIBUTE_JFCHANGETYPE + ".";
            if (UIUtil.isNotNullAndNotEmpty(changeType) && changeType.startsWith(changeTypeKeyPrefix)) {
                changeType = changeType.substring(changeTypeKeyPrefix.length());
            }
            String changeTypeKey = ATTRIBUTE_JFCHANGETYPE + "_" + changeType.replace(" ", "_");
            if (exportDANlsMap.containsKey(changeTypeKey)) {
                changeType = (String) exportDANlsMap.get(changeTypeKey);
            }
            writeCell(row, 21, changeType, style);

            StringList affectsFactoryList = new StringList();
            Object affectsFactoryObj = daMap.get("attribute[JFAffectsFactory]");
            if (affectsFactoryObj instanceof StringList) {
                affectsFactoryList = (StringList) affectsFactoryObj;
            } else if (affectsFactoryObj instanceof List) {
                List factoryList = (List) affectsFactoryObj;
                for (int j = 0; j < factoryList.size(); j++) {
                    affectsFactoryList.add(String.valueOf(factoryList.get(j)));
                }
            } else if (affectsFactoryObj != null) {
                String affectsFactoryStr = String.valueOf(affectsFactoryObj);
                String[] factoryArray = affectsFactoryStr.indexOf('\u0007') >= 0 ? affectsFactoryStr.split("\u0007") : affectsFactoryStr.split(",");
                for (int j = 0; j < factoryArray.length; j++) {
                    affectsFactoryList.add(factoryArray[j]);
                }
            }
            StringBuilder affectsFactorySb = new StringBuilder();
            for (int j = 0; j < affectsFactoryList.size(); j++) {
                String affectsFactory = String.valueOf(affectsFactoryList.get(j)).trim();
                if (UIUtil.isNullOrEmpty(affectsFactory)) {
                    continue;
                }
                String affectsFactoryKeyPrefix = "emxFramework.Range." + ATTRIBUTE_JFAFFECTSFACTORY + ".";
                if (affectsFactory.startsWith(affectsFactoryKeyPrefix)) {
                    affectsFactory = affectsFactory.substring(affectsFactoryKeyPrefix.length());
                }
                String affectsFactoryKey = ATTRIBUTE_JFAFFECTSFACTORY + "_" + affectsFactory.replace(" ", "_");
                if (exportDANlsMap.containsKey(affectsFactoryKey)) {
                    affectsFactory = (String) exportDANlsMap.get(affectsFactoryKey);
                }
                if (affectsFactorySb.length() > 0) {
                    affectsFactorySb.append(",");
                }
                affectsFactorySb.append(affectsFactory);
            }
            writeCell(row, 22, affectsFactorySb.toString(), style);
            writeCell(row, 23, UIUtil.getValue(daMap, "attribute[JFReasonDeviation]"), style);
            writeCell(row, 24, UIUtil.getValue(daMap, "attribute[JFBeforeChange]"), style);
            writeCell(row, 25, UIUtil.getValue(daMap, "attribute[JFAfterChange]"), style);
            rowIndex++;
        }
    }

    /**
     * 1. 写入模板第三张Sheet“DA任务执行计划状态”，按DATask对象输出任务执行计划明细；
     * 2. 输出任务编号、标题、父DA编号、父DA关联项目、任务状态和任务描述；
     * 3. 根据Project Role将计划完成时间、实际完成时间、责任人写入对应角色的三列区域；
     * 4. 未匹配到模板角色列的任务仍保留基础任务信息，避免因角色配置差异导致明细丢失；
     * @param context
     * @param workbook
     * @param style
     * @param allDATaskList
     * @author LIUJR
     * @throws Exception
     * @return void
     * @date 2026/6/9 15:28
     * @description 写入DA任务执行计划状态Sheet
     */
    private static void writeDADATaskPlanStatusToFile(Context context, Workbook workbook, CellStyle style, MapList allDATaskList) throws Exception {
        Sheet sheet = workbook.getSheetAt(2);
        clearSheetRows(sheet, 4);
        Map roleColumnMap = getDARoleColumnMap();
        int rowIndex = 4;
        for (int i = 0; i < allDATaskList.size(); i++) {
            Map taskMap = (Map) allDATaskList.get(i);
            Row row = sheet.createRow(rowIndex);
            for (int col = 0; col <= 37; col++) {
                writeCell(row, col, "", style);
            }
            writeCell(row, 0, i + 1, style);
            writeCell(row, 1, UIUtil.getValue(taskMap, SELECT_NAME), style);
            writeCell(row, 2, UIUtil.getValue(taskMap, SELECT_ATTRIBUTE_TITLE), style);
            writeCell(row, 3, UIUtil.getValue(taskMap, "to[JFDA2JFDATask].from.name"), style);
            writeCell(row, 4, UIUtil.getValue(taskMap, "to[JFDA2JFDATask].from.from[JFChange2Project].to.name"), style);
            writeCell(row, 5, UIUtil.getValue(taskMap, "to[JFDA2JFDATask].from.from[JFChange2Project].to.description"), style);
            writeCell(row, 6, translateDATaskCurrent(UIUtil.getValue(taskMap, SELECT_CURRENT)), style);

            String role = UIUtil.getValue(taskMap, "attribute[Project Role]");
            List columns = (List) roleColumnMap.get(role);
            if (columns != null && columns.size() == 3) {
                writeCell(row, (Integer) columns.get(0), formatDateTime(UIUtil.getValue(taskMap, "attribute[Task Estimated Finish Date]"), true), style);
                writeCell(row, (Integer) columns.get(1), formatDateTime(UIUtil.getValue(taskMap, "attribute[JF_DAActualFinishTime]"), true), style);
                writeCell(row, (Integer) columns.get(2), PersonUtil.getFullName(context, UIUtil.getValue(taskMap, "to[Assigned Tasks].from[Person].name")), style);
            }
            writeCell(row, 37, UIUtil.getValue(taskMap, SELECT_DESCRIPTION), style);
            rowIndex++;
        }
    }

    /**
     * 1. 为全部DA批量构建审批节点信息，供第一张统计Sheet和第二张DA明细Sheet复用；
     * 2. 以DA对象id作为Map键，值为直线经理、整椅经理、项目经理三个审批节点信息；
     * 3. 每个DA内部调用getDAApprovalNodes读取Approve状态对应Route下的Inbox Task；
     * 4. 统一在导出前缓存审批数据，避免写Sheet时重复查询同一个DA的路由信息；
     * @param context
     * @param allDAList
     * @author LIUJR
     * @throws Exception
     * @return java.util.Map
     * @date 2026/6/9 15:28
     * @description 构建DA审批节点信息缓存
     */
    private static Map buildDAApprovalInfo(Context context, MapList allDAList) throws Exception {
        Map result = new HashMap();
        for (int i = 0; i < allDAList.size(); i++) {
            Map daMap = (Map) allDAList.get(i);
            String daId = UIUtil.getValue(daMap, SELECT_ID);
            result.put(daId, getDAApprovalNodes(context, daId, "state_Approve"));
        }
        return result;
    }

    /**
     * 1. 查询指定DA在指定Route Base State下的审批路由任务；
     * 2. 通过Object Route关系找到Route，再通过Route Task关系读取Inbox Task；
     * 3. 根据Inbox Task标题识别直线经理、整椅经理、项目经理三个模板审批节点；
     * 4. 记录每个节点的任务状态、接收时间和完成时间，用于审批时长统计和DA明细输出；
     * @param context
     * @param daId
     * @param routeBaseState
     * @author LIUJR
     * @throws Exception
     * @return java.util.Map
     * @date 2026/6/9 15:28
     * @description 获取DA审批Route节点时间信息
     */
    private static Map getDAApprovalNodes(Context context, String daId, String routeBaseState) throws Exception {
        Map nodes = createEmptyDAApprovalNodes();

        DomainObject daObj = DomainObject.newInstance(context, daId);
        StringList routeSelects = new StringList();
        routeSelects.add(SELECT_ID);
        routeSelects.add(SELECT_CURRENT);
        StringList routeRelSelects = new StringList();
        routeRelSelects.add("attribute[Route Base State]");
        StringList taskSelects = new StringList();
        taskSelects.add(SELECT_ID);
        taskSelects.add(SELECT_CURRENT);
        taskSelects.add(SELECT_ORIGINATED);
        taskSelects.add("attribute[Title]");
        taskSelects.add("attribute[Actual Completion Date]");
        taskSelects.add("state[Assigned].actual");
        taskSelects.add("state[Complete].actual");
        MapList routeList = daObj.getRelatedObjects(context, DomainConstants.RELATIONSHIP_OBJECT_ROUTE, DomainObject.TYPE_ROUTE,
                routeSelects, routeRelSelects, false, true, (short) 1, "", "", 0);
        DomainObject routeObj = DomainObject.newInstance(context);
        for (int i = 0; i < routeList.size(); i++) {
            Map routeMap = (Map) routeList.get(i);
            String baseState = UIUtil.getValue(routeMap, "attribute[Route Base State]");
            if (UIUtil.isNotNullAndNotEmpty(routeBaseState) && !routeBaseState.equalsIgnoreCase(baseState)) {
                continue;
            }
            String routeId = UIUtil.getValue(routeMap, SELECT_ID);
            routeObj.setId(routeId);
            MapList inboxTaskList = routeObj.getRelatedObjects(context, DomainConstants.RELATIONSHIP_ROUTE_TASK, DomainObject.TYPE_INBOX_TASK,
                    taskSelects, new StringList(), true, false, (short) 1, "", "", 0);
            if (inboxTaskList.isEmpty()) {
                StringList inboxTaskIds = routeObj.getInfoList(context, "to[Route Task].from.id");
                if (inboxTaskIds != null && !inboxTaskIds.isEmpty()) {
                    inboxTaskList = DomainObject.getInfo(context, inboxTaskIds.toStringArray(), taskSelects);
                }
            }
            for (int j = 0; j < inboxTaskList.size(); j++) {
                Map taskMap = (Map) inboxTaskList.get(j);
                String title = UIUtil.getValue(taskMap, "attribute[Title]");
                String nodeKey = getDAApprovalNodeKey(context, title);
                if (UIUtil.isNullOrEmpty(nodeKey)) {
                    continue;
                }
                DAApprovalNode node = new DAApprovalNode();
                node.current = UIUtil.getValue(taskMap,  SELECT_CURRENT);
                node.receiveTime = UIUtil.getValue(taskMap, "state[Assigned].actual");
                if (UIUtil.isNullOrEmpty(node.receiveTime)) {
                    node.receiveTime = UIUtil.getValue(taskMap,  SELECT_ORIGINATED);
                }
                node.completeTime = UIUtil.getValue(taskMap,  "state[Complete].actual");
                if (UIUtil.isNullOrEmpty(node.completeTime)) {
                    node.completeTime = UIUtil.getValue(taskMap, "attribute[Actual Completion Date]");
                }
                nodes.put(nodeKey, node);
            }
        }
        return nodes;
    }

    /**
     * 1. 创建DA审批节点默认Map，固定包含LINE、CHAIR、PM三个键；
     * 2. 当DA没有审批路由、审批任务未创建或节点未匹配时，用空节点保证后续写Excel不出现空指针；
     * 3. 空节点的时间和状态字段均为空字符串，报表中显示为空；
     * @author LIUJR
     * @throws
     * @return java.util.Map
     * @date 2026/6/9 15:28
     * @description 创建DA审批节点默认结构
     */
    private static Map createEmptyDAApprovalNodes() {
        Map nodes = new HashMap();
        nodes.put("LINE", new DAApprovalNode());
        nodes.put("CHAIR", new DAApprovalNode());
        nodes.put("PM", new DAApprovalNode());
        return nodes;
    }

    /**
     * 1. 根据Inbox Task标题识别DA审批模板中的业务节点；
     * 2. 标题包含直线经理或linemanage时返回LINE，用于统计直线经理审批；
     * 3. 标题包含整椅经理、completed seat或wholechair时返回CHAIR，用于统计整椅经理审批；
     * 4. 标题包含项目经理或projectmanage时返回PM，用于统计项目经理审批；
     * @param context
     * @param title
     * @author LIUJR
     * @throws
     * @return java.lang.String
     * @date 2026/6/9 15:28
     * @description 识别DA审批任务所属模板节点
     */
    private static String getDAApprovalNodeKey(Context context, String title) throws Exception {
        if (UIUtil.isNullOrEmpty(title)) {
            return "";
        }
        String lower = title.toLowerCase(Locale.ENGLISH);
        //add by LIUJR 20260618 DA审批节点名称改为从国际化资源中获取，避免硬编码中文Unicode
        String lineManageZh = EnoviaResourceBundle.getProperty(context, SUITE_KEY, Locale.CHINA, "emxComponents.DARouteInfo.TitleMessage.LineManage");
        String lineManageUs = EnoviaResourceBundle.getProperty(context, SUITE_KEY, Locale.US, "emxComponents.DARouteInfo.TitleMessage.LineManage");
        String wholeChairZh = EnoviaResourceBundle.getProperty(context, SUITE_KEY, Locale.CHINA, "emxComponents.DARouteInfo.TitleMessage.WholeChair");
        String wholeChairUs = EnoviaResourceBundle.getProperty(context, SUITE_KEY, Locale.US, "emxComponents.DARouteInfo.TitleMessage.WholeChair");
        String projectManageZh = EnoviaResourceBundle.getProperty(context, SUITE_KEY, Locale.CHINA, "emxComponents.DARouteInfo.TitleMessage.ProjectManage");
        String projectManageUs = EnoviaResourceBundle.getProperty(context, SUITE_KEY, Locale.US, "emxComponents.DARouteInfo.TitleMessage.ProjectManage");
        //需要增加兼容 liujr 20260624  兼容旧的和新的流程title设置
        String projectManageZh_old = EnoviaResourceBundle.getProperty(context, SUITE_KEY, Locale.CHINA, "emxComponents.DARouteInfo.TitleMessage.ProjectManage_Old");
        String projectManageUs_old = EnoviaResourceBundle.getProperty(context, SUITE_KEY, Locale.US, "emxComponents.DARouteInfo.TitleMessage.ProjectManage_Old");
        String wholeChairZhName = wholeChairZh;
        int wholeChairIndex = wholeChairZh.indexOf("-");
        if (wholeChairIndex > -1) {
            wholeChairZhName = wholeChairZh.substring(wholeChairIndex + 1);
        }
        //end
        if (title.contains(lineManageZh) || title.contains(lineManageUs) || lower.contains("linemanage")) {
            return "LINE";
        }
        if (title.contains(wholeChairZh) || title.contains(wholeChairZhName) || title.contains(wholeChairUs) || lower.contains("completed seat") || lower.contains("wholechair")) {
            return "CHAIR";
        }
        //需要增加兼容 liujr 20260624  兼容旧的和新的流程title设置
        if (title.contains(projectManageZh) || title.contains(projectManageUs) || title.contains(projectManageZh_old) || title.contains(projectManageUs_old) || lower.contains("projectmanage")) {
            return "PM";
        }
        return "";
    }

    /**
     * 1. 判断DA审批节点是否超过指定天数仍未完成；
     * 2. 节点为空、当前状态为Complete或已有完成时间时不认为延期；
     * 3. 使用节点接收时间与当前时间比较，接收时间早于当前时间减去days天则判定为延期；
     * @param node
     * @param days
     * @author LIUJR
     * @throws
     * @return boolean
     * @date 2026/6/9 15:28
     * @description 判断DA审批节点是否延期
     */
    private static boolean isApprovalNodeOverdue(DAApprovalNode node, int days) {
        if (node == null || "Complete".equalsIgnoreCase(node.current)) {
            return false;
        }
        if (UIUtil.isNotNullAndNotEmpty(node.completeTime)) {
            return false;
        }
        LocalDateTime receiveTime = parseDateTime(node.receiveTime);
        return receiveTime != null && receiveTime.isBefore(LocalDateTime.now().minusDays(days));
    }

    /**
     * 1. 判断DA进入DAImplementationPlan状态后项目经理是否超过1天未发起执行计划；
     * 2. 仅当DA当前状态仍停留在DAImplementationPlan时参与判断；
     * 3. 使用状态实际到达时间state[DAImplementationPlan].actual与当前时间比较；
     * @param daMap
     * @author LIUJR
     * @throws
     * @return boolean
     * @date 2026/6/9 15:28
     * @description 判断PM发起DA执行计划是否延期
     */
    private static boolean isPMStartOverdue(Map daMap) {
        String current = UIUtil.getValue(daMap, SELECT_CURRENT);
        if (!"DAImplementationPlan".equalsIgnoreCase(current)) {
            return false;
        }
        LocalDateTime stateTime = parseDateTime(UIUtil.getValue(daMap, "state[DAImplementationPlan].actual"));
        return stateTime != null && stateTime.isBefore(LocalDateTime.now().minusDays(1));
    }

    /**
     * 1. 判断DA是否超过计划关闭时间3天仍未关闭；
     * 2. DA当前状态为Close时不参与延期判断；
     * 3. 使用属性JFDACloseTime加3天与当前时间比较，早于当前时间则判定为PM关闭延期；
     * @param daMap
     * @author LIUJR
     * @throws
     * @return boolean
     * @date 2026/6/9 15:28
     * @description 判断DA关闭是否延期
     */
    private static boolean isDACloseOverdue(Map daMap) {
        String current = UIUtil.getValue(daMap, SELECT_CURRENT);
        if ("Close".equalsIgnoreCase(current)) {
            return false;
        }
        LocalDateTime closeTime = parseDateTime(UIUtil.getValue(daMap, "attribute[JFDACloseTime]"));
        return closeTime != null && closeTime.plusDays(3).isBefore(LocalDateTime.now());
    }

    /**
     * 1. 判断DATask是否超过计划完成时间仍未完成；
     * 2. 任务当前状态为Complete或已有JF_DAActualFinishTime时不认为延期；
     * 3. 使用Task Estimated Finish Date与当前时间比较，计划完成时间早于当前时间则判定为延期；
     * @param taskMap
     * @author LIUJR
     * @throws
     * @return boolean
     * @date 2026/6/9 15:28
     * @description 判断DA任务执行计划是否延期
     */
    private static boolean isDATaskOverdue(Map taskMap) {
        String current = UIUtil.getValue(taskMap, SELECT_CURRENT);
        if ("Complete".equalsIgnoreCase(current)) {
            return false;
        }
        if (UIUtil.isNotNullAndNotEmpty(UIUtil.getValue(taskMap, "attribute[JF_DAActualFinishTime]"))) {
            return false;
        }
        LocalDateTime planFinish = parseDateTime(UIUtil.getValue(taskMap, "attribute[Task Estimated Finish Date]"));
        return planFinish != null && planFinish.isBefore(LocalDateTime.now());
    }

    /**
     * 1. 定义DA任务执行计划Sheet中Project Role与Excel列位置的对应关系；
     * 2. 每个角色对应三列，依次写入计划完成时间、实际完成时间和责任人；
     * 3. 同一映射也用于项目统计Sheet中按角色统计延期DA数量，保证角色口径一致；
     * @author LIUJR
     * @throws
     * @return java.util.Map
     * @date 2026/6/9 15:28
     * @description 获取DA任务角色和模板列映射
     */
    private static Map getDARoleColumnMap() {
        Map roleMap = new LinkedHashMap();
        roleMap.put("Chair manager", Arrays.asList(7, 8, 9));
        roleMap.put("Financial BP", Arrays.asList(10, 11, 12));
        roleMap.put("Purchasing representative", Arrays.asList(13, 14, 15));
        roleMap.put("SQD Representative", Arrays.asList(16, 17, 18));
        roleMap.put("Logistics representative", Arrays.asList(19, 20, 21));
        roleMap.put("AME representative", Arrays.asList(22, 23, 24));
        roleMap.put("Foam AME representative", Arrays.asList(25, 26, 27));
        roleMap.put("Trim AME representative", Arrays.asList(28, 29, 30));
        roleMap.put("Launch manager", Arrays.asList(31, 32, 33));
        roleMap.put("AQE representative/PQL", Arrays.asList(34, 35, 36));
        return roleMap;
    }

    /**
     * 1. 清理模板Sheet中指定起始行之后的旧数据行；
     * 2. 先删除起始行之后的合并区域，再从尾行向前删除Row对象，避免合并区域残留影响新数据写入；
     * 3. 用于三张DA报表Sheet写入前清空模板示例数据和历史占位行；
     * @param sheet
     * @param startRow
     * @author LIUJR
     * @throws
     * @return void
     * @date 2026/6/9 15:28
     * @description 清理Excel模板中的数据行
     */
    private static void clearSheetRows(Sheet sheet, int startRow) {
        removeMergedRegionsFromRow(sheet, startRow);
        for (int i = sheet.getLastRowNum(); i >= startRow; i--) {
            Row row = sheet.getRow(i);
            if (row != null) {
                sheet.removeRow(row);
            }
        }
    }

    /**
     * 1. 删除Sheet中从指定起始行开始的合并单元格区域；
     * 2. 从最后一个合并区域倒序删除，避免删除过程中索引变化导致漏删；
     * 3. 用于写入新报表数据前清理模板旧合并区域；
     * @param sheet
     * @param startRow
     * @author LIUJR
     * @throws
     * @return void
     * @date 2026/6/9 15:28
     * @description 删除指定行之后的合并单元格
     */
    private static void removeMergedRegionsFromRow(Sheet sheet, int startRow) {
        for (int i = sheet.getNumMergedRegions() - 1; i >= 0; i--) {
            CellRangeAddress region = sheet.getMergedRegion(i);
            if (region.getFirstRow() >= startRow) {
                sheet.removeMergedRegion(i);
            }
        }
    }

    /**
     * 1. 向指定Row和列写入普通单元格值；
     * 2. value为Number时按数字写入，其他类型统一转换为字符串写入，空值写为空字符串；
     * 3. 传入样式不为空时同步设置单元格样式，保证三张Sheet边框和对齐效果一致；
     * @param row
     * @param col
     * @param value
     * @param style
     * @author LIUJR
     * @throws
     * @return org.apache.poi.ss.usermodel.Cell
     * @date 2026/6/9 15:28
     * @description 写入Excel普通单元格
     */
    private static Cell writeCell(Row row, int col, Object value, CellStyle style) {
        Cell cell = row.createCell(col);
        if (value instanceof Number) {
            cell.setCellValue(((Number) value).doubleValue());
        } else {
            cell.setCellValue(value == null ? "" : String.valueOf(value));
        }
        if (style != null) {
            cell.setCellStyle(style);
        }
        return cell;
    }

    /**
     * 1. 向指定Row和列写入公式单元格；
     * 2. 公式内容不带等号，直接交给Apache POI设置到CellFormula；
     * 3. 主要用于第一张Sheet的未完成数、延期率和合计行汇总公式；
     * @param row
     * @param col
     * @param formula
     * @param style
     * @author LIUJR
     * @throws
     * @return org.apache.poi.ss.usermodel.Cell
     * @date 2026/6/9 15:28
     * @description 写入Excel公式单元格
     */
    private static Cell writeFormulaCell(Row row, int col, String formula, CellStyle style) {
        Cell cell = row.createCell(col);
        cell.setCellFormula(formula);
        if (style != null) {
            cell.setCellStyle(style);
        }
        return cell;
    }

    /**
     * 1. 构建DA报表后台导出专用国际化Map，参考JF_ECRService.exportECRNls处理方式；
     * 2. 后台终端MQL执行定时任务时无法稳定通过Web会话读取range国际化资源，因此这里直接维护导出所需中文显示值；
     * 3. 目前覆盖DA变更请求任务状态Sheet中的变更类型JFChangeType和影响工厂JFAffectsFactory两类range值；
     * @author LIUJR
     * @throws
     * @return java.util.Map
     * @date 2026/6/10 10:18
     * @description 获取DA报表导出专用国际化Map
     */
    private static Map exportDANls() {
        Map translationMap = new HashMap();
        translationMap.put("JFChangeType_", "");
        translationMap.put("JFChangeType_CustomerRequirements", "\u5ba2\u6237\u9700\u6c42");
        translationMap.put("JFChangeType_DesignDeviations", "\u8bbe\u8ba1\u504f\u5dee");
        translationMap.put("JFChangeType_ProcessDeviations", "\u5de5\u827a\u504f\u5dee");
        translationMap.put("JFChangeType_QualityDeviations", "\u8d28\u91cf\u504f\u5dee");
        translationMap.put("JFChangeType_SupplierDeviations", "\u4f9b\u5e94\u5546\u504f\u5dee");
        translationMap.put("JFChangeType_VAVE", "VAVE");
        translationMap.put("JFAffectsFactory_", "");
        translationMap.put("JFAffectsFactory_1021", "1021-\u5408\u80a5\u7ee7\u5cf0\u5de5\u5382");
        translationMap.put("JFAffectsFactory_1031", "1031-\u5e38\u5dde\u7ee7\u5cf0\u5de5\u5382");
        translationMap.put("JFAffectsFactory_1071", "1071-\u5b81\u6ce2\u7ee7\u5cf0\u5de5\u5382");
        translationMap.put("JFAffectsFactory_1091", "1091-\u5317\u4eac\u7ee7\u5cf0\u5de5\u5382");
        translationMap.put("JFAffectsFactory_1121", "1121-\u798f\u5dde\u7ee7\u5cf0\u5de5\u5382");
        translationMap.put("JFAffectsFactory_1151", "1151-\u7ee7\u5cf0\u829c\u6e56\u5de5\u5382");
        translationMap.put("JFAffectsFactory_1161", "1161-\u4e49\u4e4c\u7ee7\u5cf0\u5de5\u5382");
        translationMap.put("JFAffectsFactory_9061", "9061-\u683c\u62c9\u9ed8\u8f66\u8f86\u90e8\u4ef6\u5929\u6d25\u5de5\u5382");
        translationMap.put("JFAffectsFactory_9071", "9071-\u683c\u62c9\u9ed8\u8f66\u8f86\u90e8\u4ef6\u957f\u6625\u5de5\u5382");
        translationMap.put("JFAffectsFactory_2031", "2031-\u6cf0\u56fd\u7ee7\u5cf0\u5de5\u5382");
        translationMap.put("JFAffectsFactory_1211", "1211-\u91cd\u5e86\u7ee7\u5cf0\u5de5\u5382");
        translationMap.put("JFAffectsFactory_2021", "2021-\u6377\u514b\u5de5\u5382");
        translationMap.put("JFAffectsFactory_TBD", "TBD-New Plant");
        translationMap.put("JFAffectsFactory_2061", "2061-\u6ce2\u65af\u5c3c\u4e9a\u5de5\u5382");
        translationMap.put("JFAffectsFactory_1311", "1311-\u4e0a\u6d77\u7ee7\u5cf0\u660e\u82b3\u79d1\u6280\u6709\u9650\u516c\u53f8");
        translationMap.put("JFAffectsFactory_1191", "1191-\u4e0a\u6d77\u7ee7\u5cf0\u7ffc\u52a8\u6c7d\u8f66\u79d1\u6280\u5de5\u5382");
        translationMap.put("JFAffectsFactory_1361", "1361-\u7ee7\u5cf0\u5ea7\u6905\u5f00\u5c01\u5de5\u5382");
        translationMap.put("JFAffectsFactory_1381", "1381-\u7ee7\u5cf0\u5ea7\u6905\u67f3\u5dde\u5de5\u5382");
        return translationMap;
    }

    /**
     * 1. 将DA生命周期英文状态转换为报表中文显示值；
     * 2. 覆盖草稿、审批、任务发布、执行、关闭等DA主要状态；
     * 3. 未识别状态保留原始值，避免因状态配置扩展导致报表显示为空；
     * @param current
     * @author LIUJR
     * @throws
     * @return java.lang.String
     * @date 2026/6/9 15:28
     * @description 翻译DA当前状态
     */
    private static String translateDACurrent(String current) {
        if ("In_Work".equalsIgnoreCase(current)) {
            return "\u8349\u7a3f";
        }
        if ("Approve".equalsIgnoreCase(current)) {
            return "\u5ba1\u6279";
        }
        if ("DAImplementationPlan".equalsIgnoreCase(current)) {
            return "\u4efb\u52a1\u53d1\u5e03";
        }
        if ("Implement".equalsIgnoreCase(current)) {
            return "\u6267\u884c";
        }
        if ("Close".equalsIgnoreCase(current)) {
            return "\u5df2\u5173\u95ed";
        }
        return current;
    }

    /**
     * 1. 将DATask生命周期英文状态转换为报表中文显示值；
     * 2. 覆盖草稿、待办、工作中、审批中、已完成等任务主要状态；
     * 3. 未识别状态保留原始值，避免因状态配置扩展导致报表显示为空；
     * @param current
     * @author LIUJR
     * @throws
     * @return java.lang.String
     * @date 2026/6/9 15:28
     * @description 翻译DATask当前状态
     */
    private static String translateDATaskCurrent(String current) {
        if ("Create".equalsIgnoreCase(current)) {
            return "\u8349\u7a3f";
        }
        if ("Assign".equalsIgnoreCase(current)) {
            return "To Do";
        }
        if ("Active".equalsIgnoreCase(current)) {
            return "\u5de5\u4f5c\u4e2d";
        }
        if ("Review".equalsIgnoreCase(current)) {
            return "\u5ba1\u6279\u4e2d";
        }
        if ("Complete".equalsIgnoreCase(current)) {
            return "\u5df2\u5b8c\u6210";
        }
        return current;
    }

    /**
     * 1. 将ENOVIA返回的日期字符串格式化为报表展示格式；
     * 2. dateOnly为true时输出yyyy-MM-dd，否则输出yyyy-MM-dd HH:mm:ss；
     * 3. 日期无法解析时返回原始字符串，避免因个别格式异常导致报表导出失败；
     * @param value
     * @param dateOnly
     * @author LIUJR
     * @throws
     * @return java.lang.String
     * @date 2026/6/9 15:28
     * @description 格式化报表日期时间
     */
    private static String formatDateTime(String value, boolean dateOnly) {
        LocalDateTime dateTime = parseDateTime(value);
        if (dateTime == null) {
            return value;
        }
        return dateOnly ? dateTime.format(DA_REPORT_OUTPUT_DATE_FORMATTER) : dateTime.format(DA_REPORT_OUTPUT_DATE_TIME_FORMATTER);
    }

    /**
     * 1. 解析ENOVIA常见日期字符串为LocalDateTime；
     * 2. 支持英文AM/PM格式、yyyy-MM-dd HH:mm:ss、yyyy/MM/dd HH:mm:ss以及纯日期格式；
     * 3. 解析失败返回null，由调用方决定是否显示原始值或不参与延期判断；
     * @param value
     * @author LIUJR
     * @throws
     * @return java.time.LocalDateTime
     * @date 2026/6/9 15:28
     * @description 解析ENOVIA日期时间字符串
     */
    private static LocalDateTime parseDateTime(String value) {
        if (UIUtil.isNullOrEmpty(value)) {
            return null;
        }
        String val = value.trim();
        if (val.length() == 0) {
            return null;
        }
        DateTimeFormatter[] dateTimeFormatters = new DateTimeFormatter[]{
                DateTimeFormatter.ofPattern("M/d/yyyy h:mm:ss a", Locale.US),
                DateTimeFormatter.ofPattern("M/d/yyyy h:mm a", Locale.US),
                DateTimeFormatter.ofPattern("M/d/yyyy H:mm:ss", Locale.US),
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss", Locale.US),
                DateTimeFormatter.ofPattern("yyyy/M/d H:mm:ss", Locale.US)
        };
        for (DateTimeFormatter formatter : dateTimeFormatters) {
            try {
                return LocalDateTime.parse(val, formatter);
            } catch (Exception ignored) {
            }
        }
        DateTimeFormatter[] dateFormatters = new DateTimeFormatter[]{
                DateTimeFormatter.ofPattern("M/d/yyyy", Locale.US),
                DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.US),
                DateTimeFormatter.ofPattern("yyyy/M/d", Locale.US)
        };
        for (DateTimeFormatter formatter : dateFormatters) {
            try {
                return LocalDate.parse(val, formatter).atStartOfDay();
            } catch (Exception ignored) {
            }
        }
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("M/d/yyyy h:mm:ss a", Locale.US);
            return LocalDateTime.ofInstant(sdf.parse(val).toInstant(), TimeZone.getDefault().toZoneId());
        } catch (Exception ignored) {
        }
        return null;
    }

    /**
     * JFDR2DA关系创建后，将DR关联的零件和附件同步到DA，已关联对象不重复添加
     **
     * @param context
     * @param args Trigger参数，args[0]为DR对象ID，args[1]为DA对象ID
     * @return void
     * @throws Exception
     * @author caipan by codex
     * @date 2026/8/19 14:48
     */
    public void syncDRRelationsToDA(Context context, String[] args) throws Exception {
        String drId = args[0];
        String daId = args[1];
        DomainObject drObj = DomainObject.newInstance(context, drId);
        DomainObject daObj = DomainObject.newInstance(context, daId);

        StringList drPartIds = drObj.getInfoList(context,
                "from[" + REL_JFDR2VPMREFERENCE + "].to.id");
        Set<String> daPartIds = new HashSet<>(daObj.getInfoList(context,
                "from[" + RELATIONSHIP_DA2VPM + "].to.id"));
        int partCount = 0;
        for (String partId : drPartIds) {
            if (UIUtil.isNotNullAndNotEmpty(partId) && daPartIds.add(partId)) {
                DomainRelationship.connect(context, daObj, RELATIONSHIP_DA2VPM,
                        DomainObject.newInstance(context, partId));
                partCount++;
            }
        }

        StringList drDocumentIds = drObj.getInfoList(context,
                "from[" + REL_ReferenceDocument + "].to.id");
        Set<String> daDocumentIds = new HashSet<>(daObj.getInfoList(context,
                "from[" + REL_ReferenceDocument + "].to.id"));
        int documentCount = 0;
        for (String documentId : drDocumentIds) {
            if (UIUtil.isNotNullAndNotEmpty(documentId) && daDocumentIds.add(documentId)) {
                DomainRelationship.connect(context, daObj, REL_ReferenceDocument,
                        DomainObject.newInstance(context, documentId));
                documentCount++;
            }
        }
        logger.info("syncDRRelationsToDA drId:{} daId:{} partCount:{} documentCount:{}",
                drId, daId, partCount, documentCount);
    }

    /**
     * DA提升到审核前，校验关联DR、ECR的项目与DA当前项目一致
     **
     * @param context
     * @param args Trigger参数，args[0]为DA对象ID
     * @return int 项目一致返回0，存在不一致返回1
     * @throws Exception
     * @author caipan by codex
     * @date 2026/8/19 15:07
     */
    public int checkRelatedChangeProject(Context context, String[] args) throws Exception {
        String daId = args[0];
        DomainObject daObj = DomainObject.newInstance(context, daId);
        String projectIdSelect = "from[" + RELATIONSHIP_JFCHANGE2PROJECT + "].to.id";
        String projectNameSelect = "from[" + RELATIONSHIP_JFCHANGE2PROJECT + "].to.name";
        String projectDescriptionSelect = "from[" + RELATIONSHIP_JFCHANGE2PROJECT + "].to.description";
        Map daInfo = daObj.getInfo(context, StringList.create(projectIdSelect,
                projectNameSelect, projectDescriptionSelect));
        String daProjectId = UIUtil.getValue(daInfo, projectIdSelect);
        String daProjectName = UIUtil.getValue(daInfo, projectNameSelect);
        String daProjectDescription = UIUtil.getValue(daInfo, projectDescriptionSelect);
        String daProjectDisplay = UIUtil.isNotNullAndNotEmpty(daProjectDescription)
                ? daProjectDescription : daProjectName;

        StringList changeSelects = StringList.create(SELECT_TYPE, SELECT_NAME,
                projectIdSelect, projectNameSelect, projectDescriptionSelect);
        MapList relatedChanges = daObj.getRelatedObjects(context,
                RELATIONSHIP_JFDR2DA + "," + RELATIONSHIP_JFECR2DA,
                TYPE_JFDR + "," + TYPE_JFFormalECR,
                changeSelects,
                null,
                true,
                false,
                (short) 1,
                "",
                "",
                0);
        StringBuilder mismatchDetails = new StringBuilder();
        for (Object relatedChange : relatedChanges) {
            Map changeMap = (Map) relatedChange;
            String changeProjectId = UIUtil.getValue(changeMap, projectIdSelect);
            if (!Objects.equals(daProjectId, changeProjectId)) {
                String changeProjectName = UIUtil.getValue(changeMap, projectNameSelect);
                String changeProjectDescription = UIUtil.getValue(changeMap, projectDescriptionSelect);
                String changeProjectDisplay = UIUtil.isNotNullAndNotEmpty(changeProjectDescription)
                        ? changeProjectDescription : changeProjectName;
                mismatchDetails.append("\n")
                        .append(UIUtil.getValue(changeMap, SELECT_TYPE)).append(" ")
                        .append(UIUtil.getValue(changeMap, SELECT_NAME)).append(": ")
                        .append(UIUtil.isNotNullAndNotEmpty(changeProjectDisplay)
                                ? changeProjectDisplay : "-");
            }
        }
        if (mismatchDetails.length() > 0) {
            String message = EnoviaResourceBundle.getProperty(context,
                    "emxFrameworkStringResource", context.getLocale(),
                    "emxFramework.JFDA.RelatedChangeProjectMismatch");
            emxContextUtil_mxJPO.mqlNotice(context, message + "\nDA: "
                    + (UIUtil.isNotNullAndNotEmpty(daProjectDisplay) ? daProjectDisplay : "-")
                    + mismatchDetails);
            return 1;
        }
        return 0;
    }
}
