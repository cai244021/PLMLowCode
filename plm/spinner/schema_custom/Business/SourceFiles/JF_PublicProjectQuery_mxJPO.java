import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.dassault_systemes.dostreaminformation.ENODOStream;
import com.dassault_systemes.dostreaminformation.ENODerivedOutputStreamInfoService;
import com.dscn.plm.util.NioJDUtils;
import com.google.gson.Gson;
import com.matrixone.apps.common.CommonDocument;
import com.matrixone.apps.common.UserTask;
import com.matrixone.apps.common.util.ComponentsUtil;
import com.matrixone.apps.common.util.DocumentUtil;
import com.matrixone.apps.domain.DomainAccess;
import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.DomainRelationship;
import com.matrixone.apps.domain.util.*;
import com.matrixone.apps.framework.ui.UICache;
import com.matrixone.apps.framework.ui.UIUtil;
import com.matrixone.apps.program.PMCWorkspaceVault;
import com.matrixone.apps.program.ProgramCentralConstants;
import com.matrixone.apps.program.ProgramCentralUtil;
import ds.enovia.apps.msf.BusinessObjects.ExecuteJPO.args;
import matrix.db.*;
import matrix.util.StringList;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.text.StringEscapeUtils;
import org.apache.kafka.common.protocol.types.Field;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.io.File;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static com.matrixone.apps.domain.DomainConstants.SELECT_CURRENT;
import static com.matrixone.apps.domain.DomainConstants.SELECT_OWNER;

public class JF_PublicProjectQuery_mxJPO {

    private static final Logger JF_LOGGER = LoggerFactory.getLogger(JF_PublicProjectQuery_mxJPO.class);
    private static JF_Util_mxJPO jfUtilMxJPO = new JF_Util_mxJPO();
    private static final StringList strBusSelectsList = new StringList();
    static {
        strBusSelectsList.add("attribute[EnterpriseExtension.V_PartNumber]");
        strBusSelectsList.add("revision");
        strBusSelectsList.add("type");
        strBusSelectsList.add("name");
    }
    /**
     * 获取table展示的信息  JF_ProjectInfoTable
     */

    public MapList getVpmRefAndProjectInformation(Context context, String[] args) throws Exception {
        MapList mapList = new MapList();
        MapList mapListVPM = new MapList();
        MapList mapListFlexible = new MapList();
        StringList attrList = new StringList();
        attrList.add("attribute[EnterpriseExtension.V_PartNumber]");
        attrList.add("revision");
        ContextUtil.pushContext(context);
        try {
            Map argsMap = JPO.unpackArgs(args);
            String vpmId = UIUtil.getValue(argsMap, "objectId");
            JF_LOGGER.info("vpmId:{}", vpmId);
            DomainObject vpmObj = DomainObject.newInstance(context, vpmId);
            getVpmRefAndProjectInformationCommonByWholeChair(context,vpmId,attrList,mapListVPM,"直接引用");
            String FlexibleId = vpmObj.getInfo(context, "from[JFOriginalPart2Flexible].to.id");
            JF_LOGGER.info("FlexibleId:{}", FlexibleId);
            if (UIUtil.isNotNullAndNotEmpty(FlexibleId)){
                getVpmRefAndProjectInformationCommonByWholeChair(context,FlexibleId,attrList,mapListFlexible,"引用变形件");
            }
            mapList.addAll(mapListVPM);
            mapList.addAll(mapListFlexible);
            JF_LOGGER.info("JF_PublicProjectQuery-----getVpmRefAndProjectInformation---mapListVPM-----:{}", JSON.toJSONString(mapListVPM));
            JF_LOGGER.info("JF_PublicProjectQuery-----getVpmRefAndProjectInformation---mapListFlexible-----:{}", JSON.toJSONString(mapListFlexible));
            JF_LOGGER.info("JF_PublicProjectQuery-----getVpmRefAndProjectInformation---mapList-----:{}", JSON.toJSONString(mapList));
        }catch (Exception e){
            JF_LOGGER.info("JF_PublicProjectQuery------getVpmRefAndProjectInformation error",e);
        }finally {
            ContextUtil.popContext(context);
        }
        return mapList;
    }

    /**
     * 公共方法 查询整椅
     * @param context
     * @param vpmId
     * @param attrList
     * @param mapListVPM
     * @param referenceType
     */
    public void getVpmRefAndProjectInformationCommonByWholeChair(Context context, String vpmId, StringList attrList, MapList mapListVPM, String referenceType) throws Exception{
        //获取整椅id
        StringList wholeChairIds = jfUtilMxJPO.getWholeChair(context, new String[]{vpmId});
        JF_LOGGER.info("JF_PublicProjectQuery-----getVpmRefAndProjectInformationCommonByWholeChair---wholeChairIds-----:{}", JSON.toJSONString(wholeChairIds));
        //所有整椅的项目的id
        List<String> projectIds = wholeChairIds.stream()
                .flatMap(m -> {
                    try {
                        DomainObject parentObj = DomainObject.newInstance(context, m);
                        StringList ids = parentObj.getInfoList(context,"to[JFProject2RootPart].from.id");
                        return ids.stream(); // 将 List 转换为 Stream
                    } catch (FrameworkException e) {
                        e.printStackTrace();
                        return Stream.empty(); // 在异常情况下返回一个空的 Stream
                    }
                })
                .filter(id -> !id.isEmpty()) // 过滤掉空值
                .distinct()
                .collect(Collectors.toList());
        JF_LOGGER.info("JF_PublicProjectQuery-----getVpmRefAndProjectInformationCommonByWholeChair---projectIds-----:{}", JSON.toJSONString(projectIds));
        for (String projectId : projectIds) {
            Map projectMap = new HashMap();
            DomainObject projectObj = DomainObject.newInstance(context, projectId);
            String projectDescription= projectObj.getInfo(context, "description");
            String projectChairManager = MqlUtil.mqlCommand(context,false,"pri bus "+projectId+" select from[Member|attribute[Project Role]=='Chair manager'].to.attribute[First Name] dump",true);
            String projectProjectManager = MqlUtil.mqlCommand(context,false,"pri bus "+projectId+" select from[Member|attribute[Project Role]=='Project manager'].to.attribute[First Name] dump",true);
            projectMap.put("ProjectName",projectDescription);
            projectMap.put("ChairManager",projectChairManager);
            projectMap.put("ProjectManager",projectProjectManager);
            projectMap.put("ParentID","");
            projectMap.put("ReferenceType",referenceType);
            projectMap.put("id",projectId);//用来做判断的中转变量
            mapListVPM.add(projectMap);
        }
        //修改里面的父级编号
        for (String parentOneVpmId : wholeChairIds) {
            DomainObject parentOneVpmObj = DomainObject.newInstance(context, parentOneVpmId);
            Map parentOneVpmInfo = parentOneVpmObj.getInfo(context, attrList);
            String nameAndRevision = UIUtil.getValue(parentOneVpmInfo,"attribute[EnterpriseExtension.V_PartNumber]")+" "
                    +UIUtil.getValue(parentOneVpmInfo,"revision");
            StringList parentOneVpmProjectIds = parentOneVpmObj.getInfoList(context, "to[JFProject2RootPart].from.id");
            for (Object o : mapListVPM) {
                Map map = (Map) o;
                String id = UIUtil.getValue(map, "id");
                String ParentID = UIUtil.getValue(map, "ParentID");
                if (parentOneVpmProjectIds.contains(id)){
                    if (UIUtil.isNotNullAndNotEmpty(ParentID)){
                        ParentID = ParentID+"<br/>"+nameAndRevision;
                    }else {
                        ParentID = nameAndRevision;
                    }
                    map.put("ParentID",ParentID);
                }
            }
        }
    }


    /**
     * 公共方法
     * @param context
     * @return
     * @throws Exception
     */
    public void getVpmRefAndProjectInformationCommon(Context context,DomainObject vpmObj,StringList attrList,MapList mapListVPM, String referenceType) throws Exception{
        StringList boSel = JF_Util_mxJPO.basicBolistSel();
        //直接父级
        MapList parentOneVpmList = vpmObj.getRelatedObjects(context,"VPMInstance","VPMReference",boSel,null,true,false,(short)1,"","",0);
        JF_LOGGER.info("JF_PublicProjectQuery-----getVpmRefAndProjectInformationCommon---parentOneVpmList-----:{}", JSON.toJSONString(parentOneVpmList));
        //去重后直接父级的id
        List<String> parentOneVpmIds = (List<String>)parentOneVpmList.stream().map(m->((Map) m).get("id")).distinct().collect(Collectors.toList());
        JF_LOGGER.info("JF_PublicProjectQuery-----getVpmRefAndProjectInformationCommon---parentOneVpmIds--distinct---:{}", JSON.toJSONString(parentOneVpmIds));
        //所有父级
        MapList parentALLVpmList = vpmObj.getRelatedObjects(context,"VPMInstance","VPMReference",boSel,null,true,false,(short)0,"","",0);
        JF_LOGGER.info("JF_PublicProjectQuery-----getVpmRefAndProjectInformationCommon---parentALLVpmList-----:{}", JSON.toJSONString(parentALLVpmList));
        //去重后所有父级的id
        List<String> parentVpmIds = (List<String>) parentALLVpmList.stream().map(map->((Map) map).get("id")).distinct().collect(Collectors.toList());
        JF_LOGGER.info("JF_PublicProjectQuery-----getVpmRefAndProjectInformationCommon---parentVpmIds--distinct---:{}", JSON.toJSONString(parentVpmIds));
        //所有项目的id
        /*List<String> projectIds = parentVpmIds.stream()
                .map(m -> {
                    try {
                        DomainObject parentObj = DomainObject.newInstance(context, m);
                        return parentObj.getInfo(context, "to[JFProject2RootPart].from.id");
                    } catch (FrameworkException e) {
                        e.printStackTrace();
                        return null; // 在异常情况下返回 null
                    }
                })
                .filter(Objects::nonNull) // 过滤掉 null 值
                .filter(id -> !id.isEmpty())
                .distinct()
                .collect(Collectors.toList());*/
        List<String> projectIds = new ArrayList<>();
        for(int i=0;i<parentVpmIds.size();i++){
            DomainObject parentObj = DomainObject.newInstance(context, parentVpmIds.get(i));
            StringList list = parentObj.getInfoList(context, "to[JFProject2RootPart].from.id");
            for(int j=0;j<list.size();j++){
                if(!projectIds.contains(list.get(j))) {
                    projectIds.add(list.get(j));
                }
            }
        }


        JF_LOGGER.info("JF_PublicProjectQuery-----getVpmRefAndProjectInformationCommon---projectIds-----:{}", JSON.toJSONString(projectIds));
        for (String projectId : projectIds) {
            Map projectMap = new HashMap();
            DomainObject projectObj = DomainObject.newInstance(context, projectId);
            String projectDescription= projectObj.getInfo(context, "description");
            String projectChairManager = MqlUtil.mqlCommand(context,false,"pri bus "+projectId+" select from[Member|attribute[Project Role]=='Chair manager'].to.attribute[First Name] dump",true);
            String projectProjectManager = MqlUtil.mqlCommand(context,false,"pri bus "+projectId+" select from[Member|attribute[Project Role]=='Project manager'].to.attribute[First Name] dump",true);
            projectMap.put("ProjectName",projectDescription);
            projectMap.put("ChairManager",projectChairManager);
            projectMap.put("ProjectManager",projectProjectManager);
            projectMap.put("ParentID","");
            projectMap.put("ReferenceType",referenceType);
            projectMap.put("id",projectId);//用来做判断的中转变量
            mapListVPM.add(projectMap);
        }
        //修改里面的父级编号
        for (String parentOneVpmId : parentOneVpmIds) {
            DomainObject parentOneVpmObj = DomainObject.newInstance(context, parentOneVpmId);
            Map parentOneVpmInfo = parentOneVpmObj.getInfo(context, attrList);
            String nameAndRevision = UIUtil.getValue(parentOneVpmInfo,"attribute[EnterpriseExtension.V_PartNumber]")+" "
                    +UIUtil.getValue(parentOneVpmInfo,"revision");
            StringList parentOneVpmProjectId = parentOneVpmObj.getInfoList(context, "to[JFProject2RootPart].from.id");
            for (Object o : mapListVPM) {
                Map map = (Map) o;
                String id = UIUtil.getValue(map, "id");
                String ParentID = UIUtil.getValue(map, "ParentID");
                if (parentOneVpmProjectId.contains(id)){
                    if (UIUtil.isNotNullAndNotEmpty(ParentID)){
                        ParentID = ParentID+"<br/>"+nameAndRevision;
                    }else {
                        ParentID = nameAndRevision;
                    }
                    map.put("ParentID",ParentID);
                }
            }
        }
    }

    /**
     * table列的展示方法
     * @param context
     * @param args
     * @return
     * @throws Exception
     */
    public Vector getPartName(Context context, String[] args)
            throws Exception {
        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        Map columnMap = (Map) programMap.get("columnMap");
        String columnName = (String)columnMap.get("name");
        MapList objectList = (MapList) programMap.get("objectList");
        Vector retVector = new Vector();
        if (objectList != null && objectList.size() > 0) {
            Map objectMap = null;
            DomainObject domainObject = DomainObject.newInstance(context);
            String displayName = "";
            for (int i = 0; i < objectList.size(); i++) {
                objectMap  = (Map)objectList.get(i);
                displayName = (String)objectMap.get(columnName);
                retVector.add(displayName);
            }
        }
        return retVector;
    }


    /**
     *   1. to[XCADBaseDependency|from.type==Drawing  XCADBaseDependency该关系的from端不一定是图纸，
     *      所以需要加上类型判断---后续所有的获取都需要加上类型判断
     *   2. 获取的逻辑是物理产品和二维图纸的关系+物理产品和文档关系并且文档的属性JF_DocumentType为Drawing
     *   3. 如果列表的对象是Drawing，前面的勾选默认不可选 返回值设置 disableSelection true
     * @param context
     * @param args
     * @return
     * @throws Exception
     */
    public MapList getAllReferenceDrawing(Context context, String[] args) throws Exception{
        MapList retMapList = new MapList();
        Map map = JPO.unpackArgs(args);
        String objectId = (String) map.get("objectId");
        DomainObject domainObject = DomainObject.newInstance(context, objectId);
        MapList documentMapList = domainObject.getRelatedObjects(context, "Reference Document", "Document", StringList.create("id"), new StringList("id[connection]"),
                        false, true, (short) 1, "attribute[JF_DocumentType].value==Drawing", "", 0);
        retMapList.addAll(documentMapList);
        MapList drawingMapList = domainObject.getRelatedObjects(context, "XCADBaseDependency", "Drawing", StringList.create("id"), new StringList("id[connection]"),
                true, false, (short) 1, "", "", 0);
        retMapList.addAll(drawingMapList);
        return retMapList;
    }

    /**
     * 下载二维图纸
     * @param context
     * @param args
     * @return
     * @throws Exception
     */
    public Map downloadTwoDimensionalDrawing(Context context, String[] args) throws Exception {
        String strOwner = context.getUser();
        Map<String, Object> map = new HashMap<>();
        String objectIds = args[0];
        String objectId = args[1];
        StringList objectIdList = new StringList(objectIds.split(","));
        objectIdList = objectIdList.stream().distinct().collect(Collectors.toCollection(StringList::new));
        String workspace = context.createWorkspace();
        StringList fileList = new StringList();
        ContextUtil.pushContext(context);
        Map info = DomainObject.newInstance(context, objectId).getInfo(context, strBusSelectsList);
        String rootRevision = UIUtil.getValue(info, "revision");
        String type = UIUtil.getValue(info, "type");
        String name = UIUtil.getValue(info, "name");
        String rootV_PartNumber = UIUtil.getValue(info, "attribute[EnterpriseExtension.V_PartNumber]");
        String rootFileName = "";
        if ("VPMReference".equals(type)){
            rootFileName = rootV_PartNumber+"_"+rootRevision;
        }else {
            rootFileName = name+"_"+rootRevision;
        }

        // 创建一个ZIP文件输出流
        String zipFileName = workspace + File.separator + rootFileName+".zip";
        try (ZipOutputStream zipOut = new ZipOutputStream(new FileOutputStream(zipFileName))) {

            for (String s : objectIdList) {
                DomainObject domainObject = DomainObject.newInstance(context, s);
                Map<String, String> attrMap = domainObject.getInfo(context, strBusSelectsList);
                String revision = UIUtil.getValue(attrMap, "revision");
                String V_PartNumber = UIUtil.getValue(attrMap, "attribute[EnterpriseExtension.V_PartNumber]");

                // 创建一个子文件夹以存储当前对象的文件
                String subFolderName = V_PartNumber + "_" + revision; // 只使用相对路径
                new File(workspace, subFolderName).mkdirs();
                JF_LOGGER.info("JF_PublicProjectQuery------downloadTwoDimensionalDrawing----subFolderName::{}", subFolderName);

                MapList documentMapList = domainObject.getRelatedObjects(context, "Reference Document", "Document",
                        StringList.create("id"), new StringList(), false, true, (short) 1, "attribute[JF_DocumentType].value==Drawing", "", 0);
                MapList drawingMapList = domainObject.getRelatedObjects(context, "XCADBaseDependency", "Drawing",
                        StringList.create("id","physicalid"), new StringList(), true, false, (short) 1, "", "", 0);

                for (Object o : drawingMapList) {
                    StringList strWaterMarkList = new StringList();
                    Map<String, String> map1 = (Map<String, String>) o;
                    String id = UIUtil.getValue(map1, "id");
                    String physicalid = UIUtil.getValue(map1, "physicalid");
                    DomainObject drawingObject = DomainObject.newInstance(context, id);
                    String pdfId = drawingObject.getInfo(context, "from[DerivedOutputRelationship].to.id");
                    if(UIUtil.isNotNullAndNotEmpty(pdfId)) {
                        DomainObject pdfObject = DomainObject.newInstance(context, pdfId);
                        StringList fileNameList = jfUtilMxJPO.getDerivedOutputFileListName(context, new String[]{physicalid});
                        JF_LOGGER.info("JF_PublicProjectQuery------fileNameList ：{}", fileNameList);
                        for (String fileName : fileNameList) {
                            // 下载pdf文件到当前子文件夹
                            jfUtilMxJPO.checkOutFile(context, pdfObject, fileName, workspace + File.separator + subFolderName);
                            strWaterMarkList.add(workspace + File.separator + subFolderName + File.separator + fileName);
                            // 加水印
                            JF_WaterMarkUtils_mxJPO.addFilesWaterMark(context, strWaterMarkList, strOwner);
                            // 将文件添加到ZIP中，使用相对路径
                            addFileToZip(zipOut, workspace + File.separator + subFolderName + File.separator + fileName, subFolderName + "/" + fileName);
                        }
                    }
                }

                for (Object o : documentMapList) {
                    Map<String, String> map1 = (Map<String, String>) o;
                    String id = UIUtil.getValue(map1, "id");
                    StringList fileNameList = checkOutFileFromObject(context, workspace + File.separator + subFolderName, id);

                    for (String s1 : fileNameList) {
                        StringList strWaterMarkList = new StringList();
                        String filePath = workspace + File.separator + subFolderName + File.separator + s1;
                        if (s1.endsWith("pdf")) {
                            strWaterMarkList.add(filePath);
                        }
                        // 加水印
                        JF_WaterMarkUtils_mxJPO.addFilesWaterMark(context, strWaterMarkList, strOwner);
                        // 将文件添加到ZIP中，使用相对路径
                        addFileToZip(zipOut, filePath, subFolderName + "/" + s1);
                    }
                }
            }

            map.put("file", new File(zipFileName));
            map.put("flag", "Y");
            map.put("fileName", JF_PublicMethodClass_mxJPO.buildStringInStrings(rootFileName, ".zip"));
        } catch (Exception e) {
            map.put("flag", "N");
            JF_LOGGER.info("JF_PublicProjectQuery------downloadTwoDimensionalDrawing error", e);
        } finally {
            ContextUtil.popContext(context);
        }

        return map;
    }

    private StringList getDerivedOutputFileNameList(Context context, String physicalid) throws Exception{
        StringList retList = new StringList();
        ENODerivedOutputStreamInfoService var13 = new ENODerivedOutputStreamInfoService(context);
        StringList idlist = new StringList();
        idlist.add("73A18B56F8491F0068415BBB00000055");//图纸的物理ID
        StringList attrList = new StringList();
        attrList.add("filename");
        attrList.add("format");
        attrList.add("isSync");
        attrList.add("downloadable");
        attrList.add("visible");
        attrList.add("parameters");
        attrList.add("isExternal");
        attrList.add("deletable");
        attrList.add("title");
        Map mapDerived = var13.getDerivedOutputInformation(idlist, attrList, true);
        List<ENODOStream> list = (List<ENODOStream>) mapDerived.get(physicalid);
        JF_LOGGER.info("JF_PublicProjectQuery------list ：{}", list);
        for (ENODOStream enodoStream : list) {
            String name = enodoStream.getName();
            boolean synchroStatus = enodoStream.getSynchroStatus();
            JF_LOGGER.info("JF_PublicProjectQuery------name ：{}", name);
            JF_LOGGER.info("JF_PublicProjectQuery------synchroStatus ：{}", synchroStatus);
            if (synchroStatus){
                retList.add(name);
            }
        }
        return retList;
    }



    public void testAddWater(Context context, String[] args) throws Exception {
//        String id = args[0];
//        String workspace = context.createWorkspace();
//        System.out.println("workspace:::"+workspace);
//        StringList strWaterMarkList = new StringList();
//        DomainObject drawingObject = DomainObject.newInstance(context, id);
        ENODerivedOutputStreamInfoService var13 = new ENODerivedOutputStreamInfoService(context);
        StringList idlist = new StringList();
        idlist.add("73A18B56F8491F0068415BBB00000055");//图纸的物理ID
        StringList attrList = new StringList();
        attrList.add("filename");
        attrList.add("format");
        attrList.add("isSync");
        attrList.add("downloadable");
        attrList.add("visible");
        attrList.add("parameters");
        attrList.add("isExternal");
        attrList.add("deletable");
        attrList.add("title");
        Map var9 = var13.getDerivedOutputInformation(idlist, attrList, true);
        JF_LOGGER.info("var9===={}::",JSON.toJSONString(var9));
        jfUtilMxJPO.getDerivedOutputFileListName(context,args);
    }



    private void addFileToZip(ZipOutputStream zipOut, String filePath, String fileName) throws IOException {
        File fileToZip = new File(filePath);
        try (FileInputStream fis = new FileInputStream(fileToZip)) {
            ZipEntry zipEntry = new ZipEntry(fileName);
            zipOut.putNextEntry(zipEntry);
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                zipOut.write(buffer, 0, bytesRead);
            }
            zipOut.closeEntry();
        }
    }

    public static StringList checkOutFileFromObject(Context context, String outPath, String objectId) throws Exception {
        StringList fileNameList =  new StringList();
        try {
            DomainObject obj = new DomainObject(objectId);
            FileList fileList = obj.getFiles(context);
            for (int i = 0; i < fileList.size(); i++) {
                matrix.db.File f = (matrix.db.File) fileList.get(i);
                String format = f.getFormat();
                String fname = f.getName();
                obj.open(context);
                obj.checkoutFile(context, false, format, fname, outPath);
                obj.close(context);
                fileNameList.add(fname);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
        return fileNameList;
    }

    private static void addToZipFile(String fileName, ZipOutputStream zos) throws IOException {
        File file = new File(fileName);
        try (FileInputStream fis = new FileInputStream(file)) {
            ZipEntry zipEntry = new ZipEntry(file.getName());
            zos.putNextEntry(zipEntry);
            byte[] bytes = new byte[1024];
            int length;
            while ((length = fis.read(bytes)) >= 0) {
                zos.write(bytes, 0, length);
            }
            zos.closeEntry();
        }
    }


    /**
     * 获取ebom里面附件的文件 attribute[JF_DocumentType].value=！Drawing
     * @param context
     * @param args
     * @return
     * @throws Exception
     */
    public MapList getRelatedNotDrawingDocument(Context context, String[] args) throws Exception{
        Map map = JPO.unpackArgs(args);
        String objectId = (String) map.get("objectId");
        DomainObject domainObject = DomainObject.newInstance(context, objectId);
        MapList documentMapList = domainObject.getRelatedObjects(context, "Reference Document", "Document", StringList.create("id"), new StringList(),
                false, true, (short) 1, "attribute[JF_DocumentType].value!=Drawing", "", 0);
        return documentMapList;
    }


    /**
    * 交付物上传创建文件和关联文件
    * @param context
	* @param args
    * @author LIUJR
    * @throws
    * @return java.util.Map
    * @date 2025/10/11 10:18
    * @description
    */
    public static Map uploadFile(Context context, String[] args) throws Exception {
        JF_LOGGER.info("--------------------- uploadFile begin--------------------------");
        Map<String, String> res = new HashMap<>();
        StringBuffer sbDocDown = new StringBuffer();
        try {
            Map paramMap = (Map) JPO.unpackArgs(args);
            List files = (List) paramMap.get("files");
            String strObjectId = (String) paramMap.get("routeId");
            String strProjectDocTypeNameOID = (String) paramMap.get("JF_ProjectDocTypeNameOID");
//            String partListId = (String) paramMap.get("objectId");
            String partListId = (String) paramMap.get("partListId");
            ContextUtil.startTransaction(context, true);
            JF_LOGGER.info("strObjectId:{}", strObjectId);
            JF_LOGGER.info("partListId:{}", partListId);
            JF_LOGGER.info("paramMap:{}",paramMap);
            String sOSName = System.getProperty("os.name");
            String sFolder = sOSName.contains("Windows") ? JF_ECRRESTService_mxJPO.FolderWIN : JF_ECRRESTService_mxJPO.FolderUNIX;
            String separator = sOSName.contains("Windows") ? "\\" : "/";
            String sTmpDir = Environment.getValue(context, "TMPDIR");
            if (null != sTmpDir && !sTmpDir.trim().isEmpty()) {
                sFolder = sTmpDir;
                if (!sFolder.substring(sFolder.length() - 1).equals(separator))
                    sFolder = sFolder + separator;
            }
            Iterator iter = files.iterator();
            int index;
            String sFilename = "";
            FileItem  file = null;
            java.io.File outfile = null;
            //文档id集合
            StringList oids = new StringList();
            //标题集合
            StringList titles = new StringList();
            //创建文档
            String sObjGeneratorName = UICache.getObjectGenerator(context, "type_Document", "");
            String sName = DomainObject.getAutoGeneratedName(context, sObjGeneratorName, "");
            String docPolicy = PropertyUtil.getSchemaProperty(EnoviaResourceBundle.getProperty(context, "emxFrameowrk.FileUpload.Default.Policy"));
            String docId  = DomainConstants.EMPTY_STRING;
            String storeFromBL = null;
            CommonDocument cDoc = new CommonDocument();
            if (UIUtil.isNotNullAndNotEmpty(docPolicy)) {
                Policy policy = new Policy(docPolicy);
                String revision = policy.getFirstInSequence(context);
                PropertyUtil.setRPEValue(context, "MX_ALLOW_POV_STAMPING", "true", false);
                cDoc.createObject(context, DomainObject.TYPE_DOCUMENT, sName, revision, docPolicy, context.getVault().getName());
                cDoc.setAttributeValue(context, "Title", sFilename);
                //cDoc.checkinFile(context, true, true, "", "generic", sFilename, sFolder);
                storeFromBL = DocumentUtil.getStoreFromBL(context, "Document");
                docId = cDoc.getId(context);
                oids.add(cDoc.getId(context));
            }
            while (iter.hasNext()) {
                file = (FileItem) iter.next();
                sFilename = file.getName();
                JF_LOGGER.info(sFilename);
                if (sFilename.contains("/")) {
                    index = sFilename.lastIndexOf("/");
                    sFilename = sFilename.substring(index);
                }
                if (sFilename.contains("\\")) {
                    index = sFilename.lastIndexOf("\\");
                    sFilename = sFilename.substring(index + 1);
                }
                outfile = new java.io.File(sFolder + sFilename);
                JF_LOGGER.info(outfile.getPath());
                file.write(outfile);

                FileInputStream fis = null;
                fis = new FileInputStream(outfile);
                int read = fis.read();
                JF_LOGGER.info("read:{}", read);
                if (UIUtil.isNotNullAndNotEmpty(docId)) {
                    cDoc.createVersion(context, sFilename, sFilename, null);
                    cDoc.checkinFile(context, true, true, "", "generic", storeFromBL, sFilename, sFolder);
                }
            }
            DomainObject domainObject = DomainObject.newInstance(context, strObjectId);
            String type = domainObject.getInfo(context, "type");
            String owner = domainObject.getInfo(context, "owner");
            JF_LOGGER.error("oids：",oids);
            if ("Task".equals(type)){
                documentPostProcessUpdate(context,paramMap,oids);
            }else if ("Project Space".equals(type) || "Workspace Vault".equals(type)){
                documentPostProcessUpdateForProject(context,paramMap,oids);
                if(UIUtil.isNotNullAndNotEmpty(partListId)){
                    DomainObject partListObj = DomainObject.newInstance(context, partListId);
                    if("JFPartList".equalsIgnoreCase(partListObj.getInfo(context, DomainConstants.SELECT_TYPE))){
                        //和partList建立关系 //不一定有权限
                        DomainRelationship.connect(context, partListObj, "Reference Document", true, oids.toStringArray());
                    }
                }
            }
            //文档入库
            //文档入库  Classified Item
            for (int i = 0; i < oids.size(); i++) {
                cDoc.setId(oids.get(i));
                cDoc.addFromObject(context, new RelationshipType(DomainRelationship.RELATIONSHIP_CLASSIFIED_ITEM), strProjectDocTypeNameOID);
            }
            ContextUtil.commitTransaction(context);
            res.put("code", "200");
        } catch (Exception e) {
            JF_LOGGER.error("uploadFile----error：",e);
            ContextUtil.abortTransaction(context);
            res.put("code", "400");
            res.put("message", e.getMessage());
        }
        JF_LOGGER.info("----res-----{}-",res);
        JF_LOGGER.info("--------------------- uploadFile end--------------------------");
        return res;
    }

    private static void documentPostProcessUpdateForProject(Context context, Map paramMap, StringList documentIds) throws Exception {
        String description = UIUtil.getValue(paramMap, "description");
        String JF_ConnProjectName = UIUtil.getValue(paramMap, "JF_ConnProjectName");
        String routeId = UIUtil.getValue(paramMap, "routeId");
        String JF_ProjectDocType = UIUtil.getValue(paramMap, "JF_ProjectDocType");
        String JF_DocSecurity = UIUtil.getValue(paramMap, "JF_DocSecurity");
        String JF_ConnProjectPhase = UIUtil.getValue(paramMap, "JF_ConnProjectPhase");
        String JF_DocFileFormatRequirements = UIUtil.getValue(paramMap, "JF_DocFileFormatRequirements");
//        String JF_CounterSign = UIUtil.getValue(paramMap, "JF_ProjectDocTypeNameOID");
//        String[] JF_split = JF_CounterSign.split(",");
        String JF_ProjectDoc = UIUtil.getValue(paramMap, "JF_ProjectDoc");
        String projectId = UIUtil.getValue(paramMap, "projectId");
        String title = UIUtil.getValue(paramMap, "title");
        //选择的文件夹id
        String selectFolderId = UIUtil.getValue(paramMap, "selectFolderId");
        String folderFlag = UIUtil.getValue(paramMap, "folderFlag");
        //分类
        String JF_DocSpecialty = UIUtil.getValue(paramMap, "JF_DocSpecialty");
        String strJF_DocSpecialty = JF_DocSpecialty;
        JF_DocSpecialty = coverSpecialty(JF_DocSpecialty);
        Map setMap = new HashMap();
        setMap.put("JF_ConnProjectName", JF_ConnProjectName);
        setMap.put("JF_ConnProjectId", projectId);
        setMap.put("JF_ProjectDocType", JF_ProjectDocType);
        setMap.put("JF_DocSecurity", JF_DocSecurity);
        setMap.put("JF_ConnProjectPhase", JF_ConnProjectPhase);
        setMap.put("JF_DocFileFormatRequirements", JF_DocFileFormatRequirements);
//        setMap.put("JF_CounterSign", JF_split[0]);//第一个是是否审批
//        setMap.put("JF_DocSpecialistReview", JF_split[1]);//第二个是专家审批
//        setMap.put("JF_DocReceiptConfirmation", JF_split[2]);//第二个是回执确认
        setMap.put("JF_ProjectDoc", JF_ProjectDoc);
        setMap.put("JF_DocSpecialty", strJF_DocSpecialty);//专业
        setMap.put("Title", title);
        DomainObject domainObject = DomainObject.newInstance(context, routeId);
        String type = domainObject.getInfo(context, "type");
        if ("Task".equals(type)) {
            routeId = getProjectIdByTaskId(context, routeId);
        }else if ("Workspace Vault".equals(type)){
            Map<String,String> topLevelVault = getProjectByVaultId(context, routeId);
            routeId = UIUtil.getValue(topLevelVault, "to[Data Vaults].from.id");
        }
        String owner = MqlUtil.mqlCommand(context, false, "pri bus " + routeId + " select owner dump", true);
        //这样获取有问题
//        String valutId = domainObject.getInfo(context,"from[Data Vaults|to.attribute[Title]=='研发技术文档'].to.id");
        String valutId = MqlUtil.mqlCommand(context, false, "pri bus " + routeId + " select from[Data Vaults|to.attribute[Title]=='研发技术文档'].to.id dump", true);
        JF_LOGGER.info("----documentPostProcessUpdateForProject---valutId--{}-", valutId);
        if ("true".equalsIgnoreCase(folderFlag)) {
            if (UIUtil.isNullOrEmpty(selectFolderId)) {
                if (UIUtil.isNotNullAndNotEmpty(valutId)) {
                    ContextUtil.pushContext(context, owner, "", "");
                    String subValutId = MqlUtil.mqlCommand(context, false, "pri bus " + valutId + " select from[Sub Vaults|to.attribute[Title]=='" + JF_ProjectDocType + "'].to.id dump", true);
                    JF_LOGGER.info("----documentPostProcessUpdateForProject---subValutId--{}-", subValutId);
                    if (UIUtil.isNullOrEmpty(subValutId)) {
                        //创建分类的子书签
                        subValutId = createSubFolder(context, valutId, JF_ProjectDocType);
                    }
                    DomainObject subValutObj = DomainObject.newInstance(context, subValutId);
                    subValutObj.setOwner(context, owner);
                    String current = subValutObj.getInfo(context, "current");
                    if ("Create".equals(current)) {
                        subValutObj.promote(context);
                    }
                    String subSpecialtyValutId = MqlUtil.mqlCommand(context, false, "pri bus " + subValutId + " select from[Sub Vaults|to.attribute[Title]=='" + JF_DocSpecialty + "'].to.id dump", true);
                    if (UIUtil.isNullOrEmpty(subSpecialtyValutId)) {
                        //创建分类的子书签
                        subSpecialtyValutId = createSubFolder(context, subValutId, JF_DocSpecialty);
                    }
                    DomainObject subSpecialtyValutObj = DomainObject.newInstance(context, subSpecialtyValutId);
                    subSpecialtyValutObj.setOwner(context, owner);
                    String currentSpecialty = subSpecialtyValutObj.getInfo(context, "current");
                    if ("Create".equals(currentSpecialty)) {
                        subSpecialtyValutObj.promote(context);
                    }
                    DomainRelationship.connect(context, subSpecialtyValutObj, "Vaulted Objects", true, documentIds.toStringArray());
                    ContextUtil.popContext(context);

                    ContextUtil.pushContext(context);
                    //更新文档属性
                    for (String documentId : documentIds) {
                        DomainObject documentObj = DomainObject.newInstance(context, documentId);
                        documentObj.setAttributeValues(context, setMap);
                        documentObj.setDescription(context, description);
                    }
                    ContextUtil.popContext(context);
                } else {
                    //去创建 研发技术文档文件夹书签
                    ContextUtil.pushContext(context, owner, "", "");
                    String folderId = createFolder(context, routeId);
                    JF_LOGGER.info("----documentPostProcessUpdateForProject---folderId--{}-", folderId);
                    DomainObject valutObj = DomainObject.newInstance(context, folderId);
                    valutObj.promote(context);
                    //创建分类的子书签
                    String subFolderId = createSubFolder(context, folderId, JF_ProjectDocType);
                    JF_LOGGER.info("----documentPostProcessUpdateForProject---subFolderId--{}-", subFolderId);
                    DomainObject subValutObj = DomainObject.newInstance(context, subFolderId);
                    subValutObj.promote(context);
                    //ootb创建就自动关联了 不需要再去手动关联了
//            DomainRelationship.connect(context,domainObject, "Data Vaults", valutObj);
                    //创建专业的子书签
                    String subSpecialtyFolderId = createSubFolder(context, subFolderId, JF_DocSpecialty);
                    JF_LOGGER.info("----documentPostProcessUpdateForProject---subSpecialtyFolderId--{}-", subSpecialtyFolderId);
                    DomainObject subSpecialtyValutObj = DomainObject.newInstance(context, subSpecialtyFolderId);
                    subSpecialtyValutObj.promote(context);
                    //书签和文档关联
                    DomainRelationship.connect(context, subSpecialtyValutObj, "Vaulted Objects", true, documentIds.toStringArray());
                    ContextUtil.popContext(context);

                    ContextUtil.pushContext(context);
                    //更新文档属性
                    for (String documentId : documentIds) {
                        DomainObject documentObj = DomainObject.newInstance(context, documentId);
                        documentObj.setAttributeValues(context, setMap);
                        documentObj.setDescription(context, description);
//                    if (UIUtil.isNotNullAndNotEmpty(personIds)) {
//                        String[] split = personIds.split(",");
//                        //文档和person关联
//                        DomainRelationship.connect(context, documentObj, "JFDoc2Countersign", true, split);
//                    }
                    }
                    ContextUtil.popContext(context);
                }
            } else {
                DomainObject selectFolderObj = DomainObject.newInstance(context, selectFolderId);
                //找到父级的id看是否是研发技术文档的id
                String parentVaultId = selectFolderObj.getInfo(context, "to[Sub Vaults].from.id");
                String parentParentVaultId = "";
                if (UIUtil.isNotNullAndNotEmpty(parentVaultId)) {
                    DomainObject parentObj = DomainObject.newInstance(context, parentVaultId);
                    //再找父一级
                    parentParentVaultId = parentObj.getInfo(context, "to[Sub Vaults].from.id");
                }
                if (UIUtil.isNotNullAndNotEmpty(valutId) && (Objects.equals(valutId, selectFolderId) || Objects.equals(parentVaultId, valutId) || Objects.equals(parentParentVaultId, valutId))) {
                    ContextUtil.pushContext(context, owner, "", "");
                    String subValutId = MqlUtil.mqlCommand(context, false, "pri bus " + valutId + " select from[Sub Vaults|to.attribute[Title]=='" + JF_ProjectDocType + "'].to.id dump", true);
                    JF_LOGGER.info("----documentPostProcessUpdateForProject---subValutId--{}-", subValutId);
                    if (UIUtil.isNullOrEmpty(subValutId)) {
                        //创建分类的子书签
                        subValutId = createSubFolder(context, valutId, JF_ProjectDocType);
                    }
                    DomainObject subValutObj = DomainObject.newInstance(context, subValutId);
                    subValutObj.setOwner(context, owner);
                    String current = subValutObj.getInfo(context, "current");
                    if ("Create".equals(current)) {
                        subValutObj.promote(context);
                    }

                    String subSpecialtyValutId = MqlUtil.mqlCommand(context, false, "pri bus " + subValutId + " select from[Sub Vaults|to.attribute[Title]=='" + JF_DocSpecialty + "'].to.id dump", true);
                    if (UIUtil.isNullOrEmpty(subSpecialtyValutId)) {
                        //创建分类的子书签
                        subSpecialtyValutId = createSubFolder(context, subValutId, JF_DocSpecialty);
                    }
                    DomainObject subSpecialtyValutObj = DomainObject.newInstance(context, subSpecialtyValutId);
                    subSpecialtyValutObj.setOwner(context, owner);
                    String currentSpecialty = subSpecialtyValutObj.getInfo(context, "current");
                    if ("Create".equals(currentSpecialty)) {
                        subSpecialtyValutObj.promote(context);
                    }
                    DomainRelationship.connect(context, subSpecialtyValutObj, "Vaulted Objects", true, documentIds.toStringArray());
                    ContextUtil.popContext(context);

                    ContextUtil.pushContext(context);
                    //更新文档属性
                    for (String documentId : documentIds) {
                        DomainObject documentObj = DomainObject.newInstance(context, documentId);
                        documentObj.setAttributeValues(context, setMap);
                        documentObj.setDescription(context, description);
                    }
                    ContextUtil.popContext(context);
                } else {
                    ContextUtil.pushContext(context, owner, "", "");
                    DomainRelationship.connect(context, selectFolderObj, "Vaulted Objects", true, documentIds.toStringArray());
                    ContextUtil.popContext(context);

                    ContextUtil.pushContext(context);
                    //更新文档属性
                    for (String documentId : documentIds) {
                        DomainObject documentObj = DomainObject.newInstance(context, documentId);
                        documentObj.setAttributeValues(context, setMap);
                        documentObj.setDescription(context, description);
                    }
                    ContextUtil.popContext(context);
                }
            }
        } else {
            ContextUtil.pushContext(context);
            //更新文档属性
            for (String documentId : documentIds) {
                DomainObject documentObj = DomainObject.newInstance(context, documentId);
                documentObj.setAttributeValues(context, setMap);
                documentObj.setDescription(context, description);
            }
            ContextUtil.popContext(context);
        }
    }

    private static Map<String,String> getProjectByVaultId(Context context, String vaultId) throws FrameworkException {
        com.dassault_systemes.enovia.workspace.modeler.WorkspaceVault workspaceVault =
                (com.dassault_systemes.enovia.workspace.modeler.WorkspaceVault) DomainObject.newInstance(context,
                        DomainConstants.TYPE_WORKSPACE_VAULT, DomainConstants.WORKSPACEMDL);
        workspaceVault.setId(vaultId);
        Map<String,String> topLevelVault = (Map<String, String>) workspaceVault.getTopLevelVault(context, StringList.create("id", "to[Data Vaults].from.id"));
        return topLevelVault;
    }

    public static String createFolder(Context context, String routeId) throws Exception{
        com.dassault_systemes.enovia.workspace.modeler.WorkspaceVault workspaceVault =
                (com.dassault_systemes.enovia.workspace.modeler.WorkspaceVault) DomainObject.newInstance(context,
                        DomainConstants.TYPE_WORKSPACE_VAULT, DomainConstants.WORKSPACEMDL);
        com.dassault_systemes.enovia.workspace.modeler.WorkspaceVault workspaceVaultSource =
                (com.dassault_systemes.enovia.workspace.modeler.WorkspaceVault) DomainObject.newInstance(context,
                        DomainConstants.TYPE_WORKSPACE_VAULT, DomainConstants.WORKSPACEMDL);
        Map returnMap = new HashMap();
        String relationship = DomainConstants.RELATIONSHIP_PROJECT_VAULTS;
        String selectedType = "Workspace Vault";
        String strFolderName = DomainObject.EMPTY_STRING;
        String strFolderTitle = "研发技术文档";
        String selectedPolicy = "Workspace";
        String strDescription = "";
        String inheritedAccessType= "Inherited";
        String numberOf = "1";
        int count = 1;
        try{
            count = Integer.parseInt(numberOf);
        }catch(Exception e){
            count = 1;
        }
        boolean isAutoNameCheck = true;
        String projectID 		= routeId;
        String strObjectId = routeId;

//        JsonObjectBuilder folderInfoBuilder = Json.createObjectBuilder();
        DomainObject parentObject 	= DomainObject.newInstance(context, strObjectId);
//        PMCWorkspaceVault folder = new PMCWorkspaceVault();
//        Map parentObjectInfoMap 	= parentObject.getInfo(context, new StringList(ProgramCentralConstants.SELECT_IS_WORKSPACE_VAULT));
//        String isWorkspaceVault 	= (String)parentObjectInfoMap.get(ProgramCentralConstants.SELECT_IS_WORKSPACE_VAULT);
//        folderInfoBuilder.add("autonameCheck", String.valueOf(isAutoNameCheck));
//        folderInfoBuilder.add("parentId", strObjectId);
//        folderInfoBuilder.add("folderType", selectedType);
//        folderInfoBuilder.add("folderToAdd", String.valueOf(count));
//        folderInfoBuilder.add("folderAccessType", inheritedAccessType);
//        folderInfoBuilder.add("isWorkspaceVault", isWorkspaceVault);
//        folderInfoBuilder.add("relationship", relationship);
//        folderInfoBuilder.add("rowId", "0");
//        folderInfoBuilder.add("policy", selectedPolicy);
//        folderInfoBuilder.add("folderTitle", strFolderTitle);
//        JsonObject folderInfo = folderInfoBuilder.build();
        boolean promoteToInWork = false;
        Map attributes = new HashMap();
        attributes.put(ProgramCentralConstants.ATTRIBUTE_TITLE, strFolderTitle);
        if (ProgramCentralUtil.isNotNullString(inheritedAccessType)) {
            attributes.put(DomainObject.ATTRIBUTE_ACCESS_TYPE, inheritedAccessType);
        }

        workspaceVault.create(context, selectedType, strFolderName, selectedPolicy, parentObject, attributes, strDescription, promoteToInWork);
        String sFolderId = workspaceVault.getObjectId();

        String projectOwner = ProgramCentralConstants.EMPTY_STRING;
        DomainObject projObject = DomainObject.newInstance(context, projectID);
        Map projectInfoMap = projObject.getInfo(context, new StringList(DomainConstants.SELECT_OWNER));
        projectOwner = (String)projectInfoMap.get(DomainConstants.SELECT_OWNER);
        workspaceVault.setId(sFolderId);

        //Create inherited ownership for Template folder
        boolean isTemplate = false;
        if(ProgramCentralUtil.isNotNullString(strObjectId)){
            DomainObject projectTemplate = DomainObject.newInstance(context, strObjectId);
            isTemplate=projectTemplate.isKindOf(context, DomainObject.TYPE_PROJECT_TEMPLATE);
            if(isTemplate){
                DomainAccess.createObjectOwnership(context, sFolderId, strObjectId, "");
                JF_LOGGER.info("====createFolder====AAAAAAAAA");
            }else{
                int accessMapSize=1;
                String loggedInUser = context.getUser();
                String defaultAccessGrantPermission = EnoviaResourceBundle.getProperty(context, "emxComponents.WSO_Default_AccessGrant");

                Map accessMap = new HashMap(accessMapSize);

                if(!"Specific".equals(inheritedAccessType)){
                    accessMap.put(loggedInUser, "Full");
                }

                if(("Specific".equalsIgnoreCase(inheritedAccessType)||"No".equalsIgnoreCase(inheritedAccessType))&&ProgramCentralUtil.isNotNullString(projectOwner)&&!(loggedInUser.equals(projectOwner))&&"true".equalsIgnoreCase(defaultAccessGrantPermission))
                {
                    accessMap.put(projectOwner, "Full");
                    workspaceVault.setUserPermissions(context, accessMap);
                    JF_LOGGER.info("====createFolder====BBBBBBBBBBBB");
                }


            }
        }
        return sFolderId;
    }


    public static String createSubFolder(Context context, String routeId, String strTitle) throws Exception{
        com.dassault_systemes.enovia.workspace.modeler.WorkspaceVault workspaceVault =
                (com.dassault_systemes.enovia.workspace.modeler.WorkspaceVault) DomainObject.newInstance(context,
                        DomainConstants.TYPE_WORKSPACE_VAULT, DomainConstants.WORKSPACEMDL);
        String selectedType = "Workspace Vault";
        String strFolderName = DomainObject.EMPTY_STRING;
        String strFolderTitle = strTitle;
        String selectedPolicy = "Workspace";
        String strDescription = "";
        String inheritedAccessType= "Inherited";
        String numberOf = "1";
        String projectID 		= routeId;
        String strObjectId = routeId;

//        DomainObject parentObject 	= DomainObject.newInstance(context, strObjectId);
        boolean promoteToInWork = false;
        Map attributes = new HashMap();
        attributes.put(ProgramCentralConstants.ATTRIBUTE_TITLE, strFolderTitle);
        if (ProgramCentralUtil.isNotNullString(inheritedAccessType)) {
            attributes.put(DomainObject.ATTRIBUTE_ACCESS_TYPE, inheritedAccessType);
        }
        workspaceVault.setId(strObjectId);
        workspaceVault = workspaceVault.createSubVault(context, selectedType, strFolderName, selectedPolicy, attributes, strDescription, promoteToInWork);
        String sFolderId = workspaceVault.getObjectId();

        String projectOwner = ProgramCentralConstants.EMPTY_STRING;
        DomainObject projObject = DomainObject.newInstance(context, projectID);
        Map projectInfoMap = projObject.getInfo(context, new StringList(DomainConstants.SELECT_OWNER));
        projectOwner = (String)projectInfoMap.get(DomainConstants.SELECT_OWNER);
        workspaceVault.setId(sFolderId);

        //Create inherited ownership for Template folder
        boolean isTemplate = false;
        if(ProgramCentralUtil.isNotNullString(strObjectId)){
            DomainObject projectTemplate = DomainObject.newInstance(context, strObjectId);
            isTemplate=projectTemplate.isKindOf(context, DomainObject.TYPE_PROJECT_TEMPLATE);
            if(isTemplate){
                DomainAccess.createObjectOwnership(context, sFolderId, strObjectId, "");
                JF_LOGGER.info("====createFolder====AAAAAAAAA");
            }else{
                int accessMapSize=1;
                String loggedInUser = context.getUser();
                String defaultAccessGrantPermission = EnoviaResourceBundle.getProperty(context, "emxComponents.WSO_Default_AccessGrant");

                Map accessMap = new HashMap(accessMapSize);

                if(!"Specific".equals(inheritedAccessType)){
                    accessMap.put(loggedInUser, "Full");
                }

                if(("Specific".equalsIgnoreCase(inheritedAccessType)||"No".equalsIgnoreCase(inheritedAccessType))&&ProgramCentralUtil.isNotNullString(projectOwner)&&!(loggedInUser.equals(projectOwner))&&"true".equalsIgnoreCase(defaultAccessGrantPermission))
                {
                    accessMap.put(projectOwner, "Full");
                    workspaceVault.setUserPermissions(context, accessMap);
                    JF_LOGGER.info("====createFolder====BBBBBBBBBBBB");
                }


            }
        }
        return sFolderId;
    }


    private static void documentPostProcessUpdate(Context context, Map paramMap, StringList documentIds) throws Exception{
            String routeId = UIUtil.getValue(paramMap,"routeId");
            DomainObject taskObject = DomainObject.newInstance(context, routeId);
            DomainRelationship.connect(context,taskObject, "Task Deliverable", true, documentIds.toStringArray());
            documentPostProcessUpdateForProject(context,paramMap,documentIds);
    }

    public static  String getProjectIdByTaskId(Context context,String taskId) throws Exception{
        StringList slBusSelects = new StringList(2);
        slBusSelects.add(DomainConstants.SELECT_NAME);
        slBusSelects.add(DomainConstants.SELECT_ID);
        com.matrixone.apps.program.Task taskObj = new com.matrixone.apps.program.Task();
        taskObj.setId(taskId);
        Map taskInfo = taskObj.getProject(context, slBusSelects);
        JF_LOGGER.info("====taskInfo===={}",taskInfo);
        return UIUtil.getValue(taskInfo,DomainConstants.SELECT_ID);
    }

    public static String coverSpecialty(String specialty) {
        String specialtyCover = "";
        if ("GC".equals(specialty)){
            specialtyCover = "整椅";
        }else if ("GM".equals(specialty)){
            specialtyCover = "骨架";
        }else if ("GU".equals(specialty)){
            specialtyCover = "发泡";
        }else if ("GT".equals(specialty)){
            specialtyCover = "面套";
        }else if ("GP".equals(specialty)){
            specialtyCover = "塑料件";
        }else if ("GK".equals(specialty)){
            specialtyCover = "功能件";
        }else if ("GE".equals(specialty)){
            specialtyCover = "电器件";
        }
        return specialtyCover;
    }

    /**
    * 根据人员获取显示的文件夹
    * @param context
	* @param args
    * @author LIUJR
    * @throws
    * @return java.lang.String
    * @date 2025/10/16 9:24
    * @description
    */
    public String getWorkspaceVault(Context context, String[]args) throws Exception {
        List<Map<String, String>> response = new ArrayList<>();
        try {
            String objectId = args[0];
            String connDepartmentId = args[1];
            DomainObject domainObject = DomainObject.newInstance(context, objectId);
            String type = domainObject.getInfo(context, "type");
            if (!DomainConstants.TYPE_PROJECT_SPACE.equalsIgnoreCase(type)) {
                String projectId = domainObject.getInfo(context, "to[Project Access Key].from.from[Project Access List].to.id");
                domainObject.setId(projectId);
            }
            JF_LOGGER.info("type：：：{}",type);
            String strFolderTitle = DomainConstants.EMPTY_STRING;
            String strRDCenterDepartmentId = JF_PublicMethodClass_mxJPO.getBasicUrl(context, new String[]{"JF_RDCenterDepartment.id"});
            String[] split = strRDCenterDepartmentId.split(",");
            strRDCenterDepartmentId = split[0];
            if (connDepartmentId.equalsIgnoreCase(strRDCenterDepartmentId)) {
                //是研发部  只能选择研发技术文档
                strFolderTitle = "研发技术文档";
            }
            MapList allPhase = domainObject.getRelatedObjects(
                    context,
                    "Data Vaults,Sub Vaults",
                    "Workspace Vault",
                    StringList.create("name","id","type", DomainConstants.SELECT_ATTRIBUTE_TITLE),
                    new StringList(),
                    false,
                    true,
                    (short)0,
                    "",
                    "",
                    0
            );
            JF_LOGGER.info("allPhase：：：{}",allPhase);
            for (Object o : allPhase) {
                Map m = (Map) o;
                Map<String, String> map = new HashMap<>();
                String title = UIUtil.getValue(m, DomainConstants.SELECT_ATTRIBUTE_TITLE);
                if (UIUtil.isNotNullAndNotEmpty(strFolderTitle)) {
                    //只有一个文件夹
                    if (strFolderTitle.equalsIgnoreCase(title)) {
                        map.put("name", title);
                        map.put("id", UIUtil.getValue(m, "id"));
                        response.add(map);
                        break;
                    }
                    continue;
                }
                map.put("name", title);
                map.put("id", UIUtil.getValue(m, "id"));
                response.add(map);
            }
        } catch (Exception e) {
            JF_LOGGER.error(e.getStackTrace().toString());
            throw e;
        }
        return com.alibaba.fastjson.JSONObject.toJSONString(response);
    }

    public String getTaskPhase(Context context, String[]args) throws Exception {
        List<Map<String, String>> response = new ArrayList<>();
        try {
            String objectId = args[0];
            DomainObject domainObject = DomainObject.newInstance(context, objectId);
            String type = domainObject.getInfo(context, "type");
            MapList allPhase = new MapList();
            if ("Task".equals(type)){
                allPhase = domainObject.getRelatedObjects(context,"Subtask","Phase,Task",StringList.create("name","id","type"),new StringList(),true,false,(short)0,"","",0);
            }else if ("Project Space".equals(type)){
                allPhase = domainObject.getRelatedObjects(context,"Subtask","Phase",StringList.create("name","id","type"),new StringList(),false,true,(short)1,"","",0);
            }else if ("Workspace Vault".equals(type)){
                Map<String,String> topLevelVault = getProjectByVaultId(context, objectId);
                String projectId = UIUtil.getValue(topLevelVault, "to[Data Vaults].from.id");
                DomainObject projectObject = DomainObject.newInstance(context, projectId);
                allPhase = projectObject.getRelatedObjects(context,"Subtask","Phase",StringList.create("name","id","type"),new StringList(),false,true,(short)1,"","",0);
            }
            JF_LOGGER.info("allPhase：：：{}",allPhase);
            for (Object o : allPhase) {
                Map m = (Map) o;
                String type1 = UIUtil.getValue(m, "type");
                if ("Phase".equals(type1)){
                    Map<String, String> map = new HashMap<>();
                    map.put("name", UIUtil.getValue(m, "name"));
                    map.put("id", UIUtil.getValue(m, "id"));
                    response.add(map);
                }
            }
        } catch (Exception e) {
            JF_LOGGER.error(e.getStackTrace().toString());
            throw e;
        }
        return com.alibaba.fastjson.JSONObject.toJSONString(response);
    }

    /**
     * 图纸下载按钮仅研发部门可见
     * @param context
     * @param args
     * @return
     * @throws Exception
     */
    public boolean getDrawingDownloadAccess(Context context, String[] args) throws Exception {
        String strLoginUser = context.getUser();
        String personId = PersonUtil.getPersonObjectID(context, strLoginUser);
        JF_LOGGER.info("getDrawingDownloadAccess---------personId：：：{}",personId);
        ContextUtil.pushContext(context);
        String development =  MqlUtil.mqlCommand(context, true, "print bus "+personId+" select to[Member|from.type==Department].from.id dump", false);
        ContextUtil.popContext(context);
        Properties prop = NioJDUtils.readPageObject(context, "JFJDConfig");
        String property = prop.getProperty("JF_RDCenterDepartment.id");
        String s = property.split(",")[0];
        if (development.contains(s)){
            return true;
        }else {
            return false;
        }
    }

    /**
     * 创建技术文档权限 如果是任务页面 则是owner和分派人
     *               如果是项目页面  则是项目成员
     * @param context
     * @param args
     * @return
     * @throws Exception
     */
    public boolean getTechnicalDocumentAccess(Context context, String[] args) throws Exception {
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
        Map info = task.getInfo(context, StringList.create("type", "owner", "current"));
        String type = UIUtil.getValue(info, "type");
        String owner = UIUtil.getValue(info, "owner");
        String current = UIUtil.getValue(info, "current");
        if ("Task".equals(type)) {
            StringList nameList = task.getInfoList(context, "to[Assigned Tasks].from.name");
            if ((Objects.equals(owner,strLoginUser) || nameList.contains(strLoginUser)) && !"Complete".equals(current)){
                return true;
            }else {
                return false;
            }
        }else if ("Project Space".equals(type)) {
            StringList nameList = task.getInfoList(context, "from[Member].to.name");
            if (Objects.equals(owner,strLoginUser) || nameList.contains(strLoginUser)){
                return true;
            }else {
                return false;
            }
        }else if ("Workspace Vault".equals(type)){
            Map<String,String> topLevelVault = getProjectByVaultId(context, strObjectId);
            String projectId = UIUtil.getValue(topLevelVault, "to[Data Vaults].from.id");
            DomainObject projectObj = DomainObject.newInstance(context, projectId);
            StringList nameList = projectObj.getInfoList(context, "from[Member].to.name");
            String projectOwner = projectObj.getInfo(context,"owner");
            if (Objects.equals(projectOwner,strLoginUser) || nameList.contains(strLoginUser)){
                return true;
            }else {
                return false;
            }
        }

        return false;
    }

    /**
     * 文档编辑页面的会签人员多选框
     * @param context
     * @param args
     * @return
     * @throws Exception
     */
    public String selectSignPerson(Context context,String[] args) throws Exception {
        StringBuilder sb = new StringBuilder();
        String addContributor = EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", context.getLocale(), "emxComponents.Common.SignatoriesAddPerson");
        String remove = EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", context.getLocale(), "emxComponents.Common.SignatoriesRemovePerson");
        String contributors = DomainObject.EMPTY_STRING;
        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        JF_LOGGER.info("selectSignPerson-----programMap:{}",programMap);
        HashMap requestMap = (HashMap) programMap.get("requestMap");
        String strMode = (String) requestMap.get("mode");
        JF_LOGGER.info("selectSignPerson-----strMode:{}",strMode);
        //mod 0912
        String documentId  = (String) requestMap.get("objectId");
        DomainObject documentObj = DomainObject.newInstance(context, documentId);
        String current=documentObj.getCurrentState(context).getName();
        if (UIUtil.isNotNullAndNotEmpty(strMode)) {
            //非冻结状态编辑页面
            if ("edit".equalsIgnoreCase(strMode)&&!"FROZEN".equalsIgnoreCase(current)) {
                StringList signPersons = documentObj.getInfoList(context,"from[JFDoc2Countersign].to.id");
                for (String signPerson : signPersons) {
                    if (contributors.length() ==0){
                        contributors+=signPerson;
                    }else {
                        contributors += "," + signPerson;
                    }
                }
//            } else if ("view".equalsIgnoreCase(strMode)) {
            } else {
//                documentId = (String) requestMap.get("objectId");
//                DomainObject documentObj = DomainObject.newInstance(context, documentId);
                StringList signPersons = documentObj.getInfoList(context,"from[JFDoc2Countersign].to.name");
                StringList persons = new StringList();
                for (String signPerson : signPersons) {
                    persons.add(PersonUtil.getFullName(context, signPerson));
                }
                return String.join(";",persons);
            }
        }else {
//            documentId = (String) requestMap.get("objectId");
//            DomainObject documentObj = DomainObject.newInstance(context, documentId);
            StringList signPersons = documentObj.getInfoList(context,"from[JFDoc2Countersign].to.name");
            StringList persons = new StringList();
            for (String signPerson : signPersons) {
                persons.add(PersonUtil.getFullName(context, signPerson));
            }
            return String.join(";",persons);
        }
        //XSSOK
        sb.append("<input type=\"hidden\" name=\"IsContributorFieldModified\" id=\"IsContributorFieldModified\" value=\"false\" readonly=\"readonly\" />");
        //XSSOK
        sb.append("<input type=\"hidden\" name=\"ContributorHidden\" id=\"ContributorHidden\" value=\""+contributors+"\" readonly=\"readonly\" />");

        sb.append("<table>");
        sb.append("<tr>");
        sb.append("<th rowspan=\"2\">");
        sb.append("<select name=\"Contributor\" style=\"width:200px\" multiple=\"multiple\">");
        if (UIUtil.isNotNullAndNotEmpty(documentId)){
//            DomainObject documentObj = DomainObject.newInstance(context, documentId);
            StringList signPersonNames = documentObj.getInfoList(context,"from[JFDoc2Countersign].to.name");
            for (String signPersonName : signPersonNames) {
                String id = PersonUtil.getPersonObject(context, signPersonName).getId(context);
                sb.append("<option value=\"" + id + "\" >");
                //XSSOK
                sb.append(PersonUtil.getFullName(context, signPersonName));
                sb.append("</option>");
            }
        }
        sb.append("</select>");
        sb.append("</th>");
        sb.append("<td>");
        sb.append("<a href=\"javascript:addDocumentPerson('ContributorHidden','Contributor','')\">");
        sb.append("<img src=\"../common/images/iconStatusAdded.gif\" width=\"12\" height=\"12\" border=\"0\" />");
        sb.append("</a>");
        sb.append("<a href=\"javascript:addDocumentPerson('ContributorHidden','Contributor','')\">");
        //XSSOK
        sb.append(addContributor);
        sb.append("</a>");
        //sb.append("</div>");
        sb.append("</td>");
        sb.append("</tr>");
        sb.append("<tr>");
        sb.append("<td>");
        sb.append("<a href=\"javascript:removeDocumentPerson('ContributorHidden','Contributor','IsContributorFieldModified')\">");
        sb.append("<img src=\"../common/images/iconStatusRemoved.gif\" width=\"12\" height=\"12\" border=\"0\" />");
        sb.append("</a>");
        sb.append("<a href=\"javascript:removeDocumentPerson('ContributorHidden','Contributor','IsContributorFieldModified')\">");
        //XSSOK
        sb.append(remove);
        sb.append("</a>");
        sb.append("</td>");
        sb.append("</tr>");
        sb.append("</table>");
        return sb.toString();
    }


    public String selectReviewSpecialist(Context context,String[] args) throws Exception {
        StringBuilder sb = new StringBuilder();
        String addContributor = EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", context.getLocale(), "emxComponents.Common.SignatoriesAddPerson");
        String remove = EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", context.getLocale(), "emxComponents.Common.SignatoriesRemovePerson");
        String contributors = DomainObject.EMPTY_STRING;
        String groupName=DomainObject.EMPTY_STRING;
        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        JF_LOGGER.info("selectSignPerson-----programMap:{}",programMap);
        HashMap requestMap = (HashMap) programMap.get("requestMap");
        String strMode = (String) requestMap.get("mode");
        //mod by 0912
        String documentId  = (String) requestMap.get("objectId");
        DomainObject documentObj = DomainObject.newInstance(context, documentId);
        String current=documentObj.getCurrentState(context).getName();
        if (UIUtil.isNotNullAndNotEmpty(strMode)) {
            if ("edit".equalsIgnoreCase(strMode)&&!"FROZEN".equalsIgnoreCase(current)) {
//                documentId = (String) requestMap.get("objectId");
//                DomainObject documentObj = DomainObject.newInstance(context, documentId);
                StringList signPersons = documentObj.getInfoList(context,"from[JFDoc2SpecialistReview].to.id");
                for (String signPerson : signPersons) {
                    if (contributors.length() ==0){
                        contributors+=signPerson;
                    }else {
                        contributors += "," + signPerson;
                    }
                }
//            } else if ("view".equalsIgnoreCase(strMode)) {
            } else  {
//                documentId = (String) requestMap.get("objectId");
//                DomainObject documentObj = DomainObject.newInstance(context, documentId);
                StringList signPersons = documentObj.getInfoList(context,"from[JFDoc2SpecialistReview].to.name");
                StringList persons = new StringList();
                for (String signPerson : signPersons) {
                    persons.add(PersonUtil.getFullName(context, signPerson));
                }
                return String.join(";",persons);
            }
        }else {
//            documentId = (String) requestMap.get("objectId");
//            DomainObject documentObj = DomainObject.newInstance(context, documentId);
            StringList signPersons = documentObj.getInfoList(context,"from[JFDoc2SpecialistReview].to.name");
            StringList persons = new StringList();
            for (String signPerson : signPersons) {
                persons.add(PersonUtil.getFullName(context, signPerson));
            }
            return String.join(";",persons);
        }
        DomainObject documentObject = DomainObject.newInstance(context, documentId);
        String jf_docSpecialistReview = documentObject.getAttributeValue(context, "JF_DocSpecialistReview");
        groupName= documentObject.getAttributeValue(context, "JF_DocSpecialistReviewGroup");
        //XSSOK
        sb.append("<input type=\"hidden\" name=\"IsFollowerFieldModified\" id=\"IsFollowerFieldModified\" value=\"false\" readonly=\"readonly\" />");
        //XSSOK
        sb.append("<input type=\"hidden\" name=\"FollowerHidden\" id=\"FollowerHidden\" value=\""+contributors+"\" readonly=\"readonly\" />");

        sb.append("<table>");
        sb.append("<tr>");
        sb.append("<th rowspan=\"2\">");
        sb.append("<select name=\"Follower\" style=\"width:200px\" multiple=\"multiple\">");
        if (UIUtil.isNotNullAndNotEmpty(documentId)){
//            DomainObject documentObj = DomainObject.newInstance(context, documentId);
            StringList signPersonNames = documentObj.getInfoList(context,"from[JFDoc2SpecialistReview].to.name");
            for (String signPersonName : signPersonNames) {
                String id = PersonUtil.getPersonObject(context, signPersonName).getId(context);
                sb.append("<option value=\"" + id + "\" >");
                //XSSOK
                sb.append(PersonUtil.getFullName(context, signPersonName));
                sb.append("</option>");
            }
        }
        sb.append("</select>");
        sb.append("</th>");
        sb.append("<td>");
        sb.append("<a href=\"javascript:addDocumentPerson('FollowerHidden','Follower','"+groupName+"')\">");
        sb.append("<img src=\"../common/images/iconStatusAdded.gif\" width=\"12\" height=\"12\" border=\"0\" />");
        sb.append("</a>");
        sb.append("<a href=\"javascript:addDocumentPerson('FollowerHidden','Follower','"+groupName+"')\">");
        //XSSOK
        sb.append(addContributor);
        sb.append("</a>");
        //sb.append("</div>");
        sb.append("</td>");
        sb.append("</tr>");
        sb.append("<tr>");
        sb.append("<td>");
        sb.append("<a href=\"javascript:removeDocumentPerson('FollowerHidden','Follower','IsFollowerFieldModified')\">");
        sb.append("<img src=\"../common/images/iconStatusRemoved.gif\" width=\"12\" height=\"12\" border=\"0\" />");
        sb.append("</a>");
        sb.append("<a href=\"javascript:removeDocumentPerson('FollowerHidden','Follower','IsFollowerFieldModified')\">");
        //XSSOK
        sb.append(remove);
        sb.append("</a>");
        sb.append("</td>");
        sb.append("</tr>");
        sb.append("</table>");
        sb.append("<script language=\"JavaScript\">");
        sb.append("function checkJSReviewSpecialistIsNotNull(){\n" +
                "var strJF_DocSpecialistReview = emxFormGetValue(\"JF_DocSpecialistReview\").current.actual;"+
                "            if (\"N\" === strJF_DocSpecialistReview){"+
                "return true;"+
                "}"+
                "        const strContributorHidden = document.getElementById(\"FollowerHidden\").value;\n" +
                "        if (strContributorHidden){\n" +
                "            return true;\n" +
                "        } else{" +
                "//拿到浏览器语言\n" +
                "    var language = navigator.language || navigator.userLanguage;\n" +
                "    var strMess = \"\";\n" +
                "    if (language.includes(\"zh\") || language.includes(\"zh-CN\")) {\n" +
                "        strMess = \"\u5fc5\u987b\u8f93\u5165\u6709\u6548\u503c\uff1a \u56de\u6267\u4e13\u5bb6\uff01\"" +
                "    }else {\n" +
                "        strMess = \"Valid value must be entered: Specialist Review\";\n" +
                "    }"+
                "alert(strMess)"+
                "}\n" +
                "        return false\n" +
                "    }");
        sb.append(" // 使用 addEventListener 添加第三个 onload 函数\n" +
                "    window.addEventListener('load', function thirdOnloadHandler() {\n" +
                "        console.log(\"Third onload handler called.\");\n" +
                "        document.getElementById('FollowerHidden').customValidate = checkJSReviewSpecialistIsNotNull\n"  +
                "    }, false);");
        sb.append(" </script>");
        if ("Y".equals(jf_docSpecialistReview)){
            sb.append("<script>");
            sb.append("  var  dom = document.getElementById('");
            String strDomId = "calc_ReviewSpecialist";
            sb.append(strDomId);
            sb.append("');");
            sb.append("  if (dom){");
            sb.append("    dom.querySelector('td:first-child').className = \"createLabelRequired\";");
            sb.append("  }");
            sb.append("</script>");
        }
        return sb.toString();
    }


    /**
     * 修改document Form字段
     * @param context
     * @param args
     * @throws Exception
     */
    public void updateDocumentFormField(Context context, String[] args) throws Exception {
        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        Map paramMap = (Map) programMap.get("paramMap");
        Map fieldMap = (Map) programMap.get("fieldMap");
        Map requestMap = (Map) programMap.get("requestMap");
        String strNewId = (String) paramMap.get("New OID");
        String strNewValue = (String) paramMap.get("New Value");
        String strFieldName = (String) fieldMap.get("name");
        String strObjectId = (String) paramMap.get("objectId");
        String[] followerHidden = (String[]) requestMap.get("FollowerHidden");
        String[] contributorHidden = (String[]) requestMap.get("ContributorHidden");
        String[] receiptConfirmationHidden = (String[]) requestMap.get("ReceiptConfirmationHidden");

        JF_LOGGER.info("strFieldName:{}", strFieldName);
        JF_LOGGER.info("strObjectId:{}", strObjectId);
//        JF_LOGGER.info("followerHidden:{}", followerHidden[0]);
//        JF_LOGGER.info("contributorHidden:{}", contributorHidden[0]);
        try {
            ContextUtil.pushContext(context);
            ContextUtil.startTransaction(context, true);
            DomainObject document = DomainObject.newInstance(context, strObjectId);
            if ("signPerson".equals(strFieldName)) {
                StringList objIds = document.getInfoList(context, "from[JFDoc2Countersign].to.id");
                String objStr = String.join(",",objIds);
                StringList relIds = document.getInfoList(context, "from[JFDoc2Countersign].id");
                //如果相同则直接return
                if (Objects.equals(objStr,contributorHidden[0])){
                    return;
                }
                if (UIUtil.isNullOrEmpty(contributorHidden[0])){
                    if (CollectionUtils.isNotEmpty(relIds)){
                        //断开原来关联的会签人员
                        DomainRelationship.disconnect(context,relIds.toStringArray());
                    }
                    return;
                }
                String[] split = contributorHidden[0].split(",");
                if (CollectionUtils.isNotEmpty(relIds)){
                    //断开原来关联的会签人员
                    DomainRelationship.disconnect(context,relIds.toStringArray());
                }
                //关联新的会签人员
                for (String s : split) {
                    document.addToObject(context,new RelationshipType("JFDoc2Countersign"),s);
                }
            }else if ("ReviewSpecialist".equals(strFieldName)){
                StringList objIds = document.getInfoList(context, "from[JFDoc2SpecialistReview].to.id");
                String objStr = String.join(",",objIds);
                StringList relIds = document.getInfoList(context, "from[JFDoc2SpecialistReview].id");
                //如果相同则直接return
                if (Objects.equals(objStr,followerHidden[0])){
                    return;
                }
                if (UIUtil.isNullOrEmpty(followerHidden[0])){
                    if (CollectionUtils.isNotEmpty(relIds)){
                        //断开原来关联的专家人员
                        DomainRelationship.disconnect(context,relIds.toStringArray());
                    }
                    return;
                }
                String[] split = followerHidden[0].split(",");
                if (CollectionUtils.isNotEmpty(relIds)){
                    //断开原来关联的专家人员
                    DomainRelationship.disconnect(context,relIds.toStringArray());
                }
                //关联新的专家人员
                for (String s : split) {
                    document.addToObject(context,new RelationshipType("JFDoc2SpecialistReview"),s);
                }
            }else if ("ReceiptConfirmationlist".equals(strFieldName)){
                //mod by 20250927 关联回执专家
                StringList objIds = document.getInfoList(context, "from[JFDoc2ReceiptConfirmation].to.id");
                String objStr = String.join(",",objIds);
                StringList relIds = document.getInfoList(context, "from[JFDoc2ReceiptConfirmation].id");
                //如果相同则直接return
                if (Objects.equals(objStr,receiptConfirmationHidden[0])){
                    return;
                }
                if (UIUtil.isNullOrEmpty(receiptConfirmationHidden[0])){
                    if (CollectionUtils.isNotEmpty(relIds)){
                        //断开原来关联的专家人员
                        DomainRelationship.disconnect(context,relIds.toStringArray());
                    }
                    return;
                }
                String[] split = receiptConfirmationHidden[0].split(",");
                if (CollectionUtils.isNotEmpty(relIds)){
                    //断开原来关联的专家人员
                    DomainRelationship.disconnect(context,relIds.toStringArray());
                }
                //关联新的专家人员
                for (String s : split) {
                    document.addToObject(context,new RelationshipType("JFDoc2ReceiptConfirmation"),s);
                }
            }
            ContextUtil.commitTransaction(context);
        } catch (Exception e) {
            ContextUtil.abortTransaction(context);
            JF_LOGGER.error(e.getMessage());
            throw e;
        } finally {
            ContextUtil.popContext(context);
        }

    }

    public boolean getSignPersonAndReviewSpecialistAccess(Context context,String[] args) throws Exception{
        HashMap hashMap = JPO.unpackArgs(args);
        JF_LOGGER.info("getSignPersonAndReviewSpecialistAccess----hashMap:{}",hashMap);
        String objectId = UIUtil.getValue(hashMap, "objectId");
        HashMap settings = (HashMap) hashMap.get("SETTINGS");
        String function = UIUtil.getValue(settings, "function");
        StringList strings = StringList.create("attribute[JF_DocSpecialistReview]","attribute[JF_CounterSign]","attribute[JF_DocSpecialistReviewGroup]");
        DomainObject domainObject = DomainObject.newInstance(context, objectId);
        Map info = domainObject.getInfo(context, strings);
        String JF_DocSpecialistReview = UIUtil.getValue(info,"attribute[JF_DocSpecialistReview]");
        String JF_CounterSign = UIUtil.getValue(info,"attribute[JF_CounterSign]");
        if ("selectSignPerson".equals(function)){
            if ("Y".equals(JF_CounterSign)){
                return true;
            }else {
                return false;
            }
            //mod by 20250927 回执专家组和审核专家权限一致
        }else if ("selectReviewSpecialist".equals(function)||"selectSpecialistReviewGroup".equals(function)){
            if ("Y".equals(JF_DocSpecialistReview)){
                return true;
            }else {
                return false;
            }
        }
        return false;
    }


    public String selectSpecialistReviewGroup(Context context,String[] args) throws Exception {
        StringBuilder sb = new StringBuilder();
        String addContributor = EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", context.getLocale(), "emxComponents.Common.SignatoriesAddPerson");
        String remove = EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", context.getLocale(), "emxComponents.Common.SignatoriesRemovePerson");
        String contributors = DomainObject.EMPTY_STRING;
        String groupName = DomainObject.EMPTY_STRING;
        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        JF_LOGGER.info("selectSpecialistReviewGroup-----programMap:{}",programMap);
        HashMap requestMap = (HashMap) programMap.get("requestMap");
        String strMode = (String) requestMap.get("mode");
        //mod by 0912
        String documentId  = (String) requestMap.get("objectId");
        DomainObject documentObj = DomainObject.newInstance(context, documentId);
        String current=documentObj.getCurrentState(context).getName();
        if (UIUtil.isNotNullAndNotEmpty(strMode)) {
            if ("edit".equalsIgnoreCase(strMode)&&!"FROZEN".equalsIgnoreCase(current)) {
//                documentId = (String) requestMap.get("objectId");
//                DomainObject documentObj = DomainObject.newInstance(context, documentId);
                StringList signPersons = documentObj.getInfoList(context,"from[JFDoc2ReceiptConfirmation].to.id");
                for (String signPerson : signPersons) {
                    if (contributors.length() ==0){
                        contributors+=signPerson;
                    }else {
                        contributors += "," + signPerson;
                    }
                }
//            } else if ("view".equalsIgnoreCase(strMode)) {
            } else  {
//                documentId = (String) requestMap.get("objectId");
//                DomainObject documentObj = DomainObject.newInstance(context, documentId);
                StringList signPersons = documentObj.getInfoList(context,"from[JFDoc2ReceiptConfirmation].to.name");
                StringList persons = new StringList();
                for (String signPerson : signPersons) {
                    persons.add(PersonUtil.getFullName(context, signPerson));
                }
                return String.join(";",persons);
            }
        }else {
//            documentId = (String) requestMap.get("objectId");
//            DomainObject documentObj = DomainObject.newInstance(context, documentId);
            StringList signPersons = documentObj.getInfoList(context,"from[JFDoc2ReceiptConfirmation].to.name");
            StringList persons = new StringList();
            for (String signPerson : signPersons) {
                persons.add(PersonUtil.getFullName(context, signPerson));
            }
            return String.join(";",persons);
        }
        DomainObject documentObject = DomainObject.newInstance(context, documentId);
        String jf_docSpecialistReview = documentObject.getAttributeValue(context, "JF_DocSpecialistReview");
        groupName= documentObject.getAttributeValue(context, "JF_DocSpecialistReviewGroup");
        //XSSOK
        sb.append("<input type=\"hidden\" name=\"IsGroupFieldModified\" id=\"IsGroupFieldModified\" value=\"false\" readonly=\"readonly\" />");
        //XSSOK
        sb.append("<input type=\"hidden\" name=\"ReceiptConfirmationHidden\" id=\"ReceiptConfirmationHidden\" value=\""+contributors+"\" readonly=\"readonly\" />");

        sb.append("<table>");
        sb.append("<tr>");
        sb.append("<th rowspan=\"2\">");
        sb.append("<select name=\"GroupFollower\" style=\"width:200px\" multiple=\"multiple\">");
        if (UIUtil.isNotNullAndNotEmpty(documentId)){
//            DomainObject documentObj = DomainObject.newInstance(context, documentId);
            StringList signPersonNames = documentObj.getInfoList(context,"from[JFDoc2ReceiptConfirmation].to.name");
            for (String signPersonName : signPersonNames) {
                String id = PersonUtil.getPersonObject(context, signPersonName).getId(context);
                sb.append("<option value=\"" + id + "\" >");
                //XSSOK
                sb.append(PersonUtil.getFullName(context, signPersonName));
                sb.append("</option>");
            }
        }
        sb.append("</select>");
        sb.append("</th>");
        sb.append("<td>");
        sb.append("<a href=\"javascript:addDocumentPerson('ReceiptConfirmationHidden','GroupFollower','"+groupName+"')\">");
        sb.append("<img src=\"../common/images/iconStatusAdded.gif\" width=\"12\" height=\"12\" border=\"0\" />");
        sb.append("</a>");
//        sb.append("<a href=\"javascript:addDocumentPerson('ReceiptConfirmationHidden','GroupFollower')\">");
        sb.append("<a href=\"javascript:addDocumentPerson('ReceiptConfirmationHidden','GroupFollower','"+groupName+"')\">");
        //XSSOK
        sb.append(addContributor);
        sb.append("</a>");
        //sb.append("</div>");
        sb.append("</td>");
        sb.append("</tr>");
        sb.append("<tr>");
        sb.append("<td>");
        sb.append("<a href=\"javascript:removeDocumentPerson('ReceiptConfirmationHidden','GroupFollower','IsGroupFieldModified')\">");
        sb.append("<img src=\"../common/images/iconStatusRemoved.gif\" width=\"12\" height=\"12\" border=\"0\" />");
        sb.append("</a>");
        sb.append("<a href=\"javascript:removeDocumentPerson('ReceiptConfirmationHidden','GroupFollower','IsGroupFieldModified')\">");
        //XSSOK
        sb.append(remove);
        sb.append("</a>");
        sb.append("</td>");
        sb.append("</tr>");
        sb.append("</table>");
        sb.append("<script language=\"JavaScript\">");
        sb.append("function checkJSGroupIsNotNull(){\n" +
                "var strJF_DocSpecialistReview = emxFormGetValue(\"JF_DocSpecialistReview\").current.actual;"+
                "            if (\"N\" === strJF_DocSpecialistReview){"+
                "return true;"+
                "}"+
                "        const strContributorHidden = document.getElementById(\"ReceiptConfirmationHidden\").value;\n" +
                "        if (strContributorHidden){\n" +
                "            return true;\n" +
                "        } else{" +
                "//拿到浏览器语言\n" +
                "    var language = navigator.language || navigator.userLanguage;\n" +
                "    var strMess = \"\";\n" +
                "    if (language.includes(\"zh\") || language.includes(\"zh-CN\")) {\n" +
                "        strMess = \"\u5fc5\u987b\u8f93\u5165\u6709\u6548\u503c\uff1a \u5ba1\u6838\u4e13\u5bb6\uff01\"" +
                "    }else {\n" +
                "        strMess = \"Valid value must be entered: Specialist Review\";\n" +
                "    }"+
                "alert(strMess)"+
                "}\n" +
                "        return false\n" +
                "    }");
        sb.append(" // 使用 addEventListener 添加第三个 onload 函数\n" +
                "    window.addEventListener('load', function thirdOnloadHandler() {\n" +
                "        console.log(\"Third onload handler called.\");\n" +
                "        document.getElementById('ReceiptConfirmationHidden').customValidate = checkJSGroupIsNotNull\n"  +
                "    }, false);");
        sb.append(" </script>");
        if ("Y".equals(jf_docSpecialistReview)){
            sb.append("<script>");
            sb.append("  var  dom = document.getElementById('");
            String strDomId = "calc_ReceiptConfirmationlist";
            sb.append(strDomId);
            sb.append("');");
            sb.append("  if (dom){");
            sb.append("    dom.querySelector('td:first-child').className = \"createLabelRequired\";");
            sb.append("  }");
            sb.append("</script>");
        }
        return sb.toString();
    }
}

