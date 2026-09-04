import com.dscn.plm.util.NioJDUtils;
import com.matrixone.apps.common.CommonDocument;
import com.matrixone.apps.common.Issue;
import com.matrixone.apps.common.WorkCalendar;
import com.matrixone.apps.common.util.ComponentsUIUtil;
import com.matrixone.apps.common.util.DocumentUtil;
import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.DomainRelationship;
import com.matrixone.apps.domain.util.*;
import com.matrixone.apps.domain.util.DateUtil;
import com.matrixone.apps.framework.ui.ProgramCallable;
import com.matrixone.apps.framework.ui.UICache;
import com.matrixone.apps.framework.ui.UIUtil;
import com.matrixone.apps.program.ProgramCentralConstants;
import com.matrixone.apps.program.ProgramCentralUtil;
import com.matrixone.apps.program.ProjectSpace;
import com.matrixone.apps.program.Task;
import jakarta.mail.BodyPart;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMultipart;
import matrix.db.*;
import matrix.util.MatrixException;
import matrix.util.SelectList;
import matrix.util.StringList;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTWorksheet;
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
import java.io.*;
import java.io.File;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.Set;
import java.util.stream.Collectors;

import static com.matrixone.apps.domain.DomainConstants.*;

public class JF_ESO_mxJPO implements JF_PLMConstants_mxJPO{

    private static final Logger JF_LOGGER = LoggerFactory.getLogger(JF_ESO_mxJPO.class);
    private static final DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("M/d/yyyy h:mm:ss a", Locale.US);
    private static final DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("yyyy年M月d日", Locale.CHINA);
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static CellStyle leftBorderStyle = null;
    private static CellStyle borderStyle = null;
    //add by LIUJR 20260618 ESO记录保存后按本次修改列统一创建或覆盖签发表文档
    private static final long ESO_REVIEW_SAVE_DEBOUNCE_TIME = 2000L;
    private static final Map<String, Long> ESO_REVIEW_SAVE_DEBOUNCE_MAP = Collections.synchronizedMap(new HashMap<String, Long>());
    private static final String ESO_REVIEW_COLUMN_REVIEW_COUNT = "JF_ReviewCounte";
    private static final String ESO_REVIEW_COLUMN_PHASE_STATE = "JF_PhaseState";
    private static final String ESO_REVIEW_COLUMN_REMARK = "JF_Remark";
    private static final String ESO_REVIEW_COLUMN_DESCRIPTION = "description";
    //end
    //add by LIUJR 20260706 ESO任务批量完成逻辑使用的固定状态和角色。
    private static final String ESO_TASK_STATE_ACTIVE = "Active";
    private static final String ESO_TASK_STATE_REVIEW = "Review";
    private static final String ESO_TASK_STATE_COMPLETE = "Complete";
    private static final String ESO_ADMIN_ROLE = "JfESOAdmin";
    //end

    public Map getRangefunctional(Context context, String[] args) throws Exception {
        Map returnMap = new HashMap();
        StringList strings = new StringList();
        try {
            Map map = (Map) JPO.unpackArgs(args);
            JF_LOGGER.info("map:{}",map);
            Properties prop = NioJDUtils.readPageObject(context, "JFJDConfig");
            String JF_Function = prop.getProperty("ESOTask.JF_Function");
            if (UIUtil.isNotNullAndNotEmpty(JF_Function)) {
                String[] split = JF_Function.split(",");
                for (String s : split) {
                    strings.add(s);
                }
            }
            returnMap.put("field_choices", strings);
            returnMap.put("field_display_choices", strings);
        } catch (Exception e) {
            JF_LOGGER.error("JF_ESO----Error in getRangefunctional", e);
        }

        return returnMap;
    }


    /**
     * R3 - TKO Range
     * @param context
     * @param args
     * @author LIUJR
     * @throws
     * @return java.util.Map
     * @date 2025/9/17 13:15
     * @description
     */
    public Map getRangeJFR3TKO(Context context, String[] args) throws Exception {
        Map returnMap = new HashMap();
        StringList strings = new StringList();
        try {
            Map map = (Map) JPO.unpackArgs(args);
            JF_LOGGER.info("map:{}",map);
//            String jfR3TKO = JF_PublicMethodClass_mxJPO.getBasicUrl(context, new String[]{"ESOTask.JF_Grade"});
            String jfR3TKO = JF_PublicMethodClass_mxJPO.getBasicUrl(context, new String[]{"ESOTask.JF_R3TKO"});
            if (UIUtil.isNotNullAndNotEmpty(jfR3TKO)) {
                String[] split = jfR3TKO.split(",");
                for (String s : split) {
                    strings.add(s);
                }
            }
            returnMap.put("field_choices", strings);
            returnMap.put("field_display_choices", strings);
        } catch (Exception e) {
            JF_LOGGER.error("JF_ESO----Error in getRangefunctional", e);
        }

        return returnMap;
    }

    public StringList getfunctional(Context context, String[] args) throws Exception {
        StringList strings = new StringList();
        try {
            HashMap programMap = JPO.unpackArgs(args);
            MapList objectList = (MapList) programMap.get("objectList");
            for (Object o : objectList) {
                Map map = (Map) o;
                String id = UIUtil.getValue(map, "id");
                DomainObject domainObject = DomainObject.newInstance(context, id);
                String attributeValue = domainObject.getAttributeValue(context, "JF_Function");
                if (UIUtil.isNotNullAndNotEmpty(attributeValue)) {
                    String[] split = attributeValue.split(";");
                    strings.add(String.join("<br/>", Arrays.asList(split)));
                } else {
                    strings.add("");
                }
            }
        } catch (Exception e) {
            JF_LOGGER.error("JF_ESO----Error in getfunctional", e);
        }

        return strings;
    }

    public StringList getR3TKO(Context context, String[] args) throws Exception {
        StringList strings = new StringList();
        try {
            HashMap programMap = JPO.unpackArgs(args);
            MapList objectList = (MapList) programMap.get("objectList");
            for (Object o : objectList) {
                Map map = (Map) o;
                String id = UIUtil.getValue(map, "id");
                DomainObject domainObject = DomainObject.newInstance(context, id);
                String attributeValue = domainObject.getAttributeValue(context, "JF_R3TKO");
                if (UIUtil.isNotNullAndNotEmpty(attributeValue)) {
                    String[] split = attributeValue.split(";");
                    strings.add(String.join("<br/>", Arrays.asList(split)));
                } else {
                    strings.add("");
                }
            }
        } catch (Exception e) {
            JF_LOGGER.error("JF_ESO----Error in getfunctional", e);
        }

        return strings;
    }

    public void updateR3TKO(Context context, String[] args) throws Exception {
        try {
            HashMap programMap = JPO.unpackArgs(args);
            Map paramMap = (Map) programMap.get("paramMap");
            String newvalue = UIUtil.getValue(paramMap, "New Value");
            String oldvalue = UIUtil.getValue(paramMap, "Old Value");
            String objectId = UIUtil.getValue(paramMap, "objectId");
            JF_LOGGER.info("JF_ESO----updatefunctional----objectId:{}" + objectId);
            JF_LOGGER.info("JF_ESO----updatefunctional----newvalue:{}" + newvalue);
            DomainObject domainObject = DomainObject.newInstance(context, objectId);
            String[] split = newvalue.split(",");
            String join = String.join(";", Arrays.asList(split));
            domainObject.setAttributeValue(context, "JF_R3TKO", join);
        } catch (Exception e) {
            JF_LOGGER.error("JF_ESO----Error in updatefunctional", e);
        }
    }

    public void updatefunctional(Context context, String[] args) throws Exception {
        try {
            HashMap programMap = JPO.unpackArgs(args);
            Map paramMap = (Map) programMap.get("paramMap");
            String newvalue = UIUtil.getValue(paramMap, "New Value");
            String oldvalue = UIUtil.getValue(paramMap, "Old Value");
            String objectId = UIUtil.getValue(paramMap, "objectId");
            JF_LOGGER.info("JF_ESO----updatefunctional----objectId:{}" + objectId);
            JF_LOGGER.info("JF_ESO----updatefunctional----newvalue:{}" + newvalue);
            DomainObject domainObject = DomainObject.newInstance(context, objectId);
            String[] split = newvalue.split(",");
            String join = String.join(";", Arrays.asList(split));
            domainObject.setAttributeValue(context, "JF_Function", join);
        } catch (Exception e) {
            JF_LOGGER.error("JF_ESO----Error in updatefunctional", e);
        }
    }

    /**
     * 职能修改权限
     * @param context
     * @param args
     * @author CHEN YL
     * @throws
     * @return matrix.util.StringList
     * @date 01/07/2025 13:59
     * @description
     */
    public StringList editfunctional(Context context, String[] args) throws Exception {
        StringList result = new StringList();
        Map paramMap = JPO.unpackArgs(args);
        try {
            MapList objectList = (MapList) paramMap.get("objectList");
            for (int i = 0; i < objectList.size(); i++) {
                Map issueMap = (Map) objectList.get(i);
                String issueId = (String) issueMap.get("id");
                DomainObject issue = DomainObject.newInstance(context, issueId);
                String issueType = issue.getInfo(context, "type");
                if ("JF_ESOTask".equals(issueType)) {
                    result.add("true");
                } else {
                    result.add("false");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 职能、部门、模块、排数、定点时间、Phase结束时间(ESO任务有编辑权限--ESO任务的责任人并且有JfESOAdmin角色有权限编辑)
     * @param context
     * @param args
     * @author LIUJR
     * @throws
     * @return matrix.util.StringList
     * @date 01/07/2025 13:59
     * @description
     */
    public StringList editESOTaskAttributeAccess(Context context, String[] args) throws Exception {
        StringList result = new StringList();
        Map paramMap = JPO.unpackArgs(args);
        try {
            MapList objectList = (MapList) paramMap.get("objectList");
            Map columnMap = (Map) paramMap.get("columnMap");
            Map colAttrMap = (Map) columnMap.get("colAttrMap");
            String colName = (String) colAttrMap.get(SELECT_NAME);
            String user = context.getUser();
            //判断当前用户时候有角色
            JF_LOGGER.info("colName:{}", colName);
            JF_LOGGER.info("user:{}", user);
            for (int i = 0; i < objectList.size(); i++) {
                Map taskMap = (Map) objectList.get(i);
                String taskId = (String) taskMap.get(DomainConstants.SELECT_ID);
                DomainObject task = DomainObject.newInstance(context, taskId);
                String taskType = task.getInfo(context, DomainConstants.SELECT_TYPE);
                JF_LOGGER.info("taskType:{}", taskType);
                String taskOwner = task.getInfo(context, DomainConstants.SELECT_OWNER);
                JF_LOGGER.info("taskOwner:{}", taskOwner);
                //判断ESO任务的责任人并且有JfESOAdmin角色有权限编辑
                if ("functional".equalsIgnoreCase(colName)) {
                    if (JF_PLMConstants_mxJPO.TYPE_JF_ESOTask.equalsIgnoreCase(taskType) && user.equalsIgnoreCase(taskOwner)) {
                        result.add("true");
                    } else {
                        result.add("false");
                    }
                } else {
                    if (user.equalsIgnoreCase(taskOwner)) {
                        result.add("true");
                    } else {
                        result.add("false");
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }


    /**
     * 颜色标识和审核次数的编辑 权限 ESO审核员 ： JfESOAdmin角色有权限编辑)
     * @param context
     * @param args
     * @author LIUJR
     * @throws
     * @return matrix.util.StringList
     * @date 01/07/2025 13:59
     * @description
     */
    public StringList editColorIdentificationAccess(Context context, String[] args) throws Exception {
        StringList result = new StringList();
        JF_LOGGER.info("editColorIdentificationAccess。。。。。。。。。。。。。。。。。。。。。。");
        Map paramMap = JPO.unpackArgs(args);
        try {
            MapList objectList = (MapList) paramMap.get("objectList");
            Map columnMap = (Map) paramMap.get("columnMap");
            Map colAttrMap = (Map) columnMap.get("colAttrMap");
            String colName = (String) colAttrMap.get(SELECT_NAME);
            String user = context.getUser();
            //判断当前用户时候有角色
            JF_LOGGER.info("colName:{}", colName);
            JF_LOGGER.info("user:{}", user);
            DomainObject task = DomainObject.newInstance(context);
            for (int i = 0; i < objectList.size(); i++) {
                Map taskMap = (Map) objectList.get(i);
                String taskId = (String) taskMap.get(DomainConstants.SELECT_ID);
                task.setId(taskId);
                JF_LOGGER.info("taskId:{}", taskId);
                String taskType = task.getInfo(context, DomainConstants.SELECT_TYPE);
                //20260729 update by ljr 模块节点的颜色标识和审核次数由最新签发记录同步，不允许在WBS中手工修改；子任务保留原权限。
                if (DomainConstants.TYPE_PROJECT_SPACE.equalsIgnoreCase(taskType)
                        || JF_PLMConstants_mxJPO.TYPE_JF_ESOTask.equalsIgnoreCase(taskType)) {
                    result.add("false");
                } else {
                    StringList userName = new StringList();
                    StringList infoList = task.getInfoList(context, "to[Assigned Tasks].from.name");
                    JF_LOGGER.info("infoList:{}", infoList);
                    for (int i1 = 0; i1 < infoList.size(); i1++) {
                        Vector assignments = PersonUtil.getAssignments(context, infoList.get(i1));
                        JF_LOGGER.info("assignments:{}", assignments);
                        if (assignments.contains("JfESOAdmin")) {
                            userName.add(infoList.get(i1));
                        }
                    }
                    JF_LOGGER.info("userName:{}", userName);
                    if (userName.contains(user)) {
                        result.add("true");
                    } else {
                        result.add("false");
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 获取颜色标识的range值
     * @param context
     * @param args
     * @author LIUJR
     * @throws
     * @return java.util.Map
     * @date 01/07/2025 16:13
     * @description
     */
    public static Map getColorIdentificationRange(Context context, String[] args) throws Exception {
        Map resultMap = new HashMap();
        try {
            Map paramsMap = (Map) JPO.unpackArgs(args);
            Map columnMap = (Map) paramsMap.get(STRING_COLUMNMAP);
            String attrName = (String) columnMap.get(DomainConstants.SELECT_NAME);
            String strLanguage = context.getSession().getLanguage();
            ContextUtil.pushContext(context);
            AttributeType attributeType = new AttributeType(attrName);
            attributeType.open(context);
            StringList choices = attributeType.getChoices();
//            if ("JF_ColorIdentification".equalsIgnoreCase(attrName)) {
//                if (choices.contains("")) {
//                    choices.remove("");
//                }
//            }
            ContextUtil.popContext(context);
            StringList choicesValue = new StringList();
            for (int i = 0; i < choices.size(); i++) {
                if ("".equalsIgnoreCase(choices.get(i)) || " ".equalsIgnoreCase(choices.get(i))) {
                    choicesValue.add("NULL");
                } else {
                    choicesValue.add(EnoviaResourceBundle.getRangeI18NString(context, attrName, choices.get(i), strLanguage));
                }
            }
            resultMap.put("field_choices", choices);
            resultMap.put("field_display_choices", choicesValue);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
        return resultMap;
    }

    /**
     *
     * @param context
     * @param args
     * @author LIUJR
     * @throws
     * @return java.util.Vector<java.lang.String>
     * @date 01/07/2025 15:27
     * @description
     */
    public Vector<String> getColorIdentificationHtml(Context context, String[] args) throws Exception {
        JF_LOGGER.info("---------------------- getColorIdentificationHtml begin ----------------------");
        Vector<String> res = new Vector<>();
        try {
            Map paramMap = JPO.unpackArgs(args);
            MapList objectList = (MapList) paramMap.get("objectList");
            for (int i = 0; i < objectList.size(); i++) {
                Map map = (Map) objectList.get(i);
                String id = (String) map.get("id");
                DomainObject domainObject = DomainObject.newInstance(context, id);
                String color = domainObject.getAttributeValue(context, "JF_ColorIdentification");
                if ("green".equals(color)) {
                    res.add("<p><span></span><span style=\"display: inline-block; width: 20px; height: 20px; background-color: green;\"></span></p>");
                } else if ("red".equals(color)) {
                    res.add("<p><span></span><span style=\"display: inline-block; width: 20px; height: 20px; background-color: red;\"></span></p>");
                } else if ("yellow".equals(color)) {
                    res.add("<p><span></span><span style=\"display: inline-block; width: 20px; height: 20px; background-color: yellow;\"></span></p>");
                } else if ("NA".equalsIgnoreCase(color)){
                    res.add("<p><span></span><span style=\"display: inline-block; width: 20px; height: 20px; background-color: grey;\"></span></p>");
                } else {
                    res.add("");
                }
            }
//            _logger.info("res:{}",res);
            JF_LOGGER.info("---------------------- getColorIdentificationHtml end ----------------------");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return res;
    }


    public Map getRange3RGrade(Context context, String[] args) throws Exception {
        Map returnMap = new HashMap();
        StringList strings = new StringList();
        try {
            Properties prop = NioJDUtils.readPageObject(context, "JFJDConfig");
            String JF_Grade = prop.getProperty("ESOTask.JF_Grade");
            if (UIUtil.isNotNullAndNotEmpty(JF_Grade)) {
                String[] split = JF_Grade.split(",");
                for (String s : split) {
                    strings.add(s);
                }
            }
            returnMap.put("field_choices", strings);
            returnMap.put("field_display_choices", strings);
        } catch (Exception e) {
            JF_LOGGER.error("JF_ESO----Error in getRange3RGrade", e);
        }

        return returnMap;
    }

    public StringList get3RGrade(Context context, String[] args) throws Exception {
        StringList strings = new StringList();
        try {
            HashMap programMap = JPO.unpackArgs(args);
            MapList objectList = (MapList) programMap.get("objectList");
            for (Object o : objectList) {
                Map map = (Map) o;
                String id = UIUtil.getValue(map, "id");
                DomainObject domainObject = DomainObject.newInstance(context, id);
                String attributeValue = domainObject.getAttributeValue(context, "JF_Grade");
                if (UIUtil.isNotNullAndNotEmpty(attributeValue)) {
                    String[] split = attributeValue.split(";");
                    strings.add(String.join("<br/>", Arrays.asList(split)));
                } else {
                    strings.add("");
                }
            }
        } catch (Exception e) {
            JF_LOGGER.error("JF_ESO----Error in get3RGrade", e);
        }

        return strings;
    }

    public void update3RGrade(Context context, String[] args) throws Exception {
        try {
            HashMap programMap = JPO.unpackArgs(args);
            Map paramMap = (Map) programMap.get("paramMap");
            String newvalue = UIUtil.getValue(paramMap, "New Value");
            String oldvalue = UIUtil.getValue(paramMap, "Old Value");
            String objectId = UIUtil.getValue(paramMap, "objectId");
            JF_LOGGER.info("JF_ESO----update3RGrade----objectId:{}" + objectId);
            JF_LOGGER.info("JF_ESO----update3RGrade----newvalue:{}" + newvalue);
            DomainObject domainObject = DomainObject.newInstance(context, objectId);
            String[] split = newvalue.split(",");
            String join = String.join(";", Arrays.asList(split));
            domainObject.setAttributeValue(context, "JF_Grade", join);
        } catch (Exception e) {
            JF_LOGGER.error("JF_ESO----Error in update3RGrade", e);
        }
    }


    public StringList getCustomersClass(Context context, String[] args) throws Exception {
        StringList result = new StringList();
        try {
            StringList objectSelects = new StringList();
            objectSelects.add("id");
            MapList mapList = DomainObject.findObjects(context, "General Library", "*", "attribute[Title]=='客户库'", objectSelects);
            for (Object o : mapList) {
                Map map = (Map) o;
                String id = UIUtil.getValue(map, "id");
                DomainObject domainObject = DomainObject.newInstance(context, id);
                MapList GeneralClass = domainObject.getRelatedObjects(context, "Subclass",
                        "General Class", objectSelects, null, false, true,
                        (short) 0, DomainConstants.EMPTY_STRING,
                        DomainConstants.EMPTY_STRING, 0);
                for (Object generalClass : GeneralClass) {
                    Map map1 = (Map) generalClass;
                    String id1 = UIUtil.getValue(map1, "id");
                    result.add(id1);
                }
            }
        } catch (Exception e) {
            JF_LOGGER.error("JF_ESO----Error in getCustomersClass", e);
        }

        return result;
    }

    public void updateProjectCustomers(Context context, String[] args) throws Exception {
        try {
            ContextUtil.pushContext(context);
            HashMap programMap = (HashMap) JPO.unpackArgs(args);
            Map paramMap = (Map) programMap.get("paramMap");
            HashMap requestMap = (HashMap) programMap.get("requestMap");
            Map fieldMap = (Map) programMap.get("fieldMap");
            String strNewId = (String) paramMap.get("New OID");
            String strNewValue = (String) paramMap.get("New Value");
            String strFieldName = (String) fieldMap.get("name");
            String strObjectId = (String) paramMap.get("objectId");
            JF_LOGGER.info("strNewId:{}", strNewId);
            JF_LOGGER.info("strNewValue:{}", strNewValue);
            JF_LOGGER.info("strFieldName:{}", strFieldName);
            JF_LOGGER.info("strObjectId:{}", strObjectId);
            //进行日期格式化，支持中英文
            SimpleDateFormat dateFormat = new SimpleDateFormat(eMatrixDateFormat.getEMatrixDateFormat(),Locale.US);
            if(strFieldName.contains("PlannedCompletionTime")) {
                if (ProgramCentralUtil.isNotNullString(strNewValue)) {
                    Locale locale = (Locale) requestMap.get("locale");
                    if (null == locale) {
                        locale = (Locale) requestMap.get("localeObj");
                    }
                    TimeZone tz = TimeZone.getTimeZone(context.getSession().getTimezone());
                    double dbMilisecondsOffset = (double) (-1) * tz.getRawOffset();
                    double clientTZOffset = (new Double(dbMilisecondsOffset / (1000 * 60 * 60))).doubleValue();
                    strNewValue = eMatrixDateFormat.getFormattedInputDate(strNewValue, clientTZOffset, locale);
                    Date newDate = dateFormat.parse(strNewValue);
                    Calendar constraintDate = Calendar.getInstance();
                    constraintDate.setTime(newDate);
                    strNewValue = dateFormat.format(constraintDate.getTime());
                }
            }
            if ("JF_ProjectCustomers".equals(strFieldName)){
                if(UIUtil.isNotNullAndNotEmpty(strNewId)) {
                    DomainObject domainObject = DomainObject.newInstance(context, strNewId);
                    String title = domainObject.getInfo(context, "attribute[Title]");
                    DomainObject project = DomainObject.newInstance(context, strObjectId);
                    project.setAttributeValue(context, "JF_ProjectCustomers", title);
                }
            }else if ("JF_ProjectCode".equals(strFieldName)){
                DomainObject project = DomainObject.newInstance(context, strObjectId);
                project.setAttributeValue(context,"JF_ProjectCode",strNewValue);
            }else if ("JF_ProjectGrade".equals(strFieldName)){
                DomainObject project = DomainObject.newInstance(context, strObjectId);
                project.setAttributeValue(context,"JF_ProjectGrade",strNewValue);
            }else if ("JF_ProjectStatus".equals(strFieldName)){
                DomainObject project = DomainObject.newInstance(context, strObjectId);
                project.setAttributeValue(context,"JF_ProjectStatus",strNewValue);
            }else if ("JF_ProjectECI".equals(strFieldName)){
                DomainObject project = DomainObject.newInstance(context, strObjectId);
                project.setAttributeValue(context,"JF_ProjectECI",strNewValue);

            }

            else if ("JF_Phase1PlannedCompletionTime".equals(strFieldName)){
                DomainObject project = DomainObject.newInstance(context, strObjectId);
                project.setAttributeValue(context,"JF_Phase1PlannedCompletionTime",strNewValue);
            }  else if ("JF_Phase2PlannedCompletionTime".equals(strFieldName)){
                DomainObject project = DomainObject.newInstance(context, strObjectId);
                project.setAttributeValue(context,"JF_Phase2PlannedCompletionTime",strNewValue);
            }  else if ("JF_Phase3PlannedCompletionTime".equals(strFieldName)){
                DomainObject project = DomainObject.newInstance(context, strObjectId);
                project.setAttributeValue(context,"JF_Phase3PlannedCompletionTime",strNewValue);
            }  else if ("JF_Phase2_3PlannedCompletionTime".equals(strFieldName)){
                DomainObject project = DomainObject.newInstance(context, strObjectId);
                project.setAttributeValue(context,"JF_Phase2_3PlannedCompletionTime",strNewValue);
            }else if ("JF_Phase4PlannedCompletionTime".equals(strFieldName)){
                DomainObject project = DomainObject.newInstance(context, strObjectId);
                project.setAttributeValue(context,"JF_Phase4PlannedCompletionTime",strNewValue);
            }  else if ("JF_Phase5PlannedCompletionTime".equals(strFieldName)){
                DomainObject project = DomainObject.newInstance(context, strObjectId);
                project.setAttributeValue(context,"JF_Phase5PlannedCompletionTime",strNewValue);
            }else if ("JSOutSourceRev".equals(strFieldName)){
                DomainObject project = DomainObject.newInstance(context, strObjectId);
                project.setAttributeValue(context,"JSOutSourceRev",strNewValue);
            }
        }catch (Exception e){
            JF_LOGGER.error("JF_ESO----Error in updateProjectCustomers", e);
        }finally {
            ContextUtil.popContext(context);
        }

    }

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

    /**
     * 项目详细页面编辑按钮权限
     * @param context
     * @param args
     * @return
     * @throws Exception
     */
    public boolean getPMCProjectDetailsAccess(Context context, String[] args) throws Exception {
        String user = context.getUser();
        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        JF_LOGGER.info("getPMCProjectDetailsAccess----programMap:{}", programMap);
        String objectId = (String) programMap.get("objectId");
        DomainObject domainObject = DomainObject.newInstance(context, objectId);
        StringList strings = new StringList();
        strings.add("current");
        strings.add("current.access[modify]");
        Map info = domainObject.getInfo(context, strings);
        String current = UIUtil.getValue(info, "current");
        String modify = UIUtil.getValue(info, "current.access[modify]");
        JF_LOGGER.info("getPMCProjectDetailsAccess----modify:{}", modify);
        Map roleMap = getProjectRole(context,objectId,"Chair manager");
        String name = UIUtil.getValue(roleMap, "name");
        JF_LOGGER.info("getPMCProjectDetailsAccess----name:{}", name);
        //如果是ESOadmin 也可以编辑
        Vector assignments = PersonUtil.getAssignments(context, user);
        boolean esoFlag = assignments.contains("JfESOAdmin");
        return (Objects.equals(user, name) || "true".equalsIgnoreCase(modify) || esoFlag) && !"Complete".equals(current);
    }

    /**
     * 1. 整椅经理有权限，编辑ESO信息下面一段属性值，其他属性不可编辑
     * @param context
     * @param args
     * @return
     * @throws Exception
     */
    @ProgramCallable
    public boolean notEditAttributeAccess(Context context, String[] args) throws Exception {
        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        JF_LOGGER.info("notEditAttributeAccess----programMap:{}", programMap);
        Map requestMap = (Map) programMap.get("requestMap");
        String objectId = (String) requestMap.get("objectId");
        JF_LOGGER.info("notEditAttributeAccess----objectId:{}", objectId);
        DomainObject domainObject = DomainObject.newInstance(context, objectId);
        StringList strings = new StringList();
        strings.add("current");
        strings.add("current.access[modify]");
        Map info = domainObject.getInfo(context, strings);
        String current = UIUtil.getValue(info, "current");
        String modify = UIUtil.getValue(info, "current.access[modify]");
        JF_LOGGER.info("notEditAttributeAccess----modify:{}", modify);
        return  "true".equalsIgnoreCase(modify) && !"Complete".equals(current);
    }

    public boolean notEditAttributeAccessForChairmanager(Context context, String[] args) throws Exception {
        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        JF_LOGGER.info("notEditAttributeAccess----programMap:{}", programMap);
        Map requestMap = (Map) programMap.get("requestMap");
        String objectId = (String) requestMap.get("objectId");
        JF_LOGGER.info("notEditAttributeAccess----objectId:{}", objectId);
        DomainObject domainObject = DomainObject.newInstance(context, objectId);
        Map roleMap = getProjectRole(context,objectId,"Chair manager");
        String name = UIUtil.getValue(roleMap, "name");
        return context.getUser().equalsIgnoreCase(name);
    }

    public MapList getEsoReviewInformation(Context context, String[] args) throws Exception {
        MapList mapList = new MapList();
        ContextUtil.pushContext(context);
        StringList boSel = JF_Util_mxJPO.basicBolistSel();
        StringList relList = JF_Util_mxJPO.basicRellistSel();
        try {
            Map argsMap = JPO.unpackArgs(args);
            JF_LOGGER.info("JF_ESO------getEsoReviewInformation---argsMap:{}", argsMap);
            String taskId = UIUtil.getValue(argsMap, "objectId");
            DomainObject taskObj = DomainObject.newInstance(context, taskId);
            //add by ljr 20251022 获取TASK的所属项目 所属阶段 ESOType类型
            String projectName = taskObj.getInfo(context, "to[" + RELATIONSHIP_PROJECT_ACCESS_KEY + "].from.from[" + RELATIONSHIP_PROJECT_ACCESS_LIST + "].to.description");
            String projectId = taskObj.getInfo(context, "to[" + RELATIONSHIP_PROJECT_ACCESS_KEY + "].from.from[" + RELATIONSHIP_PROJECT_ACCESS_LIST + "].to.id");
            if (UIUtil.isNullOrEmpty(projectName)) {
                projectName = taskObj.getInfo(context, "to[" + RELATIONSHIP_PROJECT_ACCESS_KEY + "].from.from[" + RELATIONSHIP_PROJECT_ACCESS_LIST + "].to.name");
            }
            String esoTaskPhaseName = JF_ESO_mxJPO.getESOTaskPhaseId(context, new String[]{taskId, SELECT_NAME});
            String type = taskObj.getInfo(context, SELECT_TYPE);
            String esoType = EMPTY_STRING;
            if (JF_PLMConstants_mxJPO.TYPE_JF_ESOTask.equalsIgnoreCase(type)) {
                esoType = taskObj.getAttributeValue(context, JF_PLMConstants_mxJPO.ATTR_JF_ESOType);
            }
            //end
            JF_LOGGER.info("taskId:{}", taskId);
            mapList = taskObj.getRelatedObjects(context, "JFESOTask2ESOReview", "JFESOReview", boSel, relList, false, true, (short) 1, "", "", 0);
            Iterator iterator = mapList.iterator();
            while (iterator.hasNext()) {
                Map map = (Map) iterator.next();
                map.put("JF_belongProject", projectName);
                map.put("JF_belongPhase", esoTaskPhaseName);
                map.put("JF_ESOType", esoType);
                map.put("projectId", projectId);
            }
        }catch (Exception e){
            JF_LOGGER.info("JF_ESO------getEsoReviewInformation error",e);
        }finally {
            ContextUtil.popContext(context);
        }
        return mapList;
    }


    public void createESOReviewRecord(Context context , String[] args) throws Exception{
        String objectId = args[0];
        try {
            String projectId = JF_PublicProjectQuery_mxJPO.getProjectIdByTaskId(context, objectId);
            JF_LOGGER.info("projectId:{}", projectId);
            DomainObject projectObj = DomainObject.newInstance(context, projectId);
            StringList strings = new StringList();
            strings.add("attribute[JF_Phase1PlannedCompletionTime]");
            strings.add("attribute[JF_Phase2PlannedCompletionTime]");
            strings.add("attribute[JF_Phase3PlannedCompletionTime]");
            strings.add("attribute[JF_Phase2_3PlannedCompletionTime]");
            strings.add("attribute[JF_Phase4PlannedCompletionTime]");
            strings.add("attribute[JF_Phase5PlannedCompletionTime]");
            Map projectObjInfo = projectObj.getInfo(context, strings);
            DomainObject taskObj = DomainObject.newInstance(context, objectId);
            MapList  allPhase = taskObj.getRelatedObjects(context,"Subtask","Phase,Task",StringList.create("type","id","attribute[Task Estimated Finish Date]"),new StringList(),
                    true,false,(short)0,"","",0);
            JF_LOGGER.info("allPhase:{}", allPhase);
            String dateFinish = "";
            for (Object o : allPhase) {
                Map map = (Map) o;
                String type = UIUtil.getValue(map, "type");
                String id = UIUtil.getValue(map, "id");
                if ("Phase".equals(type)){
                    DomainObject phaseObj = DomainObject.newInstance(context, id);
                    String projectName = phaseObj.getInfo(context, "to[Subtask].from[Project Space].name");
                    if (UIUtil.isNotNullAndNotEmpty(projectName)){
                        String phaseName = phaseObj.getInfo(context, "name");
                        JF_LOGGER.info("phaseName:{}", phaseName);
                        if (phaseName.contains("P-2+3")){
                            dateFinish = UIUtil.getValue(projectObjInfo,"attribute[JF_Phase2_3PlannedCompletionTime]");
                        }else if (phaseName.contains("P-1")){
                            dateFinish = UIUtil.getValue(projectObjInfo,"attribute[JF_Phase1PlannedCompletionTime]");
                        }else if (phaseName.contains("P-2")){
                            dateFinish = UIUtil.getValue(projectObjInfo,"attribute[JF_Phase2PlannedCompletionTime]");
                        }else if (phaseName.contains("P-3")){
                            dateFinish = UIUtil.getValue(projectObjInfo,"attribute[JF_Phase3PlannedCompletionTime]");
                        }else if (phaseName.contains("P-4")){
                            dateFinish = UIUtil.getValue(projectObjInfo,"attribute[JF_Phase4PlannedCompletionTime]");
                        }else if (phaseName.contains("P-5")){
                            dateFinish = UIUtil.getValue(projectObjInfo,"attribute[JF_Phase5PlannedCompletionTime]");
                        }else {
                            dateFinish = UIUtil.getValue(map, "attribute[Task Estimated Finish Date]");
                        }
                        JF_LOGGER.info("dateFinish:{}", dateFinish);
                        break;
                    }
                }
            }
            String esoReviewId = FrameworkUtil.autoName(context,"type_JFESOReview","policy_JFESOReview");
            JF_LOGGER.info("esoReviewId:{}", esoReviewId);
            DomainObject esoReviewObject = DomainObject.newInstance(context, esoReviewId);
            //关联
            ContextUtil.pushContext(context);
            taskObj.addToObject(context,new RelationshipType("JFESOTask2ESOReview"),esoReviewId);
            ContextUtil.popContext(context);
            String originated = esoReviewObject.getInfo(context, "originated");
            Map setattrMap = new HashMap();
            setattrMap.put("JF_EstimatedSignDate",dateFinish);
            setattrMap.put("JF_ActualSignDate",originated);
            esoReviewObject.setAttributeValues(context,setattrMap);
        }catch (Exception e){
            JF_LOGGER.info("JF_ESO------createESOReviewRecord error:",e);
        }
    }


    /**
     * ECO创建的时候  显示当前所在的phase
     * @param context
     * @param args
     * @author LIUJR
     * @throws
     * @return java.lang.String
     * @date 20/06/2025 10:12
     * @description
     */
    public String getESOTaskPhase(Context context, String[] args) throws Exception {
        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        JF_LOGGER.info("getESOTaskPhase----programMap:{}", programMap);
        HashMap requestMap = (HashMap) programMap.get("requestMap");
        String objectId = (String) requestMap.get("objectId");
        DomainObject domainObject = DomainObject.newInstance(context);
        domainObject.setId(objectId);
        JF_LOGGER.info("objectId:{}", objectId);
        StringList busSelectList = new StringList();
        busSelectList.add(DomainConstants.SELECT_ID);
        busSelectList.add(DomainConstants.SELECT_TYPE);
        busSelectList.add(DomainConstants.SELECT_NAME);
        busSelectList.add(DomainConstants.SELECT_LEVEL);
        busSelectList.add(DomainConstants.SELECT_ORIGINATED);
        //判断当前任务的阶段是否在ESO项目模板中匹配
        MapList mapList = domainObject.getRelatedObjects(
                context,
                DomainRelationship.RELATIONSHIP_SUBTASK,
                DomainConstants.TYPE_TASK + ",Phase",
                busSelectList,
                new StringList(),
                true,
                false,
                (short) 0,
                "",
                "",
                0
        );
        JF_LOGGER.info("mapList:{}", mapList);
        mapList = (MapList) mapList.stream().filter(m -> {
            Map map = (Map) m;
            String strType = UIUtil.getValue(map, DomainConstants.SELECT_TYPE);
            if ("Phase".equalsIgnoreCase(strType)) {
                return Boolean.TRUE;
            } else {
                return Boolean.FALSE;
            }
        }).collect(Collectors.toCollection(MapList::new));
        JF_LOGGER.info("mapList:{}", mapList);
        if (mapList.isEmpty()) {
            return "";
        } else {
            mapList.addSortKey(DomainConstants.SELECT_ORIGINATED, ProgramCentralConstants.ASCENDING_SORT, ProgramCentralConstants.SORTTYPE_STRING);
            mapList.sort();
            Map map = (Map) mapList.get(0);
            String phaseName = (String) map.get(DomainConstants.SELECT_NAME);
            JF_LOGGER.info("phaseName:{}", phaseName);
            return phaseName;
        }
    }

    /**
     * 创建eso任务 phase
     * @param context
     * @param args
     * @author LIUJR
     * @throws
     * @return java.util.HashMap
     * @date 2025/9/17 15:07
     * @description
     */
    public HashMap getPhaseCombobox(Context context, String[] args) throws Exception {
        HashMap rangeMap = new HashMap();
        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        JF_LOGGER.info("getESOTaskPhase----programMap:{}", programMap);
        HashMap requestMap = (HashMap) programMap.get("requestMap");
        String objectId = (String) requestMap.get("objectId");
        String projectId = (String) requestMap.get("projectId");
        String tempId = (String) requestMap.get("tempId");
        DomainObject domainObject = DomainObject.newInstance(context);
        domainObject.setId(objectId);
        JF_LOGGER.info("objectId:{}", objectId);
        StringList busSelectList = new StringList();
        busSelectList.add(DomainConstants.SELECT_ID);
        busSelectList.add(DomainConstants.SELECT_TYPE);
        busSelectList.add(DomainConstants.SELECT_NAME);
        busSelectList.add(DomainConstants.SELECT_LEVEL);
        busSelectList.add(DomainConstants.SELECT_ORIGINATED);
        //判断当前任务的阶段是否在ESO项目模板中匹配
        MapList mapList = domainObject.getRelatedObjects(
                context,
                DomainRelationship.RELATIONSHIP_SUBTASK,
                DomainConstants.TYPE_TASK + ",Phase",
                busSelectList,
                new StringList(),
                true,
                false,
                (short) 0,
                "",
                "",
                0
        );
        JF_LOGGER.info("mapList:{}", mapList);
        mapList = (MapList) mapList.stream().filter(m -> {
            Map map = (Map) m;
            String strType = UIUtil.getValue(map, DomainConstants.SELECT_TYPE);
            if ("Phase".equalsIgnoreCase(strType)) {
                return Boolean.TRUE;
            } else {
                return Boolean.FALSE;
            }
        }).collect(Collectors.toCollection(MapList::new));
        JF_LOGGER.info("mapList:{}", mapList);
        // 使用 collect 收集成两个列表
        StringList matchedNames = new StringList();
        StringList matchedProjectIds = new StringList();
        String phaseName = DomainConstants.EMPTY_STRING;
        if (mapList.isEmpty()) {
            rangeMap.put("field_display_choices", matchedNames);
            rangeMap.put("field_choices", matchedNames);
            return rangeMap;
        } else {
            mapList.addSortKey(DomainConstants.SELECT_ORIGINATED, ProgramCentralConstants.ASCENDING_SORT, ProgramCentralConstants.SORTTYPE_STRING);
            mapList.sort();
            Map map = (Map) mapList.get(0);
            phaseName = (String) map.get(DomainConstants.SELECT_NAME);
            JF_LOGGER.info("phaseName:{}", phaseName);
            matchedNames.add(phaseName);
            matchedProjectIds.add(UIUtil.getValue(map, SELECT_ID));
        }
        //将项目的与项目模板共同的phase拿出来，加进入
        domainObject.setId(tempId);
        MapList tempPhaseSelList = domainObject.getRelatedObjects(
                context,
                DomainRelationship.RELATIONSHIP_SUBTASK,
                "Phase",
                busSelectList,
                new StringList(),
                false,
                true,
                (short) 1,
                "",
                "",
                0
        );
        JF_LOGGER.info("tempPhaseSelList:{}", tempPhaseSelList);
        domainObject.setId(projectId);
        MapList projectPhaseSelList = domainObject.getRelatedObjects(
                context,
                DomainRelationship.RELATIONSHIP_SUBTASK,
                "Phase",
                busSelectList,
                new StringList(),
                false,
                true,
                (short) 1,
                "",
                "",
                0
        );
        JF_LOGGER.info("projectPhaseSelList:{}", projectPhaseSelList);
        Map<String, String> projectNameToIdMap = (Map<String, String>) projectPhaseSelList.stream()
                .collect(Collectors.toMap(
                        map -> ((Map<String, Object>) map).get(SELECT_NAME).toString(),
                        map -> ((Map<String, Object>) map).get(SELECT_ID).toString()
                ));

        String finalPhaseName = phaseName;
        tempPhaseSelList.stream()
                .map(item -> ((Map<String, Object>) item).get("name").toString())
                .distinct()
                .filter(name -> projectNameToIdMap.containsKey(name))
                .forEach(name -> {
                    if (!finalPhaseName.equalsIgnoreCase((String)name)) {
                        matchedNames.add((String) name);
                        matchedProjectIds.add(projectNameToIdMap.get(name));
                    }

                });
        rangeMap.put("field_choices", matchedProjectIds);
        rangeMap.put("field_display_choices", matchedNames);
        return rangeMap;
    }

    /**
     * 当前所在的phase Id
     * @param context
     * @param args
     * @author LIUJR
     * @throws
     * @return java.lang.String
     * @date 20/06/2025 10:12
     * @description
     */
    public static String getESOTaskPhaseId(Context context, String[] args) throws Exception {
        String objectId = args[0];
        String key = args[1];
        DomainObject domainObject = DomainObject.newInstance(context);
        domainObject.setId(objectId);
        JF_LOGGER.info("objectId:{}", objectId);
        StringList busSelectList = new StringList();
        busSelectList.add(DomainConstants.SELECT_ID);
        busSelectList.add(DomainConstants.SELECT_TYPE);
        busSelectList.add(DomainConstants.SELECT_NAME);
        busSelectList.add(DomainConstants.SELECT_LEVEL);
        busSelectList.add(DomainConstants.SELECT_ORIGINATED);
        //判断当前任务的阶段是否在ESO项目模板中匹配
        MapList mapList = domainObject.getRelatedObjects(
                context,
                DomainRelationship.RELATIONSHIP_SUBTASK,
                DomainConstants.TYPE_TASK + ",Phase",
                busSelectList,
                new StringList(),
                true,
                false,
                (short) 0,
                "",
                "",
                0
        );
        JF_LOGGER.info("mapList:{}", mapList);
        mapList = (MapList) mapList.stream().filter(m -> {
            Map map = (Map) m;
            String strType = UIUtil.getValue(map, DomainConstants.SELECT_TYPE);
            if ("Phase".equalsIgnoreCase(strType)) {
                return Boolean.TRUE;
            } else {
                return Boolean.FALSE;
            }
        }).collect(Collectors.toCollection(MapList::new));
        JF_LOGGER.info("mapList:{}", mapList);
        if (mapList.isEmpty()) {
            return "";
        } else {
            mapList.addSortKey(DomainConstants.SELECT_ORIGINATED, ProgramCentralConstants.ASCENDING_SORT, ProgramCentralConstants.SORTTYPE_STRING);
            mapList.sort();
            Map map = (Map) mapList.get(0);
            return (String) map.get(key);
        }
    }

    /**
     * ESO创建界面联动
     * @param context
     * @param args
     * @author LIUJR
     * @throws
     * @return java.util.Map
     * @date 2025/9/18 16:28
     * @description
     */
    public Map getRangeFilter(Context context, String[] args) throws Exception {
        Map returnMap = new HashMap();
        try {
            Map map = (Map) JPO.unpackArgs(args);
            JF_LOGGER.info("map:{}", map);
            Map requestMap = (Map) map.get("requestMap");
            Map fieldMap = (Map) map.get("fieldMap");
            HashMap fieldValues = (HashMap)map.get("fieldValues");
            String objectId = (String) requestMap.get(JF_PLMConstants_mxJPO.STRING_OBJECTID);
            DomainObject domainObject = DomainObject.newInstance(context, objectId);
            String name = (String) fieldMap.get(SELECT_NAME);
            Page pageAttributePopulation = new Page("SignTaskProperties_zh.xml");
            pageAttributePopulation.open(context);
            String strProperties = pageAttributePopulation.getContents(context);
            JF_LOGGER.info("strProperties:",strProperties);
            pageAttributePopulation.close(context);
            String expression = DomainConstants.EMPTY_STRING;
            if ("JF_Function,JF_Rows".contains(name)) {
                expression = String.format("/configurations/configuration[@id='ESOTask']/%s/@Value", name);
            } else if ("JF_Modules".equalsIgnoreCase(name)) {
                // 构建安全的 XPath 表达式（防止子串误匹配）
                expression = String.format(
                        "/configurations/configuration[@id='ESOTask']/%s[" +
                                "contains(concat(',', @JF_Function, ','), ',%s,')" +
                                "]/@Value",
                        name, UIUtil.getValue(fieldValues, "JF_Function")
                );

            } else if ("JF_Locations".equalsIgnoreCase(name)) {

                expression = String.format(
                        "/configurations/configuration[@id='ESOTask']/%s[" +
                                "contains(concat(',', @JF_Modules, ','), ',%s,')" +
                                "]/@Value",
                        name, UIUtil.getValue(fieldValues, "JF_Modules")
                );

            } else if("JF_Department".equalsIgnoreCase(name)) {
                expression = String.format(
                        "/configurations/configuration[@id='ESOTask']/%s[" +
                                "contains(concat(',', @JF_Modules, ','), ',%s,') and " +
                                "contains(concat(',', @JF_Locations, ','), ',%s,')" +
                                "]/@Value",
                        name, UIUtil.getValue(fieldValues, "JF_Modules"), UIUtil.getValue(fieldValues, "JF_Locations")
                );
            } else if ("JF_ESOType".equalsIgnoreCase(name)) {
                expression = String.format(
                        "/configurations/configuration[@id='ESOTask']/%s[" +
                                "contains(concat(',', @JF_R2_TKO, ','), ',%s,')" +
                                "]/@Value",
                        name, UIUtil.getValue(fieldValues, "JF_R2_TKO"));
            }
            InputStream inputStream = new ByteArrayInputStream(strProperties.getBytes("UTF-8"));
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(inputStream);
            String value = findAttributeWithXPath(document, expression);
            StringList stringList = StringList.create(value.split(","));
            returnMap.put("RangeValues", stringList);
            returnMap.put("RangeDisplayValues", stringList);
        }catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
        return returnMap;
    }

    /**
     *
     * @param doc
     * @param expression
     * @author LIUJR
     * @throws
     * @return matrix.util.StringList
     * @date 2025/9/18 15:19
     * @description
     */
    public static String findAttributeWithXPath(Document doc, String expression) throws XPathExpressionException {

        // 创建 XPath 工厂和解析器
        XPathFactory xPathFactory = XPathFactory.newInstance();
        XPath xpath = xPathFactory.newXPath();
        // 构建 XPath 表达式
//        String expression = String.format("/configurations/configuration[@id='%s']/config_%s/", configId, config);
//        String expression = String.format("/configurations/configuration[@id='%s']/partType[@id='%s']/attribute/@name", configId, partListTypeId);
        JF_LOGGER.info("expression:{}", expression);
        // 执行查询
        NodeList nodes = (NodeList) xpath.compile(expression).evaluate(doc, XPathConstants.NODESET);
        // 检查是否找到节点
        if (nodes.getLength() == 0) {
            return DomainConstants.EMPTY_STRING;
        }
        String Value = DomainConstants.EMPTY_STRING;
        // 5. 遍历结果并存入Map
        for (int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.item(i);
            Value = node.getNodeValue();
        }
        return Value;
    }

    /**
     * 加载JS
     * @param context
     * @param args
     * @author LIUJR
     * @throws
     * @return java.lang.String
     * @date 2025/9/19 13:57
     * @description
     */
    public String runJavascriptLoader(Context context, String[] args) throws Exception {
        Map programMap = (Map) JPO.unpackArgs(args);
        JF_LOGGER.info("runJavascriptLoader-----------");
        JF_LOGGER.info("programMap:{}", programMap);
        return  "<script>" +
                "window.addEventListener(" +
                "'load', " +
                "function JFOnloadHandler() {\n" +
                "emxFormReloadField(\"JF_Modules\");}, " +
                "false);" +
                "</script>";
    }


    /**
     * 职能显示  过滤  1. 创建ESO任务的时候，如果选择的职能是 CS，勾选的ESO任务的职能不可以是Structure、Subsystem
     * @param context
     * @param args
     * @author LIUJR
     * @throws
     * @return java.util.Map
     * @date 23/06/2025 13:36
     * @description
     */
    public Map getRangefunctionalFilter(Context context, String[] args) throws Exception {
        Map returnMap = new HashMap();
        StringList strings = new StringList();
        try {
            Map map = (Map) JPO.unpackArgs(args);
            JF_LOGGER.info("map:{}",map);
            Map requestMap = (Map) map.get("requestMap");
            String objectId = (String) requestMap.get(JF_PLMConstants_mxJPO.STRING_OBJECTID);
            DomainObject domainObject = DomainObject.newInstance(context, objectId);
            String strFunction = domainObject.getAttributeValue(context, JF_PLMConstants_mxJPO.ATTR_JF_Function);
            Properties prop = NioJDUtils.readPageObject(context, "JFJDConfig");
            String JF_Function = prop.getProperty("ESOTask.JF_Function");
            StringList removeList = new StringList();
            //CS,Structure,Subsystem
            if (strFunction.contains("CS")) {
                removeList.add("CS");
//                removeList.add("CS_JIT");
            }
            if ("Structure".equalsIgnoreCase(strFunction)) {
                removeList.add("CS");
                removeList.add("Structure");
                removeList.add("Subsystem");
                removeList.add("EE");
                removeList.add("CS_JIT");
            }
            if ("Subsystem".equalsIgnoreCase(strFunction)) {
                removeList.add("CS");
                removeList.add("Structure");
                removeList.add("Subsystem");
                removeList.add("EE");
                removeList.add("CS_JIT");
            }
            if (UIUtil.isNotNullAndNotEmpty(JF_Function)) {
                String[] split = JF_Function.split(",");
                for (String s : split) {
                    if (removeList.contains(s)) {
                        continue;
                    }
                    strings.add(s);
                }
            }
            returnMap.put("field_choices", strings);
            returnMap.put("field_display_choices", strings);
        } catch (Exception e) {
            JF_LOGGER.error("JF_ESO----Error in getRangefunctional", e);
        }
        return returnMap;
    }

    /**
     * Reload Function   模块驱动部门的range值变化
     * @param context
     * @param args
     * @author LIUJR
     * @throws
     * @return java.util.HashMap
     * @date 2025/8/21 10:00
     * @description
     */
    public static Map getCreateESOAttrRange(Context context, String[] args) throws Exception {
        try {
            Map resultMap = new HashMap();
            HashMap paramsMap = (HashMap)JPO.unpackArgs(args);
            HashMap var7 = (HashMap)paramsMap.get("fieldValues");
            JF_LOGGER.info("paramsMap:{}", paramsMap);
            Map requestMap = (Map) paramsMap.get(JF_PLMConstants_mxJPO.STRING_FIELDMAP);
            String fieldName = UIUtil.getValue(requestMap, DomainConstants.SELECT_NAME);
            ContextUtil.pushContext(context);
            AttributeType attributeType = new AttributeType(fieldName);
            attributeType.open(context);
            StringList choices = attributeType.getChoices();
            ContextUtil.popContext(context);
            if ("JF_ESOType".equalsIgnoreCase(fieldName)) {
                choices.remove("TKO");
                choices.remove("Phase");
            }
            resultMap.put("field_choices", choices);
            resultMap.put("field_display_choices", choices);
            return resultMap;
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * 获取项目的顺序
     * @param context
     * @param objectid
     * @author LIUJR
     * @throws
     * @return com.matrixone.apps.domain.util.MapList
     * @date 2025/8/7 10:24
     * @description
     */
    public static MapList getObjectListFromPAL(Context context,String objectid)throws Exception{
        ProjectSpace rootNodeObj = new ProjectSpace(objectid);
        StringList strSelects = new StringList();
        strSelects.add("physicalid");
        strSelects.add(SELECT_NAME);
        strSelects.add(SELECT_ID);
        MapList rootNodeObjlist = rootNodeObj.getObjectListFromPAL(context,strSelects , (short)0);
        return rootNodeObjlist;
    }

    /**
     *
     * @param context
     * @param args
     * @author LIUJR
     * @throws
     * @return void
     * @date 2025/8/7 17:45
     * @description
     */
    public void createPostProcess(Context context, String[] args) {
        try {
            Map paramsMap = (Map) JPO.unpackArgs(args);
            JF_LOGGER.info("args：{}", paramsMap.toString());
            Map requestMap = (Map) paramsMap.get(STRING_REQUESTMAP);
            Map paramMap = (Map) paramsMap.get(STRING_PARAMMAP);
            String objectId = (String) paramMap.get(STRING_OBJECTID);
            if (UIUtil.isNullOrEmpty(objectId)) {
                objectId = (String) paramMap.get("newObjectId");
            }
            DomainObject object = DomainObject.newInstance(context, objectId);
            String attributeValue = object.getAttributeValue(context, JF_PLMConstants_mxJPO.ATTR_JF_R2_TKO);
            if ("Yes".equalsIgnoreCase(attributeValue)) {
                object.setAttributeValue(context, JF_PLMConstants_mxJPO.ATTR_JF_ESOType, "TKO");
                object.setAttributeValue(context, JF_PLMConstants_mxJPO.ATTR_JF_BackESOType, "TKO");
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * create JPO :创建ESO 任务
     * @param context
     * @param args
     * @author LIUJR
     * @throws
     * @return java.util.Map
     * @date 20/06/2025 10:29
     * @description
     */
    public Map createESOTask(Context context, String[] args) throws Exception{
        Map<String, String> returnMap = new HashMap<>();
        String taskESOId = DomainConstants.EMPTY_STRING;
        StringList esoList = new StringList();
        String duration = "";
        String finishDate = "";
        String startDate = "";
        String constraintDate = "";
        String taskConstraintType = "";
        try {
            ContextUtil.startTransaction(context, true);
            Map map = (Map) JPO.unpackArgs(args);
            //入参
            String tempId = (String) map.get("tempId");   //ESO任务模板id
            String esoTaskPhaseId = (String) map.get("Phase");   //ESO 阶段
            JF_LOGGER.info("esoTaskPhaseId:{}", esoTaskPhaseId);
            String projectId = (String) map.get("projectId");   //项目id
            String objectId = (String) map.get(JF_PLMConstants_mxJPO.STRING_OBJECTID);  //选择节点id
            //form页面上填写
            String strJF_Function = (String) map.get("JF_Function");
            String strJF_Rows = (String) map.get("JF_Rows");
            String strJF_Modules = (String) map.get("JF_Modules");
            String strJF_Locations = (String) map.get("JF_Locations");
            String strJF_Department = (String) map.get("JF_Department");
            String strJF_R2_TKO = (String) map.get("JF_R2_TKO");
            String strJF_ESOType = (String) map.get("JF_ESOType");
            String description = (String) map.get("description");
            //获取项目的Grade属性
            String strJF_Grade = DomainObject.newInstance(context, projectId).getAttributeValue(context, JF_PLMConstants_mxJPO.ATTR_JF_ProjectGrade);
            //获取选择节点的owner 和人员id
            DomainObject domainObject = DomainObject.newInstance(context);
            domainObject.setId(objectId);
            String owner = domainObject.getOwner(context).getName();
            String personObjectID = PersonUtil.getPersonObjectID(context, owner);
            //当前节点所在的第一个层级的phase id
//            String esoTaskPhaseId = JF_ESO_mxJPO.getESOTaskPhaseId(context, new String[]{objectId, DomainConstants.SELECT_ID});
            //找到当前是否是ESO 任务或者上级得ESO 任务id
            String name = domainObject.getInfo(context, SELECT_NAME);
            StringList basicBolistSel = JF_Util_mxJPO.basicBolistSel();
            StringList basicRellistSel = JF_Util_mxJPO.basicRellistSel();
            if ("ESO".equalsIgnoreCase(name) || "ESO\u7b7e\u53d1".equalsIgnoreCase(name)) {
                taskESOId = objectId;
            } else {
                MapList mapList = domainObject.getRelatedObjects(context,
                        DomainRelationship.RELATIONSHIP_SUBTASK,
                        DomainConstants.TYPE_TASK,
                        basicBolistSel,
                        basicRellistSel,
                        true,
                        false,
                        (short) 0,
                        "",
                        "",
                        0
                );
                for (int i = 0; i < mapList.size(); i++) {
                    Map map1 = (Map) mapList.get(i);
                    String name1 = UIUtil.getValue(map1, SELECT_NAME);
                    String id1 = UIUtil.getValue(map1, SELECT_ID);
                    if ("ESO".equalsIgnoreCase(name1) || "ESO\u7b7e\u53d1".equalsIgnoreCase(name1)) {
                        taskESOId = id1;
                        break;
                    }
                }
            }
            DomainObject domainESOTask = DomainObject.newInstance(context, taskESOId);
            duration = domainESOTask.getAttributeValue(context, "Task Estimated Duration");
            finishDate = domainESOTask.getAttributeValue(context, "Task Estimated Finish Date");
            startDate = domainESOTask.getAttributeValue(context, "Task Estimated Start Date");
            constraintDate = domainESOTask.getAttributeValue(context, "Task Constraint Date");
            taskConstraintType = domainESOTask.getAttributeValue(context, "Task Constraint Type");
            //拿取层级节点的计划开始时间 和计划结束时间 和计划持续时间
            AttributeList attributeList = new AttributeList();
            attributeList.add(new Attribute(JF_PLMConstants_mxJPO.ATTR_JF_Rows, strJF_Rows));
            attributeList.add(new Attribute(JF_PLMConstants_mxJPO.ATTR_JF_Modules, strJF_Modules));
            attributeList.add(new Attribute(JF_PLMConstants_mxJPO.ATTR_JF_Locations, strJF_Locations));
            attributeList.add(new Attribute(JF_PLMConstants_mxJPO.ATTR_JF_Department, strJF_Department));
            attributeList.add(new Attribute(JF_PLMConstants_mxJPO.ATTR_JF_Grade, strJF_Grade));
            attributeList.add(new Attribute(JF_PLMConstants_mxJPO.ATTR_JF_Function, strJF_Function));
            attributeList.add(new Attribute(ATTRIBUTE_NOTES, DomainObject.newInstance(context, esoTaskPhaseId).getInfo(context, SELECT_NAME)));
            //创建第一个层级的ESO任务,先创建一个ESO任务，创建页面的属性赋值到ESO任务对象上面，ESO任务的Name等于职能
            //创建任务 入参
            Map temp = new HashMap();
            temp.put("taskName", strJF_Function);  //任务名称 ESO任务的Name等于职能
//            temp.put("personId", personObjectID);  //owner和分派人
            temp.put("description", description);  //说明
            temp.put(DomainConstants.SELECT_ID, objectId);  //挂载的id
            //生成ESO任务组的第一层级：根据R2-TKO判断，如果为是，需要是两组。如果是否，一组
            MapList newTasksMapList = new MapList();
            Map<String, String> firstESOTaskMap = createFirstESOTask(context, temp, objectId, strJF_R2_TKO, strJF_ESOType, attributeList, newTasksMapList);
            JF_LOGGER.info("firstESOTaskMap:{}", firstESOTaskMap);
            //参加过滤的任务  进行创建 并挂在新创建的ESO任务下 根据R2-TKO
            //update by ljr
            MapList palList = getObjectListFromPAL(context,tempId);
            Map<String, StringList> filterESOTaskMap = filterESOTask(context, tempId, esoTaskPhaseId, strJF_Function, strJF_Grade, strJF_R2_TKO, strJF_ESOType);
            //复制任务
            JF_LOGGER.info("filterESOTaskMap:{}", filterESOTaskMap);
            String firstESOTaskId = DomainConstants.EMPTY_STRING;
            if (!filterESOTaskMap.isEmpty() && !firstESOTaskMap.isEmpty()) {
                for (Map.Entry<String, String> entry : firstESOTaskMap.entrySet()) {
                    String key = entry.getKey();
                    firstESOTaskId = entry.getValue();
                    esoList.add(firstESOTaskId);
                    // 处理每个键值对的逻辑
                    StringList esoTaskList = filterESOTaskMap.get(key);
                    JF_LOGGER.info("key:{}", key);
                    JF_LOGGER.info("firstESOTaskId:{}", firstESOTaskId);
                    JF_LOGGER.info("esoTaskList:{}", esoTaskList);
                    if (null == esoTaskList || esoTaskList.isEmpty()) {
                        continue;
                    }
                    createFilterTask(context, firstESOTaskId, esoTaskList, palList, esoList);
                    //复制任filterESOTask务的项目角色、说明、name、关联的收藏
//                    duplicateTaskRoleAndLink(context, firstESOTaskId, esoTaskList);
                }
                if (firstESOTaskMap.containsKey("TKO")) {
                    returnMap.put(DomainConstants.SELECT_ID, firstESOTaskMap.get("TKO"));
                } else {
                    returnMap.put(DomainConstants.SELECT_ID, firstESOTaskMap.get("No"));
                }
            } else {
                returnMap.put(DomainConstants.SELECT_ID, objectId);
            }
            CacheUtil.setCacheObject(context, "newTasksMapList", newTasksMapList);
            ContextUtil.commitTransaction(context);
            JF_LOGGER.info("@@@@@@@@@@@@@@@@@@@@@@@@@");
            JF_LOGGER.info("firstESOTaskId:{}", firstESOTaskId);
            JF_LOGGER.info("newTasksMapList:{}", newTasksMapList);
        }catch (Exception e) {
            e.printStackTrace();
            esoList = new StringList();
            ContextUtil.abortTransaction(context);
            throw e;
        }
        //修改创建出来任务的时间
        try {
            JF_LOGGER.info("@@@@@@@@@@@@@@@@@@@@@@@@@");
            JF_LOGGER.info("修改时间");
            if (UIUtil.isNotNullAndNotEmpty(taskESOId)) {
                JF_LOGGER.info("esoList：{}", esoList);
                esoList.add(taskESOId);
                if (!esoList.isEmpty()) {
                    for (int i = 0; i < esoList.size(); i++) {
                        String id = esoList.get(i);
                        JF_LOGGER.info("id：{}", id);
                        /*
                        *   Task Actual Start Date 7/17/2025 8:00:00 AM
                            Task Estimated Duration 1.0
                            Task Estimated Finish Date 3/6/2024 5:00:00 PM
                            Task Estimated Start Date 3/6/2024 8:00:00 AM
                            Task Constraint Date 6/26/2025 5:00:00 PM
                        * */
                        String mql = "mod bus '"+id+"' 'Task Estimated Start Date' '"+startDate+"' 'Task Estimated Finish Date' '"+finishDate+"' 'Task Estimated Duration' '"+duration+"' 'Task Constraint Date' '"+constraintDate+"' 'Task Constraint Type' '"+ taskConstraintType +"';";
                        JF_LOGGER.info("mql:{}",mql);
                        MqlUtil.mqlCommand(context,false,mql,true);
                    }
                }
            }
        }catch (Exception e) {
            e.printStackTrace();
        }finally {
        }
        return returnMap;
    }

    /**
     * 批量创建过滤的eso任务
     * @param context
     * @param firstESOTaskId
     * @param esoTaskList
     * @author LIUJR
     * @throws
     * @return void
     * @date 11/07/2025 14:56
     * @description
     */
    public static void createFilterTask(Context context, String firstESOTaskId, StringList esoTaskList, MapList palList, StringList esoList) throws Exception{
        //因为许可问题 先改成创建
        try {
            DomainObject domainObject = DomainObject.newInstance(context);
            DomainObject newDomainObject = DomainObject.newInstance(context);
            com.matrixone.apps.program.Task task = new Task();
            String taskOrder = getTaskOrder(context, palList, esoTaskList);
            String[] split = taskOrder.split("\\|");
            for (int i = 0; i < split.length; i++) {
                String taskId = split[i];
                Map createTemp = new HashMap();
                domainObject.setId(taskId);
                createTemp.put("taskName", domainObject.getInfo(context, SELECT_NAME));  //任务名称 ESO任务的Name等于职能
                createTemp.put("description", domainObject.getDescription(context));  //说明
                createTemp.put(DomainConstants.SELECT_ID, firstESOTaskId);  //挂载的id
                AttributeList attributeValues = domainObject.getAttributeValues(context);
                String newTaskId = JF_ProjectTaskUtils_mxJPO.createTask(context,firstESOTaskId,createTemp, JF_PLMConstants_mxJPO.TYPE_JF_ESOTask,"addTaskAbove");
                newDomainObject.setId(newTaskId);
                esoList.add(newTaskId);
                attributeValues.add(new Attribute(JF_PLMConstants_mxJPO.ATTR_ProjectRole, "Chair manager"));
                attributeValues = removeColorAndCount(context, attributeValues);
                newDomainObject.setAttributeValues(context, attributeValues);
                task.updateTaskType(context, newTaskId, DomainConstants.TYPE_TASK);
                StringList urlList = domainObject.getInfoList(context, "from[Link URL].to.id");
                JF_LOGGER.info("urlList:{}", urlList);
                //修改类型  将ESO类型 改成Task类型
                if (urlList.size() > 0) {
                    DomainRelationship.connect(context, newDomainObject, new RelationshipType("Task Deliverable"), true, urlList.toStringArray());
                }
            }
        } catch (FrameworkException e) {
            throw  e;
        }

    }

    /**
     * 复制任务的项目角色、说明、name、关联的收藏
     * @param context
     * @param firstESOTaskId
     * @param esoTaskList
     * @author LIUJR
     * @throws
     * @return void
     * @date 24/06/2025 15:24
     * @description
     */
    public static void duplicateTaskRoleAndLink(Context context, String firstESOTaskId, StringList esoTaskList) throws Exception{
        try {
            DomainObject domainObject = DomainObject.newInstance(context);
            domainObject.setId(firstESOTaskId);
            //获取关联的task
            StringList taskList = domainObject.getInfoList(context, "from[Subtask].to.id");
            JF_LOGGER.info("taskList:{}", taskList);
            StringList stringList = JF_Util_mxJPO.basicBolistSel();
            MapList mapList = DomainObject.getInfo(context, esoTaskList.toStringArray(), stringList);
            JF_LOGGER.info("mapList:{}", mapList);
            HashMap<String, String> hashMap = new HashMap<>();
            for (int i = 0; i < mapList.size(); i++) {
                Map map = (Map) mapList.get(i);
                String name = UIUtil.getValue(map, DomainConstants.SELECT_NAME);
                String id = UIUtil.getValue(map, DomainConstants.SELECT_ID);
                hashMap.put(name, id);
            }
            JF_LOGGER.info("hashMap:{}", hashMap);
            DomainObject domainObject1 = DomainObject.newInstance(context);
            com.matrixone.apps.program.Task task = new Task();
            for (int i = 0; i < taskList.size(); i++) {
                String taskId = taskList.get(i);
                domainObject.setId(taskId);
                String name = domainObject.getInfo(context, DomainConstants.SELECT_NAME);
                task.updateTaskType(context, taskId, DomainConstants.TYPE_TASK);
                JF_LOGGER.info("name:{}", name);
                if (hashMap.containsKey(name)) {
                    String id = hashMap.get(name);
                    domainObject1.setId(id);
                    StringList urlList = domainObject1.getInfoList(context, "from[Link URL].to.id");
                    JF_LOGGER.info("urlList:{}", urlList);
                    String attributeValue = domainObject1.getAttributeValue(context, DomainConstants.ATTRIBUTE_PROJECT_ROLE);
                    domainObject.setAttributeValue(context, DomainConstants.ATTRIBUTE_PROJECT_ROLE, attributeValue);
                    //修改类型  将ESO类型 改成Task类型
                    if (urlList.size() > 0) {
                        DomainRelationship.connect(context, domainObject, new RelationshipType("Task Deliverable"), true, urlList.toStringArray());
                    }
                }
            }
        }catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * 生成ESO任务组的第一层级
     * @param context
     * @param temp
     * @param objectId
     * @param strJFR2Tko
     * @param strJFEsoType
     * @param attributeList
     * @author LIUJR
     * @throws
     * @return java.util.Map
     * @date 24/06/2025 10:37
     * @description
     */
    public static Map<String, String> createFirstESOTask(Context context, Map temp, String objectId, String strJFR2Tko, String strJFEsoType, AttributeList attributeList, MapList newTasksMapList) throws Exception{
        Map<String, String> returnMap = new HashMap<>();
        try {
            JF_LOGGER.info("createFirstESOTask.......................");
            //R2-TKO为否
            DomainObject taskObject = DomainObject.newInstance(context);
            HashMap<String, String> hashMap = new HashMap<>();
            if ("Yes".equalsIgnoreCase(strJFR2Tko)) {
                //生成R2-TKO为是的ESO任务
                DomainObject taskObjectYes = DomainObject.newInstance(context);
                String tkoTaskId = JF_ProjectTaskUtils_mxJPO.createTask(context,objectId,temp, JF_PLMConstants_mxJPO.TYPE_JF_ESOTask,"addTaskAbove");
                attributeList.add(new Attribute(JF_PLMConstants_mxJPO.ATTR_JF_R2_TKO, strJFR2Tko));
                attributeList.add(new Attribute(JF_PLMConstants_mxJPO.ATTR_JF_ESOType, "TKO"));
                attributeList.add(new Attribute(JF_PLMConstants_mxJPO.ATTR_JF_BackESOType, "TKO"));
                attributeList.add(new Attribute(JF_PLMConstants_mxJPO.ATTR_ProjectRole, "Chair manager"));
                taskObjectYes.setId(tkoTaskId);
                JF_LOGGER.info("attributeList:{}", attributeList);
                taskObjectYes.setAttributeValues(context, attributeList);
                JF_LOGGER.info("tkoTaskId:{}", tkoTaskId);
                returnMap.put("TKO", tkoTaskId);
                updateTaskOrganization(context, context.getUser(), tkoTaskId);
                hashMap.put(SELECT_ID, tkoTaskId);
                hashMap.put("to[Subtask].from.id", objectId);
                hashMap.put("to[Subtask].id", taskObjectYes.getInfo(context, "to[Subtask].id"));
                newTasksMapList.add(hashMap);

                String phaseTKOTaskId = JF_ProjectTaskUtils_mxJPO.createTask(context,objectId,temp, JF_PLMConstants_mxJPO.TYPE_JF_ESOTask,"addTaskAbove");
                taskObject.setId(phaseTKOTaskId);
                JF_LOGGER.info("strJFR2Tko:{}", strJFR2Tko);
                JF_LOGGER.info("strJFEsoType:{}", strJFEsoType);
                JF_LOGGER.info("phaseTKOTaskId:{}", phaseTKOTaskId);
                AttributeList attributeListNo = attributeList;
                attributeListNo.add(new Attribute(JF_PLMConstants_mxJPO.ATTR_JF_R2_TKO, strJFR2Tko));
                attributeListNo.add(new Attribute(JF_PLMConstants_mxJPO.ATTR_JF_ESOType, "Phase"));
                attributeListNo.add(new Attribute(JF_PLMConstants_mxJPO.ATTR_JF_BackESOType, "Phase"));
                attributeListNo.add(new Attribute(JF_PLMConstants_mxJPO.ATTR_ProjectRole, "Chair manager"));
                JF_LOGGER.info("attributeListNo:{}", attributeListNo);
                taskObject.setAttributeValues(context, attributeListNo);
                returnMap.put("Phase", phaseTKOTaskId);
                updateTaskOrganization(context, context.getUser(), phaseTKOTaskId);
                HashMap<String, String> hashMap1 = new HashMap<>();
                hashMap1.put(SELECT_ID, phaseTKOTaskId);
                hashMap1.put("to[Subtask].from.id", objectId);
                hashMap1.put("to[Subtask].id", taskObject.getInfo(context, "to[Subtask].id"));
                newTasksMapList.add(hashMap1);
            } else {
                String noTKOTaskId = JF_ProjectTaskUtils_mxJPO.createTask(context,objectId,temp, JF_PLMConstants_mxJPO.TYPE_JF_ESOTask,"addTaskAbove");
                taskObject.setId(noTKOTaskId);
                JF_LOGGER.info("strJFR2Tko:{}", strJFR2Tko);
                JF_LOGGER.info("strJFEsoType:{}", strJFEsoType);
                JF_LOGGER.info("noTKOTaskId:{}", noTKOTaskId);
                AttributeList attributeListNo = attributeList;
                attributeListNo.add(new Attribute(JF_PLMConstants_mxJPO.ATTR_JF_R2_TKO, strJFR2Tko));
                attributeListNo.add(new Attribute(JF_PLMConstants_mxJPO.ATTR_JF_ESOType, strJFEsoType));
                attributeListNo.add(new Attribute(JF_PLMConstants_mxJPO.ATTR_JF_BackESOType, strJFEsoType));
                attributeListNo.add(new Attribute(JF_PLMConstants_mxJPO.ATTR_ProjectRole, "Chair manager"));
                JF_LOGGER.info("attributeListNo:{}", attributeListNo);
                taskObject.setAttributeValues(context, attributeListNo);
                returnMap.put("No", noTKOTaskId);
                updateTaskOrganization(context, context.getUser(), noTKOTaskId);
                hashMap.put(SELECT_ID, noTKOTaskId);
                hashMap.put("to[Subtask].from.id", objectId);
                hashMap.put("to[Subtask].id", taskObject.getInfo(context, "to[Subtask].id"));
                newTasksMapList.add(hashMap);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
        JF_LOGGER.info("returnMap:{}", returnMap);
        return returnMap;
    }

    /**
     * 修改任务的协作区
     * @param context
     * @param owner
     * @param taskId
     * @author LIUJR
     * @throws
     * @return void
     * @date 2025/7/22 10:07
     * @description
     */
    public static void updateTaskOrganization(Context context, String owner, String taskId) throws Exception{
        try {
            String personOrganization = JF_Util_mxJPO.getPersonOrganization(context, owner);
            StringBuffer mqlStr = new StringBuffer();
            mqlStr.append("mod bus ").append(taskId).append(" organization '").append(personOrganization);
            JF_LOGGER.info("mqlStr:{}",mqlStr);
            ContextUtil.pushContext(context);
            MqlUtil.mqlCommand(context, true, mqlStr.toString(), false);
        } catch (Exception e) {
            throw e;
        }finally {
            ContextUtil.popContext(context);
        }
    }

    /**
     * 过滤ESO任务 模板匹配的任务
     * 1. 首先根据Phase匹配到ESO模版的Phase，
     *    然后在根据ESO模版里面的ESO任务的进行匹配 职能、3R Grade 是属于多选择，这边过滤的时候，只需要包含就算
     * @param context
     * @param tempId
     * @param esoTaskPhaseId
     * @param strJFFunction
     * @param strJFGrade
     * @param strJFR2Tko
     * @author LIUJR
     * @throws
     * @return void
     * @date 23/06/2025 15:54
     * @description
     */
    private Map<String, StringList> filterESOTask(Context context, String tempId, String esoTaskPhaseId, String strJFFunction, String strJFGrade, String strJFR2Tko, String strJF_ESOType) {
        Map<String, StringList> hashMap = new HashMap();
        try {
            JF_LOGGER.info("filterESOTask.......................");
            DomainObject domainObject = DomainObject.newInstance(context);
            domainObject.setId(tempId);
            StringList tempPhaseSelList = domainObject.getInfoList(context, "from[Subtask].to.id");
            JF_LOGGER.info("tempPhaseSelList:{}", tempPhaseSelList);
            StringList basicBolistSel = JF_Util_mxJPO.basicBolistSel();
            StringList basicRellistSel = JF_Util_mxJPO.basicRellistSel();
            MapList tempPhaseMapList = DomainObject.getInfo(context, tempPhaseSelList.toStringArray(), basicBolistSel);
            String tempPhaseId = DomainConstants.EMPTY_STRING;
            JF_LOGGER.info("esoTaskPhaseId:{}", esoTaskPhaseId);
            DomainObject domainObject1 = DomainObject.newInstance(context, esoTaskPhaseId);
            String strPhase = domainObject1.getInfo(context, DomainConstants.SELECT_NAME);
            JF_LOGGER.info("strPhase:{}", strPhase);
            for (int i = 0; i < tempPhaseMapList.size(); i++) {
                Map map1 = (Map) tempPhaseMapList.get(i);
                String selType = UIUtil.getValue(map1, DomainConstants.SELECT_TYPE);
                String selName = UIUtil.getValue(map1, DomainConstants.SELECT_NAME);
                String selId = UIUtil.getValue(map1, DomainConstants.SELECT_ID);
                if ("Phase".equalsIgnoreCase(selType) && strPhase.equalsIgnoreCase(selName)) {
                    tempPhaseId = selId;
                }
            }
            JF_LOGGER.info("tempPhaseId:{}", tempPhaseId);
            //获取ESO项目中的所有的phase,匹配
            if (UIUtil.isNullOrEmpty(tempPhaseId)) {
                return new HashMap();
            }
            domainObject.setId(tempPhaseId);
            basicBolistSel.add(SELECT_ATTR_JF_Function);
            basicBolistSel.add(SELECT_ATTR_JF_Grade);
            basicBolistSel.add(SELECT_ATTR_JF_R2_TKO);
            basicBolistSel.add(SELECT_ATTR_JF_R3TKO);
            MapList mapList = domainObject.getRelatedObjects(
                    context,
                    DomainRelationship.RELATIONSHIP_SUBTASK,
                    DomainConstants.TYPE_TASK,
                    basicBolistSel,
                    basicRellistSel,
                    false,
                    true,
                    (short) 1,
                    "",
                    "",
                    0
            );
            JF_LOGGER.info("mapList:{}", mapList);
            if("Yes".equalsIgnoreCase(strJFR2Tko)) {
                //加上另外的条件
                StringList tkoIdList = new StringList();
                String flag = strJFGrade +  "-TKO";
                tkoIdList = (StringList) mapList.stream().filter(m -> {
                    Map map = (Map) m;
                    String r2TKO = UIUtil.getValue(map, SELECT_ATTR_JF_R2_TKO);
                    String strFunction = UIUtil.getValue(map, SELECT_ATTR_JF_Function);
                    StringList function = StringList.create(Arrays.asList(strFunction.split(";")));
                    String grade = UIUtil.getValue(map, SELECT_ATTR_JF_Grade);
                    String r3TKO = UIUtil.getValue(map, SELECT_ATTR_JF_R3TKO);
                    JF_LOGGER.info("r2TKO:{}", r2TKO);
                    JF_LOGGER.info("function:{}", function);
                    JF_LOGGER.info("grade:{}", grade);
                    JF_LOGGER.info("r3TKO:{}", r3TKO);
                    JF_LOGGER.info("r2TKO:{}", r2TKO);
                    //update by ljr 20251010 修改校验
                    if (grade.contains(strJFGrade) && function.contains(strJFFunction) && r3TKO.contains(flag)) {
//                    if (grade.contains(strJFGrade) && function.contains(strJFFunction) && r3TKO.contains(flag) && strJFR2Tko.equalsIgnoreCase(r2TKO)  ) {
                        return Boolean.TRUE;
                    } else {
                        return Boolean.FALSE;
                    }
                }).map(m -> {
                    Map map = (Map) m;
                    String id = UIUtil.getValue(map, DomainConstants.SELECT_ID);
                    return id;
                }).collect(Collectors.toCollection(StringList::new));

                StringList phaseIdList = new StringList();
                phaseIdList = (StringList) mapList.stream().filter(m -> {
                    Map map = (Map) m;
                    String r2TKO = UIUtil.getValue(map, SELECT_ATTR_JF_R2_TKO);
                    String strFunction = UIUtil.getValue(map, SELECT_ATTR_JF_Function);
                    StringList function = StringList.create(Arrays.asList(strFunction.split(";")));
                    String grade = UIUtil.getValue(map, SELECT_ATTR_JF_Grade);
                    String r3TKO = UIUtil.getValue(map, SELECT_ATTR_JF_R3TKO);
                    JF_LOGGER.info("r2TKO:{}", r2TKO);
                    JF_LOGGER.info("function:{}", function);
                    JF_LOGGER.info("grade:{}", grade);
                    JF_LOGGER.info("r3TKO:{}", r3TKO);
                    JF_LOGGER.info("r2TKO:{}", r2TKO);
                    if (grade.contains(strJFGrade) && function.contains(strJFFunction) && !r3TKO.contains(flag)) {
                        return Boolean.TRUE;
                    } else {
                        return Boolean.FALSE;
                    }
                }).map(m -> {
                    Map map = (Map) m;
                    String id = UIUtil.getValue(map, DomainConstants.SELECT_ID);
                    return id;
                }).collect(Collectors.toCollection(StringList::new));
                hashMap.put("Phase", phaseIdList);
                hashMap.put("TKO", tkoIdList);
                JF_LOGGER.info("tkoIdList:{}", tkoIdList);
                JF_LOGGER.info("phaseIdList:{}", phaseIdList);
            } else {
                StringList noIdList = (StringList) mapList.stream().filter(m -> {
                    Map map = (Map) m;
                    String strFunction = UIUtil.getValue(map, SELECT_ATTR_JF_Function);
                    StringList function = StringList.create(Arrays.asList(strFunction.split(";")));
                    String grade = UIUtil.getValue(map, SELECT_ATTR_JF_Grade);
//                    String r3TKO = UIUtil.getValue(map, SELECT_ATTR_JF_R3TKO);
//                    if (strJFR2Tko.equalsIgnoreCase(r2TKO) && grade.contains(strJFGrade) && function.contains(strJFFunction)) {
                    if (grade.contains(strJFGrade) && function.contains(strJFFunction)) {
                        return Boolean.TRUE;
                    } else {
                        return Boolean.FALSE;
                    }
                }).map(m -> {
                    Map map = (Map) m;
                    String id = UIUtil.getValue(map, DomainConstants.SELECT_ID);
                    return id;
                }).collect(Collectors.toCollection(StringList::new));
                hashMap.put("No", noIdList);
                JF_LOGGER.info("noIdList:{}", noIdList);
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
        return hashMap;
    }

    /**
     * 过滤ESO任务 模板匹配的任务
     * 1. 首先根据Phase匹配到ESO模版的Phase，
     *    然后在根据ESO模版里面的ESO任务的进行匹配 职能、3R Grade 是属于多选择，这边过滤的时候，只需要包含就算
     * @param context
     * @param tempId
     * @param phaseName
     * @param strEsoId
     * @author LIUJR
     * @throws
     * @return void
     * @date 23/06/2025 15:54
     * @description
     */
    private StringList filterESOTaskAsCopy(Context context, String tempId, String phaseName, String strEsoId, String strGrade) {
        StringList stringList = new StringList();
        try {
            JF_LOGGER.info("filterESOTaskAsCopy.......................");
            DomainObject domainObject = DomainObject.newInstance(context);
            domainObject.setId(tempId);
            StringList tempPhaseSelList = domainObject.getInfoList(context, "from[Subtask].to.id");
            JF_LOGGER.info("tempPhaseSelList:{}", tempPhaseSelList);
            JF_LOGGER.info("strGrade:{}", strGrade);
            StringList basicBolistSel = JF_Util_mxJPO.basicBolistSel();
            StringList basicRellistSel = JF_Util_mxJPO.basicRellistSel();
            MapList tempPhaseMapList = DomainObject.getInfo(context, tempPhaseSelList.toStringArray(), basicBolistSel);
            String tempPhaseId = DomainConstants.EMPTY_STRING;
            JF_LOGGER.info("strPhase:{}", phaseName);
            for (int i = 0; i < tempPhaseMapList.size(); i++) {
                Map map1 = (Map) tempPhaseMapList.get(i);
                String selType = UIUtil.getValue(map1, DomainConstants.SELECT_TYPE);
                String selName = UIUtil.getValue(map1, DomainConstants.SELECT_NAME);
                String selId = UIUtil.getValue(map1, DomainConstants.SELECT_ID);
                if ("Phase".equalsIgnoreCase(selType) && phaseName.equalsIgnoreCase(selName)) {
                    tempPhaseId = selId;
                }
            }
            JF_LOGGER.info("tempPhaseId:{}", tempPhaseId);
            //获取ESO项目中的所有的phase,匹配
            if (UIUtil.isNullOrEmpty(tempPhaseId)) {
                return stringList;
            }
            //获取复制的对象的只能 grade  R2_TKO属性
            domainObject.setId(strEsoId);
            String strJFFunction = domainObject.getAttributeValue(context, ATTR_JF_Function);
//            String strJFGrade = domainObject.getAttributeValue(context, ATTR_JF_Grade);
            String strJFR2Tko = domainObject.getAttributeValue(context, ATTR_JF_R2_TKO);
//            String strESOType = domainObject.getAttributeValue(context, ATTR_JF_ESOType);
            String strBackESOType = domainObject.getAttributeValue(context, ATTR_JF_BackESOType);
            JF_LOGGER.info("strJFFunction:{}", strJFFunction);
            JF_LOGGER.info("strJFR2Tko:{}", strJFR2Tko);
            JF_LOGGER.info("strESOType:{}", strBackESOType);
            domainObject.setId(tempPhaseId);
            basicBolistSel.add(SELECT_ATTR_JF_Function);
            basicBolistSel.add(SELECT_ATTR_JF_Grade);
            basicBolistSel.add(SELECT_ATTR_JF_R2_TKO);
            basicBolistSel.add(SELECT_ATTR_JF_R3TKO);
            MapList mapList = domainObject.getRelatedObjects(
                    context,
                    DomainRelationship.RELATIONSHIP_SUBTASK,
                    DomainConstants.TYPE_TASK,
                    basicBolistSel,
                    basicRellistSel,
                    false,
                    true,
                    (short) 1,
                    "",
                    "",
                    0
            );
            JF_LOGGER.info("mapList:{}", mapList);
            String flag = strGrade + "-TKO";
            JF_LOGGER.info("flag:{}", flag);
            StringList idList = (StringList) mapList.stream().filter(m -> {
                Map map = (Map) m;
                String strFunction = UIUtil.getValue(map, SELECT_ATTR_JF_Function);
                StringList function = StringList.create(Arrays.asList(strFunction.split(";")));
                String grade = UIUtil.getValue(map, SELECT_ATTR_JF_Grade);
                String r2TKO = UIUtil.getValue(map, SELECT_ATTR_JF_R2_TKO);
                String r3TKO = UIUtil.getValue(map, SELECT_ATTR_JF_R3TKO);
                JF_LOGGER.info("function:{}", function);
                JF_LOGGER.info("grade:{}", grade);
                JF_LOGGER.info("r2TKO:{}", r2TKO);
                JF_LOGGER.info("r3TKO:{}", r3TKO);
                if("Yes".equalsIgnoreCase(strJFR2Tko)) {
                    Boolean aBoolean = Boolean.FALSE;
                    if ("TKO".equalsIgnoreCase(strBackESOType)) {
                        if (grade.contains(strGrade) && function.contains(strJFFunction) && r3TKO.contains(flag)) {
                            aBoolean = Boolean.TRUE;
                        }
                    } else if ("Phase".equalsIgnoreCase(strBackESOType)){
                        if (grade.contains(strGrade) && function.contains(strJFFunction) && !r3TKO.contains(flag)) {
//                        if ("No".equalsIgnoreCase(r2TKO) && grade.contains(strJFGrade) && function.contains(strJFFunction) && r3TKO.contains(flag)) {
                            aBoolean = Boolean.TRUE;
                        }
                    }
                    return aBoolean;
                } else {
                    if (grade.contains(strGrade) && function.contains(strJFFunction)) {
                        return Boolean.TRUE;
                    } else {
                        return Boolean.FALSE;
                    }
                }

            }).map(m -> {
                Map map = (Map) m;
                String id = UIUtil.getValue(map, DomainConstants.SELECT_ID);
                return id;
            }).collect(Collectors.toCollection(StringList::new));
            stringList = idList;
        }catch (Exception e) {
            e.printStackTrace();
        }
        JF_LOGGER.info("stringList:{}", stringList);
        return stringList;
    }

    /**
     * @author liuxg
     * @date 2024/3/7 14:38
     * @param context
     * @param PALlist 按WBS排序后的maplist集合
     * @param subMRDidlist 需要获取顺序的子层级对象的id集合
     * @return StringList  返回的名字集合或者id集合
     * @desc 根据传入的mrdcodylist 在PALlist中找到对应的顺序 生成需要复制的id或者名字。
     */
    public static String getTaskOrder(Context context,MapList PALlist,StringList subMRDidlist)throws Exception{

        MapList mrdcodylist=new MapList();

        for(int i=0;i<PALlist.size();i++) {
            Map tempMap=(Map) PALlist.get(i);
            //System.out.println(tempMap.get("WBS")+" "+tempMap.get(DomainConstants.SELECT_NAME));
            String temid= (String) tempMap.get(DomainConstants.SELECT_ID);
            String temname= (String) tempMap.get(SELECT_NAME);
            String temtype= (String) tempMap.get(DomainConstants.SELECT_TYPE);

            //0409 subMRDidlist 第一层任务名字的集合 PALlist中可能会出现父子同名情况，需要规避
            if(subMRDidlist.contains(temid)){
                String targetname= temname;
                //0411
                DomainObject subobj=DomainObject.newInstance(context,temid);
                String parentid=subobj.getInfo(context,"from[Subtask].to.id");
                String parentname=subobj.getInfo(context,"from[Subtask].to.name");
                String parenttype=subobj.getInfo(context,"from[Subtask].to.type");
                if(!targetname.equals(parentname)){
                    mrdcodylist.add(tempMap);
                }
            }
        }
        StringBuffer sb=new StringBuffer();
        for (int i=0;i<mrdcodylist.size();i++){
            Map tempMap=(Map) mrdcodylist.get(i);
            //System.out.println(tempMap.get("WBS")+" "+tempMap.get(DomainConstants.SELECT_NAME));
            if (i != 0) {
                sb.append("|").append(tempMap.get(DomainConstants.SELECT_ID));
            } else {
                sb.append(tempMap.get(DomainConstants.SELECT_ID));
            }

        }
        return sb.toString();
    }


    /**
     * 获取ESO任务模板下的所有第一层级的阶段
     * @param context
     * @param args
     * @author LIUJR
     * @throws
     * @return matrix.util.StringList
     * @date 27/06/2025 09:46
     * @description
     */
    public static StringList getTemplateESOTaskFirstPhases(Context context, String[] args) {
        StringList returnList = new StringList();
        try {
            String key = args[0];
            String tempId = JPO.invoke(context, "JF_PublicMethodClass", null, "getBasicUrl", new String[]{"ESOTask.Template.Id"}, String.class);
            DomainObject domainObject = DomainObject.newInstance(context);
            domainObject.setId(tempId);
            StringList tempPhaseSelList = domainObject.getInfoList(context, "from[Subtask].to.id");
            StringList busSelectList = JF_Util_mxJPO.basicBolistSel();
            busSelectList.add(DomainConstants.SELECT_LEVEL);
            busSelectList.add(DomainConstants.SELECT_ORIGINATED);
            MapList tempPhaseMapList = DomainObject.getInfo(context, tempPhaseSelList.toStringArray(), busSelectList);
            tempPhaseMapList.stream().forEach(m -> {
                Map map1 = (Map) m;
                String selType = UIUtil.getValue(map1, DomainConstants.SELECT_TYPE);
                if ("Phase".equalsIgnoreCase(selType)) {
                    returnList.add(UIUtil.getValue(map1, key));
                }
            });
        }catch (Exception e) {
            e.printStackTrace();
        }
        return returnList;
    }

    /**
     * 获取项目下的第一层级的阶段
     * @param context
     * @param args
     * @author LIUJR
     * @throws
     * @return com.matrixone.apps.domain.util.MapList
     * @date 27/06/2025 09:50
     * @description
     */
    public static MapList getProjectSpaceFirstPhase(Context context, String[] args) throws Exception{
        MapList tempPhaseMapList = null;
        try {
            String projectId = args[0];
            StringList busSelectList = new StringList();
            busSelectList.add(DomainConstants.SELECT_ID);
            busSelectList.add(DomainConstants.SELECT_TYPE);
            busSelectList.add(DomainConstants.SELECT_NAME);
            busSelectList.add(DomainConstants.SELECT_LEVEL);
            busSelectList.add(DomainConstants.SELECT_ORIGINATED);
            DomainObject domainObject = DomainObject.newInstance(context);
            domainObject.setId(projectId);
            StringList tempPhaseSelList = domainObject.getInfoList(context, "from[Subtask].to.id");
            DomainObject task = DomainObject.newInstance(context);
            StringList taskIdList = new StringList();
            for (int i = 0; i < tempPhaseSelList.size(); i++) {
                String id = tempPhaseSelList.get(i);
                task.setId(id);
                String type = task.getInfo(context, DomainConstants.SELECT_TYPE);
                if ("Phase".equalsIgnoreCase(type)) {
                    taskIdList.add(id);
                }
            }
            MapList palList = getObjectListFromPAL(context,projectId);
            String taskOrder = getTaskOrder(context, palList, taskIdList);
            String[] split = taskOrder.split("\\|");
            StringList stringList = StringList.create(split);
            tempPhaseMapList = DomainObject.getInfo(context, stringList.toStringArray(), busSelectList);
//        StringList psAllPhaseNameList = new StringList();
//        StringList psAllPhaseIdList = new StringList();
//        MapList tempPhaseList = (MapList) tempPhaseMapList.stream().filter(m -> {
//            Map map1 = (Map) m;
//            String selType = UIUtil.getValue(map1, DomainConstants.SELECT_TYPE);
//            String selName = UIUtil.getValue(map1, DomainConstants.SELECT_NAME);
//            String selId = UIUtil.getValue(map1, SELECT_ID);
//            if ("Phase".equalsIgnoreCase(selType)) {
//                psAllPhaseNameList.add(selName);
//                psAllPhaseIdList.add(selId);
//                return Boolean.TRUE;
//            } else {
//                return Boolean.FALSE;
//            }
//        }).collect(Collectors.toCollection(MapList::new));
//        tempPhaseList.addSortKey(DomainConstants.SELECT_ORIGINATED, ProgramCentralConstants.ASCENDING_SORT, ProgramCentralConstants.SORTTYPE_STRING);
//        tempPhaseList.sort();
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
        return tempPhaseMapList;
    }

    public void chiefEngineerConfirmProcess(Context context,String[] args) throws Exception{
        Map mapParma = JPO.unpackArgs(args);
        try {
            ContextUtil.startTransaction(context, true);
            String personId = UIUtil.getValue(mapParma, "personId");
            String objectId = UIUtil.getValue(mapParma, "objectId");
            JF_LOGGER.info("chiefEngineerConfirmProcess----personId----objectId:{},{}",personId,objectId);
            DomainObject esoReviewObj = DomainObject.newInstance(context, objectId);
            String esoReviewCurrent = esoReviewObj.getInfo(context, "current");
            if ("Create".equals(esoReviewCurrent)){
                esoReviewObj.promote(context);
            }
            esoReviewObj.addToObject(context,new RelationshipType("JFESOReview2Person"),personId);
            //添加流程
            JF_Route_mxJPO jf_route = new JF_Route_mxJPO(context, args);
            //组装审批人员
            MapList approveList = new MapList();
            String tileMess = "总工确认";
            Map nReceiverMapOne = (Map) JF_PublicMethodClass_mxJPO.getMapAnyOrAll(personId, tileMess, "true", "1", "All");//设置审批信息 标题
            approveList.add(nReceiverMapOne);
            String state = "state_Review";//在哪个状态增加流程
            String policy = "policy_JFESOReview";//哪个Policy上面
            String routeDescription = tileMess;//流程描述
            String routeId = jf_route.createAndStartRoute(context, approveList, objectId, state, policy, routeDescription);
            JF_LOGGER.info("chiefEngineerConfirmProcess----routeId:{}",routeId);
            //修改日期：2026/7/6 修改人：LIUJR 修改内容：签发表提交总工审核时不再重新生成签发表，保留原异步生成逻辑用于追溯。
//            String esoTaskId = esoReviewObj.getInfo(context, "to[JFESOTask2ESOReview].from.id");
//            String projectId = JF_PublicProjectQuery_mxJPO.getProjectIdByTaskId(context, esoTaskId);
//            Job job = new Job("JF_ESO","createESOSignTableDocument",new String[]{objectId,esoTaskId,projectId,personId});
//            job.setTitle("生成ESO签发表");
//            job.createAndSubmit(context);
            ContextUtil.commitTransaction(context);
            //20260728 update by ljr Route事务提交成功后再发送总工审核任务邮件，避免流程回滚但邮件已经发出。
            try {
                sendChiefEngineerReviewTaskEmail(context, objectId, routeId);
            } catch (Exception mailException) {
                JF_LOGGER.error("chiefEngineerConfirmProcess----sendChiefEngineerReviewTaskEmail error, esoReviewId:{}, routeId:{}", objectId, routeId, mailException);
            }
        }catch (Exception e){
            JF_LOGGER.info("chiefEngineerConfirmProcess----error:",e);
            ContextUtil.abortTransaction(context);
        }
    }

    /**
     * 签发表记录发起总工审核后，向本次流程任务的实际审批人发送自定义邮件。
     * @param context 上下文
     * @param esoReviewId 签发表记录ID
     * @param routeId 本次创建的流程ID
     * @return void
     * @throws Exception
     * @author LIUJR
     * @date 2026/7/28
     */
    private void sendChiefEngineerReviewTaskEmail(Context context, String esoReviewId, String routeId) throws Exception {
        // 从本次新建的Route反查审批任务，确保邮件内容和收件人对应本次审批流程。
        DomainObject routeObject = DomainObject.newInstance(context, routeId);
        StringList inboxTaskIdList = routeObject.getInfoList(context, "to[Route Task].from.id");
        // 以本次Route生成的Inbox Task为准，未成功生成审批任务时不发送误导邮件。
        if (CollectionUtils.isEmpty(inboxTaskIdList)) {
            JF_LOGGER.error("sendChiefEngineerReviewTaskEmail----Inbox Task not found, esoReviewId:{}, routeId:{}", esoReviewId, routeId);
            return;
        }

        String inboxTaskId = inboxTaskIdList.get(0);
        DomainObject inboxTaskObject = DomainObject.newInstance(context, inboxTaskId);
        // 一次查询任务标题和实际审批人；流程存在委托时，Project Task关系指向最终任务接收人。
        StringList inboxTaskSelectList = StringList.create(
                SELECT_NAME,
                SELECT_ATTRIBUTE_TITLE,
                "from[Project Task].to.id",
                "from[Project Task].to.name");
        Map inboxTaskInfo = inboxTaskObject.getInfo(context, inboxTaskSelectList);
        String approverId = UIUtil.getValue(inboxTaskInfo, "from[Project Task].to.id");
        String approverName = UIUtil.getValue(inboxTaskInfo, "from[Project Task].to.name");
        String inboxTaskTitle = UIUtil.getValue(inboxTaskInfo, SELECT_ATTRIBUTE_TITLE);
        if (UIUtil.isNullOrEmpty(inboxTaskTitle)) {
            inboxTaskTitle = UIUtil.getValue(inboxTaskInfo, SELECT_NAME);
        }
        if (UIUtil.isNullOrEmpty(approverId)) {
            JF_LOGGER.error("sendChiefEngineerReviewTaskEmail----task approver not found, esoReviewId:{}, routeId:{}, inboxTaskId:{}", esoReviewId, routeId, inboxTaskId);
            return;
        }

        // 使用实际审批人的Person邮箱发送，邮箱未维护时记录错误并停止发送。
        DomainObject approverObject = DomainObject.newInstance(context, approverId);
        String approverEmail = approverObject.getAttributeValue(context, DomainObject.ATTRIBUTE_EMAIL_ADDRESS);
        if (UIUtil.isNullOrEmpty(approverEmail)) {
            JF_LOGGER.error("sendChiefEngineerReviewTaskEmail----task approver email is empty, esoReviewId:{}, approver:{}", esoReviewId, approverName);
            return;
        }

        // 一次查询签发表记录的邮件展示字段及其所属ESO任务，避免逐个属性重复访问数据库。
        DomainObject esoReviewObject = DomainObject.newInstance(context, esoReviewId);
        StringList reviewSelectList = StringList.create(
                SELECT_TYPE,
                SELECT_NAME,
                SELECT_ATTRIBUTE_TITLE,
                "attribute[JF_PhaseState]",
                "attribute[JF_ReviewCounte]",
                "attribute[JF_EstimatedSignDate]",
                "attribute[JF_ActualSignDate]",
                "from[JFESOReview2Document].to.attribute[Title]",
                "to[JFESOTask2ESOReview].from.id");
        Map reviewInfo = esoReviewObject.getInfo(context, reviewSelectList);
        if (!"JFESOReview".equals(UIUtil.getValue(reviewInfo, SELECT_TYPE))) {
            JF_LOGGER.error("sendChiefEngineerReviewTaskEmail----object is not JFESOReview, objectId:{}", esoReviewId);
            return;
        }

        String esoTaskId = UIUtil.getValue(reviewInfo, "to[JFESOTask2ESOReview].from.id");
        if (UIUtil.isNullOrEmpty(esoTaskId)) {
            JF_LOGGER.error("sendChiefEngineerReviewTaskEmail----related ESO task not found, esoReviewId:{}", esoReviewId);
            return;
        }

        String reviewTitle = UIUtil.getValue(reviewInfo, SELECT_ATTRIBUTE_TITLE);
        if (UIUtil.isNullOrEmpty(reviewTitle)) {
            reviewTitle = UIUtil.getValue(reviewInfo, SELECT_NAME);
        }
        String reviewPhaseState = UIUtil.getValue(reviewInfo, "attribute[JF_PhaseState]");
        // RN使用属性Range国际化值，R/Y/G继续显示原业务值。
        if ("RN".equalsIgnoreCase(reviewPhaseState)) {
            reviewPhaseState = EnoviaResourceBundle.getRangeI18NString(
                    context,
                    "JF_PhaseState",
                    "RN",
                    context.getSession().getLanguage());
        }
        String reviewCount = UIUtil.getValue(reviewInfo, "attribute[JF_ReviewCounte]");
        String estimatedSignDate = UIUtil.getValue(reviewInfo, "attribute[JF_EstimatedSignDate]");
        String actualSignDate = UIUtil.getValue(reviewInfo, "attribute[JF_ActualSignDate]");
        String reviewSignDocumentTitle = UIUtil.getValue(reviewInfo, "from[JFESOReview2Document].to.attribute[Title]");
        // 邮件日期统一转为业务展示格式；历史值格式不一致时保留原值，避免影响邮件发送。
        if (UIUtil.isNotNullAndNotEmpty(estimatedSignDate)) {
            try {
                estimatedSignDate = LocalDateTime.parse(estimatedSignDate, inputFormatter).format(outputFormatter);
            } catch (Exception ignored) {
            }
        }
        if (UIUtil.isNotNullAndNotEmpty(actualSignDate)) {
            try {
                actualSignDate = LocalDateTime.parse(actualSignDate, inputFormatter).format(outputFormatter);
            } catch (Exception ignored) {
            }
        }

        // 职能、拍数、模块、研发地点、部门和类型均取当前签发表记录关联的ESO任务。
        DomainObject esoTaskObject = DomainObject.newInstance(context, esoTaskId);
        StringList esoTaskSelectList = StringList.create(
                "attribute[JF_Function]",
                "attribute[JF_Rows]",
                "attribute[JF_Modules]",
                "attribute[JF_Locations]",
                "attribute[JF_Department]",
                "attribute[JF_ESOType]");
        Map esoTaskInfo = esoTaskObject.getInfo(context, esoTaskSelectList);

        // 加载已注册的ESO总工审核邮件模板，并按模板元素ID写入任务及签发表业务数据。
        String html = JF_PublicMethodClass_mxJPO.getPageHTMLResourceFile(context, "ESOReviewApprovalEmail", "zh");
        org.jsoup.nodes.Document doc = Jsoup.parse(html);
        doc.getElementById("Approver").appendText(PersonUtil.getFullName(context, approverName));
        doc.getElementById("TaskTitle").appendText(inboxTaskTitle);
        doc.getElementById("ContentTitle").appendText(reviewTitle);
        doc.getElementById("JF_Function").appendText(UIUtil.getValue(esoTaskInfo, "attribute[JF_Function]"));
        doc.getElementById("JF_Rows").appendText(UIUtil.getValue(esoTaskInfo, "attribute[JF_Rows]"));
        doc.getElementById("JF_Modules").appendText(UIUtil.getValue(esoTaskInfo, "attribute[JF_Modules]"));
        doc.getElementById("JF_Locations").appendText(UIUtil.getValue(esoTaskInfo, "attribute[JF_Locations]"));
        doc.getElementById("JF_Department").appendText(UIUtil.getValue(esoTaskInfo, "attribute[JF_Department]"));
        doc.getElementById("JF_ESOType").appendText(UIUtil.getValue(esoTaskInfo, "attribute[JF_ESOType]"));
        doc.getElementById("JF_PhaseState").appendText(reviewPhaseState);
        doc.getElementById("JF_ReviewCounte").appendText(reviewCount);
        doc.getElementById("JF_EstimatedSignDate").appendText(estimatedSignDate);
        doc.getElementById("JF_ActualSignDate").appendText(actualSignDate);
        //20260814 update by LIUJR 总工审核任务通知增加签发表文档标题。
        doc.getElementById("ReviewSignDocument").appendText(reviewSignDocumentTitle);

        // 邮件处理链接指向本次Route生成的Inbox Task，审批人点击后直接进入总工审核任务。
        Properties prop = NioJDUtils.readPageObject(context, "JFJDConfig");
        String linkAddress = prop.getProperty("JF.3dspace.JFUrl").trim() + inboxTaskId;
        Element address = doc.getElementById("Address");
        address.attr("href", linkAddress);
        address.text("点击处理总工审核任务(Click to process the chief engineer approval task)");

        // 将填充完成的HTML模板作为邮件正文发送，并记录发送结果便于问题追踪。
        MimeMultipart multipart = new MimeMultipart();
        BodyPart msgBodyPart = new MimeBodyPart();
        msgBodyPart.setContent(doc.toString(), "text/html;charset=utf-8");
        multipart.addBodyPart(msgBodyPart);
        Boolean sendResult = JF_SendEmailUtils_mxJPO.SendEmail(
                context,
                approverEmail,
                "ESO 总工审核任务通知(ESO Chief Engineer Approval Task Notice)",
                multipart);
        JF_LOGGER.info("sendChiefEngineerReviewTaskEmail----sendResult:{}, esoReviewId:{}, routeId:{}, approver:{}", sendResult, esoReviewId, routeId, approverName);
    }


    public String getRelateESOTaskHtml(Context context,String[] args) throws Exception{
        Map map = (Map) JPO.unpackArgs(args);
        JF_LOGGER.info("getRelateESOTaskHtml----map:{}",map);
        Map requestMap = (Map) map.get("requestMap");
        String objectId = (String) requestMap.get("objectId");
        JF_LOGGER.info("getRelateESOTaskHtml----objectId:{}",objectId);
        DomainObject esoReviewObj = DomainObject.newInstance(context, objectId);
        StringList esoTaskInfoList = esoReviewObj.getInfoList(context, "to[JFESOTask2ESOReview].from.id");
        StringBuilder strBuilder = new StringBuilder();
        if (esoTaskInfoList.size() > 0) {
            for (String strESOId : esoTaskInfoList) {
                DomainObject domainObject = DomainObject.newInstance(context, strESOId);
                String strESOTaskName = domainObject.getInfo(context,"name");
                String revision = domainObject.getInfo(context,"revision");
                strBuilder.append("<a href=\"JavaScript:emxFormLinkClick('../common/emxTree.jsp?DefaultCategory=PMCGateProperties&amp;objectId="+ strESOId +"&relId=null', 'content','','','','"+ strESOTaskName + " " + revision +"','','')\"");
                strBuilder.append(" class='object'>");
                strBuilder.append(strESOTaskName);
                strBuilder.append("</a>");
                if (esoTaskInfoList.size() > 1) {
                    strBuilder.append("<br/>");
                }
            }
        }
        return strBuilder.toString();
    }


    public Vector getRelateRouteInformation(Context context, String[] args) throws Exception {
        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        Map columnMap = (Map) programMap.get("columnMap");
        String columnName = (String) columnMap.get("name");
        Vector retVector = new Vector();
        StringList busSel = new StringList();
        busSel.add("attribute[Actual Completion Date]");
        busSel.add("attribute[Comments]");
        busSel.add("originated");

        StringList objectIdList = JF_MyTask_mxJPO.getObjectIdList(args);

        for (String s : objectIdList) {
            DomainObject esoReviewObj = DomainObject.newInstance(context, s);
            String itTaskId = esoReviewObj.getInfo(context, "from[Object Route].to.to[Route Task].from.id");
            if (UIUtil.isNullOrEmpty(itTaskId)) {
                retVector.add("");
                continue;
            }
            DomainObject itTaskObj = DomainObject.newInstance(context, itTaskId);
            Map info = itTaskObj.getInfo(context, busSel);

            switch (columnName) {
                case "startConfirmDate":
                    String originated = UIUtil.getValue(info, "originated");
                    retVector.add(originated);
                    break;

                case "actualConfirmDate":
                    String actual = UIUtil.getValue(info, "attribute[Actual Completion Date]");
                    retVector.add(actual);
                    break;

                case "comfirmPerson":
                    String person = itTaskObj.getInfo(context, "from[Project Task].to.name");
                    person = PersonUtil.getFullName(context, person.trim());
                    retVector.add(person);
                    break;

                case "confirmCurrent":
                    String comments = UIUtil.getValue(info, "attribute[Comments]");
                    retVector.add(comments);
                    break;

                default:
                    retVector.add(""); // 如果没有匹配的列名，添加空字符串
                    break;
            }
        }

        return retVector;
    }

    /**
     * 导出ESO
     * @param context
     * @param args
     * @return
     * @throws Exception
     */
    public Map exportESOTask(Context context, String[] args) throws Exception {
        Map resMap = new HashMap();
        String esoTaskId = args[0];
        String projectId = args[1];
        InputStream inputStream = null;
        try {
            ContextUtil.pushContext(context);
            String  classPath = JF_PublicMethodClass_mxJPO.getBasicUrl(context, new String[]{"Space.WEBINFO.Path"});
            JF_LOGGER.info("classPath:{}", classPath);
            int webInfIndex = classPath.indexOf("WEB-INF");
            if (webInfIndex == -1) {
                throw new IllegalStateException("WEB-INF not found in classPath.");
            }
            String fileTemPath = classPath.substring(0, webInfIndex);
            String filePath = JF_ECRService_mxJPO.getTemplatePath("ESOSignTableTemplate.xlsx", fileTemPath);
            //打开文件
            inputStream = new FileInputStream(filePath);
            Workbook workbook = WorkbookFactory.create(inputStream);
            String fileName = "";
            DomainObject projectObj = DomainObject.newInstance(context, projectId);
            String title = projectObj.getDescription(context);
            fileName = "工程签发ESO-"+title;
            JF_LOGGER.info("fileName:{}", fileName);
            DomainObject esoTaskObj = DomainObject.newInstance(context, esoTaskId);
            String esoLastReviewId = "";
            //20260819 update by ljr 顶层ESO/CS与子节点统一使用最新有效签发表记录，供CS总体评价和导出信息取色。
            Map esoLastReviewMap = JF_PublicMethodClass_mxJPO.getLatestESOReview(context, esoTaskId);
            if (!esoLastReviewMap.isEmpty()) {
                esoLastReviewId = UIUtil.getValue(esoLastReviewMap, SELECT_ID);
                JF_LOGGER.info("esoLastReviewId-----:{}",esoLastReviewId);
            }
            writeInformationToExcel(context,workbook,new String[]{esoLastReviewId,esoTaskId,projectId}, "export");
            JF_LOGGER.info("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~··");
            Map phaseInfo = getFirstPhaseByTask(context, esoTaskObj);
            String phaseName = UIUtil.getValue(phaseInfo, "name");
            String JF_Modules = esoTaskObj.getInfo(context, "attribute[JF_Modules]");
            String JF_Function = esoTaskObj.getInfo(context, "attribute[JF_Function]");
            String signDate = "";
            String colorFlag = "";
            if (UIUtil.isNotNullAndNotEmpty(esoLastReviewId)){
                DomainObject esoLastReviewObject = DomainObject.newInstance(context, esoLastReviewId);
                StringList esoReviewBusSel = StringList.create("attribute[JF_ActualSignDate]","attribute[JF_PhaseState]");
                Map info = esoLastReviewObject.getInfo(context, esoReviewBusSel);
                signDate = UIUtil.getValue(info,"attribute[JF_ActualSignDate]");
                colorFlag = UIUtil.getValue(info,"attribute[JF_PhaseState]");
                if (UIUtil.isNotNullAndNotEmpty(signDate)){
                    // 解析输入日期
                    LocalDateTime dateTime = LocalDateTime.parse(signDate, inputFormatter);
                    // 格式化输出日期
                    signDate = dateTime.format(outputFormatter);
                }
            }
            //获取当前时间
            LocalDateTime now = LocalDateTime.now();
            // 将 LocalDateTime 对象转换成指定格式的字符串
            String strFormattedDate = now.format(formatter);
            resMap.put("file", workbook);
            resMap.put("flag", "Y");
//            resMap.put("fileName", JF_PublicMethodClass_mxJPO.buildStringInStrings(fileName, "-",JF_Modules,"-",phaseName,"-",signDate,"-",colorFlag,"-",strFormattedDate, ".xlsx"));
            //update by ljr 20250903 导出ESO表格，名称不加最后的导出日期
            resMap.put("fileName", JF_PublicMethodClass_mxJPO.buildStringInStrings(fileName, "-",JF_Modules,"-",phaseName,"-",signDate,"-",colorFlag, ".xlsx"));
        } catch (IllegalArgumentException  e) {
            resMap.put("flag", "N");
            resMap.put("Mess", e.getMessage());
            return resMap;
        } catch (Exception e){
            resMap.put("flag", "N");
            resMap.put("Mess", e.getMessage());
            return resMap;
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
            ContextUtil.popContext(context);
        }
        JF_LOGGER.info("resMap：{}", resMap);
        return resMap;
    }


    /**
     * 创建ESO签发表
     * @param context
     * @param args
     * @throws Exception
     */
    public void createESOSignTableDocument(Context context, String[] args) throws Exception {
        String esoReviewId = args[0];
        String esoTaskId = args[1];
        String projectId = args[2];
        String personId = args[3];
        try {
            String  classPath = JF_PublicMethodClass_mxJPO.getBasicUrl(context, new String[]{"Space.WEBINFO.Path"});
            JF_LOGGER.info("classPath:{}", classPath);
            int webInfIndex = classPath.indexOf("WEB-INF");
            if (webInfIndex == -1) {
                throw new IllegalStateException("WEB-INF not found in classPath.");
            }
            String fileTemPath = classPath.substring(0, webInfIndex);
            String filePath = JF_ECRService_mxJPO.getTemplatePath("ESOSignTableTemplate.xlsx", fileTemPath);
            String workSpace = context.createWorkspace();
            String fileName = "";
            DomainObject projectObj = DomainObject.newInstance(context, projectId);
            String title = projectObj.getDescription(context);
            title = title.replace('/', '_');
            DomainObject esoTaskObj = DomainObject.newInstance(context, esoTaskId);
            String JF_Function = esoTaskObj.getInfo(context, "attribute[JF_Function]");
            String JF_Modules = esoTaskObj.getInfo(context, "attribute[JF_Modules]");
            Map phaseInfo = getFirstPhaseByTask(context, esoTaskObj);
            String phaseName = UIUtil.getValue(phaseInfo, "name");
//            fileName = "工程签发ESO-"+title + "-" + JF_Function + "-" + phaseName;
            fileName = "工程签发ESO-"+title + "-" + JF_Modules + "-" + phaseName;
            JF_LOGGER.info("fileName:{}", fileName);
            // 打开文件
            try (InputStream inputStream = new FileInputStream(filePath);
                 Workbook workbook = WorkbookFactory.create(inputStream);
                 FileOutputStream fileOut = new FileOutputStream(workSpace + "/" +fileName+".xlsx")) {

                writeInformationToExcel(context,workbook,args, "create");
                // 写入到文件
                workbook.write(fileOut);
            } catch (IOException e) {
                JF_LOGGER.info("Error writing to Excel file:", e);
            }
            String sObjGeneratorName = UICache.getObjectGenerator(context, "type_Document", "");
            String sName = DomainObject.getAutoGeneratedName(context, sObjGeneratorName, "");
            String docPolicy = PropertyUtil.getSchemaProperty(EnoviaResourceBundle.getProperty(context, "emxFrameowrk.FileUpload.Default.Policy"));
            if (UIUtil.isNotNullAndNotEmpty(docPolicy)) {
                CommonDocument cDoc = new CommonDocument();
                Policy policy = new Policy(docPolicy);
                String revision = policy.getFirstInSequence(context);
                PropertyUtil.setRPEValue(context, "MX_ALLOW_POV_STAMPING", "true", false);
                cDoc.createObject(context, DomainObject.TYPE_DOCUMENT, sName, revision, docPolicy, context.getVault().getName());
                cDoc.setAttributeValue(context, "Title", fileName);
                DomainObject esoReviewObject = DomainObject.newInstance(context, esoReviewId);
                //必须先关联签发表，后checkin,转换pdf程序才能正常转，pdf逻辑为to[JFESOReview2Document]==true&&fileName.endwith(".xlsx");
                String documentId = cDoc.getId(context);
                esoReviewObject.addToObject(context,new RelationshipType("JFESOReview2Document"),documentId);
                String storeFromBL = null;
                storeFromBL = DocumentUtil.getStoreFromBL(context, "Document");
                //这个创建版本需要在checkin之前 才能自动转换pdf
                cDoc.createVersion(context, fileName+".xlsx", fileName+".xlsx", null);
                cDoc.checkinFile(context, true, true, "", "generic", storeFromBL, fileName+".xlsx", workSpace);
                JF_LOGGER.info("documentId:{}", documentId);
            }
        } catch (Exception e) {
            JF_LOGGER.info("createESOSignTableDocument-----error:", e);
        }
    }

    public void writeInformationToExcel(Context context, Workbook workbook, String[] args, String flag) throws Exception{
        try {
            String esoReviewId = args[0];
            String esoTaskId = args[1];
            String projectId = args[2];
//        String personId = args[3];
            JF_LOGGER.info("writeInformationToExcel-----esoReviewId:{},esoTaskId:{},projectId:{}", esoReviewId,esoTaskId,projectId);
            StringList esoTaskBusSel = StringList.create("attribute[JF_Function]","attribute[JF_Rows]", "attribute[JF_ESOType]", "attribute[JF_Grade]","attribute[JF_Modules]","attribute[JF_ColorIdentification]");
            DomainObject esoTaskObj = DomainObject.newInstance(context, esoTaskId);
            String JF_Function = esoTaskObj.getInfo(context, "attribute[JF_Function]");
            String JF_Modules = esoTaskObj.getInfo(context, "attribute[JF_Modules]");
            String subEsoRows = esoTaskObj.getInfo(context, "attribute[JF_Rows]");
            String subEsoESOType = esoTaskObj.getInfo(context, "attribute[JF_ESOType]");
            String JF_ColorIdentification = EMPTY_STRING;
            if (UIUtil.isNotNullAndNotEmpty(esoReviewId)) {
                DomainObject esoReviewObj = DomainObject.newInstance(context, esoReviewId);
                JF_ColorIdentification = esoReviewObj.getAttributeValue(context, "JF_PhaseState");
            }
            String sheetFunctionAndModules = JF_Modules + "_" + subEsoRows + "_" + subEsoESOType;
            StringList sheetNameList = new StringList();
            if (sheetFunctionAndModules.length() > 31) {
                sheetNameList.add(sheetFunctionAndModules);
            }
            MapList subTask = esoTaskObj.getRelatedObjects(context,"Subtask","JF_ESOTask,Task",StringList.create("name","id","type"),new StringList(),false,true,(short)1,"","",0);
            //类型分组
            Map groupTask = (Map) subTask.stream().collect(Collectors.groupingBy(m -> {
                Map map = (Map) m;
                return UIUtil.getValue(map, "type");
            }));
            JF_LOGGER.info("writeInformationToExcel-----groupTask:{}", groupTask);
            List subEsoTaskList = (List) groupTask.get("JF_ESOTask");
            Map<String, Integer> sheetNameCountMap = new HashMap<>();
            //把选择的eso任务放进去避免最后写前两个sheet的时候sheet名字重复
            sheetNameCountMap.put(sheetFunctionAndModules,1);
            //key是eso的id  value是sheet的名称
            Map<String,String> mappingMap = new HashMap<>();
            leftBorderStyle  = getLeftBorderStyle(workbook);
            borderStyle  = getBorderStyle(workbook);
            Sheet sourceSheet = workbook.getSheetAt(2);
            Sheet source1Sheet = workbook.getSheetAt(1);
            PrintSetup sourcePrintSetup = sourceSheet.getPrintSetup();
            sourceSheet.setFitToPage(true);
            source1Sheet.setFitToPage(true);
            // ================= 1. 定义单位转换常量 =================
            // POI 的 margin 单位是英寸 (inch)，1 cm ≈ 0.3937 inch
            double CM_TO_INCH = 0.393700787;
            if (CollectionUtils.isNotEmpty(subEsoTaskList)) {
                for (int i = 0; i < subEsoTaskList.size(); i++) {
                    Map map = (Map) subEsoTaskList.get(i);
                    String subEsoTaskId = UIUtil.getValue(map, "id");
                    DomainObject subEsoTaskObj = DomainObject.newInstance(context, subEsoTaskId);
                    Map subEsoTaskInfo = subEsoTaskObj.getInfo(context, esoTaskBusSel);
                    String subEsoFunction = UIUtil.getValue(subEsoTaskInfo, "attribute[JF_Function]");
                    String subEsoModules = UIUtil.getValue(subEsoTaskInfo, "attribute[JF_Modules]");
                    subEsoRows = UIUtil.getValue(subEsoTaskInfo, "attribute[JF_Rows]");
                    subEsoESOType = UIUtil.getValue(subEsoTaskInfo, "attribute[JF_ESOType]");
                    // 组合名称
                    String sheetBaseName = subEsoModules + "_" + subEsoRows + "_" + subEsoESOType;
                    JF_LOGGER.info("sheetBaseName:{}", sheetBaseName);
                    //因为模板里面已经有了一个子的sheet页了 就不需要去创建了，直接改名
                    if (i == 0) {
                        workbook.setSheetName(2, sheetBaseName);
                        sheetNameCountMap.put(sheetBaseName, 1);
                        mappingMap.put(subEsoTaskId, sheetBaseName);
                        continue;
                    }
                    int sheetIndex = sheetNameCountMap.getOrDefault(sheetBaseName, 0);
                    sheetNameCountMap.put(sheetBaseName, sheetIndex + 1);

                    // 创建工作表名称
                    String sheetName;
                    if (sheetIndex == 0) {
                        sheetName = sheetBaseName;
                    } else {
                        sheetName = sheetBaseName + "_" + sheetIndex;
                    }
                    // 复制工作表并创建条件格式
//                    Sheet newSheet = copySheet(workbook, sheetName);
//                    createConditionalFormatting(newSheet);

                    //update bu ljr 20260114 使用clone的方式
                    Sheet newSheet = workbook.cloneSheet(2);
                    int sheetIndex1 = workbook.getSheetIndex(newSheet);
                    workbook.setSheetName(sheetIndex1, sheetName);
                    mappingMap.put(subEsoTaskId, sheetName);
                    // 2. 【关键修复】手动同步页边距 (Margins)
                    // 必须逐个设置，因为 cloneSheet 不会复制这些值
                    newSheet.setMargin(Sheet.LeftMargin, sourceSheet.getMargin(Sheet.LeftMargin));
                    newSheet.setMargin(Sheet.RightMargin, sourceSheet.getMargin(Sheet.RightMargin));
                    newSheet.setMargin(Sheet.TopMargin, sourceSheet.getMargin(Sheet.TopMargin));
                    newSheet.setMargin(Sheet.BottomMargin, sourceSheet.getMargin(Sheet.BottomMargin));
                    newSheet.setMargin(Sheet.FooterMargin, sourceSheet.getMargin(Sheet.FooterMargin));
                    newSheet.setMargin(Sheet.HeaderMargin, sourceSheet.getMargin(Sheet.HeaderMargin));

                    // 设置打印为“将整个工作表打印在一页”
                    PrintSetup printSetup = newSheet.getPrintSetup();
                    // 适用模板使用了“适应页宽/高”  自适应
                    newSheet.setFitToPage(true);
                    printSetup.setFitWidth(sourcePrintSetup.getFitWidth());
                    printSetup.setFitHeight(sourcePrintSetup.getFitHeight());
                    printSetup.setScale(sourcePrintSetup.getScale());
                }
            }
            //判断如果sheetName中的字符超过了31，就提示报错
            if (!sheetNameList.isEmpty()) {
                String mess = ComponentsUIUtil.getI18NString(context, context.getLocale().toString(), "emxComponents.Notice.ESOSheetNameError", new String[]{});
                throw new IllegalArgumentException(mess.replaceAll("sheetName", sheetNameList.join(",")));
            }
            JF_LOGGER.info("mappingMap:{}", mappingMap);
            int numberOfSheets = workbook.getNumberOfSheets();
            JF_LOGGER.info("numberOfSheets:{}", numberOfSheets);
            String sheetName = workbook.getSheetName(2);
            JF_LOGGER.info("sheetName:{}", sheetName);
            //写子sheet页
            if (CollectionUtils.isNotEmpty(subEsoTaskList)) {
                for (int i = 0; i < subEsoTaskList.size(); i++) {
                    Map map = (Map) subEsoTaskList.get(i);
                    String subEsoTaskId = UIUtil.getValue(map, "id");
                    DomainObject subEsoTaskObj = DomainObject.newInstance(context, subEsoTaskId);
                    MapList esoSubTaskList = subEsoTaskObj.getRelatedObjects(context, "Subtask", "Task", StringList.create("name", "id", "type"), new StringList(), false, true, (short) 1, "", "", 0);
                    String esoLastReviewId = "";
                    //20260819 update by ljr ESO子节点统一复用公共方法，取得审核次数最大且已维护颜色的最新签发表记录。
                    Map esoLastReviewMap = JF_PublicMethodClass_mxJPO.getLatestESOReview(context, subEsoTaskId);
                    if (!esoLastReviewMap.isEmpty()) {
                        esoLastReviewId = UIUtil.getValue(esoLastReviewMap, SELECT_ID);
                        JF_LOGGER.info("esoLastReviewId-----:{}",esoLastReviewId);
                    }
                    if (mappingMap.containsKey(subEsoTaskId)){
                        //写入Excel
                        writeToExcelCommon(context, workbook, mappingMap.get(subEsoTaskId), new String[]{esoLastReviewId, subEsoTaskId, projectId}, esoSubTaskList, flag);
                    }
                }
            }
            //如果cs下面没有ESO任务 则删掉模板里面的Structure的sheet页
            if (JF_Function.equalsIgnoreCase("CS") && CollectionUtils.isEmpty(subEsoTaskList)){
                workbook.removeSheetAt(2);
            }
            List taskList = (List) groupTask.get("Task");
            MapList subTaskLists = new MapList();
            subTaskLists.addAll(taskList);
            //写前面两个sheet页中的一个
            JF_LOGGER.info("JF_Function:{}", JF_Function);
            if (JF_Function.equalsIgnoreCase("CS")){
                writeToExcelCommon(context, workbook, "CS", args, subTaskLists, flag);
                workbook.setSheetName(1,sheetFunctionAndModules);
                //写入总体评价行
                JF_LOGGER.info("写入总体评价行:{}");
                //20260814 update by LIUJR CS总体评价仅取最新有效签发表颜色，没有最新有效签发表时显示未审核。
                if (UIUtil.isNullOrEmpty(JF_ColorIdentification) || "RN".equalsIgnoreCase(JF_ColorIdentification)) {
                    JF_ColorIdentification = EnoviaResourceBundle.getRangeI18NString(context, "JF_PhaseState", "RN", context.getSession().getLanguage());
                }
                writeOverallResult(workbook,subTaskLists.size(),JF_ColorIdentification, sheetFunctionAndModules);

            }else {
                writeToExcelCommon(context, workbook, "Structure", args, subTaskLists, flag);
                workbook.setSheetName(2,sheetFunctionAndModules);
            }
            if (!JF_Function.equalsIgnoreCase("CS") && CollectionUtils.isEmpty(subEsoTaskList)){
                workbook.removeSheetAt(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * 将当前ESO签发表记录及任务信息写入签发表工作表
     * @param context
     * @param workbook 签发表工作簿
     * @param sheetName 工作表名称
     * @param args 当前签发表记录ID、ESO任务ID和项目ID
     * @param subTaskList ESO子任务
     * @param flag 生成场景
     * @return void
     * @throws Exception
     * @author LIUJR
     * @date 2026/7/28
     */
    public void writeToExcelCommon(Context context ,Workbook workbook,String sheetName, String[] args ,MapList subTaskList, String flag)throws Exception{
        try {
            String esoReviewId = args[0];
            String esoTaskId = args[1];
            String projectId = args[2];
//        String personId = args[3];
            //add by ljr 20250922
            JF_LOGGER.info("esoReviewId:{}", esoReviewId);
            String comment = EMPTY_STRING;
            String approval = EMPTY_STRING;
            DomainObject esoReviewObj = DomainObject.newInstance(context);
            if (UIUtil.isNotNullAndNotEmpty(esoReviewId)) {
                esoReviewObj.setId(esoReviewId);
                StringList comments = esoReviewObj.getInfoList(context, "from[Object Route].to.to[Route Task].from.attribute[Comments]");
                StringList status = esoReviewObj.getInfoList(context, "from[Object Route].to.to[Route Task].from.attribute[Approval Status]");
                comment =  (comments.isEmpty() || null == comments)? "" : comments.get(0);
                approval =  (status.isEmpty() || null == status)? "" : status.get(0);
            }
            //end
            StringList esoTaskBusSel = StringList.create("attribute[JF_Function]","attribute[JF_Rows]", "attribute[JF_ESOType]", "attribute[JF_Grade]","attribute[JF_Modules]","attribute[JF_ColorIdentification]","name");
            DomainObject esoTaskObj = DomainObject.newInstance(context, esoTaskId);
            Integer startCell = 0;  //起始列
            Integer endCell = 9;    //结束列
            Integer startRow = 8;//开始行
            Integer statisticRow = 9;//决议行
            //写入固定位置的值
            Map phaseInfo = getFirstPhaseByTask(context, esoTaskObj);
            String phaseName = UIUtil.getValue(phaseInfo, "name");
            JF_LOGGER.info("sheetName:{}", sheetName);
            Sheet sheet = workbook.getSheet(sheetName);
            DomainObject projectObj = DomainObject.newInstance(context, projectId);
            StringList projectBusSel = StringList.create("attribute[JF_ProjectCustomers]","name","description","owner", JF_PLMConstants_mxJPO.SELECT_ATTR_JF_ProjectGrade);
            String strMql = JF_PublicMethodClass_mxJPO.buildStringInStrings("print bus ",projectId," select from[Member|attribute[Project Role]=='Chair manager'].to.name dump ;");
            ContextUtil.pushContext(context);
            String chairManager =  MqlUtil.mqlCommand(context, true, strMql, false);
            ContextUtil.popContext(context);
            String chairManagerFullName = PersonUtil.getFullName(context, chairManager);
            Map projectInfo = projectObj.getInfo(context, projectBusSel);
            String customers = UIUtil.getValue(projectInfo, "attribute[JF_ProjectCustomers]");
            String projectName = UIUtil.getValue(projectInfo, "name");
            String projectDescription = UIUtil.getValue(projectInfo, "description");
            String projectOwner = UIUtil.getValue(projectInfo, "owner");
            projectOwner = PersonUtil.getFullName(context, projectOwner);
            Map esoTaskInfo = esoTaskObj.getInfo(context, esoTaskBusSel);
            //Grade属性取项目的属性  update  by ljr 20260317
            String projectGrade = UIUtil.getValue(projectInfo, JF_PLMConstants_mxJPO.SELECT_ATTR_JF_ProjectGrade);
//            String esoGrade = UIUtil.getValue(esoTaskInfo, "attribute[JF_Grade]");
            String esoModules = UIUtil.getValue(esoTaskInfo, "attribute[JF_Modules]");
            String esoRows = UIUtil.getValue(esoTaskInfo, "attribute[JF_Rows]");
            String esoESOType = UIUtil.getValue(esoTaskInfo, "attribute[JF_ESOType]");
            String esoTaskName = UIUtil.getValue(esoTaskInfo, "name");
            String nameIssure = esoModules + "-" +  esoRows + "-" + esoESOType;
            //add by LIUJR 20260617 导出签发表时将当前ESO任务备注写入决议栏右侧内容区域
            String esoTaskRemark = esoTaskObj.getInfo(context, "attribute[JF_Remark]");
            //end
            StringList esoReviewBusSel = StringList.create("attribute[JF_ReviewCounte]","attribute[JF_ActualSignDate]","from[JFESOReview2Person].to.name","current","description");
            String esoReviewCount = "";
            String esoReviewActualSignDate = "";
            String chiefEngineerName = "";
            String esoReviewCurrent = "";
            String esoReviewRemark = "";
            esoReviewActualSignDate = getPhaseOutDate(context,phaseName,projectObj);
            if (UIUtil.isNotNullAndNotEmpty(esoReviewActualSignDate)){
                // 解析输入日期
                LocalDateTime dateTime = LocalDateTime.parse(esoReviewActualSignDate, inputFormatter);
                // 格式化输出日期
                esoReviewActualSignDate = dateTime.format(outputFormatter);
            }
            if (UIUtil.isNotNullAndNotEmpty(esoReviewId)){
                esoReviewObj.setId(esoReviewId);
                Map esoReviewInfo = esoReviewObj.getInfo(context, esoReviewBusSel);
                esoReviewCount = UIUtil.getValue(esoReviewInfo, "attribute[JF_ReviewCounte]");
                esoReviewCurrent = UIUtil.getValue(esoReviewInfo, "current");
                esoReviewRemark = UIUtil.getValue(esoReviewInfo, "description");
                if ("Released".equals(esoReviewCurrent)){
                    chiefEngineerName = UIUtil.getValue(esoReviewInfo, "from[JFESOReview2Person].to.name");
                    chiefEngineerName = PersonUtil.getFullName(context, chiefEngineerName);
                }
            }
            Row row = sheet.getRow(1);
            row.getCell(0).setCellValue(phaseName);

            Row row3 = sheet.getRow(3);
            row3.getCell(1).setCellValue(projectDescription);
            row3.getCell(3).setCellValue(esoModules);
            row3.getCell(5).setCellValue(projectName);
            if (!"Reject".equalsIgnoreCase(approval) && UIUtil.isNotNullAndNotEmpty(approval)) {
                row3.getCell(8).setCellValue(chiefEngineerName);
            }
            Row row4 = sheet.getRow(4);
            row4.getCell(1).setCellValue(projectOwner);
//            row4.getCell(3).setCellValue(esoGrade);
            //Grade属性取项目的属性  update  by ljr 20260317
            row4.getCell(3).setCellValue(projectGrade);
            row4.getCell(5).setCellValue(customers);
            if (UIUtil.isNotNullAndNotEmpty(comment)) {
                //add by ljr 20251104  导出的时候如果
                //            if ("Reject".equalsIgnoreCase(approval) && "export".equalsIgnoreCase(flag)) {
                //                row4.getCell(8).setCellValue("");
                //            } else {
                row4.getCell(8).setCellValue(comment);
                //            }
            }

            Row row5 = sheet.getRow(5);
            row5.getCell(1).setCellValue(chairManagerFullName);
            row5.getCell(3).setCellValue(esoReviewCount);
            row5.getCell(5).setCellValue(esoReviewActualSignDate);

            JF_LOGGER.info("sheet.getLastRowNum()----:{},sheetName:{}",sheet.getLastRowNum(),sheetName);
            if (subTaskList.size()>1){
                sheet.shiftRows(statisticRow, sheet.getLastRowNum(), subTaskList.size() - 1, true, false);
            }
            StringList subTaskBusSel = StringList.create("name","description","attribute[JF_ColorIdentification]","attribute[Title]","attribute[JF_Remark]");
            StringList issueBusSel = StringList.create("current","name","description","attribute[Actual End Date]","attribute[Estimated End Date]","attribute[Action Taken]");
            StringList documentBusSel = StringList.create("name","revision","attribute[Title]","type");
            // 创建加粗的字体
            XSSFFont boldFont = (XSSFFont) workbook.createFont();
            boldFont.setBold(true);
            XSSFRichTextString richTextString = new XSSFRichTextString();
            //eso任务名称加粗
//        richTextString.append(esoTaskName+"\n",boldFont);
            richTextString.append(nameIssure,boldFont);
            //20260814 update by LIUJR 决议说明区任务标题不再拼接签发表颜色标识。
            //add by LIUJR 20260617 决议右侧内容区域在问题描述前追加签发表备注和ESO任务备注
            if (UIUtil.isNotNullAndNotEmpty(esoReviewRemark)) {
                richTextString.append("    " + esoReviewRemark, new XSSFFont());
            }
            richTextString.append("\n", new XSSFFont());
            if (UIUtil.isNotNullAndNotEmpty(esoTaskRemark)) {
                richTextString.append(esoTaskRemark + "\n", new XSSFFont());
            }
            richTextString.append("\n", new XSSFFont());
            //end
            //存放数据容器
            MapList mapList = new MapList();
            StringList colorList = new StringList();
            //20260814 update by LIUJR 决议计算时需要优先识别任一子任务颜色为空的情况。
            boolean hasBlankTaskColor = false;
            StringBuilder stringBuilder = new StringBuilder();
            if (CollectionUtils.isNotEmpty(subTaskList)) {
                for (int i = 0; i < subTaskList.size(); i++) {
                    Map subTaskInfo = (Map) subTaskList.get(i);
                    Map<Integer, Object> hashMap = new HashMap<>();
                    String subTaskId = UIUtil.getValue(subTaskInfo, "id");
                    DomainObject subTaskObj = DomainObject.newInstance(context, subTaskId);
                    Map subTaskBusInfo = subTaskObj.getInfo(context, subTaskBusSel);
                    String subTaskName = UIUtil.getValue(subTaskBusInfo, "name");
                    String subTaskDescription = UIUtil.getValue(subTaskBusInfo, "description");
                    String JF_Remark = UIUtil.getValue(subTaskBusInfo, "attribute[JF_Remark]");
                    String subTaskColorIdentification = UIUtil.getValue(subTaskBusInfo, "attribute[JF_ColorIdentification]");
                    if (UIUtil.isNullOrEmpty(subTaskColorIdentification)) {
                        hasBlankTaskColor = true;
                    }
                    subTaskColorIdentification = colorTransform(subTaskColorIdentification);
                    if (!"NA".equalsIgnoreCase(subTaskColorIdentification)) {
                        //update  by ljr 20250923
                        colorList.add(subTaskColorIdentification);
                    }
                    StringList documentListId = subTaskObj.getInfoList(context, "from[Task Deliverable].to.id");
                    StringList documentFullName = new StringList();
                    for (String s : documentListId) {
                        DomainObject documentObj = DomainObject.newInstance(context, s);
                        Map documentMapInfo = documentObj.getInfo(context, documentBusSel);
                        String documentName = UIUtil.getValue(documentMapInfo,"name");
                        String documentRevision = UIUtil.getValue(documentMapInfo,"revision");
                        String documentType = UIUtil.getValue(documentMapInfo,"type");
                        String documentTitle = UIUtil.getValue(documentMapInfo,"attribute[Title]");
                        if ("Document".equals(documentType)){
                            documentFullName.add(documentName+"_"+documentRevision+"："+documentTitle);
                        }
                    }
                    String outputDoc = String.join("\n", documentFullName);
                    StringList taskPersonList = subTaskObj.getInfoList(context, "to[Assigned Tasks].from.name");
                    StringList taskResponsibleList = new StringList();
                    for (String s : taskPersonList) {
                        Vector assignments = PersonUtil.getAssignments(context, s);
                        //ESO审核员排除
                        if (assignments.contains("JfESOAdmin")){
                            continue;
                        }
                        taskResponsibleList.add(PersonUtil.getFullName(context, s));
                    }
                    JF_LOGGER.info("taskResponsibleList:::{}",taskResponsibleList);
                    StringList issueNameAndDescList = new StringList();
                    StringList issueCorrectiveActionList = new StringList();
                    StringList issueResponsibleList = new StringList();
                    StringList issueDueDateList = new StringList();
                    MapList taskRelateIssues = getTaskRelateIssues(context, new String[]{subTaskId});
                    //NA状态不写入决议行  任务的每一行也只保留备注信息
                    if (!"NA".equals(subTaskColorIdentification)){
                        if (CollectionUtils.isNotEmpty(taskRelateIssues)){
                            StringList ids = (StringList) taskRelateIssues.stream().map(m-> ((Map) m).get("id")).collect(Collectors.toCollection(StringList::new));
                            MapList info = DomainObject.getInfo(context, ids.toStringArray(), issueBusSel);
                            List<String> currentList = (List<String>) info.stream().map(m->((Map) m).get("current")).collect(Collectors.toList());
                            boolean closeCurrent = currentList.stream().anyMatch(s -> !"Closed".equals(s));
                            if (closeCurrent && UIUtil.isNotNullAndNotEmpty(esoReviewId)){
                                //任务名称加粗
                                richTextString.append(subTaskName+"\n",boldFont);
                            }
                        }
                        for (Object taskRelateIssue : taskRelateIssues) {
                            Map map = (Map) taskRelateIssue;
                            String issueId = UIUtil.getValue(map, "id");
                            DomainObject issueObj = DomainObject.newInstance(context, issueId);
                            Map issueObjInfo = issueObj.getInfo(context, issueBusSel);
                            String issueName = UIUtil.getValue(issueObjInfo, "name");
                            String issueDescription = UIUtil.getValue(issueObjInfo, "description");
                            String issueCurrent = UIUtil.getValue(issueObjInfo, "current");
                            if (!"Closed".equals(issueCurrent)  && UIUtil.isNotNullAndNotEmpty(esoReviewId)){
                                issueDescription = issueDescription.replace("\n", "  ").replace("\r", "  ");
                                richTextString.append(issueName + ": "+issueDescription+"\n",new XSSFFont());
                            }
                            issueNameAndDescList.add(issueName + ":" + issueDescription);
                            String issueActionTaken = UIUtil.getValue(issueObjInfo, "attribute[Action Taken]");
                            issueCorrectiveActionList.add(issueName + ":" +issueActionTaken);
                            String issueActualEndDate = UIUtil.getValue(issueObjInfo, "attribute[Actual End Date]");
                            //add by ljr 20251104
                            if ("Closed".equalsIgnoreCase(issueCurrent)&& UIUtil.isNotNullAndNotEmpty(issueActualEndDate)) {
                                issueDueDateList.add(issueActualEndDate);
                            }
                            StringList issuePersonList = issueObj.getInfoList(context, "from[Technical Assignee].to.name");
                            StringList issueFullNameList = new StringList();
                            if (CollectionUtils.isNotEmpty(issuePersonList)){
                                for (String s : issuePersonList) {
                                    Vector assignments = PersonUtil.getAssignments(context, s);
                                    JF_LOGGER.info("assignments:::{}",assignments);
                                    //ESO审核员排除
                                    if (assignments.contains("JfESOAdmin")){
                                        continue;
                                    }
                                    issueFullNameList.add(PersonUtil.getFullName(context, s));
                                }
                                issueResponsibleList.add(String.join(";", issueFullNameList));
                            }
                        }
                        JF_LOGGER.info("issueResponsibleList:::{}",issueResponsibleList);
                    }
                    String issueNameAndDesc = String.join("\n", issueNameAndDescList);
                    String issueCorrectiveAction = String.join("\n", issueCorrectiveActionList);
                    String issueResponsible = CollectionUtils.isNotEmpty(issueResponsibleList)
                            ? String.join("\n", issueResponsibleList)
                            : (CollectionUtils.isNotEmpty(taskResponsibleList) ? String.join("\n", taskResponsibleList) : "");
                    String maxDateString = EMPTY_STRING;
                    if (!issueDueDateList.isEmpty()) {
                        List<LocalDateTime> dates = new ArrayList<>();
                        for (String dateString : issueDueDateList) {
                            LocalDateTime dateTime1 = LocalDateTime.parse(dateString, inputFormatter);
                            dates.add(dateTime1);
                        }
                        // 找出最大的日期时间
                        LocalDateTime maxDateTime = Collections.max(dates);
                        // 将最大的 LocalDateTime 转换回原始字符串格式（可选）
                        maxDateString = maxDateTime.format(outputFormatter);
                    }
                    String issueDueDate = issueDueDateList.size() == taskRelateIssues.size() ? maxDateString : "";
                    if (UIUtil.isNullOrEmpty(esoReviewId)){
                        //如果记录为空 //add by ljr 20250909
                        hashMap.put(0, subTaskName);
                        hashMap.put(1, subTaskDescription);
                        //20260818 update by ljr 没有审核记录时先保留子任务当前颜色，全部任务读取完成后再统一判断是否清空。
                        hashMap.put(2, subTaskColorIdentification);
                        hashMap.put(3, "");
                        hashMap.put(4, "");
                        hashMap.put(5, "");
                        hashMap.put(6, "");
                        hashMap.put(7, "");
                        hashMap.put(8, "");
                        mapList.add(hashMap);
                    } else if ("NA".equals(subTaskColorIdentification)){
                        hashMap.put(0, subTaskName);
                        hashMap.put(1, subTaskDescription);
                        //update by ljr 20250923
                        hashMap.put(2, subTaskColorIdentification);
                        //                    hashMap.put(2, "");
                        hashMap.put(3, "");
                        hashMap.put(4, JF_Remark);
                        hashMap.put(5, "");
                        hashMap.put(6, "");
                        hashMap.put(7, "");
                        hashMap.put(8, "");
                        mapList.add(hashMap);
                    }else {
                        hashMap.put(0, subTaskName);
                        hashMap.put(1, subTaskDescription);
                        hashMap.put(2, subTaskColorIdentification);
                        hashMap.put(3, outputDoc);
                        hashMap.put(4, JF_Remark);
                        hashMap.put(5, issueNameAndDesc);
                        hashMap.put(6, issueCorrectiveAction);
                        hashMap.put(7, issueResponsible);
                        hashMap.put(8, issueDueDate);
                        mapList.add(hashMap);
                    }
                }
            }
            //20260818 update by ljr 没有审核记录且任一子任务颜色为空时，统一清空所有子任务颜色；全部有颜色时才保留。
            if (UIUtil.isNullOrEmpty(esoReviewId) && hasBlankTaskColor) {
                for (Object taskData : mapList) {
                    ((Map) taskData).put(2, "");
                }
            }
            int taskRow = startRow;
            for (Object o : mapList) {
                Row rows = sheet.createRow(taskRow);
                Map map = (Map) o;
                for (int cell = startCell; cell < endCell; cell++ ){
                    Cell cellItem = rows.createCell(cell, CellType.STRING);
                    cellItem.setCellValue(String.valueOf(map.get(cell)));
                    //                cellItem.setCellStyle(getLeftBorderStyle(workbook));
                    if (cell==3||cell==4||cell==5||cell==6 || cell==0 || cell==1){
                        cellItem.setCellStyle(leftBorderStyle);
                    }else {
                        cellItem.setCellStyle(borderStyle);
                    }
                }
                taskRow++;
            }

            //决议行写入  如果没有任务的 需要subTaskList.size + 1
            int size = subTaskList.size();
            if (size == 0) {
                size = 1;
            }
            Row decisionRow = sheet.getRow(startRow + size);
            Cell cell = decisionRow.getCell(2);
            Cell cellIssue = decisionRow.getCell(3);
            String decisionCell = "";
            String reviewPhaseState = "";
            String unreviewedPhaseState = EnoviaResourceBundle.getRangeI18NString(context, "JF_PhaseState", "RN", context.getSession().getLanguage());
            //add by LIUJR 20260617 导出签发表时统一读取签发状态，RN或空值按未审核显示
            if (UIUtil.isNotNullAndNotEmpty(esoReviewId)) {
                DomainObject reviewObj = DomainObject.newInstance(context, esoReviewId);
                reviewPhaseState = reviewObj.getInfo(context, "attribute[JF_PhaseState]");
            }
            //end
            //CS节点的决议取决于CS节点下的子任务颜色
            JF_LOGGER.info("check..............sheetName:{}", sheetName);
            JF_LOGGER.info("check..............esoReviewId:{}", esoReviewId);
            JF_LOGGER.info("check..............reviewPhaseState:{}", reviewPhaseState);
            JF_LOGGER.info("check..............colorList:{}", colorList);
            if ("CS".equals(sheetName)){
                //20260814 update by LIUJR CS决议只汇总子任务颜色，任一颜色为空或没有子任务时均为未审核。
                if (hasBlankTaskColor || CollectionUtils.isEmpty(subTaskList)) {
                    decisionCell = unreviewedPhaseState;
                } else {
                    JF_LOGGER.info("check..............CollectionUtils.isNotEmpty(colorList):{}", CollectionUtils.isNotEmpty(colorList));
                    if (CollectionUtils.isNotEmpty(colorList)) {
                        if (colorList.contains("R")) {
                            decisionCell = "R";
                        } else if (colorList.contains("Y")) {
                            decisionCell = "Y";
                        } else if (colorList.contains("NA")) {
                            decisionCell = "NA";
                        } else if (colorList.contains("G")) {
                            decisionCell = "G";
                        } else {
                            decisionCell = unreviewedPhaseState;
                        }
                    } else {
                        decisionCell = "G";
                    }
                }
            }else {
                //20260819 update by ljr ESO子节点任一子任务颜色为空时为RN；全部有颜色时取最新有效签发表记录颜色，没有有效记录时为RN。
                if (hasBlankTaskColor || CollectionUtils.isEmpty(subTaskList)) {
                    decisionCell = unreviewedPhaseState;
                } else if (UIUtil.isNotNullAndNotEmpty(reviewPhaseState)
                        && !"RN".equalsIgnoreCase(reviewPhaseState)) {
                    decisionCell = colorTransform(reviewPhaseState);
                } else {
                    decisionCell = unreviewedPhaseState;
                }
            }
            JF_LOGGER.info("check..............decisionCell:{}", decisionCell);
            //20260819 update by ljr CS和子级模块决议为未审核时，清空子任务行中状态至完成日期的内容。
            if (unreviewedPhaseState.equals(decisionCell)) {
                for (int rowIndex = startRow; rowIndex < startRow + mapList.size(); rowIndex++) {
                    Row taskDataRow = sheet.getRow(rowIndex);
                    if (taskDataRow == null) {
                        continue;
                    }
                    for (int cellIndex = 2; cellIndex < endCell; cellIndex++) {
                        Cell taskDataCell = taskDataRow.getCell(cellIndex);
                        if (taskDataCell != null) {
                            taskDataCell.setCellValue(EMPTY_STRING);
                        }
                    }
                }
            }
            cell.setCellValue(decisionCell);
            cellIssue.setCellValue(richTextString);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    public void writeOverallResult(Workbook workbook, int size ,String color, String sheetCsName) throws Exception{

        //写入总体评价行
        StringList decisionList = new StringList();
        StringList decisionContentList = new StringList();
        // 获取工作表数量
        int numberOfSheets = workbook.getNumberOfSheets();
        // 从第三个工作表（索引为2）开始遍历到最后一个工作表
        for (int i = 1; i < numberOfSheets; i++) {
            Sheet sheet = workbook.getSheetAt(i);
            String sheetName = sheet.getSheetName();
            Row row;
            if (i==1){
                row = sheet.getRow(sheet.getLastRowNum()-1);
            }else {
                row = sheet.getRow(sheet.getLastRowNum()-2);
            }
            Cell cell =row.getCell(2);
            String decision = "";
            if (cell != null) {
                decision = cell.getStringCellValue();
                decisionList.add(decision);
            }
            decisionContentList.add(sheetName+":"+decision);
        }
        Sheet sheetCS = workbook.getSheet(sheetCsName);
//        Sheet sheetCS = workbook.getSheet("CS");
//        Row rowDecision = sheetCS.getRow(8+size);
//        Cell cellDecision =rowDecision.getCell(2);
//        if (cellDecision != null) {
//            decisionList.add(cellDecision.getStringCellValue());
//        }
        Row rowLast = sheetCS.getRow(9+size);
//        String decisionCell = "";
//        if (CollectionUtils.isNotEmpty(decisionList)){
//            if (decisionList.contains("R") ||decisionList.contains("R（未审核）")){
//                decisionCell = "R";
//            }else if (decisionList.contains("Y")){
//                decisionCell = "Y";
//            }else if (decisionList.contains("G")){
//                decisionCell = "G";
//            }
//        }
        JF_LOGGER.info("decisionContentList-----:{}",decisionContentList);
        //总体评价取CS节点的颜色标识
        JF_LOGGER.info("colorTransform(color):{}", colorTransform(color));
        rowLast.getCell(2).setCellValue(colorTransform(color));
        rowLast.getCell(3).setCellValue(String.join("\n",decisionContentList));
    }

    private String getPhaseOutDate(Context context, String phaseName, DomainObject projectObj) throws Exception{
        String phaseOutDate = "";
        if (phaseName.startsWith("P-2+3")){
            phaseOutDate = projectObj.getAttributeValue(context,"JF_Phase2_3PlannedCompletionTime");
        }else if (phaseName.startsWith("P-1")){
            phaseOutDate = projectObj.getAttributeValue(context,"JF_Phase1PlannedCompletionTime");
        }else if (phaseName.startsWith("P-2")){
            phaseOutDate = projectObj.getAttributeValue(context,"JF_Phase2PlannedCompletionTime");
        }else if (phaseName.startsWith("P-3")){
            phaseOutDate = projectObj.getAttributeValue(context,"JF_Phase3PlannedCompletionTime");
        }else if (phaseName.startsWith("P-4")){
            phaseOutDate = projectObj.getAttributeValue(context,"JF_Phase4PlannedCompletionTime");
        }else if (phaseName.startsWith("P-5")){
            phaseOutDate = projectObj.getAttributeValue(context,"JF_Phase5PlannedCompletionTime");
        }
        return phaseOutDate;
    }

    /**
     * 获取任务关联的issue
     */
    public MapList getTaskRelateIssues(Context context, String[] args) throws Exception {
        String taskId = args[0];
        MapList relIssueList = null;
        try{
            DomainObject domObj = DomainObject.newInstance(context, taskId);
            String physicalId = domObj.getInfo(context, DomainConstants.SELECT_PHYSICAL_ID);
            relIssueList= new Issue().getAllIssues(context, physicalId, true, true);
            JF_LOGGER.info("getActiveIssues----relIssueList:{}",relIssueList);
        }catch(NoClassDefFoundError e){
            relIssueList= new MapList();
        }catch(Exception e){
            throw new FrameworkException(e.getMessage());
        }
        return relIssueList;
    }

    public String colorTransform(String color){
        String ret = "";
        if ("red".equals(color)){
            ret = "R";
        }else if ("green".equals(color)){
            ret = "G";
        }else if ("yellow".equals(color)){
            ret = "Y";
        }else if ("NA".equals(color)){
            ret = "NA";
        }
        if (UIUtil.isNotNullAndNotEmpty(ret)) {
            color = ret;
        }
        return color;
    }



    public static CellStyle getBorderStyle(Workbook workbook) {
        CellStyle cellBorderStyle = workbook.createCellStyle();
        cellBorderStyle.setBorderTop(BorderStyle.THIN);
        cellBorderStyle.setBorderBottom(BorderStyle.THIN);
        cellBorderStyle.setBorderLeft(BorderStyle.THIN);
        cellBorderStyle.setBorderRight(BorderStyle.THIN);
        cellBorderStyle.setAlignment(HorizontalAlignment.CENTER); // 水平居中
        cellBorderStyle.setVerticalAlignment(VerticalAlignment.CENTER); // 垂直居中
        cellBorderStyle.setWrapText(true); // 设置自动换行
        return cellBorderStyle;
    }

    public static CellStyle getLeftBorderStyle(Workbook workbook) {
        CellStyle cellBorderStyle = workbook.createCellStyle();
        cellBorderStyle.setBorderTop(BorderStyle.THIN);
        cellBorderStyle.setBorderBottom(BorderStyle.THIN);
        cellBorderStyle.setBorderLeft(BorderStyle.THIN);
        cellBorderStyle.setBorderRight(BorderStyle.THIN);
        cellBorderStyle.setAlignment(HorizontalAlignment.LEFT); // 改为左对齐
        cellBorderStyle.setVerticalAlignment(VerticalAlignment.CENTER); // 垂直居中
        cellBorderStyle.setWrapText(true); // 设置自动换行
        return cellBorderStyle;
    }

    /**
     * 获取任务的第一层的阶段
     * @param context
     * @param taskObj
     * @return
     * @throws Exception
     */
    public Map getFirstPhaseByTask(Context context,DomainObject taskObj) throws Exception {
        Map retMap = new HashMap();
        MapList  allPhase = taskObj.getRelatedObjects(context,"Subtask","Phase,Task,JF_ESOTask",StringList.create("type","id","name"),new StringList(),
                true,false,(short)0,"","",0);
        JF_LOGGER.info("getFirstPhaseByTask----allPhase:{}",allPhase);
        for (Object o : allPhase) {
            Map map = (Map) o;
            String type = UIUtil.getValue(map, "type");
            String id = UIUtil.getValue(map, "id");
            if ("Phase".equals(type)){
                DomainObject phaseObj = DomainObject.newInstance(context, id);
                String projectName = phaseObj.getInfo(context, "to[Subtask].from[Project Space].name");
                if (UIUtil.isNotNullAndNotEmpty(projectName)){
                    retMap = map;
                    break;
                }
            }
        }
        JF_LOGGER.info("getFirstPhaseByTask----retMap:{}",retMap);
        return retMap;
    }
    /**
     *复制sheet页
     */
    public Sheet copySheet(Workbook workbook, String sheetName) throws Exception {
        // 获取第三个工作表（索引为2，因为索引从0开始）
        Sheet sourceSheet = workbook.getSheetAt(2);

        // 创建新的工作表，名称为sheetName
        Sheet newSheet = workbook.createSheet(sheetName);

        // 复制列宽
        for (int i = 0; i < sourceSheet.getPhysicalNumberOfRows(); i++) {
            newSheet.setColumnWidth(i, sourceSheet.getColumnWidth(i));
        }
        // 复制行和单元格
        for (int i = 0; i <= sourceSheet.getLastRowNum(); i++) {
            Row sourceRow = sourceSheet.getRow(i);
            Row targetRow = newSheet.createRow(i);

            if (sourceRow != null) {
                // 复制行高
                targetRow.setHeight(sourceRow.getHeight());
                for (int j = 0; j < sourceRow.getLastCellNum(); j++) {
                    Cell sourceCell = sourceRow.getCell(j);
                    Cell targetCell = targetRow.createCell(j);

                    if (sourceCell != null) {
                        // 复制单元格的值
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
                            default:
                                targetCell.setCellValue(sourceCell.toString());
                        }
                        // 复制单元格的样式
                        CellStyle newCellStyle = workbook.createCellStyle();
                        newCellStyle.cloneStyleFrom(sourceCell.getCellStyle());
                        targetCell.setCellStyle(newCellStyle);

                    }
                }
            }
        }
        // 复制合并单元格
        for (int i = 0; i < sourceSheet.getNumMergedRegions(); i++) {
            CellRangeAddress mergedRegion = sourceSheet.getMergedRegion(i);
            newSheet.addMergedRegion(new CellRangeAddress(
                    mergedRegion.getFirstRow(),
                    mergedRegion.getLastRow(),
                    mergedRegion.getFirstColumn(),
                    mergedRegion.getLastColumn()
            ));
        }
        // 复制图片
//        if (workbook instanceof XSSFWorkbook) {
//            XSSFWorkbook xssfWorkbook = (XSSFWorkbook) workbook;
//            XSSFDrawing drawing = (XSSFDrawing) newSheet.createDrawingPatriarch();
////            List<XSSFPictureData> pictures = xssfWorkbook.getAllPictures();
//            //使用副本：在遍历之前，可以创建 pictures 集合的一个副本，然后对副本进行遍历。这样可以避免在遍历过程中对原集合的修改 导致报错java.util.ConcurrentModificationException: null
//            List<XSSFPictureData> pictures = new ArrayList<>(xssfWorkbook.getAllPictures());
//            for (XSSFPictureData picture : pictures) {
//                int pictureIndex = xssfWorkbook.addPicture(picture.getData(), picture.getPackagePart().getContentType().equals("image/png") ? Workbook.PICTURE_TYPE_PNG : Workbook.PICTURE_TYPE_JPEG);
//
//                // 创建一个新的锚点
//                XSSFClientAnchor anchor = new XSSFClientAnchor();
//                anchor.setCol1(7); // 设置图片的起始列
//                anchor.setRow1(1); // 设置图片的起始行
//                anchor.setCol2(8); // 设置图片的结束列
//                anchor.setRow2(2); // 设置图片的结束行
//
//                drawing.createPicture(anchor, pictureIndex);
//            }
//        }
        //update  by  ljr 20251107
        if (sourceSheet.getDrawingPatriarch() instanceof XSSFDrawing) {
            XSSFDrawing sourceDrawing = (XSSFDrawing) sourceSheet.getDrawingPatriarch();
            XSSFDrawing targetDrawing = (XSSFDrawing) newSheet.createDrawingPatriarch();
            XSSFWorkbook xssfWorkbook = (XSSFWorkbook) workbook;

            for (XSSFShape shape : sourceDrawing.getShapes()) {
                if (shape instanceof XSSFPicture) {
                    XSSFPicture pic = (XSSFPicture) shape;
                    XSSFPictureData picData = pic.getPictureData();
                    int idx = xssfWorkbook.addPicture(picData.getData(), picData.getPictureType());
                    XSSFClientAnchor anchor = pic.getClientAnchor();
                    XSSFClientAnchor newAnchor = new XSSFClientAnchor(
                            anchor.getDx1(), anchor.getDy1(),
                            anchor.getDx2(), anchor.getDy2(),
                            anchor.getCol1(), anchor.getRow1(),
                            anchor.getCol2(), anchor.getRow2()
                    );
                    targetDrawing.createPicture(newAnchor, idx);
                }
            }
        }
        return newSheet;
    }

    private void createConditionalFormatting( Sheet targetSheet) {
        // 获取条件格式实例
        SheetConditionalFormatting sheetCF = targetSheet.getSheetConditionalFormatting();
        // 创建条件格式规则：当值为 "r" 时背景为红色
        ConditionalFormattingRule ruleRed = sheetCF.createConditionalFormattingRule("ISNUMBER(SEARCH(\"r\", C9))");
        PatternFormatting fillRed = ruleRed.createPatternFormatting();
        fillRed.setFillBackgroundColor(IndexedColors.RED.getIndex());
        fillRed.setFillPattern(PatternFormatting.SOLID_FOREGROUND);
        // 创建条件格式规则：当值为 "g" 时背景为绿色
        ConditionalFormattingRule ruleGreen = sheetCF.createConditionalFormattingRule(ComparisonOperator.EQUAL, "\"g\"");
        PatternFormatting fillGreen = ruleGreen.createPatternFormatting();
        fillGreen.setFillBackgroundColor(IndexedColors.BRIGHT_GREEN.getIndex());
//        fillGreen.setFillForegroundColor(new XSSFColor(new byte[]{(byte) 0x00, (byte) 0xFF, (byte) 0x00})); // 使用自定义绿色
        fillGreen.setFillPattern(PatternFormatting.SOLID_FOREGROUND);
        // 创建条件格式规则：当值为 "y" 时背景为黄色
        ConditionalFormattingRule ruleYellow = sheetCF.createConditionalFormattingRule(ComparisonOperator.EQUAL, "\"y\"");
        PatternFormatting fillYellow = ruleYellow.createPatternFormatting();
        fillYellow.setFillBackgroundColor(IndexedColors.LIGHT_YELLOW.getIndex());
//        fillYellow.setFillForegroundColor(new XSSFColor(new byte[]{(byte) 0xFF, (byte) 0xFF, (byte) 0x99})); // 使用自定义黄色
        fillYellow.setFillPattern(PatternFormatting.SOLID_FOREGROUND);
        // 创建条件格式规则：当值为 "NA" 时背景为黄色
        ConditionalFormattingRule ruleGrey = sheetCF.createConditionalFormattingRule(ComparisonOperator.EQUAL, "\"NA\"");
        PatternFormatting fillGrey = ruleGrey.createPatternFormatting();
        fillGrey.setFillBackgroundColor(IndexedColors.GREY_40_PERCENT.getIndex());
        fillGrey.setFillPattern(PatternFormatting.SOLID_FOREGROUND);
        // 设置条件格式的范围
        CellRangeAddress[] ranges = {
//                CellRangeAddress.valueOf("$C$9:$C$100")
                CellRangeAddress.valueOf("$C$9:$C$1048576")
        };
        // 添加条件格式
        sheetCF.addConditionalFormatting(ranges, ruleRed);
        sheetCF.addConditionalFormatting(ranges, ruleGreen);
        sheetCF.addConditionalFormatting(ranges, ruleYellow);
        sheetCF.addConditionalFormatting(ranges, ruleGrey);
    }




    /**
     * 复制ESO任务   将选择的
     * @param context
     * @param args
     * @author LIUJR
     * @throws
     * @return java.lang.Boolean
     * @date 30/06/2025 10:31
     * @description
     */
    public Boolean duplicateESOTaskFilter(Context context, String[]args) throws Exception{
        Boolean flag = Boolean.TRUE;
//        String taskESOId = DomainConstants.EMPTY_STRING;
        String duration = "";
        String finishDate = "";
        String startDate = "";
        String constraintDate = "";
        String taskConstraintType = "";
        try {
            Map map = (Map) JPO.unpackArgs(args);
            String  parentId = (String) map.get("parentId");  //当前点击复制ESO任务页面的对象id
            String  phaseId = (String) map.get("phaseId");//当前选择任务所属于的阶段id
            String  strObjectId = (String) map.get("objectId"); //选择的ESO任务id
            String  projectId = (String) map.get("projectId");//所属的项目id
            String  selectPhaseIds = (String) map.get("selectPhaseIds");//点击选择复制的阶段id
            HashMap<String, String>  phaseAndEsoMap = (HashMap<String, String>) map.get("phaseAndEsoMap");//点击选择复制的阶段中的ESO 任务的id；复制的时候，就是这个复制到这个任务下
            JF_LOGGER.info("strObjectId:{}", strObjectId);
            JF_LOGGER.info("projectId:{}", projectId);
            JF_LOGGER.info("phaseAndEsoMap:{}", phaseAndEsoMap);

            //遍历需要复制的任务，找到ESO任务，然后将选择的ESO任务复制到下面，再根据参数过滤任务
            DomainObject domainObject = DomainObject.newInstance(context);
            domainObject.setId(projectId);
            String strGrade = domainObject.getAttributeValue(context, JF_PLMConstants_mxJPO.ATTR_JF_ProjectGrade);
            domainObject.setId(strObjectId);
            //查找下面的eso任务 选择CS的情况 要将下面的结构 筛选出来 然后再去过滤
            //获取阶段的任务
            MapList  allTaskMapList = domainObject.getRelatedObjects(
                    context, DomainRelationship.RELATIONSHIP_SUBTASK,"JF_ESOTask",
                    StringList.create(DomainConstants.SELECT_NAME,DomainConstants.SELECT_ID, DomainConstants.SELECT_TYPE, DomainConstants.SELECT_LEVEL),
                    new StringList(),
                    false,
                    true,
                    (short)0,
                    "","",0);
            //排序
            JF_LOGGER.info("allTaskMapList:{}", allTaskMapList);
            SelectList taskIdList = (SelectList)allTaskMapList.stream()
                    .map(m -> UIUtil.getValue((Map)m, SELECT_ID))
                    .collect(Collectors.toCollection(SelectList::new));
            //分组
            Map groupingMap = JF_NewECRService_mxJPO.getMapListGroupingMap(context, allTaskMapList, SELECT_ID);
            JF_LOGGER.info("groupingMap:{}", groupingMap);
            MapList palList = getObjectListFromPAL(context,projectId);
            String taskOrder = getTaskOrder(context, palList, taskIdList);
            String[] split = taskOrder.split("\\|");
            StringList orderList = StringList.create(split);
            JF_LOGGER.info("orderList:{}", orderList);
            Map<Integer, StringList> esoTaskMap = new HashMap<>();
            esoTaskMap.put(1, new StringList(strObjectId));
            Integer num = 1;
            if (!groupingMap.isEmpty()) {
                for (int i = 0; i < orderList.size(); i++) {
                    String orderId = orderList.get(i);
                    List oldInfoList = (List) groupingMap.get(orderId);
                    Map map1 = (Map) oldInfoList.get(0);
                    String level = UIUtil.getValue(map1, DomainConstants.SELECT_LEVEL);
                    Integer iNum = Integer.parseInt(level) + 1;
                    if (esoTaskMap.containsKey(iNum)) {
                        StringList stringList = esoTaskMap.get(iNum);
                        stringList.add(orderId);
                        esoTaskMap.put(iNum, stringList);
                    } else {
                        esoTaskMap.put(iNum, new StringList(orderId));
                    }
                    num = iNum;
                }
            }
            JF_LOGGER.info("esoTaskMap:{}", esoTaskMap);
            //获取ESO任务下的所有的
            String firstESOTaskId = DomainConstants.EMPTY_STRING;
            DomainObject newObject = DomainObject.newInstance(context);
            String tempId = JPO.invoke(context, "JF_PublicMethodClass", null, "getBasicUrl", new String[]{"ESOTask.Template.Id"}, String.class);
            JF_LOGGER.info("tempId:{}", tempId);
            palList = getObjectListFromPAL(context,tempId);
            ContextUtil.startTransaction(context, true);
            for (Map.Entry<String, String> entry : phaseAndEsoMap.entrySet()) {
                //需要复制的阶段名称
                String phaseName = entry.getKey();
                //需要复制的阶段下的name为ESO的task
                String esoTaskId = entry.getValue();
                domainObject.setId(esoTaskId);
                duration = domainObject.getAttributeValue(context, "Task Estimated Duration");
                finishDate = domainObject.getAttributeValue(context, "Task Estimated Finish Date");
                startDate = domainObject.getAttributeValue(context, "Task Estimated Start Date");
                constraintDate = domainObject.getAttributeValue(context, "Task Constraint Date");
                taskConstraintType = domainObject.getAttributeValue(context, "Task Constraint Type");
                StringList esoList = new StringList();
                JF_LOGGER.info("esoTaskId:{}", esoTaskId);
                JF_LOGGER.info("phaseName:{}", phaseName);
                JF_LOGGER.info("esoTaskId:{}", esoTaskId);
                //结构挂载id 会改变
                firstESOTaskId = esoTaskId;
                //复制任务
                for (int j = 1; j <= num; j++) {
                    //复制的ESO任务
                    StringList strEsoIdList = esoTaskMap.get(j);
                    String newTaskId = DomainConstants.EMPTY_STRING;
                    for (int i = 0; i < strEsoIdList.size(); i++) {
                        String strEsoId = strEsoIdList.get(i);
                        domainObject.setId(strEsoId);
                        //所有的属性
                        AttributeList attributeValues = domainObject.getAttributeValues(context);
                        //创建新的任务挂在结构挂载id上
                        Map temp = new HashMap();
                        temp.put("taskName", domainObject.getInfo(context, DomainConstants.SELECT_NAME));  //任务名称 ESO任务的Name等于职能
                        temp.put("description", domainObject.getDescription(context));  //说明
                        temp.put(DomainConstants.SELECT_ID, firstESOTaskId);  //挂载的id
                        newTaskId = JF_ProjectTaskUtils_mxJPO.createTask(context, firstESOTaskId, temp, JF_PLMConstants_mxJPO.TYPE_JF_ESOTask, "addTaskAbove");
                        updateTaskOrganization(context, context.getUser(), newTaskId);
                        esoList.add(newTaskId);
                        JF_LOGGER.info("newTaskId:{}", newTaskId);
                        newObject.setId(newTaskId);
                        //同步属性
                        attributeValues.add(new Attribute(JF_PLMConstants_mxJPO.ATTR_ProjectRole, "Chair manager"));
                        attributeValues = removeColorAndCount(context, attributeValues);
                        newObject.setAttributeValues(context, attributeValues);
                        //根据复制的ESO任务的属性,去对应的ESO任务模板中找到符合条件的阶段下的任务并挂上去
                        StringList copyIdList = filterESOTaskAsCopy(context, tempId, phaseName, strEsoId, strGrade);
                        JF_LOGGER.info("copyIdList:{}", copyIdList);
//                        //复制任务
//                        JF_ProjectTaskUtils_mxJPO.duplicateFilterESOTask(context, newTaskId, copyIdList);
//                        //复制任务的项目角色、说明、name、关联的收藏
//                        duplicateTaskRoleAndLink(context, newTaskId, copyIdList);
                        if (null == copyIdList || copyIdList.isEmpty()) {
                            continue;
                        }
                        createFilterTask(context, newTaskId, copyIdList, palList, esoList);
                    }
                    //挂接结构
                    firstESOTaskId = newTaskId;
                }
                try {
                    if (!esoList.isEmpty()) {
                        esoList.add(esoTaskId);
                        for (int i = 0; i < esoList.size(); i++) {
                            String id = esoList.get(i);
                            JF_LOGGER.info("@@@@@@@@@@@@@@@@@@@@@@@@");
                            JF_LOGGER.info("开始设置时间");
                    /*
                    *   Task Actual Start Date 7/17/2025 8:00:00 AM
                        Task Estimated Duration 1.0
                        Task Estimated Finish Date 3/6/2024 5:00:00 PM
                        Task Estimated Start Date 3/6/2024 8:00:00 AM
                        Task Constraint Date 6/26/2025 5:00:00 PM
                    * */
                            String mql = "mod bus '"+id+"' 'Task Estimated Start Date' '"+startDate+"' 'Task Estimated Finish Date' '"+finishDate+"' 'Task Estimated Duration' '"+duration+"' 'Task Constraint Date' '"+constraintDate+"' 'Task Constraint Type' '"+ taskConstraintType +"';";
                            JF_LOGGER.info("mql:{}",mql);
                            MqlUtil.mqlCommand(context,false,mql,true);
                        }
                    }
                }catch (Exception e) {
                    e.printStackTrace();
                    throw e;
                }finally {
                }
            }
            ContextUtil.commitTransaction(context);

        }catch (Exception e) {
            e.printStackTrace();
            flag = Boolean.FALSE;
            ContextUtil.abortTransaction(context);
        }
        return flag;
    }

    /**
     * 将指定属性去掉
     * @param context
     * @param attributeList
     * @author LIUJR
     * @throws
     * @return matrix.db.AttributeList
     * @date 2025/11/4 13:30
     * @description
     */
    public static AttributeList removeColorAndCount(Context context, AttributeList attributeList) {
        for (int i = 0; i < attributeList.size(); i++) {
            Attribute attribute = attributeList.get(i);
            String name = attribute.getName();
            if ("JF_ColorIdentification".equalsIgnoreCase(name) || "JF_ReviewCount".equalsIgnoreCase(name)) {
                attribute.setValue("");
            }
        }
        return attributeList;
    }

    /*
     * @description:项目属性的编辑权限
     * @author: caipan
     * @date: 2025/7/11 09:45:19
     * @param: * @param[1] context
     * @param[2] args
     * @return:
     **/
    public static boolean canEditProjectField(Context context, String args[]) throws Exception {
        HashMap inputMap = (HashMap)JPO.unpackArgs(args);
        HashMap field    = (HashMap) inputMap.get("field");
        HashMap requestMap    = (HashMap) inputMap.get("requestMap");
        String fieldName = UIUtil.getValue(field, SELECT_NAME);
        JF_LOGGER.info("inputMap{}",inputMap);
        StringList wholeManager = new StringList();
        wholeManager.add("JF_ProjectCode");
        wholeManager.add("JF_ProjectCustomers");
//        wholeManager.add("JF_ProjectGrade");
        wholeManager.add("JF_ProjectStatus");
        wholeManager.add("JF_ProjectECI");

        StringList ESOManager = new StringList();
        ESOManager.add("JF_Phase1PlannedCompletionTime");
        ESOManager.add("JF_Phase2PlannedCompletionTime");
        ESOManager.add("JF_Phase3PlannedCompletionTime");
        ESOManager.add("JF_Phase4PlannedCompletionTime");
        ESOManager.add("JF_Phase5PlannedCompletionTime");
        ESOManager.add("JF_Phase2_3PlannedCompletionTime");
        ESOManager.add("JF_ProjectGrade");
        String objectId = null;
        if(requestMap == null){
            objectId   = (String)inputMap.get("objectId");
        }else{
            objectId   = (String)requestMap.get("objectId");
        }
        //拿到整椅经理
        JF_ECRProcess_mxJPO process = new JF_ECRProcess_mxJPO();
        Map wholeMap = process.getProjectRole(context,objectId, "Chair manager");
        String whoelStr =UIUtil.getValue(wholeMap, SELECT_NAME);
        String currentName = context.getUser();
        Vector assignments = PersonUtil.getAssignments(context, currentName);
        boolean esoFlag = assignments.contains("JfESOAdmin");

        if(wholeManager.contains(fieldName)&&currentName.equalsIgnoreCase(whoelStr)){
            return true;
        }
        JF_LOGGER.info("esoFlag:{}",esoFlag);
        JF_LOGGER.info("ESOManager.contains(fieldName):{}",ESOManager.contains(fieldName));
        if(ESOManager.contains(fieldName)&&esoFlag){
            return true;
        }
        return false;
    }


    /**
     *
     * 签发状态按钮权限
     * @param context
     * @param args
     * @return
     * @throws Exception
     */
    public boolean getRecordSignAccess(Context context, String[] args) throws Exception {
        HashMap inputMap = (HashMap) JPO.unpackArgs(args);
        HashMap requestMap = (HashMap) inputMap.get("requestMap");
        String strObjectId = null;
        if (requestMap == null) {
            strObjectId = (String) inputMap.get("objectId");
        } else {
            strObjectId = (String) requestMap.get("objectId");
        }
        String strLoginUser = context.getUser();
        Vector assignments = PersonUtil.getAssignments(context, strLoginUser);
        boolean esoFlag = assignments.contains("JfESOAdmin");
        DomainObject task = DomainObject.newInstance(context, strObjectId);
        String taskOwner = task.getInfo(context, "owner");
        StringList nameList = task.getInfoList(context, "to[Assigned Tasks].from.name");
        if ((Objects.equals(taskOwner,strLoginUser) && esoFlag) || (nameList.contains(strLoginUser) && esoFlag)){
            return true;
        }else {
            return false;
        }
    }

    /**
     * 添加分派人
     * @param context
     * @param args
     * @throws Exception
     */
    public String addAssignPerson(Context context,String[] args)throws Exception{
        String mess = "添加成功";
        try {
            ContextUtil.pushContext(context);
            HashMap requestmap = JPO.unpackArgs(args);
            String taskIds = UIUtil.getValue(requestmap, "taskIds");
            String personIds = UIUtil.getValue(requestmap, "personIds");
            String[] personIdArray = personIds.split(",");
            String[] split = taskIds.split(",");
            JF_Util_mxJPO jfUtilMxJPO = new JF_Util_mxJPO();
            com.matrixone.apps.common.Task task = new com.matrixone.apps.common.Task();
            for (String taskId : split) {
                task.setId(taskId);
                StringList assigneesList =  task.getInfoList(context, "to[Assigned Tasks].from.id");
                for (String personId : personIdArray) {
                    if(!assigneesList.contains(personId)) {
                        jfUtilMxJPO.assignPerson(context, personId, task);
                    }
                }
            }
        }catch (Exception e){
            JF_LOGGER.error("addAssignPerson----error:",e);
            mess = e.getMessage();
        }finally {
            ContextUtil.popContext(context);
        }
        return mess;
    }

    /**
     * 移除分派人按钮权限
     * @param context
     * @param args
     * @return
     * @throws Exception
     */
    public boolean getJFPMCAssigneeDeleteAccess(Context context, String[] args) throws Exception {
        HashMap inputMap = (HashMap) JPO.unpackArgs(args);
        HashMap requestMap = (HashMap) inputMap.get("requestMap");
        String strObjectId = null;
        if (requestMap == null) {
            strObjectId = (String) inputMap.get("objectId");
        } else {
            strObjectId = (String) requestMap.get("objectId");
        }
        String strLoginUser = context.getUser();
        DomainObject task = DomainObject.newInstance(context, strObjectId);
        String taskOwner = task.getInfo(context, "owner");
        StringList nameList = task.getInfoList(context, "to[Assigned Tasks].from.name");
        if (Objects.equals(taskOwner,strLoginUser) || nameList.contains(strLoginUser)){
            return true;
        }else {
            return false;
        }
    }

    /**
     * esoReview完成时生成新的签发表  第一版问题版本
     * @param context
     * @param args
     * @throws Exception
     */
    public void reCreatingESOSignTableBack(Context context, String[] args) throws Exception {
        try {
            String esoReviewId = args[0];
            DomainObject esoReviewObj = DomainObject.newInstance(context, esoReviewId);
            String esoTaskId = esoReviewObj.getInfo(context, "to[JFESOTask2ESOReview].from.id");
            String projectId = JF_PublicProjectQuery_mxJPO.getProjectIdByTaskId(context, esoTaskId);
            String personId = esoReviewObj.getInfo(context, "to[JFESOReview2Person].from.id");
            String documentConnId = esoReviewObj.getInfo(context, "from[JFESOReview2Document].id");
            String documentId = esoReviewObj.getInfo(context, "from[JFESOReview2Document].to.id");
            String oldDocumentOwner = DomainObject.newInstance(context, documentId).getInfo(context, "owner");
            //断开旧文档
            DomainRelationship.disconnect(context,documentConnId);
            JF_LOGGER.info("生成新的签发表");
            //生成新的签发表
            ContextUtil.pushContext(context,oldDocumentOwner,"","");
            createESOSignTableDocument(context,new String[]{esoReviewId,esoTaskId,projectId,personId});
        }catch (Exception e){
            JF_LOGGER.error("---reCreatingESOSignTable---error",e);
        }finally {
            ContextUtil.popContext(context);
        }
    }


    /**
     * esoReview完成时生成新的签发表
     * ESO总工审核后，回写的签字和意见需要回写到自动生成的签发表中，而不是重新生成一份新的带签字的签发表
     * @param context
     * @param args
     * @throws Exception
     * @author LIUJR
     * @date 2026/7/28
     */
    public void reCreatingESOSignTable(Context context, String[] args) throws Exception {
        JF_LOGGER.info("reCreatingESOSignTable~~~~~~~~~~~~~~~~~~~~~~~~~``");
        Boolean isPush = Boolean.FALSE;
        try {
//            ContextUtil.pushContext(context);
            String esoReviewId = args[0];
            DomainObject esoReviewObj = DomainObject.newInstance(context, esoReviewId);
            String esoTaskId = esoReviewObj.getInfo(context, "to[JFESOTask2ESOReview].from.id");
            String documentId = esoReviewObj.getInfo(context, "from[JFESOReview2Document].to.id");
            DomainObject documentObj = DomainObject.newInstance(context, documentId);
            String oldDocumentOwner = documentObj.getInfo(context, SELECT_OWNER);
            String documentCurrent = documentObj.getInfo(context, SELECT_CURRENT);
            boolean isDocumentReleased = "RELEASED".equalsIgnoreCase(documentCurrent);
            //获取审批状态
            StringList comments = esoReviewObj.getInfoList(context, "from[Object Route].to.to[Route Task].from.attribute[Comments]");
            StringList status = esoReviewObj.getInfoList(context, "from[Object Route].to.to[Route Task].from.attribute[Approval Status]");
            String comment =  (comments.isEmpty() || null == comments)? "" : comments.get(0);
            String approval =  (status.isEmpty() || null == status)? "" : status.get(0);
            String esoReviewCurrent = esoReviewObj.getInfo(context, SELECT_CURRENT);
            String chiefEngineerName = "";
            JF_LOGGER.info("context:{}", context.getUser());
            if ("Released".equals(esoReviewCurrent)){
                chiefEngineerName = esoReviewObj.getInfo(context, "from[JFESOReview2Person].to.name");
                chiefEngineerName = PersonUtil.getFullName(context, chiefEngineerName);
            }
            JF_LOGGER.info("chiefEngineerName:{}", chiefEngineerName);
            //下载旧的签发表
            CommonDocument commonDocument = new CommonDocument();
            commonDocument.setId(documentId);
            MapList versionList = JF_PublicMethodClass_mxJPO.getDocumentFiles(context, documentId);
            JF_LOGGER.info("versionList:{}", versionList);
            FileList fileList = new FileList();
            String strDirPath = context.createWorkspace()  + File.separator;   //存储地址
            String filePath = EMPTY_STRING;
            String fileName = EMPTY_STRING;
            try {
                ContextUtil.pushContext(context);
                JF_LOGGER.info("context:{}", context.getUser());
                isPush = Boolean.TRUE;
                //20260728 update by ljr 总工审批意见写入前，已发布的签发表文档先调整为工作中，完成文件更新后再按原逻辑发布；
                if (isDocumentReleased) {
                    MqlUtil.mqlCommand(context, false, "mod bus " + documentId + " current IN_WORK", true);
                    JF_LOGGER.info("签发表文档已调整为工作中, documentId:{}", documentId);
                }
                for (int i = 0; i < versionList.size(); i++) {
                    Map map = (Map) versionList.get(i);
                    String fileName1 = (String) map.get(CommonDocument.SELECT_TITLE);
//                    String fileFormat = (String) map.get(CommonDocument.SELECT_FILE_FORMAT);
                    if (!fileName1.endsWith(".pdf")) {
                        JF_LOGGER.info(".pdf");
                        matrix.db.File file = new matrix.db.File(fileName1, "generic");
                        filePath = strDirPath + fileName1;
                        fileList.add(file);
                        fileName = fileName1;
                        commonDocument.checkoutFiles(context, false, "generic", fileList, strDirPath);
                    }
                    commonDocument.deleteFile(context, fileName1, "generic");
                }
            } catch (MatrixException e) {
                e.printStackTrace();
                throw e;
            } finally {
                if (isPush) {
                    ContextUtil.popContext(context);
                    isPush = Boolean.FALSE;
                }
            }
            //生成新的签发表，将审批意见写入
            // 第一步：读取并修改 workbook
            JF_LOGGER.info("context:{}", context.getUser());
            JF_LOGGER.info("context:{}", context.getUser());
            Workbook workbook;
            try (FileInputStream fis = new FileInputStream(filePath)) {
                workbook = WorkbookFactory.create(fis);
            }
            // 在 workbook 上做你的修改
            Sheet sheet = workbook.getSheetAt(1);
            Row row3 = sheet.getRow(3);
            if (!"Reject".equalsIgnoreCase(approval) && UIUtil.isNotNullAndNotEmpty(approval)) {
                row3.getCell(8).setCellValue(chiefEngineerName);
            }
            Row row4 = sheet.getRow(4);
            if (UIUtil.isNotNullAndNotEmpty(comment)) {
                row4.getCell(8).setCellValue(comment);
            }
            // 第二步：将修改后的 workbook 写回文件
            try (FileOutputStream fos = new FileOutputStream(filePath)) {
                workbook.write(fos); // 关键：调用 workbook.write()
            }
            // 最后关闭 workbook（虽然 write 后通常可不 close，但建议显式关闭）
            workbook.close();
            JF_LOGGER.info("生成新的签发表");
            ContextUtil.pushContext(context,oldDocumentOwner,"","");
            isPush = Boolean.TRUE;
            JF_LOGGER.info("context:{}", context.getUser());
            //将旧的文件全部断开 将新文件重新checkin进去
            //这个创建版本需要在checkin之前 才能自动转换pdf
            commonDocument.checkinFile(context, true, true, "", "generic", fileName,strDirPath);
            //add by LIUJR 20260617 签发表对象发布后同步发布关联签发表文档，失败只记录日志不阻断签发表发布
            Boolean isDocumentPush = Boolean.FALSE;
            try {
                if (UIUtil.isNotNullAndNotEmpty(documentId)) {
                    ContextUtil.pushContext(context);
                    isDocumentPush = Boolean.TRUE;
                    MqlUtil.mqlCommand(context, false, "mod bus " + documentId + " current RELEASED", true);
                }
            } catch (Exception documentPublishException) {
                JF_LOGGER.error("---reCreatingESOSignTable publish document error---", documentPublishException);
            } finally {
                if (isDocumentPush) {
                    ContextUtil.popContext(context);
                }
            }
            //end
            JF_LOGGER.info("context:{}", context.getUser());
        }catch (Exception e){
            e.printStackTrace();
            JF_LOGGER.error("---reCreatingESOSignTable---error",e);
        }finally {
            if (isPush) {
                ContextUtil.popContext(context);
            }
        }
    }

    /**
     * eso审核记录的前四列的编辑权限
     * @param context
     * @param args
     * @return
     * @throws Exception
     */


    public StringList getEsoReviewAttributeEditAccess(Context context, String[] args) throws Exception {
        StringList stringList = new StringList();
        try {
            Map programMap = (Map) JPO.unpackArgs(args);
            MapList objList = (MapList) programMap.get("objectList");
            Vector personAssignments = PersonUtil.getAssignments(context);
            boolean isRole = false;
            if(personAssignments.contains("JfESOAdmin")){
                isRole = true;
            }
            for (int i = 0; i < objList.size(); i++) {
                String id = (String) ((Map) objList.get(i)).get("id");
                DomainObject Obj = DomainObject.newInstance(context, id);
                String hasRoute = Obj.getInfo(context, "from[Object Route]");
                String current = Obj.getInfo(context, "current");
                if ("Create".equals(current) && "FALSE".equalsIgnoreCase(hasRoute)&&isRole) {
                    stringList.add(Boolean.toString(true));
                } else {
                    stringList.add(Boolean.toString(false));
                }
            }
        } catch (Exception e) {
            JF_LOGGER.error("---getEsoReviewAttributeEditAccess---error",e);
        }
        return stringList;
    }

    /**
     * 在ESO视图中增加被分派人的列，全员可见
     * @param context
     * @param args
     * @return
     * @throws Exception
     */
    public StringList getAssignPersonColumnValue(Context context,String[] args) throws Exception{
        StringList res = new StringList();
        try {
            Map argMaps = JPO.unpackArgs(args);
            MapList argMapList = (MapList) argMaps.get(JF_PLMConstants_mxJPO.STRING_OBJECTLIST);
            Map columnMap = (Map) argMaps.get("columnMap");
            String colName = (String) columnMap.get(SELECT_NAME);
            JF_LOGGER.info("colName:{}", colName);
            DomainObject domainObject = DomainObject.newInstance(context);
            ContextUtil.pushContext(context);
            for (int i = 0; i < argMapList.size(); i++) {
                Map infoMap = (Map) argMapList.get(i);
                String strId = (String) infoMap.get(SELECT_ID);
                domainObject.setId(strId);
                StringList personList = domainObject.getInfoList(context,"to[Assigned Tasks].from.name");
                StringList personFullNameList  = new StringList();
                for (String s : personList) {
                    personFullNameList.add(PersonUtil.getFullName(context, s));
                }
                res.add(String.join("\n",personFullNameList));
            }
        } catch (Exception e) {
            JF_LOGGER.error("---getAssignPersonColumnValue---error",e);
        } finally {
            ContextUtil.popContext(context);
        }
        return res;
    }

    /**
     * 签发状态的审核次数和状态需要同步到WBS中对应的审核次数和颜色标识
     * @param context
     * @param args
     * @throws Exception
     */


    public  void updateESOTaskReviewCountAndColor(Context context, String[] args) throws Exception{
        try {
            ContextUtil.pushContext(context);
            Map paramsMap = (Map) JPO.unpackArgs(args);
            Map columnMap = (Map) paramsMap.get(STRING_COLUMNMAP);
            String basicName = (String) columnMap.get(DomainConstants.SELECT_NAME);
            HashMap paramMap = (HashMap)paramsMap.get(STRING_PARAMMAP);
            String objectId = (String)paramMap.get(STRING_OBJECTID);
            String newValue = (String)paramMap.get(STRING_NEW_VALUE);
            StringList selList = JF_Util_mxJPO.basicBolistSel();
            selList.add("attribute[JF_ReviewCounte]");
            selList.add("attribute[JF_PhaseState]");
            DomainObject esoReviewObj = DomainObject.newInstance(context, objectId);
            String date = esoReviewObj.getInfo(context, SELECT_ORIGINATED);
            JF_LOGGER.error("date:{}",date);
            String phase = esoReviewObj.getInfo(context,"attribute[JF_PhaseState]");
            JF_LOGGER.info("----updateESOTaskReviewCountAndColor---phase::{}",phase);
            String esoTaskId = esoReviewObj.getInfo(context, "to[JFESOTask2ESOReview].from.id");
            DomainObject esoTaskObj = DomainObject.newInstance(context,esoTaskId);
//            StringList esoReviewList  = esoTaskObj.getInfoList(context,"from[JFESOTask2ESOReview|to.attribute[JF_ReviewCounte]!=''].to.id");
            MapList esoReviewList = esoTaskObj.getRelatedObjects(
                    context,
                    "JFESOTask2ESOReview",
                    "JFESOReview",
                    selList,
                    JF_Util_mxJPO.basicRellistSel(),
                    false,
                    true,
                    (short) 1,
                    "attribute[JF_ReviewCounte]!=''||attribute[JF_PhaseState]!=''",
                    "",
                    0);
            String maxReviewCount = "0";
            DomainObject maxReviewObj = null;
            for (int i = 0; i < esoReviewList.size(); i++) {
                Map info = (Map) esoReviewList.get(i);
                String JF_ReviewCounte = UIUtil.getValue(info, "attribute[JF_ReviewCounte]");
                String s = UIUtil.getValue(info, SELECT_ID);
                DomainObject esoOldReviewObj = DomainObject.newInstance(context, s);
                if (Objects.equals(s,objectId)){
                    continue;
                }
                if (UIUtil.isNullOrEmpty(JF_ReviewCounte)){
                    continue;
                }
                // 比较并更新最大次数和对应对象
                if (Integer.parseInt(JF_ReviewCounte) > Integer.parseInt(maxReviewCount)) {
                    maxReviewCount = JF_ReviewCounte;
                    maxReviewObj = esoOldReviewObj;
                }
            }
            //如果当前的值比最大的大 则以当前为主
            if (Integer.parseInt(newValue) > Integer.parseInt(maxReviewCount)){
                esoTaskObj.setAttributeValue(context,"JF_ReviewCount",newValue);
                esoTaskObj.setAttributeValue(context,"JF_ColorIdentification",convertColorRange(phase));
                esoReviewObj.setAttributeValue(context,"JF_ReviewCounte",newValue);
            }else {
                if (maxReviewObj != null){
                    Map info = maxReviewObj.getInfo(context, selList);
                    String JF_ReviewCounte = UIUtil.getValue(info, "attribute[JF_ReviewCounte]");
                    String JF_PhaseState = UIUtil.getValue(info, "attribute[JF_PhaseState]");
                    esoTaskObj.setAttributeValue(context,"JF_ReviewCount",JF_ReviewCounte);
                    esoTaskObj.setAttributeValue(context,"JF_ColorIdentification",convertColorRange(JF_PhaseState));
                    esoReviewObj.setAttributeValue(context,"JF_ReviewCounte",newValue);
                }
            }

        } catch (Exception e) {
            JF_LOGGER.error("---updateProjectReviewCounteAndColor---error",e);
        }finally {
            ContextUtil.popContext(context);
        }
    }

    /**
     * 保存ESO签发记录审核状态并同步模块节点颜色
     *
     * @param context 上下文
     * @param args Table列更新参数
     * @return void
     * @throws Exception 签发记录或关联任务查询失败时抛出异常
     * @author LIUJR
     * @date 2026/9/2
     */
    public  void updateESOTaskColor(Context context, String[] args) throws Exception{
        try {
            ContextUtil.pushContext(context);
            Map paramsMap = (Map) JPO.unpackArgs(args);
            Map columnMap = (Map) paramsMap.get(STRING_COLUMNMAP);
            String basicName = (String) columnMap.get(DomainConstants.SELECT_NAME);
            HashMap paramMap = (HashMap)paramsMap.get(STRING_PARAMMAP);
            String objectId = (String)paramMap.get(STRING_OBJECTID);
            String newValue = (String)paramMap.get(STRING_NEW_VALUE);
            StringList selList = new StringList();
            selList.add("attribute[JF_ReviewCounte]");
            selList.add("attribute[JF_PhaseState]");
            DomainObject esoReviewObj = DomainObject.newInstance(context, objectId);
            String date = esoReviewObj.getInfo(context, SELECT_ORIGINATED);
            JF_LOGGER.error("date:{}",date);
            Map esoReviewInfo = esoReviewObj.getInfo(context, selList);
            String esoJF_PhaseState = UIUtil.getValue(esoReviewInfo,"attribute[JF_PhaseState]");
            String esoJF_ReviewCounte = UIUtil.getValue(esoReviewInfo,"attribute[JF_ReviewCounte]");
            String esoTaskId = esoReviewObj.getInfo(context, "to[JFESOTask2ESOReview].from.id");
            DomainObject esoTaskObj = DomainObject.newInstance(context,esoTaskId);
            //20260902 update by LIUJR 审核状态低于直接普通子任务最高颜色时仅提示，不阻止保存。
            String colorWarningMessage = EMPTY_STRING;
            if (!"RN".equalsIgnoreCase(newValue)) {
                int reviewColorLevel = 0;
                if ("G".equalsIgnoreCase(newValue)) {
                    reviewColorLevel = 1;
                } else if ("Y".equalsIgnoreCase(newValue)) {
                    reviewColorLevel = 2;
                } else if ("R".equalsIgnoreCase(newValue)) {
                    reviewColorLevel = 3;
                }
                int highestTaskColorLevel = 0;
                String highestTaskColor = EMPTY_STRING;
                String taskColorSelect = "attribute[JF_ColorIdentification]";
                MapList directTaskList = esoTaskObj.getRelatedObjects(
                        context,
                        DomainConstants.RELATIONSHIP_SUBTASK,
                        DomainConstants.TYPE_TASK,
                        StringList.create(DomainConstants.SELECT_TYPE, taskColorSelect),
                        new StringList(),
                        false,
                        true,
                        (short) 1,
                        EMPTY_STRING,
                        EMPTY_STRING,
                        0);
                for (int i = 0; i < directTaskList.size(); i++) {
                    Map directTaskInfo = (Map) directTaskList.get(i);
                    if (!DomainConstants.TYPE_TASK.equalsIgnoreCase(UIUtil.getValue(directTaskInfo, DomainConstants.SELECT_TYPE))) {
                        continue;
                    }
                    String taskColor = UIUtil.getValue(directTaskInfo, taskColorSelect);
                    if (UIUtil.isNullOrEmpty(taskColor)) {
                        continue;
                    }
                    int taskColorLevel = 0;
                    if ("green".equalsIgnoreCase(taskColor) || "G".equalsIgnoreCase(taskColor)) {
                        taskColorLevel = 1;
                    } else if ("yellow".equalsIgnoreCase(taskColor) || "Y".equalsIgnoreCase(taskColor)) {
                        taskColorLevel = 2;
                    } else if ("red".equalsIgnoreCase(taskColor) || "R".equalsIgnoreCase(taskColor)) {
                        taskColorLevel = 3;
                    }
                    if (taskColorLevel > highestTaskColorLevel) {
                        highestTaskColorLevel = taskColorLevel;
                        highestTaskColor = taskColorLevel == 3 ? "R" : (taskColorLevel == 2 ? "Y" : "G");
                    }
                }
                //20260903 update by LIUJR 没有可统计的子任务颜色时不比较；审核状态高于或低于最高颜色时均提示。
                if (reviewColorLevel > 0 && highestTaskColorLevel > 0
                        && reviewColorLevel != highestTaskColorLevel) {
                    colorWarningMessage = EnoviaResourceBundle.getProperty(
                            context,
                            "emxProgramCentralStringResource",
                            context.getLocale(),
                            "emxProgramCentral.ESOReview.PhaseStateDifferentFromTaskColor");
                    colorWarningMessage = colorWarningMessage.replace("reviewState", newValue)
                            .replace("taskColor", highestTaskColor);
                }
            }
            String esoTaskJF_ReviewCount = esoTaskObj.getInfo(context,"attribute[JF_ReviewCount]");
//            StringList infoList = esoTaskObj.getInfoList(context, "from[JFESOTask2ESOReview|to.attribute[JF_ReviewCounte]!=''].to.originated");
            selList.add("originated");
            StringList infoList = (StringList) esoTaskObj.getRelatedObjects(
                    context,
                    "JFESOTask2ESOReview",
                    "JFESOReview",
                    selList,
                    JF_Util_mxJPO.basicRellistSel(),
                    false,
                    true,
                    (short) 1,
                    "attribute[JF_ReviewCounte]!=''||attribute[JF_PhaseState]!=''",
                    "",
                    0).stream().map(m -> {
                Map map = (Map) m;
                return UIUtil.getValue(map, "originated");
            }).collect(Collectors.toCollection(SelectList::new));

            List<LocalDateTime> dates = new ArrayList<>();
            for (String dateString : infoList) {
                LocalDateTime dateTime = LocalDateTime.parse(dateString, inputFormatter);
                dates.add(dateTime);
            }
            // 找出最大的日期时间
            LocalDateTime maxDateTime = Collections.max(dates);
            // 将最大的 LocalDateTime 转换回原始字符串格式（可选）
            String maxDateString = maxDateTime.format(inputFormatter);
            JF_LOGGER.error("maxDateString:{}",maxDateString);
            if (Integer.parseInt(esoJF_ReviewCounte) == Integer.parseInt(esoTaskJF_ReviewCount)) {
                if (maxDateString.equalsIgnoreCase(date)) {
                    esoTaskObj.setAttributeValue(context, "JF_ColorIdentification", convertColorRange(newValue));
                }
            }
            esoReviewObj.setAttributeValue(context,"JF_PhaseState",newValue);
            if (UIUtil.isNotNullAndNotEmpty(colorWarningMessage)) {
                emxContextUtil_mxJPO.mqlNotice(context, colorWarningMessage);
            }
        } catch (Exception e) {
            JF_LOGGER.error("---updateProjectReviewCounteAndColor---error",e);
        }finally {
            ContextUtil.popContext(context);
        }
    }

    public String convertColorRange(String color){
        if ("R".equalsIgnoreCase(color)){
            return "red";
        }else if ("Y".equalsIgnoreCase(color)){
            return "yellow";
        }else if ("G".equalsIgnoreCase(color)){
            return "green";
        }else if ("RN".equalsIgnoreCase(color)){
            return "red";
        }
        return "";
    }


    /**
     * 文档页面编辑时候所属阶段得range值
     * @param context
     * @param args
     * @return
     * @throws Exception
     */
    public Map getConnProjectPhaseRange(Context context, String[] args) throws Exception {
        Map returnMap = new HashMap();
        StringList strings = new StringList();
        try {
            Map map = (Map) JPO.unpackArgs(args);
            Map requestMap = (Map) map.get("requestMap");
            String objectId = (String) requestMap.get(JF_PLMConstants_mxJPO.STRING_OBJECTID);
            DomainObject domainObject = DomainObject.newInstance(context, objectId);
            StringList selList = new StringList();
            selList.add("attribute[JF_ProjectDoc]");
            selList.add("attribute[JF_ConnProjectName]");
            Map info = domainObject.getInfo(context, selList);
            String JF_ProjectDoc = UIUtil.getValue(info,"attribute[JF_ProjectDoc]");
            if ("N".equals(JF_ProjectDoc)){
                returnMap.put("field_choices", strings);
                returnMap.put("field_display_choices", strings);
                return returnMap;
            }
            //拿取文件所属的项目
            String strPaths = domainObject.getInfo(context, "to[Vaulted Objects].from.attribute[Folder Path]");
            String strProjectSpaceId = "";
            if (UIUtil.isNotNullAndNotEmpty(strPaths)) {
                String[] split = strPaths.split("\\|");
                strProjectSpaceId = split[0];
            } else {
                strProjectSpaceId = domainObject.getInfo(context, "to[Task Deliverable].from.to[Project Access Key].from.from[Project Access List].to.id");
            }
            if (UIUtil.isNullOrEmpty(strProjectSpaceId)){
                returnMap.put("field_choices", strings);
                returnMap.put("field_display_choices", strings);
                return returnMap;
            }
            DomainObject projectObject = DomainObject.newInstance(context, strProjectSpaceId);
            MapList allPhase = projectObject.getRelatedObjects(context,"Subtask","Phase",StringList.create("name","id","type"),new StringList(),false,true,(short)1,"","",0);
            for (Object o : allPhase) {
                Map m = (Map) o;
                String type1 = UIUtil.getValue(m, "type");
                if ("Phase".equals(type1)){
                    strings.add(UIUtil.getValue(m,"name"));
                }
            }

            returnMap.put("field_choices", strings);
            returnMap.put("field_display_choices", strings);
        } catch (Exception e) {
            JF_LOGGER.error("JF_ESO----Error in getConnProjectPhaseRange", e);
        }

        return returnMap;
    }

    /**
     * 获取本项目得人员
     * @param context
     * @param args
     * @return
     */
    public static StringList getPersonsFromProject(Context context, String[] args) {
        StringList infoList = new StringList();
        try{
            Map paramMap = (Map) JPO.unpackArgs(args);
            String objectId = (String)paramMap.get("objectId");
            String projectId = JF_PublicProjectQuery_mxJPO.getProjectIdByTaskId(context, objectId);
            DomainObject projectObj = DomainObject.newInstance(context,projectId);
            infoList = projectObj.getInfoList(context,"from[Member].to.id");
        }catch (Exception e) {
            e.printStackTrace();
        }

        return infoList;
    }


    public boolean getSyncProjectPhaseTimeAccess(Context context,String[] args) throws Exception{
        String user = context.getUser();
        HashMap inputMap = (HashMap) JPO.unpackArgs(args);
        HashMap requestMap = (HashMap) inputMap.get("requestMap");
        String strObjectId = null;
        if (requestMap == null) {
            strObjectId = (String) inputMap.get("objectId");
        } else {
            strObjectId = (String) requestMap.get("objectId");
        }
        DomainObject obj = DomainObject.newInstance(context,strObjectId);
        Vector assignments = PersonUtil.getAssignments(context, user);
        if (assignments.contains("JfESOAdmin")) {
            return true;
        }else {
            return false;
        }
    }

    public StringList getAssignPersonForIssue(Context context,String[] args) throws Exception{
        StringList res = new StringList();
        try {
            Map argMaps = JPO.unpackArgs(args);
            MapList argMapList = (MapList) argMaps.get(JF_PLMConstants_mxJPO.STRING_OBJECTLIST);
            Map columnMap = (Map) argMaps.get("columnMap");
            String colName = (String) columnMap.get(SELECT_NAME);
            JF_LOGGER.info("colName:{}", colName);
            DomainObject domainObject = DomainObject.newInstance(context);
            ContextUtil.pushContext(context);
            for (int i = 0; i < argMapList.size(); i++) {
                Map infoMap = (Map) argMapList.get(i);
                String strId = (String) infoMap.get(SELECT_ID);
                domainObject.setId(strId);
                StringList personList = domainObject.getInfoList(context,"from[Technical Assignee].to.name");
                StringList personFullNameList  = new StringList();
                for (String s : personList) {
                    personFullNameList.add(PersonUtil.getFullName(context, s));
                }
                res.add(String.join("\n",personFullNameList));
            }
        } catch (Exception e) {
            JF_LOGGER.error("---getAssignPersonForIssue---error",e);
        } finally {
            ContextUtil.popContext(context);
        }
        return res;
    }


    public Vector<String> isLastVersionForDocument(Context context,String[] args) throws Exception{
        Vector<String> res = new Vector<>();
        try {
            Map argMaps = JPO.unpackArgs(args);
            MapList argMapList = (MapList) argMaps.get(JF_PLMConstants_mxJPO.STRING_OBJECTLIST);
            Map columnMap = (Map) argMaps.get("columnMap");
            String colName = (String) columnMap.get(SELECT_NAME);
            JF_LOGGER.info("colName:{}", colName);
            DomainObject domainObject = DomainObject.newInstance(context);
            ContextUtil.pushContext(context);
            for (int i = 0; i < argMapList.size(); i++) {
                Map infoMap = (Map) argMapList.get(i);
                String strId = (String) infoMap.get(SELECT_ID);
                domainObject.setId(strId);
                BusinessObject nextRevision = domainObject.getNextRevision(context);
                if (nextRevision.exists(context)){
//                    res.add("<table><tbody><tr><td style=\"vertical-align:middle;padding-left:1px;\"><img style=\"vertical-align:middle;\" src=\"../common/images/iconSmallHigherRevision.png\" title=\"存在更高的修订版\"></td></tr></tbody></table>");
//                    res.add("<img style=\"vertical-align:middle;\" src=\"../common/images/iconSmallHigherRevision.png\" title=\"存在更高的修订版\">");
//                    res.add(StringEscapeUtils.escapeHtml4("<img border=\"0\" src=\"../common/images/iconSmallHigherRevision.png\" title=\"存在更高的修订版\">"));
//                    res.add("<img border='0' src='../common/images/iconSmallHigherRevision.png' "+StringEscapeUtils.escapeHtml4("title='存在更高的修订版'/>"));
//                    res.add(StringEscapeUtils.escapeHtml4("<img border=\"0\" src=\"../common/images/iconSmallRoute.png\"></img>"));
                    res.add("<img border='0' src='../common/images/iconSmallHigherRevision.png' title='存在更高的修订版'/>");
                }else {
                    res.add("");
                }
            }
        } catch (Exception e) {
            JF_LOGGER.error("---isLastVersionForDocument---error",e);
        } finally {
            ContextUtil.popContext(context);
        }
        return res;
    }

    public StringList editEsoRemarkAccess(Context context, String[] args) throws Exception {
        StringList result = new StringList();
        JF_LOGGER.info("editEsoRemarkAccess。。。。。。。。。。。。。。。。。。。。。。");
        Map paramMap = JPO.unpackArgs(args);
        try {
            MapList objectList = (MapList) paramMap.get("objectList");
            Map columnMap = (Map) paramMap.get("columnMap");
            Map colAttrMap = (Map) columnMap.get("colAttrMap");
            String colName = (String) colAttrMap.get(SELECT_NAME);
            String user = context.getUser();
            //判断当前用户时候有角色
            JF_LOGGER.info("colName:{}", colName);
            JF_LOGGER.info("user:{}", user);
            DomainObject task = DomainObject.newInstance(context);
            for (int i = 0; i < objectList.size(); i++) {
                Map taskMap = (Map) objectList.get(i);
                String taskId = (String) taskMap.get(DomainConstants.SELECT_ID);
                task.setId(taskId);
                JF_LOGGER.info("taskId:{}", taskId);
                String taskType = task.getInfo(context, DomainConstants.SELECT_TYPE);
                //判断ESO任务的责任人并且有JfESOAdmin角色有权限编辑
                if (DomainConstants.TYPE_PROJECT_SPACE.equalsIgnoreCase(taskType)) {
                    result.add("false");
                } else {
                    StringList infoList = task.getInfoList(context, "to[Assigned Tasks].from.name");
                    JF_LOGGER.info("infoList:{}", infoList);
                    if (infoList.contains(user)){
                        result.add("true");
                    }else {
                        result.add("false");
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 1.总工审核的结果要邮件到ESO审核员、整椅经理和直线经理；
     * 直线经理：ESO任务下的所有任务的被分配人的直线经理。
     * @param context
     * @param args
     * @author LIUJR
     * @throws
     * @return void
     * @date 2025/9/22 15:17
     * @description
     */
    public void reviewPromoteActionSendEmail(Context context, String[] args) throws Exception {
        try {
            //ESO签发状态对象id
            String esoReviewId = args[0];
            DomainObject domainObject = DomainObject.newInstance(context);
            domainObject.setId(esoReviewId);
            //流程的名称
            String route = domainObject.getInfo(context, "from[Object Route].to.name");
            //总工人员名称
            String zg = domainObject.getInfo(context, "from[Object Route].to.to[Route Task].from.from[Project Task].to.name");
            //审批备注
            StringList comments = domainObject.getInfoList(context, "from[Object Route].to.to[Route Task].from.attribute[Comments]");
            //审批状态
            StringList status = domainObject.getInfoList(context, "from[Object Route].to.to[Route Task].from.attribute[Approval Status]");
            //关联的文档
            String docTitle = domainObject.getInfo(context, "from[JFESOReview2Document].to.attribute[Title]");
            //add by LIUJR 20260616 总工确认邮件增加签发表颜色标识和签发次数
            String reviewColorIdentification = domainObject.getAttributeValue(context, "JF_PhaseState");
            //20260728 update by ljr 总工确认邮件中签发表颜色为RN时使用属性Range国际化值，避免邮件直接显示内部值RN；
            if ("RN".equalsIgnoreCase(reviewColorIdentification)) {
                reviewColorIdentification = EnoviaResourceBundle.getRangeI18NString(
                        context,
                        "JF_PhaseState",
                        "RN",
                        context.getSession().getLanguage());
            }
            String reviewCount = domainObject.getAttributeValue(context, "JF_ReviewCounte");
            //end
            //add by LIUJR 20260706 总工确认邮件增加计划签发时间和实际签发时间
            String estimatedSignDate = domainObject.getAttributeValue(context, "JF_EstimatedSignDate");
            String actualSignDate = domainObject.getAttributeValue(context, "JF_ActualSignDate");
            if (UIUtil.isNotNullAndNotEmpty(estimatedSignDate)) {
                try {
                    LocalDateTime dateTime = LocalDateTime.parse(estimatedSignDate, inputFormatter);
                    estimatedSignDate = dateTime.format(outputFormatter);
                } catch (Exception ignored) {
                }
            }
            if (UIUtil.isNotNullAndNotEmpty(actualSignDate)) {
                try {
                    LocalDateTime dateTime = LocalDateTime.parse(actualSignDate, inputFormatter);
                    actualSignDate = dateTime.format(outputFormatter);
                } catch (Exception ignored) {
                }
            }
            //end
            //关联的ESOTask
            String esoTaskId = domainObject.getInfo(context, "to[JFESOTask2ESOReview].from.id");
            domainObject.setId(esoTaskId);
            //add by LIUJR 20260706 总工确认邮件ESO任务信息增加职能、排数、研发地点和部门
            String function = domainObject.getAttributeValue(context, JF_PLMConstants_mxJPO.ATTR_JF_Function);
            String rows = domainObject.getAttributeValue(context, JF_PLMConstants_mxJPO.ATTR_JF_Rows);
            String modules = domainObject.getAttributeValue(context, JF_PLMConstants_mxJPO.ATTR_JF_Modules);
            String locations = domainObject.getAttributeValue(context, JF_PLMConstants_mxJPO.ATTR_JF_Locations);
            String department = domainObject.getAttributeValue(context, JF_PLMConstants_mxJPO.ATTR_JF_Department);
            //end
            //类型  颜色标识
            String esoType = domainObject.getAttributeValue(context, JF_PLMConstants_mxJPO.ATTR_JF_ESOType);
            String colorIdentification = domainObject.getAttributeValue(context, "JF_ColorIdentification");
            //add by LIUJR 20260616 总工确认邮件ESO任务信息增加任务签发次数
            String taskReviewCount = domainObject.getAttributeValue(context, "JF_ReviewCount");
            //end
            //所属阶段
            Map phaseInfo = getFirstPhaseByTask(context, domainObject);
            String phaseName = UIUtil.getValue(phaseInfo, "name");
            //项目id
            String projectId = JF_PublicProjectQuery_mxJPO.getProjectIdByTaskId(context, esoTaskId);
            //需要发邮件人员账号集合
            HashSet personSet = new HashSet<String>();
            //需要拿取直线经理集合
            StringList linePersonList = new StringList();
            //拿取ESO任务下的ESO人员
            StringList assignedList = domainObject.getInfoList(context, "to[Assigned Tasks].from.name");
            String esoName = DomainConstants.EMPTY_STRING;
            JF_LOGGER.info("assignedList:{}", assignedList);
            //拿取整椅经理
            String chairManagerName = JF_PublicMethodClass_mxJPO.getProjectWherePerson(context, projectId, SELECT_NAME, "", "attribute[Project Role]=='Chair manager'");
            personSet.add(chairManagerName);
            for (String personName : assignedList) {
                //判断是不是ESO审核人员
                Vector assignments = PersonUtil.getAssignments(context, personName);
                JF_LOGGER.info("assignments:{}", assignments);
                if (assignments.contains("JfESOAdmin")) {
                    esoName = personName;
                    personSet.add(personName);
                } else {
                    if (!personSet.contains(chairManagerName)) {
                        linePersonList.add(personName);
                    }
                }
            }
            //拿取ESOTask下的任务的被分派人的直线经理 需要排除ESO和整椅经理
            DomainObject taskObject = DomainObject.newInstance(context);
            StringList taskList = domainObject.getInfoList(context, "from[Subtask].to.id");
            for (int i = 0; i < taskList.size(); i++) {
                String taskId = taskList.get(i);
                taskObject.setId(taskId);
                //获取被分派人
                StringList taskAssignedList = taskObject.getInfoList(context, "to[Assigned Tasks].from.name");
                for (String personName : taskAssignedList) {
                    Vector assignments = PersonUtil.getAssignments(context, personName);
                    if (assignments.contains("JfESOAdmin")) {
                        personSet.add(personName);
                    } else {
                        if (!personSet.contains(personName)) {
                            linePersonList.add(personName);
                        }
                    }

                }
            }
            //将需要拿取直线经理的账号拿取到直线经理
            DomainObject personObject = DomainObject.newInstance(context);
            for (int i = 0; i < linePersonList.size(); i++) {
                String strLineManagerId = JF_PublicMethodClass_mxJPO.getPersonLineManager(context, null, linePersonList.get(i));
                if (UIUtil.isNotNullAndNotEmpty(strLineManagerId)) {
                    personObject.setId(strLineManagerId);
                    personSet.add(personObject.getInfo(context, SELECT_NAME));
                }
            }
            //发送邮件List
            StringList personList = StringList.create(personSet);
            //开始发邮件   ps项目下phase阶段下的ESO中的ESO任务CS的签发记录发起的签发流程route已被总工确认！
            //拿取邮件文档
            // 创建多部分消息体
            MimeMultipart multipart = new MimeMultipart(); // 默认混合模式
            //邮件内容的html模板部分
            BodyPart msgBodyPart = new MimeBodyPart();
            //邮件内容的html模板部分
            String html = JF_PublicMethodClass_mxJPO.getPageHTMLResourceFile(context, "ESOReviewEmail", "zh");
            org.jsoup.nodes.Document doc = Jsoup.parse(html);
            Element info = doc.getElementById("info1");
            JF_LOGGER.info("info：{}",info);
            JF_LOGGER.info("info：{}", null == info);
            String text = info.text();
            DomainObject project = DomainObject.newInstance(context, projectId);
            String replace = text.replace("ps", project.getDescription(context))
                    .replace("phase", phaseName)
                    .replace("cs", domainObject.getInfo(context, SELECT_NAME))
                    .replace("route", route);
            info.text(replace);
            info = doc.getElementById("info2");
            text = info.text();
            replace = text.replace("ps", project.getDescription(context))
                    .replace("phase", phaseName)
                    .replace("cs", domainObject.getInfo(context, SELECT_NAME))
                    .replace("route", route);
            info.text(replace);
            doc.getElementById("zg").append(PersonUtil.getFullName(context, zg));
            doc.getElementById("status").append(EnoviaResourceBundle.getRangeI18NString(context, "Approval Status", status.get(0), "zh"));
            doc.getElementById("advice").append(comments.get(0));
            //add by LIUJR 20260706 总工确认邮件写入ESO任务职能、排数、研发地点和部门
            doc.getElementById("JF_Function").append(function);
            doc.getElementById("JF_Rows").append(rows);
            doc.getElementById("JF_Modules").append(modules);
            doc.getElementById("JF_Locations").append(locations);
            doc.getElementById("JF_Department").append(department);
            //end
            doc.getElementById("JF_ESOType").append(esoType);
            doc.getElementById("JF_ColorIdentification").append(colorIdentification);
            //add by LIUJR 20260616 总工确认邮件写入ESO任务签发次数和签发表信息
            doc.getElementById("JF_ReviewCount").append(taskReviewCount);
            // add by LIUJR 20260707 调整总工确认邮件签发表名称展示，只展示签发表标题，避免长文件名撑开邮件内容。
            doc.getElementById("fileName").append(docTitle);
            // end
            doc.getElementById("JF_PhaseState").append(reviewColorIdentification);
            doc.getElementById("JF_ReviewCounte").append(reviewCount);
            //end
            //add by LIUJR 20260706 总工确认邮件写入计划签发时间和实际签发时间
            doc.getElementById("JF_EstimatedSignDate").append(estimatedSignDate);
            doc.getElementById("JF_ActualSignDate").append(actualSignDate);
            //end
            //链接地址
            Properties prop = NioJDUtils.readPageObject(context, "JFJDConfig");
            String linkAddress = prop.getProperty("JF.3dspace.JFUrl").trim();
            linkAddress += esoReviewId;
            //设置链接地址
            org.jsoup.nodes.Element address = doc.getElementById("Address");
            //链接href
            address.attr("href", linkAddress);
            //设置显示的值
            address.text("点击查看签发记录详情(Click to view ESO Review)");
            String htmlContent = doc.toString();
            msgBodyPart.setContent(htmlContent, "text/html;charset=utf-8");//html代码部分
            multipart.addBodyPart(msgBodyPart);
            StringList toEmailList = new StringList();
            for (int i = 0; i < personList.size(); i++) {
                domainObject = PersonUtil.getPersonObject(context, personList.get(i));
                String emailAddress = domainObject.getAttributeValue(context, DomainObject.ATTRIBUTE_EMAIL_ADDRESS);
                toEmailList.add(emailAddress);
            }
            //toEmailList.join(",")
            JF_LOGGER.error("toEmailList:{}",toEmailList);
            Boolean aBoolean = JF_SendEmailUtils_mxJPO.SendEmail(context, toEmailList.join(","), "ESO 总工确认通知(ESO Chief Engineer Confirmation Notice)", multipart);
        }catch (Exception e){
            JF_LOGGER.error("---reCreatingESOSignTable---error",e);
        }

    }

    /**
     * 获取项目成员 - 整椅经理
     * @param context
     * @param args
     * @author LIUJR
     * @throws
     * @return matrix.util.StringList
     * @date 2025/3/4 14:38
     * @description
     */
    public StringList getESOTransferOwnerToChairManager(Context context, String[] args) throws Exception{
        StringList resultList = new StringList();
        Boolean isPush = Boolean.FALSE;
        try {
            Map program = (Map)JPO.unpackArgs(args);
            ContextUtil.pushContext(context);
            JF_LOGGER.info("program:{}", program.toString());
            String projectId = (String)program.get("projectId");
            if (UIUtil.isNotNullAndNotEmpty(projectId)) {
                String chairManagerId = JF_PublicMethodClass_mxJPO.getProjectWherePerson(context, projectId, SELECT_ID, "", "attribute[Project Role]=='Chair manager'");
                resultList.add(chairManagerId);
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (isPush) {
                ContextUtil.popContext(context);
            }
            throw new RuntimeException(e);
        }
        JF_LOGGER.info("resultList:{}", resultList.toString());
        return resultList;
    }

    /**
     * 签发表 驳回生成签发表
     * @param context
     * @param args
     * @author LIUJR
     * @throws
     * @return int
     * @date 2025/10/22 10:15
     * @description
     */
    @ProgramCallable
    public void inboxTaskReviewPromoteAction(Context context, String[] args) throws Exception {
        //是否是ECR 状态Review 的项目经理审核任务
        try {
            String strObjectId = args[0];
            JF_LOGGER.info("strObjectId:{}", strObjectId);
            DomainObject inBoxTask = DomainObject.newInstance(context, strObjectId);
            //拿取到签发状态对象
            //状态
            //comments
            String strApproveState = inBoxTask.getInfo(context, "attribute[Approval Status]");
            //不是驳回直接跳过
            if (!"Reject".equalsIgnoreCase(strApproveState)) {
                return ;
            }
            //审核记录
            String id = inBoxTask.getInfo(context, "from[Route Task].to.to[Object Route].from.id");
            if (UIUtil.isNullOrEmpty(id)) {
                return;
            }
            DomainObject domainObject = DomainObject.newInstance(context, id);
            if (!"JFESOReview".equalsIgnoreCase(domainObject.getInfo(context, SELECT_TYPE))) {
                return;
            }
            //生成签发表
            reCreatingESOSignTable(context, new String[]{id});
        }catch (Exception e) {
            JF_LOGGER.error(e.getMessage());
        }
    }

    /**
     * 总工审核的时候 校验生成的去签发表sheetName是否超出限制
     * @param context
     * @param args
     * @author LIUJR
     * @throws
     * @return int
     * @date 2025/11/3 10:49
     * @description
     */
    @ProgramCallable
    public int checkESOTaskSheetNameInfo(Context context, String[] args) throws Exception {
        JF_LOGGER.info("!@@@@@@@@@@@@@@@@@@@@@@@@@@@@@22");
        try {
            String strObjectId = args[0];
            DomainObject inBoxTask = DomainObject.newInstance(context, strObjectId);
            String id = inBoxTask.getInfo(context, "from[Route Task].to.to[Object Route].from.id");
            JF_LOGGER.info("id:{}", id);
            if (UIUtil.isNullOrEmpty(id)) {
                return 0;
            }
            DomainObject domainObject = DomainObject.newInstance(context);
            domainObject.setId(id);
            if (!"JFESOReview".equalsIgnoreCase(domainObject.getInfo(context, SELECT_TYPE))) {
                return 0;
            }
            JF_LOGGER.info("JFESOReview:{}", id);
            String esoTaskId = domainObject.getInfo(context, "to[JFESOTask2ESOReview].from.id");
            if (UIUtil.isNullOrEmpty(esoTaskId)) {
                return 0;
            }
            JF_LOGGER.info("esoTaskId:{}", esoTaskId);
            domainObject.setId(esoTaskId);
            StringList busSelList = JF_Util_mxJPO.basicBolistSel();
            StringList esoTaskBusSel = StringList.create("attribute[JF_Function]","attribute[JF_Rows]", "attribute[JF_ESOType]", "attribute[JF_Grade]","attribute[JF_Modules]","attribute[JF_ColorIdentification]");
            busSelList.addAll(esoTaskBusSel);
            JF_LOGGER.info("busSelList:{}", busSelList);
            MapList subTask = domainObject.getRelatedObjects(context,"Subtask","JF_ESOTask,Task",busSelList,new StringList(),false,true,(short)1,"","",0);
            subTask.add(domainObject.getInfo(context, busSelList));
            JF_LOGGER.info("subTask:{}", subTask);
            //类型分组
            Map groupTask = (Map) subTask.stream().collect(Collectors.groupingBy(m -> {
                Map map = (Map) m;
                return UIUtil.getValue(map, "type");
            }));
            List subEsoTaskList = (List) groupTask.get("JF_ESOTask");
            if (CollectionUtils.isEmpty(subEsoTaskList)) {
                return 0;
            }
            JF_LOGGER.info("subEsoTaskList:{}", subEsoTaskList);
            StringList sheetNameList = new StringList();
            for (int i = 0; i < subEsoTaskList.size(); i++) {
                Map map = (Map) subEsoTaskList.get(i);
                String subEsoFunction = UIUtil.getValue(map, "attribute[JF_Function]");
                String subEsoModules = UIUtil.getValue(map, "attribute[JF_Modules]");
                String subEsoRows = UIUtil.getValue(map, "attribute[JF_Rows]");
                String subEsoESOType = UIUtil.getValue(map, "attribute[JF_ESOType]");
                // 组合名称
                String sheetBaseName = subEsoModules + "_" + subEsoRows + "_" + subEsoESOType;
                if (sheetBaseName.length()>31) {
                    sheetNameList.add(sheetBaseName);
                }
            }
            JF_LOGGER.info("sheetNameList:{}", sheetNameList);
            if (!sheetNameList.isEmpty()) {
                String mess = ComponentsUIUtil.getI18NString(context, context.getLocale().toString(), "emxComponents.Notice.ESOReviewError", new String[]{});
                emxContextUtil_mxJPO.mqlNotice(context, mess.replaceAll("sheetName", sheetNameList.join(",")));
                return 1;
            } else {
                return 0;
            }
        }catch (Exception e) {
            JF_LOGGER.info(e.getMessage());
        }
        return 0;
    }


    /**
     * 问题被分派人在项目成员范围中选择
     * @param context
     * @param args
     * @author LIUJR
     * @throws
     * @return matrix.util.StringList
     * @date 2025/11/5 10:04
     * @description
     */
    public StringList  addExistingIssueAssigneePerson(Context context, String[] args) throws Exception{
        StringList resultList = new StringList();
        try {
            Map paramsMap = JPO.unpackArgs(args);
            JF_LOGGER.info("paramsMap:{}", paramsMap);
            //Issue
            String strObjectId = (String) paramsMap.get(STRING_OBJECTID);
            //获取问题所属的任务的项目
            com.matrixone.apps.common.Issue issObj = new com.matrixone.apps.common.Issue();
            MapList relBusObjPageList=issObj.getAllAffectedItems(context,strObjectId,new StringList(DomainConstants.SELECT_ID));
            JF_LOGGER.info("getAllReportedAgainst:{}", relBusObjPageList);
            Map taskMap = (Map) relBusObjPageList.get(0);
            String taskId = UIUtil.getValue(taskMap, SELECT_ID);
            DomainObject domainObject = DomainObject.newInstance(context, taskId);
            String projectId = domainObject.getInfo(context, "to[Project Access Key].from.from[Project Access List].to.id");
            resultList = JF_PublicMethodClass_mxJPO.getProjectAllPersons(context, projectId, SELECT_ID);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
        return resultList;
    }

    /**
     *  ESO任务下创建的问题在提交到审批时，校验问题详情界面的采取的操作值，值为空，不允许提升状态
     * @param context
     * @param args
     * @author LIUJR
     * @throws
     * @return int
     * @date 2025/11/5 14:20
     * @description
     */
    @ProgramCallable
    public int issueRequireActionTaken(Context context, String[] args) throws Exception{
        int iReturn = 0;
        JF_LOGGER.info("issueRequireActionTaken:{}");
        try {
            String strObjectId = args[0];
            JF_LOGGER.info("strObjectId:{}", strObjectId);
            DomainObject issueBo = DomainObject.newInstance(context, strObjectId);
            //拿取采取的操作
            String strActionTaken = issueBo.getAttributeValue(context, "Action Taken");
            JF_LOGGER.info("strActionTaken:{}", strActionTaken);
            com.matrixone.apps.common.Issue issObj = new com.matrixone.apps.common.Issue();
            MapList relBusObjPageList=issObj.getAllAffectedItems(context,strObjectId,new StringList(DomainConstants.SELECT_ID));
            JF_LOGGER.info("getAllReportedAgainst:{}", relBusObjPageList);
            Map taskMap = (Map) relBusObjPageList.get(0);
            String taskId = UIUtil.getValue(taskMap, SELECT_ID);
            JF_LOGGER.info("taskId:{}", taskId);
            DomainObject domainObject = DomainObject.newInstance(context, taskId);
            String type = domainObject.getInfo(context, SELECT_TYPE);
            JF_LOGGER.info("type:{}", type);
            if (UIUtil.isNotNullAndNotEmpty(strActionTaken)) {
                iReturn = 0;
            } else {
                String fromType = domainObject.getInfo(context, "to[Subtask].from.type");
                JF_LOGGER.info("type:{}", type);
                if (JF_PLMConstants_mxJPO.TYPE_JF_ESOTask.equalsIgnoreCase(type)) {
                    iReturn = 1;
                } else if (JF_PLMConstants_mxJPO.TYPE_JF_ESOTask.equalsIgnoreCase(fromType)){
                    iReturn = 1;
                } else {
                    iReturn = 0;
                }
            }

        }catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
        JF_LOGGER.info("iReturn:{}", iReturn);
        if (iReturn != 0) {
            String strMess = EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", context.getLocale(), "emxComponents.IssueRequire.ActionTaken");
            emxContextUtilBase_mxJPO.mqlWarning(context, strMess);
        }
        return iReturn;
    }

    /**
     * WBS Table表显示ESO任务的职能属性的Range 值
     * @param context
     * @param args
     * @author LIUJR
     * @throws
     * @return java.util.Map
     * @date 2026/3/17 14:15
     * @description
     */
    public Map getFunctionReloadRange(Context context, String[] args) throws Exception {
        Map argsMap = (Map) JPO.unpackArgs(args);
        JF_LOGGER.info("argsMap:{}", argsMap);
        Map rowValues  = null ;
        if (argsMap.containsKey("rowValues")) {
            rowValues = (Map) argsMap.get("rowValues");
        }
        DomainObject domainObject = DomainObject.newInstance(context);
        String parentId = (String) rowValues.get("parentId");
        String objectId = (String) rowValues.get("objectId");
        if (UIUtil.isNotNullAndNotEmpty(parentId)) {
            domainObject.setId(parentId);
        } else {
            domainObject.setId(objectId);
            parentId = domainObject.getInfo(context, "to[Subtask].from.id");
            domainObject.setId(parentId);
        }
        Properties prop = NioJDUtils.readPageObject(context, "JFJDConfig");
        String JF_Function = prop.getProperty("ESOTask.JF_Function");
        String[] split = JF_Function.split(",");
        StringList stringList = StringList.create(split);
        String type = domainObject.getInfo(context, SELECT_TYPE);
        //如果父级为ESO任务  该选项就不能为CS,可以为CS_JIT,Structurem,Subsystem,EE
        if ("JF_ESOTask".equalsIgnoreCase(type)) {
            stringList.remove("CS");
        } else {
            //如果父级为任务，该选项需要判断子级有没有ESO任务，如果有，该任务只能为CS，如果没有，就可以全部
            domainObject.setId(objectId);
            StringList infoList = domainObject.getInfoList(context, "from[Subtask].to.type");
            if (infoList.toString().contains("JF_ESOTask")) {
                stringList = new StringList("CS");
            }
        }
        HashMap<Object, Object> res = new HashMap<>();
        res.put("RangeValues", stringList);
        res.put("RangeDisplayValue", stringList);
        return res;
    }

    /**
     * WBS Table表ESO任务的职能属性编辑后 触发模块更新Range值
     * @param context
     * @param args
     * @author LIUJR
     * @throws
     * @return java.util.Map
     * @date 2026/3/17 16:02
     * @description
     */
    public Map getWBSESOTackReloadRange(Context context, String[] args) throws Exception {
        HashMap<Object, Object> res = new HashMap<>();
        try {
            Map argsMap = (Map) JPO.unpackArgs(args);
            JF_LOGGER.info("！！！！！！！！！！！！argsMap:{}", argsMap);
            //拿取
            boolean isFlushFlag = false ;
            String name = EMPTY_STRING;
            Map columnValuesMap  = null;
            Map columnMap  = (Map) argsMap.get("columnMap");
            name = (String) columnMap.get(SELECT_NAME);
            if (argsMap.containsKey("columnValues")) {
                isFlushFlag = true ;
                columnValuesMap  = (Map) argsMap.get("columnValues");
            }
            JF_LOGGER.info("----------------name:{}", name);
            if (isFlushFlag) {
                String column = EMPTY_STRING;
                String column1 = EMPTY_STRING;
                switch (name) {
                    case "JF_Modules":
                        column = "functional";
                        break;
                    case "JF_Locations":
                        column = "JF_Modules";
                        break;
                    case "JF_Department":
                        column = "JF_Modules";
                        column1 = "JF_Locations";
                        break;
                }
                JF_LOGGER.info("---------------column:{}", column);
                String columnValue = (String) columnValuesMap.get(column);
                JF_LOGGER.info("---------------columnValue:{}", columnValue);
                String expression = DomainConstants.EMPTY_STRING;
                if ("JF_Modules".equalsIgnoreCase(name)) {
                    // 构建安全的 XPath 表达式（防止子串误匹配）
                    expression = String.format(
                            "/configurations/configuration[@id='ESOTask']/%s[" +
                                    "contains(concat(',', @JF_Function, ','), ',%s,')" +
                                    "]/@Value",
                            name, columnValue
                    );
                } else if ("JF_Locations".equalsIgnoreCase(name)) {

                    expression = String.format(
                            "/configurations/configuration[@id='ESOTask']/%s[" +
                                    "contains(concat(',', @JF_Modules, ','), ',%s,')" +
                                    "]/@Value",
                            name, columnValue
                    );
                } else if ("JF_Department".equalsIgnoreCase(name)) {
                    String column1Value = (String) columnValuesMap.get(column1);
                    expression = String.format(
                            "/configurations/configuration[@id='ESOTask']/%s[" +
                                    "contains(concat(',', @JF_Modules, ','), ',%s,') and " +
                                    "contains(concat(',', @JF_Locations, ','), ',%s,')" +
                                    "]/@Value",
                            name, columnValue,column1Value
                    );
                }
                JF_LOGGER.info("---------------expression:{}", expression);
                //开始拿取值
                Page pageAttributePopulation = new Page("SignTaskProperties_zh.xml");
                pageAttributePopulation.open(context);
                String strProperties = pageAttributePopulation.getContents(context);
                JF_LOGGER.info("strProperties:", strProperties);
                pageAttributePopulation.close(context);
                InputStream inputStream = new ByteArrayInputStream(strProperties.getBytes("UTF-8"));
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                DocumentBuilder builder = factory.newDocumentBuilder();
                Document document = builder.parse(inputStream);
                String value = findAttributeWithXPath(document, expression);
                JF_LOGGER.info("---------------value:{}", value);
                StringList stringList = StringList.create(value.split(","));
                res.put("RangeValues", stringList);
                res.put("RangeDisplayValue", stringList);
            } else {
                StringList ranges = FrameworkUtil.getRanges(context, name);
                for (int i = 0; i < ranges.size(); i++) {
                    if (UIUtil.isNullOrEmpty(ranges.get(i))) {
                        ranges.remove(i);
                    }
                }
                res.put("field_choices", ranges);
                res.put("field_display_choices", ranges);
            }
        } catch (Exception e){
            e.printStackTrace();
            throw e;
        }
        JF_LOGGER.info("---------------stringList:{}", res);
        return res;
    }

    public boolean getPhase23PlannedCompletionTimeAccess(Context context,String[] args) throws Exception{
        Boolean returnFlag = Boolean.FALSE;
        try {
            HashMap inputMap = (HashMap)JPO.unpackArgs(args);
            JF_LOGGER.info("inputMap{}",inputMap);
            String objectId = (String)inputMap.get("objectId");
            Map settingsMap = (Map) inputMap.get("SETTINGS");
            String strAdminType = (String) settingsMap.get("Admin Type");
            int index = strAdminType.indexOf("attribute_");
            String attrName = strAdminType.substring(index + "attribute_".length());
            if (UIUtil.isNullOrEmpty(objectId)) {
                objectId = (String)inputMap.get("parentOID");
            }
            DomainObject psObject = DomainObject.newInstance(context, objectId);
            Boolean noDVFlag = projectSpaceHasDVFlag(context, psObject);
            if (noDVFlag) {
                if ("JF_Phase2_3PlannedCompletionTime".equalsIgnoreCase(attrName)) {
                    returnFlag = Boolean.TRUE;
                }
            } else {
                if (!"JF_Phase2_3PlannedCompletionTime".equalsIgnoreCase(attrName)) {
                    returnFlag = Boolean.TRUE;
                }
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
        return returnFlag;
    }

    /**
     * 判断有无DV项目
     * @param context
     * @param psObject
     * @author LIUJR
     * @throws
     * @return int
     * @date 2026/5/18 15:37
     * @description
     */
    public static Boolean projectSpaceHasDVFlag(Context context, DomainObject psObject) throws Exception{
        MapList mapList = psObject.getRelatedObjects(
                context,
                DomainRelationship.RELATIONSHIP_SUBTASK,
                "Phase",
                JF_Util_mxJPO.basicBolistSel(),
                JF_Util_mxJPO.basicRellistSel(),
                Boolean.FALSE,
                Boolean.TRUE,
                (short) 1,
                "",
                "",
                0
        );
        int num = 0;
        Boolean hasFlag = Boolean.FALSE;
        for (int i = 0; i < mapList.size(); i++) {
            Map map = (Map) mapList.get(i);
            String name = UIUtil.getValue(map, SELECT_NAME);
            if (name.startsWith("P-2+3") || name.startsWith("P-1") || name.startsWith("P-2") || name.startsWith("P-3") || name.startsWith("P-4")
                    || name.startsWith("P-5")) {
                num ++;
            }
            if (name.startsWith("P-2+3")) {
                hasFlag = Boolean.TRUE;
            }
        }
        //当阶段为4个并且有p-2+3
        if (num == 4 && hasFlag) {
            return Boolean.TRUE;
        } else {
            return Boolean.FALSE;
        }
    }

    /**
    * 1. ESO记录表保存后统一处理本次修改过的签发记录；
    *     1. 从表格保存的XMLDoc中读取本次修改行和修改列；
    *     2. 只处理审核次数、审核状态、备注三类修改，其他字段修改不处理签发表文档；
    *     3. 同一签发记录同时修改审核次数、审核状态、备注时，只按签发记录id去重处理一次；
    *     4. 签发记录没有签发表文档时，调用已有创建逻辑创建签发表文档；
    *     5. 签发记录已有签发表文档时，删除原文档对象后重新生成签发表文档；
    *     6. 删除和重新生成放在同一事务中，生成失败时回滚原文档删除；
    * @param context
	* @param args
    * @author LIUJR
    * @throws
    * @return java.util.Map
    * @date 2026/7/28
    * @description
    */
    @com.matrixone.apps.framework.ui.PostProcessCallable
    public Map postProcessSaveESOReviewRecord(Context context, String[] args) throws Exception {
        JF_LOGGER.info("!!!!!!!!!postProcessSaveESOReviewRecord!!!!!!!!!!!!!!");
        Map returnMap = new HashMap();
        //20260728 update by ljr 使用Structure Browser原生refresh完成保存状态清理和表格刷新，避免execScript直接刷新后仍提示存在未保存修改；
        returnMap.put("Action", "refresh");
        try {
            // 读取表格保存后的参数，post处理只依据本次保存XML中的修改点。
            Map programMap = (Map) JPO.unpackArgs(args);
            JF_LOGGER.info("programMap：{}", programMap);
            String saveKey = EMPTY_STRING;
            Map requestMap = (Map) programMap.get("requestMap");
            if (requestMap != null) {
                saveKey = UIUtil.getValue(requestMap, STRING_OBJECTID);
            }
            JF_LOGGER.info("saveKey：{}", saveKey);
            if (UIUtil.isNullOrEmpty(saveKey)) {
                saveKey = UIUtil.getValue(programMap, STRING_OBJECTID);
            }
            JF_LOGGER.info("saveKey：{}", saveKey);
            if (UIUtil.isNotNullAndNotEmpty(saveKey)) {
                long currentTime = System.currentTimeMillis();
                Long lastSaveTime = ESO_REVIEW_SAVE_DEBOUNCE_MAP.get(saveKey);
                if (lastSaveTime != null && currentTime - lastSaveTime < ESO_REVIEW_SAVE_DEBOUNCE_TIME) {
                    JF_LOGGER.info("postProcessSaveESOReviewRecord skip repeat save:{}", saveKey);
                    return returnMap;
                }
                ESO_REVIEW_SAVE_DEBOUNCE_MAP.put(saveKey, currentTime);
            }

            com.matrixone.jdom.Document xmlDoc = (com.matrixone.jdom.Document) programMap.get("XMLDoc");
            JF_LOGGER.info("xmlDoc：{}", xmlDoc);
            Set<String> changedReviewSet = new LinkedHashSet<String>();
            StringList updateColumnList = StringList.create(ESO_REVIEW_COLUMN_REVIEW_COUNT,
                    ESO_REVIEW_COLUMN_PHASE_STATE,
                    ESO_REVIEW_COLUMN_REMARK,
                    ESO_REVIEW_COLUMN_DESCRIPTION);
            if (xmlDoc != null) {
                // 取本次保存的所有修改对象节点。
                com.matrixone.jdom.Element rootElement = xmlDoc.getRootElement();
                List objectElementList = rootElement.getChildren("object");
                for (int i = 0; i < objectElementList.size(); i++) {
                    com.matrixone.jdom.Element objectElement = (com.matrixone.jdom.Element) objectElementList.get(i);
                    String esoReviewId = objectElement.getAttributeValue("objectId");
                    if (UIUtil.isNullOrEmpty(esoReviewId)) {
                        continue;
                    }
                    // 遍历当前签发记录本次修改的列，只要三类目标字段有修改就加入处理集合。
                    List columnElementList = objectElement.getChildren("column");
                    for (int j = 0; j < columnElementList.size(); j++) {
                        com.matrixone.jdom.Element columnElement = (com.matrixone.jdom.Element) columnElementList.get(j);
                        String columnName = columnElement.getAttributeValue("name");
                        String edited = columnElement.getAttributeValue("edited");
                        String changed = columnElement.getAttributeValue("changed");
                        String modified = columnElement.getAttributeValue("modified");
                        String columnStatus = columnElement.getAttributeValue("status");
                        String oldValue = columnElement.getAttributeValue("oldValue");
                        String newValue = columnElement.getAttributeValue("newValue");
                        boolean isColumnChanged = "true".equalsIgnoreCase(edited)
                                || "true".equalsIgnoreCase(changed)
                                || "true".equalsIgnoreCase(modified)
                                || "changed".equalsIgnoreCase(columnStatus)
                                || (oldValue != null && newValue != null && !oldValue.equals(newValue));
                        if (updateColumnList.contains(columnName) && isColumnChanged) {
                            changedReviewSet.add(esoReviewId);
                            break;
                        }
                    }
                }
            }
            JF_LOGGER.info("changedReviewSet：{}", changedReviewSet);

            // 本次保存没有修改审核次数、审核状态和备注时，不创建或重新生成签发表文档。
            if (changedReviewSet == null || changedReviewSet.isEmpty()) {
                return returnMap;
            }

            // 按签发记录id去重后分别处理签发表文档，同一行改多个字段只处理一次。
            for (String esoReviewId : changedReviewSet) {
                boolean transactionStarted = false;
                boolean deleteContextPushed = false;
                try {
                    DomainObject esoReviewObj = DomainObject.newInstance(context, esoReviewId);
                    String reviewTaskId = esoReviewObj.getInfo(context, "to[JFESOTask2ESOReview].from.id");
                    String documentId = esoReviewObj.getInfo(context, "from[JFESOReview2Document].to.id");
                    if (UIUtil.isNullOrEmpty(reviewTaskId)) {
                        continue;
                    }

                    String projectId = JF_PublicProjectQuery_mxJPO.getProjectIdByTaskId(context, reviewTaskId);
                    String personId = esoReviewObj.getInfo(context, "from[JFESOReview2Person].to.id");
                    ContextUtil.startTransaction(context, true);
                    transactionStarted = true;

                    //20260728 update by ljr 审核次数、审核状态或备注变化时，已有签发表文档需要删除后重新生成；
                    if (UIUtil.isNotNullAndNotEmpty(documentId)) {
                        try {
                            ContextUtil.pushContext(context);
                            deleteContextPushed = true;
                            DomainObject.deleteObjects(context, new String[]{documentId});
                            JF_LOGGER.info("签发表原文档已删除, esoReviewId:{}, documentId:{}", esoReviewId, documentId);
                        } finally {
                            if (deleteContextPushed) {
                                ContextUtil.popContext(context);
                                deleteContextPushed = false;
                            }
                        }
                    }

                    // 没有文档时直接创建；已有文档时复用相同方法完整重建签发表。
                    createESOSignTableDocument(context, new String[]{esoReviewId, reviewTaskId, projectId, personId});
                    String newDocumentId = esoReviewObj.getInfo(context, "from[JFESOReview2Document].to.id");
                    MapList newDocumentFileList = UIUtil.isNullOrEmpty(newDocumentId)
                            ? new MapList()
                            : JF_PublicMethodClass_mxJPO.getDocumentFiles(context, newDocumentId);
                    ContextUtil.commitTransaction(context);
                    transactionStarted = false;
                    JF_LOGGER.info("签发表文档重新生成成功, esoReviewId:{}, newDocumentId:{}", esoReviewId, newDocumentId);
                } catch (Exception reviewException) {
                    if (transactionStarted) {
                        ContextUtil.abortTransaction(context);
                    }
                    JF_LOGGER.error("---postProcessSaveESOReviewRecord update one review error---", reviewException);
                } finally {
                    if (deleteContextPushed) {
                        ContextUtil.popContext(context);
                    }
                }
            }
        } catch (Exception e) {
            JF_LOGGER.error("---postProcessSaveESOReviewRecord---error", e);
        }
        return returnMap;
    }

    /**
    * 1. 项目空间WBS界面限制ESO相关任务的列编辑权限；
    *     1. Name列：ESO模块任务和ESO下级子任务均不可编辑；
    *     2. Description列：ESO视图中ESO模块任务可编辑，ESO模块任务下级子任务不可编辑；
    *     3. 保留原WBS列权限逻辑，原逻辑中已包含项目权限、任务状态等编辑控制；
    *     4. 非ESO模块任务和普通任务满足原WBS列权限时可编辑；
    * @param context
	* @param args
    * @author LIUJR
    * @throws
    * @return matrix.util.StringList
    * @date 2026/7/6 18:30
    * @description
    */
    public StringList getWBSESONameDescriptionEditAccess(Context context, String[] args) throws Exception {
        StringList result = new StringList();
        Map paramMap = JPO.unpackArgs(args);
        try {
            MapList objectList = (MapList) paramMap.get("objectList");
            Map columnMap = (Map) paramMap.get("columnMap");
            JF_LOGGER.info("columnMap:{}", columnMap);
            String colName = EMPTY_STRING;
            if (columnMap.containsKey("colAttrMap")) {
                Map colAttrMap = (Map) columnMap.get("colAttrMap");
                colName = UIUtil.getValue(colAttrMap, DomainConstants.SELECT_NAME);
            } else {
                colName = UIUtil.getValue(columnMap, DomainConstants.SELECT_NAME);
            }
            StringList oldEditAccessList = JPO.invoke(context, "emxTask", null, "isTaskNameCellEditable", args, StringList.class);

            // 一次性定义需要查询的任务信息，避免循环中重复拼接select。
            StringList taskSelectList = new StringList();
            taskSelectList.add(DomainConstants.SELECT_TYPE);
            taskSelectList.add("to[Subtask].from.type");
            DomainObject taskObj = DomainObject.newInstance(context);
            for (int i = 0; i < objectList.size(); i++) {
                Map taskMap = (Map) objectList.get(i);
                String taskId = UIUtil.getValue(taskMap, DomainConstants.SELECT_ID);
                String taskName = UIUtil.getValue(taskMap, SELECT_NAME);
                String oldEditAccess = "true";
                // 判断当前行是否为ESO模块任务，或是否挂在ESO模块任务下面。
                taskObj.setId(taskId);
                if (oldEditAccessList != null && oldEditAccessList.size() > i) {
                    oldEditAccess = (String) oldEditAccessList.get(i);
                }
                if(taskObj.isKindOf(context,"Project Space")){
                    result.add("false");
                    continue;
                }
                // 保留原始WBS列权限逻辑，已完成、归档、被动任务等原本不可编辑的场景继续不可编辑。
                if (!"true".equalsIgnoreCase(oldEditAccess)) {
                    result.add("false");
                    continue;
                }
                if (UIUtil.isNullOrEmpty(taskId)) {
                    result.add(oldEditAccess);
                    continue;
                }
                Map taskInfo = taskObj.getInfo(context, taskSelectList);
                String taskType = UIUtil.getValue(taskInfo, DomainConstants.SELECT_TYPE);

                String parentTaskType = UIUtil.getValue(taskInfo, "to[Subtask].from.type");
                boolean isESOModuleTask = JF_PLMConstants_mxJPO.TYPE_JF_ESOTask.equalsIgnoreCase(taskType);
                boolean isESOSubTask = JF_PLMConstants_mxJPO.TYPE_JF_ESOTask.equalsIgnoreCase(parentTaskType);
                // Name列：ESO模块任务和ESO下级子任务不允许修改名称。
                if ("Name".equalsIgnoreCase(colName)) {
                    result.add((isESOModuleTask || isESOSubTask) ? "false" : "true");
                    continue;
                }

                // Description列：ESO模块任务允许修改说明，ESO模块任务下级子任务不允许修改说明。
                if ("Description".equalsIgnoreCase(colName)) {
                    result.add(isESOSubTask ? (isESOModuleTask ? "true" : "false") : "true");
                    continue;
                }
                result.add("true");
            }
        } catch (Exception e) {
            JF_LOGGER.error("---getWBSESONameDescriptionEditAccess---error", e);
            throw e;
        }
        return result;
    }

    /**
    * 1. WBS界面ESO任务批量完成按钮显示权限：
    *     1. 当前用户必须拥有ESO审核员系统角色；
    *     2. 当前上下文对象可能是项目、阶段、任务或ESO任务，先反查所属项目；
    *     3. 当前用户必须是该项目成员，避免只有系统角色但不属于当前项目时显示按钮；
    * @param context
	* @param args
    * @author LIUJR
    * @throws
    * @return java.lang.Boolean
    * @date 2026/7/6 20:10
    * @description
    */
    public Boolean getESOTaskBatchCompleteAccess(Context context, String[] args) throws Exception {
        Boolean flag = Boolean.FALSE;
        try {
            Map paramMap = JPO.unpackArgs(args);
            String objectId = UIUtil.getValue(paramMap, "objectId");
            if (UIUtil.isNullOrEmpty(objectId)) {
                objectId = UIUtil.getValue(paramMap, "parentOID");
            }

            // 先判断当前用户是否具备ESO审核员角色，不满足时不继续查询项目成员。
            Vector assignments = PersonUtil.getAssignments(context, context.getUser());
            if (!assignments.contains(ESO_ADMIN_ROLE) || UIUtil.isNullOrEmpty(objectId)) {
                return flag;
            }

            // 根据当前上下文对象获取所属项目，项目对象直接使用自身id，任务对象通过Project Access反查项目id。
            DomainObject contextObject = DomainObject.newInstance(context, objectId);
            StringList selectList = new StringList();
            selectList.add(DomainConstants.SELECT_TYPE);
            selectList.add("to[" + DomainConstants.RELATIONSHIP_PROJECT_ACCESS_KEY + "].from.from[" + DomainConstants.RELATIONSHIP_PROJECT_ACCESS_LIST + "].to.id");
            Map contextInfo = contextObject.getInfo(context, selectList);
            String contextType = UIUtil.getValue(contextInfo, DomainConstants.SELECT_TYPE);
            String projectId = DomainConstants.TYPE_PROJECT_SPACE.equalsIgnoreCase(contextType) ? objectId : UIUtil.getValue(contextInfo, "to[" + DomainConstants.RELATIONSHIP_PROJECT_ACCESS_KEY + "].from.from[" + DomainConstants.RELATIONSHIP_PROJECT_ACCESS_LIST + "].to.id");
            if (UIUtil.isNullOrEmpty(projectId)) {
                return flag;
            }

            // 按项目成员关系确认当前用户属于当前项目，按钮入口只对本项目ESO审核员显示。
            DomainObject projectObject = DomainObject.newInstance(context, projectId);
            StringList projectMemberList = projectObject.getInfoList(context, "from[" + DomainRelationship.RELATIONSHIP_MEMBER + "].to.name");
            if (projectMemberList != null && projectMemberList.contains(context.getUser())) {
                flag = Boolean.TRUE;
            }
        } catch (Exception e) {
            JF_LOGGER.error("---getESOTaskBatchCompleteAccess---error", e);
            flag = Boolean.FALSE;
        }
        return flag;
    }

    /**
    * WBS状态列加载全局状态范围：
    * 1. 复用项目管理原有状态范围和国际化显示；
    * 2. 全局Range同时用于顶部批量更新，必须保留Complete供普通项目任务使用；
    * 3. ESO任务的Complete选项由单行Reload Function按任务类型移除；
    * @param context
	* @param args
    * @author LIUJR
    * @throws
    * @return java.util.Map
    * @date 2026/7/6 20:15
    * @description
    */
    public Map getWBSESOStateRange(Context context, String[] args) throws Exception {
        //20260814 update by ljr 全局Range恢复OOTB全部状态，避免普通项目任务无法通过成熟度列完成。
        return JPO.invoke(context, "emxTask", null, "getTaskManagementStateRange", args, Map.class);
    }

    /**
    * WBS状态列刷新单行状态范围：
    * 1. 先调用项目管理原有单行状态刷新方法；
    * 2. 当前行是JF_ESOTask或直接父级为JF_ESOTask时移除Complete；
    * 3. 其他项目任务保留OOTB全部状态范围；
    * @param context
	* @param args
    * @author LIUJR
    * @throws
    * @return java.util.Map
    * @date 2026/7/6 20:18
    * @description
    */
    public Map getWBSESOStates(Context context, String[] args) throws Exception {
        Map stateMap = JPO.invoke(context, "emxTask", null, "getTaskManagementStates", args, Map.class);
        try {
            //20260814 update by ljr 根据当前行任务及其直接父级类型区分ESO任务范围。
            Map argsMap = JPO.unpackArgs(args);
            Map rowValues = argsMap.get("rowValues") instanceof Map ? (Map) argsMap.get("rowValues") : null;
            String objectId = rowValues == null ? DomainConstants.EMPTY_STRING : UIUtil.getValue(rowValues, "objectId");
            if (UIUtil.isNullOrEmpty(objectId)) {
                objectId = UIUtil.getValue(argsMap, "objectId");
            }
            if (!isESOTaskOrSubTask(context, objectId)) {
                return stateMap;
            }

            // Reload Function返回的key为RangeValues/RangeDisplayValue，不能使用Range Function的field_choices。
            StringList stateList = (StringList) stateMap.get("RangeValues");
            StringList stateDisplayList = (StringList) stateMap.get("RangeDisplayValue");
            if (stateList != null) {
                //20260814 update by ljr 仅ESO模块任务及其直接子任务移除Complete，其他项目任务保留完整Range。
                for (int i = stateList.size() - 1; i >= 0; i--) {
                    String state = (String) stateList.get(i);
                    if (ESO_TASK_STATE_COMPLETE.equalsIgnoreCase(state)) {
                        stateList.remove(i);
                        if (stateDisplayList != null && stateDisplayList.size() > i) {
                            stateDisplayList.remove(i);
                        }
                    }
                }
            }
        } catch (Exception e) {
            JF_LOGGER.error("---getWBSESOStates---error", e);
            throw e;
        }
        JF_LOGGER.error("getWBSESOStates:{}", stateMap);
        return stateMap;
    }

    /**
     * WBS成熟度列状态更新
     * 1. 普通项目任务继续调用OOTB emxTask.updateState；
     * 2. JF_ESOTask及其直接子任务禁止通过成熟度列更新到Complete；
     * 3. ESO任务需要通过批量完成任务按钮进入Complete。
     *
     * @param context 上下文
     * @param args Table状态列更新参数
     * @return void
     * @throws Exception 状态检查或更新失败时抛出异常
     * @author LIUJR
     * @date 2026/8/14
     */
    public void updateWBSESOState(Context context, String[] args) throws Exception {
        Map programMap = JPO.unpackArgs(args);
        Map paramMap = programMap.get("paramMap") instanceof Map ? (Map) programMap.get("paramMap") : null;
        String objectId = paramMap == null ? DomainConstants.EMPTY_STRING : UIUtil.getValue(paramMap, "objectId");
        String newState = paramMap == null ? DomainConstants.EMPTY_STRING : UIUtil.getValue(paramMap, "New Value");
        if (ESO_TASK_STATE_COMPLETE.equalsIgnoreCase(newState) && isESOTaskOrSubTask(context, objectId)) {
            String message = EnoviaResourceBundle.getProperty(
                    context,
                    "emxProgramCentralStringResource",
                    context.getLocale(),
                    "emxProgramCentral.ESOTaskStateComplete.UseBatchComplete");
            throw new FrameworkException(message);
        }
        JPO.invoke(context, "emxTask", null, "updateState", args, null);
    }

    /**
     * 判断任务是否属于ESO任务范围
     *
     * @param context 上下文
     * @param objectId 任务对象ID
     * @return boolean 当前对象为JF_ESOTask或直接父级为JF_ESOTask时返回true
     * @throws Exception 获取任务类型失败时抛出异常
     * @author LIUJR
     * @date 2026/8/14
     */
    private boolean isESOTaskOrSubTask(Context context, String objectId) throws Exception {
        if (UIUtil.isNullOrEmpty(objectId)) {
            return false;
        }
        String parentTaskTypeSelect = "to[Subtask].from.type";
        StringList taskSelectList = StringList.create(
                DomainConstants.SELECT_TYPE,
                parentTaskTypeSelect);
        Map taskInfo = DomainObject.newInstance(context, objectId).getInfo(context, taskSelectList);
        String taskType = UIUtil.getValue(taskInfo, DomainConstants.SELECT_TYPE);
        String parentTaskType = UIUtil.getValue(taskInfo, parentTaskTypeSelect);
        return JF_PLMConstants_mxJPO.TYPE_JF_ESOTask.equalsIgnoreCase(taskType)
                || JF_PLMConstants_mxJPO.TYPE_JF_ESOTask.equalsIgnoreCase(parentTaskType);
    }

    /**
    * 1. WBS界面批量将审批中的叶子节点任务提升到已完成：
    *     1. 从页面选中行中获取任务id，统一校验后再执行生命周期提升，避免部分成功部分失败；
    *     2. 选中对象必须是叶子节点Task任务，阶段、项目、父级任务等非叶子节点不能提升；
    *     3. 选中任务必须处于Review状态，且当前用户必须是任务所属项目的ESO审核员；
    *     4. 校验通过后逐个调用OOTB emxTask.updateState更新到Complete，复用表格成熟度变更的任务联动逻辑；
    *     5. 子任务完成后，从直接父级开始逐级检查，全部直接子任务均已完成时更新父级到Complete并继续检查上一级；
    *     6. 任意提升失败时回滚本次所有状态修改，并返回统一错误提示；
    * @param context
	* @param args
    * @author LIUJR
    * @throws
    * @return java.util.Map
    * @date 2026/7/6 20:25
    * @description
    */
    public Map completeESOSubTaskByBatch(Context context, String[] args) throws Exception {
        Map resultMap = new HashMap();
        String resultMessage = DomainConstants.EMPTY_STRING;
        StringList invalidTaskNameList = new StringList();
        StringList completeTaskIdList = new StringList();
        Set<String> projectIdSet = new LinkedHashSet<String>();
        Set<String> parentTaskIdSet = new LinkedHashSet<String>();
        boolean isTransaction = false;
        try {
            Map requestMap = JPO.unpackArgs(args);
            Object listObj = requestMap.get("list");
            StringList selectTaskIdList = new StringList();
            if (listObj instanceof String[]) {
                String[] selectArray = (String[]) listObj;
                for (int i = 0; i < selectArray.length; i++) {
                    if (UIUtil.isNotNullAndNotEmpty(selectArray[i])) {
                        selectTaskIdList.add(selectArray[i]);
                    }
                }
            } else if (listObj instanceof StringList) {
                selectTaskIdList.addAll((StringList) listObj);
            } else if (listObj instanceof List) {
                selectTaskIdList.addAll((List) listObj);
            } else if (listObj instanceof String && UIUtil.isNotNullAndNotEmpty((String) listObj)) {
                selectTaskIdList.add((String) listObj);
            }

            // 未选择任何任务时直接返回提示，不进入后续状态处理。
            if (selectTaskIdList.isEmpty()) {
                resultMap.put("code", "1");
                resultMap.put("message", EnoviaResourceBundle.getProperty(context, "emxProgramCentralStringResource", context.getLocale(), "emxProgramCentral.ESOTaskBatchComplete.NoSelect"));
                return resultMap;
            }

            // 当前用户必须具备ESO审核员角色，否则不能批量完成审批中任务。
            Vector assignments = PersonUtil.getAssignments(context, context.getUser());
            if (!assignments.contains(ESO_ADMIN_ROLE)) {
                resultMap.put("code", "1");
                resultMap.put("message", EnoviaResourceBundle.getProperty(context, "emxProgramCentralStringResource", context.getLocale(), "emxProgramCentral.ESOTaskBatchComplete.NoAccess"));
                return resultMap;
            }

            StringList taskSelectList = new StringList();
            taskSelectList.add(DomainConstants.SELECT_ID);
            taskSelectList.add(DomainConstants.SELECT_TYPE);
            taskSelectList.add(DomainConstants.SELECT_NAME);
            taskSelectList.add(DomainConstants.SELECT_CURRENT);
            taskSelectList.add("from[Subtask]");
            taskSelectList.add("to[Subtask].from.id");
            taskSelectList.add("to[" + DomainConstants.RELATIONSHIP_PROJECT_ACCESS_KEY + "].from.from[" + DomainConstants.RELATIONSHIP_PROJECT_ACCESS_LIST + "].to.id");

            DomainObject taskObject = DomainObject.newInstance(context);
            for (int i = 0; i < selectTaskIdList.size(); i++) {
                String taskId = (String) selectTaskIdList.get(i);
                if (UIUtil.isNullOrEmpty(taskId)) {
                    continue;
                }

                // 查询当前选中任务、是否存在子任务和所属项目，用于统一校验。
                taskObject.setId(taskId);
                Map taskInfo = taskObject.getInfo(context, taskSelectList);
                String taskName = UIUtil.getValue(taskInfo, DomainConstants.SELECT_NAME);
                String taskType = UIUtil.getValue(taskInfo, DomainConstants.SELECT_TYPE);
                String taskCurrent = UIUtil.getValue(taskInfo, DomainConstants.SELECT_CURRENT);
                String hasSubTask = UIUtil.getValue(taskInfo, "from[Subtask]");
                String parentTaskId = UIUtil.getValue(taskInfo, "to[Subtask].from.id");
                String projectId = UIUtil.getValue(taskInfo, "to[" + DomainConstants.RELATIONSHIP_PROJECT_ACCESS_KEY + "].from.from[" + DomainConstants.RELATIONSHIP_PROJECT_ACCESS_LIST + "].to.id");

                // 只能选择审批中的叶子节点Task任务，阶段、项目、父级任务和非审批中任务都统一汇总提示。
                if (!DomainConstants.TYPE_TASK.equalsIgnoreCase(taskType)
                        || "TRUE".equalsIgnoreCase(hasSubTask)
                        || !ESO_TASK_STATE_REVIEW.equalsIgnoreCase(taskCurrent)) {
                    invalidTaskNameList.add(taskName);
                    continue;
                }

                // 任务必须能反查到所属项目，后续要按项目成员校验ESO审核员权限。
                if (UIUtil.isNullOrEmpty(projectId)) {
                    invalidTaskNameList.add(taskName);
                    continue;
                }

                completeTaskIdList.add(taskId);
                projectIdSet.add(projectId);
                if (UIUtil.isNotNullAndNotEmpty(parentTaskId)) {
                    parentTaskIdSet.add(parentTaskId);
                }
            }

            // 只要存在不符合条件的选中任务，本次不做任何状态写入。
            if (!invalidTaskNameList.isEmpty()) {
                String mess = EnoviaResourceBundle.getProperty(context, "emxProgramCentralStringResource", context.getLocale(), "emxProgramCentral.ESOTaskBatchComplete.InvalidTask");
                resultMap.put("code", "1");
                resultMap.put("message", mess.replace("taskName", invalidTaskNameList.join(",")));
                return resultMap;
            }

            // 按选中任务所属项目确认当前用户是项目成员，避免跨项目或非项目ESO审核员绕过入口。
            DomainObject projectObject = DomainObject.newInstance(context);
            for (String projectId : projectIdSet) {
                projectObject.setId(projectId);
                StringList projectMemberList = projectObject.getInfoList(context, "from[" + DomainRelationship.RELATIONSHIP_MEMBER + "].to.name");
                if (projectMemberList == null || !projectMemberList.contains(context.getUser())) {
                    resultMap.put("code", "1");
                    resultMap.put("message", EnoviaResourceBundle.getProperty(context, "emxProgramCentralStringResource", context.getLocale(), "emxProgramCentral.ESOTaskBatchComplete.NoAccess"));
                    return resultMap;
                }
            }

            ContextUtil.startTransaction(context, true);
            isTransaction = true;

            // 校验全部通过后，统一把选中的叶子节点任务按生命周期更新到Complete。
            for (int i = 0; i < completeTaskIdList.size(); i++) {
                String taskId = (String) completeTaskIdList.get(i);
                //20260814 update by ljr 调用成熟度列相同的OOTB updateState，保留任务完成度、日期及父级汇总联动逻辑。
                Map updateParamMap = new HashMap();
                updateParamMap.put("objectId", taskId);
                updateParamMap.put("New Value", ESO_TASK_STATE_COMPLETE);
                updateParamMap.put("Old Value", ESO_TASK_STATE_REVIEW);
                updateParamMap.put("Old value", ESO_TASK_STATE_REVIEW);

                Map updateProgramMap = new HashMap();
                updateProgramMap.put("paramMap", updateParamMap);
                updateProgramMap.put("requestMap", requestMap);
                JPO.invoke(context, "emxTask", null, "updateState", JPO.packArgs(updateProgramMap), null);
            }

            //20260819 update by ljr 子任务完成后按父级集合逐层汇总，直到没有更上一级需要检查。
            completeParentTasksByLevel(context, parentTaskIdSet, requestMap);

            ContextUtil.commitTransaction(context);
            isTransaction = false;
            resultMap.put("code", "0");
            resultMap.put("message", EnoviaResourceBundle.getProperty(context, "emxProgramCentralStringResource", context.getLocale(), "emxProgramCentral.ESOTaskBatchComplete.Success"));
        } catch (Exception e) {
            JF_LOGGER.error("---completeESOSubTaskByBatch---error", e);
            if (isTransaction) {
                ContextUtil.abortTransaction(context);
            }
            resultMessage = EnoviaResourceBundle.getProperty(context, "emxProgramCentralStringResource", context.getLocale(), "emxProgramCentral.ESOTaskBatchComplete.Failed");
            resultMap.put("code", "1");
            resultMap.put("message", resultMessage + e.getMessage());
        }
        return resultMap;
    }

    /**
     * 从选中任务的直接父级开始，逐级完成全部子任务均已完成的父任务
     *
     * @param context 上下文
     * @param parentTaskIdSet 待检查的直接父任务ID集合
     * @param requestMap 批量完成请求参数
     * @return void
     * @throws Exception 查询或更新任务状态失败时抛出异常
     * @author LIUJR
     * @date 2026/8/19
     */
    private void completeParentTasksByLevel(Context context, Set<String> parentTaskIdSet, Map requestMap) throws Exception {
        if (parentTaskIdSet.size() ==0 ) {
            return;
        }
        Set<String> pendingParentTaskIdSet = new LinkedHashSet<String>(parentTaskIdSet);
        DomainObject parentTaskObject = DomainObject.newInstance(context);
        String parentTaskIdSelect = "to[Subtask].from.id";
        StringList parentSelectList = StringList.create(
                DomainConstants.SELECT_CURRENT,
                parentTaskIdSelect);
        Set<String> parentTaskSet = new HashSet<String>();
        for (String parentTaskId : pendingParentTaskIdSet) {
            //20260819 update by ljr 同批次可能先检查CS再完成Subsystem，允许下级完成后重新检查已处理过的上级节点。
            if (UIUtil.isNullOrEmpty(parentTaskId)) {
                continue;
            }
            parentTaskObject.setId(parentTaskId);
            Map parentTaskInfo = parentTaskObject.getInfo(context, parentSelectList);
            String parentTaskCurrent = UIUtil.getValue(parentTaskInfo, DomainConstants.SELECT_CURRENT);
            //20260819 update by ljr 父级为非完成状态时才检查子任务并提升；已完成时不重复提升，但仍继续统计上一级父任务。
            if (ESO_TASK_STATE_COMPLETE.equalsIgnoreCase(parentTaskCurrent)) {
                continue;
            }
            StringList childStateList = parentTaskObject.getInfoList(context, "from[Subtask].to.current");
            boolean allChildrenComplete = childStateList != null && !childStateList.isEmpty();
            for (int i = 0; allChildrenComplete && i < childStateList.size(); i++) {
                if (!ESO_TASK_STATE_COMPLETE.equalsIgnoreCase((String) childStateList.get(i))) {
                    allChildrenComplete = false;
                    break;
                }
            }
            if (!allChildrenComplete) {
                continue;
            }

            Map parentUpdateParamMap = new HashMap();
            parentUpdateParamMap.put("objectId", parentTaskId);
            parentUpdateParamMap.put("New Value", ESO_TASK_STATE_COMPLETE);
            parentUpdateParamMap.put("Old Value", parentTaskCurrent);
            parentUpdateParamMap.put("Old value", parentTaskCurrent);

            Map parentUpdateProgramMap = new HashMap();
            parentUpdateProgramMap.put("paramMap", parentUpdateParamMap);
            parentUpdateProgramMap.put("requestMap", requestMap);
            JPO.invoke(context, "emxTask", null, "updateState", JPO.packArgs(parentUpdateProgramMap), null);
            String upperParentTaskId = UIUtil.getValue(parentTaskInfo, parentTaskIdSelect);
            if (UIUtil.isNotNullAndNotEmpty(upperParentTaskId)) {
                parentTaskSet.add(upperParentTaskId);
            }
        }
        completeParentTasksByLevel(context, parentTaskSet, requestMap);

    }

    /**
    * 1. WBS界面Description列编辑权限入口：
    *     1. Name列和Description列拆分不同Edit Access Function，避免页面缓存同名方法结果导致Description列不触发；
    *     2. 该方法单独作为Description列入口，内部复用已有ESO任务名称/说明权限判断；
    *     3. ESO模块任务的Description允许修改，ESO模块任务下级子任务的Description不允许修改，其他任务沿用原WBS列权限；
    * @param context
	* @param args
    * @author LIUJR
    * @throws
    * @return matrix.util.StringList
    * @date 2026/7/7 16:30
    * @description
    */
    public StringList getWBSESODescriptionEditAccess(Context context, String[] args) throws Exception {
        return getWBSESONameDescriptionEditAccess(context, args);
    }

    /**
    * 1. 项目任务从审批中提升到已完成时，限制ESO任务状态流转权限；
    *     1. 当前任务是ESO模块任务时，需要当前用户拥有ESO审核员角色，否则阻断提升；
    *     2. 当前任务的父级是ESO模块任务时，认为当前任务是ESO子任务，需要当前用户拥有ESO审核员角色，否则阻断提升；
    *     3. ESO子任务存在未关闭的问题时阻断提升，进入审批中时不执行该校验；
    *     4. 普通项目任务不属于ESO任务范围，直接放行，不影响原有项目任务生命周期逻辑；
    * @param context
	* @param args
    * @author LIUJR
    * @throws
    * @return int
    * @date 2026/7/7 18:30
    * @description
    */
    public int checkESOTaskReviewPromoteToComplete(Context context, String[] args) throws Exception {
        try {
            String objectId = args != null && args.length > 0 ? args[0] : DomainConstants.EMPTY_STRING;
            if (UIUtil.isNullOrEmpty(objectId)) {
                return 0;
            }

            // 一次查询取得任务判断及问题查询所需信息，避免重复访问任务对象。
            StringList taskSelectList = StringList.create(
                    DomainConstants.SELECT_TYPE,
                    DomainConstants.SELECT_NAME,
                    DomainConstants.SELECT_PHYSICAL_ID,
                    "to[Subtask].from.type");
            DomainObject taskObject = DomainObject.newInstance(context, objectId);
            Map taskMap = taskObject.getInfo(context, taskSelectList);
            String taskType = UIUtil.getValue(taskMap, DomainConstants.SELECT_TYPE);
            String taskName = UIUtil.getValue(taskMap, DomainConstants.SELECT_NAME);
            String taskPhysicalId = UIUtil.getValue(taskMap, DomainConstants.SELECT_PHYSICAL_ID);
            String parentTaskType = UIUtil.getValue(taskMap, "to[Subtask].from.type");

            // 不是ESO模块任务，也不是ESO模块任务下的子任务时，不做额外拦截。
            if (!JF_PLMConstants_mxJPO.TYPE_JF_ESOTask.equalsIgnoreCase(taskType)
                    && !JF_PLMConstants_mxJPO.TYPE_JF_ESOTask.equalsIgnoreCase(parentTaskType)) {
                return 0;
            }

            // ESO模块任务和ESO子任务只能由ESO审核员从审批中提升到已完成。
            Vector assignments = PersonUtil.getAssignments(context, context.getUser());
            if (!assignments.contains(ESO_ADMIN_ROLE)) {
                String strMess = EnoviaResourceBundle.getProperty(context, "emxProgramCentralStringResource", context.getLocale(), "emxProgramCentral.ESOTaskReviewPromoteCheck.NoAccess");
                emxContextUtil_mxJPO.mqlNotice(context, strMess);
                return 1;
            }

            // ESO子任务从审批中提升到完成时，全部关联问题必须已经关闭。
            // ESO模块任务本身不执行问题关闭校验，避免扩大本次需求范围。
            if (JF_PLMConstants_mxJPO.TYPE_JF_ESOTask.equalsIgnoreCase(parentTaskType)) {
                //20260729 update by ljr 直接查询未关闭问题，避免先查全部问题后再次批量查询状态；提示复用原国际化并显示任务名称；
                MapList openIssueList = new Issue().getAllIssues(context, taskPhysicalId, true, false);
                if (!openIssueList.isEmpty()) {
                    String strMess = EnoviaResourceBundle.getProperty(
                            context,
                            "emxProgramCentralStringResource",
                            context.getLocale(),
                            "emxProgramCentral.task.checkIssueError");
                    emxContextUtil_mxJPO.mqlNotice(context, strMess + ": " + taskName);
                    return 1;
                }
            }
        } catch (Exception e) {
            JF_LOGGER.error("---checkESOTaskReviewPromoteToComplete---error", e);
            throw e;
        }
        return 0;
    }

    /**
     * 字符串数字转整数，空值或异常值按0处理
     *
     * @param value 字符串数字
     * @return int 转换后的整数
     * @author LIUJR
     * @date 2026/7/17
     * @description 用于签发次数、流水码等字符串数字排序，避免出现10小于2的字符串排序问题。
     */
    private int parseIntValue(String value) {
        if (UIUtil.isNullOrEmpty(value)) {
            return 0;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * 删除ESO签发记录并同步模块节点的颜色标识和审核次数
     * 1. 删除前保存每条签发记录关联的模块节点；
     * 2. 批量删除签发记录后，按审核次数倒序、Name倒序取得当前最新记录；
     * 3. 仍有签发记录时，将最新记录的签发颜色和审核次数回写模块节点；
     * 4. 签发记录全部删除时，将模块节点的颜色标识和审核次数置空。
     *
     * @param context 上下文
     * @param args 参数Map，reviewIds为待删除签发记录ID列表
     * @return java.lang.Boolean 删除及同步成功返回true
     * @throws Exception 删除对象或同步任务属性失败时抛出异常
     * @author LIUJR
     * @date 2026/7/29
     */
    public Boolean deleteESOReviewRecords(Context context, String[] args) throws Exception {
        Map paramMap = JPO.unpackArgs(args);
        StringList reviewIdList = (StringList) paramMap.get("reviewIds");
        if (reviewIdList == null || reviewIdList.isEmpty()) {
            return Boolean.TRUE;
        }
        StringList deleteObjectIdList = new StringList();
        // 删除前查询关联模块节点，避免签发记录删除后无法反查需要同步的任务。
        Set<String> esoTaskIdSet = new LinkedHashSet<String>();
        DomainObject reviewObject = DomainObject.newInstance(context);
        StringList reviewSelectList = StringList.create(
                DomainConstants.SELECT_TYPE,
                "to[JFESOTask2ESOReview].from.id");
        for (int i = 0; i < reviewIdList.size(); i++) {
            String reviewId = (String) reviewIdList.get(i);
            reviewObject.setId(reviewId);
            String strCurrent  = reviewObject.getInfo(context, DomainConstants.SELECT_CURRENT);
            if (!"Create".equalsIgnoreCase(strCurrent)) {
                continue;
            }
            Map reviewInfo = reviewObject.getInfo(context, reviewSelectList);
            deleteObjectIdList.add(reviewId);
            //如果签发记录有文档，需要将文档一起删除
            StringList docIdList = reviewObject.getInfoList(context, "from[JFESOReview2Document].to.id");
            if (!docIdList.isEmpty()) {
                deleteObjectIdList.addAll(docIdList);
            }
            String esoTaskId = UIUtil.getValue(reviewInfo, "to[JFESOTask2ESOReview].from.id");
            if (UIUtil.isNotNullAndNotEmpty(esoTaskId)) {
                esoTaskIdSet.add(esoTaskId);
            }
        }
        boolean contextPushed = false;
        try {
            ContextUtil.pushContext(context);
            contextPushed = true;
            ContextUtil.startTransaction(context, true);
            // 先完成本次批量删除，再按数据库中实际剩余的签发记录重新计算模块节点汇总值。
            DomainObject.deleteObjects(context, deleteObjectIdList.toStringArray());
            for (String esoTaskId : esoTaskIdSet) {
                syncESOTaskReviewSummary(context, esoTaskId);
            }
            ContextUtil.commitTransaction(context);
            return Boolean.TRUE;
        } catch (Exception e) {
            ContextUtil.abortTransaction(context);
            throw e;
        } finally {
            if (contextPushed) {
                ContextUtil.popContext(context);
            }
        }
    }

    /**
     * 按模块节点当前剩余签发记录同步颜色标识和审核次数
     *
     * @param context 上下文
     * @param esoTaskId ESO模块节点ID
     * @return void
     * @throws Exception 查询签发记录或更新任务属性失败时抛出异常
     * @author LIUJR
     * @date 2026/7/29
     */
    private void syncESOTaskReviewSummary(Context context, String esoTaskId) throws Exception {
        if (UIUtil.isNullOrEmpty(esoTaskId)) {
            return;
        }

        String reviewCountSelect = "attribute[JF_ReviewCounte]";
        String phaseStateSelect = "attribute[JF_PhaseState]";
        DomainObject esoTaskObject = DomainObject.newInstance(context, esoTaskId);
        // 统一调用公共方法，按审核次数整数倒序、Name倒序取得当前最新的有效签发记录。
        Map latestReviewMap = JF_PublicMethodClass_mxJPO.getLatestESOReview(
                context,
                esoTaskId);

        // 没有剩余签发记录时写空；有记录时只取最新签发记录的审核次数和颜色。
        String taskReviewCount = DomainConstants.EMPTY_STRING;
        String taskColorIdentification = DomainConstants.EMPTY_STRING;
        if (!latestReviewMap.isEmpty()) {
            taskReviewCount = UIUtil.getValue(latestReviewMap, reviewCountSelect);
            taskColorIdentification = convertColorRange(UIUtil.getValue(latestReviewMap, phaseStateSelect));
        }
        Map taskAttributeMap = new HashMap();
        taskAttributeMap.put("JF_ReviewCount", taskReviewCount);
        taskAttributeMap.put("JF_ColorIdentification", taskColorIdentification);
        esoTaskObject.setAttributeValues(context, taskAttributeMap);
        JF_LOGGER.info("syncESOTaskReviewSummary----esoTaskId:{}, reviewCount:{}, color:{}",
                esoTaskId,
                taskReviewCount,
                taskColorIdentification);
    }

    /**
     * ESO任务相关字段的编辑权限
     * 1. 适用于任务排程视图PMCWBSViewTable和ESO视图JF_PMCWBSESOViewTable；
     * 2. ESO任务范围包括JF_ESOTask模块节点，以及通过Subtask关系直接挂在模块节点下的子任务；
     * 3. 是否APQP、是否关键任务、实际开始时间和实际结束时间：
     *    3.1 Review、Complete状态不允许任何人编辑；
     *    3.2 其他状态仅允许当前任务owner编辑，任务被分派人及其他用户均不可编辑；
     * 4. 非ESO任务的实际开始时间和实际结束时间继续沿用OOTB权限，避免影响普通项目任务；
     * 5. 非ESO任务的是否APQP和是否关键任务保留原有可编辑设置；
     * 6. 方法按objectList顺序返回每一行的true或false，保证权限结果与Table行一一对应。
     *
     * @param context 上下文
     * @param args Table列权限参数
     * @return matrix.util.StringList 每行编辑权限
     * @throws Exception 获取任务信息或OOTB权限失败时抛出异常
     * @author LIUJR
     * @date 2026/7/31
     * @description 统一控制ESO任务APQP、关键任务和实际时间字段的编辑权限。
     */
    public StringList getESOTaskOwnerWorkEditAccess(Context context, String[] args) throws Exception {
        StringList result = new StringList();
        Map programMap = JPO.unpackArgs(args);
        MapList objectList = (MapList) programMap.get("objectList");
        if (objectList.isEmpty()) {
            return result;
        }
        Map columnMap = (Map) programMap.get("columnMap");
        String columnName = DomainConstants.EMPTY_STRING;
        if (columnMap != null) {
            Map colAttrMap = (Map) columnMap.get("colAttrMap");
            columnName = colAttrMap == null
                    ? UIUtil.getValue(columnMap, DomainConstants.SELECT_NAME)
                    : UIUtil.getValue(colAttrMap, DomainConstants.SELECT_NAME);
        }

        // 这里只判断当前调用权限方法的Table列，ESO任务范围在读取任务类型后单独判断。
        boolean isActualDateColumn = "PhaseActualStartDate".equalsIgnoreCase(columnName) || "PhaseActualEndDate".equalsIgnoreCase(columnName);
        StringList ootbAccessList = new StringList();
        if (isActualDateColumn) {
            // 非ESO任务的实际时间列继续使用项目管理原有权限。
            ootbAccessList = JPO.invoke(
                    context,
                    "emxTask",
                    null,
                    "isSummaryTaskCellEditable",
                    args,
                    StringList.class);
        }

        //20260731 update by ljr 按Table行顺序批量查询任务信息，去掉中间Map构造和额外循环；
        String[] taskIdArray = new String[objectList.size()];
        for (int i = 0; i < objectList.size(); i++) {
            taskIdArray[i] = UIUtil.getValue((Map) objectList.get(i), DomainConstants.SELECT_ID);
        }

        StringList taskSelectList = JF_Util_mxJPO.basicBolistSel();
        taskSelectList.add("to[Subtask].from.type");
        MapList taskInfoList = DomainObject.getInfo(context, taskIdArray, taskSelectList);

        String loginUser = context.getUser();
        String taskType = EMPTY_STRING;
        String parentTaskType = EMPTY_STRING;
        String taskCurrent = EMPTY_STRING;
        String taskOwner = EMPTY_STRING;
        boolean isTaskOwner;
        boolean isComplete;
        boolean isReview;
        for (int i = 0; i < objectList.size(); i++) {
            Map taskMap = (Map) taskInfoList.get(i);
            if (taskMap == null) {
                result.add("false");
                continue;
            }
            taskType = UIUtil.getValue(taskMap, DomainConstants.SELECT_TYPE);
            parentTaskType = UIUtil.getValue(taskMap, "to[Subtask].from.type");
            // 当前对象是JF_ESOTask模块节点，或其直接父任务是JF_ESOTask时，均按ESO任务权限处理。
            if (JF_PLMConstants_mxJPO.TYPE_JF_ESOTask.equalsIgnoreCase(taskType) || JF_PLMConstants_mxJPO.TYPE_JF_ESOTask.equalsIgnoreCase(parentTaskType)) {
                taskCurrent = UIUtil.getValue(taskMap, DomainConstants.SELECT_CURRENT);
                taskOwner = UIUtil.getValue(taskMap, DomainConstants.SELECT_OWNER);
                isTaskOwner = loginUser.equalsIgnoreCase(taskOwner);
                isComplete = ESO_TASK_STATE_COMPLETE.equalsIgnoreCase(taskCurrent);
                isReview = ESO_TASK_STATE_REVIEW.equalsIgnoreCase(taskCurrent);
                //20260731 update by ljr 备注列拆分为独立权限方法，避免ESO视图缓存同一Program和Function后串用不同列的权限结果；
                boolean hasAccess = !isReview && !isComplete && isTaskOwner;
                result.add(String.valueOf(hasAccess));
            } else if (isActualDateColumn) {
                result.add(ootbAccessList != null && ootbAccessList.size() > i
                        ? ootbAccessList.get(i)
                        : "false");
            } else {
                // APQP和关键任务的普通任务行保持修改前行为。
                result.add("true");
            }
        }
        return result;
    }

    /**
     * ESO任务备注字段的编辑权限
     * 1. 仅用于ESO视图JF_PMCWBSESOViewTable的JF_Remark列；
     * 2. ESO任务范围包括JF_ESOTask模块节点，以及通过Subtask关系直接挂在模块节点下的子任务；
     * 3. Complete状态不允许任何人编辑备注；
     * 4. 其他状态允许当前任务owner或任务被分派人编辑备注；
     * 5. 非ESO任务不开放ESO备注字段的编辑权限；
     * 6. 使用独立的Edit Access Function，避免Table权限缓存与APQP、关键任务和实际时间列相互影响；
     * 7. 方法按objectList顺序返回每一行的true或false，保证权限结果与Table行一一对应。
     *
     * @param context 上下文
     * @param args Table列权限参数
     * @return matrix.util.StringList 每行编辑权限
     * @throws Exception 获取任务信息失败时抛出异常
     * @author LIUJR
     * @date 2026/7/31
     * @description 控制ESO任务备注字段的编辑权限。
     */
    public StringList getESOTaskRemarkEditAccess(Context context, String[] args) throws Exception {
        StringList result = new StringList();
        Map programMap = JPO.unpackArgs(args);
        MapList objectList = (MapList) programMap.get("objectList");
        if (objectList.isEmpty()) {
            return result;
        }

        // 按Table行顺序批量查询权限判断所需信息，避免逐行访问数据库。
        String[] taskIdArray = new String[objectList.size()];
        for (int i = 0; i < objectList.size(); i++) {
            taskIdArray[i] = UIUtil.getValue((Map) objectList.get(i), DomainConstants.SELECT_ID);
        }

        StringList taskSelectList = JF_Util_mxJPO.basicBolistSel();
        taskSelectList.add("to[Subtask].from.type");
        taskSelectList.add("to[Assigned Tasks].from.name");
        MapList taskInfoList = DomainObject.getInfo(context, taskIdArray, taskSelectList);

        String loginUser = context.getUser();
        for (int i = 0; i < objectList.size(); i++) {
            Map taskMap = (Map) taskInfoList.get(i);
            if (taskMap == null) {
                result.add("false");
                continue;
            }

            String taskType = UIUtil.getValue(taskMap, DomainConstants.SELECT_TYPE);
            String parentTaskType = UIUtil.getValue(taskMap, "to[Subtask].from.type");
            // 当前对象是JF_ESOTask模块节点，或其直接父任务是JF_ESOTask时，均按ESO任务权限处理。
            if (!JF_PLMConstants_mxJPO.TYPE_JF_ESOTask.equalsIgnoreCase(taskType)
                    && !JF_PLMConstants_mxJPO.TYPE_JF_ESOTask.equalsIgnoreCase(parentTaskType)) {
                result.add("false");
                continue;
            }

            String taskCurrent = UIUtil.getValue(taskMap, DomainConstants.SELECT_CURRENT);
            if (ESO_TASK_STATE_COMPLETE.equalsIgnoreCase(taskCurrent)) {
                result.add("false");
                continue;
            }

            String taskOwner = UIUtil.getValue(taskMap, DomainConstants.SELECT_OWNER);
            boolean isTaskOwner = loginUser.equalsIgnoreCase(taskOwner);
            boolean isTaskAssignee = false;
            Object taskAssigneeValue = taskMap.get("to[Assigned Tasks].from.name");
            if (taskAssigneeValue instanceof Collection) {
                for (Object taskAssignee : (Collection) taskAssigneeValue) {
                    if (loginUser.equalsIgnoreCase(String.valueOf(taskAssignee))) {
                        isTaskAssignee = true;
                        break;
                    }
                }
            } else if (taskAssigneeValue != null) {
                isTaskAssignee = loginUser.equalsIgnoreCase(String.valueOf(taskAssigneeValue));
            }
            result.add(String.valueOf(isTaskOwner || isTaskAssignee));
        }
        return result;
    }

    /**
     * ESO任务特性页是否APQP和是否关键任务字段编辑权限
     * 1. JF_ESOTask及直接父级为JF_ESOTask的任务，仅owner在非Review、非Complete状态下允许编辑；
     * 2. 其他项目任务保持原特性页编辑行为。
     *
     * @param context 上下文
     * @param args Form字段权限参数
     * @return boolean 当前用户是否允许编辑字段
     * @throws Exception 获取任务信息失败时抛出异常
     * @author LIUJR
     * @date 2026/8/5
     * @description 控制ESO任务特性页是否APQP和是否关键任务字段的编辑权限。
     */
    @ProgramCallable
    public boolean getESOTaskPropertyOwnerEditAccess(Context context, String[] args) throws Exception {
        Map programMap = JPO.unpackArgs(args);
        Map requestMap = (Map) programMap.get("requestMap");
        String objectId = requestMap == null ? DomainConstants.EMPTY_STRING : UIUtil.getValue(requestMap, "objectId");
        if (UIUtil.isNullOrEmpty(objectId)) {
            objectId = UIUtil.getValue(programMap, "objectId");
        }
        if (UIUtil.isNullOrEmpty(objectId)) {
            return false;
        }

        String parentTaskTypeSelect = "to[Subtask].from.type";
        StringList taskSelectList = StringList.create(
                DomainConstants.SELECT_TYPE,
                DomainConstants.SELECT_CURRENT,
                DomainConstants.SELECT_OWNER,
                parentTaskTypeSelect);
        Map taskInfo = DomainObject.newInstance(context, objectId).getInfo(context, taskSelectList);
        String taskType = UIUtil.getValue(taskInfo, DomainConstants.SELECT_TYPE);
        String parentTaskType = UIUtil.getValue(taskInfo, parentTaskTypeSelect);
        boolean isESOTask = JF_PLMConstants_mxJPO.TYPE_JF_ESOTask.equalsIgnoreCase(taskType)
                || JF_PLMConstants_mxJPO.TYPE_JF_ESOTask.equalsIgnoreCase(parentTaskType);
        if (!isESOTask) {
            return true;
        }

        String taskCurrent = UIUtil.getValue(taskInfo, DomainConstants.SELECT_CURRENT);
        String taskOwner = UIUtil.getValue(taskInfo, DomainConstants.SELECT_OWNER);
        return context.getUser().equalsIgnoreCase(taskOwner)
                && !ESO_TASK_STATE_REVIEW.equalsIgnoreCase(taskCurrent)
                && !ESO_TASK_STATE_COMPLETE.equalsIgnoreCase(taskCurrent);
    }

    /**
     * ESO任务特性页名称和说明字段编辑权限
     * 1. JF_ESOTask及直接父级为JF_ESOTask的任务，不允许任何人编辑名称和说明；
     * 2. 其他项目任务保持原特性页编辑行为。
     *
     * @param context 上下文
     * @param args Form字段权限参数
     * @return boolean 当前用户是否允许编辑名称或说明
     * @throws Exception 获取任务信息失败时抛出异常
     * @author LIUJR
     * @date 2026/8/5
     * @description 禁止在特性页修改ESO任务的名称和说明。
     */
    @ProgramCallable
    public boolean getESOTaskPropertyNameDescriptionEditAccess(Context context, String[] args) throws Exception {
        Map programMap = JPO.unpackArgs(args);
        Map requestMap = (Map) programMap.get("requestMap");
        String objectId = requestMap == null ? DomainConstants.EMPTY_STRING : UIUtil.getValue(requestMap, "objectId");
        if (UIUtil.isNullOrEmpty(objectId)) {
            objectId = UIUtil.getValue(programMap, "objectId");
        }
        if (UIUtil.isNullOrEmpty(objectId)) {
            return false;
        }

        String parentTaskTypeSelect = "to[Subtask].from.type";
        StringList taskSelectList = StringList.create(
                DomainConstants.SELECT_TYPE,
                parentTaskTypeSelect);
        Map taskInfo = DomainObject.newInstance(context, objectId).getInfo(context, taskSelectList);
        String taskType = UIUtil.getValue(taskInfo, DomainConstants.SELECT_TYPE);
        String parentTaskType = UIUtil.getValue(taskInfo, parentTaskTypeSelect);
        boolean isESOTask = JF_PLMConstants_mxJPO.TYPE_JF_ESOTask.equalsIgnoreCase(taskType)
                || JF_PLMConstants_mxJPO.TYPE_JF_ESOTask.equalsIgnoreCase(parentTaskType);
        return !isESOTask;
    }

    /**
     * ESO任务特性页备注字段编辑权限
     * 1. JF_ESOTask及直接父级为JF_ESOTask的任务，Complete状态不允许编辑；
     * 2. 其他状态允许任务owner或任务被分派人编辑；
     * 3. 其他项目任务保持原特性页编辑行为。
     *
     * @param context 上下文
     * @param args Form字段权限参数
     * @return boolean 当前用户是否允许编辑备注
     * @throws Exception 获取任务信息失败时抛出异常
     * @author LIUJR
     * @date 2026/8/5
     * @description 控制ESO任务特性页备注字段的编辑权限。
     */
    @ProgramCallable
    public boolean getESOTaskPropertyRemarkEditAccess(Context context, String[] args) throws Exception {
        Map programMap = JPO.unpackArgs(args);
        Map requestMap = (Map) programMap.get("requestMap");
        String objectId = requestMap == null ? DomainConstants.EMPTY_STRING : UIUtil.getValue(requestMap, "objectId");
        if (UIUtil.isNullOrEmpty(objectId)) {
            objectId = UIUtil.getValue(programMap, "objectId");
        }
        if (UIUtil.isNullOrEmpty(objectId)) {
            return false;
        }

        String parentTaskTypeSelect = "to[Subtask].from.type";
        String taskAssigneeSelect = "to[Assigned Tasks].from.name";
        StringList taskSelectList = StringList.create(
                DomainConstants.SELECT_TYPE,
                DomainConstants.SELECT_CURRENT,
                DomainConstants.SELECT_OWNER,
                parentTaskTypeSelect,
                taskAssigneeSelect);
        Map taskInfo = DomainObject.newInstance(context, objectId).getInfo(context, taskSelectList);
        String taskType = UIUtil.getValue(taskInfo, DomainConstants.SELECT_TYPE);
        String parentTaskType = UIUtil.getValue(taskInfo, parentTaskTypeSelect);
        boolean isESOTask = JF_PLMConstants_mxJPO.TYPE_JF_ESOTask.equalsIgnoreCase(taskType)
                || JF_PLMConstants_mxJPO.TYPE_JF_ESOTask.equalsIgnoreCase(parentTaskType);
        if (!isESOTask) {
            return true;
        }

        String taskCurrent = UIUtil.getValue(taskInfo, DomainConstants.SELECT_CURRENT);
        if (ESO_TASK_STATE_COMPLETE.equalsIgnoreCase(taskCurrent)) {
            return false;
        }

        String loginUser = context.getUser();
        String taskOwner = UIUtil.getValue(taskInfo, DomainConstants.SELECT_OWNER);
        if (loginUser.equalsIgnoreCase(taskOwner)) {
            return true;
        }
        Object taskAssigneeValue = taskInfo.get(taskAssigneeSelect);
        if (taskAssigneeValue instanceof Collection) {
            for (Object taskAssignee : (Collection) taskAssigneeValue) {
                if (loginUser.equalsIgnoreCase(String.valueOf(taskAssignee))) {
                    return true;
                }
            }
            return false;
        }
        return taskAssigneeValue != null
                && loginUser.equalsIgnoreCase(String.valueOf(taskAssigneeValue));
    }

    /**
     * 校验ESO子任务的估计开始时间和估计结束时间是否位于直接父任务的时间范围内。
     * 1. 当前任务为Task时，仅校验直接父任务为JF_ESOTask的场景；
     * 2. 当前任务为JF_ESOTask时，校验直接父任务为Task或JF_ESOTask的场景；
     * 3. 只修改开始或结束时间时，根据持续时间计算OOTB排程后的最终日期范围，再与直接父任务的日期比较；
     * 4. 开始和结束时间同时修改时不执行ESO父级日期范围校验；
     * 5. 校验通过后继续调用OOTB的updateScheduleChanges，保留原任务排程处理逻辑。
     *
     * @param context 上下文
     * @param args Structure Browser保存后处理参数
     * @return java.util.Map 校验失败时返回STOP和业务提示，校验成功时返回OOTB处理结果
     * @throws Exception 查询任务信息或执行OOTB后处理失败时抛出异常
     * @author LIUJR
     * @date 2026/8/3
     * @description 统一校验ESO子任务估计日期范围。
     */
    @com.matrixone.apps.framework.ui.PostProcessCallable
    public Map postProcessValidateESOTaskEstimatedDateRange(Context context, String[] args) throws Exception {
        try {
            JF_LOGGER.info("postProcessValidateESOTaskEstimatedDateRange.......................");
            Map programMap = JPO.unpackArgs(args);
            com.matrixone.jdom.Document xmlDoc = (com.matrixone.jdom.Document) programMap.get("XMLDoc");
            Map<String, Map<String, String>> changedTaskDateMap = new LinkedHashMap<String, Map<String, String>>();

            if (xmlDoc != null) {
                // 从保存XML中收集任务本次提交的估计日期新值，不能查询子任务对象上的旧值进行校验。
                List objectElementList = xmlDoc.getRootElement().getChildren("object");
                for (int i = 0; i < objectElementList.size(); i++) {
                    com.matrixone.jdom.Element objectElement = (com.matrixone.jdom.Element) objectElementList.get(i);
                    String taskId = objectElement.getAttributeValue("objectId");
                    if (UIUtil.isNullOrEmpty(taskId)) {
                        taskId = objectElement.getAttributeValue("id");
                    }
                    if (UIUtil.isNullOrEmpty(taskId)) {
                        continue;
                    }
                    List columnElementList = objectElement.getChildren("column");
                    for (int j = 0; j < columnElementList.size(); j++) {
                        com.matrixone.jdom.Element columnElement = (com.matrixone.jdom.Element) columnElementList.get(j);
                        String columnName = columnElement.getAttributeValue("name");
                        if (!"PhaseEstimatedStartDate".equalsIgnoreCase(columnName)
                                && !"PhaseEstimatedEndDate".equalsIgnoreCase(columnName)) {
                            continue;
                        }
                        boolean isChanged = "true".equalsIgnoreCase(columnElement.getAttributeValue("edited"))
                                || "true".equalsIgnoreCase(columnElement.getAttributeValue("changed"))
                                || "true".equalsIgnoreCase(columnElement.getAttributeValue("modified"))
                                || "changed".equalsIgnoreCase(columnElement.getAttributeValue("status"));
                        if (!isChanged) {
                            continue;
                        }
                        Map<String, String> changedDateMap = changedTaskDateMap.get(taskId);
                        if (changedDateMap == null) {
                            changedDateMap = new HashMap<String, String>();
                            changedTaskDateMap.put(taskId, changedDateMap);
                        }
                        String dateColumnName = "PhaseEstimatedStartDate".equalsIgnoreCase(columnName)
                                ? SELECT_ATTR_Task_Estimated_Start_Date
                                : SELECT_ATTR_Task_Estimated_Finish_Date;
                        //20260804 update by ljr XML日期保存在column文本中，不存在oldValue/newValue属性，保留原值并在比较时按页面格式解析。
                        changedDateMap.put(dateColumnName, columnElement.getValue().trim());
                    }
                }
            }
            JF_LOGGER.info("changedDateMap:{}", changedTaskDateMap);
            if (!changedTaskDateMap.isEmpty()) {
                String parentSelectPrefix = "to[Subtask].from.";
                String parentTypeSelect = parentSelectPrefix + DomainConstants.SELECT_TYPE;
                String parentStartSelect = parentSelectPrefix + SELECT_ATTR_Task_Estimated_Start_Date;
                String parentFinishSelect = parentSelectPrefix + SELECT_ATTR_Task_Estimated_Finish_Date;
                StringList taskSelectList = StringList.create(
                        DomainConstants.SELECT_ID,
                        DomainConstants.SELECT_TYPE,
                        parentTypeSelect,
                        parentStartSelect,
                        parentFinishSelect,
                        SELECT_ATTR_Task_Estimated_Start_Date,
                        SELECT_ATTR_Task_Estimated_Finish_Date,
                        JF_PLMConstants_mxJPO.SELECT_ATTR_Task_Estimated_Duration);
                MapList taskInfoList = DomainObject.getInfo(context,
                        changedTaskDateMap.keySet().toArray(new String[changedTaskDateMap.size()]), taskSelectList);
                String startBeforeParentMessage = EnoviaResourceBundle.getProperty(context, "emxProgramCentralStringResource", context.getLocale(), "emxProgramCentral.ESOSubTaskEstimatedDate.StartBeforeParent");
                String finishAfterParentMessage = EnoviaResourceBundle.getProperty(context, "emxProgramCentralStringResource", context.getLocale(), "emxProgramCentral.ESOSubTaskEstimatedDate.FinishAfterParent");
                String outOfParentRangeMessage = EnoviaResourceBundle.getProperty(context, "emxProgramCentralStringResource", context.getLocale(), "emxProgramCentral.ESOSubTaskEstimatedDate.OutOfParentRange");
                SimpleDateFormat xmlDateFormat = new SimpleDateFormat("yyyy年M月d日", Locale.CHINA);
                SimpleDateFormat matrixDateFormat = new SimpleDateFormat(eMatrixDateFormat.getEMatrixDateFormat(), Locale.US);
                SimpleDateFormat dayFormat = new SimpleDateFormat("yyyyMMdd", Locale.US);
                xmlDateFormat.setLenient(false);
                matrixDateFormat.setLenient(false);
                dayFormat.setLenient(false);
                //20260804 update by ljr 多行保存时汇总整批日期越界结果，分别命中开始和结束越界时只返回组合提示。
                boolean hasStartBeforeParent = false;
                boolean hasFinishAfterParent = false;
                for (int i = 0; i < taskInfoList.size(); i++) {
                    Map taskInfo = (Map) taskInfoList.get(i);
                    String taskType = UIUtil.getValue(taskInfo, DomainConstants.SELECT_TYPE);
                    String parentType = UIUtil.getValue(taskInfo, parentTypeSelect);
                    boolean isTaskUnderESOTask = DomainConstants.TYPE_TASK.equalsIgnoreCase(taskType)
                            && JF_PLMConstants_mxJPO.TYPE_JF_ESOTask.equalsIgnoreCase(parentType);
                    boolean isESOTaskUnderTask = JF_PLMConstants_mxJPO.TYPE_JF_ESOTask.equalsIgnoreCase(taskType)
                            && (DomainConstants.TYPE_TASK.equalsIgnoreCase(parentType)
                            || JF_PLMConstants_mxJPO.TYPE_JF_ESOTask.equalsIgnoreCase(parentType));
                    if (!isTaskUnderESOTask && !isESOTaskUnderTask) {
                        continue;
                    }

                    String taskId = UIUtil.getValue(taskInfo, DomainConstants.SELECT_ID);
                    Map<String, String> changedDateMap = changedTaskDateMap.get(taskId);
                    String taskStart = changedDateMap.get(SELECT_ATTR_Task_Estimated_Start_Date);
                    String taskFinish = changedDateMap.get(SELECT_ATTR_Task_Estimated_Finish_Date);
                    String parentStart = UIUtil.getValue(taskInfo, parentStartSelect);
                    String parentFinish = UIUtil.getValue(taskInfo, parentFinishSelect);
                    String currentTaskStart = UIUtil.getValue(taskInfo, SELECT_ATTR_Task_Estimated_Start_Date);
                    String currentTaskFinish = UIUtil.getValue(taskInfo, SELECT_ATTR_Task_Estimated_Finish_Date);
                    String taskDuration = UIUtil.getValue(taskInfo, JF_PLMConstants_mxJPO.SELECT_ATTR_Task_Estimated_Duration);
                    boolean startChanged = changedDateMap.containsKey(SELECT_ATTR_Task_Estimated_Start_Date);
                    boolean finishChanged = changedDateMap.containsKey(SELECT_ATTR_Task_Estimated_Finish_Date);
                    //20260805 update by ljr 开始和结束时间同时修改时不执行ESO父级日期范围校验，交由OOTB按提交值处理。
                    if (startChanged && finishChanged) {
                        continue;
                    }
                    boolean startBeforeParent = false;
                    boolean finishAfterParent = false;

                    //20260805 update by ljr 仅改单端日期时按OOTB最终排程结果校验，并按持续时间校验推算出的另一端日期。
                    boolean useFinishAsAnchor = finishChanged && UIUtil.isNotNullAndNotEmpty(taskFinish);
                    if (useFinishAsAnchor && UIUtil.isNotNullAndNotEmpty(parentStart)
                            && UIUtil.isNotNullAndNotEmpty(parentFinish)) {
                        Date effectiveFinishDate = xmlDateFormat.parse(taskFinish);
                        Date parentStartDate = matrixDateFormat.parse(parentStart);
                        Date parentFinishDate = matrixDateFormat.parse(parentFinish);

                        // 页面XML只有日期，补回任务结束时刻，保持工作日历计算口径与OOTB一致。
                        if (UIUtil.isNotNullAndNotEmpty(currentTaskFinish)) {
                            Calendar effectiveFinishCalendar = Calendar.getInstance();
                            effectiveFinishCalendar.setTime(effectiveFinishDate);
                            Calendar currentFinishCalendar = Calendar.getInstance();
                            currentFinishCalendar.setTime(matrixDateFormat.parse(currentTaskFinish));
                            effectiveFinishCalendar.set(Calendar.HOUR_OF_DAY, currentFinishCalendar.get(Calendar.HOUR_OF_DAY));
                            effectiveFinishCalendar.set(Calendar.MINUTE, currentFinishCalendar.get(Calendar.MINUTE));
                            effectiveFinishCalendar.set(Calendar.SECOND, currentFinishCalendar.get(Calendar.SECOND));
                            effectiveFinishDate = effectiveFinishCalendar.getTime();
                        }

                        finishAfterParent = dayFormat.format(effectiveFinishDate)
                                .compareTo(dayFormat.format(parentFinishDate)) > 0;
                        startBeforeParent = dayFormat.format(effectiveFinishDate)
                                .compareTo(dayFormat.format(parentStartDate)) < 0;
                        if (!startBeforeParent && UIUtil.isNotNullAndNotEmpty(taskDuration)) {
                            com.matrixone.apps.common.Task scheduleTask = new com.matrixone.apps.common.Task();
                            scheduleTask.setId(taskId);
                            WorkCalendar workCalendar = scheduleTask.getSchedulingCalendar(context);
                            //20260804 update by ljr 未配置排程日历时返回的对象未初始化，使用getObjectId避免getId(context)打开空业务对象。
                            String workCalendarId = workCalendar == null ? "" : workCalendar.getObjectId();
                            long availableDuration;
                            if (UIUtil.isNotNullAndNotEmpty(workCalendarId)) {
                                availableDuration = workCalendar.computeDuration(context, parentStartDate, effectiveFinishDate);
                            } else {
                                availableDuration = new DateUtil().computeDuration(parentStartDate, effectiveFinishDate);
                            }
                            startBeforeParent = Double.compare((double) availableDuration, Double.parseDouble(taskDuration)) < 0;
                        }
                    } else if (startChanged && UIUtil.isNotNullAndNotEmpty(taskStart)
                            && UIUtil.isNotNullAndNotEmpty(parentStart)
                            && UIUtil.isNotNullAndNotEmpty(parentFinish)) {
                        Date effectiveStartDate = xmlDateFormat.parse(taskStart);
                        Date parentStartDate = matrixDateFormat.parse(parentStart);
                        Date parentFinishDate = matrixDateFormat.parse(parentFinish);

                        // 页面XML只有日期，补回任务开始时刻，保持工作日历计算口径与OOTB一致。
                        if (UIUtil.isNotNullAndNotEmpty(currentTaskStart)) {
                            Calendar effectiveStartCalendar = Calendar.getInstance();
                            effectiveStartCalendar.setTime(effectiveStartDate);
                            Calendar currentStartCalendar = Calendar.getInstance();
                            currentStartCalendar.setTime(matrixDateFormat.parse(currentTaskStart));
                            effectiveStartCalendar.set(Calendar.HOUR_OF_DAY, currentStartCalendar.get(Calendar.HOUR_OF_DAY));
                            effectiveStartCalendar.set(Calendar.MINUTE, currentStartCalendar.get(Calendar.MINUTE));
                            effectiveStartCalendar.set(Calendar.SECOND, currentStartCalendar.get(Calendar.SECOND));
                            effectiveStartDate = effectiveStartCalendar.getTime();
                        }

                        startBeforeParent = dayFormat.format(effectiveStartDate)
                                .compareTo(dayFormat.format(parentStartDate)) < 0;
                        finishAfterParent = dayFormat.format(effectiveStartDate)
                                .compareTo(dayFormat.format(parentFinishDate)) > 0;
                        if (!finishAfterParent && UIUtil.isNotNullAndNotEmpty(taskDuration)) {
                            com.matrixone.apps.common.Task scheduleTask = new com.matrixone.apps.common.Task();
                            scheduleTask.setId(taskId);
                            WorkCalendar workCalendar = scheduleTask.getSchedulingCalendar(context);
                            //20260804 update by ljr 未配置排程日历时返回的对象未初始化，使用getObjectId避免getId(context)打开空业务对象。
                            String workCalendarId = workCalendar == null ? "" : workCalendar.getObjectId();
                            long availableDuration;
                            if (UIUtil.isNotNullAndNotEmpty(workCalendarId)) {
                                availableDuration = workCalendar.computeDuration(context, effectiveStartDate, parentFinishDate);
                            } else {
                                availableDuration = new DateUtil().computeDuration(effectiveStartDate, parentFinishDate);
                            }
                            finishAfterParent = Double.compare((double) availableDuration, Double.parseDouble(taskDuration)) < 0;
                        }
                    }
                    hasStartBeforeParent = hasStartBeforeParent || startBeforeParent;
                    hasFinishAfterParent = hasFinishAfterParent || finishAfterParent;
                    if (hasStartBeforeParent && hasFinishAfterParent) {
                        break;
                    }
                }

                if (hasStartBeforeParent || hasFinishAfterParent) {
                    String errorMessage = hasStartBeforeParent && hasFinishAfterParent
                            ? outOfParentRangeMessage
                            : (hasStartBeforeParent ? startBeforeParentMessage : finishAfterParentMessage);
                    Map returnMap = new HashMap();
                    returnMap.put("Action", "STOP");
                    returnMap.put("Message", errorMessage);
                    JF_LOGGER.warn("postProcessValidateESOTaskEstimatedDateRange blocked, taskIds:{}", changedTaskDateMap.keySet());
                    return returnMap;
                }
            }

            // 日期校验通过后执行原WBS后处理，保持任务排程刷新和保存行为不变。
            Map ootbReturnMap = JPO.invoke(context, "emxTask", null, "updateScheduleChanges", args, Map.class);
            return ootbReturnMap == null ? new HashMap() : ootbReturnMap;
        } catch (Exception e) {
            JF_LOGGER.error("---postProcessValidateESOTaskEstimatedDateRange---error", e);
            throw e;
        }
    }

}
