import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.dassault_systemes.platform.ven.jackson.databind.ObjectMapper;
import com.matrixone.apps.common.CommonDocument;
import com.matrixone.apps.common.util.ComponentsUIUtil;
import com.matrixone.apps.configuration.ConfigurationConstants;
import com.matrixone.apps.domain.*;
import com.matrixone.apps.domain.util.*;
import com.matrixone.apps.framework.ui.UICache;
import com.matrixone.apps.framework.ui.UIUtil;
import javassist.compiler.ast.StringL;
import matrix.db.*;
import matrix.util.MatrixException;
import matrix.util.StringList;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.fileupload.FileItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.matrixone.apps.domain.DomainConstants.*;
import static com.matrixone.apps.domain.DomainConstants.TYPE_PERSON;

/**
 * Created with IntelliJ IDEA.
 * Copyright@ Apache Open Source Organization
 *
 * @Auther: HLY
 * @Date: 2023-08-07-10:43
 * @Description: Document  触发器 JPO
 */
public class JF_DocumentTrigger_mxJPO {
    private static final Logger _logger =  LoggerFactory.getLogger(JF_DocumentTrigger_mxJPO.class);
    private static final DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("M/d/yyyy h:mm:ss a", Locale.US);
    private static final DateTimeFormatter formatterDate = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.ENGLISH);
    private static final Logger ThreadLog = LoggerFactory.getLogger("MY_CUSTOM_LOGGER");


    //office 文件类型 doc、docx、ppt、pptx、xls、xlsx


    private static final String OBJECT_MAP_IS_LATEST_REVISION = "isLatestRevision";
    private static final String OBJECT_MAP_FILE_ID = "fileId";

    private static final String ATTR_JDX_PDFSourceFileId = "attribute[JF_PDFSourceFileId]";
    private static final String PDFSourceFileId = "JF_PDFSourceFileId";
    private static final String ATTR_JDX_PDFSource = "attribute[JF_PDFSource]";

    private static final String POLICY_Version = "Version";
    private static final String INTERFACE_CHANGE_CONTROL = "Change Control";
    private static ObjectMapper objectMapper = new ObjectMapper();

    /**
     * @param context
     * @param args
     * @return int
     * @Author HLY
     * @CreateTime 2023/8/7
     * @Description: 文档创建/检入时 触发 文档转换 pdf
     */
    public int promoteFROZENConvertPdf(Context context, String[] args) {

        String jobId = "";
        try {
            //获取表达式 JDX_DocOfficeFROZENConvertPdf 的值 如果值为true 继续转pdf
//            boolean frozenExpres = checkFROZENExpression(context, args);
            boolean frozenExpres = getExpressionValue(context, "JDX_DocOfficeFROZENConvertPdf");
            _logger.info("frozenExpres {}: " , frozenExpres);

            //如果为 true
            if (frozenExpres) {
                // 文档id
                String objectId = args[0];
                _logger.info("objectId {}: " , objectId);

                //获取 提升至冻结时 要转换的文件
                Map fileMap = getFROZENConvertFile(context, objectId);
                _logger.info("fileMap {}: " , fileMap);

                //如果有需要转换的文件 创建 后台job （在job中判读文件个数 类型 是否需要转换 会有很多不必要的job）
                if (fileMap != null && !fileMap.isEmpty()) {
//                    String[] convertArgs = JPO.packArgs(fileMap);
                    //            [{relationship=Active Version, owner=2015079, level=1, attribute[Suspend Versioning]=False, format.file.format=generic, originated=4/11/2023 3:50:48 PM, type=CHIDI Document, format.file.name=重庆市水利工程信息模型设计交付标准.dwg, isLatestRevision=true, DocumentOwner=2015079, current.access[lock]=TRUE, revision=1, attribute[Title]=重庆市水利工程信息模型设计交付标准.dwg, current.access[unlock]=FALSE, attribute[Is Version Object]=True, locker=, id=14780.60259.52595.42703, current.access[checkout]=TRUE, masterId=14780.60259.52595.42676, description=, current.access[checkin]=TRUE, format.file.size=93346, format.file.modified=4/11/2023 3:50:48 PM, locked=FALSE, fileId=14780.60259.52595.42676}]

                    String filename = (String) fileMap.get("format.file.name");

                    String[] convertArgs = new String[]{(String) fileMap.get("masterId"), (String) fileMap.get("id"), filename, (String) fileMap.get("format.file.size"), (String) fileMap.get("format.file.format")};

                    StringList infoList = StringList.create(DomainConstants.SELECT_TYPE, DomainConstants.SELECT_NAME, DomainConstants.SELECT_REVISION);
                    DomainObject obj = DomainObject.newInstance(context, objectId);
                    Map infoMap = obj.getInfo(context, infoList);
                    _logger.info("infoMap {}: " , infoMap);
                    String name = (String) infoMap.get(DomainConstants.SELECT_NAME);
                    String revision = (String) infoMap.get(DomainConstants.SELECT_REVISION);
                    String docFileIsConvertingPdf = EnoviaResourceBundle.getProperty(context, "emxFrameworkStringResource", context.getLocale(), "emxFramework.jobtitle.DocFileIsConvertingPdf");
                    _logger.info("docFileIsConvertingPdf {}: " , docFileIsConvertingPdf);
                    docFileIsConvertingPdf = docFileIsConvertingPdf.replaceAll("@1", name).replaceAll("@2", revision).replaceAll("@3", filename);

                    Job job = new Job("JDX_OfficeConverPdfJob", "frozenConvertJob", convertArgs,false);
                    job.setContextObject(objectId);
                    job.setTitle(docFileIsConvertingPdf);
//                    job.setDescription("测试冻结转pdf");
                    job.createAndSubmit(context);
                    jobId = job.getInfo(context, DomainConstants.SELECT_ID);

                    _logger.info("jobId {}: " , jobId);

                }
            }
        } catch (Exception e) {
//            e.printStackTrace();
            _logger.warn(e.getMessage());
            if (UIUtil.isNotNullAndNotEmpty(jobId)) {
                _logger.warn("jobId exception: " + e.getMessage());

            }
            return 1;
        }
        return 0;

    }


//    /**
//     * @return boolean
//     * @Author HLY
//     * @CreateTime 2023/8/7
//     * @Description: 检查 获取区表达式 JDX_DocOfficeFROZENConvertPdf 的值，
//     * 只有表达式存在 且值为 true 则返回 true
//     * 否则返回false
//     */
//    public boolean checkFROZENExpression(Context context, String[] args) throws MatrixException {
//
//        //获取表达式 JDX_DocOfficeCheckinConvertPdf 的值
//        String expresValMql = "print expression $1 select $2 dump $3";
////        _logger.info("expresValMql : " + expresValMql);
//
//        String expresValMqlResult = MqlUtil.mqlCommand(context, expresValMql, "JDX_DocOfficeFROZENConvertPdf", "value", "|");
////        _logger.info("expresValMqlResult : " + expresValMqlResult);
//
//        if (UIUtil.isNotNullAndNotEmpty(expresValMqlResult) && expresValMqlResult.equalsIgnoreCase("true")) {
//            return true;
//        } else {
//            return false;
//        }
//    }

    /**
     * @return boolean
     * @Author HLY
     * @CreateTime 2023/8/7
     * @Description: 获取表达式的值
     * 只有表达式存在 且值为 true 则返回 true
     * 否则返回false
     */
    public static boolean getExpressionValue(Context context, String expression) throws MatrixException {

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
     * @return Map
     * @Author HLY
     * @CreateTime 2023/8/7
     * @Description: 获取 Checkin 触发 需要转换的文件
     */
    public Map getCheckInConvertFile(Context context, String[] args) throws Exception {
        return getConvertFile(context, args[0]);
    }

    public Map getCheckInConvertFile_new(Context context, String[] args) throws Exception {
        String docId = args[0];
        String fileName = args[1];
        Map map = new HashMap();
        map.put("format.file.size", "500");
        map.put("format.file.format", "generic");
        DomainObject docObj = DomainObject.newInstance(context,docId);
        String where = DomainConstants.SELECT_ATTRIBUTE_TITLE+"=='"+fileName+"'";
        MapList list = docObj.getRelatedObjects(context,
                CommonDocument.RELATIONSHIP_LATEST_VERSION, CommonDocument.TYPE_DOCUMENTS,
                JF_Util_mxJPO.basicBolistSel(), //the eMatrix StringList object that holds the list of select statement pertaining to Business Obejcts.
                JF_Util_mxJPO.basicRellistSel(), //the eMatrix StringList object that holds the list of select statement pertaining to Relationships.
                false, //get To relationships
                true, //get From relationships
                (short) 1, //the number of levels to expand, 0 equals expand all.
                where, //where clause to apply to objects, can be empty ""
                null, //where clause to apply to relationship, can be empty ""
                (short) 1); //limit
        if(list.size()>0){
            Map temp =(Map) list.get(0);
            String id = UIUtil.getValue(temp, SELECT_ID);
            String masterId = docId;
            map.put("id",id);
            map.put("masterId",masterId);
        }
        return map;
    }
//    /**
//     * @param context
//     * @param objectId
//     * @return Map
//     * @Author HLY
//     * @CreateTime 2023/8/7
//     * @Description: 获取 Checkin 触发 需要转换的文件
//     */
//    private Map getCheckInConvertFile(Context context, String objectId) throws Exception {
//        Map fileMap = null;
////        //获取文档下所有的文件
////        Map paramMap = new HashMap();
////        paramMap.put("objectId", objectId);
////        String[] strings = JPO.packArgs(paramMap);
//
//        //获取所有的文件
//        MapList fileList = getFiles(context, objectId);
////        _logger.info("fileList : " + fileList.size() +"    "+fileList);
//
//        //如果只有一个文件 进行转pdf
//        if (fileList.size() == 1) {
//            Map map0 = (Map) fileList.get(0);
////            _logger.info("map0 : " + map0);
//
//            //检查是不是 offcie 文件
//            boolean bofile = checkOfficeFile(map0);
////            _logger.info("bofile : " + bofile);
//
//            if (bofile) {
//                fileMap = map0;
//            }
//
//        } else if (fileList.size() == 2) {
//            //文件更新版本 这种情况下有两个文件
//            String JDX_PDFSourceFileId = "";
//            Map officeMap = null;
//            for (int i = 0; i < fileList.size(); i++) {
//                Map map_i = (Map) fileList.get(i);
////                _logger.info("map_i : " + map_i);
//
//                String JDX_PDFSourceFileId_i = (String) map_i.get(ATTR_JDX_PDFSourceFileId);
//                if (UIUtil.isNotNullAndNotEmpty(JDX_PDFSourceFileId_i)) {
//                    JDX_PDFSourceFileId = JDX_PDFSourceFileId_i;
//                } else {
//                    boolean bofile = checkOfficeFile(map_i);
//                    _logger.info("bofile : " + bofile + "    " + map_i);
//
//                    if (bofile) {
//                        officeMap = map_i;
//                    }
//                }
//            }
////            _logger.info("JDX_PDFSourceFileId : " + JDX_PDFSourceFileId);
////            _logger.info("officeMap : " + officeMap);
//            //有转换文件 有office文件 office文件没有属性JDX_PDFSourceFileId(正常情况下不存在)
//            String t_PDFSourceFileId = (String) officeMap.get(ATTR_JDX_PDFSourceFileId);
////            _logger.info("t_PDFSourceFileId : " + t_PDFSourceFileId);
////            _logger.info("UIUtil.isNotNullAndNotEmpty(JDX_PDFSourceFileId) : " + UIUtil.isNotNullAndNotEmpty(JDX_PDFSourceFileId));
////            _logger.info("officeMap != null : " + (officeMap != null));
////            _logger.info("!officeMap.isEmpty() : " + (!officeMap.isEmpty()));
////            _logger.info("UIUtil.isNullOrEmpty(t_PDFSourceFileId) : " + (UIUtil.isNullOrEmpty(t_PDFSourceFileId)));
//
//            if (UIUtil.isNotNullAndNotEmpty(JDX_PDFSourceFileId) && officeMap != null && !officeMap.isEmpty() && UIUtil.isNullOrEmpty(t_PDFSourceFileId)) {
//                fileMap = officeMap;
//            }
//        }
////        _logger.info("fileMap : " + fileMap);
//        return fileMap;
//    }


    /**
     * @param map
     * @return boolean
     * @Author HLY
     * @CreateTime 2023/8/8
     * @Description: 检查文件类型是不是 office文件
     */
    private boolean checkOfficeFile(Map map) {
        String filename = ((String) map.get("format.file.name")).toLowerCase();
//        _logger.info("filename : " + filename);
        int ftIndex = filename.lastIndexOf(".") + 1;
        if (ftIndex < filename.length()) {
            String filetype = filename.substring(ftIndex);
//            _logger.info("filetype : " + filetype);
            if (JF_PLMConstants_mxJPO.officeFileList.contains(filetype)) {
                return true;
            }
        }
        return false;
    }

    /**
     * This method is used to get the list of files in
     * master (i.e. document holder) object
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args    holds the following input arguments:
     *                0 - objectList MapList
     * @throws Exception if the operation fails
     * @returns Object
     * @grade 0
     * @since Common 10.5
     */

    @com.matrixone.apps.framework.ui.ProgramCallable
    public MapList getFiles(Context context, String[] args) throws Exception {
        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        String masterObjectId = (String) programMap.get("objectId");
        return getFiles(context, masterObjectId);
    }

    /**
     * @param context
     * @param masterObjectId
     * @return MapList
     * @Author HLY
     * @CreateTime 2023/8/8
     * @Description: 获取文档下的文件
     */
    @com.matrixone.apps.framework.ui.ProgramCallable
    public MapList getFiles(Context context, String masterObjectId) throws Exception {
        try {
            DomainObject masterObject = DomainObject.newInstance(context, masterObjectId);
            //Added to make a single database call to
            StringList masterObjectSelectList = new StringList(12);
            masterObjectSelectList.add(CommonDocument.SELECT_ID);
            masterObjectSelectList.add(CommonDocument.SELECT_TYPE);
            masterObjectSelectList.add(CommonDocument.SELECT_NAME);
            masterObjectSelectList.add(CommonDocument.SELECT_REVISION);
            masterObjectSelectList.add(CommonDocument.SELECT_OWNER);
            masterObjectSelectList.add(CommonDocument.SELECT_FILE_NAME);
            masterObjectSelectList.add(CommonDocument.SELECT_FILE_FORMAT);
            masterObjectSelectList.add(CommonDocument.SELECT_FILE_MODIFIED);
            masterObjectSelectList.add(CommonDocument.SELECT_FILE_SIZE);
            masterObjectSelectList.add(CommonDocument.SELECT_HAS_CHECKOUT_ACCESS);
            masterObjectSelectList.add(CommonDocument.SELECT_HAS_CHECKIN_ACCESS);
            masterObjectSelectList.add(CommonDocument.SELECT_HAS_LOCK_ACCESS);
            masterObjectSelectList.add(CommonDocument.SELECT_HAS_UNLOCK_ACCESS);
            masterObjectSelectList.add(CommonDocument.SELECT_SUSPEND_VERSIONING);
            masterObjectSelectList.add(CommonDocument.SELECT_MOVE_FILES_TO_VERSION);
            masterObjectSelectList.add(CommonDocument.SELECT_LATEST_REVISION);
            masterObjectSelectList.add(CommonDocument.SELECT_CURRENT);


            // get the Master Object data
            Map masterObjectMap = masterObject.getInfo(context, masterObjectSelectList);
            // Version Object seletcs
            StringList versionSelectList = new StringList(9);
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
            versionSelectList.add(DomainConstants.SELECT_ORIGINATED);
            versionSelectList.add(DomainConstants.SELECT_TYPE);
            versionSelectList.add(CommonDocument.SELECT_SUSPEND_VERSIONING);
            versionSelectList.add(CommonDocument.SELECT_IS_VERSION_OBJECT);
            versionSelectList.add(ATTR_JDX_PDFSource);
            versionSelectList.add(ATTR_JDX_PDFSourceFileId);

            // get the file (Version Object) data
//            MapList versionList = masterObject.getRelatedObjects(context, CommonDocument.RELATIONSHIP_ACTIVE_VERSION, CommonDocument.TYPE_DOCUMENTS, versionSelectList, null, false, true, (short) 1, null, null, null, null, null);
            MapList versionList = masterObject.getRelatedObjects(context, CommonDocument.RELATIONSHIP_LATEST_VERSION, CommonDocument.TYPE_DOCUMENTS, versionSelectList, null, false, true, (short) 1, null, null, null, null, null);

            // get all the files in the Master Object
            StringList fileList = (StringList) masterObjectMap.get(CommonDocument.SELECT_FILE_NAME);
            StringList fileFormatList = (StringList) masterObjectMap.get(CommonDocument.SELECT_FILE_FORMAT);
            StringList fileSizeList = (StringList) masterObjectMap.get(CommonDocument.SELECT_FILE_SIZE);
            StringList fileModifiedList = (StringList) masterObjectMap.get(CommonDocument.SELECT_FILE_MODIFIED);

            StringList tempfileFormatList = new StringList();
            StringList tempfileList = new StringList();
            for (int ii = 0; ii < fileFormatList.size(); ii++) {
                String format = (String) fileFormatList.get(ii);
                if (!DomainObject.FORMAT_MX_MEDIUM_IMAGE.equalsIgnoreCase(format)) {
                    tempfileFormatList.add(format);
                    tempfileList.add(fileList.get(ii));
                }
            }
            fileFormatList = tempfileFormatList;
            fileList = tempfileList;

            // get the Master Object meta data
            String masterId = (String) masterObjectMap.get(CommonDocument.SELECT_ID);
            String canCheckout = (String) masterObjectMap.get(CommonDocument.SELECT_HAS_CHECKOUT_ACCESS);
            String canCheckin = (String) masterObjectMap.get(CommonDocument.SELECT_HAS_CHECKIN_ACCESS);
            String canLock = (String) masterObjectMap.get(CommonDocument.SELECT_HAS_LOCK_ACCESS);
            String canUnLock = (String) masterObjectMap.get(CommonDocument.SELECT_HAS_UNLOCK_ACCESS);
            boolean isLatestRevision = ((String) masterObjectMap.get(CommonDocument.SELECT_REVISION)).equalsIgnoreCase((String) masterObjectMap.get(CommonDocument.SELECT_LATEST_REVISION));
            String suspendVersioning = (String) masterObjectMap.get(CommonDocument.SELECT_SUSPEND_VERSIONING);
            boolean moveFilesToVersion = (Boolean.valueOf((String) masterObjectMap.get(CommonDocument.SELECT_MOVE_FILES_TO_VERSION))).booleanValue();

            // to store the object ID of the object where file resides
            // this can be either master object Id or version object Id depending on the "Moves Files To Version" attribute value
            String fileId = (String) masterObjectMap.get(CommonDocument.SELECT_ID);

            // loop thru each file to build MapList, each Map corresponds to one file
            MapList fileMapList = new MapList();
            String fileFormat = null;
            String fileSize = null;
            String fileModified = null;
            Iterator versionItr = versionList.iterator();
            while (versionItr.hasNext()) {
                Map fileVersionMap = (Map) versionItr.next();
                String versionFileName = (String) fileVersionMap.get(CommonDocument.SELECT_TITLE);
                //Added information for MSF
                String locker = (String) fileVersionMap.get(CommonDocument.SELECT_LOCKER);
                String versionFileRevision = (String) fileVersionMap.get(CommonDocument.SELECT_REVISION);
                fileFormat = CommonDocument.FORMAT_GENERIC;
                fileSize = "";
                fileModified = "";
                DomainObject fileObject = DomainObject.newInstance(context, fileId);
                boolean fileObjectIsKindOfCAD = false;
                String SYMBOLIC_type_MCADDrawing = "type_MCADDrawing";
                String mCADDrawing = PropertyUtil.getSchemaProperty(context, SYMBOLIC_type_MCADDrawing);
                String mCADModel = PropertyUtil.getSchemaProperty(context, DomainSymbolicConstants.SYMBOLIC_type_MCADModel);
                if (fileObject.isKindOf(context, mCADModel) || fileObject.isKindOf(context, mCADDrawing)) {
                    fileObjectIsKindOfCAD = true;
                }
                if (fileObjectIsKindOfCAD) {
                    moveFilesToVersion = false;
                }
                if (moveFilesToVersion) {
                    fileId = (String) fileVersionMap.get(CommonDocument.SELECT_ID);
                    try {
                        String versionFiles = (String) fileVersionMap.get(CommonDocument.SELECT_FILE_NAME);
                        fileFormat = (String) fileVersionMap.get(CommonDocument.SELECT_FILE_FORMAT);
                        fileSize = (String) fileVersionMap.get(CommonDocument.SELECT_FILE_SIZE);
                        fileModified = (String) fileVersionMap.get(CommonDocument.SELECT_FILE_MODIFIED);
                    } catch (ClassCastException cex) {
                        StringList versionFilesList = (StringList) fileVersionMap.get(CommonDocument.SELECT_FILE_NAME);
                        StringList versionFileSize = (StringList) fileVersionMap.get(CommonDocument.SELECT_FILE_SIZE);
                        StringList versionFileFormat = (StringList) fileVersionMap.get(CommonDocument.SELECT_FILE_FORMAT);
                        StringList versionFileModified = (StringList) fileVersionMap.get(CommonDocument.SELECT_FILE_MODIFIED);

                        // get the file corresponding to this Version by filtering the above fileList
                        int index = versionFilesList.indexOf(versionFileName.trim());

                        // get the File Format
                        if (index != -1 && versionFileFormat != null && versionFileFormat.size() >= index) {
                            fileFormat = (String) versionFileFormat.get(index);
                        }

                        // get the File Size
                        if (index != -1 && versionFileSize != null && versionFileSize.size() >= index) {
                            fileSize = (String) versionFileSize.get(index);
                        }

                        // get the File Modified date
                        if (index != -1 && versionFileModified != null && versionFileModified.size() >= index) {
                            fileModified = (String) versionFileModified.get(index);
                        }
                    }
                } else {
                    // get the file corresponding to this Version by filtering the above fileList
                    int index = fileList.indexOf(versionFileName);

                    // get the File Format
                    if (index != -1 && fileFormatList != null && fileFormatList.size() >= index) {
                        fileFormat = (String) fileFormatList.get(index);
                    }

                    // get the File Size
                    if (index != -1 && fileSizeList != null && fileSizeList.size() >= index) {
                        fileSize = (String) fileSizeList.get(index);
                    }

                    // get the File Modified date
                    if (index != -1 && fileModifiedList != null && fileModifiedList.size() >= index) {
                        fileModified = (String) fileModifiedList.get(index);
                    }
                }
                String documentOwner = (String) masterObjectMap.get(CommonDocument.SELECT_OWNER);

                fileVersionMap.put("masterId", masterId);
                fileVersionMap.put(OBJECT_MAP_FILE_ID, fileId);
                fileVersionMap.put(CommonDocument.SELECT_FILE_FORMAT, fileFormat);
                fileVersionMap.put(CommonDocument.SELECT_FILE_MODIFIED, fileModified);
                fileVersionMap.put(CommonDocument.SELECT_FILE_NAME, versionFileName);
                fileVersionMap.put(CommonDocument.SELECT_REVISION, versionFileRevision);
                fileVersionMap.put(CommonDocument.SELECT_FILE_SIZE, fileSize);
                fileVersionMap.put(CommonDocument.SELECT_HAS_CHECKOUT_ACCESS, canCheckout);
                fileVersionMap.put(CommonDocument.SELECT_HAS_CHECKIN_ACCESS, canCheckin);
                fileVersionMap.put(CommonDocument.SELECT_HAS_LOCK_ACCESS, canLock);
                fileVersionMap.put(CommonDocument.SELECT_HAS_UNLOCK_ACCESS, canUnLock);
                fileVersionMap.put(CommonDocument.SELECT_SUSPEND_VERSIONING, suspendVersioning);
                fileVersionMap.put(OBJECT_MAP_IS_LATEST_REVISION, isLatestRevision);
                fileVersionMap.put("DocumentOwner", documentOwner);
                //Added information for MSF
                fileVersionMap.put(CommonDocument.SELECT_LOCKER, locker);
                fileMapList.add(fileVersionMap);
            }
            return fileMapList;
        } catch (Exception ex) {
            ex.printStackTrace();
            throw ex;
        }
    }

    /**
     * @param context
     * @param objectId
     * @return Map
     * @Author HLY
     * @CreateTime 2023/8/7
     * @Description: 获取 冻结 触发 需要转换的文件
     */
    private Map getFROZENConvertFile(Context context, String objectId) throws Exception {
        return getConvertFile(context, objectId);
    }

//    /**
//     * @param context
//     * @param objectId
//     * @return Map
//     * @Author HLY
//     * @CreateTime 2023/8/7
//     * @Description: 获取 Checkin 触发 需要转换的文件
//     */
//    private Map getFROZENConvertFile(Context context, String objectId) throws Exception {
//        Map fileMap = null;
//
//        //获取所有的文件
//        MapList fileList = getFiles(context, objectId);
//
//        //如果只有一个文件 进行转pdf
//        if (fileList.size() == 1) {
//            Map map0 = (Map) fileList.get(0);
//
//            //检查是不是 offcie 文件
//            boolean bofile = checkOfficeFile(map0);
//
//            if (bofile) {
//                fileMap = map0;
//            }
//
//        } else if (fileList.size() == 2) {
//            //文件更新版本（存在已转化的pdf） 这种情况下有两个文件
//            String JDX_PDFSourceFileId = "";
//            Map pdfMap = null;
//            Map officeMap = null;
//            for (int i = 0; i < fileList.size(); i++) {
//                Map map_i = (Map) fileList.get(i);
//                String JDX_PDFSourceFileId_i = (String) map_i.get(ATTR_JDX_PDFSourceFileId);
//                if (UIUtil.isNotNullAndNotEmpty(JDX_PDFSourceFileId_i)) {
//                    JDX_PDFSourceFileId = JDX_PDFSourceFileId_i;
//                    pdfMap = map_i;
//                } else {
//                    boolean bofile = checkOfficeFile(map_i);
//                    if (bofile) {
//                        officeMap = map_i;
//                    }
//                }
//            }
//            _logger.info("JDX_PDFSourceFileId : " + JDX_PDFSourceFileId);
//            _logger.info("pdfMap : " + pdfMap);
//            _logger.info("officeMap : " + officeMap);
//
//            //如果存在转换过的pdf
//            if(UIUtil.isNotNullAndNotEmpty(JDX_PDFSourceFileId) && officeMap!=null && pdfMap!=null ){
//
////            _logger.info("officeMap : " + officeMap);
//                String officeFileId = (String) officeMap.get(DomainConstants.SELECT_ID);
//                _logger.info("JDX_PDFSourceFileId : " + JDX_PDFSourceFileId);
//                _logger.info("officeFileId : " + officeFileId);
//
//                //如果要转换的文件id 和 转换后 pdf文件对象中记录的属性不同 则进行转换
//                if (!JDX_PDFSourceFileId.equals(officeFileId)) {
//                    fileMap = officeMap;
//                }
//            }
//
//        }
////        _logger.info("fileMap : " + fileMap);
//        return fileMap;
//    }

    /**
     * @param context
     * @param args
     * @return Map
     * @Author HLY
     * @CreateTime 2023/8/7
     * @Description: 获取 Checkin 触发 需要转换的文件
     */
    public Map getConvertFile(Context context, String[] args) throws Exception {
        return getConvertFile(context, args[0]);
    }

    /**
     * @param context
     * @param objectId
     * @return Map
     * @Author HLY
     * @CreateTime 2023/8/7
     * @Description: 获取 Checkin 触发 需要转换的文件
     */
    private Map getConvertFile(Context context, String objectId) throws Exception {
        Map fileMap = null;
        //获取所有的文件
        MapList fileList = getFiles(context, objectId);
        _logger.info("fileList:{}",fileList);
        //如果只有一个文件 进行转pdf
        if (fileList.size() == 1) {
            Map map0 = (Map) fileList.get(0);
            //检查是不是 offcie 文件
            boolean bofile = checkOfficeFile(map0);
            if (bofile) {
                fileMap = map0;
            }
        } else if (fileList.size() == 2) {
            //文件更新版本（存在已转化的pdf） 这种情况下有两个文件
            String JDX_PDFSourceFileId = "";
            Map pdfMap = null;
            Map officeMap = null;
            for (int i = 0; i < fileList.size(); i++) {
                Map map_i = (Map) fileList.get(i);
                String JDX_PDFSourceFileId_i = (String) map_i.get(ATTR_JDX_PDFSourceFileId);
                if (UIUtil.isNotNullAndNotEmpty(JDX_PDFSourceFileId_i)) {
                    JDX_PDFSourceFileId = JDX_PDFSourceFileId_i;
                    pdfMap = map_i;
                } else {
                    boolean bofile = checkOfficeFile(map_i);
                    if (bofile) {
                        officeMap = map_i;
                    }
                }
            }
//            _logger.info("JDX_PDFSourceFileId : " + JDX_PDFSourceFileId);
//            _logger.info("pdfMap : " + pdfMap);
//            _logger.info("officeMap : " + officeMap);
            //如果存在转换过的pdf
            if (UIUtil.isNotNullAndNotEmpty(JDX_PDFSourceFileId) && officeMap != null && pdfMap != null) {
//            _logger.info("officeMap : " + officeMap);
                String officeFileId = (String) officeMap.get(DomainConstants.SELECT_ID);
//                _logger.info("JDX_PDFSourceFileId : " + JDX_PDFSourceFileId);
//                _logger.info("officeFileId : " + officeFileId);
                //如果要转换的文件id 和 转换后 pdf文件对象中记录的属性不同 则进行转换
                if (!JDX_PDFSourceFileId.equals(officeFileId)) {
                    fileMap = officeMap;
                }
            }
        }
//        _logger.info("fileMap : " + fileMap);
        return fileMap;
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
    public int reviseHandlerPdfObj(Context context, String[] args) {
        _logger.info(" -------------------------- reviseHandlerPdfObj -------------------------- ");

        try {

//            if (delPreviousPdf) {

                String newObjId = args[0];
                String policy = args[1];
                String newRev = args[2];
                String oldObjId = args[3];
                _logger.info("newObjId {}: " , newObjId);
                _logger.info("policy {}: " , policy);
                _logger.info("newRev {}: " , newRev);
                _logger.info("oldObjId {}: " , oldObjId);

                //判断 policy
                if (policy.equals(DomainConstants.POLICY_DOCUMENT)) {

                    Policy policyObj = new Policy(policy);
                    String firstInSequence = policyObj.getFirstInSequence(context);
                    _logger.info("firstInSequence {}: " , firstInSequence);

                    //判断是不是 初版
                    if (!newRev.equals(firstInSequence)) {

                        DomainObject newObj = DomainObject.newInstance(context, newObjId);
                        _logger.info("newObj {}: " , newObj);

//                        Map oldMap = new HashMap();
//                        oldMap.put("objectId", oldObjId);
//                        String[] oldArgs = JPO.packArgs(oldMap);
//                        MapList oldFileList = JPO.invoke(context, "JDX_DocumentTrigger", null, "getFiles", oldArgs, MapList.class);
//                        _logger.info("oldFileList : " + oldFileList);
                        MapList oldFileList = getFiles(context, oldObjId);
                        _logger.info("oldFileList {}: " , oldFileList);

                        //根据属性  JDX_PDFSourceFileId 判断 有这个属性的 就是转换的文件
                        //旧版本的数据
                        List<Map> convertList = (List<Map>) oldFileList.stream().filter(m -> UIUtil.isNotNullAndNotEmpty((String) ((Map) m).get(ATTR_JDX_PDFSourceFileId))).collect(Collectors.toList());
                        _logger.info("convertList {}: " , convertList);

                        //正常情况只有一个转换的pdf文件
                        if (convertList != null && !convertList.isEmpty()) {
                            Map map = convertList.get(0);
                            _logger.info("map {}: " , map);

                            //是否删除 上一个版本的pdf
                            boolean delPreviousPdf = JF_DocumentTrigger_mxJPO.getExpressionValue(context, "JDX_DocOfficeReviseDelPreviousPdf");
                            _logger.info("delPreviousPdf {}: " , delPreviousPdf);
                            String sourceFileId = (String) map.get(ATTR_JDX_PDFSourceFileId);
                            DomainObject sourceFileObj = DomainObject.newInstance(context, sourceFileId);
                            String sourceFileName = sourceFileObj.getAttributeValue(context, "Title");

                            //添加源文件的名称
                            map.put("JDX_DocOfficeReviseDelPreviousPdf", delPreviousPdf);
                            map.put("sourceFileName",sourceFileName);
                            //添加到缓存
                            CacheUtil.setCacheObject(context, "reviseHandler" + newObjId, map);
                        }
                    }
                }
//            }

        } catch (Exception e) {
//            e.printStackTrace();
            _logger.warn("e : " + e.getMessage());
            return 1;
        }
        _logger.info(" -------------------------- reviseHandlerPdfObj -------------------------- ");

        return 0;
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
    public int delPdfObjCheck(Context context, String[] args) {
        _logger.info(" -------------------------- delPdfObjCheck -------------------------- ");

        try {


            String objectId = args[0];
            String policy = args[1];
            _logger.info("objectId {}: " , objectId);
            _logger.info("policy {}: " , policy);

            if (policy.equals(POLICY_Version)) {

                DomainObject versionObj = DomainObject.newInstance(context, objectId);
                _logger.info("versionObj {}: " , versionObj);

                StringList infoList = StringList.create(DomainConstants.SELECT_TYPE, DomainConstants.SELECT_NAME, DomainConstants.SELECT_REVISION);
                Map infoMap = versionObj.getInfo(context, infoList);
                _logger.info("infoMap {}: " , infoMap);

                String JDX_PDFSourceFileId = versionObj.getAttributeValue(context, PDFSourceFileId);
                _logger.info("JDX_PDFSourceFileId {}: " , JDX_PDFSourceFileId);

                if (UIUtil.isNotNullAndNotEmpty(JDX_PDFSourceFileId)) {

                    //转换失败 抛异常
                    String pdfVersionNoAllowDel = EnoviaResourceBundle.getProperty(context, "emxFrameworkStringResource", context.getLocale(), "emxFramework.ConvertPdfmsg.PdfVersionNoAllowDel");
                    _logger.info("pdfVersionNoAllowDel {}: " , pdfVersionNoAllowDel);

                    //自动转换的pdf 不允许删除
                    MqlUtil.mqlCommand(context, "warning $1", pdfVersionNoAllowDel);
                    return 1;
                } else {
                    String masterId = versionObj.getInfo(context, "to[Active Version].from.id");
                    _logger.info("masterId {}: " , masterId);
                    //添加到缓存
                    CacheUtil.setCacheObject(context, "rmVersionMasterId" + objectId, masterId);
                }
            }
        } catch (Exception e) {
//            e.printStackTrace();
            _logger.warn("e : " + e.getMessage());
            return 1;
        }
        _logger.info(" -------------------------- delPdfObjCheck -------------------------- ");

        return 0;
    }


//    /**
//     * @param context
//     * @param args
//     * @return int
//     * @Author HLY
//     * @CreateTime 2023/8/14
//     * @Description: 文档升版后 根据 表达式 JDX_DocOfficeReviseDelPreviousPdf 的值
//     * 进行删除 升版后自动带过来的pdf文件
//     */
//    public int delConvertPdfFile(Context context, String[] args) {
//        _logger.info(" -------------------------- delConvertPdfFile -------------------------- ");
//
//        try {
//
//
//            String objectId = args[0];
//            String policy = args[1];
//            _logger.info("objectId : " + objectId);
//            _logger.info("policy : " + policy);
//
//            if (policy.equals(POLICY_Version)) {
//
//                //从缓存 中 获取文档对象的id
//                String masterId = (String) CacheUtil.getCacheObject(context, "masterId" + objectId);
//                _logger.info("masterId : " + masterId);
//
//                if (UIUtil.isNotNullAndNotEmpty(masterId)) {
//                    CommonDocument docObj = (CommonDocument) CommonDocument.newInstance(context, masterId);
//                    _logger.info("docObj : " + docObj);
//
//                    Map argsMap = new HashMap();
//                    argsMap.put("objectId", masterId);
//                    String[] paramArgs = JPO.packArgs(argsMap);
//                    MapList fileList = JPO.invoke(context, "JDX_DocumentTrigger", null, "getFiles", paramArgs, MapList.class);
//                    _logger.info("fileList.size() : " + fileList.size());
//
//                    //根据属性  JDX_PDFSourceFileId 判断 有这个属性的 就是转换的文件
//                    List<Map> pdfList = (List<Map>) fileList.stream().filter(m -> ((String) ((Map) m).get(ATTR_JDX_PDFSourceFileId)).equals(objectId)).collect(Collectors.toList());
//                    _logger.info("pdfList : " + pdfList);
//
//
//                    if (pdfList != null && !pdfList.isEmpty()) {
//
//                        List<String> pdfIdList = (List<String>) pdfList.stream().map(m -> ((String) ((Map) m).get(DomainConstants.SELECT_ID))).collect(Collectors.toList());
//                        _logger.info("pdfIdList : " + pdfIdList);
//
//
//                        StringList interList = StringList.create("JDX_DocExt");
//                        _logger.info("interList : " + interList);
//
//
//                        //先移除接口 才可以删除
//                        boolean flag = false;
//
//                        for (int i = 0; i < pdfIdList.size(); i++) {
//                            String id_i = pdfIdList.get(i);
//                            _logger.info(" id_i : " + id_i);
//
////                            try {
////                                DomainObject obj_i = DomainObject.newInstance(context,id_i);
////                                _logger.info("obj_i : " + obj_i);
////                                obj_i.setId(id_i);
////                                if(obj_i.exists(context)){
//                            _logger.info("!objectId.equals(id_i) : " + (!objectId.equals(id_i)));
//                            if(!objectId.equals(id_i)){
//                                removeInterface(context, DomainObject.newInstance(context, id_i), interList);
//                                docObj.deleteVersion(context, new String[]{id_i}, false);
//                                flag = true;
//                                _logger.info("removeInterface(context, DomainObject.newInstance(context, id_i), interList) : " + id_i);
//                                _logger.info("docObj.deleteVersion(context, new String[]{id_i}, false) : " + id_i);
//                            }
//
//
////                                }
//
////                            } catch (MatrixException e) {
//////                                e.printStackTrace();
////                                _logger.info("e : " + e.getMessage());
////                            }
//                        }
//
//                        _logger.info("flag : " + flag);
//                        if (flag) {
//                            CacheUtil.removeCacheObject(context, "masterId" + objectId);
//                            _logger.info("CacheUtil.removeCacheObject(context, \"masterId\" + objectId) : " + objectId);
//                        }
//
//
//                    }
//                }
//            }
//        } catch (Exception e) {
////            e.printStackTrace();
//            _logger.warning("e : " + e.getMessage());
//            return 1;
//        }
//        _logger.info(" -------------------------- delConvertPdfFile -------------------------- ");
//
//        return 0;
//    }

    /**
     * 移除对象添加接口
     *
     * @param context
     * @param domainObject
     * @param interfaceList
     * @throws MatrixException
     */
    public void removeInterface(Context context, DomainObject domainObject, StringList interfaceList) throws MatrixException {
        ContextUtil.pushContext(context);
        BusinessInterfaceList businessInterfaces = domainObject.getBusinessInterfaces(context);
        for (int i = 0; i < businessInterfaces.size(); i++) {
            BusinessInterface businessInterface_i = businessInterfaces.get(i);
            String interfaceName_i = businessInterface_i.getName();
            if (interfaceList.contains(interfaceName_i)) {
                domainObject.removeBusinessInterface(context, businessInterface_i);
            }
        }
        ContextUtil.popContext(context);

    }

    /**
     * @param context
     * @param args
     * @return boolean
     * @Author HLY
     * @CreateTime 2023/8/14
     * @Description: 控制手动转换按钮权限
     */
    public boolean accessManualConvertPdf(Context context, String[] args) {
//        _logger.info(" -------------------------- accessManualConvertPdf -------------------------- ");

        try {
            Map argsMap = JPO.unpackArgs(args);
//            _logger.info("argsMap : " + argsMap);

            String objectId = (String) argsMap.get("objectId");
//            _logger.info("objectId : " + objectId);

            DomainObject docObj = DomainObject.newInstance(context, objectId);
//            _logger.info("docObj : " + docObj);

            String owner = docObj.getOwner(context).getName();
//            _logger.info("owner : " + owner);

            String contextUser = context.getUser();
//            _logger.info("contextUser : " + contextUser);

            String current = docObj.getCurrentState(context).getName();
//            _logger.info("current : " + current);

            boolean manualConvertPdf = getExpressionValue(context, "JDX_DocOfficeManualConvertPdf");
//            _logger.info("manualConvertPdf : " + manualConvertPdf);

            StringList stateList = StringList.create("PRIVATE", "IN_WORK");
//            _logger.info("stateList : " + stateList);

            if (contextUser.equals(owner) && stateList.contains(current) && manualConvertPdf) {
//                _logger.info(" return true ");
                return true;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
//        _logger.info(" -------------------------- accessManualConvertPdf -------------------------- ");

        return false;
    }


    /**
     * @param context
     * @param args
     * @return int
     * @Author HLY
     * @CreateTime 2023/8/14
     * @Description: 手动 文档转换 pdf
     */
    public int manualConvertPdf(Context context, String[] args) {

        _logger.info(" ----------------------------- manualConvertPdf ----------------------------- ");
        String jobId = "";
        try {
            _logger.info("args {}: " , Arrays.asList(args));
            // 文档id
            String objectId = args[0];
            _logger.info("objectId {}: " , objectId);


            Job job = new Job("JDX_OfficeConverPdfJob", "manualConvertPdf", args);
            job.setContextObject(objectId);
            job.setTitle(objectId + "测试冻结转pdf");
            job.setDescription("测试冻结转pdf");
            job.createAndSubmit(context);
            jobId = job.getInfo(context, DomainConstants.SELECT_ID);

            _logger.info("jobId {}: " , jobId);

            _logger.info(" ----------------------------- manualConvertPdf ----------------------------- ");
        } catch (
                Exception e) {
//            e.printStackTrace();
            _logger.warn(e.getMessage());
            if (UIUtil.isNotNullAndNotEmpty(jobId)) {
                _logger.warn("jobId exception: " + e.getMessage());
            }
            return 1;
        }
        return 0;
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
    public int rmPdfInterfaceBySourceFileDeleted(Context context, String[] args) throws FrameworkException {
        _logger.info(" -------------------------- rmPdfInterfaceBySourceFileDeleted -------------------------- ");
        String objectId = args[0];
        String policy = args[1];
        _logger.info("objectId {}: " , objectId);
        _logger.info("policy {}: " , policy);
        try {

            if (policy.equals(POLICY_Version)) {

                //从缓存 中 获取文档对象的id
                String masterId = (String) CacheUtil.getCacheObject(context, "rmVersionMasterId" + objectId);
                _logger.info("masterId {}: " , masterId);

                if (UIUtil.isNotNullAndNotEmpty(masterId)) {
                    CommonDocument docObj = (CommonDocument) CommonDocument.newInstance(context, masterId);
                    _logger.info("docObj {}: " , docObj);

                    Map argsMap = new HashMap();
                    argsMap.put("objectId", masterId);
                    String[] paramArgs = JPO.packArgs(argsMap);
                    MapList fileList = JPO.invoke(context, "JDX_DocumentTrigger", null, "getFiles", paramArgs, MapList.class);
                    _logger.info("fileList.size() {}: " , fileList.size());

                    //根据属性  JDX_PDFSourceFileId 判断 有这个属性的 就是转换的文件
                    List<Map> pdfList = (List<Map>) fileList.stream().filter(m -> ((String) ((Map) m).get(ATTR_JDX_PDFSourceFileId)).equals(objectId)).collect(Collectors.toList());
                    _logger.info("pdfList {}: " , pdfList);


                    if (pdfList != null && !pdfList.isEmpty()) {

                        List<String> pdfIdList = (List<String>) pdfList.stream().map(m -> ((String) ((Map) m).get(DomainConstants.SELECT_ID))).collect(Collectors.toList());
                        _logger.info("pdfIdList {}: " , pdfIdList);

                        StringList interList = StringList.create("JDX_DocExt");
                        _logger.info("interList {}: " , interList);
                        
                        //移除pdf对象接口 正常只有一条数据
                        String pdfId_0 = pdfIdList.get(0);

                        removeInterface(context, DomainObject.newInstance(context, pdfId_0), interList);
                        _logger.info("removeInterface(context, DomainObject.newInstance(pdfId_0, id_i), interList) {}: " , pdfId_0);
                        CacheUtil.setCacheObject(context, "rmInterVer" + objectId, pdfId_0);
                        _logger.info("CacheUtil.setCacheObject(context, \"removeInterfaceVersion\" + objectId, pdfId_0) {}: " , pdfId_0);

//                        //移除接口
//                        for (int i = 0; i < pdfIdList.size(); i++) {
//                            String id_i = pdfIdList.get(i);
//                            _logger.info(" id_i : " + id_i);
//                            _logger.info("!objectId.equals(id_i) : " + (!objectId.equals(id_i)));
//                            if (!objectId.equals(id_i)) {
//                                removeInterface(context, DomainObject.newInstance(context, id_i), interList);
//                                _logger.info("removeInterface(context, DomainObject.newInstance(context, id_i), interList) : " + id_i);
//                                CacheUtil.setCacheObject(context,"removeInterfaceVersion"+objectId,id_i);
//                            }
//                        }
                    }
                }
            }
        } catch (Exception e) {
//            e.printStackTrace();
            _logger.warn("e : " + e.getMessage());
            //移除缓存
            CacheUtil.removeCacheObject(context, "rmVersionMasterId" + objectId);
            return 1;
        }
        _logger.info(" -------------------------- rmPdfInterfaceBySourceFileDeleted -------------------------- ");

        return 0;
    }

    public String checkinFile(Context context, String[] args) throws Exception {
        JSONObject sResult = new JSONObject();
        try{
            Map paramMap = (Map) JPO.unpackArgs(args);
            String sLanguage = (String) paramMap.get("language");
            String timeZone = (String) paramMap.get("timezone");
            String sFolder  = (String) paramMap.get("folder");
            List files = (List) paramMap.get("files");
            StringList ids=new StringList();
            String strType     = PropertyUtil.getSchemaProperty(context,DomainObject.SYMBOLIC_type_DOCUMENTS);
            Iterator iter = files.iterator();
            int index;
            String sFilename="";
            FileItem file = null;
            File outfile = null;
            String docId = "";
            String version = "";
            String documentName = "";
            ContextUtil.startTransaction(context, true);
            StringList lockedFiles = new StringList();

            JSONArray dataArray = new JSONArray();
            while (iter.hasNext())
            {
                file = (FileItem) iter.next();
                sFilename 	= file.getName();
                if(sFilename.contains("/")) {
                    index = sFilename.lastIndexOf("/");
                    sFilename = sFilename.substring(index);
                }
                if(sFilename.contains("\\")) {
                    index = sFilename.lastIndexOf("\\");
                    sFilename = sFilename.substring(index+1);
                }
                outfile = new File(sFolder +  sFilename);
                file.write(outfile);
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
                    //cDoc.checkinFile(context, true, true, "", "generic", sFilename, sFolder);
                    System.out.println("L48Trace DnD 5 cDoc : " + cDoc);
                    version = cDoc.createVersion(context, sFilename, sFilename, null);
                    cDoc.checkinFile(context, true, true, "", "generic", sFilename,sFolder);
                    docId = cDoc.getObjectId();
                    documentName = cDoc.getName();

                    JSONObject data = new JSONObject();
                    data.put("enoviaDocId", docId);
                    data.put("enoviaAttachId", version);
                    data.put("fileName", sFilename);
                    data.put("enoviaDocName", documentName);
                    dataArray.add(data);

                }
                outfile.delete();
            }
            ContextUtil.commitTransaction(context);
            //JSONObject data = new JSONObject();
            //data.put("enoviaDocId", docId);
            //data.put("enoviaAttachId", version);
            //data.put("fileName", sFilename);
            //data.put("enoviaDocName", documentName);
            sResult.put("data", dataArray);
            setSuccessmsg(sResult);
            return parseToJson(sResult);
        }catch (Exception ex) {
            ContextUtil.abortTransaction(context);
            ex.printStackTrace();
            _logger.error("err:{}", ex.getMessage());
            setErrormsg(sResult, ex.getMessage());
            return parseToJson(sResult);
        } finally {
            PropertyUtil.setRPEValue(context, "MX_ALLOW_POV_STAMPING", "false", false);
        }
    }

    public static String parseToJson(Object o) {
        if (o == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(o);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public static JSONObject setErrormsg(JSONObject sResult, String msg) {
        sResult.put("code", "500");
        sResult.put("msg", "Please contact the administrator Error message:"
                + msg);
        sResult.put("status", "false");
        StackTraceElement[] stackTrace = new Exception().getStackTrace();
        String methodName = "";
        if(stackTrace.length>1) {
            methodName = stackTrace[1].getMethodName();
        }
        _logger.info(" end gate Service process method "+methodName+" end "+sResult.toString());
        return sResult;
    }
    public static JSONObject setSuccessmsg(JSONObject sResult) {
        sResult.put("code", "0000");
        sResult.put("msg", "Success");
        sResult.put("status", "true");
        StackTraceElement[] stackTrace = new Exception().getStackTrace();
        String methodName = "";
        if(stackTrace.length>1) {
            methodName = stackTrace[1].getMethodName();
        }
        _logger.info("invoke Portal time end gate Service process method "+methodName+" end ");
        return sResult;
    }

    /**
    *     在工作中-冻结的Promote增加Trigger，自动添加流程
     *     1）当“是否会签”为“否”时，审核节点为：1.审核：Line Manager；2.批准：SDT-项目经理；
     *     2）当“是否会签”为“是”时，审核节点为： 1.审核：Line Manager； 2.会签：所选人员(并联);2.批准：SDT-项目经理；
     *     3）当“是否会签”为“ ”时，不自动生成流程
     *
     *     20251011 - 新流程修改，需要重新修改，此版本作为历史备份存在
    * @param context
	* @param args
    * @author LIUJR
    * @throws
    * @return void
    * @date 29/05/2025 15:16
    * @description
    */
    public void projectDocumentGenerationRouteBack(Context context, String[] args) throws Exception{
        _logger.info("projectDocumentGenerationRouteBack start。。。。。。。。。。。。");
        try {
            String strDocId = args[0];
            DomainObject domainObject = DomainObject.newInstance(context);
            domainObject.setId(strDocId);
            //判断文档是否是项目文档
            StringList attributeList = new StringList();
            attributeList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JF_ProjectDoc);
            attributeList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JF_CounterSign);
            attributeList.add("attribute[JF_DocSpecialistReview]");
            attributeList.add(JF_PLMConstants_mxJPO.select_attr_JF_DocReceiptConfirmation);
            Map attributeMap = domainObject.getInfo(context, attributeList);
            String strJF_ProjectDoc = UIUtil.getValue(attributeMap, JF_PLMConstants_mxJPO.SELECT_ATTR_JF_ProjectDoc);
            String strJF_CounterSign = UIUtil.getValue(attributeMap, JF_PLMConstants_mxJPO.SELECT_ATTR_JF_CounterSign);
            String strJF_DocSpecialistReview = UIUtil.getValue(attributeMap, "attribute[JF_DocSpecialistReview]");
            String JF_DocReceiptConfirmation = UIUtil.getValue(attributeMap, JF_PLMConstants_mxJPO.select_attr_JF_DocReceiptConfirmation);
            //不是项目文档，是否会签为”“，不生成流程
            if ("Y".equalsIgnoreCase(strJF_ProjectDoc)) {
                if ("".equalsIgnoreCase(strJF_CounterSign) || "N".equalsIgnoreCase(strJF_CounterSign)) {
                    //当“是否审批”为“否”时，不需要走审批流程，owner一键发布
                    ContextUtil.pushContext(context);
                    //下面语句第一个为false则会加历史记录  为true不会加历史记录
//                    MqlUtil.mqlCommand(context,false,"mod bus "+strDocId+" current FROZEN",true);
                    if ("FROZEN".equals(domainObject.getInfo(context,"current"))){
                        _logger.info("projectDocumentGenerationRouteBack current。。");
                        domainObject.promote(context);
                    }
                    ContextUtil.popContext(context);
                    return;
                }
            } else {
                return;
            }
            //流程标题
            MapList approveList = new MapList();
            Integer approveIndex = 1;
            String tileMess = ComponentsUIUtil.getI18NString(context, context.getLocale().toString(), "emxComponents.DOCManger.ROUTE.TitleMess", new String[]{});

            //审批人一样 去重
            StringList personList = new StringList();
            //会签：所选人员(并联)
            if ("Y".equalsIgnoreCase(strJF_CounterSign)) {
                //Line Manager
                String strLineMess = EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", context.getLocale(), "emxComponents.DOCManger.ROUTE.LineManager");
                String lineManagerId = JF_PublicMethodClass_mxJPO.getPersonLineManager(context, null, domainObject.getOwner(context).getName());
                if (UIUtil.isNotNullAndNotEmpty(lineManagerId)) {
                    Map managerMap = JF_PublicMethodClass_mxJPO.getMapAnyOrAll(lineManagerId, strLineMess, "true", approveIndex.toString(), "All");
                    approveList.add(managerMap);
                    personList.add(lineManagerId);
                    approveIndex++;
                }
                //会签
                String strApproveMess = EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", context.getLocale(), "emxComponents.DOCManger.ROUTE.SignPerson");
                StringList approvePersonIdList = domainObject.getInfoList(context, "from[JFDoc2Countersign].to.id");
                if (CollectionUtils.isNotEmpty(approvePersonIdList)){
                    // 标记是否添加了新审批人
                    boolean addedNewPerson = false;
                    for (int i = 0; i < approvePersonIdList.size(); i++) {
                        String strPersonId = approvePersonIdList.get(i);
                        if (personList.contains(strPersonId)){
                            continue;
                        }
                        Map managerMap = JF_PublicMethodClass_mxJPO.getMapAnyOrAll(strPersonId, strApproveMess, "true", approveIndex.toString(), "All");
                        approveList.add(managerMap);
                        personList.add(strPersonId);
                        // 标记为添加了新审批人
                        addedNewPerson = true;
                    }
                    if (addedNewPerson){
                        approveIndex++;
                    }
                }

                //拿取文件所属的项目
                String strPaths = domainObject.getInfo(context, "to[Vaulted Objects].from.attribute[Folder Path]");
                if (UIUtil.isNullOrEmpty(strPaths)) {
                    //提示
                    return;
                }
                String[] split = strPaths.split("\\|");
                String strProjectSpaceId = split[0];
                //SDT-整椅经理
                String strPManagerId = JF_PublicMethodClass_mxJPO.getProjectWherePerson(context, strProjectSpaceId, SELECT_ID, "", "attribute[Project Role]=='Chair manager'");
                if (UIUtil.isNullOrEmpty(strPManagerId)) {
                    String strProjectOwner = JF_Util_mxJPO.getProjectManager(context, new String[]{strProjectSpaceId});
                    DomainObject PMObj = PersonUtil.getPersonObject(context, strProjectOwner);
                    strPManagerId = PMObj.getInfo(context, SELECT_ID);
                }
                String strProjectManagerMess = EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", context.getLocale(), "emxComponents.DRChairManger.ROUTE.MESS");
                if (UIUtil.isNotNullAndNotEmpty(strPManagerId)) {
                    if (!personList.contains(strPManagerId)){
                        Map managerMap = JF_PublicMethodClass_mxJPO.getMapAnyOrAll(strPManagerId, strProjectManagerMess, "true", approveIndex.toString(), "All");
                        approveList.add(managerMap);
                        personList.add(strPManagerId);
                        approveIndex++;
                    }
                }

                //审核专家审批
                if ("Y".equals(strJF_DocSpecialistReview)){
                    String strSpecialistMess = EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", context.getLocale(), "emxComponents.DOCManger.ROUTE.SpecialistPerson");
                    StringList specialistPersonIdList = domainObject.getInfoList(context, "from[JFDoc2SpecialistReview].to.id");
                    for (String strPersonId : specialistPersonIdList) {
                        if (personList.contains(strPersonId)){
                            continue;
                        }
                        Map managerMap = JF_PublicMethodClass_mxJPO.getMapAnyOrAll(strPersonId, strSpecialistMess, "true", approveIndex.toString(), "All");
                        approveList.add(managerMap);
                    }
                    approveIndex++;
                }
                //增加回执确认节点
                if("Y".equalsIgnoreCase(JF_DocReceiptConfirmation)){
                    {
                        String strSpecialistMess = EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", context.getLocale(), "emxComponents.DOCManger.ROUTE.DocReceiptConfirmation");
                        String strPersonId = PersonUtil.getPersonObjectID(context,domainObject.getInfo(context,DomainConstants.SELECT_OWNER));
                        if(UIUtil.isNotNullAndNotEmpty(strPersonId)) {
                            Map managerMap = JF_PublicMethodClass_mxJPO.getMapAnyOrAll(strPersonId, strSpecialistMess, "true", approveIndex.toString(), "All");
                            approveList.add(managerMap);
                        }
                    }
                }
            }
            //创建流程
            JF_Route_mxJPO jf_route = new JF_Route_mxJPO(context, args);
            String state = "state_FROZEN";//在哪个状态增加流程
            String policy = "policy_Document";   //哪个Policy上面
            String routeDescription = tileMess;//流程描述
            String routeId = jf_route.createAndStartRoute(context, approveList, strDocId, state, policy, routeDescription);
        }catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
        _logger.info("projectDocumentGenerationRouteBack end。。。。。。。。。。。。");
    }


    /**
    * 项目文档状态自动至“已发布”状态，同时对文档进行自动签名：
     * 把文档更新成打上水印版本--打上Release 和发布时间
     * update by ljr 20260515 检查DOC DOCX PPT PPTX是否都已经转换完成，如果没有转换完成提示：文件发布成功，需要3-5分钟完成PDF转换并打水印，请稍等！！！！不做校验拦截
    * @param context
	* @param args
    * @author LIUJR
    * @throws
    * @return void
    * @date 04/06/2025 09:55
    * @description
    */
    public void projectDocumentReleaseWaterMark(Context context, String[] args) throws Exception{
        _logger.info("projectDocumentReleaseWaterMark start。。。。。。。。。。。。");
        Boolean isPush = Boolean.FALSE;
        try {
            String docId = args[0];
            DomainObject domainObject = DomainObject.newInstance(context, docId);
            String user = context.getUser().toString();
            _logger.info("context：{}", context.getUser().toString());
            //判断是否项目技术文档
            String strProjectDoc = domainObject.getAttributeValue(context, JF_PLMConstants_mxJPO.ATTR_JF_ProjectDoc);
            if ("N".equalsIgnoreCase(strProjectDoc)) {
                return;
            }
            //获取文档下PDF文件
            MapList versionList = JF_PublicMethodClass_mxJPO.getDocumentFiles(context, docId);
            StringList waterMarkList = new StringList();

            String strDirPath = context.createWorkspace()  + File.separator;   //存储地址
            MapList checkInMapList = new MapList();
            StringList saveFileNameList = new StringList();
            CommonDocument commonDocument = new CommonDocument();
            commonDocument.setId(docId);
            StringList hasDocPPTList = new StringList();
            StringList pdfSourceFileIdList = new StringList();   //JF_PDFSourceFileId: 35845.4994.60916.49390
            Boolean pdfFlag = Boolean.FALSE;
            for (int i = 0; i < versionList.size(); i++) {
                Map map = (Map) versionList.get(i);
                String fileName = (String) map.get(CommonDocument.SELECT_TITLE);
                String fileId = (String) map.get(CommonDocument.SELECT_ID);
                String strPDFSourceFileId = (String) map.get("attribute[JF_PDFSourceFileId].value");
                if (fileName.endsWith(".doc") || fileName.endsWith(".docx") || fileName.endsWith(".ppt") || fileName.endsWith(".pptx")) {
                    hasDocPPTList.add(fileId);
                }
                if (fileName.endsWith(".pdf")) {
                    if (UIUtil.isNotNullAndNotEmpty(strPDFSourceFileId)) {
                        pdfSourceFileIdList.add(strPDFSourceFileId);
                    }
                    pdfFlag = Boolean.TRUE;
                }
            }
            /*   update by ljr 20260513
            * 转换pdf结果
            * 1.pdf文件数 >= docppt文件数，可能pdf文件上传的文件数量本来就多，需要再判断pdf文件的源文件的数是否与docppt文件数量一致
            *   如果pdf源文件数量<docppt数量，没转换完全需要标识
            *   数量一致，不需要标识
            * 2.pdf文件数<docppt文件数，说明本身就没转完整，需要将没转pdf的文件打上标识没有打印水印
            * */
            Boolean flag = Boolean.TRUE;
            for (int i = 0; i < hasDocPPTList.size(); i++) {
                //不包含代表没有转换了pdf
                if (!pdfSourceFileIdList.contains(hasDocPPTList.get(i))) {
                    flag = Boolean.FALSE;
                    break;
                }
            }
            if (!flag) {
                //如果需要全打印水印 先设置标识  弹出提示信息
                domainObject.setAttributeValue(context, "JF_WaterMarkFlag", "N");
                emxContextUtil_mxJPO.mqlNotice(context, ComponentsUIUtil.getI18NString(context, context.getLocale().toString(), "emxComponents.Mess.checkAllDocTranPDF", new String[]{}));
                return;
            }
            //如果没有需要打印水印
            if (!pdfFlag) {
                return;
            }
            for (int i = 0; i < versionList.size(); i++) {
                Map map = (Map) versionList.get(i);
                String fileName = (String) map.get(CommonDocument.SELECT_TITLE);
                String physicalid = (String) map.get("physicalid");
                if (fileName.endsWith(".pdf")) {
                    FileList fileList = new FileList();
                    matrix.db.File file = new matrix.db.File(fileName, "generic");
                    fileList.add(file);
                    if (saveFileNameList.contains(fileName)) {
                        //新换目录
                        waterMarkList.add(strDirPath + physicalid + File.separator + fileName);
                        map.put("dirPath", strDirPath + physicalid + File.separator);
                        commonDocument.checkoutFiles(context, false, "generic", fileList, strDirPath + physicalid + File.separator);
                    } else {
                        waterMarkList.add(strDirPath + fileName);
                        map.put("dirPath", strDirPath);
                        saveFileNameList.add(fileName);
                        commonDocument.checkoutFiles(context, false, "generic", fileList, strDirPath);
                    }
                    checkInMapList.add(map);
                }
            }
            //是否有需要加水印的PDF文件
            //加水印
            String date = domainObject.getInfo(context, "state[RELEASED].actual");
            // 解析输入日期
            LocalDateTime dateTime = LocalDateTime.parse(date, inputFormatter);
            // 格式化输出日期
            // 拆分并格式化日期部分和时间部分
            date = dateTime.format(formatterDate);
            _logger.info("waterMarkList：{}", waterMarkList);
            _logger.info("checkInMapList：{}", checkInMapList);
            JF_WaterMarkUtils_mxJPO.addWaterMarkUniformDistribution(context, waterMarkList, "Released " + date);
            //check in回去
            _logger.info("checkInMapList：{}", checkInMapList);
            if (!"User Agent".equalsIgnoreCase(user)) {
                ContextUtil.pushContext(context);
                isPush = Boolean.TRUE;
            }
            for (int i = 0; i < checkInMapList.size(); i++) {
                Map map = (Map) checkInMapList.get(i);
                String fileFormat = (String) map.get(CommonDocument.SELECT_FILE_FORMAT);
                String fileName = (String) map.get(CommonDocument.SELECT_TITLE);
                String dirPath = (String) map.get("dirPath");
                commonDocument.checkinFile(context, true, true, DomainConstants.EMPTY_STRING, fileFormat, fileName, dirPath);
                //需要将文件进行删除
                new File(dirPath + fileName).delete();
            }
        } catch (Exception exception){
            exception.printStackTrace();
            throw exception;
        } finally {
            if (isPush) {
                ContextUtil.popContext(context);
            }
        }
        _logger.info("projectDocumentReleaseWaterMark end。。。。。。。。。。。。");
    }

    /**
     * 如果文档关联的类型中“是否会签”选择“是”，则系统自动在文档属性页面添加“会签人员”，用户进行选择对应人员，用户冻结文档时对人员进行校验，
     *      如果为空，则提示信息“该类型文档发布需要指定会签人员，请指定会签人员后重新冻结数据”；
     * 按照同样逻辑设定“审核专家”选项。
     *
     * 20251011 - 校验变化，此版本保留，复制新的方法修改
     * @param context
     * @param args
     * @return
     * @throws Exception
     */
    public int checkDocumentRelateSignAndSpecialistPersonBack(Context context,String[] args)throws Exception {
        int ischeck = 0;
        String documentId = args[0];
        StringBuilder stringBuffer = new StringBuilder();
        DomainObject document = DomainObject.newInstance(context,documentId);
        StringList bosel = new StringList();
        bosel.add("attribute[JF_CounterSign]");
        bosel.add("attribute[JF_DocSpecialistReview]");
        bosel.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JF_ProjectDoc);
        Map documentInfo = document.getInfo(context, bosel);
        String JF_CounterSign = UIUtil.getValue(documentInfo, "attribute[JF_CounterSign]");
        String JF_DocSpecialistReview = UIUtil.getValue(documentInfo, "attribute[JF_DocSpecialistReview]");
        String JF_ProjectDoc = UIUtil.getValue(documentInfo, JF_PLMConstants_mxJPO.SELECT_ATTR_JF_ProjectDoc);
        //不是项目文档 跳过
        if (!"Y".equals(JF_ProjectDoc)){
            return ischeck;
        }
//        if ("Y".equals(JF_CounterSign)) {
//            StringList objIds = document.getInfoList(context, "from[JFDoc2Countersign].to.id");
//            if (CollectionUtils.isEmpty(objIds)){
//                stringBuffer.append("\u8be5\u7c7b\u578b\u6587\u6863\u53d1\u5e03\u9700\u8981\u6307\u5b9a\u4f1a\u7b7e\u4eba\u5458\uff0c\u8bf7\u6307\u5b9a\u4f1a\u7b7e\u4eba\u5458\u540e\u91cd\u65b0\u51bb\u7ed3\u6570\u636e");
//            }
//        }
        if ("Y".equals(JF_DocSpecialistReview)){
            StringList objIds = document.getInfoList(context, "from[JFDoc2SpecialistReview].to.id");
            if (CollectionUtils.isEmpty(objIds)){
                if (stringBuffer.length()>0){
                    stringBuffer.append("\n");
                }
                stringBuffer.append("\u8be5\u7c7b\u578b\u6587\u6863\u53d1\u5e03\u9700\u8981\u6307\u5b9a\u5ba1\u6838\u4e13\u5bb6\uff0c\u8bf7\u6307\u5b9a\u5ba1\u6838\u4e13\u5bb6\u540e\u91cd\u65b0\u51bb\u7ed3\u6570\u636e");
            }
        }
        if (stringBuffer.length()>0){
            ischeck = 1;
            emxContextUtil_mxJPO.mqlNotice(context, stringBuffer.toString());
        }
        return ischeck;
    }

    /*
     * @description:完成文档审核任务的时候，如果标题是回执确认的任务，需要判断该文档是是否上传了回执确认 select_attr_JF_DocReceiptConfirmation
     * 专家回执确认是否有值 SELECT_ATTR_JF_DocSpecialistReceipt
     * @author: caipan
     * @date: 2025/9/9 14:11:58
     * @param: * @param[1] context
     * @param[2] id
     * @return:
     **/
    public static  boolean docHasReceiptConfirmation(Context context,String id,String attribute) throws Exception{
        StringList selList = JF_Util_mxJPO.basicBolistSel();
        selList.add(attribute);
        String where =attribute+"==Y";
        DomainObject doc = DomainObject.newInstance(context,id);
          MapList list =   doc.getRelatedObjects(context,
            "Latest Version", //pattern to match relationships
            DomainConstants.TYPE_DOCUMENT, //pattern to match types
            selList, //the eMatrix StringList object that holds the list of select statement pertaining to Business Obejcts.
            JF_Util_mxJPO.basicRellistSel(), //the eMatrix StringList object that holds the list of select statement pertaining to Relationships.
            false, //get To relationships
            true, //get From relationships
            (short) 1, //the number of levels to expand, 0 equals expand all.
                    where, //where clause to apply to objects, can be empty ""
            null, //where clause to apply to relationship, can be empty ""
            (short) 0); //limit
        if(list.size()>0){
            return true;
        }
        return false;
    }

    /**
     * 项目技术文档 入库连接Classified Item关系的action的trigger  同步库属性到文档上
     * @param context
     * @param args
     * @author LIUJR
     * @throws
     * @return void
     * @date 2025/10/10 14:27
     * @description
     */
    public void synchronizeDocumentLibProperties(Context context, String[] args) throws Exception {
        _logger.info("synchronizeDocumentLibProperties!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        try {
            String libId = args[0];
            String docId = args[1];
            String relId = args[2];

            _logger.info("libId:{}", libId);
            _logger.info("docId:{}", docId);
            DomainObject docObject = DomainObject.newInstance(context);
            docObject.setId(docId);
            String toType = docObject.getTypeName(context);
            String toPolicy = docObject.getPolicy(context).getName();
            _logger.info("toType:{}", toType);
            _logger.info("toPolicy:{}", toPolicy);
            if (!(DomainConstants.TYPE_DOCUMENT.equalsIgnoreCase(toType) && "Document Release".equalsIgnoreCase(toPolicy))) {
                return;
            }
            _logger.info("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");

            //拿取是否是项目文档属性
            String isProjectDoc = docObject.getAttributeValue(context, JF_PLMConstants_mxJPO.ATTR_JF_ProjectDoc);
            _logger.info("isProjectDoc:{}", isProjectDoc);
            if ("N".equalsIgnoreCase(isProjectDoc)) {
                return;
            }
            _logger.info("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            //所有校验做完了 开始同步属性
            DomainObject libObject = DomainObject.newInstance(context);
            libObject.setId(libId);
            /*同步属性：
            *     JF_LeadReview                   JF_DocLeadReview
                  JF_ChairManagerReview           JF_DocChairManagerReview
                  JF_DepartmentManager            JF_DocDepartmentManager
                  JF_ProjectReview                JF_DocProjectReview
                  JF_SpecialistReview             JF_DocSpecialistReview
                  JF_SpecialistReceipt            JF_DocSpecialistReceipt
                  JF_ReceiptConfirmation          JF_DocReceiptConfirmation
                  JF_FileFormatRequirements       JF_DocFileFormatRequirements
                  * * *
                  JF_SpecialistReceiptGroup       JF_DocSpecialistReceiptGroup
                  JF_SpecialistReviewGroup        JF_DocSpecialistReviewGroup
            *
            * */
            StringList attributeList = new StringList();
            attributeList.add(JF_PLMConstants_mxJPO.ATTR_JF_LeadReview);
            attributeList.add(JF_PLMConstants_mxJPO.ATTR_JF_ChairManagerReview);
            attributeList.add(JF_PLMConstants_mxJPO.ATTR_JF_DepartmentManager);
            attributeList.add(JF_PLMConstants_mxJPO.ATTR_JF_ProjectReview);
            attributeList.add(JF_PLMConstants_mxJPO.ATTR_JF_SpecialistReview);
            attributeList.add(JF_PLMConstants_mxJPO.ATTR_JF_SpecialistReceipt);
            attributeList.add(JF_PLMConstants_mxJPO.ATTR_JF_ReceiptConfirmation);
            attributeList.add(JF_PLMConstants_mxJPO.ATTR_JF_CounterSign);
            attributeList.add(JF_PLMConstants_mxJPO.ATTR_JF_FileFormatRequirements);
            AttributeList attributeValues = libObject.getAttributeValues(context, attributeList);
            _logger.info("lib ATTR:{}", attributeValues);
            HashMap<String, String> attributeMap = new HashMap<>();
            String docSpecialistReview = EMPTY_STRING;
            String docSpecialistReceipt = EMPTY_STRING;
            for (int i = 0; i < attributeValues.size(); i++) {
                Attribute attribute = attributeValues.get(i);
                String name = attribute.getName();
                String value = attribute.getValue();
                if (JF_PLMConstants_mxJPO.ATTR_JF_CounterSign.equalsIgnoreCase(name)) {
                    attributeMap.put(JF_PLMConstants_mxJPO.ATTR_JF_CounterSign, value);
                } else {
                    String[] split = name.split("_");
                    attributeMap.put(split[0] + "_Doc" + split[1], value);
                }
                if (JF_PLMConstants_mxJPO.ATTR_JF_SpecialistReview.equalsIgnoreCase(name)) {
                    docSpecialistReview = value;
                }
                if (JF_PLMConstants_mxJPO.ATTR_JF_SpecialistReceipt.equalsIgnoreCase(name)) {
                    docSpecialistReceipt = value;
                }
            }
            _logger.info("attributeMap:{}", attributeMap);
            _logger.info("docSpecialistReview:{}", docSpecialistReview);
            _logger.info("docSpecialistReceipt:{}", docSpecialistReceipt);
            docObject.setAttributeValues(context, attributeMap);
            String projectId = docObject.getAttributeValue(context, "JF_ConnProjectId");
            if (UIUtil.isNullOrEmpty(projectId)) {
                return;
            }
            _logger.info("projectId:{}", projectId);
            //获取项目中的专家组成员
            DomainObject projectObject = DomainObject.newInstance(context);
            projectObject.setId(projectId);
            //拿到相同专业和分类的人员
            //拿取专家组
            attributeList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JF_DocSpecialty);
            attributeList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JF_ProjectDocType);
            String strJF_DocSpecialty = docObject.getAttributeValue(context, JF_PLMConstants_mxJPO.ATTR_JF_DocSpecialty);
            String strJF_ProjectDocType = docObject.getAttributeValue(context, JF_PLMConstants_mxJPO.ATTR_JF_ProjectDocType);
            //拿取属性
//            //拿取专家组
//            MapList mapList = projectObject.getRelatedObjects(
//                    context,
//                    "JFProject2ExpertsGroup",
//                    TYPE_PERSON,
//                    new StringList(SELECT_ID),
//                    StringList.create("attribute[JF_ExpertsDocSpecialty]", "attribute[JF_ExpertsDocType]"),
//                    false,
//                    true,
//                    (short) 1,
//                    "",
//                    "",
//                    0
//            );
//            // 如果id是String类型
//            StringList specialistPersonIdList = (StringList) mapList.stream().filter(m -> {
//                        Map map = (Map) m;
//                        String strJF_ExpertsDocSpecialty = UIUtil.getValue(map, "attribute[JF_ExpertsDocSpecialty]");
//                        String strJF_ExpertsDocType = UIUtil.getValue(map, "attribute[JF_ExpertsDocType]");
//                        if (strJF_ExpertsDocSpecialty.contains(strJF_DocSpecialty) && strJF_ExpertsDocType.contains(strJF_ProjectDocType)) {
//                            return true;
//                        } else {
//                            return false;
//                        }
//                    }).map(map -> UIUtil.getValue((Map) map, SELECT_ID))
//                    .collect(Collectors.toCollection(StringList::new));
//            _logger.info("personIdList:{}", specialistPersonIdList);
//            //项目专家成员同步关系  JFDocument2ExpertPerson
//            if ("Y".equalsIgnoreCase(docSpecialistReview)) {
//                DomainRelationship.connect(context, docObject, "JFDocument2ExpertPerson", Boolean.TRUE, specialistPersonIdList.toStringArray());
//            } else if ("Y".equalsIgnoreCase(docSpecialistReceipt)) {
//                DomainRelationship.connect(context, docObject, "JFDocument2ExpertPerson", Boolean.TRUE, specialistPersonIdList.toStringArray());
//            }
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * 校验部门经理审核是否关联人员，
     * 是否专家回执/是否专家审核,需要校验是否关联了人员
     *
     * @param context
     * @param args
     * @return
     * @throws Exception
     */
    public int checkDocumentRelateSignAndSpecialistPerson(Context context,String[] args)throws Exception {
        int ischeck = 0;
        try {
            String documentId = args[0];
            DomainObject document = DomainObject.newInstance(context,documentId);
            String hasBDR = document.getInfo(context, "to[Reference Document|from.type==JFBatchDocumentReview]");
            StringList bosel = new StringList();
            bosel.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JF_DocDepartmentManager);
            bosel.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JF_DocSpecialistReceipt);
            bosel.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JF_DocSpecialistReview);
            bosel.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JF_ProjectDoc);
            bosel.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JF_DocSpecialty);
            bosel.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JF_ProjectDocType);
            bosel.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JF_DocLeadReview);
            bosel.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JF_DocChairManagerReview);
            bosel.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JF_DocProjectReview);
            bosel.add(SELECT_OWNER);
            Map documentInfo = document.getInfo(context, bosel);
            String JF_DocDepartmentManager = UIUtil.getValue(documentInfo, JF_PLMConstants_mxJPO.SELECT_ATTR_JF_DocDepartmentManager);
            String JF_DocSpecialistReceipt = UIUtil.getValue(documentInfo, JF_PLMConstants_mxJPO.SELECT_ATTR_JF_DocSpecialistReceipt);
            String JF_DocSpecialistReview = UIUtil.getValue(documentInfo, JF_PLMConstants_mxJPO.SELECT_ATTR_JF_DocSpecialistReview);
            String JF_ProjectDoc = UIUtil.getValue(documentInfo, JF_PLMConstants_mxJPO.SELECT_ATTR_JF_ProjectDoc);
            String strJF_DocSpecialty = UIUtil.getValue(documentInfo, JF_PLMConstants_mxJPO.SELECT_ATTR_JF_DocSpecialty);
            String strJF_ProjectDocType = UIUtil.getValue(documentInfo, JF_PLMConstants_mxJPO.SELECT_ATTR_JF_ProjectDocType);
            String strJF_DocLeadReview = UIUtil.getValue(documentInfo, JF_PLMConstants_mxJPO.SELECT_ATTR_JF_DocLeadReview);
            String strJF_DocChairManagerReview = UIUtil.getValue(documentInfo, JF_PLMConstants_mxJPO.SELECT_ATTR_JF_DocChairManagerReview);
            String strJF_DocProjectReview = UIUtil.getValue(documentInfo, JF_PLMConstants_mxJPO.SELECT_ATTR_JF_DocProjectReview);
            String strOwner = UIUtil.getValue(documentInfo, SELECT_OWNER);
            //不是项目文档 跳过
            if (!"Y".equals(JF_ProjectDoc)){
                return ischeck;
            }
            Boolean flag =Boolean.FALSE;
            StringBuilder stringBuilder = new StringBuilder();
            String personId = EMPTY_STRING;
            DomainObject personObject = DomainObject.newInstance(context);
            //20260806 update by ljr 提前获取项目，供原审批人员校验和分类通知角色校验共同使用。
            String strPaths = document.getInfo(context, "to[Vaulted Objects].from.attribute[Folder Path]");
            String strProjectSpaceId = EMPTY_STRING;
            if (UIUtil.isNotNullAndNotEmpty(strPaths)) {
                strProjectSpaceId = strPaths.split("\\|")[0];
            } else {
                strProjectSpaceId = document.getInfo(context, "to[Task Deliverable].from.to[Project Access Key].from.from[Project Access List].to.id");
            }
            if ("XSO".equalsIgnoreCase(strJF_ProjectDocType)) {
                //校验文档owner的部门总监是否设置了人员
                personObject = PersonUtil.getPersonObject(context, strOwner);
                MapList mapList = personObject.getRelatedObjects(context,
                        RELATIONSHIP_MEMBER,
                        TYPE_DEPARTMENT,
                        StringList.create(SELECT_DESCRIPTION, SELECT_ID, SELECT_ATTRIBUTE_TITLE),
                        JF_Util_mxJPO.basicRellistSel(),
                        true,
                        false,
                        (short) 1,
                        "",
                        "",
                        0);
                _logger.info("mapList:{}", mapList);
                if (mapList.isEmpty()) {
                    stringBuilder.append(ComponentsUIUtil.getI18NString(context, context.getLocale().toString(), "emxComponents.Document.XSOError", new String[]{}));
                    flag = Boolean.TRUE;
                } else {
                    StringList nullDescList = new StringList();
                    mapList.stream().forEach(m -> {
                        Map map = (Map) m;
                        String desc = UIUtil.getValue(map, SELECT_DESCRIPTION);
                        String title = UIUtil.getValue(map, SELECT_ATTRIBUTE_TITLE);
                        if (UIUtil.isNullOrEmpty(desc)) {
                            nullDescList.add(title);
                        }
                    });
                    _logger.info("nullDescList:{}", nullDescList);
                    if (!nullDescList.isEmpty()) {
                        stringBuilder.append(ComponentsUIUtil.getI18NString(context, context.getLocale().toString(), "emxComponents.Document.XSOError", new String[]{}));
                        stringBuilder.append(nullDescList.join(","));
                        flag = Boolean.TRUE;
                    }
                }
            } else {
                //直线经理
                if ("Y".equalsIgnoreCase(strJF_DocLeadReview)) {
                    personId = JF_PublicMethodClass_mxJPO.getPersonLineManager(context, null, document.getOwner(context).getName());
                    if (UIUtil.isNullOrEmpty(personId)) {
                        stringBuilder.append(ComponentsUIUtil.getI18NString(context, context.getLocale().toString(), "emxComponents.Document.LineManagerError", new String[]{}));
                        flag = Boolean.TRUE;
                    } else {
                        //判断是否活动
                        personObject.setId(personId);
                        if ("Inactive".equalsIgnoreCase(personObject.getInfo(context, SELECT_CURRENT))) {
                            stringBuilder.append(ComponentsUIUtil.getI18NString(context, context.getLocale().toString(), "emxComponents.Document.LineManagerInactive", new String[]{}));
                            flag = Boolean.TRUE;
                        }
                    }
                }
                String mess = EMPTY_STRING;
                //项目经理
                if ("Y".equalsIgnoreCase(strJF_DocProjectReview)) {
                    personId = JF_PublicMethodClass_mxJPO.getProjectWherePerson(context, strProjectSpaceId, SELECT_ID, "", "attribute[Project Role]=='Project manager'");
                    if (UIUtil.isNullOrEmpty(personId)) {
                        stringBuilder.append(ComponentsUIUtil.getI18NString(context, context.getLocale().toString(), "emxComponents.Document.ProjectManagerError", new String[]{}));
                        flag = Boolean.TRUE;
                    } else {
                        //判断是否活动
                        personObject.setId(personId);
                        if ("Inactive".equalsIgnoreCase(personObject.getInfo(context, SELECT_CURRENT))) {
                            stringBuilder.append(ComponentsUIUtil.getI18NString(context, context.getLocale().toString(), "emxComponents.Document.ProjectManagerInactive", new String[]{}));
                            flag = Boolean.TRUE;
                        }
                    }
                }
                //整椅经理
                if ("Y".equalsIgnoreCase(strJF_DocChairManagerReview)) {
                    personId = JF_PublicMethodClass_mxJPO.getProjectWherePerson(context, strProjectSpaceId, SELECT_ID, "", "attribute[Project Role]=='Chair manager'");
                    if (UIUtil.isNullOrEmpty(personId)) {
                        stringBuilder.append(ComponentsUIUtil.getI18NString(context, context.getLocale().toString(), "emxComponents.Document.ChairManagerError", new String[]{}));
                        flag = Boolean.TRUE;
                    } else {
                        //判断是否活动
                        personObject.setId(personId);
                        if ("Inactive".equalsIgnoreCase(personObject.getInfo(context, SELECT_CURRENT))) {
                            stringBuilder.append(ComponentsUIUtil.getI18NString(context, context.getLocale().toString(), "emxComponents.Document.ChairManagerInactive", new String[]{}));
                            flag = Boolean.TRUE;
                        }
                    }
                }
                //部门经理,需要校验是否关联了人员
                if ("Y".equalsIgnoreCase(JF_DocDepartmentManager) && "FALSE".equalsIgnoreCase(hasBDR)) {
                    StringList objIds = document.getInfoList(context, "from[JFDocument2DepManager].to.id");
                    if (CollectionUtils.isEmpty(objIds)) {
                        stringBuilder.append(ComponentsUIUtil.getI18NString(context, context.getLocale().toString(), "emxComponents.Document.DepManagerError", new String[]{}));
                        flag = Boolean.TRUE;
                    } else {
                        //判断是否活动
                        mess = personAccountIsActive(context, objIds, "emxComponents.Document.DepManagerInactive");
                        if (UIUtil.isNotNullAndNotEmpty(mess)) {
                            stringBuilder.append(mess);
                            flag = Boolean.TRUE;
                        }
                    }
                }
                if ("FALSE".equalsIgnoreCase(hasBDR)) {
                    //会签人员
                    StringList signPersonList = document.getInfoList(context, "from[JFDocument2SignPerson].to.id");
                    if (!signPersonList.isEmpty()) {
                        //判断是否活动
                        mess = personAccountIsActive(context, signPersonList, "emxComponents.Document.SignPersonInactive");
                        if (UIUtil.isNotNullAndNotEmpty(mess)) {
                            stringBuilder.append(mess);
                            flag = Boolean.TRUE;
                        }
                    }
                }
                //是否专家回执/是否专家审核,需要校验是否关联了人员
                if ("Y".equalsIgnoreCase(JF_DocSpecialistReceipt) || "Y".equalsIgnoreCase(JF_DocSpecialistReview)) {
                    //20260805 update by ljr 按“项目-专家组-人员”结构获取同时匹配文档类型和专业的专家。
                    StringList specialistPersonIdList = JF_PublicMethodClass_mxJPO.getProjectDocumentExpertPersonIds(
                            context, strProjectSpaceId, strJF_ProjectDocType, strJF_DocSpecialty);
                    if (CollectionUtils.isEmpty(specialistPersonIdList)) {
                        stringBuilder.append(ComponentsUIUtil.getI18NString(context, context.getLocale().toString(), "emxComponents.Document.CheckStartRoute", new String[]{}));
                        flag = Boolean.TRUE;
                    } else {
                        //判断是否活动
                        mess = personAccountIsActive(context, specialistPersonIdList, "emxComponents.Document.SpecialistInactive");
                        if (UIUtil.isNotNullAndNotEmpty(mess)) {
                            stringBuilder.append(mess);
                            flag = Boolean.TRUE;
                        }
                    }
                }
            }
            //20260806 update by ljr 分类配置为Yes时，提交审批前必须校验项目已维护对应角色成员，缺失角色一次性汇总提示。
            Map<String, StringList> noticeRolePersonMap = getDocumentNoticeRolePersonMap(context, documentId, strProjectSpaceId);
            StringList missingNoticeRoleList = new StringList();
            for (Map.Entry<String, StringList> entry : noticeRolePersonMap.entrySet()) {
                if (CollectionUtils.isEmpty(entry.getValue())) {
                    String roleKey = "emxFramework.Range.Project_Role." + entry.getKey().replace(" ", "_");
                    missingNoticeRoleList.add(EnoviaResourceBundle.getProperty(
                            context, "emxFrameworkStringResource", context.getLocale(), roleKey));
                }
            }
            if (CollectionUtils.isNotEmpty(missingNoticeRoleList)) {
                String noticeRoleError = EnoviaResourceBundle.getProperty(
                        context, "emxFrameworkStringResource", context.getLocale(),
                        "emxFramework.Document.NoticeRoleNotMaintained");
                stringBuilder.append("\n").append(noticeRoleError.replace("{0}", missingNoticeRoleList.join(",")));
                flag = Boolean.TRUE;
            }
            //add by ljr  20260610  判断项目文档提交的时候必须有文档
            MapList fileList = getFiles(context, documentId);
            if (fileList.size() ==0 ) {
                flag = Boolean.TRUE;
                stringBuilder.append("\n");
                stringBuilder.append(ComponentsUIUtil.getI18NString(context, context.getLocale().toString(), "emxComponents.Common.NoFileAssociated", new String[]{}));
            }
            if (flag){
                ischeck = 1;
                String tileMess = stringBuilder.toString();
                emxContextUtil_mxJPO.mqlNotice(context, tileMess);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
        return ischeck;
    }

    /**
    * 判断人员账号是否为活动
    * @param context
	* @param personList
	* @param strMess
    * @author LIUJR
    * @throws
    * @return java.lang.String
    * @date 2025/10/15 10:01
    * @description
    */
    public static String personAccountIsActive(Context context, StringList personList, String strMess) throws Exception{
        //判断是否活动
        String mess = EMPTY_STRING;
        try {
            String personId = EMPTY_STRING;
            StringList personNameList = new StringList();
            DomainObject personObject = DomainObject.newInstance(context);
            for (int i = 0; i < personList.size(); i++) {
                //判断是否活动
                personId = personList.get(i);
                personObject.setId(personId);
                if ("Inactive".equalsIgnoreCase(personObject.getInfo(context, SELECT_CURRENT))) {
                    personNameList.add(JF_PublicMethodClass_mxJPO.getPersonAllName(context,personId));
                }
            }
            if (!personNameList.isEmpty()) {
                mess = ComponentsUIUtil.getI18NString(context, context.getLocale().toString(), strMess, new String[]{});
                mess = mess.replace("1", personNameList.join(","));
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
        return mess;
    }

    /**
     *     在工作中-冻结的Promote增加Trigger，自动添加流程
     *     1）当“直线领导审核	”为“是”时，审核节点添加直线经理节点
     *     2）当“部门经理审核	”为“是”时，审核节点添加部门经理节点
     *     2）当“整椅经理审核	”为“是”时，审核节点添加整椅经理节点
     *     2）当“项目经理审核	”为“是”时，审核节点添加项目经理节点
     *     2）当“是否专家审核	”为“是”时，审核节点添加专家审核节点	 Or 当“是否专家回执	”为“是”时，审核节点添加专家回执节点 (两者其一，关联人员为同一个关系)
     *     3）当会签人员有关系时，审核节点添加会签人员节点
     *     **** 20260806 update by ljr
     *     库分类上面增加三个属性
     *     JF_Notice_AME_representative、
     *     JF_Notice_AQE_representativePQL、
     *     JF_Notice_SQD_Representative 标识该库分类下面的文档流程是否需要通知
     *     文档分类任一通知属性为Yes时，获取项目对应角色成员，在最后审核顺序增加并行Comment备注任务。
     *     提交审批时同步校验项目是否维护对应角色成员，缺失时阻止提交并汇总提示。
     * @param context
     * @param args
     * @author LIUJR
     * @throws
     * @return void
     * @date 29/9/2025 15:16
     * @description
     */
    public void projectDocumentGenerationRoute(Context context, String[] args) throws Exception{
        _logger.info("projectDocumentGenerationRoute start。。。。。。。。。。。。");
        try {
            String strDocId = args[0];
            DomainObject domainObject = DomainObject.newInstance(context);
            domainObject.setId(strDocId);
            //判断文档是否是项目文档
            StringList attributeList = new StringList();
            attributeList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JF_ProjectDoc);
            attributeList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JF_CounterSign);
            attributeList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JF_DocSpecialistReview);
            attributeList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JF_DocSpecialistReceipt);
            attributeList.add(JF_PLMConstants_mxJPO.select_attr_JF_DocReceiptConfirmation);
            attributeList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JF_DocChairManagerReview);
            attributeList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JF_DocDepartmentManager);
            attributeList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JF_DocLeadReview);
            attributeList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JF_DocProjectReview);
            attributeList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JF_DocSpecialty);
            attributeList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JF_ProjectDocType);
            attributeList.add(SELECT_OWNER);
            //拿取属性
            Map attributeMap = domainObject.getInfo(context, attributeList);
            String strJF_ProjectDoc = UIUtil.getValue(attributeMap, JF_PLMConstants_mxJPO.SELECT_ATTR_JF_ProjectDoc);
            String strJF_CounterSign = UIUtil.getValue(attributeMap, JF_PLMConstants_mxJPO.SELECT_ATTR_JF_CounterSign);
            String strJF_DocSpecialistReview = UIUtil.getValue(attributeMap, JF_PLMConstants_mxJPO.SELECT_ATTR_JF_DocSpecialistReview);
            String strJF_DocSpecialistReceipt = UIUtil.getValue(attributeMap, JF_PLMConstants_mxJPO.SELECT_ATTR_JF_DocSpecialistReceipt);
            String strJF_DocChairManagerReview = UIUtil.getValue(attributeMap, JF_PLMConstants_mxJPO.SELECT_ATTR_JF_DocChairManagerReview);
            String strJF_DocDepartmentManager = UIUtil.getValue(attributeMap, JF_PLMConstants_mxJPO.SELECT_ATTR_JF_DocDepartmentManager);
            String strJF_DocLeadReview = UIUtil.getValue(attributeMap, JF_PLMConstants_mxJPO.SELECT_ATTR_JF_DocLeadReview);
            String strJF_DocProjectReview = UIUtil.getValue(attributeMap, JF_PLMConstants_mxJPO.SELECT_ATTR_JF_DocProjectReview);
            String strJF_DocReceiptConfirmation = UIUtil.getValue(attributeMap, JF_PLMConstants_mxJPO.select_attr_JF_DocReceiptConfirmation);
            String strJF_DocSpecialty = UIUtil.getValue(attributeMap, JF_PLMConstants_mxJPO.SELECT_ATTR_JF_DocSpecialty);
            String strJF_ProjectDocType = UIUtil.getValue(attributeMap, JF_PLMConstants_mxJPO.SELECT_ATTR_JF_ProjectDocType);
            String strOwner = UIUtil.getValue(attributeMap, SELECT_OWNER);
            //不是项目文档，不生成流程
            if ("N".equalsIgnoreCase(strJF_ProjectDoc)) {
                return;
            }
            //开始构造流程
            //流程标题
            MapList approveList = new MapList();
            Integer approveIndex = 1;
            String tileMess = ComponentsUIUtil.getI18NString(context, context.getLocale().toString(), "emxComponents.DOCManger.ROUTE.TitleMess", new String[]{});

            //审批人
            StringList personList = new StringList();
            String titleMess = EMPTY_STRING;
            String personId = EMPTY_STRING;
            //20260806 update by ljr 提前获取文档项目及分类通知角色，保证只有通知节点时也能正常创建流程。
            String strPaths = domainObject.getInfo(context, "to[Vaulted Objects].from.attribute[Folder Path]");
            String strProjectSpaceId = EMPTY_STRING;
            if (UIUtil.isNotNullAndNotEmpty(strPaths)) {
                strProjectSpaceId = strPaths.split("\\|")[0];
            } else {
                strProjectSpaceId = domainObject.getInfo(context, "to[Task Deliverable].from.to[Project Access Key].from.from[Project Access List].to.id");
            }
            if ("XSO".equalsIgnoreCase(strJF_ProjectDocType)) {
                titleMess = EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", context.getLocale(), "emxComponents.DOCManger.ROUTE.DepartmentManager");
                DomainObject personObject = PersonUtil.getPersonObject(context, strOwner);
                MapList mapList = personObject.getRelatedObjects(context,
                        RELATIONSHIP_MEMBER,
                        TYPE_DEPARTMENT,
                        StringList.create(SELECT_DESCRIPTION, SELECT_ID),
                        JF_Util_mxJPO.basicRellistSel(),
                        true,
                        false,
                        (short) 1,
                        "",
                        "",
                        0);
                _logger.info("mapList:{}", mapList);
                HashSet personSet = (HashSet) mapList.stream().map(m -> {
                    Map map = (Map) m;
                    return UIUtil.getValue(map, SELECT_DESCRIPTION);
                }).collect(Collectors.toCollection(HashSet::new));
                _logger.info("personSet:{}", personSet);
                personList = StringList.create(personSet);
                for (int i = 0; i < personList.size(); i++) {
                    String personName = personList.get(i);
                    personId = PersonUtil.getPersonObjectID(context, personName);
                    Map managerMap = JF_PublicMethodClass_mxJPO.getMapAnyOrAll(personId, titleMess, "true", approveIndex.toString(), "All");
                    approveList.add(managerMap);
                }
            } else {
                //获取是否有会签人员  JFDocument2SignPerson
                StringList signPersonList = domainObject.getInfoList(context, "from[JFDocument2SignPerson].to.id");
                String signPerson = signPersonList.size() > 0 ? "Y" : "N";
                //20260826 update by caipan 分类通知节点移至文档发布后的通知流程，不再参与冻结审批流程。
                //判断所有审批节点是否均为否且会签人员是否为空。
                boolean allAreN = Stream.of(strJF_DocSpecialistReview, strJF_DocSpecialistReceipt,
                                strJF_DocChairManagerReview, strJF_DocDepartmentManager,
                                strJF_DocLeadReview, strJF_DocProjectReview,
                                signPerson)
                        .allMatch(str -> "N".equalsIgnoreCase(str));
                if (allAreN) {
                    //直接发布
                    domainObject.promote(context);
                    return;
                }
                //直线经理审批
                if ("Y".equalsIgnoreCase(strJF_DocLeadReview)) {
                    titleMess = EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", context.getLocale(), "emxComponents.DOCManger.ROUTE.LineManager");
                    personId = JF_PublicMethodClass_mxJPO.getPersonLineManager(context, null, domainObject.getOwner(context).getName());
                    if (UIUtil.isNotNullAndNotEmpty(personId)) {
                        Map managerMap = JF_PublicMethodClass_mxJPO.getMapAnyOrAll(personId, titleMess, "true", approveIndex.toString(), "All");
                        approveList.add(managerMap);
                        personList.add(personId);
                        approveIndex++;
                    }
                }
                //专业会签
                if ("Y".equalsIgnoreCase(signPerson)) {
                    titleMess = EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", context.getLocale(), "emxComponents.DOCManger.ROUTE.SignPerson");
                    // 标记是否添加了新审批人
                    boolean addedNewPerson = false;
                    for (int i = 0; i < signPersonList.size(); i++) {
                        personId = signPersonList.get(i);
                        Map managerMap;
                        if (personList.contains(personId)) {
                            managerMap = JF_PublicMethodClass_mxJPO.getMapAnyOrAllNotifyOnly(personId, titleMess, "true", approveIndex.toString(), "All");
                        } else {
                            managerMap = JF_PublicMethodClass_mxJPO.getMapAnyOrAll(personId, titleMess, "true", approveIndex.toString(), "All");
                        }
                        approveList.add(managerMap);
                        personList.add(personId);
                        // 标记为添加了新审批人
                        addedNewPerson = true;
                    }
                    if (addedNewPerson) {
                        approveIndex++;
                    }
                }
                //整椅经理
                if ("Y".equalsIgnoreCase(strJF_DocChairManagerReview)) {
                    titleMess = EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", context.getLocale(), "emxComponents.DRChairManger.ROUTE.MESS");
                    //SDT-整椅经理
                    personId = JF_PublicMethodClass_mxJPO.getProjectWherePerson(context, strProjectSpaceId, SELECT_ID, "", "attribute[Project Role]=='Chair manager'");
                    if (UIUtil.isNotNullAndNotEmpty(personId)) {
                        Map managerMap;
                        if (personList.contains(personId)) {
                            managerMap = JF_PublicMethodClass_mxJPO.getMapAnyOrAllNotifyOnly(personId, titleMess, "true", approveIndex.toString(), "All");
                        } else {
                            managerMap = JF_PublicMethodClass_mxJPO.getMapAnyOrAll(personId, titleMess, "true", approveIndex.toString(), "All");
                        }
                        approveList.add(managerMap);
                        personList.add(personId);
                        approveIndex++;
                    }
                }
                //部门经理审核
                if ("Y".equalsIgnoreCase(strJF_DocDepartmentManager)) {
                    StringList departmentList = domainObject.getInfoList(context, "from[JFDocument2DepManager].to.id");
                    titleMess = EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", context.getLocale(), "emxComponents.CommonDocument.JF_DepartmentManger");
                    // 标记是否添加了新审批人
                    boolean addedNewPerson = false;
                    for (int i = 0; i < departmentList.size(); i++) {
                        personId = departmentList.get(i);
                        Map managerMap;
                        if (personList.contains(personId)) {
                            managerMap = JF_PublicMethodClass_mxJPO.getMapAnyOrAllNotifyOnly(personId, titleMess, "true", approveIndex.toString(), "All");
                        } else {
                            managerMap = JF_PublicMethodClass_mxJPO.getMapAnyOrAll(personId, titleMess, "true", approveIndex.toString(), "All");
                        }
                        approveList.add(managerMap);
                        personList.add(personId);
                        // 标记为添加了新审批人
                        addedNewPerson = true;
                    }
                    if (addedNewPerson) {
                        approveIndex++;
                    }
                }
                //项目经理
                if ("Y".equalsIgnoreCase(strJF_DocProjectReview)) {
                    titleMess = EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", context.getLocale(), "emxComponents.DOCManger.ROUTE.ProjectManager");
                    //SDT-项目经理
                    personId = JF_PublicMethodClass_mxJPO.getProjectWherePerson(context, strProjectSpaceId, SELECT_ID, "", "attribute[Project Role]=='Project manager'");
                    //SDT-项目经理
                    if (UIUtil.isNotNullAndNotEmpty(personId)) {
                        Map managerMap;
                        if (personList.contains(personId)) {
                            managerMap = JF_PublicMethodClass_mxJPO.getMapAnyOrAllNotifyOnly(personId, titleMess, "true", approveIndex.toString(), "All");
                        } else {
                            managerMap = JF_PublicMethodClass_mxJPO.getMapAnyOrAll(personId, titleMess, "true", approveIndex.toString(), "All");
                        }
                        approveList.add(managerMap);
                        personList.add(personId);
                        approveIndex++;
                    }
                }
                //专家审核
                StringList specialistPersonIdList = new StringList();
                if ("Y".equalsIgnoreCase(strJF_DocSpecialistReview)
                        || "Y".equalsIgnoreCase(strJF_DocSpecialistReceipt)) {
                    //20260805 update by ljr 复用提交校验的专家匹配逻辑，多个专家使用同一审批顺序并行处理。
                    specialistPersonIdList = JF_PublicMethodClass_mxJPO.getProjectDocumentExpertPersonIds(
                            context, strProjectSpaceId, strJF_ProjectDocType, strJF_DocSpecialty);
                }
                //审核专家审批
                if ("Y".equals(strJF_DocSpecialistReview)) {
                    titleMess = EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", context.getLocale(), "emxComponents.DOCManger.ROUTE.SpecialistPerson");
                    // 标记是否添加了新审批人
                    boolean addedNewPerson = false;
                    for (int i = 0; i < specialistPersonIdList.size(); i++) {
                        personId = specialistPersonIdList.get(i);
                        Map managerMap;
                        if (personList.contains(personId)) {
                            managerMap = JF_PublicMethodClass_mxJPO.getMapAnyOrAllNotifyOnly(personId, titleMess, "true", approveIndex.toString(), "All");
                        } else {
                            managerMap = JF_PublicMethodClass_mxJPO.getMapAnyOrAll(personId, titleMess, "true", approveIndex.toString(), "All");
                        }
                        approveList.add(managerMap);
                        personList.add(personId);
                        // 标记为添加了新审批人
                        addedNewPerson = true;
                    }
                    if (addedNewPerson) {
                        approveIndex++;
                    }
                } else if ("Y".equals(strJF_DocSpecialistReceipt)) {
                    //专家回执
                    titleMess = EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", context.getLocale(), "emxComponents.DOCManger.ROUTE.ReceiptSpecialist");
                    // 标记是否添加了新审批人
                    boolean addedNewPerson = false;
                    for (int i = 0; i < specialistPersonIdList.size(); i++) {
                        personId = specialistPersonIdList.get(i);
                        //20260903 update by liujr 专家回执必须生成独立审批任务，不受前序审批人员去重影响。
                        Map managerMap = JF_PublicMethodClass_mxJPO.getMapAnyOrAll(personId, titleMess, "true", approveIndex.toString(), "All");
                        approveList.add(managerMap);
                        personList.add(personId);
                        // 标记为添加了新审批人
                        addedNewPerson = true;
                    }
                    if (addedNewPerson) {
                        approveIndex++;
                    }
                }
                //增加回执确认节点
                if ("Y".equalsIgnoreCase(strJF_DocReceiptConfirmation)) {
                    {
                        titleMess = EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", context.getLocale(), "emxComponents.DOCManger.ROUTE.DocReceiptConfirmation");
                        personId = PersonUtil.getPersonObjectID(context, domainObject.getInfo(context, DomainConstants.SELECT_OWNER));
                        if (UIUtil.isNotNullAndNotEmpty(personId)) {
                            Map managerMap = JF_PublicMethodClass_mxJPO.getMapAnyOrAll(personId, titleMess, "true", approveIndex.toString(), "All");
                            approveList.add(managerMap);
                            approveIndex++;
                        }
                    }
                }
            }
            //创建流程
            JF_Route_mxJPO jf_route = new JF_Route_mxJPO(context, args);
            String routeDescription = tileMess;//流程描述
            String routeId = jf_route.createAndStartRoute(
                    context,
                    approveList,
                    strDocId,
                    "state_FROZEN",
                    "policy_Document",
                    routeDescription);
        }catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
        _logger.info("projectDocumentGenerationRoute end。。。。。。。。。。。。");
    }

    /**
     * 文档发布后根据分类配置创建SDT通知任务。
     *
     * @param context Matrix上下文
     * @param args Trigger传入的文档ID
     * @return void
     * @throws Exception 查询通知人员或创建流程失败时抛出异常
     * @author caipan by codex
     * @date 2026/8/26
     */
    public void projectDocumentReleaseCreateNoticeRoute(Context context, String[] args) throws Exception {
        _logger.info("projectDocumentReleaseCreateNoticeRoute start。。。。。。。。。。。。");
        try {
            String documentId = args[0];
            DomainObject document = DomainObject.newInstance(context, documentId);
            String isProjectDocument = document.getInfo(context, JF_PLMConstants_mxJPO.SELECT_ATTR_JF_ProjectDoc);
            if (!"Y".equalsIgnoreCase(isProjectDocument)) {
                return;
            }
            //20260826 update by caipan 批量发布的通知流程统一关联批量审批单，文档自身不重复创建通知流程。
            String batchReviewId = document.getInfo(
                    context, "to[Reference Document].from[JFBatchDocumentReview].id");
            if (UIUtil.isNotNullAndNotEmpty(batchReviewId)) {
                return;
            }

            String projectPath = document.getInfo(context, "to[Vaulted Objects].from.attribute[Folder Path]");
            String projectId;
            if (UIUtil.isNotNullAndNotEmpty(projectPath)) {
                projectId = projectPath.split("\\|")[0];
            } else {
                projectId = document.getInfo(context,
                        "to[Task Deliverable].from.to[Project Access Key].from.from[Project Access List].to.id");
            }
            Map<String, StringList> noticeRolePersonMap =
                    getDocumentNoticeRolePersonMap(context, documentId, projectId);
            MapList noticeTaskList = new MapList();
            for (Map.Entry<String, StringList> entry : noticeRolePersonMap.entrySet()) {
                String roleTitleKey = "emxFramework.Range.Project_Role." + entry.getKey().replace(" ", "_");
                String roleTitle = EnoviaResourceBundle.getProperty(
                        context, "emxFrameworkStringResource", context.getLocale(), roleTitleKey);
                for (String noticePersonId : entry.getValue()) {
                    // 同一发布顺序并行通知；同一人员兼任多个角色时保留各角色任务。
                    noticeTaskList.add(JF_PublicMethodClass_mxJPO.getInboxTaskMap(
                            noticePersonId, roleTitle, "true", "1", "All", "Comment", roleTitle));
                }
            }
            if (noticeTaskList.isEmpty()) {
                return;
            }

            String routeDescription = ComponentsUIUtil.getI18NString(
                    context,
                    context.getLocale().toString(),
                    "emxComponents.DOCManger.ROUTE.TitleMess",
                    new String[]{});
            new JF_Route_mxJPO(context, args).createAndStartRouteForNotify(
                    context,
                    noticeTaskList,
                    documentId,
                    "state_RELEASED",
                    "policy_Document",
                    routeDescription);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
        _logger.info("projectDocumentReleaseCreateNoticeRoute end。。。。。。。。。。。。");
    }

    /**
     * 定时器方法
     * 创建一个定时器定时去查询未打水印的标识文档，判断其pdf文件，并打上水印；
     * 把文档更新成打上水印版本--打上Release 和发布时间
     * @param context
     * @param args
     * @author LIUJR
     * @throws
     * @return void
     * @date 04/06/2025 09:55
     * @description
     */
    public void waterMarkDocumentProcess(Context context, String[] args) throws Exception{
        _logger.info("postProjectDocumentReleaseWaterMark start。。。。。。。。。。。。");
        Boolean isPush = Boolean.FALSE;
        try {
            ContextUtil.pushContext(context);
            isPush = Boolean.TRUE;
            MapList mapList = DomainObject.findObjects(context, TYPE_DOCUMENT, "*", "attribute[JF_WaterMarkFlag]==N", JF_Util_mxJPO.basicBolistSel());
            if (mapList.isEmpty()) {
                return;
            }
            _logger.info("mapList：{}",mapList);
            DomainObject domainObject = DomainObject.newInstance(context);
            for (int iDoc = 0; iDoc < mapList.size(); iDoc++) {
                Map docMap = (Map) mapList.get(iDoc);
                String docId = UIUtil.getValue(docMap, SELECT_ID);
                domainObject.setId(docId);
                //判断是否项目技术文档
                String strProjectDoc = domainObject.getAttributeValue(context, JF_PLMConstants_mxJPO.ATTR_JF_ProjectDoc);
                if ("N".equalsIgnoreCase(strProjectDoc)) {
                    continue;
                }
                _logger.info("是否项目技术文档：{}",strProjectDoc);
                //获取文档下PDF文件
                MapList versionList = JF_PublicMethodClass_mxJPO.getDocumentFiles(context, docId);
                StringList waterMarkList = new StringList();

                String strDirPath = context.createWorkspace() + File.separator;   //存储地址
                MapList checkInMapList = new MapList();
                StringList saveFileNameList = new StringList();
                CommonDocument commonDocument = new CommonDocument();
                commonDocument.setId(docId);
                StringList hasDocPPTList = new StringList();
                StringList pdfSourceFileIdList = new StringList();   //JF_PDFSourceFileId: 35845.4994.60916.49390
                Boolean pdfFlag = Boolean.FALSE;
                for (int i = 0; i < versionList.size(); i++) {
                    Map map = (Map) versionList.get(i);
                    String fileName = (String) map.get(CommonDocument.SELECT_TITLE);
                    String fileId = (String) map.get(CommonDocument.SELECT_ID);
                    String strPDFSourceFileId = (String) map.get("attribute[JF_PDFSourceFileId].value");
                    if (fileName.endsWith(".doc") || fileName.endsWith(".docx") || fileName.endsWith(".ppt") || fileName.endsWith(".pptx")) {
                        hasDocPPTList.add(fileId);
                    }
                    if (fileName.endsWith(".pdf")) {
                        if (UIUtil.isNotNullAndNotEmpty(strPDFSourceFileId)) {
                            pdfSourceFileIdList.add(strPDFSourceFileId);
                        }
                        pdfFlag = Boolean.TRUE;
                    }
                }
                _logger.info("pdfFlag：{}",pdfFlag);
                _logger.info("pdfSourceFileIdList：{}",pdfSourceFileIdList);
                _logger.info("hasDocPPTList：{}",hasDocPPTList);
                /*   update by ljr 20260513
                 * 转换pdf结果
                 * 1.pdf文件数 >= docppt文件数，可能pdf文件上传的文件数量本来就多，需要再判断pdf文件的源文件的数是否与docppt文件数量一致
                 *   如果pdf源文件数量<docppt数量，没转换完全需要标识
                 *   数量一致，不需要标识
                 * 2.pdf文件数<docppt文件数，说明本身就没转完整，需要将没转pdf的文件打上标识没有打印水印
                 * */
                Boolean flag = Boolean.TRUE;
                for (int i = 0; i < hasDocPPTList.size(); i++) {
                    //不包含代表没有转换pdf完成
                    if (!pdfSourceFileIdList.contains(hasDocPPTList.get(i))) {
                        flag = Boolean.FALSE;
                        break;
                    }
                    //没有转换pdf需要打印标识
                }
                _logger.info("flag：{}",flag);
                if (!flag) {
                    //如果需要全打印水印 先设置标识  弹出提示信息
                    continue;
                }
                _logger.info("pdfFlag：{}",pdfFlag);
                //如果没有需要打印水印
                if (!pdfFlag) {
                    continue;
                }
                //设置为已经打印  在检查完的时候  开始下载文件的时候就要设置为已打印
                _logger.info("设置为已经打印 start");
                MqlUtil.mqlCommand(context,false,"mod bus "+docId+" JF_WaterMarkFlag Y",true);
                _logger.info("设置为已经打印 end");
                for (int i = 0; i < versionList.size(); i++) {
                    Map map = (Map) versionList.get(i);
                    String fileName = (String) map.get(CommonDocument.SELECT_TITLE);
                    String physicalid = (String) map.get("physicalid");
                    if (fileName.endsWith(".pdf")) {
                        FileList fileList = new FileList();
                        matrix.db.File file = new matrix.db.File(fileName, "generic");
                        fileList.add(file);
                        if (saveFileNameList.contains(fileName)) {
                            //新换目录
                            waterMarkList.add(strDirPath + physicalid + File.separator + fileName);
                            map.put("dirPath", strDirPath + physicalid + File.separator);
                            commonDocument.checkoutFiles(context, false, "generic", fileList, strDirPath + physicalid + File.separator);
                        } else {
                            waterMarkList.add(strDirPath + fileName);
                            map.put("dirPath", strDirPath);
                            saveFileNameList.add(fileName);
                            commonDocument.checkoutFiles(context, false, "generic", fileList, strDirPath);
                        }
                        checkInMapList.add(map);
                    }
                }
                _logger.info("waterMarkList：{}",waterMarkList);
                _logger.info("checkInMapList：{}",checkInMapList);
                //是否有需要加水印的PDF文件
                //加水印
                String date = domainObject.getInfo(context, "state[RELEASED].actual");
                // 解析输入日期
                LocalDateTime dateTime = LocalDateTime.parse(date, inputFormatter);
                // 格式化输出日期
                // 拆分并格式化日期部分和时间部分
                date = dateTime.format(formatterDate);
                _logger.info("waterMarkList：{}", waterMarkList);
                _logger.info("checkInMapList：{}", checkInMapList);
                JF_WaterMarkUtils_mxJPO.addWaterMarkUniformDistribution(context, waterMarkList, "Released " + date);
                //check in回去
                _logger.info("checkInMapList：{}", checkInMapList);
                for (int i = 0; i < checkInMapList.size(); i++) {
                    Map map = (Map) checkInMapList.get(i);
                    String fileFormat = (String) map.get(CommonDocument.SELECT_FILE_FORMAT);
                    String fileName = (String) map.get(CommonDocument.SELECT_TITLE);
                    String dirPath = (String) map.get("dirPath");
                    commonDocument.checkinFile(context, true, true, DomainConstants.EMPTY_STRING, fileFormat, fileName, dirPath);
                    //需要将文件进行删除
                    new File(dirPath + fileName).delete();
                }
                _logger.info("checkInMapList：{}",checkInMapList);
            }
        } catch (Exception exception){
            exception.printStackTrace();
            _logger.info("error:{}", exception.getStackTrace().toString());
            throw exception;
        } finally {
            if (isPush) {
                ContextUtil.popContext(context);
            }
        }
        _logger.info("projectDocumentReleaseWaterMark end。。。。。。。。。。。。");
    }

    /**
     * 根据文档分类通知配置，获取项目中对应角色的成员。
     * 返回Map中只包含分类配置为Yes的角色；角色未维护成员时保留空的StringList，供提交校验识别。
     * @param context
     * @param documentId 文档ID
     * @param projectId 项目ID
     * @author LIUJR
     * @throws Exception
     * @return java.util.Map<java.lang.String, matrix.util.StringList>
     * @date 2026/8/6
     * @description
     */
    public static Map<String, StringList> getDocumentNoticeRolePersonMap(Context context, String documentId,
                                                                         String projectId) throws Exception {
        Map<String, StringList> noticeRolePersonMap = new LinkedHashMap<>();

        // 一个文档可能关联多个分类，只要任一分类配置为Yes，就启用对应项目角色通知。
        StringList classSelectList = new StringList();
        classSelectList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JF_Notice_AME_representative);
        classSelectList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JF_Notice_AQE_representativePQL);
        classSelectList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JF_Notice_SQD_Representative);
        MapList classList = DomainObject.newInstance(context, documentId).getRelatedObjects(
                context,
                "Classified Item",
                "General Class",
                classSelectList,
                new StringList(),
                true,
                false,
                (short) 1,
                EMPTY_STRING,
                EMPTY_STRING,
                0);
        for (Object classInfoObject : classList) {
            Map classInfo = (Map) classInfoObject;
            if ("Yes".equalsIgnoreCase(UIUtil.getValue(
                    classInfo, JF_PLMConstants_mxJPO.SELECT_ATTR_JF_Notice_AME_representative))) {
                noticeRolePersonMap.putIfAbsent(JF_PLMConstants_mxJPO.ATTR_PROJECT_ROLE_Range_AME, new StringList());
            }
            if ("Yes".equalsIgnoreCase(UIUtil.getValue(
                    classInfo, JF_PLMConstants_mxJPO.SELECT_ATTR_JF_Notice_AQE_representativePQL))) {
                noticeRolePersonMap.putIfAbsent(JF_PLMConstants_mxJPO.ATTR_PROJECT_ROLE_Range_AQE, new StringList());
            }
            if ("Yes".equalsIgnoreCase(UIUtil.getValue(
                    classInfo, JF_PLMConstants_mxJPO.SELECT_ATTR_JF_Notice_SQD_Representative))) {
                noticeRolePersonMap.putIfAbsent(
                        JF_PLMConstants_mxJPO.ATTR_PROJECTROLE_RANGE_SQDRepresentative, new StringList());
            }
        }
        if (noticeRolePersonMap.isEmpty() || UIUtil.isNullOrEmpty(projectId)) {
            return noticeRolePersonMap;
        }

        // 根据已启用的角色拼接一次关系过滤条件，一次查询返回所有对应项目成员。
        StringBuilder roleWhere = new StringBuilder();
        for (String projectRole : noticeRolePersonMap.keySet()) {
            if (roleWhere.length() > 0) {
                roleWhere.append(" || ");
            }
            roleWhere.append("attribute[Project Role]=='").append(projectRole).append("'");
        }
        String projectRoleSelect = "attribute[Project Role]";
        MapList projectPersonList = DomainObject.newInstance(context, projectId).getRelatedObjects(
                context,
                RELATIONSHIP_MEMBER,
                TYPE_PERSON,
                new StringList(SELECT_ID),
                new StringList(projectRoleSelect),
                false,
                true,
                (short) 1,
                EMPTY_STRING,
                roleWhere.toString(),
                0);
        for (Object personInfoObject : projectPersonList) {
            Map personInfo = (Map) personInfoObject;
            String projectRole = UIUtil.getValue(personInfo, projectRoleSelect);
            String personId = UIUtil.getValue(personInfo, SELECT_ID);
            StringList personIdList = noticeRolePersonMap.get(projectRole);
            if (personIdList != null && UIUtil.isNotNullAndNotEmpty(personId) && !personIdList.contains(personId)) {
                personIdList.add(personId);
            }
        }
        return noticeRolePersonMap;
    }

    /**
     * 单文档提交审批时添加Change Control接口。
     *
     * @param context Matrix上下文
     * @param args Trigger传入的文档ID
     * @throws Exception 文档无效或接口添加失败时抛出异常
     * @author caipan by codex
     * @date 2026/8/27 18:30
     */
    public void addDocumentChangeControl(Context context, String[] args) throws Exception {
        StringList documentIds = new StringList();
        if (args != null) {
            for (String documentId : args) {
                if (UIUtil.isNotNullAndNotEmpty(documentId) && !documentIds.contains(documentId)) {
                    documentIds.add(documentId);
                }
            }
        }
        addDocumentChangeControlForDocuments(context, documentIds);
    }

    /**
     * 给一组文档主对象添加Change Control接口。
     *
     * @param context Matrix上下文
     * @param documentIds 文档主对象ID集合
     * @throws Exception 文档无效或接口添加失败时抛出异常
     * @author caipan by codex
     * @date 2026/8/27 18:30
     */
    public static void addDocumentChangeControlForDocuments(Context context, StringList documentIds) throws Exception {
        updateDocumentChangeControl(context, documentIds, true);
    }

    /**
     * 单文档发布、撤回或驳回时移除Change Control接口。
     *
     * @param context Matrix上下文
     * @param args Trigger或业务入口传入的文档ID
     * @throws Exception 文档无效或接口移除失败时抛出异常
     * @author caipan by codex
     * @date 2026/8/27 18:30
     */
    public void removeDocumentChangeControl(Context context, String[] args) throws Exception {
        StringList documentIds = new StringList();
        if (args != null) {
            for (String documentId : args) {
                if (UIUtil.isNotNullAndNotEmpty(documentId) && !documentIds.contains(documentId)) {
                    documentIds.add(documentId);
                }
            }
        }
        removeDocumentChangeControlForDocuments(context, documentIds);
    }

    /**
     * 从一组文档主对象移除Change Control接口。
     *
     * @param context Matrix上下文
     * @param documentIds 文档主对象ID集合
     * @throws Exception 文档无效或接口移除失败时抛出异常
     * @author caipan by codex
     * @date 2026/8/27 18:30
     */
    public static void removeDocumentChangeControlForDocuments(Context context, StringList documentIds) throws Exception {
        updateDocumentChangeControl(context, documentIds, false);
    }

    /**
     * 统一维护文档主对象的Change Control接口。
     *
     * @param context Matrix上下文
     * @param documentIds 文档主对象ID集合
     * @param enable true表示添加接口，false表示移除接口
     * @throws Exception 文档无效或接口维护失败时抛出异常
     * @author caipan by codex
     * @date 2026/8/27 18:30
     */
    private static void updateDocumentChangeControl(Context context, StringList documentIds,
                                                    boolean enable) throws Exception {
        if (documentIds == null || documentIds.isEmpty()) {
            return;
        }
        Map<String, Boolean> documentInterfaceMap = new LinkedHashMap<>();
        for (String documentId : documentIds) {
            if (UIUtil.isNullOrEmpty(documentId) || documentInterfaceMap.containsKey(documentId)) {
                continue;
            }
            DomainObject document = DomainObject.newInstance(context, documentId);
            if (!document.exists(context) || !document.isKindOf(context, CommonDocument.TYPE_DOCUMENTS)) {
                throw new FrameworkException("Invalid document for Change Control: " + documentId);
            }
            StringList interfaceList = document.getInfoList(context, "interface");
            documentInterfaceMap.put(documentId, interfaceList.contains(INTERFACE_CHANGE_CONTROL));
        }

        boolean pushed = false;
        try {
            ContextUtil.pushContext(context);
            pushed = true;
            for (Map.Entry<String, Boolean> documentEntry : documentInterfaceMap.entrySet()) {
                String documentId = documentEntry.getKey();
                boolean hasChangeControl = documentEntry.getValue();
                if (enable && !hasChangeControl) {
                    MqlUtil.mqlCommand(context, "mod bus $1 add interface $2",
                            documentId, INTERFACE_CHANGE_CONTROL);
                } else if (!enable && hasChangeControl) {
                    MqlUtil.mqlCommand(context, "mod bus $1 remove interface $2",
                            documentId, INTERFACE_CHANGE_CONTROL);
                }
            }
        } finally {
            if (pushed) {
                ContextUtil.popContext(context);
            }
        }
    }

}
