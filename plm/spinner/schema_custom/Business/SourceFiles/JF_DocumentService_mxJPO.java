import com.matrixone.apps.common.CommonDocument;
import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;

import com.matrixone.apps.domain.util.EnoviaResourceBundle;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.util.PersonUtil;
import com.matrixone.apps.domain.util.i18nNow;

import com.matrixone.apps.domain.DomainRelationship;
import com.matrixone.apps.domain.util.*;

import com.matrixone.apps.framework.ui.UIUtil;
import matrix.db.*;
import matrix.util.StringList;
import org.apache.commons.text.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.Set;


import static com.matrixone.apps.domain.DomainConstants.EMPTY_STRING;
import static com.matrixone.apps.domain.DomainConstants.SELECT_ID;

/**
 * @author CHENYAN
 * @version v1.0.0
 * 创建时间：2024/7/17 16:24
 * @description  文档业务处理类
 */
public class JF_DocumentService_mxJPO {
    private static final Logger _logger =  LoggerFactory.getLogger(JF_DocumentService_mxJPO.class);
    /**
    *
    *@description 构造调用3DPlay预览URL
    *@param context
	*@param args [0] 文档id
    *@return java.lang.String
    *@throws
    *@author CHENYAN
    *@date 2024/7/17 17:08
    */
    public static String getWidgit3DPlayUrl(Context context,String[] args) throws Exception{
        String strDocId = args[0];
        Map paramMap = new HashMap();
        paramMap.put("objectId", strDocId);
        String strI18nViewerTip = i18nNow.getViewerI18NString("Default", context.getSession().getLanguage());
        String[] strings = JPO.packArgs(paramMap);
        MapList fileList = JPO.invoke(context, "JF_DocumentTrigger", null, "getFiles", strings, MapList.class);
        _logger.info("fileList.size() {}: " , fileList.size());
        String strPdfViewUrl = "";
        String strFileId = "";
        if (fileList.size() > 0){
            //只有一个文件直接预览该文件
            if (fileList.size() == 1){
                Map file = (Map)fileList.get(0);
                strFileId = (String)file.get(JF_PLMConstants_mxJPO.OBJECT_MAP_FILE_ID);
            }else {
                for (int i = 0; i < fileList.size(); i++) {
                    Map file  = (Map) fileList.get(i);
                    String strPDFSource = (String)file.get(JF_PLMConstants_mxJPO.ATTR_PDF_SOURCE);
                    //有转换PDF文件
                    if (JF_PLMConstants_mxJPO.PDF_COVERT_FLAG.equals(strPDFSource)){
                        strFileId = (String)file.get(JF_PLMConstants_mxJPO.OBJECT_MAP_FILE_ID);
                    }
                }
                //如果没有默认第一个
                if (UIUtil.isNullOrEmpty(strFileId)){
                    Map file = (Map)fileList.get(0);
                    strFileId = (String)file.get(JF_PLMConstants_mxJPO.OBJECT_MAP_FILE_ID);
                }
            }
        }else {
            // add by chenyan 2024/07/29 新增如果没有文档的话不展示眼睛
            return "";
        }
        //获取PDF预览URL
        strPdfViewUrl = emxCommonFileUIBase_mxJPO.getPdfViewUrl(context, strFileId);
        return buildDocShowAction(strPdfViewUrl, strI18nViewerTip);
    }
    /**
    *
    *@description 构造显示文档预览眼睛
    *@param strWidgitUrl 3dPlay URL
	*@param strI18nViewerTip 提示Tip
    *@return java.lang.String
    *@throws
    *@author CHENYAN
    *@date 2024/7/17 17:10
    */
    private static String buildDocShowAction(String strWidgitUrl,String strI18nViewerTip){
        String  strShowActionChar = "";
        if (UIUtil.isNotNullAndNotEmpty(strWidgitUrl)){
            strShowActionChar = JF_PublicMethodClass_mxJPO.buildStringInStrings("<a href=\"#\" onclick=\"window.open('",strWidgitUrl,
                    "', '_blank', 'width=800,height=600'); return false;\">","<img border='0' src='../common/images/iconActionView.gif' alt=\"",
                    strI18nViewerTip, "\" title=\"",strI18nViewerTip,"\"></img></a>&#160;");

        }else {
            strShowActionChar = JF_PublicMethodClass_mxJPO.buildStringInStrings("<a href=\"#\" onclick=\"getTopWindow().showTransientMessage('没有包含 PDF 文档，不能预览', 'warning', 'alert-right-search'); return false;\">",
                    "<img border='0' src='../common/images/iconActionView.gif' alt=\"",strI18nViewerTip,"\" title=\"",strI18nViewerTip,"\"></img></a>&#160;");
        }
        _logger.info("strShowActionChar:{}",strShowActionChar);
        return strShowActionChar;
    }

    /**
    * DR、DRW、DA 表单完成后文档自动发布
    * @param context
	* @param args
    * @author LIUJR
    * @throws
    * @return void
    * @date 2024/9/6 13:29
    * @description
    */
    public void completeFormControlDocRelease(Context context, String[] args) {

    }
    /*
     * @description:PartList创建交付物的权限控制
     *   项目经理和整椅经理有权限创建
     * @author: caipan
     * @date: 2025/8/5 14:36:59
     * @param: * @param[1] context
     * @param[2] args
     * @return:
     **/
    public boolean partListDocAccess(Context context,String[] args) throws Exception{
        HashMap inputMap = (HashMap) JPO.unpackArgs(args);
        HashMap requestMap = (HashMap) inputMap.get("requestMap");
        String strObjectId = null;
        if (requestMap == null) {
            strObjectId = (String) inputMap.get("objectId");
        } else {
            strObjectId = (String) requestMap.get("objectId");
        }
        DomainObject obj = DomainObject.newInstance(context,strObjectId);
        //partList找到项目SDT成员
        String projectId = obj.getInfo(context,"to["+JF_PLMConstants_mxJPO.rel_JFProject2PartList+"].from.id");
        String pmName = JF_Util_mxJPO.getProjectManager(context,new String[]{projectId});//项目经理
        JF_ECRProcess_mxJPO process = new JF_ECRProcess_mxJPO();
        Map chairManager = process.getProjectRole(context, projectId, "Chair manager");
        String chariName = UIUtil.getValue(chairManager, DomainObject.SELECT_NAME);
        String currentName = context.getUser();
        JF_PartList_mxJPO pl = new JF_PartList_mxJPO();
        boolean flag = pl.isProcessUnderApproval(context,new String[]{strObjectId});
        String current = obj.getInfo(context,DomainConstants.SELECT_CURRENT);
        //PM是工作中   ，审核中的情况下需要还正在审批
        if("InWork".equalsIgnoreCase(current)&&currentName.equalsIgnoreCase(pmName)){
            return true;
        }
        else if("Review".equalsIgnoreCase(current)&&(currentName.equalsIgnoreCase(chariName)||currentName.equalsIgnoreCase(pmName))){
            return true&&flag;
        }
        return false;
    }

    /**
     * @Author Liuxg
     * @Description 文档回执确认上传页面
     * @Date 2025/9/10 19:39
     * @Param [context, args]
     * @return java.lang.String
    **/
    public String JF_DocReceiptConfirmationDoc(Context context,String[] args) throws Exception {
        StringBuilder sb = new StringBuilder();
        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        _logger.info("JF_DocReceiptConfirmationDoc-----programMap:{}",programMap);
        HashMap requestMap = (HashMap) programMap.get("requestMap");
        String strMode = (String) requestMap.get("mode");
        String documentId = (String) requestMap.get("objectId");
        DomainObject documentObj = DomainObject.newInstance(context, documentId);


        String addContributor = EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", context.getLocale(), "emxComponents.Common.SignatoriesAddPerson");
        String remove = EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", context.getLocale(), "emxComponents.Common.RemoveReceiptConfirmationDoc");


        String DocReceiptTitle="";
        Map DocReceiptTitleMap=new HashMap();

        StringList docidlist=documentObj.getInfoList(context,"from[Active Version].to.id");
        for(int i=0;i<docidlist.size();i++){
            String sdocid=docidlist.get(i);
            DomainObject docobj=DomainObject.newInstance(context,sdocid);
            String  JF_DocReceiptConfirmation =docobj.getAttributeValue(context,"JF_DocReceiptConfirmation");
            if("Y".equals(JF_DocReceiptConfirmation)){
                String ReceiptTitle=docobj.getAttributeValue(context,"Title");
                if(UIUtil.isNotNullAndNotEmpty(DocReceiptTitle)){
                    DocReceiptTitle=DocReceiptTitle+","+ReceiptTitle;
                }else {
                    DocReceiptTitle=  ReceiptTitle;
                }

                DocReceiptTitleMap.put(sdocid,ReceiptTitle);
            }
        }

        //当前是否需要文档发起人标志
        String docflag=documentObj.getAttributeValue(context,"JF_DocReceiptConfirmation");

        if (UIUtil.isNotNullAndNotEmpty(strMode)) {
            if ("edit".equalsIgnoreCase(strMode)&&context.getUser().equals(documentObj.getOwner(context).getName())&&documentObj.getCurrentState(context).getName().equals("FROZEN")&&"Y".equals(docflag)) {
                //上传页面
                sb.append("<table>");
                sb.append("<tr>");
                sb.append("<th rowspan=\"2\">");
                sb.append("<select id= \"ReceiptDocSelect\"  name=\"ReceiptDocSelect\" style=\"width:200px\" multiple=\"multiple\">");
                if (UIUtil.isNotNullAndNotEmpty(documentId)){
                    Set set=DocReceiptTitleMap.keySet();
                    for (Object keyObj:set) {
                        String RedocTitle= (String) DocReceiptTitleMap.get(keyObj);
                        sb.append("<option value=\"" + keyObj + "\" >");
                        //XSSOK
                        sb.append(RedocTitle);
                        sb.append("</option>");
                    }
                }
                sb.append("</select>");
                sb.append("</th>");
                sb.append("<td>");


                sb.append("<input type=\"file\"   id= \"FileFullPath\"  multiple name=\"FilePath\" size=\"18\" onChange=\"javascript=checkInECRAttachment()\"  style=\"width: 160px;\" />");


                //sb.append("</div>");
                sb.append("</td>");
                sb.append("</tr>");
                sb.append("<tr>");
                sb.append("<td>");
                sb.append("<a href=\"javascript:clearECMAttachment()\">");
                sb.append("<img src=\"../common/images/iconStatusRemoved.gif\" width=\"12\" height=\"12\" border=\"0\" />");
                sb.append("</a>");
                sb.append("<a href=\"javascript:clearECMAttachment()\">");
                //XSSOK
                sb.append(remove);
                sb.append("</a>");
                sb.append("</td>");
                sb.append("</tr>");
                sb.append("</table>");



                return sb.toString();
            }else if ("view".equalsIgnoreCase(strMode)) {
                //正常显示回执文件名
                return DocReceiptTitle;
            }else {
                return DocReceiptTitle;
            }
        }else {
            //正常显示回执文件名
            return DocReceiptTitle;
        }

    }

    public String JF_DocReceiptConfirmationSpecialist(Context context,String[] args) throws Exception {
        StringBuilder sb = new StringBuilder();
        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        _logger.info("JF_DocReceiptConfirmationDoc-----programMap:{}",programMap);
        HashMap requestMap = (HashMap) programMap.get("requestMap");
        String strMode = (String) requestMap.get("mode");
        String documentId = (String) requestMap.get("objectId");
        DomainObject documentObj = DomainObject.newInstance(context, documentId);


        String addContributor = EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", context.getLocale(), "emxComponents.Common.SignatoriesAddPerson");
        String remove = EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", context.getLocale(), "emxComponents.Common.RemoveReceiptConfirmationDoc");


        String DocReceiptTitle="";
        Map DocReceiptTitleMap=new HashMap();

        StringList docidlist=documentObj.getInfoList(context,"from[Active Version].to.id");
        for(int i=0;i<docidlist.size();i++){
            String sdocid=docidlist.get(i);
            DomainObject docobj=DomainObject.newInstance(context,sdocid);
            String  JF_DocReceiptConfirmation =docobj.getAttributeValue(context,"JF_DocSpecialistReceipt");
            if("Y".equals(JF_DocReceiptConfirmation)){
                String ReceiptTitle=docobj.getAttributeValue(context,"Title");
                if(UIUtil.isNotNullAndNotEmpty(DocReceiptTitle)){
                    DocReceiptTitle=DocReceiptTitle+","+ReceiptTitle;
                }else {
                    DocReceiptTitle=  ReceiptTitle;
                }

                DocReceiptTitleMap.put(sdocid,ReceiptTitle);
            }
        }

        String JF_ConnProjectId=documentObj.getAttributeValue(context,"JF_ConnProjectId");
        String strJF_DocSpecialty = documentObj.getAttributeValue(context, JF_PLMConstants_mxJPO.ATTR_JF_DocSpecialty);
        String strJF_ProjectDocType = documentObj.getAttributeValue(context, JF_PLMConstants_mxJPO.ATTR_JF_ProjectDocType);
        String docflag=documentObj.getAttributeValue(context,"JF_DocSpecialistReceipt");
        //20260805 update by ljr 专家回执权限与流程发起使用相同的“项目-专家组-人员”类型、专业匹配规则。
        boolean isCurrentUserExpert = getJFProject2ExpertsGroup(
                context, JF_ConnProjectId, strJF_ProjectDocType, strJF_DocSpecialty);
        _logger.info("getJFProject2ExpertsGroup--->{}", isCurrentUserExpert);
        _logger.info("strMode--->"+strMode);
        _logger.info("getCurrentState--->"+documentObj.getCurrentState(context).getName());
        _logger.info("docflag--->"+docflag);
        if (UIUtil.isNotNullAndNotEmpty(strMode)) {
            //冻结和专家可见
            if ("edit".equalsIgnoreCase(strMode)&&isCurrentUserExpert&&documentObj.getCurrentState(context).getName().equals("FROZEN")&&"Y".equals(docflag)) {
                //上传页面
                sb.append("<table>");
                sb.append("<tr>");
                sb.append("<th rowspan=\"2\">");
                sb.append("<select id= \"ReceiptDocSelect2\"  name=\"ReceiptDocSelect2\" style=\"width:200px\" multiple=\"multiple\">");
                if (UIUtil.isNotNullAndNotEmpty(documentId)){
                    Set set=DocReceiptTitleMap.keySet();
                    for (Object keyObj:set) {
                        String RedocTitle= (String) DocReceiptTitleMap.get(keyObj);
                        sb.append("<option value=\"" + keyObj + "\" >");
                        //XSSOK
                        sb.append(RedocTitle);
                        sb.append("</option>");
                    }
                }
                sb.append("</select>");
                sb.append("</th>");
                sb.append("<td>");


                sb.append("<input type=\"file\"   id= \"FileFullPath2\"  multiple name=\"FilePath2\" size=\"18\" onChange=\"javascript=checkInECRAttachmentSpecial()\"  style=\"width: 160px;\" />");


                //sb.append("</div>");
                sb.append("</td>");
                sb.append("</tr>");
                sb.append("<tr>");
                sb.append("<td>");
                sb.append("<a href=\"javascript:clearECMAttachmentSpecial()\">");
                sb.append("<img src=\"../common/images/iconStatusRemoved.gif\" width=\"12\" height=\"12\" border=\"0\" />");
                sb.append("</a>");
                sb.append("<a href=\"javascript:clearECMAttachmentSpecial()\">");
                //XSSOK
                sb.append(remove);
                sb.append("</a>");
                sb.append("</td>");
                sb.append("</tr>");
                sb.append("</table>");



                return sb.toString();
            }else if ("view".equalsIgnoreCase(strMode)) {
                //正常显示回执文件名
                return DocReceiptTitle;
            }else {
                return DocReceiptTitle;
            }
        }else {
            //正常显示回执文件名
            return DocReceiptTitle;
        }

    }


    /**
     * @Author Liuxg
     * @Description 判断当前用户是否为项目专家组中匹配文档类型、专业的专家
     * @Date 2025/10/20 15:42
     * @Param [context, projectid, documentType, documentSpecialty]
     * @return boolean
    **/
    public boolean getJFProject2ExpertsGroup(Context context, String projectid,
                                             String documentType, String documentSpecialty) throws Exception{
        if (UIUtil.isNullOrEmpty(projectid)) {
            return false;
        }

        String currentPersonId = PersonUtil.getPersonObjectID(context, context.getUser());
        try {
            ContextUtil.pushContext(context);
            //20260805 update by ljr 复用公共专家匹配方法，避免回执权限与流程审批人采用不同的数据结构和匹配规则。
            StringList expertPersonIdList = JF_PublicMethodClass_mxJPO.getProjectDocumentExpertPersonIds(
                    context, projectid, documentType, documentSpecialty);
            return expertPersonIdList.contains(currentPersonId);
        } finally {
            ContextUtil.popContext(context);
        }
    }

    /**
     * @Author Liuxg
     * @Description 回执文件栏可见性控制
     * @Date 2025/9/11 2:29
     * @Param [context, args]
     * @return boolean
    **/
    public boolean JF_DocReceiptConfirmationDocAccess(Context context,String[] args) throws Exception{
        HashMap inputMap = (HashMap) JPO.unpackArgs(args);
        HashMap requestMap = (HashMap) inputMap.get("requestMap");
        String strObjectId = null;
        if (requestMap == null) {
            strObjectId = (String) inputMap.get("objectId");
        } else {
            strObjectId = (String) requestMap.get("objectId");
        }
        DomainObject obj = DomainObject.newInstance(context,strObjectId);
        String JF_DocReceiptConfirmation=obj.getAttributeValue(context,"JF_DocReceiptConfirmation");
        if("Y".equalsIgnoreCase(JF_DocReceiptConfirmation)){
            return true;
        }
        return false;
    }
    public boolean JF_DocReceiptConfirmationSpecialistAccess(Context context,String[] args) throws Exception{
        HashMap inputMap = (HashMap) JPO.unpackArgs(args);
        HashMap requestMap = (HashMap) inputMap.get("requestMap");
        String strObjectId = null;
        if (requestMap == null) {
            strObjectId = (String) inputMap.get("objectId");
        } else {
            strObjectId = (String) requestMap.get("objectId");
        }
        DomainObject obj = DomainObject.newInstance(context,strObjectId);
        String JF_DocSpecialistReceipt=obj.getAttributeValue(context,"JF_DocSpecialistReceipt");
        if("Y".equalsIgnoreCase(JF_DocSpecialistReceipt)){
            return true;
        }
        return false;
    }

    /**
     * @Author Liuxg
     * @Description 文档form中的编辑权限控制，
     * @Date 2025/9/11 15:20
     * @Param [context, args]
     * @return boolean
    **/
    public boolean JF_DocumentEditAccess(Context context,String[] args) throws Exception{
        HashMap inputMap = (HashMap) JPO.unpackArgs(args);
        HashMap requestMap = (HashMap) inputMap.get("requestMap");
        HashMap fieldMap = (HashMap) inputMap.get("field");
        String strObjectId = null;
        if (requestMap == null) {
            strObjectId = (String) inputMap.get("objectId");
        } else {
            strObjectId = (String) requestMap.get("objectId");
        }
        DomainObject obj = DomainObject.newInstance(context,strObjectId);
        if(context.getUser().equals(obj.getOwner(context).getName())){
            String current=obj.getCurrentState(context).getName();
            String field= (String) fieldMap.get("name");
            _logger.info("field:{}", field);
            //回执文凭
            if("JF_DocReceiptConfirmationDoc".equals(field)){
                if("PRIVATE".equals(current)||"IN_WORK".equals(current)||"FROZEN".equals(current)){
                    return true;
                }else {
                    return false;
                }
            }else {
                if("PRIVATE".equals(current)||"IN_WORK".equals(current)){
                    _logger.info("field:{}", field + ",True");
                    return true;
                }else {
                    _logger.info("field:{}", field + ",false");
                    return false;
                }
            }

        }else {
            //owner才有权限编辑
            return false;
        }


    }
    /*
     * @description:构建多选人界面
     * @author: caipan
     * @date: 2025/10/10 10:43:13
     * @param: * @param[1] context
     * @param[2] args
     * @return:
     **/
    public String buildMultipleChoiceSTDPersonHtml(Context context, String[] args) throws Exception {
        StringBuilder sb = new StringBuilder();
        //添加移除文字提示
        String strAddButton = EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", context.getLocale(), "emxComponents.Button.AddWCharDevEngineering");
        String strRemoveButton = EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", context.getLocale(), "emxComponents.Button.RemoveWCharDevEngineering");
        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        HashMap requestMap = (HashMap) programMap.get("requestMap");
        HashMap fieldMap = (HashMap) programMap.get("fieldMap");
        String strObjectId = (String) requestMap.get("objectId");
        DomainObject bo = DomainObject.newInstance(context, strObjectId);
        String strMode = EMPTY_STRING;
        String current = bo.getInfo(context, DomainConstants.SELECT_CURRENT);
        String owner = bo.getInfo(context, DomainConstants.SELECT_OWNER);
        if (requestMap.containsKey("mode")) {
            strMode = (String) requestMap.get("mode");
        } else {
            strMode = "view";
        }
        if ("FROZEN,RELEASED,OBSOLETE".contains(current)) {
            strMode = "view";
        }
        if ("edit".equalsIgnoreCase(strMode)) {
            if (!context.getUser().equalsIgnoreCase(owner))   {
                //编辑状态的时候  如果owner不是所有者为
                strMode = "view";
            }
        }
        String fieldName = (String) fieldMap.get("name");
        _logger.info("strMode:{}", strMode);
        _logger.info("strObjectId:{}", strObjectId);
        StringList personList;
        try {
            ContextUtil.pushContext(context);
            //查看视图返回name 编辑返回id
            if ("view".equalsIgnoreCase(strMode)) {
                personList = bo.getInfoList(context, "from[JFDocument2SignPerson].to.name");

            } else {
                personList = bo.getInfoList(context, "from[JFDocument2SignPerson].to.id");
            }
            _logger.info("personList:{}", personList);
        } finally {
            ContextUtil.popContext(context);
        }
        StringList viewList = new StringList();
        if ("view".equalsIgnoreCase(strMode) || "null".equalsIgnoreCase(strMode)) {
            for (int i = 0; i < personList.size(); i++) {
                String strPersonName = personList.get(i);
                String strFullName = PersonUtil.getFullName(context, strPersonName);
                viewList.add(strFullName);
            }
        }
        String strReturn = JF_PublicMethodClass_mxJPO.selectAddRemovePersonPublic(context, strMode, viewList, personList, fieldName, "addPersonContributor('"+fieldName+"')", "removePersonContributor('"+fieldName+"')");
        _logger.info("strReturn:{}", strReturn);
        return strReturn;
    }
    /*
     * @description:获取文档相关的审核人员
     * @author: caipan
     * @date: 2025/10/10 10:51:06
     * @param: * @param[1] context
     * @param[2] args
     * @return: 
     **/
    public String getDocReviewPerson(Context context ,String[] args) throws Exception{
        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        HashMap requestMap = (HashMap) programMap.get("requestMap");
        String strObjectId = (String) requestMap.get("objectId");
        String strPersonName = EMPTY_STRING;
        _logger.info("strObjectId:{}", strObjectId);
        DomainObject domainObject = DomainObject.newInstance(context,strObjectId);
        StringList list = domainObject.getInfoList(context,"from[JFDocument2ExpertPerson].to.id");
        for(int i=0;i<list.size();i++){
            if(strPersonName.isEmpty()){
                strPersonName = PersonUtil.getFullName(context, list.get(i));
            }else {
                strPersonName = strPersonName + "," + PersonUtil.getFullName(context, list.get(i));
            }
        }
        return strPersonName;
    }
    public String buildMultipleChoiceDepManagerPersonHtml(Context context, String[] args) throws Exception {
        StringBuilder sb = new StringBuilder();
        //添加移除文字提示
        String strAddButton = EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", context.getLocale(), "emxComponents.Button.AddWCharDevEngineering");
        String strRemoveButton = EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", context.getLocale(), "emxComponents.Button.RemoveWCharDevEngineering");
        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        _logger.info("programMap:{}", programMap.toString());
        HashMap requestMap = (HashMap) programMap.get("requestMap");
        HashMap fieldMap = (HashMap) programMap.get("fieldMap");
        String strObjectId = (String) requestMap.get("objectId");
        DomainObject bo = DomainObject.newInstance(context, strObjectId);
        String current = bo.getInfo(context, DomainConstants.SELECT_CURRENT);
        String strMode = EMPTY_STRING;
        if (requestMap.containsKey("mode")) {
            strMode = (String) requestMap.get("mode");
        } else {
            strMode = "view";
        }
        if ("FROZEN,RELEASED,OBSOLETE".contains(current)) {
            strMode = "view";
        }
        //文档对象
        String fieldName = (String) fieldMap.get("name");
        _logger.info("strMode:{}", strMode);
        _logger.info("strObjectId:{}", strObjectId);
        StringList personList ;
        try {
            ContextUtil.pushContext(context);
            //查看视图返回name 编辑返回id
            if("view".equalsIgnoreCase(strMode)) {
                personList = bo.getInfoList(context, "from[JFDocument2DepManager].to.name");
            }else {
                personList = bo.getInfoList(context, "from[JFDocument2DepManager].to.id");
            }
            _logger.info("personList:{}",personList);
        } finally {
            ContextUtil.popContext(context);
        }
        StringList viewList = new StringList();
        if("view".equalsIgnoreCase(strMode)) {
            for (int i = 0; i < personList.size(); i++) {
                String strPersonName = personList.get(i);
                String strFullName = PersonUtil.getFullName(context, strPersonName);
                viewList.add(strFullName);
            }
//            return fullNameList.join(",");
        }
        String strReturn = JF_PublicMethodClass_mxJPO.selectAddRemovePersonPublic(context, strMode, viewList, personList, fieldName, "addDepManagerPersonContributor('"+fieldName+"')", "removePersonContributor('"+fieldName+"')");
        _logger.info("strReturn:{}", strReturn);
        return strReturn;
    }

    /**
    * 关联文档会签和部门经理人员
    * @param context
	* @param args
    * @author LIUJR
    * @throws
    * @return void
    * @date 2025/10/13 17:14
    * @description
    */
    public void updateConnDepManagerOrSignPerson(Context context, String[] args) throws Exception{
        try {
            ContextUtil.pushContext(context);
            Map paramsMap = (Map) JPO.unpackArgs(args);
            _logger.info("paramsMap:{}", paramsMap);
            Map paramMap = (Map) paramsMap.get("paramMap");
            Map fieldMap = (Map) paramsMap.get("fieldMap");
            String objectId = UIUtil.getValue(paramMap, "objectId");
            String[] strNewValues = (String[]) paramMap.get("New Values");
            String strNewValue = (String) paramMap.get("New Value");
            String field = UIUtil.getValue(fieldMap, DomainConstants.SELECT_NAME);
            DomainObject domainObject = DomainObject.newInstance(context, objectId);
            if (!"IN_WORK".equalsIgnoreCase(domainObject.getInfo(context, DomainConstants.SELECT_CURRENT))) {
                return;
            }
            _logger.info("field:{}", field);
            String rel = EMPTY_STRING;
            if ("DepManager".equalsIgnoreCase(field)) {
                rel = "JFDocument2DepManager";
            } else if ("SignPerson".equalsIgnoreCase(field)) {
                rel = "JFDocument2SignPerson";
            }

            StringList personList = domainObject.getInfoList(context, "from[" + rel + "].id");
            //断开
            DomainRelationship.disconnect(context, personList.toStringArray());
            if (UIUtil.isNotNullAndNotEmpty(strNewValue)) {
                _logger.info("strNewValues:{}", strNewValues);
                _logger.info("strNewValues[0]:{}", strNewValues[0]);
                String[] split = strNewValues[0].split(",");
                _logger.info("len:{}", strNewValues.length);
                if (split.length > 0) {
                    DomainRelationship.connect(context, domainObject, new RelationshipType(rel), true, split);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            ContextUtil.popContext(context);
        }
    }

    /**
     * 查询尚未关联到上下文对象的可选文档。
     * 技术文档（JF_ProjectDoc=Y）仅允许选择已发布状态，非技术文档不限制状态；
     * ownerOnly=true时额外限制文档所有者为当前登录用户，供ECR入口使用。
     *
     * @param context Matrix上下文
     * @param args objectId为上下文对象ID，可选documentRelationship和ownerOnly
     * @return StringList 可添加的文档ID
     * @throws Exception 查询文档失败时抛出异常
     * @author LIUJR
     * @date 2026/8/28
     */
    @com.matrixone.apps.framework.ui.IncludeOIDProgramCallable
    public StringList getProjectOwnedDocumentIds(Context context, String[] args) throws Exception {
        Map parameterMap = (Map) JPO.unpackArgs(args);
        Map requestMap = parameterMap.get("requestMap") instanceof Map
                ? (Map) parameterMap.get("requestMap") : Collections.emptyMap();
        String contextObjectId = UIUtil.getValue(parameterMap, "objectId");
        if (UIUtil.isNullOrEmpty(contextObjectId)) {
            contextObjectId = UIUtil.getValue(requestMap, "objectId");
        }
        String documentRelationship = UIUtil.getValue(parameterMap, "documentRelationship");
        if (UIUtil.isNullOrEmpty(documentRelationship)) {
            documentRelationship = UIUtil.getValue(requestMap, "documentRelationship");
        }
        if (UIUtil.isNullOrEmpty(documentRelationship) || !documentRelationship.matches("[A-Za-z0-9_ ]+")) {
            documentRelationship = DomainConstants.RELATIONSHIP_REFERENCE_DOCUMENT;
        }
        String ownerOnlyValue = UIUtil.getValue(parameterMap, "ownerOnly");
        if (UIUtil.isNullOrEmpty(ownerOnlyValue)) {
            ownerOnlyValue = UIUtil.getValue(requestMap, "ownerOnly");
        }
        boolean ownerOnly = Boolean.parseBoolean(ownerOnlyValue);
        if (UIUtil.isNullOrEmpty(contextObjectId)) {
            return new StringList();
        }

        DomainObject contextObject = DomainObject.newInstance(context, contextObjectId);
        StringList connectedDocumentIds = contextObject.getInfoList(
                context, "from[" + documentRelationship + "].to.id");
        StringList selects = StringList.create(
                DomainConstants.SELECT_ID,
                DomainConstants.SELECT_CURRENT,
                JF_PLMConstants_mxJPO.SELECT_ATTR_JF_ProjectDoc);
        String where = ownerOnly
                ? "owner == '" + context.getUser() + "'"
                : DomainConstants.EMPTY_STRING;
        MapList documentList = DomainObject.findObjects(
                context,
                CommonDocument.TYPE_DOCUMENTS,
                DomainConstants.QUERY_WILDCARD,
                where,
                selects);
        StringList result = new StringList();
        for (Object item : documentList) {
            Map documentMap = (Map) item;
            String documentId = UIUtil.getValue(documentMap, DomainConstants.SELECT_ID);
            String current = UIUtil.getValue(documentMap, DomainConstants.SELECT_CURRENT);
            String projectDocument = UIUtil.getValue(
                    documentMap, JF_PLMConstants_mxJPO.SELECT_ATTR_JF_ProjectDoc);
            if ("Y".equalsIgnoreCase(projectDocument)
                    && !"RELEASED".equalsIgnoreCase(current)) {
                continue;
            }
            if (connectedDocumentIds.contains(documentId)) {
                continue;
            }
            result.add(documentId);
        }
        return result;
    }

    /**
     * 将已选文档关联到上下文对象，并统一复核对象权限、文档类型和所有者。
     * 调用方通过受控参数指定允许的上下文类型、状态及关系，文档所属项目和状态均不受限制。
     *
     * @param context Matrix上下文
     * @param args contextObjectId、documentIds、documentRelationship、allowedContextTypes和editableStates
     * @return Map code为200表示关联成功
     * @throws Exception 权限或文档校验失败时抛出异常
     * @author caipan by codex
     * @date 2026/8/27 16:30
     */
    public Map addExistingProjectDocuments(Context context, String[] args) throws Exception {
        Map parameterMap = (Map) JPO.unpackArgs(args);
        String contextObjectId = UIUtil.getValue(parameterMap, "contextObjectId");
        String documentRelationship = UIUtil.getValue(parameterMap, "documentRelationship");
        String[] documentIds = (String[]) parameterMap.get("documentIds");
        StringList allowedContextTypes = (StringList) parameterMap.get("allowedContextTypes");
        StringList editableStates = (StringList) parameterMap.get("editableStates");
        if (UIUtil.isNullOrEmpty(documentRelationship) || !documentRelationship.matches("[A-Za-z0-9_ ]+")) {
            throw new IllegalArgumentException("Invalid relationship configuration.");
        }

        String accessDenied = EnoviaResourceBundle.getProperty(
                context, "emxProgramCentralStringResource", context.getLocale(),
                "emxProgramCentral.Common.AccessDenied");
        String invalidDocument = EnoviaResourceBundle.getProperty(
                context, "emxProgramCentralStringResource", context.getLocale(),
                "emxProgramCentral.AddExistingProjectDocument.InvalidDocument");
        String ownerMismatch = EnoviaResourceBundle.getProperty(
                context, "emxProgramCentralStringResource", context.getLocale(),
                "emxProgramCentral.AddExistingProjectDocument.OwnerMismatch");
        if (UIUtil.isNullOrEmpty(contextObjectId) || documentIds == null || documentIds.length == 0) {
            throw new IllegalArgumentException(invalidDocument);
        }

        DomainObject contextObject = DomainObject.newInstance(context, contextObjectId);
        StringList contextSelects = StringList.create(
                DomainConstants.SELECT_TYPE,
                DomainConstants.SELECT_OWNER,
                DomainConstants.SELECT_CURRENT);
        Map contextInfo = contextObject.getInfo(context, contextSelects);
        String contextType = UIUtil.getValue(contextInfo, DomainConstants.SELECT_TYPE);
        String contextOwner = UIUtil.getValue(contextInfo, DomainConstants.SELECT_OWNER);
        String contextCurrent = UIUtil.getValue(contextInfo, DomainConstants.SELECT_CURRENT);
        if (allowedContextTypes == null || !allowedContextTypes.contains(contextType)
                || editableStates == null || !editableStates.contains(contextCurrent)
                || !context.getUser().equals(contextOwner)) {
            throw new Exception(accessDenied);
        }

        LinkedHashSet<String> uniqueDocumentIds = new LinkedHashSet<>(Arrays.asList(documentIds));
        String documentKindSelect = "type.kindof[" + CommonDocument.TYPE_DOCUMENTS + "]";
        String versionObjectSelect = "attribute[Is Version Object]";
        StringList documentSelects = StringList.create(
                DomainConstants.SELECT_ID,
                DomainConstants.SELECT_OWNER,
                documentKindSelect,
                versionObjectSelect);
        MapList documentInfoList = DomainObject.getInfo(
                context,
                uniqueDocumentIds.toArray(new String[uniqueDocumentIds.size()]),
                documentSelects);
        if (documentInfoList.size() != uniqueDocumentIds.size()) {
            throw new Exception(invalidDocument);
        }
        for (Object item : documentInfoList) {
            Map documentMap = (Map) item;
            if (!"true".equalsIgnoreCase(UIUtil.getValue(documentMap, documentKindSelect))
                    || "true".equalsIgnoreCase(UIUtil.getValue(documentMap, versionObjectSelect))) {
                throw new Exception(invalidDocument);
            }
            if (!context.getUser().equals(UIUtil.getValue(documentMap, DomainConstants.SELECT_OWNER))) {
                throw new Exception(ownerMismatch);
            }
        }

        StringList connectedDocumentIds = contextObject.getInfoList(
                context, "from[" + documentRelationship + "].to.id");
        StringList newDocumentIds = new StringList();
        for (String documentId : uniqueDocumentIds) {
            if (!connectedDocumentIds.contains(documentId)) {
                newDocumentIds.add(documentId);
            }
        }
        boolean transactionStarted = false;
        boolean contextPushed = false;
        try {
            ContextUtil.startTransaction(context, true);
            transactionStarted = true;
            if (!newDocumentIds.isEmpty()) {
                //任意状态文档可能不允许普通用户修改，完整业务校验通过后再使用后台上下文建立关系。
                ContextUtil.pushContext(context);
                contextPushed = true;
                contextObject.addRelatedObjects(
                        context,
                        new RelationshipType(documentRelationship),
                        true,
                        newDocumentIds.toStringArray());
            }
            ContextUtil.commitTransaction(context);
            transactionStarted = false;
        } catch (Exception e) {
            if (transactionStarted) {
                ContextUtil.abortTransaction(context);
            }
            throw e;
        } finally {
            if (contextPushed) {
                ContextUtil.popContext(context);
            }
        }
        Map result = new HashMap();
        result.put("code", "200");
        return result;
    }

    /**
     * 按项目属性、项目文件夹路径、任务交付物顺序解析文档所属项目ID。
     *
     * @param documentMap 文档查询结果
     * @param projectPathSelect 项目文件夹路径select
     * @param taskProjectSelect 任务交付物项目select
     * @return String 所属项目ID，无法解析时返回空字符串
     * @author caipan by codex
     * @date 2026/8/27 16:30
     */
    private String resolveProjectDocumentId(Map documentMap, String projectPathSelect, String taskProjectSelect) {
        String projectId = UIUtil.getValue(documentMap, JF_PLMConstants_mxJPO.SELECT_ATTR_JF_ConnProjectId);
        if (UIUtil.isNotNullAndNotEmpty(projectId)) {
            return projectId;
        }
        Object projectPathValue = documentMap.get(projectPathSelect);
        StringList projectPaths = projectPathValue instanceof StringList
                ? (StringList) projectPathValue
                : StringList.create(projectPathValue == null ? EMPTY_STRING : String.valueOf(projectPathValue));
        for (String projectPath : projectPaths) {
            if (UIUtil.isNotNullAndNotEmpty(projectPath)) {
                int separatorIndex = projectPath.indexOf('|');
                return separatorIndex > -1 ? projectPath.substring(0, separatorIndex) : projectPath;
            }
        }
        Object taskProjectValue = documentMap.get(taskProjectSelect);
        StringList taskProjectIds = taskProjectValue instanceof StringList
                ? (StringList) taskProjectValue
                : StringList.create(taskProjectValue == null ? EMPTY_STRING : String.valueOf(taskProjectValue));
        return taskProjectIds.isEmpty() ? EMPTY_STRING : taskProjectIds.get(0);
    }
}
