import com.aspose.pdf.operators.Do;
import com.dassault_systemes.enovia.tskv2.ProjectSequence;
import com.dscn.plm.util.NioJDUtils;
import com.google.gson.Gson;
import com.matrixone.apps.common.CommonDocument;
import com.matrixone.apps.common.InboxTask;
import com.matrixone.apps.common.Task;
import com.matrixone.apps.common.util.ComponentsUtil;
import com.matrixone.apps.domain.*;
import com.matrixone.apps.domain.util.*;
import com.matrixone.apps.framework.ui.PostProcessCallable;
import com.matrixone.apps.framework.ui.UIUtil;
import com.matrixone.apps.program.ProgramCentralConstants;
import com.matrixone.apps.program.ProgramCentralUtil;
import ds.enovia.apps.msf.BusinessObjects.ExecuteJPO.args;
import jakarta.mail.BodyPart;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMultipart;
import matrix.db.*;
import matrix.util.StringList;
import org.apache.commons.text.StringEscapeUtils;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jsoup.Jsoup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.Set;
import java.util.stream.Collectors;

import static com.matrixone.apps.domain.DomainConstants.*;

/*
 * @description:PCR相关java代码
 * @author: caipan
 * @date: 2025/10/28 14:55:54
 * @param: * @param[1] null
 * @return:
 **/
public class JF_PCR_mxJPO implements JF_PLMConstants_mxJPO {
    private static final Logger _logger = LoggerFactory.getLogger(JF_PCR_mxJPO.class);
    private static final String STRING_SIGN_TASK_PROPERTIES_ZH = "SignTaskProperties_zh.xml";

    /*
     * @description:PCR列表
     * @author: caipan
     * @date: 2025/10/28 15:05:23
     * @param: * @param[1] context
     * @param[2] args
     * @return:
     **/
    public MapList getJFChangeEvent(Context context, String[] args) throws Exception {
        StringList objectSelects = JF_Util_mxJPO.basicBolistSel();
        objectSelects.add(SELECT_ORIGINATED);
        MapList mapList = DomainObject.findObjects(context, "JF_PCR", "*", "", objectSelects);
        mapList.sort(SELECT_ORIGINATED, "descending", "date");
        return mapList;
    }

    /*
     * @description:创建PCR的后处理，建立项目关系、附件关系
     * @author: caipan
     * @date: 2025/10/30 14:22:21
     * @param: * @param[1] context
     * @param[2] args
     * @return:
     **/
    public void postProcessCreate(Context context, String[] args) throws Exception {
        try {
            _logger.info("postProcessCreate start");
            ContextUtil.pushContext(context);
            Map parameter = (Map) JPO.unpackArgs(args);
            Map requestMap = (Map) parameter.get("requestMap");
            Map paramMap = (Map) parameter.get("paramMap");
            //关联项目ID
            String strProjectID = (String) requestMap.get("JFProjectNameOID");
            String strmainProjectID = (String) requestMap.get("mainProjectOID");
            StringList projectList = FrameworkUtil.split(strProjectID, "|");//项目可以多选
            String strJFPCRBeforeFileId = (String) requestMap.get("JFPCRBeforeFileName");
            String strJFPCRAfterFileId = (String) requestMap.get("JFPCRAfterFileName");
            //创建CRID
            String strNewObjectId = (String) paramMap.get("newObjectId");
            DomainObject pcrObj = DomainObject.newInstance(context, strNewObjectId);
            //关联主项目
            if (UIUtil.isNotNullAndNotEmpty(strmainProjectID)) {
                DomainRelationship.connect(context, strNewObjectId, "JFChange2Project", strmainProjectID, false);
            }
            //关联涉及项目
            if (UIUtil.isNotNullAndNotEmpty(strProjectID)) {
                for (int i = 0; i < projectList.size(); i++) {
                    DomainRelationship.connect(context, strNewObjectId, "JFPCR2Project", projectList.get(i), false);
                }
            }
            //关联变更附件附件
            if (UIUtil.isNotNullAndNotEmpty(strJFPCRBeforeFileId)) {
                DomainRelationship rel = DomainRelationship.connect(context, pcrObj, RELATIONSHIP_REFERENCE_DOCUMENT, DomainObject.newInstance(context, strJFPCRBeforeFileId));
                Map relAttrMap = new HashMap<>();
                relAttrMap.put(JF_PLMConstants_mxJPO.ATTR_PROJECT_ROLE, "before");
                rel.setAttributeValues(context, relAttrMap);
            }
            if (UIUtil.isNotNullAndNotEmpty(strJFPCRAfterFileId)) {
                DomainRelationship rel = DomainRelationship.connect(context, pcrObj, RELATIONSHIP_REFERENCE_DOCUMENT, DomainObject.newInstance(context, strJFPCRAfterFileId));
                Map relAttrMap = new HashMap<>();
                relAttrMap.put(JF_PLMConstants_mxJPO.ATTR_PROJECT_ROLE, "after");
                rel.setAttributeValues(context, relAttrMap);
            }
            _logger.info("postProcessCreate end");
        } catch (Exception e) {
            e.printStackTrace();
            _logger.info(e.getMessage());
        } finally {
            ContextUtil.popContext(context);
        }
    }

    /*
     * @description:变更前附件
     * @author: caipan
     * @date: 2025/10/31 16:32:56
     * @param: * @param[1] context
     * @param[2] args
     * @return:
     **/
    public String buildPCRBeforeFileHtml(Context context, String[] args) throws Exception {
        _logger.info("--------------------- buildECRQQHtml begin --------------------------");
        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        HashMap requestMap = (HashMap) programMap.get("requestMap");
        HashMap fieldMap = (HashMap) programMap.get("fieldMap");
        String strMode = (String) requestMap.get("mode");
        String strLoginUser = context.getUser();
        String strLang = context.getSession().getLanguage();
        String strFileUploadNls = ComponentsUtil.i18nStringNow("emxComponents.Common.JFECRQQFileUpload", strLang);
        String strChangeSource = "";
        String strIsPlatForm = "";
        String objectId = null;
        if (requestMap == null) {
            objectId = (String) programMap.get("objectId");
        } else {
            objectId = (String) requestMap.get("objectId");
        }
        StringBuffer sbDocDown = new StringBuffer();

        _logger.info("objectId:{}", objectId);
        if (UIUtil.isNotNullAndNotEmpty(objectId)) {
            DomainObject ECR = DomainObject.newInstance(context, objectId);
            StringList ECRSelectList = new StringList();
            ECRSelectList.add(SELECT_ATTR_JFQQID);
            ECRSelectList.add(SELECT_CURRENT);
            ECRSelectList.add(SELECT_OWNER);
            ECRSelectList.add(SELECT_ATTR_JFCHANGESOURCE);
            ECRSelectList.add(SELECT_ATTR_JFISPLATFORMPART);
            Map ECRInfoMap = ECR.getInfo(context, ECRSelectList);
            String strCurrent = (String) ECRInfoMap.get(SELECT_CURRENT);
            String strOwner = (String) ECRInfoMap.get(SELECT_OWNER);
            boolean isEdit = strLoginUser.equals(strOwner) && "Draft".equals(strCurrent) ? true : false;
            StringList selectTypeList = new StringList();
            selectTypeList.add(SELECT_ID);
            selectTypeList.add(SELECT_ATTRIBUTE_TITLE);
            selectTypeList.add(SELECT_TYPE);
            selectTypeList.add(SELECT_NAME);
            selectTypeList.add(SELECT_REVISION);
            MapList docList = ECR.getRelatedObjects(context, RELATIONSHIP_REFERENCE_DOCUMENT, // relationship pattern
                    TYPE_DOCUMENT,                                    // object pattern
                    selectTypeList,                            // object selects
                    new StringList(SELECT_RELATIONSHIP_ID), // relationship selects
                    false,                                        // to direction
                    true,                                        // from direction
                    (short) 1,                                    // recursion level
                    "",                // object where clause
                    "attribute[Project Role]=='before'",
                    (short) 0);
            StringList docIDList = (StringList) docList.stream().map(m -> {
                Map doc = (Map) m;
                return doc.get(SELECT_ID);
            }).collect(Collectors.toCollection(StringList::new));
            sbDocDown.append("<table");
            sbDocDown.append("><tr>");
/* //            sbDocDown.append("<td>");
             strQQID = StringEscapeUtils.escapeHtml4(strQQID);
            strFileUploadNls = StringEscapeUtils.escapeHtml4(strFileUploadNls);
          if ("view".equals(strMode)) {
//                sbDocDown.append(JF_PublicMethodClass_mxJPO.buildStringInStrings("<span>", strQQID, "</span>&nbsp;"));
            } else if ("edit".equals(strMode)) {
                if (isEdit) {
//                    sbDocDown.append("<input value=\"" + strQQID + "\" id=\"JFQQ\" name=\"JFQQ\" type=\"text\"  size=\"20\">");
                } else {
                    sbDocDown.append(JF_PublicMethodClass_mxJPO.buildStringInStrings("<span>", strQQID, "</span>&nbsp;"));
                }
            }
//            sbDocDown.append("</td>");*/
            StringList QQFileIdList = new StringList();
            if (isEdit && "edit".equals(strMode)) {
                sbDocDown.append("<td id=\"fileBeforeTd\">");
                sbDocDown.append("<input  id=\"JFPCRBeforeFileId\" name=\"JFPCRBeforeFileName\" value=\"" + docIDList.join(",") + "\" type=\"hidden\">");
                sbDocDown.append("<input onclick=\"javascript:showModalDialog('../components/JF_PCRChangeFilePreCheckin.jsp?objectAction=checkin&amp;flag=before&msfBypass=true&");
                sbDocDown.append("objectId=");
                sbDocDown.append("");
                sbDocDown.append("','730','450')\"" +
                        "        value=\"");
                sbDocDown.append(strFileUploadNls);
                sbDocDown.append("\" type=\"button\">");
                sbDocDown.append("</td>");
            }
            sbDocDown.append("<td id=\"JFPCRBeforeReplace\">");
            //Show Counter Link
            for (int i = 0; i < docList.size(); i++) {
                sbDocDown.append("<div ");
                Map docMap = (Map) docList.get(i);
                Map docObjMap = new HashMap<>();
                String strFileId = (String) docMap.get(SELECT_ID);
                QQFileIdList.add(strFileId);
                sbDocDown.append("style='vertical-align:middle;padding-left:1px;cursor:pointer;display:inline-block' ");
                sbDocDown.append("onClick=\"javascript:callCheckout('").append(strFileId).append("',");
                sbDocDown.append("'download', '', '', 'null', 'null', 'structureBrowser', 'PMCPendingDeliverableSummary', 'null')\">");
                sbDocDown.append("<img style='vertical-align:middle;' src='../common/images/").append("iconSmallDocument.gif").append("'");
                sbDocDown.append(" title=\"");
                String strDocTitle = (String) docMap.get(SELECT_ATTRIBUTE_TITLE);
                strDocTitle = StringEscapeUtils.escapeHtml4(strDocTitle);
                sbDocDown.append(strDocTitle);
                sbDocDown.append("\" />");
                sbDocDown.append("</div>");
            }
            sbDocDown.append("</td>");

            if (isEdit && "edit".equals(strMode)) {
                sbDocDown.append("<td>");
                sbDocDown.append("<a href=\"javascript:removeBeforeDocument()\">");
                sbDocDown.append("<img src=\"../common/images/iconStatusRemoved.gif\" width=\"12\" height=\"12\" border=\"0\" />");
                sbDocDown.append("</a>");
                sbDocDown.append("<a href=\"javascript:removeBeforeDocument()\">");
                //XSSOK
//                sbDocDown.append(remove);
                sbDocDown.append("</a>");
                sbDocDown.append("</td>");
            }


        } else {
            sbDocDown.append("<table");
            sbDocDown.append("><tr>");
            sbDocDown.append("<td id=\"fileBeforeTd\">");
            sbDocDown.append("<input id=\"JFPCRBeforeFileId\"  name=\"JFPCRBeforeFileName\" type=\"hidden\"/>");
            sbDocDown.append("<input onclick=\"javascript:showModalDialog('../components/JF_PCRChangeFilePreCheckin.jsp?objectAction=checkin&amp;msfBypass=true&amp;flag=before&amp;objectId=;");
            sbDocDown.append("','730','450')\"" +
                    "  value=\"");
            _logger.info("strFileUploadNls:{}", strFileUploadNls);
            sbDocDown.append(strFileUploadNls);
            sbDocDown.append("\" type=\"button\"/>");
            sbDocDown.append("</td>");
            sbDocDown.append("<td id=\"JFPCRBeforeReplace\">");
            sbDocDown.append("</td>");


            sbDocDown.append("<td>");
            sbDocDown.append("<a href=\"javascript:removeBeforeDocument()\">");
            sbDocDown.append("<img src=\"../common/images/iconStatusRemoved.gif\" width=\"12\" height=\"12\" border=\"0\" />");
            sbDocDown.append("</a>");
            sbDocDown.append("<a href=\"javascript:removeBeforeDocument()\">");
            //XSSOK
//                sbDocDown.append(remove);
            sbDocDown.append("</a>");
            sbDocDown.append("</td>");
        }
        sbDocDown.append("</tr></table>");
        _logger.info("--------------------- buildECRQQHtml end --------------------------");
        return sbDocDown.toString();
    }

    public String buildPCRAfterFileHtml(Context context, String[] args) throws Exception {
        _logger.info("--------------------- buildECRQQHtml begin --------------------------");
        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        HashMap requestMap = (HashMap) programMap.get("requestMap");
        HashMap fieldMap = (HashMap) programMap.get("fieldMap");
        String strMode = (String) requestMap.get("mode");
        String strLoginUser = context.getUser();
        String strLang = context.getSession().getLanguage();
        String strFileUploadNls = ComponentsUtil.i18nStringNow("emxComponents.Common.JFECRQQFileUpload", strLang);
        String objectId = null;
        if (requestMap == null) {
            objectId = (String) programMap.get("objectId");
        } else {
            objectId = (String) requestMap.get("objectId");
        }
        StringBuffer sbDocDown = new StringBuffer();

        _logger.info("objectId:{}", objectId);
        if (UIUtil.isNotNullAndNotEmpty(objectId)) {
            DomainObject ECR = DomainObject.newInstance(context, objectId);
            StringList ECRSelectList = new StringList();
            ECRSelectList.add(SELECT_CURRENT);
            ECRSelectList.add(SELECT_OWNER);
            Map ECRInfoMap = ECR.getInfo(context, ECRSelectList);
            String strCurrent = (String) ECRInfoMap.get(SELECT_CURRENT);
            String strOwner = (String) ECRInfoMap.get(SELECT_OWNER);
            boolean isEdit = strLoginUser.equals(strOwner) && "Draft".equals(strCurrent) ? true : false;
            StringList selectTypeList = new StringList();
            selectTypeList.add(SELECT_ID);
            selectTypeList.add(SELECT_ATTRIBUTE_TITLE);
            selectTypeList.add(SELECT_TYPE);
            selectTypeList.add(SELECT_NAME);
            selectTypeList.add(SELECT_REVISION);
            MapList docList = ECR.getRelatedObjects(context, RELATIONSHIP_REFERENCE_DOCUMENT, // relationship pattern
                    TYPE_DOCUMENT,                                    // object pattern
                    selectTypeList,                            // object selects
                    new StringList(SELECT_RELATIONSHIP_ID), // relationship selects
                    false,                                        // to direction
                    true,                                        // from direction
                    (short) 1,                                    // recursion level
                    "",                // object where clause
                    "attribute[Project Role]=='after'",
                    (short) 0);
            StringList docIDList = (StringList) docList.stream().map(m -> {
                Map doc = (Map) m;
                return doc.get(SELECT_ID);
            }).collect(Collectors.toCollection(StringList::new));
            sbDocDown.append("<table");
            sbDocDown.append("><tr>");
/*            sbDocDown.append("<td>");
            strQQID = StringEscapeUtils.escapeHtml4(strQQID);
            strFileUploadNls = StringEscapeUtils.escapeHtml4(strFileUploadNls);
            if ("view".equals(strMode)) {
                sbDocDown.append(JF_PublicMethodClass_mxJPO.buildStringInStrings("<span>", strQQID, "</span>&nbsp;"));
            } else if ("edit".equals(strMode)) {
                if (isEdit) {
//                    sbDocDown.append("<input value=\"" + strQQID + "\" id=\"JFQQ\" name=\"JFQQ\" type=\"text\"  size=\"20\">");
                } else {
                    sbDocDown.append(JF_PublicMethodClass_mxJPO.buildStringInStrings("<span>", strQQID, "</span>&nbsp;"));
                }
            }
            sbDocDown.append("</td>");*/
            StringList QQFileIdList = new StringList();
            if (isEdit && "edit".equals(strMode)) {
                sbDocDown.append("<td id=\"fileAfterTd\">");
                sbDocDown.append("<input  id=\"JFPCRAfterFileId\" name=\"JFPCRAfterFileName\" value=\"" + docIDList.join(",") + "\" type=\"hidden\">");
                sbDocDown.append("<input onclick=\"javascript:showModalDialog('../components/JF_PCRChangeFilePreCheckin.jsp?objectAction=checkin&flag=after&msfBypass=true&");
                sbDocDown.append("objectId=");
                sbDocDown.append("");
                sbDocDown.append("','730','450')\"" +
                        "        value=\"");
                sbDocDown.append(strFileUploadNls);
                sbDocDown.append("\" type=\"button\">");
                sbDocDown.append("</td>");
            }
            sbDocDown.append("<td id=\"JFPCRAfterReplace\">");
            //Show Counter Link
            for (int i = 0; i < docList.size(); i++) {
                sbDocDown.append("<div ");
                Map docMap = (Map) docList.get(i);
                Map docObjMap = new HashMap<>();
                String strFileId = (String) docMap.get(SELECT_ID);
                QQFileIdList.add(strFileId);
                sbDocDown.append("style='vertical-align:middle;padding-left:1px;cursor:pointer;display:inline-block' ");
                sbDocDown.append("onClick=\"javascript:callCheckout('").append(strFileId).append("',");
                sbDocDown.append("'download', '', '', 'null', 'null', 'structureBrowser', 'PMCPendingDeliverableSummary', 'null')\">");
                sbDocDown.append("<img style='vertical-align:middle;' src='../common/images/").append("iconSmallDocument.gif").append("'");
                sbDocDown.append(" title=\"");
                String strDocTitle = (String) docMap.get(SELECT_ATTRIBUTE_TITLE);
                strDocTitle = StringEscapeUtils.escapeHtml4(strDocTitle);
                sbDocDown.append(strDocTitle);
                sbDocDown.append("\" />");
                sbDocDown.append("</div>");
            }
            sbDocDown.append("</td>");

            if (isEdit && "edit".equals(strMode)) {
                sbDocDown.append("<td>");
                sbDocDown.append("<a href=\"javascript:removeAfterDocument()\">");
                sbDocDown.append("<img src=\"../common/images/iconStatusRemoved.gif\" width=\"12\" height=\"12\" border=\"0\" />");
                sbDocDown.append("</a>");
                sbDocDown.append("<a href=\"javascript:removeAfterDocument()\">");
                //XSSOK
//                sbDocDown.append(remove);
                sbDocDown.append("</a>");
                sbDocDown.append("</td>");
            }


        } else {
            sbDocDown.append("<table");
            sbDocDown.append("><tr>");
            sbDocDown.append("<td id=\"fileTd\">");
            sbDocDown.append("<input id=\"JFPCRAfterFileId\" name=\"JFPCRAfterFileName\" type=\"hidden\"/>");
            sbDocDown.append("<input onclick=\"javascript:showModalDialog('../components/JF_PCRChangeFilePreCheckin.jsp?objectAction=checkin&amp;flag=after&amp;msfBypass=true&amp;objectId=;");
            sbDocDown.append("','730','450')\"" +
                    "  value=\"");
            _logger.info("strFileUploadNls:{}", strFileUploadNls);
            sbDocDown.append(strFileUploadNls);
            sbDocDown.append("\" type=\"button\"/>");
            sbDocDown.append("</td>");
            sbDocDown.append("<td id=\"JFPCRAfterReplace\">");
            sbDocDown.append("</td>");

            sbDocDown.append("<td>");
            sbDocDown.append("<a href=\"javascript:removeAfterDocument()\">");
            sbDocDown.append("<img src=\"../common/images/iconStatusRemoved.gif\" width=\"12\" height=\"12\" border=\"0\" />");
            sbDocDown.append("</a>");
            sbDocDown.append("<a href=\"javascript:removeAfterDocument()\">");
            //XSSOK
//                sbDocDown.append(remove);
            sbDocDown.append("</a>");
            sbDocDown.append("</td>");
        }
        sbDocDown.append("</tr></table>");
        _logger.info("--------------------- buildECRQQHtml end --------------------------");
        return sbDocDown.toString();
    }

    /*
     * @description:获取当前对象owner的部门
     * @author: caipan
     * @date: 2025/11/3 16:52:17
     * @param: * @param[1] context
     * @param[2] args
     * @return:
     **/
    public Vector getPCRORG(Context context, String[] args) throws Exception {
        _logger.info("getPCRORG");
        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        MapList objectList = (MapList) programMap.get("objectList");
        Vector retVector = new Vector();
        if (objectList != null && objectList.size() > 0) {
            Map objectMap = null;
            DomainObject domainObject = DomainObject.newInstance(context);
            String displayName = "";
            DomainObject person = DomainObject.newInstance(context);
            StringList selList = JF_Util_mxJPO.basicBolistSel();
            StringList relList = JF_Util_mxJPO.basicRellistSel();
            for (int i = 0; i < objectList.size(); i++) {
                objectMap = (Map) objectList.get(i);
                _logger.info("objectMap:{}", objectMap);
                String owner = (String) objectMap.get(DomainConstants.SELECT_OWNER);
                String personId = PersonUtil.getPersonObjectID(context, owner);
                person.setId(personId);
//                displayName =FrameworkUtil.join(person.getInfoList(context, "to[Member|from.type==Department].from.attribute[Title]"),"");
                MapList depList = person.getRelatedObjects(context,
                        RELATIONSHIP_MEMBER, //pattern to match relationships
                        TYPE_DEPARTMENT, //pattern to match types
                        selList, //the eMatrix StringList object that holds the list of select statement pertaining to Business Obejcts.
                        relList, //the eMatrix StringList object that holds the list of select statement pertaining to Relationships.
                        true, //get To relationships
                        false, //get From relationships
                        (short) 1, //the number of levels to expand, 0 equals expand all.
                        EMPTY_STRING, //where clause to apply to objects, can be empty ""
                        null, //where clause to apply to relationship, can be empty ""
                        (short) 0); //limit
                if (depList.size() > 0) {
                    for (int j = 0; j < depList.size(); j++) {
                        Map map = (Map) depList.get(j);
                        if (j != 0) {
                            displayName = displayName + "," + UIUtil.getValue(map, SELECT_ATTRIBUTE_TITLE);
                        } else {
                            displayName = UIUtil.getValue(map, SELECT_ATTRIBUTE_TITLE);
                        }
                    }
                } else {
                    displayName = "";
                }
                retVector.add(displayName);
            }
        }
        return retVector;
    }

    /**
     * @description:创建流程 在IN_Approve 状态创建流程
     * .ProjectPhase==“Phase2”||“Phase3”||“Phase2+3”:新建Route包含3个InboxTask，1：直线经理审核；2：PM审核；3：Launch经理审核  2 3 并行
     * PCR.ProjectPhase==“Phase4” 1：直线经理审核；2：Launch经理审核
     * @author: caipan
     * @date: 2025/11/4 13:39:14
     * @param: * @param[1] context
     * @param[2] args
     * @return:
     **/
    public int createRouteInReview(Context context, String[] args) throws Exception {
        _logger.info("------------------------------- createRouteInReview begin ------------------------------------------------");
        String strObjectId = args[0];
        String error = EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", context.getLocale(), "emxComponents.PCR.error");
        try {
            DomainObject pcrObj = DomainObject.newInstance(context, strObjectId);
            StringList projectList = pcrObj.getInfoList(context, "from[JFChange2Project].to.id");
            StringList selList = new StringList();
            selList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JF_PCRProjectPhase);
            selList.add(SELECT_CURRENT);
            selList.add(SELECT_OWNER);
            Map map = pcrObj.getInfo(context, selList);
            String phase = UIUtil.getValue(map, JF_PLMConstants_mxJPO.SELECT_ATTR_JF_PCRProjectPhase);
            String current = UIUtil.getValue(map, SELECT_CURRENT);
            String owner = UIUtil.getValue(map, SELECT_OWNER);
            _logger.info("current:{}", current);
            String lineManagerID = JF_PublicMethodClass_mxJPO.getPersonLineManager(context, null, owner);
            String lineManagerTitle = EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", context.getLocale(), "emxComponents.DRWManger.ROUTE.lineManager");
            String pmTitle = EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", context.getLocale(), "emxComponents.DOCManger.ROUTE.ProjectManager");
            String launchTitle = EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", context.getLocale(), "emxComponents.PCR.LaunchRouteDescription");
            //流程描述
            String routeDescription = "emxComponents.PCR." + current + ".RouteDescription";
            String strRouteDescription = EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", context.getLocale(), routeDescription);
            String messageLineManager = EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", context.getLocale(), "emxComponents.PCR.messageLineManager");
            String messageProjectRole = EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", context.getLocale(), "emxComponents.PCR.messageProjectRole");
            String messageHead = EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", context.getLocale(), "emxComponents.PCR.RouteMessage");

            String strRouteTitle = "";
            //获取到项目经理和Launch经理
            StringList pmSet = new StringList();
            StringList launchSet = new StringList();
            JF_NewECRProcess_mxJPO newEcr = new JF_NewECRProcess_mxJPO();
            for (int i = 0; i < projectList.size(); i++) {
                _logger.info("projectList:{}", projectList.get(i));
                String pm = JF_Util_mxJPO.getProjectManager(context, new String[]{projectList.get(i)});
                if (!pmSet.contains(pm)) {
                    pmSet.add(pm);
                }
                Map temp = newEcr.getProjectRole(context, projectList.get(i), "Launch manager");
                String launch = UIUtil.getValue(temp, SELECT_NAME);
                if (!launchSet.contains(launch) && UIUtil.isNotNullAndNotEmpty(launch)) {
                    launchSet.add(launch);
                }

            }
            _logger.info("launchSet:{} pmSet:{}", launchSet, pmSet);
            String message = "";
            String routeId = "";
            //校验角色是否缺失
            if (UIUtil.isNullOrEmpty(lineManagerID)) {
                message = message + messageLineManager;
            }
            if (pmSet.size() == 0 || launchSet.size() == 0) {
                if (UIUtil.isNotNullAndNotEmpty(message)) {
                    message = "\\n" + messageProjectRole;
                }
            }
            _logger.info("message:{}", message);
            if (UIUtil.isNotNullAndNotEmpty(message)) {
                message = messageHead + "\\n" + message;
                emxContextUtil_mxJPO.mqlNotice(context, message);
                return 1;
            }
            MapList approveList = new MapList();
            if ("IN_Approve".equalsIgnoreCase(current)) { //第一个流程
                String strPeronId = lineManagerID;
                String strNewTitle = lineManagerTitle;
                Map managerMap = JF_PublicMethodClass_mxJPO.getMapAnyOrAll(strPeronId, strNewTitle, "true", "1", "All");
                approveList.add(managerMap);
                //项目经理
                int sequence=2;
                // update by ljr 20260520  (1)所属项目选项值为Phase2、Phase3、Phase2+3、Phase4时 审批流程:直线经理→PM→Launch经理
//                if ("Phase3".equalsIgnoreCase(phase) || "Phase2".equalsIgnoreCase(phase)|| "Phase4".equalsIgnoreCase(phase)) {
                if ("Phase3".equalsIgnoreCase(phase)  || "Phase2+3".equalsIgnoreCase(phase) || "Phase2".equalsIgnoreCase(phase)|| "Phase4".equalsIgnoreCase(phase)) {
                    for (int i = 0; i < pmSet.size(); i++) {
                        String personName = pmSet.get(i);
                        strPeronId = PersonUtil.getPersonObjectID(context, personName);
                        strNewTitle = pmTitle;
                        managerMap = JF_PublicMethodClass_mxJPO.getMapAnyOrAll(strPeronId, strNewTitle, "true", sequence+"", "All");
                        approveList.add(managerMap);
                    }
                    sequence=sequence+1;
                }
                //项目经理、Launch经理
                for (int i = 0; i < launchSet.size(); i++) {
                    String personName = launchSet.get(i);
                    strPeronId = PersonUtil.getPersonObjectID(context, personName);
                    strNewTitle = launchTitle;
                    managerMap = JF_PublicMethodClass_mxJPO.getMapAnyOrAll(strPeronId, strNewTitle, "true", sequence+"", "All");
                    approveList.add(managerMap);
                }
                JF_Route_mxJPO jf_route = new JF_Route_mxJPO(context, args);
                String state = "state_IN_Approve";//在哪个状态增加流程
                String policy = "policy_JF_PCR";//哪个Policy上面
                _logger.info("approveList:{}", approveList);
                routeId = jf_route.createAndStartRoute(context, approveList, strObjectId, state, policy, strRouteDescription);
            } else if ("IN_Evaluation".equalsIgnoreCase(current)) { //第二个流程
                String strPeronId = "";
                String strNewTitle = "";
                Map managerMap = null;
                //项目经理
                int sequence=1;
                if ("Phase3".equalsIgnoreCase(phase) ||"Phase2+3".equalsIgnoreCase(phase) || "Phase2".equalsIgnoreCase(phase) || "Phase4".equalsIgnoreCase(phase)) {
                    for (int i = 0; i < pmSet.size(); i++) {
                        String personName = pmSet.get(i);
                        strPeronId = PersonUtil.getPersonObjectID(context, personName);
                        strNewTitle = pmTitle;
                        managerMap = JF_PublicMethodClass_mxJPO.getMapAnyOrAll(strPeronId, strNewTitle, "true", sequence+"", "All");
                        approveList.add(managerMap);
                    }
                    sequence=sequence+1;
                }
                //Launch经理
                for (int i = 0; i < launchSet.size(); i++) {
                    String personName = launchSet.get(i);
                    strPeronId = PersonUtil.getPersonObjectID(context, personName);
                    strNewTitle = launchTitle;
                    managerMap = JF_PublicMethodClass_mxJPO.getMapAnyOrAll(strPeronId, strNewTitle, "true", sequence+"", "All");
                    approveList.add(managerMap);
                }
                JF_Route_mxJPO jf_route = new JF_Route_mxJPO(context, args);
                String state = "state_IN_Evaluation";//在哪个状态增加流程
                String policy = "policy_JF_PCR";//哪个Policy上面
                _logger.info("approveList:{}", approveList);
                routeId = jf_route.createAndStartRoute(context, approveList, strObjectId, state, policy, strRouteDescription);
            } else if ("Verification".equalsIgnoreCase(current)) { //第三个流程
                String strPeronId = "";
                String strNewTitle = "";
                Map managerMap = null;
                int sequence=1;
                //项目经理
                if ("Phase3".equalsIgnoreCase(phase) ||"Phase2+3".equalsIgnoreCase(phase) || "Phase2".equalsIgnoreCase(phase) || "Phase4".equalsIgnoreCase(phase)) {
                    for (int i = 0; i < pmSet.size(); i++) {
                        String personName = pmSet.get(i);
                        strPeronId = PersonUtil.getPersonObjectID(context, personName);
                        strNewTitle = pmTitle;
                        managerMap = JF_PublicMethodClass_mxJPO.getMapAnyOrAll(strPeronId, strNewTitle, "true", sequence+"", "All");
                        approveList.add(managerMap);
                    }
                    sequence=sequence+1;
                }
                //Launch经理
                for (int i = 0; i < launchSet.size(); i++) {
                    String personName = launchSet.get(i);
                    strPeronId = PersonUtil.getPersonObjectID(context, personName);
                    strNewTitle = launchTitle;
                    managerMap = JF_PublicMethodClass_mxJPO.getMapAnyOrAll(strPeronId, strNewTitle, "true", sequence+"", "All");
                    approveList.add(managerMap);
                }
                JF_Route_mxJPO jf_route = new JF_Route_mxJPO(context, args);
                String state = "state_Verification";//在哪个状态增加流程
                String policy = "policy_JF_PCR";//哪个Policy上面
                _logger.info("approveList:{}", approveList);
                routeId = jf_route.createAndStartRoute(context, approveList, strObjectId, state, policy, strRouteDescription);
            } else if ("Execution".equalsIgnoreCase(current)) { //第四个流程
                String strPeronId = "";
                String strNewTitle = "";
                Map managerMap = null;
                //项目经理
                int sequence=1;
          /*      if ("Phase3".equalsIgnoreCase(phase) || "Phase2".equalsIgnoreCase(phase)) {
                    for (int i = 0; i < pmSet.size(); i++) {
                        String personName = pmSet.get(i);
                        strPeronId = PersonUtil.getPersonObjectID(context, personName);
                        strNewTitle = pmTitle;
                        managerMap = JF_PublicMethodClass_mxJPO.getMapAnyOrAll(strPeronId, strNewTitle, "true", sequence+"", "All");
                        approveList.add(managerMap);
                    }
                    sequence=sequence+1;
                }
                //Launch经理
                else {*/
                    for (int i = 0; i < launchSet.size(); i++) {
                        String personName = launchSet.get(i);
                        strPeronId = PersonUtil.getPersonObjectID(context, personName);
                        strNewTitle = launchTitle;
                        managerMap = JF_PublicMethodClass_mxJPO.getMapAnyOrAll(strPeronId, strNewTitle, "true", sequence+"", "All");
                        approveList.add(managerMap);
                    }
//                }
                JF_Route_mxJPO jf_route = new JF_Route_mxJPO(context, args);
                String state = "state_Execution";//在哪个状态增加流程
                String policy = "policy_JF_PCR";//哪个Policy上面
                _logger.info("approveList:{}", approveList);
                routeId = jf_route.createAndStartRoute(context, approveList, strObjectId, state, policy, strRouteDescription);
            }
            _logger.info("------------------------------- createRouteInReview end ------------------------------------------------{}", routeId);
        } catch (Exception e) {
            _logger.error("PCR createRouteInReview error:{}", e.getMessage());
            emxContextUtil_mxJPO.mqlNotice(context, error);
            return 1;
        }
        return 0;
    }

    /*
     * @description:创建评估任务
     * @author: caipan
     * @date: 2025/11/5 13:33:48
     * @param: * @param[1] context
     * @param[2] args
     * @return:
     **/
    public void createPCRTask(Context context, String[] args) throws Exception {
        {
            try {
                MapList list = readSignTaskPropertiesFile(context, STRING_SIGN_TASK_PROPERTIES_ZH, "JFPCRTask");//拿到配置的角色
                _logger.info("list:{}", list);
                //获取系统里面的角色人员组装成Map
                String id = args[0];
                DomainObject pcrObj = DomainObject.newInstance(context, id);
                String projectId = pcrObj.getInfo(context, "from[JFChange2Project].to.id");
                for (int i = 0; i < list.size(); i++) {
                    Map requestMap = (Map) list.get(i);
                    String role = UIUtil.getValue(requestMap, "id");
                    String title = UIUtil.getValue(requestMap, "title");
                    String stdname = getProjectRoleName(context, new String[]{projectId, role});
                    if (UIUtil.isNotNullAndNotEmpty(stdname)) {
                        Map map = new HashMap();
                        map.put("taskName", title);//任务标题
                        map.put("taskType", SYMBOLIC_Type_JFPCRTask);//任务类型 注册名
                        map.put("taskOwner", stdname);  //ECO的owner
                        map.put("parentId", id);
                    /*if (flag) {
                        map.put("assign", "false");
                    }*/
                        //创建PCRTask任务实体
                        JF_Util_mxJPO jfUtilMxJPO = new JF_Util_mxJPO();
                        String taskId = jfUtilMxJPO.createTask(context, JPO.packArgs(map));
                        DomainObject taskObj = new DomainObject(taskId);
                        // PCR与创建的PCRTask连接关系
                        DomainRelationship.connect(context, pcrObj, JF_PLMConstants_mxJPO.Rel_JFPCR2Task, taskObj);
//                        taskObj.setState(context, "Active");//创建完成就设置到工作中
                        setStateMql(context,new String[]{taskId,"Active"});
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /*
     * @description:获取配置文件配置的角色和标题       MapList mapList = readSignTaskPropertiesFile(context, STRING_SIGN_TASK_PROPERTIES_ZH, "signTask");
     * @author: caipan
     * @date: 2025/11/5 16:11:09
     * @param: * @param[1] context
     * @param[2] pageName
     * @param[3] configuration
     * @return: 角色和Title列表
     **/
    public MapList readSignTaskPropertiesFile(Context context, String pageName, String configuration) throws Exception {
        MapList mapList = new MapList();
        Page pageAttributePopulation = new Page(pageName);
        pageAttributePopulation.open(context);
        String strProperties = pageAttributePopulation.getContents(context);
        pageAttributePopulation.close(context);
        String createTaskType = configuration;
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
                System.out.println("configurationElement:" + configurationElement.toString());
                if (createTaskType.equals(configurationElement.getAttribute("id"))) {
                    // 找到匹配的 configuration, 获取它下的 config 元素
                    NodeList configNodes = configurationElement.getElementsByTagName("config");
                    for (int j = 0; j < configNodes.getLength(); j++) {
                        Element configElement = (Element) configNodes.item(j);
                        String id = configElement.getAttribute("id");
                        String title = configElement.getAttribute("title");
                        HashMap<String, String> map = new HashMap<>();
                        map.put("id", id);
                        map.put("title", title);
                        mapList.add(map);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return mapList;
    }

    /*
     * @description:获取项目角色
     * @author: caipan
     * @date: 2025/11/5 16:38:45
     * @param: * @param[1] context
     * @param[2] args
     * @return:
     **/
    public static String getProjectRoleName(Context context, String[] args) throws Exception {
        String projectId = args[0];
        String projectRole = args[1];
        String accout = "";
        try {
            ContextUtil.pushContext(context);
            StringList relList = JF_Util_mxJPO.basicRellistSel();
            relList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_ProjectRole);
            DomainObject projectObj = DomainObject.newInstance(context, projectId);
            String relWhere = JF_PLMConstants_mxJPO.SELECT_ATTR_ProjectRole + "=='" + projectRole + "'";
            MapList objectList = projectObj.getRelatedObjects(context,
                    DomainConstants.RELATIONSHIP_MEMBER, //pattern to match relationships
                    DomainConstants.TYPE_PERSON, //pattern to match types
                    JF_Util_mxJPO.basicBolistSel(), //the eMatrix StringList object that holds the list of select statement pertaining to Business Obejcts.
                    relList, //the eMatrix StringList object that holds the list of select statement pertaining to Relationships.
                    false, //get To relationships
                    true, //get From relationships
                    (short) 1, //the number of levels to expand, 0 equals expand all.
                    DomainConstants.EMPTY_STRING, //where clause to apply to objects, can be empty ""
                    relWhere, //where clause to apply to relationship, can be empty ""
                    (short) 1 //limit
            );
//            log.info("objectList:{}",objectList);
            if (objectList.size() == 0) {
                //拿项目的owner
            } else {
                Map map = (Map) objectList.get(0);
                accout = UIUtil.getValue(map, DomainConstants.SELECT_NAME);
            }
        } finally {
            ContextUtil.popContext(context);
        }
        _logger.info("账号:{}", accout);
        return accout;
    }

    /*
     * @description:获取评估任务
     * @author: caipan
     * @date: 2025/11/6 14:54:42
     * @param: * @param[1] context
     * @param[2] args
     * @return:
     **/
    public MapList getEvaluateTask(Context context, String[] args) throws Exception {
        Map paramsMap = (Map) JPO.unpackArgs(args);
        String strObjectId = (String) paramsMap.get(STRING_OBJECTID);
        StringList selList = JF_Util_mxJPO.basicBolistSel();
        StringList relList = JF_Util_mxJPO.basicRellistSel();
        DomainObject pcrObj = DomainObject.newInstance(context, strObjectId);
        MapList list = pcrObj.getRelatedObjects(context,
                JF_PLMConstants_mxJPO.Rel_JFPCR2Task, //pattern to match relationships
                JF_PLMConstants_mxJPO.Type_JFPCRTask, //pattern to match types
                selList, //the eMatrix StringList object that holds the list of select statement pertaining to Business Obejcts.
                relList, //the eMatrix StringList object that holds the list of select statement pertaining to Relationships.
                false, //get To relationships
                true, //get From relationships
                (short) 1, //the number of levels to expand, 0 equals expand all.
                EMPTY_STRING, //where clause to apply to objects, can be empty ""
                null, //where clause to apply to relationship, can be empty ""
                (short) 0); //limit
        _logger.info("list:{}", list);
        return list;
    }

    /*
     * @description:获取执行任务
     * @author: caipan
     * @date: 2025/11/7 15:42:14
     * @param: * @param[1] context
     * @param[2] args
     * @return:
     **/
    public MapList getExecuteTask(Context context, String[] args) throws Exception {
        Map paramsMap = (Map) JPO.unpackArgs(args);
        String strObjectId = (String) paramsMap.get(STRING_OBJECTID);
        StringList selList = JF_Util_mxJPO.basicBolistSel();
        selList.add("attribute[JF_PCRFunction]");
        StringList relList = JF_Util_mxJPO.basicRellistSel();
        DomainObject pcrObj = DomainObject.newInstance(context, strObjectId);
        MapList list = pcrObj.getRelatedObjects(context,
                JF_PLMConstants_mxJPO.Rel_JF_PCR2ExecuteTask, //pattern to match relationships
                JF_PLMConstants_mxJPO.Type_JF_PCRExecuteTask, //pattern to match types
                selList, //the eMatrix StringList object that holds the list of select statement pertaining to Business Obejcts.
                relList, //the eMatrix StringList object that holds the list of select statement pertaining to Relationships.
                false, //get To relationships
                true, //get From relationships
                (short) 1, //the number of levels to expand, 0 equals expand all.
                EMPTY_STRING, //where clause to apply to objects, can be empty ""
                null, //where clause to apply to relationship, can be empty ""
                (short) 0); //limit
        _logger.info("list:{}", list);
        return list;
    }

    /*
     * @description:获取验证任务
     * @author: caipan
     * @date: 2025/11/7 15:42:14
     * @param: * @param[1] context
     * @param[2] args
     * @return:
     **/
    public MapList getVerificationTask(Context context, String[] args) throws Exception {
        Map paramsMap = (Map) JPO.unpackArgs(args);
        String strObjectId = (String) paramsMap.get(STRING_OBJECTID);
        StringList selList = JF_Util_mxJPO.basicBolistSel();
        StringList relList = JF_Util_mxJPO.basicRellistSel();
        DomainObject pcrObj = DomainObject.newInstance(context, strObjectId);
        MapList list = pcrObj.getRelatedObjects(context,
                JF_PLMConstants_mxJPO.Rel_JF_PCR2VerificationTask, //pattern to match relationships
                JF_PLMConstants_mxJPO.Type_JF_PCRVerificationTask, //pattern to match types
                selList, //the eMatrix StringList object that holds the list of select statement pertaining to Business Obejcts.
                relList, //the eMatrix StringList object that holds the list of select statement pertaining to Relationships.
                false, //get To relationships
                true, //get From relationships
                (short) 1, //the number of levels to expand, 0 equals expand all.
                EMPTY_STRING, //where clause to apply to objects, can be empty ""
                null, //where clause to apply to relationship, can be empty ""
                (short) 0); //limit
        _logger.info("list:{}", list);
        return list;
    }

    public MapList getTaskLib(Context context, String[] args) throws Exception {
        Map argsMap = JPO.unpackArgs(args);
        StringBuilder sbWhere = new StringBuilder();
        StringList boSel = JF_Util_mxJPO.basicBolistSel();
        boSel.add(DomainConstants.SELECT_ID);
        boSel.add(DomainConstants.SELECT_DESCRIPTION);
        boSel.add(DomainConstants.SELECT_NAME);
        boSel.add("attribute[Title]");
        StringList relSelect = new StringList();
        relSelect.add("from.id");
        MapList classMapList = null;
        Properties properties = JF_Util_mxJPO.readPageObject(context, "JFJDConfig");
        String libraryName = properties.getProperty("Library.PCR.TaskLib");
        sbWhere.append("attribute[Title]");
        sbWhere.append(" == '");
        sbWhere.append(libraryName);
        sbWhere.append("'");
        _logger.info("sbwhere:{}", sbWhere);
        MapList librarys = DomainObject.findObjects(context, "General Library", "*", sbWhere.toString(), boSel);
        for (int i = 0; i < librarys.size(); i++) {
            Map library = (Map) librarys.get(i);
            library.put("disableSelection", "true");
        }
        return librarys;
    }

    public MapList getChild(Context context, String[] args) throws Exception {
        Map paramMap = JPO.unpackArgs(args);
        String objId = (String) paramMap.get("objectId");
        //标识是零件还是总成
        DomainObject object = DomainObject.newInstance(context, objId);

        StringList boSel = JF_Util_mxJPO.basicBolistSel();
        boSel.add(DomainConstants.SELECT_ATTRIBUTE_TITLE);
        boSel.add("from[Subclass]");
        MapList classMapList = object.getRelatedObjects(context, "Subclass", "*", boSel, null, false, true, (short) 1, "current==Active", "", 0);
        for (int i = 0; i < classMapList.size(); i++) {
            Map temp = (Map) classMapList.get(i);
            String hasChild = (String) temp.get("from[Subclass]");
            if ("TRUE".equalsIgnoreCase(hasChild)) {
                temp.put("disableSelection", "true");
            }
        }
        _logger.info("classMapList:{}", classMapList);
        return classMapList;
    }

    /*
     * @description:创建执行任务
     * @author: caipan
     * @date: 2025/11/12 15:03:23
     * @param: * @param[1] context
     * @param[2] args
     * @return:
     **/
    public void createPCRExecuteTask(Context context, String[] args) throws Exception {
        {
            try {
                _logger.info("createPCRExecuteTask ");
                String loginOwner = context.getUser();
                Map requestMap = JPO.unpackArgs(args);
                _logger.info("requestMap:{}", requestMap);
                String objectId = UIUtil.getValue(requestMap, "objectId");
                String[] list = (String[]) requestMap.get("list");
                DomainObject pcrObj = DomainObject.newInstance(context, objectId);
                DomainObject libObj = DomainObject.newInstance(context);
                for (int i = 0; i < list.length; i++) {
                    libObj.setId(list[i]);
                    String title = libObj.getAttributeValue(context, DomainConstants.ATTRIBUTE_TITLE);
                    //需要获取到父，拿到Title
                    StringList parentList = libObj.getInfoList(context,"to[Subclass].from.attribute[Title]");
                    Map map = new HashMap();
                    map.put("taskName", title);//任务标题
                    map.put("taskType", SYMBOLIC_JF_PCRExecuteTask);//任务类型 注册名
                    map.put("taskOwner", loginOwner);  //ECO的owner
                    map.put("parentId", objectId);
                    //创建PCRTask任务实体
                    JF_Util_mxJPO jfUtilMxJPO = new JF_Util_mxJPO();
                    String taskId = jfUtilMxJPO.createTask(context, JPO.packArgs(map));
                    DomainObject taskObj = new DomainObject(taskId);
                    // PCR与创建的PCRTask连接关系
                    DomainRelationship.connect(context, pcrObj, JF_PLMConstants_mxJPO.Rel_JF_PCR2ExecuteTask, taskObj);
                    taskObj.setState(context, "Assign");//创建完成就设置到分配中
                    if(parentList.size()>0){
                        taskObj.setAttributeValue(context,"JF_PCRFunction",parentList.get(0));
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /*
     * @description:评估任务table的权限控制
     * @author: caipan
     * @date: 2025/11/12 16:25:30
     * @param: * @param[1] context
     * @param[2] args
     * @return:
     **/
    public StringList gePCRTaskAccess(Context context, String[] args) throws Exception {
        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        Map requestMap = (Map) programMap.get(STRING_REQUESTMAP);
        String strLoginUser = context.getUser();
        MapList objectList = (MapList) programMap.get("objectList");
        StringList editAccessList = new StringList(objectList.size());
        for (int i = 0; i < objectList.size(); i++) {
            Map objectMap = (Map) objectList.get(i);
            String owner = (String) objectMap.get(SELECT_OWNER);
            String current = (String) objectMap.get(SELECT_CURRENT);
            if(current.equalsIgnoreCase("Active")) {
                if (strLoginUser.equalsIgnoreCase(owner)) {
                    editAccessList.add("true");
                } else {
                    editAccessList.add("false");
                }
            }else{
                editAccessList.add("false");
            }
        }
        return editAccessList;
    }

    /*
     * @description:获取PCR关联的评估任务，是否全部完成
     * @author: caipan
     * @date: 2025/11/12 17:50:51
     * @param: * @param[1] context
     * @param[2] args
     * @return:
     **/
    public void getActiveEvaluateTask(Context context, String[] args) throws Exception {
        _logger.info("getActiveEvaluateTask");
        String objectId = args[0];
        DomainObject pcrObj = DomainObject.newInstance(context, objectId);
        StringList selList = JF_Util_mxJPO.basicBolistSel();
        StringList relList = JF_Util_mxJPO.basicRellistSel();
        String where = "current==Assign || current==Active";
        MapList list = pcrObj.getRelatedObjects(context,
                JF_PLMConstants_mxJPO.Rel_JFPCR2Task, //pattern to match relationships
                JF_PLMConstants_mxJPO.Type_JFPCRTask, //pattern to match types
                selList, //the eMatrix StringList object that holds the list of select statement pertaining to Business Obejcts.
                relList, //the eMatrix StringList object that holds the list of select statement pertaining to Relationships.
                false, //get To relationships
                true, //get From relationships
                (short) 1, //the number of levels to expand, 0 equals expand all.
                where, //where clause to apply to objects, can be empty ""
                null, //where clause to apply to relationship, can be empty ""
                (short) 0); //limit
        if (list.size() == 0) {
            //创建流程
            _logger.info("create 流程");
            createRouteInReview(context, args);
        }

    }

    /*
     * @description:到达更改验证状态，发送邮件给owner
     * @author: caipan
     * @date: 2025/11/13 15:26:24
     * @param: * @param[1] context
     * @param[2] args
     * @return:
     **/
    public void sendEmailTOOwner(Context context, String[] args) throws Exception {

    }

    /*
     * @description:只是需要验证command的下拉值
     * @author: caipan
     * @date: 2025/11/13 16:14:16
     * @param: * @param[1] context
     * @param[2] args
     * @return:
     **/
    public Map getNeedValidationRange(Context context, String[] args) throws Exception {
        Map policyMap = new HashMap();
        Map programMap = (Map) JPO.unpackArgs(args);
        Map requestMap = (Map) programMap.get("requestMap");
        Map paramMap = (Map) programMap.get("paramMap");
        String sLanguage = (String) paramMap.get("languageStr");
        String objectId = (String) requestMap.get("objectId");
        String Y = (String) EnoviaResourceBundle.getFrameworkStringResourceProperty(context, "emxFramework.Range.JF_NeedValidation.Y", context.getLocale());
        String N = (String) EnoviaResourceBundle.getFrameworkStringResourceProperty(context, "emxFramework.Range.JF_NeedValidation.N", context.getLocale());

        DomainObject projectObj = DomainObject.newInstance(context, objectId);
        String supply = projectObj.getAttributeValue(context, JF_PLMConstants_mxJPO.ATTR_JF_NeedValidation);
        _logger.info("supply:{}",supply);
        StringList keyList = new StringList();
        StringList valueList = new StringList();
        if ("N".equalsIgnoreCase(supply)) {
            keyList.add("N");
            valueList.add(N);
            keyList.add("Y");
            valueList.add(Y);
        } else if ("Y".equalsIgnoreCase(supply)) {
            keyList.add("Y");
            valueList.add(Y);
            keyList.add("N");
            valueList.add(N);
        } else {
            keyList.add("");
            valueList.add("");
            keyList.add("N");
            valueList.add(N);
            keyList.add("Y");
            valueList.add(Y);
        }
        policyMap.put("field_choices", keyList);
        policyMap.put("field_display_choices", valueList);
        return policyMap;
    }

    /*
     * @description:验证结论
     * @author: caipan
     * @date: 2025/11/13 16:18:00
     * @param: * @param[1] context
     * @param[2] args
     * @return:
     **/
    public Map getvalidateConclusionRange(Context context, String[] args) throws Exception {
        Map policyMap = new HashMap();
        Map programMap = (Map) JPO.unpackArgs(args);
        Map requestMap = (Map) programMap.get("requestMap");
        Map paramMap = (Map) programMap.get("paramMap");
        String sLanguage = (String) paramMap.get("languageStr");
        String objectId = (String) requestMap.get("objectId");
        String Success = (String) EnoviaResourceBundle.getFrameworkStringResourceProperty(context, "emxFramework.Range.JF_validateConclusion.Success", context.getLocale());
        String Fail = (String) EnoviaResourceBundle.getFrameworkStringResourceProperty(context, "emxFramework.Range.JF_validateConclusion.Fail", context.getLocale());

        DomainObject projectObj = DomainObject.newInstance(context, objectId);
        String supply = projectObj.getAttributeValue(context, JF_PLMConstants_mxJPO.ATTR_JF_validateConclusion);
        StringList keyList = new StringList();
        StringList valueList = new StringList();
        if ("Success".equalsIgnoreCase(supply)) {
            keyList.add("Success");
            valueList.add(Success);
            keyList.add("Fail");
            valueList.add(Fail);
        } else if ("Fail".equalsIgnoreCase(supply)) {
            keyList.add("Fail");
            valueList.add(Fail);
            keyList.add("Success");
            valueList.add(Success);
//            keyList.add("N");
//            valueList.add(N);
        } else {
            keyList.add("");
            valueList.add("");
            keyList.add("Fail");
            valueList.add(Fail);
            keyList.add("Success");
            valueList.add(Success);
        }
        policyMap.put("field_choices", keyList);
        policyMap.put("field_display_choices", valueList);
        return policyMap;
    }
    /*
     * @description:创建验证任务
     * @author: caipan
     * @date: 2025/11/24 10:42:53
     * @param: * @param[1] context
     * @param[2] args
     * @return:
     **/
    public void createPCRVerificationTask(Context context, String[] args) throws Exception {
        {
            try {


                MapList list = readSignTaskPropertiesFile(context, STRING_SIGN_TASK_PROPERTIES_ZH, "JFPCRVerificationTask");//拿到配置的角色
                _logger.info("list:{}", list);
                //获取系统里面的角色人员组装成Map
                String id = args[0];
                String loginUser = context.getUser();
                DomainObject pcrObj = DomainObject.newInstance(context, id);
                //如果存在已经创建的，就不重复创建
                StringList verificaList = pcrObj.getInfoList(context,"from["+ JF_PLMConstants_mxJPO.Rel_JF_PCR2VerificationTask+"].to.id");
                _logger.info("verificaList.size:{}",verificaList);
                if(verificaList.size()==0) {
                    String projectId = pcrObj.getInfo(context, "from[JFChange2Project].to.id");
                    for (int i = 0; i < 1; i++) {//目前只增加一条验证记录
                        Map requestMap = (Map) list.get(i);
                        String role = UIUtil.getValue(requestMap, "id");
                        String title = UIUtil.getValue(requestMap, "title");
                        if (UIUtil.isNotNullAndNotEmpty(title)) {
                            Map map = new HashMap();
                            map.put("taskName", title);//任务标题
                            map.put("taskType", SYMBOLIC_JF_PCRVerificationTask);//任务类型 注册名
                            map.put("taskOwner", loginUser);  //ECO的owner
                            map.put("parentId", id);
                            //创建PCRTask任务实体
                            JF_Util_mxJPO jfUtilMxJPO = new JF_Util_mxJPO();
                            String taskId = jfUtilMxJPO.createTask(context, JPO.packArgs(map));
                            DomainObject taskObj = new DomainObject(taskId);
                            // PCR与创建的PCRTask连接关系
                            DomainRelationship.connect(context, pcrObj, JF_PLMConstants_mxJPO.Rel_JF_PCR2VerificationTask, taskObj);
//                        taskObj.setState(context, "Active");//创建完成就设置到工作中
                            setStateMql(context, new String[]{taskId, "Active"});
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    /*
     * @description:获取PCR任务状态
     * @author: caipan
     * @date: 2025/11/20 10:53:54
     * @param: * @param[1] context
     * @param[2] args
     * @return:
     **/
    public StringList pcrTaskState(Context context, String[] args) throws Exception {
        StringList resList = new StringList();
        String inwork = (String) EnoviaResourceBundle.getFrameworkStringResourceProperty(context, "emxFramework.PCRtaskState.inwork", context.getLocale());
        String review = (String) EnoviaResourceBundle.getFrameworkStringResourceProperty(context, "emxFramework.PCRtaskState.review", context.getLocale());
        String Complete = (String) EnoviaResourceBundle.getFrameworkStringResourceProperty(context, "emxFramework.State.iwSGProjectTask.Complete", context.getLocale());
        _logger.info("Complete:{}",Complete);
        HashMap argMaps = JPO.unpackArgs(args);
        MapList objectList = (MapList) argMaps.get("objectList");
        StringBuffer buffer = new StringBuffer();
        DomainObject taskObj = DomainObject.newInstance(context);
        for (int i = 0; i < objectList.size(); i++) {
            Map map= (Map) objectList.get(i);
            String id = (String) map.get(DomainConstants.SELECT_ID);
            taskObj.setId(id);
            String current = taskObj.getInfo(context, SELECT_CURRENT);
            if(current.equalsIgnoreCase("Review")){
                resList.add(review);
            }else if(current.equalsIgnoreCase("Assign")||current.equalsIgnoreCase("Active")||current.equalsIgnoreCase("Create")){
                resList.add(inwork);
            }else{
                resList.add(Complete);
            }
        }
        return resList;
    }
    /*
     * @description:审核中的人员有权限驳回
     * @author: caipan
     * @date: 2025/11/20 11:18:13
     * @param: * @param[1] context
     * @param[2] args
     * @return:
     **/
    public boolean isPM(Context context,String[] args) throws Exception{
        String user = context.getUser();
        HashMap inputMap = (HashMap) JPO.unpackArgs(args);
        HashMap requestMap = (HashMap) inputMap.get("requestMap");
        String strObjectId = null;
        if (requestMap == null) {
            strObjectId = (String) inputMap.get("objectId");
        } else {
            strObjectId = (String) requestMap.get("objectId");
        }
       return isProcessUnderApproval(context,new String[]{strObjectId});
    }
    /*
     * @description:判断PCR在当前状态的审核流程里面的人员，是否和当前登录人匹配
     * @author: caipan
     * @date: 2025/11/20 11:21:26
     * @param: * @param[1] context
     * @param[2] args
     * @return:
     **/
    public boolean isProcessUnderApproval(Context context,String[] args) throws Exception{
        //审核状态
        String id = args[0];
        String strLoginUser = context.getUser();
        DomainObject PCRBO = DomainObject.newInstance(context,id);
        StringList  selList = JF_Util_mxJPO.basicBolistSel();
        StringList  relList = JF_Util_mxJPO.basicRellistSel();
        relList.add("attribute[Route Base State]");
        String current = PCRBO.getInfo(context, SELECT_CURRENT);
        String policy  = PCRBO.getInfo(context, SELECT_POLICY);
        String symbolState = FrameworkUtil.reverseLookupStateName(context, policy,current);
        String relwhere = "attribute[Route Base State]=="+symbolState;
        //得到当前状态下的Route
         MapList mapList =  PCRBO.getRelatedObjects(context,
                RELATIONSHIP_OBJECT_ROUTE, //pattern to match relationships
                TYPE_ROUTE, //pattern to match types
                selList, //the eMatrix StringList object that holds the list of select statement pertaining to Business Obejcts.
                relList, //the eMatrix StringList object that holds the list of select statement pertaining to Relationships.
                false, //get To relationships
                true, //get From relationships
                (short) 1, //the number of levels to expand, 0 equals expand all.
                EMPTY_STRING, //where clause to apply to objects, can be empty ""
                relwhere, //where clause to apply to relationship, can be empty ""
                (short) 0); //limit
        _logger.info("isProcessUnderApproval maplist:{}",mapList);
        MapList tasklist = new MapList();
        if(mapList.size()>0) {
            Map routeMap = (Map)mapList.get(0);
            DomainObject routeObj = DomainObject.newInstance(context);
            String where = "owner=='" + strLoginUser + "' && current!=Complete";
                routeObj.setId(UIUtil.getValue(routeMap, SELECT_ID));
                MapList list = routeObj.getRelatedObjects(context,
                        RELATIONSHIP_ROUTE_TASK, //pattern to match relationships
                        TYPE_INBOX_TASK, //pattern to match types
                        JF_Util_mxJPO.basicBolistSel(), //the eMatrix StringList object that holds the list of select statement pertaining to Business Obejcts.
                        JF_Util_mxJPO.basicRellistSel(), //the eMatrix StringList object that holds the list of select statement pertaining to Relationships.
                        true, //get To relationships
                        false, //get From relationships
                        (short) 1, //the number of levels to expand, 0 equals expand all.
                        where, //where clause to apply to objects, can be empty ""
                        null, //where clause to apply to relationship, can be empty ""
                        (short) 0); //limit
                if (list.size() > 0) {
                    tasklist.addAll(list);
                }
        }
        return tasklist.size()>0;
    }
    /*
     * @description:检查验证任务是否已经创建
     * @author: caipan
     * @date: 2025/11/24 11:45:26
     * @param: * @param[1] context
     * @param[2] args
     * @return:
     **/
    public boolean checkPCRVerificationTask(Context context,String[] args) throws Exception{
        String objectId = args[0];
        DomainObject obj = DomainObject.newInstance(context,objectId);
        StringList listTask = obj.getInfoList(context,"from[JF_PCR2VerificationTask].to.id");
        return listTask.size()==0;
    }

    /*
     * @description:检查是否可以点击提交验证结论
     * @author: caipan
     * @date: 2025/11/24 14:43:50
     * @param: * @param[1] context
     * @param[2] args
     * @return:
     **/
    public Map checkJFPCRValidateConclusionSubmitCmd(Context context,String[] args) throws Exception{
        String objectId = args[0];
        Map map = new HashMap();
        boolean flag = true;
        DomainObject obj = DomainObject.newInstance(context,objectId);
        String message = "";
        String message1= (String) EnoviaResourceBundle.getFrameworkStringResourceProperty(context, "emxFramework.PCR.ValidateTask1", context.getLocale());
        String message2= (String) EnoviaResourceBundle.getFrameworkStringResourceProperty(context, "emxFramework.PCR.ValidateTask2", context.getLocale());
        //JF_NeedValidation必须等于Y
        String strNeedValidate = obj.getAttributeValue(context,"JF_NeedValidation");
        StringList selList = JF_Util_mxJPO.basicBolistSel();
        selList.add(JF_PLMConstants_mxJPO.SELECT_Attr_JF_Participant);
        selList.add(JF_PLMConstants_mxJPO.SELECT_Attr_JF_Content);
        selList.add(JF_PLMConstants_mxJPO.SELECT_Attr_JF_Record);
        selList.add(JF_PLMConstants_mxJPO.SELECT_Attr_JF_DateOfSignature);
        StringBuffer where= new StringBuffer();
        where.append(JF_PLMConstants_mxJPO.SELECT_Attr_JF_Participant).append("==''");
        where.append("||");
        where.append(JF_PLMConstants_mxJPO.SELECT_Attr_JF_Content).append("==''");
     /*   where.append("||");
        where.append(JF_PLMConstants_mxJPO.SELECT_Attr_JF_Record).append("==''");*/
        where.append("||");
        where.append(JF_PLMConstants_mxJPO.SELECT_Attr_JF_DateOfSignature).append("==''");
        if("Y".equalsIgnoreCase(strNeedValidate)){
            //关联的验证任务必须全部填写完成
            MapList list = obj.getRelatedObjects(context,
                JF_PLMConstants_mxJPO.Rel_JF_PCR2VerificationTask, //pattern to match relationships
                JF_PLMConstants_mxJPO.Type_JF_PCRVerificationTask, //pattern to match types
                selList, //the eMatrix StringList object that holds the list of select statement pertaining to Business Obejcts.
                JF_Util_mxJPO.basicRellistSel(), //the eMatrix StringList object that holds the list of select statement pertaining to Relationships.
                false, //get To relationships
                true, //get From relationships
                (short) 1, //the number of levels to expand, 0 equals expand all.
                    where.toString(), //where clause to apply to objects, can be empty ""
                null, //where clause to apply to relationship, can be empty ""
                (short) 0); //limit
            if(list.size()>0){
                flag= false;
                message = message2;
            }
        }else{
            flag = false;
            message = message1;
        }
        map.put("flag",flag);
        map.put("message",message);
        return map;
    }

    /*
     * @description:设置PCR下一个状态
     *  如果 JF_NeedValidation 为Y JF_validateConclusion 为Success  正常到下一个状态 Execution
     *  如果 JF_NeedValidation 为Y JF_validateConclusion 为Fail 直接到关闭状态 Closed
     * @author: caipan
     * @date: 2025/11/24 16:45:30
     * @param: * @param[1] context
     * @param[2] args
     * @return:
     **/
    public  void setPCRState(Context context,String[] args) throws Exception {
        String objectId = args[0];
        DomainObject obj = DomainObject.newInstance(context, objectId);
        String Conclusion = obj.getAttributeValue(context, JF_PLMConstants_mxJPO.ATTR_JF_validateConclusion);
        boolean ispush = false;
        try{
        ContextUtil.pushContext(context);
            ispush = true;
        if ("Fail".equalsIgnoreCase(Conclusion)) {
            //如果有执行任务是否直接删除掉?
            StringList executeList = obj.getInfoList(context, "from[JF_PCR2ExecuteTask].to.id");
            DomainObject.deleteObjects(context,executeList.toStringArray());
            //直接把PCR设置到关闭状态
            MqlUtil.mqlCommand(context, false, "mod bus " + objectId + " current Closed", true);
        }else{
            obj.setState(context,"Execution");
            //把所有的验证任务都提升到完成
            StringList verificationList = obj.getInfoList(context, "from[JF_PCR2VerificationTask].to.id");
            for(int i=0;i<verificationList.size();i++){
                MqlUtil.mqlCommand(context, false, "mod bus " + verificationList.get(i) + " current Complete", true);
                //把验证任务关联的文档提升到发布
                obj.setId(verificationList.get(i));
                StringList docList = obj.getInfoList(context,"from[Reference Document].to.id");
                for (int j = 0; j <docList.size() ; j++) {
                    MqlUtil.mqlCommand(context, false, "mod bus " + docList.get(j) + " current RELEASED", true);
                }
            }
        }
    }finally {
          if(ispush){
              ContextUtil.pushContext(context);
          }
        }

    }
    
    /*
     * @description:是否显示JFPCRBreakpointSwitchingDateSubmitCmd按钮
     * @author: caipan
     * @date: 2025/11/24 17:37:17
     * @param: * @param[1] context
     * @param[2] args
     * @return: 
     **/
    public boolean isJFPCRBreakpointSwitchingDateSubmitCmdShow(Context context,String[] args) throws Exception{
        boolean flag = false;
        Map inputMap = (Map) JPO.unpackArgs(args);
        HashMap requestMap = (HashMap) inputMap.get("requestMap");
        _logger.info("requestMap:{}",requestMap);
        String pmTitle_zh = EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", new Locale("zh_CN"), "emxComponents.DOCManger.ROUTE.ProjectManager");
        String pmTitle_en = EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", new Locale("en"), "emxComponents.DOCManger.ROUTE.ProjectManager");
        String strObjectId="";
        if (requestMap == null) {
            strObjectId = (String) inputMap.get("objectId");
        } else {
            strObjectId = (String) requestMap.get("objectId");
        }
        if(UIUtil.isNotNullAndNotEmpty(strObjectId)) {
            DomainObject obj = DomainObject.newInstance(context, strObjectId);
            String phase = obj.getAttributeValue(context,ATTR_JF_PCRProjectPhase);
            //Phase2/Phase3由【项目经理】进行确认；---有两个审批人员,需要确定是哪一个
         /*   if("Phase2".equalsIgnoreCase(phase)||"Phase3".equalsIgnoreCase(phase)){
                //判断审核中的人员是否是PM
                flag =isProcessUnderApprovalAndTitle(context,new String[]{strObjectId,pmTitle_zh,pmTitle_en});
            }else{*/
                //Phase4由【Launch经理】进行确认---只有一个审批人员
                flag =isProcessUnderApproval(context,new String[]{strObjectId});
//            }
        }
        return flag;
    }
    /*
     * @description:判断当前状态下，是否有符合该流程标题的审核任务
     * @author: caipan
     * @date: 2025/11/24 17:49:28
     * @param: * @param[1] context
     * @param[2] args
     * @return:
     **/
    public boolean isProcessUnderApprovalAndTitle(Context context,String[] args) throws Exception{
        //审核状态
        String id = args[0];
        String zhTitle = args[1];
        String enTitle = args[2];
        String strLoginUser = context.getUser();
        DomainObject PCRBO = DomainObject.newInstance(context,id);
        StringList  selList = JF_Util_mxJPO.basicBolistSel();
        StringList  relList = JF_Util_mxJPO.basicRellistSel();
        relList.add("attribute[Route Base State]");
        String current = PCRBO.getInfo(context, SELECT_CURRENT);
        String policy  = PCRBO.getInfo(context, SELECT_POLICY);
        String symbolState = FrameworkUtil.reverseLookupStateName(context, policy,current);
        String relwhere = "attribute[Route Base State]=="+symbolState;
        //得到当前状态下的Route
        MapList mapList =  PCRBO.getRelatedObjects(context,
                RELATIONSHIP_OBJECT_ROUTE, //pattern to match relationships
                TYPE_ROUTE, //pattern to match types
                selList, //the eMatrix StringList object that holds the list of select statement pertaining to Business Obejcts.
                relList, //the eMatrix StringList object that holds the list of select statement pertaining to Relationships.
                false, //get To relationships
                true, //get From relationships
                (short) 1, //the number of levels to expand, 0 equals expand all.
                EMPTY_STRING, //where clause to apply to objects, can be empty ""
                relwhere, //where clause to apply to relationship, can be empty ""
                (short) 0); //limit
        _logger.info("isProcessUnderApproval maplist:{}",mapList);
        MapList tasklist = new MapList();
        if(mapList.size()>0) {
            Map routeMap = (Map)mapList.get(0);
            DomainObject routeObj = DomainObject.newInstance(context);
            String where = "(owner=='" + strLoginUser + "' && current!=Complete) && (attribute[Title]=='"+zhTitle+"' || attribute[Title]=='"+enTitle+"')";
            _logger.info("where:{}",where);
            routeObj.setId(UIUtil.getValue(routeMap, SELECT_ID));
            MapList list = routeObj.getRelatedObjects(context,
                    RELATIONSHIP_ROUTE_TASK, //pattern to match relationships
                    TYPE_INBOX_TASK, //pattern to match types
                    JF_Util_mxJPO.basicBolistSel(), //the eMatrix StringList object that holds the list of select statement pertaining to Business Obejcts.
                    JF_Util_mxJPO.basicRellistSel(), //the eMatrix StringList object that holds the list of select statement pertaining to Relationships.
                    true, //get To relationships
                    false, //get From relationships
                    (short) 1, //the number of levels to expand, 0 equals expand all.
                    where, //where clause to apply to objects, can be empty ""
                    null, //where clause to apply to relationship, can be empty ""
                    (short) 0); //limit
            if (list.size() > 0) {
                tasklist.addAll(list);
            }
        }
        return tasklist.size()>0;
    }
    /*
     * @description:显示切换断点时间
     * @author: caipan
     * @date: 2025/11/25 13:28:11
     * @param: * @param[1] context
     * @param[2] args
     * @return:
     **/
    public String showSwitchingDate(Context context,String[] args) throws Exception{
        _logger.info("showSwitchingDate");
        Map inputMap = (Map) JPO.unpackArgs(args);
        HashMap requestMap = (HashMap) inputMap.get("requestMap");
        String objectId = UIUtil.getValue(requestMap,"objectId");
        DomainObject obj = DomainObject.newInstance(context,objectId);
        String swithdate = obj.getAttributeValue(context,JF_PLMConstants_mxJPO.attr_JF_BreakpointSwitchingDate);
        StringBuffer buffer = new StringBuffer();
        SimpleDateFormat dateFormat = new SimpleDateFormat(eMatrixDateFormat.getEMatrixDateFormat(),Locale.US);
        SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy/MM/dd");
       Date date = dateFormat.parse(swithdate);
        String outputStr = outputFormat.format(date);
        String label = EnoviaResourceBundle.getProperty(context, "emxFrameworkStringResource", context.getLocale(), "emxFramework.Attribute.JF_BreakpointSwitchingDate");
        outputStr= label+":"+outputStr;
        return outputStr;
    }
    /*
     * @description:检查属性是否有值
     * @author: caipan
     * @date: 2025/11/25 14:36:15
     * @param: * @param[1] context
     * @param[2] id
     * @return:
     **/
    public static boolean checkAttrHasValue(Context context,String id,String attrName) throws Exception{
        DomainObject obj = DomainObject.newInstance(context,id);
        String value = obj.getAttributeValue(context,attrName);
        if(UIUtil.isNotNullAndNotEmpty(value)){
            return true;
        }
        return false;
    }
    /*
     * @description:同意完成流程任务
     * @author: caipan
     * @date: 2025/11/25 15:01:49
     * @param: * @param[1] context
     * @param[2] args
     * @return:
     **/
    public void acceptTask(Context context,String[] args) throws Exception{
        String pcrId = args[0];
        String owner = args[1];
       String taskId =getCurrentInboxTask(context,pcrId,owner);
       _logger.info("acceptTask {}",taskId);
        boolean hashPushCtx=false;
       try {
           InboxTask taskObj01 = (InboxTask) DomainObject.newInstance(context, taskId);
           taskObj01.acceptTask(context);
           ContextUtil.pushContext(context);
            hashPushCtx = true;
           ContextUtil.startTransaction(context, true);
           taskObj01.setAttributeValue(context, "Comments", "Auto Approval");
           taskObj01.setAttributeValue(context, "Task Comments Needed", "No");
           taskObj01.setState(context, "Complete");
           ContextUtil.commitTransaction(context);
       }catch (Exception e){
           e.printStackTrace();
           ContextUtil.abortTransaction(context);
       }finally {

           if(hashPushCtx){
               ContextUtil.popContext(context);
           }
       }
    }
    /*
     * @description:设置日期格式的属性值
     * @author: caipan
     * @date: 2025/11/25 15:25:39
     * @param: * @param[1] context
     * @param[2] args
     * @return:
     **/
    public void setDateAttributeValue(Context context,String[] args) throws Exception{
        String strObjectId = args[0];
        String strNewValue = args[1];
        String JFPCRSwitchingMethodCmd = args[2];
        _logger.info("JFPCRSwitchingMethodCmd:{} {}",JFPCRSwitchingMethodCmd,strNewValue);
        try {
                DomainObject project = DomainObject.newInstance(context, strObjectId);
            if(UIUtil.isNotNullAndNotEmpty(strNewValue)&&!isValidDate(strNewValue)) {
                SimpleDateFormat dateFormat = new SimpleDateFormat(eMatrixDateFormat.getEMatrixDateFormat(), Locale.US);
                Locale locale = context.getLocale();
                TimeZone tz = TimeZone.getTimeZone(context.getSession().getTimezone());
                double dbMilisecondsOffset = (double) (-1) * tz.getRawOffset();
                double clientTZOffset = (new Double(dbMilisecondsOffset / (1000 * 60 * 60))).doubleValue();
                strNewValue = eMatrixDateFormat.getFormattedInputDate(strNewValue, clientTZOffset, locale);
                Date newDate = dateFormat.parse(strNewValue);
                Calendar constraintDate = Calendar.getInstance();
                constraintDate.setTime(newDate);
                strNewValue = dateFormat.format(constraintDate.getTime());
                project.setAttributeValue(context, JF_PLMConstants_mxJPO.attr_JF_BreakpointSwitchingDate, strNewValue);
            }
                project.setAttributeValue(context, JF_PLMConstants_mxJPO.ATTR_JF_PCRSwitchingMethod, JFPCRSwitchingMethodCmd);

        }catch (Exception e){
            e.printStackTrace();
        }
    }
    /**
     * 验证字符串是否符合 yyyy/MM/dd 格式，且日期合法（Java 8+）
     * @param dateStr 待验证的日期字符串
     * @return 合法返回 true，否则返回 false
     */
    public static boolean isValidDate(String dateStr) {
        DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy/MM/dd");
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return false;
        }

        try {
            // 解析为 LocalDate，自动严格校验格式和日期合法性
            LocalDate date = LocalDate.parse(dateStr, DATE_FORMATTER);
            // 反向校验，确保格式完全匹配（比如 2026/1/22 会被拒绝）
            return DATE_FORMATTER.format(date).equals(dateStr);
        } catch (Exception e) {
            return false;
        }
    }
    /*
     * @description:设置PCR关联的执行任务到工作中
     * @author: caipan
     * @date: 2025/11/25 15:41:50
     * @param: * @param[1] context
     * @param[2] args
     * @return:
     **/
    public void setPerformTaskState(Context context,String[] args) throws Exception{
        String objectId = args[0];
        DomainObject obj = DomainObject.newInstance(context,objectId);
        StringList executeList = obj.getInfoList(context, "from[JF_PCR2ExecuteTask].to.id");
        DomainObject taskObj = DomainObject.newInstance(context);
        try {
            ContextUtil.pushContext(context);
            for (int i = 0; i < executeList.size(); i++) {
                taskObj.setId(executeList.get(i));
//                taskObj.setState(context, "Active");
                setStateMql(context,new String[]{executeList.get(i),"Active"});
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            ContextUtil.popContext(context);
        }
    }


    /**
     * @Author Liuxg
     * @Description 当前PCR页面点击同意后提升流程中审核任务
     * @Date 2025/11/24 15:36
     * @Param [context, args]
     * @return void
     **/
    public void agreeJF_PCRTask(Context context,String []args)throws Exception{
        try {


            String PCRid=args[0];
            String loginName=args[1];
            String inboxTaskid=getCurrentInboxTask(context,PCRid,loginName);


            DomainObject taskpro=DomainObject.newInstance(context,inboxTaskid);
            String routeId=taskpro.getInfo(context,"from[Route Task].to.id");

            DomainObject obj = DomainObject.newInstance(context, routeId);
            StringList selectObjStmt = new StringList();
            selectObjStmt.add(DomainObject.SELECT_ID);
            selectObjStmt.add(DomainObject.SELECT_NAME);
            selectObjStmt.add(DomainConstants.SELECT_CURRENT);
            StringList selectRelStmt = new StringList();
            selectRelStmt.add(DomainRelationship.SELECT_ID);
            selectRelStmt.add("attribute[Route Node ID]");
            selectRelStmt.add("attribute[Route Sequence]");
            selectRelStmt.add("attribute[Comments]");
            selectRelStmt.add("attribute[Route Instructions]");
            String objWhere = "name == '"+loginName+"'";
//            String objWhere = DomainObject.EMPTY_STRING;
            String relWhere = DomainObject.EMPTY_STRING;
            //oldNodeInfos信息用于设置
            MapList NodeInfos = obj.getRelatedObjects(context, // context
                    "Route Node", // relationship pattern
                    "Person", // object pattern
                    selectObjStmt, // object selects
                    selectRelStmt, // relationship selects
                    Boolean.FALSE, // to direction
                    Boolean.TRUE, // from direction
                    (short) 1, // recursion level
                    objWhere, // object where clause
                    relWhere, // relationship where clause
                    0);
            _logger.info("loginuser--->"+loginName);
            _logger.info("NodeInfos--->"+NodeInfos);
            if(NodeInfos.size()>0){
                Map TaskInfos = (Map) NodeInfos.get(0);
                String Comments="OK";
                String connId = String.valueOf(TaskInfos.get(DomainRelationship.SELECT_ID));

                HashMap inboxTaskMap = new HashMap();
                inboxTaskMap.put("Approval Status", "Approve");
                inboxTaskMap.put("Task Comments Needed", "Yes");
                inboxTaskMap.put("Comments","OK");
                taskpro.setAttributeValues(context, inboxTaskMap);
                try {
                    taskpro.promote(context);
//                    taskpro.promote(context);
                }catch (Exception e){
                    _logger.info("promote error!");
                    e.printStackTrace();
                }

                _logger.info("agreeJF_PCRTask---promote end>");


                ContextUtil.pushContext(context);


                if (UIUtil.isNotNullAndNotEmpty(Comments)) {
                    String MQLstmt = "modify bus " + inboxTaskid + " Comments '" + Comments+"'";
                    String MQLret = MqlUtil.mqlCommand(context, MQLstmt, false);
                    String MQLstmts = "modify connection " + connId + " Comments '" + Comments+"'";
                    String MQLrets = MqlUtil.mqlCommand(context, MQLstmts, false);
                }
                String MQLstmtss = "modify connection " + connId + " 'Approval Status'" + " Approve";
                String MQLretss = MqlUtil.mqlCommand(context, MQLstmtss, false);

            }

        }catch (Exception e){
            e.printStackTrace();

        }finally {
            ContextUtil.popContext(context);
        }
    }


    public void TestDelete(Context context,String []args)throws Exception{

        String PCRid="35845.4994.552.4305";
        DomainObject Pcrobj=DomainObject.newInstance(context,PCRid);
        String Conclusion = Pcrobj.getAttributeValue(context, JF_PLMConstants_mxJPO.ATTR_JF_EvaluationConclusion);
        boolean ispush = false;
        try {
//            ContextUtil.pushContext(context);
//            ContextUtil.pushContext(context,"umeij001",null,null);
            if ("Fail".equalsIgnoreCase(Conclusion)) {
//                context.setUser("umeij001");
                StringList executeList = Pcrobj.getInfoList(context, "from[JF_PCR2ExecuteTask].to.id");
                _logger.info("--agreeJF_PCRTask--->"+executeList);
                _logger.info("--agreeJF_PCRTask--context->"+context.getUser());
//                DomainObject.deleteObjects(context,executeList.toStringArray());
                //35845.4994.552.8336
                //35845.4994.552.9085
                //
                //DomainObject.deleteObjects(context,new String[]{"35845.4994.552.8336","35845.4994.552.9085"});
                String MQLstmtss = "delete bus  35845.4994.552.8336";
                MqlUtil.mqlCommand(context, false, MQLstmtss, true);
//                String MQLretss = MqlUtil.mqlCommand(context, MQLstmtss, false);

                _logger.info("--agreeJF_PCRTask-delete-end->");
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
//            ContextUtil.popContext(context);
        }

    }


    /**
     * @Author Liuxg
     * @Description 获取评估中状态下当前审核中的任务
     * @Date 2025/11/24 16:08
     * @Param [context, PCRid, loginName]
     * @return java.lang.String
    **/
    public String getCurrentInboxTask(Context context,String PCRid,String loginName)throws Exception{
        _logger.info("PCRid:{} loginName:{}",PCRid,loginName);
        String InboxTaskid="";

        DomainObject pcrObj=DomainObject.newInstance(context,PCRid);
        StringList selList = new StringList();
        //JF_EvaluationConclusion
        selList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JF_EvaluationConclusion);
        selList.add(SELECT_CURRENT);
        selList.add(SELECT_OWNER);
        Map map = pcrObj.getInfo(context, selList);
        String current = UIUtil.getValue(map, SELECT_CURRENT);
        //评估结论，Success，Fail
        String JF_EvaluationConclusion = UIUtil.getValue(map, JF_PLMConstants_mxJPO.SELECT_ATTR_JF_EvaluationConclusion);
        //在评估中状态
        if("IN_Evaluation".equalsIgnoreCase(current)||"Execution".equalsIgnoreCase(current) ||"Verification".equalsIgnoreCase(current) ){
//                StringList routeidList=pcrObj.getInfoList(context,"from[Object Route].to,id");
            StringList routeselList = JF_Util_mxJPO.basicBolistSel();
            StringList relList = JF_Util_mxJPO.basicRellistSel();

//                boolean flag=isProcessUnderApproval(context,args);

            String where = "current=='In Process'";
            MapList list = pcrObj.getRelatedObjects(context,
                    RELATIONSHIP_OBJECT_ROUTE, //pattern to match relationships
                    TYPE_ROUTE, //pattern to match types
                    routeselList, //the eMatrix StringList object that holds the list of select statement pertaining to Business Obejcts.
                    relList, //the eMatrix StringList object that holds the list of select statement pertaining to Relationships.
                    false, //get To relationships
                    true, //get From relationships
                    (short) 1, //the number of levels to expand, 0 equals expand all.
                    where, //where clause to apply to objects, can be empty ""
                    null, //where clause to apply to relationship, can be empty ""
                    (short) 0);
            _logger.info("list Route:{}",list);
            if(list.size()>0){
                //当前应该有且只有一个启动的流程
                Map routeMap= (Map) list.get(0);
                String routeid= (String) routeMap.get(DomainConstants.SELECT_ID);
                DomainObject routeObj=DomainObject.newInstance(context,routeid);
                StringList inboxTaskCurrentList = routeObj.getInfoList(context,"to[Route Task].from.current");
                StringList inboxTaskidList = routeObj.getInfoList(context,"to[Route Task].from.id");

                for (String inboxTaskid : inboxTaskidList) {
                    InboxTask taskObj = (InboxTask) DomainObject.newInstance(context, inboxTaskid);
                    //获取当前未审批但是属于当前登录人的任务审批通过

                    String InboxCurrent=taskObj.getCurrentState(context).getName();
                    _logger.info("InboxCurrent:{} taskObj.getOwner(context).getName() : dd:{}",InboxCurrent,taskObj.getOwner(context).getName(),taskObj.getInfo(context, SELECT_OWNER));
                    if(!"Complete".equalsIgnoreCase(InboxCurrent)&&loginName.equals(taskObj.getOwner(context).getName())){
                        //当前等待审批的inboxtask和审批人是当前登录人
                        InboxTaskid=inboxTaskid;
                    }
                }
            }else{
                //没有启动流程
            }
        }
_logger.info("InboxTaskid:{}",InboxTaskid);
        return InboxTaskid;
    }

    /**
     * @Author Liuxg
     * @Description 驳回评估任务
     * @Date 2025/12/3 7:19
     * @Param [context, args]
     * @return void
    **/
    public void rejectPCRTask(Context context,String[]args)throws Exception{
        //修改route对象和审核人对象的关系Route Node的属性Comments: no ，Approval Status: Reject
        //修改route对象属性Route Activity State:Rejected，Route Status:Stopped
        //修改Inbox Task属性Comments:no,Task Comments Needed:Yes,Approval Status: Reject
        //评估中的审核任务拒绝后应该吧评估结论，是否通知和责任人清空
        try {
            _logger.info("rejectPCRTask--->");

            String PCRid=args[0];
            String loginuser=args[1];
            String rowids=args[2];


            //将选中的评估任务状态退回。
            String[] rowidArray=rowids.split("@@");
            for(String rowid:rowidArray){
                if(UIUtil.isNotNullAndNotEmpty(rowid)){
                    DomainObject pcrTaskObj=DomainObject.newInstance(context,rowid);
//                    pcrTaskObj.demote(context);
                    MqlUtil.mqlCommand(context,false,"mod bus "+rowid+" current Active",true);
                    //mod liuxg 驳回的时候如果是执行任务，就把对应的文档改为工作中
                    String current=pcrTaskObj.getCurrentState(context).getName();
                    if("Active".equals(current)){
                        String type = pcrTaskObj.getTypeName(context);
                        _logger.info("JF_PCRExecuteTaskid--->"+rowid);
                        _logger.info("type--->"+type);
                        if ("JF_PCRExecuteTask".equals(type)) {
                            StringList idlist = pcrTaskObj.getInfoList(context, "from[Reference Document].to.id");
                            for (String docid : idlist) {
                                MqlUtil.mqlCommand(context, false, "mod bus " + docid + " current IN_WORK", true);
                            }
                        }
                    }

                }
            }


            String taskId=getCurrentInboxTask(context,PCRid,loginuser);
_logger.info("taskId:{}",taskId);
            DomainObject taskpro=DomainObject.newInstance(context,taskId);
            String routeId=taskpro.getInfo(context,"from[Route Task].to.id");

            DomainObject obj = DomainObject.newInstance(context, routeId);
            StringList selectObjStmt = new StringList();
            selectObjStmt.add(DomainObject.SELECT_ID);
            selectObjStmt.add(DomainObject.SELECT_NAME);
            selectObjStmt.add(DomainConstants.SELECT_CURRENT);
            StringList selectRelStmt = new StringList();
            selectRelStmt.add(DomainRelationship.SELECT_ID);
            selectRelStmt.add("attribute[Route Node ID]");
            selectRelStmt.add("attribute[Route Sequence]");
            selectRelStmt.add("attribute[Comments]");
            selectRelStmt.add("attribute[Route Instructions]");
            String objWhere = "name == '"+loginuser+"'";
//            String objWhere = DomainObject.EMPTY_STRING;
            String relWhere = DomainObject.EMPTY_STRING;
            //oldNodeInfos信息用于设置
            MapList NodeInfos = obj.getRelatedObjects(context, // context
                    "Route Node", // relationship pattern
                    "Person", // object pattern
                    selectObjStmt, // object selects
                    selectRelStmt, // relationship selects
                    Boolean.FALSE, // to direction
                    Boolean.TRUE, // from direction
                    (short) 1, // recursion level
                    objWhere, // object where clause
                    relWhere, // relationship where clause
                    0);
            _logger.info("loginuser--->"+loginuser);
            _logger.info("NodeInfos--->"+NodeInfos);
            if(NodeInfos.size()>0){
                Map TaskInfos = (Map) NodeInfos.get(0);
                String Comments="No";
                String connId = String.valueOf(TaskInfos.get(DomainRelationship.SELECT_ID));

                HashMap inboxTaskMap = new HashMap();
                inboxTaskMap.put("Approval Status", "Reject");
                inboxTaskMap.put("Task Comments Needed", "No");
                inboxTaskMap.put("Comments","no");
                taskpro.setAttributeValues(context, inboxTaskMap);
                try {
                    taskpro.promote(context);
//                    taskpro.promote(context);
                }catch (Exception e){
                    System.out.println("promote error!");
                    e.printStackTrace();
                }


                if (UIUtil.isNotNullAndNotEmpty(Comments)) {
                    String MQLstmt = "modify bus " + taskId + " Comments '" + Comments+"'";
                    String MQLret = MqlUtil.mqlCommand(context, MQLstmt, false);
                    String MQLstmts = "modify connection " + connId + " Comments '" + Comments+"'";
                    String MQLrets = MqlUtil.mqlCommand(context, MQLstmts, false);
                }
                String MQLstmtss = "modify connection " + connId + " 'Approval Status'" + " Reject";
                String MQLretss = MqlUtil.mqlCommand(context, MQLstmtss, false);

            }




        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void test(Context context,String[]args){
        try {
            //35845.4994.7379.53208
            //35845.4994.53116.22139
            String id="35845.4994.7379.53208";
            InboxTask taskObj = (InboxTask) DomainObject.newInstance(context, id);
            String aid=taskObj.getTaskAssigneeId(context);
            String aname=taskObj.getTaskAssignee(context);
            String uname=taskObj.getOwner(context).getName();
            String current=taskObj.getCurrentState(context).getName();

            _logger.info("aid--->"+aid);
            _logger.info("aname--->"+aname);
            _logger.info("uname--->"+uname);
            _logger.info("current--->"+current);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    /*
     * @description:到达更改验证发通知给PCROwner
     * @author: caipan
     * @date: 2025/11/26 10:58:52
     * @param: * @param[1] context
     * @param[2] args
     * @return: 
     **/
    public void sendEmailtoVerification(Context context,String[] args) throws Exception{
        String objectId = args[0];
        DomainObject obj = DomainObject.newInstance(context,objectId);
        StringList selList = new StringList();
        selList.add(SELECT_NAME);
        selList.add(SELECT_ATTRIBUTE_TITLE);
        selList.add(SELECT_OWNER);
        Map temp = obj.getInfo(context, selList);
        Map map = new HashMap();
        map.put("objectId",objectId);
        map.put("taskName",UIUtil.getValue(temp,SELECT_ATTRIBUTE_TITLE));
        map.put("connectName",UIUtil.getValue(temp,SELECT_NAME));
        String email =  JF_NotificationUtils_mxJPO.getPersonEmail(context, UIUtil.getValue(temp, SELECT_OWNER), null);
        map.put("email",email);
        //如果评估结论是关闭变更，就跳过发邮件
        String conclusion = obj.getAttributeValue(context,JF_PLMConstants_mxJPO.ATTR_JF_EvaluationConclusion);
        if(!"Fail".equalsIgnoreCase(conclusion)){
            JF_SendEmailUtils_mxJPO.sendPCRArrivalEmailToPortal(context,map);
        }
    }
    /*
     * @description:同意或者拒绝Inbox Task任务
     * @author: caipan
     * @date: 2025/11/26 13:39:48
     * @param: * @param[1] context
     * @param[2] args objectId 任务Id actionType  同意(Approve)还是拒绝(Reject)  Comments 备注
     * @return:
     **/
    public void reviewInbox(Context context,String[] args) throws Exception{
 /*       Map requestMap = JPO.unpackArgs(args);
        String taskId = UIUtil.getValue(requestMap,"objectId");
        String actionType = UIUtil.getValue(requestMap,"actionType");
        String comments = UIUtil.getValue(requestMap,"Comments");*/
        _logger.info("reviewInbox");
        String pcrId = args[0];
        String owner = args[1];
        String actionType = args[2];
        String taskId =getCurrentInboxTask(context,pcrId,owner);
        _logger.info("reviewInbox:{}",taskId);
        String comments ="Approve";
        if(comments.isEmpty()){
            comments=actionType;
        }
        DomainObject inboxTask = DomainObject.newInstance(context,taskId);
         HashMap inboxTaskMap = new HashMap();
		inboxTaskMap.put("Comments",comments);
		inboxTask.setAttributeValues(context,inboxTaskMap);
		if("Reject".equals(actionType)||"Approve".equals(actionType)){
        ContextUtil.pushContext(context);
        inboxTask.setAttributeValue(context,"Approval Status",actionType);
        ContextUtil.popContext(context);
        inboxTask.promote(context);
        }
    }
    /*
     * @description:执行任务是否是最后一个
     * @author: caipan
     * @date: 2025/11/26 15:51:46
     * @param: * @param[1] context
     * @param[2] args
     * @return:
     **/
    public void getActivePerformTask(Context context, String[] args) throws Exception {
        _logger.info("getActivePerformTask");
        String objectId = args[0];
        DomainObject pcrObj = DomainObject.newInstance(context, objectId);
        StringList selList = JF_Util_mxJPO.basicBolistSel();
        StringList relList = JF_Util_mxJPO.basicRellistSel();
        String where = "current==Assign || current==Active";
        MapList list = pcrObj.getRelatedObjects(context,
                JF_PLMConstants_mxJPO.Rel_JF_PCR2ExecuteTask, //pattern to match relationships
                JF_PLMConstants_mxJPO.Type_JF_PCRExecuteTask, //pattern to match types
                selList, //the eMatrix StringList object that holds the list of select statement pertaining to Business Obejcts.
                relList, //the eMatrix StringList object that holds the list of select statement pertaining to Relationships.
                false, //get To relationships
                true, //get From relationships
                (short) 1, //the number of levels to expand, 0 equals expand all.
                where, //where clause to apply to objects, can be empty ""
                null, //where clause to apply to relationship, can be empty ""
                (short) 0); //limit
        _logger.info("list:{}",list.size());
        if (list.size() == 0) {
            //创建流程
            _logger.info("create 流程");
            createRouteInReview(context, args);
        }

    }
    /*
     * @description:是否有正在审核的流程 ，判断是否有提交的权限
     * @author: caipan
     * @date: 2025/11/26 16:12:57
     * @param: * @param[1] context
     * @param[2] args
     * @return:
     **/
    public boolean hasProcessUnderApproval(Context context,String[] args) throws Exception{
        //审核状态
        Map paramsMap = (Map) JPO.unpackArgs(args);
        String id = (String) paramsMap.get(STRING_OBJECTID);
        String strLoginUser = context.getUser();
        DomainObject PCRBO = DomainObject.newInstance(context,id);
        StringList  selList = JF_Util_mxJPO.basicBolistSel();
        selList.add("attribute[Route Status]");
        StringList  relList = JF_Util_mxJPO.basicRellistSel();
        relList.add("attribute[Route Base State]");
        String current = PCRBO.getInfo(context, SELECT_CURRENT);
        String policy  = PCRBO.getInfo(context, SELECT_POLICY);
        String symbolState = FrameworkUtil.reverseLookupStateName(context, policy,current);
        String relwhere = "attribute[Route Base State]=="+symbolState;
//        String where ="attribute[Route Status]==Stopped";
        //得到当前状态下的Route
        MapList mapList =  PCRBO.getRelatedObjects(context,
                RELATIONSHIP_OBJECT_ROUTE, //pattern to match relationships
                TYPE_ROUTE, //pattern to match types
                selList, //the eMatrix StringList object that holds the list of select statement pertaining to Business Obejcts.
                relList, //the eMatrix StringList object that holds the list of select statement pertaining to Relationships.
                false, //get To relationships
                true, //get From relationships
                (short) 1, //the number of levels to expand, 0 equals expand all.
                "", //where clause to apply to objects, can be empty ""
                relwhere, //where clause to apply to relationship, can be empty ""
                (short) 0); //limit
        _logger.info("hasProcessUnderApproval maplist:{}",mapList);
        if(mapList.size()>0){
            //看流程的状态
            Map map = (Map)mapList.get(0);
            String Status= UIUtil.getValue(map,"attribute[Route Status]");
            if("Stopped".equalsIgnoreCase(Status)){
                return true;
            }else{
                return false;
            }
        }else{
            return true;
        }
    }

    /**
     * @Author Liuxg
     * @Description 评估结论command下拉值
     * @Date 2025/11/26 16:03
     * @Param [context, args]
     * @return java.util.Map
    **/
    public Map getEvaluationConclusionRange(Context context, String[] args) throws Exception {
        Map rangeMap = new HashMap();
        Map programMap = (Map) JPO.unpackArgs(args);
        Map requestMap = (Map) programMap.get("requestMap");
        Map paramMap = (Map) programMap.get("paramMap");
        String sLanguage = (String) paramMap.get("languageStr");
        String objectId = (String) requestMap.get("objectId");
        String Success = (String) EnoviaResourceBundle.getFrameworkStringResourceProperty(context, "emxFramework.Range.JF_EvaluationConclusion.Success", context.getLocale());
        String Fail = (String) EnoviaResourceBundle.getFrameworkStringResourceProperty(context, "emxFramework.Range.JF_EvaluationConclusion.Fail", context.getLocale());

        DomainObject projectObj = DomainObject.newInstance(context, objectId);
        String JF_EvaluationConclusion = projectObj.getAttributeValue(context, JF_PLMConstants_mxJPO.ATTR_JF_EvaluationConclusion);
        _logger.info("JF_EvaluationConclusion:{}",JF_EvaluationConclusion);
        StringList keyList = new StringList();
        StringList valueList = new StringList();
        if ("Fail".equalsIgnoreCase(JF_EvaluationConclusion)) {
            keyList.add("Fail");
            valueList.add(Fail);
            keyList.add("Success");
            valueList.add(Success);
        } else if ("Success".equalsIgnoreCase(JF_EvaluationConclusion)) {
            keyList.add("Success");
            valueList.add(Success);
            keyList.add("Fail");
            valueList.add(Fail);
        } else {
            keyList.add("");
            valueList.add("");
            keyList.add("Fail");
            valueList.add(Fail);
            keyList.add("Success");
            valueList.add(Success);
        }
        rangeMap.put("field_choices", keyList);
        rangeMap.put("field_display_choices", valueList);
        return rangeMap;
    }

    /**
     * @Author Liuxg
     * @Description 是否通知客户command下拉值
     * @Date 2025/11/27 6:44
     * @Param [context, args]
     * @return java.util.Map
    **/
    public Map JFPCREvaluationInformCustomerRange(Context context, String[] args) throws Exception {
        Map rangeMap = new HashMap();
        Map programMap = (Map) JPO.unpackArgs(args);
        Map requestMap = (Map) programMap.get("requestMap");
        Map paramMap = (Map) programMap.get("paramMap");
        String sLanguage = (String) paramMap.get("languageStr");
        String objectId = (String) requestMap.get("objectId");
        String Y = (String) EnoviaResourceBundle.getFrameworkStringResourceProperty(context, "emxFramework.Range.JF_IsEvaluationInformCustomer.Y", context.getLocale());
        String N = (String) EnoviaResourceBundle.getFrameworkStringResourceProperty(context, "emxFramework.Range.JF_IsEvaluationInformCustomer.N", context.getLocale());

        DomainObject projectObj = DomainObject.newInstance(context, objectId);
        String supply = projectObj.getAttributeValue(context, JF_PLMConstants_mxJPO.ATTR_JF_IsEvaluationInformCustomer);
        _logger.info("supply:{}",supply);
        StringList keyList = new StringList();
        StringList valueList = new StringList();
        if ("N".equalsIgnoreCase(supply)) {
            keyList.add("N");
            valueList.add(N);
            keyList.add("Y");
            valueList.add(Y);
        } else if ("Y".equalsIgnoreCase(supply)) {
            keyList.add("Y");
            valueList.add(Y);
            keyList.add("N");
            valueList.add(N);
        } else {
            keyList.add("");
            valueList.add("");
            keyList.add("N");
            valueList.add(N);
            keyList.add("Y");
            valueList.add(Y);
        }
        rangeMap.put("field_choices", keyList);
        rangeMap.put("field_display_choices", valueList);
        return rangeMap;
    }


    /**
     * @Author Liuxg
     * @Description 获取关联的通知人
     * @Date 2025/12/25 22:36
     * @Param [context, args]
     * @return java.lang.String
    **/
    public Map getJFPCRInputInformOwner(Context context, String[] args) throws Exception {
        Map rangeMap = new HashMap();
        String personFullName="";
        try {
            Map programMap = (Map) JPO.unpackArgs(args);
            Map requestMap = (Map) programMap.get("requestMap");
            Map paramMap = (Map) programMap.get("paramMap");
            String sLanguage = (String) paramMap.get("languageStr");
            String objectId = (String) requestMap.get("objectId");

            StringList keyList = new StringList();
            StringList valueList = new StringList();

            DomainObject projectObj = DomainObject.newInstance(context, objectId);
            String Responsible = projectObj.getAttributeValue(context, "JF_PCREvaluationInformResponsible");
            _logger.info("Responsible:{}",Responsible);
            if(UIUtil.isNotNullAndNotEmpty(Responsible)){
                personFullName= PersonUtil.getFullName(context,Responsible);
                keyList.add(personFullName);
                valueList.add(personFullName);
            }

            rangeMap.put("field_choices", keyList);
            rangeMap.put("field_display_choices", valueList);
        }catch (Exception e){
            e.printStackTrace();
        }

        return rangeMap;
    }

    /*
     * @description:执行任务的编辑权限
     * @author: caipan
     * @date: 2025/12/2 10:31:09
     * @param: * @param[1] context
     * @param[2] args
     * @return:
     **/
    public StringList gePCRExecutionTaskAccess(Context context, String[] args) throws Exception {
        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        Map requestMap = (Map) programMap.get(STRING_REQUESTMAP);
        String parentId =(String)requestMap.get("parentOID");
        DomainObject obj = DomainObject.newInstance(context,parentId);
        String pcrCurrent = obj.getInfo(context, SELECT_CURRENT);
        String strLoginUser = context.getUser();
        MapList objectList = (MapList) programMap.get("objectList");
        StringList editAccessList = new StringList(objectList.size());
        for (int i = 0; i < objectList.size(); i++) {
            Map objectMap = (Map) objectList.get(i);
            String owner = (String) objectMap.get(SELECT_OWNER);
            String current = (String) objectMap.get(SELECT_CURRENT);
            if(current.equalsIgnoreCase("Active")) {
                if (strLoginUser.equalsIgnoreCase(owner)&&pcrCurrent.equalsIgnoreCase("Execution")) {
                    editAccessList.add("true");
                } else {
                    editAccessList.add("false");
                }
            }else{
                editAccessList.add("false");
            }
        }
        return editAccessList;
    }
    public StringList JFPCRUploadFolderClick(Context context, String[] args) throws Exception{
        StringList resList = new StringList();
        long currentMillisTimestamp = System.currentTimeMillis();
        try {
            Map argMaps = JPO.unpackArgs(args);
            Map requestMap = (Map) argMaps.get(STRING_REQUESTMAP);
            String parentId =(String)requestMap.get("parentOID");
            DomainObject obj = DomainObject.newInstance(context,parentId);
            String pcrCurrent = obj.getInfo(context, SELECT_CURRENT);
            MapList objectList = (MapList) argMaps.get("objectList");
            StringBuffer buffer = new StringBuffer();
            boolean flag = VerificationSubmitAccess(context,JPO.packArgs(requestMap));
            _logger.info("flag:{}",flag);
            for (int i = 0; i < objectList.size(); i++) {
                Map map = (Map) objectList.get(i);
                String id = (String) map.get(DomainConstants.SELECT_ID);
                DomainObject object = DomainObject.newInstance(context, id);
                State currentState = object.getCurrentState(context);
                Map responseMap = object.getRelatedObject(context, "Assigned Tasks", false, JF_Util_mxJPO.basicBolistSel(), JF_Util_mxJPO.basicRellistSel());
                if(responseMap!=null){
                    String taskResponseName = (String) responseMap.get(DomainConstants.SELECT_NAME);
                    String contextUser = context.getUser();
                    _logger.info("access:{}",contextUser.equals(taskResponseName)&&flag && "Active".equals(currentState.getName()) &&(pcrCurrent.equalsIgnoreCase("Execution") ||pcrCurrent.equalsIgnoreCase("Verification")));
                    if(contextUser.equals(taskResponseName)&&flag && "Active".equals(currentState.getName()) &&(pcrCurrent.equalsIgnoreCase("Execution") ||pcrCurrent.equalsIgnoreCase("Verification"))  ){
                        buffer.append("<a onclick=\"PCRShowModalDialog(event,'../components/JF_ECOCommonDocumentPreCheckin.jsp?objectId=");
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
    /*
     * @description:设置PCR评估状态直接到PCR关闭状态
     * @author: caipan
     * @date: 2025/12/2 15:18:47
     * @param: * @param[1] context
     * @param[2] args
     * @return:
     **/
    public  void setPCREvaluationState(Context context,String[] args) throws Exception {
        String objectId = args[0];
        DomainObject obj = DomainObject.newInstance(context, objectId);
        String Conclusion = obj.getAttributeValue(context, JF_PLMConstants_mxJPO.ATTR_JF_EvaluationConclusion);
        boolean ispush = false;
        try{
            ContextUtil.pushContext(context);
            ispush = true;
            if ("Fail".equalsIgnoreCase(Conclusion)) {
                //如果有执行任务是否直接删除掉?
                StringList executeList = obj.getInfoList(context, "from[JF_PCR2ExecuteTask].to.id");
                _logger.info("--setPCREvaluationState--->"+executeList);

//                //直接把PCR设置到关闭状态

                for(String id:executeList){
                    String MQLstmtss = "delete bus "+id;
                    MqlUtil.mqlCommand(context, false, MQLstmtss, true);
                }
                _logger.info("--setPCREvaluationState-end-->");
                MqlUtil.mqlCommand(context, false, "mod bus " + objectId + " current Closed", true);
            }else{
                obj.setState(context,"Verification");
                //把所有的验证任务都提升到完成
                StringList verificationList = obj.getInfoList(context, "from[JF_PCR2Task].to.id");
                for(int i=0;i<verificationList.size();i++){
                    MqlUtil.mqlCommand(context, false, "mod bus " + verificationList.get(i) + " current Complete", true);
                }

            }
            //创建通知的执行任务
            createPCRTaskExecuteTask(context,objectId);
        }finally {
            if(ispush){
                ContextUtil.popContext(context);
            }
        }

    }




    /**
     * @Author Liuxg
     * @Description 获取评估任务通知人
     * @Date 2025/12/3 8:48
     * @Param [context, args]
     * @return matrix.util.StringList
    **/
    public StringList getPcrTaskPerson(Context context, String[] args) throws Exception{
        StringList resultList = new StringList();
        Boolean isPush = Boolean.FALSE;
        try {
            Map program = (Map)JPO.unpackArgs(args);
            ContextUtil.pushContext(context);
            _logger.info("program:{}", program.toString());
            String Pcrid = (String)program.get("parentOID");
            _logger.info("parentOID:{}", Pcrid.toString());
            DomainObject pcrobj = DomainObject.newInstance(context, Pcrid);
            StringList taskidlist = pcrobj.getInfoList(context, "from[JF_PCR2Task].to.id");
            Map ownermap=new HashMap();
            for (int i=0;i<taskidlist.size();i++){
                String taskid=taskidlist.get(i);
                DomainObject taskobj = DomainObject.newInstance(context, taskid);
                String ownername=taskobj.getOwner(context).getName();
                String ownerid=PersonUtil.getPersonObjectID(context,ownername);
                if(!ownermap.containsKey(ownerid)){
                    resultList.add(ownerid);
                    ownermap.put(ownerid,"");
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            if (isPush) {
                ContextUtil.popContext(context);
            }
            throw new RuntimeException(e);
        }
        _logger.info("resultList:{}", resultList.toString());
        return resultList;
    }

    /**
     * @Author Liuxg
     * @Description PCR评估中状态提升后如果是选择的通知用户，创建对应的执行任务
     * @Date 2025/12/3 8:48
     * @Param [context, args]
     * @return void
    **/
    public void createPCRTaskExecuteTask(Context context, String objectId) throws Exception {
        {
            try {
                _logger.info("createPCRTaskExecuteTask ");
//                String objectId =args[0];
                DomainObject pcrObj = DomainObject.newInstance(context, objectId);
                String JF_IsEvaluationInformCustomer=pcrObj.getAttributeValue(context,"JF_IsEvaluationInformCustomer");
                String JF_PCREvaluationInformResponsible=pcrObj.getAttributeValue(context,"JF_PCREvaluationInformResponsible");

                if(UIUtil.isNotNullAndNotEmpty(JF_IsEvaluationInformCustomer)&&"Y".equalsIgnoreCase(JF_IsEvaluationInformCustomer)&&UIUtil.isNotNullAndNotEmpty(JF_PCREvaluationInformResponsible)){
                    //通知客户PCR变更内容
                    String title = EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", context.getLocale(), "emxComponents.PCR.IN_EvaluationInformCustomer.Title");

                    Map map = new HashMap();
                    map.put("taskName", title);//任务标题
                    map.put("taskType", SYMBOLIC_JF_PCRExecuteTask);//任务类型 注册名
                    map.put("taskOwner", JF_PCREvaluationInformResponsible);  //ECO的owner
                    map.put("parentId", objectId);
                    //创建PCRTask任务实体
                    JF_Util_mxJPO jfUtilMxJPO = new JF_Util_mxJPO();
                    String taskId = jfUtilMxJPO.createTask(context, JPO.packArgs(map));
                    DomainObject taskObj = new DomainObject(taskId);
                    // PCR与创建的PCRTask连接关系
                    DomainRelationship.connect(context, pcrObj, JF_PLMConstants_mxJPO.Rel_JF_PCR2ExecuteTask, taskObj);
                    taskObj.setState(context, "Assign");//创建完成就设置到分配中
                    taskObj.setAttributeValue(context,"JF_PCRFunction","\u5546\u52A1");
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    /*
     * @description:设置执行任务的状态到完成
     * @author: caipan
     * @date: 2025/12/3 10:38:21
     * @param: * @param[1] context
     * @param[2] args
     * @return:
     **/
    public  void setPCRExecutionState(Context context,String[] args) throws Exception {
        String objectId = args[0];
        DomainObject obj = DomainObject.newInstance(context, objectId);
        boolean ispush = false;
        try{
            ContextUtil.pushContext(context);
            ispush = true;
            //把所有的执行任务都提升到完成
            StringList verificationList = obj.getInfoList(context, "from[JF_PCR2ExecuteTask].to.id");
            for(int i=0;i<verificationList.size();i++){
                setDocStateRELEASED(context,new String[]{verificationList.get(i)});
                MqlUtil.mqlCommand(context, false, "mod bus " + verificationList.get(i) + " current Complete", true);

            }
        }finally {
            if(ispush){
                ContextUtil.pushContext(context);
            }
        }

    }
    /*
     * @description:设置PCR关联任务的状态,拒绝的时候把任务状态设置会工作中
     * @author: caipan
     * @date: 2025/12/3 11:05:34
     * @param: * @param[1] context
     * @param[2] args args[0]  PCR ID  args[1] PCRE和任务的关系名字
     * @return:
     **/
    public  void setPCRTaskState(Context context,String[] args) throws Exception {
        String objectId = args[0];
        String rel = args[1];
        DomainObject obj = DomainObject.newInstance(context, objectId);
        boolean ispush = false;
        try{
            ContextUtil.pushContext(context);
            ispush = true;
            //判断是否是在外面拒绝还是在里面手动驳回
            StringList verificationListState = obj.getInfoList(context, "from["+rel+"].to.current");
            if(!verificationListState.contains("Active")) {
                StringList verificationList = obj.getInfoList(context, "from[" + rel + "].to.id");
                for (int i = 0; i < verificationList.size(); i++) {
                    MqlUtil.mqlCommand(context, false, "mod bus " + verificationList.get(i) + " current Active", true);
                    setDocStateIN_WORK(context,new String[]{verificationList.get(i)});
                }
            }
        }finally {
            if(ispush){
                ContextUtil.pushContext(context);
            }
        }

    }




    /*
     * @description:Inbox Task 任务拒绝时候，需要处理的业务逻辑
     * @author: caipan
     * @date: 2025/12/3 10:56:42
     * @param: * @param[1] context
     * @param[2] args
     * @return:
     **/
    public int checkInboxTaskRejectMaintenanceInformation(Context context, String[] args) throws Exception {
        //拿取ECR所属的project
        Set<String> projectSet = new HashSet<>();
        //是否是ECR 状态Review 的项目经理审核任务
        boolean isECRReviewRoute = false;
        boolean isPop = false;
        String strPolicy = "";
        String strEcrId = "";
        try {
            String strLoginUser = context.getUser();
            _logger.info("strLoginUser：{}",strLoginUser);
            ContextUtil.pushContext(context);
            String strObjectId = args[0];
            _logger.info("strObjectId:{}", strObjectId);
            DomainObject inBoxTask = DomainObject.newInstance(context, strObjectId);
            // add by chenyan 修复当审核任务驳回时也校验了必填属性 驳回时不需要校验
            String strApproveState = inBoxTask.getInfo(context, "attribute[Approval Status].value");
            String Title = inBoxTask.getInfo(context, "attribute[Title].value");
            _logger.info("strApproveState：{}",strApproveState);
            if ("Reject".equals(strApproveState)) {

                StringList typeSelectList = new StringList();
                typeSelectList.add(SELECT_ID);
                typeSelectList.add(SELECT_TYPE);
                typeSelectList.add(SELECT_ATTRIBUTE_TITLE);
                typeSelectList.add(SELECT_ATTR_JFECRTYPE);
                typeSelectList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JFBREAKPOINTMODE);
                typeSelectList.add("from[JFChange2Project].to.id");
                typeSelectList.add("from[JFChange2Project].to.owner");
                StringList reSelectList = new StringList();
                reSelectList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_ROUTE_BASE_POLICY);
                reSelectList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_ROUTE_BASE_STATE);
                reSelectList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_ROUTE_BASE_PURPOSE);
                MapList maps = inBoxTask.getRelatedObjects(context, RELATIONSHIP_ROUTE_TASK + "," + RELATIONSHIP_OBJECT_ROUTE, // relationship pattern
                        TYPE_ROUTE + "," + TYPE_JFECR + "," + TYPE_JFNewECR + "," + TYPE_JFFormalECR+ "," + JF_PLMConstants_mxJPO.TYPE_JFPartList + "," + TYPE_DOCUMENT + "," + JF_PLMConstants_mxJPO.Type_JF_PCR,                                    // object pattern
                        typeSelectList,                            // object selects
                        reSelectList, // relationship selects
                        true,                                        // to direction
                        true,                                        // from direction
                        (short) 2,                                    // recursion level
                        "",                // object where clause
                        "",
                        (short) 0,
                        false, //checkHidden
                        true, //preventDuplicates
                        (short) 0, //pageSize
                        null,
                        null,
                        null,
                        "end"); //end(返回叶子节点)
                _logger.info("maps:{}", maps);
                ContextUtil.popContext(context);
                isPop = true;
                Map groupMap = (Map) maps.stream().collect(Collectors.groupingBy(m -> {
                    Map info = (Map) m;
                    return info.get("relationship");
                }));
                _logger.info("groupMap:{}", groupMap);
                if (groupMap.containsKey(RELATIONSHIP_OBJECT_ROUTE)) {
                    List ecrList = (List) groupMap.get(RELATIONSHIP_OBJECT_ROUTE);
                    //理应只绑定一个ECR
                    if (null != ecrList && ecrList.size() > 0) {
                        for (int i = 0; i < ecrList.size(); i++) {
                            Map ecrInfo = (Map) ecrList.get(i);
                            //  "end"); //end(返回叶子节点) 使用这个时候关系属性被OOTB拼接了end
                            String strRouteBindPolicy = (String) ecrInfo.get(JF_PublicMethodClass_mxJPO.buildStringInStrings("end", JF_PLMConstants_mxJPO.SELECT_ATTR_ROUTE_BASE_POLICY));
                            strPolicy = strRouteBindPolicy;
                            strEcrId = (String) ecrInfo.get(SELECT_ID);
                            String strRouteBindState = (String) ecrInfo.get(JF_PublicMethodClass_mxJPO.buildStringInStrings("end", JF_PLMConstants_mxJPO.SELECT_ATTR_ROUTE_BASE_STATE));
                            _logger.info("strRouteBindPolicy:{}", strRouteBindPolicy);
                            _logger.info("strRouteBindState:{}", strRouteBindState);
                            //ECRSubmit创建的流程
                             if ("policy_JF_PCR".equals(strPolicy) && "state_Execution".equals(strRouteBindState)) {//执行验证
                                    //PCRID
                                    String docId = (String) ecrInfo.get(SELECT_ID);
                                 setPCRTaskState(context,new String[]{docId,"JF_PCR2ExecuteTask"});

                                    //拒绝的时候 需要把执行任务全部退回到Active
                                }else    if ("policy_JF_PCR".equals(strPolicy) && "state_IN_Evaluation".equals(strRouteBindState)) {//评估中
                                 //PCRID
                                 String docId = (String) ecrInfo.get(SELECT_ID);
                                 //拒绝的时候 需要把执行任务全部退回到Active
                                 setPCRTaskState(context,new String[]{docId,"JF_PCR2Task"});
                                 //评估意见清空
                                 clearEvaluationReviewInfo(context,new String[]{docId});
                             }else    if ("policy_JF_PCR".equals(strPolicy) && "state_Verification".equals(strRouteBindState)) {//更改验证
                                 //PCRID
                                 String docId = (String) ecrInfo.get(SELECT_ID);
                                 //验证相关信息清空
                                 clearVerificationReviewInfo(context,new String[]{docId});
                             }

                            }
                        }
                    }
                }
        } finally {
            if (!isPop) {
                ContextUtil.popContext(context);
            }
        }

        //end
        return 0;
    }
    /*
     * @description:执行任务创建权限 JFPCRTaskLibChooserCmd
     * @author: caipan
     * @date: 2025/12/18 14:53:54
     * @param: * @param[1] context
     * @param[2] args
     * @return:
     **/
    public boolean  showTaskExecution(Context context,String[] args) throws Exception{
        Map requestMap = JPO.unpackArgs(args);
        String id = (String) requestMap.get(STRING_OBJECTID);
        _logger.info("id:{}",id);
        //查询PCR所有的评估任务的owner，如果有当前登录用户就有权限
        DomainObject obj = DomainObject.newInstance(context,id);
        StringList taskOwnerList = obj.getInfoList(context,"from[JF_PCR2Task].to.owner");
        String loginUser = context.getUser();
        if(taskOwnerList.contains(loginUser)){
            return true;
        }
        return false;
    }
    /*
     * @description:断点方式 command获取值
     * @author: caipan
     * @date: 2025/12/18 16:34:35
     * @param: * @param[1] context
     * @param[2] args
     * @return:
     **/
    public Map getSwitchingMethodRange(Context context, String[] args) throws Exception {
        Map policyMap = new HashMap();
        Map programMap = (Map) JPO.unpackArgs(args);
        Map requestMap = (Map) programMap.get("requestMap");
        Map paramMap = (Map) programMap.get("paramMap");
        String sLanguage = (String) paramMap.get("languageStr");
        String objectId = (String) requestMap.get("objectId");
        String Vertical_Switching = (String) EnoviaResourceBundle.getFrameworkStringResourceProperty(context, "emxFramework.Range.JF_PCRSwitchingMethod.Vertical_Switching", context.getLocale());
        String Seamless_Switching = (String) EnoviaResourceBundle.getFrameworkStringResourceProperty(context, "emxFramework.Range.JF_PCRSwitchingMethod.Seamless_Switching", context.getLocale());

        DomainObject projectObj = DomainObject.newInstance(context, objectId);
        String supply = projectObj.getAttributeValue(context, JF_PLMConstants_mxJPO.ATTR_JF_PCRSwitchingMethod);
        StringList keyList = new StringList();
        StringList valueList = new StringList();
        if ("Vertical_Switching".equalsIgnoreCase(supply)) {
            keyList.add("Vertical_Switching");
            keyList.add("Seamless_Switching");
            valueList.add(Vertical_Switching);
            valueList.add(Seamless_Switching);
        } else {
            keyList.add("Seamless_Switching");
            keyList.add("Vertical_Switching");
            valueList.add(Seamless_Switching);
            valueList.add(Vertical_Switching);

        }
        policyMap.put("field_choices", keyList);
        policyMap.put("field_display_choices", valueList);
        return policyMap;
    }
    /*
     * @description:mql 设置状态
     * @author: caipan
     * @date: 2025/12/19 11:24:44
     * @param: * @param[1] context
     * @param[2] args
     * @return:
     **/
    public void setStateMql(Context context,String[] args) throws Exception{
        String id = args[0];
        String state = args[1];
            String mql = "mod bus "+id+" current '"+state+"'";
            MqlUtil.mqlCommand(context,false,mql,true);
    }


    public Map processEditPCRJPO(Context context, String[] args) throws Exception {
        _logger.info("-----------------------------  processEditPCRJPO begin ---------------------------------------");
        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        Map requestMap = (Map) programMap.get("requestMap");

        String strJFECRQQFileId = (String) requestMap.get("JFPCRBeforeFileName");
        String strJFECRQQFileId2 = (String) requestMap.get("JFPCRAfterFileName");
        String mainProjectOID = (String) requestMap.get("mainProjectOID");
        String JFProjectNameOID = (String) requestMap.get("JFProjectNameOID");
        String JFProjectNameDisplay = (String) requestMap.get("JFProjectNameDisplay");
//        String strAffectedProjectIds = (String) requestMap.get("JFAffectedProject");
//        String strQQID = (String) requestMap.get("JFQQ");
        String strObjectId = (String) requestMap.get("objectId");
        Map res = new HashMap<>();
        //防止不做任何编辑点提交
        _logger.info("programMap:{}", programMap);
        _logger.info("strObjectId:{}", strObjectId);
        _logger.info("strJFECRQQFileId:{}", strJFECRQQFileId);
        _logger.info("strJFECRQQFileId2:{}", strJFECRQQFileId2);

        try {
            ContextUtil.pushContext(context);
            ContextUtil.startTransaction(context, true);

            //更新受影响项目
            if (UIUtil.isNotNullAndNotEmpty(strObjectId)) {
                StringList typeSelectList = new StringList();
                StringList reSelectList = new StringList();
                typeSelectList.add(SELECT_ID);
                typeSelectList.add(SELECT_NAME);
                reSelectList.add(SELECT_RELATIONSHIP_ID);
                DomainObject PCR = DomainObject.newInstance(context, strObjectId);
                String strCurrent = PCR.getInfo(context, SELECT_CURRENT);
//                if ("Create".equals(strCurrent)) {

                    //更新附件
                    MapList docListMap = PCR.getRelatedObjects(context, RELATIONSHIP_REFERENCE_DOCUMENT, // relationship pattern
                            TYPE_DOCUMENT,                                    // object pattern
                            typeSelectList,                            // object selects
                            reSelectList, // relationship selects
                            false,                                        // to direction
                            true,                                        // from direction
                            (short) 1,                                    // recursion level
                            "",                // object where clause
                            "attribute[Project Role]=='before'",
                            (short) 0);
                    //是否需要连接标识
                    boolean isConnectQQFile = true;
                    Set disDocIdSet = new HashSet<String>();
                    if (docListMap.size() > 0) {
                        for (int i = 0; i < docListMap.size(); i++) {
                            Map docMap = (Map) docListMap.get(i);
                            String strDocId = (String) docMap.get(SELECT_ID);
                            String strRelId = (String) docMap.get(SELECT_RELATIONSHIP_ID);
                            if (strDocId.equals(strJFECRQQFileId)) {
                                isConnectQQFile = false;
                            } else {
                                disDocIdSet.add(strRelId);
                            }
                        }
                    }
                    if (isConnectQQFile) {
                        if (UIUtil.isNotNullAndNotEmpty(strJFECRQQFileId)) {
                            DomainRelationship rel = DomainRelationship.connect(context, PCR, RELATIONSHIP_REFERENCE_DOCUMENT, DomainObject.newInstance(context, strJFECRQQFileId));
                            Map relAttrMap = new HashMap<>();
                            relAttrMap.put(JF_PLMConstants_mxJPO.ATTR_PROJECT_ROLE, "before");
                            rel.setAttributeValues(context, relAttrMap);
                        }
                        //断开原有关系
                        _logger.info("disDocIdSet:{}", disDocIdSet);
                        if (disDocIdSet.size() > 0) {
                            DomainRelationship.disconnect(context, StringList.create(disDocIdSet).toStringArray());
                        }
                    }
                    //更新问题清单附件
                    MapList docListMap2 = PCR.getRelatedObjects(context, RELATIONSHIP_REFERENCE_DOCUMENT, // relationship pattern
                            TYPE_DOCUMENT,                                    // object pattern
                            typeSelectList,                            // object selects
                            reSelectList, // relationship selects
                            false,                                        // to direction
                            true,                                        // from direction
                            (short) 1,                                    // recursion level
                            "",                // object where clause
                            "attribute[Project Role]=='after'",
                            (short) 0);
                    //是否需要连接标识
                    boolean isConnectQQFile2 = true;
                    Set disDocIdSet2 = new HashSet<String>();
                    if (docListMap2.size() > 0) {
                        for (int i = 0; i < docListMap2.size(); i++) {
                            Map docMap = (Map) docListMap2.get(i);
                            String strDocId = (String) docMap.get(SELECT_ID);
                            String strRelId = (String) docMap.get(SELECT_RELATIONSHIP_ID);
                            if (strDocId.equals(strJFECRQQFileId2)) {
                                isConnectQQFile2 = false;
                            } else {
                                disDocIdSet2.add(strRelId);
                            }
                        }
                    }
                    if (isConnectQQFile2) {
                        if (UIUtil.isNotNullAndNotEmpty(strJFECRQQFileId2)) {
                            DomainRelationship rel = DomainRelationship.connect(context, PCR, RELATIONSHIP_REFERENCE_DOCUMENT, DomainObject.newInstance(context, strJFECRQQFileId2));
                            Map relAttrMap = new HashMap<>();
                            relAttrMap.put(JF_PLMConstants_mxJPO.ATTR_PROJECT_ROLE, "after");
                            rel.setAttributeValues(context, relAttrMap);
                        }
                        //断开原有关系
                        _logger.info("disDocIdSet:{}", disDocIdSet2);
                        if (disDocIdSet2.size() > 0) {
                            DomainRelationship.disconnect(context, StringList.create(disDocIdSet2).toStringArray());
                        }
                    }

                    //更换PCR主项目
                    if(UIUtil.isNotNullAndNotEmpty(mainProjectOID)){
                        String relid=PCR.getInfo(context,"from[JFChange2Project].id");
                        _logger.info("relid:{}", relid);
                        if(UIUtil.isNotNullAndNotEmpty(relid)){
                            DomainRelationship.disconnect(context,relid);
                        }
                        DomainRelationship.connect(context, strObjectId, "JFChange2Project", mainProjectOID, false);
                    }
                    //更换PCR涉及项目
                    if(UIUtil.isNotNullAndNotEmpty(JFProjectNameOID)){
                        StringList relidlist=PCR.getInfoList(context,"from[JFPCR2Project].id");
                        _logger.info("relidlist:{}", relidlist);
                        if(relidlist.size()>0){
                            for(String relid:relidlist){
                                DomainRelationship.disconnect(context,relid);
                            }
                        }
                        String[] projectidlist=JFProjectNameOID.split("\\|");
                        for (String projectid:projectidlist){
                            DomainRelationship.connect(context, strObjectId, "JFPCR2Project", projectid, false);
                        }

                    }else {
                        //OID为空，JFProjectNameDisplay为空，即是清空
                        if(!UIUtil.isNotNullAndNotEmpty(JFProjectNameDisplay)){
                            StringList relidlist=PCR.getInfoList(context,"from[JFPCR2Project].id");
                            _logger.info("relidlist:{}", relidlist);
                            if(relidlist.size()>0){
                                for(String relid:relidlist){
                                    DomainRelationship.disconnect(context,relid);
                                }
                            }
                        }
                    }



//                }
            }
            ContextUtil.commitTransaction(context);
        } catch (FrameworkException e) {
            _logger.error(e.getMessage());
            ContextUtil.abortTransaction(context);
            throw e;
        } finally {
            ContextUtil.popContext(context);
        }
        res.put("Action", "CONTINUE");
        _logger.info("-----------------------------  processEditPCRJPO end ---------------------------------------");
        return res;
    }

    /**
     * @return void
     * @Author Liuxg
     * @Description 执行任务提交的时候文档提升到冻结（PolicyProjectTaskStateActivePromoteAction）
     * @Date 2025/12/22 17:25
     * @Param [context, args]
     **/
    public void setDocStateForzen(Context context, String[] args) throws Exception {
        _logger.info("setDocStateForzen--->start");
        try {
            String JF_PCRExecuteTaskid = args[0];
            DomainObject taskObj = DomainObject.newInstance(context, JF_PCRExecuteTaskid);
            String type = taskObj.getTypeName(context);
            _logger.info("JF_PCRExecuteTaskid--->"+JF_PCRExecuteTaskid);
            _logger.info("type--->"+type);
            if ("JF_PCRExecuteTask".equals(type)) {
                StringList idlist = taskObj.getInfoList(context, "from[Reference Document].to.id");
                for (String docid : idlist) {
                    MqlUtil.mqlCommand(context, false, "mod bus " + docid + " current FROZEN", true);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * @return void
     * @Author Liuxg
     * @Description 执行任务完成的时候文档提升到发布
     * @Date 2025/12/22 19:26
     * @Param [context, args]
     **/
    public void setDocStateRELEASED(Context context, String[] args) throws Exception {
        _logger.info("setDocStateRELEASED--->start");
        try {
            String JF_PCRExecuteTaskid = args[0];
            DomainObject taskObj = DomainObject.newInstance(context, JF_PCRExecuteTaskid);
            StringList idlist = taskObj.getInfoList(context, "from[Reference Document].to.id");
            for (String docid : idlist) {
                MqlUtil.mqlCommand(context, false, "mod bus " + docid + " current RELEASED", true);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * @return void
     * @Author Liuxg
     * @Description 执行任务驳回的时候文档退回到工作中（PolicyProjectTaskStateReviewDemoteAction）
     * @Date 2025/12/22 19:35
     * @Param [context, args]
     **/
    public void setDocStateIN_WORK(Context context, String[] args) throws Exception {
        _logger.info("setDocStateIN_WORK--->start");
        try {
            String JF_PCRExecuteTaskid = args[0];
            DomainObject taskObj = DomainObject.newInstance(context, JF_PCRExecuteTaskid);
            String type = taskObj.getTypeName(context);
            _logger.info("JF_PCRExecuteTaskid--->"+JF_PCRExecuteTaskid);
            _logger.info("type--->"+type);
            if ("JF_PCRExecuteTask".equals(type)) {
                StringList idlist = taskObj.getInfoList(context, "from[Reference Document].to.id");
                for (String docid : idlist) {
                    MqlUtil.mqlCommand(context, false, "mod bus " + docid + " current IN_WORK", true);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    /*
     * @description:PCR从草稿提升到批准的时候校验SDT角色是否完整
     * @author: caipan
     * @date: 2025/12/24 14:09:43
     * @param: * @param[1] context
     * @param[2] args
     * @return:
     **/
    public int checkSDTRole(Context context,String[] args) throws Exception{
        String objectId = args[0];
        int flag = 0;
        DomainObject obj = DomainObject.newInstance(context,objectId);
        StringList projectList = obj.getInfoList(context, "from[JFChange2Project].to.id");
        MapList list = readSignTaskPropertiesFile(context, STRING_SIGN_TASK_PROPERTIES_ZH, "JFPCRTask");//拿到配置的角色

        Map projectPersonAndProjectRole = JF_SignTask_mxJPO.getProjectPersonAndProjectRole(context, projectList.get(0), "PS", DomainConstants.EMPTY_STRING, "attribute[Project Role]!=''");
        //该项目所有的角色列表
        MapList mapList = (MapList) projectPersonAndProjectRole.get("mapList");
        //需要按照project role分组
        String STRING_MQL_ATTRIBUTE = "attribute[%s].value";
        Map projectRoleMap = (Map) mapList.stream().collect(Collectors.groupingBy(m -> {
            Map map = (Map) m;
            String projectRole = UIUtil.getValue(map, String.format(STRING_MQL_ATTRIBUTE, DomainConstants.ATTRIBUTE_PROJECT_ROLE));
            return projectRole;
        }));
        StringList errorRole = new StringList();
        for (int i = 0; i < list.size(); i++) {
            Map requestMap = (Map) list.get(i);
            String role = UIUtil.getValue(requestMap, "id");
            String title = UIUtil.getValue(requestMap, "title");
            if(!projectRoleMap.containsKey(role)){
                //项目缺失关键的SDT成员:
                if(!errorRole.contains(role)) {
                    role = role.replace(" ", "_");
                    String roleName="emxFramework.Range.Project_Role."+role;
                    String message = EnoviaResourceBundle.getProperty(context, "emxFrameworkStringResource", context.getLocale(), roleName);
                    errorRole.add(message);
                }
            }
        }
        String messageHead = EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", context.getLocale(), "emxComponents.PCR.checkSDTRole");
       _logger.info("errorRole:{}",errorRole);
        if(errorRole.size()>0){
            messageHead=messageHead+errorRole.join(",");
            emxContextUtil_mxJPO.mqlNotice(context, messageHead);
        }
        _logger.info("projectRoleMap:{}",projectRoleMap);
        return 0;
    }
    /*
     * @description:验证阶段 提交按钮的权限 JFPCRSupplySubmitCmd JFPCRValidateConclusionSubmitCmd
     * @author: caipan   没有发起流程、或者有流程拒绝了返回true
     * @date: 2025/12/29 14:39:38
     * @param: * @param[1] context
     * @param[2] args
     * @return:
     **/
    public boolean VerificationSubmitAccess(Context context,String[] args) throws Exception{
        Map paramsMap = (Map) JPO.unpackArgs(args);
        String strObjectId = (String) paramsMap.get(STRING_OBJECTID);
        _logger.info("strObjectId:{}",strObjectId);
        //如果关联了流程，并且拒绝了
        DomainObject pcr = DomainObject.newInstance(context,strObjectId);
        StringList  selList = JF_Util_mxJPO.basicBolistSel();
        selList.add("attribute[Route Status]");
        StringList  relList = JF_Util_mxJPO.basicRellistSel();
        relList.add("attribute[Route Base State]");
        String current = pcr.getInfo(context, SELECT_CURRENT);
        String policy  = pcr.getInfo(context, SELECT_POLICY);
        String symbolState = FrameworkUtil.reverseLookupStateName(context, policy,current);
        String relwhere = "attribute[Route Base State]=="+symbolState;
        //得到当前状态下的Route
        MapList mapList =  pcr.getRelatedObjects(context,
                RELATIONSHIP_OBJECT_ROUTE, //pattern to match relationships
                TYPE_ROUTE, //pattern to match types
                selList, //the eMatrix StringList object that holds the list of select statement pertaining to Business Obejcts.
                relList, //the eMatrix StringList object that holds the list of select statement pertaining to Relationships.
                false, //get To relationships
                true, //get From relationships
                (short) 1, //the number of levels to expand, 0 equals expand all.
                EMPTY_STRING, //where clause to apply to objects, can be empty ""
                relwhere, //where clause to apply to relationship, can be empty ""
                (short) 0); //limit
       //如果没有流程，返回true
        if(mapList.size()==0){
            return true;
        }else{
            //有流程，不为停止 返回false
            Map map =(Map) mapList.get(0);
            String Route_Status = UIUtil.getValue(map,"attribute[Route Status]");
         if(Route_Status.equalsIgnoreCase("Stopped")){
            return true;
        }else{
            return false;
        }
        }

    }
    public StringList gePCRVerificationTaskAccess(Context context, String[] args) throws Exception {
        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        Map requestMap = (Map) programMap.get(STRING_REQUESTMAP);
        _logger.info("requestMap:{}",requestMap);
        _logger.info("objectId:{}",requestMap.get("objectId"));
        String strLoginUser = context.getUser();
        boolean flag = VerificationSubmitAccess(context,JPO.packArgs(requestMap));
        _logger.info("flag:{}",flag);
        MapList objectList = (MapList) programMap.get("objectList");
        StringList editAccessList = new StringList(objectList.size());
        for (int i = 0; i < objectList.size(); i++) {
            Map objectMap = (Map) objectList.get(i);
            String owner = (String) objectMap.get(SELECT_OWNER);
            String current = (String) objectMap.get(SELECT_CURRENT);
            if(current.equalsIgnoreCase("Active")) {
                if (strLoginUser.equalsIgnoreCase(owner) && flag) {
                    editAccessList.add("true");
                } else {
                    editAccessList.add("false");
                }
            }else{
                editAccessList.add("false");
            }
        }
        return editAccessList;
    }
    /*
     * @description:执行任务的计划完成时间在评估中状态允许修改，发起审批后以及进入到更改验证后不允许修改。
     * @author: caipan
     * @date: 2026/1/15 14:01:46
     * @param: * @param[1] context
     * @param[2] args
     * @return:
     **/
    public StringList gePCRPerformTaskTaskEstimatedFinishDateAccess(Context context, String[] args) throws Exception {
        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        Map requestMap = (Map) programMap.get(STRING_REQUESTMAP);
        String strLoginUser = context.getUser();
        MapList objectList = (MapList) programMap.get("objectList");
        String objectId = (String)requestMap.get("objectId");
        DomainObject obj = DomainObject.newInstance(context,objectId);
        String pcrCurrent = obj.getInfo(context, SELECT_CURRENT);
        //获取到Pcr
        StringList editAccessList = new StringList(objectList.size());
        StringList  selList = JF_Util_mxJPO.basicBolistSel();
        selList.add("attribute[Route Status]");
        StringList  relList = JF_Util_mxJPO.basicRellistSel();
        relList.add("attribute[Route Base State]");
        //获取当前状态的流程
        DomainObject pcr = DomainObject.newInstance(context,objectId);
        String policy  = pcr.getInfo(context, SELECT_POLICY);
        String symbolState = FrameworkUtil.reverseLookupStateName(context, policy,pcrCurrent);
        String relwhere = "attribute[Route Base State]=="+symbolState;
        String zhtitle = EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", new Locale("zh_CN"), "emxComponents.PCR.IN_EvaluationInformCustomer.Title");
        String entitle = EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", new Locale("en"), "emxComponents.PCR.IN_EvaluationInformCustomer.Title");
        //得到当前状态下的Route
        boolean flag = false;
        MapList mapList =  pcr.getRelatedObjects(context,
                RELATIONSHIP_OBJECT_ROUTE, //pattern to match relationships
                TYPE_ROUTE, //pattern to match types
                selList, //the eMatrix StringList object that holds the list of select statement pertaining to Business Obejcts.
                relList, //the eMatrix StringList object that holds the list of select statement pertaining to Relationships.
                false, //get To relationships
                true, //get From relationships
                (short) 1, //the number of levels to expand, 0 equals expand all.
                EMPTY_STRING, //where clause to apply to objects, can be empty ""
                relwhere, //where clause to apply to relationship, can be empty ""
                (short) 0); //limit
        //如果没有流程，返回true
        if(mapList.size()==0){
            flag= true;
        }else {
            //有流程，不为停止 返回false
            Map map = (Map) mapList.get(0);
            String Route_Status = UIUtil.getValue(map, "attribute[Route Status]");
            if (Route_Status.equalsIgnoreCase("Stopped")) {
                flag = true;
            }
        }
        for (int i = 0; i < objectList.size(); i++) {
            Map objectMap = (Map) objectList.get(i);
            String owner = (String) objectMap.get(SELECT_OWNER);
            String current = (String) objectMap.get(SELECT_CURRENT);
            String title = (String) objectMap.get(SELECT_ATTRIBUTE_TITLE);
            _logger.info("title:{}",title);
            if(pcrCurrent.equalsIgnoreCase("IN_Evaluation")&&flag) {
                if (strLoginUser.equalsIgnoreCase(owner)) {
                    editAccessList.add("true");
                } else {
                    editAccessList.add("false");
                }
            }else  if(current.equalsIgnoreCase("Active")&&title.equalsIgnoreCase(zhtitle)||title.equalsIgnoreCase(entitle)) {
                if (strLoginUser.equalsIgnoreCase(owner)) {
                    editAccessList.add("true");
                } else {
                    editAccessList.add("false");
                }
            }else{
                editAccessList.add("false");
            }
        }
        return editAccessList;
    }
    /*
     * @description:执行任务的编辑权限
     * 执行任务的计划完成时间在评估中状态允许修改，发起审批后以及进入到更改验证后不允许修改。
     * @author: caipan
     * @date: 2026/1/14 14:25:15
     * @param: * @param[1] context
     * @param[2] args
     * @return:
     **/
    public StringList gePCRPerformTaskTaskEstimatedFinishDateAccess_old(Context context, String[] args) throws Exception {
        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        Map requestMap = (Map) programMap.get(STRING_REQUESTMAP);
        String strLoginUser = context.getUser();
        MapList objectList = (MapList) programMap.get("objectList");
        String objectId = (String)requestMap.get("objectId");
        DomainObject obj = DomainObject.newInstance(context,objectId);
        StringList selList = JF_Util_mxJPO.basicBolistSel();
        StringList relList = JF_Util_mxJPO.basicRellistSel();
        String where = "current==Review || current==Complete";
        String pcrCurrent = obj.getInfo(context, SELECT_CURRENT);
        //获取评估任务，未提交状态的
        MapList list = obj.getRelatedObjects(context,
                JF_PLMConstants_mxJPO.Rel_JFPCR2Task, //pattern to match relationships
                JF_PLMConstants_mxJPO.Type_JFPCRTask, //pattern to match types
                selList, //the eMatrix StringList object that holds the list of select statement pertaining to Business Obejcts.
                relList, //the eMatrix StringList object that holds the list of select statement pertaining to Relationships.
                false, //get To relationships
                true, //get From relationships
                (short) 1, //the number of levels to expand, 0 equals expand all.
                    where, //where clause to apply to objects, can be empty ""
                null, //where clause to apply to relationship, can be empty ""
                (short) 0); //limit
        _logger.info("list:{}",list);
        Map groupMap = (Map) list.stream().collect(Collectors.groupingBy(m -> {
            Map info = (Map) m;
            return info.get(SELECT_OWNER);
        }));
        _logger.info("groupMap:{}",groupMap);
        //获取到Pcr
        StringList editAccessList = new StringList(objectList.size());
        for (int i = 0; i < objectList.size(); i++) {
            boolean flag = true;
            Map objectMap = (Map) objectList.get(i);
            String owner = (String) objectMap.get(SELECT_OWNER);
            String current = (String) objectMap.get(SELECT_CURRENT);//执行任务
            String JF_PCRFunction = (String) objectMap.get("attribute[JF_PCRFunction]");
            if("\u8BBE\u5907".equals(JF_PCRFunction)){
                JF_PCRFunction="\u5DE5\u827A";
            }
            String copyJF_PCRFunction=JF_PCRFunction;
            _logger.info("JF_PCRFunction:{}",JF_PCRFunction);
            if(pcrCurrent.equalsIgnoreCase("IN_Evaluation")||pcrCurrent.equalsIgnoreCase("Execution")) {
                ArrayList taskList = (ArrayList)groupMap.get(owner);//评估任务
                if(null!=taskList)
                //获取评估任务的状态
                {
                    _logger.info("taskList:{}",taskList);
                try {
                    flag = taskList.stream().anyMatch(item -> {
                        Map mapInfo = (Map) item;
                        _logger.info("mapInfo:{}",mapInfo);
                        String itemTitle = UIUtil.getValue(mapInfo, SELECT_ATTRIBUTE_TITLE);
                       /* if (itemTitle.equals("\u4F9B\u5E94\u5546\u8D28\u91CF") || itemTitle.equals("\u91C7\u8D2D")) {//如果评估任务的标题为供应商质量或者采购就直接改成供应链
                            itemTitle = "\u4F9B\u5E94\u94FE";//因为需要的执行任务是对应的供应链
                        }*/
                        boolean mask = true;
                        if (itemTitle.contains(copyJF_PCRFunction)) {
                            mask = false;
                        }
                        //如果执行任务是设备的情况下，评估任务是工艺也算有值
                        else if (copyJF_PCRFunction.contains("\u8BBE\u5907") && (itemTitle.equals("\u5DE5\u827A"))) {
                            mask = false;
                        }
                        return mask;
                    });

                }catch (Exception e){
                    e.printStackTrace();
                }
                }else{
                    flag=true;
                }
                _logger.info("flag:{}",flag);
                if (strLoginUser.equalsIgnoreCase(owner) && flag) {
                    editAccessList.add("true");
                } else {
                    editAccessList.add("false");
                }
            }else{
                editAccessList.add("false");
            }
        }
        return editAccessList;
    }
    public StringList gePCRTaskDescriptionAccess(Context context, String[] args) throws Exception {
        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        Map requestMap = (Map) programMap.get(STRING_REQUESTMAP);
        String strLoginUser = context.getUser();
        MapList objectList = (MapList) programMap.get("objectList");
        StringList editAccessList = new StringList(objectList.size());
        boolean flag = isPM(context,args);
        for (int i = 0; i < objectList.size(); i++) {
            Map objectMap = (Map) objectList.get(i);
            String owner = (String) objectMap.get(SELECT_OWNER);
            String current = (String) objectMap.get(SELECT_CURRENT);
            if(current.equalsIgnoreCase("Review")&&flag) {
                    editAccessList.add("true");
            }else{
                editAccessList.add("false");
            }
        }
        return editAccessList;
    }
    public String showJF_EvaluateMessage(Context context,String[] args) throws Exception{
        _logger.info("showSwitchingDate");
        Map inputMap = (Map) JPO.unpackArgs(args);
        HashMap requestMap = (HashMap) inputMap.get("requestMap");
        String objectId = UIUtil.getValue(requestMap,"objectId");
        DomainObject obj = DomainObject.newInstance(context,objectId);
        String swithdate = obj.getAttributeValue(context,JF_PLMConstants_mxJPO.Attr_JF_EvaluateMessage);
        String label = EnoviaResourceBundle.getProperty(context, "emxFrameworkStringResource", context.getLocale(), "emxFramework.Attribute.JF_EvaluateMessage");
        String outputStr= label+":"+swithdate;
        return outputStr;
    }
    /*
     * @description:拒绝评估任务的时候，清空流程相关信息
     * @author: caipan
     * @date: 2026/1/8 16:33:31
     * @param: * @param[1] context
     * @param[2] args
     * @return:
     **/
    public void clearEvaluationReviewInfo(Context context,String[] args) throws Exception{
        String objectId = args[0];
        DomainObject pcr= DomainObject.newInstance(context,objectId);
        Map map = new HashMap();
        map.put(JF_PLMConstants_mxJPO.ATTR_JF_EvaluationConclusion,"");
        map.put(JF_PLMConstants_mxJPO.ATTR_JF_IsEvaluationInformCustomer,"");
        map.put(JF_PLMConstants_mxJPO.Attr_JF_EvaluateMessage,"");
        map.put("JF_PCREvaluationInformResponsible","");
        pcr.setAttributeValues(context,map);
    }
    /*
     * @description:拒绝任务的时候，清空更改验证审核信息
     * @author: caipan
     * @date: 2026/1/8 16:37:17
     * @param: * @param[1] context
     * @param[2] args
     * @return:
     **/
    public void clearVerificationReviewInfo(Context context,String[] args) throws Exception{
        String objectId = args[0];
        DomainObject pcr= DomainObject.newInstance(context,objectId);
        Map map = new HashMap();
//        map.put(JF_PLMConstants_mxJPO.ATTR_JF_NeedValidation,"");
        map.put(JF_PLMConstants_mxJPO.ATTR_JF_validateConclusion,"");
        map.put(JF_PLMConstants_mxJPO.attr_JF_BreakpointSwitchingDate,"");
        pcr.setAttributeValues(context,map);
        //验证任务状态退回到Active中，附件也回到工作中
        setJF_PCR2VerificationTaskReview(context,new String[]{objectId,"Active","IN_WORK"});
    }
    /*
     * @description:自己写command参数
     * @author: caipan
     * @date: 2026/1/12 21:43:18
     * @param: * @param[1] context
     * @param[2] args
     * @return:
     **/
    public boolean JFPCRBreakpointSwitchingDateCmdSetting(Context context, String[] args) throws Exception {
        _logger.info("JFPCRBreakpointSwitchingDateCmdSetting");
        HashMap argMap = (HashMap) JPO.unpackArgs(args);
        String objectId = UIUtil.getValue(argMap,"objectId");
        _logger.info("objectId:{}",objectId);
        DomainObject object = DomainObject.newInstance(context,objectId);
        String dateStr = object.getAttributeValue(context,JF_PLMConstants_mxJPO.attr_JF_BreakpointSwitchingDate);
        String strNewValue="";
        if(UIUtil.isNotNullAndNotEmpty(dateStr)) {
            SimpleDateFormat dateFormat = new SimpleDateFormat(eMatrixDateFormat.getEMatrixDateFormat(), Locale.US);
            Date d = dateFormat.parse(dateStr);
            SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd");
             strNewValue = format.format(d);
        }
    /*    Date newDate = dateFormat.parse(strNewValue);
        Calendar constraintDate = Calendar.getInstance();
        constraintDate.setTime(newDate);
        strNewValue = dateFormat.format(constraintDate.getTime());*/

        _logger.info("date:{}",dateStr);
        Map map =(Map)argMap.get("SETTINGS");
        map.put("Default", strNewValue);
        return true;
    }
    /*
     * @description:审批人员更新任务驳回的信息
     * @author: caipan
     * @date: 2026/1/13 14:19:46
     * @param: * @param[1] context
     * @param[2] args
     * @return:
     **/
    public void updateDescription(Context context,String[] args) throws Exception{
        HashMap programMap = (HashMap)JPO.unpackArgs(args);
        HashMap paramMap = (HashMap)programMap.get("paramMap");
        String objectId  = (String)paramMap.get("objectId");
        String description = (String)paramMap.get("New Value");
        DomainObject obj = DomainObject.newInstance(context,objectId);
        ContextUtil.pushContext(context);
        obj.setDescription(context, description);
        ContextUtil.popContext(context);

    }
    /*
     * @description:设置验证任务为Review
     * @author: caipan
     * @date: 2026/1/14 23:10:48
     * @param: * @param[1] context
     * @param[2] args
     * @return:
     **/
    public void setJF_PCR2VerificationTaskReview(Context context,String[] args) throws Exception{
        _logger.info("setJF_PCR2VerificationTaskReview");
        DomainObject pcrObj = DomainObject.newInstance(context,args[0]);
        String taskState = args[1];
        String fileState = args[2];
        StringList taskList = pcrObj.getInfoList(context,"from[JF_PCR2VerificationTask].to.id");
        for (int i = 0; i <taskList.size() ; i++) {
            MqlUtil.mqlCommand(context, false, "mod bus " + taskList.get(i) + " current "+taskState, true);
            pcrObj.setId(taskList.get(i));
            StringList docList = pcrObj.getInfoList(context,"from[Reference Document].to.id");
            for (int j = 0; j <docList.size() ; j++) {
                MqlUtil.mqlCommand(context, false, "mod bus " + docList.get(j) + " current "+fileState, true);
            }
        }
    }

    /**
     * 1. JFProjectPhase初始化
     * 2. 项目空间选择后驱动变化
     * 【PCR创建页面/属性编辑页面
     * 【项目阶段】属性根据项目是否有DV阶段显示对应的range选项值，即：
     * （1）有DV项目计划：选项值为Phase1至Phase5,SOP后完整选项；
     * （2）无DV项目计划：选项值为Phase1、Phase2+3、SOP后
     * @param context
     * @param args
     * @author LIUJR
     * @throws
     * @return java.util.Map
     * @date 2026/5/20 10:47
     * @description
     */
    public Map getJFProjectPhaseReloadRange(Context context, String[] args) throws Exception {
        Map returnMap = new HashMap();
        try {
            Map map = (Map) JPO.unpackArgs(args);
            _logger.info("map:{}", map);
            Map requestMap = (Map) map.get("requestMap");
            String objectId = (String) requestMap.get(JF_PLMConstants_mxJPO.STRING_OBJECTID);
            Map fieldMap = (Map) map.get("fieldMap");
            String name = (String) fieldMap.get(SELECT_NAME);
            StringList ranges = FrameworkUtil.getRanges(context, name);
            ranges.remove("");
            _logger.info("ranges:{}", ranges);
            if (!map.containsKey("fieldValues")) {
                if (UIUtil.isNullOrEmpty(objectId)) {
                    //初始化 创建页面
                    _logger.info("创建页面:{}", ranges);
                    ranges.remove("Phase2+3");
                    ranges.sort();
                    returnMap.put("field_choices", ranges);
                    returnMap.put("field_display_choices", ranges);
                } else {
                    _logger.info("详情界面:{}", ranges);
                    //修改界面
                    DomainObject object = DomainObject.newInstance(context, objectId);
                    String psId = object.getInfo(context, "from[JFChange2Project].to.id");
                    _logger.info("psId:{}", psId);
                    Boolean noDVFlag = JF_ESO_mxJPO.projectSpaceHasDVFlag(context, DomainObject.newInstance(context, psId));
                    _logger.info("noDVFlag:{}", noDVFlag);
                    if (noDVFlag) {
                        ranges.remove("Phase2");
                        ranges.remove("Phase3");
                    } else {
                        ranges.remove("Phase2+3");
                    }
                    ranges.sort();
                    returnMap.put("field_choices", ranges);
                    returnMap.put("field_display_choices", ranges);
                }
                _logger.info("returnMap:{}", returnMap);
                return returnMap;
            }
            _logger.info("onchange handler 事件:{}", ranges);
            HashMap fieldValues = (HashMap)map.get("fieldValues");   //里面是驱动他变更的fieldName And Value
            String strJFProjectNameOID = (String) fieldValues.get("mainProjectOID");
//            String strJFProjectName = (String) fieldValues.get("JFProjectName");
            Boolean noDVFlag = JF_ESO_mxJPO.projectSpaceHasDVFlag(context, DomainObject.newInstance(context, strJFProjectNameOID));
            if (noDVFlag) {
                ranges.remove("Phase2");
                ranges.remove("Phase3");
            } else {
                ranges.remove("Phase2+3");
            }
            ranges.sort();
            _logger.info("ranges:{}", ranges);
            returnMap.put("RangeValues", ranges);
            returnMap.put("RangeDisplayValues", ranges);
        }catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
        return returnMap;
    }

}