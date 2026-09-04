import com.dassault_systemes.enovia.enterprisechangemgt.common.ChangeAction;
import com.dscn.plm.util.NioJDUtils;
import com.matrixone.apps.common.CommonDocument;
import com.matrixone.apps.common.Route;
import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.util.*;
import com.matrixone.apps.framework.ui.UIUtil;
import com.matrixone.apps.program.ProgramCentralConstants;
import matrix.db.*;
import matrix.util.MatrixException;
import matrix.util.StringList;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URLDecoder;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created with IntelliJ IDEA.
 * Copyright@ Apache Open Source Organization
 *
 * @Auther: HLY
 * @Date: 2023-08-07-11:44
 * @Description:
 */
public class JF_OfficeConverPdfJob_mxJPO {
    private static final Logger _logger =  LoggerFactory.getLogger(JF_OfficeConverPdfJob_mxJPO.class);
    //临时路径
    private static final String dirPath = "/tmp/jdxconvertpdf/";
    private static final String ATTR_JDX_PDFSourceFileId = "attribute[JF_PDFSourceFileId]";
    private static final String TYPE_Document = "Document";
    private static final String TYPE_VPMReference = "VPMReference";

    private static final String ATTR_Title = "attribute[Title]";

    public String getStrFileName() {
        return strFileName;
    }

    public void setStrFileName(String strFileName) {
        this.strFileName = strFileName;
    }
    private String strFileName ;
    /**
     * @param context
     * @param args
     * @return void
     * @Author HLY
     * @CreateTime 2023/8/7
     * @Description: 文档检入时 转pdf job caipan 入口
     */
    public void checkInConvertJob(Context context, String[] args) throws Exception {
        _logger.info(" --------------------- checkInConvertJob 61--------------------- ");
        //转pdf 并检入
        String docId = args[0];
        synchronized (docId.intern()) {
         convertPdfAndCheckIn(context, args);
            _logger.info(" --------------------- checkInConvertJob 61end--------------------- ");
        }
    }


    /**
     * @param context
     * @param domainObject
     * @param dirPath
     * @param format
     * @param filename
     * @return void
     * @Author HLY
     * @CreateTime 2023/8/7
     * @Description: 下载文件到本地
     */
    public void checkOutFile(Context context, DomainObject domainObject, String dirPath, String format, String filename) throws Exception {
        boolean locked = domainObject.isLocked(context);
        if (locked) {
            domainObject.unlock(context);
        }
        if (!dirPath.endsWith(File.separator)) {
            dirPath = dirPath + File.separator;
        }
        File dir = new File(dirPath);
        if (!dir.exists()) {
            dir.mkdirs();
        }
//            checkoutFile(Context context, boolean lock, String format, String file, String directory)
//            domainObject.checkoutFile( context,  false,  format,  filename,  dirPath);
//        matrix.db.File cofile = new matrix.db.File(filename, format);
//        FileList fileList = new FileList();
//        fileList.add(cofile);
//        domainObject.checkoutFiles(context, false, format, fileList, dirPath);
        domainObject.checkoutFile(context, false, format, filename, dirPath);
    }

    /**
     * @param context
     * @param commonDocument
     * @return void
     * @Author HLY
     * @CreateTime 2023/8/7
     * @Description: 文档对象 检入文件
     */
    private void addOrUpdateVersionFile(Context context, CommonDocument commonDocument, String docId, String fileId, String format, String filename, String dirPath) throws MatrixException, InterruptedException {

//        _logger.info(" ---------------------------------------------- addOrUpdateVersionFile ---------------------------------------------- ");
        //检入文件
        addOrUpdateVersionFile(context, commonDocument, docId, fileId, format, filename, dirPath, null);
//        _logger.info(" ---------------------------------------------- addOrUpdateVersionFile ---------------------------------------------- ");

    }


    /**
     * @param context
     * @param commonDocument
     * @return void
     * @Author HLY
     * @CreateTime 2023/8/7
     * @Description: 文档对象 检入文件
     */
    private void addOrUpdateVersionFile(Context context, CommonDocument commonDocument, String docId, String fileId, String format, String filename, String dirPath, String JDX_PDFSource) throws MatrixException, InterruptedException {
        _logger.info("addOrUpdateVersionFile 文档对象 检入文件 start");
        //判断文件是否存在
        File file = new File(dirPath+File.separator+filename);
        if (file.exists() && file.isFile()) {
            _logger.info("more file 文件存在且是普通文件");
        //end
        _logger.info(" ---------------------------------------------- addOrUpdateVersionFile ---------------------------------------------- ");
        //检入文件
//        commonDocument.checkinFile(context, true, true, "localhost", format, filename, dirPath);
        _logger.info("commonDocument {}: " , commonDocument);
        _logger.info("format {}: " , format);
        _logger.info("filename {}: " , filename);
        _logger.info("dirPath {}: " ,dirPath);
        _logger.info("JDX_PDFSource {}: " ,JDX_PDFSource);
        _logger.info("fileId {}: " ,fileId);

        //防止自动检入 pdf 导致的并发问题
        Thread.sleep(200);//caiby caipan

//        String[] args = new String[]{docId};
//        Map cfMap = JPO.invoke(context, "JDX_DocumentDeferredTrigger", null, "getConvertFile", args, Map.class);
//        _logger.info("cfMap : "+cfMap);
        //获取文档下所有的文件
        Map paramMap = new HashMap();
        paramMap.put("objectId", docId);
        String[] strings = JPO.packArgs(paramMap);
        MapList fileList = JPO.invoke(context, "JF_DocumentTrigger", null, "getFiles", strings, MapList.class);
        _logger.info("fileList.size() {}: " , fileList.size());


        //只有一个文件
        //判断源文件还存在
        DomainObject fileObj = DomainObject.newInstance(context, fileId);
        DomainObject docObj = DomainObject.newInstance(context, docId);
       BusinessObject bus = fileObj.getPreviousRevision(context);
       String previousId = "";
       if (bus.exists(context)){
           previousId = bus.getObjectId(context);
           _logger.info("上一个版本是:{}",previousId);
       }else{
           _logger.info("没有上一个版本");
       }
        if (fileList.size() == 1) {

            //检入文件
            //第一个true unlock 第二个 appand
            ContextUtil.pushContext(context);

            try {

                for (int i = 0; i < 100; i++) {
                    try {
                        MqlUtil.mqlCommand(context, false, true, "checkin bus $1 $2 $3 $4 $5", true, new String[]{docId, "format", "generic", "append", dirPath + File.separator + filename});
                        break;
                    } catch (FrameworkException e) {
                        e.printStackTrace();
                        Thread.sleep(1000*10);
                    }
                }
               // commonDocument.checkinFile(context, true, true, DomainConstants.EMPTY_STRING, format, filename, dirPath);
                _logger.info("commonDocument.checkinFile {}: " , dirPath);

                //创建 Document 文件( policy Version)对象
//                String versionId = commonDocument.createVersion(context, "", filename, new HashMap());
                StringList titleList = commonDocument.getInfoList(context, "from[Latest Version].to.attribute[Title]");
                String versionId = createDocVersion(context,docId,titleList,filename);
                _logger.info("versionId {}: " , versionId);

                DomainObject version = DomainObject.newInstance(context, versionId);
                //添加接口
//                addInterface(context, version, StringList.create("Idm_DocExt"));

                version.setAttributeValue(context, "JF_PDFSourceFileId", fileId);
                if (UIUtil.isNotNullAndNotEmpty(JDX_PDFSource)) {
                    version.setAttributeValue(context, "JF_PDFSource", JDX_PDFSource);
                }

                String docOnwer = commonDocument.getOwner(context).getName();
                version.setOwner(context, docOnwer);
                version.setAttributeValue(context,"Originator",docOnwer);
            } catch (MatrixException e) {
                e.printStackTrace();
            }
            ContextUtil.popContext(context);


        } else if (fileList.size() >1) {
            //有两个文件 文件升版

      /*      //检查是否已经转换过数据 判断现在的数据是不是升版的数据
            Map pdfMap = null;
            Map officeMap = null;
            String JDX_PDFSourceFileId = "";
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
            }*/
//第一次创建
            if (UIUtil.isNullOrEmpty(previousId)) {
                _logger.info("多文件上传，支持转PDF");
                ContextUtil.pushContext(context);
                try {
//                    commonDocument.checkinFile(context, true, true, DomainConstants.EMPTY_STRING, format, filename, dirPath);
                    File file2 = new File(dirPath + File.separator + filename);
                    if (file.exists() && file.isFile()) {
                        _logger.info("上传文件最后一步检测文件是否存在:存在");
                        for (int i = 0; i < 100; i++) {
                            try {
                                MqlUtil.mqlCommand(context, false, true, "checkin bus $1 $2 $3 $4 $5", true, new String[]{docId, "format", "generic", "append", dirPath + File.separator + filename});
                                break;
                            } catch (FrameworkException e) {
                                e.printStackTrace();
                                Thread.sleep(1000 * 10);
                            }
                        }

                        _logger.info("commonDocument.checkinFile {}: ", dirPath);

                        //创建 Document 文件( policy Version)对象
//                    String versionId = commonDocument.createVersion(context, "", filename, new HashMap());
                        StringList titleList = commonDocument.getInfoList(context, "from[Latest Version].to.attribute[Title]");
                        String versionId = createDocVersion(context, docId, titleList, filename);
                        _logger.info("versionId {}: ", versionId);

                        DomainObject version = DomainObject.newInstance(context, versionId);
                        //添加接口
//                addInterface(context, version, StringList.create("Idm_DocExt"));

                        version.setAttributeValue(context, "JF_PDFSourceFileId", fileId);
                        if (UIUtil.isNotNullAndNotEmpty(JDX_PDFSource)) {
                            version.setAttributeValue(context, "JF_PDFSource", JDX_PDFSource);
                        }

                        String docOnwer = commonDocument.getOwner(context).getName();
                        version.setOwner(context, docOnwer);
                        version.setAttributeValue(context, "Originator", docOnwer);
                    }else{
                        _logger.info("checkIn文件不存在 274");
                    }
                }catch (MatrixException e) {
                    e.printStackTrace();
                }
                ContextUtil.popContext(context);
            } else {
                //升版的情况怎么处理
                //拿到旧版本的对象ID，匹配属性JF_PDFSourceFileId 拿到PDF文件，在进行升版
                _logger.info("升版模式");
                StringList selList = JF_Util_mxJPO.basicBolistSel();
                selList.add(ATTR_JDX_PDFSourceFileId);
                selList.add(DomainConstants.SELECT_ATTRIBUTE_TITLE);
                String where = ATTR_JDX_PDFSourceFileId + "=='" + previousId + "'";
                MapList list = docObj.getRelatedObjects(context,
                        CommonDocument.RELATIONSHIP_LATEST_VERSION, CommonDocument.TYPE_DOCUMENTS,
                        selList, //the eMatrix StringList object that holds the list of select statement pertaining to Business Obejcts.
                        JF_Util_mxJPO.basicRellistSel(), //the eMatrix StringList object that holds the list of select statement pertaining to Relationships.
                        false, //get To relationships
                        true, //get From relationships
                        (short) 1, //the number of levels to expand, 0 equals expand all.
                        where, //where clause to apply to objects, can be empty ""
                        null, //where clause to apply to relationship, can be empty ""
                        (short) 1); //limit
                _logger.info("list.size:{} where:{}", list, where);
                if (list.size() == 1) {
                    //加锁 升版
                    Map map = (Map) list.get(0);
                    _logger.info("找到了对应的PDF文件:{}", map);
                    String oldPdfName = UIUtil.getValue(map, DomainConstants.SELECT_ATTRIBUTE_TITLE);
                    DomainObject oldPdfObj = DomainObject.newInstance(context, UIUtil.getValue(map, DomainConstants.SELECT_ID));
                    Boolean flag = false;
                    try {
                        ContextUtil.pushContext(context);
                        flag = true;
                        boolean locked = oldPdfObj.isLocked(context);
                        _logger.info("locked {}: ", locked);
                        if (!locked) {
                            oldPdfObj.lock(context);
                        }
                        locked = oldPdfObj.isLocked(context);
                        _logger.info("locked {}: ", locked);

                        //文件对象(policy Version)升版
                        String newVerId = commonDocument.reviseVersion(context, oldPdfName, filename, new HashMap());
                        _logger.info("newVerId {}: ", newVerId);

                        //检入新的 pdf 文件
                        //第一个true unlock 第二个 appand
                        commonDocument.checkinFile(context, true, true, DomainConstants.EMPTY_STRING, format, filename, dirPath);
                        _logger.info("commonDocument.checkinFile {}: ", dirPath);

                        DomainObject newVerObj = DomainObject.newInstance(context, newVerId);
                        _logger.info("newVerObj {}: ", newVerObj);

                        //添加接口
//                    addInterface(context, newVerObj, StringList.create("Idm_DocExt"));

                        //更新属性
                        newVerObj.setAttributeValue(context, "JF_PDFSourceFileId", fileId);
                        if (UIUtil.isNotNullAndNotEmpty(JDX_PDFSource)) {
                            newVerObj.setAttributeValue(context, "JF_PDFSource", JDX_PDFSource);
                        }

                        String docOnwer = commonDocument.getOwner(context).getName();
                        newVerObj.setOwner(context, docOnwer);
                        newVerObj.setAttributeValue(context, "Originator", docOnwer);
                    } catch (MatrixException e) {
                        e.printStackTrace();
                    }
                    if (flag) {
                        ContextUtil.popContext(context);
                    }
                }
            }
            _logger.info("addOrUpdateVersionFile 文档对象 检入文件 end");
        }

           /* //判断当前情况下 有转换过的文件（pdf文件） ，有需要转换的文件，推断当前文件是升版
            if (officeMap != null && !officeMap.isEmpty() && pdfMap != null && !pdfMap.isEmpty()) {
                //            [{relationship=Active Version, owner=2015079, level=1, attribute[Suspend Versioning]=False, format.file.format=generic, originated=4/11/2023 3:50:48 PM, type=CHIDI Document, format.file.name=重庆市水利工程信息模型设计交付标准.dwg, isLatestRevision=true, DocumentOwner=2015079, current.access[lock]=TRUE, revision=1, attribute[Title]=重庆市水利工程信息模型设计交付标准.dwg, current.access[unlock]=FALSE, attribute[Is Version Object]=True, locker=, id=14780.60259.52595.42703, current.access[checkout]=TRUE, masterId=14780.60259.52595.42676, description=, current.access[checkin]=TRUE, format.file.size=93346, format.file.modified=4/11/2023 3:50:48 PM, locked=FALSE, fileId=14780.60259.52595.42676}]

                _logger.info("JDX_PDFSourceFileId {}: " , JDX_PDFSourceFileId);
                _logger.info("JDX_PDFSourceFileId.equals(fileId) {}: " , (JDX_PDFSourceFileId.equals(fileId)) + JDX_PDFSourceFileId + " --> " + fileId);

                if (JDX_PDFSourceFileId.equals(fileId)) {
                    _logger.info("JDX_PDFSourceFileId.equals(fileId) == true ,file is convert before!!!");
                    return;
                }

                try {
                    //判断源文件还存在


                    String oldPdfName = (String) pdfMap.get("format.file.name");
                    String oldPdfId = (String) pdfMap.get(DomainConstants.SELECT_ID);
                    DomainObject oldPdfObj = DomainObject.newInstance(context, oldPdfId);
                    _logger.info("oldPdfObj {}: " , oldPdfObj);

                    //加锁 升版
                    ContextUtil.pushContext(context);
                    boolean locked = oldPdfObj.isLocked(context);
                    _logger.info("locked {}: " , locked);
                    if (!locked) {
                        oldPdfObj.lock(context);
                    }
                    locked = oldPdfObj.isLocked(context);
                    _logger.info("locked {}: " , locked);

                    //文件对象(policy Version)升版
                    String newVerId = commonDocument.reviseVersion(context, oldPdfName, filename, new HashMap());
                    _logger.info("newVerId {}: " , newVerId);

                    //检入新的 pdf 文件
                    //第一个true unlock 第二个 appand
                    commonDocument.checkinFile(context, true, true, DomainConstants.EMPTY_STRING, format, filename, dirPath);
                    _logger.info("commonDocument.checkinFile {}: " , dirPath);

                    DomainObject newVerObj = DomainObject.newInstance(context, newVerId);
                    _logger.info("newVerObj {}: " , newVerObj);

                    //添加接口
//                    addInterface(context, newVerObj, StringList.create("Idm_DocExt"));

                    //更新属性
                    newVerObj.setAttributeValue(context, "JF_PDFSourceFileId", fileId);
                    if (UIUtil.isNotNullAndNotEmpty(JDX_PDFSource)) {
                        newVerObj.setAttributeValue(context, "JF_PDFSource", JDX_PDFSource);
                    }

                    String docOnwer = commonDocument.getOwner(context).getName();
                    newVerObj.setOwner(context, docOnwer);
                    newVerObj.setAttributeValue(context,"Originator",docOnwer);
                } catch (MatrixException e) {
                    e.printStackTrace();
                }
                ContextUtil.popContext(context);
            }*/


        }

        _logger.info(" ---------------------------------------------- addOrUpdateVersionFile ---end---------------------------------------------- ");

    }


    /**
     * @param path 文件夹完整绝对路径
     * @return boolean
     * @Author HLY
     * @CreateTime 2023/8/7
     * @Description: 删除指定文件夹下所有文件
     */
    public static boolean delAllFile(String path) {
        boolean flag = false;
        File file = new File(path);
        if (!file.exists()) {
            return flag;
        }
        if (!file.isDirectory()) {
            return flag;
        }
        String[] tempList = file.list();
        File temp = null;
        for (int i = 0; i < tempList.length; i++) {
            if (path.endsWith(File.separator)) {
                temp = new File(path + tempList[i]);
            } else {
                temp = new File(path + File.separator + tempList[i]);
            }
            if (temp.isFile()) {
                temp.delete();
            }
            if (temp.isDirectory()) {
                delAllFile(path + "/" + tempList[i]);// 先删除文件夹里面的文件
                delFolder(path + "/" + tempList[i]);// 再删除空文件夹
                flag = true;
            }
        }
        return flag;
    }

    /**
     * @param folderPath 文件夹完整绝对路径
     * @return void
     * @Author HLY
     * @CreateTime 2023/8/7
     * @Description: 删除文件夹
     */
    public static void delFolder(String folderPath) {
        try {
            delAllFile(folderPath); // 删除完里面所有内容
            String filePath = folderPath;
            filePath = filePath.toString();
            File myFilePath = new File(filePath);
            myFilePath.delete(); // 删除空文件夹
        } catch (Exception e) {
            e.printStackTrace();
        }
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
                _logger.error(e.getMessage());
            }
//            ContextUtil.popContext(context);
        }
    }

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
     * @param context
     * @param args
     * @return void
     * @Author HLY
     * @CreateTime 2023/8/7
     * @Description: 文档工作中提升至冻结 触发的 pdf 转换
     */
    public void frozenConvertJob(Context context, String[]args) throws Exception {
        _logger.info(" --------------------- frozenConvertJob --------------------- ");
        convertPdfAndCheckIn(context, args);
        _logger.info(" --------------------- frozenConvertJob --------------------- ");
    }

    /**
     * @return void
     * @Author HLY
     * @CreateTime 2023/8/9
     * @Description: 新增或更新文件版本
     */
    private void addOrUpdatePdfVersion(Context context, JSONObject convertJson, CommonDocument docObj, String docId, String fileId, String format, String tmpDir) throws Exception {

        addOrUpdatePdfVersion(context, convertJson, docObj, docId, fileId, format, tmpDir, null);

    }

    /**
     * @return void
     * @Author HLY
     * @CreateTime 2023/8/9
     * @Description: 新增或更新文件版本
     */
    private void addOrUpdatePdfVersion(Context context, JSONObject convertJson, CommonDocument docObj, String docId, String fileId, String format, String tmpDir, String JDX_PDFSource) throws Exception {
        _logger.info("addOrUpdatePdfVersion 进入创建文档版本 start");
        Properties prop = NioJDUtils.readPageObject(context, "JFJDConfig");
//        JSONObject convertJson = new JSONObject(convertResult);

        String state = convertJson.getString("state");
        _logger.info("state {}: " , state);


        if (UIUtil.isNotNullAndNotEmpty(state) && state.equals("success")) {


            //pdf 名称
            String pdfname = convertJson.getString("filename");
            pdfname = URLDecoder.decode(pdfname, "UTF-8");
            _logger.info("pdfname {}: " , pdfname);

            //缓存pdf文件到服务器
//            FileUtils.writeStringToFile(pdfFile, fileStr, "UTF-8");
            //把返回内容中的字符串转成文件
            savePdf(convertJson, tmpDir);

            File pdfFile = new File(tmpDir + pdfname);
            _logger.info("pdf文件接收成功 pdfFile {}: " , pdfFile);

            //检入文件
            if (pdfFile.exists() && pdfFile.isFile()) {
                if (UIUtil.isNullOrEmpty(this.strFileName)){
                    //源文件对象 在回传回来的时候把文件名还原
                    DomainObject fileObj = DomainObject.newInstance(context, fileId);
                    String oldTitle = fileObj.getAttributeValue(context, "Title");
                    oldTitle = getFilePrefix(oldTitle);
                    String newPdfName = oldTitle+".pdf";
                    this.setStrFileName(newPdfName);
                }
                _logger.info("this.strFileName:{}",this.strFileName);
//                //源文件对象 在回传回来的时候把文件名还原
//                DomainObject fileObj = DomainObject.newInstance(context, fileId);
//                String oldTitle = fileObj.getAttributeValue(context, "Title");
//                oldTitle = getFilePrefix(oldTitle);
//                String newPdfName = oldTitle+".pdf";
                pdfFile.renameTo(new File(tmpDir + this.strFileName));
                _logger.info("改文件名:{}",this.strFileName);
                addOrUpdateVersionFile(context, docObj, docId, fileId, format, this.strFileName, tmpDir, JDX_PDFSource);
            } else {
//                        _logger.warning("fileStr : "+fileStr);
                String convertPdfSaveFail = prop.getProperty("jf.jobtitle.ConvertPdfSaveFail");
//                String convertPdfSaveFail = EnoviaResourceBundle.getProperty(context, "emxFrameworkStringResource", context.getLocale(), "emxFramework.jobtitle.ConvertPdfSaveFail");
                _logger.info("转换失败convertPdfSaveFail {}: " , convertPdfSaveFail);
                throw new Exception(convertPdfSaveFail);
            }
        } else {
            //转换失败 抛异常
            String msg = convertJson.getString("msg");
            _logger.info("addOrUpdatePdfVersion 异常 msg {}: " , msg);
            throw new Exception(msg);
        }
        _logger.info("addOrUpdatePdfVersion 进入创建文档版本 end");
    }


    /**
     * @param context
     * @param args
     * @return void
     * @Author HLY
     * @CreateTime 2023/8/7
     * @Description: 文档工作中提升至冻结 触发的 pdf 转换
     */
    public void caRouteConvertJob(Context context, String[] args) throws Exception {
        _logger.info(" --------------------- caRouteConvertJob --------------------- ");

        try {
            String caId = args[0];
            _logger.info("caId {}: " , caId);

            String routeId = args[1];
            _logger.info("routeId {}: " , routeId);

            //CA
            ChangeAction changeAction = new ChangeAction();
            changeAction.setId(caId);

            //获取 CA受影项中的 文档 及物理产品下关联的文档
            MapList caAffectedDoc = getCaAffectedDoc(context, changeAction);
            _logger.info("caAffectedDoc {}: " , caAffectedDoc);

            //循环处理 文档
            for (int i = 0; i < caAffectedDoc.size(); i++) {
                Map map_i = (Map) caAffectedDoc.get(i);

                //处理一个 CA的文档
                handlerOneCaDoc(context, map_i);
            }



            //自动完成 流程节点 pdf机器人
            StringList busList = new StringList();
            busList.add(DomainConstants.SELECT_ID);
            busList.add(DomainConstants.SELECT_CURRENT);
            busList.add(ATTR_Title);
            Route route = (Route) Route.newInstance(context, routeId);
            MapList taskList = route.getRouteTasks(context, busList, null, null, false);

            String pdfTaskName = EnoviaResourceBundle.getProperty(context, "emxFrameworkStringResource", context.getLocale(), "emxFramework.Common.PdfTaskName");
//            _logger.info("pdfTaskName : " + pdfTaskName);

            String pdfConvertComplated = EnoviaResourceBundle.getProperty(context, "emxFrameworkStringResource", context.getLocale(), "emxFramework.Comment.PdfConvertComplated");
//            _logger.info("pdfConvertComplated : " + pdfConvertComplated);
            
            Policy policy = new Policy(DomainConstants.POLICY_INBOX_TASK);
            String firstState = policy.getStateRequirements(context).get(0).getName();
//            _logger.info("firstState : " + firstState);

            List<Map> pdfTaskList = (List<Map>) taskList.stream().filter(m -> {
                Map map = (Map)m;
                String current = (String) map.get(DomainConstants.SELECT_CURRENT);
                String title = (String) map.get(ATTR_Title);
                if(current.equals(firstState) && title.toLowerCase().equals(pdfTaskName)){
                    return true;
                }else {
                    return false;
                }

            }).collect(Collectors.toList());
//            _logger.info("pdfTaskList : " + pdfTaskList);

            if(pdfTaskList!=null && !pdfTaskList.isEmpty()){
                ContextUtil.pushContext(context);
                for (int i = 0; i <pdfTaskList.size() ; i++) {
                    Map map_i = pdfTaskList.get(i);
                    String iTaskId_i = (String) map_i.get(DomainConstants.SELECT_ID);
                    DomainObject iTask_i =  DomainObject.newInstance(context, iTaskId_i);
                    iTask_i.setAttributeValue(context,"Comments",pdfConvertComplated);
                    iTask_i.promote(context);
//                    _logger.info("iTask_i.promote(context) : " + iTaskId_i);
                }
                ContextUtil.popContext(context);
            }

        } catch (Exception e) {
            _logger.error(e.getMessage());
            throw e;
        } finally {
            //删除临时文件夹
//            delFolder(tmpDir);
        }
        _logger.info(" --------------------- caRouteConvertJob --------------------- ");

    }

    /**
     * @param context
     * @param changeAction
     * @return MapList
     * @Author HLY
     * @CreateTime 2023/8/9
     * @Description: 获取CA 下后影响项中的CA
     */
    private MapList getCaAffectedDoc(Context context, ChangeAction changeAction) throws Exception {

        MapList docList = new MapList();
        StringList docIdList = new StringList();

        //CA 下受影响项 （建议的更改）
        MapList affectedItems = changeAction.getAffectedItems(context);
//        _logger.info("affectedItems : " + affectedItems);

        for (int i = 0; i < affectedItems.size(); i++) {
            Map map_i = (Map) affectedItems.get(i);
//            _logger.info("map_i : " + map_i);

            String id_i = (String) map_i.get(DomainConstants.SELECT_ID);
            DomainObject obj_i = DomainObject.newInstance(context, id_i);
//            _logger.info("obj_i : " + obj_i);

            String physicalId_i = obj_i.getInfo(context, DomainConstants.SELECT_PHYSICAL_ID);
//            _logger.info("docIdList : " + docIdList);
//            _logger.info("!docIdList.contains(physicalId_i) : " + !docIdList.contains(physicalId_i));

            if (obj_i.isKindOf(context, "Document") && !docIdList.contains(physicalId_i)) {
//                docIdList.add(physicalId_i);
                docIdList.add(physicalId_i);
                map_i.put(DomainConstants.SELECT_PHYSICAL_ID, physicalId_i);
                docList.add(map_i);
            } else if (obj_i.isKindOf(context, TYPE_VPMReference)) {
                addVPMReferenceDoc(context, id_i, docList, docIdList);
            }
        }
        return docList;
    }

    /**
     * @param context
     * @param vpmId
     * @return MapList
     * @Author HLY
     * @CreateTime 2023/8/9
     * @Description: 获取物理产品下的文档对象
     */
//    private void addVPMReferenceDoc(Context context, DomainObject vpmObj, MapList docList, StringList docIdList) throws MatrixException {
    private void addVPMReferenceDoc(Context context, String vpmId, MapList docList, StringList docIdList) throws MatrixException {
//        _logger.info(" ----------------------------------- addVPMReferenceDoc ----------------------------------- ");
        ContextUtil.pushContext(context);
//        String vpmDocStr = vpmObj.getInfo(context, "from[VPLMrel/PLMConnection/V_Owner].to[PLMDocConnection].paths[SemanticRelation].path.element[0] dump |");
//        MqlUtil.mqlCommand(context,"print bus VPMReference prd-83114872-00000017 A select from[VPLMrel/PLMConnection/V_Owner].to[PLMDocConnection].paths[SemanticRelation].path.element[0] dump |")
//        String vpmDocStr = MqlUtil.mqlCommand(context, "print bus $1 select $2 dump $3", vpmId, "from[VPLMrel/PLMConnection/V_Owner].to[PLMDocConnection].paths[SemanticRelation].path.element[0]", "|");
        //处理PSE 中的的规格文档
//        String vpmDocStr = MqlUtil.mqlCommand(context, "print bus $1 select $2 dump $3", vpmId, "from[SpecificationDocument].to[Document].id", "|");

        boolean vpmSpecificationDoc = getExpressionValue(context, "JDX_VPMSpecificationDocConvertPdf");
        boolean vpmReferenceDoc = getExpressionValue(context, "JDX_VPMReferenceDocConvertPdf");
        boolean vpmClientDoc = getExpressionValue(context, "JDX_VPMClientDocConvertPdf");
//        _logger.info("vpmSpecificationDoc : " + vpmSpecificationDoc);
//        _logger.info("vpmReferenceDoc : " + vpmReferenceDoc);
//        _logger.info("vpmClientDoc : " + vpmClientDoc);

//        StringBuffer vpmDocBuffer = new StringBuffer();
        //PSE 规格文档
        if (vpmSpecificationDoc) {
            String vpmSpecificationStr = MqlUtil.mqlCommand(context, "print bus $1 select $2 dump $3", vpmId, "from[SpecificationDocument].to[Document].id", "|");
//            _logger.info("vpmSpecificationStr : " + vpmSpecificationStr);

            if (UIUtil.isNotNullAndNotEmpty(vpmSpecificationStr)) {
                String[] resultSplit = vpmSpecificationStr.split("\\|");
//            _logger.info("resultSplit : "+resultSplit);
                for (String str : resultSplit) {
//                _logger.info("str : "+str);
                    if (!docIdList.contains(str)) {
                        try {
                            DomainObject obj = DomainObject.newInstance(context, str);
                            String physicalid = obj.getInfo(context, DomainConstants.SELECT_PHYSICAL_ID);
                            Map map = new HashMap();
                            map.put(DomainConstants.SELECT_PHYSICAL_ID, physicalid);
                            docList.add(map);
                            docIdList.add(physicalid);
                        } catch (FrameworkException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }

        //PSE 附件
        if (vpmReferenceDoc) {
            String vpmReferenceStr = MqlUtil.mqlCommand(context, "print bus $1 select $2 dump $3", vpmId, "from[Reference Document].to[Document].id", "|");
//            _logger.info("vpmReferenceStr : " + vpmReferenceStr);

            if (UIUtil.isNotNullAndNotEmpty(vpmReferenceStr)) {
                String[] resultSplit = vpmReferenceStr.split("\\|");
//            _logger.info("resultSplit : "+resultSplit);
                for (String str : resultSplit) {
//                _logger.info("str : "+str);
                    if (!docIdList.contains(str)) {
                        try {
                            DomainObject obj = DomainObject.newInstance(context, str);
                            String physicalid = obj.getInfo(context, DomainConstants.SELECT_PHYSICAL_ID);
                            Map map = new HashMap();
                            map.put(DomainConstants.SELECT_PHYSICAL_ID, physicalid);
                            docList.add(map);
                            docIdList.add(physicalid);
                        } catch (FrameworkException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }

        //客户端添加的文档
        if (vpmClientDoc) {
            String vpmClientStr = MqlUtil.mqlCommand(context, "print bus $1 select $2 dump $3", vpmId, "from[VPLMrel/PLMConnection/V_Owner].to[PLMDocConnection].paths[SemanticRelation].path.element[0]", "|");
//            _logger.info("vpmClientStr : " + vpmClientStr);

            if (UIUtil.isNotNullAndNotEmpty(vpmClientStr)) {
                String[] resultSplit = vpmClientStr.split("\\|");
//            _logger.info("resultSplit : "+resultSplit);
                for (String str : resultSplit) {
//                _logger.info("str : "+str);
                    if (str.toLowerCase().contains("businessobject")) {
                        String[] objSplit = str.split("\\,");
//                      _logger.info("objSplit : "+objSplit);
                        String physicalid = objSplit[2];
                        try {
                            DomainObject obj = DomainObject.newInstance(context, physicalid);
//                        _logger.info("obj : "+obj);
                            if (obj.isKindOf(context, "Document") && !docIdList.contains(physicalid)) {
                                //                        Map info = obj.getInfo(context, busList);
                                Map map = new HashMap();
                                map.put(DomainConstants.SELECT_PHYSICAL_ID, physicalid);
                                docList.add(map);
                                docIdList.add(physicalid);
                            }
                        } catch (MatrixException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
        ContextUtil.popContext(context);

        _logger.info(" ----------------------------------- addVPMReferenceDoc ----------------------------------- ");

    }

    /**
     * @param context
     * @param map
     * @return void
     * @Author HLY
     * @CreateTime 2023/8/9
     * @Description: 处理一个 CA 下的文档
     */
    private void handlerOneCaDoc(Context context, Map map) throws Exception {
        _logger.info(" --------------------- handlerOneCaDoc --------------------- ");

        String physicalId = (String) map.get(DomainConstants.SELECT_PHYSICAL_ID);
        CommonDocument docObj = (CommonDocument) CommonDocument.newInstance(context, physicalId);
//        String docId = (String) map.get(DomainConstants.SELECT_ID);
//        CommonDocument docObj = (CommonDocument) CommonDocument.newInstance(context, docId);

        String docId = docObj.getInfo(context, DomainConstants.SELECT_ID);
//        _logger.info("docId : " + docId);

        Map fileMap = JPO.invoke(context, "JDX_DocumentTrigger", null, "getConvertFile", new String[]{docId}, Map.class);
//        _logger.info("fileMap : " + fileMap);

        if (fileMap != null && !fileMap.isEmpty()) {

            String[] convertArgs = new String[]{(String) fileMap.get("masterId"), (String) fileMap.get("id"), (String) fileMap.get("format.file.name"), (String) fileMap.get("format.file.size"), (String) fileMap.get("format.file.format")};

            //转pdf 并检入
            convertPdfAndCheckIn(context, convertArgs);
        }

        _logger.info(" --------------------- handlerOneCaDoc --------------------- ");
    }

    /**
     * @param convertJson
     * @param outDirPath
     * @return void
     * @Author HLY
     * @CreateTime 2023/8/11
     * @Description: 字符串 转换 pdf 文件
     */
    private void savePdf(JSONObject convertJson, String outDirPath) throws IOException {
        //                "state": "success",
//                        "msg": "",
//                        "file": "JVBERi0xLjc
//                "filename": "%E6%8E%
//                "filesize": "264761"

        File outDir = new File(outDirPath);
        if (!outDir.exists() || !outDir.isDirectory()) {
            outDir.mkdirs();
        }
        String state = convertJson.getString("state");
        //这里使用的Google的GSON库来将返回的json数据转换成对象
        //也可以直接解析json获取返回值
        if ("success".equals(state)) {
            String filename = convertJson.getString("filename");
//            String filesize = convertJson.getString("filesize");
            String fileStr = convertJson.getString("file");

            filename = URLDecoder.decode(filename, "UTF-8");
            _logger.info("pdfname {}: " , filename);
            //服务器端将文档转换成字符串用的是base64，所以接收端也需要用base64来将字符串转换成文档
            try {
                byte[] decode = Base64.getDecoder().decode(fileStr);
                try (FileOutputStream outputStream = new FileOutputStream(outDirPath + filename)) {
                    outputStream.write(decode, 0, decode.length);
                    outputStream.flush();
                }
            } catch (IOException e) {
                throw e;
            }

        }
    }


    /**
     * @param context
     * @param args
     * @return void
     * @Author HLY
     * @CreateTime 2023/8/7
     * @Description: 文档工作中提升至冻结 触发的 pdf 转换
     */
    public void manualConvertPdf(Context context, String[] args) throws Exception {
        _logger.info(" --------------------- manualConvertPdf --------------------- ");
        convertPdfAndCheckIn(context, args);
        _logger.info(" --------------------- manualConvertPdf --------------------- ");

    }


    /**
     * @param context
     * @param args
     * @return void
     * @Author HLY
     * @CreateTime 2023/8/7
     * @Description: 文件 pdf 转换 并检入 caipan
     */
    public   void convertPdfAndCheckIn(Context context, String[] args) throws Exception {
        _logger.info(" --------------------- convertPdfAndCheckIn 964 start--------------------- ");
        String tmpDir = "";
        Properties prop = NioJDUtils.readPageObject(context, "JFJDConfig");
        String docId = args[0];
        try {
            String fileId = args[1];
            String filename = args[2];
            String filesize = args[3];
            String format = args[4];
//            _logger.info("docId : " + docId + "     fileId : " + fileId + "       filename : " + filename + "       filesize : " + filesize + "       format : " + format);
                String JDX_PDFSource = null;
                if (args.length >= 6) {
                    JDX_PDFSource = args[5];
                }
                _logger.info("JDX_PDFSource {}: ", JDX_PDFSource);
                if (UIUtil.isNullOrEmpty(JDX_PDFSource)) {
                    JDX_PDFSource = "autoConvert";
                }

                CommonDocument docObj = (CommonDocument) CommonDocument.newInstance(context, docId);
                _logger.info("docObj {}: ", docObj);

                StringList infoList = new StringList();
                infoList.add(DomainConstants.SELECT_NAME);
                infoList.add(DomainConstants.SELECT_REVISION);

                Map docInfo = docObj.getInfo(context, infoList);
                _logger.info("docInfo {}: ", docInfo);

                String docName = (String) docInfo.get(DomainConstants.SELECT_NAME);
                String docRevision = (String) docInfo.get(DomainConstants.SELECT_REVISION);

                // /tmp/jdxconvertpdf/DOC-0000005_0/
                tmpDir = dirPath + docName + "_" + docRevision + File.separator;
                _logger.info("tmpDir :  {} ", tmpDir);

                //下载文件
                checkOutFile(context, docObj, tmpDir, format, filename);

                //需要转pdf的源office文件
                File file = new File(tmpDir + filename);
                _logger.info("file : {}", file);
                boolean flag = false;
                if (file != null && file.exists() && file.isFile()) {
                    _logger.info("下载完成之后执行转换操作 {}: ", file.exists());
                    String strFilenameSuffix = getFileExtension(filename);
                    //判断是否符合办公文件
                    _logger.info("是否符合办公文件strFilenameSuffix:{}", strFilenameSuffix);
                    if (JF_PLMConstants_mxJPO.officeFileList.contains(strFilenameSuffix)) {
                        String strFilePrefix = getFilePrefix(filename);
                        String fileNewName = "temp." + strFilenameSuffix;
                        File newFile = new File(tmpDir + fileNewName);
                        _logger.info("tmpDir + fileNewName:{}", tmpDir + fileNewName);
                        file.renameTo(newFile);
                        String name = file.getName();
                        _logger.info("文件改名成 temp了 name:{}", name);
                        this.setStrFileName(strFilePrefix + ".pdf");
//                //发送请求 office文件转换成pdf文件
//                String converResult = Idm_HttpUtil_mxJPO.doPostFile(JF_PLMConstants_mxJPO.pdfConvertUrl, file, filename);
                        String strUrl = (String) prop.get("PDFConvertServer.address");
                        if (UIUtil.isNullOrEmpty(strUrl)) {
                            strUrl = JF_PLMConstants_mxJPO.PDF_CONVERT_URL;
                        }
                        String converResult = OfficeUtils_mxJPO.doPostFile(strUrl, newFile);
//                        _logger.info("转换完成的结构 : " + converResult);
                        if (UIUtil.isNotNullAndNotEmpty(converResult)) {
                            JSONObject convertJson = new JSONObject(converResult);
                            _logger.info("转换文件的结构 file Result:{}",convertJson.get("state"));
                            //新增pdf文件或更新文件版本
                            addOrUpdatePdfVersion(context, convertJson, docObj, docId, fileId, format, tmpDir, JDX_PDFSource);
                        } else {
                            //转换失败 抛异常
                            String convertPdfFail = prop.getProperty("jf.jobtitle.ConvertPdfFail");
//                    String convertPdfFail = EnoviaResourceBundle.getProperty(context, "emxFrameworkStringResource", context.getLocale(), "emxFramework.jobtitle.ConvertPdfFail");
                            _logger.info("转换失败convertPdfFail {}: ", convertPdfFail);
                            throw new Exception(convertPdfFail);
                        }
                    }

                } else {
                    //文件下载失败
//                throw new Exception("file convert to file ,check out fail !!!");
                    String docCheckOutFail = prop.getProperty("jf.jobtitle.DocCheckOutFail");
//                String docCheckOutFail = EnoviaResourceBundle.getProperty(context, "emxFrameworkStringResource", context.getLocale(), "emxFramework.jobtitle.DocCheckOutFail");
                    _logger.info("docCheckOutFail checkout失败，不是转换失败 {}: ", docCheckOutFail);
                    docCheckOutFail = docCheckOutFail.replaceAll("@1", docName).replaceAll("@2", docRevision).replaceAll("@3", filename);
                    throw new Exception(docCheckOutFail);
                }

        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        } finally {
            //删除临时文件夹
            delFolder(tmpDir);
        }
        _logger.info(" --------------------- convertPdfAndCheckIn 1060 end--------------------- ");

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

    // 方法：获取文件的后缀名
    public  String getFileExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        // 检查是否存在后缀
        if (dotIndex > 0 && dotIndex < fileName.length() - 1) {
            return fileName.substring(dotIndex + 1); // 返回后缀名，不包含点
        } else {
            return ""; // 如果没有后缀，返回空字符串
        }
    }

    // 方法：获取文件的前缀名
    public static String getFilePrefix(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');

        // 检查是否存在后缀
        if (dotIndex > 0) {
            return fileName.substring(0, dotIndex); // 返回前缀名
        } else {
            return fileName; // 如果没有后缀，返回完整文件名
        }
    }
    /*
     * @description:创建文件版本
     * @author: caipan
     * @date: 2025/10/22 11:14:23
     * @param: * @param[1] context
     * @param[2] docId
     * @param[3] titleList
     * @param[4] filename
     * @return:
     **/
    public  String createDocVersion(Context context,String docId,StringList titleList,String filename) {
        _logger.info("createDocVersion start");
        String versionId="";
        try {
//            synchronized (docId.intern()) {  // 使用字符串常量池保证锁唯一性
                if (!titleList.contains(filename)) {
                    DomainObject activeObj = DomainObject.newInstance(context);
                    activeObj.createObject(context, DomainConstants.TYPE_DOCUMENT, UUID.randomUUID().toString(), "1", "Version", ProgramCentralConstants.VAULT_eSERVICE_PRODUCTION);
                    versionId = activeObj.getInfo(context, DomainObject.SELECT_ID);
                    //System.out.println("activeAndLatestId--->"+activeAndLatestId);
                    _logger.info("versionId : " + versionId);
                    activeObj.setAttributeValue(context, "Title", filename);
                    activeObj.setAttributeValue(context, "Is Version Object", "True");

                    MqlUtil.mqlCommand(context, false, true, "add connection $1 from $2 to $3", true, new String[]{"Active Version", docId, versionId});
                    MqlUtil.mqlCommand(context, false, true, "add connection $1 from $2 to $3", true, new String[]{"Latest Version", docId, versionId});

                }
//            }
        }catch (Exception e){
            e.printStackTrace();
        }
        _logger.info("createDocVersion end");
        return versionId;
    }
}
