import com.dscn.plm.util.NioJDUtils;
import com.matrixone.apps.common.CommonDocument;
import com.matrixone.apps.common.util.DocumentUtil;
import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.DomainRelationship;
import com.matrixone.apps.domain.Job;
import com.matrixone.apps.domain.util.*;
import com.matrixone.apps.framework.ui.UIUtil;
import matrix.db.*;
import matrix.util.MatrixException;
import matrix.util.StringList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created with IntelliJ IDEA.
 * Copyright@ Apache Open Source Organization
 *
 * @Auther: HLY
 * @Date: 2023-08-07-10:43
 * @Description: Document 触发器 的延迟 JPO
 */
public class JF_DocumentDeferredTrigger_mxJPO {
    private static final Logger _logger =  LoggerFactory.getLogger(JF_DocumentDeferredTrigger_mxJPO.class);

//    //office 文件类型 doc、docx、ppt、pptx、xls、xlsx
//    private static StringList officeFileList = StringList.create("doc", "docx", "ppt", "pptx", "xls", "xlsx");

    private static final String ATTR_JDX_PDFSourceFileId = "attribute[JF_PDFSourceFileId]";

    private static final String JDX_PDFSourceFileId = "JF_PDFSourceFileId";

    private static final String POLICY_Version = "Version";

    /**
     * @param context
     * @param args
     * @return int
     * @Author HLY
     * @CreateTime 2023/8/7
     * @Description: 文档创建/检入时 触发 文档转换 pdf
     */
    public int checkInConvertPdf(Context context, String[] args) {
        _logger.info(" -------------------------- checkInConvertPdf -------------------------- ");
        Boolean isPush = Boolean.FALSE;
        String jobId = "";
        try {
            //获取表达式 JDX_DocOfficeCheckinConvertPdf 的值 如果值为true 继续转pdf
            //设置文档的Title
            setDocTitle(context,args);
            boolean checkInExpres = getExpressionValue(context, "JF_DocOfficeCheckinConvertPdf");
//            boolean checkInExpres = JPO.invoke(context, "JDX_DocumentTrigger", null, "checkCheckinExpression", new String[]{}, Boolean.class);
            _logger.info("checkInExpres {}: " , checkInExpres);

            //如果为 true
            if (checkInExpres) {
                // 文档id
                String objectId = args[0];
                _logger.info("objectId {}: " , objectId);

                // 文件名称 filename
                String filename = args[1];
                _logger.info("filename {}: " , filename);

                // 文件format
                String format = args[2];
                _logger.info("format {}: " , format);

                boolean checkOffice = checkOfficeFile(filename);
                _logger.info("checkOffice {}: " , checkOffice);

                if (!checkOffice) {
                    _logger.info("return 0 ");
                    return 0;
                }

                //获取 要转换的文件
                Map fileMap = JPO.invoke(context, "JF_DocumentTrigger", null, "getCheckInConvertFile_new", new String[]{objectId,filename}, Map.class);
                _logger.info("fileMap {}: " , fileMap);

                //如果有需要转换的文件 创建 后台job （在job中判读文件个数 类型 是否需要转换 会有很多不必要的job）
                if (fileMap != null && !fileMap.isEmpty()) {
//                    String[] convertArgs = JPO.packArgs(fileMap);
                    //            [{relationship=Active Version, owner=2015079, level=1, attribute[Suspend Versioning]=False, format.file.format=generic, originated=4/11/2023 3:50:48 PM, type=CHIDI Document, format.file.name=重庆市水利工程信息模型设计交付标准.dwg, isLatestRevision=true, DocumentOwner=2015079, current.access[lock]=TRUE, revision=1, attribute[Title]=重庆市水利工程信息模型设计交付标准.dwg, current.access[unlock]=FALSE, attribute[Is Version Object]=True, locker=, id=14780.60259.52595.42703, current.access[checkout]=TRUE, masterId=14780.60259.52595.42676, description=, current.access[checkin]=TRUE, format.file.size=93346, format.file.modified=4/11/2023 3:50:48 PM, locked=FALSE, fileId=14780.60259.52595.42676}]
//                    String filename = (String) fileMap.get("format.file.name");


                    String[] convertArgs = new String[]{(String) fileMap.get("masterId"), (String) fileMap.get("id"), filename, (String) fileMap.get("format.file.size"), (String) fileMap.get("format.file.format")};

                    StringList infoList = StringList.create(DomainConstants.SELECT_TYPE, DomainConstants.SELECT_NAME, DomainConstants.SELECT_REVISION);
                    DomainObject obj = DomainObject.newInstance(context, objectId);
                    Map infoMap = obj.getInfo(context, infoList);
                    _logger.info("infoMap {}: " , infoMap);
                    String name = (String) infoMap.get(DomainConstants.SELECT_NAME);
                    String revision = (String) infoMap.get(DomainConstants.SELECT_REVISION);
                    Properties prop = NioJDUtils.readPageObject(context, "JFJDConfig");
                    String docFileIsConvertingPdf = prop.getProperty("jf.jobtitle.DocFileIsConvertingPdf");
//                    String docFileIsConvertingPdf = EnoviaResourceBundle.getProperty(context, "emxFrameworkStringResource", context.getLocale(), "emxFramework.jobtitle.DocFileIsConvertingPdf");
                    _logger.info("docFileIsConvertingPdf {}: " , docFileIsConvertingPdf);
                    docFileIsConvertingPdf = docFileIsConvertingPdf.replaceAll("@1", name).replaceAll("@2", revision).replaceAll("@3", filename);
                    _logger.info("context:{}", context.getUser());
                    String esoReview = obj.getInfo(context, "to[JFESOReview2Document]");
                    // todo 改造只有ESO签发表的excel才转换，其他的excel不转换
                    boolean isSkip = false ;
                    boolean isExcel = checkOfficeExcelFile(filename);
                    if (isExcel){
                        if (!"TRUE".equalsIgnoreCase(esoReview)) {
                            isSkip = true ;
                        }
                    }
                    if ("TRUE".equalsIgnoreCase(esoReview)) {
                        ContextUtil.pushContext(context);
                        isPush = Boolean.TRUE;
                    }
                    _logger.info("esoReview:{}",esoReview);
                    _logger.info("isSkip:{}",isSkip);
                    if (!isSkip){
                        /*Job job = new Job("JF_OfficeConverPdfJob", "checkInConvertJob", convertArgs,false);
                        job.setContextObject(objectId);
                        job.setTitle(docFileIsConvertingPdf);
//                    job.setDescription("测试检入转pdf");JDX_DocumentTrigger_mxJPO
                        job.createAndSubmit(context);
                        jobId = job.getInfo(context, DomainConstants.SELECT_ID);
                        _logger.info("jobId {}: " , jobId);*/
                        JF_Util_mxJPO.runAsync( context, convertArgs, "JF_OfficeConverPdfJob","checkInConvertJob");
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            _logger.warn(e.getMessage());
            return 1;
        } finally {
            try {
                if (isPush) {
                    ContextUtil.popContext(context);
                }
            } catch (FrameworkException e) {
                e.printStackTrace();
            }
        }
        _logger.info(" -------------------------- checkInConvertPdf -------------------------- ");

        return 0;

    }


//    /**
//     * @return boolean
//     * @Author HLY
//     * @CreateTime 2023/8/7
//     * @Description: 检查 获取区表达式 JDX_DocOfficeCheckinConvertPdf 的值，
//     * 只有表达式存在 且值为 true 则返回 true
//     * 否则返回false
//     */
//    private boolean checkCheckinExpression(Context context) throws MatrixException {
//
//        //获取表达式 JDX_DocOfficeCheckinConvertPdf 的值
//        String expresValMql = "print expression $1 select $2 dump $3";
//        _logger.info("expresValMql : " + expresValMql);
//
//        String expresValMqlResult = MqlUtil.mqlCommand(context, expresValMql, "JDX_DocOfficeCheckinConvertPdf", "value", "|");
//        _logger.info("expresValMqlResult : " + expresValMqlResult);
//
//        if (UIUtil.isNotNullAndNotEmpty(expresValMqlResult) && expresValMqlResult.equalsIgnoreCase("true")) {
//            return true;
//        } else {
//            return false;
//        }
//    }


//    /**
//     * @param context
//     * @param objectId
//     * @return Map
//     * @Author HLY
//     * @CreateTime 2023/8/7
//     * @Description: 获取需要转换的文件
//     */
//    private Map getConvertFile(Context context, String objectId) throws MatrixException {
//        Map fileMap = null;
//        //获取文档下所有的文件
//        Map paramMap = new HashMap();
//        paramMap.put("objectId", objectId);
//        _logger.info("paramMap : " + paramMap);
//
//        String[] strings = JPO.packArgs(paramMap);
//
//        MapList fileList = JPO.invoke(context, "emxCommonFileUI", null, "getFiles", strings, MapList.class);
//        _logger.info("fileList : " + fileList);
//
//        //如果只有一个文件 进行判断
//        if (fileList.size() == 1) {
//            Map map0 = (Map) fileList.get(0);
//            _logger.info("map0 : " + map0);
//
//            String filename = ((String) map0.get("format.file.name")).toLowerCase();
//            _logger.info("filename : " + filename);
//
//            int ftIndex = filename.lastIndexOf(".") + 1;
//
//            if (ftIndex < filename.length()) {
//                String filetype = filename.substring(ftIndex);
//                _logger.info("filetype : " + filetype);
////                _logger.info("officeFileList.contains(filetype) : " + officeFileList.contains(filetype));
//                if (officeFileList.contains(filetype)) {
//                    fileMap = map0;
//                }
//            }
//        }
////        _logger.info("fileMap : " + fileMap);
//        return fileMap;
//    }

    /**
     * @param filename
     * @return boolean
     * @Author HLY
     * @CreateTime 2023/8/8
     * @Description: 检查文件类型是不是 office文件
     */
    private boolean checkOfficeFile(String filename) {
        int ftIndex = filename.lastIndexOf(".") + 1;
        if (ftIndex < filename.length()) {
            String filetype = filename.substring(ftIndex);
            if (JF_PLMConstants_mxJPO.officeFileList.contains(filetype)) {
                return true;
            }
        }
        return false;
    }
    /**
    *
    *@description 判断文件是不是excel
    *@param filename
    *@return boolean
    *@throws
    *@author CHENYAN
    *@date 2026/3/2 10:40
    */
    private boolean checkOfficeExcelFile(String filename) {
        int ftIndex = filename.lastIndexOf(".") + 1;
        if (ftIndex < filename.length()) {
            String filetype = filename.substring(ftIndex);
            if ("xls".equals(filetype) || "xlsx".equals(filetype)) {
                return true;
            }
        }
        return false;
    }
//    /**
//     * @return boolean
//     * @Author HLY
//     * @CreateTime 2023/8/7
//     * @Description: 检查 获取区表达式 JDX_DocOfficeCheckinConvertPdf 的值，
//     * 只有表达式存在 且值为 true 则返回 true
//     * 否则返回false
//     */
//    public boolean checkCheckinExpression(Context context) throws MatrixException {
//
//        //获取表达式 JDX_DocOfficeCheckinConvertPdf 的值
//        String expresValMql = "print expression $1 select $2 dump $3";
////        _logger.info("expresValMql : " + expresValMql);
//
//        String expresValMqlResult = MqlUtil.mqlCommand(context, expresValMql, "JDX_DocOfficeCheckinConvertPdf", "value", "|");
////        _logger.info("expresValMqlResult : " + expresValMqlResult);
//
//        if (UIUtil.isNotNullAndNotEmpty(expresValMqlResult) && expresValMqlResult.equalsIgnoreCase("true")) {
//            return true;
//        } else {
//            return false;
//        }
//    }


//    /**
//     * @param context
//     * @param args
//     * @return int
//     * @Author HLY
//     * @CreateTime 2023/8/14
//     * @Description: 文档升版后 根据 表达式 JDX_DocOfficeReviseDelPreviousPdf 的值
//     * 进行删除 升版后自动带过来的pdf文件
//     */
//    public int delConvertPdfObj(Context context, String[] args) {
//        _logger.info(" -------------------------- delConvertPdfObj -------------------------- ");
//
//        try {
//            boolean delPreviousPdf = JDX_DocumentTrigger_mxJPO.getExpressionValue(context, "JDX_DocOfficeReviseDelPreviousPdf");
//            _logger.info("delPreviousPdf : " + delPreviousPdf);
//
//            if (delPreviousPdf) {
//
//                String newObjId = args[0];
//                String policy = args[1];
//                String newRev = args[2];
//                String oldObjId = args[3];
//                _logger.info("newObjId : " + newObjId);
//                _logger.info("policy : " + policy);
//                _logger.info("newRev : " + newRev);
//                _logger.info("oldObjId : " + oldObjId);
//
//                //判断 policy
//                if (policy.equals(DomainConstants.POLICY_DOCUMENT)) {
//
//                    Policy policyObj = new Policy(policy);
//                    String firstInSequence = policyObj.getFirstInSequence(context);
//                    _logger.info("firstInSequence : " + firstInSequence);
//
//                    //判断是不是 初版
//                    if (!newRev.equals(firstInSequence)) {
//
//                        DomainObject newObj = DomainObject.newInstance(context, newObjId);
//                        _logger.info("newObj : " + newObj);
//
//                        Map oldMap = new HashMap();
//                        oldMap.put("objectId", oldObjId);
//                        String[] oldArgs = JPO.packArgs(oldMap);
//                        MapList oldFileList = JPO.invoke(context, "JDX_DocumentTrigger", null, "getFiles", oldArgs, MapList.class);
//                        _logger.info("oldFileList : " + oldFileList);
//
//
//                        //根据属性  JDX_PDFSourceFileId 判断 有这个属性的 就是转换的文件
//                        //旧版本的数据
//                        List<Map> convertList = (List<Map>) oldFileList.stream().filter(m -> UIUtil.isNotNullAndNotEmpty((String) ((Map) m).get(ATTR_JDX_PDFSourceFileId))).collect(Collectors.toList());
//                        _logger.info("convertList : " + convertList);
//
//                        //正常情况只有一个转换的pdf文件
//                        if (convertList != null && !convertList.isEmpty()) {
//                            Map map = convertList.get(0);
//
//                            String format = (String) map.get("format.file.format");
//                            String fileName = (String) map.get("format.file.name");
//                            _logger.info("format : " + format);
//                            _logger.info("fileName : " + fileName);
//
//                            Thread.sleep(5000);
//
//                            CommonDocument newDoc = (CommonDocument) CommonDocument.newInstance(context, newObjId);
//                            _logger.info("newDoc : " + newDoc);
//
//                            StringList infoList = newDoc.getInfoList(context, "from[Active Version].to.id");
//                            _logger.info("infoList : " + infoList);
//
//                            StringList infoList1 = newDoc.getInfoList(context, "from[Latest Version].to.id");
//                            _logger.info("infoList1 : " + infoList1);
//
//                            Map newMap = new HashMap();
//                            newMap.put("objectId", newObjId);
//                            String[] newArgs = JPO.packArgs(newMap);
//                            MapList newFileList = JPO.invoke(context, "JDX_DocumentTrigger", null, "getFiles", newArgs, MapList.class);
//                            _logger.info("newFileList : " + newFileList);
//
//                            //根据属性  JDX_PDFSourceFileId 判断 有这个属性的 就是转换的文件
//                            List<Map> newconvertList = (List<Map>) newFileList.stream().filter(m -> {
//                                        Map tmap = (Map) m;
//                                        String tformat = (String) tmap.get("format.file.format");
//                                        String tfileName = (String) tmap.get("format.file.name");
//                                        if (tformat.equals(format) && tfileName.equals(fileName)) {
//                                            return true;
//                                        }
//                                        return false;
//                                    }
//                            ).collect(Collectors.toList());
//                            _logger.info("newconvertList : " + newconvertList);
//
//                            if (newconvertList != null && !newconvertList.isEmpty()) {
//
//                                List<String> newconvertIdList = (List<String>) newconvertList.stream().map(m -> ((String) ((Map) m).get(DomainConstants.SELECT_ID))).collect(Collectors.toList());
//                                _logger.info("newconvertIdList : " + newconvertIdList);
//
//                                String[] delArray = new String[newconvertIdList.size()];
//
//                                newconvertIdList.toArray(delArray);
//
//
//                                //删除文件版本
//                                newDoc.deleteVersion(context,delArray,true);
//                                _logger.info("newDoc.deleteVersion(context,delArray,true) : " + delArray.length);
//
//                            }
//
//                        }
//                    }
//                }
//            }
//
//        } catch (Exception e) {
////            e.printStackTrace();
//            _logger.warning("e : " + e.getMessage());
//            return 1;
//        }
//        _logger.info(" -------------------------- delConvertPdfObj -------------------------- ");
//
//        return 0;
//    }

    /**
     * @param context
     * @param args
     * @return int
     * @Author HLY
     * @CreateTime 2023/8/14
     * @Description: 文档升版后 根据 表达式 JDX_DocOfficeReviseDelPreviousPdf 的值 如果为true 则删除 否则就
     */
    public int pdfVerObjHandler(Context context, String[] args) {
        _logger.info(" -------------------------- pdfVerObjHandler -------------------------- ");

        try {


            String fromId = args[0];
            String toId = args[1];
            _logger.info("fromId {}: " , fromId);
            _logger.info("toId {}: " , toId);

            //从缓存中获取
            Map pdfMap = (Map) CacheUtil.getCacheObject(context, "reviseHandler" + fromId);
            _logger.info("pdfMap {}: " , pdfMap);

            if (pdfMap != null && !pdfMap.isEmpty()) {

                boolean delPreviousPdf = (boolean) pdfMap.get("JDX_DocOfficeReviseDelPreviousPdf");
                String sourceFileName = (String) pdfMap.get("sourceFileName");

//                String format = (String) pdfMap.get("format.file.format");
                String fileName = (String) pdfMap.get("format.file.name");
//                _logger.info("format : " + format);
                _logger.info("fileName {}: " , fileName);

                DomainObject versionObj = DomainObject.newInstance(context, toId);
                _logger.info("versionObj {}: " , versionObj);

                String versionTitle = versionObj.getAttributeValue(context, "Title");
                _logger.info("versionTitle {}: " , versionTitle);

                //判断是和升版前的pdf同一个文件
                if (versionTitle.equals(fileName)) {
                    CommonDocument docObj = (CommonDocument) CommonDocument.newInstance(context, fromId);
                    _logger.info("docObj {}: " , docObj);

                    //如果需要删除
                    if (delPreviousPdf) {

                        //docObj.deleteVersion(context, toId, true);
                        docObj.deleteVersion(context, new String[]{toId}, false);
                        _logger.info("docObj.deleteVersion(context,toId,false) {}: " , toId);
                        //移除缓存
                        CacheUtil.removeCacheObject(context, "reviseHandler" + fromId);
                        _logger.info("CacheUtil.removeCacheObject(context, \"reviseHandler\" + fromId) {}: " , fromId);
                    } else {
                        //修改新版本中 pdf的id
                        Map newMap = new HashMap();
                        newMap.put("objectId", fromId);
                        String[] newArgs = JPO.packArgs(newMap);
                        MapList newFileList = JPO.invoke(context, "JDX_DocumentTrigger", null, "getFiles", newArgs, MapList.class);
                        List<Map> newSourceFileList = (List<Map>) newFileList.stream().filter(m -> ((String) ((Map) m).get("format.file.name")).equals(sourceFileName)).collect(Collectors.toList());
                        _logger.info("newSourceFileList {}: " , newSourceFileList);

                        if (newSourceFileList != null && !newSourceFileList.isEmpty()) {
                            String newSourceFileId = (String) newSourceFileList.get(0).get(DomainConstants.SELECT_ID);
                            _logger.info("newSourceFileId {}: " , newSourceFileId);

                            ContextUtil.pushContext(context);
                            addInterface(context, versionObj, StringList.create("JDX_DocExt"));
                            ContextUtil.popContext(context);

                            versionObj.setAttributeValue(context, JDX_PDFSourceFileId, newSourceFileId);
                            _logger.info("versionObj.setAttributeValue(context,attr_JDX_PDFSourceFileId,newSourceFileId) {}: " , newSourceFileId);
                        }

                        //移除缓存
                        CacheUtil.removeCacheObject(context, "reviseHandler" + fromId);
                        _logger.info("CacheUtil.removeCacheObject(context, \"reviseHandler\" + fromId) {}: " , fromId);
                    }
                }

            }
        } catch (Exception e) {
//            e.printStackTrace();
            _logger.warn("e {}: " , e.getMessage());
            return 1;
        }
        _logger.info(" -------------------------- pdfVerObjHandler -------------------------- ");

        return 0;
    }

    /**
     * @return boolean
     * @Author HLY
     * @CreateTime 2023/8/7
     * @Description: 获取表达式的值
     * 只有表达式存在 且值为 true 则返回 true
     * 否则返回false
     */
    private boolean getExpressionValue(Context context, String expression) throws MatrixException {

        //获取表达式 expression 的值
        String expresValMql = "print expression $1 select $2 dump $3";
//        _logger.info("expresValMql : " + expresValMql);

        String expresValMqlResult = MqlUtil.mqlCommand(context, expresValMql, expression, "value", "|");
//        _logger.info("expresValMqlResult : " + expresValMqlResult);

        if (UIUtil.isNotNullAndNotEmpty(expresValMqlResult) && expresValMqlResult.equalsIgnoreCase("true")) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * @param context
     * @param args
     * @return int
     * @Author HLY
     * @CreateTime 2023/8/14
     * @Description: 文档升版后 根据 表达式 JDX_DocOfficeReviseDelPreviousPdf 的值
     * 进行删除 升版后自动带过来的pdf文件
     */
    public int delPdfBySourceFileDeleted(Context context, String[] args) throws FrameworkException {
        _logger.info(" -------------------------- delPdfBySourceFileDeleted -------------------------- ");

        String objectId = args[0];
        String policy = args[1];
        _logger.info("objectId {}: " , objectId);
        _logger.info("policy {}: " , policy);

        String rmInterVer = (String) CacheUtil.getCacheObject(context, "rmInterVer" + objectId);
        _logger.info("rmInterVer {}: " , rmInterVer);

        //从缓存 中 获取文档对象的id
        String masterId = (String) CacheUtil.getCacheObject(context, "rmVersionMasterId" + objectId);
        _logger.info("masterId {}: " , masterId);

        try {


            if (policy.equals(POLICY_Version)) {


                if (UIUtil.isNotNullAndNotEmpty(rmInterVer)) {

                    try {
                        CommonDocument docObj = (CommonDocument) CommonDocument.newInstance(context, masterId);
                        _logger.info("docObj = {}: " , docObj);

                        docObj.deleteVersion(context, new String[]{rmInterVer}, false);
                    } catch (Exception e) {
//                        e.printStackTrace();
                        _logger.warn(" e {}: " , e.getMessage());
                    } finally {
                        CacheUtil.removeCacheObject(context, "rmInterVer" + objectId);
                        CacheUtil.removeCacheObject(context, "rmVersionMasterId" + objectId);

                        _logger.info("CacheUtil.removeCacheObject(context, \"rmInterVer\" + objectId) {}" , objectId);
                        _logger.info("CacheUtil.removeCacheObject(context, \"rmVersionMasterId\" + objectId) {}" , objectId);
                    }
                }
            }
        } catch (Exception e) {
//            e.printStackTrace();
            CacheUtil.removeCacheObject(context, "rmInterVer" + objectId);
            CacheUtil.removeCacheObject(context, "rmVersionMasterId" + objectId);
            _logger.warn("e {}: " , e.getMessage());
            return 1;
        }
        _logger.info(" -------------------------- delPdfBySourceFileDeleted -------------------------- ");

        return 0;
    }

    /**
     * 给一个对象添加接口
     *
     * @param context
     * @param domainObject
     * @param interfaceList
     * @throws MatrixException
     */
    public void addInterface(Context context, DomainObject domainObject, StringList interfaceList) throws MatrixException {
        BusinessInterfaceList businessInterfaces = domainObject.getBusinessInterfaces(context);
        StringList isAddList = new StringList();
        for (int i = 0; i < businessInterfaces.size(); i++) {
            BusinessInterface businessInterface = businessInterfaces.get(i);
            String interfaceName_i = businessInterface.getName();
            isAddList.add(interfaceName_i);
        }
        interfaceList.removeAll(isAddList);
        if (!interfaceList.isEmpty()) {
//            ContextUtil.pushContext(context);
            try {
                for (int i = 0; i < interfaceList.size(); i++) {
                    String interfaceName_i = interfaceList.get(i);
                    if (UIUtil.isNotNullAndNotEmpty(interfaceName_i)) {
                        BusinessInterface businessInterface = new BusinessInterface(interfaceName_i, new Vault("eService Production"));
                        domainObject.addBusinessInterface(context, businessInterface);
                    }
                }
            } catch (MatrixException e) {
                e.printStackTrace();
                _logger.warn("e {}: " , e.getMessage());
            }
//            ContextUtil.popContext(context);
        }
    }

    /**
     *    项目技术文档  升版
     *     升版文档的时候只升版源文件，自动生成的PDF不带过来
     *     1. 场景：带文件升版
     *     2. 新版本文档对象，需要将源文件升版，断开自动生成的PDF文件
     * @param context
     * @param args
     * @author LIUJR
     * @throws
     * @return void
     * @date 09/06/2025 10:11
     * @description
     */
    public void documentRevisionFiles(Context context, String[] args) throws Exception{
        try {
            ContextUtil.pushContext(context);
            _logger.info("documentRevisionFiles start。。。。。。。。。。。。");
            String docId = args[0];
            _logger.info("docId:{}", docId);
            DomainObject contextFeature = new DomainObject(docId);
            String policy = contextFeature.getInfo(context, DomainConstants.SELECT_POLICY);
            if (!"Document Release".equalsIgnoreCase(policy)) {
                return;
            }
            BusinessObject nextRevision = contextFeature.getNextRevision(context);
            String objectId = nextRevision.getObjectId(context);
            DomainObject domainObject = DomainObject.newInstance(context, objectId);
            //判断是否项目技术文档
            String strProjectDoc = domainObject.getAttributeValue(context, JF_PLMConstants_mxJPO.ATTR_JF_ProjectDoc);
            _logger.info("strProjectDoc:{}", strProjectDoc);
            if (!"Y".equalsIgnoreCase(strProjectDoc)) {
                _logger.info("strProjectDoc:{}", strProjectDoc);
                return;
            }
            //获取文档下所有的文件
            Map paramMap = new HashMap();
            paramMap.put("objectId", objectId);
            String[] strings = JPO.packArgs(paramMap);
            MapList versionList = JPO.invoke(context, "JF_DocumentTrigger", null, "getFiles", strings, MapList.class);
            _logger.info("versionList:{}", versionList);
            if (versionList.isEmpty()) {
                //无带过来的文件
                return;
            }
            //找上一个版本的文件 哪一个是自动转换的
            paramMap.clear();
            paramMap.put("objectId", docId);
            strings = JPO.packArgs(paramMap);
            MapList preVersionList = JPO.invoke(context, "JF_DocumentTrigger", null, "getFiles", strings, MapList.class);
            _logger.info("preVersionList:{}", preVersionList);
            StringList preAutoConvertList = new StringList();
            for (int i = 0; i < preVersionList.size(); i++) {
                Map map = (Map) preVersionList.get(i);
                String fileName = (String) map.get(CommonDocument.SELECT_TITLE);
                String fileExtension = fileName.substring(fileName.lastIndexOf(".")).toLowerCase(); // 获取并转换为小写
                if (fileExtension.endsWith(".pdf")) {
                    String strJFPDFSourceFileId = (String) map.get(ATTR_JDX_PDFSourceFileId);
                    if (UIUtil.isNotNullAndNotEmpty(strJFPDFSourceFileId)) {
                        preAutoConvertList.add(fileName);
                    }
                }
            }
            _logger.info("preAutoConvertList:{}", preAutoConvertList);
            //如果有带过来的文件 ，需要将自动转换的pdf断开关系， 将源文件升版后关联给当前升版文件对象
            ContextUtil.startTransaction(context, true);
            Iterator iterator = versionList.iterator();
            CommonDocument commonDocument = new CommonDocument();
            commonDocument.setId(objectId);
            JF_ElectronicSignature_mxJPO jfElectronicSignatureMxJPO = new JF_ElectronicSignature_mxJPO();
            String strDirPath = context.createWorkspace()  + File.separator;   //存储地址
            while (iterator.hasNext()) {
                Map map = (Map) iterator.next();
                String fileId = (String) map.get(CommonDocument.SELECT_ID);
                String fileName = (String) map.get(CommonDocument.SELECT_TITLE);
                String fileFormat = (String) map.get(CommonDocument.SELECT_FILE_FORMAT);
                _logger.info("fileName:{}", fileName);
                _logger.info("fileFormat:{}", fileFormat);
                if (preAutoConvertList.contains(fileName)) {
                    //标识需要断开
                    _logger.info("标识需要断开fileName:{}", fileName);
                    _logger.info("标识需要断开fileId:{}", fileId);
                    commonDocument.deleteFile(context, fileName, fileFormat);
                    commonDocument.disconnect(context, new RelationshipType("Latest Version"), true, new BusinessObject(fileId));
                    commonDocument.disconnect(context, new RelationshipType("Active Version"), true, new BusinessObject(fileId));
                    continue;
                }
                //check out
                jfElectronicSignatureMxJPO.checkoutFile(context, objectId, fileFormat, fileName, strDirPath);
                _logger.info("升版fileName:{}", fileName);
                DomainObject fileObj = DomainObject.newInstance(context, fileId);
                boolean locked = fileObj.isLocked(context);
                if (!locked) {
                    fileObj.lock(context);
                }
                Map attrMap = new HashMap();
//                //2.升版文件对象升版 如果不加锁 返回值为 文档的id
                String newVerId = commonDocument.reviseVersion(context, fileName, fileName, attrMap);
                commonDocument.checkinFile(context, true, true, DomainConstants.EMPTY_STRING, fileFormat, fileName, strDirPath);
                DomainObject newVerObj = DomainObject.newInstance(context, newVerId);
                String docOnwer = commonDocument.getOwner(context).getName();
                newVerObj.setOwner(context, docOnwer);
                newVerObj.setAttributeValue(context,"Originator",docOnwer);
                //删除文件
                new File(strDirPath + File.separator + fileName).delete();
            }
            ContextUtil.commitTransaction(context);
        }catch (Exception e) {
            e.printStackTrace();
            _logger.warn(e.getMessage());
            ContextUtil.abortTransaction(context);
            throw e;
        } finally {
            ContextUtil.popContext(context);
        }
        _logger.info("documentRevisionFiles end。。。。。。。。。。。。");
    }


    /**
     * 项目文档升版后没有自动归档到项目文件夹
     * @param context
     * @param args
     * @throws Exception
     */
    public void autoConnectProjectFolder(Context context, String[] args) throws Exception{
        Boolean isPush = Boolean.FALSE;
        try {
            _logger.info("autoConnectProjectFolder start。。。。。。。。。。。。");
            String id = args[0];
            String newId = args[1];
            _logger.info("autoConnectProjectFolder id:{}---newId:{}",id,newId);
            DomainObject docObj = DomainObject.newInstance(context, id);
            //判断是否项目技术文档
            String strProjectDoc = docObj.getAttributeValue(context, JF_PLMConstants_mxJPO.ATTR_JF_ProjectDoc);
            if (!"Y".equalsIgnoreCase(strProjectDoc)) {
                _logger.info("strProjectDoc:{}", strProjectDoc);
                return;
            }
            //拿取文件所属的项目
            String strPaths = docObj.getInfo(context, "to[Vaulted Objects].from.attribute[Folder Path]");
            String strProjectSpaceId = "";
            if (UIUtil.isNotNullAndNotEmpty(strPaths)) {
                String[] split = strPaths.split("\\|");
                strProjectSpaceId = split[0];
            }
            if (UIUtil.isNullOrEmpty(strProjectSpaceId)) {
                //不需要归档
                return;
            }
            String folderId = docObj.getInfo(context, "to[Vaulted Objects].from.id");
            _logger.info("autoConnectProjectFolder folderId:{}",folderId);
            DomainObject folderObj = DomainObject.newInstance(context, folderId);
            if (UIUtil.isNullOrEmpty(newId)){
                BusinessObject nextRevision = docObj.getNextRevision(context);
                newId = nextRevision.getObjectId(context);
            }
            String owner = MqlUtil.mqlCommand(context, false, "pri bus " + strProjectSpaceId + " select owner dump", true);
            ContextUtil.pushContext(context, owner, "", "");
            isPush = Boolean.TRUE;
            DomainRelationship.connect(context, folderObj, "Vaulted Objects", true, new String[]{newId});
            _logger.info("autoConnectProjectFolder end。。。。。。。。。。。。");
        }catch (Exception e){
            _logger.warn("---autoConnectProjectFolder---error",e);
        }finally {
            if (isPush) {
                ContextUtil.popContext(context);
            }
        }

    }
    /*
     * @description:设置文档标题为第一个文件的文件名
     * @author: caipan
     * @date: 2025/10/21 21:46:46
     * @param: * @param[1] context
     * @param[2] args
     * @return:
     **/
    public void setDocTitle(Context context,String[] args) throws Exception{
        String id = args[0];
        String filename = args[1];
        DomainObject doc = DomainObject.newInstance(context,id);
        String title = doc.getAttributeValue(context,DomainConstants.ATTRIBUTE_TITLE);
        String name = doc.getName();
        if(title.equalsIgnoreCase(name)) {
            int lastDotIndex = filename.lastIndexOf('.');
            if (lastDotIndex == -1) {
            } else {
                filename = filename.substring(0, lastDotIndex);
            }
            doc.setAttributeValue(context, DomainConstants.ATTRIBUTE_TITLE, filename);
        }
    }
}
